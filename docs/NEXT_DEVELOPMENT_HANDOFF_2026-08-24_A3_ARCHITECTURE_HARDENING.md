# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.7 Mayor materializer integration GREEN / STOP before historical replay wiring or guard relaxation**  
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

Stable `main` at H7.7 handoff time:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Latest fully validated **code** checkpoint:

```text
32f246341c986275342c95fa65e15df9e9486a5a
CI #644 SUCCESS
R2 #577 SUCCESS
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
  H7.1 GREEN  hidden attack helper uses living current Demon
  H7.2 GREEN  hidden protection helper uses living current Monk
  H7.3 GREEN  Other Night mechanics materialization boundary
  H7.4 GREEN  Imp self-kill succession branching
  H7.5 GREEN  Imp self-kill integrated into materializer + convergence
  H7.6 GREEN  Mayor night-death branching primitive
  H7.7 GREEN  Mayor branching integrated into materializer + convergence
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

Actual Storyteller-hidden `Poison` / `Protect` / `Attack` / `RoleChange` targets are not player knowledge and must never constrain possible worlds directly.

### Setup identity vs current identity

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  dynamic current historical role
```

### Canonical order vs eligibility

Night schedule owns canonical ordering. Current historical world state owns actor eligibility and triggered semantics.

### Mechanical convergence

Different hidden paths ending in the same mechanical state count as one exact world. Hidden choice provenance must not inflate world cardinality.

### Incremental replay

GLOBAL_V1 remains durable chronology authority. Visible observations are revalidated against current historical state at their own GLOBAL point. Do not invent synthetic hidden `globalSequence` values.

## 4. Complete current Other Night materializer

The current Trouble Brewing rule-derived flow is:

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

No known Trouble Brewing Other Night attack outcome remains unresolved at this materializer boundary.

### Imp succession contract

```text
functioning Scarlet Woman + >=5 alive before Imp self-kill
-> forced Scarlet Woman successor

poisoned Scarlet Woman
-> no forced priority
-> branch all living current Trouble Brewing Minions

no living current Minion
-> old Imp dies
-> one null-successor branch
```

No persisted Storyteller `RoleChange` target is consumed.

### Mayor contract

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

## 5. H7.7 result to preserve

### RED

```text
917531e377f0715fb45b8605a0cc7bfbb2a92af0
message: test(a3): lock Mayor materializer integration
CI #643 expected FAILURE at :app:testDebugUnitTest
756 tests completed, exactly 1 failed
failure:
  EnumeratedWorldOtherNightMechanicsMaterializerTest
  Mayor redirect materializes and converges with direct attack and self kill outcomes
production changes = 0
ASP SUCCESS
Real Clingo SUCCESS
R2 #576 SUCCESS
```

RED exact diff from the prior docs head:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightMechanicsMaterializerTest.kt

existing test file only
+8 / -10
```

The RED requires a five-player Mayor world to produce:

```text
unresolvedBranches == empty
five mechanically distinct final worlds
all five possible single-death/succession seat sets represented
Mayor redirect paths that duplicate direct attacks converge
Mayor redirect to Imp converges with the already-existing self-kill succession state
```

### GREEN

```text
32f246341c986275342c95fa65e15df9e9486a5a
message: fix(a3): materialize Mayor night-death branches
CI #644 SUCCESS
R2 #577 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

RED -> GREEN exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightMechanicsMaterializer.kt

existing production file only
+8 / -6
RED test unchanged
```

The Mayor outcome now executes:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
-> EnumeratedWorldMayorNightDeathBranching.branches(branch)
-> derived Mayor mechanical world(s)
-> resolvedWorlds
-> final H3 convergence
```

The materializer consumes no Storyteller-selected `Attack`, `Protect`, Mayor resolution/death target, or `RoleChange` target.

## 6. Important remaining boundary

H7.7 did **not** modify:

```text
EnumeratedHistoricalExactBaseline.kt
EnumeratedHistoricalWorldReplay.kt
PlayerHistoricalTimeline.kt
```

`EnumeratedHistoricalExactBaseline.build(...)` must still reject histories containing:

```text
Attack
Protect
RoleChange
```

Do not relax these guards merely because the standalone materializer is complete. Historical transition timing and end-to-end replay exactness still need their own tests-first slice.

## 7. Next possible slice — NOT AUTHORIZED / NOT STARTED

The next likely slice is **H7.8 historical replay transition integration**.

Before writing RED, audit:

```text
EnumeratedHistoricalWorldReplay.replay
EnumeratedHistoricalWorldSetSnapshot.beginNight
PhaseAdvance handling
visible night observation ordering
transition from NIGHT to DAY
```

The key design question is exactly where a complete Other Night mechanical transition belongs relative to poison refresh, visible observations, and phase advancement. Do not invent a synthetic hidden GLOBAL event or occurrence point.

Target direction only after that audit:

```text
historical snapshot worlds
-> correct rule-owned Other Night boundary
-> materialize every world's legal hidden mechanics
-> mechanical convergence
-> continue GLOBAL visible replay
```

Keep `Attack` / `Protect` / `RoleChange` guards fail-closed through H7.8 unless a separate explicit tests-first guard slice is authorized. Historical replay wiring and guard relaxation should not be silently combined.

Host / A4 / ZDD, other scripts, history UI/misinformation, and App-root S7 remain out of scope.

## 8. Validation discipline

1. recheck live `main` and PR #48 head/state/checks;
2. compare docs-only head back to `32f246341c986275342c95fa65e15df9e9486a5a`;
3. keep the next RED test-only;
4. prove the RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, and Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before subsequent guard/Host work unless explicitly instructed.
