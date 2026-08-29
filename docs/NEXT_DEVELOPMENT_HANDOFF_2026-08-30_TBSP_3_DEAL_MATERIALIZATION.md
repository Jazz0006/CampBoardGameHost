# CampBoardGameHost — TBSP-3 Deal Materialization Handoff

> Created: 2026-08-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign: **TBSP — Trouble Brewing Setup Preset Integration**  
> Status: **TBSP-2 COMPLETE / TBSP-3 NEXT**  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR / CI carrier: **#57 — TBSP: integrate Trouble Brewing setup presets**  
> Live `main` checkpoint: `ba7cfa12853a8829ecf228c05cf2a22067f1e6e4`  
> Last fully validated code checkpoint: `80a5b9306009a4d078623b997b5b42a88de21080`  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Campaign design baseline: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-29_TB_SETUP_PRESETS.md`  
> Normative rotation policy: `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`

## 1. Live checkpoint that must be preserved

At the 2026-08-30 handoff audit:

```text
main:
ba7cfa12853a8829ecf228c05cf2a22067f1e6e4

branch:
codex/trouble-brewing-setup-presets-v2

validated code head:
80a5b9306009a4d078623b997b5b42a88de21080

PR #57:
OPEN
DRAFT
NOT MERGED

branch vs main at validated code head:
ahead 32
behind 0
merge base == main
```

The validated `80a5b930...` checkpoint passed:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
full Android unit/build step: correctly skipped by normal FAST routing
ASP contract tests: correctly skipped
Real Clingo cross-validation: correctly skipped
```

Any later documentation-only commits must be distinguished from this last fully validated production checkpoint.

Do not merge PR #57 or mark it Ready without explicit user authorization.

## 2. TBSP-1 accepted baseline — COMPLETE

The following are already implemented and accepted on the branch:

```text
final frozen Trouble Brewing preset asset
schema-v2 typed parser/models
semantic validator
focused parser/validator tests
```

Final asset:

```text
app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json
schema_version: 2
dataset_id: trouble_brewing_setup_presets_v2_final
status: final_ready_for_program_integration
preset count: 480
Git blob: a935474bec07577eb9e753bad2135a604add63f5
```

Do not reformat or regenerate this dataset during later TBSP work.

The validator preserves the accepted legality/data contract, including:

```text
unique preset IDs
correct player-count ownership
exact role count
exactly one Demon == Imp
no duplicate actual roles
canonical Trouble Brewing role/category resolution
standard composition unless Baron
Baron outsider +2 / townsfolk -2 represented exactly once
5–6 curated defaults contain no Baron
Drunk absent -> no disguise options
Drunk present -> exactly 3 unique absent Townsfolk options
208 Drunk presets / 624 option slots / 208 unique triples
```

## 3. TBSP-2 pure history-aware selector — COMPLETE

TBSP-2 is complete at validated code checkpoint `80a5b930...`.

Implemented source ownership includes:

```text
TroubleBrewingSetupPresetSelector.kt
TroubleBrewingSetupPresetRotationScorer.kt
TroubleBrewingSetupRotationHistory.kt
```

Focused test ownership includes:

```text
TroubleBrewingSetupPresetSelectorTest.kt
TroubleBrewingSetupPresetRotationScorerTest.kt
```

### 3.1 Accepted selector contracts

The selector now proves and implements:

```text
2A player-count isolation + typed provenance
2B deterministic preset replay independent of input pool order
2B deterministic Drunk shown-role replay independent of option order
2C exact previous real non-Demon composition hard rejection
2D player-count-specific last-game overlap threshold for all 5..15 players
2E five-game weighted-overlap decay
2F soft rotation weighting + minimum weight floor
2G fallback overlap relaxation to the first non-empty level
```

### 3.2 Normative rotation policy

The previously open numeric decision is resolved. The authoritative document is:

```text
docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md
```

Required history weights:

```text
most recent  1.00
age 1        0.65
age 2        0.40
age 3        0.20
age 4        0.10
```

Base novelty:

```text
weightedOverlap =
    Σ(overlap_i × historyWeight_i)
    / Σ(historyWeight_i for available history entries)

baseNoveltyWeight = max(0.20, 1.0 - weightedOverlap)
```

Soft multiplicative penalties:

```text
same immediately-previous Minion set     × 0.70
primary style seen >= 2 of previous 5    × 0.88
same consecutive Drunk shown role        × 0.40
```

Final floor:

```text
finalWeight = max(0.05, rawWeight)
```

Fallback:

```text
start at dataset player-count threshold
if no candidate survives, increase threshold by +0.05
repeat until the first non-empty eligible pool
never re-enable exact-repeat composition
never exceed 1.0
```

Exact-repeat rejection is always a hard filter and is applied before overlap fallback.

### 3.3 Deterministic namespaces already established

Current selector behavior uses deterministic seed namespaces including:

```text
tb-preset-v1
tb-drunk-v1
```

Candidate weights are consumed through a deterministic fixed-point weighted draw. When candidate weights are equal, the selector preserves the deterministic canonical-pool modulo path.

The Drunk shown role is determined once per candidate before soft weighting where required, and the selected candidate carries that same precomputed disguise forward. Do not introduce a second Drunk draw.

## 4. TBSP-2 STOP boundary remains important

Despite selector completion, production setup has **not** been cut over.

Still not implemented:

```text
seat/deal materialization
CampBoardGameHostApp production wiring
SetupCoordinationRequest locked Drunk integration
cross-game durable rotation-history persistence
active-game restore integration
A3 immutable setup snapshot ownership
```

The current production broad-random setup path remains intact until later TBSP slices deliberately replace it.

Do not wire TBSP-3 directly into App while implementing the pure deal planner.

## 5. Next slice — TBSP-3 pure deal materialization

TBSP-3 should introduce a pure setup/deal materializer, conceptually:

```text
TroubleBrewingSetupDealPlanner
```

Preferred ownership:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlanner.kt

app/src/test/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlannerTest.kt
```

Do not put the algorithm into `CampBoardGameHostApp.kt` merely because App currently owns setup orchestration.

### Inputs

The pure planner should consume typed selector output/provenance rather than re-selecting a preset.

Conceptually:

```text
selected TroubleBrewing preset selection
+ ordered player/seat identities or seat count
+ game seed / derived seat seed
-> deterministic deal plan
```

The exact API must be chosen only after auditing current `PlayerCard` / seat identity types and existing setup construction seams.

### Output

The output must provide enough typed information for later App integration without committing production state itself. At minimum it must distinguish:

```text
seat/player identity
actual role
shown role when Drunk
selected preset provenance
selected Drunk shown role provenance
```

Do not add persistence in TBSP-3.

## 6. TBSP-3 tests-first sequence

### TBSP-3A RED — no Baron double application

Construct a selected preset containing Baron and prove:

```text
materialized actual-role multiset == selected preset actual-role multiset
```

The planner must not apply any additional Baron modifier.

This is a merge-blocking invariant because the final presets already encode Baron setup effects.

### TBSP-3B RED — exact multiset identity

For ordinary and Baron presets prove:

```text
materialized roles == exact selected preset roles
```

Do not merely assert Townsfolk/Outsider/Minion/Demon counts.

### TBSP-3C RED — deterministic independent seat shuffle

Add a separate deterministic seat namespace:

```text
tb-seat-v1
```

Required behavior:

```text
same selected preset + same game seed + same seats
-> same seat assignment

input preset role-list order changed without changing role multiset
-> same deterministic seat assignment contract after canonicalization

different suitable seeds
-> may produce different seat assignments
```

A curated preset must never bind a role to a fixed seat position.

### TBSP-3D RED — Drunk actual/shown identity

For a selected Drunk preset prove:

```text
actual role == Drunk
shown role == selector-selected Drunk shown role
shown role ∈ preset.drunk_as_options
shown role absent from actual in-play roles
```

The deal planner must consume the selector's chosen disguise. It must never choose another disguise.

## 7. Required audit before writing TBSP-3 RED

In the new conversation, first inspect the live definitions/usage of:

```text
PlayerCard / player identity / seat identity types
CampBoardGameHostApp setup-start construction seam
generateClocktowerAssignments(playerCount, script)
newClocktowerSeed()
TroubleBrewingSetupPresetSelection
StorytellerDecision.DrunkShownRole
SetupCoordinationRequest.lockedDecisions
```

The purpose of this audit is only to choose the smallest typed deal-plan API. Do not perform App wiring during the audit.

If existing seat/player types provide a stable identity boundary, reuse it. Do not invent a parallel seat identity model unless required by the live code.

## 8. Production setup authority that must not be changed yet

The campaign design baseline records the current legacy ordering as:

```text
generateClocktowerAssignments(...)
-> newClocktowerSeed()
-> construct PlayerCards
```

The eventual TBSP production order remains:

```text
newClocktowerSeed()
-> select preset
-> select Drunk shown role
-> deterministic seat shuffle
-> construct PlayerCards
-> run remaining setup recommendation
```

TBSP-3 implements only the pure materialization part. The App cutover comes later.

## 9. Later TBSP sequence after TBSP-3

Do not skip forward merely because the selector is complete.

Intended remaining sequence:

```text
TBSP-3 pure deal materialization
    ↓
