# A4 ZddPlayerWorldSet prototype — implementation status

> Milestone: Phase A / A4  
> Status: IN PROGRESS  
> Started: 2026-08-12  
> Last updated: 2026-08-15  
> Production selector impact: none

## 新对话续接摘要（2026-08-15）

下一位协作者应从 A4 继续，不要重新实施 A0–A3。当前结论是：A3 枚举实现仍是 exact
correctness baseline；A4 ZDD 与 A3 在现有差分和 20 个可执行 official golden contracts 中一致；
生产默认仍为 `ENUMERATED_ONLY`，尚未授权切换为 ZDD runtime。

已完成的 A4 代码入口：

- `ZddPlayerWorldSet`：canonical ZDD、exact query、native public-state filtering、exact fallback；
- `ZddPlayerWorldSet.enumerateDirect`：不先保留完整 `EnumeratedWorldSet` 的 prefix-structured
  direct builder；
- `A4PlayerWorldSetRuntime` / `A4PlayerKnowledgeFactory`：snapshot、每位玩家知识快照和 replay；
- `EpistemicObservationLog`：与 formal snapshot 解耦的持久事件历史，已接入活动游戏存档/恢复；
- `A4IdentityRevealPrewarmCoordinator`：debug/shadow-only 的逐玩家串行预热、版本化缓存、取消、
  过期拒绝和完成/失败遥测；
- `A4DeviceBenchmarkHarness`：debug-only 五人真机诊断；
- Clocktower Day Overview 的 debug card：**A4 device diagnostic**。
- `A4ObservationCacheUpdateCoordinator`：目前只完成受众、策略和
  `UPDATED/REBUILT/MISSING/STALE` 分类及日志格式；**尚未执行缓存写回**。

最新的 phase telemetry 手机结果已经确认：五人构建的主要瓶颈是 exact-world generation
（P50/P95 72.732/94.407 ms），而不是 prefix insertion（15.080/24.219 ms）或
canonicalization（3.262/9.541 ms）。总构建 P50/P95 为 98.549/134.686 ms，仍未通过
provisional 50 ms ceiling；六人诊断继续禁用。

身份展示预热已通过五人 Trouble Brewing 真机验证。最新代表性记录为：5/5 `READY`，总构建
8.146 秒；首次推荐需求为 5/5 ready、`missingSeats=none`；950 个帧间隔样本的 P50/P95 为
8/8 ms，超过 32 ms 的样本 9 个，超过 50 ms 为 0，最大 49 ms。粗略峰值/结束堆增量为
134,747,648 / 50,284,032 bytes。Profiler heap dump 中实际保留 5 个 `ZddPlayerWorldSet` 与
5 个 `WorldZdd`；ZDD retained size 约 0.31 MiB，未发现缓存堆积证据。第二次 heap dump 因
Android Studio 长时间卡在 saving/内存不足而暂缓，不作为当前个人使用阶段的阻塞项。

真实 UI 已持久化公开死亡/处决及多类私密展示信息。Chef、Empath、Fortune Teller、
Washerwoman、Librarian、Investigator、Undertaker、Ravenkeeper 和 Spy 均有结构化记录入口；
可靠和明确选择的误导展示结果使用 proposition 传递，不解析本地化文案。存档恢复后重建所有
recipient knowledge 的一致性测试已通过。

当前可运行的核心验证命令：

```zsh
zsh -lic './gradlew testDebugUnitTest --no-daemon --tests "com.codex.campboardgamehost.clocktower.epistemic.A4*" --tests "com.codex.campboardgamehost.clocktower.epistemic.ZddPlayerWorldSetTest"'
```

需要保留的边界：不能把 OOM、time budget 或 enumeration cap 解释成 UNSAT；不能为了性能省略
Spy/Recluse/Drunk/Poisoner/red-herring 的 exact semantics；numeric/boolean ability information
当前仍是 `DECODE_REBUILD` fallback，不应作为已达标的交互快路径。

## First slice

The first A4 slice introduces `ZddPlayerWorldSet`, an immutable `PlayerWorldSet` implementation backed
by a canonical zero-suppressed decision diagram. Each exact mechanical world is encoded as a set of
stable atoms for seat roles, red herring, shown roles, death, malfunction state and explanation
clusters. Identical diagram nodes are interned and branches whose high set is empty are suppressed.

