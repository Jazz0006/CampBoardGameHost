# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-23  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 当前 live `main`：`efd63b360ca9aba8c7890594449aa5e21817f560`（PR #44 merge）  
> 当前工作：**PR #43 Clocktower host source decomposition**  
> 当前 validated implementation head：`854c2464d8a742ba0438fa700bdd2848aa88f4cf`  
> 当前执行点：**A1–A12 完成；下一步 A13 planner-driven Other Night materialization；PR 仍为 draft、未 merge**  
> 当前交接：`NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`

> 新会话实施前必须重新查询 live `main`、PR #43、feature head 和 checks，不得把本文 SHA 当作永久 HEAD。

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
PR #43 Clocktower host source decomposition            DRAFT / A1–A12 GREEN
Current implementation point                           A13 OTHER-NIGHT PLANNER-FIRST MATERIALIZATION
Next product source slice after decomposition          A3 HISTORICAL MULTI-NIGHT EXACT BASELINE
```

当前 live `main`：

```text
efd63b360ca9aba8c7890594449aa5e21817f560
```

该 commit 是 PR #44 merge commit。PR #44 在 A12 前先解决 Drunk shown Fortune Teller 与 actual Fortune Teller setup identity 混淆，确保“醒来身份”和“真实能力/setup 身份”分离。

## 2. 已完成的产品与 correctness 基础

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

PR #42 已合并，完成 shared Global timeline authority、durable semantic action persistence、lifecycle action capture、information observation capture 的 production wiring 和 restore-compatible historical inputs。

因此下一产品阶段可转向 A3 historical multi-night exact baseline，但当前先完成 PR #43 的高价值结构拆分，避免继续在单体式 host 上叠加新的产品实现。

### PR #44 — Drunk shown Fortune Teller correctness prerequisite

A12 准备审计发现：旧 `ClocktowerFlowContext` 只接收 waking/in-play role IDs，会把 Drunk shown Fortune Teller 的醒来身份误当成真实 Fortune Teller setup 身份，从而错误投影 Red Herring setup interaction。

PR #44 将两类 identity 分离：

```text
firstNightWakingRoleIds
  = actual roles + Drunk shown role

firstNightActualRoleIds
  = actual roles only
