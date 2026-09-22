# Requirements

**Product:** Support Ticket Management System  
**Status:** Phase 2 — Business requirements (authorized source of observable behavior)  
**Date:** 2026-09-17  
**Scope:** Observable system behavior only. Implementation choices (tables, annotations, class names, SQL, component structure, dependency versions) belong to later phases.

This document does not authorize application code changes.

---

## 1. Problem statement

Users need to create, find, inspect, update, comment on, and progress support tickets through a defined lifecycle. The backend must persist ticket data, validate input, reject illegal status changes, and return errors that the UI can show in a meaningful way.

## 2. Actors

| Actor | Meaning |
|---|---|
| User | A person using the system through the UI to work with tickets and comments. Authentication and authorization are **out of scope**. |
| Backend | The server-side system that validates input, enforces the ticket lifecycle, persists data, and exposes a REST API. |
| UI | The user-facing application that presents tickets, accepts input, and displays meaningful errors. |

No named roles (agent, admin, customer) are defined.

## 3. Known concepts

These are the only ticket and comment concepts in scope. Do not add others unless a later approved requirements change says so.

**Ticket**

- title
- description
- priority
- status
- assignee

The system also assigns a **unique ticket identity** so a ticket can be listed, opened, updated, commented on, searched, and filtered. Identity is a **system-generated UUID**, unique among tickets, and exposed through the API as a **string**.

**Comment**

- content
- author
- creation time

A comment belongs to one ticket.

**Status values**

- `OPEN`
- `IN_PROGRESS`
- `RESOLVED`
- `CLOSED`
- `CANCELLED`

## 4. Out of scope

The following must **not** be treated as requirements:

- Authentication, login, sessions, or authorization
- Email or other notifications
- Attachments
- Dashboards, reporting, or analytics
- SLA management
- Audit workflows or history timelines beyond what is needed to show current ticket fields and comments
- Ticket deletion
- Editing or deleting comments
- Categories, tags, due dates, ticket timestamps, or other unlisted fields

---

# Functional Requirements

## REQ-001 — Create a ticket

| Field | Content |
|---|---|
| **Requirement ID** | REQ-001 |
| **Requirement name** | Create a ticket |
| **Description** | A user can create a new support ticket using the known ticket concepts. |
| **Actor** | User |
| **Preconditions** | The system is available. |
| **Main behavior** | The user submits a new ticket. The backend validates the input, stores the ticket, assigns a unique identity, and sets status to `OPEN`. The user can subsequently list, view, and work with that ticket. |
| **Acceptance criteria** | AC-001-1: After a valid create, the ticket exists and can be viewed by its identity. AC-001-2: A newly created ticket has status `OPEN`. AC-001-3: Submitted title, description, priority, and assignee are stored as provided when those values are supplied and valid. AC-001-4: An invalid create does not produce a ticket. |
| **Validation rules** | Title must be present and not blank (VAL-001). Other create-field optionality is an open question. Status on create is not chosen by the user; it is `OPEN`. |
| **Error scenarios** | Invalid or incomplete create input is rejected by the backend. The UI shows a meaningful error. No ticket is created. |
| **Priority** | Must |
| **Dependencies** | REQ-012, REQ-013, REQ-020, REQ-034 |

## REQ-002 — List tickets

| Field | Content |
|---|---|
| **Requirement ID** | REQ-002 |
| **Requirement name** | List tickets |
| **Description** | A user can see the tickets known to the system. |
| **Actor** | User |
| **Preconditions** | The system is available. Zero or more tickets may exist. |
| **Main behavior** | The user requests the ticket list. The backend returns the current tickets. The UI presents them so the user can open a ticket’s details. |
| **Acceptance criteria** | AC-002-1: When no tickets exist, the user sees an empty list, not a failure. AC-002-2: When tickets exist, each created ticket appears in the list. AC-002-3: Listing does not change ticket data. |
| **Validation rules** | None beyond system availability. |
| **Error scenarios** | If the list cannot be retrieved, the UI shows a meaningful error and does not show a fabricated list. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-034 |

## REQ-003 — View ticket details

| Field | Content |
|---|---|
| **Requirement ID** | REQ-003 |
| **Requirement name** | View ticket details |
| **Description** | A user can open a single ticket and see its known fields and comments. |
| **Actor** | User |
| **Preconditions** | A ticket identity is known. |
| **Main behavior** | The user requests details for that identity. The backend returns the ticket’s title, description, priority, status, assignee, identity, and comments (each with content, author, and creation time). |
| **Acceptance criteria** | AC-003-1: For an existing ticket, all known ticket fields are visible. AC-003-2: Comments on that ticket are visible with content, author, and creation time. AC-003-3: Viewing does not change ticket or comment data. |
| **Validation rules** | The identity must refer to an existing ticket. |
| **Error scenarios** | Unknown identity is rejected as not found. The UI shows a meaningful error and does not present another ticket’s data as if it were this one. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-008, REQ-025, REQ-029 |

## REQ-004 — Update ticket title

| Field | Content |
|---|---|
| **Requirement ID** | REQ-004 |
| **Requirement name** | Update ticket title |
| **Description** | A user can change an existing ticket’s title. |
| **Actor** | User |
| **Preconditions** | The ticket exists. |
| **Main behavior** | The user submits a new title. The backend validates it and replaces the stored title. Later views and lists show the new title. |
| **Acceptance criteria** | AC-004-1: After a valid title update, view/list show the new title. AC-004-2: Other ticket fields remain unchanged. AC-004-3: An invalid title update leaves the previous title unchanged. |
| **Validation rules** | New title must be present and not blank (VAL-001). |
| **Error scenarios** | Blank/missing title is rejected. Unknown ticket identity is not found. The UI shows a meaningful error. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-020, REQ-036 |

## REQ-005 — Update ticket description

| Field | Content |
|---|---|
| **Requirement ID** | REQ-005 |
| **Requirement name** | Update ticket description |
| **Description** | A user can change an existing ticket’s description. |
| **Actor** | User |
| **Preconditions** | The ticket exists. |
| **Main behavior** | The user submits a new description. The backend validates it and replaces the stored description. |
| **Acceptance criteria** | AC-005-1: After a valid description update, details show the new description. AC-005-2: Other ticket fields remain unchanged. AC-005-3: A rejected update leaves the previous description unchanged. |
| **Validation rules** | Input is validated on the backend. Whether a blank description is allowed is an open question. |
| **Error scenarios** | Invalid description input is rejected. Unknown ticket identity is not found. The UI shows a meaningful error. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-020, REQ-036 |

## REQ-006 — Update ticket priority

| Field | Content |
|---|---|
| **Requirement ID** | REQ-006 |
| **Requirement name** | Update ticket priority |
| **Description** | A user can change an existing ticket’s priority. |
| **Actor** | User |
| **Preconditions** | The ticket exists. |
| **Main behavior** | The user submits a new priority. The backend accepts it only if it is a recognized priority value, then stores it. |
| **Acceptance criteria** | AC-006-1: After a valid priority update, details show the new priority. AC-006-2: Other ticket fields remain unchanged. AC-006-3: An unrecognized priority is rejected and the previous priority is unchanged. |
| **Validation rules** | Priority must be a recognized value (VAL-002). The allowed value set is an open question. |
| **Error scenarios** | Unrecognized or missing priority (when required by the update) is rejected. Unknown ticket is not found. The UI shows a meaningful error. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-022, REQ-036 |

## REQ-007 — Update ticket assignee

