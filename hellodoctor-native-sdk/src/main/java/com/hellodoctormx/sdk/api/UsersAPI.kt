package com.hellodoctormx.sdk.api

import android.content.Context
import com.hellodoctormx.sdk.types.Consultation
import kotlinx.serialization.Serializable

class UsersAPI(context: Context) : HelloDoctorHTTTPClient(context) {
    suspend fun createUser(email: String): CreateUserResponse {
        return this.post(
            path = "/users",
            postData = mutableMapOf(
                "email" to email,
            )
        )
    }

    suspend fun authenticateUser(userID: String, refreshToken: String): AuthenticateUserResponse {
        return this.post(
            path = "/users/$userID/_authenticate",
            postData = mutableMapOf("refreshToken" to refreshToken)
        )
    }

    @Serializable
    data class CreateUserResponse(val uid: String)

    @Serializable
    data class AuthenticateUserResponse(val bearerToken: String, val refreshToken: String)
}
