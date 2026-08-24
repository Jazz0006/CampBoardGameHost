# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-24  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `84a062378f13b90ce71f3801982ba3b2d3b22d80`  
> Active branch: `codex/a3-historical-multinight-exact-baseline-clean`  
> Draft PR: **#48 `A3: historical multi-night exact baseline`**  
> Latest fully validated **code** checkpoint: `4bdf317ec16ab316331a2c322338446620e43631`  
> Gates: **CI #650 SUCCESS / R2 #583 SUCCESS / Android + ASP + Real Clingo GREEN**  
> Current execution point: **A3 Architecture Hardening — H7.8 canonical Other Night replay transition GREEN; STOP before H7.9 public night-death reconciliation / guard relaxation**  
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
A3 H7.8 canonical Other Night replay transition    GREEN
Public night-death reconciliation                  NOT MODELED / BLOCKER
Attack / Protect / RoleChange guard relaxation     NOT STARTED
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

### H6 — incremental state-aware replay
GLOBAL history is replayed incrementally. Visible night observations are validated against the mechanical world state at their own historical point. Hidden mechanics must not invent synthetic `globalSequence` values.

## 3. H7 status and provenance

```text
H7.1 dynamic current-Demon attack branching      GREEN
H7.2 dynamic current-Monk protection branching   GREEN
H7.3 mechanics materialization boundary          GREEN
H7.4 Imp self-kill succession branching          GREEN
H7.5 self-kill materializer integration          GREEN
H7.6 Mayor night-death branching primitive       GREEN
H7.7 Mayor materializer integration              GREEN
H7.8 canonical Other Night replay transition     GREEN
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
H7.8 RED   938aeb42196ee886c5fffe142c3dc3a839d37c3c  CI #648 expected runtime FAILURE
     GREEN 4bdf317ec16ab316331a2c322338446620e43631  CI #650 / R2 #583 GREEN
```

## 4. Complete current Trouble Brewing Other Night materializer

The standalone rule-derived pipeline remains:

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

No known Trouble Brewing Other Night attack outcome remains unresolved at this materializer boundary. No Storyteller-selected `Protect`, `Attack`, Mayor resolution, death target, or `RoleChange` target is consumed.

## 5. H7.8 historical replay transition contract

The audit established that Other Night mechanics cannot simply be deferred to `NIGHT -> DAY`: Trouble Brewing has visible ability slots after the Imp, including Ravenkeeper, Empath and Fortune Teller. Those observations must see the mechanical state after the Demon step.

H7.8 therefore uses the validated ruleset only as a knowledge-neutral canonical-order authority:

```text
DAY -> NIGHT
-> beginNight() poison expiration + rule-derived Poisoner target branching
-> replay visible GLOBAL events
-> before the first visible ability observation whose canonical slot is after Imp:
     apply EnumeratedWorldOtherNightMechanicsMaterializer once
-> evaluate that durable observation against the successor world
-> if no post-Imp visible observation occurs, materialize before NIGHT -> DAY
```

The hidden transition receives **no synthetic `TimelinePoint` or GLOBAL sequence**. Durable `lastGlobalSequence` remains the identity of the actual visible event.

Ravenkeeper receives an additional transition proof:

```text
Ravenkeeper night observation
-> source must have been alive immediately before this materialized Demon step
-> retain only successor worlds where the source became dead at this step
-> then evaluate the Ravenkeeper information
```

This preserves the H2 triggered exception without allowing an already-dead Ravenkeeper to manufacture a later-night trigger.

### H7.8 RED/GREEN details

The first test-file creation commit `0456449ce4689674c84fe6472734314194e82c6e` contained only a test import-path mistake. `938aeb42196ee886c5fffe142c3dc3a839d37c3c` corrected that import and is the authoritative H7.8 RED checkpoint.

Net H7.7 docs-head -> H7.8 RED diff:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedHistoricalOtherNightTransitionIntegrationTest.kt

new test file only
190 additions
production changes = 0
```

CI #648 reached runtime tests and produced exactly the intended failures:

```text
758 tests completed, 2 failed
1. post Imp Fortune Teller observation is evaluated after rule derived other-night mechanics
2. Ravenkeeper observation survives only worlds where the Imp step killed the Ravenkeeper that night
```

ASP + Real Clingo + R2 #581 stayed green.

GREEN consists of two production commits:

```text
d3e227813fd012531073ebd9d649bc3827d00398
  replay rule-derived Other Night transition

4bdf317ec16ab316331a2c322338446620e43631
  provide validated night-order authority to replay
```

RED -> GREEN exact diff:

```text
EnumeratedHistoricalWorldReplay.kt  +101 / -3
EnumeratedHistoricalExactBaseline.kt +9 / -6
RED tests unchanged
```

CI #650 / R2 #583 / Android + ASP + Real Clingo all passed.

## 6. Hidden-information invariant and guards

Never feed Storyteller actual hidden targets or hidden action occurrence points into player possible worlds.

```text
current possible world
-> generate all legal hidden alternatives from rules
-> materialize mechanical outcomes at a rule-owned canonical boundary
-> converge mechanically identical states
-> consume only recipient-visible durable GLOBAL history
```

`EnumeratedHistoricalExactBaseline.build(...)` must **still** fail closed on persisted:

```text
Attack
Protect
RoleChange
```

H7.8 deliberately did not relax these guards.

## 7. Next correctness blocker — H7.9 NOT STARTED / NOT AUTHORIZED

The H7.8 audit exposed the next distinct issue: once a night death has already been materialized from rule-derived hidden mechanics, a later durable `PublicDeath(target)` cannot blindly call `eliminate(target)` in every possible world. The public death is evidence about which hidden branch actually occurred and must reconcile/filter the already-materialized worlds; otherwise replay can create impossible double/multiple deaths.

The next smallest slice should therefore be **H7.9 public night-death reconciliation**, tests-first. It should distinguish public death evidence that confirms an already materialized night outcome from ordinary public/day elimination semantics.

Do **not** combine H7.9 with:

```text
Attack / Protect / RoleChange guard relaxation
Host integration
A4/ZDD promotion
history UI / misinformation expansion
other scripts
App-root S7
```

Only after public death reconciliation is proven should a later separate slice reconsider persisted hidden-action guards.

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
