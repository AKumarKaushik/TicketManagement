# Traceability

Map work from requirement through review. Do not mark a row complete until each step exists.

```text
Requirement
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

Reviewer index: `docs/README.md`. Final status: `docs/final-project-status.md`.

Validation statuses used in the Phase 20 matrix:

- **PASS** — decided behavior has automated and/or acceptance evidence
- **BLOCKED** — cannot conclude because an Open Question is unresolved
- **NOT YET RUNTIME-VERIFIED** — implementation exists but live browser / PostgreSQL restart / live UI↔backend was not executed

Do not convert BLOCKED items to PASS. Do not treat automated H2 tests as PostgreSQL restart proof.

## How to use this file

For each requirement ID (assigned in Phase 2):

| Requirement | Specification | Task | Implementation | Test | Review |
|---|---|---|---|---|---|
| ID and title from `spec/requirements.md` | Spec section(s) | `PLAN.md` phase / task | Source files | Test class/method | Review command / result |

Rules:

- A requirement without a specification is not ready to implement.
- A specification without a task is not scheduled.
- An implementation without a test is incomplete.
- A test without a review is unverified.
- Never claim a test passed unless it was run.
- Placeholder specs from Phase 1 are not implementable requirements.
- Phase 2 requirements in `spec/requirements.md` are the behavior source.
- Phase 3 system behavior is specified in `spec/specification.md`.
- Phase 4 target structure is specified in `spec/architecture.md` (layers, boundaries, dependency direction).
- Phase 5 logical data is specified in `spec/data-model.md`.
- Phase 6 HTTP contract is specified in `spec/api-contract.md`.
- Phase 7 ticket lifecycle is specified in `spec/state-machine.md`.
- Phase 8 user journeys are specified in `spec/ui-flow.md`.
- Phase 9 verification approach is specified in `spec/test-strategy.md`.
- Phase 10 work sequence is specified in `spec/implementation-plan.md`.
- Phase 11 Backend Foundation is **complete** (Java 21, Spring Boot 3.5.5, Boot entry, no DB).
- Phase 12 Domain model and business rules is **complete** (Ticket aggregate, T1–T5, VAL-001/VAL-005).
- Phase 13 Comments/search/filter is **complete** for comments and status filter. Keyword **matching** is deferred (OQ-005).
- Phase 14 Backend testing is **complete** for the domain suite. API/persistence tests were added in Phase 16.
- Phase 15 Frontend is **complete** (React+Vite UI and HTTP client). Live browser/E2E against PostgreSQL is not claimed.
- Phase 16 Integration is **complete** (REST, application, JPA, H2 tests, PostgreSQL runtime config, `VITE_API_BASE_URL`).
- Phase 17 Acceptance testing is **complete** as an activity. Report: `docs/acceptance-test-report.md`. Production code was not changed. Browser/E2E and PostgreSQL restart were not executed.
- Phase 18 AI/code/specification review is **complete**. Report: `docs/phase-18-review.md`. Production code was not changed. Specs were not altered to hide findings.
- Phase 19 Fixes is **complete**. Report: `docs/phase-19-fixes.md`. Technical gaps from Phase 18 were addressed without resolving OQs.
- Phase 20 Final documentation is **complete**. Reports: `docs/final-project-status.md`, `docs/phase-20-final-documentation.md`. Implementation was not changed.

## Phase 20 validation status

Statuses: **PASS** (decided behavior evidenced by automated/acceptance tests), **BLOCKED** (unresolved OQ prevents a conclusive AC), **NOT YET RUNTIME-VERIFIED** (needs live browser and/or PostgreSQL restart). A row may combine statuses.

| REQ | Validation status | Notes |
|---|---|---|
| REQ-001 | PASS | Create OPEN + UUID; live UI create **NOT YET RUNTIME-VERIFIED** |
| REQ-002 | PASS | List empty/non-empty (H2, UI stub); live list **NOT YET RUNTIME-VERIFIED** |
| REQ-003 | PASS | Details + comments (H2, UI stub); live detail **NOT YET RUNTIME-VERIFIED** |
| REQ-004 | PASS | Title PATCH; terminal PATCH **BLOCKED** (OQ-008) |
| REQ-005 | PASS | Description PATCH; blank policy **BLOCKED** (OQ-004) |
| REQ-006 | PASS | Priority stored as text; VAL-002 **BLOCKED** (OQ-001) |
| REQ-007 | PASS | Assignee PATCH; unassign **BLOCKED** (OQ-003) |
| REQ-008 | PASS | Add comment; terminal comments **BLOCKED** (OQ-009) |
| REQ-009 | BLOCKED | Search matching OQ-005/006/007; fail-closed 500 is not matching |
| REQ-010 | PASS | Filter via persistence query (Phase 19); combo with search **BLOCKED** (OQ-007) |
| REQ-011 | PASS | `POST /tickets/{id}/status` |
| REQ-012 | PASS | System UUID string |
| REQ-013 | PASS | Stored create always OPEN; ignore-vs-reject **BLOCKED** (OQ-013) |
| REQ-014 | PASS | Five tokens |
| REQ-015 | PASS | T1–T5 |
| REQ-016 | PASS | Illegal pair 409 + unchanged store |
| REQ-017 | PASS | Terminal have no T-outgoing; OQ-008/009/012 still **BLOCKED** as extra rules |
| REQ-018 | PASS | Comment belongs to one ticket |
| REQ-019 | PASS | Backend 409 without UI |
| REQ-020 | PASS | Backend validation; lengths **BLOCKED** (OQ-015) |
| REQ-021 | PASS | VAL-001 |
| REQ-022 | BLOCKED | VAL-002 / OQ-001 |
| REQ-023 | PASS | VAL-005 |
| REQ-024 | PASS | VAL-003/004 |
| REQ-025 | PASS | 404 GET/PATCH/comment/status |
| REQ-026 | PASS | Rejected writes not stored as requested |
| REQ-027 | PASS | VALIDATION envelope |
| REQ-028 | PASS | 409 ILLEGAL_TRANSITION |
| REQ-029 | PASS | 404 vs blank ticket (unit); live **NOT YET RUNTIME-VERIFIED** |
| REQ-030 | PASS | 500 body has no stack |
| REQ-031 | PASS (unit) | Live UI errors **NOT YET RUNTIME-VERIFIED** |
| REQ-032 | PASS (unit) | Live success-after-2xx **NOT YET RUNTIME-VERIFIED** |
| REQ-033 | PASS (H2 HTTP) | PostgreSQL restart **NOT YET RUNTIME-VERIFIED** |
| REQ-034 | PASS (H2 HTTP) | Process restart **NOT YET RUNTIME-VERIFIED** |
| REQ-035 | PASS (H2 HTTP) | Comment restart **NOT YET RUNTIME-VERIFIED** |
| REQ-036 | PASS (H2 HTTP) | Update restart **NOT YET RUNTIME-VERIFIED** |
| REQ-037 | PASS | Rejected transition not persisted (same process) |
| REQ-038 | PASS | Java 21 |
| REQ-039 | PASS | Spring Boot 3.5.5 |
| REQ-040 | PASS | REST contract implemented |
| REQ-041 | PASS (H2 tests) | PostgreSQL runtime **NOT YET RUNTIME-VERIFIED** |
| REQ-042 | PASS (SPA + unit) | Live browser **NOT YET RUNTIME-VERIFIED**; search **BLOCKED** |
| REQ-043 | PASS | Layers; no extra product features |
| REQ-044 | PASS (automated) | Restart AC **NOT YET RUNTIME-VERIFIED** |
| REQ-045 | PASS | No invented auth; no committed credentials |
| REQ-046 | PASS | VAL-001/003/004/005; VAL-002 **BLOCKED** |
| REQ-047 | PASS | Four error classes |
| REQ-048 | NOT YET RUNTIME-VERIFIED | Durable restart vs PostgreSQL not executed |
| REQ-049 | PASS | Stable JSON names |
| REQ-050 | PASS | Env placeholders; workspace is not a git repo |

## Current matrix

Tasks, implementation, tests, and review for implemented phases are listed below. Unresolved OQs are not marked covered.

| Requirement | Specification | Task | Implementation | Test | Review |
|---|---|---|---|---|---|
| REQ-001 Create a ticket | `spec/requirements.md`; `spec/specification.md` §3.1 | Phase 12 domain; Phase 16 REST | `Ticket.create`; `TicketService.create`; `POST /tickets` | `TicketTest.create_*` (ran); `TicketApiIntegrationTest.create_assignsUuidAndOpenStatus` (ran) | Status always `OPEN`; OQ-013 not closed |
| REQ-002 List tickets | `spec/requirements.md`; `spec/specification.md` §3.2, §10 | Phase 16 REST; V-LIST | `GET /tickets`; `TicketListView` | `TicketApiIntegrationTest.list_*` (ran); `ui.test.tsx` V-LIST (ran, stub) | No pagination (OQ-014) |
| REQ-003 View ticket details | `spec/requirements.md`; `spec/specification.md` §3.3 | Phase 16 REST; V-DETAIL | `GET /tickets/{id}`; `TicketDetailView` | `TicketApiIntegrationTest.list_andDetail_*` (ran); `ui.test.tsx` V-DETAIL (ran, stub) | Unknown id → 404 |
| REQ-004 Update ticket title | `spec/requirements.md`; `spec/specification.md` §3.4 | Phase 12 domain; Phase 16 PATCH | `Ticket.changeTitle`; `PATCH /tickets/{id}` | `TicketTest.changeTitle_*` (ran); `TicketApiIntegrationTest.update_replacesSuppliedFieldsOnly` (ran) | OQ-008 terminal PATCH not tested |
| REQ-005 Update ticket description | `spec/requirements.md`; `spec/specification.md` §3.5 | Phase 16 PATCH | `Ticket.changeDescription`; `PATCH /tickets/{id}` | `TicketApiIntegrationTest.update_replacesSuppliedFieldsOnly` (ran) | OQ-004 blank policy unanswered |
| REQ-006 Update ticket priority | `spec/requirements.md`; `spec/specification.md` §3.6 | Phase 16 PATCH | `Ticket.changePriority`; `PATCH /tickets/{id}` | `TicketApiIntegrationTest.update_replacesSuppliedFieldsOnly` (ran) | VAL-002 / OQ-001 unanswered |
| REQ-007 Update ticket assignee | `spec/requirements.md`; `spec/specification.md` §3.7 | Phase 16 PATCH | `Ticket.changeAssignee`; `PATCH /tickets/{id}` | `TicketApiIntegrationTest.update_replacesSuppliedFieldsOnly` (ran) | OQ-003 unanswered |
| REQ-008 Add comments | `spec/requirements.md`; `spec/specification.md` §4 | Phase 13 domain; Phase 16 REST | `Ticket.addComment`; `POST /tickets/{id}/comments` | `CommentTest` (ran); `TicketApiIntegrationTest.comment_*` (ran) | OQ-009/011 deferred; author is request-supplied (OQ-010) |
| REQ-009 Search by keyword | `spec/requirements.md`; `spec/specification.md` §5.1 | Phase 13 deferred boundary; Phase 16 fail-closed | `TicketKeywordSearch` (no matcher); `GET /tickets?keyword=` deferred | `TicketKeywordSearchTest` (ran); `TicketApiIntegrationTest.search_isNotImplemented` (ran; 500 UNEXPECTED) | OQ-005/006/007 unanswered |
| REQ-010 Filter by status | `spec/requirements.md`; `spec/specification.md` §5.2 | Phase 13 domain; Phase 16 REST; Phase 19 persistence query | `TicketStatusFilter`; `GET /tickets?status=`; `TicketJpaRepository.findByStatus` | `TicketStatusFilterTest` (ran); `TicketApiIntegrationTest.filter_*` (ran); `TicketPersistenceQueryTest.findByStatus_*` (ran) | No OQ-007 combo |
| REQ-011 Change ticket status | `spec/requirements.md`; `spec/specification.md` §3.8 | Phase 12 domain; Phase 16 REST | `Ticket.changeStatus`; `POST /tickets/{id}/status` | Domain T1–T5 (ran); `TicketApiIntegrationTest.validTransitions_*` (ran) | No second transition table |
| REQ-012 Unique ticket identity | `spec/requirements.md`; `spec/specification.md` §2.1, §3.1 | Phase 12; Phase 16 UUID | `TicketId`; `UUID.randomUUID` on create; API string | `TicketTest.ticketId_rejectsBlankValue` (ran); `create_assignsUuidAndOpenStatus` (ran) | Client-supplied `id` → 400 |
| REQ-013 Initial status OPEN | `spec/requirements.md`; `spec/specification.md` §6.2 | Phase 12; Phase 16 create | `Ticket.create` always `OPEN` | Domain create tests (ran); API create (ran) | Client cannot set stored create status |
| REQ-014 Status values | `spec/requirements.md`; `spec/specification.md` §6.1 | Phase 12; Phase 16 API | `TicketStatus` five values | Domain + API token tests (ran) | Exact tokens |
| REQ-015 Allowed transitions | `spec/requirements.md`; `spec/specification.md` §6.3 | Phase 12; Phase 16 | `TicketLifecycle` T1–T5 | Domain t1–t5 (ran); API validTransitions (ran) | SM T1–T5 |
| REQ-016 Invalid transitions rejected | `spec/requirements.md`; `spec/specification.md` §6.4 | Phase 12; Phase 16 | `IllegalStatusTransitionException` | Domain 15 pairs (ran); API `invalidTransition_returns409AndLeavesStatus` (ran) | 409; store unchanged |
| REQ-017 Terminal statuses | `spec/requirements.md`; `spec/specification.md` §6.5 | Phase 12; Phase 16 | `TicketStatus.isTerminal`; illegal outgoing pairs | Domain terminal tests (ran); API invalid pairs from CLOSED/CANCELLED (ran) | OQ-008/009/012 not decided |
| REQ-018 Comment association | `spec/requirements.md`; `spec/specification.md` §2.2, §4 | Phase 13; Phase 16 JPA | `Comment.ticketId`; `comments.ticket_id` FK | `CommentTest.commentBelongsOnlyToItsTicket` (ran); API comment then GET details (ran) | Technical comment UUID not on wire |
| REQ-019 Backend enforces state machine | `spec/requirements.md`; `spec/specification.md` §6.6 | Phase 12; Phase 16 REST | `TicketLifecycle` via `TicketService.changeStatus` | API 409 tests (ran) | Controller has no transition table |
| REQ-020 Backend input validation | `spec/requirements.md`; `spec/specification.md` §7 | Phase 16 | Bean Validation + domain VAL-* | API create/comment/status validation (ran) | Length OQ-015 unanswered |
| REQ-021 Title validation | `spec/requirements.md`; `spec/specification.md` §7.2 VAL-001 | Phase 12; Phase 16 | `Ticket.requireTitle`; `@NotBlank` title | Domain title tests (ran); `create_rejectsBlankTitle` (ran) | OQ-015 deferred |
| REQ-022 Priority validation | `spec/requirements.md`; `spec/specification.md` §7.2 VAL-002 | Not scheduled | None (no recognized set) | None | OQ-001 deferred |
| REQ-023 Status value validation | `spec/requirements.md`; `spec/specification.md` §7.2 VAL-005 | Phase 12–13; Phase 16 | `TicketStatus.fromToken` | Domain + `changeStatus_unknownToken_*`; `filter_unknownStatus_*` (ran) | Wrong-case/blank → 400 not empty list |
| REQ-024 Comment field validation | `spec/requirements.md`; `spec/specification.md` §4, §7.2 | Phase 13; Phase 16 | VAL-003 / VAL-004 | Domain CommentTest (ran); `comment_rejectsBlankContent` (ran) | OQ-015 deferred |
| REQ-025 Unknown ticket not found | `spec/requirements.md`; `spec/specification.md` §7.3 | Phase 16 | `TicketNotFoundException` | `get_unknownTicket_returnsNotFound`; `comment_unknownTicket_isNotFound` (ran) | 404 `NOT_FOUND` |
| REQ-026 Validate before persist | `spec/requirements.md`; `spec/specification.md` §7.1 | Phase 16 | Domain rules before `tickets.save` | Invalid create/transition leave no/unchanged store (ran) | |
| REQ-027 Validation errors identifiable | `spec/requirements.md`; `spec/specification.md` §8.1 | Phase 16 | `ApiErrorResponse` + `fields` | API 400 tests (ran) | |
| REQ-028 Illegal transition business error | `spec/requirements.md`; `spec/specification.md` §6.4, §8.1 | Phase 12; Phase 16 | 409 `ILLEGAL_TRANSITION` | API invalidTransition (ran) | Not mapped as VALIDATION |
| REQ-029 Not-found errors | `spec/requirements.md`; `spec/specification.md` §8.1 | Phase 16; V-NOT-FOUND | 404 envelope; `TicketNotFoundView` | API 404 (ran); `ui.test.tsx` (ran, stub) | |
| REQ-030 Unexpected errors safe | `spec/requirements.md`; `spec/specification.md` §8.1 | Phase 16 | `RestExceptionHandler` 500; no stack in body | `search_isNotImplemented` (ran; deferred OQ → UNEXPECTED) | No secrets in body |
| REQ-031 Meaningful UI errors | `spec/requirements.md`; `spec/specification.md` §8.2, §10 | Phase 15 UI | `ErrorBanner` | `ui.test.tsx` (ran) | Browser E2E not run |
| REQ-032 UI success only after backend success | `spec/requirements.md`; `spec/specification.md` §8.2 | Phase 15–16 | Mutations wait for API `2xx` | UI tests (ran, stub) | Live browser not run |
| REQ-033 Persist ticket data | `spec/requirements.md`; `spec/specification.md` §9 | Phase 16 JPA | `TicketEntity` / H2 tests / PostgreSQL runtime config | API create then GET (ran, H2) | PostgreSQL restart not claimed |
| REQ-034 Tickets survive restart | `spec/requirements.md`; `spec/specification.md` §9 | Phase 16 (H2 across HTTP only) | JPA | GET after separate request (ran) | Not a PostgreSQL process restart |
| REQ-035 Comments survive restart | `spec/requirements.md`; `spec/specification.md` §9 | Phase 16 | `CommentEntity` | `comment_isStoredOnTicket` then GET (ran, H2) | PostgreSQL restart not claimed |
| REQ-036 Updates persist | `spec/requirements.md`; `spec/specification.md` §9 | Phase 16 | PATCH then GET | `update_replacesSuppliedFieldsOnly` (ran) | |
| REQ-037 No persistence of rejected work | `spec/requirements.md`; `spec/specification.md` §9 | Phase 16 | Invalid transition GET still old status | `invalidTransition_returns409AndLeavesStatus` (ran) | |
| REQ-038 Java 21 target runtime | `spec/requirements.md`; `spec/specification.md` §11 | Phase 11 | `pom.xml` `java.version` 21 | `mvn test` Java 21 JBR (ran) | Unchanged in Phase 16 |
| REQ-039 Spring Boot | `spec/requirements.md`; `spec/specification.md` §11 | Phase 11 | parent 3.5.5 | `mvn test` (ran) | Unchanged in Phase 16 |
| REQ-040 REST API | `spec/requirements.md`; `spec/specification.md` §6.6, §11 | Phase 16 | `TicketController` | `TicketApiIntegrationTest` (ran) | |
| REQ-041 PostgreSQL / H2 | `spec/requirements.md`; `spec/specification.md` §9, §11 | Phase 16A+16B | `application.properties` PostgreSQL placeholders; `application-test.properties` H2 | H2 integration tests (ran) | No committed credentials |
| REQ-042 React / Next.js or equivalent | `spec/requirements.md`; `spec/specification.md` §10, §11 | Phase 15–16 | `frontend/` + `VITE_API_BASE_URL` | `npm test` 13 passed; `npm run build` succeeded (Phase 19, 2026-09-21) | OQ-018 family; browser E2E not run |
| REQ-043 Maintainability | `spec/requirements.md`; `spec/specification.md` §1, §11 | Phase 16 layers | `api` / `application` / `domain` / `persistence` | Review of layering | No extra features |
| REQ-044 Testability | `spec/requirements.md`; `spec/specification.md` §11 | Phase 14–19 | Domain + API integration + persistence query tests | `mvn test` 149 tests, 0 failures (ran 2026-09-21T15:45:32+05:30) | |
| REQ-045 Security (no invented auth) | `spec/requirements.md`; `spec/specification.md` §12 | Phase 16 | No auth module; CORS localhost Vite only | Inspection | |
| REQ-046 Input validation quality | `spec/requirements.md`; `spec/specification.md` §7 | Phase 16 | API + domain | Validation API tests (ran) | |
| REQ-047 Error handling consistency | `spec/requirements.md`; `spec/specification.md` §8.1 | Phase 16 | Four error codes | API 400/404/409/500 tests (ran) | |
| REQ-048 Durable persistence quality | `spec/requirements.md`; `spec/specification.md` §9 | Phase 16 H2 | JPA | HTTP-round persistence (ran) | PostgreSQL restart not claimed |
| REQ-049 API consistency | `spec/requirements.md`; `spec/specification.md` §11 | Phase 16 | Contract JSON names | Integration tests (ran) | |
| REQ-050 No secrets in Git | `spec/requirements.md`; `spec/specification.md` §12 | Phase 16 | Env placeholders; `.gitignore` local env files | Inspection | Empty H2 test password is not a secret |

## Phase 4 architecture mapping

Structural ownership for all requirements is recorded in `spec/architecture.md` §16. Phase 16 implements those layers.

| Concern | Architecture section | Requirements |
|---|---|---|
| System boundaries, UI/backend split | `spec/architecture.md` §2–§3 | REQ-019, REQ-040, REQ-042 |
| Backend layers and dependency direction | `spec/architecture.md` §4–§5 | REQ-001–REQ-011, REQ-043 |
| Persistence boundary | `spec/architecture.md` §6 | REQ-033–REQ-037, REQ-041, REQ-048 |
| Validation and errors | `spec/architecture.md` §7 | REQ-020–REQ-032, REQ-046, REQ-047 |
| Lifecycle ownership | `spec/architecture.md` §8 | REQ-013–REQ-019, REQ-028 |
| Search/filter placement | `spec/architecture.md` §9 | REQ-009, REQ-010, REQ-023 |
| Comments | `spec/architecture.md` §10 | REQ-008, REQ-018, REQ-024, REQ-035 |
| Security (no invented auth) | `spec/architecture.md` §11 | REQ-045, REQ-050 |
| Logging | `spec/architecture.md` §12 | REQ-030, REQ-045 |
| Testing boundaries | `spec/architecture.md` §13 | REQ-044 |
| Java 21 / Spring Boot targets | `spec/architecture.md` §14 | REQ-038, REQ-039 |

## Phase 5 data-model mapping

Logical records and attributes are in `spec/data-model.md`. Phase 16 added JPA entities matching the logical Ticket/Comment model. Ticket identity is a UUID. Comment technical UUID is persistence-only.

| Concern | Data-model section | Requirements |
|---|---|---|
| Ticket attributes, identity, status, priority, assignee | `spec/data-model.md` §2 | REQ-001, REQ-012–REQ-014, REQ-006, REQ-007, REQ-021, REQ-022 |
| Comment attributes | `spec/data-model.md` §3 | REQ-008, REQ-024, REQ-035 |
| Ticket–comment relationship | `spec/data-model.md` §4 | REQ-018, REQ-003 |
| Required vs optional / deferred | `spec/data-model.md` §5 | REQ-001, OQ-001–OQ-004 |
| Validation constraints on stored data | `spec/data-model.md` §6 | REQ-020–REQ-026, VAL-001–VAL-005 |
| Persistence / restart | `spec/data-model.md` §7 | REQ-033–REQ-037, REQ-041, REQ-048 |
| Integrity including lifecycle writes | `spec/data-model.md` §8 | REQ-013–REQ-017, REQ-025, REQ-037 |
| Terminal-state data | `spec/data-model.md` §9 | REQ-017, OQ-008, OQ-009 |
| Search/filter as queries | `spec/data-model.md` §10 | REQ-009, REQ-010 |

## Phase 6 API-contract mapping

HTTP resources are in `spec/api-contract.md`. Phase 16 implemented `TicketController` and DTOs. Keyword search remains deferred (OQ-005).

| Concern | API section | Requirements |
|---|---|---|
| Create ticket | `POST /tickets` | REQ-001, REQ-012, REQ-013 |
| List / search / filter | `GET /tickets` | REQ-002, REQ-009, REQ-010, REQ-023 |
| Ticket details | `GET /tickets/{id}` | REQ-003, REQ-018, REQ-025 |
| Field updates | `PATCH /tickets/{id}` | REQ-004–REQ-007, REQ-021, REQ-022 |
| Comments | `POST /tickets/{id}/comments` | REQ-008, REQ-024 |
| Status change | `POST /tickets/{id}/status` | REQ-011, REQ-015–REQ-017, REQ-019, REQ-028 |
| Error envelope | `spec/api-contract.md` §2 | REQ-027–REQ-032, REQ-047 |
| REST consistency | `spec/api-contract.md` | REQ-040, REQ-049 |

## Phase 7 state-machine mapping

Lifecycle rules are in `spec/state-machine.md`. Domain `TicketLifecycle` is the only transition table. Phase 16 REST maps illegal pairs to `409 ILLEGAL_TRANSITION`.

| Concern | State-machine section | Requirements |
|---|---|---|
| Status set and initial `OPEN` | `spec/state-machine.md` §1–§2 | REQ-013, REQ-014 |
| Valid transitions T1–T5 | `spec/state-machine.md` §3 | REQ-015, REQ-011 |
| Invalid pairs and reject/unchanged store | `spec/state-machine.md` §4, §6, §9 | REQ-016, REQ-026, REQ-028, REQ-037 |
| Terminal `CLOSED` / `CANCELLED` | `spec/state-machine.md` §5 | REQ-017 |
| Backend authority | `spec/state-machine.md` §8 | REQ-019 |
| API `POST /tickets/{id}/status` | `spec/state-machine.md` §10 | REQ-011, REQ-040 |
| Required integration scenarios | `spec/state-machine.md` §11 | REQ-044 |
| Same-status deferred | `spec/state-machine.md` §7 | OQ-012 |

## Phase 8 UI-flow mapping

User journeys are in `spec/ui-flow.md`. Phase 15 implemented the views. Phase 16 pointed `createHttpTicketApi` at Spring Boot via `VITE_API_BASE_URL`. Browser/E2E was not run.

| Concern | UI-flow section | Requirements |
|---|---|---|
| Views and navigation | `spec/ui-flow.md` §1–§2 | REQ-002, REQ-003, REQ-042 |
| Create | `spec/ui-flow.md` §5 | REQ-001, REQ-013, REQ-021 |
| List | `spec/ui-flow.md` §4 | REQ-002 |
| Detail | `spec/ui-flow.md` §6 | REQ-003, REQ-018 |
| Edit fields | `spec/ui-flow.md` §7 | REQ-004–REQ-007 |
| Comments | `spec/ui-flow.md` §8 | REQ-008, REQ-024 |
| Status change | `spec/ui-flow.md` §9, §15 | REQ-011, REQ-015–REQ-017, REQ-028 |
| Search / filter | `spec/ui-flow.md` §10–§11 | REQ-009, REQ-010 |
| Errors / loading / empty | `spec/ui-flow.md` §3, §12–§13 | REQ-027, REQ-029–REQ-032 |
| API map | `spec/ui-flow.md` §14 | REQ-040, REQ-049 |

## Phase 9 test-strategy mapping

Planned verification is in `spec/test-strategy.md`. Domain tests ran in Phase 14. API/H2 integration tests ran in Phase 16. UI unit tests ran in Phase 15/16. E2E against PostgreSQL was not run.

| Concern | Test-strategy section | Requirements |
|---|---|---|
| Levels / unit / application / API | `spec/test-strategy.md` §2–§5 | REQ-040, REQ-044, REQ-046 |
| Persistence and restart | `spec/test-strategy.md` §6, §14 | REQ-033–REQ-037, REQ-048 |
| State machine T1–T5, invalid, terminal, OPEN | `spec/test-strategy.md` §7 | REQ-011, REQ-013, REQ-015–REQ-017, REQ-028 |
| Same-status deferred | `spec/test-strategy.md` §7.6 | OQ-012 |
| Validation / errors | `spec/test-strategy.md` §8 | REQ-020–REQ-032, REQ-047 |
| Search / filter / comments | `spec/test-strategy.md` §9–§10 | REQ-008–REQ-010, REQ-018 |
| UI / E2E | `spec/test-strategy.md` §11–§12 | REQ-031, REQ-032, REQ-042 |
| REQ → test map | `spec/test-strategy.md` §18 | REQ-001–REQ-050 |

## Phase 14 domain test mapping (executed)

| Area | Spec / IDs | Test class | Notes |
|---|---|---|---|
| Initial OPEN | REQ-013 | `TicketTest.create_*` | Create is not a from→to transition |
| T1–T5 | REQ-015; state-machine T1–T5 | `TicketStatusTransitionTest.t1`–`t5` | Spec IDs, not remapped prompt labels |
| Invalid pairs + unchanged status | REQ-016, REQ-026, REQ-028 | `TicketStatusTransitionTest.invalidTransition_*` | All 15 §4 pairs |
| Terminal CLOSED/CANCELLED | REQ-017 | `TicketStatusTransitionTest.closedAndCancelled_*` | Same-status not asserted (OQ-012) |
| VAL-001 title | REQ-021 | `TicketTest.create_rejectsMissingTitle` / `Blank` / `WhitespaceOnly` / `acceptsValidTitle` | No length tests (OQ-015) |
| VAL-005 status token | REQ-014, REQ-023 | `TicketStatusTransitionTest.changeStatus_unknownToken_*`; filter wrong-case/blank/unknown | Exact tokens |
| VAL-002 priority | REQ-022 | None | OQ-001 deferred |
| Comments VAL-003/004 | REQ-008, REQ-024 | `CommentTest.addComment_rejectsBlank*` / `WhitespaceOnly*` | No edit/delete |
| Comment association | REQ-018 | `CommentTest.commentBelongsOnlyToItsTicket` | No OQ-009 tests |
| Status filter | REQ-010 | `TicketStatusFilterTest` | No OQ-007 combo |
| Keyword search | REQ-009 | `TicketKeywordSearchTest` | OQ-005 boundary only |
| API / persistence / restart / UI | test-strategy §§5–6, 11–12, 14 | Phase 16 H2 API tests; UI unit tests | PostgreSQL restart and browser E2E not executed |

## Phase 15 UI mapping

| View / behavior | Spec | Implementation | Test |
|---|---|---|---|
| V-LIST | ui-flow §4, §10, §11 | `TicketListView.tsx` | loading/empty/error/filter (ran) |
| V-CREATE | ui-flow §5 | `TicketCreateView.tsx` | blank title; 400 fields; no status control (ran) |
| V-DETAIL | ui-flow §6–§9 | `TicketDetailView.tsx` | fields/comments; T1–T5 offers; terminal; 409 keeps status (ran) |
| V-NOT-FOUND | ui-flow §12 | `TicketNotFoundView.tsx` | heading/status (ran) |
| HTTP client | api-contract §4 | `createHttpTicketApi` + `VITE_API_BASE_URL` | unit tests inject a stub; live browser not run |
| Status options | state-machine T1–T5 | `nextStatuses` | unit asserts (ran) |

## Phase 16 integration mapping (executed, H2)

| Area | Spec / IDs | Implementation | Test |
|---|---|---|---|
| Create `POST /tickets` | REQ-001, REQ-012, REQ-013, VAL-001 | `TicketController.create`; `TicketService.create` | `create_assignsUuidAndOpenStatus`; `create_rejectsBlankTitle`; `create_rejectsClientAssignedId` |
| List `GET /tickets` | REQ-002 | `TicketController.list` | `list_returnsEmptyArrayWhenNoTickets`; `list_andDetail_returnPersistedTickets` |
| Detail `GET /tickets/{id}` | REQ-003, REQ-025 | `TicketController.get` | `get_unknownTicket_returnsNotFound` |
| PATCH fields | REQ-004–REQ-007, VAL-001 | `TicketController.update` | `update_replacesSuppliedFieldsOnly`; `update_rejectsStatusInPatchBody`; `update_emptyBody_isValidation` |
| Comments | REQ-008, REQ-018, VAL-003/004 | `TicketController.addComment` | `comment_isStoredOnTicket`; `comment_rejectsBlankContent`; `comment_unknownTicket_isNotFound` |
| Status T1–T5 | REQ-011, REQ-015 | `POST /tickets/{id}/status` via domain | `validTransitions_persistNewStatus` |
| Invalid transitions | REQ-016, REQ-017, REQ-028 | `TicketLifecycle` → 409 | `invalidTransition_returns409AndLeavesStatus` (15 pairs) |
| Filter | REQ-010, VAL-005 | `TicketStatusFilter` | `filter_matchesEachStatus`; `filter_unknownStatus_isValidationNotEmptyList`; `filter_emptyResult_isEmptyArray` |
| Search deferred | REQ-009; OQ-005 | `TicketKeywordSearch` | `search_isNotImplemented` |
| Persistence across HTTP | REQ-033, REQ-036, REQ-037 | JPA + H2 | create/update/comment/status then GET |

## Phase 18 review mapping

Review evidence is `docs/phase-18-review.md`. Specs were not changed to match implementation. Production code was not changed.

| Concern | Review result | Notes |
|---|---|---|
| REQ-001–REQ-008, REQ-010–REQ-021, REQ-023–REQ-032, REQ-037–REQ-041, REQ-043, REQ-045–REQ-047, REQ-049–REQ-050 | Implementation matches decided specs where tested | See Phase 17 PASS rows; Phase 18 found no new FAIL-equivalent |
| REQ-009 search | Deferred OQ-005/006/007 | No matcher; API 500 fail-closed |
| REQ-022 VAL-002 | Deferred OQ-001 | Free-text priority stored |
| REQ-033–REQ-036, REQ-044 restart, REQ-048 | Environment untested | H2 HTTP only |
| REQ-042 live UI | Environment untested | jsdom stubs; no browser |
| Architecture filter execution | GAP | In-memory `findAll` + `TicketStatusFilter`; not a persistence query |
| OQ-013 / OQ-015 accidental runtime | RISK | Jackson ignore create `status`; JPA `VARCHAR(255)` |
| Test gaps | GAP | PATCH/status 404; blank comment author API; create `onCreated` |

## Phase 19 fix mapping

Technical fixes from Phase 18. Specs were not rewritten. OQs remain open.

| Finding | Implementation | Test |
|---|---|---|
| REQ-010 filter at persistence | `TicketJpaRepository.findByStatus`; `TicketService.list` | `TicketPersistenceQueryTest.findByStatus_*`; API `filter_*` (ran) |
| List without comments N+1 | `TicketMapper.toListItem`; `findWithCommentsById` | `findAll_doesNotInitializeComments`; `list_doesNotExposeComments_detailDoes` (ran) |
| OQ-015 no invented 255 | text columns on title/priority/assignee/author | `create_titleLongerThan255_isPersisted` (ran) |
| REQ-025 404 type mapping | `TicketNotFoundException` only | GET/PATCH/comment/status unknown id (ran) |
| VAL-004 author API | existing `@NotBlank` + domain | `comment_rejectsMissingAuthor` / `Blank` / `WhitespaceOnlyAuthor` (ran) |
| REQ-013 create always OPEN | unchanged `Ticket.create` | `create_clientSuppliedStatus_doesNotStoreNonOpenStatus` (ran; OQ-013 still open) |
| REQ-032 create navigation | `TicketCreateView` / `App` | `ui.test.tsx` create success + App navigation (ran) |
| Deferred OQ HTTP mapping | Unchanged `500 UNEXPECTED` | `search_isNotImplemented` (ran) |

## Phase 10 implementation-plan mapping

Work is scheduled in `spec/implementation-plan.md`. Stages 1–9 and persistence/application/REST (3–6) exist after Phase 16. Keyword matching remains OQ-005. Stages 11–14 (acceptance/review/fixes/final docs) are later.

| Stage | Plan section | Requirements (primary) |
|---|---|---|
| 1 Foundation (Java 21 / Boot — later) | `spec/implementation-plan.md` Stage 1 | REQ-038, REQ-039 |
| 2 Domain | Stage 2 | REQ-012–REQ-018, REQ-015–REQ-017 |
| 3 Persistence | Stage 3 | REQ-033–REQ-037, REQ-041 |
| 4 Application | Stage 4 | REQ-001–REQ-007, REQ-011, REQ-026 |
| 5 REST API | Stage 5 | REQ-040, REQ-027–REQ-030, REQ-049 |
| 6 State machine | Stage 6 | REQ-011, REQ-019, REQ-028 |
| 7 Comments/search/filter | Stage 7 | REQ-008–REQ-010 |
| 8 Backend tests | Stage 8 | REQ-044 |
| 9–11 UI / integration / acceptance | Stages 9–11 | REQ-031, REQ-032, REQ-042 |
| 12–14 Review / fixes / docs | Stages 12–14 | REQ-043 |

## Phase 1 baseline (not product requirements)

| Item | Specification | Task | Implementation | Test | Review |
|---|---|---|---|---|---|
| Preserve existing Maven project | `PLAN.md` Phase 1 constraints | Inspect, do not restructure | Unchanged `pom.xml`, `Main.java` | None | Inspection complete |
| Establish SDD governance | Rules, commands, docs listed in `PLAN.md` | Create governance files | Files under `spec/`, `.cursor/`, `.github/`, `docs/`, `PLAN.md` | None | Created; no business code |
