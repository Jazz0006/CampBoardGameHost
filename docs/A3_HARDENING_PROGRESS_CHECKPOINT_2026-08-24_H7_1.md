# A3 Architecture Hardening — H7.1 Progress Checkpoint

> Date: 2026-08-24  
> Status: **CURRENT EXECUTION CHECKPOINT / PR #48 DRAFT / DO NOT MERGE**  
> Scope: records the live A3 historical multi-night exact-baseline state after H7.1 GREEN.  
> This checkpoint supersedes the stale execution-point fields in `CURRENT_DEVELOPMENT_ROADMAP.md` and `NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`; those older documents remain useful for background architecture and audit history.

## 1. Live repository checkpoint

Repository:

```text
Jazz0006/CampBoardGameHost
```

Stable `main` baseline:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Active branch:

```text
codex/a3-historical-multinight-exact-baseline-clean
```

Draft PR:

```text
#48  A3: historical multi-night exact baseline
```

Latest fully validated code checkpoint:

```text
5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01
```

Validation:

```text
CI #610                  SUCCESS
R2 #543                  SUCCESS
Android unit/build gate  SUCCESS
ASP contract tests       SUCCESS
Real Clingo              SUCCESS
```

PR #48 was rechecked after H7.1 GREEN and remained:

```text
OPEN
DRAFT
NOT MERGED
mergeable = true
```

Do not merge, mark ready, rebase, force-push, or widen scope without explicit user authorization.

## 2. Current hardening status

```text
H1  historical seed / exactly-once durable observation path          GREEN
H2  historical ability eligibility                                  GREEN
H3  mechanical world identity / convergence                         GREEN
H4  explicit Trouble Brewing exact-support guard                    GREEN
H5  dynamic historical current-role representation                  GREEN
H6  incremental state-aware observation replay                      GREEN
H7  hidden mechanics integration                                    IN PROGRESS
H7.1 dynamic current-Demon attack branching                         GREEN
H7.2 dynamic current-Monk protection branching                      NEXT
```

App-root S7 remains paused and is unrelated to PR #48.

Production Host integration, A4/ZDD authority promotion, history UI, misinformation expansion, and unrelated feature work remain out of scope.

## 3. H1–H6 protected architecture

The following contracts are now established and must not be regressed while H7 proceeds.

### H1 — single durable observation path

Historical construction separates:

```text
setup-only seed knowledge
+
GLOBAL_V1 historical durable observations
```

Durable observations must not be consumed through both the setup enumerator and historical replay.

### H2 — canonical order != actor eligibility

`ClocktowerFlowPlanner` / night schedule owns canonical ordering.

Historical current-world state + role semantics owns whether a seat can legally produce an ordinary or triggered ability observation at that moment.

A dead ordinary Empath cannot produce a later ordinary Empath observation, while Ravenkeeper is not rejected merely because the source is dead.

### H3 — mechanical identity != provenance

Mechanically identical worlds converge even if their explanation/provenance paths differ. Explanation metadata is merged instead of turning branch-path count into possible-world count.

### H4 — exact support boundary

Historical exact construction explicitly fails closed outside Trouble Brewing.

### H5 — setup role identity vs current historical role identity

`EnumeratedWorld` now carries both:

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  dynamic historical current-role state
```

Setup uniqueness remains enforced only on setup identity. Historical state can represent:

```text
dead former Imp
+
living successor Imp
```

Current Demon queries use current role + alive state.

Poisoner lifecycle also uses current role ownership so persistent poison can end when current Poisoner ownership changes.

### H6 — incremental state-aware replay

GLOBAL durable history remains the replay authority.

At each visible night ability observation, historical replay revalidates source eligibility against the current world at that exact GLOBAL point before applying the existing observation evaluator.

No synthetic `globalSequence` is invented for hidden mechanics.

## 4. H7.1 RED -> GREEN provenance

### RED

Commit:

```text
f4eeeb967fd0b55ce4fee9d1b3e19e20ad8d15ae
test(a3): add H7 dynamic Demon attack RED
```

RED contract:

```text
setup:
seat 3 = Scarlet Woman
seat 4 = Imp

