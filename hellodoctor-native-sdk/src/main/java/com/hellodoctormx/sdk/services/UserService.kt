package com.hellodoctormx.sdk.services

import android.content.Context
import kotlinx.serialization.Serializable

class UserService(context: Context) : HelloDoctorHTTTPClient(context) {
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
