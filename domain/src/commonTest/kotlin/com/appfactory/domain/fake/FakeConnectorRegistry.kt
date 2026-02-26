package com.appfactory.domain.fake

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.common.EntityId
import com.appfactory.domain.common.Timestamp
import com.appfactory.domain.port.ConfiguredConnector
import com.appfactory.domain.port.ConnectorConfig
import com.appfactory.domain.port.ConnectorDescriptor
import com.appfactory.domain.port.ConnectorId
import com.appfactory.domain.port.ConnectorRegistry
import com.appfactory.domain.port.TestResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory fake for ConnectorRegistry.
 * Used in all domain and application layer tests.
 * Fast, deterministic, no I/O.
 */
class FakeConnectorRegistry(
    private val availableConnectors: List<ConnectorDescriptor> = emptyList(),
) : ConnectorRegistry {
    private val _configured = MutableStateFlow<Map<ConnectorId, ConfiguredConnector>>(emptyMap())
    private var _configureCallCount = 0
    private var _removeCallCount = 0
    private var _testResult = DomainResult.success(TestResult(success = true, message = "OK"))

    val configureCallCount: Int get() = _configureCallCount
    val removeCallCount: Int get() = _removeCallCount

    override fun available(): List<ConnectorDescriptor> = availableConnectors

    override fun configured(): List<ConfiguredConnector> = _configured.value.values.toList()

    override fun observeConfigured(): Flow<List<ConfiguredConnector>> =
        MutableStateFlow(_configured.value.values.toList()).asStateFlow()

    override suspend fun configure(
        descriptor: ConnectorDescriptor,
        config: ConnectorConfig,
    ): DomainResult<ConfiguredConnector> {
        _configureCallCount++
        val connector = ConfiguredConnector(
            id = EntityId.generate(),
            descriptor = descriptor,
            config = config,
            lastModifiedAt = Timestamp.now(),
        )
        _configured.value = _configured.value + (descriptor.id to connector)
        return DomainResult.success(connector)
    }

    override suspend fun remove(connectorId: ConnectorId): DomainResult<Unit> {
        _removeCallCount++
        _configured.value = _configured.value - connectorId
        return DomainResult.success(Unit)
    }

    override suspend fun test(connectorId: ConnectorId): DomainResult<TestResult> = _testResult

    fun setTestResult(result: DomainResult<TestResult>) {
        _testResult = result
    }
}
