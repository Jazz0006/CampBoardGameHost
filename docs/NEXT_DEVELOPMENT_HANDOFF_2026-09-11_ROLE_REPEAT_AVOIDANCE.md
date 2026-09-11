# NEXT DEVELOPMENT HANDOFF — ROLE-ROTATION-1 Recent Role Rotation

> Date: 2026-09-11 Australia/Sydney  
> Status: **CURRENT — implementation complete; final R6/T4 acceptance pending**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Baseline: PR #118 merged into `main` at `3cc3d64d303236ae84b7fb14eaf066cc10aec95e`  
> Active branch: `codex/role-rotation-1-consecutive-role-repeat-avoidance`  
> Draft PR: #119

## 1. Read first

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. live `main` and current branch/PR state.

Load older UI-NAV documents only if a regression points back to that campaign. Do not continue UI-NAV feature work from historical handoffs.

## 2. Product problem

Real play exposed an avoidable experience issue across repeated games: one human can receive the same role or the same high-impact category too often. Observed bad experiences include one player receiving Demon three times in five games and another repeatedly receiving Butler.

The desired behavior is **not** deterministic rotation and **not** a change to which roles belong in the setup. The goal is to preserve randomness while improving short-run player experience.

## 3. Final assignment policy

The assignment optimizer uses strict lexicographic priorities:

```text
Priority 1 — immediately previous game exact starting identity repeat
minimize same-player / same-shownRoleId repeats

Priority 2 — immediately previous game special character-category repeat
among Priority-1-optimal assignments, minimize:
DEMON    -> DEMON
MINION   -> MINION
OUTSIDER -> OUTSIDER

Do not penalize:
TOWNSFOLK -> TOWNSFOLK

Priority 3 — exact starting identity repeats from the two earlier games
among Priorities-1/2-optimal assignments, minimize weighted repeats:
two games ago   weight 2
three games ago weight 1

Priority 4 — special-category repeats from the same two earlier games
among Priorities-1/2/3-optimal assignments, minimize weighted DEMON/MINION/OUTSIDER repeats:
two games ago   weight 2
three games ago weight 1

Priority 5 — randomness
among assignments tied on Priorities 1–4, use deterministic seeded tie-breaking.
```

The behavioral objective is a lexicographic cost tuple. Older history therefore cannot accumulate enough penalty to outweigh an avoidable immediately-previous-game repeat.

The setup must always succeed. If a preferred zero-repeat permutation is impossible, accept the minimum-cost legal assignment. Rotation never becomes a setup legality rule.

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

ROLE-ROTATION-1 uses the three most recent completed games, newest first:

- immediately previous game = strong exact/category priorities;
- two games ago = weak history, weight 2;
- three games ago = weak history, weight 1;
- the projection crosses roster-size/player-count changes;
- matching is by stable player key, not seat;
- there is no wall-clock expiry in v1.

A legacy or otherwise valid record with no player-starting-identity facts still occupies its chronological slot. Do not skip it and promote an older game into a stronger recency tier.

This is recent-session fairness, not permanent lifetime exclusion.

## 6. Ownership and architecture

### 6.1 Role-set owner

`TroubleBrewingProductionSetupPreparer` and `SetupDiversitySelector` choose the legal setup/preset. This ownership remains unchanged.

`SetupDiversityHistory` tracks real setup composition and intentionally excludes shown identity. It is not repurposed as player-role rotation history.

### 6.2 Player-to-role assignment owner

`TroubleBrewingSetupDealPlanner` maps the already-selected role tokens onto confirmed ordered players. Each token preserves:

```text
(actualRoleId, shownRoleId, actualRoleCategory)
```

The DP assignment state remains seat-assignment-only; considering three history games adds cost dimensions, not a larger assignment state space.

### 6.3 Randomness seam

ROLE-ROTATION-1 reuses `gameSeed` with the independent deterministic namespace:

`tb-role-rotation-v1`

When there is no usable player-rotation history, the exact legacy `tb-seat-v1` assignment path remains unchanged.

### 6.4 Durable history owner

Reuse the existing lifecycle:

- `TroubleBrewingSetupCompletionPersistence`
- `TroubleBrewingSetupRotationHistoryStore`

The final starting mapping is frozen from `TroubleBrewingPreparedSetup` through `TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup(...)`, before gameplay can mutate role state. Do not reconstruct starting identity from end-of-game `PlayerCard` state.

### 6.5 Stable human-player key

ROLE-ROTATION-1 uses:

```text
playerKey = exact trimmed confirmed playerName
```

Seat number is deliberately not part of player identity.

Accepted limitations for v1:

- rename breaks continuity;
- case changes are different keys;
- reusing the same placeholder name for a different human cannot be detected.

Do not introduce a broad player-profile/account system solely for those limitations.

## 7. Persistence and projection — implemented

`TroubleBrewingSetupRotationRecord` carries typed `playerStartingIdentities` containing:

- `playerKey`;
- `actualRoleId`;
- `shownRoleId`;
- `actualRoleCategory`.

Both completion persistence and completed-game rotation-history persistence are schema v2:

- v2 round-trips the typed identities;
- v1 decodes with an empty player-identity list;
- the existing SharedPreferences storage key remains unchanged;
- existing setup-diversity history remains available after migration.

The history store exposes two separate projections:

```text
historyFor(datasetId, schemaVersion, playerCount)
    -> setup-composition diversity, still player-count scoped

recentPlayerStartingIdentityHistoryFor(datasetId, schemaVersion)
    -> newest-first recent three completed games, cross-player-count
```

This separation is intentional: storage owns historical facts; `TroubleBrewingSetupDealPlanner` owns recency policy and weighting.

## 8. Tests-first evidence — completed

Meaningful typed RED/GREEN slices were used for:

- exact shown-identity avoidance;
- DEMON/MINION/OUTSIDER category avoidance;
- Drunk shown-versus-actual semantics;
- two-games-ago versus three-games-ago `2:1` weak-history weighting;
- non-consecutive Demon avoidance;
- durable starting identities from final prepared setup;
- completion persistence v2/v1 compatibility;
- rotation-history persistence v2/v1 compatibility;
- three-game history projection across player counts;
- production-preparer history propagation.

Intermediate GREEN checkpoints passed repeated forced `:app:testFast` runs.

## 9. Production App wiring — completed

`CampBoardGameHostApp.kt` now:

1. creates one `TroubleBrewingSetupRotationHistoryStore`;
2. reads the existing player-count-scoped setup-diversity history;
3. reads the separate recent three-game player-starting-identity history;
4. passes both into `TroubleBrewingProductionSetupPreparer`;
5. freezes the completion fact through `TroubleBrewingSetupRotationRecordFactory.fromPreparedSetup(preparedSetup)`.

Because `CampBoardGameHostApp.kt` is a large file, the change used the repository-approved `AGENTS.md` Path B workflow: a temporary GitHub Actions one-shot plus a separate exact Python patch script.

Evidence:

```text
pre-wiring GREEN checkpoint:
50254b45f73b4985e62ff3da78177b869f56204d

product App-wiring checkpoint:
e2ab42f6159166e56520e13a790c5af3caad124a

one-shot cleanup checkpoint:
df50b64d7087ee7a82efc5288fab67e3a018a212

one-shot run:
34555558005 — SUCCESS

inside one-shot:
bootstrap/head/blob guards       PASS
exact Python patch               PASS
focused setup/factory tests      PASS
forced :app:testFast             PASS
exact diff audit                 PASS
product commit/push              PASS
temporary workflow/script cleanup PASS
```

Net diff from `50254b45...` through cleanup contains only `CampBoardGameHostApp.kt` with the intended wiring change; temporary infrastructure does not remain in the branch.

## 10. Current execution point — R6 acceptance

R1–R5 are complete. Remaining work is R6 only:

```text
1. execute final T2/T4 acceptance according to docs/TESTING_STRATEGY.md;
2. require the explicit [full-ci] checkpoint to pass;
3. update roadmap/handoff with the final accepted checkpoint and checks;
4. keep PR #119 draft until acceptance evidence is complete;
5. do not merge PR #119 without explicit user authorization.
```

The cleanup commit generated by GitHub Actions may show `action_required` on ordinary PR workflows with zero jobs because the commit actor is `github-actions[bot]`; that is not a test failure. The user-authored `[full-ci]` checkpoint below is the authoritative final acceptance trigger.

## 11. Architecture fence

Preserve current owners. Do not introduce:

- a second setup coordinator;
- deterministic round-robin dealing;
- changes to role-count legality or script setup rules;
- a new global player database/profile system;
- wall-clock expiry in this slice;
- EPI-MQ/recommendation behavior changes;
- UI-NAV changes;
- broad persistence/recovery redesign.

## 12. Final acceptance target

ROLE-ROTATION-1 is complete only when the final acceptance gate proves the implementation while preserving these invariants:

```text
selected role multiset is unchanged
immediately previous exact shown-role repeats are minimized first
immediately previous DEMON/MINION/OUTSIDER repeats are minimized second
older exact repeats use recency weight 2:1 as the third priority
older special-category repeats use recency weight 2:1 as the fourth priority
TOWNSFOLK category repetition is neutral
Drunk semantics use shown identity + actual category correctly
equal-optimal assignments remain seeded/randomized
seat reorder and roster/player-count changes are safe
legacy/no-history path remains backward compatible
restart/completion persistence preserves starting identity history
setup never fails solely because of rotation preferences
```

## 13. UI-NAV-1 inherited status

UI-NAV-1 is closed for feature development. PR #118 received real-device acceptance after the identity-controller width and night-flow bottom-navigation follow-up fixes. Do not reopen that campaign while implementing ROLE-ROTATION-1 unless an actual regression is reproduced.
