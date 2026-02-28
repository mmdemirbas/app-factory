# App Factory — Architecture Design Document v2

> **Status:** Finalized pre-implementation design. Single source of truth before any code is written.
> Changes require explicit reasoning and must be recorded as an Architecture Decision Record in `docs/adr/`.

---

## 1. Purpose and Philosophy

The App Factory is a monorepo that is itself a fully running application — a meta/admin app for managing connectors, feature flags, and sync status. It is not a code generator, not a scaffold, not a framework. It is a working app that simultaneously serves as the template every future application is cloned from.

Every future app starts by cloning this repo, deleting the sample domain, and building the real domain on top of proven, working infrastructure.

### Priorities (in order)

1. **Clean architecture** — structure enforces correctness mechanically, not by convention
2. **AI compatibility** — well-defined, enforced boundaries make agent-generated code predictable and reviewable
3. **No lock-in** — every external dependency is hidden behind a port interface; swapping a vendor is a configuration change, not a rewrite
4. **Fast start** — cloning the repo produces a running app on all four platforms within minutes

### The core rule

The domain never knows the infrastructure exists. The dependency arrow always points inward: infrastructure depends on domain, never the reverse. This is not a convention — it is enforced mechanically by ArchUnit as a build-time gate in CI.

---

## 2. Language and Stack Decision

### Why Kotlin Multiplatform with Compose Multiplatform

The target platforms are Android, iOS, Desktop (JVM), and Web. Given this constraint alongside a Kotlin backend preference, KMP is the only realistic choice:

- **React Native + Expo** covers Android/iOS/Web but not Desktop JVM. Cross-language sharing between a Kotlin backend and a TypeScript client introduces a boundary that must be maintained forever.
- **Flutter/Dart** covers all four platforms but sharing domain models, validation rules, and serialization logic between a Dart client and a Kotlin backend requires duplication or a cross-language bridge.
- **KMP** is the only option where the domain module — entities, business rules, port interfaces, use cases — is written once in Kotlin and used unchanged by all four clients and the Ktor backend. A change to a domain model propagates everywhere and the compiler surfaces every breakage immediately.

For the web target, Compose for Web (Wasm) is the starting choice. A performance spike is part of Phase 1. No premature pessimism about maturity.

### Decided stack

| Concern | Technology | Notes |
|---|---|---|
| Shared domain + business logic | Kotlin (KMP `commonMain`) | No platform imports, no vendor SDKs |
| Android client | Compose Multiplatform | |
| iOS client | Compose Multiplatform | Minimal `main.swift` shell in Xcode project |
| Desktop client | Compose Multiplatform (JVM) | |
| Web client | Compose for Web (Wasm) | Performance spike in Phase 1 |
| Backend | Ktor (Kotlin) | |
| Local DB | SQLDelight | Query validation only under PowerSync — see Section 6.2 |
| Offline sync (mobile/desktop) | PowerSync Kotlin SDK | Behind `SyncEngine` interface |
| Offline sync (web) | PowerSync Web SDK (JS interop) | Different adapter, same `SyncEngine` port |
| Central DB | Supabase (Postgres) | Behind port interface |
| Auth | Supabase Auth (primary) | Firebase Auth adapter also present for migration |
| OAuth flows | Nango | Behind `OAuthProvider` interface — replaceable |
| Dependency injection | Koin | KMP-compatible, no annotation magic |
| Serialization | kotlinx.serialization | |
| Async | Kotlin Coroutines + Flow | |
| Build | Gradle (version catalog + convention plugins) | |
| Dependency enforcement | ArchUnit | Build-time gate in CI over compiled bytecode |
| Test coverage | Kover | Hard gate on domain/application modules |
| Mutation testing | Pitest | Hard gate on domain module |
| Static analysis | Detekt | Hard gate on domain/application, soft on infra/UI |

### Why Ktor over Spring Boot

Spring Boot's annotation-driven magic works against the architecture goals here. It encourages implicit wiring, makes it harder to trace what depends on what, and constrains design decisions to what Spring's patterns support. Ktor is Kotlin-native, explicit, lightweight, and has no framework-level opinions about application structure.

### Domain dependencies

The domain module depends only on Kotlin and a small approved set of language-level libraries: `kotlinx.coroutines` / `Flow`, `kotlinx.datetime`, and `kotlinx.serialization`. No platform APIs, no vendor SDKs, no framework imports. This is a mechanical invariant enforced by ArchUnit.

