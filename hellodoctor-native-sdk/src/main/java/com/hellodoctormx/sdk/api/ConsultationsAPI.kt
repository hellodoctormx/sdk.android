package com.hellodoctormx.sdk.api

import android.content.Context
import com.hellodoctormx.sdk.types.Consultation
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ConsultationsAPI(context: Context) : HelloDoctorHTTTPClient(context) {
    suspend fun getUserConsultations(limit: Int): GetUserConsultationsResponse {
        return this.get(
            path = "/consultations?limit=$limit"
        )
    }

    suspend fun requestCallCenterConsultation(specialty: String, requestedStart: ZonedDateTime, reason: String) {
        val payload = mapOf(
            "consultationType" to "video",
            "requestMode" to "callCenter",
            "reason" to reason,
            "specialty" to specialty,
            "requestedTime" to mapOf(
                "start" to requestedStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "end" to requestedStart.plusMinutes(30).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            )
        )

        return this.post("/consultations/call-center/_request", payload)
    }

    @Serializable
    data class GetUserConsultationsResponse(val consultations: List<Consultation>)
}
