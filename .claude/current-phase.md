# Current work: Phase 4 — Backend and central storage

## Goal
Implement Supabase Postgres schema, remote repository adapters, Ktor routes, and Supabase Auth.

## Definition of done for Phase 4
- [ ] Supabase Postgres schema defined and migrations created.
- [ ] Remote repository adapters implemented connecting to Supabase.
- [ ] Ktor backend routes implemented for core entities.
- [ ] Supabase Auth integration for user management.
- [ ] Verified remote configuration from one device appears on another via backend sync.

## What's already done (Phase 3)
- SQLDelight schema mirrors established strictly in `domain/` for compile-time validation.
- Authoritative PowerSync JSON schema maintained in `infrastructure/sync/schema/`.
- `SqlDelightAppSettingsRepository`, `SqlDelightFeatureFlagRepository`, and `SqlDelightConnectorRegistry` local repository adapters implemented.
- `AppDatabase` Driver Factories and Koin Dependency Injection bindings constructed.
- `infrastructure` JVM testing validating SQL/Domain interoperability passes successfully.
