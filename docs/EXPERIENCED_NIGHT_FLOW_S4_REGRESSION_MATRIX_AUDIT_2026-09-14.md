# EXPERIENCED NIGHT FLOW — S4 Regression Matrix Audit

> Date: 2026-09-14 Australia/Sydney  
> Status: **S4 AUDIT COMPLETE — one proven edit/reconfirm defect; automation and field acceptance next**  
> Program: `EXPERIENCED-NIGHT-FLOW-1`  
> Branch: `codex/experienced-night-flow-correctness`  
> PR: `#123` — open / draft / do not merge yet  
> Parent handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-14_EXPERIENCED_NIGHT_FLOW_CORRECTNESS.md`

## 1. Acceptance invariant

For every production-valid night scenario in the matrix:

```text
legal confirmation or Previous -> edit -> reconfirm
-> checkpoint transition remains authoritative
-> refreshed flow resolves to one renderable next step or explicit Dawn
-> restore reconstructs the same confirmed mechanics without promoting a draft
```

Beginner and Experienced consume the same rule, checkpoint, reconstruction and surface owners.
Mode may change automatic/manual interaction and compact presentation only.

## 2. Existing evidence matrix

| Required scenario | Authoritative owners | Existing durable evidence | Audit result |
|---|---|---|---|
| Ordinary Demon attack -> Dawn | `NightCheckpointHostTransaction`, `NightDawnResolutionPlanner`, dynamic advance | `ClocktowerDemonAttackDawnFactsTest`, `NightDawnResolutionPlannerAttackOutcomeContractTest`, `ClocktowerDynamicNightAdvanceTest` | Sufficient |
| Demon attacks Mayor -> manual redirect -> continuation | attack semantics, Mayor legality/planner, ruling presentation, dynamic advance | `ClocktowerDemonAttackDawnFactsTest`, `NightDawnResolutionPlannerMayorContractTest`, `ClocktowerSingleTargetInteractionPresentationTest`, `MayorRedirectDependencyInvalidationTest` | Sufficient |
| Imp self-kill -> successor -> new-Demon identity -> Dawn | succession semantics, checkpoint transaction, reconstructor, Dawn planner | `SNE7NightTransactionBehaviorMatrixTest`, `NightTransactionHostIntegrationSmokeTest`, `NightDawnResolutionPlannerContractTest`, `NightDawnResolutionPlannerSuccessionResolutionTest`, `ClocktowerDynamicNightAdvanceTest` | Sufficient |
| Previous preserves confirmed state | checkpoint transaction/reducer | `NightCheckpointHostTransactionTest`, `NightCheckpointReducerTest`, `NightTransactionHostIntegrationSmokeTest` | Sufficient |
| Edit draft then reconfirm | reducer plus target-selection UI projection | reducer tests cover Poison/Monk/Demon/Mayor/successor; `ClocktowerMonkRetargetRavenkeeperDeathTest` covers changed Monk consequence | **UI toggle defect found** |
| Restore/reconstruction | persisted checkpoint, restore composition, reconstructor, retry convergence | `NightTransactionReconstructionContractTest`, `NightTransactionRestoreCompositionTest`, `NightDawnRestoreRetryConvergenceAcceptanceTest`, integration smoke | Sufficient |
| Single-target surfaces | S3 selection/eligibility projection | `ClocktowerSingleTargetInteractionPresentationTest`, `ClocktowerNightActionSquareTablePresentationTest` | Sufficient after S3 |
| Pair, numeric and manual information | family presentation/selection owners and typed surface plan | `ClocktowerPairInformationSquareTablePresentationTest`, `ClocktowerNumericInformationOptionPreparationTest`, `ClocktowerInformationStepBuilderManualAuthorityTest`, `ClocktowerNightFullScreenOwnershipTest` | Sufficient |
| Beginner shared-owner regression | same surface plan and selection contract; compact presentation only | `ClocktowerBeginnerCompactNightPresentationTest`, `ClocktowerBeginnerMinimalSurfaceTest`, `ClocktowerNightFullScreenOwnershipTest`, S3 eligibility tests | Sufficient automated coverage |

The existing tests are complementary rather than one end-to-end UI test: each is attached to the
owner that can execute deterministically on the JVM. S4 should run them together as the regression
matrix and add evidence only for the proven gap below.

## 3. Proven defect — draft toggle compares the wrong authority

`ClocktowerJudgeScreen` correctly projects the visible single-target selection as:

```text
Poison -> poisonDraftTarget
Monk   -> monkProtectedDraftTarget
```

The target click callback later compares the tapped name against:

```text
Poison -> poisonTarget             (confirmed fact)
Monk   -> monkProtectedTarget      (confirmed fact)
```

This mismatch predates the reducer-backed confirmed/draft split. It is observable after:

```text
confirmed A
-> Previous
-> choose draft B
-> tap visible selected B again
```

Expected: the visible draft is cleared and S3 disables confirmation.

Current behavior: because `B != confirmed A`, the callback emits `B` again and cannot deselect it.
The reducer and persistence contracts are correct; the defect is in the Host interaction projection.

## 4. Frozen correction

Add one pure selection-edit helper at the existing Host selection-semantics boundary:

```text
visible current selection == tapped selection -> null
otherwise                                    -> tapped selection
```

The Host must calculate this once from `selectedNightName`, which is already the canonical visible
draft projection for every single-target action, then route the result to the action-specific
callback. This fixes Poison and Monk and prevents future confirmed/draft drift across Red Herring,
Butler, Demon attack, Mayor redirect, Demon successor and Ravenkeeper.

Do not change reducer semantics, candidate legality, confirmation eligibility, persistence, or
automatic-mode rules.

## 5. Evidence plan

1. Add a typed RED beside the existing `ClocktowerHostSelectionSemanticsCharacterizationTest`:
   tapping the visible current selection clears it; tapping another selection replaces it; a null
   current selection accepts the tap.
2. Add the helper in `ClocktowerHostSelectionSemantics.kt`.
3. Replace per-action comparisons in the Host callback with one helper result derived from
   `selectedNightName`.
4. Re-run the S4 focused matrix and `:app:testFast`; use `[full-ci]` for the logical checkpoint.
5. Audit exact changed paths and dispatch the existing `Field Test APK` workflow from the immutable
   accepted head for real-device testing.

## 6. Intentional boundaries

- Two-target Fortune Teller and Chambermaid retain their distinct ordered-selection semantics.
- Day selections, Slayer, Artist and Klutz are outside the night-flow matrix.
- App/session revision, history and durable materialization remain unchanged.
- No new source-string test is added. The typed helper protects edit behavior; exact diff and
  compilation protect the final non-callable Compose routing boundary.

## 7. Real-device acceptance checklist

Use the field-test APK produced from the final immutable S4 checkpoint and verify:

1. Experienced ordinary Demon attack reaches the next real step or Dawn without a blank screen.
2. Experienced Mayor attack presents manual `Mayor dies` / redirect selection and continues.
3. Experienced Imp self-kill presents legal successor choice, new-Demon handoff and then continues.
4. On Poison and Monk: confirm A, go Previous, select B, tap B again; selection clears and Next is
   disabled; reselect B, confirm, and continue.
5. Kill a functioning Ravenkeeper and confirm its target/result path.
6. Resume a saved intermediate night and confirm the visible draft/confirmed state remains coherent.
7. Repeat representative ordinary target and automatic ruling paths in Beginner mode.

Automated acceptance can prove structure and state semantics. S4 remains open until this device
check is explicitly reported complete.
