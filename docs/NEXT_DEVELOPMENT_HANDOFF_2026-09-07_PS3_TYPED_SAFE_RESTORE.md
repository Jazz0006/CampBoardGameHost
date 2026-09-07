# Next Development Handoff — PS3 Typed Safe Restore

> Date: 2026-09-07 Australia/Sydney  
> Status: **CURRENT PS3 IMPLEMENTATION HANDOFF / START HERE IN A NEW CHAT**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/persistence-simplification`  
> Draft PR: #112  
> Live branch head before this docs update: `cc9538a101150c4d99fa506e94eaefff4844eedc`  
> Last fully validated production checkpoint: `abeb056d9f8b2fbe99b62da7f3dcaa5c169b522a`

## 1. Mission

Implement PS3 as the **minimal process-loss emergency recovery reader/apply path** for the current game.

The product does not need a general Save/Load feature. The intended behavior is:

```text
App process still alive
-> continue using the existing in-memory Compose/App state
-> no recovery operation is needed

App process was killed / app crashed / app was accidentally closed
-> read the one recent RecoverySnapshot from disk
-> validate it completely
-> restore the committed game plus only mandatory continuation state
-> enter a safe gameplay screen
```

Primary rule:

> **Restore the game, not the App.**

Do not implement long-lived save slots, exact UI reconstruction, cross-version migration, or a general session database.

## 2. Frozen product boundary after the 2026-09-07 re-audit

Recent Emergency Recovery exists only as a fallback for **loss of in-memory process state**.

Ordinary app switching, a phone call, screen lock or background/foreground movement does not itself require a restore when the Android process survives. In that case the live in-memory state remains authoritative.

Disk recovery requirements:

- keep one current recovery snapshot only;
- use it after process death, crash, accidental close or equivalent loss of memory state;
- recover the same currently-running game, not a deliberately saved future session;
- preserve already-committed game facts and already-published information;
- preserve only those unfinished continuations whose loss would force a committed/social interaction to be repeated;
- rebuild derived state from current code/rules where safe;
- discard ordinary unconfirmed UI input;
- do not promise recovery across incompatible app/schema versions.

Current stale-recovery hygiene window: **4 hours from the last successful persisted snapshot**.

This 4-hour value is deliberately simple. It is not a user-facing save duration and must not create a migration/session subsystem. It exists only to prevent an abandoned old game from appearing as resumable much later or the next day.

## 3. Current branch state

PS1 and PS2 are complete on draft PR #112.

PS1:

- Archive and active Recovery are separate products.
- New archive writes use `GameArchiveRecord` / `GameArchiveJsonCodec`.

PS2:

- production active writes use typed `RecoverySnapshot` / `RecoverySnapshotJsonCodec`;
- `persistActiveGameStateIfNeeded()` no longer writes broad `activeGameSnapshotJson()` output;
- arbitrary UI state is no longer part of the intended recovery model;
- `LegacyRestoreCompatibility` exists only because the old reader still consumes legacy v3 fields;
- lifecycle save timing and A4 persistence ordering were intentionally left unchanged.

Do not reopen PS1/PS2 unless PS3 proves a real contract defect.

## 4. Re-audit findings that PS3 must fix

### 4.1 Stable Recovery can currently be deleted by the legacy preview reader

PS2 intentionally omits generic `screen` for `RecoveryEntryPoint.Stable`.

However the current `savedGamePreviewFromJson()` still requires a raw `screen` key. `loadSavedGamePreview()` clears active recovery when preview parsing returns null.

Therefore a valid PS2 stable snapshot can currently follow this path:

```text
valid typed stable recovery
-> no raw screen key by design
-> legacy preview parser returns null
-> active recovery is cleared
```

This is a real writer/reader integration defect. PS3 preview cutover must fix it before the campaign can merge.

### 4.2 Klutz is a mandatory continuation and must derive a safe UI re-entry

PS2 correctly stopped persisting normal Clocktower `dayMode` UI.

But when `pendingKlutzName != null`, the game has a rule-required unresolved continuation. Restoring to generic Day Overview would lose the obligation even though the durable fact remains.

Typed restore must derive:

```text
pendingKlutzName != null
-> Clocktower Judge
-> Day phase
-> Klutz continuation mode
```

Do not restore arbitrary historic `dayMode`; derive this exception from durable game state.

### 4.3 Legacy NightCheckpoint decoding can recreate discarded drafts

`ClocktowerNightCheckpoint.fromPersistedValues()` contains legacy fallback behavior such as deriving draft attack/confirmed values from old flat keys.

For the new typed Recovery path this is undesirable: PS2 intentionally discards ordinary unconfirmed attack/poison/Monk/Mayor/successor draft targets.

The new strict reader must map confirmed typed facts directly and leave discarded drafts null. Do not route new RecoverySnapshot through legacy fallback semantics that synthesize drafts.

### 4.4 Current restore is not atomic

The existing `restoreSavedGame()` parses some data, mutates live Compose/App state, then continues parsing and assigning more fields inside `runCatching`.

A later failure clears the persisted snapshot but does not roll back already-mutated in-memory state.

PS3 must use:

```text
raw persisted data
-> complete parse
-> complete validation
-> derive all recovery/runtime values
-> ValidatedRecoveryPlan
-> one application phase with no remaining JSON parsing/content resolution
```

`invalidateA4SessionBoundary()` should occur only after a valid plan exists and immediately before applying the new recovered session.

### 4.5 Active Recovery must decode strictly, unlike Archive

`AppGameStateJsonCodec` currently has tolerant decoders suitable for archive/history compatibility: malformed cards/records/events may be skipped.

Active recovery cannot silently restore a partial game.

Example forbidden behavior:

```text
8 persisted cards
-> one malformed card silently skipped
-> recovery succeeds as a 7-player game
```

Archive may remain tolerant. Recent Emergency Recovery must be all-or-nothing.

## 5. Updated field decisions

### Keep as durable/current-game facts

- game kind;
- cards including actual/shown Clocktower identity and elimination/death state;
- round and semantic phase where gameplay depends on them;
- Clocktower game id/seed;
- confirmed active attack/poison/Monk/Mayor/successor facts;
- red herring;
- applied Demon bluff role names;
- Butler master while relevant;
- Virgin/Slayer/Artist consumed/claim history already represented in the typed model;
- last executed player where later rules consume it;
- ghost-vote authority and confirmed highest vote;
- semantic action timeline;
- epistemic observations;
- semantic-history mode;
- next global timeline sequence;
- revisions while current semantics still use them;
- Clocktower events/records/outcome while production consumes them.

### Keep as narrow mandatory/social continuation

- `PassPhone` / `RevealCard` and current deal index;
- Werewolf judge step plus already-performed night interactions needed to avoid re-waking roles;
- pending Clocktower Klutz owner and whether resolution returns to Dawn;
- unresolved Demon succession continuation;
- pending Werewolf last-words prompt names after a committed elimination/death, because dropping them would skip a configured post-death flow.

### Discard / recompute

- generic raw screen for stable gameplay;
- Clocktower generic day mode;
- nomination/nominator/current unconfirmed vote UI;
- selected execution before confirmation;
- unconfirmed Slayer/Artist UI;
- Fortune Teller/Chambermaid/Ravenkeeper current draft targets;
- attack/poison/Monk/Mayor/successor draft targets;
- recommendation/loading/cache/lock state;
- provisional Drunk recommendation cache;
- selected Werewolf day exile before confirmation;
- `clocktowerKlutzChoiceName` before confirmation;
- `showResults` as stored UI state: derive it from durable `gameOutcome`;
- setup/count fields that can safely be derived from cards/current rules;
- event counter when reconstructible from durable events.

### Remove from final Recovery model after typed restore proves replacement

`LegacyRestoreCompatibility` is transitional, not a product concept.

Re-audit result:

- `activeGameStateVersion`: legacy v3 baggage -> remove;
- persisted active-game content identity envelope: old cross-version/content-hash restore mechanism -> new short-horizon recovery should not depend on it;
- full `committedClocktowerSetup`: no post-start gameplay consumer found in this audit -> remove from Recovery;
- `clocktowerRulesetRoleIds`: derive from recovered cards -> do not persist for Recovery;
- `clocktowerRulesetRef`: runtime still uses it, but reconstruct from recovered assigned roles + current Trouble Brewing ruleset knowledge; if this cannot be resolved safely, fail closed;
- `troubleBrewingSetupRotationRecord`: has a real post-game bookkeeping consumer -> keep, but move to an explicit Clocktower recovery bookkeeping owner instead of `LegacyRestoreCompatibility`.

## 6. Validity policy

Implement a small pure policy. Do not recreate the old compatibility framework under a new name.

A persisted recovery is eligible only when all are true:

```text
recoveryFormatVersion == current supported format
compatibilityToken == current expected token/contract
savedAtMillis is valid
0 <= now - savedAtMillis <= 4 hours
strict payload validation succeeds
current code/rules can reconstruct required derived runtime state
```

Otherwise:

```text
fail closed
-> do not mutate live game state
-> clear/reject the stale or incompatible active recovery
-> continue to normal app landing/setup flow
```

No cross-version migration framework is required.

## 7. Safe re-entry policy

The UI is derived from validated game/continuation facts rather than restored as arbitrary historical UI state.

| Recovery state | Safe re-entry |
|---|---|
| `RecoveryEntryPoint.PassPhone` | PassPhone at recovered deal index |
| `RecoveryEntryPoint.RevealCard` | RevealCard at recovered deal index |
| Undercover stable | main game screen |
| Werewolf stable | Werewolf Judge at preserved safe judge step |
| Clocktower stable Day | Clocktower Judge / Day Overview |
| Clocktower stable Night | Clocktower Judge / recovered safe night step with drafts cleared |
| `pendingKlutzName != null` | Clocktower Judge / Day / Klutz continuation |
| unresolved Demon succession | Clocktower Judge / corresponding required continuation |
| `gameOutcome != null` | game state restored with results presentation derived as active |

If multiple continuations can conflict, validation must reject impossible combinations or choose an explicitly documented precedence. Do not silently guess.

## 8. Target architecture

```text
SharedPreferences raw string / JSONObject
        ↓
