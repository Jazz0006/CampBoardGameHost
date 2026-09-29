# DLB-5 Staged First-Night Dependency Planner Audit — 2026-09-29

> Status: **CURRENT DLB-5 IMPLEMENTATION / CLOSEOUT AUTHORITY**
> Baseline at audit creation: `main@e83b413a1583c8d2a9cdef8126f2401cd610e093`
> Last synchronized: 2026-09-30 Australia/Sydney
> Current Draft PR: **#183 — `DLB-5: stage first-night dependency barriers`**
> Latest validated executable checkpoint: `bdc31a31bc4652be13e31d7d4c8c6bacd68b9dd5`; CI #3600 / R2 #3331 GREEN
> Current status: **DLB-5.1 COMPLETE; DLB-5.2 COMPLETE; DLB-5.3 COMPLETE; DLB-5.4 COMPLETE; DLB-5.5 COMPLETE; DLB-5 overall COMPLETE / final PR acceptance pending**
> Scope: latest-safe commitment barriers for Trouble Brewing first-night setup facts. No Beginner Drunk-policy cutover, no DLB-6 retirement, no broad App/Host decomposition.

## 1. Canonical rule

DLB-5 follows one rule:

> A Storyteller-controlled value may remain uncommitted until the latest semantically safe point, but it must be committed before the first interaction that can observe it or whose legal/result semantics depend on it.

This is a commitment-lifecycle rule, not a recommendation-policy rewrite.

## 2. Baseline production ownership and defects addressed by DLB-5

### 2.1 Early setup auto-apply is the primary defect

At the audit baseline, `CampBoardGameHostApp.onApplyRecommendation` immediately copied two setup-plan decisions into runtime state as soon as the setup recommendation became Ready:

- `StorytellerDecision.RedHerring` -> `clocktowerRedHerring`;
- `StorytellerDecision.DemonBluffs` -> `clocktowerRecommendedDemonBluffRoleNames`.

That happens before first-night order reaches either semantic dependency. It is the main DLB-5 cutover target.

The setup recommendation service may still evaluate these candidate families early. Evaluation is not commitment. DLB-5 must separate:

```text
candidate generation / evaluation / recommendation
!=
canonical runtime commitment
```

### 2.2 Demon bluffs

Current presentation has already separated legality and rendering reasonably well:

- `SetupCandidateGenerator.generateDemonBluffCandidates` owns the setup legal domain;
- SDE shadow/joint-output evaluators consume that legal domain;
- `demonBluffRoleNamesForPresentation` / `resolveDemonBluffPresentation` own presentation resolution;
- the first actual observer is the first-night Demon-info interaction.

At the audit baseline, AUTO required an already-applied `clocktowerRecommendedDemonBluffRoleNames`, so setup recommendation readiness committed the value too early. DLB-5.3 removes that early commitment.

Target:

```text
setup recommendation may be ready early
-> bluff decision stays uncommitted
-> immediately before first Demon-info presentation dependency
     resolve/select the current recommended legal triple
     commit once
-> presentation consumes committed triple
```

Experienced/manual presentation may continue to expose the current recommendation before commitment only as an uncommitted recommendation; actual player reveal must consume the committed value.

### 2.3 Red Herring

At the audit baseline, `FortuneTellerInteractionHandler` inserted a Storyteller setup interaction directly before the Fortune Teller role interaction; DLB-5.2 now reorders the commit interaction to the typed earliest-safe barrier.

That is correct only when no earlier interaction can observe the Red Herring fact.

Trouble Brewing Spy acts before Fortune Teller and may observe the Grimoire. Therefore:

- if a live/effective Grimoire observer can actually see the Grimoire, Red Herring must be committed before that observation;
- otherwise commitment may remain deferred until immediately before Fortune Teller;
- Drunk shown as Fortune Teller must not create a real Red Herring commitment.

The implementation must use a typed observation/dependency contract. Do not encode a Host-level `if (Spy)` timing branch.

### 2.4 Poisoner and downstream first-night information

This area already has the correct core lifecycle and should be reused:

