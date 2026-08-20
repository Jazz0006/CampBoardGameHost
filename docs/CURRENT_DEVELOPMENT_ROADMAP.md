# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-20  
> 当前分支：`codex/storyteller-algorithm-v4`  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> Phase A 退出评审：`phase_a_exit_review_2026-08-20.md`

## 1. 当前结论

2026-08-19 对 A0–A4.5 的重新审计发现：A3 存在真实规则语义缺口，A4.5 也存在 lifecycle / persistence correctness 缺口，因此原先“可以直接进入 Phase B”的前提失效。

随后按以下顺序完成 remediation：

```text
R1  A3 correctness hotfix
  ↓
R2  MainActivity mechanical decomposition
  ↓
R3  A2/A3 validation contract hardening
  ↓
R4  A4.5 lifecycle / durability / cache hardening
  ↓
R5  A3/A4/A4.5 correctness re-exit
  ↓
R5.5 Script & Dynamic Flow Foundation
  ↓
ONLY THEN R6 revision-driven dynamic decision engine
```

**当前执行点（2026-08-20）：R1、R2、R3、R4、R5 已通过；Phase A correctness exit 已签署。现在正式进入 R5.5 — Script & Dynamic Flow Foundation，第一批是 S0 Schema / Catalog / official-custom JSON normalization / validation。**

R5 通过只解锁 R5.5，**没有**直接解锁 R6。

A4 ZDD 也没有因为 Phase A 退出而获得 production 授权：现阶段继续保持 exact shadow/prototype；`ZDD_DEVICE_VALIDATED` 仍未授权，目标设备 latency / memory / degradation gate 是未来 runtime promotion 的独立门槛。

## 2. 阶段状态

| 阶段 | 当前状态 | 说明 |
|---|---|---|
| A0 外部参考冻结 | PASS | 冻结参考继续有效。 |
| A1 Unified Semantic Model | PASS | storyteller truth / observation / player knowledge 三层边界保留。 |
| A1.1 Semantic Hardening | PASS WITH FOLLOW-UP | schema-v2、registration interaction binding、world-set identity 成立；B4 前仍需完成 timeline identity 等 P1 语义债务。 |
| A2 ASP Oracle harness | PASS / R3.1 | nested `FormalGameState` 已迁移 schema-v2；Python 与 Android typed decoder 共用 fail-closed 合同。 |
| A2.1 Golden corpus | PASS / R3.1 | 52 total；24 Clingo executable；18 AGREE / 1 EXPECTED_COVERAGE_GAP / 5 KNOWN_ORACLE_VARIANCE / 28 ORACLE_NOT_APPLICABLE；`UNEXPLAINED_MISMATCH=0`、`NOT_RUN=0`。 |
| **A3 EnumeratedWorldSet** | **PASS / R5 RE-EXIT VALIDATED** | R1 official-rules hotfix、R3 typed real-enumerator golden path、R5 Enumerated↔ZDD differential 与 full regression 均通过。A3 继续作为 exact correctness baseline。 |
| **MainActivity decomposition** | **PASS / R2 BATCHES 1–10 VALIDATED** | Activity shell、三游戏边界、Clocktower setup/day/night/host/history 已机械拆分；read-only structural verifier 继续守护边界。 |
| **A4 ZDD prototype** | **PASS AS EXACT SHADOW / NOT DEVICE-VALIDATED** | correctness differential 通过；production selector 不读取 shadow；现有设备性能证据仍不足以授权 `ZDD_DEVICE_VALIDATED`。 |
| **A4.5 observation cache rebuild** | **PASS / R4 VALIDATED** | durable-before-build、cancellation、generation identity、revision/session invalidation、OOM/failure semantics 与 production-shadow isolation 均通过。 |
| **R5 Phase A re-exit** | **PASS** | 新 exit review 已签署；clean-head Android/ASP/Clingo/structural CI 通过。 |
| **R5.5 Script & Dynamic Flow Foundation** | **ACTIVE / S0** | 当前唯一实施方向。规范见 `多剧本多板子与动态游戏流程架构设计_v1.md`。 |
| R6 revision-driven production expansion | BLOCKED | R5.5 全部通过后才可从 BLOCKED 切到 READY。 |

