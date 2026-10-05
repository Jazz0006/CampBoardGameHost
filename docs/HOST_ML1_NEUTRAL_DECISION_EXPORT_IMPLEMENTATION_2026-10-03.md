# HOST-ML1 Neutral Recommendation Decision Export — 2026-10-03

> Status: **HOST-ML1 COMPLETE / ACCEPTED**
>
> Repository: `Jazz0006/CampBoardGameHost`
>
> Implementation PRs: `#208` (`host-ml1-neutral-decision-export`) + `#209` (`host-ml1-pair-domain-payload`)
>
> Final accepted executable checkpoint: `b91fcd298621e9064f11c26c1a0398c5c14fc13c`
>
> Final acceptance: CI #3710 GREEN / R2 #3416 GREEN; merged via PR #209 squash `2d4681115b9c373bfbfa85a7b385aba4e59e1062`
>
> Parent architecture: `docs/HOST_ML_READINESS_MODELLAB_BOUNDARY_AUDIT_2026-10-03.md`
>
> Scope: typed, policy-neutral, read-only export contract for mature Trouble Brewing Drunk-assignment and first-night pair-information decision surfaces. No training-example schema, dataset recipe, model dependency, inference adapter, legality change, canonical-state mutation, or production recommendation-policy change.
>
> 2026-10-05 GSP amendment: HOST-ML1 remains accepted infrastructure. GSP may reuse/extend this neutral seam for frozen manual benchmark materialization and later provider automation, but no API/model dependency is implied and the export must remain non-authoritative.

## 1. Decision

HOST-ML1 begins with a typed neutral export seam rather than a training format.

The accepted first slice is:

`RecommendationDecisionExportV1`

It is a derived immutable projection over already-authoritative Host state and replay structures. It does not own game state, evidence, legality, policy authority, or training semantics.

The export is physically partitioned into the four HOST-ML0 leakage classes:

```text
INPUT_ELIGIBLE
TARGET_OR_LABEL
EVALUATION_METADATA
PROVENANCE_ONLY
```

This boundary is deliberate. A future corpus/dataset builder must not flatten target/evaluation fields back into historical model input.

## 2. Bounded decision surfaces

HOST-ML1 supports only the two surfaces whose canonical context/replay boundaries are already mature:

1. Trouble Brewing Drunk assignment;
2. Trouble Brewing first-night functioning pair information.

No generic every-decision export registry is introduced.

### 2.1 Drunk assignment

The export consumes:

- `DrunkAssignmentDecisionContext`;
- the complete rules-owned legal Drunk domain;
- the canonical setup-precommit snapshot;
- canonical source revision / history-prefix identity;
- the dedicated `DrunkAssignmentFeatureEvaluation`;
- one or more `DrunkAssignmentShadowReplayRecord` policy-evaluation records.

It does not regenerate Drunk legality or select/commit a Drunk seat.

### 2.2 First-night pair information

The export consumes:

- `PairInformationShadowReplayRequest`;
- the canonical runtime Trouble Brewing snapshot;
- the complete ordered `PairInformationLegalCandidate` domain;
- per-candidate semantic payload: shown role, candidate seats, semantic truth and registration witnesses;
- canonical source revision / global history prefix;
- deterministic `DecisionFeatureEvaluation`;
- one or more canonical `DecisionTrace` replay traces.

The candidate semantic payload is copied into the export rather than reconstructed by parsing opaque candidate IDs. The export fails closed if semantic-payload order/IDs diverge from replay legal-domain order, if candidate/source/registration seats fall outside the canonical snapshot, or if a functioning reliable pair export contains a false semantic candidate.

It does not regenerate pair legality, publish information, or alter production recommendation authority.

## 3. Export structure

`RecommendationDecisionExportV1` contains:

```text
schema identity
decision type

inputEligible
  canonical TroubleBrewingGameSnapshotV1
  decision identity / lifecycle
  source revision
  canonical committed history-prefix reference
  complete ordered legal candidate IDs
  typed decision context
  deterministic feature projection

targetOrLabel
  actual authoritative historical choice state
  Host-derivable historical-domain relation

evaluationMetadata
  policy/replay traces
  policy selections
  evidence checkpoint identity

provenanceOnly
  game grouping key
  script grouping key
  ordered policy evidence checkpoints
```

