# TBSP Production Cutover Contract V1

> Created: 2026-08-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign: **TBSP — Trouble Brewing Setup Preset Integration**  
> Scope: **Trouble Brewing production cutover only**  
> Status: **NORMATIVE FOR TBSP-4/5/6 PLANNING AND ACCEPTANCE**

## 1. Purpose

The curated Trouble Brewing preset pipeline replaces three legacy setup authorities that currently live in or around `generateClocktowerAssignments(...)` and the synchronous start-game setup flow.

For Trouble Brewing production cutover, the following legacy responsibilities must no longer decide setup state:

```text
1. broad random role-composition generation
2. Baron-triggered post-generation composition mutation
3. later setup/recommendation selection or replacement of the Drunk shown role
```

The cutover must also remove the current latency coupling that makes identity dealing wait for complex first-night/setup recommendation work.

No Greater Joy is explicitly outside this contract and must retain its current behavior until separately audited.

## 2. Setup authority after cutover

The intended Trouble Brewing authority chain is:

```text
newClocktowerSeed()
    ↓
TroubleBrewingSetupPresetSelector
    ├─ selected curated preset
    └─ selectedDrunkShownRole when Drunk is present
    ↓
TroubleBrewingSetupDealPlanner
    ├─ deterministic independent seat assignment
    ├─ actual-role identity
    └─ shown-role identity
    ↓
PlayerCards become deal-ready
    ↓
identity reveal may begin immediately
```

The selected preset is already the final actual-role composition. Materialization must never reinterpret it as a request to re-run setup modifiers.

## 3. Cutover invariant A — no legacy broad-random Trouble Brewing setup

After production cutover, the Trouble Brewing start path must not use `generateClocktowerAssignments(...)` or any equivalent broad-random role-composition generator as an authority.

Missing, corrupt or invalid Trouble Brewing preset data must fail safely. It must not silently fall back to legacy broad-random Trouble Brewing generation.

The legacy helper may remain temporarily for other scripts such as No Greater Joy; physical deletion is not required by this invariant.

## 4. Cutover invariant B — Baron is data, not a second setup command

The curated preset dataset already encodes Baron setup modification exactly once.

Therefore, once a preset has been selected:

```text
selected preset actual-role multiset
==
materialized actual-role multiset
```

No later layer may see `Baron` and apply another `+2 Outsider / -2 Townsfolk` transform.

This invariant is first enforced by TBSP-3A and remains merge-blocking through production acceptance.

## 5. Cutover invariant C — one Drunk shown-role authority

`TroubleBrewingSetupPresetSelector` is the only authority that chooses the Drunk shown role.

For a selected Drunk preset:

```text
actual role = Drunk
shown role = selection.selectedDrunkShownRole
shown role ∈ preset.drunk_as_options
```

The deal planner consumes that value without another draw.

Later setup recommendation may consume the committed shown role as a locked fact when computing compatible information, but it must not choose, replace or reroll the Drunk shown role.

The following legacy Trouble Brewing behaviors must therefore leave the production path:

```text
generateClocktowerAssignments(...) random Drunk fake-role draw
SetupCoordination / StorytellerDecision.DrunkShownRole as a replacement authority
```

If Investigator is the committed Drunk shown role, later recommendation logic may still compute compatible Drunk Investigator information. That is downstream information generation, not shown-role selection.

## 6. Cutover invariant D — identity dealing must not wait for complex first-night calculation

The Trouble Brewing start/deal path must not synchronously block identity dealing on complex first-night/setup recommendation computation.

Required lifecycle:

```text
select preset + Drunk shown role
    ↓
materialize deterministic deal
    ↓
commit deal-ready PlayerCards
    ↓
enter PassPhone / RevealCard immediately
    ↓
use the identity-reveal window for background first-night/setup computation
    ↓
consume completed results before the relevant first-night information is actually needed
```

The expensive calculation must run off the UI/main thread and must not mutate the already committed actual/shown identity deal.

Existing `A4IdentityRevealPrewarmCoordinator` behavior demonstrates that the App already has an identity-reveal lifecycle seam and background execution pattern. It is not itself the production implementation of this contract: current A4 prewarm is debug-scoped and narrower than the production setup/first-night requirement.

## 7. Ownership boundary for TBSP slices

This contract does not broaden TBSP-3.

```text
TBSP-3  pure deal materialization only
TBSP-4  integrate committed Drunk shown role with remaining setup recommendation
TBSP-5  durable cross-game rotation history
TBSP-6  Trouble Brewing production cutover / restore / no-reroll ownership
```

The latency/lifecycle change in section 6 belongs to production cutover work, not the TBSP-3 pure planner.

Do not pull App production wiring, persistence, recommendation retuning, No Greater Joy changes or A3 immutable setup snapshots into TBSP-3.

## 8. Production acceptance requirements

Before TBSP production acceptance, typed tests must prove at minimum:

```text
C1  Trouble Brewing start never uses the legacy broad-random setup authority.
C2  Selected preset roles materialize exactly; Baron is never applied twice.
C3  Drunk shown role is selector-owned and cannot be replaced downstream.
C4  Start/deal can enter identity reveal without waiting for complex first-night/setup calculation.
C5  Background calculation consumes the committed deal and cannot reroll actual/shown identities.
C6  Results needed for First Night are ready or safely awaited at the point of use, not before dealing begins.
C7  No Greater Joy behavior remains unchanged.
C8  Restore/recomposition does not select or materialize a second setup.
C9  Invalid TB preset data never falls back to broad random TB setup.
```

These requirements supplement, rather than replace, the merge-blocking invariants in `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.