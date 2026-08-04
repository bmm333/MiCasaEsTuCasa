const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();

const db = admin.firestore();

/**
 * Sends a push notification to a user, swallowing errors so triggers don't retry forever.
 * @param {string} userId Firestore user document id
 * @param {{title: string, body: string, type: string, targetId: string}} payload
 * @return {Promise<string|null>} FCM message id or null
 */
async function sendPushToUser(userId, payload) {
    if (!userId) return null;
    try {
        const userDoc = await db.collection("users").doc(userId).get();
        const fcmToken = userDoc.data()?.fcmToken;
        if (!fcmToken) return null;
        return await admin.messaging().send({
            token: fcmToken,
            notification: { title: payload.title, body: payload.body },
            data: { type: payload.type, targetId: payload.targetId },
            android: { notification: { channelId: "micasa_notifications" } },
        });
    } catch (error) {
        console.error(`FCM send failed for user ${userId}:`, error);
        return null;
    }
}

/**
 * Trigger: each time that a booking is created in req status, sends a notification push
 * to the host of the property
 */
exports.onNewBookingRequest = functions.region("europe-west1").firestore
    .document("bookings/{bookingId}")
    .onCreate(async (snap, context) => {
        const booking = snap.data();
        if (!booking || booking.status !== "REQUESTED") return null;
        return sendPushToUser(booking.hostId, {
            title: "Nuova richiesta di prenotazione",
            body: "Hai ricevuto una nuova richiesta per la tua proprietà",
            type: "NEW_BOOKING_REQUEST",
            targetId: context.params.bookingId,
        });
    });

/**
 * Trigger: updated booking - notifies renter on accept, reject, or cancel
 */
exports.onBookingUpdate = functions.region("europe-west1").firestore
    .document("bookings/{bookingId}")
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();
        if (!before || !after || before.status === after.status) return null;

        let title;
        let body;
        let type;
        if (after.status === "ACCEPTED") {
            title = "Prenotazione accettata";
            body = "La tua richiesta di prenotazione è stata accettata";
            type = "BOOKING_ACCEPTED";
        } else if (after.status === "REJECTED") {
            title = "Prenotazione rifiutata";
            body = "La tua richiesta di prenotazione è stata rifiutata";
            type = "BOOKING_REJECTED";
        } else if (after.status === "CANCELLED") {
            title = "Prenotazione annullata";
            body = "Una prenotazione è stata annullata";
            type = "BOOKING_CANCELLED";
        } else {
            return null;
        }

        return sendPushToUser(after.renterId, {
            title,
            body,
            type,
            targetId: context.params.bookingId,
        });
    });

/**
 * trigger for new chat message notifies the recipient
 */
exports.onNewChatMessage = functions.region("europe-west1").firestore
    .document("conversations/{convId}/messages/{msgId}")
    .onCreate(async (snap, context) => {
        const message = snap.data();
        if (!message) return null;

        const convDoc = await db.collection("conversations")
            .doc(context.params.convId)
            .get();
        const conv = convDoc.data();
        if (!conv) return null;

        const receiverId = message.senderId === conv.hostId ?
            conv.renterId :
            conv.hostId;
        if (!receiverId || receiverId === message.senderId) return null;

        return sendPushToUser(receiverId, {
            title: "Nuovo messaggio",
            body: message.text || "Hai ricevuto un'immagine",
            type: "NEW_MESSAGE",
            targetId: context.params.convId,
        });
    });

/**
 * When a user's status changes to BANNED or SUSPENDED, puts all their
 * properties on hold and cancels active bookings. Reverses on reactivation.
 */
exports.onUserStatusChanged = functions.region("europe-west1").firestore
    .document("users/{userId}")
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();
        const userId = context.params.userId;

        if (!before || !after || before.status === after.status) return null;

        const batch = db.batch();
        const isBlocked = after.status === "BANNED" || after.status === "SUSPENDED";

        const propertiesSnap = await db.collection("properties")
            .where("ownerId", "==", userId)
            .get();

        propertiesSnap.docs.forEach((doc) => {
            batch.update(doc.ref, { isOnHold: isBlocked });
        });

        if (isBlocked) {
            const bookingsSnap = await db.collection("bookings")
                .where("hostId", "==", userId)
                .where("status", "in", ["REQUESTED", "ACCEPTED"])
                .get();

            bookingsSnap.docs.forEach((doc) => {
                batch.update(doc.ref, {
                    status: "CANCELLED",
                    cancellationReason: `Host account ${after.status.toLowerCase()}`,
                });
            });

            await admin.auth().updateUser(userId, { disabled: true }).catch(e => console.error("Error disabling user auth:", e));
        }

        if (after.status === "ACTIVE") {
            await admin.auth().updateUser(userId, { disabled: false }).catch(e => console.error("Error enabling user auth:", e));
            propertiesSnap.docs.forEach((doc) => {
                batch.update(doc.ref, { isOnHold: false });
            });
        }

        return batch.commit();
    });

