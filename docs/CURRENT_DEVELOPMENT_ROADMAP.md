# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-20
> 当前分支：`codex/storyteller-algorithm-v4`  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

## 1. 当前结论

2026-08-19 对 A0–A4.5 做了重新审计。审计推翻了“可以继续进入 Phase B”的前提：A3 存在真实规则语义缺口，A4.5 也有未满足的生命周期/持久化合同。

同时，当前 `MainActivity.kt` 已承担过多职责：Android Activity 生命周期、Compose 根界面、多游戏模式 UI、Clocktower 夜间/白天流程、持久化、revision wiring、A4/A4.5 shadow wiring 等均集中在同一大文件。继续直接在其中实施 A4.5 lifecycle 修复会提高误改和回归风险。

2026-08-20 的多剧本/多板子架构审计进一步确认：后续 BotC Script Catalog、ClocktowerFlowPlanner、狼人杀 Board/RoleRegistry 必须在 revision-driven dynamic decision engine 进入新的 production 实施前建立，否则 R6 会继续固化 Trouble Brewing / fixed JudgeStep 假设。但该工作同样不能插入当前 R2–R5 correctness remediation。

因此当前唯一正确的实施方向是：

```text
Phase A remediation
    ↓
A3 correctness hotfix
    ↓
MainActivity mechanical decomposition
    ↓
A2/A3 validation contract hardening
    ↓
A4.5 lifecycle hardening
    ↓
A3/A4/A4.5 regression + device gate review
    ↓
Phase A final exit review
    ↓
R5.5 Script & Dynamic Flow Foundation
    ↓
ONLY THEN unlock revision-driven dynamic decision plan / Phase B production implementation
```

**当前执行点（2026-08-20）：R1、R2、R3 已通过；R4.1 cache invariant / telemetry 与 R4.2 durable-before-build / coroutine cancellation 已通过；当前进入 R4.3 — revision/session lifecycle invalidation。**

**在 Phase A 重新退出前，不开始新的 B1/B2/B3 功能扩展，也不把 B4 shadow 变成生产依赖；R5.5 也只能作为未来架构约束，不能提前进入 R2–R5。**

## 2. 阶段状态

| 阶段 | 当前状态 | 说明 |
|---|---|---|
| A0 外部参考冻结 | PASS | 冻结参考仍有效；旧文档中的权威顺序文字需要以后统一，但不阻塞修复。 |
| A1 Unified Semantic Model | PASS | storyteller truth / observation / player knowledge 三层边界保留。 |
| A1.1 Semantic Hardening | PASS WITH FOLLOW-UP | schema-v2、registration interaction binding、world-set identity 基本成立；B4 前需补时间线身份细节。 |
| A2 ASP Oracle harness | PASS / R3.1 | nested `FormalGameState` 已迁移到 schema-v2；Python harness 与 Android typed decoder 共用 v2 合同，schema-v1 fail closed，canonical hash 与真实 Clingo baseline 已重新签署。 |
| A2.1 Golden corpus | PASS / R3.1 | 52 个 official/golden contracts 保留；24 个进入 Clingo baseline、28 个明确 `ORACLE_NOT_APPLICABLE`，baseline 为 18 AGREE / 1 EXPECTED_COVERAGE_GAP / 5 KNOWN_ORACLE_VARIANCE / 28 ORACLE_NOT_APPLICABLE，且 `UNEXPLAINED_MISMATCH=0`、`NOT_RUN=0`。 |
| **A3 EnumeratedWorldSet** | **REOPEN / R1+R3 VALIDATION PASS** | R1 correctness hotfix 与 R3 typed real-enumerator golden path 均已通过；A3 contract validation 已补齐，但整体 re-exit 仍等待 R5 的 A3/A4 differential 与 Phase A exit review。 |
| **MainActivity decomposition** | **PASS / R2 BATCHES 1–10 VALIDATED** | Activity shell、三游戏边界及 Clocktower setup/day/night/host/history 主要职责已机械拆分；最终 read-only structural verifier 与标准 CI 验收通过后关闭 R2。 |
| **A4 ZDD prototype** | **IN PROGRESS** | 仍为 exact shadow/prototype；设备性能门槛未完成，需在 R5 前重新跑 differential/device gate。 |
| **A4.5 observation cache rebuild** | **IN PROGRESS / R4.1+R4.2 PASS** | cache scope invariant、telemetry contract、durable-before-build 与 coroutine cancellation 已修复并通过验证；当前补 revision/session 主动 invalidation 与剩余 acceptance evidence。 |
| **R5.5 Script & Dynamic Flow Foundation** | **FUTURE / BLOCKED** | R5 通过后才实施；规范见 `多剧本多板子与动态游戏流程架构设计_v1.md`。 |
| B1+ / revision-driven production expansion | BLOCKED | 先完成 Phase A final exit，再完成 R5.5。 |