---

## 3. Monorepo Structure

Everything — clients, backend, infrastructure adapters, scripts, documentation — lives in one repository.

```
app-factory/
│
├── build-logic/                  # Gradle convention plugins
│   └── src/main/kotlin/
│       ├── kmp-library.gradle.kts
│       ├── kmp-application.gradle.kts
│       ├── android-app.gradle.kts
│       ├── jvm-backend.gradle.kts
│       └── detekt-rules.gradle.kts
│
├── gradle/
│   └── libs.versions.toml        # Single version catalog. No version strings elsewhere.
│
├── domain/                       # The heart of the system — see Section 4
│
├── application/                  # Use cases — see Section 5
│
├── infrastructure/               # All adapter implementations — see Section 6
│   ├── storage/
│   ├── sync/
│   ├── auth/
│   ├── connectors/
│   └── replication/
│
├── clients/
│   ├── shared-ui/                # Design system + shared Compose screens
│   ├── android/
│   ├── ios/
│   ├── desktop/
│   └── web/
│
├── backend/                      # Ktor — organized by feature, not by technical layer
│   ├── connectors/
│   ├── featureflags/
│   ├── sync/
│   ├── auth/
│   └── Application.kt
│
├── scripts/
│   ├── setup.sh
│   ├── migrate.sh
│   └── deploy/
│       ├── docker-compose.yml
│       ├── docker-compose.prod.yml
│       ├── replication/
│       └── supabase/
│
└── docs/
    ├── architecture.md
    ├── adr/
    ├── ai-workflow.md
    ├── connector-guide.md
    └── new-app-guide.md
```

### Module dependency graph

```
          ┌─────────────┐
          │   domain    │  ← depends only on Kotlin + approved language libs
          └──────┬──────┘
                 │
          ┌──────▼──────┐
          │ application │  ← depends only on domain
          └──────┬──────┘
                 │
    ┌────────────┼────────────┐
    ▼            ▼            ▼
infrastructure  clients    backend
```

ArchUnit enforces this graph as a build-time CI gate. Any import violating it fails the build.

---

## 4. Domain Module

The `domain` module contains:

1. **Domain entities and value objects**
2. **Business rules**
3. **Port interfaces** — everything the domain needs from the outside world
4. **Domain events**

It depends only on Kotlin and the approved language-level libraries. No platform APIs, no vendor SDKs.

### The `common` sub-package

A `domain.common` package holds truly generic types with no business logic: `EntityId`, `Timestamp`, `Result`, `ValidationError`, `Page`. These are used by infrastructure adapters and connectors, so if a connector is later extracted as a standalone library, these types travel with it without carrying any app-specific domain concepts.

An ArchUnit rule enforces that nothing in `domain.common` imports from elsewhere in `domain`. This keeps the package extractable as its own Gradle module when library extraction becomes concrete.

```kotlin
// domain/src/commonMain/kotlin/common/EntityId.kt
@JvmInline
value class EntityId(val value: String) {
    init { require(value.isNotBlank()) { "EntityId cannot be blank" } }
    companion object { fun generate() = EntityId(uuid4().toString()) }
}
```

### Port interfaces

All ports live in `domain`. The domain declares what it needs; infrastructure fulfills it. There is no intermediate "ports module."

```kotlin
// domain/src/commonMain/kotlin/port/SyncEngine.kt
interface SyncEngine {
    suspend fun syncNow(scope: SyncScope): SyncResult
    fun observeSyncState(scope: SyncScope): Flow<SyncState>
}

// domain/src/commonMain/kotlin/port/AuthProvider.kt
interface AuthProvider {
    suspend fun signIn(credentials: Credentials): AuthResult
    suspend fun signOut()
    fun observeAuthState(): Flow<AuthState>
    suspend fun currentUser(): User?
}

// domain/src/commonMain/kotlin/port/ConnectorRegistry.kt
interface ConnectorRegistry {
    fun available(): List<ConnectorDescriptor>
    fun configured(): List<ConfiguredConnector>
    suspend fun configure(descriptor: ConnectorDescriptor, config: ConnectorConfig)
    suspend fun remove(connectorId: ConnectorId)
    suspend fun test(connectorId: ConnectorId): TestResult
}

// domain/src/commonMain/kotlin/port/OAuthProvider.kt
interface OAuthProvider {
    suspend fun initiateFlow(service: OAuthService): OAuthFlowResult
    suspend fun refreshToken(connectorId: ConnectorId): TokenRefreshResult
    suspend fun revokeToken(connectorId: ConnectorId)
}

// domain/src/commonMain/kotlin/port/ReplicaSink.kt
interface ReplicaSink {
    suspend fun onInsert(table: String, record: Map<String, Any?>)
    suspend fun onUpdate(table: String, record: Map<String, Any?>, old: Map<String, Any?>)
    suspend fun onDelete(table: String, id: String)
}
```

