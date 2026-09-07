# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-07 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Live `main` at campaign start:

```text
ac71cbe392fb542727dc0c2d69ac82c5fdc0435e
Merge pull request #110 — Audit Poisoner execution dusk crash path
```

Always re-query live GitHub state before implementation, validation or merge.

Active working branch:

```text
codex/persistence-simplification
```

This branch was created directly from the live `main` checkpoint above. The previous docs-only D6 branch
`codex/d6-ownership-plan` and draft PR #111 are **superseded historical planning evidence**. PR #111 was
closed without merge. Do not implement the old D6 save/restore plan.

## 2. Current priority — Persistence Simplification / Recent Emergency Recovery

### 2.1 Product decision

The app does **not** need a general-purpose long-lived Save Game system.

The supported product need is narrower than the old implementation:

- a game is actively being hosted on one phone;
- ordinary app switching, a phone call, screen lock or background/foreground movement should simply continue from the existing in-memory App/Compose state when the Android process survives;
- disk recovery is needed only when the in-memory process state is actually lost, for example Android process reclamation, crash, accidental close or equivalent process death;
- reopening shortly afterwards should recover enough durable game state to continue the same game safely;
- there is no requirement to save today and continue tomorrow;
- there is no promise that an active game can survive an app upgrade or an incompatible recovery-schema change;
- there is no requirement to reconstruct the exact pre-crash UI/runtime state.

The governing rule is:

> **Restore the game, not the App.**

The active-game persistence feature is therefore redefined as **Recent Emergency Recovery**, specifically a
process-loss fallback rather than a normal Save/Load workflow.

Current stale-recovery hygiene window: **4 hours from the last successful persisted snapshot**. This is not a
user-facing save duration and must not create a session/migration subsystem. It exists only so an abandoned old
game is not presented as resumable much later or the next day.

### 2.2 Why this work precedes D6 decomposition

The persistence requirement audit found that the current active snapshot mixes:

- durable game facts;
- current flow position;
- unconfirmed draft input;
- transient UI state;
- recommendation/cache state;
- setup provenance and compatibility metadata;
- archive/history data;
- duplicated Clocktower checkpoint fields.

The restore path correspondingly reconstructs a large portion of App/Compose state. At the same time, some
Host-local `remember` state is not persisted at all, so the current system already does not provide true
full-fidelity runtime restoration.

Therefore this is a **requirements and feature simplification campaign**, not a file-size/decomposition slice.
Optimizing the existing full snapshot first would preserve unnecessary architecture. Persistence must be
reduced before App/Host ownership is re-audited.

## 3. Persistence Requirement Reduction Audit — accepted classification

Every active-recovery field must be classified into one of the following categories before it is retained.

| Category | Meaning | Recovery policy |
|---|---|---|
| `DURABLE_GAME_FACT` | Losing it changes the game that has already happened | Persist |
| `RECOVERY_CONTINUATION` | Needed to safely resume an unfinished mandatory/social flow | Persist narrowly |
| `DERIVED_RECOMPUTABLE` | Can be deterministically rebuilt from durable facts/current rules | Recompute |
| `TRANSIENT_UI` | Unconfirmed selection, navigation, presentation, loading/cache state | Do not persist |
| `ARCHIVE_OR_BOOKKEEPING` | Long-lived review/history/cross-game accounting, not ordinary active recovery | Separate owner or keep only the minimum current-game bookkeeping actually required |

### 3.1 Must remain durable

At minimum, preserve the already-committed facts needed to avoid changing or replaying the game:

- dealt player/card state including actual/shown identity and death/elimination state;
- game kind, Clocktower script, stable game id and seed where required for deterministic semantics;
- current round/phase where it changes game semantics;
- confirmed mechanical facts that are still active or awaiting later resolution;
- once-per-game / consumed ability facts such as Virgin, Slayer and Artist usage;
- confirmed nomination/vote consequences that affect the rest of the day, including ghost-vote authority and current highest vote;
- mandatory pending continuations such as unresolved Demon succession or Klutz resolution;
- durable records/events while production logic still consumes them;
- semantic action timeline and epistemic observations;
- semantic-history mode and global timeline cursor;
- revision fields that currently participate in durable session/history semantics until a separate proof shows they are reconstructible;
- the minimal setup-rotation bookkeeping required to record a completed game.

