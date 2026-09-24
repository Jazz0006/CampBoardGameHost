# SDE-3C3B — Strict Persistence Codec / Storage Architecture Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> Scope: architecture/fanout audit before executable persistence work.

## 1. Existing owners

- `DecisionTraceArchive` is the immutable diagnostic/replay owner introduced by SDE-3C3A.
- `ActionFactTimeline + EpistemicObservationLog` remain canonical game/history authority.
- `RecoverySnapshot` is short-horizon process-death recovery for canonical facts and mandatory continuation; it is transport, not DecisionTrace semantic ownership.
- `DecisionHistoryArchive / DecisionHistoryRepository` are legacy StorytellerDecisionEvent/pressure/misinformation/registration history and must not own SDE-3 traces.
- `InformationDecisionContext` remains recommendation/confirmation authority; persistence must never authorize confirmation or commit.

## 2. Existing persistence fanout

The app currently uses SharedPreferences-backed string/JSON persistence in two materially different styles:

1. active-game recovery: `RecoverySnapshotJsonCodec` + strict decoder + committed SharedPreferences write;
2. bounded auxiliary history: `TroubleBrewingSetupRotationHistoryStore`, which wraps raw string read/write transport.

The reusable boundary for 3C3B is the second shape only at the transport level:

```text
DecisionTraceArchive
    -> strict typed/versioned DecisionTraceArchiveJsonCodec
    -> raw string storage adapter
    -> SharedPreferences transport
```

No DecisionTrace field is added to `RecoverySnapshot`, `ClocktowerRecoveryHistory`,
`ActionFactTimeline`, `EpistemicObservationLog`, or legacy `DecisionHistoryArchive`.

## 3. Chosen ownership

Introduce:

- `DecisionTraceArchiveJsonCodec`: current-format deterministic encoder + strict decoder;
- `DecisionTraceArchiveStore`: load/append adapter over injected raw string transport;
- Android `fromContext` construction only as the physical persistence transport.

The store loads a complete immutable archive, applies `DecisionTraceArchive.append`, and persists
the resulting complete archive. It is not a mutable history ledger and exposes no recommendation,
confirmation, policy-selection, or canonical commit operation.

## 4. Strictness contract

Decoder requirements:

- exact archive format version;
- exact in-memory `DecisionTrace.CURRENT_SCHEMA_VERSION`;
- typed discriminators for every sealed variant;
- required field/type checks;
- enum/value-class reconstruction through existing constructors;
- canonical `Global` history-prefix requirement;
- duplicate archive keys rejected during decode;
- malformed, unknown, or incompatible payloads fail closed;
- no silent partial recovery, entry skipping, or fallback-to-empty for non-empty persisted data.

Store requirements:

- absent/blank storage means an empty archive;
- identical same-key append is idempotent and does not require a second write;
- same-key different-content append fails before write;
- malformed stored content propagates failure rather than being erased;
- failed raw write is reported as failure without changing in-memory/canonical game authority.

## 5. Determinism

The encoder preserves archive/candidate list order and emits all set-valued fields in explicit stable
sorted order. Encoding the same archive twice, and encoding a decoded round-trip, must produce the
same bytes.

## 6. Test-first acceptance

Before production implementation, add durable tests for:

1. deterministic Ready + Deferred archive round-trip;
2. malformed required field/type/discriminator rejection;
3. incompatible archive format and nested trace schema rejection;
4. non-canonical history-prefix rejection;
5. duplicate/conflicting archive-key rejection;
6. store empty-load, durable append, identical idempotence, conflict fail-closed, malformed-storage fail-closed.

## 7. Explicit non-goals

SDE-3C3B does not:

- correlate authoritative choice (3C4);
- replay multiple policy versions (3C5);
- change visible recommendation;
- change confirmation/commit authority;
- make persisted feature snapshots canonical historical truth;
- add role-specific or fixture-specific persistence branches.
