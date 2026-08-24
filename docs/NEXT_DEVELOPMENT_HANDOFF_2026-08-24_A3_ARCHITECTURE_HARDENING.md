# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.3 other-night mechanics materialization GREEN / STOP before historical replay wiring or guard relaxation**  
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
c20a8a8f3392d82f08fe1ab57f97988ef8db4da8
CI #623 SUCCESS
R2 #556 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

Documentation-only commits may advance the branch/PR head beyond this code checkpoint.

PR #48 remains open, draft, mergeable, and not merged after H7.3 GREEN validation.

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
  H7.3 GREEN  resolved other-night mechanics materialization + explicit unresolved branch boundary
```

End-to-end hidden Attack/Protect historical replay is **not wired**. App-root S7 remains paused and must not be restarted in this A3 branch.

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

This supports states such as a dead former Imp plus a living successor Imp without weakening setup uniqueness.

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

`EnumeratedWorldOtherNightAttackBranching`:

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

GREEN:

```text
8e49772707835e0071774cf6b8ef38ad842041a1
CI #619 SUCCESS
R2 #552 SUCCESS
Android + ASP + Real Clingo GREEN
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

Stable target-seat choice domain remains `rolesBySeat.keys`.

## 6. H7.3 result to preserve

H7.3 introduced a knowledge-safe materialization boundary without wiring historical replay and without relaxing exact-baseline guards.

RED:

```text
9b6127d517b3cf4bca6add72fcc14dce99bef3e5
CI #622 expected FAILURE at :app:compileDebugUnitTestKotlin
root cause:
  unresolved reference EnumeratedWorldOtherNightMechanicsMaterializer
production changes = 0
ASP SUCCESS
Real Clingo SUCCESS
R2 #555 SUCCESS
```

RED file only:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightMechanicsMaterializerTest.kt
```

GREEN:

```text
c20a8a8f3392d82f08fe1ab57f97988ef8db4da8
CI #623 SUCCESS
R2 #556 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

RED -> GREEN exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightMechanicsMaterializer.kt

new file only
74 additions
```

The materializer accepts only an `EnumeratedWorld` and composes the existing rule-derived protection/attack helpers:

```text
possible world
-> all legal current-Monk protection branches
-> all legal current-Imp direct attack branches
-> materialize NO_DEATH / TARGET_DIES
-> converge resolved mechanical worlds
```

It deliberately keeps these outcomes explicit rather than discarding them:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

Those are returned in `unresolvedBranches` with their branch context. They are **not** counted as resolved exact worlds.

Locked H7.3 test world:

```text
seat 1 Empath
seat 2 Chef
seat 3 Monk
seat 4 Poisoner
seat 5 Imp
```

The resolved branch set converges to the mechanically distinct outcomes:

```text
no death
death seat 1
death seat 2
death seat 3
death seat 4
```

Imp self-target remains unresolved for the legal Monk-protection branches where seat 5 is not protected. Hidden path multiplicity is not mistaken for exact world cardinality.

The materializer consumes no storyteller-selected `Protect` or `Attack` target.

## 7. Explicitly still fail-closed / out of scope

`EnumeratedHistoricalExactBaseline.build(...)` must continue to fail closed on:

```text
Attack
Protect
RoleChange
```

H7.3 did **not** modify:

```text
EnumeratedHistoricalExactBaseline.kt
EnumeratedHistoricalWorldReplay.kt
PlayerHistoricalTimeline.kt
```

Do not yet:

```text
wire H7.3 materialization into historical replay
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

## 8. Why replay wiring must still stop

A functioning Imp's legal target set includes the Imp itself. Therefore a complete rule-derived other-night transition can legally produce:

```text
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

A world containing a functioning Mayor can also produce:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
```

Because those are legal possible branches, wiring only H7.3 `resolvedWorlds` into historical replay would silently delete possible worlds and would falsely report a partial result as exact.

The exact path must therefore remain fail-closed until the unresolved branch boundary is resolved or otherwise represented completely.

## 9. Next possible slice — NOT AUTHORIZED / NOT STARTED

The next architectural decision is no longer “how to enumerate Monk/Imp choices”; H7.1–H7.3 now provide that substrate.

The next blocker is how to complete unresolved legal outcomes, especially:

```text
Imp self-kill -> successor selection / role transition
Mayor target -> death or redirect branching
```

The user previously excluded Imp succession and Mayor redirect from the H7.2 scope. Do not begin either one automatically. Obtain explicit authorization for the next blocker slice before changing their semantics.

Only after unresolved outcomes are complete should a later slice wire rule-derived other-night mechanics into `EnumeratedHistoricalWorldReplay` and consider relaxing `Attack` / `Protect` guards.

Host / A4 / ZDD remain separately out of scope.

## 10. Validation discipline for the next authorized slice

1. recheck live `main` and PR #48 head/state/checks;
2. compare any docs-only head back to `c20a8a8f3392d82f08fe1ab57f97988ef8db4da8`;
3. keep the next RED test-only;
4. prove the RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, and Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before any subsequent slice unless explicitly instructed.
