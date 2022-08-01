package com.hellodoctormx.sdk.video

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.WithVideoCallPermissions
import com.hellodoctormx.sdk.ui.theme.HelloDoctorSDKTheme

enum class Actions(val action: String) {
    INCOMING_VIDEO_CALL_ANSWERED("com.hellodoctormx.sdk.action.INCOMING_VIDEO_CALL_ANSWERED"),
    INCOMING_VIDEO_CALL_ENDED("com.hellodoctormx.sdk.action.INCOMING_VIDEO_CALL_ENDED"),
    INCOMING_VIDEO_CALL_FULLSCREEN("com.hellodoctormx.sdk.action.INCOMING_VIDEO_CALL_FULLSCREEN"),
    INCOMING_VIDEO_CALL_REJECTED("com.hellodoctormx.sdk.action.INCOMING_VIDEO_CALL_REJECTED")
}

const val INCOMING_VIDEO_CALL_NOTIFICATION_ID = 42
const val INCOMING_VIDEO_CALL_ACTION = "INCOMING_VIDEO_CALL_ACTION"
const val INCOMING_VIDEO_CALL_CHANNEL = "incoming_video_call"
const val VIDEO_ROOM_SID = "VIDEO_ROOM_SID"
const val CALLER_DISPLAY_NAME = "CALLER_DISPLAY_NAME"
const val CALLER_PHOTO_URL = "CALLER_PHOTO_URL"

open class IncomingVideoCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoRoomSID = intent.getStringExtra(VIDEO_ROOM_SID)
        val callerDisplayName = intent.getStringExtra(CALLER_DISPLAY_NAME)
        val callerPhotoURL = intent.getStringExtra(CALLER_PHOTO_URL)

        if (videoRoomSID == null) {
            finish()
            return
        }

        hideSystemBars()

        HelloDoctorClient.registerIncomingVideoCall(videoRoomSID, callerDisplayName, callerPhotoURL)

        val videoCallModel = VideoCallModel.getInstance()
        videoCallModel.roomStatus = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        setContent {
            HelloDoctorSDKTheme {
                WithVideoCallPermissions {
                    when (intent.action) {
                        Actions.INCOMING_VIDEO_CALL_REJECTED.action,
                        Actions.INCOMING_VIDEO_CALL_ENDED.action -> {
                            IncomingVideoCallNotification.cancel(this)
                            finish()
                        }
                        else -> {
                            VideoCallScreen(
                                videoCallModel = videoCallModel,
                                isConnected = intent.action == Actions.INCOMING_VIDEO_CALL_ANSWERED.action
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        showSystemBars()
    }

    private fun hideSystemBars() {
        ViewCompat.getWindowInsetsController(window.decorView)?.apply {
            // Configure the behavior of the hidden system bars
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            // Hide both the status bar and the navigation bar
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    private fun showSystemBars() {
        ViewCompat.getWindowInsetsController(window.decorView)?.apply {
            show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
