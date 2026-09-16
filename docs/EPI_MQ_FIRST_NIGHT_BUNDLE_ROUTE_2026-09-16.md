# EPI-MQ Route Decision — First-Night Bundle Badness Filtering

> Date: 2026-09-16 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT ROUTE DECISION**  
> Supersedes as execution authority: `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`

## 1. Product decision

The recommendation engine will **not** try to identify one mathematically optimal Storyteller clue.

The target behavior is:

> **Generate all rules-legal information choices, reject choices that are clearly poor for the intended recipient/table skill profile, then select randomly from the remaining acceptable candidates.**

For the first implementation and experiment phase, the only supported difficulty profile is **BEGINNER**.

General-purpose LLM recommendation is explicitly deferred until the deterministic/evidence-driven algorithm has been implemented and validated.

Conceptually:

```text
legal FirstNightInformationBundles
        ↓
exact / capability-aware epistemic evaluation
        ↓
interpretable Badness diagnostics
        ↓
BEGINNER Badness Gates
        ↓
acceptable bundle pool
        ↓
uniform random selection
```

Do not reintroduce a single opaque scalar score whose maximum is treated as the best Storyteller choice.

## 2. Main experimental unit: complete first-night information bundle

The primary experiment and future selection unit is **not one Investigator clue in isolation**.

It is the complete first-night information ecology for a fixed real setup and seating arrangement:

```text
rule-determined observations
+ Storyteller-controlled legal choices
+ relevant impairment / registration semantics
= FirstNightInformationBundle
```

Examples of rule-determined observations include healthy Chef and Empath results determined by the actual seating structure.

Examples of Storyteller-controlled choices include, where applicable:

- Investigator pair / shown Minion information;
- Washerwoman pair / shown Townsfolk information;
- Librarian pair / shown Outsider information or legal zero-Outsider result;
- Fortune Teller Red Herring;
- Drunk shown role and legal false information;
- demon bluffs;
- legal registration choices where rules permit Storyteller discretion.

Fortune Teller nightly target selection remains player-controlled and must not be treated as a Storyteller first-night choice.

## 3. Why bundle-level evaluation is mandatory

Single clues can look acceptable while becoming destructive when combined.

The evaluator must detect interactions such as:

```text
Washerwoman supports Empath identity
        +
Empath 0 clears both neighbours
        +
Investigator / other information clears another region
        ↓
large trusted-good block / tiny evil search region
```

Therefore final Badness decisions must be based on the consequences of the **whole public information structure**, not independent per-role scores summed together.

Role-level experiments still exist, but only as lower-level correctness/diagnostic tests for the evaluator.

## 4. Public-share stress model

The first experiment profile will use a deliberate stress assumption:

> Assume healthy-good players publicly reveal their first-night information on Day 1.

This is a **behavioral evaluation profile**, not a rules claim and not a permanent assumption about all tables.

The purpose is to detect first-night bundles that allow the true world structure to become too dominant when information is shared aggressively.

Later profiles may model different table behaviors, but they are out of scope for the first experiment.

## 5. Initial Badness dimensions

The first experiment must measure interpretable diagnostics before defining final thresholds.

### 5.1 Strategically distinct evil topology

Raw world count is insufficient.

Measure at least:

- distinct current demon-seat possibilities;
- distinct evil-team seat configurations;
- evil-team topology retention relative to the BEFORE state.

Many surviving worlds that differ only in good-role permutations do not provide meaningful strategic ambiguity.

### 5.2 Forced-good / forced-evil structure

Measure seats that are logically good or evil across all surviving exact hypotheses under the selected evaluation profile.

Diagnostics should include:

- `forcedGoodSeats` / count;
- `forcedEvilSeats` / count where meaningful;
- current demon-seat support/diversity.

Do not call unweighted world fractions posterior probabilities unless an explicit prior/weighting model is introduced later.

### 5.3 Evil cover

Measure how many seats are still required to cover all plausible current evil placements.

Useful diagnostics include:

- all-evil `evilCoverSize`;
- current-demon `demonCoverSize`.

A tiny suspect region can make a game mechanically solvable even when the full world count remains high.

### 5.4 Confirmation chains / interaction collapse

Detect combinations where clues jointly collapse the hypothesis space much more than any one clue does alone.

The first explainable implementation should prefer counterfactual removal diagnostics:

```text
all clues -> W_all
remove clue A -> W_-A
remove clue B -> W_-B
```

and pair/leave-one-out interaction evidence before introducing opaque graph scores.

A support/exclusion graph may be added later for explanation, but exact hypothesis consequences remain the ground truth where supported.

### 5.5 Minimum information value

A clue/bundle can also be poor because it is nearly useless.

Do not optimize for maximum uncertainty. Measure whether the bundle meaningfully changes strategically relevant hypotheses while avoiding destructive over-convergence.

The acceptable region is between:

```text
too weak  -> reject
acceptable
 too strong -> reject
```

### 5.6 Counterworld viability

Where tractable, measure whether several structurally distinct alternative worlds remain coherent enough to support evil bluffing rather than leaving only the true evil topology plus cosmetic good-role permutations.

Initial exact diagnostics should remain conservative and explainable. Do not invent a complex scalar `counterworldScore` before experiments show what representation is useful.

## 6. Beginner-first skill adaptation

The long-term product should adapt information difficulty to the **recipient/table experience profile**.

A clue that is helpful and appropriately clear for a beginner can be too revealing for an experienced player.

Long-term architecture should therefore permit conceptually:

```text
evaluate(gameState, recipientId, candidateObservation, difficultyProfile)
```

or bundle-level equivalent with recipient-specific experience inputs.

However, the first experiment deliberately supports only:

```text
BEGINNER
```

