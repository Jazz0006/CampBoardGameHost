# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-20  
> 当前分支：`codex/storyteller-algorithm-v4`  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> 多剧本架构规范：`多剧本多板子与动态游戏流程架构设计_v1.md`  
> 历史 R5.5 交接：`r5_5_multiscript_progress_handoff_2026-08-20.md`  
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

**当前执行点（2026-08-20）：R5.5 / S4 NEXT。S0 PASS；S1 Trouble Brewing FlowPlanner migration PASS；S2 No Greater Joy real second-script proof PASS；S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner shadow migration PASS。下一 source batch 从 S4 persistence / ruleset identity migration 开始。**

最新已验证 source baseline：

```text
90d0fd01e2424189cd6a078aafadd2878a48e383
fix(r5.5): fail closed invalid Werewolf board counts
```

该 source head 的 normal CI #135 已通过 Android unit tests + debug APK、ASP contract tests、real Clingo cross-validation；R2 structural verifier #127 也通过。

S3 使用两组 tests-first / contract-first 提交完成：

```text
a20cef0b64fcb7c73af5e43147a06c5aab228e52
  test(r5.5): define Werewolf registry planner parity

ecbe6d60af2e489104326da371403811cfa1ae34
  feat(r5.5): add Werewolf registry shadow planner

b33506acc9398546c46b925afc236ad4c9587c53
  test(r5.5): harden Werewolf legacy planner differential

90d0fd01e2424189cd6a078aafadd2878a48e383
  fix(r5.5): fail closed invalid Werewolf board counts
```

第一组 red 在 registry/planner 类型尚不存在时按预期 Android compile fail；green 后 CI #133 / R2 #125 全绿。第二组 hardening red 的 363 个 Android tests 中只有 non-positive role-count contract 失败，证明旧实现会静默删除 count=0 角色；最终 green 改为 fail closed 后 CI #135 / R2 #127 全绿。

R5 只解锁 R5.5，**没有**直接解锁 R6。R6 仍必须等待 S4–S5 全部通过。

A4 ZDD 继续保持 exact shadow/prototype；`ZDD_DEVICE_VALIDATED` 仍未授权。

## 2. 阶段状态

| 阶段 | 当前状态 | 说明 |
|---|---|---|
| A0 外部参考冻结 | PASS | 冻结参考继续有效。 |
| A1 Unified Semantic Model | PASS | storyteller truth / observation / player knowledge 三层边界保留。 |
| A1.1 Semantic Hardening | PASS WITH FOLLOW-UP | schema-v2、registration interaction binding、world-set identity 成立；R6 前仍需完成 timeline identity 等 P1 语义债务。 |
| A2 ASP Oracle harness | PASS / R3.1 | nested `FormalGameState` 已迁移 schema-v2；Python 与 Android typed decoder 共用 fail-closed 合同。 |
| A2.1 Golden corpus | PASS / R3.1 | 52 total；24 Clingo executable；`UNEXPLAINED_MISMATCH=0`、`NOT_RUN=0`。 |
| **A3 EnumeratedWorldSet** | **PASS / R5 RE-EXIT VALIDATED** | exact correctness baseline；R1/R3/R5 验证通过。 |
| **MainActivity decomposition** | **PASS / R2 BATCHES 1–10 VALIDATED** | Activity shell、三游戏边界、Clocktower setup/day/night/host/history 已机械拆分；read-only structural verifier 持续守护。 |
| **A4 ZDD prototype** | **PASS AS EXACT SHADOW / NOT DEVICE-VALIDATED** | correctness differential 通过；production selector 不读取 shadow。 |
| **A4.5 observation cache rebuild** | **PASS / R4 VALIDATED** | durable-before-build、cancellation、generation identity、revision/session invalidation、OOM/failure semantics、production-shadow isolation 均通过。 |
| **R5 Phase A re-exit** | **PASS** | exit review 已签署；clean-head Android/ASP/Clingo/structural CI 通过。 |
| **R5.5 S0 Schema/Catalog** | **PASS** | official/custom JSON normalization、typed validation、canonical TB asset 已通过。 |
| **R5.5 S1 TB FlowPlanner migration** | **PASS** | S1.1–S1.4 全部通过；shadow-only；legacy production Host 尚未切换。 |
| **R5.5 S2 NGJ second-script proof** | **PASS** | canonical NGJ asset、central registry metadata、conditional handlers、dual-script structural tests 全绿；FlowPlanner core 无 script-name 分支。 |
| **R5.5 S3 Werewolf registry/planner** | **PASS** | typed role/board registry、house-rule separation、pure shadow planner、all classic-template legacy parity 与 fail-closed validation 全绿。 |
| **R5.5 S4 persistence/ruleset identity** | **NEXT** | 统一 variant/content/semantic compatibility identity，并明确旧存档 migration / rejection。 |
| R5.5 S5 regression/legacy removal/R6 handoff | BLOCKED BY S4 | full regression 后才允许关闭 legacy flow 并准备 R6。 |
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

