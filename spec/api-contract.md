# API Contract

**Product:** Support Ticket Management System  
**Phase:** 6 — API Contract  
**Status:** Complete (contract only; no controllers, DTOs, or Java code)  
**Date:** 2026-09-21  
**Behavior source:** `spec/requirements.md`, `spec/specification.md`, `spec/architecture.md`, `spec/data-model.md`  
**Style:** `.cursor/rules/api-standards.md`

This document is the public HTTP contract. It does not authorize implementation. Open questions **OQ-001–OQ-015** and **OQ-017–OQ-019** remain unanswered; API details that depend on them are **Deferred**. Ticket `id` is a **system-generated UUID** exposed as a **string**. **OQ-016** is not HTTP (resolved: PostgreSQL runtime / H2 tests).

There is **no authentication**. `401` / `403` are not used. Ticket deletion and comment edit/delete are not in the contract.

---

## 1. Conventions

| Topic | Contract |
|---|---|
| Protocol | HTTP REST, JSON (`Content-Type: application/json`) |
| Base resource | `/tickets` |
| Ticket identity | Path `{id}` and JSON `id`. **System-generated UUID** unique among tickets; **string** on the wire |
| Collections | Plural nouns. Empty collection is `200` with `[]`, not an error [REQ-002] |
| Mutations | Never `GET` |
| Partial field update | `PATCH /tickets/{id}` — title, description, priority, assignee only |
| Lifecycle | Dedicated `POST /tickets/{id}/status` (not mixed into PATCH) [REQ-011] |
| Fail the whole request | No partial success |
| Server-owned fields | Client cannot set `id`, comment `creationTime`, or stored create `status` (create is always `OPEN` if accepted) [REQ-012, REQ-013, REQ-024] |

**Not in this contract:** API version prefix, pagination/sort parameters (**OQ-014**), auth headers, attachment uploads.

---

## 2. Error envelope [REQ-027–REQ-030, REQ-047, REQ-049]

Every failed response uses the same JSON object. Success responses do **not** use this envelope.

```json
{
  "status": 400,
  "errorCode": "VALIDATION",
  "message": "Human-readable explanation safe for the UI.",
  "fields": [
    { "field": "title", "message": "Title must not be blank." }
  ]
}
```

| JSON field | Required | Meaning |
|---|---|---|
| `status` | Yes | HTTP status code |
| `errorCode` | Yes | Stable machine code (table below) |
| `message` | Yes | Safe for UI; no stack traces, SQL, or secrets [REQ-030, REQ-045] |
| `fields` | When `errorCode` is `VALIDATION` and a specific field failed | `field` names match request JSON names |

### 2.1 Error classes

| Class | `errorCode` | HTTP | When |
|---|---|---|---|
| Validation | `VALIDATION` | `400 Bad Request` | VAL-* failure, malformed JSON, unknown status **token**, unrecognized priority, missing required write fields, empty PATCH [REQ-020–REQ-024, REQ-027] |
| Business (lifecycle) | `ILLEGAL_TRANSITION` | `409 Conflict` | Recognized status, but (current → target) is not allowed [REQ-016, REQ-028] |
| Not found | `NOT_FOUND` | `404 Not Found` | `{id}` does not exist [REQ-025, REQ-029] |
| Unexpected | `UNEXPECTED` | `500 Internal Server Error` | Technical failure; generic message only [REQ-030] |

`ILLEGAL_TRANSITION` must not be returned as `VALIDATION`. `NOT_FOUND` must not be returned as `200` with an empty ticket.

`fields` is omitted or empty for `ILLEGAL_TRANSITION`, `NOT_FOUND`, and `UNEXPECTED`.

---

## 3. Shared representations

JSON names are stable [REQ-049]. Types below are logical, not Java classes.

### 3.1 Ticket (no comments)

Used for create/list/update/status success bodies.

