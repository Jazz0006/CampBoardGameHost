# Storyteller Decision Engine — Unified Strategic-Robustness Route

> Date: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT ARCHITECTURE / PRODUCT ROUTE**  
> Current implementation entry: **SDE-0 / PR #143**  
> Supersedes as execution authority: first-night-only EPI-MQ routes, earlier productive-uncertainty scoring plans, and the older revision-driven dynamic-decision implementation plan.

## 1. Product target

The long-term product target is an automatic Blood on the Clocktower Storyteller capable of making ordinary Storyteller discretionary decisions without requiring an experienced human Storyteller to mentally repair the game state.

Immediate profile:

```text
BEGINNER / ordinary players
+ ordinary / inexperienced evil team
+ PUBLIC_GOOD_INFO stress assumption
```

The engine must generate decisions that are:

- rules-legal;
- internally coherent;
- useful to good players;
- strategically robust enough that an ordinary evil team still has several understandable worlds in which it can continue to play.

Night 1 is the first calibration surface, not the architectural endpoint.

## 2. Core architecture

Do not treat clue recommendation as independent per-role scoring.

Authoritative route:

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
profile / phase Storyteller policy
        ↓
reject clearly bad candidates / bundles
        ↓
select among acceptable survivors
        ↓
commit through canonical session / flow ownership
```

Conceptual orchestration owner:

```text
StorytellerDecisionEngine
```

This owner must remain thin. It is not a second rules engine, second state engine, or second possible-world solver.

## 3. Existing foundations to preserve

### Rules / candidate legality

Rules and canonical candidate producers remain the only legality authority.

Recommendation consumes legal outcomes; it must not recreate role rules.

### InformationProposition / observations

`InformationProposition` remains the common semantic language for what a player is shown or what a public claim means epistemically.

`ShownRoleAt` remains distinct from actual `RoleAt`. Public shown-role semantics must not leak hidden Drunk identity or other hidden actual-state facts.

### Exact epistemic evaluator

The existing exact hypothetical evaluator remains the single consequence authority.

Do not create a parallel `StrategicWorldSolver` in recommendation.

Current useful diagnostics include:

- exact BEFORE / AFTER world counts;
- possible Demon seats;
- distinct evil-team seat configurations;
- Demon / evil cover;
- forced-good seats;
- forced-evil seats;
- leave-one-out interaction evidence.

### Historical replay

Historical exact replay is required for later-night decisions. Do not architect around pristine Night 1 only.

### Dynamic / effective-state infrastructure

Existing dynamic candidate generation, impaired-information semantics, registration legality, interaction-time effective-state projection, and dynamic game-state infrastructure are foundations to evolve, not replace with parallel state.

## 4. Strategic worlds, not raw role permutations

Raw world cardinality is not the primary quality metric.

Two worlds that differ only because healthy good players swap similar good roles can have nearly identical strategic meaning. Moving one player between good / Minion / Demon is much more important.

Primary product-level structure therefore includes:

```text
possible Demon seats
Demon + Minion seat configurations
evil cover
forced-good seats
forced-evil seats
```

A recommendation can have thousands of surviving role worlds and still be strategically fragile if nearly all survivors share the same evil topology.

## 5. Target region: neither too strong nor too weak

The policy must distinguish:

```text
BAD_TOO_WEAK
ACCEPTABLE
BAD_TOO_STRONG
UNCERTAIN
```

The target is not maximum uncertainty.

An acceptable result should provide meaningful information while preserving multiple understandable strategic counterworlds for ordinary evil players.

Initial production semantics remain conceptually:

```text
hard legality
→ exact structural diagnostics
→ profile Badness gates
→ random selection among acceptable survivors
```

Do not use one opaque global scalar whose maximum becomes the authority.

A later bounded soft-preference layer is allowed only after deterministic Badness behavior is validated.

## 6. PUBLIC_GOOD_INFO model

The first policy profile uses a deliberate stress assumption: healthy good players eventually make their role and received information public.

Public speech is **not** Storyteller confirmation.

PR #143 establishes the healthy-stage public-claim model conceptually as:

```text
speaker is evil
OR
(shown role matches claim AND claimed clue is mechanically true)
```

This preserves evil bluff worlds while keeping healthy good speakers truthful under the stress model.

Strict mechanically known `ShownRoleAt` remains exact and must not be weakened merely because public speech is modeled permissively.

Later, if the app records actual claims, assumed-public information may be replaced by actual-public information. That is not required for the first deterministic baseline.

## 7. Narrative viability for ordinary evil players

Mathematical existence of an alternative world is not sufficient.

For the ordinary-player profile, future policy should distinguish simple viable counterworlds from technically legal worlds that require several simultaneous obscure exceptions.

Low-complexity explanations can include:

- Fortune Teller YES explained by Red Herring;
- Investigator information explained by legal Recluse registration;
- one legal Spy/Recluse registration choice.

This dimension is currently called **narrative viability / narrative complexity**.

Do not freeze a numeric formula yet.

## 8. Whole-bundle / whole-history interaction

Individually reasonable clues can combine into destructive information.

The evaluator must reason over the complete relevant information ecology rather than summing isolated role scores.

Canonical adversarial lessons:

### Pair information + Fortune Teller

Washerwoman / Librarian information can support an information source while a Fortune Teller `NO` can reverse-confirm that source as non-Demon, creating a confirmation loop.

Red Herring placement can materially cut or create this chain.

The engine should discover the consequence through world evaluation, not hard-coded `if Washerwoman + Fortune Teller` rules.

### Investigator + Chef + Empath

Three individually normal first-night facts can jointly reduce Demon/Minion topology to almost one world.

This is a canonical proof that local pressure scoring is insufficient.

### Recluse restoring ambiguity

Legal Recluse registration can restore substantial strategic ambiguity to the same Investigator + Chef + Empath structure.

The lesson is not `Recluse should always register evil`; registration choice itself belongs to the global decision problem.

## 9. Spy / Recluse registration

Registration is **per interaction**, not a persistent global boolean.

Do not model:

```text
Recluse = evil
Spy = good
```

as permanent state.

Rules own which registrations are legal for the current observing interaction.

Storyteller policy chooses among those legal registrations using exact whole-state consequences.

If poison / drunkenness disables special registration at the interaction point, effective-state legality removes the candidate.

## 10. First-night decision lifecycle

Do not compute one final immutable Night-1 bundle before player-controlled state changes occur.

Use lifecycle semantics:

```text
PERSISTENT
    actual roles / seating
    setup-level commitments
    Demon bluffs
    Fortune Teller Red Herring
    Drunk identity / shown-role commitments where persistence is required

