# Test Strategy

**Product:** Support Ticket Management System  
**Phase:** 9 — Test Strategy  
**Status:** Complete (strategy only; no test classes, fixtures, or runs)  
**Date:** 2026-09-21  
**Behavior source:** `spec/requirements.md`, `spec/specification.md`, `spec/architecture.md` §13, `spec/data-model.md`, `spec/api-contract.md`, `spec/state-machine.md` §11, `spec/ui-flow.md`  
**Practice:** `.cursor/rules/testing.md`

This document says **what** must be verified later (Phases 14, 16, 17). It does **not** add `src/test/java`, UI tests, or test data files. **Do not claim tests passed unless they were executed.**

Open questions **OQ-001–OQ-015** and **OQ-017–OQ-019** remain unanswered. **OQ-016 is resolved** (PostgreSQL development/runtime; H2 automated tests). Tests that need remaining answers are **deferred**, not invented.

There is no authentication: no 401/403 test suite.

---

## 1. Testing objectives [REQ-044]

1. Prove each Must requirement with an observable outcome.  
2. Prove the lifecycle: all **valid** transitions succeed; **invalid** transitions fail without changing stored status.  
3. Prove backend validation even when the UI is bypassed [REQ-019, REQ-046].  
4. Prove the four error classes are distinguishable [REQ-047].  
5. Prove accepted data survives process restart [REQ-034–REQ-037, REQ-048].  
6. Isolate tests; name them by scenario and expected outcome.  
7. Never weaken assertions to get a green run.

---

## 2. Test levels

Align with architecture layers. Pyramid: many domain/unit tests, fewer API/persistence tests, fewest UI/E2E.

| Level | What | When (later phase) |
|---|---|---|
| Unit (domain) | Lifecycle, VAL-* invariants, no HTTP/DB | 12, 14 |
| Application | Use cases with persistence port substituted | 11, 14 |
| API / controller | HTTP mappings, status codes, error envelope | 14 |
| Persistence | Queries, uniqueness, restart against isolated DB | 14 |
| UI | Flows and error display [REQ-031, REQ-032] | 15–16 |
| Acceptance / E2E | Journeys in `spec/ui-flow.md` against a running system | 17 |

Integration tests use an **isolated** database. Automated tests use **H2**. Development/runtime uses **PostgreSQL**. They must not use a developer’s manual production data. The engine-per-environment split is **OQ-016 resolved**.

---

## 3. Unit testing

**Scope:** domain status machine and field invariants without Spring Web or JDBC.

| Area | Must assert |
|---|---|
| Create status | New ticket status is `OPEN` [REQ-013] |
| VAL-001 | Blank / whitespace title rejected |
| VAL-003, VAL-004 | Blank comment content/author rejected |
| VAL-005 | Unknown status token is validation, not a transition |
| T1–T5 | Each allowed pair succeeds; other fields conceptually unchanged |
| Invalid pairs | Each §4 pair (or equivalent domain API) rejected; status unchanged |
| Terminal | `CLOSED` / `CANCELLED` cannot move to a different status |

**Do not** unit-test same-status until **OQ-012** is decided.

Priority enumerated values (**OQ-001**) and optional create fields (**OQ-002**) wait for those answers.

---

## 4. Service / application testing

**Scope:** create, list, get, patch fields, add comment, search/filter, change status — persistence **port mocked or faked**.

| Must assert |
|---|
| Unknown id → not-found; no insert |
| Illegal transition → business rejection; port **save** of new status not committed |
| Valid transition → save of new status only |
| Patch does not change status |
| Comment attached only to the requested ticket |
| Failed validation → no persist |

---

## 5. API / integration testing [REQ-040, REQ-049]

**Scope:** HTTP per `spec/api-contract.md` (MockMvc or equivalent against a test slice).

