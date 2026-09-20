# Storyteller Decision Engine — Unified Strategic-Robustness Route

> Date: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT ARCHITECTURE / PRODUCT ROUTE**  
> Current D5F policy authority: `docs/SDE_2D5F_FIRST_NIGHT_INFORMATION_POLICY_SYNTHESIS_2026-09-21.md`  
> Current implementation entry: **SDE-2D / pre-SDE-3 strategic generalization**  
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

Raw world cardinality is not the primary quality metric or the long-term production representation.

Two worlds that differ only because healthy good players swap similar good roles can have nearly identical strategic meaning. Moving one player between good / Minion / Demon is much more important.

The primary recommendation representation therefore moves toward an exact strategic quotient:

```text
StrategicWorldKey(
    demonSeat,
    minionSeats
)
```

A strategic world survives when at least one mechanically legal role assignment / state / registration witness satisfies the same visible facts and hypothetical observations.

Complete mechanical worlds remain necessary as:

- rule-semantic correctness witnesses;
- exact/symbolic feasibility support;
- sources of role-information and narrative explanation detail.

They must not receive additional strategic weight merely because many equivalent Townsfolk permutations realize the same evil topology.

Primary product-level structure therefore includes:

```text
possible Demon seats
Demon + Minion seat configurations
evil cover
forced-good seats
forced-evil seats
narrative support / complexity
```

Keep **role-information utility** separate from strategic pressure: a useful Washerwoman/Librarian-style clue may preserve every evil topology while still giving legitimate good-team information.

For large-player production, prefer exact quotient / constraint / symbolic feasibility over exhaustive materialization and over unlabelled random sampling. The epistemic layer remains the single consequence authority; do not create a second recommendation-owned StrategicWorldSolver.

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

PR #143 established the healthy-stage public-claim model conceptually as:

```text
speaker is evil
OR
(shown role matches claim AND claimed clue is mechanically true)
```

SDE-2D generalizes the model for Drunk/Poisoned information without exposing hidden impairment:

```text
speaker is evil
OR
(shown role matches AND source ability is malfunctioning)
OR
(shown role matches AND source ability is functioning AND clue is mechanically true)
```

This preserves evil bluff worlds, allows unreliable information to be strategically evaluated, and keeps hidden actual Drunk/Poisoner state private.

Strict mechanically known `ShownRoleAt` remains exact and must not be weakened merely because public speech is modeled permissively.

Later, if the app records actual claims, assumed-public information may be replaced by actual-public information.

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

The BEGINNER default is to use the Recluse's special Evil/Minion/Demon registration because that expresses the role's intended ambiguity. This is an interaction-scoped strong prior, not a permanent state or hard rule; whole-bundle health may materially override it.

## 9. Spy / Recluse registration

Registration is **per interaction**, never a persistent global boolean or canonical identity mutation.

Rules own the complete legal registration domain for the current interaction.

BEGINNER policy applies a thematic default:

~~~text
Spy
    -> normally use legal Good/Townsfolk/Outsider registration

Recluse
    -> normally use legal Evil/Minion/Demon registration
~~~

This does **not** mean storing `Spy = good` or `Recluse = evil` across the game. Different abilities may receive different legal registration rulings.

The thematic default is a strong prior. Override it only when the default materially harms whole-bundle health, such as creating an excessive confirmation chain or exposing Evil more strongly.

Role-function exposure is separately relevant:

- avoid Librarian -> Recluse when a healthy alternative exists;
- strongly avoid Investigator -> Spy when a healthy alternative exists;
- accept either when rules force the outcome.

If poison/drunkenness disables special registration at the interaction point, effective-state legality removes the special candidate.

## 10. First-night decision lifecycle

Do not compute one final immutable Night-1 bundle before player-controlled state changes occur.

Use lifecycle semantics:

```text
PERSISTENT
    actual roles / seating
    committed setup-level choices
    committed Demon bluffs
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

Before reveal/commit, Demon bluff triplets are **PLANNED output variables**, not external strategic inputs. Their legality stays in `SetupCandidateGenerator`; strategic selection migrates from `SetupRecommendationService` `demon-bluff-ease` / `bluffDifficulty` heuristics into SDE whole-bundle policy. Once shown, the selected bluff triplet becomes PERSISTENT and later replanning must preserve it.

The Drunk shown role is already PERSISTENT before information recommendation. SDE may select the unreliable clue but must never reselect the shown identity.

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

The same decision architecture must handle every supported later interaction whose legal output depends on impairment, registration or prior visible history.

The architecture must generalize by **information semantics / decision shape**, not by named examples.

Per-role modules answer:

> What outcomes are legal here, and how is each outcome represented semantically?

Shared Storyteller policy/history answers:

> Which legal outcome is appropriate for this state, phase, skill profile, accumulated public information and previously established impaired narrative?

For repeated or history-dependent impaired information, maintain one persistent role-agnostic narrative state representing the believable world already communicated to the affected player.

Examples may include recurring numeric checks, repeated boolean/target checks, later-event role identification, pair/categorical information and future-script abilities. These examples are non-exhaustive and must not produce separate policy algorithms.

A named-role branch is justified only by an actual rules distinction. Cross-night coherence, truth-vs-misinformation pacing and believable counterworld continuity belong to shared policy.

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

## 18. Corpus / evidence requirements

Synthetic fixtures remain useful for correctness, regression and metric sensitivity, but they are not the primary source of policy truth.

Current policy calibration is **expert-observation-first**:

1. identify verified experienced/trusted Storytellers;
2. reconstruct real Trouble Brewing Night-1 state and observed decisions;
3. recover the exact legal alternative set at each lifecycle stage;
4. compare observed expert choices with legal counterfactuals through existing whole-bundle/topology diagnostics;
5. infer only repeated or explicitly reasoned policy constraints;
6. validate those constraints against broader high-fidelity real-game logs;
7. use bounded human review for ambiguity, evidence conflicts and BEGINNER-specific product adaptation.

Evidence classes:

~~~text
GOLD             verified expert/trusted real play
SILVER           high-fidelity real logs; expertise unverified
QUALITATIVE      tutorials / postmortems / community rationale
DIAGNOSTIC_ONLY  synthetic / extreme / counterfactual fixtures
~~~

A chosen expert action is preference evidence, not a binary label over all alternatives. Final game outcome must not be used as a Storyteller-choice quality label.

The existing descriptive categories `BAD_TOO_STRONG / ACCEPTABLE / BAD_TOO_WEAK / UNCERTAIN` may remain useful downstream, but they must not be derived solely from one reviewer's intuition.

Keep expensive corpus/reconstruction work outside ordinary FAST regression.

## 19. Frozen vs deliberately unfrozen

### Frozen

- rules own legality;
- exact epistemic evaluator owns world consequences;
- strategic evil topology matters more than raw role-world count;
- strategic topology is a quotient/feasibility view over mechanically legal worlds, not a second solver;
- raw role permutations sharing one evil topology do not gain strategic weight merely by multiplicity;
- role-information utility remains distinct from strategic pressure;
- whole-bundle / whole-history interaction matters;
- Spy/Recluse registration is per interaction;
- BEGINNER registration policy has a thematic strong prior: Spy normally hides as Good and Recluse normally registers as Evil, with material whole-bundle override;
- Poisoner can invalidate uncommitted decisions;
- repeated/history-dependent impaired information uses shared persistent narrative state rather than named-role policy branches;
- Drunk shown role is persistent while the unshown clue is a whole-bundle output;
- uncommitted Demon bluff triplets are SDE outputs; committed bluff triplets are persistent later inputs;
- Demon bluff legality remains setup-owned;
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
- exact strategic-quotient implementation shape and symbolic backend details;
- measured production switch/cost thresholds, if any;
- optional bounded soft preference;
- production cutover timing.

## 20. Implementation route

### SDE-0 — COMPLETE / PR #143

Established the first BEGINNER strategic-robustness corpus and healthy PUBLIC_GOOD_INFO evidence.

### SDE-1 — COMPLETE / PR #144

Established thin exact-consequence orchestration, production shadow integration, lifecycle ownership and revision-bound planning.

### SDE-2A/B/C — COMPLETE / PR #144

Established:

1. Drunk shown-role / planned-clue ownership and replanning;
2. Spy/Recluse interaction-local registration witness binding;
3. Poisoner invalidation and broad replanning.

### SDE-2D — CURRENT / required before SDE-3

Authority:

`docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`

Execute:

```text
SDE-2D1  Drunk whole-bundle semantics
    ↓
SDE-2D2  Demon bluff joint-output strategic migration
    ↓
SDE-2D3  strategic-world quotient / exact feasibility seam
    ↓
SDE-2D4  5–15 player semantic + performance validation
    ↓
SDE-2D5  expert-observed calibration / policy evidence
```

Do not begin SDE-3 before this gate is complete.

### SDE-3 — cross-night information

Bring later impaired / registration decisions through the same engine using historical exact replay after SDE-2D establishes the corrected strategic representation.

### SDE-4 — production cutover / cleanup

- cut production callers to unified policy;
- preserve Experienced-mode manual override;
- migrate only useful legacy context inputs;
- delete `ConsequenceEvaluator` and stale heuristic-only state after fanout audit;
- retire `SetupRecommendationService` bluffDifficulty strategic authority once SDE bluff selection is the validated owner;
- retire superseded recommendation paths/tests when stronger typed coverage exists;
- reject role-specific/fixture-specific misinformation coherence code when the same behavior belongs to the shared narrative/history owner.

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
5. `docs/SDE_2D_PRE_SDE3_STRATEGIC_GENERALIZATION_ROUTE_2026-09-18.md`;
6. this route as architecture background;
7. query live `main` and current checks;
8. continue the current SDE-2D slice from the handoff.

Do not reopen completed FN-BUNDLE/SDE-0/SDE-1/SDE-2A/B/C work without a concrete regression.

## 23. Stable decision

> **The automatic Storyteller is a persistent strategic decision engine, not a collection of independent clue recommenders. Rules generate legal outcomes; the epistemic layer proves mechanical feasibility and hypothetical consequences; strategic evaluation operates primarily on evil-team topology while preserving role-information utility and narrative support. Impaired information uses a shared persistent narrative state so repeated/history-dependent outputs remain coherent with the player's perceived world; named roles provide legality/semantics, not separate misinformation policy. Demon bluff triplets are SDE outputs until shown, then persistent inputs. Large-player production must move toward exact strategic-topology quotient/constraint feasibility rather than giving repeated weight to exhaustive raw role permutations. SDE-2D is the current mandatory corrective route before SDE-3; no second world solver, no fixture-specific policy patching, and no opaque global scalar are allowed.**
