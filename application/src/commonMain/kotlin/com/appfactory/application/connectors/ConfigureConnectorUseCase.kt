package com.appfactory.application.connectors

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.ConfiguredConnector
import com.appfactory.domain.port.ConnectorConfig
import com.appfactory.domain.port.ConnectorDescriptor
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.OAuthProvider

import com.appfactory.domain.model.TeamId

/**
 * Use case: Configure a connector.
 *
 * Initiates the OAuth flow if the connector requires it,
 * enriches the config with received credentials,
 * and persists via ConnectorRegistry.
 *
 * This use case is shared by backend (REST endpoint) and clients (UI action).
 */
class ConfigureConnectorUseCase(
    private val registry: ConnectorRegistry,
    private val oauthProvider: OAuthProvider,
) {
    suspend operator fun invoke(
        teamId: TeamId,
        descriptor: ConnectorDescriptor,
        config: ConnectorConfig,
    ): DomainResult<ConfiguredConnector> {
        val oauthService = descriptor.oauthService
        val configToSave = if (oauthService != null) {
            when (val oauthResult = oauthProvider.initiateFlow(oauthService)) {
                is DomainResult.Success -> config.withCredentials(oauthResult.value.credentials)
                is DomainResult.Failure -> return oauthResult
            }
        } else {
            config
        }

        return when (val validation = configToSave.validate()) {
            is DomainResult.Success -> registry.configure(teamId, descriptor, configToSave)
            is DomainResult.Failure -> validation
        }
    }
}