| Call | Success | Negative |
|---|---|---|
| `POST /tickets` | `201`, `Location`, body `status`=`OPEN` | `400` blank title |
| `GET /tickets` | `200` array; `[]` if none | `500` path not invented; unexpected if forced |
| `GET /tickets/{id}` | `200` with comments | `404` unknown id |
| `PATCH /tickets/{id}` | `200`; only sent fields change | `400` blank title; `400` if `status` in body; `404` |
| `POST /tickets/{id}/comments` | `201` | `400` blank content/author; `404` |
| `POST /tickets/{id}/status` | `200` for T1–T5 | `400` unknown token; `409 ILLEGAL_TRANSITION`; `404` |
| `GET /tickets?status=` | `200` only that status; `[]` ok | `400` unknown status token — **not** unfiltered 200 |
| `GET /tickets?keyword=` | `200` matches or `[]` | Semantics **OQ-005/006/007 deferred** |

Assert error JSON: `status`, `errorCode` (`VALIDATION` / `ILLEGAL_TRANSITION` / `NOT_FOUND` / `UNEXPECTED`), safe `message`, `fields` only for validation.

Direct HTTP (no UI) must still get `409` for illegal transitions [REQ-019].

---

## 6. Persistence testing [REQ-033–REQ-037, REQ-041]

| Must assert |
|---|
| Unique ticket identity |
| Get-by-id, list, filter-by-status queries |
| Comment stored only with existing ticket |
| After process or context restart: last **accepted** ticket fields and comments remain |
| After rejected create/transition: no new ticket / old status still stored |

Schema/SQL is not designed here. Search field matching waits on **OQ-005**.

---

## 7. State-machine testing [REQ-011, REQ-015–REQ-017, REQ-028]

Authoritative scenarios: `spec/state-machine.md` §11. Required coverage:

### 7.1 Every valid transition

| ID | Given | When | Then |
|---|---|---|---|
| T1 / SM-IT-01 | `OPEN` | → `IN_PROGRESS` | `200`; stored `IN_PROGRESS` |
| T2 / SM-IT-02 | `IN_PROGRESS` | → `RESOLVED` | `200`; stored `RESOLVED` |
| T3 / SM-IT-03 | `RESOLVED` | → `CLOSED` | `200`; stored `CLOSED` |
| T4 / SM-IT-04 | `OPEN` | → `CANCELLED` | `200`; stored `CANCELLED` |
| T5 / SM-IT-05 | `IN_PROGRESS` | → `CANCELLED` | `200`; stored `CANCELLED` |

Also: other fields unchanged on success.

### 7.2 Representative invalid transitions

Minimum set (full pair list in state-machine §4 is preferred at API/domain level):

| Given | Target | Why representative |
|---|---|---|
| `OPEN` | `RESOLVED` | Skip |
| `OPEN` | `CLOSED` | Skip |
| `CLOSED` | `OPEN` | Terminal reopen |
| `CANCELLED` | `OPEN` | Terminal reopen |
| `RESOLVED` | `CANCELLED` | Cancel after resolve |
| `IN_PROGRESS` | `CLOSED` | Skip `RESOLVED` |

Then: `409`, `errorCode`=`ILLEGAL_TRANSITION` (not `VALIDATION`), **stored status unchanged**.

### 7.3 Terminal states

From `CLOSED` and from `CANCELLED`, every **different** target is rejected; stored status unchanged (SM-IT-17–24).

### 7.4 Invalid transition must not change stored status

After `409`, GET (and restart SM-IT-25) still shows the **from** status [REQ-026, REQ-037].

### 7.5 Initial `OPEN`

`POST /tickets` success body and subsequent GET have `status`=`OPEN`. Create is not a from→to transition.

### 7.6 Same-status — deferred [OQ-012]

Do **not** specify pass/fail for `OPEN`→`OPEN` (or any status → itself). Do not treat it as T1–T5 or as §4 until OQ-012 is decided. No tests that invent success, no-op, or `409` for same-status.

---

## 8. Validation / error testing [REQ-020–REQ-032, REQ-047]

