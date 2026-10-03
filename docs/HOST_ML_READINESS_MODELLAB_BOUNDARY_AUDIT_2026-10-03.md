# Host ML Readiness / ModelLab Boundary Audit — 2026-10-03

> Status: **HOST-ML0 COMPLETE / ARCHITECTURE ACCEPTED**
>
> Repository: `Jazz0006/CampBoardGameHost`
>
> Host baseline: `main@21d14270415a2ace5746840cf60f477bdd917f1d`
>
> EvidenceLab contract: `ClocktowerEvidenceLab/docs/ML_RECOMMENDATION_EVIDENCE_READINESS_CONTRACT_2026-10-03.md`
>
> EvidenceLab accepted baseline observed for this audit: `main@a575ecc05ccb77cf4aaddcad7f772b0fe920d3d6`
>
> Scope: docs-only architecture audit. No model training, dataset generation, production recommendation change, legality change, persistence migration or C5-B behavior change is authorized by this document.

## 1. Decision

The future ML route should reuse the existing canonical-state / legal-domain / SDE replay architecture rather than create a second recommendation pipeline.

Accepted ownership:

```text
EvidenceLab canonical evidence
        |
        | future RecommendationEvidenceSeedV1
        v
CampBoardGameHost
  canonical pre-decision state
  + rules-owned complete legal domain
  + typed decision context
  + deterministic feature projection
  + replay / DecisionTrace identity
        |
        | future policy-neutral decision export
        v
future ModelLab / dataset builder
  dataset recipe
  + train/eval split
  + SFT / preference / ranking formatting
  + negative sampling
  + model training / evaluation
```

The Host does **not** become a Hugging Face / LoRA / DPO training project. Model training infrastructure should live outside the Android/runtime Host.

The Game Engine remains authoritative for rules and accepted state transitions. A future model remains a replaceable recommendation policy/evaluator over already-legal candidates and must never gain canonical-state mutation authority.

## 2. EvidenceLab contract consumed by Host

HOST-ML0 accepts the EL-ML0 boundary:

- historical observed choice is distinct from explicit preference;
- explicit rejection/comparison loser is distinct from legal unchosen;
- `LEGAL_UNCHOSEN` must not automatically become a DPO rejected sample;
- synthetic negatives are downstream training-recipe artifacts, never canonical evidence;
- historical model input must be restricted to the committed prefix before the decision;
- game/source/Storyteller independence keys must survive into future dataset building;
- EvidenceLab owns external evidence/provenance, not BotC legality or legal candidate enumeration.

Host must not mirror EvidenceLab into a second mutable evidence catalog.

## 3. Current Host readiness

The repository already owns most of the difficult runtime/replay semantics needed by a later dataset builder.

### 3.1 Canonical pre-decision state

`ClocktowerGameSession` / session-owned history remain the single mutable game-truth authority.

`TroubleBrewingGameSnapshotV1` is the current TB-only immutable semantic projection. Decision-specific builders such as `DrunkAssignmentDecisionContext` and `TroubleBrewingFirstNightPairDecisionContext` already demonstrate the intended typed-context boundary.

No ML work may introduce a universal God recommendation context.

### 3.2 Rules-owned candidate legality

Drunk assignment and pair-information surfaces already derive candidate legality from rules/domain owners.

Future ML/export code must consume the complete rules-owned legal domain. It must not independently reconstruct legality, filter the manual legal domain, or invent candidate IDs.

### 3.3 Stable decision/candidate/replay identity

`DecisionTrace` already preserves:

- stable decision identity;
- source revision;
- canonical history-prefix reference;
- complete ordered legal candidate IDs;
- deterministic feature evaluation;
- policy snapshot/selection;
- actual authoritative choice separately from policy choice;
- evidence checkpoint provenance.

Its invariants require feature and policy candidate order to equal the complete legal domain.

This is directly reusable for future evaluation/export.

### 3.4 Multi-policy replay

`MultiPolicyReplayEngine` already enforces same-decision / same-revision / same-prefix / same-legal-domain replay across explicit policy versions.

