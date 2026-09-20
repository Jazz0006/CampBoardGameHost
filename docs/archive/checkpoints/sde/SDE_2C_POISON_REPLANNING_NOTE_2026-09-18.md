# SDE-2C — Poisoner invalidation / replanning completion note

> Date: 2026-09-18 Australia/Sydney  
> Status: **COMPLETE**  
> Branch: `sde-1-orchestration-seam`  
> PR: #144  
> Executable validation SHA: `fd90b8dc0433dbd925f0be9f94f3a2693e416f4b`

## 1. Objective

SDE-2C proves the first-night Poisoner lifecycle without adding another recommendation-state or revision authority.

Required behavior:

```text
Poisoner source facts change
→ prior PLANNED / UNCOMMITTED decisions become stale
→ stale plans are discarded
→ existing legality/materialization owners rebuild candidates
→ SDE exact consequences are recomputed
→ fresh PlannedDecisionRef values bind the current session revision
```

Already COMMITTED player-visible information remains durable history and is never rewritten.

## 2. Existing authorities were sufficient

No new production abstraction was required.

### Draft target

A Poisoner draft edit remains player input:

```text
ClocktowerGameSession.recordPlayerInput()
→ playerInputRevision + 1
→ prior PlannedDecisionRef becomes stale
```

### Confirmed target

Production confirmation already owns the durable boundary:

```text
ClocktowerGameSession.commitGlobalActionFact(ActionFactDraft.Poison)
→ ClocktowerGameSession.commitPoisonTargetBoundary(targetSeat)
→ gameStateRevision + 1
```

The action fact is durable Storyteller/mechanical history. The poison projection is canonical session mechanical state.

### Replanning

`StructuredInformationProductionShadow` already rebuilds first-night exact input from:

- persistent `CommittedClocktowerSetup`;
- current canonical `GameSnapshot`;
- current session revisions;
- durable action timeline;
- durable epistemic observation log.

`StructuredInformationShadowAdapter` then produces fresh revision-bound plans and routes exact consequence evaluation through `StorytellerDecisionEngine`.

No SDE-specific replanning counter, generation store, dependency graph, or alternate GameState owner was added.

## 3. Broad invalidation proof

Focused regression:

`Sde2PoisonReplanningTest`

The test plans two independent first-night information decisions before poison:

- Empath;
- Chef.

Then it performs:

```text
draft poison edit
→ playerInputRevision changes
→ both prior plan sets become stale

confirmed poison on Empath
→ durable Poison action committed
→ canonical poison state committed
→ gameStateRevision changes
→ new Empath context is rebuilt as POISONED
→ new Chef context is rebuilt at the same fresh revision
→ both receive fresh PlannedDecisionRef values
```

This deliberately proves broad correct invalidation rather than only replacing the poisoned role's one clue.

The unaffected Chef may retain the same legal candidate IDs and the same player-visible exact diagnostics, but its old plan is still stale because it was produced from an obsolete source revision.

## 4. Hidden Poison target remains outside player knowledge

The first RED version incorrectly assumed that a confirmed Poisoner target should directly change the recipient's exact possible-world baseline.

That assumption is wrong.

`PlayerHistoricalTimeline` intentionally excludes hidden Storyteller/mechanical actions such as:

- Poison;
- Protect;
- Attack;
- RoleChange.

This prevents the exact player-world replay from learning Storyteller-hidden choices.

Therefore SDE-2C freezes the following distinction:

```text
Storyteller-side current effective state / legality
    DOES observe the confirmed poison target

recipient epistemic baseline
    DOES NOT learn the hidden poison target merely because Storyteller committed it
```

A poisoned information role can therefore have broader legal Storyteller outputs while those outputs still have ordinary epistemic consequences from the recipient's point of view.

This is required information-hiding behavior, not a replay deficiency.

## 5. Durable history invariants

The regression proves:

- planning alone does not append observations;
- planning alone does not move session revisions;
- an already COMMITTED observation remains byte-for-byte unchanged across poison draft/confirmation/replanning;
- stale detection uses the existing `gameStateRevision` / `playerInputRevision`;
- fresh plans are bound through `PlannedDecisionRef`;
- session remains the sole durable writer.

## 6. Production selection

SDE-2C remains shadow/evaluation infrastructure.

It does **not**:

- select a production clue from exact diagnostics;
- replace current recommendation ranking;
- change UI behavior;
- create a new Poisoner policy;
- change night ordering;
- rewrite committed information.

Production selection cutover remains a later stage.

## 7. Executable validation

Executable SHA:

`fd90b8dc0433dbd925f0be9f94f3a2693e416f4b`

CI run:

`35296161489`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

The relevant exact registration semantics from SDE-2B remain separately cross-validated by Real Clingo at `a977c01c0f4ae634dd60e4999759838f4005228c`.

Later documentation-only commits are not executable validation evidence.

## 8. Frozen result

> **Poisoner changes invalidate all still-uncommitted first-night plans through existing session revisions. Replanning rebuilds legal candidates and exact consequences from current authoritative state/history, while already committed observations remain immutable. Hidden Poisoner choices affect Storyteller-side effective legality but are not injected into recipient knowledge. No additional replanning revision, dependency store, GameState authority, or production selection cutover is required.**
