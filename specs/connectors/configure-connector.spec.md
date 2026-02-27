## Feature: Configure a connector

### Inputs
- ConnectorDescriptor (from ConnectorRegistry.available())
- ConnectorConfig (credentials, fieldMappings, syncSchedule)

### Behavior
- If descriptor.oauthService is not null, initiates OAuth flow first
- OAuth credentials are merged into the config before saving
- Calls ConnectorConfig.validate() before persisting
- Persists via ConnectorRegistry.configure()
- Returns the ConfiguredConnector on success

### Acceptance criteria
- [ ] Non-OAuth connector with valid config is saved and returned
- [ ] OAuth connector triggers OAuthProvider.initiateFlow() exactly once
- [ ] OAuth credentials replace the config's credentials before saving
- [ ] Invalid config (empty credentials) returns DomainError.ValidationFailed
- [ ] OAuth flow failure propagates as DomainResult.Failure without calling registry

### Out of scope
- Retry on OAuth failure
- Duplicate connector detection
