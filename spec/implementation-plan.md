# Implementation Plan

**Product:** Support Ticket Management System  
**Phase:** 10 — Implementation Plan  
**Status:** Complete (plan only; no code, `pom.xml`, or `Main.java` changes)  
**Date:** 2026-09-21

This plan sequences work for **later** PLAN.md phases 11–20. It does **not** authorize starting Phase 11.

**Current repository (must be preserved, not regenerated):**

- Maven `com.enterprise.ai:TicketManagement:1.0-SNAPSHOT`
- Java **17** (`maven.compiler.source/target`)
- Single source: `src/main/java/com/enterprise/ai/Main.java` (Hello World)
- No Spring Boot, no dependencies, no `src/main/resources`, no `src/test/java`

**Specs (inputs):** `spec/requirements.md`, `specification.md`, `architecture.md`, `data-model.md`, `api-contract.md`, `state-machine.md`, `ui-flow.md`, `test-strategy.md`.

Unresolved **OQ-001–OQ-015** and **OQ-017–OQ-019** stay unanswered. **OQ-016 is resolved** (PostgreSQL development/runtime; H2 automated tests). Persistence access is Spring Data JPA / Hibernate (**not OQ-017**). Ticket identity is a system-generated UUID (API string). Tasks that need remaining OQs are **blocked/deferred**.

AI suggestions in this file are **not** requirements. Humans must validate the items in §16 before those tasks run.

---

## 0. Order of implementation

| Stage | Name | PLAN.md |
|---|---|---|
| 1 | Project / Spring Boot foundation | 11 (start) |
| 2 | Domain model and business rules | 11 |
| 3 | Persistence | 11 |
| 4 | Application / service layer | 11 |
| 5 | REST API (tickets, fields, status) | 11 |
| 6 | State-machine enforcement | 12 |
| 7 | Comments / search / filter | 13 |
| 8 | Backend tests | 14 |
| 9 | Frontend | 15 |
| 10 | Frontend / backend integration | 16 |
| 11 | Acceptance testing | 17 |
| 12 | AI / code / spec review | 18 |
| 13 | Fixes | 19 |
| 14 | Final documentation | 20 |

Do not skip stages. Do not add auth, attachments, dashboards, or other out-of-scope features.

**Target packages** (under existing `com.enterprise.ai`, not created now):

```text
com.enterprise.ai                 # Boot entry (Main.java change is Stage 1, later)
com.enterprise.ai.domain          # ticket/comment/status/lifecycle
com.enterprise.ai.application     # use cases, ports
com.enterprise.ai.persistence     # adapters
com.enterprise.ai.api             # HTTP, DTOs, error mapping
```

Exact class names are implementation-time; this plan names **roles**, not a generated codebase.

---

## Stage 1 — Project / Spring Boot foundation

**Objective:** Turn the existing Maven project into a Spring Boot app on the **target** runtime without replacing the repo.

**Inputs:** architecture §1, REQ-038, REQ-039, OQ-016, OQ-017.

**Must happen later — not in Phase 10:**

1. **Human validation (OQ-017):** Java 17 → Java 21. Do not upgrade until approved.  
2. **Human validation:** Spring Boot **version** and starters (web, validation, data). Do not add dependencies now.  
3. **Human validation (OQ-016):** H2 vs PostgreSQL per environment. **Recorded Phase 16A:** PostgreSQL development/runtime; H2 automated tests.  
4. After approval: update `pom.xml` (parent, Java 21, starters).  
5. Controlled `Main.java` change **or** additional Boot entry keeping the existing class until the entry point is confirmed — PLAN requires an explicit later-phase decision before editing `Main.java`.  
6. Add `src/main/resources` configuration **without secrets** (REQ-050).  
7. Confirm `mvn -q test` / compile on the chosen JDK.

**Expected files (later):** `pom.xml` (modified), Boot entry, `application*.properties`/`yml` without credentials, maybe `.mvn/wrapper`.

**Requirements:** REQ-038, REQ-039, REQ-045, REQ-050.

**Tests this stage:** compile; empty context load if Boot is present. No ticket tests yet.

**Depends on:** Phase 10 complete; **human go-ahead** for Java 21 and Boot version.

**Checkpoint:** Repo still the same Maven tree; Hello World replaced only by an approved Boot entry; no ticket API yet.

**Blocked without:** OQ-017 (when/how Java 21), Spring Boot version pick. OQ-016 no longer blocks engine choice (**recorded Phase 16A**).

---

## Stage 2 — Domain model and business rules

**Objective:** Framework-light domain: ticket/comment concepts, VAL-* that are decided, lifecycle T1–T5 and invalid pairs.

**Inputs:** data-model, state-machine, specification §6–§7, architecture §4.3 / §8.

**Expected components:** domain ticket/comment types; status tokens; transition rules; domain errors (validation vs illegal transition vs not found as domain outcomes, not HTTP).

**Requirements:** REQ-012–REQ-018, REQ-021, REQ-024 (invariants), REQ-015–REQ-017.

**Tests:** domain unit tests — T1–T5, representative invalid, terminal, initial OPEN, blank title/comment. **Not** OQ-012 same-status.

**Depends on:** Stage 1 (compile/JDK). Domain can be written with little Spring.

**Checkpoint:** `/review-spec` vs state-machine; no persistence.

**Blocked/deferred:** OQ-001 priority set; OQ-002–004 optionality; OQ-012 same-status; OQ-008/009 terminal field/comment **guards** (do not invent).

---

## Stage 3 — Persistence

**Objective:** Durable Ticket and Comment records; unique id; parameterized access.

**Inputs:** data-model, architecture §6, REQ-033–REQ-037, REQ-041.

**Expected components:** persistence port + adapter; Spring Data JPA / Hibernate mapping to relational store; identity assignment as a **system-generated UUID** (API string).

**Human validation:** **Recorded Phase 16A:** Spring Data JPA / Hibernate; PostgreSQL development/runtime and H2 automated tests (OQ-016). **Not OQ-017.**

**Requirements:** REQ-012, REQ-018, REQ-033–REQ-037, REQ-041, REQ-048.

**Tests:** repository/integration: unique UUID id, get, list, filter-by-status, comment FK, restart. Automated tests use H2.

**Depends on:** Stages 1–2.

**Checkpoint:** no lifecycle bypass via raw status SQL from services.

**Blocked:** search indexes until OQ-005. Identity format and OQ-016 are **no longer blocked**.

---

## Stage 4 — Application / service layer

**Objective:** Use cases: create, list, get, patch fields, change status; transactions; load-miss → not found.

**Inputs:** specification §3, architecture §4.2, API contract (behavior, not HTTP yet).

**Expected components:** application services; constructor injection; `@Transactional` on writes (when Spring exists).

**Requirements:** REQ-001–REQ-007, REQ-011, REQ-020, REQ-025, REQ-026.

**Tests:** application tests with fake port — no persist on validation/lifecycle failure.

**Depends on:** Stages 2–3.

**Checkpoint:** PATCH does not change status; create always OPEN.

**Blocked:** OQ-002 create body; OQ-008 terminal PATCH; OQ-013 create `status` field handling; OQ-019 concurrency.

---

## Stage 5 — REST API

**Objective:** HTTP for tickets, field PATCH, status POST — **without** requiring comments/search yet (those are Stage 7). Error envelope.

**Inputs:** `spec/api-contract.md`, api-standards, architecture §4.1 / §7.

**Expected components:** HTTP adapters, request/response types, global error mapping to `VALIDATION` / `ILLEGAL_TRANSITION` / `NOT_FOUND` / `UNEXPECTED`.

**Endpoints this stage:** `POST/GET /tickets`, `GET/PATCH /tickets/{id}`, `POST /tickets/{id}/status`.  
**Not this stage:** comments, `keyword` (Stage 7). `status` **filter** may wait for Stage 7 with search, or ship with GET query if cheap — prefer **Stage 7** so filter/search stay together per PLAN Phase 13.

**Requirements:** REQ-001–REQ-007, REQ-011, REQ-027–REQ-030, REQ-040, REQ-047, REQ-049.

**Tests:** controller/API tests per test-strategy §5 (minus comment/keyword).

**Depends on:** Stage 4.

**Checkpoint:** `/review-code` vs contract; no entities on the wire.

**Human validation:** JSON date/creation-time wire format (still deferred). Ticket `id` is a UUID string (**recorded Phase 16A**).

---

## Stage 6 — State-machine enforcement

**Objective:** Every `POST /tickets/{id}/status` goes through domain; UI bypass still `409`; T1–T5 and invalid pairs on the live API.

**Inputs:** state-machine, test-strategy §7, architecture §8.

**Expected work:** wire-up review; no separate “StateMachineService” required if domain already owns transitions — **do not invent extra types**. Add any missing domain cases; API tests SM-IT-*.

**Requirements:** REQ-011, REQ-015–REQ-017, REQ-019, REQ-028, REQ-036, REQ-037.

**Tests:** all valid T1–T5; representative invalid; terminal; unchanged store; restart after reject; **not** SM-IT-40 (OQ-012).

**Depends on:** Stages 2, 4, 5.

**Checkpoint:** persistence cannot update status without domain.

---

## Stage 7 — Comments / search / filter

**Objective:** `POST /tickets/{id}/comments`; `GET /tickets?status=`; `GET /tickets?keyword=` **as far as OQs allow**.

**Inputs:** API §4.2 / §4.5, data-model comments, test-strategy §9–§10.

**Requirements:** REQ-008, REQ-009, REQ-010, REQ-018, REQ-023, REQ-024, REQ-035.

**Tests:** comment success/negatives; filter all five statuses + unknown token `400`; search only after OQ-005.

**Depends on:** Stages 3–5.

**Blocked/deferred:**

| OQ | Task |
|---|---|
| OQ-005, OQ-006, OQ-007 | Keyword matching / blank / combo |
| OQ-009 | Comments on terminal tickets |
| OQ-010 | Author UX (JSON field still required) |
| OQ-011 | Comment order |
| OQ-014 | Pagination/sort |

Until OQ-005, implement filter fully; search endpoint may return a documented **temporary limitation** only if a human approves — **default: do not invent a match rule**. Prefer leaving keyword behavior unimplemented until OQ-005 rather than guessing SQL `LIKE`.

---

## Stage 8 — Backend tests

**Objective:** Execute test-strategy §§3–10, 14; record real results.

**Inputs:** `spec/test-strategy.md`, state-machine §11.

**Expected files (later):** `src/test/java` mirroring layers; isolated DB.

**Requirements:** REQ-044 and all backend Must REQs.

**Depends on:** Stages 1–7.

**Checkpoint:** never claim pass without the Maven (or equivalent) run. Add test deps only in an approved implementation phase (`pom.xml` change with Stage 1 or 8 — **human**).

---

## Stage 9 — Frontend

**Objective:** Views V-LIST, V-CREATE, V-DETAIL, V-NOT-FOUND per ui-flow.

**Inputs:** ui-flow, api-contract, OQ-018.

**Expected (later):** UI app **location deferred** (same repo vs sibling). No CSS/component inventory in this plan.

**Requirements:** REQ-001–REQ-011, REQ-031, REQ-032, REQ-042.

**Tests:** UI tests per test-strategy §11.

**Depends on:** Stage 5+7 API available (or contract mocks). Prefer real API after Stage 8.

**Blocked:** OQ-018 stack; OQ-001 priority widgets; OQ-002 form required fields; OQ-014 list columns.

---

## Stage 10 — Frontend / backend integration

**Objective:** UI calls live REST; error classes mapped; no success on `4xx`/`5xx`.

**Inputs:** ui-flow §3, §14; architecture §3.

**Requirements:** REQ-031, REQ-032, REQ-040, REQ-047.

**Tests:** wired journeys create/list/detail/patch/comment/status/filter.

**Depends on:** Stages 8–9.

---

## Stage 11 — Acceptance testing

**Objective:** Run test-strategy §12; include restart.

**Requirements:** REQ-044, REQ-048, functional Musts.

**Depends on:** Stage 10.

**Evidence:** command + outcome. Do not claim pass if not run.

---

## Stage 12 — AI / code / spec review

**Objective:** `/review-code`, `/review-spec`; check out-of-scope creep; secrets; traceability.

**Inputs:** all spec/, `docs/ai-validation.md`.

**Depends on:** Stage 11 (or parallel with 8–10 if blocking defects).

---

## Stage 13 — Fixes

**Objective:** Repair review/test defects; add regression tests; log AI mistakes in `docs/ai-validation.md`.

**Depends on:** Stage 12.

---

## Stage 14 — Final documentation

**Objective:** README (setup, Java version **as actually built**, how to test), sync specs if behavior changed, prompt-history, traceability complete.

**Requirements:** REQ-043, documentation rules.

**Depends on:** Stage 13. Do not describe unimplemented features as done.

---

## Mapping: PLAN.md phases 11–20

| PLAN | Implementation stages |
|---|---|
| 11 Backend | 1–5 (foundation through ticket REST) |
| 12 State machine | 6 |
| 13 Comments/search/filter | 7 |
| 14 Backend testing | 8 |
| 15 Frontend | 9 |
| 16 Integration | 10 |
| 17 Acceptance | 11 |
| 18 AI review | 12 |
| 19 Fixes | 13 |
| 20 Final docs | 14 |

---

## Requirement paths (summary)

| REQs | Stages |
|---|---|
| REQ-038, REQ-039 | 1 (after human upgrade decision) |
| REQ-001–REQ-007, REQ-012–REQ-014, REQ-020–REQ-022, REQ-025–REQ-030 | 2–5, 8 |
| REQ-011, REQ-015–REQ-017, REQ-019, REQ-028 | 2, 6, 8 |
| REQ-008–REQ-010, REQ-018, REQ-023–REQ-024, REQ-035 | 7, 8 |
| REQ-033–REQ-037, REQ-041, REQ-048 | 3, 8, 11 |
| REQ-031, REQ-032, REQ-042 | 9–11 |
| REQ-040, REQ-047, REQ-049 | 5, 8 |
| REQ-044 | 8, 11 |
| REQ-043, REQ-045, REQ-046, REQ-050 | 1, 5, 12, 14 |

---

## Unresolved blockers (OQ)

Do not implement guessed behavior:

| OQ | Blocks |
|---|---|
| OQ-001 | Priority validation, UI options, tests |
| OQ-002–004 | Create/PATCH optionality |
| OQ-005–007 | Search (and combo with filter) |
| OQ-008–009 | Terminal PATCH/comments |
| OQ-010–011 | Author UX, comment order |
| OQ-012 | Same-status POST |
| OQ-013 | `status` on create |
| OQ-014 | List/sort/page |
| OQ-015 | Length validation |
| OQ-016 | **Resolved Phase 16A:** PostgreSQL development/runtime; H2 automated tests |
| OQ-017 | Java 21 timing — **Stage 1** |
| OQ-018 | Frontend Stage 9 |
| OQ-019 | Concurrency |

Work that **can** proceed without those OQs: foundation (after human Java/Boot choice), domain lifecycle T1–T5, persist tickets/comments, create with **title**, list/get, PATCH title, status POST, filter by status, error envelope, backend tests for decided rules.

---

## AI / human validation (do not auto-accept)

| Decision | Why a human must confirm |
|---|---|
| Java 17 → 21 (OQ-017) | Conflicts with current `pom.xml`; REQ-038 is a **target**, not a Phase 10 action |
| Spring Boot version / starters | Not in requirements; architecture only names Spring Boot |
| PostgreSQL vs H2 (OQ-016) | **Recorded Phase 16A:** PostgreSQL development/runtime; H2 automated tests |
| Persistence tech (JPA vs other) | **Recorded Phase 16A:** Spring Data JPA / Hibernate (**not OQ-017**) |
| Ticket `id` wire format | **Recorded Phase 16A:** system-generated UUID; API string; must be unique |
| Keyword search algorithm | OQ-005 — **do not** invent `LIKE` |
| Frontend React vs Next (OQ-018) | Family only |
| Frontend directory | Architecture: location deferred |
| `Main.java` vs new Boot class | PLAN: modify Main only when that later phase explicitly requires it |
| Any extra field, endpoint, or auth | Out of scope unless requirements change |

Record AI mistakes during implementation in `docs/ai-validation.md` (template already there). Process note already logged: Phase 1 prompt assumed Spring Boot was present; the repo was a Java 17 skeleton.

---

## Java / Spring Boot transition — validation before proceeding

**Before Stage 1 edits `pom.xml` or `Main.java`, confirm:**

1. Human approved Java 21 (OQ-017) and a Spring Boot version.  
2. Human approved DB for local/test (OQ-016). **Recorded Phase 16A:** PostgreSQL development/runtime; H2 automated tests.  
3. Existing Maven coordinates and `com.enterprise.ai` package kept.  
4. No greenfield Initializr overwrite of the repo.  
5. Secrets policy: no passwords in Git.  
6. After the change: compile on the chosen JDK; document actual Java in README only in Stage 14 / when true.

**This Phase 10 does not perform that transition.**

---

## What Phase 10 does not do

- Create Java/frontend/test files  
- Modify `pom.xml` or `Main.java`  
- Add dependencies or schema  
- Run tests  
- Resolve OQs  
- Start Phase 11
