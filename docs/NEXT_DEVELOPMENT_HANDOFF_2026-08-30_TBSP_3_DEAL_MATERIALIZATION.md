# CampBoardGameHost — TBSP-3 Deal Materialization Completion Handoff

> Updated: 2026-08-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign: **TBSP — Trouble Brewing Setup Preset Integration**  
> Status: **TBSP-3 COMPLETE / TBSP-4 NOT STARTED**  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR / CI carrier: **#57 — TBSP: integrate Trouble Brewing setup presets**  
> Live `main` baseline: `ba7cfa12853a8829ecf228c05cf2a22067f1e6e4`  
> TBSP-2 validated checkpoint: `80a5b9306009a4d078623b997b5b42a88de21080`  
> TBSP-3 validated code checkpoint: `8e918f69f6184a6389a23881af42127a3d761ef2`  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Production cutover contract: `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`

## 1. Live state to preserve

At TBSP-3 completion:

```text
main:
ba7cfa12853a8829ecf228c05cf2a22067f1e6e4

branch:
codex/trouble-brewing-setup-presets-v2

PR #57:
OPEN
DRAFT
NOT MERGED

latest validated TBSP-3 code checkpoint:
8e918f69f6184a6389a23881af42127a3d761ef2
```

Validation at `8e918f69...`:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
full Android unit/build step: skipped by normal FAST routing
ASP contract tests: skipped
Real Clingo cross-validation: skipped
```

No local Gradle T0 was executed from this Chat environment because no repository checkout / `gradlew` is mounted here. Test evidence above is from the GitHub Actions Gradle FAST route triggered by the commits themselves. Do not record a local T0 that did not happen.

Later documentation-only commits must be distinguished from the validated code checkpoint above.

Do not merge PR #57 or mark it Ready without explicit user authorization.

## 2. TBSP-3 implemented ownership

Source:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlanner.kt
```

Tests:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/setup/
    TroubleBrewingSetupDealPlannerTest.kt
```

The pure planner consumes:

```text
TroubleBrewingSetupPresetSelection
+ orderedPlayerNames
```

and produces:

```text
TroubleBrewingSetupDealPlan
    datasetId
    schemaVersion
    presetId
    playerCount
    gameSeed
    selectedDrunkShownRole
    assignments[]
        seat
        playerName
        actualRoleId
        shownRoleId
```

No App state is committed by the planner.

## 3. Accepted materialization contracts

### 3.1 Exact preset identity / no Baron replay

The selected preset is already the final actual-role composition.

The planner materializes exactly:

```text
preset.townsfolk
+ preset.outsiders
+ preset.minions
+ preset.demons
```

It never interprets `Baron` as a command to apply another `+2 Outsider / -2 Townsfolk` transform.

This closes the pure-planner portion of the no-Baron-double-application requirement.

### 3.2 Deterministic independent seat assignment

Role-list order is canonicalized and seat materialization uses the independent namespace:

```text
tb-seat-v1
```

Current key shape:

```text
tb-seat-v1|datasetId|playerCount|presetId|gameSeed|roleId
```

The planner sorts unique actual role IDs by the unsigned MurmurHash3 key and then maps them to the stable ordered player identities:

```text
seat = orderedPlayerNames index + 1
```

Required behavior is covered:

```text
same selection + same seed + same players -> same assignment
same role multiset with role-list input reordered -> same assignment
different suitable seeds -> can produce different assignment
```

The curated dataset role-list order therefore does not bind roles to player seats.

### 3.3 Drunk identity ownership

The selector remains the only Drunk shown-role authority.

For a Drunk selection:

```text
actualRoleId == drunk
shownRoleId == selection.selectedDrunkShownRole
selectedDrunkShownRole ∈ preset.drunkAsOptions
selectedDrunkShownRole not in actual in-play roles
```

For all non-Drunk assignments:

```text
shownRoleId == actualRoleId
```

The planner performs no second disguise draw and no recommendation call.

For a non-Drunk preset, carrying a non-null Drunk shown role is rejected as inconsistent input.

## 4. Tests-first history

### TBSP-3A — Baron exact multiset

RED:

```text
cb84e04db546c41494a87a1298d1ec48f4211c38
```

Observed failure through `:app:testFast` CI:

```text
Unresolved reference 'TroubleBrewingSetupDealPlanner'
```

GREEN:

```text
39fcedfe57fd214479a1afad802a9f5cb4648f34
```

### TBSP-3B — ordinary exact multiset lock-in

Test commit:

```text
a45ad77f6beadda2275a82d98de1d44a48284069
```

This test was GREEN immediately because the generalized 3A implementation already preserved exact ordinary preset role identity. **Do not invent a RED for 3B.**

### TBSP-3C — deterministic seat materialization

RED:

```text
20e383f1c30e62569ec4523c35ce2340082c6009
```

At that point the planner ignored `gameSeed`; Android FAST failed as expected on the new deterministic/different-seed contract.

GREEN:

```text
3f8d7312075e872e25d6413bb7bf06f2a1b8ff10
```

Introduced canonicalized `tb-seat-v1` materialization. R2 and Android FAST / CI were GREEN.

### TBSP-3D — selector-owned Drunk shown role

RED:

```text
6fc96b642695ff70c5fec30ce77de63e08ff6fbf
```

The new test required assignment `shownRoleId` and plan `selectedDrunkShownRole`; Android FAST / CI failed as expected because that contract did not yet exist.

GREEN / final code checkpoint:

```text
8e918f69f6184a6389a23881af42127a3d761ef2
```

R2, Android FAST and CI gate all passed.

## 5. Scope audit — what TBSP-3 did NOT change

TBSP-3 remained pure. It did not modify:

```text
CampBoardGameHostApp production start wiring
active-game persistence
cross-game rotation-history persistence
SetupCoordination / recommendation wiring
No Greater Joy behavior
A3 immutable setup snapshot ownership
```

Current Trouble Brewing production still uses the legacy path and therefore remains intentionally unchanged at this checkpoint.

## 6. Frozen production-cutover requirements

During this work the production-cutover requirements were made explicit in:

```text
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

