# Phase 19 — Fixes

**Product:** Support Ticket Management System  
**Date:** 2026-09-21  
**Phase:** 19 — Technical fixes only  
**Phase 20:** Not started

Approved decisions unchanged: Java 21, Spring Boot 3.5.5, PostgreSQL runtime / H2 tests, Spring Data JPA, UUID ticket ids, `Main.java` unchanged.

---

## Fix Summary

| Finding | Fix | Evidence | Status |
| ------- | --- | -------- | ------ |
| Persistence status filtering ran in memory after `findAll()` | `TicketJpaRepository.findByStatus(String)` query; `TicketService.list` uses `tickets.findByStatus` after VAL-005 `fromToken`. Combined keyword+status still OQ-007 fail-closed. | `TicketJpaRepository`; `TicketRepositoryAdapter.findByStatus`; `TicketService.list`; `TicketPersistenceQueryTest.findByStatus_queriesMatchingRowsOnly`; existing `filter_*` API tests | Done |
| List loaded comments / N+1 risk | List/filter mapping uses `toListItem` (does not touch comments). Detail uses `findWithCommentsById` + entity graph. Relationship stays LAZY (not EAGER). | `TicketMapper.toListItem`; `TicketJpaRepository.findWithCommentsById`; `TicketPersistenceQueryTest.findAll_doesNotInitializeComments`; `list_doesNotExposeComments_detailDoes` | Done |
| Undocumented VARCHAR(255) | `columnDefinition = "text"` on title, priority, assignee, comment author (description/content already text). Status remains a short token column. No `@Size`. OQ-015 not decided. | `TicketEntity`; `CommentEntity`; `create_titleLongerThan255_isPersisted` | Done |
| Fragile 404 via `IllegalArgumentException` message prefix | Removed string-match handler. Unknown ids throw `TicketNotFoundException` only (`requireTicket` blank/missing → not-found; invalid UUID → empty optional). | `RestExceptionHandler`; `TicketService.requireTicket`; API 404 tests | Done |
| Missing PATCH/status 404 tests | Added. | `update_unknownTicket_returnsNotFound`; `changeStatus_unknownTicket_returnsNotFound` | Done |
| Blank comment author API test | Missing, blank, and whitespace-only author → 400 `VALIDATION` field `author`. | `comment_rejectsMissingAuthor`; `comment_rejectsBlankAuthor`; `comment_rejectsWhitespaceOnlyAuthor` | Done |
| Create-status regression (REQ-013, not OQ-013) | Create with `"status":"CLOSED"` must not store CLOSED. Current run returned 201 with stored `OPEN`. Ignore-vs-reject remains unresolved. | `create_clientSuppliedStatus_doesNotStoreNonOpenStatus` | Done |
| Successful create navigation | UI unit tests: `onCreated` only after backend resolve; `App` shows detail only after create succeeds. Optional `ticketApi` prop for the test (production still uses `createHttpTicketApi`). | `ui.test.tsx` | Done |
| Deferred OQs as 500 UNEXPECTED | Inspected; left unchanged (would invent an API class). | `RestExceptionHandler.deferred`; `search_isNotImplemented` | Unchanged (OQ/API) |

---

## Tests

### Backend

Command: IntelliJ Maven 3 + JBR 21.0.8 — `mvn test`

```text
Tests run: 149, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Finished at: 2026-09-21T15:45:32+05:30
```

Breakdown: API integration 53; CommentTest 17; TicketKeywordSearchTest 2; TicketStatusFilterTest 22; TicketStatusTransitionTest 30; TicketTest 20; TicketPersistenceQueryTest 5.

### Frontend tests

Command: `cd frontend && npm test` (`vitest run`)

```text
Test Files  1 passed (1)
Tests  13 passed (13)
```

### Frontend build

Command: `cd frontend && npm run build`

```text
tsc --noEmit && vite build
✓ built in 428ms
```

---

## Regression Results

### Domain

T1–T5, 15 illegal pairs, terminal states, VAL-001/003/004/005, initial OPEN: existing domain suites still pass (91 tests).

### API

Create, list, detail, PATCH, comments, status change, status filter (five statuses, unknown token, empty), 400/404/409/500: `TicketApiIntegrationTest` 53 passed. Search still fail-closed 500 (OQ-005).

### Frontend

List, create validation, detail, 409, 404, status buttons, create→detail after backend success: 13 passed. Live browser/E2E not run.

---

## OQ Preservation

Unresolved (not answered in this phase):

| ID | Status |
|---|---|
| OQ-001 priority set | Unresolved |
| OQ-002 create required besides title | Unresolved |
| OQ-003 unassigned / empty assignee | Unresolved |
| OQ-004 blank description | Unresolved |
| OQ-005 search semantics | Unresolved — no matcher |
| OQ-006 blank search | Unresolved — fail-closed 500 |
| OQ-007 search + filter | Unresolved — fail-closed 500 |
| OQ-008 terminal PATCH | Unresolved — no extra 409 added |
| OQ-009 terminal comments | Unresolved — fail-closed 500 |
| OQ-010 comment author source | Unresolved — request JSON |
| OQ-011 comment order | Unresolved |
| OQ-012 same-status | Unresolved — fail-closed 500 |
| OQ-013 create-body status ignore vs reject | Unresolved — test only proves stored create stays OPEN |
| OQ-014 pagination/order | Unresolved |
| OQ-015 field lengths | Unresolved — 255 default removed; no VAL max added |
| OQ-017 Java 21 timing | Not reopened |
| OQ-018 frontend family | Unresolved |
| OQ-019 concurrency | Unresolved — no `@Version` |

Resolved items not reopened: OQ-016 PostgreSQL/H2, JPA/Hibernate, UUID ids, Java 21 / Boot 3.5.5.

`500 UNEXPECTED` for deferred OQs was inspected and **left unchanged** so Phase 19 does not invent an error class. Carry to Phase 20 / human API decision.

---

## Remaining Gaps

- Browser/E2E against a live API
- PostgreSQL process restart (H2 HTTP + query tests only)
- Keyword search matching (OQ-005/006/007)
- VAL-002 / OQ-001
- Terminal PATCH/comments and same-status product rules (OQ-008/009/012)
- Create-body `status` ignore vs reject (OQ-013)
- Concurrency (OQ-019)
- Workspace is not a git repository

---

## Production files changed

- `src/main/java/com/enterprise/ai/persistence/TicketJpaRepository.java`
- `src/main/java/com/enterprise/ai/persistence/TicketMapper.java`
- `src/main/java/com/enterprise/ai/persistence/TicketRepositoryAdapter.java`
- `src/main/java/com/enterprise/ai/persistence/TicketEntity.java`
- `src/main/java/com/enterprise/ai/persistence/CommentEntity.java`
- `src/main/java/com/enterprise/ai/application/TicketService.java`
- `src/main/java/com/enterprise/ai/api/RestExceptionHandler.java`
- `frontend/src/App.tsx` (optional `ticketApi` for unit tests)

`Main.java`, `pom.xml`, and `spec/` were not modified.

## Tests added/updated

- `src/test/java/com/enterprise/ai/api/TicketApiIntegrationTest.java` (404s, author, create status, long title, filter non-mutation, list vs detail comments)
- `src/test/java/com/enterprise/ai/persistence/TicketPersistenceQueryTest.java` (new)
- `frontend/src/views/ui.test.tsx` (create success + App navigation)

---

## Diff review (no git repository)

Inspected the changed files. No secrets, no new dependencies, no auth, no search matcher, no OQ answers encoded as product rules, no Phase 20 README rewrite.
