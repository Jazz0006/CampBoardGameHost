# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-11 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / merged
R3 transaction-application viability audit        COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE
UI-R5 square-table convergence                    COMPLETE / merged via PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / merged via PR #118
ROLE-ROTATION-1 recent role rotation              COMPLETE / merged via PR #119
EPI-MQ-0 baseline / ownership re-audit            COMPLETE — old EPI-MQ-1 = MODIFY

UX-MODE-1 Beginner / Experienced Storyteller Mode CURRENT — next independent task
EPI-MQ / Productive Uncertainty                   QUEUED immediately after UX-MODE-1
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign documents are historical evidence, not default execution authority.

## 2. Immediate priority — UX-MODE-1

The current development task is **UX-MODE-1 Beginner / Experienced Storyteller Mode**.

Product intent:

```text
BEGINNER
- for new app users and inexperienced Storytellers
- no ordinary clue-selection burden
- current recommended clue/result is used automatically
- fewer buttons
- clearer / somewhat richer instructions
- host mainly performs wake/show/confirm/continue actions

EXPERIENCED
- for experienced Storytellers and users familiar with the app
- recommended clue/result remains primary
- rule-valid manual alternatives remain available
- shorter instructions
- fast, low-friction operation
```

Critical invariant:

> **The two modes share one rules/candidate/recommendation pipeline. They differ in presentation and interaction policy only.**

For identical semantic inputs, both modes must receive the same legal candidate set and the same recommended candidate before presentation policy is applied.

Beginner mode must not introduce a separate rule such as `Drunk/Poisoned => always false`. It automatically consumes the existing recommendation policy; future EPI-MQ can improve that recommendation without requiring a second beginner-specific algorithm.

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_UX_MODE_1_BEGINNER_EXPERIENCED.md`

First execution step is a read-only ownership/migration audit of existing automatic/manual information settings and affected information UI surfaces before production edits.

## 3. EPI-MQ-0 closeout and resumed direction

EPI-MQ-0 is complete as an architecture/ownership audit. The old EPI-MQ-1 proposal remains directionally correct but must be modified to reuse current typed owners rather than create parallel infrastructure.

Authoritative audit/design record:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

Key findings:

```text
- InformationDecisionContext already owns validated legal candidate -> EpistemicObservationDraft.
- EpistemicObservationDraft already represents unbound player-visible information.
- ClocktowerGameSession already has non-mutating global observation preflight.
- EnumeratedHistoricalExactBaseline is the current exact multi-night recipient-world authority.
- B4DynamicPlayerWorldSetShadow already proves exact historical BEFORE -> AFTER candidate cardinality.
- B4 is intentionally shadow-only; recommendation must not directly depend on B4.
```

Therefore the next epistemic work after UX-MODE-1 is:

```text
EPI-MQ-0.5  dynamic-script extensibility guard
            - generic epistemic evaluation seam
            - explicit capability / DEFERRED contract
            - no EPI-MQ dependency on TroubleBrewing concrete classes

EPI-MQ-1    neutral hypothetical observation evaluator
            - exact
            - recipient-knowledge-safe
            - mutation-free
            - BEFORE / AFTER diagnostics
            - B4 shadow becomes a consumer of the neutral owner

EPI-MQ-2    credibility / immediate contradiction / impairment-exposure gates