| Field | Content |
|---|---|
| **Requirement ID** | REQ-007 |
| **Requirement name** | Update ticket assignee |
| **Description** | A user can change an existing ticket’s assignee. |
| **Actor** | User |
| **Preconditions** | The ticket exists. |
| **Main behavior** | The user submits a new assignee. The backend stores it. There is no user directory or permission model in scope. |
| **Acceptance criteria** | AC-007-1: After a valid assignee update, details show the new assignee. AC-007-2: Other ticket fields remain unchanged. AC-007-3: A rejected update leaves the previous assignee unchanged. |
| **Validation rules** | Input is validated on the backend. Whether assignee may be blank/unassigned is an open question. |
| **Error scenarios** | Invalid assignee input is rejected. Unknown ticket is not found. The UI shows a meaningful error. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-020, REQ-036 |

## REQ-008 — Add comments to a ticket

| Field | Content |
|---|---|
| **Requirement ID** | REQ-008 |
| **Requirement name** | Add comments to a ticket |
| **Description** | A user can add a comment to an existing ticket. |
| **Actor** | User |
| **Preconditions** | The ticket exists. |
| **Main behavior** | The user submits comment content and author. The backend validates the input, records creation time, and stores the comment against that ticket. The comment then appears in ticket details. |
| **Acceptance criteria** | AC-008-1: After a valid add, the comment is visible on that ticket with content, author, and creation time. AC-008-2: Existing comments remain. AC-008-3: A rejected add does not store a comment. AC-008-4: A comment is not attached to any other ticket. |
| **Validation rules** | Content must be present and not blank (VAL-003). Author must be present and not blank (VAL-004). Ticket must exist. |
| **Error scenarios** | Blank content or author is rejected. Unknown ticket is not found. The UI shows a meaningful error. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-018, REQ-024, REQ-035 |

## REQ-009 — Search tickets by keyword

| Field | Content |
|---|---|
| **Requirement ID** | REQ-009 |
| **Requirement name** | Search tickets by keyword |
| **Description** | A user can search tickets using a keyword and receive matching tickets. |
| **Actor** | User |
| **Preconditions** | The system is available. Zero or more tickets may exist. |
| **Main behavior** | The user supplies a keyword. The backend returns tickets that match that keyword. Search does not change stored data. How matching is implemented (query mechanism) is not specified here. |
| **Acceptance criteria** | AC-009-1: A search returns only tickets that match the keyword under the (yet to be decided) match rule. AC-009-2: Tickets that do not match are not included. AC-009-3: A keyword with no matches yields an empty result, not a failure. AC-009-4: Search does not modify tickets. |
| **Validation rules** | Keyword input is accepted by the backend. Behavior of a blank keyword is an open question (treat as list-all vs reject). |
| **Error scenarios** | If search cannot be performed, the UI shows a meaningful error. Invalid search input, if rejected, does not return an arbitrary subset. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-002 |

## REQ-010 — Filter tickets by status

| Field | Content |
|---|---|
| **Requirement ID** | REQ-010 |
| **Requirement name** | Filter tickets by status |
| **Description** | A user can restrict the ticket list to a single status. |
| **Actor** | User |
| **Preconditions** | The system is available. |
| **Main behavior** | The user chooses a status value. The backend returns only tickets whose status equals that value. |
| **Acceptance criteria** | AC-010-1: Filtering by `OPEN` returns only `OPEN` tickets. The same holds for `IN_PROGRESS`, `RESOLVED`, `CLOSED`, and `CANCELLED`. AC-010-2: Tickets in other statuses are excluded. AC-010-3: A valid filter with no matching tickets yields an empty result, not a failure. AC-010-4: Filtering does not change ticket data. |
| **Validation rules** | The filter value must be one of the five statuses (VAL-005). |
| **Error scenarios** | An unrecognized status filter is rejected by the backend. The UI shows a meaningful error and does not show an unfiltered list as if the filter succeeded. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-014, REQ-023 |

## REQ-011 — Change ticket status

| Field | Content |
|---|---|
| **Requirement ID** | REQ-011 |
| **Requirement name** | Change ticket status |
| **Description** | A user can request a status change. The backend applies it only when the transition is allowed. This is the observable operation that exercises the lifecycle. |
| **Actor** | User |
| **Preconditions** | The ticket exists and has a current status. |
| **Main behavior** | The user requests a target status. If the transition from current status to target status is allowed, the backend stores the new status. If it is not allowed, the backend rejects the request and leaves status unchanged. |
| **Acceptance criteria** | AC-011-1: Each allowed transition in REQ-015 succeeds and the new status is visible afterward. AC-011-2: Each disallowed transition in REQ-016 is rejected and status is unchanged. AC-011-3: Other ticket fields remain unchanged on both success and rejection. |
| **Validation rules** | Target status must be a recognized status (VAL-005). Transition must be allowed (BR-TRANS). |
| **Error scenarios** | Invalid transition → backend business error; UI shows a meaningful error. Unrecognized status → validation error. Unknown ticket → not found. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-015, REQ-016, REQ-028 |

---

# Business Rules

## REQ-012 — Unique ticket identity

| Field | Content |
|---|---|
| **Requirement ID** | REQ-012 |
| **Requirement name** | Unique ticket identity |
| **Description** | Every stored ticket has an identity that uniquely refers to that ticket. |
| **Actor** | Backend |
| **Preconditions** | A ticket is being created. |
| **Main behavior** | The backend assigns a **UUID** that is unique among tickets and stable for later view, update, comment, search, and filter operations. The API exposes that identity as a **string**. |
| **Acceptance criteria** | AC-012-1: Two different tickets never share an identity. AC-012-2: The same ticket is retrieved by the same identity after restart (REQ-034). AC-012-3: Identity is a system-generated UUID (not client-assigned). |
| **Validation rules** | Identity is system-assigned on create. Clients must not supply the stored id. |
| **Error scenarios** | Operations that supply an unknown identity fail as not found (REQ-025). |
| **Priority** | Must |
| **Dependencies** | REQ-001 |

## REQ-013 — New tickets start as OPEN

| Field | Content |
|---|---|
| **Requirement ID** | REQ-013 |
| **Requirement name** | Initial status is OPEN |
| **Description** | The lifecycle starts at `OPEN`. A create must not place a ticket in another status. |
| **Actor** | Backend |
| **Preconditions** | A valid create is accepted. |
| **Main behavior** | Stored status is `OPEN` regardless of any client-supplied status. |
| **Acceptance criteria** | AC-013-1: Every newly created ticket has status `OPEN`. AC-013-2: A client cannot create a ticket directly as `IN_PROGRESS`, `RESOLVED`, `CLOSED`, or `CANCELLED`. |
| **Validation rules** | Create does not accept a client-chosen status as the stored status. |
| **Error scenarios** | If a create attempt includes a client-chosen non-`OPEN` status, the backend either ignores it and stores `OPEN`, or rejects the create. Which of those two is an open question; bypassing the lifecycle is not allowed. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-014 |

## REQ-014 — Status values

| Field | Content |
|---|---|
| **Requirement ID** | REQ-014 |
| **Requirement name** | Allowed status values |
| **Description** | Ticket status is only one of `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`. |
| **Actor** | Backend |
| **Preconditions** | A ticket exists or a status value is supplied. |
| **Main behavior** | The backend stores and returns only those five values. |
| **Acceptance criteria** | AC-014-1: No ticket is persisted with a status outside the five values. AC-014-2: An unknown status value on a status change or filter is rejected. |
| **Validation rules** | VAL-005 |
| **Error scenarios** | Unknown status → validation error; no status change. |
| **Priority** | Must |
| **Dependencies** | REQ-011, REQ-010 |

