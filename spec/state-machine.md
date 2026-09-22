# State Machine

**Product:** Support Ticket Management System  
**Phase:** 7 — State Machine  
**Status:** Complete (rules only; no enums, services, or tests)  
**Date:** 2026-09-21  
**Behavior source:** `spec/requirements.md`, `spec/specification.md` §6, `spec/architecture.md` §8, `spec/data-model.md` §8–§9, `spec/api-contract.md` §4.6

This document is the authoritative transition table. It does not authorize Java code. **OQ-008, OQ-009, OQ-012, OQ-013** remain unanswered.

There are no roles or permissions on transitions (authentication is out of scope). There are no notification or audit-history side effects.

---

## 1. Valid statuses [REQ-014]

Exactly five values may be stored or requested as a status **token**:

| Status | Kind |
|---|---|
| `OPEN` | Initial |
| `IN_PROGRESS` | Intermediate |
| `RESOLVED` | Intermediate |
| `CLOSED` | Terminal |
| `CANCELLED` | Terminal |

No other status exists. An unknown token is **not** a transition; it is validation (VAL-005) — see §6.

---

## 2. Initial status [REQ-001, REQ-013]

- A ticket enters the machine only by **create**.
- Accepted create stores **`OPEN`**. It is not a from→to transition.
- Create must not store `IN_PROGRESS`, `RESOLVED`, `CLOSED`, or `CANCELLED`.
- Client-supplied `status` on `POST /tickets`: **OQ-013 Deferred** (ignore vs `400`). If create is accepted, stored status is still `OPEN`.

```text
(create) ──▶ OPEN
```

---

## 3. Valid transitions [REQ-015]

A **transition** is a request to change stored status from a current value to a **different** target value. It succeeds only for these pairs:

| ID | From | To | Path |
|---|---|---|---|
| T1 | `OPEN` | `IN_PROGRESS` | Happy path |
| T2 | `IN_PROGRESS` | `RESOLVED` | Happy path |
| T3 | `RESOLVED` | `CLOSED` | Happy path |
| T4 | `OPEN` | `CANCELLED` | Cancellation |
| T5 | `IN_PROGRESS` | `CANCELLED` | Cancellation |

Required lifecycle: **`OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`**.  
Additional valid: **`OPEN` → `CANCELLED`**, **`IN_PROGRESS` → `CANCELLED`**.

There is no valid path `OPEN` → `RESOLVED`, `OPEN` → `CLOSED`, or `RESOLVED` → `CANCELLED`.

```text
        ┌─────────────────────────────────────────┐
        │                                         ▼
      OPEN ──▶ IN_PROGRESS ──▶ RESOLVED ──▶ CLOSED
        │            │
        │            ▼
        └──────▶ CANCELLED ◀── (from IN_PROGRESS)
```

**On accept:** stored status becomes To. Title, description, priority, assignee, identity, and comments are unchanged [REQ-011].

---

## 4. Invalid transitions [REQ-016]

Every **status-changing** pair not in §3 is invalid. Complete set:

| From | To | Notes |
|---|---|---|
| `OPEN` | `RESOLVED` | Skip `IN_PROGRESS` |
| `OPEN` | `CLOSED` | Skip path |
| `IN_PROGRESS` | `OPEN` | Backward |
| `IN_PROGRESS` | `CLOSED` | Skip `RESOLVED` |
| `RESOLVED` | `OPEN` | Backward |
| `RESOLVED` | `IN_PROGRESS` | Backward |
| `RESOLVED` | `CANCELLED` | Cancel not allowed after resolve |
| `CLOSED` | `OPEN` | Terminal |
| `CLOSED` | `IN_PROGRESS` | Terminal |
| `CLOSED` | `RESOLVED` | Terminal |
| `CLOSED` | `CANCELLED` | Terminal |
| `CANCELLED` | `OPEN` | Terminal |
| `CANCELLED` | `IN_PROGRESS` | Terminal |
| `CANCELLED` | `RESOLVED` | Terminal |
| `CANCELLED` | `CLOSED` | Terminal |

Same-status pairs (`OPEN`→`OPEN`, etc.) are **not** in this table. They are **OQ-012** (§7).

---

## 5. Terminal states [REQ-017]

`CLOSED` and `CANCELLED` are **terminal**.

- No request to a **different** status may succeed.
- All such requests are invalid transitions (§4) and follow §6.
- The ticket record remains; list/view/search/filter may still return it.
- Whether **fields** may be PATCHed while terminal is **OQ-008 Deferred**.
- Whether **comments** may be added while terminal is **OQ-009 Deferred**.
- Those OQs do not add or remove status values or from→to pairs.

