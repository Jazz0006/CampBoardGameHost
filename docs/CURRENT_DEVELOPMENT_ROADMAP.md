# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-20  
> 当前分支：`codex/storyteller-algorithm-v4`  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> 多剧本架构规范：`多剧本多板子与动态游戏流程架构设计_v1.md`  
> 历史 R5.5 交接：`r5_5_multiscript_progress_handoff_2026-08-20.md`  
> Phase A 退出评审：`phase_a_exit_review_2026-08-20.md`

## 1. 当前结论

Phase A remediation 与 R5.5 software gate 已按以下顺序完成：

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
R5.5 Script & Dynamic Flow Foundation / production flow cutover
  ↓
DEVICE REGRESSION + PR READY/MERGE RELEASE GATE
  ↓
R6 revision-driven dynamic decision engine
```

R1–R5 已全部通过，Phase A correctness exit 已签署。

**当前执行点（2026-08-20）：R5.5 / S5 SOFTWARE PASS。S0–S5 的代码、differential、persistence regression 与 clean-head CI gate 已完成。Production Clocktower Host 与 Werewolf Judge 的步骤顺序/eligibility 已由各自 FlowPlanner 负责；旧的独立 flow-order authority 已移除。下一步不是继续 R6 开发，而是先执行真实设备全量回归；设备通过后将 PR #2 Draft→Ready、再次确认 CI、合并 main。R6 当前为 SOFTWARE READY / RELEASE HOLD。**

最新已验证 clean source baseline：

```text
cf324b3794feea15fcb5e7f7551d91bf1e7f181c
clean S5 source head（temporary transport workflows 已全部移除）

Clocktower cutover clean baseline:
948c45fd2b7cad9cf7b562aa96ceda9db886dbe6

