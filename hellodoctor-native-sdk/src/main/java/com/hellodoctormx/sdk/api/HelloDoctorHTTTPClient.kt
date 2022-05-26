package com.hellodoctormx.sdk.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.hellodoctormx.sdk.HelloDoctorClient
import com.hellodoctormx.sdk.auth.HDCurrentUser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject


abstract class HelloDoctorHTTTPClient(val context: Context) {
    val tag = "HelloDoctorHTTTPClient"

    suspend inline fun <reified T> get(path: String): T {
        return doRequest(Request.Method.GET, path, null)
    }

    suspend inline fun <reified T> post(path: String, postData: MutableMap<Any, Any>?): T {
        return doRequest(Request.Method.POST, path, postData)
    }

    suspend inline fun <reified T> put(path: String, postData: MutableMap<Any, Any>?): T {
        return doRequest(Request.Method.PUT, path, postData)
    }

    suspend inline fun <reified T> delete(path: String): T {
        return doRequest(Request.Method.DELETE, path, null)
    }

    suspend inline fun <reified T> doRequest(
        method: Int,
        path: String,
        data: MutableMap<Any, Any>?
    ): T = withContext(Dispatchers.IO) {
        val responseChannel = Channel<String>()

        val url = "${HelloDoctorClient.serviceHost}$path"

        val asyncRequest = async(Dispatchers.IO) {
            val jsonPostData = if (data == null) JSONObject() else JSONObject(data as Map<Any, Any>)

            val jsonObjectRequest = object : JsonObjectRequest(
                method,
                url,
                jsonPostData,
                {
                    launch(Dispatchers.IO) {
                        responseChannel.send(it.toString())
                        responseChannel.close()
                    }
                },
                {
                    Log.w(tag, "[doRequest:$method:$url:ERROR] ${it.message}")
                    throw it
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    return getAuthorizationHeaders()
                }
            }

            Log.v(tag, "[request] $jsonObjectRequest")

            with(Volley.newRequestQueue(context)) {
                add(jsonObjectRequest)
            }

            responseChannel.receive()
        }

        val response = asyncRequest.await()

        Log.v(tag, "[response] $response")

        return@withContext Json.decodeFromString(response)
    }

    fun getAuthorizationHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Content-Type"] = "application/json"

        HDCurrentUser.jwt?.let {
            headers["Authorization"] = "Bearer $it"
        }

        HelloDoctorClient.apiKey?.let {
            headers["X-Api-Key"] = it
        }

        return headers
    }
}