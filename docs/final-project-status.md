# Final project status

**Product:** Support Ticket Management System  
**Date:** 2026-09-21  
**SDD phase:** 20 — Final documentation (this document)  
**Implementation:** Unchanged in Phase 20

This status is truthful to recorded evidence. Automated tests passed in Phase 19. Manual runtime, live UI↔backend, and PostgreSQL restart checks are **not yet performed**.

---

## 1. Project Overview

The Support Ticket Management System lets users create, list, view, and update support tickets, add comments, change ticket status along an approved lifecycle, and filter the list by status.

The backend is the authority for validation and lifecycle. The React UI is a client of the REST API. There is no authentication, no ticket deletion, and no comment edit/delete. Keyword **search matching is not implemented** because match semantics remain unresolved (OQ-005/OQ-006/OQ-007).

---

## 2. Technology Stack

Technologies actually present in this repository:

| Area | Technology | Evidence |
|---|---|---|
| Language | Java 21 (`pom.xml` `java.version`) | Human-approved Phase 11 |
| Application framework | Spring Boot 3.5.5 | Parent POM |
| Build (backend) | Maven | Existing project |
| HTTP | Spring Web (`spring-boot-starter-web`) | Phase 11+ |
| Persistence access | Spring Data JPA / Hibernate | Phase 16A decision, 16B implementation |
| Runtime/development database | PostgreSQL (env placeholders; process not started in SDD phases) | `application.properties` |
| Automated-test database | H2 in-memory, PostgreSQL compatibility mode | `application-test.properties` |
| Frontend | React 18.3.1 | `frontend/package.json` |
| Bundler | Vite 6.3.4 | same |
| Language (UI) | TypeScript 5.8.3 | same |
| UI tests | Vitest 3.2.4 + Testing Library + jsdom | same |

Not present as product features: Next.js, Flyway/Liquibase, authentication libraries, search engines.

---

## 3. Architecture

Implemented layered flow:

```text
React/Vite UI
      ↓
REST API
      ↓
Application Service
      ↓
Domain
      ↓
Persistence Port
      ↓
JPA/Hibernate
      ↓
PostgreSQL (runtime config) / H2 (automated tests)
```

| Layer | Responsibility | Must not |
|---|---|---|
| React/Vite UI | Views, forms, loading/empty/error display; calls contract paths | Be the only enforcer of lifecycle |
| REST API (`TicketController`, DTOs, `RestExceptionHandler`) | HTTP mapping, boundary validation, error envelope | Own T1–T5 |
| Application (`TicketService`) | Use cases, transactions, UUID assignment, clock for comments | Talk HTTP or embed JPA types |
| Domain (`Ticket`, `TicketLifecycle`, `Comment`, …) | Invariants, VAL-001/003/004/005, lifecycle T1–T5 | Depend on Spring Web or JPA |
| Persistence port (`TicketRepository`) | Load/save/query abstraction | Encode lifecycle |
| JPA adapter / entities | Map tickets and comments; `findByStatus` query; lazy comments on list | Leak into domain |

CORS is limited to local Vite origins. `Main.java` remains the original Hello World; Boot entry is `TicketManagementApplication`.

---

## 4. Core Business Rules

Source: `spec/specification.md`, `spec/state-machine.md`. Not a new product spec.

**Create.** Title is required and non-blank (VAL-001). Identity is a system-generated UUID (API string). Stored status of an accepted create is always `OPEN`.

**Statuses.** Exactly: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`. Unknown tokens are validation (VAL-005), not transitions.

**Valid transitions (T1–T5):**

```text
OPEN → IN_PROGRESS
OPEN → CANCELLED
IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED
RESOLVED → CLOSED
```

**Terminal:** `CLOSED` and `CANCELLED` have no outgoing approved transitions.

**Invalid pairs** are rejected as a business error (`ILLEGAL_TRANSITION` / HTTP 409). Stored status is unchanged.

**Comments.** Content and author required and non-blank (VAL-003/VAL-004). A comment belongs to one ticket. No edit/delete in the contract. Terminal comments are OQ-009 (fail-closed, not a product allow/deny).

**Status filter.** Exact match on one of the five tokens. Unknown filter token is 400, not an empty success list. Empty match is `[]`. Combined keyword + status is OQ-007 (not implemented).

**Priority set (VAL-002)** is OQ-001 and is not enforced.

---

## 5. API Summary

From `spec/api-contract.md` as implemented. No extra endpoints.

| Method | Path | Purpose | Success | Typical errors |
|---|---|---|---|---|
| `POST` | `/tickets` | Create ticket | `201` + `Location: /tickets/{id}`; body status `OPEN` | `400 VALIDATION` |
| `GET` | `/tickets` | List; optional `status` filter; `keyword` deferred | `200` array (possibly `[]`) | `400` unknown status; `500` if keyword present (OQ-005 fail-closed) |
| `GET` | `/tickets/{id}` | Details including comments | `200` | `404 NOT_FOUND` |
| `PATCH` | `/tickets/{id}` | Title, description, priority, assignee | `200` | `400`; `404` |
| `POST` | `/tickets/{id}/comments` | Add comment | `201` | `400`; `404` |
| `POST` | `/tickets/{id}/status` | Change status | `200` | `400` bad token; `404`; `409 ILLEGAL_TRANSITION` |

Error envelope: `status`, `errorCode`, `message`, optional `fields`. Codes: `VALIDATION`, `NOT_FOUND`, `ILLEGAL_TRANSITION`, `UNEXPECTED`. No stack traces in JSON. No `401`/`403` (no auth).

---

## 6. Frontend

Location: `frontend/` (React + Vite). HTTP client `createHttpTicketApi` uses `VITE_API_BASE_URL` (development file contains only `http://localhost:8080`).

