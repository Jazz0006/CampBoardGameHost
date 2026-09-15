# NEXT DEVELOPMENT HANDOFF — Pair-display ADB diagnosis

> Updated: 2026-09-15 Australia/Sydney  
> Status: **CURRENT / canonical active handoff**  
> Product-code baseline after merged PR #133: `154e0c7f92a1e4007f4590b1105bfb5bc2b4f897`  
> PR #134 closes the current UI-polish checkpoint; after merge, query live `main` and use that merge SHA as the new baseline.

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. query live `main`, open PRs and checks.

Do not use dated handoffs in `docs/archive/` as execution authority.

## 1. Immediate goal

The current real-device UI/small-bug pass is closed at the PR #134 checkpoint.

The next phase is:

1. reproduce the Pair-information player-display stall on a real device;
2. capture ADB/logcat, crash, memory and frame evidence before changing behavior;
3. identify the actual synchronous critical path or failure mode;
4. if raw evidence is insufficient, add narrowly scoped debug-only timing markers;
5. make the smallest evidence-backed correction;
6. verify on the affected device(s), including the older OPPO if available.

Do not resume EPI-MQ, UX-R6 or another broad architecture campaign unless the user explicitly reprioritizes.

## 2. UI-polish checkpoint completed through #134

Recent merged/ready sequence:

```text
PR #130  EXPERIENCED-UI-1: compact Trouble Brewing host surfaces
PR #131  EXPERIENCED-UI-2: compact night information surfaces
PR #132  EXPERIENCED-UI-3: device-tested information refinements
PR #133  EXPERIENCED-UI-4: effective poison marker + real Top-3 recommendations
PR #134  DAY-UI-1: keep daytime domain actions in square-table center
```

Stable outcomes include:

- compact Imp kill, Demon succession, Scarlet Woman succession, Virgin, Slayer and Mayor event surfaces;
- shared Beginner-style wake hierarchy for Experienced mode without creating a second gameplay pipeline;
- Minion group wake rows/highlights;
- compact Poisoner, Butler, Monk, Spy, Ravenkeeper and Undertaker interaction surfaces;
- Washerwoman/Librarian/Investigator Pair candidates use the same selected style, no 1/2 markers;
- `✓` is Storyteller-facing truth and includes typed Spy/Recluse registration hits;
- Fortune Teller Red Herring is a persistent yellow host-only corner badge;
- current effective poison state is shown on the player seat without changing legality;
- Experienced information surfaces show up to three real ranked recommendations first, then legal manual alternatives, with no synthetic padding;
- shared `RegistrationHint` palette distinguishes Spy/Recluse registration context;
- Empath decorates only authoritative living-neighbour `subjectSeats`;
- Undertaker player reveal uses the unified executed-player + large-role template;
- compact Imp succession includes `选择继任小恶魔的爪牙`;
- daytime nomination hint no longer starts with `自由讨论 / Open discussion`;
- nomination-to-vote, vote confirmation and Slayer resolution live in the square-table center rather than the bottom `Next` slot.

Do not redo these changes unless new device evidence identifies a concrete defect.

## 3. Stable presentation/architecture invariants

Experienced mode is not another rules or state pipeline.

Preserve:

- Beginner and Experienced share one gameplay/rules/legal-candidate/recommendation/session/persistence pipeline;
- presentation consumes typed projections and submits intent;
- Highlight = current waking / acted-on / selected player;
- `✓` = actual truth or typed registration truth hit, not reliability;
- yellow corner badge = persistent special Storyteller state such as Fortune Teller Red Herring;
- poison marker = current effective poison state projected for the relevant step; presentation only, never interaction legality;
- `RegistrationHint` = Spy/Recluse host assistance, visually closer to evil but distinct from normal evil;
- Experienced recommendations = up to three actual ranked recommendations, followed by manual legal choices; never pad with fabricated candidates;
- manual Experienced alternatives come from the same authoritative legal/recommendation owner;
- do not parse presentation strings to reconstruct gameplay semantics;
- bottom navigation is for navigation/utility; in-table domain actions should not be relabeled as `Next` merely to fit the scaffold.

Use the efficient Beginner surface as the default Experienced visual base: remove teaching explanation, retain or add only Storyteller-required truth and legal choice assistance.

## 4. Deferred issue now becomes CURRENT — Pair display stalls before fullscreen

Observed on real devices:

- on Pair-information steps such as Washerwoman, tapping `展示` can leave the current screen apparently unresponsive for several seconds before player fullscreen appears;
- on an older OPPO phone, an abnormal app exit has also occurred around this transition;
- this has happened more than once.

