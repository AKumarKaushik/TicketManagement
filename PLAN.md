# PLAN.md

Spec-Driven Development plan for the existing TicketManagement repository.

This plan does not authorize skipping phases. Do not implement Support Ticket business functionality until Phase 11 (and later implementation phases) are explicitly started.

## Constraints (all phases)

- Do not delete, replace, regenerate, or restructure the existing project.
- Do not modify `src/main/java/com/enterprise/ai/Main.java` unless a later approved phase requires a controlled entry-point change.
- Java 21 and Spring Boot 3.5.5 are recorded (Phase 11). Persistence is recorded (Phase 16A) and implemented (Phase 16): PostgreSQL for development/runtime, H2 for automated tests, Spring Data JPA / Hibernate, system-generated UUID ticket ids. Do not change those versions.
- Do not modify `pom.xml` unless absolutely necessary for the active phase.
- Do not invent specification details before the matching specification phase.
- Never claim tests passed unless they were actually run.

## Phase status

| Phase | Name | Status |
|---|---|---|
| 1 | Existing project assessment | Complete (2026-09-17) |
| 2 | Requirements | Complete (2026-09-17) |
| 3 | Specification | Complete (2026-09-21) |
| 4 | Architecture | Complete (2026-09-21) |
| 5 | Data model | Complete (2026-09-21) |
| 6 | API contract | Complete (2026-09-21) |
| 7 | State machine | Complete (2026-09-21) |
| 8 | UI flow | Complete (2026-09-21) |
| 9 | Test strategy | Complete (2026-09-21) |
| 10 | Implementation plan | Complete (2026-09-21) |
| 11 | Backend implementation | Complete (2026-09-21) — foundation only |
| 12 | Domain model and business rules | Complete (2026-09-21) |
| 13 | Comments/search/filter | Complete (2026-09-21) |
| 14 | Backend testing | Complete (2026-09-21) — domain suite |
| 15 | Frontend | Complete (2026-09-21) — UI only, not integrated |
| 16 | Integration | Complete (2026-09-21) |
| 17 | Acceptance testing | Complete (2026-09-21) — report in `docs/acceptance-test-report.md` |
| 18 | AI review | Complete (2026-09-21) — report in `docs/phase-18-review.md` |
| 19 | Fixes | Complete (2026-09-21) — report in `docs/phase-19-fixes.md` |
| 20 | Final documentation | Complete (2026-09-21) — `docs/final-project-status.md`, `docs/README.md` |

## Completed vs not yet runtime-verified

### Completed (SDD phases 1–20)

Requirements, specification, architecture, data model, API contract, state machine, UI flow, test strategy, implementation plan, backend implementation, frontend implementation, automated testing (latest run: Phase 19), acceptance testing as an activity (H2/jsdom evidence), AI/code/spec review, Phase 19 technical fixes, Phase 20 documentation.

### Not yet runtime-verified (intentionally deferred)

- Live browser testing
- Live UI ↔ backend testing
- PostgreSQL runtime verification
- PostgreSQL restart / persistence-after-restart verification
- Complete manual end-to-end journeys

Do **not** mark these as passed. Automated tests passed ≠ manual runtime testing passed.

---

## Phase 1 - Existing project assessment

**Status:** Complete.

Inspected the repository without changing application source.

Findings to carry forward:

- Maven project `com.enterprise.ai:TicketManagement:1.0-SNAPSHOT`
- Java **17** (`maven.compiler.source/target` and IntelliJ `JDK_17` / `temurin-17`)
- **No Spring Boot parent, plugin, or dependencies**
- No declared Maven dependencies
- Single source file: `src/main/java/com/enterprise/ai/Main.java` (Hello World)
- `src/main/resources` is not present as a source tree
- `src/test/java` is not present
- Standard Maven/IDE `.gitignore`

Governance structure created in this phase. Stopped before requirements analysis.

## Phase 2 - Requirements

**Status:** Complete.

Populated `spec/requirements.md` with REQ-001–REQ-050 (functional, business rules, validation, error handling, persistence, non-functional). Review recorded in `docs/requirements-review.md`. Open questions were listed, not answered.

Did not implement application code, modify `pom.xml`, add dependencies, or start architecture.

## Phase 3 - Specification

**Status:** Complete.

Created `spec/specification.md` as an implementation-independent behavioral specification covering tickets, comments, search/filter, validation, errors, persistence, lifecycle, UI-observable behavior, NFRs, security, and OQ-001–OQ-019. REQ-001–REQ-050 are traced to specification sections.

