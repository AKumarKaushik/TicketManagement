# AI Validation Log

Record AI mistakes found during the SDD exercise so they are not repeated.

AI was used as an engineering assistant. Requirements, technology decisions, unresolved OQs, acceptance, and “tests passed” claims remained human-controlled via phase prompts and recorded evidence.

## Human control (how to read this log)

| Theme | What happened | Entry |
|---|---|---|
| Inspect before assuming | Prompt said the repo was already Spring Boot; it was Java 17 Hello World | 001 |
| Do not invent versions | Boot/DB not added until humans recorded Java 21, Boot 3.5.5, then OQ-016/JPA/UUID | 002, 003, 008, 009 |
| Do not invent OQs | Search matching, VAL-002, terminal PATCH/comments, same-status left deferred | 005, 010 |
| Do not trust prompt labels over spec | Phase 14 T2/T5 labels vs `spec/state-machine.md`; tests kept spec IDs | 006 |
| Stop when blocked | Phase 16 halted until persistence decisions existed | 008, 009 |
| Tests are evidence, not runtime proof | Green H2/jsdom suites ≠ PostgreSQL restart or browser E2E | 010, 011 |
| Review then fix | Phase 18 recorded defects; Phase 19 fixed technical issues without resolving OQs | 011, 012 |
| Comment edit/delete | Out of scope in the API contract / requirements review; not added as extra APIs | 005; `spec/api-contract.md` |

Do not treat this log as a score. Do not invent extra AI mistakes.

## Entry template

### Entry NNN

- **Date:**
- **Phase:**
- **AI mistake:** What the model did or claimed
- **Why it was wrong:** The spec, rule, or fact it violated
- **How it was detected:** Review, test, compilation, or human inspection
- **Correction:** What was changed
- **Regression test:** Test name or command that would fail if the mistake returned

---

## Entries

### Entry 001 — Process finding (Phase 1)

- **Date:** 2026-09-17 (recorded 2026-09-21)
- **Phase:** 1
- **AI mistake:** The first prompt assumed the repository already contained Spring Boot.
- **Why it was wrong:** Inspection showed a Maven Java 17 Hello World with no Spring Boot parent or dependencies.
- **How it was detected:** Reading `pom.xml` and `Main.java`.
- **Correction:** Governance treated Spring Boot as a later target, not as present code.
- **Regression test:** N/A (process). Re-inspect `pom.xml` before claiming Spring Boot exists.

### Entry 002 — Prevented guess (Phase 11)

- **Date:** 2026-09-21
- **Phase:** 11 Backend Foundation
- **AI mistake (avoided):** Choosing an unpublished Spring Boot version (for example 3.4.x / 3.5.x) and adding it to `pom.xml` so the app would “start.”
- **Why it was wrong:** Phase 10 and this phase forbid silently inventing Spring Boot version, DB product, or persistence tech. No spec records a Boot version.
- **How it was detected:** Pre-change review of `spec/implementation-plan.md` Stage 1 and OQ-016 / OQ-017.
- **Correction:** Did not modify `pom.xml` or `Main.java`. Reported the blocker instead.
- **Regression test:** N/A until a human records the Boot version in specs or this prompt thread; then foundation may proceed.

### Entry 003 — Human-approved foundation (Phase 11)

- **Date:** 2026-09-21
- **Phase:** 11 Backend Foundation
- **Decision (human, not AI-invented):** Java 21; Spring Boot 3.5.5; add `TicketManagementApplication`; keep `Main.java`.
- **AI-adjacent choice (documented, not an OQ answer):** `spring-boot-starter-web` only so the process can start an HTTP runtime for the future REST API. No `data-jpa`, H2, or PostgreSQL.
- **AI-adjacent choice:** `spring-boot-maven-plugin` `mainClass` = `com.enterprise.ai.TicketManagementApplication` because `Main.java` also has `main`.
- **Validation issue:** Default port 8080 was already in use. First `spring-boot:run` failed. Retry used `--server.port=0` (not written to `application.properties`).
- **Correction:** Did not commit a product port or a database URL.
- **Still deferred:** OQ-016 DB, persistence technology, ID format, OQ-018 frontend.
- **Regression test:** N/A (foundation). Re-run compile with JDK 21; do not add datasource properties until OQ-016 is decided.

### Entry 004 — Domain choices that are not OQ answers (Phase 12)