Werewolf production cutover source commit:
74b3efc3baca70186b982b6b1e035da870dcbc02
```

clean S5 head `cf324b37...`：

- normal CI #214：SUCCESS；
- Android unit tests：SUCCESS；
- debug APK build：SUCCESS；
- ASP contract tests：SUCCESS；
- real Clingo cross-validation：SUCCESS；
- R2 structural verifier #207：SUCCESS。

Clocktower cutover 的前一 clean gate `948c45fd...` 也已通过 CI #205 + R2 #197。

S5 exact-diff / authority audit 结论：

- Clocktower production other-night 不再存在 `legacyOtherNightOrder` / numeric order table；planner 使用 resolved facts + stable interaction identities 决定顺序；
- daytime Scarlet Woman succession 使用独立 next-night identity interaction，并在 Imp action 前由 planner 排序；same-night Imp self-kill succession confirmation 继续作为独立 UI lifecycle，不构成第二套 flow-order authority；
- Werewolf production 已删除 `val steps = buildList { Wolves / optional Seer / Witch / Hunter / Dawn / DayVote }` 以及对应 role-name eligibility 分支；
- Werewolf production 从全部 dealt cards 机械构造 runtime role deck，经 `WerewolfRoleRegistry` fail-closed 映射，再由 `WerewolfFlowPlanner` 投影现有 `WerewolfJudgeStep` UI adapter；死亡角色不改变 role-existence step eligibility；
- `ClocktowerNightAction` / `WerewolfJudgeStep` 等 UI/action adapter 仍保留，但不再独立决定流程顺序；
- S5 没有修改 A3/A4 recommendation correctness source，也没有借 cutover 修改游戏规则；
- workflow 目录已恢复为永久 `ci.yml` + `r2-write-probe.yml`，无 one-shot patch workflow 残留。

A4 ZDD 继续保持 exact shadow/prototype；`ZDD_DEVICE_VALIDATED` 仍未授权。

## 2. 阶段状态

| 阶段 | 当前状态 | 说明 |
|---|---|---|
| A0 外部参考冻结 | PASS | 冻结参考继续有效。 |
| A1 Unified Semantic Model | PASS | storyteller truth / observation / player knowledge 三层边界保留。 |
| A1.1 Semantic Hardening | PASS WITH FOLLOW-UP | schema-v2、registration interaction binding、world-set identity 成立；R6 正式多夜 player-world reasoning 前仍需处理 P1。 |
| A2 ASP Oracle harness | PASS / R3.1 | nested `FormalGameState` schema-v2 与 fail-closed typed decoder 已验证。 |
| A2.1 Golden corpus | PASS / R3.1 | 52 total；24 Clingo executable；`UNEXPLAINED_MISMATCH=0`、`NOT_RUN=0`。 |
| **A3 EnumeratedWorldSet** | **PASS / R5 RE-EXIT VALIDATED** | exact correctness baseline。 |
| **MainActivity decomposition** | **PASS / R2 BATCHES 1–10 VALIDATED** | Activity shell、三游戏边界、Clocktower setup/day/night/host/history 已机械拆分。 |
| **A4 ZDD prototype** | **PASS AS EXACT SHADOW / NOT DEVICE-VALIDATED** | correctness differential 通过；production selector 不读取 shadow。 |
| **A4.5 observation cache rebuild** | **PASS / R4 VALIDATED** | durability、cancellation、generation identity、revision/session invalidation、OOM/failure semantics 与 production-shadow isolation 已验证。 |
| **R5 Phase A re-exit** | **PASS** | exit review 已签署。 |
| **R5.5 S0 Schema/Catalog** | **PASS** | official/custom JSON normalization、typed validation、canonical TB asset。 |
| **R5.5 S1 TB FlowPlanner migration** | **PASS** | pure planner / projector / resolved facts / stable identities / legacy differential 全绿；S5 已接 production。 |
| **R5.5 S2 NGJ second-script proof** | **PASS** | canonical NGJ asset、metadata/handlers、dual-script structural proof；S5 production path 共用同一 planner seam。 |
| **R5.5 S3 Werewolf registry/planner** | **PASS** | typed role/board registry、house-rule separation、classic-template parity、fail-closed validation；S5 已接 production。 |
| **R5.5 S4 persistence/ruleset identity** | **PASS** | schema v2、Clocktower/Werewolf content identity、显式 v1 migration、immutable TB ruleset basis 与 succession recovery。 |
| **R5.5 S5 regression/legacy removal/R6 handoff** | **SOFTWARE PASS** | Clocktower/Werewolf production planner cutover、legacy authority removal、persistence regression、clean-head CI/R2 全部通过。 |
| **R5.5 release/device gate** | **NEXT** | 真实设备完整回归 → Draft→Ready → final green CI → merge main。 |
| R6 revision-driven production expansion | SOFTWARE READY / RELEASE HOLD | 不在本 long-lived branch 上继续；release/device gate 与 merge 后再启动。 |

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

这些语义债务不重新打开 Phase A 或 R5.5，但必须在真正 production 化多夜 player-world reasoning 前处理。

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

目标是在不重写 Possible Worlds 的前提下，让内容身份、角色注册和游戏流程不再硬编码为 Trouble Brewing enum / fixed Werewolf JudgeStep，并为 R6 提供稳定 variant-aware decision seam。

总体结果：

```text
S0 Schema / Catalog / official-custom JSON normalization / validation      PASS
S1 Trouble Brewing FlowPlanner golden-equivalent migration                 PASS
S2 No Greater Joy real second-script structural proof                      PASS
S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration           PASS
S4 persistence/ruleset identity migration                                  PASS
S5 production planner cutover + regression + legacy authority removal      SOFTWARE PASS
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

已建立 strong typed catalog、`RulesetJsonLoader.parseScript(...)` adapter、deterministic normalized hash、typed validation、official/custom schema compatibility、homebrew/bootlegger safe downgrade、canonical `trouble_brewing.json`。

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
- deterministic / unique stable interaction identity；
- FlowPlanner 不 hardcode script name，也不重复实现 poison/protection/death correctness。

S1 阶段先以 shadow/differential 方式建立 parity；**S5 已完成 production Clocktower Host cutover**。

## 6. S2 — No Greater Joy real second-script structural proof — PASS

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

S5 production cutover 继续使用同一 variant-aware planner seam，没有为 NGJ 建第二套 Host flow。

