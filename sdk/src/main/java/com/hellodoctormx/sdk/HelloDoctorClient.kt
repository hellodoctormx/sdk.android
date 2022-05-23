package com.hellodoctormx.sdk

import android.content.Context
import com.hellodoctormx.sdk.auth.HDCurrentUser
import com.hellodoctormx.sdk.clients.AbstractServiceClient
import com.hellodoctormx.sdk.clients.LOCAL_USER_SERVICE_HOST
import com.hellodoctormx.sdk.clients.UserServiceClient
import com.hellodoctormx.sdk.clients.VideoServiceClient
import com.hellodoctormx.sdk.types.Consultation
import com.hellodoctormx.sdk.video.VideoCallController
import kotlinx.coroutines.runBlocking

class HelloDoctorClient(private val context: Context) {
    private val userServiceClient = UserServiceClient(context)

    suspend fun signIn(userID: String, serverAuthToken: String) {
        HDCurrentUser.signIn(context, userID, serverAuthToken)
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

    companion object {
        var apiKey: String? = null
        var serviceHost: String? = null

        fun configure(apiKey: String, serviceHost: String) {
            AbstractServiceClient.apiKey = apiKey
            AbstractServiceClient.defaultServiceHost = serviceHost
        }
    }

    object IncomingVideoCall {
        var videoRoomSID: String? = null
        var callerDisplayName: String? = null
    }
}
