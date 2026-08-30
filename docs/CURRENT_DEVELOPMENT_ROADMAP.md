# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
main baseline:
ba7cfa12853a8829ecf228c05cf2a22067f1e6e4

main meaning:
PR #55 merged — Dawn poison exactly-once materialization
PR #56 merged — next-night / Dusk poison expiry exactly-once materialization

current branch:
codex/trouble-brewing-setup-presets-v2

current Draft PR / CI carrier:
PR #57 — TBSP: integrate Trouble Brewing setup presets
OPEN / DRAFT / NOT MERGED

last fully validated TBSP code checkpoint:
5c10cd29111449e1f8af2b8944609a2002048679

current RED code checkpoint:
a26c221670fdea2612626f762d162b66091896af

current code state:
TBSP-6G-A setup recommendation prewarm coordinator RED

active handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md

normative TBSP rotation policy:
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md

normative Trouble Brewing production cutover contract:
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

Any later documentation-only commits on top of `a26c221...` are **docs-only carriers on top of a RED code state**. Do not treat a docs-only CI result as code GREEN evidence.

## 2. Current campaign status

```text
TBSP-0 documentation / campaign plan                         COMPLETE
TBSP-1 final dataset asset + parser + semantic validator     COMPLETE
TBSP-2 deterministic history-aware preset selector           COMPLETE
TBSP-3 deterministic exact deal materialization              COMPLETE
TBSP-4 recommendation lock / selector-owned Drunk identity   COMPLETE
TBSP-5 durable cross-game rotation-history store             COMPLETE
TBSP-6A active setup provenance codec                        COMPLETE
TBSP-6B production setup preparer                            COMPLETE
TBSP-6C production deal-role resolver                        COMPLETE
TBSP-6D Trouble Brewing production start cutover             COMPLETE
TBSP-6E active-game provenance persist/restore               COMPLETE
TBSP-6F true-completion rotation-history wiring              COMPLETE
TBSP-6G-A setup recommendation prewarm core                  CURRENT RED
TBSP-6G-B reveal-window production wiring                    NOT STARTED
TBSP-6H First Night background precompute                    NOT STARTED
TBSP-6I cutover acceptance matrix                            NOT STARTED
TBSP-6J cleanup                                              NOT STARTED
TBSP-6K final full acceptance                                NOT STARTED
A3 immutable setup snapshot                                  DEFERRED UNTIL TBSP ACCEPTANCE
```

The project is no longer in “TBSP-6 NOT STARTED” state.

## 3. Accepted predecessor correctness baseline

PR #54 same-night/GCR correctness, PR #55 Dawn poison exactly-once and PR #56 Dusk poison-expiry exactly-once are accepted predecessor behavior. TBSP must preserve them.

Protected examples include:

- First Night Fortune Teller uses base/current-role authority rather than Other Night chronology projection.
- Other Night Fortune Teller uses canonical same-night effective-state projection.
- current living-Demon UI authority remains distinct from pending-succession night reconstruction.
- poisoned Spy follows the accepted fail-safe policy: wake normally, no fabricated Grimoire and no false Grimoire observation persistence.
- Dawn poison materialization is exactly-once and retry-convergent.
- next-night/Dusk poison expiry is exactly-once, restore/retry convergent and durably ordered before Night phase/round advancement.

Do not reopen these semantics merely because TBSP touches initial setup or First Night lifecycle timing.

## 4. TBSP production architecture

Goal:

Replace broad random Trouble Brewing setup authority with the final curated preset pipeline while preserving legality, determinism, immediate dealing, restore/no-reroll semantics and existing No Greater Joy behavior.