### Domain testing

Property-based tests are preferred over example-based tests for business rules. They probe edge cases AI-generated code commonly misses and cannot be satisfied by empty assertions.

```kotlin
class ConnectorConfigPropertyTest : StringSpec({
    "connector config with empty credentials always fails validation" {
        checkAll(Arb.connectorDescriptor()) { descriptor ->
            val config = ConnectorConfig(descriptor.id, credentials = emptyMap())
            config.validate().isFailure shouldBe true
        }
    }
})
```

---

## 5. Application Module

The `application` module contains use cases. It depends only on `domain`. Organized by feature, mirroring the backend structure, so a feature addition touches coherent directories in both modules.

```
application/
├── connectors/
│   ├── ConfigureConnectorUseCase.kt
│   ├── RemoveConnectorUseCase.kt
│   └── TestConnectorUseCase.kt
├── featureflags/
│   ├── CreateFeatureFlagUseCase.kt
│   └── ToggleFeatureFlagUseCase.kt
└── sync/
    └── TriggerSyncUseCase.kt
```

A use case: single public `invoke` function, receives ports via constructor injection, orchestrates domain objects, has zero knowledge of HTTP, databases, UI, or platform. Use cases are shared by the Ktor backend and all client platforms.

```kotlin
// application/connectors/ConfigureConnectorUseCase.kt
class ConfigureConnectorUseCase(
    private val registry: ConnectorRegistry,
    private val oauthProvider: OAuthProvider
) {
    suspend operator fun invoke(
        descriptor: ConnectorDescriptor,
        config: ConnectorConfig
    ): Result<ConfiguredConnector> = runCatching {
        val oauthResult = oauthProvider.initiateFlow(descriptor.oauthService)
        val enrichedConfig = config.withCredentials(oauthResult.credentials)
        registry.configure(descriptor, enrichedConfig)
        registry.configured().first { it.descriptor.id == descriptor.id }
    }
}
```

---

## 6. Infrastructure Module

All adapter implementations. Depends on `domain` and `application`. Organized into sub-packages by concern — logical separation enforced by ArchUnit, not by separate Gradle modules.

### 6.1 Storage adapters (`infrastructure/storage/`)

Two adapters implement repository ports: Supabase (remote) and SQLDelight (local). Both implement identical interfaces. DI configuration selects which is injected based on platform and connectivity.

The use-case decides whether to read from local or remote storage by selecting the appropriate repository. This is where offline-first vs online-first strategy lives — at the use-case level, not as a mode on `SyncEngine`. A use case needing fresh data injects the remote repository. A use case needing offline support injects the local repository. A use case wanting fast local reads with background sync injects both and applies its own staleness logic.

```kotlin
// infrastructure/storage/supabase/SupabaseTaskRepository.kt
class SupabaseTaskRepository(
    private val client: SupabaseClient,
    private val mapper: TaskMapper
) : TaskRepository {
    override suspend fun getAll(): List<Task> =
        client.from("tasks").select().decodeList<TaskDto>().map(mapper::toDomain)
}

// infrastructure/storage/local/SqlDelightTaskRepository.kt
class SqlDelightTaskRepository(
    private val queries: TaskQueries,
    private val mapper: TaskMapper
) : TaskRepository {
    override suspend fun getAll(): List<Task> =
        queries.selectAll().executeAsList().map(mapper::toDomain)
}
```

### 6.2 Sync adapter (`infrastructure/sync/`) — Schema authority

**Critical constraint:** Under PowerSync, the runtime database schema is managed by PowerSync itself, not by SQLDelight. SQLDelight `.sq` files serve as **compile-time query validators only** — they validate that queries reference valid tables and columns at build time, but PowerSync creates and owns the actual SQLite schema at runtime.