```

Projector 只对真实角色保留 `STORYTELLER_SETUP`，但仍允许 Drunk 按 shown Townsfolk 的普通醒来 interaction 行动。

```text
PR #44 merge: efd63b360ca9aba8c7890594449aa5e21817f560
CI #524:      SUCCESS
R2 #464:      SUCCESS
```

## 3. PR #43 — Clocktower host source decomposition

### 3.1 目标与边界

```text
branch:    codex/source-decomposition-clocktower-host
PR:        #43 — Refactor: decompose Clocktower host monolith
state:     DRAFT / OPEN / NOT MERGED
main base after A11.1 integration: efd63b360ca9aba8c7890594449aa5e21817f560
validated implementation head:     854c2464d8a742ba0438fa700bdd2848aa88f4cf
```

目标：

- structural refactor only；
- 保留规则、recommendation ordering、persistence、history identity 和 Compose state ownership；
- 让 `ClocktowerJudgeScreen` 收敛为清晰 coordinator / orchestrator；
- 让已经进入 production 的 R5.5 planner / interaction authority 真正成为夜间步骤 materialization 的上游；
- handwritten production source 约 50 KiB 是 **soft maintainability guideline**，不是硬 merge gate；
- 不为了 byte threshold 引入弱抽象、巨型参数袋、额外 `internal` 泄漏、Compose lifetime 移动或 transaction-order 风险；
- 每个 slice 使用 characterization/ownership contract、RED/GREEN（适用时）、local/remote validation、exact diff、GitHub CI/R2；
- 未完成高价值 decomposition 和最终架构审计前不 merge。

### 3.2 A1–A10 已完成

```text
A1  ClocktowerHostCoreSemantics.kt
A2  ClocktowerHostSelectionSemantics.kt
A3  ClocktowerHostPresentationModels.kt
A4  ClocktowerStorytellerRecommendationUi.kt — recommendation screen/reason
A5  ClocktowerStorytellerRecommendationUi.kt — card/editor
A6  ClocktowerPlayerDisplayUi.kt
A7  ClocktowerRegistrationUi.kt
A8  ClocktowerNightStepUi.kt
A9  unreachable legacy fallback cleanup
A10 ClocktowerInformationStepBuilder.kt
```

A8 最终只移动 `ClocktowerNightStepCardLocalized`；`ClocktowerInfoCard` 因仍有 6 个 active host 调用点而保留。A9 删除 unconditional return 后不可达的 legacy UI。A10 只抽 generic stateless information-step packaging，不移动 recommendation、registration state、Compose lifecycle 或 transaction authority。

关键证据：

```text
A8 production GREEN                 fdab916dd8f7e9b4614bf16b79355036ff45fe41
A9 RED                              3ecbcadbd728ac83f7ab1f8d1d40175795e44078
A9 GREEN                            00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
A10 RED                             3377fdbea83727a797afce28064b924a074df5c3
A10 GREEN                           363629ed45f0f044da021f77bb52c5c3ff3c9e20
A10 Host size                       287,597 bytes
A10 builder size                      9,095 bytes
A10 CI #514 / R2 #454               SUCCESS / SUCCESS
```

### 3.3 A11 — Night Step Materialization seam / registry — COMPLETE

A11 建立 planner-first materialization 的稳定桥，但不切生产 Host：

- `ClocktowerProductionFirstNightFlow.interactions()`；
- `ClocktowerProductionOtherNightFlow.interactions()`；
- stateless `ClocktowerNightStepMaterializerRegistry`；
- projected interaction list 控制顺序；
- `SYSTEM_BOUNDARY` 不产生现有 production UI step；
- 只调用 projected actionable interaction 的 lazy builder；
- missing materializer / duplicate identity fail closed；
- extra registered materializers 允许存在但不执行；
- builder 是普通 `() -> ClocktowerNightStepUi`，不是 `@Composable`。

```text
A11 RED:   19bdaf5525c4979f36d44ed0213c0b3c60f4ff7d
A11 GREEN: c893300b8d8dbc7ea845849b81416259da32d485
CI #520:   SUCCESS
R2 #460:   SUCCESS
ASP:       SUCCESS
Real Clingo: SUCCESS
```

### 3.4 A11.1 — integrate PR #44 correctness baseline — COMPLETE

PR #44 merge 后，将新的 main 合入 #43；不 rebase、不 force-push、不开始 A12。

最终组合语义：

- A11 `interactions()` seam 保留；
- `actualRoleIds` 贯穿 FirstNightFlow `interactions()` / `order()`；
- Host 同时保留 `firstNightActualRoleIds` 与 `firstNightWakingRoleIds`；
- A11 registry 不变；
- #44 behavioral hotfix 不作为 #43 的独立行为改动出现。

```text
A11.1 integration head: 4eaa9863070b1eee571169bde737b249379e28ee
CI #526:                SUCCESS
R2 #465:                SUCCESS
```

### 3.5 A12 — planner-driven First Night materialization — COMPLETE

A12 将 First Night 从：

```text
prebuild all supported first-night UI steps
        ↓
filteredNightSteps
        ↓
ClocktowerProductionFirstNightFlow.order(...)
```

切为：

```text
ClocktowerProductionFirstNightFlow.interactions(...)
        ↓
ClocktowerNightStepMaterializerRegistry(FIRST_NIGHT)
        ↓
materialize(projected interactions)
        ↓
