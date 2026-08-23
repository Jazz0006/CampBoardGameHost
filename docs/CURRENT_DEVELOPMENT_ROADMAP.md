# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-23  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 当前 live `main`：`88164a5bba1fa80695a0247538e632d127e5cfa1`（PR #42 merge）  
> 当前工作：**PR #43 Clocktower host source decomposition**  
> 当前 validated A10 head：`363629ed45f0f044da021f77bb52c5c3ff3c9e20`  
> 当前执行点：**A1–A10 完成；A11 Night Step Materialization seam / registry；PR 仍为 draft、未 merge**  
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
PR #43 Clocktower host source decomposition            DRAFT / A1–A10 GREEN
Current implementation point                           A11 NIGHT STEP MATERIALIZATION SEAM
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

因此下一产品阶段可转向 A3 historical multi-night exact baseline，但当前先完成 PR #43 的高价值结构拆分，避免继续在单体式 host 上叠加新的产品实现。

## 3. PR #43 — Clocktower host source decomposition

### 3.1 目标与边界

PR #43 基于 PR #42 merge：

```text
base main: 88164a5bba1fa80695a0247538e632d127e5cfa1
branch:    codex/source-decomposition-clocktower-host
PR:        #43 — Refactor: decompose Clocktower host monolith
state:     DRAFT / NOT MERGED
validated implementation head: 363629ed45f0f044da021f77bb52c5c3ff3c9e20
```

目标：

- structural refactor only；
- 保留规则、recommendation ordering、persistence、history identity 和 Compose state ownership；
- 让 `ClocktowerJudgeScreen` 从 monolith 收敛为清晰的 coordinator / orchestrator；
- 让已经进入 production 的 R5.5 planner / interaction authority 真正成为夜间步骤 materialization 的上游，而不是仅在 UI steps 全部预构造后做排序/一致性校验；
- handwritten production source 约 50 KiB 作为 **soft maintainability guideline**，不是硬 merge gate；
- 不为了满足 byte threshold 引入弱抽象、巨型参数袋、额外 `internal` 泄漏、Compose state/effect lifetime 移动或 transaction-order 风险；
- 每个 slice 使用 characterization/ownership contract、local/remote validation、exact diff、GitHub CI/R2；
- 未完成高价值 decomposition 和最终架构审计前不 merge。

### 3.2 A1–A10 已完成

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

A9  ClocktowerHostScreen.kt dead-code cleanup
    unreachable legacy LazyColumn + ClocktowerInfoCard removed

A10 ClocktowerInformationStepBuilder.kt
    generic stateless information-step packaging seam
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

A10 GREEN 后：

```text
ClocktowerHostScreen.kt              287,597 bytes
ClocktowerInformationStepBuilder.kt    9,095 bytes
```

A9 删除了 unconditional `return` 后可证明不可达的 legacy `LazyColumn`、其中 6 个 `ClocktowerInfoCard` 调用点及尾部私有 helper，共 25,068 bytes / 484 lines。A10 又将 generic `infoStep` implementation 精确移出 Host，并机械替换 21 个调用点。

当前 host 仍几乎完全由单个大型 `ClocktowerJudgeScreen` 构成。后续拆分必须依据真实 ownership、R5.5 flow authority 和 future-change locality，而不是按固定字节块机械切割。

### 3.4 2026-08-23 架构决策：50 KiB 从 hard gate 改为 soft guideline

此前计划把“handwritten production source <= 50 KiB”作为硬终点。A9 后重新审计发现：剩余代码包含大量 Compose state/effect lifetime、registration mutable state、selection audit、history/information lifecycle 和 transaction ordering；若仅为文件尺寸继续机械外移，会导致更差的跨文件耦合。

因此正式调整：

```text
优先级
1. cohesive ownership
2. product/semantic correctness
3. Compose state/effect lifetime stability
4. transaction/callback ordering
5. future feature isolation
6. file-size guideline
```

约 50 KiB 仍然是新文件的优先目标和 code-review warning，但不再是 PR #43 merge 的绝对条件。

若后续高价值拆分完成后 `ClocktowerJudgeScreen` 已经主要承担 coordinator/orchestration 职责，即使仍约 100–150 KiB，也可在最终审计后认定 PR #43 达到架构目标。不得仅为达到数字目标继续 architecture-negative extraction。

### 3.5 动态多剧本夜间流程的当前真实状态

R5.5 已经完成并进入 production 的部分：

```text
script/catalog/ruleset normalization
        ↓
ClocktowerFlowPlanner
        ↓
ClocktowerHostInteractionProjector
        ↓
stable interaction IDs + conditional interactions
        ↓
ClocktowerProductionFirstNightFlow / OtherNightFlow
        ↓
production order authority
```

