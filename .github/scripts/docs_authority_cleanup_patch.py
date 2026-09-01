from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one match, found {count}")
    return text.replace(old, new, 1)


# 1) Root documentation authority: replace only the final documentation section.
agents_path = Path("AGENTS.md")
agents = agents_path.read_text(encoding="utf-8")
marker = "## 8. Current project documents and precedence\n"
if agents.count(marker) != 1:
    raise SystemExit(f"AGENTS section marker: expected one match, found {agents.count(marker)}")
prefix, _ = agents.split(marker, 1)
new_agents_tail = '''## 8. Current project documents and precedence

### 8.1 Current authorities

Read these according to task scope:

1. `docs/CURRENT_DEVELOPMENT_ROADMAP.md` — **single current project-status, active-scope and execution-sequence authority**;
2. the one `docs/NEXT_DEVELOPMENT_HANDOFF_*.md` explicitly named as ACTIVE by the current roadmap — current slice execution detail;
3. `docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md` — current clue-selection product/manual/recommendation architecture;
4. `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md` — current Productive Uncertainty / misinformation-quality campaign design;
5. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md` — current Chat/connector/Luna execution contract;
6. `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` — normative large/truncated-file one-shot patch SOP;
7. `docs/TESTING_STRATEGY.md` — authoritative test tiers, evidence model, and subsystem mapping.

### 8.2 Foundational / supporting documents

- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md` remains the detailed Possible Worlds / epistemic foundation, but its historical phase sequence and AUTO/ASSISTED front-door assumptions are **not** current product/execution authority where superseded by the roadmap, clue UX decision, or Productive Uncertainty plan.
- `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`, `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`, and `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md` remain supporting evidence for their domains.
- `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md` remains connector guidance where non-conflicting.
- `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md` is older local-worktree guidance and is subordinate where it conflicts with V2 or this file.

### 8.3 Historical handoff rule

Dated handoffs/checkpoints are **historical evidence by default**. A handoff is current only when the current roadmap explicitly names it as ACTIVE.

Do not choose a handoff merely because it has the newest filename/date. Do not execute an older MS-SETUP, architecture-hardening, decomposition, or same-night handoff as current work unless the roadmap explicitly reactivates it.

### 8.4 Precedence

If documents disagree, apply this precedence:

1. newest explicit user instruction;
2. this root `AGENTS.md` for repository-wide working/governance rules;
3. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md` for execution mechanics and `docs/TESTING_STRATEGY.md` for test/evidence definitions;
4. `docs/CURRENT_DEVELOPMENT_ROADMAP.md` for current live scope, campaign ordering and active document set;
5. current domain-specific approved authority documents named by the roadmap (for example clue UX or Productive Uncertainty);
6. the active handoff for the current implementation slice;
7. foundational/historical documents only where non-conflicting.

Live GitHub state overrides stale embedded branch/PR/SHA status in any document. Re-query GitHub, distinguish executable checkpoints from later docs-only heads, then correct stale/conflicting current documentation instead of silently carrying the conflict forward.
'''
agents_path.write_text(prefix + new_agents_tail, encoding="utf-8")


# 2) v2.2: preserve the detailed foundation, but remove stale claims that it is the
# current execution/product-UX authority.
v22_path = Path("docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md")
v22 = v22_path.read_text(encoding="utf-8")

old_meta = '''> 版本：2.2  
> 日期：2026-08-11  
> 动态决策架构修订：2026-08-15
> 状态：当前唯一实施规范  
> 适用范围：优先覆盖《暗流涌动》（Trouble Brewing），架构支持后续剧本扩展  
> 取代文档：v2.0、v2.1；旧文档仅保留为设计演进记录  
> 当前实施基线：A0、A1、A2、A1.1、A2.1、A3 已完成；A4 已启动
> 动态决策实施合同：`storyteller_revision_driven_dynamic_decision_engine_plan.md`
'''
new_meta = '''> 版本：2.2
> 日期：2026-08-11
> 动态决策架构修订：2026-08-15
> Authority clarification：2026-09-01
> 状态：**Possible Worlds / 玩家认知一致性基础设计 authority；不再是当前实施顺序或产品 UX 的唯一 authority**
> 适用范围：优先覆盖《暗流涌动》（Trouble Brewing），架构支持后续剧本扩展
> 取代文档：v2.0、v2.1；旧文档仅保留为设计演进记录
> 当前执行顺序：以 `CURRENT_DEVELOPMENT_ROADMAP.md` 为准
> 当前 clue/manual/recommendation 产品边界：以 `CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md` 为准
> 当前 Productive Uncertainty 实施计划：以 `EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md` 为准
'''
v22 = replace_once(v22, old_meta, new_meta, "v2.2 metadata")

