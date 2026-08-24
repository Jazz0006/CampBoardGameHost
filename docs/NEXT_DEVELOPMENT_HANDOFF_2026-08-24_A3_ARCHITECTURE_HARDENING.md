# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.5 Imp self-kill materializer integration GREEN / STOP before Mayor redirect branching or historical replay wiring**  
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
c8a53ca2b5eb8b4fe94e10d9c962b7e597d9e953
CI #631 SUCCESS
R2 #564 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

Documentation-only commits may advance the branch/PR head beyond this code checkpoint.

PR #48 remains open, draft, mergeable, and not merged after H7.5 GREEN validation.

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
  H7.3 GREEN  other-night mechanics materialization boundary
  H7.4 GREEN  Imp self-kill succession branching from current possible-world state
  H7.5 GREEN  self-kill succession integrated into materializer + convergence
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

## 4. H7.1 / H7.2 substrate to preserve

H7.1 proved hidden attack branching must use current historical Demon identity:

```text
RED   f4eeeb967fd0b55ce4fee9d1b3e19e20ad8d15ae
      CI #609 expected FAILURE
GREEN 5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01
      CI #610 SUCCESS / R2 #543 SUCCESS
```

`EnumeratedWorldOtherNightAttackBranching` finds the attacking Imp from `currentRolesBySeat + aliveSeats`, builds `AbilitySubject.actualRole` from current role identity, and keeps stable target-seat choices over `rolesBySeat.keys`.

H7.2 proved Monk branching must likewise use current historical role state:

```text
RED   2b103eaa8386359460e986e8ee35a9e550b76fcd
      CI #618 expected FAILURE
GREEN 8e49772707835e0071774cf6b8ef38ad842041a1
      CI #619 SUCCESS / R2 #552 SUCCESS
```

A former setup Monk that is no longer the current Monk cannot generate effective protection branches.

## 5. H7.3 materialization boundary to preserve

H7.3 introduced the knowledge-safe materializer:

```text
RED   9b6127d517b3cf4bca6add72fcc14dce99bef3e5
      CI #622 expected FAILURE at test compilation
GREEN c20a8a8f3392d82f08fe1ab57f97988ef8db4da8
      CI #623 SUCCESS / R2 #556 SUCCESS
```

The boundary composes:

```text
possible world
-> all legal current-Monk protection branches
-> all legal current-Imp attack branches
-> materialize resolved mechanical outcomes
-> H3 convergence
```

It consumes no storyteller-selected hidden target.

At H7.3, both Mayor redirect and Imp self-kill were deliberately explicit unresolved outcomes. H7.4/H7.5 have now completed only the Imp self-kill side.

## 6. H7.4 standalone Imp succession result

H7.4 introduced `EnumeratedWorldImpSelfKillSuccessionBranching` as a standalone rule-derived primitive.

RED:

```text
6b8de75b6e864ca733d5cc08a2ba031b5355b182
CI #626 expected FAILURE at :app:compileDebugUnitTestKotlin
root cause: missing EnumeratedWorldImpSelfKillSuccessionBranching
production changes = 0
ASP SUCCESS
Real Clingo SUCCESS
R2 #559 SUCCESS
```

GREEN:

```text
e4e8932821db4a785ea783479a3cff1cd54bb75d
CI #627 SUCCESS
R2 #560 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

RED -> GREEN exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldImpSelfKillSuccessionBranching.kt

new file only
86 additions
```

Locked successor semantics:

```text
functioning Scarlet Woman + >=5 alive before Imp self-kill
-> exactly one successor branch: Scarlet Woman

poisoned Scarlet Woman
-> forced Scarlet Woman priority does not apply
-> branch all living current Trouble Brewing Minions

no living current Minion
-> old Imp dies
-> one branch with successorSeat = null
```

The helper derives the living current Imp and successor candidates from `currentRolesBySeat + aliveSeats`, kills the old Imp, and applies the successor with `withCurrentRoles`.

Preserved mechanical state shape:

```text
dead former Imp remains current Imp at its dead seat
+
living successor becomes current Imp
```

No Storyteller-selected `RoleChange` target is consumed.

## 7. H7.5 materializer integration result

H7.5 integrates only the already-rule-derived H7.4 transition into the H7.3 materializer. It does **not** touch historical replay or guards.

