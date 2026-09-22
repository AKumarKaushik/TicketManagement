# UI Flow

**Product:** Support Ticket Management System  
**Phase:** 8 — UI Flow  
**Status:** Complete (user journeys only; no React/Next.js, components, or CSS)  
**Date:** 2026-09-21  
**Behavior source:** `spec/requirements.md`, `spec/specification.md` §10, `spec/architecture.md` §3, `spec/api-contract.md`, `spec/state-machine.md`

The UI is a client of the REST contract. Backend rules still apply if the UI is bypassed [REQ-019]. Framework choice is **OQ-018 Deferred**. This document names **views**, not classes or files.

There is **no login**. Do not add an auth screen.

Open questions **OQ-001–OQ-015** and **OQ-017–OQ-019** remain unanswered. **OQ-016 is not UI** (resolved: PostgreSQL runtime / H2 tests). UI that depends on unanswered OQs is marked **Deferred**.

---

## 1. Required views

| ID | View | Purpose |
|---|---|---|
| V-LIST | Ticket list | See tickets; search; filter; open a ticket; start create [REQ-002, REQ-009, REQ-010] |
| V-CREATE | Create ticket | Submit a new ticket [REQ-001] |
| V-DETAIL | Ticket detail | Fields, comments, field updates, status change, add comment [REQ-003–REQ-008, REQ-011] |
| V-NOT-FOUND | Ticket not found | Meaningful not-found; not a blank ticket [REQ-029, REQ-031] |

List, create, and detail each have **loading**, **success**, and **error** presentations (not separate product features). Empty list is a success state of V-LIST, not V-NOT-FOUND.

Out of scope views: login, dashboards, settings, attachment managers, delete confirmations.

---

## 2. Navigation

```text
                    ┌─────────────┐
                    │   V-LIST    │◀──────────────────┐
                    └──────┬──────┘                   │
           open ticket /   │   create                 │ back
           failed GET id   │                          │
              ┌────────────┼────────────┐             │
              ▼            ▼            ▼             │
        V-DETAIL      V-CREATE     V-NOT-FOUND ───────┘
              │            │
              │ success    │ success → V-DETAIL of new id
              │            │ (user can also return to V-LIST)
              └────────────┴──────────────────────────┘
```

| From | Action | To |
|---|---|---|
| V-LIST | Choose “create ticket” | V-CREATE |
| V-LIST | Choose a listed ticket | V-DETAIL (`GET /tickets/{id}`) |
| V-LIST | Open an unknown id (bookmark/stale link) | V-NOT-FOUND |
| V-CREATE | Cancel / back | V-LIST (no create) |
| V-CREATE | Create succeeds (`201`) | V-DETAIL of returned `id` |
| V-DETAIL | Back | V-LIST |
| V-DETAIL | `GET` returns `404` | V-NOT-FOUND |
| V-NOT-FOUND | Back to list | V-LIST |

No status-only screen: status change happens on V-DETAIL.

---

## 3. Shared UI rules [REQ-031, REQ-032, REQ-047]

| Rule | Behavior |
|---|---|
| Success | Only after HTTP `2xx`. Do not show saved/created/transitioned until then |
| Validation `400` `VALIDATION` | Show `message`; if `fields` present, associate with those inputs. Stay on the same view; do not clear the form unless specified below |
| Lifecycle `409` `ILLEGAL_TRANSITION` | Show as a **status/lifecycle** error, not a field-format error. Keep displaying the **previous** status from last successful GET |
| Not found `404` `NOT_FOUND` | V-NOT-FOUND (or equivalent). Do not render empty title/description as a real ticket |
| Unexpected `500` `UNEXPECTED` or transport failure | Meaningful generic error. **No** stack traces, SQL, or secrets |
| Filter/search failure | Do not replace the screen with an unfiltered list presented as a successful filter [REQ-010] |
| Backend is authority | UI may hide illegal next statuses as convenience; `409` must still be handled [REQ-019] |

---

## 4. Ticket list flow [REQ-002]

**API:** `GET /tickets` (no query = all tickets).

| State | User sees |
|---|---|
| Loading | List is in progress; not an error |
| Empty | No tickets — empty list, **not** a failure [REQ-002] |
| Success | Tickets the user can distinguish and open. **Which columns, sort, pagination: OQ-014 Deferred** — at minimum identity (or a label derived from the ticket) so V-DETAIL can be opened |
| Load error | Unexpected error; do not invent tickets |

Choosing a row/item navigates to V-DETAIL.

---

## 5. Create-ticket flow [REQ-001, REQ-013, REQ-021]

**API:** `POST /tickets`  
**View:** V-CREATE

