# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-21  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 当前 `main` 基线：`3db66482d9367c6b42a3f2550b979c28bfafea42`  
> 当前 source PR：#24 `R6: establish production semantic-history foundation`（Draft）  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> 多剧本架构规范：`多剧本多板子与动态游戏流程架构设计_v1.md`  
> Post-P1 production audit：`post_p1_production_rollout_entry_audit_2026-08-21.md`  
> 当前交接：`NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md`

## 1. 当前结论

Phase A、R5.5 与 R6 P1 semantic prerequisites 已完成。

```text
Phase A correctness foundation              PASS
R5.5 Script & Dynamic Flow Foundation       CLOSED / MERGED
R6 P1.1 Spy Grimoire truth boundary         PASS
R6 P1.2 Global timeline semantics            PASS
R6 P1.3 Knowledge-safe input boundary        PASS
R6 P1 semantic prerequisites                CLOSED
Post-P1 production-rollout entry audit      COMPLETE
Production Semantic-History Foundation      IN PROGRESS / PR #24
```

当前第一 rollout slice 只建立：

- explicit semantic-history mode；
- durable timeline cursor / session persistence contract；
- active-game schema v3；
- restore-time fail-closed validation。

**production Host/Compose observation semantics 仍不切 Global。**

### 2026-08-21 persistence policy decision

项目目前没有外部用户，因此不再为未发布的旧 active-game save 承担 migration complexity。

```text
active-game v1 → UNSUPPORTED
active-game v2 → UNSUPPORTED
active-game v3 → ONLY SUPPORTED SCHEMA
```

旧 v1/v2 save 在 restore 最入口直接拒绝，并清除失效 active save；**不得**因为缺字段猜成 `LEGACY_LOCAL`。

`LEGACY_LOCAL` 仍保留，但它是 **v3 中显式写入的新语义状态**，不是旧 save migration fallback。

当前仍未授权：

- production Global observation producer cutover；
- production Spy `VERIFIED_EXACT` cutover；
- production historical multi-night Possible Worlds；
- B4 production authority；
- ZDD production promotion / `ZDD_DEVICE_VALIDATED`；
- recommendation legacy direct entry removal（它属于后续独立阶段）。

## 2. 当前阶段状态

| 阶段 | 状态 | 当前含义 |
|---|---|---|
| A0 / A1 / A1.1 | PASS | unified semantic model 与 player-knowledge boundary 成立。 |
| A2 ASP Oracle | PASS | typed/fail-closed oracle contract 已建立。 |
| A2.1 Golden corpus | PASS | **52 total semantic contracts；24 当前 oracle/A3 executable；28 deferred / `ORACLE_NOT_APPLICABLE`。** |
| A3 EnumeratedWorldSet | PASS / exact baseline | exact correctness baseline；尚不是完整 historical state-transition engine。 |
| A4 ZDD | PASS AS EXACT SHADOW | differential correctness 成立；device gate 未通过，不得驱动 production decisions。 |
| A4.5 cache/lifecycle | PASS | observation cache rebuild/durability boundary 已验证。 |
| R5.5 | CLOSED / MERGED | production flow planner 与 multi-script structural foundation 已完成。 |
| R6 P1.1 | PASS | VerifiedExact Grimoire semantic contract 已完成；production producer 仍 Legacy。 |
| R6 P1.2 | PASS | Global action/observation chronology contract 已完成；production ownership 尚未切换。 |
| R6 P1.3 | PASS | world/knowledge safe-core 不再接收完整 storyteller truth。 |
| Post-P1 entry audit | COMPLETE | production authority map、persistence gaps、rollout dependency 已确认。 |
| **Production Semantic-History Foundation** | **IN PROGRESS / PR #24** | schema v3-only + explicit mode + existing cursor key wiring + fail-closed restore；不切 production Global producer。 |
| New-game Global observation ownership | NEXT AFTER FOUNDATION | 新 game 才开始通过 session allocator 写 Global observation。 |
| Recommendation Entry-Point Unification | REQUIRED FOLLOW-UP | Global/session ownership稳定后，删除 legacy direct recommendation path。 |
| Historical multi-night engine | NOT AUTHORIZED | 先完成 production history ownership。 |
| Spy VerifiedExact production | NOT AUTHORIZED | 等 authoritative durable physical Grimoire ledger。 |
| ZDD production promotion | NOT AUTHORIZED | 等 exact multi-night baseline + realistic device gate。 |

