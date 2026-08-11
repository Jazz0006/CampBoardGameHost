# ASP Oracle cross-validation harness (A2)

> Milestone: Phase A / PR A2  
> Frozen oracle: `pnkfelix/botc-asp@616e61b720cc853af031f2623fd6bde33b869865`  
> Production rollout: development/test only; Android gameplay dependencies unchanged

## Delivered boundary

The A2 harness turns A1-shaped `FormalGameState` fixtures plus a typed query into a generated ASP program. It invokes Clingo with the frozen `botc.lp` and `tb.lp`, parses Clingo's JSON output, and compares:

- SAT or UNSAT;
- required or excluded legal-output atoms;
- interaction-local Spy/Recluse registration atoms.

Legal-output and registration assertions use separate exact atom-existence SAT probes. This avoids enumerating every irrelevant full-world variation while retaining exact yes/no answers for each expected or forbidden atom.

The fixture catalog contains 18 executable priority scenarios covering setup and Baron, Washerwoman, Librarian, Investigator, Chef, Empath, Fortune Teller, Drunk, Poisoning, Spy, and Recluse. Scenario IDs remain aligned with `epistemic_reference_matrix.md`.

Fixtures retain the app's `RoleId` spelling (for example `Fortune Teller`). The adapter owns the explicit conversion to the frozen Oracle's lowercase ASP symbol (`fortune_teller`), keeping external predicate spelling out of the Android semantic model.

## Reproducibility and reports

`tools/asp_oracle/oracle_harness.py fetch` obtains only the frozen commit and verifies the checkout's exact `HEAD`. A run report records:

```text
oracle repository and revision
timeout
effective Clingo command per scenario
canonical catalog and scenario SHA-256
SAT / UNSAT result
observed output and registration atoms
duration
comparison classification
```

The classifications are the A0 protocol values: `AGREE`, `EXPECTED_COVERAGE_GAP`, `KNOWN_SEMANTIC_VARIANCE`, `UNEXPLAINED_MISMATCH`, and `NOT_RUN`. A missing solver, timeout, or enumeration failure is `NOT_RUN`; it is never converted to `UNSAT`. The command exits non-zero for `NOT_RUN` or an unexplained mismatch.

## Known frozen-oracle boundary

`TB-LIB-03` intentionally permits `EXPECTED_COVERAGE_GAP`: the A1 state correctly records an actual Drunk with a shown Librarian token, while the frozen oracle's Librarian rules bind the acting player through actual `character_assignment_state_at_time(..., librarian)`. The harness reports disagreement rather than patching the external model or leaking storyteller truth into player knowledge.

## Non-production guarantee

All A2 code is under `tools/asp_oracle`; no Gradle dependency or Android source-set entry was added. Generated ASP, external source, Clingo output, and reports are development artifacts and must not be displayed as official rulings in the player UI.

## Verification

The standard-library unit suite checks fixture breadth, schema validation, deterministic translation and hashing, comparison behavior, and the invariant that `NOT_RUN != UNSAT`. A real Clingo run remains the required environment-level gate whenever Git and Clingo 5.x are available.

The 2026-08-11 A2 baseline was run with Clingo 5.8.0 against the exact frozen commit. Catalog SHA-256 was `5b424f718446c3a6cc30aed66d65a0a39441525f103b2d98c863b73b4fe8da9e` and the result was:

```text
AGREE                     17
EXPECTED_COVERAGE_GAP      1  (TB-LIB-03)
KNOWN_SEMANTIC_VARIANCE    0
UNEXPLAINED_MISMATCH       0
NOT_RUN                    0
```

Seven standard-library unit tests also passed. The single coverage gap is documented above and is not counted as independent Oracle agreement.
