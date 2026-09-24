# SDE-3C3B — Strict Persistence Codec / Storage Completion Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> Oracle base HEAD before this checkpoint: `a75f482dd5a9aa5a128525c2cfb47be127ded72b`  
> Status: **CODE COMPLETE / REMOTE ACCEPTANCE PENDING**

## 1. Scope completed

SDE-3C3B now provides a durable persistence boundary for the immutable SDE replay archive without
changing production recommendation, confirmation, or canonical commit authority.

Implemented:

- `DecisionTraceArchiveJsonCodec`
  - archive format version `1`;
  - requires the current nested `DecisionTrace.CURRENT_SCHEMA_VERSION`;
  - fully typed encode/decode of Ready and Deferred traces;
  - typed encode/decode of current feature projections, policy snapshots, selections, and actual-choice shape;
  - requires persisted traces to carry `SdeHistoricalPrefixRef.Global`;
  - rejects malformed fields, unexpected fields, unknown discriminators/enums, duplicate set members,
    duplicate archive keys, unsupported archive versions, and unsupported trace schema versions;
  - canonicalizes every set-valued field into an explicit sorted JSON order while preserving
    list-valued contract order.
- `DecisionTraceArchiveStore`
  - reconstructs the complete immutable archive on every load;
  - treats absent/blank storage as an empty archive only;
  - propagates malformed non-empty storage failures;
  - delegates duplicate/conflict semantics to `DecisionTraceArchive.append`;
  - identical append is idempotent and does not perform another durable write;
  - same-key different-content append fails before write;
  - failed physical writes return `false` and do not claim success.
- `DecisionTraceArchivePreferencesStorage`
  - binds the raw store to the existing application SharedPreferences transport;
  - uses a dedicated `decision_trace_archive_v1` key;
  - does not place DecisionTrace inside RecoverySnapshot or canonical semantic history.

## 2. Ownership audit result

No second mutable game/history owner was introduced.

Still authoritative:

- `ActionFactTimeline + EpistemicObservationLog`: canonical committed semantic history;
- `InformationDecisionContext` / existing session path: legality, visible recommendation,
  confirmation, and commit;
- `RecoverySnapshot`: emergency recovery transport for canonical application state.

New persistence is diagnostic/replay storage only.

The SharedPreferences transport is intentionally **not auto-invoked from
`StructuredInformationProductionShadow`**. That object remains a read-only evaluator. The exact
lifecycle point at which a pending trace becomes associated with an authoritative committed choice
belongs to SDE-3C4. 3C3B therefore supplies real durable storage capability without prematurely
creating an append side effect at recommendation-evaluation time.

## 3. Tests-first contract

Added `DecisionTraceArchivePersistenceTest` covering:

1. deterministic Ready + Deferred round-trip;
2. repeat encoding byte equality after round-trip;
3. missing required field rejection;
4. wrong JSON type rejection;
5. unknown discriminator rejection;
6. incompatible archive format rejection;
7. incompatible nested DecisionTrace schema rejection;
8. non-canonical history-prefix rejection;
9. duplicate identical archive-key rejection during decode;
10. duplicate conflicting archive-key rejection during decode;
11. empty durable store load;
12. successful durable append/load;
13. identical append idempotence without a second write;
14. same-key different-content conflict before write;
15. malformed persisted data fail-closed;
16. failed physical write reports failure.

The Ready fixture exercises projected strategic, confirmation-chain, healthy-information,
truth/credibility, role-function exposure, semantic-truth, impaired-narrative, bluff-narrative,
relationship, and future-flexibility shapes plus policy evaluation/selection/actual-choice
serialization. A second candidate also exercises an explicit unavailable feature projection.
The Deferred fixture exercises exact capability deferral and policy deferral.

## 4. Local validation status

Mini MCP `test:fast` was invoked both after the tests-first contract was added and after the
production codec/store implementation.

Both invocations stopped during Gradle configuration before Kotlin compilation:

```text
Could not determine the dependencies of task ':app:testFast'.
SDK location not found.
Define ANDROID_HOME or sdk.dir in /home/opc/repos/CampBoardGameHost/local.properties.
```

Therefore:

- there is **no local RED/GREEN execution evidence** for the new Kotlin tests;
- there is also no observed Kotlin compiler failure from this checkpoint, because compilation was
  never reached;
- the checkpoint must remain **REMOTE ACCEPTANCE PENDING** until the independent Android CI/R2
  surface validates it.

This is an infrastructure limitation of the Oracle host, not a passed test claim.

## 5. Boundary checks

Confirmed by production-source search:

- `DecisionTraceArchiveStore` is referenced only by its SharedPreferences transport;
- no existing recommendation/confirmation/commit owner imports or calls the new store;
- no DecisionTrace persistence field was added to RecoverySnapshot;
- no ActionFactTimeline/EpistemicObservationLog mutation was added;
- no role-specific persistence branch or fixture-specific production branch was introduced.

## 6. Next gate

After commit/push and remote CI/R2 acceptance, SDE-3C3B can be marked fully COMPLETE.

Then proceed to **SDE-3C4 authoritative-choice correlation**:

- correlate only after successful authoritative confirmation/commit;
- require matching decision/lifecycle/revision/domain identity;
- record actual committed candidate and optional structured/textual override rationale;
- never reinterpret a human override as an automatic quality label;
- keep persisted trace/archive diagnostic rather than canonical game-state authority.
