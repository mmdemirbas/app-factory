# ADR-005: Koin over Hilt

**Status:** Accepted

## Context
Dependency injection for a KMP project.

## Decision
Use Koin.

## Consequences
- Koin is KMP-compatible. Hilt is Android-only.
- Explicit module declarations; no annotation processing.
- Trade-off: runtime DI errors instead of compile-time (unlike Hilt).
