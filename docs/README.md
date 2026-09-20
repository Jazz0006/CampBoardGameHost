# CampBoardGameHost 文档入口

> 最后整理：2026-09-20 Australia/Sydney  
> 目标：新开发会话只读取当前权威；完成阶段的审计、checkpoint 与旧 handoff 从 `archive/` 按需查阅。

## 默认阅读顺序

1. root `AGENTS.md`
2. [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
3. [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md) — **唯一当前状态 / 优先级权威**
4. [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md) — **唯一 active handoff**
5. [`SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md`](SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md) — 当前 D5F-B3 / v2 review model 权威
6. [`SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv`](SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv) — 当前 external-human review evidence
7. 需要理解后续 SDE-3 前置约束时，再读 [`SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`](SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md)
8. 需要全局 SDE 架构背景时，再读 [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)

然后查询 live branch / PR / checks。不要从 archive、旧 PR 或历史文档里的 `NEXT / READY / COMPLETE` 推断当前状态。

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
  D5F-A                                COMPLETE
  D5F-B infrastructure                 COMPLETE
  D5F-B3 policy-model correction       COMPLETE
  D5F-B v2 human review                NEXT

D5F-C gate/band derivation             BLOCKED
sealed holdout                         CLOSED
SDE-3                                  BLOCKED
~~~

Canonical human-review manifest:

`app/src/test/resources/review/sde-2d5f-human-label-manifest.tsv`

Version:

`d5f-b-calibration-v2`

It contains **11 reviewable records, all currently `UNREVIEWED`**.

The obsolete v1 manifest is retained only as historical evidence:

`app/src/test/resources/review/sde-2d5f-human-label-manifest-v1-obsolete.tsv`

Do not transfer provisional v1 judgments into v2.

## Current policy-model invariants

- Optimize only variables still owned and controllable at the current lifecycle stage.
- Drunk shown identity is persistent setup input after commit; SDE owns the unshown unreliable clue, not the shown role.
- Drunk review compares truthful / mild-false / stronger-false candidates on one same-setup surface; truth relation is descriptive, not a quality ranking.
- Chef / Empath healthy information is rule-determined after setup.
- Washerwoman / Librarian / Investigator may still expose Storyteller-controlled legal outputs.
- Fortune Teller pair selection belongs to the player.
- Red Herring is setup-controllable only before persistence.
- Poisoner target belongs to the Evil player; future Poisoner targeting is not setup mitigation.
- Demon bluff triplet is a planning output before reveal and persistent input after reveal.
- Shared/union bluff support is a coherence/fragility diagnostic, not a monotone quality objective.
- Healthy-bundle risk is joint confirmation-chain collapse, not “a single strong healthy clue is bad”.
- Do not add an opaque global confirmation score or a numeric coverage-complementarity reward.

Compatibility code intentionally retained until later production cutover:

- impaired-information approximate 90/10 false-family bridge;
- legacy `MalfunctionPolicy`.

Do not restore old recommendation-owned Drunk shown-role scoring.

## Active long-lived references

- [`CURRENT_DEVELOPMENT_ROADMAP.md`](CURRENT_DEVELOPMENT_ROADMAP.md)
- [`NEXT_DEVELOPMENT_HANDOFF.md`](NEXT_DEVELOPMENT_HANDOFF.md)
- [`SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md`](SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md)
- [`SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv`](SDE_2D5F_EXTERNAL_HUMAN_CASE_CATALOG_2026-09-20.tsv)
- [`SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`](SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md)
- [`STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`](STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md)
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`](GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md)
- [`EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`](EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md)
- [`epistemic_reference_matrix.md`](epistemic_reference_matrix.md)
- [`asp_oracle_cross_validation.md`](asp_oracle_cross_validation.md)

Completed FN-BUNDLE / SDE slice-level audit evidence has moved to:

- [`archive/checkpoints/fn-bundle/`](archive/checkpoints/fn-bundle/README.md)
- [`archive/checkpoints/sde/`](archive/checkpoints/sde/README.md)

## Normative engineering workflow

- root `AGENTS.md`
- [`TESTING_STRATEGY.md`](TESTING_STRATEGY.md)
- [`AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`](AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md)
- [`LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`](LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md) when applicable

## Status authority

If documents conflict, use this order:

1. official BoTC rules/rulings — gameplay correctness;
2. root `AGENTS.md` — execution / architecture / test rules;
3. `CURRENT_DEVELOPMENT_ROADMAP.md` — current state / priority;
4. `NEXT_DEVELOPMENT_HANDOFF.md` — current continuation point;
5. current specialized authority named by roadmap/handoff;
6. long-lived architecture/reference docs;
7. archive / old branches / old PRs / Git history — evidence only.
