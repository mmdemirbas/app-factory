## Feature: Create a feature flag

### Inputs
- key: String (must be lowercase snake_case)
- description: String
- defaultEnabled: Boolean (default false)

### Behavior
- Delegates validation to FeatureFlag.create() domain factory
- Persists via FeatureFlagRepository.save() on success
- Returns the created FeatureFlag

### Acceptance criteria
- [ ] Valid key and description creates and saves a flag with a generated id
- [ ] Blank key returns DomainError.ValidationFailed without calling repository
- [ ] Key with uppercase letters returns DomainError.ValidationFailed
- [ ] Key with spaces returns DomainError.ValidationFailed
- [ ] defaultEnabled=true is persisted correctly
- [ ] Repository.save() is called exactly once on success
- [ ] Repository.save() is never called on validation failure

### Out of scope
- Duplicate key detection (repository concern)
