package com.hellodoctormx.foodpass

import android.app.PendingIntent
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
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hellodoctormx.foodpass.ui.theme.FoodPassTheme
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.video.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

const val HELLO_DOCTOR_API_KEY = "Ax3JVY2pal5f8i6NwLNX3wjssyiR46u7itHypjZe"
const val HELLO_DOCTOR_TEST_TOKEN = "NOT_A_TOKEN"
const val INCOMING_CALL_CHANNEL_ID = "incoming_call"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = HomeScreenViewModel()

        HelloDoctorClient.configure(HELLO_DOCTOR_API_KEY, "http://192.168.100.26:3009")

        val notificationManager = NotificationManagerCompat.from(this)

        val channel = NotificationChannelCompat
            .Builder(INCOMING_CALL_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName("Incoming calls")
            .setDescription("Incoming video call alerts")
            .build()

        notificationManager.createNotificationChannel(channel)

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
            onClick = { displayIncomingCallNotification(context) }
        ) {
            Text("test")
        }
    }
}

fun displayIncomingCallNotification(context: Context) {
    VideoCallController.getInstance(context).apply {
        localAudioController.setRingtonePlaying(true)
    }

    val answerCallIntent = Intent(context, IncomingVideoCallActivity::class.java).apply {
        action = INCOMING_VIDEO_CALL_ACTION
        putExtra(INCOMING_VIDEO_CALL_STATE, "answered")
        putExtra(VIDEO_ROOM_SID, "RMb81ada7840f1a0d3ab592784dc35f46e")
        putExtra(CALLER_DISPLAY_NAME, "Daniel Doctor")
    }

    val answerCallPendingIntent = PendingIntent.getActivity(context, 0, answerCallIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    val answerCallAction = NotificationCompat.Action.Builder(
        IconCompat.createWithResource(context, com.hellodoctormx.sdk.R.drawable.ic_video_solid),
        "Accept",
        answerCallPendingIntent
    ).build()

    val notification = NotificationCompat
        .Builder(context, INCOMING_CALL_CHANNEL_ID)
        .setSmallIcon(com.hellodoctormx.sdk.R.drawable.ic_phone_solid)
        .setContentTitle("Incoming call")
        .setContentText("Dr. Daniel Sarfati")
        .setOngoing(true)
        .addAction(answerCallAction)
        .setFullScreenIntent(answerCallPendingIntent, true)
        .build()

    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(INCOMING_VIDEO_CALL_NOTIFICATION_ID, notification)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeScreen(HomeScreenViewModel())
}