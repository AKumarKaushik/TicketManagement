# Architecture

**Product:** Support Ticket Management System  
**Phase:** 4 — Architecture  
**Status:** Complete (target architecture; repository unchanged)  
**Date:** 2026-09-21  
**Behavior source:** `spec/requirements.md`, `spec/specification.md`  
**Constraint:** The current repository remains a Java 17 Maven Hello World project. This document describes the **target** architecture. It does not authorize code, `pom.xml` changes, dependency adds, or upgrades.

Open questions **OQ-001–OQ-015** and **OQ-017–OQ-019** remain unresolved. **OQ-016 is resolved** (PostgreSQL development/runtime; H2 automated tests). Persistence access is **Spring Data JPA / Hibernate** (recorded, not an OQ). This document does not invent remaining OQ answers.

Later phases own: data model (Phase 5), API contract (Phase 6), state-machine detail (Phase 7), UI flow (Phase 8), test strategy (Phase 9).

---

## 1. Current repository vs target

| Aspect | Current (Phase 1) | Target (this architecture) |
|---|---|---|
| Build | Maven `com.enterprise.ai:TicketManagement:1.0-SNAPSHOT` | Same Maven project, extended later — not replaced or regenerated |
| Language | Java 17 | Java 21 when OQ-017 is decided (REQ-038). **Not now.** |
| Backend | `Main.java` Hello World; no Spring Boot | Spring Boot application exposing a REST API (REQ-039, REQ-040) |
| Persistence | None | Relational store: PostgreSQL for development/runtime; H2 for automated tests (REQ-041; OQ-016 resolved). Access: Spring Data JPA / Hibernate (not OQ-017) |
| UI | None | React, Next.js, or equivalent SPA/app (REQ-042; OQ-018 deferred) |
| Entry point | `src/main/java/com/enterprise/ai/Main.java` | Controlled later change only; **do not modify in this phase** |

Implementation must add to this project. It must not delete the existing tree or invent a greenfield replacement.

---

## 2. System boundaries

```text
┌─────────┐     ┌──────────────────┐     ┌─────────────────────────┐     ┌──────────────┐
│  User   │────▶│  Frontend (UI)   │────▶│  Backend (REST API)     │────▶│  Relational  │
│         │◀────│  presentation    │◀────│  validation, lifecycle, │◀────│  database    │
└─────────┘     │  error display   │     │  persistence            │     │  PostgreSQL  │
                └──────────────────┘     └─────────────────────────┘     │  and/or H2   │
                         │                            ▲                  └──────────────┘
                         │                            │
                         └──── other HTTP clients ────┘
                              (same backend rules)
```

**Inside the system**

- UI: ticket/comment presentation and user input (REQ-031, REQ-032, REQ-042)
- Backend: authority for validation, lifecycle, and durability (REQ-019, REQ-020, REQ-033)
- Database: durable ticket and comment records (REQ-033–REQ-037)

**Outside the system (not built)**

- Identity provider, email, file storage, reporting, SLA engines, user directories

**Trust boundary:** The backend does not trust the UI. Direct HTTP callers receive the same validation and lifecycle enforcement (REQ-019, REQ-046).

---

## 3. Frontend / backend separation

Two deployable parts, coupled only by a later REST contract (Phase 6).

| Part | Responsibility | Must not |
|---|---|---|
| Frontend | Screens, forms, navigation, showing backend success/error classes in human-readable form | Be the only enforcer of lifecycle or field rules; claim success on backend failure; leak stack traces |
| Backend | REST operations for tickets and comments; persist accepted data; reject invalid input and illegal transitions | Embed UI components; persist entities through HTTP types |

The UI is a client of the API. If the UI is bypassed, backend behavior is unchanged (REQ-019, REQ-040).

**Physical location** of frontend source (same Git repo vs sibling project) is deferred. The existing Maven layout must not be replaced. Frontend stack choice is OQ-018.

---

## 4. Backend layers and components

