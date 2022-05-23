package com.hellodoctormx.examples.foodpass

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hellodoctormx.examples.foodpass.FPFirebaseMessagingService.Companion.registerFirebaseMessagingToken
import com.hellodoctormx.examples.foodpass.ui.theme.FoodPassTheme
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.video.*
import kotlinx.coroutines.launch

const val HELLO_DOCTOR_API_KEY = "Ax3JVY2pal5f8i6NwLNX3wjssyiR46u7itHypjZe"
const val HELLO_DOCTOR_TEST_TOKEN = "NOT_A_TOKEN"
const val INCOMING_CALL_CHANNEL_ID = "incoming_call"

class MainActivity : ComponentActivity() {
    private val tag = "FoodPass"

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        auth = Firebase.auth

        HelloDoctorClient.configure(HELLO_DOCTOR_API_KEY, "http://192.168.100.26:3009")

        val vm = HomeScreenViewModel()

        setContent {
            HomeScreen(vm)
        }
    }

    public override fun onStart() {
        super.onStart()

        if (auth.currentUser == null){
            authenticateTestUser()
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelID = getString(R.string.video_calls_default_channel_id)
            val channelName = getString(R.string.video_calls_channel)
            val descriptionText = getString(R.string.video_calls_channel_description)

            val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun authenticateTestUser() {
        auth.signInWithEmailAndPassword("fatisar@gmail.com", "Password123")
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    registerFirebaseMessagingToken()
                }
            }
    }
}

class HomeScreenViewModel : ViewModel() {
    fun doSignIn(context: Context, currentUser: FirebaseUser) {
        viewModelScope.launch {
            val client = HelloDoctorClient(context)
            client.signIn(currentUser.uid, HELLO_DOCTOR_TEST_TOKEN)
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Firebase.auth) {
        Firebase.auth.currentUser?.let {
            viewModel.doSignIn(context, it)
        }
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