RecoverySnapshotJsonCodec.decodeStrict(...)
        ↓
RecoveryValidityPolicy
        ↓
RecoveryRestorePlanner
  - validate game-specific invariants
  - derive setup/ruleset/runtime state
  - validate semantic history/timeline
  - derive safe re-entry
        ↓
ValidatedRecoveryPlan
       ↙          ↘
SavedGamePreview   App apply
                     ↓
              no JSON parsing
              no compatibility/content lookup
              no fallible reconstruction
```

Preview and actual Restore must share this parse/validation pipeline. Do not retain two independent raw JSON readers.

## 9. PS3 implementation sequence

### PS3.1 — Typed read foundation

Create the durable pure-Kotlin seams before touching the large App restore block.

Expected responsibilities:

- strict RecoverySnapshot decoder;
- recovery validity/age/compatibility policy;
- game-specific validation;
- `ValidatedRecoveryPlan` or equivalent immutable application object;
- explicit safe-reentry derivation.

This changes a real persistence contract, so focused typed RED/GREEN tests are appropriate.

At minimum test:

- supported recent stable snapshot parses;
- missing stable raw `screen` does not invalidate recovery;
- expired (>4h) recovery is rejected;
- future/negative age is rejected;
- wrong format/token is rejected;
- malformed card/record/event does not silently produce a partial recovery;
- Clocktower confirmed facts survive while corresponding drafts remain null;
- invalid semantic timeline/observation compatibility fails before application;
- Klutz continuation derives Klutz re-entry;
- game outcome derives results presentation;
- Werewolf pending last words / already-performed night continuation is retained appropriately.

Do not use source-string tests for this behavior.

### PS3.2 — Preview cutover

Change saved-game preview to consume validated typed recovery metadata/plan rather than raw independent keys.

Primary regression to prove:

```text
valid Stable RecoverySnapshot without raw screen
-> preview exists
-> active recovery is not cleared
```

Invalid/expired/incompatible recovery may be cleared safely.

### PS3.3 — Restore cutover

Replace the current large raw `restoreSavedGame()` mutation sequence with:

```text
load raw
-> prepare validated plan
-> if invalid: clear recovery, no live mutation
-> invalidate old session boundary
-> apply validated plan
```

The App edit is large/truncated. Follow root `AGENTS.md`: use connector for small/medium typed files/tests and the established tightly locked GitHub Actions one-shot Python patch for the localized large App rewiring if the patch can be expressed with unique anchors.

Do not opportunistically perform D6 decomposition.

Keep lifecycle persistence trigger timing unchanged during PS3 unless a failing behavior proves a defect.

### PS3.4 — PS3 checkpoint audit

Before declaring PS3 complete:

- focused typed recovery/preview tests GREEN;
- exact App diff audit;
- `git diff --check`;
- `:app:testFast`;
- `:app:assembleDebug`;
- normal PR CI/R2 appropriate to the production checkpoint;
- no temporary one-shot workflow/script remains;
- confirm production write and read paths both use typed Recovery;
- confirm `activeGameSnapshotJson()` is no longer on either production active write or read path, but leave broad deletion to PS4.

Then update docs and stop. Do not merge PR #112 without explicit user authorization.

## 10. What PS3 must not do

Do not:

- build a manual Save/Load UI;
- support deliberate next-day continuation;
- introduce multiple active save slots;
- build version migrations;
- preserve old content-hash identity validation only because tests currently exist;
- restore arbitrary Compose UI state;
- persist every field simply because old `restoreSavedGame()` reads it;
- change Archive tolerant compatibility policy by association;
- redesign Werewolf transaction semantics beyond the minimum recovery need;
- change persistence triggers yet;
- start fresh D6 decomposition.

## 11. PS4/PS5 boundary after PS3

PS4 owns deletion/retirement after typed read/write is proven, including likely removal or narrowing of:

- `LegacyRestoreCompatibility`;
- old active v3 restore coordinator paths and tests that protect only the removed contract;
- `activeGameSnapshotJson()`;
- duplicate raw restore/preview helpers;
- obsolete committed-setup/ruleset persistence plumbing used only by old active recovery.

PS5 then audits when writes occur. Do not mix trigger redesign into PS3.

## 12. Instructions for the next chat

Start by reading:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this file;
4. `docs/PS2_TYPED_RECOVERY_SNAPSHOT_CHECKPOINT_2026-09-07.md`;
5. current live `RecoverySnapshot.kt`, `RecoverySnapshotJsonCodec.kt`, `ActiveGamePersistenceCoordinator.kt` and the App restore/preview anchors.

Then:

1. re-query live `main`, PR #112, branch head and checks;
2. distinguish docs-only head from last validated production checkpoint;
3. re-confirm the two known integration defects before implementation: stable preview deletion and Klutz safe re-entry;
4. design/implement **PS3.1 only first**;
5. use meaningful typed tests-first evidence for the new strict recovery contract;
6. do not begin App large-file rewiring until PS3.1 is GREEN and remotely audited;
7. do not merge PR #112.

The new chat should treat this document as the narrow PS3 implementation authority unless live code proves a documented assumption false.