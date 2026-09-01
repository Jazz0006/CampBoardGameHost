# CampBoardGameHost 文档入口

> Updated: 2026-09-01 Australia/Sydney
> Purpose: keep the active `docs/` root small, authoritative, and safe for new development sessions.

## 1. Start here

For a new development session, read in this order:

1. root `AGENTS.md`;
2. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — single current project-status / execution-sequence authority;
3. [`NEXT_DEVELOPMENT_HANDOFF_2026-09-01_UX_R2_DECISION_FOUNDATION.md`](NEXT_DEVELOPMENT_HANDOFF_2026-09-01_UX_R2_DECISION_FOUNDATION.md) — the one ACTIVE handoff;
4. the domain-specific authority needed for the task;
5. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md) and live GitHub state before implementation/merge.

Do not infer current work from archived handoffs, old checkpoint SHAs, or completed campaign documents.

## 2. Current campaign

Current Draft PR: **#63 — UX-R2 pair decision foundation**.

Current sequence:

```text
UX-R2A  pair semantic scenario contracts
UX-R2B  pair adoption of shared InformationDecision authority
UX-R2C  pair production vertical slice (next PR)
UX-R2D  manual-authority audit across major clue families
UX-R3/R4 remove global mode only after manual authority is complete
EPI-MQ  Productive Uncertainty / cognitive-consistency campaign
UX-R6   provider cutover behind the stable UI/decision contract
```

The global Manual front-door must not be removed until every currently supported major information family has an independent correct manual path.

## 3. Current product / algorithm authorities

- [`CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`](CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md) — clue recommendation/manual product boundary;
- [`EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`](EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md) — Productive Uncertainty campaign;
- [`CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`](CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md) — Possible Worlds / epistemic foundation;
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md), [`unified_semantic_model.md`](unified_semantic_model.md), and [`r6_p1_2_knowledge_timeline_semantics_2026-08-21.md`](r6_p1_2_knowledge_timeline_semantics_2026-08-21.md) — detailed supporting semantic references;
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md) and [`external_solver_evaluation.md`](external_solver_evaluation.md) — oracle/solver references.

## 4. Long-lived engineering authorities

- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md);
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md);
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md);
- [`SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`](SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md);
- [`SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`](SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md).

Same-night work is complete as a campaign. Keep the compact long-lived references in root:

- [`SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`](SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md);
- [`DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`](DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md).

Detailed completed-campaign architecture is archived.

## 5. Other retained foundational architecture

- [`多剧本多板子与动态游戏流程架构设计_v1.md`](多剧本多板子与动态游戏流程架构设计_v1.md).

A document can remain in root because it is a durable reference even when its date is old. Age alone is not an archival criterion.

## 6. Archive policy

Historical material is under [`archive/`](archive/README.md).

Archive when a document is primarily:

- a completed campaign checkpoint/audit;
- a closed handoff;
- superseded workflow/process guidance;
- superseded implementation design;
- paused unfinished work that is explicitly deferred.

Do not delete useful history merely because it is old. Archive preserves evidence while preventing it from competing with current authorities.

A file under `archive/` is never current execution authority unless `CURRENT_DEVELOPMENT_ROADMAP.md` explicitly reactivates it.
