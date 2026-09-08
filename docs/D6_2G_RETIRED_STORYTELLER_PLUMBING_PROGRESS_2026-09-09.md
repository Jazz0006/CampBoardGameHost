# D6.2g Retired Storyteller Plumbing Progress

> Date: 2026-09-09 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/d6-2-ui-composition`
> D6.2f progress checkpoint: `d0f91b223a22f444af6e8bdc047560b6e711dc2c`
> Validated D6.2g production checkpoint: `15342f9e22ac204602680e6ef831fb4e95c7b0bf`
> Status: **D6.2g COMPLETE / FAST CI + R2 PASS**

## Purpose

D6.2f removed the unreachable legacy Storyteller UI tail. That deletion left several Judge inputs and local helpers with zero consumers.

D6.2g removes only that newly exposed dead plumbing. It does not change live UI behavior, session/domain ownership, Recovery, recommendation logic, nomination/vote semantics or Night semantics.

## Exact production changes

Relative to `d0f91b223a22f444af6e8bdc047560b6e711dc2c`:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
  +0 / -26

app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
  +0 / -42

production files changed: exactly 2
production commit count: exactly 1
additions: 0
deletions: 68
```

Removed Judge parameters:

```text
records
onPhaseChange
onShowResults
```

Removed App-to-Judge forwarding:

```text
records = records
onPhaseChange = { ... }
onShowResults = { ... }
```

The shared App `records` collection remains intact because Recovery/history/Werewolf/Game paths still use it.

Removed zero-consumer Judge locals:

```text
phaseTitle
phaseProgress
phaseScript
phaseAction
recordCurrentVote()
```

## Fan-out result

The bootstrap mechanically re-counted the final `ClocktowerJudgeScreen` signature after the cleanup:

```text
parameters: 95 -> 92
on... callbacks: 36 -> 34
provider functions: unchanged at 3
MutableState parameters: unchanged at 8
```

From the original D6.2 baseline:

```text
parameters: 103 -> 92
on... callbacks: 39 -> 34
MutableState parameters: 10 -> 8
App-root Clocktower vars: 41 -> 36
```

## Validation

The fail-closed bootstrap asserted exact source occurrences and final signature counts, then ran:

```text
:app:compileDebugKotlin
:app:testFast
```

Result:

```text
BUILD SUCCESSFUL
```

Clean production checkpoint:

```text
15342f9e22ac204602680e6ef831fb4e95c7b0bf
refactor: remove retired storyteller plumbing
```

Clean checkpoint validation:

```text
R2 34289616209 — PASS
CI 34289616204 — PASS
  change classifier — PASS / Android FAST selected
  Android FAST unit tests — PASS
  Android FULL — correctly skipped
  ASP — correctly skipped
  Real Clingo — correctly skipped
  CI gate — PASS
```

D6.2f immediately before this slice already passed a full T4 checkpoint (`34288731376`), so another full validation was intentionally not repeated for this pure dead-plumbing deletion.

## History hygiene

A temporary GitHub Actions bootstrap was required because the current container could not clone the repository directly.

The first bootstrap workflow had a YAML parse-level failure before any production edit. The corrected bootstrap passed exact source assertions, compile and FAST tests, produced the final production tree, and deleted itself.

The branch history was then rebuilt so neither bootstrap attempt appears on the final production line. `15342f9...` is directly parented by `d0f91b2...` and contains exactly the two production files listed above.

## Architectural conclusion

The legacy Storyteller retirement sequence is now closed:

```text
D6.2e  prove legacy tail unreachable
D6.2f  delete legacy tail + writerless ExecutionResult
D6.2g  remove dead plumbing exposed by deletion
```

Do not continue extracting or preserving retired `HostScriptCard` UI structure.

## Next: D6.2h surviving UI ownership re-audit

The next task should be read-only first. Re-rank only live square-table/table paths.

Primary candidate groups:

1. Day nomination/vote flow:
   - `dayModeState`
   - `nominatorNameState`
   - `nomineeNameState`
   - `currentVoteCountState`
   - `ghostVoteAuthority`
   - `highestVoteNameState`
   - `highestVoteCountState`
   - corresponding durable nomination/vote/execution callbacks

2. Night navigation/checkpoint flow:
   - `nightStartedState`
   - `nightStepIndexState`
   - `nightCheckpoint`
   - phase-specific selection values/callbacks

The Day group has an existing square-table composition seam but several fields participate in Recovery mechanics. The Night group is even more tightly bound to checkpoint/recovery semantics.

Therefore do not move either group wholesale. First map exact external writers/readers, Recovery restore requirements, and which state is truly transient versus durable/recovery-authoritative. Prefer a smaller sub-boundary if one exists.

Also perform a repository-wide orphan-helper audit separately, but only delete a helper after proving zero live consumers; helper cleanup must not drive ownership design.
