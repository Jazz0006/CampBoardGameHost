# TBGS-2D DynamicGameState Consumer Selection Audit — 2026-10-03

> Repository: `Jazz0006/CampBoardGameHost`  
> Baseline audited: `main@5b32640d26f39dcf137bcff672686f0a6ee801c4`  
> Status: **IMPLEMENTATION GREEN / FINAL T4 PENDING**  
> Selected family: **TBGS-2D — Demon succession typed recommendation context**  
> Implementation checkpoint: `b62980929c143439e5b65836e251c746eda14afc`; Android FAST / R2 #3428 GREEN

## 1. Decision

After TBGS-2A / 2B / 2C, the remaining `ClocktowerJudgeScreen.dynamicStorytellerState()` consumers are not one coherent migration surface.

They split into four production concerns:

1. cross-cutting balance/style enrichment;
2. Spy/Recluse special-registration recommendation;
3. Mayor death-redirection recommendation;
4. Demon succession recommendation.

The next bounded decision-family migration should be **Demon succession**.

Reason: it has the smallest required state surface, an already-existing rules-owned legal domain, and no need for protection state or registration/misinformation ledgers. It can therefore prove the next runtime typed-context pattern without broadening into Mayor, registration, generic history cutover, or a universal DynamicGameState replacement.

## 2. Current legacy aggregate

`ClocktowerJudgeScreen.dynamicStorytellerState()` currently reconstructs:

```text
PlayerCard list
  -> cards.toClocktowerGameState(...)
  -> phase / round
  -> monk protection
  -> Virgin / Slayer / Artist spent seats
  -> UI-event-derived information pressure
  -> UI-event-title-derived registration ledger
  -> GameBalanceEvaluator
  -> DynamicGameState
```

That aggregate is then consumed by:

- `GameBalanceEvaluator.adjustInformationStyle` and automatic information selection via `evilAdvantage`;
- `RegistrationPolicy`;
- `MayorRedirectRecommender`;
- `DemonSuccessorRecommender`.

The aggregate therefore mixes canonical mechanical truth, rules legality, UI event history, ability-use bookkeeping and strategic enrichment. It should be retired incrementally, not replaced with another God object.

## 3. Canonical/history owner findings

### 3.1 Mechanical truth

`ClocktowerGameSession -> GameSnapshot -> TroubleBrewingGameSnapshotV1` already owns the mechanical facts needed by Demon succession recommendation:

- actual role / alignment / character type;
- alive/dead state;
- poison state;
- seat identity;
- runtime phase / round;
- seed and revisions.

No TB snapshot schema expansion is required.

### 3.2 Decision-history projections exist but production cutover is incomplete

`DecisionHistoryRepository.project()` is already the proper typed projector for:

- `pressureBySeat`;
- `misinformationLedger`;
- `registrationLedgerBySeat`.

However, the current production `ClocktowerRecommendationCoordinator.appendDecision()` / status transition APIs have no production call sites. Therefore the session `decisionHistory` must **not** yet be treated as a complete replacement for current UI-derived recommendation history.

TBGS-2D must preserve the current production enrichment semantics rather than silently replacing them with empty or incomplete session history.

### 3.3 Spent-ability bookkeeping is not reconstructable from ActionFactTimeline

The canonical action timeline currently models poison, protect, attack, execution, death, role change and phase advance. It does not encode Virgin / Slayer / Artist spent state.

Those spent flags remain explicit App/Recovery mechanics inputs for now.

This is acceptable enrichment debt and must not trigger a broad session/recovery rewrite inside TBGS-2D.

## 4. Consumer comparison

### 4.1 Demon succession — SELECTED

Current recommender consumes only:

- mechanical `GameState`;
- per-seat information pressure;
- `evilAdvantage`.

It does not directly consume:

- protected seats;
- spent seats except indirectly through balance calculation;
- registration ledger;
- misinformation ledger;
- public balance hint.

Its legality is already owned elsewhere by:

```text
DemonSuccessionSemantics
  -> resolveTroubleBrewingImpSelfKillSuccession(...)
  -> DemonSuccessionResolution.None / Choice / Forced
```

This makes it the narrowest next typed decision family.

### 4.2 Mayor death redirection — later

Mayor recommendation additionally consumes:

- Monk protection state;
- spent ability seats;
- per-seat information pressure;
- public balance hint;
- current alive/dead and role metadata.

It also independently enumerates redirect candidates while the night host projection already exposes a rules-filtered redirect domain.

This is a valid later migration, but it has more coordination inputs and should follow the succession pattern.

### 4.3 Special registration — later

Registration recommendation additionally depends on:

- allowed registration roles;
- interaction-time effective registration subject;
- registration history;
- misinformation consequence inputs;
- evil-advantage adjustment;
- outcome-specific pressure/discussion values.

The current Host also derives registration history from localized UI event titles while a typed decision-history projector exists but is not yet production-complete.

This family should not be the next migration because it would entangle state migration with history producer/cutover questions.

### 4.4 Cross-cutting balance/style enrichment — explicit dependency, not the selected family

`evilAdvantage` also feeds Artist and night information automatic selection.

This is a real remaining `DynamicGameState` consumer, but it is strategic enrichment rather than a single decision family.

TBGS-2D may recompute the same balance value inside the succession context from explicitly supplied legacy-equivalent enrichment, but it must not simultaneously migrate Artist/night information balance consumers. A later focused slice can retire the cross-cutting balance dependency after the decision-family pattern is proven.

## 5. Important legality ownership defect

The current Demon succession path duplicates legality:

