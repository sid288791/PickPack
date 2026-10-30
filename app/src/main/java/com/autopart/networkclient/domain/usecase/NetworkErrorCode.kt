/*
 * Copyright (c) Koninklijke Philips N.V.,2026
 * All rights reserved
 */

package com.autopart.networkclient.domain.usecase

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Enumerates known network error codes returned by the ecommerce services.
 *
 * The string *code* value from the backend is mapped to a strongly-typed [NetworkErrorCode]
 * allowing callers to use exhaustive `when` expressions without relying on magic strings.
 *
 * Supports both numeric codes (e.g., "401") and enum names (e.g., "INVALID_AUTHORIZATION")
 * for backward compatibility.
 *
 * Unknown or unexpected codes are mapped to [UNKNOWN].
 */
@Serializable(with = NetworkErrorCodeSerializer::class)
enum class NetworkErrorCode(val code: String) {
    BAD_REQUEST("400"),
    INVALID_AUTHORIZATION("401"),
    FORBIDDEN("403"),
    NOT_FOUND("404"),
    INTERNAL_SERVER_ERROR("500"),
    SERVICE_UNAVAILABLE("503"),
    GENERIC_ERROR("5555"),
    PARSING_ERROR("422"),
    REGISTRATION_ID_EMPTY("6666"),
    ERROR_REGISTERED_PRODUCT_NULL("7777"),
    /** Represents any code that is not explicitly handled above. */
    UNKNOWN("");

    companion object {
        /**
         * Returns the matching [NetworkErrorCode] for the given backend string [code].
         * Supports both numeric codes ("401") and enum names ("INVALID_AUTHORIZATION").
         * If the code is null or not recognised, [UNKNOWN] is returned.
         */
        fun from(code: String?): NetworkErrorCode {
            if (code == null) return UNKNOWN

            // Try to match by numeric code first
            entries.firstOrNull { it.code == code }?.let { return it }

            // Try to match by enum name
            return try {
                valueOf(code)
            } catch (_: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}

/**
 * Custom serializer for NetworkErrorCode that supports both:
 * 1. Numeric codes: "401", "404", etc.
 * 2. Enum names: "INVALID_AUTHORIZATION", "NOT_FOUND", etc.
 */
object NetworkErrorCodeSerializer : KSerializer<NetworkErrorCode> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NetworkErrorCode", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NetworkErrorCode) {
        // Always serialize as enum name for consistency
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): NetworkErrorCode {
        val value = decoder.decodeString()
        return NetworkErrorCode.from(value)
    }
}

