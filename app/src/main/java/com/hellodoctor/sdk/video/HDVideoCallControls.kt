package com.hellodoctor.sdk.video

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
import com.hellodoctor.sdk.R
import com.hellodoctor.sdk.ui.theme.Gray500
import com.hellodoctor.sdk.ui.theme.Gray900
import com.hellodoctor.sdk.ui.theme.Green200
import com.hellodoctor.sdk.ui.theme.Red500
import com.hellodoctor.sdk.video.ui.theme.HelloDoctorSDKTheme

fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Preview(showBackground = true)
@Composable
fun HDVideoCallControlsPreview() {
    val currentContext = LocalContext.current
    val hdVideo = HDVideo.getInstance(currentContext, currentContext.getActivity())
    HDVideoCallControlsComponent(hdVideo)
}

@Composable
fun HDVideoCallControlsComponent(hdVideo: HDVideo?) {
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
fun EndCallControl(hdVideo: HDVideo?) {
    var isConnected by remember {
        mutableStateOf(true)
    }

    ActiveCallControl(
        iconResource = R.drawable.ic_phone_solid,
        iconRotateDegrees = 135f,
        background = if (isConnected) Red500 else Green200,
        controlDescription = "endCall",
        onClick = {
            if (isConnected) {
                hdVideo?.stopLocalCapture()
            } else {
                hdVideo?.startLocalCapture()
            }

            isConnected = !isConnected
        }
    )
}

@Composable
fun ToggleVideoStateControl(hdVideo: HDVideo?) {
    var isVideoEnabled by remember {
        mutableStateOf(true)
    }

    val videoStateIcon = if (isVideoEnabled) R.drawable.ic_video_solid else R.drawable.ic_video_slash_solid

    ActiveCallControl(
        iconResource = videoStateIcon,
        controlDescription = "toggleVideo",
        onClick = {
            hdVideo?.setVideoEnabled(!isVideoEnabled)
            isVideoEnabled = !isVideoEnabled
        }
    )
}

@Composable
fun ToggleAudioStateControl(hdVideo: HDVideo?) {
    var isAudioEnabled by remember {
        mutableStateOf(true)
    }

    val audioStateIcon = if (isAudioEnabled) R.drawable.ic_microphone_solid else R.drawable.ic_microphone_slash_solid

    ActiveCallControl(
        iconResource = audioStateIcon,
        controlDescription = "toggleAudio",
        onClick = {
            hdVideo?.setAudioEnabled(!isAudioEnabled)
            isAudioEnabled = !isAudioEnabled
        }
    )
}

@Composable
fun ToggleCameraStateControl(hdVideo: HDVideo?) {
    var selectedCamera by remember {
        mutableStateOf("front")
    }

    ActiveCallControl(
        iconResource = R.drawable.ic_arrows_rotate_solid,
        iconRotateDegrees = if (selectedCamera == "front") 0f else 90f,
        controlDescription = "toggleAudio",
        onClick = {
            hdVideo?.flipCamera()
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