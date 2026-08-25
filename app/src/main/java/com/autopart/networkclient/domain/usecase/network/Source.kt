package com.autopart.networkclient.domain.usecase.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Source(
    @SerialName(value = "parameter") val parameter: String? = null
)
