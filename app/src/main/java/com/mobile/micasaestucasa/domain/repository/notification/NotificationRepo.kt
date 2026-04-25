package com.mobile.micasaestucasa.domain.repository.notification

/**
 * Definition of the notification contract
 * occupied with fcm token handling and notification preferences.
 *
 * Repo dose not send notifications since that is an duty of could functions server side.
 * client handles only Saving/updating token fcm in firestore
 * removing  token on logout to not recive notifications anymore
 * */
interface NotificationRepo {
    /**
     * Saves or updates the fcm token of user in firestore
     *
     * the fcm token is rotated from firebase preiodically `MiCasaFireBaseMessagingService` calls the onNewToken each rotation
     *
     * @param userId UID Firebase of the user
     * @param token current FCM TOken of thedevice
     * @param [Result.success] if saved [Result.failure] in case of firestore error
     * */
    suspend fun saveFCMToken(userId: String, token: String): Result<Unit>

    /**
     * Removes FCM token of the user on logout.
     * without this operation the user would continue to recive notifications
     * this would be unaceptable
     * @param userUID Uid Friestore of the user logging out
     * @param Result.success if removed [Result.failure] in case of firestore error
     * */
    suspend fun removeFCMToken(userId: String): Result<Unit>

    /**
     * Gets the current FCM token of the device from firebase messaging.
     *
     * @return Result with tthe toekn as a String , or Failure if FCM not avalible.
     * */
    suspend fun getCurrentToken(): Result<String>
}
