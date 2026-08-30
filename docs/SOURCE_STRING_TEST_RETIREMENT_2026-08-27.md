# Source-String Test Retirement Policy and SNE Audit

> Role: **ACTIVE TEST-DEBT / RETIREMENT MAP**  
> Date: 2026-08-27  
> Review queue refreshed: 2026-08-30

## Decision

Gameplay/rules correctness must move toward typed behavior tests. Source-string tests that inspect production `.kt` implementation shape are not trusted as long-lived behavioral proof.

The repository now follows the risk-based evidence policy in root `AGENTS.md` and `docs/TESTING_STRATEGY.md`: a new source-string RED must not be created merely because an intermediate production edit needs to be made. Existing source tests are reviewed by the behavior or architectural boundary they uniquely protect, not preserved because the suite is append-only.

On 2026-08-27 the 2026-08-26 same-night wiring-test additions were deliberately cleaned up so later bug-fix review can rebuild coverage from independently audited semantics instead of preserving implementation-shaped assertions.

## Removed 2026-08-26 wiring tests

Deleted completely:

- `ClocktowerDemonSuccessionProductionWiringRegressionTest.kt`
- `ClocktowerDemonSuccessorConfirmationWiringTest.kt`
- `ClocktowerDemonSuccessorEffectiveRoleWiringTest.kt`
- `ClocktowerDemonSuccessorLegalityWiringTest.kt`
- `ClocktowerFortuneTellerCurrentDemonWiringTest.kt`
- `ClocktowerPoisonSourceCurrentRoleWiringTest.kt`
- `ClocktowerRegistrationCurrentRoleWiringTest.kt`
- `ClocktowerRoleActorCurrentRoleWiringTest.kt`

The 2026-08-26 source-string additions to these pre-existing tests were also reverted to their pre-day versions:

- `ClocktowerAdvanceNightStepTransactionOwnershipTest.kt`
- `ClocktowerSameNightEffectiveStateProductionWiringTest.kt`
- `clocktower/flow/ClocktowerProductionOtherNightWiringTest.kt`

Typed rule/state tests added or expanded on 2026-08-26 were intentionally retained. In particular, direct tests of effective-night state, Demon succession semantics, poison lifecycle, and checkpoint behavior are not part of this cleanup.

## Rule for future bug-fix review

The production bug fixes from the same campaign are not considered trustworthy merely because the removed wiring tests once passed.

For each bug fix that is re-audited:

1. restate the intended rule/product behavior independently of the current implementation;
2. inspect the production diff and current data flow;
3. determine whether existing stable typed/integration evidence already protects the behavior;
4. create a typed RED at the smallest real semantic owner only when there is a genuine uncovered behavior/regression gap;
5. make or correct the production patch after the required evidence is established;
6. run focused validation and checkpoint-level regression appropriate to the risk;
7. use source inspection only for a minimal architecture/ownership invariant or temporary non-callable production boundary.

Do not recreate the deleted tests merely to recover old coverage counts or to make an intermediate implementation step independently RED.

## Remaining source-based tests

Older source-based ownership/decomposition/wiring tests still exist in the repository. Their existence is not an endorsement of the pattern. Review them when their protected boundary is changed and during periodic test-debt audits.

Classify each test by the distinct evidence it contributes:

```text
A. typed/integration replacement already proves the complete required contract
   and the source assertion adds no distinct production/architecture evidence
   -> retire source-string test

B. behavior matters, but a non-callable production boundary is still the only missing proof
   -> retain the smallest temporary source guard; introduce a typed seam later only if it is
      architecturally justified independently of the test process

C. genuinely architecture/ownership-only invariant
   -> keep a coarse source guard, remove gameplay-detail/order/format assertions

D. obsolete, duplicated, checkpoint-only, or implementation-shape assertion
   -> delete or narrow
```

A source-reading test is therefore not automatically disposable, and a typed lower-layer test does not by itself prove that production actually consumes that layer. Conversely, a source test that merely repeats another source test or fixes exact local variable/format shape contributes no additional confidence.

## SNE-7 closeout retirement

After typed Host transaction, reconstruction and Dawn lifecycle integration became executable, the following temporary implementation-shaped guards were retired:

- `ClocktowerDemonSuccessorReducerProductionWiringTest.kt` during the 7.6A production adapter cut-over;
- `ClocktowerPoisonReducerProductionWiringTest.kt`;
- `ClocktowerMonkReducerProductionWiringTest.kt`;
- `ClocktowerDemonAttackReducerProductionWiringTest.kt`;
- `ClocktowerMayorRedirectReducerProductionWiringTest.kt`;
- `ClocktowerDawnDeathPlannerProductionWiringTest.kt`;
- `ClocktowerNewDemonCheckpointProductionWiringTest.kt`;
- `ClocktowerNewDemonPoisonAuthorityProductionWiringTest.kt`;
- `ClocktowerHostTransactionProductionWiringTest.kt`;
- `ClocktowerDawnExactDemonSuccessorWiringTest.kt`.

