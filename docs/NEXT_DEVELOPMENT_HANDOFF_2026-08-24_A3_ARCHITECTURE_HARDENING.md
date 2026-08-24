# A3 Historical Multi-Night Exact Baseline — Architecture Hardening Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Purpose: hand off the current A3 historical multi-night exact-baseline work to a new conversation after a global architecture audit.  
> This document supersedes older “resume App-root decomposition” handoffs for the current execution point.

## 1. Mandatory startup in the next conversation

Before editing code:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. query live `main` again;
5. query PR #48 live state, head and checks again;
6. do **not** assume the SHAs below are still the latest after documentation commits;
7. continue on the existing A3 branch unless the live audit shows an unexpected branch/base problem;
8. do not merge, mark ready, rebase, force-push or broaden scope without explicit user authorization.

Repository:

```text
Jazz0006/CampBoardGameHost
```

Active branch:

```text
codex/a3-historical-multinight-exact-baseline-clean
```

Draft PR:

```text
#48  A3: historical multi-night exact baseline
```

Stable `main` baseline before this documentation update:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

That commit is the corrected App-root S0–S6 checkpoint after PR #47 removed the accidentally included unfinished S7 RED. S7 remains paused.

Last fully validated **code** checkpoint before this handoff documentation was written:

```text
9909e7fc76c0ef700d617ee1c70ae465b1565e67
CI #591   SUCCESS
R2 #524   SUCCESS
Android   GREEN
ASP       GREEN
Real Clingo GREEN
```

Documentation commits created after that code checkpoint may advance the PR head without changing production/test code. Re-query the live head and checks in the next conversation.

## 2. Important terminology: old A3 vs current A3 extension

There are two historical uses of “A3” in this repository.

### Old completed A3

The original algorithm-plan A3 was the transparent setup/first-night `EnumeratedWorldSet` correctness baseline. It passed its exit review on 2026-08-12 and remains a correctness/debugging baseline.

See:

```text
docs/archive/storyteller_a3_exit_review.md
```

That work is **not being redone**.

### Current A3 work

PR #48 extends the exact enumerated baseline into:

```text
historical multi-night possible-world evolution
```

The current work therefore needs historical chronology, dynamic world state and hidden-mechanics branching that the old setup-only A3 deliberately did not model.

## 3. Structural checkpoint completed before PR #48

App-root decomposition is no longer the next task.

The structural pass completed S0–S6 and reduced:

```text
CampBoardGameHostApp.kt
~325,556 bytes
-> ~229,822 bytes
```

Completed slices:

```text
S0 dynamic-flow guard
S1 player setup presentation
S2 settings presentation
S3 Clocktower landing presentation
S4 deal / reveal presentation
S5 results / host-tools / history presentation
S6 legacy generic GameScreen presentation
```

S7 is deliberately paused.

Corrected stable main checkpoint:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Do not restart App-root decomposition in the next conversation.

## 4. Current PR #48 architecture already implemented

The current draft PR has built the following exact-baseline seams tests-first.

### 4.1 Knowledge-safe historical timeline

`PlayerHistoricalTimeline` projects durable player-visible history from:

```text
ActionFactTimeline
+
EpistemicObservationLog
```

It preserves GLOBAL_V1 ordering and only exposes knowledge-safe historical events.

It maps:

```text
Execution
Death
PhaseAdvance
visible Observation
```

It intentionally drops storyteller-hidden actual-world payloads:

```text
Poison
Protect
Attack
RoleChange
```

Critical invariant:

> Never feed storyteller actual hidden ActionFacts directly into player possible worlds.

Even the occurrence of a hidden action can leak hidden role/action truth, so actual hidden actions are not valid timing checkpoints for possible-world simulation.

### 4.2 Exact historical replay

`EnumeratedHistoricalWorldReplay` currently supports:

```text
public Execution / Death
PhaseAdvance
visible observations
Poisoner lifecycle branching
```

