/*
 * Copyright (c) Koninklijke Advance N.V.,2025
 * All rights reserved
 */

package com.autopart.networkclient.domain.repository

import com.autopart.networkclient.data.remote.network.model.RequestMethod

/**
 * Interface defining the contract for API requests in the PRG module.
 * This interface represents the domain-level abstraction of a network request,
 * decoupling the domain layer from the specific implementation details of the network layer.
 * Implementations of this interface provide the necessary information to make API requests.
 *
 * @see RequestMethod
 * @see HttpApiClient
 */

interface ApiRequest {
    /**
     * The URL of the API endpoint to request.
     * This should be the complete URL including protocol, domain, and path.
     */
    val url: String
    
    /**
     * The HTTP method to use for the request.
     * @see RequestMethod
     */
    val method: RequestMethod
    
    /**
     * Optional query parameters to include in the request URL.
     * These parameters will be appended to the URL as a query string.
     * The map should be mutable to allow for modification in the HttpApiClient.
     */
    val queryParameters: Map<String, String>?
    
    /**
     * Optional HTTP headers to include in the request.
     * The map should be mutable to allow for modification in the HttpApiClient.
     */
    val headers: Map<String, String>?
    
    /**
     * Optional request body to send with the request.
     * This can be any object that can be serialized to the appropriate format (usually JSON).
     */
    val body: Any?
}
