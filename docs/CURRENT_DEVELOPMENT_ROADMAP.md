# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-09 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
D6.1 merge commit on main: 112572cbd3d990737a412cc4b8ead766d00867e8
merged PR: #113 D6: Clocktower session authority cutover
D6.2 branch base main: d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e
D6.2 latest validated production checkpoint: 6a5723af0e9fb646d0c66e900a1c4d215d6a2fef
draft PR: #115 D6.2 UI composition
next phase: D6.2e legacy Storyteller UI reachability characterization
```

D6.2d validation on the exact production checkpoint:

```text
R2 34286858464 — PASS
CI 34286858453 — PASS
  Android FULL unit tests — PASS
  debug APK build — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
```

Docs-only commits may follow the validated production checkpoint. Keep production-code checkpoint and branch/documentation head distinct when handing off.

## Current priority

> **PS5 COMPLETE → D6.1 COMPLETE / MERGED → D6.2a/b/c/d COMPLETE / VALIDATED → D6.2e LEGACY UI REACHABILITY AUDIT NEXT.**

D6.2 remains on `codex/d6-2-ui-composition`, originally cut from main checkpoint `d76b0854...`. Do not reopen PR #113 or move D6.2 work back to `codex/d6-root-reaudit`.

## UI direction now constraining D6.2

Near-term Storyteller UX direction is explicit:

> **Operational Storyteller interactions should converge on the square-table/table-based UI. Older HostScriptCard-style interaction screens are being retired progressively.**

Therefore D6.2 should:

- optimize boundaries that survive the square-table migration;
- avoid new State/Actions/Controller abstractions whose main purpose is preserving legacy HostScriptCard UI;
- avoid tests that freeze obsolete layout/composition structure unless a still-reachable behavior contract needs them;
- prove legacy reachability before spending more decomposition effort there;
- delete dead legacy UI when safely proven unreachable instead of refactoring it.

This UI direction does **not** relax gameplay/session/recovery/recommendation invariants.

## D6.1 — COMPLETE / ACCEPTED / MERGED

D6.1 selected `ClocktowerGameSession` as the strongest existing canonical writable owner rather than inventing a new Manager/Controller.

Accepted ownership:

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

Dynamic canonical mechanics include actual/shown role identity, alive/death and poison state. Exact revision cadence remains frozen.

PR #113 merged as:

```text
112572cbd3d990737a412cc4b8ead766d00867e8
```

Detailed evidence:

- `docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md`
- `docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`

## Post-D6.1 residual re-audit — COMPLETE

Residual-root audit `34223904849` measured the D6.2 baseline:

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
App-root clocktower vars 41
```

Conclusion: canonical session/GameState ownership was no longer the dominant debt. The highest-value residual problem became `ClocktowerJudgeScreen` / `ClocktowerHostScreen.kt` UI-composition fan-out.

Detailed route audit:

- `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`

## D6.2a — COMPLETE: UI composition characterization

Complete 103-row consumption/responsibility matrix:

- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

Baseline:

```text
103 total Judge parameters
39 callbacks
3 additional function-valued providers
10 MutableState<T> parameters
```

Key conclusions:

- `records` and `onPhaseChange` are zero-consumer Judge inputs;
- Night navigation and several Day/vote states are checkpoint/recovery/mechanics coupled;
- no broad `ClocktowerJudgeActions`, `DayState`, Controller or ViewModel bag is justified;
- ownership must move by real cohesive UI boundary rather than parameter packaging.

## D6.2b — COMPLETE / VALIDATED: Slayer UI selection ownership

Production checkpoint:

```text
58bc1e51440d44f36e14d1a9d5a45cfe9c235955
```

Ownership result:

```text
App no longer owns/forwards Slayer claimant + target MutableState.
Judge owns both with remember(gameId).
onSlayerShot(claimantName, targetName, recluseRegistersAsDemon)
remains the durable action boundary.
```

Metrics:

```text
ClocktowerJudgeScreen    101 parameters / 39 callbacks
MutableState parameters    8
App-root clocktower vars   39
```

Validation:

```text
R2 34283098478 — PASS
CI 34283098477 — PASS
```

## D6.2c — COMPLETE: Artist confirmation-contract characterization

Audit:

- `docs/D6_2C_ARTIST_CONFIRMATION_CONTRACT_AUDIT_2026-09-08.md`

Selected architecture:

```text
Judge-local transient Artist selection
  claimantName
  truthfulAnswer
  shownAnswer

-> onConfirmArtistQuestion(claimantName, truthfulAnswer, shownAnswer)

App durable Artist behavior
  artistUsed / artistClaimedNames
  records / events
  Day routing
  game-state revision
```

The audit explicitly rejected Artist state/actions bags.

## D6.2d — COMPLETE / VALIDATED: Artist transient selection ownership

Validated production checkpoint:

```text
6a5723af0e9fb646d0c66e900a1c4d215d6a2fef
refactor: localize Artist selection ownership [full-ci]
```

