# Legacy Recommendation Engine Retirement Audit — 2026-10-04

> Repository: `Jazz0006/CampBoardGameHost`
>
> Audited baseline: `main@881d4252c1a242c5ea523718fb8f780f693e9ab5`
>
> Status: **LRE-0 AUDIT COMPLETE / WHOLE LEGACY HEURISTIC ENGINE RETIREMENT AUTHORIZED**
>
> Product-owner decision: the existing heuristic recommendation algorithm is not a trustworthy product-policy baseline. It frequently produces poor Storyteller clues/choices and should be retired as a whole rather than preserved family by family for behavior parity.
>
> Next implementation lane: **LRE-1 — preserve complete legal/manual authority, then revoke legacy automatic/recommendation authority fail-closed**.

## 1. Executive decision

The retirement target is broader than `RecommendationStyle.GENTLE / BALANCED / AGGRESSIVE`.

The current legacy recommendation stack combines:

- hand-authored role metadata;
- hand-authored integer score weights;
- global style multipliers;
- fixed score tolerances;
- fixed probability budgets;
- hash-based weighted choice;
- scalar “evil advantage” and information-pressure estimates;
- UI/presentation-derived history approximations;
- synthetic calibration that demonstrates deterministic behavior but not recommendation quality.

This stack is now classified as **retirement-only compatibility code**.

From this checkpoint forward:

1. **legacy behavior parity is not a product acceptance goal**;
2. no new production decision family may depend on legacy score/style/balance machinery;
3. old weights must not be renamed into a new “NORMAL player policy”;
4. if several legal outcomes exist and no accepted versioned policy can distinguish them, the correct answer is **MANUAL_REQUIRED / DEFERRED**, not an arbitrary legacy recommendation;
5. a unique rules-legal outcome may still auto-resolve because that is rules determinism, not recommendation;
6. accepted evidence-backed/versioned policies remain valid on their explicitly admitted surfaces;
7. the complete rules-owned legal domain, canonical state, exact epistemic evaluation, replay/trace and neutral export are preserved.

This is a deliberate product-quality change. Beginner mode may temporarily require human choice on more Storyteller decisions while replacement policies are still incomplete. A bad automatic answer is no longer preferable to an explicit manual choice.

## 2. Why the old algorithm is not a trustworthy policy

### 2.1 Hand-tuned setup weights are policy guesses, not evidence

`RecommendationProfiles` defines three manually chosen weight bundles.

Examples include:

- `evilCandidatePenalty = 18 / 14 / 5`;
- `redHerringOverlapPenalty = 12 / 8 / 3`;
- `diversityPenalty = 30 / 24 / 20`;
- several other exposure, discussion, pair-spacing and bluff-ease weights.

`TroubleBrewingRecommendationMetadata` assigns role-specific integers such as:

- exposure sensitivity;
- discussion value;
- bluff difficulty;
- Red-Herring suitability;
- Investigator display suitability.

`SetupEvaluator` multiplies and sums these values into one scalar score.

`SetupRecommendationService` then:

- evaluates the candidate space separately under GENTLE / BALANCED / AGGRESSIVE;
- keeps candidates inside a fixed score tolerance;
- converts score distance into weights;
- uses deterministic weighted selection;
- deliberately diversifies the three style outputs.

This can produce stable and different recommendations. It does not establish that the chosen recommendations are good Storyteller decisions.

### 2.2 Impaired-information policy contains arbitrary strength assumptions

`MalfunctionPolicy` assigns fixed style-specific scores for:

- distance from the truthful number;
- categorical truth vs falsehood;
- misinformation pressure;
- cross-night continuity.

Examples include formulas such as “distance × 6”, fixed truth bonuses/penalties, and pressure multipliers.

`DynamicCandidateGenerator` then adds another heuristic layer:

- preferred misinformation pressure = 1 / 2 / 4 by style;
- fixed penalties for style distance;
- fixed penalties for pressure distance;
- weighted stable selection.

`ImpairedInformationPolicy` additionally gives false-information families a default **90% mass** whenever both truthful and false impaired outcomes are legal, unless an explicit exception applies.

