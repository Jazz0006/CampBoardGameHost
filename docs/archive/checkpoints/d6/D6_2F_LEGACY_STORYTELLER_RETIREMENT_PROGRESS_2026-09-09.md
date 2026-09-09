# D6.2f Legacy Storyteller UI Retirement Progress

> Date: 2026-09-09 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/d6-2-ui-composition`
> Base before D6.2f: `3502c3ea39ded38a2b61c374e04ea9d05c238220`
> Validated production checkpoint: `cee19c1ab85b4b4400a4d38f958a9014dd10a5e3`
> Status: **D6.2f COMPLETE / FULL CI + R2 PASS**

## Scope

D6.2e proved the old trailing `ClocktowerDarkTheme { LazyColumn { ... } }` storyteller fallback in `ClocktowerJudgeScreen` unreachable for every production-writable phase/day-mode state.

D6.2f therefore performed deletion, not decomposition:

1. removed the unreachable legacy storyteller tail from `ClocktowerHostScreen.kt`;
2. removed the writerless `ClocktowerDayMode.ExecutionResult` enum value;
3. deliberately did not mix dead-parameter cleanup, helper cleanup, Recovery, recommendation, gameplay or state-ownership changes into this slice.

## Exact production diff

Relative to `3502c3ea39ded38a2b61c374e04ea9d05c238220`:

```text
app/src/main/java/com/codex/campboardgamehost/ClocktowerAppModels.kt
  +0 / -1

app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
  +0 / -684

production files changed: exactly 2
production commits ahead: exactly 1
```

The clean production tree contains no temporary workflow files.

## Size result

```text
ClocktowerHostScreen.kt
  lines: 5,491 -> 4,807  (-684, about -12.5%)
  bytes: 330,257 -> 283,849  (-46,408, about -14.1%)
```

This is materially higher-value than decomposing the retired `HostScriptCard` UI.

## Structural result

The end of `ClocktowerJudgeScreen` is now the modern Night path:

```text
ClocktowerNightActiveScreen(...)
return
}
```

There is no trailing HostScriptCard/LazyColumn fallback after that return.

`ClocktowerDayMode` now contains only the seven production-used modes:

```text
Overview
Slayer
Artist
Klutz
Nomination
Vote
EndConfirm
```

## Bootstrap / history hygiene

The large-file edit was applied through a fail-closed temporary GitHub Actions bootstrap because the current container could not resolve `github.com` for a local checkout.

The first bootstrap attempt failed before writing production because an audit occurrence count expected five `SelectablePlayerChips` calls while the actual dead tail contained six. The count was corrected; the second bootstrap passed exact removal assertions and `:app:compileDebugKotlin`.

Afterward the branch history was rebuilt so all bootstrap add/fix/delete commits are absent from the final branch line. The validated clean production commit is directly parented by the D6.2e docs checkpoint.

## Pre-commit compile evidence

The successful bootstrap measured:

```text
ClocktowerHostScreen lines: 5491 -> 4807
ClocktowerHostScreen bytes: 330257 -> 283849
2 production files changed, 685 deletions
:app:compileDebugKotlin — BUILD SUCCESSFUL
```

## Final acceptance

Clean checkpoint:

```text
cee19c1ab85b4b4400a4d38f958a9014dd10a5e3
[full-ci] refactor: retire unreachable legacy storyteller UI
```

Validation:

```text
R2 34288731422 — PASS

CI 34288731376 — PASS
  change classifier — PASS / FULL selected
  Android FULL unit tests — PASS
  debug APK build — PASS
  ASP contract tests — PASS
  Real Clingo cross-validation — PASS
  CI gate — PASS
```

## D6.2g read-only orphan inventory

Removing the dead tail also removed the last consumers of several Judge inputs/local helpers. Initial exact occurrence audit on the clean Host shows each of the following now appears only at its declaration/definition:

```text
records
onPhaseChange
onShowResults
phaseTitle
phaseProgress
phaseScript
phaseAction
recordCurrentVote()
```

The App still legitimately owns the shared `records` collection for Recovery/history/other game screens; only the Clocktower Judge forwarding argument is dead.

`onPhaseChange` and the Clocktower Judge `onShowResults` lambda are also now pure dead forwarding because Judge has no consumer. Other games/screens retain their own result callbacks.

### Recommended next slice

D6.2g should remain a small dead-plumbing cleanup:

- remove Judge `records` parameter and only its Clocktower call-site argument;
- remove Judge `onPhaseChange` parameter and only its Clocktower call-site lambda;
- remove Judge `onShowResults` parameter and only its Clocktower call-site lambda;
- remove the now-unused local phase-title/progress/script/action values;
- remove the now-unused `recordCurrentVote()` local helper;
- do not remove globally shared records state;
- do not combine nomination/vote ownership, Recovery or recommendation changes;
- do not broadly delete old UI helper definitions until repository-wide consumers are separately proven absent.

No new behavior characterization test is justified for values proven to have zero consumers. Exact source diff + compilation/FAST validation is the appropriate guard for this slice.