Additional PowerSync constraints:
- PowerSync sets the database `user_version` to 1. SQLDelight schema migrations must start from version 2.
- SQLDelight support in the PowerSync Kotlin MPP SDK is Beta.
- Triggers, indexes, and views in `.sq` files are ignored by PowerSync unless raw table mode is configured.

**Schema authority rule:** The authoritative schema lives in `infrastructure/sync/schema/`. SQLDelight `.sq` files in `domain/` are maintained as mirrors of this schema for compile-time query validation only. Every `.sq` file contains a comment stating this explicitly.

This is recorded in ADR-010.

```sql
-- domain/src/commonMain/sqldelight/Task.sq
-- COMPILE-TIME VALIDATION STUB ONLY.
-- Runtime schema is owned by PowerSync. See infrastructure/sync/schema/task.json
-- Keep this file in sync with the PowerSync schema definition.
CREATE TABLE Task (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    completed INTEGER NOT NULL DEFAULT 0,
    updated_at INTEGER NOT NULL
);

selectAll:
SELECT * FROM Task ORDER BY updated_at DESC;

upsert:
INSERT OR REPLACE INTO Task(id, title, completed, updated_at)
VALUES (?, ?, ?, ?);
```

**Sync engine adapters:**

`SyncEngine` has two adapters:
- `PowerSyncKotlinAdapter` — Android, iOS, Desktop. Uses the PowerSync Kotlin MPP SDK.
- `PowerSyncWebAdapter` — Web client. Uses the PowerSync Web SDK via JS interop.

Same port, different adapter per platform. DI wiring in each platform's entry point selects the correct adapter. No other code is aware of the distinction.

**Conflict resolution:** Last-write-wins by `updated_at` server timestamp, documented per entity. CRDT-based merging is a future adapter concern; the `SyncEngine` interface does not change.

### 6.3 Auth adapters (`infrastructure/auth/`)

Three adapters implement `AuthProvider`:

```kotlin
class SupabaseAuthAdapter(private val client: SupabaseClient) : AuthProvider { ... }

class FirebaseAuthAdapter(private val firebaseAuth: FirebaseAuth) : AuthProvider { ... }

// Used during Firebase → Supabase migration window.
// Reads from Firebase, writes to both. Flips to Supabase-only when migration completes.
class MigratingAuthAdapter(
    private val firebase: FirebaseAuthAdapter,
    private val supabase: SupabaseAuthAdapter,
    private val identityLinkRepository: IdentityLinkRepository
) : AuthProvider { ... }
```

**Identity linking:** During migration, Firebase UIDs and Supabase UIDs must not be treated as interchangeable. An `identity_link` table maps both to an internal `user_id` that is the authoritative identity across the entire system. All data ownership, replication rules, and authorization are based on internal `user_id`, never on the auth provider's subject ID directly. This prevents a class of data ownership bugs that are difficult to diagnose after the fact. Recorded in ADR-011.

```kotlin
// domain/src/commonMain/kotlin/model/IdentityLink.kt
data class IdentityLink(
    val internalUserId: EntityId,
    val firebaseUid: String?,
    val supabaseUid: String?,
    val createdAt: Timestamp
)
```

### 6.4 Connector adapters (`infrastructure/connectors/`)

Each connector adapter defines its own stable intermediate record types and a concrete interface. These types are owned by the connector package — not SDK types, not domain types. The adapter maps `SDK type → connector record type` entirely internally. The application layer maps `connector record type → domain type`.

This means:
- Swapping the underlying Google Sheets SDK affects only a private mapping function inside the adapter. The connector's public interface and record types are stable.
- Connectors are domain-agnostic and extractable as standalone libraries.
- No SDK type ever crosses a package boundary.

