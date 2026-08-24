# A3 Historical Multi-Night Exact Baseline — H7.2 Continuation Handoff

> Date: 2026-08-24  
> Status: **NEXT / PR #48 DRAFT / DO NOT MERGE**  
> Immediate target: **H7.2 dynamic current-Monk protection RED -> GREEN**  
> This handoff supersedes older handoffs only for the current execution point. Keep the older architecture audit as background.

## 1. Mandatory startup

Before editing code in the next conversation:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`;
4. read `docs/A3_HARDENING_PROGRESS_CHECKPOINT_2026-08-24_H7_1.md`;
5. read this handoff;
6. query live `main` again;
7. query PR #48 live state/head/checks again;
8. do not assume a docs-only head is the latest validated code head;
9. do not merge, mark ready, rebase, force-push, or widen scope without explicit authorization.

Repository:

```text
Jazz0006/CampBoardGameHost
```

Active branch:

```text
codex/a3-historical-multinight-exact-baseline-clean
```

Draft PR:

```text
#48  A3: historical multi-night exact baseline
```

Stable `main` baseline at handoff time:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Latest fully validated **code** checkpoint:

```text
5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01
CI #610 SUCCESS
R2 #543 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

The branch head may be newer because this checkpoint/handoff documentation was committed after the code gate. Compare any docs-only head back to `5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01` before implementation.

## 2. Current hardening state

```text
H1 GREEN  historical seed / exactly-once durable observations
H2 GREEN  current-state ability eligibility + Ravenkeeper exception
H3 GREEN  mechanical convergence independent of provenance
H4 GREEN  explicit Trouble Brewing support guard
H5 GREEN  immutable setup roles + dynamic currentRolesBySeat
H6 GREEN  incremental state-aware observation replay
H7 IN PROGRESS
  H7.1 GREEN  hidden attack helper uses living current Demon
  H7.2 NEXT   hidden protection helper uses current historical Monk
```

Do not restart App-root S7. It remains paused.

## 3. H7.1 result that H7.2 must preserve

`EnumeratedWorldOtherNightAttackBranching` now uses historical current-state identity:

```text
attacking Imp discovery:
currentRolesBySeat + aliveSeats

AbilitySubject.actualRole:
currentRolesBySeat
```

This was locked by a state containing:

```text
setup seat 3 = Scarlet Woman
setup seat 4 = Imp
current seat 3 = living Imp successor
current seat 4 = dead former Imp
```

The attack helper must continue to branch from seat 3.

Do not regress existing attack semantics for:

```text
poisoned Imp
dead Imp
dead attack target
functioning Monk protection
Soldier
Mayor
Imp self-target
```

## 4. H7.2 problem

`EnumeratedWorldOtherNightProtectionBranching` still reads setup identity for Monk ownership and its actor subject.

Current shape at the H7.1 code checkpoint is conceptually:

```text
find Monk from rolesBySeat
build AbilitySubject.actualRole from rolesBySeat
```

That is inconsistent with H5 dynamic historical current-role state and with the now-corrected attack helper.

H7.2 should make the protection helper a current-state consumer without changing end-to-end historical replay yet.

## 5. H7.2 RED contract

Create a focused new test rather than modifying old tests first.

Minimum RED should prove:

```text
setup:
seat 3 = Monk

current historical role:
seat 3 != Monk

=> seat 3 cannot continue to generate functioning Monk protection branches
```

A useful complementary representation-level assertion is:

```text
setup seat is not Monk
current historical role is Monk
current seat is alive and functioning

=> protection helper recognizes the current Monk and branches every legal other-seat target
```

Important qualification:

> These are current-state consumer contracts. They do not assert that Trouble Brewing currently contains a generic arbitrary role-change mechanic that turns a character into Monk. Historical exact support remains Trouble Brewing only, and actual `RoleChange` facts remain fail-closed.

Expected RED characteristics:

```text
new focused H7.2 test only
production changes = 0
Android compiles
one focused semantic failure
ASP / Real Clingo / R2 remain GREEN
```

Stop and inspect if RED fails because of fixture/setup invariants rather than the intended stale setup-role lookup.

## 6. H7.2 GREEN ownership

Preferred GREEN is narrow:

```text
EnumeratedWorldOtherNightProtectionBranching.kt
```

Expected semantic change:

```text
Monk actor discovery:
rolesBySeat
-> currentRolesBySeat + alive/current ability functioning

AbilitySubject.actualRole:
rolesBySeat
-> currentRolesBySeat
```

Stable seat-choice domain can remain the setup seat set (`rolesBySeat.keys`) because seats are stable even when current roles change.

Do not modify the H7.2 RED test in GREEN unless the RED itself revealed a test bug.

## 7. Explicitly out of scope for H7.2

Do not:

```text
relax Attack constructor guard
relax Protect constructor guard
relax RoleChange constructor guard
wire hidden Monk/Imp helpers into historical replay
implement Mayor redirect branching
implement Imp self-kill successor selection
implement Scarlet Woman succession transition
change PlayerWorldSet cache identity
wire Host / A4 / ZDD
restart App-root decomposition
expand to other scripts
```

## 8. Hidden-information invariant

Actual storyteller-selected hidden actions must never be used as player-world truth.

Keep:

```text
rule-derived hidden alternatives
!= storyteller actual hidden target
```

For future end-to-end wiring:

```text
possible hidden choice
-> mechanical result
-> converge by mechanical state
```

Do not use branch count as exact world count.

## 9. After H7.2 GREEN

Recheck PR/head/checks and stop before broadening unless the user explicitly says continue.

The next likely RED after H7.2 is end-to-end other-night rule-derived branching:

```text
Monk protection alternatives
-> Imp attack alternatives
-> materialized mechanical outcomes
-> convergence
```

But this must remain fail-closed if unresolved legal outcomes cannot yet be represented, especially:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

Do not silently drop those branches to obtain a partial “exact” result.

## 10. Validation discipline

For every H7.2 RED/GREEN step:

1. recheck live PR head before writing;
2. keep RED test-only;
3. verify RED is the intended semantic failure;
4. keep GREEN production diff minimal;
5. compare RED head -> GREEN head exactly;
6. wait for full CI, R2, ASP, and Real Clingo results;
7. recheck PR remains open/draft/not merged;
8. stop before the next slice unless explicitly instructed to continue.
