---
description: Retrieve only the context needed for correctness; never trade accuracy for fewer tokens
alwaysApply: true
---

# Context Optimization

Reduce unnecessary AI context while remaining correct. Correctness always wins.

## Read the specification first

Before implementing or reviewing a feature, read only the relevant files in `spec/` and `PLAN.md`. Do not guess product behavior.

## Inspect existing code before modification

Open the files that will change and their direct callers/callees. Do not regenerate or restructure the existing project.

## Use targeted files

- Prefer a specific file path over repository-wide reads.
- Search with a focused pattern, then read matching files.
- Do not dump the entire repository into context.

## Do not read or index noise

Avoid:

- `node_modules/`
- `target/`
- `build/`
- `out/`
- generated sources
- IDE internals unless inspecting project configuration
- unrelated modules
- binaries, lockfile blobs, and large data files

## Retrieve context incrementally

1. Identify the task and phase from `PLAN.md`
2. Read the matching spec
3. Locate candidate files with search
4. Read those files
5. Fetch more only if the first set is insufficient

## Use codebase indexing / context tools

Where available, use search, glob, and semantic lookup instead of opening every file. Request only the slices needed.

## Never sacrifice correctness

Do not skip a spec, test, or security check to save tokens. If more context is required to avoid a wrong change, retrieve it.

## Phase discipline

If the current phase is governance or specification only:

- Do not implement Ticket entities, controllers, services, repositories, REST endpoints, UI, schema, or business logic
- Do not modify `pom.xml` or `Main.java` unless the current task explicitly requires it
