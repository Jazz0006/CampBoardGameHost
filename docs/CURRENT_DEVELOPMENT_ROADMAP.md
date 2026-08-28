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
last audited head before blocker fixes: e9a30ffc353d870df388329986be433268558661
```

Current accepted GCR checkpoints:

```text
GCR-1 executable acceptance:
974f617adffd08cc7de0924f6fea4f96f3d73f0c

GCR-1 + GCR-2 full production acceptance:
474103ed13caaf34a329ca5e80e2f0ba64963b86

GCR-3 final test-quality acceptance:
383ad0e695656124f9dc608fd5ce06b72de6b499
```

These checkpoints remain valid evidence for the behavior they exercised. They are no longer sufficient merge evidence for the current PR because post-acceptance review found two additional runtime P1 regressions not covered by those tests.

## 2. Current priority — PR #54 post-acceptance runtime blockers

Status:

```text
PR54-P1-1  First-night Fortune Teller incorrectly enters Other Night projection   CONFIRMED / BLOCKING / FIX FIRST
PR54-P1-2  Imp self-kill pending successor cannot reconstruct Host safely         CONFIRMED / BLOCKING / FIX SECOND
```

### PR54-P1-1 — First-night Fortune Teller projection crash

Observed production path:

```text
First Night
-> Fortune Teller selects two players
-> Host constructs Fortune Teller OTHER_NIGHT interaction id
-> Host calls effectiveNightStateAt(...)
-> otherNightCanonicalInteractionIds is empty on First Night
-> ClocktowerEffectiveNightState rejects unknown interaction
-> crash
```

Required contract:

```text
First Night
-> Fortune Teller uses base/current persisted role state
-> must not enter Other Night chronology projection

Other Night
-> Fortune Teller continues to use canonical same-night effective-state projection
```

Do **not** weaken the effective-state projector to accept unknown interactions. The caller must select the correct authority for the current phase.

Implementation order:

```text
1. typed focused RED proving First Night does not evaluate the Other Night projection provider;
2. minimal production GREEN;
3. focused Fortune Teller + effective-state + First/Other Night regressions;
4. exact diff audit before moving to PR54-P1-2.
```

### PR54-P1-2 — Imp self-kill pending successor reconstruction crash

Observed production lifecycle:

```text
Imp self-kill confirmed
-> old Imp becomes mechanically dead
-> pendingNewDemonName is set
-> phase remains Night
-> Host recomposes before successor identity confirmation completes
-> there is temporarily no living Demon card
-> Host requires non-null current living Demon role for reconstruction
-> crash before new-Demon identity confirmation UI
```

Required contract:

- the pending succession window may legally contain no currently living Demon card;
- succession reconstruction must obtain the role authority from the confirmed attacker / current-night historical Demon authority, or otherwise route the pending-confirmation state before requiring a living Demon;
- do not keep the old Imp alive merely to avoid the crash;
- do not prematurely durably mutate the successor into Demon before confirmation/materialization;
- do not turn the invariant into an unprincipled nullable fallback.

PR54-P1-2 must remain a separate micro-slice after PR54-P1-1 is GREEN.

## 3. GCR-1 — Current Demon authority — ACCEPTED WITH NEW PENDING-SUCCESSION REGRESSION OUTSIDE PRIOR COVERAGE

Accepted correctness remains:

```text
Night N:   Imp0 self-kills -> Imp1 becomes current Demon
Night N+1: Imp1 acts normally
Later:     Imp1 can self-kill -> Imp2 becomes current Demon
```

Historical dead Demon role identity remains represented correctly. Current authority derives from the single live Demon after succession has been completed.

The newly discovered PR54-P1-2 concerns the **transient pending-confirmation window before the successor becomes the completed current living Demon**. It does not invalidate the accepted cross-night repeated-succession behavior, but it blocks the PR until the missing lifecycle case is covered and fixed.

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

## 5. GCR blocker full acceptance — HISTORICAL CHECKPOINT, NOT CURRENT MERGE GATE

Full production-tree acceptance checkpoint:

```text
474103ed13caaf34a329ca5e80e2f0ba64963b86
```

Evidence:

```text
CI #963 / run 33175600756 SUCCESS
- Android :app:testFull + :app:assembleDebug SUCCESS
- ASP contract tests SUCCESS
- Real Clingo cross-validation SUCCESS
- CI gate SUCCESS

R2 run 33175600749 SUCCESS
```

This remains the authoritative T4 evidence for the GCR-1/GCR-2 behavior actually covered at that checkpoint. It is not current merge authorization because PR54-P1-1 and PR54-P1-2 are newly identified runtime paths outside that coverage.

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

## 7. SNE-7 — CLOSED / BROAD GREEN, WITH NEW CALLER REGRESSION TO FIX

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

PR54-P1-1 is a caller phase-selection regression: First Night must not be forced through an Other Night chronology. The projector invariant itself remains protected and should not be relaxed.

Historical SNE execution handoffs are consolidated under:

```text
docs/archive/SNE7_AND_PRE_GCR_HANDOFF_CLOSEOUT_2026-08-28.md
```

## 8. Deferred work registry

These remain deferred and are **not** the two current PR blockers:

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

For PR54-P1-1, prefer a small callable typed phase-authority seam that can prove First Night does not invoke the Other Night projection provider. Do not add a source-string gameplay assertion as the primary RED.

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

## 11. Scope / branch discipline

Until explicit authorization:

- keep PR #54 open/unmerged;
- do not merge;
- do not rebase/force-push;
- fix PR54-P1-1 first, then PR54-P1-2 as a separate micro-slice;
- do not mix deferred GCR-4/5, A3, S9.2, A4/ZDD or recommendation work into these fixes;
- preserve complete-worktree safety for `CampBoardGameHostApp.kt` / `ClocktowerHostScreen.kt`;
- do not weaken the shared effective-state projector merely to make First Night tolerate an invalid Other Night interaction id.

## 12. Current next action

**PR #54 is blocked by two newly confirmed runtime P1 regressions.**

Current execution order:

```text
1. PR54-P1-1 Fortune Teller First Night projection crash
   -> typed RED
   -> minimal GREEN
   -> focused regression validation
   -> remote diff audit

2. PR54-P1-2 Imp self-kill pending-successor Host reconstruction crash
   -> separate typed RED/GREEN slice

3. after both blockers are GREEN
   -> logical checkpoint validation
   -> refresh merge-readiness evidence
```

Do not merge PR #54 until both blockers are fixed and the required validation is green.
