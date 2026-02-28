# Current work: Phase 5 — Sync

## Goal
Implement PowerSync-based sync adapters and validate offline-first behavior end-to-end.

## Definition of done for Phase 5
- [ ] `PowerSyncKotlinAdapter` implemented for Android/Desktop targets.
- [ ] `PowerSyncWebAdapter` implemented for Wasm/Web target.
- [ ] Sync status surfaced in UI and backend-facing diagnostics.
- [ ] Offline write/read behavior verified, then synced when connectivity returns.
- [ ] End-to-end sync flow validated across at least two clients.

## Phase 4 completion snapshot
- [x] Supabase-backed remote adapters added for settings, feature flags, connectors, and auth.
- [x] Ktor routes added for auth/settings/feature flags/connectors.
- [x] Backend DI wiring updated to expose remote adapters.
- [x] Build/test baselines pass across backend and infrastructure modules.
