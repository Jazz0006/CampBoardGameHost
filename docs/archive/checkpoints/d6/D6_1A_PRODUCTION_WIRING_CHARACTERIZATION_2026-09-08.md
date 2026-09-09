# D6.1a — Clocktower Production Wiring Characterization

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Baseline main: `2c495ee547e8327b0d3c3a811f5c891ba34fd863`
> D6 branch: `codex/d6-root-reaudit`
> Status: **D6.1a COMPLETE — PRODUCTION CODE UNCHANGED**

## Purpose

D6.0 selected the existing `ClocktowerGameSession` as the intended long-term Clocktower session owner. D6.1a characterizes the real post-PS5 production wiring before any ownership write.

This audit deliberately distinguishes three different things that were previously easy to conflate:

1. the production Clocktower session facts that must exist for every supported script;
2. the stricter `GameSnapshot` projection currently used by advanced ruleset/epistemic consumers;
3. App/Compose state required only for presentation, interaction drafts or navigation.

The result changes the exact D6.1 implementation sequence, but not the ownership-first goal established by D6.0.

---

## Executive conclusion

The strongest remaining ownership defect is confirmed: `CampBoardGameHostApp.kt` is still the writable production authority for Clocktower identity, revisions, semantic chronology and canonical game-state source data while `ClocktowerGameSession` is used mainly through temporary stateless transition helpers.

However, D6.1a found that a direct "instantiate `ClocktowerGameSession(GameSnapshot)` and replace all root fields" cutover is **not safe** for two reasons:

1. **Production session identity is broader than the current `GameSnapshot` contract.** No Greater Joy is a supported production/recovery script whose validated recovery runtime intentionally has `rulesetRef = null`, while `GameSnapshot` requires a non-null `RulesetRef`.
2. **Production revision semantics are broader than `ClocktowerGameSession.updateGameState()`.** App root explicitly advances `gameStateRevision` at accepted event/decision boundaries, including cases where the derived `GameState` may compare equal. Replacing that behavior with equality-based `updateGameState()` would silently alter recommendation/A4 keys and recovery-visible revision cadence.

Therefore D6.1 must not create a Trouble-Brewing-only live owner, must not invent a synthetic No Greater Joy `RulesetRef`, and must not change revision cadence as a side effect of decomposition.

The corrected route is:

```text
D6.1a production wiring characterization COMPLETE
-> D6.1b adapt the existing ClocktowerGameSession core authority to represent production session identity/history without requiring an advanced RulesetRef
-> D6.1c cut over identity + revisions + semantic chronology through one observable session authority while preserving existing revision cadence
-> D6.1d cut over canonical GameState projection/mutation where behavior equivalence is proven
-> D6.1e Recovery/A4/recommendation projection cleanup and acceptance
```

`GameSnapshot` remains a strict projection for consumers that have a resolved `RulesetRef`; it should not be made the reason No Greater Joy needs a fake ruleset reference.

---

## 1. Current production ownership map

### 1.1 App-root writable session identity

`CampBoardGameHostApp.kt` directly owns:

- `currentClocktowerScript`;
- `clocktowerGameId`;
- `clocktowerGameSeed`;
- `clocktowerRulesetRef` (nullable in production);
- `clocktowerRulesetRoleIds`.

`resetDealState()` is the main new-game lifetime boundary. For Clocktower it creates a new game ID and seed, resets both revisions and semantic history, switches semantic mode to `GLOBAL_V1`, and resolves a Trouble Brewing ruleset reference when applicable.

For No Greater Joy it deliberately keeps `clocktowerRulesetRef = null`.

### 1.2 App-root writable revisions

The root directly owns:

- `clocktowerGameStateRevision`;
- `clocktowerPlayerInputRevision`.

The local helpers:

```text
advanceClocktowerGameStateRevision()
advanceClocktowerPlayerInputRevision()
```

both increment the root counter and invalidate the current A4 revision scope.

These counters are used by:

- `ClocktowerJudgeScreen` inputs;
- recommendation/dynamic decision keys;
- A4 prewarm/rebuild request identity;
- recovery history;
- action IDs/debug state;
- night checkpoints.

### 1.3 App-root writable semantic chronology

The root directly owns:

- `clocktowerSemanticHistoryMode`;
- `clocktowerActionTimeline`;
- `clocktowerEpistemicObservations`;
- `clocktowerNextTimelineGlobalSequence`.

For `GLOBAL_V1`, root functions delegate transition legality/idempotency to the stateless companion APIs:

```text
ClocktowerGameSession.commitGlobalActionFact(...)
ClocktowerGameSession.commitGlobalEpistemicObservation(...)
```

and then copy the returned timeline/log/cursor/revision back into root state.

This proves that semantic transition behavior is already session-owned conceptually, but storage/mutation authority remains duplicated in App root.

### 1.4 Canonical `GameState` is derived, not currently stored as one root object

Production `GameState` is repeatedly reconstructed from `cards` plus current mechanical state using `toClocktowerGameState(...)`.

Therefore the current production authority is not a single `GameState` variable; it is the combination of:

- `cards` actual role/alive state;
- script + seed;
- confirmed poison/mechanical facts required by the adapter;
- explicit revision counters.

This matters because moving only a derived `GameState` object would not remove the real mutation authority from root.

---

## 2. Creation/start wiring

### Trouble Brewing

The production route is:

```text
startTroubleBrewingGame()
-> prepare committed cards/setup
-> committed-deal callback
-> resetDealState(Clocktower, TroubleBrewing, preparedSeed)
-> assign committed setup/rotation bookkeeping
-> persist recovery
-> prewarm first-night recommendation
```

At `resetDealState()` the root establishes the Clocktower session lifetime:

- new `gameId`;
- fixed game seed;
- revisions = 0;
- semantic mode = `GLOBAL_V1`;
- empty action/observation history;
- cursor = 0;
- Trouble Brewing ruleset basis/ref.

### No Greater Joy

The production route is:

```text
startClocktowerGame()
-> prepare assignments/cards
-> optional setup recommendation
-> commit cards
-> resetDealState(Clocktower, NoGreaterJoy, preparedSeed)
```

It establishes the same session identity/revision/history facts but intentionally leaves the advanced `clocktowerRulesetRef` null.

### New-game invariant

A D6 live session owner must exist no later than the point at which `resetDealState()` has committed the new Clocktower identity and before recovery/A4/recommendation consumers observe canonical session state.

It must be replaced atomically on every Clocktower quick restart/new game and cleared at an end/archive boundary before a stale session can be observed by a later game.

---

## 3. Recovery build wiring

`activeGameRecoverySnapshot()` constructs `ClocktowerRecovery` from App-root state.

### Recovery identity

```text
script
gameId
gameSeed
```

### Recovery history

```text
gameStateRevision
playerInputRevision
semanticHistoryMode
actionTimeline
nextTimelineGlobalSequence
events
epistemicObservations
```

Recovery intentionally persists cards/mechanics separately rather than serializing `GameSnapshot.gameState`.

This is a useful architecture constraint, not a defect: Recovery is a current-version emergency continuity contract and should remain independent from the internal session projection model.

D6 must therefore make Recovery *read* canonical identity/history from the session owner without changing Recovery v2 schema.

---

## 4. Recovery restore wiring

`applyValidatedRecoveryPlan()` currently resets all root Clocktower state, then for `ClocktowerRecovery` writes the restored identity/history fields back into root one by one:

```text
currentClocktowerScript
clocktowerGameId
clocktowerGameSeed
clocktowerGameStateRevision
clocktowerPlayerInputRevision
clocktowerSemanticHistoryMode
clocktowerNextTimelineGlobalSequence
clocktowerRulesetRef
clocktowerEpistemicObservations
clocktowerActionTimeline
```

and separately restores cards, phase and durable mechanics.

No live `ClocktowerGameSession` instance is currently restored.