The prototype currently provides exact cardinality, `require`, `exclude`, possible role/Demon/Minion
queries, per-role and per-Demon counts, explanation clusters, immutable O(1) snapshots, and node-count
instrumentation. Unsupported observations deliberately decode paths and rebuild the diagram, keeping
the exact enumerated evaluator as the correctness fallback. Selected direct predicates use native
symbolic restriction, and each result records which path was taken.

## Validation boundary

Differential tests compare the ZDD implementation with `EnumeratedWorldSet` before and after Chef
observation filtering, including identity, exact cardinality, SAT/UNSAT, possible seats and roles,
counts, explanation clusters, require and exclude. All 20 A3-applicable official golden contracts
also execute through both representations and agree, including CHOICE and registration-bound cases.

The initial five-player differential fixture contains 15,936 exact worlds represented by 1,390 ZDD
nodes. This is evidence of structural sharing, not yet a memory or runtime acceptance result; object
layout, build cost and filtering cost still require controlled benchmarks and Android measurements.

The shared `PlayerWorldSet` contract now also provides immutable checkpoints/restoration and exact
candidate-value discovery from a finite legal observation domain. Numeric and boolean queries,
filter/checkpoint/restore sequences, and cross-identity checkpoint rejection are differential-tested
against both A3 and A4.

## Desktop comparison

An 11-sample development run on the 15,936-world fixture (first sample excluded from warm
percentiles) produced:

| Metric | Result |
|---|---:|
| ZDD nodes | 1,485 |
| ZDD cold construction | 202.840 ms |
| ZDD warm construction P50 / P95 | 66.089 / 146.003 ms |
| Estimated retained ZDD heap delta | 47,528 bytes |
| Numeric fallback: enumerated P50 / ZDD P50 | 10.563 / 64.007 ms |
| `RoleInPlay(Spy, false)`: enumerated P50 / ZDD P50 | 5.968 / 0.616 ms |
| `AliveAt(2, true)`: enumerated P50 / ZDD P50 | 5.919 / 0.014 ms |

The native supported filters are a clear desktop win (roughly 10x for `RoleInPlay` and over 400x for
the simple alive predicate at P50). The numeric fallback remains materially slower than enumeration,
so it must remain a correctness fallback rather than a performance claim. The retained-heap figure is
a best-effort JVM delta after GC, not a retained-size measurement and not Android peak memory.

## Hot-path optimization started

World atoms are now compact typed variables interned to integer IDs; ZDD nodes no longer carry or
compare encoded strings. A native immutable restriction operation filters direct public-state facts
without decoding paths or rebuilding the family. The first supported predicates are alive/dead and
explicit ability-state facts, with explanation-cluster overlays preserving exact A3 semantics.
Role-in-play is also compiled as a native ZDD set restriction across all seat-role variables.
Seat role/type/alignment predicates are compiled natively only after the *reachable* ZDD state has
eliminated both Spy and Recluse at that seat. Otherwise they use the exact fallback: either role can
create a legal interaction-local registration, so a simple per-seat restriction would silently
discard valid worlds.
Registration-sensitive, numeric and boolean ability observations continue through the exact
decode-and-rebuild fallback until each has a reviewed symbolic compiler. `lastFilterStrategy`
instrumentation makes fallback use visible in tests and future telemetry.

## Runtime policy and degradation contract

`A4WorldEngineRuntimePolicy` keeps the runtime on `ENUMERATED_ONLY` by default. The ZDD can only
be selected in `ZDD_SHADOW` (measurement only) or `ZDD_DEVICE_VALIDATED` after the POCO X5/X8 gates
are explicitly recorded. This is deliberately separate from the prototype: there is no real-game
snapshot integration yet, so the policy cannot silently alter storyteller decisions.

For each exact build/filter/query operation, the integration will record representation, native versus
decode-rebuild filter strategy, elapsed time, exact cardinality and the resulting recommendation.
The provisional pre-device budget is a 15 ms target and 50 ms acceptable ceiling, not a rejection
gate. An operation above target may reduce optional explanation detail; one above the ceiling moves
the caller to `ASSISTED`, and repeated breaches may recommend `MANUAL_ONLY`. None of these actions may
truncate worlds, change SAT/UNSAT, change a registration fact, or substitute approximate legality.