The legality and semantic family distinction are valuable. The 90/10 product-policy choice is not currently justified strongly enough to remain automatic authority.

### 2.3 Registration policy is similarly hand tuned

Rules-owned `TroubleBrewingRegistrationDomain` correctly owns what Spy/Recluse registrations are legal.

The old ranking layer does not share that quality:

- GENTLE/BALANCED/AGGRESSIVE use different hard-coded base scores;
- misinformation pressure, history and global-balance terms have fixed multipliers;
- a hash-derived “stable variation” perturbation is added;
- current dynamic Host history still reconstructs some registration history by matching localized UI event titles.

Separately, `TemporaryAutomaticStorytellerPolicy.selectRegistration` currently assigns:

- actual registration: 10%;
- special registration: 90%.

That ratio is a temporary UX policy, not evidence-backed Storyteller policy.

### 2.4 Mayor and Demon successor have arbitrary ranking policy

The TBGS-2D/2E work improved the **mechanical context and legal domains**, which must be preserved.

The recommendation scores remain legacy:

`MayorRedirectRecommender` contains hand-authored values for:

- Mayor dies vs survives;
- redirect/no-death;
- evil target death;
- pressure continuity;
- final-three leverage;
- public-balance adjustment.

`DemonSuccessorRecommender` contains style-dependent role priorities and an `evilAdvantage` correction.

The currently active Beginner automatic path is even more explicit:

`TemporaryAutomaticStorytellerPolicy` uses:

- Mayor dies 10% vs Townsfolk redirect 90%;
- Demon successor weights Baron 4 / Scarlet Woman 3 / Spy 2 / Poisoner 1.

These are not rules. They are not accepted evidence-backed policies. They must lose production authority.

### 2.5 Global balance / pressure are heuristic conclusions

`GameBalanceEvaluator` derives one `evilAdvantage` scalar from hand-authored terms such as:

- relative death rate × 70;
- alive-count bonuses;
- spent-ability counts;
- information-pressure totals;
- late-round corrections.

It then uses fixed ±15 / ±20 / ±55 thresholds for public-balance/style behavior.

`ConsequenceEvaluator` consumes the same style/balance/pressure worldview to score repeated targets, one-shot exposure, high-impact misinformation and alignment effects.

The long-lived SDE architecture already classifies `ConsequenceEvaluator` as legacy and targets it for removal. LRE-0 extends that retirement conclusion to the whole heuristic stack rather than leaving adjacent score owners intact.

### 2.6 Some inputs are themselves weak reconstructions

Current Host compatibility state still includes examples such as:

- `recentMisinformationStreak` inferred from localized event titles containing “misleading / 误导”;
- registration-history counts inferred from localized event titles containing “registration / 登记”;
- `PlayerInformationPressure` partly reconstructed from UI/history helper counts.

These can be useful migration clues, but they must not become long-lived policy authority.

Typed history remains worth preserving once production events are actually complete.

## 3. Why the old test suite did not prove recommendation quality

The old recommendation tests provide valuable software-correctness coverage, but several names overstate what they prove.

### 3.1 “ExpertRecommendationReviewTest” does not review recommendation quality

Its current assertions verify that:

- recommendations are non-empty;
- plans are rules-legal;
- renaming players does not change deterministic output.

Those are useful invariants. No expert preference, real-game evidence, or quality comparison is asserted.

### 3.2 Baseline simulation measures behavior, not goodness

`StorytellerV4BaselineSimulationTest` records:

- generated role/template distributions;
- repeat rates;
- legality;
- impaired-number output counts.

It does not establish that one generated clue is strategically better than a legal alternative.

### 3.3 Tolerance calibration is self-referential

Synthetic calibration around fixed score tolerances demonstrates that the selector behaves consistently with the chosen scoring model.

It does not independently validate the scoring model.

### 3.4 Test-retirement rule

During LRE:

- preserve tests for legality, candidate completeness, determinism of accepted policies, replay, state ownership and publication;
- retire or rewrite tests whose only contract is “the old weights/styles produce these outputs”;
- never keep obsolete policy semantics merely to keep an old regression test green.

