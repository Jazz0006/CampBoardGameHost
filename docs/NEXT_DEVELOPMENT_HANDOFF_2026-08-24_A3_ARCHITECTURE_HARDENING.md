# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.4 Imp self-kill succession branching GREEN / STOP before H7.4→H7.3 materializer integration**  
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
e4e8932821db4a785ea783479a3cff1cd54bb75d
CI #627 SUCCESS
R2 #560 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

Documentation-only commits may advance the branch/PR head beyond this code checkpoint.

PR #48 remains open, draft, mergeable, and not merged after H7.4 GREEN validation.

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
  H7.4 GREEN  Imp self-kill succession branching from current possible-world state
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

`EnumeratedWorldOtherNightAttackBranching` finds the attacking Imp from `currentRolesBySeat + aliveSeats`, builds `AbilitySubject.actualRole` from current role identity, and keeps stable target-seat choices over `rolesBySeat.keys`.

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

GREEN:

```text
c20a8a8f3392d82f08fe1ab57f97988ef8db4da8
CI #623 SUCCESS
R2 #556 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

The materializer composes:

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

Those remain in `unresolvedBranches` at the current checkpoint. The materializer consumes no storyteller-selected `Protect` or `Attack` target.

## 7. H7.4 result to preserve

H7.4 implemented the rule-derived Imp self-kill successor transition as a standalone primitive. It is **not yet wired into H7.3**.

RED:

```text
6b8de75b6e864ca733d5cc08a2ba031b5355b182
CI #626 expected FAILURE at :app:compileDebugUnitTestKotlin
root cause:
  unresolved reference EnumeratedWorldImpSelfKillSuccessionBranching
production changes = 0
ASP SUCCESS
Real Clingo SUCCESS
R2 #559 SUCCESS
```

RED file only:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldImpSelfKillSuccessionBranchingTest.kt
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
-> Scarlet Woman forced priority does not apply
-> branch all living current Trouble Brewing Minions

no living current Minion
-> old Imp dies
-> one branch with successorSeat = null
```

The helper derives the living current Imp and Minion candidates from `currentRolesBySeat + aliveSeats`, not immutable setup identity. It requires a functioning current Imp, kills that Imp mechanically, then applies a successor with `withCurrentRoles`.

Important preserved state shape:

```text
dead former Imp remains current Imp at its dead seat
+
living successor becomes current Imp
```

This intentionally uses H5's duplicate-current-role support. `withCurrentRoles` also preserves the existing Poisoner identity transition rule: if a living Poisoner becomes Imp, stale `MALFUNCTIONING_POISONED` state is cleared because the Poisoner seat changed; if another Minion becomes Imp while the Poisoner remains, poison state is not silently cleared.

No Storyteller-selected `RoleChange` target is consumed.

## 8. Explicitly still fail-closed / out of scope

`EnumeratedHistoricalExactBaseline.build(...)` must continue to fail closed on:

```text
Attack
Protect
RoleChange
```

H7.4 did **not** modify:

```text
EnumeratedWorldOtherNightMechanicsMaterializer.kt
EnumeratedHistoricalExactBaseline.kt
EnumeratedHistoricalWorldReplay.kt
PlayerHistoricalTimeline.kt
```

Do not yet:

```text
wire H7.4 successor branches into H7.3 materializer
wire other-night mechanics into historical replay
relax Attack guard
relax Protect guard
relax RoleChange guard
implement Mayor redirect
change PlayerWorldSet cache identity
wire Host / A4 / ZDD
restart App-root S7
expand to other scripts
```

Actual hidden storyteller `Attack` / `Protect` / `RoleChange` payloads remain forbidden as player possible-world truth.

## 9. Why replay wiring must still stop

H7.4 resolves the standalone successor transition, but H7.3 still returns `IMP_SELF_KILL_SUCCESSOR_REQUIRED` as unresolved until the helper is explicitly integrated.

Separately, a functioning Mayor target still produces:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
```

Therefore historical replay must remain fail-closed. Wiring only a subset of resolved branches would silently delete legal worlds and falsely report a partial result as exact.

## 10. Next possible slice — NOT AUTHORIZED / NOT STARTED

The next smallest slice is **H7.5**:

```text
H7.3 self-kill unresolved branch
-> H7.4 successor branching
-> materialize successor worlds
-> mechanical convergence
```

The expected scope is focused tests plus `EnumeratedWorldOtherNightMechanicsMaterializer.kt`. Do not consume `ActionFact.RoleChange` or any Storyteller-selected successor target.

H7.5 must continue to return Mayor redirect as explicit unresolved state:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
```

Even after H7.5, do not wire historical replay or relax `Attack` / `Protect` / `RoleChange` guards while Mayor semantics remain incomplete.

Host / A4 / ZDD remain separately out of scope.

## 11. Validation discipline for the next authorized slice

1. recheck live `main` and PR #48 head/state/checks;
2. compare any docs-only head back to `e4e8932821db4a785ea783479a3cff1cd54bb75d`;
3. keep the next RED test-only;
4. prove the RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, and Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before any subsequent slice unless explicitly instructed.
