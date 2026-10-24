# SDE-3C4 — Authoritative Choice Correlation Architecture Audit

> Date: 2026-09-24 Australia/Sydney  
> Branch: `sde-3c-decision-trace-shadow-replay`  
> Entry HEAD: `de707e689539db759567da2ec1e8c81a5cdc6724`  
> Scope: ownership/fanout audit before executable authoritative-choice correlation.

## 1. Existing authoritative boundary

The structured information path has three distinct lifecycle states:

1. `InformationDecisionContext.confirm` validates a candidate against the exact immutable
   `InformationDecisionSnapshot` and returns `ConfirmedInformationDecision`.
2. Player-reveal authorization rechecks that confirmation against the expected snapshot and current
   revision before publication effects begin.
3. The actual private information fact becomes canonical only when
   `ClocktowerGameSession.commitGlobalEpistemicObservation` successfully returns the bound
   `RecordedEpistemicObservation`.

Therefore confirmation alone is not commit evidence. A globally bound record produced by
`preflightGlobalEpistemicObservation` is also insufficient because preflight is deliberately
non-mutating. SDE-3C4 must require both the returned committed observation and a post-commit
`ClocktowerSessionView` that contains that exact observation with advanced canonical revision/cursor state.

## 2. Stable correlation identity

For a structured-information DecisionTrace, the SDE decision identity is already copied from
`InformationDecisionSnapshot.semanticIdentity`. Correlation must require all of:

- identical semantic decision identity;
- identical source `InformationDecisionRevision`;
- identical complete ordered legal-candidate domain;
- identical interaction lifecycle (phase / round / sequence);
- selected candidate contained in the traced domain;
- the confirmed draft exactly matching the committed `RecordedEpistemicObservation`;
- a Global timeline binding on the committed observation;
- matching canonical game identity in the post-commit session view;
- unchanged game-state revision and an advanced player-input revision;
- exact presence of the committed record in the canonical post-commit observation log;
- a semantic timeline cursor advanced beyond the committed record.

A candidate ID alone is insufficient.

## 3. Actual-choice semantics

The trace records the authoritative selected candidate and `InformationDecisionSource`.

For this shadow phase:

- `RECOMMENDATION_ACCEPTED` means the existing production-visible recommendation path was accepted;
- `MANUAL` means the Storyteller used the existing structured manual path;
- `manualOverride` is true exactly for `MANUAL`;
- an optional structured/textual override reason is permitted only for a manual override.

The hidden SDE shadow recommendation is not treated as a recommendation the human could have
explicitly overridden. A human-vs-shadow mismatch remains observable by comparing
`policySelection` with `actualChoice.candidateId`, but it is not itself the manual-override flag.

## 4. Archive transition

SDE-3C3A correctly rejects arbitrary same-key different-content traces. SDE-3C4 introduces one
narrow immutable value transition:

```text
same DecisionTraceKey
same trace content except actualChoice
Pending -> Committed
```

Rules:

- missing key fails closed;
- changing any non-`actualChoice` field fails closed;
- `Committed -> different Committed` fails closed;
- repeating the exact same committed finalization is idempotent;
- the archive remains immutable: finalization returns a replacement archive value.

## 5. Durable store integration

`DecisionTraceArchiveStore` may expose correlation/finalization only when the caller supplies:

- the exact archive key of the pending trace;
- the `ConfirmedInformationDecision`;
- the returned committed `RecordedEpistemicObservation`;
- the post-commit canonical `ClocktowerSessionView`.

The store must not call `confirm`, must not commit the observation, and must not turn DecisionTrace
into canonical game state. A preflight-only record cannot satisfy the post-commit log/revision checks.

## 6. Tests-first acceptance

Before production implementation add durable tests for:

1. exact successful correlation after a matching Global committed observation;
2. manual source + optional reason producing a manual override;
3. identity/revision/domain/lifecycle mismatch rejection;
4. mismatched/non-Global observation rejection and preflight-only evidence rejection;
5. archive Pending -> Committed replacement, exact retry idempotence, and conflicting retry rejection;
6. store durable correlation plus no second write on identical retry.

Oracle Android execution remains subject to the known SDK limitation; GitHub CI/R2 is the independent
Android acceptance surface.

## 7. Explicit non-goals

SDE-3C4 does not:

- change visible recommendation;
- authorize or perform information confirmation;
- commit canonical observations;
- interpret a human override as a quality label;
- add UI collection for override rationale;
- replay multiple policy versions (SDE-3C5);
- introduce a second canonical decision/history ledger.
