# EPI-MQ Route Re-audit — Unified Impaired-Information Selection

> Date: 2026-09-15 Australia/Sydney  
> Repository baseline: `main` at `1cdc35886aea654ad82bf4e2a388095880686ed5`  
> Program: Epistemic Misinformation Quality / Productive Uncertainty  
> Decision: **MODIFY — EPI-MQ must evaluate truthful and false legal information in one quality model and ultimately decide both whether to lie and which lie to tell.**

## 1. Why this re-audit exists

The earlier EPI-MQ audit correctly established the architectural foundations: legal candidates already exist, exact recipient-world reasoning already exists, unsupported epistemic semantics must defer rather than fake UNSAT, and the B4-specific historical evaluator should be extracted into neutral epistemic ownership.

A later global live-code audit exposed one important route correction.

The current recommendation pipeline first gives truthful and false candidate families separate probability mass through `ImpairedInformationPolicy`, then ranks candidates inside the selected family. The current default impaired policy gives false information most of the probability mass. This was a useful temporary implementation because it prevented impaired information from being deterministically false, but it is not the desired final Storyteller behavior.

For the product goal of replacing a human Storyteller, EPI-MQ must not permanently inherit a fixed truth-vs-false probability gate as the primary decision authority.

## 2. Core product decision

The first-class EPI-MQ question is not only:

> Which legal false clue is best?

It is:

> Among all legal information the impaired ability may present, which candidate best creates coherent, productive uncertainty in the recipient's current knowledge state?

Therefore truthful and false candidates must eventually compete in the same epistemic-quality model.

Conceptually:

```text
all legal candidates
    ├─ truthful
    └─ false
          ↓
neutral hypothetical epistemic evaluation
          ↓
hard consistency / contradiction gates
          ↓
productive-uncertainty quality model
          ↓
selection
          ↓
result naturally determines:
    - tell the truth, or
    - lie, and if so which lie
```

`truthRelation` remains important typed metadata. It is not the primary quality score.

## 3. Why a fixed truth/false probability is insufficient

A fixed probability does not know the current recipient knowledge state.

For example, an impaired Empath may legally receive truthful `0`, false `1`, or false `2`.

In one game state:

```text
0 truthful -> preserves several coherent but mistaken worlds
1 false    -> contradicts prior information and exposes impairment
2 false    -> conflicts with public facts
```

The correct Storyteller choice may be truthful `0`, not because a random truthful budget happened to win, but because truth is the most misleading coherent information at that moment.

In another game state:

```text
0 truthful -> strongly confirms the real evil structure
1 false    -> preserves several plausible mistaken worlds
2 false    -> legal but weak or temporally implausible
```

The correct choice may be false `1`.

The algorithm should make this distinction from epistemic consequences, not from a fixed family roll.

## 4. Existing ownership that remains valid

The current ownership direction remains sound:

```text
rules
  -> legal information shape / truthful semantics / registration semantics

recommendation
  -> legal typed candidates
  -> stable selection / recommendation presentation

session
  -> canonical game state
  -> GLOBAL semantic timeline
  -> durable observation commit authority

epistemic
  -> recipient-visible knowledge
  -> exact possible worlds
  -> historical replay
  -> hypothetical observation consequences

UI
  -> presentation and explicit user confirmation
```

Do not introduce a parallel legality system or a second candidate DTO.

`InformationDecisionContext<T>`, `DecisionCandidate`, `DecisionEvaluation`, `EpistemicObservationDraft`, session preflight, exact historical baseline and replay remain the foundation.

## 5. Current implementation interpretation

Current code has two separate levels:

```text
1. truthful-vs-false family probability
   -> ImpairedInformationPolicy

2. candidate ranking inside the chosen family
   -> MalfunctionPolicy / pressure / history / stable selector
```

This separation is useful as a fallback architecture, but the fixed family probability must not remain the final production authority once EPI-MQ is trusted.

