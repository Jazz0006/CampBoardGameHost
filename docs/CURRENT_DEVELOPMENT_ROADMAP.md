# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-17 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition                               COMPLETE / merged
D6 decomposition campaign                         COMPLETE
UI-R5 square-table convergence                    COMPLETE / PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / PR #118
ROLE-ROTATION-1 recent role rotation              COMPLETE / PR #119
UX-MODE-1 Beginner / Experienced mode             COMPLETE / PR #120
UI-INFO-1 information filtering & layout          COMPLETE / PR #121
Dead-player square-table marking                  COMPLETE / PR #122
EXPERIENCED-NIGHT-FLOW-1 correctness              COMPLETE / PR #123
GLOBAL-OWNERSHIP-CLEANUP steps 1-6                COMPLETE / PRs #124-#129
EXPERIENCED-UI-1 compact TB host surfaces         COMPLETE / PR #130
EXPERIENCED-UI-2 night information surfaces       COMPLETE / PR #131
EXPERIENCED-UI-3 device-feedback refinements      COMPLETE / PR #132
EXPERIENCED-UI-4 poison + ranked recommendations  COMPLETE / PR #133
DAY-UI-1 centered daytime domain actions          COMPLETE / PR #134
EPI-MQ-0.5 epistemic capability boundary          COMPLETE / PR #135
Exact historical hypothetical bundle seam         COMPLETE / PR #137
First-night experiment contract                   COMPLETE / PR #138
FN-BUNDLE-0 candidate-space audit + pair cleanup  COMPLETE / PR #139
FN-BUNDLE-1 evaluator/shown-role semantics        COMPLETE / PR #140
FN-BUNDLE-2 healthy whole-bundle harness          COMPLETE / merged PR #142

CURRENT:
SDE-0 — BEGINNER strategic-robustness corpus + policy contract

NEXT:
SDE-1 — unified StorytellerDecisionEngine orchestration seam
SDE-2 — Drunk -> Spy/Recluse registration -> Poisoner first-night uncertainty
SDE-3 — cross-night impaired / registration decisions
SDE-4 — production cutover + legacy heuristic retirement

DEFERRED:
NORMAL / EXPERT numeric profile thresholds
Narrative-complexity formula
Information-pacing curve
Optional soft preference / LLM critic
Pair-information display latency / old-device ADB diagnosis
```

Live product baseline after FN-BUNDLE-2 merge:

`690bc93b33b87fd54a911f4b9dbfc770f2a16a51`

Documentation-only route commits may advance `main`; always query live `main` before editing.

Current architecture / product route:

`docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

Older EPI-MQ first-night / productive-uncertainty routes are no longer execution authority.

## 2. Product direction

The recommendation system is no longer treated as a set of independent per-role clue scorers.

The long-term target is a persistent automatic Storyteller decision engine:

```text
canonical GameState / effective interaction state
        ↓
rules-owned legal candidates
        ↓
InformationProposition / EpistemicObservation
        ↓
exact hypothetical epistemic evaluation
        ↓
strategic world-structure diagnostics
        ↓
profile / phase policy
        ↓
acceptable survivor pool
        ↓
selection + canonical commit
```

The first supported policy profile remains BEGINNER / ordinary players under a `PUBLIC_GOOD_INFO` stress assumption.

## 3. Exact consequence ownership

`epistemic` remains the sole world-consequence authority.

Current exact diagnostics already include:

- exact BEFORE / AFTER world counts;
- possible Demon seats / Demon cover;
- distinct evil-team seat configurations;
- forced-good seats;
- forced-evil seats;
- evil cover;
- leave-one-out interaction evidence.

Do not create a second strategic world solver in `recommendation`.

Raw full-role world count is descriptive only. Strategic evil topology is the main product-level structure.

## 4. BEGINNER robustness goal

The policy must reject both:

```text
BAD_TOO_STRONG
BAD_TOO_WEAK
```

and preserve an acceptable middle region where:

- good information has useful value;
- Demon / evil-team topology does not collapse prematurely;
- large trusted-good blocks do not form too early;
- ordinary evil players retain multiple understandable counterworlds.

Do not maximize possible-world count.

