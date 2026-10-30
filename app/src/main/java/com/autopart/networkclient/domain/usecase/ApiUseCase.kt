/*
 * Copyright (c) Koninklijke Advance N.V.,2025
 * All rights reserved
 */

package com.autopart.networkclient.domain.usecase

import com.autopart.networkclient.data.remote.network.model.NetworkResult
import com.autopart.networkclient.data.repository.HttpApiClient
import com.autopart.networkclient.domain.repository.ApiRequest


/**
 * Use case for executing API requests in the domain layer.
 * This class follows the Clean Architecture principles by providing a domain-level abstraction
 * for executing API requests. It acts as an intermediary between the presentation layer and
 * the data layer, delegating the actual execution to the HttpApiClient.
 *
 * @property httpApiClient The HTTP API client used to execute the requests
 * @see ApiRequest
 * @see HttpApiClient
 * @see NetworkResult
 */
 class ApiUseCase(private val httpApiClient: HttpApiClient = HttpApiClient.getInstance()) {
    /**
     * Executes an API request and returns the result.
     * This method delegates the execution to the HttpApiClient and returns the result.
     * It serves as a domain-level entry point for making API requests.
     *
     * @param request The API request to execute
     * @return NetworkResult containing either the successful response data or error information
     * @throws Exception if there's an error during request execution
     * @see ApiRequest
     * @see NetworkResult
     */
    suspend fun execute(request: ApiRequest): NetworkResult<Any> {
        return httpApiClient.execute(request)
    }
}