Current inspection indicates the Pair button itself hands an already-resolved display option upward. The suspicious critical-path ordering is that authoritative apply work happens before player fullscreen state is installed. Heavy recomputation, recovery snapshot construction, serialization/persistence, GC/memory pressure, an exception, ANR or process kill are all still possible. **No root cause is confirmed.**

Do not optimize this path by guess.

## 5. First-pass ADB investigation

Reproduce without adding diagnostic code first.

```bash
adb devices
adb logcat -c
adb logcat -v threadtime > botc_washerwoman.log
```

If the app exits abnormally:

```bash
adb logcat -b crash -v threadtime > botc_crash.log
adb bugreport
```

Capture memory around reproduction:

```bash
adb shell dumpsys meminfo <package>
```

Capture frame/jank information:

```bash
adb shell dumpsys gfxinfo <package> reset
# reproduce
adb shell dumpsys gfxinfo <package> framestats
```

Inspect for:

```text
FATAL EXCEPTION
AndroidRuntime
OutOfMemoryError
ANR
GC / allocation pressure
Skipped frames / Choreographer
ActivityManager process death
low-memory / LMK evidence
```

Record the device model / Android version and whether the symptom is a visible stall, ANR, process death, Activity recreation, or crash.

## 6. If raw ADB evidence is insufficient

Add only a small debug-only diagnostic slice with timestamp/thread markers around:

```text
DISPLAY_CLICK
APPLY_START / APPLY_DONE
SESSION_COMMIT_START / DONE
RECOVERY_SNAPSHOT_START / DONE
PERSIST_START / DONE
DISPLAY_STATE_SET
PLAYER_DISPLAY_COMPOSED / first frame
```

Requirements:

- diagnostics must not change gameplay behavior;
- do not move authority merely to improve timing;
- capture main-thread duration explicitly;
- use one monotonic clock source for elapsed timings;
- keep diagnostic output removable after root cause is found;
- do not add a loading animation as a substitute for evidence.

## 7. Likely code-path audit scope

Only after reproducing, inspect the path from Pair-information display callback through:

1. selected/resolved `ClocktowerDisplayOption` submission;
2. authoritative apply/commit;
3. derived projection/recommendation recomputation triggered synchronously;
4. recovery snapshot construction;
5. serialization/persistence;
6. fullscreen player-display state installation;
7. first player-display composition/frame.

Do not assume any one item is expensive until timing/log evidence supports it.

## 8. Validation for the eventual fix

At minimum:

```bash
./gradlew :app:testFast --no-daemon --build-cache
./gradlew :app:assembleDebug --no-daemon --build-cache
```

Then repeat the same real-device reproduction and compare:

- display-click-to-fullscreen latency;
- logcat exceptions/ANR/process-death evidence;
- memory before/after;
- frame timing/jank;
- fresh and restored session behavior if the fix touches commit/recovery/persistence seams.

If the fix changes a shared contract rather than only scheduling/performance, expand tests according to `docs/TESTING_STRATEGY.md`.

## 9. Completed architecture campaigns

Do not treat old handoffs as pending work:

- EXPERIENCED-NIGHT-FLOW-1 is complete via PR #123;
- GLOBAL-OWNERSHIP-CLEANUP Steps 1-6 are complete via PRs #124-#129;
- the old Step 3/4/5/6 handoffs are archived historical checkpoints;
- EPI-MQ-0.5 is paused, not current.

The main long-lived ownership audit remains:

`docs/GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`

## 10. Paused programs

EPI-MQ remains queued. When explicitly resumed, start with:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

UX-R6 recommendation-provider replacement also remains queued and must not be mixed into the current latency investigation.

## 11. Unrelated work

PR #109 (`Reproduce restored execution preflight crash`) is unrelated unless its live state has changed. Re-query before touching it. Do not merge or combine it with the Pair-display investigation without a separate decision.

## 12. Large-file and test rules

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

If a file cannot be fully retrieved safely, follow `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`; never replace a large/truncated file from partial content.

Do not add brittle source-string tests for callback spelling/local variable/render-function shape when a typed behavior/presentation seam can protect the invariant.

## 13. First action in the next conversation

After reading the canonical documents and checking live `main`, start directly with the ADB Pair-display investigation. Do not ask for a historical recap.

If the user can reproduce on-device, collect the logs/evidence above first. If device evidence has already been supplied, analyze it before proposing code changes.

## 14. Stable rule

> **PR #134 closes the current UI-polish checkpoint. The next phase is evidence-first diagnosis of the Pair-information display stall / old-device abnormal exit, followed by the smallest verified fix.**
