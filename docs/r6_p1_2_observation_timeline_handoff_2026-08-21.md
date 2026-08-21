# R6 / P1.2 Observation Timeline Migration Handoff

Date: 2026-08-21  
Baseline: `main` after R6.2 merge `6520dc39ecadfcdf518a5a52b77d0626e9a396f6`  
Development branch: `codex/r6-observation-timeline-contract`

## Completed foundation

R6.1 and R6.2 are merged and green.

R6.1 established:

- `TimelinePoint.globalSequence: Long`;
- global sequence as the cross-phase/cross-round ordering authority;
- canonical RegistrationQuery serialization including global sequence;
- explicit fail-closed behavior for legacy schema-v2 TimelinePoint JSON without a global sequence.

R6.2 established:

- `GameSnapshot.nextTimelineGlobalSequence` as the per-session cursor;
- `ClocktowerGameSession.allocateTimelinePoint(...)` as the single domain allocation seam;
- restore continuity without changing game-state or player-input revisions;
- overflow and negative cursor fail-closed behavior;
- `ClocktowerNightCheckpoint` cursor persistence contract;
- missing legacy checkpoint cursor -> zero, but present malformed/negative cursor -> explicit failure;
- PR review P2 for malformed persisted cursor was fixed tests-first before merge;
- R2, Android tests/build, ASP contract tests, and real Clingo cross-validation all passed.

Production Host/Compose still does not own the allocator, by design.

## Current P1.2 gap

`RecordedEpistemicObservation` still stores independent fields:

```text
phase
round
sequence
```

`EpistemicObservationLog` still canonicalizes legacy records by:

```text
round -> sequence -> recordId
```

This remains ambiguous across multiple phases/nights.

There are currently two production observation write seams:

1. private/night information in the Clocktower Host screen;
2. public death/alive observations in the main Clocktower game runtime.

Both still construct `RecordedEpistemicObservation` from local phase/round/sequence context. Neither currently receives a `TimelinePoint` allocated by `ClocktowerGameSession`.

Therefore directly replacing the existing constructor with a required `TimelinePoint` would force production Host/runtime migration into the semantic-contract slice. That is intentionally out of scope.

## Required migration model

Do **not** add a nullable `timelinePoint` and silently fall back.

The next slice should introduce an explicit typed migration state, conceptually:

```kotlin
sealed interface ObservationTimelineBinding {
    data object LegacyLocal : ObservationTimelineBinding
    data class Global(val point: TimelinePoint) : ObservationTimelineBinding
}
```

Exact naming may change during tests-first implementation, but the semantics must remain explicit.

### LegacyLocal

- represents pre-global-timeline persisted observations;
- keeps current local `phase/round/sequence` replay context;
- may continue using the legacy canonical order only for compatibility;
- is **not** authorized as proof of multi-night timeline safety.

### Global(TimelinePoint)

- carries the authoritative global timeline identity;
- flat replay/display fields, while still present during migration, must exactly match the TimelinePoint phase/round/local sequence;
- canonical ordering must use TimelinePoint/global sequence;
- duplicate global sequence within one observation log must fail closed.

## Whole-log mode rule

A single `EpistemicObservationLog` must not mix LegacyLocal and Global records.

Reason: allowing mixed logs would require inventing an ordering relation between old local-only observations and globally identified observations. Any inference from local indices would recreate the exact P1.2 ambiguity this work is meant to remove.

Migration policy for eventual production cutover:

- an already-running legacy saved game stays LegacyLocal until that game ends or is explicitly migrated by a separately proven migration;
- a newly started globally-timed game writes Global observations from its first observation onward;
- no silent mid-game upgrade by guessing global sequence values.

## Serialization is part of the same contract

Do not land Global binding as an in-memory-only field.

The first Global-capable slice must include JSON persistence/restore at the same time, because a global identity that disappears on process restart is not a durable identity.

Required JSON behavior:

- old schema-v2 recorded-observation JSON without a timeline binding decodes explicitly as LegacyLocal;
- new Global observation JSON round-trips the complete `TimelinePoint`, including `globalSequence`;
- malformed Global timeline JSON fails closed;
- a Global TimelinePoint whose phase/round/local sequence disagrees with the observation replay fields fails closed;
- legacy encoding remains readable by the current decoder contract;
- no local sequence is ever inferred into a global sequence.

Whether this remains a compatible schema-v2 extension or requires a schema bump must be decided by tests and fixture impact before implementation. Do not change the global schema version merely to make the code easier.

## Bind-to-knowledge boundary

The same migration state must survive:

```text
RecordedEpistemicObservation
    -> EpistemicObservationLog
    -> bindTo(FormalGameState)
    -> EpistemicObservation
```

Do not discard the Global binding during `bindTo`.

However, this next slice should **not yet** decide that global sequence belongs in `PlayerWorldSetIdentity` / knowledge hash. Persistence/order correctness and knowledge-identity semantics are separate decisions.

## Tests-first exit contract for the next slice

Before implementation, add tests proving at least:

1. Global records sort by global sequence even when round/local sequence would produce the opposite order.
2. Duplicate global sequence in one Global log fails closed.
3. Mixed LegacyLocal + Global log fails closed.
4. Legacy schema-v2 JSON without timeline binding restores as LegacyLocal.
5. Global JSON round-trip preserves the exact TimelinePoint.
6. Malformed Global timeline JSON fails closed.
7. Global TimelinePoint vs replay-field mismatch fails closed.
8. `bindTo` preserves Global timeline identity into `EpistemicObservation`.
9. Existing player-world identity/hash behavior is unchanged in this slice.
10. No production Host/Compose/flow-planner change appears in the diff.

## After this slice

Once the explicit observation migration seam is green, the next P1.2 decision is knowledge identity:

```text
Which semantic timeline fields belong in recipient knowledge identity / world-set cache identity?
```

Expected direction to prove, not assume:

- phase: semantic;
- round/night number: semantic;
- global semantic timeline position: include only if receiving the same proposition at a different game-time position can change valid world constraints;
- storage IDs such as recordId/snapshotId: exclude;
- UI-only display order: exclude.

Only after observation persistence, canonical ordering, and knowledge identity are proven should production multi-night Possible Worlds consume the new timeline semantics.
