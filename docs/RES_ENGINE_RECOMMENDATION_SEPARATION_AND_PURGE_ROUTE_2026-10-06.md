# RES — Engine / Recommendation Separation and Obsolete Recommender Purge Route — 2026-10-06

> Repository: `Jazz0006/CampBoardGameHost`
>
> Status: **CURRENT ARCHITECTURE / EXECUTION AUTHORITY — RES-0/1/2/3 COMPLETE / ACCEPTED; RES-4 NEXT**
>
> Supersedes the previous immediate continuation `GSP-2B3 -> GSP-2C -> GSP-2D`. GSP remains the later provider/benchmark route, but it is paused until this separation campaign establishes a clean engine-only boundary and removes obsolete recommendation implementations.
>
> Product invariant: **Clocktower gameplay, rules, legal domains, state progression, persistence/recovery and Manual Storyteller decisions must remain complete when no recommendation module/provider exists.**

## 1. Architecture decision

The product now treats Game Engine and Storyteller Recommendation as independently evolvable modules connected only by typed contracts.

```text
Canonical Game Session / Truth
        |
        v
Game Engine
- rules
- phase/progression
- legal decision domains
- authoritative commits
        |
        | PendingStorytellerDecision / legal candidate domain
        v
Host/App orchestration
        |
        +--------------------+
        |                    |
        v                    v
Manual Storyteller UI   optional Recommendation Provider
                              |
                              | structured non-authoritative suggestion
                              v
                         Host validation
        |                    |
        +--------- candidateId / explicit choice --------+
                              |
                              v
                         Game Engine commit
```

The Game Engine MUST NOT require a Recommendation implementation to initialize, generate a legal domain, progress a game or commit a legal Storyteller decision.

The recommendation side is read-only. It may consume immutable state/context and already-legal candidate identities. It may not own legality, mutate canonical state, commit outcomes or become a gameplay availability dependency.

## 2. Why this reset is required

Two recommendation-generation approaches have now been rejected as long-term product architecture:

1. **Legacy heuristic/style/weighted recommendation**
   - GENTLE / BALANCED / AGGRESSIVE;
   - hand-authored scores, pressure/exposure/discussion weights;
   - fixed probability budgets and weighted selection;
   - local role-by-role recommenders and balance-driven adjustments.

2. **Evidence-case -> predicate -> deterministic named policy**
   - `BEGINNER_CONSERVATIVE_V1/V2`;
   - `DRUNK_ASSIGNMENT_Q04_V1`;
   - functioning Librarian V2 selector;
   - INV1-A Investigator selector;
   - future code of the same pattern.

GSP-1 correctly revoked discretionary automatic authority, but authority revocation alone is insufficient. Retaining executable obsolete selectors/scorers in production source keeps accidental dependencies alive and contaminates the new provider contract.

Therefore physical retirement is moved forward from the old late GSP-6 position.

## 3. RES-0 live-code audit findings

Audit baseline: live `main@10d1eae72ad564155dc7faa53092b0203b135fbc`.

### 3.1 Legacy heuristic code is still product-reachable

Current production/UI source still reaches old recommendation behavior:

- `ClocktowerHostScreen` instantiates `ClocktowerRecommendationCoordinator`;
- registration UI calls `recommendRegistration(...)`;
- pair-information UI calls `recommendPair(...)`;
- number-information screens call legacy recommendation-option builders;
- `ClocktowerHostScreen.dynamicStorytellerState()` still calls `GameBalanceEvaluator.evaluate(...)`;
- `automaticStorytellerStyle` and `RecommendationStyle` still flow through Host/night information UI;
- `ClocktowerFirstNightInformationRequest` still applies style-dependent weighted behavior;
- `ClocktowerRecommendationCoordinator` still imports and exposes `WeightedStableSelector`, `SetupRecommendationService`, `RecommendationStyle`, dynamic recommenders and legacy explanation/scoring data.

This is not merely historical code. It is a live coupling surface.

### 3.2 Some old automatic-policy implementations are now dead or reference-only, but remain in production source

Examples include:

- `TemporaryAutomaticStorytellerPolicy` and its registration/Mayor/Demon-successor wrappers: production definitions remain, but current main-source search finds no product caller of the wrapper selection functions;
- `FunctioningLibrarianV2ProductionSelector` and `FunctioningInvestigatorInv1ProductionSelector`: retained selector definitions; their former Host helper functions remain but have no production caller after GSP-1;
- `DrunkAssignmentQ04V1Policy`: production adapter/replay/reference implementation remains after automatic authority revocation.

These are physical-retirement targets. Historical evidence should survive as fixtures/data/reference notes rather than executable answer authority.

### 3.3 Setup recommendation structures remain coupled even where product call paths have narrowed

`SetupRecommendationService.ConstrainedResult` still appears in:

- `RecommendationModules`;
- `ClocktowerRecommendationCoordinator`;
- setup recommendation prewarm/reveal coordinators;
- Red Herring / Demon bluff shadow adapters.

Before deleting the setup recommender, rules/legal candidate generation and any still-useful diagnostic projection must be separated from score/style plan output.

### 3.4 The new GSP provider seam is directionally correct but still recommendation/SDE-shaped

`StorytellerPolicyRequestV1 / ResponseV1` already enforce important invariants:

- Host-owned legal candidate IDs;
- stale revision validation;
- provider deferral;
- no commit authority.

However the request currently lives under `recommendation/sde` and directly depends on:

- `RecommendationDecisionInputV1`;
- `RecommendationFeatureProjectionV1`;
- `SdeDecisionLifecycleStage`;
- `SdeHistoricalPrefixRef`;
- `TroubleBrewingGameSnapshotV1`;
- an export-type enum currently covering only Drunk assignment and first-night pair information.

This seam must be extracted into a neutral engine/provider contract before adding more provider context, history or API materialization.

## 4. Ownership map

### KEEP in Game Engine / domain / session ownership

- `ClocktowerGameSession` / `ClocktowerSessionState` canonical mutable truth;
- current-version Recovery and revision/freshness identity;
- script/rules mechanics and phase progression;
- complete rules-owned legal candidate domains;
- actual/shown identity and effective-state semantics;
- Drunk late binding;
- Red Herring and Demon-bluff latest-safe commitment barriers;
- registration legality/witness semantics;
- Mayor redirect legal domain;
- Demon succession legal domain;
- recipient information / observation history;
- Storyteller committed decision history;
- per-player experience, claimed roles and declared pressure as Host-owned inputs;
- `TroubleBrewingGameSnapshotV1` as the current TB semantic snapshot;
- Manual selection and authoritative commit paths.

### KEEP but MOVE / NEUTRALIZE

- stable decision IDs and candidate IDs;
- immutable pre-decision snapshot/context;
- legal-domain serialization;
- request/response freshness validation;
- provider deferral / uncertainty / rationale shape;
- policy-neutral benchmark/export material;
- historical actual choice as target/evaluation metadata;
- replay identity needed to reproduce canonical decision inputs.

These must not depend on obsolete policy versions, style scores or executable case selectors.

### DELETE after dependencies are extracted

Legacy heuristic family:

- `RecommendationStyle` and compatibility/style UI flow;
- `GameBalanceEvaluator` where it exists only to drive recommendation policy;
- `SetupRecommendationService` scoring/ranking/style plan generation;
- `MalfunctionPolicy`;
- recommendation portions of `RegistrationPolicy`;
- recommendation portions of `DynamicCandidateGenerator`;
- `MayorRedirectRecommender` and `DemonSuccessorRecommender` ranking/scoring;
- temporary 90/10, 10/90 and 4/3/2/1 automatic policies;
- `WeightedStableSelector` usages that exist only for obsolete Storyteller policy;
- score/style explanations, telemetry and tests whose sole contract is obsolete ranking.

Named deterministic special-policy family:

- `BEGINNER_CONSERVATIVE_V1/V2` executable selectors once neutral replay/export no longer requires them;
- `DRUNK_ASSIGNMENT_Q04_V1` executable policy/production adapter;
- functioning Librarian V2 production selector;
- INV1-A Investigator production selector;
- special-policy-specific traces/tests whose useful historical evidence can be represented as benchmark fixtures.

## 5. Multi-script contract rule

Unify the **outer protocol**, not every script's internal state.

Conceptually:

```text
PendingStorytellerDecision
- contractVersion
- scriptId
- decisionType
- decisionId
- sourceRevision
- stateSnapshot
    - versioned script-specific payload
- legalCandidates
    - stable candidateId
    - decision/script-specific semantic payload
- requiredContext
- optionalEnrichment
```

For example TB may continue to use a versioned Trouble Brewing snapshot payload while a future script provides its own versioned snapshot. A provider adapter can dispatch by `scriptId + snapshotSchema`.

Do not build one giant cross-script mutable GameState DTO.

### 5.1 Stateless provider does not mean memoryless recommendation

The provider MUST NOT depend on LLM/chat conversation memory as the authoritative source of game continuity. Every invocation must be reconstructable from Host-owned canonical state and explicit persisted/rebuildable context. This does **not** mean that current-state-only context is sufficient.

High-quality Storyteller decisions require four distinct context layers:

1. **Current canonical state** — current grimoire/session truth, legal candidate domain, current player experience/claims/declared pressure and the exact decision revision.
2. **Current-game longitudinal memory** — prior observations and committed Storyteller decisions across rounds/nights, including information already shown, Drunk misinformation continuity, poisoning/registration timing, claim evolution and other narrative facts needed to judge consistency across the whole game.
3. **Cross-game player experience** — relevant prior-game history such as recent roles, repeated high-pressure treatment, repeated Drunk assignment or other experience patterns. These are recommendation enrichments/soft constraints, never legality.
4. **Recommendation/diversity history** — relevant similar prior situations and previously selected alternatives so a provider may moderately avoid repeating the same choice when comparably strong alternatives exist. Diversity is a soft preference and must never force a materially worse recommendation.

Therefore the target flow is:

```text
canonical current state
+ current-game event / observation / decision history
+ current-game narrative projection
+ relevant cross-game player history
+ relevant prior recommendation/diversity history
        |
        v
Host-owned Context Materializer / Retriever
        |
        v
bounded typed context for this decision
        |
        v
stateless optional provider
```

The Host owns and versions this memory. It must survive provider replacement, application restart/recovery and model/API changes. Provider conversation/session memory may be used only as a transport optimization; correctness must never depend on it and every request must remain independently reconstructable and auditable.

RES-2 establishes the neutral request/response shell and the first same-game context reconstruction seam. Full longitudinal/narrative and cross-game retrieval are follow-on context capabilities after the engine/provider separation is accepted; they must target this neutral contract rather than reintroduce recommendation-owned mutable state.

## 6. RES execution stages

### RES-0 — separation + purge audit / authority reset

Status: **COMPLETE / ACCEPTED**.

Acceptance:

- this route is current authority;
- GSP-2B3/2C/2D are paused;
- obsolete algorithms are explicitly classified;
- roadmap/handoff/docs index no longer direct work back to the old order;
- no production behavior changes are required in this docs/audit slice.

### RES-1 — engine-only Storyteller decision boundary

Status: **COMPLETE / ACCEPTED**. Pair information established the first vertical slice; Mayor redirect then proved the same generic pending-decision contract across a structurally different mechanical-result decision.

Create the smallest engine-owned neutral boundary for a pending Storyteller decision and legal domain.

Acceptance:

1. legality and candidate generation live outside recommendation implementation;
2. engine/session can expose a pending decision without constructing legacy recommendation models;
3. a Manual choice can be validated and committed without any recommendation coordinator/provider;
4. architecture tests/guards prevent Game Engine owners from importing recommendation implementation packages;
5. no provider/API implementation is required.

Use an existing mature TB decision surface as the first vertical slice; prefer first-night pair information because complete legal/manual authority already exists. Do not rewrite every decision family in one PR.

### RES-2 — neutral provider contract extraction

Status: **COMPLETE / ACCEPTED**. The neutral outer contract, Host-owned same-game context reconstruction, direct pair-information request materialization and direct Mayor-redirect request materialization were accepted in PR #236 (`19abd61cf79088b16577461f3b7c0f40b30a5674`). PR #237 closed the remaining representative gap by adding direct engine-owned Drunk-assignment pending-decision -> neutral-provider materialization and recording the Host-owned context-memory rule; squash merge `7ae03105f05e8754851b6134d5c5c9c25dfa5e0f`, CI #3800 GREEN, R2 #3482 GREEN.

