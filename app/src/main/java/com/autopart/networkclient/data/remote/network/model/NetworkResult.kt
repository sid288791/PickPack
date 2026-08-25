/*
 * Copyright (c) Koninklijke Philips N.V., 2025.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.model

import com.autopart.networkclient.domain.usecase.NetworkErrorCode
import com.autopart.networkclient.domain.usecase.network.NetworkErrorResponse


/**
 * Represents the result of a network operation.
 * This sealed class provides a type-safe way to handle both successful and failed network operations.
 * It follows the Result pattern, where a result can be either a Success containing the data,
 * or an Error containing error information.
 *
 * @param T The type of data returned on successful operations
 * @see Success
 * @see Error
 * @see NetworkErrorResponse
 */
open class NetworkResult<T>(val data: T? = null, val errorCode: NetworkErrorResponse? = null)  {
    /**
     * Represents a successful network operation with data.
     * This class encapsulates the successful result of a network operation,
     * containing the requested data of type T.
     *
     * @param T The type of data returned
     * @property data The data returned from the successful network operation
     */
    open class Success<T>(data: T) : NetworkResult<T>(data)
    
    /**
     * Represents a failed network operation with error details.
     * This class encapsulates the error result of a network operation,
     * containing optional error information.
     *
     * @property errorCode Optional error details containing information about what went wrong
     * @see NetworkErrorResponse
     */
    open class Error<T>(
        val details: NetworkErrorResponse? = null,
        data: T? = null
    ) : NetworkResult<T>(data, details)

    companion object {
        fun getGenericError(error: NetworkErrorResponse?) = Error<String>(
            error ?: NetworkErrorResponse(
                id = NetworkErrorCode.GENERIC_ERROR.code,
                code = NetworkErrorCode.GENERIC_ERROR,
                status = NetworkErrorCode.GENERIC_ERROR.code
            )
        )
        fun <T> getGenericSuccess(data: T) = Success(data)
    }
}
