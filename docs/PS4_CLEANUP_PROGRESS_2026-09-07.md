# PS4 Persistence Cleanup — Progress Checkpoint

> Date: 2026-09-07 Australia/Sydney  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Status: **PS4.2 complete; stop before PS4.3**

## 1. Current campaign position

Persistence Simplification status:

```text
PS0 contract freeze                 COMPLETE
PS1 Archive / Recovery separation   COMPLETE
PS2 typed Recovery writer           COMPLETE
PS3 typed safe Preview/Restore      COMPLETE
PS4 legacy cleanup                  IN PROGRESS
  PS4.1 dead active snapshot        COMPLETE
  PS4.2 Recovery token ownership    COMPLETE
  PS4.3 Recovery wire cleanup       NOT STARTED
PS5 trigger simplification          NOT STARTED
```

Live `main` remained:

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

PR #112 remains open, draft and unmerged.

PS3 authoritative production checkpoint remains:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery
```

PS4.1 production checkpoint remains:

```text
721c7394115cdc839afb189f88eb9234cd5ea204
refactor: remove dead active snapshot
```

PS4.2 started from the fully validated PS4.1 checkpoint head:

```text
3c6b76c898127e7fd1d9e2384f43c32a3bc69c1c
docs: record PS4.1 cleanup checkpoint [full-ci]
```

## 2. PS4.1 — completed

PS4.1 removed the proven-dead `activeGameSnapshotJson()` path and only helpers proven exclusive to it. It did not change Recovery format/token semantics or remove `LegacyRestoreCompatibility`.

See the PS4.1 production checkpoint above and repository history for the exact deletion audit.

## 3. PS4.2 — completed Recovery compatibility ownership

### Ownership problem before PS4.2

Before this slice, current Recovery compatibility was generated as:

```text
active-v${ActiveGamePersistenceCoordinator.CURRENT_VERSION}:<GameKind>
```

That made short-horizon typed Recovery depend on the obsolete ActiveGame persistence version authority even though Recovery already owned:

```text
RecoverySnapshot.CURRENT_FORMAT_VERSION = 1
```

PS4.2 deliberately cut only this ownership dependency. It did **not** clean the legacy Recovery wire fields or bump the Recovery format; those remain PS4.3 work.

### Chosen current contract

Recovery now owns its compatibility token through its own current format version:

```text
RecoveryCompatibilityToken.currentFor(gameKind)
-> "recovery-v${RecoverySnapshot.CURRENT_FORMAT_VERSION}:${gameKind.name}"
```

For current format v1 this produces, for example:

```text
recovery-v1:Undercover
recovery-v1:Clocktower
```

There is intentionally no second independent Recovery compatibility-version constant. A later deliberate Recovery format bump naturally changes the current token as well.

Old `active-v3:*` tokens are not migrated. This is consistent with the frozen product contract: active Recovery is current-format-only, short-horizon emergency continuity with no cross-version migration promise.

## 4. PS4.2 RED/GREEN evidence

### Typed RED

RED checkpoint:

```text
64c9866fccffa501ae1e1e3889c478b33764231e
test: define Recovery-owned compatibility token contract
```

Added:

```text
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryCompatibilityTokenTest.kt
```

The new typed test protects two durable ownership expectations:

- Undercover current token is `recovery-v1:Undercover` and is accepted by the planner when writer/reader agree;
- Clocktower uses the same Recovery-owned format authority and produces `recovery-v1:Clocktower`.

The RED was exact and meaningful:

```text
:app:testFast
1185 tests completed, 2 failed
```

Both failures were only the two new `RecoveryCompatibilityTokenTest` comparison assertions. There was no compile failure or unrelated regression.

### Production GREEN

Production checkpoint:

```text
9fa0e3f86832cd847fb71126e4de54524e0326d2
refactor: give Recovery independent compatibility token
```

The production change was exactly one line in:

```text
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryAppEnvironment.kt
```

Changed from ActiveGame-owned versioning to:

```text
RecoverySnapshot.CURRENT_FORMAT_VERSION
```

The PS4.2 source/test diff from the PS4.1 checkpoint contains only:

```text
app/src/main/java/com/codex/campboardgamehost/persistence/RecoveryAppEnvironment.kt
app/src/test/java/com/codex/campboardgamehost/persistence/RecoveryCompatibilityTokenTest.kt
```

No Recovery codec/schema fields were changed.

## 5. Validation evidence

GREEN checkpoint evidence:

```text
RecoveryCompatibilityTokenTest                 PASS
:app:testFast                                  PASS
CI gate                                        PASS
R2 main-thread boundary                        PASS
```

Existing typed Recovery tests continue to protect the unchanged eligibility/fail-closed matrix:

- correct current token accepted;
- wrong token rejected as `CompatibilityMismatch`;
- wrong Recovery format rejected;
- future timestamp rejected;
- exactly 4 hours accepted;
- older than 4 hours expired.

The 4-hour `RecoveryValidityPolicy` and strict preparation/application behavior were not changed.

A one-shot checkpoint audit then independently passed:

```text
exact branch/lineage lock                       PASS
git diff --check over PS4.2 source/test diff    PASS
exact two-file source/test allowlist            PASS
absence of ActiveGame CURRENT_VERSION coupling  PASS
presence of Recovery format ownership           PASS
roadmap exact-anchor patch                       PASS
remote-head race lock                            PASS
temporary tooling self-removal                   PASS
```

Roadmap checkpoint:

```text
e3f40244348dfa8cb5ec22df28c1ba59ff1eb3b6
docs: record PS4.2 roadmap checkpoint
```

Temporary checkpoint tooling was removed in:

```text
31753859578907eadaec50efb1fdbf5cedabbcac
chore: remove PS4.2 checkpoint tooling
```

## 6. Frozen boundaries after PS4.2

PS4.2 did **not**:

- bump `RecoverySnapshot.CURRENT_FORMAT_VERSION`; it remains v1;
- remove or rename any current Recovery wire field;
- remove `LegacyRestoreCompatibility`;
- move `TroubleBrewingSetupRotationRecord` out of the legacy shell;
- remove legacy persisted identity/setup/ruleset metadata from Recovery;
- change Recovery save timing or persistence triggers;
- alter the 4-hour validity window;
- add migration support for old Recovery tokens/formats;
- change Archive compatibility;
- delete the Werewolf module;
- begin PS5;
- begin D6;
- merge PR #112.

Current production active writes still use:

```text
RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot())
```

`LegacyRestoreCompatibility` remains intentionally present because its `troubleBrewingSetupRotationRecord` field still carries genuine current-game bookkeeping until PS4.3 moves that ownership safely.

## 7. Next approved slice — PS4.3, not started

The roadmap now points to:

```text
PS4.3 — typed Recovery wire cleanup / format bump
```

PS4.3 is a materially larger schema/ownership change and must remain separate from PS4.2. Its planned order remains:

1. move `TroubleBrewingSetupRotationRecord` into typed Clocktower Recovery ownership;
2. update typed writer / strict decoder / App apply;
3. deliberately bump current Recovery format;
4. remove obsolete ActiveGame-shaped metadata from the Recovery wire;
5. remove `LegacyRestoreCompatibility` only after no genuine durable field remains in it.

Do not introduce migration support for the previous Recovery format; the previous format should fail closed.

## 8. Current stop point

**Stop after PS4.2. Do not begin PS4.3 without a fresh live-state/checks review and explicit continuation.**

Before PS4.3 implementation:

1. re-query live `main`;
2. re-query PR #112 head/state/checks;
3. confirm the PS4.2 checkpoint head is green;
4. keep PR #112 draft and unmerged;
5. re-audit the exact `TroubleBrewingSetupRotationRecord` writer/decoder/application/bookkeeping path before moving it;
6. establish the PS4.3 current-format/previous-format behavior tests before production schema changes.

## 9. Retain boundaries throughout remaining PS4

Must remain intact unless separately re-audited:

- `GameArchiveRecord` / `GameArchiveJsonCodec`;
- real legacy archive-read compatibility;
- Trouble Brewing setup-rotation bookkeeping/history;
- durable Clocktower semantic history;
- action timeline / epistemic observations;
- current ruleset resolution behavior;
- 4-hour Recovery validity policy;
- fail-closed all-or-nothing preparation/application;
- atomic `RecoveryApplicationCoordinator` apply boundary.

## 10. Post-PS4 order

Preferred order remains:

```text
PS5 trigger audit/simplification if still warranted
-> final Persistence Simplification validation
-> explicit merge authorization for PR #112
-> separate Werewolf module removal
-> fresh D6 ownership/decomposition audit
```

PR #112 remains draft. Do not merge without explicit user authorization.
