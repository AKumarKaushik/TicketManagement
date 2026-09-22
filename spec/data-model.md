# Data Model

**Product:** Support Ticket Management System  
**Phase:** 5 — Data Model  
**Status:** Complete (logical model only; no schema, SQL, JPA, or classes)  
**Date:** 2026-09-21  
**Behavior source:** `spec/requirements.md`, `spec/specification.md`, `spec/architecture.md`

This document defines **logical records, attributes, relationships, and integrity rules**. It does not authorize tables, DDL, Java entities, repositories, or column types.

Open questions **OQ-001–OQ-015** and **OQ-017–OQ-019** remain unanswered. **OQ-016 is resolved** (PostgreSQL development/runtime; H2 automated tests). Persistence access is **Spring Data JPA / Hibernate** (recorded, not an OQ). Where a property depends on an unanswered open question, it is marked **Deferred**.

Out of scope as data: authentication principals, attachments, tags, categories, due dates, SLA fields, ticket created/updated timestamps, comment edit history, user directory records.

---

## 1. Logical records

Two persistable records exist. No other business records are in scope.

```text
Ticket 1 ────────── * Comment
```

| Record | Meaning | Cardinality |
|---|---|---|
| Ticket | Durable support ticket | Independent; identified uniquely |
| Comment | Durable remark on one ticket | Exists only with a ticket; many comments per ticket; a comment belongs to exactly one ticket |

Search and status filter are **queries over Ticket** (and possibly Comment text — **OQ-005**). They are not additional records.

---

## 2. Ticket data

[REQ-001, REQ-003, REQ-012, REQ-014]

| Attribute | Required on stored ticket? | Source | Constraints | Unresolved |
|---|---|---|---|---|
| Identity | **Required** | System-generated UUID on create | Unique among tickets; stable for view, update, comment, search, filter, and restart; API exposes the UUID as a **string** | — |
| Title | **Required** | User | Present and not blank (not whitespace-only) on create and on title update [VAL-001] | Max length **OQ-015** |
| Description | **Deferred** | User | Stored when supplied and valid. Whether it must be present on create, and whether blank is allowed, is not decided | **OQ-002**, **OQ-004**, length **OQ-015** |
| Priority | **Deferred** as required/optional on create | User | When a value is stored or supplied, it must be a **recognized priority** [VAL-002]. Unrecognized values are not stored | Value set **OQ-001**; required on create **OQ-002** |
| Status | **Required** | System on create; user request afterward via lifecycle only | Exactly one of `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`. Create always stores `OPEN` | Same-status write **OQ-012**; client-supplied status on create **OQ-013** (must never store non-`OPEN` on create) |
| Assignee | **Deferred** as required/optional | User | No user-directory identity. When stored, it is an opaque assignee value, not a foreign key to accounts | Required on create **OQ-002**; empty/unassign **OQ-003**; length **OQ-015** |

**Not ticket attributes:** comment fields, search keywords, UI-only flags, version/lock fields (**OQ-019** deferred), secrets.

### 2.1 Identity requirements [REQ-012, REQ-025]

- Every accepted ticket has exactly one identity.
- Two tickets never share an identity.
- The same identity retrieves the same ticket after restart [REQ-034].
- An identity that matches no ticket is **not found**; the model does not create a ticket on miss.
- Identity is not a user-invented business code. It is a **system-generated UUID**, unique among tickets, and exposed through the API as a **string**. Clients must not assign the stored identity.

### 2.2 Status representation [REQ-014]

Stored status is a closed set of five tokens:

`OPEN` | `IN_PROGRESS` | `RESOLVED` | `CLOSED` | `CANCELLED`

- No other status value may be persisted [REQ-014].
- Status is a single current value, not a history table (audit timelines are out of scope).
- Allowed replacements of that value are domain lifecycle rules (Phase 7 / specification §6), not extra status columns.

### 2.3 Priority representation [REQ-006, REQ-022]

- Priority is a single stored attribute on Ticket, not a related record.
- The **allowed token set is Deferred (OQ-001)**. Until that set exists, the model only states: stored priority ∈ recognized set, or the attribute is absent if optionality allows it (OQ-002).
- Do not persist free-text priorities outside the future recognized set.

### 2.4 Assignee representation [REQ-007]

- Assignee is a single stored attribute on Ticket.
- It is **not** a reference to a User record (no user table in this model).
- Whether the attribute may be absent or blank is **OQ-003** / **OQ-002**.

---

## 3. Comment data

[REQ-008, REQ-018, REQ-024, REQ-035]