## 3. P0 — 必须先修的 correctness 问题

### P0.1 Poisoned Spy/Recluse 在 Chef/Empath 数字信息中仍可特殊登记

审计发现：`TroubleBrewingWorldObservationEvaluator.registrationMatch()` 会在 Role/Type/Alignment 类查询中检查被登记角色自身的 ability state；但是 `evaluateNumeric()` 使用的 `alignmentOptions()` 原先没有做同样检查。

这会允许：

```text
poisoned Spy     -> Chef / Empath 仍可按 GOOD 登记
poisoned Recluse -> Chef / Empath 仍可按 EVIL 登记
```

修复要求：

1. `alignmentOptions()` 只有在 Spy/Recluse 自身 `AbilityState.FUNCTIONING` 时才能增加 special-registration branch。
2. 保留实际 alignment 分支；中毒只关闭角色能力产生的可选登记，不改变真实身份。
3. 增加 Chef + poisoned Spy、Chef + poisoned Recluse、Empath + poisoned Spy、Empath + poisoned Recluse 回归测试。
4. 将这些组合加入 official golden corpus；外部 Oracle 的已知错误不得覆盖官方规则。
5. 修复后重新跑 Enumerated/ZDD differential；“A3 == A4”不能替代 official/golden 独立验证。

**实施状态（2026-08-19～20）：代码修复、4 组直接单元回归、4 个 machine-readable golden contracts（`TB-MAL-05`–`08`）和 Oracle authority-boundary 测试均已通过。冻结 `pnkfelix/botc-asp` 的 Spy/Recluse misregistration 规则不受 impairment 门控，因此这 4 个场景继续登记为 `KNOWN_ORACLE_VARIANCE`。R3 又补齐 schema-v2 typed fixture 与真实 enumerator golden path；A3 contract validation 已通过，但整体 re-exit 仍等待 R5 A3/A4 differential 与 Phase A exit review。**

### P0.2 A2 fixture schema 与当前 A1 schema-v2 不一致

当前 catalog envelope 是 schema v2，但其中 `formalStates[*].schemaVersion` 仍是旧 v1 shape，Python harness 甚至显式要求该值。

修复要求：

1. 将 A2/A2.1 nested FormalGameState fixture 迁移到当前 schema-v2。
2. fixture 必须能通过当前 typed semantic decoder/adapter，而不是由 Python 维护一套“近似 A1”结构。
3. schema-v1 必须继续 fail closed，不允许为了兼容测试重新静默接受。
4. 更新 canonical hash 和 Oracle baseline report。

**实施状态（2026-08-20 / R3.1 PASS）：所有 nested `FormalGameState` 已迁移到 schema-v2；Python harness 只接受当前 `SCHEMA_VERSION`，并新增 schema-v1 fail-closed regression；Android `A3GoldenContractCatalogTest` 会让每个 fixture 实际经过 `EpistemicSemanticJson.decodeFormalGameState()`。canonical fixture SHA-256 与真实 Clingo baseline 已重新生成并相互校验，`UNEXPLAINED_MISMATCH=0`、`NOT_RUN=0`。source commit：`d683fe383b6c56d74b3206b6c325d6ba3b31bed2`。**

### P0.3 A3 official golden runner 需要真正覆盖 enumerator

当前 A3 golden runner 的多数场景是从 JSON actual assignment 直接创建单个 `EnumeratedWorld`，然后验证 evaluator。它不能证明：

