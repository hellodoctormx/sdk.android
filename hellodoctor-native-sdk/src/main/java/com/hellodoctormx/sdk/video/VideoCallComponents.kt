package com.hellodoctormx.sdk.video

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.hellodoctormx.sdk.R
import com.hellodoctormx.sdk.ui.theme.Gray500
import com.hellodoctormx.sdk.ui.theme.Gray900
import com.hellodoctormx.sdk.ui.theme.Green200
import com.hellodoctormx.sdk.ui.theme.Red500
import com.hellodoctormx.sdk.video.ui.theme.HelloDoctorSDKTheme

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Preview(showBackground = true)
@Composable
fun ActiveVideoCallControlsPreview() {
    HelloDoctorSDKTheme {
        ActiveVideoCallControls(VideoCallModel())
    }
}

@Preview(showBackground = true)
@Composable
fun IncomingVideoCallControlsPreview() {
    HelloDoctorSDKTheme {
        IncomingVideoCallControls(VideoCallModel())
    }
}

@Composable
fun LocalParticipantAndroidView() {
    AndroidView(factory = { context ->
        LocalParticipantView(context).apply {
            val videoCallController = VideoCallController.getInstance(context)
            videoCallController.setLocalParticipantView(this)
            videoCallController.startLocalCapture()
        }
    })
}

@Composable
fun RemoteParticipantAndroidView() {
    AndroidView(factory = { context ->
        RemoteParticipantView(context).apply {
            val videoCallController = VideoCallController.getInstance(context)
            videoCallController.remoteParticipantView = this
        }
    }, modifier = Modifier.zIndex(0f))
}

@Composable
fun IncomingVideoCallControls(videoCallModel: VideoCallModel) {
    val context = LocalContext.current

    Surface(color = Color.Transparent) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 42.dp, end = 42.dp, bottom = 42.dp)
        ) {
            Button(
                onClick = { videoCallModel.doConnect(context) },
                colors = ButtonDefaults.buttonColors(backgroundColor = Green200),
                modifier = Modifier.fillMaxWidth().size(72.dp).padding(bottom = 24.dp)
            ) {
                Text(text = "Comenzar consulta", color = Color.White)
            }
            Button(
                onClick = { videoCallModel.doDisconnect(context) },
                colors = ButtonDefaults.buttonColors(backgroundColor = Red500),
                modifier = Modifier.fillMaxWidth().size(72.dp).padding(bottom = 24.dp)
            ) {
                Text(text = "Rechazar consulta", color = Color.White)
            }
        }
    }
}

@Composable
fun ActiveVideoCallControls(videoCallModel: VideoCallModel) {
    Surface(
        shape = CircleShape,
        modifier = Modifier.padding(15.dp),
        color = Gray900.copy(alpha = 0.75f)
    ) {
        Row(verticalAlignment = CenterVertically, modifier = Modifier.padding(9.dp)) {
            EndCallControl(videoCallModel)
            ToggleCameraEnabledButton(videoCallModel)
            ToggleMicrophoneEnabledButton(videoCallModel)
            ToggleCameraButton(videoCallModel)
        }
    }
}

@Composable
fun EndCallControl(videoCallModel: VideoCallModel) {
    val context = LocalContext.current

    ActiveCallControlButton(
        iconResource = R.drawable.ic_phone_solid,
        iconRotateDegrees = 135f,
        size = 64.dp,
        background = Red500,
        controlDescription = "end-call",
        onClick = { videoCallModel.doDisconnect(context) }
    )
}

@Composable
fun ToggleCameraEnabledButton(videoCallModel: VideoCallModel, size: Dp = 54.dp) {
    val context = LocalContext.current

    ActiveCallControlButton(
        iconResource = if (videoCallModel.isCameraEnabled) R.drawable.ic_video_solid else R.drawable.ic_video_slash_solid,
        size = size,
        controlDescription = "toggle-camera-enabled",
        onClick = {
            videoCallModel.toggleCameraEnabled(context)
        }
    )
}

@Composable
fun ToggleMicrophoneEnabledButton(videoCallModel: VideoCallModel, size: Dp = 54.dp) {
    val context = LocalContext.current

    ActiveCallControlButton(
        iconResource = if (videoCallModel.isMicrophoneEnabled) R.drawable.ic_microphone_solid else R.drawable.ic_microphone_slash_solid,
        size = size,
        controlDescription = "toggle-microphone-enabled",
        onClick = {
            videoCallModel.toggleMicrophoneEnabled(context)
        }
    )
}

@Composable
fun ToggleCameraButton(videoCallModel: VideoCallModel, size: Dp = 54.dp) {
    val context = LocalContext.current

    ActiveCallControlButton(
        size = size,
        iconResource = R.drawable.ic_arrows_rotate_solid,
        iconRotateDegrees = if (videoCallModel.activeCamera == "front") 0f else 90f,
        controlDescription = "toggle-camera",
        onClick = {
            videoCallModel.toggleCamera(context)
        }
    )
}

@Composable
fun ActiveCallControlButton(
    iconResource: Int,
    iconRotateDegrees: Float = 0f,
    controlDescription: String,
    background: Color = Gray500,
    size: Dp = 48.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.padding(12.dp)
    ) {
        Box(modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable { onClick() }
        ) {
            Icon(
                painter = painterResource(id = iconResource),
                contentDescription = controlDescription,
                tint = Color.White,
                modifier = Modifier
                    .size(size / 2)
                    .padding(2.dp)
                    .rotate(iconRotateDegrees)
                    .align(Alignment.Center)
            )
        }
    }
}