## 3. 已确认的长期架构边界

### 3.1 Multi-script foundation：结构层已验证，advanced semantics 仍 TB-first

R5.5 已通过 Trouble Brewing + No Greater Joy 证明：

```text
script asset / imported JSON
        ↓
parser + normalizer
        ↓
ClocktowerCharacterRegistry + ClocktowerScriptCatalog
        ↓
ValidatedClocktowerRuleset
        ↓
ClocktowerFlowPlanner
        ↓
ClocktowerHostInteraction
        ↓
production UI adapter
```

当前准确边界：

```text
catalog / normalization / registry / flow / ruleset identity
    → MULTI-SCRIPT VERIFIED

recommendation metadata / Possible Worlds / role-specific epistemic semantics
    → TB-FIRST / EXPAND LATER
```

未来不得因为 advanced semantics 仍 TB-first 就重建第二套 catalog / flow framework。

### 3.2 Flow authority 与 UI adapter 必须分离

legacy adapter 可以保留，但不得重新决定：

- next interaction；
- eligibility；
- semantic recommendation truth；
- global timeline identity。

### 3.3 Player knowledge boundary

P1.3 已锁定：

```text
Actual FormalGameState
        ↓ one-way projection
KnowledgeSafeWorldInput / KnowledgeConstructionInput
        ↓
world / player-knowledge core
```

actual role/alignment/type、poison、shown role、storyteller-only propositions 不得通过完整 Formal state 偷渡到 player-world safe core。

## 4. Post-P1 production audit 的关键发现

### 4.1 Timeline/session authority 已存在，但 production 尚未切换

正确 semantic authority 已存在：

```text
ClocktowerGameSession
    ├── GameSnapshot.epistemicObservationLog
    ├── GameSnapshot.nextTimelineGlobalSequence
    ├── allocateTimelinePoint(...)
    └── recordEpistemicObservation(...)
```

foundation 之前 production 仍是：

```text
private observation → nightStepIndex
public observation  → clocktowerEventCounter
observation storage → App/Compose mutable list
```

`ClocktowerNightCheckpoint` 已经有 `clocktowerNextTimelineGlobalSequence` persistence key，但 production live/restore wiring 原先没有真正接通 cursor。

**决定：复用这个现有 key；禁止新增第二套 cursor representation。**

### 4.2 Spy production truth 仍不足以升级 VerifiedExact

production 仍没有 durable authoritative physical Grimoire ledger，因此不能仅通过改变 binding flag 把 Spy producer 升级为 `VERIFIED_EXACT`。

### 4.3 A3 / ZDD / B4 仍不是完整 historical engine

```text
A3  = exact baseline, not general historical transition engine
ZDD = exact shadow/prototype
B4  = isolated historical shadow with deferred action classes
```

shared chronology support != historical multi-night semantic engine。

## 5. 当前实施：Production Semantic-History Foundation

### 5.1 目标模型

```text
ClocktowerSemanticHistoryMode
├── LEGACY_LOCAL
└── GLOBAL_V1
```

`LEGACY_LOCAL` 表示当前 v3 game 明确使用 legacy-local production observation semantics；它不再代表“旧 save 猜出来的模式”。

### 5.2 Active-game v3 contract

必须满足：

