# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## Live context

```text
main:
f64245573db246cfb3900d8f0a698e94158b9b1c
docs: close D6.2 merge and set R3 audit next

D6.2 merge commit:
c75e0f0bc4635ef42ffbece41470c3437a205910
Merge pull request #115 from Jazz0006/codex/d6-2-ui-composition

D6.2 PR:
#115 — MERGED

Final D6.2 production checkpoint:
a692cc722f1e597e747154bf05a2689fee9bed4c

Final production-equivalent code/test head:
b2263cd08bc2ce223598698324bf2b22243c91f2

Final acceptance trigger head:
9ec4ce2f9e0d4114f84ee7bde90ff7e409caac8e

R3 audit branch:
codex/r3-transaction-application-viability-audit

R3 audit result commit:
a2d09c8a6bde652a4fbd3fd4f62dfe5f35019a59
```

`f642455...` is documentation-only relative to the D6.2 merge. The production/test/workflow tree remains the same production-equivalent tree that received the final FULL acceptance gate.

## Current priority

> **D6.1 COMPLETE / MERGED → D6.2 R0–R2 COMPLETE / FULL ACCEPTED / MERGED → R3 READ-ONLY VIABILITY AUDIT COMPLETE / NO-GO → D6 DECOMPOSITION CAMPAIGN COMPLETE.**

Do not reopen completed D6.2 slices. Do not implement an R3 transaction controller/applier merely to reduce `CampBoardGameHostApp.kt` size.

The next engineering/product priority is:

```text
square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

## Final D6.2 acceptance

### Structural / ownership result

D6.2 completed the first-wave UI-composition decomposition without introducing a replacement mega-state or callback/context bag.

Measured from the post-D6.1 baseline:

```text
ClocktowerHostScreen.kt
  329,172 -> 260,686 bytes   (~-20.8%)

ClocktowerDayScreen.kt
  50,927 -> 31,846 bytes     (~-37.5%)

ClocktowerHistoryScreen.kt
  38,365 -> 29,188 bytes     (~-23.9%)

CampBoardGameHostApp.kt
  241,986 -> 233,013 bytes   (~-3.7%)

ClocktowerNightStepUi.kt
  ~47,970 -> ~45,697 bytes   (~-4.7%)
```

Whole-PR production Kotlin delta from base `d76b085...` to final code/test head `b2263cd...`:

```text
+276 / -2,005
net -1,729 lines
```

`ClocktowerHostScreen.kt` alone:

```text
+106 / -1,141
net -1,035 lines
```

Judge ownership surface:

```text
ClocktowerJudgeScreen
  parameters:          103 -> 87
  callbacks:            39 -> 34
  providers:             3 -> 3
  MutableState params:  10 -> 5

NightStep parameters:
  49 -> 48
  intentionally not chased with a parameter bag

Reproducible App scalar-state metric:
  47 -> 44
```

### Final FULL/T4

Final acceptance checkpoint:

```text
9ec4ce2f9e0d4114f84ee7bde90ff7e409caac8e
[full-ci] docs(d6.2): request final acceptance gate
```

Validation:

```text
CI 34307304901 — PASS
  Android full JVM tests + debug APK — PASS
  ASP contract tests — PASS
  Real Clingo cross-validation — PASS
  CI gate — PASS

R2 34307304900 — PASS
```

The `[full-ci]` commit was documentation-only and used the repository's existing supported FULL-checkpoint mechanism. It did not alter production source, tests or workflow routing.

### Real-device status

Real-device critical-path testing was **explicitly waived by the user for PR #115 merge**.

This is a waiver, not a claim that real-device testing executed or passed. Real-device coverage remains part of UI-R5 / field stabilization.

### Merge

PR #115 was marked ready after the final gate and merged with the reviewed exact head.

```text
merge commit:
c75e0f0bc4635ef42ffbece41470c3437a205910
```

## D6.2 completed ownership decisions

### R0 — dead / wrong ownership cleanup

Completed:

- characterized Judge inputs instead of introducing a mega-state;
- localized Slayer, Artist and nomination transient state while preserving durable callbacks;
- proved and deleted the unreachable legacy HostScriptCard/HostProgressCard Storyteller tail;
- removed dead phase/result/record plumbing and dormant diagnostics;
- retired unused Day/History UI plus obsolete R2 source-shape assertions;
- removed the isolated private App decoder island while preserving live archive/Recovery codecs.

### R1 — information preparation / role materializer

Completed:

- extracted the honest shared previous-number/recommendation-to-display seam;
- prepared Chambermaid immutable seat/proposition presentation once;
- extracted one narrow Chambermaid materializer reused in both night phases;
- kept recommendation invocation, history, style/pressure, telemetry and publication in their existing owners.

### R1/R2 residual NO-GO decisions

The following were audited and deliberately **not** extracted:

- generic Clockmaker/Chef/Empath numeric materializer family;
- generic numeric NightStep interaction owner;
- generic all-family NightStep context/owner;
- extra Day-vote wrapper/controller;
- generic setup-effect owner/context.

These are accepted outcomes, not unfinished D6.2 work. The audits found that existing specialized typed owners already carry the meaningful responsibility and another wrapper would mostly relocate fan-out.

## R3 — read-only deep transaction application audit — COMPLETE / NO-GO

Authoritative audit:

- `docs/R3_TRANSACTION_APPLICATION_VIABILITY_AUDIT_2026-09-09.md`

The strongest candidate was deep transaction application in `CampBoardGameHostApp.kt`, especially night-confirm / Dawn / succession application sequencing.

The planning and durable authority were already typed before R3:

```text
NightCheckpointHostTransaction
  -> checkpoint + revision intent
  -> no durable side effects

