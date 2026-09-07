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
  - `persistActiveGameStateIfNeeded`
  - `persistAndReleaseA4ObservationRebuildIfDurable`
  - `restoreSavedGame`
  - `archiveCurrentGameForRestart`
  - `loadSavedGamePreview` / saved-game preview path
- `persistence/ActiveGamePersistenceCoordinator.kt`
- `persistence/AppJsonPrimitives.kt`
- `clocktower/session/ClocktowerNightCheckpoint.kt`
- archive/history helpers and related tests
- Clocktower vote transaction and night confirmation owners
- Werewolf Dawn confirmation path

The audit reference is:

`docs/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`

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

**This is the next implementation slice.**

Goal: stop using the active-game snapshot as the archive payload.

#### Required design

Introduce a narrow typed archive projection such as:

```text
GameArchiveRecord
├── archivedAt / id metadata owned by archive store
├── gameKind
├── round/stage needed for review
├── cards / final player state
├── records
├── Clocktower events where applicable
├── outcome
└── only other fields that archive review actually consumes
```

The exact name may change after code audit, but the concept must remain independent from `RecoverySnapshot`.

#### PS1 acceptance

- new archive writes no longer call/use `activeGameSnapshotJson()` as their payload;
- archive review shows the same information for newly archived games;
- existing stored archive entries remain readable where currently supported;
- active recovery save/restore behavior remains otherwise unchanged;
- no lifecycle persistence trigger changes;
- no D6/App-root extraction beyond what is necessary to create the archive boundary.

#### PS1 test strategy

First inspect existing archive tests and review parsing. Add behavior coverage only where the current contract is not
already directly protected.

At minimum characterize/prove:

- a new archive contains all review-visible data currently expected;
- old `{"snapshot": ...}` archive entries still parse/review;
- archive read does not require current active-save compatibility identity;
- archive write does not mutate or consume active recovery state.

This is a durability/data-model boundary, so tests-first behavior coverage is appropriate when a real missing
contract is identified. Do not create source-string tests to assert `activeGameSnapshotJson` text disappeared.

Stop after PS1 validation before beginning PS2 unless the user explicitly asks to continue.

### PS2 — Minimal typed RecoverySnapshot

Goal: create a common envelope with game-specific payloads and remove persistence of state that is not needed for
emergency continuation.

Expected envelope:

```text
RecoveryEnvelope(
    recoveryFormatVersion,
    compatibilityToken,
    savedAtMillis,
    game = UndercoverRecovery | WerewolfRecovery | ClocktowerRecovery,
)
```

Requirements:

- deterministic codec;
- capture timestamp/compatibility outside encoding;
- no Android/Compose dependency in the codec/model;
- no flat God snapshot;
- no cross-version migration framework;
- preserve current persistence trigger timing during this slice unless a behavior test proves it must change.

Clocktower should group identity, safe position, durable mechanics, history and bookkeeping. Unconfirmed drafts are
excluded unless individually justified.

### PS3 — Typed safe restore

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

### PS4 — Delete superseded active-save infrastructure

After PS2/PS3 are proven, remove the old responsibilities rather than maintaining dual active schemas.

Audit/deletion candidates:

- old active-save v3 compatibility/identity plumbing no longer needed by short-horizon recovery;
- raw saved-game preview parser;
- duplicate Clocktower checkpoint key mapping/writes;
- Clocktower draft persistence;
- committed setup provenance used only for old restore validation;
- unreachable legacy active-restore branches/shims;
- obsolete tests tied only to removed active-save structure.

Do not delete archive compatibility or durable history tests by association.

### PS5 — Persistence trigger simplification

Only after payload and restore are stable, consider replacing blanket/full synchronous writes with explicit durable
checkpoint/dirty semantics plus lifecycle last-chance persistence.

Preserve existing failure ordering and A4 durability behavior. This phase may require separate transaction-focused
coverage and full validation.

## 6. Recovery classification working table

Treat this as a starting audit table, not a license to delete without checking consumers.

| State family | Initial classification |
|---|---|
| cards / actual + shown identity / elimination | DURABLE_GAME_FACT |
| current round + semantic Clocktower phase | DURABLE_GAME_FACT / RECOVERY_CONTINUATION |
| Clocktower confirmed poison/Monk/attack/Mayor/successor facts | DURABLE_GAME_FACT when still relevant |
| Clocktower corresponding draft targets | TRANSIENT_UI by default |
| Virgin/Slayer/Artist consumed state | DURABLE_GAME_FACT |
| ghost vote + confirmed highest vote | DURABLE_GAME_FACT |
| current nomination/nominee/vote count before confirmation | TRANSIENT_UI |
| current Slayer/Artist input before confirm | TRANSIENT_UI |
| pending Klutz / unresolved Demon succession | RECOVERY_CONTINUATION |
| semantic action timeline / epistemic observations | DURABLE_GAME_FACT |
| `clocktowerEvents` / records | DURABLE while production still reads them |
| `nextTimelineGlobalSequence` | DURABLE_GAME_FACT |
| game/input revisions | KEEP initially; re-audit later |
| recommendation loading/candidate/lock UI | TRANSIENT_UI / DERIVED_RECOMPUTABLE |
| provisional Drunk recommendation | DERIVED_RECOMPUTABLE |
| applied Demon bluff role names | KEEP initially pending better owner |
| full `committedClocktowerSetup` | candidate for removal from recovery |
| setup rotation completion record | ARCHIVE_OR_BOOKKEEPING; keep minimal |
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

Start **PS1 only**:

1. re-query branch/main heads;
2. inspect the current archive write/read/review path and its tests;
3. establish missing behavior characterization where needed;
4. introduce the narrow archive projection;
5. switch new archive writes away from active snapshot;
6. validate old archive reading + new archive review parity;
7. stop and report the PS1 checkpoint before PS2.

No production implementation has been performed by this documentation checkpoint.