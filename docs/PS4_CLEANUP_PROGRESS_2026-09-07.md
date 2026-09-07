# PS4 Persistence Cleanup — Progress Checkpoint

> Date: 2026-09-07 Australia/Sydney  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Status: **PS4.1 complete; stop before PS4.2**

## 1. Current campaign position

Persistence Simplification status:

```text
PS0 contract freeze                 COMPLETE
PS1 Archive / Recovery separation   COMPLETE
PS2 typed Recovery writer           COMPLETE
PS3 typed safe Preview/Restore      COMPLETE
PS4 legacy cleanup                  IN PROGRESS
  PS4.1 dead active snapshot        COMPLETE
  PS4.2 Recovery token ownership    NOT STARTED
PS5 trigger simplification          NOT STARTED
```

PS3 authoritative production checkpoint remains:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery
```

PS3 final audit/cleanup lineage includes:

```text
28ad2f7cc734be80cd8aecce718e074f38c38082
8ab643bec947d818b12b29ad43cee3b02ccf1727
```

PS4 planning after PS3 was docs-only before implementation. Live `main` remained:

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
```

PR #112 remains open, draft and unmerged.

## 2. PS4.1 — completed production checkpoint

### Fresh reference audit

Immediately before deletion, repository-wide audit proved:

- `activeGameSnapshotJson(` had exactly one `app/src/main` occurrence;
- that occurrence was the definition itself;
- there was no `app/src/test` caller;
- archive writes, active Recovery writes, Preview and Restore had already moved away from this function.

Therefore `activeGameSnapshotJson()` had no real production consumer and was safe for behavior-preserving dead-code removal.

### Deleted dead implementation

PS4.1 production commit:

```text
721c7394115cdc839afb189f88eb9234cd5ea204
refactor: remove dead active snapshot
```

Deleted:

- `activeGameSnapshotJson()`;
- `PlayerCard.toJson()`;
- `playerCardsToJsonArray()`;
- `EliminationRecord.toJson()`;
- `eliminationRecordsToJsonArray()`;
- `GameOutcome.toJson()`;
- `ClocktowerEvent.toJson()`;
- `clocktowerEventsToJsonArray()`;
- `recordedEpistemicObservationsToJsonArray()`;
- `putNullableBoolean()` after proving it had no remaining production consumer.

`AppJsonPrimitivesTest` was adjusted only to seed boolean JSON values directly so the retained `optNullableBoolean()` reader coverage remains intact.

### Explicitly retained dependencies

Fresh audit proved the following remain shared/current and were not deleted:

- `ACTIVE_GAME_STATE_VERSION`;
- `activeGamePersistenceCoordinator`;
- `ActiveGamePersistenceInputs`;
- `PersistedActiveGameIdentityJsonCodec`;
- `CommittedClocktowerSetupPersistence`;
- `TroubleBrewingSetupCompletionPersistence`;
- `stringsToJsonArray`;
- `putNullableString`;
- `putNullableInt`;
- `optNullableBoolean`;
- `ClocktowerSemanticHistoryPersistence`;
- `ClocktowerRulesetPersistenceBasisJsonCodec`;
- `ClocktowerNightCheckpoint`;
- `ClocktowerGhostVoteAuthorityPersistence`;
- `EpistemicSemanticJson`.

Reader-side legacy helpers such as `playerCardFromJson`, `toPlayerCards`, `eliminationRecordFromJson`, `toEliminationRecords`, `clocktowerEventFromJson`, `toClocktowerEvents`, `toRecordedEpistemicObservations` and `gameOutcomeFromJson` were also intentionally retained because PS4.1 did not authorize broad restore-shell cleanup.

`LegacyRestoreCompatibility` remains present.

### Validation evidence

PS4.1 used no manufactured RED. The cleanup passed:

```text
fresh repository-wide reference audit        PASS
exclusive encode-helper dependency audit     PASS
retained shared-dependency audit              PASS
post-patch exact reference/scope audit        PASS
owning Recovery/persistence tests             PASS
:app:compileDebugKotlin --rerun-tasks         PASS
:app:testFast --rerun-tasks                   PASS
git diff --check                              PASS
final exact changed-file/reference audit      PASS
remote-head race lock before commit           PASS
```

The exact production diff was limited to:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/persistence/AppJsonPrimitives.kt
app/src/test/java/com/codex/campboardgamehost/persistence/AppJsonPrimitivesTest.kt
```

Production/test patch statistics were:

```text
3 files changed, 2 insertions(+), 254 deletions(-)
```

Temporary one-shot patch/workflow tooling was then removed in:

```text
9c69c30ce57c57d8927c3b9e99dd49a8488e53d2
chore: remove PS4.1 one-shot tooling
```

That bot-authored cleanup head caused the normal PR CI/R2 runs to be reported as `action_required` with no jobs. This progress commit is intentionally user-authored and marked `[full-ci]` so normal PR gates can run against the cleaned branch head.

## 3. Frozen boundaries after PS4.1

PS4.1 did **not**:

- change Recovery format;
- change Recovery compatibility-token semantics;
- remove `LegacyRestoreCompatibility`;
- move `TroubleBrewingSetupRotationRecord`;
- change persistence triggers/save timing;
- delete the Werewolf module;
- begin PS5;
- begin D6;
- merge PR #112.

Current production active writes still use:

```text
RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot())
```

The typed Recovery / Preview / Restore architecture and 4-hour fail-closed contract are unchanged.

## 4. Transitional shell still present for later PS4 work

`LegacyRestoreCompatibility` is mostly obsolete, but one field remains genuine current bookkeeping:

```text
troubleBrewingSetupRotationRecord
```

It must not be deleted until a separately authorized later PS4 slice moves that durable field into typed Recovery ownership and proves the bookkeeping path.

`RecoveryCompatibilityToken.currentFor(gameKind)` also still derives its version component from `ActiveGamePersistenceCoordinator.CURRENT_VERSION`. Changing that ownership belongs to PS4.2, not PS4.1.

Typed Recovery also still serializes transitional active-save metadata such as old state version/identity/setup/ruleset fields. Their removal belongs to later explicit PS4 slices and may require a deliberate Recovery format bump.

## 5. Approved PS4 sequence remains unchanged

```text
PS4.1  delete proven-dead activeGameSnapshotJson path     COMPLETE
  |
  v
PS4.2  give Recovery independent format/token ownership   NOT STARTED
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

## 6. Current stop point

**Stop after PS4.1. Do not begin PS4.2 without a fresh live-state/checks review and explicit continuation.**

Before any next implementation slice:

1. re-query live `main`;
2. re-query PR #112 head/state/checks;
3. confirm this `[full-ci]` checkpoint is green;
4. keep PR #112 draft and unmerged;
5. only then plan the separately scoped PS4.2 work if authorized.

## 7. Retain boundaries throughout PS4

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

PR #112 remains draft. Do not merge without explicit user authorization.
