# CampBoardGameHost 下一阶段开发交接 — 2026-08-21

> 当前阶段：R6 P1 semantic prerequisites CLOSED  
> 当前 production 基线必须包含：PR #21 merge `f77338bc85ae4a81b7e54e456b430e2f7f35c51a`  
> 当前文档分支：`codex/r6-p1-1-closeout-docs`  
> 当前路线权威：`CURRENT_DEVELOPMENT_ROADMAP.md`  
> P1.1 收尾：`r6_p1_1_closeout_2026-08-21.md`  
> P1.2 收尾：`r6_p1_2_closeout_2026-08-21.md`  
> P1.3 收尾：`r6_p1_3_closeout_2026-08-21.md`

## 1. 当前结论

R6 P1 的三个语义前置条件已经完成：

```text
P1.1 Spy Grimoire truth boundary             PASS
P1.2 Timeline identity semantic prerequisite PASS
P1.3 Knowledge-safe input boundary           PASS
```

这意味着正式 production rollout 需要的关键**语义边界**已经具备，但不意味着 production multi-night Possible Worlds 已经启用。

当前仍然成立的 guardline：

- production Host/Compose 尚未切换为 VerifiedExact Spy Grimoire producer；
- production Host/Compose 尚未从游戏第一条 committed semantic event 开始统一使用 Global timeline allocator；
- legacy running/saved games 不允许通过推断 local sequence 的方式升级为 Global chronology；
- `TroubleBrewingWorldEnumerator` 仍不是完整 historical multi-night state engine；
- B4 仍是 isolated shadow，production recommendation selection 不依赖它；
- recommendation-information UI migration 仍是独立 known limitation，不应与本轮 rollout audit 混成一个大改动。

**下一个开发动作不是接 production Host，而是 post-P1 production-rollout entry audit。**

## 2. 开始下一阶段前必须先做的基线确认

文档 PR 合并后，新会话第一步必须重新读取最新 `main`，确认：

1. `main` ancestry 包含 PR #21 merge：
   - `f77338bc85ae4a81b7e54e456b430e2f7f35c51a`
2. `main` 已包含：
   - `CURRENT_DEVELOPMENT_ROADMAP.md` 的 P1 CLOSED 更新；
   - `r6_p1_1_closeout_2026-08-21.md`；
   - `NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md`。
3. 从该最新 `main` 新建新的短生命周期 audit/development branch。
4. 不要继续使用旧的 `codex/storyteller-algorithm-v4` 长分支。
5. 不要在 docs-only closeout branch 上开始 production implementation。

如果 `main` 在文档 merge 后又有其他提交，必须以最新 `main` 为基线重新审计，不要机械复用本交接文档中的旧行号或调用点。

## 3. Post-P1 production-rollout entry audit 的目标

这一步只回答一个问题：

> **现有 production runtime 与已经完成的 P1 semantic contracts 之间，究竟缺哪些 authority / persistence / lifecycle seams，最小安全 rollout 顺序是什么？**

Audit 本身优先保持 read-only。除非发现一个独立、显然错误、且必须先修的测试基础设施问题，否则不要在 audit 过程中顺手修改 production Host/Compose。

最终应形成：

```text
production producer / commit point
        ↓
semantic authority
        ↓
persistence / restore
        ↓
knowledge/world consumer
        ↓
production decision/display path
```

并明确每一层当前是：

```text
AUTHORITATIVE
COMPATIBILITY / LEGACY
SHADOW ONLY
MISSING
```

## 4. Audit A — Spy Grimoire production truth authority

### 4.1 必须定位现有 production producer

找出所有 production `InformationProposition.GrimoireState` / Spy Grimoire observation 的创建点，并区分：

- Host/runtime producer；
- compatibility/test producer；
- `SpyGrimoireTruthProjector` VerifiedExact producer。

已知边界：当前 production Host producer 仍属于 `LEGACY_DISPLAY_ONLY`。Audit 必须重新从最新 `main` 证明这一点，而不是只依赖文档结论。

### 4.2 对每个 physical truth field 追 authority

