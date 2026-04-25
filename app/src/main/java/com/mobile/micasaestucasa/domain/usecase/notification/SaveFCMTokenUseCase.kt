package com.mobile.micasaestucasa.domain.usecase.notification

import com.mobile.micasaestucasa.domain.repository.notification.NotificationRepo
import javax.inject.Inject

/**
 * Use Case for saving token on login or token rotation
 *
 * Called in two separate moments:
 * 1 Right away after succesful login [AuthViewModel]
 * 2. IN MiCasaFirebaseMessagingService.onNewToken when FCM rotates the token
 *
 * @property notificationRepo repo for the fCM operations
 * */
class SaveFCMTokenUseCase @Inject constructor(private val notificationRepo: NotificationRepo) {

    /**
     * @param userId UID of the logged user cant be blank
     * @param token FCM Token to save cannot be blank either
     * @return Result.success if saved otherwise result.failure with illegalArgExcp if params not valid
     *
     * */
    suspend operator fun invoke(userId: String, token: String): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("userID not valid"))
        }
        if (token.isBlank()) {
            return Result.failure(IllegalArgumentException("Invalid FCM token"))
        }
        return notificationRepo.saveFCMToken(userId, token)
    }
}
