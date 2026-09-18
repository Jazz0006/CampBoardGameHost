# SDE-1B/1C Exact-Consequence Seam Note

> Date: 2026-09-17 Australia/Sydney  
> Branch: `sde-1-orchestration-seam`  
> PR: #144  
> Status: **COMPLETE ON BRANCH — no production cutover**

## Scope

This first executable SDE slice is deliberately evaluation-only.

Implemented flow:

```text
already-legal / already-materialized hypothetical observation bundle
→ ExactConsequenceRequest + ExactConsequenceContext
→ StorytellerDecisionEngine.evaluateExactConsequences(...)
→ ExactHistoricalHypotheticalObservationBundleEvaluator
→ CandidateConsequence
```

The seam does not own or change:

- legal candidate generation;
- proposition semantics;
- exact world solving;
- production candidate selection;
- UI routing;
- `InformationDecisionContext` confirmation;
- session semantic-history commit;
- flow ordering;
- Drunk / Spy-Recluse / Poisoner uncertainty.

## Why the bounded contracts are not named `StorytellerDecisionRequest`

The repository already contains `clocktower/domain/DecisionRequest.kt` with a `StorytellerDecisionRequest` that embeds `DynamicGameState` plus migration-era algorithm/config fields.

SDE-1A explicitly classified `DynamicGameState` as a migration read model that mixes canonical/effective game facts with heuristic summaries. Reusing that request as the new SDE boundary would preserve exactly the ownership coupling SDE-1 is intended to remove.

The first seam therefore uses intentionally narrow names:

```text
ExactConsequenceContext
ExactConsequenceRequest
ExactConsequenceCandidate
ExactConsequenceEvaluation
CandidateConsequence
```

These are bounded SDE-1B/1C envelopes, not a competing final global request/state model. They carry only what exact consequence orchestration requires and do not copy `DynamicGameState`, selection weights, heuristic pressure/balance state, lifecycle mutation, or durable session history ownership.

`ExactConsequenceCandidate` carries a non-empty `List<EpistemicObservation>` rather than a single observation so the orchestration seam preserves the existing whole-bundle contract instead of forcing a later API break.

The existing generic `domain.DecisionCandidate<T>` is also not reused directly in this first seam because it carries candidate-family, ability-state, truth-relation, registration/effect and recommendation-era metadata beyond the exact-evaluation boundary. A later caller adapter may project from an existing legal `DecisionCandidate<T>` into `ExactConsequenceCandidate` without moving legality ownership.

## Focused contract

`StorytellerDecisionEngineTest` uses healthy first-night structured numeric observations and proves:

1. a single Empath candidate returns the same exact BEFORE/AFTER cardinality and structural diagnostics as direct invocation of the existing exact evaluator;
2. repeated evaluation is deterministic and preserves candidate identity;
3. a multi-observation bundle is forwarded as one candidate consequence and matches direct bundle evaluation;
4. multiple candidates are evaluated from the same immutable exact context and each matches its direct exact-evaluator result;
5. action timeline and observation log are not mutated;
6. exact capability deferral is surfaced unchanged, with no heuristic fallback;
7. duplicate candidate identity fails at the bounded request boundary before exact evaluation.

The test does not implement another world oracle. `ExactHistoricalHypotheticalObservationBundleEvaluator` remains the sole consequence authority used for differential comparison.

## Production ownership remains unchanged

No production caller has been cut over in this slice.

In particular:

- `ClocktowerGameSession` remains canonical state/revision/durable semantic-history owner;
- `InformationDecisionContext` remains structured-information confirmation/freshness owner;
- rules/candidate domains remain legality owners;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` remains world-consequence owner;
- flow remains ordering owner;
- host UI/coordinator remain current production callers;
- `ConsequenceEvaluator` remains present until its three production caller families are migrated later.

## Validation

Executable validation head:

`a0c4fe2a2d1c1c94daedd976285cd29e40a48ee6`

PR #144 validation on that head:

```text
R2 main-thread boundary        SUCCESS   run 2556
Android FAST unit tests        SUCCESS   CI run 2787
CI gate                        SUCCESS   CI run 2787
ASP contract tests             SKIPPED   not selected for recommendation-only surface
Real Clingo cross-validation   SKIPPED   not selected for recommendation-only surface
```

The Android job compiled and executed the final bounded bundle API and focused SDE regression tests. Full Android/APK validation was not selected by the current PR classifier because this is an ordinary recommendation/test surface, not a full-checkpoint or central runtime cutover.

## Completion decision

SDE-1B/1C is complete for its intended bounded slice:

- a recommendation-owned orchestration entry point exists;
- legal candidate/proposition ownership remains outside SDE;
- the existing exact evaluator remains the only world-consequence authority;
- whole-bundle semantics are preserved;
- focused differential tests are green;
- no production caller behavior changed.

The next SDE-1 slice is lifecycle ownership (`PERSISTENT`, `COMMITTED`, `PLANNED / UNCOMMITTED`) before any meaningful production cutover or Poisoner replanning work.
