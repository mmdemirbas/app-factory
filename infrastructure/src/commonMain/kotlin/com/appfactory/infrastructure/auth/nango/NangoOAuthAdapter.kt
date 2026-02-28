package com.appfactory.infrastructure.auth.nango

import com.appfactory.domain.common.DomainError
import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.ConnectorId
import com.appfactory.domain.port.OAuthFlowResult
import com.appfactory.domain.port.OAuthProvider
import com.appfactory.domain.port.OAuthService
import com.appfactory.domain.port.TokenRefreshResult

/**
 * Implementation of OAuthProvider using Nango.
 * Currently stubbed to fetch the token directly, assuming the frontend Nango SDK
 * has already handled the interactive portion of the flow and established the connection.
 */
class NangoOAuthAdapter(
    private val nangoClient: NangoClient
) : OAuthProvider {

    override suspend fun initiateFlow(service: OAuthService): DomainResult<OAuthFlowResult> {
        return try {
            // In a real flow, the frontend uses the Nango SDK to initiate the auth popup.
            // When it succeeds, it returns a connectionId. We then use that connectionId
            // on the backend to fetch the actual tokens securely using our secret key.
            // For this initial implementation, we simulate fetching an already established connection.
            
            // In actual usage, the connectionId should be passed in from the frontend, 
            // but the OAuthProvider interface currently doesn't take a connectionId.
            // We will need to adjust the interface or the ConfigureConnectorUseCase.
            // For now, we stub it.
            
            // A hardcoded connection id for local testing.
            val connectionId = "test-connection-id" 
            val providerConfigKey = getProviderConfigKey(service)
            
            val response = nangoClient.getConnection(connectionId, providerConfigKey)
            
            val credentials = mutableMapOf<String, String>()
            credentials["access_token"] = response.credentials.access_token
            response.credentials.refresh_token?.let { credentials["refresh_token"] = it }
            
            DomainResult.success(
                OAuthFlowResult(
                    credentials = credentials,
                    scopes = emptyList() // Scopes are usually managed/returned by Nango
                )
            )
        } catch (e: Exception) {
            DomainResult.failure(DomainError.Unknown(e.message ?: "Failed to initiate Nango OAuth flow"))
        }
    }

    override suspend fun refreshToken(connectorId: ConnectorId): DomainResult<TokenRefreshResult> {
        // Build out later if we need explicit token refresh (Nango often handles this automatically)
         return DomainResult.failure(DomainError.Unknown("Not implemented yet"))
    }

    override suspend fun revokeToken(connectorId: ConnectorId): DomainResult<Unit> {
        // Build out later
        return DomainResult.success(Unit)
    }
    
    // Map our internal OAuthService enum to Nango's Provider Config Keys
    private fun getProviderConfigKey(service: OAuthService): String {
        return when (service) {
            OAuthService.GOOGLE_SHEETS -> "google-sheets" // Must match nango.yaml
            // Add others as needed
            else -> service.name.lowercase()
        }
    }
}
