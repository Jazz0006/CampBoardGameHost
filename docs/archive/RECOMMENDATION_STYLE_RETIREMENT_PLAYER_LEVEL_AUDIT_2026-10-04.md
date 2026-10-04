# RecommendationStyle Retirement / Player-Level Recommendation Context Audit — 2026-10-04

> Repository: `Jazz0006/CampBoardGameHost`
>
> Audited baseline: `main@f7b70a7403c6dbfcc782a69f10e9540346a484ff`
>
> Status: **RSR-0 AUDIT COMPLETE / TARGET CONTRACT ACCEPTED**
>
> Historical production verdict at RSR-0: **NO STYLE-RETIREMENT CODE SLICE IS SAFE YET WITHOUT FIRST DEFINING STYLE-NEUTRAL POLICY SEMANTICS**. RSR-1A later completed the safe Storyteller-mode/style ownership decoupling. The planned family-by-family behavior-preserving continuation is now **superseded** by `docs/LEGACY_RECOMMENDATION_ENGINE_RETIREMENT_AUDIT_2026-10-04.md`, because the product owner has withdrawn trust from the whole legacy heuristic recommender rather than only its three styles.
>
> Product-owner direction: future recommendation differentiation should primarily use **per-player experience level**. The intended small vocabulary is **BEGINNER / NORMAL / EXPERT**. The product currently exposes no player-level setting, so every player is treated as **NORMAL** until the future player-profile/player-management slice exists.

## 1. Decision

The legacy global recommendation dimension:

`RecommendationStyle.GENTLE / BALANCED / AGGRESSIVE`

is obsolete product architecture and should be retired.

It must **not** be renamed or mechanically reinterpreted as player level:

- `GENTLE != BEGINNER`;
- `BALANCED != NORMAL`;
- `AGGRESSIVE != EXPERT`.

Those old values describe global recommendation aggressiveness. The future values describe attributes of individual players and may affect a recommendation differently depending on the decision family, the source player, the target player, the evil team, or the table composition.

The replacement architecture is therefore:

```text
StorytellerExperienceMode
  -> UI / authority mode only
     BEGINNER: more automatic authority
     EXPERIENCED: more manual authority

per-player experience enrichment
  -> recommendation input only
     BEGINNER / NORMAL / EXPERT
  -> currently every seated player resolves NORMAL
  -> future player profile / management UI may override per player

rules / Game Engine
  -> legality and canonical mechanical truth

Recommendation Engine
  -> legal candidates
  + typed decision context
  + optional per-player experience snapshot
  -> versioned policy / ranking
```

There is no global replacement `RecommendationStyle`.

## 2. Four concepts that must remain separate

### 2.1 Storyteller experience mode

Existing `StorytellerExperienceMode.BEGINNER / EXPERIENCED` is about **the host/operator experience**.

Its durable responsibilities are interaction policy such as:

- automatic execution vs manual confirmation;
- whether manual alternatives are exposed;
- how many alternatives are presented.

It must not choose recommendation aggressiveness or encode player skill.

The current `StorytellerRecommendationUxPolicy.recommendationStyle` field is therefore a legacy ownership error. Both current Storyteller modes already pass `AGGRESSIVE`, which further demonstrates that this field is not a meaningful mode distinction.

### 2.2 Player experience level

Future recommendation context uses one level per player:

```text
BEGINNER
NORMAL
EXPERT
```

Current product default: **NORMAL for every player**.

This explicit product default supersedes the earlier provisional statement that missing player context could not be treated as average. Until the player-profile producer exists, the product's effective player-experience snapshot is all-NORMAL.

No games-played thresholds are frozen by this audit.

### 2.3 Recommendation policy version

A recommendation policy remains versioned independently of player level.

Examples already in the repository include `BEGINNER_CONSERVATIVE_V1/V2` and `DRUNK_ASSIGNMENT_Q04_V1`. A future policy may consume player experience, but player level itself is not the policy version.

### 2.4 QualityTier.EXPERT_ONLY

`QualityTier.EXPERT_ONLY` currently means a higher-risk candidate intended for an experienced **Storyteller/manual surface**.

It does not mean “the target player is EXPERT”.

Do not rename or reinterpret this tier as player experience during style retirement.

## 3. Future player-level ownership contract

