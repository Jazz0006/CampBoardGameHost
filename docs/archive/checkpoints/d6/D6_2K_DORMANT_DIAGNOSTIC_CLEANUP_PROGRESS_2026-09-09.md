# D6.2k — Dormant diagnostic composition cleanup

> Date: 2026-09-09
> Status: COMPLETE / VALIDATED; Android FAST + CI gate and R2 PASS.
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.
> Parent: `333fd6d97d734ec28d03832fd5b1a1b2bb3fb82c`.
> Production commit: `c3a25f640e7e6c9ef2537d7d9387eb27b28b1ece`.
> Production tree: `fa0f97fd81c740eeb41dfb273557ce33881f4f41`.
> main remains `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`.

## Contract and result

Implemented the exact three-file contract from `D6_2J_RESIDUAL_COMPOSITION_AUDIT_2026-09-09.md`, following AGENTS.md and TESTING_STRATEGY.md. This is behavior-preserving deletion of unreachable diagnostic execution and zero-consumer inputs, not a change to diagnostic services or gameplay.

- Removed three Host diagnostic counter/report/error groups and their guarded effects, plus the unused availability expression.
- Removed the unused diagnostics-expanded state, child argument and child parameter.
- Removed the child first-night pool benchmark counter/report/error group and its guarded effect.
- Removed Judge `rulesetRef` and `confirmedDemonSuccessorTarget` inputs and only the matching App forwarding arguments.
- Removed eight unused pure role lookup locals.
- Removed 26 imports whose remaining usages disappeared directly with these deletions; unrelated import debt stays outside this slice.

All four trigger counters were initialized to zero and had no increment/write entry point. Each effect returned immediately on zero. The report/error outputs and diagnostics-expanded flag had no live rendering consumer. No new runtime behavior or replacement abstraction was introduced.

## Measured result

| Surface | Before | After |
|---|---:|---:|
| Judge parameters | 89 | 87 |
| Judge on-callbacks | 34 | 34 |
| Judge function providers | 3 | 3 |
| Judge MutableState inputs | 5 | 5 |
| NightStep parameters | 49 | 48 |
| NightStep on-callbacks | 13 | 13 |
| App remembered Clocktower scalars | 44 | 44 |
| Host LaunchedEffect call sites | 10 | 7 |
| NightStep LaunchedEffect call sites | 2 | 1 |

App scalar definition remains 38 delegated remembered vars plus six explicit remembered scalar state vals. Thirteen diagnostic remembered states were deleted (ten Host, three child). Signature counts use parameter declaration lines within the named function signature; effect counts use `LaunchedEffect(` call sites.

| File | Bytes before → after | Lines before → after | Exact diff |
|---|---:|---:|---:|
| CampBoardGameHostApp.kt | 237112 → 236956 | 4238 → 4236 | +0 / -2 |
| clocktower/ui/ClocktowerHostScreen.kt | 281354 → 268260 | 4755 → 4545 | +0 / -210 |
| ClocktowerNightStepUi.kt | 47970 → 45697 | 913 → 868 | +0 / -45 |

Total: three production files, +0 / -257; no test, workflow, dependency or asset changes.

## Preserved boundaries

The full App text is byte-identical after accounting for exactly the two removed forwarding lines. Its actual ruleset and confirmed successor state, reset/restore, Recovery serialization and transaction callbacks are untouched. All five externally owned mutable inputs stay external.

Live A4 identity prewarming, recommendation preparation/application, first-night pools/publication/parity telemetry, registration maps and their lifetimes remain intact. Benchmark harnesses, coordinators and existing owning tests are unchanged. The removed cancellation probe used an isolated coordinator and had no runnable trigger; this deletion does not retire the real cancellation or stale-publication contract.

## Evidence

- Local `git diff --check` and exact allowlist/deletion audit passed.
- Signature/effect counts match the contract; dead diagnostic names have no remaining production references.
- Local blob SHAs and complete staged tree matched the GitHub-created objects.
- Remote compare confirms one commit from the expected parent, zero behind, exactly three files and +0 / -257.
- Source was synchronized by normal fast-forward; no force-push or temporary workflow.
- [CI 34293746781](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34293746781) PASS: Android compilation, FAST tests and CI gate. FULL/debug APK, ASP and Real Clingo were correctly skipped.
- [R2 34293746779](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34293746779) PASS.
- Android job logs confirm `:app:compileDebugKotlin`, `:app:testFast` and `BUILD SUCCESSFUL`; this is executed remote evidence, not only a workflow status. Existing `A4DeviceBenchmarkHarnessTest`, `UnifiedSetupSelectorDeviceBenchmarkTest` and `UnifiedSelectionPoolDeviceBenchmarkTest` are within default FAST coverage and remain unchanged.

This slice requires compile/static/exact-diff evidence and ordinary Android FAST + CI/R2 under the D6.2j contract. It does not alter the live epistemic algorithm, checkpoint ordering or workflow, so no new RED or full-suite escalation was introduced. The preceding D6.2i full gate remains historical full-suite evidence, not a claim that the current commit ran FULL.

Local Gradle bootstrap was previously blocked by unavailable network access, and this workspace has no Android SDK. No local JVM GREEN, debug APK or real-device verification is claimed.

## Next bounded slice

Follow the global audit's R0 ordering: retire the proven unused Day/History UI declarations together with only the obsolete R2 existence assertions that protect those declarations. Recheck live call sites before deletion, retain current square-table UI and live history rendering, and run full CI because the workflow changes. Keep private App decoder cleanup in a separate slice; then move to the information preparation/materializer boundaries.

The current implementation does not yet establish those later boundaries. Do not count this deletion as completion of the broader composition decomposition, and do not localize Recovery-coupled state or introduce broad state/action bags.
