# PR #55 Dawn Poison Exactly-Once Hotfix Handoff

Date: 2026-08-29
Branch: `codex/hotfix-dawn-poison-exactly-once`
PR: #55 (draft; do not merge without explicit authorization)
Base: `main@57b61a6a7d5be375612c2ec3590ff84518c9f277`

## P1 defect

`DawnCommitIntent.poisonCarry` was not owned by `NightDawnDurableMaterializationPlanner`.
The App instead recorded Dawn poison changes through a revision/event-counter-derived `poison-after-night` action ID and inferred whether to write history from the current mechanical poison target.

This broke partial-persistence retry convergence in both directions:

1. Mechanical poison clear persisted first, history missing: restore saw desired `null == null` and permanently skipped the missing `Poison(null)` history fact.
2. `Poison(null)` history persisted first, mechanical poison still stale: restore generated a new dynamic action ID and could append duplicate poison history.

## Accepted semantic contract

Dawn poison is an explicit transition with durable identity:

- `DawnPoisonCarryIntent.previousTargetSeat` = poison target before Dawn.
- `DawnPoisonCarryIntent.targetSeat` = poison target after Dawn; `null` means an explicit clear.
- `null` `poisonCarry` means no poison materialization responsibility, not "clear poison".
- A transition is history-materialized only when previous and desired target differ.
- Stable Dawn poison action identity is independent of event/revision counters.
- Mechanical state repair and history repair are independently planned.

Therefore:

- state-first partial persistence repairs missing history only;
- history-first partial persistence repairs mechanical state only;
- fully durable replay is a no-op;
- unchanged carry does not create redundant Dawn Poison history;
- Poisoner -> Imp clears the prior poison effect exactly once;
- an obsolete confirmed poison is also explicitly cleared when no Poisoner resolution input remains.

## Tests-first evidence

Initial poison-clear intent RED:
- checkpoint `96e82515...`
- production and tests compiled;
- FAST executed 915 tests with exactly one intended assertion failure.

Pure/session GREEN evolved through:
- explicit previous/desired poison transition intent;
- poison materialization state/plan;
- stable poison action ID;
- state-first/history-first/fully-durable/unchanged-carry typed coverage;
- restore/retry convergence acceptance coverage.

Second edge RED:
- checkpoint `9864e47557d1cb7acc48ec6e5f6df7657c92f24b`
- FAST executed 920 tests with exactly one failure proving that confirmed poison still needed explicit clear when `poisonResolutionInput == null`.

Pure/session GREEN checkpoint:
- `884add9a3d06e817c327fdaa97da7edfc8540c39`
- CI #1017 SUCCESS
- R2 #943 SUCCESS

## Final App wiring

Production checkpoint:
- `d80e7742edfcfea78a1b3e05d1cbe732c799c4f5`
- commit: `fix: make dawn poison materialization exactly once`

Exact diff from `884add9a...`:
- exactly one changed file: `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt`
- 22 additions / 23 deletions

Wiring changes:
- passes current mechanical poison seat into `DawnDurableMaterializationState`;
- consumes `durableMaterializationPlan.poison`;
- uses `materialization.actionIdToCommit` as the Poison action identity;
- retains `clocktowerEventCounter + 1` only as action sequence metadata;
- mutates confirmed/draft poison target only when `stateMutationRequired`;
- removes the legacy Dawn `clocktowerActionId(kind = "poison-after-night", ...)` path.

Validation for `d80e7742...`:
- CI #1018 SUCCESS
- Android FAST SUCCESS
- CI gate SUCCESS
- R2 #944 SUCCESS

## Earlier full acceptance checkpoint

Full-ci checkpoint:
- `b7acdeef9ad399417003b26a2e49357818bcee24`
- docs-only `[full-ci]` commit over unchanged production tree `d80e7742edfcfea78a1b3e05d1cbe732c799c4f5`

T4 results:
- CI #1019 SUCCESS
- Android `:app:testFull` + `:app:assembleDebug`: SUCCESS
- ASP contract tests: SUCCESS
- Real Clingo cross-validation: SUCCESS
- CI gate: SUCCESS
- R2 #945: SUCCESS

That checkpoint fully validated production tree `d80e7742...`. The post-audit recovery follow-up below supersedes it as the latest production tree.

## Post-audit state-first recovery follow-up

A subsequent audit found that the pure poison materializer retained the correct stable transition identity, but production restore still rebuilt `previousTargetSeat` only from the mutable checkpoint poison target. In a state-first restore, that checkpoint field was already `null`, so the missing `Poison(null)` history fact could not be repaired.

