# SDE-3B Structure / Feature / Expert-Evidence Staging — 2026-09-23

> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `sde-3b-beginner-conservative-v1`  
> PR: #153 (draft)  
> Evidence input: `Jazz0006/ClocktowerEvidenceLab` / `e1-domain-persistence-foundation` / `docs/C0_TB_TO_CAMPBOARDGAMEHOST_SDE_HANDOFF_2026-09-23.md`  
> Status: **CURRENT SDE-3B EXECUTION ORDER**

## 1. Why the SDE-3B route changes

The first SDE-3B core checkpoint is valid:

- unavailable features remain explicit;
- no unsupported numeric thresholds exist;
- only exact zero Evil strategic-topology retention is a generic hard contradiction gate;
- otherwise-viable candidates remain in an explicit survivor equivalence band;
- seeded survivor selection is deterministic and weight-free;
- current numeric shadow preserves source `AbilityState` and actual-state semantic truth.

The new Evidence Lab handoff changes what should happen next.

The main risk is no longer “missing a policy rule”. The main risk is **turning expert observations into policy before the engine has the lifecycle/history/input structure needed to represent why the expert choice mattered**.

Therefore SDE-3B proceeds in three engineering stages:

```text
STRUCTURE
    ->
FEATURE SEMANTICS
    ->
EXPERT-INFORMED PROVISIONAL POLICY
```

Do not reverse this order.

## 2. Four different ways expert evidence may enter

Expert/real-game evidence is allowed to influence the project at four different authority levels.

### E1 — architecture / feature-existence evidence — use NOW

Whole-game records may establish that a dependency exists and therefore deserves a generic feature or ownership seam.

Examples:

- R02/R04 show confirmation chains matter;
- R04 repeated Fortune Teller information shows trajectory/history matters;
- R02/R04/R06 show impairment needs historical context;
- R01/R04 show healthy truthful channels alter the marginal value of misinformation;
- expert Ravenkeeper/Spy guidance shows role exposure is lifecycle/context dependent.

Permitted use:

- choose feature-projector priority;
- choose required lifecycle/input fields;
- define generic typed contracts;
- define regression scenarios.

Not permitted:

- candidate preference;
- score;
- weight;
- threshold.

### E2 — semantic regression evidence — use once the structural seam exists

A reconstructed real game may test whether the generic projector represents the intended dependency correctly.

Examples:

- R02 can validate that a later Undertaker result participates in a confirmation chain without leaking backward into Night 1;
- R04 can validate repeated-information trajectory and Demon-transfer chronology;
- R06 can validate temporary Poisoner corruption rather than persistent Drunk-style impairment.

Permitted use:

- fixture/replay-level semantic tests;
- expected dependency/trajectory shape;
- committed-prefix / no-hindsight assertions;
- feature provenance validation.

The production implementation must remain generic. Never branch on R02/R04/R06 IDs or named role combinations merely to pass the evidence case.

### E3 — expert qualitative policy evidence — use only after the matching feature is stable

A feature may become a V1 soft preference only when:

1. the feature has a production-owned typed projector;
2. the lifecycle/input binding is complete for that feature;
3. semantic regression cases are green;
4. the preference has explicit qualified Storyteller rationale or sufficiently strong cross-expert qualitative support;
5. the rule can be expressed generically without a numeric strength threshold.

Permitted examples, once prerequisites are satisfied:

- prefer a continuation that preserves an already-established believable impaired narrative over an unnecessary contradiction;
- avoid a destructive confirmation-chain collapse when a healthy alternative exists;
- prefer preserving at least one usable healthy-information route when a candidate would otherwise leave none.

These are soft/near-hard qualitative reasons, not scores.

### E4 — calibrated thresholds / tradeoffs — defer to SDE-3D

Numeric gates, player-count bands, severity thresholds and multi-axis tradeoff strength require broader calibrated evidence.

Examples that remain blocked:

- exact healthy-information floor;
- “how much” Evil-topology retention is enough;
- exact role-exposure severity;
- Demon-bluff triplet ordering weights;
- multi-axis weighted score;
- player-count coefficients.

This remains SDE-3D, not SDE-3B.

## 3. Revised SDE-3B implementation order

### SDE-3B0 — conservative policy core — COMPLETE CHECKPOINT

Already implemented on PR #153:

- decision-level Ready/Deferred policy result;
- explicit limitations;
- exact zero Evil strategic-topology contradiction gate;
- survivor equivalence band;
- deterministic weight-free seeded selector;
- Chef/Empath shadow attachment;
- source `AbilityState`;
- actual-state semantic truth.

No production cutover.

### SDE-3B1 — historical interaction structure and input binding — COMPLETE

This is a structural slice, not an expert-policy slice.

Goals:

1. generalize the SDE shadow evaluation input from “first-night numeric interaction only” toward a lifecycle-safe historical interaction envelope;
2. preserve exact committed-prefix chronology;
3. bind contextual inputs explicitly rather than leaving `SdeDecisionInputBindings.NotCaptured` where policy depends on them;
4. reuse canonical `ActionFactTimeline` and `EpistemicObservationLog`; do not add a second mutable narrative history;
5. preserve player-owned versus Storyteller-owned choices.

Inputs that must become bindable/referenceable include, as applicable:

- Fortune Teller selected targets;
- Poisoner target;
- Red Herring setup commitment;
- Drunk shown identity;
- revealed/locked Demon bluffs;
- prior delivered observations relevant to the interaction;
- role/alignment transitions such as Demon succession.

Important:

- do not infer actual impairment from player-facing `ObservationReliability.RECEIVED_AS_FUNCTIONING`;
- current actual impairment comes from canonical rules/session semantics, already beginning with projected `AbilityState`;
- historical impairment should be reconstructed from canonical setup/action history where possible rather than copied into a second state model.

Evidence role in 3B1: **E1 only**. Real games tell us which lifecycle dependencies the structure must be capable of representing. They do not choose candidates.

Completion authority: `SDE_3B1_HISTORICAL_LIFECYCLE_INPUT_BINDING_COMPLETION_AUDIT_2026-09-23.md`.

Implemented completion boundary:

- historical structured shadow supports later lifecycle points;
- canonical `ActionFactTimeline + EpistemicObservationLog` committed-prefix refs are attached to candidates;
- future facts are rejected as hindsight;
- contextual inputs use typed owner refs and remain `NotCaptured` until audited by a consumer;
- Poisoner target ownership is exercised as a player-controlled binding;
- Drunk shown identity remains setup-owned;
- FT targets, Red Herring and Demon bluffs are bindable without copying their external owner state;
- no policy preference changed.

### SDE-3B2 — confirmation-chain feature projector — COMPLETE

First new non-strategic feature after the lifecycle/input seam.

Why first:

- it has repeated whole-game support;
- it affects both healthy and impaired information;
- it is required before “more misinformation” can be evaluated safely;
- it is not specific to one role.

Generic semantics should represent:

- support/contradiction relation between committed observations/claims and the current candidate;
- whether a new observation authenticates another information source or claim;
- whether a major ambiguity route is removed;
- committed-prefix only: future confirmation must never leak backward.

Do not hard-code Ravenkeeper → Undertaker, Investigator → Undertaker, or any named chain.

Evidence usage:

- E1 defined the feature boundary;
- E2 is now represented by bounded R02/R04-style semantic regressions with strict no-hindsight;
- E3 remains deferred until a generic policy reason is separately justified.

Completion authority: `SDE_3B2_CONFIRMATION_CHAIN_COMPLETION_AUDIT_2026-09-23.md`.

Implemented completion boundary:

- pure score-free `ConfirmationChainFeaturesProjector` owns support / contradiction / independent-contribution classification plus exact ambiguity-restoration facts;
- `HistoricalConfirmationChainFeatureProjector` owns leave-one-out orchestration over canonical committed observations without persisting/copying history;
- recipient visibility is enforced before a historical observation can contribute to a candidate's chain;
- future observations are rejected by the 3B1 committed-prefix boundary;
- structured shadow attaches the projection to `DecisionFeatures.confirmationChainImpact`;
- V1 policy does not use confirmation-chain features to rank/reject candidates;
- no named-role chain table, numeric threshold, or new policy reason was introduced.

### SDE-3B3 — impaired-narrative coherence projector — CURRENT

Build after 3B1, and preferably after the confirmation/dependency representation is available.

Owner:

```text
ActionFactTimeline
+ EpistemicObservationLog
+ current candidate proposition
+ authoritative source AbilityState / impairment episode
    ->
derived historical narrative features
```

No separate persisted perceived-world state.

Required distinctions:

- Drunk: persistent subjective-world pressure across interactions;
- Poisoner: temporary target/time-bounded corruption;
- both use the same generic narrative projector;
- role-specific code owns legality/proposition semantics only.

Initial feature output should remain descriptive, for example:

- candidate remains historically coherent;
- candidate introduces a new contradiction;
- candidate requires a narrative transition;
- prior narrative is no longer feasible;
- transition is forced versus avoidable.

Do not create numeric “coherence scores” yet.

Evidence usage:

- E2: R04 repeated Drunk trajectory and R06 temporary poison;
- E3: explicit expert believability/continuity rationale may justify a soft “avoid needless contradiction” preference;
- E4 severity remains SDE-3D.

### SDE-3B4 — healthy-information utility / remaining usable channels

Only after history/confirmation semantics are available.

Goal:

- evaluate the marginal effect of another candidate on the whole information bundle;
- distinguish “false output exists” from “table still has enough independent useful healthy structure”.

Do not implement a fixed misinformation budget or healthy-information percentage.

Initial exact/categorical features may include:

- whether any independent healthy route remains;
- which healthy observation routes are preserved/lost;
- whether a candidate removes the last currently usable healthy route.

Evidence usage:

- E1/E2: R01/R04;
- E3 only for exact categorical boundaries that have explicit support;
- numeric floor stays SDE-3D.

### SDE-3B5 — contextual role-function exposure

Implement only after lifecycle and confirmation context are available, because exposure severity changes with game stage.

Examples such as Spy/Ravenkeeper are evidence for the dimension, not special cases.

Feature should capture generic exposure mechanics such as:

- whether a candidate directly reveals a hidden mechanic/role whose function depends on ambiguity;
- whether the information is already independently exposed;
- whether the candidate creates a new confirmation route;
- whether exposure is forced or avoidable.

At first this should remain diagnostic / limitation-bearing.

Do not turn it into a universal reject until replayable expert evidence contains explicit considered alternatives.

### SDE-3B6 — expert-informed V1 soft priorities

Only now should PR #153 begin to grow beyond its current structural contradiction gate.

Policy may consume only projectors that have completed 3B2–3B5 semantic validation.

Preferred rule form:

```text
typed feature predicate
    ->
typed reason code
    ->
REJECT / ACCEPT / SURVIVOR refinement
```

No total score.

Candidate ordering should remain partial. When evidence does not justify an ordering, retain a tie and use the existing seeded selector.

Potential first qualitative preferences, subject to completed feature evidence:

1. avoid an avoidable historical narrative contradiction;
2. avoid a clearly destructive confirmation-chain collapse when a healthier alternative exists;
3. avoid eliminating the last usable healthy-information route;
4. keep contextual role-function exposure as a cost, not a blanket prohibition.

Red Herring and Demon-bluff policy should enter only when their contextual feature projectors exist.

## 4. Where SDE-3C now begins

SDE-3C remains the owner of durable `DecisionTrace` persistence and multi-policy replay records.

However, SDE-3B1 may add a **non-persistent historical shadow/replay seam** because feature semantics cannot be validated cross-night without replaying canonical history.

Boundary:

```text
SDE-3B
    may reconstruct/read historical state for feature evaluation
    must not persist recommendation traces

SDE-3C
    persists DecisionTrace
    records policy version/features/recommendation/actual choice/override
    supports systematic V1/V2/V3 replay
```

This avoids blocking cross-night feature work on trace persistence while preserving ownership.

## 5. Revised evidence-to-engine loop

```text
ClocktowerEvidenceLab whole game
    ->
identify lifecycle/feature dependency              [E1]
    ->
generic CampBoardGameHost structure/projector
    ->
replay matching real-game prefix as regression     [E2]
    ->
explicit qualified rationale / cross-expert signal [E3]
    ->
qualitative V1 reason / partial ordering
    ->
DecisionTrace + broad replay (SDE-3C)
    ->
versioned corpus comparison
    ->
calibrated thresholds/tradeoffs (SDE-3D / E4)
```

This is the default path for every new expert-derived idea.

## 6. Evidence cases and intended engineering use

| Evidence | Structural use now | Feature regression later | Policy use |
| --- | --- | --- | --- |
| R02 Drunk → Undertaker | committed-prefix / cross-night history | confirmation + narrative | qualitative only after projector |
| R04 long trajectory / moving poison / Demon transfers | lifecycle + input binding | confirmation, narrative, future flexibility | no numeric weights |
| R06 poison → RK false info | impairment episode boundary | temporary-poison narrative | qualitative only |
| R01 parallel true/false channels | whole-bundle requirement | healthy-information utility | no budget threshold |
| Beardy RK/Spy guidance | feature existence / lifecycle context | role exposure + confirmation semantics | soft reason only if generic |
| Ben/Evin explicit rationale already in SDE corpus | contextual feature motivation | RH / impaired believability regressions | qualitative support, not weight |

## 7. Stop conditions

Do not promote an expert-derived idea into policy when any of the following is true:

- the matching feature is still `Unavailable`;
- required historical inputs remain `NotCaptured`;
- the implementation would need a named role/game special case;
- the evidence is only an observed silent choice with no rationale;
- the rule requires choosing a numeric threshold not supported by calibration;
- the result depends on later events that were not committed at the original decision boundary;
- the candidate depends on a player-controlled choice not yet bound as input.

In those cases keep the candidate set tied and expose the limitation.

## 8. Immediate next action on PR #153

Do **not** add another policy reason first.

Next work:

1. perform the 3B3 architecture/fanout audit before production edits;
2. treat canonical setup/action history plus current authoritative `AbilityState` as impairment truth; `ObservationReliability` is not that authority;
3. derive narrative features from `ActionFactTimeline + EpistemicObservationLog` rather than persisting a second perceived-world state;
4. define one role-agnostic feature contract capable of persistent Drunk trajectories and temporary Poisoner episodes without policy special cases;
5. start with descriptive coherence / contradiction / transition / detectability facts, not a score;
6. add generic tests first, then bounded R02/R04/R06 E2 semantic regressions;
7. keep V1 policy unchanged until 3B3 feature semantics are stable.

Keep #153 draft.