This makes the current fallback explicit: unsupported and registration-sensitive observations retain
the exact evaluator/decode-rebuild path. They are reported as `DECODE_REBUILD`, rather than being
treated as a silent native-ZDD success.

## Snapshot integration seam

`A4PlayerWorldSetRuntime` is now the snapshot-facing A3/A4 entry point. It derives a
`FormalGameState` from `GameSnapshot`, validates the knowledge snapshot binding and recipient seat,
and adds only the structural player-count fact needed by the Trouble Brewing enumerator. It does not
read actual roles from the formal state when constructing a player's possibilities. This preserves the
player-knowledge boundary while giving the future game flow one measurable construction seam.

In `ZDD_SHADOW`, the selected result remains `EnumeratedWorldSet`; a matching `ZddPlayerWorldSet` is
built, cardinality-checked and recorded only as telemetry. `ZDD_DEVICE_VALIDATED` is the sole mode
that can select ZDD, and remains off by default. The production recommendation flow is intentionally
not switched; UI observations now create per-recipient knowledge for replay/shadow measurement only.

`A4PlayerKnowledgeFactory` now supplies that Phase B1 seam: given explicit perceived roles and an
observation replay log, it builds one stable knowledge snapshot per seat. Public observations are
replayed to every recipient; private observations are replayed only to their declared recipients;
the result is canonicalized by round, sequence and observation ID. `A4PlayerWorldSetRuntime.buildAll`
constructs one isolated exact world set per recipient. The UI now persists and replays supported
public/private records into this seam; production recommendations still do not consume the shadow.

## Device benchmark harness

`A4DeviceBenchmarkHarness` is a diagnostic-only API for POCO measurement. It currently accepts only
the validated five-player fixture: the transparent A3 baseline must materialize worlds *before* ZDD
compression, and an eight-player live setup exhausted a 256 MB Android heap. This is an explicit A4
prototype limitation, not an UNSAT result or a gameplay degradation path. It runs an explicit set
of labelled observations over repeated cold/warm samples and produces one stable `toLogLine()` record
containing device label, sample count, exact world count, ZDD nodes, build P50/P95, coarse heap delta,
and separate P50/P95 values for each native or `DECODE_REBUILD` filter case. Use at least 11 samples,
discarding the first warm-up result (the harness does this). Capture the resulting line from logcat or
a diagnostic screen together with Android's profiler peak-memory and ANR evidence; the coarse heap
delta alone is not a memory acceptance result.

The caller must provide the formal snapshot, one recipient's knowledge snapshot, role definitions,
and cases that state their expected filtering strategy. A strategy mismatch throws rather than
mislabeling a fallback as native. This harness is deliberately not invoked during normal gameplay.

The debug Clocktower screen now exposes **A4 device diagnostic** only for an eligible five-player
Trouble Brewing game with complete role data. The control is compiled behind `BuildConfig.DEBUG`, runs the
11-sample harness on `Dispatchers.Default`, and displays the pasteable report in the screen. It uses
the live structural snapshot and synthetic `alive` and `Spy absent` probes. When a living,
non-poisoned actual Chef or Empath is available, it also adds a correctly source-bound numeric-fallback
probe. The screen states explicitly that it does *not* replay the persisted historical information
log: death and multi-night state transitions belong to B4 and must not collapse an A4 setup benchmark
to a false UNSAT result.

The Day Overview uses a dedicated screen which returns before the shared judge-screen list. The
diagnostic is therefore also inserted directly below the Day Overview player tiles (the screen used
for normal daytime play), rather than relying on the shared navigation/footer insertion point.

## First phone result

User-reported debug-device run, 11 samples, five-player Trouble Brewing fixture (device model not
included in the pasted excerpt):

