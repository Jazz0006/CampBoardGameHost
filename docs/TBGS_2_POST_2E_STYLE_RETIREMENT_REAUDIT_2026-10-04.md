# TBGS-2 Post-2E Fresh Re-Audit / Legacy Recommendation-Style Retirement Decision — 2026-10-04

> Repository: `Jazz0006/CampBoardGameHost`
>
> Audited baseline: `main@8af8f031f3b410fe281695183898ac925def7dd7`
>
> Status: **COMPLETE / ACCEPTED AUDIT**
>
> TBGS-2 implementation verdict: **NO NEW IMPLEMENTATION-READY FAMILY AFTER 2E**
>
> Product-owner correction during audit: the legacy **GENTLE / BALANCED / AGGRESSIVE recommendation-style routes do not need to be preserved**.

## 1. Fresh decision

The post-TBGS-2E audit originally had two major remaining candidates:

1. Spy/Recluse special-registration recommendation;
2. cross-cutting balance/style migration.

The product-owner correction changes that comparison materially.

The old `RecommendationStyle.GENTLE / BALANCED / AGGRESSIVE` dimension is no longer a product requirement. Therefore the project must **not** spend a TBGS-2 slice migrating the old balance/style machinery from `PlayerCard -> DynamicGameState` into a cleaner canonical-snapshot implementation merely to preserve it.

The correct post-2E result is:

- **cross-cutting balance/style: RETIRE, DO NOT MIGRATE**;
- **special registration: still not cleanly implementation-ready because typed history production is incomplete**;
- therefore **TBGS-2 pauses after TBGS-2E rather than inventing TBGS-2F**.

A separate focused retirement audit should remove the legacy three-style dimension safely before later recommendation-family cleanup chooses any new single-policy semantics. That follow-up is now complete as **RSR-0** at `docs/RECOMMENDATION_STYLE_RETIREMENT_PLAYER_LEVEL_AUDIT_2026-10-04.md`.

Important boundary: this decision retires the old three recommendation-style routes. It does not automatically delete every unrelated enum/string containing the word “balanced”; for example, a public game-state balance label must be judged by its actual consumers during retirement cleanup.

## 2. Current remaining DynamicGameState consumers

At the audited baseline, `ClocktowerJudgeScreen.dynamicStorytellerState()` still reconstructs:

```text
PlayerCard list
  -> toClocktowerGameState(...)
  -> phase / round
  -> Monk protection
  -> Virgin / Slayer / Artist spent seats
  -> UI-derived player information pressure
  -> localized UI-event-title-derived registration ledger
  -> GameBalanceEvaluator
  -> DynamicGameState
```

The remaining direct production uses split as follows.

### 2.1 Trouble Brewing special registration

`registrationRecommendationOptions(...)` still creates a `DynamicDecisionRequest(SPECIAL_REGISTRATION)` using `dynamicStorytellerState()`.

This path serves multiple Spy/Recluse interactions, including day and night surfaces.

This remains the only major TB decision-family migration candidate.

### 2.2 Legacy balance/style Host aggregate

The Host also eagerly builds `currentDynamicStorytellerState` to obtain `evilAdvantage` and derive `automaticInformationStyle`.

That value currently reaches:

- reliable Artist automatic style selection;
- Artist unreliable-information selector plumbing;
- generic night-information selector plumbing.

However the generic impaired-information selector currently does not use `evilAdvantage` to set its truthful/false family budget. More importantly, the product-owner decision now makes preserving this three-style route unnecessary.

Therefore this is **retirement debt**, not a migration target.

### 2.3 Intentional compatibility

Mayor and Demon succession use `dynamicStorytellerState()` only in non-TB compatibility adapters after TBGS-2D/2E.

Those compatibility paths are not reopened by this audit.

`DynamicGameState` also remains a generic recommendation model in other internal APIs/tests. This audit does not authorize a universal model deletion.

## 3. Special registration — true readiness

Verdict: **BLOCKED FOR CLEAN TBGS-2 MIGRATION**.

### 3.1 What is ready

Rules legality is already owned correctly.

`TroubleBrewingRegistrationDomain.resolve(...)` owns the legal registration outcomes, and the Host already resolves the legal special role domain before recommendation.

The canonical TB runtime snapshot already carries the mechanical truth needed for a future typed registration context:

- seat;
- actual role/alignment/type;
- shown role;
- alive/dead;
- poison;
- phase / round / seed.

`TroubleBrewingRegistrationSubject` already models the interaction-time effective role/poison projection.

No snapshot schema expansion is currently required.

### 3.2 What style retirement simplifies

Current `RegistrationPolicy` generates GENTLE / BALANCED / AGGRESSIVE recommendation variants and changes:

- base special-vs-natural score;
- misinformation-pressure preference;
- repeated-registration penalty;
- global-balance strength;
- quality-tier downgrade behavior;
- stable variation radius.

Those style-specific branches are now legacy behavior and should not constrain the future typed registration context.

This removes one source of migration complexity.

It does **not** resolve the history ownership gap.

### 3.3 Typed-history blocker remains

`DecisionHistoryRepository.project()` is already the intended typed projector for:

- `pressureBySeat`;
- `misinformationLedger`;
- `registrationLedgerBySeat`.

But production producer/cutover is incomplete:

- `ClocktowerRecommendationCoordinator.appendDecision(...)` has no production caller;
- production normal recommendation commits do not construct typed `StorytellerDecisionEvent` values;
- `DynamicDecisionTransactionAggregate` has no production consumer;
- Spy/Recluse registration history is currently recorded as presentation-level `ClocktowerEvent`;
- `dynamicStorytellerState()` reconstructs registration counts by searching localized event titles for “registration” / “登记”;
- current Host production does not populate a complete typed misinformation ledger.

