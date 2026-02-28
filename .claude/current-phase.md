# Current work: Phase 3 — Local persistence

## Goal
Implement SQLDelight schema stubs, local repository adapters, and wire everything up for local-only operation.

## Definition of done for Phase 3
- [ ] SQLDelight schema mirrors established (for compile-time validation) in `domain` or `infrastructure` as per ADR.
- [ ] Local repository adapters implemented using SQLDelight (`infrastructure/storage/local/`).
- [ ] Integration tests against local SQLite verifying persistence.
- [ ] DI wiring configured to allow local-only operation by default.
- [ ] App runs safely across platforms retaining offline data state.

## What's already done (Phase 2)
- Domain entities (`AppSettings`, `FeatureFlag`, etc.) configured.
- Port interfaces (`SyncEngine`, `AuthProvider`, `ConnectorRegistry`, `AppSettingsRepository`, etc.) established.
- Fake implementations supplied for all ports.
- App use cases implemented with 100% property-based testing.
- ArchUnit verifications for 0 infrastructure dependencies remain unbroken.
