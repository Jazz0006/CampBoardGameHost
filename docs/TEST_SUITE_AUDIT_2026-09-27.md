# Test suite audit: FAST cost, retired calibration, N3150 workflow

> Historical first-pass audit. The authorized follow-up in [TEST_COST_RETIREMENT_2026-09-27.md](TEST_COST_RETIREMENT_2026-09-27.md) supersedes retention/routing decisions below.

Date: 2026-09-27, Australia/Sydney. Baseline: `1299fba4`.
Scope: local repository test/configuration maintenance authorized by the user. No production behavior, policy, solver, sample sizes, or exactness assertions changed.

## Findings

The old 770-test timing baseline no longer described the current suite. Fresh FAST execution found **1,551 tests / 357 classes**, all passing, with **187.842s** summed suite time. Four whole-bundle classes alone took **174.373s (92.8%)**. This was the dominant FAST problem, not general test-count growth.

| Class (under `clocktower.recommendation`) | Cases | Baseline seconds | Decision |
|---|---:|---:|---|
| `FirstNightBundleHealthyHarnessAcceptanceTest` | 1 | 71.302 | affected T2 / FULL |
| `FirstNightDrunkPairWholeBundleEvaluatorTest` | 1 | 65.646 | affected T2 / FULL |
| `FirstNightDrunkNumericWholeBundleEvaluatorTest` | 2 | 26.125 | affected T2 / FULL |
| `FirstNightDrunkFortuneTellerWholeBundleEvaluatorTest` | 1 | 11.300 | affected T2 / FULL |

These tests still protect lossless quotienting, complete legal candidate domains, healthy-core/marginal consistency (not independent exact-count parity). They were not deleted or weakened. Their affected-change triggers are recorded in `TESTING_STRATEGY.md`; default FULL still discovers them. FAST keeps `FirstNightBundleCandidateSpaceAuditTest`, `PairInformationLegalDomainTest`, `FirstNightNumericLegalDomainTest`, `FirstNightBundleExperimentContractTest`, `FirstNightBundleExperimentExactFixtureTest`, and the rule/flow/feature tests.

Names and loop counts alone are misleading: `ToleranceCalibrationTest` includes 50,000 samples but took 0.149s. The device benchmark contract tests are also bounded. `A3EnumerationBenchmarkTest` took 1.431s; the Evin C4 regression took 1.921s. These remain in FAST pending measurements on N3150. Do not replace exact assertions or reduce samples just to meet a wall-time target.

The reverse drift also mattered: FULL measured `SetupMigrationTest` at 0.053s, `DemonBluffJointOutputEvaluatorTest` at 0.722s, and the setup/observation/bundle topology differential classes at 0.003/0.008/0.011s. Their current bounded fixtures no longer match the historical expensive classification. These **five classes / eleven cases were restored to FAST**, costing only 0.797s in the FULL sample. Old setup thousand-sample loops are absent; the differential fixtures are bounded. This restores useful inexpensive coverage rather than optimizing only for a lower case count.

## Retirement accounting

The roadmap's seven “clean” 7–9 player scenarios were already removed by `82a4200b` on September 21. The stale roadmap wording was not evidence that those files still existed.

This audit removed **3,220 lines of test-only code**: eleven files plus four experiment methods. There are **13 retired ordinary regression cases and four retired dedicated-experiment cases**. No production source was changed.

