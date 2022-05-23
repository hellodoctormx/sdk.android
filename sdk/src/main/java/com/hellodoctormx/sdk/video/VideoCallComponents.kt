package com.hellodoctormx.sdk.video

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.R
import com.hellodoctormx.sdk.clients.VideoServiceClient
import com.hellodoctormx.sdk.ui.theme.Gray500
import com.hellodoctormx.sdk.ui.theme.Gray900
import com.hellodoctormx.sdk.ui.theme.Green200
import com.hellodoctormx.sdk.ui.theme.Red500
import com.hellodoctormx.sdk.video.ui.theme.HelloDoctorSDKTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Preview(showBackground = true)
@Composable
fun ActiveVideoCallControlsPreview() {
    val currentContext = LocalContext.current
    val hdVideo = VideoCallController(currentContext)
    ActiveVideoCallControls(hdVideo)
}

@Composable
fun ActiveVideoCallControls(hdVideo: VideoCallController?) {
    HelloDoctorSDKTheme {
        Surface(
            shape = CircleShape,
            modifier = Modifier.padding(12.dp),
            color = Gray900
        ) {
            Row {
                EndCallControl(hdVideo)
                ToggleVideoStateControl(hdVideo)
                ToggleAudioStateControl(hdVideo)
                ToggleCameraStateControl(hdVideo)
            }
        }
    }
}

@Composable
fun IncomingVideoCallControls(videoCallController: VideoCallController?) {
    HelloDoctorSDKTheme {
        Surface(
            shape = CircleShape,
            modifier = Modifier.padding(12.dp),
            color = Gray900
        ) {
            Row {
                EndCallControl(videoCallController)
                StartCallControl(videoCallController)
            }
        }
    }
}

@Composable
fun StartCallControl(videoCallController: VideoCallController?) {
    val coroutineScope = rememberCoroutineScope()

    var isConnected by remember {
        mutableStateOf(true)
    }

    val videoServiceClient = VideoServiceClient(LocalContext.current)
    val videoRoomSID = HelloDoctorClient.IncomingVideoCall.videoRoomSID!!

    val startCall: () -> Unit = {
        runBlocking {
            val videoAccessTokenResponse = videoServiceClient.requestVideoCallAccess(videoRoomSID)

            videoCallController?.connect(
                videoRoomSID = videoRoomSID,
                accessToken = videoAccessTokenResponse.accessToken
            )
        }

        isConnected = !isConnected
    }

    ActiveCallControl(
        iconResource = R.drawable.ic_phone_solid,
        iconRotateDegrees = 135f,
        background = Green200,
        controlDescription = "endCall",
        onClick = { startCall() }
    )
}

@Composable
fun EndCallControl(hdVideo: VideoCallController?) {
    var isConnected by remember {
        mutableStateOf(true)
    }

    ActiveCallControl(
        iconResource = R.drawable.ic_phone_solid,
        iconRotateDegrees = 135f,
        background = Red500,
        controlDescription = "endCall",
        onClick = {
            hdVideo?.disconnect()

            isConnected = !isConnected
        }
    )
}

@Composable
fun ToggleVideoStateControl(hdVideo: VideoCallController?) {
    var isVideoEnabled by remember {
        mutableStateOf(true)
    }

    val videoStateIcon = if (isVideoEnabled) R.drawable.ic_video_solid else R.drawable.ic_video_slash_solid

    ActiveCallControl(
        iconResource = videoStateIcon,
        controlDescription = "toggleVideo",
        onClick = {
            hdVideo?.localVideoController?.setVideoEnabled(!isVideoEnabled)
            isVideoEnabled = !isVideoEnabled
        }
    )
}

@Composable
fun ToggleAudioStateControl(hdVideo: VideoCallController?) {
    var isAudioEnabled by remember {
        mutableStateOf(true)
    }

    val audioStateIcon = if (isAudioEnabled) R.drawable.ic_microphone_solid else R.drawable.ic_microphone_slash_solid

    ActiveCallControl(
        iconResource = audioStateIcon,
        controlDescription = "toggleAudio",
        onClick = {
            hdVideo?.localAudioController?.setAudioEnabled(!isAudioEnabled)
            isAudioEnabled = !isAudioEnabled
        }
    )
}

@Composable
fun ToggleCameraStateControl(hdVideo: VideoCallController?) {
    var selectedCamera by remember {
        mutableStateOf("front")
    }

    ActiveCallControl(
        iconResource = R.drawable.ic_arrows_rotate_solid,
        iconRotateDegrees = if (selectedCamera == "front") 0f else 90f,
        controlDescription = "toggleAudio",
        onClick = {
            hdVideo?.cameraController?.switchCamera()
            selectedCamera = if (selectedCamera == "front") "back" else "front"
        }
    )
}

@Composable
fun ActiveCallControl(
    iconResource: Int,
    iconRotateDegrees: Float = 0f,
    controlDescription: String,
    background: Color = Gray500,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.padding(12.dp)
    ) {
        Box(modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable { onClick() }
        ) {
            Icon(
                painter = painterResource(id = iconResource),
                contentDescription = controlDescription,
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
                    .rotate(iconRotateDegrees)
                    .align(Alignment.Center)
            )
        }
    }
}