1. User opens V-CREATE from V-LIST.  
2. User enters **title** (required, non-blank).  
3. Description, priority, assignee: **Deferred OQ-002 / OQ-001 / OQ-003 / OQ-004** — if shown, they map to the same JSON names; priority choices cannot be finalized until OQ-001.  
4. UI **does not** offer a status control and **does not** send `status` or `id` (avoids OQ-013; stored status is always `OPEN` if accepted).  
5. Submit → wait (loading).  
6. `201` → show success only then; go to V-DETAIL of `Location` / body `id`. Status shown is `OPEN`.  
7. `400 VALIDATION` → stay on V-CREATE; show field errors (e.g. title); **no** new ticket in the list.  
8. `500` / network → unexpected error; do not claim created.

Cancel returns to V-LIST without calling POST.

---

## 6. Ticket-detail flow [REQ-003, REQ-018]

**API:** `GET /tickets/{id}`  
**View:** V-DETAIL

| State | User sees |
|---|---|
| Loading | Details in progress |
| Success | `id`, title, description, priority, status, assignee, and comments (`content`, `author`, `creationTime`) |
| `404` | V-NOT-FOUND |
| Unexpected | Error on this view; not a fake ticket |

GET does not mutate. Comment **order** is **OQ-011 Deferred** (all comments for this ticket still appear).

---

## 7. Edit / update flow [REQ-004–REQ-007]

**API:** `PATCH /tickets/{id}` with one or more of `title`, `description`, `priority`, `assignee`. Never `status` or `id`.  
**View:** V-DETAIL (same view; not a separate product screen).

1. User changes one or more editable fields and submits.  
2. Loading until response.  
3. `200` → display returned ticket fields.  
4. `400 VALIDATION` → keep previous **saved** values as source of truth; show field errors (blank title, unrecognized priority, etc.). Do not look saved [REQ-032].  
5. `404` → V-NOT-FOUND.  
6. Unexpected → error; previous values remain.

**OQ-008 Deferred:** whether field edit is offered or accepted when status is `CLOSED` or `CANCELLED`. Until decided, this flow does not require hiding editors and does not require allowing them.

**OQ-001 / OQ-003 / OQ-004 / OQ-015:** option lists, empty assignee, blank description, lengths — deferred.

---

## 8. Comment flow [REQ-008, REQ-024]

**API:** `POST /tickets/{id}/comments`  
**View:** V-DETAIL

1. User supplies **content** and **author** (required). How author is chosen without login: **OQ-010 Deferred** (still a required field, not an account picker).  
2. UI does not send `creationTime`.  
3. `201` → comment appears on this ticket with system `creationTime`; existing comments remain. Optionally refresh via `GET /tickets/{id}`.  
4. `400` → stay on detail; show content/author errors; no new comment.  
5. `404` → V-NOT-FOUND.  
6. Unexpected → error; comments unchanged.

No comment edit/delete UI.

**OQ-009 Deferred:** add-comment on `CLOSED` / `CANCELLED`.

---

## 9. Status-change flow [REQ-011, REQ-015–REQ-017, REQ-028]

**API:** `POST /tickets/{id}/status` with `{ "status": "<target>" }`  
**View:** V-DETAIL

**Convenience (not a substitute for the backend):** offer only **valid next** targets from `spec/state-machine.md` §3:

| Current | Offer |
|---|---|
| `OPEN` | `IN_PROGRESS`, `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED`, `CANCELLED` |
| `RESOLVED` | `CLOSED` |
| `CLOSED` | none (terminal) |
| `CANCELLED` | none (terminal) |

Do not offer skip transitions (`OPEN`→`RESOLVED`, `OPEN`→`CLOSED`, etc.).

1. User selects an offered target and confirms.  
2. Loading until response.  
3. `200` → show new status; other fields unchanged.  
4. `409 ILLEGAL_TRANSITION` → lifecycle message; **displayed status stays the last successful GET/POST value**.  
5. `400 VALIDATION` (bad token) → validation message, not lifecycle.  
6. `404` → V-NOT-FOUND.  
7. Unexpected → error; previous status remains.

**OQ-012 Deferred:** no control whose purpose is “submit the current status again.”

Terminal: no next-status actions for `CLOSED` / `CANCELLED`. If a stale UI still posts an illegal pair, handle `409` as above.

---

## 10. Search flow [REQ-009]

**API:** `GET /tickets?keyword=...`  
**View:** V-LIST

1. User enters a keyword and applies search.  
2. Loading.  
3. `200` with matches → list those tickets only.  
4. `200` with `[]` → empty **result**, not a failure.  
5. Failure → unexpected (or `400` if later OQ-006 is reject); do not show an arbitrary full list as a successful search.

