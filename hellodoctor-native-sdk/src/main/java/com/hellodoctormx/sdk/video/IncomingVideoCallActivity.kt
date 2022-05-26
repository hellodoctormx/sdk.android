package com.hellodoctormx.sdk.video

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.ui.theme.HelloDoctorSDKTheme

enum class Actions(val action: String) {
    INCOMING_VIDEO_CALL_ANSWERED("com.hellodoctormx.sdk.action.INCOMING_VIDEO_CALL_ANSWERED"),
    INCOMING_VIDEO_CALL_REJECTED("com.hellodoctormx.sdk.action.INCOMING_VIDEO_CALL_REJECTED")
}

const val INCOMING_VIDEO_CALL_NOTIFICATION_ID = 42
const val INCOMING_VIDEO_CALL_ACTION = "INCOMING_VIDEO_CALL_ACTION"
const val VIDEO_ROOM_SID = "VIDEO_ROOM_SID"
const val CALLER_DISPLAY_NAME = "CALLER_DISPLAY_NAME"

open class IncomingVideoCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoRoomSID = intent.getStringExtra(VIDEO_ROOM_SID)
        val callerDisplayName = intent.getStringExtra(CALLER_DISPLAY_NAME)

        HelloDoctorClient.IncomingVideoCall.videoRoomSID = videoRoomSID
        HelloDoctorClient.IncomingVideoCall.callerDisplayName = callerDisplayName

        val activeVideoCallModel = ActiveVideoCallModel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        setContent {
            HelloDoctorSDKTheme {
                VideoCallPermissions(
                    content = {
                        val action = intent.getStringExtra(INCOMING_VIDEO_CALL_ACTION)

                        if (action == "rejected") {
                            IncomingVideoCallNotification.cancel(this)
                            finish()
                        } else {
                            VideoCallScreen(
                                activeVideoCallModel = activeVideoCallModel,
                                callerDisplayName = intent.getStringExtra(CALLER_DISPLAY_NAME),
                                isConnected = action == "answered"
                            )
                        }
                    }
                )
            }
        }
    }
}
