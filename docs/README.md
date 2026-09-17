# CampBoardGameHost 文档入口

> 最后整理：2026-09-17 Australia/Sydney  
> 目标：新开发会话只读取少量当前权威文档；历史 checkpoint / superseded route 默认不加载。

## 当前默认阅读顺序

1. root `AGENTS.md` — 项目执行、architecture、test、Git 规范；
2. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
3. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态与优先级权威**；
4. [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md) — **唯一 active handoff**；
5. [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md) — 当前自动说书人架构 / 产品路线；
6. 查询 live GitHub `main`、open PR、checks 后再实施。

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

DEFERRED:
NORMAL / EXPERT numeric thresholds
Narrative-complexity formula
Information-pacing curve
Optional soft preference / LLM critic
Pair-information display latency / old-device ADB diagnosis
```

PR #143 predates the SDE naming pivot; it is **not obsolete work**. Its current BEGINNER corpus / healthy PUBLIC_GOOD_INFO work is the active implementation of SDE-0. Before further implementation, sync that branch with latest `main` if behind.

## 当前 active references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md)
- [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) when applicable

## 当前核心架构结论

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

长期目标不是独立角色 clue scoring，而是一个持续整局运行的 `StorytellerDecisionEngine`。

关键决策：

- strategic evil topology 比原始 full-role world count 更重要；
- complete bundle / whole-history interaction 必须统一评价；
- Spy / Recluse registration 是 per-interaction decision；
- Poisoner 目标确定后可使尚未 commit 的推荐失效并触发重评；
- 后续 poisoned/drunk Empath、Fortune Teller、Undertaker、Ravenkeeper 等使用同一引擎；
- `recommendation/dynamic/ConsequenceEvaluator` 是 legacy heuristic layer，目标是完成统一 policy cutover 后删除，不再扩展其产品策略。

## SDE-0 当前执行重点

继续 PR #143，避免重复已经完成的 corpus 基础。

当前要做的是：

1. 保留已实现的 healthy `PUBLIC_GOOD_INFO` 语义：邪恶发言者可撒谎，健康好人公开 claim 必须与 shown role / mechanical clue 一致；
2. 保留 calibration / sealed holdout 分区；
3. 加入 deliberately adversarial strategic-collapse fixtures，而不只看随机 representative samples；
4. 特别覆盖 Pair + Fortune Teller confirmation、Red Herring 影响、Investigator + Chef + Empath collapse、Recluse 恢复歧义、too-weak bundle；
5. 人工标注 `BAD_TOO_STRONG / ACCEPTABLE / BAD_TOO_WEAK / UNCERTAIN`；
6. 先验证哪些 exact diagnostics 真能区分这些标签，再定 BEGINNER Badness gates；
7. SDE-0 不做 production selector cutover，不猜最终 numeric thresholds。

## 长期有价值、按需读取的参考

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

## 文档清理原则

- 当前执行方向只由 roadmap + handoff + current SDE route 决定；
- superseded 的 EPI-MQ / first-night-only route 不再保留在 active docs 入口；
- 历史 commit / PR 保留完整演进证据，无需让旧路线继续污染新会话上下文；
- audit / rule / ownership 文档只要仍提供独立事实价值，就保留为按需参考，而不是因为日期旧就删除。

## Normative engineering workflow

- root `AGENTS.md`;
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) when applicable.

## Status authority

如果文档冲突，优先级为：

1. official BoTC rules/rulings — gameplay correctness；
2. root `AGENTS.md` — execution / architecture / test rules；
3. `CURRENT_DEVELOPMENT_ROADMAP.md` — current state / priority；
4. `NEXT_DEVELOPMENT_HANDOFF.md` — current continuation point；
5. `STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md` — current SDE architecture/product route；
6. specialized domain docs where non-conflicting；
7. archive / old branches / old PRs / Git history as evidence only。
