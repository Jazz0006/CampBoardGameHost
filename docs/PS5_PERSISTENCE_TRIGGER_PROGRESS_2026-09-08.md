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

Latest validated production-code GREEN for PS5.2a:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Validation:

```text
one-shot 34178595756
  focused owning tests PASS
  :app:testFast       PASS
  git diff --check    PASS

R2 34178562642        PASS
```

Later commits contain validation-runner cleanup and documentation only. Treat `5926138...` as the production checkpoint and re-query live branch state before PS5.2b implementation.

The ordinary PR CI on `5926138...` was cancelled only because the branch immediately advanced to the temporary GREEN validation commit. The one-shot runner executed the required full `:app:testFast` on the same GREEN content and passed. Cleanup commits authored by `github-actions[bot]` may show `action_required` because GitHub does not recursively trigger workflows from that bot push; this is not a test failure.

Full T4 remains reserved for final PS5 acceptance.

## 3. Frozen product / correctness boundary

PS5 changes **when/how often current Recovery is attempted or physically written**. It must not redesign the Recovery payload stabilized by PS4.

Frozen constraints:

- Recovery remains current-version-only emergency continuity;
- Recovery v2 schema/content ownership is unchanged;
- unsupported/old Recovery fails closed;
- Archive remains independent;
- freshness remains 4 hours;
- A4 observation/cache publication may not cross its durability boundary before persistence succeeds;
- failed physical writes must retain a future retry path;
- trigger reduction must reduce real synchronous persistence work, not merely relocate it.

## 4. PS5.0 — fresh trigger audit — COMPLETE

The important persistence path is concentrated around:

```text
persistActiveGameStateIfNeeded()
-> persistAndReleaseA4ObservationRebuildIfDurable()
```

At PS5 start:

```text
Compose SideEffect
-> ordinary persistence attempt

ON_PAUSE / ON_STOP
-> force=true lifecycle persistence attempt
```

The A4 invariant is:

```text
A4 observation becomes durable
-> persistence succeeds
-> durability gate releases
-> cache rebuild may publish
```

PS5 therefore did not start by deleting triggers.

## 5. PS5.1a / PS5.1b — RecoveryWriteGate — COMPLETE

PS5.1a semantic duplicate suppression:

```text
d4eb000e602ef3a7c170e071292b7249c3a09c2f  RED
22086dd984fb7992d00e3226d444affb76adb509  gate introduction
dbc4dcbd6b81d27524bb6f6688f49972ef17de4d  cutover
```

PS5.1b failed-write retry:

```text
1b22563691c2a7e6d3ba07cdc37b3c8c03a91130  RED
39229bfdddba5837a9368706946f62fd94915109  GREEN
CI 34174011104 PASS
R2 34174011121 PASS
```

Current gate contract:

- first Recovery physically writes;
- unchanged ordinary attempt may suppress physical write;
- real durable change writes;
- `force=true` writes;
- failed write sets `retryRequired`;
- retry-required disables duplicate suppression until success;
- clear/reset clears the gate;
- A4 release remains persistence-gated.

## 6. PS5.1c — mutable-alias safety — COMPLETE

Audit found a real nested alias hazard: Recovery freezes major outer lists, but nested epistemic proposition collections are not universally deep-frozen. A shallow remembered Recovery graph was therefore unsafe for equality.

```text
5736951007f66df042cb55d5a2b4122d064cf321  RED
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23  GREEN
CI 34177323891 PASS
R2 34177323827 PASS
```

The gate now stores timestamp-normalized persisted Recovery representation as immutable content identity rather than retaining a shallow `RecoverySnapshot` graph.

The requested graph audit also covered night checkpoint, action facts/timeline, semantic events, setup rotation, ghost vote/highest vote, cards, eliminations, outcome and other nested Recovery values.

Carry-forward: an ordinary suppressed attempt still constructs a Recovery snapshot and serialized identity.

## 7. PS5.2a — lifecycle pause/stop duplicate physical write — COMPLETE

