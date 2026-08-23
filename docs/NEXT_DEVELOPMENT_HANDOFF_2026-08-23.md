# NEXT DEVELOPMENT HANDOFF — 2026-08-23

> Project: `Jazz0006/CampBoardGameHost`  
> Parent roadmap: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Project AI instructions: `AGENTS.md`  
> Development operations: `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`  
> Large-file execution: `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`  
> Current task: **PR #43 Clocktower host source decomposition**  
> Immediate next step: **A11 Night Step Materialization seam / registry — Chat designs, connector can implement because target files are small**  
> Status: **CURRENT HANDOFF**

## 1. Trusted live state

```text
repository: Jazz0006/CampBoardGameHost
live main: 88164a5bba1fa80695a0247538e632d127e5cfa1
main source: PR #42 Historical Action + Observation Capture merge

PR: #43 — Refactor: decompose Clocktower host monolith
branch: codex/source-decomposition-clocktower-host
state: DRAFT / OPEN / NOT MERGED
validated A10 implementation head: 363629ed45f0f044da021f77bb52c5c3ff3c9e20
latest documentation head after roadmap update: b795d2b91560033a537debf0f0ce472191ccdf52
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

A10 Generic information-step packaging owner
    ClocktowerInformationStepBuilder.kt
```

All A1–A10 slices passed independent remote validation before moving on.

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
exact deletion audit:         PASS
active prefix through return: byte-for-byte identical
removed:                      25,068 bytes / 484 lines
CI #506:                      SUCCESS
R2 #446:                      SUCCESS
```

## 6. A10 final evidence

A10 deliberately used a narrow seam rather than moving recommendation/registration logic wholesale.

Moved owner:

```text
ClocktowerInformationStepBuilder.kt
```

It owns only the previous generic `infoStep` packaging behavior and receives the former captured dependencies explicitly. It does not own Compose state/effects, recommendation calculation, dynamic decision, registration mutable maps, history/session authority or transaction ordering.

```text
RED commit:                         3377fdbea83727a797afce28064b924a074df5c3
CI #513:                            EXPECTED FAILURE
Android tests:                      658 total / 1 expected ownership failure
ASP contract tests:                 SUCCESS
Real Clingo:                        SUCCESS
R2 #453:                            SUCCESS

GREEN commit:                       363629ed45f0f044da021f77bb52c5c3ff3c9e20
new builder size:                   9,095 bytes
Host after GREEN:                   287,597 bytes
infoStep call sites replaced:       21
exact move / scope audit:           PASS
CI #514:                            SUCCESS
R2 #454:                            SUCCESS
```

## 7. Dynamic multi-script night flow — precise current state

Do not describe multi-script dynamic flow as either “not implemented” or “fully complete”. The current production state is split at a clear boundary.

Already implemented and production-authoritative since R5.5:

```text
ValidatedClocktowerRuleset
        ↓
ClocktowerFlowPlanner
        ↓
ClocktowerHostInteractionProjector
        ↓
stable + conditional ClocktowerHostInteraction
        ↓
ClocktowerProductionFirstNightFlow / OtherNightFlow
        ↓
canonical production ordering
```

This layer already makes script composition, night ordering and conditional/event interaction existence dynamic. Trouble Brewing and No Greater Joy use the same catalog/planner seam.

Still transitional:

```text
ClocktowerJudgeScreen
  -> constructs hardcoded unfilteredNightSteps for currently supported roles/events
  -> filters them
  -> asks planner-backed production flow to exact-match/reorder them
```

Therefore the remaining architectural gap is **production step materialization**, not flow-order authority.

Target direction:

```text
planner/projector interaction plan first
        ↓
materializer registry keyed by stable interaction identity
        ↓
