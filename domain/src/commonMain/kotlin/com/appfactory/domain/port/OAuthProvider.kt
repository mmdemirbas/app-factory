package com.appfactory.domain.port

import com.appfactory.domain.common.DomainResult

/**
 * Port: OAuthProvider
 *
 * Manages OAuth flows for connector authentication.
 * Does not know which OAuth management service is used.
 *
 * Default implementation: NangoOAuthAdapter (infrastructure/auth/)
 * Replacement: any custom implementation, or manual OAuth per connector.
 *
 * See ADR-008.
 */
interface OAuthProvider {
    suspend fun initiateFlow(service: OAuthService): DomainResult<OAuthFlowResult>
    suspend fun refreshToken(connectorId: ConnectorId): DomainResult<TokenRefreshResult>
    suspend fun revokeToken(connectorId: ConnectorId): DomainResult<Unit>
}

data class OAuthFlowResult(
    val credentials: Map<String, String>,
    val scopes: List<String>,
)

data class TokenRefreshResult(
    val credentials: Map<String, String>,
)
