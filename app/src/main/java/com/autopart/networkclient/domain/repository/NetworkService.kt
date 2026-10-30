/*
 * Copyright (c) Koninklijke Advance N.V.,2025
 * All rights reserved
 */
package com.autopart.networkclient.domain.repository


import com.autopart.networkclient.data.remote.network.model.NetworkResult
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

class NetworkService {
    suspend fun processApiResponse(
        response: NetworkResult<Any>,
        source: String
    ): NetworkResult<String?> {
        return when (response) {
            is NetworkResult.Success -> {
                val httpResponse = response.data as? HttpResponse
                val responseBody = httpResponse?.body<String>()
                NetworkResult.Success(responseBody)
            }

            is NetworkResult.Error -> NetworkResult.Error(response.details)
            else -> {NetworkResult.Error(null)}
        }
    }

    suspend fun processNonEssentialApiResponse(
        response: NetworkResult<Any>,
        source: String
    ): String? {
        val processedResponse = processApiResponse(
            response = response,
            source = source
        )
        return when (processedResponse) {
            is NetworkResult.Success -> processedResponse.data

            is NetworkResult.Error -> null
            else -> null
        }
    }
}
