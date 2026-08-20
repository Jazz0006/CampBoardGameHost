# Official golden corpus and ASP Oracle cross-validation (A2.1)

> Milestone: Phase A / PR A2.1  
> Frozen oracle: `pnkfelix/botc-asp@616e61b720cc853af031f2623fd6bde33b869865`  
> Production rollout: development/test only; Android gameplay dependencies unchanged

## Authority and delivered boundary

A2.1 enforces `OFFICIAL > PROJECT_GOLDEN > EXTERNAL_ORACLE`. The official expectation is retained when the frozen Oracle disagrees.

The harness validates 48 official contracts. For the 20 scenarios the frozen model can faithfully express, it turns A1-shaped `FormalGameState` fixtures plus a typed query into ASP, invokes Clingo, parses JSON output, and compares:

- SAT or UNSAT;
- required or excluded legal-output atoms;
- interaction-local Spy/Recluse registration atoms.

Legal-output and registration assertions use separate exact atom-existence SAT probes. This avoids enumerating every irrelevant full-world variation while retaining exact yes/no answers for each expected or forbidden atom.

The other 28 scenarios cover timeline transitions and recipient projections such as poison duration, protection/death, starpass, Scarlet Woman, Undertaker, Ravenkeeper, Virgin, Saint, Mayor, Spy grimoire, and secret-leak boundaries. They remain mandatory official contracts but are explicitly `ORACLE_NOT_APPLICABLE`; the harness does not invent lossy constraints to inflate agreement.

Fixtures retain the app's `RoleId` spelling (for example `Fortune Teller`). The adapter owns the explicit conversion to the frozen Oracle's lowercase ASP symbol (`fortune_teller`), keeping external predicate spelling out of the Android semantic model.

## Reproducibility and reports

`tools/asp_oracle/oracle_harness.py fetch` obtains only the frozen commit and verifies the checkout's exact `HEAD`. A run report records:

```text
oracle repository and revision
authority order and official validation
timeout
effective Clingo command per scenario
canonical catalog and scenario SHA-256
SAT / UNSAT result
observed output and registration atoms
duration
comparison classification
```

Classifications are `AGREE`, `EXPECTED_COVERAGE_GAP`, `KNOWN_ORACLE_VARIANCE`, `ORACLE_NOT_APPLICABLE`, `UNEXPLAINED_MISMATCH`, and `NOT_RUN`. A missing solver or timeout is `NOT_RUN`; it is never converted to `UNSAT`. `ORACLE_NOT_APPLICABLE` is a declared model/adapter limitation, not an environment failure. The command exits non-zero for `NOT_RUN` or an unexplained mismatch.

## Known frozen-oracle boundary

`TB-LIB-03` intentionally permits `EXPECTED_COVERAGE_GAP`: the A1 state correctly records an actual Drunk with a shown Librarian token, while the frozen oracle's Librarian rules bind the acting player through actual `character_assignment_state_at_time(..., librarian)`. The harness reports disagreement rather than patching the external model or leaking storyteller truth into player knowledge.

`TB-FT-04` is a `KNOWN_ORACLE_VARIANCE`: official Fortune Teller setup requires the red herring to be an actual good player, so Spy is illegal. The frozen model instead checks setup-time `registers_as(..., good, ...)` and accepts Spy. `TB-FT-05` separately verifies that Recluse, an actual good player, is a legal red herring.

## Non-production guarantee

All A2 code is under `tools/asp_oracle`; no Gradle dependency or Android source-set entry was added. Generated ASP, external source, Clingo output, and reports are development artifacts and must not be displayed as official rulings in the player UI.

## Verification

The standard-library unit suite checks authority order, fixture breadth, schema validation, deterministic translation and hashing, red-herring authority, explicit non-applicability, comparison behavior, and `NOT_RUN != UNSAT`. A real Clingo run remains the release gate.

The 2026-08-11 A2.1 baseline was run with Clingo 5.8.0 against the exact frozen commit. Catalog SHA-256 was `e0770f927a52357078be3dd10c765f8a99e40ff5a43dc581bf5932d2847f2d2b` and the result was:

```text
official PASS               48
AGREE                       18
EXPECTED_COVERAGE_GAP        1  (TB-LIB-03)
KNOWN_ORACLE_VARIANCE        1  (TB-FT-04)
ORACLE_NOT_APPLICABLE       28
UNEXPLAINED_MISMATCH         0
NOT_RUN                      0
```

Eleven standard-library unit tests also passed. Coverage gaps, variances, and non-applicable scenarios remain visible and are never counted as independent Oracle agreement.
