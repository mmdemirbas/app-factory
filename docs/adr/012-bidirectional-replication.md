# ADR-012: Bidirectional replication — dual change listeners with origin tagging

**Status:** Accepted

## Context
During Firebase → Supabase migration, the old Flutter app may still write directly
to Firestore. These writes must be captured without routing through the new backend.

## Decision
Two change listeners run concurrently:
1. Firestore change listener (Firebase SDK) → pushes to Supabase via ReplicaSink
2. Postgres change listener (Supabase Realtime / WAL) → pushes to Firestore

Loop prevention: writes originating from the replicator are tagged with an origin marker.
Each listener ignores changes tagged with its own origin.
Conflict resolution: last-write-wins by server timestamp.

Runs as a separate Docker container. Zero domain knowledge.

## Consequences
- Old Flutter app writes are captured without code changes to the old app.
- Loop prevention must be verified in integration tests.
- Bidirectional sync adds complexity; decommission this service when migration completes.
