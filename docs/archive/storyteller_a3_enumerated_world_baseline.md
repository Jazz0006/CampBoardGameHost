# A3 EnumeratedWorldSet Baseline — implementation status

> Milestone: Phase A / A3  
> Status: COMPLETE  
> Started: 2026-08-11  
> Production selector impact: none

## Delivered first slice

The first A3 slice establishes a CampBoardGameHost-owned, representation-independent
`PlayerWorldSet` contract and a transparent `EnumeratedWorldSet` correctness implementation.
The implementation is isolated under the epistemic package and is not connected to the current
AUTO or ASSISTED recommendation paths.

The baseline currently provides:

- exact materialized world identity with canonical seat order and unique character assignments;
- exact `WorldCardinality` values backed by `BigInteger` at the public boundary;
- `require` and `exclude` observation filtering;
- possible role, Demon-seat, Minion-seat, per-role count, and per-Demon-seat count queries;
- explanation-cluster counts kept separate from candidate-family taxonomy;
- Trouble Brewing standard setup distributions for 5–15 players;
- Baron-shifted setup distributions without leaking Baron presence from public player-count knowledge;
- recipient identity alternatives for a perceived Townsfolk versus the Drunk;
- enforcement that the Drunk's shown Townsfolk character is not also actually in play;
- hidden Poisoner target states as distinct mechanical worlds;
- hidden Fortune Teller red-herring choices as distinct worlds, restricted to actual good players;
- pair, categorical, Chef, Empath, and Fortune Teller observation evaluation;
- interaction-local Spy and Recluse registration explanations;
- explicit functioning, Drunk, and poisoned explanation handling by `EpistemicHypothesis`;
- schema-v2 serialization for player-count and boolean information propositions.

Latest increment (2026-08-12): observation evaluation now preserves the interaction-local
`RegistrationFact` alternatives that make a world match. `EnumeratedWorldSet.boundRegistrationFacts`
exposes these facts for A3 inspection, keyed by the observation ID, subject seat, detected value and
Spy/Recluse reason. This is the prerequisite for the remaining golden adapter to assert that a
CHOICE output has a concrete registration selection, rather than merely a broad explanation label.

The typed catalog adapter now classifies all 48 A2.1 contracts: 20 setup/first-night contracts are
executed through `EnumeratedWorldSet`, and the 28 `official-contract` timeline or private
knowledge-projection contracts are explicitly deferred to B4. All 20 A3-executable contracts pass,
including SAT/UNSAT, multi-output CHOICE, red-herring legality and bound Spy/Recluse registration
assertions. The first corpus run exposed and fixed a Fortune Teller bug: Recluse registering as a
Demon is optional per interaction, so both YES and NO remain legal when no actual Demon or red
herring is selected.

The A3 gate also preserves the frozen A2.1 authority comparison as executable assertions: 18
`AGREE`, `TB-LIB-03` as the single `EXPECTED_COVERAGE_GAP`, and `TB-FT-04` as the single
`KNOWN_ORACLE_VARIANCE`, with zero `UNEXPLAINED_MISMATCH` and zero `NOT_RUN`. The test pins the
official-first authority order and exact frozen Oracle repository revision.

## Constrained development benchmark

Exact `SetupProfile` and `RoleAt` knowledge are now pushed into enumeration rather than applied only
after materializing every assignment. This is an exact constraint optimization: it changes neither
SAT/UNSAT semantics nor cardinality, and it does not introduce a cap.

Five-run desktop JVM samples (first run cold, remaining four warm) produced:

| Players | Exact worlds | Cold | Warm P50 | Warm P95 | Max observed heap delta |
|---:|---:|---:|---:|---:|---:|
| 8 | 13,440 | 129 ms | 36 ms | 42 ms | 32.0 MiB |
| 10 | 6,552 | 21 ms | 22 ms | 24 ms | 15.9 MiB |
| 12 | 14,040 | 25 ms | 22 ms | 23 ms | 35.9 MiB |
| 15 | 16,200 | 26 ms | 26 ms | 28 ms | 46.7 MiB |

These figures are development boundaries for constrained exact snapshots, not Android device
acceptance numbers. JVM heap deltas are coarse process observations, and unconstrained enumeration
is intentionally not presented as a runtime strategy. POCO X5/X8 P50/P95 and peak-memory acceptance
remain A4 work for the compact representation.

## Correctness boundaries

`PlayerCount` is distinct from `SetupProfile`. A normal player's knowledge of the number of players
does not imply knowledge of the actual Townsfolk/Outsider distribution, because that would reveal
whether Baron setup modification occurred. `SetupProfile` remains available for a perspective that
really knows the exact distribution or for an explicit golden constraint.

Poisoner targets and Fortune Teller red herrings are part of mechanical world identity. They are not
treated as cost-free evaluator guesses, so cardinality and explanation queries preserve hidden setup
and first-night choices. Registration remains scoped to each observation interaction and does not
rewrite a character's permanent role or alignment.

The enumerator has no enumeration cap and never converts an exhausted budget into UNSAT. It is a
development correctness baseline, not a mobile runtime implementation. A4 remains responsible for
evaluating a compact runtime representation.

## Verification completed

The complete Android JVM unit suite passes: 195 tests across 46 result files, with zero failures,
errors or skips. The focused epistemic package contributes 24 tests covering semantic JSON round
trips, identity boundaries, setup distributions, hidden Baron worlds, exact cardinality,
red-herring legality, registration, numeric information, malfunction hypotheses, visibility
boundaries, golden execution, Oracle classification and constrained benchmarks.

The Python ASP harness remains unchanged and its 11 standard-library tests pass. A full Android
Gradle run was attempted but the current environment has no Android SDK. A temporary JDK and Gradle
distribution were sufficient for direct Kotlin/JUnit verification; no project dependency or local
SDK configuration was added.

## A3 exit

A3 passed its exit review on 2026-08-12. See `docs/storyteller_a3_exit_review.md`. The next phase is
A4, a compact `ZddPlayerWorldSet` prototype cross-validated against this enumerated baseline.
