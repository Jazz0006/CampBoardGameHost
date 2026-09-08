# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## Live context

```text
main: d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e
D6.2 branch: codex/d6-2-ui-composition
Draft PR: #115 — OPEN / DRAFT / DO NOT AUTO-MERGE
Latest validated production checkpoint:
15342f9e22ac204602680e6ef831fb4e95c7b0bf
refactor: remove retired storyteller plumbing
```

Commits after `15342f9...` are documentation-only unless a later handoff explicitly records a newer validated production checkpoint. Always re-query live branch head and keep **production checkpoint** distinct from **docs-only branch head**.

Validated D6.2g clean-head gates:

```text
R2 34289616209 — PASS
CI 34289616204 — PASS
  Android FAST — PASS
  Android FULL — correctly skipped
  ASP — correctly skipped
  Real Clingo — correctly skipped
  CI gate — PASS
```

The immediately preceding D6.2f production checkpoint passed FULL T4:

```text
cee19c1ab85b4b4400a4d38f958a9014dd10a5e3
R2 34288731422 — PASS
CI 34288731376 — PASS
  Android FULL + debug APK — PASS
  ASP — PASS
  Real Clingo — PASS
  CI gate — PASS
```

## Current priority

> **PS5 COMPLETE → D6.1 COMPLETE / MERGED → D6.2a–g COMPLETE / VALIDATED → D6.2h READ-ONLY RE-AUDIT COMPLETE → D6.2i DAY NOMINATION TRANSIENT OWNERSHIP NEXT.**

D6.2 stays on `codex/d6-2-ui-composition`. Do not reopen PR #113 or move this work back to `codex/d6-root-reaudit`.

## Architecture direction

The surviving Storyteller UI is now square-table/table based. The old HostScriptCard-style fallback was proven unreachable and deleted.

D6.2 should therefore:

- optimize only live square-table/table ownership seams;
- distinguish transient UI selection from durable/Recovery authority;
- move ownership only when a real cohesive boundary exists;
- delete dead state rather than wrapping it;
- avoid broad `ClocktowerJudgeState`, `DayState`, `Actions`, Controller or ViewModel bags;
- preserve `ClocktowerGameSession` as canonical writable session/domain owner.

File size is a useful signal, not the goal.

## D6.1 — COMPLETE / MERGED

D6.1 established:

```text
ClocktowerGameSession
= canonical writable identity
+ revisions
+ semantic chronology
+ dynamic GameState mechanics

ClocktowerSessionView
= narrow immutable Compose-facing projection

App-root PlayerCard / flow variables
= presentation and orchestration mirrors
```

Merged PR #113:

```text
112572cbd3d990737a412cc4b8ead766d00867e8
```

## D6.2 baseline

Post-D6.1 residual audit measured:

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
MutableState params       10
App-root clocktower vars  41
```

The dominant residual debt was UI-composition fan-out rather than canonical domain ownership.

## D6.2a — COMPLETE: consumption characterization

Authority:

- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

Key result: parameters do not form one natural mega-state. Ownership must move by cohesive feature/phase boundary.

## D6.2b — COMPLETE / VALIDATED: Slayer transient ownership

Checkpoint:

```text
58bc1e51440d44f36e14d1a9d5a45cfe9c235955
```

Result:

```text
Slayer claimant/target -> Judge-local remember(gameId)
Durable onSlayerShot(...) boundary unchanged
Judge params 103 -> 101
App-root vars 41 -> 39
```

Validation:

```text
R2 34283098478 — PASS
CI 34283098477 — PASS / FULL
```

## D6.2c/d — COMPLETE / VALIDATED: Artist transient ownership

Audit:

- `docs/D6_2C_ARTIST_CONFIRMATION_CONTRACT_AUDIT_2026-09-08.md`

Validated production checkpoint:

```text
6a5723af0e9fb646d0c66e900a1c4d215d6a2fef
```

Result:

```text
Artist claimant/truthful/shown -> Judge-local transient state
Durable callback -> onConfirmArtistQuestion(String, Boolean, Boolean)
Judge params 101 -> 95
callbacks 39 -> 36
App-root vars 39 -> 36
```

Validation:

```text
R2 34286858464 — PASS
CI 34286858453 — PASS / FULL
```

## D6.2e — COMPLETE: legacy Storyteller reachability proof

Authority:

- `docs/D6_2E_LEGACY_STORYTELLER_REACHABILITY_AUDIT_2026-09-09.md`

Exhaustive control-flow audit proved the trailing HostScriptCard/HostProgressCard Storyteller UI was unreachable because every legal phase/dayMode/nightStarted state already returned through the modern/table path.

## D6.2f — COMPLETE / VALIDATED: retire unreachable legacy tail

Authority:

- `docs/D6_2F_LEGACY_STORYTELLER_RETIREMENT_PROGRESS_2026-09-09.md`

Validated checkpoint:

```text
cee19c1ab85b4b4400a4d38f958a9014dd10a5e3
```

Net result:

```text
exactly 2 production files
0 additions / 685 deletions
ClocktowerHostScreen.kt
  330,257 -> 283,849 bytes
  5,491 -> 4,807 lines
