package com.hellodoctormx.sdk.video

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.api.VideoCallsAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoCallModel(val isPreview: Boolean = false) : ViewModel() {
    var isConnected by mutableStateOf(false)
    var roomStatus by mutableStateOf("")
    var isCameraEnabled by mutableStateOf(true)
    var isMicrophoneEnabled by mutableStateOf(true)
    var activeCamera by mutableStateOf("front")
    var areControlsVisible by mutableStateOf(false)

    fun doConnect(context: Context) {
        roomStatus = "connecting"

        val videoCallController = VideoCallController.getInstance(context)
        val videoCallsAPI = VideoCallsAPI(context)
        val videoRoomSID = HelloDoctorClient.IncomingVideoCall.videoRoomSID!!

        viewModelScope.launch(Dispatchers.IO) {
            val videoAccessTokenResponse = videoCallsAPI.requestVideoCallAccess(videoRoomSID)

            videoCallController.connect(
                videoRoomSID = videoRoomSID,
                accessToken = videoAccessTokenResponse.accessToken
            )

            isConnected = true
        }
    }

    fun doDisconnect(context: Context) {
        isConnected = false

        viewModelScope.launch(Dispatchers.IO) {
            val videoCallController = VideoCallController.getInstance(context)
            videoCallController.disconnect()

            (context as Activity).finish()
        }

        viewModelScope.launch(Dispatchers.IO) {
            VideoCallsAPI(context).endVideoCall(HelloDoctorClient.IncomingVideoCall.videoRoomSID!!)
        }
    }

    fun toggleControls() {
        areControlsVisible = !areControlsVisible
    }

    fun toggleCameraEnabled(context: Context) {
        isCameraEnabled = !isCameraEnabled

        val videoCallController = VideoCallController.getInstance(context)
        videoCallController.localVideoController.setCapturerEnabled(isCameraEnabled)
    }

    fun toggleMicrophoneEnabled(context: Context) {
        isMicrophoneEnabled = !isMicrophoneEnabled

        val videoCallController = VideoCallController.getInstance(context)
        videoCallController.localAudioController.setMicrophoneEnabled(isMicrophoneEnabled)
    }

    fun toggleCamera(context: Context) {
        val videoCallController = VideoCallController.getInstance(context)
        videoCallController.cameraController.switchCamera()

        activeCamera = if (activeCamera == "front") "back" else "front"
    }

    companion object {
        private var instance_: VideoCallModel? = null

        fun getInstance(): VideoCallModel {
            if (instance_ == null) {
                instance_ = VideoCallModel()
            }

            return instance_!!
        }
    }
}