current historical state:
seat 3 = living Imp successor
seat 4 = dead former Imp
```

Hidden attack branching must use seat 3 as the current Demon rather than setup Imp identity.

Expected semantics included:

```text
target seat 3 -> IMP_SELF_KILL_SUCCESSOR_REQUIRED
target seat 4 -> NO_DEATH (already-dead former Imp)
ordinary living target -> TARGET_DIES unless another rule intervenes
```

RED validation:

```text
CI #609       FAILURE (expected)
Android       748 tests completed, 1 failed
              only EnumeratedHistoricalDynamicDemonAttackBranchingTest failed
ASP           SUCCESS
Real Clingo   SUCCESS
R2 #542       SUCCESS
```

This proved that `EnumeratedWorldOtherNightAttackBranching` still looked at setup `rolesBySeat` and therefore could not consume H5 dynamic current-role state correctly.

### GREEN

Commit:

```text
5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01
fix(a3): use current Demon in hidden attack branching
```

Exact RED->GREEN production diff:

```text
1 production file
0 test files changed

EnumeratedWorldOtherNightAttackBranching.kt
+3 / -3
```

The helper now:

```text
finds the attacking Imp from currentRolesBySeat + aliveSeats
builds AbilitySubject.actualRole from currentRolesBySeat
keeps legal target-seat choices over the stable seat set
```

GREEN validation:

```text
CI #610       SUCCESS
R2 #543       SUCCESS
Android       SUCCESS
ASP           SUCCESS
Real Clingo   SUCCESS
```

Existing tests for ordinary setup Imp, poisoned Imp, dead Imp, Soldier, Mayor, Monk protection interaction, and self-target precedence remained GREEN.

## 5. Hidden mechanics safety invariant

Actual storyteller-selected hidden targets remain forbidden as player possible-world inputs.

The correct direction remains:

```text
current possible world
-> generate all legal hidden choices from rules
-> materialize mechanical outcomes
-> converge mechanically identical states
```

Never use actual hidden `Poison` / `Protect` / `Attack` / `RoleChange` payloads, targets, or occurrence points as hidden player-world truth.

## 6. Constructor guards remain closed

`EnumeratedHistoricalExactBaseline.build(...)` must continue to fail closed on:

```text
Attack
Protect
RoleChange
```

H7 helper hardening is not permission to relax those guards.

Mayor redirect, Imp self-kill successor selection, Scarlet Woman succession transition, and end-to-end Monk/Imp replay wiring are not complete yet.

## 7. Next execution point: H7.2 RED

The next narrow tests-first slice is **dynamic current-Monk protection branching**.

Current `EnumeratedWorldOtherNightProtectionBranching` still discovers Monk ownership and builds its actor subject from setup `rolesBySeat`.

H7.2 should lock that hidden-mechanics helpers consume historical current-role state consistently.

Recommended RED contract:

```text
setup identity says seat 3 = Monk
current historical role says seat 3 != Monk
=> seat 3 must not keep producing functioning Monk protection branches
```

A complementary representation-level case may prove:

```text
setup seat is not Monk
current historical role is Monk
=> current-role helper semantics recognize the current Monk
```

This is a current-state consumer contract; it does not claim that Trouble Brewing currently has a normal arbitrary role-change mechanic that creates Monk. `RoleChange` remains fail-closed until the exact supported transition semantics are modeled.

Likely owners:

```text
EnumeratedWorldOtherNightProtectionBranching.kt
new focused H7.2 test
```

Do not change attack replay, constructor guards, Mayor redirect, or Demon succession in H7.2.

## 8. After H7.2

Only after H7.2 is GREEN should the next RED move toward end-to-end hidden other-night mechanics:

```text
rule-derived Monk protection
-> rule-derived Imp attack
-> mechanical outcome materialization
-> convergence
```

A key architecture warning remains:

```text
hidden choice branch count != exact world count
```

Also, a partial attack implementation must not silently discard unresolved legal branches such as:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

If those legal possibilities cannot yet be represented, the end-to-end exact path must remain fail-closed rather than claim partial exactness.

## 9. New-conversation startup rule

Before implementation in a new conversation:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md` for long-range background;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md` for the original architecture audit;
4. read this checkpoint for the newer H1–H7.1 execution state;
5. read the newer H7.2 handoff if present;
6. re-query live `main`, PR #48 head/state, and checks;
7. if the live head differs because of docs-only commits, compare back to `5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01` before touching production;
8. continue tests-first with H7.2 only.
