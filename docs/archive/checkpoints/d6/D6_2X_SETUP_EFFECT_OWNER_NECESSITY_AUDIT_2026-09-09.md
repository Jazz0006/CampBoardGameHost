# D6.2x — Setup effect-owner necessity audit

> Date: 2026-09-09 Australia/Sydney  
> Status: COMPLETE / READ-ONLY / NO-GO FOR A NEW GENERIC SETUP EFFECT OWNER  
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.  
> D6.2w docs checkpoint: `2b152e4ad08e9707fad238abe262a93761b9187c`.  
> Latest validated production checkpoint remains `a692cc722f1e597e747154bf05a2689fee9bed4c`; final validated code/test head remains `b2263cd08bc2ce223598698324bf2b22243c91f2`.

## Decision

Do **not** create a generic setup effect controller, `SetupEffectOwner`, `SetupEffectContext`, or broad Compose state holder.

The surviving setup / first-night effects do not form one transaction. They belong to several different lifecycles:

1. setup recommendation demand + asynchronous UI loading;
2. automatic recommendation application + selection telemetry;
3. first-night natural-pair precompute consumption and start gating;
4. first-night poison-driven invalidation of only unshown information;
5. A4 identity reveal prewarm and A4 observation-cache rebuild;
6. the earlier setup-commit/reveal transition, which is already owned by dedicated session coordinators rather than a Compose effect.

The expensive or semantic work is already delegated to typed coordinators/services. Compose is mostly retaining the correct cancellation/key lifetime and writing interaction-local UI state. Pulling those triggers under one new owner would join unrelated lifetimes and would require a broad callback/state bag containing recommendation, telemetry, A4, route, persistence and first-night inputs.

That would increase coupling and make stale/cancellation behavior harder to reason about.

## Evidence by surviving effect family

### 1. Setup recommendation generation effect

`ClocktowerJudgeScreen` has a `LaunchedEffect(recommendationKey, lockedRecommendationDecisions)` that:

- records initial recommendation demand through the supplied callback;
- marks `recommendationUiState` loading;
- performs the recommendation build off-main through either `setupRecommendationResultProvider` or `ClocktowerRecommendationCoordinator.recommendSetup(...)`;
- relies on Compose cancellation (`isActive`) so an old request cannot publish into a newer recommendation key;
- maps the constrained result into the local `RecommendationUiState` variants.

This effect does **not** own setup legality or recommendation semantics. Those already live below the UI in the provider/coordinator/service. Its remaining responsibility is a normal keyed async UI load lifecycle.

Moving it into a generic effect owner would either:

- hide Compose cancellation behind a non-Compose abstraction; or
- require an owner that mutates `RecommendationUiState`, knows recommendation keys/locks, and also accepts UI/instrumentation callbacks.

Neither is a cleaner ownership boundary.

### 2. Automatic setup recommendation application effect

The separate automatic-storyteller `LaunchedEffect` reacts to automation mode/style plus the ready recommendation UI state. It:

- asks `recommendationCoordinator.selectSetupPlan(...)` for the typed automatic plan;
- records selection preview telemetry;
- invokes the existing durable `onApplyRecommendation(automaticPlan)` boundary;
- records committed selection telemetry;
- updates only local selected/applied recommendation style state.

This is deliberately not the same lifecycle as recommendation generation. Generation can run in manual/assisted modes, while automatic application is conditional on automation policy and a ready result.

A new common owner would have to take the recommendation coordinator, telemetry recorder, durable apply callback and local Compose style setters. That is orchestration plumbing, not a cohesive new domain responsibility.

The actual setup selection policy already belongs to `ClocktowerRecommendationCoordinator`; no second selection owner should be introduced.

### 3. First-night natural-pair precompute consumption effect

The first-night natural-pair `LaunchedEffect` owns only point-of-use UI lifecycle:

- derives the exact current request;
- checks `firstNightNaturalPairReadyProvider` first;
- otherwise awaits `firstNightNaturalPairResultProvider` off-main;
- rejects stale completion with `isActive`;
- updates local candidate/loading-failure state.

The real exact-input precompute lifecycle already belongs to `TroubleBrewingFirstNightPrecomputeCoordinator`.

That coordinator explicitly owns:

- exact request identity;
- MISS / BUSY / READY state;
- BUSY await;
- READY reuse;
- failed-result fallback;
- cancellation behavior;
- stale-result rejection.

Its source contract also deliberately leaves dispatch policy to the caller: reveal wiring supplies an off-main launcher, while point-of-use resolution runs from an already-safe background context.

Therefore moving the Compose effect into another controller would duplicate the coordinator's lifecycle or violate its caller-owned dispatch boundary.

### 4. First-night start gate effect

A second tiny effect waits for both:

- `firstNightNaturalPairStartRequested`;
- `firstNightNaturalPairPrecomputeReady`.

When both become true it clears the local start request and flips `nightStarted = true`.

This is route/interaction gating, not precompute ownership. It intentionally remains adjacent to the UI state that requested entering the night.

Combining this with the precompute coordinator would make a reusable computation/cache owner responsible for navigation state. That is the wrong direction.

### 5. First-night poison / information invalidation effect

The poison-target observer invalidates only unshown first-night information when the confirmed poison target changes during First Night.

