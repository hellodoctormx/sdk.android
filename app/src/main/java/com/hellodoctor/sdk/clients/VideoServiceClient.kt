package com.hellodoctor.sdk.clients

import android.content.Context
import kotlinx.serialization.Serializable

const val LOCAL_VIDEO_SERVICE_HOST = "http://192.168.100.26:3005"

class VideoServiceClient(context: Context, host: String? = LOCAL_VIDEO_SERVICE_HOST) : AbstractServiceClient(context, host = host) {
    @Serializable
    data class RequestVideoCallAccessResponse(val accessToken: String)
    suspend fun requestVideoCallAccess(videoRoomSID: String): RequestVideoCallAccessResponse {
        return this.get(path = "/access-token?videoRoomSID=$videoRoomSID")
    }

    @Serializable
    data class GetVideoCallResponse(val sid: String, val status: String)
    suspend fun getVideoCall(videoRoomSID: String): GetVideoCallResponse {
        return this.get(path = "/access-token?videoRoomSID=$videoRoomSID")
    }

    suspend fun endVideoCall(videoRoomSID: String) {
        this.post<String>(path = "/calls/$videoRoomSID/_end", postData = null)
    }

    suspend fun rejectVideoCall(videoRoomSID: String) {
        this.post<String>(path = "/calls/$videoRoomSID/_reject", postData = null)
    }
}
