# CampBoardGameHost 文档入口

> 最后整理：2026-09-21 Australia/Sydney  
> 目标：新开发会话只读取当前权威。历史过程、已撤销路线和中间校准实验不再留在 active docs 中制造歧义。

## 默认阅读顺序

1. root `AGENTS.md`
2. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
3. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态 / 优先级权威**
4. [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md) — **唯一 active handoff**
5. [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md) — **当前 first-night policy authority**
6. [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv) — 外部证据 seed catalog；当前不含已验证 GOLD expert case
7. 需要 SDE-2D / SDE-3 长期约束时，再读 [`SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`](SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md)
8. 需要全局 SDE 架构背景时，再读 [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)

随后查询 live branch / PR / checks。不要从 Git history、archive、旧 PR 或旧 manifest 的 `NEXT / READY / COMPLETE` 推断当前任务。

## 当前状态

~~~text
SDE-0                                  COMPLETE / PR #143
SDE-1                                  COMPLETE / PR #144
SDE-2D1 Drunk whole-bundle             COMPLETE / PR #145
SDE-2D2 Demon bluff joint-output       COMPLETE / PR #146
SDE-2D3 strategic-world quotient       COMPLETE / PR #147
SDE-2D4 5–15 generalization            COMPLETE / PR #149

SDE-2D5 calibration / policy evidence  CURRENT / PR #150 draft
  D5A–D5E                              COMPLETE
  D5F-A / B infrastructure             COMPLETE
  D5F-B3 correction                    HISTORICAL CHECKPOINT
  D5F-B4 expert-observed calibration   CURRENT

D5F-C gate/band derivation             BLOCKED
sealed holdout                         CLOSED
SDE-3                                  BLOCKED
~~~

## 当前政策核心

- legality、consequence、selection policy 必须分层。
- Spy / Recluse registration 永远是 interaction-scoped，不修改 canonical identity。
- BEGINNER 默认采用角色主题注册：Spy 优先登记为 Good/Townsfolk/Outsider；Recluse 优先登记为 Evil/Minion/Demon。
- 这个默认是强 prior，不是硬规则；只有 whole-bundle health 有实质改善时才 override。
- 有健康替代时，避免 Librarian 直接暴露 Recluse，强烈避免 Investigator 直接暴露 Spy；forced case 正常接受。
- Chef / Empath 只有在所有合法 Spy/Recluse registration 分支都得到同一个值时才是 rule-determined。
- Drunk misinformation 不等于必须说假话；多夜信息应保持可信的 shadow-world / narrative trajectory。
- Demon bluffs、Red Herring、pair clues、numeric registration branches、Drunk/poisoned information应在生命周期允许范围内按 whole-bundle 评价。
- strategic evil topology 比 raw role-world multiplicity 更重要，但不能代替 role-information utility、confirmation-chain、role-function exposure、bluff usability 等语义维度。
- 不引入 opaque global scalar。

## 校准证据原则

当前路线是 **expert-observation-first**，不是 single-reviewer-label-first。

~~~text
GOLD
    已验证的资深 / 可信 Storyteller 真人对局
    可重建 setup + Night 1 choices
    explicit rationale 优先

SILVER
    高完整度真实记录，例如 ClockTracker
    Storyteller 水平未独立验证

QUALITATIVE
    教程、复盘、资深社区讨论

DIAGNOSTIC_ONLY
    synthetic / extreme / counterfactual fixture
~~~

现有 external evidence catalog 只是 seed；在完成专家身份/经验验证前，不得把其中任何条目当作 GOLD。

专家实际选择 A 也不代表所有未选择的 B/C/D 都是 BAD。强 preference evidence 需要 explicit rationale、明确拒绝替代项、重复可比选择或跨来源一致模式。

最终胜负不能作为 Storyteller 决策质量标签。

## 已撤销的 D5 路线

2026-09-20 的中间 policy-correction / extreme-fixture / clean-representative calibration 文档已从 active docs 删除。有效结论已经合并进 2026-09-21 policy synthesis、roadmap 和 handoff；Git history / archive 仅用于必要的历史追溯。

旧 human-label manifests 与 test-only calibration artifacts 目前只是历史/兼容资产；是否删除属于下一步代码/测试清理审计，不从它们推断当前 policy。

## Active long-lived references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md)
- [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md)
- [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv)
- [`SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`](SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md)
- [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`](GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)

Completed slice-level evidence lives under [`archive/`](archive/README.md).

## Status authority

If documents conflict, use this order:

1. official BoTC rules/rulings — gameplay correctness;
2. root `AGENTS.md` — execution / architecture / testing;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` — current state / priority;
4. `NEXT_DEVELOPMENT_HANDOFF.md` — current continuation point;
5. `SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md` — current first-night policy;
6. current external evidence catalog — source inventory only, not normative truth;
7. long-lived architecture/reference docs;
8. archive / Git history — historical evidence only.