## 4. Production authority inventory

### 4.1 P0 — legacy decisions that can affect automatic game behavior

These are highest retirement priority.

| Surface | Current legacy authority | Risk | Target |
| --- | --- | --- | --- |
| setup plan / Red Herring / Demon bluff source | `SetupRecommendationService` + style selection | old score model can drive actual first-night commitments | manual/legal-only or accepted versioned policy |
| generic first-night/later information fallback | `MalfunctionPolicy` / `RegistrationPolicy.recommendPair` + `DynamicCandidateGenerator` | old clue can be automatically shown | no legacy fallback; accepted policy or manual |
| Spy/Recluse automatic registration | `TemporaryAutomaticStorytellerPolicy` 90/10 | arbitrary interaction ruling applied automatically | manual or future evidence-backed policy |
| Mayor automatic ruling | temporary 10/90 policy | arbitrary death/redirect decision | manual or future accepted policy |
| Demon successor automatic ruling | temporary 4/3/2/1 role weights | arbitrary successor decision | manual or future accepted policy |
| dynamic balance-adjusted style | `GameBalanceEvaluator.adjustInformationStyle` | heuristic global balance changes recommendation behavior | remove as authority |

### 4.2 P1 — visible recommendation/advice surfaces

These may not always auto-commit, but can mislead the Storyteller:

- setup recommendation cards/reasons;
- old GENTLE/BALANCED/AGGRESSIVE information alternatives;
- special-registration recommendation labels;
- old Mayor/Demon successor recommendation lists;
- old score explanations presented as if they were meaningful product rationale.

These should disappear or be clearly unavailable once the corresponding P0 authority is cut.

### 4.3 P2 — compatibility / identity / telemetry / tests

After production authority is gone, retire:

- `RecommendationStyle`;
- `RecommendationProfile(s)`;
- `RecommendationPlan.style`;
- `DynamicDecisionRecommendation.style`;
- `ClocktowerDisplayOption.recommendationStyle`;
- `LegacyRecommendationStyleCompatibility`;
- `StorytellerPolicySnapshot.style`;
- `DynamicRecommendationKey.style`;
- style dimensions in selection telemetry;
- style labels/default-marker UI;
- old score tolerances and synthetic calibration;
- style-keyed setup shadow comparison maps;
- old score/style simulations and preference tests.

## 5. Infrastructure to preserve

Whole-engine retirement must **not** delete the good architectural work that the legacy recommender currently happens to call.

### 5.1 Rules / legal domains

Preserve:

- `SetupCandidateGenerator` legality portions, but separate them from old ranking;
- `PairInformationLegalDomain`;
- `NaturalPairInformationCandidateGenerator`;
- `TroubleBrewingRegistrationDomain`;
- Mayor redirect legal/decision domains;
- Demon succession resolution/legal domain;
- `PlanLegalityValidator`;
- legal Demon-bluff role/triplet generation;
- all rules-owned role/effective-state semantics.

### 5.2 Canonical state/context

Preserve:

- `TroubleBrewingGameSnapshotV1`;
- typed decision contexts;
- DLB staged commitments;
- canonical session/reducer/planner authority;
- complete Manual legal domains.

### 5.3 Epistemic / strategic evaluation

Preserve:

- `InformationProposition` / observations;
- exact hypothetical world evaluation;
- strategic evil-topology diagnostics;
- whole-bundle and historical replay;
- feature projection with explicit unavailable/deferred semantics.

### 5.4 Versioned evidence-backed policy infrastructure

Preserve:

- `PolicyVersion`;
- score-free `PolicyEvaluation`;
- explicit rejection / preference reason codes;
- policy limitation / deferral states;
- evidence checkpoint identity;
- `DecisionTrace`;
- deterministic replay;
- neutral HOST-ML export;
- deterministic tie selection when candidates are explicitly policy-equivalent.

`WeightedStableSelector` core mathematics may remain as generic infrastructure only if a future accepted policy explicitly supplies an evidence-backed probability distribution. Its legacy style helper and arbitrary legacy weights are retirement targets.