至少逐项追踪：

- expected seat roster；
- physical character token per seat；
- alive state；
- Drunk physical Townsfolk token；
- `IS THE DRUNK` placement；
- Fortune Teller `RED HERRING` placement；
- Poisoner current `POISONED` placement；
- 其他可见但当前 evaluator 不精确解释的 reminder placements。

每项必须回答：

```text
Who authors it?
When is it committed?
Is it durable?
Can restore reproduce it exactly?
Can caller/UI state mutate it after validation?
Does legacy state omit it?
```

### 4.3 禁止的 shortcut

不得：

- 从 UI label 反推 mechanical reminder identity；
- 把当前 `shownRole` 自动当成任意时刻的 physical character token；
- 把空 reminder list 当成“确定没有 reminder”；
- 从不完整 `GameState` 拼出 `VERIFIED_EXACT`；
- 为了接 production 而放宽 projector 的 complete-roster / rule-backed validation。

如果 production authority 不完整，正确结论是继续保持 Legacy，而不是降级 VerifiedExact 的定义。

## 5. Audit B — Global timeline production ownership

### 5.1 找出所有 committed semantic event

至少覆盖：

- action facts；
- public observations；
- private observations；
- storyteller-committed information observations；
- restore 后继续产生的新事件。

### 5.2 对每个 event 找 allocation / persistence path

需要证明：

- 当前 production 是否调用 `ClocktowerGameSession.allocateTimelinePoint(...)`；
- 哪些路径仍依赖 phase / round / local sequence；
- allocator cursor 是否与对应 game/session lifecycle 一致持久化；
- restore 后是否可能重用已提交 global sequence；
- action 与 observation 是否可以真正共享同一条 global chronology；
- 是否存在先 commit semantic fact、后补 timeline identity 的路径。

### 5.3 rollout guardline

新的 Global mode 必须从一个**新游戏的第一条需要 timeline identity 的 committed semantic event** 开始。

不得：

- 给 legacy observation 猜 global sequence；
- 把已有 local order 批量映射为“看起来合理”的 Global order；
- 让一个 non-empty history 同时存在 Global action 与 LegacyLocal observation 后再由 consumer 猜 interleaving。

如果 production 不能保证从游戏开始统一进入 Global mode，应先设计 explicit session/history mode boundary，而不是直接把 allocator 调用塞进若干 Host callback。

## 6. Audit C — Historical engine / consumer readiness

必须重新确认各层真实能力，避免把 semantic plumbing 误称为 historical reasoning：

### A3 / EnumeratedWorldSet

确认：

- 初始 world construction 仍是什么时间语义；
- 当前 filtering 是否只看 proposition/current mechanical dimensions；
- 是否真正 replay historical state transition。

### ZDD

确认：

- 与 A3 共享哪些 chronology/filtering seam；
- 当前仍是 exact shadow 还是 production authority；
- 是否有任何 production selector 读取其结果。

### B4

确认：

- `ActionFactTimeline` / observation chronology 如何进入 request；
- reducer/materialization 是否仍主要用于 shadow differential；
- 是否真正形成跨多夜 historical player-world state。

Audit 输出必须明确：

```text
shared chronology support
!=
historical multi-night semantic engine
```

不要因为 P1.2 已 PASS 就默认 evaluator 已经 time-aware。

## 7. Audit D — Persistence / migration / lifecycle

需要建立一张 persistence matrix，至少包括：

| State | New Global/Exact capable? | Legacy compatible? | Restore authority | Fail-closed condition |
|---|---|---|---|---|
| active ruleset identity | | | | |
| reminder-token metadata identity | | | | |
| Grimoire truth binding | | | | |
| observation timeline binding | | | | |
| formal action timeline binding | | | | |
| timeline allocator cursor | | | | |

重点检查：

- schema-v2 compatibility 是否依赖 missing-field => Legacy；
- explicit null / unknown 是否 fail closed；
- content hash 改变后的 built-in save 行为；
- partially migrated save 是否有被 production UI 意外恢复成“新模式”的可能；
- Activity/process recreation 是否保持同一 semantic mode 和 allocator state。

