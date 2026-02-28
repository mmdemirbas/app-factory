# Current work: Phase 7 — User System

## Goal
Implement unified team & user model allowing collaborative multi-tenant configurations of the meta-app, replacing single-owner limits.

## Definition of done for Phase 7
- [ ] Base `User` and `Team` entity domain models created.
- [ ] Connectors and mapping references updated to belong to `TeamId` instead of global singletons.
- [ ] Team switching UI implemented.
- [ ] Access control limits API access purely to a users active team.

## Persistent Progress Tracking
Previous phase completions and walkthroughs are stored persistently in the repository at:
- `docs/phases/phase-5.md`
- `docs/phases/phase-5-walkthrough.md`
- `docs/phases/phase-6.md`
- `docs/phases/phase-6-walkthrough.md`
