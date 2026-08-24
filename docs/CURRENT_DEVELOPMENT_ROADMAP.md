# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-24  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `84a062378f13b90ce71f3801982ba3b2d3b22d80`  
> Active branch: `codex/a3-historical-multinight-exact-baseline-clean`  
> Draft PR: **#48 `A3: historical multi-night exact baseline`**  
> Latest fully validated **code** checkpoint: `c20a8a8f3392d82f08fe1ab57f97988ef8db4da8`  
> Gates: **CI #623 SUCCESS / R2 #556 SUCCESS / Android + ASP + Real Clingo GREEN**  
> Current execution point: **A3 Architecture Hardening — H7.3 mechanics materialization boundary GREEN; STOP before historical replay wiring / guard relaxation**  
> Detailed handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`

> Documentation-only commits may move the branch/PR head beyond the validated code SHA. New conversations must re-query live `main`, PR #48 head/state/checks before editing.

## 1. Project status

```text
Phase A correctness foundation                     PASS
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic prerequisites                          CLOSED
PR #39 Information Decision Foundation             CLOSED / MERGED
PR #40 Structured Manual UI                        CLOSED / MERGED
PR #42 Historical Action + Observation Capture     CLOSED / MERGED
PR #44 Drunk / Fortune Teller hotfix               CLOSED / MERGED
PR #43 Clocktower host decomposition A1–A13        CLOSED / MERGED
App-root decomposition S0–S6                       CLOSED / MERGED CHECKPOINT
App-root S7                                        PAUSED
PR #48 historical multi-night exact baseline       OPEN / DRAFT / ACTIVE
A3 hardening H1–H6                                 GREEN
A3 H7.1 current-Demon attack helper                GREEN
A3 H7.2 current-Monk protection helper             GREEN
A3 H7.3 other-night mechanics materializer         GREEN
End-to-end hidden Attack/Protect replay             NOT WIRED / BLOCKED
Production Host / A4 / ZDD authority promotion     NOT STARTED / BLOCKED
```

App-root S7 stays paused. Do not mix it into PR #48.

## 2. Current A3 meaning

The original setup/first-night `EnumeratedWorldSet` A3 is already complete. PR #48 extends that exact baseline into:

```text
historical multi-night possible-world evolution
```

This work owns:

```text
GLOBAL durable chronology
alive/dead evolution
persistent ability-state evolution
historical actor eligibility
dynamic current roles
rule-derived hidden mechanics
mechanical-state convergence
```

## 3. Protected architecture contracts

### H1 — one durable observation path

Historical construction separates setup seed knowledge from GLOBAL_V1 durable observation replay. Durable observations must be consumed exactly once.

### H2 — order != eligibility

`ClocktowerFlowPlanner` / night schedule owns canonical order. Historical current-world state + role semantics owns whether an actor can legally act. Dead ordinary Empath is rejected; Ravenkeeper is not rejected merely because dead.

### H3 — mechanical identity != provenance

Mechanically identical states converge even when hidden paths/explanations differ. Branch count must never become exact world count.

### H4 — support boundary

Historical exact construction is explicitly Trouble Brewing-only and fails closed for unsupported scripts.

### H5 — setup role vs current role

`EnumeratedWorld` carries:

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  dynamic historical current-role state
```

Historical state can represent a dead former Imp plus a living successor Imp without weakening setup uniqueness.

### H6 — incremental state-aware replay

GLOBAL history is replayed incrementally. Each visible night ability observation is validated against the current historical state at its own GLOBAL point before proposition evaluation.

No synthetic hidden `globalSequence` values are invented.

## 4. H7 status

```text
H7.1 dynamic current-Demon attack branching      GREEN
H7.2 dynamic current-Monk protection branching   GREEN
H7.3 resolved mechanics materialization boundary GREEN
```

H7.1 RED/GREEN:

```text
RED   f4eeeb967fd0b55ce4fee9d1b3e19e20ad8d15ae
      CI #609 FAILURE as expected
      Android 748 tests / exactly 1 new H7 failure
      ASP + Real Clingo + R2 #542 GREEN

GREEN 5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01
      CI #610 SUCCESS
      R2 #543 SUCCESS
      Android + ASP + Real Clingo GREEN
```

