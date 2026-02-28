## Feature: Sync status dashboard

### Inputs
- `SyncEngine` from platform adapter wiring.
- User action: "Sync now".
- Scope: `SyncScope.All`.

### Behavior
- Observe `SyncEngine.observeSyncState(SyncScope.All)` and render current state.
- On "Sync now", call `TriggerSyncUseCase(SyncEngine)` once.
- Show last sync result counts or last error message.

### Acceptance criteria
- [x] Dashboard renders current sync state (`Idle`, `Syncing`, `Synced`, `Error`, `Offline`).
- [x] Tapping "Sync now" invokes sync and updates visible state.
- [x] Success path shows synced record/conflict counts.
- [x] Failure path shows a readable error message.
- [x] Works on Android/Desktop/Web entrypoints.

### Out of scope
- Background scheduling.
- Retry/backoff policy.
- Rich per-entity scope filtering UI.