NightDawnResolutionPlanner
  -> pure continuation/checkpoint/DawnCommitIntent

NightDawnDurableMaterializationPlanner
  -> pure exactly-once materialization plan
  -> stable IDs support partial-persistence repair

ClocktowerGameSession
  -> canonical writable GameState/revision/history authority
  -> atomic action/observation commit primitives
```

### R3 finding

The remaining App code is not one missing cohesive transaction owner. It is cross-boundary choreography among existing owners plus:

- non-mutating public-observation/A4 preflight;
- session mechanical mutation and publication;
- App `PlayerCard` presentation projection;
- localized records/events;
- succession/Klutz/Ravenkeeper/outcome continuation;
- phase-specific revision cadence;
- Day/Dusk-specific lifecycle and debug evidence.

A new executor that truly owned the full order would require a broad context/callback surface equivalent to App. A narrow executor would leave the real ordering in App and merely re-express instructions already produced by `NightDawnDurableMaterializationPlanner`.

The audit therefore rejects:

- further night-checkpoint controller extraction;
- a generic Dawn death executor;
- a generic Dawn effect executor;
- a shared Dawn/Day transaction owner;
- a separate Day execution applier;
- expanding `ClocktowerGameSession` to absorb presentation/UI application;
- a low-value materialization-state factory as justification for a second-wave R3 implementation.

### R3 test evidence

`NightTransactionHostIntegrationSmokeTest` explicitly proves the real typed Host/session seams while stopping before App-owned durable side effects so that no second coordinator/state owner is introduced.

`NightDawnRestoreRetryConvergenceAcceptanceTest` consumes a `DawnDurableMaterializationPlan` in a test-local materializer and proves partial-persistence convergence and exactly-once replay. This validates the **plan** as the stable abstraction; it does not establish a missing production executor contract.

### R3 GO-gate result

```text
small typed inputs/outputs                         FAIL for a complete executor
clear new single application-order owner           FAIL
preserve one ClocktowerGameSession authority       PASS by keeping current boundary
no Compose dependency in session/domain            PASS by keeping current boundary
typed behavioral evidence                          PASS for existing planner/session seam
maintainability gain beyond line movement          FAIL
```

**Final R3 decision: NO-GO.**

Per the original roadmap rule, this ends the D6 decomposition campaign.

## Frozen architecture after D6/R3

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- pure planner/reducer ownership of transaction semantics and exactly-once intent planning;
- App ownership of the remaining cross-boundary application choreography and Compose-facing projection;
- exact game/player revision cadence and ordering;
- one monotonic collision-free semantic chronology;
- action/observation idempotency and non-mutating preflight;
- no storyteller-hidden target leak;
- Recovery v2 current-version-only policy;
- SideEffect / ON_PAUSE / ON_STOP + `RecoveryWriteGate` topology;
- A4 durability/invalidation/prewarm ordering;
- no Compose dependency in session/domain;
- gameplay/recommendation semantics unless separately authorized;
- Undercover/Werewolf isolation.

A large composition root is acceptable when the remaining code is the wiring between narrower authoritative owners. File size alone is not a reason to create another controller/context layer.

## R3 revisit trigger

Do not reopen R3 merely because `CampBoardGameHostApp.kt` remains large.

Re-audit only if a future product change naturally creates a new seam, such as:

- a real typed atomic session transaction API required by multiple non-UI callers;
- a dedicated presentation projector introduced for independent product reasons;
- a second production host surface that genuinely reuses the same Dawn commit protocol;
- persistence semantics changing so that an explicit durable transaction object becomes necessary.

## Authoritative references

For the next session read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/R3_TRANSACTION_APPLICATION_VIABILITY_AUDIT_2026-09-09.md`;
5. `docs/D6_2Y_FIRST_WAVE_ACCEPTANCE_REMEASUREMENT_2026-09-09.md`;
6. `docs/D6_2Z_FINAL_PR_DIFF_OWNERSHIP_REVIEW_2026-09-09.md`;
7. `docs/D6_2AA_FINAL_ACCEPTANCE_GATE_2026-09-09.md`;
8. `docs/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md` for historical D6 residual-candidate context.

Historical D6.2 implementation details remain in `docs/D6_2A...D6_2X...`. Do not re-implement completed slices.

## Next priority after D6

```text
square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
