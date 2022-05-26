package com.hellodoctormx.examples.foodpass

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hellodoctormx.examples.foodpass.FPFirebaseMessagingService.Companion.registerFirebaseMessagingToken
import com.hellodoctormx.examples.foodpass.ui.theme.FoodPassTheme
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.HelloDoctorClient.IncomingVideoCall.videoRoomSID
import com.hellodoctormx.sdk.video.CALLER_DISPLAY_NAME
import com.hellodoctormx.sdk.video.INCOMING_VIDEO_CALL_ACTION
import com.hellodoctormx.sdk.video.VIDEO_ROOM_SID
import kotlinx.coroutines.launch

const val HELLO_DOCTOR_API_KEY = "NOT_A_KEY"
const val HELLO_DOCTOR_TEST_TOKEN = "NOT_A_TOKEN"

class MainActivity : ComponentActivity() {
    private val tag = "FoodPass"

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        HelloDoctorClient.configure(HELLO_DOCTOR_API_KEY, "http://192.168.100.26:3010")

        val vm = HomeScreenViewModel()

        setContent {
            HomeScreen(vm)
        }
    }

    public override fun onStart() {
        super.onStart()

        if (auth.currentUser == null){
            authenticateTestUser()
        } else {
            registerFirebaseMessagingToken()
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

fun launchCall(currentActivity: Context) {
    val launchCallIntent = Intent(currentActivity, FPIncomingVideoCallActivity::class.java).apply {
        putExtra(VIDEO_ROOM_SID, "RM3b74771ad32b0cfcb76087fa9e4fa61e")
        putExtra(CALLER_DISPLAY_NAME, "Dr. Daniel Tester")
        putExtra(INCOMING_VIDEO_CALL_ACTION, "answered")
    }

    currentActivity.startActivity(launchCallIntent)
}

class HomeScreenViewModel : ViewModel() {
    fun doSignIn(context: Context, currentUser: FirebaseUser) {
        viewModelScope.launch {
            val client = HelloDoctorClient(context)
            client.signIn(currentUser.uid, HELLO_DOCTOR_TEST_TOKEN)
            launchCall(context)
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
        Text("FoodPass")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeScreen(HomeScreenViewModel())
}