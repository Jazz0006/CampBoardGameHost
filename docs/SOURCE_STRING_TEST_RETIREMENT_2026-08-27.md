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

## 2026-08-28 Global Correctness Review queue

The post-SNE whole-PR review identified the following tests or source-wiring portions for explicit reclassification during **GCR-3**:

```text
ClocktowerDawnDurableMaterializationProductionWiringTest
ClocktowerGlobalObservationProductionWiringTest
InformationDecisionProductionAuthorityWiringTest
ClocktowerDemonSuccessionProductionWiringTest
ClocktowerHistoricalActionProductionWiringTest
ClocktowerNightRestoreProductionOwnershipTest
ClocktowerSameNightEffectiveStateProductionWiringTest
ClocktowerMayorDemonExclusionWiringTest
ClocktowerProductionOtherNightWiringTest
ClocktowerNightTransactionArchitectureGuardTest
source-wiring portion of ClocktowerChambermaidSelectionAuthorityTest
```

This is **not** a bulk-delete instruction. Apply the A/B/C/D classification above before changing each item.

### High-priority production seam candidate

The Dawn restore/retry acceptance now proves typed planning, real durable ActionFact/Observation commits and convergence, but the final base-card/phase mutation is still represented by a test-local materializer while App production orchestration remains partly protected by source inspection.

Preferred future seam, if it can remain narrow and is justified by production architecture rather than test ceremony:

```text
DawnCommitIntent
+ current durable state
-> callable ProductionDawnMaterializer
-> thin App/Compose callback
```

Do not use test retirement as justification for broad App-root decomposition.

## 2026-08-30 risk-based audit

The new evidence policy triggered a fresh audit of source-level tests. The purpose is to remove accidental implementation contracts while preserving real production-consumer and architecture evidence.

### Retired immediately

- `clocktower/flow/ClocktowerActualRoleFlowWiringTest.kt`
  - reason: it asserted exact local variable names, block boundaries, and expressions already duplicated inside `ClocktowerProductionFirstNightWiringTest`;
  - no distinct behavior or architecture invariant was lost by removing the duplicate file;
  - the remaining First Night production guard must itself be narrowed when stable production integration coverage makes its implementation-shaped assertions unnecessary.

### Retained temporarily: unique non-callable production boundary

- `ClocktowerPoisonedSpyFailSafePolicyWiringTest.kt`
  - poisoned-Spy no-Grimoire behavior is a real product policy;
  - existing typed poison, information and Grimoire tests cover their semantic owners but do not yet prove the Host materializer selects the no-Grimoire branch;
  - retirement trigger: a callable typed/integration production publication seam proves both first-night and other-night poisoned-Spy behavior.

- `ClocktowerCurrentDemonProductionWiringTest.kt`
  - current-Demon authority is strongly covered by typed authority/regression tests;
  - the remaining source assertion uniquely checks that Host consumes the canonical authority seam;
  - retirement trigger: callable Host/presentation integration covers canonical current-Demon consumption.

- `ClocktowerSameNightEffectiveStateProductionWiringTest.kt`
  - retained per the earlier SNE closeout because Host effective-subject/poison-lifecycle consumption remains non-callable.

- `ClocktowerMayorDemonExclusionWiringTest.kt`
  - retained per the earlier SNE closeout because typed legality does not yet prove the remaining Host/UI consumer boundary.

### Retained as consolidated/coarse architecture evidence

- `ClocktowerNightTransactionArchitectureGuardTest.kt`
  - remains the consolidated ownership guard for pure-vs-durable transaction boundaries after the SNE-7 wiring-test retirement;
  - future edits should prefer narrowing/consolidation rather than adding neighboring source-wiring tests.

### Next consolidation candidates

The following are not approved for blind deletion. They should be checked for overlap with typed integration coverage and the consolidated architecture guards, then narrowed or retired when no unique production-boundary evidence remains:

```text
ClocktowerDawnDurableMaterializationProductionWiringTest
InformationDecisionProductionAuthorityWiringTest
ClocktowerDemonSuccessionProductionWiringTest
clocktower/flow/ClocktowerProductionFirstNightWiringTest
clocktower/flow/ClocktowerProductionOtherNightWiringTest
AppRootDynamicFlowDecompositionGuardTest
clocktower/flow/ClocktowerNewDemonProductionWiringTest
```

In particular, `ClocktowerProductionFirstNightWiringTest` contains several exact local-variable and source-shape assertions. Preserve only the minimum production ownership/cut-over evidence that cannot yet be expressed by typed integration tests.

### New rule for future correctness work

The source-string test count should not grow merely to enforce RED/GREEN ceremony.

GCR-1 current-Demon continuity and GCR-2 poisoned-Spy information integrity must continue to be owned semantically by typed behavior tests. Temporary source guards may only cover the remaining production-consumer gap and must be retired once that gap becomes callable.
