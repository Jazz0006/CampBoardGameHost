# Storyteller Decision Engine — Unified Strategic-Robustness Route

> Date: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT ARCHITECTURE / PRODUCT ROUTE**  
> Supersedes as execution authority: the first-night-only EPI-MQ route and earlier productive-uncertainty scoring plans.

## 1. Product target

The long-term product target is an automatic Blood on the Clocktower Storyteller capable of making ordinary Storyteller discretionary decisions without requiring an experienced human Storyteller to repair the game state mentally.

The immediate target profile is:

```text
BEGINNER / ordinary players
+ ordinary / inexperienced evil team
+ PUBLIC_GOOD_INFO stress assumption
```

The algorithm must not merely generate rules-legal information. It must generate information that is legal, coherent, useful, and strategically robust enough that an ordinary evil team still has several understandable worlds in which it can continue to play.

The engine must eventually cover both first-night and later-game discretionary decisions, including impairment and special registration.

## 2. Core architectural decision

Do not treat clue recommendation as independent per-role scoring.

The authoritative route is:

```text
canonical actual GameState / interaction-time effective state
        ↓
rules-owned legal outcome generation
        ↓
InformationProposition / EpistemicObservation materialization
        ↓
exact hypothetical epistemic consequence evaluation
        ↓
strategic world-structure diagnostics
        ↓
profile-specific Storyteller policy
        ↓
reject clearly bad candidates / bundles
        ↓
select among acceptable survivors
        ↓
commit through canonical session / flow ownership
```

The long-term orchestration owner is conceptually:

```text
StorytellerDecisionEngine
```

This is an orchestration / policy owner, not a second rules engine and not a second possible-world solver.

## 3. Existing foundations to preserve

The current codebase already contains most of the required lower-level architecture. Preserve and extend it rather than rebuilding parallel systems.

### 3.1 Rules / candidate legality

Rules and canonical candidate producers remain the only legality authority.

Examples include existing pair-information legality and registration domains. Recommendation policy must consume legal candidates rather than re-encode role rules.

### 3.2 InformationProposition / observations

`InformationProposition` remains the common semantic language for what a player is shown or can publicly claim from Storyteller information.

`ShownRoleAt` remains distinct from actual `RoleAt`. Public shown-role claims must not leak hidden Drunk identity or other hidden actual-state facts.

### 3.3 Exact epistemic evaluator

The existing exact hypothetical evaluator remains the single consequence authority.

Do **not** create a second `StrategicWorldSolver` or a second rules-aware possible-world implementation inside recommendation.

The current exact diagnostics already expose the strategically important structure required by the new route, including:

- possible Demon seats;
- distinct evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil cover;
- exact BEFORE / AFTER world counts;
- leave-one-out interaction evidence.

Recommendation policy consumes these outputs.

### 3.4 Historical multi-night replay

The historical exact replay path is a required foundation for later-night decisions. First-night experiments are not the architectural boundary of the system.

### 3.5 Dynamic decision / registration infrastructure

Existing dynamic candidate generation, impaired-information semantics, registration legality, interaction-time effective-state projection, and `DynamicGameState` are reusable foundations. They should be evolved rather than replaced by a parallel state model.

## 4. Strategic worlds, not raw role-permutation worlds

Raw possible-world cardinality is not the primary product metric.

Two worlds that differ only because two healthy good players exchange similar good roles can have almost identical strategic meaning. By contrast, moving one player from good to Minion/Demon is highly significant.

The primary structural projections therefore focus on evil topology:

```text
possible Demon seats
Demon + Minion seat configurations
evil cover
forced-good seats
forced-evil seats
```

A large exact world count does not make a recommendation robust if almost every surviving world has the same Demon/Minion layout.

Likewise, a recommendation is not automatically good merely because it preserves the maximum number of worlds.

## 5. Target region: neither too strong nor too weak

The policy must reject both extremes:

```text
too weak
    -> information has little useful strategic value

acceptable
    -> good receives meaningful information
    -> several viable evil topologies / narratives survive

 too strong
    -> Demon / evil topology collapses too early
    -> large trusted-good core forms
    -> ordinary evil players have no simple viable counterworld
```

Do not maximize uncertainty.

Do not search for one mathematically optimal clue.

Do not reintroduce one opaque scalar whose maximum becomes the product decision.

Initial production semantics should remain conceptually:

```text
hard legality
→ exact structural diagnostics
→ profile Badness gates
→ random selection among acceptable survivors
```

A later bounded soft-preference layer is allowed only after deterministic Badness behavior is validated.

