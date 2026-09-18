# NEXT DEVELOPMENT HANDOFF — SDE-2 First-Night Uncertainty / Replanning

> Updated: 2026-09-18 Australia/Sydney  
> Status: **CURRENT / SDE-2A COMPLETE / SDE-2B NEXT EXECUTABLE SLICE**  
> SDE-1E completion: `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`  
> SDE-2A completion: `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`  
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

## 3. Current objective — SDE-2B

SDE-2A is complete. Continue with Spy/Recluse interaction-scoped registration uncertainty.

```text
TroubleBrewingRegistrationDomain
→ legal interaction-local registration alternatives
→ exact hypothetical evaluation branch
→ SDE consequence diagnostics
```

No canonical player identity mutation is allowed.

## 4. SDE-2A — Drunk — COMPLETE

Completion authority:

`docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`

Executable evidence:

`6c9d7fe8776fea4723004e22790ba11c1f140b8c`

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
```

Frozen result:

- shown role is PERSISTENT setup/session truth;
- unshown clue is PLANNED/disposable;
- shown clue is COMMITTED/immutable;
- Poisoner draft stales plans through `playerInputRevision`;
- Poisoner confirm stales plans through `gameStateRevision`;
- no new SDE dependency store or replanning revision counter exists.

## 5. SDE-2B — Spy / Recluse — CURRENT

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

> **SDE-1A/B/C/D/E and SDE-2A are complete on PR #144. SDE-2A executable evidence is `6c9d7fe8776fea4723004e22790ba11c1f140b8c`. Continue with SDE-2B: audit `TroubleBrewingRegistrationDomain`, the legacy `RegistrationPolicy` caller, and exact world-evaluation registration semantics; then add the smallest typed interaction-local registration assumption needed by exact consequence evaluation. Do not mutate canonical Spy/Recluse identity, duplicate registration legality, or cut production selection over to SDE.**