The current policy should evolve toward:

```text
EPI-MQ READY
    -> unified truthful + false candidate evaluation and selection

EPI-MQ DEFERRED / unsupported semantics
    -> current ImpairedInformationPolicy + current heuristic ranking fallback
```

Thus existing policy code remains valuable as a compatibility and capability-degradation path.

## 6. Corrected implementation sequence

### EPI-MQ-0.5 — Capability boundary

Establish a generic capability/result contract for exact hypothetical evaluation.

Required semantics:

```text
READY
  -> exact evaluation available

DEFERRED / UNSUPPORTED_SEMANTICS
  -> required script/role epistemic semantics are not implemented
```

Invariant:

```text
DEFERRED != UNSAT
```

Do not implement all future dynamic-script semantics in this phase.

### EPI-MQ-1 — Neutral hypothetical observation evaluator

Extract the proven historical hypothetical-evaluation mechanics from B4-specific ownership into neutral `clocktower/epistemic` ownership.

Requirements:

- exact;
- deterministic;
- mutation-free;
- recipient-knowledge-safe;
- candidate observation consumed exactly once;
- BEFORE / AFTER diagnostics;
- hidden Storyteller state must not leak into recipient constraints;
- B4 becomes a consumer of the neutral evaluator rather than the authority EPI-MQ depends on.

### EPI-MQ-2 — Hard semantic gates

Add typed diagnostics for conditions that should not be hidden inside a scalar score.

Examples:

- exact contradiction / AFTER world set empty;
- immediate conflict with already public facts;
- strong impairment exposure;
- temporal inconsistency with previous recipient-visible information;
- unsupported evaluation -> DEFERRED, not contradiction.

Do not start by compressing every diagnostic into one arbitrary score.

### EPI-MQ-2.5 — Shadow integration

Run the new evaluator over **all legal impaired-information candidates, truthful and false**, while production selection still uses the existing policy.

Capture enough diagnostics to compare the old and new decisions, for example:

```text
candidate id
truth relation
old heuristic/family result
new epistemic diagnostics
BEFORE world count
AFTER world count
contradiction / impairment-exposure flags
new preferred candidate
actual production-selected candidate
```

This phase exists to validate the new decision model against simulation and real game behavior before changing live selection.

Shadow mode must already answer:

- would EPI-MQ tell the truth here?
- if not, which false candidate would it choose?

### EPI-MQ-3 — Unified information-quality model

Build the productive-uncertainty model across truthful and false candidates in one pool.

Candidate quality should be based on epistemic consequences, not simply truthfulness or falsehood.

Likely dimensions include:

```text
+ coherence
+ productive uncertainty
+ plausible mistaken worlds
+ temporal consistency
+ explanation diversity

- immediate contradiction
- impairment exposure
- confirmation lock
- excessive information destruction
```

World cardinality alone is insufficient. Two candidates can leave the same number of worlds but very different explanation quality.

The model must avoid the opposite failure mode of maximizing uncertainty forever. Useful information still needs to move the game forward; the goal is coherent, playable uncertainty, not maximum ignorance.

### EPI-MQ-4 — Unified impaired-information production cutover

After shadow validation, replace the current fixed truth/false family gate as the normal READY-path decision authority.

EPI-MQ then decides in one selection:

```text
- whether this impaired ability should tell the truth;
- if it should lie, which legal lie is best.
```

Preserve:

- candidate legality ownership;
- exact recipient-knowledge boundary;
- stable/deterministic selection requirements where applicable;
- auditability;
- existing fallback policy for DEFERRED capability cases.

The previous proposed phase name `False-family Selection Cutover` is superseded by **Unified impaired-information production cutover**.

### EPI-MQ-5 — Calibration and fallback refinement

Only after production-quality evidence exists:

- tune quality weights / thresholds;
- calibrate randomness or diversity if deterministic best-choice play becomes repetitive;
- decide whether some roles need role-specific policy hints;
- refine fallback behavior for partially supported dynamic scripts.

Do not reintroduce a global fixed truth/false percentage as the main decision rule unless evidence shows a specific need for bounded stochasticity.

## 7. Architectural dependency rule

Avoid this dependency cycle:

```text
recommendation -> session -> epistemic -> recommendation
```

The intended flow is:

```text
rules
  -> legal candidates

session/composition
  -> correctly bound hypothetical observation context

epistemic
  -> neutral candidate diagnostics

recommendation policy
  -> consume diagnostics
  -> select among legal candidates
```

The exact composition seam should be confirmed during EPI-MQ-1 fan-out audit rather than solved by making recommendation depend directly on B4 or concrete session internals.

## 8. Exact engine and dynamic-script rules

The earlier decisions remain unchanged:

- Trouble Brewing exact enumerated/historical machinery remains current correctness authority;
- A4/ZDD remains representation/shadow work until an explicit future cutover;
- ASP/Clingo remains cross-validation evidence;
- EPI-MQ must depend on generic epistemic capability, not concrete `TroubleBrewing...` classes;
- unsupported custom-script semantics return DEFERRED;
- future roles such as Pukka remain useful capability stress tests but are not prerequisites for EPI-MQ-0.5.

## 9. Hidden-information boundary

EPI-MQ may reason only from knowledge legitimately available to the recipient.

It must not directly constrain worlds with Storyteller-only facts such as:

- actual hidden roles beyond recipient knowledge;
- actual Poisoner/Pukka target;
- hidden protection/attack target;
- hidden transition cause before observable;
- Fortune Teller red-herring identity outside legal FT semantics;
- demon bluffs unknown to the recipient;
- recommendation seed or internal score metadata.

Historical replay must regenerate hidden mechanics from rules/per-world state rather than copying actual hidden Storyteller choices into every possible world.

## 10. Test strategy

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Durable new semantic seams should receive typed behavior tests. Pure extraction should use existing GREEN characterization where sufficient rather than manufacturing ritual RED tests.

Minimum future proof includes:

```text
supported Trouble Brewing -> READY
unsupported script/role semantics -> DEFERRED
DEFERRED != UNSAT
same history + same candidate -> deterministic diagnostics
live session remains unchanged during hypothetical evaluation
candidate observation applied exactly once
hidden target changes do not leak into recipient result
truthful and false candidates are both evaluated in shadow mode
unified model can legitimately prefer truth in one state and falsehood in another
```

At logical checkpoints run the epistemic/enumeration escalation defined by `docs/TESTING_STRATEGY.md`.

## 11. What is explicitly not part of the immediate phase

Do not mix the EPI-MQ restart with:

- Pair-display ADB/performance diagnosis;
- another UI redesign;
- full custom-script exact semantics;
- ZDD production correctness cutover;
- a new candidate legality model;
- hidden Storyteller-state shortcuts;
- replacement of all recommendation architecture at once.

## 12. Current execution order

The authoritative route is now:

```text
EPI-MQ-0.5  Capability Boundary
EPI-MQ-1    Neutral Hypothetical Evaluator
EPI-MQ-2    Hard Consistency / Exposure Gates
EPI-MQ-2.5  Truth+False Shadow Integration
EPI-MQ-3    Unified Productive-Uncertainty Quality Model
EPI-MQ-4    Unified Impaired-Information Production Cutover
EPI-MQ-5    Calibration / Fallback Refinement
```

The immediate implementation target is **EPI-MQ-0.5**.

## 13. Stable decision

> **The fixed truthful-vs-false probability policy is a temporary/fallback mechanism, not the final Storyteller intelligence. When exact EPI-MQ evaluation is READY, truthful and false legal candidates must be evaluated together so that the algorithm itself decides whether to tell the truth and, when lying is better, which lie to tell.**