ClocktowerNightStepUi
```

First Night 当前注册的 14 个 stable materializer identities：

```text
MINION_INFO
DEMON_INFO
Poisoner
Fortune Teller Red Herring setup
Clockmaker
Washerwoman
Librarian
Investigator
Chef
Empath
Chambermaid
Fortune Teller
Butler
Spy
```

关键保持项：

- planner/projector 是 First Night existence/order authority；
- `actualRoleIds = firstNightActualRoleIds` 与 `inPlayRoleIds = firstNightWakingRoleIds` 分离；
- role/recommendation/registration semantics 不变；
- Compose state/effect lifetime 不移动；
- first-night information migration lifecycle 不移动；
- materializer registry 保持 non-Compose；
- Minion/Demon `stringResource()` 只在 Compose scope 预求值为 String，step 本身仍 lazy 构造；
- Other Night 仍保留旧 `.order(... productionSteps = filteredNightSteps ...)` 路径，A13 尚未开始。

A12 RED 期间发现 `ClocktowerLegacyPlannerDifferentialTest` 仍要求 obsolete First Night `.order()` source call。该测试本意是锁 canonical planner ownership，因此由 Chat 单独迁移为要求 `.interactions()` 且禁止 First Night `.order()`；不是 production workaround。

```text
A12 RED head:                 43f64fc6b2123a35bd9e89b3f6120a8adb7ec809
CI #528:                      EXPECTED FAILURE
Android tests:                669 total / 2 intended wiring failures
assembleDebug:                SUCCESS
ASP / Real Clingo / R2 #467:  SUCCESS / SUCCESS / SUCCESS

legacy contract fix:          3715c5428b52bcce87781fb48ab715338227e19f
A12 GREEN:                    854c2464d8a742ba0438fa700bdd2848aa88f4cf
GREEN exact diff from fix:    1 commit / ClocktowerHostScreen.kt only
A12 remote exact-diff audit:  PASS
CI #530:                      SUCCESS
R2 #469:                      SUCCESS
ASP:                          SUCCESS
Real Clingo:                  SUCCESS
```

A12 后 `ClocktowerHostScreen.kt`：

```text
294,922 bytes
```

尺寸较 A10 略增是 lazy registry wiring 的结构开销，不是职责回流。A12 后审计决定 **不做 A12b 强制 owner extraction**：当前 First Night builder lambdas 捕获大量 Host-derived facts、recommendation helpers 和 registration state；强抽会产生巨型 context bag，属于 architecture-negative extraction。

## 4. 50 KiB policy — soft guideline, not a hard gate

正式优先级：

```text
1. cohesive ownership
2. product / semantic correctness
3. Compose state/effect lifetime stability
4. transaction/callback ordering
5. future feature isolation
6. file-size guideline
```

约 50 KiB 是自然新 owner 的优先目标与 code-review warning，不是 PR #43 merge 的绝对条件。

若高价值拆分完成后 `ClocktowerJudgeScreen` 主要承担 coordinator/orchestration，即使仍明显大于 50 KiB，也可在最终审计后结束 decomposition。不得仅为数字目标继续 architecture-negative extraction。

## 5. 动态多剧本夜间流程的当前真实状态

A12 后 First Night 已完成最后一层 production authority cutover：

```text
ValidatedClocktowerRuleset
        ↓
ClocktowerFlowPlanner
        ↓
ClocktowerHostInteractionProjector
        ↓
stable + conditional ClocktowerHostInteraction
        ↓
ClocktowerProductionFirstNightFlow.interactions()
        ↓
ClocktowerNightStepMaterializerRegistry
        ↓
lazy production First Night UI steps
```

Other Night 仍处于过渡路径：

```text
Host eagerly constructs supported Other Night steps
        ↓
filteredNightSteps
        ↓
ClocktowerProductionOtherNightFlow.order(...)
        ↓
planner-backed exact match / reorder
```

因此 A13 的唯一核心目标是把 **Other Night / conditional-event materialization authority** 做同样 cutover，而不是重新设计 planner。

## 6. A13 — planner-driven Other Night materialization — NEXT

A13 应从 live #43 head 重新审计并 tests-first 开始。

目标架构：

```text
ruleset + waking roles + resolved flow facts
        ↓
