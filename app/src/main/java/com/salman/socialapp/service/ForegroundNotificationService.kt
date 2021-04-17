package com.salman.socialapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.salman.socialapp.R
import com.salman.socialapp.ui.activities.IncomingInvitationActivity
import com.salman.socialapp.util.Constants
import com.salman.socialapp.util.Converter

private const val TAG = "ForegroundNotifications"
private const val CHANNEL_ID = "socialapp_channel"
private const val CHANNEL_NAME = "Socialapp"
private const val NOTIFICATION_ID = 1
class ForegroundNotificationService : FirebaseMessagingService() {

    lateinit var mBuilder: NotificationCompat.Builder
    lateinit var mIntent: Intent
    lateinit var pendingIntent: PendingIntent

    override fun onNewToken(s: String) {
        Log.d(TAG, "onNewToken: called $s")
        super.onNewToken(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: called")
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        val clickAction = remoteMessage.notification?.clickAction
        val fromUserId = remoteMessage.data[Constants.FROM_USER_ID]
        val fromUserName = remoteMessage.data[Constants.FROM_USER_NAME]
        val fromUserImage = remoteMessage.data[Constants.FROM_USER_IMAGE]
        val message = remoteMessage.data[Constants.MESSAGE]
        val isImage = remoteMessage.data[Constants.IS_IMAGE]
        val isFromNotification = remoteMessage.data[Constants.IS_FROM_NOTIFICATION]
        val type = remoteMessage.data[Constants.MSG_TYPE]
        Log.d(TAG, "data: $title, $body, $fromUserName, $isImage, $isFromNotification")

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(notificationManager)

        mIntent = Intent(clickAction)

        if (remoteMessage.data.size > 0) {
            if (isFromNotification != null && remoteMessage.data.containsKey(Constants.IS_FROM_NOTIFICATION)) {
                val bundle = Bundle()
                bundle.putString("isFromNotification", isFromNotification)
                mIntent.putExtras(bundle)
                mIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            } else if (isImage !=null && remoteMessage.data.containsKey(Constants.IS_IMAGE)) {
                mIntent.apply {
                    putExtra("fromUserId", fromUserId)
                    putExtra("fromUserName", fromUserName)
                    putExtra("fromUserImage", fromUserImage)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            } else if (type != null && remoteMessage.data.containsKey(Constants.MSG_TYPE)) {
                if (type.equals(Constants.MSG_INVITATION)) {
                    val intent = Intent(applicationContext, IncomingInvitationActivity::class.java)
                    intent.putExtra(Constants.MSG_MEETING_TYPE, remoteMessage.data.get(Constants.MSG_MEETING_TYPE))
                    intent.putExtra(Constants.KEY_USERNAME, remoteMessage.data.get(Constants.KEY_USERNAME))
                    intent.putExtra(Constants.MSG_SENDER_TOKEN, remoteMessage.data.get(Constants.MSG_SENDER_TOKEN))
                    intent.putExtra(Constants.MSG_MEETING_ROOM, remoteMessage.data.get(Constants.MSG_MEETING_ROOM))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } else if (type.equals(Constants.MSG_INVITATION_RESPONSE)) {
                    val intent = Intent(Constants.MSG_INVITATION_RESPONSE)
                    intent.putExtra(Constants.MSG_INVITATION_RESPONSE,
                        remoteMessage.data.get(Constants.MSG_INVITATION_RESPONSE))
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                }
            }

            pendingIntent = PendingIntent.getActivity(
                this,
                0,
                mIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        if (remoteMessage.notification != null) {
            when {
                isImage.equals("false") -> {
                    showNotification(title, body)
                }
                isImage.equals("true") -> {
                    showImageNotification(message, title, body)
                }
                isFromNotification.equals("true") -> {
                    showNotification(title, body)
                }
            }

            notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
        }

    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "Notification for messaging"
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        title: String?,
        body: String?
    ) {
        mBuilder = NotificationCompat.Builder(
            this,
            getString(R.string.default_notification_channel_id)
        ).apply {
            setSmallIcon(R.drawable.ic_notification)
            setColor(Color.RED)
            setContentTitle(title)
            setContentText(body)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            setWhen(System.currentTimeMillis())
        }
    }

    private fun showImageNotification(
        message: String?,
        title: String?,
        body: String?
    ) {

        val bitmap =
            Converter.StringBase64ToBitmap(
                message!!
            )
        val bigPictureStyle =
            NotificationCompat.BigPictureStyle().also {
                it.bigPicture(bitmap)
                it.bigLargeIcon(null)
            }

        mBuilder = NotificationCompat.Builder(
            this,
            getString(R.string.default_notification_channel_id)
        ).apply {
            setSmallIcon(R.drawable.ic_notification)
            setLargeIcon(bitmap)
            setColor(resources.getColor(R.color.colorAccent))
            setContentTitle(title)
            setContentText(body)
            setStyle(bigPictureStyle)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            setWhen(System.currentTimeMillis())
        }
    }
}