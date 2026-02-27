## Feature: Test connector reachability

### Inputs
- ConnectorId of an already-configured connector

### Behavior
- Looks up the connector in ConnectorRegistry.configured()
- Calls ConnectorRegistry.test() with the connectorId
- Returns TestResult with success flag, message, and optional latency

### Acceptance criteria
- [ ] Known configured connector returns TestResult
- [ ] Unknown connectorId returns DomainError.NotFound before calling test()
- [ ] Registry.test() is called exactly once
- [ ] TestResult.success=false is returned as DomainResult.Success (not a failure — a failed test is a valid result)

### Out of scope
- Timeout configuration
- Retry on failure