- Poisoner target is player-controlled;
- App confirmation calls `ClocktowerGameSession.commitPoisonTargetBoundary`, publishes the canonical session view and invalidates revision-scoped caches;
- Host observes first-night `poisonTarget` changes and calls `FirstNightInformationMigration.invalidateUnshown()`;
- that invalidation retains already displayed observations and discards unshown candidate drafts;
- `clocktowerFirstNightInformationRequest` builds current candidate semantics against `cards.toClocktowerGameState(..., poisonTarget)`.

Therefore DLB-5 does **not** need a new first-night information cache/controller. The acceptance task is to preserve and characterize this existing latest-state behavior while removing the early setup commitments around it.

## 3. Required typed dependency surface

Introduce a small flow/session-level contract for deferred first-night setup facts.

Suggested semantic vocabulary:

```text
DeferredFirstNightSetupFact
- DEMON_BLUFFS
- RED_HERRING

FirstNightSetupFactDependency
- PRESENTATION_REQUIRED
- OBSERVATION_REQUIRED
- RESULT_SEMANTICS_REQUIRED
```

A dependency planner receives the ordered first-night interaction sequence plus typed dependency descriptors and returns the earliest interaction before which each still-uncommitted fact must be committed.

The planner must be role-name agnostic. Role-specific capability registration is allowed at a typed registry boundary; the barrier calculation itself must operate only on semantic facts/capabilities.

Do not put localized text, Compose state, recommendation scores, session mutation, or legality generation into this planner.

## 4. Red Herring observation capability

The Red Herring barrier needs two dependency sources:

1. Fortune Teller result semantics require Red Herring before the real Fortune Teller role interaction.
2. Any interaction that can observe the current Grimoire requires Red Herring before that observation.

The current Trouble Brewing Spy is one concrete Grimoire observer. Future characters/scripts must be able to register the same semantic capability without changing the dependency algorithm.

Poisoned/malfunctioning observers that are deliberately not shown the real Grimoire must not force an earlier observation barrier merely because their role normally has that capability.

This effective-observer condition belongs at the adapter that projects current runtime facts into dependency descriptors, not in the generic planner.

## 5. Demon bluff presentation dependency

The Demon-info system interaction is the first presentation dependency for Demon bluffs.

The generic planner should be able to express this as:

```text
interaction: first-night Demon info
dependency: DEMON_BLUFFS / PRESENTATION_REQUIRED
```

No setup-time commitment is required merely because the setup recommendation result is available.

## 6. Existing first-night information lifecycle to preserve

`FirstNightInformationMigration` remains authoritative for JIT information publication:

```text
current effective state
-> build current request/candidate domain
-> publish/resolve
-> display = sole observation commit boundary

poison target changes
-> invalidateUnshown()
-> displayed observations stay committed
-> all unshown drafts are discarded/rebuilt
```

DLB-5 should add regression coverage where necessary, not create a parallel staged-information planner.

## 7. Implementation slices

### DLB-5.1 — typed dependency planner — COMPLETE

Add the pure semantic contract and planner only.

Acceptance:

- returns Demon-bluff barrier at Demon-info presentation;
- returns Red-Herring barrier at Fortune Teller when no earlier Grimoire observer exists;
- returns Red-Herring barrier before an earlier effective Grimoire observer;
- ignores a non-observing/malfunctioning interaction;
- no role-name branching exists in the planner.

This is a durable new contract and should use RED/GREEN.

### DLB-5.2 — Red Herring production cutover — COMPLETE

- stop AUTO setup-plan application from committing Red Herring;
- project current effective Grimoire observation capability into the dependency planner;
- commit the selected/recommended legal Red Herring at the barrier;
- keep manual Storyteller authority on the rules-owned legal target domain;
- ensure Drunk shown Fortune Teller still creates no Red Herring;
- bind committed SDE input only after actual commitment.

Acceptance includes Spy-before-Fortune-Teller and poisoned-Spy cases.

### DLB-5.3 — Demon bluff production cutover — COMPLETE

- stop AUTO setup-plan application from committing Demon bluffs;
- at Demon-info barrier, resolve/select the current recommended legal triple;
- commit exactly once before reveal;
- presentation consumes the committed triple;
- fail closed if recommendation is pending/invalid rather than inventing a triple.

### DLB-5.4 — first-night information / poison convergence audit — COMPLETE

