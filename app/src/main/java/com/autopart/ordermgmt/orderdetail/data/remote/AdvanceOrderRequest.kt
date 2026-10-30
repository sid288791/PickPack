/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.orderdetail.data.remote

import com.autopart.networkclient.data.remote.network.model.RequestMethod
import com.autopart.networkclient.domain.repository.ApiRequest
import kotlinx.serialization.Serializable

class AdvanceOrderRequest(
    private val baseUrl: String,
    override val method: RequestMethod = RequestMethod.POST
) : ApiRequest {

    override var url: String = baseUrl
        private set

    override val queryParameters: Map<String, String>? = null
    override val headers: Map<String, String> = mutableMapOf()
    override var body: Any? = null
        private set

    /**
     * Builds the full URL for advancing a store task and sets the request body.
     */
    fun buildUrl(orderId: String, employeeId: String) {
        url = "$baseUrl/store-tasks/$orderId/advance"
        body = AdvanceRequestBody(employeeId = employeeId)
    }
}

@Serializable
data class AdvanceRequestBody(
    val employeeId: String
)
