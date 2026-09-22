# Acceptance Test Report — Phase 17

**Product:** Support Ticket Management System  
**Date:** 2026-09-21  
**Phase:** 17 — Acceptance testing only  
**Production code:** Not modified in this phase

---

## 1. Scope

Validate the implemented application against REQ-001–REQ-050, the API contract, state machine, UI flow, and test strategy.

This report records evidence from:

- H2 Spring Boot API integration tests (`TicketApiIntegrationTest`)
- Domain tests (Phases 12–14)
- Frontend Vitest UI tests (jsdom, stubbed `TicketApi`)
- Static inspection of REST, persistence configuration, security, and UI source

Out of scope for this phase: fixing defects, resolving OQs, Phase 18 review, Phase 19 fixes, Phase 20 final documentation.

`spec/traceability.md` is not present. `docs/traceability.md` was used.

---

## 2. Environment

| Item | Actual |
|---|---|
| Git | Not a git repository (no `.git`). Baseline is the current workspace tree, not a diff. |
| Java | 21.0.8 (IntelliJ JBR) |
| Spring Boot | 3.5.5 |
| Automated persistence | H2 in-memory (`jdbc:h2:mem:ticketmanagement`, profile `test`) |
| Runtime DB config | PostgreSQL placeholders in `application.properties` (not started in this phase) |
| Frontend tests | Vitest 3.2.4, jsdom, stubbed API |
| Browser/E2E | Not executed |
| PostgreSQL process | Not started; no restart test |

---

## 3. Requirement acceptance matrix

Statuses: **PASS** (verified with evidence), **FAIL** (contradicts the requirement), **BLOCKED** (OQ or environment prevents a conclusive AC), **NOT APPLICABLE** (none used).

