# Official golden corpus and ASP Oracle cross-validation harness

This development-only harness validates the CampBoardGameHost official golden contract and translates the subset faithfully supported by the frozen `pnkfelix/botc-asp` model. It compares SAT/UNSAT, selected legal outputs, and local registration behavior, then writes a machine-readable JSON report.

It is not part of the Android source set and is not packaged into the app. Authority is fixed as `OFFICIAL > PROJECT_GOLDEN > EXTERNAL_ORACLE`; Oracle disagreement never rewrites the official expectation.

## Reproducible use

```bash
python3 tools/asp_oracle/oracle_harness.py validate \
  --fixtures tools/asp_oracle/scenarios/trouble_brewing_a2.json

python3 tools/asp_oracle/oracle_harness.py fetch \
  --checkout-dir build/oracles/botc-asp

python3 tools/asp_oracle/oracle_harness.py run \
  --fixtures tools/asp_oracle/scenarios/trouble_brewing_a2.json \
  --botc-asp-dir build/oracles/botc-asp \
  --report build/reports/asp-oracle-a2.json \
  --timeout 30
```

Requirements for a real oracle run are Git and Clingo 5.x. Fixture validation and unit tests use only Python 3:

```bash
python3 -m unittest discover -s tools/asp_oracle -p 'test_*.py'
```

The schema-v2 catalog contains 48 coverage-driven Trouble Brewing scenarios. Every scenario has an official basis, explicit assertions, a perspective/hypothesis boundary, and stable canonical hash. Twenty scenarios run against Clingo; 28 transition or knowledge-projection contracts are explicitly `ORACLE_NOT_APPLICABLE` until a faithful adapter exists.

The fetch command checks out exactly `616e61b720cc853af031f2623fd6bde33b869865` in detached-HEAD mode and verifies `HEAD`. Output assertions are checked by exact atom-existence SAT probes rather than enumerating every complete world. The report records the authority order, official validation, revision, timeout, effective commands, fixture hashes, observed atoms, and comparison classification.

`ORACLE_NOT_APPLICABLE` means the external model/adapter cannot faithfully express that official contract; it is not an execution failure. `NOT_RUN` is reserved for tool, timeout, or environment failure and is never treated as `UNSAT`. Any `UNEXPLAINED_MISMATCH` or `NOT_RUN` makes the command fail. Expected coverage gaps and known Oracle variances remain visible and do not masquerade as agreement.

Frozen A2.1 baseline (Clingo 5.8.0):

```text
official PASS               48
AGREE                       18
EXPECTED_COVERAGE_GAP        1
KNOWN_ORACLE_VARIANCE        1
ORACLE_NOT_APPLICABLE       28
UNEXPLAINED_MISMATCH         0
NOT_RUN                      0
```
