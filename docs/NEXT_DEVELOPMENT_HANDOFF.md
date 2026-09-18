# NEXT DEVELOPMENT HANDOFF — SDE-2 First-Night Uncertainty / Replanning

> Updated: 2026-09-18 Australia/Sydney  
> Status: **CURRENT / SDE-1A–E COMPLETE ON PR #144 BRANCH**  
> SDE-1E completion: `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`  
> Architecture background: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/SDE_1A_ORCHESTRATION_SEAM_AUDIT_2026-09-17.md`;
6. `docs/SDE_1B_EXACT_CONSEQUENCE_SEAM_NOTE_2026-09-17.md`;
7. `docs/SDE_1D_LIFECYCLE_OWNERSHIP_NOTE_2026-09-18.md`;
8. `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`;
9. Storyteller Decision Engine route;
10. query live `main`, PR #144 head and current checks before executable edits.

Do not restart completed FN-BUNDLE, SDE-0, or SDE-1A–E work.

## 1. Live continuation point

Branch: `sde-1-orchestration-seam`  
PR: #144, still draft and unmerged.

Live `main` at SDE-1E validation:

`4d6e90a7a268570d261048931a3557433ea01d83`

Final SDE-1E executable validation head:

`e7bb31937db32863e5606044b443818011d16236`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

CI run: `35285847867`.

Later documentation commits are not executable validation evidence.

## 2. SDE-1 completed contracts

### Ownership

- `ClocktowerGameSession` remains canonical actual-state, revisions, durable action/observation history and commit owner.
- rules/candidate domains own legal outcomes.
- `TroubleBrewingRegistrationDomain` owns Spy/Recluse registration legality.
- `ExactHistoricalHypotheticalObservationBundleEvaluator` owns exact possible-world consequences.
- `InformationDecisionContext` owns structured-information freshness/confirmation.
- flow planner/projector owns interaction ordering.
- SDE owns orchestration/evaluation and disposable planned metadata only.

### Exact consequence seam

```text
ExactConsequenceContext
ExactConsequenceRequest
ExactConsequenceCandidate
CandidateConsequence
ExactConsequenceEvaluation
StorytellerDecisionEngine.evaluateExactConsequences(...)
```

The exact historical baseline revision is distinct from current decision freshness revisions. Production callers must pass current session revisions explicitly when they differ.

### Lifecycle

```text
PERSISTENT   -> session/setup-owned durable commitments
COMMITTED    -> session/history-owned executed/shown facts
PLANNED      -> SDE-owned disposable identity/freshness references only
```

`PlannedDecisionRef` owns no state store, history, candidate pool, selected payload, mutation handle, or independent revision counter.

### Production shadow proof

The healthy structured numeric path now has a bounded production shadow bridge:

```text
existing legal/materialized candidates
→ StructuredInformationProductionShadow
→ StructuredInformationShadowAdapter
→ StorytellerDecisionEngine
→ exact evaluator
→ PlannedDecisionRef provenance
```

Tests prove visible choices and confirmation semantics remain unchanged and only explicit `ClocktowerGameSession.commitGlobalEpistemicObservation(...)` performs durable observation/revision mutation.

## 3. Current objective — SDE-2

Implement first-night uncertainty and replanning in this order:

```text
SDE-2A Drunk ownership / dependency audit
→ SDE-2B Spy/Recluse interaction-scoped registration uncertainty
→ SDE-2C Poisoner-driven invalidation/replanning
```

Do not jump directly to a global optimization policy.

## 4. SDE-2A — Drunk

Audit before editing:

- where the Drunk shown role is committed;
- which first-night clues depend on that shown role;
- which recommendation decisions are still uncommitted when Poisoner acts;
- which existing semantic identities/revisions already express those dependencies.

Freeze the distinction:

```text
Drunk shown role
    PERSISTENT setup commitment

Drunk clue recommendation
    PLANNED until confirmed/shown

shown clue observation
    COMMITTED durable history
```

SDE must never treat the already committed shown role as a disposable plan.

Add tests before implementation for:

- a Drunk shown-role commitment surviving replanning;
- an uncommitted clue plan becoming stale when its source dependency changes;
- a committed clue observation never being rewritten.

## 5. SDE-2B — Spy / Recluse

Registration remains interaction-scoped.

Do not mutate canonical player identity to represent a registration branch.

Audit:

- current `TroubleBrewingRegistrationDomain` inputs/outputs;
- existing `RegistrationPolicy` legacy heuristic caller;
- where exact world evaluation needs a branch-local registration assumption;
- whether the assumption belongs in a request/context envelope or an existing semantic fact type.

Required proof:

- legality remains in `TroubleBrewingRegistrationDomain`;
- alternate registration is scoped to one interaction/evaluation;
- no permanent identity/state mutation is introduced;
- exact evaluator consumes the branch without creating another registration rules engine.

## 6. SDE-2C — Poisoner

Poisoner changes may invalidate still-uncommitted decisions.

Use existing session/input revisions and `PlannedDecisionRef` freshness. Do not introduce a new replanning revision counter.

Required behavior:

```text
source facts change
→ prior PLANNED decision becomes stale
→ stale plan is discarded
→ legal candidates are regenerated by existing owners
→ SDE exact consequences are recomputed
→ fresh plan is produced
```

Already COMMITTED information remains history and is not rewritten.

## 7. Test-first expectations

For each SDE-2 slice:

- begin with a focused RED contract;
- reuse exact evaluator as oracle;
- assert canonical session snapshot/history are unchanged by planning alone;
- assert stale detection uses existing revision/semantic identity boundaries;
- assert durable mutation occurs only through existing session APIs;
- assert visible production selection remains unchanged until a later explicit cutover stage.

Keep expensive corpus/calibration work outside ordinary FAST tests.

## 8. Non-goals

Do not add:

- production selection cutover to SDE diagnostics;
- global Badness thresholds;
- another GameState/history/revision authority;
- permanent Spy/Recluse identity mutation;
- committed-history rewriting;
- all-caller `ConsequenceEvaluator` migration;
- `ConsequenceEvaluator` deletion;
- cross-night policy beyond what first-night correctness requires.

## 9. Remaining legacy retirement obligations

`ConsequenceEvaluator` still has three production caller families:

1. `DynamicCandidateGenerator.evaluation(...)` when state is supplied;
2. `RegistrationPolicy.generateCandidates(...)`;
3. `DayRecommendationModule` malfunction path.

SDE-2 may touch the registration family only when required by the scoped uncertainty work. Do not broaden this into retirement work.

## 10. Stable handoff

> **SDE-1A/B/C/D/E are complete on PR #144, with final executable evidence at `e7bb31937db32863e5606044b443818011d16236`. Continue with SDE-2A: audit Drunk first-night ownership/dependencies and add tests proving persistent shown identity, disposable clue planning, stale-plan invalidation, and immutable committed history. Then proceed to interaction-scoped Spy/Recluse uncertainty and Poisoner-driven replanning. Preserve all SDE-1 ownership boundaries and do not cut production selection over to SDE yet.**
