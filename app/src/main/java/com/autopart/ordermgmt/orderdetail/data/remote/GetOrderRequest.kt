/*
 * Copyright (c) Koninklijke Advance N.V.,2026
 * All rights reserved
 */
package com.autopart.ordermgmt.orderdetail.data.remote

import com.autopart.networkclient.data.remote.network.model.RequestMethod
import com.autopart.networkclient.domain.repository.ApiRequest

class GetOrderRequest(
    private val baseUrl: String,
    override val method: RequestMethod = RequestMethod.GET
) : ApiRequest {

    override var url: String = baseUrl
        private set

    override val queryParameters: Map<String, String>? = null
    override val headers: Map<String, String> = mutableMapOf()
    override val body: Any? = null

    /**
     * Builds the full URL for fetching an order by orderId.
     */
    fun buildUrl(orderId: String) {
        url = "$baseUrl/orders/$orderId"
    }
}