| Metric | Result |
|---|---:|
| Exact worlds / ZDD nodes | 1,080 / 386 |
| Build P50 / P95 | 77.527 / 89.944 ms |
| Coarse maximum build heap delta | 11,325,440 bytes |
| `alive-seat-2` native P50 / P95 | 0.165 / 0.208 ms |
| `spy-absent` native P50 / P95 | 0.084 / 0.352 ms |
| Numeric decode/rebuild P50 / P95 | 190.692 / 212.712 ms |

Interpretation against the provisional pre-device targets:

- native restrictions comfortably meet the 15 ms operation target;
- ZDD construction exceeds both the 15 ms target and 50 ms provisional ceiling;
- numeric decode/rebuild exceeds the ceiling by a large margin;
- the coarse 10.8 MiB heap delta exceeds the 8 MiB engineering target and still requires Android
  profiler peak/retained-memory confirmation;
- combined with the observed eight-player 256 MB OOM before compression, converting a fully
  materialized `EnumeratedWorldSet` cannot be accepted as the mobile runtime construction path.

This result supports continuing the ZDD filtering prototype, but blocks runtime promotion until setup
worlds can be constructed directly into a compact representation (or another exact bounded builder)
without first retaining every enumerated world.

## Direct streaming construction started

`TroubleBrewingWorldEnumerator.stream` exposes the existing exact setup traversal as a lazy sequence.
`ZddPlayerWorldSet.enumerateDirect` consumes each emitted world without constructing an intermediate
`EnumeratedWorldSet` or list of encoded atom sets. Setup facts and visible observation replay retain
the same exact evaluator semantics as the A3 baseline.

The five-player differential fixture produces the same identity, cardinality, query answers and node
count through direct construction and enumerate-then-convert construction. The focused A4/ZDD suite
passes, including shadow selection and a validated-mode assertion that no enumerated baseline is
constructed when direct ZDD is selected.

An 11-sample desktop comparison on the larger 15,936-world fixture measured:

| Construction path | Warm P50 | Warm P95 |
|---|---:|---:|
| Enumerated set already built → ZDD conversion | 45.569 ms | 90.705 ms |
| Direct streamed enumeration → ZDD | 68.902 ms | 84.117 ms |

Direct construction is currently a memory-lifetime improvement, not a latency improvement. Its
incremental union still retains builder nodes until final compaction, so the five-player device gate
remains in place until larger-player heap behavior is measured safely. The device diagnostic now uses
this direct path, allowing the next phone run to compare its build time and coarse heap delta with the
previous enumerate-then-convert result.

### Direct-construction phone result

The same user-tested five-player phone scenario was rerun after switching the diagnostic to direct
streaming construction:

| Metric | Enumerate then convert | Direct streaming |
|---|---:|---:|
| Build P50 | 77.527 ms | 118.899 ms |
| Build P95 | 89.944 ms | 161.206 ms |
| Coarse maximum build heap delta | 11,325,440 bytes | 10,067,968 bytes |
| `alive-seat-2` native P50 / P95 | 0.165 / 0.208 ms | 1.503 / 1.817 ms |
| `spy-absent` native P50 / P95 | 0.084 / 0.352 ms | 1.782 / 1.940 ms |
| Numeric decode/rebuild P50 / P95 | 190.692 / 212.712 ms | 178.316 / 207.992 ms |

The direct path reduced the coarse heap delta by 1,257,472 bytes (about 11.1%) but remains above the
8 MiB engineering target. Construction became materially slower and exceeds the 50 ms provisional
ceiling at both percentiles. Native operations remain safely below the 15 ms target, while numeric
fallback remains unacceptable for an interactive runtime path.

Therefore the direct incremental-union builder does not pass the A4 runtime gate in its present form.
The next builder optimization must avoid accumulating unreachable intermediate union nodes until one
final compaction. Periodic compaction may cap temporary heap, but a structural setup compiler that
builds shared role-assignment branches directly is the preferred route because it also avoids one
union operation per exact world.

### Prefix-structured compiler

The per-world union builder has now been replaced. Streamed worlds are inserted into a shared trie of
ordered world variables; a single bottom-up pass then turns each prefix family into canonical ZDD
nodes. Sibling alternatives become low branches and the selected variable's continuation becomes the
high branch. This creates no intermediate ZDD union results and requires no final unreachable-node
compaction.

