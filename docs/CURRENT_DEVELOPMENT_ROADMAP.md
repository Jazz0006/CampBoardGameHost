# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-20  
> 当前分支：`codex/storyteller-algorithm-v4`  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> 多剧本架构规范：`多剧本多板子与动态游戏流程架构设计_v1.md`  
> 当前 R5.5 交接：`r5_5_multiscript_progress_handoff_2026-08-20.md`  
> Phase A 退出评审：`phase_a_exit_review_2026-08-20.md`

## 1. 当前结论

Phase A remediation 已按以下顺序完成：

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

R1–R5 已全部通过，Phase A correctness exit 已签署。

**当前执行点（2026-08-20）：R5.5 / S1 ACTIVE。S0 已 PASS；S1.1 pure shadow base-night planner 已 PASS；S1.2 stable HostInteraction projection 已 PASS；下个 source batch 从 S1.3 resolved flow facts / conditional-event interaction 开始。**

最新已验证 source baseline：

```text
5a628675e3f047d919908fbd7f14eaa31b788ac6
feat(r5.5): project shadow host interactions
```

该 source head 的 normal CI #124 已通过 Android unit/build + debug APK、ASP、real Clingo；R2 structural verifier #116 也通过。

之后只有 docs-only 交接更新；S1.3 **尚无 source commit**。下个会话不得假设任何未提交本地/临时 patch 存在。

R5 只解锁 R5.5，**没有**直接解锁 R6。

A4 ZDD 也没有因为 Phase A 退出而获得 production 授权：现阶段继续保持 exact shadow/prototype；`ZDD_DEVICE_VALIDATED` 仍未授权，目标设备 latency / memory / degradation gate 是未来 runtime promotion 的独立门槛。

## 2. 阶段状态

| 阶段 | 当前状态 | 说明 |
|---|---|---|
| A0 外部参考冻结 | PASS | 冻结参考继续有效。 |
| A1 Unified Semantic Model | PASS | storyteller truth / observation / player knowledge 三层边界保留。 |
| A1.1 Semantic Hardening | PASS WITH FOLLOW-UP | schema-v2、registration interaction binding、world-set identity 成立；B4 前仍需完成 timeline identity 等 P1 语义债务。 |
| A2 ASP Oracle harness | PASS / R3.1 | nested `FormalGameState` 已迁移 schema-v2；Python 与 Android typed decoder 共用 fail-closed 合同。 |
| A2.1 Golden corpus | PASS / R3.1 | 52 total；24 Clingo executable；18 AGREE / 1 EXPECTED_COVERAGE_GAP / 5 KNOWN_ORACLE_VARIANCE / 28 ORACLE_NOT_APPLICABLE；`UNEXPLAINED_MISMATCH=0`、`NOT_RUN=0`。 |
| **A3 EnumeratedWorldSet** | **PASS / R5 RE-EXIT VALIDATED** | exact correctness baseline；R1/R3/R5 验证通过。 |
| **MainActivity decomposition** | **PASS / R2 BATCHES 1–10 VALIDATED** | Activity shell、三游戏边界、Clocktower setup/day/night/host/history 已机械拆分；read-only structural verifier 持续守护。 |
| **A4 ZDD prototype** | **PASS AS EXACT SHADOW / NOT DEVICE-VALIDATED** | correctness differential 通过；production selector 不读取 shadow。 |
| **A4.5 observation cache rebuild** | **PASS / R4 VALIDATED** | durable-before-build、cancellation、generation identity、revision/session invalidation、OOM/failure semantics、production-shadow isolation 均通过。 |
| **R5 Phase A re-exit** | **PASS** | exit review 已签署；clean-head Android/ASP/Clingo/structural CI 通过。 |
| **R5.5 S0 Schema/Catalog** | **PASS** | official/custom JSON normalization、typed validation、canonical TB asset 已通过。 |
| **R5.5 S1 TB FlowPlanner migration** | **ACTIVE / S1.1+S1.2 PASS / S1.3 NEXT** | 新 planner 仍 shadow-only；production Host 尚未切换。 |
| R5.5 S2 NGJ second-script proof | READY AFTER S1 | 调整为直接使用现有真实 No Greater Joy，而不是 toy fixture。 |
| R5.5 S3–S5 | BLOCKED BY PRIOR BATCHES | Werewolf registry/planner、persistence identity、legacy removal/R6 handoff。 |
| R6 revision-driven production expansion | BLOCKED | R5.5 全部通过后才可切 READY。 |

