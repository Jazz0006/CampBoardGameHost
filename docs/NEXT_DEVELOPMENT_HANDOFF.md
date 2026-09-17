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

Do not use removed/superseded EPI-MQ / first-night-only route documents as execution authority. Their history remains available in Git.

## 1. Live continuation point

Relevant completed foundation:

```text
FN-BUNDLE-0   COMPLETE / PR #139
FN-BUNDLE-1   COMPLETE / PR #140
FN-BUNDLE-2   COMPLETE / PR #142
```

Current active implementation:

- PR #143;
- branch `fn-bundle-3-beginner-corpus`;
- legacy title began as `FN-BUNDLE-3: build BEGINNER review corpus`;
- program mapping: **PR #143 is the active SDE-0 implementation**;
- latest observed PR head before this documentation closeout: `b41547660645114b0178b6ac365f5e6278872f18`;
- current documentation `main` has advanced beyond the PR base.

**First action in the next development conversation:** query live refs and synchronize PR #143 with latest `main` before additional executable edits if it is still behind.

Do not discard or restart PR #143. Do not merge it without explicit user authorization.

## 2. What PR #143 already establishes

Do not repeat these parts without a concrete defect.

### Healthy PUBLIC_GOOD_INFO claim semantics

Public speech is not Storyteller confirmation.

Healthy-stage public claims are modeled conceptually as:

```text
speaker is evil
OR
(shown role matches claim AND claimed clue is mechanically true)
```

Consequences:

- evil speakers retain bluff worlds;
- healthy good speakers are truthful under this stress model;
- strict mechanically known `ShownRoleAt` remains exact;
- public-claim permissiveness must not weaken actual Storyteller-shown facts.

### Corpus / holdout contract

PR #143 already contains:

- CALIBRATION scenarios;
- a sealed replacement HOLDOUT scenario;
- partitioning by whole setup + seating scenario, not sibling signatures;
- `UNREVIEWED` initial labels;
- deterministic sampling across structural extremes / quartiles;
- retained raw public claims, provenance multiplicity, exact diagnostics and leave-one-out evidence;
- no scalar quality score;
- no automatic labels.

The holdout must remain sealed while deriving gates.

### Experiment / regression boundary

The expensive calibration generator is an explicit experiment, not an ordinary regression test.

Keep the intended split:

- lightweight typed tests protect durable corpus contracts;
- exact corpus generation runs through the dedicated calibration task/workflow;
- ordinary FAST / bounded FULL regression must not regenerate the expensive calibration corpus by default.

Do not reverse this separation.

## 3. Latest observed PR #143 validation

At head `b41547660645114b0178b6ac365f5e6278872f18`, the latest observed runs were green:

- R2 main-thread boundary — success;
- CI — success;
- FN-BUNDLE-3 calibration workflow — success.

PR history also contains a prior full validation checkpoint with Android full + assemble, ASP and Real Clingo green.

After syncing with current `main`, use the actual diff and `TESTING_STRATEGY.md` to determine required validation. Old-head evidence does not automatically validate a new executable head.

## 4. Architectural route now governing the work

The long-term target is no longer a first-night-only clue recommender.

Conceptual owner:

```text
StorytellerDecisionEngine
```

Authority split:

```text
rules
  -> legal outcomes / registration legality

session
  -> canonical actual state / timeline / commit

flow
  -> interaction ordering / projection

epistemic
  -> recipient-visible exact hypothetical consequences
  -> strategic structural diagnostics
  -> historical replay

recommendation / StorytellerDecisionEngine
  -> compose legal candidates
  -> consume exact diagnostics
  -> apply profile / phase policy
  -> choose among acceptable outcomes

UI
  -> display / confirmation / manual Experienced-mode override
```

Do not create a second rules engine, a second state authority, or a second possible-world solver.

## 5. Strategic robustness model

Raw exact world count is not the product objective.

Primary structure includes:

- possible Demon seats;
- distinct Demon/Minion seat configurations;
- evil cover;
- forced-good seats;
- forced-evil seats;
- leave-one-out / interaction recovery.

Worlds that differ only by cosmetic healthy-good role permutations should not dominate quality assessment.

Target categories remain:

```text
BAD_TOO_WEAK
ACCEPTABLE
BAD_TOO_STRONG
UNCERTAIN
```

The first profile assumes ordinary / inexperienced evil players plus aggressive healthy-good public sharing.

Do not maximize uncertainty and do not search for one opaque scalar optimum.

## 6. Required SDE-0 adversarial evidence

PR #143 must be extended so calibration deliberately includes interaction-collapse cases, not only representative/random bundles.

Required benchmark families:

1. Pair-information + Fortune Teller confirmation chain.
2. Red Herring placements that create/cut those confirmation paths.
3. Investigator + Chef + Empath combinations where individually normal clues jointly collapse evil topology.
4. Equivalent structures where legal Recluse registration restores meaningful ambiguity.
5. Too-weak bundles that preserve uncertainty but give little strategic value.
6. Clearly acceptable healthy examples for contrast.

The evaluator should detect these from exact consequences. Do not implement hard-coded rules such as `if Investigator + Chef + Empath then penalty`.

## 7. Registration decision frozen architecture

Spy / Recluse registration is **per interaction**.

