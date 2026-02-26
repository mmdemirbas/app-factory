# ADR-011: Identity linking for multi-issuer authentication

**Status:** Accepted

## Context
During migration, users may authenticate via Firebase OR Supabase.
Using the auth provider's subject ID as the system's user identity creates coupling.

## Decision
An `identity_link` table maps {firebase_uid, supabase_uid} → internal_user_id.
All data ownership, replication rules, and authorization use internal_user_id only.

## Consequences
- Auth provider can be swapped or dual-used without cascading data changes.
- identity_link table must be kept consistent during migration.
- Slightly more complex auth middleware.
