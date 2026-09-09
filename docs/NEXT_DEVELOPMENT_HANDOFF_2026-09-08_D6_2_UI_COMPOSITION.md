# Next Development Handoff — D6.2 UI Composition

> Date: 2026-09-09 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/d6-2-ui-composition`
> Draft PR: `#115`
> Latest validated production-code checkpoint: `6a5723af0e9fb646d0c66e900a1c4d215d6a2fef`
> Status: **D6.2a/b/c/d COMPLETE — D6.2e LEGACY STORYTELLER UI REACHABILITY AUDIT NEXT**

## Read first

Treat these as current authority before continuing D6.2:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md`
5. `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`
6. `docs/D6_2C_ARTIST_CONFIRMATION_CONTRACT_AUDIT_2026-09-08.md`
7. this handoff

Always re-confirm live GitHub state before production writes.

## Closed predecessor: D6.1

PR #113 is merged. D6.1 canonical session/GameState ownership is frozen:

```text
ClocktowerGameSession
= canonical writable identity/revisions/semantic chronology/dynamic GameState

App-root PlayerCard / flow state
= presentation + orchestration mirrors
```

Do not reopen broad session authority, Recovery v2, PS5 lifecycle topology, A4 chronology, or derived `cards.toClocktowerGameState(...)` reader migration merely to simplify D6.2.

## D6.2 completed work

### D6.2a — Judge responsibility characterization

Baseline:

```text
ClocktowerJudgeScreen
  103 parameters
   39 callbacks
    3 providers
   10 MutableState<T> parameters

App-root clocktower vars: 41
```

Important findings:

- `records` and `onPhaseChange` have no Judge consumer;
- several Night/Day states are checkpoint/recovery/mechanics coupled;
- no mega `JudgeState`, `JudgeActions`, Controller or broad ViewModel is justified;
- ownership must move by real cohesive UI boundary.

### D6.2b — Slayer transient selection ownership

Validated production checkpoint:

```text
58bc1e51440d44f36e14d1a9d5a45cfe9c235955
```

Result:

```text
Judge parameters:          103 -> 101
MutableState params:        10 -> 8
App-root clocktower vars:   41 -> 39
```

Slayer claimant/target selections moved to Judge-local `remember(gameId)`; durable `onSlayerShot(...)` stayed above.

Acceptance:

```text
R2 34283098478 — PASS
CI 34283098477 — PASS
```

### D6.2c — Artist confirmation-contract characterization

Selected boundary:

```text
Judge-local transient Artist selection
  claimantName
  truthfulAnswer
  shownAnswer

-> onConfirmArtistQuestion(claimantName, truthfulAnswer, shownAnswer)

App durable behavior
  claimed/used flags
  records/events
  Day routing
  game-state revision
```

No Artist state/actions bag was introduced.

### D6.2d — Artist ownership implementation / VALIDATED

Latest validated production checkpoint:

```text
6a5723af0e9fb646d0c66e900a1c4d215d6a2fef
refactor: localize Artist selection ownership [full-ci]
```

Exact net production diff from the D6.2c docs checkpoint `7a80d4c690e41190c5d70430733056caf05b1523`:

```text
CampBoardGameHostApp.kt
  +19 / -50

ClocktowerHostScreen.kt
  +64 / -46

changed production files: exactly 2
commits ahead of D6.2c checkpoint: exactly 1
```

Ownership result:

- App no longer declares/forwards/resets the three transient Artist values;
- Judge owns them with `remember(gameId)`;
- claimant change clears truthful + shown answers;
- truthful-answer change clears shown answer;
- confirm callback now carries all three complete values;
- App still performs durable Artist mechanics/history/revision/day routing;
- both the square-table Artist path and temporary legacy compatibility path consume the same local state helpers;
- no legacy-specific Controller, State, Actions or extra abstraction was added.

Post-D6.2d metrics:

```text
ClocktowerJudgeScreen
  parameters:            95   (103 -> 95 total from D6.2 baseline)
  callbacks:             36   (39 -> 36)
  providers:              3
  MutableState params:    8   (10 -> 8)

App-root clocktower vars: 36   (41 -> 36)
```

A first full-CI attempt exposed Kotlin delegated-property smart-cast errors after moving `artistTruthfulAnswer` local. The repair was deliberately compile-only: both Artist UI paths snapshot the delegated nullable Boolean into a stable local immutable value before Boolean calculations. The branch history was then rebuilt so the failed/bootstrap commits are not in the final production history.

Final acceptance on the clean checkpoint:

```text
R2 34286858464 — PASS
CI 34286858453 — PASS
  Android FULL unit tests — PASS
  debug APK build — PASS
  ASP contract tests — PASS
  Real Clingo cross-validation — PASS
  CI gate — PASS
