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
80a5b9306009a4d078623b997b5b42a88de21080

current post-checkpoint TBSP code state:
TBSP-3A RED test added at cb84e04db546c41494a87a1298d1ec48f4211c38
expected missing planner contract observed through :app:testFast CI
later commits may be docs-only until TBSP-3A GREEN begins

validated checkpoint vs main:
ahead 32 / behind 0
merge base == main

current campaign:
TBSP — Trouble Brewing Setup Preset Integration

current successor handoff:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_3_DEAL_MATERIALIZATION.md

campaign design baseline:
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_TB_SETUP_PRESETS.md

normative TBSP-2 rotation policy:
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md

normative Trouble Brewing production cutover contract:
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

The poison exactly-once hotfixes remain part of the required TBSP baseline and must be preserved.

Current TBSP implementation status:

```text
TBSP-0 documentation / campaign plan: COMPLETE
TBSP-1A final dataset asset + typed parser: COMPLETE
TBSP-1B semantic validator: COMPLETE
TBSP-2 pure history-aware selector: COMPLETE
TBSP-3 pure deal materialization: IN PROGRESS — 3A RED ESTABLISHED / GREEN NEXT
TBSP-4 setup recommendation integration: NOT STARTED
TBSP-5 durable cross-game rotation-history storage: NOT STARTED
TBSP-6 production cutover / restore ownership: NOT STARTED
A3 immutable setup snapshot: DEFERRED UNTIL TBSP PRODUCTION ACCEPTANCE
```

At code checkpoint `80a5b9306009a4d078623b997b5b42a88de21080`:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
full Android unit/build step: correctly skipped by normal FAST routing
ASP contract tests: correctly skipped
Real Clingo cross-validation: correctly skipped
```

Later documentation-only or RED-test commits must be distinguished from that last fully validated production checkpoint.

## 2. Accepted predecessor correctness baseline

### PR #54 — same-night correctness / GCR hardening

PR #54 is merged and closed. Its accepted correctness remains part of the baseline and must not be reopened merely because TBSP touches initial setup.

Important accepted contracts include:

- First Night Fortune Teller uses base/current-role authority rather than entering Other Night chronology projection.
- Other Night Fortune Teller continues to use canonical same-night effective-state projection.
- current living-Demon UI authority remains distinct from transient current-night reconstruction authority during pending Imp succession.
- old Imp mechanical death, pending successor identity and canonical ordering remain separate concerns.
- poisoned Spy uses the accepted fail-safe product policy: wake normally, but no fabricated Grimoire is produced and no false Grimoire observation is persisted.
- gameplay semantics remain primarily protected by typed tests rather than brittle source-string assertions.

### PR #55 — Dawn poison exactly-once

Merged into `main` at:

```text
160f730594d76c294542cd22a5220baeb73d1bc9
```

It established exactly-once Dawn poison materialization and retry convergence across ordinary and successor-Dawn paths.

### PR #56 — next-night / Dusk poison expiry exactly-once

Merged into `main` at:

```text
ba7cfa12853a8829ecf228c05cf2a22067f1e6e4
```

It established typed Dusk poison-expiry ownership, stable history identity, restore/retry convergence, and completion before Night phase/round become durable.

TBSP must preserve both PR #55 and PR #56 behavior.

## 3. Current priority — TBSP Trouble Brewing Setup Preset Integration

Goal:

Replace broad random Trouble Brewing role-composition generation with selection from the final curated Trouble Brewing preset dataset while preserving:

- rules legality as an independent authority;
- deterministic/reproducible setup materialization;
- independent seat shuffling;
- one Drunk shown-role authority;
- existing setup recommendation behavior for remaining downstream information decisions;
- immediate identity dealing without synchronously waiting for expensive first-night/setup calculation;
- No Greater Joy current behavior;
- safe cross-game rotation without conflating it with A3 historical setup provenance.

The production-cutover lifecycle and ownership contract is frozen in:

```text
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

Final external dataset:

```text
trouble_brewing_setup_presets_v2_final.json
schema_version: 2
dataset_id: trouble_brewing_setup_presets_v2_final
status: final_ready_for_program_integration
```

Audited aggregate:

```text
player counts: 5..15
preset count: 480
pool sizes: 30,30,50,50,50,50,50,50,40,40,40
Drunk presets: 208
Drunk options per Drunk preset: exactly 3
```