/**
 * Trigger: quando un account viene eliminato da Firebase Auth
 * (dalla console, dall'app, o da un'altra Cloud Function),
 * elimina a cascata TUTTE le risorse associate all'utente.
 */
exports.onUserDeleted = functions.region("europe-west1").auth.user().onDelete(async (user) => {
    const db = admin.firestore();
    const storage = admin.storage().bucket();
    const userId = user.uid;

    console.log(`Starting cascade delete for user: ${userId}`);

    try {
        try {
            await storage.file(`profile_photos/${userId}.jpg`).delete();
            console.log(`Deleted profile photo for ${userId}`);
        } catch (e) {
            console.log(`No profile photo found for ${userId}`);
        }
        const propertiesSnap = await db.collection("properties")
            .where("ownerId", "==", userId)
            .get();

        for (const propertyDoc of propertiesSnap.docs) {
            const propertyId = propertyDoc.id;

            const activeBookings = await db.collection("bookings")
                .where("propertyId", "==", propertyId)
                .where("status", "in", ["REQUESTED", "ACCEPTED"])
                .get();

            const bookingBatch = db.batch();
            activeBookings.docs.forEach(doc => {
                bookingBatch.update(doc.ref, {
                    status: "CANCELLED",
                    cancellationReason: "Host account deleted"
                });
            });
            if (!activeBookings.empty) await bookingBatch.commit();

            try {
                const [files] = await storage.getFiles({
                    prefix: `properties/${propertyId}/`
                });
                await Promise.all(files.map(f => f.delete()));
            } catch (e) {
                console.log(`No storage files for property ${propertyId}`);
            }
        }

        const propertyBatch = db.batch();
        propertiesSnap.docs.forEach(doc => propertyBatch.delete(doc.ref));
        if (!propertiesSnap.empty) await propertyBatch.commit();

        console.log(`Deleted ${propertiesSnap.size} properties`);

        const renterBookingsSnap = await db.collection("bookings")
            .where("renterId", "==", userId)
            .get();

        const renterBookingBatch = db.batch();
        renterBookingsSnap.docs.forEach(doc => {
            const status = doc.data().status;
            if (status === "REQUESTED" || status === "ACCEPTED") {
                renterBookingBatch.update(doc.ref, {
                    status: "CANCELLED",
                    cancellationReason: "Renter account deleted"
                });
            }
        });
        if (!renterBookingsSnap.empty) await renterBookingBatch.commit();

        console.log(`Processed ${renterBookingsSnap.size} renter bookings`);

        const conversationsAsHostSnap = await db.collection("conversations")
            .where("hostId", "==", userId)
            .get();

        const conversationsAsRenterSnap = await db.collection("conversations")
            .where("renterId", "==", userId)
            .get();

        const allConversations = [
            ...conversationsAsHostSnap.docs,
            ...conversationsAsRenterSnap.docs
        ];

        for (const convDoc of allConversations) {
            const messagesSnap = await convDoc.ref.collection("messages").get();

            for (const msgDoc of messagesSnap.docs) {
                const imageUrl = msgDoc.data().imageUrl;
                if (imageUrl && imageUrl.includes("firebase")) {
                    try {
                        const filePath = decodeURIComponent(
                            imageUrl.split("/o/")[1].split("?")[0]
                        );
                        await storage.file(filePath).delete();
                    } catch (e) {
                        // non fatale
                    }
                }
            }

            const msgChunks = chunkArray(messagesSnap.docs, 500);
            for (const chunk of msgChunks) {
                const msgBatch = db.batch();
                chunk.forEach(doc => msgBatch.delete(doc.ref));
                await msgBatch.commit();
            }

            await convDoc.ref.delete();
        }

        console.log(`Deleted ${allConversations.length} conversations`);

        const reviewsSnap = await db.collection("reviews")
            .where("authorId", "==", userId)
            .get();

        const reviewBatch = db.batch();
        reviewsSnap.docs.forEach(doc => reviewBatch.delete(doc.ref));
        if (!reviewsSnap.empty) await reviewBatch.commit();

        console.log(`Deleted ${reviewsSnap.size} reviews`);

        const reportsSnap = await db.collection("reports")
            .where("reporterId", "==", userId)
            .get();

        const reportBatch = db.batch();
        reportsSnap.docs.forEach(doc => reportBatch.delete(doc.ref));
        if (!reportsSnap.empty) await reportBatch.commit();

        const notificationsSnap = await db
            .collection("users").doc(userId)
            .collection("notifications").get();

        const notifBatch = db.batch();
        notificationsSnap.docs.forEach(doc => notifBatch.delete(doc.ref));
        if (!notificationsSnap.empty) await notifBatch.commit();
        await db.collection("users").doc(userId).delete();
        const locksSnap = await db.collection("property_locks")
            .where("userId", "==", userId)
            .get();

        if (!locksSnap.empty) {
            const lockBatch = db.batch();
            locksSnap.docs.forEach(doc => lockBatch.delete(doc.ref));
            await lockBatch.commit();
        }

        console.log(`Cascade delete complete for user: ${userId}`);
        return null;
    } catch (error) {
        console.error(`Error during cascade delete for user ${userId}:`, error);
        throw error;
    }
});