| Requirement | Acceptance condition | Evidence | Status |
|---|---|---|---|
| REQ-001 | Create ticket: exists after create, `OPEN`, fields stored, invalid create stores nothing | `TicketApiIntegrationTest.create_assignsUuidAndOpenStatus`; `create_rejectsBlankTitle`; domain `TicketTest.create_*` | **PASS** |
| REQ-002 | List empty vs non-empty; list does not mutate | API `list_returnsEmptyArrayWhenNoTickets`; `list_andDetail_returnPersistedTickets`; UI V-LIST empty/loading/error | **PASS** |
| REQ-003 | Details show fields and comments; view does not mutate | API GET after create/comment; UI `renders ticket fields and comments` | **PASS** |
| REQ-004 | Title update; blank title rejected | API `update_replacesSuppliedFieldsOnly`; domain `changeTitle_*` | **PASS** |
| REQ-005 | Description update | API PATCH then GET `description` | **PASS** |
| REQ-006 | Assignee/priority stored as supplied (no recognized priority set) | API PATCH `priority` | **PASS** (VAL-002 set is OQ-001; see REQ-022) |
| REQ-007 | Assignee update | API PATCH `assignee` | **PASS** |
| REQ-008 | Add comment; persist on that ticket | API `comment_isStoredOnTicket`; domain `CommentTest` | **PASS** |
| REQ-009 | Search by keyword | Matching not implemented; `TicketKeywordSearch`; API `search_isNotImplemented` | **BLOCKED** (OQ-005/006/007) |
| REQ-010 | Filter by each status; unknown token not an empty success list | API `filter_matchesEachStatus`; `filter_unknownStatus_isValidationNotEmptyList`; `filter_emptyResult_isEmptyArray`; domain `TicketStatusFilterTest` including `filter_doesNotMutateTickets` | **PASS** |
| REQ-011 | Status change via dedicated operation | API `validTransitions_persistNewStatus`; `POST /tickets/{id}/status` | **PASS** |
| REQ-012 | Unique system UUID, API string | API create UUID pattern + Location; client `id` → 400 | **PASS** |
| REQ-013 | Create always `OPEN` | API create `$.status` OPEN; domain `create` | **PASS** |
| REQ-014 | Five status tokens only | Domain `fiveStatusesExist`; API unknown token 400 | **PASS** |
| REQ-015 | T1–T5 allowed | Domain t1–t5; API five valid transitions | **PASS** |
| REQ-016 | Illegal pair rejected; status unchanged | Domain 15 pairs; API `invalidTransition_returns409AndLeavesStatus` | **PASS** |
| REQ-017 | Terminal `CLOSED`/`CANCELLED` have no outgoing transitions | Domain terminal tests; API illegal pairs from CLOSED/CANCELLED | **PASS** |
| REQ-018 | Comments belong only to their ticket | Domain association; API comment then details `comments[0]` | **PASS** |
| REQ-019 | Backend enforces lifecycle without UI | API 409 without UI | **PASS** |
| REQ-020 | Backend validation without UI | API 400 title/comment/status token | **PASS** |
| REQ-021 | VAL-001 title | Domain + API blank title 400 | **PASS** |
| REQ-022 | VAL-002 recognized priority set | No set decided; free text stored | **BLOCKED** (OQ-001) |
| REQ-023 | VAL-005 unknown status token | API status POST and filter `open` → 400 VALIDATION | **PASS** |
| REQ-024 | VAL-003/004 comment content/author | Domain CommentTest; API `comment_rejectsBlankContent`; `@NotBlank` author (API blank-author case not separately named) | **PASS** |
| REQ-025 | Unknown id → not found | API GET/comment 404 `NOT_FOUND` | **PASS** |
| REQ-026 | Validate before persist | Invalid create 400; illegal transition GET still old status | **PASS** |
| REQ-027 | Validation errors identifiable | Envelope `errorCode` VALIDATION + `fields` | **PASS** |
| REQ-028 | Illegal transition is business 409 | `ILLEGAL_TRANSITION` not VALIDATION | **PASS** |
| REQ-029 | Not-found distinguishable | 404 `NOT_FOUND`; UI V-NOT-FOUND / detail `onNotFound` | **PASS** |
| REQ-030 | Unexpected errors safe | 500 body `UNEXPECTED` / generic message; no stack in JSON (`search_isNotImplemented`) | **PASS** |
| REQ-031 | Meaningful UI errors | UI tests: list 500, create 400, detail 409 | **PASS** (unit; not live browser) |
| REQ-032 | UI success only after backend 2xx | `TicketCreateView` calls `onCreated` only after `createTicket`; create blank title does not call API | **PASS** (code + unit; success-path unit test not present) |
| REQ-033 | Persist so data survives process stop/start | Writes to JPA/H2 confirmed across HTTP in one JVM; process restart not executed | **BLOCKED** (restart environment) |
| REQ-034 | Tickets survive restart | PostgreSQL/H2 process restart not executed | **BLOCKED** |
| REQ-035 | Comments survive restart | Comment survives GET in same JVM; restart not executed | **BLOCKED** |
| REQ-036 | Updates persist after restart | PATCH/status then GET in same JVM; restart not executed | **BLOCKED** |
| REQ-037 | Rejected work not stored as requested state | 409 leaves status; blank create does not create ticket | **PASS** (same-process). Restart wording of AC-037-1 not executed. |
| REQ-038 | Java 21 | `pom.xml` java.version 21; tests ran on JBR 21.0.8 | **PASS** |
| REQ-039 | Spring Boot | parent 3.5.5; tests started Boot 3.5.5 | **PASS** |
| REQ-040 | REST API | `TicketController` vs contract; integration tests | **PASS** |
| REQ-041 | PostgreSQL runtime / H2 tests | H2 tests ran; PostgreSQL URL is placeholder-only | **PASS** (H2 automated). PostgreSQL runtime **not executed**. |
| REQ-042 | Users perform flows in React/equivalent UI | Views exist; unit tests; live browser and search matching not done | **BLOCKED** (browser/E2E not run; search OQ-005) |
| REQ-043 | Traceable; no extra features | Layers + `docs/traceability.md`; no auth/dashboards/attachments | **PASS** (inspection) |
| REQ-044 | Testable outcomes; restart verifiable | Transitions verified. AC-044-2 restart not verified. Tests were actually run. | **BLOCKED** (restart AC) |
| REQ-045 | No secrets; no invented auth | Inspection: placeholders, no login | **PASS** |
| REQ-046 | Validation quality | VAL-001/003/004/005 exercised | **PASS** (VAL-002 blocked by OQ-001) |
| REQ-047 | Four error classes | 400/404/409/500 mapped and tested | **PASS** |
| REQ-048 | Durable persistence quality / restart evidence | H2 HTTP persist only | **BLOCKED** (no PostgreSQL restart) |
| REQ-049 | Stable API names | Contract JSON used in tests | **PASS** |
| REQ-050 | No secrets in repo | Placeholders; `.gitignore` local env; `.env.development` is a public URL | **PASS** |