## 3. Phase A remediation 结果

### R1 — A3 registration correctness — PASS

修复 poisoned Spy/Recluse 在 Chef/Empath 数字信息中的错误 special-registration：

- Spy/Recluse 的特殊阵营登记只有在角色能力 functioning 时才可进入；
- 中毒不改变真实 alignment，只关闭能力提供的 optional registration；
- Chef/Empath × poisoned Spy/Recluse 四组合有直接回归；
- catalog 增加 `TB-MAL-05`–`08`；
- 冻结 `pnkfelix/botc-asp` 的对应差异继续分类为 `KNOWN_ORACLE_VARIANCE`，不覆盖官方规则。

R5 已重新签署 A3/A4 exact differential，因此 R1 的整体退出条件现在全部满足。

### R2 — MainActivity mechanical decomposition — PASS

第一轮结构拆分保持 behavior-preserving，不改变规则、revision、persistence、recommendation/cache/hash 或 rollout 语义。

已形成主要边界：

```text
MainActivity.kt                 Activity/window/setContent shell
CampBoardGameHostApp.kt         app root / state owner
undercover/                     Undercover UI/support
werewolf/                       Werewolf host UI/support
clocktower/ui/
    ClocktowerSetupScreen.kt
    ClocktowerDayScreen.kt
    ClocktowerNightScreen.kt
    ClocktowerHostScreen.kt
    ClocktowerHistoryScreen.kt
```

R2 batches 1–10 均通过 Android / ASP / real Clingo 验证，最终 `R2 main-thread boundary` read-only workflow 持续守护主要结构合同。

ViewModel / SessionController / immutable state ownership 属于以后独立架构工作，不回填 R2。

### R3 — A2/A3 validation contract — PASS

R3.1：

- nested `FormalGameState.schemaVersion` 从 v1 迁移 v2；
- schema-v1 fail closed；
- Android catalog 实际经过 `EpistemicSemanticJson.decodeFormalGameState()`；
- canonical hash 与 real Clingo baseline 重新签署。

R3.2：

- 保留 24 条 evaluator-level executable contracts；
- `A3EndToEndGoldenContractTest.kt` 真实调用 `TroubleBrewingWorldEnumerator.enumerate()`；
- 代表性覆盖 hidden Baron / Drunk、Poisoner target、Fortune Teller red herring、Spy/Recluse registration-sensitive information。

关键 source：

- R3.1 `d683fe383b6c56d74b3206b6c325d6ba3b31bed2`
- R3.2 `58574f6dcc094737e5d202ce5facfefc8a6b357b`

### R4 — A4.5 lifecycle hardening — PASS

#### R4.1 cache invariant + telemetry

- `commitIfCurrent()` 核对 `gameId / gameStateRevision / formalSnapshotId / rollout / current generation`；
- scope mismatch fail closed；
- observation rebuild telemetry 使用真实含义的 `coarseEndHeapDeltaBytes`。

Source：`46f17b7b99eec5a2178560429eb216655505d3b5`

#### R4.2 durable-before-build + coroutine cancellation

- 新增 `A4ObservationDurabilityGate`；
- persistence success 后才释放 observation rebuild request；
- persistence failure 保留 pending，不把未 durable observation 用作 rebuild basis；
- coroutine cancellation 进入 executor；
- exact build 返回后、cache publication 前再次检查 cancellation。

Source：`7dc1a8a5afd69ed1b0f87406c71d84adbdf602cb`

#### R4.3 revision/session lifecycle invalidation

- 新增 `A4ShadowLifecycleInvalidator`；
- same-session revision supersede 同步 invalidate 当前 game generation + cancel rebuild；
- same-session revision 不清 durability pending；
- reset/archive/restore 同步 invalidate cache、clear pending、cancel rebuild；
- 原 revision bump 位置保持不变，只收口到 helper。

Source：`2981b86374284cd2967037942c14011d01700c23`

#### R4 final acceptance