| Retired files / methods | Former contract | Why retirement is safe / remaining evidence |
|---|---|---|
| `FirstNightBundleBeginnerRealPresetCalibration.kt` (563 lines) | intermediate Stage-7A preset report and fixture-local factor materialization | No callers or tests remain anywhere in executable sources/workflows. Canonical candidate-space, setup generator, pair/numeric domains and current Fortune Teller corpus pilot remain. It was compiled dead support code, not an executed test. |
| `Sde2D5RoleInformationRealCalibration.kt`, `Sde2D5FRealCalibrationReview.kt` and four methods in `Sde2D5CalibrationExperiment` | extreme seven-player fixture contrast discovery, leave-one-out report, old human review export/manifest gate | The roadmap explicitly retires this fixture as policy evidence. No current expert/SILVER/SDE feature or production caller uses these builders. The remaining cross-regime, Drunk, full-domain bluff/Butler, and ct-01 experiment methods are unchanged. |
| `Sde2D5RoleInformationContrastEvidence{,Test}.kt` (3 cases) | test-only near-raw contrast selection and review data | Only the retired report used this model; its ratio-comparison helper disappears with it. Current exact/topology separation and normalization remain under `NormalizedStrategicDiagnosticsTest`, `StrategicWorldKeyTest`, exact bundle tests and the SDE feature tests. The obsolete report-selection contract is intentionally not preserved. |
| `Sde2D5BundleConfirmationChainEvidence{,Test}.kt` (1 case) | review-only multi-channel/leave-one-out selection | Superseded for production semantics by `ConfirmationChainFeaturesProjectorTest` (restored ambiguity, independent contribution, collapse) and `HistoricalConfirmationChainFeatureProjectorTest` (canonical history and recipient visibility). No parallel review projector is needed after its only report consumer is retired. |
| `Sde2D5FCalibrationReviewExport{,Test}.kt` (3 cases), `Sde2D5FHumanLabelManifest{,Test}.kt` (6 cases) | old label vocabulary, TSV codec, report formatting, human-label completeness for deriving numeric gates | These are solely test-side workflow contracts for the inactive single-reviewer numeric calibration route. They are not game-save codecs or active policy gates. Current typed policy/DecisionTrace/replay contracts and expert evidence are preserved. Historical TSV manifests remain archival artifacts, not executable inputs to policy. |

Post-edit symbol searches found no executable references to any removed type. Test compilation verifies there is no surviving Kotlin dependency. No production visibility/scaffolding changes were needed.

Cheap test-only removals account for just **0.021s** of the baseline suite sum. Their benefit is reducing dead compilation/maintenance surface and misleading workflow authority; almost all execution savings come from re-tiering the four expensive classes. No claim is made that deleting 3,220 lines alone speeds up compilation by a measured amount.

## Explicitly retained / future candidates

- Legacy `MalfunctionPolicy`, temporary automatic Storyteller/scoring and registration behavior remain used by production before surface cutover; their tests must remain. “Legacy” is not a deletion criterion.
- Current A Stud, Evin, Live, Human Remains and B4F SILVER cases are evidence on the active route. Keep cheap legality contracts in FAST and expensive report-generation harnesses behind dedicated tasks.
- The old FN corpus and the remaining `sde2D5Calibration` experiments still exercise shared exact/domain contracts. Their wider retirement would need a separate caller/coverage decision. In particular, the retained legacy ct-01 experiment still has an expensive broad builder; use the bounded B4F task for current SILVER work, not the entire legacy calibration task on N3150.
- Searching tests for production `.kt` reads found one remaining catalog-to-App consumer guard (`BuiltInClocktowerRulesetCatalogTest`). It adds non-callable production wiring evidence and was retained. Most other file-reading tests read JSON assets, not source strings; deleting them as “source tests” would remove useful behavior coverage.
- Shared test-source compilation remains a cost even with `--tests`. Source-set/module separation is a possible later optimization only after N3150 compilation profiling; this audit does not introduce a second compilation graph or dependency boundary speculatively.

## Execution and inventory evidence

Measurements are on the local Mac with Gradle 9.5.0 / the configured JDK 25 daemon, not the future N3150 host. Baseline wall time was **3m40s**, including compilation/startup; summed XML durations are a separate measurement.

| Run | Classes | Cases | Failures / skips | Suite-time sum |
|---|---:|---:|---:|---:|
| Baseline FAST | 357 | 1,551 | 0 / 0 | 187.842s |
| After retirement / expensive-class exclusion | 349 | 1,533 | 0 / 0 | 12.903s |
| Same configuration, task-local forced warm run | 349 | 1,533 | 0 / 0 | 13.307s |
| FULL after retirement | 363 | 1,562 | 0 / 0 | 188.668s |
| Final FAST including restored cheap cases | 354 | 1,544 | 0 / 0 | 14.109s |

The first FAST adjustment reduced summed execution time by **93.1%**. Its exact method-identity diff was 18 removed, zero added: five re-tiered cases plus the thirteen retired report-contract cases above. The final FAST configuration additionally restores eleven cheap cases, yielding **1,544 cases / 354 classes**. The four deleted experiment methods were already excluded from FAST/FULL.

