## Feature: Sign out

### Inputs
- None (signs out current session)

### Behavior
- Calls AuthProvider.signOut()
- Returns Unit on success

### Acceptance criteria
- [ ] Successful sign-out returns DomainResult.Success(Unit)
- [ ] AuthProvider failure propagates as DomainResult.Failure
- [ ] AuthProvider.signOut() is called exactly once