```text
PlayerKnowledgeSnapshot
  -> TroubleBrewingWorldEnumerator
  -> complete possible worlds
  -> official observation contract
```

修复要求：

- 保留 evaluator-level tests；
- 新增至少一条 typed end-to-end golden path，直接经过 `TroubleBrewingWorldEnumerator`；
- setup、Drunk identity alternative、hidden Baron、Poisoner target、red herring、registration-sensitive info 至少有代表性场景贯穿完整路径；
- 只有通过该路径后才能重新签署 A3 exit review。

**实施状态（2026-08-20 / R3.2 PASS）：保留原有 24 条 evaluator-level executable golden contracts，并新增 `A3EndToEndGoldenContractTest.kt`，从 schema-v2 canonical fixture 解码 typed state/role catalog 后直接调用 `TroubleBrewingWorldEnumerator.enumerate()`。代表性路径覆盖 hidden Baron + Drunk identity alternative、Poisoner target / malfunctioning Empath information、Fortune Teller red herring 的 actual-good 资格与 Recluse registration branch，以及 Spy interaction-local registration。focused Enumerated/ZDD/golden suite 与 full Android regression 均通过。source commit：`58574f6dcc094737e5d202ce5facfefc8a6b357b`。**

## 4. P0 — MainActivity 结构性拆分

### P0.S1 为什么在 A4.5 前拆分

当前 `MainActivity.kt` 已同时承担过多不同层次的职责。A4.5 下一步恰好需要修改 persistence、lifecycle cancellation、revision invalidation 和 coroutine wiring，如果继续直接叠加在大文件中，会显著增加以下风险：

- 小范围修改需要整体重写超大文件；
- Clocktower 改动误伤狼人杀/谁是卧底 UI；
- Compose UI 与领域状态/持久化逻辑难以区分；
- lifecycle/persistence 修复与界面重构混杂后难以定位回归来源；
- GitHub connector 对大单体文件的小差异修改不够理想。

因此在 A3 最小 correctness hotfix 之后、A4.5 lifecycle hardening 之前，安排一次**纯结构性 mechanical extraction**。

### P0.S2 第一轮目标结构

第一轮只要求形成清晰职责边界，不追求一次完成最终架构。目标可等价于：

```text
MainActivity.kt
    Android Activity / window / setContent only

CampBoardGameHostApp.kt
    app root / top-level game navigation

undercover/
    UndercoverScreen.kt

werewolf/
    WerewolfHostScreen.kt

clocktower/ui/
    ClocktowerHostScreen.kt
    ClocktowerSetupScreen.kt
    ClocktowerNightScreen.kt
    ClocktowerDayScreen.kt
    ClocktowerHistoryScreen.kt

clocktower/persistence/
    ClocktowerGamePersistence.kt   (仅在可机械抽取且不改变语义时)
```

实际文件名可按现有代码边界调整。第一轮目标约为 8–12 个职责明确的文件，而不是拆成大量几十行的小文件。

### P0.S3 第一轮严格禁止事项

本阶段是 **behavior-preserving structural refactor**。禁止同时做以下改动：

- 不修 Clocktower 规则行为；
- 不改变 `gameStateRevision` / `playerInputRevision` 增加时机；
- 不改变 observation append/persistence 顺序；
- 不改变 recommendation key、cache key 或 hash；
- 不改变 A4/A4.5 rollout；
- 不引入新的 ViewModel/Redux/MVI 状态架构；
- 不把 Compose mutable state 迁移为新的领域状态模型；
- 不改持久化 schema；
- 不顺便清理业务逻辑或重命名大批领域概念；
- 不改变任何用户可见流程、文案或推荐结果。

允许的操作主要是：

- 移动顶层/Composable/helper function 到职责明确的新 `.kt` 文件；
- 机械调整 visibility/import；
- 为了拆文件添加最小参数传递或轻量参数容器，但不得改变状态拥有者；
- 抽离无状态 UI helper；
- 抽离已经是纯函数的 formatting/mapping helper。

### P0.S4 为什么暂时不同时引入 ViewModel

长期目标仍然应该从：