## 3. Phase A 已完成结果

详细退出证据见：

- `phase_a_exit_review_2026-08-20.md`
- `storyteller_a4_5_observation_cache_rebuild_spec.md`
- `storyteller_a4_zdd_prototype.md`

关键结论保持：

- A3 exact enumerator 是 correctness baseline；
- A4 ZDD 与 A3 exact differential 已通过，但仍是 shadow/prototype；
- stale/cancel/failure/OOM 不会被解释为 UNSAT；
- multi-night 未建模 transition 明确 `DEFERRED_B4`；
- shadow cache readiness 不影响 production recommendation；
- `ZDD_DEVICE_VALIDATED` 未授权。

关键 R4 source：

```text
R4.1 46f17b7b99eec5a2178560429eb216655505d3b5
R4.2 7dc1a8a5afd69ed1b0f87406c71d84adbdf602cb
R4.3 2981b86374284cd2967037942c14011d01700c23
```

## 4. P1 — R6 / 正式多夜 Possible Worlds 前必须解决

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

## 5. R5.5 — Script & Dynamic Flow Foundation

目标是在不重写 Possible Worlds 的前提下，让内容身份、角色注册和游戏流程不再硬编码为 Trouble Brewing enum / fixed Werewolf JudgeStep，为 R6 提供稳定 script-aware decision seam。

总体顺序保持：

```text
S0 Schema / Catalog / official-custom JSON normalization / validation      PASS
S1 Trouble Brewing FlowPlanner golden-equivalent migration                 ACTIVE
S2 No Greater Joy real second-script structural proof                      NEXT AFTER S1
S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration
S4 persistence/ruleset identity migration
S5 full regression + legacy flow removal + R6 handoff
```

### 5.1 S0 — PASS

S0 建立的多剧本基础已经足够，不再新增另一套 framework：

```text
ClocktowerCharacterDefinition
ClocktowerCharacterRegistry
ClocktowerScriptDefinition
ClocktowerScriptCatalog
ValidatedClocktowerRuleset
NightOrderToken
```

#### S0.1 — catalog core

Source：`beb23512b4133712025e534152d268fd9315a5bf`

- strong typed catalog；
- existing `RulesetJsonLoader.parseScript(...)` adapter；
- deterministic normalized hash；
- TB composition/night-order parity；
- fail-closed validation；
- homebrew/bootlegger safe downgrade。

CI #120 success；R2 #112 success。

#### S0.2 — official schema compatibility

Source：`b02153f821f2403aa39d733d3fe30ace3e6abebd`

- typed validation codes；
- current custom-script schema constraints；
- `remindersGlobal / jinxes / special` metadata；
- decimal night priority；
- custom-character unknown-field rejection；
- metadata 继续不被解释为通用规则 DSL。

CI #121 success；R2 #113 success。

#### S0.3 — canonical TB asset

Source：`df0509da...`

新增：

```text
app/src/main/assets/scripts/trouble_brewing.json
```

作为 S1 的稳定 canonical input。

CI #122 success；R2 #114 success。

### 5.2 S1.1 — pure shadow base-night planner — PASS

Source：`e76614ffc4382e247cf37d6c994172642b922a8c`

`ClocktowerFlowPlanner` 当前输入：

```text
ValidatedClocktowerRuleset
+ playerCount
+ inPlayRoleIds
        ↓
base night token plan
```

已验证：

- explicit script override 优先；
- 无 override 时按 character priority 派生；
- system tokens；
- 5–6 / 7+ 首夜 evil-info eligibility；
- in-play filtering；
- off-script fail closed；
- TB first/other-night canonical parity。

CI #123 success；R2 #115 success。

### 5.3 S1.2 — stable HostInteraction projection — PASS

Source：`5a628675e3f047d919908fbd7f14eaa31b788ac6`

建立：

