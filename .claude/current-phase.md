# Current work: Phase 6 — First connector

## Goal
Implement the first full connector (Google Sheets) with stable intermediate types, field mapping UI, and two-way sync end-to-end.

## Definition of done for Phase 6
- [ ] Google Sheets connector implemented with stable intermediate types (`infrastructure/connectors/google-sheets/`).
- [ ] `NangoOAuthAdapter` implemented and configured for Google Sheets OAuth flow.
- [ ] Field mapping UI created to map Google Sheets columns to domain entities.
- [ ] Complete lifecycle verified: discover → authenticate → configure → sync → display → push change back.
- [ ] Two-way sync end-to-end validated.

## Persistent Progress Tracking
Previous phase completions and walkthroughs are stored persistently in the repository at:
- `docs/phases/phase-5.md`
- `docs/phases/phase-5-walkthrough.md`