## 6. BEGINNER / PUBLIC_GOOD_INFO stress model

The first policy profile assumes healthy good players eventually make their role and received information public on Day 1.

This is deliberately conservative. It models the user's current table style and protects against information bundles that become destructive when players share aggressively.

The model is not a claim that all real tables always share all information.

Later, if the product records actual claims / speeches, the evaluator may replace assumed-public information with actual-public information. That is a future refinement, not a prerequisite for the deterministic baseline.

## 7. Narrative viability for ordinary evil players

Mathematical existence of an alternative world is not sufficient.

For the BEGINNER / ordinary-player profile, the engine should eventually distinguish:

```text
simple viable counterworld
```

from:

```text
technically legal but requiring several simultaneous obscure exceptions
```

Examples of low-complexity alternative explanations include:

- a Fortune Teller YES caused by the Red Herring;
- an Investigator result involving a Recluse registering as a Minion;
- one legal Spy/Recluse registration choice.

An alternative world that requires a long stack of unrelated special assumptions should contribute less to beginner robustness.

This concept is currently named **narrative viability / narrative complexity**.

Do **not** freeze a numeric formula yet. It remains a calibration target.

## 8. Whole-bundle interaction remains mandatory

Individually reasonable clues can combine into a destructive information chain.

The evaluator must reason over the complete relevant information ecology, not sum isolated per-role scores.

Important benchmark patterns discovered during design include:

### 8.1 Pair-information + Fortune Teller confirmation

A Washerwoman or Librarian clue can support an information source, while a Fortune Teller `NO` can then hard-confirm the pair-information source as non-Demon, creating a self-reinforcing confirmation chain.

The Red Herring can materially change this structure. For example, making the pair-information source the Red Herring can prevent a Fortune Teller `NO` involving that player and therefore cut the reverse-confirmation edge.

The policy must discover this through exact consequences rather than hard-coded `if Washerwoman + Fortune Teller` rules.

### 8.2 Investigator + Chef + Empath collapse

A setup containing Investigator, Chef, and Empath can produce three individually normal first-night facts whose conjunction identifies the Minion/Demon topology almost uniquely.

This is a canonical adversarial benchmark because it proves local pressure scoring is insufficient.

### 8.3 Recluse restoring ambiguity

The same Investigator + Chef + Empath structure can regain substantial strategic robustness when legal Recluse registrations are included.

The lesson is not `Recluse should always register evil`.

The lesson is that registration choices are part of the same global decision problem and should be selected according to whole-structure consequences.

## 9. Spy / Recluse registration is a per-interaction decision

Do not model:

```text
Recluse = evil
Spy = good
```

as persistent global booleans.

Registration is interaction-specific. The same player may legally register differently for different abilities / questions when the rules permit it.

The engine should treat each legal registration as a candidate decision attached to the observing interaction.

Conceptually:

```text
RegistrationDecision
  observer / source ability
  subject seat
  registration question
  registered alignment
  registered type
  registered role
```

Rules own which registrations are legal. The Storyteller policy chooses among those legal candidates using exact strategic consequences.

If poison / drunkenness disables the special registration ability at the interaction point, the special registration candidate must disappear through effective-state legality rather than through recommendation hacks.

## 10. First-night planning is not a permanently frozen static bundle

The old mental model of `compute one final FirstNightBundle before Night 1 begins and never revisit it` is insufficient.

Use lifecycle semantics:

```text
PERSISTENT
    actual roles / seating
    Drunk identity and shown-role commitment where rules require persistence
    Demon bluffs
    Fortune Teller Red Herring
    other setup-level persistent decisions

COMMITTED
    information already shown / action already committed
    immutable for later planning

PLANNED / UNCOMMITTED
    recommendation decisions not yet shown
    may be invalidated and re-evaluated when effective state changes
```

A first-night planning object may precompute useful candidate structure, but the runtime authority is the current effective game state plus already committed decisions.

## 11. Poisoner target handling

A first-night Poisoner acts early enough to change later information legality and quality.

Do not merely replace the poisoned information role's one clue while leaving every other precomputed recommendation frozen.

After the Poisoner target becomes known:

```text
apply poison to effective NightState
→ preserve PERSISTENT decisions
→ preserve already COMMITTED decisions
→ invalidate affected PLANNED / UNCOMMITTED decisions
→ regenerate / re-evaluate the remaining relevant decision ecology
```

For the first implementation, correctness is more important than incremental optimization. Re-evaluating all still-uncommitted relevant first-night decisions is acceptable.

