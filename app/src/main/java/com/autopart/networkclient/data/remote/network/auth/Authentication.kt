/*
 * Copyright (c) Koninklijke Advance N.V., 2025.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.auth

/**
 * Interface for authentication methods
 */
interface Authentication {
    /**
     * Apply authentication settings to header params.
     *
     * @param headers Header parameters.
     */
    fun apply(headers: MutableMap<String, String>)

    /**
     * Apply given parameters to parameters used in the auth
     *
     * @param param1 First parameter
     * @param param2 Second parameter
     */
    fun applyParams(param1: String?, param2: String?)
}