## 8. Audit E — Production rollout seam selection

Audit 完成前不要预设“下一步一定是把 Host 全接上”。

应比较候选最小 slice，例如：

- explicit new-game semantic-history mode；
- one authoritative committed timeline event path；
- one durable physical Grimoire truth authoring seam；
- one producer-only migration with consumers still shadow；
- one persistence boundary before any UI adoption。

选择标准：

1. tests-first 可以写出失败测试；
2. 只改变一个 authority；
3. legacy compatibility/fail-closed 行为清楚；
4. 不需要同时修改 Host flow、Compose UI、world evaluator、persistence 四层；
5. rollback 后不会改变既有实战行为；
6. 能独立通过 R2 / Android / ASP / real Clingo gates；
7. review 能够用 exact diff 解释，不靠“整体应该没问题”。

## 9. 下一步实施方法

Audit 结束后，先提交一份**最小 slice proposal**，内容至少包括：

```text
Observed current authority
Missing seam
Why this is the first rollout dependency
Failing tests to add first
Minimal source files expected to change
Explicit non-goals
Persistence compatibility
Rollback behavior
Required gates
```

然后才开始 tests-first implementation。

### 每个 production rollout PR 的固定顺序

```text
1. baseline / exact diff audit
2. failing contract/regression tests
3. smallest implementation
4. focused tests
5. R2 main-thread boundary
6. Android unit tests + debug APK
7. ASP contract tests
8. real Clingo cross-validation
9. exact diff audit
10. final correctness review / inline-thread audit
11. merge
```

如果 review 发现语义 hole：

```text
add regression test first
→ fix
→ rerun all gates
→ re-audit final diff
```

不要在 review 后直接补 implementation 而没有对应测试。

## 10. Production Host / Compose 禁区

在 entry audit 完成并选定最小 tests-first slice 前：

**不要修改 production Host/Compose。**

尤其不要一次性：

- 把所有 Host observations 改成 Global；
- 把 Spy producer 直接改成 VerifiedExact；
- 把 A3/ZDD/B4 其中一个 shadow consumer 直接设为 recommendation authority；
- 重写 save schema；
- 顺便重构 recommendation UI；
- 把 legacy games 自动迁移到新 semantic mode。

目标是逐个 authority cutover，而不是“大接线”。

## 11. 已知独立事项，不要混入第一个 rollout slice

### Recommendation-information UI migration

仍是 known limitation：later-night information / manual mode 等 UI/presentation path 尚未完全统一。

它不属于 P1 semantic closeout，也不应因为现在进入 production rollout audit 就顺手修。

### 2026-08-22 real-game field validation

实战发现应分类：

- 核心 rules/flow/persistence/state correctness defect：可作为高优先级回归修复；
- recommendation UX / presentation issue：进入独立 UI migration backlog；
- preference/polish：不打断 rollout correctness work。

## 12. 下一会话推荐起始指令

可直接使用：

```text
继续 CampBoardGameHost。先读取 CURRENT_DEVELOPMENT_ROADMAP.md、r6_p1_1_closeout_2026-08-21.md 和 NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md，确认最新 main/head 与 docs closeout merge 已落地。R6 P1.1/P1.2/P1.3 已 PASS，但 production rollout 尚未授权。先做 post-P1 production-rollout entry audit：逐项追 Spy Grimoire truth producer、Global timeline production ownership、historical engine consumer、persistence/migration/lifecycle 边界。先给出 current authority map 与最小 tests-first rollout slice，不要提前修改 production Host/Compose。
```

## 13. 最终状态摘要

```text
R5.5 production flow foundation        CLOSED / MERGED
R6 P1.1 Spy truth prerequisite         PASS
R6 P1.2 timeline prerequisite          PASS
R6 P1.3 knowledge-safe boundary        PASS
R6 P1 semantic prerequisites           CLOSED
Production multi-night rollout         NOT YET AUTHORIZED
Next action                            ENTRY AUDIT
```