```

Also removed writerless `ClocktowerDayMode.ExecutionResult`.

This was a major architecture cleanup: the legacy Storyteller composition path is gone rather than decomposed.

## D6.2g — COMPLETE / VALIDATED: dead plumbing exposed by retirement

Authority:

- `docs/D6_2G_RETIRED_STORYTELLER_PLUMBING_PROGRESS_2026-09-09.md`

Validated checkpoint:

```text
15342f9e22ac204602680e6ef831fb4e95c7b0bf
```

Removed zero-consumer Judge/App plumbing:

```text
records forwarding
onPhaseChange
onShowResults
phaseTitle
phaseProgress
phaseScript
phaseAction
recordCurrentVote()
```

Net diff:

```text
CampBoardGameHostApp.kt +0 / -26
ClocktowerHostScreen.kt +0 / -42
0 additions / 68 deletions
```

Post-D6.2g metrics:

```text
ClocktowerJudgeScreen
  parameters:              92   (103 -> 92)
  callbacks:               34   (39 -> 34)
  providers:                3
  MutableState params:      8   (10 -> 8)

App-root clocktower vars:   36   (41 -> 36)
```

## D6.2h — COMPLETE: surviving square-table ownership re-audit

Authority:

- `docs/D6_2H_SURVIVING_SQUARE_TABLE_OWNERSHIP_AUDIT_2026-09-09.md`

Key findings:

### Safe transient/dead sub-boundary

```text
nominatorNameState   -> transient square-table nomination selection
nomineeNameState     -> transient square-table nomination selection
currentVoteCountState -> dead; modern typed vote state owns pending vote count
```

`ClocktowerVoteTableScreen` already owns pending voter selection/count using typed `ClocktowerTableVoteState` and submits it as one value to the durable vote transaction.

### Must remain external for now

```text
dayModeState
  -> App/Recovery/Klutz/Artist routing still writes it

ghostVoteAuthority
highestVoteNameState
highestVoteCountState
  -> durable vote mechanics + Recovery serialization/restore
```

### Lifecycle proof

The nomination pair can be safely Judge-local with:

```kotlin
remember(gameId, round)
```

because Day completion advances `round` before entering Night, while Virgin and Klutz special paths preserve equivalent reset semantics. No Recovery contract requires restoring an in-progress nomination pair.

## D6.2i — NEXT: Day nomination transient ownership

Recommended exact slice:

```text
App removes:
  clocktowerNominatorNameState
  clocktowerNomineeNameState
  clocktowerCurrentVoteCountState

Judge removes 3 MutableState parameters.
Judge owns:
  nominatorName
  nomineeName
with remember(gameId, round).

currentVoteCount is deleted completely.
```

Preserve:

```text
dayModeState
ghostVoteAuthority
highestVoteNameState
highestVoteCountState
Virgin callbacks
vote transaction semantics
Recovery schema/state
```

Expected metrics:

```text
Judge parameters:         92 -> 89
callbacks:                34 unchanged
MutableState params:       8 -> 5
App-root clocktower vars: 36 -> 33
```

Relevant existing tests:

- `ClocktowerDayNominationGestureTest`
- `ClocktowerTableVoteStateTest`
- `ClocktowerVoteTransactionTest`

Do not add source-string tests just to claim tests-first. For this behavior-preserving ownership refactor, use exact source assertions, compile, focused existing behavior coverage and FAST; prefer a final clean `[full-ci]` checkpoint because state lifetime moves across App/Judge.

## GitHub large-file execution rule

For `CampBoardGameHostApp.kt` / `ClocktowerHostScreen.kt`, do **not** reconstruct the whole file from truncated connector output.

Normative SOP:

- `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`

The active 2026-09-09 handoff additionally records the D6.2-proven bootstrap + history-squash procedure. A new conversation must read that before attempting a large-file mutation.

## Frozen invariants

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- exact game/player revision cadence and ordering;
- one monotonic collision-free semantic chronology;
- action/observation idempotency and non-mutating preflight;
- no storyteller-hidden target leak;
- Recovery v2 current-version-only policy;
- SideEffect / ON_PAUSE / ON_STOP + `RecoveryWriteGate` topology;
- A4 durability/invalidation/prewarm ordering;
- no Compose dependency in session/domain;
- gameplay/recommendation semantics unless separately authorized;
- Undercover/Werewolf isolation.

## Active handoff

For the next conversation, read in this order:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_2H_SURVIVING_SQUARE_TABLE_OWNERSHIP_AUDIT_2026-09-09.md`
5. latest `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-09_*.md`
6. `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`

The 2026-09-08 D6.2 handoff is historical after the new 2026-09-09 handoff is created.

## Later priority after D6

```text
D6 ownership/composition decomposition
-> square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
