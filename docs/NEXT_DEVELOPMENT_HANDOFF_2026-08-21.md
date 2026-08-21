# CampBoardGameHost 下一阶段开发交接 — 2026-08-21

> 当前路线权威：`CURRENT_DEVELOPMENT_ROADMAP.md`  
> 当前 rollout：**Production Semantic-History Foundation → New-game Global Observation Ownership Cutover**  
> 基线 `main`：`3db66482d9367c6b42a3f2550b979c28bfafea42`  
> Foundation PR：#24 `R6: establish production semantic-history foundation`（Draft）  
> Stacked next PR：#27 `R6: cut over new observations to global session authority`（Draft，base = #24 branch）  
> 状态时间：2026-08-21 17:21 Australia/Sydney

## 1. 当前最重要结论

R6 P1 semantic prerequisites 与 Post-P1 production audit 已完成。

当前 development chain 是：

```text
main
  ↓
PR #24 / codex/r6-semantic-history-foundation
  Production Semantic-History Foundation
  head: cdd3d7d300379c4e4a31ee000453a168188d1537
  ↓ stacked
PR #27 / codex/r6-global-observation-cutover
  New-game Global Observation Ownership Cutover
  semantic code checkpoint before handoff-doc commits:
  808168cf9f4ba8b39c80472181b5dc68a01ef0e1
```

**#27 不得在 #24 前合并。**

## 2. Active-game persistence policy 已定稿：v3-only

项目目前没有外部用户，因此已经明确放弃未发布旧 active-game save 的兼容性。

```text
v1 → UNSUPPORTED
v2 → UNSUPPORTED
v3 → ONLY SUPPORTED ACTIVE-GAME SCHEMA
```

必须区分：

```text
LEGACY_LOCAL
    = v3 内显式写入的 chronology mode

missing semanticHistoryMode
    != LEGACY_LOCAL
    = invalid / unsupported payload → fail closed
```

Clocktower v3 还必须显式包含已有的：

```text
clocktowerNextTimelineGlobalSequence
```

该 key 是唯一 durable global cursor；不得增加第二套 cursor representation，也不得从 `nightStepIndex` / `clocktowerEventCounter` / round 推断 global chronology。

## 3. PR #24 — Production Semantic-History Foundation

### 状态

```text
implementation: COMPLETE / STATICALLY AUDITED
CI validation: BLOCKED BY GITHUB-HOSTED RUNNER QUOTA
merge: NOT AUTHORIZED YET
PR state: DRAFT
head: cdd3d7d300379c4e4a31ee000453a168188d1537
```

### 已实现

- `ClocktowerSemanticHistoryMode`：`LEGACY_LOCAL` / `GLOBAL_V1`；
- `GameSnapshot.semanticHistoryMode`；
- `GameSnapshot.nextTimelineGlobalSequence` compatibility validation；
- active-game schema bump 到 v3；
- v1/v2 对所有 game kind unsupported；
- Clocktower v3 missing/null/unknown mode fail closed；
- Clocktower v3 existing cursor key required，missing/null/non-integer/negative fail closed；
- Global history / LegacyLocal binding mismatch fail closed；
- Global cursor 必须严格大于 committed Global observation position；
- save/restore wiring 复用 `ClocktowerNightCheckpoint.nextTimelineGlobalSequence`；
- Trouble Brewing v3 restore 必须有 immutable ruleset basis + matching RulesetRef；
- 删除真实 v1 Clocktower migration factory/tests；
- legacy helper 若仍因大 App 编译边界残留，只允许 fail-only/unreachable，不构成 compatibility promise。

### Tests-first red evidence

初始 semantic-history tests-only commit：

`4759c6ee95bbbae53f4b43412bf75b7ee4cf5768`

CI #308 的 Android unit-test compilation 曾按预期因为新 contract 不存在而失败，这是有效 tests-first red evidence。

### 仍未完成

只有 **有效 CI green gate** 尚未取得。不要把 runner 启动前失败当作代码 red 或 green。

## 4. GitHub Actions 阻塞原因已经确认

此前所有新的 Actions job 表现为：

```text
queued briefly
→ failure
steps = null
no checkout
no compiler/test logs
```

现在已经由账户 Billing 页面确认：

```text
GitHub Actions included minutes
2000 / 2000 used
```

因此当前 runner-start failure 的根因是 **GitHub-hosted Actions included minutes 已耗尽**，不是已知的 Kotlin / ASP / Clingo / workflow 代码失败。

在 Actions 额度恢复、增加 budget，或配置可用 self-hosted runner 前：

