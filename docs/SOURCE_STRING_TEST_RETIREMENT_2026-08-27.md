# Source-String Test Retirement Policy and SNE Audit

> Role: **ACTIVE TEST-DEBT / RETIREMENT MAP**  
> Date: 2026-08-27

## Decision

Gameplay/rules correctness must move toward typed behavior tests. Source-string tests that inspect production `.kt` implementation shape are not trusted as long-lived behavioral proof.

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
3. create a typed RED at the smallest real semantic owner;
4. require assertion-level RED provenance when tests-first is applicable;
5. make or correct the production patch only after the RED is valid;
6. run focused GREEN and checkpoint-level regression;
7. use source inspection only for a minimal architecture/ownership invariant when no callable seam can express the boundary.

Do not recreate the deleted tests merely to recover old coverage counts.

## Remaining source-based tests

Older source-based ownership/decomposition/wiring tests still exist in the repository. Their existence is not an endorsement of the pattern. Review them when their protected boundary is changed, and retire them when typed production coverage becomes available.
