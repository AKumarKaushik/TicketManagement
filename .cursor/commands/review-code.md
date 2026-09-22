---
description: Review implementation against SDD specs, architecture, API, tests, and security
---

# /review-code

Review the current code changes against the Spec-Driven Development artifacts. Do not implement new business functionality while reviewing unless the user explicitly asks for fixes.

## Before reviewing

1. Read `PLAN.md` and identify the current phase.
2. Read only the relevant files in `spec/` (`requirements.md`, `architecture.md`, `data-model.md`, `api-contract.md`, `state-machine.md`, `ui-flow.md`, `test-strategy.md`).
3. Inspect the existing files being reviewed. Do not read `node_modules/`, `target/`, `build/`, or generated output.
4. Read `.cursor/rules/java-springboot.md`, `.cursor/rules/testing.md`, and `.cursor/rules/api-standards.md` as applicable.

## Review for

- Conformance to approved specifications (not guessed behavior)
- Layered architecture and constructor injection
- DTO usage, validation, and exception mapping
- REST naming, HTTP methods, and status codes
- Transactions and database access
- Security: no secrets, no auth bypass, no unsafe queries
- Logging without sensitive data
- Test coverage of happy path, negatives, and state-machine rules
- Traceability from requirement to test
- Accidental implementation ahead of the current SDD phase

## Output format

- Findings first, grouped as Critical / Important / Minor
- File path and what is wrong
- Spec or rule that it violates
- Recommended fix (do not apply unless asked)
- Tests that are missing
- Explicit statement if no tests were run

## Constraints

- Do not upgrade Java, Spring Boot, or dependencies during review.
- Do not modify `Main.java` or `pom.xml` unless the user asked for a reviewed fix that requires it.
- Never claim tests passed unless they were executed in the session.