The differential fixture still produces the exact same identity, world cardinality, query answers,
explanation clusters and 1,485-node canonical diagram. The complete focused A4/ZDD regression suite
passes. On the 15,936-world desktop fixture, the new direct-build warm latency is 21.066 ms P50 and
31.621 ms P95, compared with 68.902/84.117 ms for the replaced incremental-union implementation.
This is approximately a 69% P50 improvement. Phone heap and latency remain the decisive gate.

### Prefix-compiler phone result

The same five-player phone scenario was rerun with the prefix-structured compiler:

| Metric | Incremental union | Prefix compiler |
|---|---:|---:|
| Build P50 | 118.899 ms | 114.388 ms |
| Build P95 | 161.206 ms | 123.150 ms |
| Coarse maximum build heap delta | 10,067,968 bytes | 6,651,904 bytes |
| `alive-seat-2` native P50 / P95 | 1.503 / 1.817 ms | 1.522 / 1.742 ms |
| `spy-absent` native P50 / P95 | 1.782 / 1.940 ms | 1.396 / 1.906 ms |
| Numeric decode/rebuild P50 / P95 | 178.316 / 207.992 ms | 194.221 / 221.685 ms |

The coarse build heap delta fell by 3,416,064 bytes (about 33.9%) and is now approximately 6.34 MiB,
below the provisional 8 MiB engineering target. Build P95 also became substantially more stable, but
P50/P95 remain above the 50 ms provisional ceiling. Both native filters remain comfortably below the
15 ms target. Numeric fallback remains far outside the interactive budget and did not improve.

This is a partial A4 gate result: five-player construction now meets the coarse heap target, but not
the latency target. A six-player probe remains disabled. The subsequent phase-enabled benchmark below
splits construction into exact-world generation, prefix insertion and bottom-up canonicalization so
the next optimization can target the measured bottleneck rather than the overall build blindly.

### Phase-level construction telemetry

The diagnostic now measures the three construction phases independently for every sample:

- `generationP50P95Us`: lazy exact-world traversal, including setup constraints and mechanical
  variants;
- `prefixInsertP50P95Us`: world-atom encoding, ordering and insertion into the shared prefix trie;
- `canonicalizeP50P95Us`: bottom-up conversion from the trie into interned canonical ZDD nodes.

The first sample is excluded from each phase percentile exactly as it is for total build latency. The
report continues to include total build P50/P95 because setup/catalog preparation and measurement
overhead are intentionally not hidden inside one of the three phases. Exact-world count is checked
against the resulting ZDD cardinality in the harness test. This telemetry does not change the world
set, rollout mode or production selection behavior.

### Phase-telemetry phone result

The phase-enabled debug build was measured on a Xiaomi 2511FPC34G in the same 11-sample five-player
scenario. The result was captured from the on-screen diagnostic on 2026-08-13:

| Metric | P50 | P95 |
|---|---:|---:|
| Total build | 98.549 ms | 134.686 ms |
| Exact-world generation | 72.732 ms | 94.407 ms |
| Prefix insertion | 15.080 ms | 24.219 ms |
| Canonicalization | 3.262 ms | 9.541 ms |
| `alive-seat-2` native filter | 1.652 ms | 2.072 ms |
| `spy-absent` native filter | 1.287 ms | 1.995 ms |
| Numeric decode/rebuild fallback | 159.734 ms | 219.985 ms |

The build produced 1,080 exact worlds and 386 ZDD nodes. Its coarse maximum heap delta was
6,651,904 bytes, matching the previous prefix-compiler heap result and remaining below the
provisional 8 MiB target. Total latency varies from the preceding phone run, but it remains clearly
above the 50 ms ceiling in both runs. Phase telemetry identifies exact-world generation as the
dominant measured phase: its P95 is about four times prefix insertion and about ten times
canonicalization. Native filters remain within the interactive target; numeric decode/rebuild does
not.

### Identity-reveal prewarming design

The product has a natural startup computation window. Once the storyteller has committed the player
count and role assignment, the phone is handed to players sequentially so each player can view their
identity. During most of that interval the application only renders a stable identity screen and
waits for the player. A4 will use this interval to prepare exact initial knowledge in the background,
instead of requiring every world set to be constructed when a recommendation is first requested.

