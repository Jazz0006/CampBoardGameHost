# NEXT DEVELOPMENT HANDOFF — EPI-MQ-0 Baseline Re-audit

> Date: 2026-09-11 Australia/Sydney  
> Status: **NEXT EXECUTION TARGET — begin immediately after PR #119 merges**  
> Program: Epistemic Misinformation Quality / Productive Uncertainty  
> Base rule: **re-query live `main` after PR #119 merge before creating the EPI-MQ branch or accepting any historical ownership assumption**

## 1. Transition state

ROLE-ROTATION-1 is complete, T4 validated, and merge-ready in PR #119. Once that PR is merged, EPI-MQ-0 becomes the active development task.

Known pre-merge anchors are evidence only:

```text
PR #119 base main:
3cc3d64d303236ae84b7fb14eaf066cc10aec95e

ROLE-ROTATION-1 T4 acceptance checkpoint:
4d75ed4a147304e3a01c2bc233dbdc33c802ea6f
CI #2274 / run 34556109636 — SUCCESS
Android full + debug APK — PASS
ASP contracts — PASS
Real Clingo — PASS
R2 #2136 — PASS
```

Do not use either SHA as the EPI-MQ implementation baseline after merge. Resolve the actual live `main` and merged PR #119 commit first.

## 2. Read first

When EPI-MQ-0 starts:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. live `main` / current branches / PR state;
6. `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`;
7. `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`;
8. `docs/epistemic_reference_matrix.md`;
9. `docs/asp_oracle_cross_validation.md` only where exact-world oracle evidence is needed.

Completed ROLE-ROTATION, UI-NAV, UI-R5, D6 and persistence handoffs are historical evidence only and should not be loaded by default.

## 3. Why re-audit before implementation

The EPI-MQ plan was written on 2026-09-01 and intentionally deferred. Since then the repository has gone through semantic hardening, decomposition, persistence simplification, UI convergence, navigation convergence, and recent-role-rotation persistence/setup wiring.

Therefore old class names may still exist while old ownership and integration assumptions may no longer be exact. EPI-MQ-0 is a **re-baselining stage**. It does not authorize user-visible ranking changes.

## 4. Audit target

Trace the current live path for misinformation candidates and recipient-visible epistemic evaluation, specifically the ownership chain around:

```text
legal semantic candidate
-> candidate visible observation
-> recipient-visible history / knowledge snapshot
-> hypothetical replay / perceived world set
-> BEFORE vs AFTER world effects
-> candidate quality diagnostics
```

Determine the actual current owners and dependency direction. Do not force the old plan's proposed types onto the current architecture.

At minimum answer:

1. Where are legal Drunk/Poisoned information candidates currently generated?
2. Where is truth/reliability separated from strategic recommendation quality?
3. What is the current typed representation of a player-visible ability observation?
4. Can an uncommitted hypothetical observation be evaluated without mutating session/history/recovery state?
5. Which world-set/replay implementation is the correctness oracle for small Trouble Brewing scenarios today?
6. What hidden Storyteller facts are available in production but must be excluded from recipient knowledge?
7. Is there already a narrow typed seam sufficient for EPI-MQ-1, or must one be established first?
8. Did any post-2026-09-01 persistence/setup ownership change invalidate assumptions in the old EPI-MQ plan?

## 5. EPI-MQ-0 behavior corpus

Before scoring or weights, define a small deterministic Trouble Brewing corpus covering at least:

- pair information: plausible false pair vs obviously disconnected/contradictory pair;
- numeric information: same numeric distance but different epistemic consequences;
- Fortune Teller repeated information / temporal consistency;
- Drunk durable mistaken-world behavior;
- Poisoned temporary impairment and later discoverability;
- Spy/Recluse registration as an alternative explanation;
- excessive confirmation-lock risk;
- candidate that would immediately reveal impairment through a public contradiction.

The first corpus should express comparative/invariant expectations, not a giant exact numeric score table.

## 6. Required output of EPI-MQ-0

Produce an audit/spec document that records:

```text
- live main / branch baseline used for the audit
- live owners and dependency graph
- exact candidate/observation/world-evaluation seams
- mutable state and mutation hazards
- hidden-information boundary
- behavior corpus
- focused test owner(s)
- performance/correctness oracle strategy
- GO / MODIFY / NO-GO decision for the old EPI-MQ-1 proposal
```

If the result is GO or MODIFY, define the smallest tests-first EPI-MQ-1 slice. Do not implement broader ranking policy in the same audit step.

## 7. Architecture pre-flight gate

If a later EPI-MQ slice proposes substantial production edits to protected/core handwritten files or handwritten source over ~1000 LOC, the active audit/spec must first record the root `AGENTS.md` architecture pre-flight block:

```text
Architecture pre-flight:
- current owner:
- proposed responsibility:
- authoritative state owner(s):
- narrow typed input/output seam:
- keep in current owner / extract:
- reason:
```

Absence of this record is a stop condition for such production edits.

For large-file edits, follow the repository's `AGENTS.md` large-file SOP rather than unsafe whole-file connector replacement.

## 8. Scope fence

During EPI-MQ-0 do not:

- change production recommendation ranking or weights;
- change Storyteller-visible recommendation output;
- replace the recommendation provider;
- activate A4/ZDD in production;
- reopen D6 solely because a source file is large;
- redesign UI-R5 / UI-NAV surfaces;
- change persistence/recovery ordering unless the audit proves it is required;
- alter ROLE-ROTATION policy;
- leak actual Storyteller-hidden action targets into player knowledge;
- broaden to other scripts before Trouble Brewing behavior is understood.

## 9. Likely following sequence

Subject to EPI-MQ-0 findings:

```text
EPI-MQ-0  baseline / ownership audit + behavior corpus
EPI-MQ-1  hypothetical observation evaluation seam
EPI-MQ-2  credibility + immediate contradiction gates
EPI-MQ-3+ metrics/ranking only after the earlier contracts are stable
```

UX-R6 recommendation-provider replacement remains a later program and is not part of this handoff.
