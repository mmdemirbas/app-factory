# AI Workflow Guide

**Read this before writing any code in this repository.**
This document defines the rules AI agents must follow when working in the App Factory codebase.

---

## Module Dependency Rules

The dependency arrow always points inward. Violations fail the CI build (ArchUnit hard gate).

```
domain          ← no imports from infrastructure, application, clients, or backend
application     ← may import domain only
infrastructure  ← may import domain and application
clients         ← may import domain, application, infrastructure
backend         ← may import domain, application, infrastructure
```

**Forbidden patterns — the build will fail if an agent writes these:**
- Any `import com.appfactory.infrastructure.*` inside `domain/` or `application/`
- Any `import com.appfactory.clients.*` inside `domain/` or `application/`
- Any `import com.appfactory.domain.model.*` inside `domain.common.*`
- Any business logic inside a Ktor route handler
- Any platform-specific import in a `commonMain` source set

---

## Definition of Done per Layer

### Adding a domain entity or port
1. Write the failing test first (property-based preferred)
2. Implement the entity or port interface
3. Add a `Fake*` implementation in `domain/src/commonTest/kotlin/.../fake/`
4. Verify ArchUnit tests still pass: `./gradlew :domain:jvmTest`

### Adding an infrastructure adapter
1. Identify which port interface it implements
2. Write an integration test (using Docker for real DBs)
3. Implement the adapter
4. Register in the appropriate Koin module
5. No domain logic in the adapter — only translation

### Adding a use case
1. Write a `.spec.md` file in the relevant `application/` package
2. Acceptance criteria in the spec become test function names
3. Use fakes for all dependencies — no real adapters in use case tests
4. One use case = one file = one `invoke` function

### Adding a backend route
1. Route handler calls exactly one use case
2. No business logic in the route handler — only parsing and serialization
3. Add request/response DTOs in the same feature package as the route

### Adding a Compose screen or component
1. New component → add `@Preview` function in same file
2. New component → add snapshot test in the shared-ui test suite
3. Business logic belongs in a ViewModel or use case, never in a Composable

---

## How to Add a New Connector

See `docs/connector-guide.md` for the step-by-step guide.

High level:
1. Define stable record types in `infrastructure/connectors/<name>/model/`
2. Define a concrete connector interface in `infrastructure/connectors/<name>/`
3. Implement the adapter (SDK types are private to the adapter file)
4. Register OAuth service in `OAuthService` enum if needed
5. Add `ConnectorDescriptor` to the registry
6. Write integration tests against the real API (or a sandbox)

---

## Common AI Mistakes in This Codebase

These are mistakes observed and corrected. Agents should check for these explicitly.

1. **Putting infrastructure imports in domain code.** The ArchUnit test will catch this,
   but it wastes a build cycle. Check imports before submitting.

2. **Using platform-specific APIs in `commonMain`.** Example: `java.util.UUID` in a
   `commonMain` file. Use `com.benasher44:uuid` (`uuid4()`) instead.

3. **Adding business logic to route handlers.** Route handlers parse + call use case + respond.
   If you find yourself writing an `if` about a business rule in a route handler, extract it.

4. **Modifying `.sq` files without updating dependent queries.** SQLDelight will fail to compile.
   Always check `TaskQueries` (or equivalent) after schema changes.

5. **Using `kotlin.Result` in domain code.** Use `DomainResult` from `domain.common` instead.
   `kotlin.Result` is for Kotlin-stdlib-level error handling; `DomainResult` carries domain errors.

6. **Creating mutable shared state in a use case.** Use cases should be stateless.
   State lives in repositories (infrastructure) or ViewModels (clients).

7. **Sharing `Fake*` classes across modules.** Due to Kotlin Multiplatform restrictions, `testFixtures` is unreliable for `commonMain`. Instead, `application/build.gradle.kts` natively maps `domain/src/commonTest/kotlin` and excludes `*Test.kt` files. Always name your test implementations starting with `Fake` and place them properly in `domain/src/commonTest/kotlin/.../fake/` so they successfully export to `application` tests.

---

## Architecture Decision Records

Significant design decisions are in `docs/adr/`. Agents must not override a recorded decision
without flagging the conflict explicitly in their response.
