# DLB-1 Visible-Roster Deal Cutover — Live Fan-out and Implementation Audit

> Date: 2026-09-29 Australia/Sydney
> Baseline: `main@b739f47453ad22b3e71ec07148a99c871208d3a5`
> Branch: `dlb-1-visible-roster-deal-cutover`
> Scope: DLB-1 only — cut Trouble Brewing production setup over to visible-role seating while retaining a bounded compatibility projection for the current committed runtime.
> Authority: `docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`.

## 1. Architecture pre-flight

- current owner: `TroubleBrewingProductionSetupPreparer` selects the preset, then uses `TroubleBrewingShownIdentityPolicySource -> SetupShownIdentityCommitter` to bind a Drunk shown role before seating; `TroubleBrewingSetupDealPlanner` then seats an actual Drunk token.
- proposed responsibility: production preparation realizes one visible roster, seats only visible identities, and returns the DLB-0 `TroubleBrewingIntermediateSetup` as the primary setup result.
- authoritative state owner(s): preset/setup owns visible-roster realization and seating; no Drunk seat is canonical in DLB-1. Current committed runtime remains temporarily supplied by a narrow compatibility projection only.
- narrow typed seam: selected preset + deterministic transitional `drunk_as_options` choice -> visible roster -> shown-seat assignments -> `TroubleBrewingIntermediateSetup`.
- compatibility seam: intermediate setup + the transient added Townsfolk choice -> old `TroubleBrewingSetupDealPlan` shape for current App/history consumers, by treating that added Townsfolk as the fallback Drunk.
- keep/extract: keep one shared seating optimizer in `TroubleBrewingSetupDealPlanner`; add a visible-roster entry point rather than duplicating the rotation algorithm. Keep generic `SetupShownIdentityCommitter` unchanged because No Greater Joy still owns that generic contract.
- reason: DLB-2 requires every dealt Townsfolk seat to be a peer candidate. Therefore seating must happen over visible roles, not over an already-special actual Drunk token.

## 2. Live fan-out

Current live production path before DLB-1:

```text
preset selection
-> TroubleBrewingShownIdentityPolicySource
-> SetupShownIdentityCommitter
-> selectedDrunkShownRole
-> TroubleBrewingSetupPresetSelection
-> TroubleBrewingSetupDealPlanner(actual Drunk token)
-> TroubleBrewingPreparedSetup.selection + dealPlan
-> App resolver/cards/recommendation/prewarm
-> rotation history + CommittedClocktowerSetup
```

Search confirms:

- `TroubleBrewingShownIdentityPolicySource` has one production caller: `TroubleBrewingProductionSetupPreparer`; other callers are tests.
- `TroubleBrewingSetupDealPlanner.plan` has one production caller: the same preparer; other callers are tests.
- `TroubleBrewingPreparedSetup.selection` is consumed in production only by `TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup`.
- `TroubleBrewingPreparedSetup.dealPlan` is consumed by App plus the rotation-record factory.
- DLB-0 `TroubleBrewingIntermediateSetup` currently has no production caller.

The old `TroubleBrewingSetupPresetSelector` / `selectedDrunkShownRole` path remains legacy/dead relative to current production selection. DLB-1 must not restore it as the live authority.

## 3. Target production flow

```text
validated preset pool
-> generic preset diversity selection                 [unchanged]
-> deterministic transitional added Townsfolk choice
-> TroubleBrewingVisibleRosterRealizer
-> TroubleBrewingSetupDealPlanner.planVisibleRoster
-> TroubleBrewingIntermediateSetup                   [new primary result]
-> temporary compatibility projection
     added Townsfolk seat -> actual Drunk, shown Townsfolk
     every other seat -> actual == shown
-> current App / history / committed setup consumers [temporary until DLB-4]
```

### Important ownership rule

The transitional added Townsfolk choice may be needed by the compatibility bridge, but it must **not** be stored in `TroubleBrewingIntermediateSetup`. Once the intermediate setup exists, the added Townsfolk has no privileged status in the new DLB contract.

The compatibility projection is not a canonical Drunk decision and must not be consumed by DLB-2 candidate generation or DLB-3 SDE ranking.

## 4. Deterministic visible-roster option selection

Production must stop using `SetupShownIdentityCommitter` to represent a Drunk shown-identity commitment.

Introduce a Trouble-Brewing-specific transitional option selector for the extra visible Townsfolk. To avoid unnecessary seeded-output churn during this migration, it should reproduce the former production deterministic draw for the same selected candidate/preset/seed, while changing the semantic name from “Drunk shown role” to “added visible Townsfolk”.