It tracks:

```text
aliveSeats
abilityStatesBySeat
phase
round
lastGlobalSequence
```

Historical events must be strictly increasing by global sequence.

### 4.3 Poisoner lifecycle

Already implemented and validated:

```text
Poisoner death -> persistent poison ends
DAY -> NIGHT dusk -> previous poison expires
new night -> living Poisoner branches all legal player targets
Drunk target remains DRUNK in the collapsed ability-state model
actual hidden Poison ActionFact target is never consumed
converged mechanical states are deduplicated
```

This direction remains correct.

### 4.4 End-to-end exact constructor

`EnumeratedHistoricalExactBaseline.build(...)` currently combines:

```text
TroubleBrewingWorldEnumerator
PlayerHistoricalTimeline
night chronology compatibility
EnumeratedHistoricalWorldReplay
```

The constructor deliberately fails closed for hidden mechanics not yet fully modeled.

At the last code checkpoint it rejects:

```text
Attack
Protect
RoleChange
```

Poison is allowed because its hidden targets are independently generated from rules rather than read from actual-world history.

### 4.5 Possible-world canonical night schedule

`EnumeratedWorldNightSchedule` derives the canonical night order from:

```text
ValidatedClocktowerRuleset
ClocktowerFlowPlanner
EnumeratedWorld
```

Important actual-vs-shown identity rule:

```text
actual roles
+
shown roles only for waking-slot participation
```

A Drunk shown Fortune Teller may occupy the Fortune Teller waking slot while the actual mechanical identity remains Drunk.

Do not replace this with actual storyteller hidden action chronology.

### 4.6 Night observation anchoring

Visible night ability observations are anchored using:

```text
source seat
source ability
actual/shown role identity
canonical night slot
```

GLOBAL_V1 still owns durable observation ordering. Night rank is not inferred from `globalSequence`.

This is a useful ordering seam but, after the global audit, it is explicitly **not sufficient as a complete eligibility/trigger authority**. See Section 6.

### 4.7 Demon direct-attack rules seam

`DemonNightAttackSemantics` is a pure rule seam for direct Imp attack resolution.

It distinguishes:

```text
NO_DEATH
TARGET_DIES
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
IMP_SELF_KILL_SUCCESSOR_REQUIRED
```

It uses existing `AbilityFunctioningSemantics` and accounts for:

```text
malfunctioning attacker
already-dead target
functioning Monk protection
Imp self-target
functioning Soldier
functioning Mayor
```

It deliberately does **not** pick Mayor redirect targets or Demon successors.

### 4.8 Hidden Monk/Imp branch generators

Pure helper seams now exist for possible-world hidden choices:

```text
EnumeratedWorldOtherNightProtectionBranching
EnumeratedWorldOtherNightAttackBranching
```

Their governing principle is correct:

> Generate legal hidden alternatives from rules and the possible world; never read the storyteller's actual hidden Protect/Attack target.

They are not yet production authority and should not be widened into a second complete night simulator before the architecture hardening below is complete.

## 5. PR scope invariant

Before this documentation handoff, PR #48 production/test changes were confined to:

```text
clocktower/epistemic
clocktower/rules
corresponding tests
```

No Host/App-root/persistence/B4/ZDD production authority wiring has been added.

Keep that invariant during architecture hardening unless a narrowly justified type contract requires otherwise.

In particular do not start:

```text
Host integration
A4/ZDD production promotion
history UI changes
misinformation expansion
new scripts/roles
state-management framework work
App-root S7
```

## 6. Global audit findings — blockers before further mechanics expansion

On 2026-08-24 a global audit compared the new findings with the earlier v2.2/P1 goals. The overall direction remains correct, but several architecture gaps are now visible because the engine has crossed from static setup reasoning into time-aware historical reasoning.

### P0 — historical seed currently reuses durable observations

This is the clearest correctness issue.

`PlayerKnowledgeSnapshot` contains:

