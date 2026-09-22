package com.mobile.micasaestucasa.domain.model.chat

/**
 * Rappresents a singular unit of message in a conversation between two users.
 *
 * @property id unique id of the message , created by firestore.
 * @property conversationId Id of the conversation this message belongs to
 * @property senderId UID firebase of the user who sent it
 * @property text the content of the message . can be empty if the message contains only an image
 * @property imageUrl url of the image on firebase. Null if the message contains only text.
 * @property timestamp timestamp of msg creation
 * @property isRead indicates if the reciver read the message.
 * */
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
