# ASP Oracle cross-validation harness

This development-only harness translates CampBoardGameHost A1 semantic fixtures into constraints for the frozen `pnkfelix/botc-asp` model. It compares SAT/UNSAT, selected legal outputs, and local registration behavior, then writes a machine-readable JSON report.

It is not part of the Android source set, is not packaged into the app, and is not a rules authority.

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

The fetch command checks out exactly `616e61b720cc853af031f2623fd6bde33b869865` in detached-HEAD mode and verifies `HEAD`. Output assertions are checked by exact atom-existence SAT probes rather than enumerating every complete world. The report records the revision, timeout, each effective command, fixture hashes, observed atoms, and comparison classification.

`NOT_RUN` is never treated as `UNSAT`. Any `UNEXPLAINED_MISMATCH` or `NOT_RUN` makes the command fail. Expected coverage gaps and frozen-model semantic variances remain visible in the report but do not masquerade as independent agreement.
