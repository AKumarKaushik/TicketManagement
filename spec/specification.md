# System Specification

**Product:** Support Ticket Management System  
**Phase:** 3 — Specification  
**Status:** Complete (implementation-independent)  
**Date:** 2026-09-21  
**Behavior source:** `spec/requirements.md` (REQ-001–REQ-050)  
**Open questions:** OQ-001–OQ-015 and OQ-017–OQ-019 remain unresolved. **OQ-016 is resolved** (PostgreSQL development/runtime; H2 automated tests). Ticket identity is a system-generated UUID exposed as an API string. Persistence access is Spring Data JPA / Hibernate (not OQ-017). This specification does not answer remaining OQs.

This document translates approved requirements into a single behavioral specification. It does **not** authorize application code, REST path design, database schema, class names, or frontend components.

Related later documents (still placeholders until their phases): `spec/architecture.md`, `spec/data-model.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/ui-flow.md`, `spec/test-strategy.md`.

---

## 1. Purpose and system context

The system lets a user create, list, inspect, update, comment on, search, filter, and progress support tickets. The backend is the authority for validation, lifecycle, and persistence. The UI presents tickets and shows meaningful errors. Clients that bypass the UI are still bound by backend rules.

**Actors**

| Actor | Role in this specification |
|---|---|
| User | Uses the UI to perform ticket and comment operations. No login or roles. |
| Backend | Validates input, enforces lifecycle, persists accepted data, exposes operations over a REST API (paths and payloads are defined later). |
| UI | Presents tickets, accepts user input, and displays errors that match backend outcomes. |

**In-scope concepts only:** ticket (title, description, priority, status, assignee, system-assigned identity) and comment (content, author, system-assigned creation time) belonging to one ticket.

**Out of scope (must not appear as specified behavior):** authentication, authorization, notifications, attachments, dashboards, SLA, audit timelines, ticket deletion, comment edit/delete, categories, tags, due dates, ticket timestamps, user directories.

---

## 2. Conceptual information

This section is conceptual. It is not a schema.

### 2.1 Ticket [REQ-001, REQ-012, REQ-014]

A ticket is a durable record with:

| Concept | Specified behavior | Unresolved |
|---|---|---|
| Identity | Assigned by the backend on create as a **system-generated UUID**. Unique among tickets. Stable across view, update, comment, search, filter, and restart. Exposed through the API as a **string**. | — |
| Title | User-supplied. Required and not blank on create and title update. | Length (OQ-015) |
| Description | User-supplied known field. Stored when supplied and valid. | Required? Blank allowed? (OQ-002, OQ-004) |
| Priority | User-supplied known field. When supplied, must be a recognized value. | Value set (OQ-001); required on create? (OQ-002) |
| Status | One of `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`. Set to `OPEN` on create. Changed only via the lifecycle. | Same-status request (OQ-012); client status on create (OQ-013) |
| Assignee | User-supplied known field. No user directory. | Required? Empty/unassign? (OQ-002, OQ-003) |

### 2.2 Comment [REQ-008, REQ-018, REQ-024]

A comment is a durable record attached to exactly one ticket:

| Concept | Specified behavior | Unresolved |
|---|---|---|
| Content | User-supplied. Required and not blank. | Length (OQ-015) |
| Author | User-supplied field (not a logged-in account). Required and not blank. | How supplied (OQ-010); length (OQ-015) |
| Creation time | Assigned by the backend on accept. Not user-edited. | Display order (OQ-011) |

Comments are not edited or deleted in this specification.

---

## 3. Ticket operations

Reads do not change stored data. Writes change only the fields specified for that operation. Unknown identity on view/update/comment/status change is **not found** (no create-on-missing, no silent success).

### 3.1 Create ticket [REQ-001, REQ-012, REQ-013, REQ-021]

**Precondition:** System available.

**Success behavior:**

1. User submits a new ticket using known ticket concepts.
2. Backend validates input.
3. Backend assigns a unique identity.
4. Stored status is `OPEN`. The user does not choose the stored create status.
5. Valid supplied title, description, priority, and assignee are stored as provided.
6. The ticket can then be listed, viewed, updated, commented on, searched, and filtered.