Do not use a single opaque scalar as the product authority.

Initial selection semantics remain:

```text
hard legality
→ exact structural diagnostics
→ interpretable Badness gates
→ random selection among acceptable survivors
```

## 5. Strategic-world definition

Worlds that differ only in cosmetic good-role permutations should not dominate recommendation quality.

Primary strategic diagnostics are based on:

```text
possible Demon seats
evil-team seat configurations
evil cover
forced-good seats
forced-evil seats
```

A large raw world family with one surviving evil topology is strategically fragile.

## 6. Whole-bundle / whole-history interaction

Local clue scores are insufficient.

Retained adversarial design lessons include:

- pair-information + Fortune Teller can form confirmation chains;
- Red Herring placement can break or create those chains;
- Investigator + Chef + Empath can jointly collapse the evil topology even when each clue is individually normal;
- Recluse registration can restore legitimate ambiguity;
- the same exact-consequence framework must later evaluate impaired Empath / Fortune Teller / Undertaker / Ravenkeeper information.

The algorithm should discover these consequences from the world model rather than from hard-coded role-combination exceptions.

## 7. Registration and impairment decisions

Spy / Recluse registration is interaction-specific, not a global persistent `good/evil` flag.

Rules own the legal registration candidate set. Storyteller policy chooses among legal registrations using global exact consequence evidence.

Poison / drunkenness must be represented at interaction-time effective state. If impairment disables a special registration, legality removes it rather than recommendation policy compensating afterward.

## 8. First-night decision lifecycle

Do not freeze every first-night recommendation before player-controlled actions occur.

Use lifecycle semantics:

```text
PERSISTENT
    setup-level commitments such as actual roles, seating, Demon bluffs,
    Red Herring and other decisions that must persist

COMMITTED
    already shown / executed decisions; immutable

PLANNED / UNCOMMITTED
    recommendations that may be invalidated and re-evaluated
```

When the Poisoner target becomes known, rebuild the effective night state and re-evaluate all relevant uncommitted decisions. Do not only replace the poisoned role's one information result.

Poisoning Spy / Recluse can change information semantics even when the target itself is not a normal information role.

## 9. Cross-night engine scope

The same architecture must eventually handle later interactions:

- poisoned / drunk Empath;
- poisoned / drunk Fortune Teller;
- poisoned / drunk Undertaker;
- poisoned / drunk Ravenkeeper;
- later Spy / Recluse registrations;
- other Storyteller-discretion decisions such as Mayor redirect or Demon succession where supported.

Per-role code generates legal outcomes. Shared policy evaluates global consequences.

Historical exact replay is the required consequence basis after Night 1.

## 10. Information pacing

The acceptable information-convergence region changes over the game.

Early-game policy should protect against premature collapse; later-game policy must allow legitimate convergence and eventual solving.

`information pacing` is therefore a required future policy dimension.

No numeric phase curve is frozen yet.

## 11. ConsequenceEvaluator retirement

`recommendation/dynamic/ConsequenceEvaluator` is now explicitly **legacy / targeted for retirement**.

Do not add new product policy to it.

Its heuristic concepts such as repeated-target pressure, one-shot protection, high-impact misinformation, final-day penalty and `evilAdvantage` adjustment should not remain a second authority beside exact strategic consequence evaluation.

Retirement route:

1. identify useful context signals that are genuinely independent inputs;
2. move those inputs into the unified decision context / policy seam;
3. cut callers over to exact strategic robustness policy;
4. delete `ConsequenceEvaluator` when no unique contract remains;
5. audit `evilAdvantage`, `PublicBalanceHint`, information-pressure fields and other heuristic-only state for retirement or narrower use.

Do not delete it before safe fanout/caller migration.

## 12. Frozen vs unfrozen decisions

### Frozen

- rules own legality;
- exact epistemic evaluator owns possible-world consequences;
- strategic evil topology matters more than raw role-world count;
- whole-bundle / whole-history interaction is required;
- Spy/Recluse registration is per interaction;
- Poisoner can invalidate uncommitted recommendations;
- one engine continues after Night 1;
- BEGINNER / ordinary-player policy is first;
- `ConsequenceEvaluator` is targeted for removal;
- no second rules engine;
- no second possible-world solver;
- no opaque global-optimum scalar.

