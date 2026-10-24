# SDE-3B2 — Confirmation-Chain Feature Projector Completion Audit

> Date: 2026-09-23 Australia/Sydney  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: **#153 — draft**  
> Status: **COMPLETE**  
> Code checkpoint: `3cf767606354a08d5516e8c945fc600e6ffa544c`

## 1. Scope completed

SDE-3B2 adds a production-owned, score-free confirmation-chain feature projection beside the existing exact strategic diagnostics.

It does **not**:

- change visible recommendation or canonical commit authority;
- add a V1 confirmation-based preference/rejection reason;
- introduce a numeric score, weight, band, player-count threshold, or role-pair table;
- persist a second history/dependency graph;
- promote review/calibration types into production authority.

## 2. Production semantic contract

`ConfirmationChainFeatures` now describes, per current candidate:

- exact ambiguity removed by the candidate itself;
- relation of each eligible committed historical observation to the candidate:
  - `SUPPORTS_EXISTING_OBSERVATION`;
  - `CONTRADICTS_EXISTING_OBSERVATION`;
  - `INDEPENDENT_CONTRIBUTION`;
  - `NO_CONTRIBUTION`;
  - `BASELINE_ALREADY_COLLAPSED`;
- exact ambiguity restored when one historical observation is omitted:
  - exact world count;
  - strategic-world keys;
  - possible Demon seats;
- source/channel provenance and whether a contributing historical observation authenticates a distinct source;
- whether contributing evidence spans multiple channels.

The projector has no total score and imposes no policy ordering.

## 3. Historical orchestration and ownership

`HistoricalConfirmationChainFeatureProjector` reuses the existing exact historical evaluator instead of owning another world model.

For each committed historical observation visible to at least one candidate recipient:

1. evaluate the full historical prefix + current candidate;
2. omit exactly that historical observation from a copied evaluation context;
3. batch all candidate queries for which that observation is recipient-visible;
4. evaluate the leave-one-out context through `ExactHistoricalHypotheticalObservationBundleEvaluator`;
5. feed exact diagnostics into the pure `ConfirmationChainFeaturesProjector`.

Durable ownership remains unchanged:

- mechanical history: `ActionFactTimeline`;
- player-visible semantic history: `EpistemicObservationLog`;
- setup identity: `CommittedClocktowerSetup`;
- current candidate legality/proposition: existing information-decision authority.

No confirmation history or dependency graph is persisted by SDE.

## 4. Knowledge and lifecycle boundaries

The completed projector enforces:

- exact SDE/exact-candidate identity and source-revision alignment;
- canonical historical-prefix identity;
- strict history-before-decision chronology;
- recipient visibility before a historical observation may contribute to a candidate chain;
- explicit `HISTORICAL_INPUT_NOT_CAPTURED` when the historical prefix was not captured;
- explicit `MISSING_CAPABILITY` when exact historical evaluation cannot run;
- `NOT_APPLICABLE` outside the interaction lifecycle.

Future confirmation cannot leak backward into an earlier recommendation.

## 5. Structured-shadow integration

`StructuredInformationShadowAdapter` now:

1. evaluates exact consequences;
2. projects existing strategic/semantic-truth features;
3. projects historical confirmation-chain features when exact consequences are ready;
4. attaches them to `DecisionFeatures.confirmationChainImpact`;
5. evaluates `BEGINNER_CONSERVATIVE_V1`.

The policy result is regression-tested against the same feature set with confirmation reset to unavailable. They are equal.

Therefore 3B2 changes the diagnostic feature surface only. V1 candidate rejection/survival/order remains strategic-only.

## 6. Evidence Lab E2 boundary

ClocktowerEvidenceLab evidence is used only as semantic/lifecycle regression, not policy truth.

- **R02:** validates the no-hindsight shape: a later Undertaker confirmation may be studied as a consequence, but cannot become input to the earlier Drunk-information decision.
- **R04:** validates a multi-night earlier-observation -> later-decision confirmation lifecycle shape. The reconstruction remains partial for direct grimoire/setup fields, so the test does not fabricate missing game state or claim exact whole-game replay.
- No R02/R04 fixture ID or named role pair exists in production projector logic.

E3 expert preference and E4 numeric strength remain deferred.

## 7. Test provenance

Tests-first provenance began at:

- `196ed56a1aad8679f6f1deef2439e4f361bc4759` — RED contract for generic confirmation-chain semantics.

Subsequent coverage includes:

- support, contradiction, independent contribution, no contribution and baseline-collapsed semantics;
- exact ambiguity-restoration facts;
- distinct-channel authentication and multi-channel collapse;
- canonical history ownership / no mutation;
- explicit missing-capability behavior;
- strict no-hindsight rejection;
- structured-shadow attachment;
- V1 policy non-consumption;
- Evidence Lab R02/R04-style semantic prefixes;
- private historical observations excluded from another recipient's confirmation chain.

Two test-premise defects discovered at the final FAST gate were corrected without changing production code:

- later-night hypothetical observations must bind the replay baseline formal snapshot;
- a structured Empath support regression must use the real Empath source ability rather than a fabricated ability identity.

Final code checkpoint:

- `3cf767606354a08d5516e8c945fc600e6ffa544c`
- R2 main-thread boundary run `35834078861`: **SUCCESS**
- CI run `35834078859`: **SUCCESS**
- Android FAST unit tests: **SUCCESS**
- ASP / Real Clingo: skipped by the incremental classifier for this app/test-only checkpoint, as expected.

T4 `[full-ci]` was **not** run here. Per `TESTING_STRATEGY.md`, T4 remains the SDE-3B acceptance gate, not the per-feature-slice gate.

## 8. Fanout / ownership audit

Final producer/consumer audit found:

- `StructuredInformationShadowAdapter` is the production structured-history integration point;
- `HistoricalConfirmationChainFeatureProjector` is the historical confirmation orchestration owner;
- `ConfirmationChainFeaturesProjector` is the pure relation/feature owner;
- review-only `Sde2D5BundleConfirmationChainEvidenceProjector` remains non-authoritative;
- no UI, canonical session commit, persistence, rules owner, or visible recommendation authority was moved;
- no second history owner was introduced.

## 9. Next slice

SDE-3B3 is next.

Its architecture must preserve the same ownership discipline:

```text
CommittedClocktowerSetup
+ ActionFactTimeline
+ EpistemicObservationLog
+ current authoritative AbilityState
+ current legal candidate
    ->
derived impaired-narrative features
```

Important pre-audit conclusion:

- Drunk identity/show-role facts already belong to committed setup;
- poison lifetime belongs to rules/session history;
- current `AbilityState` is already projected into the SDE candidate;
- `ObservationReliability` describes the player's received-information semantics and must not be used as hidden impairment truth;
- therefore 3B3 must derive coherence/detectability and must not create a persisted “Drunk world” or second narrative state.

PR #153 remains **draft** until explicit user authorization to merge.
