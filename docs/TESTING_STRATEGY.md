# CampBoardGameHost Testing Strategy

> Role: **LONG-LIVED TEST EXECUTION / VALIDATION STRATEGY**
> Baseline: S1.1/S1.2 measured at `d52f53b4a1821cc000368c393721d1d5a073aafc`
> Date: 2026-08-25

## 1. Purpose

The purpose of this strategy is not to reduce the number of tests. It is to maximize confidence per unit of feedback time while preserving complete regression coverage.

Frequent developer feedback and complete regression validation are separate concerns. Tests must not be deleted merely because they are expensive, broad, or old. Expensive tests remain mandatory coverage; their execution frequency is governed by the affected semantic area.

## 2. Measured baseline

The S1.1 Android JVM baseline contained 770 tests with 0 failures, 0 errors, and 0 skipped tests.

- Forced full rerun Run 1: 208.65s
- Forced full rerun Run 2: 100.06s
- JUnit testcase duration sum: 61.625s
- Warmed no-change Gradle floor: approximately 5.5–5.8s
- Focused tiny-class Gradle invocation: approximately 5.5–6.3s
- ASP corpus validation: approximately 0.14s
- ASP Python tests: approximately 0.08s
- Real Clingo cross-validation: approximately 16.12s

The first forced Android run contained substantial environment, compilation, and startup noise. The 154s average of the two forced runs is not a stable performance target.

Six Android classes account for approximately 90% of testcase duration:

- `SetupMigrationTest`: approximately 31–34s
- `ZddPlayerWorldSetTest`: approximately 10–11s
- `ExpertRecommendationReviewTest`: approximately 5–6s
- `StorytellerV4BaselineSimulationTest`: approximately 3–4s
- `A4ZddBenchmarkTest`: approximately 2–3s
- `A3EnumerationBenchmarkTest`: approximately 1s

The repository therefore has a small expensive tail, alongside significant Gradle/configuration/compile/startup cost. The 770 tests are not generally slow.

S2.1 established executable Android JVM suites at commit `99b340635e04abd64341e5aa2fc202d6c46d9842`:

- `:app:testFast`: 751 tests, 0 failures/errors/skipped
- `:app:testFull`: 770 tests, 0 failures/errors/skipped
- `FULL - FAST`: exactly 19 testcases from the five approved excluded classes
- warmed no-change `testFast`: approximately 5.75–6.25s, with tasks `UP-TO-DATE`

`testFast` retains `A3EnumerationBenchmarkTest`, representative utility tests, integration/wiring tests, and ownership/characterization tests.

## 3. T0–T4 execution model

### T0 — FOCUSED

The exact test method, class, or small directly related class set for the behavior under development. T0 is developer-selected and is intended for repeated RED/GREEN feedback. T0 is not one permanent global Gradle suite.

### T1 — FAST

Broad, cheap regression confidence. T1 includes ordinary Android JVM tests, pure domain and rule tests, cheap deterministic contracts, ownership and characterization tests, narrow integration/production-wiring tests, and cheap golden tests.

The executable Android JVM entry point is:

```text
:app:testFast
```

Its governance is default inclusion of all Android JVM tests except a small explicit list of measured expensive or broad tests. This is safer than a large whitelist because newly added tests naturally enter the default set.

### T2 — AFFECTED

T2 is dependency-aware escalation, not necessarily a permanent Gradle task. It consists of T1 plus tests required by the changed semantic/dependency area plus any relevant T3 tests triggered by that change.

### T3 — EXPENSIVE

T3 contains high-volume regression corpora, simulations, large or repeated enumeration, benchmarks, heap/GC measurements, broad review corpora, and external solver validation. T3 remains mandatory coverage. T3 does not mean PR-only.

### T4 — FULL

T4 is the complete applicable repository regression gate. For each validation component selected by the S3 change classifier, T4 preserves full-strength coverage: all Android JVM tests plus debug assemble/compile checks when Android is selected, ASP validation and ASP Python tests when the ASP contract surface is selected, and real Clingo cross-validation when exact/oracle semantics are selected.

S3 may skip an entire validation component only when the changed-path classification explicitly establishes that the component is irrelevant. A selected component must not be downgraded from FULL merely to save CI time.

The executable Android JVM full-suite entry point is:

```text
:app:testFull
```

`testFull` delegates to the existing AGP `:app:testDebugUnitTest` task so Android JVM full coverage remains anchored to the current source of truth.

## 4. Tier is not trigger

Tier answers: “What kind or cost of test is this?”
Trigger answers: “When must this test run?”

For example, `SetupMigrationTest` is a T3 expensive test because its high-volume recommendation loops consume most of Android testcase time. A change to `SetupRecommendationService`, history/cooldown, setup scoring, or family selection triggers it at the T2 checkpoint. An unrelated UI-only change does not need to run it during every local edit loop.

Similarly, `ZddPlayerWorldSetTest` has T3-like execution characteristics but is an affected T2 requirement for ZDD and epistemic representation changes. Real Clingo is T3 external validation, but it is mandatory whenever the S3 classifier selects exact/oracle semantics and remains part of applicable T4 validation.