- 不再重复 rerun #24；
- #24 保持 Draft；
- 不宣称 CI green；
- trusted-writer workflow 也视为不可用，因为它同样需要 runner。

恢复 runner 后，#24 必须重新真实执行并通过：

```text
R2 main-thread boundary
Android unit tests + debug APK
ASP contract tests
real Clingo cross-validation
```

然后做 exact diff + review-thread audit，才可合并。

## 5. PR #27 — New-game Global Observation Ownership Cutover

#27 是 stacked Draft PR，base 直接指向 #24 branch。纯 semantic implementation 在 handoff 文档提交之前的 code checkpoint 是：

`808168cf9f4ba8b39c80472181b5dc68a01ef0e1`

该 SHA 用于区分“语义代码完成点”和之后仅更新交接文档产生的 branch-head 前移；恢复开发时应重新查询 #27 当前 head，而不是假设 branch head 仍等于此 SHA。

当前相对 #24 的 intended semantic diff 围绕四个 semantic/test 文件：

```text
app/src/main/java/.../clocktower/epistemic/EpistemicObservationDraft.kt
app/src/main/java/.../clocktower/session/ClocktowerGameSession.kt
app/src/test/java/.../clocktower/session/ClocktowerGlobalObservationCommitTest.kt
app/src/test/java/.../persistence/ClocktowerGlobalObservationProductionWiringTest.kt
```

另外包含本 handoff 文档作为当前开发 checkpoint。

### 5.1 新增 unbound observation draft

`EpistemicObservationDraft` 表示“刚刚展示/公开了什么”，但**没有 timeline binding**。

核心边界：

```text
producer / Host
    may describe observation content
    MUST NOT assign global identity
```

不得让 Host 先构造一个 `RecordedEpistemicObservation(Global(...))` 再交给 App。

### 5.2 `ClocktowerGameSession` 成为 Global observation semantic authority

已增加 Global observation atomic transition，语义要求：

```text
EpistemicObservationDraft
        ↓
ClocktowerGameSession authority
        ↓ atomically
TimelinePoint(globalSequence)
RecordedEpistemicObservation(Global)
nextTimelineGlobalSequence + 1
playerInputRevision + 1
```

已覆盖的 contract：

1. first Global observation 从 persisted cursor 分配；
2. private / public observations共用一个 global allocator namespace；
3. local `sequence` 可以不同，但不决定 global ordering；
4. restore 后从 persisted cursor 继续；
5. exact duplicate `recordId` 幂等，不消耗新 slot/revision；
6. same `recordId` + different content fail closed，且不 mutation；
7. `LEGACY_LOCAL` session 不允许调用 Global commit API；
8. `GLOBAL_V1` session 禁止直接注入 pre-bound durable record；
9. global cursor exhaustion 必须在 partial commit 前失败；
10. player-input revision exhaustion 也必须在 log/cursor mutation 前失败。

### 5.3 NGJ / multi-script 特别边界

审计发现：No Greater Joy production 当前 `clocktowerRulesetRef = null`，而 Trouble Brewing 有 advanced ruleset-ref persistence。

因此 production observation commit **不能**通过“为了调用 session，临时伪造完整 `GameSnapshot` / RulesetRef”实现，否则会把 Global cutover 意外做成 TB-only。

当前设计：

- 同一份 Global transition 由 `ClocktowerGameSession` 定义；
- real session instance 可调用；
- Compose adapter 也可调用 session companion/stateless transition；
- stateless return 只是 transient transition result；
- durable state 仍只有 observation log + playerInputRevision + **现有** global cursor key。

这不创建第二个长期 session authority，也不创建第二个 cursor persistence model。

## 6. #27 tests-first production wiring contract

已经新增 structural/red test，要求最终 production wiring 变成：

```text
ClocktowerHostScreen
    ↓ EpistemicObservationDraft
CampBoardGameHostApp
    ↓
ClocktowerGameSession global transition
    ↓
RecordedEpistemicObservation(Global)
+ clocktowerPlayerInputRevision
+ existing clocktowerNextTimelineGlobalSequence
```

同时要求新创建的 Clocktower game 切到：

```text
semanticHistoryMode = GLOBAL_V1
cursor = 0
empty observation log
```

注意：这是 **#27** 的目标，不是 #24 的 foundation contract。

## 7. 当前 production 仍未 wiring 的位置

截至本 checkpoint，以下仍保持旧 producer ownership：

### private observation

`ClocktowerHostScreen.recordReliablePrivateInformation(...)`

当前仍构造：

```text
RecordedEpistemicObservation
sequence = nightStepIndex
```

并通过 callback 交给 App。

### public observation

`CampBoardGameHostApp.addClocktowerEvent(...)`

Death / Execution public observation 当前仍使用：

```text
sequence = clocktowerEventCounter
```

### App durable list

App 当前仍直接维护：

```text
clocktowerEpistemicObservations
clocktowerPlayerInputRevision
clocktowerNextTimelineGlobalSequence
```

#27 下一 production patch 的任务就是让 private/public producer 都先生成 **draft**，然后统一经过 `ClocktowerGameSession` transition 分配 global identity。

## 8. 为什么当前没有继续改 App / Host 大文件

`CampBoardGameHostApp.kt` 和 `ClocktowerHostScreen.kt` 都是超大文件。

GitHub connector 当前没有 safe patch-hunk write API；直接 Contents API 更新意味着全文件替换，风险高。

过去使用的 trusted-writer workflow 可以做到：

- marker-count fail closed；
- only allowlisted file；
- narrow text replacement；
- focused tests green 才 commit。

但 trusted writer 依赖 GitHub Actions runner；当前 2000/2000 minutes exhausted，因此 writer 也被阻塞。

**决定：在安全 writer / runner 恢复前，不为了赶进度整文件覆盖 App/Host。**

## 9. 下一次恢复开发的精确顺序

### A. 先解决 runner availability

任选其一：

```text
GitHub Actions monthly minutes reset
OR small paid Actions budget
OR self-hosted runner
```

### B. 先完成 #24

1. 确认 #24 head 仍是预期 head；
2. 跑真实 R2；
3. 跑 Android tests + debug APK；
4. 跑 ASP contract tests；
5. 跑 real Clingo；
6. 修复任何**真实** compiler/test failure；
7. exact diff audit；
8. review threads = 0；
9. merge #24。

### C. 再整理 #27 stacking

1. #24 merge 后，把 #27 rebase/retarget 到 `main`；
2. 确认纯 semantic tests 编译/运行；
3. 使用 safe trusted writer 修改 App/Host；
4. new game → explicit `GLOBAL_V1`；
5. Host callback：`RecordedEpistemicObservation` → `EpistemicObservationDraft`；
6. private/public commit 都调用 `ClocktowerGameSession` Global transition；
7. write back observation log / playerInputRevision / existing cursor；
8. persistence round-trip / restore continuity test；
9. focused tests → full gate → exact audit；
10. #27 通过后才进入 Recommendation Entry-Point Unification。

## 10. #27 明确 non-goals

不要顺便做：

- historical action timeline capture；
- recommendation entry-point cleanup；
- Spy `VERIFIED_EXACT` production；
- authoritative physical Grimoire ledger；
- A3 historical multi-night engine；
- B4 production authority；
- ZDD promotion；
- second global cursor key；
- old save migration；
- TB-only fake RulesetRef for NGJ。

## 11. Rollout 顺序保持不变

```text
1. Production Semantic-History Foundation          #24
2. New-game Global Observation Ownership Cutover  #27
3. Production Recommendation Entry-Point Unification
4. Historical action + observation capture
5. A3 historical multi-night exact baseline
6. Authoritative physical Grimoire ledger
7. Spy production VERIFIED_EXACT
8. B4 historical expansion
9. Revision-driven recommendation unification
10. Reconsider ZDD promotion
```

## 12. 下一会话可直接使用的起始指令

```text
继续 CampBoardGameHost R6 rollout。先读 docs/CURRENT_DEVELOPMENT_ROADMAP.md 和 docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-21.md。当前 PR #24 semantic foundation code head 为 cdd3d7d300379c4e4a31ee000453a168188d1537，foundation implementation 已完成但 GitHub Actions 2000/2000 included minutes 已耗尽，因此所有新 job 都在 runner 启动前 steps=null 失败；这不是有效代码 red/green。PR #27 codex/r6-global-observation-cutover stacked 在 #24 上；其 handoff 文档提交前的 semantic code checkpoint 为 808168cf9f4ba8b39c80472181b5dc68a01ef0e1，已完成 EpistemicObservationDraft + ClocktowerGameSession atomic Global observation transition + tests-first production wiring contract，但尚未改 CampBoardGameHostApp.kt / ClocktowerHostScreen.kt。开始工作时先重新查询 #24/#27 当前 head。runner 恢复后先让 #24 真正通过 R2 + Android/APK + ASP + real Clingo 并合并，再 retarget/rebase #27，使用 safe trusted writer 做 App/Host production cutover。不要提前进入 recommendation/Spy/A3/B4/ZDD，不新增第二 cursor，不恢复 v1/v2 save migration。
```