COMMITTED
    already shown / executed
    immutable

PLANNED / UNCOMMITTED
    not yet shown
    may be invalidated and re-evaluated
```

A planning object may precompute candidates, but runtime authority is always current effective state plus committed history.

## 11. Poisoner handling

After a Poisoner target becomes known:

```text
apply poison to effective NightState
→ preserve PERSISTENT decisions
→ preserve COMMITTED decisions
→ invalidate affected PLANNED / UNCOMMITTED decisions
→ regenerate / re-evaluate the remaining relevant decision ecology
```

Do not merely replace the poisoned information role's one clue while freezing all other recommendations.

Poisoning can also affect non-information roles that participate in information semantics, especially Spy / Recluse registration legality.

For the first implementation, broad correct re-evaluation is preferred over premature incremental optimization.

## 12. Engine responsibility continues after Night 1

The same decision architecture must handle later interactions such as:

- poisoned/drunk Empath numeric result;
- poisoned/drunk Fortune Teller YES/NO;
- poisoned/drunk Undertaker shown role;
- poisoned/drunk Ravenkeeper shown role;
- later Spy/Recluse registration;
- supported Mayor redirect / Demon succession and other Storyteller-discretion choices.

Per-role modules answer:

> What outcomes are legal here?

Shared Storyteller policy answers:

> Which legal outcome is appropriate for this state, phase, skill profile, and accumulated public information?

## 13. Event-driven decision model

Later-game decisions should be interaction-driven:

```text
interaction begins
→ build current effective DecisionContext
→ generate legal outcomes
→ materialize hypothetical propositions / effects
→ exact hypothetical evaluation against durable history
→ apply phase/profile policy
→ select acceptable outcome
→ commit through session authority
→ next interaction observes new committed history
```

Recommendation does not own night ordering, canonical mutation, poison derivation, deaths, protection, or execution history.

## 14. Information pacing

A fixed Day-1 robustness threshold cannot govern the whole game.

Conceptually:

```text
early game
    resist premature Demon / evil-topology collapse

mid game
    permit stronger convergence and clearer competing narratives

final stages
    allow legitimate information to solve the game
```

`information pacing` is therefore a required future policy dimension based on phase / round / alive count and possibly other durable state.

Do not freeze the numeric pacing curve before corpus evidence.

## 15. ConsequenceEvaluator retirement

`recommendation/dynamic/ConsequenceEvaluator` is legacy and **not part of the long-term architecture**.

Do not add new product policy to it.

Its heuristic repeated-target, one-shot, high-impact misinformation, final-day and `evilAdvantage` logic attempts to estimate effects that the new route should measure directly through exact strategic diagnostics plus profile/phase policy.

Keeping both would create dual ownership and conflicting decisions.

Retirement route:

1. audit callers / fanout;
2. identify context signals that are genuine inputs rather than heuristic conclusions;
3. migrate those inputs into unified DecisionContext / StorytellerPolicy;
4. cut callers to exact strategic robustness policy;
5. delete `ConsequenceEvaluator` once no unique contract depends on it;
6. audit `evilAdvantage`, `PublicBalanceHint`, information-pressure and related heuristic-only state for deletion or narrower non-authoritative use.

The target state is removal, not permanent secondary scoring.

## 16. Ownership boundaries

```text
rules
  -> legal ability outcomes
  -> registration legality
  -> role semantics

session
  -> canonical actual state
  -> persistent / committed history
  -> authoritative mutation

flow
  -> interaction ordering / projection
  -> consumes resolved facts

epistemic
  -> recipient-visible knowledge
  -> exact hypothetical consequences
  -> strategic structural diagnostics
  -> historical replay