```kotlin
// infrastructure/connectors/googlesheets/model/GoogleSheetsRow.kt
// Stable, connector-owned. Not an SDK type. Not a domain type.
data class GoogleSheetsRow(
    val spreadsheetId: String,
    val sheetName: String,
    val rowIndex: Int,
    val cells: Map<String, CellValue>
)

// infrastructure/connectors/googlesheets/GoogleSheetsConnector.kt
interface GoogleSheetsConnector {
    suspend fun fetchRows(config: ConnectorConfig): List<GoogleSheetsRow>
    suspend fun pushRows(rows: List<GoogleSheetsRow>, config: ConnectorConfig): PushResult
    fun observeChanges(config: ConnectorConfig): Flow<RemoteChange<GoogleSheetsRow>>
}

// infrastructure/connectors/googlesheets/GoogleSheetsConnectorAdapter.kt
class GoogleSheetsConnectorAdapter(
    private val sheetsApi: SheetsApiClient  // SDK type — never escapes this class
) : GoogleSheetsConnector {
    override suspend fun fetchRows(config: ConnectorConfig): List<GoogleSheetsRow> {
        val sdkResponse = sheetsApi.spreadsheets().values().get(...).execute()
        return sdkResponse.toGoogleSheetsRows()
    }

    // Private. The only place SDK types appear.
    private fun SheetsApiResponse.toGoogleSheetsRows(): List<GoogleSheetsRow> { ... }
}
```

Connector-specific utilities (parsing multi-level headers, normalizing field types, rate limit handling) live as private extension functions inside the adapter. They do not leak into domain or shared utility modules.

**OAuth for connectors:** `OAuthProvider` is implemented by `NangoOAuthAdapter` in `infrastructure/auth/`. Nango is self-hostable via Docker Compose for local development. It is accessed exclusively through the `OAuthProvider` interface. Replacing it means writing a new adapter and changing one DI binding.

### 6.5 Replication service (`infrastructure/replication/`)

The replication service enables true bidirectional sync during the Firebase → Supabase migration. This is required because the old Flutter app writes directly to Firestore during the migration window — writes that must be captured without routing through the new backend.

**Architecture: dual change listeners with origin tagging**

Two listeners run concurrently:
- A **Firestore change listener** (Firebase SDK) captures Firestore writes and pushes them to Supabase via `ReplicaSink`.
- A **Postgres change listener** (Supabase Realtime or WAL logical replication) captures Supabase writes and pushes them to Firestore.

**Loop prevention:** Every write originating from the replication service is tagged with an `_origin` marker. Each listener ignores changes tagged with its own origin, breaking the reflection loop without requiring distributed coordination.

**Conflict resolution:** Last-write-wins by server timestamp, consistent with the general sync strategy. During the migration window, simultaneous edits to the same record from both apps are not expected; this strategy is sufficient.

```kotlin
// infrastructure/replication/FirestoreToSupabaseReplicator.kt
class FirestoreToSupabaseReplicator(
    private val firestoreListener: FirestoreChangeListener,
    private val supabaseSink: ReplicaSink,
    private val originTag: OriginTag
) {
    suspend fun start() {
        firestoreListener.changes()
            .filter { !it.isTaggedWith(originTag) }   // loop prevention
            .collect { change ->
                when (change) {
                    is Insert -> supabaseSink.onInsert(
                        change.collection,
                        change.data.withTag(originTag)
                    )
                    is Update -> supabaseSink.onUpdate(
                        change.collection,
                        change.data.withTag(originTag),
                        change.old
                    )
                    is Delete -> supabaseSink.onDelete(change.collection, change.id)
                }
            }
    }
}
```

The replication service runs as a separate Docker container, configured entirely by environment variables, with zero domain knowledge. Started during the migration window, stopped when the Flutter app is retired. All configuration, Docker setup, and operational runbook live in `scripts/deploy/replication/`. No manual steps are undocumented.

Recorded in ADR-012.

---

## 7. Backend Architecture (Ktor)

The backend is organized by feature (screaming architecture), not by technical layer. The top level tells you what the application does, not how it is built. Adding a feature means adding a new directory and touching nothing else.

```
backend/
├── connectors/
│   ├── ConnectorRoutes.kt
│   ├── ConnectorDto.kt
│   └── ConnectorModule.kt     # Koin DI wiring for this feature
├── featureflags/
│   ├── FeatureFlagRoutes.kt
│   ├── FeatureFlagDto.kt
│   └── FeatureFlagModule.kt
├── sync/
│   ├── SyncRoutes.kt
│   └── SyncModule.kt
├── auth/
│   ├── AuthRoutes.kt
│   └── AuthModule.kt
└── Application.kt              # Server startup: installs plugins, registers all modules
```

`application/` mirrors the same structure, so a complete feature addition touches `application/newfeature/` and `backend/newfeature/` only — nowhere else.

Route handlers are thin: parse request, call use case, serialize response. Business logic in a route handler is a violation.

