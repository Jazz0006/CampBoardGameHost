# Next Development Handoff — D6.2 UI Composition

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> D6.1 merge commit: `112572cbd3d990737a412cc4b8ead766d00867e8`
> Status: **D6.1 MERGED / MAIN VALIDATED — D6.2a CHARACTERIZATION NEXT**

## Read first

Treat these as current authority before doing D6.2 work:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`
5. this handoff
6. `docs/D6_1E_ACCEPTANCE_PROGRESS_2026-09-08.md` only for accepted D6.1 evidence
7. specialized UI/rules docs only when the selected slice needs them

Always re-confirm live GitHub state before writes.

## Closed predecessor: D6.1

PR #113 is merged. Do not reopen its branch for new work.

```text
merge commit: 112572cbd3d990737a412cc4b8ead766d00867e8
CI 34225075948 — PASS
Field Test APK 34225075992 — PASS
```

The merge CI passed Android full JVM + debug APK, ASP contracts, Real Clingo and the aggregate gate. Field Test APK passed FAST/build, identity/version, signing and stable release publication.

Accepted D6.1 architecture remains frozen:

```text
ClocktowerGameSession
= canonical writable identity/revisions/semantic chronology/dynamic GameState

App-root PlayerCard / flow state
= presentation + orchestration mirrors
```

Do not reopen broad session/GameState ownership, Recovery v2, PS5 lifecycle topology or derived `cards.toClocktowerGameState(...)` reader migration merely to simplify D6.2.

## Why D6.2 is next

Post-D6.1 residual audit `34223904849` passed and found:

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
App-root clocktower vars 41
```

The dominant remaining debt is now UI composition fan-out. D6.2 should reduce cross-phase knowledge and change amplification around Judge/Host composition while preserving domain/session ownership.

## D6.2a — first task: characterization only

Do not edit production first.

### Step 1 — live state / branch

1. re-confirm current `main`;
2. confirm PR #113 remains merged;
3. create a fresh branch from current `main` (recommended: `codex/d6-2-ui-composition`);
4. do not reuse `codex/d6-root-reaudit`.

### Step 2 — build the full consumption matrix

For all 103 `ClocktowerJudgeScreen` parameters record:

```text
name
kind: value / MutableState / callback
responsibility group
phase scope: shared / FirstNight / Night / Dawn / Day
actual consumer(s)
forwarded unchanged?
transient UI vs durable/domain data
current owner
existing focused tests
candidate cohesive contract
```

For all 39 callbacks additionally classify:

- selection-only/transient;
- flow/navigation;
- semantic event/observation;
- durable/session/domain boundary;
- ability-specific action;
- phase-level commit.

### Step 3 — map existing owners

Explicitly map:

- `ClocktowerDayScreen` consumption;
- `ClocktowerNightScreen` consumption;
- `ClocktowerNightStepUi`/ability presentation seams;
- History/recommendation components;
- `MutableState<T>` values still owned by App/Host composition;
- callbacks that are merely forwarded versus interpreted in Judge.

### Step 4 — rank candidate first slices

Prefer the smallest group that:

- removes unrelated concepts from both App and HostScreen;
- belongs to one real phase or child owner;
- has clear transient UI ownership;
- does not own canonical mechanical/domain state;
- has strong existing focused characterization;
- reduces real fan-out rather than hiding it.

Do not freeze the first implementation slice until this matrix is complete.

## Explicit anti-patterns

Do not:

- create one mega `ClocktowerJudgeActions` with 39 functions;
- create one giant Judge state bag with most of the 103 values;
- introduce a broad Controller/ViewModel to move the same coupling behind one parameter;
- move Compose types into session/domain code;
- combine unrelated Night/Day/ability responsibilities because they share a screen;
- change user-visible behavior during structural slices;
- mix Recovery/session/recommendation redesign into D6.2.

## Testing strategy

D6.2a is read/design-first. Existing tests should be treated as characterization evidence.

For each selected implementation slice:

1. identify affected focused tests first;
2. add a new typed RED only if a genuine stable coverage gap exists;
3. use fail-closed large-file workflow for `ClocktowerHostScreen.kt` or `CampBoardGameHostApp.kt` edits;
4. require exact changed-file/anchor audit;
5. focused/T0 → FAST/T1 → affected T2 according to `TESTING_STRATEGY.md`;
6. re-audit parameter/callback fan-out after each completed slice;
7. reserve T4 for a logical acceptance checkpoint, not every structural move.

## Frozen invariants

Preserve:

- `ClocktowerGameSession` canonical ownership;
- exact game/player revision cadence;
- semantic chronology/idempotency/non-mutating preflight;
- no storyteller-hidden information leak;
- Recovery v2 and PS5 persistence lifecycle/write-gate semantics;
- A4 durability/invalidation/prewarm ordering;
- recommendation/gameplay semantics;
- current user-visible behavior for structural slices;
- no Compose dependency in session/domain;
- Undercover/Werewolf isolation.

## Suggested continuation prompt for the new conversation

```text
请读取根目录 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md 和 docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_2_UI_COMPOSITION.md。先重新确认 live main 和 PR #113 已 merged；D6.1 已完成并在 main 上通过 CI 34225075948 与 Field Test APK 34225075992。然后从最新 main 创建独立 D6.2 分支，不要复用 codex/d6-root-reaudit。开始 D6.2a，只读审计 ClocktowerJudgeScreen 的 103 个参数、39 个 callbacks、MutableState ownership、phase scope、actual consumers 和 child-screen forwarding，形成完整 consumption/responsibility matrix，最后选出最小的真实 cohesive UI boundary。不要先改 production，不要用 mega Actions/State bag 或 broad Controller/ViewModel 伪装解耦。
```
