# Phase 7 Multi-Tenancy Walkthrough

## What was completed
Phase 7 ("User System") has been completed end-to-end.

The implementation established team-aware ownership across domain, application, infrastructure, UI, and backend authorization. The final gap was API access control, now enforced in backend routes via JWT authentication and team membership validation.

## Implementation Details

1. **Domain team/user modeling**
   - Added and wired `User`, `Team`, and `TeamMembership` concepts in the domain model.
   - Added explicit `TeamRole` semantics for team membership.

2. **Team-scoped repositories and use cases**
   - Updated app settings, feature flags, and connector contracts to operate on `TeamId`.
   - Added active team management use cases in the application module:
     - `GetActiveTeamUseCase`
     - `ObserveActiveTeamUseCase`
     - `SwitchActiveTeamUseCase`

3. **UI team switching**
   - Shared UI now exposes active team and allows switching through the active team repository.

4. **Backend authorization hardening**
   - Added Ktor JWT authentication setup with bearer token verification.
   - Added shared team access guard requiring both:
     - valid authenticated subject from JWT
     - `X-Team-ID` request header
   - Added membership check through `TeamRepository` before serving team-scoped routes.
   - Removed fallback behavior that previously defaulted missing team context to `default_team`.

5. **Infrastructure membership adapter**
   - Added `SupabaseTeamRepository` to validate membership from `team_membership` table.
   - Added DI binding so backend routes can enforce team membership checks through domain port contracts.

## Validation Strategy

1. **Automated route behavior coverage**
   - Added backend tests for `/api/settings` access control to validate:
     - `401 Unauthorized` when bearer token is missing/invalid
     - `400 Bad Request` when `X-Team-ID` is missing
     - `403 Forbidden` when authenticated user is not a member of requested team
     - `200 OK` when membership validation passes

2. **Backend test run**
   - Executed:
     - `./gradlew :backend:test --no-daemon`
   - Result: passed.

## Results
Phase 7 goals are fully met: the app now has team-aware data ownership, active-team switching in UI/application flows, and backend access checks that constrain team-scoped API operations to authorized memberships.