Target backend is a single Spring Boot process with a layered modular monolith. No extra bounded-context services are required for this product.

```text
                    ┌─────────────────────────────────────┐
                    │           API (HTTP) layer          │
                    │  map HTTP ↔ DTOs; start validation; │
                    │  map errors to a stable API shape   │
                    └──────────────────┬──────────────────┘
                                       │ depends on
                    ┌──────────────────▼──────────────────┐
                    │        Application layer            │
                    │  use cases: create, list, view,     │
                    │  update fields, comments, search,   │
                    │  filter, change status; transactions│
                    └──────────────────┬──────────────────┘
                                       │ depends on
              ┌────────────────────────┼────────────────────────┐
              │                        │                        │
              ▼                        ▼                        ▼
     ┌─────────────────┐    ┌─────────────────┐      ┌─────────────────┐
     │  Domain layer   │    │ Persistence     │      │ (no other      │
     │  ticket/comment │    │ port + adapter  │      │  services)     │
     │  invariants and │    │ load/save/query │      │                │
     │  lifecycle      │    │                 │      │                │
     └─────────────────┘    └────────┬────────┘      └────────────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │ Relational DB   │
                            └─────────────────┘
```

Layers are responsibilities, not a list of class files. Class names, packages, and annotations are implementation later. Root package remains `com.enterprise.ai` unless a later phase records a change.

### 4.1 API (HTTP) layer

**Responsibilities**

- Accept HTTP requests and return HTTP responses for ticket and comment operations (paths/methods/status codes: Phase 6)
- Translate request/response **DTOs** to application inputs/outputs
- Trigger **boundary** validation of shape/presence (VAL-* as request-level checks)
- Translate application/domain outcomes into the four error classes: validation, business (lifecycle), not-found, unexpected (REQ-027–REQ-030, REQ-047)
- Do not expose persistence records as the public contract (REQ-049)

**Must not:** encode allowed status transitions; open database sessions; invent a ticket identity scheme other than the recorded UUID / API string; contain UI logic.

### 4.2 Application layer

**Responsibilities**

- Orchestrate each use case in `spec/specification.md` §3–§5 (create, list, view, field updates, comments, search, filter, status change)
- Own **transaction** boundaries for writes so a rejected operation does not persist partial invalid state (REQ-026, REQ-037)
- Load a ticket, invoke domain rules, persist only on accept
- Assign create-time comment clock (precision deferred) and request ticket identity assignment as a **system-generated UUID** (API string)
- Map “ticket does not exist” to not-found (REQ-025)

**Must not:** talk HTTP; bypass domain lifecycle; concatenate query strings from untrusted input.

### 4.3 Domain layer

**Responsibilities**

- Ticket and comment concepts (not HTTP, not tables)
- Invariants: title not blank; status only the five values; comment content/author not blank; comment belongs to one ticket
- **Lifecycle:** allowed vs invalid transitions; new tickets `OPEN`; `CLOSED`/`CANCELLED` terminal (REQ-013–REQ-017)
- Raise a **business** rejection for illegal transitions, distinct from field validation (REQ-016, REQ-028)

**Must not:** depend on the API layer, Spring Web, or a concrete database API. Framework-agnostic rules are preferred so lifecycle tests do not need a server (REQ-044).

OQ-008, OQ-009, OQ-012, OQ-013 remain **outside** this layer until decided: the architecture places those rules here *once specified*, and does not invent them now.

### 4.4 Persistence layer

**Responsibilities**

- Map domain ticket/comment records to the relational store
- Provide unique, stable identity on insert (REQ-012; identity **format** deferred)
- Query for list, get-by-id, keyword search, and status filter (REQ-002, REQ-003, REQ-009, REQ-010)
- Survive process restart (REQ-033–REQ-036)
- Use parameterized access only (security: no string-built queries)

**Must not:** enforce lifecycle (a raw status write that skips the domain is forbidden); return HTTP types; invent schema in this phase (Phase 5).