TBSP-4 integrate selected Drunk role with existing setup recommendation
    ↓
TBSP-5 dedicated durable cross-game rotation-history storage
    ↓
TBSP-6 production cutover / restore / no-reroll ownership
    ↓
full acceptance checkpoint
    ↓
only then resume deferred A3 immutable setup snapshot work
```

### TBSP-4 key boundary

The selected Drunk shown role must flow through:

```text
StorytellerDecision.DrunkShownRole
-> SetupCoordinationRequest.lockedDecisions
-> existing recommendation coordinator
```

If Investigator is the locked shown role, compatible `DrunkInvestigatorInfo` must remain available.

Do not retune recommendation scoring in that slice.

### TBSP-5 key boundary

Cross-game rotation history is a small dedicated durable record and is **not** A3 setup snapshot history.

Do not infer initial setup from mutable end-game roles.

### TBSP-6 key boundary

Production cutover is Trouble Brewing only. `ClocktowerScript.NoGreaterJoy` remains on its current behavior.

Missing/corrupt/invalid TB preset data must never silently fall back to legacy broad random Trouble Brewing generation.

## 10. Production merge-blocking invariants still outstanding

Before the campaign is accepted, typed tests must ultimately prove:

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
```

TBSP-3 directly advances P1–P4 and the seat-shuffle portion of P6; it does not claim the App/start/restore invariants yet.

## 11. Testing cadence

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

For TBSP-3:

```text
RED 3A
-> focused T0 expected failure
-> minimum production GREEN
-> focused T0 GREEN --rerun-tasks where available
-> git diff --check
-> commit/push
-> remote parent/diff/scope audit

repeat for 3B / 3C / 3D

logical TBSP-3 checkpoint
-> :app:testFast
```

Do not run expensive `SetupMigrationTest` after every pure planner micro-slice. It becomes relevant when production/recommendation wiring is actually affected.

Do not claim a RED/GREEN execution that was not actually observed locally or through CI.

## 12. Hard STOP conditions

Return to architecture review instead of broadening scope if TBSP-3 would require:

- applying Baron again after reading a preset;
- choosing a second Drunk shown role;
- changing the selector's approved rotation policy;
- adding persistence;
- cutting over App production setup;
- changing No Greater Joy;
- changing recommendation ranking;
- implementing A3 setup snapshot ownership;
- adding large policy blocks to App/Host;
- binding curated preset role order directly to player seats.

## 13. New-conversation resume protocol

Start the next conversation with this exact sequence:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. read `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md` only as the frozen TBSP-2 policy reference;
5. read `docs/TESTING_STRATEGY.md`;
6. re-query live `main`, PR #57 head/state/checks and branch comparison;
7. distinguish later docs-only commits from last fully validated code checkpoint `80a5b9306009a4d078623b997b5b42a88de21080`;
8. audit the live player/seat/card types and setup construction seam listed in section 7;
9. start **TBSP-3A RED — no Baron double application**;
10. do not merge, mark Ready, or wire App production without explicit authorization.

## 14. Exact next executable action

After the live audit, the first code change should be a focused RED test for the pure deal planner:

```text
TBSP-3A RED
```

Required proof:

```text
given a selected Baron preset,
when the pure deal planner materializes seats,
then the actual-role multiset is exactly the preset's actual-role multiset,
with no second Baron outsider/townsfolk adjustment.
```

Do not implement the planner GREEN before that RED has been executed and fails for the expected missing/not-yet-implemented contract.