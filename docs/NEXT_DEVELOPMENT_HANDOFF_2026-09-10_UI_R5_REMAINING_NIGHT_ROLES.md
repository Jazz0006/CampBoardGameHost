# NEXT DEVELOPMENT HANDOFF — UI-R5 Remaining Night-role Square-table Convergence

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — SAGE IMPLEMENTED / LEGACY RETIREMENT NEXT**  
> Scope: Storyteller night-role square-table UI convergence, legacy-surface retirement, and real-device stabilization  
> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117

## 1. Read first

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/TESTING_STRATEGY.md`;
5. `docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`;
6. `ClocktowerSageStepMaterializer.kt`;
7. current pair-manual / generic NightStep presentation owners before legacy retirement.

D6/R3 are closed; do not reopen them by default.

## 2. Live branch / checkpoints

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
latest full-T4 verified head: c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
Ravenkeeper checkpoint: 06e01ec7c0410412b38d104f4a5f72bc72811ffe
Spy checkpoint: d45f96ddcdb1142a422d64ee87cf61c5475121f9
Clockmaker verified head: 9119ec83f036432ec9b5f0f3a920690ab9719e16
Sage materializer owner: d82e1254bdb413445adb1fc2e66bb4401eea719e
Sage Host delegation: b9a67a4dc7a336d1a8693f76aa383646e3c063d7
Sage one-shot cleanup head: 053461b7d9868056593ca7a5e3ce2d8e68eb6330
```

PR #117 remains open, Draft and unmerged. Do not merge without explicit user authorization.

## 3. Sage ownership extraction — implemented

The Sage slice is now split by ownership rather than left inside the protected Host.

Final dependency direction:

```text
ClocktowerHostScreen
  - owns current Demon / Sage death trigger / effective ability state / card order
  - passes prepared facts only
        |
        v
ClocktowerSageStepMaterializer
  - owns Sage pair candidate projection
  - owns Sage recommendation/display metadata
  - owns presentationSubjectSeats
  - builds the Sage information step through ClocktowerInformationStepBuilder
        |
        v
ClocktowerNightStepUi / ClocktowerSageSquareTableUi
  - owns result projection and rendering
```

Preserved invariants:

- Host/session/canonical flow remain authoritative for whether Sage was actually killed by the Demon;
- Host remains authoritative for current Demon and effective death-trigger ability state;
- `ClocktowerInformationStepBuilder` still owns generic reliable/unreliable/manual/automatic information-step mechanics;
- unreliable Sage choices remain `proposition = null`;
- typed `presentationSubjectSeats` are presentation-only and are not converted into epistemic truth;
- no parsing of `displaySecondary` is used to recover seat identity;
- global `clocktowerInformationCandidateId(...)` remains unchanged;
- no other role materializer was extracted as part of this slice.

The old Host-local `recommendedSageOptions(...)` function is removed. Host now contains one narrow call to `clocktowerSageStepMaterializer(...)` while keeping `sageNightDeath` and `sageDeathTriggerAbilityState` locally authoritative.

## 4. Sage validation evidence

One-shot run:

```text
run: 34429902491
job: 102722989741
result: PASS
```

Executed evidence:

```text
focused Sage tests:
./gradlew :app:testDebugUnitTest \
  --tests 'com.codex.campboardgamehost.ClocktowerSageStepMaterializerTest' \
  --tests 'com.codex.campboardgamehost.ClocktowerSageSquareTablePresentationTest' \
  --tests 'com.codex.campboardgamehost.ClocktowerSageSquareTableWiringTest' \
  --rerun-tasks
BUILD SUCCESSFUL
26 actionable tasks: 26 executed

Android FAST:
./gradlew :app:testFast --rerun-tasks
BUILD SUCCESSFUL
26 actionable tasks: 26 executed
```

The one-shot also passed:

- exact trigger HEAD / source blob checks;
- `git diff --check`;
- exact production changed-file allowlist;
- Host absence check for `recommendedSageOptions`;
- exactly one `clocktowerSageStepMaterializer(` Host delegation;
- preservation checks for `sageNightDeath` and `sageDeathTriggerAbilityState`;
- automatic removal of the temporary workflow and patch script.

Independent remote diff audit from the post-preflight clean head `7ca02b9c...` to `053461b7...` shows only the intended persistent Sage files:

- added `ClocktowerSageStepMaterializer.kt`;
- modified `ClocktowerHostScreen.kt`;
- added `ClocktowerSageStepMaterializerTest.kt`;
- modified `ClocktowerSageSquareTableWiringTest.kt`.

No temporary workflow/script survives in the final tree.

## 5. Checkpoint CI/R2 note

The product and cleanup commits were authored by `github-actions[bot]`. Normal PR CI/R2 runs created for cleanup head `053461b7...` reported `action_required` with zero jobs, so those runs are **not** accepted as checkpoint validation and are not treated as test failures.

This docs-only connector-authored checkpoint exists specifically to trigger the normal PR CI/R2 gate on the same production tree. Accept Sage as the per-role automated checkpoint only after those normal runs complete successfully.

A new full T4 is still due only after Sage + legacy retirement / final reachability cleanup unless a concrete risk requires earlier escalation.

## 6. Next implementation target — retire unreachable pair-manual UI

The next slice is legacy retirement, not another role migration.

`ClocktowerPairManualSelectionDialog` is no longer called by current NightStep UI, but the file still owns helpers used by the accepted pair-information square table.

Retire only the unreachable UI:

- `ClocktowerPairManualSelectionDialog`;
- `ClocktowerPairManualCenterControls`.

Retain or relocate as needed:

- `clocktowerPairManualSquareTableSeat(...)`;
- `clocktowerPairManualSeatState(...)`;
- `ClocktowerPairManualSelectionModel`;
- still-valid typed tests.

Before production editing, apply the root `AGENTS.md` architecture pre-flight gate if the slice touches a protected/core or >1000 LOC handwritten production file and moves/adds responsibility.

## 7. After pair-manual retirement

Perform a fresh reachability audit of the generic recommendation/result-first/unreliable/direct result blocks in `ClocktowerNightStepUi`.

Remove only branches proven unreachable or superseded after all specialized square-table owners are active. Do not use byte reduction as the justification.

Then execute:

```text
exact scope/diff audit
-> final logical UI-R5 T4
-> cross-role real-device acceptance
-> UI-R5 closeout
-> merge only with explicit user authorization
```

## 8. Scope fence

Preserve:

- `ClocktowerGameSession` writable authority;
- Planner/Reducer rule ownership;
- current recommendation semantics and ranking;
- revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- current Spy Grimoire reveal behavior;
- current dynamic Ravenkeeper-trigger correctness fix;
- New Demon identity specialized confirmation surface.

Do not broaden into EPI-MQ, recommendation-provider redesign, Persistence, unrelated gameplay changes, A4/ZDD, generalized App-root rewrite, or renewed D6 decomposition.

## 9. Remaining execution sequence

```text
1. confirm normal CI/R2 on connector-authored Sage checkpoint
2. retire unreachable pair-manual dialog / center controls
3. re-audit generic result branches and remove only proven dead paths
4. exact final scope audit
5. final logical UI-R5 T4
6. cross-role real-device acceptance
7. UI-R5 closeout / merge only with explicit user authorization
```
