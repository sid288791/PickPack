/*
 * Copyright (c) Koninklijke Philips N.V.,2025
 * All rights reserved
 */
package com.autopart.networkclient.data.remote.network.model

import com.autopart.networkclient.data.remote.network.api.EndpointProvider


/**
 * Defines a configuration object for a network request.
 * This data class encapsulates all the necessary parameters to make a network request,
 * excluding the request body. The body is excluded to allow for caching of the constructed
 * object for many request definitions with different bodies.
 *
 * @param T The expected response type
 * @property method The HTTP method to use for the request (GET, POST, etc.)
 * @property endpointProvider Provider for the endpoint URL
 * @property headers HTTP headers to include in the request (mutable to allow for modification)
 * @property params Query parameters to include in the request URL
 * @property query Additional query string to append to the URL
 * @property responseType Optional expected response type information
 * @property requiresAuthentication Whether the request requires authentication
 *
 * @note Headers is a Map<String,String> because RFC 2616 defines multi-valued headers as comma-separated values only.
 *
 * @see RequestMethod
 * @see EndpointProvider
 */
data class RequestConfig<T>(
    val method: RequestMethod,
    val endpointProvider: EndpointProvider? = null,
    val headers: MutableMap<String, String> = mutableMapOf(),
    val params: Map<String, Any>? = mutableMapOf(),
    val query: String = "",
    val responseType: T? = null,
    val requiresAuthentication: Boolean = false
)
