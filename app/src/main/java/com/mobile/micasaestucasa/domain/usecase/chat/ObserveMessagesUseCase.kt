package com.mobile.micasaestucasa.domain.usecase.chat

import com.mobile.micasaestucasa.domain.model.chat.Message
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(
    private val chatRepo: ChatRepo
) {
    suspend operator fun invoke(conversationId: String): Flow<List<Message>> {
        require(conversationId.isNotBlank()) { "conversationId is required" }
        return chatRepo.observeMessages(conversationId)
    }
}
