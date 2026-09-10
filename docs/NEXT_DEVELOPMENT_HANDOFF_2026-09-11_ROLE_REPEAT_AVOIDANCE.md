# NEXT DEVELOPMENT HANDOFF — ROLE-ROTATION-1 Consecutive Role Repeat Avoidance

> Date: 2026-09-11 Australia/Sydney  
> Status: **NEXT — begin after PR #118 merge**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Current target after UI-NAV-1: reduce the chance that the same human player receives the same starting identity in consecutive games without changing legal setup composition.

## 1. Read first

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. live `main` and current branch/PR state.

Load older UI-NAV documents only if a regression points back to that campaign. Do not continue UI-NAV feature work from historical handoffs.

## 2. Product problem

Real play has exposed an avoidable experience issue: when the same group plays repeatedly, a player can randomly receive the same role/identity again in the next game.

The desired behavior is **not** deterministic rotation and **not** a change to which roles belong in the setup. The goal is to preserve randomness while avoiding an immediate same-player/same-identity repeat whenever a legal alternative assignment exists.

Baseline product rule:

```text
1. Determine the legal role set exactly as today.
2. Assign those roles to players.
3. Prefer assignments with zero consecutive same-player/same-identity repeats.
4. If zero is impossible, minimize the number of repeats.
5. Randomize among equally good assignments.
6. Never make setup impossible merely to satisfy anti-repeat preference.
```

This is a **soft assignment constraint**, not a new legality rule.

## 3. First task is read-only ownership/history audit

Do not implement immediately. First trace the current setup pipeline end to end and identify:

- who selects the role set;
- who maps selected roles onto players;
- what randomness source is used;
- whether any recent-setup / rotation history already exists;
- what that history actually stores: role-set history, player-to-role history, or both;
- how history is persisted/recovered and when it is committed;
- whether a stable human-player identifier exists independently of seat number;
- how common/saved player names participate in identity;
- what happens when players join, leave, reorder seats, or duplicate names exist.

Do **not** assume seat number means the same human across games.

## 4. Identity semantics to settle before GREEN

The anti-repeat key should match the player experience, but Clocktower has hidden setup semantics such as Drunk.

Audit whether the relevant repeat key should be:

- actual starting role;
- player-visible starting identity/token;
- or a narrowly typed existing setup concept that already represents what is shown to the player.

Do not leak hidden truth into player-facing state merely to implement this feature. Do not rewrite Drunk/registration/gameplay semantics.

## 5. Architecture fence

Preserve the current setup/session/persistence owners. Do not introduce:

- a second setup coordinator;
- a new global player database solely for this feature;
- deterministic round-robin dealing;
- changes to role-count legality or script setup rules;
- EPI-MQ/recommendation behavior changes;
- UI-NAV changes;
- broad persistence redesign.

Prefer reusing an existing persisted recent-game history if it already carries enough information. If it does not, design the smallest durable record needed for the anti-repeat preference and characterize recovery/version behavior before production changes.

## 6. Tests-first acceptance cases

The first meaningful RED/GREEN should prove at least:

1. **Avoidable repeat:** previous game gives player A role X; next legal role set contains X plus alternatives; assignment does not give X to A when a zero-repeat permutation exists.
2. **Role set invariant:** anti-repeat assignment preserves exactly the selected multiset of roles.
3. **Impossible avoidance:** when constraints make a repeat unavoidable, setup still succeeds and the number of repeats is minimal.
4. **Tie randomness:** multiple equally optimal assignments are not collapsed into one deterministic rotation.
5. **Player reorder:** changing seat order does not defeat the rule if stable player identity is available.
6. **Roster change:** new/missing players do not corrupt assignment or make setup fail.
7. **Persistence/restart:** if history is meant to survive app restart, the same preference remains available after recovery.

Add Drunk/player-visible identity characterization only after the ownership audit determines which identity concept is correct.

## 7. Immediate next action for the new conversation

Start with a **read-only audit**. Produce a short architecture pre-flight identifying:

```text
current role-set owner
current player-assignment owner
current randomness seam
current recent-history owner / persisted shape
stable human-player identity seam
smallest place to apply a soft anti-repeat assignment cost
```

Then recommend GO / MODIFY / NO-GO for reusing the existing history mechanism. Only after that should tests and production changes begin.

## 8. UI-NAV-1 inherited status

UI-NAV-1 is closed for feature development. PR #118 received real-device acceptance after the identity-controller width and night-flow bottom-navigation follow-up fixes. Do not reopen that campaign while implementing ROLE-ROTATION-1 unless an actual regression is reproduced.
