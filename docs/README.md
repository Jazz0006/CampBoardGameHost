# CampBoardGameHost 文档入口

> 最后整理：2026-08-31 Australia/Sydney
> 目标：让新的开发会话只读少量真正有权威性的文档，不再被历史 handoff / checkpoint 淹没。

## 1. 新任务默认阅读顺序

1. 根目录 `AGENTS.md` — 项目级 AI / Git / 测试执行规范；
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态权威**；
3. 当前 active handoff；
4. 当前任务需要的 specialized design / semantic reference；
5. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)；
6. 查询 live GitHub state 后再实施。

不要从旧 commit SHA、旧 PR 状态、archive 文档或历史 handoff 推断当前状态。

## 2. 当前 active task

当前最高优先级：

```text
MS-SETUP — Generic Multi-Script Setup Architecture
MS-S0 — fresh live-state + ownership audit
```

Active handoff：

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md) — **CURRENT HANDOFF**

Merged baseline：

```text
PR #57 — TBSP: integrate Trouble Brewing setup presets
MERGED

main merge checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

post-merge full CI:
CI #1179 / run 33346311357 — SUCCESS
```

Always re-query live `main` before implementation. The current task is planning/audit first; do not begin MS-S1 production work before MS-S0 completes.

## 3. Current setup-architecture references

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md) — current generic setup architecture handoff；
- [`TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`](TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md) — accepted Trouble Brewing behavior to preserve during genericization；
- [`TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`](TBSP_ROTATION_WEIGHT_CONTRACT_V1.md) — accepted TB diversity/rotation semantics；
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — T0/T1/T2/T3/T4 validation strategy.

Frozen Trouble Brewing preset dataset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
```

Do not regenerate or reformat it during MS-SETUP genericization.

## 4. Current MS-SETUP sequence

```text
MS-S0  live-state + ownership audit                          NEXT
MS-S1  generic CommittedClocktowerSetup / provenance model  PLANNED
MS-S2  generic SetupCandidate + candidate-source contract   PLANNED
MS-S3  optional TemplateRepository                          PLANNED
MS-S4  deterministic GeneratedSetupCandidateSource          PLANNED
MS-S5  common SetupDiversityHistory / scorer / selector     PLANNED
MS-S6  generic shown-identity policy                        PLANNED
MS-S7  adapt accepted TB pipeline with parity               PLANNED
MS-S8  adapt NGJ/no-template path with parity               PLANNED
MS-S9  generic acceptance / future-script proof             PLANNED
A3 immutable setup snapshot                                 DEFERRED / NOT CURRENT
```

TBSP-1 through TBSP-6L are complete, accepted, and merged. Do not reopen them without concrete regression evidence.

## 5. Active long-lived architecture / semantic references

These remain in the active docs root because they define long-lived contracts rather than temporary branch state.

### Same-night / night transaction

- [`SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md`](SAME_NIGHT_EFFECTIVE_STATE_ARCHITECTURE_2026-08-25.md)
- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md)
- [`SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`](SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md)
- [`DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`](DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md)
- [`SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`](SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md)

### Information / epistemic architecture

- [`R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md`](R6_IMPAIRED_INFORMATION_AND_STORYTELLER_DECISION_DESIGN_2026-08-22.md)
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md)
- [`r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`](r6_p1_2_knowledge_timeline_semantics_2026-08-21.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`unified_semantic_model.md`](unified_semantic_model.md)

### Future architecture references

- [`storyteller_revision_driven_dynamic_decision_engine_plan.md`](storyteller_revision_driven_dynamic_decision_engine_plan.md)
- [`storyteller_a4_zdd_prototype.md`](storyteller_a4_zdd_prototype.md)
- [`storyteller_a4_5_observation_cache_rebuild_spec.md`](storyteller_a4_5_observation_cache_rebuild_spec.md)
- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)
- [`external_solver_evaluation.md`](external_solver_evaluation.md)

These documents may describe older milestones, but remain because their primary role is semantic/design authority, not current status.

## 6. Deferred unfinished work

Unfinished future work that is not currently active belongs under `archive/deferred/` and may only be resumed after the roadmap explicitly reactivates it.

Important deferred items include:

- `archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md` — immutable setup-snapshot ownership/persistence；
- `archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_APP_ROOT_S9.md` — App Root S9.2 persistence boundary.

A deferred handoff is context only. Before resuming it, re-audit live `main` and current architecture.

## 7. Historical handoff cleanup

Completed execution handoffs from GCR, PR55/PR56 and TBSP-1..5 are no longer current instructions and have been removed from the active docs root.

Historical index:

- [`archive/TBSP_AND_PREDECESSOR_HANDOFF_CLOSEOUT_2026-08-30.md`](archive/TBSP_AND_PREDECESSOR_HANDOFF_CLOSEOUT_2026-08-30.md)

Git history and the corresponding PRs preserve the exact old files when detailed provenance is required.

## 8. Normative development workflow

- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — T0/T1/T2/T3/T4 test strategy；
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md) — current AI development workflow；
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) — normative large/truncated-file one-shot workflow + Python patch SOP；
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md) — GitHub connector workflow；
- [`CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`](CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md) — historical/large-file local workflow where not superseded by V2；
- [`github_connector_large_file_editing_playbook.md`](github_connector_large_file_editing_playbook.md) — connector large-file constraints/playbook.

## 9. Documentation lifecycle policy

Active docs root should contain only:

```text
CURRENT_DEVELOPMENT_ROADMAP.md
one active NEXT_DEVELOPMENT_HANDOFF_*.md
long-lived architecture / semantic references
normative test / workflow references
```

When a task closes:

- completed execution handoffs and micro-checkpoints should leave the active root;
- if only historical traceability remains, consolidate them into an archive closeout/index and rely on Git/PR history for exact old content;
- if the document carries genuinely unfinished future work, move it intact under `archive/deferred/`;
- do not create a new handoff for every GREEN micro-step;
- update the existing active handoff and roadmap instead.

## 10. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` controls current project state;
4. the one active task handoff controls the approved narrow implementation plan;
5. specialized design docs control their semantic/architecture domain;
6. archive / historical Git / old PR records are evidence, not current instructions.