| Attribute | Required on stored comment? | Source | Constraints | Unresolved |
|---|---|---|---|---|
| Ticket identity (association) | **Required** | System (from the ticket being commented on) | Must refer to an existing ticket; comment is not stored otherwise [REQ-025] | — |
| Content | **Required** | User | Present and not blank [VAL-003] | Length **OQ-015** |
| Author | **Required** | User field (not a login) | Present and not blank [VAL-004] | How the UI collects it **OQ-010**; length **OQ-015** |
| Creation time | **Required** | System on accept | Not user-edited; retained across restart | Ordering of comments **OQ-011**; clock/precision deferred |

**Comment record identity (technical):** each stored comment is a distinct record. Whether uniqueness is a surrogate key, or another non-user-visible identifier, is **deferred**. It is not a known business field and must not appear as a ticket concept.

**Forbidden comment data:** edited content versions, deleted flags (deletion out of scope), author account ids, attachments.

---

## 4. Relationships

[REQ-018]

| From | To | Rule |
|---|---|---|
| Ticket | Comment | One ticket has zero or more comments |
| Comment | Ticket | Each comment belongs to exactly one ticket |
| Comment | Ticket (other) | A comment is never visible or stored under a different ticket |

- Adding a comment to an unknown ticket identity stores nothing [REQ-025].
- Comments are not moved between tickets.
- Ticket deletion is out of scope; no delete-cascade policy is specified.

---

## 5. Required vs optional (summary)

### Always required once a ticket is accepted

- Identity  
- Title (non-blank)  
- Status (one of five; `OPEN` at create)

### Always required once a comment is accepted

- Association to an existing ticket  
- Content (non-blank)  
- Author (non-blank)  
- Creation time  

### Deferred optionality (do not invent null/not-null)

| Attribute | Question |
|---|---|
| Description on create / blank description | OQ-002, OQ-004 |
| Priority on create | OQ-002; values OQ-001 |
| Assignee on create / empty assignee | OQ-002, OQ-003 |

### Not stored as data

Keywords, status **filter** values, and UI error messages are request/response concerns, not Ticket attributes.

---

## 6. Validation constraints (data-level)

These constrain what may be **stored**. HTTP mapping is Phase 6. Bean Validation annotations are not specified.

| ID | Applies to | Store only if |
|---|---|---|
| VAL-001 | Ticket.title | Present and not blank (create and title update) |
| VAL-002 | Ticket.priority | If supplied/stored, member of recognized set (**set = OQ-001**) |
| VAL-003 | Comment.content | Present and not blank |
| VAL-004 | Comment.author | Present and not blank |
| VAL-005 | Status **values** used to change Ticket.status or to filter | Token is one of the five statuses. Filter is not a stored field |

Whitespace-only title, content, or author is treated as blank (specification §7.2).

Length limits for title, description, content, author, assignee: **OQ-015** (not invented).

Validation and lifecycle checks occur **before** a new stored state is committed [REQ-026, REQ-037].

---

## 7. Persistence requirements

[REQ-033–REQ-037, REQ-041, REQ-048]

| Rule | Data implication |
|---|---|
| Durable store | Ticket and Comment records live in relational storage. In-memory-only is insufficient |
| Survive restart | After process restart, identity, title, description, priority, status, assignee, and all comment content/author/creation time match the last **accepted** values |
| Rejected work | Failed create does not leave a Ticket. Failed field/status/comment write does not change stored attributes. After restart, last accepted state remains |
| Environment | **OQ-016 resolved:** PostgreSQL for development/runtime; H2 for automated tests. Logical attributes do not change. Access style **recorded (not an OQ):** Spring Data JPA / Hibernate |
| Secrets | Connection credentials are not data-model attributes and must not be stored in Git [REQ-050] |

Physical tables, indexes, and types are **not** defined here.

---

## 8. Data integrity rules

1. **Uniqueness:** Ticket identity is unique [REQ-012].  
2. **Closed status set:** Ticket.status ∈ {`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`} [REQ-014].  
3. **Create status:** Inserted tickets have status `OPEN` only [REQ-013].  
4. **Lifecycle writes:** A stored status may change to another status only when the (from → to) pair is allowed (specification §6.3). Illegal pairs must not appear in storage [REQ-016]. Enforcement is domain, not a second status column.  
5. **Referential:** Comment.ticket identity must exist; otherwise no comment row/record.  
6. **Isolation of comments:** Comments of ticket A never appear as data of ticket B [REQ-018].  
7. **Partial update:** Updating title, description, priority, assignee, or status replaces only that attribute; others remain [REQ-004–REQ-007, REQ-011].  
8. **No silent upsert:** Unknown identity does not insert a ticket [REQ-025].  
9. **Immutability of accepted comments:** Content, author, and creation time are not updated or deleted in this model.  
10. **Concurrency:** Overlapping writes (**OQ-019**) — no version attribute invented.

Integrity 4 is behavioral; the transition table is owned by the domain / Phase 7, not by extra tables in this model.

---

