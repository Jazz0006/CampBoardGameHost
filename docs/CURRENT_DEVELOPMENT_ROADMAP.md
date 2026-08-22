# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-22  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 最近已验证 source baseline：`5bbb607ae408d5d9d25812825200304054a7aced`（PR #27 merge commit）  
> 当前开发重点：**Storyteller Information Decision Unification — Foundation**  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> 当前专项设计：`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`  
> 当前交接：`NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md`

> 注意：本文件所在的 docs-only commit 会使 live `main` 晚于上面的 source baseline。任何新会话开始实施前，都必须重新查询 live `main`；不得把上面的 SHA 当作永远不变的 HEAD。

## 1. 当前结论

Phase A、R5.5、R6 P1 prerequisites、Production Semantic-History Foundation、Impaired Information Semantics、New-game Global Observation Ownership 均已完成并进入 `main`。

```text
Phase A correctness foundation                    PASS
R5.5 Script & Dynamic Flow Foundation             CLOSED / MERGED
R6 P1.1 Spy Grimoire truth boundary               PASS
R6 P1.2 Global timeline semantics                 PASS
R6 P1.3 Knowledge-safe input boundary             PASS
R6 P1 semantic prerequisites                      CLOSED
PR #28 Drunk/Poison mechanical correctness        CLOSED / MERGED
PR #24 Production Semantic-History Foundation     CLOSED / MERGED
PR #29 Impaired Information Semantics             CLOSED / MERGED
PR #27 New-game Global Observation Ownership      CLOSED / MERGED
Repository visibility                             PUBLIC
GitHub Actions public runner validation           GREEN
Next focused work                                 STORYTELLER INFORMATION DECISION FOUNDATION
```

### 当前最重要的产品/架构原则

```text
Official rules / legal information space
        >>>
impairment semantics
        >>>
Storyteller decision authority
        >>>
recommendation / balance preference
```

**Recommendation is advice, not authority. Storyteller confirmation is authority.**

自动 recommendation 与人工 Storyteller 选择最终必须进入同一条 rules validation → `EpistemicObservationDraft` → `ClocktowerGameSession` pipeline。

## 2. 已完成的关键 R6 rollout

### 2.1 PR #28 — mechanical ability correctness

Drunk / Poisoned 的真实机械能力效果统一失效；“玩家尝试/体验技能”与“真实 mechanical effect 生效”分离。

### 2.2 PR #29 — Impaired Information Semantics

Merge commit：`b2c0b2c7a91290670d908292b3db5719d6bd6ddb`。

当前 contract：

```text
HEALTHY information
    → truthful unless real role/registration semantics allow otherwise

DRUNK / POISONED information
    → strongly prefer legal false information
    → truthful only through explicit, explainable exception paths

GameBalanceEvaluator / style
    → may rank legal candidates
    → must not own truthful-vs-false family choice
```

PR #29 还建立了 explicit truthful-exception propagation 与 aggregate-safe reason metadata；registration semantics 仍与 impairment policy 分离。

### 2.3 PR #24 — Production Semantic-History Foundation

当前 active-game persistence 基线：

- schema v3-only；
- Clocktower semantic history mode 必须显式；
- `LEGACY_LOCAL` / `GLOBAL_V1` binding fail-closed；
- 复用 `clocktowerNextTimelineGlobalSequence` 作为唯一 Global cursor；
- 不从 round / nightStepIndex / eventCounter 猜 global sequence；
- 不新增第二个 production cursor key。

### 2.4 PR #27 — New-game Global Observation Ownership

Merge commit：`5bbb607ae408d5d9d25812825200304054a7aced`。

PR 最终 head：`0b740a5026d46beb88d18092185ce9b7bd5700ce`。

当前 production contract：

- new Clocktower games 使用 `GLOBAL_V1` observation ownership；
- restored `LEGACY_LOCAL` games 保留兼容 commit path；
- Host 生产 unbound `EpistemicObservationDraft`，Global timeline identity 由 `ClocktowerGameSession` 分配；
- exact duplicate 在 App adapter 层为无副作用 no-op；
- successful Global commit supersedes A4 revision work before durability publication；
- Death / Execution 在 caller mutation 前进行 pure Global preflight；
- Virgin + Spy registration ordering 已保证 preflight 早于 registration event publication；
- reliable private information record ID 由 canonical proposition versioning，修改目标/结果后重新展示不会发生 ID conflict。

最终验证：CI #402 GREEN（Android unit tests + debug APK、ASP、Real Clingo），R2 #358 GREEN；final Codex review 对最终 head 未发现 major issue，所有六个 review threads resolved。

## 3. NEXT — Storyteller Information Decision Unification

原来的 `Recommendation Entry-Point Unification` 已升级为本阶段。

### 3.1 目标模型

```text
Actual / registered game state
        ↓
role-specific legal information builder
        ↓
impairment policy
        ↓
InformationDecisionContext
   ├── recommended candidate(s)
   └── manual legal candidate(s)
        ↓
Storyteller chooses / confirms
        ↓
shared semantic + rules validation
        ↓
confirmed information result
        ↓
EpistemicObservationDraft
        ↓
ClocktowerGameSession
        ↓
Global semantic history
```

Manual 是一等 authority input，不是 debug bypass，也不是 unrestricted free text。

### 3.2 下一份代码 PR 的最小目标

**先做 Foundation，不直接做完整 manual UI。**

建议下一 PR 建立纯语义/authority seam，至少能够表达：

```text
InformationDecisionContext
InformationDecisionSource
    MANUAL
    RECOMMENDATION_ACCEPTED
shared legal-result validation
shared confirmed-result → EpistemicObservationDraft conversion
stale revision/context rejection
```

第一份 PR 的核心任务是让 recommendation 不再能够绕过统一 decision authority，并为后续 structured manual UI 提供合法候选和验证模型。

### 3.3 Required RED contracts

实现 production code 前至少锁定：

1. recommendation accepted 与 manual 选择同一个合法结果时，生成等价的 confirmed semantic result / observation draft；
2. healthy information role 的非法 false manual result 必须 hard block；
3. Drunk/Poisoned 可从合法 unreliable result space 中人工选择；
4. role-format / target-count / candidate-shape 非法结果必须 hard block；
5. legal-but-discouraged manual result 可以通过，但返回 soft warning；
6. manual 与 recommendation 都不能绕过 impairment / registration / role-format validation；
7. stale recommendation / stale manual context 在 revision 或 decision context 变化后必须拒绝；
8. decision source 至少可区分 `MANUAL` 与 `RECOMMENDATION_ACCEPTED`；
9. 两条路径最终都生产同一种 `EpistemicObservationDraft` 并交给 session authority；
10. regression test 防止 legacy direct recommendation path 回归。

### 3.4 下一 PR 明确 non-goals

不要在 Foundation PR 同时做：

- 完整 manual Storyteller UI；
- history UI redesign；
- Historical Action + Observation Capture；
- Spy/Recluse registration rewrite；
- Investigator 小人数平衡调参；
- broad evil-side win-rate tuning；
- A3/B4 historical engine 扩展；
- ZDD production promotion；
- ML / personalized learning；
- 新剧本高级 recommendation semantics。

如果 tests 暴露新的 official-rules correctness bug，先单独分类，再决定是否需要独立 hotfix。

## 4. Structured Manual UI — Foundation 之后

Foundation 稳定后再做 UI。UI 必须由 legal information model 驱动，而不是自行重算规则。

示例：

- Empath：结构化选择 0 / 1 / 2；
- Fortune Teller：合法 yes/no presentation；
- Undertaker：合法 role identity；
- Investigator：合法 Minion identity + 两名玩家组合。

Hard block：违反 official role format、target count/type、healthy false legality、无法转换成合法 proposition。

Soft warning：合法但与 recommendation 显著不同、Drunk/Poisoned 在强 false candidate 存在时选择 truthful、可能暴露 impairment 或严重扭曲局面。

## 5. 更新后的 rollout 顺序

```text
1. Production Semantic-History Foundation                    DONE / #24
2. Impaired Information Semantics                            DONE / #29
3. New-game Global Observation Ownership                     DONE / #27
4. Storyteller Information Decision Foundation               NEXT
5. Structured Manual Storyteller Information UI              AFTER FOUNDATION
6. Historical Action + Observation Capture
7. A3 historical multi-night exact baseline
8. Authoritative physical Grimoire ledger / Spy VerifiedExact
9. B4 historical expansion
10. Revision-driven recommendation/history unification
11. Reconsider ZDD production promotion
```

不要跳过第 4 步直接做 UI，也不要在第 4 步提前进入 historical capture。

## 6. Multi-script capability levels

长期支持等级保持：

```text
LEVEL 1  Flow supported
LEVEL 2  Manual legal information supported
LEVEL 3  Automatic recommendation supported
LEVEL 4  Advanced balance-aware recommendation supported
```

Storyteller Information Decision Unification 是达到 Level 2 的关键基础，因此也直接降低未来增加新剧本的门槛。

## 7. 长期架构边界

### 7.1 Registration 与 impairment 分层

```text
actual world
    ↓
registration projection (Spy / Recluse / future roles)
    ↓
truthful result + legal information space
    ↓
impairment policy
    ↓
storyteller decision
```

