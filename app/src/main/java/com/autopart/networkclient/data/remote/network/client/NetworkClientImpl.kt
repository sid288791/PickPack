/*
 * Copyright (c) Koninklijke Philips N.V.,2025
 * All rights reserved
 */
package com.autopart.networkclient.data.remote.network.client

import com.autopart.networkclient.data.remote.network.auth.ApiKeyAuth
import com.autopart.networkclient.data.remote.network.auth.Authentication
import com.autopart.networkclient.data.remote.network.auth.HttpBasicAuth
import com.autopart.networkclient.data.remote.network.auth.HttpBearerAuth
import com.autopart.networkclient.data.remote.network.auth.OAuth
import com.autopart.networkclient.data.remote.network.model.RequestConfig
import com.autopart.networkclient.data.remote.network.model.RequestMethod
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.encodedPath

/**
 * Implementation of the NetworkClient interface for making HTTP requests.
 * This class provides concrete implementations for making JSON and generic HTTP requests
 * using the Ktor HTTP client. It handles URL construction, authentication, headers,
 * and request body serialization. It follows the singleton pattern for efficient resource usage.
 *
 * @property client The Ktor HttpClient used to make the actual HTTP requests
 * @see NetworkClient
 * @see HttpClient
 * @see RequestConfig
 * @see Authentication
 */
class NetworkClientImpl(private val client: HttpClient) : NetworkClient {

    /**
     * Makes a JSON request to the specified endpoint.
     * This method delegates to performRequest, setting the content type to JSON.
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
    override suspend fun <T> jsonRequest(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse = performRequest(requestConfig, body, authNames)

    /**
     * Makes a generic HTTP request to the specified endpoint.
     * This method delegates to performRequest for the actual request execution.
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
    override suspend fun <T> request(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse = performRequest(requestConfig, body, authNames)
    
    /**
     * Gets the available authentication methods.
     * This method returns a map of authentication method names to their implementations.
     *
     * @return Map of authentication method names to Authentication implementations
     * @see Authentication
     */
    override fun getAuthentications(): Map<String, Authentication> = authentications

