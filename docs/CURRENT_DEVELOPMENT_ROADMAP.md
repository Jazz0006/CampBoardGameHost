# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-23  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 当前 live `main`：`88164a5bba1fa80695a0247538e632d127e5cfa1`（PR #42 merge）  
> 当前工作：**PR #43 Clocktower host source decomposition**  
> 当前 feature head：`e1f94fbe01ab95312555ae4524bbc6ad9204b820`  
> 当前执行点：**A1–A8 完成；A9 规划中；PR 仍为 draft、未 merge**  
> 当前交接：`NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`

> 新会话实施前必须重新查询 live `main`、PR #43 和 feature head，不得把本文 SHA 当作永久 HEAD。

## 1. 当前状态

```text
Phase A correctness foundation                         PASS
R5.5 Script & Dynamic Flow Foundation                  CLOSED / MERGED
R6 semantic prerequisites                              CLOSED
PR #39 Storyteller Information Decision Foundation     CLOSED / MERGED
PR #40 Structured Manual UI — Empath numeric slice     CLOSED / MERGED
PR #41 developer workflow + LF policy                  CLOSED / MERGED
PR #42 Historical Action + Observation Capture         CLOSED / MERGED
PR #43 Clocktower host source decomposition            DRAFT / A1–A8 GREEN
Current implementation point                           A9 PLANNING
Next product source slice after decomposition          A3 HISTORICAL MULTI-NIGHT EXACT BASELINE
```

当前 live `main`：

```text
88164a5bba1fa80695a0247538e632d127e5cfa1
```

该 commit 是 PR #42 merge commit。PR #42 已完成 Historical Action + Observation Capture，因此旧文档中把它标为 NEXT 的状态已经作废。

## 2. 已完成的 R6 产品基础

### PR #39 — Storyteller Information Decision Foundation

PR #39 建立 recommendation/manual 共用的 legal decision seam：

```text
actual / registered state
  -> legal information builder
  -> impairment policy
  -> InformationDecisionContext
  -> Storyteller confirmation
  -> shared validation
  -> EpistemicObservationDraft
  -> ClocktowerGameSession authority
```

Recommendation 是建议，不是 durable authority；Global identity / sequence 仍由 session 分配。

### PR #40 — Structured Manual Storyteller UI first slice

当前 production rollout 是 Empath numeric first slice，不是所有信息角色的完整 manual UI rollout。它已覆盖 structured 0/1/2 choice、healthy fallback、prior shown value、assisted recommendation 与 telemetry preview/commit 边界。

### PR #42 — Historical Action + Observation Capture

PR #42 已合并，完成：

- shared Global timeline authority；
- durable semantic action persistence；
- lifecycle action capture；
- information observation capture 的 production wiring；
- restore-compatible historical inputs；
- full CI/R2 validation。

因此下一产品阶段可转向 A3 historical multi-night exact baseline，但当前先完成 PR #43 的结构拆分，避免在 300+ KB host monolith 上叠加新的产品实现。

## 3. PR #43 — Clocktower host source decomposition

### 3.1 目标与边界

PR #43 基于 PR #42 merge：

```text
base main: 88164a5bba1fa80695a0247538e632d127e5cfa1
branch:    codex/source-decomposition-clocktower-host
PR:        #43 — Refactor: decompose Clocktower host monolith
state:     DRAFT / NOT MERGED
head:      e1f94fbe01ab95312555ae4524bbc6ad9204b820
```

目标：

- structural refactor only；
- 保留规则、recommendation ordering、persistence、history identity 和 Compose state ownership；
- handwritten production source 最终不超过 50 KiB，优先不超过 40 KiB；
- 每个 slice 使用 characterization/ownership contract、local validation、exact diff、GitHub CI/R2；
- 未完成整个 decomposition 前不 merge。

### 3.2 A1–A8 已完成

```text
A1  ClocktowerHostCoreSemantics.kt
    core host semantics / stable observation identity / fixed-information helpers

A2  ClocktowerHostSelectionSemantics.kt
    display/decision/registration selection models and unified pool helpers

A3  ClocktowerHostPresentationModels.kt
    recommendation labels, registration/pair enums, ClocktowerNightStepUi

A4  ClocktowerStorytellerRecommendationUi.kt
    recommendation screen and reason summary

A5  ClocktowerStorytellerRecommendationUi.kt
    recommendation card and decision editor

A6  ClocktowerPlayerDisplayUi.kt
    localized player display, player display card, evil info display

A7  ClocktowerRegistrationUi.kt
    SpyRegistrationPanel and RecluseRegistrationPanel

A8  ClocktowerNightStepUi.kt
    ClocktowerNightStepCardLocalized only
```

A8 纠正记录：`ClocktowerInfoCard` 有 6 个 host 调用点，不能在跨文件后继续保持 file-private，因此 A8 最终只移动 night-step composable。A8 production commit：

```text
fdab916dd8f7e9b4614bf16b79355036ff45fe41
```

随后 source-contract test 跟随新 owner：

```text
e1f94fbe01ab95312555ae4524bbc6ad9204b820
```

最终 A8 gate：

