# CampBoardGameHost 文档入口

> 最后整理：2026-08-30 Australia/Sydney  
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
TBSP — Trouble Brewing Setup Preset Integration
TBSP-6G-A — setup recommendation prewarm coordinator
```

Active handoff：

- [`NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md`](NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md) — **CURRENT HANDOFF**

Current code checkpoints：

```text
last fully GREEN code checkpoint:
5c10cd29111449e1f8af2b8944609a2002048679

current TBSP-6G RED code checkpoint:
a26c221670fdea2612626f762d162b66091896af
```

Later docs-only commits may sit on top of the RED checkpoint. Do not treat docs-only CI as proof that 6G code is GREEN.

PR #57 remains Draft/Open/Unmerged unless explicitly authorized otherwise.

## 3. TBSP active normative references

- [`TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`](TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md) — Trouble Brewing production cutover / non-blocking reveal / restore / no-reroll acceptance contract；
- [`TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`](TBSP_ROTATION_WEIGHT_CONTRACT_V1.md) — frozen selector rotation weighting policy；
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) — T0/T1/T2/T3/T4 validation strategy.

The final preset dataset itself is frozen under:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
```

Do not regenerate or reformat it during TBSP-6.

## 4. Current TBSP sequence

```text
TBSP-1..5                         COMPLETE
TBSP-6A provenance codec          COMPLETE
TBSP-6B setup preparer            COMPLETE
TBSP-6C deal-role resolver        COMPLETE
TBSP-6D production start cutover  COMPLETE
TBSP-6E active restore provenance COMPLETE
TBSP-6F completion history wiring COMPLETE
TBSP-6G-A prewarm core            CURRENT RED
TBSP-6G-B reveal wiring           NEXT
TBSP-6H First Night precompute    LATER
TBSP-6I acceptance matrix         LATER
TBSP-6J cleanup                   LATER
TBSP-6K full acceptance           FINAL TBSP GATE
A3 immutable setup snapshot       DEFERRED UNTIL TBSP ACCEPTED
```

Do not reopen completed selector/deal/history work without concrete regression evidence.

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