- **Date:** 2026-09-21
- **Phase:** 12 Domain model and business rules
- **AI-adjacent choice:** `TicketId` is an opaque non-blank string supplied by the caller. Generation and format (UUID vs numeric, etc.) were **not** decided.
- **AI-adjacent choice:** Priority and assignee are stored as `String` with no recognized-set check. VAL-002 remains unimplemented because OQ-001 is unanswered.
- **AI-adjacent choice:** Same-status `changeStatus` raises `OpenQuestionDeferredException` (OQ-012). It is neither T1–T5 nor an illegal pair. No test asserts success, no-op, or `ILLEGAL_TRANSITION` for same-status.
- **AI-adjacent choice:** `IllegalStatusTransitionException.ERROR_CODE` is `ILLEGAL_TRANSITION` as the **business** error name from REQ-028. Domain code does not map HTTP 409.
- **AI-adjacent choice:** `junit-jupiter` added with `test` scope so mandated domain tests can compile and run. No Spring Test, JPA, or web test slices.
- **Not implemented (this phase instruction):** Comment types and VAL-003/VAL-004, despite implementation-plan Stage 2 mentioning comments.
- **Still deferred:** OQ-001–OQ-016, OQ-018, OQ-019, identity format, persistence, API.
- **Regression test:** `mvn test` — `TicketStatusTransitionTest` and `TicketTest` (ran, 43 tests, 0 failures).

### Entry 005 — Comments / search / filter boundaries (Phase 13)

- **Date:** 2026-09-21
- **Phase:** 13 Comments, search, and filter
- **Search (OQ-005):** No matching algorithm. `TicketKeywordSearch.matching` always raises `OpenQuestionDeferredException` OQ-005. OQ-006 blank keyword and OQ-007 combined filter were not implemented. Search is **not** complete.
- **Terminal comments (OQ-009):** `Ticket.addComment` on `CLOSED`/`CANCELLED` raises `OpenQuestionDeferredException` OQ-009. Not a silent allow and not `VALIDATION` / lifecycle reject. No pass/fail tests for that path.
- **Comment identity:** No surrogate `CommentId`. Spec says technical identity is deferred and must not appear as a ticket business field.
- **Comment order (OQ-011):** In-memory list is only a holding structure. Display order was not specified; tests do not assert sort.
- **Creation time:** Caller supplies `Instant` (system clock later). Precision/timezone policy was not invented beyond `Instant`.
- **Author:** Stored required string (VAL-004). OQ-010 UX was not invented.
- **Status filter:** Exact equality on the five `TicketStatus` tokens; unknown token VAL-005. In-memory helper, not a repository.
- **Still deferred:** OQ-001–OQ-007, OQ-008–OQ-016, OQ-018, OQ-019, identity format, persistence, API.
- **Regression test:** `mvn test` — 75 tests, 0 failures (includes Phase 12).

### Entry 006 — Backend testing decisions (Phase 14)

- **Date:** 2026-09-21
- **Phase:** 14 Backend testing
- **T1–T5 numbering:** The Phase 14 prompt labeled T2 as `OPEN→CANCELLED` and T5 as `RESOLVED→CLOSED`. Tests keep **`spec/state-machine.md` IDs** (T1 `OPEN→IN_PROGRESS`, T2 `IN_PROGRESS→RESOLVED`, T3 `RESOLVED→CLOSED`, T4 `OPEN→CANCELLED`, T5 `IN_PROGRESS→CANCELLED`). All five approved pairs are covered.
- **Wrong-case status:** Exact tokens are required (`TicketStatus.fromToken` / VAL-005). Tests assert `"open"` is rejected; this is not a new product rule.
- **Search tests:** `TicketKeywordSearchTest` only asserts OQ-005 is still deferred. No title/description/comment/assignee/partial/case assertions.
- **Not executed:** API/controller, repository, restart persistence, UI, E2E — those layers are not implemented. Creating them would have been Phase 14 scope creep.
- **Defects found:** None in production code. A broken test-file edit during this phase was repaired before the suite run (restored `t1_openToInProgress`, `ticketId_rejectsBlankValue`, `filter_doesNotMutateTickets`).
- **Still deferred:** OQ-001–OQ-016, OQ-018, OQ-019; identity format; persistence; API; frontend.
- **Regression test:** `mvn test` — 90 tests, 0 failures.

### Entry 007 — Frontend stack and OQ boundaries (Phase 15)

- **Date:** 2026-09-21
- **Phase:** 15 Frontend
- **Stack:** React 18 + Vite, directory `frontend/`. The Phase 15 prompt permitted React / Next.js / equivalent. This does **not** close OQ-018 as a product-wide Next.js vs React decision beyond this SPA choice. Next.js was not used.
- **No mock backend:** `createHttpTicketApi` calls the contract paths. Tests inject a `TicketApi` stub. Live integration is Phase 16.
- **Verified not invented:** no create status selector; next-status buttons are T1–T5 only; search has no match algorithm (blank search is not submitted — OQ-006 not answered as list-all or 400); priority is free text (OQ-001); comment form is not hidden/forced on terminal (OQ-009); no same-status control (OQ-012); no auth.
- **Search vs filter:** last applied list mode is either all, status-only, or keyword-only. Combined query params are not sent (OQ-007).
- **Still deferred:** remaining OQs including OQ-001, OQ-005–007, OQ-008–010, OQ-012, OQ-014, OQ-018 exact stack family, persistence, REST.
- **Regression test:** `cd frontend && npm test` — 11 passed.

