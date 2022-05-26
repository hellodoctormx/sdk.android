package com.hellodoctormx.sdk.api

import android.content.Context
import com.hellodoctormx.sdk.types.Consultation
import kotlinx.serialization.Serializable

class ConsultationsAPI(context: Context) : HelloDoctorHTTTPClient(context) {
    suspend fun getUserConsultations(limit: Int): GetUserConsultationsResponse {
        return this.get(
            path = "/consultations?limit=$limit"
        )
    }

    @Serializable
    data class GetUserConsultationsResponse(val consultations: List<Consultation>)
}