- `ClocktowerHostInteraction` stable flow model；
- character interaction registry；
- pure projector；
- Fortune Teller red herring 由 FT handler 产生 storyteller-setup interaction；
- FlowPlanner 不 hardcode `TroubleBrewing`；
- `DynamicDecisionRequest` 不被当成通用流程节点。

TB golden 已锁定关键顺序：

```text
Empath
→ Fortune Teller red-herring setup
→ Fortune Teller action
```

CI #124 success；R2 #116 success。

**Production Host 仍完全使用 legacy flow。**

### 5.4 S1.3 — NEXT：resolved flow facts + conditional/event interaction

下个会话从这里开始。

legacy 审计已确认，以下 official night-order token 不能简单解释为“角色在场就执行”：

- Undertaker：需要当天实际处决；
- Ravenkeeper：需要实际夜死且死亡角色匹配；
- Mayor：需要 Demon attack / death resolution 已满足 redirect 前提；
- Scarlet Woman：主要作为 succession/order anchor，不应直接成为普通 wake step；
- Demon successor：Imp self-kill 等条件结算后临时产生。

S1.3 应建立/完善：

```text
ClocktowerResolvedFlowFacts
handler eligibility
before-role / after-role conditional interactions
pending-event interactions
```

要求：

1. FlowPlanner 消费规则层已经解析的 facts，不重复计算 death/protection/poison correctness；
2. 无 fact 不误生成；
3. 角色不在场不凭空生成；
4. 条件 interaction 顺序与 legacy TB 一致；
5. tests first / contract first；
6. 继续 shadow-only，不接 Compose Host。

### 5.5 S1.4 — planned：legacy ↔ planner shadow differential

S1.3 通过后建立真实 differential：

- first night；
- other night；
- day transition；
- conditional/event-triggered interactions；
- committed action/observation order and stable identity。

只有 shadow/golden parity 通过后，才允许考虑关闭 `officialNightOrder()` / fixed legacy source。

## 6. 多剧本实施调整：S2 直接使用真实 No Greater Joy

重新审计确认：当前 App 已经存在真实双剧本产品行为，而不是只有未来占位。

现有 legacy 已支持：

- 5–6 人 setup UI 选择 Trouble Brewing / No Greater Joy；
- 5–6 人默认 No Greater Joy；
- `generateClocktowerAssignments(playerCount, script)` 按 script 选择角色池；
- NGJ 已包含实际 day/night/trigger gameplay。

No Greater Joy 当前角色池：

```text
Clockmaker
Investigator
Empath
Chambermaid
Artist
Sage
Drunk
Klutz
Baron
Scarlet Woman
Imp
```

因此明确调整：

> **不再新增一层“多剧本框架”；S2 直接把现有 No Greater Joy 接入 S0/S1 已建立的 catalog/registry/FlowPlanner，并把它作为第二个真实 structural proof。**

S2 目标：

1. canonical `no_greater_joy.json` script asset；
2. NGJ-only character metadata / behavior bindings 进入 existing central CharacterRegistry；
3. same parser / normalizer / ScriptCatalog；
4. same ClocktowerFlowPlanner；
5. `Clockmaker / Chambermaid / Artist / Sage / Klutz` 等通过 metadata + handler 接入；
6. TB + NGJ dual-script structural regression；
7. 不新增 script-specific Host UI 主流程分支。

S2 强判据：

```text
新增 NGJ
= script asset + character metadata/handler + tests
≠ FlowPlanner core 中新增 NoGreaterJoy when/if
```

如果接入 NGJ 必须修改 FlowPlanner 主干以识别剧本名字，则先修架构 seam，不继续堆分支。

## 7. 明确继续延后的范围

### 7.1 A3/A4 Possible Worlds

不要因为 NGJ 接入顺手泛化 A3/A4。

允许：

```text
TB:  Catalog ✅ / FlowPlanner ✅ / Possible Worlds exact+shadow baseline
NGJ: Catalog ✅ / FlowPlanner ✅ / Possible Worlds deferred
```

### 7.2 Persistence / Ruleset identity

当前 save 已经同时保存 legacy `currentClocktowerScript` 与 `clocktowerRulesetRef`。统一 identity、handler compatibility、旧存档 migration 继续属于 S4。

### 7.3 `troubleBrewingRulesetRefFor(...)`

