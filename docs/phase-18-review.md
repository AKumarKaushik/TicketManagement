# Phase 18 — AI / Code / Specification Review

**Product:** Support Ticket Management System  
**Date:** 2026-09-21  
**Phase:** 18 — Review only  
**Production code:** Not modified  
**Tests:** Not re-executed in this phase (source review). Last recorded run: Phase 17 (`mvn test` 135/0/0 BUILD SUCCESS at 2026-09-21T14:22:37+05:30; frontend 11 passed; `npm run build` succeeded).

---

## Executive Summary

Phase 18 compared the implemented application to REQ-001–REQ-050, the approved specifications, architecture, API contract, state machine, UI flow, test strategy, Phase 17 acceptance, and project history.

Decided behavior is largely implemented as specified:

- Create stores a system UUID and `OPEN`.
- Lifecycle T1–T5 and the fifteen invalid pairs live in `TicketLifecycle` only; REST maps illegal pairs to `409 ILLEGAL_TRANSITION` without changing stored status.
- Comments, status filter, and the four error classes match the contract for the paths that were tested.
- Keyword search matching was not invented (OQ-005 fail-closed).
- Authentication was not invented. Credentials are environment placeholders.

No requirement was found where implemented behavior **contradicts a decided rule** in a way that Phase 17 already classified as FAIL.

The review did find:

- Architecture drift: status filter is executed in memory after `findAll()`, not as a persistence query.
- Accidental implicit limits/behaviors that the OQ list still treats as unresolved: Hibernate default `VARCHAR(255)`, create-`status` ignored by Jackson, terminal PATCH allowed by omission.
- Test-quality gaps that let the 135/11 passing suites hide missing API/UI cases and untested environments (browser, PostgreSQL restart).
- Deferred OQs still unresolved; some are fail-closed as `500 UNEXPECTED`, which is not a matcher invention but is a product-facing mapping that needs human awareness.

Phases 19 and 20 were not started. Open questions were not resolved.

---

## Specification Findings