ClocktowerProductionOtherNightFlow.interactions(...)
        ↓
ClocktowerNightStepMaterializerRegistry(OTHER_NIGHT)
        ↓
lazy materialize only projected actionable interactions
        ↓
ClocktowerNightStepUi
```

预计需要覆盖当前 production Other Night 的 role/event interactions，例如：

```text
Poisoner
Butler
Empath
Chambermaid
Fortune Teller
Undertaker
Monk
Imp / DemonKill
new Demon identity
Demon succession
Mayor redirect
Sage
Ravenkeeper
Spy
```

新会话必须从源码重新确认 exact identity set，不得仅按本文列表机械实现。

### A13 明确保持的 Host ownership

**不要移动 `advanceNightStep` transaction。** 当前关键顺序继续由 Host 控制：

```text
confirm pending poison / monk / demon choices
-> mayor / successor audits where applicable
-> registration recording
-> semantic event recording
-> night step index / finalization
```

同样不得移动：

- Compose `remember` state / `LaunchedEffect`；
- recommendationCoordinator / telemetry recorder lifetime；
- Spy/Recluse registration mutable state；
- history/session authority；
- player-display commit/migration ordering；
- day routing；
- A3 product behavior。

### A13 tests-first direction

RED 应优先锁 production wiring authority，而不是文件大小：

- Other Night must call `ClocktowerProductionOtherNightFlow.interactions(...)`；
- Other Night must use `ClocktowerNightStepMaterializerRegistry(OTHER_NIGHT)`；
- Other Night must no longer call `.order(... productionSteps = filteredNightSteps ...)`；
- First Night planner-first path must remain unchanged；
- resolved event interactions 的存在性/order 继续来自 projector/planner；
- `advanceNightStep` ownership / ordering contract 保持原位。

在 RED 前先检查是否还有像 A12 `ClocktowerLegacyPlannerDifferentialTest` 一样的旧 source-contract 需要随 authority seam 合法迁移。

## 7. A14 — optional clean day routing

A13 GREEN + full remote audit 后先重新测量 host 和 responsibility cohesion。

只有仍有明显高价值、低耦合 owner 时才进入 A14。优先候选：

```text
Overview
Vote
EndConfirm
```

Nomination/Virgin、Slayer、Artist、Klutz 只有在 owner 边界仍然清晰时继续；如果需要暴露大量 registration/recommendation/state internals，则停止。

A14 是 optional，不是 PR #43 merge 的机械完成条件。

## 8. PR #43 后的下一产品阶段

PR #43 完成、最终审计通过并由用户明确授权 merge 后，下一 product source slice 才进入：

# A3 historical multi-night exact baseline

目标是使用 `EnumeratedWorldSet` 建立跨夜历史 reconstruction 的 exact correctness baseline。

不得把 A3、B4/ZDD promotion、history UI redesign 或 broader manual UI rollout 混入 PR #43。

## 9. 长期架构边界

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

### Host / flow / materialization boundary

```text
ClocktowerJudgeScreen
  -> Compose state/effect lifetime
  -> current snapshot / derived orchestration state
  -> setup recommendation lifecycle
  -> registration mutable state
  -> phase routing
  -> commit / callback ordering

ClocktowerFlowPlanner + ClocktowerHostInteractionProjector
  -> what interactions exist
  -> canonical order
  -> conditional/event interactions

ClocktowerNightStepMaterializerRegistry
  -> stable interaction identity -> lazy step materializer
  -> no Compose state/effect ownership

ClocktowerInformationStepBuilder
  -> generic reliable/unreliable information-step packaging

Day UI owners
  -> optional clean presentation routes only