The prewarming boundary is:

1. Role assignment is committed and a stable game revision is created.
2. `A4PlayerKnowledgeFactory` derives each recipient's initial perceived role and starting
   observations. The true assignment may select the applicable ability/perception contract, but it
   must not eliminate a world merely because the recipient does not know that fact.
3. A single low-priority worker constructs recipient world sets sequentially. The currently displayed
   recipient is prioritized, then remaining seats are processed in reveal order. Avoiding parallel
   builds bounds transient heap and reduces contention with identity-screen rendering.
4. Each completed result is stored under `(gameId, gameRevision, recipientSeat,
   knowledgeSnapshotIdentity, enginePolicy)`. Recommendation code may consume it only when every key
   component still matches.
5. A role reassignment, player-count change, perceived-role change, observation-log revision or new
   game cancels pending work and invalidates mismatched entries. Cancellation or resource exhaustion
   is operational state, never `UNSAT`.
6. Subsequent public/private observations update only the affected recipient caches through exact
   filtering or replay from an immutable checkpoint. Events that have not happened are never
   predicted or applied during prewarming.

The first rollout remains `ZDD_SHADOW`: production recommendations continue to use the enumerated
result, while the prewarmer records readiness before the first recommendation request, per-recipient
build time, total wall-clock completion time, cancellation/staleness, peak/coarse heap evidence and
main-thread frame impact. If a cache is not ready, gameplay proceeds through the existing exact path;
the identity flow must never wait for A4.

Prewarming changes the user-perceived latency objective, but not the safety gates. The five-player
heap result still needs profiler confirmation, larger-player construction remains disabled after the
256 MB OOM, and numeric/boolean decode-rebuild remains unsuitable for an interactive post-observation
path. The synchronous 50 ms ceiling is retained as a diagnostic signal, even though an initial build
that completes safely before its first consumer may no longer block the product experience.

### Identity-reveal prewarming first slice

The first debug/shadow-only slice is implemented. `A4IdentityRevealPrewarmCoordinator` owns one
revision-bound session, constructs recipients sequentially, prioritizes the recipient displayed when
the session starts, and retains only each matching ZDD shadow after the enumerated differential
baseline has been released. Cache keys include game ID, game-state revision, player-input revision,
formal snapshot ID, recipient seat, knowledge snapshot ID and rollout mode.

The Compose integration starts only for a five-player Trouble Brewing debug game while the app is on
`PassPhone` or `RevealCard`. It runs on `Dispatchers.Default`; a concurrent frame callback records the
main-thread frame-interval distribution. Completion or cancellation writes one `A4IdentityPrewarm`
logcat line with recipient counts, per-seat status, total build milliseconds, frame sample count,
frame P50/P95, counts above 32/50 ms, the maximum interval, and coarse process-heap deltas before,
during and after retaining the prewarm cache. The heap values are deliberately labelled coarse: they
are not Android-profiler peak/retained-size evidence and must not be used as a memory acceptance gate.
Leaving the
identity flow, starting a new session or changing a revision cancels pending seats. An in-flight exact
build may finish, but its result is marked `STALE` and is never cached. `OutOfMemoryError` is recorded
as `FAILED`, the remaining queue is cancelled, and no resource failure becomes `UNSAT`.

Focused tests cover displayed-seat priority and sequential order, identical-key reuse, cancellation
of in-flight work, revision invalidation, ordinary failure, resource exhaustion and rejection of a
non-shadow rollout. The production selector and recommendation consumer remain unchanged.

A debug-only readiness demand probe is implemented immediately before the first `recommendSetup`
call. It rebuilds the initial version-bound keys, records only aggregate hit/miss counts as
`A4_IDENTITY_PREWARM_DEMAND`, and never returns a cached `PlayerWorldSet` to the recommender. This
measures whether the prewarm cache was available at first demand while preserving the
`ENUMERATED_ONLY` production result.

### Durable observation-log first slice

