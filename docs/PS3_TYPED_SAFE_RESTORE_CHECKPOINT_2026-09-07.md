# PS3 Typed Safe Restore — Final Checkpoint

> Date: 2026-09-07 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/persistence-simplification`
> Draft PR: #112
> Status: **PS3 COMPLETE — stop before PS4**

## 1. Product boundary

Recent Emergency Recovery is a short-horizon process-loss fallback, not a general Save/Load system.

The governing rule remains:

> **Restore the game, not the App.**

Active recovery accepts only the current recovery format and exact current compatibility token, rejects future timestamps, and uses a four-hour stale-recovery window. Exactly four hours remains eligible; older recovery is expired.

The supported PS3 typed-recovery surface is **Undercover + Blood on the Clocktower**. Werewolf recovery was intentionally not expanded because the current product direction is to remove that module separately.

## 2. Final architecture

```text
live game state
-> typed RecoverySnapshot
-> RecoverySnapshotJsonCodec
-> recent active recovery

raw active recovery
-> validity policy
-> strict complete decoder
-> game-specific validation
-> current Clocktower runtime/ruleset resolution
-> ValidatedRecoveryPlan
       ↙             ↘
typed preview       RecoveryApplicationCoordinator
                    -> session boundary
                    -> one atomic typed apply
```

After a `ValidatedRecoveryPlan` exists, Preview/Apply do not regain independent JSON parsing, content lookup or fallible reconstruction authority.

## 3. PS3.1 — typed read foundation

Completed:

- strict Recovery decoder rather than tolerant Archive-style skipping;
- current-format and exact compatibility-token gate;
- four-hour validity policy including exact-boundary and future-time behavior;
- complete game-specific validation before application;
- current Clocktower ruleset/runtime resolution before Ready;
- `ValidatedRecoveryPlan` and safe re-entry derivation;
- malformed cards, records, Clocktower events, semantic timeline seats and epistemic observations fail closed;
- typed tests protect the behavior contract rather than source-string implementation details.

## 4. PS3.2 — Preview cutover

Completed:

- SavedGamePreview consumes the same validated typed plan used by production restore;
- PS2 Stable snapshots intentionally lacking generic raw `screen` remain resumable;
- invalid/expired recovery is rejected and cleared through the typed path;
- the old independent Preview parser is no longer production authority.

This closes the known **Stable preview deletion** defect.

## 5. PS3.3 — Restore cutover

Production checkpoint:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery
```

The product commit changes only:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
+184 / -319
```

Completed:

- `RecoveryApplicationCoordinator` enforces prepare-before-mutate ordering;
- Rejected recovery clears persisted recovery without crossing the session boundary or applying live state;
- Ready recovery crosses the session boundary once, then applies one complete validated plan;
- production restore no longer parses raw JSON while mutating Compose/App state;
- `ClocktowerNightCheckpoint.fromPersistedValues()` is absent from the production restore path;
- `activeGamePersistenceCoordinator.resolveForRestore()` is absent from the production restore path;
- intentionally discarded Clocktower draft targets and transient day/UI state are reset rather than synthesized;
- confirmed attack/poison/Monk/Mayor/successor facts, game history, revisions, vote authority, events, semantic timeline and epistemic observations restore from typed durable state;
- pending Klutz derives safe re-entry to `Clocktower Judge -> Day -> Klutz`;
- `gameOutcome` derives results presentation rather than restoring raw `showResults` UI state;
- Trouble Brewing setup-rotation bookkeeping remains because completed-game history consumes it; full committed setup is not required as post-start recovery authority.

## 6. PS3.4 — final checkpoint gates

Successful authoritative final audit:

```text
GitHub Actions run: 34096340940
Audit bootstrap: 659567031fe80e6099e8214b6b4305dec900830d
Cleanup checkpoint: 28ad2f7cc734be80cd8aecce718e074f38c38082
```

PASS:

- focused typed Recovery tests:
  - `RecoveryApplicationCoordinatorTest`
  - `RecoveryRestorePlannerTest`
  - `RecoveryPreviewLoaderTest`
  - `RecoveryRestorePlannerNoGreaterJoyTest`
- `:app:testFast --rerun-tasks --no-daemon`;
- `:app:assembleDebug --rerun-tasks --no-daemon`;
- typed production writer assertion: `RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot())`;
- shared Preview/Restore planner assertion: two production `prepareCurrentRecoveryPlan(raw)` call sites;
- exactly one production `RecoveryApplicationCoordinator.apply(...)` restore entry;
- `activeGameSnapshotJson(` has exactly one remaining main-source occurrence — its dead definition, not a caller;
- no `ClocktowerNightCheckpoint.fromPersistedValues` in App production restore;
- no `activeGamePersistenceCoordinator.resolveForRestore` in App production restore;
- exact PS3.3 product diff contains only `CampBoardGameHostApp.kt`;
- exact product `git diff --check`;
- remote-head race lock before final audit cleanup;
- PS3.3 and PS3.4 temporary one-shot machinery removed.

The first PS3.4 attempt also passed the executable gates but failed an over-broad `git diff --check origin/main...HEAD` because older project Markdown intentionally uses trailing double spaces for hard line breaks. The final audit narrowed that check to the authoritative PS3 product/audit changes while preserving every production-path assertion.

The final checkpoint head is intentionally re-triggered with a user-authored `[full-ci]` docs commit so ordinary PR CI and the R2 boundary execute against the final repository state rather than being skipped as docs-only or blocked by bot-authored workflow dispatch.

## 7. Remaining cleanup is PS4, not PS3

PS3 deliberately does not delete every superseded source artifact. PS4 is the cleanup campaign for proven-dead compatibility and duplicate persistence infrastructure, including candidates such as:

- `activeGameSnapshotJson()` dead active-save implementation;
- `LegacyRestoreCompatibility` and obsolete active-state compatibility plumbing;
- superseded raw Preview/Restore helpers and duplicate key mapping;
- obsolete ruleset/setup persistence used only by old active recovery;
- tests that protect removed schema implementation rather than the current product contract.

Archive compatibility, setup-rotation bookkeeping and durable semantic/history contracts must remain unless separately proven obsolete.

## 8. Merge / next-step boundary

PR #112 remains **draft and unmerged**.

Do not merge PR #112 automatically. Do not start PS4, D6, or Werewolf module deletion as part of this checkpoint. The next implementation step requires explicit authorization.
