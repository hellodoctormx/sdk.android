package com.hellodoctormx.sdk.video

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.ui.theme.HelloDoctorSDKTheme

const val INCOMING_VIDEO_CALL_ACTION = "com.hellodoctormx.sdk.action.INCOMING_VIDEO_CALL"
const val INCOMING_VIDEO_CALL_STATE = "com.hellodoctormx.sdk.video.INCOMING_VIDEO_CALL_STATE"
const val VIDEO_ROOM_SID = "com.hellodoctormx.sdk.video.VIDEO_ROOM_SID"
const val CALLER_DISPLAY_NAME = "com.hellodoctormx.sdk.video.CALLER_DISPLAY_NAME"

class IncomingVideoCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoRoomSID = intent.getStringExtra(VIDEO_ROOM_SID)
        HelloDoctorClient.IncomingVideoCall.videoRoomSID = videoRoomSID

        when(intent.getStringExtra(INCOMING_VIDEO_CALL_STATE)) {
            "answered" -> setContent {
                VideoCallScreen()
            }
            "incoming" -> setContent {
                IncomingVideoCallScreen(callerDisplayName = intent.getStringExtra(CALLER_DISPLAY_NAME))
            }
        }
    }
}

@Composable
fun IncomingVideoCallScreen(callerDisplayName: String? = "HelloDoctor Médico") {
    val videoCallController = VideoCallController.getInstance(LocalContext.current)

    HelloDoctorSDKTheme {
        VideoCallPermissions(
            content = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black,
                ) {
                    AndroidView(factory = { context -> LocalParticipantView(context).apply {
                        videoCallController.setLocalParticipantView(this)
                        videoCallController.startLocalCapture()
                    }})
                    Box(modifier = Modifier.background(Color.Black)) {
                        Text(text = callerDisplayName!!, color = Color.White)
                    }
                    Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(2.dp)) {
                        IncomingVideoCallControls(videoCallController)
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IncomingVideoCallScreenPreview() {
    IncomingVideoCallScreen()
}