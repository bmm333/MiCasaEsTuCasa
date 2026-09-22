package com.mobile.micasaestucasa.domain.usecase.chat

import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import javax.inject.Inject

class GetConversationsForUserUseCase @Inject constructor(
    private val chatRepo: ChatRepo
) {
    suspend operator fun invoke(userId: String): Result<List<Conversation>> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("userId is required"))
        }
        return chatRepo.getConversationsForUser(userId)
    }
}
