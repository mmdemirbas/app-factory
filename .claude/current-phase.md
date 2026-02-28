# Current work: Phase 5 — Sync

## Goal
Implement PowerSync-based sync adapters and validate offline-first behavior end-to-end.

## Definition of done for Phase 5
- [x] `PowerSyncKotlinAdapter` implemented for Android/Desktop targets.
- [x] `PowerSyncWebAdapter` implemented for Wasm/Web target.
- [x] Sync status surfaced in UI and backend-facing diagnostics.
- [x] Offline write/read behavior verified, then synced when connectivity returns.
- [ ] End-to-end sync flow validated across at least two clients.

## Phase 4 completion snapshot
- [x] Supabase-backed remote adapters added for settings, feature flags, connectors, and auth.
- [x] Ktor routes added for auth/settings/feature flags/connectors.
- [x] Backend DI wiring updated to expose remote adapters.
- [x] Build/test baselines pass across backend and infrastructure modules.

## Phase 5 progress snapshot
- [x] Sync status dashboard UI added with live state observation and manual trigger action.
- [x] Android/Desktop/Web entry points now wire platform `SyncEngine` adapters into shared UI.
- [x] Backend sync diagnostics routes are covered by route-level tests and explicit dependency wiring.
- [x] Sync adapters queue offline scopes and flush them on reconnect (covered by `PowerSyncKotlinAdapterTest`).
- [x] Local settings repository now returns/observes defaults when local DB is empty (offline-safe read path).
- [x] Desktop and Web client compile targets pass with current Phase 5 sync wiring.
- [x] Local backend transport sync engine added for Desktop/Web to validate cross-client end-to-end sync state against one backend instance.
- [x] Sync engine mode selection is explicit at composition roots (`SyncEngineMode`), removing hidden env/query runtime toggles.
- [x] Dashboard layout now handles unbounded web viewport height and keeps controls reachable with internal scrolling.
