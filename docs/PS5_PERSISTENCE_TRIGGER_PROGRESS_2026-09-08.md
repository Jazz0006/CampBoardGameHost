# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 IN PROGRESS — PS5.1 COMPLETE; PS5.2 planning is NEXT**

## 1. Campaign position

```text
PS0  Recovery product contract                         COMPLETE
PS1  Archive / Recovery separation                     COMPLETE
PS2  typed Recovery writer                             COMPLETE
PS3  typed safe Preview/Restore + atomic apply         COMPLETE
PS4  legacy persistence cleanup                        COMPLETE
PS5  persistence-trigger simplification                IN PROGRESS
  PS5.0 fresh trigger/ownership audit                  COMPLETE
  PS5.1a semantic Recovery write deduplication         COMPLETE
  PS5.1b failed-write retry correctness                COMPLETE
  PS5.1c RecoverySnapshot equality/object-graph audit  COMPLETE
  PS5.2 further trigger reduction                      NEXT / PLANNING
```

PS4 completion evidence remains frozen in:

- `docs/PS4_FINAL_CHECKPOINT_2026-09-08.md`
- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`

Do not reopen PS4 schema/content cleanup as part of PS5 unless a correctness defect proves it necessary.

## 2. Live repository state at the PS5.1c GREEN checkpoint

The branch was re-queried before PS5.1c implementation. At that time:

```text
main:
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e

branch / PR head before PS5.1c code:
2c56bb026521bd4f7946e77f5dd12651c8023f94
```

The head after `39229bf...` and before PS5.1c contained documentation-only commits; no unreviewed production change had appeared.

PR #112 was confirmed:

```text
state: open
draft: true
merged: false
base: main @ ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

Latest PS5.1c production GREEN checkpoint:

```text
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
fix: freeze recovery write identity
```

Validation on `e2dbd1d...`:

```text
CI run 34177323891
Android FAST unit tests  PASS
CI gate                 PASS
full Android/T4         intentionally not selected

R2 run 34177323827      PASS
```

This remains a FAST logical checkpoint. Full T4 is reserved for final PS5 acceptance.

## 3. PS5 product/architecture boundary

PS5 changes **when/how often current Recovery is physically written**. It must not redesign the Recovery payload that PS4 already stabilized.

Frozen constraints:

- Recovery remains current-version-only emergency continuity;
- Recovery v2 schema/content ownership is unchanged;
- unsupported/old Recovery still fails closed;
- Archive remains independent;
- the 4-hour freshness contract remains;
- A4 observation/cache publication may not cross its durability boundary before persistence is successful;
- lifecycle last-chance persistence must remain safe until a later audit proves a trigger is redundant.

Do not combine trigger cleanup with another Recovery schema migration.

## 4. PS5.0 — fresh trigger audit

The fresh branch audit found that the current app is not dominated by many independent persistence writers. The important path is concentrated around `persistActiveGameStateIfNeeded()` / `persistAndReleaseA4ObservationRebuildIfDurable()`.

The two principal trigger classes are:

```text
Compose SideEffect
-> ordinary persistence attempt

ON_PAUSE / ON_STOP
-> force=true lifecycle persistence attempt
```

The main inefficiency was that `SideEffect` can run repeatedly during recomposition while the actual durable game content has not changed, causing repeated synchronous SharedPreferences writes.

The audit also confirmed a real correctness invariant:

```text
A4 durable observation
-> persistence succeeds
-> durability gate releases
-> cache rebuild may be published
```

Therefore the first PS5 change was deliberately **not** “delete SideEffect”. It was to make duplicate ordinary attempts cheap while preserving the existing durability boundary.

## 5. PS5.1a — semantic Recovery write gate

Tests-first RED checkpoint:

```text
d4eb000e602ef3a7c170e071292b7249c3a09c2f
test: define semantic Recovery write deduplication
```

Production introduction:

```text
22086dd984fb7992d00e3226d444affb76adb509
feat: add Recovery semantic write gate

dbc4dcbd6b81d27524bb6f6688f49972ef17de4d
refactor: dedupe semantic Recovery writes
```

Current behavior:

- first snapshot must physically write;
- ordinary attempts with equal game/recovery content but a different `savedAtMillis` reuse the last successful durable result and do not physically write again;
- real durable snapshot-content changes must physically write;
- failed writes are never treated as newly durable;
- `clearSavedGameState()` clears the write gate;
- lifecycle persistence uses `force=true` and therefore still performs a physical write / freshness refresh;
- the A4 release path continues to consume the persistence result rather than bypassing durability.

Current production wiring remains:

```text
SideEffect
-> persistAndReleaseA4ObservationRebuildIfDurable(force = false)
-> RecoveryWriteGate

ON_PAUSE / ON_STOP
-> persistAndReleaseA4ObservationRebuildIfDurable(force = true)
-> RecoveryWriteGate
```

No trigger was removed during PS5.1.

## 6. PS5.1b — failed forced-write retry correctness

A safety review found an edge case in the first gate design:

```text
A successfully durable
-> forced lifecycle rewrite of A fails
-> next ordinary A attempt
```

The ordinary attempt must **not** be deduplicated merely because A matched the last previously successful content. A failed physical write means another physical attempt is required before duplicate suppression may resume.

RED checkpoint:

```text
1b22563691c2a7e6d3ba07cdc37b3c8c03a91130
test: pin recovery retry after forced failure
CI run 34173747359
Android FAST unit tests: expected FAIL
```

GREEN checkpoint:

```text
39229bfdddba5837a9368706946f62fd94915109
fix: retry recovery write after forced failure
```

`RecoveryWriteGate` tracks `retryRequired`:

- any failed physical write sets `retryRequired = true`;
- ordinary duplicate suppression is disabled while retry is required;
- the next successful physical write clears the retry requirement;
- clearing saved state also clears the retry requirement.

Validation on that GREEN head:

```text
CI 34174011104  PASS (FAST)
R2 34174011121  PASS
```

## 7. PS5.1c — RecoverySnapshot mutable-alias safety audit — COMPLETE

### 7.1 Risk found

The audit disproved the assumption that the complete reachable Recovery object graph can safely be retained as the equality baseline merely because the top-level models use Kotlin `data class` / `val` fields.

The original gate normalized only the timestamp:

```text
lastDurableContent = snapshot.copy(savedAtMillis = 0)
```

That copy was shallow. A nested mutable object supplied by a caller could therefore remain shared between live state and the gate's remembered baseline.

A concrete durable reachable hole exists in epistemic observations:

- `activeGameRecoverySnapshot()` copies the **outer** `clocktowerEpistemicObservations` list;
- `RecordedEpistemicObservation` directly retains `recipientSeats` and `proposition`;
- `InformationProposition.AnyOf`, `AllOf`, `NumericResult.subjectSeats`, and `BooleanResult.subjectSeats` directly retain incoming collection references rather than taking defensive immutable copies;
- therefore a caller-owned mutable nested collection can be shared by the new snapshot and the gate's previous shallow baseline.

That is sufficient for this failure mode:

```text
first write succeeds
-> gate remembers shallow Recovery object graph
-> a nested durable collection mutates in place
-> remembered baseline sees the same mutation
-> new structural equality says “unchanged”
-> required physical write is incorrectly suppressed
```

### 7.2 Object-graph audit evidence

The rest of the specifically requested graph was checked rather than assumed safe:

- `ClocktowerNightCheckpoint` consists of scalar/string/enum checkpoint values and emits a fresh persisted-value map; it is also transiently constructed by the Recovery codec rather than retained as the gate baseline.
- `ActionFact` variants contain scalar/value identifiers only.
- `ActionFactTimeline` defensively snapshots `entries.toList()`, wraps it with `Collections.unmodifiableList`, and appends by returning a new timeline.
- semantic `ClocktowerEvent` elements are value objects; Recovery copies the outer event list.
- `TroubleBrewingSetupRotationRecord` contains Sets; the type itself does not enforce defensive copying, although the production factory creates `toSet()` snapshots. This is another reason the gate must not depend on universal deep immutability.
- `ClocktowerGhostVoteAuthority.confirmVote()` returns a copied authority with a replacement set; app wiring replaces the authority state rather than mutating the retained object in place.
- highest-vote name/count are scalar state replacements.
- `PlayerCard`, `EliminationRecord`, and `GameOutcome` are value objects; `ClocktowerRole` is scalar/string/enum data. Recovery copies the outer card/record lists.
- `ClocktowerRecoveryIdentity`, position, scalar mechanics, revisions, sequence counters and outcome fields are value-semantic scalar/enums/strings.
- `activeGameRecoverySnapshot()` freezes the major outer lists (`cards`, `records`, bluff/slayer/artist name lists, events, observations), but this is not a recursive deep freeze and therefore was not accepted as sufficient protection.

Conclusion: **the reachable graph is not universally alias-safe, so the gate itself must hold an identity that cannot share mutable nested objects with live state.**

### 7.3 Tests-first RED

Behavior-level RED checkpoint:

```text
5736951007f66df042cb55d5a2b4122d064cf321
test: expose mutable recovery alias suppression
```

The owning `RecoveryWriteGateTest` constructs a snapshot backed by a mutable durable nested list, successfully persists it, mutates the same list in place, then attempts persistence again. Correct behavior requires two physical writes.

Against the pre-fix shallow-baseline implementation, the old remembered snapshot and new snapshot share that list, so the second equality check incorrectly suppresses the write. The RED commit's automatically started CI was superseded/cancelled when the GREEN commit advanced the same PR branch; the failing behavior is nevertheless deterministic from the test and the pre-fix implementation. The RED commit remains separate in history as tests-first evidence.

### 7.4 Minimal GREEN

GREEN checkpoint:

```text
e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23
fix: freeze recovery write identity
```

