# NEXT DEVELOPMENT HANDOFF — 2026-08-23

> Project: `Jazz0006/CampBoardGameHost`  
> Parent roadmap: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Project AI instructions: `AGENTS.md`  
> Development operations: `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`  
> Large-file execution: `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`  
> Current task: **PR #43 Clocktower host source decomposition**  
> Immediate next step: **A10 Information / Step Builder seam — Chat designs, executor only implements**  
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

## 6. Remaining host state and revised completion criterion

```text
ClocktowerHostScreen.kt: 294,769 bytes
ClocktowerHostScreen.kt: 4,818 lines
```

The previous plan treated <= 50 KiB as a hard end-state. That has now been revised.

The remaining file is almost entirely one large active `ClocktowerJudgeScreen`. File size remains a useful maintainability signal, but it is **not a hard merge gate** when the only way to satisfy it would be to create weak abstractions, giant parameter bags, move Compose state/effect lifetime, or expose tightly coupled internals across files.

The new architectural completion criterion is:

> `ClocktowerJudgeScreen` becomes a coherent coordinator/orchestrator. Role-information construction, first-night step construction, other-night step construction, and suitable presentation routing move to stable owners; state/effect lifetime and transaction ordering remain in the host when that is the safer ownership boundary.

A remaining host in roughly the 100–150 KiB range may be acceptable if what remains is genuinely orchestration and further extraction would increase coupling or regression risk.

## 7. Planned remaining decomposition

### A10 — Information / Step Builder seam

Create a non-state-owning builder boundary for the current nested information/step construction helpers.

Target responsibilities include, where the boundary is natural:

- reliable/unreliable information display-option construction;
- number / yes-no / role-reveal / pair-information recommendation-backed options;
- registration-aware information modeling;
- `infoStep`-style model construction.

Constraints:

- no `remember` ownership moves;
- no `LaunchedEffect` ownership moves;
- no transaction commit ordering changes;
- no product behavior, recommendation ranking, registration, information lifecycle, history, persistence, or session-authority change.

A10 is a seam/foundation slice, not a broad product refactor.

### A11 — First Night Step Factory

Move first-night role-by-role step construction behind a cohesive factory returning `List<ClocktowerNightStepUi>` (or the smallest equivalent stable model boundary).

The host remains responsible for state/effect lifetime and commit sequencing.

### A12 — Other Night Step Factory

Move other-night role-by-role step construction behind a separate cohesive factory.

Do **not** move the sensitive `advanceNightStep` transaction merely to reduce file size. Confirm/audit/registration/event/index/finalization ordering stays host-owned unless a later dedicated architecture decision proves a safer transaction boundary.

### A13 — Day routing consolidation

Consolidate low-coupling day presentation/routing only where callbacks form a clean boundary. Overview/Vote/EndConfirm are likely candidates.

Nomination/Virgin, Slayer, Artist, and Klutz remain optional follow-up candidates. Stop rather than force them across files if doing so requires exporting large amounts of registration/recommendation/state internals.

### Post-A13 re-audit

Re-measure the host, inspect responsibility cohesion, and decide whether PR #43 is complete.

Do **not** pre-commit to reaching <= 50 KiB. Stop when further extraction would be architecture-negative.

## 8. Explicit host-owned responsibilities that are not current decomposition targets

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

## 9. Working model — Chat decides, connector first, Codex/Luna executes heavy edits

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

## 10. Validation rules for each later slice

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

## 11. Stop conditions

- PR #43 must remain draft and unmerged;
- no A3 product work in this PR;
- no file-size-driven state-lifetime move;
- no callback ordering, recommendation, registration, information, history or session-authority changes without a dedicated explicit decision;
- do not continue decomposition solely to satisfy a byte threshold once the host is a coherent coordinator.

## 12. Merge boundary

PR #43 remains draft. Do not mark ready or merge until the planned high-value decomposition is complete, the post-A13 architecture/size audit is satisfactory, the latest full CI/R2 gates are GREEN, and the user explicitly authorizes merge.