A new snapshot-backed registration context should not freeze this localized presentation-history reconstruction as its durable input contract.

Therefore special registration should wait for a bounded typed-history producer/cutover decision.

## 4. Cross-cutting balance/style — true readiness

Verdict: **NO LONGER A MIGRATION CANDIDATE**.

The legacy style dimension has broad production fan-out:

- setup recommendation profiles;
- malfunction/unreliable-information policy;
- special registration;
- Mayor redirect ranking;
- Demon succession ranking;
- consequence evaluation;
- weighted selector fallback/defaults;
- simulation/calibration;
- UI labels and “default recommendation” markers;
- structured information adapters;
- selection telemetry;
- `StorytellerRecommendationUxPolicy`.

Notably, current Beginner and Experienced UX policies both pass `RecommendationStyle.AGGRESSIVE`, while deeper code still constructs three style variants. This is strong evidence that the style dimension is now historical architecture rather than a meaningful user-facing mode distinction.

But **AGGRESSIVE must not automatically become the new canonical policy** merely because it is currently the UX-supplied value. Retiring three old variants and defining one durable recommendation policy are separate decisions.

The dedicated style-retirement audit is now complete. Its continuation authority is `docs/RECOMMENDATION_STYLE_RETIREMENT_PLAYER_LEVEL_AUDIT_2026-10-04.md`: future recommendation differentiation is per-player BEGINNER/NORMAL/EXPERT enrichment with current NORMAL defaults, while RSR-1 must define style-neutral family policies before production code retirement.

## 5. Recommended next family

### TBGS-2 recommendation

**No next TBGS-2 implementation family should be selected now.**

TBGS-2 should remain accepted through:

```text
2A pair precompute
2B pair manual/publication
2C setup coordination
2D Demon succession
2E Mayor redirect
```

and then pause.

### Recommended next independent work

The focused **Recommendation Style Retirement audit** is COMPLETE / ACCEPTED as RSR-0. Continue with RSR-1 family-by-family style-neutral policy-contract work before any production retirement.

That audit should answer:

1. which style fields are presentation-only versus algorithmically active;
2. which accepted policies already have a style-independent semantic owner;
3. which APIs can collapse from three variants to one candidate/ranking contract without inventing new weights;
4. which tests are protecting obsolete style variation versus real correctness;
5. whether `GameBalanceEvaluator` remains useful for any independent recommendation feature after style removal;
6. whether `PublicBalanceHint` / `evilAdvantage` have any surviving non-style consumer;
7. which telemetry/export contracts contain style and need compatibility handling;
8. how style retirement interacts with HOST-ML neutral export and replay contracts.

After style retirement is bounded, re-audit special registration. If registration still depends on presentation-derived history, perform a separate typed-history producer/cutover slice rather than embedding that debt in TBGS-2.

## 6. Why this is better than forcing another TBGS-2 slice

Continuing with a snapshot-backed balance context would improve the implementation of behavior the product no longer wants.

Continuing with special registration now would require either:

- preserving old style-dependent scoring that is now obsolete; and/or
- freezing localized UI-event-derived history into a new typed context.

Both would create migration work that should soon be removed again.

Stopping TBGS-2 at 2E preserves the architectural wins already achieved without manufacturing another migration target.

## 7. Bounded implementation contract

There is **no production implementation contract for TBGS-2F**.

The next implementation contract must come from RSR-1 style-neutral family policy semantics defined under `docs/RECOMMENDATION_STYLE_RETIREMENT_PLAYER_LEVEL_AUDIT_2026-10-04.md`.

Until then:

- do not create a canonical balance/style context;
- do not migrate `GameBalanceEvaluator.adjustInformationStyle`;
- do not select one of GENTLE/BALANCED/AGGRESSIVE as the new canonical policy by default;
- do not migrate special registration onto snapshot-backed history using localized UI-event parsing;
- do not broaden TBGS-2 into generic recommendation cleanup.

## 8. Tests-first acceptance

No new production RED/GREEN is required for this audit-only result.

A future style-retirement implementation must build its acceptance around durable semantics rather than preserving three legacy output variants. At minimum it will need to prove:

- rules-owned legal candidate domains are unchanged;
- accepted single-decision behavior does not become illegal;
- Beginner/Experienced authority differences remain UX/interaction differences, not hidden style weights;
- replay/export contracts remain deterministic or receive an explicit versioned migration;
- removal of style variants does not silently alter probability-family correctness for impaired information;
- obsolete style-label/default-marker tests are retired only after their real product contract is identified;
- no old style-specific score branch remains accidentally active.

## 9. Explicit non-goals

This audit does not:

- delete `RecommendationStyle` yet;
- choose a replacement single policy;
- make current AGGRESSIVE semantics canonical;
- change setup ranking weights;
- change malfunction truth/false budgets;
- change Mayor redirect scoring;
- change Demon succession scoring;
- change special-registration scoring/history;
- wire typed decision history;
- change snapshot schema;
- change Recovery;
- change C5/V2 or HOST-ML;
- change accepted TBGS-2A–2E behavior.

## 10. Authoritative sequence

```text
TBGS-2A  COMPLETE / ACCEPTED
TBGS-2B  COMPLETE / ACCEPTED
TBGS-2C  COMPLETE / ACCEPTED
TBGS-2D  COMPLETE / ACCEPTED
TBGS-2E  COMPLETE / ACCEPTED

post-2E fresh re-audit
  -> special registration: BLOCKED on typed-history production
  -> legacy balance/style: RETIRE, DO NOT MIGRATE
  -> no TBGS-2F implementation

NEXT:
  RSR-0 RecommendationStyle retirement / player-level audit COMPLETE / ACCEPTED
  -> RSR-1 style-neutral family policy contract
  -> then re-audit special registration / typed-history dependency
```