Do not model persistent globals such as:

```text
Recluse = evil
Spy = good
```

Rules generate legal registration candidates for each observing interaction. Future Storyteller policy chooses among them using exact global consequences.

If poison/drunkenness disables special registration at that interaction, effective-state legality removes the candidate.

## 8. Poisoner / first-night lifecycle frozen architecture

Do not freeze all first-night information before player-controlled state changes.

Use:

```text
PERSISTENT
    actual roles / seating / setup commitments
    Demon bluffs
    Fortune Teller Red Herring
    other persistent decisions

COMMITTED
    already shown / executed; immutable

PLANNED / UNCOMMITTED
    may be invalidated and re-evaluated
```

After Poisoner chooses a target:

```text
apply effective poison
→ preserve PERSISTENT
→ preserve COMMITTED
→ invalidate affected uncommitted planning
→ re-evaluate remaining relevant decision ecology
```

Do not only replace the poisoned player's single clue. Poisoning Spy / Recluse can change other information semantics too.

Broad correct re-evaluation is preferred before incremental optimization.

## 9. Cross-night scope frozen architecture

The same engine later handles legal discretionary outcomes for:

- poisoned/drunk Empath;
- poisoned/drunk Fortune Teller;
- poisoned/drunk Undertaker;
- poisoned/drunk Ravenkeeper;
- later Spy/Recluse registration;
- supported Mayor redirect / succession and similar Storyteller choices.

Per-role code owns legal outcomes. Shared policy owns selection among legal outcomes.

Historical exact replay is the consequence baseline after Night 1.

## 10. Information pacing

Early game must resist premature collapse; late game must permit legitimate convergence.

`information pacing` is a required future policy dimension.

Do not invent a numeric phase curve during SDE-0.

## 11. ConsequenceEvaluator retirement decision

`recommendation/dynamic/ConsequenceEvaluator` is legacy and **targeted for deletion** after unified-policy cutover.

Do not add new policy to it.

Future route:

1. audit its callers/fanout;
2. preserve only context signals that are genuine inputs;
3. migrate those inputs to DecisionContext / StorytellerPolicy;
4. cut callers to exact strategic robustness policy;
5. delete `ConsequenceEvaluator` when no unique contract remains;
6. audit `evilAdvantage`, `PublicBalanceHint`, information-pressure and related heuristic-only state for deletion/narrower use.

The target is removal, not a permanent secondary scoring layer.

## 12. What is deliberately not frozen

Do not decide these from intuition alone:

- exact Demon-candidate thresholds;
- exact evil-team configuration thresholds;
- forced-good / forced-evil limits;
- narrative-complexity formula;
- information-pacing curve;
- NORMAL / EXPERT numeric profiles;
- exhaustive vs beam search for larger products;
- optional soft-preference formula;
- production cutover timing.

These require corpus evidence.

## 13. Immediate SDE-0 execution order

After syncing PR #143 with current `main`:

1. audit current PR #143 diff and retain all valid existing corpus/public-claim work;
2. add the missing adversarial benchmark scenarios above at the true corpus/evaluator ownership seams;
3. run the dedicated calibration experiment and inspect exact structural outputs;
4. construct a manageable human-review calibration set;
5. label calibration items `BAD_TOO_STRONG / ACCEPTABLE / BAD_TOO_WEAK / UNCERTAIN`;
6. identify which existing exact diagnostics separate labels and where diagnostics are insufficient;
7. define the smallest durable BEGINNER policy input/output contract;
8. derive interpretable candidate Badness gates only after review;
9. keep HOLDOUT sealed until the gates are sufficiently fixed;
10. validate on holdout and review false accepts / false rejects;
11. do **not** perform production selector cutover during SDE-0.

## 14. Testing rules

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Especially:

- exploratory measurement does not need manufactured RED tests;
- durable new policy contracts do need typed regression evidence at the true owner;
- exact epistemic semantic changes trigger relevant oracle / broader validation;
- central orchestration/shared semantic cutovers require broader T2/T4 evidence;
- do not bring the expensive calibration experiment back into normal regression execution.

## 15. Cleanup state

Current active docs are now centered on:

- `CURRENT_DEVELOPMENT_ROADMAP.md`;
- `NEXT_DEVELOPMENT_HANDOFF.md`;
- `STORYTELLER_DECISION_ENGINE_ROUTE_2026-09-17.md`;
- `TESTING_STRATEGY.md`;
- `AGENTS.md`.

Superseded first-night-only EPI-MQ / productive-uncertainty / old dynamic-engine execution plans were removed from active `docs/`; Git history preserves them for archaeology.

Audits and rule/ownership documents that retain independent factual value remain available as on-demand references.

## 16. Stable handoff

> **Start the next development conversation by reading the five current authority documents, query live `main` and PR #143, synchronize #143 with `main` if behind, then continue SDE-0 from the existing PR rather than recreating FN-BUNDLE work. The immediate engineering objective is corpus-backed BEGINNER strategic-robustness policy evidence. Exact epistemic consequences remain authoritative; Spy/Recluse registration is per-interaction; Poisoner may invalidate uncommitted plans; the same engine extends across nights; and `ConsequenceEvaluator` is targeted for retirement rather than preservation.**
