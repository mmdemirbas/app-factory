## Feature: Remove a connector

### Inputs
- ConnectorId of an existing configured connector

### Behavior
- Calls ConnectorRegistry.remove()
- Does NOT revoke OAuth tokens (that is a separate explicit action)
- Returns Unit on success

### Acceptance criteria
- [ ] Known connectorId is removed and returns success
- [ ] Unknown connectorId returns DomainError.NotFound
- [ ] Registry.remove() is called exactly once
- [ ] OAuthProvider is never called

### Out of scope
- OAuth token revocation (separate use case)
- Cascading cleanup of synced data
