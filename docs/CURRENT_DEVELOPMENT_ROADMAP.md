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
refactor(clocktower): extract Chambermaid step materializer

Final validated code/test head:
b2263cd08bc2ce223598698324bf2b22243c91f2

after D6.2y acceptance remeasurement:
4fa857105304d85500e7932241f3fd6758a7a892
```

All commits after `b2263cd...` are documentation-only unless a later handoff explicitly records a newer production checkpoint.

PR #115 remains OPEN / DRAFT. No merge or ready-for-review authorization has been given.

## Current priority

> **D6.1 COMPLETE / MERGED → D6.2 R0–R2 FIRST WAVE STRUCTURAL ACCEPTANCE PASS → CURRENT PR SCOPE FROZEN → FINAL T4/FULL + R2 + REAL-DEVICE CRITICAL-PATH VALIDATION NEXT.**

Do **not** continue speculative Host/NightStep/Day decomposition inside PR #115. Do **not** start R3 implementation on this branch.

The detailed acceptance authority is:

- `docs/D6_2Y_FIRST_WAVE_ACCEPTANCE_REMEASUREMENT_2026-09-09.md`

The residual architecture route remains:

- `docs/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md`

## Architecture direction

The surviving Storyteller UI is square-table/table based. The old HostScriptCard-style fallback was proven unreachable and deleted.

D6 follows these rules:

- optimize only live ownership seams;
- distinguish transient UI selection from durable/Recovery authority;
- move ownership only when a real cohesive boundary exists;
- delete dead state instead of wrapping it;
- do not create broad `ClocktowerJudgeState`, `DayState`, `NightStepArgs`, `SetupEffectContext`, Controller/ViewModel or callback bags;
- preserve `ClocktowerGameSession` as canonical writable session/domain owner;
- preserve exact transaction/revision/persistence ordering;
- treat file size as a maintainability signal, not an architecture goal;
- stop decomposition when another extraction would increase coupling more than maintainability.

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

Post-D6.1 baseline:

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
MutableState params       10
```

The dominant residual debt was UI-composition fan-out rather than canonical domain ownership.

## D6.2 first wave — COMPLETE structurally

### D6.2a–g — characterize ownership, remove unreachable/dead Storyteller composition

Completed work includes:

- characterized all 103 Judge inputs rather than replacing them with a mega-state;
- localized Slayer and Artist transient ownership while preserving durable callbacks;
- proved the legacy HostScriptCard/HostProgressCard Storyteller tail unreachable;
- deleted that legacy tail instead of decomposing it;
- removed dead phase/result/record plumbing exposed by the deletion.

Major checkpoint after legacy retirement:

```text
ClocktowerHostScreen.kt
  330,257 -> 283,849 bytes
  5,491 -> 4,807 lines
```

### D6.2h–i — surviving square-table Day ownership

Completed:

- proved pending vote count already belongs to typed `ClocktowerTableVoteState`;
- moved transient nomination pair to Judge-local `remember(gameId, round)`;
- preserved external `dayModeState`, ghost-vote authority, highest-vote state, Virgin/Klutz routing and Recovery.

D6.2i validated checkpoint:

```text
5e0891e7611787300b01d83b889f27a903c0768b
CI 34291666237 — FULL PASS
R2 34291666241 — PASS
```

### D6.2j–m — R0 cleanup COMPLETE

Completed:

- removed dormant diagnostics and their untriggerable Compose effects;
- removed dead Judge/child inputs and unused pure role lookups;
- retired unused Day/History UI plus only the obsolete R2 assertions that enforced it;
- removed the isolated private App JSON decoder island while preserving live archive/Recovery codecs and setup assignment paths.

D6.2l is the **last true FULL/T4 checkpoint**:

```text
69655de1d992ec6ec7cf45b2639a048ae4fb32e4
CI 34294224391 — PASS
  Android full JVM + debug APK — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
R2 34294224399 — PASS
```

D6.2m changed production after this full gate.

### D6.2n–s — R1 information/materializer boundary COMPLETE

Completed:

- audited six numeric information preparation call sites;
- extracted the honest common prior-number/recommendation-to-display seam;
- rejected a broad shared Clockmaker/Chef/Empath/Chambermaid step-input object;
- prepared Chambermaid immutable seat/proposition presentation once;
- extracted the proven shared Chambermaid materializer used in both night phases.

Final production checkpoint:

```text
a692cc722f1e597e747154bf05a2689fee9bed4c
```

Final code/test correction head:

```text
b2263cd08bc2ce223598698324bf2b22243c91f2
```

Validation at that head:

```text
CI 34299329715 — PASS
  Android FAST — PASS
  FULL — skipped by routing
  ASP — skipped by routing
  Real Clingo — skipped by routing
  CI gate — PASS
R2 34299329713 — PASS
```

Current Host:

```text
4,439 lines / 260,686 bytes
```