新增 `A4ShadowProductionIsolationTest.kt`，直接证明：

- shadow cache miss → ready 不改变 production setup recommendation；
- production demand probe 只暴露 readiness/revision/seat metadata；
- shadow `PlayerWorldSet` 不进入 production recommendation。

Test-only commits：

- `6ee3815489bdf23abd28188e21c7e2a43677be17`
- `8bfb9e6cc2f95987c22ff8f133a84c918c02ca10`

CI #116：Android + debug APK、ASP、real Clingo success；R2 boundary #108 success。

## 4. R5 — Phase A re-exit — PASS

Phase A exit review：`docs/phase_a_exit_review_2026-08-20.md`

### 4.1 A3 ↔ A4 exact differential

`ZddPlayerWorldSetTest` 当前直接比较 Enumerated 与 ZDD：

- identity；
- exact cardinality；
- empty/non-empty；
- possible Demon/Minion seats；
- possible roles；
- per-role/per-Demon world counts；
- explanation clusters；
- require/exclude；
- checkpoint/restore；
- possible-value discovery；
- native 与 decode/rebuild filtering。

同一测试还把**全部当前 A3 executable golden contracts**同时跑过 Enumerated 与 ZDD，并要求结果一致。

结论：**无新的 A3/A4 exact differential mismatch。**

### 4.2 Failure / OOM / cancellation / stale semantics

直接 executor/coordinator tests 已证明：

- wrong identity/scope → FAILED / no write；
- stale generation → STALE / no current commit；
- cancellation → CANCELLED；
- ordinary exception → FAILED；
- OOM → resource failure / queue stop；
- 上述状态都没有伪造 `cardinality=0` 或 UNSAT。

### 4.3 Multi-night boundary

`B4DynamicPlayerWorldSetShadowTest` 对未建模 role-change transition 明确返回 `DEFERRED_B4` 且不执行 world query；hidden poison replacement 也不会泄露或形成 pseudo-UNSAT。

因此当前多夜未覆盖边界是**明确 defer**，不是 false logical impossibility。

### 4.4 Clean-head regression

Reviewed head：`ec93dfed61ef71af1869387b24907a85069f51c6`

- CI #117 — success
  - Android unit tests + debug APK — success
  - ASP contract tests — success
  - real Clingo cross-validation — success
- R2 main-thread boundary #109 — success

### 4.5 Device gate decision

**不授权 ZDD runtime promotion。**

现有手机诊断已经说明：native restrictions 可以很快，但 ZDD construction 与 numeric decode/rebuild fallback 仍可能超过 provisional 50 ms ceiling。目标设备 gate 因此继续独立存在。

Phase A correctness exit 不要求在 shadow-only 状态下强行通过 device gate；只有未来要启用 `ZDD_DEVICE_VALIDATED` 时，才必须重新执行并通过目标设备 latency / memory / degradation 验收。

R5 结论：**PASS**。

## 5. P1 — R6 / 正式多夜 Possible Worlds 前必须解决

这些语义债务不重新打开 Phase A，但必须在真正 production 化多夜 player-world reasoning 前处理。

### P1.1 Spy Grimoire reminder tokens

`GrimoireState` 已包含 reminder tokens；正式使用 Spy perspective 前要确定哪些 token 属于 mechanical truth，并与 filtering/schema 承诺一致。

### P1.2 Observation timeline identity

当前部分 canonical order 仍依赖：

```text
round -> sequence -> id
```

多 phase/multi-night 前需要统一 TimelinePoint / global monotonic sequence，并明确哪些时间字段进入 knowledge identity/hash。

### P1.3 Actual truth vs knowledge-safe world-builder input

必须进一步明确：

```text
Actual FormalGameState
vs
Player-world construction input
```

长期不能用把真实 secret 字段传 `null` 来同时表达两种语义。

## 6. R5.5 — Script & Dynamic Flow Foundation

**当前状态：ACTIVE — S0。**

目标是在不重写 Possible Worlds 的前提下，让内容身份、角色注册和游戏流程不再硬编码为 Trouble Brewing enum / fixed Werewolf JudgeStep，为 R6 提供稳定 script-aware decision seam。

