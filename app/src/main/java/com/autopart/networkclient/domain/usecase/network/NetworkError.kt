/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.networkclient.domain.usecase.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkError(
    @SerialName(value = "errors")  val errors: List<NetworkErrorResponse>? = null
)
