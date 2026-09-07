# Next Development Handoff — Persistence Simplification

> Date: 2026-09-07 Australia/Sydney  
> Status: **CURRENT ACTIVE HANDOFF / IMPLEMENTATION CONTRACT**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign branch: `codex/persistence-simplification`  
> Baseline main: `ac71cbe392fb542727dc0c2d69ac82c5fdc0435e`

## 1. Mission

Replace the current broad active-game Save/Restore design with a much smaller **Recent Emergency Recovery**
feature appropriate to the actual product need.

The app should survive short accidental interruptions without carrying the architectural cost of a long-lived,
cross-version, full-runtime save system.

Primary rule:

> **Restore the game, not the App.**

This campaign is **not D6 decomposition**. Do not use file-size reduction as the goal and do not opportunistically
extract unrelated App/Host responsibilities. After this campaign is merged, perform a fresh D6 ownership audit on
the reduced codebase.

## 2. Frozen product contract

Active recovery supports:

- the one currently active game;
- process death, crash, accidental close and other short interruption;
- an initial 12-hour validity horizon;
- exact preservation of already-committed game facts and already-published player information;
- safe re-entry into unfinished mandatory flows.

Active recovery does not promise:

- manual save slots;
- long-term retention;
- next-day continuation;
- cross-version migration;
- exact restoration of transient UI/navigation/cache state.

Expired or incompatible recovery may be discarded safely.

## 3. Current audit anchors

Before production edits, re-read/live-audit at least:

- `CampBoardGameHostApp.kt`
  - `activeGameSnapshotJson`
  - `activeGameRecoverySnapshot`
  - `persistActiveGameStateIfNeeded`
  - `persistAndReleaseA4ObservationRebuildIfDurable`
  - `restoreSavedGame`
  - `archiveCurrentGameForRestart`
  - `loadSavedGamePreview` / saved-game preview path
- `persistence/RecoverySnapshot.kt`
- `persistence/RecoverySnapshotJsonCodec.kt`
- `persistence/ActiveGamePersistenceCoordinator.kt`
- `persistence/AppJsonPrimitives.kt`
- `clocktower/session/ClocktowerNightCheckpoint.kt`
- archive/history helpers and related tests
- Clocktower vote transaction and night confirmation owners
- Werewolf Dawn confirmation path

The audit reference is:

`docs/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`

PS2 completion evidence is:

`docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`

## 4. Non-negotiable behavior invariants

### 4.1 No duplicate committed game action

Recovery must never cause a previously committed action, vote consequence, information publication, semantic
ActionFact or epistemic observation to be committed again with different content.

### 4.2 Published information is durable

A recommendation/prepared choice may be regenerated. Information already shown to a player may not silently
change because the App restarted.

### 4.3 Draft is not automatically durable

Clocktower unconfirmed target selections should normally be discarded and re-entered after recovery. Keep only a
field that qualifies as a durable fact or a justified recovery continuation.

### 4.4 Mandatory continuation is durable

Klutz, unresolved Demon succession and similar rule-required unfinished states must re-enter safely rather than
falling back to a generic screen that loses the game obligation.

### 4.5 Werewolf social interactions are special

Do not discard night-role inputs merely because they are called selections. Until there is an explicit per-step
commit model, retain enough continuation state to avoid waking a role again after it already acted.

### 4.6 Parse before live mutation

A recovery JSON/document must be fully parsed and validated into a typed object before applying it to live App
state. Invalid/expired/incompatible recovery must not partially mutate the running application.

### 4.7 Archive compatibility is separate

Do not use strict active-recovery validation as the archive-review reader. Completed historical records may need
a broader compatibility policy than recent active recovery.

### 4.8 Preserve A4 durability ordering

Current observation-rebuild release is coupled to successful persistence. Do not casually change save timing or
persistence success/failure semantics while shrinking the payload.

## 5. Campaign sequence

### PS0 — Product contract freeze

Status: **complete in docs**.

No production code required. The product contract and classification rules are frozen in the roadmap/audit.

### PS1 — Archive / Recovery separation

**Status: complete on draft PR #112.**

Production owns an independent `GameArchiveRecord` / `GameArchiveJsonCodec` boundary. New archive writes no longer call or consume `activeGameSnapshotJson()`, while legacy snapshot-shaped archive records remain reviewable without active-Recovery version/identity validation.

PS1 validation evidence is recorded in:

`docs/PS1_ARCHIVE_RECOVERY_SEPARATION_CHECKPOINT_2026-09-07.md`

