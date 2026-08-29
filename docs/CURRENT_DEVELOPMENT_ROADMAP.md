# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-29 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
main baseline: c8985cb4991f6c7e5ea02adedb932d2d86452da1
active branch: codex/clocktower-same-night-effective-state-correctness
PR: #54
PR policy: open / unmerged until explicit authorization
last audited pre-blocker head: e9a30ffc353d870df388329986be433268558661
PR54-P1-1 accepted checkpoint: f97276139ab0123329c1e73c0380048c5ac98e3d
PR54-P1-2 production wiring checkpoint: 8a6f8cea481edbaef8a5946abb19ef0d0f483005
current full T4 acceptance checkpoint: 8e0f62c059c814ad744514f7213040ad8bc74119
```

Current accepted GCR checkpoints:

```text
GCR-1 executable acceptance:
974f617adffd08cc7de0924f6fea4f96f3d73f0c

GCR-1 + GCR-2 historical full production acceptance:
474103ed13caaf34a329ca5e80e2f0ba64963b86

GCR-3 final test-quality acceptance:
383ad0e695656124f9dc608fd5ce06b72de6b499
```

Those historical checkpoints remain valid evidence for the behavior they exercised. Post-acceptance review later found two additional runtime P1 regressions outside their coverage; both have now been fixed and accepted. A fresh full-tree T4 run at `8e0f62c0...` is the current PR merge-readiness evidence.

## 2. PR #54 post-acceptance runtime blockers — CLOSED / ACCEPTED

Status:

```text
PR54-P1-1  First-night Fortune Teller incorrectly enters Other Night projection   FIXED / ACCEPTED
PR54-P1-2  Imp self-kill pending successor cannot reconstruct Host safely         FIXED / ACCEPTED
```

There is no remaining known PR54 runtime P1 blocker from this closeout campaign.

### PR54-P1-1 — First-night Fortune Teller projection crash — FIXED / ACCEPTED

Original production path:

```text
First Night
-> Fortune Teller selects two players
-> Host constructs Fortune Teller OTHER_NIGHT interaction id
-> Host calls effectiveNightStateAt(...)
-> otherNightCanonicalInteractionIds is empty on First Night
-> ClocktowerEffectiveNightState rejects unknown interaction
-> crash
```

Accepted contract:

```text
First Night
-> Fortune Teller uses base/current persisted role state
-> must not enter Other Night chronology projection

Other Night
-> Fortune Teller continues to use canonical same-night effective-state projection
```

The effective-state projector remains fail-closed for unknown interactions. The caller now selects the correct authority for the current phase.

Acceptance evidence:

```text
Historical behavioral RED checkpoint:
779a58ea83ef2fc1a07aae67e63b929454c0c8ab
CI #987 / run 33220660973 FAILURE as expected
- production and tests compiled successfully
- :app:testFast executed
- 907 tests completed, 1 failed
- failing test: ClocktowerFortuneTellerPhaseAuthorityTest
- failure: First Night evaluated the Other Night provider

Minimal production GREEN:
11cc2c78b336f11e7bb9722b1913c2cfabe8d109
- exactly one commit after the RED checkpoint
- production diff limited to ClocktowerHostCoreSemantics.kt and ClocktowerHostScreen.kt
- First Night uses base role authority
- Other Night uses lazy canonical effective-state projection

Strengthened typed regression checkpoint:
f97276139ab0123329c1e73c0380048c5ac98e3d
CI #989 / run 33221389915 SUCCESS
- Android :app:testFast actually executed and passed
- CI gate SUCCESS
R2 #916 / run 33221389931 SUCCESS
```

The strengthened typed regression covers Fortune Teller Demon-match semantics in both directions: First Night base Demon/non-Demon without evaluating Other Night projection, and Other Night projected role overriding the base role.

### PR54-P1-2 — Imp self-kill pending successor reconstruction crash — FIXED / ACCEPTED

Observed failure lifecycle:

```text
Imp self-kill confirmed
-> old Imp becomes mechanically dead
-> pending successor exists
-> phase remains Night
-> Host recomposes before successor identity confirmation completes
-> there is temporarily no living Demon card
-> old Host required living-Demon authority for reconstruction
-> crash before new-Demon identity confirmation UI
```

Accepted authority split:

```text
living-Demon UI authority
-> resolveCurrentDemonHostContext()
-> remains living-only

current-night transaction/reconstruction authority
-> resolveNightReconstructionDemonRoleId(...)
-> exactly one live Demon when available
-> zero-live pending succession may recover the explicit confirmed dead Demon attacker
-> multiple-live-Demon ambiguity remains fail-closed

canonical succession requirement
-> resolveNightDemonSuccessionForHost(...)
-> delegates to existing Trouble Brewing Imp self-kill semantics

canonical Other Night ordering
-> clocktowerOtherNightWakingRoleIds(...)
-> normal nights use living current roles + Drunk shown role
-> pending succession retains the historical Imp only as a canonical ordering anchor
```

The old Imp remains mechanically dead, the successor is not role-mutated before confirmation, and `NightTransactionReconstructor` remains fail-closed and unchanged.

Tests-first evidence:

```text
reconstruction-role RED:
5dcf91e0...
- dead old Imp / zero living Demon
- expected historical Imp role authority