`RecordedEpistemicObservation` and `EpistemicObservationLog` now provide an immutable,
canonical, snapshot-independent event history. A record is rebound to the formal snapshot used for
each calculation, so a game-state revision cannot discard information a player already received.
Public records replay to every player; private records replay only to declared recipients. The log is
part of `GameSnapshot` and `ClocktowerGameSession`, increments player-input revision on append, and
has canonical schema-v2 JSON encoding for app persistence.

The Compose session persists this log with the existing active-game save. Its first live UI adapter
records public `AliveAt(seat, false)` facts for confirmed executions and night deaths; it does not
attempt to infer private ability facts from localized display text. Restore now rejects an invalid or
unsupported observation record explicitly instead of silently dropping part of player knowledge.
The device diagnostic deliberately excludes this persisted log until B4 implements death and
multi-night state transitions; persistence/replay tests remain separate from the A4 setup benchmark.
The A3/A4 setup enumerator also rejects non-first-night observations with an explicit B4-required
error, so an unsupported timeline can never be surfaced as an empty exact world set.

The first supported private-display adapters now cover Chef, Empath, Fortune Teller, Washerwoman,
Librarian, Investigator, Undertaker, Ravenkeeper and Spy. Reliable and deliberately misleading
display options carry the exact proposition shown to the recipient; public deaths/executions are
also persisted. Focused tests verify that saving/restoring a session and rebuilding all recipient
knowledge snapshots produces identical results.

When a generated setup contains a Drunk, the app now creates the game seed and synchronously selects
the style-appropriate full setup recommendation before the first identity card is shown. The selected
plan's `DrunkShownRole` becomes the identity card; only a recommendation failure uses a random absent
Townsfolk fallback. This setup selection measured about 22 ms in the focused JVM test and remains
separate from the much slower A4 world-set prewarm that runs during identity reveal.

The identity shown to a Drunk is then treated as a committed setup fact. Later setup recommendation
search receives that `DrunkShownRole` as a non-clearable lock and optimizes the remaining decisions
around it; applying an automatic or manual recommendation cannot overwrite an identity already shown
to a player. Spy/Recluse registration evaluation also checks the queried character's own ability
state, so poisoning removes the optional special-registration branch while preserving actual-role
matches.

### Observation cache-update status and correction

`A4ObservationCacheUpdateCoordinator` currently implements only the safe planning/telemetry layer:
public records select all seats, private records select only their recipients, and each affected seat
is classified as `UPDATED`, `REBUILT`, `MISSING` or `STALE`. Tests cover audience isolation, native
versus replay strategy classification, and outcome classification. `A4ObservationCacheUpdateReport`
defines the future `A4_OBSERVATION_CACHE_UPDATE` logcat format.

This is **not yet an executing cache updater**. A previous implementation direction proposed calling
`PlayerWorldSet.require()` on an old cached ZDD and storing that result under a new version key. That
is invalid with the present identity contract: the filtered world set retains the old
`knowledgeSnapshotId`, while the new key contains the rebuilt knowledge snapshot ID. Writing it under
the new key would violate cache identity and recipient-knowledge boundaries.

The next safe slice must therefore rebuild affected seats from the complete immutable observation log
and store those new shadows under keys derived from the new formal/knowledge snapshots. Native
filtering may be reconsidered only after `PlayerWorldSet` gains an explicitly verified rebinding API;
until then, the `NATIVE_FILTER` classification is diagnostic only and must not perform a writeback.

### Verification state for handoff

The latest focused runs completed successfully after the UI observation adapters, Spy grimoire,
session restore test and cache-update classifier were added. Relevant suites include
`A4PlayerKnowledgeFactoryTest`, `EpistemicSemanticModelTest`, `ClocktowerGameSessionTest`, and
`A4ObservationCacheUpdateCoordinatorTest`. Android Debug Kotlin compilation occurred as part of these
runs. No APK should be generated by Codex; the user builds/runs APKs in Android Studio.

The worktree is intentionally uncommitted and contains the complete A4 work plus pre-existing user
changes. Do not reset, discard, stage or commit unrelated files. Production selection remains
`ENUMERATED_ONLY`; all ZDD prewarming and future update work remains debug/shadow-only.

## Remaining before A4 decision

