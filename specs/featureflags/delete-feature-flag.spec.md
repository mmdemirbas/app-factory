## Feature: Delete a feature flag

### Inputs
- flagId: EntityId

### Behavior
- Verifies the flag exists
- Calls FeatureFlagRepository.delete()
- Returns Unit on success

### Acceptance criteria
- [ ] Known flagId is deleted and returns success
- [ ] Unknown flagId returns DomainError.NotFound
- [ ] Repository.delete() is called exactly once on success
- [ ] Repository.delete() is never called when flag not found
