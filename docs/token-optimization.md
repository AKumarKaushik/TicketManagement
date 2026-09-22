# Token Optimization

This project reduces unnecessary AI context while keeping answers and code changes correct. Correctness is never traded for a smaller prompt.

## Goals

- Load only the files needed for the current SDD phase
- Avoid generated and third-party trees
- Prefer search then targeted reads over full-repository dumps
- Keep specifications as the behavior source, not chat memory

## Required practice

1. **Read the relevant specification first.** Use the matching file in `spec/` and the current phase in `PLAN.md`.
2. **Inspect existing code before modification.** Read the files that will change and their direct neighbors.
3. **Use targeted files.** Open exact paths. Do not attach the whole repository.
4. **Avoid noise directories:** `node_modules/`, `target/`, `build/`, `out/`, generated sources, binaries.
5. **Avoid unrelated modules** and IDE metadata unless the task is project-configuration inspection.
6. **Retrieve context incrementally.** Search → skim hits → read the few files that matter → fetch more only if blocked.
7. **Use codebase indexing / search tools** when available instead of concatenating large trees.
8. **Never sacrifice correctness.** If a spec, security check, or test is required, include it.

## What not to put in context

- Full `target/` class output
- Dependency jars
- Unrelated conversation history about later phases when still in Phase 1
- Invented product details from placeholder specs
- Secrets, `.env` files, credentials

## Phase-aware context

| Phase | Minimum context | Do not load |
|---|---|---|
| 1 Assessment / governance | `pom.xml`, source tree, `.gitignore`, this docs set | Ticket domain guesses |
| 2–9 Specification | `PLAN.md` + the spec being written + related specs | Implementation code generation |
| 11+ Implementation | Relevant spec + files in the layer being changed + tests | Entire frontend and backend at once unless integrating |

## Review commands

`/review-code`, `/review-spec`, and `/generate-tests` should follow the same targeting rules and must not claim tests passed without running them.

## Enforcement

`.cursor/rules/context-optimization.md` is always-on guidance for agents working in this repository.
