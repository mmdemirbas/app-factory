package com.appfactory.application.connectors

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.ConnectorId
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.OAuthProvider

class RemoveConnectorUseCase(
    private val registry: ConnectorRegistry,
    private val oauthProvider: OAuthProvider
) {
    suspend operator fun invoke(connectorId: ConnectorId): DomainResult<Unit> {
        // First try to revoke token logic, log error but allow removal even if it fails
        oauthProvider.revokeToken(connectorId)
        
        // Then detach from registry
        return registry.remove(connectorId)
    }
}
