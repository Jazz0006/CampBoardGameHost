# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## Live context

```text
main:
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
```

All commits after `b2263cd...` and before the merge were documentation-only. The merged production/test/workflow tree is therefore the same tree that received the final FULL acceptance gate.

## Current priority

> **D6.1 COMPLETE / MERGED → D6.2 R0–R2 FIRST WAVE COMPLETE / FULL ACCEPTED / MERGED → NEXT: READ-ONLY R3 VIABILITY AUDIT ON A NEW BRANCH.**

Do not reopen completed D6.2 slices. Do not continue mechanical Host/NightStep/Day extraction merely to reduce file size.

R3 is optional and higher risk. Its first step is **audit only**. Implementation requires a separate GO decision after the audit demonstrates a narrow cohesive transaction-application boundary.

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

This is a waiver, not a claim that real-device testing executed or passed. Real-device coverage remains part of the later UI-R5 / field-stabilization work.

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

## R3 — optional second wave / READ-ONLY AUDIT NEXT

R3 must start on a **new branch from current `main`**. Do not reuse `codex/d6-2-ui-composition`.

The strongest candidate is deep transaction application in `CampBoardGameHostApp.kt`, especially night-confirm / Dawn / succession application sequencing.

The planning side is already typed:

```text
NightCheckpointHostTransaction
  -> checkpoint + revision intent
  -> no durable side effects

NightDawnResolutionPlanner
  -> pure continuation/checkpoint/DawnCommitIntent

NightDawnDurableMaterializationPlanner
  -> pure exactly-once materialization plan
  -> stable IDs support partial-persistence repair
```

### R3 audit question

Determine whether App's remaining application sequence can be expressed as one or more **narrow immutable apply contracts** without introducing:

- `AppTransactionContext` or equivalent broad state bag;
- a callback collection mirroring App;
- a second rules resolver;
- hidden persistence/Recovery ownership;
- altered revision/preflight/chronology ordering;
- altered retry/partial-materialization semantics.

### R3 GO criteria

Proceed only if the audit proves a cohesive boundary with:

1. small typed inputs/outputs;
2. clear single ownership of application ordering;
3. no duplicate `ClocktowerGameSession` authority;
4. no Compose dependency in session/domain;
5. existing behavior characterized by typed tests or existing meaningful coverage;
6. measurable maintainability benefit beyond line-count movement.

If these conditions cannot be met, record R3 **NO-GO** and end the decomposition campaign.

## Frozen invariants

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
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

## Authoritative references

For the next session read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/D6_2Y_FIRST_WAVE_ACCEPTANCE_REMEASUREMENT_2026-09-09.md`;
5. `docs/D6_2Z_FINAL_PR_DIFF_OWNERSHIP_REVIEW_2026-09-09.md`;
6. `docs/D6_2AA_FINAL_ACCEPTANCE_GATE_2026-09-09.md`;
7. `docs/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md` when performing the R3 audit.

Historical D6.2 implementation details remain in `docs/D6_2A...D6_2X...`. Do not re-implement completed slices.

## Later priority after D6

```text
R3 read-only viability audit
-> if GO: bounded R3 implementation on separate branch
-> if NO-GO or after R3 completion: square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
