# NEXT DEVELOPMENT HANDOFF — EPI-MQ Unified Impaired-Information Selection

> Updated: 2026-09-15 Australia/Sydney  
> Status: **CURRENT / canonical active handoff**  
> Product-code baseline after merged PR #134: `1cdc35886aea654ad82bf4e2a388095880686ed5`  
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

The user has explicitly reprioritized away from the Pair-display ADB investigation and back to EPI-MQ / consistency-algorithm work.

The immediate implementation target is:

**EPI-MQ-0.5 — generic epistemic capability / DEFERRED boundary.**

Before implementing it, perform a focused live-code fan-out audit of the current exact epistemic contracts and consumers so the new capability seam fits existing ownership rather than creating another parallel engine.

Do not resume ADB debugging unless explicitly requested later.

## 2. Corrected product decision

The current fixed truthful-vs-false family probability is a temporary/fallback mechanism, not the final Storyteller decision model.

The final READY-path EPI-MQ behavior must evaluate truthful and false legal candidates together and decide:

```text
- should this impaired ability tell the truth?
- if not, which legal false result should it present?
```

This supersedes the earlier narrower idea that EPI-MQ should only rank false candidates after `ImpairedInformationPolicy` has already decided truth-versus-false.

A truthful candidate may be the best misleading choice if false alternatives expose impairment or contradict current recipient knowledge. A false candidate may be best if truth would strongly confirm the real world while a false clue preserves coherent mistaken worlds.

Therefore truthfulness is typed candidate metadata, not the primary quality objective.

## 3. Authoritative EPI-MQ sequence

```text
EPI-MQ-0.5  Capability Boundary
EPI-MQ-1    Neutral Hypothetical Evaluator
EPI-MQ-2    Hard Consistency / Exposure Gates
EPI-MQ-2.5  Truth+False Shadow Integration
EPI-MQ-3    Unified Productive-Uncertainty Quality Model
EPI-MQ-4    Unified Impaired-Information Production Cutover
EPI-MQ-5    Calibration / Fallback Refinement
```

The previous `False-family Selection Cutover` concept is obsolete.

### EPI-MQ-0.5 — Capability Boundary

Introduce a generic high-level result that distinguishes:

```text
READY
  exact hypothetical evaluation supported

DEFERRED / UNSUPPORTED_SEMANTICS
  required role/script epistemic semantics are unavailable
```

Invariant:

```text
DEFERRED != UNSAT
```

Trouble Brewing should map to the existing exact machinery. Unsupported/custom scripts should defer cleanly instead of looking like zero possible worlds.

Do not implement Pukka/Moonchild exact semantics here.

### EPI-MQ-1 — Neutral Hypothetical Evaluator

Extract the proven hypothetical historical evaluation from B4-specific ownership into neutral `clocktower/epistemic` ownership.

Requirements:

- exact;
- deterministic;
- mutation-free;
- recipient-knowledge-safe;
- BEFORE/AFTER diagnostics;
- hypothetical observation consumed exactly once;
- no hidden Storyteller-state leakage;
- B4 becomes a consumer of the neutral owner.

### EPI-MQ-2 — Hard Consistency / Exposure Gates

Add typed diagnostics for:

- exact contradiction;
- public-fact contradiction;
- temporal inconsistency;
- impairment exposure;
- DEFERRED capability.

Do not hide these all inside one arbitrary scalar score.

### EPI-MQ-2.5 — Truth+False Shadow Integration

Evaluate every legal impaired-information candidate, truthful and false, through the new epistemic path while leaving production selection unchanged.

Record enough data to compare old and new behavior, including candidate truth relation, old choice/rank, new diagnostics, BEFORE/AFTER world counts, contradiction/exposure flags, and new preferred candidate.

Shadow mode must already answer whether the new model would tell truth or lie.

### EPI-MQ-3 — Unified Productive-Uncertainty Quality Model

Truthful and false candidates compete in one pool.

Likely dimensions:

```text
+ coherence
+ productive uncertainty
+ plausible mistaken worlds
+ temporal consistency
+ explanation diversity

- immediate contradiction
- impairment exposure
- confirmation lock
- excessive information destruction
```

World count alone is not enough and maximum uncertainty is not the goal. The target is coherent, playable uncertainty that still allows the game to progress.

### EPI-MQ-4 — Unified Impaired-Information Production Cutover

After shadow validation, the new model becomes the READY-path authority for both:

```text
truth versus false
and
which false result
```

`ImpairedInformationPolicy` and the existing heuristic path remain fallback behavior for DEFERRED/unsupported cases rather than normal primary authority.

### EPI-MQ-5 — Calibration / fallback refinement

Tune weights, thresholds and bounded diversity only after simulation/real-game evidence. Do not restore a global fixed truth/false percentage as the primary decision rule without evidence.

## 4. Ownership constraints

Preserve:

```text
rules
  -> legal candidate semantics

recommendation
  -> consume legal candidates + neutral diagnostics
  -> final selection policy

session
  -> canonical state / timeline / durable observation identity

epistemic
  -> recipient knowledge / exact worlds / historical replay
  -> hypothetical observation consequences

UI
  -> presentation / confirmation
```

