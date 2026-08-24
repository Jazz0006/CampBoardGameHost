# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-24  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `84a062378f13b90ce71f3801982ba3b2d3b22d80`  
> Active branch: `codex/a3-historical-multinight-exact-baseline-clean`  
> Draft PR: **#48 `A3: historical multi-night exact baseline`**  
> Latest fully validated **code** checkpoint: `3d446937b7cbef36b3fed679fff64b9582e450ac`  
> Gates: **CI #662 SUCCESS / R2 #595 SUCCESS / Android + ASP + Real Clingo GREEN**  
> Current execution point: **A3 Architecture Hardening — H7.10 no-public-death dawn reconciliation GREEN; STOP before persisted hidden-action guard relaxation**  
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
A3 H7.9 public night-death reconciliation          GREEN
A3 H7.10 no-public-death dawn reconciliation       GREEN
Attack / Protect / RoleChange guard relaxation     NOT STARTED / NEXT BLOCKER
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

## 3. H7 status and checkpoints

```text
H7.1  dynamic current-Demon attack branching      GREEN
H7.2  dynamic current-Monk protection branching   GREEN
H7.3  mechanics materialization boundary          GREEN
H7.4  Imp self-kill succession branching          GREEN
H7.5  self-kill materializer integration          GREEN
H7.6  Mayor night-death branching primitive       GREEN
H7.7  Mayor materializer integration              GREEN
H7.8  canonical Other Night replay transition     GREEN
H7.9  public night-death reconciliation           GREEN
H7.10 no-public-death dawn reconciliation         GREEN
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
H7.9 RED   6fad4ed5f51590ab884af88437b711a494c2e3c9  CI #656 expected runtime FAILURE
     GREEN a81b5949e00b18a24f4f1b0522a3f41aa892efa1  CI #658 / R2 #591 GREEN
H7.10 RED  28b56c83b3c8efbca570f3b8c3c40219b3076535  CI #661 expected runtime FAILURE
      GREEN 3d446937b7cbef36b3fed679fff64b9582e450ac  CI #662 / R2 #595 GREEN
```

## 4. Complete Trouble Brewing Other Night materializer

The rule-derived pipeline is:

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

No known Trouble Brewing Other Night attack outcome remains unresolved at this materializer boundary. No Storyteller-selected `Protect`, `Attack`, Mayor resolution/death target, or `RoleChange` target is consumed.

## 5. Historical Other Night replay contract after H7.8–H7.10

The validated ruleset is used only as knowledge-neutral canonical-order authority. Hidden mechanics receive no synthetic durable timeline point.

```text
DAY -> NIGHT
-> beginNight() poison expiration + rule-derived Poisoner target branching
-> replay visible GLOBAL events
-> before first visible ability observation canonically after Imp:
     materialize Other Night mechanics once
-> evaluate that durable observation against successor worlds
```

Ravenkeeper information additionally requires proof that its source changed alive -> dead at that materialized Demon step.

Public night outcomes are then reconciled as follows:

```text
PublicDeath(target) compatible with the Demon transition
-> retain only compatible target alive -> dead successor worlds
-> do not kill target a second time

PublicDeath(target) proven by earlier GLOBAL evidence to occur later
-> keep chronology intact
-> eliminate at the PublicDeath event's own GLOBAL point

completed NIGHT -> DAY with no PublicDeath during that night
-> this is the durable semantic-history no-death evidence in the current producer model
-> retain only Other Night successor states whose aliveSeats did not change
```

If a post-Imp observation already forced early materialization, dawn no-death reconciliation re-materializes no-death-compatible states from the saved pre-transition snapshot and mechanically intersects them with the current observation-filtered worlds. Explanation provenance remains excluded from identity.

The absence of `PublicDeath` is **not** interpreted before the night completes; it becomes evidence only at the durable phase boundary.

## 6. H7.10 RED/GREEN details

Production chronology audit confirmed that the current Host commits an actual night death before the phase transition. A “No night death” display event exists, but there is no separate durable `ActionFact.NoDeath`; therefore the current GLOBAL semantic-history representation of a completed peaceful night is the `NIGHT -> DAY` boundary with no preceding durable `PublicDeath` in that night.

RED `28b56c83b3c8efbca570f3b8c3c40219b3076535` added only:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedHistoricalNoPublicDeathDawnReconciliationTest.kt

184 additions
production changes = 0
docs changes = 0
```

The tests lock both paths:

```text
1. no post-Imp observation, no PublicDeath, then NIGHT -> DAY
   -> materialize and retain only no-death outcomes

2. post-Imp Fortune Teller observation materializes mechanics early,
   then no PublicDeath before NIGHT -> DAY
   -> reconcile the current observation-filtered worlds to no-death outcomes
```

CI #661 reached runtime tests: **762 tests completed, exactly 2 failed**, exactly these H7.10 tests. R2 #594 / ASP / Real Clingo were green.

GREEN `3d446937b7cbef36b3fed679fff64b9582e450ac` changes only `EnumeratedHistoricalWorldReplay.kt` (`+39/-12` from RED); RED tests are unchanged. It tracks whether a public death occurred in the current night and applies no-death filtering only at the completed `NIGHT -> DAY` boundary. CI #662 / R2 #595 / Android + ASP + Real Clingo all passed.

## 7. Hidden-information invariant and remaining guards

Never feed Storyteller actual hidden targets or hidden action occurrence points into player possible worlds.

```text
current possible world
-> generate all legal hidden alternatives from rules
-> materialize at rule-owned canonical boundaries
-> reconcile recipient-visible public outcomes
-> converge mechanically identical states
-> consume only recipient-visible durable GLOBAL history
```

`EnumeratedHistoricalExactBaseline.build(...)` still fails closed on persisted:

```text
Attack
Protect
RoleChange
```

H7.10 deliberately did not relax those guards.

## 8. Next correctness slice — NOT STARTED / NOT AUTHORIZED

With positive public night death and completed no-death outcomes both reconciled, the next blocker is the persisted hidden-action guard itself.

Tentative next slice: **H7.11 persisted Attack / Protect / RoleChange guard-relaxation audit**.

The audit must prove tests-first that persisted hidden facts may be present in the Storyteller history without their hidden payloads becoming player knowledge. The expected direction is to continue projecting only knowledge-safe `PlayerHistoricalTimeline` events and reproduce hidden mechanics from rules/world state, not from actual Storyteller targets.

Do **not** combine guard relaxation with:

```text
Host integration
A4/ZDD promotion
history UI / misinformation expansion
other scripts
App-root S7
```

## 9. Working discipline

For each RED/GREEN slice:

1. recheck live PR head before writing;
2. keep RED test-only;
3. confirm RED is the intended semantic failure;
4. keep GREEN production diff minimal;
5. exact-compare RED head -> GREEN head;
6. wait for CI, R2, ASP and Real Clingo;
7. recheck PR remains open/draft/not merged;
8. stop before the next slice unless explicitly instructed.

Documentation should be updated automatically only at meaningful architecture/phase checkpoints; do not create a docs commit for every small code change.

Do not merge, mark ready, rebase, force-push, or broaden scope without explicit user authorization.
