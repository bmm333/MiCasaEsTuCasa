package com.mobile.micasaestucasa.domain.usecase.chat

import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import javax.inject.Inject

class MarkMessagesAsReadUseCase @Inject constructor(
    private val chatRepo: ChatRepo
) {
    suspend operator fun invoke(conversationId: String, userId: String): Result<Unit> {
        if (conversationId.isBlank()) {
            return Result.failure(IllegalArgumentException("conversationId is required"))
        }
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("userId is required"))
        }
        return chatRepo.markMessagesAsRead(conversationId, userId)
    }
}