Search/filter **execution** lives here; search/filter **meaning** (which fields, blank keyword, combination) is still OQ-005–OQ-007.

---

## 5. Dependency direction

Dependencies point **inward**. Outer layers may depend on inner layers. Inner layers must not depend on outer layers.

| From | May depend on | Must not depend on |
|---|---|---|
| Frontend | Published REST contract | Domain classes, persistence, database |
| API layer | Application layer, DTOs | Persistence adapters, database APIs, UI |
| Application layer | Domain layer, persistence **ports** (interfaces) | HTTP types, UI |
| Domain layer | Nothing outside the domain | API, persistence adapters, Spring Web, JDBC/JPA types if those leak HTTP/DB into rules |
| Persistence adapters | Domain types, database | API layer, UI |

Injection: constructors, not field injection, when Spring is introduced. Application layer depends on persistence **abstractions**, not on a specific SQL product, so PostgreSQL (development/runtime) vs H2 (automated tests) can vary by environment without changing domain rules.

---

## 6. Persistence boundary

- The database is the durability boundary. In-memory-only storage does not satisfy REQ-033–REQ-048.
- The application/domain never assume a table layout. Phase 5 defines the data model.
- Writes that fail validation or lifecycle must not remain in the database (REQ-037). That is guaranteed by validating and applying domain rules **before** commit, inside an application transaction.
- Read after restart is a persistence-adapter + database concern, verified later as a test (REQ-048), not designed as a schema here.

**Technology target:** PostgreSQL for development/runtime and H2 for automated tests (REQ-041; **OQ-016 resolved**). Access style is **Spring Data JPA / Hibernate** (recorded; **not OQ-017**). JPA types must not leak into the domain.

---

## 7. Validation and error-handling boundaries

Two validation moments, both required (REQ-020, REQ-046):

| Boundary | What it checks | Insufficient alone? |
|---|---|---|
| API | Request shape, presence, recognized enumerations when those sets are known (VAL-001–VAL-005) | Yes. Callers can skip the UI and send HTTP directly. |
| Domain / application | Invariants and lifecycle; reject before persist | Yes. HTTP-layer checks can be incomplete; domain remains the last gate. |

**Error mapping**

| Outcome | Produced by | Mapped at API to error class |
|---|---|---|
| Field/rule failure (blank title, unknown status token, unrecognized priority) | API and/or domain | Validation (REQ-027) |
| Illegal from→to status | Domain | Business / lifecycle (REQ-028) |
| Unknown identity | Application (load miss) | Not found (REQ-029) |
| Infrastructure failure | Persistence / runtime | Unexpected (REQ-030); no stack traces, SQL, or secrets to clients |

The UI maps those **classes** to messages (REQ-031). HTTP status numbers and payload JSON are Phase 6.

---

## 8. Ticket lifecycle / business-rule responsibility

| Rule | Owner |
|---|---|
| Allowed and invalid transitions (specification §6) | Domain |
| Create always stored as `OPEN` | Domain + application (ignore vs reject extra client status = OQ-013) |
| Terminal `CLOSED` / `CANCELLED` | Domain |
| Enforcement even if UI is bypassed | API → application → domain (no persistence shortcut) |
| Same-status request | **Deferred (OQ-012)** |
| Field updates / comments on terminal tickets | **Deferred (OQ-008, OQ-009)** |

The UI may hide illegal actions later (Phase 8) as a convenience. Hidden buttons are not the control (REQ-019).

Phase 7 will record the transition table in `spec/state-machine.md`. Architecture only assigns **where** that table is enforced: the domain, invoked by the application on every status-change use case.

---

## 9. Search and filter responsibility