```

Do not merge PR #115 automatically.

## New product/UI direction constraint

Near-term Storyteller UI direction is now explicit:

> **Operational Storyteller screens should converge on the square-table/table-based UI. Older HostScriptCard-style interaction screens are being retired progressively.**

Architecture consequences for D6.2:

- do not create new abstractions whose main purpose is to preserve legacy HostScriptCard composition;
- do not add dedicated tests that freeze legacy layout/interaction structure unless required for a still-reachable behavior contract;
- during migration, legacy paths may share the same narrow state/action helpers as the square-table path only for minimum compatibility;
- if a legacy block is already unreachable because a newer table path returns earlier, prove that first and delete it rather than decomposing it;
- future ownership cuts should favor the component boundaries intended to survive the square-table migration.

This does **not** authorize changing gameplay semantics, durable mechanics, recovery behavior or recommendation rules as part of UI cleanup.

## D6.2e — NEXT: legacy storyteller fallback reachability audit

A preliminary read-only control-flow audit found a potentially high-value dead-code boundary in `ClocktowerJudgeScreen`.

Before the trailing legacy block:

```text
Dawn -> ClocktowerDawnSummaryScreen -> return

Day Overview -> new table screen -> return
Day Nomination -> new table screen -> return
Day Vote -> new table screen -> return
Day EndConfirm -> new screen -> return
Day Slayer -> square-table screen -> return
Day Artist -> square-table screen -> return
Day Klutz -> square-table screen -> return

FirstNight !nightStarted -> recommendation screen -> return
Night !nightStarted -> ready screen -> return
FirstNight/Night + nightStarted -> ClocktowerNightActiveScreen -> return
```

After those branches, the file still contains a large older:

```text
ClocktowerDarkTheme {
  LazyColumn {
    HostScriptCard / HostProgressCard based Dawn/Day interaction UI
    ...
  }
}
```

This tail **appears** unreachable, but deletion is not yet authorized until D6.2e proves phase/day-mode exhaustiveness and confirms no alternate entry path.

### D6.2e required audit

Read-only first:

1. locate the authoritative `ClocktowerDayMode` definition and enumerate every value;
2. prove that every reachable `ClocktowerPhase` / `nightStarted` / `dayMode` combination returns before the legacy tail;
3. identify the exact start/end range of the unreachable legacy block;
4. inventory functions/imports/helpers that become dead only after deleting that block;
5. check whether any tests directly target legacy-only presentation rather than surviving behavior;
6. estimate line/byte reduction and compile-risk;
7. select one deletion slice only if reachability proof is complete.

Do not combine this audit with `records` / `onPhaseChange`, nomination/vote ownership, Night navigation or durable semantics.

If D6.2e proves the tail unreachable, D6.2f should prioritize deletion of that dead legacy UI over further decomposition of it.

## Frozen invariants

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- exact game/player revision cadence;
- semantic chronology/idempotency/non-mutating preflight;
- no storyteller-hidden information leak;
- Recovery v2 and PS5 persistence lifecycle/write-gate semantics;
- A4 durability/invalidation/prewarm ordering;
- recommendation/gameplay semantics;
- no Compose dependency in session/domain;
- Undercover/Werewolf isolation.

## Repository route

```text
main d76b0854...
-> codex/d6-2-ui-composition
-> D6.2a characterization COMPLETE
-> D6.2b Slayer ownership VALIDATED @ 58bc1e5...
-> D6.2c Artist contract audit COMPLETE
-> D6.2d Artist ownership VALIDATED @ 6a5723a...
-> R2 34286858464 PASS
-> FULL CI 34286858453 PASS
-> draft PR #115 OPEN / DO NOT AUTO-MERGE
-> D6.2e legacy storyteller fallback reachability audit NEXT
```

## Suggested continuation prompt

```text
请读取 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md、docs/D6_2C_ARTIST_CONFIRMATION_CONTRACT_AUDIT_2026-09-08.md 和本 handoff。重新确认 branch codex/d6-2-ui-composition、draft PR #115 与最新 checks。最新验证 production checkpoint 是 6a5723af0e9fb646d0c66e900a1c4d215d6a2fef，R2 34286858464 PASS，CI 34286858453 PASS。接着做 D6.2e，只读证明 ClocktowerJudgeScreen 尾部旧 HostScriptCard/Legacy Storyteller UI 是否在所有 phase/dayMode/nightStarted 组合下都已被新方桌/表格 UI 的 early-return 遮蔽；先完成 exhaustiveness/reachability matrix，不要先删除代码，不要为即将淘汰的 legacy UI 新建 State/Actions/Controller 或专门布局测试。
```
