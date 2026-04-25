package com.mobile.micasaestucasa.domain.usecase.chat

import com.mobile.micasaestucasa.domain.model.chat.Conversation
import com.mobile.micasaestucasa.domain.repository.chat.ChatRepo
import javax.inject.Inject

class GetOrCreateConversationUseCase @Inject constructor(private val chatRepo: ChatRepo) {

    suspend operator fun invoke(
        hostId: String,
        renterId: String,
        propertyId: String
    ): Result<Conversation> {
        if (hostId.isBlank() || renterId.isBlank() || propertyId.isBlank()) {
            return Result.failure(IllegalArgumentException("Parametri non validi"))
        }
        if (hostId == renterId) {
            return Result.failure(IllegalArgumentException("host e renter non possono essere uguali"))
        }
        return chatRepo.getOrCreateConversation(hostId, renterId, propertyId)
    }
}
