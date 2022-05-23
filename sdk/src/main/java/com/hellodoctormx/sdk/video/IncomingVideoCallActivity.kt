package com.hellodoctormx.sdk.video

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationManagerCompat
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.ui.theme.Blue700
import com.hellodoctormx.sdk.ui.theme.HelloDoctorSDKTheme

const val INCOMING_VIDEO_CALL_ACTION = "com.hellodoctormx.sdk.action.INCOMING_VIDEO_CALL"
const val INCOMING_VIDEO_CALL_NOTIFICATION_ID = 42
const val INCOMING_VIDEO_CALL_STATE = "com.hellodoctormx.sdk.video.INCOMING_VIDEO_CALL_STATE"
const val VIDEO_ROOM_SID = "com.hellodoctormx.sdk.video.VIDEO_ROOM_SID"
const val CALLER_DISPLAY_NAME = "com.hellodoctormx.sdk.video.CALLER_DISPLAY_NAME"

class IncomingVideoCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoRoomSID = intent.getStringExtra(VIDEO_ROOM_SID)
        HelloDoctorClient.IncomingVideoCall.videoRoomSID = videoRoomSID

        val activeVideoCallModel = ActiveVideoCallModel()

        VideoCallController.getInstance(this).apply {
            localAudioController.setRingtonePlaying(false)
        }

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(INCOMING_VIDEO_CALL_NOTIFICATION_ID)

        setContent {
            HelloDoctorSDKTheme {
                VideoCallPermissions(
                    content = {
                        when (intent.getStringExtra(INCOMING_VIDEO_CALL_STATE)) {
                            "answered" -> setContent {
                                ActiveVideoCallScreen(activeVideoCallModel)
                            }
                            "incoming" -> setContent {
                                IncomingVideoCallScreen(
                                    activeVideoCallModel = activeVideoCallModel,
                                    callerDisplayName = intent.getStringExtra(CALLER_DISPLAY_NAME)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun IncomingVideoCallScreen(
    activeVideoCallModel: ActiveVideoCallModel,
    callerDisplayName: String? = "HelloDoctor Médico",
    isPreview: Boolean? = false
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        if (isPreview != true) LocalParticipantAndroidView()
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(2.dp)
        ) {
            IncomingVideoCallControls(activeVideoCallModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncomingVideoCallScreenPreview() {
    IncomingVideoCallScreen(activeVideoCallModel = ActiveVideoCallModel(), isPreview = true)
}

@Composable
fun ActiveVideoCallScreen(activeVideoCallModel: ActiveVideoCallModel, isPreview: Boolean? = false) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        activeVideoCallModel.doConnect(context)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Box {
            if (isPreview != true) RemoteParticipantAndroidView()
            if (isPreview == true) Box(modifier = Modifier.background(Blue700).fillMaxSize().zIndex(0f))
            LocalParticipantPortal(content = {
                Box {
                    if (isPreview != true) LocalParticipantAndroidView()
                }
            })
        }
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(2.dp)
        ) {
            ActiveVideoCallControls(activeVideoCallModel)
        }
    }
}

@Composable
fun LocalParticipantPortal(content: @Composable () -> Unit = { }) {
    Column(verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .width(128.dp)
                .height((1.4 * 128).dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            Box(modifier = Modifier
                .background(Color.Black)
                .fillMaxSize()
                .zIndex(1f)) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActiveVideoCallScreenPreview() {
    ActiveVideoCallScreen(ActiveVideoCallModel(), isPreview = true)
}