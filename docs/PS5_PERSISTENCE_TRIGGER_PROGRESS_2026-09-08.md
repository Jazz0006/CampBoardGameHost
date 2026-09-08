# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 IN PROGRESS — PS5.2a COMPLETE; PS5.2b ordinary-trigger audit is NEXT**

## 1. Campaign position

```text
PS0  Recovery product contract                         COMPLETE
PS1  Archive / Recovery separation                     COMPLETE
PS2  typed Recovery writer                             COMPLETE
PS3  typed safe Preview/Restore + atomic apply         COMPLETE
PS4  legacy persistence cleanup                        COMPLETE
PS5  persistence-trigger simplification                IN PROGRESS
  PS5.0  fresh trigger/ownership audit                 COMPLETE
  PS5.1a semantic Recovery write deduplication         COMPLETE
  PS5.1b failed-write retry correctness                COMPLETE
  PS5.1c RecoverySnapshot equality/object-graph audit  COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b ordinary trigger / SideEffect ownership audit NEXT
```

PS4 completion evidence remains frozen in:

- `docs/PS4_FINAL_CHECKPOINT_2026-09-08.md`
- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`

Do not reopen PS4 schema/content cleanup unless a correctness defect proves it necessary.

## 2. Live repository / checkpoint state

Persistence Simplification still targets:

```text
main:
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

PR #112 remains:

```text
state: open
draft: true
merged: false
base: main @ ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

Latest production-code GREEN for PS5.2a:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

The branch later advanced through temporary validation-runner cleanup only. Immediately before this documentation update the live branch head was:

```text
31287f4e1d1999dbdba866db06052230d4179034
chore: remove PS5 lifecycle GREEN runner
```

That cleanup commit has the same production/test tree as `5926138...` plus no temporary workflow files.

Validation for the PS5.2a GREEN content:

```text
focused owning GREEN + :app:testFast + git diff --check
one-shot run 34178595756  PASS

R2 on production GREEN 5926138...
run 34178562642          PASS
```

The ordinary PR CI on `5926138...` was cancelled only because the branch immediately advanced to the temporary GREEN validation commit. The one-shot runner executed the required full `:app:testFast` on the same GREEN content and passed. Cleanup commits authored by `github-actions[bot]` report `action_required` for ordinary CI/R2 because GitHub does not recursively trigger workflows from that bot push; this is not a test failure.

Full T4 remains reserved for the final PS5 acceptance checkpoint.

## 3. Frozen product / correctness boundary

PS5 changes **when/how often current Recovery is attempted or physically written**. It must not redesign the Recovery payload stabilized by PS4.

Frozen constraints:

- Recovery remains current-version-only emergency continuity;
- Recovery v2 schema/content ownership is unchanged;
- unsupported/old Recovery fails closed;
- Archive remains independent;
- the freshness window remains 4 hours;
- A4 observation/cache publication may not cross its durability boundary before persistence succeeds;
- failed physical writes must retain a future retry path;
- trigger reduction must reduce real synchronous persistence work, not merely relocate it.

## 4. PS5.0 — fresh trigger audit — COMPLETE

The live persistence path was concentrated around:

```text
persistActiveGameStateIfNeeded()
-> persistAndReleaseA4ObservationRebuildIfDurable()
```

At PS5 start the principal trigger topology was:

```text
Compose SideEffect
-> ordinary persistence attempt

