# Global Ownership Cleanup — Step 6 Handoff

> Updated: 2026-09-14 Australia/Sydney
> Status: PR #129 remote gates pass; user acceptance pending

## 1. Baseline and scope

Step 6 began from merged `main` `0aad20e341af912338f9fae7ea11aca5d71c539f`, which includes
Step 5 via PR #128. The live fan-out and recorded architecture pre-flight are in section 13 of
`docs/GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`.

This final cleanup slice is limited to the repeated phase conversion, immutable Host mechanical
projection and common night square-table presentation shell. It does not change gameplay rules,
recommendation ranking, durable checkpoint/session authority, persistence, callback ordering or
role-specific interaction state.

## 2. Implemented boundary

- `ClocktowerPhase.toStorytellerPhase()` is the common exhaustive phase conversion consumed by all
  six full-mapping production paths.
- `ClocktowerNightHostProjectionFactory` composes existing rules/session owners into one immutable,
  non-Compose projection for Host. Host retains orchestration, materialization and callbacks.
- `ClocktowerHostSquareTableScaffold` owns common navigation, stable seat mapping and the square-table
  surface. Six night square-table callers supply only their local visual state and center content.
- The normal Host overview table remains intentionally separate because it does not own night-step
  navigation or role-specific interaction flow.

## 3. Local evidence

- Meaningful typed phase-adapter RED: expected unresolved adapter reference; GREEN after extraction.
- Meaningful typed night-projection RED: expected unresolved factory reference; GREEN after
  implementation and verifies Ravenkeeper death projection plus before/after mechanical chronology.
- Focused phase, effective-night-state, night transaction/Host integration, current-Demon,
  square-table presentation and scaffold-ownership tests pass with forced execution.
- Forced `:app:testFast`: pass.
- Forced `:app:testFull :app:assembleDebug`: pass.
- `git diff --check`: pass.
- Final production search: the complete phase mapping has one owner; all six night square-table
  callers use the shared scaffold; direct seat-surface ownership remains only in the shared scaffold
  and the intentional Host overview.
- PR #129 code head `ceca17cb30a9ea0ca6a2c13c80f8c20da1a1e742`: remote Android Full, ASP
  contracts, real-Clingo, aggregate CI and R2 boundary checks pass.

## 4. Remote acceptance gate

- Verify the exact parent is merged `main` `0aad20e341af912338f9fae7ea11aca5d71c539f`.
- Verify changed-file scope is limited to the three Step 6 seams, their consumers/tests and campaign
  documents.
- Require remote Android Full, ASP contracts, real-Clingo, aggregate CI and R2 boundary validation.
- Keep the PR Draft and unmerged until the user explicitly authorizes merge.

## 5. Next target after merge

Global ownership cleanup ends with Step 6. After merge and a fresh live-state audit, the queued next
program is `EPI-MQ-0.5` dynamic-script extensibility guard, using
`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-13_EPI_MQ_0_5.md`. Do not begin it in the Step 6 PR.

## 6. Stable rule

> App/Host may coordinate lifecycle and rendering, but complete phase translation, unfinished-night
> mechanical composition and repeated square-table shell behavior each have one explicit owner.
