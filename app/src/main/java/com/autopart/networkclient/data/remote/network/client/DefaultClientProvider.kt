/*
 * Copyright (c) Koninklijke Philips N.V., 2025.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Provider for the default HTTP client
 */
class DefaultClientProvider {
    /**
     * Get the default HTTP client with standard configuration
     */
    fun getHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