```text
ClocktowerNightStepUi.kt                 45,251 bytes
function body exact move audit          PASS (private -> internal only)
ClocktowerInfoCard + 6 call sites        UNCHANGED
CI #503                                  SUCCESS
  Android tests + debug APK              SUCCESS
  ASP contract tests                     SUCCESS
  Real Clingo cross-validation           SUCCESS
R2 #443                                  SUCCESS
```

### 3.3 当前剩余规模

在 A8 后：

```text
ClocktowerHostScreen.kt   319,837 bytes
ClocktowerHostScreen.kt   5,303 lines
```

50 KiB 目标尚未完成。当前文件几乎只剩一个超大的 `ClocktowerJudgeScreen` 与尾部 helper，后续不再只是简单 top-level move；必须先处理可证明的 dead fallback，再逐步分离 state/model construction 与 phase presentation。

## 4. NEXT — A9 规划边界

当前最安全的下一候选不是立即拆分 state ownership，而是先删除 A8 后确认存在的 unreachable legacy fallback：

```text
ClocktowerDarkTheme { ... }
return
LazyColumn { ... legacy fallback ... }   <- unreachable
private fun ClocktowerInfoCard(...)       <- only used by that fallback
```

候选范围约 25.8 KB / 513 lines。A9 目前是**规划状态，尚未提交 tests 或 production 改动**。

进入 A9 前必须：

1. 用 source contract 锁定 unconditional `return` 后 legacy `LazyColumn` 不应存在；
2. 更新 A8 ownership test，不再把 `ClocktowerInfoCard` 固定为 host owner；
3. GREEN 只删除 unreachable fallback 与其私有专用 helper；
4. 不触碰 `ClocktowerDarkTheme` 内实际 production UI；
5. 不修改 day/night flow、registration、recommendation、history/session ownership；
6. local focused/full tests + assembleDebug；
7. exact deletion audit + GitHub CI/R2；
8. A9 完成后重新测量 host，并重新规划更深层 extraction。

## 5. PR #43 后的下一产品阶段

PR #42 已提供 historical action/observation inputs。PR #43 完成、通过最终审计并由用户明确授权 merge 后，下一 product source slice 才进入：

# A3 historical multi-night exact baseline

其目标是使用 `EnumeratedWorldSet` 建立跨夜历史 reconstruction 的 exact correctness baseline。不得把 A3、B4/ZDD promotion、history UI redesign 或 broader manual UI rollout 混入 PR #43。

## 6. 当前长期架构边界

### Registration 与 impairment

```text
actual world
  -> registration projection
  -> truthful result / legal information space
  -> impairment policy
  -> storyteller decision
```

### Session authority

UI、recommendation、manual selector、history adapter 都不得自行分配 Global timeline identity。Global identity / sequence 仍由 `ClocktowerGameSession` authority 负责。

### Solver rollout

- A3 `EnumeratedWorldSet`：下一 exact correctness baseline；
- A4 ZDD：exact shadow/prototype，未获 production promotion；
- B4：isolated shadow；
- ZDD production reconsideration 保持 LATER。

## 7. 开发与 CI 策略

Behavior change：

```text
live state recheck
-> focused branch
-> tests-only RED
-> real CI RED
-> smallest GREEN
-> focused/full tests + APK
-> ASP + Real Clingo + R2
-> exact diff + final review
-> explicit merge authorization
```

Pure structural refactor：

```text
live state recheck
-> characterization/ownership contract
-> Luna mechanical implementation when large-file work is safer locally
-> local focused/full validation
-> remote exact diff
-> GitHub CI/R2
-> boundary re-audit before next slice
```

规范：

- `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`
- `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`

## 8. 关键证据

```text
PR #39 merge / Decision Foundation        faad0e52dbbe55e1a7cc09c642318d0f6ef99342
PR #40 merge / Empath UI                  205473868b50e159977a8ad34e2cf239a711a79d
PR #41 merge / workflow docs-infra        7bbe754bf02638e311bdb7292792795eb7e18648
PR #42 merge / historical capture         88164a5bba1fa80695a0247538e632d127e5cfa1
PR #43 A6 GREEN                           0cdc8fec53980da986e7bb723e9dd73833b177a8
PR #43 A7 GREEN                           be7234210f8d6249e6da8237cbd0f48ee4708dd7
PR #43 A8 production GREEN                fdab916dd8f7e9b4614bf16b79355036ff45fe41
PR #43 current validated head             e1f94fbe01ab95312555ae4524bbc6ad9204b820
```

## 9. 新会话启动顺序

1. 读 `docs/README.md`；
2. 读 `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
3. 读 `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
4. 读本文件；
5. 读 `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`；
6. 查询 live `main`、PR #43、feature head 和 checks；
7. 若 PR #43 仍在开发，从其 live head 继续，不从 main 另开重复 branch；
8. 当前只规划/实施 A9，不提前进入 A3 product work；
9. 未经用户明确授权不得 merge。

## 10. 文档维护规则

- 本文件是当前执行点唯一权威；
- handoff 服务下一次开发；
- specialized design 维护语义边界，不维护 live branch 状态；
- historical audit 不得覆盖本文件；
- 每完成一个 decomposition slice，至少更新当前 head、gate、剩余规模和下一规划边界。
