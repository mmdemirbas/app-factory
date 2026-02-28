package com.appfactory.domain.port

/**
 * Standard definitions for connectors bundled with the App Factory.
 */
object KnownConnectors {
    
    val GOOGLE_SHEETS = ConnectorDescriptor(
        id = ConnectorId("google-sheets"),
        name = "Google Sheets",
        description = "Two-way sync with Google Sheets.",
        oauthService = OAuthService.GOOGLE_SHEETS,
        capabilities = setOf(ConnectorCapability.PULL, ConnectorCapability.PUSH)
    )

    fun all(): List<ConnectorDescriptor> = listOf(GOOGLE_SHEETS)
}
