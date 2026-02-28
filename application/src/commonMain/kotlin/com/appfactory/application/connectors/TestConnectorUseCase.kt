package com.appfactory.application.connectors

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.ConnectorId
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.TestResult

class TestConnectorUseCase(
    private val registry: ConnectorRegistry
) {
    suspend operator fun invoke(connectorId: ConnectorId): DomainResult<TestResult> {
        return registry.test(connectorId)
    }
}