The next implementation slice is frozen as **A4.5 Observation cache rebuild executor**. Its exact
scope, cache-identity prerequisite, state machine, test matrix, Terra-sized batches and stop
conditions are defined in `docs/storyteller_a4_5_observation_cache_rebuild_spec.md`. That document is
normative for the next slice; this status document records results after implementation.

1. Implement the safe version-bound cache rebuild executor. Its input must contain the new formal
   snapshot, complete observation log, perceived roles and role definitions; the executor derives
   before/after per-seat knowledge itself. Rebuild only affected recipients, write results only under
   exact new keys, reject stale results, and emit `A4_OBSERVATION_CACHE_UPDATE`. Do not reuse old
   filtered world sets under new identities.
2. Add focused tests for public/all-seat rebuild, private/single-recipient rebuild, missing old cache,
   cancellation/stale revision, builder failure/OOM, exact new-key identity and unaffected-seat reuse.
3. Wire the executor to the real observation append path in Debug/Trouble Brewing/five-player shadow
   mode. Execute only first-night/round-1 rebuilds; Day, death, execution and multi-night records must
   report `DEFERRED_B4` without invoking the builder. Then collect one real private-update logcat flow
   and one deferred-B4 flow. Gameplay must not wait for or consume the shadow result.
4. Complete the deferred leave/restart cancellation check. Repeat Android memory profiling only if
   later usage shows growth; the current second heap dump is intentionally deferred.
5. Profile and optimize exact-world generation only if update rebuilds fail to finish within the
   normal host interaction window or produce frame/thermal pressure.
6. Compile registration-sensitive numeric and boolean observations symbolically only if target-device
   profiling shows the fallback exceeds the agreed post-observation experience budget.
7. Decide whether this prototype should become the
   runtime world-set implementation.

## A4.5 completion record — 2026-08-15

The A4.5 observation-cache rebuild executor is complete. This section supersedes the earlier
“not yet an executing cache updater” and “remaining before A4 decision” notes above.

Implemented files:

- `A4ShadowWorldSetCache.kt`: shared JVM-only cache, generation scope, atomic current-generation
  commit and cancellation.
- `A4ObservationCacheRebuildExecutor.kt`: complete-log before/after replay, recipient isolation,
  sequential rebuild, terminal outcomes and aggregate-only telemetry.
- `A4IdentityRevealPrewarmCoordinator.kt`: now uses the shared cache and validates recipient,
  knowledge, hypothesis and exact world-set identity.
- `A4PlayerKnowledgeFactory.kt`: canonical knowledge includes the publicly known player count, so
  the cache key and runtime builder share one identity.
- `MainActivity.kt`: Debug-only, five-player Trouble Brewing, `ZDD_SHADOW` dispatch after durable
  private observation append and death/execution append; work runs on `Dispatchers.Default`.

The focused A4 test set contains 35 `@Test` cases. It covers key/hypothesis identity, atomic stale
commit rejection, private/public rebuilds, complete-log replay, missing/reused/unaffected values,
cancellation, ordinary failure, synthetic OOM, deferred B4 and deterministic redacted telemetry.
Production recommendation does not receive a cache value or otherwise consume the shadow result.

### Device observations

On Xiaomi 22101320G, Debug five-player Trouble Brewing recorded:

- first-night private Spy update: recipient 2 `READY_REBUILT`, recipients 1/3/4
  `READY_REUSED`, recipient 5 `MISSING_UNAFFECTED`; 1,162 ms and zero coarse heap delta;
- first-night private Librarian update: recipient 3 `MISSING_REBUILT`; 3,381 ms and
  38,614,024-byte coarse heap delta;
- night-two Empath observation: `DEFERRED_B4`, no builder invocation, zero build time and zero
  coarse heap delta.

The earlier first-night identity mismatch was repaired by canonicalizing `PlayerCount` in player
knowledge. No real OOM or ANR was observed in the successful rebuild flows. The historical prewarm
diagnostic still showed frame intervals above 50 ms; that is an existing A4 performance finding and
does not promote rollout. Production remains `ENUMERATED_ONLY`; A4 remains Debug `ZDD_SHADOW` only.

Verification completed: focused A4 regression, full JVM `testDebugUnitTest`, ASP Oracle harness
(11 tests), and `git diff --check`.
