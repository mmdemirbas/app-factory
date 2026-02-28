## Feature: Bidirectional replication (Firebase ↔ Supabase)

### Context
Migration window only. Old Flutter app writes directly to Firestore.
This service keeps both stores consistent. See ADR-012.

### Architecture
- Separate Docker container (zero domain knowledge)
- Firestore listener → ReplicaSink (Supabase)
- Postgres/Supabase listener → ReplicaSink (Firestore)
- Loop prevention: writes tagged with _origin marker

### Behavior
- On Firestore insert/update/delete → replicate to Supabase
- On Supabase insert/update/delete → replicate to Firestore
- Skip events where _origin matches own service tag
- Conflict resolution: last-write-wins by server timestamp

### Acceptance criteria
- [ ] Firestore insert triggers ReplicaSink.onInsert() in Supabase sink
- [ ] Supabase insert triggers ReplicaSink.onInsert() in Firestore sink
- [ ] Event tagged with own _origin is ignored (no infinite loop)
- [ ] Event tagged with other origin IS replicated
- [ ] Untagged event (from real user) IS replicated
- [ ] ReplicaSink failure is logged; service continues (no crash)

### Configuration (environment variables)
- FIREBASE_SERVICE_ACCOUNT_JSON
- SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY
- REPLICATION_ORIGIN_TAG (default: "supabase-replicator")

### Out of scope
- Schema migration (data shape differences between stores)
- Conflict resolution beyond last-write-wins
- This service is decommissioned when the Flutter app is retired
