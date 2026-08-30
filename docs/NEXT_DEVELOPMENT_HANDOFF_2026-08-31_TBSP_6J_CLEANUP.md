# Next Development Handoff — TBSP-6J Cleanup

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR: #57  
> Status: TBSP-6I ACCEPTED; TBSP-6J is next. Do not merge or mark Ready without explicit authorization.

## 1. Resume anchors

Re-query live GitHub state before changing code.

Accepted logical code/test checkpoints:

- TBSP-6G-B production wiring product commit: `52378a6887553fb37692def96c1657110151f114`
- TBSP-6G-B cleanup/final carrier: `bf8c46bd1acde81c15c7323d858594c648b3b845`
- TBSP-6H-B production code commit: `ff1c99fe97552dc65f3d1bf8326bdb451c8e25a0`
- TBSP-6H-B cleanup commit: `f12e6369e7b25519c97f69271619a32770eca60f`
- TBSP-6H-B docs checkpoint: `aeed30411aefa0b27b107c966341c3a7b9cddaf5`
- TBSP-6I NGJ acceptance test checkpoint: `f7e877f6881cc74b9d8e7f4f8db2b2fb406b84d4`

6I same-head validation for `f7e877...`:

- CI run #1148 / `33341819960`: SUCCESS
- Android FAST unit tests (`:app:testFast`): SUCCESS
- CI gate: SUCCESS
- R2 run #1071 / `33341819962`: SUCCESS

## 2. TBSP-6I acceptance result

No production bug was found during 6I. One missing durable regression was added:

`app/src/test/java/com/codex/campboardgamehost/NoGreaterJoySetupRegressionTest.kt`

It locks the established No Greater Joy role pool, 5/6-player team distributions and script start eligibility. The 6I checkpoint changes no production code.

Evidence matrix:

- P8 recomposition cannot reroll a started setup: accepted structurally. Setup selection/preparation is inside the explicit Start callback; Compose recomposition does not invoke that callback. Committed setup provenance is remembered state after Start.
- P9 navigation before Start does not commit a preset: accepted structurally. `ClocktowerScriptSelectionScreen` receives `onStart = ::startClocktowerGame`; `onBack` only changes screen state. There is no setup-selection side effect in composition/navigation.
- P10 No Greater Joy unchanged: new typed `NoGreaterJoySetupRegressionTest`, GREEN in T1.
- P11 restore does not select a new preset: typed provenance round-trip restores the exact `TroubleBrewingSetupPresetSelection`; existing App wiring guard verifies restore does not invoke preparer/selector.
- P12 invalid preset data has no broad-random fallback: `TroubleBrewingProductionSetupPreparerTest` expects `TroubleBrewingSetupPresetValidationException` / `INVALID_DEMON` for invalid data.
- P13 reveal is non-blocking: `TroubleBrewingSetupRecommendationRevealCoordinatorTest` proves reveal precedes background dispatch and no build occurs synchronously.
- P14 background computation is exact-input/stale-safe: 6G recommendation prewarm and 6H First Night precompute tests prove exact request reuse, BUSY wait at point of use, MISS recompute and stale-work non-overwrite; production wiring builds requests from the committed deal and does not mutate identities.
- P15 only true completed TB games enter rotation history: completion gate requires Clocktower + Trouble Brewing + non-null outcome + committed provenance.
- P16 completion persistence is retry-safe/original-selection based: typed rotation-history store test proves same gameId/selection retry writes once and conflicting gameId reuse is rejected.

Do not create new source-string tests merely to restate P8/P9. There is no existing `androidTest`/Compose instrumentation harness in this repo; introducing one solely for these static event-wiring facts is disproportionate to the remaining risk.

## 3. TBSP-6J exact scope

6J is cleanup only. Do not broaden into new behavior.

A concrete dormant API is now confirmed in `CampBoardGameHostApp.kt`:

```kotlin
fun resetDealState(
    nextGameKind: GameKind,
    clocktowerScript: ClocktowerScript = ClocktowerScript.TroubleBrewing,
    preparedClocktowerSeed: Long? = null,
    preparedSetupPlan: RecommendationPlan? = null,
)
```

`preparedSetupPlan` is not consumed by `resetDealState`. The only four-argument call is the existing No Greater Joy / legacy Clocktower start path:

```kotlin
resetDealState(GameKind.Clocktower, script, preparedSeed, preparedSetupPlan)
```

The local `preparedSetupPlan` itself remains behaviorally meaningful there because it is used to derive the legacy recommended Drunk shown role before cards are committed. Only the dead pass-through argument is the cleanup target.

### 6J intended change

- remove the unused `preparedSetupPlan` parameter from `resetDealState`;
- change the legacy call to `resetDealState(GameKind.Clocktower, script, preparedSeed)`;
- remove an import only if it becomes genuinely unused;
- make no TB/NGJ setup, identity, recommendation, persistence, First Night, history or navigation behavior change.

Do not delete the local legacy `preparedSetupPlan` calculation unless a separate proof shows it is dead; current source shows it feeds `recommendedDrunkShownRole`.

## 4. 6J validation

This is a behavior-preserving dead-parameter cleanup, so do not manufacture a RED test for the implementation detail.

Minimum evidence:

1. exact diff audit: only the dead parameter/call-site cleanup and any resulting unused import;
2. `git diff --check`;
3. compile (`:app:compileDebugKotlin` or equivalent CI compile path);
4. focused existing setup/start/NGJ tests if practical;
5. T1 `:app:testFast` at the logical 6J checkpoint;
6. same-head R2/CI as required by repository policy.

Stop after 6J acceptance. TBSP-6K is the separate final full-acceptance slice.

## 5. Scope exclusions

Do not in 6J:

- regenerate/reformat the frozen TB preset dataset;
- change selector/rotation weights;
- change Drunk ownership;
- change No Greater Joy generation semantics;
- change 6G/6H background lifecycle semantics;
- reopen Dawn/Dusk exactly-once semantics;
- resume A3/A4/ZDD, Mayor, Imp succession or broad App/Host decomposition;
- merge PR #57 or mark it Ready.
