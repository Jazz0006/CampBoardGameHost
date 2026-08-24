# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-24  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> 当前 live `main`（本次文档更新前已确认）：`84a062378f13b90ce71f3801982ba3b2d3b22d80`  
> 当前工作分支：`codex/a3-historical-multinight-exact-baseline-clean`  
> 当前 draft PR：**#48 `A3: historical multi-night exact baseline`**  
> 最后完全验证的代码 checkpoint：`9909e7fc76c0ef700d617ee1c70ae465b1565e67`  
> 该代码 checkpoint gates：**CI #591 SUCCESS / R2 #524 SUCCESS / Android + ASP + Real Clingo GREEN**  
> 当前执行点：**A3 Historical Multi-Night Exact Baseline — Architecture Hardening H1**  
> 下一任务 handoff：`NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`

> 新会话实施前必须重新查询 live `main`、PR #48/head 和 checks。本文记录的 branch/code SHA 可能因 docs-only commits 或后续 slices 前进，不能当作永久 HEAD。

## 1. 当前状态总览

```text
Phase A correctness foundation                         PASS
R5.5 Script & Dynamic Flow Foundation                  CLOSED / MERGED
R6 semantic prerequisites                              CLOSED
PR #39 Storyteller Information Decision Foundation     CLOSED / MERGED
PR #40 Structured Manual UI — Empath numeric slice     CLOSED / MERGED
PR #41 developer workflow + LF policy                  CLOSED / MERGED
PR #42 Historical Action + Observation Capture         CLOSED / MERGED
PR #44 Drunk / Fortune Teller correctness hotfix       CLOSED / MERGED
PR #43 Clocktower host source decomposition            CLOSED / MERGED / A1–A13 GREEN
App-root structural decomposition S0–S6               CLOSED / MERGED CHECKPOINT
App-root S7                                             PAUSED
PR #48 historical multi-night exact baseline           OPEN / DRAFT / ACTIVE
Current execution point                                A3 ARCHITECTURE HARDENING H1
Mayor / Demon-transition expansion                     PAUSED UNTIL HARDENING
Production Host/A4/ZDD promotion                       NOT STARTED / BLOCKED
```

## 2. Stable `main` checkpoint

The corrected current structural baseline is:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

This is the merge of PR #47 that removed the unfinished S7 RED accidentally included in the earlier checkpoint.

Therefore the true App-root structural checkpoint contains **S0–S6 only**.

Do not reintroduce unfinished S7 work into A3.

## 3. App-root decomposition is complete for the current structural pass

The old roadmap said App-root decomposition was still next. That is now historical and must not drive new work.

Completed slices:

```text
S0  dynamic-flow decomposition guard
S1  player setup presentation
S2  settings presentation
S3  Clocktower landing presentation
S4  deal / reveal presentation
S5  results / host-tools / history presentation
S6  legacy generic GameScreen presentation
```

Approximate result:

```text
CampBoardGameHostApp.kt
~325,556 bytes
-> ~229,822 bytes
```

This did not reach the soft 50 KiB guideline, but the current pass stopped because the remaining root responsibilities have significantly higher state/effect/transaction coupling. The project policy remains:

```text
cohesive ownership
> correctness
> state/effect lifetime stability
> transaction ordering
> file-size guideline
```

S7 is paused and is **not** part of PR #48.

## 4. Important terminology — old A3 vs current A3 extension

### 4.1 Original A3 already completed

The older algorithm-plan A3 created the transparent exact `EnumeratedWorldSet` setup/first-night correctness baseline.

It passed the 2026-08-12 exit review.

See:

```text
docs/archive/storyteller_a3_exit_review.md
```

That A3 is not being redone.

### 4.2 Current A3 / PR #48

The active work extends the exact enumerated baseline into:

```text
historical multi-night possible-world evolution
```

This requires semantics that the setup-only baseline deliberately did not own:

```text
GLOBAL historical chronology
alive/dead evolution
persistent ability-state evolution
hidden-mechanics rule branching
historical actor eligibility
role transitions / Demon succession
mechanical-state convergence
```

## 5. PR #48 active branch and boundary

Active branch:

```text
codex/a3-historical-multinight-exact-baseline-clean
```

Draft PR:

```text
#48  A3: historical multi-night exact baseline
```

Base:

```text
main @ 84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Last fully validated code checkpoint before the 2026-08-24 documentation update:

```text
9909e7fc76c0ef700d617ee1c70ae465b1565e67
CI #591 SUCCESS
R2 #524 SUCCESS
Android GREEN
ASP GREEN
Real Clingo GREEN
```

PR remains draft. No merge/ready action is authorized.

Before this documentation update, production/test changes were confined to:

```text
clocktower/epistemic
clocktower/rules
corresponding tests
```

No Host/App-root/persistence/B4/ZDD production authority wiring has been added.

Keep that boundary during hardening.

## 6. Historical exact baseline already implemented

### 6.1 Knowledge-safe historical timeline

`PlayerHistoricalTimeline` merges recipient-visible durable history from:

```text
ActionFactTimeline
+
EpistemicObservationLog
```

It preserves compatible GLOBAL_V1 chronology.

Allowed player-safe historical events currently include:

```text
PublicExecution
PublicDeath
PhaseAdvance
visible Observation
```

Actual storyteller-hidden mechanics are intentionally not exposed:

```text
Poison
Protect
Attack
RoleChange
```

Critical rule:

> Never use actual storyteller-hidden ActionFacts, targets or hidden action occurrence as player possible-world constraints or hidden timing checkpoints.

### 6.2 Exact historical replay

`EnumeratedHistoricalWorldReplay` can evolve exact worlds through:

```text
Execution / Death -> aliveSeats
PhaseAdvance       -> phase / round
Observation        -> exact world filtering
Poisoner lifecycle -> rule-derived possible hidden targets
```

It requires strictly increasing `globalSequence` for historical events.

### 6.3 Poisoner lifecycle

Validated semantics include:

```text
Poisoner death -> persistent poison ends
dusk / DAY->NIGHT -> previous poison expires
new night -> living Poisoner branches legal possible targets
actual hidden Poison target is ignored
Drunk remains DRUNK in the collapsed ability-state model
converged current mechanical states dedupe
```

These conclusions remain valid after the global audit.

### 6.4 End-to-end constructor

`EnumeratedHistoricalExactBaseline.build(...)` currently composes:

```text
TroubleBrewingWorldEnumerator
-> PlayerHistoricalTimeline.project(...)
-> per-world night chronology compatibility
-> EnumeratedHistoricalWorldReplay
```

It still fails closed for:

```text
Attack
Protect
RoleChange
```

Those guards must not be relaxed until exact hidden successor mechanics are fully modeled.

### 6.5 Per-world canonical night schedule

`EnumeratedWorldNightSchedule` reuses `ClocktowerFlowPlanner` to derive canonical role order for each possible world.

Waking identities follow the existing Drunk split:

```text
actual roles
+
shown role for waking slot only
```

Actual role remains mechanical identity.

### 6.6 Visible observation anchoring

`EnumeratedWorldNightObservationAnchor` relates visible night observations to:

```text
source seat
source ability
actual/shown identity
canonical night role slot
```

This is a canonical-order seam only. It is **not** a complete historical ability eligibility or trigger model.

### 6.7 Demon attack rule seam

`DemonNightAttackSemantics` provides a pure direct Imp-target resolution seam:

```text
NO_DEATH
TARGET_DIES
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

It uses existing ability-functioning semantics and handles dead target / Monk protection / Soldier / Mayor / self-target precedence.

Mayor redirect choice and Demon successor choice remain separate branching boundaries.

### 6.8 Hidden Monk and Imp branch helpers

Pure rule-derived possible-world helper seams exist for:

```text
Monk protection choices
Imp attack choices
```

They do not consume the actual storyteller Protect/Attack target.

Their direction is correct, but further horizontal mechanics expansion is paused until the architecture issues in Section 7 are resolved.

## 7. 2026-08-24 global audit findings

A global audit compared current discoveries against v2.2, P1 boundaries, earlier exact-world conclusions and the newest Monk/Imp/night-order work.

Overall verdict:

> **The original direction remains correct. The engine has now reached the boundary where a static setup-world model must be hardened into a dynamic historical-world model.**

The audit found the following blockers.

### 7.1 P0 — durable observations can be consumed through the setup path more than once

`PlayerKnowledgeSnapshot` contains setup knowledge plus durable public/private observations.

The setup-only `TroubleBrewingWorldEnumerator` rejects observations outside FIRST_NIGHT / round 1.

However `EnumeratedHistoricalExactBaseline.build()` currently passes the full snapshot through setup enumeration, and later rebuilds `EnumeratedWorldSet` using the same full snapshot.

