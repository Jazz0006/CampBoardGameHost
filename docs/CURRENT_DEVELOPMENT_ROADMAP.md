# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

Completed foundation relevant to the current program:

```text
D6 decomposition / ownership cleanup                  COMPLETE
Beginner / Experienced host modes                     COMPLETE
Same-night effective-state / transaction foundation   COMPLETE
EPI-MQ capability boundary                            COMPLETE / PR #135
Exact historical hypothetical bundle seam             COMPLETE / PR #137
First-night experiment contract                       COMPLETE / PR #138
FN-BUNDLE-0 candidate-space + pair ownership           COMPLETE / PR #139
FN-BUNDLE-1 proposition / ShownRoleAt semantics        COMPLETE / PR #140
FN-BUNDLE-2 healthy whole-bundle exact harness         COMPLETE / PR #142
```

Current product-code baseline after FN-BUNDLE-2 merge:

`690bc93b33b87fd54a911f4b9dbfc770f2a16a51`

Documentation-only architecture commits have advanced `main` beyond that product baseline. Always query live `main` before editing.

### CURRENT

**SDE-0 — BEGINNER strategic-robustness corpus + policy contract**

Active implementation is already underway in:

- PR #143 — `FN-BUNDLE-3: build BEGINNER review corpus`;
- branch `fn-bundle-3-beginner-corpus`;
- legacy FN-BUNDLE-3 naming maps directly to **SDE-0** and must not be restarted from zero.

Before additional implementation, synchronize PR #143 with current `main` if behind, then continue from its existing corpus / PUBLIC_GOOD_INFO work.

### NEXT

```text
SDE-1 — unified StorytellerDecisionEngine orchestration seam
SDE-2 — Drunk -> Spy/Recluse -> Poisoner first-night uncertainty
SDE-3 — cross-night impaired / registration decisions
SDE-4 — production cutover + legacy heuristic retirement
```

### DEFERRED / UNFROZEN

```text
NORMAL / EXPERT numeric thresholds
Narrative-complexity formula
Information-pacing curve
Exact Demon / evil-topology Badness thresholds
Exhaustive vs beam search for larger candidate products
Optional bounded soft preference / LLM critic
Pair-information display latency / old-device ADB diagnosis
```

Current architecture / product route:

`docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

Older first-night-only EPI-MQ and productive-uncertainty scoring plans are superseded and are not execution authority.

## 2. Product direction

The recommendation system is no longer a collection of independent per-role clue scorers.

The long-term target is a persistent automatic Storyteller decision engine:

```text
canonical GameState / interaction-time effective state
        ↓
rules-owned legal candidate outcomes
        ↓
InformationProposition / EpistemicObservation
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

The first target profile is BEGINNER / ordinary players, with ordinary / inexperienced evil players and a conservative `PUBLIC_GOOD_INFO` stress model.

## 3. Ownership boundaries

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
  -> presentation / confirmation / manual Experienced-mode override
```

Do not create a second rules engine or a second possible-world solver in recommendation.

## 4. Exact consequence model

`epistemic` remains the sole world-consequence authority.

Existing exact diagnostics already provide the important structure:

- exact BEFORE / AFTER world counts;
- possible Demon seats / Demon cover;
- distinct evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil cover;
- leave-one-out interaction evidence.

Raw full-role world count is descriptive only. Strategic evil topology is the primary product-level structure because cosmetic good-role permutations do not provide the same gameplay ambiguity as alternative Demon / Minion placements.

## 5. BEGINNER robustness goal

The target is not maximum information and not maximum uncertainty.

The policy must distinguish:

```text
BAD_TOO_WEAK
ACCEPTABLE
BAD_TOO_STRONG
UNCERTAIN
```

An acceptable result should simultaneously provide useful good information and preserve several understandable strategic worlds for an ordinary evil team.

Initial product semantics remain conceptually:

```text
hard legality
→ exact structural diagnostics
→ interpretable profile Badness gates
→ random selection among acceptable survivors
```

Do not reintroduce one opaque scalar whose maximum becomes the authoritative Storyteller choice.

## 6. Whole-bundle / whole-history interaction is mandatory

Local clue scoring is insufficient.

Retained adversarial design lessons include:

- Pair information + Fortune Teller can create strong confirmation chains;
- Red Herring placement can break or create those chains;
- Investigator + Chef + Empath can jointly collapse the evil topology even when every clue is individually normal;
- legal Recluse registration can restore large amounts of legitimate ambiguity;
- later poisoned/drunk Empath, Fortune Teller, Undertaker and Ravenkeeper information must be evaluated against the same accumulated history.

These must be discovered through exact consequences rather than hard-coded role-combination exceptions.

## 7. Registration and impairment

Spy / Recluse registration is **per interaction**, not a persistent global `good/evil` flag.

Rules own legal registration candidates. Storyteller policy chooses among legal registrations using whole-state exact consequences.

Poison / drunkenness is interaction-time effective state. If impairment disables a registration ability or changes information legality, that change must come from rules/effective-state semantics rather than recommendation hacks.

## 8. First-night decision lifecycle

Do not freeze one complete final Night-1 bundle before all player-controlled actions occur.

Use:

```text
PERSISTENT
    roles / seating / setup-level commitments
    Demon bluffs
    Fortune Teller Red Herring
    Drunk identity / shown-role commitments where required