| Case | Expected class |
|---|---|
| Blank title create/patch | `VALIDATION` `400` |
| Unrecognized priority when a set exists | `VALIDATION` — **OQ-001 deferred** until set known |
| Unknown status token on POST status or GET filter | `VALIDATION` `400` |
| Illegal pair of recognized statuses | `ILLEGAL_TRANSITION` `409` |
| Unknown `{id}` | `NOT_FOUND` `404` |
| Forced infrastructure failure (if testable) | `UNEXPECTED` `500`; no stack/SQL/secrets in body |

UI-level: each class shows a distinct, safe message; success chrome only after `2xx` [REQ-031, REQ-032].

No 401/403 suite.

---

## 9. Search / filter testing [REQ-009, REQ-010]

**Filter (fully specified):**

- `status=OPEN` (and each of the five) returns only that status.  
- Valid filter with no rows → `[]`, not 4xx.  
- Unknown token → `400`, not a successful unfiltered list.  
- GET does not mutate.

**Search:**

- No matches → `[]`, not failure.  
- Search does not mutate.  
- **Deferred OQ-005** (which fields, case, partial).  
- **Deferred OQ-006** (blank keyword).  
- **Deferred OQ-007** (keyword + status).  
Do not write tests that assume a match rule.

**OQ-014** list sort/pagination: no paging tests until decided.

---

## 10. Comment testing [REQ-008, REQ-018, REQ-024, REQ-035]

| Must assert |
|---|
| Valid comment: content, author, system `creationTime`; visible on that ticket only |
| Existing comments remain |
| Blank content or author → `400`; no comment stored |
| Unknown ticket → `404`; no comment |
| Comments survive restart |

**Deferred:** order (**OQ-011**), author-without-login UX (**OQ-010** still required field), comments on terminal tickets (**OQ-009**).

No comment edit/delete tests (out of scope).

---

## 11. UI testing approach [REQ-042, `spec/ui-flow.md`]

After a frontend exists (Phase 15+). Not in this phase.

| Flow | Assert |
|---|---|
| V-LIST | Empty vs populated; open detail |
| V-CREATE | Title required; no status picker; success → detail `OPEN` |
| V-DETAIL | Fields + comments |
| Edit | PATCH; rejected edit still shows previous saved values |
| Comment | Appears after `201` |
| Status | Only offered next statuses; `409` keeps old status |
| Search/filter | Empty result vs error |
| V-NOT-FOUND | Not a blank ticket |
| Loading | Mutations wait; no premature success |

Framework **OQ-018**: choose tools later (e.g. component vs E2E). Do not invent component names here.

---

## 12. End-to-end / acceptance testing [Phase 17]

Run against UI + API + DB. Map to ui-flow journeys:

1. Create → list → detail.  
2. Patch title (and other fields once optionality known).  
3. Add comment; see it on detail.  
4. Drive T1–T3 happy path and T4 or T5 cancel.  
5. Attempt `OPEN`→`CLOSED` (or equivalent) via UI or API; see lifecycle error; status still `OPEN`.  
6. Filter by each status.  
7. Restart backend; ticket and comments still there.

Do not claim acceptance passed without a recorded run.

---

## 13. Negative testing

Required negatives: missing ticket, invalid payloads, illegal transitions, PATCH with `status`, POST comment without ticket, unknown filter status, blank title, blank comment fields.

**Not required:** unauthorized (no auth). **Deferred:** concurrent writes (**OQ-019**), terminal PATCH/comment (**OQ-008/009**), same-status (**OQ-012**).

---

## 14. Persistence across restart [REQ-034–REQ-037, REQ-048]

Mandatory, not optional:

1. Accept create (+ optional comment and allowed status change).  
2. Stop and start the application (or equivalent test harness restart).  
3. GET: same `id`, fields, comments, status.  
4. Separate case: rejected transition then restart → old status.

---

## 15. Test data considerations