```kotlin
// backend/connectors/ConnectorRoutes.kt
fun Route.connectorRoutes(configureConnector: ConfigureConnectorUseCase) {
    post("/connectors/{id}/configure") {
        val descriptor = call.receive<ConnectorDescriptorDto>().toDomain()
        val config = call.receive<ConnectorConfigDto>().toDomain()
        configureConnector(descriptor, config)
            .fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it.toDto()) },
                onFailure = { call.respond(HttpStatusCode.BadRequest, it.message) }
            )
    }
}
```

### Backend deployment

Packaged as a Docker image. `scripts/deploy/docker-compose.yml` starts the backend, Postgres, and Nango with a single command. The same image deploys to any container platform with a free tier (Railway, Fly.io, Render).

All environment variables documented in `.env.example`. Setup script generates `.env` and verifies all required values before starting.

---

## 8. Client Architecture

### Shared UI module

```
clients/shared-ui/
├── designsystem/
│   ├── theme/
│   ├── components/
│   └── preview/
├── navigation/
└── features/
    ├── connectors/
    ├── featureflags/
    └── syncstatus/
```

Every component in `designsystem/components` has a `@Preview` and a snapshot test. Missing either triggers a soft-gate warning in CI.

**Design system:** built from scratch, covering components needed by actual features. No premature extraction. External Compose libraries evaluated case by case; adoption recorded as an ADR.

### Navigation

Uses the **JetBrains Multiplatform Navigation library**: `org.jetbrains.androidx.navigation:navigation-compose`. This is distinct from the AndroidX navigation artifact and is required for correct behavior on non-Android targets. Reference: [JetBrains Compose Multiplatform Navigation docs](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation.html). Recorded in ADR-013.

```kotlin
// clients/shared-ui/navigation/Routes.kt
@Serializable object ConnectorList
@Serializable data class ConnectorDetail(val connectorId: String)
@Serializable object FeatureFlags
@Serializable object SyncStatus
```

### Platform entry points

Each platform module contains only: entry point and bootstrapping, DI wiring for this platform, platform-specific overrides where necessary. All real code lives in `shared-ui` or deeper.

### iOS specifics

Minimal `main.swift` and Xcode project in `clients/ios/`. Not modified after initial setup. App Store distribution target; Apple compliance requirements configured during Phase 1.

---

## 9. AI-Assisted Development Workflow

### The core problem

AI agents generate code faster than humans can review it. The bottleneck is verification. The solution is mechanical, automated verification as the primary quality gate — not human code review.

### Hard gates (CI fails, no exceptions)

**1. Dependency direction (ArchUnit — build-time CI gate over compiled bytecode)**

```kotlin
@ArchTest
val domainDoesNotDependOnInfrastructure: ArchRule =
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("..infrastructure..")

@ArchTest
val commonDoesNotImportRestOfDomain: ArchRule =
    noClasses().that().resideInAPackage("..domain.common..")
        .should().dependOnClassesThat()
        .resideInAPackage("..domain..").and()
        .doNotResideInAPackage("..domain.common..")
```

**2. Test coverage (Kover):** domain and application modules fail CI below 90% line coverage.

**3. Mutation testing (Pitest):** domain module fails CI above 20% surviving mutations. AI-generated tests that pass without verifying anything will not survive mutation testing.

**4. SQLDelight compilation:** invalid queries or schema changes that break queries are compile errors before tests run.

### Soft gates (reports only)

**5. Detekt** — strict on `domain`/`application`, lenient on `infrastructure`/`clients`.

**6. Coverage report** on infrastructure and UI — reported, no hard threshold.

**7. Compose snapshot tests** — diffs reported, do not block merge.

### The AI agent instruction file: `docs/ai-workflow.md`

Every AI agent reads this before writing code. Contents: module dependency rules, forbidden patterns with examples, definition of done per layer, how to add a connector, how to add a feature flag, how to add a use case, common mistakes observed in this codebase (updated as new patterns emerge).

The quality of this file is the largest single multiplier on AI agent output quality.

### Specification-driven development

A `.spec.md` file is written before any agent begins a task:

```markdown
## Feature: Connector sync scheduling

### Inputs
- ConnectorId
- SyncSchedule: immediate | hourly | daily | manual

### Behavior
- Storing a schedule persists it via ConnectorRepository
- Hourly schedule triggers syncNow once per hour
- Immediate triggers syncNow once then resets to manual

### Acceptance criteria
- [ ] Given hourly schedule, when 1 hour elapses, syncNow is called exactly once
- [ ] Given manual schedule, syncNow is never called automatically
- [ ] Changing from hourly to manual stops future auto-sync

### Out of scope
- UI for schedule configuration (separate task)
- Retry logic on failure (separate task)
```

