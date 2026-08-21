# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-21  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 审计基线：`main@46ec09c22f0e991cb4ad2a6a51e493c751255f1e`  
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
```

**当前执行点：下一步不是 broad production Host wiring，而是 `Production Semantic-History Foundation`。**

这个第一 rollout slice 只建立显式 semantic-history mode、timeline cursor/session ownership 与 persistence/restore contract。它应 tests-first，并继续保持 **production Host/Compose 行为不变**。

当前仍未授权：

- production Spy `VERIFIED_EXACT` cutover；
- production historical multi-night Possible Worlds；
- B4 production authority；
- ZDD production promotion / `ZDD_DEVICE_VALIDATED`；
- legacy save 通过猜测 local sequence 自动升级为 Global chronology。

## 2. 当前阶段状态

| 阶段 | 状态 | 当前含义 |
|---|---|---|
| A0 / A1 / A1.1 | PASS | unified semantic model 与 player-knowledge boundary 成立。 |
| A2 ASP Oracle | PASS | typed/fail-closed oracle contract 已建立。 |
| A2.1 Golden corpus | PASS | **52 total semantic contracts；24 当前 oracle/A3 executable；28 deferred / `ORACLE_NOT_APPLICABLE`。** 52 是 corpus size，不应解释成“52 个都已由 Clingo 执行”。 |
| A3 EnumeratedWorldSet | PASS / exact baseline | 当前 exact correctness baseline；尚不是完整 historical state-transition engine。 |
| A4 ZDD | PASS AS EXACT SHADOW | differential correctness 成立；device latency/memory gate 未通过，不得驱动 production decisions。 |
| A4.5 cache/lifecycle | PASS | observation cache rebuild/durability boundary 已验证。 |
| R5.5 | CLOSED / MERGED | production Clocktower/Werewolf flow planner cutover 与 multi-script structural foundation 已完成。 |
| R6 P1.1 | PASS | VerifiedExact Grimoire semantic contract 已完成；production producer 仍 Legacy。 |
| R6 P1.2 | PASS | Global action/observation chronology contract 已完成；production ownership 尚未切换。 |
| R6 P1.3 | PASS | world/knowledge safe-core 不再接收完整 storyteller truth。 |
| R6 P1 | CLOSED | 三个 semantic prerequisite 全部 PASS。 |
| Post-P1 production-rollout entry audit | **COMPLETE** | production authority map、migration risk 与 rollout dependency 已确认。 |
| **Production Semantic-History Foundation** | **NEXT** | explicit history mode + durable global cursor/session ownership；第一片不改 Host/Compose 行为。 |
| Production Recommendation Entry-Point Unification | REQUIRED FOLLOW-UP | semantic-history/session ownership稳定后、扩大 recommendation 语义前必须移除 legacy direct recommendation path。 |
| Historical multi-night engine | NOT AUTHORIZED | 先建立 production history ownership，再扩展 A3/B4 historical semantics。 |
| Spy VerifiedExact production | NOT AUTHORIZED | 等 authoritative durable physical Grimoire ledger。 |
| ZDD production promotion | NOT AUTHORIZED | 等 exact multi-night baseline + realistic device gate。 |

## 3. 已确认的长期架构边界

### 3.1 Multi-script foundation：结构层已验证，advanced semantics 仍 TB-first

R5.5 已通过 Trouble Brewing + No Greater Joy 证明以下 seam 是真实 multi-script foundation：

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

因此未来不得因为 advanced semantics 仍 TB-first 就重建第二套 catalog/flow framework。

当前准确边界是：

```text
catalog / normalization / registry / flow / ruleset identity
    → MULTI-SCRIPT VERIFIED

recommendation metadata / Possible Worlds / role-specific epistemic semantics
    → TB-FIRST / EXPAND LATER
```

### 3.2 Flow authority 与 UI adapter 必须分离

R5.5 删除的是独立 legacy flow-order / eligibility authority，不是机械删除所有旧 adapter。

允许保留纯 adapter，但它们不得重新决定：

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

完整证据见：`post_p1_production_rollout_entry_audit_2026-08-21.md`。

### 4.1 Timeline/session authority 已存在，但 production 还没有使用它

正确 semantic authority 已经存在：

```text
ClocktowerGameSession
    ├── GameSnapshot.epistemicObservationLog
    ├── GameSnapshot.nextTimelineGlobalSequence
    ├── allocateTimelinePoint(...)
    └── recordEpistemicObservation(...)