- Build data **in the test** via API or factories; no shared mutable globals.  
- Reset between tests.  
- Do not commit secrets or production dumps.  
- Identity is a system-generated UUID exposed as an API string: tests use ids returned by create and assert uniqueness.  
- Priority values: only after **OQ-001**. Until then, omit priority or skip those cases.  
- Titles unique per test where helpful for list assertions.  
- Reach `IN_PROGRESS` / `RESOLVED` / terminal only via **allowed** transitions, not by writing status in the database.

---

## 16. Important edge cases

| Case | Expect |
|---|---|
| Empty list | `200 []` |
| Whitespace-only title | `400` |
| GET does not change data | Idempotent read |
| Other fields unchanged on status change | REQ-011 |
| Comment isolation ticket A vs B | REQ-018 |
| Filter `[]` vs unknown token `400` | Distinct |
| `409` vs `400` vs `404` | Distinct codes |
| Terminal has no valid different To | REQ-017 |

---

## 17. Unresolved / OQ-dependent tests

Do not implement these as pass/fail until the OQ is decided:

| OQ | Blocked tests |
|---|---|
| OQ-001 | Recognized vs unrecognized priority |
| OQ-002 | Create with only title vs missing description/priority/assignee |
| OQ-003 | Unassign / empty assignee |
| OQ-004 | Blank description |
| OQ-005–007 | Keyword match, blank keyword, combined search+filter |
| OQ-008 | PATCH on `CLOSED`/`CANCELLED` |
| OQ-009 | Comment on terminal |
| OQ-010 | Author collection UX (field still required) |
| OQ-011 | Comment order |
| OQ-012 | Same-status POST |
| OQ-013 | `status` on create body |
| OQ-014 | Pagination/sort |
| OQ-015 | Length limits |
| OQ-016 | **Resolved:** automated tests use H2; runtime PostgreSQL |
| OQ-018 | Specific UI test runner |
| OQ-019 | Concurrent PATCH/status |

---

## 18. Requirement-to-test traceability

| REQ | Planned verification |
|---|---|
| REQ-001, REQ-012, REQ-013 | API create `201` + `OPEN` + unique id |
| REQ-002 | List `200`, empty `[]` |
| REQ-003, REQ-025, REQ-029 | GET detail / `404` |
| REQ-004–REQ-007 | PATCH success + negatives (priority set deferred) |
| REQ-008, REQ-018, REQ-024, REQ-035 | Comment tests §10 |
| REQ-009–REQ-010, REQ-023 | §9 |
| REQ-011, REQ-015–REQ-017, REQ-019, REQ-028 | §7 + API status |
| REQ-014 | Only five tokens; unknown → `400` |
| REQ-020–REQ-022, REQ-026 | Validation + no persist on fail |
| REQ-027, REQ-030–REQ-032, REQ-047 | Error envelope + UI display |
| REQ-033–REQ-037, REQ-041, REQ-048 | §6, §14 |
| REQ-038–REQ-039, REQ-042 | Build/UI later; not this phase |
| REQ-040, REQ-049 | API tests §5 |
| REQ-044 | This strategy; run before claiming pass |
| REQ-045, REQ-050 | No secrets in tests; no auth tests |
| REQ-043, REQ-046 | Review extras absent; bypass UI still validates |

Named scenarios SM-IT-* in `spec/state-machine.md` §11 are in scope for Phase 14 except SM-IT-40–43 (deferred OQs).

---

## 19. Isolation, naming, evidence

- Independent tests; reset store; no order dependence.  
- Names: `shouldRejectTransitionFromClosedToOpen`, `shouldReturnNotFoundWhenTicketDoesNotExist`.  
- Reporting a run: command, outcome, what was not run.  
- Defects: regression test + `docs/ai-validation.md` when AI-caused.

---

## 20. Existing repository

No `src/test/java` and no test dependencies today. This phase does not add them. Test libraries belong to a later implementation/testing phase, not now.

---

## 21. What this phase does not do

- Create JUnit/TestNG/Jest files  
- Add Maven test dependencies  
- Invent OQ answers  
- Claim any test passed  
