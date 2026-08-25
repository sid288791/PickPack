/*
 * Copyright (c) Koninklijke Philips N.V., 2025.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.auth

/**
 * OAuth Authentication
 */
class OAuth : Authentication {
    var accessToken: String? = null

    override fun apply(
        headers: MutableMap<String, String>
    ) {
        val token: String = accessToken ?: return
        headers["Authorization"] = "Bearer $token"
    }

    override fun applyParams(param1: String?, param2: String?) {
        accessToken = param1
    }
}
