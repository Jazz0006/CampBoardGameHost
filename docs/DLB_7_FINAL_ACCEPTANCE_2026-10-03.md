# DLB-7 — Final Acceptance Audit

> Date: 2026-10-03  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `dlb-7-final-acceptance`  
> Draft PR: #196 — `DLB-7: finalize Drunk late-binding acceptance`  
> Status: **READY FOR FINAL T4 — acceptance matrix complete; final [full-ci] checkpoint pending**

## 1. Scope

DLB-7 is the final acceptance gate for the Drunk late-binding / staged first-night sequencing route.

It does not introduce new recommendation policy semantics. Its job is to prove that the completed DLB implementation satisfies the route-wide behavior matrix after DLB-6 removed the old compatibility contracts.

The accepted Q04 policy boundary remains unchanged:

- `DRUNK_ASSIGNMENT_Q04_V1` is the bounded evidence-backed production Drunk policy;
- `BEGINNER_CONSERVATIVE_V1` remains immutable;
- `DRUNK_ASSIGNMENT_SHADOW_V1` remains deferral-only;
- C5 / `BEGINNER_CONSERVATIVE_V2` remains a separate E3/E4 evidence gate.

## 2. Acceptance matrix

| DLB-7 requirement | Direct acceptance evidence | Result |
| --- | --- | --- |
| Legal visible roster for every Drunk template | `Dlb7TroubleBrewingAcceptanceTest.every built in Drunk template option exposes every and only dealt Townsfolk and commits one Drunk` loads the built-in 5–15 player preset dataset and iterates every Drunk preset plus every `drunk_as_options` value. | PASS at dataset-wide checkpoint `c773e643df7b214f336578b16b042b834c85cc0e`; CI #3642 / R2 #3360 GREEN. |
| Every eligible dealt Townsfolk can be projected as Drunk | Same dataset-wide test enumerates the complete rules-owned legal candidate domain for each realized Drunk roster and projects every candidate. `TroubleBrewingDrunkCandidateProjectionTest.every legal candidate projects exactly that seat as good outsider drunk while shown identity stays dealt townsfolk` protects the typed seam. | PASS |
| No non-Townsfolk legal candidate | Dataset-wide legal candidate role set is asserted equal to the visible Townsfolk set. `TroubleBrewingDrunkCandidateProjectionTest.non drunk setup has no candidates and non townsfolk candidate is rejected without mutating input` protects rejection behavior. | PASS |
| Exactly one committed Drunk when required | Dataset-wide test commits every legal candidate and asserts exactly one actual Drunk at the chosen seat. `TroubleBrewingSetupCommitterTest.legal original townsfolk candidate commits exactly one Drunk while preserving shown identities` protects the canonical commit seam. | PASS |
| Shown identity remains the dealt Townsfolk identity | Dataset-wide projection/commit assertions compare the candidate external role with the canonical shown role. Existing projector/committer tests assert the same invariant on focused fixtures. | PASS |
| Drunk-as-Fortune-Teller wakes as Fortune Teller without actual-Fortune-Teller Red Herring ownership | `ClocktowerDrunkFortuneTellerFlowTest.Drunk shown Fortune Teller wakes without creating the real Fortune Teller setup ability` and its production-seam test distinguish waking roles from actual roles. | PASS |
| Actual Fortune Teller gets a Red Herring before the earliest observer/result dependency | `ClocktowerDrunkFortuneTellerFlowTest.actual Fortune Teller still creates red herring before the role interaction` plus `FirstNightSetupDependencyPlannerTest` earliest-semantic-dependency / earlier-observer cases. | PASS |
| Poison confirmation advances canonical revision and invalidates stale downstream plans | `Sde2PoisonReplanningTest.Poisoner change stales all uncommitted plans and broadly recomputes exact consequences` verifies draft input revision, one committed state revision, stale-plan invalidation and fresh replanning while committed history remains stable. | PASS |
| DecisionTrace/replay correlates selected Drunk candidate | `DrunkAssignmentShadowReplayTest.Q04 replay over reconstructable G10 preserves Empath when its topology fails the Q04 condition` verifies legal candidate identity, Q04 evaluation and historical actual-choice correlation. Existing shadow replay tests preserve legal-domain order/features. | PASS |
| Current-version Recovery restores finalized setup only | `RecoverySnapshotJsonCodecTest.clocktowerRecoveryRoundTripsFinalDrunkActualAndShownIdentity` explicitly round-trips finalized `actual=Drunk / shown=Townsfolk` identity. Existing `clocktowerRecoveryKeepsConfirmedFactsButDiscardsDraftTargetsAndDayUi`, `RecoveryRestorePlannerTest.clocktowerConfirmedFactsRoundTripWhileDraftTargetsRemainAbsent`, and current-only Recovery validation protect confirmed-only/current-format behavior. | PASS pending final T4 execution of the new explicit identity test. |
| Non-Drunk templates remain behaviorally unchanged | `Dlb7TroubleBrewingAcceptanceTest.every built in non Drunk template remains candidate free and commits shown identities unchanged` iterates every built-in non-Drunk preset and verifies exact visible role set, empty Drunk candidate domain, no committed Drunk, and `actual == shown`. | PASS at dataset-wide checkpoint `c773e643df7b214f336578b16b042b834c85cc0e`; CI #3642 / R2 #3360 GREEN. |

