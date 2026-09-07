# PS2 Typed RecoverySnapshot Checkpoint — 2026-09-07

> Status: **PS2 COMPLETE / PS3 NOT STARTED**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/persistence-simplification`  
> PR: #112 — Persistence Simplification: recent emergency recovery

## 1. Result

PS2 has replaced the production active-save writer's broad App-runtime snapshot input with a typed emergency-recovery model.

The production write path is now:

```text
live game state
-> activeGameRecoverySnapshot(): RecoverySnapshot
-> RecoverySnapshotJsonCodec.encode(...)
-> saveActiveGameState(...)
```

`persistActiveGameStateIfNeeded()` no longer writes `activeGameSnapshotJson()`.

The old `activeGameSnapshotJson()` implementation intentionally remains in the source tree for now. PS2 does not combine the new write-model cutover with deletion of the old restore infrastructure; removal belongs to PS4 after typed restore is proven.

## 2. New typed recovery boundary

PS2 introduced:

- `persistence/RecoverySnapshot.kt`
- `persistence/RecoverySnapshotJsonCodec.kt`
- `persistence/RecoverySnapshotJsonCodecTest.kt`

The model uses a common recovery envelope plus game-specific payloads:

```text
RecoverySnapshot
├── recovery format / compatibility / saved-at metadata
├── transitional LegacyRestoreCompatibility
└── game
    ├── UndercoverRecovery
    ├── WerewolfRecovery
    └── ClocktowerRecovery
```

Clocktower recovery is grouped by concepts such as identity, position, durable mechanics and durable history rather than recreating the old flat App snapshot DTO.

## 3. Recovery state policy implemented in PS2

### 3.1 Stable navigation by default

PS2 does not persist arbitrary App navigation state.

Only two narrow deal-flow continuation points are retained:

- `PassPhone`
- `RevealCard`

All other screens are represented as a stable recovery entry point. This implements the product rule:

> **Restore the game, not the App.**

### 3.2 Clocktower drafts are not durable by default

The new writer retains confirmed or mandatory continuation facts, including where applicable:

- confirmed attack target;
- confirmed Poisoner target;
- confirmed Monk protection;
- confirmed Mayor redirect;
- confirmed Demon successor;
- unresolved Demon/new-identity continuation;
- pending Klutz continuation;
- Virgin / Slayer / Artist consumed state;
- ghost-vote authority and confirmed highest-vote state;
- game/input revisions;
- semantic-history mode;
- semantic ActionFact timeline and global sequence cursor;
- Clocktower events;
- recorded epistemic observations.

It deliberately does not retain normal unconfirmed Clocktower interaction drafts or day UI state such as current nomination/vote input.

### 3.3 Werewolf continuation remains conservative

Werewolf night-role inputs that may represent already-completed social interactions remain in recovery until the game has a finer per-step commit boundary. This avoids forcing a role to repeat an interaction after process death.

## 4. Transitional legacy-restore compatibility

PS2 intentionally changes the write model before changing the old restore parser.

The current restore path still requires some legacy compatibility/provenance fields, especially for Clocktower. To avoid shipping an intermediate version that writes snapshots the existing restore cannot consume, PS2 carries these fields inside an explicitly transitional `LegacyRestoreCompatibility` structure.

This is **not** the final Recent Emergency Recovery design and must not be treated as justification to preserve the old compatibility system indefinitely.

PS3 owns typed parse/validation and atomic safe restore. PS4 owns deletion of legacy active-save infrastructure after the new read path is proven.

## 5. Lifecycle behavior intentionally unchanged

PS2 did not redesign persistence timing.

The existing active-save trigger behavior remains in place, including lifecycle last-chance persistence. A4 persistence-success ordering was not changed.

Trigger simplification remains PS5 work.

## 6. Test and audit evidence

The successful controlled PS2 cutover was GitHub Actions run `34081179361`.

All relevant steps completed successfully:

1. exact parent/blob locks;
2. focused typed recovery/archive tests before App wiring;
3. App writer patch;
4. exact single-file App diff audit plus `git diff --check`;
5. focused typed recovery/archive tests after App wiring;
6. `:app:testFast`;
7. `:app:assembleDebug`;
8. product commit and push;
9. self-removal of temporary one-shot machinery.

Focused contract tests prove representative behavior for:

- stable Undercover recovery without arbitrary UI state;
- narrow `PassPhone` / `RevealCard` continuation;
- Werewolf already-performed night interaction continuation;
- Clocktower confirmed/mandatory facts surviving while draft targets and day UI are omitted.

The successful product commit is:

```text
abeb056d9f8b2fbe99b62da7f3dcaa5c169b522a
refactor: write typed emergency recovery snapshots
```

The temporary one-shot workflow/script were removed by:

```text
37ec38a4ed2a57ba42b4ab2f50a09d34db589601
ci: remove PS2 recovery one-shot
```

At that cleanup checkpoint, PR #112 live head matched the cleanup commit and the temporary PS2 workflow/script were absent from the live tree.

## 7. Known non-blocking warnings

Validation still reports pre-existing warnings including:

- deprecated Android Gradle Plugin settings/APIs;
- Kotlin data-class generated `copy()` visibility warnings;
- the existing always-true instance check warning in `InformationDecisionFoundationTest.kt`.

These did not block PS2 and are unrelated to the recovery-model cutover.

## 8. Next slice

**PS3 — Typed safe restore** is next, but has not started.

PS3 must establish:

```text
raw stored recovery
-> complete typed parse + validation
-> validity/compatibility decision
-> atomic application to live game state
-> safe recovery entry point
```

Key constraints:

- parsing must not partially mutate live Compose/App state;
- invalid, expired or incompatible recent recovery must fail closed;
- preview should become a projection from typed recovery rather than another independent raw-JSON parser;
- confirmed game actions/information must not replay;
- mandatory continuations must re-enter safely;
- legacy active restore support should be reduced deliberately rather than copied into the new typed model.

Do not start PS3 without a fresh live-head audit and explicit continuation from the user.