`EnumeratedWorldOtherNightAttackBranching` finds the attacking Imp from `currentRolesBySeat + aliveSeats` and builds `AbilitySubject.actualRole` from current role identity.

H7.2 RED/GREEN:

```text
RED   2b103eaa8386359460e986e8ee35a9e550b76fcd
      CI #618 FAILURE as expected
      Android 749 tests / exactly 1 new H7.2 failure
      ASP + Real Clingo + R2 #551 GREEN

GREEN 8e49772707835e0071774cf6b8ef38ad842041a1
      CI #619 SUCCESS
      R2 #552 SUCCESS
      Android + ASP + Real Clingo GREEN
```

Locked H7.2 contract:

```text
setup seat 3 = Monk
current role seat 3 != Monk
=> seat 3 does not produce functioning Monk protection branches
```

`EnumeratedWorldOtherNightProtectionBranching` discovers a living Monk from `currentRolesBySeat + aliveSeats` and builds `AbilitySubject.actualRole` from `currentRolesBySeat`. Stable protection target-seat enumeration remains `rolesBySeat.keys` because seat identity is stable.

H7.3 RED/GREEN:

```text
RED   9b6127d517b3cf4bca6add72fcc14dce99bef3e5
      CI #622 FAILURE as expected at test compilation
      missing EnumeratedWorldOtherNightMechanicsMaterializer API
      production changes = 0
      ASP + Real Clingo + R2 #555 GREEN

GREEN c20a8a8f3392d82f08fe1ab57f97988ef8db4da8
      CI #623 SUCCESS
      R2 #556 SUCCESS
      Android + ASP + Real Clingo GREEN
```

`EnumeratedWorldOtherNightMechanicsMaterializer` now composes only rule-derived possible-world Monk protection and Imp attack branches. It:

```text
NO_DEATH      -> materializes unchanged mechanical state
TARGET_DIES   -> materializes death state
resolved paths -> mechanical convergence
Mayor redirect / Imp self-kill -> explicit unresolvedBranches
```

It consumes no Storyteller-selected `Protect` or `Attack` target. Hidden branch provenance is not counted as extra exact worlds.

## 5. Hidden-information invariant

Never feed storyteller actual hidden targets or hidden action occurrence points into player possible worlds.

Correct direction:

```text
current possible world
-> generate all legal hidden alternatives from rules
-> materialize mechanical outcomes
-> converge mechanically identical states
```

Actual `Poison` / `Protect` / `Attack` / `RoleChange` payloads remain forbidden as player-world truth.

## 6. Guards and still-out-of-scope work

`EnumeratedHistoricalExactBaseline.build(...)` must still fail closed on:

```text
Attack
Protect
RoleChange
```

H7.3 does **not** wire the new materializer into historical replay and does **not** relax those guards.

Do not yet implement or wire:

```text
end-to-end Monk/Imp replay
Mayor redirect branching
Imp self-kill successor selection
Scarlet Woman succession transition
Host integration
A4/ZDD promotion
history UI / misinformation expansion
other scripts
App-root S7
```

Unresolved legal branches must never be silently dropped to claim a partial “exact” result.

## 7. Next possible slice — NOT STARTED

The next exact-replay step is currently blocked by unresolved legal branches.

A functioning Imp's legal target domain includes itself, so a complete other-night exact transition can produce:

```text
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

and Mayor worlds can produce:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
```

Because those outcomes remain legal possible worlds, historical replay must not wire only the resolved H7.3 subset and call it exact.

The next slice therefore requires an explicit decision/authorization about the unresolved transition boundary — especially Imp self-kill succession, and separately Mayor redirect — before `Attack` / `Protect` guards can safely be relaxed.

Production Host / A4 / ZDD work remains blocked and must not be mixed into this decision.

## 8. Working discipline

For each RED/GREEN slice:

1. recheck live PR head before writing;
2. keep RED test-only;
3. confirm RED is the intended semantic failure;
4. keep GREEN production diff minimal;
5. exact-compare RED head -> GREEN head;
6. wait for CI, R2, ASP and Real Clingo;
7. recheck PR remains open/draft/not merged;
8. stop before the next slice unless explicitly instructed.

Do not merge, mark ready, rebase, force-push, or broaden scope without explicit user authorization.