Extract the durable parts of GSP-2A/2B into a script-neutral outer contract.

Acceptance:

- request owns no obsolete SDE policy type;
- response contains only candidate IDs already in the engine legal domain plus rationale/alternatives/uncertainty or deferral;
- freshness validation remains Host-owned;
- script-specific state/context is versioned behind the outer contract;
- recommendation provider can be absent.

### RES-3 — legacy heuristic purge

Status: **COMPLETE / ACCEPTED.** Product/UI/session reachability has been migrated away from the obsolete style/score/weighted recommender stack. The old dynamic/setup recommenders, weighted selectors, recommendation profiles, balance scorer, temporary impaired-information probability policy, local rank/weight metadata, recommendation-plan/style models and their dead tests/benchmarks have been physically removed. Complete Manual legal domains, rule-deterministic unique outcomes, Host-owned longitudinal/history context and the optional RES-2 neutral provider seam remain. Historical `StorytellerDecisionEvent` score/probability/pressure/explanation fields are retained only as archival/replay compatibility data and are not current selection authority. Accepted executable checkpoint: `5b4b2a94c332bde462124b133955514a31f23183`; CI #3815 GREEN with full Android unit tests + debug assemble, ASP contract and Real Clingo gates; R2 #3495 GREEN.

Migrate live UI/session call paths from old recommendations to:

- complete Manual legal domains;
- rule-deterministic unique outcomes;
- optional neutral provider output only after RES-2.

Then delete/narrow obsolete style/score/weighted recommendation code and its tests.

Acceptance includes repository-wide proof that no product/UI path uses legacy style/score/weight to rank Storyteller choices.

### RES-4 — deterministic special-policy purge

Replace executable Q04/V1/V2/Librarian/Investigator special-policy code with non-executable benchmark/reference material where historical evidence is still valuable.

Acceptance:

- no named evidence-case selector remains in product execution source;
- benchmark fixtures preserve reconstructable inputs/actual historical choices/rationale without turning them into production if/else policy;
- no special-policy identity is required to play a game or invoke a future general provider.

### RES-5 — physical module boundary

Converge toward explicit module/dependency ownership, potentially:

```text
clocktower-engine
storyteller-contract
storyteller-recommendation
app
```

Exact Gradle module names are not frozen before the dependency audit.

Required dependency direction:

```text
app -> engine
app -> storyteller-contract
app -> optional recommendation adapter

recommendation -> storyteller-contract
recommendation -> immutable engine projections only

engine -X-> recommendation implementation
```

### Resume GSP

Only after RES establishes the clean boundary:

```text
cross-game history/diversity context
-> prompt/response materializer
-> manual blind benchmark
-> repeated/cross-model benchmark
-> optional API adapter
-> later model/deployment decision
```

GSP concepts remain valuable; their implementation must target the new contract rather than inherit obsolete SDE/recommender ownership.

## 7. Validation strategy

RES is architecture cleanup with behavior-preserving and deletion-heavy slices.

For each executable slice:

- map producers/consumers before changing shared contracts;
- protect rules legality and complete Manual domain first;
- prefer contract/architecture tests at the true owner;
- do not preserve obsolete ranking tests merely to keep historical test count;
- delete superseded tests after their real legality/manual invariant is protected elsewhere;
- run focused T0/T1 as appropriate and full T4 at logical merge checkpoints.

The final purge campaign must prove:

1. full offline game progression works with no Recommendation implementation;
2. every multi-choice Storyteller decision has a complete Manual legal domain;
3. unique rule-deterministic decisions still resolve correctly;
4. no old style/score/probability/case-selector can influence a product decision;
5. provider output cannot invent candidates or mutate state;
6. current Recovery behavior remains valid;
7. full repository acceptance is green.

## 8. Immediate next step

RES-0/1/2/3 are complete and accepted. The next executable stage is RES-4 named deterministic special-policy purge.

> **RES-4 must remove named evidence-case selectors from product execution while preserving reconstructable benchmark/reference evidence and the clean neutral provider boundary established by RES-1/2/3.**

Do not resume old GSP-2B3/2C/2D or add recommendation-owned memory. Later longitudinal/cross-game context work must target the neutral RES provider contract after RES-4/5 convergence.
