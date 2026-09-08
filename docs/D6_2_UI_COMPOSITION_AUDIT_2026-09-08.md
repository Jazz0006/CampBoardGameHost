# D6.2 UI Composition Boundary Audit

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> D6.1 merge commit: `112572cbd3d990737a412cc4b8ead766d00867e8`  
> D6.2 branch base / current main: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`  
> Latest validated production checkpoint: `15342f9e22ac204602680e6ef831fb4e95c7b0bf`  
> Status: **D6.2a–g COMPLETE / VALIDATED; D6.2h READ-ONLY RE-AUDIT COMPLETE; D6.2i NEXT**

## Purpose

D6.1 solved canonical Clocktower session/domain writer ownership. D6.2 addresses the remaining Compose/UI composition fan-out around `ClocktowerJudgeScreen` and `ClocktowerHostScreen.kt`.

The optimization target is **responsibility ownership and modification radius**, not file size alone.

## D6.2 baseline

Post-D6.1 residual audit:

```text
ClocktowerHostScreen.kt     329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt     241,986 bytes / 4,315 lines
ClocktowerJudgeScreen       103 parameters
on... callbacks              39
function-valued providers     3
MutableState<T> params        10
App-root Clocktower vars      41
```

## Architecture rules

Do not solve fan-out by packaging it into:

```text
ClocktowerJudgeState
ClocktowerJudgeActions
DayState / NightState mega-bags
broad Controller / ViewModel wrappers
```

Do not move Compose concerns into `ClocktowerGameSession` or domain code.

Move ownership only when a real cohesive UI/phase boundary exists. Keep Recovery-authoritative and durable mechanics state external even if it currently passes through Compose.

The near-term UI direction is square-table/table based. Retired HostScriptCard composition should not receive further architecture investment.

## D6.2a — consumption matrix COMPLETE

Authority:

- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

The 103 inputs were audited by actual consumers rather than name grouping. Key result: they do not form one natural state object.

The baseline also identified zero-consumer `records` and `onPhaseChange`, but they were intentionally left for a later dead-plumbing slice rather than mixed into feature ownership work.

## D6.2b — Slayer transient ownership COMPLETE / VALIDATED

Checkpoint:

```text
58bc1e51440d44f36e14d1a9d5a45cfe9c235955
```

Judge-local:

```kotlin
remember(gameId) { slayerClaimantName }
remember(gameId) { slayerTargetName }
```

Durable action boundary unchanged:

```text
onSlayerShot(claimantName, targetName, recluseRegistersAsDemon)
```

Metrics:

```text
Judge params:             103 -> 101
callbacks:                 39 unchanged
MutableState params:       10 -> 8
App-root Clocktower vars:  41 -> 39
```

Validation:

```text
R2 34283098478 — PASS
CI 34283098477 — PASS / FULL
```

## D6.2c/d — Artist contract + transient ownership COMPLETE / VALIDATED

Authority:

- `docs/D6_2C_ARTIST_CONFIRMATION_CONTRACT_AUDIT_2026-09-08.md`

Final validated checkpoint:

```text
6a5723af0e9fb646d0c66e900a1c4d215d6a2fef
```

Judge-local transient state:

```text
artistClaimantName
artistTruthfulAnswer
artistShownAnswer
```

Durable callback:

```text
onConfirmArtistQuestion(String, Boolean, Boolean)
```

App retains durable Artist mechanics/history/revision/day routing.

Metrics after D6.2d:

```text
Judge params:             101 -> 95
callbacks:                 39 -> 36
MutableState params:        8 unchanged
App-root Clocktower vars:  39 -> 36
```

Validation:

```text
R2 34286858464 — PASS
CI 34286858453 — PASS / FULL
```

A first implementation attempt exposed delegated-property smart-cast compilation issues. The correction only snapshots nullable Compose delegated values into stable locals before Boolean calculations. Failed/bootstrap history was removed from the final production line.

## D6.2e — legacy Storyteller reachability proof COMPLETE

Authority:

- `docs/D6_2E_LEGACY_STORYTELLER_REACHABILITY_AUDIT_2026-09-09.md`

The exhaustive phase/nightStarted/dayMode audit proved the trailing HostScriptCard/HostProgressCard Storyteller block unreachable.

Every legal state already returned through one of the live modern/table paths:

```text
Dawn
Day Overview
Day Nomination
Day Vote
Day EndConfirm
Day Slayer
Day Artist
Day Klutz
FirstNight setup
Night ready
active FirstNight/Night flow
```

Conclusion: delete the old tail rather than decompose it.

## D6.2f — legacy tail retirement COMPLETE / VALIDATED

Authority:

- `docs/D6_2F_LEGACY_STORYTELLER_RETIREMENT_PROGRESS_2026-09-09.md`

Checkpoint:

```text
cee19c1ab85b4b4400a4d38f958a9014dd10a5e3
```

Net production diff:

```text
exactly 2 production files
0 additions / 685 deletions
```

`ClocktowerHostScreen.kt`:

```text
330,257 -> 283,849 bytes
5,491 -> 4,807 lines
```

Also removed writerless `ClocktowerDayMode.ExecutionResult`.

Validation:

```text
R2 34288731422 — PASS
CI 34288731376 — PASS / FULL
  Android FULL + debug APK — PASS
  ASP — PASS
  Real Clingo — PASS
  CI gate — PASS
