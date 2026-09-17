# NEXT DEVELOPMENT HANDOFF — SDE-0 Strategic-Robustness Calibration

> Updated: 2026-09-17 Australia/Sydney  
> Status: **CURRENT / continue PR #143 as SDE-0**  
> Current architecture route: `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
6. query live `main`, PR #143 and current checks before editing.

Do not use superseded EPI-MQ / first-night-only route documents as execution authority.

## 1. Current repository state

FN-BUNDLE-0, FN-BUNDLE-1 and FN-BUNDLE-2 are complete foundations.

Merged baseline after FN-BUNDLE-2:

`690bc93b33b87fd54a911f4b9dbfc770f2a16a51`

Documentation-only architecture commits subsequently advanced `main`. Always query the live ref before editing.

Current active implementation PR:

- PR #143 — `FN-BUNDLE-3: build BEGINNER review corpus`
- branch: `fn-bundle-3-beginner-corpus`
- current observed head: `b41547660645114b0178b6ac365f5e6278872f18`
- conceptual program name going forward: **SDE-0**

PR #143 predates the SDE naming pivot. Do **not** discard or restart it. Its valid corpus-first work is the implementation already underway for SDE-0.

The branch must be synchronized with the latest `main` documentation/route commits before further implementation if it is behind.

Do not merge PR #143 without explicit user authorization.

## 2. What PR #143 already establishes

Do not repeat these parts unless a concrete defect is found.

### Healthy PUBLIC_GOOD_INFO claim semantics

Public speech is not Storyteller confirmation.

The healthy-stage model is conceptually:

```text
speaker is evil
OR
(shown role matches claim AND claimed clue is mechanically true)
```

This preserves evil bluff counterworlds while keeping healthy good speakers truthful.

Strict mechanically known `ShownRoleAt` remains exact. Do not weaken it merely because public claims are modeled more permissively.

### Corpus structure

PR #143 already contains:

- CALIBRATION scenarios;
- a sealed HOLDOUT scenario;
- partitioning by whole setup + seating scenario, not sibling signatures;
- `UNREVIEWED` initial labels;
- deterministic review sampling across structural extremes and quartiles;
- raw public claims, multiplicity, exact diagnostics and leave-one-out evidence;
- no scalar quality score;
- no invented automatic labels.

The holdout must remain sealed while deriving gates.

### Experiment / regression boundary

The expensive calibration generator is an explicit experiment, not an ordinary regression test.

Current intended shape:

- lightweight typed tests protect durable corpus contracts;
- the expensive exact corpus experiment runs only through the dedicated calibration task/workflow;
- ordinary `testFast`, bounded `testFull`, and default debug unit tests do not regenerate the corpus.

Do not move the expensive experiment back into the normal full regression path.

## 3. Current validation state

For PR #143 head `b41547660645114b0178b6ac365f5e6278872f18`, the latest observed workflow runs are green:

- R2 main-thread boundary — success;
- CI — success;
- FN-BUNDLE-3 calibration workflow — success.

The PR body also records a prior full validation checkpoint with Android full + assemble, ASP and Real Clingo green on an earlier accepted head.

After syncing the branch with current `main`, rerun the validation required by the actual diff and `TESTING_STRATEGY.md`; do not assume old-head evidence applies to a new executable head.

## 4. Architectural pivot that now governs PR #143

The implementation target is no longer a first-night-only recommendation system.

The long-term owner is conceptually:

```text
StorytellerDecisionEngine
```

with the following authority split:

```text
rules
  -> legal outcomes / registration legality

session
  -> canonical actual state / timeline / commit

flow
  -> interaction ordering / projection

epistemic
  -> recipient-visible exact hypothetical consequences
  -> strategic world-structure diagnostics

recommendation / StorytellerDecisionEngine
  -> compose legal options
  -> consume exact diagnostics
  -> apply profile / phase policy
  -> choose among acceptable outcomes

UI
  -> display / confirmation / manual override
