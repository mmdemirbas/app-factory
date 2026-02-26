# Current work: Phase 2 — Domain and Application Layer

## Goal
Implement the full meta/admin domain with all port interfaces, fakes, 
property-based tests, and use cases. Zero infrastructure code yet.

## Definition of done for Phase 2
- [ ] All port interfaces defined in domain/port/
- [ ] Fake* implementation for every port in domain/src/commonTest/.../fake/
- [ ] Property-based tests covering all domain business rules
- [ ] ./gradlew :domain:jvmTest passes with >90% coverage
- [ ] ./gradlew :application:jvmTest passes
- [ ] Zero infrastructure imports in domain or application

## What's already done
- EntityId, Timestamp, DomainResult in domain.common
- SyncEngine, AuthProvider, ConnectorRegistry, OAuthProvider, ReplicaSink ports
- FeatureFlag entity with validation
- FeatureFlagRepository port
- FakeConnectorRegistry, FakeFeatureFlagRepository
- ConfigureConnectorUseCase, CreateFeatureFlagUseCase, ToggleFeatureFlagUseCase
- ArchUnit tests for domain and application