Did not implement application code, modify `pom.xml` or `Main.java`, design REST endpoints or schema, or start Phase 4 Architecture.

## Phase 4 - Architecture

**Status:** Complete.

Populated `spec/architecture.md` with the **target** architecture (system boundaries, UI/backend split, layered backend, dependency direction, persistence/validation/lifecycle/search/comment ownership, security, logging, testing boundaries, technology decisions, deferred OQs). Existing Java 17 Maven repository was not changed.

Did not implement code, modify `pom.xml` or `Main.java`, add dependencies, design schema or REST paths, or start Phase 5.

## Phase 5 - Data model

**Status:** Complete.

Populated `spec/data-model.md` with the logical Ticket and Comment model (attributes, optionality, identity, status/priority/assignee representation, relationships, validation/integrity, persistence, terminal-state considerations). OQ-001–OQ-019 remain unanswered. No entities, SQL, tables, or repositories were created.

Did not implement code, modify `pom.xml` or `Main.java`, or start Phase 6.

## Phase 6 - API contract

**Status:** Complete.

Populated `spec/api-contract.md` with REST paths, methods, JSON representations, HTTP statuses, and four error classes. OQ-001–OQ-019 remain unanswered where they affect the API. No controllers, DTOs, or Java code were created.

Did not implement endpoints, modify `pom.xml` or `Main.java`, or start Phase 7.

## Phase 7 - State machine

**Status:** Complete.

Populated `spec/state-machine.md` with five statuses, initial `OPEN`, valid transitions T1–T5, the full invalid pair table, terminal `CLOSED`/`CANCELLED`, reject/persist rules, API mapping, and required integration-test scenarios. OQ-012 (same-status) and related OQs remain deferred. No workflow code or tests were created.

Did not implement Java, modify `pom.xml` or `Main.java`, or start Phase 8.

## Phase 8 - UI flow

**Status:** Complete.

Populated `spec/ui-flow.md` with views, navigation, create/list/detail/edit/comment/status/search/filter flows, error/loading/empty states, API and state-machine interaction, and deferred OQs. No frontend code was created.

Did not implement React/Next.js, modify `pom.xml` or `Main.java`, or start Phase 9.

## Phase 9 - Test strategy

**Status:** Complete.

Populated `spec/test-strategy.md` with test levels, state-machine coverage (all valid transitions, representative invalid, terminal, unchanged store, initial OPEN, OQ-012 deferred), validation/search/comment/UI/E2E/restart, traceability, and OQ-blocked tests. No test classes or runs.

Did not implement tests, modify `pom.xml` or `Main.java`, or start Phase 10.

## Phase 10 - Implementation plan

**Status:** Complete.

Created `spec/implementation-plan.md` with 14 stages (foundation through final docs), Java 21/Spring Boot as a **later** Stage 1 task requiring human validation, OQ blockers, and REQ mapping. No product code, `pom.xml`, or `Main.java` changes.

Did not start Phase 11.

## Phase 11 - Backend implementation

**Status:** Complete — **foundation only** (implementation-plan Stage 1).

Human-approved: Java **21**, Spring Boot **3.5.5**, new Boot entry point, keep `Main.java`.

Done:

- `pom.xml`: parent `spring-boot-starter-parent` 3.5.5, `java.version` 21, `spring-boot-starter-web` only
- `TicketManagementApplication` as Boot entry; `spring-boot-maven-plugin` `mainClass` set
- `Main.java` unchanged
- `src/main/resources/application.properties` with `spring.application.name` only (no datasource, no secrets)
- Empty layer packages: `domain`, `application`, `persistence`, `api` (`package-info.java` only)

Not done (later phases): persistence, REST, comments, search/filter, frontend. Domain and lifecycle were added in Phase 12. Comments and status filter were added in Phase 13; keyword matching remains deferred (OQ-005).

Deferred at the time of Phase 11: PostgreSQL vs H2, persistence technology, ID format, frontend. **Phase 16A later recorded** the database split, JPA/Hibernate, and UUID identity. OQ-018 remains open.

Maven compile/package succeeded with Java 21. First `spring-boot:run` failed (port 8080 in use). Retry with `--server.port=0` started successfully (Tomcat ephemeral port; process then stopped). No port committed in config.

## Phase 12 - Domain model and business rules

**Status:** Complete.

Implemented the Ticket domain and approved lifecycle in `com.enterprise.ai.domain` (no Spring/HTTP/JPA).

Done:

- Ticket fields: identity (`TicketId` opaque value supplied by caller), title, description, priority, assignee, status
- Statuses: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`
- Create always `OPEN`; T1–T5 allowed; §4 invalid pairs rejected as `IllegalStatusTransitionException`; status unchanged on reject
- Terminal `CLOSED` / `CANCELLED`
- VAL-001 title present/non-blank; VAL-005 unknown status token is validation, not a transition
- Domain unit tests only (`TicketTest`, `TicketStatusTransitionTest`)
- `junit-jupiter` test scope in `pom.xml` so those tests can run
- `Main.java` unchanged

Not done: persistence, REST, DTOs, frontend. Comments and status filter were added in Phase 13.

Deferred at the time of Phase 12: OQ-001 priority set (VAL-002 not enforced), OQ-002–004 optionality, OQ-012 same-status (not classified as pass or fail), OQ-008/009 terminal field/comment guards. Identity **format** and PostgreSQL vs H2 were still open then; **Phase 16A later recorded** UUID identity and the PostgreSQL/H2 split.

`mvn test` ran 2026-09-21T13:27:48+05:30: Tests run: 43, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS.

## Phase 13 - Comments/search/filter

**Status:** Complete — comments and status filter in the domain; **keyword matching not implemented** (OQ-005).

Done:

- `Comment`: content, author, system `creationTime` (`Instant` supplied by caller), associated `TicketId`
- `Ticket.addComment` with VAL-003 / VAL-004; comment cannot be created except on a ticket; comments of ticket A are not on ticket B; no edit/delete
- Terminal comments (`CLOSED`/`CANCELLED`): `OpenQuestionDeferredException` OQ-009 — not allowed and not rejected as a product rule
- `TicketStatusFilter`: exact match on the five statuses; unknown token VAL-005; empty match list is success
- `TicketKeywordSearch.matching`: deferred boundary only (always OQ-005). No field/case/partial rule invented
- Domain tests: `CommentTest`, `TicketStatusFilterTest`. No search-match assertions. No OQ-009 pass/fail tests
- `Main.java` unchanged; no REST/JPA/frontend

`mvn test` ran 2026-09-21T13:33:26+05:30: Tests run: 75, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS (includes Phase 12 suite).

## Phase 14 - Backend testing

**Status:** Complete — domain/unit suite for implemented Phases 11–13 behavior. API, persistence, restart, UI, and E2E tests were **not** added because those layers do not exist yet.

Coverage executed:

- Lifecycle: initial `OPEN`; spec T1–T5; all 15 invalid pairs; terminal `CLOSED`/`CANCELLED`; status unchanged on reject; illegal pair is `ILLEGAL_TRANSITION` not VAL-005
- Validation: missing/blank/whitespace/valid title (VAL-001); invalid status tokens (VAL-005). No length or priority-set tests (OQ-015, OQ-001)
- Comments: valid create; blank/whitespace content and author; ticket association; no cross-ticket visibility. No OQ-009 or edit/delete tests
- Status filter: each of five statuses; unknown/blank/wrong-case VAL-005; empty result success; no mutation; no OQ-007 combo
- Search: `TicketKeywordSearchTest` asserts OQ-005 deferred boundary only — **not** a matching implementation
- Regression: Phase 12 and 13 tests kept

Not run (no implementation): controller/API, repository/JPA, process-restart persistence, UI, E2E.

`mvn test` ran 2026-09-21T13:38:00+05:30: Tests run: 90, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS.

## Phase 15 - Frontend

**Status:** Complete — React + Vite SPA in `frontend/`. **Not** wired to the Spring Boot process (Phase 16).

Technology: React 18.3.1 + Vite 6.3.4 (assignment-permitted React/equivalent SPA). Location: `frontend/` beside the existing Maven tree. Java/`pom.xml`/`Main.java` unchanged.

Views: V-LIST, V-CREATE, V-DETAIL, V-NOT-FOUND with loading/empty/error. Create has no status picker. Status buttons follow T1–T5 only; terminal offers none. Search input present without matching rules. Filter uses the five statuses. HTTP client `createHttpTicketApi` matches `spec/api-contract.md` paths; no mock backend.

`npm test` (frontend): 11 passed (2026-09-21). `npm run build`: succeeded (vite production build).

## Phase 16 - Integration

**Status:** Complete.

### Phase 16A — Recorded human decisions (2026-09-21)

Documentation first. OQ-016, Spring Data JPA / Hibernate (not OQ-017), and UUID ticket identity were recorded before implementation.

### Phase 16B — Implementation (2026-09-21)

End-to-end path: React/Vite HTTP client → REST → application service → domain → JPA → H2 (tests) / PostgreSQL (runtime config).

Done:

- Maven: `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, PostgreSQL runtime, H2 test, `spring-boot-starter-test`
- JPA `TicketEntity` / `CommentEntity`; comments have a technical persistence UUID not exposed on the API
- Application `TicketService` orchestrates create/list/get/update/comment/status/filter; lifecycle stays in `TicketLifecycle`
- REST per `spec/api-contract.md`; error envelope `VALIDATION` / `NOT_FOUND` / `ILLEGAL_TRANSITION` / `UNEXPECTED`
- CORS limited to local Vite origins
- Frontend `VITE_API_BASE_URL` in `frontend/.env.development` (`http://localhost:8080`); existing `createHttpTicketApi` unchanged in paths
- `Main.java` unchanged; Java 21 / Spring Boot 3.5.5 unchanged