## REQ-015 — Allowed status transitions

| Field | Content |
|---|---|
| **Requirement ID** | REQ-015 |
| **Requirement name** | Allowed lifecycle transitions |
| **Description** | Only the following status changes may succeed. |
| **Actor** | Backend |
| **Preconditions** | A status change is requested for an existing ticket. |
| **Main behavior** | The backend applies the new status only when the pair (current → target) is in this list. |

**Allowed transitions**

| From | To |
|---|---|
| `OPEN` | `IN_PROGRESS` |
| `OPEN` | `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED` |
| `IN_PROGRESS` | `CANCELLED` |
| `RESOLVED` | `CLOSED` |

Lifecycle path: `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`, with cancellation from `OPEN` or `IN_PROGRESS` to `CANCELLED`.

| Field | Content |
|---|---|
| **Acceptance criteria** | AC-015-1: Each row in the allowed table succeeds when requested on a ticket in the From status. AC-015-2: After success, stored status equals To. |
| **Validation rules** | Current and target statuses must be recognized. |
| **Error scenarios** | See REQ-016 for all other pairs. |
| **Priority** | Must |
| **Dependencies** | REQ-011, REQ-014 |

## REQ-016 — Invalid status transitions are rejected

| Field | Content |
|---|---|
| **Requirement ID** | REQ-016 |
| **Requirement name** | Invalid transitions rejected |
| **Description** | Any status change that is not in REQ-015 must be rejected by the backend with a business error. The stored status must not change. |
| **Actor** | Backend |
| **Preconditions** | A status change is requested. |
| **Main behavior** | The backend refuses the change and reports a business error. The UI shows that error meaningfully. |

**Invalid transitions (complete set of status-changing pairs not in REQ-015)**

| From | To | Notes |
|---|---|---|
| `OPEN` | `RESOLVED` | Explicitly invalid |
| `OPEN` | `CLOSED` | Explicitly invalid |
| `IN_PROGRESS` | `OPEN` | Not in allowed list |
| `IN_PROGRESS` | `CLOSED` | Not in allowed list |
| `RESOLVED` | `OPEN` | Explicitly invalid |
| `RESOLVED` | `IN_PROGRESS` | Not in allowed list |
| `RESOLVED` | `CANCELLED` | Not in allowed list |
| `CLOSED` | `OPEN` | Explicitly invalid |
| `CLOSED` | `IN_PROGRESS` | Terminal |
| `CLOSED` | `RESOLVED` | Terminal |
| `CLOSED` | `CANCELLED` | Terminal |
| `CANCELLED` | `OPEN` | Explicitly invalid |
| `CANCELLED` | `IN_PROGRESS` | Terminal |
| `CANCELLED` | `RESOLVED` | Terminal |
| `CANCELLED` | `CLOSED` | Terminal |

| Field | Content |
|---|---|
| **Acceptance criteria** | AC-016-1: Each invalid pair is rejected. AC-016-2: After rejection, status equals the status before the request. AC-016-3: The error is a business error (illegal lifecycle change), distinct from a generic technical failure. AC-016-4: The UI displays a meaningful message for the rejection. |
| **Validation rules** | Backend enforces this rule; the UI must not be the only control. |
| **Error scenarios** | Invalid transition; UI shows why the change could not be applied, without stack traces. |
| **Priority** | Must |
| **Dependencies** | REQ-011, REQ-015, REQ-019, REQ-028, REQ-031 |

## REQ-017 — Terminal statuses

| Field | Content |
|---|---|
| **Requirement ID** | REQ-017 |
| **Requirement name** | CLOSED and CANCELLED are terminal |
| **Description** | From `CLOSED` or `CANCELLED`, no status change to a different status is allowed. |
| **Actor** | Backend |
| **Preconditions** | Ticket status is `CLOSED` or `CANCELLED`. |
| **Main behavior** | Any request to move to another status is rejected per REQ-016. |
| **Acceptance criteria** | AC-017-1: A `CLOSED` ticket cannot become `OPEN`, `IN_PROGRESS`, `RESOLVED`, or `CANCELLED`. AC-017-2: A `CANCELLED` ticket cannot become `OPEN`, `IN_PROGRESS`, `RESOLVED`, or `CLOSED`. |
| **Validation rules** | Same as REQ-016. |
| **Error scenarios** | Business error; status unchanged. |
| **Priority** | Must |
| **Dependencies** | REQ-016 |

## REQ-018 — Comments belong to one ticket

| Field | Content |
|---|---|
| **Requirement ID** | REQ-018 |
| **Requirement name** | Comment association |
| **Description** | Each comment is stored against exactly one ticket and is visible only on that ticket’s details. |
| **Actor** | Backend |
| **Preconditions** | A comment is added to an existing ticket. |
| **Main behavior** | The comment is retrievable when that ticket is viewed and not as part of a different ticket. |
| **Acceptance criteria** | AC-018-1: Viewing ticket A never shows comments added to ticket B. AC-018-2: All comments added to ticket A appear when viewing A (subject to later ordering decision). |
| **Validation rules** | Comment cannot be added without a valid ticket identity. |
| **Error scenarios** | Add comment to unknown ticket → not found; no comment stored. |
| **Priority** | Must |
| **Dependencies** | REQ-008, REQ-003 |

## REQ-019 — Lifecycle enforced on the backend

| Field | Content |
|---|---|
| **Requirement ID** | REQ-019 |
| **Requirement name** | Backend enforces the state machine |
| **Description** | Illegal transitions must fail even if the UI is bypassed (for example, a direct API request). |
| **Actor** | Backend |
| **Preconditions** | A status change request reaches the backend. |
| **Main behavior** | The backend is the authority for allowed vs invalid transitions. |
| **Acceptance criteria** | AC-019-1: An invalid transition sent directly to the backend is rejected. AC-019-2: A client cannot persist a new status by skipping backend lifecycle checks. |
| **Validation rules** | REQ-015 and REQ-016 apply to every status change. |
| **Error scenarios** | Business error for invalid transition. |
| **Priority** | Must |
| **Dependencies** | REQ-016, REQ-040 |

---

# Validation Requirements

Validation IDs (`VAL-*`) are business rules. They do not specify Bean Validation annotations or other implementation.

## REQ-020 — Backend validates mutating input

| Field | Content |
|---|---|
| **Requirement ID** | REQ-020 |
| **Requirement name** | Backend input validation |
| **Description** | Create, field updates, comments, status changes, and status filters are validated by the backend before data changes. |
| **Actor** | Backend |
| **Preconditions** | A write or filtered-read with user-supplied values is received. |
| **Main behavior** | Invalid input is rejected. Invalid requests do not partially apply. |
| **Acceptance criteria** | AC-020-1: Invalid input never persists a new or changed ticket/comment. AC-020-2: Validation failures produce a validation error the UI can display. AC-020-3: UI-only checks are not sufficient; backend rejection still occurs if the UI is bypassed. |
| **Validation rules** | VAL-001 through VAL-005 and open field-optionality questions. |
| **Error scenarios** | Validation error; meaningful UI message. |
| **Priority** | Must |
| **Dependencies** | REQ-031, REQ-037 |

## REQ-021 — Title is required and not blank