### Test fakes

Every port interface has a `Fake*` in `domain/src/commonTest/kotlin/fake/`. All domain and application tests use fakes — no real DB, no network, fully deterministic.

To allow the `application` module to access these fakes natively without executing the `domain` module's actual tests a second time during the build, `application/build.gradle.kts` uses a Gradle source directory mapping that filters out files ending in `Test.kt` or `Spec.kt`.

```kotlin
class FakeConnectorRegistry : ConnectorRegistry {
    private val connectors = mutableMapOf<ConnectorId, ConfiguredConnector>()
    val configureCallCount get() = _configureCallCount
    private var _configureCallCount = 0

    override fun available(): List<ConnectorDescriptor> = ConnectorDescriptor.all()
    override fun configured(): List<ConfiguredConnector> = connectors.values.toList()
    override suspend fun configure(descriptor: ConnectorDescriptor, config: ConnectorConfig) {
        _configureCallCount++
        connectors[descriptor.id] = ConfiguredConnector(descriptor, config)
    }
    override suspend fun test(connectorId: ConnectorId): TestResult = TestResult.Success
    override suspend fun remove(connectorId: ConnectorId) { connectors.remove(connectorId) }
}
```

---

## 10. Testing Pyramid

```
                   ┌──────────────┐
                  │  E2E / UI     │  Soft gate. Catch integration-level issues.
                 └────────────────┘
               ┌────────────────────┐
              │  Integration Tests   │  Soft gate. Real DBs in Docker.
             └──────────────────────┘
           ┌────────────────────────────┐
          │  Unit + Property Tests       │  Hard gate: 90% coverage,
          │  (domain + application)      │  <20% mutation survival.
          │  Mutation testing            │  Fast. No I/O. All fakes.
         └──────────────────────────────┘
```

Integration tests use Docker containers and GitHub Actions service containers. Never run against production.

---

## 11. Library Extraction Plan

### Extract after the second app

- `infrastructure/sync/` — `SyncEngine` interface + PowerSync adapters
- `infrastructure/connectors/google-sheets/` — complete, domain-agnostic connector
- `infrastructure/connectors/notion/` — same
- `domain.common` package — generic types
- `domain/src/commonTest/kotlin/fake/` — fake implementations

Extraction mechanism: GitHub Packages (free), Gradle composite builds for local development.

### Extract once stable

- `clients/shared-ui/designsystem/` — after stabilizing across two real apps
- `infrastructure/auth/` — Supabase and Firebase auth adapters
- `build-logic/` — convention plugins as a standalone Gradle plugin

### Never extract

`domain` and `application` are always app-specific.

---

## 12. The Meta/Admin App

Exercises every infrastructure layer with real code:
- **Local DB** — connector configs and feature flags in SQLDelight
- **Sync** — configs sync between devices via PowerSync
- **Auth** — admin login via Supabase Auth
- **Connectors** — the app manages connectors
- **Backend** — connector OAuth relay in Ktor
- **UI** — management screens on all four platforms

### Features

**Connector Management** — discover connectors, complete OAuth, configure field mappings, set sync schedule, test connection, view sync status.

**Feature Flags** — define flags, toggle per environment, override per user, view change history.

**Sync Status Dashboard** — view sync state per entity and connector, trigger manual sync, view and resolve conflicts, view replication health.

**App Settings** — auth backend toggle, environment selector, clear local DB, export local DB for debugging.

---

## 13. Bootstrap and Onboarding

### `scripts/setup.sh`

1. Check prerequisites: JDK 17+, Android SDK, Xcode (macOS), Docker
2. Create `.env` from `.env.example`, prompt for required values
3. Start Docker services: Postgres + backend + Nango
4. Run DB migrations
5. Run hard-gate test suite
6. Print summary and next steps

### Architecture Decision Records

