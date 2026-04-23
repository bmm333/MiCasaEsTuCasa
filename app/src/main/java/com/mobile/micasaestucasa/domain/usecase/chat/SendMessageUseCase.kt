package com.mobile.micasaestucasa.domain.usecase.chat

import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import javax.inject.Inject

/**
 * Use case for sending messages in chat
 *
 * Responsabilities:
 *  -Validation of input
 *  -Repo call to send message
 *
 *  dose not handle upload of image that will be handled by the viewmodel which calls
 *  first the uploadimageusecase end then this usecase with the url
 *
 *  @property chatRepo Repository of the chat operations
 *
 * */
class SendMessageUseCase @Inject constructor(private val chatRepo: ChatRepo){

    suspend operator fun invoke(
        conversationId: String,
        senderId: String,
        text:String,
        imageUrl:String?=null
    ): Result<Message>{
        if(conversationId.isBlank())
            return Result.failure(IllegalArgumentException("ConversationId non valido"))
        if (senderId.isBlank())
            return Result.failure(IllegalArgumentException("SenderId non valido"))
        if (text.isBlank() && imageUrl == null)
            return Result.failure(IllegalArgumentException("Messaggio non può essere vuoto"))
        return chatRepo.sendMessage(conversationId, senderId, text, imageUrl);
    }
}