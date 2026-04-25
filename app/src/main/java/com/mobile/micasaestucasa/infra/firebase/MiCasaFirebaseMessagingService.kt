package com.mobile.micasaestucasa.infra.firebase
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mobile.micasaestucasa.MainActivity
import com.mobile.micasaestucasa.R
import com.mobile.micasaestucasa.domain.usecase.notification.SaveFCMTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * FCM Service for reciving push notifications
 * */
@AndroidEntryPoint
class MiCasaFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var saveFCMTokenUseCase: SaveFCMTokenUseCase

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    /**
     * Called when FCM rotates the toekn of the device
     *
     * Usally called on:
     * new device registartion
     * user installs/uninstalls app
     * FCM invalidates token
     * maybe someother edge case which im forgeting.
     *
     * */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = firebaseAuth.currentUser?.uid ?: return
        // launched ina  corutinescope bcs onenewtoekn is called
        // on mianthread and cannot be suspended directly
        kotlinx.coroutines.MainScope().launch {
            saveFCMTokenUseCase(userId, token)
        }
    }

    /**
     * Called when a new FCM message arrives with the app in background
     *
     * Android handles the messages with app in background while in foreground we have to construct
     * */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: return
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: return
        val type = remoteMessage.data["type"] ?: "UNKNOWN"
        val targetId = remoteMessage.data["targetId"] ?: ""
        showNotification(title, body, type, targetId)
    }

    /**
     * Constructs and shows a notification in the drawer
     *
     * uses NotificationCompact for android compatibility
     * @param title title of the notification
     * @param body body of the notification
     * @param type String of the NotificationType used for the deep linking
     * @param targetId id of the contextual entity of the notificatio
     * */
    private fun showNotification(title: String, body: String, type: String, targetId: String) {
        val channelId = "micasa_notifications"
        // intent with deep link targetid and type are extras
        // to be read in mainactivity and launch the right screen
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notificationType", type)
            putExtra("targetID", targetId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "MiCasa Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
