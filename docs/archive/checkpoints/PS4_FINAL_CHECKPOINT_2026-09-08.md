# PS4 Persistence Cleanup — Final Checkpoint

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS4 COMPLETE; PS5 NEXT / NOT STARTED**

## 1. Final product contract

Persistence is current-version continuity infrastructure, not a durable cross-version save-file product.

The supported contract is:

1. Active Recovery exists for short process-loss continuity and remains limited to the current Recovery format/token and the 4-hour validity window.
2. Unsupported/old Recovery fails closed as `UnsupportedFormat`; there is no migration framework.
3. Completed-game Archive remains a separate review/history product, but older Archive wire layouts are not a compatibility promise.
4. No legacy decoder, migration bridge, field fallback, or long-lived compatibility metadata is retained solely to read data written by older app versions.
5. Old persisted data may be discarded after a schema/app-version change.
6. Explicit format/version validation remains as a fail-closed safety boundary; it is not a compatibility layer.

## 2. Final architecture

```text
Current game state
-> typed RecoverySnapshot v2
-> RecoverySnapshotJsonCodec
-> strict current-format decode
-> RecoveryRestorePlanner
-> prepareCurrentRecoveryPlan(raw)
-> ValidatedRecoveryPlan
-> atomic RecoveryApplicationCoordinator apply

Completed game
-> GameArchiveRecord
-> current GameArchiveJsonCodec
-> current-version review history
```

Preview and Restore share the same preparation path. Archive remains independent from active Recovery.

## 3. Cleanup completed

PS4 removed or retired the superseded persistence ownership chain, including:

- `activeGameSnapshotJson()` and dependencies proven exclusive to it;
- Recovery compatibility dependence on the old ActiveGame coordinator;
- `LegacyRestoreCompatibility` and obsolete Recovery wire metadata;
- old ActiveGame persistence coordinator/identity/factory schema;
- dead committed-setup persistence codec;
- legacy ruleset JSON/restore helpers;
- legacy Archive `{ "snapshot": ... }` reader;
- Archive `id -> archivedAtMillis` compatibility fallback;
- tests whose only purpose was to protect retired schema/implementation compatibility.

Real save-time invariants were retained and re-homed into current validators rather than deleted with the old coordinator.

## 4. Retained durable/current behavior

The cleanup deliberately retains:

- Trouble Brewing setup completion/rotation bookkeeping;
- durable Clocktower semantic history;
- action timeline;
- epistemic observations;
- current Clocktower ruleset basis/ref behavior;
- `ClocktowerNightCheckpoint` state and current writer mapping;
- atomic Recovery apply;
- current-format Archive review;
- Undercover and Blood on the Clocktower typed Recovery support.

PS4 did not redesign persistence triggers, remove Werewolf as a whole module, begin D6, or enter A4/ZDD.

## 5. Archive compatibility cleanup — tests-first evidence

Legacy snapshot Archive rejection:

```text
RED  f50c3df06f00b860205c1c388bc56d7c66fbb64d
     CI run 34169274065
     exactly one new contract test failed because the legacy reader was still active

GREEN 4d6bb2bc2f4c2f278ca11400430439140887c670
      8ce4472562211cd5e3f6d4489b6123e3c2f78144
      CI run 34169457561
      :app:testFast PASS / CI gate PASS / R2 PASS
```

Archive identity fallback rejection:

```text
RED  9928fac0c66c9fa8a3ec808c094c85e9ec85e377
     CI run 34169634414
     exactly one new contract test failed because archivedAtMillis still substituted for id

GREEN 7f4cd7ff202284c270c73b9c57f7cfc77a6611d6
      CI run 34169741287
      :app:testFast PASS / CI gate PASS / R2 PASS
```

## 6. Final static architecture audit

Checkpoint:

```text
5ca1f8143ab17bb512107f96bf37c9080de066a4
ci: audit current-only persistence contract
PS4.6 final architecture audit run 34169917902
PASS
```

The audit proved:

- retired ActiveGame/legacy persistence owners absent;
- Recovery v2 wire excludes retired identity/setup/ruleset authorities;
- `RecoverySnapshotJsonCodec.decodeStrict` has one production owner (`RecoveryRestorePlanner`);
- Preview and Restore share `prepareCurrentRecoveryPlan`;
- Recovery application remains atomic through `RecoveryApplicationCoordinator`;
- Archive codec is independent from active Recovery;
- no `decodeLegacyEntry`, legacy `snapshot` reader, `archiveId()` helper, or `id -> archivedAtMillis` fallback remains;
- current durable Clocktower facts remain;
- `git diff --check` passed.

## 7. Final full validation

Stable final validation head:

```text
3e81f08b6ef8afc5c4b701bfd5dcfe33b177bba0
docs: trigger PS4 final validation [full-ci]
```

Final CI:

```text
CI run 34170266988                     SUCCESS
Android testFull + assembleDebug       SUCCESS
ASP contract / oracle harness          SUCCESS
Real Clingo cross-validation           SUCCESS
CI gate                                SUCCESS
```

Structural regression:

```text
R2 main-thread boundary run 34170266998
SUCCESS
```

A final compare from static-audit checkpoint `5ca1f814...` to full-validation head `3e81f08...` shows no `app/src` changes after the successful architecture audit: only PS4 documentation and deletion of the temporary audit workflow changed. Therefore the full validation ran against the same audited production architecture.

## 8. Campaign result

```text
PS0 contract freeze                    COMPLETE
PS1 Archive / Recovery separation      COMPLETE
PS2 typed Recovery writer              COMPLETE
PS3 typed safe Preview/Restore         COMPLETE
PS4 legacy persistence cleanup         COMPLETE
PS5 persistence-trigger simplification NEXT / NOT STARTED
```

PR #112 remains draft, open and unmerged. Do not merge without explicit user authorization.