**FAIL count:** 0 (no implemented behavior was found that contradicts a *decided* requirement).

---

## 4. User journey results

### A. Create ticket

| Check | Result | Evidence |
|---|---|---|
| User can create from UI | **NOT RUN** live; code calls `api.createTicket` then `onCreated(id)` | `TicketCreateView.tsx`; no unit test of success navigation; no browser |
| Title validation | **PASS** | UI blank title without API call; API `create_rejectsBlankTitle` 400 VALIDATION |
| Ticket persisted | **PASS** (H2) | Create then GET |
| Generated UUID returned | **PASS** | `$.id` matches UUID; `Location: /tickets/{uuid}` |
| Status `OPEN` | **PASS** | API create body |
| Navigates/shows ticket after success | **NOT RUN** (browser); code intends `onCreated` | Gap: no UI success-path test |
| UI success only after backend | **PASS** (code + negative unit test) | API not called on blank title |

### B. List tickets

| Check | Result | Evidence |
|---|---|---|
| Tickets can be listed | **PASS** | API GET `/tickets` size 2 |
| Empty state | **PASS** | API `[]`; UI “No tickets to display.” |
| Loading state | **PASS** | UI “Loading tickets” |
| Backend errors meaningful | **PASS** | UI alert “Temporary failure”; no fabricated list |
| Persisted tickets appear | **PASS** (API/H2) | List contains created ids |

### C. Ticket details

| Check | Result | Evidence |
|---|---|---|
| Details can be opened | **PASS** (API + UI unit) | GET details; V-DETAIL fields |
| Comments visible | **PASS** | API comments array; UI “Checking routers” |
| Unknown ticket not-found | **PASS** | API 404; UI `onNotFound`, no title field |
| UI does not show blank ticket on 404 | **PASS** | `signals not found instead of rendering a blank ticket` |

### D. Update ticket

| Check | Result | Evidence |
|---|---|---|
| Title/description/priority/assignee | **PASS** | API PATCH then GET |
| Backend validation | **PASS** | Domain VAL-001; API empty PATCH 400; PATCH `status` 400 |
| UI success after backend | **PASS** (code inspection of `TicketDetailView.saveFields`) | No dedicated UI success unit test |
| Terminal PATCH (OQ-008) | **Not decided / not tested** | No extra 409 invented |

### E. Comments

| Check | Result | Evidence |
|---|---|---|
| Add comment | **PASS** | API 201 then GET |
| Blank content rejected | **PASS** | API + domain |
| Blank author rejected | **PASS** (domain + `@NotBlank`) | No separately named API integration method |
| Unknown ticket 404 | **PASS** | `comment_unknownTicket_isNotFound` |
| Comment persists | **PASS** (H2 subsequent GET) | Not a process restart |
| OQ-009/010/011 | Unresolved; not invented | Author remains request JSON (OQ-010 dependency) |

### F. Status lifecycle

See §5.

### G. Status filter