RED:

```text
630b2bb8532c915ec9bf317ed66fd7eb121af3b3
CI #630 expected FAILURE at :app:testDebugUnitTest
Android: 754 tests completed, exactly 2 failed
both failures:
  EnumeratedWorldOtherNightMechanicsMaterializerTest
production changes = 0
ASP SUCCESS
Real Clingo SUCCESS
R2 #563 SUCCESS
```

RED exact diff from the previous docs head:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightMechanicsMaterializerTest.kt

one test file only
+33 / -8
```

The RED used the existing API and proved two missing semantics:

```text
1. Imp self-kill successor worlds must become resolved materialized worlds and converge across hidden Monk-protection provenance.
2. After that integration, Mayor redirect must remain the only unresolved other-night attack outcome.
```

GREEN:

```text
c8a53ca2b5eb8b4fe94e10d9c962b7e597d9e953
CI #631 SUCCESS
R2 #564 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

RED -> GREEN exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightMechanicsMaterializer.kt

one production file only
+14 / -8
RED test unchanged
```

Current materializer flow is now:

```text
NO_DEATH
-> unchanged resolved mechanical world

TARGET_DIES
-> direct death mechanical world

IMP_SELF_KILL_SUCCESSOR_REQUIRED
-> EnumeratedWorldImpSelfKillSuccessionBranching
-> successor mechanical world(s)

all resolved paths
-> EnumeratedWorldMechanicalConvergence

MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
-> unresolvedBranches
```

The H7.5 Monk test contains three different legal protection choices that all reach the same Imp-self-kill successor mechanical state. The result must contain that state exactly once after convergence; hidden protection provenance is not counted as extra possible worlds.

The Mayor-focused test proves self-kill no longer leaks into `unresolvedBranches`: Mayor is now the sole unresolved outcome in that world.

The materializer consumes no Storyteller-selected `Protect`, `Attack`, or `RoleChange` target.

## 8. Explicitly still fail-closed / out of scope

`EnumeratedHistoricalExactBaseline.build(...)` must continue to fail closed on:

```text
Attack
Protect
RoleChange
```

H7.5 did **not** modify:

```text
EnumeratedHistoricalExactBaseline.kt
EnumeratedHistoricalWorldReplay.kt
PlayerHistoricalTimeline.kt
```

Do not yet:

```text
implement Mayor redirect branching
wire other-night mechanics into historical replay
relax Attack guard
relax Protect guard
relax RoleChange guard
change PlayerWorldSet cache identity
wire Host / A4 / ZDD
restart App-root S7
expand to other scripts
```

Actual hidden storyteller `Attack` / `Protect` / `RoleChange` payloads remain forbidden as player possible-world truth.

## 9. Why replay wiring must still stop

Imp self-kill is now fully represented at the materializer boundary, but a functioning Mayor target still yields:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
```

That is a legal possible-world branch. Therefore historical exact replay must remain fail-closed: consuming only the currently resolved subset would delete legal Mayor worlds and falsely call a partial result exact.

## 10. Next possible slice — NOT AUTHORIZED / NOT STARTED

The next remaining blocker is a focused Mayor transition slice, tentatively H7.6:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
-> complete rule-derived Mayor death / redirect alternatives
-> materialize mechanical outcomes
-> converge mechanically identical states
```

Before implementation, audit the existing Mayor / night-death semantics carefully. Do not assume that an alternate death is merely an Imp attack retarget; protection/immunity semantics may differ depending on the actual rule cause. Lock those semantics tests-first.

Do not use the Storyteller's actual hidden Mayor resolution or actual death target as player knowledge.

Only after Mayor semantics are complete should a later separate slice wire the complete other-night transition into `EnumeratedHistoricalWorldReplay` and then consider relaxing `Attack` / `Protect` / `RoleChange` guards.

Host / A4 / ZDD remain separately out of scope.

## 11. Validation discipline for the next authorized slice

1. recheck live `main` and PR #48 head/state/checks;
2. compare any docs-only head back to `c8a53ca2b5eb8b4fe94e10d9c962b7e597d9e953`;
3. keep the next RED test-only;
4. prove the RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, and Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before any subsequent slice unless explicitly instructed.
