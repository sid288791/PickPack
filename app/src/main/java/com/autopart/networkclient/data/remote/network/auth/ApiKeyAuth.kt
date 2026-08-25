/*
 * Copyright (c) Koninklijke Philips N.V., 2025.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.auth

/**
 * API key authentication
 */
class ApiKeyAuth(private val location: String, val paramName: String) : Authentication {
    var apiKey: String? = null
    var apiKeyPrefix: String? = null


    override fun apply(headers: MutableMap<String, String>) {
        val key: String = apiKey ?: return
        val prefix: String? = apiKeyPrefix
        val value: String = if (prefix != null) "$prefix $key" else key
        if (location == "header") {
            headers[paramName] = value
        }
        // Query parameters are handled separately
    }

    override fun applyParams(param1: String?, param2: String?) {
        apiKey = param1
        apiKeyPrefix = param2
    }
}
