# A4.5 Observation cache rebuild executor — Terra implementation contract

> Status: READY FOR IMPLEMENTATION  
> Date: 2026-08-15  
> Parent specification: `CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> Current implementation status: `storyteller_a4_zdd_prototype.md`  
> Rollout boundary: Debug + five-player Trouble Brewing + `ZDD_SHADOW` only
> Later dynamic lifecycle plan: `storyteller_revision_driven_dynamic_decision_engine_plan.md`; A4.5 must remain shadow and must not implement that plan's production wiring.

## 1. One-sentence objective

当一条已持久化的首夜 observation 被追加后，从完整不可变 observation log 为受影响的接收者
重建新的 ZDD shadow，只在所有版本与身份校验仍然成立时写入新 cache key。

本切片不优化 `PlayerWorldSet.require()`，不接入生产推荐，不实现 B4 跨夜 timeline。

## 2. Current facts that implementation must preserve

1. A3 `EnumeratedWorldSet` 仍是 exact correctness baseline。
2. `A4IdentityRevealPrewarmCoordinator` 的 cache 只服务 shadow 遥测；任何结果都不得改变说书人选择。
3. `A4PlayerKnowledgeFactory` 必须从完整 `EpistemicObservationLog` 重放，不得只应用最后一条记录。
4. `TroubleBrewingWorldEnumerator` 目前只支持 `FIRST_NIGHT` + round 1。Day、death、execution
   和跨夜状态必须等待 B4。
5. OOM、取消、过期、超时、不支持的 timeline 都是运行状态，绝不是 `UNSAT`。
6. 新 observation 到达后，旧 world set 的 `knowledgeSnapshotId` 不能被修改，也不能直接换 key。
7. 现有 `NATIVE_FILTER` 只是诊断分类。A4.5 对所有受影响 seat 都执行完整-log rebuild，
   不得根据该分类写回旧 filtered value。

## 3. Explicit non-goals

本切片禁止：

- 将 rollout 改为 `ZDD_DEVICE_VALIDATED`；
- 让 setup/night/day recommendation 读取 shadow cache；
- 新增 enumeration cap、sampling 或 approximate SAT；
- 实现 death/execution/poison duration/role transition timeline；
- 为了速度省略 Spy、Recluse、Drunk、Poisoner 或 red-herring 语义；
- 用 `require(oldObservation)` 的结果伪装成新 knowledge identity；
- 解析 UI 本地化文案来构造 proposition；
- 生成 APK、切换分支、stage、commit 或清理用户的 dirty worktree。

## 4. Required execution order

### A4.5.0 — Cache identity hardening

在实现 executor 前先完成。

`A4IdentityRevealPrewarmCacheKey` 必须能区分不同 hypothesis。推荐将下列字段加入 key：

```kotlin
val worldSetIdentity: PlayerWorldSetIdentity
```

它已经由 ruleset、knowledge snapshot、recipient 和 hypothesis 稳定派生，比只添加
`hypothesis` 更难遗漏身份边界。`cacheKey()` 必须用
`PlayerWorldSetIdentity.create(formal.rulesetRef, knowledge, hypothesis)` 构造它。

`playerInputRevision` 是运行时 generation，不是单个 recipient world-set 的 value identity。它必须从
cache key 移入下文的 `A4ShadowCacheScope`。否则一条只发给 seat 2 的私密信息会让所有
seat 的 key 同时失效，违反 recipient isolation。目标 key 语义为：

```kotlin
data class A4IdentityRevealPrewarmCacheKey(
    val gameId: String,
    val gameStateRevision: Long,
    val formalSnapshotId: String,
    val recipientSeat: Int,
    val knowledgeSnapshotId: String,
    val worldSetIdentity: PlayerWorldSetIdentity,
    val rollout: A4WorldEngineRollout,
)
```

builder 返回后必须同时校验：

```text
result.recipientSeat == expected recipient
result.knowledgeSnapshotId == expected knowledgeSnapshotId
result.hypothesis == request.hypothesis
result.identity == key.worldSetIdentity
```

任一不一致都记录 `FAILED/IDENTITY_MISMATCH`，不得写 cache。

### A4.5.1 — Shared shadow cache store

将 prewarm 和 rebuild 需要的最小 cache 操作收敛到一个共享 store。命名可等价，但必须
提供以下语义：

```kotlin
interface A4ShadowWorldSetCache {
    fun read(key: A4IdentityRevealPrewarmCacheKey): PlayerWorldSet?
    fun beginVersion(scope: A4ShadowCacheScope): A4ShadowCacheGeneration
    fun commitIfCurrent(
        generation: A4ShadowCacheGeneration,
        key: A4IdentityRevealPrewarmCacheKey,
        value: PlayerWorldSet,
    ): Boolean
    fun invalidateGame(gameId: String)
}
```

scope 必须精确包含：

```kotlin
data class A4ShadowCacheScope(
    val gameId: String,
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val formalSnapshotId: String,
    val rollout: A4WorldEngineRollout,
)
```

generation 控制“这次计算还能不能写”；key 控制“这个 value 属于哪个玩家知识世界”。
两者不得混用。

`commitIfCurrent` 是必需的 atomic check-and-write；不允许先检查 revision，然后在另一个锁区间写入。
新 game/revision/input revision 开始后，旧 generation 的 commit 必须返回 `false`。
`beginVersion` 不得仅因 `playerInputRevision` 增加就删除已存 value；未受私密观察影响的
recipient 仍具有相同 exact key。新 game 必须清除旧 game values；formal revision 不同的
values 不得被新 key 命中。

不得把 store 设计成 Android/Compose 类；它必须是 JVM unit test 可独立验证的 Kotlin 类。

### A4.5.2 — Rebuild request and outcome contract

建议请求对象：

```kotlin
data class A4ObservationCacheRebuildRequest(
    val formal: FormalGameState,
    val playerInputRevision: Long,
    val perceivedRolesBySeat: Map<Int, RoleId>,
    val observationLog: EpistemicObservationLog,
    val appendedRecordId: String,
    val hypothesis: EpistemicHypothesis,
    val roleDefinitions: Collection<RoleDefinition>,
    val rollout: A4WorldEngineRollout = A4WorldEngineRollout.ZDD_SHADOW,
)
```

不建议由 UI 直接传入 `knowledgeBySeat`。Executor 应在 worker 中调用
`A4PlayerKnowledgeFactory.createAll(formal, perceivedRolesBySeat, observationLog)`，以免 UI 传入与 log
不一致的快照。

请求初始化或执行前必须校验：

- rollout 只能为 `ZDD_SHADOW`；
- `playerInputRevision >= 0`；
- perceived-role seats 与 formal seats 完全一致；
- `appendedRecordId` 在 log 中恰好存在一次；
- record 引用的 seat 都存在；
- formal game/ruleset 与当前 cache scope 一致。

每个 report entry 的终态必须是下列之一（命名可等价，语义不可合并）：

```text
READY_REBUILT       从完整 log 成功重建并写入新 key
READY_REUSED        未受影响，新旧 exact cache key 完全相同
MISSING_REBUILT     没有旧 cache，但从完整 log 成功重建
MISSING_UNAFFECTED  未受影响且本来就没有 cache；不为它触发构建
DEFERRED_B4         非 FIRST_NIGHT/round 1，未调用 builder
CANCELLED           尚未开始的工作被取消
STALE               已完成的结果因 generation 变化被丢弃
FAILED              Exception/identity mismatch，没有写 cache
RESOURCE_EXHAUSTED  OutOfMemoryError，没有伪造 cardinality，停止后续 queue
```

`MISSING_REBUILT` 不是 UNSAT，也不是一个空 world set。

### A4.5.3 — Exact rebuild algorithm

必须按以下顺序执行：

```text
1. Validate request and locate appended record.
2. If formal.phase != FIRST_NIGHT or formal.round != 1:
      emit DEFERRED_B4 for affected seats; call no world builder; return.
