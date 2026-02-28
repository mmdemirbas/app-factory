## Feature: Local end-to-end sync state via backend transport

### Inputs
- Desktop/Web client `SyncEngine` calls.
- Local backend sync diagnostics endpoints:
  - `POST /api/sync/trigger`
  - `GET /api/sync/state`

### Behavior
- Client sync engine sends trigger/state requests to backend over HTTP when sync mode is set to `backend`.
- Trigger success maps to `DomainResult.Success(SyncResult)` and local `SyncState.Synced`.
- Trigger failure maps to `DomainResult.Failure` and local `SyncState.Error`.
- State observation polls backend and emits mapped `SyncState`.

### Acceptance criteria
- [x] Shared `BackendSyncEngine` is covered by unit tests for success/failure/polling behavior.
- [x] Desktop client can use backend transport adapter for sync state via explicit `SyncEngineMode.BackendTransport(...)`.
- [x] Web client can use backend transport adapter for sync state via explicit `SyncEngineMode.BackendTransport(...)`.
- [x] Backend allows local browser origin for web client sync route calls.

### Out of scope
- Full PowerSync SDK replication wiring and conflict stream.
- Multi-entity payload replication semantics.
