# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-22  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 当前 `main` 基线：`9c1996dfc6b615a12014fb11dbb5ca9a43064b99`（PR #24 merged）  
> 当前开发重点：**Impaired Information Semantics**（醉酒/中毒信息语义）  
> 主架构规范：`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> 本轮增量设计：`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`  
> 多剧本架构规范：`多剧本多板子与动态游戏流程架构设计_v1.md`  
> 当前交接：`NEXT_DEVELOPMENT_HANDOFF_2026-08-22.md`

## 1. 当前结论

Phase A、R5.5、R6 P1 semantic prerequisites、Production Semantic-History Foundation 已完成。

```text
Phase A correctness foundation                 PASS
R5.5 Script & Dynamic Flow Foundation          CLOSED / MERGED
R6 P1.1 Spy Grimoire truth boundary            PASS
R6 P1.2 Global timeline semantics              PASS
R6 P1.3 Knowledge-safe input boundary          PASS
R6 P1 semantic prerequisites                   CLOSED
Post-P1 production-rollout entry audit         COMPLETE
PR #28 Drunk/Poison mechanical correctness     MERGED
Production Semantic-History Foundation / #24   CLOSED / MERGED
Repository visibility                          PUBLIC
GitHub Actions public runner validation        GREEN
Next focused work                              IMPAIRED INFORMATION SEMANTICS
```

### 2026-08-22 field-test priority decision

实战确认两个不同层次的问题：

1. **机械能力 correctness**：醉酒/中毒角色的真实机械效果必须 100% 失效；该类硬 bug 已由 PR #28 修复并进入 `main`。
2. **信息能力 policy/correctness**：醉酒/中毒玩家目前收到的信息仍过于可信，局面平衡对“真假信息”的影响过大，破坏了中毒/醉酒的游戏体验。

新的产品原则：

```text
Official rules / impairment semantics
        >>>>>>>>>>>>
information reliability intent
        >>>>>
game-balance preference
```

即：**局面平衡不得成为“是否给真信息”的主要决定因素。**

### 2026-08-22 storyteller-authority decision

随着说书人经验增长，系统不能把自动 recommendation 当成最终 authority。

未来统一模型：

```text
legal information space
        ↓
Storyteller Information Decision
   ├── automatic recommendation
   └── manual storyteller selection
        ↓
shared semantic/rules validation
        ↓
confirmed EpistemicObservationDraft
        ↓
ClocktowerGameSession / global history authority
```

手动设置不是绕过算法的 debug/escape hatch，而是长期产品的一等能力。

## 2. 当前阶段状态

| 阶段 | 状态 | 当前含义 |
|---|---|---|
| A0 / A1 / A1.1 | PASS | unified semantic model 与 player-knowledge boundary 成立。 |
| A2 ASP Oracle | PASS | typed/fail-closed oracle contract 已建立。 |
| A2.1 Golden corpus | PASS | 52 total semantic contracts；当前 oracle/A3 executable baseline 保持。 |
| A3 EnumeratedWorldSet | PASS / exact baseline | exact correctness baseline；尚不是完整 historical state-transition engine。 |
| A4 ZDD | PASS AS EXACT SHADOW | differential correctness 成立；device gate 未通过，不得驱动 production decisions。 |
| A4.5 cache/lifecycle | PASS | observation cache rebuild/durability boundary 已验证。 |
| R5.5 | CLOSED / MERGED | production flow planner 与 multi-script structural foundation 已完成。 |
| R6 P1.1 | PASS | VerifiedExact Grimoire semantic contract 已完成；production Spy producer 仍未升级。 |
| R6 P1.2 | PASS | Global action/observation chronology contract 已完成。 |
| R6 P1.3 | PASS | world/knowledge safe-core 不再接收完整 storyteller truth。 |
| Post-P1 entry audit | COMPLETE | production authority map、persistence gaps、rollout dependency 已确认。 |
| PR #28 ability correctness hotfix | CLOSED / MERGED | Drunk/Poison mechanical ability-functioning semantics 修复。 |
| Production Semantic-History Foundation / #24 | CLOSED / MERGED | schema v3-only + explicit history mode + cursor wiring + fail-closed restore。 |
| **Impaired Information Semantics** | **NEXT / REQUIRED** | 统一醉酒/中毒信息真假 policy；降低 balance 对 truthful-vs-false 的权重。 |
| PR #27 New-game Global observation ownership | PAUSED / RESUME AFTER ABOVE | 先吸收新的 information semantics，再继续 production Global producer cutover。 |
| Storyteller Information Decision Unification | REQUIRED AFTER #27 | 自动推荐与手动选择统一成同一 decision/validation/observation pipeline。 |
| Historical action + observation capture | DEFERRED | 等 Global ownership 与 storyteller decision authority 稳定。 |
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

UI/legacy adapter 可以展示和收集选择，但不得重新决定：

- next interaction；
- eligibility；
- ability functioning；
- legal information space；
- recommendation semantics；
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

### 3.4 Registration semantics 与 impairment semantics 必须分离

Spy / Recluse 的“可登记为其他身份/阵营”属于 registration projection；Drunk / Poison 属于 ability functioning / information reliability。

目标顺序：

```text
actual world state
      ↓