Not done / not claimed:

- Keyword search matching (OQ-005/006/007)
- PostgreSQL process-restart durability (H2 HTTP integration only)
- Browser/E2E against a live PostgreSQL instance
- Phase 17 acceptance testing

Tests run: `mvn test` — Tests run: 135, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS (Finished at 2026-09-21T14:07:03+05:30). `cd frontend && npm test` — 11 passed. `npm run build` — succeeded.

## Phase 17 - Acceptance testing

**Status:** Complete (activity documented). Production code was not changed.

Evidence: `docs/acceptance-test-report.md`.

Re-ran automated suites (2026-09-21T14:22:37+05:30 backend; frontend tests/build same session):

- `mvn test`: Tests run: 135, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS
- `cd frontend && npm test`: 11 passed
- `cd frontend && npm run build`: succeeded

Browser/E2E: not run. PostgreSQL runtime/restart: not run. REQ FAIL count: 0. Several Must items remain BLOCKED (search OQs, restart ACs, live UI).

Phase 18 was not started as part of Phase 17.

## Phase 18 - AI review

**Status:** Complete (review only; no production or test code changes).

Evidence: `docs/phase-18-review.md`.

Inspected specs, architecture, implementation, tests, acceptance report, AI validation, and rules. Did not re-run automated suites in this phase (last run remains Phase 17).

Findings (factual, no score): decided lifecycle/API/error classes largely compliant; search still deferred; no FAIL-equivalent against decided requirements. Architecture gap: status filter executed in memory after `findAll()`. Risks: implicit `VARCHAR(255)` (OQ-015), create-body `status` ignored (OQ-013), terminal PATCH allowed by omission (OQ-008), N+1 comment load on list, deferred OQs mapped to `500 UNEXPECTED`. Test gaps: PATCH/status 404, blank comment author API, create success navigation. Browser/E2E and PostgreSQL restart remain unexecuted.

Phase 19/20 were not started. OQs were not resolved.

## Phase 19 - Fixes

**Status:** Complete (2026-09-21). Report: `docs/phase-19-fixes.md`.

Technical fixes only. OQs not resolved. `Main.java` unchanged. Specs not rewritten.

Done:

- Status filter via `TicketJpaRepository.findByStatus` (not in-memory `findAll`)
- List/filter mapping does not initialize comments; detail uses entity graph `findWithCommentsById`
- Text columns for title, priority, assignee, comment author (OQ-015 still open; no `@Size`)
- 404 only via `TicketNotFoundException` (removed IAE message-prefix mapping)
- API tests: unknown PATCH/status 404, comment author VAL-004, create-body status cannot store non-OPEN, title > 255 persists, filter non-mutation
- UI tests: create `onCreated` after backend success; App navigates to detail only then
- Deferred OQ → `500 UNEXPECTED` left unchanged

Tests: `mvn test` — Tests run: 149, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS (Finished at 2026-09-21T15:45:32+05:30). `cd frontend && npm test` — 13 passed. `npm run build` — succeeded.

Phase 20 was not started during Phase 19.

## Phase 20 - Final documentation

**Status:** Complete (2026-09-21). Documentation only. No production, test, or specification behavior changes.

Created: `docs/README.md`, `docs/final-project-status.md`, `docs/phase-20-final-documentation.md`.

Updated: this file, `docs/traceability.md`, `docs/ai-validation.md`, `docs/prompt-history.md`.

Runtime UI / PostgreSQL verification was not started.

---

## Next allowed action

Phase 20 documentation is complete. The next **runtime** activity (not an SDD implementation phase) is live UI + backend + PostgreSQL verification.

Do not invent remaining OQs. Do not treat H2 HTTP persistence as PostgreSQL restart proof. Do not claim browser/E2E passed.