Production checkpoint:
- `b2da33e2255b58d568296343673b80e153c00376`
- commit: `fix: recover Dawn poison transition from history`

The repair:
- derives the current-round previous poison target from the ordered durable action timeline;
- uses that value only as a fallback when the mutable checkpoint target is already absent;
- preserves checkpoint authority when history-first restore leaves the mechanical target stale;
- changes the convergence acceptance test to restore the real production shape: successor role already materialized, checkpoint poison already `null`, and clear history still missing.

Validation for `b2da33e2...`:
- focused restore/retry acceptance: SUCCESS with `--rerun-tasks`;
- focused Dawn planner + Host integration group: SUCCESS with `--rerun-tasks`;
- local `:app:testFast`: SUCCESS with `--rerun-tasks`;
- GitHub Android FAST, CI gate and R2: SUCCESS (runs `33239658233` and `33239658238`).

Full acceptance checkpoint:
- `9a6c2367fcb93cf91a84f3f8fde9ad332437a480`
- docs-only `[full-ci]` commit over production tree `b2da33e2...`

T4 results:
- GitHub Actions run `33239767543`: SUCCESS;
- Android `:app:testFull` + `:app:assembleDebug`: SUCCESS;
- ASP contract tests: SUCCESS;
- Real Clingo cross-validation: SUCCESS;
- CI gate: SUCCESS;
- R2 run `33239767557`: SUCCESS.

## Ordinary Dawn poison ownership follow-up

A later tests-first audit found that ordinary Night -> Day poison handling still bypassed the typed Dawn durable-materialization ownership path. The obsolete App block used `PoisonEffectLifecycle.afterNight(...)`, compared the desired carry target with mutable mechanical state, and generated a dynamic `poison-after-night` action ID.

Behavioral RED provenance was preserved before production wiring changed. The production repair then moved ordinary Dawn poison through the same typed ownership chain:

`NightDawnPoisonRecoveryAuthority`
-> `NightDawnResolutionPlanner.planPoisonCarry`
-> `NightDawnDurableMaterializationPlanner`
-> `poisonMaterialization.actionIdToCommit`
-> `ActionFactDraft.Poison`

Latest production checkpoint:
- `996201046e53de4395aaf555f50ae0f04a70b5ba`
- commit: `fix: materialize ordinary Dawn poison exactly once`
- parent: `d2cc25845f6790f6e41d8a7ebb91fb7801cae9c1`
- exactly two changed files from the parent:
  - `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt`
  - `app/src/test/java/com/codex/campboardgamehost/persistence/ClocktowerHistoricalActionLifecycleProductionWiringTest.kt`

The production repair:
- recovers the durable previous poison target from current-round ActionFact history;
- plans ordinary Dawn poison carry using `NightDawnResolutionPlanner.planPoisonCarry`;
- routes history/state convergence through `NightDawnDurableMaterializationPlanner`;
- uses only planner-provided `actionIdToCommit` for the Poison action identity;
- mutates confirmed/draft poison state only when `stateMutationRequired`;
- removes the ordinary Dawn `poisonCarriedIntoTomorrow` and `poison-after-night` writer path;
- leaves deferred `poison-expire` paths outside this follow-up unchanged.

The source-wiring characterization test was migrated from the deleted implementation detail to the typed ownership contract. It now requires the recovery/planner/materializer chain and asserts that `poisonCarriedIntoTomorrow` and `poison-after-night` are absent from the Night confirmation block.

Validation for `9962010...`:
- focused ordinary Dawn poison + convergence suite: SUCCESS locally;
- local `:app:testFast`: SUCCESS;
- `git diff --check`: SUCCESS;
- GitHub CI #1027 Android FAST: SUCCESS;
- GitHub CI #1027 gate: SUCCESS;
- GitHub R2 #953: SUCCESS;
- full Android / ASP / Real Clingo were correctly skipped under T1 routing.

Final full acceptance checkpoint:
- `aa0d9cb8526cef2f96894505dee9fd173e8f8bbd`
- docs-only `[full-ci]` commit over unchanged production tree `996201046e53de4395aaf555f50ae0f04a70b5ba`

Final T4 results:
- GitHub CI #1028 / run `33244041684`: SUCCESS;
- Android `:app:testFull` + `:app:assembleDebug`: SUCCESS;
- ASP contract tests: SUCCESS;
- Real Clingo cross-validation: SUCCESS;
- CI gate: SUCCESS;
- GitHub R2 #954 / run `33244041658`: SUCCESS.

The ordinary Dawn poison ownership follow-up is fully accepted. The authorized PR #55 hotfix scope has no known blocker remaining.

Do not mark PR ready or merge without explicit user authorization.