```

但 production 当前仍分散使用：

```text
private observation → nightStepIndex
public observation  → clocktowerEventCounter
observation storage → App/Compose mutable list
active save         → 未持久化 semantic global cursor/mode
```

因此下一步不是再设计一个 timeline model，而是安全地把 production ownership 迁到现有 session authority。

### 4.2 Spy production truth 仍不足以升级 VerifiedExact

当前 Host 的 Spy Grimoire producer 仍直接从 player card 组装 Legacy `GrimoireState`，没有经过 `SpyGrimoireTruthProjector`。

虽然 production 已持久化 actual/shown role、red herring、poison target 等碎片状态，但仍不存在一份 durable authoritative physical Grimoire ledger，能够完整重建：

- physical character token per seat；
- `IS THE DRUNK`；
- `RED HERRING`；
- current physical `POISONED`；
- 其他 visible reminder placements。

旧 save 的 poison compatibility fallback 也意味着旧数据不能自动提升成 exact physical reminder truth。

**结论：Spy VerifiedExact 不是第一个 rollout slice。**

### 4.3 A3 / ZDD / B4 仍不能被误称为 production historical engine

- A3：exact enumerated baseline，但不是通用 multi-night state-transition replay engine；
- ZDD：exact shadow/prototype，production selector 不读取它；
- B4：isolated shadow，重要 Attack / Protect / RoleChange historical classes 仍 deferred。

```text
shared chronology support
!=
historical multi-night semantic engine
```

## 5. 下一步：Production Semantic-History Foundation

第一 source PR 应只建立 production history/persistence contract，概念上引入：

```text
ClocktowerSemanticHistoryMode
├── LEGACY_LOCAL
└── GLOBAL_V1
```

类型名可在实现审查中调整，但必须满足以下 contract：

1. 旧 v1/v2 save 缺少 mode → 明确恢复为 `LEGACY_LOCAL`；
2. 绝不从 legacy local sequence 推断 Global chronology；
3. `GLOBAL_V1` 必须显式持久化；
4. Global mode 持久化并恢复 `nextTimelineGlobalSequence`；
5. restore 后 cursor 必须大于所有 committed global positions；
6. Global history 中出现 `LegacyLocal` observation → fail closed；
7. unknown / explicit-null / incompatible mixed payload → fail closed；
8. **第一 foundation PR 不把现有或新 production game 自动切为 Global**，避免行为变化；
9. 不修改 Spy truth、A3/ZDD/B4 authority、Host flow、Compose UI 或 recommendation UI。

因此第一片仍可满足：

```text
tests first
→ persistence/session contract
→ no Host/Compose behavior change
```

## 6. 已确定的后续 rollout 顺序

```text
1. Production Semantic-History Foundation
   explicit mode + durable cursor/session ownership contract

2. New-game Global observation ownership cutover
   从第一条 committed semantic event 开始使用 session allocator

3. Production Recommendation Entry-Point Unification
   移除 legacy direct recommendation button/path
   所有推荐入口只走一个 recommendation authority/coordinator

4. Historical action + observation capture
   action 与 observation 共用同一 global allocator namespace

5. A3 historical multi-night exact baseline
   tests-first 扩展真实 state-transition semantics

6. Authoritative physical Grimoire ledger
   然后才允许 production Spy VERIFIED_EXACT cutover

7. B4 historical expansion
   补齐 deferred action/state classes 后再讨论 production authority

8. Revision-driven recommendation unification
   session ownership稳定后统一 recompute/context ownership

9. Reconsider ZDD promotion
   exact multi-night baseline + realistic device gate 全部通过后才重开
```

这是 dependency order，不代表所有阶段必须是一个大版本；每一步继续使用短生命周期 tests-first PR。

## 7. Recommendation UI：legacy direct 按钮的正式定位

2026-08-21 实机测试确认，当前 recommendation/presentation 仍存在 transitional legacy direct entry path。

它不是简单的按钮文案问题，而是潜在双 decision authority：

```text
new recommendation path
    → revision/context/coordinator

legacy direct button/path
    → 可能绕过部分统一 context / revision / eligibility
```

随着 poison/protection/alive state、observation history 和 historical revision 进入推荐语义，两条入口可能给出不同答案。

因此建立独立阶段：**Production Recommendation Entry-Point Unification**。

### Trigger

```text
semantic-history/session ownership foundation 已稳定
        ↓
