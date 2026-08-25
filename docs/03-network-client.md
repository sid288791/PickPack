# Step 3: Network Client Layer

This step creates a reusable Ktor-based network client layer under the `com.autopart.networkclient` package. It includes authentication, request models, response handling, error codes, and a generic API client.

**Package:** `com.autopart.networkclient`

---

## 3.1 Authentication Interface

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/auth/Authentication.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.auth

interface Authentication {
    fun apply(headers: MutableMap<String, String>)
    fun applyParams(param1: String?, param2: String?)
}
```

---

## 3.2 API Key Authentication

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/auth/ApiKeyAuth.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.auth

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
    }

    override fun applyParams(param1: String?, param2: String?) {
        apiKey = param1
        apiKeyPrefix = param2
    }
}
```

---

## 3.3 HTTP Basic Authentication

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/auth/HttpBasicAuth.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.auth

import kotlin.io.encoding.Base64

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
```

---

## 3.4 HTTP Bearer Authentication

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/auth/HttpBearerAuth.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.auth

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
```

---

## 3.5 OAuth Authentication

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/auth/OAuth.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.auth

class OAuth : Authentication {
    var accessToken: String? = null

    override fun apply(headers: MutableMap<String, String>) {
        val token: String = accessToken ?: return
        headers["Authorization"] = "Bearer $token"
    }

    override fun applyParams(param1: String?, param2: String?) {
        accessToken = param1
    }
}
```

---

## 3.6 Request Method Enum

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/model/RequestMethod.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.model

enum class RequestMethod {
    GET,
    DELETE,
    HEAD,
    OPTIONS,
    PATCH,
    POST,
    PUT
}
```

---

## 3.7 Endpoint Provider

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/api/EndpointProvider.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.api

fun interface EndpointProvider {
    fun getHost(): String
}
```

---

## 3.8 Request Config

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/model/RequestConfig.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.model

import com.autopart.networkclient.data.remote.network.api.EndpointProvider

data class RequestConfig<T>(
    val method: RequestMethod,
    val endpointProvider: EndpointProvider? = null,
    val headers: MutableMap<String, String> = mutableMapOf(),
    val params: Map<String, Any>? = mutableMapOf(),
    val query: String = "",
    val responseType: T? = null,
    val requiresAuthentication: Boolean = false
)
```

---

## 3.9 Network Error Models

### `NetworkErrorCode.kt`

**File:** `app/src/main/java/com/autopart/networkclient/domain/usecase/NetworkErrorCode.kt`

```kotlin
package com.autopart.networkclient.domain.usecase

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = NetworkErrorCodeSerializer::class)
enum class NetworkErrorCode(val code: String) {
    BAD_REQUEST("400"),
    INVALID_AUTHORIZATION("401"),
    FORBIDDEN("403"),
    NOT_FOUND("404"),
    INTERNAL_SERVER_ERROR("500"),
    SERVICE_UNAVAILABLE("503"),
    GENERIC_ERROR("5555"),
    PARSING_ERROR("422"),
    REGISTRATION_ID_EMPTY("6666"),
    ERROR_REGISTERED_PRODUCT_NULL("7777"),
    UNKNOWN("");

    companion object {
        fun from(code: String?): NetworkErrorCode {
            if (code == null) return UNKNOWN
            entries.firstOrNull { it.code == code }?.let { return it }
            return try {
                valueOf(code)
            } catch (_: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}

object NetworkErrorCodeSerializer : KSerializer<NetworkErrorCode> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NetworkErrorCode", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NetworkErrorCode) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): NetworkErrorCode {
        val value = decoder.decodeString()
        return NetworkErrorCode.from(value)
    }
}
```

### `Source.kt`

**File:** `app/src/main/java/com/autopart/networkclient/domain/usecase/network/Source.kt`

```kotlin
package com.autopart.networkclient.domain.usecase.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Source(
    @SerialName(value = "parameter") val parameter: String? = null
)
```

### `NetworkErrorResponse.kt`