3. Derive previousLog by removing exactly appendedRecordId from the complete durable log.
4. Bind previousLog and the complete new log to the exact same formal snapshot. Build before/after
   PlayerKnowledgeSnapshot values with A4PlayerKnowledgeFactory.
5. Compute affected seats from record visibility:
      PUBLIC  -> every formal seat
      PRIVATE -> exactly record.recipientSeats
6. Begin/capture one cache generation for this request.
7. Process affected seats sequentially on one worker:
      a. derive the exact new cache key;
      b. derive/read the exact before key only to distinguish READY_REBUILT from MISSING_REBUILT;
      c. build through A4PlayerWorldSetRuntime(ZDD_SHADOW);
      d. retain only zddShadow;
      e. validate recipient, knowledge ID, hypothesis and world-set identity;
      f. commitIfCurrent; false means STALE.
8. For unaffected seats, require beforeKey == afterKey. A mismatch is
   FAILED/UNAFFECTED_IDENTITY_CHANGED and exposes a recipient-isolation bug; do not silently rebuild.
   Reuse only if the exact key is present, otherwise report MISSING_UNAFFECTED without building.
   Because playerInputRevision belongs to the generation rather than the value key, a private record
   does not invalidate unrelated recipients. No old value is ever aliased under a different key.
9. Emit one deterministic report after all entries reach a terminal state.
```

不允许并行构建多个 seat。五人预热已证明串行可控内存；本切片不重新打开并发设计。

### A4.5.4 — UI wiring boundary

只在以下条件全部成立时调度 executor：

```text
BuildConfig.DEBUG
currentGameKind == Clocktower
script == TroubleBrewing
playerCount == 5
rollout == ZDD_SHADOW
observation has already been durably appended
```

调度点在 `recordEpistemicObservation` 成功去重并追加 log 之后。必须先持久化/更新
revision，再捕获不可变 request，最后在 `Dispatchers.Default` 运行。UI 不得等待结果。

首个真实接入范围只包含首夜已有结构化 proposition 的私密展示。Day/death/execution
记录可以进入 durable log，但 executor 必须返回 `DEFERRED_B4` 而不构建。

离开当前 game、restart、role reassignment、player-count 变化或更新 revision 时，旧 generation
必须立即失效。已在运行的 exact build 可以结束，但只能记录 `STALE`。

## 5. File-level change budget

预期只修改/新增：

```text
app/src/main/java/.../epistemic/A4IdentityRevealPrewarmCoordinator.kt
app/src/main/java/.../epistemic/A4ObservationCacheUpdateCoordinator.kt
app/src/main/java/.../epistemic/A4ShadowWorldSetCache.kt             (new, if extracted)
app/src/main/java/.../MainActivity.kt                                 (debug wiring only)
app/src/test/java/.../epistemic/A4IdentityRevealPrewarmCoordinatorTest.kt
app/src/test/java/.../epistemic/A4ObservationCacheUpdateCoordinatorTest.kt
app/src/test/java/.../epistemic/A4ShadowWorldSetCacheTest.kt           (new, if extracted)
docs/archive/deferred/storyteller_a4_zdd_prototype.md
```

如果必须修改 `PlayerWorldSet`、`ZddPlayerWorldSet`、setup recommendation 或 domain schema，应停止并先记录
设计变更；这表明实现已超出 A4.5 范围。

## 6. Mandatory test matrix

| ID | Scenario | Required assertion |
|---|---|---|
| K1 | same structural fields, different hypothesis | cache keys and world-set identities differ |
| K2 | builder returns wrong recipient/knowledge/hypothesis/identity | `FAILED`, no cache write |
| C1 | private record for seat 2 | only seat 2 rebuilds; other identities do not change |
| C2 | public first-night record | every seat rebuilds, strictly sequential |
| C3 | two prior records plus appended record | rebuilt knowledge contains all three in canonical order |
| C4 | affected seat has no old cache | `MISSING_REBUILT`, valid non-null cardinality |
| C5 | unaffected seat identity exactly matches | same instance is reused under the same exact key |
| C6 | unaffected seat identity differs | invariant failure; no reuse, rebuild or cache write |
| V1 | newer revision starts during build | completed old result is `STALE` and absent from new key |
| V2 | cancel before next seat | queued entries are `CANCELLED` |
| E1 | ordinary builder exception | `FAILED`, null cardinality, remaining seats may continue |
| E2 | synthetic OOM | `RESOURCE_EXHAUSTED`, null cardinality, queue stops |
| B1 | Day/death record | `DEFERRED_B4`, builder invocation count is zero |
| R1 | deterministic report | entries sorted by seat and stable log format |
| U1 | production isolation | recommendation result is unchanged whether cache is ready or absent |

测试不得只断言 status；还要断言新 key、`PlayerWorldSet.identity`、cache 中的实例和 builder
调用次数。

## 7. Telemetry contract

日志前缀保持：

```text
A4_OBSERVATION_CACHE_UPDATE
```

每次 report 至少包含：

```text
gameId=<id>
gameStateRevision=<n>
playerInputRevision=<n>
recordId=<id>
phase=<phase>
round=<n>
affected=<comma-separated seats>
status=<seat:terminal-status,...>
totalBuildMs=<n>
coarseMaxHeapDeltaBytes=<n>
```

日志不得包含真实角色表、poison target、red herring 或私密 proposition 内容。

## 8. Terra-sized implementation batches

每批必须独立通过 focused tests 后再进入下一批：

1. **Batch 0 — identity only**  
   加固 cache key 和 builder identity checks；补 K1/K2；不加 executor。
2. **Batch 1 — store and pure executor**  
   实现 atomic generation/store、完整 log rebuild 和 terminal outcomes；补 C1–C6。
3. **Batch 2 — cancellation and failure**  
   实现 V1/V2/E1/E2/B1；保证所有资源失败不生成 cardinality。
4. **Batch 3 — debug UI wiring**  
   只接首夜 shadow；补 R1/U1；不改 recommendation consumer。
5. **Batch 4 — device observation**  
   用五人 Trouble Brewing 记录至少一条 private update 和一条 `DEFERRED_B4`；
   评估帧、heap 与构建时间，不在本批改 rollout。

## 9. Required verification commands

```zsh
./gradlew testDebugUnitTest --no-daemon \
  --tests "com.codex.campboardgamehost.clocktower.epistemic.A4IdentityRevealPrewarmCoordinatorTest" \
  --tests "com.codex.campboardgamehost.clocktower.epistemic.A4ObservationCacheUpdateCoordinatorTest" \
  --tests "com.codex.campboardgamehost.clocktower.epistemic.A4ShadowWorldSetCacheTest"

