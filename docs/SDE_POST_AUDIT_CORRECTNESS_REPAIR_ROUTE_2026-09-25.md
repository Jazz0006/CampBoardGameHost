# SDE post-audit correctness repair route — 2026-09-25

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/sde-history-prefix-route-closure`  
> Audit evidence: `artifacts/sde-audit-2026-09-25/AUDIT.md`  
> Audited production baseline: `15fd898faa88795b73ea769c41dc042ec5a91680`  
> Current authority: this document defines the bounded repair checkpoint that must close before SDE-3D2 feature production work.

## 1. Decision

The 2026-09-25 audit does **not** invalidate the accepted SDE-3A/3B/3C architecture, canonical owners, frozen `BEGINNER_CONSERVATIVE_V1`, or the historical C0–C3 checkpoint evidence.

It does show that three correctness contracts are not yet strong enough to use the replay/feature pipeline as a calibration-ready foundation. Therefore:

```text
C0–C3 historical integration checkpoint
        ↓
CR-A impaired-narrative semantic correctness
        ↓
CR-B typed game/request identity binding
        ↓
CR-C strict nested replay decoding
        ↓
correctness repair checkpoint accepted
        ↓
C4 / SDE-3D2 architecture + feature work
```

CR-A/CR-B/CR-C are **hard prerequisites for SDE-3D2 production feature edits**.

Two additional findings remain required follow-up work but do not block the SDE-3D2 architecture/evidence/fanout audit:

- IF-D durable App replay capture/rebuild across cold start;
- RH-E serialized background trace persistence plus complete timing/accounting.

No V2, policy preference, cutover, rules rewrite, second mutable truth store, or role-specific fixture patch is authorized by this repair route.

## 2. CR-A — impaired-narrative semantic correctness

### Defect

The current impaired-narrative projection can treat a candidate as compatible when it is explainable only because the information source is impaired. Mechanical credibility and player-believable continuity are therefore conflated.

The exact evaluator may correctly allow arbitrary output for a Drunk/poisoned source under `MECHANICALLY_CREDIBLE`; that does not prove that a player who believes their ability is functioning can reconcile the new output with the previously established narrative.

### Required invariant

Keep two questions separate:

1. **mechanical legality / credibility** — may use the existing mechanically credible world set;
2. **perceived narrative continuity** — must derive evidence for whether the prior player-believable functioning story can continue.

Do not globally change the exact evaluator default to `FUNCTIONING_ONLY`. Do not add Empath-, Chef-, Fortune-Teller-, or fixture-specific policy branches. Canonical history remains the only durable history owner; perceived narrative remains a derived projection, not a second mutable world state.

### Acceptance

Typed regression must cover the shared owner and representative fanout across:

- numeric, boolean, and categorical/role-location information shapes;
- persistent setup-bound impairment and temporary action-bound impairment;
- accidental truth;
- repeated history;
- genuinely unavoidable state transitions;
- already-infeasible prior narrative.

V1 ordering remains unchanged.

### 2026-09-26 implementation checkpoint

CR-A shared-owner implementation is **accepted at the combined correctness-repair checkpoint**.

Implemented direction:

- `HistoricalImpairedNarrativeFeatureProjector` no longer reuses the ordinary mechanically-credible confirmation projection as narrative-continuity evidence;
- the projector derives a **source-scoped perceived-functioning replay** only for the current impaired source / active impairment episode;
- canonical `ActionFactTimeline` and `EpistemicObservationLog` remain the only durable history owners;
- other players' impairment semantics remain mechanically credible and are not globally normalized;
- `ImpairedNarrativeCandidateEvidence` now names this input explicitly as `perceivedConfirmation`;
- first-output impaired information with no prior same-episode narrative remains `NO_PRIOR_IMPAIRED_NARRATIVE` without unnecessary narrative replay.

Representative regressions now cover:

- categorical / role-location conflict;
- persistent setup-bound Drunk history;
- temporary action-bound Poison history;
- numeric accidental truth;
- boolean perceived-functioning conflict;
- protection against accidentally normalizing a different impaired source;
- existing pure-projector coverage for avoidable/forced break and already-infeasible prior narrative remains in place.

Validation status:

- Oracle `test:fast` still cannot reach Kotlin compilation because Android SDK is not installed/configured on that host; this remains a local-host capability limitation, not acceptance evidence;
- validation-only Draft PR #156 exposed one bad Fortune Teller regression fixture: the candidate queried a seat already fixed as the Demon, so the current NO was impossible even after removing the prior YES; the production projector was not changed for that failure;
- the regression was corrected to a Red-Herring-specific perceived-functioning conflict where the prior YES is explained only by the fixed Red Herring and the later NO remains legal when that prior is removed;
- exact validation head `4245ddb8c12782775a6b4c237f5dd7f0f1ce92c0` passed full CI #3459 and R2 #3212;
- Android FULL `:app:testFull :app:assembleDebug`, ASP contracts, Real Clingo cross-validation, final CI gate and R2 boundary all passed;
- CR-A is therefore **COMPLETE / ACCEPTED**.

## 3. CR-B — typed game/request identity binding

### Defect

Offline replay can currently accept an `InformationDecisionContext` from another game when ruleset and revisions happen to match. `semanticIdentity` is a free string and is not a sufficient typed game/request boundary.

### Required invariant

The information-decision owner must expose an explicit typed identity sufficient to bind the decision request to its game/session identity. Before exact evaluation, replay input, decision context, and current runtime/session identity must agree.

Do not parse display/semantic identity strings to recover game identity.

### Acceptance

Regression must reject:

- different game + same revisions;
- stale/mismatched request identity;
- mismatched runtime identity before feature evaluation or trace persistence.

Valid same-game replay remains deterministic and unchanged.

### 2026-09-26 implementation / acceptance checkpoint

CR-B is **COMPLETE / ACCEPTED**.

- `InformationDecisionRequestIdentity(gameId, requestId)` is the shared owner contract at `InformationDecisionContext` / `InformationDecisionSnapshot`; compatibility getters remain for existing semantic/request-ID consumers;
- production numeric/boolean adapters construct the typed identity directly from their existing `gameId`; no semantic-string parsing exists in production;
- offline replay rejects a different game even when revisions match, before exact evaluation;
- runtime shadow rejects mismatched request identity as stale before evaluation or trace persistence;
- production historical shadow requires the decision-context game identity to match the current snapshot;
- `SdeRuntimeShadowIdentity` now binds the typed request identity together with revisions/timeline cursor;
- same-game deterministic behavior remains unchanged;
- exact validation head `4245ddb8c12782775a6b4c237f5dd7f0f1ce92c0` passed full CI #3459 and R2 #3212.

Preferred minimal contract identified by audit:

```text
InformationDecisionRequestIdentity(
    gameId,
    requestId
)
```

where `requestId` preserves the existing stable semantic request identity while `gameId` becomes an explicit typed boundary. Existing UI/trace consumers may keep a compatibility getter for semantic/request ID; they must not parse it to recover game identity.

## 4. CR-C — strict nested replay decoding

### Defect

`SdeHistoricalReplayInputJsonCodec.decodeStrict` validates top-level shape but delegates action/observation/proposition decoding to compatibility decoders that can ignore unknown nested fields or coerce invalid numeric types.

This violates the C1 fail-closed replay contract.

### Required invariant

SDE replay input must reject unknown or malformed nested semantics for the complete replay schema, including action points/facts, observations, propositions, required keys, enum values, primitive types, and numeric types.

Do not globally tighten legacy game-save compatibility behavior without a separate migration decision. Prefer a strict replay-specific validation/decoding mode or a complete pre-validation layer that reuses canonical semantic owners.

### Acceptance

At minimum prove:

- unknown nested keys fail closed;
- fractional values cannot silently become integers;
- required nested fields/types fail closed;
- known canonical material round-trips deterministically;
- legacy save compatibility behavior is unchanged unless separately authorized.

### 2026-09-26 implementation / acceptance checkpoint

CR-C is **COMPLETE / ACCEPTED**.

- `SdeHistoricalReplayInputJsonCodec` performs replay-specific strict nested pre-validation before delegating canonical semantic decoding;
- action facts / timeline points, recorded observations, timeline bindings, grimoire material and every current `InformationProposition` subtype are shape- and primitive-type-checked;
- unknown nested fields fail closed;
- fractional numeric material cannot silently coerce into integer fields;
- required nested fields and wrong primitive types fail closed;
- recursive `any-of` / `all-of` / `not` proposition material is validated recursively;
- the legacy save decoder was not globally tightened;
- deterministic canonical round-trip coverage remains in place;
- exact validation head `4245ddb8c12782775a6b4c237f5dd7f0f1ce92c0` passed full CI #3459 and R2 #3212.

## 5. IF-D — durable App replay capture/rebuild follow-up

C1 remains a valid immutable transport contract and C2 remains a valid offline vertical slice, but the App does not yet persist enough material to rebuild a complete decision request after cold start without retained process objects.

Required future closure:

```text
canonical setup/history/request identity
    -> versioned durable replay export
    -> process/object destruction
    -> restore bytes
    -> canonical legal-candidate owner rebuilds decision context
    -> features / trace / actual-choice / multi-policy replay
