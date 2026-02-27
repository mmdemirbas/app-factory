package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.ConnectorId
import com.appfactory.domain.port.OAuthFlowResult
import com.appfactory.domain.port.OAuthService
import com.appfactory.domain.port.TokenRefreshResult
import com.appfactory.domain.port.OAuthProvider

class FakeOAuthProvider : OAuthProvider {
    var flowResultOverride: OAuthFlowResult? = null
    var refreshResultOverride: TokenRefreshResult? = null
    
    val initiateFlowCallCount get() = _initiateFlowCallCount
    private var _initiateFlowCallCount = 0
    
    val refreshTokenCallCount get() = _refreshTokenCallCount
    private var _refreshTokenCallCount = 0
    
    val revokeTokenCallCount get() = _revokeTokenCallCount
    private var _revokeTokenCallCount = 0

    override suspend fun initiateFlow(service: OAuthService): DomainResult<OAuthFlowResult> {
        _initiateFlowCallCount++
        return DomainResult.Success(flowResultOverride ?: OAuthFlowResult(emptyMap(), emptyList()))
    }

    override suspend fun refreshToken(connectorId: ConnectorId): DomainResult<TokenRefreshResult> {
        _refreshTokenCallCount++
        return DomainResult.Success(refreshResultOverride ?: TokenRefreshResult(emptyMap()))
    }

    override suspend fun revokeToken(connectorId: ConnectorId): DomainResult<Unit> {
        _revokeTokenCallCount++
        return DomainResult.Success(Unit)
    }
}
