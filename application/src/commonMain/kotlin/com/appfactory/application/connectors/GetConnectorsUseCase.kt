package com.appfactory.application.connectors

import com.appfactory.domain.port.ConfiguredConnector
import com.appfactory.domain.port.ConnectorDescriptor
import com.appfactory.domain.port.ConnectorRegistry

class GetConnectorsUseCase(
    private val registry: ConnectorRegistry
) {
    fun available(): List<ConnectorDescriptor> {
        return registry.available()
    }

    fun configured(): List<ConfiguredConnector> {
        return registry.configured()
    }
}
