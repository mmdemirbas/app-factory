# ADR-006: Port interfaces live in the domain module

**Status:** Accepted

## Context
Where to define port interfaces in a hexagonal architecture.

## Decision
All port interfaces (SyncEngine, AuthProvider, ConnectorRegistry, etc.) live in `domain/`.

## Consequences
- Domain is self-contained: it declares what it needs; infrastructure fulfills it.
- No intermediate "ports module."
- Trade-off: when extracting connectors/sync as libraries, the interfaces travel with the
  library and must not depend on app-specific domain types. `domain.common` types are
  explicitly designed to be extractable for this reason. See ADR-007.
