# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate target: **H7.2 dynamic current-Monk protection RED -> GREEN**  
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Startup in the next conversation

Before editing code:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. query live `main`;
5. query PR #48 live head/state/checks;
6. if the branch head is newer because of docs-only commits, compare back to the validated code checkpoint below;
7. continue on the existing A3 branch;
8. do not merge, mark ready, rebase, force-push, or widen scope without explicit authorization.

Active branch:

```text
codex/a3-historical-multinight-exact-baseline-clean
```

Stable `main` at handoff time:

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

PR #48 was open, draft, mergeable, and not merged at that checkpoint.

## 2. Current hardening state

```text
H1 GREEN  historical seed / exactly-once durable observations
H2 GREEN  state-aware ability eligibility + Ravenkeeper exception
H3 GREEN  mechanical convergence independent of provenance
H4 GREEN  explicit Trouble Brewing support guard
H5 GREEN  immutable setup roles + dynamic currentRolesBySeat
H6 GREEN  incremental state-aware observation replay
H7 IN PROGRESS
  H7.1 GREEN  hidden attack helper uses living current Demon
  H7.2 NEXT   hidden protection helper uses current historical Monk
```

App-root S7 remains paused and must not be restarted in this A3 branch.

## 3. Core architecture that must remain true

### Knowledge-safe chronology

`PlayerHistoricalTimeline` exposes only recipient-visible durable history:

```text
PublicExecution
PublicDeath
PhaseAdvance
visible Observation
```

Actual storyteller-hidden `Poison` / `Protect` / `Attack` / `RoleChange` targets are not player knowledge and must never constrain possible worlds directly.

### Setup identity vs current identity

`EnumeratedWorld` now separates:

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  dynamic current historical role
```

This supports states such as:

```text
dead former Imp
+
living successor Imp
```

without weakening setup uniqueness.

### Canonical order vs eligibility

Night schedule / `ClocktowerFlowPlanner` owns canonical ordering. Current historical world state owns actor eligibility/trigger semantics.

### Mechanical convergence

Different hidden paths that end in the same mechanical state count as one exact world. Explanation/provenance is merged rather than counted as world cardinality.

### Incremental replay

GLOBAL_V1 remains durable chronology authority. Visible night ability observations are revalidated against the current historical state at their own GLOBAL point.

Do not invent synthetic hidden `globalSequence` values.

## 4. H7.1 result to preserve

H7.1 proved that hidden attack branching must consume `currentRolesBySeat`, not setup Demon identity.

RED:

```text
f4eeeb967fd0b55ce4fee9d1b3e19e20ad8d15ae
CI #609 expected FAILURE
Android 748 tests / exactly 1 H7 failure
ASP + Real Clingo + R2 #542 GREEN
```

GREEN:

```text
5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01
CI #610 SUCCESS
R2 #543 SUCCESS
Android + ASP + Real Clingo GREEN
```

Locked scenario:

```text
setup:
seat 3 = Scarlet Woman
seat 4 = Imp

current:
seat 3 = living Imp
seat 4 = dead former Imp
```

`EnumeratedWorldOtherNightAttackBranching` must branch from seat 3.

The helper now:

```text
finds attacking Imp from currentRolesBySeat + aliveSeats
builds AbilitySubject.actualRole from currentRolesBySeat
keeps stable seat choices over rolesBySeat.keys
```

Existing poisoned/dead Imp, Soldier, Mayor, Monk-protection and self-target semantics remain GREEN.

## 5. Immediate task — H7.2 RED

Current problem:

`EnumeratedWorldOtherNightProtectionBranching` still discovers Monk ownership and builds the actor subject from setup `rolesBySeat`.

Create a new focused test first.

Minimum RED contract:

```text
setup:
seat 3 = Monk

current:
seat 3 != Monk

=> seat 3 must not continue generating functioning Monk protection branches
```

A complementary current-state consumer case may prove:

```text
setup seat is not Monk
current seat is alive/functioning Monk
=> helper recognizes current Monk and branches legal other-seat protections
```

These are representation/current-state consumer contracts only. They do not claim a generic Trouble Brewing mechanic can arbitrarily turn someone into Monk. Historical exact support remains Trouble Brewing-only and actual `RoleChange` remains fail-closed.

Expected RED:

```text
new focused H7.2 test only
production changes = 0
compile succeeds
one intended semantic failure
ASP / Real Clingo / R2 remain GREEN
```

Stop if the failure comes from fixture/setup invariants instead of the stale setup-role lookup.

## 6. H7.2 GREEN ownership

Preferred GREEN scope:

```text
EnumeratedWorldOtherNightProtectionBranching.kt
```

Expected semantic change:

```text
Monk discovery:
rolesBySeat
-> currentRolesBySeat + alive/current ability functioning

AbilitySubject.actualRole:
rolesBySeat
-> currentRolesBySeat
```

Stable target-seat choice domain may remain:

```text
rolesBySeat.keys
```

because seats are stable even when current roles change.

Do not modify the RED test in GREEN unless the RED itself reveals a test bug.

## 7. Explicitly out of scope for H7.2

Do not:

```text
relax Attack guard
relax Protect guard
relax RoleChange guard
wire hidden Monk/Imp helpers into historical replay
implement Mayor redirect
implement Imp self-kill successor selection
implement Scarlet Woman succession transition
change PlayerWorldSet cache identity
wire Host / A4 / ZDD
restart App-root S7
expand to other scripts
```

`EnumeratedHistoricalExactBaseline.build(...)` must continue to fail closed on:

```text
Attack
Protect
RoleChange
```

## 8. After H7.2 GREEN

Only then should the next RED move toward end-to-end rule-derived other-night mechanics:

```text
Monk protection alternatives
-> Imp attack alternatives
-> mechanical outcome materialization
-> convergence
```

Do not silently discard unresolved legal outcomes such as:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

If those possibilities cannot yet be represented, the exact path must remain fail-closed rather than claim partial exactness.

## 9. Validation discipline

For H7.2 RED/GREEN:

1. recheck live PR head before writing;
2. keep RED test-only;
3. prove the RED is the intended semantic failure;
4. keep GREEN production diff minimal;
5. exact-compare RED -> GREEN;
6. wait for CI, R2, ASP, and Real Clingo;
7. recheck PR remains open/draft/not merged;
8. stop before the next slice unless the user explicitly says continue.