Backend REST was not added.

### Entry 008 — Prevented persistence guess (Phase 16)

- **Date:** 2026-09-21
- **Phase:** 16 Integration
- **AI mistake (avoided):** Adding H2 or PostgreSQL, JPA or JDBC, and REST so the UI could “talk to a backend.”
- **Why it was wrong:** OQ-016 is unanswered. Architecture defers access style (JPA vs other). Data-model forbids in-memory-only durability. Identity format is unspecified. The Phase 16 prompt forbids silently resolving those.
- **How it was detected:** Pre-change review of `spec/requirements.md` OQ-016, `spec/architecture.md` §6, `spec/data-model.md` §7, `spec/implementation-plan.md` Stage 3.
- **Correction:** Did not modify `pom.xml`, domain, controllers, or frontend API base URL. Reported the blocker.
- **Note:** The Phase 16 prompt labeled “OQ-017 — persistence technology.” In this repo **OQ-017 is Java 21 timing** (already approved). Persistence access style has **no OQ number**.
- **Regression test:** N/A until a human records OQ-016, access style, and id format; then integration may proceed.

No REST or database adapter was generated.

### Entry 009 — Decision recording only (Phase 16A)

- **Date:** 2026-09-21
- **Phase:** 16A Record human decisions
- **AI mistake (avoided):** Implementing JPA dependencies, entities, repositories, database configuration, controllers, REST, or frontend wiring while recording approved decisions; labeling Spring Data JPA / Hibernate as OQ-017.
- **Why it was wrong:** Phase 16A is documentation-only. OQ-017 is Java 21 timing, already approved in Phase 11. Remaining OQs must stay open. Phase 16 must not be marked complete without implementation.
- **How it was detected:** Phase 16A prompt constraints vs repository (no persistence code requested).
- **Correction:** Specs and governance docs only. Recorded OQ-016 (PostgreSQL development/runtime; H2 automated tests), Spring Data JPA / Hibernate (not an OQ), and system-generated UUID ticket identity (unique; API string). Did not close OQ-001, OQ-005–OQ-009, OQ-011–OQ-015, OQ-018, OQ-019.
- **Regression test:** N/A (no implementation). Confirm `pom.xml`, Java sources, and `frontend/` are unchanged.

No REST or database adapter was generated.

### Entry 010 — Phase 16 integration review

- **Date:** 2026-09-21
- **Phase:** 16 Integration
- **AI mistake (avoided):** Implementing keyword search matching (LIKE/fields/case) so list search would “work.”
- **Why it was wrong:** OQ-005/006/007 remain unresolved. `TicketKeywordSearch` must stay a deferred boundary.
- **How it was detected:** Pre-implementation review of `TicketKeywordSearch` and the Phase 16 prompt §11.
- **Correction:** `GET /tickets?keyword=` raises `OpenQuestionDeferredException` and maps to `500 UNEXPECTED`. No matcher was added.
- **Regression test:** `TicketApiIntegrationTest.search_isNotImplemented`

Additional review (not product OQ answers):

- **OQ-013:** Create DTO does not bind `status`. Stored create status is always `OPEN` via `Ticket.create`. Ignore-vs-reject of a supplied status is still unanswered.
- **OQ-008:** No extra 409 for PATCH on `CLOSED`/`CANCELLED`. Domain field mutators were not given a new terminal policy. Terminal PATCH is untested.
- **OQ-009 / OQ-012:** Domain still throws `OpenQuestionDeferredException`. REST maps that to `500 UNEXPECTED` (fail-closed), not allow/deny as a product rule. Not tested as 409/200.
- **OQ-010:** Comment `author` remains request JSON. This is not an authentication design.
- **OQ-011 / OQ-014 / OQ-015 / OQ-019:** No display-order rule, pagination, length limits, or concurrency control was added.
- **Lifecycle:** Only `TicketLifecycle` classifies T1–T5 vs illegal pairs. Controller/service do not copy the table.
- **Persistence before validation:** Create/update/comment/status run domain rules before `save`. Invalid transitions leave stored status unchanged (API tests).
- **Secrets:** Datasource username/password are environment placeholders. `.gitignore` covers local env files. Frontend `.env.development` contains only `VITE_API_BASE_URL=http://localhost:8080`.
- **CORS:** Allowed origins are local Vite (`localhost` / `127.0.0.1:5173`), not `*`.
- **Comment technical id:** JPA UUID on `CommentEntity` is not in the API JSON.
- **PostgreSQL restart:** Not claimed. Tests used H2.
- **Browser/E2E:** Not performed (no browser tooling in this session).

