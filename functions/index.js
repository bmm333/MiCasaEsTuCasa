const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Trigger: each time that a booking is created in req status, sends a notification push
 * to the host of the property
 */
exports.onNewBookingRequest = functions.firestore
    .document("bookings/{bookingId}")
    .onCreate(async (snap, context) => {
        const booking = snap.data();
        if (booking.status !== "REQUESTED") return null;
        // rec FCM token of the host
        const hostDoc = await admin.firestore()
            .collection("users")
            .doc(booking.hostId)
            .get();
        const fcmtoken = hostDoc.data()?.fcmToken;
        if (!fcmtoken) return null;
        return admin.messaging().send({
            token: fcmtoken,
            notification: {
                title: "New Booking Request",
                body: "You just recived a new booking request for your property",
            },
            data: {
                type: "NEW_BOOKING_REQUEST",
                targetId: context.params.bookingId,
            },
        });
    });

/**
 * Trigger: updated booking - notifies renter in both cases accepted or rejected
 */
exports.onBookingUpdate = functions.firestore
    .document("bookings/{bookingId}")
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();
        // notifies only on status change
        if (before.status === after.status) return null;
        const renterDoc = await admin.firestore()
            .collection("users")
            .doc(after.renterId)
            .get();

        const fcmtoken = renterDoc.data()?.fcmToken;
        if (!fcmtoken) return null;
        let title;
        let body;
        let type;
        if (after.status === "ACCEPTED") {
            title = "Booking Accepted";
            body = "Your booking request has been accepted";
            type = "BOOKING_ACCEPTED";
        } else if (after.status == "REJECTED") {
            title = "Booking Rejected";
            body = "Your booking request has been rejected";
            type = "BOOKING_REJECTED";
        }
        else if(after.status=="COMPLETED")
        {
            title="Booking Completed";
            body ="You have completed your stay";
            type="BOOKING_COMPLETED";
        }
        else {
            return null;
        }
        return admin.messaging().send({
            token: fcmtoken,
            notification: {title, body},
            data: {type, targetId: context.params.bookingId},
        });
    });


/**
 * trigger for new chat message notifies the recipient
 */
exports.onNewChatMessage = functions.firestore
    .document("conversations/{convId}/messages/{msgId}")
    .onCreate(async (snap, context) => {
        const message = snap.data();
        const convDoc = await admin.firestore()
            .collection("conversations")
            .doc(context.params.convId)
            .get();
        const conv = convDoc.data();
        const reciverId = message.senderId === conv.hostId ? conv.renterId : conv.hostId;
        const reciverDoc = await admin.firestore()
            .collection("users")
            .doc(reciverId)
            .get();
        const fcmtoken = reciverDoc.data()?.fcmToken;
        if (!fcmtoken) return null;
        return admin.messaging().send({
            token: fcmtoken,
            notification: {
                title: "New Message",
                body: message.text || "You Recived an Image",
            },
            data: {
                type: "NEW_MESSAGE",
                targetId: context.params.convId,
            },
        });
    });