实施顺序严格保持：

```text
S0 Schema / Catalog / official-custom JSON normalization / validation
S1 Trouble Brewing FlowPlanner golden-equivalent migration
S2 No Greater Joy second-script structural proof
S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration
S4 persistence/ruleset identity migration
S5 full regression + legacy flow removal + R6 handoff
```

### S0 当前目标

先只建立**内容描述与身份合同**，不改现有游戏流程：

1. 审计当前 `ClocktowerScript` / role definition / ruleset identity 的硬编码边界；
2. 定义 BotC Script Catalog 的最小 schema；
3. 支持 official/custom JSON normalization 与 fail-closed validation；
4. 建立稳定 script ID / content hash / rules coverage identity；
5. 建立 role registry lookup seam，但复杂角色规则继续留在 Kotlin handler/domain；
6. 加入 Trouble Brewing canonical catalog fixture；
7. 只做 schema/catalog tests，不把 FlowPlanner 或 UI migration 偷跑进 S0。

### S0 禁止事项

- 不构建通用规则 JSON DSL；
- 不把复杂 ability behavior 搬进 JSON；
- 不改现有 Clocktower night/day ordering；
- 不移除 legacy Trouble Brewing flow；
- 不改 recommendation behavior；
- 不开始 No Greater Joy 的 gameplay migration；
- 不开始 Werewolf FlowPlanner；
- 不改 production A3/A4 rollout。

### R5.5 全局约束

- Script/Board 只组合角色，复杂规则留 Kotlin；
- ClocktowerFlowPlanner 与 WerewolfFlowPlanner 分离；
- `VERIFIED / PARTIAL / UNVERIFIED` 决定 custom/homebrew 自动化安全等级；
- TB legacy flow 只有 shadow/golden parity 后才能移除；
- 新增只由已有角色构成的 script/board 不应要求修改 Host UI 或 flow core；
- R6 decision point 必须来自 script-aware `FlowPlanner -> HostInteraction / StorytellerDecisionPoint` seam。

## 7. R6 — revision-driven dynamic decision engine

**BLOCKED BY R5.5。**

只有 S0–S5 通过后，`storyteller_revision_driven_dynamic_decision_engine_plan.md` 才能从 BLOCKED 改为 READY。

R6 不得重新引入：

- Trouble Brewing enum/role-name `when` 作为流程事实；
- Compose UI 决定“下一个角色是谁”；
- `nightOrderPosition` 作为唯一流程定义；
- Werewolf fixed `JudgeStep` 扩展模式。

## 8. 生产保护线

在后续路线明确修改前：

```text
Production recommendation engine: existing production path
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
A4.5 cache: debug/shadow only
B4 DynamicPlayerWorldSetShadow: isolated shadow only
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
R5.5 S0: schema/catalog foundation only
R6: BLOCKED
```

任何后续优化或结构重构都不能：

- 截断 exact worlds 后仍声称 exact；
- 把 timeout/OOM/cap 当 UNSAT；
- 省略 Spy/Recluse/Drunk/Poisoner/red-herring 规则分支；
- 把 storyteller-only truth 放入普通玩家知识；
- 让 background result 覆盖已展示/提交决定；
- 以“多剧本准备”为名提前改写现有 flow/recommendation；
- 以 JSON 内容化为名把复杂规则变成未经验证的通用规则 DSL。

## 9. 开发操作策略

默认：

```text
read-only audit
→ Git Data API / Contents API 的最小原子 source commit
→ normal PR CI
→ exact diff audit
```

只有超大文件、connector 无法安全构造完整内容时，才使用 temporary trusted writer；结束后必须清理 writer/trigger/temporary base，并恢复 normal read-only workflow。

详细规则：`docs/github_connector_large_file_editing_playbook.md`。

PR #2 在 R5.5 期间继续保持：

- open；
- Draft；
- base=`main`；
- **do not merge**。

## 10. 文档状态维护

后续只在本文更新“当前执行点”。专项设计/退出证据可维护独立 spec/review，但不得再创建并列的“当前路线”。
