# D6.2y — First-wave acceptance remeasurement

> Date: 2026-09-09 Australia/Sydney  
> Status: STRUCTURAL PASS / FINAL T4 + REAL-DEVICE GATE PENDING  
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT / SCOPE FROZEN.  
> PR base: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`.  
> R0–R2 measurement baseline: `5e0891e7611787300b01d83b889f27a903c0768b` (D6.2i).  
> Latest production checkpoint: `a692cc722f1e597e747154bf05a2689fee9bed4c`.  
> Final validated code/test head: `b2263cd08bc2ce223598698324bf2b22243c91f2`.  
> Docs head before this acceptance record: `1be59e2c9da153a81726937f8a7ae6b5f4159a31`.

## Decision

The D6.2 first wave (R0–R2) **passes structural acceptance**.

Freeze further Host/NightStep/Day/R1/R2 extraction in PR #115. The campaign has reached the point where additional line-count-driven decomposition would increase abstraction and callback/context coupling more quickly than it reduces ownership ambiguity.

This is **not yet final validation acceptance or merge readiness**. The latest production tree after D6.2m–s has Android FAST + R2 evidence, but the last true FULL/T4 gate predates those production changes. Real-device critical-path coverage is also not claimed.

Do not start R3 implementation on this PR. If the user later chooses a second wave, begin with a separate read-only R3 viability audit on a new branch after the current PR has completed its acceptance/merge lifecycle.

## 1. Measured production change

### R0–R2 first-wave delta

Measured from the D6.2i validated checkpoint `5e0891e...` to the final validated code/test head `b2263cd...`:

| Production file | Additions | Deletions | Net |
|---|---:|---:|---:|
| `CampBoardGameHostApp.kt` | 0 | 96 | -96 |
| `ClocktowerChambermaidPresentationSemantics.kt` | 46 | 14 | +32 |
| `ClocktowerChambermaidStepMaterializer.kt` | 50 | 0 | +50 |
| `ClocktowerNightStepUi.kt` | 0 | 45 | -45 |
| `ClocktowerNumericInformationOptionPreparation.kt` | 55 | 0 | +55 |
| `clocktower/ui/ClocktowerDayScreen.kt` | 0 | 417 | -417 |
| `clocktower/ui/ClocktowerHistoryScreen.kt` | 0 | 195 | -195 |
| `clocktower/ui/ClocktowerHostScreen.kt` | 57 | 373 | -316 |
| **Total production Kotlin** | **208** | **1,140** | **-932** |

The same interval added roughly 297 net lines of focused typed tests, chiefly for the new numeric-option, Chambermaid presentation and Chambermaid materializer seams. Production therefore became smaller while ownership contracts became more explicit.

### Entire PR #115 production delta

Measured from PR base `d76b085...` to final validated code/test head `b2263cd...`:

- production Kotlin additions: **276**;
- production Kotlin deletions: **2,005**;
- production Kotlin net: **-1,729 lines**.

For `ClocktowerHostScreen.kt` specifically:

- additions: **106**;
- deletions: **1,141**;
- net: **-1,035 lines**.

This is an important acceptance signal: the campaign did not merely relocate the original monolith into an equal amount of new production code. Most of the gain came from deleting unreachable/dead ownership and extracting only a small number of cohesive typed seams.

## 2. Principal file-size remeasurement

| File | PR base | Current | Change |
|---|---:|---:|---:|
| `ClocktowerHostScreen.kt` | 329,172 B | 260,686 B | -68,486 B / ~20.8% |
| `ClocktowerDayScreen.kt` | 50,927 B | 31,846 B | -19,081 B / ~37.5% |
| `ClocktowerHistoryScreen.kt` | 38,365 B | 29,188 B | -9,177 B / ~23.9% |
| `CampBoardGameHostApp.kt` | 241,986 B | 233,013 B | -8,973 B / ~3.7% |
| `ClocktowerNightStepUi.kt` | ~47,970 B | ~45,697 B | ~-2,273 B / ~4.7% |

Current documented line counts include:

- Host: 4,439 lines / 260,686 bytes;
- App: 4,142 lines / 233,013 bytes;
- Day: 616 lines / 31,846 bytes;
- History: 530 lines / 29,188 bytes.

The Host and App remain numerically large. That alone is not a failed acceptance criterion. Their remaining size must be classified by responsibility before any second-wave work.

## 3. Ownership-surface remeasurement

From the D6.2a baseline to the post-R0/R1/R2 state:

```text
ClocktowerJudgeScreen
  parameters:        103 -> 87   (-16 / ~15.5%)
  callbacks:          39 -> 34   (-5)
  providers:           3 -> 3    (unchanged)
  MutableState params:10 -> 5    (halved)