| Field | Content |
|---|---|
| **Requirement ID** | REQ-021 |
| **Requirement name** | Title validation |
| **Description** | A ticket title must be present and not blank on create and on title update. |
| **Actor** | Backend |
| **Preconditions** | Create or title update is requested. |
| **Main behavior** | Blank, missing, or whitespace-only titles are rejected. |
| **Acceptance criteria** | AC-021-1: Create without a usable title is rejected; no ticket is stored. AC-021-2: Title update to a blank title is rejected; previous title remains. |
| **Validation rules** | VAL-001: title present and not blank. Length limits are an open question. |
| **Error scenarios** | Validation error naming the title problem. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-004 |

## REQ-022 — Priority must be a recognized value

| Field | Content |
|---|---|
| **Requirement ID** | REQ-022 |
| **Requirement name** | Priority validation |
| **Description** | When priority is supplied, it must be one of the recognized priority values. |
| **Actor** | Backend |
| **Preconditions** | Create or priority update includes a priority. |
| **Main behavior** | Unrecognized priority is rejected. The recognized set is an open question. |
| **Acceptance criteria** | AC-022-1: An unrecognized priority is rejected. AC-022-2: A recognized priority is stored. |
| **Validation rules** | VAL-002 |
| **Error scenarios** | Validation error for priority. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-006 |

## REQ-023 — Status values validated

| Field | Content |
|---|---|
| **Requirement ID** | REQ-023 |
| **Requirement name** | Status value validation |
| **Description** | Status change targets and status filters must be one of the five statuses. |
| **Actor** | Backend |
| **Preconditions** | A status value is supplied by the user. |
| **Main behavior** | Unknown values are rejected before any filter result or status write. |
| **Acceptance criteria** | AC-023-1: Unknown status on change is rejected; ticket status unchanged. AC-023-2: Unknown status filter is rejected; the UI does not treat it as success. |
| **Validation rules** | VAL-005 |
| **Error scenarios** | Validation error, distinct from an illegal-but-recognized transition (REQ-016). |
| **Priority** | Must |
| **Dependencies** | REQ-010, REQ-011, REQ-014 |

## REQ-024 — Comment content and author required

| Field | Content |
|---|---|
| **Requirement ID** | REQ-024 |
| **Requirement name** | Comment field validation |
| **Description** | Comment content and author must be present and not blank. Creation time is set by the system. |
| **Actor** | Backend |
| **Preconditions** | Add-comment is requested. |
| **Main behavior** | Missing/blank content or author is rejected. Creation time is not a user-edited business field. |
| **Acceptance criteria** | AC-024-1: Blank content is rejected; no comment stored. AC-024-2: Blank author is rejected; no comment stored. AC-024-3: A valid comment has a creation time assigned by the system. |
| **Validation rules** | VAL-003, VAL-004. Length limits are an open question. |
| **Error scenarios** | Validation error identifying content and/or author. |
| **Priority** | Must |
| **Dependencies** | REQ-008 |

## REQ-025 — Unknown ticket identity

| Field | Content |
|---|---|
| **Requirement ID** | REQ-025 |
| **Requirement name** | Unknown ticket is not found |
| **Description** | View, update, comment, and status change against an identity that does not exist are rejected as not found. |
| **Actor** | Backend |
| **Preconditions** | Caller supplies a ticket identity. |
| **Main behavior** | No create-on-missing, and no silent no-op that looks like success. |
| **Acceptance criteria** | AC-025-1: View of unknown identity → not-found error. AC-025-2: Update/comment/status change of unknown identity → not-found error and no new data. |
| **Validation rules** | Identity must match an existing ticket. |
| **Error scenarios** | Not-found error; UI shows a meaningful message. |
| **Priority** | Must |
| **Dependencies** | REQ-003, REQ-004, REQ-005, REQ-006, REQ-007, REQ-008, REQ-011 |

## REQ-026 — Validate before persist

| Field | Content |
|---|---|
| **Requirement ID** | REQ-026 |
| **Requirement name** | Reject invalid input before changing data |
| **Description** | Validation and lifecycle checks happen before stored ticket or comment data changes. |
| **Actor** | Backend |
| **Preconditions** | A mutating request is invalid or an illegal transition. |
| **Main behavior** | Stored data remains as before the request. |
| **Acceptance criteria** | AC-026-1: Failed validation leaves data unchanged. AC-026-2: Failed transition leaves status and other fields unchanged. |
| **Validation rules** | Applies to all mutating requirements. |
| **Error scenarios** | Validation or business error; no partial write. |
| **Priority** | Must |
| **Dependencies** | REQ-016, REQ-020, REQ-037 |

---

# Error Handling Requirements

## REQ-027 — Backend validation errors

| Field | Content |
|---|---|
| **Requirement ID** | REQ-027 |
| **Requirement name** | Validation errors are identifiable |
| **Description** | When input is invalid, the backend returns a validation error that identifies the problem clearly enough for the UI to show it. |
| **Actor** | Backend |
| **Preconditions** | Input fails validation. |
| **Main behavior** | The error is distinguishable from not-found, illegal transition, and unexpected technical failure. |
| **Acceptance criteria** | AC-027-1: The UI can tell the user that input was invalid. AC-027-2: Where a specific field failed (title, priority, comment content, status filter), the user can understand which input to fix. |
| **Validation rules** | N/A (error shape details belong to a later API contract). |
| **Error scenarios** | Missing title, blank comment, unknown priority, unknown status filter. |
| **Priority** | Must |
| **Dependencies** | REQ-020, REQ-031 |

## REQ-028 — Backend business error for illegal transition

| Field | Content |
|---|---|
| **Requirement ID** | REQ-028 |
| **Requirement name** | Illegal transition business error |
| **Description** | An invalid status transition is rejected with a business error, not stored, and not treated as success. |
| **Actor** | Backend |
| **Preconditions** | Requested (current → target) is not in REQ-015. |
| **Main behavior** | Backend reports a business error. Status unchanged. |
| **Acceptance criteria** | AC-028-1: Illegal transition produces a business error. AC-028-2: That error is not a silent success. AC-028-3: The UI presents it as a lifecycle/status problem. |
| **Validation rules** | REQ-016 |
| **Error scenarios** | Examples: `CLOSED` → `OPEN`; `RESOLVED` → `OPEN`; `CANCELLED` → `OPEN`; `OPEN` → `RESOLVED`; `OPEN` → `CLOSED`. |
| **Priority** | Must |
| **Dependencies** | REQ-016, REQ-031 |

## REQ-029 — Not-found errors

| Field | Content |
|---|---|
| **Requirement ID** | REQ-029 |
| **Requirement name** | Not-found errors |
| **Description** | Missing tickets produce a not-found error. |
| **Actor** | Backend |
| **Preconditions** | Identity does not exist. |
| **Main behavior** | Backend reports not found. UI shows a meaningful message. |
| **Acceptance criteria** | AC-029-1: The user can tell the ticket was not found. AC-029-2: The UI does not display an empty ticket as if it were valid details. |
| **Validation rules** | REQ-025 |
| **Error scenarios** | View/update/comment/status on unknown identity. |
| **Priority** | Must |
| **Dependencies** | REQ-025, REQ-031 |

## REQ-030 — Unexpected failures

| Field | Content |
|---|---|
| **Requirement ID** | REQ-030 |
| **Requirement name** | Unexpected errors are safe |
| **Description** | If the system fails unexpectedly, the user sees a meaningful generic error. Internal details are not shown. |
| **Actor** | Backend, UI |
| **Preconditions** | An unexpected technical failure occurs. |
| **Main behavior** | Operation is not reported as successful. No stack traces, SQL, or secrets appear in the UI. |
| **Acceptance criteria** | AC-030-1: Unexpected failure is visible as an error. AC-030-2: UI message does not include stack traces, connection strings, or secrets. |
| **Validation rules** | N/A |
| **Error scenarios** | Persistence unavailable or other unexpected failure. |
| **Priority** | Must |
| **Dependencies** | REQ-031, REQ-045 |

