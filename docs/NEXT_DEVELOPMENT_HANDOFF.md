# NEXT DEVELOPMENT HANDOFF — SDE-1E Structured Information Shadow Integration

> Updated: 2026-09-18 Australia/Sydney  
> Status: **CURRENT / SDE-1A–D COMPLETE ON PR #144 BRANCH**  
> Audit authority: `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`  
> Exact-seam completion: `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`  
> Lifecycle completion: `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`  
> Architecture background: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. SDE-1A audit;
6. SDE-1B exact-seam note;
7. SDE-1D lifecycle note;
8. Storyteller Decision Engine route;
9. query live `main`, PR #144 head and current checks before executable edits.

Do not restart completed FN-BUNDLE, SDE-0, SDE-1A, SDE-1B, SDE-1C or SDE-1D work.

## 1. Live continuation point

Branch: `sde-1-orchestration-seam`  
PR: #144, keep draft until explicitly advanced later.

Live `main` at the SDE-1D validation point:

`4d6e90a7a268570d261048931a3557433ea01d83`

SDE-1D executable validation head:

`c4e0967e416f0a259e50ab38ef2d832f6ab1b7c0`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

The branch was ahead of and not behind `main` at validation.

## 2. Completed SDE-1 contracts

### SDE-1A

Ownership/fanout audit complete. The host UI/coordinator remain migration callers, not target authorities. `ConsequenceEvaluator` still has three production caller families and is not yet deleted.

### SDE-1B/1C

Bounded exact consequence seam:

```text
ExactConsequenceContext
ExactConsequenceRequest
ExactConsequenceCandidate
CandidateConsequence
ExactConsequenceEvaluation
StorytellerDecisionEngine.evaluateExactConsequences(...)
```

The exact evaluator remains the oracle/authority. No selection or commit moved into SDE.

### SDE-1D

Lifecycle ownership is now explicit:

```text
PERSISTENT   -> session/setup-owned durable commitments
COMMITTED    -> session/history-owned executed/shown facts
PLANNED      -> SDE-owned disposable identity/freshness reference only
```

`PlannedDecisionRef` contains:

```text
decisionId
candidateId
source InformationDecisionRevision
optional source semantic snapshot identity
```

It owns no candidate pool, `GameState`, history, revision counter, observation log or session mutation handle.

`fromInformationSnapshot(...)` validates that the candidate belonged to the source legal candidate space, then stores only stable identity/freshness provenance.

## 3. Frozen authority boundary

Do not disturb these owners:

- `ClocktowerGameSession` — canonical actual state, revisions, durable action/observation history and commit;
- rules/candidate domains — legal outcomes and registration legality;
- existing proposition/observation adapters — semantic materialization;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` — exact possible-world consequences;
- `InformationDecisionContext` — structured-information confirmation/freshness;
- production flow planner/projector — interaction ordering;
- UI — display/confirmation/manual Experienced-mode override.

SDE owns only orchestration/evaluation plus disposable planned metadata.

## 4. Current objective — SDE-1E

Wire one **healthy structured-information production path** through the SDE in shadow mode.

Preferred first path: structured numeric information.

Why:

- stable candidate IDs already exist;
- legal candidates already come from the established rules/dynamic candidate pipeline;
- typed proposition / observation materialization already exists;
- `InformationDecisionContext` already owns revision validation and confirmation;
- the structured numeric adapter currently builds its dynamic generation context without `state`, so it does not invoke legacy `ConsequenceEvaluator`;
- this minimizes accidental migration scope.

## 5. Production path to trace first

Audit the live branch before editing and confirm exact current names/call sites:

```text
ClocktowerStructuredInformationPreparation
→ StructuredNumericInformationAdapter
→ ClocktowerRecommendationCoordinator.resolveNumberInformation
→ DynamicCandidateGenerator.generateNumeric
→ ClocktowerRecommendationCoordinator.informationDecisionContext
→ StructuredNumberInformationUiModel
→ InformationDecisionContext.confirm(...)
→ record reliable private information
→ ClocktowerGameSession observation commit
```

Find the narrowest place where an already-legal/materialized candidate set plus current exact context can be evaluated by SDE **without affecting the returned recommendation/selection**.

## 6. Required SDE-1E proof

The integration must demonstrate one real caller chain where:

1. existing rules/candidate generation still produces legality;
2. existing semantic adapters materialize hypothetical observations;
3. SDE evaluates the same stable candidate identities using the existing exact evaluator;
4. `PlannedDecisionRef` can bind the shadow result to the same source revisions/snapshot identity;
5. current visible recommendation output is unchanged by the shadow result;
6. `InformationDecisionContext` still performs confirmation/freshness validation;
7. session still performs the only durable observation commit/revision movement;
8. shadow evaluation failure/deferral does not silently become heuristic selection authority.

Prefer an explicit typed shadow result or adapter over hidden side effects.

## 7. Tests first

Add a focused integration/owner-level regression before production wiring.

A good test should compare:

```text
existing structured numeric preparation/result
vs
same preparation with SDE shadow consequence evaluation attached
```

and prove:

- candidate IDs and recommended IDs are identical;
- confirmation behavior is identical;
- SDE consequence diagnostics are available separately;
- no observation is committed merely by shadow evaluation;
- source session revisions do not change merely by shadow evaluation;
- after a real confirmation/commit, existing session behavior remains the authority.

Use the exact evaluator as oracle where necessary; do not implement another world solver in the test.

## 8. What not to build

Do not add:

- production selection based on SDE diagnostics;
- numeric Badness thresholds;
- another `GameState`, `DynamicGameState`, history store or revision owner;
- Drunk uncertainty;
- Spy/Recluse uncertainty selection;
- Poisoner invalidation/replanning;
- UI routing to SDE;
- a global SDE lifecycle registry;
- broad `ConsequenceEvaluator` migration/deletion;
- all-caller conversion.

SDE-1E is one bounded shadow integration proof only.

## 9. `ConsequenceEvaluator` retirement obligations retained

Three production caller families remain:

1. `DynamicCandidateGenerator.evaluation(...)` when state is supplied;
2. `RegistrationPolicy.generateCandidates(...)`;
3. `DayRecommendationModule` malfunction path.

Structured numeric is deliberately chosen because its current path avoids that legacy evaluator, making it a clean integration proof rather than a heuristic cutover.

## 10. Testing expectations

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

For SDE-1E:

- focused owner/integration test first;
- real Android FAST validation on the final executable SHA;
- exact-world semantic differential tests only where the new shadow adapter needs them;
- do not manufacture ASP/Clingo work if the classifier correctly excludes it;
- central runtime fanout changes require broader validation only if the chosen hook actually changes that fanout;
- after executable CI is green, update docs; do not place a docs-only commit in front of executable validation and then report a skipped Android job as evidence.

## 11. Stable handoff

> **SDE-1A/B/C/D are complete on PR #144. Continue with SDE-1E by tracing the real healthy structured-numeric production path and adding the narrowest possible shadow hook: existing legal/materialized candidates -> SDE exact consequence evaluation -> disposable PlannedDecisionRef provenance, while existing recommendation output, InformationDecisionContext confirmation and ClocktowerGameSession commit remain authoritative and unchanged. Do not start SDE-2 uncertainty or production selection cutover.**
