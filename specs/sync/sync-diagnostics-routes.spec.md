## Feature: Backend sync diagnostics routes

### Inputs
- HTTP request to sync diagnostics endpoints.
- Optional sync scope (`entityType`, optional filter).

### Behavior
- `POST /api/sync/trigger`:
  - Triggers `TriggerSyncUseCase` for requested scope.
  - Returns success payload with scope and counters.
  - Returns failure payload with error message.
- `GET /api/sync/state`:
  - Reads current sync state from `ObserveSyncStateUseCase`.
  - Returns state label for requested scope.

### Acceptance criteria
- [x] Trigger route calls application sync use case (no sync logic in route).
- [x] State route reports one of: `Idle`, `Syncing`, `Synced`, `Error`, `Offline`.
- [x] Routes are registered in Ktor application module.
- [x] Responses are JSON and serializable.

### Out of scope
- Streaming state updates (SSE/WebSocket).
- Authz rules for diagnostics endpoints.