### D6.2t–x — R1/R2 residual audits COMPLETE / deliberate NO-GO

The post-extraction audits deliberately rejected several tempting but low-quality abstractions.

D6.2t:

- no new Clockmaker/Chef/Empath numeric materializer family;
- `ClocktowerInformationStepBuilder` is already the honest common shell;
- Chef/Empath registration/history differences remain typed rather than nullable bag fields.

D6.2u/v:

- no generic numeric interaction owner;
- no generic all-family NightStep owner/context;
- structured number, structured Boolean, Fortune Teller pair+Boolean, pair-information, registration and single-target interactions already have specialized typed owners;
- `ClocktowerNightStepUi.kt` is about 45.7 KiB and its residual responsibility is mainly cross-family orchestration.

D6.2w:

- no extra Day-vote wrapper/controller;
- `ClocktowerVoteTableScreen` owns pending taps;
- `ClocktowerTableVoteState` owns pending-vote invariants;
- `commitClocktowerVoteTransaction` owns the atomic semantic vote calculation;
- Host/Judge legitimately applies the result to external durable owners.

D6.2x:

- no generic setup-effect owner/context;
- recommendation loading, automatic apply, first-night precompute, poison-information invalidation and A4 prewarming are different lifetimes;
- existing typed coordinators already own their semantic/cache responsibilities while Compose retains keyed cancellation/trigger lifetime.

Authorities:

- `docs/D6_2T_NUMERIC_MATERIALIZER_FAMILY_AUDIT_2026-09-09.md`
- `docs/D6_2U_NIGHTSTEP_NUMERIC_INTERACTION_OWNERSHIP_AUDIT_2026-09-09.md`
- `docs/D6_2V_NIGHTSTEP_BOOLEAN_TARGET_RESIDUAL_AUDIT_2026-09-09.md`
- `docs/D6_2W_DAY_VOTE_ORCHESTRATION_RESIDUAL_AUDIT_2026-09-09.md`
- `docs/D6_2X_SETUP_EFFECT_OWNER_NECESSITY_AUDIT_2026-09-09.md`

## D6.2y — FIRST-WAVE ACCEPTANCE REMEASUREMENT

Status:

```text
STRUCTURAL PASS
FINAL T4 PENDING
REAL-DEVICE CRITICAL PATH PENDING
CURRENT PR SCOPE FROZEN
```

### Actual production delta

From D6.2i `5e0891e...` through final validated code/test head `b2263cd...`:

```text
Production Kotlin:
  +208 / -1,140
  net -932 lines

Focused typed tests:
  approximately +297 net lines
```

Across the entire PR from base `d76b085...` to `b2263cd...`:

```text
Production Kotlin:
  +276 / -2,005
  net -1,729 lines

ClocktowerHostScreen.kt:
  +106 / -1,141
  net -1,035 lines
```

This confirms the campaign mostly deleted dead/wrong ownership and added a small amount of typed structure rather than merely relocating the monolith.

### Principal file-size remeasurement

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

### Ownership-surface result

```text
ClocktowerJudgeScreen
  parameters:         103 -> 87
  callbacks:           39 -> 34
  providers:            3 -> 3
  MutableState params: 10 -> 5

NightStep parameters:
  49 -> 48
  deliberately not chased further with a parameter bag

Reproducible App scalar-state metric:
  47 -> 44
```

No new broad state/action/context bag was introduced.

## Current validation gate

Before PR #115 can be recommended as merge-ready, still require:

1. a fresh FULL/T4 against the `b2263cd...`-equivalent production tree;
2. R2 on that same production tree or a documentation-only descendant;
3. critical real-device verification, or an explicit documented user/project waiver;
4. final exact PR-diff/ownership review.

The D6.2y docs-only checkpoint itself passed normal docs routing:

```text
4fa857105304d85500e7932241f3fd6758a7a892
CI 34306132881 — PASS
R2 34306132874 — PASS
```

No Android/ASP/Clingo execution is claimed for that documentation-only commit.

No real-device validation is currently claimed.

## R3 — SECOND WAVE, NOT STARTED

R3 is optional/high-risk work and is **not part of finishing PR #115**.

Only after the first-wave PR is accepted and explicitly merged should a new branch begin with a **read-only R3 viability audit**.

The strongest current candidate is night-confirm / Dawn application sequencing in `CampBoardGameHostApp.kt`, because the planning/domain side is already typed:

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

Do not create `AppTransactionContext`, a broad callback collection, or another rules resolver merely to shorten App.

If a narrow apply contract cannot be demonstrated, R3 should record NO-GO.

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
5. `docs/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md` when considering any second wave.

For historical implementation details, use the corresponding `docs/D6_2A...D6_2X...` audit/progress documents rather than reopening completed slices.

## Later priority after D6

```text
D6 first-wave final validation / merge decision
-> square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
