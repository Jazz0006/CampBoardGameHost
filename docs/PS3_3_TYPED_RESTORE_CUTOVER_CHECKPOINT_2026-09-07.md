# PS3.3 Typed Restore Cutover Checkpoint — 2026-09-07

> Status: **PS3.3 COMPLETE / PS3.4 FINAL CHECKPOINT NOT STARTED**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/persistence-simplification`  
> PR: #112 — Persistence Simplification: recent emergency recovery

## 1. Result

PS3.3 has cut the production active-game restore path over from the legacy raw-JSON mutation sequence to the typed recovery pipeline established by PS3.1/PS3.2.

The production restore path is now:

```text
raw active recovery
-> prepareCurrentRecoveryPlan(...)
-> strict decode + validity + complete validation/runtime resolution
-> ValidatedRecoveryPlan
-> invalidate old A4 session boundary
-> applyValidatedRecoveryPlan(...)
-> safe recovery screen
```

Rejected recovery is cleared without crossing the session boundary or mutating live game state.

The supported PS3 recovery surface remains intentionally limited to:

- Who Is Undercover;
- Blood on the Clocktower.

Werewolf recovery is outside this PS3 typed restore surface and was not expanded or repaired in this slice.

## 2. Atomic application boundary

PS3.3 introduced:

- `persistence/RecoveryApplicationCoordinator.kt`;
- `persistence/RecoveryApplicationCoordinatorTest.kt`.

The coordinator has three outcomes:

```text
Absent   -> no callbacks
Rejected -> clear rejected recovery only
Ready    -> cross session boundary -> apply exactly one ValidatedRecoveryPlan
```

The typed contract proves that validation/preparation completes before either the A4 session boundary or live-state application is entered.

## 3. App restore cutover

The previous `restoreSavedGame()` implementation was replaced as one controlled large-file patch.

The old restore path no longer:

- validates legacy active-game version/content identity inside the App restore block;
- parses cards, records, events or epistemic observations from raw JSON during application;
- resolves current Clocktower rulesets during application;
- calls `ClocktowerNightCheckpoint.fromPersistedValues()`;
- reconstructs discarded Clocktower drafts from confirmed state;
- restores arbitrary raw `screen`, generic `clocktowerDayMode`, nomination/vote input, current Slayer/Artist interaction state, or other non-durable drafts;
- mutates live state before the recovery payload has completely passed typed preparation.

`restoreSavedGame()` is now a narrow orchestration call to `RecoveryApplicationCoordinator.apply(...)` using the same production `prepareCurrentRecoveryPlan(...)` environment already used by typed preview.

## 4. Durable state application

`applyValidatedRecoveryPlan(...)` applies only typed recovery state and safe derived presentation.

Common durable state includes:

- player cards and names;
- elimination records;
- game kind, round and deal index;
- typed game outcome;
- safe recovery entry point.

Undercover restores its durable configuration fields.

Clocktower restores typed durable facts including:

- script, game ID and seed;
- game/input revisions;
- semantic-history mode, ActionFact timeline, cursor, events and epistemic observations;
- current resolved Trouble Brewing ruleset basis/ref where applicable;
- confirmed attack, Poisoner, Monk, Mayor and Demon-successor facts;
- unresolved new-Demon continuation facts;
- red herring, Demon bluffs and Butler master;
- Virgin / Slayer / Artist consumed-state and claim history;
- last executed player;
- pending Klutz continuation;
- ghost-vote authority and confirmed highest vote.

Normal UI/draft state is reset instead of recovered. Pending Klutz re-entry is the deliberate exception: the typed safe re-entry derives `Clocktower Judge -> Day -> Klutz` rather than restoring arbitrary day mode.

## 5. Setup/runtime boundary audit

The old broad committed Clocktower setup object is not reapplied to the live game after recovery. Current App usage shows it is setup/write compatibility state rather than a post-start gameplay dependency.

The Trouble Brewing setup-rotation record is retained because completed-game bookkeeping still consumes it.

Current Clocktower ruleset reconstruction completes before `ValidatedRecoveryPlan` exists; the apply phase consumes the already resolved typed runtime and performs no content lookup.

## 6. Validation evidence

Controlled large-file run:

```text
GitHub Actions run 34094363739
```

The run completed successfully with:

1. exact bootstrap head/blob locks;
2. forced typed restore baseline tests;
3. exact whole-restore-block App patch;
4. forced post-patch focused GREEN tests;
5. `:app:testFast --rerun-tasks --no-daemon`;
6. `git diff --check`;
7. exact App-only changed-file allowlist;
8. assertions that legacy `ClocktowerNightCheckpoint.fromPersistedValues()` and `activeGamePersistenceCoordinator.resolveForRestore(...)` are absent from the App restore path;
9. remote-head race recheck before product push;
10. separate removal of temporary one-shot workflow/script.

Focused tests included:

- `RecoveryApplicationCoordinatorTest`;
- `RecoveryRestorePlannerTest`;
- `RecoveryPreviewLoaderTest`;
- `RecoveryRestorePlannerNoGreaterJoyTest`.

Production cutover commit:

```text
74535e3e17091219520bc4a1d3fbdb60436ece91
refactor: cut restore over to typed recovery
```

The product commit changes only:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
```

with 184 additions and 319 deletions.

Temporary one-shot cleanup commit:

```text
828ae7a90ccc5fd69dbe0d35b7a223463a8c1237
ci: remove ps3.3 restore cutover one-shot
```

The temporary PS3.3 workflow and patch script are absent from the live branch after cleanup.

## 7. What remains

PS3.3 is complete, but PS3 as a whole is not yet closed.

**PS3.4 — final checkpoint gates** remains next. It must perform the final campaign-level audit required by the PS3 handoff, including at minimum:

- focused recovery/preview/atomic-apply contracts;
- exact App diff / source-path audit;
- `git diff --check`;
- `:app:testFast`;
- `:app:assembleDebug`;
- normal PR CI/R2 verification;
- confirmation that production active Recovery write + preview + restore all use the typed pipeline;
- confirmation that legacy `activeGameSnapshotJson()` is no longer on the production active write/read path;
- documentation/status update.

Do not begin PS4 legacy-deletion work, D6, or PR merge as part of PS3.4 unless separately authorized.
