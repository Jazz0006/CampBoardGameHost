# CampBoardGameHost 历史文档归档

> 该目录中的文档只用于追溯设计演进、历史审计、阶段验收和已关闭 handoff。  
> **不得把 archive 中的 `PASS / COMPLETE / READY / NEXT` 当作当前开发状态。**

当前开发入口：[`../README.md`](../README.md) 与 [`../CURRENT_DEVELOPMENT_ROADMAP.md`](../CURRENT_DEVELOPMENT_ROADMAP.md)。

## 2026-10-09 global documentation convergence

The full pre-MEM0 authoritative texts are archived as **historical, non-executable snapshots**:

- [Former current roadmap](checkpoints/CURRENT_DEVELOPMENT_ROADMAP_PRE_MEM0_CONVERGENCE_2026-10-09.md) — 62 KB of sequential project history, PR/CI/R2 references and prior NEXT labels.
- [Former active handoff](handoffs/NEXT_DEVELOPMENT_HANDOFF_PRE_MEM0_CONVERGENCE_2026-10-09.md) — cumulative 46 KB handoff history.
- [Former docs index](checkpoints/DOCS_INDEX_PRE_MEM0_CONVERGENCE_2026-10-09.md) — superseded default reading list and stale SDE/GSP status summaries.

These are faithful source copies plus an archive banner and a link to their exact original Git commit, so prior evidence remains inspectable. **2026-10-10 documentation cleanup:** active entry documents no longer treat MEM0/API0 or PROD-0 as NEXT; the originally retained API0 and MEM0 experiment files are explicitly marked historical/non-blocking rather than deleted or duplicated. Full-product work now follows PROD-GLOBAL-1D (measured API latency, shown-role reveal overlap and global event-driven replanning) in [current roadmap](../CURRENT_DEVELOPMENT_ROADMAP.md) and [current handoff](../NEXT_DEVELOPMENT_HANDOFF.md). Older checklists and prior PR/CI results are evidence only; their NEXT labels have no execution authority.

## 1. Directory layout

```text
handoffs/     closed or superseded NEXT handoffs
checkpoints/  completed implementation/test/checkpoint evidence
               including fn-bundle/ and sde/ campaign histories
ui/           superseded UI plans and UI campaign closeout evidence
deferred/     unfinished but explicitly deferred future work
workflows/    superseded workflow instructions
```

Files directly under `archive/` are older consolidated closeouts, superseded design versions, or historical reports retained for traceability.

### 2026-09-10 UI-R5 closeout

The completed UI-R5 square-table convergence evidence is grouped under:

```text
checkpoints/ui-r5/
```

Use `checkpoints/ui-r5/UI_R5_CLOSEOUT_INDEX_2026-09-10.md` as the historical entry point. Closed UI-R5 handoffs live under `handoffs/`.

UI-R5 implementation was merged in PR #117 at main commit `b47b00fd0c727e048dcb1b57260b8dd6fff466a1`. Remaining real-device exercises are field-test follow-up, not an active broad UI-R5 migration campaign.

### 2026-09-09 D6 closeout

The completed D6.0 / D6.1 / D6.2 / R3 decomposition evidence is grouped under:

```text
checkpoints/d6/
```

Use `checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md` as the historical entry point. The individual D6 audit/progress files should not be loaded by default in new sessions.

### 2026-09-20 FN-BUNDLE / SDE checkpoint consolidation

Completed FN-BUNDLE and SDE slice-level audits were moved out of active `docs/` so new sessions do not load obsolete execution details by default.

Historical indexes:

- [`checkpoints/fn-bundle/README.md`](checkpoints/fn-bundle/README.md)
- [`checkpoints/sde/README.md`](checkpoints/sde/README.md)

Current SDE status must be read from `../CURRENT_DEVELOPMENT_ROADMAP.md`, `../NEXT_DEVELOPMENT_HANDOFF.md`, and the current specialized authority named there.

### 2026-09-20 completed host/UI audit consolidation

Additional completed campaign evidence was moved out of active `docs/`:

- Experienced Night Flow S2–S4 -> [`checkpoints/experienced-night-flow/`](checkpoints/experienced-night-flow/README.md)
- UI-NAV-1 audit/closeout -> `ui/`
- superseded R6 impaired-information design -> `checkpoints/sde/`
- source-string test retirement evidence -> `checkpoints/`

These moves are documentation lifecycle cleanup only; they do not reactivate or change the archived decisions.

### 2026-09-21 D5F policy consolidation

The active D5F route was consolidated around expert-observed calibration.

Three 2026-09-20 active intermediate documents were deleted rather than retained as parallel authorities:

- `SDE_2D5_POLICY_MODEL_CORRECTION_AUDIT_2026-09-20.md`;
- `SDE_2D5F_EXTREME_FIXTURE_CALIBRATION_SCOPE_CORRECTION_2026-09-20.md`;
- `SDE_2D5F_REPRESENTATIVE_HEALTHY_INFORMATION_CORPUS_DESIGN_2026-09-20.md`.

Their Git history remains available if historical reconstruction is needed. Current conclusions were folded into:

- `../SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`;
- `../CURRENT_DEVELOPMENT_ROADMAP.md`;
- `../NEXT_DEVELOPMENT_HANDOFF.md`.

The previous external-human catalog was renamed/reclassified as an external evidence source catalog. Existing entries are SILVER/QUALITATIVE seeds until expert provenance is independently verified.

### 2026-09-28 SDE route consolidation

Two completed execution routes were removed from the active `docs/` root after their work and later integration gates had closed:

- [`checkpoints/sde/SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md`](checkpoints/sde/SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md) — historical C0–C3 integration/acceptance route;
- [`checkpoints/sde/SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md`](checkpoints/sde/SDE_POST_AUDIT_CORRECTNESS_REPAIR_ROUTE_2026-09-25.md) — historical CR-A/B/C / IF-D / RH-E repair route.

Their embedded pending/next-step language is historical only. Current execution and evidence gates live in `../CURRENT_DEVELOPMENT_ROADMAP.md`, `../NEXT_DEVELOPMENT_HANDOFF.md`, and `../SDE_E3_E4_QUALIFICATION_AUDIT_2026-09-27.md`.

### 2026-09-29 DLB authority consolidation

The product lifecycle changed from pre-seat Drunk ownership to late-bound Drunk assignment and staged first-night dependency barriers. The DLB campaign is now COMPLETE / ACCEPTED; those documents remain historical/architectural references rather than the current execution lane.

Historical DLB authorities:

- `../DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`
- `../DRUNK_LATE_BINDING_AND_FIRST_NIGHT_DECISION_SEQUENCE_AUDIT_2026-09-28.md`
- `../APP_HOST_DECOMPOSITION_PLAN_AUDIT_2026-09-28.md`

Fully superseded active documents moved into archive:

- `TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md` — historical pre-seat `selectedDrunkShownRole` normative contract;
- `checkpoints/sde/SDE_3B1_HISTORICAL_LIFECYCLE_INPUT_BINDING_COMPLETION_AUDIT_2026-09-23.md`;
- `checkpoints/sde/SDE_3B_STRUCTURE_AND_EXPERT_EVIDENCE_STAGING_2026-09-23.md`.

The first-night policy synthesis remains in active docs only as policy/evidence background because many of its evidence principles remain useful; its old setup/sequence model is explicitly marked superseded rather than duplicated into a second current authority.

### 2026-10-04 LRE consolidation / 2026-10-05 GSP supersession

The product owner first broadened RecommendationStyle retirement into retirement of the whole hand-tuned legacy heuristic recommendation engine. LRE-0 retirement evidence and LRE-1 Manual/fail-closed safety remain accepted.

On 2026-10-05 the family-by-family special-case replacement continuation was superseded. Current execution authority is now:

- `../GSP_GENERAL_STORYTELLER_POLICY_ROUTE_2026-10-05.md` for recommendation/provider/benchmark continuation;
- `../LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md` for retirement evidence/inventory;
- `../CURRENT_DEVELOPMENT_ROADMAP.md` and `../NEXT_DEVELOPMENT_HANDOFF.md` for live priority/status.

