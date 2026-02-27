## Feature: Sign in

### Inputs
- Credentials (EmailPassword or OAuthCode)

### Behavior
- Delegates to AuthProvider.signIn(credentials)
- Returns AuthenticatedUser using internalUserId (never auth provider's subject id)
- On failure, returns DomainError

### Acceptance criteria
- [ ] Valid EmailPassword credentials return AuthenticatedUser
- [ ] Valid OAuthCode credentials return AuthenticatedUser
- [ ] AuthenticatedUser.internalUserId is set (not null, not blank)
- [ ] Invalid credentials return DomainResult.Failure
- [ ] AuthProvider.signIn() is called exactly once

### Out of scope
- Token refresh (separate use case)
- Identity linking logic (infrastructure/auth concern, see ADR-011)