## 6. Accepted new-policy islands that survive LRE

### 6.1 Drunk assignment — `DRUNK_ASSIGNMENT_Q04_V1`

Keep the accepted bounded Q04 policy.

It has:

- an explicit EvidenceLab checkpoint;
- a precise conditional predicate;
- a complete rules-legal Drunk domain;
- typed features;
- replay;
- an explicit compatibility fallback outside the bounded evidence condition.

That fallback is part of the accepted Q04 policy contract and is not the generic legacy score engine. It can be revisited later with more evidence, but LRE must not replace it with old setup scoring.

### 6.2 Functioning reliable Librarian — V2 production selector

Keep `FunctioningLibrarianV2ProductionSelector` on its exact admitted scope.

It:

- consumes the complete truth-only legal domain;
- uses an evidence-backed qualitative future-flexibility predicate;
- has no RecommendationStyle input;
- applies no numeric score table;
- replays against the exact C5 oracle.

However, its current **fallback to the generic legacy selector** is not part of the desired end state. Once a manual-required fallback is guaranteed, policy failure/out-of-scope should defer instead of silently returning to the legacy algorithm.

### 6.3 Frozen V1/V2 policy identities

Historical policy identities remain immutable for replay/export compatibility.

The string `BEGINNER_CONSERVATIVE_V1/V2` predates the new per-player BEGINNER/NORMAL/EXPERT model. Do not reinterpret or rename those historical IDs.

## 7. New default authority rule

The target production rule is:

```text
build current canonical decision context
-> rules generate complete legal domain

if legal domain has exactly one outcome:
    RULE_DETERMINISTIC
    -> may resolve automatically

else if an accepted versioned policy explicitly admits this surface:
    POLICY_READY
    -> policy may recommend / auto-select according to its contract

else:
    MANUAL_REQUIRED / POLICY_DEFERRED
    -> expose complete legal domain
    -> require Storyteller choice
    -> do NOT invoke legacy heuristic fallback
```

A recommendation provider being unavailable must not imply that the game action itself is unavailable.

The UI should distinguish:

- **rules cannot determine a legal action** — actual game/rules problem;
- **recommendation unavailable** — legal choices exist, Storyteller must choose.

## 8. Manual-surface readiness

The audit confirms substantial reusable Manual infrastructure already exists:

- first-night pair information has complete `ClocktowerPairManualAuthority`;
- structured numeric/categorical information already has manual candidate/result surfaces;
- Mayor redirect and Demon succession have Experienced manual ruling callbacks/surfaces;
- Red Herring has a legal first-night target-selection path;
- Spy/Recluse registration has manual/result-first interaction support.

Important remaining gaps/ownership issues:

### 8.1 Demon bluffs

Current Demon reveal waits for three bluff roles originating from the setup recommendation path.

The existing `RecommendationDecisionEditor` can edit Demon bluffs, but it is seeded from an existing legacy `RecommendationPlan`.

Therefore LRE cannot simply switch off setup recommendation and expect Demon information to remain playable.

Before old setup authority is revoked, add a **legal-only Demon-bluff manual selection surface** at/before the Demon-information barrier.

### 8.2 Beginner fallback presentation

Several Manual controls were designed for Experienced mode while Beginner currently uses invisible automatic effects.

LRE must allow the same complete legal domain to become temporarily interactive in Beginner mode whenever recommendation authority is absent.

That is a fallback UX change, not a new recommendation algorithm.

### 8.3 Setup recommendation editor

The setup editor currently edits a legacy plan rather than owning an independent legal decision form.

Do not preserve `SetupRecommendationService` just to bootstrap that editor.

Move any still-needed Red Herring / Demon-bluff manual controls onto their canonical latest-safe decision barriers.

## 9. Retirement route

### LRE-0 — whole-engine audit / policy freeze

Status: **COMPLETE / RETIREMENT AUTHORIZED**.

Actions:

- classify all legacy heuristic policy as retirement-only;
- forbid new dependencies;
- supersede RSR’s family-by-family behavior-preservation assumption;
- preserve new policy islands and legal infrastructure.