Android asset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
```

Verified Git blob identity:

```text
a935474bec07577eb9e753bad2135a604add63f5
```

Do not reformat or regenerate the source dataset during integration.

## 4. TBSP-1 accepted checkpoint — COMPLETE

### TBSP-1A — final asset and parser

Accepted ownership:

```text
final dataset asset
TroubleBrewingSetupPresetModels
TroubleBrewingSetupPresetJson
focused parser contract test
```

Executable contract includes:

```text
schema_version == 2
dataset_id == trouble_brewing_setup_presets_v2_final
status == final_ready_for_program_integration
pools 5..15 all exist
pool sizes match the final dataset
total presets == 480
```

The parser intentionally keeps dataset character IDs in their external lowercase representation until canonical resolution is explicitly required.

### TBSP-1B — semantic validator

Accepted validator coverage includes:

```text
unique preset IDs
preset.playerCount matches owning pool
total actual roles == player count
exactly one Demon == Imp
no duplicate actual role
all IDs resolve through canonical Trouble Brewing registry
role category matches registry ownership
standard composition unless Baron
Baron outsider +2 / townsfolk -2 exactly once
5–6 curated defaults contain no Baron
Drunk absent -> empty drunk_as_options
Drunk present -> exactly three unique absent Townsfolk options
208 Drunk presets / 624 option slots / 208 unique option triples
```

TBSP-1 did not wire production setup.

## 5. TBSP-2 accepted checkpoint — COMPLETE

TBSP-2 is a pure history-aware selector and remains outside App production wiring.

Accepted ownership includes:

```text
TroubleBrewingSetupPresetSelector.kt
TroubleBrewingSetupPresetRotationScorer.kt
TroubleBrewingSetupRotationHistory.kt
TroubleBrewingSetupPresetSelectorTest.kt
TroubleBrewingSetupPresetRotationScorerTest.kt
```

Accepted tests-first slices:

```text
TBSP-2A player-count isolation + provenance
TBSP-2B deterministic preset and Drunk-option replay
TBSP-2C exact previous real non-Demon composition rejection
TBSP-2D last-game overlap threshold for all player counts 5..15
TBSP-2E five-game history decay
TBSP-2F soft rotation weighting + minimum weight floor
TBSP-2G +0.05 fallback to first non-empty level while exact repeat remains hard
```

The previously open soft-weight policy is now frozen by:

```text
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md
```

Normative values:

```text
history weights:
1.00, 0.65, 0.40, 0.20, 0.10

baseNoveltyWeight:
max(0.20, 1.0 - weightedOverlap)

soft multipliers:
same immediately-previous Minion set  × 0.70
primary style seen >=2 of 5 x0.88
same Drunk shown x0.40

final weight floor:
0.05

fallback:
+0.05 last-game overlap threshold repeatedly
stop at first non-empty eligible pool
exact-repeat composition never becomes eligible
maximum threshold 1.0
```

Selector deterministic namespaces already include:

```text
tb-preset-v1
tb-drunk-v1
```

The selector uses deterministic fixed-point weighted drawing. The candidate Drunk shown role used for soft weighting is the same selected disguise carried forward; there is no second Drunk authority.

Accepted final TBSP-2 code checkpoint:

```text
80a5b9306009a4d078623b997b5b42a88de21080
```

## 6. TBSP-3 current checkpoint — pure deal materialization

TBSP-3 is now in progress and remains **pure**, not production App wiring.

Successor handoff:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_3_DEAL_MATERIALIZATION.md
```

Preferred ownership:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlanner.kt

