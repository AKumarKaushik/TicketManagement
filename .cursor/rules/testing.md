---
description: Testing standards for unit, integration, API, and state-machine tests
globs: "**/{src/test/**,*.java}"
alwaysApply: false
---

# Testing Guidelines

Tests prove behavior. They are not optional documentation.

## Never claim tests passed without running them

- Do not state that tests passed, failed, or were skipped unless they were actually executed in this session.
- If tests were not run, say so and explain why.
- Do not invent coverage percentages or green CI results.

## Test types

### Unit tests

- Test one class or behavior in isolation.
- Mock collaborators at trust boundaries, not every trivial value object.
- Cover business rules, mapping, and validation logic.

### Integration tests

- Test Spring context, persistence, and real wiring where the behavior depends on them.
- Use an isolated test database or equivalent test slice.
- Do not depend on a developer’s local manual data.

### Controller tests

- Verify HTTP mappings, status codes, validation, and error bodies.
- Use `MockMvc` or the project’s equivalent.
- Assert both happy-path and invalid-request behavior.

### Repository tests

- Verify queries, constraints, and persistence mapping.
- Cover empty results, uniqueness, and filtering.
- Keep tests independent of execution order.

### Negative tests

- Missing resources
- Invalid payloads
- Unauthorized/forbidden actions when auth exists
- Illegal state transitions
- Duplicate or conflicting updates

### State-machine tests

- Allowed transitions succeed
- Disallowed transitions fail with the specified error
- Guards and side effects match `spec/state-machine.md`
- Terminal states reject further illegal moves

### Regression tests

- When a defect is found, add a test that would have failed before the fix.
- Record the link in `docs/ai-validation.md` and `docs/traceability.md` when the defect was AI-caused.

## Isolation

- Tests must not depend on other tests.
- Reset state between tests.
- Do not share mutable static state.
- Do not require a running production database.

## Naming

Use behavior-oriented names:

- `shouldReturnNotFoundWhenTicketDoesNotExist`
- `shouldRejectTransitionFromClosedToOpen`
- `methodName_condition_expectedResult` is acceptable if already used in the project

The name must say the scenario and the expected outcome.

## Alignment with specifications

- Derive tests from `spec/requirements.md`, `spec/api-contract.md`, `spec/state-machine.md`, and `spec/test-strategy.md`.
- Do not write tests for unimplemented phases.
- Do not weaken assertions to make tests pass.

## Evidence

When reporting test results, include:

- Command run
- Outcome
- What was not run

If the project has no tests yet, do not generate a fake suite during specification-only phases.
