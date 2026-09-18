# SDE-2D2 Demon Bluff Joint-Output Fanout / Ownership Audit

> Date: 2026-09-18 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-2d2-demon-bluff-joint-output`  
> Baseline main: `6fc0d99f1a250b91925280ed12ec0b199220b060`  
> Status: **SDE-2D2 EXECUTION AUDIT / IMPLEMENTATION AUTHORITY**

## 1. Live-state correction

PR #145 is merged. Live `main` is `6fc0d99f1a250b91925280ed12ec0b199220b060`.

The merge-status text still present in `CURRENT_DEVELOPMENT_ROADMAP.md` and `NEXT_DEVELOPMENT_HANDOFF.md` is stale. SDE-2D1 is therefore complete on live main and the SDE-2D2 start gate is satisfied.

## 2. Frozen target

SDE-2D2 migrates **strategic choice of uncommitted Demon bluff triplets** into the Storyteller Decision Engine whole-bundle ecology.

Ownership remains:

```text
SetupCandidateGenerator
    -> legal Demon bluff triplets

epistemic exact evaluator
    -> exact counterworld consequences / structure

StorytellerDecisionEngine / SDE policy
    -> compose strategic bluff support
    -> later select among still-uncommitted legal triplets

applied / shown Demon bluff triplet
    -> persistent input for later planning
```

This slice does not create a second rules engine, second world solver, or second persistence authority.

## 3. Production fanout audit

### 3.1 Legal producer — MUST REMAIN

`recommendation/setup/SetupCandidateGenerator.kt`

- `generateDemonBluffCandidates()` is the direct legal candidate producer.
- Trouble Brewing requires a Demon, at least seven players, and three distinct out-of-play GOOD roles.
- `generatePlans()` composes the bluff decision with other setup decisions.
- a supplied locked `StorytellerDecision.DemonBluffs` filters the legal domain rather than regenerating a different triplet.
- stable IDs and canonical plan identity already include the bluff triplet.

**Decision:** legality stays here. SDE consumes already-legal candidates and must not reproduce these rules.

### 3.2 Current strategic owner — MIGRATION TARGET

`recommendation/setup/SetupRecommendationService.kt`

`rankedPlans()` currently evaluates the non-bluff base plan and then adds:

```text
demon-bluff-ease
= sum(5 - bluffDifficulty(role))
  × profile.bluffEaseWeight
```

This is role-metadata ease scoring. It does not ask whether the three bluff narratives are supported by the current whole-bundle public information, whether they depend on the same narrow counterworlds, or whether an information-role bluff has viable compatible claims.

**Decision:** `demon-bluff-ease` remains compatibility behavior during shadow migration only. It is not the target strategic authority.

### 3.3 Presentation / applied boundary — PRESERVE

`ClocktowerDemonBluffPresentation.kt`

- applied role names take precedence over recommendation output;
- automatic presentation waits for the already-applied setup decision;
- missing recommendation remains pending;
- partial / duplicate / illegal triples are invalid and are never silently replaced.

**Decision:** presentation remains a consumer, not a policy owner.

### 3.4 Active-game persistence / recovery — PRESERVE

Current host state stores applied Demon bluff names in `ClocktowerRecoveryMechanics.demonBluffRoleNames`, writes them into active-game recovery state, and restores them on recovery.

`onApplyRecommendation` copies the chosen `StorytellerDecision.DemonBluffs` into that applied state and advances the player-input revision only when the applied setup choice changes.

**Decision:** SDE-2D2 does not add Demon bluffs to `CommittedClocktowerSetup` or invent another persistence DTO. Existing applied/recovery state is the current persistent commitment carrier.

### 3.5 Setup completion history — INTENTIONALLY EXEMPT

`TroubleBrewingSetupCompletionPersistence.kt` stores compact rotation/completion metadata and does not own live bluff commitments.

**Decision:** no Demon-bluff field is added there.

## 4. Why a bluff is not an ordinary current observation

A Demon bluff identity is a future narrative option. Before the Demon claims a bluff, the triplet is not a public fact and must not be appended to durable observation history.

Using the ordinary PUBLIC_GOOD_INFO claim shape directly would also be useless for support measurement:

```text
speaker is evil
OR claimed-good branch
```

because the actual Demon can always satisfy the evil-lie branch.

The useful diagnostic question is instead:

> Under the already-visible whole-bundle facts, does a recipient still have a mechanically legal counterworld in which the **actual Demon seat** is shown as this bluff role?

This is a strict hypothetical `ShownRoleAt(actualDemonSeat, bluffRole)` probe. It is an evaluation-only counterworld filter, not a claim, commit, or mutation.

## 5. Exact support overlay

For one current whole-bundle public observation set:

```text
shared public whole-bundle observations
        +
strict ShownRoleAt(actualDemonSeat, bluffRole)
        ↓
ExactHistoricalHypotheticalObservationBundleEvaluator
        ↓
