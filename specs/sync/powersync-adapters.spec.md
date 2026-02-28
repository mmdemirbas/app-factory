## Feature: Platform-specific PowerSync adapters behind SyncEngine

### Inputs
- `SyncScope` (entityType + optional filter)
- Platform runtime:
  - Android/iOS/Desktop -> Kotlin adapter
  - Web/Wasm -> Web adapter

### Behavior
- `syncNow(scope)`:
  - If online: emit `Syncing` then `Synced(result)`, return `DomainResult.Success`.
  - If offline: emit `Offline`, return `DomainResult.Failure`.
- `observeSyncState(scope)`:
  - Returns per-scope state as `Flow<SyncState>`.
  - Initial state is `Idle` unless platform is offline.

### Acceptance criteria
- [ ] `PowerSyncKotlinAdapter` implements `SyncEngine` contract for non-web targets.
- [ ] `PowerSyncWebAdapter` implements `SyncEngine` contract for wasm/web targets.
- [ ] `createPlatformSyncEngine()` resolves to Kotlin adapter on JVM/Android/iOS and web adapter on wasm.
- [ ] `InfrastructureModule` binds `SyncEngine` via platform factory.

### Out of scope (this slice)
- Full PowerSync SDK replication wiring.
- Conflict-resolution customization beyond basic state transitions.
- UI-level sync dashboard rendering.
