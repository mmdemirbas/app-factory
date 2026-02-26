# ADR-004: PowerSync for offline-first sync

**Status:** Accepted

## Context
Offline-first sync between client SQLite and Supabase Postgres.

## Decision
Use PowerSync behind the `SyncEngine` port interface.

## Consequences
- Handles bidirectional sync, partial replication, conflict resolution.
- Two adapters: Kotlin SDK (Android/iOS/Desktop), Web SDK via JS interop (Web).
- SQLDelight schema constraint: see ADR-010.
- Trade-off: Beta SQLDelight support; tied to Postgres on the backend.