C5-B/C/D should mature this architecture for pair information before any external-model adapter is designed.

## 4. Future Host-side neutral export

The first future Host ML implementation should be a **policy-neutral decision export**, not a training-example schema.

Provisional conceptual name:

`RecommendationDecisionExportV1`

The implementation name is not frozen by HOST-ML0.

It should carry, where applicable:

```text
schema/version identity
decision identity / decision type
canonical pre-decision state or stable snapshot reference
history-prefix identity
source revision / freshness identity

complete ordered legal candidate domain
stable candidate IDs
typed decision-context identity/payload
deterministic feature projection

EvidenceLab seed/provenance references
historical actual choice
explicit source-backed preference/rejection relations when available

policy/replay traces for evaluation
grouping keys needed by downstream split logic
```

The export is derived, immutable and non-authoritative. It must not become canonical game state or a second evidence store.

## 5. Input / target / evaluation separation

Any future Host export must make leakage boundaries explicit.

At minimum downstream consumers must be able to classify fields as:

```text
INPUT_ELIGIBLE
TARGET_OR_LABEL
EVALUATION_METADATA
PROVENANCE_ONLY
```

For historical decisions:

- pre-decision canonical state, legal domain and permitted enrichment may be input;
- observed/expert choice is target/evaluation metadata, not input;
- source rationale is target/evaluation/provenance unless a specific future task explicitly defines rationale generation;
- resulting commitment and later game history are not recommendation input;
- final outcome/winner must never leak into the historical recommendation input.

EvidenceLab's historical prefix and Host's canonical `historyPrefixRef` must converge on the same anti-hindsight boundary for imported historical cases.

## 6. Candidate and negative semantics

The Host is allowed to derive:

`LEGAL_UNCHOSEN`

because it owns legal-domain reconstruction.

That relation means only:

> legal at the decision boundary and not the historical selected choice.

It does **not** mean rejected, poor or lower-ranked.

The future export/dataset path must preserve at least:

```text
EXPLICIT_REJECTED
EXPLICIT_COMPARISON_LOSER
LEGAL_UNCHOSEN
SYNTHETIC_NEGATIVE
```

Only the first two are source-backed negative/preference evidence.

Negative-sampling policy belongs to ModelLab/dataset recipes, not to canonical Host state, legality, DecisionTrace or EvidenceLab.

## 7. Future model policy boundary

A future model must enter through a replaceable recommendation-policy / replay adapter after legality and canonical context projection.

Target shape:

```text
canonical state / typed decision context
        |
rules-owned complete legal domain
        |
deterministic feature/context projection
        |
policy runner
   +-- BEGINNER_CONSERVATIVE_V1
   +-- BEGINNER_CONSERVATIVE_V2
   +-- future external/model policy
        |
validated recommendation / DecisionTrace
```

HOST-ML0 does **not** authorize an `MLPolicyRunner` now.

Do not force pair outcomes into unrelated dynamic-outcome abstractions merely to prepare for ML. Let C5-B establish the correct pair replay seam first; generalize only after the common semantic interface is proven.

## 8. ModelLab ownership

A future independent ModelLab/dataset builder should own:

- conversion from Host neutral exports + EvidenceLab seeds into actual training examples;
- SFT / preference / ranking JSONL schemas;
- train/validation/test split manifests;
- grouping by game, source and Storyteller independence key;
- negative sampling recipes;
- tokenizer/prompt formatting;
- model selection;
- LoRA / QLoRA / DPO or other training;
- model checkpoints/adapters;
- offline benchmark aggregation.

These concerns must not leak into the Android Host production dependency graph.

## 9. Relationship to C5

HOST-ML0 must not delay or broaden C5.

Current route remains:

```text
C5-A pair future-flexibility projector     COMPLETE / ACCEPTED
-> C5-B pair SDE shadow + replay bridge    COMPLETE / ACCEPTED
-> C5-C BEGINNER_CONSERVATIVE_V2           COMPLETE / ACCEPTED
-> C5-D canonical V1/V2 replay             COMPLETE / ACCEPTED
-> C5-E surface-scoped production cutover  NEXT
```