Net production diff from D6.2c checkpoint `7a80d4c690e41190c5d70430733056caf05b1523`:

```text
CampBoardGameHostApp.kt     +19 / -50
ClocktowerHostScreen.kt     +64 / -46
changed production files: exactly 2
commit count: exactly 1
```

Result:

- removed the three App-root Artist transient vars;
- removed their App reset/forwarding plumbing;
- removed three Artist selection callbacks from Judge;
- Judge owns claimant/truthful/shown selection with `remember(gameId)`;
- changing claimant invalidates truthful/shown answers;
- changing truthful answer invalidates shown answer;
- durable confirmation is now `(String, Boolean, Boolean) -> Unit`;
- durable Artist mechanics/history/revision/day routing remain in App;
- square-table and temporary legacy Artist consumers share the same narrow local helpers;
- no replacement DTO/controller abstraction was introduced.

Post-D6.2d metrics:

```text
ClocktowerJudgeScreen
  parameters:            95   (103 -> 95 from baseline)
  callbacks:             36   (39 -> 36)
  providers:              3
  MutableState params:    8   (10 -> 8)

App-root clocktower vars: 36   (41 -> 36)
```

A first full-CI attempt exposed Kotlin delegated-property smart-cast errors after `artistTruthfulAnswer` became Compose-local. The repair only snapshots the nullable delegated value into a stable immutable local value before Boolean calculations in the new square-table path and the legacy compatibility path. The final branch history was rebuilt so failed/bootstrap commits do not remain in the production line.

Final acceptance:

```text
R2 34286858464 — PASS
CI 34286858453 — PASS
  :app:testFull + :app:assembleDebug — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
```

Draft PR #115 remains open. Do not merge automatically.

## D6.2e — NEXT: legacy Storyteller UI reachability audit

A preliminary read-only audit found a potentially much larger payoff than another small parameter cut.

`ClocktowerJudgeScreen` already has early-return modern/table paths for:

```text
Dawn
Day Overview
Day Nomination
Day Vote
Day EndConfirm
Day Slayer
Day Artist
Day Klutz
FirstNight before night start
Night before night start
FirstNight/Night active flow
```

After those returns, the file still contains a large older `ClocktowerDarkTheme { LazyColumn { ... } }` block using HostScriptCard/HostProgressCard-style Dawn/Day interaction UI.

Current evidence strongly suggests this tail may be entirely unreachable, but deletion is **not yet authorized**.

D6.2e is read-only first and must:

1. locate the authoritative `ClocktowerDayMode` definition and enumerate every value;
2. build an exhaustive phase / nightStarted / dayMode reachability matrix;
3. prove every reachable state returns before the legacy tail, or identify the exact remaining reachable cases;
4. identify the exact legacy block start/end boundaries;
5. inventory imports/helpers/functions made dead by its removal;
6. inspect tests for legacy-only layout coupling versus surviving behavior contracts;
7. estimate byte/line reduction and risk;
8. authorize deletion only if the proof is complete.

If the block is proven unreachable, D6.2f should delete it before spending effort decomposing it.

Do not mix D6.2e with:

- `records` / `onPhaseChange` cleanup;
- nomination/vote ownership;
- Night checkpoint/navigation ownership;
- recommendation behavior;
- Recovery/session changes.

## Deferred D6 candidates

Re-rank after D6.2e rather than following an old rigid sequence:

1. dead legacy storyteller UI deletion if D6.2e proves it unreachable;
2. `records` / `onPhaseChange` dead-parameter cleanup as a separate tiny slice if still useful;
3. nomination/vote subsets with Recovery coupling preserved;
4. whole Day dispatcher only after surviving square-table seams are clean;
5. Night navigation only with checkpoint/recovery-safe ownership;
6. cohesive durable orchestration extraction only where real root policy remains;
7. root Recovery composition simplification without changing PS5 schema/lifecycle semantics;
8. final App/HostScreen file-size and callback-fanout audit.

Broad `cards.toClocktowerGameState(...)` reader replacement remains explicitly **not** a decomposition goal.

## Frozen invariants

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- no synthetic NGJ `RulesetRef`;
- exact game/player revision cadence and ordering;
- one monotonic collision-free semantic chronology;
- action/observation idempotency and non-mutating preflight;
- no storyteller-hidden target leak;
- Recovery v2 current-version-only policy;
- SideEffect / ON_PAUSE / ON_STOP + `RecoveryWriteGate` topology;
- A4 durability/invalidation/prewarm ordering;
- no Compose dependency in session/domain;
- gameplay/recommendation semantics unless a separately authorized feature task changes them;
- Undercover/Werewolf isolation.

## Active handoff

Use:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_2_UI_COMPOSITION.md`
- `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`
- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`
- `docs/D6_2C_ARTIST_CONFIRMATION_CONTRACT_AUDIT_2026-09-08.md`

The previous D6 post-persistence handoff is historical only.

## Later priority after D6

```text
D6 ownership/composition decomposition
-> square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