这意味着 **夜序、剧本角色集合和条件 interaction authority 已经动态化**。Trouble Brewing 与 No Greater Joy 共用同一 ruleset/catalog/planner seam；Mayor / Ravenkeeper / Sage / Undertaker / Demon succession 等条件 interaction 由 resolved facts / registry 决定。

尚未完成的是最后一层 production materialization。目前 Host 仍然：

```text
先 hardcode 构造所有当前 ClocktowerNightStepUi
        ↓
unfilteredNightSteps
        ↓
filter
        ↓
planner projection exact-match / reorder
```

目标架构应逐步反转为：

```text
ruleset + current state
        ↓
planner/projector 先产生 HostInteraction plan
        ↓
materializer registry 按 stable interaction identity 懒生成 UI step
        ↓
ClocktowerNightStepUi
```

因此 PR #43 后续不再把当前 hardcoded list 原样搬进 `FirstNightStepFactory` / `OtherNightStepFactory`；那样只会把旧架构封装到新文件中。

## 4. A9/A10 证据与剩余拆分计划

### A9 evidence

```text
RED commit:              3ecbcadbd728ac83f7ab1f8d1d40175795e44078
CI #505:                 EXPECTED FAILURE
unit tests:              657 total / 2 expected ownership failures
assembleDebug:           SUCCESS
ASP contract tests:      SUCCESS
Real Clingo:             SUCCESS
R2 #445:                 SUCCESS

GREEN commit:            00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
exact deletion audit:    PASS
active prefix audit:     byte-for-byte identical through unconditional return
Host reduction:          25,068 bytes / 484 lines
CI #506:                 SUCCESS
R2 #446:                 SUCCESS
```

### A10 — Information Step Builder seam — COMPLETE

A10 最终采用窄边界：只移动 generic `infoStep` packaging，不移动 recommendation calculation、registration mutable state、dynamic decision、Compose state/effect 或 transaction authority。

```text
RED commit:                         3377fdbea83727a797afce28064b924a074df5c3
CI #513:                            EXPECTED FAILURE
Android tests:                      658 total / 1 expected ownership failure
ASP contract tests:                 SUCCESS
Real Clingo:                        SUCCESS
R2 #453:                            SUCCESS

GREEN commit:                       363629ed45f0f044da021f77bb52c5c3ff3c9e20
new owner:                          ClocktowerInformationStepBuilder.kt
new owner size:                     9,095 bytes
Host after GREEN:                   287,597 bytes
mechanical call-site replacements:  21
exact move / boundary audit:        PASS
CI #514:                            SUCCESS
R2 #454:                            SUCCESS
```

### A11 — Night Step Materialization seam / registry

A11 先建立 planner-first materialization 的稳定桥，不立即改写大型 Host role list。

目标：

- `ClocktowerProductionFirstNightFlow` / `ClocktowerProductionOtherNightFlow` 暴露 canonical projected `ClocktowerHostInteraction` list；
- 新增无 Compose state 的 `ClocktowerNightStepMaterializerRegistry`；
- registry 以 stable interaction identity 注册 lazy step materializer；
- materialization 顺序完全来自 projected interaction list；
- `SYSTEM_BOUNDARY` 不生成现有 production `ClocktowerNightStepUi`；
- 仅 materialize planner 实际要求的 interaction；未选中的 materializer 不应执行；
- projected actionable interaction 缺失 materializer 时 fail closed；
- 不修改 Host 当前 production behavior，现有 `.order(...)` path 继续工作并复用同一 interaction projection seam。

A11 不移动：

- role-specific Host construction；
- recommendation / registration calculations；
- Compose state/effects；
- `advanceNightStep`；
- any product behavior。

### A12 — planner-driven First Night materialization

A12 再把 first-night production 从：

```text
prebuild all first-night UI steps -> order
```

切为：

```text
project first-night interactions -> lazy materialize requested steps
```

范围包括 evil information、Poisoner、Fortune Teller red herring、Clockmaker、Washerwoman、Librarian、Investigator、Chef、Empath、Chambermaid、Fortune Teller、Butler、Spy 等当前已支持 interaction。

必须保持：

- planner/projector 是唯一 ordering authority；
- current role/recommendation/registration semantics 不变；
- Compose state/effect lifetime 不移动；
- first-night information migration lifecycle 不移动；
- no script-specific UI ordering branch。

### A13 — planner-driven Other Night materialization

A13 对 other-night 做同样 cutover，包括当前已支持的 role/event interactions。

可覆盖 Poisoner / Butler / Empath / Chambermaid / Fortune Teller / Undertaker / Monk / Imp / new Demon identity / Demon succession / Mayor redirect / Sage / Ravenkeeper / Spy 等。

