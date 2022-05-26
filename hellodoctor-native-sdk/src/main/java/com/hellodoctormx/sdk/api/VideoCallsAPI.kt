package com.hellodoctormx.sdk.api

import android.content.Context
import kotlinx.serialization.Serializable

class VideoServiceClient(context: Context) : HelloDoctorHTTTPClient(context) {
    suspend fun requestVideoCallAccess(videoRoomSID: String): RequestVideoCallAccessResponse {
        return this.get(path = "/video/$videoRoomSID/access-token")
    }

    suspend fun getVideoCall(videoRoomSID: String): GetVideoCallResponse {
        return this.get(path = "/video/$videoRoomSID")
    }

    suspend fun endVideoCall(videoRoomSID: String) {
        return this.post(path = "/video/$videoRoomSID/_end", postData = null)
    }

    suspend fun rejectVideoCall(videoRoomSID: String) {
        return this.post(path = "/video/$videoRoomSID/_reject", postData = null)
    }

    @Serializable
    data class RequestVideoCallAccessResponse(val accessToken: String)

    @Serializable
    data class GetVideoCallResponse(val sid: String, val status: String)
}
