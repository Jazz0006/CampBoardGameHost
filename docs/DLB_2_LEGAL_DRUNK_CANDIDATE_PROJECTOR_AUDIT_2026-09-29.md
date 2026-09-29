# DLB-2 Legal Drunk Candidate Domain + Hypothetical Projector Audit — 2026-09-29

> Baseline: `main@608dcfac898863464e4dd14235e53a2bba4c0f25`
> Branch: `dlb-2-legal-drunk-candidate-projector`
> Scope: rules/setup-owned legal Drunk-seat enumeration plus a pure hypothetical effective-setup projector.
> Authority: `docs/DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`.

## 1. Architecture pre-flight

- current owner: `TroubleBrewingIntermediateSetup` owns the immutable post-visible-roster/post-seat state; DLB-1 still exposes `compatibilityDealPlan` only for current runtime/history compatibility.
- proposed responsibility: derive the complete rules-legal Drunk-seat domain from the intermediate setup and project each legal candidate into an immutable hypothetical `GameState`.
- authoritative state owner(s): setup/rules own legality; the projector owns no mutable truth; canonical session/setup remains unchanged until DLB-4.
- narrow typed seam:
  `TroubleBrewingIntermediateSetup -> List<TroubleBrewingDrunkCandidate> -> TroubleBrewingDrunkHypotheticalSetup`.
- keep/extract: add one cohesive pure DLB-2 owner under `clocktower/setup`; do not wire App/Host/SDE yet.
- reason: DLB-3 needs already-legal, already-materialized hypothetical states. DLB-2 must establish that domain independently of policy, UI, compatibility finalization, and runtime session mutation.

## 2. Legal candidate contract

When `intermediateSetup.visibleRoster.hasDrunk == true`:

- every shown seat whose `shownRoleId` is in `visibleRoster.townsfolkRoleIds` is legal;
- no Outsider, Minion or Demon seat is legal;
- the transitional Townsfolk added from `drunk_as_options` has no privileged status;
- candidates are returned in canonical seat order;
- candidate identity contains only stable seat/player/shown-role facts.

When `hasDrunk == false`, the legal candidate set is empty.

The domain must not consult:

- `compatibilityDealPlan`;
- `selectedDrunkShownRole`;
- SDE scores/policy;
- setup history;
- UI state;
- session state.

This is rules legality, not recommendation.

## 3. Hypothetical projection contract

For one legal candidate, project a pristine Trouble Brewing `GameState` from the intermediate setup.

Candidate seat:

```text
actualRole      = canonical Drunk RoleId
actualType      = OUTSIDER
actualAlignment = GOOD
shownRole       = original dealt Townsfolk RoleId
alive           = true
poisoned        = false
```

Every other seat:

```text
actualRole      = its original dealt shown role
actualType      = type of that shown role
actualAlignment = alignment implied by that shown role's team
shownRole       = its original dealt shown role
alive           = true
poisoned        = false
```

Therefore selecting an original preset Townsfolk as Drunk is allowed: the transitional added visible Townsfolk remains an actual Townsfolk in that hypothetical world. DLB-2 must not force the old preset actual-role multiset. Only choosing the transitional added Townsfolk reproduces the DLB-1 compatibility projection.

The projector:

- requires the candidate to belong to the current legal domain;
- resolves role IDs/types through the validated character registry;
- requires canonical Drunk to be an Outsider;
- is deterministic and side-effect free;
- returns new immutable values only;
- never mutates `ClocktowerGameSession`, `CommittedClocktowerSetup`, App state, history, or Recovery.

## 4. Output type

Introduce:

- `TroubleBrewingDrunkCandidate(seat, playerName, shownRoleId)`;
- `TroubleBrewingDrunkHypotheticalSetup(candidate, gameState)`;
- one pure owner that enumerates and projects.

The projected `GameState.script` is Trouble Brewing's recommendation script ID and `seed` is the intermediate setup's game seed.

This output is intentionally close to existing exact/SDE machinery, but DLB-2 does not yet create an SDE candidate, DecisionTrace or policy ordering.

## 5. Fan-out / ownership boundary

DLB-2 production code should be additive and have no live runtime caller yet.

Expected existing dependencies:

- `TroubleBrewingIntermediateSetup`;
- `ClocktowerCharacterRegistry`;
- domain `GameState / PlayerState / RoleId / Alignment / CharacterType`;
- Trouble Brewing script ID mapping.

Explicitly do **not** modify:

- `TroubleBrewingCompatibilityDealPlanAdapter`;
- `CampBoardGameHostApp`;
- `ClocktowerHostScreen`;
- session mutation;
- persistence/history;
- `BEGINNER_CONSERVATIVE_V1`;
- generic setup-effect/transaction owners;
- Host `dynamicStorytellerState()`.

## 6. Test boundary

Add one owning DLB-2 typed test.

Required durable contracts:

1. Drunk-bearing intermediate setup returns every and only dealt Townsfolk seat;
2. candidate order is stable seat order;
3. the transitional added Townsfolk is a peer candidate, not the sole/default candidate;
4. projecting every legal candidate makes exactly that seat:
   - actual Drunk,
   - actual Outsider,
   - Good,
   - while preserving its original shown Townsfolk;
5. all other seats remain actual==shown with correct type/alignment;
6. selecting a non-transitional Townsfolk leaves the transitional Townsfolk as an actual Townsfolk;
7. non-Drunk setup yields no legal candidates;
8. projection rejects a non-legal/non-Townsfolk candidate;
9. repeated projection does not mutate the intermediate input and produces equal results.

This is a new stable rules/setup contract, so test-first RED is required.

## 7. Intended allowlist

New files only for the executable DLB-2 slice:

- `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDrunkCandidateProjection.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingDrunkCandidateProjectionTest.kt`
- this audit document

No existing production file should need modification in DLB-2.

## 8. Explicit NO-GO

- no SDE ranking or shadow execution yet;
- no DecisionTrace/replay integration yet;
- no automatic or manual Drunk choice;
- no canonical commit;
- no compatibility bridge migration/removal;
- no App/Host/UI changes;
- no first-night sequencing changes;
- no persistence/Recovery changes;
- no policy V1 change;
- no broad decomposition.