**Failure behavior:** Invalid create is rejected. No ticket is stored. UI shows a meaningful validation error.

**Unresolved:** OQ-002 (description/priority/assignee required?), OQ-001 (priority values), OQ-003, OQ-004, OQ-013 (ignore vs reject client-supplied status), OQ-015 (lengths). Either ignore-and-store-`OPEN` or reject-create is allowed for a client-supplied non-`OPEN` status; **storing a non-`OPEN` create status is forbidden**.

### 3.2 List tickets [REQ-002]

**Success behavior:** Backend returns the current tickets. UI presents them so the user can open details. Zero tickets is an empty list, not a failure. Listing does not mutate data.

**Failure behavior:** If the list cannot be retrieved, UI shows a meaningful error and must not fabricate tickets.

**Unresolved:** OQ-014 (which list fields, sort, pagination).

### 3.3 View ticket details [REQ-003, REQ-018, REQ-025, REQ-029]

**Precondition:** A ticket identity is supplied.

**Success behavior:** Backend returns identity, title, description, priority, status, assignee, and that ticket’s comments (content, author, creation time). Viewing does not mutate data.

**Failure behavior:** Unknown identity → not-found error. UI must not show another ticket’s data as this ticket, and must not show a blank ticket as successful details.

**Unresolved:** OQ-011 (comment order).

### 3.4 Update title [REQ-004, REQ-021]

**Success:** New non-blank title replaces the stored title. Other fields unchanged. Later list/details show the new title.

**Failure:** Missing, blank, or whitespace-only title → validation error; previous title unchanged. Unknown identity → not found.

**Unresolved:** OQ-008 (allowed on `CLOSED`/`CANCELLED`?), OQ-015.

### 3.5 Update description [REQ-005]

**Success:** New valid description replaces the stored description. Other fields unchanged.

**Failure:** Invalid description → previous description unchanged. Unknown identity → not found.

**Unresolved:** OQ-004 (blank allowed?), OQ-008, OQ-015.

### 3.6 Update priority [REQ-006, REQ-022]

**Success:** New recognized priority replaces stored priority. Other fields unchanged.

**Failure:** Unrecognized priority → validation error; previous priority unchanged. Unknown identity → not found.

**Unresolved:** OQ-001 (allowed values), OQ-008.

### 3.7 Update assignee [REQ-007]

**Success:** New valid assignee replaces stored assignee. Other fields unchanged. No permission or directory check.

**Failure:** Invalid assignee → previous assignee unchanged. Unknown identity → not found.

**Unresolved:** OQ-003 (empty/unassign?), OQ-008, OQ-015.

### 3.8 Change status [REQ-011, REQ-015, REQ-016, REQ-017, REQ-019]

**Success:** If (current status → target status) is an allowed pair, backend stores the target status. Other fields unchanged.

**Failure:**

- Target not one of the five statuses → validation error; status unchanged.
- Target recognized but pair not allowed → **business error** (illegal lifecycle); status unchanged.
- Unknown identity → not found.

The backend enforces this even if the UI is bypassed. A client cannot persist a skipped lifecycle.

**Unresolved:** OQ-012 (requesting the current status again).

Field updates (title, description, priority, assignee) are specified independently of status change. Whether they may occur in terminal statuses is OQ-008, not answered here.

---

## 4. Comments [REQ-008, REQ-018, REQ-024, REQ-035]

**Precondition:** Ticket exists. Whether `CLOSED`/`CANCELLED` tickets accept comments is **OQ-009** (unresolved). Until decided, this specification does not permit or forbid comments on terminal tickets.

**Success:**

1. User submits content and author.
2. Backend validates both as present and not blank.
3. Backend assigns creation time.
4. Comment is stored against that ticket only.
5. Details for that ticket show the new comment; existing comments remain.
6. Details for any other ticket do not show it.

**Failure:** Blank content or author → validation error; no comment stored. Unknown ticket → not found; no comment stored.