**明确不随 A13 搬走 `advanceNightStep` transaction。** 当前 confirm → audit → registration recording → event recording → index/finalization 的顺序必须继续由 host 控制，除非将来有独立架构决策和针对性 characterization。

### A14 — optional clean day routing consolidation

A13 后先做架构/规模复审。只有仍有明显高价值且低耦合的 day routing owner 时才进入 A14。

优先候选：

```text
Overview
Vote
EndConfirm
```

Nomination/Virgin、Slayer、Artist、Klutz 仅在参数/owner 边界仍然清晰时继续；如果需要暴露大量 registration/recommendation/state internals，则停止，不为了 size 强行拆。

### Post-A13/A14 final decomposition audit

重新测量 host 并审核 responsibility cohesion：

- 若剩余内容主要是 Compose state/effect ownership、setup lifecycle、registration state、phase orchestration、transaction ordering，则允许结束 PR #43；
- 不预先承诺 host 必须 <= 50 KiB；
- A14 是 optional，不是机械完成条件；
- 若仍存在明显独立且高价值 owner，再单独规划额外 slice。

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

### Host / flow / materialization boundary

当前 decomposition 的理想终态：

```text
ClocktowerJudgeScreen
  -> Compose state/effect lifetime
  -> current snapshot / derived orchestration state
  -> setup recommendation lifecycle
  -> registration mutable state
  -> phase routing
  -> commit / callback ordering

ClocktowerFlowPlanner + InteractionProjector
  -> what interactions exist
  -> canonical order
  -> conditional/event interactions

ClocktowerNightStepMaterializerRegistry
  -> stable interaction identity -> lazy step materializer
  -> no Compose state/effect ownership

ClocktowerInformationStepBuilder
  -> generic reliable/unreliable information-step packaging

First/Other-night materializer owners
  -> role/event-specific ClocktowerNightStepUi construction
  -> invoked only for projected interactions

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

## 7. 开发工作方式与 CI 策略

项目级长期工作方式现在由根目录 `AGENTS.md` 明确规定。

### 决策责任

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

除非用户明确改变分工，Codex/Luna 不负责自行选择 decomposition boundary 或重新设计架构。

### 写入路径优先级

```text
1. GitHub connector 能安全完整读写
   -> Chat 直接修改
   -> exact remote diff
   -> GitHub checks / CI

2. 大文件、truncation、declaration extraction、多处机械修改或本地重型验证更安全
   -> Chat 给出精确机械任务
   -> 用户转给 Codex/Luna
   -> Luna local tests + commit/push
   -> Chat 重新读取 GitHub 做最终远端审计
```

Behavior change：

```text
live state recheck
-> Chat decision
-> tests-only RED
-> real RED provenance when required
-> smallest GREEN
-> focused/full tests + APK
-> ASP + Real Clingo + R2
-> exact diff + final review
-> explicit merge authorization
```

Pure structural refactor：

```text
live state recheck
-> Chat boundary decision
-> characterization/ownership contract
-> connector direct implementation when safe
   OR Luna mechanical implementation when large-file work is safer locally
-> focused/full validation
-> exact remote diff
-> GitHub CI/R2
-> Chat boundary re-audit before next slice
```

规范：

- `AGENTS.md`
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
PR #43 A9 RED                             3ecbcadbd728ac83f7ab1f8d1d40175795e44078
PR #43 A9 GREEN                           00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
PR #43 A10 RED                            3377fdbea83727a797afce28064b924a074df5c3
PR #43 A10 GREEN / validated implementation 363629ed45f0f044da021f77bb52c5c3ff3c9e20
```

## 9. 新会话启动顺序

1. 读根目录 `AGENTS.md`；
2. 读 `docs/README.md`；
3. 读 `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
4. 读 `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`；
5. 读本文件；
6. 读 `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-23.md`；
7. 查询 live `main`、PR #43、feature head 和 checks；
8. 若 PR #43 仍在开发，从其 live head 继续，不从 main 另开重复 branch；
9. 当前从 A11 Night Step Materialization seam / registry 开始，由 Chat 决策边界；
10. 未经用户明确授权不得 merge。

## 10. 文档维护规则

- 本文件是当前执行点唯一权威；
- `AGENTS.md` 是长期 AI 协作与执行分工的项目级规范；
- handoff 服务下一次开发；
- specialized design 维护语义边界，不维护 live branch 状态；
- historical audit 不得覆盖本文件；
- 每完成一个 decomposition slice，至少更新当前 head、gate、剩余规模和下一规划边界；
- 如果新的用户决策改变了长期工作方式或 decomposition completion criterion，应同步更新 `AGENTS.md` 和本 roadmap，而不是只留在聊天记录中。
