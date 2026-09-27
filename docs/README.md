# CampBoardGameHost 文档入口

> 最后整理：2026-09-27 Australia/Sydney  
> 目标：新开发会话只读取当前权威。历史过程、已撤销路线和中间校准实验不再留在 active docs 中制造歧义。

## 默认阅读顺序

1. root `AGENTS.md`
2. [`AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`](AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md) — **当前 GitHub Connector-first / Mini MCP+Codex 大文件补充工作流**
3. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
4. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态 / 优先级权威**
5. [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md) — **唯一 active handoff**
6. [`SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md`](SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md) — **已完成 CR-A/B/C + IF-D，并定义当前 RH-E integration-hardening contract**
7. [`SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md`](SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md) — C0–C3 历史集成闭环契约与已验收边界
8. [`SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`](SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md) — SDE-3 architecture / evidence route；**不承担 live PR/执行状态**
9. [`SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`](SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md) — **3D/3E surface-scoped gate authority**
10. [`SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md`](SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md) — **V1 immutable baseline**
11. [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md) — first-night policy / evidence synthesis
12. [`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`](SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md) — targeted evidence acquisition contract
13. [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv) — external evidence catalog
14. 需要全局 SDE 架构背景时，再读 [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)

随后检查 live 分支、工作区和差异；远端验收时独立查询 exact-head PR / checks。不要从 memory、Git history、archive、已完成 slice audit 或旧 PR 的 `NEXT / READY / COMPLETE` 推断当前状态。执行环境以 root `AGENTS.md` 和当前 workflow 为准：**GitHub Connector 默认负责日常 repository / PR / CI 工作；Mini MCP + Codex CLI 只作为需要完整本地上下文的大文件分析/执行补充。**

## 当前状态

~~~text
SDE-0 / SDE-1                          COMPLETE
SDE-2D1–SDE-2D4                        COMPLETE
SDE-2D5 evidence/calibration           CHECKPOINT MERGED / PARALLEL TARGETED EVIDENCE
  B4 expert/SILVER infrastructure      COMPLETE FOR CURRENT ANCHORS
  targeted evidence acquisition       EXTERNAL / CONTINUOUS
  old D5F-C numeric gate route         NOT CURRENT PRODUCTION CRITICAL PATH
sealed holdout                         CLOSED

SDE-3A engine/feature/policy contract  COMPLETE / PR #151/#152
SDE-3B BEGINNER_CONSERVATIVE_V1        COMPLETE / PR #153 MERGED
SDE-3C DecisionTrace/replay            COMPLETE / 3C0–3C5 historical checkpoint preserved
SDE integration closure               C0–C3 HISTORICAL CHECKPOINT ACCEPTED
Post-audit correctness repair         CR-A / CR-B / CR-C COMPLETE / ACCEPTED
SDE-3D calibrated policy freeze        3D0–3D1 + C4/SDE-3D2 COMPLETE / IF-D COMPLETE / RH-E COMPLETE
SDE-3E automatic production cutover    C5 BLOCKED ON QUALIFYING E3/E4 / PER-SURFACE GATES
~~~

当前远端边界：正式 continuation 是 `codex/sde-history-prefix-route-closure`，由 Draft PR #157 承载；2026-09-27 live-main integration 已完成并验收。integration head `371ebf624898c08747203aceaed1254647867214` 吸收了 `main` 的 M8G5 ancestry，同时保留新的 GitHub Connector-first 规范并将旧 M8G5 control-plane 文档归档；CI #3479 与 R2 #3232 均 GREEN，#157 已恢复 mergeable 且继续保持 Draft。#160/#161/#162 等 validation-only PR 已在证据进入正式 continuation 后关闭而未合并；#109 也已按 current-format Recovery 边界关闭。任何 ready / PR merge / rebase / force-push 仍需单独授权。RH-E 验收见 [`archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md`](archive/checkpoints/sde/SDE_RH_E_RUNTIME_PERSISTENCE_TIMING_COMPLETION_2026-09-27.md)，main integration 验收见 [`archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md`](archive/checkpoints/sde/SDE_PR157_MAIN_INTEGRATION_COMPLETION_2026-09-27.md)。

## 当前政策核心

