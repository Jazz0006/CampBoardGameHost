# NEXT DEVELOPMENT HANDOFF — ROLE-ROTATION-1 Consecutive Role Repeat Avoidance

> Date: 2026-09-11 Australia/Sydney  
> Status: **CURRENT — ownership/history audit complete; implementation authorized**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Baseline: PR #118 merged into `main` at `3cc3d64d303236ae84b7fb14eaf066cc10aec95e`  
> Active branch: `codex/role-rotation-1-consecutive-role-repeat-avoidance`

## 1. Read first

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. live `main` and current branch/PR state.

Load older UI-NAV documents only if a regression points back to that campaign. Do not continue UI-NAV feature work from historical handoffs.

## 2. Product problem

Real play has exposed an avoidable experience issue: when the same group plays repeatedly, a player can randomly receive the same role/identity again in the next game. A second, weaker experience issue is receiving the same special character category again even when the exact role changes.

The desired behavior is **not** deterministic rotation and **not** a change to which roles belong in the setup. The goal is to preserve randomness while improving short-run player experience.

## 3. Agreed assignment policy

The assignment optimizer must use strict lexicographic priorities:

```text
Priority 1 — exact starting identity repeat
minimize same-player / same-shownRoleId repeats

Priority 2 — special character-category repeat
among Priority-1-optimal assignments, minimize:
DEMON    -> DEMON
MINION   -> MINION
OUTSIDER -> OUTSIDER

Do not penalize:
TOWNSFOLK -> TOWNSFOLK

Priority 3 — randomness
among assignments tied on Priorities 1 and 2, use deterministic seeded tie-breaking from the existing gameSeed with a new independent namespace.
```

Do not encode this as arbitrary weighted penalties where multiple category improvements could outweigh one avoidable exact identity repeat.

The setup must always succeed. If zero exact repeats are impossible, minimize them. If a special-category repeat is unavoidable after the exact-repeat optimum is fixed, accept it.

## 4. Identity semantics

Two different identity concepts are intentionally used:

- **Exact repeat** compares player-visible **starting shown identity** (`shownRoleId`).
- **Category repeat** compares the **actual role character type/category**.

This preserves Drunk semantics correctly. Example:

```text
previous: actual Drunk / shown Investigator
current:  actual Investigator / shown Investigator
```

This is an exact identity repeat, but it is OUTSIDER -> TOWNSFOLK and therefore not a category repeat.

Do not leak hidden truth into player-facing state and do not rewrite Drunk/registration/gameplay semantics.

## 5. History horizon

The product goal is recent repeated-play experience, not lifetime exclusion.

For ROLE-ROTATION-1, keep the implementation simple:

- reuse bounded recent completed-game history;
- do not add wall-clock expiry or time decay;
- it is acceptable if an older game still participates while it remains in the bounded retained history;
- a future time-window rule can be added only if real play shows it is needed.

The existing Trouble Brewing history already retains only a small number of recent completed games. Do not broaden retention for this feature.

## 6. Completed ownership/history audit

### 6.1 Role-set owner

`TroubleBrewingProductionSetupPreparer` and `SetupDiversitySelector` choose the legal setup/preset. This ownership must remain unchanged.

`SetupDiversityHistory` tracks real setup composition and intentionally excludes shown identity. Do not repurpose it as player-role rotation history.

### 6.2 Player-to-role assignment owner

`TroubleBrewingSetupDealPlanner` maps the already-selected role tokens onto confirmed ordered players. This is the smallest and correct seam for the soft rotation objective.

Treat each current role token as an immutable pair:

```text
(actualRoleId, shownRoleId)
```

Role-set selection, shown-identity commitment, and setup legality all happen before or outside the assignment optimization.

### 6.3 Randomness seam

The root setup obtains `gameSeed` from the existing random seed source. Downstream setup decisions use deterministic namespaced MurmurHash derivations.

ROLE-ROTATION-1 must reuse the same `gameSeed` and add an independent namespace such as:

`tb-role-rotation-v1`

Do not introduce an unseeded `Random` source.

When there is no usable player-rotation history, preserve the exact legacy `tb-seat-v1` assignment path so first-run/legacy behavior remains unchanged.

### 6.4 Durable history owner

Reuse the existing Trouble Brewing completion/rotation lifecycle:

- `TroubleBrewingSetupCompletionPersistence`
- `TroubleBrewingSetupRotationHistoryStore`

The current rotation record stores setup composition and Drunk shown-role metadata but not player-to-starting-identity mapping. Add only the smallest typed durable player-starting fact required by this feature.

