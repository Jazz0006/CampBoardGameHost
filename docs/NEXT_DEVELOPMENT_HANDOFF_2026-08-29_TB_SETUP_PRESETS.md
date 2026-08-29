# CampBoardGameHost — Trouble Brewing Setup Preset Integration Handoff

> Created: 2026-08-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign: **TBSP — Trouble Brewing Setup Preset Integration**  
> Status: **PLANNED / NOT YET IMPLEMENTED**  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Branch baseline: `57b61a6a7d5be375612c2ec3590ff84518c9f277`  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Deferred successor: `docs/archive/deferred/NEXT_DEVELOPMENT_HANDOFF_2026-08-25_A3_SETUP_SNAPSHOT.md`

## 1. Why this campaign exists

Trouble Brewing setup is currently generated through broad runtime randomness. The production path in `CampBoardGameHostApp.kt` currently owns a private `generateClocktowerAssignments(playerCount, script)` helper that:

```text
1. chooses a Demon randomly;
2. chooses Minions randomly;
3. applies Baron +2 Outsider / -2 Townsfolk dynamically;
4. chooses Outsiders and Townsfolk randomly;
5. shuffles the final roles;
6. if Drunk is present, chooses an absent Townsfolk randomly as the shown role.
```

The project now has a separately curated and audited final Trouble Brewing preset dataset prepared for program integration:

```text
trouble_brewing_setup_presets_v2_final.json
schema_version: 2
dataset_id: trouble_brewing_setup_presets_v2_final
status: final_ready_for_program_integration
```

The final dataset contains 480 curated Trouble Brewing setups across player counts 5–15. Its default pool sizes are:

```text
5   30
6   30
7   50
8   50
9   50
10  50
11  50
12  50
13  40
14  40
15  40
TOTAL 480
```

This campaign integrates that final dataset into production without turning the data file into a rules engine.

## 2. Sequencing decision relative to A3

TBSP is intentionally scheduled **before** the remaining A3 immutable setup-snapshot ownership work.

Required sequence:

```text
PR #54 same-night/GCR correctness       MERGED
        ↓
TBSP Trouble Brewing preset integration CURRENT
        ↓
A3 immutable setup snapshot ownership   DEFERRED UNTIL TBSP COMPLETE
        ↓
subsequent historical-exact work
```

Reason:

A3 must eventually persist a trustworthy immutable setup origin. It is safer to harden the final production setup contract after the setup-generation authority has changed from broad random generation to the preset pipeline. Implementing A3 first would risk hardening an obsolete generation path and then reworking setup provenance immediately afterward.

TBSP must **not** implement A3 itself. In particular, the cross-game rotation memory introduced by TBSP is not the immutable setup snapshot required by A3.

## 3. Normative dataset facts

The final JSON and its accompanying audit material establish the following input contract.

### 3.1 Pool and legality facts

- 480 total presets.
- Player-count pools exist for every count from 5 through 15.
- Exactly one Imp is present in every preset.
- No setup contains duplicate actual roles.
- Same-player-count presets do not contain exact duplicate setups.
- Baron setup modification is already represented in each final preset.
- Default 5–6 player curated pools intentionally omit Baron, although Baron remains rules-legal outside this curated-default policy.
- Official Trouble Brewing examples were retained as hard anchors during curation.

### 3.2 Drunk facts

The final dataset contains 208 Drunk presets.

For every preset containing Drunk:

```text
drunk_as_options.size == 3
all options are unique Townsfolk
all options are absent from the real in-play setup
```

For every preset without Drunk:

```text
drunk_as_options is empty
```

The final Drunk-option audit reports 624 option slots and 208 unique three-role option triples.

### 3.3 Runtime selection policy explicitly defined by the dataset

The final JSON explicitly defines:

```text
exact_repeat: reject
similarity_scope:
    real in-play non-Demon role IDs
    Imp excluded because it is always present

overlap formula:
    intersection(candidate_non_demon_roles, previous_non_demon_roles)
    / (player_count - 1)
```

Maximum overlap with the immediately previous game:

```text
5   0.60
6   0.60
7   0.70
8   0.72
9   0.75
10  0.78
11  0.80
12  0.82
13  0.83
14  0.85
15  0.86
```

Recent-history weights:

```text
most recent      1.00
age 1            0.65
age 2            0.40
age 3            0.20
age 4            0.10
```

The dataset also names three additional soft penalties:

```text
same minion set as the immediately previous game
same primary style tag as several recent games
same Drunk disguise in consecutive games when Drunk is present
```

