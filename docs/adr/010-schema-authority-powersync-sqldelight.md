# ADR-010: Schema authority — PowerSync runtime, SQLDelight compile-time only

**Status:** Accepted

## Context
PowerSync manages the runtime SQLite schema on clients. SQLDelight validates SQL queries
at compile time. These are different concerns and must not be confused.

## Decision
- Authoritative schema: `infrastructure/sync/schema/` (PowerSync schema definitions)
- SQLDelight `.sq` files: compile-time query validation stubs only
- Every `.sq` file has a comment: "COMPILE-TIME VALIDATION STUB ONLY. Runtime schema owned by PowerSync."
- SQLDelight migrations start from version 2 (PowerSync sets user_version=1)

## Consequences
- If the PowerSync schema and SQLDelight stubs diverge, queries will fail at runtime.
- CI cannot detect this divergence automatically; schema changes require updating both files.
- Trade-off: manual discipline required to keep stubs in sync with authoritative schema.
