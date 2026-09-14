# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-14 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition                               COMPLETE / merged
D6 decomposition campaign                         COMPLETE
UI-R5 square-table convergence                    COMPLETE / merged via PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / merged via PR #118
ROLE-ROTATION-1 recent role rotation              COMPLETE / merged via PR #119
UX-MODE-1 Beginner / Experienced mode             COMPLETE / merged via PR #120
UI-INFO-1 information filtering & layout          COMPLETE / merged via PR #121
Dead-player square-table marking                  COMPLETE / merged via PR #122

EXPERIENCED-NIGHT-FLOW-1 navigation correctness   COMPLETE / merged via PR #123; device pass is follow-up
GLOBAL-OWNERSHIP-CLEANUP steps 1-2                COMPLETE / merged via PRs #124/#125
GLOBAL-OWNERSHIP-CLEANUP step 3                   COMPLETE / merged via PR #126
GLOBAL-OWNERSHIP-CLEANUP step 4                   COMPLETE / merged via PR #127
GLOBAL-OWNERSHIP-CLEANUP step 5                   COMPLETE / merged via PR #128
GLOBAL-OWNERSHIP-CLEANUP step 6                   IMPLEMENTED — local gates pass; remote acceptance pending
EPI-MQ / Productive Uncertainty                   PAUSED / queued after flow correctness
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign documents are historical evidence, not default execution authority.

## 2. Immediate priority — cleanup step 6

Campaign authority:

`docs/GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`

Steps 1-5 are merged. Step 6 is implemented on `codex/global-ownership-cleanup-6` from merged `main`
`0aad20e3`; local focused, `testFast`, `testFull`, Debug assembly and diff gates pass. Its bounded
seams are the shared phase conversion, immutable night-host mechanical projection outside Compose,
and the common square-table scaffold/seat bridge. Role-specific UI, mutable lifecycle state, rules
algorithms and persistence remain with their current owners. The live fan-out, architecture
pre-flight, implementation result and evidence are recorded in section 13 of the campaign audit.
The remaining work is remote PR/CI/R2 acceptance and explicit user merge authorization.

Historical starting `main` for EXPERIENCED-NIGHT-FLOW-1:

`940ba1df68365974ba366985d66d1cb583682ba7`

Real-device Experienced-mode testing exposed a severe regression:

> after the Demon selects/confirms a night kill target, the app can enter a black screen with no usable Previous or Next navigation.

The read-only audit indicates this should be treated as a night-navigation / rendering ownership defect, not as a DemonKill-screen-only bug.

The campaign-level invariant is:

> **After every legal night confirmation, the app must resolve to exactly one of two states: a valid renderable next night step, or an explicit night-completion/Dawn transition. There is no third out-of-range, blank, or ownerless UI state.**

## 3. EXPERIENCED-NIGHT-FLOW-1 implementation sequence

```text
S1  Night navigation ownership / reproduced black-screen regression
    - remove dependence on out-of-range stored night-step cursor
    - unify post-confirm forward resolution around typed ownership
    - establish smallest durable typed RED before production fix

S2  Full-screen surface totality
    - every claimed full-screen night step must resolve to a concrete surface
    - cover both Beginner and Experienced modes
    - preserve shared Activity-root navigation scaffold ownership
    - typed ownership implementation accepted at 19ecaf37
    - remote CI/testFast and exact scope audit passed at 6088af21

S3  Skilled interaction eligibility
    - required single-target actions enable confirmation only for a valid current selection
    - consume upstream legality; do not reimplement legality in presentation
    - producer/consumer fan-out audited and shared derived contract accepted at c9faee03
    - remote full Android CI and R2 passed

S4  Experienced night-flow regression matrix
    - ordinary Demon attack -> Dawn
    - Mayor redirect manual branch
    - Imp self-kill -> Demon successor continuation
    - Previous/edit/reconfirm
    - restore/reconstruction
    - representative target/pair/numeric/manual-information steps
    - Beginner regression over the same shared owners
    - real-device acceptance
    - automated matrix and draft-toggle correction accepted at 4d340c57
    - full Android/ASP/Clingo/CI and R2 passed; field-test APK/device checklist pending
```

## 4. Ownership findings that are now roadmap constraints

Existing SNE/UI contracts remain authoritative:

- `ClocktowerNightCheckpoint.nightStepIndex` remains the sole stored night navigation position;
- `NightCheckpointReducer` / `NightCheckpointHostTransaction` remain the typed checkpoint/Host transition boundary;
- durable mechanics/history/persistence remain owned by the existing checkpoint/session/App transaction boundaries;
- global navigation/scaffold code remains presentation, not a second gameplay owner;
- Beginner and Experienced share rules, legality and recommendation pipelines.

The current implementation has a high-risk split in which dynamic forward advance can depend on Host/Compose deferred state while Previous already consumes the typed transaction boundary. It can also temporarily represent a next position outside the currently renderable step list and rely on later recomposition/effect resolution.

Do not preserve this implementation shape merely because an older source-string/wiring test expects it.

## 5. Scope fences

EXPERIENCED-NIGHT-FLOW-1 may:

- correct night navigation ownership;
- correct dynamic post-confirmation resolution;
- establish full-screen render totality;
- correct shared confirmation eligibility for required target actions;
- add typed regression/integration evidence;
- make the smallest Host/App wiring changes required to consume corrected typed seams;
- narrow/retire superseded implementation-shaped tests.

It must not:

- create a DemonKill-only special-case fix when the defect belongs to shared night navigation;
- create separate Beginner/Experienced gameplay pipelines;
- change gameplay rules or legal candidate semantics without an independently proven rule bug;
- redesign recommendation ranking;
- resume EPI-MQ in the same branch;
- perform broad UI visual redesign;
- perform another general Host/App decomposition campaign;
- move durable ActionFact/history/Dawn materialization/persistence authority for navigation convenience;
- introduce Navigation Compose or another phase coordinator.

## 6. Testing and implementation policy

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

S1 is a genuine reproduced regression gap, so the default evidence order is:

```text
stable invariant
-> smallest durable typed T0 RED
-> minimal production fix
-> exact T0 GREEN
-> git diff --check
-> remote exact diff/scope audit
```

At logical checkpoints run T1 `:app:testFast` plus triggered T2/T3 evidence. Final campaign acceptance requires real-device validation because the original defect is a device-visible navigation/rendering failure.

Do not create source-string tests for callback spelling, local variables, `LaunchedEffect` structure, or other implementation shape. Existing source tests that encode the defective deferred/out-of-range design must be reclassified after durable typed coverage exists; narrow or retire them when they no longer protect a unique architecture invariant.

Shared-contract changes must follow the AGENTS fan-out gate: map all production producers/consumers, classify must-inherit vs intentional exemption, and verify both Beginner/Experienced plus fresh/restored paths where applicable.

## 7. Repository writing rule for the current Work environment

Use local file edits, normal Git commits and authenticated terminal `git push`. The user explicitly
directed this campaign not to use Connector writes or a one-shot GitHub workflow for large files.
Verify exact diff and full-file integrity before every push; never replace a large source file from
a truncated read or partial representation.

## 8. EPI-MQ status — paused, not cancelled

EPI-MQ-0.5 remains the next algorithm/architecture program after the live-game flow regression is closed.

Existing resume handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-13_EPI_MQ_0_5.md`

Primary architecture/audit reference:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

Its prior design remains valid unless the later live delta audit proves otherwise. Do not mix epistemic capability work into the current navigation-correctness branch.

Intended EPI-MQ sequence remains:

```text
EPI-MQ-0.5  dynamic-script extensibility guard
EPI-MQ-1    neutral hypothetical observation evaluator
EPI-MQ-2    credibility / immediate contradiction / impairment-exposure gates
EPI-MQ-3+   productive-uncertainty metrics and ranking
```

## 9. UI campaign continuity

UI-INFO-1 remains accepted and closed. Its stable outcomes still include:

- Beginner guidance hierarchy;
- Drunk shown-role square-table routing without changing actual-role truth ownership;
- shared Activity-root Night/Day fullscreen/navigation ownership;
- square-table ownership for real unreliable-information steps;
- renderability guards for transient invalid table geometry;
- automatic Beginner Demon succession continuation;
- removal of unnecessary standalone Beginner Mayor redirect presentation while preserving Mayor semantics;
- restored Dawn host-facing announcement/review surface;
- explicit Storyteller confirmation before entering Day.

The newly reproduced Experienced-mode black-screen regression is an independently reproduced defect, so reopening the affected night-flow ownership boundary is permitted without reopening the completed UI-INFO-1 campaign as a whole.

## 10. Live-state notes

Recorded live state at the Step 4 implementation checkpoint:

```text
main: b77560628054dee90c78a91d4bab8cb4185fda59 (PR #126 merged)
PR #124: MERGED as 4cd49d4279a176b9e32a51db4606f0e801e57ceb
PR #125: MERGED as d6773a2e14ea5242c9fa432893d40f3a566ec625
PR #126: MERGED as b77560628054dee90c78a91d4bab8cb4185fda59
Step 4: codex/global-ownership-cleanup-4, based exactly on current origin/main
PR #127: Draft / mergeable / code head fec12982 / full CI, Real Clingo and R2 boundary passed
```

Unrelated open draft PR:

```text
#109 — Reproduce restored execution preflight crash
branch: codex/execution-restore-crash-repro
```

Do not modify or merge PR #109 as part of the ownership-cleanup campaign.

Always re-query live refs/checks at the start of the next conversation.

## 11. Default reading order for the next development conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`;
5. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-14_GLOBAL_OWNERSHIP_CLEANUP_STEP_6.md`;
6. the Step 6 phase adapter, night-host projection, square-table scaffold and owning tests;
7. query live `main`, working branch and PR/check status;
8. complete the exact remote diff, CI and R2 acceptance audit; do not merge without explicit user
   authorization and do not begin EPI-MQ in the Step 6 PR.

## 12. Stable rule

> **Current roadmap + one active handoff define what happens next. Host presentation consumes one
> immutable mechanical projection and must not reconstruct unfinished-night mechanics in Compose.**