This compatibility of the **draw** is temporary migration behavior, not permanent Drunk ownership.

## 5. Visible seating

Refactor the existing seating optimizer so both legacy and new entry points reuse one private seat-assignment core.

New production entry point consumes:

- dataset/schema/preset provenance;
- `TroubleBrewingVisibleRoster`;
- ordered player names;
- game seed;
- recent player starting-identity history.

Candidate tokens are visible identities:

- every `visibleRoster.townsfolkRoleIds` token is category `TOWNSFOLK`, including the transitional added Townsfolk;
- visible outsiders/minions/demon keep their visible categories;
- there is no actual Drunk token in this seating step.

Output is `List<TroubleBrewingShownSeatAssignment>`.

This prevents the old hidden Drunk seat from influencing pre-Drunk player-category rotation.

## 6. Prepared setup contract

Change current production `TroubleBrewingPreparedSetup` to expose:

- selected `preset`;
- primary `intermediateSetup`;
- explicitly temporary `compatibilityDealPlan`.

It must no longer expose a live `TroubleBrewingSetupPresetSelection(selectedDrunkShownRole)`.

Current App and completion/history consumers move from `.dealPlan` to `.compatibilityDealPlan` only as a temporary bridge. DLB-4 will replace that bridge with the canonical Drunk commit.

## 7. Compatibility projection

Add a pure compatibility adapter:

```text
intermediate setup
+ selected preset
+ transient addedVisibleTownsfolkRoleId?
-> legacy TroubleBrewingSetupDealPlan
```

For a Drunk preset:

- the seat whose shown identity equals the transient added Townsfolk becomes `actualRoleId = "drunk"`;
- that seat retains `shownRoleId = added Townsfolk`;
- all other seats use `actualRoleId == shownRoleId`;
- final actual role multiset must exactly equal the preset's old actual role multiset.

For a non-Drunk preset, all actual/shown identities remain identical.

This proves the required fallback equivalence without putting the fallback seat into the new intermediate contract.

## 8. Persistence / history boundary

DLB-1 does **not** migrate rotation/completion persistence yet.

`TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup` moves to the new prepared contract but may still serialize the compatibility-finalized actual/shown result, including the historical `selectedDrunkShownRole` field, until DLB-6 deliberately retires that persisted meaning.

Emergency Recovery remains finalized-game-only and is not changed.

## 9. Test boundary

Add a new owning DLB-1 typed test before production implementation.

Required durable behavior:

1. production preparation of a Drunk preset returns an intermediate setup with:
   - `hasDrunk = true`;
   - no visible literal `drunk`;
   - one extra visible Townsfolk from `drunk_as_options`;
   - shown-seat assignments exactly covering the visible roster;
2. no Drunk seat or actual-role assignment exists in the intermediate contract;
3. compatibility projection produces exactly one actual Drunk and preserves its visible shown Townsfolk;
4. final compatibility actual-role multiset equals the original preset actual-role multiset;
5. non-Drunk presets preserve actual==shown behavior;
6. current player-rotation seating remains deterministic, but the extra visible Townsfolk is treated as Townsfolk before Drunk commitment.

Because this is a changed stable production setup contract, an executable test-first RED is required.

## 10. Intended production allowlist

Expected production changes:

- `TroubleBrewingProductionSetupPreparer.kt`
- `TroubleBrewingSetupDealPlanner.kt`
- `TroubleBrewingIntermediateSetup.kt` or one narrow visible-roster selector file
- one narrow compatibility deal-plan adapter
- `TroubleBrewingSetupRotationRecordFactory.kt`
- `CampBoardGameHostApp.kt` only for the explicit `compatibilityDealPlan` field rename/wiring

Expected tests:

- new DLB-1 typed contract test;
- update existing preparer/deal/rotation tests only where the production contract intentionally changed.

Global workflow docs changed on this branch solely to record the user's 2026-09-29 standing merge authorization.

## 11. Explicit NO-GO

- no DLB-2 legal Drunk candidate enumeration;
- no hypothetical projector;
- no SDE Drunk recommendation;
- no automatic Beginner authority;
- no Experienced “Choose the Drunk” UI;
- no canonical Drunk commit yet;
- no Red Herring / Poisoner / Demon-bluff sequencing changes;
- no Recovery migration;
- no broad App/Host decomposition;
- no removal of legacy history fields outside what DLB-1 requires;
- no change to `BEGINNER_CONSERVATIVE_V1`.