Fallback policy:

```text
if filtering leaves no candidates,
relax the last-game overlap threshold by +0.05 repeatedly,
then select from the first non-empty eligible pool.
```

## 4. OPEN DECISION — soft-penalty numeric contract

The final JSON names the three soft penalties but does **not** define:

- numeric penalty strengths;
- exact style-repeat trigger count;
- whether penalties are additive or multiplicative;
- minimum candidate weight;
- tie/probability combination details.

No implementation agent may invent these values silently.

Before TBSP-2 production selector GREEN is written, this contract must be explicitly frozen in this handoff or a successor decision document.

Current proposal for discussion, **NOT YET NORMATIVE**:

```text
weightedOverlap =
    Σ(overlap_i × historyWeight_i)
    / Σ(historyWeight_i)

baseNoveltyWeight = max(0.20, 1.0 - weightedOverlap)

additional multiplicative penalties:
    same immediately-previous minion set    × 0.70   (medium)
    primary style seen >= 2 of previous 5   × 0.88   (light)
    same consecutive Drunk shown role       × 0.40   (strong)

finalWeight = baseNoveltyWeight × applicable multipliers
```

The strong/medium/light ordering is consistent with the existing project `HistoryCooldown` philosophy, where immediate Drunk shown-role repetition is already treated as a strong cooldown signal. The exact TBSP values above remain an explicit product decision, not a fact derived from the preset dataset.

## 5. Core architectural decisions

### 5.1 Preset selects composition; rules remain legality authority

Required boundary:

```text
Preset data
-> recommends/selects one curated legal role composition

Rules/domain validation
-> remains the authority that decides whether the composition is legal
```

The JSON must not become a second rules engine. A parser and semantic validator are mandatory even though the dataset has already been audited externally.

### 5.2 Trouble Brewing only

Production cutover applies only to:

```text
ClocktowerScript.TroubleBrewing
```

`ClocktowerScript.NoGreaterJoy` retains its current setup-generation behavior in this campaign.

Do not generalize TBSP into a custom-script preset framework.

### 5.3 One setup seed must precede every random setup decision

Current production generates random assignments before creating `preparedSeed`. TBSP must reverse this ordering.

Required model:

```text
newClocktowerSeed()
        ↓
select preset
        ↓
select Drunk shown role when applicable
        ↓
shuffle selected roles onto seats
        ↓
construct PlayerCards
        ↓
run remaining setup recommendation logic
```

Random substeps should derive independent deterministic namespaces from the game seed, for example:

```text
tb-preset-v1
tb-drunk-v1
tb-seat-v1
```

Acceptance invariant:

```text
same dataset + same rotation history + same game seed
= same complete initial setup materialization
```

### 5.4 Presets never bind roles to seats

A preset determines the role multiset only. Seat assignment is a separate seeded shuffle.

This prevents curated templates from creating long-term seat-position bias.

### 5.5 Baron must never be applied twice

Final presets already encode Baron setup modification.

When a selected preset contains Baron, production must consume the preset role list exactly. It must **not** re-run the old `baronOutsiderIncrease` transform.

This is a merge-blocking invariant.

### 5.6 Drunk has one authority

The preset pipeline owns the allowed Drunk shown-role set and the selected shown role.

Required flow:

```text
preset.drunk_as_options
-> choose exactly one option deterministically
-> materialize actual role = Drunk
-> materialize shown role = selected Townsfolk
-> pass StorytellerDecision.DrunkShownRole(selectedRole) as lockedDecisions
-> existing setup recommendation system produces remaining setup decisions
```

Do not retain the old independent random Drunk shown-role selection in `generateClocktowerAssignments`.

The existing `SetupCoordinationRequest.lockedDecisions` seam is the intended integration boundary. Do not redesign the recommendation architecture merely to integrate presets.

If the locked shown role is Investigator, existing compatible `DrunkInvestigatorInfo` generation must remain available.

## 6. Intended source ownership

New policy/algorithm code should live outside `CampBoardGameHostApp.kt`.

Preferred structure:

```text
app/src/main/assets/setup/
    trouble_brewing_setup_presets_v2_final.json

app/src/main/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupPresetModels.kt
    TroubleBrewingSetupPresetJson.kt
    TroubleBrewingSetupPresetValidator.kt
    TroubleBrewingSetupPresetSelector.kt
    TroubleBrewingSetupDealPlanner.kt
    TroubleBrewingSetupRotationHistory.kt

app/src/main/java/com/codex/campboardgamehost/persistence/
    ClocktowerSetupRotationHistoryStore.kt
```