| ID | Area | Finding | Evidence | Classification |
| -- | ---- | ------- | -------- | -------------- |
| SPEC-001 | Create / identity | Create assigns `UUID.randomUUID`, stores `OPEN`, rejects client `id` with `400 VALIDATION`, returns `Location: /tickets/{uuid}`. | `TicketService.create`; `CreateTicketRequest`; `TicketApiIntegrationTest.create_assignsUuidAndOpenStatus`; `create_rejectsClientAssignedId` | COMPLIANT |
| SPEC-002 | Title VAL-001 | Blank/whitespace title rejected at Bean Validation and domain `requireTitle`. | `CreateTicketRequest.@NotBlank`; `Ticket.requireTitle`; `create_rejectsBlankTitle`; `TicketTest` | COMPLIANT |
| SPEC-003 | Lifecycle T1–T5 | Only the five approved pairs succeed. Table is in `TicketLifecycle`, not the controller. | `TicketLifecycle.isAllowed`; domain t1–t5; API `validTransitions_persistNewStatus` | COMPLIANT |
| SPEC-004 | Invalid transitions | Fifteen spec §4 pairs return `409 ILLEGAL_TRANSITION`; subsequent GET shows previous status. | `TicketApiIntegrationTest.invalidTransition_returns409AndLeavesStatus`; `TicketStatusTransitionTest` | COMPLIANT |
| SPEC-005 | Status filter | Exact five tokens; unknown token `400 VALIDATION` not `[]`; empty match is `[]`. | `TicketStatus.fromToken`; API `filter_*` tests | COMPLIANT |
| SPEC-006 | Search matching | No field/LIKE/case/partial matcher. `TicketKeywordSearch.matching` always OQ-005. | `TicketKeywordSearch`; `search_isNotImplemented` (500 UNEXPECTED) | DEFERRED |
| SPEC-007 | Error envelope | `status`, `errorCode`, `message`; `fields` for VALIDATION; omitted for 404/409/500. No stack in JSON. | `ApiErrorResponse`; `RestExceptionHandler`; API tests | COMPLIANT |
| SPEC-008 | Comment association | Comment belongs to one ticket; technical JPA UUID not on the wire; `Location` optional `/tickets/{id}`. | `Comment`; `CommentResponse`; `comment_isStoredOnTicket` | COMPLIANT |
| SPEC-009 | Create `status` (OQ-013) | DTO does not bind `status`. Spring Boot Jackson defaults ignore unknown properties, so a supplied `status` is dropped and create still stores `OPEN`. Ignore-vs-reject was never recorded as a human decision. | `CreateTicketRequest` (no `status`); Boot default `FAIL_ON_UNKNOWN_PROPERTIES=false`; `spec/api-contract.md` §4.1 | NEEDS HUMAN DECISION |
| SPEC-010 | Terminal PATCH (OQ-008) | Field mutators have no terminal guard. PATCH on `CLOSED`/`CANCELLED` currently succeeds if VAL-001 passes. Contract does not require 409 or allow. Untested. | `Ticket.changeTitle` etc.; no OQ-008 tests | DEFERRED |
| SPEC-011 | Terminal comments (OQ-009) | `addComment` on terminal throws `OpenQuestionDeferredException` → API `500 UNEXPECTED`. Not allow and not `VALIDATION`/`ILLEGAL_TRANSITION`. | `Ticket.addComment`; `RestExceptionHandler.deferred` | DEFERRED |
| SPEC-012 | Same-status (OQ-012) | `from == to` throws `OpenQuestionDeferredException` → `500 UNEXPECTED`. Domain tests explicitly do not assert 200 or 409. | `TicketLifecycle` lines 23–27; `TicketStatusTransitionTest` comment at OQ-012 | DEFERRED |
| SPEC-013 | VAL-002 / OQ-001 | No recognized priority set. Free-text priority is stored. Data-model §2.3 says not to persist free-text outside a recognized set; that set does not exist. | `Ticket.changePriority`; no VAL-002 tests | DEFERRED |
| SPEC-014 | Field lengths (OQ-015) | Spec has no max length. JPA default `String` columns are `VARCHAR(255)` for title, priority, assignee, comment author. Overflow would likely surface as `500 UNEXPECTED`, not VAL-*. | `TicketEntity`; `CommentEntity` (content/description are `text`) | RISK |
| SPEC-015 | Combined search+filter | Both query params → OQ-007 deferred → 500. No combined meaning. | `TicketService.list` | DEFERRED |
| SPEC-016 | Blank keyword | Blank `keyword` → OQ-006 deferred → 500. Neither list-all nor 400. | `TicketService.list` | DEFERRED |
| SPEC-017 | Pagination/order | No `page`/`size`/`sort`. Full `findAll()`. Consistent with “do not add until OQ-014”. | `TicketController.list`; `TicketJpaRepository` | DEFERRED |
| SPEC-018 | Concurrency | No `@Version` / locking. Passing tests do not prove OQ-019. | Entities; test suite | DEFERRED |
| SPEC-019 | Extra features | No auth, delete ticket, comment edit/delete, dashboards, attachments. | Controllers; frontend routes | COMPLIANT |
| SPEC-020 | UI success after 2xx | Create/update/comment/status wait for API then update UI. Create success navigation is coded (`onCreated` → detail) but not unit-tested. | `TicketCreateView`; `App.tsx`; `ui.test.tsx` | GAP |

---

## Architecture Findings

Intended dependency direction is intact: API → application → domain + persistence port; JPA types stay in `persistence`.

| ID | Finding | Evidence | Classification |
| -- | ------- | -------- | -------------- |
| ARCH-001 | `TicketController` maps HTTP/DTOs only. No transition table. | `TicketController.java` | COMPLIANT |
| ARCH-002 | `TicketService` orchestrates use cases and transactions; lifecycle stays in domain. | `TicketService`; `Ticket.changeStatus` → `TicketLifecycle` | COMPLIANT |
| ARCH-003 | Persistence isolated behind `TicketRepository`. Adapter is package-private. | `application/TicketRepository`; `TicketRepositoryAdapter` | COMPLIANT |
| ARCH-004 | DTOs are not domain types. Entities are not returned from REST. | `api/dto/*`; `TicketApiMapper` | COMPLIANT |
| ARCH-005 | **Status filter execution is not in persistence.** Architecture §4.4 / §9 say filter queries run against storage. `TicketService.list` calls `tickets.findAll()` then `TicketStatusFilter.apply`. Unused `TicketRepository.findByStatus` itself also `findAll()` + in-memory filter. | `TicketService.list` lines 65–67; `TicketRepositoryAdapter.findByStatus` | GAP |
| ARCH-006 | List mapping reconstitutes **comments** for every ticket even though list JSON has no comments. Default `@OneToMany` is LAZY → N+1 on `findAll()`. `spring.jpa.open-in-view=false` is correct; work happens inside `@Transactional`. | `TicketMapper.toDomain`; `TicketEntity` comments; `application.properties` | RISK |
| ARCH-007 | Frontend copies T1–T5 in `statusTransitions.ts`. Architecture allows UI convenience; backend remains authority. Drift risk if one table changes. | `frontend/src/domain/statusTransitions.ts`; `TicketLifecycle` | RISK |
| ARCH-008 | Constructor injection, `final` collaborators, no field `@Autowired`. | Controllers/services/adapters | COMPLIANT |
| ARCH-009 | `TicketRepository.deleteAll()` is a production port used as a test seam. Not exposed over HTTP. | `TicketRepository`; `TicketApiIntegrationTest.clearStore` | RISK |
| ARCH-010 | Runtime `spring.jpa.hibernate.ddl-auto=update` (overridable). No Flyway/Liquibase. Schema is generated, not versioned. Specs did not require a migrator. | `application.properties` | RISK |

