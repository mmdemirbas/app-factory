# Phase 6 Google Sheets Connector Implementation Plan

## Goal
Implement the first full connector (Google Sheets) with stable intermediate types, field mapping UI, and two-way sync end-to-end.

## User Review Required
No immediate user review is required to begin drafting the Google Sheets connector structure. I have saved the planning tracker persistently in `.claude/current-phase.md`.

## Proposed Changes
1. **Infrastructure**:
    * Create `infrastructure/src/commonMain/kotlin/com/appfactory/infrastructure/connectors/googlesheets/model/GoogleSheetsRow.kt` for stable types.
    * Create `infrastructure/src/commonMain/kotlin/com/appfactory/infrastructure/connectors/googlesheets/GoogleSheetsConnector.kt`
    * Create `GoogleSheetsConnectorAdapter.kt`.
    * Ensure the SDK is only used inside the Adapter file.

2. **Domain Integration**:
    * Add `GoogleSheets` definition to the core `ConnectorRegistry` or `KnownConnectors`.

3. **Authentication**:
    * Setup `NangoOAuthAdapter` to handle the Google Sheets OAuth flow using Nango.

4. **UI**:
    * Create a basic Field Mapping UI so users can map the spreadsheet headers to standard application fields.

5. **Validation**:
    * Ensure `pushRows` and `fetchRows` synchronize data to a valid sheet.

## Verification Plan
### Automated Tests
* Create unit tests verifying the stable data structures (`GoogleSheetsRow.kt`).
* Ensure architecture tests (`ArchUnit`) continue passing without domain leakage.

### Manual Verification
* Run the app, try to configure the Google Sheets connector from the frontend UI, and map fields to initiate a sync.
