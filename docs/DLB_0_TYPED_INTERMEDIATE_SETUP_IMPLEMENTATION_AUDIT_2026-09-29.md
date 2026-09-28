# DLB-0 Typed Intermediate Setup — Live Fan-out and Implementation Audit

> Date: 2026-09-29 Australia/Sydney
> Baseline: `main@9ccf895bec8e8158ec0181a1ca041029f601ac02`
> Scope: DLB-0 only — typed intermediate setup contract before any production deal cutover.
> Product authority: `DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md`.

## 1. Architecture pre-flight

- current owner: Trouble Brewing preset metadata/validation, `TroubleBrewingProductionSetupPreparer`, generic shown-identity commitment, and `TroubleBrewingSetupDealPlanner`.
- proposed responsibility: represent one post-roster/post-seat, pre-Drunk-commit setup state in which shown identities are assigned to stable seats and only the latent fact `hasDrunk` is known.
- authoritative state owner(s): preset/setup code owns visible-roster realization; this DLB-0 model owns no mutable state and performs no recommendation; later rules/setup code will own Drunk legality and canonical setup/session will own the committed Drunk.
- narrow typed input/output seam: validated preset + optional transitional added Townsfolk identity -> realized visible roster; realized visible roster + canonical shown-seat assignments + provenance -> immutable intermediate setup.
- keep in current owner / extract: add a pure Trouble Brewing setup contract under `clocktower/setup`; do not modify App/Host, generic `SetupShownIdentityCommitter`, SDE, persistence, Recovery, or current runtime wiring in DLB-0.
- reason: DLB requires a state that can exist after shown identities are seated but before any seat is actual Drunk. The current deal model cannot represent that state because it already contains `actualRoleId=drunk`.

## 2. Live production ownership and fan-out

### Current live runtime producer chain

```text
TroubleBrewingSetupPresetDataset
-> TroubleBrewingProductionSetupPreparer
-> TroubleBrewingShownIdentityPolicySource
-> SetupShownIdentityCommitter
-> TroubleBrewingSetupPresetSelection(selectedDrunkShownRole)
-> TroubleBrewingSetupDealPlanner
-> TroubleBrewingSetupDealPlan(actualRoleId + shownRoleId)
-> TroubleBrewingDealRoleResolver
-> CampBoardGameHostApp PlayerCard materialization
-> SetupRecommendation / prewarm
-> TroubleBrewingCommittedSetupAdapter
```

The old `TroubleBrewingSetupPresetSelector` still contains `selectedDrunkShownRole` logic, but live production search finds no production caller. Its `TroubleBrewingSetupPresetRotationScorer.scoreFinalWeight` path is likewise not the current runtime setup selector. DLB implementation must follow the live preparer/generic-setup path rather than restore this legacy seam.

### Shared owner that must not be repurposed

`SetupShownIdentityCommitter` is shared by Trouble Brewing and No Greater Joy. DLB must not change its global semantics to mean “late-bound Drunk.” Trouble Brewing will stop depending on its Drunk override in DLB-1; No Greater Joy remains intentionally exempt.

### Old Drunk-bound fan-out intentionally preserved in DLB-0

The following remain unchanged in this slice and are later migration targets:

- `TroubleBrewingProductionSetupPreparer`
- `TroubleBrewingSetupPresetSelection.selectedDrunkShownRole`
- `TroubleBrewingSetupDealPlan.selectedDrunkShownRole`
- `TroubleBrewingSetupDealPlanner`
- `TroubleBrewingSetupRotationRecordFactory`
- `TroubleBrewingSetupRotationHistory`
- `TroubleBrewingSetupRotationHistoryStore`
- `TroubleBrewingSetupCompletionPersistence`
- App setup startup / reveal / prewarm wiring
- setup recommendation/SDE consumers of already committed Drunk state

DLB-0 adds the new contract in parallel. It does not delete, reinterpret, or wire over these current producers/consumers yet.

## 3. DLB-0 typed model

### `TroubleBrewingVisibleRoster`

Immutable realized visible identities:

- `hasDrunk: Boolean`
- `townsfolkRoleIds: List<String>`
- `outsiderRoleIds: List<String>`
- `minionRoleIds: List<String>`
- `demonRoleIds: List<String>`

For a Drunk preset, the realizer removes `drunk` from visible outsiders and adds exactly one validated `drunkAsOptions` identity to the visible Townsfolk list.

Important: the realized roster does **not** retain which Townsfolk came from `drunkAsOptions`. Once realized, that identity has no privileged Drunk ownership.

For a non-Drunk preset, no added identity is allowed and the visible roster is the preset roster unchanged.

### `TroubleBrewingShownSeatAssignment`

Immutable shown-seat fact:

- `seat: Int`
- `playerName: String`
- `shownRoleId: String`

It deliberately has no `actualRoleId`.

### `TroubleBrewingIntermediateSetup`

