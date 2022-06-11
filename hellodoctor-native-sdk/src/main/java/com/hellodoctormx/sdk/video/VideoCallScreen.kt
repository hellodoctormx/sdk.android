package com.hellodoctormx.sdk.video

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.R
import com.hellodoctormx.sdk.ui.theme.Blue500
import com.hellodoctormx.sdk.ui.theme.Blue700
import com.hellodoctormx.sdk.ui.theme.HelloDoctorSDKTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoCallScreen(
    videoCallModel: VideoCallModel,
    isConnected: Boolean = false
) {
    if (isConnected) ActiveVideoCallScreen(videoCallModel)
    else IncomingVideoCallScreen(videoCallModel)
}

@Composable
fun IncomingVideoCallScreen(videoCallModel: VideoCallModel) {
    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        if (videoCallModel.isConnected) {
            ActiveVideoCallScreen(videoCallModel = videoCallModel)
        } else {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.9f)) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    IncomingVideoCallHeader()
                    Box(
                        modifier = Modifier
                            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 6.dp)
                            .alpha(0.5f)
                            .fillMaxHeight(0.5f)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        if (!videoCallModel.isPreview) LocalParticipantAndroidView()
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        ToggleCameraEnabledButton(videoCallModel, size = 58.dp)
                        ToggleMicrophoneEnabledButton(videoCallModel, size = 58.dp)
                        ToggleCameraButton(videoCallModel, size = 58.dp)
                    }
                    IncomingVideoCallControls(videoCallModel)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncomingVideoCallScreenPreview() {
    HelloDoctorSDKTheme {
        IncomingVideoCallScreen(VideoCallModel(isPreview = true))
    }
}

@Preview(showBackground = true)
@Composable
fun ActiveVideoCallScreenPreview() {
    HelloDoctorSDKTheme {
        ActiveVideoCallScreen(VideoCallModel(isPreview = true))
    }
}

@Composable
fun HelloDoctorLogo(height: Dp = 36.dp) {
    Image(
        painter = painterResource(id = R.drawable.hd_logo_white),
        contentDescription = null,
        modifier = Modifier
            .height(height)
            .padding(bottom = 3.dp)
    )
}

@Composable
fun IncomingVideoCallHeader() {
    HelloDoctorLogo()
    Box(modifier = Modifier.padding(6.dp)) {
        Image(
            painter = rememberAsyncImagePainter(HelloDoctorClient.IncomingVideoCall.callerPhotoURL),
            contentDescription = null,
            modifier = Modifier
                .size(128.dp)
                .border(width = 2.dp, color = Blue700, shape = CircleShape)
                .clip(CircleShape)
        )
    }
    Text(
        text = HelloDoctorClient.IncomingVideoCall.callerDisplayName
            ?: "Médico de HelloDoctor",
        color = Color.White,
        fontSize = 24.sp
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ActiveVideoCallScreen(videoCallModel: VideoCallModel) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val callerDisplayName =
        HelloDoctorClient.IncomingVideoCall.callerDisplayName ?: "HelloDoctor Médico"

    LaunchedEffect(Unit) {
        IncomingVideoCallNotification.cancel(context)

        if (!videoCallModel.isConnected) {
            videoCallModel.doConnect(context)
        }
    }

    LaunchedEffect(videoCallModel.areControlsVisible) {
        if (videoCallModel.areControlsVisible) {
            launch {
                delay(4000L)
                videoCallModel.areControlsVisible = false
            }
        }
    }

    AnimatedVisibility(visible = videoCallModel.isConnected, enter = fadeIn(), exit = fadeOut()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            onClick = { videoCallModel.toggleControls() }) {
            if (!videoCallModel.isPreview) RemoteParticipantAndroidView()
            else Box(
                modifier = Modifier
                    .background(Blue500)
                    .fillMaxSize()
                    .zIndex(0f)
            )
            LocalParticipantPortal {
                if (!videoCallModel.isPreview) LocalParticipantAndroidView()
                else Box {}
            }
            AnimatedVisibility(
                visible = videoCallModel.areControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(top = 18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HelloDoctorLogo()
                    Box(modifier = Modifier.padding(24.dp)) {
                        CallerPhoto()
                    }
                    Text(
                        text = callerDisplayName,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                }
            }
            AnimatedVisibility(
                visible = videoCallModel.areControlsVisible,
                enter = slideInVertically(initialOffsetY = { with(density) { 20.dp.roundToPx() } }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { with(density) { 20.dp.roundToPx() } }) + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    ActiveVideoCallControls(videoCallModel)
                }
            }
        }
    }
}

@Composable
fun CallerPhoto() {
    val callerPhotoURL = HelloDoctorClient.IncomingVideoCall.callerPhotoURL
        ?: "https://firebasestorage.googleapis.com/v0/b/hellodoctor-production-303000-public/o/icon512.png?alt=media&token=78c8d2dc-2e3b-49a2-87f5-7a6d6cfab7eb"

    Image(
        painter = rememberAsyncImagePainter(callerPhotoURL),
        contentDescription = null,
        modifier = Modifier
            .size(96.dp)
            .border(width = 1.dp, color = Blue700, shape = CircleShape)
            .clip(CircleShape)
    )
}

@Composable
fun LocalParticipantPortal(content: @Composable () -> Unit = { }) {
    Column(verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxSize()) {
        Card(
            elevation = 4.dp,
            modifier = Modifier
                .padding(12.dp)
                .width(148.dp)
                .height((1.4 * 148).dp)
                .clip(RoundedCornerShape(6.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                content()
            }
        }
    }
}
