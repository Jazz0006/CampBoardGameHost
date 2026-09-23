# SDE-2D5F Traveller Model Boundary Audit — 2026-09-22

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-2d5-calibration-policy-evidence`  
> PR: #150 (**must remain draft**)  
> Trigger: primary-verified `goldcand-ben-04 / A Fond Farewell`

## 1. Decision

**Do not add Traveller support inside the current B4 evidence-calibration slice.**

`A Fond Farewell` should remain primary-verified evidence plus a requirements fixture, but its legal-counterfactual execution is deferred until Traveller support is deliberately introduced as a separate production capability.

This is not because the observed Night-1 state is ambiguous. The blocker is architectural: the current canonical setup/world model assumes every seated participant belongs to the base Townsfolk / Outsider / Minion / Demon setup quota.

Adding Travellers is therefore not a small evidence-fixture adaptation.

## 2. Production boundary findings

### 2.1 `GameState` has no Traveller character type or setup-participation distinction

`clocktower/domain/GameState.kt` currently defines:

```text
CharacterType =
    TOWNSFOLK
    OUTSIDER
    MINION
    DEMON
```

`PlayerState` stores one `actualType`, one `actualAlignment`, and a seat, but has no independent notion such as:

- base-setup participant vs Traveller;
- Traveller public alignment;
- Traveller excluded-from-base-quota status.

Blindly appending `TRAVELLER` to the enum is not enough, because several consumers assume the existing four values form an exhaustive setup partition.

### 2.2 Trouble Brewing built-in script/catalog contains only the 22 base characters

`app/src/main/assets/scripts/trouble_brewing.json` contains the ordinary Trouble Brewing script roles and no Traveller definitions.

`BuiltInClocktowerRulesetCatalog` builds the Trouble Brewing registry from the base role definitions and that asset. The five TB Travellers in `A Fond Farewell` therefore have no current canonical role-definition source in this ruleset path.

### 2.3 Strategic topology assumes every seat belongs to the base setup

`TroubleBrewingStrategicTopologyDomain.enumerate(playerCount, profile)` treats `1..playerCount` as the complete base-player seat set and chooses:

- exactly one Demon seat;
- the profile's Minion seats;
- every remaining seat as a Good base seat.

There is no representation for a seated Good/Evil Traveller that:

- is visible in the circle;
- affects adjacency;
- is not part of Townsfolk/Outsider/Minion/Demon setup quotas.

Passing `20` for A Fond would therefore mean “20 base players”, which is false. Passing `15` would remove five physically seated players and corrupt adjacency.

### 2.4 Setup witness feasibility also assumes the four base types exhaust all seats

`TroubleBrewingTopologySetupWitnessEvaluator` constructs per-seat allowed types from exactly:

- DEMON;
- MINION;
- TOWNSFOLK / OUTSIDER.

Its remaining-role flow quotas are also exactly those four `CharacterType` values.

Traveller seats cannot be inserted honestly without changing this setup ownership model.

### 2.5 Player-count knowledge currently means all formal seats

`A4PlayerKnowledgeFactory` automatically publishes `InformationProposition.PlayerCount(input.playerCount)` from the formal state.

The topology evaluator then uses that public player count directly to select `TroubleBrewingSetupProfiles.legalProfiles(playerCount)`.

With Travellers, at least two quantities become semantically distinct:

- total seated participants;
- base setup player count used for Townsfolk/Outsider/Minion/Demon counts.

The current contract has only one.

### 2.6 Chef adjacency itself is already generic over the seated list

`FixedInformationEvaluator.chefEvilPairs` sorts the supplied `PlayerState` list by seat and counts Evil-Evil adjacency around the full circle.

That is a useful positive finding: the arithmetic does not intrinsically need a Traveller-specific Chef special case.

Similarly, first-night numeric truth obtains Evil registration from each player's alignment, except Spy/Recluse interaction-local registration.

Therefore a future canonical Traveller model should let ordinary adjacency semantics consume the **full seated circle**, rather than adding a Traveller-specific Chef implementation.

## 3. Why the obvious shortcuts are invalid

### Drop the Travellers and evaluate the 15 base players

Invalid. Removing seats changes circular neighbors. Chef, Empath, target ecology, and later public reasoning can change.

### Keep 20 seats but pretend Travellers are Townsfolk/Outsiders/Minions

Invalid. This contaminates setup quotas, role uniqueness, topology enumeration, and possible-world feasibility.

### Run only Chef arithmetic with a hand-authored 20-seat fixture

Useful as a manually derived provenance fact, but not acceptable as B4 legal-counterfactual execution. B4 requires production legality owners to define the complete candidate domain.

### Add `TRAVELLER` to `CharacterType` in B4

Insufficient and high-fanout. Existing exhaustive `when` branches, setup quotas, world construction, persistence/projection, role registries, and UI/session assumptions would all need deliberate semantics.

## 4. Correct future capability boundary

If Traveller support becomes a product goal, start a separate capability slice with a fan-out audit before implementation.

The design should explicitly answer:

1. how a seated participant is marked base-setup vs Traveller;
2. how Traveller role identity is represented in the canonical registry;
3. how Traveller public Good/Evil alignment is represented and persisted;
4. how total seated count differs from base setup count;
5. how circle adjacency includes Travellers;
6. how world/topology enumeration holds Traveller state fixed or models its known alignment without consuming base-role quotas;
7. how first-night / other-night order incorporates Traveller abilities;
8. how restore/history/UI/session projections preserve those facts;
9. which algorithms intentionally ignore Traveller abilities while still respecting their seat/alignment effects.

The preferred architecture is **not predetermined by this audit**. In particular, do not assume the only correct design is simply `CharacterType.TRAVELLER`; setup participation may need an orthogonal canonical property.

## 5. B4 consequence

For the current calibration route:

- preserve `A Fond Farewell` as primary-verified source evidence;
- use Ben's explicit rationale as qualitative expert evidence;
- keep its exact reconstructed 20-seat state for future replay;
- do not calculate a fake production legal domain;
- do not block B4 source collection on Traveller implementation;
- make the independent Evin Trouble Brewing recording the next fresh primary-extraction target;
- continue primary verification of the three already-executable Ben reconstructions when efficient.

This leaves **0 admitted GOLD** for now because no case yet satisfies both:

1. material primary-state verification; and
2. production-owned complete legal-counterfactual recovery.

That is an evidence-gate result, not a reason to weaken either requirement.
