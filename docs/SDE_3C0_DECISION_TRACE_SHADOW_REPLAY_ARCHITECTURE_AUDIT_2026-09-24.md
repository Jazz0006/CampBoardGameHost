# SDE-3C0 — DecisionTrace / Shadow Replay Architecture Audit

> Date: 2026-09-24 Australia/Sydney  
> Phase: SDE-3C0  
> Branch: `sde-3c-decision-trace-shadow-replay`

## 1. Entry state

SDE-3B `BEGINNER_CONSERVATIVE_V1` is merged through PR #153. The merged `main` baseline is
`7045ae746fd11a26371127c584c91e2a183c4c75`.

SDE-3C owns durable diagnostic DecisionTrace persistence and systematic multi-policy replay. It must
not move visible recommendation or canonical game-commit authority into SDE.

## 2. Existing authoritative owners

The current production seams already separate the required responsibilities:

- `InformationDecisionContext` owns the validated legal candidate set, legacy visible recommendation
  membership, stale-revision validation, and confirmation by stable candidate ID.
- `StructuredInformationShadowAdapter` is the common shadow aggregation boundary where legal
  candidates, exact consequences, typed `DecisionFeatures`, and
  `BeginnerConservativePolicyEvaluation` already meet.
- `BeginnerConservativeV1Selector` owns deterministic, order-independent, weight-free selection
  among V1 survivors, but currently has no production caller.
- `StructuredInformationProductionShadow` binds the shadow evaluation to committed setup plus the
  canonical `GameSnapshot`; it is read-only.
- `ClocktowerGameSession`, `ActionFactTimeline`, and `EpistemicObservationLog` remain canonical
  game/history authorities.
- `InformationDecisionContext.confirm` remains the only current structured-information confirmation
  path for recommendation acceptance and manual choice.

## 3. Fanout result

`StructuredInformationShadowEvaluation` has one production constructor:
`StructuredInformationShadowAdapter.evaluate`.

Therefore the first shadow-recommendation slice can be implemented once at the common adapter
boundary. No UI caller, role adapter, rules owner, or confirmation path needs a parallel selector
call.

The selector must use stable decision identity plus the canonical game seed. A deferred V1 policy
must produce no shadow selection.

## 4. Persistence ownership

### 4.1 Do not use canonical semantic history

A DecisionTrace is an evaluation record, not a game fact.

It must not be appended to:

- `ActionFactTimeline`;
- `EpistemicObservationLog`;
- a second perceived-world or narrative ledger.

Historical replay must reconstruct feature/policy evaluation from the canonical committed prefix,
not treat previously persisted derived features as historical truth.

### 4.2 Do not overload legacy DecisionHistoryArchive

`DecisionHistoryArchive` and `DecisionHistoryRepository` currently carry older
`StorytellerDecisionEvent` / pressure / misinformation / registration projection semantics.
Making that archive the SDE-3 trace owner would couple the new typed policy record to legacy
heuristic authority that SDE-3 is explicitly replacing.

SDE-3C should introduce a separate versioned diagnostic trace/archive contract.

### 4.3 RecoverySnapshot is transport, not semantic owner

`RecoverySnapshot` is explicitly a short-horizon process-death recovery contract for game facts and
mandatory continuation. SDE-3C must not make recovery state the semantic owner of DecisionTrace.

If trace durability later shares the existing persistence transport, the dependency direction must
remain:

```text
DecisionTraceArchive
    -> persistence codec / export transport
    -> restore diagnostic archive

canonical game/session history remains independently authoritative
```

Whether the archive is embedded in the recovery payload or stored beside it is a 3C3 persistence
decision after the typed trace contract exists.

## 5. Actual choice / override correlation

The trace must distinguish the shadow recommendation from the authoritative confirmation:

```text
shadow policy selection A
    !=
InformationDecisionContext.confirm(candidate B)
```

A later trace finalization step may record:

- actual committed candidate ID;
- recommendation accepted versus manual source;
- manual-override flag;
- optional structured/textual reason.

That correlation must require the same decision identity and source revision/candidate domain. It
must never authorize or perform the confirmation itself.

A human override is calibration evidence only; it is not automatically a quality label.

## 6. Replay contract

Multi-policy replay must use the same canonical committed game prefix as input:

```text
canonical committed prefix
    -> feature projection
    -> Policy V1
    -> Policy V2
    -> Policy V3
```

Persisted old feature snapshots are useful for audit reproducibility ("what V1 saw then"), but a new
policy replay must recompute through current/versioned projectors against the historical truth
contract rather than mutating or replacing that truth.

## 7. Implementation staging

### SDE-3C1 — production shadow recommendation

Add `PolicySelection?` to the common structured shadow result.

Acceptance:

- Ready V1 evaluation yields exactly the deterministic selector result;
- selection uses stable decision identity + canonical game seed;
- Deferred V1 evaluation yields null;
- visible legacy recommendations remain unchanged;
- confirmation remains owned by `InformationDecisionContext`;
- shadow evaluation remains side-effect free.

### SDE-3C2 — typed DecisionTrace contract

Introduce a versioned, score-free diagnostic record containing at least:

- trace/schema version;
- policy version;
- evidence/corpus checkpoint;
- decision identity + lifecycle/state revision;
- complete legal candidate IDs;
- typed projected features;
- policy rejection/survival/limitation data;
- shadow recommendation;
- actual committed candidate when known;
- override metadata when known.

Do not persist canonical game facts by value merely to make the trace self-contained.

### SDE-3C3 — durable archive / codec

Create one diagnostic archive owner and persistence codec with strict round-trip/version tests.
Keep persistence ownership separate from canonical history ownership.

### SDE-3C4 — confirmation correlation

Finalize the matching trace only after an authoritative confirmation/commit succeeds, with stale
decision/revision protection.

### SDE-3C5 — multi-policy replay

Replay one canonical historical prefix through multiple explicit policy versions without mutating
the historical source or production recommendation authority.

## 8. Validation strategy

SDE-3C changes stable diagnostic/persistence contracts, so use risk-based tests at their true owners:

- 3C1: structured-shadow integration RED/GREEN;
- 3C2: typed contract invariants;
- 3C3: persistence round-trip / strict decoder / migration compatibility;
- 3C4: identity/revision correlation integration;
- 3C5: deterministic same-history multi-policy replay.

No numeric policy calibration, new soft preference, visible recommendation cutover, or legacy
authority deletion belongs to SDE-3C.