COMMITTED
    already shown / executed; immutable

PLANNED / UNCOMMITTED
    not yet shown; may be invalidated and re-evaluated
```

After a Poisoner target becomes known:

```text
apply poison to effective state
→ keep PERSISTENT decisions
→ keep COMMITTED decisions
→ invalidate affected uncommitted planning
→ regenerate / re-evaluate the remaining relevant decision ecology
```

Do not only replace the poisoned role's one clue. Poisoning Spy / Recluse can alter other information semantics as well.

Correct broad re-evaluation is preferred over premature incremental optimization.

## 9. Cross-night engine scope

The same architecture continues beyond Night 1.

Initial Trouble Brewing later-interaction targets include:

- poisoned/drunk Empath numeric results;
- poisoned/drunk Fortune Teller YES/NO;
- poisoned/drunk Undertaker shown role;
- poisoned/drunk Ravenkeeper shown role;
- later Spy/Recluse registrations;
- other supported Storyteller-discretion decisions such as Mayor redirect / succession.

Per-role modules answer **what is legal**. Shared policy answers **which legal outcome is appropriate now**.

Historical exact replay is the consequence basis after Night 1.

## 10. Information pacing

A Day-1 robustness target cannot remain fixed throughout the game.

Early game should resist premature topology collapse; later game must allow legitimate convergence and eventual solving.

`information pacing` is therefore a required future policy dimension based on phase / round / alive count and possibly other durable state.

No numeric pacing curve is frozen yet.

## 11. ConsequenceEvaluator retirement

`recommendation/dynamic/ConsequenceEvaluator` is explicitly **legacy and targeted for removal**.

Do not add new product policy to it.

Its current repeated-target / one-shot / high-impact misinformation / final-day / `evilAdvantage` heuristics estimate consequences that the new architecture should measure directly using exact strategic diagnostics plus profile/phase policy.

Keeping both as independent authorities would recreate dual ownership.

Retirement route:

1. audit all callers and fanout;
2. identify useful context signals that are truly inputs rather than heuristic conclusions;
3. move those inputs into the unified DecisionContext / StorytellerPolicy boundary;
4. cut callers over to exact strategic robustness policy;
5. delete `ConsequenceEvaluator` once no unique contract depends on it;
6. audit `evilAdvantage`, `PublicBalanceHint`, information-pressure and related heuristic-only state for deletion or narrower non-authoritative use.

Do not delete it before safe cutover; the target state is removal, not permanent secondary scoring.

## 12. SDE-0 — current implementation program

PR #143 already provides valid SDE-0 groundwork. Do not repeat it.

Current retained contracts include:

- healthy `PUBLIC_GOOD_INFO` public claims do **not** equal Storyteller confirmation;
- an evil speaker may lie;
- a healthy good speaker's public claim must match shown role plus mechanically true clue;
- strict mechanically known `ShownRoleAt` remains exact;
- calibration and sealed holdout scenarios are separated by whole setup + seating scenario;
- corpus items start `UNREVIEWED`;
- deterministic review sampling preserves exact structural diagnostics and leave-one-out evidence;
- expensive calibration generation is an explicit experiment, not ordinary regression workload;
- no scalar quality score or automatic labels are introduced.

SDE-0 must now ensure deliberate adversarial coverage, especially:

1. Pair-information + Fortune Teller confirmation chains;
2. Red Herring choices that cut/create those chains;
3. Investigator + Chef + Empath evil-topology collapse;
4. the same structures with Recluse registration alternatives;
5. too-weak bundles;
6. representative healthy acceptable bundles.

Then:

1. human-review calibration items;
2. label `BAD_TOO_STRONG / ACCEPTABLE / BAD_TOO_WEAK / UNCERTAIN`;
3. determine which exact diagnostics actually separate labels;
4. define the smallest durable policy input/output seam;
5. derive first interpretable BEGINNER Badness gates;
6. keep the holdout sealed until gates are frozen enough to validate;
7. do not cut over production selection during SDE-0.

## 13. SDE-1 — unified orchestration seam

After SDE-0 evidence is credible:

- formalize a thin `StorytellerDecisionEngine` orchestration owner;
- reuse canonical candidate generators;
- reuse the existing exact epistemic evaluator;
- define DecisionContext and persistent / committed / planned lifecycle;
- preserve session mutation and flow ordering ownership;
- avoid a parallel state model.

## 14. SDE-2 — staged first-night uncertainty

Expand deliberately and separately:

1. Drunk;
2. Spy / Recluse per-interaction registration;
3. Poisoner target / effective-state invalidation and re-planning.

Each stage must be corpus-backed and preserve hidden-information boundaries.

## 15. SDE-3 — cross-night decisions

Route later impaired / registration decisions through the same engine using historical exact replay.

Add information-pacing behavior only from measured scenarios, not guessed thresholds.

## 16. SDE-4 — production cutover / cleanup

- cut production callers to the unified engine / policy;
- preserve Experienced-mode manual override UX;
- migrate only useful legacy context inputs;
- retire `ConsequenceEvaluator` and stale heuristic state after fanout audit;
- retire superseded recommendation paths/tests when stronger typed coverage exists.

## 17. Frozen vs unfrozen

### Frozen architecture/product decisions

- rules own legality;
- exact epistemic evaluator owns world consequences;
- strategic evil topology matters more than raw full-role world count;
- whole-bundle / whole-history interactions matter;
- Spy/Recluse registration is per interaction;
- Poisoner can invalidate uncommitted recommendations;
- one engine continues after Night 1;
- BEGINNER / ordinary-player profile is first;
- `ConsequenceEvaluator` is targeted for removal;
- no second rules engine;
- no second world solver;
- no opaque global-optimum scalar.

### Deliberately unfrozen

- exact Demon-candidate thresholds;
- exact evil-team-configuration thresholds;
- forced-good / forced-evil limits;
- narrative-complexity formula;
- information-pacing curve;
- NORMAL / EXPERT numeric profiles;
- exhaustive vs beam search;
- optional bounded soft-preference formula;
- production cutover timing.

## 18. Testing / acceptance

`AGENTS.md` and `docs/TESTING_STRATEGY.md` remain authoritative.

For SDE work:

- test stable contracts at their true typed ownership seam;
- exact consequence changes trigger appropriate epistemic/oracle validation;
- experimental corpus measurement does not require manufactured RED tests;
- stable corpus/gate contracts require durable regression evidence;
- central orchestration / shared semantic cutovers require broader T2/T4 validation;
- documentation-only main updates use the lightweight docs-only route.

## 19. New-conversation reading order

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
6. query live `main`, PR #143 and current CI;
7. synchronize #143 with latest `main` if necessary;
8. continue SDE-0 from existing PR #143 work.

Do not reopen FN-BUNDLE-0/1/2 audits without a concrete regression. Do not reload superseded EPI-MQ execution plans as authority.

## 20. Stable rule

> **The current program is SDE: a persistent Storyteller decision engine using canonical legal candidates plus the existing exact epistemic evaluator to measure strategically meaningful evil-world consequences. PR #143 is the active SDE-0 implementation, not disposable legacy work. SDE-0 calibrates BEGINNER strategic robustness before production cutover. `ConsequenceEvaluator` is a migration-era heuristic layer targeted for removal after unified-policy cutover.**
