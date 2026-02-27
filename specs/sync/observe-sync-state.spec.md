## Feature: Observe live sync state

### Inputs
- entityType: String
- filter: SyncFilter?

### Behavior
- Constructs a SyncScope
- Returns SyncEngine.observeSyncState(scope) as a Flow<SyncState>
- Caller subscribes; states are: Idle, Syncing, Synced, Error, Offline

### Acceptance criteria
- [ ] Returns a Flow that emits SyncState values
- [ ] Flow emits Idle when no sync is in progress
- [ ] Flow emits Syncing during an active sync
- [ ] Flow emits Offline when connectivity is lost
- [ ] SyncEngine.observeSyncState() is called with the correct scope

### Notes
This use case is a thin delegation — testing focuses on correct scope
construction and flow passthrough, not on sync state machine logic
(that is SyncEngine's responsibility).
