# A3 EnumeratedWorldSet exit review

> Review date: 2026-08-12  
> Decision: PASS  
> Production selector impact: none  
> Next milestone: A4 ZddPlayerWorldSet prototype

## Exit-gate evidence

| v2.2 A3 exit condition | Result | Evidence |
|---|---|---|
| All official executable golden scenarios pass | PASS | All 20 A3-applicable setup and first-night contracts execute through `EnumeratedWorldSet`; the 28 timeline/projection contracts remain explicitly assigned to B4. |
| No unexplained ASP difference | PASS | 18 `AGREE`, one documented `EXPECTED_COVERAGE_GAP` (`TB-LIB-03`), one documented `KNOWN_ORACLE_VARIANCE` (`TB-FT-04`), zero `UNEXPLAINED_MISMATCH`, zero `NOT_RUN`. |
| SAT/UNSAT is independent of an enumeration cap | PASS | The baseline has no enumeration cap or sampling path. Exact setup and role constraints are pushed into enumeration without changing semantics. |
| Registration choices remain candidate-bound | PASS | Spy/Recluse `RegistrationFact` records carry interaction ID, subject, registered values, question and reason; golden CHOICE assertions require bound facts. |

## Verification record

- Complete Android JVM unit suite: 195 tests, zero failures, zero errors, zero skips.
- Focused epistemic suite: 24 tests, all passing.
- Python Oracle harness: 11 tests, all passing.
- Official fixture validation: 48 contracts; canonical SHA-256
  `e0770f927a52357078be3dd10c765f8a99e40ff5a43dc581bf5932d2847f2d2b`.
- Constrained exact benchmarks cover 8, 10, 12 and 15 players with stable cardinality over five
  runs and no correctness cap.
- User-reported physical Android phone smoke test: OK. This supports integration confidence but is
  not substituted for A4 POCO X5/X8 performance acceptance.

## Findings resolved during A3

The golden runner exposed one semantic defect: Fortune Teller evaluation treated a possible Recluse
Demon registration as mandatory. The evaluator now preserves both legal branches when neither an
actual Demon nor red herring is selected: YES with bound Recluse registration and NO without it.

## Non-blocking follow-up

Gradle 9.5 / AGP 9.3 emits legacy Android DSL and restricted-native-access warnings. They do not
affect A3 correctness or the passing test suite, but should be handled as a separate build-toolchain
cleanup before AGP 10.

The materialized enumerator remains a correctness and debugging baseline. Its desktop JVM memory
observations are not runtime acceptance targets; A4 must evaluate compact representation correctness,
P50/P95 latency, peak memory and explicit degradation behavior on the target POCO devices.