No controller business-rule violation was found. No JPA type appears in `com.enterprise.ai.domain`.

---

## Domain Findings

| ID | Finding | Evidence | Classification |
| -- | ------- | -------- | -------------- |
| DOM-001 | `Ticket.create` always `OPEN`. Identity is supplied; UUID generation is application-layer. | `Ticket.create`; `TicketService.create` | COMPLIANT |
| DOM-002 | VAL-001 in `requireTitle`. VAL-003/004 in `addComment`. VAL-005 in `TicketStatus.fromToken` (exact, case-sensitive). | Domain classes | COMPLIANT |
| DOM-003 | Authoritative transition table is only `TicketLifecycle`. CLOSED/CANCELLED have no outgoing T-pairs (`false`). | `TicketLifecycle` switch | COMPLIANT |
| DOM-004 | Same-status is not classified as allowed or illegal (OQ-012). | `TicketLifecycle` throws `OpenQuestionDeferredException` | DEFERRED |
| DOM-005 | Terminal comments fail-closed (OQ-009), not a product allow/deny. | `Ticket.addComment` | DEFERRED |
| DOM-006 | `reconstitute` does not re-apply VAL-001. Intended for persistence load; a corrupt row could re-enter the domain. | `Ticket.reconstitute` | RISK |
| DOM-007 | Check order for status: token parse (VAL-005) then `isAllowed` (OQ-012 then T1–T5 / illegal). Matches `spec/state-machine.md` §6 as documented on `Ticket.changeStatus`. | `Ticket.changeStatus(String)` → `fromToken` → `isAllowed` | COMPLIANT |

Valid transitions verified in code:

```text
OPEN → IN_PROGRESS
OPEN → CANCELLED
IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED
RESOLVED → CLOSED
```

No other pair returns true from `TicketLifecycle.isAllowed`.

---

## Persistence Findings

| ID | Finding | Evidence | Classification |
| -- | ------- | -------- | -------------- |
| PER-001 | Ticket id persisted as `UUID`. API string via `UUID.toString` / `fromString`. | `TicketEntity.id`; mapper | COMPLIANT |
| PER-002 | Comments: `ManyToOne` ticket, `CascadeType.ALL`, `orphanRemoval=true`. Technical comment UUID generated, not in API JSON. | `CommentEntity`; `comment_isStoredOnTicket` asserts `$.id` absent | COMPLIANT |
| PER-003 | Status stored as token string; reload via `TicketStatus.valueOf`. Invalid DB token would become unexpected 500. | `TicketMapper.toDomain` | RISK |
| PER-004 | Comment copy is **append-by-size**: new domain comments after `entity.getComments().size()` are inserted. No edit/delete in spec, so this matches “comments only grow.” Fragile if collection size/order diverges (OQ-011). | `TicketMapper.copyToEntity` | RISK |
| PER-005 | Nullability: title/status required; description/priority/assignee nullable. Comment content/author/creationTime required. | Entities | COMPLIANT with deferred optionality |
| PER-006 | No `@OrderColumn`. Comment order is not a specified product rule (OQ-011). | `TicketEntity.comments` | DEFERRED |
| PER-007 | Transactions on `TicketService` write/read methods. Controllers are not transactional. | `@Transactional` on service | COMPLIANT |
| PER-008 | Filter/search not executed as parameterized SQL. Filter is in-memory; search never matches. Architecture wanted persistence execution for filter. | Adapter `findAll`; `TicketKeywordSearch` | GAP (filter) / DEFERRED (search) |
| PER-009 | Hibernate default 255-char columns invent a length until OQ-015. Description/comment content are `text`. | `TicketEntity` title/priority/assignee; `CommentEntity.author` | RISK |
| PER-010 | H2 tests use `create-drop` + PostgreSQL mode. Runtime PostgreSQL + restart were not reviewed as executed behavior (Phase 17 NOT RUN). | `application-test.properties`; acceptance report §12 | DEFERRED (environment) |
| PER-011 | No optimistic lock column (OQ-019). Last write wins. | Entities | DEFERRED |