`LRE_STAGED_POLICY_REPLACEMENT_AND_RETIREMENT_ROUTE_2026-10-04.md` is archived here as a historical route record only; its LRE-P loop is not current authority.

The following completed/superseded intermediate audits were removed from active `docs/` and retained directly under `archive/` for traceability:

- `TBGS_2_POST_2E_STYLE_RETIREMENT_REAUDIT_2026-10-04.md`;
- `RECOMMENDATION_STYLE_RETIREMENT_PLAYER_LEVEL_AUDIT_2026-10-04.md`;
- `RSR_1_STYLE_NEUTRAL_POLICY_READINESS_AUDIT_2026-10-04.md`.

Their embedded NEXT/continuation language is historical. Current sequence is GSP-0 route reset -> GSP-1 special-policy authority revocation -> provider/global-context contract -> manual benchmark before any API integration -> later deployment/model and physical cleanup decisions.

## 2. Handoffs

A handoff moves to `handoffs/` when its execution contract is completed, cancelled, or superseded by a new active handoff.

Even if the filename still contains `NEXT_DEVELOPMENT_HANDOFF`, it is not current. Only the single handoff linked from `../README.md` and `../CURRENT_DEVELOPMENT_ROADMAP.md` is active.

## 3. Checkpoints

`checkpoints/` contains historical slice-level implementation, audit and acceptance records such as completed MS/TBSP/UI/D6 checkpoints.

Their detailed SHAs, CI results and file lists remain useful evidence, but they should not be loaded by default in new development sessions.

## 4. Superseded UI plans

`ui/` contains UI plans/closeouts whose implementation campaign has completed or whose product assumptions were later changed.

In particular, pre-PR #100 plans that require a distinct WAKE acknowledgement state are historical. Current Night actor/wake behavior must be read from live code and the current roadmap/handoff.

## 5. Deferred unfinished work

`deferred/` is different from completed history: these files may describe unfinished future work, but current execution is not authorized until the roadmap explicitly reactivates it and a fresh live-state audit confirms the assumptions.

## 6. Superseded workflows

`workflows/` contains older process documents replaced by root `AGENTS.md` and current workflow/testing documents.

The 2026-09-24 M8G5 Mini MCP remote-control adoption record is archived as `workflows/MINI_MCP_REMOTE_PR_CONTROL_PLANE_ADOPTION_2026-09-24.md`. It records the then-valid Mini MCP-first control-plane experiment and is superseded by root `AGENTS.md` plus `../AI_DEVELOPMENT_WORKFLOW_CURRENT_2026-09-27.md`, which restore GitHub Connector-first operation and retain Mini MCP/Codex primarily for complete-local-context work.

The superseded `AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md` and `MINI_MCP_DEVELOPMENT_WORKFLOW_AND_MEMORY_ADOPTION_2026-09-25.md` were also moved into `workflows/` on 2026-09-28 so only the current workflow remains in the active docs root.

Do not resurrect an archived workflow merely because a historical handoff references it.

## 7. Older algorithm / architecture history

Older algorithm versions, previous recommendation implementation descriptions and historical A3/V4 acceptance reports remain archived for traceability. A historical PASS is evidence of that checkpoint only and does not override newer audits or current architecture.

## 8. Archive rule

Archive when any of these is true:

- a higher-version/current specification clearly supersedes the document;
- the corresponding implementation phase is complete and the file is now only evidence;
- an acceptance conclusion was superseded or revoked;
- a newer unified plan replaces the old path;
- the document mainly describes an implementation path that no longer exists;
- a handoff is unfinished but explicitly deferred.

Keep in active docs root when the document is a long-lived semantic/architecture/product/workflow authority or an active/future design that remains intentionally referenced.

Recommended lifecycle:

```text
active status / current handoff
-> docs root

completed slice evidence
-> archive/checkpoints or archive/ui

closed/superseded handoff
-> archive/handoffs

unfinished but deferred
-> archive/deferred

superseded process
-> archive/workflows
```

Any task resumed from archive must first re-query live repository state and re-establish ownership, scope and evidence requirements.