ON_PAUSE / ON_STOP
-> force=true lifecycle persistence attempt
```

The A4 ordering invariant was confirmed as real:

```text
A4 observation becomes durable
-> persistence succeeds
-> durability gate releases
-> cache rebuild may publish
```

Therefore PS5 did not begin by deleting triggers.

## 5. PS5.1a / PS5.1b — RecoveryWriteGate — COMPLETE

PS5.1a established semantic duplicate suppression:

```text
d4eb000e602ef3a7c170e071292b7249c3a09c2f  RED
22086dd984fb7992d00e3226d444affb76adb509  gate introduction
dbc4dcbd6b81d27524bb6f6688f49972ef17de4d  production cutover
```

PS5.1b then pinned failed-write retry:

```text
1b22563691c2a7e6d3ba07cdc37b3c8c03a91130  RED
39229bfdddba5837a9368706946f62fd94915109  GREEN
CI 34174011104 PASS
R2 34174011121 PASS
```

Current gate contract:

- the first Recovery must physically write;
- an ordinary attempt with unchanged durable semantic content may skip the physical write;
- a real content change must physically write;
- `force=true` always attempts a physical write;
- any failed physical write sets `retryRequired`;
- ordinary duplicate suppression is disabled while retry is required;
- the next successful physical write clears retry-required state;
- `clearSavedGameState()` resets the gate;
- A4 release still consumes the persistence result and cannot bypass durability.

## 6. PS5.1c — mutable-alias safety — COMPLETE

The full reachable Recovery object-graph audit disproved the assumption that shallow `data class` equality was universally safe.

Concrete risk:

- Recovery copies major outer lists;
- `RecordedEpistemicObservation` retains nested proposition values;
- `InformationProposition.AnyOf`, `AllOf`, `NumericResult.subjectSeats`, and `BooleanResult.subjectSeats` do not universally deep-freeze incoming collections;
- a shallow remembered `RecoverySnapshot` could therefore alias live nested mutable state and miss a required write after in-place mutation.

Other requested branches were also audited: `ClocktowerNightCheckpoint`, `ActionFact`, `ActionFactTimeline`, semantic events, setup rotation bookkeeping, ghost-vote authority, highest-vote state, cards, elimination records, outcome and remaining scalar/value recovery fields. Several are intrinsically immutable/replacement based, but the complete graph is not guaranteed recursively immutable.

Tests-first lineage:

```text
5736951007f66df042cb55d5a2b4122d064cf321
  test: expose mutable recovery alias suppression

e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
  fix: freeze recovery write identity
```

The gate now remembers the timestamp-normalized **persisted Recovery representation** as an immutable content identity rather than retaining a shallow Recovery object graph.

Validation:

```text
CI 34177323891 PASS — Android FAST + CI gate
R2 34177323827 PASS
```

Important performance consequence carried into PS5.2: an ordinary attempt still constructs a Recovery snapshot and serialized durable identity even when `.commit()` is suppressed.

## 7. PS5.2a — lifecycle pause/stop duplicate physical write — COMPLETE

### 7.1 Problem proven

The old lifecycle observer treated both `ON_PAUSE` and the normally following `ON_STOP` as unconditional `force=true` writes. A normal background transition could therefore perform two synchronous physical writes of identical durable content.

The lifecycle policy was first extracted behind a typed helper without changing behavior. The App now delegates lifecycle events to `persistRecoveryForLifecycleEvent(...)`; `SideEffect` remains separate and untouched.

### 7.2 Behavior-level RED

RED test checkpoint:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
test: define lifecycle Recovery write deduplication
```

The owning test fixes three contracts:

1. successful `ON_PAUSE` followed by unchanged `ON_STOP` must result in only one physical write;
2. if the pause physical write fails, stop must still physically retry;
3. if durable content changes between pause and stop, stop must physically write the changed content.

Focused RED validation used a temporary exact-head runner:

```text
run 34178392065  PASS as RED harness
3 tests total
exactly 1 failure required:
pauseThenStopAvoidsSecondPhysicalWriteAfterSuccessfulPause
expected physical writes: 1
actual physical writes:   2
```

The retry and changed-content tests already passed against the old policy, proving the RED was specific to duplicate successful pause/stop writes rather than a broken fixture.

### 7.3 Minimal GREEN

GREEN checkpoint:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Current lifecycle policy:

```text
ON_PAUSE
-> force=true
-> freshness physical-write checkpoint

ON_STOP
-> force=false ordinary attempt
-> unchanged content after successful pause: suppressed
-> pause failure / retryRequired: physically retried
-> changed durable content since pause: physically written
```

This deliberately does **not** delete either lifecycle event. It changes ownership semantics so `ON_STOP` remains a last follow-up opportunity without automatically paying for a second identical `.commit()`.

A4 ordering remains unchanged because both lifecycle paths still call the same `persistAndReleaseA4ObservationRebuildIfDurable(force=...)` boundary.

Validation:

