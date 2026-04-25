package com.mobile.micasaestucasa.domain.repository.chat

import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.domain.model.chat.Message
import kotlinx.coroutines.flow.Flow

/**
 * Real time chat operations Contract
 *
 * */
interface ChatRepo {

    /**
     *  sends a message which can be textual or image,
     *  if the message is an image then this method should only be called after the image is uploaded to firebase
     *  repo dose not handle image upload
     *
     * */
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String,
        imageUrl: String? = null
    ): Result<Message>

    suspend fun getOrCreateConversation(hostId: String, renterId: String, propertyId: String): Result<Conversation>

    suspend fun observeMessages(conversationId: String): Flow<List<Message>>

    /**
     * this function marks all not read messages as read for the current user
     *
     * */
    suspend fun markMessagesAsRead(
        conversationId: String,
        userId: String
    ): Result<Unit>

    /**
     * Returns all the conversations of an unser ordered by last message timestamp
     * */
    suspend fun getConversationsForUser(userId: String): Result<List<Conversation>>
}
