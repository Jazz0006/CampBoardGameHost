# CampBoardGameHost 文档入口

> 最后整理：2026-10-04 Australia/Sydney  
> 目标：新开发会话只读取当前权威。历史过程、已撤销路线和中间校准实验不再留在 active docs 中制造歧义。

## 默认阅读顺序

1. root `AGENTS.md`
2. [`AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`](AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md)
3. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
4. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态 / 优先级权威**
5. [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md) — **唯一 active handoff**
6. [`LRE_STAGED_POLICY_REPLACEMENT_AND_RETIREMENT_ROUTE_2026-10-04.md`](LRE_STAGED_POLICY_REPLACEMENT_AND_RETIREMENT_ROUTE_2026-10-04.md) — **当前推荐系统执行主线：Manual safety → family cutover → final deletion**
7. [`LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md`](LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md) — **旧 heuristic 全面退休的证据/范围权威**
8. [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md) — **长期 Recommendation Engine 架构权威**
9. [`TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md`](TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md) — **TB canonical snapshot / typed decision context 长期边界**
10. [`CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md`](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md) — **Setup / Game Engine / Recommendation ownership guardrail**

DLB、C5、TBGS-2A–2E、RSR-0/1A 均已完成或进入历史状态；其 completion audit、cutover audit 和中间路线只在具体 ownership / replay / evidence 问题需要时读取，不再属于默认启动集合。

随后检查 live 分支、工作区和差异；远端验收时独立查询 exact-head PR / checks。不要从 memory、Git history、archive、已完成 slice audit 或旧 PR 的 `NEXT / READY / COMPLETE` 推断当前状态。执行环境以 root `AGENTS.md` 和当前 workflow 为准：**GitHub Connector 默认负责日常 repository / PR / CI 工作；Mini MCP + Codex CLI 只作为需要完整本地上下文的大文件分析/执行补充。**

## 当前状态

~~~text
SDE-0 / SDE-1 / SDE-3A / SDE-3C        COMPLETE
SDE-3B BEGINNER_CONSERVATIVE_V1         COMPLETE / HISTORICAL IMMUTABLE ID
C4 / IF-D / RH-E                        COMPLETE / ACCEPTED
DLB campaign                            COMPLETE / ACCEPTED
TBGS-0 / TBGS-1                         COMPLETE / ACCEPTED
TBGS-2A / 2B / 2C / 2D / 2E            COMPLETE / ACCEPTED; PAUSED AFTER 2E
DRUNK_ASSIGNMENT_Q04_V1                 PRODUCTION CUTOVER / ACCEPTED
C5-E functioning Librarian V2           PRODUCTION CUTOVER / ACCEPTED
HOST-ML0 / HOST-ML1                     COMPLETE / ACCEPTED
RSR-0 / RSR-1A                          COMPLETE / HISTORICAL; docs archived
LRE-0 whole-engine retirement audit     COMPLETE / RETIREMENT AUTHORIZED
LRE staged replacement route            CURRENT EXECUTION AUTHORITY
LRE-1 Manual / fail-closed safety gate  NEXT
LRE-P family replacement + cutover      ITERATIVE AFTER LRE-1
LRE-2..5 physical legacy deletion       AFTER production dependency reaches zero
bounded App/Host decomposition          GUARDRAIL / INDEPENDENT MAINTENANCE ONLY
~~~

当前远端状态必须在会话开始时实时查询，不在索引里复制易过期的 branch HEAD。DLB 已完成；Q04 Drunk 与 functioning Librarian V2 已完成 production cutover。当前主线不再是继续维护旧推荐行为，而是 LRE：先让所有缺少成熟 policy 的多候选决策安全地回退到 Manual，再按 decision family 逐个完成 evidence-backed policy、replay/evaluation 和 production cutover；每个 family cutover 后立即撤销旧 heuristic 在该 scope 的 authority，最后才删除无生产依赖的旧 scoring/style/telemetry 代码。EvidenceLab 持续负责 candidate-comparison / rejection / rationale 证据。

## 当前政策核心

