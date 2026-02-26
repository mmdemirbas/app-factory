# ADR-009: Supabase primary, Firebase migration path

**Status:** Accepted

## Context
New apps use Supabase. An existing Flutter/Firebase app needs migration.

## Decision
- Supabase is the primary backend.
- Firebase auth adapter is also implemented.
- MigratingAuthAdapter supports simultaneous dual-auth during migration.
- Bidirectional replication service handles data sync (see ADR-012).

## Consequences
- Migration is configuration-driven, not code-driven.
- Identity linking via internal user_id prevents auth provider coupling (see ADR-011).