---

## API Findings

Endpoints match `spec/api-contract.md` §5:

| Method | Path | Implementation | Notes |
| --- | --- | --- | --- |
| POST | `/tickets` | `TicketController.create` | 201 + `Location` |
| GET | `/tickets` | `list` | `status` / `keyword` query params |
| GET | `/tickets/{id}` | `get` | details + comments |
| PATCH | `/tickets/{id}` | `update` | field presence flags |
| POST | `/tickets/{id}/comments` | `addComment` | 201 + Location `/tickets/{id}` |
| POST | `/tickets/{id}/status` | `changeStatus` | domain lifecycle |

| ID | Finding | Evidence | Classification |
| -- | ------- | -------- | -------------- |
| API-001 | PATCH `status`/`id` → 400 VALIDATION. Empty PATCH → 400. | `TicketService.update`; tests | COMPLIANT |
| API-002 | Status POST does not bypass domain. | `tickets.changeStatus` → `ticket.changeStatus` | COMPLIANT |
| API-003 | `ChangeStatusRequest` has no `@NotBlank`; missing/unknown token still VAL-005 via domain. | `ChangeStatusRequest`; `changeStatus_unknownToken_isValidation` | COMPLIANT |
| API-004 | Malformed JSON / wrong media type → 400 VALIDATION, generic message, no stack. **No dedicated integration test.** | `RestExceptionHandler.malformed` | GAP (test) |
| API-005 | Deferred OQs mapped to `500 UNEXPECTED` with generic `"The request could not be completed."` Search, blank keyword, combined filter, same-status, terminal comments all look like infrastructure failure to the client. Fail-closed, not a fifth error class. | `RestExceptionHandler.deferred` | RISK |
| API-006 | `IllegalArgumentException` whose message starts with `"Ticket identity"` is forced to 404. `TicketId` blank uses that prefix. Fragile string coupling. Invalid UUID path uses adapter `parseUuid` → empty → `TicketNotFoundException` (404), which is appropriate. | `RestExceptionHandler.illegalArgument`; `TicketId`; `TicketRepositoryAdapter.parseUuid` | RISK |
| API-007 | No API test for PATCH unknown id, POST status unknown id, or blank comment author (domain + `@NotBlank` exist). | `TicketApiIntegrationTest` | GAP |
| API-008 | Create with extra `status` is untested; likely 201 OPEN (SPEC-009). | No test | GAP |
| API-009 | Keyword search 500 is tested as not-implemented. That test would still pass if someone later mapped a different failure to UNEXPECTED without matching. It does **not** prove matching is absent except together with `TicketKeywordSearch`. | `search_isNotImplemented`; domain search test | COMPLIANT (with the domain test) |

UUID on the wire is a string. Comment technical id is absent. Error classes 400/404/409/500 match the contract for implemented paths.

---

## Frontend Findings

| ID | Finding | Evidence | Classification |
| -- | ------- | -------- | -------------- |
| UI-001 | Views V-LIST, V-CREATE, V-DETAIL, V-NOT-FOUND exist. In-memory route in `App.tsx`. | `frontend/src/views/*`; `App.tsx` | COMPLIANT |
| UI-002 | No create status control. Status buttons from `nextStatuses` only (T1–T5). Terminal offers none. | `TicketCreateView`; `TicketDetailView`; `ui.test.tsx` | COMPLIANT |
| UI-003 | HTTP client uses contract paths and `VITE_API_BASE_URL`. No mock backend in the app. Tests inject a stub. | `httpTicketApi.ts`; `App.tsx` | COMPLIANT |
| UI-004 | Search input does not invent matching; blank search is not submitted (OQ-006 not answered as list-all/400). Live search will receive 500 until OQ-005. | `TicketListView.applySearch`; backend `TicketKeywordSearch` | DEFERRED |
| UI-005 | Filter and search are exclusive modes (OQ-007 not combined). | `ListMode` union | DEFERRED |
| UI-006 | 404 detail calls `onNotFound`; no blank ticket form. V-NOT-FOUND copy states it is not an empty ticket. | `TicketDetailView`; `TicketNotFoundView`; tests | COMPLIANT |
| UI-007 | Comment form remains visible on terminal tickets (OQ-009 not invented as hide/force). Submit would 500. | `TicketDetailView` comments section | DEFERRED |
| UI-008 | Save-fields always PATCHes title, description, priority, assignee together. Contract allows supplying those fields; blank strings may be stored (OQ-003/004). | `TicketDetailView.saveFields` | DEFERRED |
| UI-009 | Successful create → `onCreated(id)` → detail route is implemented, **not covered by a unit test**. | `App.tsx`; `ui.test.tsx` V-CREATE | GAP |
| UI-010 | Browser/E2E not executed. jsdom tests do not prove live CORS, Vite proxy, or PostgreSQL. | Phase 17 §11 | GAP (environment) |
| UI-011 | Create/update wait for API before success UI. Blank title does not call create. | `TicketCreateView`; tests | COMPLIANT |