registration projection (Spy / Recluse / future roles)
      ↓
truthful ability result / legal result space
      ↓
impairment policy (healthy / drunk / poisoned)
      ↓
storyteller decision / presentation
```

禁止把 Spy/Recluse 的登记逻辑塞进醉酒/中毒 misinformation probability。

## 4. 已完成的 Production Semantic-History Foundation

PR #24 已合并到 `main`，当前 v3 contract：

1. `CURRENT_VERSION = 3`；
2. v1 / v2 对所有 game kind unsupported；
3. unsupported save 在 live-state mutation 前拒绝；
4. Clocktower v3 必须显式包含 `clocktowerSemanticHistoryMode`；
5. missing / null / unknown / invalid mode → fail closed；
6. 复用 `clocktowerNextTimelineGlobalSequence` 作为唯一 cursor key；
7. cursor 必须是非负整数；
8. 不从 nightStepIndex / round / eventCounter 猜 global sequence；
9. `LEGACY_LOCAL` 只接受 LegacyLocal records；
10. `GLOBAL_V1` 只接受 Global records；
11. mixed binding 必须显式拒绝；
12. Global cursor 必须严格大于所有 committed Global observation positions；
13. new/reset production game 仍从 `LEGACY_LOCAL + cursor 0` 开始，直到 #27 cutover。

### 4.1 CI 状态

仓库于 2026-08-22 转为 Public；随后重新运行 GitHub Actions，public runner 正常启动并完整执行：

```text
R2 main-thread boundary           PASS
Android unit tests + assemble     PASS
ASP contract tests                PASS
Real Clingo cross-validation      PASS
```

`steps=null` 的历史失败只作为旧 private-minute quota infrastructure evidence，不再影响当前开发 gate。

## 5. NEXT：Impaired Information Semantics

### 5.1 官方语义边界

官方规则不规定 95% 等固定概率，但允许醉酒/中毒信息偶尔真实，同时明确其信息通常应该不可靠/误导。

项目采用以下产品 contract：

```text
Mechanical ability effect:
  HEALTHY      → according to role rules
  DRUNK        → no true mechanical effect
  POISONED     → no true mechanical effect

Information ability:
  HEALTHY      → truthful unless role/registration semantics explicitly allow otherwise
  DRUNK        → strongly prefer legal false information
  POISONED     → strongly prefer legal false information
```

“strongly prefer false” 的目标统计可以接近 95%–99%，但**不能把一个全局随机百分比当成最高层业务规则**。

### 5.2 Truthful vs false 的决定顺序

```text
1. 先计算 truthful result 与所有 legal false candidates
2. 判断 impairment state
3. healthy → truthful
4. drunk/poisoned + legal false candidate exists
       → false is default
5. 仅在明确例外时允许 truthful
6. balance/style 主要用于 false candidates 之间的选择
```

允许 truthful 的典型例外：

- 没有合法 false candidate；
- false result 会几乎直接暴露 drunk/poison 状态；
- 角色具体规则/信息格式要求；
- 明确且很小的 deliberate uncertainty allowance。

### 5.3 GameBalanceEvaluator 的新边界

禁止：

```text
evilAdvantage high
    ↓
将 poisoned/drunk information 从 false 自动改回 true
```

推荐：

```text
truthful-vs-false decision
    → impairment semantics first

which false candidate
    → balance / information style / disruption risk may participate
```

因此本阶段优先改“authority/boundary”，不是简单把 balance weight 从 1.0 改成 0.1。

### 5.4 Tests-first minimum contracts

至少覆盖：

- healthy information role 必须 truthful；
- poisoned Empath 有合法 false candidate 时默认 false；
- Drunk shown information role 有合法 false candidate 时默认 false；
- poisoned Undertaker/Investigator 等结构化信息仍产生合法格式；
- evil heavily losing/winning 不得直接把 impaired false 恢复为 truthful；
- no legal false candidate → truthful exception 合法；
- explicit “avoid exposing impairment” exception 合法；
- Spy/Recluse registration 不通过本 policy 偷渡处理。

## 6. 更新后的 rollout 顺序

```text
1. Production Semantic-History Foundation                         DONE / #24