| Field | Type | Notes |
|---|---|---|
| `id` | string | System-generated UUID; unique among tickets |
| `title` | string | Non-blank when present |
| `description` | string or `null` | Nullability **OQ-002 / OQ-004** |
| `priority` | string or `null` | Must be a recognized value when non-null; **set = OQ-001** |
| `status` | string | One of `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` |
| `assignee` | string or `null` | Opaque text; **OQ-002 / OQ-003** |

### 3.2 Comment

| Field | Type | Notes |
|---|---|---|
| `content` | string | Required, non-blank |
| `author` | string | Required, non-blank; not an account id [OQ-010] |
| `creationTime` | string | System-assigned; **wire format / precision deferred** |

Comment technical id is not a business field and is **not** required on the wire (data-model deferred).

### 3.3 Ticket details

Ticket fields plus:

| Field | Type | Notes |
|---|---|---|
| `comments` | array of Comment | Only comments for this ticket [REQ-018]. Order **OQ-011** |

---

## 4. Endpoints

### 4.1 Create ticket [REQ-001, REQ-012, REQ-013, REQ-021]

`POST /tickets`

**Request**

| Field | Required | Rules |
|---|---|---|
| `title` | Yes | VAL-001 |
| `description` | **Deferred OQ-002 / OQ-004** | If sent, stored when valid |
| `priority` | **Deferred OQ-002**; if sent, VAL-002 / **OQ-001** | Unrecognized → `VALIDATION` |
| `assignee` | **Deferred OQ-002 / OQ-003** | |
| `status` | Must not select stored status | If present, **OQ-013** (ignore vs reject). Accepted create still stores `OPEN` only |
| `id` | Must not be client-assigned | If present: treat as **VALIDATION** (server-owned) |

**Success:** `201 Created`  
Body: Ticket (§3.1). Header `Location: /tickets/{id}`. `status` in body is `OPEN`.

**Errors:** `400 VALIDATION` (e.g. blank title); `500 UNEXPECTED`. No ticket stored [REQ-026].

---

### 4.2 List, search, filter [REQ-002, REQ-009, REQ-010, REQ-023]

`GET /tickets`

**Query parameters**

| Name | Required | Behavior |
|---|---|---|
| *(none)* | — | All tickets [REQ-002] |
| `status` | No | Exact match to one of the five statuses [REQ-010]. Unknown token → `400 VALIDATION` (not an empty success list) [REQ-023] |
| `keyword` | No | Restrict to tickets matching the keyword [REQ-009]. **Match fields/rule OQ-005**. Blank keyword **OQ-006** (list-all vs `400`) |
| both `keyword` and `status` | — | **OQ-007 Deferred** — do not implement a combined meaning until decided |

**Not specified (OQ-014):** `page`, `size`, `sort`. Do not add them until decided.

**Success:** `200 OK`  
Body: JSON array of Ticket (§3.1). No `comments`. Zero matches → `[]`. GET does not mutate.

**Errors:** `400 VALIDATION` (invalid `status`; invalid `keyword` only if OQ-006 is later “reject”); `500 UNEXPECTED`. Must not return a fabricated list.

---

### 4.3 Get ticket details [REQ-003, REQ-018, REQ-025]

`GET /tickets/{id}`

**Success:** `200 OK` — Ticket details (§3.3). No mutation.

**Errors:** `404 NOT_FOUND`; `500 UNEXPECTED`.

---

### 4.4 Update ticket fields [REQ-004–REQ-007, REQ-021, REQ-022]

`PATCH /tickets/{id}`

Partial update. Only supplied attributes are candidates for change. **`status` is not accepted here** (use §4.6).

**Request** (all fields optional in JSON, but see “at least one”)

| Field | Effect |
|---|---|
| `title` | Replace title; VAL-001 if present |
| `description` | Replace description; blank allowed? **OQ-004** |
| `priority` | Replace priority; VAL-002 / **OQ-001** |
| `assignee` | Replace assignee; empty/unassign **OQ-003** |
| `id` or `status` | **VALIDATION** if present |

