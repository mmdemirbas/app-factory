package com.appfactory.application.connectors

import com.appfactory.domain.common.DomainResult
import com.appfactory.domain.fake.FakeConnectorRegistry
import com.appfactory.domain.fake.FakeOAuthProvider
import com.appfactory.domain.port.ConnectorConfig
import com.appfactory.domain.port.ConnectorDescriptor
import com.appfactory.domain.port.ConnectorId
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class ConnectorUseCasesTest : StringSpec({

    val connectorIdArb = arbitrary { ConnectorId(Arb.string(minSize = 5).bind()) }

    "GetConnectorsUseCase returns configured connectors" {
        val fakeRegistry = FakeConnectorRegistry(availableConnectors = emptyList())
        val getConnectors = GetConnectorsUseCase(fakeRegistry)

        val descriptor = ConnectorDescriptor(ConnectorId("mock-id"), "Mock", "Mock", null, emptySet())
        val config = ConnectorConfig(ConnectorId("mock-id"), mapOf("token" to "abc"))
        
        fakeRegistry.configure(descriptor, config)

        val configured = getConnectors.configured()
        configured.size shouldBe 1
        configured.first().descriptor.id.value shouldBe "mock-id"
    }

    "RemoveConnectorUseCase revokes tokens and removes config from registry" {
        val fakeRegistry = FakeConnectorRegistry()
        val fakeOAuthProvider = FakeOAuthProvider()
        val removeConnector = RemoveConnectorUseCase(fakeRegistry, fakeOAuthProvider)

        checkAll(connectorIdArb) { connectorId ->
            val result = removeConnector(connectorId)
            
            result.isSuccess shouldBe true
            fakeOAuthProvider.revokeTokenCallCount shouldBe 1
            fakeRegistry.removeCallCount shouldBe 1
        }
    }

    "TestConnectorUseCase returns test results from registry" {
        val fakeRegistry = FakeConnectorRegistry()
        val testConnector = TestConnectorUseCase(fakeRegistry)

        checkAll(connectorIdArb) { connectorId ->
            val result = testConnector(connectorId)
            result.isSuccess shouldBe true
            result.getOrNull()?.success shouldBe true
        }
    }
})