C5-B should make **no ML-specific production changes**. It should simply preserve the existing architecture invariants that future ML needs:

- complete legal domain;
- stable candidate order/identity;
- registration witnesses;
- canonical history prefix;
- source revision/freshness identity;
- deterministic feature recomputation;
- actual historical/manual choice separate from policy choice;
- replay is read-only and non-authoritative.

C5-C remains a normal evidence-backed deterministic V2 policy slice.

C5-D has now passed the implementation trigger for the first Host ML-ready code: exact-head `5241518e002e65993eed594632604e13e2979bfd`, CI #3693 / R2 #3401 GREEN, proves deterministic same-history V1/V2 replay and trace round-trip on the real pair surface. HOST-ML1 is therefore technically eligible, but must remain a separate lane and must not delay the immediate C5-E product cutover gate.

## 10. Ordered ML route

Accepted long-horizon order:

```text
EL-ML0 EvidenceLab readiness contract                 COMPLETE / ACCEPTED
HOST-ML0 Host/ModelLab boundary audit                 COMPLETE / ACCEPTED
-> C5-B pair shadow/replay                            COMPLETE / ACCEPTED
-> C5-C deterministic V2                              COMPLETE / ACCEPTED
-> C5-D canonical V1/V2 replay                        COMPLETE / ACCEPTED
-> HOST-ML1 neutral RecommendationDecisionExportV1    ELIGIBLE / SEPARATE LANE; DO NOT BLOCK C5-E
-> EL-ML1 RecommendationEvidenceSeedV1                TRIGGER ONLY IF HOST-ML1 NEEDS MACHINE-READABLE SEEDS
-> MODELLAB-0 offline dataset/evaluation builder      AFTER STABLE EXPORT/SEED CONTRACTS
-> first zero-shot / SFT / preference experiment      OFFLINE ONLY
-> external-model policy adapter                      SHADOW/REPLAY FIRST
-> any production model cutover                       REQUIRES SEPARATE FUTURE GATE
```

This route is not a reason to pause TBGS-2. TBGS-2 continues incremental migration toward canonical snapshot + typed decision contexts and therefore improves ML readiness indirectly.

## 11. HOST-ML1 acceptance trigger

Do not implement HOST-ML1 merely because HOST-ML0 exists.

HOST-ML1 becomes eligible after:

1. C5-B pair replay bridge is accepted;
2. C5-C creates a real immutable V2;
3. C5-D proves deterministic same-history V1/V2 replay and trace round-trip;
4. there is a concrete downstream need to materialize a policy-neutral offline decision corpus.

Initial HOST-ML1 scope should be bounded to already-mature decision surfaces, expected to begin with:

- Drunk assignment;
- first-night pair information.

Do not generalize to every Storyteller decision family in the first export.

## 12. Deferred work

Explicitly deferred:

- `RecommendationDecisionExportV1` implementation;
- `RecommendationTrainingExampleV1`;
- EvidenceLab `RecommendationEvidenceSeedV1` implementation;
- durable Q04-style expert-guidance schema changes;
- ModelLab repository/toolchain;
- prompt format;
- embeddings/RAG;
- SFT/DPO/QLoRA;
- model selection/size;
- synthetic negatives;
- external-model runtime inference;
- production model authority;
- non-TB/cross-script generalization.

## 13. Immediate next task

Continue the product mainline with **C5-E — surface-scoped production cutover gate**. HOST-ML1 is now technically eligible because C5-B/C/D replay prerequisites are accepted, but it remains a separate ML-readiness lane and must not delay the bounded functioning-Librarian production cutover.

When HOST-ML1 is started, keep its first export scope bounded to mature Drunk-assignment and first-night pair-information surfaces, and trigger EvidenceLab EL-ML1 only if machine-readable evidence seeds are actually required.
