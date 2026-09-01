# Epistemic Misinformation Quality & Productive Uncertainty — Design Plan

> Date: 2026-09-01 Australia/Sydney  
> Status: **DESIGN / DEFERRED — DO NOT IMPLEMENT IN PR #61**  
> Related foundation: `CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> Current prerequisite: complete MS-S6D semantic authority and close the current MS-SETUP PR first.

## 1. Problem statement

For a Drunk or Poisoned information ability, the objective should not be:

> generate a random answer that is not true.

The objective should be:

> create a reasonable, sustainable and interactive mistaken world that supports genuine player reasoning, remains discoverable over time, and does not let the Storyteller directly decide the game.

This raises the algorithmic question from:

> **Is this answer false?**

into:

> **What mistaken world does this answer create, and is that world credible, interactive, sustainable, eventually breakable and fair?**

This is an epistemic/cognitive-consistency problem, not merely a role-rule truth-classification problem.

## 2. Separation from current S6D

Current S6D owns semantic correctness:

```text
perceived ability
-> healthy legal/truth semantic space
-> reliability state
-> generic selector
```

This future stage owns quality/ranking **inside the already legal candidate space**:

```text
legal semantic candidate
-> hypothetical player-visible observation
-> resulting perceived worlds
-> misinformation-world quality evaluation
-> ranking / storyteller policy
```

Therefore:

- S6D must finish first;
- PR #61 must not grow a large false-world strategy engine;
- current recommendation policy remains temporarily in place except for semantic correctness/authority fixes;
- this design resumes on a separate post-PR branch/campaign built on the cognitive-consistency architecture.

## 3. Core design principles

### 3.1 Credibility

False information should look plausible under the current visible game state. Avoid candidates that immediately contradict strong public facts or are trivially falsified.

### 3.2 Mistaken-world persistence

Prefer information that can sustain a coherent alternative explanation across later discussion and observations rather than collapsing immediately after the first identity exchange.

Persistence is not the same as permanence. A good mistaken world survives long enough to create play, not forever.

### 3.3 Breakability

The mistaken world must retain plausible discovery paths. Later claims, public events, executions, deaths, ability results and registration explanations should eventually allow players to revise or reject it.

A candidate that is impossible to disprove is generally lower quality than one that supports meaningful investigation.

### 3.4 Cross-role interaction value

Prefer candidates that interact productively with other information roles, identity claims, Demon bluffs, Outsider counts, registration mechanics and public events.

Isolated random falsehoods usually provide less game value than falsehoods that participate in the information network.

### 3.5 Social impact

Different roles affect the social game differently:

- Washerwoman commonly establishes trust;
- Investigator commonly creates suspicion;
- Librarian changes Outsider/configuration worlds;
- Chef/Empath influence alignment/location theories;
- Fortune Teller influences Demon suspicion and repeated testing.

The quality evaluator should eventually model the *social consequence* of a candidate, not just its formal truth status.

### 3.6 Productive Uncertainty / multiple reasonable explanations

A strong candidate should often preserve several plausible explanations at once, for example:

- the information is genuinely true;
- the information source is Drunk or Poisoned;
- Spy/Recluse registration explains the anomaly;
- a named player is bluffing;
- a configuration or identity assumption is wrong.

The goal is not maximum entropy for its own sake. It is **Productive Uncertainty**: enough ambiguity to support reasoning and interaction without making reasoning meaningless.

### 3.7 Avoid direct Drunk exposure

Obviously absurd or immediately contradictory information can become free confirmation that the player is Drunk/Poisoned. That is usually poor information unless a later explicit Storyteller policy intentionally accepts that consequence.

### 3.8 Avoid confirmation locks

False information should not create an excessively strong mutual-confirmation chain that gives an evil player near-irreversible good certification or gives a good player an almost impossible-to-rebut false conviction.

The algorithm should detect when one candidate collapses too many worlds around a socially decisive but false conclusion.

### 3.9 Narrative value over degree of falsity

“Further from the truth” is not inherently better.

A partially wrong candidate can create more play than a maximally wrong candidate. Examples include:

- correct role but one wrong named player;
- plausible role with shifted location;
- false identity linked to a real configuration tension;
- numeric information one step away from truth but highly interactive with claims.

Existing `misinformationPressure` / distance-from-truth style signals should eventually become features, not the primary objective function.

### 3.10 Player agency and fairness

The Storyteller algorithm should create inference space, not manufacture a decisive faction advantage.

Players must retain meaningful ways to influence the result through discussion, claims, logic, public abilities, risk-taking and revising assumptions.

## 4. Proposed evaluation architecture

The long-term pipeline should become:

```text
actual game state
+ player-visible history
+ recipient knowledge state
+ perceived ability semantic domain