`EnumeratedWorldSet.fromWorlds(...)` itself applies:

```text
knowledge.setupKnowledge
+
knowledge.worldReplayObservationsInTimelineOrder()
```

The historical path then applies GLOBAL chronology/replay again.

This creates two risks:

```text
multi-night observation rejected before historical replay starts
same durable observation statically/historically applied more than once
```

Earlier this was usually invisible because observation filtering was time-insensitive. It is no longer acceptable once historical state changes semantics.

Required boundary:

```text
full knowledge snapshot
    -> setup-only seed without durable observations
    -> GLOBAL historical timeline consumes durable observations exactly once
```

This is **H1 and the next implementation slice**.

### 7.2 P0 — canonical night order does not prove historical actor eligibility

The schedule answers:

```text
where does this role belong in canonical night order?
```

It does not fully answer:

```text
was this seat alive/functioning/triggered and legally able to produce this observation at that historical moment?
```

A standard nightly information role killed before its later normal wake should not simply keep producing ordinary later-night information.

But triggered death roles are exceptions; therefore a naive `dead -> cannot act` rule is also wrong.

Required split:

```text
NightSchedule / FlowPlanner
    -> canonical rank/order authority

historical current-world state + role semantics
    -> ability eligibility / trigger authority
```

### 7.3 P0 — `EnumeratedWorld` still represents setup, not dynamic historical identity

Current setup worlds enforce character uniqueness. That is correct for setup.

Historical role transitions can produce states such as:

```text
dead former Imp
+
living successor Imp
```

Therefore simply mutating setup `rolesBySeat` is not a safe dynamic role model.

Preferred direction:

```text
immutable setup world identity
+
dynamic historical current role/alive/effect state
```

`RoleChange` remains fail-closed until this representation exists.

### 7.4 P1 — mechanical identity must be separated from explanation/provenance

`explanationClusters` currently participates in `EnumeratedWorld` equality because the type is a data class.

That means two mechanically identical states with different explanation paths can survive `distinct()` as separate worlds.

Required semantic rule:

```text
same mechanical state
+ different provenance/explanation
=> one exact possible world
   + merged explanation metadata
```

### 7.5 P1 — hidden branch count must not become world count

Monk/Imp hidden-choice helpers may produce many choices that converge to the same mechanical result.

Therefore:

```text
hidden choice path != possible-world identity
```

When wired into replay, materialize current mechanical results and dedupe/converge them by the explicit mechanical-state contract.

### 7.6 P1 — Trouble Brewing support must fail closed explicitly

The current exact evaluator is TB-specific even though the repository supports multiple scripts structurally.

The historical exact baseline must explicitly reject unsupported scripts rather than accidentally produce an empty or misleading “exact” result.

Current supported exact target:

```text
Trouble Brewing only
```

### 7.7 P1 — whole-night static prefilter is not the final historical model

Static night chronology compatibility remains useful characterization, but exact historical semantics must eventually become incremental and state-aware:

```text
current historical world
-> canonical hidden mechanic / durable event
-> mutate current state
-> visible observation at its semantic slot
-> validate using the state at that moment
```

Do not replace GLOBAL_V1 with synthetic hidden sequences. Durable history and hidden canonical night ordering remain separate authorities.

## 8. Prior architecture decisions that remain protected

The global audit confirmed these earlier decisions:

```text
1. actual hidden storyteller actions never constrain player worlds directly
2. GLOBAL_V1 remains durable chronology authority
3. EnumeratedWorldSet remains exact transparent correctness baseline before ZDD
4. rule-derived hidden branching is correct
5. Drunk actual vs shown identity split is correct
6. RoleChange fail-closed is correct
7. no production Host/App-root integration yet
8. no A4/ZDD production promotion yet
9. no synthetic globalSequence for hidden mechanics
10. no reuse of resolved storyteller-fact interaction registry as hidden player-world truth
```

## 9. Current next sequence — A3 Architecture Hardening

Do **not** continue directly with Mayor branching.

The revised tests-first sequence is:

```text
H1  historical seed / exactly-once observation consumption
H2  state-aware standard/triggered ability eligibility
H3  mechanical world identity + convergence independent of provenance
H4  explicit Trouble Brewing historical-exact support guard
H5  dynamic historical role representation / Demon succession state
H6  incremental state-aware night replay
H7  wire Monk -> Imp -> Mayor -> successor mechanics with convergence
```

### H1 — immediate next slice

First RED should prove:

- multi-night durable observations do not enter setup-only enumeration;
- setup-only propositions still constrain initial worlds;
- durable observations are consumed exactly once by the GLOBAL historical replay path;
- fixing the seed does not expose hidden actual ActionFact targets;
- no ZDD/Host authority changes.

Likely scope:

```text
EnumeratedHistoricalExactBaselineTest.kt
EnumeratedHistoricalExactBaseline.kt
```

Keep the slice narrow unless the RED proves a dedicated tiny seed type/helper is required.

### H2 — ability eligibility

Add tests for both:

```text
standard nightly role killed before ordinary later wake -> no ordinary observation
triggered death ability -> not globally rejected just because source is dead
```

Do not implement `dead players never act`.

### H3 — mechanical convergence

Lock:

```text
same mechanical state + different explanation/provenance = cardinality 1
```

while retaining merged explanation metadata.

### H4 — script guard

Unsupported script must fail closed clearly.

### H5 — dynamic historical roles

Tests-first model must support at least:

```text
Imp self-kill
former Imp remains dead historical character
legal successor becomes living Imp
current Demon queries identify successor
setup uniqueness remains intact
```

Also ensure persistent Poisoner poison ends if the source ceases to be Poisoner through a role change.

### H6 — incremental replay

Move observation/mechanical validation to the correct historical state rather than a single initial/static world snapshot.

### H7 — mechanics integration

Only after H1–H6:

```text
Monk hidden protection branching
Imp hidden attack branching
Mayor redirect alternatives
Imp self-kill / Scarlet Woman / successor transition
mechanical-state convergence
```

Relax `Attack` / `Protect` / `RoleChange` fail-closed guards only when each corresponding exact transition is complete.

## 10. PlayerWorldSet identity/cache warning

R6/P1.2 deliberately kept `globalSequence` outside `PlayerWorldSetIdentity` while the world evaluator was time-insensitive.

Historical multi-night reasoning is becoming time-aware now.

Therefore identity/cache semantics must be revisited tests-first when chronology actually changes world semantics.

Do not add timeline fields speculatively merely for cache invalidation. First define the semantic identity of the historical world snapshot.

Relevant background:

```text
docs/r6_p1_2_knowledge_timeline_semantics_2026-08-21.md
docs/r6_p1_2_closeout_2026-08-21.md
```

## 11. PR #48 completion definition

PR #48 remains draft until the exact historical baseline has at least:

```text
single durable chronology authority
exactly-once observation consumption
knowledge-safe hidden mechanics
state-aware ability eligibility
mechanical identity independent of branch provenance
explicit supported-script boundary
dynamic historical role state / Demon succession representation
fail-closed unsupported mechanics
no Host/A4/ZDD production promotion
full CI/R2/Android/ASP/Real-Clingo GREEN
exact changed-file audit
```

A function that merely returns a multi-night replay result is no longer sufficient to call the baseline complete.

## 12. Explicitly out of scope now

Do not mix into H1–H7:

```text
production Host wiring
A4/ZDD authority promotion
history UI redesign
misinformation expansion
new scripts or roles
App-root S7
state-management framework migration
broad refactor of all epistemic types
```

## 13. Working model

Project-level execution rules remain in root `AGENTS.md`.

```text
ChatGPT / Chat
  -> live-state audit
  -> architecture / scope / risk decisions
  -> RED/characterization design
  -> constrained implementation
  -> remote diff / CI / merge review

GitHub connector
  -> preferred small/medium-file implementation and docs writer

Codex / Luna
  -> large/mechanical/full-worktree executor when needed
```

Do not let Luna independently redefine architecture/scope.

Never merge, mark ready, rebase or force-push without explicit user authorization.

## 14. New-conversation startup

For the immediate next conversation:

1. read root `AGENTS.md`;
2. read this roadmap;
3. read `NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md`;
4. re-query live `main`;
5. re-query PR #48 live head/draft state/checks;
6. distinguish docs-only head advancement from the last validated code checkpoint;
7. audit the concrete observation-consumption path:

```text
PlayerKnowledgeSnapshot
-> TroubleBrewingWorldEnumerator
-> EnumeratedWorldSet.fromWorlds
-> PlayerHistoricalTimeline
-> EnumeratedHistoricalWorldReplay
```

8. design and establish **H1 RED only** first;
9. do not start Mayor branching / RoleChange GREEN / Host-A4-ZDD wiring;
10. do not merge PR #48.
