# CampBoardGameHost 历史文档归档

> 该目录中的文档仅用于追溯设计演进、旧算法行为、历史审计与验收证据。  
> **不得把 archive 中的 `PASS / COMPLETE / READY` 当作当前开发状态。**

当前开发入口见 `../README.md` 和 `../CURRENT_DEVELOPMENT_ROADMAP.md`。

## 1. 自动说书人早期/旧版设计

- `CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v1.md` — 玩家认知一致性早期方案，已被 v2.2 取代。
- `CampBoardGameHost_自动说书人算法改进设计_最终实施版_v3.md` — V3 设计历史。
- `CampBoardGameHost_自动说书人算法改进设计_最终实施版_v4.md` — 2026-08-10 前后完成的 V4 推荐算法设计；其代码成果仍可能构成生产基线，但它不再是当前 Possible Worlds/Phase A 的主路线。
- `storyteller_v4_phase0_baseline_report.md` — V4 实施前旧算法行为基线。
- `storyteller_v4_final_acceptance_report.md` — V4 PR0–PR11 当时的工程验收；只说明 V4 里程碑，不代表后来的 epistemic engine 已验收。

## 2. 已被后续计划取代的实现说明

- `dynamic_storyteller_decision_recommendation_implementation.md` — 已由 `../storyteller_revision_driven_dynamic_decision_engine_plan.md` 取代。
- `storyteller_information_recommendation_implementation_design.md` — 旧信息推荐实现设计。
- `storyteller_recommendation_offline_review_report.md` — 旧离线审查记录。

## 3. A3 历史验收记录

- `storyteller_a3_enumerated_world_baseline.md` — A3 当时的 baseline/status 记录。
- `storyteller_a3_exit_review.md` — 2026-08-12 的 A3 PASS。

**2026-08-19 审计已撤销该 PASS 作为当前有效退出结论。** 原因包括：

1. Chef/Empath numeric registration path 没有关闭 poisoned Spy/Recluse 的特殊登记能力；
2. A3 official golden runner 主要验证单个已知 world 的 evaluator，并未充分端到端验证 `PlayerKnowledge -> TroubleBrewingWorldEnumerator -> world set`；
3. A2 nested FormalGameState fixture 仍使用旧 schema shape。

这些文件保留是为了说明“当时为什么认为通过”，不是为了证明现在仍然通过。

## 4. UI / 产品历史材料

- `ui-audit.md` — 早期 UI 审计。
- `血染钟楼说书人助手_UI重设计需求与设计系统草案.md` — UI 重设计草案。

## 5. 归档规则

满足以下任一条件即可归档：

- 被更高版本主规范明确取代；
- 对应阶段已经完成且后续只需历史证据；
- 验收结论被新审计撤销；
- 未来计划已被新的统一计划重写；
- 文档主要描述已经不存在的实现路径。

归档文件通常保持原文件名和内容不变，以保留 Git 历史和引用上下文。