```

Do not serialize a parallel mutable game truth.

This must be complete before runtime traces are treated as a durable real-game calibration corpus, but it need not block the SDE-3D2 architecture/evidence/fanout audit or policy-neutral projector work once CR-A/B/C are green.

## 6. RH-E — runtime persistence/timing hardening follow-up

Current runtime timing measures evaluation before synchronous archive persistence. Archive load/decode/encode/write and post-commit lookup can run on the caller thread, and archive growth is unbounded.

Future hardening must:

- move diagnostic I/O to a serialized background persistence lane;
- preserve atomic append/correlation and idempotency;
- avoid naive parallel read-modify-write races;
- measure evaluation, persistence, and total latency separately;
- test cancellation/backlog/slow storage/archive growth;
- define retention or storage-growth behavior before broad runtime collection.

Canonical session commit remains first and must never depend on diagnostic persistence success.

## 7. Historical completion semantics

Historical completion documents retain their exact-head evidence value:

- SDE-3B3 established the intended shared impaired-narrative owner and policy-neutral feature seam; CR-A repairs a semantic defect inside that accepted architecture.
- C1 established the replay transport/reconstruction seam; CR-C repairs strict nested validation.
- C2/C3 established the bounded offline/runtime composition; CR-B repairs typed identity binding and RH-E documents runtime hardening still required.
- C1/C2 did not, by themselves, prove App cold-start decision-request reconstruction; IF-D remains open.

Do not rewrite history by marking the old checkpoints as never completed. Current roadmap/handoff must instead show the discovered defects and the repair gate explicitly.

## 8. Validation and exit gate

For each CR item use the smallest durable typed RED/GREEN at the true owner, then run the affected SDE/information-decision regression set. At the combined repair checkpoint:

- focused CR-A/B/C regressions GREEN;
- existing C0–C3 and frozen-V1 regressions GREEN;
- FAST + debug assembly according to `TESTING_STRATEGY.md`;
- `git diff --check` GREEN;
- full producer/consumer fanout re-audit for any shared typed contract changed;
- exact-head remote CI/R2 according to the active PR acceptance state.

### Combined repair acceptance — COMPLETE 2026-09-26

The combined CR-A / CR-B / CR-C repair checkpoint is accepted on validation-only Draft PR #156 at exact head `4245ddb8c12782775a6b4c237f5dd7f0f1ce92c0`:

- Android FULL `:app:testFull :app:assembleDebug` — SUCCESS;
- ASP contract tests — SUCCESS;
- Real Clingo cross-validation — SUCCESS;
- final CI gate — SUCCESS;
- R2 main-thread boundary #3212 — SUCCESS;
- PR #156 remained Draft and is not a merge target.

The first full-ci attempt intentionally exposed a malformed Fortune Teller regression fixture; fixing that fixture without changing the production projector produced the accepted green checkpoint. This is the expected value of the combined gate, not evidence to weaken it.

CR-A/B/C no longer block C4/SDE-3D2. This checkpoint is being persisted on the formal development branch through a GitHub tree/commit/ref fast-forward fallback because Mini MCP `git_state` cannot hash the current large dirty worktree within its 131072-byte Git-output bound. The local Oracle working tree is intentionally left untouched; once the remote formal branch contains this accepted material, C4 production feature implementation may begin while the Mini MCP state-hash defect is repaired separately.

## 9. Parallel C4 architecture/evidence/fanout audit — COMPLETE 2026-09-26

The non-production C4 audit is complete and recorded in:

- `SDE_3D2_TRUTH_CREDIBILITY_RED_HERRING_ARCHITECTURE_AUDIT_2026-09-26.md`

Key conclusion:

```text
existing legal Red Herring candidate
+ existing setup commitment reference
+ rule-determined / committed healthy information source
+ exact descriptive consequence
-> typed truth-danger / credibility-disruption feature
-> trace/replay
-> V1 remains policy-neutral
```

The audit confirms that Red Herring legality remains setup-owned, `SdeCommittedDecisionInputKind.RED_HERRING` already exists as the correct binding vocabulary, the Night-1 bundle already excludes player-controlled Fortune Teller future targets, and `DecisionFeatures.truthCredibility` already exists as an unimplemented slot. The durable C4 contract should be typed, score-free, role-agnostic and no-hindsight. Evin GOLD supports the existence of truth-danger / credibility-disruption semantics, but does not authorize a Chef bonus, neighbour bonus, fixed seat ordering, threshold or scalar weight.

C4 production feature edits are unblocked by correctness-repair acceptance. The formal development branch persistence performed by this checkpoint is sufficient to begin C4; the local Oracle HEAD/worktree alignment remains a tooling follow-up because Mini MCP `git_state` currently cannot emit a stale-state token for this large diff.

## 9. Documentation cleanup rule

Current status belongs to:

1. `CURRENT_DEVELOPMENT_ROADMAP.md`;
2. `NEXT_DEVELOPMENT_HANDOFF.md`;
3. this repair route;
4. `SDE_INTEGRATION_CLOSURE_ROUTE_2026-09-25.md` for the historical C0–C3 contract;
5. the SDE-3 / 3D route documents for later feature/policy gates.

Older completion audits remain historical evidence and should not be deleted merely because a later audit found a defect. Any stale “3D2 NEXT” or “complete durable replay loop” wording in active authorities must defer to this repair gate.