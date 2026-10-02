# Imp Podcast Product Policy Calibration — 2026-10-02

> Status: **PRODUCT-OWNER CALIBRATION DRAFT / NON-EVIDENCE / NO PRODUCTION CUTOVER AUTHORIZED**
>
> Scope: current Trouble Brewing Storyteller Recommendation Engine behavior for the user's present app/player population.
>
> Source context: machine-first full-transcript semantic review of **22: Imp (Trouble Brewing)**, followed by direct product-owner question/answer calibration.
>
> This document is intentionally separate from EvidenceLab verification. It records the product owner's current policy interpretation and may be revised later as the app, player population, or evidence base changes.

## 1. Boundary

This calibration does **not**:

- convert machine-assisted podcast findings into VERIFIED EvidenceLab evidence;
- satisfy the existing C3 Stage-1 same-prefix candidate-comparison gate by itself;
- mutate frozen `BEGINNER_CONSERVATIVE_V1`;
- authorize numeric weights/thresholds;
- authorize Beginner automatic Drunk cutover.

It **does** provide an explicit product-policy target for later versioned recommendation work once the existing cutover/evidence gates are satisfied.

## 2. Drunk candidate calibration

### 2.1 Empath as Drunk

Treat **Empath-as-Drunk** as a meaningful positive recommendation factor, not a hard rule.

Current product-owner interpretation:

- Empath is worth considering when truthful Empath information would materially help Good, especially around Evil/Demon adjacency;
- this factor is medium-to-strong rather than dominant;
- compare it with all other setup/context signals;
- absent stronger context, making Empath Drunk is generally more useful than making a low-impact role such as Soldier Drunk;
- apply a meaningful **recent-repeat penalty**: if Empath has repeatedly been used as Drunk in recent games, reduce or reverse that preference and consider Soldier or another role instead.

Implication: role strategic value + current setup interaction + recent-role history should be separable inputs rather than one fixed role ranking.

## 3. Impaired-information policy

These are strong default principles for Drunk/Poisoned or otherwise legally impaired information.

### 3.1 Legality first

False information is allowed only where the rules permit it. Storyteller discretion never overrides legality.

### 3.2 Plausible strategic misinformation — strong default

When false information is legal, strongly prefer information that is:

1. plausible;
2. capable of supporting a coherent wrong world;
3. strategically meaningful to the current table state.

Obviously absurd information that merely reveals impairment should normally be strongly down-ranked.

### 3.3 World-model impact

When multiple false results are all legal and plausible, prefer the result that more effectively changes Good's current world model, creates useful misattribution, or supports a wrong-but-coherent explanation.

Plausibility is a gate, not the final objective.

### 3.4 Avoid exposing impairment

If one false result is more disruptive but would strongly reveal that the player is Drunk/Poisoned, while another is slightly weaker but preserves uncertainty, normally prefer the latter.

Expose impairment only when doing so serves a larger intentional plan.

### 3.5 True information remains available

Drunk/Poisoned does **not** mean “always give false information.”

Correct information can be intentionally recommended to:

- preserve uncertainty about impairment;
- avoid repetitive patterns;
- support a larger multi-night wrong world;
- maintain narrative coherence.

This is an advanced option, not the default.

### 3.6 Multi-night coherence is high priority

For roles receiving information across multiple nights, evaluate the sequence, not only the current night.

Normally prefer preserving a coherent cross-night narrative over maximizing one night's misleading strength.

Break or redirect the narrative only when the game state has materially changed or the prior wrong world is no longer useful.

## 4. Competitive-game protection

When Good's information structure is rapidly collapsing onto the Imp, preserving Demon ambiguity and keeping the game competitive is a **strong Storyteller objective**, within legal bounds.

This is not “help Evil regardless.” It is a balancing objective:

- preserve reasonable uncertainty;
- prevent premature game collapse;
- avoid overcorrection when Evil is already mechanically dominant.

Mechanical state should be treated as more reliable than table “vibes.”

## 5. Demon bluff policy

### 5.1 Evaluate both claim value and absence-information value

A Demon bluff is valuable for two distinct reasons:

1. how usable the role is as a claim;
2. what Evil learns from knowing that role is not actually in play.

Example implication: an Undertaker bluff can change Evil's risk tolerance because Evil also learns there is no real Undertaker.

### 5.2 Score bluff sets as sets

Do not choose three bluffs by independently ranking roles and taking the top three.

Evaluate:

- functional coverage;
- early-public vs long-lived claims;
- Outsider/world-building utility;
- information revealed by absent roles;
- coordination opportunities;
- risk distribution;
- interaction between the three claims.

### 5.3 Avoid all-high-maintenance sets

Avoid a bluff set where all three roles require sustained complex information maintenance.

This is especially important for inexperienced Evil players. More experienced players may tolerate higher-complexity sets.

## 6. Star-pass / successor calibration

When an Imp star-passes and multiple Minions are legal recipients, **which Minion ability is preserved or lost** is a strong factor.

Current product-owner interpretation:

- if Evil is struggling, preserving a useful ongoing Minion ability such as Poisoner/Spy should weigh strongly; making a lower-value-now Minion such as Baron the new Imp may be preferable;
- if Evil is already mechanically very strong, deliberately losing a strong ongoing Minion ability can be a legitimate balancing action;
- the “preserve useful ability” signal is stronger than the “actively weaken Evil for balance” signal;
- balance remains contextual, not a hard rule.

Scarlet Woman behavior remains rules-bound; this calibration does not redefine legal star-pass mechanics.

## 7. Judging who is ahead

Do not strongly rebalance based only on inferred trust or the impression that Evil “looks ahead.”

Prefer:

1. mechanically established state;
2. explicit observable public information/claims;
3. inferred table sentiment with lower confidence.

A trusted Imp can become suspected quickly as the candidate field narrows. Storyteller recommendations should preserve uncertainty about inferred player beliefs.

## 8. Player-experience balancing

Player experience is a meaningful balancing factor.

For a highly experienced Evil player among mostly new players, stronger balancing intervention may be appropriate.

However, extreme examples such as showing the Drunk as one of Evil's three “not in play” roles should remain an **edge case**, not a normal recommendation.

Use player experience to adjust recommendation strength/complexity, not to create unconditional named-role rules.

## 9. Evil intended plan as enrichment

If Evil communicates an intended plan or requests Storyteller support, preserve that as optional recommendation context.

It should materially inform recommendations when known, but it must not override:

- game rules;
- legal candidate domains;
- overall competitive balance;
- stronger mechanical state;
- explicit product-policy constraints.

This is **optional enrichment**, not required context.

## 10. Policy-shape summary

Current intended qualitative ordering:

```text
hard legality
-> coherent / plausible strategic effect
-> current world-model impact
-> avoid prematurely revealing impairment
-> preserve cross-night narrative
-> preserve competitive game
-> setup / role interactions
-> player experience
-> recent-repeat penalties
-> optional Evil plan / public-claim enrichment
```

This is not a scalar-weight specification. Later implementation should prefer explicit predicates, reason codes, partial ordering and typed unavailable context over invented precision.

## 11. Revisit rule

These calibrations are intentionally revisable.

Revisit when:

- new VERIFIED EvidenceLab evidence materially conflicts with them;
- the app moves from beginner/mixed groups toward experienced groups;
- repeated live usage exposes predictable patterns;
- recommendation telemetry or human review identifies systematic over/under-balancing;
- a later policy version gains authoritative multi-night narrative, player-history or public-claim inputs.

Until then, treat this document as the current product-owner interpretation target, not as external expert evidence or production authority.