| ADR | Decision |
|---|---|
| 001 | KMP over React Native |
| 002 | Ktor over Spring Boot |
| 003 | SQLDelight over Room |
| 004 | PowerSync for offline sync |
| 005 | Koin over Hilt |
| 006 | Ports in domain module |
| 007 | common package in domain |
| 008 | Nango for OAuth flows |
| 009 | Supabase primary, Firebase migration path |
| 010 | Schema authority: PowerSync runtime, SQLDelight compile-time validation only |
| 011 | Identity linking for multi-issuer auth migration |
| 012 | Bidirectional replication: dual change listeners with origin tagging for loop prevention |
| 013 | Navigation: JetBrains androidx navigation for Compose Multiplatform |

---

## 14. Phased Build Plan

**Phase 1 — Skeleton**
Monorepo structure, Gradle convention plugins, version catalog, all module directories. Hello World on all four platforms + Ktor response. Compose for Web Wasm performance spike. ArchUnit rules active.
*Deliverable:* `./gradlew build` succeeds. All four clients launch. Backend returns 200.

**Phase 2 — Domain and application layer**
Meta/admin domain entities, all port interfaces, fakes for all ports, property-based tests, Kover and Pitest configured.
*Deliverable:* Domain >90% coverage, <20% mutation survival. Zero infrastructure code.

**Phase 3 — Local persistence (Done)**
SQLDelight schema stubs, local repository adapters, integration tests against SQLite, DI wiring for local-only operation.
*Deliverable:* App runs on all platforms. Config persists across restarts. No network required.

**Phase 4 — Backend and central storage (Done)**
Supabase Postgres schema, remote repository adapters, Ktor routes, Supabase Auth.
*Deliverable:* Logged-in user configures connector. Config stored in Supabase. Retrieved on second device.

**Phase 5 — Sync (Current)**
`PowerSyncKotlinAdapter` and `PowerSyncWebAdapter`. Offline mode verified end-to-end. Sync status dashboard.
*Deliverable:* Full offline-first flow on Android and Desktop. Web syncs via JS adapter.

**Phase 6 — First connector**
Google Sheets connector with stable intermediate types. `NangoOAuthAdapter`. Field mapping UI. Two-way sync end-to-end.
*Deliverable:* Complete lifecycle: discover → authenticate → configure → sync → display → push change back.

**Phase 7 — Template hardening**
Write all `docs/` guides. Verify `scripts/setup.sh` on clean machine. Tag `v1.0-template`.
*Deliverable:* Repo ready to clone for the first real app.

---

## 15. Resolved Design Decisions

| Decision | Resolution |
|---|---|
| Module granularity | 6 top-level concerns. Sub-packages within `infrastructure` for logical separation without Gradle overhead. |
| `common` vs separate module | Merged into `domain` as `domain.common`. ArchUnit prevents cross-contamination. Extract when library extraction is concrete. |
| Where ports live | In `domain`. No separate ports module. |
| Connector interface design | Each connector defines its own stable intermediate record types. SDK types never cross the adapter boundary. Application layer maps connector types to domain types. |
| Offline/online-first strategy | Use-case-level decision via repository selection. Not a mode on `SyncEngine`. `SyncEngine` handles replication only. |
| Nango | `OAuthProvider` adapter. Replaceable via DI. Self-hostable for local dev. |
| Auth providers | Supabase primary. Firebase adapter present. `MigratingAuthAdapter` for simultaneous use. Internal `user_id` via `identity_link` table. |
| PowerSync + SQLDelight | PowerSync owns runtime schema. SQLDelight `.sq` files are compile-time validation stubs only. Authoritative schema in `infrastructure/sync/schema/`. |
| Bidirectional replication | Dual change listeners with origin tagging for loop prevention. Last-write-wins. Separate Docker container. |
| Backend structure | Feature-based (screaming architecture). Adding a feature touches one directory. |
| Navigation library | JetBrains `org.jetbrains.androidx.navigation:navigation-compose`. Not AndroidX. |
| Web sync adapter | PowerSync Web SDK via JS interop. Same `SyncEngine` port, platform-specific adapter. |
| ArchUnit enforcement | Build-time gate running over compiled bytecode in CI. Not a compiler check. |
| Domain dependencies | Kotlin + coroutines/Flow + kotlinx.datetime + kotlinx.serialization. No platform APIs, no vendor SDKs. |
| Compose Web | Start with Compose for Web Wasm. Spike in Phase 1. |
| iOS distribution | App Store. Apple compliance from Phase 1. |
| AI enforcement level | Hard gates on `domain` and `application`. Soft gates on `infrastructure` and `clients`. |
