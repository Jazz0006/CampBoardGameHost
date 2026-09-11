# NEXT DEVELOPMENT HANDOFF — EPI-MQ-0 Baseline Re-audit

> Date: 2026-09-11 Australia/Sydney  
> Status: **COMPLETE / SUPERSEDED — audit finished; EPI-MQ implementation paused behind UX-MODE-1**  
> Program: Epistemic Misinformation Quality / Productive Uncertainty  
> Merged predecessor: ROLE-ROTATION-1 via PR #119  
> Merge commit: `72775b63f2b98322f2ceb8972fd1cea8f7a46007`

## 0. Closeout

This handoff is historical evidence and is no longer the active execution authority.

The EPI-MQ-0 audit completed against live `main` at:

`42820353f5ea311f10b6e198ba34f6927edfd4ac`

Decision:

```text
old EPI-MQ-1 = MODIFY
```

The audit found that current architecture already provides most of the required candidate -> observation -> exact historical BEFORE/AFTER infrastructure. The revised design and future dynamic-script extensibility rules are recorded in:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

The current active task is now:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_UX_MODE_1_BEGINNER_EXPERIENCED.md`

EPI-MQ implementation resumes only after UX-MODE-1 is complete.

## 1. Audit baseline rule used

EPI-MQ-0 started from live `main` after the ROLE-ROTATION-1 merge and did not reuse an older UI-R5/UI-NAV or pre-merge SHA as the implementation baseline.

ROLE-ROTATION-1 acceptance evidence remains historical context only:

```text
T4 acceptance checkpoint:
4d75ed4a147304e3a01c2bc233dbdc33c802ea6f
CI #2274 / run 34556109636 — SUCCESS
Android full + debug APK — PASS
ASP contracts — PASS
Real Clingo — PASS
R2 #2136 — PASS

PR #119 merged at:
72775b63f2b98322f2ceb8972fd1cea8f7a46007
```

## 2. Historical read order

This was the read order used for the audit:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. live `main` / current branches / PR state;
6. `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`;
7. `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`;
8. `docs/epistemic_reference_matrix.md`;
9. `docs/asp_oracle_cross_validation.md` where exact-world oracle evidence was needed.

Completed ROLE-ROTATION, UI-NAV, UI-R5, D6 and persistence handoffs were historical evidence only.

## 3. Why re-audit was required

The EPI-MQ plan was written on 2026-09-01 and intentionally deferred. Since then the repository went through semantic hardening, decomposition, persistence simplification, UI convergence, navigation convergence, and recent-role-rotation persistence/setup wiring.

The audit therefore re-baselined actual live ownership instead of forcing old proposed types onto the current architecture.

## 4. Audit target that was completed

The audit traced the live path around:

```text
legal semantic candidate
-> candidate visible observation
-> recipient-visible history / knowledge snapshot
-> hypothetical replay / perceived world set
-> BEFORE vs AFTER world effects
-> candidate quality diagnostics
```

It answered:

1. where legal Drunk/Poisoned information candidates are generated;
2. where truth/reliability is separated from strategic recommendation quality;
3. the current typed representation of a player-visible ability observation;
4. whether an uncommitted hypothetical observation can be evaluated without mutating session/history/recovery state;
5. the current exact correctness oracle for Trouble Brewing;
6. the Storyteller-hidden facts that must be excluded from recipient knowledge;
7. whether a narrow typed seam already exists for EPI-MQ-1;
8. which assumptions in the 2026-09-01 plan were invalidated by later architecture work.

## 5. Behavior corpus retained for future EPI-MQ

The following corpus remains required when EPI-MQ implementation resumes:

- pair information: plausible false pair vs obviously disconnected/contradictory pair;
- numeric information: same numeric distance but different epistemic consequences;
- Fortune Teller repeated information / temporal consistency;
- Drunk durable mistaken-world behavior;
- Poisoned temporary impairment and later discoverability;
- Spy/Recluse registration as an alternative explanation;
- excessive confirmation-lock risk;
- candidate that would immediately reveal impairment through a public contradiction;
- equal world cardinality but different explanation structure / impairment exposure.

The first implementation should express comparative/invariant expectations rather than a giant exact numeric score table.

## 6. Resulting revised architecture

The audit established:

```text
InformationDecisionContext
-> EpistemicObservationDraft
-> non-mutating session preflight
-> neutral exact hypothetical evaluator [to be established]
-> BEFORE / AFTER diagnostics
```

`B4DynamicPlayerWorldSetShadow` already contains exact historical BEFORE/AFTER mechanics, but its shadow-only ownership fence must remain intact. Recommendation must not directly depend on B4.

The future implementation should extract the proven mechanics into a neutral epistemic owner and make both B4 and EPI-MQ consumers of that owner.

## 7. Future architecture pre-flight gate

The next EPI-MQ implementation phase must use the architecture pre-flight recorded in:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

It must also preserve explicit `DEFERRED / UNSUPPORTED` capability behavior for future custom/dynamic scripts rather than treating unmodelled role mechanics as absent.

## 8. Historical scope fence

EPI-MQ-0 did not authorize:

- production recommendation ranking or weight changes;
- Storyteller-visible recommendation output changes;
- recommendation-provider replacement;
- A4/ZDD activation;
- D6 reopening solely because a source file is large;
- UI-R5 / UI-NAV redesign;
- persistence/recovery ordering changes without evidence;
- ROLE-ROTATION policy changes;
- Storyteller-hidden action-target leakage into player knowledge.

## 9. Revised following sequence

The old direct sequence has been superseded by:

```text
UX-MODE-1  Beginner / Experienced Storyteller Mode

then

EPI-MQ-0.5  dynamic-script extensibility guard
EPI-MQ-1    neutral hypothetical observation evaluator
EPI-MQ-2    credibility + immediate contradiction + impairment-exposure gates
EPI-MQ-3+   metrics/ranking after earlier contracts are stable
```

UX-R6 recommendation-provider replacement remains a later program.