```text
setupKnowledge
publicObservations
privateObservations
```

The setup-only `TroubleBrewingWorldEnumerator` explicitly rejects non-FIRST_NIGHT / non-round-1 observations.

However `EnumeratedHistoricalExactBaseline.build()` currently passes the full `setupKnowledge` snapshot into setup enumeration and later rebuilds an `EnumeratedWorldSet` with the same full knowledge.

`EnumeratedWorldSet.fromWorlds(...)` itself already applies:

```text
knowledge.setupKnowledge
+
knowledge.worldReplayObservationsInTimelineOrder()
```

Then the historical constructor applies GLOBAL chronology and historical replay again.

Consequences:

1. a current multi-night knowledge snapshot can be rejected by the old setup-only enumerator before historical replay even starts;
2. the same durable observation can be consumed more than once;
3. duplicate application happened to be mostly harmless while the evaluator was time-insensitive, but becomes structurally wrong now that history affects world state.

Required direction:

```text
full PlayerKnowledgeSnapshot
        |
        +--> setup-only seed knowledge
        |      - recipient/perceived role
        |      - setup-only propositions
        |      - no durable observation replay
        |
        +--> GLOBAL_V1 historical timeline
               - every durable observation exactly once
```

Do not casually change cache/world identity while fixing this. Lock the intended seed/history contract tests-first.

### P0 — night order is not the same as actor eligibility

`EnumeratedWorldNightSchedule` currently derives role slots from actual + shown roles but does not by itself represent historical `aliveSeats` eligibility or triggered abilities.

`EnumeratedWorldNightObservationAnchor` proves that an observation belongs at a canonical role slot, but it does not yet prove that the source seat was legally capable of producing that standard nightly observation at that historical moment.

Important rule consequence:

```text
if a standard nightly information role dies before its slot,
it normally does not later wake and produce that ordinary information
```

But triggered death abilities such as Ravenkeeper are different. Therefore **do not implement a naive rule “dead players can never act.”**

Required architecture:

```text
ClocktowerFlowPlanner / NightSchedule
    owns canonical rank/order

historical current-world state + role semantics
    owns alive/functioning/trigger eligibility
```

The next tests should lock both the standard case and a triggered exception.

### P0 — `EnumeratedWorld` is still a setup-world representation

Current `EnumeratedWorld` includes:

```text
rolesBySeat
shownRolesBySeat
aliveSeats
abilityStatesBySeat
...
```

It also enforces setup character uniqueness.

That is correct for initial setup but insufficient for dynamic role transitions.

Example:

```text
seat 1 = Imp
seat 2 = Minion
Imp self-kills
seat 1 remains dead former Imp
seat 2 becomes living Imp
```

A historical model may therefore need both:

```text
immutable setup identity
current historical role identity
```

Do not simply remove the setup uniqueness invariant from `EnumeratedWorld`; the setup enumerator relies on it.

Preferred design direction to test before implementation:

```text
EnumeratedWorld                    immutable setup-world baseline
        +
EnumeratedHistoricalWorld/state    dynamic current roles/alive/effects
```

or an equivalent explicit split.

`RoleChange` must remain fail-closed until this representation can express Demon succession safely.

### P1 — mechanical world identity must be separate from explanation/provenance

`EnumeratedWorld` is a Kotlin data class. `explanationClusters` participates in equality, so two mechanically identical states with different explanation provenance may survive `distinct()` as separate worlds.

That can bias exact cardinality once hidden branch paths converge.

Required semantic rule:

```text
same current mechanical state
+ different hidden path / explanation provenance
=> one possible world
   with merged explanation/provenance metadata
```

Do not let branch-path count become possible-world count.

This is the general form of the Poisoner convergence rule already discovered earlier.

### P1 — hidden choice branches are not world cardinality

The Monk/Imp helpers expose legal hidden choices. Several choices can resolve to the same mechanical state.

