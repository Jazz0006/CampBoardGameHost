# NEXT DEVELOPMENT HANDOFF — EPI-MQ-0 Baseline Re-audit

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — READ-ONLY BASELINE / OWNERSHIP RE-AUDIT FIRST**  
> Base: live `main` after UI-R5 merge and docs closeout  
> Program: Epistemic Misinformation Quality / Productive Uncertainty

## 1. Stable inherited baseline

```text
UI-R5 merged production baseline: b47b00fd0c727e048dcb1b57260b8dd6fff466a1
UI-R5 merge: PR #117
final logical UI-R5 T4: 2958f334fc7cccd59ed2e75a3bdaa60684492292
CI #2143 / run 34435295215 PASS
R2 #2010 / run 34435295217 PASS
post-merge main CI #2146 / run 34437247431 PASS
Field Test APK #34 / run 34437247434 PASS
```

**Always re-query live `main` before creating a branch or beginning the audit.** Docs-only closeout commits intentionally advance `main` beyond the production baseline above.

## 2. Read first

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/TESTING_STRATEGY.md`;
5. `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`;
6. `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`;
7. `docs/epistemic_reference_matrix.md`;
8. `docs/asp_oracle_cross_validation.md` only where exact-world oracle evidence is needed.

UI-R5 / D6 archives are historical evidence only and should not be loaded by default.

## 3. Why re-audit before implementation

The EPI-MQ plan was written on 2026-09-01 and intentionally deferred. Since then the repository has gone through semantic hardening, decomposition, persistence simplification and UI-R5 convergence. Therefore old class names may still exist, but old ownership and integration assumptions must not be accepted without checking live code.

EPI-MQ-0 is a **re-baselining stage**. It does not authorize user-visible ranking changes.

## 4. Audit target

Trace the current live path for misinformation candidates and recipient-visible epistemic evaluation, specifically the ownership chain around concepts such as:

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

## 8. Scope fence

During EPI-MQ-0 do not:

- change production recommendation ranking or weights;
- change Storyteller-visible recommendation output;
- replace the recommendation provider;
- activate A4/ZDD in production;
- reopen D6 solely because a source file is large;
- redesign UI-R5 surfaces;
- change persistence/recovery ordering;
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