    /**
     * Performs the actual HTTP request.
     * This method applies authentication, sets up the URL, headers, content type,
     * and request body before executing the request.
     *
     * @param T The expected response type
     * @param requestConfig Configuration for the request
     * @param body Optional request body
     * @param authNames List of authentication method names to apply
     * @return HttpResponse containing the raw response from the server
     * @throws Exception if there's an error during the network request
     */
    private suspend fun <T> performRequest(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse {
        applyAuth(requestConfig.headers, authNames)
        return client.request {
            method = requestConfig.method.toHttpMethod()
            url.setFrom(requestConfig)
            contentType(ContentType.Application.Json)
            headers.appendAllFrom(requestConfig.headers)
            setRequestBodyIfNeeded(requestConfig, body)
        }
    }

    /**
     * Sets the URL components from the request configuration.
     * This method handles parsing the URL from the endpoint provider,
     * extracting query parameters, and combining them with any additional
     * parameters from the request configuration.
     *
     * @param T The expected response type
     * @param config The request configuration containing the endpoint provider and parameters
     */
    private fun <T> URLBuilder.setFrom(config: RequestConfig<T>) {
        val provider = config.endpointProvider

        if (provider != null) {
            // Get the complete URL
            val completeUrl = provider.getHost()
            
            // Check if the URL contains query parameters
            val queryStartIndex = completeUrl.indexOf('?')
            
            if (queryStartIndex > 0) {
                // Parse the base part of the URL
                val baseUrl = completeUrl.substring(0, queryStartIndex)
                val urlBuilder = URLBuilder(baseUrl)
                
                // Set the base URL components
                protocol = urlBuilder.protocol
                host = urlBuilder.host
                port = urlBuilder.port
                encodedPath = urlBuilder.encodedPath
                
                // Parse and add query parameters
                val queryPart = completeUrl.substring(queryStartIndex + 1)
                queryPart.split('&').forEach { param ->
                    val keyValue = param.split('=', limit = 2)
                    if (keyValue.size == 2) {
                        parameters.append(keyValue[0], keyValue[1])
                    }
                }
            } else {
                // No query parameters, just use the URL as is
                val urlBuilder = URLBuilder(completeUrl)
                protocol = urlBuilder.protocol
                host = urlBuilder.host
                port = urlBuilder.port
                encodedPath = urlBuilder.encodedPath
            }
            
            // Also add any parameters from the config
            config.params?.forEach { (key, value) ->
                if (value is List<*>) {
                    value.forEach { item ->
                        parameters.append(key, item.toString())
                    }
                } else {
                    parameters.append(key, value.toString())
                }
            }
        }
    }

    /**
     * Appends all headers from a map to the HeadersBuilder.
     *
     * @param headers Map of header names to values to append
     */
    private fun HeadersBuilder.appendAllFrom(headers: Map<String, String>) {
        headers.forEach { (key, value) -> append(key, value) }
    }

    /**
     * Sets the request body if the HTTP method requires one.
     * This method checks if the HTTP method requires a body (e.g., POST, PUT, PATCH)
     * and sets the body if needed.
     *
     * @param T The expected response type
     * @param config The request configuration containing the HTTP method
     * @param body The body to set if needed
     */
    private fun <T> HttpRequestBuilder.setRequestBodyIfNeeded(
        config: RequestConfig<T>,
        body: Any?
    ) {
        if (config.method.requiresBody()) {
            setBody(body)
        }
    }

    /**
     * Converts a RequestMethod to the corresponding Ktor HttpMethod.
     *
     * @return The Ktor HttpMethod corresponding to this RequestMethod
     * @see RequestMethod
     * @see HttpMethod
     */
    private fun RequestMethod.toHttpMethod(): HttpMethod = when (this) {
        RequestMethod.DELETE -> HttpMethod.Delete
        RequestMethod.GET -> HttpMethod.Get
        RequestMethod.HEAD -> HttpMethod.Head
        RequestMethod.PATCH -> HttpMethod.Patch
        RequestMethod.PUT -> HttpMethod.Put
        RequestMethod.POST -> HttpMethod.Post
        RequestMethod.OPTIONS -> HttpMethod.Options
    }

    /**
     * Determines if a RequestMethod requires a request body.
     * POST, PUT, and PATCH methods typically require a body.
     *
     * @return True if this method requires a body, false otherwise
     */
    private fun RequestMethod.requiresBody(): Boolean =
        this in listOf(RequestMethod.PUT, RequestMethod.POST, RequestMethod.PATCH)

    /**
     * Applies the specified authentication methods to the request headers.
     * This method looks up each authentication name in the authentications map
     * and applies it to the headers.
     *
     * @param headers The headers to apply authentication to
     * @param authNames List of authentication method names to apply
     * @throws IllegalArgumentException if an authentication name is not found
     */
    private fun applyAuth(headers: MutableMap<String, String>, authNames: List<String>) {
        authNames.forEach { name ->
            val auth = authentications[name]
                ?: throw IllegalArgumentException("Authentication undefined: $name")
            auth.apply(headers)
        }
    }

    companion object {
        private val authentications: Map<String, Authentication> by lazy {
            mapOf(
                "apiKey" to ApiKeyAuth("header", "Api-Key"),
                "basicAuth" to HttpBasicAuth(),
                "bearerAuth" to HttpBearerAuth("bearer"),
                "oauth" to OAuth()
            )
        }

        /**
         * Gets the singleton instance of NetworkClientImpl.
         * If a custom HttpClient is provided, it will be used; otherwise,
         * a default HttpClient from DefaultClientProvider will be created.
         *
         * @param client Optional custom HttpClient to use
         * @return The singleton instance of NetworkClient
         * @see DefaultClientProvider
         * @see HttpClient
         */
        fun getInstance(client: HttpClient = DefaultClientProvider().getHttpClient()): NetworkClient =
            NetworkClientImpl(client)
    }
}