## REQ-031 — Meaningful UI errors

| Field | Content |
|---|---|
| **Requirement ID** | REQ-031 |
| **Requirement name** | Display meaningful errors in the UI |
| **Description** | When the backend rejects or fails an operation, the UI tells the user what happened in language they can act on. |
| **Actor** | UI, User |
| **Preconditions** | Backend returned a validation error, business error, not-found error, or unexpected error. |
| **Main behavior** | The UI shows a human-readable message appropriate to the error class. It does not fail silently or appear successful. |
| **Acceptance criteria** | AC-031-1: Invalid create/update/comment shows an input error in the UI. AC-031-2: Illegal status change shows a lifecycle/status error in the UI. AC-031-3: Unknown ticket shows a not-found error in the UI. AC-031-4: Unexpected failure shows a meaningful error, not a blank screen with no explanation. AC-031-5: The UI does not show stack traces or internal diagnostics. |
| **Validation rules** | Messages must match the actual backend outcome. |
| **Error scenarios** | All error scenarios on REQ-001 through REQ-011. |
| **Priority** | Must |
| **Dependencies** | REQ-027, REQ-028, REQ-029, REQ-030, REQ-042 |

## REQ-032 — UI success only after backend success

| Field | Content |
|---|---|
| **Requirement ID** | REQ-032 |
| **Requirement name** | UI does not claim success on failure |
| **Description** | The UI reports success only when the backend accepted the operation. |
| **Actor** | UI |
| **Preconditions** | User submitted a mutating action. |
| **Main behavior** | Failed backend results stay in an error state; displayed data is not updated as if the write succeeded. |
| **Acceptance criteria** | AC-032-1: After a rejected title update, the UI still shows the previous title (once refreshed from backend truth). AC-032-2: After a rejected transition, the UI still shows the previous status. |
| **Validation rules** | N/A |
| **Error scenarios** | Any rejected mutation. |
| **Priority** | Must |
| **Dependencies** | REQ-031, REQ-026 |

---

# Persistence Requirements

## REQ-033 — Persist ticket data

| Field | Content |
|---|---|
| **Requirement ID** | REQ-033 |
| **Requirement name** | Persist ticket data |
| **Description** | Accepted tickets are stored so they are not only held in memory for a single process lifetime. |
| **Actor** | Backend |
| **Preconditions** | A ticket create or update was accepted. |
| **Main behavior** | Ticket fields are written to durable storage. |
| **Acceptance criteria** | AC-033-1: An accepted ticket remains available after the application process is stopped and started again. |
| **Validation rules** | Only valid, accepted data is persisted. |
| **Error scenarios** | If persistence fails, the operation is an error; the UI does not report success. |
| **Priority** | Must |
| **Dependencies** | REQ-001, REQ-041 |

## REQ-034 — Tickets survive restart

| Field | Content |
|---|---|
| **Requirement ID** | REQ-034 |
| **Requirement name** | Tickets survive application restart |
| **Description** | After restart, previously accepted tickets are still listable and viewable with the same identity and fields. |
| **Actor** | Backend |
| **Preconditions** | One or more tickets were accepted, then the application restarted. |
| **Main behavior** | Stored tickets are reloaded from durable storage. |
| **Acceptance criteria** | AC-034-1: Title, description, priority, status, assignee, and identity match pre-restart values. AC-034-2: Tickets do not disappear solely because of a restart. |
| **Validation rules** | N/A |
| **Error scenarios** | If data cannot be read after restart, the UI shows a meaningful error rather than an empty system that looks newly initialized when data should exist. |
| **Priority** | Must |
| **Dependencies** | REQ-033 |

## REQ-035 — Comments survive restart

| Field | Content |
|---|---|
| **Requirement ID** | REQ-035 |
| **Requirement name** | Comments survive application restart |
| **Description** | Accepted comments remain on their tickets after restart, including content, author, and creation time. |
| **Actor** | Backend |
| **Preconditions** | Comments were accepted, then the application restarted. |
| **Main behavior** | Comments are reloaded with their ticket association. |
| **Acceptance criteria** | AC-035-1: All previously accepted comments are visible on the same tickets after restart. AC-035-2: Content, author, and creation time are unchanged. |
| **Validation rules** | N/A |
| **Error scenarios** | Persistence read failure → meaningful error. |
| **Priority** | Must |
| **Dependencies** | REQ-008, REQ-033 |

## REQ-036 — Updates persist

| Field | Content |
|---|---|
| **Requirement ID** | REQ-036 |
| **Requirement name** | Field and status updates persist |
| **Description** | Accepted changes to title, description, priority, assignee, and status remain after restart. |
| **Actor** | Backend |
| **Preconditions** | An update or allowed status change was accepted. |
| **Main behavior** | The new values are durably stored. |
| **Acceptance criteria** | AC-036-1: After restart, the last accepted values are the values returned by view. |
| **Validation rules** | Rejected updates are not persisted (REQ-037). |
| **Error scenarios** | Persistence failure → error, no false success. |
| **Priority** | Must |
| **Dependencies** | REQ-004, REQ-005, REQ-006, REQ-007, REQ-011, REQ-033 |

## REQ-037 — Failed operations do not persist invalid state

| Field | Content |
|---|---|
| **Requirement ID** | REQ-037 |
| **Requirement name** | No persistence of rejected work |
| **Description** | Validation failures and illegal transitions must not leave stored data in the requested invalid state. |
| **Actor** | Backend |
| **Preconditions** | A mutating request is rejected. |
| **Main behavior** | Durable storage still reflects the last accepted state. |
| **Acceptance criteria** | AC-037-1: After a rejected transition, restart still shows the old status. AC-037-2: After a rejected create, restart does not show that ticket. |
| **Validation rules** | REQ-016, REQ-020 |
| **Error scenarios** | Rejection remains a rejection after restart. |
| **Priority** | Must |
| **Dependencies** | REQ-016, REQ-026, REQ-034 |

---

# Non-Functional Requirements

These constrain platform and quality. They do not choose class names, annotations, table layouts, or UI components.

## REQ-038 — Java 21 target runtime

| Field | Content |
|---|---|
| **Requirement ID** | REQ-038 |
| **Requirement name** | Java 21 as the target runtime |
| **Description** | The intended runtime for the implemented system is Java 21. |
| **Actor** | Backend |
| **Preconditions** | Implementation phases have started (not this phase). |
| **Main behavior** | The running backend targets Java 21. The existing repository currently configures Java 17; changing that is a later, explicit decision and must not happen in the requirements phase. |
| **Acceptance criteria** | AC-038-1: When implementation is complete, the backend is specified and built to run on Java 21. AC-038-2: The upgrade is not performed during specification-only phases. |
| **Validation rules** | N/A |
| **Error scenarios** | N/A |
| **Priority** | Must (target); deferred change |
| **Dependencies** | Phase 1 assessment (current Java 17) |

## REQ-039 — Spring Boot

| Field | Content |
|---|---|
| **Requirement ID** | REQ-039 |
| **Requirement name** | Spring Boot backend |
| **Description** | The backend is a Spring Boot application. Exact version, starters, and packaging are later decisions. The current repository has no Spring Boot dependency; adding it is not authorized in this phase. |
| **Actor** | Backend |
| **Preconditions** | Implementation phases. |
| **Main behavior** | Ticket operations are provided by a Spring Boot backend. |
| **Acceptance criteria** | AC-039-1: Implemented backend is Spring Boot. AC-039-2: Spring Boot is not added during this requirements phase. |
| **Validation rules** | N/A |
| **Error scenarios** | N/A |
| **Priority** | Must (target); deferred change |
| **Dependencies** | REQ-040 |