该 legacy helper 仍服务 TB A3/A4 correctness boundary。不要在 S1.3 为了形式通用化而提前修改；dual-script catalog 通过后再在 S4 收口。

## 8. R5.5 全局约束

- Script/Board 只组合角色，复杂规则留 Kotlin；
- 不构建通用规则 JSON DSL；
- 不从 ability text 推断 behavior；
- Character/Role Registry 是 metadata + handler binding 的单一入口；
- ClocktowerFlowPlanner 与 WerewolfFlowPlanner 分离；
- `VERIFIED / PARTIAL / UNVERIFIED` 决定 custom/homebrew 自动化安全等级；
- TB legacy flow 只有 shadow/golden parity 后才能移除；
- 新增只由已有角色构成的 script/board 不应要求修改 Host UI 或 flow core；
- R6 decision point 必须来自 script-aware `FlowPlanner -> HostInteraction / StorytellerDecisionPoint` seam。

## 9. R6 — revision-driven dynamic decision engine

**BLOCKED BY R5.5。**

只有 S0–S5 通过后，`storyteller_revision_driven_dynamic_decision_engine_plan.md` 才能从 BLOCKED 改为 READY。

R6 不得重新引入：

- Trouble Brewing enum/role-name `when` 作为流程事实；
- Compose UI 决定“下一个角色是谁”；
- `nightOrderPosition` 作为唯一流程定义；
- Werewolf fixed `JudgeStep` 扩展模式。

## 10. 生产保护线

在后续路线明确修改前：

```text
Production Host flow: existing legacy path
Production recommendation engine: existing production path
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
A4.5 cache: debug/shadow only
B4 DynamicPlayerWorldSetShadow: isolated shadow only
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
R5.5 ClocktowerFlowPlanner: shadow-only during S1
R6: BLOCKED
```

任何后续优化或结构重构都不能：

- 截断 exact worlds 后仍声称 exact；
- 把 timeout/OOM/cap 当 UNSAT；
- 省略 Spy/Recluse/Drunk/Poisoner/red-herring 规则分支；
- 把 storyteller-only truth 放入普通玩家知识；
- 让 background result 覆盖已展示/提交决定；
- 以“多剧本准备”为名提前改写 production recommendation；
- 以 JSON 内容化为名把复杂规则变成未经验证的通用规则 DSL；
- 为 No Greater Joy 新建第二套 catalog/flow framework。

## 11. GitHub 开发操作策略

默认：

```text
read-only audit
→ Git Data API / Contents API 的最小原子 source commit
→ exact diff audit
→ normal PR CI
```

只有超大文件、connector 无法安全构造完整内容时，才使用 temporary trusted writer；结束后必须清理 writer/trigger/temporary base，并恢复 normal read-only workflow。

详细规则：`docs/github_connector_large_file_editing_playbook.md`。

本次多剧本重新审计**没有发现新的 GitHub Actions / connector 机制**，因此 playbook 不需要追加重复规则，也不需要新增 workflow。现有正常 gate 继续使用：

```text
Android unit tests + debug APK
ASP contract tests
real Clingo cross-validation
R2 structural verifier
```

PR #2 在 R5.5 期间继续保持：

- open；
- Draft；
- base=`main`；
- **do not merge**。

## 12. 下个新会话启动点

直接读取：

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
2. `docs/r5_5_multiscript_progress_handoff_2026-08-20.md`
3. `docs/多剧本多板子与动态游戏流程架构设计_v1.md`

然后：

```text
确认 PR #2 / branch head
→ 确认 5a628675... 之后只有 docs-only handoff updates
→ 审计 S1.2 flow files 与 legacy conditional night-step eligibility
→ S1.3 tests-first implementation
→ minimal atomic source commit
→ exact diff audit
→ normal CI
→ S1.3 PASS
→ S1.4 legacy↔planner shadow differential
→ S1 PASS 后直接进入真实 No Greater Joy S2 proof
```

不要重新讨论是否需要新“多剧本框架”：当前结论已经冻结为 **现有 S0/S1 seam 足够，第二剧本用于验证框架而不是触发再抽象**。

## 13. 文档状态维护

后续只在本文更新“当前执行点”。专项设计/退出证据维护独立 spec/review/handoff，但不得创建并列的“当前路线”。
