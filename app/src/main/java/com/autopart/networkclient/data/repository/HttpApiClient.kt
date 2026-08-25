/*
 * Copyright (c) Koninklijke Philips N.V.,2025
 * All rights reserved
 */

package com.autopart.networkclient.data.repository

import com.autopart.networkclient.data.remote.network.api.BaseApiClient
import com.autopart.networkclient.data.remote.network.api.EndpointProvider
import com.autopart.networkclient.data.remote.network.client.NetworkClient
import com.autopart.networkclient.data.remote.network.client.NetworkClientImpl
import com.autopart.networkclient.data.remote.network.model.NetworkResult
import com.autopart.networkclient.data.remote.network.model.RequestConfig
import com.autopart.networkclient.domain.repository.ApiRequest


/**
 * HTTP API client implementation for making network requests.
 * This class extends BaseApiClient and provides functionality to execute API requests
 * with proper configuration. It follows the singleton pattern for efficient resource usage.
 *
 * @property networkClient The network client used to make HTTP requests
 * @see PRGBaseApiClient
 * @see NetworkClient
 * @see ApiRequest
 */
open class HttpApiClient(networkClient: NetworkClient) : BaseApiClient(networkClient) {

    /**
     * Executes an API request and returns the result.
     * This method configures the request based on the provided ApiRequest object,
     * creates a RequestConfig, and delegates to the base class for actual execution.
     *
     * @param T The expected response type
     * @param request The API request to execute
     * @return NetworkResult containing either the successful response data of type T or error information
     * @throws Exception if there's an error during request execution
     * @see ApiRequest
     * @see RequestConfig
     * @see NetworkResult
     */
    internal suspend inline fun <reified T> execute(request: ApiRequest): NetworkResult<T> {
        val endpointProvider = EndpointProvider { request.url }
        val localVariableConfig = RequestConfig<T>(
            request.method,
            endpointProvider = endpointProvider,
            headers = request.headers as MutableMap<String, String>,
            requiresAuthentication = false,
            params = request.queryParameters
        )

        return request(localVariableConfig, request.body)
    }

    companion object {
        private var instance: HttpApiClient? = null
        /**
         * Gets the singleton instance of HttpApiClient.
         * If no instance exists, a new one is created. If a NetworkClient is provided,
         * it will be used; otherwise, a default NetworkClientImpl will be created.
         *
         * @param networkClient Optional network client to use for the HttpApiClient instance
         * @return The singleton instance of HttpApiClient
         * @see NetworkClient
         * @see NetworkClientImpl
         */
        fun getInstance(networkClient: NetworkClient? = null): HttpApiClient {
            if (instance == null) {
                instance = HttpApiClient(
                    networkClient ?: NetworkClientImpl.getInstance()
                )
            }
            return instance!!
        }
        
        /**
         * Reset the singleton instance for testing purposes
         */
        internal fun resetInstance() {
            instance = null
        }
    }
}