Do not use the generic game archive as the authoritative source for rotation because live shown identity can mutate after setup.

The starting mapping must be frozen from the committed deal/setup, not reconstructed at game completion from mutable session state.

### 6.5 Stable human-player key

There is no durable UUID/player-account identity today. Confirmed seating uses unique trimmed player names and seats are not stable across games.

For ROLE-ROTATION-1 use:

```text
playerKey = exact trimmed confirmed playerName
```

This tracks a human across seat reorder and roster-size changes when the same name is reused.

Known limitations are accepted for this scope:

- rename breaks continuity;
- case changes are different keys;
- reusing one placeholder name for a different human cannot be detected.

Do not introduce a broad player-profile/account system solely to solve those limitations.

## 7. Persistence shape and projection

Add a small typed record representing the frozen starting player experience, for example:

```text
TroubleBrewingPlayerStartingIdentity(
    playerKey,
    actualRoleId or actualCategory,
    shownRoleId,
)
```

Store enough actual information to recover the special category without depending on mutable or future script metadata ambiguity. Keep the schema narrow.

Persist this through the existing setup-completion fact and then the existing completed-game rotation history store.

Version the affected persisted schemas and support legacy v1 data:

- v1 decodes with no player-rotation facts;
- v2 carries the new typed mappings;
- unsupported/corrupt semantics continue to follow existing fail-safe rules.

Keep the current setup-diversity query filtered by player count. Add a separate player-rotation projection that may inspect recent completed games across player-count changes and intersects records with the current roster by `playerKey`.

For the first implementation, use the most recent usable previous identity for a matched player within the bounded recent history. Do not add elapsed-time expiry.

## 8. Tests-first execution order

### R1 — exact shown-identity DealPlanner RED/GREEN

First genuine typed RED must prove:

1. previous player A shown identity X;
2. current selected role tokens contain X and a zero-repeat legal permutation exists;
3. assignment does not give shown X to A;
4. exact selected actual role multiset remains unchanged.

Also prove unavoidable repeats are minimized and setup still succeeds.

### R2 — special category objective

Add typed cases proving, after Priority 1 is held optimal:

- DEMON -> DEMON is avoided when possible;
- MINION -> MINION is avoided when possible;
- OUTSIDER -> OUTSIDER is avoided when possible;
- TOWNSFOLK -> TOWNSFOLK is not penalized;
- exact shown-identity avoidance always outranks category avoidance;
- Drunk compares shown identity for Priority 1 and actual category for Priority 2.

### R3 — durable starting facts

Freeze player starting identity/category from the final deal/committed setup and add record-factory characterization.

### R4 — persistence migration

Add RED/GREEN for:

- v2 roundtrip;
- v1 decode -> empty player-rotation facts;
- restart preserves starting facts;
- completion idempotence remains unchanged;
- corrupt/unsupported behavior remains explicit/fail-safe as already contracted.

### R5 — production wiring

Feed the recent completed-game player rotation projection into `TroubleBrewingProductionSetupPreparer` / `TroubleBrewingSetupDealPlanner` without changing setup-diversity selection.

Prove:

- seat reorder follows player key, not seat;
- roster add/remove remains safe;
- player-count change can still match retained players;
- no usable history preserves exact legacy assignment behavior.

### R6 — validation and closeout

Run focused T0 throughout, then T1 `:app:testFast`, persistence/history affected T2, and the required final broader gate according to `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Do not use manufactured source-string tests.

## 9. Architecture fence

Preserve current owners. Do not introduce:

- a second setup coordinator;
- deterministic round-robin dealing;
- changes to role-count legality or script setup rules;
- a new global player database/profile system;
- wall-clock expiry/history-decay logic in this slice;
- EPI-MQ/recommendation behavior changes;
- UI-NAV changes;
- broad persistence/recovery redesign.

## 10. Final acceptance

ROLE-ROTATION-1 is complete only when:

```text
selected role multiset is unchanged
exact shown-role repeats are minimized first
DEMON/MINION/OUTSIDER category repeats are minimized second
TOWNSFOLK category repetition is neutral
Drunk semantics use shown identity + actual category correctly
equal-optimal assignments remain seeded/randomized
seat reorder and roster changes are safe
legacy/no-history path remains backward compatible
restart preserves the intended recent rotation history
setup never fails solely because of rotation preferences
```

## 11. UI-NAV-1 inherited status

UI-NAV-1 is closed for feature development. PR #118 received real-device acceptance after the identity-controller width and night-flow bottom-navigation follow-up fixes. Do not reopen that campaign while implementing ROLE-ROTATION-1 unless an actual regression is reproduced.