Trouble Brewing production must eventually remove these old authorities:

```text
1. broad random role-composition generation
2. Baron post-generation composition mutation
3. random/later replacement of the Drunk shown role
```

The intended authority chain is now:

```text
newClocktowerSeed()
-> TroubleBrewingSetupPresetSelector
   -> selected preset
   -> selectedDrunkShownRole
-> TroubleBrewingSetupDealPlanner
   -> deterministic tb-seat-v1 assignments
   -> actual/shown identities
-> commit deal-ready PlayerCards
```

The final cutover must not silently fall back to broad random Trouble Brewing generation when the preset asset is invalid or unavailable.

No Greater Joy remains outside this cutover contract until separately audited.

## 7. Identity-reveal latency contract

The production start flow currently still has a latency coupling:

```text
legacy assignments
-> provisional cards
-> if Drunk, synchronous SetupCoordination
-> possibly replace shown role
-> commit cards
-> identity reveal
```

The required future Trouble Brewing lifecycle is:

```text
select preset + Drunk shown role
-> materialize deterministic deal
-> commit PlayerCards
-> enter PassPhone / RevealCard immediately
-> perform expensive remaining first-night/setup calculation in background
-> consume results when first-night information is actually needed
```

The expensive computation must not mutate/reroll the committed actual or shown identities.

Existing `A4IdentityRevealPrewarmCoordinator` demonstrates that the App already has an identity-reveal/background-execution seam, but its current DEBUG/5-player scope is not the production implementation.

This lifecycle change belongs to later production cutover, not TBSP-3.

## 8. Next logical slice — TBSP-4, but NOT STARTED

Conceptual next work is:

```text
TBSP-4 — integrate selector-owned Drunk shown role with existing setup recommendation
```

Required architectural boundary when that work is authorized:

```text
selected Drunk shown role is already committed identity
-> pass it downstream as a locked fact/decision
-> recommendation may compute compatible information
-> recommendation must never replace/reroll the shown identity
```

If Investigator is the committed shown role, compatible `DrunkInvestigatorInfo` must remain available.

Do not retune recommendation scoring merely to implement this lock.

However, the user's existing scope explicitly excluded setup recommendation wiring during the current TBSP-3 work. Therefore **do not start TBSP-4 implementation from this handoff unless that scope is explicitly lifted.**

## 9. Remaining campaign sequence

```text
TBSP-3 pure deal materialization              COMPLETE
        ↓
TBSP-4 recommendation lock integration        NOT STARTED
        ↓
TBSP-5 durable cross-game rotation history    NOT STARTED
        ↓
TBSP-6 production cutover / restore ownership NOT STARTED
        ↓
full TBSP acceptance
        ↓
A3 immutable setup snapshot
```

TBSP-6 must include the reveal-window background-computation requirement from `TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`.

## 10. Resume protocol

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this completion handoff;
4. read `docs/TESTING_STRATEGY.md`;
5. read `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`;
6. re-query live `main`, PR #57 head/state/checks and branch comparison;
7. distinguish validated TBSP-3 code checkpoint `8e918f69f6184a6389a23881af42127a3d761ef2` from later docs-only commits;
8. treat TBSP-4 as NOT STARTED;
9. before any recommendation integration, reconfirm that setup-recommendation wiring is now authorized;
10. keep PR #57 Draft and do not merge or mark Ready without explicit authorization;
11. do not change No Greater Joy, persistence or A3 as part of the TBSP-3 checkpoint.