Example:

```text
malfunctioning Imp chooses seat 1 -> NO_DEATH
malfunctioning Imp chooses seat 2 -> NO_DEATH
...
```

Those are distinct hypothetical choices, not necessarily distinct current worlds.

When these helpers are eventually wired into replay:

```text
choice/provenance
!=
mechanical world identity
```

Materialize mechanical results, then converge/dedupe by the mechanical state contract.

### P1 — Trouble Brewing support boundary is not explicit enough

The current exact engine uses Trouble Brewing-specific setup and observation semantics:

```text
TroubleBrewingWorldEnumerator
TroubleBrewingWorldObservationEvaluator
Baron / Drunk / Poisoner / Fortune Teller / Spy / Recluse logic
```

The constructor should not accidentally report an “exact” result for an unsupported script just because the role catalog is non-empty.

Add an explicit fail-closed support contract for the historical exact baseline.

Current target is Trouble Brewing only.

### P1 — static chronology prefilter must not become the final replay model

Current night chronology filtering checks visible night observations against a per-world canonical schedule before full historical mutation.

That is useful characterization, but the final exact engine needs incremental state-aware replay:

```text
current historical world
-> role/mechanic slot or public event
-> mutate current state
-> next visible observation
-> validate against the state at that exact moment
```

Do not throw away GLOBAL_V1. GLOBAL_V1 remains durable history authority; canonical night slots are a separate semantic ordering layer for hidden mechanics.

## 7. Findings that remain VALID from earlier work

The audit did **not** invalidate the core direction.

Keep these decisions:

1. actual storyteller hidden Poison/Protect/Attack/RoleChange facts must never be passed directly to player-world construction;
2. GLOBAL_V1 is the durable public/visible chronology authority;
3. exact `EnumeratedWorldSet` remains the transparent correctness baseline before ZDD promotion;
4. Poisoner hidden targets are rule-generated, not actual-history-derived;
5. Drunk actual identity and shown waking identity remain distinct;
6. `RoleChange` remains fail-closed until dynamic historical role state is modeled;
7. no production Host/App-root wiring yet;
8. no A4/ZDD authority promotion yet;
9. no synthetic hidden `globalSequence` values should be invented merely to place private mechanics between durable events;
10. `ClocktowerCharacterInteractionRegistry` resolved storyteller facts are not the right source for player-world hidden mechanics.

## 8. Revised next implementation sequence

Do **not** continue directly to Mayor redirect branching.

The next phase is:

# A3 Architecture Hardening

Recommended tests-first order:

### H1 — historical seed contract

First RED should prove at minimum:

- a multi-night visible observation does not enter the setup-only enumerator path;
- setup-only propositions still seed exact initial worlds;
- durable observations are consumed exactly once by the historical/GLOBAL replay path;
- the resulting exact baseline is not changed merely by duplicate static preprocessing;
- no hidden actual action target is introduced while fixing the seed.

Likely owner:

```text
EnumeratedHistoricalExactBaselineTest.kt
EnumeratedHistoricalExactBaseline.kt
```

Do not widen to ZDD.

### H2 — historical ability eligibility

RED contracts should prove both sides:

- a standard nightly information role killed before a later ordinary night cannot produce a normal later-night observation from that role;
- a triggered death ability is not rejected merely because the player is dead.

Do not implement one global `alive == false -> cannot act` rule.

### H3 — mechanical world identity / convergence

RED should prove:

```text
mechanically identical worlds with different explanation/provenance
count as one exact world
```

while explanation metadata remains recoverable/merged.

This contract must also protect future Monk/Imp branch convergence.

### H4 — explicit Trouble Brewing support guard

RED should prove that an unsupported script fails closed with a clear unsupported-exact-baseline reason rather than returning empty or misleading exact results.

### H5 — dynamic historical role representation

Design this tests-first before changing `EnumeratedWorld`.

Minimum target scenario:

