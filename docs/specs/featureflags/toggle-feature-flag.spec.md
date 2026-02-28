## Feature: Toggle a feature flag for a specific environment

### Inputs
- flagId: EntityId
- environment: Environment (DEV, STAGING, PROD)
- enabled: Boolean

### Behavior
- Loads flag by id from repository
- Sets environment override: environmentOverrides[environment] = enabled
- Updates updatedAt timestamp
- Persists via repository.save()
- Returns the updated flag

### Acceptance criteria
- [ ] Known flag gets environment override set correctly
- [ ] Unknown flagId returns DomainError.NotFound without calling save()
- [ ] Toggling PROD does not affect DEV or STAGING overrides
- [ ] updatedAt is updated on every toggle
- [ ] Repository.save() is called with the updated flag, not the original