The gate no longer stores a `RecoverySnapshot` object graph as `lastDurableContent`. It computes the exact persisted Recovery representation with `savedAtMillis` normalized to zero and stores its resulting immutable `String` as `lastDurableContentIdentity`.

Consequences:

- the remembered baseline cannot alias any caller/live mutable object;
- a later in-place nested mutation produces a new serialized durable identity and therefore requires a physical write;
- timestamp-only changes remain suppressible;
- `force=true`, `retryRequired`, clear/reset, and A4 persistence-result ordering are unchanged;
- semantic comparison now follows the actual Recovery payload rather than non-persisted UI/object identity.

Trade-off to carry into PS5.2: ordinary save attempts now build the serialized durable identity before deciding whether `.commit()` is necessary. This still suppresses synchronous physical writes, but `SideEffect` may cause avoidable snapshot/JSON work even when the physical write is skipped. PS5.2 should reduce unnecessary **attempts/identity construction** as well as count actual synchronous `.commit()` calls; do not mistake moving work for eliminating it.

Validation:

```text
CI 34177323891  PASS
  Android FAST unit tests  PASS
  CI gate                 PASS
R2 34177323827            PASS
```

PS5.1c is therefore COMPLETE.

## 8. PS5.2 — further trigger reduction — NEXT / PLANNING

PS5.2 must start from the still-live topology, not from a predetermined trigger-deletion target:

```text
Compose SideEffect
-> ordinary persistence attempt
-> RecoveryWriteGate

ON_PAUSE / ON_STOP
-> force=true lifecycle persistence
-> RecoveryWriteGate
```

Planning questions and current direction:

1. **`SideEffect`**: retaining it is currently the safest generic retry/change detector, but it also rebuilds snapshots/serialized identities on recomposition. Prefer moving toward explicit durable-change ownership or a durable dirty/revision marker only after every durable mutation path is proven covered.
2. **Durable transactions**: evaluate a small transaction/dirty boundary that marks Recovery dirty when durable game facts commit, rather than making Compose recomposition the primary semantic trigger. Do not introduce this until coverage of all durable mutation owners is auditable.
3. **`ON_PAUSE` + `ON_STOP`**: do not mechanically delete either. Audit whether the successful `ON_PAUSE` write makes an unconditional successful `ON_STOP` force write redundant. A promising shape is to preserve `ON_STOP` as a retry fallback only when the preceding lifecycle write failed, if tests prove the lifecycle contract.
4. **Failed writes**: any reduced trigger topology must guarantee a future physical attempt while `retryRequired` is true, even if there is no further recomposition or game mutation.
5. **A4**: `persistAndReleaseA4ObservationRebuildIfDurable()` remains a hard ordering boundary; any dirty/batched model must still synchronously establish durability before releasing the A4 rebuild.
6. **Measure the right thing**: acceptance should count physical write callbacks / synchronous `.commit()` invocations and also watch snapshot/serialization attempt frequency. Reducing function-call count without reducing physical commits or main-thread work is not success.

Do **not** delete `SideEffect`, `ON_PAUSE`, or `ON_STOP` until the above is proven with owning behavior tests and exact topology audit.

## 9. Validation route

For PS5 behavioral slices:

1. behavioral RED when the contract is changing or a correctness bug is being fixed;
2. focused `RecoveryWriteGate` / persistence tests;
3. `:app:testFast` at each logical GREEN checkpoint;
4. R2 structural/main-thread boundary gate;
5. `git diff --check` and exact changed-file/reference audit;
6. re-query remote head before/after writes.

At the final PS5 acceptance checkpoint, run the full persistence T4 gate, including:

- Android `testFull`;
- `:app:assembleDebug`;
- ASP contract/oracle tests;
- real Clingo cross-validation;
- R2;
- exact production-path/static audit;
- real-device process-loss/restart acceptance before calling the whole Persistence Simplification campaign release-ready.

Do not use full CI after every tiny PS5 substep; reserve it for a logical campaign checkpoint unless a broader invariant specifically requires it.

## 10. Explicit non-goals

PS5 does not include:

- Recovery schema/content redesign;
- cross-version migration support;
- Archive redesign;
- Werewolf whole-module removal;
- D6/App large-file decomposition;
- A4/ZDD production rollout;
- unrelated Host/UI work;
- DataStore migration merely for modernization.

## 11. Next conversation start point

The next conversation should:

1. read root `AGENTS.md`;
2. read `docs/TESTING_STRATEGY.md`;
3. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. read this file;
5. re-query live `main`, PR #112, branch head and current checks;
6. distinguish the PS5.1c production GREEN `e2dbd1db05812ecbd0c0b2e751dd42b9fdc3cd23` from any later docs-only commits;
7. continue **PS5.2 trigger-topology planning/audit** from the still-live `SideEffect` + `ON_PAUSE` + `ON_STOP` architecture;
8. do not remove a trigger until retry coverage, A4 ordering and actual `.commit()` reduction are proven;
9. do not merge PR #112 without explicit authorization;
10. do not start Werewolf removal, D6, A4/ZDD or unrelated UI work.