## 9. Terminal-state data considerations

[REQ-017, OQ-008, OQ-009]

**Decided**

- When status is `CLOSED` or `CANCELLED`, stored status must not change to a different status [REQ-017].
- Terminal status does not remove the ticket or its comments from storage. List, view, search, and filter may still return them (no “hide closed” rule was specified).

**Deferred — do not add constraints yet**

| Topic | Open question | Model impact if later forbidden |
|---|---|---|
| Change title, description, priority, assignee while terminal | **OQ-008** | Additional write guards; attributes remain stored either way |
| Add comments while terminal | **OQ-009** | Insert of Comment may be rejected; existing comments remain |

Until those questions are answered, this model **does not** add “frozen field” flags or a separate terminal-ticket record type.

---

## 10. Search and filter (data implications)

[REQ-009, REQ-010]

- Status filter matches Ticket.status by exact token among the five values.
- Keyword search does **not** add attributes. Which stored attributes participate (**OQ-005**), blank keyword (**OQ-006**), and combining with status (**OQ-007**) are deferred.
- List field set, sort, pagination (**OQ-014**) do not add records; they only affect how Ticket data is read.

---

## 11. Unresolved / open questions (data-model view)

| ID | Question | Effect on this model |
|---|---|---|
| OQ-001 | Priority value set | Cannot close Ticket.priority domain |
| OQ-002 | Required create fields besides title | Description/priority/assignee nullability on insert |
| OQ-003 | Unassigned / empty assignee | Assignee optional vs required |
| OQ-004 | Blank description | Description constraint |
| OQ-005 | Search fields / match rule | No extra attributes; query shape later |
| OQ-006 | Blank keyword | Not a stored field |
| OQ-007 | Search + filter together | Query only |
| OQ-008 | Field updates when terminal | Write permissions, not new columns |
| OQ-009 | Comments when terminal | Comment insert guard |
| OQ-010 | Author without login | Author remains a required string field; no User record |
| OQ-011 | Comment order | Creation time already stored; sort rule later |
| OQ-012 | Same-status request | Whether status value is rewritten unchanged |
| OQ-013 | Client status on create | Ignore vs reject; stored create status still `OPEN` if accepted |
| OQ-014 | List/sort/page | Read model, not new entities |
| OQ-015 | Length limits | String constraints not set |
| OQ-016 | PostgreSQL vs H2 | **Resolved:** PostgreSQL development/runtime; H2 automated tests |
| OQ-017 | Java 17 → 21 | Not data |
| OQ-018 | Frontend stack | Not data |
| OQ-019 | Concurrent updates | No lock/version field invented |

Also deferred (not numbered OQs): comment technical identifier; creation-time precision/timezone.

Ticket identity format is **recorded:** system-generated UUID, unique, API string. Persistence access is **recorded:** Spring Data JPA / Hibernate (not OQ-017).

---

## 12. What this model does not decide

- Table or column names, SQL types, indexes, sequences  
- JPA/Hibernate entity/table mappings, Java class names (access style is recorded; mappings are not)  
- REST payload shapes  
- State-machine implementation  
- Answers to remaining open questions (OQ-001–OQ-015, OQ-017–OQ-019)  

---

## 13. Requirement traceability

| REQ | Data-model relevance |
|---|---|
| REQ-001 | Ticket insert: identity, title, optional-deferred fields, status `OPEN` |
| REQ-002 | Read many Tickets |
| REQ-003 | Read one Ticket + its Comments |
| REQ-004 | Ticket.title replace |
| REQ-005 | Ticket.description replace |
| REQ-006 | Ticket.priority replace |
| REQ-007 | Ticket.assignee replace |
| REQ-008 | Comment insert |
| REQ-009 | Query Tickets (fields deferred OQ-005) |
| REQ-010 | Query Tickets by status |
| REQ-011 | Ticket.status replace if allowed |
| REQ-012 | Unique stable Ticket identity |
| REQ-013 | Create status `OPEN` only |
| REQ-014 | Status closed set |
| REQ-015–REQ-017 | Which status values may replace which (integrity; table in specification) |
| REQ-018 | Comment–Ticket cardinality |
| REQ-019 | No persistence path that skips lifecycle |
| REQ-020–REQ-026 | What may be stored; unknown identity |
| REQ-027–REQ-032 | Not attributes (error handling) |
| REQ-033–REQ-037 | Durability of Ticket and Comment |
| REQ-038–REQ-040, REQ-042–REQ-044, REQ-047, REQ-049 | Not data attributes |
| REQ-041, REQ-048 | Relational durable store |
| REQ-045, REQ-050 | No secrets as data; no user-account entity |
| REQ-046 | Same as validation constraints |

---

## 14. Existing repository

No Ticket/Comment entities, tables, or repositories exist today. This phase does not add them.