### PS2 — Minimal typed RecoverySnapshot

**Status: complete on draft PR #112.**

Goal achieved: create a common typed envelope with game-specific payloads and remove persistence of state that is not needed for emergency continuation from the production active-save writer.

Implemented shape:

```text
RecoverySnapshot
├── recoveryFormatVersion
├── compatibilityToken
├── savedAtMillis
├── transitional LegacyRestoreCompatibility
└── game
    ├── UndercoverRecovery
    ├── WerewolfRecovery
    └── ClocktowerRecovery
```

Key implementation results:

- `persistActiveGameStateIfNeeded()` now writes `RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot())`;
- `activeGameSnapshotJson()` is no longer the production active-save payload, but remains temporarily in source until PS4 cleanup;
- arbitrary UI/navigation state is not persisted;
- only `PassPhone` / `RevealCard` are retained as narrow deal-flow recovery entry points;
- Clocktower confirmed facts, mandatory continuations and durable semantic/history state are retained;
- ordinary Clocktower night draft targets and day UI state are omitted;
- Werewolf already-completed night-role interactions remain conservative continuation state until finer commit boundaries exist;
- lifecycle persistence timing and A4 success/failure ordering remain unchanged.

The old restore parser still requires selected legacy identity/setup/provenance fields. PS2 therefore isolates those requirements inside `LegacyRestoreCompatibility` so the writer cutover does not create an intermediate broken recovery format. This structure is transitional PS2→PS3 support, not the intended final product model.

PS2 validation:

- controlled cutover run `34081179361` GREEN;
- focused `RecoverySnapshotJsonCodecTest` + `GameArchiveJsonCodecTest` GREEN before App wiring;
- exact App single-file diff audit + `git diff --check` GREEN;
- focused recovery/archive tests GREEN after wiring;
- `:app:testFast` GREEN;
- `:app:assembleDebug` GREEN;
- product commit `abeb056d9f8b2fbe99b62da7f3dcaa5c169b522a`;
- one-shot workflow/script removed by cleanup commit `37ec38a4ed2a57ba42b4ab2f50a09d34db589601`.

Detailed checkpoint:

`docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`

### PS3 — Typed safe restore

**Status: next slice; not started.**

Goal:

```text
raw recovery
-> complete parse/validation
-> typed RecoverySnapshot
-> atomic-at-application-boundary state application
-> safe UI re-entry
```

The parser must not mutate Compose state.

Preview becomes a typed projection, not another raw JSON parser.

Recovery should prefer stable entry points (for example Day Overview) over reconstructing arbitrary UI modes.
Mandatory continuations are exceptions.

PS3 must also implement the frozen Recent Emergency Recovery validity policy rather than preserving the old broad restore contract by inertia:

- validate the typed recovery envelope completely before live mutation;
- enforce the current short-horizon compatibility/expiry policy;
- fail closed on invalid/expired/incompatible recovery;
- preserve confirmed/published facts without replay;
- safely re-enter unresolved mandatory continuations;
- preserve enough Werewolf continuation to avoid repeating already-performed social interactions;
- remove old restore-only compatibility/provenance fields from the typed model when PS3 proves they are unnecessary.

Do not begin PS3 from a mechanical port of the existing raw JSON mutation sequence. First audit the current restore parser's consumers and distinguish genuine game facts from legacy restore-validation baggage.

### PS4 — Delete superseded active-save infrastructure

After PS2/PS3 are proven, remove the old responsibilities rather than maintaining dual active schemas.

Audit/deletion candidates:

- old active-save v3 compatibility/identity plumbing no longer needed by short-horizon recovery;
- raw saved-game preview parser;
- duplicate Clocktower checkpoint key mapping/writes;
- Clocktower draft persistence;
- committed setup provenance used only for old restore validation;
- unreachable legacy active-restore branches/shims;
- obsolete tests tied only to removed active-save structure;
- old `activeGameSnapshotJson()` after the typed read/write path no longer depends on it.

Do not delete archive compatibility or durable history tests by association.

### PS5 — Persistence trigger simplification

Only after payload and restore are stable, consider replacing blanket/full synchronous writes with explicit durable
checkpoint/dirty semantics plus lifecycle last-chance persistence.

Preserve existing failure ordering and A4 durability behavior. This phase may require separate transaction-focused
coverage and full validation.

## 6. Recovery classification working table

Treat this as a starting audit table, not a license to delete without checking consumers.

