package com.mobile.micasaestucasa.data.repository.chat

import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import javax.inject.Inject
//welp stopping here for now , next job on the clock is to implement the chat repo
// hell of a job XD and yeah update the indexes too dont forget ben
class FirebaseChatRepo @Inject constructor(private val firestore: FirebaseFirestore): ChatRepo {
    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String,
        imageUrl: String?
    ): Result<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun getOrCreateConversation(
        hostId: String,
        renterId: String,
        propertyId: String
    ): Result<Conversation> {
        TODO("Not yet implemented")
    }

    override suspend fun markMessagesAsRead(
        conversationId: String,
        userId: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getConversationsForUser(userId: String): Result<List<Conversation>> {
        TODO("Not yet implemented")
    }

}