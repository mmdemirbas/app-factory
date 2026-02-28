# App Factory — Claude Code Instructions

## What this project is
A Kotlin Multiplatform monorepo that is both a running meta/admin app and a 
template for future apps. Read docs/architecture.md for the full design.

## Before doing anything
1. Read docs/architecture.md (full design and all decisions)
2. Read docs/ai-workflow.md (rules you must follow)
3. Read docs/adr/ for the relevant ADR before touching any architectural concern

## Current phase
Phase 2 complete. Starting Phase 3: local persistence.
See docs/architecture.md Section 14 for the phase plan.

## Module rules (hard gates — violations fail CI)
- domain/ → zero imports from infrastructure, application, clients, backend
- application/ → imports domain only
- infrastructure/ → imports domain and application
- Never put business logic in a Ktor route handler
- Never use platform-specific APIs in commonMain source sets
- Never use kotlin.Result in domain code — use DomainResult from domain.common

## Build commands
./gradlew :domain:jvmTest          # Run hard-gate tests (ArchUnit + domain tests)
./gradlew :application:jvmTest     # Run application architecture tests
./gradlew build                    # Full build all modules
./gradlew :clients:desktop:run     # Run desktop client
./gradlew :backend:run             # Run backend (port 8080)

## Tech stack at a glance
- KMP + Compose Multiplatform (Android, iOS, Desktop, Web/Wasm)
- Ktor backend (not Spring)
- SQLDelight for local DB (compile-time query validation)
- PowerSync for offline sync (Phase 5)
- Koin for DI
- Kotest for tests (property-based preferred for domain rules)
- ArchUnit for dependency direction enforcement

## When adding anything new
1. Write a .spec.md file first (see docs/ai-workflow.md for format)
2. Write failing tests before implementation
3. Add Fake* in domain/src/commonTest/kotlin/.../fake/ for every new port
4. Run ./gradlew :domain:jvmTest before declaring done