## 7. S3 / S4 / S5 — Werewolf、persistence 与 production cutover

### 7.1 S3 typed Werewolf registry / board / planner — PASS

新增：

```text
WerewolfRoleId / WerewolfRoleIds
WerewolfRoleDefinition / WerewolfRoleRegistry
WerewolfBoardId / WerewolfBoardDefinition / WerewolfBoardRegistry
WerewolfRuleOptions
WerewolfFlowPlanner
```

当前 built-in interaction order：

- Werewolf：10，ROLE_ACTION；
- Seer：20，ROLE_ACTION；
- Witch：30，ROLE_ACTION；
- Hunter：40，ROLE_STATUS；
- 再追加 Dawn / DayVote system boundaries。

全部 4–12 人 `werewolfTemplates` 都映射为 `classic_<playerCount>` typed board；`LastWordsMode` 与 board composition identity 分离；invalid empty/non-positive role count fail closed。

S3 阶段 planner 先作为 shadow 与 legacy builder 做 differential；所有 classic templates 均 order equivalent，并明确保留“角色存在即保留 step”，而不是 alive-only eligibility。

### 7.2 S4 persistence / ruleset identity — PASS

S4 冻结“游戏按什么内容与执行语义创建/恢复”的 identity，并把 production active-game persistence 升级为 schema v2：

- `PersistedGameContentIdentity` 分离 variant kind / stable ID / normalized hash / semantic version / provenance；
- Clocktower v1 script enum 只做显式 ID migration，assigned roles 只做一致性验证；
- Werewolf identity 使用 board ID + normalized role-deck hash，house-rule options 独立比较；
- malformed / content mismatch / semantic mismatch / house-rule mismatch 全部 fail closed；
- restore 在 live-state mutation 前完成 identity 与 TB ruleset basis/ref 验证；
- `RulesetRef.scriptContentHash` 继续服务 A3/A4 correctness，不与 normalized script content hash 混用；
- TB setup-time role basis immutable；Scarlet Woman / Imp succession 不改变该 identity；
- v1 succession 若无法凭旧 `RulesetRef` 唯一反推原 basis，则拒绝恢复而不猜测。

S4 clean source head：`6b3052ba...`；CI #164 success；R2 #156 success。

### 7.3 S5 Clocktower production cutover — PASS

Clocktower 的 production cutover 采用 contract-first / differential-first：

- first-night 与 other-night production order 由 canonical planner seam 决定；
- production 把已由现有规则逻辑解析出的事实作为 `ClocktowerResolvedFlowFacts` 输入 planner，不在 planner 重算游戏规则；
- `DemonSuccessor`、Mayor redirect、Ravenkeeper、Undertaker、Sage 等 conditional interactions 使用 stable identities；
- daytime Scarlet Woman 因 Demon 白天死亡而继任时，记录独立 `clocktowerPendingNightNewDemonIdentityName`；下一夜生成稳定 `new_demon_identity` interaction，并由 planner 排在 Imp action 前；
- same-night Imp self-kill succession 继续使用既有 confirmation lifecycle，未错误复用 next-night pending；
- new identity pending 进入 `ClocktowerNightCheckpoint` save/restore，并只在整夜完成 boundary 清除；
- `legacyOtherNightOrder` numeric table 已从 production 删除并由负向 source guard 守护。

Clocktower clean baseline `948c45fd...`：CI #205 success；R2 #197 success。

### 7.4 S5 Werewolf production cutover — PASS

Werewolf RED `001c67cf...`：442 tests 中恰好 2 个新 production wiring guards 失败；编译/APK、其余 440 tests、R2、ASP、Clingo 均正常。

Production cutover source `74b3efc3...` 只替换 `WerewolfHostScreen.kt` 的 legacy step builder：

```text
all dealt cards
→ roleRegistry.roleIdFor(...) fail closed
→ normalized runtime role deck
→ WerewolfBoardDefinition
→ WerewolfFlowPlanner.plan(...)
→ existing WerewolfJudgeStep UI adapter
```

保留语义：