在 recommendation 扩大到 historical multi-night semantics 之前完成
```

### Exit criteria

1. production 中不存在绕过统一 recommendation authority 的按钮/入口；
2. UI 不自行重新计算 recommendation semantics；
3. revision/context 变化后 stale result 不可通过 legacy path 复用；
4. `MANUAL_ONLY` / `INELIGIBLE` / unavailable 由同一 result model 驱动；
5. regression test 防止 legacy direct path 回归。

不要只隐藏 `legacy` label，也不要把这个工作伪装成 R5.5 hotfix。

## 8. Deferred Decisions / Reopen Triggers

以下事项必须被追踪，但不应提前实现：

| Decision | 当前决定 | 何时重新打开 |
|---|---|---|
| `globalSequence` 是否进入 `PlayerWorldSetIdentity` | **NO** | 只有当 timeline position 本身会改变 world constraints / cache identity 时 tests-first 重审。 |
| `StoryDisruptionRisk` thresholds | UNSET | 累积足够真实游戏样本、且 recommendation policy 准备消费该维度时。 |
| `MANUAL_ONLY` vs `INELIGIBLE` UI wording | DEFERRED | Production Recommendation Entry-Point Unification 阶段。 |
| Revision-driven recommendation full unification | PARTIAL | semantic-history/session ownership完成后，并在复杂 multi-night/多剧本 recommendation 扩张前。 |
| Spy `VERIFIED_EXACT` production | LEGACY ONLY | durable authoritative physical Grimoire ledger 可完整 restore 后。 |
| ZDD production promotion | SHADOW ONLY | historical exact baseline 稳定 + realistic device latency/memory gate 通过后。 |
| Advanced multi-script Possible Worlds | TB-FIRST | TB historical exact baseline稳定后，以第二剧本做独立 structural/semantic proof；不得重建 catalog/flow foundation。 |

“当前决定为 NO”与“尚未决定”必须区分。例如 `globalSequence` 当前明确**不进入** `PlayerWorldSetIdentity`；不是待办事项，除非 trigger 成立。

## 9. Production guardlines

在后续路线明确修改前：

```text
Production Clocktower flow order: planner-backed
Production Werewolf flow order: planner-backed
Legacy UI adapters: only as non-authoritative adapters
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
B4: isolated shadow only
R6 P1: CLOSED
Post-P1 entry audit: COMPLETE
Production semantic-history mode/cursor cutover: FOUNDATION NEXT
Production Global producer cutover: NOT YET WIRED
Production VerifiedExact Spy producer: NOT AUTHORIZED
Production historical multi-night Possible Worlds: NOT AUTHORIZED
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
```

禁止：

- 从 legacy local sequence 猜 global identity；
- 从 legacy Grimoire/空 reminder/UI label 猜 `VERIFIED_EXACT`；
- 把 timeout/OOM/cap 当 UNSAT；
- 截断 exact worlds 后仍声称 exact；
- 把 storyteller-only truth 放入 player knowledge；
- 让 shadow result 驱动 production decision；
- 为新剧本重建第二套 catalog/flow framework；
- 在第一 semantic-history foundation PR 中顺便修改 Host/Compose/recommendation UI。

## 10. 2026-08-22 实战验证

实战仍用于发现真实 runtime/UX 问题，但分类必须保持：

- rules / flow order / persistence / state correctness defect → correctness follow-up；
- recommendation decision-entry / presentation issue → Recommendation Entry-Point Unification / migration backlog；
- preference / cosmetic polish → 不打断 rollout correctness work。

最高价值观察包括：TB/NGJ night-flow continuity、Scarlet Woman / Imp succession、Mayor / Ravenkeeper / Undertaker / Sage conditional flow、navigation/back/restore，以及 recommendation 双路径是否造成实际摩擦。

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

Review 发现 semantic hole：

```text
regression test first
→ fix
→ rerun gates
→ exact final diff audit
```

不要在 `main` 直接开展 source work；从最新 `main` 创建新的短生命周期 branch。

## 12. 关键历史证据

- R5.5 merge：`7add8569e2484a350f6cf1512a730e9f4db469c5`
- P1.3 safe-input PR #7/#8：`19b91887344655285ec8bd93ca5bdb51bcfff445` / `8f5ccc551948fea085caf8df3eb100ef67eae438`
- P1.1 VerifiedExact semantic close / PR #21 merge：`f77338bc85ae4a81b7e54e456b430e2f7f35c51a`
- P1 docs closeout / PR #22 merge：`d56edd1552dc25dc73574c311179b9fe5a9d216b`
- Design-plan audit addition：`46ec09c22f0e991cb4ad2a6a51e493c751255f1e`

详细证据保留在：

- `r5_5_stage_close_known_limitations_2026-08-21.md`
- `r6_p1_1_closeout_2026-08-21.md`
- `r6_p1_2_closeout_2026-08-21.md`
- `r6_p1_3_closeout_2026-08-21.md`
- `design_plan_audit_2026-08-21.md`
- `post_p1_production_rollout_entry_audit_2026-08-21.md`

## 13. 文档维护规则

1. **只有本文件维护“当前执行点 / 当前阶段状态”。**
2. 专项设计、audit、closeout、handoff 保存证据与上下文，不创建第二个并列 roadmap。
3. 历史 handoff/closeout 若正文包含已失效的“下一步”指令，保留历史正文，但在顶部加 `SUPERSEDED` / historical-status banner。
4. 新 handoff 必须指向本 roadmap，并不得覆盖本 roadmap 的 status authority。
5. 如果专项文档与本文件的当前状态冲突，以本文件为准；如果是规范语义冲突，则回到对应 normative design spec 做显式评审，不靠 handoff 猜测。
