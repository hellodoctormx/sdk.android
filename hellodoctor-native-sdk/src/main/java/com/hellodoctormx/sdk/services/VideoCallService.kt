package com.hellodoctormx.sdk.services

import android.content.Context
import kotlinx.serialization.Serializable

class VideoCallService(context: Context) : HelloDoctorHTTTPClient(context) {
    suspend fun requestVideoCallAccess(videoRoomSID: String): RequestVideoCallAccessResponse {
        return this.get(path = "/video/$videoRoomSID/access-token")
    }

    suspend fun getVideoCall(videoRoomSID: String): GetVideoCallResponse {
        return this.get(path = "/video/$videoRoomSID")
    }

    suspend fun endVideoCall(videoRoomSID: String): EndVideoCallResponse {
        return this.post(path = "/video/$videoRoomSID/_end", postData = null)
    }

    @Serializable
    data class RequestVideoCallAccessResponse(val accessToken: String)

    @Serializable
    data class GetVideoCallResponse(val sid: String, val status: String)

    @Serializable
    data class EndVideoCallResponse(val status: String)
}