./gradlew testDebugUnitTest --no-daemon \
  --tests "com.codex.campboardgamehost.clocktower.epistemic.A4*" \
  --tests "com.codex.campboardgamehost.clocktower.epistemic.EnumeratedWorldSetTest" \
  --tests "com.codex.campboardgamehost.clocktower.epistemic.ZddPlayerWorldSetTest" \
  --tests "com.codex.campboardgamehost.clocktower.session.ClocktowerGameSessionTest"

./gradlew testDebugUnitTest --no-daemon
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
git diff --check
```

如果没有新建 `A4ShadowWorldSetCacheTest`，第一条命令中应删除该 filter，不得伪造空测试类。

## 10. Definition of done

只有同时满足以下条件才可标记 A4.5 完成：

- K1–U1 全部有自动化测试并通过；
- 所有 cache commit 都是 generation-bound atomic commit；
- 受影响 seat 均从完整 durable log 重建；
- 非首夜 observation 只会产生 `DEFERRED_B4`；
- 错误/OOM/取消/过期没有一种会产生空 world set 或 UNSAT；
- 真实 UI 线程不等待 executor；
- production recommendation 与 selector 没有读取 shadow cache；
- focused、A4 regression、full JVM、Oracle harness 和 `git diff --check` 全部通过；
- `storyteller_a4_zdd_prototype.md` 记录实际文件、测试数、设备日志和未完成边界；
- rollout 仍然是 `ENUMERATED_ONLY` production + `ZDD_SHADOW` debug。

## 11. Stop conditions

Terra 遇到以下任一情况应停止实现并报告，而不是自行扩大范围：

- 必须改变 `PlayerWorldSetIdentity` 公共语义才能写 cache；
- 必须接入 B4 timeline 才能让测试通过；
- 必须让生产 recommendation 消费 shadow 结果才能观测；
- 五人构建出现真实 OOM、ANR 或持续主线程帧间隔 > 50 ms；
- 工作区出现与本切片重叠且无法保留的用户修改。
