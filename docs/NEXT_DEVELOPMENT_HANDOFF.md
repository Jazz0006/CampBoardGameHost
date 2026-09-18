# NEXT DEVELOPMENT HANDOFF — SDE-3 Cross-Night Impaired / Registration Decisions

> Updated: 2026-09-18 Australia/Sydney  
> Status: **CURRENT / SDE-2 COMPLETE / SDE-3 AUDIT NEXT**  
> SDE-1E completion: `docs/SDE_1E_STRUCTURED_SHADOW_INTEGRATION_NOTE_2026-09-18.md`  
> SDE-2A completion: `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`  
> SDE-2B completion: `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`  
> SDE-2C completion: `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`  
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
9. `docs/SDE_2A_DRUNK_OWNERSHIP_REPLANNING_AUDIT_2026-09-18.md`;
10. `docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`;
11. `docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`;
12. Storyteller Decision Engine route;
13. query live `main`, PR #144 head and current checks before executable edits.

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

## 3. Current objective — SDE-3 audit

SDE-2 is complete. Do not add more first-night lifecycle infrastructure unless a concrete regression requires it.

Start SDE-3 with a fresh fanout/ownership audit for later-game impaired or registration decisions.

Preferred target properties:

```text
later Night interaction
+ already committed public/private history
+ current poison/drunk/effective-state semantics
+ optional Spy/Recluse registration branch
→ existing legal candidate owner
→ typed hypothetical observation/effect
→ exact historical replay
→ SDE consequence diagnostics
→ existing freshness / confirmation / durable commit boundaries
```

Do not start with broad production cutover. First prove one bounded later-game interaction through the existing seam.

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

## 5. SDE-2B — Spy / Recluse — COMPLETE

Completion authority:

`docs/SDE_2B_REGISTRATION_BRANCH_AUDIT_2026-09-18.md`

Executable evidence:

```text
a977c01...  exact witness alternatives + binding
             R2 SUCCESS
             Android FAST SUCCESS
             Real Clingo SUCCESS
             CI gate SUCCESS

ec970aaa...  SDE forwarding + pair candidate projection
             R2 SUCCESS
             Android FAST SUCCESS
             CI gate SUCCESS
```

Frozen result:

- registration legality remains exclusively in `TroubleBrewingRegistrationDomain`;
- exact world results preserve complete registration witness alternatives;
- an exact hypothetical candidate may bind one selected interaction-local witness;
- natural truth uses an explicit empty witness when the candidate selected no special registration;
- generator-local interaction IDs/questions are provenance, not canonical identity;
- SDE does not mutate Spy/Recluse identity or re-run legality.

## 6. SDE-2C — Poisoner — COMPLETE

Completion authority:

`docs/SDE_2C_POISON_REPLANNING_NOTE_2026-09-18.md`

Executable evidence:

`fd90b8dc0433dbd925f0be9f94f3a2693e416f4b`

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

CI run: `35296161489`.

Frozen result:

- Poisoner draft edits invalidate uncommitted plans through `playerInputRevision`;
- confirmed poison commits hidden mechanical history and advances `gameStateRevision`;
- old plans for both affected and unaffected decisions become stale;
- fresh decision contexts produce fresh revision-bound plans;
- committed observations are unchanged;
- hidden Poison/Protect/Attack/RoleChange facts remain outside recipient-visible historical replay;
- no new replanning counter/store was added;
- production selection remains unchanged.

## 7. Test-first expectations

For SDE-3:

- begin with a focused audit, then a RED contract only when executable behavior is missing;
- reuse exact historical evaluator as oracle;
- preserve recipient visibility boundaries for hidden Storyteller actions;
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

SDE-3 may intersect these caller families only when required by the bounded later-game proof. Do not broaden the task into SDE-4 retirement work.

## 10. Stable handoff

> **SDE-1A/B/C/D/E and SDE-2A/B/C are complete on PR #144. Final SDE-2C executable evidence is `fd90b8dc0433dbd925f0be9f94f3a2693e416f4b`, with R2 / Android FAST / CI gate SUCCESS. Continue with SDE-3 by auditing one later-game impaired/registration interaction that can reuse the existing exact historical replay, registration legality, revision freshness and session commit authorities. Do not create a second effective-state/history owner and do not cut production selection over yet.**