At least one of `title`, `description`, `priority`, `assignee` must be present; otherwise `400 VALIDATION`.

Unspecified attributes remain unchanged.

**Success:** `200 OK` — Ticket (§3.1) after the update.

**Errors:** `400 VALIDATION`; `404 NOT_FOUND`; `500 UNEXPECTED`.

**Deferred OQ-008:** whether PATCH is allowed when `status` is `CLOSED` or `CANCELLED`. Until decided, this contract does not require extra `409` for terminal field updates and does not require allowing them.

**Deferred OQ-019:** concurrent overlapping PATCH behavior.

---

### 4.5 Add comment [REQ-008, REQ-018, REQ-024, REQ-025]

`POST /tickets/{id}/comments`

**Request**

| Field | Required | Rules |
|---|---|---|
| `content` | Yes | VAL-003 |
| `author` | Yes | VAL-004; **OQ-010** how the UI fills it |
| `creationTime` | Must not be client-set | If present → `400 VALIDATION` |

**Success:** `201 Created`  
Body: Comment (§3.2). Optional `Location` pointing at `GET /tickets/{id}` (comment is not a separately addressable resource in this contract). Existing comments unchanged.

**Errors:** `400 VALIDATION`; `404 NOT_FOUND` (unknown `{id}`; no comment stored); `500 UNEXPECTED`.

**Deferred OQ-009:** comments when ticket is `CLOSED` / `CANCELLED`.

There is no `GET /tickets/{id}/comments` collection besides details; no PATCH/DELETE on comments.

---

### 4.6 Change status [REQ-011, REQ-014–REQ-017, REQ-019, REQ-023, REQ-028]

`POST /tickets/{id}/status`

Documented non-idempotent-style action on a sub-resource. Body is the **target** status only.

**Request**

```json
{ "status": "IN_PROGRESS" }
```

| Field | Required | Rules |
|---|---|---|
| `status` | Yes | Must be one of the five tokens (VAL-005). Missing/unknown token → `400 VALIDATION` |

**Success:** `200 OK` — Ticket (§3.1) with the new `status`, iff (current → target) is allowed (specification §6.3 / later Phase 7). Other ticket fields unchanged.

**Errors:**

| Situation | HTTP | `errorCode` |
|---|---|---|
| Unknown `{id}` | `404` | `NOT_FOUND` |
| Missing or unknown `status` token | `400` | `VALIDATION` |
| Token valid but transition not allowed (including all pairs in specification §6.4, and leaving `CLOSED` / `CANCELLED`) | `409` | `ILLEGAL_TRANSITION` |
| Unexpected | `500` | `UNEXPECTED` |

On `409`, stored status is unchanged [REQ-016, REQ-026]. Direct clients (no UI) receive the same codes [REQ-019].

**Deferred OQ-012:** requesting the ticket’s **current** status again (success vs `409`). Not listed as allowed or invalid in the transition tables.

---

## 5. HTTP method and status summary

| Operation | Method | Path | Success | Typical errors |
|---|---|---|---|---|
| Create ticket | `POST` | `/tickets` | `201` | `400`, `500` |
| List / search / filter | `GET` | `/tickets` | `200` | `400`, `500` |
| Get details | `GET` | `/tickets/{id}` | `200` | `404`, `500` |
| Update fields | `PATCH` | `/tickets/{id}` | `200` | `400`, `404`, `500` |
| Add comment | `POST` | `/tickets/{id}/comments` | `201` | `400`, `404`, `500` |
| Change status | `POST` | `/tickets/{id}/status` | `200` | `400`, `404`, `409`, `500` |

No `PUT`, `DELETE`, or `GET` mutations.

Malformed JSON or unsupported media type: `400 VALIDATION` (no stack trace).

---

## 6. Behavior and constraints

