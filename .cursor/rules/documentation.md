---
description: Documentation standards for README, specs, setup, decisions, and AI changes
globs: "**/{README.md,PLAN.md,spec/**,docs/**,.github/**}"
alwaysApply: false
---

# Documentation Guidelines

Documentation must stay synchronized with the approved specification and the actual implementation.

## README

When a README is introduced or updated, it must include:

- What the project is
- Current SDD phase / status
- Prerequisites
- How to build
- How to run
- How to test
- Link to `PLAN.md` and `spec/`

Do not describe unimplemented features as if they exist.

## Architecture documentation

- Keep `spec/architecture.md` as the architecture source of truth after Phase 4.
- Record significant decisions and the reason they were chosen.
- Update architecture docs in the same change that alters structure or runtime topology.

## API documentation

- Keep `spec/api-contract.md` aligned with controllers and DTOs.
- Document endpoints, payloads, statuses, and errors.
- Do not leave README examples that contradict the contract.

## Setup instructions

Document:

- JDK version actually configured in the project
- Maven usage
- Required local services, if any
- Configuration files and which values are secrets (never paste secrets)

If Java 21 is required later, document the decision and the `pom.xml` change together.

## Testing instructions

Document:

- How to run unit tests
- How to run integration tests
- What a passing run means
- Any test containers or profiles

Never claim tests pass in documentation without evidence from an actual run.

## Decision records

Record non-obvious decisions in `docs/` or in the relevant spec:

- Context
- Decision
- Consequences
- Date / phase

Do not silently change a prior decision.

## AI-generated changes

- Record prompts in `docs/prompt-history.md`.
- Record AI mistakes and corrections in `docs/ai-validation.md`.
- Keep requirement-to-test mapping in `docs/traceability.md`.
- Distinguish generated drafts from approved specifications.

## Synchronization rule

If implementation changes behavior:

1. Update the relevant `spec/` file
2. Update tests
3. Update README / setup / API docs if user-facing
4. Update traceability

Do not update application code during documentation-only phases, and do not invent product details in placeholder specs.
