# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-24  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `84a062378f13b90ce71f3801982ba3b2d3b22d80`  
> Active branch: `codex/a3-historical-multinight-exact-baseline-clean`  
> Draft PR: **#48 `A3: historical multi-night exact baseline`**  
> Latest fully validated **code** checkpoint: `3236c7747941cf2feac416e095f3d5de0135a899`  
> Gates: **CI #640 SUCCESS / R2 #573 SUCCESS / Android + ASP + Real Clingo GREEN**  
> Current execution point: **A3 Architecture Hardening — H7.6 standalone Mayor night-death branching GREEN; STOP before H7.7 materializer integration / historical replay wiring**  
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
End-to-end hidden Attack/Protect replay             NOT WIRED / BLOCKED
Production Host / A4 / ZDD authority promotion     NOT STARTED / BLOCKED
```

App-root S7 stays paused. Do not mix it into PR #48.

## 2. Current A3 meaning

PR #48 extends the exact setup/first-night `EnumeratedWorldSet` baseline into historical multi-night possible-world evolution. The architecture owns GLOBAL durable chronology, alive/dead evolution, persistent ability-state evolution, historical actor eligibility, dynamic current roles, rule-derived hidden mechanics, and mechanical-state convergence.

## 3. Protected architecture contracts

### H1 — one durable observation path
Historical construction separates setup seed knowledge from GLOBAL_V1 durable observation replay. Durable observations are consumed exactly once.

### H2 — order != eligibility
Night schedule owns canonical order. Current historical possible-world state and role semantics own whether an actor can legally act. Triggered exceptions such as Ravenkeeper remain explicit.

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
GLOBAL history is replayed incrementally. Visible night ability observations are validated against the current historical state at their own GLOBAL point. No synthetic hidden `globalSequence` values are invented.

## 4. H7 status and provenance

```text
H7.1 dynamic current-Demon attack branching      GREEN
H7.2 dynamic current-Monk protection branching   GREEN
H7.3 resolved mechanics materialization boundary GREEN
H7.4 Imp self-kill succession branching          GREEN
H7.5 self-kill materializer integration          GREEN
H7.6 Mayor night-death branching primitive       GREEN
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
```

### H7.4 / H7.5 Imp self-kill contract

`EnumeratedWorldImpSelfKillSuccessionBranching` derives succession only from current possible-world state. It never consumes a persisted Storyteller `RoleChange` target.

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

H7.5 wires those worlds into the materializer and H3 convergence. Different hidden Monk-protection paths reaching the same successor state converge mechanically.

### H7.6 Mayor night-death contract

H7.6 introduced standalone `EnumeratedWorldMayorNightDeathBranching`. It consumes a rule-derived Mayor attack branch and generates every legal Mayor resolution from current possible-world state:

```text
Mayor may die
OR
Mayor remains alive and another stable seat is selected as the redirect target
```

Redirect resolution preserves Demon-night safety semantics:

```text
dead redirect target             -> no death
functioning Soldier              -> no death
functioning Monk-protected seat  -> no death
ordinary living redirect target  -> redirect target dies
current living Imp               -> reuse H7.4 Imp succession branching
```

If the redirected death removes the current Poisoner, active `MALFUNCTIONING_POISONED` state is cleared consistently with existing death reducers.

H7.6 RED was test-only: one new test file, +83, production changes = 0. CI #639 failed exactly because `EnumeratedWorldMayorNightDeathBranching` did not exist; ASP + Real Clingo + R2 #572 stayed green. GREEN added exactly one production helper file (+108) with RED tests unchanged; CI #640 / R2 #573 / Android + ASP + Real Clingo all passed.

**H7.6 is deliberately not integrated into `EnumeratedWorldOtherNightMechanicsMaterializer` yet.** At this checkpoint the materializer still exposes `MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED` as unresolved.

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

`EnumeratedHistoricalExactBaseline.build(...)` must still fail closed on:

```text
Attack
Protect
RoleChange
```

H7.6 does **not** wire Mayor branching into the materializer, does not wire other-night materialization into historical replay, and does not relax those guards.

Do not yet implement or wire:

```text
H7.6 Mayor helper into H7.3/H7.5 materializer
end-to-end Monk/Imp/Mayor historical replay
Attack / Protect / RoleChange guard relaxation
Host integration
A4/ZDD promotion
history UI / misinformation expansion
other scripts
App-root S7
```

Unresolved legal branches must never be silently dropped to claim a partial “exact” result.

## 7. Next possible slice — NOT STARTED

The next smallest slice is **H7.7 Mayor materializer integration**:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
-> EnumeratedWorldMayorNightDeathBranching
-> resolved Mayor/deputy-death/sink/succession worlds
-> H3 mechanical convergence
```

H7.7 should modify only focused materializer tests plus `EnumeratedWorldOtherNightMechanicsMaterializer.kt`. It must consume no Storyteller-selected Mayor resolution or hidden death target.

Only after H7.7 proves the materializer has no unresolved legal Other Night outcomes should a later **separate** slice consider wiring the complete transition into `EnumeratedHistoricalWorldReplay` and relaxing `Attack` / `Protect` / `RoleChange` guards. Do not combine those concerns.

Production Host / A4 / ZDD remains separately blocked.

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