### Not frozen

- exact Demon-candidate thresholds;
- exact evil-team-configuration thresholds;
- forced-good / forced-evil limits;
- narrative-complexity formula;
- information-pacing curve;
- NORMAL / EXPERT thresholds;
- exhaustive vs beam search for large candidate products;
- optional soft-preference formula;
- final production cutover timing.

## 13. SDE-0 — current task

SDE-0 replaces the old `FN-BUNDLE-3` label while preserving its valid corpus-first intent.

Goals:

1. use merged FN-BUNDLE-2 healthy whole-bundle harness as baseline;
2. build a human-reviewed BEGINNER strategic-robustness corpus;
3. include deliberate adversarial confirmation-chain / topology-collapse examples;
4. validate which existing exact structural diagnostics separate `BAD_TOO_STRONG`, `ACCEPTABLE`, `BAD_TOO_WEAK`, `UNCERTAIN`;
5. define the smallest durable profile-policy input/output contract;
6. derive first interpretable Badness gates only after corpus review;
7. keep production selection unchanged during SDE-0.

Corpus must include calibration and holdout sets.

## 14. SDE-1 — unified orchestration seam

After SDE-0 policy evidence is credible:

- introduce / formalize `StorytellerDecisionEngine` as a thin orchestration owner;
- reuse canonical candidate producers;
- reuse exact epistemic evaluation;
- define decision context and persistent/committed/planned lifecycle;
- preserve session mutation and flow ordering ownership;
- avoid a new state engine or new rules engine.

## 15. SDE-2 — staged first-night uncertainty

Expand deliberately:

1. Drunk;
2. Spy / Recluse per-interaction registration;
3. Poisoner target effects, effective-state invalidation and re-planning.

Each stage remains corpus-backed and must preserve hidden-information boundaries.

## 16. SDE-3 — later-night dynamic decisions

Route later impaired / special-registration information through the same engine using historical exact replay.

Initial Trouble Brewing targets should include Empath, Fortune Teller, Undertaker and Ravenkeeper.

Add information-pacing policy only from measured scenarios, not guessed thresholds.

## 17. SDE-4 — production cutover / cleanup

- cut production decision callers to the unified engine/policy;
- preserve Experienced-mode manual override UX;
- migrate useful legacy context;
- retire `ConsequenceEvaluator` and dead heuristic state after fanout audit;
- retire superseded recommendation paths and tests when stronger typed coverage exists.

## 18. Architecture ownership

```text
rules
  -> legal information / registration semantics

session
  -> canonical actual state / timeline / commit authority

flow
  -> interaction ordering / projection; consumes resolved facts

epistemic
  -> recipient-visible knowledge / exact hypothetical consequences

recommendation / StorytellerDecisionEngine
  -> compose legal options
  -> consume exact diagnostics
  -> apply profile / phase policy
  -> select acceptable outcome

UI
  -> presentation / confirmation / manual override
```

Badness/policy code must not become a rules engine.

## 19. Testing / acceptance

`AGENTS.md` and `docs/TESTING_STRATEGY.md` remain authoritative.

For SDE work:

- behavior / policy contracts should be typed at their true owner;
- exact consequence changes trigger relevant epistemic/oracle validation;
- exploratory corpus measurement does not need manufactured RED tests;
- stable corpus/gate contracts do require durable tests before acceptance;
- central orchestration / shared semantic cutovers require broader T2/T4 validation;
- documentation-only main updates use the existing lightweight docs-only CI route.

## 20. New-conversation reading order

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
6. query live `main`, open PRs and current checks;
7. continue from SDE-0 only.

Do not reload superseded EPI-MQ execution plans as authority.

## 21. Stable rule

> **FN-BUNDLE-0/1/2 are complete foundations. The current program is SDE: a persistent Storyteller decision engine using canonical legal candidates plus the existing exact epistemic evaluator to measure strategically meaningful evil-world consequences. SDE-0 calibrates BEGINNER robustness before production cutover. `ConsequenceEvaluator` is a legacy heuristic layer targeted for retirement after unified-policy migration.**
