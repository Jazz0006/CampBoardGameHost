# D6.2 UI Composition Boundary Audit

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> D6.1 merge commit: `112572cbd3d990737a412cc4b8ead766d00867e8`  
> D6.2 branch base / current main: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`  
> Latest validated production checkpoint: `c3a25f640e7e6c9ef2537d7d9387eb27b28b1ece`
> Status: **D6.2a–g COMPLETE / VALIDATED; D6.2h READ-ONLY RE-AUDIT COMPLETE; D6.2i COMPLETE / VALIDATED; D6.2j AUDIT COMPLETE; D6.2k COMPLETE / VALIDATED**

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

## D6.2i — COMPLETE / VALIDATED: Day nomination transient ownership

Production checkpoint: `5e0891e7611787300b01d83b889f27a903c0768b`.

- App no longer declares, resets or forwards the nomination pair or dead outer vote count.
- Judge owns `nominatorName` and `nomineeName` with `remember(gameId, round)`.
- Cancel nomination and confirm vote still clear both names; cancel vote retains the pair.
- Day mode, ghost-vote authority, highest-vote mechanics, Virgin callbacks and Recovery remain external and unchanged.

Verified boundary metrics:

```text
Judge parameters:                  92 -> 89
on... callbacks:                   34 unchanged
function-valued providers:          3 unchanged
MutableState parameters:            8 -> 5
App explicit remembered state vals: 9 -> 6
App delegated remembered vars:     38 unchanged
Combined scalar state declarations:47 -> 44
```

**Metric correction:** the old expected `App-root vars 36 -> 33` had no reproducible counting definition. The counts above include root `clocktower*` declarations directly initialized with `remember { mutableStateOf(...) }`, both delegated `var` and explicit `val`; they exclude derived views and state lists. Earlier campaign figures are historical, not the current measured total.

Exact production diff: App +0/-12; Host +2/-11; exactly two files and one commit from `bffa52095585bd9ee37e77fce31c2a71219ce895`.

Acceptance:

```text
CI 34291666237 — PASS / FULL
  Android full JVM + debug APK — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
R2 34291666241 — PASS
```

Local Gradle could not download its distribution because of restricted network access. Compilation and the existing nomination/vote tests were validated by the remote full JVM suite, not by a claimed local GREEN. No new tests or production seams were introduced.

Detailed evidence: `docs/D6_2I_DAY_NOMINATION_OWNERSHIP_PROGRESS_2026-09-09.md`.

## D6.2j residual audit / D6.2k implementation COMPLETE

The D6.2j contract was implemented and validated at `c3a25f640e7e6c9ef2537d7d9387eb27b28b1ece`. Exact production diff: three files, +0 / -257; four dormant effects and 13 diagnostic states removed. Judge now has 87 parameters, 34 on-callbacks, three providers and five MutableState inputs. NightStep has 48 parameters and 13 callbacks; App remembered Clocktower scalars remain 44.

CI 34293746781 PASS (Android compile + FAST and CI gate); R2 34293746779 PASS. Live recommendation/publication, A4 services and Recovery were preserved. Detailed evidence: `docs/D6_2K_DORMANT_DIAGNOSTIC_CLEANUP_PROGRESS_2026-09-09.md`.

Next follow the global audit's R0 route: retire unused Day/History UI and the corresponding obsolete R2 assertions with full CI, then separately remove App decoder islands before preparing live rendering/information boundaries.

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
-> D6.2i Day nomination transient ownership VALIDATED @ 5e0891e...
-> D6.2j residual composition audit COMPLETE
-> D6.2k dormant diagnostics / dead inputs VALIDATED @ c3a25f64...
-> retired Day/History UI + R2 assertion cleanup NEXT
-> PR #115 remains OPEN / DRAFT / DO NOT AUTO-MERGE
```
