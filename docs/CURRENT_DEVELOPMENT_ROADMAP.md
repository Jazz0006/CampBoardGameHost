# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
D6.1 merge commit on main: 112572cbd3d990737a412cc4b8ead766d00867e8
merged PR: #113 D6: Clocktower session authority cutover
D6.2 branch base main: d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e
D6.2 latest validated production checkpoint: 58bc1e51440d44f36e14d1a9d5a45cfe9c235955
draft PR: #115 D6.2: localize Slayer UI selection ownership
next phase: D6.2c Artist confirmation-contract characterization
```

D6.2b validation on the exact production checkpoint:

```text
R2 34283098478 — PASS
CI 34283098477 — PASS
  Android FULL unit tests — PASS
  debug APK build — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
```

Docs-only commits may follow the validated production checkpoint. Keep production-code checkpoint and branch/documentation head distinct when handing off.

## Current priority

> **PS5 COMPLETE → D6.1 COMPLETE / MERGED → D6.2a COMPLETE → D6.2b COMPLETE / VALIDATED → D6.2c CHARACTERIZATION NEXT.**

D6.2 remains on the fresh branch `codex/d6-2-ui-composition`, originally cut from current-main checkpoint `d76b0854...`. Do not reopen PR #113 or move D6.2 work back to `codex/d6-root-reaudit`.

## D6.1 — COMPLETE / ACCEPTED / MERGED

D6.1 selected the strongest existing owner, `ClocktowerGameSession`, rather than inventing a new Manager/Controller.

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

Dynamic canonical mechanics now include actual/shown role identity, alive/death and poison state. Poison `+1` versus `+0` revision cadence is preserved exactly.

Acceptance evidence:

```text
D6.1d global ownership audit 34221212685 — PASS
D6.1e compatibility/read-side audit 34222474745 — PASS
T4 CI 34223133695 — PASS
T4 R2 34223133706 — PASS
post-D6.1 residual audit 34223904849 — PASS
```

PR #113 was explicitly user-authorized and merged into `main` as:

```text
112572cbd3d990737a412cc4b8ead766d00867e8
```

The merge push then passed full CI and Field Test APK publication. D6.1 is closed; do not append new D6.2 production work to its old branch/PR.

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

Conclusion: canonical session/GameState ownership was no longer the dominant architecture problem. The highest-value residual debt was the `ClocktowerJudgeScreen` / `ClocktowerHostScreen.kt` UI-composition surface.

Detailed route audit:

- `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`

## D6.2a — COMPLETE: UI composition characterization

The fresh-branch read-only audit produced the complete 103-row Judge consumption/responsibility matrix:

- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

Exact baseline decomposition:

```text
103 total Judge parameters
39 callbacks
3 additional function-valued providers
10 MutableState<T> parameters
```

Key conclusions:

- `records` and `onPhaseChange` are zero-consumer Judge inputs and remain separate cleanup candidates;
- Night navigation and several Day/vote states are Recovery/checkpoint/mechanics coupled and cannot simply become child-local;
- Slayer claimant/target selection was the smallest real cohesive ownership boundary;
- no broad `ClocktowerJudgeActions`, `DayState`, Controller or ViewModel bag was justified.

## D6.2b — COMPLETE / VALIDATED: Slayer UI selection ownership

Production checkpoint:

```text
58bc1e51440d44f36e14d1a9d5a45cfe9c235955
refactor: localize Slayer selection ownership [full-ci]
```

Exact production diff from D6.2a:

```text
CampBoardGameHostApp.kt  +0 / -8
ClocktowerHostScreen.kt  +2 / -4
```

Ownership result:

```text
App no longer owns or forwards Slayer claimant/target MutableState.
ClocktowerJudgeScreen owns the two transient selections with remember(gameId).
onSlayerShot(claimantName, targetName, recluseRegistersAsDemon)
remains the complete durable action boundary.
```

Post-slice metrics:

```text
ClocktowerJudgeScreen    101 parameters / 39 callbacks
MutableState parameters    8
App-root clocktower vars   39
```

The reduction is deliberately small but real: ownership moved to its actual UI consumer rather than being hidden inside a replacement bag.

No change was made to:

- `slayerUsed` / `slayerClaimedNames` durable mechanics;
- outcome/session/history/revision semantics;
- Recovery v2 / PS5;
- Artist, nomination/vote or Night navigation;
- Undercover/Werewolf flows.

Validation:

```text
R2 34283098478 — PASS
CI 34283098477 — PASS
  :app:testFull + :app:assembleDebug — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
```

Draft PR #115 is open. Do not merge automatically.

## D6.2c — NEXT: Artist confirmation-contract characterization

Artist is the next strongest candidate, but unlike Slayer its durable confirmation callback currently reads App-owned transient values.

Current App-owned selection values:

```text
clocktowerArtistClaimantName
clocktowerArtistTruthfulAnswer
clocktowerArtistShownAnswer
```

Before any Artist production edit:

1. map the exact `ClocktowerArtistTableScreen` selection/confirmation flow;
2. audit every read/write/reset consumer of the three Artist transient values;
3. inspect existing Artist-focused tests and use them before inventing new coverage;
4. determine whether `onConfirmArtistQuestion` can become a narrow value-carrying callback without moving durable behavior;
5. keep `artistUsed`, `artistClaimedNames`, records/events, revisions and Day routing above;
6. add a typed RED only if the exact confirmation contract reveals a real stable coverage gap;
7. do not combine Artist with nomination/vote, Night navigation or unrelated dead-parameter cleanup.

Do not create `ArtistState` / `ArtistActions` bags merely for symmetry.

## Deferred D6 candidates

After the Artist evidence, re-rank rather than following a rigid sequence:

1. `records` / `onPhaseChange` dead-parameter cleanup as a separate trivial slice if still useful;
2. nomination/vote subsets with Recovery coupling explicitly preserved;
3. whole Day dispatcher only after smaller ownership seams are cleaner;
4. Night navigation only with a checkpoint/recovery-safe contract;
5. cohesive durable day/night orchestration extraction only where real root policy remains;
6. root Recovery composition simplification without changing PS5 schema/lifecycle semantics;
7. final App/HostScreen file-size and callback-fanout audit.

Broad `cards.toClocktowerGameState(...)` reader replacement is explicitly **not** a decomposition goal.

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
- recommendation/gameplay/user-visible behavior for structural D6.2 slices;
- Undercover/Werewolf isolation.

## Active handoff

Use:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_2_UI_COMPOSITION.md`
- `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`
- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

The previous `NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md` is closed historical handoff material.

## Later priority after D6

```text
D6 ownership/composition decomposition
-> UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