1. `CURRENT_VERSION = 3`；
2. v1 / v2 对所有 game kind 都 unsupported；
3. unsupported save 在 live-state mutation 前拒绝；
4. Clocktower v3 必须显式包含 `clocktowerSemanticHistoryMode`；
5. missing / null / unknown / invalid mode → fail closed；
6. **必须复用**现有 `clocktowerNextTimelineGlobalSequence` 作为唯一 cursor key；
7. Clocktower v3 必须显式包含该 cursor，且必须为非负整数；
8. 不从 `nightStepIndex` / round / eventCounter 合成或猜测 global sequence；
9. `GLOBAL_V1` history 中出现 `LegacyLocal` observation → fail closed；
10. `LEGACY_LOCAL` history 中出现 Global observation → fail closed；
11. Global cursor 必须严格大于所有 committed Global observation positions；
12. v3 Trouble Brewing restore 必须具有 immutable ruleset basis + matching persisted ruleset ref，不再重建旧 basis；
13. 新创建/reset 的 production game 仍显式从 `LEGACY_LOCAL + cursor 0` 开始；
14. 第一 foundation PR 不切 production observation producer 到 Global。

### 5.3 明确删除的旧兼容能力

以下不再属于产品 contract：

```text
v1 Clocktower identity migration
v1/v2 active-game restore
missing semantic-history mode -> LEGACY_LOCAL
missing ruleset ref -> reconstruct
succession 后通过旧 hash 猜 setup role basis
```

旧 `ClocktowerLegacyPersistenceIdentityFactory` 与 migration tests 应删除。

如果因为超大 App 文件机械清扫暂时保留 compile-only legacy symbol，它必须是 **unreachable / fail-only shim**：

- `isSupportedVersion(1/2)` 必须为 false；
- shim 不得产生 migration result；
- 直接调用 legacy helper 也必须失败；
- 后续大文件拆分时删除 dead branch，不把它重新解释为 compatibility promise。

## 6. Foundation 后的 rollout 顺序

```text
1. Production Semantic-History Foundation
   schema v3-only + explicit mode + existing cursor/session persistence

2. New-game Global observation ownership cutover
   新 game 从第一条 committed semantic event 开始走 session allocator

3. Production Recommendation Entry-Point Unification
   移除 legacy direct recommendation button/path

4. Historical action + observation capture
   action 与 observation 共用同一 global allocator namespace

5. A3 historical multi-night exact baseline

6. Authoritative physical Grimoire ledger
   然后才允许 production Spy VERIFIED_EXACT

7. B4 historical expansion

8. Revision-driven recommendation unification

9. Reconsider ZDD promotion
```

## 7. Recommendation UI：legacy direct 按钮的正式定位

legacy direct recommendation path 是双 decision-authority debt，不是 cosmetic UI polish。

Trigger：

```text
semantic-history/session ownership stable
        ↓
Recommendation Entry-Point Unification
        ↓
historical recommendation semantics expansion
```

Exit criteria：

1. production 不存在绕过统一 recommendation authority 的入口；
2. UI 不自行重新计算 recommendation semantics；
3. revision/context 变化后 stale result 不可通过 legacy path 复用；
4. `MANUAL_ONLY` / `INELIGIBLE` / unavailable 由同一 result model 驱动；
5. regression test 防止 legacy direct path 回归。

## 8. Deferred Decisions / Reopen Triggers

| Decision | 当前决定 | 何时重新打开 |
|---|---|---|
| `globalSequence` 是否进入 `PlayerWorldSetIdentity` | **NO** | timeline position 本身改变 world constraints / cache identity 时。 |
| `StoryDisruptionRisk` thresholds | UNSET | 有足够真实游戏样本且 recommendation policy 准备消费时。 |
| `MANUAL_ONLY` vs `INELIGIBLE` UX | DEFERRED | Recommendation Entry-Point Unification 阶段。 |
| Revision-driven recommendation full unification | PARTIAL | semantic-history/session ownership完成后。 |
| Spy `VERIFIED_EXACT` production | LEGACY ONLY | durable authoritative physical Grimoire ledger 完整可 restore 后。 |
| ZDD production promotion | SHADOW ONLY | historical exact baseline + realistic device gate 通过后。 |
| Advanced multi-script Possible Worlds | TB-FIRST | TB historical exact baseline稳定后，用第二剧本做独立 semantic proof。 |
| old active-game save migration | **NO / DROPPED** | 只有产品真正发布并拥有外部用户后，未来 schema 迁移策略才重新设计；v1/v2 不恢复。 |