不得把 Spy/Recluse registration 塞进 Drunk/Poison misinformation policy。

### 7.2 Player knowledge boundary

Storyteller-only truth 不得为了 recommendation/manual UI 方便而进入 player-knowledge-safe core。

### 7.3 Session/global identity authority

UI、recommendation、manual selector 都不得自行分配 Global identity。最终 durable observation identity / sequence 仍由 `ClocktowerGameSession` authority 负责。

### 7.4 A3 / A4 / B4

- A3 `EnumeratedWorldSet`：exact correctness baseline；
- A4 ZDD：exact shadow/prototype，未获 production promotion；
- B4：isolated shadow；
- historical multi-night authority 尚未授权扩展。

## 8. Production guardrails

```text
Production Clocktower flow order: planner-backed
Production Werewolf flow order: planner-backed
R5.5 multi-script structural foundation: MERGED
R6 P1 prerequisites: CLOSED
Active-game schema: V3 ONLY
Drunk/Poison mechanical functioning: CENTRALIZED / #28
Impaired information truth policy: CENTRALIZED / #29
New-game Global observation ownership: LIVE / #27
Manual/recommendation shared decision authority: NEXT
Production VerifiedExact Spy producer: NOT AUTHORIZED
Production historical multi-night Possible Worlds: NOT AUTHORIZED
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
```

禁止：

- recommendation 直接成为 durable fact 而绕过 Storyteller decision confirmation；
- manual input 绕过 legal information / impairment / registration / role-format validation；
- UI 自行重新计算 role semantics；
- game balance 覆盖 Drunk/Poison reliability semantics；
- 新增第二个 production timeline cursor persistence key；
- 从 legacy data 猜 Global identity 或 `VERIFIED_EXACT`；
- storyteller-only truth 泄漏进 player knowledge；
- shadow solver result 驱动 production decision；
- 为新剧本重建第二套 catalog / flow framework。

## 9. 开发与 CI 策略

每个 behavior-changing PR：

```text
query latest live main
→ create focused short-lived branch
→ RED contracts first
→ smallest semantic implementation
→ focused tests
→ R2 main-thread boundary
→ Android unit tests + debug APK
→ ASP contract tests
→ Real Clingo cross-validation
→ exact diff audit
→ final Codex/review-thread audit
→ merge only after explicit gate is clean
```

本仓库按 `SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` 执行：大文件允许 whole-file replace，前提是使用目标 branch live head / blob SHA guard，并在写后做 exact diff audit；不要仅因文件大就默认搭 temporary writer。

## 10. 关键历史证据

- R5.5 merge：`7add8569e2484a350f6cf1512a730e9f4db469c5`
- P1.1 VerifiedExact semantic close / PR #21：`f77338bc85ae4a81b7e54e456b430e2f7f35c51a`
- P1 docs closeout / PR #22：`d56edd1552dc25dc73574c311179b9fe5a9d216b`
- Drunk/Poison correctness hotfix / PR #28：`241cb34a848833b27842d1233c37daabea244899`
- Semantic-History Foundation / PR #24：`9c1996dfc6b615a12014fb11dbb5ca9a43064b99`
- Impaired Information Semantics / PR #29 merge：`b2c0b2c7a91290670d908292b3db5719d6bd6ddb`
- Global Observation Ownership / PR #27 merge：`5bbb607ae408d5d9d25812825200304054a7aced`
- PR #27 final validation：CI #402 + R2 #358 GREEN；final review clean。

## 11. 新会话启动规则

新会话继续开发时，按顺序：

1. 读 `docs/README.md`；
2. 读 `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`；
3. 读本 `CURRENT_DEVELOPMENT_ROADMAP.md`；
4. 读 `NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md`；
5. 读 `R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md` 的 Storyteller Decision 部分；
6. 查询 live `main`，确认 PR #27 已 merged，并确认当前 source baseline 是其后代；
7. 从最新 `main` 创建新的 focused branch；
8. 从 Storyteller Information Decision Foundation 的 RED contracts 开始，不直接先做 UI。

## 12. 文档维护规则

1. **只有本文件维护当前执行点 / 当前阶段状态。**
2. 专项 design / audit / closeout / handoff 保存证据，不得覆盖 roadmap 当前状态。
3. `R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md` 继续作为 impaired-information + storyteller-decision 的专项权威设计。
4. 历史文档中仍写“Impaired Information NEXT”或“#27 PAUSED”的内容均视为历史状态，不得执行。
5. `NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md` 必须与本 roadmap 的 NEXT 保持一致。
6. 未来一旦 Storyteller Decision Foundation 合并，应立即更新本文件与 handoff，再进入 Structured Manual UI。