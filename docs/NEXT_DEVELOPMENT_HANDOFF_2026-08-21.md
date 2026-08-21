# CampBoardGameHost 下一阶段开发交接 — 2026-08-21

> 当前路线权威：`CURRENT_DEVELOPMENT_ROADMAP.md`  
> 当前阶段：R6 P1 CLOSED；post-P1 production-rollout entry audit **COMPLETE**  
> 下一 source slice：**Production Semantic-History Foundation**  
> 技术审计证据：`post_p1_production_rollout_entry_audit_2026-08-21.md`

## 1. 当前状态

已经完成：

```text
R5.5 production flow foundation        CLOSED / MERGED
R6 P1.1 Spy truth prerequisite         PASS
R6 P1.2 timeline prerequisite          PASS
R6 P1.3 knowledge-safe boundary        PASS
R6 P1 semantic prerequisites           CLOSED
Post-P1 rollout entry audit            COMPLETE
```

不要再重复执行旧 handoff 中的“先做 entry audit”步骤。最新 audit 已经确认 production authority map 和 dependency order。

当前仍未授权：

- broad production Host/Compose wiring；
- production Spy `VERIFIED_EXACT`；
- production historical multi-night Possible Worlds；
- B4 production authority；
- ZDD promotion；
- legacy save 自动推断为 Global chronology。

## 2. Entry audit 的核心发现

### 2.1 正确 Global timeline/session authority 已经存在

`ClocktowerGameSession` / `GameSnapshot` 已经提供：

```text
epistemicObservationLog
nextTimelineGlobalSequence
allocateTimelinePoint(...)
recordEpistemicObservation(...)
```

但 production 当前仍主要由 App/Compose 管理：

```text
private observation sequence → nightStepIndex
public observation sequence  → clocktowerEventCounter
observation collection       → mutable App/Compose list
active-game persistence      → no explicit semantic-history mode/cursor
```

因此不要重新设计 timeline model。下一步是建立 production ownership/persistence seam。

### 2.2 Spy 仍不能安全升级 VerifiedExact

Host 当前 Spy Grimoire producer 仍是 Legacy display-only。现有 actual/shown role、red-herring、poison state 等只是碎片，并不构成一份 durable authoritative physical Grimoire ledger。

旧 save 还存在 poison confirmed-target compatibility fallback，因此旧持久化数据尤其不能被自动提升为 exact `POISONED` reminder truth。

所以 Spy VerifiedExact **不是第一 rollout slice**。

### 2.3 A3/ZDD/B4 不是完整 historical engine

```text
A3  = exact baseline, not general historical transition engine
ZDD = exact shadow/prototype
B4  = isolated historical shadow with deferred action classes
```

shared chronology support 不等于 production historical multi-night reasoning。

### 2.4 legacy direct recommendation button 是 authority debt

实机测试确认 recommendation UI 仍保留 legacy direct entry path。

这不是简单 label/UI polish。随着 revision/context/history 丰富，它可能绕过统一 coordinator 并产生与新 path 不一致的答案。

因此已建立后续独立阶段：**Production Recommendation Entry-Point Unification**。

它位于：

```text
semantic-history/session ownership stable
        ↓
Recommendation Entry-Point Unification
        ↓
historical recommendation semantics expansion
```

不要在第一个 semantic-history foundation PR 中顺手改它。

## 3. 下一步唯一目标：Production Semantic-History Foundation

第一 source PR 只建立显式 history mode + durable cursor/session persistence contract。

概念模型：

```text
ClocktowerSemanticHistoryMode
├── LEGACY_LOCAL
└── GLOBAL_V1
```

名字可以在实现 review 中调整；不要为了名字扩大设计范围。

### 必须先写的 failing tests

至少覆盖：

1. existing v1/v2 payload missing mode → restore `LEGACY_LOCAL`；
2. legacy local observations 不被推断/重编号成 Global；
3. explicit `GLOBAL_V1` mode 可以持久化/恢复；
4. Global mode 持久化/恢复 `nextTimelineGlobalSequence`；
5. restore cursor 必须严格大于所有 committed global positions；
6. Global mode + `LegacyLocal` observation → fail closed；
7. unknown/null semantic-history mode → fail closed；
8. incompatible partially migrated payload → fail closed；
9. persistence round-trip 保持 mode/cursor/observation binding；
10. foundation 完成后现有 production behavior 仍保持 Legacy，不发生 Host/Compose 行为切换。

### 预期最小 source 范围

优先审计/修改：

- active-game persistence model/coordinator；
- semantic persistence codec/model；
- `GameSnapshot` / `ClocktowerGameSession` restore contract；
- 对应 JVM persistence/session tests。

只有确实需要时才增加一个很小的 typed history-mode model。

### 明确 non-goals

第一 PR 不做：

