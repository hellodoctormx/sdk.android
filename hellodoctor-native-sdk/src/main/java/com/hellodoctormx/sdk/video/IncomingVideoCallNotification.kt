package com.hellodoctormx.sdk.video

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.R

object IncomingVideoCallNotification {
    fun createChannel(
        context: Context,
        channelID: String = "incoming_video_call",
        channelName: String = "Videollamadas",
        descriptionText: String = "Notificaciones de videollamadas"
    ) {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
            description = descriptionText
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC

            vibrationPattern = longArrayOf(
                0, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500
            )

            val ringtoneSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtoneAttributes = AudioAttributes.Builder()
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            setSound(ringtoneSound, ringtoneAttributes)
        }

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannel(channel)
    }

    inline fun <reified IncomingVideoCallActivityType> display(
        context: Context,
        videoRoomSID: String,
        callerDisplayName: String?
    ) {
        val client = HelloDoctorClient(context)
        client.registerIncomingVideoCall(videoRoomSID, callerDisplayName!!)

        val answerCallAction = run {
            val answerCallIntent = Intent(context, IncomingVideoCallActivityType::class.java)
            answerCallIntent.action = Actions.INCOMING_VIDEO_CALL_ANSWERED.action
            answerCallIntent.putExtra(INCOMING_VIDEO_CALL_ACTION, "answered")
            answerCallIntent.putExtra(VIDEO_ROOM_SID, videoRoomSID)
            answerCallIntent.putExtra(CALLER_DISPLAY_NAME, callerDisplayName)

            val answerCallPendingIntent = PendingIntent.getActivity(
                context,
                0,
                answerCallIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            NotificationCompat.Action(
                IconCompat.createWithResource(context, R.drawable.ic_video_solid),
                "Contestar",
                answerCallPendingIntent
            )
        }

        val rejectCallAction = run {
            val rejectCallIntent = Intent(context, IncomingVideoCallActivityType::class.java).apply {
                action = Actions.INCOMING_VIDEO_CALL_REJECTED.action
                putExtra(INCOMING_VIDEO_CALL_ACTION, "rejected")
            }

            val rejectCallPendingIntent = PendingIntent.getActivity(
                context,
                0,
                rejectCallIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            NotificationCompat.Action.Builder(
                IconCompat.createWithResource(context, R.drawable.ic_video_solid),
                "Colgar",
                rejectCallPendingIntent
            ).build()
        }

        val fullScreenIntent = Intent(context, IncomingVideoCallActivityType::class.java)
        fullScreenIntent.putExtra(VIDEO_ROOM_SID, videoRoomSID)
        fullScreenIntent.putExtra(CALLER_DISPLAY_NAME, callerDisplayName)

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationTitle = String.format("%s de HelloDoctor", callerDisplayName)

        val notification = NotificationCompat.Builder(context, "incoming_video_call")
            .setSmallIcon(R.drawable.ic_phone_solid)
            .setContentTitle(notificationTitle)
            .setContentText("Tu médico te está llamando para tu asesoría")
            .setOngoing(true)
            .addAction(answerCallAction)
            .addAction(rejectCallAction)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(INCOMING_VIDEO_CALL_NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(INCOMING_VIDEO_CALL_NOTIFICATION_ID)
    }
}