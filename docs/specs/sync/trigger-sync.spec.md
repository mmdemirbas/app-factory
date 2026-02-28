## Feature: Trigger an immediate sync

### Inputs
- entityType: String (maps to a table/collection name)
- filter: SyncFilter? (optional, e.g. by userId)

### Behavior
- Constructs a SyncScope from inputs
- Calls SyncEngine.syncNow(scope)
- Returns SyncResult on success

### Acceptance criteria
- [ ] Valid entityType triggers sync and returns SyncResult
- [ ] SyncEngine.syncNow() is called exactly once with the correct scope
- [ ] filter=null triggers an unfiltered sync
- [ ] filter with field+value triggers a filtered sync
- [ ] SyncEngine failure propagates as DomainResult.Failure

### Out of scope
- Scheduling (that belongs in infrastructure)
- Retry logic
