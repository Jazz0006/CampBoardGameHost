# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-10 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current project state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / FULL accepted / merged
R3 transaction-application viability audit        COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE

UI-R5 square-table convergence                    COMPLETE / merged in PR #117
UI-R5 final logical T4                            PASS
UI-R5 post-merge main CI                          PASS
UI-R5 Field Test APK                              PASS
UI-R5 real-device acceptance                      PARTIAL / follow-up remains

EPI-MQ / Productive Uncertainty                   NEXT — EPI-MQ-0 baseline re-audit
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign evidence is historical and must not be treated as current execution authority.

## 2. Live baseline after UI-R5 merge

```text
main: b47b00fd0c727e048dcb1b57260b8dd6fff466a1
merged PR: #117 — UI: unify Storyteller night roles on square table
final branch head: 99bb4e5323f68231eaa1e08e16520a4340f56faf
final logical T4 checkpoint: 2958f334fc7cccd59ed2e75a3bdaa60684492292
```

Validation evidence:

```text
T4 CI #2143 / run 34435295215             PASS
- Android full unit tests + debug APK      PASS
- ASP contract tests                       PASS
- Real Clingo cross-validation             PASS
- CI gate                                  PASS
R2 #2010 / run 34435295217                PASS

post-merge main CI #2146 / run 34437247431 PASS
Field Test APK #34 / run 34437247434       PASS
```

PR #117 is closed and merged. Its former branch/handoff/audit documents are historical evidence only.

## 3. Current priority — EPI-MQ-0 baseline re-audit

> **CURRENT: restart the Epistemic Misinformation Quality / Productive Uncertainty program from the merged live architecture, beginning with a read-only design/ownership audit and behavior corpus.**

The long-lived product objective remains:

> For Drunk/Poisoned information, choose misinformation that creates credible, interactive, sustainable, breakable and fair mistaken worlds rather than merely choosing an answer that is locally false.

The existing design plan is:

`docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`

That document predates several later architecture/UI campaigns. Its PR #61 / MS-S6D wording is historical context, not a current implementation instruction. The first task is therefore **re-audit, not immediate production ranking changes**.

## 4. EPI-MQ-0 execution contract

Read first:

1. root `AGENTS.md`;
2. this roadmap;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`;
4. `docs/TESTING_STRATEGY.md`;
5. `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`;
6. `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`;
7. `docs/epistemic_reference_matrix.md` and `docs/asp_oracle_cross_validation.md` only where relevant.

Immediate sequence:

```text
EPI-MQ-0A  confirm live main / architecture / existing epistemic owners
EPI-MQ-0B  map legal misinformation candidate -> hypothetical visible observation -> world evaluation path
EPI-MQ-0C  build a small Trouble Brewing behavior corpus: good / acceptable / poor misinformation
EPI-MQ-0D  identify the narrow typed seam and test owner for BEFORE/AFTER hypothetical world evaluation
EPI-MQ-0E  record GO / MODIFY / NO-GO for the old EPI-MQ-1 proposal
```

Do **not** change production recommendation ranking, weights or Storyteller-visible output during EPI-MQ-0 merely because the old plan names a desired pipeline.

If EPI-MQ-0 concludes that a substantial production edit is appropriate, apply the root `AGENTS.md` recorded architecture pre-flight gate before editing protected/core or >1000 LOC handwritten source.

## 5. Architectural constraints carried forward

Preserve unless a new audit explicitly proves a better boundary:

- `ClocktowerGameSession` remains the canonical writable session owner;
- Planner/Reducer keep gameplay-rule authority;
- role semantics / legal candidate generation remain separate from misinformation-quality ranking;
- player-visible epistemic replay must not ingest Storyteller-hidden action targets merely to make scoring convenient;
- `AbilityObservation`, historical timeline and world-set semantics remain typed; do not reconstruct semantics from localized UI strings;
- timeouts/resource exhaustion in exact/compressed world evaluation must not be interpreted as false UNSAT;
- UI composition must not become the owner of epistemic/ranking truth;
- no broad Host/App-root rewrites or renewed D6 decomposition for file-size reasons;
- no recommendation-provider replacement during EPI-MQ-0 unless the audit proves it is a prerequisite and the roadmap is explicitly updated.

## 6. UI-R5 post-merge device follow-up

UI-R5 implementation and automated validation are complete and merged. Real-device acceptance remains a **field-test follow-up**, not an excuse to keep the development campaign open indefinitely.

Already user-reported device PASS:

- Washerwoman / Librarian / Investigator pair-information baseline;
- Chef square-table.

Still worth exercising during normal field testing:

- Empath;
- Undertaker;
- Ravenkeeper;
- Spy;
- Clockmaker;
- Sage;
- original dynamic-trigger regression path:
  `Monk protects Ravenkeeper -> change Monk target -> Demon kills Ravenkeeper -> Ravenkeeper ability step appears`.

Any concrete device defect should become a focused bugfix with its own reproduction/test evidence. Do not infer a device PASS from CI.

## 7. Historical UI-R5 evidence

UI-R5 handoffs have moved to:

`docs/archive/handoffs/`

UI-R5 audits/acceptance evidence and closeout index have moved to:

`docs/archive/checkpoints/ui-r5/`

The durable product design remains active at:

`docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

Do not load the full UI-R5 archive in a normal EPI-MQ session.

## 8. Next planned program after EPI-MQ

`UX-R6 recommendation-provider replacement` remains queued after EPI-MQ, as previously recorded in the documentation index. It is **not authorized by the current EPI-MQ-0 audit** and may be reprioritized later by an explicit roadmap update.

## 9. Current active handoff

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`

Only this handoff is current. Any `NEXT_DEVELOPMENT_HANDOFF*` under `docs/archive/` is historical.
