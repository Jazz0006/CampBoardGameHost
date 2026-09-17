# SDE-1A — Global Fanout / Orchestration Seam Audit

> Date: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-1-orchestration-seam`  
> Live `main` baseline: `4d6e90a7a268570d261048931a3557433ea01d83`  
> Status: **ACTIVE AUDIT — no production cutover**

## 1. Authority and scope

This audit follows, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
6. live `main` at the baseline above.

SDE-1A maps the real ownership and fanout before introducing `StorytellerDecisionEngine`.

This audit does **not**:

- reopen completed FN-BUNDLE candidate-space/setup-pair audits;
- introduce a second rules engine, state model, or possible-world solver;
- perform a production caller cutover;
- implement Drunk / Spy-Recluse policy / Poisoner replanning from SDE-2;
- delete `ConsequenceEvaluator` before its callers are migrated;
- change runtime semantics.

## 2. Current live topology

The current production/experiment topology is not one recommendation pipeline. It is a set of authorities plus migration-era orchestration spread across `rules`, `recommendation`, `epistemic`, `session`, `flow`, and `UI`.

```text
ClocktowerGameSession
    canonical GameState + revisions + durable semantic history
        |
        +--> flow planner/projector
        |       interaction ordering only
        |
        +--> UI / host screen                            [legacy fanout hotspot]
                |
                +--> ClocktowerRecommendationCoordinator
                |       |
                |       +--> SetupRecommendationModule
                |       +--> NightRecommendationModule
                |       |       +--> DynamicCandidateGenerator
                |       |       |       +--> ConsequenceEvaluator   [legacy heuristic]
                |       |       +--> RegistrationPolicy
                |       |       +--> DynamicCandidateGenerator.select
                |       +--> DayRecommendationModule
                |       +--> InMemoryDecisionEventStore
                |
                +--> FirstNightInformationMigration
                +--> registration/effective-state UI adapters

rules-owned / canonical legal producers
    +--> NaturalPairInformationCandidateGenerator
    +--> FirstNightNumericInformationSemantics / FixedInformationEvaluator
    +--> SetupCandidateGenerator
    +--> registration legality / interaction semantics

legal information/effects
    +--> TroubleBrewingFirstNightInformationPropositionMaterializer
    +--> EpistemicObservation materialization
            |
            +--> ExactHistoricalHypotheticalObservationBundleEvaluator
                    BEFORE / AFTER exact worlds
                    structural diagnostics

SDE-0 experiment path
    TroubleBrewingFirstNightHealthyBundleHarness
        canonical candidate producers
        -> PUBLIC_GOOD_INFO projection
        -> ExactHistoricalHypotheticalObservationBundleEvaluator
        -> structural / leave-one-out diagnostics
