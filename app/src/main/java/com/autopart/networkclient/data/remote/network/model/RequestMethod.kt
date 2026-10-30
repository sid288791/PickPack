/*
 * Copyright (c) Koninklijke Philips N.V., 2026.
 * All rights reserved.
 */
package com.autopart.networkclient.data.remote.network.model

/**
 * Provides enumerated HTTP verbs for network requests.
 * This enum represents the standard HTTP methods as defined in RFC 7231.
 * These methods are used to specify the desired action to be performed on the identified resource.
 *
 * @see RequestConfig
 * @see <a href="https://tools.ietf.org/html/rfc7231#section-4">RFC 7231 Section 4: Request Methods</a>
 */
enum class RequestMethod {
    /**
     * The GET method requests a representation of the specified resource.
     * Requests using GET should only retrieve data and should have no other effect.
     */
    GET,
    
    /**
     * The DELETE method deletes the specified resource.
     */
    DELETE,
    
    /**
     * The HEAD method asks for a response identical to a GET request,
     * but without the response body.
     */
    HEAD,
    
    /**
     * The OPTIONS method describes the communication options for the target resource.
     */
    OPTIONS,
    
    /**
     * The PATCH method applies partial modifications to a resource.
     */
    PATCH,
    
    /**
     * The POST method submits an entity to the specified resource,
     * often causing a change in state or side effects on the server.
     */
    POST,
    
    /**
     * The PUT method replaces all current representations of the target resource with the request payload.
     */
    PUT
}
