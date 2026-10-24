# Test cost retirement follow-up

Date: 2026-09-27. Parent: `119c5464a37bbfce1cd5c6733575a7b5a3a7a6f7` on `codex/test-audit-n3150`.
User authorized the recommended retirement plus the broader expert-score and device-report cleanup.
No production source, policy, rules, AGENTS, or handoff changes.

## Decisions and remaining proof

| Surface | Retired cost / assertion | Remaining evidence |
|---|---|---|
| Healthy whole-bundle acceptance | No longer a default FAST/FULL gate; its only callers are experimental/test paths | Unchanged 110,000-combination/100-signature experiment through `testCostExperiments`; canonical domain, projection and exact-bundle contracts stay in regression |
| Drunk pair / numeric / Fortune Teller adapters | Full-catalog counterworld search is removed from the fixtures | All four original tests and assertions retained with explicit small catalogs: current actual roles plus shown ability. Canonical full-domain tests remain in `PairInformationLegalDomainTest`, `FirstNightNumericLegalDomainTest`, `FirstNightBundleCandidateSpaceAuditTest`; public truth/impairment and conjunction are covered by `FirstNightBundleExperimentExactFixtureTest` and `ExactHistoricalHypotheticalObservationBundleEvaluatorTest` |
| A4 ZDD benchmark | Delete 11-sample timing/heap/GC comparison | Unchanged seven `ZddPlayerWorldSetTest` cases protect conversion, direct construction, require/exclude, possible values, checkpoint identity, native/fallback registration handling and golden parity; production device benchmark remains callable |
| A3 enumeration benchmark | Remove repeated timing, heap report and 10-second machine-dependent threshold; rename to smoke test | One exact, nonempty enumeration for each constrained 8/10/12/15-player fixture |
| Topology bundle performance | Full 5–15 sweep/report moves to manual `testCostExperiments` | New `TopologyBundleBoundaryTest` reuses correctness checks for 5/8/12/15-player regimes; no duplicate fixture implementation |
| V4 baseline simulation | Remove thousand-game scale and unused markdown report | 25-sample legality/counts plus duplicate-run determinism; no old distribution preference is imposed |
| Expert recommendation review | Remove legacy-first-legal baseline, comparative average score, recommendation tier, evil-hit and diversity preferences, warmup/timing/report scaffolding | All 24 existing scenarios must return nonempty legal plans; four scenarios retain name-independent deterministic decisions. Existing live production ranking tests are unchanged |
| Device benchmark report | No world generation or three-sample benchmark solely to test log fields | Construct a report with distinct known values and assert the complete public log line, including native/fallback markers and count/timing fields |

The bounded adapter tests prove candidate completeness **within their explicit catalog**, not the old full-catalog combination space. They are not independent exact-count oracles. This is an intentional retirement of experimental breadth, not a claim of equivalent exhaustive coverage.

The device-report test no longer proves benchmark `run()` integration. ZDD behavior/measurement owners remain tested elsewhere; a real device diagnostic is manual evidence, not manufactured by a JVM formatting test.

The topology smoke covers four regimes, not every count from 5 through 15. The complete sweep remains executable manually. ZDD representation parity and current Evin/history/commit/replay contracts are not removed.

## Routing

- FAST includes bounded adapters, expert legality/determinism, small simulation, enumeration and topology smoke.
- FULL adds all seven ZDD representation cases; it includes every remaining ordinary Android JVM regression test.
- `./gradlew :app:testCostExperiments` runs exactly the healthy-bundle acceptance and complete topology performance sweep.
- Five pre-existing calibration/scale experiments retain their dedicated tasks and exclusions. No calibration/evidence files are deleted in this follow-up.
- Historical evidence in the first-pass audit is not a description of the new fixture sizes/routing.

## Validation

Initial focused execution passed all 11 selected cases; the four bounded Drunk cases took 0.236s total versus 90.317s in the prior FULL run. These are local JVM timings, not N3150 measurements.


Final checkpoint: `:app:testFast :app:testFull :app:assembleDebug --max-workers=2` succeeded in **48s**. FAST and FULL actually executed; APK/unchanged production compilation reused up-to-date outputs.

| Suite | Previous | Follow-up | Failures / skipped |
|---|---:|---:|---|
| FAST | 1,546 cases / 355 classes / 13.986s | 1,555 cases / 361 classes / 14.117s | 0 / 0 |
| FULL | 1,564 cases / 364 classes / 188.465s | 1,562 cases / 362 classes / 29.753s | 0 / 0 |
| Manual cost experiments | previously embedded in FULL | 2 cases / 2 classes / 55.817s | 0 / 0 |

Times in this table are summed JUnit suite durations, not wall time or N3150 estimates. FULL's measured suite sum fell **84.2%**; FAST now includes more contracts at approximately the same measured cost. Manual task wall time was 57s.

Inventory verified against current source class declarations and `@Test` counts: FULL contains every ordinary test after exactly seven named experiment exclusions. FAST is a subset of FULL; the difference is exactly seven unchanged ZDD cases. FULL count delta is -1 deleted benchmark -2 manual experiments +1 boundary smoke = -2. FAST adds four bounded Drunk cases, two expert cases, two simulation cases and one topology smoke = +9.

Evidence: `artifacts/test-audit-2026-09-27/retirement-{fast,full,manual}.csv`, focused/checkpoint/manual logs, and `retirement-inventory-check.txt`. Logs normalize trailing whitespace. `git diff --check` passed. No production, AGENTS or handoff diff. Remote CI/R2 remains a merge gate; local results are not remote acceptance.
