package com.hellodoctormx.sdk

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationManagerCompat
import com.hellodoctormx.sdk.api.ConsultationsAPI
import com.hellodoctormx.sdk.auth.HDCurrentUser
import com.hellodoctormx.sdk.types.Consultation
import com.hellodoctormx.sdk.video.INCOMING_VIDEO_CALL_CHANNEL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val DEFAULT_SERVICE_HOST = "https://public-api-pusuheofiq-uc.a.run.app"

class HelloDoctorClient {
    companion object {
        var apiKey: String? = null
        var serviceHost: String = DEFAULT_SERVICE_HOST

        fun configure(apiKey: String, serviceHost: String) {
            this.apiKey = apiKey
            this.serviceHost = serviceHost
        }

        suspend fun signIn(context: Context, userID: String, serverAuthToken: String) {
            HDCurrentUser.signIn(context, userID, serverAuthToken)
        }

        fun signInWithJWT(userID: String, jwt: String) {
            HDCurrentUser.uid = userID
            HDCurrentUser.jwt = jwt
        }

        fun signOut() {
            HDCurrentUser.signOut()
        }

        fun createVideoCallNotificationChannel(
            context: Context,
            channelName: String = "Videollamadas",
            descriptionText: String = "Notificaciones de videollamadas"
        ) {
            val notificationManager = NotificationManagerCompat.from(context)

            if (notificationManager.getNotificationChannel(INCOMING_VIDEO_CALL_CHANNEL) == null) {
                val channel = NotificationChannel(INCOMING_VIDEO_CALL_CHANNEL, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                    description = descriptionText
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC

                    vibrationPattern = longArrayOf(
                        0, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500,
                        1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500,
                        900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900,
                        1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500, 900, 1500
                    )

                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)?.let { ringtoneSound ->
                        val ringtoneAttributes = AudioAttributes.Builder()
                            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .build()

                        setSound(ringtoneSound, ringtoneAttributes)
                    }
                }

                notificationManager.createNotificationChannel(channel)
            }
        }

        suspend fun getConsultations(context: Context, limit: Int): List<Consultation> = withContext(
            Dispatchers.IO) {
            val consultationsAPI = ConsultationsAPI(context)

            val response = consultationsAPI.getUserConsultations(limit)
            return@withContext response.consultations
        }

        fun registerIncomingVideoCall(videoRoomSID: String, callerDisplayName: String?, callerPhotoURL: String?) {
            IncomingVideoCall.videoRoomSID = videoRoomSID
            IncomingVideoCall.callerDisplayName = callerDisplayName
            IncomingVideoCall.callerPhotoURL = callerPhotoURL
        }
    }

    object IncomingVideoCall {
        var videoRoomSID: String? = null
        var callerDisplayName: String? = null
        var callerPhotoURL: String? = null
    }
}
