# Starting a New App from the App Factory

This guide walks through creating a new application using the App Factory as a template.

---

## Prerequisites

- App Factory Phase 1 complete (`v1.0-template` tag)
- New app concept with a defined domain

---

## Steps

### 1. Clone and rename

```bash
git clone <app-factory-repo> my-new-app
cd my-new-app
git remote set-url origin <new-repo-url>
```

Update `rootProject.name` in `settings.gradle.kts`.
Update namespace/applicationId strings in `build.gradle.kts` files.

### 2. Delete the sample domain

Remove the meta/admin app domain:
- Delete `domain/src/commonMain/kotlin/com/appfactory/domain/model/FeatureFlag.kt`
- Delete `domain/src/commonMain/kotlin/com/appfactory/domain/port/FeatureFlagRepository.kt`
- Delete corresponding fakes and tests
- Delete `application/featureflags/`
- Delete meta/admin screens from `clients/shared-ui/features/`

### 3. Define your domain

Add your entities, value objects, and business rules to `domain/`.
Add port interfaces for anything your domain needs from the outside world.
Add `Fake*` implementations for each port in `domain/src/commonTest/.../fake/`.

### 4. Write domain tests first

Write property-based tests before implementing anything.
Verify: `./gradlew :domain:jvmTest`

### 5. Add infrastructure adapters (Phase 3+)

Follow the phase plan from `docs/architecture.md`.
Local persistence first, then backend, then sync, then connectors.

### 6. Run setup

```bash
bash scripts/setup.sh
```

---

## What stays the same across apps

- All of `build-logic/` (convention plugins)
- `gradle/libs.versions.toml` (version catalog — update as needed)
- `infrastructure/sync/` (PowerSync adapters)
- `infrastructure/auth/` (Supabase/Firebase auth adapters)
- `infrastructure/connectors/` (reusable connectors)
- `scripts/deploy/` (Docker setup)
- `docs/ai-workflow.md`

## What you replace

- `domain/` — your app's entities, ports, business rules
- `application/` — your app's use cases
- `clients/shared-ui/features/` — your app's screens
- `backend/` feature packages — your app's routes