Their behavioral replacements are the typed reducer/planner/reconstruction contracts plus `NightCheckpointHostTransactionTest` and `NightTransactionHostIntegrationSmokeTest`. `ClocktowerNightTransactionArchitectureGuardTest` is the consolidated coarse ownership guard for the remaining non-callable Compose/App boundary.

`ClocktowerSameNightEffectiveStateProductionWiringTest` and the Mayor rules/UI ownership guard remain intentionally because they still protect coarse production-consumer boundaries that are not directly callable from JVM tests. Their retention does not make source-string testing the primary correctness layer.

## 2026-08-28 Global Correctness Review

The post-SNE whole-PR review classified the then-current source guards under the A/B/C/D model. The final GCR-3 status is recorded in:

```text
docs/GCR3_SOURCE_STRING_RETIREMENT_AUDIT_2026-08-28.md
```

That audit intentionally slimmed and retained several Class B/C/C guards rather than deleting them. In particular, do not re-open those decisions merely because a test reads `.kt` source. Re-audit only when the protected production boundary changes or becomes callable.

Current examples intentionally retained from that audit include:

```text
ClocktowerDawnDurableMaterializationProductionWiringTest
persistence/ClocktowerGlobalObservationProductionWiringTest
InformationDecisionProductionAuthorityWiringTest
ClocktowerDemonSuccessionProductionWiringTest
persistence/ClocktowerHistoricalActionProductionWiringTest
ClocktowerNightRestoreProductionOwnershipTest
ClocktowerSameNightEffectiveStateProductionWiringTest
ClocktowerMayorDemonExclusionWiringTest
clocktower/flow/ClocktowerProductionOtherNightWiringTest
ClocktowerNightTransactionArchitectureGuardTest
source-wiring portion of ClocktowerChambermaidSelectionAuthorityTest
ClocktowerCurrentDemonProductionWiringTest
ClocktowerPoisonedSpyFailSafePolicyWiringTest
```

Do not extract a production seam solely to remove one of these remaining guards. A seam should exist only when production architecture independently benefits from it.

## 2026-08-30 risk-based audit

The risk-based evidence policy triggered a fresh audit of historical source-level tests, especially tests created during the App-root / Host decomposition campaign. The key distinction is between a **completed extraction checkpoint** and a **durable architecture boundary**.

### Retired: completed extraction/location checkpoints

The following tests primarily asserted that a declaration had moved into a named `.kt` file, no longer appeared in a former owner, or retained a stage-specific source layout. The extraction work is already accepted history; keeping these tests would freeze temporary file placement without adding behavioral confidence.

```text
AppGameModelsOwnershipTest.kt
ClocktowerAppModelsOwnershipTest.kt
ClocktowerHostSelectionSemanticsOwnershipTest.kt
HostInteractionUiOwnershipTest.kt
GameSettingsUiOwnershipTest.kt
WerewolfPresentationOwnershipTest.kt
ClocktowerNewDemonPresentationOwnershipTest.kt
AppDealPresentationOwnershipTest.kt
AppGameReviewPresentationOwnershipTest.kt
AppClocktowerPresentationThemeOwnershipTest.kt
AppClocktowerLandingPresentationOwnershipTest.kt
AppGameScreenPresentationOwnershipTest.kt
AppPlayerSetupPresentationOwnershipTest.kt
AppSettingsPresentationOwnershipTest.kt
ClocktowerNightStepUiOwnershipTest.kt
ClocktowerPlayerDisplayUiOwnershipTest.kt
ClocktowerRegistrationUiOwnershipTest.kt
ClocktowerStorytellerRecommendationCardOwnershipTest.kt
ClocktowerStorytellerRecommendationUiOwnershipTest.kt
```

These tests were useful as temporary RED/GREEN extraction scaffolding. They are not durable product contracts.

### Retired: obsolete or duplicated source-shape checks

```text
clocktower/flow/ClocktowerActualRoleFlowWiringTest.kt
ClocktowerLegacyFallbackOwnershipTest.kt
ClocktowerAdvanceNightStepTransactionOwnershipTest.kt
clocktower/flow/ClocktowerProductionDebugUiCleanupTest.kt
```

Reasons:

- duplicate source assertions already covered by a remaining production boundary plus typed behavior;
- exact callback/token ordering that is better owned by typed checkpoint/transaction tests and the consolidated night-transaction architecture guard;
- checking that historical fallback/debug strings remain absent after an accepted cleanup;
- no unique behavior or architecture evidence remained.

