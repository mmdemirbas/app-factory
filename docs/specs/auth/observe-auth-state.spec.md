## Feature: Observe authentication state

### Inputs
- None

### Behavior
- Returns AuthProvider.observeAuthState() as Flow<AuthState>
- States: Unauthenticated, Authenticated(user), Loading

### Acceptance criteria
- [ ] Returns a Flow emitting AuthState values
- [ ] Emits Unauthenticated when no user is signed in
- [ ] Emits Authenticated with correct user when signed in
- [ ] Emits Loading during sign-in/sign-out transitions

### Notes
Thin delegation. Tests verify correct passthrough and that
AuthenticatedUser.internalUserId is always present in Authenticated state.