This is the clearest production-wiring gap exposed by D6.1a: recovery validation proves the state is coherent, but ownership returns to App-root parallel fields rather than reconstructing the intended session authority.

### No Greater Joy contract

`RecoveryRestorePlanner` deliberately resolves:

```text
TroubleBrewing -> current RulesetRef or reject
NoGreaterJoy   -> null
```

and `RecoveryRestorePlannerNoGreaterJoyTest` explicitly protects successful NGJ recovery with a null ruleset ref.

D6.1 must preserve this. A fake/synthetic ref is not an acceptable decomposition shortcut.

---

## 5. A4 consumer wiring

A4 is a derived consumer, not an owner.

The App currently reconstructs temporary `GameSnapshot` values in:

- initial identity prewarm;
- observation-cache rebuild.

Those snapshots use root identity/revisions/derived `GameState` and a resolved `rulesetRef`; the epistemic observation log is also supplied separately to knowledge/rebuild components.

A4 effects are keyed by root game ID/revisions/ruleset identity and rely on explicit invalidation when either revision advances.

### A4 invariants for D6

1. session cutover cannot change the revision values that key A4 work;
2. every accepted revision change must still invalidate superseded A4 work;
3. observation rebuild remains gated behind successful Recovery persistence;
4. session end/restart/recovery still crosses the A4 session boundary;
5. A4 must consume an observable projection of session state rather than reach into hidden mutable session internals.

---

## 6. Recommendation / judge consumer wiring

`ClocktowerJudgeScreen` currently receives identity/revisions/ruleset and many mechanical fields as individual arguments.

Draft callbacks generally increment `playerInputRevision`; confirmed mechanical/event boundaries generally increment `gameStateRevision`.

Recommendation paths use the same revision values as cache/request identity.

D6.1 does **not** attempt to collapse the giant Judge callback surface yet. The safe first cut is to change where the canonical identity/revision/history values come from while preserving the existing callback behavior and values exactly.

The callback surface itself remains D6.3 work after session authority is stable.

---

## 7. Revision-semantics characterization

This is the most important behavioral constraint discovered by D6.1a.

### Current instance API semantics

`ClocktowerGameSession.updateGameState(nextState)` increments `gameStateRevision` only when `nextState != snapshot.gameState`.

### Current production semantics

App root explicitly advances `gameStateRevision` at accepted transition/event boundaries. Examples include:

- phase/decision-window closure;
- event recording through `addClocktowerEvent()`;
- role/shown-role changes;
- execution/day confirmation;
- night/dawn resolution;
- one-shot role usage and other committed facts.

Some flows can increment the revision more than once during one user action because multiple accepted durable/event transitions occur.

### D6 invariant

D6 is behavior-preserving. It must preserve **the current revision cadence**, not merely the weaker property that revisions remain monotonic.

Therefore the first cutover cannot replace every existing `advanceClocktowerGameStateRevision()` with equality-based `updateGameState()`.

The existing session owner needs a production-compatible explicit revision transition before App-root counters can be retired.

Similarly, player-input revision increments must preserve current no-op/idempotency behavior, especially recommendation application and duplicate epistemic-observation commits.

---

## 8. Semantic-history characterization

### Action facts

`recordClocktowerAction()` already delegates global action commit/idempotency/cursor allocation to `ClocktowerGameSession.commitGlobalActionFact(...)` and copies the result back to root.

### Epistemic observations

`recordEpistemicObservation()`:

- LegacyLocal: appends locally and increments player-input revision;
- GlobalV1: delegates atomic log/cursor/revision transition to the session companion API.

### Preflight special case

`preflightClocktowerPublicAliveObservation()` intentionally performs a **non-mutating validation/preflight** by calling the stateless transition and checking that the proposed record would be new.

A live session cutover must retain a non-mutating preflight path. Replacing this call with the mutating instance `commitGlobalEpistemicObservation()` would commit an observation too early and is forbidden.

### Required semantic invariants