- **Regression test (suite):** `mvn test` — Tests run: 135, Failures: 0, Errors: 0, Skipped: 0, BUILD SUCCESS (Finished at 2026-09-21T14:07:03+05:30)

### Entry 011 — Phase 18 review (no production changes)

- **Date:** 2026-09-21
- **Phase:** 18 AI / code / specification review
- **AI mistake:** Hibernate default `VARCHAR(255)` on title, priority, assignee, and comment author invents a length while OQ-015 is unanswered. Overflow would likely be `500 UNEXPECTED`, not VAL-*.
- **Why it was wrong:** Specs forbid inventing max lengths. Data-model marks length as OQ-015.
- **How it was detected:** Phase 18 inspection of `TicketEntity` / `CommentEntity` vs OQ-015.
- **Correction:** Not applied in Phase 18 (review only). Recommended Phase 19: use unbounded/`text` columns until a human decides lengths. Do not pick a business max.
- **Regression test:** None yet. After a Phase 19 schema change, add a test that a title longer than 255 is not rejected as an invented VAL unless OQ-015 is decided.

Additional Phase 18 review (not product OQ answers):

- **OQ-013:** Create DTO still omits `status` (stored create remains `OPEN`). Jackson unknown-property ignore means a supplied `status` is dropped rather than rejected. Ignore-vs-reject is still a human decision.
- **OQ-008:** Terminal PATCH still has no extra 409; field updates succeed by omission. Untested.
- **OQ-005/006/007/009/012:** Still fail-closed via `OpenQuestionDeferredException` → `500 UNEXPECTED`. No matcher. Same-status still not 200/409.
- **OQ-019:** No `@Version`. Green tests are not a concurrency proof.
- **Architecture:** `GET /tickets?status=` filters in memory after `findAll()`. `TicketRepository.findByStatus` is unused by the service and also in-memory. Recommended Phase 19 parameterized query for filter only.
- **Tests:** Passing 135/11 suites do not prove PostgreSQL restart, browser E2E, PATCH/status 404, blank comment author API, or create success navigation.
- **Historical (already logged):** Phase 1 Boot assumption (Entry 001); Boot/DB invention avoided (002, 008, 009); Phase 14 T2/T5 prompt mislabels vs spec IDs (006); search matching avoided (005, 010).
- **Regression test:** N/A this phase (no code change). Full write-up: `docs/phase-18-review.md`.

### Entry 012 — Phase 19 technical corrections

- **Date:** 2026-09-21
- **Phase:** 19 Fixes
- **AI mistake (corrected):** Status filter used `findAll()` plus in-memory `TicketStatusFilter`; list mapping initialized comments (N+1 risk); JPA default `VARCHAR(255)` on unspecified-length fields; `IllegalArgumentException` message prefix mapped to 404.
- **Why it was wrong:** Architecture places filter execution in persistence; list JSON has no comments; OQ-015 forbids inventing a 255 limit; 404 must be `TicketNotFoundException`, not string matching.
- **How it was detected:** Phase 18 review (`docs/phase-18-review.md` ARCH-005, ARCH-006, SPEC-014, API-006).
- **Correction:** `findByStatus` query; `toListItem` without comments and `findWithCommentsById` entity graph; text columns; 404 handler only for `TicketNotFoundException`. Deferred OQ → 500 left unchanged (would invent an API contract). OQs not resolved.
- **Regression test:** `mvn test` — `TicketPersistenceQueryTest`; `TicketApiIntegrationTest.filter_*`, `update_unknownTicket_returnsNotFound`, `changeStatus_unknownTicket_returnsNotFound`, `comment_rejects*Author`, `create_titleLongerThan255_isPersisted`, `create_clientSuppliedStatus_doesNotStoreNonOpenStatus`. Frontend: create success / App navigation in `ui.test.tsx`. Suite: Tests run: 149, Failures: 0, Errors: 0 (Finished at 2026-09-21T15:45:32+05:30)

### Entry 013 — Phase 20 documentation only

- **Date:** 2026-09-21
- **Phase:** 20 Final documentation
- **AI mistake (avoided):** Presenting automated results as live browser or PostgreSQL restart success; resolving remaining OQs in “final” docs; changing production code during documentation.
- **Why it was wrong:** Phase 20 is documentation. Runtime verification is scheduled separately. OQs stay open.
- **How it was detected:** Phase 20 prompt constraints vs Phase 17/19 evidence.
- **Correction:** Status docs distinguish PASS / BLOCKED / NOT YET RUNTIME-VERIFIED. Implementation trees were not modified.
- **Regression test:** N/A (docs). Confirm `src/main/java`, `src/test/java`, `frontend/src`, and `pom.xml` unchanged in this phase..
