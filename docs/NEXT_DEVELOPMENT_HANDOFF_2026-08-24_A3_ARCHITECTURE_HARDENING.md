# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.9 public night-death reconciliation GREEN / STOP before no-public-death dawn reconciliation or guard relaxation**  
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

Stable `main` at H7.9 handoff time:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Latest fully validated **code** checkpoint:

```text
a81b5949e00b18a24f4f1b0522a3f41aa892efa1
CI #658 SUCCESS
R2 #591 SUCCESS
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
  H7.8 GREEN  canonical Other Night transition wired into historical replay
  H7.9 GREEN  public night-death reconciliation
```

The next known exactness blocker is no-public-death/dawn reconciliation. Persisted Attack/Protect/RoleChange guards remain fail-closed. App-root S7 remains paused and must not be restarted in this A3 branch.

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

## 4. Other Night replay baseline now in place

The standalone Trouble Brewing materializer remains:

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

H7.8 inserts this complete transition at the canonical Imp boundary relative to visible ability observations:

```text
DAY -> NIGHT
-> beginNight() poison expiration + rule-derived Poisoner branching
-> replay visible GLOBAL events
-> before first visible ability observation canonically after Imp:
     materialize Other Night mechanics once
-> evaluate that observation against successor worlds
-> if no post-Imp visible observation occurs:
     materialize before NIGHT -> DAY
```

No synthetic hidden timeline point is created. Ravenkeeper information additionally requires alive -> dead transition proof at that night's materialized Demon step.

## 5. H7.9 result to preserve

### Problem solved

Before H7.9, once H7.8 had already materialized a hidden night death, a later durable `PublicDeath(target)` still called unconditional `eliminate(target)`. That could preserve a world where another seat had already died and then kill `target` as well, creating an impossible double-death state.

H7.9 uses a public NIGHT death as outcome evidence when it is compatible with the rule-derived hidden transition. It never consumes an actual Storyteller `Attack` or `Protect` target.

### RED

```text
6fad4ed5f51590ab884af88437b711a494c2e3c9
message: test(a3): lock public night-death reconciliation
```

Exact diff from the prior docs head:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedHistoricalPublicNightDeathReconciliationTest.kt

new test file only
227 additions
production changes = 0
```

The tests prove both orderings:

```text
1. post-Imp visible observation first, then PublicDeath(target)
   -> public death filters the already-materialized hidden outcomes
   -> no second death is added

2. PublicDeath(target) is the first post-Imp public evidence
   -> materialize hidden mechanics first
   -> retain only target alive -> dead successor worlds
```

CI #656 reached runtime tests:

```text
760 tests completed, exactly 2 failed
exactly the two new H7.9 tests
```

ASP SUCCESS / Real Clingo SUCCESS / R2 #589 SUCCESS.

### First GREEN attempt and H6 regression

```text
6626d9f97c43b901b60b17385ec791d52fa12274
message: fix(a3): reconcile public night deaths
```

This made the H7.9 tests pass but CI #657 exposed exactly one pre-existing H6 regression:

```text
EnumeratedHistoricalIncrementalReplayTest
H6 ordinary night observation is evaluated against state at its global point
```

The scenario has an ordinary Empath observation at GLOBAL sequence 8 and that Empath's public death at sequence 9. The observation proves the Empath was alive at its own point, so the later public death cannot be retroactively forced into the earlier canonical Imp step.

R2 #590 / ASP / Real Clingo remained green.

### Final GREEN

```text
a81b5949e00b18a24f4f1b0522a3f41aa892efa1
message: fix(a3): preserve later public night deaths
CI #658 SUCCESS
R2 #591 SUCCESS
Android SUCCESS
ASP SUCCESS
Real Clingo SUCCESS
```

From RED `6fad4ed5...` to final GREEN `a81b5949...`:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedHistoricalWorldReplay.kt

+97 / -24
RED tests unchanged
```

The correction from the first GREEN attempt to final GREEN is only `+6/-1` in the same production file.

### Final reconciliation contract

`EnumeratedHistoricalWorldReplay` now retains a transient pre-mechanics snapshot for the current night.

```text
PublicDeath(target) before mechanics have materialized
-> materialize with target as PUBLIC death confirmation
-> retain target alive -> dead successor worlds

PublicDeath(target) after mechanics have materialized
-> re-materialize from the saved pre-transition snapshot with target confirmation
-> mechanically intersect with the current observation-filtered successor worlds
-> explanation clusters/provenance do not participate in identity

if the intersection is non-empty
-> public death confirms/selects the hidden night outcome
-> do not kill target again

if the intersection is empty because earlier visible GLOBAL evidence proves the death happened later
-> keep current worlds
-> apply public elimination at the PublicDeath event's own GLOBAL point
```

This preserves H3 mechanical identity, H6 state-at-global-point semantics, and H7.8 canonical hidden-transition timing simultaneously.

## 6. Guards remain fail-closed

`EnumeratedHistoricalExactBaseline.build(...)` must still reject persisted:

```text
Attack
Protect
RoleChange
```

H7.9 does not authorize using Storyteller-selected hidden targets. Do not relax these guards merely because public death reconciliation is now implemented.

## 7. Next possible slice — NOT AUTHORIZED / NOT STARTED

The next known exactness question is **what `NIGHT -> DAY` means when no durable `PublicDeath` occurred that night**.

Current replay may still retain materialized death branches even when the durable visible history contains no night death before the day transition. A separate tests-first slice should determine whether phase advancement itself provides the public “no one died tonight” fact and how that constrains hidden Other Night outcomes.

Tentative name:

```text
H7.10 no-public-death / dawn reconciliation
```

Do not infer no-death merely from absence until this chronology contract is locked by RED.

Do not combine H7.10 with:

```text
Attack / Protect / RoleChange guard relaxation
Host integration
A4/ZDD promotion
other scripts
history UI / misinformation expansion
App-root S7
```

Only after both positive public-death and no-public-death outcomes are reconciled should a separate guard-relaxation slice be considered.

## 8. Validation discipline

1. recheck live `main` and PR #48 head/state/checks;
2. compare docs-only head back to `a81b5949e00b18a24f4f1b0522a3f41aa892efa1`;
3. keep the next RED test-only;
4. prove RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before subsequent guard/Host work unless explicitly instructed.

Documentation should be updated automatically only at meaningful architecture/phase checkpoints; do not create a docs commit for every small code change.