Expensive does not mean PR-only, and FAST exclusion does not mean coverage exclusion.

## 5. Default FAST policy

All Android JVM tests default to FAST unless explicit measured evidence identifies them as expensive, broad, simulation-based, benchmark-based, or otherwise unsuitable for every edit loop.

The implemented `:app:testFast` task uses default inclusion plus a small explicit exclusion set. It does not use a large manual whitelist, so newly added ordinary tests naturally enter FAST unless explicitly excluded later with measured justification.

The current FAST exclusions are exactly:

- `com.codex.campboardgamehost.clocktower.recommendation.setup.SetupMigrationTest`
- `com.codex.campboardgamehost.clocktower.epistemic.ZddPlayerWorldSetTest`
- `com.codex.campboardgamehost.clocktower.review.ExpertRecommendationReviewTest`
- `com.codex.campboardgamehost.clocktower.simulation.StorytellerV4BaselineSimulationTest`
- `com.codex.campboardgamehost.clocktower.epistemic.A4ZddBenchmarkTest`

`A3EnumerationBenchmarkTest` remains in FAST because its measured cost is low.

## 6. Known non-FAST classes and specialized triggers

These classifications are not permanent measurements. Re-measure them when the test, algorithm, Gradle behavior, or surrounding suite materially changes.

| Class | Approximate measured cost | Reason | Role | Trigger family |
|---|---:|---|---|---|
| `SetupMigrationTest` | 31–34s testcase | 1000-sample and 200+200 recommendation/history loops | T3 | setup generation, scoring, family selection, history, determinism |
| `ZddPlayerWorldSetTest` | 10–11s | repeated enumeration, ZDD conversion, filtering and representation parity | affected T2 / T3 execution | ZDD, epistemic worlds, filtering, registration, checkpoint/restore |
| `ExpertRecommendationReviewTest` | 5–6s | 24-scenario recommendation quality corpus | T3 | recommendation scoring, legality, setup plans, quality/diversity |
| `StorytellerV4BaselineSimulationTest` | 3–4s | 1000 setup samples plus 1000 dynamic selections | T3 | selection distribution and simulation semantics |
| `A4ZddBenchmarkTest` | 2–3s | repeated benchmark, heap and GC measurements | T3 | ZDD construction/filter performance |
| `A3EnumerationBenchmarkTest` | approximately 1s | 20 exact enumerations and performance guards | T1/T2 specialized | world enumeration and scalability |

T3 tests are invoked through the existing full test machinery with exact `--tests` filters when triggered; S2 intentionally does not create a static `testAffected` or `testExpensive` suite.

## 7. Dependency-aware escalation matrix

| Change family | T0 | T1 | T2 affected validation | T3 trigger | T4 |
|---|---|---|---|---|---|
| Documentation-only | none | none | none | none | CI classifier/gate only unless another executable surface changes |
| Utility/helper or hashing | exact utility test | cheap domain tests | direct consumers if shared | normally none | applicable full |
| Rule semantics | exact rule test | rule/domain tests | related flow and recommendation tests | simulation if distribution changes | Android full + exact/oracle gates when selected |
| Night/day flow | exact flow test | flow/session tests | transaction, integration, and wiring tests | simulation if behavior changes | applicable full |
| Setup recommendation | exact setup test | recommendation tests | `SetupMigrationTest`, expert review | simulation if selection distribution changes | applicable full |
| Scoring/selection | exact scoring test | recommendation/selection tests | review, distribution, and setup tests | simulation/benchmark when relevant | applicable full |
| Epistemic/enumeration | exact epistemic test | cheap epistemic tests | ZDD, A3 golden, and end-to-end golden tests | A3/A4 benchmarks | Android full + Real Clingo |
| ZDD | exact ZDD method | cheap epistemic tests | `ZddPlayerWorldSetTest` and golden tests | `A4ZddBenchmarkTest`, A3 benchmark | applicable full |
| Persistence/schema | exact persistence test | persistence tests | restore, migration, and production-wiring tests | broad migration if applicable | applicable full |
| Historical timeline/identity | exact history test | history/session tests | historical action/observation wiring | migration corpus if applicable | Android full + Real Clingo |
| Production wiring/orchestration | exact ownership/wiring test | ownership and wiring tests | relevant integration/session tests | simulation if central behavior changes | applicable full |
| ASP/oracle/scenarios | exact harness command | ASP validation and Python tests | affected scenario tests | real Clingo | ASP contracts + Real Clingo |
| Gradle/build/dependency | exact build check | affected JVM tests | dependent Android tests | all external validation when contract changes | Android full and any additionally selected gates |
| Shared interfaces/game-state authority | exact contract test | all cheap consumers | broad dependent integration tests | all affected expensive tests | applicable full |

The mapping is semantic rather than a giant fragile file-path list.

## 8. Developer workflow

For a behavior change:

```text
RED                    -> T0 confirms the failure
GREEN                  -> T0 focused tests pass
local iteration        -> owning/local focused tests
logical checkpoint     -> T1 + T2 affected validation
larger slice/pre-commit-> T1 + triggered T3 tests
PR                     -> T4 applicable gates selected by S3 routing
```

