/*
 * Copyright (c) Koninklijke Advance N.V.,2025
 * All rights reserved
 */
package com.autopart.networkclient.data.remote.network.api

import com.autopart.networkclient.data.remote.network.client.NetworkClient
import com.autopart.networkclient.data.remote.network.model.NetworkResult
import com.autopart.networkclient.data.remote.network.model.RequestConfig
import com.autopart.networkclient.data.remote.network.util.ResponseHandler
import com.autopart.networkclient.domain.usecase.NetworkErrorCode
import com.autopart.networkclient.domain.usecase.network.NetworkErrorResponse


/**
 * Base API client that provides common functionality for all API clients.
 * This abstract class serves as the foundation for all API clients in the PRG module.
 * It encapsulates the common behavior for making network requests and handling responses.
 * 
 * @property networkClient The network client used to make HTTP requests
 * @see NetworkClient
 * @see NetworkResult
 * @see RequestConfig
 * @see ResponseHandler
 */
 abstract class BaseApiClient(protected val networkClient: NetworkClient) {
    /**
     * Makes a network request with the specified configuration and body.
     * This method handles the common logic for making API requests and processing responses.
     * It uses the provided NetworkClient to execute the request and ResponseHandler to process the response.
     *
     * @param T The expected response type
     * @param R The request body type
     * @param requestConfig Configuration for the request including method, URL, headers, etc.
     * @param body Optional request body data
     * @param authNames Optional list of authentication names to apply
     * @return NetworkResult containing either the successful response data of type T or error information
     * @throws Exception if there's an error during the network request or response processing
     * @see NetworkResult
     * @see RequestConfig
     * @see ResponseHandler
     */
    protected suspend inline fun <reified T, R> request(
        requestConfig: RequestConfig<T>,
        body: R? = null,
        authNames: List<String> = emptyList()
    ): NetworkResult<T> {
        try {
            val response = networkClient.jsonRequest(
                requestConfig.copy(),
                body,
                authNames
            )
            return ResponseHandler.handleResponse(response)
        } catch (exp: Exception) {
            return NetworkResult.Error(
                NetworkErrorResponse(
                    id = NetworkErrorCode.GENERIC_ERROR.code,
                    code = NetworkErrorCode.GENERIC_ERROR,
                    status = NetworkErrorCode.GENERIC_ERROR.code,
                    title = exp.message,
                    endpoint = requestConfig.endpointProvider?.getHost()
                )
            )
        }
    }
}

