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
    activeVideoCallModel: ActiveVideoCallModel,
    isConnected: Boolean = false
) {
    if (isConnected) ActiveVideoCallScreen(activeVideoCallModel)
    else IncomingVideoCallScreen(activeVideoCallModel)
}

@Composable
fun IncomingVideoCallScreen(
    activeVideoCallModel: ActiveVideoCallModel,
    isPreview: Boolean? = false
) {
    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        if (activeVideoCallModel.isConnected) {
            ActiveVideoCallScreen(activeVideoCallModel = activeVideoCallModel)
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
                        if (isPreview != true) LocalParticipantAndroidView()
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        ToggleCameraEnabledButton(activeVideoCallModel, size = 58.dp)
                        ToggleMicrophoneEnabledButton(activeVideoCallModel, size = 58.dp)
                        ToggleCameraButton(activeVideoCallModel, size = 58.dp)
                    }
                    IncomingVideoCallControls(activeVideoCallModel)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncomingVideoCallScreenPreview() {
    HelloDoctorSDKTheme {
        IncomingVideoCallScreen(activeVideoCallModel = ActiveVideoCallModel(), isPreview = true)
    }
}

@Preview(showBackground = true)
@Composable
fun ActiveVideoCallScreenPreview() {
    HelloDoctorSDKTheme {
        ActiveVideoCallScreen(ActiveVideoCallModel(), isPreview = true)
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
fun ActiveVideoCallScreen(activeVideoCallModel: ActiveVideoCallModel, isPreview: Boolean? = false) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val callerDisplayName =
        HelloDoctorClient.IncomingVideoCall.callerDisplayName ?: "HelloDoctor Médico"

    LaunchedEffect(Unit) {
        IncomingVideoCallNotification.cancel(context)

        if (!activeVideoCallModel.isConnected) {
            activeVideoCallModel.doConnect(context)
        }
    }

    LaunchedEffect(activeVideoCallModel.areControlsVisible) {
        if (activeVideoCallModel.areControlsVisible) {
            launch {
                delay(4000L)
                activeVideoCallModel.areControlsVisible = false
            }
        }
    }

    AnimatedVisibility(visible = activeVideoCallModel.isConnected, enter = fadeIn(), exit = fadeOut()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black,
            onClick = { activeVideoCallModel.toggleControls() }) {
            if (isPreview != true) RemoteParticipantAndroidView()
            if (isPreview == true) Box(
                modifier = Modifier
                    .background(Blue500)
                    .fillMaxSize()
                    .zIndex(0f)
            )
            LocalParticipantPortal {
                if (isPreview != true) LocalParticipantAndroidView()
                else Box {}
            }
            AnimatedVisibility(
                visible = activeVideoCallModel.areControlsVisible,
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
                visible = activeVideoCallModel.areControlsVisible,
                enter = slideInVertically(initialOffsetY = { with(density) { 20.dp.roundToPx() } }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { with(density) { 20.dp.roundToPx() } }) + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    ActiveVideoCallControls(activeVideoCallModel)
                }
            }
        }
    }
}

@Composable
fun CallerPhoto() {
    val callerPhotoURL = HelloDoctorClient.IncomingVideoCall.callerPhotoURL
        ?: "https://storage.googleapis.com/hellodoctor-staging-uploads/cc3823ec-c046-4e24-bcd5-da65a7012759-"

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
