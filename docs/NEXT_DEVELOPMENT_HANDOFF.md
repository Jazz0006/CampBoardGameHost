# NEXT DEVELOPMENT HANDOFF — Real-device UI polish, then ADB diagnosis

> Updated: 2026-09-15 Australia/Sydney  
> Status: **CURRENT / canonical active handoff**  
> Product-code baseline after merged PR #132: `ce1925c5ab70416c6b46981538ab91726e5f8a4d`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. query live `main`, open PRs and checks.

Do not use dated handoffs in `docs/archive/` as execution authority.

## 1. Immediate goal

Continue real-device acceptance with this order:

1. finish UI optimizations reported from device testing;
2. fix the remaining small reproducible bugs found during the same pass;
3. only after that pass is complete, return to the deferred Pair-information display latency / old OPPO abnormal-exit investigation using ADB evidence.

Do not resume EPI-MQ or another broad architecture campaign unless the user explicitly reprioritizes.

## 2. Recently completed work

The following Experienced-mode UI sequence is merged to `main`:

```text
PR #130  EXPERIENCED-UI-1: compact Trouble Brewing host surfaces
PR #131  EXPERIENCED-UI-2: compact night information surfaces
PR #132  EXPERIENCED-UI-3: device-tested information refinements
```

Stable outcomes include:

- compact Imp kill, Demon succession, Scarlet Woman succession, Virgin, Slayer and Mayor event surfaces;
- shared Beginner-style wake hierarchy for Experienced mode without creating a second gameplay pipeline;
- Minion group wake rows/highlights;
- compact Poisoner, Butler, Monk, Spy, Ravenkeeper and Undertaker interaction surfaces;
- Washerwoman/Librarian/Investigator Pair candidates use the same selected style, no 1/2 markers;
- `✓` is Storyteller-facing truth and includes typed Spy/Recluse registration hits;
- Fortune Teller Red Herring is a persistent yellow host-only corner badge;
- Chef/Empath recommended and alternative information are separated for one-tap Storyteller use;
- shared `RegistrationHint` palette distinguishes Spy/Recluse registration context;
- Empath decorates only authoritative living-neighbour `subjectSeats`;
- Undertaker player reveal uses the unified executed-player + large-role template;
- compact Imp succession includes `选择继任小恶魔的爪牙`.

Do not redo these changes unless new device evidence identifies a concrete defect.

## 3. Presentation/architecture invariants

Experienced mode is not another rules or state pipeline.

Preserve:

- Beginner and Experienced share one gameplay/rules/legal-candidate/recommendation/session/persistence pipeline;
- presentation consumes typed projections and submits intent;
- Highlight = current waking / acted-on / selected player;
- `✓` = actual truth or typed registration truth hit, not reliability;
- yellow corner badge = persistent special Storyteller state such as Fortune Teller Red Herring;
- `RegistrationHint` = Spy/Recluse host assistance, visually closer to evil but distinct from normal evil;
- manual Experienced alternatives must come from the same authoritative legal/recommendation owner;
- do not parse presentation strings to reconstruct gameplay semantics.

Use the efficient Beginner surface as the default Experienced visual base: remove teaching explanation, retain or add only Storyteller-required truth and legal choice assistance.

## 4. Immediate implementation style

For each new UI/bug report:

1. reproduce/locate the true owner;
2. distinguish presentation issue from gameplay/rules/state issue;
3. reuse existing typed data rather than introducing UI-side inference;
4. make the smallest coherent change;
5. use risk-appropriate focused tests;
6. for executable checkpoints run `:app:testFast`; add `:app:assembleDebug` when a device-test APK is needed;
7. inspect exact diff and keep unrelated programs out.

Potential role surfaces for later consistency review — **not confirmed defects** — include Chambermaid, Clockmaker, Flowergirl, Town Crier, Sage and other No Greater Joy roles. Do not manufacture work for them without evidence.

## 5. Deferred issue — Pair display stalls before fullscreen

Observed on real devices:

- on Pair-information steps such as Washerwoman, tapping `展示` can leave the current screen apparently unresponsive for several seconds before player fullscreen appears;
- on an older OPPO phone, an abnormal app exit has also occurred around this transition;
- this has happened more than once.

Current inspection indicates the Pair button itself hands an already-resolved display option upward. The suspicious critical-path ordering is that authoritative apply work happens before player fullscreen state is installed. Heavy recomputation, recovery snapshot construction, serialization/persistence, GC/memory pressure, an exception, ANR or process kill are all still possible. **No root cause is confirmed.**

Do not optimize this path by guess while the current UI/small-bug pass is still active.

## 6. ADB investigation plan after UI pass

First reproduce without adding diagnostic code:

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

If raw ADB evidence is insufficient, add a small debug-only diagnostic slice with timestamp markers around:

```text
DISPLAY_CLICK
APPLY_START / APPLY_DONE
SESSION_COMMIT_DONE
RECOVERY_SNAPSHOT_START / DONE
PERSIST_START / DONE
DISPLAY_STATE_SET
PLAYER_DISPLAY_COMPOSED / first frame
```

The diagnostic branch should not change gameplay behavior. Do not hide a blocked main thread behind a loading animation.

## 7. Completed architecture campaigns

Do not treat old handoffs as pending work:

- EXPERIENCED-NIGHT-FLOW-1 is complete via PR #123;
- GLOBAL-OWNERSHIP-CLEANUP Steps 1-6 are complete via PRs #124-#129;
- the old Step 3/4/5/6 handoffs are archived historical checkpoints;
- EPI-MQ-0.5 is paused, not current.

The main long-lived ownership audit remains:

`docs/GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`

## 8. Paused program

EPI-MQ remains queued. When explicitly resumed, start with:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

The previous dated EPI-MQ handoff is archived and no longer active.

## 9. Unrelated work

PR #109 (`Reproduce restored execution preflight crash`) was an unrelated diagnostic draft at this handoff point. Re-query its live state before touching it. Do not merge or combine it with current UI/device work without a separate decision.

## 10. Large-file and test rules

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

If a file cannot be fully retrieved safely, follow `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`; never replace a large/truncated file from partial content.

Do not add brittle source-string tests for callback spelling/local variable/render-function shape when a typed behavior/presentation seam can protect the invariant.

## 11. First action in the next conversation

After reading the four canonical documents and checking live `main`, ask for no historical recap if the user has supplied a concrete new device/UI finding. Continue directly with the next UI optimization or small bug.

Only after the user indicates that UI/small-bug testing is sufficiently complete should the work switch to the ADB diagnostic plan in section 6.

## 12. Stable rule

> **Finish the current real-device UI and small-bug pass first. Then diagnose the Pair-display stall/old-OPPO exit with ADB evidence before changing the performance-critical path.**