exact role-support diagnostics
```

Evaluate each **distinct legal bluff role once per recipient**.

Then compose legal triplets from the cached per-role support results:

```text
role A support ─┐
role B support ─┼─> triplet A/B/C diagnostics
role C support ─┘
```

Do **not** run a separate full exact enumeration for every triplet.

The exact evaluator remains the world/consequence authority. SDE only composes already-exact diagnostics.

## 6. First durable D2 contracts

### D2A — role-support exact overlay

Add a small SDE seam that:

- accepts the actual Demon seat;
- accepts the already-public whole-bundle observations;
- accepts already-legal bluff triplets;
- canonicalizes the distinct bluff roles;
- makes one exact support query for each role/recipient pair;
- returns exact diagnostics keyed by role;
- performs no selection and no mutation.

### D2B — triplet joint-output composition

For every legal triplet, expose its three role-support records from the shared cache.

Initial descriptive outputs may safely include:

- per-role exact support by recipient;
- whether each role has at least one surviving counterworld for a recipient;
- the supported evil-team seat configurations already present in exact diagnostics;
- overlap / equality of role-support topology sets as descriptive redundancy evidence.

Do not freeze policy thresholds or a global bluff score in this step.

### D2C — committed persistence proof

Strengthen typed lifecycle evidence:

- an applied triplet wins over a changed pending recommendation;
- later recommendation/replanning must not silently replace the applied triplet;
- recovery preserves the applied triplet through the existing recovery owner.

No new lifecycle counter is introduced.

### D2D — shadow integration / explicit cutover gate

Only after D2A–C are validated:

- wire SDE bluff diagnostics beside the existing setup recommendation path;
- prove existing visible production selection is unchanged while shadow diagnostics are produced;
- perform an explicit later cutover before removing `demon-bluff-ease` as strategic authority.

## 7. D2 / D3 boundary

SDE-2D2 may use the existing exact structure:

- possible Demon seats;
- evil-team seat configurations;
- forced-good / forced-evil seats;
- evil cover.

It must **not** introduce a replacement strategic-world solver or prematurely freeze the final `StrategicWorldKey(demonSeat, minionSeats)` schema. That durable quotient belongs to SDE-2D3.

## 8. Test / validation plan

For the new durable D2A/B seam:

1. create a typed test before production implementation;
2. prove the role-support result is exact-evaluator-equivalent for the same strict probe;
3. prove shared roles appearing in multiple triplets are represented by one role-support record rather than triplet-owned recomputation;
4. prove the input legal candidate identities/roles are preserved;
5. prove evaluator calls do not mutate timeline or observation history.

For lifecycle:

- use typed presentation/recovery tests; do not add source-string behavior tests.

Checkpoint validation follows `AGENTS.md` / `TESTING_STRATEGY.md`.

## 9. Explicit non-goals

This audit does not authorize:

- duplicating Demon-bluff legality in SDE;
- treating unclaimed bluffs as durable observations;
- changing Host UI;
- replacing exact evaluation with bluff metadata heuristics;
- deleting `bluffDifficulty` before explicit production cutover;
- starting SDE-2D3 or SDE-3;
- adding a second setup/session/persistence authority.

## 10. Stable implementation decision

> **SDE-2D2 starts with an exact per-role bluff-support overlay over the current whole-bundle public facts, keyed at the actual Demon seat. Legal triplets remain setup-owned. SDE composes triplet diagnostics from shared per-role exact results, so the system does not multiply full world enumeration by every triplet. Applied bluffs continue through the existing persistent recovery boundary and are not replanned. Production selection remains unchanged during the initial shadow migration.**


## 11. Implemented checkpoint

SDE-2D2 implementation now satisfies the audited ownership split:

### D2A — COMPLETE

- setup-owned legal bluff candidates are projected losslessly into SDE;
- the SDE adapter no longer calls `SetupCandidateGenerator` itself;
- exact support is evaluated as hypothetical `ShownRoleAt(actualDemonSeat, bluffRole)` over the supplied public whole-bundle facts;
- no observation/history mutation occurs.

### D2B — COMPLETE

- distinct bluff roles are evaluated once per recipient at the SDE layer;
- legal triplets reuse the same `DemonBluffRoleSupport` objects;
- triplet diagnostics expose supported roles, evil-team topology union, shared topology intersection and distinct topology-pattern count;
- no opaque scalar or D3 strategic-world schema has been introduced.

### D2C — COMPLETE

- applied bluff roles continue to win over changed pending recommendation output;
- recovery round-trip preserves the applied triplet;
- `ClocktowerRecommendationCoordinator.evaluateSetupDemonBluffShadow()` rejects a request containing locked `DemonBluffs`, so committed/persistent bluff choices cannot enter uncommitted replanning;
- no new persistence DTO, revision counter or lifecycle owner was added.

### D2D — COMPLETE AS SHADOW PATH

The validated orchestration shape is:

```text
existing SetupRecommendationService visible result
        +
SetupCandidateGenerator legal bluff candidates
        +
