# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
D6.1 merge commit on main: 112572cbd3d990737a412cc4b8ead766d00867e8
merged PR: #113 D6: Clocktower session authority cutover
D6.1 T4 acceptance commit: 30adff1ecb195123c7096757c1454c4a4a61ccca
next phase: D6.2a Judge UI composition characterization
```

Merge validation on `main`:

```text
CI 34225075948 — PASS
  Android full JVM + debug APK — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
Field Test APK 34225075992 — PASS
  FAST tests/build/signature/publish — PASS
```

Always re-confirm live `main` before the next write sequence because docs-only descendants may exist after the merge commit.

## Current priority

> **PS5 COMPLETE → D6.1 COMPLETE / MERGED → D6.2a CHARACTERIZATION NEXT.**

D6.2 must start on a **fresh branch from current `main`**. Do not reopen PR #113 or continue D6.2 work on `codex/d6-root-reaudit`.

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

Residual-root audit `34223904849` measured:

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
App-root clocktower vars 41
```

Conclusion: canonical session/GameState ownership is no longer the dominant architecture problem. The highest-value residual debt is the `ClocktowerJudgeScreen` / `ClocktowerHostScreen.kt` UI-composition surface.

Detailed route audit:

- `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`

## D6.2a — NEXT: UI composition characterization

Before any production edit:

1. create a fresh D6.2 branch from current `main`;
2. classify all 103 `ClocktowerJudgeScreen` parameters by responsibility, phase scope and actual consumers;
3. classify all 39 callbacks into cohesive phase/ability groups;
4. map shared shell context versus FirstNight/Night/Day/Dawn-only inputs;
5. map values forwarded unchanged into existing Day/Night/ability children;
6. classify every `MutableState<T>` parameter by real transient-UI owner;
7. separate selection-only callbacks from callbacks crossing durable/session/domain boundaries;
8. rank the smallest cohesive extraction slice by reduction in cross-phase knowledge and testable blast radius;
9. use existing focused behavior evidence first; add RED only for a genuine stable coverage gap.

### Critical design rule

Do **not** replace 39 callback parameters with one 39-function `Actions` bag. Do **not** create a broad Controller/ViewModel merely to hide the same fan-out. A successful extraction must reduce the number of unrelated concepts known by caller and callee.

Likely shape after characterization:

```text
stable shared Judge read context
+ small phase-specific UI state/action contracts
+ existing Day/Night/ability child owners
```

The exact first extraction group is intentionally not frozen until D6.2a produces consumption evidence.

## Deferred D6 candidates

After D6.2, re-audit rather than following a rigid sequence:

1. cohesive durable day/night orchestration extraction only where real root policy remains;
2. root Recovery composition simplification without changing PS5 schema/lifecycle semantics;
3. final App/HostScreen file-size and callback-fanout audit.

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

The previous `NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md` is closed historical handoff material.

## Later priority after D6

```text
D6 ownership/composition decomposition
-> UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