| Surface | Behavior |
|---|---|
| List | Loading, empty, error; open ticket; create button |
| Create | Title required client-side; no status picker; `onCreated` after backend success → detail |
| Detail | Fields, comments, T1–T5 status buttons, terminal offers none |
| Search input | Present; blank search is not submitted; **matching is not implemented** (OQ-005) |
| Status filter | Five statuses or all |
| Errors | Validation, lifecycle (`409`), not-found (dedicated view, not a blank ticket) |
| Not-found | V-NOT-FOUND copy states the ticket does not exist |

UI unit tests use a `TicketApi` stub. That is **not** live browser verification.

---

## 7. Testing

Latest **executed** automated results are from **Phase 19** (2026-09-21T15:45:32+05:30 backend). Phase 20 did **not** re-run tests.

**Backend** (`mvn test`):

```text
Tests run: 149
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

**Frontend** (`cd frontend && npm test`):

```text
Tests  13 passed (13)
```

**Frontend build** (`npm run build`):

```text
tsc --noEmit && vite build
SUCCESS
```

These results prove automated domain, H2 API, persistence-query, and jsdom UI tests. They do **not** prove:

```text
Automated tests passed
        ≠
Manual runtime testing passed
```

---

## 8. AI-Assisted Development

Cursor/AI was used inside an SDD workflow, not as an unsupervised code generator.

| Stage | AI role | Human control |
|---|---|---|
| Requirements / spec | Draft REQ-001–REQ-050 and later spec files | OQs listed, not answered |
| Architecture through test strategy | Draft layer ownership, contract, T1–T5, UI flow | No schema/REST invented before those phases |
| Implementation | Java 21/Boot, domain, comments, UI, REST/JPA | Versions and DB/UUID decided by humans first |
| Tests | Domain, API, UI tests | Results only claimed after actual runs |
| Review | Phase 18 findings without code changes | Defects recorded, not silently “fixed in review” |
| Fixes | Phase 19 technical corrections | No OQ resolution |
| Documentation | This phase | No production changes |

Prompts are in `docs/prompt-history.md`. Corrections are in `docs/ai-validation.md`.

---

## 9. Human Validation / AI Corrections

Factual examples (see `docs/ai-validation.md`):

1. Phase 1 prompt assumed Spring Boot was already in the repo; inspection showed Java 17 Hello World.
2. Java 21 and Spring Boot 3.5.5 were not invented in `pom.xml` until a human approved them (Phase 11).
3. Phase 16 stopped until OQ-016, JPA/Hibernate, and UUID identity were recorded (16A), then implemented (16B).
4. Keyword search matching was never invented (OQ-005).
5. Phase 14 prompt mislabeled T2/T5; tests kept `spec/state-machine.md` IDs.
6. Passing tests were not treated as PostgreSQL restart or browser proof (Phases 17–18).
7. Phase 19 corrected in-memory filter, list comment loading, accidental `VARCHAR(255)`, and fragile 404 mapping.
8. Comment edit/delete were out of scope (`spec/api-contract.md`, requirements review); they were not added as extra product features.

---

## 10. Open Questions

**Still unresolved** (do not treat as decided):

```text
OQ-001  Priority value set
OQ-002  Required fields on create other than title
OQ-003  Unassigned tickets
OQ-004  Blank description
OQ-005  Keyword search match fields and rule
OQ-006  Blank keyword
OQ-007  Combining search and status filter
OQ-008  Field updates on terminal tickets
OQ-009  Comments on terminal tickets
OQ-010  Comment author without authentication
OQ-011  Comment order
OQ-012  Same-status request
OQ-013  Client-supplied status on create (ignore vs reject)
OQ-014  List contents, sort, pagination
OQ-015  Field length limits
OQ-018  React vs Next.js vs equivalent (React+Vite used; family not closed as a product-wide Next.js decision)
OQ-019  Concurrent updates
```

**Already decided (do not reopen):**

| Item | Decision |
|---|---|
| Java 21 | Phase 11 human approval |
| Spring Boot 3.5.5 | Phase 11 |
| PostgreSQL development/runtime | OQ-016, Phase 16A |
| H2 automated tests | OQ-016, Phase 16A |
| Spring Data JPA / Hibernate | Recorded Phase 16A (not OQ-017) |
| System-generated UUID ticket ids (API string) | Phase 16A |

OQ-017 (Java 21 timing) is historically the upgrade question; Java 21 is approved. Do not relabel JPA as OQ-017.

Deferred operations currently fail-closed as `500 UNEXPECTED` (search, blank keyword, combined filter, same-status, terminal comments). That mapping is **not** a fifth documented product error class; changing it needs a human API decision.

---

## 11. Known Gaps

- Search matching not implemented (OQ-005/006/007)
- Browser/E2E not run
- Live UI ↔ backend verification not performed
- PostgreSQL runtime not started in SDD phases; restart/persistence-after-restart not verified
- VAL-002 / OQ-001 unresolved
- OQ-008/009/012/013 product outcomes unresolved
- OQ-019 concurrency unresolved (no `@Version`; green tests are not a concurrency proof)
- No Git repository in this workspace (no remote history / secret scan of commits)
- Complete manual end-to-end journeys not executed

```text
Application implemented
        ≠
Application fully runtime-validated
```

---

## 12. Tomorrow's Runtime Validation

**Planned. Not executed in Phase 20.**

```text
Start PostgreSQL
      ↓
Start Spring Boot backend
      ↓
Start React/Vite frontend
      ↓
Open browser
      ↓
Create ticket
      ↓
List/detail/update
      ↓
Comments
      ↓
Status transitions
      ↓
Error scenarios
      ↓
Filtering
      ↓
Restart backend
      ↓
Verify persistence
```

These are planned runtime checks, not completed tests.
