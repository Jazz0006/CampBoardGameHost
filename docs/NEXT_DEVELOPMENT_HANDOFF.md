# NEXT DEVELOPMENT HANDOFF — EPI-MQ Neutral Hypothetical Evaluator

> Updated: 2026-09-15 Australia/Sydney  
> Status: **CURRENT / canonical active handoff**  
> Product-code baseline after merged PR #135: `13d0921b3df13c2618a15eb3c4d7840c750eba3a`  
> Current route decision: `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`

## 0. Start here

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`;
6. `docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md` as supporting architecture/history;
7. query live `main`, relevant open PRs and checks.

Do not use dated handoffs in `docs/archive/` as execution authority.

## 1. Immediate goal

EPI-MQ-0.5 is complete and merged via PR #135.

The immediate implementation target is:

**EPI-MQ-1 — neutral hypothetical observation evaluator.**

Extract the already-proven exact historical BEFORE/AFTER evaluation currently embedded in B4-specific ownership into neutral `clocktower/epistemic` ownership. B4 should become a consumer of that owner.

Do not resume ADB debugging unless explicitly requested later.

## 2. Completed EPI-MQ-0.5 contract

PR #135 added:

```text
EpistemicEvaluationCapability
  EXACT_HISTORICAL_REPLAY
  EXACT_HYPOTHETICAL_OBSERVATION

EpistemicEvaluationAvailability
  Ready
  Deferred(missingCapabilities)
```

`EpistemicEvaluationCapabilityBoundary` currently marks only the validated built-in official Trouble Brewing ruleset as READY for the combined historical+hypothetical requirement.

Important properties:

- support is assessed from the validated ruleset, not only `ScriptId`;
- imported/homebrew content cannot inherit Trouble Brewing exact support by reusing `trouble_brewing`;
- unsupported high-level evaluation is DEFERRED and carries explicit missing capabilities;
- availability carries no world cardinality, so unsupported semantics cannot masquerade as zero worlds / UNSAT;
- `B4DynamicPlayerWorldSetShadow` now checks the boundary before its historical exact path and returns `DEFERRED_B4` for unsupported semantics;
- direct `EnumeratedHistoricalExactBaseline` behavior remains fail-closed for unsupported scripts.

Invariant:

```text
DEFERRED != UNSAT
```

No recommendation result, truth/false family probability, UI behavior, or Trouble Brewing exact semantics changed.

## 3. Corrected product decision remains authoritative

The current fixed truthful-vs-false family probability is a temporary/fallback mechanism, not the final Storyteller decision model.

The final READY-path EPI-MQ behavior must evaluate truthful and false legal candidates together and decide:

```text
- should this impaired ability tell the truth?
- if not, which legal false result should it present?
```

A truthful candidate may be the best misleading choice if false alternatives expose impairment or contradict current recipient knowledge. A false candidate may be best if truth would strongly confirm the real world while a false clue preserves coherent mistaken worlds.

Truthfulness is typed candidate metadata, not the primary quality objective.

## 4. Authoritative EPI-MQ sequence

```text
EPI-MQ-0.5  Capability Boundary                         COMPLETE / PR #135
EPI-MQ-1    Neutral Hypothetical Evaluator              CURRENT
EPI-MQ-2    Hard Consistency / Exposure Gates
EPI-MQ-2.5  Truth+False Shadow Integration
EPI-MQ-3    Unified Productive-Uncertainty Quality Model
EPI-MQ-4    Unified Impaired-Information Production Cutover
EPI-MQ-5    Calibration / Fallback Refinement
```

The previous `False-family Selection Cutover` concept is obsolete.

## 5. EPI-MQ-1 required behavior

The new neutral evaluator must own the semantic operation:

```text
recipient-visible setup/history
+ validated ruleset/capability
+ exact hypothesis
+ one hypothetical EpistemicObservation
        ↓
exact BEFORE world state
        ↓
apply hypothetical observation exactly once
        ↓
exact AFTER diagnostics
```

It must be:

- exact;
- deterministic;
- mutation-free;
- recipient-knowledge-safe;
- capability-aware;
- explicit about READY vs DEFERRED;
- independent of recommendation policy;
- independent of B4 reporting vocabulary;
- extensible beyond cardinality diagnostics later.

B4 must reuse the neutral evaluator rather than retain a second implementation of the same exact hypothetical filtering operation.

## 6. Critical apply-once rule

The existing session preflight can calculate a correctly bound hypothetical global observation without mutating the live session.

Preserve this ordering:

```text
current durable observation log
  -> build BEFORE world state

preflight(candidate)
  -> obtain correctly bound hypothetical observation

neutral evaluator
  -> apply that hypothetical observation exactly once
  -> produce AFTER
```

Do not build BEFORE from a preflight result that already contains the candidate in its next observation log and then apply the candidate again.

This is a semantic correctness issue, not merely an optimization detail.

## 7. Ownership constraints

Preserve:

```text
rules
  -> legal candidate semantics

recommendation
  -> consume legal candidates + neutral diagnostics
  -> final selection policy

session
  -> canonical state / timeline / durable observation identity
  -> non-mutating preflight binding

epistemic
  -> recipient knowledge / exact worlds / historical replay
  -> neutral hypothetical observation consequences

UI
  -> presentation / confirmation
