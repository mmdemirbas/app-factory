# ADR-002: Ktor over Spring Boot

**Status:** Accepted

## Context
Backend needs a Kotlin-native HTTP server.

## Decision
Use Ktor.

## Consequences
- Explicit, readable wiring. No annotation magic.
- Lightweight. No framework-level opinions about architecture.
- Application structure is owned by this codebase, not by Spring.
- Trade-off: more boilerplate for features Spring provides automatically (security, etc.).
