# PS4.4 ActiveGame Identity Retirement — Checkpoint

> Date: 2026-09-07 Australia/Sydney  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Status: **PS4.4 complete; PS4.5 fresh audit next**

## 1. Result

PS4.4 retired the superseded ActiveGame identity/coordinator chain without changing the Recent Emergency Recovery product contract.

Current Recovery remains:

```text
live App state
-> typed RecoverySnapshot v2
-> RecoverySnapshotJsonCodec
-> current-format / exact token / <=4h validation
-> prepareCurrentRecoveryPlan(raw)
-> ValidatedRecoveryPlan
-> atomic RecoveryApplicationCoordinator apply
```

The old ActiveGame identity system is no longer a production or test dependency.

## 2. Save-time invariant ownership was moved before deletion

The old `ActiveGamePersistenceCoordinator.identityForSave(...)` call still contained genuine save-time validation, so it was not deleted blindly.

Those invariants were first re-homed to narrow owners:

```text
ClocktowerActiveSessionValidator
WerewolfActiveGameSaveValidator
```

`CampBoardGameHostApp` now validates Clocktower Recovery saves through the shared built-in ruleset catalog plus `ClocktowerActiveSessionValidator`, and Werewolf saves through `WerewolfActiveGameSaveValidator`.

The returned legacy identity object is no longer constructed or persisted.

## 3. App cutover evidence

Locked App cutover workflow:

```text
PS4.4 active-game validation cutover
run 34119106836
```

All product-critical stages passed:

```text
immutable preconditions                         PASS
legacy-reference audit                         PASS
exact App cutover                              PASS
post-cutover diff / removed App dependency     PASS
focused validation ownership test              PASS
:app:testFast                                  PASS
post-cutover reference audit                   PASS
commit + self-clean                            PASS
```

App cutover production checkpoint:

```text
b561aa7afdb2a9db1b6e707a9caef1205e63c74e
```

## 4. Test-only residue retirement

A fresh audit found four non-production references after App cutover:

- three unused legacy identity fixtures inside typed Recovery tests;
- one source-shape catalog test tied to the old persistence coordinator path.

The three unused fixtures were removed. The catalog architecture test was rewritten toward the new positive ownership boundary: Recovery save validation consumes the shared catalog directly.

Cleanup workflow:

```text
PS4.4 test residue cleanup
run 34119550758
```

Affected tests and `:app:testFast` passed.

The remaining negative string assertion naming `ActiveGamePersistenceCoordinator` was later removed because it was redundant once the positive new-owner assertion existed and it prevented a true zero-legacy-symbol audit.

## 5. Final legacy identity retirement

The first retirement attempt correctly failed closed because the catalog test still contained that old coordinator name as a string literal. No production file was deleted in that failed attempt.

After removing the redundant old-name assertion, retirement run #2 proved the exact legacy-symbol reference set before deletion and then deleted only the isolated chain.

Successful workflow:

```text
PS4.4 retire legacy active identity
run 34120236145
```

Passed gates:

```text
exact branch / blob preconditions                   PASS
legacy symbols isolated to exact deletion set       PASS
delete retired schema + dedicated tests             PASS
zero legacy symbols after deletion                  PASS
focused validator compile/test                      PASS
:app:testFast                                       PASS
commit + workflow self-clean                        PASS
```

Authoritative PS4.4 production checkpoint:

```text
7ba98aa78285daa29e2380d0c6b0f933d8e37133
refactor: retire legacy active-game identity schema
```

## 6. Deleted production files

```text
app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGamePersistenceCoordinator.kt
app/src/main/java/com/codex/campboardgamehost/persistence/ActiveGameIdentityEnvelope.kt
app/src/main/java/com/codex/campboardgamehost/persistence/PersistedGameIdentity.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/persistence/ClocktowerPersistenceIdentityFactory.kt
app/src/main/java/com/codex/campboardgamehost/werewolf/WerewolfPersistenceIdentityFactory.kt
```

Dedicated tests protecting only that retired implementation/schema were also deleted:

```text
app/src/test/java/com/codex/campboardgamehost/persistence/ActiveGamePersistenceCoordinatorTest.kt
app/src/test/java/com/codex/campboardgamehost/persistence/ActiveGameIdentityEnvelopeTest.kt
app/src/test/java/com/codex/campboardgamehost/persistence/PersistedGameIdentityTest.kt
```

This follows the root `AGENTS.md` test-retirement policy: durable save invariants are now covered at the new validator ownership seam, while tests tied only to the obsolete identity schema were retired.

## 7. Retained boundaries

PS4.4 did **not** change or remove:

- typed Recovery v2 contents;
- 4-hour Recovery expiry;
- fail-closed current-format behavior;
- Preview/Restore shared preparation authority;
- atomic `RecoveryApplicationCoordinator` apply;
- Archive compatibility;
- Trouble Brewing setup-rotation bookkeeping/history;
- durable semantic history/action timeline/epistemic observations;
- current Clocktower ruleset resolution;
- persistence trigger timing;
- the Werewolf module itself.

## 8. CI note

The final production commit was pushed by `github-actions[bot]`. The ordinary PR CI and R2 runs created directly from that bot commit were marked `action_required` with no jobs, which is a GitHub Actions trigger/approval condition rather than a test failure.

This checkpoint document is intentionally committed through the normal GitHub connector to provide a non-bot PR checkpoint that can run the normal CI/R2 gates.

## 9. Next slice — PS4.5 fresh audit

Do not assume the remaining persistence-named code is obsolete by name alone.

PS4.5 begins with repository-wide ownership/reference audit of potential transitional residue, especially:

```text
CommittedClocktowerSetup / CommittedClocktowerSetupPersistence
legacy ruleset restore shims
Clocktower checkpoint persistence/mapping residue
tests coupled only to removed active-save schema shape
```

Explicitly retain unless a new proof says otherwise:

```text
TroubleBrewingSetupRotationRecord
real Archive compatibility
current ClocktowerRulesetPersistenceBasis/current ruleset resolution
durable semantic/history persistence
action timeline / epistemic observations
```

Do not enter PS5 trigger timing changes, Werewolf whole-module deletion or D6 decomposition from PS4.5.

PR #112 remains draft and must not be merged without explicit user authorization.
