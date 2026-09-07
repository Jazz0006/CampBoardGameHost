# PS4 Persistence Cleanup — Progress Checkpoint

> Date: 2026-09-08 Australia/Sydney  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Status: **PS4.6 final validation in progress; PS5 not started**

## 1. Campaign position

```text
PS0 contract freeze                    COMPLETE
PS1 Archive / Recovery separation      COMPLETE
PS2 typed Recovery writer              COMPLETE
PS3 typed safe Preview/Restore         COMPLETE
PS4 legacy cleanup                     IN PROGRESS
  PS4.1 dead active snapshot           COMPLETE
  PS4.2 Recovery token ownership       COMPLETE
  PS4.3 Recovery wire cleanup          COMPLETE
  PS4.4 ActiveGame identity cleanup    COMPLETE
  PS4.5 persistence/setup hygiene      COMPLETE
  PS4.6 final audit / full validation  IN PROGRESS
PS5 trigger simplification             NOT STARTED
```

Live base remains:

```text
main = ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

PR #112 remains open, draft and unmerged.

## 2. Current persistence product contract

Persistence is intentionally **current-version continuity infrastructure**, not a cross-version save-file product.

The supported contract is:

1. preserve committed game facts and mandatory continuation across short process-loss interruptions;
2. current active Recovery is valid only for the current Recovery format and compatibility token;
3. unsupported/old Recovery fails closed as `UnsupportedFormat`;
4. Archive stores current-version review history, but old Archive wire layouts are not promised to remain readable;
5. no migration framework, old-schema decoder, compatibility fallback, or long-lived persistence metadata should be retained solely to read older app versions;
6. old persisted data may be discarded after an app/schema version change.

A version field and explicit `UnsupportedFormat` rejection are safety boundaries, not compatibility layers, and remain required.

## 3. PS4.4 — ActiveGame identity ownership retired

PS4.4 moved remaining real save-time invariants to current domain validators and retired the obsolete ActiveGame identity/coordinator schema.

Final production retirement checkpoint:

```text
7ba98aa78285daa29e2380d0c6b0f933d8e37133
refactor: retire legacy active-game identity schema
```

Deleted production ownership includes:

```text
ActiveGamePersistenceCoordinator
ActiveGameIdentityEnvelope
PersistedGameIdentity
ClocktowerPersistenceIdentityFactory
WerewolfPersistenceIdentityFactory
```

Current save validation remains owned by:

```text
ClocktowerActiveSessionValidator
WerewolfActiveGameSaveValidator
BuiltInClocktowerRulesetCatalog
```

Formal checkpoint:

```text
docs/PS4_4_ACTIVE_IDENTITY_RETIREMENT_CHECKPOINT_2026-09-07.md
```

## 4. PS4.5 — persistence/setup/ruleset hygiene complete

Removed as dead legacy persistence infrastructure:

```text
CommittedClocktowerSetupPersistence.kt
CommittedClocktowerSetupPersistenceTest.kt
ClocktowerRulesetPersistenceBasisJsonCodec
resolveForRestore
resolveLegacyBasisForRestore
```

Retained deliberately because they still represent current behavior or high-value durable-state boundaries:

- `ClocktowerRulesetPersistenceBasis` and `TroubleBrewingRulesetPersistence.refFor()`;
- `CommittedClocktowerSetup` domain model and `TroubleBrewingCommittedSetupAdapter`, whose construction path still validates setup consistency;
- `ClocktowerNightCheckpoint` core state;
- `ClocktowerNightCheckpoint.persistedValues()` current writer mapping;
- the map restore seam used by high-value night transaction/SNE regression tests.

Validation checkpoint:

```text
PS4.5a persistence hygiene validation
run 34121447570
PASS
```

## 5. PS4.6 — current-only Archive cleanup

A fresh compatibility audit found two remaining Archive fallbacks that existed only for older wire layouts:

```text
missing "archive" payload -> decode legacy "snapshot"
missing "id"              -> use archivedAtMillis as archive id
```

The current producer always writes both explicit `id` and `archivedAtMillis`, so neither fallback is needed for current-version behavior.

### 5.1 Legacy snapshot reader — tests first

RED:

```text
f50c3df06f00b860205c1c388bc56d7c66fbb64d
test: reject legacy snapshot archives
CI run 34169274065
1169 tests completed, exactly 1 failed:
GameArchiveLegacyRejectionTest > legacy snapshot archive is rejected
```

GREEN:

```text
4d6bb2bc2f4c2f278ca11400430439140887c670
refactor: drop legacy archive snapshot reader

