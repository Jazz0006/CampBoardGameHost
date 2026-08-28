# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-28 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
main baseline: c8985cb4991f6c7e5ea02adedb932d2d86452da1
active branch: codex/clocktower-same-night-effective-state-correctness
PR: #54
PR policy: draft / open / unmerged until explicit authorization
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

## 2. Current priority — GCR Global Correctness Review Follow-up

Status:

```text
GCR     STATUS
GCR-1   Current Demon authority / cross-night succession         GREEN / ACCEPTED
GCR-2   Poisoned Spy fail-safe information policy                GREEN / ACCEPTED
GCR-3   Typed production acceptance + source-string retirement   GREEN / ACCEPTED
GCR-4   Chambermaid actual wake-history authority                DEFERRED FOLLOW-UP
GCR-5   Durable identity + reconstruction API hardening          DEFERRED FOLLOW-UP
```

Current handoff:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-28_GLOBAL_CORRECTNESS_FOLLOWUP.md
```

SNE-7 remains closed. GCR was a separate follow-up campaign and must not be used to reopen accepted same-night/Dawn work without a new typed regression.

## 3. GCR-1 — Current Demon authority — ACCEPTED

Accepted correctness:

```text
Night N:   Imp0 self-kills -> Imp1 becomes current Demon
Night N+1: Imp1 acts normally
Later:     Imp1 can self-kill -> Imp2 becomes current Demon
```

Historical dead Demon role identity remains represented correctly. Current authority derives from the single live Demon and fails closed if the live-Demon invariant is violated.

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

## 5. GCR blocker full acceptance — COMPLETE

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

This is the authoritative full T4 checkpoint for GCR-1/GCR-2 production behavior.

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

Therefore a second T4 is not required for GCR-3: the production tree is identical to the production tree already validated at `474103ed`, while the final slimmed test tree has its own successful FAST/R2 acceptance.

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

Historical SNE execution handoffs are consolidated under:

```text
docs/archive/SNE7_AND_PRE_GCR_HANDOFF_CLOSEOUT_2026-08-28.md
```

## 8. Deferred work registry

These are intentionally **not blockers for PR #54**:

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

Do not create production seams solely to reduce source-string test count when the remaining guard is already coarse and the extraction has no independent architecture benefit.

## 10. Validation policy

Follow `docs/TESTING_STRATEGY.md`.

Behavior-changing correctness work:

```text
T0 focused typed RED
-> assertion-level RED provenance
-> minimal GREEN
-> focused affected regressions
-> T1 :app:testFast
-> T2/T3 when required
-> T4 before merge-blocking production closure
```

A skipped, cached-only or `UP-TO-DATE` route is not evidence that a required gate executed.

## 11. Scope / branch discipline

Until explicit authorization:

- keep PR #54 draft/open/unmerged;
- do not mark ready;
- do not merge;
- do not rebase/force-push;
- do not mix deferred GCR-4/5, A3, S9.2, A4/ZDD or recommendation work into this accepted campaign;
- preserve complete-worktree safety for `CampBoardGameHostApp.kt` / `ClocktowerHostScreen.kt`.

## 12. Current next action

**The GCR-1/GCR-2/GCR-3 campaign is accepted. No further correctness implementation is required on PR #54 before a user decision.**

PR #54 should remain draft/open/unmerged until explicit authorization. The next action is a release/PR decision: either keep the accepted checkpoint as-is for review, explicitly mark ready, or explicitly merge. Do not begin GCR-4/5 inside this PR unless a new regression proves one of them blocks the accepted behavior.