---

## Test Quality Findings

Last recorded counts (Phase 17, not re-run here): backend 135 (API 44, CommentTest 17, TicketKeywordSearchTest 2, TicketStatusFilterTest 22, TicketStatusTransitionTest 30, TicketTest 20); frontend 11.

What the tests **do** prove for decided rules:

- Domain T1–T5, all 15 invalid pairs, status unchanged on reject, VAL-001, VAL-003/004, VAL-005, filter exactness and non-mutation, search deferred boundary.
- HTTP create UUID/OPEN/Location, list empty/non-empty, GET 404, PATCH fields, reject PATCH status, empty PATCH, comment persist, blank content, comment 404, valid transitions persist, invalid → 409 + unchanged, unknown status token 400, filter five statuses / unknown / empty.
- UI loading/empty/error list, filter call, blank create without API, backend 400 on create, detail fields/buttons, terminal none, 409 keeps status, 404 not a blank ticket.

| ID | Finding | Why it matters | Classification |
| -- | ------- | -------------- | -------------- |
| TEST-001 | Frontend tests stub `TicketApi`. They cannot fail if REST paths, CORS, or JSON names drift. | Passing 11 tests ≠ live contract | RISK |
| TEST-002 | H2 integration ≠ PostgreSQL restart. | REQ-033–036/048 unproven | GAP (environment) |
| TEST-003 | No API PATCH/status 404 tests. | Unknown-id mapping could regress | GAP |
| TEST-004 | No named API test for blank comment author. | Relies on `@NotBlank` + domain | GAP |
| TEST-005 | No UI success-path create navigation test. | Acceptance gap carried forward | GAP |
| TEST-006 | No tests encode OQ-012 as 200 or 409. Domain comments say not to. | Correct deferral | COMPLIANT |
| TEST-007 | `search_isNotImplemented` asserts 500. Coupled to fail-closed mapping, not to “no LIKE”. Domain test asserts OQ-005 id. | Together they prove no matcher | COMPLIANT |
| TEST-008 | VAL-002, terminal PATCH, same-status POST, search matching, browser E2E, PostgreSQL restart remain untested. | Same as Phase 17 gaps | DEFERRED / GAP |
| TEST-009 | Parameterized lifecycle tests are strong. Filter empty-result uses an OPEN ticket vs CLOSED filter — proves empty array not 404. | Good oracle | COMPLIANT |
| TEST-010 | `ticketAt` helper uses real T1–T5 setup, not a persistence backdoor. | Does not bypass domain | COMPLIANT |
| TEST-011 | `deleteAll` between tests isolates API tests. Order independence is OK on H2. | Isolation | COMPLIANT |
| TEST-012 | Passing domain tests would not catch N+1, VARCHAR(255), or Jackson ignore of create `status`. | Implementation-detail gaps | RISK |

Tests that could pass while production is wrong for a **decided** rule were not found for T1–T5 or 409 mapping. Tests **can** pass while search, restart, live UI, VAL-002, and OQ-008/012 remain unspecified or untested.

---

## Security Findings

