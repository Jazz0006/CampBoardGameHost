# NEXT DEVELOPMENT HANDOFF — 2026-08-23

> Project: `Jazz0006/CampBoardGameHost`  
> Parent roadmap: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Development operations: `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`  
> Large-file execution: `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`  
> Current task: **PR #43 Clocktower host source decomposition**  
> Immediate next step: **A10 boundary re-audit only — no implementation yet**  
> Status: **CURRENT HANDOFF**

## 1. Trusted live state

```text
repository: Jazz0006/CampBoardGameHost
live main: 88164a5bba1fa80695a0247538e632d127e5cfa1
main source: PR #42 Historical Action + Observation Capture merge

PR: #43 — Refactor: decompose Clocktower host monolith
branch: codex/source-decomposition-clocktower-host
state: DRAFT / OPEN / NOT MERGED
validated A9 implementation head: 00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
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

A9 Unreachable legacy fallback cleanup
   ClocktowerHostScreen.kt
```

All A1–A9 slices passed independent local/remote validation before moving on.

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

## 5. A9 final evidence

A9 removed only the unreachable legacy `LazyColumn` after the active themed UI's unconditional return, its six `ClocktowerInfoCard` call sites, and the now-unused private helper.

```text
RED commit:                   3ecbcadbd728ac83f7ab1f8d1d40175795e44078
CI #505:                      EXPECTED FAILURE
unit tests:                   657 total / 2 expected failures
assembleDebug:                SUCCESS
ASP contract tests:           SUCCESS
Real Clingo:                  SUCCESS
R2 #445:                      SUCCESS

GREEN commit:                 00a2d19e45415614fbd8e93e83a53ba4d2cf9d35
changed GREEN files:          ClocktowerHostScreen.kt + r2-write-probe.yml only
exact deletion audit:         PASS
active prefix through return: byte-for-byte identical
removed:                      25,068 bytes / 484 lines
CI #506:                      SUCCESS
R2 #446:                      SUCCESS
```

## 6. Remaining host state

```text
ClocktowerHostScreen.kt: 294,769 bytes
ClocktowerHostScreen.kt: 4,818 lines
```

The <= 50 KiB objective is not complete. The remaining file is now almost entirely one large active `ClocktowerJudgeScreen`; there is no longer a dead fallback or tail `ClocktowerInfoCard` seam.

## 7. Immediate next step — A10 boundary re-audit

A10 has not started. Do not write tests or production code until the active screen is re-audited.

The re-audit must identify:

1. cohesive state/model construction that can move without changing Compose state lifetime;
2. active phase-presentation blocks with stable parameter/callback boundaries;
3. callback invocation and selection-audit ordering that must remain exact;
4. cross-file visibility changes required by any candidate;
5. expected new-file size before choosing a move;
6. a focused ownership/characterization contract that can produce a real RED.

Stop if the candidate would change product behavior, recommendation ranking, information lifecycle, registration semantics, persistence/history, or session authority.

## 8. Validation rules for any later slice

```text
live head recheck
-> focused tests-only RED
-> remote RED provenance
-> Luna local mechanical GREEN for large-file changes
-> focused/full unit tests + assembleDebug
-> exact move/deletion audit
-> GitHub CI + ASP + Real Clingo + R2
-> stop and re-audit
```

Use `GRADLE_USER_HOME="$PWD/.gradle-codex"`; keep `.gradle-codex/` untracked.

## 9. Stop conditions

- PR #43 must remain draft and unmerged;
- no A3 product work in this PR;
- no A10 implementation before the boundary audit is complete;
- no state lifetime, callback ordering, recommendation, registration, information, history or session-authority changes.

## 10. Merge boundary

PR #43 remains draft. Do not mark ready or merge until the full decomposition goal, final size audit, full CI/R2 and final review are complete, and the user explicitly authorizes merge.
