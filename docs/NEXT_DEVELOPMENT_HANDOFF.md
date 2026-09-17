# NEXT DEVELOPMENT HANDOFF — SDE-1B/1C Exact-Consequence Orchestration Seam

> Updated: 2026-09-17 Australia/Sydney  
> Status: **CURRENT / SDE-1A COMPLETE; begin SDE-1B/1C**  
> Audit authority: `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`  
> Architecture background: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
6. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
7. query live `main`, branch head, open PRs and current checks before editing.

Do not restart completed FN-BUNDLE/SDE-0/SDE-1A work.

## 1. Live continuation point

SDE-1A is complete on branch `sde-1-orchestration-seam`.

The audit established:

- canonical state/revision/history owner: `ClocktowerGameSession`;
- structured-information confirmation/freshness owner: `InformationDecisionContext`;
- legal candidate owners remain in rules/candidate domains;
- registration legality owner: `TroubleBrewingRegistrationDomain`;
- exact hypothetical consequence owner: `ExactHistoricalHypotheticalObservationBundleEvaluator`;
- flow/order owner remains `ClocktowerProductionFirstNightFlow` / flow planner-projector;
- host UI/coordinator modules are migration callers, not target authorities;
- SDE-0 healthy bundle harness remains experiment/evidence infrastructure, not production runtime orchestration.

`ConsequenceEvaluator` is migration-era and has three confirmed production caller families:

```text
DynamicCandidateGenerator.evaluation(...), when state != null
RegistrationPolicy.generateCandidates(...)
DayRecommendationModule malfunction path
```

Do not delete it until all three obligations are migrated.

## 2. Current objective

**SDE-1B / SDE-1C — establish the smallest typed exact-consequence orchestration seam.**

Target proof:

```text
existing healthy typed legal candidate/effect
→ StorytellerDecisionEngine request/context
→ existing proposition / hypothetical observation adapter
→ ExactHistoricalHypotheticalObservationBundleEvaluator
→ typed CandidateConsequence
```

The new seam is evaluation-only for this slice.

It must not own:

- production selection;
- UI routing;
- confirmation;
- durable semantic commit;
- interaction ordering;
- role legality;
- world solving;
- Drunk / Spy-Recluse / Poisoner uncertainty.

## 3. First fixture selected by SDE-1A

Use one already-supported healthy structured-information interaction, preferably structured numeric.

Reasons:

- legal candidates are already typed;
- candidate IDs are stable;
- `InformationDecisionContext` already provides revision validation;
- proposition/observation materialization already exists;
- durable session commit handoff already exists;
- the same hypothetical observation can be evaluated directly by the exact authority;
- current structured numeric `DynamicGenerationContext` does not supply `state`, so the path bypasses legacy `ConsequenceEvaluator`;
- no SDE-2 uncertainty is required.

Do not use the SDE-0 healthy bundle harness as a production dependency. It may be used only as evidence/reference where useful.

## 4. SDE-1B typed boundary

Create the smallest useful contracts under recommendation ownership, preferably a dedicated `clocktower/recommendation/sde` package.

Names are still allowed to tighten during the focused test, but the conceptual boundary is:

```text
StorytellerDecisionEngine
    evaluate(request, context)

DecisionContext
    revision-bound snapshot/reference
    source gameStateRevision / playerInputRevision
    phase / round / interaction identity
    validated ruleset and semantic-history inputs required by exact evaluation
    profile input only where actually needed

DecisionRequest
    typed candidate identity
    already-materialized hypothetical observation/effect or a thin typed materializer input

CandidateConsequence
    candidate identity
    exact BEFORE / AFTER world cardinality
    before/after structural diagnostics

StorytellerPolicyResult
    classification shell only when evidence justifies it
```

Prefer direct reuse of existing exact diagnostic types if package visibility and ownership remain clean. Do not copy diagnostics merely to rename them.

Do not create a shadow `GameState`, a second observation log, a second revision counter, or a second possible-world solver.

## 5. SDE-1C focused test first

Add a focused orchestration differential test before production integration.

Required assertion shape:

```text
SDE evaluate(candidate, context)
    returns the same exact consequence diagnostics as
ExactHistoricalHypotheticalObservationBundleEvaluator.evaluate(...)
    for the same hypothetical observation/context
```

Also prove:

- evaluation is deterministic;
- source context/history is not mutated;
- candidate identity is preserved;
- evaluator capability deferral is surfaced, not converted into guessed heuristics;
- no selection or commit side effect occurs.

This is not a second oracle. The exact evaluator remains the oracle/authority.

## 6. Implementation constraints

Keep the first implementation narrow:

- one request shape is acceptable if it proves the seam cleanly;
- one candidate or a small typed list is sufficient;
- no production caller cutover is required;
- no UI change is required;
- do not move `InformationDecisionContext` confirmation logic into SDE;
- do not move `ClocktowerGameSession.commitGlobalEpistemicObservation` into SDE;
- do not add numeric BEGINNER Badness thresholds;
- do not add policy logic to `ConsequenceEvaluator`;
- do not change exact evaluator semantics unless the focused test exposes a real defect.

## 7. Lifecycle ownership for follow-up SDE-1D

The seam should not block the already-decided lifecycle distinction:

```text
PERSISTENT
    reference to session/setup-owned durable commitments

COMMITTED
    session-owned immutable committed history

PLANNED / UNCOMMITTED
    disposable orchestration-local recommendation identity + source revision
```

For the first exact-consequence seam, carry only what is needed for stable interaction/candidate identity and source revision. Do not prematurely build replanning machinery.

## 8. Production migration map retained from SDE-1A

Later migration must separately cover:

1. dynamic information callers using `DynamicGameState` / `ConsequenceEvaluator`;
2. registration policy after `TroubleBrewingRegistrationDomain` legality;
3. day malfunction recommendation path;
4. Mayor redirect / Demon successor heuristic callers;
5. host UI fanout and display-option adaptation.

These are not first-seam tasks.

## 9. Testing expectations

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

For the current slice:

- focused typed unit/contract tests are required;
- direct exact-evaluator comparison is the key differential evidence;
- no broad Android/UI suite is necessary until executable production routing changes;
- if central runtime fanout is later changed, run the corresponding broader T2/T4 gates;
- keep expensive calibration/holdout experiments out of ordinary FAST regression.

## 10. Completion target for this slice

SDE-1B/1C is ready to advance when:

- recommendation/SDE package owns one thin typed orchestration entry point;
- exact evaluator remains the only world-consequence authority;
- focused differential test passes conceptually and in CI/tooling available for the branch;
- no production caller behavior changes;
- roadmap/handoff record the next lifecycle/integration task.

## 11. Stable handoff

> **SDE-1A is complete. Begin SDE-1B/1C by implementing one evaluation-only `StorytellerDecisionEngine` seam for a healthy structured-information candidate. Reuse existing rules/candidate ownership, semantic observation materialization, and `ExactHistoricalHypotheticalObservationBundleEvaluator`; return typed exact consequence diagnostics; preserve `InformationDecisionContext`, session commit, flow ordering, UI routing and selection ownership unchanged.**