## REQ-040 — REST API

| Field | Content |
|---|---|
| **Requirement ID** | REQ-040 |
| **Requirement name** | REST API |
| **Description** | Ticket and comment operations are available through a REST API. Paths, methods, and payload shapes are defined in a later API contract. |
| **Actor** | Backend |
| **Preconditions** | Implementation after API contract. |
| **Main behavior** | The UI uses the REST API. Backend rules apply to API requests, including those not sent by the UI. |
| **Acceptance criteria** | AC-040-1: Each functional behavior is reachable via the REST API once the contract exists. AC-040-2: Invalid transitions and invalid input are rejected by that API. |
| **Validation rules** | N/A |
| **Error scenarios** | API errors must be usable by the UI (REQ-031). |
| **Priority** | Must |
| **Dependencies** | REQ-019, REQ-049 |

## REQ-041 — PostgreSQL / H2 persistence

| Field | Content |
|---|---|
| **Requirement ID** | REQ-041 |
| **Requirement name** | Relational persistence (PostgreSQL / H2) |
| **Description** | Durable storage uses a relational database in the PostgreSQL and/or H2 family. **Recorded (OQ-016):** PostgreSQL for development/runtime; H2 for automated tests. Access style **recorded (not an OQ):** Spring Data JPA / Hibernate. Schema remains an implementation concern. |
| **Actor** | Backend |
| **Preconditions** | Implementation after data-model phase. |
| **Main behavior** | Accepted tickets and comments are stored in that database so they survive restart. |
| **Acceptance criteria** | AC-041-1: Persistence requirements REQ-033–REQ-037 are met using PostgreSQL for development/runtime and H2 for automated tests. AC-041-2: Schema is not defined in this document. |
| **Validation rules** | N/A |
| **Error scenarios** | Database unavailability → unexpected error, no false success. |
| **Priority** | Must |
| **Dependencies** | REQ-033 |

## REQ-042 — React / Next.js or equivalent UI

| Field | Content |
|---|---|
| **Requirement ID** | REQ-042 |
| **Requirement name** | Frontend technology |
| **Description** | Users interact through a React, Next.js, or equivalent frontend. Screen design is a later UI-flow phase. |
| **Actor** | UI |
| **Preconditions** | Frontend implementation phases. |
| **Main behavior** | The UI supports the functional behaviors and meaningful errors. |
| **Acceptance criteria** | AC-042-1: Users can perform REQ-001–REQ-011 through the UI. AC-042-2: REQ-031 is satisfied in that UI. |
| **Validation rules** | N/A |
| **Error scenarios** | UI error display per REQ-031. |
| **Priority** | Must |
| **Dependencies** | REQ-031, REQ-040 |

## REQ-043 — Maintainability

| Field | Content |
|---|---|
| **Requirement ID** | REQ-043 |
| **Requirement name** | Maintainability |
| **Description** | Behavior stays aligned with this specification so later changes remain reviewable. |
| **Actor** | Backend, UI |
| **Preconditions** | Implementation and documentation phases. |
| **Main behavior** | Structure and naming follow later architecture without extra unrequested features. |
| **Acceptance criteria** | AC-043-1: Implemented behavior traces to a REQ-ID. AC-043-2: Unrequested features from the out-of-scope list are absent. |
| **Validation rules** | N/A |
| **Error scenarios** | N/A |
| **Priority** | Should |
| **Dependencies** | `docs/traceability.md` |

## REQ-044 — Testability

| Field | Content |
|---|---|
| **Requirement ID** | REQ-044 |
| **Requirement name** | Testability |
| **Description** | Each Must requirement has a verifiable outcome (see traceability). Lifecycle, validation, search, filter, persistence-after-restart, and UI errors are testable without relying on unstated features. |
| **Actor** | Backend, UI |
| **Preconditions** | Test-strategy and testing phases. |
| **Main behavior** | Tests can observe success and rejection without production secrets. |
| **Acceptance criteria** | AC-044-1: Allowed and invalid transitions can be verified independently. AC-044-2: Persistence-after-restart can be verified. AC-044-3: Tests are not claimed passed unless run. |
| **Validation rules** | N/A |
| **Error scenarios** | N/A |
| **Priority** | Must |
| **Dependencies** | `spec/test-strategy.md` (later) |

## REQ-045 — Security

| Field | Content |
|---|---|
| **Requirement ID** | REQ-045 |
| **Requirement name** | Security (no invented auth) |
| **Description** | Secrets must not be committed to Git. Input is validated. Errors must not leak secrets or internal internals. Authentication and authorization are **not** required in this phase. |
| **Actor** | Backend, UI |
| **Preconditions** | Any configuration or logging. |
| **Main behavior** | Credentials, tokens, and private keys are not stored in the repository. User input is not trusted without backend validation. |
| **Acceptance criteria** | AC-045-1: No secrets in Git. AC-045-2: Error output does not include secrets. AC-045-3: Login/roles are not implemented as if they were required. |
| **Validation rules** | REQ-020 |
| **Error scenarios** | N/A |
| **Priority** | Must |
| **Dependencies** | REQ-020, REQ-050 |

## REQ-046 — Input validation (quality)

| Field | Content |
|---|---|
| **Requirement ID** | REQ-046 |
| **Requirement name** | Input validation as a quality attribute |
| **Description** | Backend validation is mandatory for correctness, not optional UI convenience. |
| **Actor** | Backend |
| **Preconditions** | Mutating and filter inputs. |
| **Main behavior** | Same as REQ-020, stated as a non-functional quality bar. |
| **Acceptance criteria** | AC-046-1: Bypassing the UI still meets VAL-* rules. |
| **Validation rules** | VAL-001–VAL-005 |
| **Error scenarios** | Validation errors. |
| **Priority** | Must |
| **Dependencies** | REQ-020 |

## REQ-047 — Error handling consistency

| Field | Content |
|---|---|
| **Requirement ID** | REQ-047 |
| **Requirement name** | Consistent error handling |
| **Description** | Validation, not-found, illegal transition, and unexpected failures remain distinguishable from each other across operations. |
| **Actor** | Backend, UI |
| **Preconditions** | Any failed operation. |
| **Main behavior** | Error class is stable enough for the UI to show the right kind of message. Exact payload schema is later. |
| **Acceptance criteria** | AC-047-1: Illegal transition is not presented as a field-format issue. AC-047-2: Not-found is not presented as a successful empty ticket. |
| **Validation rules** | N/A |
| **Error scenarios** | Mixed failure types. |
| **Priority** | Must |
| **Dependencies** | REQ-027, REQ-028, REQ-029, REQ-030 |

## REQ-048 — Persistence quality

| Field | Content |
|---|---|
| **Requirement ID** | REQ-048 |
| **Requirement name** | Durable persistence |
| **Description** | Durability in REQ-033–REQ-037 is a quality requirement, not an in-memory demo. |
| **Actor** | Backend |
| **Preconditions** | Accepted writes. |
| **Main behavior** | Restart test is a required verification, not optional. |
| **Acceptance criteria** | AC-048-1: Restart verification is part of planned verification for persistence REQs. |
| **Validation rules** | N/A |
| **Error scenarios** | Loss of accepted data after restart is a defect. |
| **Priority** | Must |
| **Dependencies** | REQ-034, REQ-035, REQ-041 |