**Deferred:** match fields/rule (**OQ-005**), blank keyword (**OQ-006**), combining with status (**OQ-007**). Until OQ-007, do not specify a combined result. Until OQ-006, do not specify blank-keyword as “list all” or as error.

---

## 11. Status-filter flow [REQ-010, REQ-023]

**API:** `GET /tickets?status=<one of five>`  
**View:** V-LIST

1. User chooses `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`, or a clear/all control that calls `GET /tickets` with no `status`.  
2. Loading.  
3. `200` → only that status, or `[]` if none.  
4. UI must not send unknown status tokens. If `400 VALIDATION` occurs anyway, show validation error; **do not** display the unfiltered list as if the filter succeeded.

Clearing the filter returns to the full list (empty list still allowed).

---

## 12. Error behaviors (summary)

| Situation | View | User experience |
|---|---|---|
| Validation on create/edit/comment | Same form | Field-oriented message; not saved |
| Illegal status change | V-DETAIL | Lifecycle message; old status still shown |
| Unknown ticket | V-NOT-FOUND | Ticket was not found; path back to V-LIST |
| Unexpected / down | Current view | Generic safe message |
| Empty list / empty search / empty filter | V-LIST | Empty success, not V-NOT-FOUND |

---

## 13. Loading / empty / success (required)

| View | Loading | Empty | Success |
|---|---|---|---|
| V-LIST | In-progress fetch | No tickets or no matches | Array rendered; items openable |
| V-CREATE | Submit in progress | — | `201` then V-DETAIL |
| V-DETAIL | Fetch or mutation in progress | No comments yet is valid | Fields + comments |
| V-NOT-FOUND | — | — | Not-found message only |

Mutations disable double-submit while waiting (behavior, not a component name).

---

## 14. API map

| UI action | Contract |
|---|---|
| Load list | `GET /tickets` |
| Search | `GET /tickets?keyword=` |
| Filter | `GET /tickets?status=` |
| Open detail | `GET /tickets/{id}` |
| Create | `POST /tickets` |
| Edit fields | `PATCH /tickets/{id}` |
| Add comment | `POST /tickets/{id}/comments` |
| Change status | `POST /tickets/{id}/status` |

The UI never uses `GET` to mutate and never PATCHes `status`.

---

## 15. State-machine interaction

- Create success always shows `OPEN`.  
- Status control options follow §9 / state-machine T1–T5.  
- Terminal tickets: no next-status actions.  
- `409` is always possible (stale page, direct API). UI must not assume hidden options make `409` impossible.  
- OQ-008 / OQ-009 / OQ-012 do not change the transition table; they only gate extra controls.

---

## 16. Deferred UI (open questions)

| ID | UI impact |
|---|---|
| OQ-001 | Priority options on create/edit |
| OQ-002 | Which create fields besides title are shown as required |
| OQ-003 | Unassign / empty assignee control |
| OQ-004 | Blank description allowed in the form |
| OQ-005 | Search help text / what matches |
| OQ-006 | Blank search submit |
| OQ-007 | Search + filter together |
| OQ-008 | Edit fields when terminal |
| OQ-009 | Comment form when terminal |
| OQ-010 | Author input vs other non-auth mechanism |
| OQ-011 | Comment list order |
| OQ-012 | No same-status action until decided |
| OQ-013 | No status on create form |
| OQ-014 | List columns, sort, paging controls |
| OQ-015 | Client-side length hints (backend still validates) |
| OQ-018 | React vs Next.js vs equivalent |
| OQ-019 | Overlapping clicks; no invented lock UI |
| OQ-016 | Not UI (**resolved:** PostgreSQL runtime / H2 tests) |
| OQ-017 | Not UI |

---

## 17. Out of UI scope

Login, roles, email, attachments, dashboards, SLA, ticket delete, comment edit/delete, inventing extra screens.

---

## 18. Requirement traceability

| REQ | UI |
|---|---|
| REQ-001 | §5 V-CREATE |
| REQ-002 | §4 V-LIST |
| REQ-003 | §6 V-DETAIL |
| REQ-004–REQ-007 | §7 |
| REQ-008 | §8 |
| REQ-009 | §10 |
| REQ-010 | §11 |
| REQ-011, REQ-015–REQ-017 | §9, §15 |
| REQ-013 | Create shows `OPEN`; no status picker |
| REQ-016, REQ-019, REQ-028 | `409` on V-DETAIL |
| REQ-021–REQ-024 | Validation on forms |
| REQ-025, REQ-029 | V-NOT-FOUND |
| REQ-027, REQ-030–REQ-032, REQ-047 | §3, §12 |
| REQ-040, REQ-042, REQ-049 | §14 client of REST |
| REQ-045 | No login; no secrets on screen |

---

## 19. Existing repository

No frontend exists. This phase does not add one.
