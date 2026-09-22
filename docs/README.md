# Documentation index

Support Ticket Management System — Spec-Driven Development (SDD) record.

**Start here if you are reviewing the project.** Read in the order below. `PLAN.md` (repository root) is the phase status board.

This index is documentation only. It does not claim live browser, live UI↔backend, or PostgreSQL restart verification. Those remain **not yet performed**.

---

## Suggested reading order

| # | Document | Path |
|---|---|---|
| 1 | Requirements | [`spec/requirements.md`](../spec/requirements.md) |
| 2 | Specification | [`spec/specification.md`](../spec/specification.md) |
| 3 | Architecture | [`spec/architecture.md`](../spec/architecture.md) |
| 4 | Data model | [`spec/data-model.md`](../spec/data-model.md) |
| 5 | API contract | [`spec/api-contract.md`](../spec/api-contract.md) |
| 6 | State machine | [`spec/state-machine.md`](../spec/state-machine.md) |
| 7 | UI flow | [`spec/ui-flow.md`](../spec/ui-flow.md) |
| 8 | Test strategy | [`spec/test-strategy.md`](../spec/test-strategy.md) |
| 9 | Implementation plan | [`spec/implementation-plan.md`](../spec/implementation-plan.md) |
| 10 | Traceability | [`traceability.md`](traceability.md) |
| 11 | Acceptance report | [`acceptance-test-report.md`](acceptance-test-report.md) |
| 12 | Phase 18 review | [`phase-18-review.md`](phase-18-review.md) |
| 13 | Phase 19 fixes | [`phase-19-fixes.md`](phase-19-fixes.md) |
| 14 | AI validation | [`ai-validation.md`](ai-validation.md) |
| 15 | Prompt history | [`prompt-history.md`](prompt-history.md) |
| 16 | Final project status | [`final-project-status.md`](final-project-status.md) |

Also useful:

| Document | Path |
|---|---|
| Phase plan | [`../PLAN.md`](../PLAN.md) |
| Phase 20 close-out | [`phase-20-final-documentation.md`](phase-20-final-documentation.md) |
| Requirements review (Phase 2) | [`requirements-review.md`](requirements-review.md) |
| Token optimization | [`token-optimization.md`](token-optimization.md) |

---

## What each layer is

```text
Requirements
     ↓
Specification
     ↓
Architecture
     ↓
Data Model
     ↓
API Contract
     ↓
State Machine
     ↓
UI Flow
     ↓
Test Strategy
     ↓
Implementation Plan
     ↓
Implementation
     ↓
Testing
     ↓
Acceptance
     ↓
AI/Code/Spec Review
     ↓
Fixes
     ↓
Final Documentation
```

Do not treat automated test success as runtime UI or PostgreSQL restart proof.

---

## Status snapshot (Phase 20)

| Area | Status |
|---|---|
| Phases 1–20 SDD work | Complete as documentation/implementation phases described in `PLAN.md` |
| Automated backend tests | 149 passed (Phase 19 run) |
| Automated frontend tests | 13 passed (Phase 19 run) |
| Frontend build | Passed (Phase 19 run) |
| Live browser / E2E | **Not yet performed** |
| Live UI ↔ backend | **Not yet performed** |
| PostgreSQL runtime / restart | **Not yet performed** |
| Remaining OQs | OQ-001–OQ-015 except OQ-016; OQ-018; OQ-019 |
