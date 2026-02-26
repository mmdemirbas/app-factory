# ADR-013: JetBrains navigation library for Compose Multiplatform

**Status:** Accepted

## Context
Compose Multiplatform navigation requires a multiplatform-compatible navigation library.
AndroidX Navigation only works on Android.

## Decision
Use: org.jetbrains.androidx.navigation:navigation-compose
NOT: androidx.navigation:navigation-compose

## Consequences
- Type-safe, @Serializable route data classes work on all platforms.
- Dependency is in the JetBrains Compose dev Maven repository.
- Version must be kept aligned with the Compose Multiplatform version.
- Reference: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation.html
