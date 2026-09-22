# Requirements Review

**Phase:** 2 — Requirements  
**Date:** 2026-09-17  
**Reviewed artifact:** `spec/requirements.md`  
**Reviewer role:** Requirements analyst (no implementation)

This review does not add product features and does not decide architecture.

---

## Requirement completeness check

| Requested capability | Covered by | Complete? |
|---|---|---|
| Create a ticket | REQ-001, REQ-013, REQ-021 | Yes, with open field-optionality (OQ-002) |
| List tickets | REQ-002 | Yes, with open list/sort/paging (OQ-014) |
| View ticket details | REQ-003, REQ-018 | Yes |
| Update title | REQ-004, REQ-021 | Yes |
| Update description | REQ-005 | Yes, blank description open (OQ-004) |
| Update priority | REQ-006, REQ-022 | Yes, value set open (OQ-001) |
| Update assignee | REQ-007 | Yes, unassigned open (OQ-003) |
| Add comments | REQ-008, REQ-018, REQ-024, REQ-035 | Yes, author/order/terminal open (OQ-009–OQ-011) |
| Search by keyword | REQ-009 | Yes as observable behavior; match rule open (OQ-005) |
| Filter by status | REQ-010, REQ-023 | Yes |
| Persist ticket data | REQ-033–REQ-037, REQ-041, REQ-048 | Yes, including survive-restart |
| Validate input at backend | REQ-020–REQ-026, REQ-046 | Yes at business level; lengths open (OQ-015) |
| Meaningful UI errors | REQ-027–REQ-032, REQ-047 | Yes |
| Enforce state machine on backend | REQ-011, REQ-014–REQ-019, REQ-028 | Yes |
| Allowed lifecycle OPEN → IN_PROGRESS → RESOLVED → CLOSED | REQ-015 | Yes |
| Cancellation OPEN → CANCELLED, IN_PROGRESS → CANCELLED | REQ-015 | Yes |
| Invalid transitions rejected | REQ-016, examples and full pair list | Yes |
| Java 21 target | REQ-038 | Yes (deferred upgrade) |
| Spring Boot | REQ-039 | Yes (deferred add) |
| REST API | REQ-040, REQ-049 | Yes (contract later) |
| PostgreSQL/H2 | REQ-041 | Yes (environment choice later) |
| React/Next.js or equivalent | REQ-042 | Yes (stack choice later) |
| Maintainability, testability, security, no secrets | REQ-043–REQ-050 | Yes |

**Extra requirement that was necessary, not invented product scope:** REQ-011 (change status). Without a status-change operation, the lifecycle cannot be observed. No new domain concepts were added for it.

**Sequential IDs:** REQ-001 through REQ-050 are assigned with no gaps.

---

## Ambiguities found

Documented as open questions in `spec/requirements.md`:

- Priority allowed values (OQ-001)
- Which create fields besides title are mandatory (OQ-002)
- Whether assignee may be empty (OQ-003)
- Whether description may be blank (OQ-004)
- Search fields and match rule (OQ-005)
- Blank keyword meaning (OQ-006)
- Combining search and filter (OQ-007)
- Updates on `CLOSED` / `CANCELLED` (OQ-008)
- Comments on terminal tickets (OQ-009)
- Comment author without authentication (OQ-010)
- Comment ordering (OQ-011)
- Requesting the same status again (OQ-012)
- Client-supplied status on create: ignore vs reject (OQ-013)
- List columns, sort, pagination (OQ-014)
- Field length limits (OQ-015)
- PostgreSQL vs H2 by environment (OQ-016)
- When to move the existing Java 17 Maven project to Java 21 (OQ-017)
- React vs Next.js vs equivalent (OQ-018)
- Concurrent update behavior (OQ-019)

These are genuine gaps. They were not filled with invented answers.

---

## Conflicting requirements

| Conflict | Detail | Resolution in this phase |
|---|---|---|
| Java 21 (REQ-038) vs existing Java 17 `pom.xml` | Phase 1 found compiler source/target 17. | Recorded as OQ-017. **Do not upgrade now.** |
| Spring Boot (REQ-039) vs no Spring Boot in the repo | Phase 1 found no parent, starters, or version. | Deferred to implementation after architecture. **Do not add dependencies now.** |
| PostgreSQL and H2 both named (REQ-041) | Two technologies, one durability requirement. | Both allowed; environment split is OQ-016. |
| React **or** Next.js **or** equivalent (REQ-042) | Not a single stack. | Left as a later choice (OQ-018). |
| REQ-013 vs possible client status on create | Lifecycle must start at `OPEN`, but ignore vs reject is unspecified. | OQ-013; either is allowed as long as create cannot skip to later statuses. |

No two Must behaviors directly contradict each other (for example, invalid transitions are never also listed as allowed).

---

## Assumptions

Recorded in `spec/requirements.md` and repeated here:

1. Status change is a required operation (REQ-011) so the lifecycle is exercisable.
2. New tickets start as `OPEN`.
3. Every from→to pair not in the allowed table is invalid, not only the examples.
4. Title is required and non-blank.
5. The system assigns a unique ticket identity.
6. Comment creation time is system-assigned.
7. There is no authentication; comment author is a field, not a login.

---

## Requirements that should NOT be invented

Do not add these in later specification phases unless a human explicitly expands scope:

- Authentication, authorization, roles, sessions
- Email or other notifications
- Attachments
- Dashboards, reports, metrics
- SLA, due dates, escalation
- Audit timelines / history log (beyond current fields + comments)
- Ticket deletion
- Comment edit/delete
- Categories, tags, ticket created/updated timestamps as extra fields
- User directory for assignees
- Exact table names, JPA mappings, package structure
- Controller/service/repository class names
- Bean Validation annotation choices
- Exact SQL or search engine
- React component tree
- Spring Boot starter/version list
- HTTP status code mapping (belongs in API contract)

---

## Questions requiring human decision

Priority for unblocking later phases:

1. **OQ-001** Priority values — needed before data model and validation can be completed.
2. **OQ-002 / OQ-003 / OQ-004** Create/update optionality — needed before API contract and UI forms.
3. **OQ-005 / OQ-006 / OQ-007** Search/filter rules — needed before API contract and tests.
4. **OQ-008 / OQ-009** Terminal-state field/comment policy — needed before state machine spec.
5. **OQ-010 / OQ-011** Comment author and order — needed before data model and UI flow.
6. **OQ-012 / OQ-013** Same-status and create-status behavior — needed before state machine and API contract.
7. **OQ-017 / OQ-018 / OQ-016** Java 21 timing, frontend stack, database per environment — needed before architecture, still **not** to be executed in this phase.

---

## Implementation leakage check

The requirements document specifies observable behavior (reject illegal transitions, survive restart, show meaningful UI errors). It does **not** specify:

- `StateMachineService` or other class names
- JPA annotations
- Table DDL
- Controller names
- Repository implementation
- SQL
- React components

Platform NFRs name Java 21, Spring Boot, REST, PostgreSQL/H2, and React/Next.js or equivalent as **targets**, which the phase prompt required.

---

## Traceability note

`spec/requirements.md` maps REQ-ID → acceptance criteria → planned verification.  
`docs/traceability.md` should list the same REQ-IDs with specification = `spec/requirements.md`. Tasks, implementation, tests, and review remain empty until later phases.

No implementation tasks were created in this review.
