# App Factory

A Kotlin Multiplatform monorepo that is simultaneously a running meta/admin application
and a reusable template for future applications.

**Targets:** Android · iOS · Desktop (JVM) · Web (Wasm) · Ktor backend

---

## Getting started

**→ [docs/setup.md](docs/setup.md)** — complete setup guide for a fresh clone

Short version:

```bash
# Prerequisites: JDK 21, Gradle 8.11+, Docker
# See docs/setup.md for installation instructions

rm -rf .gradle build build-logic/build          # clear any stale state
gradle wrapper --gradle-version 8.11.1 --distribution-type bin
cp .env.example .env                            # fill in your credentials
docker compose -f scripts/deploy/docker-compose.yml up -d
./gradlew :domain:jvmTest :application:jvmTest  # hard-gate tests
./gradlew build                                 # full build
```

---

## Documentation

| Document | Purpose |
|---|---|
| [docs/setup.md](docs/setup.md) | Prerequisites, first-time setup, troubleshooting |
| [docs/architecture.md](docs/architecture.md) | Full system design and decisions |
| [docs/ai-workflow.md](docs/ai-workflow.md) | Rules for AI agents working in this repo |
| [docs/connector-guide.md](docs/connector-guide.md) | How to add a new external connector |
| [docs/new-app-guide.md](docs/new-app-guide.md) | How to start a new app from this template |
| [docs/adr/](docs/adr/) | Architecture Decision Records |

---

## Module structure

```
domain/          Pure Kotlin — entities, ports, business rules. No external deps.
application/     Use cases. Depends on domain only.
infrastructure/  Adapter implementations (Supabase, SQLDelight, PowerSync, etc.)
clients/
  shared-ui/     Compose Multiplatform screens and design system
  android/       Android entry point
  ios/           iOS entry point (Xcode project — see docs/setup.md)
  desktop/       Desktop JVM entry point
  web/           Compose for Web (Wasm) entry point
backend/         Ktor server, organised by feature
build-logic/     Gradle convention plugins
```

## Key rules

- `domain/` has zero imports from infrastructure, clients, or backend — enforced by ArchUnit
- `application/` imports `domain/` only — enforced by ArchUnit
- Use cases live in `application/`, never in route handlers
- Every port interface has a `Fake*` implementation in `commonTest`

These are hard gates — violations fail the CI build.
