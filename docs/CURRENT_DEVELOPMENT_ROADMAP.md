# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
live main: 2c495ee547e8327b0d3c3a811f5c891ba34fd863
active branch: codex/d6-root-reaudit
active draft PR: #113 D6: Clocktower session authority cutover
D6.1 T4 acceptance commit: 30adff1ecb195123c7096757c1454c4a4a61ccca
post-D6.1 residual audit cleanup head: b61af7e7029f8b7df441fb32b35c1fed3a2d4b18
```

The acceptance commit and post-audit cleanup have the same tree SHA:

```text
dfe4ad99f331361697ea8976c7d72fb529106d54
```

So no unvalidated production or document change exists after the accepted D6.1 tree.

PR #113 remains **Draft / open / mergeable / not merged**. Do not merge automatically.

## Current priority

> **PS5 COMPLETE → D6.0 COMPLETE → D6.1 COMPLETE / T4 GREEN → user-approved PR #113 merge → D6.2 UI composition boundary characterization.**

Do not append D6.2 production work to PR #113. D6.1 is a complete logical checkpoint and should remain independently reviewable.

## D6.1 — COMPLETE: Clocktower session authority

D6.1 selected the strongest existing owner, `ClocktowerGameSession`, instead of inventing a new Manager/Controller.

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

Key evidence:

```text
D6.1d global ownership audit 34221212685 — PASS
D6.1e compatibility/read-side audit 34222474745 — PASS
T4 acceptance commit 30adff1ecb195123c7096757c1454c4a4a61ccca
CI 34223133695 — PASS
R2 34223133706 — PASS
Android full JVM + debug APK — PASS
ASP contracts — PASS
Real Clingo — PASS
CI gate — PASS
```

Recovery/persistence, Clocktower rules, A4 epistemic production code and Werewolf were not redesigned by the canonical writer cutover.

Detailed evidence:

- `docs/D6_1D_GAME_STATE_PROGRESS_2026-09-08.md`
- `docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md`

## Post-D6.1 global re-audit — COMPLETE

Residual-root audit:

```text
34223904849 — PASS
```

Measured current hotspots:

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
App-root clocktower vars 41
```

This changes the D6 priority. Canonical state ownership is no longer the dominant problem; the highest-value residual architecture debt is the `ClocktowerJudgeScreen` / `ClocktowerHostScreen.kt` UI-composition surface.

Detailed route audit:

- `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`

## D6.2 — NEXT: UI composition boundary decomposition

### D6.2a first — characterization only

Before production edits:

- build a 103-parameter consumption/responsibility matrix;
- group 39 callbacks by actual phase/ability responsibility;
- map shared shell context versus FirstNight/Night/Day/Dawn-only inputs;
- map existing child-screen owners and unchanged forwarding;
- classify `MutableState<T>` ownership;
- separate transient UI selection callbacks from durable/session boundaries;
- rank the smallest cohesive extraction slice;
- use existing focused behavior evidence first; add RED only for a genuine stable coverage gap.

### Critical design rule

Do not replace 39 callback parameters with one 39-function `Actions` bag. Do not create a broad Controller/ViewModel. A successful extraction must reduce cross-phase knowledge and dependency fan-out, not hide it.

Likely direction after characterization:

```text
shared Judge read context
+ small phase-specific UI state/action contracts
+ existing Day/Night/ability child owners
```

The exact first group is not frozen until D6.2a consumption evidence exists.

## Deferred D6 candidates

After D6.2, re-audit rather than following a rigid sequence:

1. cohesive durable day/night orchestration extraction, only where existing planners/session boundaries leave real root policy;
2. root Recovery composition simplification without changing PS5 schema/lifecycle semantics;
3. final root/HostScreen file-size and callback-fanout re-audit.

Broad `cards.toClocktowerGameState(...)` reader replacement is explicitly **not** a decomposition goal.

## Frozen invariants

Preserve:

- no synthetic NGJ `RulesetRef`;
- exact revision cadence/order;
- one monotonic collision-free semantic chronology;
- action/observation idempotency and non-mutating preflight;
- no storyteller-hidden target leak;
- Recovery v2 current-version-only policy;
- SideEffect / ON_PAUSE / ON_STOP + `RecoveryWriteGate` topology;
- A4 durability/invalidation/prewarm ordering;
- no Compose dependency in session/domain;
- no gameplay/user-visible semantic change;
- Undercover/Werewolf isolation.

## Later priority after D6

```text
D6 ownership/composition decomposition
-> UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