- 死亡角色仍属于 dealt board，因此 role step eligibility 不变；
- `aliveCards` 仍只负责可操作目标；
- Witch / Hunter / death / callbacks / UI 逻辑未因 cutover 改写；
- legacy `buildList + Role.Seer/Witch/Hunter` independent eligibility branches 已删除。

### 7.5 S5 final regression / authority audit — SOFTWARE PASS

S5 门槛已按顺序完成：

```text
full multi-script / multi-board regression                         PASS
→ production Clocktower Host planner cutover + legacy differential PASS
→ production Werewolf Judge planner cutover + legacy differential  PASS
→ legacy independent flow authority removal                        PASS
→ persistence restore/recovery regression                           PASS
→ clean-head Android/ASP/Clingo/R2 validation                       PASS
→ R6 SOFTWARE READY
```

最终 clean source head：`cf324b3794feea15fcb5e7f7551d91bf1e7f181c`。

最终 gate：CI #214 success；R2 #207 success。

S5 没有借结构迁移修改 Clocktower/Werewolf 游戏规则；没有扩大 A3/A4；没有引入通用 JSON rules DSL。

## 8. 明确继续延后的范围

### 8.1 A3/A4 Possible Worlds

不要因为 NGJ、Werewolf 或 flow production cutover 顺手泛化 A3/A4。

允许：

```text
TB:  Catalog ✅ / FlowPlanner production ✅ / Possible Worlds exact+shadow baseline
NGJ: Catalog ✅ / FlowPlanner production seam ✅ / Possible Worlds deferred
```

### 8.2 Legacy UI adapters vs legacy flow authority

S5 删除/绕过的是**能独立决定步骤顺序或 eligibility 的旧 flow authority**，不是机械删除所有 legacy-named UI types。

因此当前允许继续存在：

- `ClocktowerNightAction`：UI/action dispatch adapter；
- `WerewolfJudgeStep`：planner interaction 到现有 Judge UI 的 adapter；
- same-night Imp succession confirmation screen：独立 lifecycle UI，不与 daytime next-night identity 共用 pending；
- 其他不决定 flow order 的 display/record adapter。

不得重新引入：

- Clocktower numeric/manual night order table；
- Werewolf `cards.any(Role.X)` + manual `buildList` 作为 production step authority；
- Compose UI 自己决定“下一个角色是谁”。

### 8.3 Trouble Brewing ruleset persistence boundary

S4 已将该边界拆为 `troubleBrewingRulesetKnowledge()`、immutable `ClocktowerRulesetPersistenceBasis` 与 `TroubleBrewingRulesetPersistence`；existing `RulesetRef` 继续服务 TB A3/A4 correctness，normalized script identity 保持独立，后续不得混用两种 hash 语义。

## 9. R5.5 全局约束

- Script/Board 只组合角色，复杂规则留 Kotlin；
- 不构建通用规则 JSON DSL；
- 不从 ability text 推断 behavior；
- Character/Role Registry 是 metadata + handler binding 的单一入口；
- ClocktowerFlowPlanner 与 WerewolfFlowPlanner 分离；
- `VERIFIED / PARTIAL / UNVERIFIED` 决定 custom/homebrew 自动化安全等级；
- 新增由已有角色构成的 script/board 不应要求为角色顺序修改 Host flow core；
- board composition 与独立 house-rule options 分离；
- invalid content/identity 必须 fail closed，不能 silent rewrite；
- R6 decision point 必须来自 variant-aware `FlowPlanner -> HostInteraction / StorytellerDecisionPoint` seam。

## 10. R6 — revision-driven dynamic decision engine

**SOFTWARE READY / RELEASE HOLD。**

S0–S5 software gate 已通过，因此 `storyteller_revision_driven_dynamic_decision_engine_plan.md` 的软件前置条件已满足。

但本 long-lived branch 的结束策略不变：

```text
S5 clean software gate
→ real-device full regression
→ PR #2 Draft → Ready
→ final CI confirmation
→ merge main once
→ THEN start R6 from post-merge main / new branch
```

R6 不得重新引入：

