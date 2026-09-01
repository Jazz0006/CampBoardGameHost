# Epistemic Misinformation Quality & Productive Uncertainty — Design Plan

> Date: 2026-09-01 Australia/Sydney  
> Status: **APPROVED NEXT PRIMARY ALGORITHM CAMPAIGN — START AFTER STABLE INFORMATION-DECISION / MANUAL UX BOUNDARY**  
> Foundational design: `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`  
> Current execution sequencing: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`

## 1. Problem statement

For a Drunk or Poisoned information ability, the objective is not:

> generate a random answer that is not true.

The objective is:

> create a reasonable, sustainable and interactive mistaken world that supports genuine player reasoning, remains discoverable over time, and does not let the Storyteller directly decide the game.

The algorithmic question is therefore:

> **What mistaken world does this legal candidate create, and is that world credible, interactive, sustainable, eventually breakable and fair?**

This is an epistemic/cognitive-consistency problem layered **after** rules legality and semantic truth classification.

## 2. Prerequisites and boundary

The old prerequisite “finish PR #61 first” is complete and historical.

The current prerequisites are narrower and product-oriented:

1. semantic legality/truth remains upstream and independent of recommendation quality;
2. manual selection and recommendation acceptance share one stable candidate/decision authority;
3. every currently supported major clue family remains manually playable without recommendation coverage;
4. the product has a provider-neutral recommendation shell that can display primary + 0–2 alternatives + manual control without depending on Balanced/Aggressive/Conservative as front-door modes.

Do **not** wait for a perfect UI redesign before starting this campaign. Once the durable decision/manual/recommendation boundary exists, algorithm work becomes the mainline.

Do **not** build a second temporary recommendation engine merely to improve the legacy shell before Productive Uncertainty.

## 3. Core causal pipeline

Permanent separation:

```text
actual game state
+ perceived ability
+ interaction-scoped registration semantics
        ↓
complete legal semantic candidate domain
        ↓
shared information-decision authority
        ↓
manual path OR recommendation provider
        ↓
confirmed candidate
        ↓
player-visible AbilityObservation / durable history
```

Productive Uncertainty is a provider/ranking layer inside that boundary:

```text
legal candidate
-> hypothetical player-visible observation
-> recipient PlayerWorldSet BEFORE / AFTER
-> epistemic and structural quality features
-> hard safety/fairness gates
-> Productive Uncertainty tier / ranking
-> RecommendationResult
```

It must never redefine the legal candidate set.

## 4. Core quality principles

### 4.1 Credibility

Prefer misinformation that is plausible under the recipient's current visible state. Penalize or reject candidates that immediately contradict strong public facts or require implausible hidden exceptions merely to survive.

### 4.2 Mistaken-world persistence

A good mistaken world should survive long enough to create meaningful play, discussion and reasoning.

Persistence is not permanence. The goal is not to make a false world impossible to escape.

### 4.3 Breakability

Players should retain realistic future paths to discover or challenge the mistaken world through later information, claims, executions, deaths, public abilities, poison expiry or registration explanations.

A false world that can only be disproved using hidden Storyteller knowledge is poor quality.

### 4.4 Cross-role interaction

Prefer clues that participate meaningfully in the existing information network:

- role claims and Demon bluffs;
- Outsider-count/configuration theories;
- other information-role observations;
- Spy/Recluse registration explanations;
- nominations, tests and future verification paths.

Disconnected falsehoods generally have less game value.

### 4.5 Productive uncertainty

A strong clue often preserves several reasonable explanations simultaneously, for example:

- the clue is true;
- the source is Drunk or Poisoned;
- Spy/Recluse registration explains the anomaly;
- a player claim is false;
- a setup/configuration assumption is wrong.

The target is a useful range of uncertainty, not maximum entropy.

### 4.6 Avoid impairment self-exposure

Obviously absurd or immediately contradictory misinformation can become free confirmation that the source is Drunk/Poisoned. This should normally be rejected or heavily penalized unless an explicit future Storyteller policy intentionally accepts that consequence.

### 4.7 Avoid confirmation locks

A clue should not create an excessively strong false mutual-confirmation chain or an almost irreversible false conviction that later evidence cannot reasonably overcome.

### 4.8 Narrative value over degree of falsity

“Further from truth” is not inherently better.

Often a partially wrong but coherent clue creates more play than a maximally wrong disconnected clue.

Existing `misinformationPressure`, distance-from-truth or former style signals should become features/diagnostics, not the primary objective.

### 4.9 Player agency and fairness

The Storyteller algorithm should create inference space, not manufacture a decisive faction verdict.

Players must retain meaningful ways to influence outcomes through discussion, claims, logic, public abilities, risk-taking and revision of assumptions.

## 5. Correct epistemic evaluation seam

The first implementation seam is deliberately ranking-free:

```text
recipient PlayerKnowledgeSnapshot
        ↓
PlayerWorldSet BEFORE
        +
