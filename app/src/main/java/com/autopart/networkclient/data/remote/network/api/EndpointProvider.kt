/*
 * Copyright (c) Koninklijke Advance N.V., 2025.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.api

/**
 * Provider for endpoint URLs.
 * This functional interface is responsible for providing the host URL for API endpoints.
 * It allows for dynamic endpoint resolution at runtime, which is useful for switching between
 * different environments (e.g., development, staging, production) or for handling different
 * API versions.
 *
 * @see BaseApiClient
 */
fun interface EndpointProvider {
    /**
     * Gets the host URL for the API endpoint.
     * This method should return the complete base URL for the API endpoint,
     * which may include protocol, domain, port, and base path.
     *
     * @return The complete host URL as a String
     */
    fun getHost(): String
}
