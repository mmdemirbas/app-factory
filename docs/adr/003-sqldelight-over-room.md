# ADR-003: SQLDelight over Room Multiplatform

**Status:** Accepted

## Context
KMP local database with compile-time query validation.

## Decision
Use SQLDelight.

## Consequences
- Queries are validated at compile time against the schema. Invalid SQL = build failure.
- Works across all KMP targets.
- IMPORTANT: Under PowerSync, SQLDelight `.sq` files are compile-time validation stubs only.
  PowerSync manages the runtime schema. See ADR-010.
- Trade-off: SQL must be written explicitly; no ORM-style query building.
