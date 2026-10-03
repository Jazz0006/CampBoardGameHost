# TBGS-2C Setup Coordination Snapshot Context Audit — 2026-10-03

> Repository: `Jazz0006/CampBoardGameHost`  
> Baseline audited: `main@22d18fdcb51998716e29569e5263e6ca45511884`  
> Status: **IMPLEMENTATION COMPLETE / T4 ACCEPTANCE PENDING**

## 1. Decision

TBGS-2C should migrate only the **Trouble Brewing setup recommendation mechanical/rules base context**. It must not turn the entire `SetupCoordinationRequest` into snapshot state.

Target production path:

```text
canonical committed/runtime owner
    -> TroubleBrewingGameSnapshotV1
    -> TroubleBrewingSetupRecommendationDecisionContext
    -> SetupCoordinationRequest
    -> SetupRecommendationService
```

The typed context owns the snapshot-backed mechanical compatibility projection and rules catalog. Locks and cross-game history stay explicit request inputs.

## 2. Current production consumer

`ClocktowerHostScreen` currently constructs the visible setup recommendation request from:

```text
recommendationCards.toClocktowerGameState(script, seed, poisonTarget)
clocktowerRoleDefinitionsForScript(script)
lockedRecommendationDecisions + committed Red Herring / Demon bluffs
setupHistory
```

The resulting `SetupRecommendationService.ConstrainedResult` is real production output shown on the Storyteller screen. This is therefore a production-output migration, not a shadow-only cleanup.

The initial Trouble Brewing prewarm already starts from `committedSetup.gameState`, which is canonical rather than UI reconstruction, but it should converge on the same typed context so prewarm/live requests use one semantic base.

## 3. Shadow / diagnostic classification

`evaluateSetupDemonBluffShadow()` and `evaluateSetupRedHerringShadow()` are currently called by tests only; there is no production runtime call site. Their adapters explicitly preserve the visible legacy setup result and remain non-authoritative diagnostics.

TBGS-2C therefore does **not** migrate these shadow APIs. Existing shadow parity assertions remain regression coverage for `SetupCoordinationRequest.game` semantics.

## 4. Required mechanical facts

Setup candidate generation/evaluation uses:

- actual role, alignment and character type;
- shown role for Drunk/Investigator setup information;
- alive state;
- poison state, including functioning Empath and poisoned impaired-pair sources;
- seat topology and player count;
- game seed;
- script role definitions.

`TroubleBrewingGameSnapshotV1` already carries all required per-seat mechanical facts plus seed/script. The validated character registry can supply the rules-owned `RoleDefinition` list exactly as TBGS-2B already demonstrated.

No snapshot schema expansion is needed.

## 5. Player names are not a blocker

`PlayerState.name` participates in Kotlin data-class equality but is not consumed by setup candidate generation, setup evaluation or historical clue signatures. Existing `ExpertRecommendationReviewTest` explicitly proves recommendation decisions are invariant under player renaming.

The typed compatibility projection may therefore use deterministic `Seat N` names. Tests should compare recommendation/candidate semantics rather than raw legacy `GameState` equality across presentation names.

## 6. Snapshot positions

The same context must support two finalized boundaries:

1. `SETUP_COMMITTED` from `TroubleBrewingGameSnapshotProjector.fromCommitted()` for initial background prewarm;
2. `RUNTIME + FIRST_NIGHT + round 1` from canonical `ClocktowerGameSession.toGameSnapshot(...)` for the visible pre-first-night request.

Reject `SETUP_PRECOMMIT`, later rounds and non-first-night runtime positions.

Unlike `TroubleBrewingFirstNightPairDecisionContext`, **do not normalize poison away**. Preserve the snapshot poison field exactly.

## 7. Explicit non-snapshot coordination inputs

Keep these outside `TroubleBrewingGameSnapshotV1` and outside the typed mechanical base:

- mutable recommendation locks selected in the Storyteller UI;
- already committed Red Herring / Demon-bluff decisions passed as locks;
- `CrossGameHistory` enrichment.

They are decision coordination/history inputs, not canonical mechanical grimoire truth. This audit does not authorize a generic setup-effect owner or history merger.

## 8. Exact TBGS-2C implementation scope

TBGS-2C may:

1. add `TroubleBrewingSetupRecommendationDecisionContext` plus a pure builder from TB snapshot + validated character registry;
2. preserve actual/shown/alive/poison/seed semantics in a compatibility `GameState` derived only from the immutable snapshot;
3. derive role definitions from the rules registry rather than presentation role lists;
4. build the initial prewarm context from the finalized committed setup snapshot;
5. build the live TB pre-first-night context from the canonical session runtime snapshot;
6. make the Trouble Brewing Host setup request consume `context.recommendationGameState` and `context.roleDefinitions` instead of `PlayerCard.toClocktowerGameState()` / presentation role definitions;
7. retain the existing non-TB compatibility path unchanged.

TBGS-2C must not:

- change `SetupRecommendationService` policy, candidate ordering, scoring, weighted selection or history cooldown;
- move locks/history into the snapshot;
- change Red Herring / Demon-bluff commitment timing or ownership;
- change SDE setup shadow APIs or make them production authority;
- migrate DynamicGameState consumers;
- expand `TroubleBrewingGameSnapshotV1`;
- touch Recovery, A3, R3 or broad Host decomposition.

## 9. Test gate

### T0 / focused contract evidence

Add a typed-context test proving:

- `SETUP_COMMITTED` and runtime first-night round-1 snapshots are accepted;
- precommit/later runtime snapshots fail closed;
- actual/shown/alive/poison/seed are preserved;
- rules-registry role definitions are equivalent to the existing TB adapter;
- setup recommendation candidate/plan output from the context-derived base matches the legacy TB path for representative fixtures, including poison-sensitive coverage and locked decisions/history where useful.

Retain/run:

- `SetupRecommendationServiceTest` / setup migration coverage;
- `SetupRecommendationShownIdentityOwnershipTest`;
- `TroubleBrewingSetupRecommendationPrewarmCoordinatorTest`;
- `TroubleBrewingSetupRecommendationRevealCoordinatorTest`;
- existing Red Herring / Demon bluff shadow integration coverage.

### T1 / remote acceptance

Run Android FAST at the logical checkpoint plus normal static/diff gates. Because this changes the production setup recommendation input authority, final T4 must execute Android `:app:testFull + :app:assembleDebug`, ASP contracts, Real Clingo and R2 before merge.

## 10. Implementation checkpoint

Production implementation is complete at exact head `ac69be12f429b462ab2a8f800a3661699e522312`.

Ordinary acceptance at that exact head:

- CI #3662 — GREEN, including Android FAST;
- R2 #3377 — GREEN;
- PR #199 — mergeable, zero unresolved review threads.

Implementation preserves the historical setup-recommendation role order while deriving each role definition from the validated rules registry. This keeps Demon-bluff list presentation parity with the pre-TBGS production path without restoring presentation catalog ownership.

Final acceptance still requires a docs-only `[full-ci]` checkpoint and T4:

- Android `:app:testFull + :app:assembleDebug`;
- ASP contract tests;
- Real Clingo cross-validation;
- R2 main-thread boundary.

## 11. Next after TBGS-2C

If TBGS-2C is accepted, re-audit the remaining `DynamicGameState` production consumers **one typed decision family at a time**. Do not automatically broaden into Mayor, succession and special registration together.
