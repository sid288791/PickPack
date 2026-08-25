/*
 * Copyright (c) Koninklijke Philips N.V.,2025
 * All rights reserved
 */

package com.autopart.networkclient.domain.usecase.network
import com.autopart.networkclient.domain.usecase.NetworkErrorCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents an error response from the network.
 * This data class is used to deserialize error responses from API calls.
 * It follows the standard error response format used by the ecommerce services.
 *
 * @property id Optional unique identifier for the error
 * @property code Optional error code, typically used for categorizing errors
 * @property status Optional status code or message (e.g., "404", "Failed")
 * @property title Optional human-readable error title or message
 * @property endpoint Optional endpoint that generated the error
 *
 * @see NetworkResult.Error
 * @see ResponseHandler
 */



@Serializable
data class NetworkErrorResponse(
    @SerialName(value = "id") val id: String? = null,
    @SerialName(value = "code") var code: NetworkErrorCode = NetworkErrorCode.UNKNOWN,
    @SerialName(value = "status") val status: String? = null,
    @SerialName(value = "title") var title: String? = null,
    @SerialName(value = "endpoint") var endpoint: String? = null,
    val source: Source? = null
) {
    companion object {
        fun createError(
            exception: Exception,
            code: String? = null
        ): NetworkErrorResponse {
            return NetworkErrorResponse(
                code = NetworkErrorCode.from(code),
                title = exception.message ?: "Unknown error occurred"
            )
        }
    }
}