```

The target SDE seam belongs **between legal candidate/effect production and session commit**, while consuming the epistemic evaluator as an authority rather than absorbing it.

## 3. Ownership classification

Classification values are the SDE-1 handoff values.

| Node / symbol | Current responsibility | Classification | SDE-1A conclusion |
| --- | --- | --- | --- |
| `ClocktowerGameSession` / `ClocktowerSessionState` | canonical `GameState`, revisions, histories, timeline and durable observation commit | **REUSE AS AUTHORITY** | SDE must read/project this state; it must never become a second writer. |
| `ClocktowerGameSession.toGameSnapshot(...)` | revision-bound epistemic snapshot projection | **REUSE AS AUTHORITY** | Preferred bridge from canonical session state to exact epistemic evaluation when a validated ruleset is available. |
| `InformationDecisionRevision` / `InformationDecisionSnapshot` | freshness and immutable candidate-space identity for information confirmation | **REUSE AS AUTHORITY** | Reuse the session revision values; do not create another revision counter. |
| `InformationDecisionContext` | shared validation/confirmation boundary for recommended vs structured-manual information choices | **REUSE AS AUTHORITY** | Strong existing confirmation seam. SDE should produce/consume its legal candidate IDs rather than bypass it. |
| `DecisionEventStore` / `DecisionRevision` | atomic event append, idempotency, status/correction archive | **REUSE AS AUTHORITY** with ownership cleanup later | Event semantics are useful, but the store is currently embedded in `ClocktowerRecommendationCoordinator`; SDE must not clone this history. |
| `NaturalPairInformationCandidateGenerator` | canonical typed pair-information domain | **REUSE AS AUTHORITY** | Keep legality here; SDE consumes candidates only. |
| `FirstNightNumericInformationSemantics` / fixed rules evaluators | healthy numeric truth and mechanical information semantics | **REUSE AS AUTHORITY** | Rules-owned truth remains outside SDE. |
| `SetupCandidateGenerator` | typed setup-level legal candidate producers such as Red Herring / Demon bluffs | **REUSE AS AUTHORITY** | Setup legality remains producer-owned. |
| `TroubleBrewingFirstNightInformationPropositionMaterializer` | already-legal TB first-night information -> `InformationProposition` | **ADAPT INTO SDE SEAM** | Useful semantic adapter; it is not legality or policy authority and is currently first-night/TB-specific. |
| `ExactHistoricalHypotheticalObservationBundleEvaluator` | exact mutation-free hypothetical consequence evaluation and structural diagnostics | **REUSE AS AUTHORITY** | This is the narrow exact consequence authority. Do not add a recommendation-owned world solver. |
| `TroubleBrewingFirstNightHealthyBundleHarness` | SDE-0 experimental composition, public projection, exact diagnostic queries, leave-one-out evidence | **OUT OF SCOPE** for production ownership; **REUSE AS EVIDENCE** | Preserve as calibration/contract evidence. Do not make the experiment harness the production engine. |
| `DynamicCandidateGenerator.generate*` | typed dynamic candidate/evaluation construction; currently also invokes legacy consequence heuristic when state is supplied | **ADAPT INTO SDE SEAM** | Split candidate/effect production from legacy consequence scoring during later migration. Do not treat current evaluation score as exact strategic consequence. |
| `DynamicCandidateGenerator.select` | impaired-information family budget + weighted stable selection | **LEGACY CALLER TO MIGRATE LATER** | Preserve behavior until SDE policy/selection cutover. Family legality/budget semantics may remain reusable independently. |
| `ImpairedInformationPolicy` | impaired truthful-vs-false semantic family budget | **REUSE AS AUTHORITY / SDE-2 INPUT** | Do not pull SDE-2 uncertainty into the first SDE-1 slice. |
| `RegistrationPolicy` / registration rules domain | currently mixes legal registration choices with recommendation helpers | **ADAPT INTO SDE SEAM** | Preserve rules-owned legal registration domain; later separate policy choice from legal-option generation. |
| `SetupRecommendationModule`, `NightRecommendationModule`, `DayRecommendationModule` | thin coordinator adapters | **LEGACY CALLER TO MIGRATE LATER** | They are routing layers, not durable ownership. New SDE should not simply wrap them wholesale. |
| `ClocktowerRecommendationCoordinator` | broad setup/night/day/history/selection/event-store facade | **LEGACY CALLER TO MIGRATE LATER** | Current fanout aggregation point, but too broad/mixed to become the final SDE authority unchanged. |
| `ConsequenceEvaluator` | soft heuristic scoring/quality-tier mutation of already-legal dynamic candidates | **DUPLICATE / RETIRE AFTER CUTOVER** | No new policy here. Exact structural diagnostics + Storyteller policy are the replacement route. |
| `DynamicGameState` | post-setup recommendation read model mixing mechanical state and heuristic state | **ADAPT INTO SDE SEAM** | Do not promote the entire type to canonical SDE context; extract genuine interaction facts from session/effective-state authority. |
| `FirstNightInformationMigration` | migration parity/publication lifecycle and displayed-observation tracking | **LEGACY CALLER TO MIGRATE LATER** | Contains useful planned/displayed lifecycle lessons, but it is a migration object and must not become a shadow game-state owner. |
| `TroubleBrewingFirstNightPrecomputeCoordinator` | exact-input prewarm/cache lifecycle only | **OUT OF SCOPE** | Scheduling/cache concern; SDE semantics should be independent of precompute policy. |
| `ClocktowerProductionFirstNightFlow` / planner / projector | canonical interaction ordering/projection | **REUSE AS AUTHORITY** | SDE resolves an interaction; flow decides when that interaction occurs. |
| `ClocktowerJudgeScreen` / host UI | currently constructs dynamic state, invokes coordinator, converts recommendations to display options, holds migration/registration transient state | **LEGACY CALLER TO MIGRATE LATER** | Major orchestration fanout hotspot. UI must eventually consume typed results rather than assemble Storyteller policy inputs itself. |

## 4. Real fanout findings

### 4.1 Canonical state and freshness

`ClocktowerGameSession` is the only acceptable canonical actual-state owner.

Its state already contains:

- `GameState`;
- `gameStateRevision`;
- `playerInputRevision`;
- decision/cross-game history;
- action timeline;
- epistemic observation log;
- semantic-history mode and global timeline cursor.

`toGameSnapshot(rulesetRef)` already projects the revision-bound state needed by the epistemic subsystem.

Therefore the SDE `DecisionContext` should be a **read-only interaction projection/reference**, not a new persistent `GameState` aggregate.

`InformationDecisionRevision(gameStateRevision, playerInputRevision)` and `DecisionRevision` duplicate the two numbers as narrow validation values, but neither should own revision advancement. The values must come from the live session.

### 4.2 Legal candidate production

Already-typed producers exist and should remain authoritative where they are rules-complete:

- pair information: `NaturalPairInformationCandidateGenerator`;
- first-night healthy numeric information: `FirstNightNumericInformationSemantics` / fixed rules evaluators;
- setup decisions: `SetupCandidateGenerator`;
- interaction registration legality: rules/registration domain;
- dynamic information: `DynamicCandidateGenerator.generateNumeric/generateCategorical/generatePairInformation` provides a useful typed transport, but currently mixes construction with `ConsequenceEvaluator` when `DynamicGenerationContext.state` is present.

The SDE seam should consume typed legal candidates/effects. It must not reconstruct role rules from UI display choices.

### 4.3 Proposition / observation construction

`TroubleBrewingFirstNightInformationPropositionMaterializer` is already deliberately thin: it converts an already-legal `EffectDraft.PlayerInformation` to `InformationProposition` and delegates mechanical structure such as Empath living neighbours to rules code.

This is the right direction for an SDE adapter layer:

```text
legal typed effect
-> semantic proposition / observation
-> exact evaluator query
```

The adapter may need a more general interaction-aware shape later, but SDE-1 should not duplicate the first-night materializer just to change package names.

### 4.4 Narrow exact evaluator API

The narrowest existing exact API suitable for SDE orchestration is:

```text
ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
    validatedRuleset,
    ExactHistoricalHypotheticalContext,
    List<ExactHypotheticalObservationBundleQuery>,
)
```

It returns either capability deferral or exact per-query diagnostics with:

- exact BEFORE world cardinality;
- exact AFTER world cardinality;
- possible Demon seats;
- distinct evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil-cover seats.

Those diagnostics already carry the primary SDE-0 structural evidence. Leave-one-out / recovery evidence is a **query-composition concern**, demonstrated by the healthy bundle harness; it does not require another solver.

### 4.5 First-night bundle fanout

The SDE-0 healthy harness is a valuable reference implementation of the intended authority chain:

```text
canonical candidate producers
-> proposition/public-observation materialization
-> exact hypothetical bundle evaluator
-> structural diagnostics
```

However it is explicitly a bounded experiment with staged exclusions and corpus concerns. It should remain calibration/evidence infrastructure, not become the runtime orchestration owner.

Production first-night information still has separate UI/migration/precompute paths. SDE-1 must bridge these gradually rather than replacing the healthy harness with a new production bundle engine.

### 4.6 Dynamic recommendation fanout

Current dynamic fanout is predominantly:

```text
ClocktowerJudgeScreen
-> ClocktowerRecommendationCoordinator
-> Night/DayRecommendationModule
-> DynamicCandidateGenerator / RegistrationPolicy / MayorRedirectRecommender / DemonSuccessorRecommender
```

The host screen also builds `DynamicGameState` itself and converts recommendation records into display options.

This is the principal production seam smell: **UI is supplying policy-era state and invoking multiple recommendation families directly**.

SDE-1 should eventually replace this with one typed interaction request/context boundary, but the first SDE-1 slice must not globally cut these callers over.

### 4.7 `ConsequenceEvaluator` direct caller and input split

The direct typed caller found in the current dynamic information path is `DynamicCandidateGenerator.evaluation(...)`:

```text
DynamicGenerationContext.state != null
-> ConsequenceEvaluator.evaluate(base, ConsequenceContext(...))
```

`ConsequenceEvaluator` consumes these signals:

| Signal | Audit interpretation |
| --- | --- |
| canonical/effective `GameState` / alive state | **genuine context input**, but source it from session/effective-state projection |
| phase / round | **genuine context input** for future pacing; already represented elsewhere even though the evaluator uses only state-derived final-day logic today |
| ability/recipient/target seats | **genuine interaction identity/input** |
| reliability / effective ability state | **genuine rules/effective-state input** |
| `isOneShotAbility` | **genuine ability property**; should come from typed rules metadata/context, not heuristic inference |
| `playerSelectedTarget` | **genuine interaction provenance** when mechanically relevant to policy explanation |
| Storyteller/table profile (`style`) | **policy input**, not mechanical state |
| `alignmentImpact` | **heuristic conclusion/tuning input**; do not copy into the canonical DecisionContext without a separately justified contract |
| `evilAdvantage` | **legacy heuristic conclusion** targeted for retirement/redefinition |
| `PublicBalanceHint` | **legacy heuristic summary**; not canonical mechanical truth |
| `playerInformationPressureBySeat` | **legacy heuristic state** unless a narrower descriptive telemetry use is independently justified |
| `MisinformationLedger` high/consecutive false counts | **legacy policy-history heuristic**; committed semantic history should be the source of truth for new exact policy inputs |
| registration ledger | potentially **genuine interaction history**, but must be derived/owned by canonical semantic history/rules rather than copied as a second authoritative ledger |

No new SDE policy should be added to `ConsequenceEvaluator`.

## 5. Lifecycle ownership

SDE-1 needs lifecycle metadata, but it must not shadow session state.

Recommended ownership:

```text
PERSISTENT
    reference to setup/session-owned durable commitments
    examples: Red Herring, Demon bluffs, durable shown-role commitments where applicable

COMMITTED
    session-owned durable event / action / observation history
    immutable input to subsequent SDE decisions

PLANNED / UNCOMMITTED
    orchestration-local recommendation identity + source revision
    disposable; never a second game fact
```

A planned recommendation should therefore minimally carry:

- stable interaction/decision identity;
- source `gameStateRevision` + `playerInputRevision`;
- candidate-space/snapshot identity where confirmation needs it;
- lifecycle kind (`PLANNED`, with persistent intent represented separately if required);
- no mutable copy of canonical game state.

On confirmation, existing session validation/commit ownership remains authoritative. SDE should return a typed result/proposal, not mutate the game directly.

## 6. Decisions still requiring legacy adaptation

The host UI currently contains several display-oriented/manual domains that are not yet one canonical SDE request shape:

- number options built from `UnreliableNumberContext`;
- categorical yes/no or role-name options;
- registration display options assembled from `SpecialRegistrationContext`;
- Mayor redirect and Demon successor recommendation display mapping;
- first-night migration objects converting typed observations into legacy reveal behavior.

These should be migrated by adapting **typed legal options to SDE requests**, not by teaching SDE to parse localized labels or `ClocktowerDisplayOption`.

## 7. Smallest first vertical slice

The smallest seam-proving slice should be **non-production-authoritative typed information consequence orchestration**, not a full first-night bundle selector and not a Mayor/registration cutover.

Proposed bounded proof:

```text
existing typed legal information candidate/effect
-> existing proposition/observation adapter
-> ExactHistoricalHypotheticalObservationBundleEvaluator
-> typed CandidateConsequence carrying exact structural diagnostics
-> thin policy-result classification shell
```

Constraints for the slice:

- use a healthy, already-supported information surface so no Drunk/Spy/Recluse/Poisoner uncertainty is added;
- no global selection cutover;
- no UI changes required to prove the seam;
- compare the SDE-produced exact diagnostics against the existing SDE-0 exact path for the same hypothetical observations;
- keep `InformationDecisionContext` as the later confirmation/freshness boundary rather than inventing a replacement.

This proves the architectural seam that matters:

```text
legal candidate
-> exact consequence
-> policy boundary
```

without prematurely solving lifecycle invalidation or replacing production recommendation routing.

## 8. Provisional SDE boundary after audit

Do **not** freeze names yet, but the narrow boundary now appears to be:

```text
StorytellerDecisionEngine
    evaluate(request, context)

DecisionContext
    session snapshot/reference
    gameStateRevision / playerInputRevision
    phase / round / interaction identity
    validated ruleset / semantic history references
    profile input
    lifecycle references (not copied state)

DecisionRequest
    typed legal candidates or a typed provider owned by the relevant rules/candidate domain
    proposition/observation materializer

CandidateConsequence
    candidate identity
    exact bundle diagnostics
    optional composed/leave-one-out diagnostics when requested

StorytellerPolicyResult
    ACCEPTABLE / BAD_TOO_STRONG / BAD_TOO_WEAK / UNCERTAIN
    selected candidate only when the policy surface actually owns selection
    reason/provenance diagnostics
```

Important: a first seam can expose evaluation without selection. Selection authority should only move when policy gates have durable evidence.

## 9. Architectural decisions from SDE-1A so far

1. **Canonical actual state:** `ClocktowerGameSession.state`; use `toGameSnapshot(...)` for exact epistemic projection.
2. **Freshness:** session `gameStateRevision` + `playerInputRevision`; reuse `InformationDecisionRevision`/snapshot semantics where information confirmation needs them.
3. **Legal outcomes:** keep existing typed rules/candidate producers; do not regenerate legality in SDE.
4. **Exact consequence:** reuse `ExactHistoricalHypotheticalObservationBundleEvaluator`.
5. **Structural evidence:** current exact world-structure diagnostics are sufficient for the SDE-0 primary topology dimensions.
6. **Lifecycle:** planned state is ephemeral; persistent/committed truth remains session-owned.
7. **Flow:** ordering stays in `flow`; SDE resolves decisions but does not decide when interactions occur.
8. **UI:** current host screen is a migration caller, not an orchestration authority.
9. **Legacy heuristic:** `ConsequenceEvaluator` is not part of the target architecture; its genuine inputs must be separated from its heuristic conclusions before caller cutover.
10. **First vertical slice:** typed healthy information candidate -> exact diagnostics -> thin policy boundary, with no production cutover.

## 10. Remaining SDE-1A audit before implementation

Before adding the first orchestration types, finish these narrow checks on the branch:

- enumerate every production call site that constructs `DynamicGameState` and every call path into `DynamicCandidateGenerator.generate*`;
- map `InformationDecisionContext` confirmation to the exact production observation-commit call so the new seam has one explicit handoff point;
- inventory registration legal-option generation separately from registration recommendation scoring;
- verify whether any non-dynamic caller reaches `ConsequenceEvaluator` indirectly;
- enumerate first-night setup/bundle production callers versus experiment-only callers to avoid accidental SDE-0 harness cutover;
- identify the exact package owner for the first new typed SDE boundary after those call-site checks.

No runtime change should begin until those remaining fanout checks are recorded here or in a follow-up SDE-1A commit.