## REQ-049 — API consistency

| Field | Content |
|---|---|
| **Requirement ID** | REQ-049 |
| **Requirement name** | API consistency |
| **Description** | REST operations use consistent resource meaning, error classes, and field names once the API contract exists. |
| **Actor** | Backend |
| **Preconditions** | API contract phase completed. |
| **Main behavior** | Clients can rely on stable names for title, description, priority, status, assignee, and comment content/author/creation time. |
| **Acceptance criteria** | AC-049-1: Known concepts are named consistently across operations. AC-049-2: Error classes in REQ-047 are used consistently. |
| **Validation rules** | N/A |
| **Error scenarios** | N/A |
| **Priority** | Must |
| **Dependencies** | REQ-040, later `spec/api-contract.md` |

## REQ-050 — No secrets committed to Git

| Field | Content |
|---|---|
| **Requirement ID** | REQ-050 |
| **Requirement name** | No secrets in Git |
| **Description** | Passwords, API keys, tokens, private keys, and connection credentials must not be committed. |
| **Actor** | Backend, UI, contributors |
| **Preconditions** | Any change that might include configuration. |
| **Main behavior** | Secrets stay outside the repository. |
| **Acceptance criteria** | AC-050-1: Repository history for this work does not add secret files or hardcoded credentials. |
| **Validation rules** | N/A |
| **Error scenarios** | If a secret appears, it must be removed and rotated (operational), not documented with the secret value. |
| **Priority** | Must |
| **Dependencies** | REQ-045 |

---

## Validation rule index

| ID | Rule |
|---|---|
| VAL-001 | Title present and not blank on create and title update. |
| VAL-002 | Priority, when supplied, is a recognized value (set TBD). |
| VAL-003 | Comment content present and not blank. |
| VAL-004 | Comment author present and not blank. |
| VAL-005 | Status values (change target or filter) are one of the five statuses. |

---

## Acceptance criteria (requirement → verifiable outcome)

| REQ | Verifiable outcome |
|---|---|
| REQ-001 | Valid create yields an `OPEN` ticket that can be viewed; invalid create yields none. |
| REQ-002 | All existing tickets can be listed; empty list is not an error. |
| REQ-003 | Details show known ticket fields and comments; unknown id is not found. |
| REQ-004 | Title changes persist; blank title is rejected. |
| REQ-005 | Description changes persist; rejected update leaves old value. |
| REQ-006 | Recognized priority changes persist; unrecognized priority is rejected. |
| REQ-007 | Assignee changes persist; rejected update leaves old value. |
| REQ-008 | Comment appears with content, author, creation time; blank content/author rejected. |
| REQ-009 | Keyword search returns matches only; no matches → empty, not failure. |
| REQ-010 | Status filter returns only that status; unknown filter value rejected. |
| REQ-011 | Allowed transitions succeed; disallowed transitions fail with status unchanged. |
| REQ-012 | Identities are unique and stable across restart. |
| REQ-013 | New tickets are `OPEN`; create cannot skip the lifecycle. |
| REQ-014 | Only five statuses exist in stored data. |
| REQ-015 | The five allowed transitions succeed. |
| REQ-016 | All non-allowed transitions are rejected with a business error. |
| REQ-017 | `CLOSED` and `CANCELLED` cannot move to another status. |
| REQ-018 | Comments stay on the correct ticket. |
| REQ-019 | Lifecycle still enforced if the UI is bypassed. |
| REQ-020 | Backend validates writes; invalid data is not stored. |
| REQ-021 | Blank title cannot be created or saved. |
| REQ-022 | Unrecognized priority cannot be saved. |
| REQ-023 | Unrecognized status cannot be used to change or filter. |
| REQ-024 | Comment requires content and author; system sets creation time. |
| REQ-025 | Unknown identity is not found for view/update/comment/status. |
| REQ-026 | Failed requests leave stored data unchanged. |
| REQ-027 | Validation errors are identifiable for the UI. |
| REQ-028 | Illegal transition is a business error. |
| REQ-029 | Missing ticket is a not-found error. |
| REQ-030 | Unexpected errors do not leak internals. |
| REQ-031 | UI shows meaningful messages for validation, lifecycle, not-found, and unexpected errors. |
| REQ-032 | UI does not show success when the backend rejected the action. |
| REQ-033 | Accepted tickets are durably stored. |
| REQ-034 | Tickets remain after restart. |
| REQ-035 | Comments remain after restart. |
| REQ-036 | Accepted field/status updates remain after restart. |
| REQ-037 | Rejected work is still absent after restart. |
| REQ-038 | Implemented system targets Java 21 (change deferred). |
| REQ-039 | Implemented backend is Spring Boot (change deferred). |
| REQ-040 | Operations are exposed as a REST API. |
| REQ-041 | Durability uses PostgreSQL (development/runtime) and H2 (automated tests). |
| REQ-042 | Users work through React/Next.js or equivalent. |
| REQ-043 | Behavior traces to REQ-IDs; out-of-scope features absent. |
| REQ-044 | Outcomes above can be verified by tests later. |
| REQ-045 | No secrets; no invented authentication. |
| REQ-046 | Validation holds without the UI. |
| REQ-047 | Error classes stay distinguishable. |
| REQ-048 | Restart durability is a required quality check. |
| REQ-049 | API uses consistent names and error classes. |
| REQ-050 | Secrets are not committed to Git. |

---

## Requirement Traceability

Map: **REQ-ID → Acceptance Criteria → Planned Verification**

Do not treat these as implementation tasks.

