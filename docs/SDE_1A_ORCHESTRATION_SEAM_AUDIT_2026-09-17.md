# SDE-1A — Global Fanout / Orchestration Seam Audit

> Date: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-1-orchestration-seam`  
> Live `main` baseline audited: `4d6e90a7a268570d261048931a3557433ea01d83`  
> Status: **COMPLETE — no production cutover performed**

## 1. Scope and result

SDE-1A mapped the live ownership and call graph required before introducing a unified `StorytellerDecisionEngine` orchestration seam.

The audit followed, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
6. live `main` at the baseline above.

The audit did not reopen completed FN-BUNDLE candidate-space/setup-pair ownership work, did not introduce a second rules/state/world-solver authority, and did not cut production callers over.

The architectural conclusion is stable:

```text
canonical session state / effective-state authority
        ↓
rules-owned legal candidates
        ↓
typed proposition / hypothetical observation adapter
        ↓
existing exact epistemic evaluator
        ↓
exact structural consequence diagnostics
        ↓
Storyteller policy boundary
        ↓
selection only where that surface owns selection
        ↓
existing confirmation / session commit authority
```

`StorytellerDecisionEngine` belongs between legal candidate/effect production and confirmation/commit. It must compose existing authorities rather than absorb them.

## 2. Canonical ownership map

| Node / symbol | Classification | SDE conclusion |
| --- | --- | --- |
| `ClocktowerGameSession` / `ClocktowerSessionState` | **REUSE AS AUTHORITY** | Only canonical actual-state/revision/history writer. SDE reads/projects it and never becomes a second state owner. |
| `ClocktowerGameSession.toGameSnapshot(...)` | **REUSE AS AUTHORITY** | Preferred revision-bound bridge into exact epistemic evaluation. |
| `InformationDecisionRevision` / `InformationDecisionSnapshot` | **REUSE AS AUTHORITY** | Reuse session `gameStateRevision` + `playerInputRevision`; no new revision counter. |
| `InformationDecisionContext` | **REUSE AS AUTHORITY** | Existing revision-bound confirmation/freshness seam. SDE must not bypass it. |
| `DecisionEventStore` / `DecisionRevision` | **REUSE AS AUTHORITY, CLEANUP LATER** | Useful event semantics, but current coordinator ownership is migration-era; do not clone history. |
| `NaturalPairInformationCandidateGenerator` | **REUSE AS AUTHORITY** | Pair-information legality/domain remains rules-owned. |
| `FirstNightNumericInformationSemantics` / fixed rules evaluators | **REUSE AS AUTHORITY** | Healthy numeric truth/mechanics remain rules-owned. |
| `SetupCandidateGenerator` | **REUSE AS AUTHORITY** | Setup legal candidates remain producer-owned. |
| `TroubleBrewingRegistrationDomain` | **REUSE AS AUTHORITY** | Single TB Spy/Recluse registration-legality owner. |
| `TroubleBrewingFirstNightInformationPropositionMaterializer` | **ADAPT INTO SDE SEAM** | Thin legal-effect -> semantic proposition adapter; not legality/policy authority. |
| structured numeric/boolean information adapters | **ADAPT INTO SDE SEAM** | Preserve candidate IDs, typed observations and confirmation semantics; move strategic evaluation outside UI fanout. |
| `ExactHistoricalHypotheticalObservationBundleEvaluator` | **REUSE AS AUTHORITY** | Narrow exact consequence authority. Do not add a recommendation-owned solver. |
| `TroubleBrewingFirstNightHealthyBundleHarness` | **OUT OF SCOPE FOR PRODUCTION; REUSE AS EVIDENCE** | SDE-0 experiment/calibration harness, not runtime orchestration owner. |
| `DynamicCandidateGenerator.generate*` | **ADAPT INTO SDE SEAM** | Useful typed candidate/effect construction but currently mixes in legacy consequence scoring when state is supplied. |
| `DynamicCandidateGenerator.select` | **LEGACY CALLER TO MIGRATE LATER** | Preserve impaired-family budget/selection until policy cutover. |
| `ImpairedInformationPolicy` | **REUSE AS AUTHORITY / SDE-2 INPUT** | Keep SDE-2 uncertainty out of first seam. |
| `RegistrationPolicy` | **ADAPT INTO SDE SEAM** | Keep registration legality in domain; replace only downstream recommendation scoring later. |
| `SetupRecommendationModule` / `NightRecommendationModule` / `DayRecommendationModule` | **LEGACY CALLER TO MIGRATE LATER** | Routing layers, not durable ownership. |
| `ClocktowerRecommendationCoordinator` | **LEGACY CALLER TO MIGRATE LATER** | Current broad fanout facade is too mixed to become final SDE unchanged. |
| `ConsequenceEvaluator` | **DUPLICATE / RETIRE AFTER CUTOVER** | Migration-era heuristic scorer; no new SDE policy belongs here. |
| `DynamicGameState` | **ADAPT INTO SDE SEAM** | Mixed mechanical + heuristic read model; never promote wholesale to canonical `DecisionContext`. |
| `FirstNightInformationMigration` | **LEGACY CALLER TO MIGRATE LATER** | Useful lifecycle lessons only; not a future shadow state owner. |
| `TroubleBrewingFirstNightPrecomputeCoordinator` | **OUT OF SCOPE** | Cache/prewarm scheduling only. |
| `ClocktowerProductionFirstNightFlow` / planner / projector | **REUSE AS AUTHORITY** | Flow owns ordering; SDE resolves one interaction. |
| `ClocktowerJudgeScreen` / host UI | **LEGACY CALLER TO MIGRATE LATER** | Primary production orchestration fanout hotspot; eventually caller-only. |

## 3. Canonical state, revisions and durable commit

`ClocktowerGameSession` already owns:

- canonical `GameState`;
- `gameStateRevision`;
- `playerInputRevision`;
- decision/cross-game history;
- action timeline;
- epistemic observation log;
- semantic-history mode / global timeline cursor.

Therefore future `DecisionContext` is a read-only interaction projection/reference, not another persistent state aggregate.

The structured information production chain already provides the required confirmation/commit boundary:

```text
ClocktowerStructuredInformationPreparation
→ StructuredNumericInformationAdapter
→ ClocktowerRecommendationCoordinator.resolveNumberInformation
→ DynamicCandidateGenerator.generateNumeric
→ ClocktowerRecommendationCoordinator.informationDecisionContext
→ StructuredNumberInformationUiModel
→ InformationDecisionContext.confirm(currentRevision)
→ ClocktowerHostScreen.recordReliablePrivateInformation
→ onRecordEpistemicObservation(confirmation.draft)
→ CampBoardGameHostApp.recordEpistemicObservation
→ ClocktowerGameSession.commitGlobalEpistemicObservation
```

SDE must sit before confirmation/commit and return typed evaluation/recommendation results. It must never write semantic history directly.

`LEGACY_LOCAL` remains compatibility only and is not justification for another commit owner.

## 4. Legal candidate and registration ownership

Existing typed legal producers are sufficient to preserve rules ownership:

- pair information: `NaturalPairInformationCandidateGenerator`;
- healthy first-night numeric truth: `FirstNightNumericInformationSemantics` / fixed rules evaluators;
- setup choices: `SetupCandidateGenerator`;
- special registration: `TroubleBrewingRegistrationDomain`.

Registration is explicitly split:

```text
TroubleBrewingRegistrationDomain
    legal actual/special registration candidates
    typed RegistrationFact projection

RegistrationPolicy
    recommendation adaptation
    style/discussion/pressure/history/balance heuristics
    ConsequenceEvaluator
    selection
```

A later SDE migration therefore consumes the legal domain and replaces downstream policy; it does not rewrite Spy/Recluse legality.

## 5. Semantic adapter and exact consequence authority

The desired adapter shape already exists in pieces:

```text
legal typed effect
→ InformationProposition / EpistemicObservation
→ ExactHistoricalHypotheticalObservationBundleEvaluator
```

`TroubleBrewingFirstNightInformationPropositionMaterializer` is deliberately thin and delegates mechanics back to rules code. Structured numeric/boolean adapters likewise create typed propositions and observation drafts.

The narrow exact API is:

```text
ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(
    validatedRuleset,
    ExactHistoricalHypotheticalContext,
    List<ExactHypotheticalObservationBundleQuery>,
)
```

It already returns exact BEFORE/AFTER world cardinality and structural diagnostics including:

- possible Demon seats;
- distinct evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil-cover seats.

Leave-one-out / recovery is query composition, already proven by the SDE-0 healthy bundle harness. No second solver is needed.

## 6. Completed production fanout inventory

### 6.1 `DynamicGameState`

The production construction hotspot is `ClocktowerJudgeScreen`. The UI currently assembles the migration-era dynamic read model and then invokes recommendation families through `ClocktowerRecommendationCoordinator`.

This confirms the architectural smell is orchestration fanout in the host layer, not missing rules ownership.

`MayorRedirectRecommender` and `DemonSuccessorRecommender` consume heuristic `DynamicGameState` fields such as public-balance, information-pressure and evil-advantage summaries. They are later caller-migration targets, not the first exact-information seam.

### 6.2 `DynamicCandidateGenerator.generate*`

Production dynamic information routes through the coordinator/night-module path. Structured numeric Foundation also uses `DynamicCandidateGenerator.generateNumeric`, but its adapter constructs `DynamicGenerationContext` without `state`.

That negative finding matters: the structured numeric path does **not** currently invoke legacy `ConsequenceEvaluator`, making it a clean low-coupling seam proof.

### 6.3 `ConsequenceEvaluator` production callers

The source-wide audit identified three production caller families:

```text
1. DynamicCandidateGenerator.evaluation(...)
   when DynamicGenerationContext.state != null
   → ConsequenceEvaluator.evaluate(...)

2. RegistrationPolicy.generateCandidates(...)
   legal TroubleBrewingRegistrationDomain candidates
   → legacy registration scoring
   → ConsequenceEvaluator.evaluate(...)

3. DayRecommendationModule malfunction path
   → ConsequenceEvaluator.evaluate(...)
```

Retirement therefore cannot be accomplished by migrating only dynamic information or only registration. All three obligations must be covered before deletion.

Genuine future context inputs include canonical/effective game state, phase/round, ability/recipient/target identity, effective reliability, typed one-shot metadata, player-selected-target provenance and profile/style input.

Do **not** promote legacy heuristic conclusions wholesale into `DecisionContext`, including:

- `alignmentImpact`;
- `evilAdvantage`;
- `PublicBalanceHint`;
- `playerInformationPressureBySeat`;
- `MisinformationLedger` counters;
- duplicated registration history that can instead be derived from canonical semantic history/rules.

## 7. Production first-night versus SDE-0 harness

The production first-night path and SDE-0 experiment harness are separate owners:

```text
production
    ClocktowerProductionFirstNightFlow
    ClocktowerFirstNightInformationRequest / structured adapters
    FirstNightInformationMigration
    TroubleBrewingFirstNightPrecomputeCoordinator

experiment / evidence
    TroubleBrewingFirstNightHealthyBundleHarness
        canonical candidate producers
        → PUBLIC_GOOD_INFO projection
        → ExactHistoricalHypotheticalObservationBundleEvaluator
        → structural / leave-one-out diagnostics
```

The harness remains calibration/contract evidence only. The first SDE seam must not accidentally cut production over to the experiment harness.

## 8. Lifecycle ownership

SDE-1 requires lifecycle metadata without shadowing session state:

```text
PERSISTENT
    references to setup/session-owned durable commitments

COMMITTED
    session-owned durable event/action/observation history

PLANNED / UNCOMMITTED
    orchestration-local recommendation identity + source revision
    disposable and never a second game fact
```

A planned record should carry only stable decision/interaction identity, source revisions, candidate-space/snapshot identity where needed, and lifecycle kind. It should not carry a mutable copy of canonical state.

## 9. Final SDE-1A boundary

The first durable orchestration shape is now sufficiently constrained:

```text
StorytellerDecisionEngine.evaluate(request, context)

DecisionContext
    revision-bound session snapshot/reference
    gameStateRevision / playerInputRevision
    phase / round / interaction identity
    validated ruleset / semantic-history references
    profile input
    lifecycle references, not copied state

DecisionRequest
    typed legal candidates or typed legal provider
    proposition/observation materializer

CandidateConsequence
    candidate identity
    exact bundle diagnostics
    optional composed / leave-one-out diagnostics

StorytellerPolicyResult
    ACCEPTABLE | BAD_TOO_STRONG | BAD_TOO_WEAK | UNCERTAIN
    selected candidate only when the policy surface owns selection
    reason/provenance diagnostics
```

Names remain implementation details until SDE-1B tests pin the smallest useful contract.

Package ownership should be under `clocktower/recommendation` with a dedicated `sde` subpackage. `session` remains state/commit authority and `epistemic` remains exact-evaluation authority.

## 10. First implementation slice chosen by the audit

The first seam proof is a **healthy structured-information exact-consequence evaluation**, with structured numeric as the preferred initial fixture.

Why this slice:

- already-typed legal candidates;
- stable candidate IDs;
- revision validation through `InformationDecisionContext`;
- typed observation materialization;
- durable session commit handoff already exists;
- existing exact evaluator can consume the same hypothetical observation;
- current structured numeric path does not invoke legacy `ConsequenceEvaluator`;
- no Drunk / Spy / Recluse / Poisoner uncertainty required;
- no UI or production selection cutover required.

Required proof:

```text
existing healthy legal candidate/effect
→ SDE request/context
→ existing semantic observation adapter
→ ExactHistoricalHypotheticalObservationBundleEvaluator
→ typed CandidateConsequence
```

The focused test should compare the SDE consequence output with the same exact hypothetical observation evaluated directly through the existing exact authority. This is an orchestration differential test, not a new oracle.

## 11. SDE-1A completion decisions

1. `ClocktowerGameSession` remains the only canonical actual-state and durable semantic-history owner.
2. Session revisions remain the only freshness source.
3. Rules/candidate domains remain legal-outcome owners.
4. `TroubleBrewingRegistrationDomain` remains registration-legality owner.
5. `ExactHistoricalHypotheticalObservationBundleEvaluator` remains exact consequence authority.
6. SDE-0 healthy bundle harness remains experiment/evidence infrastructure, not runtime owner.
7. Flow remains interaction-ordering owner.
8. `InformationDecisionContext` remains confirmation/freshness owner for structured information.
9. Host UI/coordinator modules are migration callers, not target authorities.
10. `ConsequenceEvaluator` is targeted for retirement only after all three production caller families are migrated.
11. The first SDE seam is evaluation-only; selection authority does not move prematurely.
12. The first seam uses a healthy structured-information fixture and performs no production cutover.
13. SDE orchestration belongs under recommendation/SDE; session and epistemic ownership stay unchanged.

## 12. Validation and closeout

SDE-1A was documentation/audit-only. No runtime semantics changed and no Gradle/runtime suite was required for the audit commits under the testing strategy.

The next executable work is **SDE-1B / SDE-1C**:

1. add the smallest typed orchestration contracts;
2. add a focused exact-consequence orchestration differential test;
3. implement the bounded healthy structured-information evaluation seam;
4. do not change UI, selection, commit ownership or production routing yet.

> **SDE-1A is COMPLETE. The next task is to implement the smallest recommendation-owned exact-consequence orchestration seam over existing legal-candidate and epistemic authorities, beginning with a healthy structured-information fixture and no production cutover.**
