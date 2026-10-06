# GSP — General Storyteller Policy / Global Recommendation Route — 2026-10-05

> Repository: `Jazz0006/CampBoardGameHost`
>
> Status: **CURRENT RECOMMENDATION-LANE EXECUTION AUTHORITY — GSP-0 ROUTE RESET**
>
> Product decision: preserve CampBoardGameHost as an **offline-first Android authoritative Host**. Network/LLM capability is optional enhancement, never a gameplay dependency.
>
> Supersedes as current execution authority: archived `docs/archive/LRE_STAGED_POLICY_REPLACEMENT_AND_RETIREMENT_ROUTE_2026-10-04.md` family-by-family special-policy replacement loop.
>
> Preserves: LRE-0 legacy-heuristic retirement verdict, LRE-1 Manual/fail-closed safety gate, TBGS, rules-owned legal domains, DecisionTrace/replay, epistemic/consequence diagnostics, HOST-ML0/1 neutral export, evidence provenance, and complete Manual gameplay.

## 1. Why the route changes

The original product goal is whole-game Storyteller reasoning: recommendations should account for interacting information, registrations, bluff structure, player experience, public narrative, history and downstream choices.

The recent implementation path narrowed that goal into evidence-bounded named policies. A trusted case or expert statement was converted into a predicate and then into a deterministic production selector. This produced software that was reproducible and testable, but it introduced the wrong abstraction:

```text
expert case
-> bounded predicate
-> named deterministic policy
-> matching state selects one fixed route
```

A real Storyteller example proves that a consideration can matter. It does not prove that every future state matching a few predicates should take the same action.

The product therefore stops treating individual examples as executable policy templates.

From this route forward:

> **Expert experience is evidence / context / evaluation material, not predicate => answer authority.**

No new Storyteller recommendation family should be implemented by encoding one observed case, podcast statement, reviewer preference or narrow topology as a production auto-selector.

## 2. What remains valuable

This route does **not** undo the core architecture built before the policy-layer correction.

Preserve and continue using:

- canonical session truth and `TroubleBrewingGameSnapshotV1`;
- rules-owned complete legal candidate domains;
- deterministic rule calculations and forced outcomes;
- typed decision contexts;
- Drunk late-binding and latest-safe commitment barriers;
- registration witnesses and effective-state semantics;
- exact hypothetical / PlayerWorldSet / epistemic / strategic-topology analysis;
- consequence diagnostics and whole-bundle evaluation when they describe effects rather than declare a winner;
- history-prefix identity and cross-game structured history;
- `DecisionTrace`, replay, stable candidate identity and provenance;
- `RecommendationDecisionExportV1` and HOST-ML leakage boundaries;
- Manual selection/presentation for every multi-choice Storyteller decision.

These components answer questions such as:

- what is legal?
- what is true?
- what candidate space exists?
- what would a candidate structurally change?
- what information would different players receive?
- what history/context is available?

They must not be deleted merely because hand-written final ranking is being retired.

## 3. What is no longer the product target

The following are no longer a long-term route:

- family-by-family conversion of expert examples into named V1/V2 deterministic production policies;
- hand-authored weighted scoring as final Storyteller authority;
- fixed probability budgets used as Storyteller quality policy;
- one matched case forcing the same answer every time;
- role-by-role local optimization without whole-game interaction;
- treating a historical expert choice as proof that every legal unchosen alternative was worse.

Existing special-policy code can remain temporarily for replay/benchmark comparison, but it is not the architecture to extend.

## 4. Current executable reality vs target state

This document is a route reset. **GSP-0 is documentation-only and does not itself change production behavior.**

At the current `main` baseline, previously accepted special-policy islands still exist and may still have automatic authority, including:

- `DRUNK_ASSIGNMENT_Q04_V1`;
- functioning Librarian V2 production selection;
- the merged INV1-A functioning-Investigator cutover from PR #222.

PR #223 only attempted documentation closure for INV1-A under the now-superseded LRE-P route and was closed without merge during GSP-0 cleanup. It is not current execution authority.

The next executable phase, GSP-1, is responsible for auditing and revoking these special-case automatic authorities safely.

Historical acceptance remains valid as evidence that the code behaved as designed at those checkpoints. GSP does not rewrite that history; it changes which behavior should remain product authority.

## 5. Production authority invariant

The LRE-1 fail-closed safety rule remains the foundation.

Target authority after GSP-1:

```text
exactly one rules-legal outcome
-> RULE_DETERMINISTIC
-> automatic resolution allowed

multiple legal outcomes
-> no trusted general Policy Provider
-> MANUAL_REQUIRED
-> expose the complete legal domain

multiple legal outcomes
-> trusted general Policy Provider available
-> provider may rank / recommend / explain
-> Host validates returned candidate identity against current legal domain
-> Storyteller remains confirmation authority
```