```

关键规则：

> Planner 决定 **what / when**；materializer 决定 **how to represent the requested interaction as a production step**；Host 保留 **state lifetime / commit ordering**。

### Solver rollout

- A3 `EnumeratedWorldSet`：下一 exact correctness baseline；
- A4 ZDD：exact shadow/prototype，未获 production promotion；
- B4：isolated shadow；
- ZDD production reconsideration 保持 LATER。

## 10. 开发工作方式与 CI 策略

项目级长期工作方式由根目录 `AGENTS.md` 规定。

```text
ChatGPT / Chat
  -> live-state audit
  -> architecture / scope / risk decisions
  -> slice boundary
  -> tests / characterization strategy
  -> implementation specification
  -> remote diff / CI review

Codex / Luna
  -> constrained implementation / local validation executor
```

写入优先级：

```text
GitHub connector 能安全完整读写小文件
  -> Chat 直接修改 + exact remote audit

大文件 / truncation / 多处机械修改 / 本地重型验证更安全
  -> Chat 给精确任务
  -> Luna 只执行 + local validation + commit/push
  -> Chat remote audit
```

Mac / Codex 本地 Gradle：

```bash
GRADLE_USER_HOME="$PWD/.gradle-codex"
```

保持 `.gradle-codex/` untracked。

未经用户明确授权不得 merge。

## 11. 关键证据

```text
PR #39 merge / Decision Foundation        faad0e52dbbe55e1a7cc09c642318d0f6ef99342
PR #40 merge / Empath UI                  205473868b50e159977a8ad34e2cf239a711a79d
PR #41 merge / workflow docs-infra        7bbe754bf02638e311bdb7292792795eb7e18648
PR #42 merge / historical capture         88164a5bba1fa80695a0247538e632d127e5cfa1
PR #44 merge / FT setup identity hotfix    efd63b360ca9aba8c7890594449aa5e21817f560
PR #43 A9 RED                              3ecbcadbd728ac83f7ab1f8d1d40175795e44078
PR #43 A9 GREEN                            00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
PR #43 A10 RED                             3377fdbea83727a797afce28064b924a074df5c3
PR #43 A10 GREEN                           363629ed45f0f044da021f77bb52c5c3ff3c9e20
PR #43 A11 RED                             19bdaf5525c4979f36d44ed0213c0b3c60f4ff7d
PR #43 A11 GREEN                           c893300b8d8dbc7ea845849b81416259da32d485
PR #43 A11.1 integration                   4eaa9863070b1eee571169bde737b249379e28ee
PR #43 A12 RED                             43f64fc6b2123a35bd9e89b3f6120a8adb7ec809
A12 legacy source-contract migration       3715c5428b52bcce87781fb48ab715338227e19f
PR #43 A12 GREEN / validated head          854c2464d8a742ba0438fa700bdd2848aa88f4cf
```

## 12. 新会话启动顺序

1. 读根目录 `AGENTS.md`；
2. 读 `docs/README.md`；
3. 读 `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
4. 读 `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
5. 读本文件；
6. 读 `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`；
7. 查询 live `main`、PR #43、feature head 和 checks；
8. 确认 A12 GREEN head 仍是当前 branch ancestry 中的已验证基线，若 head 已推进则先审推进内容；
9. 从 A13 tests-first 规划开始，不从 main 另开重复 branch；
10. 不让 Luna 自行决定架构；大 Host 的 GREEN 实现可由 Chat 给 exact task 后交 Luna 执行；
11. 未经用户明确授权不得 merge。

## 13. 文档维护规则

- 本文件是当前执行点唯一权威；
- `AGENTS.md` 是长期 AI 协作与执行分工的项目级规范；
- handoff 服务下一次开发；
- specialized design 维护语义边界，不维护 live branch 状态；
- historical audit 不得覆盖本文件；
- 每完成一个 decomposition slice，至少更新 current head、gate、Host size/architecture judgment 和下一规划边界；
- 如果新的用户决策改变长期工作方式或 decomposition completion criterion，应同步更新 `AGENTS.md` 和本 roadmap，而不是只留在聊天记录中。