Exact filenames may change only if a live code audit shows a stronger existing ownership boundary. Do not move the new algorithm into Host or App root merely because the current setup callback has convenient state access.

## 7. TBSP-0 — baseline, documentation, executable contract

Status at creation of this handoff:

```text
live main:
57b61a6a7d5be375612c2ec3590ff84518c9f277

main commit:
Merge pull request #54 from Jazz0006/codex/clocktower-same-night-effective-state-correctness

new campaign branch:
codex/trouble-brewing-setup-presets-v2
```

TBSP-0 requirements:

1. correct `docs/CURRENT_DEVELOPMENT_ROADMAP.md` so PR #54 is recorded as merged;
2. record TBSP as the current implementation priority;
3. record A3 setup-snapshot work as deferred until TBSP is complete;
4. preserve the soft-penalty numeric contract as OPEN until explicitly frozen;
5. do not add production code in TBSP-0.

Docs-only validation follows the repository testing strategy: no local Android regression is required solely for documentation changes; CI change classification remains the remote gate if/when a PR exists.

## 8. TBSP-1 — final dataset asset, parser, validator

### 8.1 Scope

Package the exact final JSON as an offline Android asset and introduce typed parsing/validation.

Do not implement runtime selection in this slice.

### 8.2 RED 1A — parser contract

Create focused typed tests proving at least:

```text
schema_version == 2
dataset_id == trouble_brewing_setup_presets_v2_final
status == final_ready_for_program_integration
pools 5..15 all exist
pool sizes exactly match the final dataset
total presets == 480
```

Then implement the minimum parser GREEN.

### 8.3 RED 1B — semantic dataset validator

Validate all 480 presets:

```text
preset IDs unique
preset.playerCount matches owning pool
total actual roles == player count
exactly one Demon
Demon == Imp
no duplicate actual role
all role IDs resolve to Trouble Brewing roles
```

Composition validation:

```text
without Baron:
    composition == standard player-count distribution

with Baron:
    Outsider = standard + 2
    Townsfolk = standard - 2
    Minion/Demon counts unchanged

5–6 curated defaults:
    Baron absent
```

Drunk validation:

```text
Drunk absent:
    drunk_as_options empty

Drunk present:
    exactly 3 options
    options unique
    all are Townsfolk
    all are absent from actual setup
```

Dataset-level assertions may also prove the audited aggregate:

```text
208 Drunk presets
624 option slots
208 unique Drunk-option triples
```

### 8.4 TBSP-1 STOP

Do not add:

- history;
- selector policy;
- seat assignment;
- App wiring;
- persistence.

## 9. TBSP-2 — pure history-aware preset selector

### 9.1 Scope

Introduce a pure selector, conceptually:

```text
TroubleBrewingSetupPresetSelector
```

Inputs:

```text
dataset
playerCount
gameSeed
recentSetupRotationHistory
```

Output should contain typed provenance sufficient for later materialization, at minimum:

```text
datasetId
schemaVersion
presetId
playerCount
gameSeed
selected preset role IDs
selected Drunk shown role when applicable
```

### 9.2 RED 2A — player-count isolation

A request for N players can only return a preset from pool N.

### 9.3 RED 2B — deterministic replay

```text
same dataset + same player count + same history + same seed
-> same preset
-> same Drunk option
```

Input iteration order must not change the result.

### 9.4 RED 2C — exact repeat rejection

The exact previous real non-Demon composition is rejected.

Do not compare preset IDs as a substitute for role-composition equality.

### 9.5 RED 2D — last-game overlap threshold

For each player count, prove candidate eligibility at or below the configured threshold and rejection above it.

### 9.6 RED 2E — five-game history decay

Prove that history ages use the explicit final weights:

```text
1.00 > 0.65 > 0.40 > 0.20 > 0.10
```

### 9.7 RED 2F — soft rotation

After the numeric soft-penalty contract is frozen, separately test:

```text
same minion set
repeated primary style
same consecutive Drunk disguise
```

Each remains eligible unless another hard rule rejects it, but receives lower selection weight/probability.

### 9.8 RED 2G — fallback relaxation

Construct a fixture where:

```text
initial threshold -> no candidates
+0.05             -> no candidates
+0.05             -> candidates exist
```

Selection must occur from the first non-empty relaxation level.

Exact-repeat rejection must remain hard while overlap threshold relaxes.

### 9.9 TBSP-2 STOP