The semantic operation itself is already pure and typed: `FirstNightInformationLifecycle.invalidateUnshown()` advances the generation and clears only ready/unshown decisions while preserving displayed decisions.

The Compose effect merely observes an external confirmed mechanical input and applies that pure transition to the interaction-local first-night migration state.

It must not be merged into setup recommendation loading or A4 prewarm merely because all are implemented with `LaunchedEffect`.

### 6. App-level A4 effects are separate performance/cache lifecycles

`CampBoardGameHostApp.kt` has distinct A4 effects:

- identity-reveal prewarm keyed by reveal-active/session/input identity;
- observation-cache rebuild keyed by a rebuild request.

The identity-reveal effect delegates to `A4IdentityRevealPrewarmCoordinator`, owns Compose coroutine cancellation/frame telemetry, and guarantees cancellation/reporting in `finally`.

The observation-cache effect delegates the rebuild to `A4ObservationCacheRebuildExecutor` off-main and logs its report.

These effects do not mutate setup recommendation UI state or own setup application. They are A4 performance/cache lifecycles and should stay separate from setup recommendation orchestration.

The initial-recommendation-demand callback passed into Judge is also only an A4 probe/telemetry hook; it does not make A4 prewarming part of recommendation ownership.

## Setup commit/reveal already has a dedicated orchestration owner

The earlier transition from committed Trouble Brewing setup into identity reveal is **not** an unowned Compose effect.

`TroubleBrewingSetupRecommendationRevealCoordinator.onCommittedDeal(...)` already establishes the meaningful ordering contract:

1. call `enterReveal()`;
2. then launch recommendation prewarm in the background.

`TroubleBrewingSetupRecommendationPrewarmCoordinator` separately owns exact-request setup recommendation cache/reuse.

In App composition, the `enterReveal` callback still owns application-specific work such as committed setup/session updates and persistence, and it starts the separate first-night precompute coordinator after the committed setup exists. The reveal coordinator only owns the narrow ordering it can honestly name.

This is the preferred D6 architecture: several small coordinators with explicit responsibilities, rather than one broad setup effect controller.

## Why there is no honest common transaction

A hypothetical common effect owner would need to receive some combination of:

- recommendation key and lock state;
- recommendation provider/coordinator;
- automatic storyteller mode/style;
- selection telemetry recorder;
- durable `onApplyRecommendation` callback;
- first-night precompute ready/result providers;
- local loading/failure/start state setters;
- poison target and first-night information migration;
- A4 prewarm/cache requests and executors;
- coroutine dispatch policy;
- route/night-start mutation;
- persistence/session callbacks.

That parameter surface would be broader than the responsibility it claims to isolate. It would be a state/action bag whose only common property is "these operations happen near setup or First Night".

Temporal proximity is not sufficient cohesion.

## Rejected shapes

This audit explicitly rejects:

1. `SetupEffectContext` / `SetupEffectArgs` containing recommendation, A4, first-night and route state;
2. a generic controller that owns multiple `LaunchedEffect` lifetimes outside Compose;
3. moving `nightStarted` routing into the first-night precompute coordinator;
4. moving persistence/session mutation into recommendation prewarm/reveal coordinators;
5. merging A4 identity/cache prewarm with setup recommendation loading;
6. making telemetry callbacks part of setup recommendation domain ownership;
7. extracting a wrapper whose only benefit is reducing lines in Host/App while preserving the same dependency fan-out.

## D6 consequence

D6.2x closes the conditional setup-effect-owner item from the global R2 plan as **not necessary**.

R0–R2 now have a natural first-wave stopping point:

- R0 dead/dormant UI and decoder cleanup completed;
- R1 information preparation and one proven role-local materializer boundary completed;
- further generic numeric/materializer-family extraction rejected where cohesion was insufficient;
- R2 NightStep numeric/boolean/target extraction rejected because typed specialized owners already exist;
- Day vote residual extraction rejected because transient table state + atomic vote transaction already form the correct boundary;
- setup effect aggregation rejected because the surviving effects have different lifetimes and existing dedicated coordinators.

The next action should be the planned **R0–R2 first-round acceptance remeasurement**, not another speculative extraction.

## Next — D6.2y first-wave acceptance remeasurement

Before considering R3 deep App transactions:

1. remeasure the principal large production files and key composition parameter counts against the pre-D6.2 / R0 checkpoints;
2. summarize actual deleted/moved/added production code and the number of new typed owners;
3. verify no new broad state/action bags were introduced;
4. inspect the PR diff for accidental ownership drift or duplicated authority;
5. run the planned first-wave FULL/T4 + focused R2 acceptance gate on the latest production checkpoint, unless no production has changed since the already-validated D6.2s checkpoint — in that case first determine whether the existing D6.2s FULL-equivalent coverage satisfies the documented acceptance rule or whether one final fresh full run is still required;
6. record real-device coverage honestly and do not claim it if unavailable.

Only after that acceptance checkpoint should the campaign decide whether R3 (night confirm / Day execution / Dawn-succession deep transactions) has sufficient remaining ROI to justify a second wave.

## Validation classification

This slice is documentation/read-only architecture work only. No production source, tests, workflows, persistence, recommendation semantics, A4 behavior, first-night behavior or runtime ordering changed. Per `AGENTS.md` and `TESTING_STRATEGY.md`, no Android RED/GREEN or broad regression run is required for this docs-only checkpoint.
