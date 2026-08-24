# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.6 standalone Mayor night-death branching GREEN / STOP before H7.7 materializer integration or historical replay wiring**  
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Startup in the next conversation

Before editing code:

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. query live `main`;
5. query PR #48 live head/state/checks;
6. if the branch head is newer because of docs-only commits, compare it back to the validated code checkpoint below;
7. continue on the existing A3 branch only if the user explicitly authorizes the next slice;
8. do not merge, mark ready, rebase, force-push, or widen scope without explicit authorization.

Active branch:

```text
codex/a3-historical-multinight-exact-baseline-clean
```

Stable `main` at H7.6 handoff time:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Latest fully validated **code** checkpoint:

```text
3236c7747941cf2feac416e095f3d5de0135a899
CI #640 SUCCESS
R2 #573 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

Documentation-only commits may advance the branch/PR head beyond this code checkpoint.

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
  H7.3 GREEN  other-night mechanics materialization boundary
  H7.4 GREEN  Imp self-kill succession branching
  H7.5 GREEN  Imp self-kill integrated into materializer + convergence
  H7.6 GREEN  Mayor night-death branching primitive
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

This supports a dead former Imp plus a living successor Imp without weakening setup uniqueness.

### Canonical order vs eligibility

Night schedule owns canonical ordering. Current historical world state owns actor eligibility and triggered semantics.

### Mechanical convergence

Different hidden paths that end in the same mechanical state count as one exact world. Explanation/provenance must not inflate world cardinality.

### Incremental replay

GLOBAL_V1 remains durable chronology authority. Visible observations are revalidated against current historical state at their own GLOBAL point. Do not invent synthetic hidden `globalSequence` values.

## 4. H7 substrate already complete

H7.1/H7.2 made attack/protection branching use current historical roles and alive state rather than immutable setup identity.

H7.3 introduced the knowledge-safe Other Night materializer boundary:

```text
possible world
-> all legal current-Monk protection branches
-> all legal current-Imp attack branches
-> resolved mechanical worlds
-> H3 convergence
```

H7.4 introduced standalone `EnumeratedWorldImpSelfKillSuccessionBranching` and H7.5 integrated that transition into the materializer.

Important H7.4 successor contract:

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

No Storyteller-selected `RoleChange` target is consumed.

H7.5 current materializer flow remains:

```text
NO_DEATH                           -> resolved unchanged world
TARGET_DIES                        -> resolved death world
IMP_SELF_KILL_SUCCESSOR_REQUIRED   -> H7.4 successor world(s)
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED -> unresolved
all resolved paths                 -> H3 convergence
```

## 5. H7.6 result to preserve

H7.6 was intentionally implemented as a **standalone Mayor primitive first**, mirroring H7.4. It is not yet integrated into the materializer.

### RED

```text
a94c4c37d245adf709802b5e7e86d20ed4b01004
message: test(a3): lock Mayor night-death branching
CI #639 expected FAILURE at :app:compileDebugUnitTestKotlin
root cause: missing EnumeratedWorldMayorNightDeathBranching
production changes = 0
ASP SUCCESS
Real Clingo SUCCESS
R2 #572 SUCCESS
```

RED exact diff from the prior docs head:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldMayorNightDeathBranchingTest.kt

new test file only
83 additions
```

The tests lock:

```text
Mayor direct night death is a legal branch
Mayor redirect may choose every other stable seat
redirect to a dead player -> no death
redirect to a functioning Soldier -> no death
redirect to the functioning Monk-protected seat -> no death
redirect to an ordinary living player -> that player dies
redirect to the current Imp -> reuse Imp succession branching
redirected Poisoner death -> clear active poison state
```

### GREEN

```text
3236c7747941cf2feac416e095f3d5de0135a899
message: feat(a3): branch Mayor night-death worlds
CI #640 SUCCESS
R2 #573 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

RED -> GREEN exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldMayorNightDeathBranching.kt

new production file only
108 additions
RED tests unchanged
```

The new data shape is:

```text
EnumeratedWorldMayorNightDeathBranch(
  redirectTargetSeat: Int?,
  world: EnumeratedWorld,
)
```

`redirectTargetSeat == null` means the Mayor dies. A non-null redirect seat is generated from the current possible world and stable seat domain; it is **not** the persisted Storyteller Mayor resolution.

The helper requires an existing `MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED` attack branch, validates a living functioning current Mayor and exactly one living current Imp, then derives all legal outcomes.

For a redirect to the current Imp, H7.6 delegates to `EnumeratedWorldImpSelfKillSuccessionBranching` rather than treating the Demon as an ordinary death. This preserves the existing H7.4 succession transition and current-role state shape.

For a redirected Poisoner death, active `MALFUNCTIONING_POISONED` state is cleared consistently with historical/public/direct-death reducers.

No Storyteller-selected `Attack`, `Protect`, Mayor redirect/death choice, or `RoleChange` target is consumed.

## 6. Important current boundary

**H7.6 has not modified:**

```text
EnumeratedWorldOtherNightMechanicsMaterializer.kt
EnumeratedHistoricalExactBaseline.kt
EnumeratedHistoricalWorldReplay.kt
PlayerHistoricalTimeline.kt
```

Therefore the materializer still deliberately returns:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
```

inside `unresolvedBranches` at this checkpoint.

Do not describe Other Night materialization as complete until H7.7 integrates the standalone Mayor helper and focused tests prove unresolved branches disappear without dropping legal worlds.

## 7. Guards remain fail-closed

`EnumeratedHistoricalExactBaseline.build(...)` must still reject histories containing:

```text
Attack
Protect
RoleChange
```

Do not relax these guards in H7.7. Historical replay wiring and guard relaxation are a later, separate concern even if H7.7 completes the materializer.

Actual hidden Storyteller payloads remain forbidden as player possible-world truth.

## 8. Next possible slice — NOT AUTHORIZED / NOT STARTED

The next smallest slice is **H7.7 Mayor materializer integration**:

```text
MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED branch
-> EnumeratedWorldMayorNightDeathBranching
-> add Mayor-derived world(s) to resolvedWorlds
-> H3 mechanical convergence
-> unresolvedBranches empty when no other unsupported outcome exists
```

Expected scope:

```text
focused materializer tests
+
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedWorldOtherNightMechanicsMaterializer.kt
```

Do not rewrite the H7.6 helper unless a focused integration test exposes a real defect. Do not consume any actual hidden Storyteller Mayor redirect/death choice.

After H7.7, stop again. A later separate slice may evaluate wiring the now-complete Other Night transition into `EnumeratedHistoricalWorldReplay`, chronology timing, and exactly which `Attack` / `Protect` / `RoleChange` guards can safely move. Do not combine that work with H7.7.

Host / A4 / ZDD, other scripts, history UI/misinformation, and App-root S7 remain out of scope.

## 9. Validation discipline

1. recheck live `main` and PR #48 head/state/checks;
2. compare any docs-only head back to `3236c7747941cf2feac416e095f3d5de0135a899`;
3. keep H7.7 RED test-only;
4. prove the RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, and Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before any later replay/guard slice unless explicitly instructed.