### LRE-1 — manual fallback / authority gate

Goal: make it safe to turn old recommendations off before deleting code.

#### LRE-1A — recommendation authority contract

Introduce the smallest typed boundary that can distinguish, conceptually:

- `RULE_DETERMINISTIC`;
- `POLICY_READY(policyVersion)`;
- `MANUAL_REQUIRED(reason)`.

Exact type names are not frozen by this audit.

Acceptance:

- multiple legal choices without accepted policy never silently reach legacy selectors;
- recommendation unavailability does not invalidate the legal game action;
- no new score/weight is introduced.

#### LRE-1B — close Manual fallback gaps

At minimum:

- legal-only Demon-bluff picker;
- Beginner-mode manual fallback for registration;
- Beginner-mode manual fallback for Mayor/successor;
- verify generic information Manual surfaces remain complete when automatic fallback is absent.

#### LRE-1C — cut legacy automatic authority

Once LRE-1A/B are green:

- stop automatic setup-plan selection from old `SetupRecommendationService`;
- stop temporary 90/10 registration;
- stop temporary 10/90 Mayor;
- stop temporary 4/3/2/1 successor selection;
- stop generic legacy information fallback;
- functioning Librarian V2 failure/out-of-scope -> manual-required, not generic old selection;
- keep accepted Q04/V2 policy islands.

This is the first major product-risk reduction checkpoint.

### LRE-2 — retire legacy setup ranking

After staged manual barriers are independent:

- separate remaining legal candidate generation from `SetupEvaluator`;
- remove setup score/profile ranking;
- remove three-style plan generation/diversification;
- remove score-temperature/tolerance setup selection;
- remove old recommendation-card dependency;
- preserve Red Herring/Demon-bluff legality and SDE exact evaluators.

### LRE-3 — retire legacy dynamic ranking

Remove production dependence on:

- `MalfunctionPolicy`;
- generic `RegistrationPolicy` ranking;
- `PairInformationAbilityRecommender` legacy ranking;
- `DynamicCandidateGenerator` style/pressure weighting;
- `ImpairedInformationPolicy` default 90/10 probability as automatic policy;
- `MayorRedirectRecommender` legacy scoring;
- `DemonSuccessorRecommender` legacy scoring;
- `TemporaryAutomaticStorytellerPolicy`.

Keep their rules/legal semantics only where those semantics already live in separate owners.

### LRE-4 — delete heuristic state/scoring model

After no accepted production policy consumes them as authority, remove or narrow:

- `GameBalanceEvaluator`;
- `ConsequenceEvaluator`;
- `PublicBalanceHint` if no non-legacy consumer remains;
- `evilAdvantage` if no non-legacy consumer remains;
- heuristic-only information-pressure uses;
- score tolerances;
- role recommendation metadata;
- legacy history reconstruction from localized UI titles.

Typed decision history may survive as factual enrichment, but it must not preserve the old scoring formula.

### LRE-5 — delete style / compatibility / telemetry shell

Remove:

- `RecommendationStyle`;
- `RecommendationProfiles`;
- `LegacyRecommendationStyleCompatibility`;
- style fields from models/keys/telemetry;
- style-specific labels/default flags;
- legacy style-keyed SDE comparison bridges;
- obsolete tests/simulations whose only contract is the retired engine.

### LRE-6 — final no-legacy acceptance

Repository-wide proof:

1. no production call path reaches legacy heuristic policy;
2. every automatic multi-choice decision is backed by an explicit accepted policy version;
3. every unsupported multi-choice decision remains playable through a complete legal Manual domain;
4. no legacy weight/style/balance value affects candidate ranking;
5. replay/export still reproduce accepted policy decisions;
6. rules legality remains unchanged;
7. current Recovery contract remains unchanged unless independently required;
8. full T4 green;
9. docs/archive clearly separate historical heuristic experiments from live policy authority.

## 10. Replacement-policy standard

A future recommendation may become production authority only when it has:

1. a rules-owned complete legal domain;
2. a typed pre-decision context;
3. explicit required vs enrichment inputs;
4. a versioned policy identity;
5. explicit reason / limitation / deferral semantics;
6. evidence provenance for any preference beyond hard correctness;
7. deterministic replay;
8. complete alternative preservation for evaluation/export;
9. no later-history leakage;
10. acceptance tests against source-backed evidence or an explicitly bounded policy contract.

Numeric weighting is not forbidden forever, but any number used as product policy must have a defensible calibration/evaluation chain. “It seemed reasonable” or “simulation is stable” is insufficient.

Future ModelLab/LLM ranking must enter through the same boundary. It does not receive legality authority.

## 11. Player experience after legacy retirement

Future player experience remains:

```text
BEGINNER
NORMAL
EXPERT
```

per player / per seat, current default `NORMAL`.

It is an enrichment feature for new versioned policies, not a replacement global style and not a reason to create three new hand-tuned weight tables.

A policy that does not have evidence for player-level effects should ignore the field.

When a policy does consume player level, freeze the decision-time values for replay/export.

## 12. Tests to preserve vs retire

### Preserve / strengthen

- rule legality;
- complete candidate domains;
- forced-outcome semantics;
- current canonical state/context;
- manual-domain completeness;
- exact world/strategic diagnostics;
- accepted policy predicates/reasons;
- DecisionTrace/replay;
- policy-neutral export;
- stale-context/current-domain rebind safety;
- no-history-leakage;
- deterministic equivalence tie-breaking.

### Retire / rewrite

Tests whose primary assertion is:

- exactly three recommendation styles exist;
- one style scores higher than another;
- old numeric weights yield a specific ranking;
- 90/10 or 4/3/2/1 temporary policy distributions;
- old score tolerances include/exclude a candidate;
- synthetic distribution “looks varied”;
- style appears in telemetry/IDs;
- old setup plans remain diverse.

When such a test also protects legality or candidate completeness, preserve that invariant in a policy-neutral test before deleting the obsolete assertion.

## 13. Relationship to prior routes

### RSR

RSR-0 remains useful for its style/player-level fan-out audit.

RSR-1A is COMPLETE / ACCEPTED:

- RED: `5645c6fbf2585ca23a6648e1c7e44e68cd03506d`;
- implementation GREEN: `e3e94136e3e6658f1d69ab5cd1e97114ada4238a`, CI #3739 / R2 #3438;
- exact-head T4: `d8ac254b8f40d2a5106c871dcd9eaa567ab93fde`, CI #3740 / R2 #3439 GREEN;
- PR #217 squash merge: `881d4252c1a242c5ea523718fb8f780f693e9ab5`.

The planned RSR-1B/RSR-2 “family style collapse while preserving legacy behavior” sequence is superseded by LRE. Style retirement is now one downstream part of whole legacy-engine retirement.

### SDE

The long-lived SDE architecture is reaffirmed, not replaced.

Its core principles already point away from the legacy heuristic stack:

- rules own legal outcomes;
- exact epistemic consequences precede policy;
- whole-bundle / whole-history structure matters;
- policy should reject clearly bad candidates and expose uncertainty;
- evidence rather than synthetic intuition calibrates preferences;
- `ConsequenceEvaluator` is explicitly legacy.

LRE is the production-authority cleanup required to finish that convergence.

### HOST-ML / ModelLab

HOST-ML1 neutral export remains valid.

Retiring old heuristics improves ML readiness because future datasets will not accidentally treat arbitrary legacy scores/styles as ground-truth quality labels.

## 14. Immediate next action

Do **not** start by deleting `RecommendationStyle` or `SetupRecommendationService`.

Start **LRE-1 — manual fallback / authority gate audit + tests-first implementation**.

First executable checkpoint should prove this safety invariant:

> **No multi-choice Storyteller decision without an accepted policy may be automatically selected by the legacy recommender.**

Before broad cutoff, close the legal-only Manual gaps identified above, especially Demon bluffs and Beginner-mode fallback presentation.

This sequence stops bad recommendations from controlling games earlier than a bottom-up code deletion would, while preserving a playable Host during the retirement campaign.
