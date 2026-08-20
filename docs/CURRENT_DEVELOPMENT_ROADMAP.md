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

**当前执行点（2026-08-20）：R5.5 / S3 NEXT。S0 PASS；S1 Trouble Brewing FlowPlanner migration 已完整 PASS；S2 No Greater Joy real second-script structural proof 已 PASS。下一 source batch 从 S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration 开始。**

最新已验证 source baseline：

```text
5058a473e5ef46a69c1bd81239b725dff684181d
feat(r5.5): add No Greater Joy structural proof
```

该 source head 的 normal CI #130 已通过 Android unit tests + debug APK、ASP contract tests、real Clingo cross-validation；R2 structural verifier #122 也通过。

S2 使用 tests-first 两提交完成：

```text
ea9a34ef36ab93f6528a669d3f5a036b81a07da6
  test(r5.5): define No Greater Joy structural proof

5058a473e5ef46a69c1bd81239b725dff684181d
  feat(r5.5): add No Greater Joy structural proof
```

red commit 的 Android 编译在缺失 `NoGreaterJoyOfficialCharacterMetadata`、`SCARLET_WOMAN_BECAME_DEMON`、`SAGE_KILLED_BY_DEMON` 时按预期失败；implementation commit 补齐合同后全部 normal gates 转绿。

R5 只解锁 R5.5，**没有**直接解锁 R6。R6 仍必须等待 S3–S5 全部通过。

A4 ZDD 也没有因为 Phase A 退出或多剧本 structural proof 获得 production 授权：继续保持 exact shadow/prototype；`ZDD_DEVICE_VALIDATED` 仍未授权。

## 2. 阶段状态

| 阶段 | 当前状态 | 说明 |
|---|---|---|
| A0 外部参考冻结 | PASS | 冻结参考继续有效。 |
| A1 Unified Semantic Model | PASS | storyteller truth / observation / player knowledge 三层边界保留。 |
| A1.1 Semantic Hardening | PASS WITH FOLLOW-UP | schema-v2、registration interaction binding、world-set identity 成立；B4 前仍需完成 timeline identity 等 P1 语义债务。 |
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
| **R5.5 S3 Werewolf registry/planner** | **NEXT** | 下一 source batch；迁移 board/role identity 与 fixed JudgeStep flow seam。 |
| R5.5 S4 persistence/ruleset identity | BLOCKED BY S3 | 统一 identity、handler compatibility、旧存档 migration。 |
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

目标是在不重写 Possible Worlds 的前提下，让内容身份、角色注册和游戏流程不再硬编码为 Trouble Brewing enum / fixed Werewolf JudgeStep，为 R6 提供稳定 script-aware decision seam。

总体顺序：

```text
S0 Schema / Catalog / official-custom JSON normalization / validation      PASS
S1 Trouble Brewing FlowPlanner golden-equivalent migration                 PASS
S2 No Greater Joy real second-script structural proof                      PASS
S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration           NEXT
S4 persistence/ruleset identity migration
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

已建立：strong typed catalog、existing `RulesetJsonLoader.parseScript(...)` adapter、deterministic normalized hash、typed validation、official/custom schema compatibility、homebrew/bootlegger safe downgrade、canonical `trouble_brewing.json`。

### 5.2 S1.1 — pure shadow base-night planner — PASS

Source：`e76614ffc4382e247cf37d6c994172642b922a8c`

`ClocktowerFlowPlanner` 输入：

```text
ValidatedClocktowerRuleset
+ playerCount
+ inPlayRoleIds
        ↓