pending-succession RED:
b9cb90d3...
- old Imp dead
- pending successor present
- expected canonical successor Choice

ordering-anchor behavioral RED:
5ba1c162b8c3c7eb40b5d4ecd01a9dbd935c51b5
CI #1000 / run 33222642480
- production/test compilation succeeded
- :app:testFast executed
- 914 tests completed, 1 failed
- only failure: pending succession retains historical Imp ordering anchor after old Imp is dead
```

Typed seam GREEN checkpoint:

```text
13a5fb03a9e5d49200f0516cbe9db16d3b6f0a11
CI #1001 / run 33223172126 SUCCESS
- Android FAST unit tests SUCCESS
- Real Clingo cross-validation SUCCESS
- CI gate SUCCESS
R2 #928 / run 33223172160 SUCCESS
```

Final Host wiring checkpoint:

```text
8a6f8cea481edbaef8a5946abb19ef0d0f483005
commit: fix: wire pending Imp succession into night host
```

Remote exact-diff audit from `13a5fb03...`:

```text
ahead_by: 1
behind_by: 0
changed files: exactly 1
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
23 additions / 50 deletions
```

Host wiring now uses one canonical transaction role for succession resolution, canonical interaction planning, `NightTransactionReconstructor`, and resolved Demon mechanical-death interaction identity. `demonCard` remains the living-Demon UI wake/display authority.

T1 evidence:

```text
CI #1002 / run 33223959281 SUCCESS
- Android :app:testFast SUCCESS
- CI gate SUCCESS
R2 #929 / run 33223959279 SUCCESS
```

## 3. GCR-1 — Current Demon authority — ACCEPTED

Accepted correctness:

```text
Night N:   Imp0 self-kills -> Imp1 becomes current Demon
Night N+1: Imp1 acts normally
Later:     Imp1 can self-kill -> Imp2 becomes current Demon
```

Historical dead Demon role identity remains represented correctly. Current living-Demon authority derives from the single live Demon after succession has completed.

The previously missing transient pending-confirmation window is now covered by PR54-P1-2: while the old Imp is already dead and before the successor is materialized as Demon, current-night transaction/reconstruction authority can still preserve the canonical Imp role without pretending a living Demon exists.

Accepted executable checkpoint:

```text
974f617adffd08cc7de0924f6fea4f96f3d73f0c
```

Evidence:

```text
CI #959 / run 33174380352 SUCCESS
R2 run 33174380336 SUCCESS
```

## 4. GCR-2 — Poisoned Spy fail-safe information policy — ACCEPTED

Product decision, 2026-08-28:

The app intentionally does **not** implement fabricated/misleading Grimoire generation for a poisoned Spy. This is an intentional product simplification / house-rule deviation from official poisoned-information semantics.

Accepted policy:

```text
healthy Spy
-> wake normally
-> show the true Grimoire

poisoned Spy
-> wake normally
-> show no Grimoire information
-> Host may explicitly identify the poisoned state to the Storyteller
-> do not create/persist a Spy Grimoire information observation
```

The interaction-shape difference is intentional and must not be reopened as a correctness defect unless the product policy is deliberately changed later.

No fake-Grimoire or generic misinformation subsystem was introduced.

## 5. PR #54 current full acceptance — GREEN

Current full-tree acceptance checkpoint:

```text
8e0f62c059c814ad744514f7213040ad8bc74119
```

This is a docs-only `[full-ci]` checkpoint directly after production head `8a6f8cea...`, so it validates the same production tree while forcing all full gates.

Routing evidence:

```text
Full checkpoint selected.
Routing: android=true android_full=true asp=true oracle=true
```

Evidence:

```text
CI #1003 / run 33224102399 SUCCESS
- Android :app:testFull + :app:assembleDebug SUCCESS
- FAST route skipped as expected for the full checkpoint
- ASP contract tests SUCCESS
- Real Clingo cross-validation SUCCESS
- CI gate SUCCESS

R2 #930 / run 33224102366 SUCCESS
```

This supersedes the older `474103ed...` checkpoint as **current PR merge-readiness evidence**. It does not itself authorize merge; PR #54 remains open/unmerged until explicit user authorization.

Historical GCR-1/GCR-2 full checkpoint:

```text
474103ed13caaf34a329ca5e80e2f0ba64963b86
CI #963 / run 33175600756 SUCCESS
R2 run 33175600749 SUCCESS
```

## 6. GCR-3 — source-string retirement — ACCEPTED

GCR-3 audited correctness-adjacent source-string tests under this classification:

```text
A. typed replacement already proves behavior -> retire
B. behavior matters but no callable production seam exists -> narrow seam only if worthwhile
C. architecture/ownership-only invariant -> keep coarse source guard
D. obsolete implementation-shape assertion -> delete
```

Result:

- gameplay semantics remain owned by typed tests;
- remaining source inspection is coarse App/Host ownership only;
- callback-local variable names, statement order, role lists, exact UI text and gameplay-result source assertions were removed where redundant;
- no production file was changed during GCR-3;
- Dawn materializer seam extraction was deliberately deferred because it would add production risk solely to remove already-coarse ownership guards.

Final GCR-3 test checkpoint:

```text
383ad0e695656124f9dc608fd5ce06b72de6b499
```

Evidence:

```text
CI #980 / run 33177405639 SUCCESS
- Android FAST unit tests SUCCESS
- CI gate SUCCESS