```

`ClocktowerNightStepCardLocalized` changed only from 49 to 48 parameters. D6.2u/v correctly rejected treating that number as a decomposition target: the remaining inputs span several already-specialized interaction families and publication/navigation orchestration.

The reproducible App scalar-state metric established in D6.2i is 47 -> 44. Later R0/R1 work did not manufacture a new broad App state holder to make that number look smaller.

## 4. Abstraction-debt audit

The first wave did **not** introduce a compensating mega-context, Controller/ViewModel bag, or second durable state owner.

### New numeric option owner

`ClocktowerNumericInformationOptionPreparation.kt` depends only on narrow recommendation/proposition/display/event inputs. It has no Compose, session, Recovery, persistence or durable-publication authority.

### New Chambermaid materializer owner

`ClocktowerChambermaidStepMaterializer.kt` owns only stable Chambermaid content/step assembly/production identity and receives a lazy option provider. It does not own recommendation invocation, Compose state, session, Recovery, telemetry or publication.

### Deliberate NO-GO decisions prevented new debt

The later read-only audits explicitly rejected:

- a Clockmaker/Chef/Empath generic numeric materializer family;
- a generic numeric interaction owner;
- a generic NightStep interaction/context owner;
- another Day-vote controller/wrapper;
- a generic setup-effect controller/context.

Those rejections are part of the acceptance result, not unfinished work. Each proposed wrapper would have preserved or widened dependency fan-out without gaining a distinct responsibility.

## 5. Remaining large-file classification

### `ClocktowerHostScreen.kt`

Still large, but the residual R1/R2 surface is now primarily high-fan-out Storyteller composition and role-specific preparation over specialized typed owners. D6.2t–x found no further narrow shared owner worth extracting without nullable role bags, broad callback contexts or lifecycle movement.

**Decision:** stop current-wave Host peeling.

### `ClocktowerNightStepUi.kt`

At about 45.7 KiB, it is already below the project's approximate 50 KiB maintainability signal. The remaining complexity is mostly routing among structured number, structured Boolean, Fortune Teller pair+Boolean, pair-information, registration and single-target families, plus durable publication/navigation callbacks.

**Decision:** stop current-wave NightStep peeling.

### Day / History

Both are now comfortably below the size signal after proven dead UI was retired. Live Day vote/transient nomination ownership is already separated through typed state/transaction boundaries.

**Decision:** accepted for this campaign.

### `CampBoardGameHostApp.kt`

At 233 KiB it remains the clearest numerical outlier. Its remaining Clocktower debt is not the same UI-composition problem addressed by R0–R2. It contains high-risk application sequencing across session mutation, revisions, Recovery/persistence, chronology, phase/route transitions and already-typed night/Dawn plans.

That is **R3 transaction-application territory**, not permission to continue D6.2 UI decomposition.

## 6. Validation status

### Latest production/code-test evidence

Final validated code/test head:

`b2263cd08bc2ce223598698324bf2b22243c91f2`

CI `34299329715`:

- Android FAST unit tests: **PASS**;
- full Android unit tests + debug APK: **SKIPPED by routing**;
- ASP: **SKIPPED by routing**;
- Real Clingo: **SKIPPED by routing**;
- CI gate: **PASS**.

R2 `34299329713`: **PASS**.

The current documentation-only head also has successful CI/R2 routing, with Android/ASP/Clingo correctly skipped because no production source changed.

### Last true FULL/T4

The last verified true FULL gate is the D6.2l checkpoint:

`69655de1d992ec6ec7cf45b2639a048ae4fb32e4`

CI `34294224391`:

- full Android JVM tests + debug APK: **PASS**;
- ASP contracts: **PASS**;
- Real Clingo cross-validation: **PASS**;
- CI gate: **PASS**.

R2 `34294224399`: **PASS**.

However D6.2m–s changed production after that checkpoint. Therefore this older FULL is strong historical evidence but **cannot be represented as the final first-wave T4 on the current production tree**.

### Remaining acceptance gates

Before recommending PR #115 as merge-ready, obtain:

1. one fresh FULL/T4 on the `b2263cd...`-equivalent production tree, including full Android JVM tests + debug APK and the normally selected ASP/Real Clingo gates;
2. focused R2 on the same production tree or a docs-only descendant;
3. critical real-device verification, or an explicit documented project/user waiver of that manual gate;
4. final exact PR-diff/ownership review.

No real-device validation is currently claimed.

## 7. R3 viability decision

R3 has enough architectural signal to justify a **separate read-only viability audit**, but not enough evidence to authorize implementation inside PR #115.

The strongest candidate is the night-confirm / Dawn application sequence in `CampBoardGameHostApp.kt` because the domain/planning side is already substantially typed:

- `NightCheckpointHostTransaction` emits checkpoint + revision intent and explicitly does not perform durable side effects;
- `NightDawnResolutionPlanner` is pure and returns continuation / checkpoint / `DawnCommitIntent`;
- `NightDawnDurableMaterializationPlanner` is a pure exactly-once planner over immutable durable projection, including repair of partial persistence via stable IDs.

The likely remaining boundary is therefore **application sequencing of an already-planned transaction**, not another rules resolver.

A future R3 audit must prove a narrow contract around:

- immutable input snapshot;
- immutable application plan/result;
- exact mutation / chronology / persistence / phase order;
- preflight and revision expectations;
- retry / partial-materialization behavior;
- unchanged `ClocktowerGameSession` and Recovery authority;
- no `AppTransactionContext`, broad action bag or callback collection.

If that cannot be expressed as a narrow stable apply contract, R3 should record NO-GO rather than moving the App callback wholesale.

## 8. First-wave exit decision

**Structural acceptance: PASS.**  
**Further R0–R2 decomposition in PR #115: STOP / SCOPE FROZEN.**  
**Final T4: PENDING.**  
**Real-device critical-path gate: PENDING / no claim.**  
**Merge authorization: NOT GRANTED.**

After the final validation gates are satisfied and the user explicitly authorizes the current PR lifecycle, any R3 second wave should start from the accepted mainline on a new branch, beginning with a read-only transaction-application audit rather than code changes.

## Validation classification for this document

D6.2y itself is documentation/read-only acceptance work. It does not modify production source, tests, workflows, persistence, recommendation semantics, A4 behavior, Recovery, transaction order or runtime behavior. No new Android run is claimed for this docs-only commit.