| State family | Current classification |
|---|---|
| cards / actual + shown identity / elimination | DURABLE_GAME_FACT |
| current round + semantic Clocktower phase | DURABLE_GAME_FACT / RECOVERY_CONTINUATION |
| Clocktower confirmed poison/Monk/attack/Mayor/successor facts | DURABLE_GAME_FACT when still relevant |
| Clocktower corresponding draft targets | TRANSIENT_UI by default; omitted by PS2 writer |
| Virgin/Slayer/Artist consumed state | DURABLE_GAME_FACT |
| ghost vote + confirmed highest vote | DURABLE_GAME_FACT |
| current nomination/nominee/vote count before confirmation | TRANSIENT_UI; omitted by PS2 writer |
| current Slayer/Artist input before confirm | TRANSIENT_UI; omitted by PS2 writer |
| pending Klutz / unresolved Demon succession | RECOVERY_CONTINUATION |
| semantic action timeline / epistemic observations | DURABLE_GAME_FACT |
| `clocktowerEvents` / records | DURABLE while production still reads them |
| `nextTimelineGlobalSequence` | DURABLE_GAME_FACT |
| game/input revisions | KEEP initially; re-audit later |
| recommendation loading/candidate/lock UI | TRANSIENT_UI / DERIVED_RECOMPUTABLE |
| provisional Drunk recommendation | DERIVED_RECOMPUTABLE |
| applied Demon bluff role names | KEEP initially pending better owner |
| full `committedClocktowerSetup` | transitional legacy-restore compatibility; PS3 re-audit |
| setup rotation completion record | transitional/bookkeeping; PS3 re-audit |
| setup counts/include flags derivable from cards | DERIVED_RECOMPUTABLE |
| Werewolf already-performed night-role inputs pre-Dawn | RECOVERY_CONTINUATION initially |

## 7. Test/validation gates

Use risk-based project policy, but persistence changes require stronger evidence than pure file moves.

At each logical slice:

- focused affected tests;
- `:app:testFast` when required by current project policy;
- exact diff/ownership audit;
- `git diff --check` or equivalent remote diff review.

Before campaign merge:

- full Android JVM suite;
- debug APK assemble;
- relevant external gates already required by project CI;
- real-device scenarios covering foreground/background, explicit app restart and process-loss-style recovery;
- at least one full Clocktower game/recovery path through second night;
- targeted Werewolf interruption case.

Do not claim real-device acceptance from a successful APK workflow alone.

## 8. Explicit out-of-scope items

Do not expand this campaign into:

- D6 large-file decomposition;
- broad Host command-transaction redesign;
- global ViewModel/state-container migration;
- DataStore migration without a demonstrated need;
- long-term manual Save/Load UX;
- UI-R5 general redesign;
- recommendation quality policy;
- beginner mode;
- A4/ZDD production rollout.

## 9. D6 handoff rule after this campaign

When Persistence Simplification is complete and merged, do **not** revive the closed PR #111 plan as-is.

Instead:

1. re-query live main;
2. remeasure `CampBoardGameHostApp.kt` and `ClocktowerHostScreen.kt`;
3. audit current responsibility/ownership hotspots from scratch;
4. use old D6 notes only as reference evidence;
5. write a fresh D6 implementation route based on the reduced architecture.

## 10. Immediate next action

PS1 and PS2 are complete on draft PR #112. The next implementation slice is **PS3 — Typed safe restore**, but it has **not** started.

Before PS3 production edits:

1. re-query live `main`, PR #112 and branch head and distinguish docs-only head from the last validated product checkpoint;
2. re-read `RecoverySnapshot.kt`, `RecoverySnapshotJsonCodec.kt`, `restoreSavedGame`, saved-game preview parsing and `ActiveGamePersistenceCoordinator`;
3. inventory every old restore field and classify it as typed recovery fact, safe derived state, mandatory continuation, or legacy validation baggage;
4. design a complete parse/validate object that performs no live Compose/App mutation;
5. design a single atomic application boundary from validated typed recovery into live state;
6. enforce the 12-hour/compatibility fail-closed policy;
7. define safe UI re-entry for stable gameplay and mandatory continuations;
8. add tests-first coverage for genuine restore behavior/atomicity contracts, but do not create source-string RED tests for mechanical rewiring;
9. keep lifecycle persistence trigger timing unchanged during PS3 unless a proven defect requires otherwise.

Do not begin PS3 without explicit user authorization.