```

## D6.2g — retired plumbing cleanup COMPLETE / VALIDATED

Authority:

- `docs/D6_2G_RETIRED_STORYTELLER_PLUMBING_PROGRESS_2026-09-09.md`

Checkpoint:

```text
15342f9e22ac204602680e6ef831fb4e95c7b0bf
```

Removed zero-consumer plumbing exposed by D6.2f:

```text
Judge records parameter + App forwarding
onPhaseChange + App lambda
onShowResults + App lambda
phaseTitle
phaseProgress
phaseScript
phaseAction
recordCurrentVote()
```

The shared App `records` collection itself remains because Recovery/history/other game paths still use it.

Net diff:

```text
CampBoardGameHostApp.kt +0 / -26
ClocktowerHostScreen.kt +0 / -42
0 additions / 68 deletions
```

Current validated metrics:

```text
ClocktowerJudgeScreen
  parameters:              92   (103 -> 92)
  callbacks:               34   (39 -> 34)
  providers:                3
  MutableState params:      8   (10 -> 8)

App-root Clocktower vars:   36   (41 -> 36)
```

Validation:

```text
R2 34289616209 — PASS
CI 34289616204 — PASS
  Android FAST — PASS
  CI gate — PASS
```

A second FULL gate was intentionally not repeated immediately after D6.2f's T4 because D6.2g was pure dead-plumbing deletion.

## D6.2h — surviving square-table ownership re-audit COMPLETE

Authority:

- `docs/D6_2H_SURVIVING_SQUARE_TABLE_OWNERSHIP_AUDIT_2026-09-09.md`

This re-audit considered only live table UI.

### `currentVoteCountState` is dead

The modern vote screen owns pending voter selection/count inside typed `ClocktowerTableVoteState` and submits it through `onConfirm(voteState)`.

The outer `currentVoteCount` is never read; it is only reset to `0` at several transitions.

Conclusion: delete it.

### nomination pair is transient UI state

```text
nominatorNameState
nomineeNameState
```

App-side responsibilities are declaration/reset/forwarding only. They are not serialized/restored as Clocktower Recovery mechanics.

The only live semantic consumers are the square-table nomination and vote composition paths.

Recommended local lifetime:

```kotlin
remember(gameId, round)
```

The round lifecycle proves equivalent reset semantics across normal Day completion, Virgin immediate execution and Klutz paths.

### state that must remain external

```text
dayModeState
  App/Recovery/Klutz/Artist routing still writes it

ghostVoteAuthority
highestVoteNameState
highestVoteCountState
  durable vote mechanics + Recovery serialization/restore
```

Do not move these with the nomination pair.

## D6.2i — recommended next implementation slice

Exact scope:

```text
remove App:
  clocktowerNominatorNameState
  clocktowerNomineeNameState
  clocktowerCurrentVoteCountState

remove 3 Judge MutableState parameters

Judge-local:
  nominatorName
  nomineeName
  keyed by remember(gameId, round)

remove currentVoteCount entirely
```

Preserve:

```text
dayModeState
ghostVoteAuthority
highestVoteNameState
highestVoteCountState
Virgin durable callbacks
vote transaction behavior
Recovery v2
revision cadence
```

Expected metrics:

```text
Judge params:             92 -> 89
callbacks:                34 unchanged
MutableState params:       8 -> 5
App-root Clocktower vars: 36 -> 33
```

Existing relevant tests:

- `ClocktowerDayNominationGestureTest`
- `ClocktowerTableVoteStateTest`
- `ClocktowerVoteTransactionTest`

Do not manufacture source-string tests. For this behavior-preserving ownership move, use fail-closed source assertions, compile, existing focused tests and FAST inside the implementation workflow. Prefer a final clean `[full-ci]` checkpoint because state lifetime changes across App/Judge.

## Large-file mutation method

`CampBoardGameHostApp.kt` and `ClocktowerHostScreen.kt` are still large enough that connector whole-file replacement is unnecessarily risky.

Normative SOP:

- `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`

The active 2026-09-09 D6.2i handoff records the D6.2-proven variant used in this connector environment, including:

- bootstrap workflow triggered by the workflow-file push itself;
- fail-closed exact occurrence and final signature assertions;
- compile/test before product commit;
- self-removal of temporary workflow/script;
- reconstructing one clean production commit from the final tree with the intended parent;
- force-updating only the feature branch ref;
- `compare_commits` exact one-commit/file-allowlist audit;
- normal clean-head CI/R2 after bootstrap history removal.

A new conversation must read that before mutating either large file.

## Frozen invariants

Preserve:

- `ClocktowerGameSession` canonical writable session/domain ownership;
- exact game/player revision cadence;
- semantic chronology and idempotency;
- Recovery v2 + PS5 write-gate topology;
- A4 durability/invalidation ordering;
- no storyteller-hidden target leak;
- no Compose dependency in session/domain;
- recommendation/gameplay semantics unless separately authorized;
- Undercover/Werewolf isolation.

## Branch / PR route

```text
main d76b0854...
-> codex/d6-2-ui-composition
-> D6.2a consumption audit COMPLETE
-> D6.2b Slayer ownership VALIDATED
-> D6.2c/d Artist ownership VALIDATED
-> D6.2e unreachable legacy proof COMPLETE
-> D6.2f legacy retirement VALIDATED @ cee19c1...
-> D6.2g dead plumbing VALIDATED @ 15342f9...
-> D6.2h surviving square-table audit COMPLETE
-> D6.2i Day nomination transient ownership NEXT
-> PR #115 remains OPEN / DRAFT / DO NOT AUTO-MERGE
```
