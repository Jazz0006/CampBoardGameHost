# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-24  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `84a062378f13b90ce71f3801982ba3b2d3b22d80`  
> Active branch: `codex/a3-historical-multinight-exact-baseline-clean`  
> Draft PR: **#48 `A3: historical multi-night exact baseline`**  
> Latest fully validated **code** checkpoint: `32f246341c986275342c95fa65e15df9e9486a5a`  
> Gates: **CI #644 SUCCESS / R2 #577 SUCCESS / Android + ASP + Real Clingo GREEN**  
> Current execution point: **A3 Architecture Hardening — H7.7 Mayor materializer integration GREEN; STOP before historical replay wiring / guard relaxation**  
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
A3 H7.4 Imp self-kill succession helper            GREEN
A3 H7.5 self-kill materializer integration         GREEN
A3 H7.6 Mayor night-death branching helper         GREEN
A3 H7.7 Mayor materializer integration             GREEN
End-to-end hidden Attack/Protect replay             NOT WIRED / BLOCKED
Production Host / A4 / ZDD authority promotion     NOT STARTED / BLOCKED
```

App-root S7 stays paused. Do not mix it into PR #48.

## 2. Protected architecture contracts

### H1 — one durable observation path
Historical construction separates setup seed knowledge from GLOBAL_V1 durable observation replay. Durable observations are consumed exactly once.

### H2 — order != eligibility
Night schedule owns canonical order. Current historical possible-world state and role semantics own actor eligibility. Triggered exceptions such as Ravenkeeper remain explicit.

### H3 — mechanical identity != provenance
Mechanically identical states converge even when hidden paths/explanations differ. Hidden branch count is not exact world count.

### H4 — support boundary
Historical exact construction is explicitly Trouble Brewing-only and fails closed for unsupported scripts.

### H5 — setup role vs current role

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  dynamic historical current-role state
```

This permits a dead former Imp plus a living successor Imp without weakening setup uniqueness.

### H6 — incremental state-aware replay
GLOBAL history is replayed incrementally. Visible night ability observations are validated against current historical state at their own GLOBAL point. No synthetic hidden `globalSequence` values are invented.

## 3. H7 status and provenance

```text
H7.1 dynamic current-Demon attack branching      GREEN
H7.2 dynamic current-Monk protection branching   GREEN
H7.3 mechanics materialization boundary          GREEN
H7.4 Imp self-kill succession branching          GREEN
H7.5 self-kill materializer integration          GREEN
H7.6 Mayor night-death branching primitive       GREEN
H7.7 Mayor materializer integration              GREEN
```

Key checkpoints:

```text
H7.1 RED   f4eeeb967fd0b55ce4fee9d1b3e19e20ad8d15ae  CI #609 expected FAILURE
     GREEN 5cc3bbc64b9ba47c788a8a97eb8a8992d9befa01  CI #610 / R2 #543 GREEN

H7.2 RED   2b103eaa8386359460e986e8ee35a9e550b76fcd  CI #618 expected FAILURE
     GREEN 8e49772707835e0071774cf6b8ef38ad842041a1  CI #619 / R2 #552 GREEN

H7.3 RED   9b6127d517b3cf4bca6add72fcc14dce99bef3e5  CI #622 expected compile FAILURE
     GREEN c20a8a8f3392d82f08fe1ab57f97988ef8db4da8  CI #623 / R2 #556 GREEN

H7.4 RED   6b8de75b6e864ca733d5cc08a2ba031b5355b182  CI #626 expected compile FAILURE
     GREEN e4e8932821db4a785ea783479a3cff1cd54bb75d  CI #627 / R2 #560 GREEN

H7.5 RED   630b2bb8532c915ec9bf317ed66fd7eb121af3b3  CI #630 expected runtime FAILURE
     GREEN c8a53ca2b5eb8b4fe94e10d9c962b7e597d9e953  CI #631 / R2 #564 GREEN

H7.6 RED   a94c4c37d245adf709802b5e7e86d20ed4b01004  CI #639 expected compile FAILURE
     GREEN 3236c7747941cf2feac416e095f3d5de0135a899  CI #640 / R2 #573 GREEN

H7.7 RED   917531e377f0715fb45b8605a0cc7bfbb2a92af0  CI #643 expected runtime FAILURE
     GREEN 32f246341c986275342c95fa65e15df9e9486a5a  CI #644 / R2 #577 GREEN
```