---

## 6. Invalid transition request — behavior [REQ-016, REQ-019, REQ-026, REQ-028]

When current and target are both recognized statuses and the pair is in §4:

| Step | Rule |
|---|---|
| Who rejects | **Backend domain**, invoked by the application on every status-change use case. UI hiding buttons is not sufficient [REQ-019] |
| Client result | Business error, not validation, not-found, or unexpected |
| HTTP (contract) | `409 Conflict`, `errorCode`: `ILLEGAL_TRANSITION` |
| Stored status | **Unchanged** |
| Other fields | Unchanged |
| After restart | Still the pre-request status [REQ-037] |

Order of checks for `POST /tickets/{id}/status`:

1. Ticket exists? If no → `404 NOT_FOUND` (not a transition).  
2. Target present and one of the five tokens? If no → `400 VALIDATION` (VAL-005).  
3. Target equals current? → **OQ-012 Deferred** (do not treat as T1–T5 or as §4 until decided).  
4. Pair in §3? If yes → accept, persist To.  
5. Otherwise → `409 ILLEGAL_TRANSITION`, no persist.

---

## 7. Same-status requests [OQ-012]

**Deferred.** Not decided whether `POST /tickets/{id}/status` with the ticket’s current status is:

- success / no-op (`200`, status unchanged), or  
- `409 ILLEGAL_TRANSITION`, or  
- another error.

This document does **not** classify `OPEN`→`OPEN`, `IN_PROGRESS`→`IN_PROGRESS`, `RESOLVED`→`RESOLVED`, `CLOSED`→`CLOSED`, or `CANCELLED`→`CANCELLED` as valid or invalid.

---

## 8. Transition validation responsibility [REQ-019, REQ-020, architecture §8]

| Layer | Responsibility |
|---|---|
| UI | May offer only likely next statuses; must still handle `409` |
| API | Maps HTTP; VAL-005 on the token; maps domain rejection to `ILLEGAL_TRANSITION` |
| Application | Loads ticket, calls domain, persists **only** on accept; transaction so reject writes nothing |
| Domain | Sole authority for §3 vs §4 (and OQ-012 once decided) |
| Persistence | Stores the status the application commits; **must not** apply a status write that skipped domain |

`PATCH /tickets/{id}` must not change `status` (API contract). Lifecycle is only `POST /tickets/{id}/status` plus create’s initial `OPEN`.

---

## 9. Persistence after transitions [REQ-033–REQ-037]

| Outcome | Stored status | After process restart |
|---|---|---|
| Accepted T1–T5 | New status (To) | Same To; other fields unchanged [REQ-036] |
| Rejected §4 pair | Previous status | Previous status [REQ-037] |
| `400` / `404` / `500` | Unchanged (or no ticket) | Unchanged |
| Create accepted | `OPEN` | `OPEN` |

Illegal transitions must never appear as stored status.

---

## 10. API interaction (contract level) [REQ-011, REQ-040]

| Action | HTTP | Success | Lifecycle failure |
|---|---|---|---|
| Enter machine | `POST /tickets` | `201`, `status`: `OPEN` | N/A (not a transition) |
| Request To | `POST /tickets/{id}/status` body `{ "status": "<To>" }` | `200`, ticket with new `status` | `409 ILLEGAL_TRANSITION` |
| Observe | `GET /tickets/{id}` or `GET /tickets?status=` | Current stored status | — |

Filter `GET /tickets?status=CLOSED` is a **read** of stored status, not a transition. Unknown filter token is `400 VALIDATION`, not `409`.

---

## 11. Integration-test scenarios (required; do not implement in this phase)

These are **required later** tests [REQ-044]. They are not a test suite now. Do not claim they passed.

### 11.1 Setup

Create a ticket (always `OPEN`), then apply only allowed transitions to reach the **From** status under test.

### 11.2 Accepted transitions (one test each)

| Scenario | Given | When `POST .../status` | Then |
|---|---|---|---|
| SM-IT-01 | `OPEN` | `IN_PROGRESS` | `200`; stored `IN_PROGRESS` |
| SM-IT-02 | `IN_PROGRESS` | `RESOLVED` | `200`; stored `RESOLVED` |
| SM-IT-03 | `RESOLVED` | `CLOSED` | `200`; stored `CLOSED` |
| SM-IT-04 | `OPEN` | `CANCELLED` | `200`; stored `CANCELLED` |
| SM-IT-05 | `IN_PROGRESS` | `CANCELLED` | `200`; stored `CANCELLED` |
| SM-IT-06 | After SM-IT-01 | Restart, then GET | Status still `IN_PROGRESS` |