```text
Night Host rules path
  -> DemonSuccessionResolution
  -> demonSuccessorTargetSeats

DemonSuccessorRecommender
  -> independently filters alive Minions
  -> independently applies Scarlet Woman mandatory logic

UI
  -> filters recommender output by demonSuccessorTargetSeats
```

This violates the target boundary:

> Game Engine / rules own legality; Recommendation ranks an already-legal domain.

TBGS-2D should consume the existing `DemonSuccessionResolution` directly. The recommender must stop recreating the legal target domain, and the UI must stop post-filtering recommendations after selection.

This is an ownership convergence, not a new rules policy.

## 6. Proposed TBGS-2D typed context

Introduce a decision-specific context, provisionally:

`TroubleBrewingDemonSuccessorDecisionContext`

Required correctness context:

- runtime `TroubleBrewingGameSnapshotV1`, NIGHT phase;
- rules-owned `DemonSuccessionResolution` for the current succession point.

Explicit enrichment context, preserving current production semantics:

- `playerInformationPressureBySeat`;
- spent-ability seats only for the existing balance calculation;
- derived `evilAdvantage` using the existing `GameBalanceEvaluator`.

The builder may derive a bounded compatibility `GameState` from the immutable TB snapshot while the existing scoring implementation still consumes PlayerState-style values. That compatibility projection is not authority and must not be built from `PlayerCard`.

Do not add player names to the TB snapshot. The succession recommender scores and outputs by seat/role; presentation names remain UI lookup data.

## 7. Exact TBGS-2D implementation scope

TBGS-2D may:

1. add the typed Demon succession context and pure builder;
2. project the mechanical game from canonical TB runtime snapshot;
3. accept the existing rules-owned `DemonSuccessionResolution` as the legal candidate domain;
4. preserve current pressure and spent-ability enrichment as explicit inputs;
5. derive the same balance assessment using `GameBalanceEvaluator`;
6. make `DemonSuccessorRecommender` rank only candidates supplied by the legal resolution;
7. preserve the existing role-suitability / pressure / global-balance scoring and warning semantics;
8. remove the Host-side post-selection `legalTargetSeats` filter for this recommendation path once the recommender consumes the legal domain;
9. retain existing NightCheckpoint / current-Demon / succession-commit ownership unchanged.

TBGS-2D must not:

- migrate Mayor redirection;
- migrate Spy/Recluse registration;
- switch production pressure/history to `DecisionHistoryRepository`;
- change Artist/night-information balance consumers;
- add Virgin/Slayer/Artist spent state to GameSnapshot/session;
- change Demon succession rules;
- change current-Demon identity or NightCheckpoint transaction ownership;
- expand `TroubleBrewingGameSnapshotV1`;
- touch Recovery, A3, R3, SDE policy, or `BEGINNER_CONSERVATIVE_V1`.

## 8. Test gate

Add typed-context / recommender coverage proving:

- only valid TB runtime NIGHT succession contexts are accepted;
- actual role/type/alignment, alive and poison semantics match the canonical snapshot;
- `DemonSuccessionResolution.None` yields no recommendations;
- `Forced` yields exactly the forced target and preserves the `scarlet-woman-mandatory` warning when applicable;
- `Choice` ranks exactly the supplied legal seats and cannot introduce an extra Minion;
- poisoned Scarlet Woman behavior follows the rules-provided resolution rather than recommender-side reimplementation;
- per-seat pressure and global-balance scoring remain parity-equivalent to the legacy DynamicGameState path for representative fixtures;
- Host recommendation options no longer disappear because an already-selected recommendation is filtered after ranking.

Retain / run:

- `DemonSuccessionSemanticsTest`;
- `TroubleBrewingCurrentDemonRegressionTest`;
- `DemonSuccessorRecommenderTest`;
- relevant Night host projection / checkpoint transaction tests.

Run Android FAST at the logical checkpoint. Final T4 should include Android `:app:testFull + :app:assembleDebug`, ASP contracts, Real Clingo and R2 before merge because this changes a production recommendation input/legality boundary.

## 9. Sequence after audit

```text
TBGS-2A pair precompute                 COMPLETE / ACCEPTED
-> TBGS-2B pair manual/publication      COMPLETE / ACCEPTED
-> TBGS-2C setup recommendation base    COMPLETE / ACCEPTED
-> TBGS-2D Demon succession context     IMPLEMENTATION GREEN / T4 PENDING
-> fresh re-audit AFTER TBGS-2D ACCEPTANCE
   -> Mayor OR special registration OR balance enrichment
   -> choose one bounded family/surface only
```

Do not assume the post-2D ordering in advance.

## 10. Implementation result before final T4

The implementation now follows the audited ownership boundary:

- `TroubleBrewingDemonSuccessorDecisionContext` is built from the canonical runtime TB snapshot plus rules-owned `DemonSuccessionResolution`;
- pressure and spent-ability inputs remain explicit legacy-equivalent enrichment and feed the existing `GameBalanceEvaluator`;
- `DemonSuccessorRecommender` no longer enumerates Minions or reimplements Scarlet Woman legality; it ranks only rules-supplied legal seats;
- the TB Host path no longer reconstructs succession mechanical truth from `PlayerCard.toClocktowerGameState()`;
- the UI no longer post-filters an already-ranked recommendation against `legalTargetSeats`;
- non-TB compatibility retains legacy enrichment but also consumes a rules-owned succession resolution;
- Mayor, special registration, cross-cutting balance/style ownership, history-producer cutover, Recovery and snapshot schema remain unchanged.

The tests-first correction also clarifies that `Choice(setOf(...))` defines the legal domain; a ranking result is not required to surface every legal seat in its final style winners. The required invariant is that every returned candidate is inside the rules-owned domain and no recommender-side mandatory Scarlet Woman rule is invented.
