# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-15 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition                               COMPLETE / merged
D6 decomposition campaign                         COMPLETE
UI-R5 square-table convergence                    COMPLETE / PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / PR #118
ROLE-ROTATION-1 recent role rotation              COMPLETE / PR #119
UX-MODE-1 Beginner / Experienced mode             COMPLETE / PR #120
UI-INFO-1 information filtering & layout          COMPLETE / PR #121
Dead-player square-table marking                  COMPLETE / PR #122
EXPERIENCED-NIGHT-FLOW-1 correctness              COMPLETE / PR #123
GLOBAL-OWNERSHIP-CLEANUP steps 1-6                COMPLETE / PRs #124-#129
EXPERIENCED-UI-1 compact TB host surfaces         COMPLETE / PR #130
EXPERIENCED-UI-2 night information surfaces       COMPLETE / PR #131
EXPERIENCED-UI-3 device-feedback refinements      COMPLETE / PR #132

CURRENT:
Real-device UI polish + small bug fixes

NEXT:
ADB diagnosis of Pair-information display latency / old-device abnormal exit

PAUSED / QUEUED:
EPI-MQ-0.5 dynamic-script extensibility guard
EPI-MQ-1+ productive-uncertainty work
UX-R6 recommendation-provider replacement
```

Product-code baseline after PR #132 merge:

`ce1925c5ab70416c6b46981538ab91726e5f8a4d`

Documentation-only closeout commits after that SHA do not change the product baseline. Always query live `main` before editing.

Completed campaign documents are historical evidence, not current execution authority.

## 2. Immediate priority — finish UI polish and small real-device bugs

The next development conversation should continue the current real-device acceptance pass before starting another architecture campaign.

Priorities:

1. apply concrete UI feedback found during device testing;
2. fix small reproducible UI/interaction bugs discovered in the same pass;
3. keep changes narrowly owned by presentation/interaction unless evidence proves a deeper defect;
4. preserve Beginner/Experienced shared gameplay, legality, recommendation, session and persistence owners;
5. run risk-appropriate focused tests and `:app:testFast` for executable changes, plus `assembleDebug` when preparing a device-test build.

Do not reopen completed #130-#132 work merely to redesign it. Treat new device findings as new focused defects/refinements.

Potential audit targets, **not confirmed defects**, include remaining role families such as Chambermaid, Clockmaker, Flowergirl, Town Crier, Sage and other No Greater Joy surfaces, plus generic wake/Drunk shown-role/Red-Herring consistency. Only work on them when device review or a focused audit identifies a concrete UX/correctness gap.

## 3. Stable Experienced-mode presentation contract

Experienced mode is not a second visual or gameplay system. It should reuse the efficient Beginner interaction structure, remove teaching prose, and add only Storyteller-facing truth/choice assistance.

Stable semantics:

- highlight = player currently waking, acted on or selected;
- `✓` = Storyteller-facing actual truth / typed registration truth hit;
- yellow corner badge = persistent special truth/state the Storyteller must remember, e.g. Fortune Teller Red Herring;
- `RegistrationHint` palette = Spy/Recluse host registration hint, visually closer to evil but distinct from normal good and ordinary evil;
- Experienced manual alternatives consume the same legal candidate/recommendation authority as Beginner automation.

Recent accepted behavior from #130-#132 includes compact kill/event surfaces, shared wake hierarchy, Minion group highlighting, Pair-information convergence, concise Chef/Empath/Spy/Ravenkeeper/Undertaker surfaces, persistent Fortune Teller Red Herring indication, typed Spy/Recluse truth cues, separated recommended/alternative numeric information, and compact Imp succession guidance.

## 4. Deferred performance/stability investigation — ADB first

A real-device issue remains intentionally deferred until the current UI/small-bug pass is complete:

> On Pair-information steps such as Washerwoman, tapping the player-display button can leave the current screen unresponsive for several seconds before the fullscreen player display appears. On an older OPPO device, an abnormal app exit has also been observed around this transition.

This is **not yet root-caused**. Current code inspection suggests the button already owns a resolved display option, while the upper callback applies authoritative state before the fullscreen display state is installed. Heavy synchronous work in that critical path — projection/recommendation recomputation, recovery snapshot construction, serialization/persistence, GC/memory pressure, or another failure — remains a hypothesis, not a conclusion.

When this investigation resumes, collect evidence before changing behavior:

```text
adb devices
adb logcat -c
adb logcat -v threadtime > botc_washerwoman.log

# if the app exits abnormally
adb logcat -b crash -v threadtime > botc_crash.log
adb bugreport

# before/after reproduction
adb shell dumpsys meminfo <package>
adb shell dumpsys gfxinfo <package> reset
adb shell dumpsys gfxinfo <package> framestats
```

Look for `FATAL EXCEPTION`, `AndroidRuntime`, `OutOfMemoryError`, ANR, GC pressure, skipped frames/Choreographer, ActivityManager and low-memory/process-kill evidence.

If ADB evidence does not identify the critical section, add debug-only timing markers around display click, apply/commit, recovery snapshot/persist, display-state installation and first fullscreen composition. Do not add a loading animation as a substitute for root-cause evidence.

## 5. Architecture constraints that remain in force

- Beginner and Experienced share one authoritative gameplay/rules/legal-candidate/recommendation/session/persistence pipeline.
- UI/presentation consumes typed projections and submits intents; it does not recreate legality or registration rules.
- `ClocktowerNightCheckpoint.nightStepIndex` remains the stored night-navigation position and completed night-flow invariants remain in force.
- Host/App may coordinate lifecycle and rendering, but complete phase translation, unfinished-night mechanical composition and repeated square-table shell behavior each have one explicit owner.
- Unsupported epistemic semantics must remain explicit (`DEFERRED != UNSAT`) when EPI-MQ resumes.

## 6. Testing and repository-writing policy

Follow:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` when a file cannot be safely retrieved/replaced in full.

Use risk-based evidence. Do not manufacture rules tests for presentation-only changes. Prefer typed behavior/integration seams over source-shape assertions. Shared-contract changes require producer/consumer fan-out and Beginner/Experienced plus fresh/restored checks where applicable.

For normal Android checkpoints:

```text
./gradlew :app:testFast --no-daemon --build-cache
```

For device-test builds, add `:app:assembleDebug` as appropriate.

## 7. Paused programs

EPI-MQ is paused, not cancelled. Its architecture reference remains:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

Planned sequence remains:

```text
EPI-MQ-0.5  generic/dynamic-script capability boundary
EPI-MQ-1    neutral hypothetical observation evaluator
EPI-MQ-2    credibility / contradiction / impairment-exposure gates
EPI-MQ-3+   productive-uncertainty metrics and ranking
```

Do not resume it until the user explicitly finishes/reprioritizes the current UI/device-stability pass.

## 8. Unrelated open diagnostic work

PR #109 — `Reproduce restored execution preflight crash` — remains an unrelated diagnostic draft unless its live status has changed. Do not merge or fold it into UI/device work without a separate decision.

## 9. Next-conversation reading order

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. query live `main` and open PR/check state;
6. continue concrete real-device UI/small-bug feedback first;
7. return to the ADB Pair-display investigation only after that pass is complete.

Historical dated handoffs are archived and are not execution authority.

## 10. Stable rule

> **Current roadmap + `docs/NEXT_DEVELOPMENT_HANDOFF.md` define what happens next. Finish the real-device UI/small-bug pass first; diagnose the deferred Pair-display latency with ADB evidence before attempting a performance fix.**