1. Reads do not change stored data.  
2. Unknown `{id}` never creates a ticket [REQ-025].  
3. Invalid writes do not persist [REQ-026, REQ-037].  
4. UI must treat only `2xx` as success [REQ-032]; error `message` / `errorCode` are what the UI shows [REQ-031].  
5. Persistence after restart is a backend property of these resources, not extra endpoints [REQ-033–REQ-036].  
6. Lifecycle allowed/invalid pairs are specified in `spec/specification.md` §6; this API only defines **how** a change is requested and **which error class** illegal changes use. Phase 7 owns the state-machine document.  
7. Priority enumerated values are **OQ-001**; the API type is string until that set exists.  
8. Length limits **OQ-015** — not encoded as numeric max in this contract.  
9. No secrets in URLs, payloads, or error bodies [REQ-050].  
10. Compatibility: do not rename these paths or JSON fields without a versioned change [REQ-049].

---

## 7. Deferred API details (open questions)

| ID | API impact |
|---|---|
| OQ-001 | `priority` allowed strings |
| OQ-002 | Create body required fields besides `title` |
| OQ-003 | `assignee` null/empty on PATCH |
| OQ-004 | Empty `description` on create/PATCH |
| OQ-005 | Semantics of `keyword` |
| OQ-006 | Empty `keyword` |
| OQ-007 | `keyword` + `status` together |
| OQ-008 | PATCH on terminal tickets |
| OQ-009 | POST comment on terminal tickets |
| OQ-010 | How `author` is populated (still required JSON) |
| OQ-011 | Order of `comments` |
| OQ-012 | POST status equal to current |
| OQ-013 | `status` on `POST /tickets` |
| OQ-014 | List columns, sort, pagination query params |
| OQ-015 | Max lengths → future `VALIDATION` |
| OQ-016 | Not HTTP (**resolved:** PostgreSQL development/runtime; H2 automated tests) |
| OQ-017 | Not HTTP |
| OQ-018 | Not HTTP |
| OQ-019 | Concurrent PATCH/POST status |

---

## 8. Out of contract

- `/login`, roles, API keys in the contract  
- `DELETE /tickets/{id}`  
- Comment PATCH/DELETE  
- Nested user resources  
- Endpoints for dashboards, SLA, attachments  

---

## 9. Requirement traceability

| REQ | Contract |
|---|---|
| REQ-001 | `POST /tickets` |
| REQ-002 | `GET /tickets` |
| REQ-003 | `GET /tickets/{id}` |
| REQ-004–REQ-007 | `PATCH /tickets/{id}` |
| REQ-008, REQ-018, REQ-024 | `POST /tickets/{id}/comments`; details `comments` |
| REQ-009 | `GET /tickets?keyword=` |
| REQ-010, REQ-023 | `GET /tickets?status=` |
| REQ-011, REQ-015–REQ-017, REQ-019 | `POST /tickets/{id}/status` |
| REQ-012 | `id` on create response; path `{id}` |
| REQ-013 | Create success `status` is `OPEN` |
| REQ-014 | Status tokens in body and query |
| REQ-020–REQ-026 | `400` before persist; `404` unknown id |
| REQ-027 | `VALIDATION` + `fields` |
| REQ-028 | `409` `ILLEGAL_TRANSITION` |
| REQ-029 | `404` `NOT_FOUND` |
| REQ-030 | `500` `UNEXPECTED`; safe `message` |
| REQ-031, REQ-032 | Envelope for UI; success only `2xx` |
| REQ-033–REQ-037, REQ-041, REQ-048 | Same resources, durable; not extra URLs |
| REQ-038, REQ-039 | Platform; not paths |
| REQ-040, REQ-049 | This REST contract |
| REQ-042 | UI consumes this contract |
| REQ-043–REQ-046 | Consistency, testability, validation without UI |
| REQ-047 | Four error codes / HTTP mapping |
| REQ-045, REQ-050 | No auth API; no secrets on the wire |
| REQ-044 | Contract is the API test oracle later |

---

## 10. Existing repository

No controllers or DTO classes exist. This phase does not add them.