recommendation / StorytellerDecisionEngine
  -> compose legal options
  -> consume exact diagnostics
  -> apply skill-profile / game-phase policy
  -> select acceptable outcomes

UI
  -> display / confirmation / manual Experienced-mode override
```

## 17. Skill profiles

The first target remains BEGINNER / ordinary-player oriented.

Do not prematurely create many skill levels.

Long-term product thinking currently favors a small number of meaningful profiles, roughly:

```text
NORMAL / ordinary
EXPERT
```

Player/table skill profile is distinct from Storyteller UI mode. Experienced Storyteller mode may allow manual selection while using the same underlying table-skill policy.

No final NORMAL / EXPERT thresholds are frozen.

## 18. Corpus requirements

SDE-0 / PR #143 must include deliberate adversarial examples, not only random healthy bundles.

Required families include:

- Pair-information + Fortune Teller confirmation chains;
- Red Herring variants that cut/create those chains;
- Investigator + Chef + Empath collapse;
- equivalent cases with Recluse registration alternatives;
- too-weak bundles;
- clearly acceptable healthy contrasts.

Later staged expansion will add:

- Drunk shown-role / false information;
- Spy registration;
- Poisoner target / re-planning;
- later impaired Empath / Fortune Teller / Undertaker / Ravenkeeper;
- late-game cases where stronger convergence is correct.

Human labels:

```text
BAD_TOO_STRONG
ACCEPTABLE
BAD_TOO_WEAK
UNCERTAIN
```

Use calibration and sealed holdout scenarios. Do not derive gates and validate them on the same examples.

## 19. Frozen vs deliberately unfrozen

### Frozen

- rules own legality;
- exact epistemic evaluator owns world consequences;
- strategic evil topology matters more than raw role-world count;
- whole-bundle / whole-history interaction matters;
- Spy/Recluse registration is per interaction;
- Poisoner can invalidate uncommitted decisions;
- the same engine continues after Night 1;
- BEGINNER / ordinary-player profile is first;
- `ConsequenceEvaluator` is targeted for removal;
- no second rules engine;
- no second possible-world solver;
- no opaque global-optimum scalar.

### Unfrozen

- exact Demon-candidate thresholds;
- exact evil-team-configuration thresholds;
- forced-good / forced-evil limits;
- narrative-complexity formula;
- information-pacing curve;
- NORMAL / EXPERT numeric profiles;
- exhaustive vs beam search;
- optional bounded soft preference;
- production cutover timing.

## 20. Implementation route

### SDE-0 — active now / PR #143

Use the merged healthy whole-bundle harness plus existing PR #143 corpus foundation.

Continue with deliberate adversarial scenarios, human review, interpretable diagnostics and first BEGINNER policy gates.

Do not restart PR #143 and do not cut over production selection yet.

### SDE-1 — unified orchestration seam

- formalize thin `StorytellerDecisionEngine` / DecisionContext boundary;
- reuse canonical candidate generators;
- reuse exact epistemic evaluator;
- preserve session / flow ownership;
- establish persistent / committed / planned lifecycle.

### SDE-2 — first-night uncertainty

Expand separately:

1. Drunk;
2. Spy/Recluse per-interaction registration;
3. Poisoner target / effective-state invalidation / re-planning.

### SDE-3 — cross-night information

Bring later impaired / registration decisions through the same engine using historical exact replay.

### SDE-4 — production cutover / cleanup

- cut production callers to unified policy;
- preserve Experienced-mode manual override;
- migrate only useful legacy context inputs;
- delete `ConsequenceEvaluator` and stale heuristic-only state after fanout audit;
- retire superseded recommendation paths/tests when stronger typed coverage exists.

## 21. Validation principles

Follow `AGENTS.md` and `TESTING_STRATEGY.md`.

- test durable behavior at the true typed owner;
- exact semantic changes trigger appropriate epistemic/oracle validation;
- exploratory corpus measurement does not require manufactured RED tests;
- stable policy/gate contracts require durable regression evidence;
- central orchestration/shared semantic cutovers require broader T2/T4 validation;
- keep expensive calibration experiments outside ordinary bounded regression execution.

## 22. New-conversation start

Read:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. this route;
6. query live `main`, PR #143 and current CI;
7. synchronize #143 with latest `main` if behind;
8. continue SDE-0 from the existing PR.

Do not reopen completed FN-BUNDLE-0/1/2 audits without a concrete regression.

## 23. Stable decision

> **The automatic Storyteller is a persistent game-state decision engine, not a collection of independent clue recommenders. Rules produce legal outcomes; the existing epistemic engine measures exact hypothetical consequences; Storyteller policy evaluates strategically meaningful evil topology, confirmation structure, information value, narrative viability and game-phase pacing; then it selects among acceptable legal outcomes. First-night bundles are the first calibration surface, not the endpoint. Spy/Recluse registration and poisoning are interaction-time state inputs; later impaired information uses the same engine; and `ConsequenceEvaluator` is a migration-era heuristic layer targeted for removal after unified-policy cutover. PR #143 is the active SDE-0 implementation and must be continued rather than restarted.**
