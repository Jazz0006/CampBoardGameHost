# NEXT DEVELOPMENT HANDOFF — SDE-1D Lifecycle Ownership

> Updated: 2026-09-17 Australia/Sydney  
> Status: **CURRENT / SDE-1A/B/C COMPLETE ON PR #144 BRANCH**  
> Audit authority: `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`  
> Exact-seam completion: `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`  
> Architecture background: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
6. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
7. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
8. query live `main`, PR #144 head and current checks before executable edits.

Do not restart completed FN-BUNDLE, SDE-0, SDE-1A, SDE-1B or SDE-1C work.

## 1. Live continuation point

Branch: `sde-1-orchestration-seam`  
PR: #144, still draft unless explicitly advanced later.

SDE-1A completed the ownership/fanout audit.

SDE-1B/1C added a bounded recommendation-owned exact-consequence seam:

```text
ExactConsequenceContext
ExactConsequenceRequest
ExactConsequenceCandidate
CandidateConsequence
ExactConsequenceEvaluation
StorytellerDecisionEngine.evaluateExactConsequences(...)
```

Important naming/ownership result:

- the existing `domain.StorytellerDecisionRequest` is **not** the new SDE canonical request because it embeds migration-era `DynamicGameState`;
- the existing generic `DecisionCandidate<T>` remains a richer legal/recommendation-era carrier and may later be adapted into the bounded exact candidate;
- `ExactConsequenceCandidate` carries a non-empty observation bundle, preserving whole-bundle semantics.

Executable validation head:

`a0c4fe2a2d1c1c94daedd976285cd29e40a48ee6`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
```

No production caller was cut over.

## 2. Frozen authority boundary

Do not disturb these owners:

- `ClocktowerGameSession` — canonical actual state, revisions, durable action/observation history and commit;
- rules/candidate domains — legal outcomes and registration legality;
- `ExactHistoricalHypotheticalObservationBundleEvaluator` — exact possible-world consequences;
- `InformationDecisionContext` — structured-information confirmation/freshness;
- production flow planner/projector — interaction ordering;
- UI — display/confirmation/manual Experienced-mode override.

The SDE currently owns only bounded orchestration of already-legal hypothetical observation bundles into exact consequence records.

## 3. Current objective — SDE-1D

Model the decision lifecycle without creating a second state/history owner.

Required semantic distinction:

```text
PERSISTENT
    setup/session-owned durable commitments
    examples: Demon bluffs, Red Herring, durable shown-role commitments where applicable

COMMITTED
    already shown/executed durable facts
    immutable from the SDE planning perspective

PLANNED / UNCOMMITTED
    recommendation-local disposable plan identity
    bound to source revisions
    may become stale before execution
```

SDE-1D is about **ownership and freshness**, not Poisoner replanning policy.

## 4. Audit before implementation

Before adding types, inspect existing lifecycle representations and classify them as reuse/adapt/legacy:

- `FirstNightInformationMigration` planned/displayed tracking;
- `InformationDecisionRevision` / `InformationDecisionSnapshot`;
- `DecisionRevision` / `DecisionEventStore`;
- session setup commitments and durable observation history;
- any precompute/cache identity that must remain non-authoritative;
- existing interaction/request IDs that can provide stable decision identity.

The goal is to reuse existing identifiers/revision values and avoid another registry/store.

## 5. Minimum lifecycle contract

A likely minimum planned record is conceptually:

```text
PlannedDecisionRef
    decision / interaction identity
    candidate identity
    source gameStateRevision
    source playerInputRevision
    optional snapshot/candidate-space identity where confirmation requires it
    lifecycle kind = PLANNED
```

Persistent/committed states should normally be **references or classification of session-owned facts**, not mutable copies held by SDE.

Names are not frozen until the audit/test proves the smallest useful shape.

## 6. Required SDE-1D behavior

Focused tests should prove:

1. a planned decision is explicitly bound to source revisions;
2. it is fresh when compared with matching current revisions;
3. it is stale when either source revision changes;
4. stale detection has no mutation/commit side effect;
5. persistent/committed lifecycle references do not become writable copies of session truth;
6. lifecycle metadata cannot advance session revisions or append semantic observations;
7. no Poisoner-specific invalidation logic is smuggled into this phase.

Prefer a small pure typed owner-level contract.

## 7. What not to build

Do not add:

- another `GameState` or `DynamicGameState` copy;
- another observation/action history store;
- another revision counter;
- a global mutable planned-decision registry unless a later real caller proves it necessary;
- Poisoner target invalidation/replanning policy;
- Drunk or Spy/Recluse uncertainty policy;
- Storyteller badness thresholds;
- production selection cutover;
- UI changes;
- `ConsequenceEvaluator` retirement yet.

## 8. Follow-up SDE-1E

After lifecycle ownership is explicit, wire one existing healthy structured-information path through SDE as a bounded shadow/integration proof.

That later slice should preserve existing visible behavior while proving:

```text
rules legal candidate
→ existing semantic materialization
→ SDE exact consequence evaluation
→ existing InformationDecisionContext confirmation
→ existing session commit
```

SDE should observe/evaluate the same candidate identity without becoming confirmation or commit authority.

Only after this boundary proof should wider caller migration be considered.

## 9. `ConsequenceEvaluator` retirement obligations retained

Three production caller families remain:

1. `DynamicCandidateGenerator.evaluation(...)` when state is supplied;
2. `RegistrationPolicy.generateCandidates(...)`;
3. `DayRecommendationModule` malfunction path.

SDE-1D does not migrate or delete any of them.

## 10. Testing expectations

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

For SDE-1D:

- focused JVM owner-level tests first;
- no exact-world oracle run is required unless exact semantics are changed;
- no broad UI tests unless UI is touched;
- no full calibration corpus;
- preserve the current PR classifier behavior unless the changed surface requires a stronger gate.

## 11. Stable handoff

> **SDE-1A/B/C are complete on PR #144's branch. Continue with SDE-1D by auditing existing planned/displayed/revision lifecycle types, then add the smallest pure revision-bound lifecycle metadata needed to distinguish session-owned PERSISTENT/COMMITTED truth from disposable PLANNED/UNCOMMITTED recommendations. Do not implement Poisoner replanning or production cutover yet.**