### Mixed tests narrowed instead of deleted

`clocktower/flow/ClocktowerProductionFirstNightWiringTest.kt`

- removed import, materializer-identity, legacy-order-table and other exact implementation-shape assertions;
- retained only the non-callable Host adapter invariant separating actual role identities from Drunk waking identities.

`ClocktowerInformationStepBuilderOwnershipTest.kt`

- removed detailed scans of builder internals and forbidden dependency strings;
- retained only Host -> `ClocktowerInformationStepBuilder` / `build` ownership to prevent a parallel inline information path from returning.

`clocktower/flow/ClocktowerNewDemonProductionWiringTest.kt`

- reduced five detailed source tests to two coarse lifecycle boundaries;
- retained restore -> canonical Other Night new-Demon identity flow and clear-at-night-completion ownership;
- typed interaction identity/order/checkpoint semantics remain in `ClocktowerNewDemonIdentityContractTest`.

`persistence/AppJsonPrimitivesTest.kt`

- removed extraction-owner and forbidden-source-content tests;
- retained enum lookup, nullable JSON primitive and string-array behavior tests.

`persistence/ActiveGameProductionPersistenceWiringTest.kt`

- reduced detailed field/local-variable assertions to canonical save ownership and restore validation-before-mutation boundaries;
- typed `ActiveGamePersistenceCoordinator` and ruleset persistence tests own schema/content semantics.

`persistence/ActiveGameSemanticHistoryProductionWiringTest.kt`

- reduced detailed source-state assertions to canonical semantic-history save/restore validation and new-Clocktower global-mode ownership;
- typed persistence/session tests own compatibility and history semantics.

### Intentionally retained behavior tests despite historical names

Do not classify by filename alone. These tests directly call typed helpers/models and therefore remain normal behavioral evidence:

```text
ClocktowerHostDecompositionCharacterizationTest
ClocktowerHostPresentationModelsCharacterizationTest
ClocktowerHostSelectionSemanticsCharacterizationTest
ClocktowerFortuneTellerPhaseAuthorityTest
ClocktowerOtherNightWakingRoleAuthorityTest
ClocktowerPendingSuccessionFlowAuthorityTest
ClocktowerDemonAttackProductionAdapterTest
ClocktowerNightStepMaterializerRegistryTest
```

### Temporary mixed source/behavior test retained pending a natural seam

`StructuredEmpathInformationAdapterTest.kt` contains strong typed behavior tests plus several source assertions around the non-callable Host/UI adapter. Those source assertions remain for now because they uniquely cover production delivery of:

- telemetry commit only after a real automatic preview;
- impaired recommendation derivation from the unreliable display-option path;
- previous shown number propagation into later-night Empath decisions.

Retirement trigger: those production adapter inputs/side effects become callable through an independently justified typed UI/Host seam. Do not create a seam solely to remove these assertions.

### Durable coarse guards retained

The following are examples of source-reading tests that remain justified because their purpose is architecture or a non-callable final production boundary, not intermediate file placement:

```text
AppRootDynamicFlowDecompositionGuardTest
ClocktowerNightTransactionArchitectureGuardTest
ClocktowerCurrentDemonProductionWiringTest
ClocktowerPoisonedSpyFailSafePolicyWiringTest
ClocktowerSameNightEffectiveStateProductionWiringTest
ClocktowerMayorDemonExclusionWiringTest
ClocktowerDawnDurableMaterializationProductionWiringTest
InformationDecisionProductionAuthorityWiringTest
ClocktowerDemonSuccessionProductionWiringTest
ClocktowerNightRestoreProductionOwnershipTest
clocktower/flow/ClocktowerProductionOtherNightWiringTest
persistence/ClocktowerGlobalObservationProductionWiringTest
persistence/ClocktowerHistoricalActionProductionWiringTest
persistence/ClocktowerHistoricalActionLifecycleProductionWiringTest
```

These should shrink or disappear when the final App/Host boundary becomes callable naturally, but they should not be removed merely to reduce source-test count.

## New rule for future correctness work

The source-string test count should not grow merely to enforce RED/GREEN ceremony.

For future work:

1. prefer direct typed behavior at the semantic owner;
2. use existing behavioral evidence for mechanical refactors when it already proves the contract;
3. treat extraction/location tests as temporary scaffolding and retire them after the checkpoint is accepted;
4. keep source inspection only for a coarse architecture invariant or the last non-callable production-consumer gap;
5. record a retirement trigger for every temporary source guard when practical;
6. do not introduce production abstractions solely to make tests more aesthetically pure.