2. Impaired Information Semantics                                 NEXT
   统一 Drunk/Poison information truth/false authority
   balance 退出 truthful-vs-false 主决策

3. New-game Global Observation Ownership Cutover                  PR #27
   将最新 main/信息语义整合进 #27
   新 game observation 通过 session allocator 获取 global identity

4. Storyteller Information Decision Unification                   REQUIRED
   自动 recommendation 与手动 storyteller selection 同级
   共用 legal information space / validation / observation commit

5. Historical Action + Observation Capture
   action 与 observation 共用同一 global allocator namespace

6. A3 historical multi-night exact baseline

7. Authoritative physical Grimoire ledger
   然后才允许 production Spy VERIFIED_EXACT

8. B4 historical expansion

9. Revision-driven recommendation/history unification

10. Reconsider ZDD promotion
```

## 7. Storyteller Information Decision Unification

原 `Recommendation Entry-Point Unification` 正式升级为这一阶段。

### 7.1 核心原则

**Recommendation is advice, not authority. Storyteller confirmation is authority.**

```text
current game state
      ↓
legal information space
      ↓
InformationDecisionContext
   ├── recommendation candidate(s)
   └── manual storyteller selection
      ↓
shared validation
      ↓
confirmed information result
      ↓
EpistemicObservationDraft
      ↓
ClocktowerGameSession
```

### 7.2 Manual 不等于 free text / bypass

手动模式必须结构化并受规则验证：

- 健康角色：非法 false information hard block；
- drunk/poisoned：可在合法 unreliable result space 中选择；
- 合法但不推荐的结果：soft warning；
- UI 不自行重算角色规则；
- auto/manual 最终生成同一种 observation draft。

### 7.3 Decision provenance

第一阶段仅记录轻量 provenance：

```text
MANUAL
RECOMMENDATION_ACCEPTED
```

这为未来实战统计与个性化说书风格学习提供数据，但当前不引入 ML/self-learning authority。

### 7.4 Exit criteria

1. production 不存在绕过统一 information-decision authority 的 recommendation 入口；
2. manual 与 recommendation 共用 legal result model；
3. manual 不得绕过 impairment / registration / role-format validation；
4. illegal manual result hard block；
5. legal-but-discouraged result soft warning；
6. recommendation accepted 与 manual selection 都生成同一种 `EpistemicObservationDraft`；
7. 两者都通过 `ClocktowerGameSession` 获取 global timeline identity；
8. stale recommendation 在 revision/context 变化后不可直接复用；
9. decision source 可持久/记录；
10. regression test 防止 legacy direct recommendation path 回归。

## 8. Multi-script capability levels

为了避免每增加一个剧本都要求完整 AI recommendation，长期支持分级：

```text
LEVEL 1  Flow supported
LEVEL 2  Manual legal information supported
LEVEL 3  Automatic recommendation supported
LEVEL 4  Advanced balance-aware recommendation supported
```

这意味着新剧本只要达到 Level 2，就可以由有经验的真人说书人借助 APP 正常主持，而不必等待高级算法完成。

这也是 future multi-script rollout 的推荐最小产品边界。

## 9. Deferred Decisions / Reopen Triggers

| Decision | 当前决定 | 何时重新打开 |
|---|---|---|
| impaired truthful fallback 精确概率 | 不固定为官方式 95%；采用 semantic-first policy | 有足够真实游戏样本后根据 telemetry 调整 deliberate uncertainty。 |
| `StoryDisruptionRisk` thresholds | UNSET | false-candidate selector 准备消费真实样本时。 |
| manual UX 具体控件 | STRUCTURED / NOT FREE-TEXT | Storyteller Information Decision Unification 实施前按角色类型设计。 |
| decision provenance 扩展 | 先 `MANUAL` / `RECOMMENDATION_ACCEPTED` | 有足够 field data 后再扩展。 |
| `globalSequence` 是否进入 `PlayerWorldSetIdentity` | NO | timeline position 本身改变 world constraints/cache identity 时。 |
| Spy `VERIFIED_EXACT` production | LEGACY ONLY | durable authoritative physical Grimoire ledger 完整可 restore 后。 |
| ZDD production promotion | SHADOW ONLY | historical exact baseline + realistic device gate 通过后。 |
| Advanced multi-script Possible Worlds | TB-FIRST | TB historical exact baseline 稳定后，用第二剧本做独立 semantic proof。 |
| old active-game save migration | NO / DROPPED | 产品拥有外部用户后重新设计 future schema migration policy。 |

## 10. Production guardlines

```text
Production Clocktower flow order: planner-backed
Production Werewolf flow order: planner-backed
A3 EnumeratedWorldSet: exact correctness baseline
A4 ZDD: exact shadow/prototype only
B4: isolated shadow only
R6 P1: CLOSED
Active-game schema: V3 ONLY
V1/V2 restore: UNSUPPORTED
Clocktower v3 history mode: REQUIRED / EXPLICIT
Clocktower v3 cursor key: EXISTING KEY / REQUIRED / DO NOT DUPLICATE
Drunk/Poison mechanical functioning: CENTRALIZED / #28
Impaired information truth policy: NEXT / CENTRALIZE BEFORE #27
Production Global producer cutover: NOT YET WIRED / PR #27 PAUSED
Manual storyteller information: REQUIRED AFTER #27
Production VerifiedExact Spy producer: NOT AUTHORIZED
Production historical multi-night Possible Worlds: NOT AUTHORIZED
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
```

禁止：

- 用 game balance 直接覆盖 Drunk/Poison information semantics；
- 把 95% 写成不可解释的单层 RNG 规则；
- 把 Spy/Recluse registration 与 impairment misinformation 混为一层；
- manual storyteller input 绕过规则/语义 validation；
- recommendation 直接成为 durable fact 而不经过统一 decision confirmation；
- UI 自行重新计算 recommendation/role semantics；
- 从缺字段猜 `LEGACY_LOCAL`；
- 从 legacy local sequence 猜 global identity；
- 新增第二个 production timeline cursor persistence key；
- 恢复 v1/v2 active save；
- 从 legacy Grimoire 猜 `VERIFIED_EXACT`；
- 把 timeout/OOM/cap 当 UNSAT；
- 截断 exact worlds 后仍声称 exact；
- 把 storyteller-only truth 放入 player knowledge；
- 让 shadow result 驱动 production decision；
- 为新剧本重建第二套 catalog/flow framework。

## 11. 实战验证策略

实战反馈按以下类别处理：

```text
rules / ability functioning / persistence / state correctness
    → correctness priority