// helper — spezza array in chunks da N elementi
function chunkArray(array, size) {
    const chunks = [];
    for (let i = 0; i < array.length; i += size) {
        chunks.push(array.slice(i, i + size));
    }
    return chunks;
}

/**
 * Scheduled Cloud Function.
 * Marks as COMPLETED all ACCEPTED bookings with endDate in the past.
 * Sends FCM notifications to both renter and host prompting them to leave a review.
 */
exports.completeExpiredBookings = functions.region("europe-west1").pubsub
    .schedule("0 0 * * *")
    .timeZone("Europe/Rome")
    .onRun(async () => {
        const today = new Date().toISOString().split("T")[0]; // "2026-07-31"

        const snap = await db
            .collection("bookings")
            .where("status", "==", "ACCEPTED")
            .where("endDate", "<", today)
            .get();

        if (snap.empty) {
            console.log("No bookings to complete");
            return null;
        }

        // batch update — max 500 per batch
        const chunks = chunkArray(snap.docs, 500);
        for (const chunk of chunks) {
            const batch = db.batch();
            chunk.forEach((doc) => {
                batch.update(doc.ref, {
                    status: "COMPLETED",
                    completedAt: admin.firestore.FieldValue.serverTimestamp(),
                });
            });
            await batch.commit();
        }

        console.log(`Completed ${snap.size} bookings`);

        // send FCM notifications to renter and host
        for (const doc of snap.docs) {
            const booking = doc.data();

            await sendPushToUser(booking.renterId, {
                title: "Soggiorno concluso!",
                body: "Come e andata? Lascia una recensione",
                type: "BOOKING_COMPLETED",
                targetId: doc.id,
            });

            await sendPushToUser(booking.hostId, {
                title: "Soggiorno concluso!",
                body: "Valuta il tuo ospite",
                type: "BOOKING_COMPLETED",
                targetId: doc.id,
            });
        }

        return null;
    });

/**
 * Aggregates property ratings on new/updated/deleted property review
 */
exports.onReviewWritten = functions.region("europe-west1").firestore
    .document("reviews/{reviewId}")
    .onWrite(async (change, context) => {
        const review = change.after.exists ? change.after.data() : change.before.data();
        if (!review || review.reviewType !== "PROPERTY_REVIEW" || !review.propertyId) {
            return null;
        }

        const propertyId = review.propertyId;
        const reviewsSnap = await db.collection("reviews")
            .where("propertyId", "==", propertyId)
            .where("reviewType", "==", "PROPERTY_REVIEW")
            .get();

        let totalRating = 0;
        let reviewsCount = reviewsSnap.size;

        if (reviewsCount > 0) {
            reviewsSnap.forEach(doc => {
                totalRating += (doc.data().stars || 0);
            });
        }

        const averageRating = reviewsCount > 0 ? (totalRating / reviewsCount) : 0;

        return db.collection("properties").doc(propertyId).update({
            rating: averageRating,
            reviewsCount: reviewsCount
        }).catch(err => {
            console.error(`Error updating rating for property ${propertyId}:`, err);
        });
    });
