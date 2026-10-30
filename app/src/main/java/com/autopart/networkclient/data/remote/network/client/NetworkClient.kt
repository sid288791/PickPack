/*
 * Copyright (c) Koninklijke Advance N.V.,2025
 * All rights reserved
 */
package com.autopart.networkclient.data.remote.network.client
import com.autopart.networkclient.data.remote.network.auth.Authentication
import com.autopart.networkclient.data.remote.network.model.RequestConfig
import io.ktor.client.statement.HttpResponse

/**
 * Network client interface for making HTTP requests.
 * This interface defines the contract for network communication in the PRG module.
 * It provides methods for making JSON and generic HTTP requests with various configurations.
 * The interface abstracts the underlying HTTP client implementation, allowing for easier testing
 * and flexibility in choosing different HTTP client libraries.
 *
 * @see RequestConfig
 * @see Authentication
 * @see HttpResponse
 */
interface NetworkClient {
    /**
     * Makes a JSON request to the specified endpoint.
     * This method handles serializing the request body to JSON format and setting appropriate content headers.
     *
     * @param T The expected response type
     * @param requestConfig Configuration for the request including method, URL, headers, etc.
     * @param body Optional request body to be serialized as JSON
     * @param authNames List of authentication method names to apply to the request
     * @return HttpResponse containing the raw response from the server
     * @throws Exception if there's an error during the network request
     * @see RequestConfig
     * @see HttpResponse
     */
    suspend fun <T> jsonRequest(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse

    /**
     * Makes a generic HTTP request to the specified endpoint.
     * This method is used for non-JSON requests or when custom content handling is required.
     *
     * @param T The expected response type
     * @param requestConfig Configuration for the request including method, URL, headers, etc.
     * @param body Optional request body
     * @param authNames List of authentication method names to apply to the request
     * @return HttpResponse containing the raw response from the server
     * @throws Exception if there's an error during the network request
     * @see RequestConfig
     * @see HttpResponse
     */
    suspend fun <T> request(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse
    
    /**
     * Gets the available authentication methods.
     * This method returns a map of authentication method names to their implementations.
     * These authentication methods can be used to secure API requests.
     *
     * @return Map of authentication method names to Authentication implementations
     * @see Authentication
     */
    fun getAuthentications(): Map<String, Authentication>
}