- Trouble Brewing enum/role-name `when` 作为流程事实；
- Compose UI 决定“下一个角色是谁”；
- `nightOrderPosition` 作为唯一流程定义；
- Werewolf fixed `JudgeStep` 扩展模式；
- 在未解决 P1 的情况下直接 production 化多夜 Possible Worlds。

## 11. 生产保护线

在后续路线明确修改前：

```text
Production Clocktower Host flow order: Clocktower planner-backed
Production Werewolf Judge flow order: Werewolf planner-backed
Legacy UI adapters: allowed only when they do not own flow order/eligibility
Production recommendation engine: existing production path
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
A4.5 cache: debug/shadow only
B4 DynamicPlayerWorldSetShadow: isolated shadow only
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
R6 software prerequisite: READY
R6 branch execution: HOLD until device regression + PR merge
```

任何后续优化或结构重构都不能：

- 截断 exact worlds 后仍声称 exact；
- 把 timeout/OOM/cap 当 UNSAT；
- 省略 Spy/Recluse/Drunk/Poisoner/red-herring 规则分支；
- 把 storyteller-only truth 放入普通玩家知识；
- 让 background result 覆盖已展示/提交决定；
- 以“多剧本准备”为名改写 production recommendation correctness；
- 以 JSON 内容化为名把复杂规则变成未经验证的通用 DSL；
- 为 No Greater Joy 新建第二套 catalog/flow framework；
- 重新引入第二套 production flow-order authority；
- 静默把旧 save 解释为最新同名 content。

## 12. GitHub / release 操作策略

默认开发策略：

```text
read-only audit
→ tests-first / contract-first where behavior changes
→ 最小原子 source commit / CAS contents update
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

PR #2 当前继续保持：

- open；
- Draft；
- base=`main`；
- **do not merge before device regression**。

### 12.1 下一步真实设备回归

至少覆盖：

1. Trouble Brewing：新建 → 第一夜 → 白天 → 第二夜 → 天亮；确认 planner order 与 interaction UI 连续性；
2. Trouble Brewing：白天 Demon 死亡 + Scarlet Woman 继任；下一夜先私下确认新 Imp 身份，再进入 Imp action；
3. Trouble Brewing：Imp 夜间 self-kill + successor；确认仍走 same-night succession lifecycle；
4. No Greater Joy：新建 → 第一夜 → 白天 → 第二夜，覆盖 Sage / Scarlet Woman conditional flow；
5. Werewolf：至少一个含 Seer/Witch/Hunter 的 board；死亡特殊角色后确认其 role step existence 仍与旧行为一致；
6. Werewolf：Witch save/poison、Hunter death shot、Dawn、DayVote；
7. persistence：TB、NGJ、Werewolf 各至少一次 create → restart → restore；
8. legacy v1：至少一次可机械迁移的 restore smoke；如有 succession legacy fixture，确认 ambiguous/missing ref 仍 fail closed；
9. navigation/back/step index：确认 planner cutover 后不会出现跳步、重复步或返回后错位；
10. 结果页/重新开局：确认 pending night identity / draft state 不跨游戏泄漏。

设备回归通过后：

```text
记录 device evidence
→ PR #2 Draft → Ready
→ 确认 latest clean head CI green
→ merge main
→ 结束 codex/storyteller-algorithm-v4 长分支
```

## 13. 下个新会话启动点

直接读取：

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
2. 如需架构细节，再读 `docs/多剧本多板子与动态游戏流程架构设计_v1.md`
3. 如需 R5.5 历史，再读 `docs/r5_5_multiscript_progress_handoff_2026-08-20.md`

然后：

```text
确认 PR #2 / branch head
→ 确认 latest validated S5 source baseline cf324b37...
→ 不再进行 S5 source 扩展
→ 执行/记录 real-device full regression
→ 如设备发现 defect：tests-first 修复 + clean CI + 重跑相关 device case
→ 全部通过后 Draft→Ready + final CI + merge main
→ R6 从 post-merge main / 新分支开始
```

## 14. 文档状态维护

后续只在本文更新“当前执行点”。专项设计/退出证据维护独立 spec/review/handoff，但不得创建并列的“当前路线”。
