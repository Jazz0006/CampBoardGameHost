# D6.1b — Clocktower Session Core Progress

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/d6-root-reaudit`
> Draft PR: #113
> Status: **COMPLETE / GREEN — APP ROOT CUTOVER NOT STARTED**

## Goal

Make the existing `ClocktowerGameSession` capable of owning the production Clocktower identity/revision/semantic-history core for both Trouble Brewing and No Greater Joy without inventing a synthetic advanced `RulesetRef`.

## Tests-first evidence

RED commit:

```text
97ff03989c57bb6e5337380cf14d080164685b64
test: define D6.1b production session contract
```

The RED required typed APIs that did not yet exist:

- ruleset-independent production create/restore;
- explicit game-state revision advancement;
- non-mutating Global observation preflight;
- strict `GameSnapshot` projection only when a real `RulesetRef` is available.

Expected RED CI:

```text
CI 34197821003 — FAIL
Android FAST — FAIL in the test step
ASP / Real Clingo — not selected
```

GREEN production commit:

```text
793a3e44b96f996fdb20486895f01f6e2f4faf48
refactor: add production-compatible Clocktower session core
```

Follow-up compatibility/test commits:

```text
b96217eab573eec4342becf7be719cfd3f6c3fef
test: align no-op assertion with session core state

4373c0225deb733c305e233e07bf7078577de05d
test: cover production action timeline restore
```

Final automated evidence at `4373c022...`:

```text
CI 34198474844 — PASS
  :app:testFast — PASS
  full Android/debug APK — correctly skipped for this FAST checkpoint
  ASP — not selected
  Real Clingo — not selected
  CI gate — PASS

R2 34198474815 — PASS
```

## Exact GREEN diff

Relative to the post-RED/no-temporary-workflow checkpoint `6a957d1ee2462d6b36baa5a2c2a4417910976a87`, the complete D6.1b GREEN changes exactly two files:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionTest.kt
```

No `CampBoardGameHostApp.kt`, Recovery schema/planner/codec, `GameSnapshot.kt`, CI workflow or gameplay file was changed.

## Architecture established

### Ruleset-independent production core

`ClocktowerGameSession` now owns a ruleset-independent immutable state value containing:

- game ID;
- game seed;
- current script through `GameState.script`;
- game-state revision;
- player-input revision;
- decision/cross-game history;
- action timeline;
- epistemic observation log;
- semantic-history mode;
- next global timeline sequence.

No Greater Joy can therefore use the same session owner without a fake/synthetic `RulesetRef`.

### Strict snapshot remains strict

`GameSnapshot` itself remains unchanged and still requires a real `RulesetRef`.

A production session can create a strict `GameSnapshot` projection only when such a ref is actually available. Trouble Brewing compatibility remains intact; NGJ is not forced into fake advanced-ruleset metadata.

### Revision semantics

Two distinct operations are retained:

```text
updateGameState(nextState)
-> state-difference semantics
-> revision only when GameState actually differs

advanceGameStateRevision()
-> accepted production event/decision boundary
-> revision advances independently of GameState equality
```

This separation is required to preserve current production revision cadence during the later App-root cutover.

### Semantic chronology

Instance session APIs now cover:

- Global action commit;
- Global observation commit;
- non-mutating Global observation preflight;
- one shared monotonic global cursor;
- existing ID idempotency/fail-closed collision behavior.

The final restore test commits both an action fact and an observation before restore, proving their common global cursor and durable history survive `restoreProduction()` exactly.

### Compatibility path

The existing stateless companion transitions remain for the current App root until D6.1c production wiring is cut over. Their presence is temporary compatibility, not a second durable owner.

## Invariants preserved

- no synthetic No Greater Joy `RulesetRef`;
- `GameSnapshot` strict script/seed/ruleset invariants unchanged;
- Recovery v2 schema and planner untouched;
- semantic global cursor ordering unchanged;
- duplicate/conflicting action/observation semantics unchanged;
- preflight remains non-mutating;
- existing equality-based `updateGameState()` behavior retained;
- no Compose dependency added to session/domain code;
- no gameplay or user-visible behavior changed.

## D6.1c read-only anchor audit

Before closing D6.1b, the current App root was re-read to map the next production wiring slice.

The duplicated root authority remains concentrated around:

```text
currentClocktowerScript
clocktowerGameId
clocktowerGameSeed
clocktowerGameStateRevision
clocktowerPlayerInputRevision
clocktowerSemanticHistoryMode
clocktowerNextTimelineGlobalSequence
clocktowerActionTimeline
clocktowerEpistemicObservations
```

Key mutation/consumer anchors are:

- declarations + A4 invalidation helpers;
- `recordClocktowerAction()`;
- `recordEpistemicObservation()`;
- `preflightClocktowerPublicAliveObservation()`;
- `activeGameRecoverySnapshot()`;
- `applyValidatedRecoveryPlan()`;
- `resetDealState()`;
- A4 snapshot/prewarm/rebuild requests;
- `ClocktowerJudgeScreen` identity/revision inputs;
- night checkpoint/action-ID helpers.

## Important D6.1c constraint

Do not merely store a mutable `ClocktowerGameSession` inside `remember { mutableStateOf(...) }`: internal session mutations would not themselves trigger Compose recomposition.

The next slice must maintain this one-way direction:

```text
ClocktowerGameSession = writable transition authority
-> immutable/read-only observable session projection at App boundary
-> Compose / Recovery / A4 / recommendation consumers
```

The observable projection may be reassigned only from `session.state`; it must not become an independently writable second canonical authority.

Also, D6.1c is specifically the identity/revision/semantic-chronology cutover. Do not falsely claim complete `GameState`/mechanical ownership before the later GameState cutover is actually performed and validated.

## Next

Proceed to **D6.1c App-root authority cutover planning/characterization**, then perform a localized fail-closed giant-file patch only after the exact projection/mutation synchronization contract is explicit.

Do not merge PR #113 yet.
