# CampBoardGameHost Testing Strategy

> Role: **LONG-LIVED TEST EXECUTION / VALIDATION STRATEGY**
> Baseline: S1.1/S1.2 measured at `d52f53b4a1821cc000368c393721d1d5a073aafc`
> Date: 2026-08-28

## 1. Purpose

The purpose of this strategy is not to reduce the number of tests. It is to maximize confidence per unit of feedback time while preserving complete regression coverage.

Frequent developer feedback and complete regression validation are separate concerns. Tests must not be deleted merely because they are expensive, broad, or old. Expensive tests remain mandatory coverage; their execution frequency is governed by the affected semantic area and acceptance checkpoint cadence.

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

T4 is the complete applicable repository regression gate. For each validation component selected at a T4 checkpoint, T4 preserves full-strength coverage: all Android JVM tests plus debug assemble/compile checks when Android is selected, ASP validation and ASP Python tests when the ASP contract surface is selected, and real Clingo cross-validation when exact/oracle semantics are selected.

T4 is an **acceptance tier**, not the default tier for every PR synchronize event. Ordinary PR micro-commits may run T1/T2 feedback. Once a logical checkpoint is explicitly escalated to T4, a selected component must not be downgraded merely to save CI time.

The executable Android JVM full-suite entry point is:

```text
:app:testFull
```

`testFull` delegates to the existing AGP `:app:testDebugUnitTest` task so Android JVM full coverage remains anchored to the current source of truth.

## 4. Tier is not trigger

Tier answers: “What kind or cost of test is this?”
Trigger answers: “When must this test run?”

For example, `SetupMigrationTest` is a T3 expensive test because its high-volume recommendation loops consume most of Android testcase time. A change to `SetupRecommendationService`, history/cooldown, setup scoring, or family selection triggers it at the T2/T4 checkpoint. An unrelated UI-only change does not need to run it during every local edit loop.

Similarly, `ZddPlayerWorldSetTest` has T3-like execution characteristics but is an affected T2 requirement for ZDD and epistemic representation changes. Real Clingo is T3 external validation and remains mandatory whenever the change classifier selects exact/oracle semantics or when a T4 checkpoint selects every gate.

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
| Documentation-only | none | none | none | none | CI classifier/gate only unless explicitly escalated |
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
ordinary PR iteration  -> T1 FAST + any selected semantic/external gate
logical checkpoint     -> T1 + T2 affected validation
T4 acceptance checkpoint -> [full-ci] -> full selected gates
merge/main             -> full gate
```

Ordinary PR synchronization is intentionally optimized for feedback latency. An Android-relevant micro-commit runs `:app:testFast`; it does **not** automatically run `:app:testFull` plus `assembleDebug` merely because earlier commits in the same PR touched production code.

A logical checkpoint that requires full acceptance uses `[full-ci]` in the checkpoint commit message. That escalation runs the complete Android JVM suite plus debug assemble and all other full-checkpoint gates. `testFast` is never accepted as a substitute for that explicit T4 gate.

For a structural-only refactor:

```text
focused ownership/characterization tests
-> T1
-> affected integration when a boundary changes
-> T4 at the logical acceptance checkpoint
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

Real Clingo is external and expensive. It is T3 validation triggered by changes to the ASP oracle, scenario corpus, exact epistemic semantics, solver integration, or the external solver contract. CI routes it for the corresponding exact semantic surfaces and for any workflow change or unknown repository surface that requires fail-safe validation. A `[full-ci]`, `workflow_dispatch`, or main acceptance gate also selects it.

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

Use `--rerun-tasks` only when intentionally measuring forced execution or when the focused RED/GREEN proof explicitly requires actual execution. It is not the default normal-development invocation.

S2.1 verified that `testFast` and `testFull` have separate execution identities, that `testFast` does not invoke `testDebugUnitTest`, and that `testFull` does.

## 13. Executable suite contract

Current Android JVM commands:

```text
./gradlew :app:testFast
./gradlew :app:testFull
```

`testFast` is an independent `Test` task that reuses the AGP debug-unit-test classes/runtime classpath and excludes only the five approved classes above.

`testFull` is a verification/lifecycle task that depends on `:app:testDebugUnitTest` without filtering.

The validated coverage invariant at the S2 baseline is:

```text
testFull Android JVM coverage = :app:testDebugUnitTest coverage = 770 tests
FULL - FAST = exactly 19 testcases from the five approved excluded classes
```

The absolute test count will grow as new tests are added; the invariant is that no test may disappear from full validation.

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

CI path routing and acceptance cadence are implemented directly in `.github/workflows/ci.yml` using repository-native `git diff`; no third-party path-filter action is required.

### 15.1 Incremental PR feedback

For a normal `pull_request` `synchronize` event, changed-path discovery compares:

```text
previous PR head (`github.event.before`)
..
current PR head (`github.event.pull_request.head.sha`)
```

It deliberately does **not** compare the accumulated `main → PR head` range. This prevents an old production change in a long-running PR from forcing every later test-only or documentation commit through the same expensive full gate.

Routing principles for ordinary PR iteration:

- `docs/**`, `AGENTS.md`, `README.md`, and `player/**` are explicitly non-executable surfaces and skip Android, ASP, and Clingo heavy jobs.
- Ordinary `app/**` application/test/assets changes select Android FAST validation (`:app:testFast`).
- `tools/asp_oracle/**` selects ASP contract tests and Real Clingo.
- Clocktower `domain/**`, `epistemic/**`, `history/**`, `rules/**`, matching test packages, core `Clocktower*Semantics.kt`, and built-in rules/script assets additionally select Real Clingo because they can affect exact semantic contracts.
- Any repository path that is not deliberately classified fails safe to every gate at full strength rather than silently skipping validation.
- Changed-path discovery uses `git diff --name-only --no-renames`, so a rename/move cannot hide the old executable path behind a newly safe destination.

For an opened/reopened PR event, there is no prior PR head to compare, so classification may use the PR base.

### 15.2 Full acceptance escalation

The following always select full-strength acceptance validation:

- a PR checkpoint commit whose message contains `[full-ci]`;
- `workflow_dispatch`;
- every push to `main`;
- `.github/workflows/**` changes;
- Gradle/build/dependency configuration changes for Android full validation;
- unknown/unclassified repository surfaces through fail-safe routing.

At a full checkpoint, Android runs:

```text
:app:testFull :app:assembleDebug
```

and the selected ASP/Real-Clingo gates run at full strength. This is the T4 boundary; FAST is not a substitute here.

PR concurrency intentionally cancels an older run when a newer head is pushed. Therefore, once a `[full-ci]` checkpoint is being used as acceptance evidence, do not immediately push another micro-commit until that required run concludes.

### 15.3 Stable aggregate gate

A stable `CI gate` job always runs after classification. Selected validation jobs must succeed; deliberately irrelevant jobs may be `skipped`. This gives one aggregate check whose meaning remains stable even when the heavy job set varies by change.

The CI-routing change itself is fail-safe: editing `.github/workflows/ci.yml` selects every validation component so routing changes exercise the full branch. A later docs-only validation on PR #54 demonstrated incremental classification by completing the classifier and aggregate CI gate while Android, ASP, and Real Clingo were all skipped.
