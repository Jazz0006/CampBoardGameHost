# CampBoardGameHost — TBSP-5 Rotation History Persistence Handoff

> Updated: 2026-08-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign: **TBSP — Trouble Brewing Setup Preset Integration**  
> Status: **TBSP-5 COMPLETE / TBSP-6 NOT STARTED**  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR / CI carrier: **#57 — TBSP: integrate Trouble Brewing setup presets**  
> Live `main` baseline at completion: `ba7cfa12853a8829ecf228c05cf2a22067f1e6e4`  
> TBSP-4 validated checkpoint: `f68d8326de6bf57ecfd632fef73689c4900f87a9`  
> TBSP-5 code/test checkpoint: `3c9603312c0f3694d91d9707d9ece89e4edc24f9`  
> TBSP-5 full-acceptance carrier: `97e39027849a1d4925e94dc61329aa6c560db97b`

## 1. Scope completed

TBSP-5 adds a dedicated durable store for Trouble Brewing preset-rotation history.

Source:

```text
app/src/main/java/com/codex/campboardgamehost/persistence/
    TroubleBrewingSetupRotationHistoryStore.kt
```

Tests:

```text
app/src/test/java/com/codex/campboardgamehost/persistence/
    TroubleBrewingSetupRotationHistoryStoreTest.kt
```

This slice deliberately does **not** wire the store into the live App start/end lifecycle. That belongs to TBSP-6 production cutover / restore ownership.

## 2. Persistence ownership decision

The new TB rotation-history store is independent from both:

```text
ACTIVE_GAME_STATE_KEY
GAME_HISTORY_KEY
```

The existing `GAME_HISTORY_KEY` is a review/archive history and is not a valid initial-setup authority:

- `archiveCurrentGameForRestart()` can archive a game merely because the user is restarting or returning to player management;
- therefore archive membership does not prove that the game actually completed;
- archived `PlayerCard` values represent the later/current game state and can no longer be assumed to equal the immutable initial role composition after role changes.

TBSP rotation history must therefore be written from the original `TroubleBrewingSetupPresetSelection` provenance, not reconstructed from final cards.

## 3. Store contract

The pure/durable API is:

```text
TroubleBrewingSetupRotationHistoryStore
    .recordCompletedGame(gameId, selection)

TroubleBrewingSetupRotationHistoryStore
    .historyFor(datasetId, schemaVersion, playerCount)
```

`recordCompletedGame` is intentionally named for the semantic event that authorizes rotation-history insertion. TBSP-6 must call it only from a true completed-game lifecycle point; generic Restart/archive is not sufficient.

The Context adapter uses a separate SharedPreferences key:

```text
tb_setup_rotation_history_v1
```

under the existing application preference file:

```text
camp_board_game_host
```

Writes use synchronous `commit()` so a completion record has an explicit durable success result before the caller treats it as persisted.

## 4. Persisted provenance

Each completed record stores a stable `gameId` plus the selector-owned initial setup provenance required by TBSP-2:

```text
datasetId
schemaVersion
presetId
playerCount
realNonDemonRoleIds
minionRoleIds
primaryStyleTag
selectedDrunkShownRole
```

The record is derived directly from the selected preset:

```text
realNonDemonRoleIds = townsfolk + outsiders + minions
minionRoleIds       = preset.minions
primaryStyleTag     = preset.styleTags.firstOrNull()
selectedDrunkShownRole = selection.selectedDrunkShownRole
```

The Demon is intentionally excluded from `realNonDemonRoleIds`, matching the established TBSP-2 exact-repeat and overlap contract.

JSON set values are encoded in sorted order so persistence text does not depend on Set iteration order.

## 5. Isolation and bounded history

Selector-facing projection is filtered by exact:

```text
datasetId
+ schemaVersion
+ playerCount
```

History is returned newest-first.

The store retains at most:

```text
5 records per player count
```

matching the normative TBSP history horizon and scorer weights:

```text
1.00, 0.65, 0.40, 0.20, 0.10
```

Therefore an 8-player completed setup cannot enter the 9-player selector history projection.

## 6. Exactly-once / retry semantics

A stable `gameId` is persisted with each retained rotation record.

Within retained history:

```text
same gameId + same setup provenance
-> idempotent success
-> no duplicate write

same gameId + different setup provenance
-> IllegalArgumentException
-> existing record remains authoritative
```

