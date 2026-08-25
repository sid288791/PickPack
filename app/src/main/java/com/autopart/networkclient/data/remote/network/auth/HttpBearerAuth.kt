/*
 * Copyright (c) Koninklijke Philips N.V., 2025.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.auth

/**
 * HTTP Bearer Authentication
 */
class HttpBearerAuth(private val scheme: String?) : Authentication {
    var bearerToken: String? = null

    override fun apply(headers: MutableMap<String, String>) {
        val token: String = bearerToken ?: return
        headers["Authorization"] =
            (if (scheme != null) upperCaseBearer(scheme) + " " else "") + token
    }

    private fun upperCaseBearer(scheme: String): String {
        return if ("bearer".equals(scheme, ignoreCase = true)) "Bearer" else scheme
    }

    override fun applyParams(param1: String?, param2: String?) {
        bearerToken = param1
    }
}