Do not wire recommendation directly to `B4DynamicPlayerWorldSetShadow`.

Avoid a dependency cycle:

```text
recommendation -> session -> epistemic -> recommendation
```

The exact composition/injection seam should be chosen during EPI-MQ-1 fan-out audit.

## 5. Existing seams to reuse

The live system already has the foundations EPI-MQ needs.

Reuse rather than duplicate:

- `DecisionCandidate` / `DecisionEvaluation`;
- `InformationDecisionContext<T>`;
- `EpistemicObservationDraft`;
- `ClocktowerGameSession.preflightGlobalEpistemicObservation()`;
- `EnumeratedHistoricalExactBaseline`;
- `EnumeratedHistoricalWorldReplay`;
- existing B4 historical exact characterization.

Current recommendation behavior separates:

```text
ImpairedInformationPolicy
  -> truthful-vs-false family probability

MalfunctionPolicy / pressure / history / WeightedStableSelector
  -> ranking/selection within family
```

Treat this as fallback-compatible existing behavior, not the final EPI-MQ architecture.

## 6. Exact-engine and dynamic-script constraints

Preserve the earlier audit decisions:

- current Trouble Brewing enumerated/historical exact logic is the correctness authority;
- A4/ZDD remains representation/shadow work, not production EPI-MQ truth;
- ASP/Clingo remains cross-validation evidence;
- EPI-MQ depends on a generic capability/evaluator seam, not concrete `TroubleBrewing...` classes;
- unsupported semantics return DEFERRED;
- `DEFERRED != UNSAT`;
- future Pukka/Moonchild support is not required before EPI-MQ-0.5 or EPI-MQ-1.

## 7. Hidden-information boundary

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

## 8. First implementation fan-out audit

Before writing EPI-MQ-0.5 code, inspect current live implementations and tests around at least:

```text
clocktower/epistemic contracts / world-set interfaces
EnumeratedHistoricalExactBaseline
EnumeratedHistoricalWorldReplay
TroubleBrewingWorldObservationEvaluator
B4DynamicPlayerWorldSetShadow
B4 historical exact bridge tests
unsupported-script exact tests
session preflight / observation binding
DecisionCandidate / DecisionEvaluation
InformationDecisionContext
```

Map:

- current producers;
- current consumers;
- which layer should own capability status;
- which low-level fail-closed contracts should remain unchanged;
- where READY/DEFERRED should become visible to higher-level EPI-MQ consumers.

Choose concrete type/file names only after this live fan-out audit.

## 9. Test direction

Follow root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

EPI-MQ-0.5 introduces a durable semantic contract, so use focused typed tests.

Minimum capability proof:

```text
Trouble Brewing supported semantics -> READY
unsupported script/role semantics -> DEFERRED
DEFERRED != UNSAT / zero-world contradiction
```

EPI-MQ-1 must additionally prove:

```text
same history + same candidate -> deterministic BEFORE/AFTER
live session is not mutated
hypothetical observation applied exactly once
hidden target changes do not leak into recipient result
B4 behavior remains equivalent through the neutral evaluator
```

Do not manufacture a new RED merely for behavior-preserving extraction if existing exact characterization already protects the contract.

At logical checkpoints use the epistemic/enumeration T0/T1/T2/T3/T4 escalation in `docs/TESTING_STRATEGY.md`.

## 10. Stable UI and gameplay contracts

Do not mix UI redesign into this work.

Preserve:

- Beginner and Experienced share one gameplay/rules/legal-candidate/recommendation/session/persistence pipeline;
- highlight = current waking / acted-on / selected;
- `✓` = Storyteller-facing truth / typed registration truth hit;
- yellow badge = persistent host-only special state such as Fortune Teller Red Herring;
- poison marker = visual effective poison state only;
- Experienced recommendation surfaces show real ranked recommendations followed by legal manual alternatives;
- bottom navigation remains navigation/utility; daytime domain actions stay in the square-table center.

## 11. Paused Pair-display device issue

The Pair-information display stall / old-device abnormal exit remains unresolved but is intentionally paused by user priority.

Do not spend the next phase collecting ADB evidence or changing display timing unless the user explicitly resumes that issue.

## 12. Unrelated work

PR #109 (`Reproduce restored execution preflight crash`) remains unrelated unless live status has changed. Re-query before touching it and do not fold it into EPI-MQ without a separate decision.

## 13. First concrete action in the next conversation

After reading the canonical documents and querying live `main`:

1. perform the EPI-MQ-0.5 live fan-out audit;
2. define the smallest generic READY/DEFERRED capability contract consistent with current code style;
3. add focused typed capability tests;
4. implement the Trouble Brewing READY adapter / unsupported DEFERRED path;
5. run the required focused and FAST tests;
6. do not change production recommendation results yet.

Then proceed to EPI-MQ-1 neutral evaluator extraction.

## 14. Stable rule

> **The EPI-MQ algorithm is responsible for the truth-versus-lie decision as well as lie selection. First establish exact capability and neutral evaluation; validate the unified truth+false model in shadow mode; then cut it over as the normal READY-path authority, retaining the current fixed family policy only as fallback when exact epistemic semantics are deferred.**