caller-supplied exact historical / whole-bundle context
        ↓
ClocktowerRecommendationCoordinator.evaluateSetupDemonBluffShadow()
        ↓
TroubleBrewingDemonBluffJointOutputEvaluator
        ↓
exact epistemic evaluator
        ↓
DemonBluffSetupShadowAdapter
        ↓
unchanged visible result
+ legacy selected candidate IDs by style
+ SDE joint-output diagnostics
```

The existing production recommendation result object is returned unchanged. No Host/UI caller has been switched to the shadow result and `demon-bluff-ease / bluffDifficulty` remains the production compatibility selector pending the later explicit cutover phase.

## 12. Exact fanout performance correction

Initial SDE-2D2 CI exposed an important execution issue: the pristine exact evaluator grouped strict shown-role queries by the full `ShownRoleAt(seat, role)` identity. Four candidate bluff roles at the same Demon seat therefore regenerated and rescanned the same pristine mechanical world stream four times.

That execution strategy was corrected **inside the epistemic exact owner**, not in recommendation:

```text
same recipient
+ same strict-shown seat set
+ same public-claim identity envelope
        ↓
one pristine world-generation pass
        ↓
each role value checked exactly per query
```

Consequences:

- exact semantics remain per-query;
- role values are not merged semantically;
- SDE does not cache or own mechanical worlds;
- different bluff roles for the same Demon seat share the expensive source enumeration;
- Real Clingo cross-validation passed after the change.

The real whole-bundle Demon-bluff contract remains intentionally expensive enough to be classified as affected T2 / T3 execution rather than ordinary FAST. `DemonBluffJointOutputEvaluatorTest` remains in `:app:testFull`; the FAST exclusion and its measured rationale are recorded in `TESTING_STRATEGY.md`.

## 13. Final ownership audit

Exact diff review confirms:

- `SetupRecommendationService.kt` is unchanged;
- `SetupCandidateGenerator` remains the only bluff legality owner;
- SDE package code does not call the setup candidate generator;
- `ClocktowerRecommendationCoordinator` is the only new cross-boundary orchestration caller;
- no UI production path consumes the new shadow diagnostics;
- no persistence schema changed;
- no session/history commit API changed;
- no selection/fallback path was added to the exact evaluator;
- no second strategic-world solver was introduced;
- committed bluff persistence uses the pre-existing applied/recovery owner.

Therefore SDE-2D2 establishes the **validated SDE strategic path and lifecycle proof** required before D2D3, but deliberately does **not** perform production strategic-selection cutover. Retirement of `demon-bluff-ease` remains an explicit later cutover concern.

## 14. T4 acceptance checkpoint

This document update is the logical SDE-2D2 acceptance checkpoint and is committed with `[full-ci]`.

Required acceptance evidence:

```text
R2 main-thread boundary
Android testFull
Debug APK assemble
ASP contract validation
Real Clingo cross-validation
CI gate
```

Do not advance the roadmap/handoff to SDE-2D3 until this checkpoint is green.


### T4 retry note

The first `[full-ci]` attempt reached all selected gates. R2, ASP and Real Clingo passed, while Android `:app:testFull` exposed one fixture-boundary failure in `DemonBluffJointOutputEvaluatorTest`.

The failing test had reused a deliberately reduced exact-world role domain as the input to the legacy `SetupRecommendationService`. That made the test alter the existing setup scorer's production role domain while trying to prove shadow non-interference.

The evidence was corrected by separating the concerns:

- exact Demon-bluff consequence/parity evidence uses the bounded mechanical fixture only;
- production setup shadow wiring uses the full Trouble Brewing setup role domain;
- the wiring test forces exact capability deferral, so it proves the visible legacy result remains unchanged without performing a second expensive enumeration;
- the committed-bluff rejection test uses a typed empty visible result because rejection occurs before exact evaluation or visible-result inspection.

No production behavior was changed by this correction.

This updated audit commit requests a fresh `[full-ci]` acceptance run. Only the successful retry may be cited as final SDE-2D2 acceptance evidence.


### T4 retry 2 root-cause correction

The second full Android attempt exposed a real shadow-adapter identity bug rather than an exact-semantic failure.

The two existing setup producers encode the same legal bluff triplet with different list ordering:

- `generateDemonBluffCandidates()` sorts the three role IDs before constructing the legal candidate;
- `generatePlans()` preserves script/catalog order in the visible legacy `StorytellerDecision.DemonBluffs`.

The initial shadow adapter incorrectly used the raw `List<RoleId>` as the lookup key, so an order-only difference made a valid visible triplet appear absent from the legal candidate domain.

The correction canonicalizes triplet identity by sorted role ID **only inside the shadow comparison adapter**. No setup legality, production selection, persistence or exact semantics changed.

The existing production-shadow wiring test now protects this cross-owner ordering difference. This document commit requests a fresh `[full-ci]` run; only that successful run is final SDE-2D2 acceptance evidence.