| REQ-ID | Acceptance criteria | Planned verification |
|---|---|---|
| REQ-001 | AC-001-1–4 | Backend create tests; UI create journey (later) |
| REQ-002 | AC-002-1–3 | Backend list tests; UI list empty and non-empty |
| REQ-003 | AC-003-1–3 | Backend get-by-identity tests; UI details |
| REQ-004 | AC-004-1–3 | Backend title update + negative blank title |
| REQ-005 | AC-005-1–3 | Backend description update + rejection unchanged |
| REQ-006 | AC-006-1–3 | Backend priority update + unrecognized value |
| REQ-007 | AC-007-1–3 | Backend assignee update + rejection unchanged |
| REQ-008 | AC-008-1–4 | Backend add-comment + negative blank fields |
| REQ-009 | AC-009-1–4 | Backend search tests once match fields are decided |
| REQ-010 | AC-010-1–4 | Backend filter-by-each-status + unknown status |
| REQ-011 | AC-011-1–3 | State-machine tests for every allowed pair |
| REQ-012 | AC-012-1–3 | Uniqueness test; identity stable after restart; system-generated UUID exposed as API string |
| REQ-013 | AC-013-1–2 | Create always `OPEN`; cannot create in other statuses |
| REQ-014 | AC-014-1–2 | Reject unknown status values |
| REQ-015 | AC-015-1–2 | One test per allowed transition |
| REQ-016 | AC-016-1–4 | One test per invalid pair; UI message check |
| REQ-017 | AC-017-1–2 | Terminal-state tests |
| REQ-018 | AC-018-1–2 | Comments isolated per ticket |
| REQ-019 | AC-019-1–2 | Direct API invalid transition (bypass UI) |
| REQ-020 | AC-020-1–3 | Negative tests without UI |
| REQ-021 | AC-021-1–2 | Blank title create/update |
| REQ-022 | AC-022-1–2 | Unrecognized priority |
| REQ-023 | AC-023-1–2 | Unknown status on change and filter |
| REQ-024 | AC-024-1–3 | Blank comment content/author; creation time present |
| REQ-025 | AC-025-1–2 | Unknown identity on each mutating/read-by-id operation |
| REQ-026 | AC-026-1–2 | Unchanged data after failures |
| REQ-027 | AC-027-1–2 | Error payload distinguishable as validation |
| REQ-028 | AC-028-1–3 | Business error on illegal transition |
| REQ-029 | AC-029-1–2 | Not-found vs empty success |
| REQ-030 | AC-030-1–2 | UI/API must not leak stack traces/secrets |
| REQ-031 | AC-031-1–5 | UI error cases for each error class |
| REQ-032 | AC-032-1–2 | UI still shows previous values after rejection |
| REQ-033 | AC-033-1 | Persistence write of accepted ticket |
| REQ-034 | AC-034-1–2 | Restart then list/view |
| REQ-035 | AC-035-1–2 | Restart then view comments |
| REQ-036 | AC-036-1 | Restart after updates |
| REQ-037 | AC-037-1–2 | Restart after rejected create/transition |
| REQ-038 | AC-038-1–2 | Build/runtime check in implementation phase only |
| REQ-039 | AC-039-1–2 | Implementation-phase check; not now |
| REQ-040 | AC-040-1–2 | API contract tests later |
| REQ-041 | AC-041-1–2 | Integration with PostgreSQL (runtime) and H2 (tests) later |
| REQ-042 | AC-042-1–2 | UI flow / acceptance later |
| REQ-043 | AC-043-1–2 | Review against out-of-scope list |
| REQ-044 | AC-044-1–3 | Test strategy phase; run tests before claiming pass |
| REQ-045 | AC-045-1–3 | Repo review for secrets; no auth feature |
| REQ-046 | AC-046-1 | Same as backend validation tests |
| REQ-047 | AC-047-1–2 | Cross-operation error-class tests |
| REQ-048 | AC-048-1 | Restart included in persistence verification |
| REQ-049 | AC-049-1–2 | API contract review later |
| REQ-050 | AC-050-1 | Repository inspection |

---

## Open Questions

Decisions are **not** invented here. Each unanswered item blocks a later spec from being complete. **OQ-016 is resolved** (see below). Persistence access (Spring Data JPA / Hibernate) is recorded in architecture and is **not** OQ-017.

### OQ-001 — Priority value set

**Question:** What are the allowed priority values?  
**Why it matters:** REQ-006 and REQ-022 cannot be fully tested until the recognized set is known. Wrong values would be accepted or valid values rejected.

### OQ-002 — Required fields on create (other than title)

**Question:** Must description, priority, and assignee be supplied to create a ticket, or may they be omitted/empty?  
**Why it matters:** Determines whether a create with only a title is valid and what the UI must require.

### OQ-003 — Unassigned tickets

**Question:** May assignee be empty, and may an assigned ticket be updated to unassigned?  
**Why it matters:** Affects REQ-007 validation and list/details display.

### OQ-004 — Blank description

**Question:** May description be blank on create or update?  
**Why it matters:** REQ-005 error vs success path.

### OQ-005 — Keyword search match fields and matching rule

**Question:** Does the keyword match title, description, comments, assignee, identity, or a subset? Is matching exact, partial, and/or case-insensitive?  
**Why it matters:** REQ-009 acceptance is otherwise untestable. Must not be answered by choosing a SQL implementation here.

### OQ-006 — Blank keyword

**Question:** Does a blank keyword mean “list all”, or is it rejected?  
**Why it matters:** Search vs list overlap and validation.

### OQ-007 — Combining search and status filter

**Question:** May keyword search and status filter be used together?  
**Why it matters:** Observable result set when both are supplied; later API and UI flow.

### OQ-008 — Field updates on terminal tickets

**Question:** May title, description, priority, or assignee be changed when status is `CLOSED` or `CANCELLED`?  
**Why it matters:** If forbidden, updates need additional business errors; if allowed, tests must allow them.

### OQ-009 — Comments on terminal tickets

**Question:** May comments be added when status is `CLOSED` or `CANCELLED`?  
**Why it matters:** REQ-008 preconditions.

### OQ-010 — Comment author without authentication

**Question:** How is author supplied if there is no login (free-text name vs other rule)?  
**Why it matters:** REQ-008/REQ-024 need a stable rule; do not invent an account system.

### OQ-011 — Comment order

**Question:** Must comments be shown in creation-time order (and in which direction)?  
**Why it matters:** Details screen and tests for REQ-003/REQ-008.

### OQ-012 — Same-status request

**Question:** Is requesting the current status again success (no change), a no-op, or a business error?  
**Why it matters:** Not listed as allowed or invalid in the from→to tables.

### OQ-013 — Client-supplied status on create

**Question:** If create includes a status field, is the request rejected, or is the field ignored and `OPEN` stored?  
**Why it matters:** REQ-013 allows either as long as the lifecycle is not bypassed; API contract needs one behavior.

### OQ-014 — List contents, sort, and pagination

**Question:** Which fields appear in the list, what is the default order, and is pagination required?  
**Why it matters:** REQ-002 observability; do not invent UX here.

### OQ-015 — Field length limits

**Question:** Maximum lengths for title, description, comment content, author, and assignee?  
**Why it matters:** Validation is incomplete without limits, but inventing them would be an unrequested constraint.

### OQ-016 — PostgreSQL vs H2 by environment

**Question:** Which database is used for development, test, and any other environment?  
**Why it matters:** Persistence NFR names both; architecture/data-model must choose without changing requirements semantics.  
**Status:** **Resolved** (human, Phase 16A, 2026-09-21).  
**Answer:** PostgreSQL for development/runtime. H2 for automated tests. The environment-specific database choice is closed.

### OQ-017 — Java 17 existing project vs Java 21 target

**Question:** When and how will the existing Maven Java 17 project move to Java 21?  
**Why it matters:** REQ-038 conflicts with current `pom.xml` until a later phase records the upgrade. This phase must not upgrade.  
**Note:** This OQ is **not** persistence access. JPA/Hibernate is recorded separately and must not be labeled OQ-017. Java 21 / Spring Boot 3.5.5 were approved in Phase 11.

### OQ-018 — React vs Next.js vs equivalent

**Question:** Which frontend stack is required?  
**Why it matters:** REQ-042 allows any of them; UI-flow/architecture should pick one.

### OQ-019 — Concurrent updates

**Question:** What should happen if two updates to the same ticket overlap?  
**Why it matters:** Last-write-wins vs conflict is unspecified; inventing locking would be extra scope.

---

## Assumptions

These are the minimum readings required to make the requested behavior testable. They are not extra features.

1. Changing status is a user-visible operation (REQ-011), because otherwise the lifecycle cannot be exercised.
2. New tickets start as `OPEN` because the stated lifecycle begins at `OPEN`.
3. Any from→to pair not listed as allowed is invalid, including pairs not given as examples.
4. Title is required and non-blank because create/update title are requested and a blank title is not a usable ticket.
5. The system assigns a unique identity because view/update/comment/filter require referring to one ticket.
6. Comment creation time is system-assigned, not a user-edited lifecycle field.
7. Authentication is absent; “author” is a comment field, not a logged-in account.

---

## Related documents

- `docs/requirements-review.md` — completeness, ambiguities, conflicts
- `docs/traceability.md` — requirement → spec → task → implementation → test → review
- `PLAN.md` — Phase 2 complete; Phase 3 not started
- Later: `spec/architecture.md`, `spec/data-model.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/ui-flow.md`, `spec/test-strategy.md`