When the S3 classifier selects Android validation for a PR, the Android gate runs `:app:testFull` plus debug assemble; `testFast` is not a substitute for that selected PR gate.

For a structural-only refactor:

```text
focused ownership/characterization tests
-> T1
-> affected integration when a boundary changes
-> applicable T4 at PR
```

Documentation-only changes do not require local Android regression unless executable configuration or contracts change. In CI, explicitly classified documentation-only changes retain the lightweight change-classification and aggregate gate while unrelated heavy validation jobs are skipped.

## 9. Conservative escalation rules

Require wider-than-focused validation when touching:

- serialization format or persistence schema;
- timeline identity or game-state authority;
- central orchestration;
- shared/common interfaces;
- shared recommendation semantics;
- algorithm representation;
- build or dependency configuration.

If impact is uncertain, escalate upward. Do not optimize for minimal test execution at the expense of ambiguous coverage.

## 10. ASP and Clingo

ASP corpus validation and ASP Python tests are cheap and should be T1 for ASP-related work and remain CI checkpoint validation.

Real Clingo is external and expensive. It is T3 validation triggered by changes to the ASP oracle, scenario corpus, exact epistemic semantics, solver integration, or the external solver contract. S3 routes it for the corresponding exact semantic surfaces and for any workflow change or unknown repository surface that requires fail-safe validation.

## 11. Engineering time targets

These are goals, not hard correctness guarantees:

- T0: near the Gradle floor; prefer below 10s warmed where practical
- T1: approximately 15–30s warmed for actual execution; no-change cached runs may approach the Gradle floor
- T2: normally below 60s where practical
- T3: no strict edit-loop budget
- T4: several minutes is acceptable

Coverage must not be removed merely to satisfy a time target.

## 12. Gradle and cache semantics

`UP-TO-DATE` and `FROM-CACHE` do not mean that tests executed in that invocation. Validation reports must distinguish executed test tasks from skipped or cached tasks.

Use `--rerun-tasks` only when intentionally measuring forced execution. It is not the default normal-development invocation.

S2.1 verified that `testFast` and `testFull` have separate execution identities, that `testFast` does not invoke `testDebugUnitTest`, and that `testFull` does.

## 13. Executable suite contract

Current Android JVM commands:

```text
./gradlew :app:testFast
./gradlew :app:testFull
```

`testFast` is an independent `Test` task that reuses the AGP debug-unit-test classes/runtime classpath and excludes only the five approved classes above.

`testFull` is a verification/lifecycle task that depends on `:app:testDebugUnitTest` without filtering.

The validated coverage invariant is:

```text
testFull Android JVM coverage = :app:testDebugUnitTest coverage = 770 tests
FULL - FAST = exactly 19 testcases from the five approved excluded classes
```

No test may disappear from full validation.

## 14. Maintenance

Re-audit when:

- T1 becomes materially slower;
- a new simulation or benchmark is introduced;
- new external validation is added;
- a large integration suite appears;
- Gradle or build behavior changes;
- a new top-level repository surface or new semantic owner is added;
- CI routing or branch-protection expectations change.

Review value, duplication, flakiness, semantic layer, cost, and routing coverage. Do not delete tests merely because they have aged or become expensive.

## 15. CI routing contract

S3 establishes path-aware GitHub CI routing without weakening selected PR validation. The classifier is implemented directly in `.github/workflows/ci.yml` using repository-native `git diff`; it does not depend on a third-party path-filter action.

Routing principles:

- `docs/**`, `AGENTS.md`, `README.md`, and `player/**` are explicitly classified as non-executable surfaces and skip Android, ASP, and Clingo heavy jobs.
- Android application/test/assets and Gradle/build-system changes select Android FULL validation plus debug assemble.
- `tools/asp_oracle/**` selects ASP contract tests and Real Clingo.
- Clocktower `domain/**`, `epistemic/**`, `history/**`, `rules/**`, matching test packages, core `Clocktower*Semantics.kt`, and built-in rules/script assets additionally select Real Clingo because they can affect exact semantic contracts.
- `.github/workflows/**` changes select every gate so routing changes validate themselves.
- `workflow_dispatch` selects every gate.
- Any repository path that is not deliberately classified fails safe to every gate rather than silently skipping validation.
- Changed-path discovery uses `git diff --name-only --no-renames`, so a rename/move cannot hide the old executable path behind a newly safe destination.

A stable `CI gate` job always runs after classification. Selected validation jobs must succeed; deliberately irrelevant jobs may be `skipped`. This gives one aggregate check whose meaning remains stable even when the heavy job set varies by change.

S3 was validated on draft PR #50 at head `2135ea19d09ee19a3cb5e9357414efb6acf33f72`:

```text
CI #684
Classify changes             SUCCESS
Android tests (FULL+assemble) SUCCESS
ASP contract tests           SUCCESS
Real Clingo cross-validation SUCCESS
CI gate                      SUCCESS

R2 #615                      SUCCESS
```

The PR deliberately includes `.github/workflows/ci.yml`, so the classifier correctly selected all gates during this validation. PR #50 remains draft and is not merge-authorized by this document.
