package com.hellodoctormx.sdk.video

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.hellodoctormx.sdk.ui.theme.HelloDoctorSDKTheme

class VideoCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoCallScreen()
        }
    }
}

@Composable
fun VideoCallScreen() {
    var videoCallController: VideoCallController? by remember {
        mutableStateOf(null)
    }

    HelloDoctorSDKTheme {
        VideoCallPermissions(
            content = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black,
                ) {
                    AndroidView(factory = { context -> LocalParticipantView(context).apply {
                        videoCallController = VideoCallController(context)
                        videoCallController?.setLocalParticipantView(this)
                        videoCallController?.startLocalCapture()
                    }})
                    Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(2.dp)) {
                        ActiveVideoCallControls(videoCallController)
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VideoCallScreenPreview() {
    VideoCallScreen()
}