```text
Compose mutable state + callbacks
```

演进为：

```text
Compose UI
  -> controller / ViewModel
  -> immutable session/domain state
  -> rules / recommendation / epistemic engine
```

但这属于第二阶段架构重构，不与本次文件拆分混做。先完成零行为变化的 mechanical extraction，后续才能更安全地判断哪些状态真正应该进入 `ClocktowerSessionController` / ViewModel。

## 5. P0 — A4.5 合同修复

A4.5 的 complete-log rebuild、recipient isolation、sequential build、stale commit、OOM != UNSAT 等设计保留，但以下合同必须补齐。

### P0.4 Durable-before-build

原 spec 要求 observation 已经 durable append 后才能捕获 rebuild request。当前 UI 先更新 Compose 内存状态并捕获 request，SharedPreferences persistence 随后由 `SideEffect` 完成。

要求调整为明确顺序：

```text
append / deduplicate observation
  -> persist active game state successfully
  -> bump/capture revisions consistently
  -> capture immutable rebuild request
  -> Dispatchers.Default execute
```

如果持久化失败，不得把该 observation 当作可重放的 cache rebuild 基础。

**实施状态（R4.2 PASS）：新增 `A4ObservationDurabilityGate`；observation append 只标记 pending，只有 `SharedPreferences.commit()` 返回成功后才释放 record ID 并捕获 immutable rebuild request。持久化失败保持 pending，不生成 rebuild。source commit：`7dc1a8a5afd69ed1b0f87406c71d84adbdf602cb`。**

### P0.5 Coroutine/lifecycle cancellation 必须进入 executor

`A4ObservationCacheRebuildExecutor.execute()` 已支持 `isCancelled`，但真实 UI 调用原先没有传入 coroutine/lifecycle cancellation。

要求：

- UI worker 将 coroutine active/cancel state 映射到 `isCancelled`；
- 离开 game、restart、role reassignment、player-count 改变或 revision supersede 时主动使旧 generation 失效；
- 已经运行的 exact build 可以结束，但结果只能成为 `STALE/CANCELLED`，不得继续写为 current cache；
- 不依赖“恰好会启动下一次 rebuild”来使旧 generation 失效。

**实施状态：R4.2 已把 coroutine active state 传入 executor，并在 exact build 返回后、`commitIfCurrent()` 前再次检查 cancellation，因此中途取消的已完成 build 不会 publish cache。R4.3 继续补 leave/restart/revision/session 主动 invalidation。**

### P0.6 Cache generation invariant 加固

`commitIfCurrent()` 除了 gameId/current generation，还应验证 key 与 scope 的结构一致性，至少包括：

- `gameStateRevision`；
- `formalSnapshotId`；
- `rollout`；
- 其他属于 generation scope 且能从 key 验证的字段。

builder 返回的 recipient、knowledge ID、hypothesis、world-set identity 校验继续保留。

**实施状态（R4.1 PASS）：`commitIfCurrent()` 已原子核对 `gameId`、`gameStateRevision`、`formalSnapshotId`、`rollout` 与 current generation，scope mismatch fail closed，并有直接回归测试。**

### P0.7 Heap telemetry 命名/实现一致

A4.5 report 原字段名为 `coarseMaxHeapDeltaBytes`，但 executor 只计算结束时 heap 与开始时 heap 的差值。

二选一：

- 真正在阶段间采样并维护 coarse maximum；或
- 改名为 `coarseEndHeapDeltaBytes`，避免把 end delta 当 peak。

性能数据不得作为 retained-size 结论。

**实施状态（R4.1 PASS）：observation rebuild report 已改名为 `coarseEndHeapDeltaBytes`，与当前 end-start 实现一致；不再把该数据表述为 peak/retained size。**

### P0.8 补齐直接 acceptance tests

需要确保至少有直接 executor/integration 证据覆盖：

- wrong recipient / knowledge / hypothesis / identity -> FAILED, no write；
- production recommendation 在 shadow cache ready 与 absent 两种情况下结果完全一致；
- leave/restart/revision cancellation 的真实 wiring；
- durable-before-build 顺序。

