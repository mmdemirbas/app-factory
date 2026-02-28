# Current work: Phase 7 — User System

## Goal
Implement unified team and user model allowing collaborative multi-tenant configurations of the meta-app, replacing single-owner limits.

## Definition of done for Phase 7
- [x] Base `User` and `Team` entity domain models created.
- [x] Connectors and mapping references updated to belong to `TeamId` instead of global singletons.
- [x] Team switching UI implemented.
- [x] Access control limits API access purely to a user's active team.

## Phase 7 completion snapshot
- [x] Added `User`, `Team`, `TeamMembership`, and `TeamRole` domain modeling to represent collaborative ownership.
- [x] Migrated team-scoped ports and adapters (`AppSettingsRepository`, `FeatureFlagRepository`, `ConnectorRegistry`) to require `TeamId`.
- [x] Added active-team application use cases (`GetActiveTeamUseCase`, `ObserveActiveTeamUseCase`, `SwitchActiveTeamUseCase`) and wired team switching into shared UI.
- [x] Removed permissive `default_team` backend access path in protected routes and introduced explicit team authorization checks.
- [x] Added backend JWT authentication and team-membership enforcement for team-scoped endpoints.
- [x] Added route-level backend tests to verify `401` (missing token), `400` (missing team header), `403` (not a member), and `200` (authorized) behavior.

## Persistent Progress Tracking
Previous phase completions and walkthroughs are stored persistently in the repository at:
- `docs/phases/phase-5.md`
- `docs/phases/phase-5-walkthrough.md`
- `docs/phases/phase-6.md`
- `docs/phases/phase-6-walkthrough.md`
