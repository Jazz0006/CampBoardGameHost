# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.2 dynamic current-Monk protection GREEN / STOP before end-to-end Attack/Protect replay**  
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Startup in the next conversation

Before editing code:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. query live `main`;
5. query PR #48 live head/state/checks;
6. if the branch head is newer because of docs-only commits, compare back to the validated code checkpoint below;
7. continue on the existing A3 branch only if the user explicitly authorizes the next slice;
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
8e49772707835e0071774cf6b8ef38ad842041a1
CI #619 SUCCESS
R2 #552 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

Documentation-only commits may advance the branch/PR head beyond this code checkpoint.

PR #48 was open, draft, mergeable, and not merged after H7.2 GREEN validation.

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
  H7.2 GREEN  hidden protection helper uses living current Monk
```

The next end-to-end hidden Attack/Protect replay slice is **not started**. App-root S7 remains paused and must not be restarted in this A3 branch.

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

`EnumeratedWorld` separates:

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

`EnumeratedWorldOtherNightAttackBranching` branches from seat 3.

The helper:

```text
finds attacking Imp from currentRolesBySeat + aliveSeats
builds AbilitySubject.actualRole from currentRolesBySeat
keeps stable seat choices over rolesBySeat.keys
```

## 5. H7.2 result to preserve

H7.2 proved that hidden Monk protection branching must consume current historical role state rather than immutable setup identity.

RED:

```text
2b103eaa8386359460e986e8ee35a9e550b76fcd
CI #618 expected FAILURE
Android 749 tests / exactly 1 failure:
  former setup Monk that is no longer current Monk has no effective protection branch
ASP SUCCESS
Real Clingo SUCCESS
R2 #551 SUCCESS
```

The RED was test-only:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightProtectionBranchingTest.kt

+16 lines
production changes = 0
```

GREEN:

```text
8e49772707835e0071774cf6b8ef38ad842041a1
CI #619 SUCCESS
R2 #552 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

RED -> GREEN exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightProtectionBranching.kt

3 additions / 3 deletions
```

Semantic change:

```text
Monk discovery:
rolesBySeat
-> currentRolesBySeat + aliveSeats

AbilitySubject.actualRole:
rolesBySeat
-> currentRolesBySeat
```

Stable target-seat choice domain remains:

```text
rolesBySeat.keys
```

Locked scenario:

```text
setup seat 3 = Monk
current seat 3 = Soldier
=> no functioning Monk protection branch from seat 3
```

This is a current-state consumer contract only. It does not claim a generic Trouble Brewing mechanic can arbitrarily transform Monk into Soldier.

## 6. Explicitly still fail-closed / out of scope

`EnumeratedHistoricalExactBaseline.build(...)` must continue to fail closed on:

```text
Attack
Protect
RoleChange
```

Do not:

```text
wire hidden Monk/Imp helpers into historical replay
relax Attack guard
relax Protect guard
relax RoleChange guard
implement Mayor redirect
implement Imp self-kill successor selection
implement Scarlet Woman succession transition
change PlayerWorldSet cache identity
wire Host / A4 / ZDD
restart App-root S7
expand to other scripts
```

Actual hidden storyteller `Attack` / `Protect` / `RoleChange` payloads remain forbidden as player possible-world truth.

## 7. Next possible slice — NOT AUTHORIZED / NOT STARTED

Only after explicit user authorization should the next RED move toward end-to-end rule-derived other-night mechanics:

```text
Monk protection alternatives
-> Imp attack alternatives
-> mechanical outcome materialization
-> convergence
```

That future slice must preserve fail-closed handling for unresolved legal outcomes such as:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

If those possibilities cannot yet be represented, the exact path must remain fail-closed rather than claim partial exactness.

## 8. Validation discipline for the next authorized slice

1. recheck live `main` and PR #48 head/state/checks;
2. compare any docs-only head back to `8e49772707835e0071774cf6b8ef38ad842041a1`;
3. keep the next RED test-only;
4. prove the RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, and Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before any subsequent slice unless explicitly instructed.
