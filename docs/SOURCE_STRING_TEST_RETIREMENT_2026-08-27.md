# Source-String Test Retirement Policy and SNE Audit

> Role: **ACTIVE TEST-DEBT / RETIREMENT MAP**  
> Date: 2026-08-27  
> Scope: current same-night correctness campaign and adjacent production-wiring tests

## 1. Purpose

Source-string tests were useful while important production behavior lived inside large Compose/App/Host functions that had no callable seam. They are not the preferred long-lived proof of gameplay behavior.

The repository is moving toward typed reducers, planners, projectors, lifecycle helpers, and small transaction seams. As those seams become production-authoritative, source-string behavior tests must be retired instead of forcing new code to preserve old function names, local variable names, formatting, or inline call order.

This cleanup is about replacing proof, not reducing confidence.

## 2. Policy

### Long-lived rule

Business/rules behavior MUST NOT be proved solely by reading production `.kt` source text when a callable typed seam exists or can reasonably be introduced.

Preferred proof order:

```text
typed pure/domain behavior
-> typed session/reducer/planner behavior
-> typed adapter/integration behavior
-> small architecture/ownership guard only where runtime proof is impractical
```

### Temporary source-wiring tests

A source-string wiring test is allowed only when all of the following are true:

1. the production boundary is not yet callable in a focused test;
2. the test protects a real production wiring gap that typed lower-layer tests cannot prove;
3. the test avoids locking whitespace/formatting where possible;
4. the test has a clear retirement trigger;
5. when the production cutover reaches the typed seam, the source-string assertions are deleted or reduced in the same campaign.

### Failure interpretation

When a source-string test fails after a deliberate typed-seam refactor while the owning typed behavior tests remain GREEN:

- do **not** restore a legacy helper, variable name, inline expression, or formatting merely to satisfy the string test;
- first determine whether the source-string assertion is now superseded;
- delete or narrow the superseded assertion when equivalent or stronger typed coverage exists;
- keep only the still-unique production-wiring guard.

### Architecture guards

Source inspection remains acceptable for explicit architecture/ownership invariants, for example preventing App root from reclaiming a responsibility that has been intentionally extracted. Such tests should protect coarse ownership boundaries, not implementation spelling.

## 3. Already retired in SNE-7

### Fully retired

`clocktower/session/NightDawnResolutionPlannerTest.kt`

Reason: its source-string checks for Demon successor materialization, Poisoner-to-Demon poison termination, and stale Mayor redirect legality were replaced by typed `NightDawnResolutionPlanner*ContractTest` behavior coverage.

### Partially retired

`ClocktowerDawnExactDemonSuccessorWiringTest.kt`

Removed assertions that required:

- the legacy `materializeConfirmedNightDemonSuccessor()` helper to exist;
- `onConfirmNewDemon` to call that helper directly.

The remaining assertions still guard the legacy `onConfirmNight` production path until that path consumes the typed planner.

## 4. Current SNE temporary wiring debt

The following tests are intentionally retained for now because the corresponding production App/Host boundary is not yet fully cut over to the typed owner.

| Test | Current reason to keep | Retirement trigger |
|---|---|---|
| `ClocktowerAdvanceNightStepTransactionOwnershipTest` | Host `advanceNightStep` still owns confirmation/audit/record/finalize ordering | delete/replace after reducer/coordinator owns the transaction and Host becomes adapter-only |
| `ClocktowerDawnExactDemonSuccessorWiringTest` | remaining `onConfirmNight` succession gating is still inline production wiring | delete after `onConfirmNight` succession planning consumes `NightDawnResolutionPlanner` |
| `ClocktowerDemonSuccessionProductionWiringRegressionTest` | Host still assembles self-kill facts and stable successor seats | delete after typed succession adapter/integration tests consume the same production seam |
| `ClocktowerDemonSuccessorConfirmationWiringTest` | App callbacks still own draft/confirmed writes and upstream invalidation | delete after those callbacks consume `NightCheckpointReducer` and persistence/restore behavior is covered directly |
| `ClocktowerDemonSuccessorEffectiveRoleWiringTest` | Host still assembles current-role projection inputs | delete after typed Host adapter coverage proves the same projector input/output contract |
| `ClocktowerDemonSuccessorLegalityWiringTest` | Host/UI adapters still expose and consume rules-owned legal target sets inline | replace after legality adapter/UI smoke coverage exists without source inspection |
| `ClocktowerMayorDemonExclusionWiringTest` | recommendation, Host legality, restored fact, and manual UI are still separate production adapters | shrink as each boundary consumes shared typed legality/planner; delete when no unique source-wiring proof remains |
| `ClocktowerPoisonSourceCurrentRoleWiringTest` | Host cursor-relative poison source ownership is still assembled inline | delete after typed production adapter coverage proves effective role/alive/functioning lifecycle |
| `ClocktowerFortuneTellerCurrentDemonWiringTest` | Host current-Demon lookup remains inline production wiring | delete after typed current-role/current-Demon adapter coverage |
| `ClocktowerRegistrationCurrentRoleWiringTest` | registration helpers still depend on Host cursor wiring | delete after typed registration adapter coverage |
| `ClocktowerRoleActorCurrentRoleWiringTest` | normal actor eligibility remains Host cursor wiring | delete after typed actor eligibility adapter coverage |
| `ClocktowerSameNightEffectiveStateProductionWiringTest` | umbrella production-wiring proof while multiple Host paths are still inline | shrink continuously; delete individual assertions as typed production seams become authoritative |
| `clocktower/flow/ClocktowerActualRoleFlowWiringTest` | Host still constructs actual-role vs waking-role inputs | delete after a callable production adapter owns and tests this mapping |
| `clocktower/flow/ClocktowerNewDemonProductionWiringTest` | daytime promotion / next-night identity lifecycle is still App+Host wiring | retain until that separate lifecycle has typed production adapter coverage |

This table is not permission to keep adding source-string tests. It is a retirement list for existing debt.

## 5. Long-lived architecture/ownership examples

Tests such as `AppRootDynamicFlowDecompositionGuardTest` may remain source-based when they protect a deliberate coarse ownership boundary such as:

- App root must not reclaim production night ordering;
- production flow must remain planner/projector backed;
- an extracted presentation or model owner must remain outside App root.

Even these guards should avoid local variable names and exact formatted call strings unless no coarser invariant can express the boundary.

## 6. SNE-7 cleanup order

```text
SNE-7.4 App/Host typed cutover
-> retire source assertions for each cut-over boundary immediately
-> SNE-7.5 restore/reconstruction behavior coverage
-> SNE-7.6 minimal Compose smoke
-> SNE-7.7 remove remaining superseded SNE source-string behavior tests
-> SNE-7.8 keep only minimal architecture/ownership guards
```

Do not postpone all deletion until the end if a source assertion becomes fully superseded earlier.

## 7. Definition of done for SNE source-string retirement

SNE source-string cleanup is complete when:

- gameplay/rules correctness is covered by typed behavior tests rather than production-source spelling;
- App/Host source inspection is limited to still-necessary architecture/ownership boundaries;
- no test requires a legacy helper or local variable name solely because an earlier implementation used it;
- `testFast` failures caused by intentional typed refactors represent real contract/architecture regressions, not obsolete implementation-shape assertions.