### 3.2 Default to recompute or discard

The new recovery design should remove active persistence of state whose loss merely requires re-entering an
unfinished interaction or rebuilding presentation:

- generic raw `screen` restoration for stable gameplay;
- Clocktower day `dayMode` where a safe Day Overview can be derived instead;
- current nominator/nominee/current vote count before vote confirmation;
- selected execution before end-day confirmation;
- Slayer claimant/target UI selection before resolution;
- Artist claimant/truthful/shown-answer UI selection before confirmation;
- Clocktower attack/poison/Monk/Mayor/successor **draft** targets when the corresponding fact has not been confirmed;
- Fortune Teller/Chambermaid/Ravenkeeper current draft targets;
- recommendation loading state, locks, temporary candidate lists and other cache/UI state;
- provisional Drunk-information recommendation cache;
- setup/count flags that can be derived from the committed dealt cards;
- event counter when it can be safely reconstructed from durable events;
- `showResults` as stored UI state when it can be derived from a durable `gameOutcome`;
- preview-only data that can be projected from a typed validated recovery object.

### 3.3 Important exceptions and cautions

Do not apply “drafts are disposable” mechanically across all games.

The current Werewolf night flow retains several selections until a single Dawn confirmation. Re-waking roles
after a crash may itself change the social game. Until Werewolf gains explicit per-step commit boundaries, its
already-completed night-role inputs may remain `RECOVERY_CONTINUATION` even though they look like draft fields.

A pending Werewolf last-words prompt created after a committed death/exile is also a narrow recovery continuation:
dropping it would skip a configured post-death flow rather than merely discard unconfirmed UI input.

Clocktower information also uses a publication boundary:

```text
recommendation / prepared choice
!=
published information
```

Recommendation state may be recomputed, but information already shown to a player is durable history and must
not silently change after recovery.

`recommendedDemonBluffRoleNames` currently behaves closer to an applied/committed bluff triple than a disposable
cache; retain it until publication ownership proves a better durable source.

For Klutz, preserve the pending Klutz owner and whether the resolution returns to Dawn, but do not preserve the
unconfirmed `clocktowerKlutzChoiceName`. On recovery the mandatory Klutz interaction should be re-entered safely.

## 4. Archive and active recovery are separate products

PS1 enforces the archive boundary: new archive writes project live game state into a narrow `GameArchiveRecord` and encode it with `GameArchiveJsonCodec`; they no longer consume `activeGameSnapshotJson()`. Legacy `{"snapshot": ...}` archive entries remain readable through an archive-only compatibility fallback.

PS2 independently enforces the active-write boundary: production active saves build a typed `RecoverySnapshot` and encode it with `RecoverySnapshotJsonCodec`; `persistActiveGameStateIfNeeded()` no longer writes the broad `activeGameSnapshotJson()` payload. The old restore parser remains temporarily supported until PS3, so PS2 carries an explicit transitional `LegacyRestoreCompatibility` bridge rather than breaking recovery between slices.

Define two independent concepts:

```text
RecoverySnapshot
    short-lived current-game crash/process-death recovery

GameArchiveRecord
    long-lived completed/restarted-game review and cross-game history
```

Archive compatibility may remain broader than active recovery compatibility. Old archive records should remain
readable where practical, but that requirement must not force the active recovery format to carry long-term
migration baggage.

A strict active-game restore parser must never become the archive-review parser.

## 5. Active campaign sequence

### PS0 — Freeze the recovery product contract

Status: **complete at planning level; refined after PS3 re-audit**.

Frozen requirements:

- one recent active recovery only;
- recovery exists for loss of in-memory process state, not ordinary background/foreground movement while the process survives;
- current stale-recovery hygiene window is 4 hours from the last successful persisted snapshot;
- no user-facing long-term Save Game contract;
- no next-day continuation contract;
- no cross-version active-recovery compatibility promise;
- exact UI/runtime reconstruction is not required;
- confirmed game facts and already-published information must survive;
- unconfirmed transient UI may be discarded;
- recovery must enter a safe, non-duplicating continuation point.

