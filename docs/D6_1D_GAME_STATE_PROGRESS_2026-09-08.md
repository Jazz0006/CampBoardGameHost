# D6.1d Canonical GameState Ownership Progress

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/d6-root-reaudit`
> Draft PR: #113 — do not merge yet
> Status: **D6.1d COMPLETE — canonical dynamic GameState writer cutover + global ownership audit PASS — D6.1e acceptance next**

## Baseline

D6.1c already made `ClocktowerGameSession` the sole writable owner for identity, revisions, semantic history and global chronology. D6.1d is moving the remaining mechanical `GameState` ownership in cohesive pieces without replacing App-root `PlayerCard` presentation state wholesale.

`GameState` contains only script/seed plus per-seat domain mechanics (`actualRole`, alignment/type, `shownRole`, `alive`, `poisoned`). `PlayerCard` also carries presentation/localization and `eliminatedRound`, so `cards` cannot simply be replaced by session `GameState`.

## D6.1d foundation — atomic accepted GameState boundary COMPLETE

Typed RED:

```text
ec315ba7c8f23c04ac5764a216c0b502602319e9
CI 34207189842 — expected FAIL
```

The RED failed only because `commitGameStateBoundary` did not yet exist.

GREEN product:

```text
89f96bcf1403e43b0ed1da5908977ad7c3766f2e
feat: add atomic Clocktower game-state boundary
```

The new API atomically replaces `GameState` and advances the accepted production `gameStateRevision` exactly once, including value-equal accepted boundaries. Existing equality-based `updateGameState()` semantics remain unchanged.

## D6.1d slice 1 — active-session role identity ownership COMPLETE

### Session-side typed RED/GREEN

Typed RED:

```text
86c9b5d338271af8eab874358226558a8d4e2cfa
CI 34210813585 — expected FAIL
```

The failure was limited to the intentionally missing APIs:

- `commitActualRoleBoundary`
- `commitShownRoleBoundary`

GREEN source checkpoint:

```text
9dc48b0975ea27ce54fa14413f3c9e7d874ac2a8
feat: add Clocktower role identity boundaries
CI 34211156595 — PASS
R2 34211156602 — PASS
```

The role-boundary tests prove:

- actual-role change modifies only target `actualRole` / alignment / character type;
- shown-role change modifies only target `shownRole`;
- alive/poisoned state remains unchanged;
- other seats remain unchanged;
- each accepted role boundary advances game-state revision exactly once.

### App-root production cutover

One-shot run:

```text
34211594069 — PASS
```

The run passed, in order:

1. fail-closed parent/blob verification;
2. bounded App patch;
3. exact product-diff / ownership audit;
4. focused GameState + role-identity session contracts;
5. `:app:testFast`;
6. product commit/push;
7. one-shot tooling cleanup.

Product checkpoint:

```text
6a1fdb6467f3810bab937953914ea9d7d2d1eb37
refactor: route Clocktower role identity through session
```

Cleanup head:

```text
aa7e828518fe877fc53f257a7fa044e6cb16944f
chore: remove D6.1d role-identity one-shot tooling
```

Production ownership is now:

```text
ClocktowerGameSession role identity = canonical writer
-> App-root PlayerCard role fields = presentation/read mirror
```

`setClocktowerActualRole` and `setClocktowerShownRole` now commit the session boundary first, publish the session view / invalidate A4 at the same revision boundary, and only then update the `PlayerCard` mirror. The previous direct `advanceClocktowerGameStateRevision()` calls inside those helpers are gone.

Remaining `clocktowerRole` / `clocktowerShownRole` assignments outside these active-session helpers are setup/restore construction or JSON materialization, not a parallel live-session writer. The Drunk setup-plan shown-role assignment occurs while building `committedCards` before the production session is created.

## D6.1d final cutover — COMPLETE

The remaining dynamic per-seat mechanics were completed after the role-identity slice:

- alive/death: ordinary execution, Virgin, Slayer and Dawn/night-death materialization all synchronize canonical session state before the `PlayerCard.eliminatedRound` mirror changes;
- poisoned: Poisoner confirmation owns a `+1` accepted session boundary, while ordinary Dawn, Dusk expiry and successor/retry convergence use `+0` synchronization inside an already-accepted revision;
- role identity: actual-role and shown-role changes remain session-first;
- session creation/restore remains exactly one live owner per active Clocktower game.

The important acceptance rule is **writer ownership**, not elimination of every `PlayerCard`-to-`GameState` projection. `cards` still contains UI/presentation fields such as localization and `eliminatedRound`, and pre-session/recommendation/screen-local consumers legitimately construct derived read projections. Those projections are not writable canonical authorities.

## Global ownership audit — PASS

Read-only full-checkout audit run:

```text
34221212685 — PASS
```

It proved the post-cutover topology:

```text
death session sync calls       4
poison +1 commit calls         1
poison +0 sync calls           3
actual-role session writers    1
shown-role session writers     1
production session create      1
production session restore     1
```

It also proved that from `main` the only changed production files are the App root plus the four Clocktower session/boundary files, and that Recovery, Clocktower rules, A4 epistemic production code and Werewolf production code remain unchanged. Combined D6.1d focused contracts and `:app:testFast` both passed in the same audit run.

## D6.1e compatibility/read-side audit — PASS

Read-only callsite audit run:

```text
34222474745 — PASS
```

Findings:

1. no production caller uses the stateless `ClocktowerGameSession.commitGlobalActionFact(...)` or `commitGlobalEpistemicObservation(...)` companion forms directly; the live App uses instance methods;
2. the stateless forms remain useful as pure transitions behind the instance API and deterministic replay/contract tests, so deleting them would not improve ownership;
3. `updateGameState()` has no production callsite but remains a deliberate equality-based session contract protected by tests;
4. `toGameSnapshot(...)` remains the strict ruleset-backed projection and is not a competing production owner;
5. `cards.toClocktowerGameState(...)` has many production read callsites across pre-session, recommendation and UI flows, so wholesale replacement would mix presentation concerns back into session ownership;
6. the only genuinely stale production items were two ownership comments in `ClocktowerGameSession.kt`; D6.1e updates those comments without changing runtime behavior.

## Final D6.1d ownership model

```text
ClocktowerGameSession
= canonical writable session + dynamic mechanical GameState authority

PlayerCard / App-root flow variables
= presentation and orchestration mirrors
= updated only after the relevant session boundary for canonical mechanics

Derived cards.toClocktowerGameState(...) values
= read/pre-session/recommendation adapters
= not writable session authority
```

## NEXT — D6.1e acceptance

No additional production refactor is selected before acceptance. The next step is one user-authored `[full-ci]` logical checkpoint, then record the T4 result and re-check PR #113 merge readiness. Do not merge automatically.
