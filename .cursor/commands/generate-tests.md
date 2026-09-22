---
description: Generate tests from SDD specs without claiming they passed until they are run
---

# /generate-tests

Generate or update tests from the approved specifications. Do not implement production business functionality unless required for a compiling test of already-implemented code.

## Before generating tests

1. Confirm the current phase in `PLAN.md`. If implementation phases have not started, do not create a fake ticket-system test suite.
2. Read `spec/test-strategy.md`, `spec/requirements.md`, `spec/api-contract.md`, and `spec/state-machine.md`.
3. Inspect existing test layout and `pom.xml` test dependencies. Do not upgrade dependencies unless an approved phase requires it.
4. Inspect the production code under test. Do not read `target/` or generated files.
5. Follow `.cursor/rules/testing.md`.

## Generate

- Unit tests for domain/services already implemented
- Controller tests for HTTP mappings, validation, and errors
- Repository tests for queries and constraints
- Negative tests
- State-machine tests for allowed and rejected transitions
- Regression tests for defects listed in `docs/ai-validation.md`

## Naming and isolation

- Name tests by scenario and expected outcome
- Keep tests independent and repeatable
- Do not depend on execution order or local manual data

## After writing tests

- Run the relevant Maven test command if the user asked to execute them, or if reporting pass/fail
- Never claim tests passed without running them
- If tests were not run, say so
- Update `docs/traceability.md` with requirement → test mapping
- Update `docs/ai-validation.md` when a generated test is a regression for a known AI mistake

## Constraints

- Do not invent endpoints, entities, or behavior that are not in the spec and not in the code.
- Do not modify `Main.java` unless a later approved phase changed the entry point and tests require it.
- Do not add Ticket system production code under the guise of tests.