Cross-invariants fail closed if feature or replay candidate order diverges from the complete rules-owned legal domain, if snapshot/prefix/revision identity diverges, or if replay records do not represent one common canonical decision input.

## 4. Historical-choice semantics

HOST-ML1 deliberately exposes only relations the Host itself can prove from a committed historical choice plus the complete legal domain:

```text
OBSERVED_CHOICE
LEGAL_UNCHOSEN
```

`LEGAL_UNCHOSEN` still means only:

> legal at the decision boundary and not the historical selected choice.

It is not rejection, inferiority, negative preference, or a DPO loser.

HOST-ML1 intentionally does **not** define `EXPLICIT_REJECTED` or `EXPLICIT_COMPARISON_LOSER` fields. Those relations require source-backed EvidenceLab evidence and must not be represented by a Host-only placeholder that could later be mistaken for evidence.

This means EL-ML1 is **not triggered by the current typed Host export alone**. It becomes relevant when the next downstream consumer actually needs machine-readable source-backed preference/rejection seeds.

## 5. Anti-leakage boundary

For historical decisions:

- canonical pre-decision state, legal domain, typed context and deterministic feature projection live in `inputEligible`;
- historical actual choice lives in `targetOrLabel`;
- policy/replay outputs live in `evaluationMetadata`;
- grouping/checkpoint identity lives in `provenanceOnly`;
- later commitment state, later history and final outcome are not added to historical recommendation input.

The pair export also verifies that the replay source revision matches the canonical snapshot revision. Drunk export verifies the setup-precommit context, legal domain, source revision and canonical game-prefix identity.

## 6. Test evidence

Focused tests cover:

1. Drunk observed choice vs `LEGAL_UNCHOSEN` classification without manufacturing rejection;
2. pair canonical prefix/revision preservation and policy replay separation from input;
3. pending actual choice produces no fabricated historical target relations;
4. pair export fails closed when replay revision does not match the canonical snapshot;
5. pair semantic candidate payload preserves shown role, seats, truth and registration witnesses;
6. pair export fails closed when semantic candidate order diverges from replay legal-domain order.

Initial accepted checkpoint:

`d7880bf6dbf71b86288ca9ce58370cc321f38f84` — CI #3700 / R2 #3407 GREEN.

Final HOST-ML1 acceptance checkpoint:

`b91fcd298621e9064f11c26c1a0398c5c14fc13c`

Acceptance:

- CI #3710 — GREEN;
- Android FAST unit tests — GREEN;
- R2 #3416 — GREEN;
- PR boundary check — GREEN;
- PR #209 squash merge — `2d4681115b9c373bfbfa85a7b385aba4e59e1062`.

## 7. What remains outside HOST-ML1

The current slice does not freeze an external JSON/JSONL or model-training format.

Deferred until a concrete downstream consumer requires them:

- deterministic external interchange / corpus materialization;
- EvidenceLab `RecommendationEvidenceSeedV1`;
- source-backed explicit preference/rejection joins;
- ModelLab dataset recipes and split manifests;
- prompt/tokenizer schemas;
- SFT / preference / ranking examples;
- synthetic-negative generation;
- model selection/training;
- external-model replay adapter;
- production model authority.

This is intentional: the typed Host ownership/leakage boundary is now stable, while the wire/training format remains free to follow real ModelLab and EvidenceLab requirements instead of being guessed inside the Android Host.

## 8. Next ML task

HOST-ML1 is complete at the typed neutral-export boundary. The next ML-readiness step is a separate follow-up only when there is a concrete offline consumer:

```text
RecommendationDecisionExportV1 typed contract
-> deterministic machine-readable interchange / corpus materializer
-> trigger EL-ML1 only if source-backed evidence seeds are required
-> MODELLAB-0 consumes stable Host export + optional EvidenceLab seeds
```

Any future machine-readable materializer must remain read-only/offline and must not become a production recommendation-policy cutover.

C5-E remains an independent product lane and is not blocked by HOST-ML1.