| ID | Finding | Evidence | Classification |
| -- | ------- | -------- | -------------- |
| SEC-001 | Datasource username/password are `${…}` placeholders. No committed production password or API key in application config. | `application.properties` | COMPLIANT |
| SEC-002 | H2 test `password=` empty for in-memory `sa`. Not a production secret. | `application-test.properties` | COMPLIANT |
| SEC-003 | `frontend/.env.development` is only `VITE_API_BASE_URL=http://localhost:8080`. Not gitignored (unlike `.env.local`). Public URL, not a credential. | `.gitignore`; `.env.development` | COMPLIANT |
| SEC-004 | `.gitignore` covers `.env`, `.env.local`, `application-local.properties`, `frontend/.env.local`. | `.gitignore` | COMPLIANT |
| SEC-005 | No authentication module, login route, or 401/403. Matches “do not invent auth.” | API contract §1; `TicketController` | COMPLIANT |
| SEC-006 | CORS origins default to local Vite (`localhost` / `127.0.0.1:5173`), methods GET/POST/PATCH/OPTIONS, not `*`, `allowCredentials=false`. Overridable via `APP_CORS_ALLOWED_ORIGINS`. | `ApplicationConfig`; `application.properties` | COMPLIANT |
| SEC-007 | API 500 body is generic. Stack traces go to server logs only (`log.error`). | `RestExceptionHandler` | COMPLIANT |
| SEC-008 | No query-string concatenation; JPA parameterized. | Adapter uses Spring Data | COMPLIANT |
| SEC-009 | Workspace is not a git repository. History-wide secret scan is not applicable. | Phase 17 | GAP (environment) |
| SEC-010 | `ddl-auto=update` and overridable CORS are operational risks, not committed secrets. | properties | RISK |

---

## AI Validation Findings

Evidence is taken from `docs/ai-validation.md`, `docs/prompt-history.md`, and the current tree. No fabricated mistakes.

### AV-001 — Assumed Spring Boot already existed

```text
AI suggestion/behavior
→ Why it was questionable
→ Specification/reference
→ Human decision
→ Current implementation impact
```

- **AI suggestion/behavior:** Phase 1 user prompt stated the repo already contained Spring Boot/Maven.
- **Why it was questionable:** Inspection showed Java 17 Hello World, no Boot parent, no dependencies.
- **Specification/reference:** Phase 1 inspect-first; `pom.xml` / `Main.java` at that time.
- **Human decision:** Treat Boot as a later target; do not invent it in Phase 1.
- **Current impact:** None remaining. Boot 3.5.5 was added only after Phase 11 human approval. Recorded as `docs/ai-validation.md` Entry 001.

### AV-002 — Inventing a Spring Boot / DB version to “make it start”

- **AI suggestion/behavior:** Pressure in Phase 11/16 to pick unpublished Boot versions, H2/PostgreSQL, and JPA so the app would run.
- **Why it was questionable:** OQ-016 and persistence access style were unanswered; Stage 1 forbade silent invention.
- **Specification/reference:** `spec/implementation-plan.md` Stage 1; OQ-016; architecture §6.
- **Human decision:** Phase 11 recorded Java 21 + Boot 3.5.5. Phase 16 stopped, then 16A recorded PostgreSQL/H2 + JPA + UUID, then 16B implemented.
- **Current impact:** Versions and persistence match recorded decisions. Entries 002, 008, 009.

### AV-003 — Phase 14 prompt mislabeled T2/T5

- **AI suggestion/behavior:** Phase 14 prompt called T2 `OPEN→CANCELLED` and T5 `RESOLVED→CLOSED`.
- **Why it was questionable:** That contradicts `spec/state-machine.md` IDs (T2 is `IN_PROGRESS→RESOLVED`, T3 `RESOLVED→CLOSED`, T4 `OPEN→CANCELLED`).
- **Specification/reference:** `spec/state-machine.md` §3.
- **Human decision:** Tests kept spec IDs, not the prompt’s remapping. Entry 006.
- **Current impact:** Domain and API tests still use spec T1–T5. A passing suite following the prompt labels would have been a specification bug hidden by tests.

### AV-004 — Inventing keyword search so the list “works”

- **AI suggestion/behavior:** Implement LIKE/field/case matching in Phase 13/16.
- **Why it was questionable:** OQ-005/006/007 unresolved.
- **Specification/reference:** `spec/specification.md` search; `TicketKeywordSearch`.
- **Human decision:** Always throw OQ-005; API fail-closed 500. Entry 005, 010.
- **Current impact:** Search still deferred. UI search will show unexpected error. No matcher in code.

### AV-005 — Mislabeling JPA as OQ-017

- **AI suggestion/behavior:** Phase 16 text treated persistence technology as OQ-017.
- **Why it was questionable:** In this repo OQ-017 is Java 21 timing (already approved).
- **Specification/reference:** `spec/requirements.md` OQ list; Phase 16A prompt.
- **Human decision:** Record JPA/Hibernate as a decision, not OQ-017. Entry 008–009.
- **Current impact:** Java 21 was not reopened. JPA is in use as recorded.

