# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-23  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> PR #43 merge baseline：`520be741fabb47f03ab1fb8852139a1c4cccb9fd`  
> 当前 live `main`：**每次新会话重新查询；可能包含 merge 后的 docs-only commits**  
> 当前工作：**下一 structural task：`CampBoardGameHostApp.kt` App-root decomposition**  
> PR #43 validated implementation head：`b37f0067b674a0cd4bee5ff311840d1c52ce8c05`  
> 当前执行点：**PR #43 已合并；下一任务必须在新对话 / fresh branch 中开始 App-root decomposition；A3 顺延**  
> 下一任务交接：`NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md`

> 新会话实施前必须重新查询 live `main`、目标 PR/branch 和 checks，不得把本文 SHA 当作永久 HEAD。

## 1. 当前状态

```text
Phase A correctness foundation                         PASS
R5.5 Script & Dynamic Flow Foundation                  CLOSED / MERGED
R6 semantic prerequisites                              CLOSED
PR #39 Storyteller Information Decision Foundation     CLOSED / MERGED
PR #40 Structured Manual UI — Empath numeric slice     CLOSED / MERGED
PR #41 developer workflow + LF policy                  CLOSED / MERGED
PR #42 Historical Action + Observation Capture         CLOSED / MERGED
PR #44 Drunk / Fortune Teller setup correctness hotfix CLOSED / MERGED
PR #43 Clocktower host source decomposition            CLOSED / MERGED / A1–A13 GREEN
Optional A14 Host day-routing extraction               SKIPPED
Current execution point                                START APP-ROOT DECOMPOSITION IN NEW CONVERSATION
Next structural task                                   APP-ROOT DECOMPOSITION
Next product task after structural pass                A3 HISTORICAL MULTI-NIGHT EXACT BASELINE
```

PR #43 merge commit：

```text
520be741fabb47f03ab1fb8852139a1c4cccb9fd
```

PR #43 已结束。不得继续在 `codex/source-decomposition-clocktower-host` 上叠加下一任务；下一 structural work 从 live `main` 新开 branch。

## 2. PR #43 — Clocktower host decomposition summary

### 2.1 目标

PR #43 的目标不是机械把 `ClocktowerHostScreen.kt` 压到 50 KiB，而是：

- 提取有清晰 ownership 的 semantics / UI / materialization seam；
- 保留 Blood on the Clocktower 规则、recommendation ordering、registration semantics；
- 保留 persistence/history/global timeline identity；
- 保留 Compose state/effect lifetime；
- 保留 callback/audit/commit transaction ordering；
- 让 R5.5 planner/projector 真正成为 First Night / Other Night interaction existence/order authority；
- 避免 giant parameter bags、弱抽象和仅为 byte threshold 的高风险移动。

### 2.2 已完成 slices

```text
A1   ClocktowerHostCoreSemantics.kt
A2   ClocktowerHostSelectionSemantics.kt
A3   ClocktowerHostPresentationModels.kt
A4   ClocktowerStorytellerRecommendationUi.kt — recommendation screen/reason
A5   ClocktowerStorytellerRecommendationUi.kt — card/editor
A6   ClocktowerPlayerDisplayUi.kt
A7   ClocktowerRegistrationUi.kt
A8   ClocktowerNightStepUi.kt
A9   unreachable legacy fallback cleanup
A10  ClocktowerInformationStepBuilder.kt
A11  ClocktowerNightStepMaterializerRegistry + interactions() seams
A11.1 integrate PR #44 actual-role vs waking-role correctness baseline
A12  First Night planner-first lazy materialization
A13  Other Night planner-first lazy materialization
```

### 2.3 Key evidence

```text
PR #44 merge / correctness baseline       efd63b360ca9aba8c7890594449aa5e21817f560
A9 RED                                    3ecbcadbd728ac83f7ab1f8d1d40175795e44078
A9 GREEN                                  00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
A10 RED                                   3377fdbea83727a797afce28064b924a074df5c3
A10 GREEN                                 363629ed45f0f044da021f77bb52c5c3ff3c9e20
A11 RED                                   19bdaf5525c4979f36d44ed0213c0b3c60f4ff7d
A11 GREEN                                 c893300b8d8dbc7ea845849b81416259da32d485
A11.1 integration                         4eaa9863070b1eee571169bde737b249379e28ee
A12 RED                                   43f64fc6b2123a35bd9e89b3f6120a8adb7ec809
A12 source-contract migration             3715c5428b52bcce87781fb48ab715338227e19f
A12 GREEN                                 854c2464d8a742ba0438fa700bdd2848aa88f4cf
A13 RED                                   4e638c345ed50a3bc65abdc22ac5487172bf9f32
A13 New-Demon contract migration          bae29d5fccb988f641a95e743f899be56ae84299
A13 GREEN / validated implementation      b37f0067b674a0cd4bee5ff311840d1c52ce8c05
A13 CI / R2                               #534 SUCCESS / #473 SUCCESS
Final PR head before merge                3082462fe450db491a8b2c8ed8795fdca60f4b80
Final head CI / R2                        #546 SUCCESS / #485 SUCCESS
PR #43 merge                              520be741fabb47f03ab1fb8852139a1c4cccb9fd
```