```

Do not wire recommendation directly to `B4DynamicPlayerWorldSetShadow`.

Avoid a dependency cycle:

```text
recommendation -> session -> epistemic -> recommendation
```

Do not move session mutation/identity authority into the neutral evaluator. The evaluator should consume already-correct semantic input and remain mutation-free.

## 8. Existing seams to reuse

Reuse rather than duplicate:

- `EpistemicEvaluationCapabilityBoundary` from EPI-MQ-0.5;
- `DecisionCandidate` / `DecisionEvaluation`;
- `InformationDecisionContext<T>`;
- `EpistemicObservationDraft`;
- `ClocktowerGameSession.preflightGlobalEpistemicObservation()`;
- `EnumeratedHistoricalExactBaseline`;
- `EnumeratedHistoricalWorldReplay`;
- current exact Trouble Brewing observation matching semantics;
- existing `B4HistoricalExactShadowBridgeTest` characterization.

Do not introduce a second candidate legality model or a recommendation-owned world engine.

## 9. EPI-MQ-1 fan-out audit before extraction

Inspect live current code around at least:

```text
EpistemicEvaluationCapability.kt
Epistemic contracts / world-set interfaces
EnumeratedHistoricalExactBaseline
EnumeratedHistoricalWorldReplay
current Trouble Brewing world-observation evaluator implementation
B4DynamicPlayerWorldSetShadow
B4HistoricalExactShadowBridgeTest
session preflight / observation binding
EpistemicObservation / EpistemicObservationDraft
```

Map:

- exact input needed to build BEFORE;
- exact current filtering needed to derive AFTER;
- whether evaluator should return cardinality only or a neutral diagnostic object carrying exact world-set evidence internally;
- how B4 converts neutral diagnostics into its existing `B4CandidateWorldQuery`;
- how capability DEFERRED propagates without becoming an empty exact result;
- how later EPI-MQ-2 diagnostics can extend the result without redesigning the API.

Prefer a narrow neutral semantic result over exposing storyteller-hidden world contents to recommendation callers.

## 10. Test direction

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

The extraction itself is behavior-preserving, so existing GREEN B4 exact characterization should be reused rather than inventing a ritual RED solely for moving code.

Add focused typed tests only where the neutral public/internal contract adds a new durable guarantee not already characterized.

Minimum EPI-MQ-1 proof:

```text
same history + same candidate -> deterministic BEFORE/AFTER
B4 result remains equivalent after delegating to neutral evaluator
unsupported capability -> DEFERRED, not zero worlds / UNSAT
hypothetical observation is applied exactly once
input/session/history is not mutated
hidden Storyteller target changes do not leak into recipient result
```

At a logical checkpoint run the epistemic/enumeration escalation required by `docs/TESTING_STRATEGY.md`; do not rely on `UP-TO-DATE` alone as execution evidence.

## 11. Hidden-information boundary

EPI-MQ must reason only from recipient-available knowledge.

Do not directly constrain worlds using Storyteller-only facts such as:

- actual hidden roles beyond recipient knowledge;
- actual Poisoner/Pukka target;
- hidden protection/attack target;
- hidden transition cause before observable;
- Fortune Teller red-herring identity outside legal FT semantics;
- unknown demon bluffs;
- recommendation seeds / internal scores / Storyteller-only metadata.

Historical replay must regenerate hidden mechanics from rules and per-world state rather than copying actual hidden selections into all worlds.

## 12. What EPI-MQ-1 must not do

Do not:

- change current truth/false family probability;
- change candidate ranking or selection;
- integrate recommendation with B4;
- implement productive-uncertainty scoring yet;
- implement Pukka/Moonchild exact semantics;
- cut ZDD over to correctness authority;
- redesign UI;
- resume Pair-display ADB work.

EPI-MQ-1 is the neutral semantic foundation for those later decisions, not the cutover itself.

## 13. Stable UI and gameplay contracts

Do not mix UI redesign into this work.

Preserve:

- Beginner and Experienced share one gameplay/rules/legal-candidate/recommendation/session/persistence pipeline;
- highlight = current waking / acted-on / selected;
- `✓` = Storyteller-facing truth / typed registration truth hit;
- yellow badge = persistent host-only special state such as Fortune Teller Red Herring;
- poison marker = visual effective poison state only;
- Experienced recommendation surfaces show real ranked recommendations followed by legal manual alternatives;
- bottom navigation remains navigation/utility; daytime domain actions stay in the square-table center.

## 14. Paused Pair-display device issue

The Pair-information display stall / old-device abnormal exit remains unresolved but is intentionally paused by user priority.

Do not spend the current phase collecting ADB evidence or changing display timing unless the user explicitly resumes that issue.

## 15. Unrelated work

PR #109 (`Reproduce restored execution preflight crash`) remains unrelated unless live status has changed. Re-query before touching it and do not fold it into EPI-MQ without a separate decision.

## 16. First concrete action in the next conversation

After reading the canonical documents and querying live `main`:

1. perform the EPI-MQ-1 live fan-out audit;
2. identify the exact B4-owned BEFORE/AFTER logic to extract;
3. define the smallest neutral evaluator input/output contract;
4. preserve the 0.5 READY/DEFERRED boundary;
5. make B4 delegate to the neutral evaluator;
6. prove behavior equivalence and apply-once/non-mutation semantics;
7. run the required focused and broader tests;
8. do not alter production recommendation behavior.

Then proceed to EPI-MQ-2 hard consistency/exposure gates.

## 17. Stable rule

> **EPI-MQ-0.5 is complete. EPI-MQ-1 now extracts the exact hypothetical observation consequence into neutral epistemic ownership, preserving capability DEFERRED semantics and applying each candidate exactly once. The later unified truth+false model—not a fixed family roll—will ultimately decide whether to tell truth or lie.**
