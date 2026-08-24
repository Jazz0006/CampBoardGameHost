# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.10 no-public-death dawn reconciliation GREEN / STOP before persisted hidden-action guard relaxation**  
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Startup in the next conversation

Before editing code:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. query live `main`;
5. query PR #48 live head/state/checks;
6. if docs-only commits advanced the branch, compare back to the validated code checkpoint below;
7. continue only if the user explicitly authorizes the next slice;
8. do not merge, mark ready, rebase, force-push, or widen scope without explicit authorization.

Active branch:

```text
codex/a3-historical-multinight-exact-baseline-clean
```

Stable `main` at H7.10 handoff time:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Latest fully validated **code** checkpoint:

```text
3d446937b7cbef36b3fed679fff64b9582e450ac
CI #662 SUCCESS
R2 #595 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

Documentation-only commits may advance the PR head beyond this SHA.

## 2. Current hardening state

```text
H1 GREEN  historical seed / exactly-once durable observations
H2 GREEN  state-aware ability eligibility + Ravenkeeper exception
H3 GREEN  mechanical convergence independent of provenance
H4 GREEN  Trouble Brewing-only support guard
H5 GREEN  immutable setup roles + dynamic currentRolesBySeat
H6 GREEN  incremental state-aware observation replay
H7 IN PROGRESS
  H7.1 GREEN   hidden attack helper uses living current Demon
  H7.2 GREEN   hidden protection helper uses living current Monk
  H7.3 GREEN   Other Night mechanics materialization boundary
  H7.4 GREEN   Imp self-kill succession branching
  H7.5 GREEN   Imp self-kill integrated into materializer + convergence
  H7.6 GREEN   Mayor night-death branching primitive
  H7.7 GREEN   Mayor branching integrated into materializer + convergence
  H7.8 GREEN   canonical Other Night transition wired into historical replay
  H7.9 GREEN   public night-death reconciliation
  H7.10 GREEN  no-public-death dawn reconciliation
```

The next blocker is persisted `Attack` / `Protect` / `RoleChange` guard relaxation. App-root S7 remains paused and must not be restarted in this A3 branch.

## 3. Architecture contracts that must remain true

### Knowledge-safe durable chronology

`PlayerHistoricalTimeline` exposes only recipient-visible history:

```text
PublicExecution
PublicDeath
PhaseAdvance
visible Observation
```

Actual Storyteller-hidden `Poison` / `Protect` / `Attack` / `RoleChange` targets are not player knowledge and must never constrain possible worlds directly.

### Setup vs current identity

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  dynamic historical current role
```

### Canonical order is not a hidden durable event

The validated script's night order may position rule-derived mechanics relative to visible observations. It must never create a synthetic `TimelinePoint` or `globalSequence` for a hidden action.

### Mechanical convergence

Different hidden paths ending in the same mechanical state count as one exact world. Hidden choice provenance must not inflate world cardinality.

## 4. Current Trouble Brewing Other Night replay model

The standalone materializer remains:

```text
possible world
-> current-Monk protection alternatives
-> current-Imp attack alternatives
-> DemonNightAttackSemantics
   NO_DEATH                         -> unchanged world
   TARGET_DIES                      -> direct death world
   IMP_SELF_KILL_SUCCESSOR_REQUIRED -> H7.4 Imp succession world(s)
   MAYOR_TARGET_OR_REDIRECT...      -> H7.6 Mayor night-death world(s)
-> EnumeratedWorldMechanicalConvergence
```

No known Trouble Brewing Other Night attack outcome is unresolved at this materializer boundary.

H7.8 inserts the transition at the canonical Imp boundary relative to visible ability observations:

```text
DAY -> NIGHT
-> beginNight() poison expiration + rule-derived Poisoner branching
-> replay visible GLOBAL events
-> before first visible ability observation canonically after Imp:
     materialize Other Night mechanics once
-> evaluate that observation against successor worlds
```

Ravenkeeper observations additionally require alive -> dead transition proof at that night's materialized Demon step. No synthetic hidden timeline point is created.

## 5. H7.9 public night-death reconciliation to preserve

H7.9 prevents a later public death from adding a second death to a world whose hidden Demon transition already killed someone.

```text
PublicDeath(target) before mechanics materialize
-> materialize with target as public death confirmation
-> retain only target alive -> dead successor worlds

PublicDeath(target) after mechanics materialize
-> re-materialize target-compatible outcomes from the saved pre-transition snapshot
-> mechanically intersect with current observation-filtered successor worlds

compatible intersection
-> public death confirms/selects the hidden outcome
-> target is not killed again

empty intersection because earlier GLOBAL evidence proves death happened later
-> preserve chronology
-> eliminate at PublicDeath's actual GLOBAL point
```

H7.9 checkpoints:

```text
RED   6fad4ed5f51590ab884af88437b711a494c2e3c9
      CI #656: 760 tests / exactly 2 focused failures

GREEN a81b5949e00b18a24f4f1b0522a3f41aa892efa1
      CI #658 / R2 #591 / Android + ASP + Real Clingo GREEN
```

