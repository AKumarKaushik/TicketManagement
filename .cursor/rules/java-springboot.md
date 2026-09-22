---
description: Java and Spring Boot engineering standards for this repository
globs: **/*.{java,xml,yml,yaml,properties}
alwaysApply: false
---

# Java / Spring Boot Guidelines

These rules apply when implementing or modifying Java/Spring Boot code. They do not authorize implementation during specification-only phases.

## Language and platform

- Target **Java 21** for new Spring Boot work once an approved later phase decides the upgrade.
- Do not upgrade Java, Spring Boot, or dependencies unless a later approved phase explicitly requires it.
- Do not modify `pom.xml` unless necessary for that approved phase.
- Do not replace, restructure, or delete the existing Maven project.
- Do not modify `src/main/java/com/enterprise/ai/Main.java` unless an approved later phase requires a controlled Spring Boot entry-point change.

## Layered architecture

Keep a clear layered design:

- Controller: HTTP mapping, request validation, DTO mapping, status codes
- Service: business rules, orchestration, transactions
- Repository: persistence only
- Domain/entity: data and invariants, not HTTP concerns

Do not put business logic in controllers or persistence logic in services beyond transaction boundaries.

## Dependency injection

- Use constructor injection.
- Prefer `final` collaborators.
- Do not use field injection (`@Autowired` on fields).
- Do not use `new` for Spring-managed collaborators.

## DTOs

- Keep API request/response types separate from persistence entities.
- Map explicitly between DTOs and domain objects.
- Do not expose entities directly from REST controllers.
- Name DTOs by purpose (`Create...Request`, `...Response`, `...SearchCriteria`).

## Validation

- Validate at the API boundary with Bean Validation annotations on request DTOs.
- Enforce domain invariants in services/domain, not only in controllers.
- Reject invalid input before mutating state.
- Return structured validation errors; do not leak stack traces.

## Exception handling

- Use a consistent API exception model.
- Map domain/validation/not-found/conflict failures to stable HTTP statuses.
- Prefer a global `@ControllerAdvice` / `@RestControllerAdvice` handler.
- Do not swallow exceptions.
- Do not return ad-hoc error strings from controllers.

## REST API design

Follow `.cursor/rules/api-standards.md` and `spec/api-contract.md` when those specs exist.

- Resource-oriented URLs
- Correct HTTP methods and status codes
- Idempotent updates where practical
- No business logic in mapping layers

## Transactions

- Place `@Transactional` on service methods that must be atomic.
- Keep transactions short.
- Use read-only transactions for queries when appropriate.
- Do not open transactions in controllers or repositories unless there is a documented reason.

## Database access

- Access data through repositories.
- Use parameterized queries only.
- Do not concatenate untrusted input into queries.
- Do not introduce a schema, entities, or repositories until the data-model phase is approved.
- Prefer Spring Data and explicit query methods over hidden native SQL.

## Maintainability

- Keep methods small and named for behavior.
- Follow existing package `com.enterprise.ai` unless a later spec changes it.
- Avoid unused dependencies, speculative abstractions, and unrelated refactors.
- Prefer the smallest change that satisfies the approved specification.

## Security

- Never hardcode secrets, tokens, passwords, or keys.
- Never commit `.env`, keystores, or credential files.
- Validate and authorize every mutating operation.
- Do not disable CSRF, TLS, or authentication to make a demo work.
- Do not introduce permissive CORS or security bypasses.
- Log security-relevant failures without logging secrets.

## Logging

- Use a logger, not `System.out`.
- Log at info/warn/error with enough context to diagnose (ids, operation, outcome).
- Do not log passwords, tokens, personal secrets, or full request bodies that may contain secrets.
- Prefer structured, stable log messages.

## Testability

- Write code that can be unit-tested without a full server where practical.
- Inject collaborators; avoid hidden static state.
- Keep side effects behind interfaces or Spring beans.
- Follow `.cursor/rules/testing.md`.

## Spec-driven constraint

Before writing Java code:

1. Read the relevant `spec/` files.
2. Inspect existing code in the files you will change.
3. Implement only what the current approved phase allows.
4. Do not invent Ticket entities, controllers, services, repositories, or endpoints before those phases.
