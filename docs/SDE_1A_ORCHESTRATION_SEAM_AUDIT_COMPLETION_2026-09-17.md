# SDE-1A — Global Fanout / Orchestration Seam Audit Completion

> Date: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-1-orchestration-seam`  
> Live `main` baseline immediately before implementation: `4d6e90a7a268570d261048931a3557433ea01d83`  
> Parent audit: `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`  
> Status: **SDE-1A FANOUT AUDIT COMPLETE — bounded seam implementation may begin**

## 1. Completion findings

The remaining call-site checks do not change the architecture recorded in the parent audit. They sharpen the migration inventory and close the blockers that were intentionally left open before code work.

### 1.1 `DynamicGameState` production ownership

Production construction is concentrated in `ClocktowerJudgeScreen.dynamicStorytellerState()` / `ClocktowerHostScreen.kt`.

That UI-owned projection combines:

- canonical/effective game facts;
- phase / round;
- spent/protected/registration interaction state;
- `playerInformationPressureBySeat`;
- `MisinformationLedger`;
- `PublicBalanceHint`;
- `evilAdvantage`.

Conclusion: `DynamicGameState` is a migration-era recommendation read model, not the future SDE context. The first SDE context must reference canonical session/epistemic inputs and must not copy the heuristic fields above as if they were game truth.

### 1.2 `ConsequenceEvaluator` production fanout

Three production call families are now confirmed:

1. `DynamicCandidateGenerator.evaluation(...)` when `DynamicGenerationContext.state != null`;
2. `RegistrationPolicy.generateCandidates(...)` after `TroubleBrewingRegistrationDomain` has produced legal registration choices;
3. `DayRecommendationModule` malfunction evaluation in `RecommendationModules.kt`.

Retiring `ConsequenceEvaluator` therefore requires migration coverage for **dynamic information + special registration + day malfunction**. Replacing only the `DynamicCandidateGenerator` path would leave a hidden duplicate consequence policy alive.

The current structured numeric information route is an important negative case: `StructuredNumericInformationAdapter` creates `DynamicGenerationContext` without `state`, so its legal candidate generation does **not** invoke `ConsequenceEvaluator` today.

### 1.3 Registration ownership split

`TroubleBrewingRegistrationDomain` remains the single Trouble Brewing authority for Spy/Recluse special-registration legality and typed `RegistrationFact` projection.

`RegistrationPolicy` is an adapter/recommendation layer around that legal domain. SDE migration must consume the domain result; it must not reproduce Spy/Recluse legality.

### 1.4 Information confirmation and durable commit

The structured information handoff is already explicit:

```text
legal typed candidates
-> InformationDecisionContext
-> Structured*InformationUiModel
-> InformationDecisionContext.confirm(currentRevision)
-> EpistemicObservationDraft
-> host callback
-> CampBoardGameHostApp.recordEpistemicObservation
-> ClocktowerGameSession.commitGlobalEpistemicObservation   [GLOBAL_V1]
```

`InformationDecisionContext` validates candidate identity and source revision but does not become a second durable-history owner. `ClocktowerGameSession` remains the commit authority.

The SDE may return evaluation/recommendation metadata keyed by the same stable candidate IDs, but it must not commit an observation or advance session revisions.

### 1.5 Production first-night versus SDE-0 experiment separation

Production first-night information uses the production request/precompute/migration path:

```text
ClocktowerFirstNightInformationRequest
TroubleBrewingFirstNightPrecomputeCoordinator
FirstNightInformationMigration
production Host/step adapters
```

The SDE-0 complete-bundle work remains an experiment/test/calibration path around:

```text
TroubleBrewingFirstNightHealthyBundleHarness
FirstNightBundle* experiment contracts/corpora/tests
ExactHistoricalHypotheticalObservationBundleEvaluator
```

The first SDE-1 implementation must reuse exact-evaluator semantics without cutting production over to the SDE-0 harness.

## 2. Frozen authority boundary for the first seam

For the first implementation slice:

- **rules / existing candidate producers own legality**;
- **`ClocktowerGameSession` owns actual state, revisions and durable commit**;
- **`ExactHistoricalHypotheticalObservationBundleEvaluator` owns exact possible-world consequence calculation**;
- **`flow` owns interaction ordering**;
- **SDE owns only orchestration of already-legal hypothetical candidates into exact consequence records**;
- **policy filtering / selection remains intentionally uncut over in this first slice**.

The package owner for the new bounded seam is:

`app/src/main/java/com/codex/campboardgamehost/clocktower/recommendation/sde/`

This placement keeps orchestration beside recommendation policy while preserving `session`, `rules`, and `epistemic` as independent authorities.

## 3. First bounded vertical slice

Implement only:

```text
DecisionContext
    read-only exact historical context
    existing gameStateRevision / playerInputRevision identity

DecisionRequest
    stable decision ID
    already-materialized, already-legal observation candidates

DecisionCandidate
    stable candidate ID
    recipient seat
    hypothetical EpistemicObservation bundle

StorytellerDecisionEngine.evaluateExactConsequences(...)
    -> ExactHistoricalHypotheticalObservationBundleEvaluator

CandidateConsequence
    stable candidate ID
    exact BEFORE / AFTER diagnostics

Ready | Deferred result
```

Explicit non-goals for this slice:

- no production UI caller cutover;
- no replacement of `InformationDecisionContext`;
- no new legality generation;
- no new world solver;
- no badness thresholds or Storyteller policy calibration yet;
- no weighted selection;
- no mutation or commit;
- no `DynamicGameState` dependency;
- no `ConsequenceEvaluator` deletion yet;
- no SDE-2 Drunk / Spy-Recluse uncertainty / Poisoner replanning work.

## 4. Tests required before any later caller migration

The seam tests must prove:

1. candidate IDs survive orchestration unchanged;
2. exact diagnostics are identical to direct calls to `ExactHistoricalHypotheticalObservationBundleEvaluator`;
3. multiple candidates are evaluated from the same immutable baseline;
4. exact capability deferral is surfaced without fallback heuristics;
5. input history / observations are not mutated;
6. malformed request/candidate identity fails before exact evaluation.

Only after this contract exists should a later SDE-1 slice wire one real structured information caller through it and compare behavior before wider migration.