- one game-wide monotonic cursor across action and observation points;
- existing IDs are idempotent only when content matches;
- conflicting reuse fails closed;
- preflight does not mutate session state;
- no hidden storyteller target is added to player-visible history;
- A4 durability gate is marked only after the same accepted observation transition as today.

---

## 9. `RulesetRef` model mismatch

`GameSnapshot` currently requires a non-null `RulesetRef` and requires that its script ID match `GameState.script`.

Production reality is different:

- built-in catalog supports both Trouble Brewing and No Greater Joy as validated scripts;
- Recovery intentionally resolves a persistence/advanced `RulesetRef` only for Trouble Brewing;
- No Greater Joy recovery is explicitly valid with `rulesetRef = null`.

`ClocktowerGameSession` companion comments already acknowledge this: its temporary stateless production adapter avoids requiring a synthetic `RulesetRef` for scripts whose advanced ruleset is not loaded.

### Decision

Do **not**:

- create a synthetic/fake NGJ `RulesetRef`;
- make only Trouble Brewing session-owned while NGJ remains App-owned;
- weaken Recovery validation to accommodate the refactor;
- change Recovery v2 payload to persist a new ruleset field.

Instead, D6.1b should adapt the existing `ClocktowerGameSession` so its **core production authority** can exist for every production Clocktower script without an advanced ruleset reference.

A strict `GameSnapshot` should remain available as a projection when a resolved `RulesetRef` exists and a consumer such as current A4 requires it.

The exact implementation should prefer changing `ClocktowerGameSession` internals/API over introducing a parallel generic manager or second independently writable canonical owner.

---

## 10. Compose observability constraint

`ClocktowerGameSession.snapshot` is currently an ordinary mutable Kotlin property inside a plain class.

Holding only a `ClocktowerGameSession` reference in `remember { mutableStateOf(session) }` would **not** cause Compose recomposition when the internal snapshot changes, because the object identity does not change.

A correct production cutover therefore needs a one-way observable projection at the App boundary.

Acceptable architecture:

```text
ClocktowerGameSession = sole writable authority
-> mutation returns/current state projection
-> App stores/observes read-only projection for Compose keys/rendering
-> projection is never independently mutated
```

Unacceptable architecture:

```text
ClocktowerGameSession writable state
<-> App writable duplicate state
```

The implementation must make the one-way direction obvious and test/audit that no second mutation authority remains.

Do not make the domain/session owner depend on Compose merely to solve observation.

---

## 11. Existing test evidence

### Session owner evidence already present

- `ClocktowerGameSessionTest`
  - independent game/input revision behavior;
  - no-op equal `GameState` update;
  - seed invariant;
  - restore behavior;
  - LegacyLocal observation handling.
- `ClocktowerGlobalObservationCommitTest`
- `ClocktowerHistoricalActionObservationCaptureTest`
- `ClocktowerTimelineSequenceAllocatorTest`
- other session/history tests.

### Recovery evidence already present

- `RecoveryRestorePlannerTest`;
- `RecoveryRestorePlannerNoGreaterJoyTest`;
- `RecoverySnapshotJsonCodecTest`;
- `ClocktowerSemanticHistoryPersistenceTest`;
- action-timeline persistence tests;
- write-gate/lifecycle tests.

### Real uncovered stable invariant

There is no current typed contract proving that the **production session owner can represent both supported scripts while preserving the current optional-ruleset production contract and exact restored identity/revisions/history**.

That is a real D6.1b RED target.

Do not add a source-string/Compose-internals test to prove it. The RED belongs at the pure `ClocktowerGameSession` production-authority API boundary.

---

## 12. D6.1b tests-first contract

Before App-root production wiring changes, add the smallest typed RED(s) against `ClocktowerGameSession` that establish:

1. a production session can be created/restored for No Greater Joy without a synthetic advanced `RulesetRef`;
2. identity (`gameId`, script, seed), game/player revisions, semantic mode, action timeline, observation log and global cursor survive restore exactly;
3. explicit production game-state revision advancement preserves the historical revision cadence independently of `GameState` equality;
4. existing `updateGameState()` equality semantics remain available where that operation is actually intended;
5. non-mutating global-observation preflight remains possible without changing the session state;
6. when a resolved `RulesetRef` is available, a strict `GameSnapshot` projection keeps the existing seed/script/ruleset invariants.