- Host observation callback Global cutover；
- Compose state architecture refactor；
- Spy VerifiedExact；
- physical Grimoire ledger；
- A3 historical state transitions；
- B4 productionization；
- ZDD promotion；
- recommendation UI / legacy direct button removal；
- revision-engine broad refactor；
- new-game automatic Global activation。

## 4. Persistence / migration 设计原则

最重要的是**不猜历史**。

```text
old save
    missing semantic-history mode
        ↓
LEGACY_LOCAL
```

不得：

```text
nightStepIndex / round / eventCounter
        ↓ guess
synthetic globalSequence
```

对 Global 模式：

```text
explicit mode
+ explicit Global bindings
+ durable allocator cursor
        ↓
restore same chronology authority
```

如果 payload 处在无法证明一致的中间状态，正确行为是 fail closed，而不是“尽量恢复”。

## 5. Foundation 后的 rollout dependency order

不要一次性实施，顺序是：

```text
1. Semantic-History Foundation
2. New-game Global observation ownership cutover
3. Recommendation Entry-Point Unification
4. Historical action + observation capture
5. A3 historical multi-night exact baseline
6. Authoritative physical Grimoire ledger + Spy VerifiedExact
7. B4 historical expansion
8. Revision-driven recommendation unification
9. ZDD reconsideration
```

### 为什么 recommendation button 放在第 3 步

太早处理会因 session ownership 变化而返工；太晚处理则会让 historical/revision-aware recommendation 与 legacy direct path 并存。

所以最合适的窗口是：

> **底层 production semantic ownership 稳定以后，推荐算法继续扩张以前。**

## 6. Deferred Decisions，不要提前实现

- `globalSequence` 当前**不进入** `PlayerWorldSetIdentity`；只有 timeline position 改变 world semantics 时才重新审计。
- `StoryDisruptionRisk` threshold 暂不设定；等真实游戏样本足够且 recommendation policy 准备消费。
- `MANUAL_ONLY` / `INELIGIBLE` 文案在 Recommendation Entry-Point Unification 阶段处理。
- advanced multi-script Possible Worlds 继续 TB-first；R5.5 multi-script catalog/flow foundation 已验证，不重新建设。
- ZDD 保持 shadow；等 exact historical baseline + realistic device gate。

## 7. 开发执行顺序

从最新 `main` 创建新的短生命周期 source branch，不要继续使用：

```text
codex/storyteller-algorithm-v4
codex/r6-p1-1-closeout-docs
codex/r6-design-doc-cleanup
```

建议 branch 名：

```text
codex/r6-semantic-history-foundation
```

每个 rollout PR 固定执行：

```text
1. confirm latest main/head
2. exact baseline audit
3. failing tests first
4. smallest implementation
5. focused tests
6. R2 main-thread boundary
7. Android unit tests + debug APK
8. ASP contract tests
9. real Clingo cross-validation
10. exact diff audit
11. final correctness + review-thread audit
12. merge
```

如果 review 发现 semantic hole：

```text
regression test first
→ fix
→ rerun all gates
→ re-audit final diff
```

## 8. Production Host / Compose guardline

第一 semantic-history foundation slice 中：

**不要修改 production Host/Compose。**

特别禁止顺手：

- 把 private/public observation producers 改成 Global；
- 删除 legacy recommendation button；
- 把 Spy producer 改成 VerifiedExact；
- 把 A3/ZDD/B4 设为新的 recommendation authority；
- 把旧 game/save 自动迁移到 Global。

这些都有后续明确阶段。

## 9. 下一会话可直接使用的起始指令

```text
继续 CampBoardGameHost。先确认最新 main/head，读取 CURRENT_DEVELOPMENT_ROADMAP.md、post_p1_production_rollout_entry_audit_2026-08-21.md 和 NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md。Post-P1 entry audit 已完成，下一目标是 Production Semantic-History Foundation。先审计 active-game persistence、GameSnapshot/ClocktowerGameSession allocator restore contract 和现有 persistence tests，然后 tests-first 建立 explicit LEGACY_LOCAL / GLOBAL_V1 history-mode + durable cursor contract。第一 PR 不改 production Host/Compose，不切 Global producer，不改 Spy/recommendation/A3/ZDD/B4 authority。
```

## 10. 最终状态摘要

```text
R6 P1 semantic prerequisites            CLOSED
Post-P1 production entry audit          COMPLETE
Next source slice                       SEMANTIC-HISTORY FOUNDATION
Production Host/Compose cutover         NOT IN FIRST SLICE
Recommendation legacy direct path       REQUIRED FOLLOW-UP
Historical multi-night Possible Worlds  NOT YET AUTHORIZED
Spy VERIFIED_EXACT production            NOT YET AUTHORIZED
ZDD production                           SHADOW ONLY
```
