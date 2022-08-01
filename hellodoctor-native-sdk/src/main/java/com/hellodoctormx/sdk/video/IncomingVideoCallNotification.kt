package com.hellodoctormx.sdk.video

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.R


object IncomingVideoCallNotification {
    private const val tag = "IncomingVideoCallNotification"

    fun display(
        context: Context,
        videoRoomSID: String,
        callerDisplayName: String,
        callerPhotoURL: String? = null
    ) {
        HelloDoctorClient.registerIncomingVideoCall(videoRoomSID, callerDisplayName, callerPhotoURL)

        val answerCallAction = run {
            val answerCallIntent = Intent(context, IncomingVideoCallActivity::class.java).apply {
                action = Actions.INCOMING_VIDEO_CALL_ANSWERED.action
                putExtra(INCOMING_VIDEO_CALL_ACTION, "answered")
                putExtra(VIDEO_ROOM_SID, videoRoomSID)
                putExtra(CALLER_DISPLAY_NAME, callerDisplayName)
                putExtra(CALLER_PHOTO_URL, callerPhotoURL)
            }

            val answerCallPendingIntent = PendingIntent.getActivity(
                context,
                0,
                answerCallIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            NotificationCompat.Action(
                IconCompat.createWithResource(context, R.mipmap.ic_launcher),
                "Contestar",
                answerCallPendingIntent
            )
        }

        val rejectCallAction = run {
            val rejectCallIntent = Intent(context, IncomingVideoCallActivity::class.java).apply {
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
                IconCompat.createWithResource(context, R.mipmap.ic_launcher),
                "Colgar",
                rejectCallPendingIntent
            ).build()
        }

        val fullScreenIntent = Intent(context, IncomingVideoCallActivity::class.java).apply {
            action = Actions.INCOMING_VIDEO_CALL_FULLSCREEN.action
            putExtra(VIDEO_ROOM_SID, videoRoomSID)
            putExtra(CALLER_DISPLAY_NAME, callerDisplayName)
            putExtra(CALLER_PHOTO_URL, callerPhotoURL)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationTitle = String.format("%s de HelloDoctor", callerDisplayName)

        val notification = NotificationCompat.Builder(context, INCOMING_VIDEO_CALL_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notificationTitle)
            .setContentText("Tu médico te está llamando para tu asesoría")
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .addAction(answerCallAction)
            .addAction(rejectCallAction)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(tag, INCOMING_VIDEO_CALL_NOTIFICATION_ID, notification)
    }

    fun cancel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(tag, INCOMING_VIDEO_CALL_NOTIFICATION_ID)
    }

    fun reject(context: Context) {
        cancel(context)

        VideoCallModel.getInstance().apply {
            roomStatus = "disconnected"
        }
    }

    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName

        fun isProcessInForeground(p: RunningAppProcessInfo): Boolean = p.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && p.processName == packageName

        return appProcesses.any { isProcessInForeground(it) }
    }
}