Poisoning can also affect non-information roles that influence information semantics, especially Spy / Recluse registration legality. Therefore the invalidation condition must not be `if poisoned target is an information role`.

## 12. Engine responsibility continues after Night 1

The same decision architecture must serve later nights and days.

Examples include:

- poisoned / drunk Empath numeric result;
- poisoned / drunk Fortune Teller YES/NO;
- poisoned / drunk Undertaker shown role;
- poisoned / drunk Ravenkeeper shown role;
- Spy / Recluse registration during later interactions;
- Mayor death redirection;
- Demon succession or other Storyteller-discretion choices where supported by rules.

Per-role modules answer:

> What outcomes are legal here?

The shared Storyteller policy answers:

> Which legal outcome is appropriate for this game state, phase, skill profile, and accumulated public information?

## 13. Event-driven decision model

Later-night decision making should be interaction-driven rather than one giant whole-night static bundle.

Conceptually:

```text
interaction begins
→ build current effective DecisionContext
→ generate legal candidate outcomes
→ materialize hypothetical propositions / effects
→ exact hypothetical evaluation against durable history
→ apply phase/profile robustness policy
→ select acceptable outcome
→ commit through session authority
→ next interaction observes the new committed history
```

This preserves the existing flow/session ownership boundaries. The recommendation engine does not own night ordering, canonical mutation, poison derivation, deaths, protection, or execution history.

## 14. Information pacing across the game

A fixed Day-1 robustness threshold cannot govern the entire game.

Early game should generally preserve more viable strategic worlds. Later game should naturally converge.

The policy therefore needs a future **information pacing** dimension based on game phase / round / alive count and possibly other durable state.

Conceptually:

```text
D1 / early game
    avoid premature Demon / evil-topology collapse

mid game
    allow stronger convergence and clearer competing narratives

final stages
    allow legitimate information to solve the game
```

Do not freeze the pacing curve before corpus calibration.

## 15. ConsequenceEvaluator retirement decision

`recommendation/dynamic/ConsequenceEvaluator` is a legacy heuristic layer and is **not** part of the long-term target architecture.

Current heuristics such as:

- repeated-target penalty;
- one-shot misinformation penalty;
- high-impact misinformation penalty;
- final-day misinformation penalty;
- `evilAdvantage` adjustment;

attempt to estimate consequences that the new route will measure directly through exact strategic diagnostics and phase/profile policy.

Keeping both as independent authorities would create dual ownership and contradictory decisions.

Therefore:

1. do not add new product policy to `ConsequenceEvaluator`;
2. identify any context signals that remain independently useful (`oneShotAbility`, player-selected target, phase, history, etc.);
3. migrate useful context into the unified decision context / policy boundary;
4. cut callers over to exact strategic robustness policy;
5. delete `ConsequenceEvaluator` once no unique contract depends on it;
6. audit `evilAdvantage`, `PublicBalanceHint`, information-pressure fields and related heuristic-only state for retirement or narrower non-authoritative use.

Do not delete the class before caller migration is safe. The target state, however, is removal rather than permanent secondary scoring.

## 16. Ownership boundaries

Preserve the following ownership:

```text
rules
  -> legal ability outcomes
  -> registration legality
  -> role semantics

session
  -> canonical actual state
  -> persistent / committed action history
  -> authoritative state mutation

flow
  -> interaction ordering / projection
  -> consumes resolved facts
  -> must not become a rules or recommendation engine

epistemic
  -> recipient-visible knowledge
  -> exact hypothetical world consequences
  -> strategic structural diagnostics
  -> historical replay

recommendation / StorytellerDecisionEngine
  -> compose legal candidates / bundles
  -> consume exact diagnostics
  -> apply skill-profile / phase policy
  -> choose among acceptable legal outcomes

UI
  -> display / confirmation / manual expert override
  -> must not reconstruct decision semantics
```

## 17. Selection policy and skill profiles

The first policy remains BEGINNER-oriented.

Do not prematurely create many skill levels.

Long term, the product only needs a small number of meaningful table/player profiles. Current product thinking favors approximately:

```text
NORMAL / ordinary
EXPERT
```

This player-skill profile is distinct from Storyteller UI mode. Experienced Storyteller mode may allow manual selection even while using the same underlying table-skill policy.

No final NORMAL / EXPERT thresholds are frozen yet.

## 18. Benchmarks / corpus requirements

The next calibration corpus must include more than random healthy 7-player bundles.

Retain and add adversarial fixtures representing at least:

- pair-information + Fortune Teller confirmation chains;
- Red Herring choices that cut or create confirmation paths;
- Investigator + Chef + Empath collapse;
- the same structures with Recluse registration alternatives;
- Spy registration alternatives;
- Drunk shown-role / false-information alternatives;
- Poisoner target effects and re-planning;
- later poisoned Empath / Fortune Teller / Undertaker / Ravenkeeper decisions;
- too-weak information bundles;
- late-game cases where stronger convergence is correct.

Human labels remain useful:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

Use calibration and holdout sets. Do not derive gates and validate them on exactly the same corpus.

## 19. What is frozen vs deliberately unfrozen

### Frozen architecture / product decisions

- exact epistemic consequence remains the sole world-consequence authority;
- strategic evil topology matters more than raw full-role world count;
- whole-bundle / whole-history interaction matters;
- rules own legality;
- Spy/Recluse registration is per interaction;
- Poisoner can invalidate uncommitted recommendations;
- the same engine continues after Night 1;
- BEGINNER / ordinary-player mode is the first policy target;
- `ConsequenceEvaluator` is targeted for retirement;
- no second rules engine, no second possible-world solver, no opaque global optimum score.

### Not frozen yet

- exact Demon-candidate count thresholds;
- exact evil-team-configuration thresholds;
- exact forced-good / forced-evil limits;
- narrative-complexity formula;
- information-pacing curve;
- NORMAL vs EXPERT numeric profiles;
- exhaustive search vs beam search for larger candidate spaces;
- any bounded soft-preference score;
- production cutover timing.

These require measurement and corpus review.

## 20. Implementation route from the current baseline

FN-BUNDLE-0, FN-BUNDLE-1 and FN-BUNDLE-2 remain completed foundations.

The route now becomes:

### SDE-0 — BEGINNER strategic-robustness corpus and policy contract

- use the merged healthy whole-bundle harness as the baseline;
- add deliberately adversarial strategic-collapse fixtures;
- formalize policy input around existing exact structural diagnostics;
- derive interpretable candidate Badness gates from human-reviewed data;
- do not perform production selector cutover yet.

This absorbs the useful intent of the former `FN-BUNDLE-3` stage.

### SDE-1 — unified decision orchestration seam

- define the durable `StorytellerDecisionEngine` / decision-context boundary;
- reuse canonical candidate generators and exact evaluator;
- preserve current session / flow ownership;
- establish planned / committed / persistent decision lifecycle;
- no second rules or world model.

### SDE-2 — staged uncertainty in first-night ecology

Expand in controlled steps:

1. Drunk;
2. Spy / Recluse per-interaction registration;
3. Poisoner target / effective-state invalidation and re-planning.

Keep each stage corpus-backed and explainable.

### SDE-3 — cross-night dynamic information

Bring later impaired / registration decisions through the same engine, starting with Trouble Brewing information roles such as Empath, Fortune Teller, Undertaker and Ravenkeeper.

Use historical exact replay as the consequence baseline.

### SDE-4 — policy cutover and heuristic retirement

- cut production decision paths to the unified policy;
- migrate any useful context from legacy local heuristics;
- retire `ConsequenceEvaluator` and stale heuristic-only state after fanout audit;
- retain manual Experienced-mode override paths where product UX requires them.

## 21. Validation principles

Follow `AGENTS.md` and `TESTING_STRATEGY.md`.

For new durable policy contracts:

- test the true ownership seam;
- use exact fixtures for world-structure consequences;
- use corpus / holdout evidence for Badness behavior;
- do not manufacture RED tests for exploratory measurement;
- do not add source-string tests when typed seams exist;
- exact/oracle semantic changes require the corresponding broad validation / T4 gate before merge.

Documentation-only route updates do not require Android regression.

## 22. Immediate next-conversation start

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. this document;
6. query live `main`, open PRs and current CI;
7. start from SDE-0 only.

Do not reopen already completed FN-BUNDLE-0/1/2 audits unless a concrete regression requires it.

## 23. Stable decision

> **The automatic Storyteller is a persistent game-state decision engine, not a collection of independent clue recommenders. Rules produce legal outcomes; the existing epistemic engine measures exact hypothetical consequences; the Storyteller policy evaluates strategically meaningful evil worlds, confirmation structure, information value, narrative viability and game-phase pacing; then the engine selects among acceptable legal outcomes. First-night bundles are the first calibration surface, not the architectural endpoint. Spy/Recluse registration and poisoning are interaction-time state inputs, and later-night impaired information uses the same engine. `ConsequenceEvaluator` is a migration-era heuristic layer targeted for retirement after unified-policy cutover.**
