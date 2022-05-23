package com.hellodoctormx.sdk.clients

import android.content.Context
import com.hellodoctormx.sdk.types.Consultation
import kotlinx.serialization.Serializable

const val LOCAL_USER_SERVICE_HOST = "http://192.168.100.26:3009"
//const val LOCAL_USER_SERVICE_HOST = "https://c798-2806-2f0-93a0-dad9-8503-160d-13a7-c712.ngrok.io"

class UserServiceClient(context: Context, host: String? = LOCAL_USER_SERVICE_HOST) : AbstractServiceClient(context, host = host) {
    @Serializable
    data class CreateUserResponse(val uid: String)
    suspend fun createUser(thirdPartyUserID: String, email: String, phoneNumber: String?): CreateUserResponse {
        return this.post(
            path = "/third-party/users",
            postData = mutableMapOf(
                "role" to "patient",
                "thirdPartyUserID" to thirdPartyUserID,
                "email" to email,
                "phoneNumber" to (phoneNumber ?: ""),
            )
        )
    }

    @Serializable
    data class AuthenticateUserResponse(val jwt: String, val refreshToken: String)
    suspend fun authenticateUser(userID: String, serverAuthToken: String): AuthenticateUserResponse {
        return this.post(
            path = "/third-party/users/$userID/_authenticate",
            postData = mutableMapOf("token" to serverAuthToken)
        )
    }

    @Serializable
    data class GetUserConsultationsResponse(val consultations: List<Consultation>)
    suspend fun getUserConsultations(limit: Int): GetUserConsultationsResponse {
        return this.get(
            path = "/third-party/users/consultations?limit=$limit"
        )
    }
}