当前 R4.1/R4.2 已补 cache scope mismatch、durability failure、build-completion cancellation 等直接证据；R4.3 补真实 revision/session invalidation，之后再做 R4 final acceptance sweep。

## 6. P1 — Phase B/B4 前必须解决的语义债务

这些问题不阻止当前 P0 修复，但在正式多夜 Possible Worlds 前必须完成。

### P1.1 Spy Grimoire reminder tokens

`GrimoireState` schema 已包含 reminder tokens，但 world evaluator 当前主要验证 displayed role/alive。正式使用 Spy perspective 前必须决定哪些 reminder token 属于机械 truth，并让 world filtering 与 schema 承诺一致。

### P1.2 Observation timeline identity

当前多处 canonical order 主要按：

```text
round -> sequence -> id
```

进入多 phase/multi-night 后必须有唯一明确的全局顺序；不能让不同 phase 的相同 sequence 最后由 recordId 决定历史顺序。

在 B4 前统一：

- phase order；
- round；
- global/monotonic sequence 或等价 TimelinePoint；
- knowledge identity 中哪些时间字段必须参与 hash。

### P1.3 FormalGameState actual truth 与 structural world-builder seam

`FormalGameState` 规范上代表说书人真实世界，包含真实 poisoned status；但 A4 UI seam 为避免泄露 player-world secret，目前构造 structural snapshot 时会隐藏实际 poison target。

正式 Phase B 前需要把这两个概念拆清楚：

```text
Actual FormalGameState (storyteller truth)
vs
Player-world construction input (knowledge-safe structural facts)
```

不能靠把真实字段传 `null` 来长期表达两种不同语义。

## 7. 实施批次

### R1 — A3 registration correctness

范围：P0.1 + golden regression。

当前状态：**PASS**。Android unit tests、debug APK、ASP contract tests 与真实 Clingo cross-validation 均已有通过证据；R3 validation contract 现已补齐，A3 overall re-exit 仍待 R5 differential / Phase A review。

已实施：

- `alignmentOptions()` 对被登记角色的 `AbilityState` fail closed；
- 直接单元测试覆盖 Chef/Empath × poisoned Spy/Recluse 四组合；
- machine-readable catalog 从 48 扩充为 52，新增 `TB-MAL-05`–`08`；
- A3 executable golden contracts 从 20 扩充为 24；
- 冻结 ASP Oracle 的 impairment/misregistration 差异登记为 `KNOWN_ORACLE_VARIANCE`；
- Python harness 增加 authority-boundary regression，防止以后误把外部 Oracle 提升为规则权威。

R3 已补充验证：

- focused `EnumeratedWorldSetTest` / `A3GoldenContractCatalogTest` 已实际运行；
- Python oracle harness tests 已通过；
- real Clingo cross-validation 已重新签署 52-case catalog；
- full JVM regression 与 debug APK build 已通过；
- 仍待 R5 执行 A3/A4 differential 与 Phase A re-exit。

退出条件：

- poisoned Spy/Recluse numeric registration 回归全部通过；
- official expectation 明确；
- A3/A4 differential 无新差异；
- 无 cap/OOM 被翻译为 UNSAT。

### R2 — MainActivity mechanical decomposition

范围：P0.S1–P0.S4。

当前状态：**PASS / BATCHES 1–10 VALIDATED**。

已完成并通过 CI：