## 3. Dataset-wide coverage

The new DLB-7 dataset gate consumes the production asset:

`app/src/main/assets/setup/trouble_brewing_setup_presets_v2_final.json`

It validates the complete built-in 5–15 player pool rather than one synthetic setup. For each Drunk preset, every declared absent-Townsfolk option is realized and every resulting legal Townsfolk candidate is projected and canonically committed.

This closes the only material acceptance-coverage gap found by the DLB-7 audit. The remaining route requirements already had direct typed or integration coverage.

## 4. Recovery boundary

DLB-7 does not reopen legacy Recovery migration.

The Recovery acceptance requirement means:

```text
current Recovery format
+ finalized actual/shown identities
+ confirmed durable mechanics/history
-> current validated restore
```

It does not mean persisting a precommit Drunk candidate, pending manual selection, old DealPlan, or old `selectedDrunkShownRole` setup authority.

The new explicit Recovery test verifies that a finalized Drunk retains:

- actual role: Drunk;
- shown role: the dealt Townsfolk identity.

Draft targets/UI state remain absent under the already-accepted current-only Recovery contract.

## 5. Producer / consumer closure

DLB-6 already established that the obsolete production ownership is gone:

```text
compatibilityConfirmedDrunkCandidate      0
CompatibilityImmediate                   0
TroubleBrewingSetupPresetSelection       0
TroubleBrewingSetupDealAssignment        0
TroubleBrewingSetupPresetSelector        0
TroubleBrewingSetupPresetRotationScorer  0
TroubleBrewingCommittedSetupAdapter      0
```

The old `selectedDrunkShownRole` literal remains only in bounded v2 rotation/completion migration readers and is not part of the current model.

DLB-7 does not reopen those contracts.

## 6. Final T4 gate

Before DLB-7 is marked COMPLETE / ACCEPTED, create an exact logical checkpoint whose commit message contains `[full-ci]`.

Required final acceptance:

- Android `:app:testFull`;
- Android debug assemble/compile;
- ASP contract validation;
- Real Clingo cross-validation;
- independent R2;
- exact PR head clean/mergeable;
- zero unresolved review threads.

After that exact head is GREEN, update this document and the current roadmap/handoff, then merge PR #196 under the standing merge authorization.

## 7. Next route after acceptance

```text
DLB-7 COMPLETE / ACCEPTED
-> DLB campaign COMPLETE
-> TBGS-2 incremental runtime recommendation-state migration / A3 re-audit NEXT
```

C5 / `BEGINNER_CONSERVATIVE_V2` remains separately blocked on qualifying E3/E4 evidence.