目标是在不重写 Possible Worlds 的前提下，让内容身份、角色注册和游戏流程不再硬编码为 Trouble Brewing enum / fixed Werewolf JudgeStep，为 R6 提供稳定 variant-aware decision seam。

总体顺序：

```text
S0 Schema / Catalog / official-custom JSON normalization / validation      PASS
S1 Trouble Brewing FlowPlanner golden-equivalent migration                 PASS
S2 No Greater Joy real second-script structural proof                      PASS
S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration           PASS
S4 persistence/ruleset identity migration                                  NEXT
S5 full regression + legacy flow removal + R6 handoff
```

### 5.1 S0 — PASS

S0 建立的多剧本基础继续作为唯一 Clocktower framework：

```text
ClocktowerCharacterDefinition
ClocktowerCharacterRegistry
ClocktowerScriptDefinition
ClocktowerScriptCatalog
ValidatedClocktowerRuleset
NightOrderToken
```

关键 source：

```text
S0.1 beb23512b4133712025e534152d268fd9315a5bf
S0.2 b02153f821f2403aa39d733d3fe30ace3e6abebd
S0.3 df0509da...
```

已建立 strong typed catalog、existing `RulesetJsonLoader.parseScript(...)` adapter、deterministic normalized hash、typed validation、official/custom schema compatibility、homebrew/bootlegger safe downgrade、canonical `trouble_brewing.json`。

### 5.2 S1 — Trouble Brewing FlowPlanner golden migration — PASS

关键 source：

```text
S1.1 e76614ffc4382e247cf37d6c994172642b922a8c
S1.2 5a628675e3f047d919908fbd7f14eaa31b788ac6
S1.3 e4fec575c974839941e79db1f55fa16f5971ad83
S1.4 b32101d80b3c70f674ed9d864d85a4bd16ce5b81
```

已验证：

- pure base night plan；
- stable `ClocktowerHostInteraction`；
- Fortune Teller red-herring setup；
- resolved facts / conditional-event interactions；
- DemonSuccessor / Mayor / Ravenkeeper / Undertaker ordering；
- production `officialNightOrder()` ↔ shadow planner differential；
- deterministic / unique stable interaction identity；
- FlowPlanner 不 hardcode script name，也不重复实现 poison/protection/death correctness。

**Production Clocktower Host 仍完全使用 legacy flow。**

## 6. S2 — No Greater Joy real second-script structural proof — PASS

S2 直接使用 App 已有真实 No Greater Joy，而不是 toy fixture。

No Greater Joy 角色池：

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

关键 source：

```text
ea9a34ef36ab93f6528a669d3f5a036b81a07da6  tests-first red
5058a473e5ef46a69c1bd81239b725dff684181d  implementation
```

已建立 canonical `no_greater_joy.json`、NGJ-only official metadata、Sage/Scarlet Woman resolved-fact handlers，并证明：

```text
新增 NGJ
= script asset + character metadata/handler + tests
≠ FlowPlanner core 中新增 NoGreaterJoy when/if
```

`ClocktowerFlowPlanner.kt` 在 S2 diff 中零改动。验证：CI #130 success；R2 #122 success。

**仍未接 production Clocktower Host。**