## 4. Current complete Trouble Brewing Other Night materializer

The rule-derived pipeline is now:

```text
possible world
-> all legal living-current-Monk protection alternatives
-> all legal living-current-Imp attack alternatives
-> resolve attack outcome
   NO_DEATH                         -> unchanged mechanical world
   TARGET_DIES                      -> death mechanical world
   IMP_SELF_KILL_SUCCESSOR_REQUIRED -> H7.4 succession world(s)
   MAYOR_TARGET_OR_REDIRECT...      -> H7.6 Mayor night-death world(s)
-> H3 mechanical convergence
```

H7.4/H7.5 succession contract remains:

```text
functioning Scarlet Woman + >=5 alive before Imp self-kill
-> forced Scarlet Woman successor

poisoned Scarlet Woman
-> no forced priority
-> branch every living current Trouble Brewing Minion

no living current Minion
-> old Imp dies
-> one null-successor branch
```

H7.6 Mayor contract remains:

```text
Mayor may die
OR Mayor remains alive and another stable seat is selected

dead redirect target             -> no death
functioning Soldier              -> no death
functioning Monk-protected seat  -> no death
ordinary living redirect target  -> redirect target dies
current living Imp               -> reuse Imp succession branching
```

Redirected current-Poisoner death clears active poison state.

### H7.7 RED/GREEN details

RED `917531e3...` changed only the existing materializer test (`+8/-10`, production changes = 0). CI #643 reached runtime tests and produced:

```text
756 tests completed, 1 failed
only failure:
EnumeratedWorldOtherNightMechanicsMaterializerTest
  Mayor redirect materializes and converges with direct attack and self kill outcomes
```

ASP + Real Clingo + R2 #576 remained green.

GREEN `32f24634...` changed only `EnumeratedWorldOtherNightMechanicsMaterializer.kt` (`+8/-6`, RED test unchanged). `MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED` now delegates to `EnumeratedWorldMayorNightDeathBranching`, and its derived worlds enter the same final H3 convergence as direct attacks and Imp self-kill branches.

The focused 5-player Mayor test proves:

```text
unresolvedBranches == empty
5 mechanically distinct final worlds
Mayor redirects that duplicate direct attack outcomes converge
Mayor redirect to Imp duplicates the existing self-kill succession world and converges
```

CI #644 / R2 #577 / Android + ASP + Real Clingo all passed.

No Storyteller-selected `Protect`, `Attack`, Mayor resolution, death target, or `RoleChange` target is consumed.

## 5. Hidden-information invariant

Never feed Storyteller actual hidden targets or hidden action occurrence points into player possible worlds.

```text
current possible world
-> generate all legal hidden alternatives from rules
-> materialize mechanical outcomes
-> converge mechanically identical states
```

Actual `Poison` / `Protect` / `Attack` / `RoleChange` payloads remain forbidden as player-world truth.

## 6. Guards and still-out-of-scope work

`EnumeratedHistoricalExactBaseline.build(...)` must **still** fail closed on:

```text
Attack
Protect
RoleChange
```

H7.7 completes the currently known Trouble Brewing Other Night materializer outcomes, but does **not** wire that transition into `EnumeratedHistoricalWorldReplay` and does **not** relax any exact-baseline guard.

Do not yet implement or wire:

```text
historical replay consumption of Other Night materialization
Attack / Protect / RoleChange guard relaxation
Host integration
A4/ZDD promotion
history UI / misinformation expansion
other scripts
App-root S7
```

## 7. Next possible slice — NOT STARTED / NOT AUTHORIZED

The next smallest architecture slice is tentatively **H7.8 historical replay transition integration**.

Before writing RED, audit exactly where the complete Other Night mechanical transition belongs in `EnumeratedHistoricalWorldReplay` relative to `PhaseAdvance`, `beginNight()` poison refresh, visible night observations, and transition to day. Do not invent hidden GLOBAL points.

Target direction:

```text
current historical world-set snapshot
-> at the correct rule-owned night boundary
-> apply complete rule-derived Other Night materializer to every possible world
-> converge mechanically identical successor worlds
-> continue visible GLOBAL replay
```

Keep `Attack` / `Protect` / `RoleChange` guards fail-closed until a separate tests-first slice proves end-to-end exact replay is complete and identifies which guards can safely move. Do not combine replay wiring and guard relaxation by default.

Production Host / A4 / ZDD remain separately blocked.

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
