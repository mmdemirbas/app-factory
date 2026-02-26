# Connector Guide

How to add a new connector to the App Factory.

---

## What is a Connector?

A connector is a domain-agnostic bridge to an external system (Google Sheets, Notion, Jira, etc.).
It knows about the external system's data model but nothing about this app's domain.

Key rule: **SDK types never cross the connector boundary.**
The adapter maps `SDK type → connector record type` internally.
The application layer maps `connector record type → domain type`.

---

## Step-by-Step

### 1. Create the connector package

```
infrastructure/connectors/<name>/
├── model/
│   └── <Name>Record.kt     # Stable connector-owned types (not SDK types)
├── <Name>Connector.kt      # Concrete interface
└── <Name>ConnectorAdapter.kt # Implementation (SDK usage is private here)
```

### 2. Define stable record types

```kotlin
// infrastructure/connectors/googlesheets/model/GoogleSheetsRow.kt
data class GoogleSheetsRow(
    val spreadsheetId: String,
    val sheetName: String,
    val rowIndex: Int,
    val cells: Map<String, CellValue>,
)
```

These types are owned by you. They are stable even if the SDK changes.

### 3. Define the connector interface

```kotlin
interface GoogleSheetsConnector {
    suspend fun fetchRows(config: ConnectorConfig): List<GoogleSheetsRow>
    suspend fun pushRows(rows: List<GoogleSheetsRow>, config: ConnectorConfig): PushResult
    fun observeChanges(config: ConnectorConfig): Flow<RemoteChange<GoogleSheetsRow>>
}
```

### 4. Implement the adapter

```kotlin
class GoogleSheetsConnectorAdapter(
    private val sheetsApi: SheetsApiClient,  // SDK — never escapes this class
) : GoogleSheetsConnector {
    override suspend fun fetchRows(config: ConnectorConfig): List<GoogleSheetsRow> {
        val sdkResponse = sheetsApi.get(...)
        return sdkResponse.toGoogleSheetsRows()  // private extension function
    }

    private fun SdkResponse.toGoogleSheetsRows(): List<GoogleSheetsRow> { ... }
}
```

### 5. Register in ConnectorRegistry

Add a `ConnectorDescriptor` to `KnownConnectors.kt` (created in Phase 5).

### 6. Add OAuth service (if needed)

Add the service to the `OAuthService` enum in `domain/port/AuthProvider.kt`.

### 7. Write integration tests

Test against the real API (use a sandbox/test account) or a mock HTTP server.
Integration tests live in `infrastructure/src/jvmTest/`.