lazy ClocktowerNightStepUi construction
```

Do not simply move the current hardcoded list into large FirstNight/OtherNight factory files.

## 8. Revised remaining decomposition

### A11 — Night Step Materialization seam / registry

A11 is a small seam slice and should not touch the huge Host list yet.

Required design:

- add canonical interaction-projection access to `ClocktowerProductionFirstNightFlow` and `ClocktowerProductionOtherNightFlow` while preserving existing `.order(...)` behavior;
- add a stateless `ClocktowerNightStepMaterializerRegistry` outside Compose;
- registry registration uses stable `ClocktowerProductionNightStepIdentity` / `ClocktowerInteractionId` semantics;
- projected interaction order is authoritative;
- `SYSTEM_BOUNDARY` interactions do not create current production UI steps;
- only projected actionable interactions invoke their lazy materializer;
- missing projected materializer fails closed;
- duplicate registered identity fails closed;
- extra registered materializers are allowed and remain unevaluated, enabling a registry to support roles not present in the current script/table.

A11 must not:

- cut over `ClocktowerJudgeScreen` yet;
- move role-specific construction;
- absorb Compose state/effects;
- absorb recommendation/dynamic-decision logic;
- alter ordering or behavior.

### A12 — planner-driven First Night materialization

Cut first night over from:

```text
prebuild all supported first-night steps -> planner order
```

to:

```text
planner interactions -> lazy materialize requested first-night steps
```

Keep planner/projector as sole order authority. Keep state/effect lifetime, recommendation/registration semantics and first-night information migration host-owned unless Chat explicitly proves another safe owner.

### A13 — planner-driven Other Night materialization

Perform the same cutover for other night and conditional/event interactions.

Do **not** move `advanceNightStep`. Confirm/audit/registration/event/index/finalization ordering remains Host-owned.

### A14 — optional clean day routing

Only after A13 re-audit. Overview/Vote/EndConfirm remain likely low-coupling candidates. Nomination/Virgin, Slayer, Artist and Klutz are optional and should stay in Host if extraction worsens coupling.

### Post-A13/A14 re-audit

Re-measure the host and inspect responsibility cohesion. Do not pre-commit to <=50 KiB. A14 is optional; stop when further extraction is architecture-negative.

## 9. Explicit host-owned responsibilities that are not current decomposition targets

The following should remain in `ClocktowerJudgeScreen` during the current plan unless Chat makes a later explicit architecture decision:

- Compose `remember` state ownership;
- Compose effect lifecycle (`LaunchedEffect`, related lifecycle-bound work);
- setup recommendation lifecycle;
- first-night migration lifecycle;
- telemetry recorder lifetime;
- registration mutable maps/state;
- night commit transaction and callback ordering;
- top-level phase routing where it is truly orchestration;
- debug/A4 benchmark lifecycle when extraction has little maintainability value.

## 10. Working model — Chat decides, connector first, Codex/Luna executes heavy edits

The project-level authority is `AGENTS.md`.

Default flow:

```text
Chat
  -> query live state
  -> perform architecture / risk / boundary audit
  -> decide exact slice and validation strategy

If GitHub connector can safely read/write the target
  -> Chat performs the edit directly through connector
  -> exact remote diff audit
  -> appropriate GitHub checks / CI

If file size/truncation/mechanical complexity makes connector editing unsafe
  -> Chat writes a precise implementation task
  -> user sends it to Codex/Luna
  -> Codex/Luna performs only the specified implementation + local validation + commit/push
  -> Chat re-reads GitHub and audits the remote result
```

Codex/Luna is not the default architecture decision-maker. Do not ask it to choose decomposition boundaries unless the user explicitly changes this working model.

A11 target files are small enough for connector-first implementation. A12/A13 are expected to touch the large Host and may require the Luna local-worktree path after Chat fixes the exact boundary.

## 11. Validation rules for each later slice

```text
live head recheck
-> Chat architecture/scope decision
-> focused characterization or tests-only RED where required
-> connector direct GREEN when safe, otherwise Luna local mechanical GREEN
-> focused/full unit tests + assembleDebug as appropriate
-> exact move/deletion/diff audit
-> GitHub CI + ASP + Real Clingo + R2 as required
-> Chat boundary re-audit before next slice
```

Use `GRADLE_USER_HOME="$PWD/.gradle-codex"`; keep `.gradle-codex/` untracked.

## 12. Stop conditions

- PR #43 must remain draft and unmerged;
- no A3 product work in this PR;
- no file-size-driven state-lifetime move;
- no callback ordering, recommendation, registration, information, history or session-authority changes without a dedicated explicit decision;
- do not reintroduce UI/script order authority beside the R5.5 planner;
- do not continue decomposition solely to satisfy a byte threshold once the host is a coherent coordinator.

## 13. Merge boundary

PR #43 remains draft. Do not mark ready or merge until the planned high-value decomposition is complete, the final architecture/size audit is satisfactory, the latest full CI/R2 gates are GREEN, and the user explicitly authorizes merge.