**File:** `app/src/main/java/com/autopart/networkclient/domain/usecase/network/NetworkErrorResponse.kt`

```kotlin
package com.autopart.networkclient.domain.usecase.network

import com.autopart.networkclient.domain.usecase.NetworkErrorCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkErrorResponse(
    @SerialName(value = "id") val id: String? = null,
    @SerialName(value = "code") var code: NetworkErrorCode = NetworkErrorCode.UNKNOWN,
    @SerialName(value = "status") val status: String? = null,
    @SerialName(value = "title") var title: String? = null,
    @SerialName(value = "endpoint") var endpoint: String? = null,
    val source: Source? = null
) {
    companion object {
        fun createError(exception: Exception, code: String? = null): NetworkErrorResponse {
            return NetworkErrorResponse(
                code = NetworkErrorCode.from(code),
                title = exception.message ?: "Unknown error occurred"
            )
        }
    }
}
```

### `NetworkError.kt`

**File:** `app/src/main/java/com/autopart/networkclient/domain/usecase/network/NetworkError.kt`

```kotlin
package com.autopart.networkclient.domain.usecase.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetworkError(
    @SerialName(value = "errors") val errors: List<NetworkErrorResponse>? = null
)
```

---

## 3.10 Network Result

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/model/NetworkResult.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.model

import com.autopart.networkclient.domain.usecase.NetworkErrorCode
import com.autopart.networkclient.domain.usecase.network.NetworkErrorResponse

