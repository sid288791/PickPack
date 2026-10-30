/*
 * Copyright (c) Koninklijke Advance N.V., 2025.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.auth

import kotlin.io.encoding.Base64

/**
 * HTTP Basic Authentication
 */
class HttpBasicAuth : Authentication {
    var username: String? = null
    var password: String? = null

    override fun apply(headers: MutableMap<String, String>) {
        if (username == null && password == null) return
        val str = (username ?: "") + ":" + (password ?: "")
        val auth = Base64.encode(str.encodeToByteArray())
        headers["Authorization"] = "Basic $auth"
    }

    override fun applyParams(param1: String?, param2: String?) {
        username = param1
        password = param2
    }
}
