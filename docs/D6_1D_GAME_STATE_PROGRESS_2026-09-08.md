# D6.1d Canonical GameState Ownership Progress

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/d6-root-reaudit`
> Draft PR: #113 — do not merge yet
> Status: **IN PROGRESS — role identity ownership slice complete; poison cadence characterization next**

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

## Remaining canonical GameState fields

After role identity, the remaining dynamic per-seat mechanical fields are primarily:

- `alive` — currently derived from `PlayerCard.eliminatedRound`;
- `poisoned` — currently derived from `clocktowerConfirmedPoisonTarget` plus alive state.

Seat/name, script and seed are stable/session identity rather than the next dynamic cutover target.

## Important revision-cadence finding

Neither remaining field can blindly reuse “field mutation = +1 revision”.

### Alive

Alive/death materialization is distributed across execution, Slayer, Virgin and dawn/night death flows. Several of these mutations occur inside a broader accepted day/night revision boundary rather than owning a separate revision. It also interacts with public observations, semantic action facts, Scarlet Woman succession and game-outcome evaluation.

Therefore alive is not the next smallest safe slice.

### Poisoned

Poison ownership is more concentrated, but it still has two revision shapes:

1. some poison confirmations explicitly own/trigger a game-state revision;
2. Dusk expiry and some dawn materializations mutate poison state inside an already accepted broader day/night boundary and must **not** add a second revision.

Example: `materializeClocktowerPoisonExpiryAtDusk()` records the durable poison-expiry action and clears the current/confirmed poison target when materialization requires it, but does not itself call `advanceClocktowerGameStateRevision()`.

This means the next step must characterize and preserve both semantics before moving poison ownership.

## NEXT — poison cadence characterization

Before production edits, prove a narrow session contract capable of updating poison-owned `GameState` fields without forcing revision drift.

Required characterization questions:

1. which poison transitions own a new game-state revision;
2. which poison transitions occur inside an already-revisioned accepted boundary;
3. target switching/clearing must modify only `PlayerState.poisoned` values;
4. dead targets must not remain mechanically poisoned in the canonical projection;
5. role/alive/shown identity and unrelated seats must remain unchanged;
6. Recovery v2 and the existing durable poison action/materialization contracts remain unchanged.

Do not begin the alive/death cutover until poison is complete and the remaining writer topology is re-audited.

## Guardrails

- preserve exact revision count and ordering;
- do not change gameplay semantics;
- do not change Recovery v2 or persistence topology;
- do not expose session `GameState` as globally canonical until all remaining audited writers are moved;
- do not rewrite day/night mechanics wholesale;
- do not touch Undercover/Werewolf;
- do not merge PR #113.