Do not rebuild the old content identity framework merely under a new name. Compatibility for Recent Emergency
Recovery should remain a small current-format/current-contract check.

### PS1 — Separate Archive from Recovery

Status: **complete on draft PR #112**.

Goal: remove the product/data-model assumption that the active recovery snapshot is also the archive payload.

Required results achieved:

- typed `GameArchiveRecord` / `GameArchiveJsonCodec` archive projection;
- archive write no longer consumes the active recovery JSON object;
- existing user-visible archive/review content preserved;
- old archive-read compatibility retained where currently supported;
- archive review remains separate from strict recent-recovery validation.

PS1 validation completed with the focused `GameArchiveJsonCodecTest`, `:app:testFast`, `git diff --check`, and an exact App wiring diff audit.

### PS2 — Introduce minimal typed `RecoverySnapshot`

Status: **complete on draft PR #112**.

Goal achieved: production active-save writes no longer serialize the broad App-runtime snapshot. They project live state into a typed common envelope with game-specific payloads and encode through `RecoverySnapshotJsonCodec`.

Implemented shape:

```text
RecoverySnapshot
├── recoveryFormatVersion
├── compatibility token
├── savedAtMillis
├── transitional LegacyRestoreCompatibility
└── game
    ├── UndercoverRecovery
    ├── WerewolfRecovery
    └── ClocktowerRecovery
```

Key PS2 results:

- `persistActiveGameStateIfNeeded()` now writes `RecoverySnapshotJsonCodec.encode(activeGameRecoverySnapshot())`;
- arbitrary navigation state is not persisted; only narrow `PassPhone` / `RevealCard` deal continuation survives explicitly;
- Clocktower confirmed facts, mandatory continuations and durable semantic/history state are retained;
- normal Clocktower draft targets and day UI state are omitted from the new write model;
- Werewolf already-performed night-role inputs remain conservative recovery continuation until finer commit boundaries exist;
- current lifecycle save trigger timing and A4 durability ordering remain unchanged;
- the old `activeGameSnapshotJson()` implementation remains temporarily in source but is no longer the production active-save payload;
- old restore requirements are isolated behind `LegacyRestoreCompatibility` pending PS3/PS4 rather than copied as the intended final design.

PS2 validation evidence:

- successful controlled cutover run `34081179361`;
- focused `RecoverySnapshotJsonCodecTest` + `GameArchiveJsonCodecTest` passed before and after App wiring;
- exact App single-file diff audit and `git diff --check` passed;
- `:app:testFast` passed;
- `:app:assembleDebug` passed;
- product commit `abeb056d9f8b2fbe99b62da7f3dcaa5c169b522a`;
- temporary one-shot workflow/script removed by cleanup commit `37ec38a4ed2a57ba42b4ab2f50a09d34db589601`.

Detailed checkpoint:

- `docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`

### PS3 — Typed safe restore for process-loss recovery

Status: **next large implementation task; not started**.

Dedicated implementation authority:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS3_TYPED_SAFE_RESTORE.md`

Target architecture:

```text
raw persisted recovery
-> strict complete typed decode
-> current-format / current-contract / <=4h validation
-> derive current-rules/runtime state
-> ValidatedRecoveryPlan
       ↙             ↘
