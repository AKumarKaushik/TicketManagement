---
description: REST API naming, HTTP, DTO, validation, and compatibility standards
globs: "**/{*Controller.java,*Resource.java,*Dto.java,*DTO.java,*Request.java,*Response.java,spec/api-contract.md}"
alwaysApply: false
---

# API Standards

These rules apply when designing or implementing HTTP APIs. They do not authorize creating endpoints before the API-contract phase.

## REST naming

- Use plural nouns for collections: `/tickets`, not `/getTickets`.
- Use path identifiers for a single resource: `/tickets/{id}`.
- Nest subresources only when they belong to a parent: `/tickets/{id}/comments`.
- Use kebab-case in URLs if multi-word path segments are required.
- Do not encode actions in URLs when an HTTP method already expresses them (`/tickets/{id}/update` is wrong).
- State-changing domain actions may use a dedicated sub-resource only when documented in `spec/api-contract.md`.

## HTTP methods

- `GET`: read, no side effects
- `POST`: create or documented non-idempotent action
- `PUT`: replace a resource when the contract says replace
- `PATCH`: partial update when the contract says patch
- `DELETE`: remove a resource when allowed
- Do not use `GET` for mutations

## HTTP status codes

Use precise statuses:

- `200 OK` — successful read or in-place update
- `201 Created` — successful create, with `Location` when practical
- `204 No Content` — successful delete with no body, if the contract says so
- `400 Bad Request` — malformed or failed field validation
- `401 Unauthorized` — missing/invalid authentication
- `403 Forbidden` — authenticated but not allowed
- `404 Not Found` — resource does not exist
- `409 Conflict` — uniqueness, version, or illegal state conflict
- `422 Unprocessable Entity` — semantically invalid request when distinct from 400 in the contract
- `500 Internal Server Error` — unexpected failure; do not leak internals

## DTOs

- Request and response DTOs are the public contract.
- Do not return JPA entities.
- Include only fields defined in `spec/api-contract.md`.
- Keep names stable once published.

## Validation

- Validate required fields, formats, lengths, and enums at the boundary.
- Fail the whole request on validation errors unless the contract defines partial success.
- Do not trust client-supplied server-owned fields (createdAt, status) unless the contract allows them.

## Error responses

Use one stable error shape for all APIs, defined later in `spec/api-contract.md`. Until then, plan for:

- timestamp or correlation id
- HTTP status
- error code
- message safe for clients
- field errors for validation

Do not return stack traces or SQL to clients.

## Search / filter conventions

When search is specified:

- Collection `GET` supports filter, sort, and pagination query parameters
- Unknown filter values return `400` unless the contract says ignore them
- Pagination is explicit (`page`, `size` or documented equivalents)
- Default sort and page size are documented
- Filtering must not bypass authorization

## Backward compatibility

- Do not remove or rename fields from published responses without a versioned change.
- Adding optional fields is preferred over breaking changes.
- Treat `spec/api-contract.md` as the compatibility baseline once it exists.
- Additive query parameters must have defaults that preserve old behavior.