hypothetical legal visible observation
        ↓
PlayerWorldSet AFTER
        ↓
metrics / diagnostics
```

Candidate simulation must use only information the recipient could legitimately observe/reason from.

Never inject actual Storyteller-hidden action targets, hidden setup decisions, poison targets, Demon bluffs or private registration choices into the recipient's world model merely to make scoring easier.

## 6. Feature families

Do not freeze exact weights before behavior evidence exists.

### A. Credibility / contradiction

Possible measures:

- surviving perceived worlds after the observation;
- direct contradiction with durable visible observations;
- dependence on extraordinary exceptions;
- conflict with public facts;
- immediate self-exposure of impairment.

### B. Persistence

Possible measures:

- how quickly likely near-future observations collapse the mistaken world;
- consistency with prior private/public information;
- temporal continuity for repeated information roles;
- whether later recovery from Poison naturally creates fair discrepancy evidence.

### C. Breakability / discovery paths

Possible measures:

- number and accessibility of future player-visible events that distinguish the false world from actual state;
- public events capable of exposing contradictions;
- whether discovery requires hidden information;
- estimated reasoning depth before the world becomes distinguishable.

### D. Interaction value

Possible measures:

- overlap/tension with other information propositions;
- role claims / Demon bluff interactions;
- Outsider-count pressure;
- Spy/Recluse explanation value;
- ability to motivate meaningful nominations, tests, private conversations or claim comparison.

### E. Productive uncertainty

Possible measures:

- number and diversity of surviving world families;
- distribution across true / impaired / registration / bluff / configuration explanations;
- avoidance of both near-zero ambiguity and unusably diffuse ambiguity.

This is likely better represented as preferred ranges, tiers or Pareto objectives than “more worlds is always better”.

### F. Confirmation-lock risk

Possible measures:

- reciprocal corroboration between claims;
- whether an evil bluff becomes uniquely certified;
- whether a good player becomes uniquely condemned;
- how difficult later public evidence would be to overcome.

### G. Faction impact / fairness

Possible measures:

- large shifts in surviving good-vs-evil world structure;
- Storyteller-created structural advantage;
- whether a candidate behaves more like a verdict than ambiguous information.

The objective is not to equalize every candidate; it is to avoid extreme Storyteller-created locks while preserving legitimate asymmetry.

## 7. Pair-role behavior corpus first

Start with Trouble Brewing pair-information roles because their legal domain is explicit and their social effects differ strongly.

The initial corpus should include approximately 15–30 concrete scenario judgments labeled with broad quality such as:

```text
GOOD
ACCEPTABLE
POOR
```

plus reasons, rather than exact target scores.

### Washerwoman hypotheses

Compare examples such as:

- real shown role + shifted pair member;
- real shown role + entirely wrong pair;
- absent but Demon-bluff/configuration-relevant Townsfolk role;
- Spy-registration-compatible world;
- role/pair that creates useful cross-role tension;
- disconnected fake role/pair;
- clue that gives an evil bluff excessive good certification.

“Correct role + strategically wrong pair member” may often be good, but this remains a hypothesis to test, not a hard-coded rule.

### Librarian hypotheses

Compare:

- plausible false Outsider world;
- false Outsider that interacts with configuration count;
- bluff-linked Outsider theory;
- Spy registration explanation;
- `No Outsiders` when impaired;
- clues that collapse configuration worlds too aggressively;
- clues that immediately reveal impairment.

### Investigator hypotheses

Compare:

- plausible suspicion spread across two players;
- Recluse registration explanation;
- suspicion that creates future tests;
- false implication that effectively condemns one player with no breakability;
- role/pair that interacts with existing claims without creating a hard lock.

## 8. Numeric and repeated-information implications

### Numeric roles

Numeric distance alone is insufficient.

For Chef/Empath and similar roles, evaluate which alignment/location worlds each value supports. Equally distant numeric values can have very different social consequences.

### Fortune Teller / repeated Yes-No information

Temporal consistency is especially important.

Quality should consider prior query history, red-herring explanations, target repetition, future re-testing and discovery paths rather than scoring each Yes/No independently.

## 9. Drunk vs Poisoned

Semantic role rules remain shared; ranking policy may differ.

### Drunk

- mistaken shown identity is durable;
- coherent longer-lived false worlds may be valuable;
- avoid accidental immediate self-diagnosis;
- multi-night misinformation should not oscillate incoherently without a reason.

### Poisoned

- impairment may end;
- before/after continuity becomes visible evidence;
- false clues should often leave fair future paths for detecting disruption;
- temporary coherence may be preferable to a permanent impossible-to-resolve world.

These differences belong to strategic weighting/policy, not duplicated role semantics.

## 10. Implementation campaign

### EPI-MQ-0 — scenario corpus / diagnostic vocabulary

- build the first pair-role behavior corpus;
- record GOOD / ACCEPTABLE / POOR comparisons and explanation tags;
- do not freeze numeric weights.

### EPI-MQ-1 — hypothetical observation seam

- candidate -> temporary player-visible observation;
- recipient PlayerWorldSet BEFORE/AFTER;
- exact A3 baseline for small states;
- diagnostics only, no production ranking change.

### EPI-MQ-2 — hard credibility / contradiction / fairness gates

Start with robust failures:

- public impossibility;
- obvious impairment self-exposure;
- hidden-fact leakage;
- extreme confirmation lock;
- unreasonably direct faction verdict.

Prefer explicit gate/warning semantics before hiding these behind a total score.

### EPI-MQ-3 — persistence + breakability metrics

- timeline continuity;
- likely future distinguishing observations;
- accessible discovery paths;
- early focus on pair roles.

### EPI-MQ-4 — cross-role interaction + confirmation-lock metrics

- proposition overlap/tension;
- claim/bluff/configuration interaction;
- trust/suspicion chains;
- mutual-confirmation risk.

### EPI-MQ-5 — Productive Uncertainty ranking

Combine diagnostics through explainable tiers/Pareto ranking first.

Only introduce a weighted aggregate if scenario evidence shows it improves decisions and remains interpretable.

### EPI-MQ-6 — Drunk vs Poisoned policy tuning

- shared legal/semantic domain;
- differing strategic emphasis only where behavior evidence supports it.

### EPI-MQ-7 — shadow production evaluation

Compare:

```text
legacy provider chose A
new provider would choose B
why?
```

without changing user-visible output.

Collect scenario, distribution, performance and explanation evidence.

### EPI-MQ-8 — controlled rollout

Replace the provider only after:

- semantic correctness;
- scenario-corpus quality;
- exact-world cross-validation;
- performance/resource behavior;
- explanation quality;
- distribution/fairness review

all pass the appropriate gates.

The stable UI/decision contract should not need redesign during this cutover.

## 11. Recommendation output contract

The provider should conceptually return:

```text
RecommendationResult
- primary: Candidate?
- alternatives: List<Candidate>   // normal UI max 2
- confidence / quality tier
- explanation / reason codes
- warning codes
```

Requirements:

- primary and alternatives belong to the current legal candidate domain;
- absence/low confidence is representable;
- manual selection remains unaffected by provider failure;
- alternatives need not be filled to two;
- provider replacement must not change candidate legality or confirmation semantics.

## 12. Exact baseline and future scalability

A3 exact enumeration remains the correctness oracle for early tests and small Trouble Brewing states.

A4/ZDD or another compressed backend may later accelerate evaluation, but remains shadow until equivalence and resource behavior are separately validated.

Timeout/resource exhaustion must be represented as unknown/degraded evaluation, never false `UNSAT`.

The quality engine must allow world backend replacement without changing:

- role semantics;
- legal candidate identity;
- shared decision confirmation;
- public recommendation/manual UX.

## 13. Testing philosophy

Prefer:

- scenario-level behavior tests;
- comparative expectations such as “plausible breakable candidate outranks immediately impossible candidate”;
- hard invariants such as no hidden Storyteller facts entering recipient knowledge;
- exact-world oracle cross-validation for small states;
- monotonic/gate relations where robust;
- explanation assertions at the reason-code/tier level;
- distribution tests only after semantic and scenario correctness are established.

Avoid:

- exact numeric score tables as the primary contract;
- brittle source-shape tests;
- forcing every intermediate implementation edit through a new RED;
- broad all-script rollout before Trouble Brewing behavior is understood.

## 14. Non-goals

Do not initially attempt:

- general human psychology simulation;
- conversational NLP truth detection;
- perfect prediction of future player claims;
- omniscient game-theoretic win-rate maximization;
- Storyteller policy that directly maximizes one faction's win probability;
- broad future-script optimization before Trouble Brewing quality is understood;
- production A4/ZDD cutover as part of this campaign's early phases;
- another legacy recommendation-style redesign before Productive Uncertainty.

## 15. Acceptance philosophy

A high-quality misinformation recommendation should be explainable in terms of:

1. what player-visible worlds it creates or preserves;
2. why those worlds are currently credible;
3. what other information/claims they interact with;
4. why the clue does not immediately expose impairment;
5. why it does not create an excessive confirmation lock;
6. how players could later discover or challenge it;
7. why the resulting ambiguity remains fair and useful.

That explanation is more important than proving the selected clue is simply “farther from the truth”.

## 16. Relationship to current UX work

Current UX work is a prerequisite boundary campaign, not a competing algorithm campaign.

The intended sequence is:

```text
pair/manual decision Foundation
-> production pair vertical slice
-> manual-authority audit across clue families
-> remove global mode + freeze provider-neutral recommendation shell
-> EPI-MQ becomes mainline
-> shadow comparison
-> provider cutover
```

If UX work begins expanding into new legacy ranking heuristics, stop and return to this algorithm campaign instead.
