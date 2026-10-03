# TBGS-2 Runtime Recommendation-State Migration Audit — 2026-10-03

> Repository: `Jazz0006/CampBoardGameHost`  
> Baseline audited: `main@da5e9947e7a2799d94720454b67fdb6de2ef54a3`  
> Status: **FOCUSED READ-ONLY AUDIT COMPLETE / TBGS-2A APPROVED**

## 1. Decision

TBGS-2 should proceed as an incremental consumer migration, not a Host rewrite.

The first migration slice is:

**TBGS-2A — Trouble Brewing first-night natural pair recommendation context**

Targeted production surface:

```text
Washerwoman / Librarian / Investigator
first-night natural pair candidate precompute
```

Current production input path:

```text
ClocktowerJudgeScreen PlayerCard list
    -> cards.toClocktowerGameState()
    -> TroubleBrewingFirstNightPrecomputeCoordinator<GameState, ...>
    -> ClocktowerRecommendationCoordinator.naturalPairCandidates(GameState)
    -> NaturalPairInformationCandidateGenerator
```

TBGS-2A target path:

```text
ClocktowerGameSession
    -> GameSnapshot
    -> TroubleBrewingGameSnapshotProjector.fromRuntime()
    -> TroubleBrewingFirstNightPairDecisionContextBuilder
    -> first-night precompute / naturalPairCandidates(context)
```

The context may carry a compatibility `GameState` derived purely from the immutable TB snapshot while the existing pair generator still consumes `GameState`. That compatibility projection is not authority and must not be built from `PlayerCard`.

## 2. Production recommendation consumers still on legacy projection paths

### 2.1 Setup coordination — production output, broad fan-out

`ClocktowerJudgeScreen` builds `SetupCoordinationRequest.game` from `recommendationCards.toClocktowerGameState()`.

This feeds real setup recommendations and automatic selection, including Red Herring and Demon bluffs. It also sits beside SDE shadow diagnostics.

Classification: **production-output consumer**.

Why not first: setup coordination spans several decision families, lock semantics, history, setup UI and SDE shadow parity. Migrating it first would violate the “one consumer family per slice” rule.

### 2.2 First-night natural pair precompute — production output, narrow and snapshot-complete

`ClocktowerJudgeScreen` builds the precompute request from `cards.toClocktowerGameState()`. The resulting candidate space is consumed by Washerwoman / Librarian / Investigator production information preparation.

Classification: **production-output consumer**.

Why first: its required mechanical facts are already represented by TB snapshot V1 — seat, actual/shown identity, alive, seed and runtime position. The canonical snapshot also carries poisoned state, but the historical natural-truth precompute intentionally ignored poison; TBGS-2A therefore retains poison in the snapshot while normalizing it away only in the typed context's compatibility candidate-space projection. It does not require history-pressure, spent-ability, protection or mutable registration ledgers.

### 2.3 Pair manual/publication adapter — production confirmation path

`ClocktowerFirstNightInformationRequest` and `ClocktowerPairManualAuthority` still reconstruct `GameState` from `PlayerCard` when validating pair selections/publication.

Classification: **production-output / confirmation consumer**.

Why not bundled into TBGS-2A: it is downstream confirmation authority with separate parity/manual-domain invariants. Migrate after the precompute seam proves the snapshot-backed pair context.

### 2.4 DynamicGameState family — production output, higher-context migration

`ClocktowerJudgeScreen.dynamicStorytellerState()` reconstructs `GameState` from cards and augments it with phase/round, spent abilities, protection, information-history pressure, registration history and balance.

Current production users include:

- special Spy/Recluse registration recommendations;
- Mayor death redirection recommendations;
- Demon succession recommendations.

Classification: **production-output consumers**.

Why later: TB snapshot V1 does not yet contain all enrichment inputs used by this adapter. The correct migration needs typed per-decision contexts and canonical/history owners, not expansion of the snapshot into a God object.

### 2.5 Unreliable number / categorical selectors

Chef / Empath / Fortune Teller and similar information surfaces use recommendation ranking for already-derived truthful values/options. Their score selectors are production output, but the selectors themselves do not all consume a reconstructed `GameState`.

Classification: **production recommendation output, but not all are direct TBGS state-projection consumers**.

They should be migrated only where a concrete upstream state reconstruction remains, not merely because they use recommendation code.

## 3. Shadow / replay / diagnostic paths

The following are not TBGS-2A production-output targets:

- SDE runtime shadow evaluation from `SdeHistoricalReplayInputFactory.captureFresh(session.toGameSnapshot(...))`;
- Red Herring / Demon bluff setup SDE shadow adapters;
- DecisionTrace / multi-policy replay;
- debug A4 identity prewarm and observation-cache rebuild paths that still create `GameSnapshot` from cards;
- diagnostic / parity telemetry that does not choose or publish the visible production result.

These may later benefit from canonical snapshot cleanup, but they must not be used to justify broadening TBGS-2A.

## 4. TBGS-2A exact scope

TBGS-2A may:

1. add one typed `TroubleBrewingFirstNightPairDecisionContext`;
2. add a pure builder from `TroubleBrewingGameSnapshotV1` plus the validated TB character registry;
3. preserve the existing natural-pair generator semantics by deriving any temporary compatibility `GameState` only inside the typed context boundary; that compatibility projection must normalize `poisoned=false` exactly as the historical first-night natural-pair precompute did, while the canonical snapshot retains the real poison fact;
4. change the TB first-night precompute coordinator request type from raw `GameState` to the typed context;
5. build that context in App/root from the canonical `ClocktowerGameSession.toGameSnapshot(...)` -> `TroubleBrewingGameSnapshotProjector.fromRuntime(...)` path;
6. make the TB production Judge-screen precompute consume that context instead of `PlayerCard.toClocktowerGameState()`;
7. retain non-TB compatibility behavior unchanged.

TBGS-2A must not migrate setup coordination, pair manual confirmation, DynamicGameState, Mayor, succession, registration, day-table rendering, Recovery or A4 in the same slice.

## 5. TBGS-2A invariants

- `TroubleBrewingGameSnapshotV1` remains immutable and read-only.
- `ClocktowerGameSession` remains the mutable mechanical authority.
- Candidate IDs, order, registrations, outcomes and visible recommendation behavior remain unchanged.
- No new ranking or evidence semantics.
- No `BEGINNER_CONSERVATIVE_V1` change.
- `DRUNK_ASSIGNMENT_SHADOW_V1` remains deferral-only.
- No C5/V2 policy work.
- No R3 transaction extraction.
- No generic setup-effect owner.
- No broad Host decomposition.

## 6. Test gate

### T0

Add a typed-context contract test that proves:

- only a runtime / first-night TB snapshot is accepted;
- actual/shown roles and alive state are reconstructed from snapshot semantics, while canonical poisoned state remains visible on the snapshot and is deliberately normalized away only for the natural-pair candidate-space compatibility projection;
- snapshot revisions remain available on the context;
- natural-pair candidate IDs/outcomes from the new context exactly match the existing GameState path for a representative TB fixture, including Drunk shown pair role and Spy/Recluse registration-sensitive coverage where applicable.

Run the existing `NaturalPairInformationCandidateGeneratorTest` and `TroubleBrewingFirstNightPrecomputeCoordinatorTest`.

### T1 logical checkpoint

Run `:app:testFast` plus the standard static/diff checks required by `TESTING_STRATEGY.md`.

### Remote acceptance

Latest logical checkpoint must pass GitHub CI/R2 before merge. No old-head waiting is required between micro-edits.

## 7. A3 presentation-catalog re-audit

The original reason to defer A3 was DLB setup/presentation fan-out instability. DLB is now COMPLETE / ACCEPTED, so that deferral condition is resolved.

Current fan-out is still broad: `clocktowerRolesForScript` and the presentation role lists are consumed by Host rendering, first-night adapters, localization, setup recommendation UI, domain role adapters and Recovery.

Conclusion:

**A3 is now READY as an independent maintenance slice, but it is not a TBGS-2 prerequisite and must not be bundled into TBGS-2A.**

A3 may later move the static presentation role catalog plus narrow access functions out of `CampBoardGameHostApp.kt`, while preserving:

- exact names/descriptions/membership/order;
- Trouble Brewing and No Greater Joy behavior;
- `BuiltInClocktowerRulesetCatalog` as rules authority;
- archive and Recovery role lookup behavior.

A3 must not invent catalog fallbacks, regenerate rules from presentation data, or widen private APIs merely to reduce App file size.

## 8. Planned order after TBGS-2A

Tentative next re-audit order, not pre-authorized implementation:

```text
TBGS-2A first-night natural-pair precompute
-> pair manual/publication consumer
-> setup coordination consumer
-> DynamicGameState consumers one typed decision family at a time
```

Re-audit after each accepted slice. Do not assume this ordering remains optimal if ownership or evidence changes.