-> candidate legal observation
-> hypothetical apply observation
-> recipient PlayerWorldSet AFTER
-> cross-player / cross-role interaction projection
-> quality features
-> safety/fairness gates
-> Productive Uncertainty score / Pareto ranking
-> storyteller policy
-> generic selector
-> committed AbilityObservation
```

The exact world representation can evolve, but candidate semantics must remain independent of the quality layer.

## 5. Candidate quality feature families

Do not freeze exact weights yet. First establish measurable behavior and diagnostic output.

### A. Plausibility / credibility

Possible measures:

- surviving player worlds after candidate observation;
- contradiction count against durable visible observations;
- whether candidate requires one ordinary explanation or several low-probability exceptions;
- immediate public-fact conflicts;
- dependence on hidden Storyteller-only facts that the recipient could not reason about.

### B. Persistence

Possible measures:

- how many likely near-future observations would immediately eliminate the candidate world;
- consistency with already established claims/history;
- temporal continuity for repeated information roles;
- whether later poison expiry naturally provides a fair discrepancy signal.

Drunk and Poisoned may eventually have different persistence weights even though they share the same underlying role semantics.

### C. Breakability / discovery path

Possible measures:

- number and accessibility of future player-visible events capable of distinguishing the false world from the actual world;
- whether public events can expose contradictions;
- whether discovery requires impossible hidden information;
- estimated reasoning depth before the world becomes distinguishable.

### D. Interaction value

Possible measures:

- overlap with other information-role propositions;
- interaction with declared identities / bluffs;
- Outsider-count tension;
- Spy/Recluse alternative explanations;
- ability to motivate meaningful nominations, tests, private conversations or claim comparison.

### E. Productive uncertainty

Possible measures:

- number/diversity of surviving plausible world families;
- distribution across explanations such as true / impaired / registration / bluff / configuration alternatives;
- avoidance of both near-zero ambiguity and unmanageably diffuse ambiguity.

This may be better modeled as a preferred range or Pareto objective than a simple “more worlds is better” score.

### F. Confirmation-lock risk

Possible measures:

- whether the candidate creates reciprocal corroboration between two claims;
- whether an evil bluff becomes uniquely favored;
- whether a good player becomes uniquely condemned;
- how difficult later public evidence would be to overcome socially/epistemically.

### G. Faction impact / fairness

Possible measures:

- estimated shift in surviving good-vs-evil world mass from the recipient perspective;
- whether one faction receives an unusually direct structural advantage;
- whether the candidate acts more like a Storyteller verdict than information ambiguity.

Do not optimize to equalize every candidate. The objective is to avoid extreme Storyteller-created locks while preserving legitimate game asymmetry.

## 6. Role-family strategy implications

The quality layer should remain generic where possible, but role families require different interpretation of the same metrics.

### Pair roles — Washerwoman / Librarian / Investigator

Important dimensions:

- which of the two named players carries the false implication;
- whether shown role is in play, bluffable, registered, or configuration-relevant;
- trust vs suspicion consequences;
- whether one named player can reasonably challenge the clue;
- whether the candidate creates a too-strong two-player confirmation lock.

“Correct role + strategically wrong pair member” may often be higher quality than a completely disconnected role/pair, but this is a hypothesis to evaluate, not a hard rule.

### Numeric roles — Chef / Empath

Numeric distance alone is insufficient.

Evaluate which alignment/location worlds each number supports. Values equally distant from truth can have very different consequences for neighbor suspicion, evil-pair theories and public claims.

### Fortune Teller

Repeated queries make temporal consistency especially important. Candidate quality should consider prior query history, red-herring explanations, target repetition and later discovery paths rather than scoring each YES/NO in isolation.

## 7. Drunk vs Poisoned strategy layer

Semantic role truth remains shared. Quality policy may later differ.

### Drunk

- mistaken identity is durable;
- long-lived coherent false worlds can be valuable;
- immediate self-diagnosis should generally be avoided;
- information across days should not accidentally oscillate in a way that makes Drunk status obvious without reason.

### Poisoned

- impairment may be temporary;
- before/after continuity is itself player-visible evidence;
- a false answer can be judged partly by how fairly later healthy information allows the player to detect the disruption;
- the algorithm may favor misinformation that remains explainable but not permanently coherent after poison expires.

These are future quality-policy differences. They must **not** fork the underlying role semantic evaluator.

## 8. Integration with cognitive-consistency architecture

This stage should reuse, rather than bypass, existing epistemic foundations such as:

```text
PlayerKnowledgeSnapshot
PlayerHistoricalTimeline
EnumeratedWorldSet / exact enumeration
EnumeratedHistoricalWorldReplay
AbilityObservation
registration facts / semantic observations
```

Conceptual flow:

```text
candidate misinformation
-> encode as hypothetical visible AbilityObservation
-> replay recipient-visible history
-> compute surviving perceived worlds
-> compare BEFORE vs AFTER
-> derive quality metrics
```

Never inject actual Storyteller-hidden action targets into the recipient's knowledge model merely to make scoring convenient.

## 9. Exact baseline and future scalability

Use exact A3 enumeration as the correctness oracle for early tests and small Trouble Brewing states.

A4/ZDD or other compressed representations may later accelerate evaluation, but must remain shadow until equivalence/resource behavior is proven. Timeouts/resource exhaustion must be represented as unknown/degraded evaluation, never false UNSAT.

The quality engine should be designed so the world backend can be replaced without changing role semantics or the high-level quality contract.

## 10. Suggested implementation campaign

Do not start until the current PR is merged and a fresh branch is created.

### EPI-MQ-0 — design audit / behavior corpus

- convert these principles into a small set of concrete TB scenarios;
- identify good / acceptable / poor misinformation examples;
- avoid freezing subjective exact weights too early.

### EPI-MQ-1 — hypothetical observation evaluation seam

- candidate -> temporary observation;
- recipient world set BEFORE/AFTER;
- no production ranking yet.

### EPI-MQ-2 — credibility + immediate contradiction gates

- reject obviously self-exposing / publicly impossible misinformation where appropriate;
- retain multiple reasonable explanation paths.

### EPI-MQ-3 — persistence + breakability metrics

- use historical timeline and plausible future distinguishing events;
- start with first-night pair roles.

### EPI-MQ-4 — interaction / confirmation-lock metrics

- cross-role proposition interaction;
- trust/suspicion and mutual-confirmation risk.

### EPI-MQ-5 — Productive Uncertainty ranking

- combine features through transparent policy or Pareto ranking;
- diagnostics must explain *why* a candidate ranked well/poorly.

### EPI-MQ-6 — Drunk vs Poisoned policy tuning

- shared semantics;
- separate strategic weighting only where behavior evidence supports it.

### EPI-MQ-7 — shadow production evaluation

- compare current recommendation vs new ranking without changing user-visible output;
- collect scenario/regression evidence.

### EPI-MQ-8 — controlled rollout

- only after exact correctness, quality corpus, performance and distribution gates pass.

## 11. Testing philosophy

This domain is too strategic for brittle implementation tests.

Prefer:

- scenario-level behavior tests;
- monotonic relations such as “candidate A should be rejected when it immediately contradicts a public fact”;
- invariants such as “quality scoring cannot mutate committed shown identity”;
- comparative expectations where robust, e.g. “a plausible breakable false world outranks an immediately impossible one”;
- exact-world oracle cross-validation for small states;
- distribution tests only after deterministic semantic correctness is established.

Avoid hard-coding a large table of exact numeric weights as the main contract.

## 12. Non-goals for the first campaign

Do not initially attempt:

- general human psychology simulation;
- full conversational NLP truth detection;
- perfect prediction of player claims;
- omniscient game-theoretic win-rate optimization;
- Storyteller control that directly maximizes one faction's expected win probability;
- broad all-script rollout before Trouble Brewing behavior is understood.

The first goal is narrower: **make false information create better reasoning worlds than random/locally-scored wrong answers while preserving fairness and explainability.**

## 13. Acceptance philosophy

A future misinformation-quality rollout is successful when the system can explain, for a proposed false clue:

1. what player-visible worlds it creates or preserves;
2. why those worlds are currently credible;
3. what other information/claims they interact with;
4. why the clue does not immediately expose impairment;
5. why it does not create an excessive confirmation lock;
6. how players could later discover or challenge it;
7. why the resulting ambiguity remains fair and useful.

That explanation is more important than proving the selected clue is simply “farther from the truth.”