old_pipeline = '''Formal Game State + Interaction-scoped Registration Semantics
                    ↓
Legal Choice Layer
                    ↓
PlayerKnowledgeSnapshot + EpistemicHypothesis
                    ↓
PlayerWorldSet(P, t, hypothesisMode)
                    ↓
Candidate Simulation: beforeWorlds → observation → afterWorlds
                    ↓
Epistemic Metrics + Structural Metrics + Narrative Metrics
                    ↓
Shared Quality Gates
                    ↓
Runtime Storyteller Policy
                    ↓
AUTO / ASSISTED Unified Selector
'''
new_pipeline = '''Formal Game State + Interaction-scoped Registration Semantics
                    ↓
Complete Legal Semantic Candidate Domain
                    ↓
Shared Information-Decision Authority
          ├── Manual selection
          └── Recommendation Provider
                    ↓
PlayerKnowledgeSnapshot + EpistemicHypothesis
                    ↓
PlayerWorldSet(P, t, hypothesisMode)
                    ↓
Candidate Simulation: beforeWorlds → observation → afterWorlds
                    ↓
Epistemic Metrics + Structural Metrics + Narrative Metrics
                    ↓
Shared Quality Gates / Productive Uncertainty
                    ↓
RecommendationResult (primary + 0–2 alternatives)
'''
v22 = replace_once(v22, old_pipeline, new_pipeline, "v2.2 top-level pipeline")

old_single = '''### 2.1 单一实施规范

从本版开始：

- v2.2 是后续实现、审查和验收的唯一主规范；
- v2.0、v2.1 仅说明设计演进，不再作为并列要求来源；
- 专题文档可补充实现细节，但不得改变 v2.2 的规则权威、数据边界或退出条件；
- 如果专题文档与 v2.2 冲突，必须先修订 v2.2 或显式记录新的决策。
'''
new_single = '''### 2.1 文档角色与当前权威

v2.2 继续作为玩家认知一致性、Possible Worlds、知识边界、registration 和 exact-world correctness 的详细基础设计文档，但 2026-09-01 之后不再承担全部当前实施顺序与产品 UX 的唯一 authority。

当前规则为：

- v2.0、v2.1 仅说明设计演进，不再作为并列要求来源；
- `CURRENT_DEVELOPMENT_ROADMAP.md` 决定当前 campaign 顺序、active PR/slice 和恢复入口；
- clue manual/recommendation 产品边界由 `CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md` 决定；
- misinformation-quality / Productive Uncertainty 当前 campaign 由对应 2026-09-01 专题计划决定；
- 本文后续出现的旧阶段顺序、AUTO/ASSISTED front-door 或历史 rollout 名称，在与上述 current authorities 冲突时视为历史设计背景，而不是当前执行要求；
- 本文关于官方规则权威、玩家知识隔离、interaction-scoped registration、A3 exact correctness baseline、unknown ≠ UNSAT 等基础不变量继续有效，除非新的显式架构决策另行修订。
'''
v22 = replace_once(v22, old_single, new_single, "v2.2 authority section")

historical_anchor = '本版新增的关键决定：\n'
if v22.count(historical_anchor) != 1:
    raise SystemExit(f"v2.2 decision-list anchor: expected one match, found {v22.count(historical_anchor)}")
v22 = v22.replace(
    historical_anchor,
    '本版新增的关键决定（以下阶段编号/rollout 顺序记录 v2.2 当时的设计历史；当前执行顺序以 roadmap 为准）：\n',
    1,
)

v22_path.write_text(v22, encoding="utf-8")
