# ADR-007: domain.common package merged into domain module

**Status:** Accepted

## Context
Generic types (EntityId, Timestamp, DomainResult) are used by both domain code
and infrastructure adapters. If they were in a separate module, every adapter would
depend on that module. If they're in domain, extracted libraries must depend on domain.

## Decision
Merge common types into `domain/` as a `domain.common` sub-package.
An ArchUnit rule prevents `domain.common` from importing the rest of `domain`.

## Consequences
- One fewer Gradle module.
- When connector/sync library extraction becomes real, `domain.common` becomes its own
  module — a mechanical file move, not a refactor.
- Trade-off: some conceptual co-location of generic types with app-specific domain code.
