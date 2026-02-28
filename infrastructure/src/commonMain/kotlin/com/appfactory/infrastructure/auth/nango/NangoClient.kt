package com.appfactory.infrastructure.auth.nango

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Client for interacting with the Nango REST API.
 */
class NangoClient(
    private val httpClient: HttpClient,
    private val nangoSecretKey: String,
    private val nangoBaseUrl: String = "http://localhost:3003", // Default local nango url
) {

    /**
     * Fetches the connection details (including credentials) for a specific connection ID.
     */
    suspend fun getConnection(connectionId: String, providerConfigKey: String): NangoConnectionResponse {
        val response = httpClient.get("$nangoBaseUrl/connection/$connectionId") {
            header("Authorization", "Bearer $nangoSecretKey")
            header("Provider-Config-Key", providerConfigKey)
        }
        return response.body()
    }
}

// Nango API Response Types
@Serializable
data class NangoConnectionResponse(
    val credentials: NangoCredentials,
    val connection_config: Map<String, String>? = null
)

@Serializable
data class NangoCredentials(
    val access_token: String,
    val refresh_token: String? = null,
    val expires_in: Long? = null,
    val raw: Map<String, kotlinx.serialization.json.JsonElement>? = null
)