## 9. Production guardlines

```text
Production Clocktower flow order: planner-backed
Production Werewolf flow order: planner-backed
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
B4: isolated shadow only
R6 P1: CLOSED
Post-P1 entry audit: COMPLETE
Active-game schema: V3 ONLY
V1/V2 restore: UNSUPPORTED
Clocktower v3 history mode: REQUIRED / EXPLICIT
Clocktower v3 cursor key: EXISTING KEY / REQUIRED / DO NOT DUPLICATE
Production Global producer cutover: NOT YET WIRED
Production VerifiedExact Spy producer: NOT AUTHORIZED
Production historical multi-night Possible Worlds: NOT AUTHORIZED
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
```

禁止：

- 从缺字段猜 `LEGACY_LOCAL`；
- 从 legacy local sequence 猜 global identity；
- 新增第二个/平行 production timeline cursor persistence key；
- 恢复 v1/v2 active save；
- 从旧 hash/当前角色猜旧 setup ruleset basis；
- 从 legacy Grimoire/空 reminder/UI label 猜 `VERIFIED_EXACT`；
- 把 timeout/OOM/cap 当 UNSAT；
- 截断 exact worlds 后仍声称 exact；
- 把 storyteller-only truth 放入 player knowledge；
- 让 shadow result 驱动 production decision；
- 为新剧本重建第二套 catalog/flow framework；
- 在 foundation PR 中顺便切 Global producer、改 recommendation UI、Spy、A3/B4/ZDD authority。

## 10. 实战验证

实战用于发现 runtime/UX 问题，但分类保持：

- rules / flow / persistence / state correctness defect → correctness follow-up；
- recommendation decision-entry issue → Recommendation Entry-Point Unification；
- cosmetic preference → 不打断 rollout correctness work。

由于 v1/v2 已主动放弃，升级到 v3 后当前设备上的旧 active-game save 无需继续兼容；重新开局即可。

## 11. 开发与 CI 策略

每个 behavior-changing rollout PR：

```text
latest main audit
→ failing contract/regression tests
→ smallest implementation
→ focused tests
→ R2 main-thread boundary
→ Android unit tests + debug APK
→ ASP contract tests
→ real Clingo cross-validation
→ exact diff audit
→ final correctness/review-thread audit
→ merge
```

CI 必须**实际启动并执行 steps**。GitHub Actions 在 runner 启动前失败、job `steps=null`、没有 checkout/compiler/test log 时，只能记为 infrastructure failure，不能视为代码通过或失败。

PR #24 在上述完整 gate 真正执行并通过前保持 Draft，不合并。

## 12. 关键历史证据

- R5.5 merge：`7add8569e2484a350f6cf1512a730e9f4db469c5`
- P1.3 safe-input PR #7/#8：`19b91887344655285ec8bd93ca5bdb51bcfff445` / `8f5ccc551948fea085caf8df3eb100ef67eae438`
- P1.1 VerifiedExact semantic close / PR #21 merge：`f77338bc85ae4a81b7e54e456b430e2f7f35c51a`
- P1 docs closeout / PR #22 merge：`d56edd1552dc25dc73574c311179b9fe5a9d216b`
- docs/design cleanup / PR #23 merge：`3db66482d9367c6b42a3f2550b979c28bfafea42`
- Semantic-History tests-first red evidence：`4759c6ee95bbbae53f4b43412bf75b7ee4cf5768` / CI #308

## 13. 文档维护规则

1. **只有本文件维护当前执行点 / 当前阶段状态。**
2. 专项 design/audit/closeout/handoff 保存证据，不创建第二个并列 roadmap。
3. 历史文档中的旧 migration 设计可以保留为历史证据，但不得覆盖本文件的 **v3-only / v1-v2 unsupported** 决策。
4. 新 handoff 必须指向本 roadmap。
5. 未发布阶段可以主动丢弃不再有价值的 compatibility burden；一旦产品对外发布，未来 schema migration policy 必须重新显式设计。
