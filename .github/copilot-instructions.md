# GitHub Copilot Instructions

These instructions apply to AI-assisted work in the TicketManagement repository.

## Project posture

This repository is under Spec-Driven Development (SDD).

- Read `PLAN.md` and the relevant files in `spec/` before changing code.
- Inspect existing code before modifying it.
- Do not delete, replace, regenerate, or restructure the existing Maven project.
- Do not implement Support Ticket business functionality until the matching SDD phase is approved.
- Do not modify `src/main/java/com/enterprise/ai/Main.java` unless a later approved phase requires a controlled entry-point change.
- Do not upgrade Java, Spring Boot, or dependencies unless an approved phase explicitly requires it.
- Do not modify `pom.xml` unless absolutely necessary for that approved phase.

## Engineering standards

- Target Java 21 for new Spring Boot work only after the upgrade decision is recorded.
- Use layered architecture: controller, service, repository, domain.
- Use constructor injection. Do not use field injection.
- Keep HTTP DTOs separate from persistence entities.
- Validate input at the API boundary and enforce invariants in the domain/service layer.
- Use consistent exception handling and stable HTTP error responses.
- Design REST APIs with resource names, correct HTTP methods, and precise status codes.
- Keep transactions on service operations that must be atomic.
- Access the database through repositories and parameterized queries only.
- Prefer small, maintainable, testable changes.

## Security

- Never hardcode or commit secrets, tokens, passwords, or private keys.
- Do not disable authentication, TLS, or other security controls to make a sample work.
- Do not introduce permissive CORS or validation bypasses.
- Do not log secrets or sensitive payloads.

## Testing

- Add or update unit, integration, controller, repository, negative, state-machine, and regression tests when behavior changes.
- Keep tests isolated and named by scenario and expected outcome.
- Never claim tests passed unless they were actually run.
- If tests were not run, say so.

## Documentation

- Keep README, architecture, API, setup, and testing docs synchronized with implementation.
- Record prompts in `docs/prompt-history.md`.
- Record AI mistakes in `docs/ai-validation.md`.
- Keep `docs/traceability.md` updated.
- Do not describe unimplemented features as complete.

## Context use

- Read targeted files, not the entire repository.
- Avoid `node_modules/`, `target/`, `build/`, and generated files.
- Retrieve additional context incrementally.
- Never sacrifice correctness to reduce context.
