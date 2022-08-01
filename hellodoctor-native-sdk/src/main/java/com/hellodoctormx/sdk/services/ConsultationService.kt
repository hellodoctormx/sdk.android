package com.hellodoctormx.sdk.services

import android.content.Context
import com.hellodoctormx.sdk.types.Availability
import com.hellodoctormx.sdk.types.Consultation
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ConsultationService(context: Context) : HelloDoctorHTTTPClient(context) {
    suspend fun getUserConsultations(limit: Int): GetUserConsultationsResponse {
        return this.get(
            path = "/consultations?limit=$limit"
        )
    }

    suspend fun getCallCenterAvailability(specialty: String, start: ZonedDateTime, end: ZonedDateTime): GetAvailabilityResponse {
        val formattedStart = start.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val formattedEnd = end.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        return this.get("/consultations/call-center/availability?specialty=$specialty&start=$formattedStart&end=$formattedEnd")
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

    @Serializable
    data class GetAvailabilityResponse(val availableTimes: List<Availability>)
}
