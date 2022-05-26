package com.hellodoctormx.sdk.video

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.ui.theme.Blue500
import com.hellodoctormx.sdk.ui.theme.Blue700

@Composable
fun VideoCallScreen(
    activeVideoCallModel: ActiveVideoCallModel,
    callerDisplayName: String? = "HelloDoctor Médico",
    isConnected: Boolean = false
){
    if (isConnected) ActiveVideoCallScreen(activeVideoCallModel)
    else IncomingVideoCallScreen(activeVideoCallModel, callerDisplayName)
}

@Composable
fun IncomingVideoCallScreen(
    activeVideoCallModel: ActiveVideoCallModel,
    callerDisplayName: String? = "HelloDoctor Médico",
    isPreview: Boolean? = false
) {
    AnimatedVisibility(visible = true) {
        if (activeVideoCallModel.isConnected) {
            ActiveVideoCallScreen(activeVideoCallModel = activeVideoCallModel)
        } else {
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
    }
}

@Preview(showBackground = true)
@Composable
fun IncomingVideoCallScreenPreview() {
    IncomingVideoCallScreen(activeVideoCallModel = ActiveVideoCallModel(), isPreview = true)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ActiveVideoCallScreen(activeVideoCallModel: ActiveVideoCallModel, isPreview: Boolean? = false) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val maskAlpha: Float by animateFloatAsState(
        targetValue = if (activeVideoCallModel.areControlsVisible) 0.7f else 0f
    )

    val callerDisplayName = HelloDoctorClient.IncomingVideoCall.callerDisplayName ?: "HelloDoctor Médico"

    LaunchedEffect(Unit) {
        IncomingVideoCallNotification.cancel(context)

        if (!activeVideoCallModel.isConnected) {
            activeVideoCallModel.doConnect(context)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black, onClick = { activeVideoCallModel.toggleControls() }) {
        if (isPreview != true) RemoteParticipantAndroidView()
        if (isPreview == true) Box(modifier = Modifier.background(Blue500).fillMaxSize().zIndex(0f))
        LocalParticipantPortal(content = {
            if (isPreview != true) LocalParticipantAndroidView()
            else Box {}
        })
        Box(modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = maskAlpha)
            .background(Color.Black)) {
            Text(text = callerDisplayName, color = Color.White, modifier = Modifier.padding(48.dp))
        }
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            AnimatedVisibility(
                visible = activeVideoCallModel.areControlsVisible,
                enter = slideInVertically {
                    with(density) { 40.dp.roundToPx() }
                },
                exit = slideOutVertically {
                    with(density) { 40.dp.roundToPx() }
                }
            ) {
                ActiveVideoCallControls(activeVideoCallModel)
            }
        }
    }
}

@Composable
fun LocalParticipantPortal(content: @Composable () -> Unit = { }) {
    Column(verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxSize()) {
        Card(
            elevation = 4.dp,
            modifier = Modifier
                .padding(12.dp)
                .width(128.dp)
                .height((1.4 * 128).dp)
                .clip(RoundedCornerShape(6.dp))
                .border(2.dp, Blue700, RoundedCornerShape(6.dp))
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