Final FAST passed in **18s wall time**, including incremental test compilation (two tasks executed, 24 up-to-date). Its **14.109s** suite sum is **92.5% below baseline** while restoring the eleven useful cheap cases. FULL minus final FAST is exactly **18 cases / nine classes**; `inventory-check.txt` records the complete class accounting. No current bounded regression disappears from FULL.

Class timing inventories live in [`artifacts/test-audit-2026-09-27`](../artifacts/test-audit-2026-09-27). The dependency-free `tools/testing/summarize_junit.py` reports counts, failures/skips, suite/testcase times, slow classes, and optional exact method inventories. Its aggregation/exit codes were checked with pass/failure/error/skipped, empty and malformed XML inputs. XML is evidence only after the corresponding Gradle task completed; old result files do not establish a new execution. Archived logs/CSVs normalize line endings and trailing whitespace for repository diff checks; test results and task outcomes are unchanged.

`./gradlew :app:testFast :app:testFull --console=plain` completed successfully in **3m29s**, including the test compilation after retirement and both suites. Every FAST method is present in FULL. The source inventory (actual class declarations, not filenames) exactly matches all **363 classes / 1,562 `@Test` methods**, after excluding the same five explicit experiment harnesses as the build. The four re-tiered classes are present and passing in FULL.

`./gradlew :app:testFast --rerun --max-workers=2 --console=plain` verified task-local rerun behavior: **15s wall time, one task executed, 25 up-to-date**, with actual test execution. This was before restoring the eleven cheap cases. `:app:assembleDebug --max-workers=2` subsequently passed in **13s**, three tasks executed and 35 up-to-date. FULL's regression filter and all production behavior remained unchanged by the later FAST restoration; only two test comments were updated to describe that routing.

The dedicated heavyweight calibration experiments were compiled but not executed; this audit removed their obsolete methods without changing the surviving experiment methods. No external ASP/Clingo suite or remote CI/PR operation was performed. Local JVM/build evidence is not remote merge acceptance.

## Historical N3150 workflow — retired from current execution path

This section records the workflow that was proposed during the audit. It is no longer current execution guidance. `TESTING_STRATEGY.md` §12.1 now retires N3150 from the default path because the measured end-to-end cost is too high; GitHub CI/R2 is the normal Android execution/acceptance surface unless the user explicitly reopens the N3150 experiment.

Use a focused `testDebugUnitTest --tests` invocation for a micro-slice, FAST plus affected heavy tests at a logical checkpoint, and FULL/assemble plus selected external gates for acceptance. Use task-local `--rerun` only when fresh execution is needed. Avoid global `--rerun-tasks`, `clean`, daemon teardown and unnecessary APK assembly in routine edit loops. No Android/N3150 host configuration or remote CI/PR state was changed by this local audit.

## Integration onto the formal continuation branch

The audit was transplanted onto `codex/sde-history-prefix-route-closure` at **`24726c492df215d57c49af19b16bcaa18f68e91c`** in an isolated worktree. No merge of the validation branch's history was used. `AGENTS.md`, the current handoff and all production sources remain byte-identical to this base; the roadmap receives only the test-maintenance note.

The changes are split into two commits: FAST routing/tooling (`9c2cdbbc`) and obsolete calibration retirement with its evidence. The first commit executed **1,559 tests / 359 classes**, all passing, before the thirteen report-contract cases were removed.

On the combined destination tree, `./gradlew :app:testFast :app:testFull :app:assembleDebug --max-workers=2 --console=plain` passed in **3m39s** (22 executed tasks, 23 up-to-date):

- FAST: **1,546 tests / 355 classes**, 13.986s suite sum, zero failures/errors/skips;
- FULL: **1,564 tests / 364 classes**, 188.465s suite sum, zero failures/errors/skips;
- debug APK assembly passed;
- FULL equals the current source `@Test` inventory after the five intentional experiment exclusions;
- every FAST method is in FULL, with exactly eighteen additional FULL methods.

The destination includes two newer `RedHerringSetupShadowIntegrationTest` methods, explaining the two-case increase over the original 1,544/1,562 audit counts. The previous timings remain historical baseline evidence, not substitutes for this integration run. `integration-*.csv`, `integration-*.log` and `integration-inventory-check.txt` contain the destination results. N3150 itself has not been benchmarked.
