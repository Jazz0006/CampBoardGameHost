# NEXT DEVELOPMENT HANDOFF — 2026-08-23

> Project: `Jazz0006/CampBoardGameHost`  
> Parent roadmap: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Development operations: `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`  
> Large-file execution: `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`  
> Current task: **PR #43 Clocktower host source decomposition**  
> Immediate next step: **A9 planning — unreachable legacy fallback cleanup**  
> Status: **CURRENT HANDOFF**

## 1. Trusted live state

```text
repository: Jazz0006/CampBoardGameHost
live main: 88164a5bba1fa80695a0247538e632d127e5cfa1
main source: PR #42 Historical Action + Observation Capture merge

PR: #43 — Refactor: decompose Clocktower host monolith
branch: codex/source-decomposition-clocktower-host
state: DRAFT / OPEN / NOT MERGED
validated head: e1f94fbe01ab95312555ae4524bbc6ad9204b820
```

New sessions must query live state again. Do not assume these SHAs remain current.

## 2. Product progress before PR #43

PR #42 has already completed Historical Action + Observation Capture. The previous handoff that named it as NEXT is obsolete.

PR #42 provides shared Global timeline authority, durable semantic action persistence, lifecycle capture and information-observation production wiring. The next product source phase after decomposition is A3 historical multi-night exact baseline.

PR #43 is structural-only and must not implement A3 product behavior.

## 3. PR #43 completed slices

```text
A1 Core semantics owner
   ClocktowerHostCoreSemantics.kt

A2 Selection semantics owner
   ClocktowerHostSelectionSemantics.kt

A3 Presentation models owner
   ClocktowerHostPresentationModels.kt

A4 Recommendation screen/reason UI
   ClocktowerStorytellerRecommendationUi.kt

A5 Recommendation card/editor UI
   ClocktowerStorytellerRecommendationUi.kt

A6 Player display UI
   ClocktowerPlayerDisplayUi.kt

A7 Spy/Recluse registration UI
   ClocktowerRegistrationUi.kt

A8 Night-step presentation UI
   ClocktowerNightStepUi.kt
```

All A1–A8 slices passed independent local/remote validation before moving on.

## 4. A8 final evidence

A8 originally attempted to move `ClocktowerInfoCard` while keeping it file-private. Audit found six remaining host call sites, so that boundary was corrected before GREEN.

Final A8 scope:

- move only `ClocktowerNightStepCardLocalized`;
- change only `private -> internal`;
- leave `ClocktowerInfoCard` and all six host calls unchanged;
- update structural/source-contract tests to follow the new owner.

```text
A8 production commit:  fdab916dd8f7e9b4614bf16b79355036ff45fe41
A8 validated head:      e1f94fbe01ab95312555ae4524bbc6ad9204b820
new file size:          45,251 bytes
exact move audit:       PASS
CI #503:                SUCCESS
R2 #443:                SUCCESS
```

## 5. Remaining host state

```text
ClocktowerHostScreen.kt: 319,837 bytes
ClocktowerHostScreen.kt: 5,303 lines
```

The file now consists primarily of one very large `ClocktowerJudgeScreen` plus a small tail helper. The <= 50 KiB objective is not complete.

## 6. Immediate A9 candidate

Static control-flow inspection found an unconditional return after the active themed UI:

```text
ClocktowerDarkTheme { ... active production UI ... }
return
LazyColumn { ... old legacy fallback ... }
```

The fallback and `ClocktowerInfoCard` occupy about 25.8 KB / 513 lines. All six `ClocktowerInfoCard` calls are inside that unreachable fallback; the helper has no active call site.

A9 should first prove and remove this dead block before introducing any new state-owner abstraction.

### Proposed A9 tests-first boundary

1. Add a source contract that rejects the `return` followed by legacy `LazyColumn` pattern.
2. Update `ClocktowerNightStepUiOwnershipTest` so it no longer requires `ClocktowerInfoCard` to remain in host.
3. Require `ClocktowerInfoCard` to be absent after cleanup.
4. Confirm the active `ClocktowerDarkTheme` block remains present.
5. RED must fail only because the unreachable block/helper still exist.

### Proposed A9 GREEN allowlist

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
app/src/test/java/com/codex/campboardgamehost/ClocktowerNightStepUiOwnershipTest.kt
app/src/test/java/com/codex/campboardgamehost/ClocktowerLegacyFallbackOwnershipTest.kt
.github/workflows/r2-write-probe.yml
```

Tests should be committed separately before the large deletion. Luna should perform the production deletion in a complete local worktree.

## 7. A9 invariants

- structural cleanup only;
- do not change active themed UI;
- do not change phase/day/night flow;
- do not change recommendation ranking or telemetry;
- do not change Spy/Recluse registration semantics;
- do not change information decision lifecycle;
- do not change persistence/history/session authority;
- do not begin A3 product work;
- do not merge PR #43.

## 8. A9 validation

```text
focused ownership/source contract
-> full :app:testDebugUnitTest
-> :app:assembleDebug
-> git diff --check
-> exact deletion audit
-> verify only unreachable fallback/helper were removed
-> measure remaining host bytes/lines
-> push feature branch
-> GitHub CI + ASP + Real Clingo + R2
-> re-audit next decomposition boundary
```

Use:

```bash
GRADLE_USER_HOME="$PWD/.gradle-codex"
```

Keep `.gradle-codex/` untracked.

## 9. Stop conditions

Stop and report if:

- live head differs from the expected head without explanation;
- local target diverged and cannot fast-forward;
- the fallback is not provably after the unconditional return;
- any `ClocktowerInfoCard` call exists in active code;
- deletion changes active production code;
- focused/full tests expose a product behavior dependency;
- work would require modifying history, recommendation, registration or session semantics.

After A9 GREEN and remote gates, stop before A10 and re-measure/re-plan.

## 10. Merge boundary

PR #43 remains draft. Do not mark ready or merge until the full decomposition goal, final size audit, full CI/R2 and final review are complete, and the user explicitly authorizes merge.
