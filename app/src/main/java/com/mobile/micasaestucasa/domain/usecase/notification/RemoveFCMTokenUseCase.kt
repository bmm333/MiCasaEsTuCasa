package com.mobile.micasaestucasa.domain.usecase.notification

import com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
import javax.inject.Inject

class RemoveFCMTokenUseCase @Inject constructor(
    private val notificationRepo: NotificationRepo
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("userId is required"))
        }
        return notificationRepo.removeFCMToken(userId)
    }
}
