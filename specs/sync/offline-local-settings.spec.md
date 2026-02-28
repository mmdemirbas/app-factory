## Feature: Offline-safe local settings defaults

### Inputs
- Local settings repository request when local DB has no `AppSettings` row yet.
- Local settings update payload.

### Behavior
- `getSettings()`:
  - Returns `DomainResult.Success(AppSettings())` when local DB is empty.
  - Returns persisted local values when present.
- `observeSettings()`:
  - Emits `AppSettings()` when local DB is empty.
  - Emits updated persisted local values after writes.
- `updateSettings(settings)`:
  - Persists settings to local DB and returns success.

### Acceptance criteria
- [x] Empty local DB reads return default settings instead of an error.
- [x] Empty local DB observe stream emits default settings.
- [x] Updating settings persists and can be read back locally.

### Out of scope
- Remote fetch fallback when local DB is empty.
- Conflict resolution between local and remote settings.
