---
description: Review SDD specifications for completeness, consistency, and phase readiness
---

# /review-spec

Review specification documents for quality and consistency. Do not implement application code.

## Before reviewing

1. Read `PLAN.md` to know which phases are complete.
2. Read all populated files under `spec/`.
3. Compare related docs: requirements, architecture, data model, API contract, state machine, UI flow, test strategy.
4. Read `docs/traceability.md` and `.cursor/rules/documentation.md`.

## Review for

- Placeholder vs approved content: do not treat placeholders as product truth
- Completeness for the current phase
- Numbered, testable requirements
- Consistency of names, statuses, fields, and endpoints across specs
- Missing states, transitions, error cases, or unauthorized paths
- API contract alignment with data model and UI flow
- Test strategy covering negatives, state machine, and regression
- Traceability gaps (requirement without spec, spec without test plan)
- Invented details that were not approved
- Conflicts with the existing repository (Java version, Maven layout, `Main.java`, no Spring Boot yet)

## Output format

- Ready / not ready for the next phase
- Inconsistencies across spec files
- Missing sections
- Ambiguities that would cause incorrect implementation
- Questions that must be answered before coding
- Updates needed in `docs/traceability.md`

## Constraints

- Do not populate invented application details.
- Do not start implementation.
- Do not modify `pom.xml` or `Main.java`.