8ce4472562211cd5e3f6d4489b6123e3c2f78144
test: retire legacy archive readability contract
CI run 34169457561
:app:testFast PASS
CI gate PASS
R2 PASS
```

`GameArchiveJsonCodec.decodeEntry()` now requires the current `archive` payload; no `decodeLegacyEntry()` remains.

### 5.2 Archive id fallback — independent tests-first cut

RED:

```text
9928fac0c66c9fa8a3ec808c094c85e9ec85e377
test: reject archive identity fallback
CI run 34169634414
1169 tests completed, exactly 1 failed:
GameArchiveLegacyRejectionTest > current archive without explicit id is rejected
```

GREEN:

```text
7f4cd7ff202284c270c73b9c57f7cfc77a6611d6
refactor: drop archive identity fallback
CI run 34169741287
:app:testFast PASS
CI gate PASS
R2 PASS
```

Current Archive decoding now rejects a missing explicit `id`; `archivedAtMillis` is no longer an identity compatibility fallback.

## 6. PS4.6 static architecture audit

The final static audit was updated from the obsolete requirement “Archive legacy compatibility remains” to the current contract “legacy Archive reader/fallback must stay absent”.

Checkpoint:

```text
5ca1f8143ab17bb512107f96bf37c9080de066a4
ci: audit current-only persistence contract
PS4.6 final architecture audit run 34169917902
PASS
```

The audit proves:

- retired ActiveGame/legacy persistence owners remain absent;
- Recovery v2 wire contains no retired identity/setup/ruleset authorities;
- `RecoverySnapshotJsonCodec.decodeStrict` has one production owner: `RecoveryRestorePlanner`;
- Preview and Restore share `prepareCurrentRecoveryPlan`;
- Recovery apply remains owned by `RecoveryApplicationCoordinator`;
- Archive remains independent from Recovery;
- Archive production codec contains no `decodeLegacyEntry`, old `snapshot` reader, `archiveId()` helper, or `id -> archivedAtMillis` fallback;
- explicit current Archive rejection tests exist;
- durable Clocktower facts remain;
- `git diff --check` passes.

## 7. Current architecture boundary

```text
Current game state
-> typed RecoverySnapshot v2
-> strict current-format encode/decode
-> 4-hour validity + compatibility token
-> ValidatedRecoveryPlan
-> atomic RecoveryApplicationCoordinator apply

Completed game
-> GameArchiveRecord
-> current GameArchiveJsonCodec v1
-> current-version review history
```

No cross-version migration bridge is part of either path.

Durable Clocktower facts that must remain include:

- Trouble Brewing setup completion / rotation bookkeeping;
- semantic history;
- action timeline;
- epistemic observations;
- current ruleset hash basis/ref;
- `ClocktowerNightCheckpoint` writer/state.

## 8. Remaining PS4.6 work

Before PS4 can be declared complete:

1. remove the temporary PS4.6 audit workflow after its successful run;
2. update the main development roadmap to this current checkpoint;
3. run the final full validation checkpoint:
   - `:app:testFull`;
   - `:app:assembleDebug`;
   - ASP compile/static/contract gate;
   - Python knowledge-oracle parity;
   - real Clingo semantic smoke;
   - R2 structural regression;
4. perform final changed-file/reference/race audit;
5. only then mark PS4 complete and identify PS5 as the next slice.

Do not begin PS5, Werewolf whole-module removal, D6, A4/ZDD, or merge PR #112 during this checkpoint.

PR #112 remains draft. Do not merge without explicit user authorization.