If implementing these invariants requires broad changes outside the session/domain boundary, stop and re-audit rather than expanding D6 silently.

---

## 13. Corrected D6.1 extraction order

### D6.1b — session core compatibility

Goal: make the **existing** `ClocktowerGameSession` capable of owning the common production session identity/revision/history state for both TB and NGJ without a synthetic ruleset ref.

Expected files:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt
possibly one tightly related domain projection type only if unavoidable
app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionTest.kt
possibly one dedicated typed session-production-contract test
```

No App-root edit yet if the session API can be established and validated independently.

### D6.1c — App-root authority cutover: identity/revisions/chronology

Goal: create/restore/end one session owner and route identity/revision/action/observation/cursor writes through it.

Preserve App Compose observability through a read-only projection; remove corresponding writable root variables in the same atomic GREEN slice where practical.

Expected major file:

```text
CampBoardGameHostApp.kt
```

Use the large-file fail-closed patch workflow.

### D6.1d — canonical `GameState` projection cutover

Only after revision semantics are already session-owned and characterized, route canonical `GameState` updates/projections through the session where equivalence is proven.

Do not rewrite all Clocktower mechanics in this slice.

### D6.1e — Recovery/A4/recommendation cleanup + acceptance

- Recovery reads identity/history from session authority and restore constructs it before consumers run;
- A4 consumes strict snapshot/projection without becoming owner;
- recommendation/Judge receive session-derived revisions;
- remove obsolete compatibility copies/stateless production usage when no longer needed;
- run acceptance cadence.

---

## 14. D6.1b changed-file allowlist

Initial allowlist:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionTest.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/<one new typed production-contract test>.kt   # only if clearer than extending the existing test
docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md
```

Do not modify `CampBoardGameHostApp.kt` in the first D6.1b session-API GREEN unless a compile-only adapter is unavoidable. Prefer proving the owner API before touching the 238 KB root.

Do not modify Recovery schema/codec/planner in D6.1b.

---

## 15. Validation cadence

D6.1b:

```text
typed RED at ClocktowerGameSession production contract
-> focused session tests
-> GREEN session API
-> focused session/history tests
-> :app:testFast
-> git diff --check + exact changed-file audit
```

D6.1c/d:

```text
existing owner tests + any stable wiring characterization
-> localized fail-closed App patch
-> focused session/recovery/A4 evidence
-> :app:testFast
-> affected T2
-> exact ownership audit
```

D6.1 acceptance:

```text
one reserved [full-ci] T4
+ R2 only if the final structural/main-thread risk selects it
```

Real Clingo remains unselected because D6.1 must not change epistemic semantics.

---

## 16. Fail-closed acceptance conditions

D6.1 is not complete merely because `CampBoardGameHostApp.kt` becomes shorter.

It is complete only when:

1. there is one writable authority for the selected Clocktower session identity/revision/history state;
2. both Trouble Brewing and No Greater Joy use the same authority model;
3. NGJ still does not require a synthetic advanced `RulesetRef`;
4. revision values/cadence are behaviorally unchanged;
5. semantic action/observation ordering/idempotency is unchanged;
6. Recovery v2 schema and current-version-only contract are unchanged;
7. A4 invalidation/durability ordering is unchanged;
8. Compose observes session-derived values without owning a second mutable copy;
9. session end/restart/recovery cannot leak stale state;
10. no gameplay semantics change is introduced.

---

## Final recommendation

Proceed to **D6.1b session-core compatibility RED** before any `CampBoardGameHostApp.kt` production edit.

The first RED should prove the common production session contract across Trouble Brewing and No Greater Joy, especially optional advanced-ruleset identity, restored revisions/history and explicit revision advancement. Once that boundary is GREEN, the large App-root cutover becomes a wiring refactor rather than a simultaneous domain-model redesign.
