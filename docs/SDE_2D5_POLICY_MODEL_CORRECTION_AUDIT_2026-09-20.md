# SDE-2D5 — BEGINNER Policy Model Correction Audit

> Date: 2026-09-20 Australia/Sydney
> Repository: Jazz0006/CampBoardGameHost
> Branch: sde-2d5-calibration-policy-evidence
> Status: ROUTE CORRECTION REQUIRED BEFORE D5F HUMAN LABELS / GATE DERIVATION

## 1. Why this audit exists

D5A-E were built around a materially better model than the legacy score heuristics:

- exact legality remains outside recommendation policy;
- strategic Evil topology replaces raw role-world multiplicity as the primary strategic unit;
- role-information utility remains separate from strategic pressure;
- Drunk clues are whole-bundle candidates;
- Demon bluffs are joint outputs;
- normalized diagnostics scale across player counts.

Subsequent external-human evidence and APP ownership review improved the definition of a good BEGINNER clue further.

The correction is primarily in the policy layer above the diagnostics, not in the D2D1-D2D4 epistemic/topology foundation.

## 2. Old assumptions versus corrected model

### 2.1 Old approximation: information strength is mostly a monotone pressure problem

Earlier calibration emphasized:

- lower/higher world retention;
- strongest strategic collapse;
- weakest mechanical information;
- Drunk marginal pressure;
- high/low Demon-bluff shared-to-union support.

These are useful diagnostics but are not themselves a quality ordering.

Corrected model:

> BEGINNER quality is an acceptable ecology band: enough reliable information to reason, enough ambiguity for Evil to play, and no premature multi-channel collapse.

A single strong clue is not automatically bad. A rare healthy Empath 2 may simply be an Evil challenge. The primary pathology is catastrophic interaction / confirmation-chain collapse, not isolated clue strength.

### 2.2 Old approximation: impaired information should usually be false

Current legacy production code still expresses this directly:

- `ImpairedInformationPolicyConfig.falseFamilyMassFixedPoint = 900_000`;
- `IMPAIRED_FALSE_PREFERRED`;
- tests require roughly 90% false-family selection;
- `MalfunctionPolicy` scores numeric candidates partly by distance from truth and categorical candidates by misinformation pressure.

Corrected model:

> Drunk/Poisoned legality includes truthful and false candidates. Truth relation is descriptive, not a preferred semantic family.

A truthful impaired clue may be preferable when it:

- avoids making impairment obvious;
- preserves temporal continuity;
- supports a more believable alternate world;
- avoids overcorrecting a table already favorable to Evil;
- leaves future correction capacity.

The fixed false-family budget is therefore a legacy policy contradiction.

### 2.3 Old approximation: Demon-bluff robustness can be calibrated mainly through shared support

D5D correctly kept union/shared/distinct-pattern metrics separate and did not freeze a threshold. However D5F currently selects only low/high `sharedToUnionRetention` bluff contrasts for human review.

Corrected model:

- individual bluff support needs a floor;
- shared support may be a coherence floor, not a monotone objective;
- route diversity matters;
- execution / claim burden matters;
- complementary strategic coverage may be useful after coherence is adequate;
- cross-channel narrative coherence matters;
- later continuation/supportability matters.

Therefore low/high shared-to-union is useful reference evidence but insufficient as the primary bluff calibration surface.

### 2.4 Old approximation: healthy Night-1 information factors are broadly Storyteller choices

The pre-SDE-3 route describes `healthy information choices` too broadly.

Current candidate-space code is more correct than that wording:

- healthy Chef/Empath fixed numeric information is `RULE_DETERMINED` when only one healthy truth exists;
- Washerwoman/Librarian/Investigator pair information remains Storyteller-controlled within legal domains;
- Fortune Teller targets are player-controlled;
- Red Herring is setup/Storyteller-controlled;
- impaired information is Storyteller-controlled where rules allow.

Corrected rule:

> A policy may optimize only variables still owned and controllable at that lifecycle stage.

Healthy deterministic Chef/Empath results are diagnostics after setup, not SDE decision variables.

### 2.5 Old approximation: dangerous healthy truth should be repaired structurally

External-human Storytellers sometimes respond to dangerous seating by changing which token is the Drunk while distributing roles. The APP cannot do this after setup persistence.

A temporary audit proposed `SetupTruthExposureRisk` as a possible reroll/reassignment trigger.

After probability analysis and product discussion, the corrected decision is narrower:

- keep current random/template seating behavior;
- do not reroll merely because one healthy Empath gets an extreme result;
- treat isolated strong truth as legitimate game variance;
- retain truth-danger only as diagnostic context and as an input to bundle-level catastrophic-interaction analysis;
- only future evidence of systematic multi-channel setup collapse would justify a setup reroll policy.

Therefore no new automatic setup-seating reroll gate is currently required.

## 3. What remains correct and should be preserved

### 3.1 Preserve without redesign

The following architecture is still aligned with the corrected model:

- exact legality / epistemic consequence authority;
- `TroubleBrewingRegistrationDomain`;
- `StrategicWorldKey(demonSeat, minionSeats)`;
- topology-first 5-15 evaluator;
- `NormalizedStrategicDiagnosticsProjector`;
- Demon-cover / Evil-topology / Evil-cover / forced-good diagnostics;
- role-information utility separated from strategic pressure;
- `HealthyCore / FullBundle / DrunkMarginal`;
- all legal Drunk outputs, including accidentally truthful outputs;
- Demon bluff legality in `SetupCandidateGenerator`;
- Demon-bluff per-role strategic support, union/shared support and distinct strategic-pattern diagnostics;
- Drunk shown-role persistence;
- committed Demon-bluff persistence;
- Poisoner target ownership by the Evil player;
- first-night candidate-space control metadata (`RULE_DETERMINED` vs `STORYTELLER_CONTROLLED`);
- holdout discipline and no-opaque-global-score rule.

D2D1-D2D4 do not need to be rolled back.

### 3.2 D5A and D5B remain valid

D5A normalized projections and D5B cross-regime baselines are foundational descriptive evidence and require no conceptual repair.

### 3.3 D5C-D5E evidence remains valid but is incomplete for policy calibration

The existing evidence should be retained as descriptive contrast data.

The correction is:

- do not infer a monotone quality direction from it;
- expand the review surface before gate derivation;
- do not let one selected contrast stand in for the whole policy axis.

## 4. Implemented code that is now policy-stale

### 4.1 `ImpairedInformationPolicy` — keep as temporary compatibility policy until SDE cutover

Current stale behavior:

~~~text
default false family mass = 90%
truth vs false family chosen before strategic consequence quality
IMPAIRED_FALSE_PREFERRED encoded as the normal path
~~~

Long-term target remains:

~~~text
all rule-legal truthful/false candidates
    -> same consequence/policy comparison surface
truthRelation
    -> diagnostic feature, not family budget
~~~

However the current 90/10 split was deliberately introduced as a temporary product bridge before the consistency/SDE algorithm existed. Do not remove the production compatibility behavior during D5F-B3.

What should be removed now are tests that freeze the exact 90/10 percentage as if it were durable policy. Keep only compatibility/safety tests that prove legal candidates, deterministic replay and fallback behavior. The production bridge is retired only when the replacement SDE owner is ready.

### 4.2 `DynamicCandidateGenerator.select` — remove semantic truth-family preallocation

Current selection first allocates probability mass to truthful vs false families through `ImpairedInformationPolicy`, then ranks inside the active family.

Target:

- do not exclude/downweight a candidate merely because it is truthful while impaired;
- evaluate legal candidates through SDE consequence diagnostics;
- apply interpretable gates/bands;
- perform deterministic weighted selection only among candidates that remain acceptable;
- preserve truth relation in telemetry/explanations.

The current `misinformationMassFixedPoint(...)` compatibility surface and 90%-false tests should be retired when the new owner cuts over.

### 4.3 `MalfunctionPolicy` — retain as legacy recommendation bridge, not target policy

Stale concepts:

- aggressive style rewards distance from truth;
- categorical score rewards misinformation pressure;
- `maximum-false-pressure` is treated as a special semantic condition.

Useful parts to preserve temporarily:

- candidate legality is not owned here;
- previous shown value / history continuity is already represented;
- warning plumbing can be reused.

Target:

- do not spend D5F-B3 effort rewriting this legacy bridge;
- keep current production behavior until the consistency/SDE replacement is ready;
- stop treating its static truth-distance/misinformation-pressure scores as calibration truth;
- later move temporal continuity into exact/history-aware SDE context;
- retire this policy as final ranking owner only at cross-night/production cutover.

### 4.4 `SetupEvaluator` / `SetupRecommendationService` — keep as compatibility only

Current legacy heuristics include:

- `demon-bluff-ease` based on static `bluffDifficulty`;
- static Red-Herring role suitability/exposure;
- static pair discussion/exposure values;
- old setup score tolerances and GENTLE/BALANCED/AGGRESSIVE profiles.

Correction:

- `bluffDifficulty` may survive only as execution-burden metadata;
- it must not represent complete bluff quality;
- Red-Herring static suitability is insufficient for future trajectory quality;
- `RecommendationProfiles` styles are legacy selection profiles, not the new BEGINNER policy definition;
- normal mode targets one calibrated BEGINNER recommendation; experienced mode is manual rather than an "aggressive auto" policy.

`SetupCandidateGenerator` legality ownership remains valid.

### 4.5 Legacy shown-role scoring versus durable shown-identity setup pipeline

`SetupCandidateGenerator.generatePlans` already rejects `StorytellerDecision.DrunkShownRole`, because shown identity is now committed upstream.

`SetupEvaluator.evaluateClue` still contains a legacy `DrunkShownRole` scoring branch and `TroubleBrewingRecommendationMetadata.drunkSuitability`. That legacy scoring path is not the durable owner.

The durable capability is already generic and must be preserved for future scripts without curated templates:

- `SetupShownIdentityPolicyResolver.resolveGenerated(...)` derives legal shown-role options for a generated setup (currently all unused Townsfolk for Drunk);
- `SetupShownIdentityCommitter` commits one option deterministically from the setup seed before seating/materialization;
- template-backed scripts can supply a smaller curated option set through `TemplateShownIdentityPolicySource`.

Therefore future generated/custom scripts do still need **shown-identity selection**, but they do not need the old `SetupEvaluator` scoring branch.

If quality-aware shown-role selection is later desired, insert a generic setup-stage selector between policy resolution and commitment rather than reviving `drunk-shown-role-suitability`. That selector may use role/composition-level diagnostics while preserving the setup persistence boundary. Current seeded-random commitment is an acceptable fallback.

## 5. D5 calibration corrections required before human review

The current eight-item v1 manifest is all `UNREVIEWED`, which is fortunate.

Do not proceed from it directly to D5F-C.

### 5.1 Drunk review surface

Current D5F exports one false numeric Drunk candidate.

That is inadequate for the corrected model.

Add a deterministic contrast group containing, for the same setup:

- the truthful candidate;
- at least one mild false candidate;
- a stronger false candidate when legal;
- HealthyCore reference;
- FullBundle and marginal strategic diagnostics for each.

Review should ask about the whole ecology, not whether "false" is desirable.

First-night D5 need not solve temporal consistency; that remains SDE-3. But D5 should stop encoding falsehood as the representative impaired case.

### 5.2 Demon-bluff review surface

Keep:

- supported-role count;
- per-role strategic support;
- union/shared strategic worlds;
- distinct strategic patterns.

Change review selection:

- low/high shared-to-union become reference contrasts, not the sole bluff review sample;
- add at least one human-observed external triplet;
- include individual-support floor;
- expose pairwise/role coverage difference diagnostically;
- add explicit static role-trait metadata for:
  - claim burden/cadence;
  - narrative route class;
  - beginner execution difficulty.

Do not yet reward coverage complementarity numerically.

### 5.3 Healthy role-information / bundle interaction

D5E's topology-neutral and near-raw/different-topology evidence remains valuable.

Add a bundle-interaction view to distinguish:

~~~text
single strong clue
    acceptable challenge

several individually reasonable clues
    -> joint Demon-cover / topology collapse
    problematic confirmation chain
~~~

Minimum first implementation can reuse existing leave-one-out evaluations:

- full-bundle normalized diagnostics;
- each leave-one-out normalized diagnostic;
- worst good-recipient pressure, not one anchor only where feasible;
- count/identify clues whose removal restores Demon cover/topology;
- identify full-bundle collapse that is not attributable to one uniquely catastrophic clue.

Do not invent a single "confirmation-chain score" yet.

### 5.4 Review reason vocabulary

Current reasons are too narrow.

Retain existing reasons but add/replace policy-facing reasons sufficient to express:

- `EXCESSIVE_CONFIRMATION_CHAIN`;
- `INSUFFICIENT_HEALTHY_INFORMATION`;
- `IMPAIRED_CLUE_TOO_REVEALING`;
- `IMPAIRED_CLUE_COHERENT`;
- `BLUFF_EXECUTION_BURDEN`;
- `BLUFF_NARRATIVE_REDUNDANCY`;
- `BLUFF_COHERENCE_FRAGILE`;
- `BLUFF_ROUTES_USABLE`;
- `CROSS_CHANNEL_NARRATIVE_COHERENCE`;
- `OTHER_EXPLICIT_REVIEW_REASON`.

Temporal inconsistency belongs primarily to SDE-3 cross-night review rather than first-night D5F.

### 5.5 Manifest versioning

The current v1 manifest should not be hand-labeled and then retrofitted.

Recommended transition:

~~~text
current v1 manifest
    -> preserve as obsolete/unreviewed evidence checkpoint

correct review schema + selected records
    -> generate v2 manifest, all UNREVIEWED

human review v2
    -> D5F-C candidate gates
~~~

No existing human labels need migration because v1 is still fully UNREVIEWED.

## 6. Route impact

### Current route before correction

~~~text
D5F-B human review
-> D5F-C gates
-> freeze
-> holdout
-> D5 acceptance
-> SDE-3
~~~

### Corrected route

~~~text
D5F-B2 external-human evidence              COMPLETE / ACTIVE CATALOG
D5F-B3 policy-model correction              NEXT
    - freeze control-surface semantics
    - remove false-is-preferred assumption from target model
    - expand D5 review schema
    - add bluff execution/narrative traits
    - add Drunk truth/false contrast set
    - add bundle confirmation-chain diagnostics
    - generate v2 review manifest

D5F-B human review v2
D5F-C interpretable gate/band derivation
D5F-D freeze
D5F-E sealed holdout
D5F-F final acceptance
SDE-3 cross-night policy
    - temporal consistency
    - impairment detectability
    - live narrative coherence
    - future correction capacity
    - replacement of fixed false-family production policy
SDE-4 production cutover / legacy retirement
~~~

This adds one corrective slice but avoids calibrating the wrong policy and then rebuilding it after holdout.

## 7. What should NOT be added now

Do not add:

- automatic reroll for a lone healthy Empath 2;
- setup mutation because a healthy Chef/Empath truth is merely strong;
- a new global weighted balance score;
- a hard "false clues preferred" rule;
- a hard shared-to-union maximization rule;
- a numeric coverage-diversity bonus before human calibration;
- a second world solver;
- SDE ownership of Poisoner targets;
- SDE ownership of committed Drunk shown identity.

## 8. Concrete keep / change / remove summary

### KEEP

- D2D1-D2D4 mechanics and topology architecture;
- D5A/B normalized cross-regime evidence;
- D5C/D/E raw descriptive evidence;
- all legal candidate generation;
- current setup/template/deal distribution;
- strategic topology metrics;
- holdout process;
- external human case catalog.

### CHANGE / EXPAND BEFORE D5F LABELING

- D5 Drunk calibration from one false point to truthful+mild/strong false contrast;
- D5 bluff review from shared/union extremes to multi-axis review;
- D5 bundle review to expose confirmation-chain interaction and multi-recipient risk;
- review reason vocabulary;
- manifest version to v2;
- route wording that currently overstates "healthy information choices" as Storyteller-controlled.

### RETIRE / REPLACE DURING SDE-3 / SDE-4

- 90% impaired false-family budget (retain temporarily until SDE replacement, but remove tests that freeze the exact percentage as durable policy);
- `IMPAIRED_FALSE_PREFERRED` as long-term policy meaning (temporary compatibility code may remain);
- truth-distance / misinformation-pressure ranking as final malfunction owner (legacy bridge may remain until cutover);
- legacy `demon-bluff-ease` as complete bluff-quality owner;
- GENTLE/BALANCED/AGGRESSIVE as the target BEGINNER policy model;
- obsolete Drunk shown-role scoring path after caller audit;
- legacy static setup recommendation scoring once SDE production cutover is complete.

## 9. Acceptance condition for resuming human review

Do not resume the eight-record human review until:

1. the corrected v2 review dimensions are implemented;
2. the generated review set explicitly covers truth/false Drunk alternatives;
3. bluff records expose execution/narrative dimensions in addition to strategic support;
4. confirmation-chain evidence is visible without treating isolated strong clues as automatic failures;
5. every review record states its lifecycle owner/control surface;
6. the v2 manifest is deterministically regenerated and validates as incomplete/all-UNREVIEWED;
7. sealed holdout remains untouched.