Frozen dataset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
schema_version: 2
dataset_id: trouble_brewing_setup_presets_v2_final
status: final_ready_for_program_integration
player counts: 5..15
preset count: 480
Drunk presets: 208
Drunk options per Drunk preset: exactly 3
```

Do not regenerate or reformat this dataset.

Required authority chain:

```text
frozen curated preset dataset
-> history-aware selector
-> selected preset + selector-owned Drunk shown role
-> deterministic tb-seat-v1 deal materialization
-> commit actual/shown identities
-> immediate PassPhone / RevealCard
-> identity-reveal window performs background setup/first-night computation
-> relevant First Night consumer uses ready result or safely waits/falls back at point of use
```

The background calculation must run off the UI/main thread and must never reroll or mutate already committed actual/shown identities.

No Greater Joy remains on its existing setup-generation path during TBSP.

## 5. Accepted lower-layer ownership — TBSP-1 through TBSP-5

### TBSP-1

Accepted typed parser/validator proves dataset/schema/pool identity, role legality, exact team composition including Baron already represented once, and Drunk-option validity.

### TBSP-2

Accepted pure selector proves:

```text
player-count isolation
deterministic preset + Drunk-option replay
exact-repeat role-composition rejection
player-count overlap thresholds
five-game decay weights
soft rotation penalties
+0.05 first-nonempty fallback while exact repeat remains hard
```

Normative values live in `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`.

The selector is the only authority that chooses `selectedDrunkShownRole`.

### TBSP-3

Accepted deal planner proves:

```text
selected preset role multiset materializes exactly
Baron is never applied a second time
seat assignment uses independent deterministic namespace tb-seat-v1
same selection/seed/players replay identically
actual Drunk remains Drunk
shown Drunk role equals selector-owned choice
non-Drunk shown roles equal actual roles
```

### TBSP-4

Accepted recommendation lock bridge converts the selector/deal Drunk choice into exactly one locked downstream `DrunkShownRole` fact. Recommendation may generate compatible information, including Investigator-compatible information, but cannot replace the shown identity.

### TBSP-5

Dedicated rotation-history persistence owns:

```text
recordCompletedGame(gameId, selection)
historyFor(datasetId, schemaVersion, playerCount)
```

Accepted behavior includes stable-game retry idempotence, conflict rejection, newest-first order, max-five retention, player-count/dataset/schema isolation and fail-soft corrupt-history recovery.

Rotation history is cross-game diversity memory. It is **not** A3 immutable setup snapshot authority.

## 6. TBSP-6 production cutover — completed slices

### 6A — provenance codec — COMPLETE

`TroubleBrewingSetupProvenancePersistence` encodes/decodes exact preset selection provenance and never selects/shuffles/rerolls.

### 6B — production setup preparer — COMPLETE

Production preparation combines frozen dataset, rotation history, selector and exact deal planning through typed owners outside App root.

### 6C — deal role resolver — COMPLETE

Production `PlayerCard` actual/shown roles come from the committed deal plan.

### 6D — production start cutover — COMPLETE

Trouble Brewing start now follows:

```text
newClocktowerSeed()
-> load/parse final dataset
-> load matching rotation history
-> prepare preset + Drunk shown role + deal
-> resolve exact PlayerCards
-> commit cards / reset deal state with prepared seed
```

`startClocktowerGame()` branches Trouble Brewing before the legacy broad-random generator. Trouble Brewing no longer uses broad random role composition, Baron post-processing or a second Drunk shown-role draw.

### 6E — active-game provenance persist/restore — COMPLETE

Committed `TroubleBrewingSetupPresetSelection` is persisted in active game state and restored by exact decode. Restore does not invoke selector/preparer.

Older supported snapshots with no TBSP provenance restore with selection `null`; they must not fabricate an initial preset from later/current cards.

### 6F — true-completion rotation-history wiring — COMPLETE

Latest fully validated code checkpoint:

```text
5c10cd29111449e1f8af2b8944609a2002048679
```

Production gate semantics:

```text
not Clocktower -> no TB history write
not Trouble Brewing -> no TB history write
gameOutcome == null -> Restart/abandon/archive does not record
missing committed provenance -> do not fabricate history
true completed TB + committed provenance
    -> recordCompletedGame(clocktowerGameId, selection)
```

A failed durable completion-history write blocks archive/save clearing so retry can converge. Store `gameId` semantics own exactly-once behavior; do not add a second App dedupe mechanism.

## 7. Current work — TBSP-6G-A RED

Current RED code checkpoint:

```text
a26c221670fdea2612626f762d162b66091896af
```

Focused test:

```text
TroubleBrewingSetupRecommendationPrewarmCoordinatorTest
```

Current contract is intentionally narrow:

```text
same committed SetupCoordinationRequest
-> build once
-> prewarm reuses result
-> readyFor(same request) returns same result

changed committed request
-> readyFor misses
-> prewarm rebuilds
-> old request no longer counts as ready
```

Expected owner:

```text
TroubleBrewingSetupRecommendationPrewarmCoordinator
```

Do not broaden 6G-A into Compose lifecycle, First Night epistemic replay, A4/ZDD or persistence work.

## 8. Revised remaining TBSP-6 sequence

The original broad “background first-night/setup computation” task is now deliberately separated into two lifecycle layers.

### 6G-A — setup recommendation prewarm core — CURRENT

Implement the minimum coordinator required by the existing RED.

```text
SetupCoordinationRequest
-> SetupRecommendationService.ConstrainedResult
-> exact-request cache/reuse
```

### 6G-B — identity-reveal production wiring

Tests-first prove:

```text
committed TB deal
-> immediately enter PassPhone / RevealCard
-> prewarm setup recommendation off main thread during reveal
-> exact current request can reuse READY result
-> stale/mismatched request cannot be consumed as current
```

A cache miss must retain a safe existing computation path.

### 6H — First Night background precompute

Treat this separately from `SetupRecommendationService` caching.

Tests-first prove:

```text
RevealCard window begins
-> exact committed-game First Night input
-> background computation
-> First Night consumer
   READY -> consume
   BUSY  -> safely await at point of use
   MISS/stale -> safe fallback/recompute