A provider failure, timeout, unavailable network or missing model must never make a legal game action unavailable.

Offline gameplay remains complete.

## 6. General Storyteller Policy Provider

`Policy` changes meaning.

It no longer means “a named hard-coded route for one special case.” It means a replaceable reasoner over Host-owned state and Host-owned legal candidates.

Conceptual provider family:

```text
StorytellerPolicyProvider
  +-- Manual
  +-- optional Remote LLM
  +-- future trained model
  +-- future on-device model
```

The exact production type/API is not frozen by GSP-0.

A provider may:

- compare legal candidates;
- rank or recommend;
- propose a coordinated bundle;
- explain interactions, risks and alternatives;
- report uncertainty or request missing context.

A provider may not:

- invent legal candidates;
- override rules;
- mutate canonical Game State;
- commit identity, registration, death, poison, drunkenness or information;
- bypass latest-safe commitment barriers;
- turn later history/outcomes into recommendation input for an earlier historical decision.

The Host validates provider output before it reaches any authoritative commit path.

## 7. Decision-specific global context

The solution is not an unbounded universal “God object”, and it is not isolated per-role scoring.

Each recommendation request should contain:

```text
canonical pre-decision state
+ complete legal domain for the current decision
+ required typed facts
+ explicit optional/enrichment facts
+ relevant public/private information prefix
+ player-experience context
+ relevant Storyteller/group history
+ bounded coordination horizon
```

The **coordination horizon** is important.

For example, a first-night Drunk decision may need to reason jointly about:

- Drunk candidate;
- resulting information options;
- Investigator/Washerwoman/Librarian interactions;
- Recluse/Spy registration possibilities;
- Fortune Teller Red Herring;
- Demon bluff candidates;
- likely public narrative and player experience.

The model can propose a coordinated plan, while each eventual commitment still passes through the correct rules-owned decision boundary at the correct time.

## 8. EvidenceLab relationship

EvidenceLab remains valuable, but its primary role changes.

High-value evidence is:

- complete real games;
- reconstructable pre-decision state;
- actual Storyteller choice;
- legal alternatives;
- explicit rationale/comparisons when available;
- rejected alternatives and reasons;
- player experience;
- public claims/narrative;
- later outcome only as evaluation metadata, never leaked into historical input.

Podcast/expert principles remain useful as reasoning knowledge.

They should not automatically become executable Host predicates.

A source saying “A is often useful” is evidence that a general reasoner should consider A in context, not a command to implement `IF X THEN A`.

## 9. Benchmark before API

API integration is **not** required to test this route.

Before adding any production network dependency, use frozen benchmark cases in clean independent LLM sessions.

Initial experiment path:

### GSP-3A — manual blind benchmark

- freeze case input;
- keep human evaluator/reference notes separate;
- run the same prompt in clean independent sessions/models;
- preserve raw outputs;
- validate proposed actions against Host rules/legal domains where available;
- score reasoning quality, global interaction, player-level calibration, solvability, alternatives and uncertainty;
- do not require exact match to one human answer.

### GSP-3B — repeated / cross-model comparison

Test whether quality is stable across:

- different clean runs;
- different model capability tiers;
- natural-language vs structured state;
- structured state + legal-domain input;
- optional general expert evidence.

Measure **quality stability**, not identical choices.

### GSP-3C — automated API harness

Only after GSP-3A/B justify it:

- define an automated provider adapter;
- send structured decision context and legal candidate IDs;
- validate returned IDs locally;
- retain Manual fallback;
- measure latency, cost and failure behavior.

No Android product dependency on an API is authorized by GSP-0.

## 10. Offline-first product invariant

CampBoardGameHost remains:

> **a complete offline Android Storyteller Host.**

Offline must retain:

- rule correctness;
- canonical Game State;
- all legal decision domains;
- all Manual Storyteller decisions;
- game progression;
- recovery/persistence;
- history/review functions that do not inherently require a network.

Optional online mode may offer higher-quality AI recommendations.

A future remote Policy Service may live in separate web/server infrastructure, including potentially the WebHost ecosystem, without moving canonical game ownership out of the Android Host.

A future sufficiently capable small on-device model may implement the same provider boundary.

Deployment is a later decision, not a prerequisite for proving recommendation quality.

## 11. GSP stages

### GSP-0 — route reset / documentation synchronization

Status at creation: **IN PROGRESS / DOCS ONLY**.

Goals:

- make GSP the current recommendation execution authority;
- stop LRE-P special-case policy expansion;
- preserve LRE-1 fail-closed safety;
- synchronize roadmap, handoff and document index;
- de-authorize the old LRE staged replacement route;
- preserve history rather than rewriting accepted checkpoints.

No executable behavior changes.

### GSP-1 — special-policy authority revocation

**COMPLETE / ACCEPTED — PR #225, squash merge `2a4884c856423d0268b020bd774dcb95954954de`; CI #3764 / R2 #3456 GREEN.**

The production audit covered every known live special-policy island and revoked automatic authority where the choice was discretionary and based on a bounded case/predicate rather than a generally trusted reasoner.

Known initial audit set:

- Q04 Drunk assignment;
- functioning Librarian V2;
- INV1-A functioning Investigator.

Acceptance target:

- unique legal result remains `RULE_DETERMINISTIC`;
- multi-choice discretionary decisions fail closed to complete Manual legal domain;
- no fallback to legacy heuristic scoring;
- special-policy code may remain shadow/benchmark-only;
- no physical deletion required yet;
- replay/trace/reference behavior remains available for comparison.

### GSP-2 — provider / global-context contract

Define the smallest stable provider seam and context contract.

Must specify:

- required vs enrichment context;
- complete legal candidate identity;
- decision freshness/revision;
- coordination-horizon representation;
- player experience/history enrichment;
- provider response shape;
- local validation;
- Manual/offline fallback;
- provenance/replay identity.

Do not bind the contract to a vendor or model.

### GSP-3A — manual blind LLM benchmark

Run frozen benchmark cases without API integration.

At least one case must exercise multiple interacting first-night decisions; later cases should include different topologies, Poisoner/Drunk states, mid-game history and player-skill variation.

### GSP-3B — repeated/cross-model benchmark

Compare quality and robustness across models and prompt/context variants.

### GSP-3C — automated API harness

Only if manual benchmarks justify automation.

This is the first stage that may introduce a remote inference adapter, and even then it remains optional.

### GSP-4 — global/full-game evidence expansion

Prioritize complete-game reconstruction and multi-decision context in EvidenceLab. Preserve candidate comparisons and rationale when available.

### GSP-5 — deployment/model decision

Use benchmark evidence to decide among:

- optional remote general LLM;
- trained/fine-tuned model;
- hybrid;
- future on-device model.

Do not train merely to avoid small API costs; train when quality/control/offline requirements justify it.

### GSP-6 — physical retirement cleanup

After production no longer depends on obsolete policy implementations, delete/narrow:

- legacy heuristic scoring/ranking;
- fixed recommendation-style/probability authority;
- special-case auto-selectors that are no longer benchmark/reference dependencies;
- tests whose sole contract is obsolete policy output.

Preserve legality, deterministic rules, context, diagnostics, replay and benchmark assets.

## 12. Relationship to LRE

LRE-0 remains valid:

- the legacy heuristic recommendation stack is not a trustworthy Storyteller policy.

LRE-1 remains valid and becomes more important:

- unsupported multi-choice decisions fail closed to Manual;
- legacy heuristics cannot regain authority simply because a newer provider is unavailable.

The superseded part is LRE-P:

```text
evidence
-> smallest bounded deterministic policy
-> family cutover
-> repeat
```

That loop is no longer the product route.

Physical legacy deletion remains necessary, but its timing is now GSP-6 after authority separation and benchmark/provider work establish which infrastructure is still useful.

## 13. Relationship to HOST-ML0 / HOST-ML1

HOST-ML0/1 are preserved.

Their strongest conclusions fit GSP directly:

- Host owns canonical input and legality;
- export is policy-neutral;
- historical choice is distinct from input;
- legal unchosen is not automatically a negative label;
- models are replaceable policy/evaluation consumers;
- model training/deployment must not gain game-state authority.

The neutral export may be reused for manual benchmark materialization, later automated harnesses and future training/evaluation, without becoming canonical state.

## 14. Immediate continuation

GSP-1 is now COMPLETE / ACCEPTED. Production call-path re-audit after merge confirms:

- Q04 production adapter has no production caller; it remains definition/test/reference material;
- Librarian V2 and INV1-A selectors remain reachable only from retained, uncalled helper/oracle code, not from the first-night `automaticPolicyRecommendation` wiring;
- unique rules-legal outcomes remain eligible for deterministic automatic resolution;
- discretionary multi-choice decisions expose the complete Manual legal domain;
- no legacy heuristic fallback was reintroduced.

The next planned route phase is GSP-2, but it is **not started by this closure**. Do not implement another named special-case deterministic policy, API integration, or LLM production path as part of GSP-1 bookkeeping.
