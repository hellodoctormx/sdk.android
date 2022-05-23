package com.hellodoctormx.sdk.clients

import android.content.Context
import kotlinx.serialization.Serializable

const val LOCAL_VIDEO_SERVICE_HOST = "http://192.168.100.26:3002"

class VideoServiceClient(context: Context, host: String? = LOCAL_VIDEO_SERVICE_HOST) : AbstractServiceClient(context, host = host) {
    @Serializable
    data class RequestVideoCallAccessResponse(val accessToken: String)
    suspend fun requestVideoCallAccess(videoRoomSID: String): RequestVideoCallAccessResponse {
        return this.get(path = "/calls/$videoRoomSID/access-token")
    }

    @Serializable
    data class GetVideoCallResponse(val sid: String, val status: String)
    suspend fun getVideoCall(videoRoomSID: String): GetVideoCallResponse {
        return this.get(path = "/calls/$videoRoomSID")
    }

    suspend fun endVideoCall(videoRoomSID: String) {
        return this.post(path = "/calls/$videoRoomSID/_end", postData = null)
    }

    suspend fun rejectVideoCall(videoRoomSID: String) {
        return this.post(path = "/calls/$videoRoomSID/_reject", postData = null)
    }
}
