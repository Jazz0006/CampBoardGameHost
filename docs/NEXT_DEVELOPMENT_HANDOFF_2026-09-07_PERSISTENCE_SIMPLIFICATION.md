# Next Development Handoff — Persistence Simplification

> Date: 2026-09-07 Australia/Sydney  
> Status: **CURRENT ACTIVE CAMPAIGN HANDOFF / IMPLEMENTATION CONTRACT**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign branch: `codex/persistence-simplification`  
> Baseline main: `ac71cbe392fb542727dc0c2d69ac82c5fdc0435e`

## 1. Mission

Replace the current broad active-game Save/Restore design with a much smaller **Recent Emergency Recovery**
feature appropriate to the actual product need.

The app should survive loss of in-memory process state without carrying the architectural cost of a long-lived,
cross-version, full-runtime save system.

Primary rule:

> **Restore the game, not the App.**

This campaign is **not D6 decomposition**. Do not use file-size reduction as the goal and do not opportunistically
extract unrelated App/Host responsibilities. After this campaign is merged, perform a fresh D6 ownership audit on
the reduced codebase.

## 2. Frozen product contract

Active recovery supports:

- the one currently active game;
- process death, Android process reclamation, crash, accidental close and equivalent loss of in-memory state;
- a current **4-hour stale-recovery hygiene window from the last successful persisted snapshot**;
- exact preservation of already-committed game facts and already-published player information;
- safe re-entry into unfinished mandatory/social flows whose loss would force a committed interaction to repeat.

Ordinary app switching, a phone call, screen lock or background/foreground movement is **not itself a recovery event** when the Android process survives. In that case the existing in-memory App/Compose state remains authoritative and should simply continue.

Active recovery does not promise:

- manual save slots;
- long-term retention;
- next-day continuation;
- cross-version migration;
- exact restoration of transient UI/navigation/cache state;
- a general session database.

Expired or incompatible recovery may be discarded safely. The 4-hour value is a simple stale-data guard, not a
user-facing save duration and must not grow into a migration/retention subsystem.

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
- `persistence/AppGameStateJsonCodec.kt`
- `persistence/AppJsonPrimitives.kt`
- `clocktower/session/ClocktowerNightCheckpoint.kt`
- archive/history helpers and related tests
- Clocktower vote transaction and night confirmation owners
- Werewolf Dawn / last-words confirmation path

The original requirement-reduction audit is:

`docs/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`

Later product-horizon and PS3 findings supersede any earlier 12-hour wording in that audit.

PS2 completion evidence is:

`docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`

The current narrow PS3 implementation authority is:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS3_TYPED_SAFE_RESTORE.md`

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

For Klutz, keep the pending Klutz owner and return-to-Dawn fact, but do not preserve an unconfirmed Klutz choice.

### 4.5 Werewolf social interactions are special

Do not discard night-role inputs merely because they are called selections. Until there is an explicit per-step
commit model, retain enough continuation state to avoid waking a role again after it already acted.

A pending last-words prompt created after a committed elimination/death is also a narrow recovery continuation;
dropping it would skip a configured post-death flow.

### 4.6 Parse before live mutation

A recovery JSON/document must be fully parsed, validated and converted into a non-fallible application plan before applying it to live App state. Invalid/expired/incompatible recovery must not partially mutate the running application.

### 4.7 Active recovery is strict; Archive may be tolerant

Do not use strict active-recovery validation as the archive-review reader. Completed historical records may need
a broader/tolerant compatibility policy than recent active recovery.

Active recovery must never silently skip a malformed card/record/event and continue with a partial game.

### 4.8 Preview and restore share one typed pipeline

Saved-game preview must be a projection from the same validated typed recovery path used by actual restore. Do not
maintain an independent raw-JSON preview parser with separate schema assumptions.

### 4.9 Preserve A4 durability ordering

Current observation-rebuild release is coupled to successful persistence. Do not casually change save timing or
persistence success/failure semantics while shrinking the payload.

## 5. Campaign sequence

### PS0 — Product contract freeze

Status: **complete in docs; refined after PS3 audit**.

The final contract is process-loss emergency recovery only, with one recent snapshot, a simple 4-hour stale-data
window, no long-term Save/Load UX, no cross-version promise and no exact UI reconstruction contract.

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

**Status: next large implementation task; not started.**

Read and follow:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS3_TYPED_SAFE_RESTORE.md`