### 3.1 Per-player, not global

The authoritative enrichment shape should remain per player / per seat at the decision boundary, conceptually:

`seat -> BEGINNER | NORMAL | EXPERT`.

Do not persist a derived “table level” as a second authority.

If a future policy needs table composition, it may derive features such as:

- number of beginners;
- experience imbalance between alignments;
- source-player level;
- target-player level;
- min/max/mixture of relevant candidate players;

from the per-player snapshot.

Those derived features belong to the versioned recommendation policy/feature projection.

### 3.2 Not canonical game truth

Player experience is **recommendation enrichment**, not BotC mechanical truth.

Therefore it should not be added to:

- `GameState`;
- `TroubleBrewingGameSnapshotV1`;
- rules-owned legal domains;
- role/registration legality;
- canonical action history.

Missing or changed player profile data must never change what is legal.

### 3.3 Future live owner

The current “common players” persistence stores only a list of names, and `ConfirmedHostSeating` freezes only seat + player name.

A future player-profile/player-management slice should own player metadata, including experience level. That slice may later decide whether a stable player-profile ID is required in addition to display name.

This retirement audit does not change persistence, seating, Recovery or UI.

### 3.4 Decision-time freezing

Once a policy actually consumes player experience, the values used for that decision must be frozen as decision-time recommendation input.

Later edits to a player's profile must not rewrite historical replay.

For a policy version that consumes experience:

```text
live player profile
  -> seating mapping
  -> decision-time PlayerExperienceSnapshot
  -> typed recommendation context / feature projection
  -> DecisionTrace / neutral export input identity as required
```

The exact DTO name is not frozen.

### 3.5 ML/export boundary

Player experience may be `INPUT_ELIGIBLE` in `RecommendationDecisionExportV1` or a later schema only when the corresponding policy/decision actually consumed a decision-time frozen value.

It is never a target label merely because the historical player later became more experienced.

Do not materialize profile values by joining today's profile onto old decisions.

## 4. Current RecommendationStyle fan-out

The current enum is not presentation-only. Retirement must be staged.

### 4.1 Algorithmically active — cannot mechanically delete

#### Setup recommendation

`RecommendationProfiles.gentle / balanced / aggressive` change multiple setup weights.

`SetupRecommendationService` explicitly:

- ranks the full setup candidate space three times;
- selects one plan per style;
- diversifies the three outputs;
- includes `profile.style` in the stable selector seed.

Therefore removing styles changes setup ranking and deterministic identity unless a replacement single-policy contract is defined.

#### Malfunction / unreliable information

`MalfunctionPolicy` has materially different truth-distance, misinformation-pressure and continuity scoring for all three styles.

It intentionally produces multiple distinct recommendations.

This is policy behavior, not UI decoration.

#### Generic dynamic candidate selection

`DynamicCandidateGenerator.select(...)` uses style to prefer different misinformation pressure and includes style in selection behavior.

The current `evilAdvantage` parameter is not the source of the truthful/false family budget, but style itself remains algorithmically active.

#### Special registration / consequence evaluation

`RegistrationPolicy` and `ConsequenceEvaluator` use style for:

- natural vs special-registration preference;
- misinformation pressure;
- repeat-history pressure;
- global-balance strength;
- quality-tier decisions;
- stable selection identity.

Special registration therefore cannot simply drop style while its replacement policy semantics are undefined.

#### Mayor redirect

`MayorRedirectRecommender` generates one recommendation per style. Style changes:

- Mayor direct-death preference;
- Mayor survival preference;
- no-death preference;
- killing an evil player;
- pressure continuity;
- one `EXPERT_ONLY` gate.

The rules-owned Mayor legal domain is already clean, but ranking is still style-dependent.

#### Demon succession

`DemonSuccessorRecommender` generates style-specific rankings when multiple successors are legal. Role preference and public-pressure behavior vary by style.

Its canonical mechanical context is already clean from TBGS-2D, but the policy remains style-dependent.

#### Balance-to-style adapter

`GameBalanceEvaluator.adjustInformationStyle(...)` converts `evilAdvantage` into another global RecommendationStyle.

That adapter has no place in the target architecture. Game-balance evidence may remain useful as an independent feature, but it must not survive merely to mutate a retired global style.