```text
initial Imp self-kills
former Imp remains dead historical character
legal successor becomes living Imp
current Demon queries identify the successor
setup uniqueness remains intact
```

Also ensure Poisoner persistent poison ends if the Poisoner ceases to be Poisoner through role change.

### H6 — incremental state-aware night replay

Once the representation exists, move from static whole-night compatibility toward ordered state transitions.

Desired conceptual shape:

```text
GLOBAL durable boundary
+
canonical per-world night slots
+
current historical mechanical state
+
visible observation anchors
```

No actual hidden-action occurrence should be used as a secret timing marker.

### H7 — then wire hidden mechanics

Only after H1–H6 are stable:

```text
Monk protection branching
-> Imp attack branching
-> Mayor redirect alternatives
-> Imp self-kill / Scarlet Woman / successor transition
-> dedupe converged mechanical states
```

Attack/Protect/RoleChange constructor guards should be relaxed only when their entire exact transition semantics are modeled and characterized.

## 9. Identity/cache warning

Earlier R6/P1.2 deliberately kept `globalSequence` out of `PlayerWorldSetIdentity` while world filtering was time-insensitive.

Historical multi-night reasoning is now becoming time-aware.

Therefore the identity contract must be revisited tests-first **when chronology changes actual world semantics**.

Do not add `globalSequence` to identity as a speculative cache invalidation field; first define what historical snapshot/state the identity represents.

Relevant background:

```text
docs/r6_p1_2_knowledge_timeline_semantics_2026-08-21.md
docs/r6_p1_2_closeout_2026-08-21.md
```

## 10. Current PR boundary and completion definition

PR #48 should remain draft while hardening continues.

Do not call the historical exact baseline complete merely because a multi-night replay function returns a result.

A credible completion gate now requires at least:

```text
single durable chronology authority
single observation consumption path
knowledge-safe hidden mechanics
state-aware ability eligibility
mechanical world identity independent of branch provenance
explicit supported-script boundary
dynamic historical role representation
Demon-transition-capable current-state model
fail-closed unsupported mechanics
no production Host/A4/ZDD authority promotion
full CI/R2/ASP/Real-Clingo GREEN
exact changed-file audit
```

## 11. Expected changed-file discipline

Prefer narrow RED/GREEN slices.

For H1, a good slice should normally touch only:

```text
EnumeratedHistoricalExactBaselineTest.kt
EnumeratedHistoricalExactBaseline.kt
```

If a new tiny seed/helper type is necessary, add it only if the test demonstrates a real ownership need.

Do not use the audit as justification for a mass rewrite.

## 12. PR/body/docs cleanup before eventual merge discussion

Before PR #48 can be considered for readiness:

- update PR body from the older checkpoint to the final architecture and validated head;
- update `CURRENT_DEVELOPMENT_ROADMAP.md` with final A3 status;
- keep this handoff or a successor handoff current;
- document remaining deliberately unsupported mechanics;
- run exact diff audit and confirm no Host/App-root/persistence/B4/ZDD authority drift;
- confirm all remote gates on the final head.

Do not mark ready or merge without explicit user authorization.

## 13. Suggested first prompt for the new conversation

Use approximately:

```text
请读取根目录 AGENTS.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md 和
 docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_A3_ARCHITECTURE_HARDENING.md。
先重新确认 live main、draft PR #48/head 和最新 checks。
然后从 A3 Architecture Hardening 的 H1 historical seed contract 开始：
先审计 PlayerKnowledgeSnapshot -> TroubleBrewingWorldEnumerator -> EnumeratedWorldSet.fromWorlds
-> historical replay 的 observation consumption 路径，建立 RED，证明 multi-night durable observations
不会进入 setup-only enumeration，且每条 durable observation 只由 GLOBAL historical replay 消费一次。
先不要做 Mayor branching、RoleChange GREEN、Host/A4/ZDD production wiring，也不要 merge PR #48。
```
