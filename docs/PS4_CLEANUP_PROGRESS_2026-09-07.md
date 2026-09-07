# PS4 Persistence Cleanup — Progress Checkpoint

> Date: 2026-09-07 Australia/Sydney  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Status: **PS4 audited/planned; implementation not started**

## 1. Current campaign position

Persistence Simplification status:

```text
PS0 contract freeze                 COMPLETE
PS1 Archive / Recovery separation   COMPLETE
PS2 typed Recovery writer           COMPLETE
PS3 typed safe Preview/Restore      COMPLETE
PS4 legacy cleanup                  AUDITED / NEXT
PS5 trigger simplification          NOT STARTED
```

PS3 authoritative production checkpoint:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery
```

PS3 final audit/cleanup lineage includes:

```text
28ad2f7cc734be80cd8aecce718e074f38c38082
8ab643bec947d818b12b29ad43cee3b02ccf1727
```

At the PS4 planning audit, PR #112 was open, draft, unmerged and mergeable. Its head before the PS4 documentation commits was `8ab643...`.

The PS4 route handoff itself was then created in commit:

```text
53478e05295375489dbd69b12b623ef58f12255f
docs: add PS4 cleanup implementation handoff
```

Later documentation commits may advance the head again. Re-query live state before implementation.

## 2. What is already proven by PS3

The current production path is:

```text
live state
-> typed RecoverySnapshot
-> RecoverySnapshotJsonCodec
-> disk recovery

raw recovery
-> prepareCurrentRecoveryPlan(raw)
-> strict current recovery validation
-> ValidatedRecoveryPlan
       |                  |
       v                  v
    Preview       RecoveryApplicationCoordinator
                         |
                         v
                    atomic App apply
```

PS3 proved:

- active Recovery writes no longer use the broad `activeGameSnapshotJson()` payload;
- Preview no longer has an independent raw parser;
- Restore no longer uses the legacy raw restore path;
- malformed/rejected Recovery fails before live-state mutation;
- Stable recovery no longer depends on generic `screen`;
- pending Klutz derives a safe mandatory continuation;
- confirmed durable facts/history are restored while ordinary unconfirmed Clocktower UI drafts are discarded;
- typed Recovery support is intentionally Undercover + Blood on the Clocktower only.

## 3. PS4 audit findings

### Proven-dead / first cleanup target

`activeGameSnapshotJson()` has no current Recovery write/Preview/Restore caller and is the safest first deletion target.

### Transitional shell that must be dismantled in order

`LegacyRestoreCompatibility` is mostly obsolete, but one field is not dead:

```text
troubleBrewingSetupRotationRecord
```

Current typed Clocktower App recovery still restores this record from the legacy shell, and completed-game setup-rotation bookkeeping needs it. It must move into the typed `ClocktowerRecovery` model before the shell is removed.

### Old ownership coupling still present

`RecoveryCompatibilityToken.currentFor(gameKind)` still derives its version component from:

```text
ActiveGamePersistenceCoordinator.CURRENT_VERSION
```

Recovery should own its own compatibility/version identity before the old ActiveGame coordinator/identity system is deleted.

### Legacy-shaped current wire payload remains

Current typed Recovery still serializes transitional active-save metadata such as:

```text
version
PersistedActiveGameIdentity
CommittedClocktowerSetup
clocktowerRulesetRoleIds
clocktowerRulesetRef
```

The 4-hour/current-format Recovery product contract does not justify keeping this migration baggage indefinitely.

### Important retain decision

`ClocktowerRulesetPersistenceBasis` is not automatically legacy debt. Current typed Clocktower runtime resolution still uses a basis to derive the current Trouble Brewing `RulesetRef`. Keep this current semantic unless a later reference/architecture audit proves it unnecessary.

## 4. Approved PS4 checkpoint sequence

```text
PS4.1  delete proven-dead activeGameSnapshotJson path
  |
  v
PS4.2  give Recovery independent format/token ownership
  |
  v
PS4.3  typed Recovery wire cleanup + format bump + move setup rotation
        -> remove LegacyRestoreCompatibility
  |
  v
PS4.4  remove now-dead ActiveGame identity/coordinator plumbing
  |
  v
PS4.5  remaining setup/ruleset/test hygiene
  |
  v
PS4.6  full architecture/reference/test checkpoint
```

Do not collapse these into one broad deletion commit.

## 5. Immediate next slice — PS4.1

### Scope

Start only with a fresh live reference audit around:

```text
activeGameSnapshotJson()
```

Then delete:

- that dead function;
- only helpers/constants/imports proven exclusive to it;
- tests only if they protect no remaining product contract and are demonstrably implementation-only.

Do not in PS4.1:

- change Recovery format;
- change compatibility token semantics;
- remove `LegacyRestoreCompatibility`;
- change save timing/triggers;
- delete Werewolf module;
- begin D6 decomposition.

### Evidence strategy

This is behavior-preserving dead-code cleanup. Under `AGENTS.md`, do not manufacture a RED test merely because source is deleted.

Use:

```text
fresh reference audit
-> identify existing owning tests/baseline
-> delete dead source
-> focused affected tests/compile
-> :app:testFast at logical checkpoint
-> git diff --check
-> exact diff/reference audit
```

Escalate if the compiler/reference audit reveals a hidden consumer; do not broaden deletion heuristically.

## 6. Retain boundaries throughout PS4

Must remain intact unless separately re-audited:

- `GameArchiveRecord` / `GameArchiveJsonCodec`;
- legacy archive-read compatibility where still supported;
- Trouble Brewing setup-rotation current-game bookkeeping and history;
- durable Clocktower semantic history;
- action timeline / epistemic observations;
- current ruleset resolution behavior;
- 4-hour Recovery validity policy;
- fail-closed all-or-nothing preparation/application;
- atomic App apply boundary.

## 7. Werewolf product direction

Do not spend PS4 effort repairing Werewolf recovery. The current product direction is to remove the Werewolf module in a separate campaign after Persistence Simplification is complete.

This means:

- Werewolf may remain as legacy source while PS4 removes shared obsolete persistence infrastructure;
- do not keep shared dead infrastructure solely to support a recovery feature that is intentionally outside the typed-recovery product surface;
- do not delete the entire Werewolf module as part of PS4 unless separately authorized.

## 8. Post-PS4 order

Preferred order after PS4:

```text
PS5 trigger audit/simplification if still warranted
-> final Persistence Simplification validation
-> explicit merge authorization for PR #112
-> separate Werewolf module removal
-> fresh D6 ownership/decomposition audit
```

The old D6 route is historical evidence only; do not resume it unchanged.

## 9. Next-conversation loading set

Read in this order:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS4_CLEANUP.md`;
4. this progress file;
5. `docs/PS3_TYPED_SAFE_RESTORE_CHECKPOINT_2026-09-07.md` only when PS3 validation detail is needed.

Then re-query live GitHub state and begin PS4.1 only.

PR #112 remains draft. Do not merge without explicit user authorization.