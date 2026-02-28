package com.appfactory.application.connectors

import com.appfactory.domain.port.ConfiguredConnector
import com.appfactory.domain.port.ConnectorDescriptor
import com.appfactory.domain.port.ConnectorRegistry

import com.appfactory.domain.model.TeamId

class GetConnectorsUseCase(
    private val registry: ConnectorRegistry
) {
    fun available(): List<ConnectorDescriptor> {
        return registry.available()
    }

    fun configured(teamId: TeamId): List<ConfiguredConnector> {
        return registry.configured(teamId)
    }
}
