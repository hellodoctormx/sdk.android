
package com.hellodoctormx.foodpass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellodoctormx.foodpass.ui.theme.FoodPassTheme
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.video.CALLER_DISPLAY_NAME
import com.hellodoctormx.sdk.video.INCOMING_VIDEO_CALL_STATE
import com.hellodoctormx.sdk.video.IncomingVideoCallActivity
import com.hellodoctormx.sdk.video.VIDEO_ROOM_SID
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val HELLO_DOCTOR_API_KEY = "Ax3JVY2pal5f8i6NwLNX3wjssyiR46u7itHypjZe"
const val HELLO_DOCTOR_TEST_TOKEN = "NOT_A_TOKEN"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = HomeScreenViewModel()

        HelloDoctorClient.configure(HELLO_DOCTOR_API_KEY, "http://192.168.100.26:3009")

        setContent {
            HomeScreen(vm)
        }
    }
}

class HomeScreenViewModel : ViewModel() {
    fun doSignIn(context: Context) {
        viewModelScope.launch {
            val client = HelloDoctorClient(context)
            client.signIn("ZUXfYyKlEUbFV1qTy9GpyYbL04q1", HELLO_DOCTOR_TEST_TOKEN)
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.doSignIn(context)
    }

    FoodPassTheme {
        Button(
            onClick = {
                val intent = Intent(context, IncomingVideoCallActivity::class.java).apply {
                    action = "com.hellodoctor.sdk.action.INCOMING_VIDEO_CALL"
                    putExtra(INCOMING_VIDEO_CALL_STATE, "answered")
                    putExtra(VIDEO_ROOM_SID, "RMb81ada7840f1a0d3ab592784dc35f46e")
                    putExtra(CALLER_DISPLAY_NAME, "Daniel Doctor")
                }

                context.startActivity(intent)
            }
        ) {
            Text("test")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeScreen(HomeScreenViewModel())
}