### 4.2 Identity / dedup / telemetry — must migrate with policy

Style is embedded in non-gameplay infrastructure:

- `StorytellerPolicySnapshot`;
- `DynamicRecommendationKey`;
- setup unified-selection candidate IDs;
- pair-information stable keys;
- `SelectionAuditDimensions` and `SelectionAuditKey`;
- selection distribution review/cohorts;
- simulation/tolerance calibration IDs;
- several weighted-selector seeds.

These fields currently prevent two style variants from colliding.

They cannot be deleted before the generating policy stops producing style variants.

Target identity should be based on:

- policy version;
- algorithm/config version;
- canonical decision/context identity;
- decision-time enrichment digest when the policy actually consumes enrichment.

Player level should not be inserted as a global “style” substitute.

### 4.3 UI / presentation — obsolete once producer is single-policy

Style currently appears in:

- recommendation labels such as “gentle / balanced / aggressive”;
- `RecommendationPlan.style`;
- `DynamicDecisionRecommendation.style`;
- structured display models;
- selected/applied style state in `ClocktowerHostScreen`;
- “default recommendation == BALANCED” markers.

The current first-night setup UI contains `selectedStyle` state but no visible `onSelectStyle` control is actually used. In practice the App supplies the UX policy's current style, currently AGGRESSIVE for both Storyteller modes.

These presentation fields should disappear after each producer returns one policy result rather than three style variants.

### 4.4 Replay / SDE / neutral export

The mature SDE replay contract is already in a better shape:

- `DecisionTracePolicySnapshot` is keyed by `PolicyVersion`, not `RecommendationStyle`;
- HOST-ML1 `RecommendationPolicyTraceV1` exports policy snapshots/selections, not global style;
- Recovery/persistence has no direct `RecommendationStyle` serialization.

Therefore no Recovery schema migration is currently indicated by style retirement.

Two setup-era SDE adapters still expose legacy style maps:

- `RedHerringSetupShadowEvaluation.legacyRedHerringCandidateIdByStyle`;
- `DemonBluffSetupShadowEvaluation.legacyBluffCandidateIdByStyle`.

Those are compatibility/differential bridges to the old three-plan setup recommender. They should be retired or reshaped only when setup recommendation becomes single-policy.

HOST-ML1 itself does not need to be rewritten merely because global style is retired.

## 5. Why “current AGGRESSIVE” is not the replacement policy

Both current Storyteller modes set `recommendationStyle = AGGRESSIVE`.

That is a fact about current wiring, not evidence that AGGRESSIVE represents NORMAL players.

Similarly, the name BALANCED does not make BALANCED the correct NORMAL-player policy.

The old style policies mix many unrelated choices:

- misinformation strength;
- continuity;
- balance intervention;
- role preferences;
- risk tolerance;
- target pressure;
- diversification.

Future player-level policy should be allowed to use player experience only where evidence shows it matters, while keeping other policy dimensions independent.

Therefore retirement must not perform:

`NORMAL -> BALANCED`

or:

`EXPERT -> AGGRESSIVE`

as a compatibility shortcut.

## 6. Retirement dependency graph

### RSR-0 — architecture / fan-out audit

Status: **COMPLETE / ACCEPTED**.

This document is the authority.

### RSR-1 — style-neutral policy contract, family by family

Status: **NEXT / POLICY SEMANTICS REQUIRED BEFORE CODE**.

For each active recommendation family, define one versioned policy output without using GENTLE/BALANCED/AGGRESSIVE as the public contract.

A family may use:

- existing evidence-backed policy semantics;
- explicit player-level enrichment when supported;
- explicit unavailable/defer behavior;
- policy-neutral legal/manual options.

It must not obtain its new semantics by averaging or copying the three old style score tables.

Recommended order for RSR-1 audit:

1. setup recommendation;
2. malfunction/unreliable information;
3. Mayor redirect and Demon succession;
4. special registration after typed-history producer ownership is resolved.

These can be split further if evidence/readiness differs.

### RSR-2 — production style collapse

Only after a family has a style-neutral policy contract:

- return one recommendation/ranking result rather than one per style;
- remove style from that family's request/result models;
- remove style-dependent selector seeds and candidate IDs;
- move tests from “three style outputs” to the new durable policy contract;
- preserve complete rules-owned legal/manual domains.