1. `WerewolfGameSupport.kt`：狼人杀纯模型/规则 helper 机械抽取；
2. `MainActivity.kt`：缩减为 Android Activity / immersive mode / `setContent` 壳；`CampBoardGameHostApp.kt` 保留 app root；
3. `undercover/UndercoverSupport.kt`：词库、选词 helper 与 Undercover 设置 UI；
4. `werewolf/WerewolfHostScreen.kt`：Werewolf 设置页与 Judge UI；
5. `clocktower/ui/ClocktowerSetupScreen.kt`：pre-game setup UI；
6. `clocktower/ui/ClocktowerDayScreen.kt`：day overview、dawn summary、nomination、vote、execution confirmation 与特殊白天行动 UI；
7. `clocktower/ui/ClocktowerNightScreen.kt`：建立 night presentation seam，迁移 callback-driven 的 `ClocktowerNightActiveScreen`；
8. `clocktower/ui/ClocktowerNightScreen.kt`：继续迁移纯展示的 `ClocktowerNightReadyCard`；
9. `clocktower/ui/ClocktowerHostScreen.kt`：一次机械迁移完整 Host/Judge + night/recommendation helper 连续区块；`ClocktowerJudgeScreen` 的 state ownership、revision/recommendation/A4 wiring 保持原语义，仅跨文件迁移并放宽必要 visibility。source diff 仅涉及 root 与新 Host 文件；Android、ASP、真实 Clingo 均通过；
10. `clocktower/ui/ClocktowerHistoryScreen.kt`：机械迁移 Clocktower game record/timeline/player status 与 result dialog/player-row 两个 history/result 区块；增加 fast `compileDebugKotlin` seam gate 后通过完整 Android、ASP 与真实 Clingo 验收。

Batch 1–10 均保持 behavior-preserving：没有改变 Clocktower 规则分支、revision 增加时机、observation/persistence 顺序、recommendation/cache/hash 语义，也没有引入新的 ViewModel/MVI/Redux 状态架构。Batch 6–10 的 guarded extraction 使用 untracked-aware scope、单 EOF、`bash ./gradlew` 与 staged-scope commit guard，相关流程经验记录在 `docs/github_connector_large_file_editing_playbook.md`。

R2 第一轮拆分现在关闭。最终结构已经形成明确的 setup / day / night / host / history 文件边界；`ClocktowerLandingScreen`、发牌 handoff/reveal 以及新恶魔确认等少量顶层壳 UI 留在 app root 不构成 R2 blocker，因为它们不承载 Judge state owner、recommendation engine、day flow 或 history/result ownership。继续为了“root 中零 Clocktower composable”而拆分会超出本轮风险收益目标。

退出证据：

- `MainActivity.kt` 已是轻量 Activity/window/setContent 壳；
- 三种游戏模式入口路径保持不变；
- Clocktower setup/day/night/host/history 主要职责已有独立文件；
- Batch 9 source commit 仅 root + `ClocktowerHostScreen.kt`，Batch 10 source commit 仅 root + `ClocktowerHistoryScreen.kt`；
- focused/full Android tests 与 debug APK build 通过；
- ASP contract tests 与真实 Clingo cross-validation 通过；
- `git diff --check` 与 staged-scope guards 通过；
- 最终 read-only `R2 main-thread boundary` verifier 固化主要结构合同；
- 临时 Actions write executor 已撤销，workflow 恢复 `contents: read`。

**R2 已关闭，不再作为当前 blocker；R3 validation contract 现已通过，下一步进入 R4。** ViewModel/SessionController/immutable state ownership 重构属于后续独立架构工作，不回填到 R2。

### R3 — A2/A3 validation contract

范围：P0.2 + P0.3。

当前状态：**PASS**。R3.1 与 R3.2 均已由 guarded writer、focused tests、full Android regression 和 clean read-only CI 验收。

R3.1 — schema contract：

- nested `FormalGameState.schemaVersion` 全部由 v1 迁移到 v2；
- Python harness 使用当前 `SCHEMA_VERSION`，schema-v1 明确 fail closed；
- Android catalog loader 让所有 nested state 经过 `EpistemicSemanticJson.decodeFormalGameState()`；
- canonical catalog hash 与真实 Clingo report hash 相互校验；
- baseline：18 AGREE / 1 EXPECTED_COVERAGE_GAP / 5 KNOWN_ORACLE_VARIANCE / 28 ORACLE_NOT_APPLICABLE；
- `UNEXPLAINED_MISMATCH=0`，`NOT_RUN=0`。

R3.2 — real enumerator golden path：

- 保留 24 条 evaluator-level executable golden contracts；
- 新增独立 `A3EndToEndGoldenContractTest.kt`，真实调用 `TroubleBrewingWorldEnumerator.enumerate()`；
- 覆盖 hidden Baron / Drunk、Poisoner target、red herring actual-alignment qualification、Spy/Recluse registration-sensitive information；
- focused Enumerated/ZDD/golden tests 与 full JVM/debug APK regression 通过。

