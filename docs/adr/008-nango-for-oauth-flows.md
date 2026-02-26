# ADR-008: Nango for OAuth flow management

**Status:** Accepted

## Context
Each external connector needs OAuth. Building and maintaining OAuth flows for 
each provider is significant effort.

## Decision
Use Nango behind the `OAuthProvider` port interface.
NangoOAuthAdapter implements OAuthProvider.

## Consequences
- Nango handles OAuth for 200+ SaaS tools.
- Self-hostable via Docker for local development.
- Replacing Nango = new OAuthProvider implementation + one DI binding change.
- No connector adapter, no use case, no domain class imports Nango directly.