Also: other ticket fields unchanged on success (SM-IT-01).

### 11.3 Rejected transitions (one test each pair in §4)

| Scenario | Given | Target | Then |
|---|---|---|---|
| SM-IT-10 | `OPEN` | `RESOLVED` | `409`; stored still `OPEN` |
| SM-IT-11 | `OPEN` | `CLOSED` | `409`; stored still `OPEN` |
| SM-IT-12 | `IN_PROGRESS` | `OPEN` | `409`; still `IN_PROGRESS` |
| SM-IT-13 | `IN_PROGRESS` | `CLOSED` | `409`; still `IN_PROGRESS` |
| SM-IT-14 | `RESOLVED` | `OPEN` | `409` |
| SM-IT-15 | `RESOLVED` | `IN_PROGRESS` | `409` |
| SM-IT-16 | `RESOLVED` | `CANCELLED` | `409` |
| SM-IT-17 | `CLOSED` | `OPEN` | `409`; still `CLOSED` |
| SM-IT-18 | `CLOSED` | `IN_PROGRESS` | `409` |
| SM-IT-19 | `CLOSED` | `RESOLVED` | `409` |
| SM-IT-20 | `CLOSED` | `CANCELLED` | `409` |
| SM-IT-21 | `CANCELLED` | `OPEN` | `409`; still `CANCELLED` |
| SM-IT-22 | `CANCELLED` | `IN_PROGRESS` | `409` |
| SM-IT-23 | `CANCELLED` | `RESOLVED` | `409` |
| SM-IT-24 | `CANCELLED` | `CLOSED` | `409` |
| SM-IT-25 | After SM-IT-10 | Restart, GET | Still `OPEN` |

`errorCode` must be `ILLEGAL_TRANSITION`, not `VALIDATION`.

### 11.4 Not a transition (contrast)

| Scenario | When | Then |
|---|---|---|
| SM-IT-30 | `POST .../status` unknown `{id}` | `404 NOT_FOUND` |
| SM-IT-31 | Body `status` missing or not one of five | `400 VALIDATION`; stored status unchanged |
| SM-IT-32 | `POST .../status` without using UI (API client) | Same as SM-IT-10 for an illegal pair [REQ-019] |
| SM-IT-33 | `PATCH /tickets/{id}` with `status` | `400 VALIDATION`; status unchanged (contract) |

### 11.5 Deferred — do not write pass/fail tests until decided

| Scenario | Open question |
|---|---|
| SM-IT-40 `OPEN` → `OPEN` (same for each status) | OQ-012 |
| SM-IT-41 PATCH fields on `CLOSED` / `CANCELLED` | OQ-008 |
| SM-IT-42 POST comment on `CLOSED` / `CANCELLED` | OQ-009 |
| SM-IT-43 `POST /tickets` with `status` not `OPEN` | OQ-013 |

---

## 12. Related deferred questions (not transition pairs)

| ID | Topic | State-machine effect |
|---|---|---|
| OQ-012 | Same-status POST | §7 |
| OQ-013 | Status on create | §2; never persist non-`OPEN` on create |
| OQ-008 | Field updates when terminal | Not a status transition |
| OQ-009 | Comments when terminal | Not a status transition |
| OQ-019 | Concurrent status POSTs | No invented locking |

---

## 13. Requirement traceability

| REQ | This document |
|---|---|
| REQ-011 | Status change operation / API row |
| REQ-013 | §2 Initial `OPEN` |
| REQ-014 | §1 Five statuses |
| REQ-015 | §3 Valid transitions T1–T5 |
| REQ-016 | §4–§6 Invalid + reject + unchanged store |
| REQ-017 | §5 Terminal |
| REQ-019 | Domain + API-bypass scenario SM-IT-32 |
| REQ-023 | Unknown token `400`, not `409` |
| REQ-026, REQ-037 | §9 Persistence |
| REQ-028 | `409 ILLEGAL_TRANSITION` |
| REQ-036 | Accepted To survives restart |
| REQ-040 | §10 |
| REQ-044 | §11 scenarios |

---

## 14. Existing repository

No state-machine types or tests exist. This phase does not add them.