EPI-MQ-3+   productive-uncertainty metrics and ranking
```

No recommendation weights or user-visible recommendation behavior should change in EPI-MQ-0.5 or the initial evaluator extraction.

## 4. Dynamic/custom script extensibility rule

Future custom scripts, including combinations such as a Trouble Brewing base with Butler replaced by Moonchild and Pukka added, must not force EPI-MQ to be rewritten as a Trouble-Brewing-specific ranking system.

Game-execution support and epistemic support may land at different times.

Required principle:

```text
role can be playable in game flow/UI
while
advanced epistemic/EPI-MQ support explicitly reports DEFERRED / UNSUPPORTED
```

Unknown epistemic semantics must never be silently treated as absent mechanics or fake exact `UNSAT`.

Future world/evaluation APIs should expose capability coverage, conceptually:

```text
READY + exact diagnostics
```

or:

```text
DEFERRED / UNSUPPORTED_SEMANTICS + missing capability identifiers
```

This preserves the established rule:

> **DEFERRED != UNSAT.**

Pukka is a particularly valuable future architecture stress test because it combines hidden target selection, temporary poisoned information, delayed death, recovery and cross-night causal state.

New role support should move toward composable role semantics rather than accumulating role-specific branches in the large Host/UI source.

## 5. Architecture / scope continuity

Do not reopen completed D6, UI-R5, UI-NAV-1, or ROLE-ROTATION work unless a concrete regression requires it.

For UX-MODE-1:

- treat Beginner / Experienced as one typed product mode, not several overlapping booleans;
- audit existing automatic/manual setting ownership before choosing migration behavior;
- preserve rules legality, recommendation semantics, session authority and history semantics;
- do not move recommendation or rule logic into presentation policy;
- do not perform unsafe whole-file replacement of large Host source;
- follow `AGENTS.md` architecture pre-flight requirements before substantial edits to protected/core or >1000 LOC handwritten source;
- follow `docs/TESTING_STRATEGY.md` for risk-based RED/GREEN and T0–T4 escalation.

For EPI-MQ when it resumes:

- preserve current setup/session/persistence owners;
- do not leak Storyteller-hidden action facts into recipient knowledge;
- do not mutate live session/history merely to evaluate a hypothetical observation;
- consume a generic epistemic world/evaluation contract rather than Trouble Brewing concrete classes;
- make unsupported script/role semantics explicit;
- do not activate A4/ZDD or replace the recommendation provider as part of the initial evaluator work.

## 6. ROLE-ROTATION-1 closeout

ROLE-ROTATION-1 is complete and no longer an active development stream.

The shipped policy preserves setup legality, role multiset, Drunk semantics and seeded randomness while applying a strict lexicographic recent-history preference:

```text
1. minimize exact shown-identity repeats from the immediately previous completed game
2. among those optima, minimize DEMON/MINION/OUTSIDER category repeats from that game
3. among those optima, minimize exact repeats from two and three games ago with weights 2:1
4. among those optima, minimize special-category repeats from those games with weights 2:1
5. break remaining ties deterministically from the game seed
```

TOWNSFOLK category repetition is neutral. History is keyed by exact trimmed confirmed player name, works across seat and roster-size changes, and never makes setup fail merely to satisfy rotation preference.

Durable starting identity/category facts are frozen from the final prepared setup and persisted through the existing Trouble Brewing completion/history lifecycle with backward-compatible legacy decoding.

Acceptance evidence:

```text
App wiring product checkpoint:
e2ab42f6159166e56520e13a790c5af3caad124a

T4 acceptance checkpoint:
4d75ed4a147304e3a01c2bc233dbdc33c802ea6f
CI #2274 / run 34556109636 — SUCCESS
Android full unit tests + debug APK — PASS
ASP contract tests — PASS
Real Clingo cross-validation — PASS
R2 main-thread boundary #2136 / run 34556109649 — PASS

PR #119 cleanup head:
732bad2d2da92c1035700bd9d683f966a56f9ed7
CI #2279 — SUCCESS
R2 #2141 — SUCCESS

merge commit:
72775b63f2b98322f2ceb8972fd1cea8f7a46007
```

The completed ROLE-ROTATION handoff has been removed from the active documentation chain.

## 7. Queued program — UX-R6

`UX-R6 recommendation-provider replacement` remains queued after EPI-MQ unless the roadmap is explicitly reprioritized.

Do not pull UX-R6 into UX-MODE-1 or early EPI-MQ merely because all three touch recommendation presentation/behavior.

## 8. Default reading order for the next development conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_UX_MODE_1_BEGINNER_EXPERIENCED.md`;
5. live GitHub `main` and current PR/branch state;
6. only the source/tests needed for UX-MODE-1 ownership/migration audit.

Do not load the full EPI-MQ reference stack until UX-MODE-1 is complete unless a narrow semantic dependency must be checked.

After UX-MODE-1 is accepted, the active reading chain should switch to:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

and the new EPI-MQ implementation handoff created from that audit.

## 9. Stable rule

> **Current roadmap + one active handoff define what happens next. Historical campaign documents provide evidence, not execution authority.**
