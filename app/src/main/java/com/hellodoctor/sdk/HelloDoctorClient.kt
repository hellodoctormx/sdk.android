package com.hellodoctor.sdk

import android.content.Context
import com.hellodoctor.sdk.auth.HDCurrentUser
import com.hellodoctor.sdk.clients.AbstractServiceClient
import com.hellodoctor.sdk.clients.LOCAL_USER_SERVICE_HOST
import com.hellodoctor.sdk.clients.UserServiceClient
import com.hellodoctor.sdk.clients.VideoServiceClient
import com.hellodoctor.sdk.types.Consultation
import com.hellodoctor.sdk.video.VideoCallController
import kotlinx.coroutines.runBlocking

const val API_KEY = "Ax3JVY2pal5f8i6NwLNX3wjssyiR46u7itHypjZe"

class HelloDoctorClient(private val context: Context, apiKey: String = API_KEY, serviceHost: String = LOCAL_USER_SERVICE_HOST) {
    private val userServiceClient = UserServiceClient(context)

    init {
        AbstractServiceClient.apiKey = apiKey
        AbstractServiceClient.defaultServiceHost = serviceHost
    }

    fun signIn(userID: String, serverAuthToken: String) {
        runBlocking {
            HDCurrentUser.signIn(context, userID, serverAuthToken)
        }
    }

    fun signOut() {
        HDCurrentUser.signOut()
    }

    fun createUser(thirdPartyUserID: String, email: String, phoneNumber: String?): String {
        var response: UserServiceClient.CreateUserResponse

        runBlocking {
            response = userServiceClient.createUser(thirdPartyUserID, email, phoneNumber)
        }

        return response.uid
    }

    fun getConsultations(limit: Int): List<Consultation> {
        var response: UserServiceClient.GetUserConsultationsResponse

        runBlocking {
            response = userServiceClient.getUserConsultations(limit)
        }

        return response.consultations
    }

    fun registerIncomingVideoCall(videoRoomSID: String, callerDisplayName: String) {
        IncomingVideoCall.videoRoomSID = videoRoomSID
        IncomingVideoCall.callerDisplayName = callerDisplayName
    }

    fun answerIncomingVideoCall() {
        val incomingVideoCallRoomSID = IncomingVideoCall.videoRoomSID
            ?: throw Error("no incoming video call")

        var videoCallAccessToken: String? = null

        runBlocking {
            with (VideoServiceClient(context)) {
                val videoCallAccessResponse = this.requestVideoCallAccess(incomingVideoCallRoomSID)
                videoCallAccessToken = videoCallAccessResponse.accessToken
            }
        }

        val videoCallController = VideoCallController.getInstance(context)
        videoCallController.connect(incomingVideoCallRoomSID, videoCallAccessToken!!)
    }

    fun rejectIncomingVideoCall() {
        val incomingVideoCallRoomSID = IncomingVideoCall.videoRoomSID
            ?: throw Error("no incoming video call")

        runBlocking{
            val videoServiceClient = VideoServiceClient(context)
            videoServiceClient.rejectVideoCall(incomingVideoCallRoomSID)
        }
    }

    object IncomingVideoCall {
        var videoRoomSID: String? = null
        var callerDisplayName: String? = null
    }
}