SavedGamePreview     atomic App apply
```

Do not incrementally mutate live Compose state during parsing, content/ruleset resolution or semantic validation.

Known defects found in the PS3 audit that must be fixed:

1. **Stable preview deletion** — PS2 stable snapshots intentionally omit generic `screen`, but the legacy preview reader still requires it and can clear a valid recovery.
2. **Klutz safe re-entry** — PS2 correctly omits generic `dayMode`, but `pendingKlutzName` must derive the mandatory Klutz continuation rather than fall back to Day Overview.
3. **Legacy checkpoint draft synthesis** — the new typed read path must not use legacy fallback decoding that recreates intentionally discarded draft targets from confirmed facts.
4. **Partial live mutation risk** — old `restoreSavedGame()` can mutate live state before a later parse/validation failure.
5. **Tolerant decoder mismatch** — Archive may skip malformed historical entries, but active Recovery must reject malformed cards/records/events rather than silently restore a partial game.

Re-audited transitional compatibility fields:

- old active-state version -> remove from final Recovery;
- old persisted content identity envelope -> do not preserve as the new short-horizon contract;
- full `committedClocktowerSetup` -> remove from Recovery when typed restore proves no gameplay consumer;
- `clocktowerRulesetRoleIds` -> derive from cards;
- `clocktowerRulesetRef` -> reconstruct from recovered cards/current ruleset knowledge and fail closed if impossible;
- Trouble Brewing setup rotation record -> keep as explicit current-game bookkeeping because completed-game rotation history still consumes it.

PS3 implementation sequence:

#### PS3.1 — Typed read foundation

Create pure Kotlin strict decoder, validity policy, game-specific validation and `ValidatedRecoveryPlan`/equivalent
safe-reentry derivation. Use meaningful typed RED/GREEN tests for the new durability contract.

#### PS3.2 — Preview cutover

Saved-game preview must consume the same validated typed recovery path as actual restore. A valid Stable snapshot
without a raw `screen` key must remain resumable and must not be cleared.

#### PS3.3 — Restore cutover

Replace the large raw mutation sequence with load -> prepare validated plan -> apply. Use the established locked
GitHub Actions one-shot Python patch for the large App file if stable unique anchors make it safe. Do not mix D6 decomposition into this change.

#### PS3.4 — checkpoint audit

Require focused recovery tests, exact App diff audit, `git diff --check`, `:app:testFast`, `:app:assembleDebug`,
normal PR CI/R2 appropriate to the production checkpoint, and verification that no temporary one-shot machinery
remains. Stop before PS4 and do not merge PR #112 without explicit authorization.

### PS4 — Retire superseded active-save infrastructure

After the new write/read path is proven, delete obsolete active-save responsibilities rather than leaving dual
schemas indefinitely.

Candidates include:

- `LegacyRestoreCompatibility` and old active v3 restore compatibility/identity plumbing no longer needed by short-horizon recovery;
- separate raw saved-game preview parser;
- duplicate Clocktower checkpoint key mapping/writes;
- persistence of Clocktower draft fields and other transient UI;
- committed setup provenance that has no post-start gameplay consumer;
- obsolete ruleset-basis/reference persistence used only by old active recovery when the new reader can derive it;
- unreachable legacy active-save branches/shims;
- tests that only protect removed schema/implementation details;
- old `activeGameSnapshotJson()` after typed production read/write no longer depends on it.

Retain real archive compatibility, setup-rotation bookkeeping and durable semantic/history tests.

### PS5 — Simplify persistence triggers

Only after the new recovery model/restore behavior is stable, audit the current blanket synchronous write path.

Target direction:

```text
durable game transaction committed
-> recovery becomes dirty / checkpointed

