# ADR-001: Kotlin Multiplatform over React Native

**Status:** Accepted

## Context
Targeting Android, iOS, Desktop (JVM), and Web with a Kotlin backend.

## Decision
Use Kotlin Multiplatform with Compose Multiplatform.

## Consequences
- Domain, application logic, and port interfaces are written once and used by all targets including the backend.
- Compiler enforces cross-platform consistency (a change to a domain model fails compilation everywhere simultaneously).
- Trade-off: tooling less mature than React Native; iOS support still maturing.

## Alternatives considered
- React Native: no Desktop JVM target without extra work; cross-language boundary with Kotlin backend.
- Flutter/Dart: all four platforms, but Dart ≠ Kotlin; no code sharing with Kotlin backend.