**Forbidden in this specification:** editing comments, deleting comments, moving a comment to another ticket.

**Unresolved:** OQ-009, OQ-010, OQ-011, OQ-015.

---

## 5. Search and filtering

Search and filter do not mutate tickets.

### 5.1 Keyword search [REQ-009]

**Success:** User supplies a keyword. Backend returns only tickets that match under the (undecided) match rule. No matches → empty result, not failure.

**Failure:** If search cannot be performed, UI shows a meaningful error. If search input is rejected, the system must not return an arbitrary subset presented as a successful search.

**Unresolved:**

- OQ-005 — which fields match; exact vs partial; case sensitivity
- OQ-006 — blank keyword: list-all vs reject
- OQ-007 — combination with status filter

This specification does not choose a query language or index.

### 5.2 Status filter [REQ-010, REQ-023]

**Success:** User supplies one of the five statuses. Result contains only tickets with that exact status. Valid filter with no matches → empty result, not failure.

**Failure:** Unrecognized status filter → validation error. UI must not show the unfiltered list as if the filter succeeded.

Equality is by status value (`OPEN` filter returns only `OPEN`, and likewise for the other four).

**Unresolved:** OQ-007 (combine with keyword).

---

## 6. Ticket lifecycle

Detailed transition tables belong also to a later state-machine document. Behavior specified here is binding.

### 6.1 Status set [REQ-014]