Target:

```text
raw recovery
-> strict complete decode
-> current-format/current-contract/<=4h validation
-> derive safe runtime state
-> ValidatedRecoveryPlan
       ↙             ↘
preview              atomic App apply
```

The parser/planner must not mutate Compose state.

Known defects found in the PS3 audit:

1. PS2 Stable recovery intentionally omits generic `screen`, but legacy preview still requires it and may clear a valid recovery.
2. `pendingKlutzName` can survive while legacy restore falls back to generic Day Overview instead of mandatory Klutz mode.
3. legacy `ClocktowerNightCheckpoint.fromPersistedValues()` fallbacks can synthesize draft targets that PS2 intentionally discarded.
4. current `restoreSavedGame()` can mutate live state before later parsing/validation fails.
5. shared tolerant JSON decoders can skip malformed entries; active Recovery needs strict all-or-nothing decoding.

PS3 re-audit also determined:

- old active v3 version/content identity machinery should not become the new short-horizon recovery contract;
- full `committedClocktowerSetup` should leave Recovery when typed restore proves it has no post-start gameplay consumer;
- ruleset role ids should derive from recovered cards;
- runtime `clocktowerRulesetRef` should be rebuilt from recovered cards/current rules and recovery rejected if that cannot be done safely;
- Trouble Brewing setup rotation record has a real post-game bookkeeping consumer and must remain, but under explicit bookkeeping ownership rather than `LegacyRestoreCompatibility`;
- unconfirmed `clocktowerKlutzChoiceName` should not be durable;
- pending Werewolf last-words names should be treated as narrow recovery continuation;
- raw `showResults` UI should not be durable; derive results presentation from `gameOutcome`.

PS3 sequence:

#### PS3.1 — Typed read foundation

Create strict decoder, validity policy, game-specific validation, safe re-entry derivation and an immutable
`ValidatedRecoveryPlan` or equivalent. Use meaningful typed RED/GREEN tests for the new durability contract.

#### PS3.2 — Preview cutover

Preview consumes validated typed recovery. Prove a valid Stable snapshot without raw `screen` remains resumable
and is not cleared.

#### PS3.3 — Restore cutover

Replace raw incremental mutation with prepare validated plan -> apply. Use the established tightly locked GitHub
Actions one-shot Python patch for the large App file if stable unique anchors make it safe. Do not mix D6 decomposition into this slice.

#### PS3.4 — Checkpoint audit

Require focused tests, exact App diff audit, `git diff --check`, `:app:testFast`, `:app:assembleDebug`, normal PR
CI/R2 appropriate to the production checkpoint and proof that temporary one-shot machinery has self-removed.
Then stop before PS4.

### PS4 — Delete superseded active-save infrastructure

After PS2/PS3 are proven, remove the old responsibilities rather than maintaining dual active schemas.

Audit/deletion candidates:

- `LegacyRestoreCompatibility` after all real current-game bookkeeping has a correct owner;
- old active-save v3 compatibility/identity plumbing no longer needed by short-horizon recovery;
- raw saved-game preview parser;
- duplicate Clocktower checkpoint key mapping/writes;
- Clocktower draft persistence;
- committed setup provenance used only for old restore validation;
- obsolete ruleset-basis/reference persistence used only by old active recovery once derivation is proven;
- unreachable legacy active-restore branches/shims;
- obsolete tests tied only to removed active-save structure;
- old `activeGameSnapshotJson()` after the typed read/write path no longer depends on it.