app/src/test/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlannerTest.kt
```

Required tests-first sequence:

```text
TBSP-3A RED — Baron preset materializes exact preset multiset; no second Baron modifier
TBSP-3B RED — exact selected preset role multiset identity
TBSP-3C RED — independent deterministic seat shuffle using tb-seat-v1
TBSP-3D RED — Drunk actual identity + selector-owned shown role preserved
```

### TBSP-3A current state

The live player/seat/card audit confirmed:

```text
PlayerCard has no explicit seat field.
Stable gameplay seat identity is existing card/player order: seat = index + 1.
PlayerState is a post-materialization gameplay type and is not appropriate planner input.
The smallest pure planner input can therefore consume ordered player identities/names and derive seat in output.
```

The focused TBSP-3A RED test was added at:

```text
cb84e04db546c41494a87a1298d1ec48f4211c38
```

It constructs an already Baron-adjusted 7-player selected preset and requires the materialized actual-role multiset to equal the selected preset multiset exactly.

CI `:app:testFast` failed for the expected missing contract:

```text
Unresolved reference 'TroubleBrewingSetupDealPlanner'
```

This is the intended RED, not a pre-existing production regression. An exact developer-selected T0 has not yet been observed in this environment; do not falsely record it as executed.

Next implementation action is the **minimum TBSP-3A GREEN** only. Do not pre-implement 3C seat-shuffle policy or 3D Drunk mapping while satisfying 3A.

TBSP-3 must remain pure:

```text
NO App production cutover
NO persistence
NO setup recommendation rewiring
NO No Greater Joy changes
NO A3 setup snapshot work
```

## 7. Current production setup authority audit

The current production setup path remains centered in `CampBoardGameHostApp.kt`.

Relevant legacy helper:

```text
generateClocktowerAssignments(playerCount, script)
```

Current Trouble Brewing production ordering remains:

```text
generateClocktowerAssignments(...)
-> newClocktowerSeed()
-> construct provisional PlayerCards
-> if Drunk, synchronously run SetupCoordination
-> possibly replace Drunk shown role
-> commit cards / enter deal flow
```

The legacy helper currently owns three Trouble Brewing responsibilities that must leave the TB production path at cutover:

```text
broad random role-composition generation
Baron +2 Outsider / -2 Townsfolk post-generation mutation
random Drunk fake/shown-role selection
```

Final presets already encode Baron setup modification. Production must never apply the old Baron transform after consuming a preset.

The selector already owns the single deterministic Drunk shown-role choice. Later recommendation may consume that identity as a locked fact, but it must not select or replace it.

The eventual Trouble Brewing production lifecycle is now explicitly:

```text
newClocktowerSeed()
-> select preset + selector-owned Drunk shown role
-> deterministic seat materialization
-> construct/commit deal-ready PlayerCards
-> enter PassPhone / RevealCard without waiting for expensive first-night/setup calculation
-> run remaining first-night/setup calculation in the identity-reveal window off the main thread
-> consume completed results when first-night information is actually needed
```

The existing `A4IdentityRevealPrewarmCoordinator` is an architectural precedent for the reveal-window/background-execution seam, but its current DEBUG/5-player scope is not the production implementation.

The normative cutover contract is:

```text
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

## 8. Remaining TBSP sequence

```text
TBSP-3 pure deal materialization              IN PROGRESS
        ↓
TBSP-4 existing recommendation integration
        ↓
TBSP-5 durable cross-game rotation history
        ↓
TBSP-6 production cutover / restore ownership
        ↓
full acceptance checkpoint
        ↓
A3 immutable setup snapshot ownership
```

TBSP-4 must turn the committed selector-owned Drunk shown role into a locked downstream fact rather than another recommendation choice.

TBSP-6 owns the production lifecycle change that allows identity reveal to begin before expensive first-night/setup computation completes.

A3 should harden the final production setup-origin contract, not the legacy broad-random generator.

## 9. Production merge-blocking invariants still outstanding

Before TBSP campaign acceptance, typed tests must ultimately prove:

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
```

TBSP-3 directly advances P1–P4 and the seat-materialization portion of P6. P13–P14 belong to the later production cutover, not the pure planner.

## 10. Resume protocol for next conversation

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-30_TBSP_3_DEAL_MATERIALIZATION.md`;
4. read `docs/TESTING_STRATEGY.md`;
5. read `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md` when production-cutover ownership or lifecycle is relevant;
6. re-query live `main`, PR #57 head/state/checks and branch comparison;
7. distinguish validated code checkpoint `80a5b9306009a4d078623b997b5b42a88de21080`, later docs-only commits and the TBSP-3A RED test commit `cb84e04db546c41494a87a1298d1ec48f4211c38`;
8. continue **TBSP-3A minimum GREEN — no Baron double application**;
9. run the smallest available Gradle T0 directly when a local checkout is available; otherwise record only tests actually observed through CI;
10. keep PR #57 Draft;
11. do not merge, mark Ready, or broaden into App wiring without explicit authorization.

## 11. Deferred work registry

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | PAUSED UNTIL TBSP PRODUCTION ACCEPTANCE |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |