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

## 3. T0–T4 execution model

### T0 — FOCUSED

The exact test method, class, or small directly related class set for the behavior under development. T0 is developer-selected and is intended for repeated RED/GREEN feedback. T0 is not one permanent global Gradle suite.

### T1 — FAST

Broad, cheap regression confidence. T1 includes ordinary Android JVM tests, pure domain and rule tests, cheap deterministic contracts, ownership and characterization tests, narrow integration/production-wiring tests, and cheap golden tests.

The planned governance is default inclusion of all Android JVM tests except a small explicit list of measured expensive or broad tests. This is safer than a large whitelist because newly added tests naturally enter the default set.

### T2 — AFFECTED

T2 is dependency-aware escalation, not necessarily a permanent Gradle task. It consists of T1 plus tests required by the changed semantic/dependency area plus any relevant T3 tests triggered by that change.

### T3 — EXPENSIVE

T3 contains high-volume regression corpora, simulations, large or repeated enumeration, benchmarks, heap/GC measurements, broad review corpora, and external solver validation. T3 remains mandatory coverage. T3 does not mean PR-only.

### T4 — FULL

T4 is the complete repository regression gate: all Android JVM tests, debug assemble/compile checks, ASP validation, ASP Python tests, real Clingo cross-validation, and other existing CI-required validation.

## 4. Tier is not trigger

Tier answers: “What kind or cost of test is this?”
Trigger answers: “When must this test run?”

For example, `SetupMigrationTest` is a T3 expensive test because its high-volume recommendation loops consume most of Android testcase time. A change to `SetupRecommendationService`, history/cooldown, setup scoring, or family selection triggers it at the T2 checkpoint. An unrelated UI-only change does not need to run it during every local edit loop.

Similarly, `ZddPlayerWorldSetTest` has T3-like execution characteristics but is an affected T2 requirement for ZDD and epistemic representation changes. Real Clingo is T3 external validation, but it is mandatory in T4 and is triggered by ASP, solver, or exact epistemic contract changes.

Expensive does not mean PR-only, and FAST exclusion does not mean coverage exclusion.

## 5. Default FAST policy

All Android JVM tests should default to FAST unless explicit measured evidence identifies them as expensive, broad, simulation-based, benchmark-based, or otherwise unsuitable for every edit loop.

Future S2 implementation should prefer default inclusion plus a small explicit exclusion set. It must not use a large manual whitelist that allows new tests to silently miss FAST.

Executable `testFast` and `testFull` tasks do not exist yet. They are planned future S2 work.

## 6. Known non-FAST candidates

These are candidates, not permanent measurements. Re-measure them when the test, algorithm, Gradle behavior, or surrounding suite materially changes.

| Class | Approximate measured cost | Reason | Candidate role | Trigger family |
|---|---:|---|---|---|
| `SetupMigrationTest` | 31–34s testcase | 1000-sample and 200+200 recommendation/history loops | T3 | setup generation, scoring, family selection, history, determinism |
| `ZddPlayerWorldSetTest` | 10–11s | repeated enumeration, ZDD conversion, filtering and representation parity | affected T2 / T3 execution | ZDD, epistemic worlds, filtering, registration, checkpoint/restore |
| `ExpertRecommendationReviewTest` | 5–6s | 24-scenario recommendation quality corpus | T3 | recommendation scoring, legality, setup plans, quality/diversity |
| `StorytellerV4BaselineSimulationTest` | 3–4s | 1000 setup samples plus 1000 dynamic selections | T3 | selection distribution and simulation semantics |
| `A4ZddBenchmarkTest` | 2–3s | repeated benchmark, heap and GC measurements | T3 | ZDD construction/filter performance |
| `A3EnumerationBenchmarkTest` | approximately 1s | 20 exact enumerations and performance guards | T1/T2 specialized candidate | world enumeration and scalability |

`A3EnumerationBenchmarkTest` is not excluded solely because its name contains “Benchmark”; its current measured cost is low.

## 7. Dependency-aware escalation matrix

| Change family | T0 | T1 | T2 affected validation | T3 trigger | T4 |
|---|---|---|---|---|---|
| Documentation-only | none | none | none | none | normal PR gate when required |
| Utility/helper or hashing | exact utility test | cheap domain tests | direct consumers if shared | normally none | full |
| Rule semantics | exact rule test | rule/domain tests | related flow and recommendation tests | simulation if distribution changes | full |
| Night/day flow | exact flow test | flow/session tests | transaction, integration, and wiring tests | simulation if behavior changes | full |
| Setup recommendation | exact setup test | recommendation tests | `SetupMigrationTest`, expert review | simulation if selection distribution changes | full |
| Scoring/selection | exact scoring test | recommendation/selection tests | review, distribution, and setup tests | simulation/benchmark when relevant | full |
| Epistemic/enumeration | exact epistemic test | cheap epistemic tests | ZDD, A3 golden, and end-to-end golden tests | A3/A4 benchmarks | full |
| ZDD | exact ZDD method | cheap epistemic tests | `ZddPlayerWorldSetTest` and golden tests | `A4ZddBenchmarkTest`, A3 benchmark | full |
| Persistence/schema | exact persistence test | persistence tests | restore, migration, and production-wiring tests | broad migration if applicable | full |
| Historical timeline/identity | exact history test | history/session tests | historical action/observation wiring | migration corpus if applicable | full |
| Production wiring/orchestration | exact ownership/wiring test | ownership and wiring tests | relevant integration/session tests | simulation if central behavior changes | full |
| ASP/oracle/scenarios | exact harness command | ASP validation and Python tests | affected scenario tests | real Clingo | full |
| Gradle/build/dependency | exact build check | affected JVM tests | dependent Android tests | all external validation when contract changes | full |
| Shared interfaces/game-state authority | exact contract test | all cheap consumers | broad dependent integration tests | all affected expensive tests | full |

The mapping is semantic rather than a giant fragile file-path list.

## 8. Developer workflow

For a behavior change:

```text
RED                    -> T0 confirms the failure
GREEN                  -> T0 focused tests pass
local iteration        -> owning/local focused tests
logical checkpoint     -> T1 + T2 affected validation
larger slice/pre-commit-> T1 + triggered T3 tests
PR                     -> T4 FULL
```

For a structural-only refactor:

```text
focused ownership/characterization tests
-> T1
-> affected integration when a boundary changes
-> T4 at PR
```

Documentation-only changes do not require local Android regression unless executable configuration or contracts change.

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

Real Clingo is external and expensive. It is T3 validation triggered by changes to the ASP oracle, scenario corpus, exact epistemic semantics, solver integration, or the external solver contract. It remains mandatory in T4.

## 11. Engineering time targets

These are goals, not hard correctness guarantees:

- T0: near the Gradle floor; prefer below 10s warmed where practical
- T1: approximately 15–30s warmed
- T2: normally below 60s where practical
- T3: no strict edit-loop budget
- T4: several minutes is acceptable

Coverage must not be removed merely to satisfy a time target.

## 12. Gradle and cache semantics

`UP-TO-DATE` and `FROM-CACHE` do not mean that tests executed in that invocation. Validation reports must distinguish executed test tasks from skipped or cached tasks.

Use `--rerun-tasks` only when intentionally measuring forced execution. It is not the default normal-development invocation.

## 13. S2 implementation direction

The planned S2 direction is custom Gradle tasks with default-all behavior and a small explicit expensive exclusion list. S2 must validate actual Gradle semantics before adoption.

Future `testFull` must preserve every test currently covered by `:app:testDebugUnitTest`. No test may disappear from full validation.

## 14. Maintenance

Re-audit when:

- T1 becomes materially slower;
- a new simulation or benchmark is introduced;
- new external validation is added;
- a large integration suite appears;
- Gradle or build behavior changes.

Review value, duplication, flakiness, semantic layer, and cost. Do not delete tests merely because they have aged or become expensive.