Do not wire the selector into App production yet.

## 10. TBSP-3 — deal materialization and Drunk identity

Introduce a pure deal materializer, conceptually:

```text
TroubleBrewingSetupDealPlanner
```

Responsibilities:

```text
selected preset
+ selected Drunk shown role
+ player seats
+ derived seat seed
-> deterministic seat assignments
```

### RED 3A — no Baron double application

For a Baron preset:

```text
materialized actual-role multiset == preset actual-role multiset
```

No second setup modifier is allowed.

### RED 3B — exact multiset identity

Every materialized deal must contain exactly the selected preset roles, not merely the same team counts.

### RED 3C — deterministic seat shuffle

```text
same seat seed -> same seat map
different suitable seat seeds -> may produce different seat maps
```

Role order in the JSON must not be interpreted as seat order.

### RED 3D — Drunk actual/shown identity

```text
actual role == Drunk
shown role ∈ selected preset.drunk_as_options
shown role not actually in play
```

## 11. TBSP-4 — integrate with existing setup recommendation

Preset selection must not replace the existing setup clue/recommendation system.

Required integration:

```text
selected Drunk shown role
-> StorytellerDecision.DrunkShownRole
-> SetupCoordinationRequest.lockedDecisions
-> existing ClocktowerRecommendationCoordinator / setup recommendation
-> remaining setup decisions
```

### RED 4A — locked Drunk role is stable

Every returned setup plan must preserve the exact locked Drunk shown role.

### RED 4B — Drunk Investigator companion information survives

When Investigator is the locked Drunk shown role, compatible `DrunkInvestigatorInfo` must still be generated rather than suppressed by the lock.

### Scope guard

Do not retune:

- red-herring scoring;
- Demon bluff scoring;
- setup plan styles;
- general recommendation ranking.

## 12. TBSP-5 — dedicated cross-game setup rotation history

The runtime selector needs recent setup history, but this must remain separate from the in-game GLOBAL action/observation timeline and from future A3 immutable setup-snapshot ownership.

Do **not** reconstruct initial composition from mutable end-game cards. Roles may change during play through Demon succession, Scarlet Woman promotion, and future mechanics.

Introduce a small dedicated durable rotation record. Conceptual shape:

```text
selectionId
datasetId
schemaVersion
presetId
playerCount
nonDemonRoleIds
minionRoleIds
primaryStyleTag
drunkShownRole?
```

Keep only the most recent five entries because the final runtime policy defines five history weights.

### RED 5A — bounded history

Appending a sixth unique record removes the oldest and leaves exactly five.

### RED 5B — idempotent record commit

Recommitting the same `selectionId` must not double-count one game because of recomposition/retry.

### RED 5C — corrupt stored history

Corrupt optional rotation-history storage must fail safely to an empty/usable history state rather than prevent application startup.

### Architectural warning

`SetupRotationHistory` is **not** A3 `setupSnapshot`.

It must never be used as the exact historical replay origin.

## 13. TBSP-6 — production cutover

Only after TBSP-1 through TBSP-5 typed behavior is GREEN should production setup be switched.

Intended orchestration:

```text
user confirms Start
-> create gameSeed once
-> choose path by script

TroubleBrewing:
    load validated final dataset
    -> load recent setup rotation history
    -> select preset
    -> choose Drunk shown role if present
    -> shuffle roles onto seats
    -> create PlayerCards
    -> run existing setup recommendation with locked Drunk decision

NoGreaterJoy:
    retain current legacy setup generation
```

Selection must occur at the start/commit boundary, not on Compose recomposition and not while moving backward/forward through setup screens.

## 14. Production merge-blocking invariants

Before TBSP is accepted, typed tests must prove all of the following:

```text
P1  Trouble Brewing actual roles originate from the selected preset.
P2  Baron is never applied a second time.
P3  Drunk actual identity remains Drunk.
P4  Drunk shown role comes only from the selected preset's three options.
P5  Later setup recommendation cannot replace the preset-selected Drunk shown role.
P6  Same dataset/history/seed reproduces the same initial setup.
P7  Start selects/materializes the setup only once.
P8  Compose recomposition cannot reroll a started setup.
P9  Navigation before Start does not commit a preset selection.
P10 No Greater Joy behavior remains unchanged.
P11 Restoring an already-started active game does not select a new preset.
P12 A missing/corrupt/invalid Trouble Brewing preset dataset does not silently fall back to broad random Trouble Brewing generation.
```

P12 should fail closed or present an explicit recoverable setup-start failure. Silent legacy-random fallback would hide a broken production contract.