impaired information feels implausibly truthful
    → Impaired Information Semantics

storyteller wants a different legal clue/info result
    → Storyteller Information Decision Unification

recommendation distribution / balance quality
    → recommendation policy tuning after semantics are correct

cosmetic preference
    → 不打断 authority/correctness rollout
```

重要原则：**先保证信息“规则上合法、语义上合理”，再讨论“平衡上最优”。**

## 12. 开发与 CI 策略

每个 behavior-changing rollout PR：

```text
latest main audit
→ failing contract/regression tests
→ smallest semantic implementation
→ focused tests
→ R2 main-thread boundary
→ Android unit tests + debug APK
→ ASP contract tests
→ real Clingo cross-validation
→ exact diff audit
→ final correctness/review-thread audit
→ merge
```

仓库当前为 Public；GitHub-hosted public runners 已实际验证可用。CI 结果必须以真实 checkout/compiler/test steps 为准。

## 13. 关键历史证据

- R5.5 merge：`7add8569e2484a350f6cf1512a730e9f4db469c5`
- P1.3 safe-input PR #7/#8：`19b91887344655285ec8bd93ca5bdb51bcfff445` / `8f5ccc551948fea085caf8df3eb100ef67eae438`
- P1.1 VerifiedExact semantic close / PR #21 merge：`f77338bc85ae4a81b7e54e456b430e2f7f35c51a`
- P1 docs closeout / PR #22 merge：`d56edd1552dc25dc73574c311179b9fe5a9d216b`
- docs/design cleanup / PR #23 merge：`3db66482d9367c6b42a3f2550b979c28bfafea42`
- Drunk/Poison correctness hotfix / PR #28 merge：`241cb34a848833b27842d1233c37daabea244899`
- Semantic-History Foundation / PR #24 merge：`9c1996dfc6b615a12014fb11dbb5ca9a43064b99`
- 2026-08-22 Public runner validation：R2 / Android / ASP / real Clingo all GREEN。

## 14. 文档维护规则

1. **只有本文件维护当前执行点 / 当前阶段状态。**
2. 专项 design/audit/closeout/handoff 保存证据，不创建第二个并列 roadmap。
3. `R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md` 是本轮新增语义/产品边界的专项权威设计；若与旧 recommendation 文档冲突，以本 roadmap + 该专项设计为准。
4. 历史文档中的旧 migration / recommendation-entry 设计可以保留为历史证据，但不得覆盖当前 v3-only、impaired-information-first、manual-storyteller-authority 决策。
5. 新 handoff 必须指向本 roadmap。
6. 未发布阶段可以主动丢弃不再有价值的 compatibility burden；一旦产品对外发布，future schema migration policy 必须重新显式设计。