```

Do not create a second rules engine or a second possible-world solver.

## 5. Core SDE policy model

Raw exact world count is not the main product metric.

The policy must emphasize strategically meaningful evil structure:

- possible Demon seats;
- distinct Demon/Minion seat configurations;
- evil cover;
- forced-good seats;
- forced-evil seats;
- whole-bundle / whole-history interaction evidence.

The target region is neither maximum information nor maximum uncertainty:

```text
BAD_TOO_WEAK
ACCEPTABLE
BAD_TOO_STRONG
UNCERTAIN
```

For the first profile, assume ordinary / inexperienced evil players and aggressive good public sharing.

Mathematically possible counterworlds are not sufficient if they require implausibly complex stacks of exceptions. Narrative viability / narrative complexity is a future policy dimension, but its numeric formula is not frozen.

## 6. Required adversarial evidence

SDE-0 must deliberately include cases that expose interaction collapse rather than only random representative bundles.

Important benchmark families from design review:

1. Pair information + Fortune Teller confirmation chains.
2. Red Herring placements that cut or create those chains.
3. Investigator + Chef + Empath combinations where individually normal clues jointly collapse the evil topology.
4. The same structures with Recluse registration restoring ambiguity.
5. Too-weak bundles that preserve uncertainty but provide little useful information.

Later staged corpus expansion must cover Drunk, Spy/Recluse, Poisoner and later-night impaired information, but do not mix all uncertainty sources into the first healthy calibration slice at once.

## 7. Spy / Recluse registration decision

Registration is per interaction, not a persistent global boolean.

Do not model:

```text
Recluse = evil
Spy = good
```

as permanent state.

Rules generate all legal interaction-specific registration candidates. The future Storyteller policy chooses among them using exact strategic consequences.

If poison/drunkenness disables a special registration ability at interaction time, effective-state legality must remove that candidate.

## 8. Poisoner / first-night lifecycle decision

A final first-night recommendation bundle must not be frozen before player-controlled state changes such as Poisoner targeting.

Use lifecycle semantics:

```text
PERSISTENT
    setup-level commitments: roles, seats, bluffs, Red Herring, etc.

COMMITTED
    already shown / executed; immutable

PLANNED / UNCOMMITTED
    may be invalidated and re-evaluated
```

After Poisoner chooses a target:

```text
apply effective poison
→ keep PERSISTENT decisions
→ keep COMMITTED decisions
→ invalidate affected uncommitted planning
→ re-evaluate the remaining relevant decision ecology
```

Do not only replace the poisoned player's one clue. Poisoning Spy/Recluse can also change other information semantics.

Correct full re-evaluation is acceptable before attempting incremental optimization.

## 9. Cross-night scope

The same engine must later evaluate discretionary outcomes for:

- poisoned/drunk Empath;
- poisoned/drunk Fortune Teller;
- poisoned/drunk Undertaker;
- poisoned/drunk Ravenkeeper;
- later Spy/Recluse registration;
- other legal Storyteller choices such as Mayor redirect / Demon succession where applicable.

Use existing historical exact replay. First night is the calibration surface, not the architecture boundary.

Information pacing must eventually vary by game phase: early game protects against premature collapse; late game must allow legitimate solving. No numeric pacing curve is frozen yet.

## 10. ConsequenceEvaluator retirement

`recommendation/dynamic/ConsequenceEvaluator` is legacy and is targeted for removal.

Do not add new product policy to it.

Its current heuristics must not remain a second authority beside exact strategic consequence evaluation.

Migration sequence:

1. fanout-audit callers and unique contracts;
2. identify context signals that remain useful inputs;
3. move those inputs to the unified decision context / policy boundary;
4. cut callers to the new exact robustness policy;
5. delete `ConsequenceEvaluator` once safe;
6. audit `evilAdvantage`, `PublicBalanceHint`, information-pressure fields and other heuristic-only state for retirement or narrower use.

Do not delete it prematurely during SDE-0 calibration.

## 11. Frozen vs unfrozen decisions

### Frozen

- exact epistemic evaluator is the sole possible-world consequence authority;
- strategic evil topology matters more than raw role-permutation count;
- whole-bundle / whole-history interactions matter;
- rules own legality;
- Spy/Recluse registration is interaction-specific;
- Poisoner may invalidate uncommitted recommendations;
- one decision architecture continues beyond Night 1;
- BEGINNER / ordinary-player profile is first;
- `ConsequenceEvaluator` is targeted for retirement;
- no second rules engine;
- no second world solver;
- no opaque global-optimum scalar.

### Not frozen

- exact Demon-cover thresholds;
- exact evil-team-configuration thresholds;
- forced-good / forced-evil thresholds;
- narrative-complexity formula;
- information-pacing curve;
- NORMAL / EXPERT numeric thresholds;
- exhaustive versus beam search for larger products;
- bounded soft preference;
- production cutover timing.

Do not encode these as product constants during SDE-0 without corpus evidence.

## 12. Immediate next task

In the next development conversation:

1. read the authority files in the order in section 0;
2. query live `main`, PR #143 and checks;
3. synchronize `fn-bundle-3-beginner-corpus` with current `main` if needed;
4. audit the existing PR #143 corpus against the new SDE-0 benchmark requirements;
5. preserve all valid existing corpus/public-claim work;
6. add only the missing adversarial calibration cases and the smallest durable policy contract needed to consume existing exact diagnostics;
7. keep holdout sealed;
8. do **not** invent final thresholds yet;
9. do **not** start SDE-1 orchestration cutover until SDE-0 evidence is reviewed and accepted.

## 13. Stable handoff

> **Continue PR #143 as SDE-0; do not restart FN-BUNDLE-3. Synchronize it with the current architecture-route documentation, then use the existing exact epistemic diagnostics plus adversarial human-reviewed corpus evidence to define an interpretable BEGINNER strategic-robustness policy contract. Preserve rules/session/flow/epistemic ownership, keep the holdout sealed, and make no production selector cutover yet.**