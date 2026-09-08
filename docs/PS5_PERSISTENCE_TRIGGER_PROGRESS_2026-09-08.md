# PS5 Persistence Trigger Simplification — Progress / Handoff

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS5 IN PROGRESS — PS5.1 write gate implemented; semantic-equality safety audit is NEXT**

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
  PS5.1c RecoverySnapshot equality/object-graph audit  NEXT / IN PROGRESS
  PS5.2 further trigger reduction                      NOT STARTED
```

PS4 completion evidence remains frozen in:

- `docs/PS4_FINAL_CHECKPOINT_2026-09-08.md`
- `docs/PS4_CLEANUP_PROGRESS_2026-09-07.md`

Do not reopen PS4 schema/content cleanup as part of PS5 unless a correctness defect proves it necessary.

## 2. Live repository state at this checkpoint

Before this documentation update, the live branch head was:

```text
39229bfdddba5837a9368706946f62fd94915109
fix: retry recovery write after forced failure
```

PR #112 was confirmed:

```text
state: open
draft: true
merged: false
base: main @ ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

Validation on `39229bf...`:

```text
CI run 34174011104
Android FAST unit tests  PASS
CI gate                 PASS

R2 run 34174011121      PASS
```

This is a FAST checkpoint, not the final PS5 full-CI acceptance checkpoint.

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
- real snapshot-content changes must physically write;
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

The temporary trigger-audit/cutover workflow was removed after validation; no PS5 temporary workflow should be assumed to remain in the tree.

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

`RecoveryWriteGate` now tracks `retryRequired`:

- any failed physical write sets `retryRequired = true`;
- ordinary duplicate suppression is disabled while retry is required;
- the next successful physical write clears the retry requirement;
- clearing saved state also clears the retry requirement.

Validation on the GREEN head:

```text
CI 34174011104  PASS (FAST)
R2 34174011121  PASS
```

## 7. Current safety question — PS5.1c

Before using semantic equality as the basis for more aggressive trigger removal, audit the complete object graph reachable from `RecoverySnapshot`.

The risk to exclude is mutable aliasing:

```text
lastDurableContent and new snapshot share a mutable nested object
-> nested object is modified in place
-> both snapshots now observe the same mutation
-> structural equality may incorrectly report "unchanged"
-> a required physical write could be skipped
```

The next implementation conversation must therefore prove that each durable nested component used by `RecoverySnapshot` has safe value semantics / immutable replacement semantics, or change the gate identity strategy if that proof does not hold.

Audit at minimum:

- Clocktower night checkpoint state;
- action timeline / action facts;
- epistemic observations;
- semantic event/history lists;
- Trouble Brewing setup-rotation record;
- ghost-vote / highest-vote durable authority;
- committed cards/records/outcome and other nested recovery value objects.

Do not infer safety merely because the top-level `RecoverySnapshot` is a data class.

If a mutable-alias risk is found:

1. establish a behavioral RED that reproduces a missed physical write;
2. fix the gate identity/snapshotting strategy narrowly;
3. rerun focused tests and `:app:testFast`;
4. do not proceed to PS5.2 until green.

If the entire object graph is proven safe, record that evidence in this document and then proceed to PS5.2 planning.

## 8. PS5.2 — not yet authorized by the current checkpoint

Only after PS5.1c is complete should the remaining trigger topology be reconsidered.

Questions for the next audit:

1. Is `SideEffect` still the right ordinary trigger once semantic deduplication exists, or should durable transactions explicitly mark/write Recovery?
2. Are both `ON_PAUSE` and `ON_STOP` required, or is one a redundant last-chance write?
3. Can any explicit business-event persistence trigger replace recomposition-driven attempts without missing durable transitions?
4. How should a failed physical write remain retryable without requiring UI recomposition?
5. Does any proposed trigger reduction preserve A4 persistence-before-release ordering?

Do **not** delete `SideEffect`, `ON_PAUSE`, or `ON_STOP` simply to reduce call count before these questions are answered with tests/audit evidence.

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
6. confirm the expected code checkpoint is at or after `39229bfdddba5837a9368706946f62fd94915109` and distinguish later docs-only commits;
7. continue **PS5.1c RecoverySnapshot value-semantics / mutable-alias audit** first;
8. if the audit is clean, design PS5.2 from the live trigger topology; if not, fix the gate tests-first;
9. do not merge PR #112 without explicit authorization;
10. do not start Werewolf removal, D6, A4/ZDD or unrelated UI work.
