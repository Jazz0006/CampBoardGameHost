# PR #57 TBSP Test Audit — Risk-Based Evidence Review

> Date: 2026-08-30 Australia/Sydney  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> PR: #57 — OPEN / DRAFT / NOT MERGED  
> Policy basis: root `AGENTS.md`, `docs/TESTING_STRATEGY.md`, `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`

## 1. Purpose

Re-audit tests introduced by PR #57 after the repository adopted risk-based / evidence-driven testing. The goal is not to minimize test count. It is to retire implementation-shaped RED scaffolding and preserve durable behavioral or production-boundary evidence.

## 2. Retired

### `TroubleBrewingSetupRecommendationProductionWiringTest.kt`

Deleted on 2026-08-30.

Reason:

- the test read `CampBoardGameHostApp.kt` / `ClocktowerHostScreen.kt` and asserted exact call spellings such as `onCommittedDeal`, `launch(Dispatchers.Default)` and a specific provider expression;
- the stable lifecycle contract is already executable through `TroubleBrewingSetupRecommendationRevealCoordinatorTest`;
- exact-request cache semantics are already executable through `TroubleBrewingSetupRecommendationPrewarmCoordinatorTest`;
- keeping a newly-created source RED solely to force the intermediate App/Host wiring would violate the new repository policy.

This retirement does **not** mark TBSP-6G-B production wiring complete. It only changes the evidence model from source-RED ceremony to typed-contract-first + production wiring + focused/compile/CI validation.

## 3. Narrowed App-root source guards

These tests still protect a real non-callable App-root boundary, but detailed implementation-shape assertions were removed.

### `TroubleBrewingProductionStartWiringTest.kt`

Retained only:

- Trouble Brewing production routes through the curated start owner before the legacy generator;
- curated start consumes the typed production setup/deal owners;
- curated dealing does not call the broad random generator or synchronously run setup recommendation.

Removed as redundant implementation detail:

- parser call spelling;
- rotation-store call spelling;
- duplicated lower-layer sequencing already proved by typed tests.

Retirement trigger: production start becomes callable through a stable typed application/service boundary that proves the cutover and non-blocking behavior.

### `TroubleBrewingActiveGameProvenanceWiringTest.kt`

Retained only:

- curated start commits selected provenance into active-game ownership;
- active snapshot uses the canonical provenance codec;
- restore consumes canonical provenance and does not rerun setup selection/materialization.

Codec schema/round-trip semantics remain owned by `TroubleBrewingSetupProvenancePersistenceTest`.

Retirement trigger: active-game save/restore production integration becomes callable through a typed persistence/application boundary.

### `TroubleBrewingCompletionRotationHistoryWiringTest.kt`

Retained only:

- non-Clocktower / non-TB / incomplete / missing-provenance states cannot reach rotation-history recording;
- completion persistence happens before review archive or active-save clearing;
- generic archive delegates instead of directly recording every restart.

Store idempotence, conflict rejection, retention and corruption recovery remain owned by `TroubleBrewingSetupRotationHistoryStoreTest`.

Retirement trigger: completed-game archival becomes callable through a typed transaction/application boundary.

## 4. Typed tests retained

The following PR #57 tests directly execute stable domain/session/persistence behavior. They remain valuable across internal refactors and should not be removed merely because they were added during TBSP development:

```text
clocktower/session/TroubleBrewingSetupRecommendationLockTest.kt
clocktower/session/TroubleBrewingSetupRecommendationPrewarmCoordinatorTest.kt
clocktower/session/TroubleBrewingSetupRecommendationRevealCoordinatorTest.kt
clocktower/setup/TroubleBrewingDealRoleResolverTest.kt
clocktower/setup/TroubleBrewingProductionSetupPreparerTest.kt
clocktower/setup/TroubleBrewingSetupDealPlannerTest.kt
clocktower/setup/TroubleBrewingSetupPresetJsonTest.kt
clocktower/setup/TroubleBrewingSetupPresetRotationScorerTest.kt
clocktower/setup/TroubleBrewingSetupPresetSelectorTest.kt
clocktower/setup/TroubleBrewingSetupPresetValidatorTest.kt
persistence/TroubleBrewingSetupProvenancePersistenceTest.kt
persistence/TroubleBrewingSetupRotationHistoryStoreTest.kt
```

### Why they remain

They cover durable contracts including:

- frozen dataset/schema/pool identity and semantic validity;
- exact preset composition and Baron no-double-application;
- deterministic seed replay and independent seat assignment;
- actual Drunk vs shown-role identity;
- history-aware selection thresholds, decay and fallback;
- setup recommendation identity lock;
- exact-request prewarm cache behavior;
- reveal-before-background-dispatch lifecycle;
- exact-request ready reuse and stale-request safe fallback;
- provenance round-trip / legacy-null / mismatch rejection;
- completion-history idempotence, conflict rejection, retention and corrupt-state recovery.

These are behavior tests, not temporary source-location checkpoints.

## 5. Current evidence model for TBSP-6G-B

Stable typed contract already exists:

```text
TroubleBrewingSetupRecommendationPrewarmCoordinatorTest
TroubleBrewingSetupRecommendationRevealCoordinatorTest
```

Therefore the remaining production slice follows:

```text
existing typed GREEN contract
-> App/Host production wiring
-> focused typed tests
-> Android compile / :app:testFast at logical checkpoint
-> exact diff audit
-> GitHub CI / R2 checkpoint
```

Do not recreate a source-string RED solely to make the App/Host edit independently RED.

## 6. Scope conclusion

The PR #57 test suite is not append-only. After this audit:

- one temporary 6G-B source-wiring RED is retired;
- three App-root source tests remain, but are reduced to coarse final-boundary guards;
- the typed TBSP tests remain intentional and form the primary correctness layer.

Future TBSP source guards should shrink or disappear only when their final App/Host boundary becomes naturally callable. Do not introduce a production abstraction solely to delete a source test.
