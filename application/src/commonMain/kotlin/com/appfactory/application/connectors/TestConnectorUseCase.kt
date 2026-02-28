package com.appfactory.application.connectors

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.port.ConnectorId
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.TestResult

import com.appfactory.domain.model.TeamId

class TestConnectorUseCase(
    private val registry: ConnectorRegistry
) {
    suspend operator fun invoke(teamId: TeamId, connectorId: ConnectorId): DomainResult<TestResult> {
        return registry.test(teamId, connectorId)
    }
}
