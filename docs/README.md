# CampBoardGameHost 文档入口

> 最后整理：2026-09-17 Australia/Sydney  
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / superseded route 默认不加载。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
3. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
4. [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md) — **唯一 active handoff**；
5. [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md) — 当前自动说书人架构 / 产品路线；
6. 查询 live GitHub `main`、PR #143、checks 后再实施。

不要从 `archive/`、旧 branch、旧 PR 或历史文档中的 `PASS / COMPLETE / READY / NEXT` 推断当前状态。

## 当前开发状态

```text
FN-BUNDLE-0                                      COMPLETE / PR #139
FN-BUNDLE-1                                      COMPLETE / PR #140
FN-BUNDLE-2 healthy whole-bundle harness         COMPLETE / PR #142

CURRENT:
SDE-0 — BEGINNER strategic-robustness corpus + policy contract
Active implementation: PR #143 / fn-bundle-3-beginner-corpus

NEXT:
SDE-1 — unified StorytellerDecisionEngine orchestration seam
SDE-2 — Drunk -> Spy/Recluse -> Poisoner first-night uncertainty
SDE-3 — cross-night impaired / registration decisions
SDE-4 — production cutover + legacy heuristic retirement
```

PR #143 predates the SDE naming pivot；它不是废弃工作。其 BEGINNER corpus / healthy PUBLIC_GOOD_INFO 实现就是当前 SDE-0。新开发会话首先查询 live refs，并在需要时把 #143 同步到最新 `main`，然后继续而不是重做。

## 当前核心架构

```text
rules
  -> legal outcomes / registration legality

session
  -> canonical actual state / timeline / commit

flow
  -> interaction ordering / projection

epistemic
  -> recipient-visible exact hypothetical consequences
  -> strategic world-structure diagnostics

recommendation / StorytellerDecisionEngine
  -> compose legal candidates
  -> consume exact diagnostics
  -> apply profile / phase policy
  -> select acceptable outcomes

UI
  -> presentation / confirmation / manual override
```

关键决定：

- strategic evil topology 比 raw full-role world count 更重要；
- complete bundle / whole-history interaction 必须统一评价；
- Spy / Recluse registration 是 per-interaction decision；
- Poisoner 目标可使尚未 commit 的推荐失效并触发重评；
- 后续 poisoned/drunk Empath、Fortune Teller、Undertaker、Ravenkeeper 等使用同一引擎；
- `recommendation/dynamic/ConsequenceEvaluator` 是 legacy heuristic layer，统一 policy cutover 后目标是删除，不再扩展产品策略；
- 不建立第二套 rules engine、state engine 或 possible-world solver；
- 不使用一个 opaque scalar 作为最终 Storyteller 决策权威。

## SDE-0 当前执行重点

继续 PR #143，保留已经完成的：

- healthy `PUBLIC_GOOD_INFO` 语义：邪恶发言者可撒谎；健康好人的 claim 必须与 shown role + mechanical clue 一致；
- strict mechanically known `ShownRoleAt` 仍然是 exact；
- calibration / sealed holdout 分区；
- deterministic review sampling；
- exact structural diagnostics + leave-one-out evidence；
- expensive calibration experiment 与普通 regression 分离。

下一步重点加入 deliberately adversarial scenarios：

1. Pair + Fortune Teller confirmation chain；
2. Red Herring 对 confirmation path 的影响；
3. Investigator + Chef + Empath topology collapse；
4. Recluse registration 恢复歧义；
5. too-weak bundle；
6. clearly acceptable healthy contrast。

然后人工标注：

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

先验证哪些 exact diagnostics 能区分这些标签，再制定 BEGINNER Badness gates。SDE-0 不做 production selector cutover，不猜最终 numeric thresholds。

## 当前 active references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md)
- [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) when applicable

## 长期有独立事实价值、按需读取的参考

- [`GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`](GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md)
- [`EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`](EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md)
- [`FN_BUNDLE_0_LIVE_SEAM_AUDIT_2026-09-16.md`](FN_BUNDLE_0_LIVE_SEAM_AUDIT_2026-09-16.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)
- [`external_solver_evaluation.md`](external_solver_evaluation.md)
- [`BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`](BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md)
- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md)

## 已清理的旧执行路线

以下旧方向已从 active `docs/` 移除，历史仍可从 Git 找回：

- first-night-only EPI-MQ route；
- EPI-MQ route re-audit；
- productive-uncertainty scoring plan；
- old epistemic recommendation v2.2 plan；
- old revision-driven dynamic-decision implementation plan。

不要为了历史考古把它们重新作为执行 authority 加回默认阅读路径。

## Normative engineering workflow

- root `AGENTS.md`；
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md)；
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) when applicable。

## Status authority

如果文档冲突，优先级为：

1. official BoTC rules/rulings — gameplay correctness；
2. root `AGENTS.md` — execution / architecture / test rules；
3. `CURRENT_DEVELOPMENT_ROADMAP.md` — current state / priority；
4. `NEXT_DEVELOPMENT_HANDOFF.md` — current continuation point；
5. `STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` — current SDE architecture/product route；
6. specialized domain docs where non-conflicting；
7. archive / old branches / old PRs / Git history as evidence only。
