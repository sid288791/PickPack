package com.autopart.ordermgmt.orderdetail.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoreTaskAdvanceResponse(
    val id: Int? = null,
    val taskNumber: String? = null,
    val status: String? = null,
    val nextAction: String? = null
)