退出证据：

- R3.1 source `d683fe383b6c56d74b3206b6c325d6ba3b31bed2`；
- R3.2 source `58574f6dcc094737e5d202ce5facfefc8a6b357b`；
- clean read-only CI #96：Android、ASP contract、真实 Clingo 全部 success；
- 临时 R3 writer 已撤销，`.github/workflows/ci.yml` 恢复 `contents: read`。

**R3 已关闭。下一步正式进入 R4 — A4.5 lifecycle hardening。A3 contract validation 已满足，但 A3 overall re-exit 仍由 R5 differential / Phase A exit review 签署。**

### R4 — A4.5 lifecycle hardening

范围：P0.4–P0.8。

当前状态：**IN PROGRESS — R4.1 PASS / R4.2 PASS / R4.3 ACTIVE**。

已完成：

- **R4.1 — cache invariant + telemetry contract**
  - `commitIfCurrent()` 原子核对 `gameId / gameStateRevision / formalSnapshotId / rollout / current generation`；
  - scope mismatch fail closed；
  - observation rebuild telemetry 改为 `coarseEndHeapDeltaBytes`；
  - source commit `46f17b7b99eec5a2178560429eb216655505d3b5`；
  - clean-head CI 与 structural verifier 通过。
- **R4.2 — durable-before-build + coroutine cancellation**
  - 新增 `A4ObservationDurabilityGate`；
  - persistence success 后才释放 observation rebuild request；
  - persistence failure 不释放 pending observation；
  - coroutine active/cancel state 进入 executor；
  - build 返回后、cache publication 前再次检查 cancellation；
  - source commit `7dc1a8a5afd69ed1b0f87406c71d84adbdf602cb`；
  - compile、focused tests、full Android、ASP、真实 Clingo 全部通过；
  - temporary writer/trigger 已撤销，PR base 已恢复 `main`。

当前 R4.3：

- revision supersede 必须同步失效当前 game 的 A4 shadow generation；
- leave/restart/restore 必须同步 invalidate cache、clear durability pending、cancel observation rebuild request；
- 同一 session 的 revision bump **不得**清 durability pending，否则刚 append、等待 durable commit 的 observation 会丢失；
- `gameStateRevision` / `playerInputRevision` 的原有增加时机保持不变，只把同步 invalidation 收口到 helper；
- in-flight exact build 可以结束，但旧 generation 已失效，不能 publish current cache。

R4 退出条件：

- durable-before-build 可测试；
- leave/restart/revision 可立即失效旧 generation；
- cache scope invariant fail closed；
- OOM/failure/cancel/stale 均不产生 empty logical result/UNSAT；
- production recommendation 完全不读取 shadow cache；
- R4 final acceptance sweep 通过。

开发操作策略（R4.2 后更新）：

- 默认：read-only audit → **Git Data API 原子 source commit** → normal PR CI → exact diff audit；
- temporary trusted writer 只用于超大文件且 connector 无法安全构造完整内容的机械 patch；
- 已确认 `pull_request` workflow 由 base branch 上的 workflow 定义控制，修改已有 base workflow 不自动产生新的 PR event，同-tree/no-op commit 不是可靠 trigger；
- 详细规则见 `docs/github_connector_large_file_editing_playbook.md`。

### R5 — Phase A re-exit

执行：

1. focused epistemic tests；
2. full `testDebugUnitTest`；
3. Python ASP Oracle tests + real Clingo baseline；
4. `git diff --check`；
5. A4 target-device measurements（仅在 correctness 通过后）；
6. 新建一份 Phase A exit review，旧 A3 PASS 不恢复原文件。

A4 仍只有在目标设备 correctness、latency、memory 和 degradation gates 全部满足后，才可以讨论 `ZDD_DEVICE_VALIDATED`。

### R5.5 — Script & Dynamic Flow Foundation

**状态：FUTURE / BLOCKED BY R5。** 详细规范：`docs/多剧本多板子与动态游戏流程架构设计_v1.md`。