```text
run 34178595756
  exact slice changed-file allowlist PASS
  git diff --check                    PASS
  focused RecoveryLifecyclePersistenceTest PASS
  :app:testFast                       PASS

R2 34178562642                        PASS
```

Exact final diff from the pre-PS5.2a docs head `e2ed598...` through cleanup head `31287f4...` contains only:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

The App change is only the behavior-preserving delegation from inline lifecycle branching to the typed policy helper. No SideEffect, Recovery schema, A4 semantics or domain state was removed.

## 8. Current live trigger architecture

After PS5.2a:

```text
Compose SideEffect
-> persistAndReleaseA4ObservationRebuildIfDurable(force = false)
-> RecoveryWriteGate

ON_PAUSE
-> persistAndReleaseA4ObservationRebuildIfDurable(force = true)
-> RecoveryWriteGate

ON_STOP
-> persistAndReleaseA4ObservationRebuildIfDurable(force = false)
-> RecoveryWriteGate
```

Consequences:

- normal recomposition ordinary attempts still build a snapshot/content identity but duplicate physical commits are suppressed;
- pause remains the explicit lifecycle freshness checkpoint;
- stop no longer guarantees a duplicate commit, yet still covers retryRequired and real durable changes;
- `SideEffect` remains the broad ordinary-attempt safety net;
- A4 durability ordering remains unchanged.

## 9. PS5.2b — ordinary trigger / SideEffect ownership audit — NEXT

Do **not** delete `SideEffect` yet.

The next audit must answer from the live code, not from a desired architecture:

1. enumerate every durable mutation that can change `activeGameRecoverySnapshot()` and identify whether it already crosses an explicit persistence/transaction boundary;
2. distinguish durable mutations from transient Compose/UI changes that merely cause recomposition;
3. determine whether a small durable dirty/revision marker can cover all durable mutations without sprinkling persistence calls across UI code;
4. prove how a failed ordinary physical write obtains another future retry if no further game mutation occurs;
5. preserve A4's synchronous persistence-before-release contract as a special hard durability boundary;
6. count both physical `.commit()` calls **and** avoidable snapshot/serialization identity construction;
7. compare the complexity/risk of explicit durable ownership with simply retaining cheap semantic `SideEffect` attempts.

A valid outcome of PS5.2b may be **retaining `SideEffect`** if removing it requires broad, fragile mutation instrumentation for little measured gain. Trigger reduction is not an end in itself.

Likely investigation order:

```text
A. map activeGameRecoverySnapshot durable inputs -> mutation owners
B. map existing explicit persist calls / A4 boundaries
C. identify uncovered mutations currently relying only on recomposition
D. design behavior tests for any proposed dirty/revision owner
E. only then decide whether SideEffect can be removed or narrowed
```

No PS5.2b production change is authorized by this document merely because the audit has started.

## 10. Validation route

For each PS5 behavioral slice:

1. behavior-level RED when correctness/behavior changes;
2. focused owning tests first;
3. `:app:testFast` at logical GREEN;
4. R2;
5. `git diff --check`;
6. exact changed-file/reference audit;
7. remote-head race check before/after writes.

Final PS5 acceptance only:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle tests;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit;
- real-device process-loss/restart acceptance before the overall Persistence Simplification campaign is release-ready.

## 11. Explicit non-goals

PS5 does not include:

- Recovery schema/content redesign;
- cross-version migration support;
- Archive redesign;
- Werewolf whole-module removal;
- D6/App large-file decomposition;
- A4/ZDD production rollout;
- unrelated Host/UI work;
- DataStore migration merely for modernization.

## 12. Next conversation start point

1. Read root `AGENTS.md`, `docs/TESTING_STRATEGY.md`, `docs/CURRENT_DEVELOPMENT_ROADMAP.md` and this file.
2. Re-query live `main`, branch, PR #112 and current checks.
3. Distinguish production GREEN `5926138...` from later validation/docs-only commits.
4. Continue **PS5.2b ordinary-trigger / SideEffect ownership audit**.
5. Do not delete `SideEffect` until durable-mutation coverage, failure retry and A4 ordering are proven.
6. Do not merge PR #112 without explicit authorization.
7. Do not start Werewolf removal, D6, A4/ZDD or unrelated UI work.