| Check | Result | Evidence |
|---|---|---|
| Each of five statuses | **PASS** | `filter_matchesEachStatus` |
| Invalid status rejected | **PASS** | `status=open` → 400 VALIDATION, not `[]` |
| Empty results | **PASS** | Filter CLOSED with only OPEN ticket → `[]` |
| Source not mutated | **PASS** | `TicketStatusFilterTest.filter_doesNotMutateTickets` |
| Combined search+filter | **Not implemented** | OQ-007 |

---

## 5. State-machine acceptance results

Valid transitions (API + domain):

| ID | From → To | Result |
|---|---|---|
| T1 | OPEN → IN_PROGRESS | **PASS** |
| T4 | OPEN → CANCELLED | **PASS** |
| T2 | IN_PROGRESS → RESOLVED | **PASS** |
| T5 | IN_PROGRESS → CANCELLED | **PASS** |
| T3 | RESOLVED → CLOSED | **PASS** |

Representative / all practical invalid pairs (API parameterized 15 pairs including the requested set): **PASS** — HTTP 409, `errorCode` `ILLEGAL_TRANSITION`, subsequent GET shows previous status.

Same-status (OQ-012): **not classified, not tested**.

---

## 6. API error acceptance

| Class | HTTP | `errorCode` | Evidence | Result |
|---|---|---|---|---|
| Validation | 400 | `VALIDATION` | Blank title; empty PATCH; unknown status token; `fields` present for field errors | **PASS** |
| Not found | 404 | `NOT_FOUND` | Unknown GET/comment | **PASS** |
| Lifecycle | 409 | `ILLEGAL_TRANSITION` | Illegal POST status | **PASS** |
| Unexpected | 500 | `UNEXPECTED` | Keyword search deferred path; generic message; `fields` omitted | **PASS** (structure). Search is not a specified 500 product rule; fail-closed for OQ-005. |

Envelope fields `status`, `errorCode`, `message` match `spec/api-contract.md` §2. Stack traces and credentials are not in the JSON body (`RestExceptionHandler`).

---

## 7. UI acceptance

| Item | Result |
|---|---|
| V-CREATE | Unit: blank title, backend 400 fields, no status control |
| V-LIST | Unit: loading, empty, error, status filter |
| V-DETAIL | Unit: fields/comments, T1–T5 buttons, terminal none, 409 keeps status |
| V-NOT-FOUND | Unit: heading + status text |
| No mock backend in app | `createHttpTicketApi` uses `fetch` + `VITE_API_BASE_URL` |
| Live browser smoke | **NOT RUN — browser/E2E environment unavailable/not executed** |
| Search matching | UI submits keyword; backend does not match (OQ-005) |

---

## 8. Persistence acceptance

### H2 automated persistence validation

**Executed.** Create, PATCH, comment, and allowed status change remain visible on a later GET in the same Spring test process. Illegal transitions do not change stored status.

### PostgreSQL runtime/restart validation

**NOT RUN.** No PostgreSQL instance was started. No application stop/start against PostgreSQL (or file-backed H2) was performed.

Do not treat H2 HTTP round-trips as process-restart proof of REQ-033–REQ-036 / REQ-048.

---

## 9. Security / repository checks

| Check | Result |
|---|---|
| Real passwords / API keys in source | **Not found** in application config |
| PostgreSQL credentials | `${SPRING_DATASOURCE_USERNAME:}` / `${SPRING_DATASOURCE_PASSWORD:}` |
| Frontend secrets | `.env.development` contains only `VITE_API_BASE_URL=http://localhost:8080` |
| Authentication invented | **No** |
| `.gitignore` | Ignores `.env`, `.env.local`, `application-local.properties`, `frontend/.env.local` |
| H2 test `password=` | Empty default for in-memory `sa`; not a production secret |
| Git history | **N/A** (workspace is not a git repository) |

---

## 10. Automated test results

### Backend

Command: IntelliJ Maven 3 + `JAVA_HOME` JBR 21 — `mvn test`

