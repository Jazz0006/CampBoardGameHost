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
EXPERIENCED-UI-4 poison + ranked recommendations  COMPLETE / PR #133
DAY-UI-1 centered daytime domain actions          COMPLETE / PR #134

CURRENT:
ADB diagnosis of Pair-information display latency / old-device abnormal exit

NEXT:
Evidence-driven fix of the confirmed Pair-display critical path, followed by real-device verification

PAUSED / QUEUED:
EPI-MQ-0.5 dynamic-script extensibility guard
EPI-MQ-1+ productive-uncertainty work
UX-R6 recommendation-provider replacement
```

Product-code baseline after PR #133 merge:

`154e0c7f92a1e4007f4590b1105bfb5bc2b4f897`

PR #134 closes the current daytime/UI-polish checkpoint. After it merges, query live `main` and use that merge SHA as the next product baseline.

Completed campaign documents are historical evidence, not current execution authority.

## 2. Immediate priority — diagnose Pair-information display latency with evidence

The real-device UI polish checkpoint is complete through PR #134. The next development phase is the deferred Pair-information display latency / old-device abnormal-exit investigation.

Observed symptom:

> On Pair-information steps such as Washerwoman, tapping the player-display button can leave the current screen unresponsive for several seconds before the fullscreen player display appears. On an older OPPO device, an abnormal app exit has also been observed around this transition.

This is **not yet root-caused**. Current code inspection suggests the button already owns a resolved display option, while the upper callback applies authoritative state before the fullscreen display state is installed. Heavy synchronous work in that critical path — projection/recommendation recomputation, recovery snapshot construction, serialization/persistence, GC/memory pressure, or another failure — remains a hypothesis, not a conclusion.

Start with device evidence before changing behavior:

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

If raw ADB evidence does not identify the critical section, add debug-only timing markers around display click, apply/commit, recovery snapshot/persist, display-state installation and first fullscreen composition. Do not add a loading animation as a substitute for root-cause evidence.

## 3. Stable presentation contracts after the UI-polish checkpoint

Experienced mode is not a second visual or gameplay system. It reuses the efficient Beginner interaction structure, removes teaching prose, and adds only Storyteller-facing truth/choice assistance.

Stable semantics:

- highlight = player currently waking, acted on or selected;
- `✓` = Storyteller-facing actual truth / typed registration truth hit;
- yellow corner badge = persistent special truth/state the Storyteller must remember, e.g. Fortune Teller Red Herring;
- poison marker = current effective poison state projected for the relevant step; it is visual state only and must not change interaction legality;
- `RegistrationHint` palette = Spy/Recluse host registration hint, visually closer to evil but distinct from normal good and ordinary evil;
- Experienced recommendation surfaces show up to three **real ranked recommendations** first, followed by legal manual alternatives; never synthesize padding candidates;
- Experienced manual alternatives consume the same legal candidate/recommendation authority as Beginner automation.

Recent accepted behavior from #130-#134 includes compact kill/event surfaces, shared wake hierarchy, Minion group highlighting, Pair-information convergence, concise Chef/Empath/Spy/Ravenkeeper/Undertaker surfaces, persistent Fortune Teller Red Herring indication, typed Spy/Recluse truth cues, effective poison marking, Top-3 ranked recommendation presentation, and daytime domain actions kept in the square-table center instead of being mislabeled as bottom-bar “Next” navigation.

## 4. Daytime action ownership checkpoint

PR #134 establishes a stable UI ownership rule for the current Day workspace:

- bottom navigation is for actual navigation/utility actions;
- nomination-to-vote, vote confirmation and Slayer resolution are domain actions and belong in the square-table center;
- the daytime nomination hint no longer contains the redundant `自由讨论 / Open discussion` prefix;
- moving these controls did not change nomination, voting, Slayer, session or rules state machines;
- the shared bottom action bar exposes visibility as presentation configuration rather than forcing every domain action into its `Next` slot.

Do not regress these domain actions back into bottom navigation merely for layout convenience.

## 5. Architecture constraints that remain in force

- Beginner and Experienced share one authoritative gameplay/rules/legal-candidate/recommendation/session/persistence pipeline.
- UI/presentation consumes typed projections and submits intents; it does not recreate legality or registration rules.
- `ClocktowerNightCheckpoint.nightStepIndex` remains the stored night-navigation position and completed night-flow invariants remain in force.
- Host/App may coordinate lifecycle and rendering, but complete phase translation, unfinished-night mechanical composition and repeated square-table shell behavior each have one explicit owner.
- Visual state and interaction eligibility remain separate contracts.
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

Do not resume it until the user explicitly reprioritizes away from the current device-stability investigation.

## 8. Unrelated open diagnostic work

PR #109 — `Reproduce restored execution preflight crash` — remains unrelated unless its live status has changed. Do not merge or fold it into the Pair-display investigation without a separate decision.

## 9. Next-conversation reading order

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. query live `main` and open PR/check state;
6. begin the Pair-information display latency / old-device abnormal-exit investigation with ADB evidence;
7. only after evidence identifies a likely critical section, make the smallest diagnostic or corrective code change.

Historical dated handoffs are archived and are not execution authority.

## 10. Stable rule

> **The current UI-polish checkpoint ends with PR #134. Next, diagnose the Pair-display latency/old-device exit with ADB evidence first; do not guess at a performance fix or resume a broad architecture campaign.**
