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
      notification: {title: payload.title, body: payload.body},
      data: {type: payload.type, targetId: payload.targetId},
      android: {notification: {channelId: "micasa_notifications"}},
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
exports.onNewBookingRequest = functions.firestore
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
exports.onBookingUpdate = functions.firestore
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
exports.onNewChatMessage = functions.firestore
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