Final pre-merge CI confirmed Android tests/build, ASP contract and Real Clingo cross-validation all GREEN.

## 3. Night-flow architecture after A13

First Night and Other Night now use the same authority shape:

```text
ValidatedClocktowerRuleset
        ↓
ClocktowerFlowPlanner
        ↓
ClocktowerHostInteractionProjector
        ↓
stable / conditional ClocktowerHostInteraction
        ↓
ClocktowerProductionFirstNightFlow.interactions(...)
        or
ClocktowerProductionOtherNightFlow.interactions(...)
        ↓
ClocktowerNightStepMaterializerRegistry(FIRST_NIGHT / OTHER_NIGHT)
        ↓
lazy materialize only projected actionable interactions
        ↓
ClocktowerNightStepUi
```

Planner/projector owns **what exists and canonical order**. Materializer registry owns **stable interaction identity -> lazy production UI step**. Host owns **Compose lifetime, derived orchestration state and protected transaction ordering**.

First Night identity split remains protected:

```text
firstNightWakingRoleIds
  = actual roles + Drunk shown role

firstNightActualRoleIds
  = actual roles only
```

Projector only treats actual roles as setup-ability identity while still allowing Drunk to wake/act in the shown role slot.

## 4. Protected Host ownership

`ClocktowerHostScreen.kt` still legitimately owns some stateful coordinator responsibilities:

- Compose `remember` / `LaunchedEffect` lifetime；
- recommendation coordinator / telemetry lifetime；
- Spy/Recluse mutable registration state；
- current-snapshot derived orchestration facts；
- first-night information migration lifecycle；
- player display / observation commit ordering；
- history/session integration wiring；
- phase routing；
- stateful day-action transactions；
- `advanceNightStep` transaction。

`advanceNightStep` conceptual order remains:

```text
confirm poison / monk / demon draft
-> Mayor redirect audit + confirm
-> Demon successor audit
-> Spy registration record
-> Recluse registration record
-> semantic night-step record
-> index advance OR onConfirmNight()
```

A13 added characterization coverage to prevent accidental relocation/reordering.

## 5. Post-PR #43 Host growth rule

PR #43 does **not** mean `ClocktowerHostScreen.kt` is the permanent home for future Clocktower feature code.

From this point forward it is under a **new-responsibility growth freeze**:

- new algorithms -> domain / epistemic / history / recommendation / session owner；
- new role/interaction presentation -> dedicated materializer/UI owner when a cohesive seam exists；
- new persistence/history/session behavior -> dedicated owner；
- Host additions should normally remain thin orchestration, phase routing, current-snapshot wiring or protected stateful transactions；
- if a feature would add hundreds of lines of new policy/UI/algorithm code to Host, stop and identify a natural owner before implementation。

This is not a byte freeze. It is a responsibility-growth constraint.

## 6. Large-file inventory and revised priority

At PR #43 merge, the main handwritten production large files are approximately:

```text
CampBoardGameHostApp.kt      325,556 bytes   NEXT STRUCTURAL PRIORITY
ClocktowerHostScreen.kt      295,644 bytes   A1–A13 high-value pass complete; growth-frozen
ClocktowerDayScreen.kt        63,135 bytes   audit later; split only on natural seam
ClocktowerNightStepUi.kt      45,251 bytes
ClocktowerHistoryScreen.kt    38,365 bytes
ClocktowerStorytellerRecommendationUi.kt 33,459 bytes
WerewolfHostScreen.kt         29,673 bytes
ClocktowerNightScreen.kt      17,833 bytes
ClocktowerSetupScreen.kt      17,362 bytes
MainActivity.kt                1,102 bytes
```

Important distinction:

- earlier R2 **MainActivity decomposition** is complete；
- `MainActivity.kt` is now only Android Activity/window/`setContent` shell；
- the remaining root monolith is `CampBoardGameHostApp.kt` (~325 KiB)。

Therefore “大文件治理”尚未结束，即使 PR #43 已经完成并合并。

## 7. 50 KiB policy

约 50 KiB 是 maintainability guideline，不是机械 gate。

正式优先级：

```text
1. cohesive ownership
2. semantic/product correctness
3. Compose state/effect lifetime stability
4. transaction/callback ordering
5. future feature isolation
6. file-size guideline
```

实践信号：

```text
> 50 KiB   review warning / seek natural owner
> 100 KiB  strong warning / explicit architecture audit
```

不得仅为达成阈值制造 context bag、额外 `internal` 泄漏或弱 owner。

## 8. Current next sequence

用户已决定采用以下顺序：

```text
1. PR #43 merged on main
2. 本对话停止
3. 新对话确认 live main
4. 从 live main 创建 fresh structural branch
5. audit + decompose CampBoardGameHostApp.kt
6. remeasure all large production files
7. audit ClocktowerDayScreen.kt (~63 KiB); only split on clean seam
8. structural pass 完成后再进入 A3 historical multi-night exact baseline
```

**A3 不再是 PR #43 merge 后的直接下一任务。**

下一任务 authority：

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md
```

## 9. App-root decomposition scope

下一 structural PR 的第一步必须是 live audit，不是直接 mass extraction。

重点盘点 `CampBoardGameHostApp.kt` 的 ownership，包括但不限于：

- app navigation / landing / settings shell；
- shared game lifecycle；
- Undercover root state/wiring；
- Werewolf root state/wiring；
- Clocktower root setup/game state；
- Clocktower persistence / restore / archive integration；
- Clocktower session/history/observation wiring；
- role/card pass-phone / reveal；
- result/end-game routing；
- shared root models；
- app-scoped Compose effects/persistence effects。

优先顺序：pure models/helpers -> isolated presentation -> game-specific root wiring -> characterized persistence/history adapters -> only then consider larger state-owner movement if a natural owner has emerged.

禁止把“拆大文件”偷换成 ViewModel/MVI/Redux 全量重构。

## 10. App-root protected invariants

下一 structural PR 必须保持：

- 三游戏入口和导航行为；
- Clocktower 规则和 precedence；
- setup/recommendation ordering；
- registration / impairment semantics；
- persistence / restore / archive；
- historical action / observation ordering and identity；
- `ClocktowerGameSession` global timeline authority；
- Compose state/effect/lifecycle/cancellation semantics；
- cross-game reset behavior；
- transaction/callback ordering；
- PR #43 First/Other Night planner-first architecture；
- Demon transition semantics。

## 11. Explicitly out of scope for the App-root PR

Do not mix:

- A3 historical multi-night product behavior；
- B4/ZDD production promotion；
- history UI redesign；
- misinformation expansion；
- broader manual Storyteller UI rollout；
- new roles/scripts；
- state-management framework migration。

## 12. Product work after structural pass

After App-root decomposition is complete and audited, resume:

# A3 historical multi-night exact baseline

Primary direction remains `EnumeratedWorldSet` as exact correctness baseline before any ZDD production reconsideration.

## 13. Working model and CI

Project-level execution rules live in `AGENTS.md`.

```text
ChatGPT / Chat
  -> live-state audit
  -> architecture / scope / risk decision
  -> characterization/test plan
  -> constrained implementation spec
  -> remote exact-diff / CI review

GitHub connector
  -> preferred safe small-file writer

Codex / Luna
  -> constrained local executor for large/mechanical changes
```

For structural slices require as appropriate:

- focused characterization tests；
- full `:app:testDebugUnitTest`；
- `:app:assembleDebug`；
- `git diff --check`；
- ASP / Real Clingo remote gates；
- exact changed-file audit。

Never merge, mark ready, rebase or force-push without explicit user authorization.

## 14. New-conversation startup

For the next task:

1. confirm live `main` and record the current SHA；
2. confirm PR #43 is merged（merge baseline `520be741fabb47f03ab1fb8852139a1c4cccb9fd`）；
3. read `AGENTS.md`；
4. read this roadmap；
5. read `NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md`；
6. create a fresh structural branch from live `main`；
7. audit `CampBoardGameHostApp.kt` before editing；
8. return responsibility inventory + proposed slice plan + first RED/characterization strategy；
9. only then implement。
