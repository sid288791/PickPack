/*
 * Copyright (c) Koninklijke Advance N.V.,2025
 * All rights reserved
 */
package com.autopart.networkclient.data.remote.network.util

import com.autopart.networkclient.data.remote.network.model.NetworkResult
import com.autopart.networkclient.domain.usecase.NetworkErrorCode
import com.autopart.networkclient.domain.usecase.network.NetworkError
import com.autopart.networkclient.domain.usecase.network.NetworkErrorResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/**
 * Handler for HTTP responses
 */
object ResponseHandler {
    /**
     * Handle HTTP response and convert to NetworkResult
     *
     * @param response HTTP response
     * @return NetworkResult containing the response data or error
     */
    suspend inline fun <reified T> handleResponse(response: HttpResponse): NetworkResult<T> {
        return try {
            when {
                response.status.isSuccess() -> {
                    try {
                        val responseBody = response.body<T>()
                        NetworkResult.Success(responseBody)
                    } catch (e: Exception) {
                        NetworkResult.Error(
                            details = NetworkErrorResponse(
                                title = "Response Parsing Error",
                                status = response.status.value.toString(),
                                code = NetworkErrorCode.PARSING_ERROR
                            )
                        )
                    }
                }

                else -> {
                    try {
                        val errorBody = response.body<NetworkError>()
                        NetworkResult.Error(
                            details = errorBody.errors?.get(0)
                        )
                    } catch (e: Exception) {
                        NetworkResult.Error(
                            details = NetworkErrorResponse(
                                title = "Error Response Parsing Failed $e",
                                status = response.status.value.toString()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                details = NetworkErrorResponse(
                    title = e.message,
                    status = response.status.value.toString(),
                )
            )
        }
    }
}
