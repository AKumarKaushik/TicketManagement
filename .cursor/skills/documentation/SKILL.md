---
name: documentation
description: Keep SDD documentation synchronized with implementation, record prompts, AI validation, and traceability. Use when updating README, spec files, PLAN.md, docs/, decision records, or when the user asks to document AI changes, setup, API, architecture, or testing.
---

# Documentation Skill

Use this skill when creating or updating project documentation.

## Instructions

1. Read `PLAN.md` and the relevant `spec/` files before writing docs.
2. Inspect existing documentation and only the code needed to verify facts.
3. Follow `.cursor/rules/documentation.md`.
4. Do not invent product behavior. If a spec is still a placeholder, say so.
5. Do not implement application code while documenting unless the user asked for both.

## Required records

### Prompt history

Append every user prompt used in the SDD exercise to `docs/prompt-history.md`:

- Date
- Phase
- Prompt summary or verbatim prompt
- Resulting artifacts

### AI validation

When an AI mistake is found, append `docs/ai-validation.md`:

- AI mistake
- Why it was wrong
- How it was detected
- Correction
- Regression test

### Traceability

Keep `docs/traceability.md` as:

Requirement → Specification → Task → Implementation → Test → Review

Do not leave a completed implementation without a traceability row.

## Document types

- README: purpose, setup, build, run, test, current phase
- Architecture: `spec/architecture.md` after Phase 4
- API: `spec/api-contract.md` after Phase 6
- Setup: real JDK/Maven values from the repository, not assumed Java 21 until decided
- Testing: actual commands and scope
- Decisions: context, decision, consequences
- Token optimization: `docs/token-optimization.md`

## Synchronization

If code and docs disagree, treat approved specs as the intended behavior and flag implementation drift. Update both in the same change when behavior is intentionally changed.

## Constraints

- Never paste secrets.
- Never claim tests passed without a real test run.
- Do not upgrade Java, Spring Boot, or dependencies in a documentation task.
- Do not modify `Main.java` or implement ticket functionality during documentation-only work.