- legality、consequence、selection policy 必须分层。
- `BEGINNER_CONSERVATIVE_V1` 已冻结为 immutable provisional baseline：只有 exact zero Evil-topology hard rejection，其余 viable survivors 保持 equivalence band，并使用 `SEEDED_HASH_V1`。
- Spy / Recluse registration 永远是 interaction-scoped，不修改 canonical identity。
- Chef / Empath 只有在所有合法 Spy/Recluse registration 分支都得到同一个健康值时才是 rule-determined。
- 受损信息不等于必须说假话；重复/历史依赖的信息必须通过共享、角色无关的 derived narrative projection over canonical history 维持一致性，不能按角色分别写 policy 特判。
- confirmation chain、healthy-information utility、impaired narrative、role-function exposure 已有 typed descriptive feature，但 **V1 不使用它们排序**；2026-09-25 审计发现的 impaired-narrative semantic defect 已由 CR-A 在共享 projector owner 修复并验收。
- role-function exposure 的存在已经被建模；Librarian→Recluse、Investigator→Spy 的 avoidance severity 仍缺 E3 证据，不能把旧设计建议当成当前 V1 policy。
- Red Herring contextual utility 与 truth danger / credibility disruption 有目前最强的跨专家基础之一；CR-A/B/C 与 C4 / SDE-3D2 已完成 typed production descriptive feature closure，但这些特征仍不得在缺少 qualifying E3/E4 evidence 时直接变成新的 preference。
- strategic evil topology 是重要 structural evidence，但不能代替 healthy information、confirmation、role-function exposure、bluff usability、narrative coherence 等维度。
- 不引入 opaque global scalar；Gap E 只有在未来 policy 真正需要 numeric weighting 时才成为 blocker。
- automatic production cutover 按 decision surface gating；一个“大 equivalence class + seeded selection”的 V1 结果本身不足以授权 cutover。

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

现有 evidence surface 已包含两个独立 primary-verified GOLD anchors（Ben / A Stud In Scarlet；Evin 2019）以及 bounded SILVER replay。新的 policy strength 仍必须按 provenance、Storyteller independence、explicit rationale 和 production-recoverable legal alternatives 分级进入 E1/E2/E3/E4。

专家实际选择 A 也不代表所有未选择的 B/C/D 都是 BAD。强 preference evidence 需要 explicit rationale、明确拒绝替代项、重复可比选择或跨来源一致模式。

最终胜负不能作为 Storyteller 决策质量标签。

## 已撤销的 D5 路线

2026-09-20 的中间 policy-correction / extreme-fixture / clean-representative calibration 文档已从 active docs 删除。有效结论已经合并进 2026-09-21 policy synthesis、roadmap 和 handoff；Git history / archive 仅用于必要的历史追溯。

旧 human-label manifests 与 test-only calibration artifacts 目前只是历史/兼容资产；是否删除属于下一步代码/测试清理审计，不从它们推断当前 policy。

## Active long-lived references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md)
- [`AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`](AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md)
- [`SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`](SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md)
- [`SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`](SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md)
- [`SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md`](SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md)
- [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md)
- [`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`](SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md)
- [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv)
- [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`](GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)

Older completed/superseded implementation routes live under [`archive/`](archive/README.md). Some recent SDE-3 slice audits remain in the active directory temporarily because the current roadmap/handoff links them as completion evidence; their embedded historical `NEXT`/PR state is not execution authority.

## Status authority

If documents conflict, use this order:

1. official BoTC rules/rulings — gameplay correctness;
2. root `AGENTS.md` — execution / architecture / testing;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` — current state / priority;
4. `NEXT_DEVELOPMENT_HANDOFF.md` — current continuation point;
5. `AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md` — current GitHub Connector-first / Mini MCP+Codex large-file execution workflow;
6. `SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md` plus current 3D audit — active SDE execution/calibration contract;
7. `SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md` and targeted evidence contract — policy/evidence background;
8. external evidence catalog — source inventory/provenance, not standalone normative truth;
9. long-lived architecture/reference docs;
10. Mini MCP developer memory — optional advisory navigation/experience only;
11. completed/superseded workflow docs, slice audits, archive and Git history — historical/completion evidence only.