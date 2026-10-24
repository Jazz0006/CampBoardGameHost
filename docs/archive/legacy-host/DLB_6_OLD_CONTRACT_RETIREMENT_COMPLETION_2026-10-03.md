# DLB-6 — Old Contract Retirement Completion Audit

> Date: 2026-10-03  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `dlb-6-old-contract-retirement`  
> Draft PR: #195 — `DLB-6: retire old Drunk setup contracts`  
> Status: **COMPLETE / ACCEPTED — executable checkpoint `52c2e73ca455a62c31065ce0e6fca4edda8713ec`, CI #3639 GREEN, R2 #3358 GREEN**

## 1. Scope

DLB-6 retires the transitional Trouble Brewing contracts that existed only to bridge the old pre-seat Drunk model into the late-binding DLB model.

This slice does **not** change the accepted `DRUNK_ASSIGNMENT_Q04_V1` ordering rule. It removes obsolete ownership and persistence around that rule after Beginner production cutover was accepted.

## 2. Accepted checkpoints

| Slice | Checkpoint | Acceptance |
| --- | --- | --- |
| DLB-6A compatibility DealPlan retirement | `343b11a36494b920539f9a4f24da922698b56d07` | CI #3636 / R2 #3355 GREEN |
| DLB-6B pre-seat Drunk contract retirement | `2929e1d955e06b3a207f51ac3f2f74be4fe93bbb` | CI #3637 / R2 #3356 GREEN |
| DLB-6C persisted Drunk shown-field retirement | `248899e7ff7bdcc4b4f1066b1b4ed5d390d1044b` | CI #3638 / R2 #3357 GREEN |
| DLB-6D compatibility candidate ownership retirement | `52c2e73ca455a62c31065ce0e6fca4edda8713ec` | CI #3639 / R2 #3358 GREEN |

## 3. Retired runtime contracts

The production/runtime surface no longer contains:

- `TroubleBrewingSetupPresetSelection`;
- `TroubleBrewingSetupDealPlan` / `TroubleBrewingSetupDealAssignment`;
- `TroubleBrewingSetupPresetSelector`;
- `TroubleBrewingSetupPresetRotationScorer`;
- `TroubleBrewingCommittedSetupAdapter`;
- `TroubleBrewingCompatibilityDealPlanAdapter`;
- `TroubleBrewingPreparedSetup.compatibilityDealPlan`;
- `TroubleBrewingPreparedSetup.compatibilityConfirmedDrunkCandidate`;
- `TroubleBrewingDrunkSelectionRoute.CompatibilityImmediate`;
- current-model `TroubleBrewingSetupRotationRecord.selectedDrunkShownRole`.

The live precommit flow is now:

```text
generic preset selection
-> deterministic visible-roster option realization
-> visible identities seated
-> rules-owned legal Drunk candidate domain
-> Q04 production policy derives its bounded baseline from preset + seated visible roster
-> Beginner automatic candidate / Experienced manual candidate
-> canonical Drunk commit
```

No pre-seat Drunk seat is committed by setup generation.

## 4. Q04 fallback ownership after retirement

`DRUNK_ASSIGNMENT_Q04_V1` remains the same evidence-bounded policy:

- only Empath -> Monk may override under the accepted Q04 topology predicate;
- otherwise the policy preserves the pre-existing deterministic baseline.

DLB-6D moves the baseline **ownership**, not its behavior. The production adapter reconstructs that baseline from:

```text
intermediate.visibleRoster.townsfolkRoleIds - preset.townsfolk
```

The visible-roster realizer guarantees that a Drunk preset adds exactly one `drunk_as_options` Townsfolk to the preset Townsfolk set, so this resolves the same seed-determined added visible Townsfolk that the transitional compatibility candidate represented.

The word `compatibilityCandidate` remains inside the already-versioned Q04 V1 policy/replay vocabulary. It is historical policy-contract terminology only; it is no longer setup state, persistence state, router fallback authority, or a pre-seat Drunk commitment.

## 5. Persistence retirement

`selectedDrunkShownRole` is no longer part of the current rotation/completion model.

Current writes use schema/version 3:

- active-game Trouble Brewing completion persistence writes v3 without `selectedDrunkShownRole`;
- setup rotation history writes v3 without `selectedDrunkShownRole`;
- canonical shown identity is carried by `playerStartingIdentities`.

A bounded one-step v2 reader remains on these two historical product-history surfaces. It reads the old key only to validate migration consistency against canonical starting identities and does not reintroduce the field into the current model.

This does not change current-only Emergency Recovery. Recovery still has no legacy-format migration requirement.

## 6. Producer / consumer audit

Post-DLB-6 executable source search:

```text
compatibilityConfirmedDrunkCandidate   0
CompatibilityImmediate                0
TroubleBrewingSetupPresetSelection    0
TroubleBrewingSetupDealAssignment     0
TroubleBrewingSetupPresetSelector     0
TroubleBrewingSetupPresetRotationScorer 0
TroubleBrewingCommittedSetupAdapter   0
```

`selectedDrunkShownRole` remains only as the literal v2 migration key in the two bounded persistence readers.

## 7. Acceptance conclusion

DLB-6 requirements are satisfied:

- obsolete pre-seat Drunk production ownership is retired;
- old score/history coupling is retired;
- visible-roster seating remains the production precommit path;
- Q04 Beginner production authority remains intact;
- canonical commit remains the only final Drunk truth;
- current persistence no longer owns the obsolete shown-role field;
- old source-shape tests were removed or replaced by live-seam tests;
- producer/consumer searches show no remaining runtime compatibility ownership.

**DLB-6 is COMPLETE / ACCEPTED.**

## 8. Next route

```text
DLB-6 COMPLETE / ACCEPTED
-> DLB-7 acceptance NEXT
-> TBGS-2 incremental runtime recommendation-state migration only after DLB-7
```

DLB-7 must verify the complete accepted behavior matrix from the authoritative route, including legal visible rosters, all legal Townsfolk Drunk candidates, canonical single-Drunk commit, shown identity preservation, Fortune Teller / Red Herring separation, poison revision invalidation, DecisionTrace/replay correlation, current Recovery behavior, and non-Drunk behavioral stability.
