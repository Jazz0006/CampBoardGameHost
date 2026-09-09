# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## Live context

```text
main:
d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e

D6.2 branch:
codex/d6-2-ui-composition

Draft PR:
#115 — OPEN / DRAFT / DO NOT AUTO-MERGE

Latest production checkpoint:
a692cc722f1e597e747154bf05a2689fee9bed4c

Final validated production-equivalent code/test head:
b2263cd08bc2ce223598698324bf2b22243c91f2

First-wave acceptance remeasurement:
4fa857105304d85500e7932241f3fd6758a7a892

Final PR diff / ownership review:
7c759cbfe65f17f9b8d4e6593a05c328e17f397d
```

All commits after `b2263cd...` are documentation-only unless a later handoff explicitly records a newer production checkpoint.

PR #115 remains OPEN / DRAFT. No merge or ready-for-review authorization has been given.

## Current priority

> **D6.1 COMPLETE / MERGED → D6.2 R0–R2 FIRST WAVE STRUCTURAL PASS → FINAL PR DIFF/OWNERSHIP REVIEW PASS → CURRENT PR SCOPE FROZEN → FRESH FULL/T4 + REAL-DEVICE CRITICAL-PATH VALIDATION REMAIN.**

Do **not** continue speculative Host/NightStep/Day decomposition inside PR #115. Do **not** start R3 implementation on this branch.

Current acceptance authorities:

- `docs/D6_2Y_FIRST_WAVE_ACCEPTANCE_REMEASUREMENT_2026-09-09.md`
- `docs/D6_2Z_FINAL_PR_DIFF_OWNERSHIP_REVIEW_2026-09-09.md`

Residual/second-wave architecture authority:

- `docs/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md`

## Architecture rules

D6 preserves these rules:

- optimize only live ownership seams;
- distinguish transient UI selection from durable/Recovery authority;
- move ownership only when a real cohesive boundary exists;
- delete dead state rather than wrapping it;
- do not create broad `ClocktowerJudgeState`, `DayState`, `NightStepArgs`, `SetupEffectContext`, Controller/ViewModel, callback or transaction-context bags;
- preserve `ClocktowerGameSession` as canonical writable session/domain owner;
- preserve exact transaction/revision/persistence ordering;
- treat file size as a maintainability signal, not an architecture goal;
- stop when another extraction would increase coupling more than maintainability.

## D6.1 — COMPLETE / MERGED

D6.1 established:

```text
ClocktowerGameSession
= canonical writable identity
+ revisions
+ semantic chronology
+ dynamic GameState mechanics

ClocktowerSessionView
= narrow immutable Compose-facing projection

App-root PlayerCard / flow variables
= presentation and orchestration mirrors
```

Merged PR #113:

```text
112572cbd3d990737a412cc4b8ead766d00867e8
```

## D6.2 baseline

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
MutableState params       10
```

The dominant residual debt was UI-composition fan-out rather than canonical domain ownership.

## D6.2 first wave — COMPLETE structurally

### R0 — cleanup / dead ownership

D6.2a–m completed:

- characterized Judge ownership rather than replacing 103 parameters with a mega-state;
- localized Slayer, Artist and nomination transient state while preserving durable commit boundaries;
- proved and deleted the unreachable legacy HostScriptCard/HostProgressCard Storyteller tail;
- removed dead phase/result/record plumbing and dormant diagnostics;
- retired unused Day/History UI plus only the obsolete R2 source-shape assertions;
- removed the isolated private App decoder island while preserving live archive/Recovery codecs and setup paths.

D6.2i validated full checkpoint:

```text
5e0891e7611787300b01d83b889f27a903c0768b
CI 34291666237 — FULL PASS
R2 34291666241 — PASS
```

D6.2l remains the last true FULL/T4 checkpoint:

```text
69655de1d992ec6ec7cf45b2639a048ae4fb32e4
CI 34294224391 — PASS
  Android full JVM + debug APK — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
R2 34294224399 — PASS
```

D6.2m–s changed production after this FULL, so it cannot serve as the final acceptance T4.

### R1 — information preparation / materializer

D6.2n–s completed:

- audited six numeric recommendation call sites;
- extracted only the honest common previous-number/recommendation-to-display seam;
- rejected a broad shared Clockmaker/Chef/Empath/Chambermaid input object;
- prepared Chambermaid immutable seat/proposition presentation once;
- extracted one narrow Chambermaid materializer reused in both night phases.

Final production checkpoint:

```text
a692cc722f1e597e747154bf05a2689fee9bed4c
```

Final production-equivalent code/test head:

```text
b2263cd08bc2ce223598698324bf2b22243c91f2
CI 34299329715 — PASS
  Android FAST — PASS
  FULL — skipped by routing
  ASP — skipped by routing
  Real Clingo — skipped by routing
  CI gate — PASS