R2 run 33177405675 SUCCESS
```

Exact post-T4 audit:

```text
474103ed -> 383ad0e6
17 commits
13 modified test files
1 added audit doc
0 production files
```

Detailed audit:

```text
docs/GCR3_SOURCE_STRING_RETIREMENT_AUDIT_2026-08-28.md
```

## 7. SNE-7 — CLOSED / BROAD GREEN

Earlier accepted executable SNE-7 full checkpoint:

```text
70935644daf5c06985420f19833dbda3a160bbfa
```

Durable contracts to preserve:

- `ClocktowerNightCheckpoint` owns unfinished-night durable state;
- GameState + action/observation timelines own durable historical truth;
- mechanical death and public death announcement are distinct;
- stable durable IDs do not depend on mutable revisions/event counters;
- current role/effective state must be read at the correct same-night interaction point;
- Dawn replay/retry must be idempotent and exactly-once at history level.

The later First Night Fortune Teller caller phase-selection regression has been fixed without relaxing the projector invariant, and pending Imp succession reconstruction has been fixed without weakening Demon authority invariants.

Historical SNE execution handoffs are consolidated under:

```text
docs/archive/SNE7_AND_PRE_GCR_HANDOFF_CLOSEOUT_2026-08-28.md
```

## 8. Deferred work registry

These remain deferred and are **not** current PR blockers:

| Deferred area | Status |
|---|---|
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED FOLLOW-UP |
| GCR-5 reconstructor naming clarity | DEFERRED FOLLOW-UP |
| Dawn systematic crash cut-point matrix | DEFERRED FOLLOW-UP |
| A3 immutable setup snapshot ownership/persistence | NOT STARTED |
| App Root S9.2 Active Game Persistence Boundary | AUDITED / NOT STARTED |
| generic custom-script Demon succession | NOT AUTHORIZED |
| Mayor redirect to Demon with generic succession | DELIBERATELY CONSTRAINED |
| Host/A4/ZDD recommendation promotion | NOT AUTHORIZED |
| history UI / generic misinformation tuning | NOT CURRENT |

Each deferred item must be re-audited against live `main` before implementation.

## 9. Source-string test policy

Gameplay/rules correctness must be typed.

Long-term source inspection is allowed only for coarse architecture/ownership assertions where there is no callable seam. Source-string tests must not freeze incidental implementation details or substitute for rules behavior.

The PR54 runtime blocker closeout reinforced this policy: both First Night Fortune Teller phase authority and pending Imp succession authority were established with typed behavioral seams; Host source diff inspection was used only as an independent wiring audit, not as the primary correctness proof.

## 10. Validation policy

Follow `docs/TESTING_STRATEGY.md`.

Behavior-changing correctness work:

```text
T0 focused typed RED
-> assertion-level RED provenance
-> minimal GREEN
-> focused affected regressions
-> T1 :app:testFast at the logical blocker checkpoint
-> T2/T3 when required
-> T4 before merge-blocking production closure
```

A skipped, cached-only or `UP-TO-DATE` route is not evidence that a required gate executed.

PR54 runtime closeout has now completed both T1 and fresh T4 validation.

## 11. Scope / branch discipline

Until explicit authorization:

- keep PR #54 open/unmerged;
- do not merge;
- do not rebase/force-push;
- PR54-P1-1 and PR54-P1-2 are both closed/accepted;
- do not start deferred GCR-4/5, A3, S9.2, A4/ZDD or recommendation work on this branch merely because the blockers are closed;
- preserve complete-worktree safety for `CampBoardGameHostApp.kt` / `ClocktowerHostScreen.kt`;
- preserve the accepted authority split between living-Demon UI state and current-night transaction/reconstruction state.

## 12. Current next action

**PR #54 has no remaining known runtime P1 blocker from the post-acceptance closeout, and the fresh T4 merge-readiness gate is green.**

Current state:

```text
P1-1 Fortune Teller runtime blocker       CLOSED / ACCEPTED
P1-2 pending Imp succession blocker        CLOSED / ACCEPTED
T1 FAST checkpoint                         GREEN
T4 full Android + APK                       GREEN
ASP contract                                GREEN
Real Clingo                                 GREEN
R2                                           GREEN
PR #54                                      OPEN / UNMERGED
```

Next action requires explicit user direction. Reasonable options are:

```text
A. authorize final PR #54 merge/readiness action;
B. keep PR #54 open and return to the previously paused A3 architecture-hardening line;
C. keep PR #54 open and resume another explicitly selected deferred workstream on a separate branch.
```

Do not merge PR #54 without explicit user authorization.