Do not implement Beginner/Intermediate/Experienced/Expert thresholds yet.

First establish whether the selected diagnostics can distinguish obviously poor from acceptable first-night bundles for beginners.

## 7. Drunk and other uncertainty sources

The experiment must not permanently assume every good player is healthy and truthful.

A staged route is required:

### Experiment 0 — evaluator correctness

Small typed fixtures for individual roles/observations:

- Investigator;
- Washerwoman;
- Librarian;
- Empath;
- Chef;
- representative Drunk false information.

Purpose: prove exact BEFORE/AFTER semantics, not final recommendation quality.

### Experiment 1 — complete healthy first night

Use fixed 7-player Trouble Brewing setups and seat layouts.

Evaluate complete first-night bundles containing all rule-determined healthy observations plus all relevant Storyteller-controlled legal choices.

This is the first real Badness experiment.

### Experiment 2 — Drunk

Add Drunk shown-role and false-information choices so the evaluator can observe how a coherent mistaken world changes confirmation chains and evil topology.

### Experiment 3 — Spy / Recluse registration

Add registration ambiguity and use Investigator+Recluse setups as important stress fixtures.

### Experiment 4 — Poisoner

Add first-night Poisoner target choice after the simpler bundle model is validated, because it expands the candidate state space and introduces dynamic impairment.

## 8. Experimental method before thresholds

Do **not** guess final thresholds first.

For each fixed setup / seating layout:

1. construct the exact BEFORE hypothesis state for the selected public-share profile;
2. generate complete legal first-night bundles;
3. evaluate each bundle and collect diagnostics;
4. inspect extreme and representative bundles;
5. manually label a manageable corpus, initially:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

6. test which diagnostics actually separate bad from acceptable bundles;
7. only then define first BEGINNER Badness Gates.

The existing 7-player Trouble Brewing presets with a real Investigator and no Recluse are useful stress fixtures, but the primary experiment must include the complete first-night information structure rather than Investigator in isolation.

## 9. Selection semantics

After Badness Gates are validated, initial production selection should be deliberately simple:

```text
Hard legality
    ↓
Badness filtering
    ↓
uniform random among survivors
```

Do not add soft weighting in the first cutover.

If later evidence shows that some acceptable bundles are systematically preferable without becoming mandatory, a separate bounded soft-weight layer may be added:

```text
Hard legality
→ Badness gates
→ optional soft preference
→ random selection
```

The hard Badness layer must remain independently inspectable.

## 10. Relationship to existing EPI-MQ infrastructure

The existing capability and neutral-evaluator work remains valuable foundation.

Keep/reuse:

- `EpistemicEvaluationCapabilityBoundary` and `READY / DEFERRED` semantics;
- exact recipient-visible possible-world reasoning;
- historical replay;
- mutation-free hypothetical observation evaluation;
- apply-once semantics;
- hidden-information boundaries;
- legal candidate ownership in rules/recommendation seams.

But the previous product route is changed:

- **do not make a unified scalar productive-uncertainty score the immediate target;**
- **do not make impaired truth-vs-false cutover the immediate product priority;**
- **do not optimize a single candidate independently of the rest of Night 1.**

The next implementation work should use the neutral evaluator only to the extent required to support the first-night bundle experiment cleanly.

## 11. LLM decision

General-purpose LLM recommendation is deferred.

Current research indicates a useful future role for an LLM as a soft critic over already legal, already Badness-qualified candidates, especially for hard-to-formalize human-play concerns.

It is not an authority for:

- legality;
- exact rules;
- possible-world consequences;
- Badness Gates;
- initial production selection.

Do not integrate an LLM until the deterministic bundle evaluator and BEGINNER Badness experiment have produced a validated baseline that allows incremental value to be measured.

## 12. External calibration data

ClockTracker and expert Storyteller game records are promising calibration sources, not direct imitation targets.

Use them later to answer questions such as:

- do experienced Storytellers routinely avoid the bundles our gates reject?
- are our thresholds too strict or too permissive?
- where do real expert choices fall inside the acceptable pool?

Do not treat a recorded Storyteller choice as automatically correct merely because it occurred in a real game.

## 13. Architecture rule

Preserve ownership:

```text
rules
  -> legal information semantics / registration legality

session
  -> canonical game state / timeline / hidden actual state

epistemic
  -> recipient-visible hypotheses / exact consequence diagnostics

recommendation
  -> compose legal first-night bundles
  -> apply Badness policy
  -> random selection among acceptable bundles

UI
  -> show recommendations / confirmation / player-experience configuration
```

Badness policy consumes epistemic diagnostics; it must not become a second rules engine.

## 14. Immediate next phase

The next conversation should **not** begin by tuning thresholds or integrating LLMs.

It should:

1. audit current production seams required to represent a complete `FirstNightInformationBundle`;
2. identify which Night 1 observations are fixed by rules/state and which are Storyteller-controlled candidates;
3. define the smallest experiment-only typed bundle representation;
4. confirm exact/capability-aware evaluation can evaluate the complete public-share bundle without hidden-state leakage or double application;
5. implement Experiment 0 correctness fixtures only where needed;
6. implement Experiment 1 harness for complete healthy 7-player Trouble Brewing first nights;
7. emit interpretable diagnostics, without final thresholds;
8. manually inspect/label the first corpus before introducing BEGINNER gates.

## 15. Stable decision

> **The Storyteller recommendation algorithm does not search for a unique optimal clue. It evaluates complete legal first-night information bundles, rejects bundles that are demonstrably poor for the intended skill profile using interpretable epistemic Badness criteria, and then selects randomly from the acceptable pool. The first implementation targets beginners, evaluates the whole first-night information ecology rather than Investigator alone, and defers general-purpose LLM advice until the deterministic algorithm has been validated.**
