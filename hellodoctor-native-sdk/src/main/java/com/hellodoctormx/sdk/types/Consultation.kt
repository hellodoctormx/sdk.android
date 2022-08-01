package com.hellodoctormx.sdk.types

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime
import java.time.ZonedDateTime

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = ZonedDateTime::class)
object KZonedDateTimeSerializer : KSerializer<ZonedDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): ZonedDateTime {
        val string = decoder.decodeString()
        return ZonedDateTime.parse(string)
    }
}

@Serializable
data class Consultation(val id: String, @Contextual val scheduledStart: LocalDateTime, val status: String)

@Serializable
data class AvailabilityInterval(
    @Serializable(with=KZonedDateTimeSerializer::class) val start: ZonedDateTime,
    @Serializable(with=KZonedDateTimeSerializer::class) val end: ZonedDateTime)

@Serializable
data class Availability(val interval: AvailabilityInterval, val total: Int)