## 7. S3 — Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration — PASS

S3 从当前 legacy 行为出发：

```text
WerewolfTemplate(playerCount, werewolfCount, includeSeer/includeWitch/includeHunter)
        ↓
WerewolfRoleRegistry + WerewolfBoardRegistry
        ↓
pure WerewolfFlowPlanner shadow
        ↓
legacy JudgeStep differential
```

### 7.1 typed role registry

新增：

```text
WerewolfRoleId / WerewolfRoleIds
WerewolfRoleDefinition
WerewolfRoleRegistry
WerewolfTeam
WerewolfInteractionKind
WerewolfWakePolicy
WerewolfInteractionCompletionPolicy
```

当前 built-in 角色：

- Villager：无夜间 interaction；
- Werewolf：order 10，ROLE_ACTION；
- Seer：order 20，ROLE_ACTION；
- Witch：order 30，ROLE_ACTION；
- Hunter：order 40，ROLE_STATUS。

Hunter 保留当前 legacy 的“夜间状态步骤”；实际死亡后开枪仍由现有 Dawn / DayVote 行为处理。S3 没有借结构迁移改规则。

### 7.2 typed board registry + house-rule separation

新增：

```text
WerewolfBoardId
WerewolfBoardDefinition
WerewolfBoardRegistry
WerewolfRuleOptions
```

全部现有 4–12 人 `werewolfTemplates` 都映射为 `classic_<playerCount>` typed board，并保持相同角色组成。

`LastWordsMode` 放在独立 `WerewolfRuleOptions`，不进入 board composition source。board content hash 由 normalized role deck 决定。

`WerewolfBoardDefinition.create(...)` 对空 deck / non-positive role count fail closed，不允许把非法输入静默规范化成另一副板子。

### 7.3 pure WerewolfFlowPlanner shadow

新增独立 `WerewolfFlowPlanner`；不复用 `ClocktowerFlowPlanner`。

planner：

- 从 RoleRegistry 读取 role-specific interaction metadata；
- 按 metadata order 生成实际在 board 中存在的 role interactions；
- 再追加 Dawn / DayVote system boundaries；
- stable interaction IDs deterministic + unique；
- planner core 不 hardcode `Role.Seer/Witch/Hunter` 或 `classic_8` 等 board name。

### 7.4 legacy differential

`WerewolfLegacyPlannerDifferentialTest` 直接锚定 production `WerewolfHostScreen.kt` 当前 step builder：

```text
Wolves
→ optional Seer
→ optional Witch
→ optional Hunter
→ Dawn
→ DayVote
```

所有 4–12 人 classic templates 均 legacy ↔ planner order equivalent。

当前 production 使用 `cards.any { role == ... }`，而不是 alive-only eligibility；S3 differential 明确保留该可观察行为，不因结构迁移静默改变死亡角色步骤存在性。

最终 S3 exact diff 从 `8aad6bf3...` 到 `90d0fd01...` 只有：

```text
4 个新增 shadow source files
2 个新增 test files
```

没有修改：

```text
WerewolfGameSupport.kt
WerewolfHostScreen.kt
CampBoardGameHostApp persistence/state
Clocktower production flow
A3/A4 recommendation correctness path
```

验证：CI #135 success；R2 #127 success。

**S3 结论：PASS。Production Werewolf Judge 仍完全使用 legacy step builder。**

### 7.5 S4 — NEXT：persistence / ruleset identity migration

S4 开始正式冻结“游戏是按什么内容与执行语义创建/恢复”的 identity。至少需要同时覆盖 Clocktower 和 Werewolf：

```text
variant/script/board id
normalized content hash
ruleset / semantic-handler compatibility version
actual assigned role IDs
relevant house-rule options
import/source provenance where applicable
```

旧存档不得只按“当前 catalog 中同名 script/board”静默重新解释；必须有显式 migration 或 fail-closed rejection。

S4 仍不负责关闭 legacy Host/Judge flow；legacy removal 属于 S5。

## 8. 明确继续延后的范围

### 8.1 A3/A4 Possible Worlds