```text
Tests run: 135, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Finished at: 2026-09-21T14:22:37+05:30
```

Breakdown: API integration 44; CommentTest 17; TicketKeywordSearchTest 2; TicketStatusFilterTest 22; TicketStatusTransitionTest 30; TicketTest 20.

### Frontend tests

Command: `cd frontend && npm test` (`vitest run`)

```text
Test Files  1 passed (1)
Tests  11 passed (11)
```

### Frontend build

Command: `cd frontend && npm run build`

```text
tsc --noEmit && vite build
✓ built in 833ms
```

---

## 11. Browser/E2E result

**NOT RUN — browser/E2E environment unavailable/not executed.**

---

## 12. PostgreSQL runtime/restart result

**NOT RUN.** Runtime database was not started. Restart durability was not measured.

---

## 13. Remaining OQs

| OQ | Classification |
|---|---|
| OQ-016 PostgreSQL vs H2 | **Resolved** (human Phase 16A) |
| Persistence JPA/Hibernate | **Resolved** (human Phase 16A; not an OQ) |
| UUID ticket ids | **Resolved** (human Phase 16A) |
| Java 21 / Boot 3.5.5 | **Resolved** by human Phase 11 (OQ-017 catalog text still exists; do not reopen) |
| OQ-001 priority set | Unresolved, **blocks** REQ-022 / VAL-002 completeness |
| OQ-002 create required fields besides title | Unresolved, non-blocking for title-only create |
| OQ-003 unassigned / empty assignee | Unresolved, non-blocking for storing supplied assignee |
| OQ-004 blank description | Unresolved, non-blocking for storing supplied description |
| OQ-005 search semantics | Unresolved, **blocks** REQ-009 acceptance |
| OQ-006 blank search | Unresolved, **blocks** REQ-009 |
| OQ-007 search + filter | Unresolved, **blocks** combined list query |
| OQ-008 terminal field updates | Unresolved, **blocks** terminal PATCH acceptance |
| OQ-009 terminal comments | Unresolved, **blocks** terminal comment acceptance |
| OQ-010 comment author source | Unresolved; request-supplied author is an implementation dependency, not auth |
| OQ-011 comment order | Unresolved, non-blocking for storing comments |
| OQ-012 same-status transition | Unresolved, **blocks** same-status acceptance |
| OQ-013 client status on create | Unresolved; DTO does not bind `status`; stored create is always `OPEN` |
| OQ-014 pagination/order | Unresolved, non-blocking for unpaginated list |
| OQ-015 field lengths | Unresolved, **blocks** length-validation completeness |
| OQ-018 frontend family details | Unresolved; React+Vite used within allowed family |
| OQ-019 concurrent updates | Unresolved, **blocks** overlap/conflict acceptance |

Search implementation is consistent with unresolved OQ-005: no field/LIKE/case/fuzzy matcher exists.

---

## 14. Acceptance gaps

1. Browser/E2E not executed (create navigation, live list/detail/filter/errors).
2. PostgreSQL runtime and process-restart ACs (REQ-033–REQ-036, REQ-048, AC-044-2).
3. REQ-009 search matching (OQ-005/006/007) — deferred by design.
4. VAL-002 priority set (OQ-001).
5. Terminal PATCH/comments and same-status POST (OQ-008/009/012).
6. UI unit tests do not cover successful create → `onCreated`.
7. No separately named API test for blank comment author (domain + Bean Validation cover the rule).
8. Workspace is not a git repo; git history secret scan not applicable.

No production defect was recorded as **FAIL** against decided behavior. Gaps above are environment/OQ/test-coverage limits, not silent requirement changes.

---

## 15. Overall factual status

Acceptance **activity completed and documented**. Automated backend and frontend suites were re-run successfully. Live browser and PostgreSQL restart were not run. Several Must ACs remain **BLOCKED** on unresolved OQs or unexecuted restart/E2E environments.

Phase 18/19/20 were not started. Production code was not changed.
