# Dead-code cleanup — slice 1

Date: 2026-09-28. Baseline: `311eaa472eebe627fd1ca9865ed4702fe28c43cc`.

## Scope and architecture pre-flight

Change type: behavior-preserving deletion of unreachable code and write-only state.

- Current owner: App root and Clocktower Host presentation wiring.
- Proposed responsibility: none added or moved; remove obsolete paths.
- Authoritative state owners: ClocktowerGameSession retains mechanical/revision/history authority; existing App/Host owners retain live drafts and presentation state.
- Narrow typed input/output seam: remove the two always-empty Drunk/Investigator Judge inputs; preserve the existing unreliable-information candidate generator and all legal/automatic selection inputs.
- Keep in current owner / extract: keep live behavior in its current owners; no extraction.
- Reason: the deleted functions have no callers, the deleted state has no effective readers, and the removed optional candidate is always absent.

## Deletion evidence

- App-local `nextNightPublicAliveObservationPreflightOrNull` and `setClocktowerShownRole` have no callers. Live preflight and session role-boundary APIs remain.
- `lastWordsMode` is initialized/reset but never read. Its enum and exclusive labels can retire.
- `clocktowerRulesetRoleIds` is assigned but never read. Ruleset basis construction and ruleset reference resolution remain.
- App Drunk/Investigator recommendation role/seats are only assigned null/empty values, including restore and new-game paths. App is the sole production Judge caller. Therefore the Host optional recommendation builder always returns null. Remove the state, resets, parameters, builder, and empty list contribution together. Preserve the live candidate generator unchanged.
- `EliminationRecord.displayText`, `clocktowerVisibleHostPhase`, Host-local `twoSeatNumbers`, and `recommendationReasonLabel` have no callers.
- R2 currently requires the unused reason-label helper to exist. Retire only that assertion; keep guards for live presentation types and other ownership boundaries.
- Remove resources only when deletion leaves them with no remaining references.

Werewolf retirement, recommendation facades, test-only experiments, compatibility redesign, and Recovery R7 are outside this slice.

## Evidence and acceptance

No manufactured RED or new source-string tests: no stable behavior is introduced or changed. Retain existing typed tests and negative Recovery wire assertions.

Before commit: re-search all removed symbols and Judge producers/consumers, inspect the complete diff, check resource XML, and run `git diff --check`.

GitHub acceptance: Android FULL (`:app:testFull :app:assembleDebug`), R2, and the workflow-triggered ASP/Real-Clingo gates. The local host has no Android SDK; local static checks do not replace those gates. Merge requires separate authorization.