No production rewrite was required. Existing typed lifecycle and replanning evidence closes all four acceptance statements:

- `ClocktowerGameSession.commitPoisonTargetBoundary` owns exactly one canonical game-state revision for a confirmed poison target; `ClocktowerGameSessionPoisonBoundaryTest` characterizes the one-revision contract.
- `ClocktowerHostScreen` observes confirmed first-night `poisonTarget` changes and calls `FirstNightInformationMigration.invalidateUnshown()`; `FirstNightInformationLifecycleTest` and `FirstNightInformationMigrationTest` prove that ready/unshown drafts are discarded while displayed decision IDs and displayed observations remain unchanged.
- `performClocktowerPlayerRevealHandoff` records private observation/history only for a newly created first-night publication; reopening an already published decision opens the reveal without duplicating observation/history.
- `clocktowerFirstNightInformationRequest` and the Host pair-information projectors rebuild from the current `poisonTarget`; recommendation inputs also consume the already committed `events` history. `Sde2PoisonReplanningTest` proves that poison confirmation advances the canonical revision, preserves the committed epistemic observation log, stales prior uncommitted plans, and regenerates subsequent exact consequences against the poisoned snapshot/current revision.

Producer/consumer search found one production poison-confirmation boundary in `CampBoardGameHostApp`, one first-night draft invalidation owner in `ClocktowerHostScreen`, and the existing shared `FirstNightInformationMigration` lifecycle. No second cache/controller/planner is needed and no production behavior gap was found.

### DLB-5.5 — cleanup within DLB-5 scope — COMPLETE

After 5.2/5.3 production cutover:

- remove only the now-obsolete early App auto-apply mutations for Red Herring / Demon bluffs;
- keep setup recommendation generation/shadow evaluation unless DLB-6 explicitly retires it;
- re-run producer/consumer searches.

Do not perform DLB-6 old-contract retirement here.

## 8. H1 presentation extraction gate

DLB-5H1 remains deferred until the above lifecycle exists in production.

Only then may first-night minion/demon presentation be extracted if it has a narrow seam:

```text
committed presentation facts
+ existing DemonBluffPresentationResolution
-> localized content
```

That module must not choose candidates, own commitment timing, mutate session state, or own setup legality.

## 9. Explicit non-goals

DLB-5 must not:

- choose Poisoner target for the player;
- use future Poisoner/Fortune-Teller choices as hindsight;
- promote `DRUNK_ASSIGNMENT_SHADOW_V1` or modify frozen `BEGINNER_CONSERVATIVE_V1`;
- create a generic global transaction manager;
- duplicate `FirstNightInformationMigration`;
- persist uncommitted setup facts into Emergency Recovery;
- add old Recovery compatibility;
- perform DLB-6 retirement early.

## 10. Current implementation record and immediate next step

Current PR #183 implementation sequence:

- `1893b44688596540ac3084e3c4109d904019979a` — DLB-5.1 dependency planner GREEN;
- `c0f79e2bbf809f99d514a1f002b05e303f543781` / `4eb15fab46f208dfbe756cb4f805b1e1bcfb292a` — DLB-5.2 capability projection and production ordering;
- `ae0eadd66398bc5e6210616720219f0cd7a0a80b` — DLB-5.2C Red Herring barrier commit GREEN;
- `bde21c264fa8430be2f24f7a54c4bc83fdf6524e` — DLB-5.3 Demon-bluff barrier commit GREEN;
- `bdc31a31bc4652be13e31d7d4c8c6bacd68b9dd5` — DLB-5.5 obsolete setup auto-apply cleanup; CI #3600 / R2 #3331 GREEN.

**DLB-5.4 result: COMPLETE with no production gap.**

The existing Poisoner / first-night information lifecycle satisfies the four closeout statements in §7 through the typed session, migration, reveal-handoff and replanning contracts above. No production file changed for 5.4.

**Immediate next step:** synchronize roadmap/handoff, perform final PR #183 changed-files/scope/review-thread/exact-head CI/R2 audit, then mark ready and merge under the standing authorization. After merge, query live `main` and choose the next phase from the current authoritative route rather than from historical audit NEXT markers.
