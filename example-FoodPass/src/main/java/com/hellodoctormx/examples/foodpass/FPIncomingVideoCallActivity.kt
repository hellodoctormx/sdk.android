package com.hellodoctormx.examples.foodpass

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.video.IncomingVideoCallActivity
import kotlinx.coroutines.launch

class FPIncomingVideoCallActivity: IncomingVideoCallActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth

        HelloDoctorClient.configure(HELLO_DOCTOR_API_KEY, "http://192.168.100.26:3010")

        val context = this

        Firebase.auth.currentUser?.let { currentUser ->
            lifecycleScope.launch {
                val client = HelloDoctorClient(context)
                client.signIn(currentUser.uid, HELLO_DOCTOR_TEST_TOKEN)
            }
        }

        super.onCreate(savedInstanceState)
    }
}