This supports completion-persistence retry convergence without adding duplicate recent-game weight.

## 7. Corruption and version behavior

Rotation history is a diversity/selection input, not mechanical game-state truth. The store therefore uses a fail-soft read policy:

```text
missing raw history        -> EMPTY
malformed JSON             -> EMPTY
unsupported store version -> EMPTY
```

A later valid `recordCompletedGame` can replace malformed/unsupported stored data with a valid v1 payload.

This policy is intentionally separate from active-game restore, whose compatibility requirements remain strict.

## 8. Tests-first history

### TBSP-5A RED

Commit:

```text
8325a58cd20526befaa013d5964ded91f0e76220
```

The test first required durable store recreation and exact reconstruction of the selector-facing rotation record.

Observed Android FAST failure was the expected compile RED only:

```text
Unresolved reference 'TroubleBrewingSetupRotationHistoryStore'
```

No local T0 was run from this Chat runtime; the RED was observed through the real GitHub Actions `:app:testFast` route.

### TBSP-5A GREEN

Commit:

```text
f92452ed5241813716605ddecc790895b1bf7ba4
```

Added the dedicated v1 store and Context/SharedPreferences adapter.

Validation:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
```

### TBSP-5B / 5C / 5D lock-in

Commit / final code-test checkpoint:

```text
3c9603312c0f3694d91d9707d9ece89e4edc24f9
```

Added typed coverage for:

```text
same-game completion retry is idempotent
conflicting reuse of gameId is rejected
newest-first ordering
five-record bound per player count
player-count isolation
dataset isolation
schema isolation
malformed JSON fail-soft
unsupported persistence version fail-soft
recovery by next valid completion write
```

These tests were GREEN immediately because the generalized 5A implementation already satisfied the contract. Do not invent RED states for 5B–5D.

Validation at `3c960331...`:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
```

## 9. Exact diff audit

Relative to the TBSP-4 docs head `452e65c7dd50d064c2e074f93ea2fc91fce3d313`, TBSP-5 code/test work through `3c960331...` changes exactly two files:

```text
ADDED app/src/main/java/com/codex/campboardgamehost/persistence/
      TroubleBrewingSetupRotationHistoryStore.kt

ADDED app/src/test/java/com/codex/campboardgamehost/persistence/
      TroubleBrewingSetupRotationHistoryStoreTest.kt
```

No `CampBoardGameHostApp.kt` production lifecycle wiring was changed.

No No Greater Joy, A3, setup scoring, selector weighting, deal materialization, night semantics, or active-game schema was changed.

## 10. Full acceptance checkpoint — GREEN

The docs-only carrier:

```text
97e39027849a1d4925e94dc61329aa6c560db97b
```

intentionally used `[full-ci]` because TBSP-5 introduces a new durable persistence schema and is a logical campaign checkpoint.

The workflow actually selected the wider route rather than FAST-only validation:

```text
R2 main-thread boundary: GREEN
Android FAST step: skipped by full-ci routing
full Android unit tests + debug APK: GREEN
ASP contract tests / golden corpus: GREEN
Real Clingo 5.8.0 cross-validation: GREEN
CI gate: GREEN
```

Therefore TBSP-5 is accepted as COMPLETE.

The full-acceptance carrier is documentation-only; the latest TBSP-5 code/test checkpoint remains `3c960331...`.

## 11. Next slice — TBSP-6, NOT STARTED

The next campaign slice is:

```text
TBSP-6 — production cutover / restore ownership
```

TBSP-6 must integrate, tests-first:

```text
newClocktowerSeed()
-> load eligible TB rotation history
-> select curated preset exactly once
-> materialize deterministic deal exactly once
-> persist/restore committed initial setup provenance
-> commit PlayerCards before expensive recommendation work
-> enter PassPhone / RevealCard immediately
-> run remaining setup / first-night calculation in reveal window off main thread
-> preserve selector-owned Drunk shown identity as locked downstream fact
-> record completed setup into TB rotation history only at true game completion
```

TBSP-6 must also prove:

```text
Restart/archive before actual completion does not enter rotation history
restore does not reroll preset, Drunk shown role or seats
recomposition does not reroll
navigation before Start does not commit selection
invalid preset data never silently falls back to broad random TB setup
No Greater Joy remains unchanged
```

Do not begin A3 or No Greater Joy changes as part of this checkpoint. Keep PR #57 Draft and do not merge or mark Ready without explicit authorization.
