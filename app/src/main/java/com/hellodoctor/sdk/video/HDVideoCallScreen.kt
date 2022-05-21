package com.hellodoctor.sdk.video

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.hellodoctor.sdk.ui.theme.HelloDoctorSDKTheme

class HDVideoCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HDVideoCallScreen()
        }
    }
}

@Composable
fun HDVideoCallScreen() {
    var hdVideo: HDVideo? by remember {
        mutableStateOf(null)
    }

    HelloDoctorSDKTheme {
        HDVideoPermissions(
            content = {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black,
                ) {
                    AndroidView(factory = { context -> HDVideoLocalView(context).apply {
                        hdVideo = HDVideo.getInstance(context, context.getActivity())
                        hdVideo?.setLocalView(this)
                        hdVideo?.prepareLocalMedia()
                    }})
                    Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(2.dp)) {
                        HDVideoCallControlsComponent(hdVideo)
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HDVideoCallScreenPreview() {
    HDVideoCallScreen()
}