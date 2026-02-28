## Feature: Platform-specific PowerSync adapters behind SyncEngine

### Inputs
- `SyncScope` (entityType + optional filter)
- Platform runtime:
  - Android/iOS/Desktop -> Kotlin adapter
  - Web/Wasm -> Web adapter

### Behavior
- `syncNow(scope)`:
  - If online: emit `Syncing` then `Synced(result)` for the requested scope and any pending queued scopes.
  - If online: return `DomainResult.Success` with `recordsSynced` equal to the number of scopes synced in that cycle.
  - If offline: queue the scope, emit `Offline`, return `DomainResult.Failure`.
- `observeSyncState(scope)`:
  - Returns per-scope state as `Flow<SyncState>`.
  - Initial state is `Idle` unless platform is offline.

### Acceptance criteria
- [x] `PowerSyncKotlinAdapter` implements `SyncEngine` contract for non-web targets.
- [x] `PowerSyncWebAdapter` implements `SyncEngine` contract for wasm/web targets.
- [x] `createPlatformSyncEngine()` resolves to Kotlin adapter on JVM/Android/iOS and web adapter on wasm.
- [x] `InfrastructureModule` binds `SyncEngine` via platform factory.
- [x] Offline sync attempts are queued and flushed when connectivity returns.

### Out of scope (this slice)
- Full PowerSync SDK replication wiring.
- Conflict-resolution customization beyond basic state transitions.
- UI-level sync dashboard rendering.
