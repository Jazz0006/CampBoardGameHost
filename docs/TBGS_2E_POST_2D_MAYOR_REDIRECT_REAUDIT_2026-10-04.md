# TBGS-2E Post-2D Fresh Re-Audit — Mayor Redirect Context — 2026-10-04

> Repository: `Jazz0006/CampBoardGameHost`
>
> Baseline: `main@e249781966287c03c33d9c155f6ddcfbca84ae9b`
>
> Status: **COMPLETE / ACCEPTED**
>
> Selected family: **Mayor night-death redirection recommendation**

## 1. Fresh decision

After TBGS-2D, the remaining `DynamicGameState` consumers were re-audited without assuming the old ordering.

The next bounded migration is **Mayor redirect**.

Why Mayor wins now:

- it is one decision family rather than a cross-cutting style/balance surface;
- current night rules already determine whether Mayor redirection is available;
- target legality is already expressed by the rules-owned `MayorRedirectLegality` restriction;
- canonical TB snapshot already owns role/type/alignment/alive/poison/seat truth;
- the missing inputs are bounded, explicit enrichment rather than a new schema campaign.

Special registration remains deferred because its scoring depends on registration/misinformation history whose typed production producer is not complete. Cross-cutting balance remains deferred because changing its owner would simultaneously touch Artist and multiple automatic-information surfaces.

## 2. Current ownership split

Current Mayor recommendation still consumes:

```text
ClocktowerJudgeScreen.dynamicStorytellerState()
  -> PlayerCard -> GameState reconstruction
  -> protectedSeats
  -> spentAbilitySeats
  -> pressureBySeat
  -> publicBalanceHint
        |
MayorRedirectRecommender
  -> independently enumerates Mayor + non-Demon targets
  -> independently checks MayorRedirectLegality
  -> resolves no-death from dead/protected/Soldier
```

Meanwhile the Host/rules path already owns:

```text
resolveTroubleBrewingDawnDeathResolution(...)
  -> mayorRedirectEligible
  -> mayorSeat

ClocktowerNightHostProjection
  -> MayorRedirectLegality
  -> mayorRedirectTargetCards
```

This duplicates legality in recommendation and Host presentation.

## 3. Canonical snapshot coverage

`TroubleBrewingGameSnapshotV1` already provides the mechanical player truth needed by Mayor scoring:

- actual role/type/alignment;
- alive/dead;
- poison state;
- stable seat;
- phase/round/seed/revision.

No snapshot schema expansion is authorized.

The snapshot does **not** currently carry Monk protection. Therefore current protected-seat truth remains an explicit runtime enrichment input for TBGS-2E. It must not be invented from UI labels or silently dropped.

## 4. Proposed TBGS-2E context

Introduce:

`TroubleBrewingMayorRedirectDecisionContext`

Required correctness inputs:

- runtime TB snapshot at NIGHT;
- Mayor seat;
- rules-owned legal decision domain:
  - direct Mayor death seat;
  - legal redirect target seats.

Explicit enrichment inputs preserving current behavior:

- protected seats;
- spent-ability seats;
- player information pressure by seat;
- derived `PublicBalanceHint` via existing `GameBalanceEvaluator`.

The context may project a bounded compatibility `GameState` from the snapshot for existing scoring. It must never reconstruct mechanical truth from `PlayerCard`.

## 5. Legality convergence

TBGS-2E must make rules/Host own the complete legal decision domain.

The recommender must stop doing:

- player-wide target enumeration;
- Demon-target legality filtering.

It may only rank the supplied legal seats.

The direct-Mayor-death option remains part of the legal decision domain when the Mayor redirect decision is active.

The UI must not post-filter an already-ranked recommendation against a separately reconstructed target list.

## 6. Outcome semantics

Existing outcome semantics remain unchanged:

- choose Mayor seat -> Mayor dies;
- choose dead target -> no death;
- choose Monk-protected target -> no death;
- choose Soldier -> no death;
- otherwise chosen target dies.

TBGS-2E changes ownership/input plumbing, not Mayor rules or recommendation weights.

## 7. Explicit non-goals

TBGS-2E must not:

- migrate special registration;
- migrate cross-cutting Artist/night-information balance ownership;
- switch pressure/history to `DecisionHistoryRepository`;
- add protection to TB snapshot schema;
- change NightCheckpoint/Dawn transaction ownership;
- change Mayor redirect product legality;
- alter scoring weights;
- touch C5/V2, Drunk policy, Recovery, or HOST-ML export.

## 8. Acceptance tests

Tests-first coverage must prove:

1. only runtime NIGHT TB contexts are accepted;
2. snapshot projection preserves role/type/alignment/alive/poison truth;
3. recommender returns only seats supplied by the legal domain;
4. a Demon cannot be reintroduced when absent from that domain;
5. direct Mayor death remains rankable when supplied;
6. dead/protected/Soldier redirect outcomes remain no-death;
7. protected-seat enrichment is explicit and affects outcome exactly as before;
8. spent-seat / pressure / public-balance scoring remains parity-equivalent on representative fixtures;
9. Host no longer builds the TB Mayor recommendation from `dynamicStorytellerState()`;
10. UI does not apply a second Mayor legality filter after ranking;
11. non-TB compatibility is unchanged unless explicitly required by compile-safe adapter work;
12. final T4 passes Android FULL/assemble + ASP + Real Clingo + R2.

## 9. Sequence

```text
TBGS-2A pair precompute                 COMPLETE / ACCEPTED
TBGS-2B pair manual/publication        COMPLETE / ACCEPTED
TBGS-2C setup coordination             COMPLETE / ACCEPTED
TBGS-2D Demon succession               COMPLETE / ACCEPTED
TBGS-2E Mayor redirect                 COMPLETE / ACCEPTED
-> fresh re-audit after 2E             NEXT
   -> special registration OR cross-cutting balance
```

Do not preselect the post-2E family before another fresh audit.

## 10. Implementation result before final T4

Implementation checkpoint `f512fa2339677de4e9e8c12ea3694938867ed584` passed CI #3730 / R2 #3432 GREEN.

Final exact-head T4 `ce5b9943bf4c0d1a27b73c5de5d45a841349949f` passed CI #3731 / R2 #3433 GREEN across Android FULL/assemble, ASP and Real Clingo.

The implementation now:

- introduces rules-owned `MayorRedirectDecisionDomain` / `MayorRedirectLegalDomain`;
- builds `TroubleBrewingMayorRedirectDecisionContext` from the canonical runtime TB snapshot;
- carries Monk protection, spent seats and pressure explicitly as enrichment and derives the existing `PublicBalanceHint` with `GameBalanceEvaluator`;
- makes `MayorRedirectRecommender` rank only legal seats supplied by the rules domain rather than re-enumerating Demon legality;
- keeps direct-Mayor-death inside the decision domain;
- preserves dead/protected/Soldier no-death outcome semantics;
- routes the TB Host through the App/root snapshot provider instead of `dynamicStorytellerState()`;
- preserves a non-TB compatibility adapter;
- leaves special registration, cross-cutting balance/style, typed-history production, snapshot schema and transaction ownership unchanged.