R2 34299329713 — PASS
```

### R1/R2 residual audits — deliberate NO-GO

D6.2t–x closed the remaining speculative first-wave candidates:

- no generic Clockmaker/Chef/Empath numeric materializer family;
- no generic numeric interaction owner;
- no generic all-family NightStep owner/context;
- no additional Day-vote controller/wrapper;
- no generic setup-effect owner/context.

These NO-GO decisions are accepted outcomes. The existing typed owners already separate the semantic responsibilities; another wrapper would mostly move parameter/callback fan-out.

## D6.2y — first-wave acceptance remeasurement

Status:

```text
STRUCTURAL PASS
CURRENT PR SCOPE FROZEN
```

Measured from D6.2i `5e0891e...` to final code/test head `b2263cd...`:

```text
Production Kotlin:
  +208 / -1,140
  net -932 lines

Focused typed tests:
  approximately +297 net lines
```

Measured across the whole PR from base `d76b085...` to `b2263cd...`:

```text
Production Kotlin:
  +276 / -2,005
  net -1,729 lines

ClocktowerHostScreen.kt:
  +106 / -1,141
  net -1,035 lines
```

Principal size change:

```text
ClocktowerHostScreen.kt  329,172 -> 260,686 bytes   (~-20.8%)
ClocktowerDayScreen.kt    50,927 -> 31,846 bytes    (~-37.5%)
ClocktowerHistoryScreen   38,365 -> 29,188 bytes    (~-23.9%)
CampBoardGameHostApp.kt  241,986 -> 233,013 bytes   (~-3.7%)
ClocktowerNightStepUi.kt ~47,970 -> ~45,697 bytes   (~-4.7%)
```

Ownership surface:

```text
ClocktowerJudgeScreen
  parameters:          103 -> 87
  callbacks:            39 -> 34
  providers:             3 -> 3
  MutableState params:  10 -> 5

NightStep parameters:
  49 -> 48
  deliberately not chased with a parameter bag

Reproducible App scalar-state metric:
  47 -> 44
```

No broad replacement state/action/context bag was introduced.

## D6.2z — final PR diff / ownership review

Status:

```text
PASS
```

Reviewed the complete PR changed-file set:

- one R2 workflow file;
- nine production Kotlin files;
- four focused test Kotlin files;
- D6 audit/progress/handoff documentation.

Key conclusions:

- workflow change only retires obsolete source-existence assertions for proven-dead Day/History UI;
- `ClocktowerAppModels.kt` only removes writerless `ExecutionResult`;
- Day/History changes are deletion-only dead UI cleanup;
- NightStep change removes dormant diagnostic plumbing only;
- numeric option preparation is a pure narrow helper with no session/Recovery/Compose authority;
- Chambermaid presentation/materializer remains narrow and does not absorb recommendation/history/telemetry/publication lifecycle;
- App removes transient/dead ownership but retains durable application sequencing;
- Host removes unreachable/dead composition, localizes only transient state, and preserves durable callbacks and recommendation/Recovery authority;
- no production/test/workflow drift occurred after `b2263cd...`; subsequent commits are docs-only.

Authority:

- `docs/D6_2Z_FINAL_PR_DIFF_OWNERSHIP_REVIEW_2026-09-09.md`

## Remaining acceptance gates for PR #115

Already satisfied:

```text
R0–R2 structural acceptance             PASS
Final PR diff / ownership review        PASS
Final production-equivalent FAST        PASS
Final production-equivalent R2          PASS
No broad replacement context/state bag  PASS
No post-validation source drift         PASS
```

Still required before recommending merge-ready:

```text
Fresh FULL/T4 on b2263cd-equivalent production tree   PENDING
Critical real-device Storyteller path                  PENDING / no claim
```

The GitHub capability available in the current Chat session can inspect and rerun existing workflow runs but does not expose a fresh workflow-dispatch action for this ref. Re-running the D6.2l FULL would test the old commit and does not satisfy the gate.

Do **not** create a fake source/workflow change just to force FULL.

If the project/user explicitly waives the real-device gate, record the waiver. Otherwise do not claim it.

PR #115 is therefore **structurally accepted but not yet merge-ready**.

## R3 — optional second wave, NOT STARTED

R3 is not part of finishing PR #115.

Only after the first-wave PR is accepted and explicitly merged should a new branch begin with a read-only R3 viability audit.

The strongest candidate is the night-confirm / Dawn application sequence in `CampBoardGameHostApp.kt`, because the planning/domain side is already typed:

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

A future R3 owner is justified only if it can express a narrow immutable application contract preserving:

- exact mutation/order semantics;
- revision/preflight expectations;
- retry/partial-materialization behavior;
- canonical `ClocktowerGameSession` ownership;
- Recovery/persistence authority;
- chronology/action/observation idempotency.

Do not create `AppTransactionContext`, a broad callback collection or another rules resolver merely to shorten App. If a narrow apply contract cannot be demonstrated, R3 should record NO-GO.

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

## Active handoff

For the current state read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/D6_2Y_FIRST_WAVE_ACCEPTANCE_REMEASUREMENT_2026-09-09.md`;
5. `docs/D6_2Z_FINAL_PR_DIFF_OWNERSHIP_REVIEW_2026-09-09.md`;
6. `docs/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md` only when considering a second wave.

Historical implementation detail remains in the corresponding `docs/D6_2A...D6_2X...` audit/progress documents. Do not reopen completed slices.

## Later priority after D6

```text
D6 first-wave final validation / merge decision
-> square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