Old behavior:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=true
```

A normal pause→stop transition could write identical content twice.

A typed lifecycle policy helper was first extracted without behavior change. The owning tests then fixed three contracts:

1. successful pause + unchanged stop => one physical write;
2. failed pause write => stop retries;
3. changed durable content between pause and stop => stop writes.

RED:

```text
192f031b67c9b4eb46bada4928425f9de332bb4a
test: define lifecycle Recovery write deduplication
```

Focused RED harness:

```text
34178392065 PASS as RED harness
3 tests total
exactly 1 expected failure:
pauseThenStopAvoidsSecondPhysicalWriteAfterSuccessfulPause
expected 1 physical write; actual 2
```

GREEN:

```text
5926138d0557835f281ba15b4051f50fa3ae741e
fix: dedupe successful pause-stop Recovery writes
```

Current policy:

```text
ON_PAUSE -> force=true
ON_STOP  -> force=false
```

Therefore:

- successful pause refreshes durable freshness;
- unchanged stop is deduplicated;
- failed pause leaves `retryRequired`, so stop physically retries;
- changed content after pause still writes at stop.

A4 ordering is unchanged because lifecycle calls still go through `persistAndReleaseA4ObservationRebuildIfDurable(force=...)`.

GREEN validation:

```text
34178595756
  exact allowlist PASS
  git diff --check PASS
  focused owning tests PASS
  :app:testFast PASS
R2 34178562642 PASS
```

Exact PS5.2a production/test net diff from `e2ed598...` through validation cleanup contains only:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistence.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryLifecyclePersistenceTest.kt
```

The App change is only delegation to the typed lifecycle helper. No SideEffect, Recovery schema, A4 behavior or game domain state was removed.

## 8. Current live trigger architecture

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

`SideEffect` remains the broad ordinary-attempt safety net.

## 9. PS5.2b — SideEffect ownership audit — NEXT

Do **not** delete `SideEffect` yet.

Audit requirements:

1. enumerate every input to `activeGameRecoverySnapshot()`;
2. trace every production mutation owner for those inputs;
3. map existing explicit persistence/A4 boundaries;
4. identify durable mutations currently relying only on later recomposition/SideEffect;
5. distinguish durable changes from transient UI recomposition;
6. determine whether a central dirty/revision marker can cover all durable changes without scattered save calls;
7. prove future retry when `retryRequired` is true but no further durable mutation occurs;
8. preserve A4 synchronous persistence-before-release;
9. measure physical `.commit()` count and avoidable snapshot/serialization work.

Valid outcomes include retaining `SideEffect` if removing it would require broad fragile instrumentation for little gain.

Investigation order:

```text
A. activeGameRecoverySnapshot inputs -> mutation owners
B. existing explicit persist / A4 boundaries
C. uncovered mutations relying on SideEffect
D. tests for any proposed dirty/revision owner
E. then decide remove / narrow / retain SideEffect
```

## 10. Validation route

For each behavioral slice:

1. behavior-level RED;
2. focused owning tests;
3. `:app:testFast` at GREEN;
4. R2;
5. `git diff --check`;
6. exact changed-file/reference audit;
7. remote-head race check.

Final PS5 acceptance only:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit;
- real-device process-loss/restart acceptance.

## 11. Explicit non-goals

PS5 does not include Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, D6 decomposition, A4/ZDD rollout, unrelated Host/UI work, or DataStore migration merely for modernization.

## 12. Next conversation start point

1. Read `AGENTS.md`, `docs/TESTING_STRATEGY.md`, `docs/CURRENT_DEVELOPMENT_ROADMAP.md` and this file.
2. Re-query live `main`, branch, PR #112 and checks.
3. Distinguish production GREEN `5926138...` from later validation/docs-only commits.
4. Continue PS5.2b ordinary-trigger / SideEffect ownership audit.
5. Do not delete SideEffect until durable-mutation coverage, retry and A4 ordering are proven.
6. Do not merge PR #112 without explicit authorization.