### AV-006 — Passing tests hiding specification / environment problems

- **AI suggestion/behavior:** Treat 135 backend + 11 frontend greens as full acceptance.
- **Why it was questionable:** H2 HTTP round-trips are not PostgreSQL restart. Stubbed UI is not browser E2E. Deferred search returns 500 by design. VAL-002 has no tests.
- **Specification/reference:** `spec/test-strategy.md` restart/E2E; Phase 17 report §§11–14.
- **Human decision:** Phase 17 marked those BLOCKED / NOT RUN, FAIL count 0 only against **decided** behavior.
- **Current impact:** Same gaps remain. Phase 18 confirms they are not proven by the green suites.

### AV-007 — Hibernate default column length (this review)

- **AI suggestion/behavior:** JPA `String` fields without `length`/`text` (except description/content).
- **Why it was questionable:** OQ-015 forbids inventing max lengths. Default `VARCHAR(255)` is an implicit product limit and likely a 500 on overflow.
- **Specification/reference:** OQ-015; `spec/data-model.md` §2 title length deferred.
- **Human decision:** None yet. Discovered in Phase 18. Do not “fix” by picking a business max in Phase 19 without a human OQ-015 answer; prefer not inventing 255.
- **Current impact:** Untested titles/authors > 255 may fail at the database.

### AV-008 — Jackson ignore of create `status` (this review)

- **AI suggestion/behavior:** Omit `status` from `CreateTicketRequest` so stored create cannot be non-OPEN.
- **Why it was questionable:** That correctly protects REQ-013, but combined with default unknown-property ignore it **silently ignores** client `status` instead of leaving ignore-vs-reject (OQ-013) undecided at runtime.
- **Specification/reference:** `spec/api-contract.md` §4.1 OQ-013.
- **Human decision:** Not recorded. Phase 16 docs said ignore-vs-reject remains unanswered.
- **Current impact:** Create with `"status":"CLOSED"` is expected to 201 with stored `OPEN`. No test records that.

---

## Open Question Findings

Do not resolve. Classifications below are about **implementation vs documented deferral**.

| OQ | Documented status | Implementation | Review |
| --- | --- | --- | --- |
| OQ-001 priority set | Unresolved | Free-text stored; VAL-002 off | **Still correctly deferred** (stores strings; does not invent a set). Conflicts with data-model “do not persist free-text outside the set” until a human set exists. |
| OQ-002 create required besides title | Unresolved | Only title required | **Still correctly deferred** (title is specified required). |
| OQ-003 unassigned / empty assignee | Unresolved | Stored as supplied, including `""` from UI | **Still correctly deferred** |
| OQ-004 blank description | Unresolved | Stored as supplied | **Still correctly deferred** |
| OQ-005 search semantics | Unresolved | No matcher | **Still correctly deferred** |
| OQ-006 blank search | Unresolved | Fail-closed 500, not list-all/400 | **Still correctly deferred** (neither product answer) |
| OQ-007 search + filter | Unresolved | Fail-closed 500 | **Still correctly deferred** |
| OQ-008 terminal PATCH | Unresolved | Allowed by omission (no extra 409) | **Implementation behavior exists without a recorded rule.** Not a silent 409 product rule. **Needs human decision** if allow vs reject must be explicit. |
| OQ-009 terminal comments | Unresolved | Fail-closed 500 | **Still correctly deferred** |
| OQ-010 comment author | Unresolved | Request JSON; not auth | **Still correctly deferred** |
| OQ-011 comment order | Unresolved | No `@OrderColumn`; tests do not sort-assert | **Still correctly deferred** |
| OQ-012 same-status | Unresolved | Fail-closed 500; tests do not classify | **Still correctly deferred** |
| OQ-013 create status | Unresolved | Stored always OPEN; extra JSON `status` ignored | **Accidentally decided toward ignore** at runtime. Stored OPEN still matches REQ-013. |
| OQ-014 pagination/order | Unresolved | Unpaginated `findAll` | **Still correctly deferred** (params not added) |
| OQ-015 field lengths | Unresolved | Implicit VARCHAR(255) on several columns | **Accidentally decided by schema default** |
| OQ-016 PostgreSQL vs H2 | Resolved Phase 16A | Config matches | Do not reopen |
| Java 21 / Boot 3.5.5 | Approved Phase 11 | `pom.xml` | Do not reopen |
| JPA/Hibernate | Recorded 16A, not an OQ | In use | Do not reopen |
| UUID ids | Recorded 16A | In use | Do not reopen |
| OQ-018 frontend family | Unresolved | React 18 + Vite in `frontend/` | **Still documented open**; SPA choice is within allowed family, not a Next.js vs React product close |
| OQ-019 concurrent updates | Unresolved | No versioning | **Still correctly deferred.** Ordinary tests must not be treated as a concurrency proof. |