- legality、consequence、selection policy 必须分层。
- TB 当前统一读边界采用 `TroubleBrewingGameSnapshotV1`：它从 canonical setup/session/history 纯投影出来，供 Game/rules consumers、Recommendation Context Builder 与 EvidenceLab replay 共享语义；它不是第二个 mutable state owner。
- Snapshot 必须区分 `KNOWN(value)` / `UNCOMMITTED` / `UNKNOWN` / `NOT_APPLICABLE`。尤其 Drunk assignment 前“尚未选择 Drunk”是 `UNCOMMITTED`，不能与 EvidenceLab “历史事实无法恢复”的 `UNKNOWN` 混为一谈。TBGS-0 已在 `f367c0d3ec23ebf452c924ff7c0921cd978a800f` 通过 CI #3618 / R2 #3343，含稳定 external role IDs、precommit/committed/runtime projectors、deterministic V1 JSON 与 G10 golden fixture。
- Drunk assignment 的新权威链为：shown-seat assignment -> TB snapshot -> rules-owned legal candidate domain -> typed decision context -> hypothetical consequence / shadow evaluation -> canonical commit。TBGS-1A 已在 `ae4dc2400325d233da033d3c86d2863bde1bd485` 通过 CI #3621 / R2 #3345；EvidenceLab `970e7e6430f7088ac004cd1e5696759da4d52003` 已完成同一 G10 historical-prefix materializer，Host 独立复核两个 V1 golden fixture 2115 bytes 完全一致，因此 TBGS-1 整体 COMPLETE / ACCEPTED。该结果仍没有新增排序语义。
- Red Herring 使用 generic observation/dependency barrier；Spy 是当前 TB 的一个 observer，而不是需要写死的 policy 特判。
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
- 当前统一 authority invariant：唯一合法结果可规则自动；多个合法结果且有 accepted versioned policy 才可自动；多个合法结果但无 accepted policy 必须 `MANUAL_REQUIRED / POLICY_DEFERRED`，不得回落旧 heuristic。
- 新 policy 采用 family-by-family cutover，不等待所有 family 一次完成；物理删除旧引擎则等待生产依赖归零。
- 已接受的 production policy islands：`DRUNK_ASSIGNMENT_Q04_V1` 与 functioning Librarian V2。

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
- [`LRE_STAGED_POLICY_REPLACEMENT_AND_RETIREMENT_ROUTE_2026-10-04.md`](LRE_STAGED_POLICY_REPLACEMENT_AND_RETIREMENT_ROUTE_2026-10-04.md)
- [`LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md`](LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md)
- [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)
- [`TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md`](TB_CANONICAL_GAME_SNAPSHOT_INTEGRATION_ROUTE_2026-09-30.md)
- [`CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md`](CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md)
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

Older completed/superseded implementation routes live under [`archive/`](archive/README.md). Completed integration/correctness-repair routes are archived under `archive/checkpoints/sde/`. 默认阅读路径只包含当前长期语义、架构、产品与 evidence-gate 权威；少数近期 SDE-3 completion audit 仍暂留 `docs/` 根目录作为 bounded historical evidence，但不进入默认阅读、也不承担 execution authority。若以后要做大批量文件搬迁，应作为独立纯文档整理处理。任何历史 `NEXT`/PR state 都不是 execution authority。

## Status authority

If documents conflict, use this order:

1. official BoTC rules/rulings — gameplay correctness;
2. root `AGENTS.md` — execution / architecture / governance;
3. `TESTING_STRATEGY.md` — test-tier and validation evidence definitions;
4. `CURRENT_DEVELOPMENT_ROADMAP.md` + `NEXT_DEVELOPMENT_HANDOFF.md` + `LRE_STAGED_POLICY_REPLACEMENT_AND_RETIREMENT_ROUTE_2026-10-04.md` + `LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md` — current state, continuation point, replacement/cutover order and legacy-retirement authority;
5. `AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md` — current GitHub Connector-first / Mini MCP+Codex large-file execution workflow;
6. `SDE_3_PROVISIONAL_POLICY_AND_CONTINUOUS_CALIBRATION_ROUTE_2026-09-23.md` plus current 3D audit — long-lived SDE/calibration contract where not superseded by DLB lifecycle changes;
7. `SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md` and targeted evidence contract — policy/evidence background; its pre-DLB setup/sequence model is historical;
8. external evidence catalog — source inventory/provenance, not standalone normative truth;
9. long-lived architecture/reference docs;
10. Mini MCP developer memory — optional advisory navigation/experience only;
11. completed/superseded workflow docs, slice audits, archive and Git history — historical/completion evidence only.