### RSR-3 — cross-cutting identity / telemetry cleanup

After no production family needs global style:

- remove style from `StorytellerPolicySnapshot` / `DynamicRecommendationKey`;
- replace telemetry style dimension with policy/version and only evidence-backed enrichment dimensions;
- remove style from simulation calibration;
- remove dead style labels/default markers and Host selected/applied style state.

### RSR-4 — enum/profile retirement

Finally remove:

- `RecommendationStyle`;
- `RecommendationProfiles.gentle/balanced/aggressive`;
- `GameBalanceEvaluator.adjustInformationStyle`;
- legacy style-keyed setup SDE shadow maps;
- obsolete style-only tests.

## 7. Is an implementation slice safe now?

Verdict: **NO meaningful production retirement slice should start yet.**

The architecture direction is clear, but every material producer still needs a style-neutral single-policy semantic contract.

Removing only UI/style fields now would either:

- hide an implicit hard-coded legacy style;
- accidentally make AGGRESSIVE canonical because it is today's UX value;
- accidentally make BALANCED canonical because of its name/default markers;
- change deterministic candidate identity without defining the replacement policy;
- break setup SDE differential bridges without replacing their semantic comparison.

A trivial cleanup such as deleting the currently-unused `onSelectStyle` callback is technically safe but does not advance the actual retirement enough to justify a standalone product slice.

Therefore this audit should close as docs/architecture only.

## 8. Future player-management slice

The eventual player UI/profile work is independent from RSR-1 policy design.

Target product behavior:

- adding/managing a player exposes experience level;
- choices: BEGINNER / NORMAL / EXPERT;
- default: NORMAL;
- existing players without explicit setting resolve NORMAL;
- changing a profile affects future recommendations only;
- existing historical decisions retain their decision-time enrichment;
- Storyteller BEGINNER/EXPERIENCED mode remains a separate setting.

Do not implement this UI/persistence as part of style retirement.

## 9. Future tests-first acceptance

A later RSR implementation should prove, as applicable:

1. rules/legal candidates are identical across player levels;
2. Storyteller experience mode changes authority/presentation, not hidden ranking style;
3. players without explicit level resolve NORMAL;
4. explicit player levels are supplied per seat/player, not as a single global style;
5. same canonical state + same policy version + same decision-time player-level snapshot is deterministic;
6. changing a profile later does not rewrite historical replay;
7. player experience reaches only policies that declare it as enrichment;
8. policy versions that do not consume player level remain invariant to it;
9. `DecisionTrace` / neutral export preserve decision-time input when a consuming policy requires it;
10. no GENTLE/BALANCED/AGGRESSIVE label, score branch, selector seed, telemetry dimension or candidate identity remains after final retirement;
11. `QualityTier.EXPERT_ONLY` remains Storyteller/manual-risk semantics unless separately redesigned.

## 10. Explicit non-goals

RSR-0 does not:

- implement `PlayerExperienceLevel` in production;
- add player-management UI;
- change common-player persistence;
- add a player-profile database;
- change Recovery;
- add experience to `GameState` or TB snapshot V1;
- freeze games-played thresholds;
- choose a NORMAL ranking policy;
- map legacy styles to player levels;
- change setup ranking;
- change misinformation truth/false policy;
- change Mayor or Demon successor outputs;
- change special registration;
- wire typed decision-history producers;
- modify C5/V2, Drunk Q04 V1, HOST-ML1, or accepted TBGS-2A–2E behavior.

## 11. Current sequence

```text
TBGS-2A..2E                         COMPLETE / ACCEPTED
post-2E re-audit                    COMPLETE / no TBGS-2F
RSR-0 style-retirement/player-level audit
                                    COMPLETE / ACCEPTED
-> RSR-1A Storyteller-mode/style ownership decoupling
                                    COMPLETE / ACCEPTED
-> LRE-0 whole legacy heuristic recommender retirement audit
                                    COMPLETE / RETIREMENT AUTHORIZED
-> LRE-1 manual fallback + fail-closed authority cutoff
                                    NEXT
-> later LRE slices remove old setup/dynamic/scoring/style code

parallel later:
player profile / management
  -> BEGINNER / NORMAL / EXPERT
  -> default NORMAL
  -> future recommendation enrichment producer
```