Resolved items not reopened: PostgreSQL runtime + H2 tests, JPA/Hibernate, UUID ticket ids, Java 21.

---

## Acceptance Gaps

Carried from `docs/acceptance-test-report.md` §14, with Phase 18 classification:

| Gap | Phase 18 classification |
| --- | --- |
| Browser/E2E not executed | **Environment limitation** |
| PostgreSQL runtime and process-restart ACs | **Environment limitation** |
| REQ-009 search matching (OQ-005/006/007) | **Expected/deferred** |
| VAL-002 priority set (OQ-001) | **Expected/deferred** |
| Terminal PATCH/comments and same-status POST (OQ-008/009/012) | **Expected/deferred** (OQ-008 currently allow-by-omission — **needs human decision** if that must be explicit) |
| UI unit tests omit successful create → `onCreated` | **Test gap** (code exists; not a proven defect) |
| No separately named API blank-author test | **Test gap** (rule implemented) |
| Workspace not a git repo | **Environment limitation** |
| Implicit VARCHAR(255) (new in Phase 18) | **Risk / accidental OQ-015**; not a Phase 17 FAIL |
| In-memory status filter vs architecture query (new) | **Architecture gap**; behavior still matches REQ-010 ACs on H2 |
| Create `status` silently ignored (new emphasis) | **Needs human decision** (OQ-013) |

No new **decided-rule defect** (FAIL-equivalent) was added beyond Phase 17’s FAIL count of 0.

---

## Recommended Fixes

For **Phase 19 only**. Do not implement in Phase 18. Do **not** resolve OQs in order to make these look closed.

### Should fix without answering OQs

1. **API tests:** unknown `PATCH /tickets/{id}` → 404 `NOT_FOUND`; unknown `POST /tickets/{id}/status` → 404; blank comment `author` → 400 `VALIDATION`.
2. **UI test:** successful create calls `onCreated` with the returned `id` (and does not navigate before the API resolves).
3. **Stop inventing VARCHAR(255):** map title, priority, assignee, and comment author to unbounded/`text` columns until OQ-015 is decided, so overflow is not a silent 500. Do **not** pick a business max length.
4. **Status filter in persistence:** implement `findByStatus` as a parameterized query (or equivalent) and use it from `TicketService.list` when only `status` is present. Keep VAL-005 in the domain. Do not add search SQL.
5. **List without comment N+1:** do not initialize comment collections when mapping list summaries; load comments on get-by-id (e.g. entity graph).
6. **Replace message-prefix 404 mapping** in `RestExceptionHandler.illegalArgument` with an explicit identity/not-found path (invalid UUID already 404 via adapter).

### Do not “fix” without a human OQ answer

7. Do not implement keyword matching, blank-search list-all/400, or combined search+filter.
8. Do not add extra 409 for terminal PATCH (OQ-008) or classify same-status as 200 vs 409 (OQ-012).
9. Do not change terminal comments from fail-closed to allow/deny (OQ-009).
10. Do not add `@Version` / locking and claim OQ-019 solved.
11. Do not add VAL-002 priority enums.
12. Do not add authentication, pagination params, or Flyway unless a later approved phase records them.
13. Ask a human whether create-body `status` should be **ignored** (current) or **rejected** (OQ-013) before changing create DTO behavior.

### Environment / later verification (not code defects)

14. Browser smoke/E2E against a running API.
15. PostgreSQL process restart persistence (distinct from H2 HTTP tests).

---

## Review method

- Read `PLAN.md`, `spec/*`, `docs/acceptance-test-report.md`, `docs/traceability.md`, `docs/ai-validation.md`, `docs/prompt-history.md`, `.cursor/rules/*`.
- Inspect domain, application, API, persistence, frontend, tests, `pom.xml`, configuration, `.gitignore`.
- Did not modify production or test code.
- Did not re-run `mvn test` / `npm test` in this phase.
- Did not start Phase 19 or Phase 20.
