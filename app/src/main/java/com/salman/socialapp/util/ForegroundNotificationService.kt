package com.salman.socialapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.salman.socialapp.R

const val CHANNEL_ID = "socialapp_channel"
const val CHANNEL_NAME = "Socialapp"
const val NOTIFICATION_ID = 1
class ForegroundNotificationService : FirebaseMessagingService() {

    override fun onNewToken(s: String) {
        super.onNewToken(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val mIntent = Intent(remoteMessage.notification?.clickAction)
        val bundle = Bundle()
        if (remoteMessage.data.containsKey("isFromNotification")) {
            bundle.putString("isFromNotification", remoteMessage.data.get("isFromNotification"))
        }
        mIntent.putExtras(bundle)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())

        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManagerCompat.createNotificationChannel(notificationChannel)
        }
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }
}