Do not delete archive compatibility, setup-rotation bookkeeping or durable history tests by association.

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
| Clocktower corresponding draft targets | TRANSIENT_UI; omitted by PS2 writer and must remain null after typed restore |
| Virgin/Slayer/Artist consumed state | DURABLE_GAME_FACT |
| ghost vote + confirmed highest vote | DURABLE_GAME_FACT |
| current nomination/nominee/vote count before confirmation | TRANSIENT_UI |
| current Slayer/Artist input before confirm | TRANSIENT_UI |
| pending Klutz owner / return-to-Dawn | RECOVERY_CONTINUATION |
| unconfirmed Klutz choice | TRANSIENT_UI |
| unresolved Demon succession | RECOVERY_CONTINUATION |
| semantic action timeline / epistemic observations | DURABLE_GAME_FACT |
| `clocktowerEvents` / records | DURABLE while production still reads them |
| `nextTimelineGlobalSequence` | DURABLE_GAME_FACT |
| game/input revisions | KEEP initially; re-audit later |
| recommendation loading/candidate/lock UI | TRANSIENT_UI / DERIVED_RECOMPUTABLE |
| provisional Drunk recommendation | DERIVED_RECOMPUTABLE |
| applied Demon bluff role names | KEEP initially pending better owner |
| full `committedClocktowerSetup` | transitional legacy baggage; planned removal from Recovery in PS3/PS4 |
| Trouble Brewing setup rotation record | current-game bookkeeping with real post-game consumer; keep explicitly |
| persisted ruleset role ids/ref | derive for new Recovery; fail closed if current rules cannot reconstruct safely |
| setup counts/include flags derivable from cards | DERIVED_RECOMPUTABLE |
| Werewolf already-performed night-role inputs pre-Dawn | RECOVERY_CONTINUATION initially |
| pending Werewolf last-words names after committed death/exile | RECOVERY_CONTINUATION |
| raw `showResults` | DERIVED_RECOMPUTABLE from `gameOutcome` |

## 7. Test/validation gates

Use risk-based project policy, but persistence changes require stronger evidence than pure file moves.

At each logical slice:

- focused affected tests;
- `:app:testFast` when required by current project policy;
- exact diff/ownership audit;
- `git diff --check` or equivalent remote diff review.

PS3 focused coverage must include at least:

- valid Stable snapshot without raw `screen` remains valid;
- >4h, future-dated, wrong-format and incompatible recovery fail closed;
- malformed active cards/records/events cannot silently produce partial recovery;
- confirmed Clocktower mechanics survive while discarded drafts stay null;
- semantic timeline/history incompatibility fails before application;
- Klutz derives mandatory continuation UI;
- `gameOutcome` derives result presentation;
- Werewolf social continuation/last-words state is preserved where needed.

Before campaign merge:

- full Android JVM suite;
- debug APK assemble;
- relevant external gates already required by project CI;
- real-device ordinary background/foreground scenario confirming in-memory continuation;
- real-device explicit restart/process-loss-style recovery scenario;
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
- next-day continuation;
- cross-version recovery migration;
- a general session database;
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

PS1 and PS2 are complete on draft PR #112. The next large task is **PS3 — Typed safe restore**, and it should be
started in a fresh chat using:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS3_TYPED_SAFE_RESTORE.md`

The new chat must first:

1. read root `AGENTS.md`, `docs/CURRENT_DEVELOPMENT_ROADMAP.md`, the dedicated PS3 handoff and the PS2 checkpoint;
2. re-query live `main`, PR #112, branch head and checks and distinguish docs-only head from the last validated production checkpoint;
3. re-confirm the known Stable-preview and Klutz safe-reentry defects against live code;
4. implement **PS3.1 only first** — strict typed read/validity/planner foundation;
5. use meaningful typed tests-first coverage for the new persistence contract;
6. not begin large App rewiring until PS3.1 is GREEN and remotely audited;
7. keep lifecycle persistence trigger timing unchanged;
8. not merge PR #112 without explicit authorization.