Stored and returned status is only: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`.

### 6.2 Initial status [REQ-013]

Every accepted create has status `OPEN`. Create must not store `IN_PROGRESS`, `RESOLVED`, `CLOSED`, or `CANCELLED`. Handling of a client-supplied status field on create is OQ-013.

### 6.3 Allowed transitions [REQ-015]

These pairs succeed when requested on a ticket whose current status is From:

| From | To |
|---|---|
| `OPEN` | `IN_PROGRESS` |
| `OPEN` | `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED` |
| `IN_PROGRESS` | `CANCELLED` |
| `RESOLVED` | `CLOSED` |

Happy path: `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`.  
Cancellation: `OPEN` → `CANCELLED`; `IN_PROGRESS` → `CANCELLED`.

After success, stored status equals To.

### 6.4 Invalid transitions [REQ-016, REQ-028]

Any status-changing pair **not** in §6.3 is rejected with a **business error**. Stored status remains the previous value. This includes the stated examples and all other non-allowed pairs:

| From | To |
|---|---|
| `OPEN` | `RESOLVED` |
| `OPEN` | `CLOSED` |
| `IN_PROGRESS` | `OPEN` |
| `IN_PROGRESS` | `CLOSED` |
| `RESOLVED` | `OPEN` |
| `RESOLVED` | `IN_PROGRESS` |
| `RESOLVED` | `CANCELLED` |
| `CLOSED` | `OPEN` |
| `CLOSED` | `IN_PROGRESS` |
| `CLOSED` | `RESOLVED` |
| `CLOSED` | `CANCELLED` |
| `CANCELLED` | `OPEN` |
| `CANCELLED` | `IN_PROGRESS` |
| `CANCELLED` | `RESOLVED` |
| `CANCELLED` | `CLOSED` |

Illegal transition is not a field-validation error and not an unexpected technical failure.

### 6.5 Terminal statuses [REQ-017]

`CLOSED` and `CANCELLED` cannot change to a different status. Requests to do so follow §6.4.

### 6.6 Backend authority [REQ-019, REQ-040]

Lifecycle checks apply to every status-change request that reaches the backend, including those not issued by the UI.

### 6.7 Same-status request [OQ-012]

Not specified. Not treated as allowed or invalid until a human decision.

---

## 7. Validation [REQ-020–REQ-026, REQ-046]

Validation is a backend rule, not a UI convenience. UI checks do not replace backend checks.

### 7.1 When validation runs

Before any create, field update, comment add, status write, or status filter is accepted, the backend validates user-supplied values. Failed validation or failed lifecycle checks occur **before** stored data changes [REQ-026].

### 7.2 Rules

| ID | Rule | Applies to |
|---|---|---|
| VAL-001 | Title present and not blank (blank includes whitespace-only) | Create; title update |
| VAL-002 | Priority, when supplied, is a recognized value | Create (if priority present); priority update |
| VAL-003 | Comment content present and not blank | Add comment |
| VAL-004 | Comment author present and not blank | Add comment |
| VAL-005 | Status is one of the five values | Status change target; status filter |

Invalid input never persists a new or changed ticket or comment [REQ-020].

### 7.3 Not-found vs validation vs lifecycle [REQ-025]

| Situation | Result |
|---|---|
| Identity does not exist (view, update, comment, status change) | Not found; no new data |
| Known identity, bad field | Validation error; data unchanged |
| Known identity, recognized but illegal status pair | Business error; status unchanged |
| Unknown status token on change or filter | Validation error (VAL-005), not a lifecycle business error |

### 7.4 Unresolved validation

OQ-001, OQ-002, OQ-003, OQ-004, OQ-006, OQ-015. Length limits and extra required fields are not invented here.

---

## 8. Errors [REQ-027–REQ-032, REQ-047]

### 8.1 Error classes

The system distinguishes four classes. Exact payload shape is deferred to the API contract.

| Class | When | Backend | UI |
|---|---|---|---|
| Validation | VAL-* failure, including unknown status token and unrecognized priority | Identifiable validation error; name the field when a specific field failed (title, priority, comment content/author, status filter) | Input error the user can act on |
| Business (lifecycle) | Illegal status transition | Business error; not success | Lifecycle/status message |
| Not found | Unknown ticket identity | Not-found error | Ticket was not found; not an empty successful ticket |
| Unexpected | Technical failure (e.g. persistence unavailable) | Failure, not success | Generic meaningful error; **no** stack traces, SQL, connection strings, or secrets |

Illegal transition must not be presented as a field-format problem. Not-found must not be presented as successful empty details.

### 8.2 UI-observable error behavior [REQ-031, REQ-032]

- The UI shows a human-readable message matching the actual backend class.
- The UI does not fail silently or appear successful when the backend rejected or failed the operation.
- After a rejected mutation, displayed data must not look updated; once aligned with backend truth, previous title/status remain visible.
- Messages must not include stack traces or internal diagnostics.

---

## 9. Persistence [REQ-033–REQ-037, REQ-041, REQ-048]

Accepted data is durable, not only process memory.

| Rule | Behavior |
|---|---|
| Tickets persist | After process stop and start, accepted tickets remain listable and viewable [REQ-033, REQ-034] |
| Field stability | Identity, title, description, priority, status, and assignee match pre-restart values [REQ-034] |
| Comments persist | Accepted comments remain on the same tickets with the same content, author, and creation time [REQ-035] |
| Updates persist | Last **accepted** title, description, priority, assignee, and status remain after restart [REQ-036] |
| Rejections do not persist | Rejected create does not appear after restart. Rejected transition still shows the old status after restart [REQ-037] |
| Persistence failure | Operation is an error; UI must not report success [REQ-033, REQ-030] |
| Read failure after restart | Meaningful error; must not look like a freshly empty system when data should exist [REQ-034] |

Storage technology target is PostgreSQL for development/runtime and H2 for automated tests [REQ-041; **OQ-016 resolved**]. Access style is Spring Data JPA / Hibernate (recorded; **not OQ-017**). Schema and SQL are **not** specified here. Restart verification is required quality evidence, not optional [REQ-048].

---

## 10. UI-observable behavior [REQ-002, REQ-003, REQ-031, REQ-032, REQ-042]

The user-facing application (React, Next.js, or equivalent — OQ-018 unresolved) must make the following observable without this specification designing screens or components.

**Journeys (what the user can do):**

- Create a ticket and then see it in the list and open its details
- See an empty list when no tickets exist
- Open details and see known ticket fields and comments
- Change title, description, priority, and assignee and see the new values after success
- Add a comment and see it on that ticket
- Search by keyword and see only matches (rule TBD)
- Filter by each of the five statuses
- Request allowed status changes and see the new status
- Receive a visible error on validation, illegal transition, not found, and unexpected failure

**Truthfulness:**

- Success indication only after backend accept
- Rejected updates do not display as saved
- Filter failure does not masquerade as an unfiltered list
- Not-found does not masquerade as a blank ticket

Screen layout, routing, and component structure are Phase 8.

---

## 11. Non-functional specification [REQ-038–REQ-050]

These are targets and quality bars. They are **not** an implementation plan and do not change the existing repository in this phase.

| ID | Constraint | Specified now | Deferred |
|---|---|---|---|
| REQ-038 | Java 21 target runtime | Intended runtime when implemented | When/how to leave current Java 17 (OQ-017). No upgrade in this phase. |
| REQ-039 | Spring Boot backend | Intended backend platform | Version, starters. Not added in this phase. |
| REQ-040 | REST API | Operations available over REST; backend rules apply to all API callers | Paths, methods, payloads (Phase 6) |
| REQ-041 | PostgreSQL (runtime) and H2 (tests) | Durable relational storage; OQ-016 resolved | Schema (implementation); JPA mappings |
| REQ-042 | React / Next.js / equivalent UI | Users work through such a UI | Which stack (OQ-018); screens (Phase 8) |
| REQ-043 | Maintainability | Implemented behavior traces to a REQ-ID; out-of-scope features absent | Structure/naming (Phase 4) |
| REQ-044 | Testability | Allowed/invalid transitions, restart persistence, and UI errors are independently verifiable. Tests must not be claimed passed unless run. | Test suite (Phase 9/14) |
| REQ-049 | API consistency | Stable names for known concepts and stable error classes once the contract exists | Contract document |

---

## 12. Security [REQ-045, REQ-050, REQ-020, REQ-030]

Authentication and authorization are **not** specified and must not be added as implied features.

Specified security behavior:

- Passwords, API keys, tokens, private keys, and connection credentials are not committed to Git and are not hardcoded in the repository.
- User input is not trusted until the backend validates it (including when the UI is bypassed).
- Errors and UI messages do not include secrets, stack traces, or connection material.
- Login, sessions, and roles are absent unless a later approved requirements change adds them.

If a secret appears, it is removed and rotated operationally; this specification will not record secret values.

---

## 13. Concurrency [OQ-019]

Overlapping updates to the same ticket are **unspecified**. This specification does not invent last-write-wins, locking, or conflict errors.

---

## 14. Assumptions carried forward

Same as Phase 2; not new product scope:

1. Status change is a specified operation so the lifecycle can be exercised.
2. New tickets start as `OPEN`.
3. Every from→to pair not in the allowed table is invalid.
4. Title is required and non-blank.
5. The backend assigns unique ticket identity.
6. Comment creation time is system-assigned.
7. Comment author is a field, not an account.

---

## 15. Open questions (preserved)

Copied from `spec/requirements.md`. Remaining items are unanswered. **OQ-016 is resolved** (Phase 16A).

| ID | Question | Why it still matters |
|---|---|---|
| OQ-001 | Allowed priority values? | Priority validation and tests |
| OQ-002 | Are description, priority, assignee required on create? | Create validity and UI required fields |
| OQ-003 | May assignee be empty / unassigned? | Assignee update and display |
| OQ-004 | May description be blank? | Description update success vs error |
| OQ-005 | Search fields and match rule? | Search tests; must not be answered by picking SQL |
| OQ-006 | Blank keyword = list all or reject? | Search vs list |
| OQ-007 | Combine search and status filter? | Combined result set |
| OQ-008 | Field updates on `CLOSED`/`CANCELLED`? | Extra business errors or allowed updates |
| OQ-009 | Comments on terminal tickets? | Add-comment preconditions |
| OQ-010 | How is comment author supplied without login? | Comment validation; do not invent accounts |
| OQ-011 | Comment display order? | Details and tests |
| OQ-012 | Same-status request: success, no-op, or error? | Lifecycle completeness |
| OQ-013 | Client status on create: reject or ignore and store `OPEN`? | Create API behavior |
| OQ-014 | List fields, sort, pagination? | List observability |
| OQ-015 | Field length limits? | Validation completeness |
| OQ-016 | PostgreSQL vs H2 per environment? | **Resolved:** PostgreSQL development/runtime; H2 automated tests |
| OQ-017 | When/how Java 17 → Java 21? | Must not upgrade in this phase. **Not** persistence access |
| OQ-018 | React vs Next.js vs equivalent? | UI implementation later |
| OQ-019 | Concurrent updates? | Unspecified; do not invent locking |

---

## 16. Requirement-to-specification traceability

Every REQ-001–REQ-050 maps to at least one section. Tasks, code, and tests remain unscheduled.

| REQ | Specification section(s) |
|---|---|
| REQ-001 | §3.1 Create ticket |
| REQ-002 | §3.2 List tickets; §10 UI-observable |
| REQ-003 | §3.3 View ticket details |
| REQ-004 | §3.4 Update title |
| REQ-005 | §3.5 Update description |
| REQ-006 | §3.6 Update priority |
| REQ-007 | §3.7 Update assignee |
| REQ-008 | §4 Comments |
| REQ-009 | §5.1 Keyword search |
| REQ-010 | §5.2 Status filter |
| REQ-011 | §3.8 Change status |
| REQ-012 | §2.1 Ticket identity; §3.1 |
| REQ-013 | §6.2 Initial status |
| REQ-014 | §6.1 Status set |
| REQ-015 | §6.3 Allowed transitions |
| REQ-016 | §6.4 Invalid transitions |
| REQ-017 | §6.5 Terminal statuses |
| REQ-018 | §2.2 Comment; §4 |
| REQ-019 | §6.6 Backend authority |
| REQ-020 | §7 Validation |
| REQ-021 | §7.2 VAL-001 |
| REQ-022 | §7.2 VAL-002 |
| REQ-023 | §7.2 VAL-005; §5.2 |
| REQ-024 | §7.2 VAL-003, VAL-004; §4 |
| REQ-025 | §7.3 Not-found |
| REQ-026 | §7.1 Validate before persist |
| REQ-027 | §8.1 Validation error class |
| REQ-028 | §8.1 Business error class; §6.4 |
| REQ-029 | §8.1 Not-found class |
| REQ-030 | §8.1 Unexpected class |
| REQ-031 | §8.2 UI errors; §10 |
| REQ-032 | §8.2 UI success only after backend success |
| REQ-033 | §9 Persistence |
| REQ-034 | §9 Tickets survive restart |
| REQ-035 | §9 Comments persist |
| REQ-036 | §9 Updates persist |
| REQ-037 | §9 Rejections do not persist |
| REQ-038 | §11 Java 21 target |
| REQ-039 | §11 Spring Boot |
| REQ-040 | §11 REST API; §6.6 |
| REQ-041 | §9; §11 PostgreSQL/H2 |
| REQ-042 | §10; §11 Frontend target |
| REQ-043 | §11 Maintainability; §1 out of scope |
| REQ-044 | §11 Testability |
| REQ-045 | §12 Security |
| REQ-046 | §7 Validation quality |
| REQ-047 | §8.1 Error class distinction |
| REQ-048 | §9 Restart as required quality |
| REQ-049 | §11 API consistency |
| REQ-050 | §12 No secrets in Git |

---

## 17. What this specification explicitly does not decide

- REST paths, HTTP methods, status codes, DTO names
- Database tables, columns, JPA entity mappings, SQL
- Package structure, class names, Spring beans
- Bean Validation annotations
- React/Next.js component tree
- Java/Spring Boot/dependency upgrades
- Answers to remaining open questions (OQ-001–OQ-015, OQ-017–OQ-019)

Persistence access Spring Data JPA / Hibernate, OQ-016 (PostgreSQL runtime / H2 tests), and UUID ticket identity are **recorded** (Phase 16A) and are not left unspecified here.
