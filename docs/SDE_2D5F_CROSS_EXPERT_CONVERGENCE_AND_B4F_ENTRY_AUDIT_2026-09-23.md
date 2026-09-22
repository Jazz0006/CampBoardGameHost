# SDE-2D5F — Cross-expert convergence and bounded B4F entry audit

> Date: 2026-09-23 Australia/Sydney  
> Branch: `sde-2d5-calibration-policy-evidence`  
> PR: #150 — must remain draft

## 1. Purpose

This audit decides what the two independent admitted GOLD decision slices actually justify, and whether B4 should continue broad expert-source collection or begin bounded SILVER generalization.

The two independent GOLD Storyteller keys are:

- `st-ben-burns` — `A Stud In Scarlet`;
- `st-evin` — Evin's 2019 first full Trouble Brewing playthrough.

Primary-verified Ben material from `A Fond Farewell` is also usable as high-value rationale evidence, but it remains outside executable GOLD because production Traveller semantics are not implemented.

## 2. Evidence discipline

A GOLD case is not one indivisible vote for every policy dimension.

Only Storyteller-controlled decisions with reconstructable legal alternatives and relevant provenance can support preference inference.

Mechanical outputs must not be counted as expert preferences.

For the Evin case:

- Washerwoman pair = Storyteller-controlled;
- Red Herring seat = Storyteller-controlled;
- Demon bluff triplet = Storyteller-controlled, but no choice-specific rationale is known;
- Chef=1 = mechanically forced by the actual adjacent Evil pair;
- Fortune Teller YES = mechanically forced after the player-selected pair includes the committed Red Herring.

Therefore Chef and the later FT answer are trajectory/state evidence, not preference samples.

## 3. Strongest cross-expert convergence: Red Herring contextual utility

Two independent experienced Storytellers provide explicit Red-Herring rationale through different mechanisms.

### Ben / A Fond Farewell

Ben places the Red Herring on Undertaker and explicitly notes that Fortune Tellers often choose their neighbours.

Supported mechanism:

`PLAYER_CHOICE_LIKELIHOOD / TARGET_ECOLOGY`

### Evin / 2019 first playthrough

Evin places the Red Herring on Doug/Chef because Chef's information is especially damaging to Evil. A later Fortune Teller hit can make the table doubt Doug and his Chef information.

Supported mechanism:

`TRUTH_DANGER / CREDIBILITY_DISRUPTION`

### Cross-expert policy constraint

These are not the same heuristic and must not be collapsed into one fixed score.

They do support one higher-level policy invariant:

> Red Herring placement is a contextual precommit whose value comes from its likely downstream interaction with player choices, information credibility, and the whole first-night information ecology.

Allowed future features include:

- likelihood the Fortune Teller will select the seat;
- expected lifetime / opportunity to be checked;
- whether contaminating that seat meaningfully changes confidence in a high-impact information source;
- collision or complementarity with Recluse and other misinformation routes;
- confirmation-chain effects.

Not justified:

- fixed neighbour bonus;
- fixed “strongest information role” bonus;
- always target Chef;
- seat-specific rules;
- one scalar weight frozen from these two cases.

Maturity:

`CROSS_EXPERT_CONTEXTUAL_FOUNDATION`

## 4. Truth danger / credibility disruption

Evin supplies a primary GOLD choice-specific rationale: Chef's healthy information is dangerous to Evil, so Red-Herring contamination can reduce its credibility.

Independent external evidence already contains related high-impact-information interventions, including Storyteller decisions around dangerous Chef/Empath information and Evil seating.

This supports keeping `truthDanger` / `credibilityDisruption` as a first-class policy dimension.

It does **not** justify a deterministic “make strong information false” rule. Healthy information still needs a floor, and an impaired player may legally receive truthful information.

Maturity:

`GOLD_SINGLE_EXPERT_PLUS_REPEATED_EXTERNAL`

## 5. Impaired-information believability

Ben provides repeated primary rationale:

- A Stud: Drunk Empath 0 because 2 would be “a little unbelievable”;
- A Fond Farewell: Drunk Chef 4 as “a somewhat believable number”.

Qualitative evidence independently supports persistent believable counterworlds and avoiding arbitrary information oscillation.

However, the two explicit expert rationales still share `st-ben-burns`.

Current conclusion:

- preserve believable perceived-world / narrative continuity as a first-class policy dimension;
- do not freeze a numeric believability score or false-at-all-costs rule;
- independent GOLD evidence would strengthen this dimension but is not a reason to resume broad video collection immediately.

Maturity:

`SINGLE_EXPERT_EXPLICIT_PLUS_REPEATED_QUALITATIVE`

## 6. Washerwoman pair selection

Evin's observed Washerwoman clue is:

`Julian(Imp) / Filip(Undertaker) = Undertaker`

This is a genuine Storyteller-controlled legal truthful pair and happens to include the Demon as the decoy.

There is no recovered choice-specific rationale.

Therefore it is valid observed-choice evidence but does not justify:

- “prefer the Demon as decoy”;
- “avoid the Demon as decoy”;
- a seat-class bonus;
- a role-strength rule.

Maturity:

`GOLD_OBSERVATION_NO_PREFERENCE_GENERALIZATION`

## 7. Demon bluff selection

Evin's primary image verifies:

`Recluse / Slayer / Soldier`

This makes the case usable as one authentic expert bluff-triplet sample.

There is no recovered rationale and no cross-expert comparable GOLD triplet with independent Storyteller key.

The current architecture remains appropriate:

- legality is setup-owned;
- SDE treats the triplet as a joint output;
- candidate features may describe claim burden, narrative-route diversity, role collision, and support;
- no role-specific preference is frozen.

Maturity:

`GOLD_OBSERVATION_ONLY`

## 8. Bounded SILVER generalization

B4F may now begin, but only for dimensions that already have a GOLD-derived or explicit-primary hypothesis.

### 8.1 Red Herring trajectory

Existing SILVER ClockTracker evidence includes:

- `ct-04`: repeated Fortune Teller hits on the Red Herring produce repeated YES results;
- `ct-01`: a structured first-night bundle includes a committed Red Herring alongside several other misinformation / information channels.

These records support the proposition that Red Herring is a persistent downstream information route whose consequences interact with the whole bundle.

They do **not** reveal why those particular seats were selected, so they cannot validate a candidate-ranking heuristic.

B4F result:

`GENERALIZES_DOWNSTREAM_IMPORTANCE_NOT_SELECTION_ORDERING`

### 8.2 Truth danger

`ct-02` records a Storyteller reacting to Evil sitting in a row and considering Drunk Chef versus Drunk Investigator. This is consistent with the broader truth-danger / Evil-topology-coupling dimension.

Because Storyteller expertise is not independently verified at GOLD level and the mechanism differs from Red Herring placement, use it as generalization evidence only.

B4F result:

`CONSISTENT_WITH_TRUTH_DANGER_DIMENSION`

## 9. What remains under-evidenced

Do not enter D5F-C yet.

The following remain insufficiently supported for gate/band derivation:

- numeric healthy-information floor;
- severity/order of role-function exposure costs;
- Demon-bluff triplet preference ordering;
- independent-expert calibration of persistent impaired-information believability;
- quantitative tradeoff between topology retention, confirmation chains, information utility, and credibility disruption.

The current evidence is enough to define **dimensions and ownership**, not enough to freeze weights or thresholds.

## 10. Execution decision

Broad GOLD source discovery is no longer the default next action.

From this checkpoint:

1. keep source discovery demand-driven;
2. begin bounded B4F SILVER generalization for already-established dimensions;
3. verify another primary video only when it fills a named evidence gap;
4. do not collect additional Ben videos merely to increase sample count under the same independence key;
5. keep D5F-C and SDE-3 blocked;
6. do not implement Traveller support inside B4.

This moves B4 from “collect more expert videos by default” to “test explicit hypotheses against structured external evidence, and collect new primary evidence only when a specific unresolved dimension needs it.”