| Concern | Layer | Notes |
|---|---|---|
| Accept keyword / status from client | API | Unknown status filter is validation (VAL-005), not an empty successful list |
| Decide to search vs list vs filter | Application | Combination of both is **OQ-007** |
| Match rule (fields, case, partial) | Deferred **OQ-005** | Architecture does not pick SQL `LIKE` or an index |
| Blank keyword | Deferred **OQ-006** | |
| Execute read against storage | Persistence | Parameterized queries only |
| Present results / empty list | UI | Empty is success; transport failure is unexpected error |

List without filter remains an application “list all current tickets” use case (REQ-002). Pagination/sort fields are **OQ-014**.

---

## 10. Comment handling

Comments are part of the **same** ticket module, not a separate service.

| Step | Layer |
|---|---|
| Submit content + author | UI → API DTO |
| VAL-003, VAL-004 | API + domain |
| Ticket must exist | Application (else not found) |
| Assign creation time | Application/domain (system clock; format deferred) |
| Associate to one ticket | Domain invariant + persistence foreign association (schema later) |
| Load with ticket details | Application read; persistence fetch |

No comment edit/delete/move APIs in the target architecture (out of scope). Author is a stored field, not an authenticated principal (OQ-010 unresolved; do not add an identity service).

---

## 11. Security considerations

In scope for this architecture (REQ-045, REQ-050):

- No authentication/authorization subsystem — **do not add one** to “complete” security
- Secrets never in Git or source (DB passwords, keys, tokens)
- Backend validation of all mutating and filter inputs
- Parameterized persistence; no query assembly from raw client strings
- Error bodies and logs must not contain secrets, stack traces to the UI, or connection strings (REQ-030)
- Do not disable TLS or invent permissive CORS as a substitute for a contract; CORS policy is deferred to API/implementation if the UI is on another origin

Out of scope: login, roles, OAuth, CSRF strategy for cookie sessions (no sessions specified). If a later requirement adds auth, it becomes a new boundary in front of the API layer; it is not implied now.

---

## 12. Logging and observability

When the backend exists:

- Use a logger, not standard output (existing `Main.java` prints are not the target pattern)
- Log operation, ticket identity when known, and outcome (accepted / validation / business / not-found / unexpected)
- Do not log passwords, tokens, or full payloads that may contain secrets
- Correlation identifiers for a request are optional and **not** a new product requirement; if added later they must not appear as ticket fields

No metrics/dashboard product is in scope (requirements out of scope).

---

## 13. Testing boundaries

Detailed strategy is Phase 9. Architecture only separates **what can be tested where**:

| Boundary | Intent (REQ-044) |
|---|---|
| Domain unit | Allowed and invalid transitions; title/comment invariants; no server |
| Application unit | Use-case orchestration with persistence port substituted |
| API slice | HTTP mapping, validation errors, error classes — after the contract exists |
| Persistence integration | Queries, uniqueness, restart durability against H2 and/or PostgreSQL |
| UI | Meaningful errors and success-only-after-backend (REQ-031, REQ-032) |
| Direct API (bypass UI) | Illegal transition still rejected (REQ-019) |

Tests must not be claimed passed unless run. This phase adds no tests.

---

## 14. Major technology decisions and rationale

| Decision | Status | Rationale |
|---|---|---|
| Keep the existing Maven project | **Decided** | Phase 1/PLAN: do not replace or regenerate |
| Layered modular monolith (API / application / domain / persistence) | **Decided** | Matches governance layered architecture; small domain; clear dependency direction |
| Backend is Spring Boot | **Decided as target** (REQ-039) | Required NFR; version and starters deferred; **not added now** |
| Java 21 target runtime | **Decided as target** (REQ-038) | Required NFR; current compiler is 17; upgrade is OQ-017, **not now** |
| REST as the UI–backend interface | **Decided as target** (REQ-040) | Required NFR; paths/verbs/payloads are Phase 6 |
| DTOs at the API boundary, distinct from persistence | **Decided** | Prevents leaking storage into the UI contract (REQ-049) |
| Domain owns lifecycle | **Decided** | REQ-019: backend authority, UI-bypass safe, testable without HTTP |
| Relational persistence PostgreSQL (runtime) / H2 (tests) | **Decided** (REQ-041; OQ-016 resolved Phase 16A) | Durability after restart; environment split recorded |
| Persistence access Spring Data JPA / Hibernate | **Decided** (Phase 16A; **not OQ-017**) | Approved access style; mappings/schema remain implementation |
| Ticket identity UUID (API string) | **Decided** (REQ-012; Phase 16A) | System-generated, unique; wire type is string |
| Frontend React / Next.js / equivalent | **Decided as family** (REQ-042) | Exact stack OQ-018 |
| Constructor injection | **Decided for when Spring exists** | Governance; testability |
| No auth module | **Decided** | Out of scope; inventing it would violate requirements |
| Preserve `Main.java` until an approved entry-point change | **Decided** | PLAN constraint |

