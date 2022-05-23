package com.hellodoctormx.sdk.clients

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.hellodoctormx.sdk.auth.HDCurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONException
import org.json.JSONObject

abstract class AbstractServiceClient(
    val context: Context,
    val host: String? = defaultServiceHost
) {
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

    suspend inline fun <reified T> doRequest(method: Int, path: String, data: MutableMap<Any, Any>?): T {
        val responseChannel = Channel<String>()

        val url = "$host$path"

        val jsonPostData = if (data == null) JSONObject() else JSONObject(data as Map<Any, Any>)

        Log.i("AbstractServiceClient:DOING_REQUEST","[$url]")
        val jsonObjectRequest = object : JsonObjectRequest(
            method,
            url,
            jsonPostData,
            Response.Listener { response -> // response listener
                Log.i("AbstractServiceClient:GOT_RESPONSE","[$response]")
                try {
                    val obj: JSONObject = response
                    runBlocking {
                        responseChannel.send(obj.toString())
                    }

                }catch (e: JSONException){
                    Log.w("AbstractServiceClient:JSONException","[doRequest:$method:$url:ERROR] ${e.message}")
                }
            },
            Response.ErrorListener {
                Log.w("AbstractServiceClient","[doRequest:$method:$url:ERROR] ${it.message}")
                throw it
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return getAuthorizationHeaders()
            }
        }

        val queue = Volley.newRequestQueue(context)
        queue.add(jsonObjectRequest)

        val response = responseChannel.receive()

        return Json.decodeFromString(response)
    }

    fun getAuthorizationHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Content-Type"] = "application/json"

        HDCurrentUser.jwt?.let {
            headers["Authorization"] = "Bearer $it"
        }

        apiKey?.let {
            headers["X-Api-Key"] = it
        }

        return headers
    }

    companion object {
        var apiKey: String? = null
        var defaultServiceHost: String? = null
    }
}