open class NetworkResult<T>(val data: T? = null, val errorCode: NetworkErrorResponse? = null) {
    open class Success<T>(data: T) : NetworkResult<T>(data)
    
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
```

---

## 3.11 Response Handler

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/util/ResponseHandler.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.util

import com.autopart.networkclient.data.remote.network.model.NetworkResult
import com.autopart.networkclient.domain.usecase.NetworkErrorCode
import com.autopart.networkclient.domain.usecase.network.NetworkError
import com.autopart.networkclient.domain.usecase.network.NetworkErrorResponse
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

object ResponseHandler {
    suspend inline fun <reified T> handleResponse(response: HttpResponse): NetworkResult<T> {
        return try {
            when {
                response.status.isSuccess() -> {
                    try {
                        val responseBody = response.body<T>()
                        NetworkResult.Success(responseBody)
                    } catch (e: Exception) {
                        NetworkResult.Error(
                            details = NetworkErrorResponse(
                                title = "Response Parsing Error",
                                status = response.status.value.toString(),
                                code = NetworkErrorCode.PARSING_ERROR
                            )
                        )
                    }
                }
                else -> {
                    try {
                        val errorBody = response.body<NetworkError>()
                        NetworkResult.Error(details = errorBody.errors?.get(0))
                    } catch (e: Exception) {
                        NetworkResult.Error(
                            details = NetworkErrorResponse(
                                title = "Error Response Parsing Failed $e",
                                status = response.status.value.toString()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                details = NetworkErrorResponse(
                    title = e.message,
                    status = response.status.value.toString(),
                )
            )
        }
    }
}
```

---

## 3.12 Network Client Interface

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/client/NetworkClient.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.client

import com.autopart.networkclient.data.remote.network.auth.Authentication
import com.autopart.networkclient.data.remote.network.model.RequestConfig
import io.ktor.client.statement.HttpResponse

interface NetworkClient {
    suspend fun <T> jsonRequest(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse

    suspend fun <T> request(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse

    fun getAuthentications(): Map<String, Authentication>
}
```

---

## 3.13 Default Client Provider

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/client/DefaultClientProvider.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class DefaultClientProvider {
    fun getHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}
```

---

## 3.14 Network Client Implementation

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/client/NetworkClientImpl.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.client

import com.autopart.networkclient.data.remote.network.auth.ApiKeyAuth
import com.autopart.networkclient.data.remote.network.auth.Authentication
import com.autopart.networkclient.data.remote.network.auth.HttpBasicAuth
import com.autopart.networkclient.data.remote.network.auth.HttpBearerAuth
import com.autopart.networkclient.data.remote.network.auth.OAuth
import com.autopart.networkclient.data.remote.network.model.RequestConfig
import com.autopart.networkclient.data.remote.network.model.RequestMethod
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpMethod
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.encodedPath

class NetworkClientImpl(private val client: HttpClient) : NetworkClient {

    override suspend fun <T> jsonRequest(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse = performRequest(requestConfig, body, authNames)

    override suspend fun <T> request(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse = performRequest(requestConfig, body, authNames)

    override fun getAuthentications(): Map<String, Authentication> = authentications

    private suspend fun <T> performRequest(
        requestConfig: RequestConfig<T>,
        body: Any?,
        authNames: List<String>
    ): HttpResponse {
        applyAuth(requestConfig.headers, authNames)
        return client.request {
            method = requestConfig.method.toHttpMethod()
            url.setFrom(requestConfig)
            contentType(ContentType.Application.Json)
            headers.appendAllFrom(requestConfig.headers)
            setRequestBodyIfNeeded(requestConfig, body)
        }
    }

    private fun <T> URLBuilder.setFrom(config: RequestConfig<T>) {
        val provider = config.endpointProvider
        if (provider != null) {
            val completeUrl = provider.getHost()
            val queryStartIndex = completeUrl.indexOf('?')
            if (queryStartIndex > 0) {
                val baseUrl = completeUrl.substring(0, queryStartIndex)
                val urlBuilder = URLBuilder(baseUrl)
                protocol = urlBuilder.protocol
                host = urlBuilder.host
                port = urlBuilder.port
                encodedPath = urlBuilder.encodedPath
                val queryPart = completeUrl.substring(queryStartIndex + 1)
                queryPart.split('&').forEach { param ->
                    val keyValue = param.split('=', limit = 2)
                    if (keyValue.size == 2) {
                        parameters.append(keyValue[0], keyValue[1])
                    }
                }
            } else {
                val urlBuilder = URLBuilder(completeUrl)
                protocol = urlBuilder.protocol
                host = urlBuilder.host
                port = urlBuilder.port
                encodedPath = urlBuilder.encodedPath
            }
            config.params?.forEach { (key, value) ->
                if (value is List<*>) {
                    value.forEach { item ->
                        parameters.append(key, item.toString())
                    }
                } else {
                    parameters.append(key, value.toString())
                }
            }
        }
    }

    private fun HeadersBuilder.appendAllFrom(headers: Map<String, String>) {
        headers.forEach { (key, value) -> append(key, value) }
    }

    private fun <T> HttpRequestBuilder.setRequestBodyIfNeeded(
        config: RequestConfig<T>,
        body: Any?
    ) {
        if (config.method.requiresBody()) {
            setBody(body)
        }
    }

    private fun RequestMethod.toHttpMethod(): HttpMethod = when (this) {
        RequestMethod.DELETE -> HttpMethod.Delete
        RequestMethod.GET -> HttpMethod.Get
        RequestMethod.HEAD -> HttpMethod.Head
        RequestMethod.PATCH -> HttpMethod.Patch
        RequestMethod.PUT -> HttpMethod.Put
        RequestMethod.POST -> HttpMethod.Post
        RequestMethod.OPTIONS -> HttpMethod.Options
    }

    private fun RequestMethod.requiresBody(): Boolean =
        this in listOf(RequestMethod.PUT, RequestMethod.POST, RequestMethod.PATCH)

    private fun applyAuth(headers: MutableMap<String, String>, authNames: List<String>) {
        authNames.forEach { name ->
            val auth = authentications[name]
                ?: throw IllegalArgumentException("Authentication undefined: $name")
            auth.apply(headers)
        }
    }

    companion object {
        private val authentications: Map<String, Authentication> by lazy {
            mapOf(
                "apiKey" to ApiKeyAuth("header", "Api-Key"),
                "basicAuth" to HttpBasicAuth(),
                "bearerAuth" to HttpBearerAuth("bearer"),
                "oauth" to OAuth()
            )
        }

        fun getInstance(client: HttpClient = DefaultClientProvider().getHttpClient()): NetworkClient =
            NetworkClientImpl(client)
    }
}
```

---

## 3.15 Base API Client

**File:** `app/src/main/java/com/autopart/networkclient/data/remote/network/api/BaseApiClient.kt`

```kotlin
package com.autopart.networkclient.data.remote.network.api

import com.autopart.networkclient.data.remote.network.client.NetworkClient
import com.autopart.networkclient.data.remote.network.model.NetworkResult
import com.autopart.networkclient.data.remote.network.model.RequestConfig
import com.autopart.networkclient.data.remote.network.util.ResponseHandler
import com.autopart.networkclient.domain.usecase.NetworkErrorCode
import com.autopart.networkclient.domain.usecase.network.NetworkErrorResponse

abstract class BaseApiClient(protected val networkClient: NetworkClient) {
    protected suspend inline fun <reified T, R> request(
        requestConfig: RequestConfig<T>,
        body: R? = null,
        authNames: List<String> = emptyList()
    ): NetworkResult<T> {
        try {
            val response = networkClient.jsonRequest(
                requestConfig.copy(),
                body,
                authNames
            )
            return ResponseHandler.handleResponse(response)
        } catch (exp: Exception) {
            return NetworkResult.Error(
                NetworkErrorResponse(
                    id = NetworkErrorCode.GENERIC_ERROR.code,
                    code = NetworkErrorCode.GENERIC_ERROR,
                    status = NetworkErrorCode.GENERIC_ERROR.code,
                    title = exp.message,
                    endpoint = requestConfig.endpointProvider?.getHost()
                )
            )
        }
    }
}
```

---

## 3.16 API Request Interface

**File:** `app/src/main/java/com/autopart/networkclient/domain/repository/ApiRequest.kt`

```kotlin
package com.autopart.networkclient.domain.repository

import com.autopart.networkclient.data.remote.network.model.RequestMethod

interface ApiRequest {
    val url: String
    val method: RequestMethod
    val queryParameters: Map<String, String>?
    val headers: Map<String, String>?
    val body: Any?
}
```

---

## 3.17 HTTP API Client

**File:** `app/src/main/java/com/autopart/networkclient/data/repository/HttpApiClient.kt`

```kotlin
package com.autopart.networkclient.data.repository

import com.autopart.networkclient.data.remote.network.api.BaseApiClient
import com.autopart.networkclient.data.remote.network.api.EndpointProvider
import com.autopart.networkclient.data.remote.network.client.NetworkClient
import com.autopart.networkclient.data.remote.network.client.NetworkClientImpl
import com.autopart.networkclient.data.remote.network.model.NetworkResult
import com.autopart.networkclient.data.remote.network.model.RequestConfig
import com.autopart.networkclient.domain.repository.ApiRequest

open class HttpApiClient(networkClient: NetworkClient) : BaseApiClient(networkClient) {

    internal suspend inline fun <reified T> execute(request: ApiRequest): NetworkResult<T> {
        val endpointProvider = EndpointProvider { request.url }
        val localVariableConfig = RequestConfig<T>(
            request.method,
            endpointProvider = endpointProvider,
            headers = request.headers as MutableMap<String, String>,
            requiresAuthentication = false,
            params = request.queryParameters
        )

        return request(localVariableConfig, request.body)
    }

    companion object {
        private var instance: HttpApiClient? = null
        fun getInstance(networkClient: NetworkClient? = null): HttpApiClient {
            if (instance == null) {
                instance = HttpApiClient(
                    networkClient ?: NetworkClientImpl.getInstance()
                )
            }
            return instance!!
        }
        
        internal fun resetInstance() {
            instance = null
        }
    }
}
```

---

## 3.18 API Use Case

**File:** `app/src/main/java/com/autopart/networkclient/domain/usecase/ApiUseCase.kt`

```kotlin
package com.autopart.networkclient.domain.usecase

import com.autopart.networkclient.data.remote.network.model.NetworkResult
import com.autopart.networkclient.data.repository.HttpApiClient
import com.autopart.networkclient.domain.repository.ApiRequest

class ApiUseCase(private val httpApiClient: HttpApiClient = HttpApiClient.getInstance()) {
    suspend fun execute(request: ApiRequest): NetworkResult<Any> {
        return httpApiClient.execute(request)
    }
}
```

---

## 3.19 Network Service

**File:** `app/src/main/java/com/autopart/networkclient/domain/repository/NetworkService.kt`

```kotlin
package com.autopart.networkclient.domain.repository

import com.autopart.networkclient.data.remote.network.model.NetworkResult
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

class NetworkService {
    suspend fun processApiResponse(
        response: NetworkResult<Any>,
        source: String
    ): NetworkResult<String?> {
        return when (response) {
            is NetworkResult.Success -> {
                val httpResponse = response.data as? HttpResponse
                val responseBody = httpResponse?.body<String>()
                NetworkResult.Success(responseBody)
            }
            is NetworkResult.Error -> NetworkResult.Error(response.details)
            else -> { NetworkResult.Error(null) }
        }
    }

    suspend fun processNonEssentialApiResponse(
        response: NetworkResult<Any>,
        source: String
    ): String? {
        val processedResponse = processApiResponse(response = response, source = source)
        return when (processedResponse) {
            is NetworkResult.Success -> processedResponse.data
            is NetworkResult.Error -> null
            else -> null
        }
    }
}
```

---

## Architecture Overview

```
com.autopart.networkclient/
├── data/
│   ├── remote/network/
│   │   ├── api/
│   │   │   ├── BaseApiClient.kt          # Abstract base for API clients
│   │   │   └── EndpointProvider.kt       # Functional interface for URL provider
│   │   ├── auth/
│   │   │   ├── Authentication.kt         # Auth interface
│   │   │   ├── ApiKeyAuth.kt             # API key auth
│   │   │   ├── HttpBasicAuth.kt          # Basic auth
│   │   │   ├── HttpBearerAuth.kt         # Bearer token auth
│   │   │   └── OAuth.kt                  # OAuth auth
│   │   ├── client/
│   │   │   ├── NetworkClient.kt          # Network client interface
│   │   │   ├── NetworkClientImpl.kt      # Ktor-based implementation
│   │   │   └── DefaultClientProvider.kt  # Default HttpClient provider
│   │   ├── model/
│   │   │   ├── NetworkResult.kt          # Sealed result class
│   │   │   ├── RequestConfig.kt          # Request configuration
│   │   │   └── RequestMethod.kt          # HTTP method enum
│   │   └── util/
│   │       └── ResponseHandler.kt        # HTTP response handler
│   └── repository/
│       └── HttpApiClient.kt              # Concrete API client
├── domain/
│   ├── repository/
│   │   ├── ApiRequest.kt                 # Request interface
│   │   └── NetworkService.kt             # Response processing service
│   └── usecase/
│       ├── ApiUseCase.kt                 # Domain use case for API calls
│       ├── NetworkErrorCode.kt           # Error code enum + serializer
│       └── network/
│           ├── NetworkError.kt           # Error response list wrapper
│           ├── NetworkErrorResponse.kt   # Error response model
│           └── Source.kt                 # Error source model
```

---

## Summary

After this step you should have the complete reusable network client layer with:
- 4 authentication strategies (API Key, Basic, Bearer, OAuth)
- Ktor-based `NetworkClientImpl` with URL building, auth, and request execution
- `NetworkResult` sealed class for type-safe success/error handling
- `ResponseHandler` for parsing HTTP responses and errors
- `BaseApiClient` / `HttpApiClient` / `ApiUseCase` chain for Clean Architecture
- `NetworkErrorCode` enum with custom serializer for kotlinx.serialization
