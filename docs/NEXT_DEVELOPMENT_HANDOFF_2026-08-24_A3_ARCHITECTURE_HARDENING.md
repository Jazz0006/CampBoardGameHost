# A3 Historical Multi-Night Exact Baseline — Current Handoff

> Date: 2026-08-24  
> Status: **ACTIVE / PR #48 DRAFT / DO NOT MERGE**  
> Immediate state: **H7.8 canonical Other Night replay transition GREEN / STOP before H7.9 public night-death reconciliation or guard relaxation**  
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

Stable `main` at H7.8 handoff time:

```text
84a062378f13b90ce71f3801982ba3b2d3b22d80
```

Latest fully validated **code** checkpoint:

```text
4bdf317ec16ab316331a2c322338446620e43631
CI #650 SUCCESS
R2 #583 SUCCESS
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
```

Public night-death reconciliation is still incomplete. Persisted Attack/Protect/RoleChange guards remain fail-closed. App-root S7 remains paused and must not be restarted in this A3 branch.

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

## 4. Complete current Other Night materializer

The standalone Trouble Brewing flow remains:

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

No Storyteller-selected `Attack`, `Protect`, Mayor resolution/death target, or `RoleChange` target is consumed.

## 5. H7.8 result to preserve

### Why the transition is not a dawn-only reducer

Trouble Brewing Other Night canonical order places the Imp before several visible ability slots, including Ravenkeeper, Empath and Fortune Teller. Therefore a post-Imp observation must be evaluated against a world in which the rule-derived Demon step has already occurred.

H7.8 uses the validated ruleset as an internal canonical-order substrate while preserving the visible GLOBAL timeline:

```text
DAY -> NIGHT
-> beginNight() poison expiration + rule-derived Poisoner branching
-> consume visible GLOBAL events
-> before first visible ability observation canonically after Imp:
     materialize Other Night mechanics exactly once
-> evaluate that visible observation against successor worlds
-> if no post-Imp visible ability observation occurs:
     materialize before NIGHT -> DAY
```

The hidden transition does not receive or advance `lastGlobalSequence`.

### Triggered Ravenkeeper semantics

The existing H2 exception allows a dead Ravenkeeper to be compatible with a night observation source, but that compatibility alone is not trigger proof. H7.8 adds transition evidence:

```text
Ravenkeeper night observation
-> source was alive immediately before this night's materialized Demon step
-> retain only successor worlds where source became dead during that step
-> then evaluate the received Ravenkeeper information
```

An already-dead Ravenkeeper cannot generate a later-night Ravenkeeper trigger merely because the role is present in setup history.

### RED

The first test creation commit:

```text
0456449ce4689674c84fe6472734314194e82c6e
```

contained only a test import-path mistake. The correction was also test-only:

```text
938aeb42196ee886c5fffe142c3dc3a839d37c3c
message: test(a3): fix H7.8 metric import
```

Treat `938aeb42196ee886c5fffe142c3dc3a839d37c3c` as the authoritative H7.8 RED checkpoint.

Net diff from the H7.7 docs head:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedHistoricalOtherNightTransitionIntegrationTest.kt

new test file only
190 additions
production changes = 0
```

The two tests lock:

```text
1. a normal Fortune Teller observation in its post-Imp slot sees already-materialized Demon mechanics,
   while the Fortune Teller source itself must still be alive;
2. a Ravenkeeper observation survives only worlds where that Ravenkeeper changed alive -> dead at
   this night's materialized Demon step.
```

CI #648 reached runtime tests:

```text
758 tests completed, 2 failed
exactly the two new H7.8 tests
```

ASP SUCCESS / Real Clingo SUCCESS / R2 #581 SUCCESS.

### GREEN

Two production commits:

```text
d3e227813fd012531073ebd9d649bc3827d00398
message: feat(a3): replay rule-derived other-night transition

4bdf317ec16ab316331a2c322338446620e43631
message: feat(a3): provide night-order authority to replay
```

RED -> GREEN exact production diff:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/epistemic/
  EnumeratedHistoricalWorldReplay.kt  +101 / -3
  EnumeratedHistoricalExactBaseline.kt +9 / -6

RED tests unchanged
```

`EnumeratedHistoricalExactBaseline` passes its existing `ValidatedClocktowerRuleset` into historical replay. `EnumeratedHistoricalWorldReplay` uses observation anchors and the per-world canonical Other Night schedule to identify the Imp boundary, materializes the complete H7.7 transition once, and then continues normal durable observation replay.

CI #650 SUCCESS / R2 #583 SUCCESS / Android + ASP + Real Clingo SUCCESS.

## 6. Important remaining blocker: public night-death reconciliation

H7.8 deliberately does **not** make the whole historical baseline exact yet.

Current `PublicDeath` replay still uses unconditional elimination semantics. That was safe before hidden night deaths were materialized, but is not sufficient afterward. Example failure mode:

```text
hidden possible-world branch already materializes seat 2 dying at the Demon step
GLOBAL history later contains PublicDeath(seat 2)
```

The public fact should confirm/select compatible hidden branches. It must not mechanically kill seat 2 again in every world or preserve branches where some different player already died and then also eliminate seat 2, producing impossible double-death states.

Therefore **do not describe end-to-end historical exactness as complete yet**.

## 7. Guards remain fail-closed

`EnumeratedHistoricalExactBaseline.build(...)` must still reject persisted:

```text
Attack
Protect
RoleChange
```

The rule-derived transition is now replayed without those hidden payloads. H7.8 does not authorize using the Storyteller-selected targets, and the guards were intentionally left in place.

## 8. Next possible slice — H7.9 NOT AUTHORIZED / NOT STARTED

The next smallest correctness slice should be **H7.9 public night-death reconciliation**.

Tests-first design should prove at minimum:

```text
public night death after a materialized Demon step
-> retains only worlds whose already-derived night outcome matches that public death
-> does not add a second death to an incompatible branch

ordinary day/public death semantics
-> continue to eliminate normally where no pre-materialized hidden night outcome is being reconciled
```

The slice should also determine how no-death nights are reconciled when no `PublicDeath` exists before day begins; do not guess by absence without a focused chronology contract.

Do not combine H7.9 with:

```text
Attack / Protect / RoleChange guard relaxation
Host integration
A4/ZDD promotion
other scripts
history UI / misinformation expansion
App-root S7
```

Only after public death reconciliation is proven should a separate guard-relaxation slice be considered.

## 9. Validation discipline

1. recheck live `main` and PR #48 head/state/checks;
2. compare docs-only head back to `4bdf317ec16ab316331a2c322338446620e43631`;
3. keep the next RED test-only;
4. prove RED is the intended semantic failure;
5. keep GREEN production diff minimal;
6. exact-compare RED -> GREEN;
7. wait for CI, R2, ASP, Real Clingo;
8. recheck PR remains open/draft/not merged;
9. stop before subsequent guard/Host work unless explicitly instructed.
