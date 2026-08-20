# Phase A Exit Review — 2026-08-20

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/storyteller-algorithm-v4`  
> Review scope: R1–R5 / A0–A4.5 correctness re-exit  
> Decision: **PASS FOR PHASE A CORRECTNESS EXIT**  
> ZDD runtime promotion: **NOT APPROVED; remains shadow/prototype**

## 1. Decision

Phase A correctness remediation is complete enough to exit into R5.5 Script & Dynamic Flow Foundation.

This review does **not** authorize `ZDD_DEVICE_VALIDATED`, does not move shadow cache into production recommendation, and does not unlock the revision-driven dynamic decision engine directly. R5.5 remains the next required stage before R6.

## 2. A3 correctness and validation

### Registration correctness

R1 fixed the poisoned Spy/Recluse numeric-registration defect in the official semantics path. Direct Chef/Empath regressions and machine-readable golden contracts cover poisoned Spy/Recluse combinations. The frozen external ASP Oracle still differs for these interactions; those cases remain explicitly classified as `KNOWN_ORACLE_VARIANCE` rather than overriding official behavior.

### Schema and typed execution

R3 migrated nested `FormalGameState` fixtures to schema-v2, kept schema-v1 fail-closed, and routed the Android golden catalog through the typed semantic decoder.

The real-enumerator golden path executes `TroubleBrewingWorldEnumerator.enumerate()` over representative hidden-Baron/Drunk, Poisoner, red-herring, Spy and Recluse-sensitive scenarios.

## 3. A3 ↔ A4 exact differential

`ZddPlayerWorldSetTest` is the current differential contract.

It compares Enumerated and ZDD representations for:

- world-set identity;
- exact cardinality;
- empty/non-empty state;
- possible Demon and Minion seats;
- possible roles per seat;
- per-role world counts;
- per-seat Demon world counts;
- explanation clusters;
- `require` / `exclude` filtering;
- checkpoint / restore behavior;
- finite possible-value queries;
- native and decode/rebuild filtering paths.

It also executes the entire current A3 executable golden corpus through both Enumerated and ZDD representations and requires all results to agree.

The latest clean-head `testDebugUnitTest` therefore re-ran the differential after the R1–R4 remediation work.

**Result: PASS — no new A3/A4 exact differential mismatch is known.**

## 4. A4.5 durability, cancellation and cache identity

R4 completed P0.4–P0.8:

- observation rebuild release only after successful durable persistence;
- coroutine cancellation is mapped into executor cancellation;
- cancellation is rechecked after exact build and before cache publication;
- stale generation commits fail closed;
- cache key/generation scope validates game ID, game-state revision, formal snapshot ID and rollout;
- revision supersession invalidates current game shadow generation immediately;
- reset/archive/restore boundaries invalidate cache, cancel rebuild and clear pending durability state;
- same-session revision bumps do not incorrectly clear a pending observation waiting for durable persistence;
- production recommendation remains unchanged whether shadow cache is absent or ready.

Failure states remain separate from logical emptiness. Ordinary failures, cancellation, stale work and OOM/resource exhaustion do not produce a zero-cardinality/UNSAT logical result.

**Result: PASS.**

## 5. Multi-night / unsupported transition boundary

B4 remains isolated shadow work. Unsupported dynamic transitions are explicitly represented as `DEFERRED_B4`; tests verify an unmodelled role-change transition returns `DEFERRED_B4` with no world queries rather than false UNSAT.

Hidden poison-target replacement remains private and does not become pseudo-UNSAT.

**Result: PASS for current Phase A boundary.**

## 6. Oracle and full regression evidence

Current golden/Oracle baseline remains:

- catalog total: 52;
- Clingo-executable: 24;
- `AGREE`: 18;
- `EXPECTED_COVERAGE_GAP`: 1;
- `KNOWN_ORACLE_VARIANCE`: 5;
- `ORACLE_NOT_APPLICABLE`: 28;
- `UNEXPLAINED_MISMATCH`: 0;
- `NOT_RUN`: 0.

Latest clean-head evidence on commit `ec93dfed61ef71af1869387b24907a85069f51c6`:

- CI #117 — **success**;
  - Android unit tests + debug APK — success;
  - ASP contract tests — success;
  - real Clingo cross-validation — success;
- R2 main-thread boundary #109 — **success**.

The immediately preceding R4 final-acceptance head also passed CI #116 and R2 boundary #108 after adding the direct shadow-production-isolation regression.

## 7. Device-performance gate

A4 target-device performance is **not** promoted by this exit review.

Existing phone diagnostics already show that native restrictions can be fast while ZDD construction and numeric decode/rebuild fallback can exceed the provisional 50 ms ceiling. The A4 design therefore correctly keeps runtime policy on `ENUMERATED_ONLY` / shadow measurement unless a future target-device gate explicitly authorizes `ZDD_DEVICE_VALIDATED`.

Because the Phase A exit condition only requires target-device acceptance **if ZDD is to be promoted**, device validation is not a blocker for correctness exit while ZDD remains shadow-only.

Status after this review:

```text
Production recommendation engine: existing production path
A3 EnumeratedWorldSet: validated exact correctness baseline
A4 ZDD: exact shadow/prototype only
A4.5 cache: debug/shadow only
B4 dynamic world-set work: isolated shadow only
ZDD_DEVICE_VALIDATED: NOT AUTHORIZED
```

## 8. Exit checklist

- [x] Known A3 official-rules correctness defect fixed and regression-covered.
- [x] MainActivity mechanical decomposition completed without intentional behavior changes.
- [x] A2 nested formal fixtures use schema-v2; schema-v1 fails closed.
- [x] Representative official golden path executes the real A3 enumerator.
- [x] Oracle differences are classified; no unexplained mismatch or not-run executable case remains.
- [x] A3/A4 exact differential remains green after remediation.
- [x] A4.5 durability/cancellation/cache-identity contracts pass.
- [x] Shadow cache cannot influence production recommendation.
- [x] Failure/OOM/cancel/stale states are not translated to UNSAT.
- [x] Unsupported multi-night transition explicitly defers instead of false UNSAT.
- [x] Full Android + ASP + real Clingo regression passes on the reviewed clean head.
- [x] ZDD runtime promotion remains separately gated by target-device evidence.

## 9. Handoff

Phase A correctness exit is signed off.

Next stage:

**R5.5 — Script & Dynamic Flow Foundation**

Required order remains:

```text
S0 Schema / Catalog / official-custom JSON normalization / validation
→ S1 Trouble Brewing FlowPlanner golden-equivalent migration
→ S2 No Greater Joy second-script structural proof
→ S3 Werewolf BoardRegistry + RoleRegistry + FlowPlanner migration
→ S4 persistence/ruleset identity migration
→ S5 full regression + legacy flow removal + R6 handoff
```

R6 remains blocked until R5.5 passes.