目标：在不重新设计 Possible Worlds 的前提下，建立多剧本/多板子的内容身份、角色注册和动态流程上游，使 R6 不再依赖 Trouble Brewing enum、固定夜序或 Werewolf fixed JudgeStep。

按以下小批次实施：

```text
S0 Schema / Catalog / official-custom JSON normalization / validation
S1 Trouble Brewing FlowPlanner golden-equivalent migration
S2 No Greater Joy second-script structural proof
S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration
S4 persistence/ruleset identity migration
S5 full regression + legacy flow removal + R6 handoff
```

必须保持：

- Script/Board 只组合角色，复杂规则留在 Kotlin handler/domain；
- 不构建通用规则 JSON DSL；
- ClocktowerFlowPlanner 与 WerewolfFlowPlanner 分离；
- `VERIFIED / PARTIAL / UNVERIFIED` 决定 custom/homebrew 自动化安全等级；
- TB legacy flow 只有在 shadow/golden parity 后才能移除；
- 新增只由已有角色组成的 script/board 不需要修改 Host UI 或 flow core；
- R6 的 decision point 必须来自 script-aware FlowPlanner/HostInteraction seam。

R5.5 不要求实现 BMR/S&V 全部角色；内容扩展在 foundation 和 R6 script-aware seam 稳定后进行。

### R6 — Unlock revision-driven dynamic decision engine

只有 R5 **以及 R5.5** 通过后，`storyteller_revision_driven_dynamic_decision_engine_plan.md` 才从 `BLOCKED` 变为 `READY`。

开始前重新核对该计划的输入是否仍与最新 `GameSnapshot`、revision、observation log、PlayerWorldSet seam，以及 `ClocktowerFlowPlanner -> HostInteraction/StorytellerDecisionPoint` seam 一致；若 Phase A 或 R5.5 改变公共语义，先更新计划再写代码。

R6 不得重新：

- 按 Trouble Brewing enum/role-name `when` 生成流程；
- 在 Compose UI 中决定“下一个角色是谁”；
- 把 `nightOrderPosition` 当成流程定义的唯一事实；
- 为狼人杀恢复固定 `WerewolfJudgeStep` 扩展模式。

## 8. Phase A 最终退出条件

必须同时满足：

- A3 known correctness defect 已修复且有 official/golden regression；
- MainActivity 第一轮 mechanical decomposition 已完成且行为零变化；
- A2 fixture 使用当前 schema-v2；
- A3 至少有代表性 official golden 端到端经过真实 enumerator；
- 所有已运行 Oracle 差异已分类，无 unexplained mismatch；
- A4 ZDD 与修复后的 A3 在适用 exact 场景保持一致；
- A4.5 durability/cancellation/cache identity contract 通过；
- shadow cache 不影响 production recommendation；
- 资源失败不被解释为 UNSAT；
- 多夜尚未支持的边界明确 defer，而不是 false UNSAT；
- 完整回归通过；
- 若要推广 ZDD，目标设备 gate 另外通过。

**R5 退出只解锁 R5.5，不再直接解锁 R6。**

## 9. 生产保护线

在本路线另行更新前：

```text
Production recommendation engine: existing production path
A3 enumerator: correctness/debug baseline only
A4 ZDD: shadow/prototype only
A4.5 cache: debug/shadow only
B4 DynamicPlayerWorldSetShadow: isolated shadow only
R5.5 multi-script/board flow: design-only until R5 passes
```

任何性能优化或结构重构都不能：

- 截断 exact worlds 后仍声称 exact；
- 把 timeout/OOM/cap 当 UNSAT；
- 省略 Spy/Recluse/Drunk/Poisoner/red-herring 的规则分支；
- 把 storyteller-only truth 放入普通玩家知识；
- 让 background result 覆盖已经展示/提交的决定；
- 借“拆文件”名义改变 revision、persistence 或 recommendation 语义；
- 借“为多剧本做准备”名义在 R2–R5 中改写当前 flow/persistence 行为。

## 10. 文档状态维护

后续只在本文更新阶段状态。完成 R1/R2/R3/R4/R5/R5.5 时不要再创建新的并列“最终路线”。专项实现细节可以追加到对应 spec/status log，但“下一步做什么”始终回到本文。