Immutable pre-Drunk-commit setup state:

- dataset/schema/preset provenance
- player count and setup seed
- `visibleRoster`
- canonical ordered `shownSeatAssignments`

Invariants:

- seat numbers are exactly `1..playerCount`;
- player names are nonblank and unique;
- shown role multiset exactly equals the realized visible roster;
- visible roster contains exactly `playerCount` identities;
- visible role IDs are nonblank and unique;
- if `hasDrunk`, literal `drunk` is not a visible identity;
- there is no field for `selectedDrunkShownRole`, added-role provenance, actual role, or Drunk seat.

This model owns representation/invariants only. It does not select a Drunk, rank candidates, mutate a session, persist a draft, or render UI.

## 4. DLB-0 realizer

Add a pure `TroubleBrewingVisibleRosterRealizer`:

```text
validated preset
+ addedVisibleTownsfolkRoleId?   // transitional input only
-> TroubleBrewingVisibleRoster
```

For a Drunk preset, the input must be one of `preset.drunkAsOptions` and must not already be an actual preset role. For a non-Drunk preset it must be null.

The transitional input is intentionally not stored in the result. DLB-1 will change the runtime producer that supplies this input; DLB-0 does not choose it.

## 5. Test boundary

New owning T0: `TroubleBrewingIntermediateSetupTest`.

Durable contracts to prove:

1. a Drunk preset realizes one extra visible Townsfolk, removes visible `drunk`, preserves player-count cardinality, and does not retain privileged added-role/Drunk-seat state;
2. a non-Drunk preset preserves its visible roster and rejects an added Townsfolk input;
3. the intermediate setup accepts only canonical shown-seat assignments whose shown-role multiset exactly matches the visible roster.

Because this is a genuinely new typed contract, a test-first RED is appropriate. The first RED may be compilation failure because the new contract does not yet exist; production implementation follows only after that exact test-only head is observed failing for the expected missing-symbol reason.

Existing runtime behavior remains unchanged in DLB-0. At GREEN, run the owning test plus `TroubleBrewingProductionSetupPreparerTest` / shared shown-identity coverage as focused preservation evidence, then T1 at the logical checkpoint.

## 6. Symbol allowlist for DLB-0

Expected new files only:

- `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingIntermediateSetup.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingIntermediateSetupTest.kt`
- this audit document

No App/Host, SDE, persistence, Recovery, asset, policy V1, or existing setup/deal file should change in DLB-0.

## 7. Explicit NO-GO in this slice

- no runtime cutover;
- no removal of `selectedDrunkShownRole`;
- no changes to `BEGINNER_CONSERVATIVE_V1`;
- no SDE Drunk candidate ranking;
- no canonical Drunk commit;
- no Experienced UI;
- no Red Herring / Poisoner / Demon bluff sequencing changes;
- no Recovery compatibility work;
- no App/Host decomposition;
- no generic setup-effect or transaction owner.


## 8. RED / GREEN implementation evidence

### RED

Test-only head:

`46b6a6dd391408f8da99f30a58c520cef462e288`

GitHub CI #3504 / run `36499359330` failed in Android compilation exactly because the new DLB-0 contract did not yet exist:

- unresolved `TroubleBrewingVisibleRosterRealizer`;
- unresolved `TroubleBrewingIntermediateSetup`;
- unresolved `TroubleBrewingShownSeatAssignment`.

R2 #3249 / run `36499359292` was GREEN. No production file existed on the RED head.

### GREEN / T1

Production head:

`e37c19221c075990473ceed712fafb1cd3dd9d40`

GitHub CI #3505 / run `36499609448` executed:

`./gradlew :app:testFast --no-daemon --build-cache`

and completed `BUILD SUCCESSFUL`. The Android job completed GREEN; CI gate completed GREEN. R2 #3250 / run `36499609438` also completed GREEN.

The PR diff at this checkpoint contains exactly the DLB-0 production contract, its owning typed test, and this audit document. Existing Trouble Brewing runtime wiring remains unchanged.

### Acceptance escalation

The next commit carries `[full-ci]` only to escalate this exact DLB-0 logical checkpoint to T4. It introduces no additional production behavior.


## 9. T4 acceptance result

Accepted executable checkpoint:

`f81350ec1341cb83f2b4c2f31d80b9c61c7cec52`

Exact-head remote acceptance:

- CI #3506 / run `36499904998`: GREEN;
- Android: `./gradlew :app:testFull :app:assembleDebug --no-daemon --rerun-tasks` — GREEN, `BUILD SUCCESSFUL`;
- ASP contract tests — GREEN;
- Real Clingo cross-validation — GREEN;
- aggregate CI gate — GREEN;
- R2 #3251 / run `36499905042` — GREEN.

This establishes DLB-0 as COMPLETE at the executable checkpoint above. Any later commit in this PR that changes only documentation must not be relabelled as a different executable acceptance tree.