ON_PAUSE / ON_STOP
-> last-chance durable write
```

Do not change save contents and save timing in the same early checkpoint unless a failing behavior forces it.
A4 durability ordering must remain protected where observation publication currently depends on successful
persistence.

## 6. Validation strategy

This campaign changes a durability contract, so behavior tests are justified even when structural source tests
are not.

Required coverage should include representative Undercover, Werewolf and Clocktower recovery states and the
highest-risk boundaries:

- valid stable Recovery without generic `screen` survives preview/restart;
- confirmed fact survives restart;
- unconfirmed Clocktower draft is intentionally discarded/re-entered and is not recreated by legacy fallback;
- published information/history is unchanged;
- ghost-vote/highest-vote state survives while unconfirmed current nomination does not;
- pending Klutz derives the mandatory Klutz continuation;
- unresolved Demon continuation survives;
- Werewolf night continuation does not force already-performed social interactions to repeat;
- pending configured Werewolf last-words continuation is not silently skipped;
- `gameOutcome` derives result presentation rather than persisting raw `showResults` UI state;
- expired (>4h), future-dated, wrong-format or incompatible recovery fails closed without partially mutating live state;
- malformed active-recovery cards/records/events fail all-or-nothing rather than silently restoring a partial game;
- archive review remains readable after archive/recovery separation;
- no duplicate semantic action/observation occurs because of recovery.

Use frozen representative recovery fixtures where wire compatibility itself is the contract. Do not prove a
codec by encoding and immediately decoding the same object only. Avoid source-string tests for normal behavior.

At logical checkpoints use the project risk-based test policy from `AGENTS.md` / `TESTING_STRATEGY.md`.
Persistence/restore/transaction boundaries merit focused behavior coverage and a full validation gate before
merge. Real-device recovery testing is required before this campaign is considered complete.

Real-device acceptance should distinguish:

- ordinary background/foreground with process alive -> live in-memory state continues normally;
- explicit restart/process-loss-style recovery -> typed disk Recovery restores the same game safely.

## 7. Explicit non-goals

The current campaign does **not** include:

- D6 App/Host large-file decomposition;
- a global ViewModel migration;
- DataStore migration merely for modernization;
- long-lived save slots or manual Save/Load UX;
- next-day continuation;
- cross-version active-game migration framework;
- a general session database;
- recommendation-quality redesign;
- A4/ZDD production cutover;
- unrelated Host UI redesign.

File-size reduction is a welcome consequence, not an acceptance metric.

## 8. D6 status — deferred and must be re-audited

Night Step decomposition D1–D5 is complete and integrated through PR #106. The subsequent crash/initialization/
Poisoner corrections #107, #108 and #110 are also integrated into current `main`.

The previous D6 plan assumed preservation and decomposition of the current full active-save system. That premise
is now invalid. Draft PR #111 was closed without merge.

After Persistence Simplification is completed and merged:

1. re-fetch live `main`;
2. re-measure App/Host ownership and change-context hotspots;
3. ignore the old D6 sequence except as historical evidence;
4. write a fresh D6 ownership/decomposition audit against the reduced codebase;
5. then execute the separately approved decomposition campaign.

Do not mix large-file extraction into PS1–PS5 simply because persistence code moves naturally. Move code only
when required to establish the new recovery/archive responsibility boundary.

## 9. Other pending roadmap items

### UI-R5 — real-device stabilization

Still open. Prior CI built debug APKs, but successful workflow execution is not physical-device acceptance.
Persistence Simplification itself will require targeted process-death/restart/device testing. The broader UI-R5
matrix remains after the fresh D6 campaign unless a release-blocking UI defect is encountered earlier.

### EPI-MQ — information quality / Productive Uncertainty

Remain deferred until the current structural/recovery work is stable. The accepted future direction is to reject
obviously unreasonable unreliable clues and optimize useful uncertainty/evil-side playability without returning
committed shown-identity authority to recommendation.

### Beginner Mode

Deferred until the cognitive-consistency/information-quality foundation is ready. Product direction remains:
minimal user choices, automatic clues, unreliable drunk/poison information policy, and automatic Spy/Recluse
false registration policy suitable for inexperienced Storytellers.

### UX-R6

Legacy recommendation-provider replacement remains after EPI-MQ as currently planned.

### A4 / ZDD

Remain non-production. Do not treat this persistence campaign as authorization for rollout.

## 10. Current references

Active campaign documents:

- `docs/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md` — original requirement-reduction audit; later product-horizon refinements are superseded by this roadmap and the PS3 handoff;
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PERSISTENCE_SIMPLIFICATION.md` — campaign umbrella handoff;
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS3_TYPED_SAFE_RESTORE.md` — **current next-chat implementation authority**;
- `docs/PS1_ARCHIVE_RECOVERY_SEPARATION_CHECKPOINT_2026-09-07.md`;
- `docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`.

Long-lived engineering authority:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Prior Night Step decomposition/reference evidence remains historical support, not the active execution route:

- `docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`;
- PR #106 and its merged commits/checks;
- closed PR #111 for the superseded D6 plan.

## 11. Status authority rule

If documents disagree:

1. official Blood on the Clocktower rules/rulings control gameplay correctness;
2. root `AGENTS.md` controls project execution and architecture/test rules;
3. this roadmap controls current project state, product boundary and priority;
4. `NEXT_DEVELOPMENT_HANDOFF_2026-09-07_PS3_TYPED_SAFE_RESTORE.md` controls the approved PS3 implementation slice;
5. the umbrella Persistence Simplification handoff controls the remaining campaign where it does not conflict with the PS3 handoff;
6. specialized design docs control their own semantic/product domain where non-conflicting;
7. archive documents, old Git branches and historical PR records are evidence only.