不要因为 NGJ、Werewolf 或 persistence migration 顺手泛化 A3/A4。

允许：

```text
TB:  Catalog ✅ / FlowPlanner ✅ / Possible Worlds exact+shadow baseline
NGJ: Catalog ✅ / FlowPlanner ✅ / Possible Worlds deferred
```

### 8.2 Legacy flow cutover

S0–S3 建立的是已验证 shadow foundation。Production Clocktower Host 与 Werewolf Judge 继续使用 legacy source，直到 S5 full regression / removal gate 明确授权。

### 8.3 `troubleBrewingRulesetRefFor(...)`

该 helper 继续服务 TB A3/A4 correctness boundary；S4 可以审计并设计统一 identity seam，但不能破坏当前 exact correctness contract。

## 9. R5.5 全局约束

- Script/Board 只组合角色，复杂规则留 Kotlin；
- 不构建通用规则 JSON DSL；
- 不从 ability text 推断 behavior；
- Character/Role Registry 是 metadata + handler binding 的单一入口；
- ClocktowerFlowPlanner 与 WerewolfFlowPlanner 分离；
- `VERIFIED / PARTIAL / UNVERIFIED` 决定 custom/homebrew 自动化安全等级；
- 新增只由已有角色构成的 script/board 不应要求修改 Host UI 或 flow core；
- board composition 与独立 house-rule options 分离；
- invalid content/identity 必须 fail closed，不能 silent rewrite；
- R6 decision point 必须来自 variant-aware `FlowPlanner -> HostInteraction / StorytellerDecisionPoint` seam。

## 10. R6 — revision-driven dynamic decision engine

**BLOCKED BY R5.5。**

只有 S0–S5 通过后，`storyteller_revision_driven_dynamic_decision_engine_plan.md` 才能从 BLOCKED 改为 READY。

R6 不得重新引入：

- Trouble Brewing enum/role-name `when` 作为流程事实；
- Compose UI 决定“下一个角色是谁”；
- `nightOrderPosition` 作为唯一流程定义；
- Werewolf fixed `JudgeStep` 扩展模式。

## 11. 生产保护线

在后续路线明确修改前：

```text
Production Clocktower Host flow: existing legacy path
Production Werewolf Judge flow: existing legacy path
Production recommendation engine: existing production path
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
A4.5 cache: debug/shadow only
B4 DynamicPlayerWorldSetShadow: isolated shadow only
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
R5.5 ClocktowerFlowPlanner: validated shadow foundation; not production Host source
R5.5 WerewolfFlowPlanner: validated shadow foundation; not production Judge source
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
- 为 No Greater Joy 新建第二套 catalog/flow framework；
- 在 S5 前切换 production Clocktower Host / Werewolf Judge flow；
- 在 S4 中静默把旧 save 解释为最新同名 content。

## 12. GitHub 开发操作策略

默认：

```text
read-only audit
→ tests-first / contract-first where behavior changes
→ Git Data API / Contents API 的最小原子 source commit
→ exact diff audit
→ normal PR CI
```

现有 normal gates：

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

## 13. 下个新会话启动点

直接读取：

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
2. `docs/多剧本多板子与动态游戏流程架构设计_v1.md`
3. 如需历史上下文，再读 `docs/r5_5_multiscript_progress_handoff_2026-08-20.md`

然后：

```text
确认 PR #2 / branch head
→ 确认 latest validated source baseline 90d0fd01...
→ 审计当前 Clocktower / Werewolf save + restore identity 字段和 migration 行为
→ 列出 legacy currentClocktowerScript / clocktowerRulesetRef / Werewolf settings 的真实 persistence seam
→ S4 tests-first 冻结 variant/content/semantic compatibility identity contract
→ 最小 schema/migration implementation
→ old-save explicit migration or fail-closed rejection
→ exact diff audit
→ normal CI
```

S4 不切 production flow；S5 才负责 full regression、legacy flow removal 与 R6 handoff。

## 14. 文档状态维护

后续只在本文更新“当前执行点”。专项设计/退出证据维护独立 spec/review/handoff，但不得创建并列的“当前路线”。