```

Background work must never select another preset, reshuffle seats, reroll Drunk or mutate actual/shown identity.

### 6I — cutover acceptance matrix

Typed acceptance must close the remaining production gaps:

```text
restore preserves exact preset/seed/cards/shown identities/seat mapping
restore never selects/materializes a second setup
legacy supported save without TB provenance never fabricates rotation history
invalid/rejected TB preset data never falls back to broad-random TB setup
navigation before Start does not commit a preset selection
recomposition/navigation cannot reroll an already-started setup
incomplete Restart/abandon/archive never enters rotation history
completed TB records original initial selection exactly once
No Greater Joy setup behavior remains unchanged
identity reveal begins without synchronously waiting for expensive setup/first-night computation
```

Prefer typed seams and retire superseded brittle source-string assertions where possible.

### 6J — cleanup

After lifecycle ownership is stable, remove or give real ownership to dormant cutover parameters/APIs such as an unused `preparedSetupPlan`-style pass-through. Do not perform speculative cleanup before 6G/6H clarifies the true owner.

### 6K — final acceptance

```text
all focused acceptance GREEN
-> :app:testFast
-> affected T2/T3 when required by TESTING_STRATEGY
-> :app:testFull
-> R2
-> final GitHub CI
-> exact diff / scope audit
```

PR #57 remains Draft. Do not mark Ready or merge without explicit authorization.

## 9. Production merge-blocking invariants

Before TBSP acceptance, typed tests must prove:

```text
P1  Trouble Brewing actual roles originate from selected preset.
P2  Baron is never applied a second time.
P3  Drunk actual identity remains Drunk.
P4  Drunk shown role comes only from selected preset options.
P5  Later recommendation cannot replace selected Drunk shown role.
P6  Same dataset/history/seed reproduces same initial setup.
P7  Start selects/materializes setup only once.
P8  Compose recomposition cannot reroll a started setup.
P9  Navigation before Start does not commit a preset selection.
P10 No Greater Joy behavior remains unchanged.
P11 Restoring an already-started game does not select a new preset.
P12 Invalid TB preset data never silently falls back to broad random TB setup.
P13 Trouble Brewing identity dealing does not synchronously wait for complex first-night/setup calculation.
P14 Background first-night/setup computation consumes the committed deal and cannot mutate/reroll actual or shown identities.
P15 Only true completed Trouble Brewing games enter preset rotation history; Restart/abandon/archive alone does not.
P16 Completion-history persistence is retry-safe and records the original initial selection rather than later role state.
```

Current assessment:

```text
P1  IMPLEMENTED
P2  IMPLEMENTED
P3  IMPLEMENTED
P4  IMPLEMENTED
P5  IMPLEMENTED
P6  IMPLEMENTED; final production acceptance still required
P7  IMPLEMENTED; final production acceptance still required
P8  NEEDS explicit acceptance
P9  NEEDS explicit acceptance
P10 NEEDS final NGJ regression acceptance
P11 IMPLEMENTED; strengthen end-to-end restore acceptance
P12 NEEDS explicit typed no-fallback acceptance
P13 IMPLEMENTED structurally; lock with 6G-B lifecycle test
P14 OPEN — primary 6G/6H remaining risk
P15 IMPLEMENTED
P16 IMPLEMENTED
```

Priority of remaining risk:

```text
P14 async/reveal/First Night lifecycle
> P12 invalid-data no-fallback acceptance
> restore/recomposition/navigation acceptance
> NGJ regression acceptance
```

Do not reopen TBSP-1 through 6F without concrete regression evidence.

## 10. Testing cadence

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Default tests-first micro-cycle:

```text
RED
-> exact T0 RED
-> GREEN
-> exact T0 GREEN with --rerun-tasks
-> git diff --check / exact remote diff audit
```

`:app:testFast` is T1 and belongs at logical checkpoints rather than every micro-commit.

Run T4 `:app:testFull` at final TBSP production acceptance unless a specific earlier risk justifies escalation.

Local/focused evidence and GitHub CI serve different purposes; local validation does not replace final remote R2/CI.

## 11. Documentation authority and lifecycle

Active docs root should use:

```text
AGENTS.md
CURRENT_DEVELOPMENT_ROADMAP.md
NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md
TESTING_STRATEGY.md
TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
TBSP_ROTATION_WEIGHT_CONTRACT_V1.md when rotation semantics are relevant
```

Completed GCR/PR55/PR56/TBSP-1..5 execution handoffs are historical and should not be used as current instructions. Their closeout index lives under `docs/archive/` and Git history preserves exact old content.

A3 remains under `docs/archive/deferred/` until this roadmap explicitly reactivates it.

## 12. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_6_PRODUCTION_CUTOVER.md`;
4. read `docs/TESTING_STRATEGY.md`;
5. read `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`;
6. re-query live `main`, PR #57 head/state/checks and branch comparison;
7. distinguish later docs-only head from:
   - last fully GREEN code checkpoint `5c10cd29111449e1f8af2b8944609a2002048679`;
   - current 6G RED code checkpoint `a26c221670fdea2612626f762d162b66091896af`;
8. continue tests-first from **TBSP-6G-A coordinator GREEN**;
9. do not expand to 6G-B/6H in the same micro-slice;
10. preserve Dawn/Dusk exactly-once behavior;
11. do not change No Greater Joy behavior;
12. do not resume A3/A4/ZDD/Mayor/Imp-succession work;
13. keep PR #57 Draft and do not merge or mark Ready without explicit authorization.

## 13. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED UNTIL TBSP PRODUCTION ACCEPTANCE |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |
