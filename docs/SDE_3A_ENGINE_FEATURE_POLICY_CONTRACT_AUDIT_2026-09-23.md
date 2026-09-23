# SDE-3A — Engine / Feature / Policy Contract Architecture & Fanout Audit

> Date: 2026-09-23 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Checkpoint PR #151: merged  
> Continuation branch: `sde-3a-feature-projection-shadow-pipeline`  
> Continuation PR: #152 (draft)
> Base: live `main` at `c5b6e4d6f1dec8a68e7df3e8989e3b47425c080f`
> Status: architecture pre-flight and first score-free contract checkpoint complete; structured feature-projection continuation in progress

## 1. Boundary correction

SDE-2D5 is merged. Evidence collection no longer blocks SDE-3A/B/C.

The current route is:

```text
SDE-3A engine / feature / policy contract
SDE-3B BEGINNER_CONSERVATIVE_V1 interpretable policy
SDE-3C shadow recommendation / DecisionTrace / replay
SDE-3D calibrated policy freeze                 BLOCKED ON EVIDENCE
SDE-3E automatic production cutover             BLOCKED ON 3D
```

Older statements that PR #150 is still draft or that all of SDE-3 is blocked are historical drift and are not authority for this branch.

SDE-3A must not:
- create a second rules engine;
- take ownership of player-controlled choices;
- invent final numeric weights/gates;
- create a global scalar score;
- mutate canonical session state from shadow evaluation;
- encode fixture-specific or named-role coherence hacks.

## 2. Current ownership map

| Responsibility | Current production owner | SDE-3A disposition |
| --- | --- | --- |
| canonical game/session state | `ClocktowerGameSession` / `ClocktowerSessionState` | keep |
| game/input revisions | `ClocktowerGameSession` | keep; SDE references only |
| durable semantic action history | `ActionFactTimeline` through session-owned commit APIs | keep |
| durable information history | `EpistemicObservationLog` through `commitGlobalEpistemicObservation` | keep |
| structured information candidate snapshot / confirmation | `InformationDecisionContext` / `InformationDecisionSnapshot` | keep |
| exact hypothetical consequence | `ExactHistoricalHypotheticalObservationBundleEvaluator` | keep |
| topology-first hypothetical consequence | `TroubleBrewingTopologyHypotheticalBundleEvaluator` | keep |
| strategic quotient | `StrategicWorldKey` and exact structure diagnostics | keep |
| normalized strategic projection | `NormalizedStrategicDiagnosticsProjector` | reuse beneath `DecisionFeatures` |
| current SDE orchestration | `StorytellerDecisionEngine.evaluateExactConsequences` | extend only as thin orchestration |
| current structured SDE shadow | `StructuredInformationProductionShadow` + `StructuredInformationShadowAdapter` | first SDE-3A migration seam |
| current setup recommendation | `SetupRecommendationService` / `SetupEvaluator` | production authority during shadow migration |
| setup legal candidate generation | `SetupCandidateGenerator` | keep legality authority |
| current later/dynamic recommendation | `DynamicCandidateGenerator`, `ConsequenceEvaluator`, role/dynamic policies | production authority during shadow migration |
| UI recommendation presentation | Host presentation adapters / `ClocktowerRecommendationPresentation` / storyteller UI | presentation only; do not move policy here |
| legacy decision audit/history | `StorytellerDecisionEvent`, `DecisionEventStore` | compatibility; do not turn into canonical SDE trace state |

The existing `domain.DecisionCandidate<T>` is a legacy shared recommendation candidate and the existing `DecisionEvaluation<T>` contains quality tier, integer score, probability weight, pressure, warnings, and explanation codes. It has broad setup/dynamic fanout. SDE-3A must not silently redefine this type into the new policy contract.

The long-term conceptual name can remain “DecisionCandidate”, but the migration type should use an unambiguous SDE-specific name until the legacy type is retired.

## 3. Legal candidate producer map

### Pair information — Washerwoman / Librarian / Investigator

- natural/registered healthy truth: `NaturalPairInformationCandidateGenerator.generateHealthyInformationSpace`;
- complete player-visible legal domain under impairment: `PairInformationLegalDomain`;
- Spy/Recluse interaction legality: `TroubleBrewingRegistrationDomain`;
- semantic proposition: `TroubleBrewingFirstNightInformationPropositionMaterializer`.

SDE consumes these candidates; it does not regenerate pair geometry, registration legality, or truth.

### Chef / Empath numeric information

- mechanically healthy truth values: `FirstNightNumericInformationSemantics`;
- player-visible legal result domain under impairment: `FirstNightNumericLegalDomain`;
- proposition materialization: `TroubleBrewingFirstNightInformationPropositionMaterializer`.

### Fortune Teller

- target legality and healthy result: `FortuneTellerInformationSemantics`;
- target choice is player-controlled and is a fixed committed input to SDE;
- Red Herring is a setup precommit from `SetupCandidateGenerator`;
- if the source is impaired, SDE may later choose among legal result values only after the player targets are fixed.

### Spy / Recluse

`TroubleBrewingRegistrationDomain` is the single Trouble Brewing owner for interaction-local registration legality and typed `RegistrationFact` projection.

Registration is not a permanent alternate identity and must remain bound to the detecting interaction.

### Drunk

The perceived ability determines which existing legal display domain applies. Current first-night bundle code already reuses pair/numeric/boolean legal domains rather than inventing Drunk-specific rules legality.

Future SDE owns strategic/narrative selection among those legal outcomes, not role legality.

### Poisoned information

Poisoner target selection is player-controlled and is canonical action/history input. SDE must not choose the target.

Once a source is poisoned, legal result/display space comes from the same role semantics plus impairment legality/policy seams. SDE may eventually choose the result while preserving the committed target as an input reference.

### Red Herring

`SetupCandidateGenerator.generateRedHerringCandidates` owns setup legality. The current `SetupEvaluator` still ranks candidates using legacy role metadata such as `redHerringSuitability` / exposure sensitivity. This ranking is production-authoritative today but is a retirement target, not the SDE-3 feature model.

Red Herring must migrate as a contextual setup precommit. No fixed neighbour bonus, Chef bonus, or fixed role ranking is introduced in SDE-3A.

### Demon bluffs

- legality: `SetupCandidateGenerator.generateDemonBluffCandidates`;
- current visible ranking: `SetupRecommendationService`, including legacy `bluffDifficulty`;
- joint strategic diagnostics: `TroubleBrewingDemonBluffJointOutputEvaluator`;
- setup-to-SDE projection: `SetupDemonBluffJointOutputAdapter`;
- shadow attachment: `DemonBluffSetupShadowAdapter`.

The three roles are one joint output. Once shown/locked they become persistent committed input and must not be replanned.

### Later-night Storyteller-controlled information

Current production recommendation still flows through the dynamic generation/policy stack (`DynamicCandidateGenerator`, `MalfunctionPolicy`, `RegistrationPolicy`, `ConsequenceEvaluator`) and then through shared information confirmation where migrated.

SDE-3A should migrate these surfaces after the structured first-night seam, reusing their rules/legal producers rather than reproducing them.

### Player-controlled choices

At minimum:
- Fortune Teller targets;
- Poisoner target.

These are inputs to consequence/policy evaluation, never SDE-selected candidates.

## 4. Reusable consequence / feature primitives

| Needed SDE feature family | Existing primitive | Reuse status |
| --- | --- | --- |
| Demon cover retention | exact/topology `ExactWorldStructureDiagnostics.possibleDemonSeats`; `NormalizedStrategicDiagnostics` | ready |
| Evil topology retention | `StrategicWorldKey`, exact/topology strategic-world sets | ready |
| Evil cover retention | exact/topology `evilCoverSeats`; normalized projector | ready |
| forced-Good effect | exact/topology `forcedGoodSeats`; normalized forced-Good fraction | ready |
| forced-Evil effect | exact/topology `forcedEvilSeats`; Drunk marginal diagnostics | primitive ready; SDE feature projector can normalize it without widening the legacy diagnostics contract |
| exact hypothetical bundle | `ExactHistoricalHypotheticalObservationBundleEvaluator` | ready |
| topology-first bundle | `TroubleBrewingTopologyHypotheticalBundleEvaluator` | ready |
| registration witness | `ExactRegistrationWitnessBinding` + registration domain | ready |
| public-good information projection | `FirstNightPublicGoodInfoProjection` | ready for its explicit experiment profile only |
| healthy whole-bundle comparison | `FirstNightBundleHealthyHarness` family | reusable experiment primitive |
| impaired whole-bundle marginal | `TroubleBrewingFirstNightDrunkWholeBundleExactEvaluator` | ready; role-agnostic consequence shape should be reused |
| Demon-bluff joint support | role support + union/shared topology/strategic patterns | ready |
| confirmation-chain impact | legacy score reasons exist, but no canonical SDE feature primitive yet | gap |
| healthy-information utility | public projection + whole-bundle diagnostics are inputs; final feature contract not yet unified | partial |
| truth danger / credibility disruption | evidence supports dimension; no canonical general production projector yet | gap |
| role-function exposure | policy synthesis defines dimension; legacy metadata is not sufficient authority | gap |
| semantic truth | existing `SemanticTruth` / truth relation seams | ready as descriptive input |
| impaired narrative coherence / detectability | semantic history exists; shared role-agnostic projector not yet implemented | gap |
| bluff claim burden / route diversity / collision/support | joint-output structural support exists; claim burden/collision policy not yet canonical | partial |
| future flexibility | future candidate/history semantics exist, but no canonical feature projector yet | gap |

SDE-3A should expose missing dimensions explicitly as “not projected / unavailable” rather than fill them with invented numbers.

## 5. Recommended SDE DecisionCandidate contract

Do not replace the legacy `domain.DecisionCandidate<T>` in the first slice.

Introduce an SDE-specific envelope whose long-term conceptual contract is:

```text
DecisionCandidate
  decisionId
  candidateId
  lifecycleStage
  sourceInteraction
    phase
    round
    sourceSeat?
    abilityRole?
    interactionId
  sourceRevision
    gameStateRevision
    playerInputRevision
  committedInputRefs[]
  playerControlledInputRefs[]
  legalOutcomeIdentity
  hypotheticalEffectRef
  legalityProvenance
    ownerId
    candidateSpaceIdentity
    candidateSchemaVersion?
```

Rules:
- IDs/provenance reference existing owners; they do not copy GameState.
- committed inputs identify already-made setup/history decisions.
- player-controlled inputs are recorded separately so policy cannot accidentally take ownership.
- the hypothetical effect references already-materialized propositions/effects; the SDE envelope does not reconstruct legality.
- source revision binds the candidate to one canonical historical state.

For the migration, prefer the class name `SdeDecisionCandidate` (or equivalent) until legacy `DecisionCandidate<T>` retirement makes the shorter name safe.

## 6. Recommended DecisionFeatures contract

No total score and no implicit ordering.

`DecisionFeatures` should independently expose:

1. strategic topology:
   - Demon-cover retention;
   - Evil-topology retention;
   - Evil-cover retention;
   - forced-Good seats/fraction;
   - forced-Evil seats/fraction.
2. confirmation-chain impact.
3. healthy-information utility.
4. truth danger / credibility disruption.
5. role-function exposure.
6. semantic truth.
7. impaired narrative:
   - coherence;
   - detectability.
8. bluff:
   - claim burden;
   - narrative-route diversity.
9. relationships:
   - collision;
   - support.
10. future flexibility.

Use a typed availability wrapper such as:

```text
FeatureProjection<T>
  Projected(value)
  Unavailable(reason)
```

This prevents “0” from ambiguously meaning neutral, missing, unsupported, or not applicable.

Descriptive feature payloads should carry exact sets/counts/relationships/reason codes where possible. Do not collapse the feature vector to `w1*A + w2*B + ...`.

## 7. Recommended PolicyEvaluation contract

A per-candidate policy result should contain:

```text
PolicyEvaluation
  candidateId
  policyVersion
  disposition
    REJECTED
    ACCEPTED
    SURVIVOR
  rejectionReasons[]
  softPreferenceReasons[]
  equivalenceState
    NOT_EVALUATED
    UNIQUE
    TIED_WITH(candidateIds)
```

Semantics:
- REJECTED: removed by an explicit hard/near-hard policy rule;
- ACCEPTED: passed hard gates but is not in the final soft-priority survivor band;
- SURVIVOR: remains in the final equivalence band eligible for seeded selection.

The policy layer should use stable typed reason codes. A later `BEGINNER_CONSERVATIVE_V1` selector performs seeded randomness only among survivors.

## 8. Persistent impaired narrative owner

Required owner shape:

```text
canonical semantic history
      +
committed misinformation / registration observations
      +
current legal candidate proposition
      ↓
shared role-agnostic narrative projector
      ↓
coherence + detectability features
```

It must not live in:
- Drunk Empath code;
- Drunk Fortune Teller code;
- role-specific fixture branches;
- setup templates.

Role-specific code owns only:
- rules legality;
- proposition semantics.

The shared history/policy layer owns:
- prior committed misinformation;
- cross-night continuity;
- perceived-world coherence;
- impairment detectability.

The existing semantic action/observation timeline is the source history. SDE must not create a second canonical history store.

## 9. DecisionTrace ownership / persistence

`DecisionTrace` is a recommendation-side diagnostic sidecar, not part of canonical GameState.

Minimum long-term fields:

```text
traceId
decisionId
policyVersion
evidenceCorpusCheckpoint
stateRevision
legalCandidateIds
projectedFeaturesByCandidate
policyEvaluationByCandidate
recommendedCandidateId?
actualCommittedCandidateId?
humanOverride
overrideRationale?
```

Persistence rules:
- trace references canonical game/revision/history identities;
- trace never embeds or owns a mutable GameState;
- actual commitment remains owned by `ClocktowerGameSession` and existing commit transaction/lifecycle seams;
- trace may later be annotated with the actually committed candidate after the canonical commit succeeds;
- stored trace feature values describe the historical policy run, but replay must reconstruct legal candidates and consequences from canonical history using current production legality.

Replay:

```text
same canonical historical game/revision
        ↓
rebuild legal candidates with production owners
        ↓
re-project features
        ↓
Policy V1 / V2 / V3
        ↓
separate DecisionTrace results
```

A future `DecisionTraceRepository` should therefore be separate from `ClocktowerSemanticHistoryPersistence` and from `DecisionEventStore`.

## 10. Legacy owner migration / retirement map

| Owner/surface | Classification now | Migration |
| --- | --- | --- |
| rules/legal domains | A production authority | retain |
| `InformationDecisionContext` / session commit | A production authority | retain |
| `SetupRecommendationService` / `SetupEvaluator` | A production authority | shadow SDE beside it, later retire policy authority |
| setup `bluffDifficulty` | A production authority + C retirement target | keep until SDE bluff policy cutover |
| setup Red-Herring role suitability/exposure metadata | A production authority + C retirement target | keep until contextual SDE RH cutover |
| `DynamicCandidateGenerator` | A production authority + C migration target | migrate decision families incrementally |
| `ConsequenceEvaluator` | A production authority + C retirement target | replace with explicit feature + policy reasons |
| `evilAdvantage` | A legacy policy input + C retirement target | do not propagate into new SDE contract |
| `PublicBalanceHint` | A legacy state/policy input + C retirement target | do not propagate into new SDE contract |
| player-information pressure / misinformation scalar heuristics | A current legacy policy input + C retirement target | replace only after feature/policy migration |
| `StorytellerDecisionEngine` exact consequence seam | B shadow authority for SDE consequence | extend |
| structured information shadow adapters | B shadow | first migration |
| Demon bluff joint evaluator/adapter | B shadow | later 3A/3B migration |
| legacy `StorytellerDecisionEvent` score/probability audit fields | B compatibility/history | do not use as new trace schema |
| delete-ready production code | D | none established by this audit; deletion requires separate fanout proof |

## 11. First shadow migration surface

Best first surface: **structured first-night numeric information (Chef / Empath), using the existing structured information decision context**.

Why:
- complete legal candidate identity already exists;
- semantic observation drafts already exist;
- revision/freshness ownership is explicit;
- the exact production shadow bridge already exists;
- canonical commit ownership is proven separate;
- no player-controlled target selection is needed;
- it avoids pair-registration witness complexity for the first slice;
- it can exercise the new candidate/features/policy contracts without changing visible recommendations.

Fortune Teller should follow only after target bindings are treated as committed player-controlled inputs. Pair information should follow after the SDE envelope carries registration-witness provenance cleanly. Demon bluffs and Red Herring remain setup-specific shadow migrations.

## 12. Minimal SDE-3A implementation slice

1. add SDE-specific typed candidate identity/provenance contract without modifying legacy `domain.DecisionCandidate<T>`;
2. add typed `DecisionFeatures` availability contract;
3. expose the already-existing normalized strategic diagnostics through the new feature contract;
4. add forced-Evil normalized projection inside the SDE feature projector from the already-existing exact structure primitive;
5. add typed `PolicyEvaluation` / policy-version / reason / equivalence contracts only — no policy scoring yet;
6. adapt the existing structured-information shadow to preserve candidate identity, revision, legality provenance, and exact hypothetical reference;
7. keep visible recommendation, confirmation, commit, and UI unchanged;
8. do not add DecisionTrace persistence until 3C.

## 13. Test strategy

Tests-first focus:

### T1 contract tests
- candidate IDs unique and nonblank;
- lifecycle/source/revision provenance preserved;
- player-controlled bindings cannot be mistaken for SDE-owned choices;
- feature availability distinguishes missing vs projected;
- policy evaluation reason/disposition invariants;
- no score/probability field exists in the SDE policy contract.

### T1 projector tests
- existing exact and topology-first structure project to identical strategic feature values;
- forced-Evil fraction is baseline/player-count normalized like forced-Good;
- empty baselines remain explicit `Undefined`, never NaN/infinity.

### T1 shadow adapter tests
- legal candidate order/IDs equal the source `InformationDecisionSnapshot`;
- source revision and semantic identity are preserved;
- no extra legal candidates appear;
- shadow evaluation does not mutate visible choices or session state.

### T2 integration
Required if the structured production shadow adapter changes across session/UI boundaries.

### T3 semantic differential
Not required for a contract-only/projection slice that delegates unchanged exact semantics. Required later if proposition/world/topology semantics change.

### T4
Before SDE-3A acceptance, run the repository full gate per `TESTING_STRATEGY.md`.

## 14. Risks / fanout

### High risk
- renaming/redefining legacy `domain.DecisionCandidate<T>`: broad setup/dynamic/session fanout and score coupling;
- moving legality into SDE: creates a second rules engine;
- persisting trace inside GameState/semantic history: creates competing canonical state;
- allowing SDE to choose FT/Poisoner targets: ownership violation.

### Medium risk
- `InformationDecisionContext` currently consumes legacy `DecisionEvaluation<T>`; later migration must separate legal candidate transport from legacy ranking without breaking manual confirmation;
- setup Red Herring / bluff ranking remains intertwined with `SetupEvaluator`;
- later-night dynamic paths still depend on legacy pressure and alignment heuristics;
- impairment narrative needs historical semantics across role changes/poison boundaries and cannot be implemented as a first-night-only shortcut.

### Low risk for first slice
- additive SDE contracts;
- additive feature projection over existing exact diagnostics;
- additive shadow metadata beside unchanged visible recommendation.

## 15. SDE-3A exit direction

PR #151 established the stable score-free SDE envelope and feature/policy result contract. PR #152 continues the same milestone by proving the legal-candidate -> exact-consequence -> `DecisionFeatures` path in the structured numeric shadow, including explicit deferred-capability handling, before SDE-3B begins.

SDE-3B can then implement `BEGINNER_CONSERVATIVE_V1` as explicit reasoned filtering/preferences over those features. SDE-3C can persist and replay traces. Evidence calibration remains an offline/versioned input to later policy versions and SDE-3D, not an online learner.