base night token plan
```

已验证：explicit script override、metadata derived order、system tokens、5–6 / 7+ evil-info eligibility、in-play filtering、off-script fail closed、TB first/other-night parity。

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

TB golden 锁定：

```text
Empath
→ Fortune Teller red-herring setup
→ Fortune Teller action
```

CI #124 success；R2 #116 success。

### 5.4 S1.3 — resolved flow facts + conditional/event interactions — PASS

Source：`e4fec575c974839941e79db1f55fa16f5971ad83`

建立/扩展：

```text
ClocktowerResolvedFlowFacts
CharacterInteractionHandler eligibility
before-role / after-role conditional interactions
EVENT_RESOLUTION interaction
```

已锁定：

- no fact → 不误生成 conditional interaction；
- fact 有但角色不在场 → 不凭空生成；
- DemonKill → optional DemonSuccessor → optional MayorRedirect → Ravenkeeper → Undertaker ordering；
- Scarlet Woman 普通 night token 作为 ordering anchor，不误投影普通 wake step；
- FlowPlanner 不重复计算 poison/protection/death correctness，只消费规则层已解析 facts。

CI #127 success；R2 #119 success。

### 5.5 S1.4 — legacy ↔ planner shadow differential — PASS

Source：`b32101d80b3c70f674ed9d864d85a4bd16ce5b81`

新增真实 differential，直接锚定 production `ClocktowerHostScreen.kt` 的 `officialNightOrder()`，覆盖：

- first night；
- other night；
- day execution → Undertaker transition；
- conditional/event-triggered interaction；
- deterministic / unique stable interaction identity。

该 batch 只新增测试文件，没有 production source 改动。

CI #128 success；R2 #120 success。

**S1 结论：PASS。Production Host 仍完全使用 legacy flow。**

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

### 6.1 tests-first red contract

Source：`ea9a34ef36ab93f6528a669d3f5a036b81a07da6`

新增 `NoGreaterJoyStructuralProofTest`，先要求：

- canonical `no_greater_joy.json`；
- NGJ-only official metadata；
- Clockmaker / Chambermaid / Artist / Sage / Klutz behavior keys；
- Artist / Klutz 不进入 night flow；
- 5–6 人 first-night evil info suppression；
- Sage 只在 Demon kill fact 后触发；
- Scarlet Woman 只有规则层已解析“已继任 Demon”事实时才在对应 ordering slot 产生 interaction；
- `ClocktowerFlowPlanner.kt` 不允许出现 TB/NGJ script-name literals。

red Android compile 按预期失败在上述尚未实现的 metadata/facts。

### 6.2 implementation

Source：`5058a473e5ef46a69c1bd81239b725dff684181d`

新增：

```text
app/src/main/assets/scripts/no_greater_joy.json
app/src/main/java/.../catalog/NoGreaterJoyOfficialCharacterMetadata.kt
```

central CharacterRegistry 通过现有 registry 类型扩展 NGJ-only official metadata；没有新增第二套 catalog / loader / generic script engine。

新增 resolved facts：

```text
SCARLET_WOMAN_BECAME_DEMON
SAGE_KILLED_BY_DEMON
```

现有 interaction registry 增加 Sage handler，并让 Scarlet Woman 的 ordering-anchor token 在规则层已经解析“已继任 Demon”时成为有效 interaction。复杂规则仍不进入 JSON，也不进入 FlowPlanner。

canonical NGJ first-night order：

```text
Investigator
→ Empath
→ Clockmaker
→ Chambermaid
```

canonical NGJ other-night structural order：

```text
Scarlet Woman
→ Imp
→ Sage
→ Empath
→ Chambermaid
```

S2 强判据已满足：

```text
新增 NGJ
= script asset + character metadata/handler + tests
≠ FlowPlanner core 中新增 NoGreaterJoy when/if
```

`ClocktowerFlowPlanner.kt` 在 S2 diff 中 **零改动**。

验证：CI #130 success；R2 #122 success。

**S2 结论：PASS。仍未接 production Host。**

## 7. S3 — NEXT：Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration

下一批进入狼人杀板子/角色/流程的结构迁移，但继续遵守 R5.5 的渐进式原则：

```text
现有 Werewolf board / role / JudgeStep 行为
        ↓
先建立 typed BoardRegistry / RoleRegistry
        ↓
再建立 pure WerewolfFlowPlanner shadow
        ↓
legacy ↔ planner parity tests
        ↓
通过前不切 production Judge UI
```

S3 第一批应先审计当前 Werewolf board identity、角色配置来源、`JudgeStep` 固定流程与夜间条件步骤，明确最小 typed model 和 golden parity；不要在第一批同时重写 production UI。

S3 不复用 `ClocktowerFlowPlanner`：两种游戏保留各自 planner，只共享更高层 HostInteraction/decision seam 的设计原则。

## 8. 明确继续延后的范围

### 8.1 A3/A4 Possible Worlds

不要因为 NGJ 或 Werewolf migration 顺手泛化 A3/A4。

允许：

```text
TB:  Catalog ✅ / FlowPlanner ✅ / Possible Worlds exact+shadow baseline
NGJ: Catalog ✅ / FlowPlanner ✅ / Possible Worlds deferred
```

### 8.2 Persistence / Ruleset identity

当前 save 同时保存 legacy `currentClocktowerScript` 与 `clocktowerRulesetRef`。统一 `ScriptId / RulesetRef / contentHash / handler compatibility identity` 与旧存档 migration 继续属于 S4。

### 8.3 `troubleBrewingRulesetRefFor(...)`

该 legacy helper 继续服务 TB A3/A4 correctness boundary；S4 前不为形式通用化提前改掉。

## 9. R5.5 全局约束

- Script/Board 只组合角色，复杂规则留 Kotlin；
- 不构建通用规则 JSON DSL；
- 不从 ability text 推断 behavior；
- Character/Role Registry 是 metadata + handler binding 的单一入口；
- ClocktowerFlowPlanner 与 WerewolfFlowPlanner 分离；
- `VERIFIED / PARTIAL / UNVERIFIED` 决定 custom/homebrew 自动化安全等级；
- TB legacy flow 只有 shadow/golden parity 后才能移除；
- 新增只由已有角色构成的 script/board 不应要求修改 Host UI 或 flow core；
- R6 decision point 必须来自 script-aware `FlowPlanner -> HostInteraction / StorytellerDecisionPoint` seam。

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
Production Host flow: existing legacy path
Production recommendation engine: existing production path
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
A4.5 cache: debug/shadow only
B4 DynamicPlayerWorldSetShadow: isolated shadow only
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
R5.5 ClocktowerFlowPlanner: validated shadow foundation; not production Host source
R5.5 WerewolfFlowPlanner: not implemented yet
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
- 在 S3 parity 通过前切换 production Werewolf Judge flow。

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
→ 确认 latest validated source baseline 5058a473...
→ 审计现有 Werewolf board / role / JudgeStep source
→ 定义 S3.1 最小 typed BoardRegistry / RoleRegistry contract
→ tests first
→ shadow-only implementation
→ exact diff audit
→ normal CI
```

不要重新讨论是否需要新“多剧本框架”：Clocktower 的 S2 real second-script proof 已经证明现有 S0/S1 seam 足够。

## 14. 文档状态维护

后续只在本文更新“当前执行点”。专项设计/退出证据维护独立 spec/review/handoff，但不得创建并列的“当前路线”。