The first GREEN attempt `6626d9f97c43b901b60b17385ec791d52fa12274` exposed one H6 regression and is not the validated checkpoint; the final H7.9 correction preserves later public deaths at their own GLOBAL point.

## 6. H7.10 no-public-death dawn reconciliation

### Production chronology audit

Current Host night confirmation establishes this semantic ordering:

```text
night outcome resolved
-> actual public Death fact is committed when a player dies
-> phase transition closes the night
```

The UI can display `No night death`, but there is no durable semantic `ActionFact.NoDeath`. Therefore, for the current GLOBAL history model, a **completed NIGHT -> DAY boundary with no PublicDeath recorded during that night** is the only durable evidence that no player died that night.

Absence is not evidence while the night is still open. It becomes evidence only when the durable phase boundary closes the night.

### RED

```text
28b56c83b3c8efbca570f3b8c3c40219b3076535
message: test(a3): lock no-public-death dawn reconciliation
```

Exact diff from the H7.9 docs head:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedHistoricalNoPublicDeathDawnReconciliationTest.kt

new test file only
184 additions
production changes = 0
docs changes = 0
```

The fixed Trouble Brewing world is:

```text
1 Fortune Teller
2 Chef
3 Soldier
4 Scarlet Woman
5 Imp
```

This gives both legal no-death and legal death alternatives without Poisoner branching complexity.

The two RED tests lock:

```text
1. no post-Imp visible observation, no PublicDeath, then NIGHT -> DAY
   -> materialize hidden mechanics at dawn
   -> retain only outcomes with unchanged aliveSeats

2. post-Imp Fortune Teller observation materializes hidden mechanics early,
   no PublicDeath occurs before NIGHT -> DAY
   -> re-derive no-death-compatible successor states from the saved pre-transition snapshot
   -> mechanically intersect them with the current observation-filtered worlds
```

CI #661 reached runtime tests:

```text
762 tests completed, exactly 2 failed
exactly the two H7.10 tests
```

R2 #594 / ASP / Real Clingo were green; compilation and APK assembly succeeded.

### GREEN

```text
3d446937b7cbef36b3fed679fff64b9582e450ac
message: fix(a3): reconcile no-death nights at dawn
```

RED -> GREEN exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedHistoricalWorldReplay.kt

+39 / -12
RED tests unchanged
```

Implementation contract:

```text
track publicDeathObservedThisNight

if PublicDeath occurs in NIGHT
-> H7.9 handles/reconciles that public outcome
-> mark the night as having public death evidence

at completed NIGHT -> DAY with validated ruleset:
  if no PublicDeath occurred and mechanics are not yet materialized:
    -> materialize with confirmedNoPublicDeath
    -> retain only successor worlds whose aliveSeats equal the pre-transition world

  if no PublicDeath occurred and mechanics already materialized:
    -> re-materialize no-death-compatible successors from saved pre-transition snapshot
    -> mechanically intersect with current observation-filtered worlds

  if PublicDeath occurred:
    -> do not apply no-death reconciliation
```

This also means contradictory history, such as a Ravenkeeper death-trigger observation followed by a completed night with no public death, may correctly eliminate all worlds.

CI #662 SUCCESS / R2 #595 SUCCESS / Android + ASP + Real Clingo SUCCESS.

## 7. Guards remain fail-closed

`EnumeratedHistoricalExactBaseline.build(...)` must still reject persisted:

```text
Attack
Protect
RoleChange
```

H7.8–H7.10 now reproduce and reconcile the relevant hidden mechanics from rule/world state and visible chronology, but these slices do **not** authorize reading Storyteller-selected hidden targets.

## 8. Next possible slice — NOT AUTHORIZED / NOT STARTED

Tentative next slice:

```text
H7.11 persisted Attack / Protect / RoleChange guard-relaxation audit
```

Before changing the guard, re-audit at minimum:

```text
EnumeratedHistoricalExactBaseline.build
PlayerHistoricalTimeline.project
EnumeratedHistoricalWorldReplay.replay
RoleChange/currentRolesBySeat transition paths
```

Tests-first objectives should prove:

```text
1. persisted hidden Attack / Protect / RoleChange facts may exist in Storyteller history without
   their target payloads entering PlayerHistoricalTimeline or constraining player worlds;

2. hidden mechanics continue to be generated from rules + possible-world state, not copied from
   the actual Storyteller action;

3. visible GLOBAL observations/public outcomes remain consumed exactly once and in order;

4. rule-derived Imp succession/current-role state still converges correctly when an actual hidden
   RoleChange fact is present but ignored as player knowledge;

5. unsupported/non-Trouble-Brewing boundaries continue to fail closed.
```

Do not combine guard relaxation with:

```text
Host integration
A4/ZDD promotion
other scripts
history UI / misinformation expansion
App-root S7
```

## 9. Validation discipline

1. recheck live `main` and PR #48 head/state/checks;
2. compare docs-only head back to `3d446937b7cbef36b3fed679fff64b9582e450ac`;
3. keep the next RED test-only;
4. prove RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before subsequent Host/A4/ZDD work unless explicitly instructed.

Documentation should be updated automatically only at meaningful architecture/phase checkpoints; do not create a docs commit for every small code change.
