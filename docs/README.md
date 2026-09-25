# CampBoardGameHost 文档入口

> 最后整理：2026-09-25 Australia/Sydney  
> 目标：新开发会话只读取当前权威。历史过程、已撤销路线和中间校准实验不再留在 active docs 中制造歧义。

## 默认阅读顺序

1. root `AGENTS.md`
2. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
3. [`MINI_MCP_DEVELOPMENT_WORKFLOW_AND_MEMORY_ADOPTION_2026-09-25.md`](MINI_MCP_DEVELOPMENT_WORKFLOW_AND_MEMORY_ADOPTION_2026-09-25.md) — **当前 repository/GitHub/developer-memory 工作流**
4. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态 / 优先级权威**
5. [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md) — **唯一 active handoff**
6. [`SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md`](SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md) — **当前 SDE-3 执行路线**
7. [`SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md`](SDE_3D0_CALIBRATED_POLICY_FREEZE_CUTOVER_GATE_ARCHITECTURE_AUDIT_2026-09-24.md) — **3D/3E surface-scoped gate authority**
8. [`SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md`](SDE_3D1_V1_IMMUTABLE_BASELINE_FREEZE_COMPLETION_AUDIT_2026-09-24.md) — **V1 immutable baseline**
9. [`SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`](SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md) — first-night policy / evidence synthesis
10. [`SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md`](SDE_2D5F_B4F_TARGETED_EVIDENCE_GAP_CONTRACT_2026-09-23.md) — targeted evidence acquisition contract
11. [`SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv`](SDE_2D5F_EXTERNAL_EVIDENCE_SOURCE_CATALOG_2026-09-21.tsv) — external evidence catalog
12. 需要全局 SDE 架构背景时，再读 [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)

随后使用 Mini MCP 查询 live branch / PR / checks。对实质性开发任务，再做一次 task-specific、bounded `memory_search({ repo: "clocktower" })`，只对相关候选调用 `memory_get`。不要从 memory、Git history、archive、已完成 slice audit 或旧 PR 的 `NEXT / READY / COMPLETE` 推断可实时验证的状态。

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
SDE-3C DecisionTrace/replay            COMPLETE / 3C0–3C5
SDE-3D calibrated policy freeze        IN PROGRESS / 3D0–3D1 COMPLETE / 3D2 NEXT
SDE-3E automatic production cutover    BLOCKED PER DECISION SURFACE
~~~

## 当前政策核心

- legality、consequence、selection policy 必须分层。
- `BEGINNER_CONSERVATIVE_V1` 已冻结为 immutable provisional baseline：只有 exact zero Evil-topology hard rejection，其余 viable survivors 保持 equivalence band，并使用 `SEEDED_HASH_V1`。
- Spy / Recluse registration 永远是 interaction-scoped，不修改 canonical identity。
- Chef / Empath 只有在所有合法 Spy/Recluse registration 分支都得到同一个健康值时才是 rule-determined。
- 受损信息不等于必须说假话；重复/历史依赖的信息必须通过共享、角色无关的 derived narrative projection over canonical history 维持一致性，不能按角色分别写 policy 特判。
- confirmation chain、healthy-information utility、impaired narrative、role-function exposure 已有 typed descriptive feature，但 **V1 不使用它们排序**。
- role-function exposure 的存在已经被建模；Librarian→Recluse、Investigator→Spy 的 avoidance severity 仍缺 E3 证据，不能把旧设计建议当成当前 V1 policy。
- Red Herring contextual utility 与 truth danger / credibility disruption 有目前最强的跨专家基础之一；3D2 先建立 descriptive feature surface，仍不得直接产生 preference。
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
- [`MINI_MCP_DEVELOPMENT_WORKFLOW_AND_MEMORY_ADOPTION_2026-09-25.md`](MINI_MCP_DEVELOPMENT_WORKFLOW_AND_MEMORY_ADOPTION_2026-09-25.md)
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
5. `MINI_MCP_DEVELOPMENT_WORKFLOW_AND_MEMORY_ADOPTION_2026-09-25.md` — current repository/GitHub/developer-memory execution workflow;
6. `SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md` plus current 3D audit — active SDE execution/calibration contract;
7. `SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md` and targeted evidence contract — policy/evidence background;
8. external evidence catalog — source inventory/provenance, not standalone normative truth;
9. long-lived architecture/reference docs;
10. Mini MCP developer memory — advisory navigation/experience only;
11. completed slice audits, archive and Git history — historical/completion evidence only.