## 15. Test cadence

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Normal development cycle:

```text
T0 exact focused RED
-> expected assertion/JUnit failure = RED PASS
-> minimal GREEN
-> exact T0 GREEN with actual execution when required
-> git diff --check
-> commit/push
-> remote parent/diff/scope audit
```

Logical checkpoints:

```text
TBSP-1 parser/validator
    focused tests
    -> T1 :app:testFast

TBSP-2 selector
    focused tests
    -> T1
    -> affected selection/distribution validation

TBSP-3 deal planner
    focused tests
    -> T1

TBSP-4 recommendation integration
    focused tests
    -> SetupMigrationTest as affected T2/T3 validation
    -> ExpertRecommendationReview when actually affected

TBSP-5 persistence
    focused persistence tests
    -> affected restore/integration tests
    -> T1

TBSP-6 production cutover
    focused typed integration/wiring tests
    -> T1
    -> affected T2/T3
    -> T4 full acceptance checkpoint before merge
```

`SetupMigrationTest` is already classified as expensive T3-style coverage and must not be rerun after every micro-edit merely because this campaign concerns setup.

Final acceptance should use an explicit `[full-ci]` checkpoint when appropriate so the repository performs complete selected acceptance gates, including full Android JVM validation and debug assemble plus other full gates selected by CI routing.

## 16. Preferred test ownership

Prefer typed tests under the new setup package:

```text
clocktower/setup/
    TroubleBrewingSetupPresetJsonTest.kt
    TroubleBrewingSetupPresetValidatorTest.kt
    TroubleBrewingSetupPresetSelectorTest.kt
    TroubleBrewingSetupDealPlannerTest.kt
    TroubleBrewingSetupRecommendationIntegrationTest.kt

persistence/
    ClocktowerSetupRotationHistoryStoreTest.kt
```

Production wiring should be proved through a callable typed seam where practical. Retain source inspection only for coarse architecture/ownership guarantees when runtime proof is impractical.

Do not create brittle source-string tests for local variable names, formatting, or exact call spelling.

## 17. Explicitly out of scope

TBSP does **not** authorize:

- A3 immutable setup snapshot implementation;
- historical-exact replay redesign;
- GLOBAL timeline changes;
- A4/ZDD recommendation authority promotion;
- GCR-4 or GCR-5 deferred work;
- custom-script preset infrastructure;
- No Greater Joy presets;
- Mayor/Imp generic succession expansion;
- misinformation redesign;
- history UI;
- user-facing preset browser/manual preset chooser;
- changes to the curated content of the 480 final presets;
- unrelated App-root decomposition.

If a defect is found in the final dataset itself, stop and treat dataset correction as a separately reviewed data change rather than silently editing presets during integration.

## 18. Hard STOP conditions

Stop and return to Chat architecture review if implementation would:

- apply Baron setup modification after consuming a final preset;
- allow two independent Drunk shown-role authorities;
- silently fall back from invalid preset data to broad random Trouble Brewing generation;
- infer initial setup history from mutable end-game roles;
- treat cross-game rotation history as the future A3 immutable setup snapshot;
- add hundreds of lines of new policy to `CampBoardGameHostApp.kt` or Host;
- broaden presets to other scripts;
- alter existing recommendation ranking merely to make preset integration easier;
- require guessing the unresolved soft-penalty numeric contract;
- change persistence/session identity outside the explicitly scoped rotation-history storage without a dedicated RED/schema plan.

## 19. Resume protocol

When continuing TBSP in a future conversation/session:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. read `docs/TESTING_STRATEGY.md`;
5. re-query live `main`, this branch head and any active PR/checks;
6. distinguish docs-only head from the last fully validated production checkpoint;
7. re-confirm the exact final preset JSON source before copying it into app assets;
8. check whether the soft-penalty numeric contract has been explicitly frozen;
9. continue from the first incomplete TBSP slice using tests-first;
10. do not start A3 until TBSP production acceptance is complete unless the user explicitly changes the roadmap.

## 20. Next executable action

The next implementation slice after this documentation checkpoint is:

```text
TBSP-1A RED
```

Create the typed dataset parser contract test first, proving:

```text
schema_version == 2
dataset_id == trouble_brewing_setup_presets_v2_final
status == final_ready_for_program_integration
all player-count pools 5..15 exist
exact configured pool sizes
total == 480
```

Do not write the parser GREEN until the focused RED has been executed and fails for the expected missing/not-yet-implemented contract.