---

## 15. Deferred decisions and open questions

**Belong to later named phases (not decided here)**

| Topic | Phase |
|---|---|
| Tables, columns, indexes | 5 Data model / later implementation |
| Ticket identity type | **Recorded Phase 16A:** UUID, unique, API string |
| REST paths, methods, status codes, error JSON | 6 API contract |
| Full transition documentation and guards | 7 State machine (behavior already in specification §6) |
| Screens, navigation, form layout | 8 UI flow |
| Test pyramid, tools, commands | 9 Test strategy |
| Spring Boot version, starters, how `Main.java` becomes the Boot entry | 10/11 Implementation plan |
| Frontend repo layout and exact framework | 8 / 15 after OQ-018 |

**Unresolved requirements (still unanswered)**

OQ-001 priority values · OQ-002 create optionality · OQ-003 unassigned · OQ-004 blank description · OQ-005 search match · OQ-006 blank keyword · OQ-007 search+filter · OQ-008 terminal field updates · OQ-009 terminal comments · OQ-010 comment author source · OQ-011 comment order · OQ-012 same-status request · OQ-013 client status on create · OQ-014 list/sort/page · OQ-015 lengths · OQ-017 Java 17→21 timing · OQ-018 frontend stack · OQ-019 concurrency.

**Resolved (Phase 16A):** OQ-016 — PostgreSQL development/runtime; H2 automated tests. Persistence access Spring Data JPA / Hibernate (not an OQ). Ticket identity UUID / API string.

Architecture impact if remaining OQs stay open: some domain and API details cannot be fully completed, but layer ownership does not change.

---

## 16. Traceability (architecture → requirements)

| Architecture concern | Requirements |
|---|---|
| UI / backend split, REST, bypass-safe rules | REQ-019, REQ-031, REQ-032, REQ-040, REQ-042, REQ-046 |
| API layer + DTOs + error classes | REQ-020, REQ-027–REQ-030, REQ-047, REQ-049 |
| Application use cases | REQ-001–REQ-011 |
| Domain lifecycle and invariants | REQ-012–REQ-018, REQ-021–REQ-024, REQ-028 |
| Persistence durability | REQ-033–REQ-037, REQ-041, REQ-048 |
| Search/filter placement | REQ-009, REQ-010, REQ-023 |
| Comments in ticket module | REQ-008, REQ-018, REQ-024, REQ-035 |
| Security / no secrets / no invented auth | REQ-045, REQ-050 |
| Testability by layer | REQ-044 |
| Maintainability / no extra features | REQ-043 |
| Java 21 / Spring Boot targets | REQ-038, REQ-039 |

Specification mapping: `spec/specification.md` §1–§12 remain the behavioral source. This file only assigns **where** that behavior lives.

---

## 17. Explicitly not in this document

- Entity/table design, JPA entity mappings, SQL
- Endpoint paths, HTTP codes, DTO field lists
- Executable state-machine code or extra undocumented transitions
- React/Next components or routes
- Test class names or Maven Surefire configuration
- Answers to remaining open questions (OQ-001–OQ-015, OQ-017–OQ-019)
- Any change to `pom.xml` or `Main.java`
