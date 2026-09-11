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

EPI-MQ / Productive Uncertainty                   CURRENT — EPI-MQ-0 baseline re-audit
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign documents are historical evidence, not default execution authority.

## 2. Immediate priority — EPI-MQ-0

The current development task is **EPI-MQ-0 baseline / ownership re-audit**.

Live baseline transition:

```text
ROLE-ROTATION-1 merged via PR #119
merge commit: 72775b63f2b98322f2ceb8972fd1cea8f7a46007
```

Before implementation:

```text
re-query live main from the merge baseline
trace the current misinformation-candidate / visible-observation / epistemic-world path
identify the current typed hypothetical-evaluation seam
record mutation and hidden-information boundaries
define the small deterministic Trouble Brewing behavior corpus
make a GO / MODIFY / NO-GO decision for the older EPI-MQ-1 proposal
```

EPI-MQ-0 is an audit/spec stage. It does **not** authorize production recommendation-ranking changes.

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`

## 3. ROLE-ROTATION-1 closeout

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

## 4. Architecture / scope continuity

Do not reopen completed D6, UI-R5, UI-NAV-1, or ROLE-ROTATION work unless a concrete regression requires it.

For EPI-MQ:

- preserve current setup/session/persistence owners;
- do not leak Storyteller-hidden action facts into recipient knowledge;
- do not mutate live session/history merely to evaluate a hypothetical observation;
- do not activate A4/ZDD or replace the recommendation provider as part of the baseline audit;
- follow `AGENTS.md` architecture pre-flight requirements before substantial edits to protected/core or >1000 LOC handwritten source;
- follow `docs/TESTING_STRATEGY.md` for risk-based RED/GREEN and T0–T4 escalation.

## 5. Queued program — UX-R6

`UX-R6 recommendation-provider replacement` remains queued after EPI-MQ unless the roadmap is explicitly reprioritized.

Do not pull UX-R6 into EPI-MQ-0 merely because both touch recommendation behavior.

## 6. Default reading order for the next development conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`;
5. live GitHub `main` and current PR/branch state;
6. only the specialized EPI-MQ/reference documents named by the active handoff.

Do not load completed ROLE-ROTATION, UI-NAV, UI-R5, D6, or persistence handoffs by default.

## 7. Stable rule

> **Current roadmap + one active handoff define what happens next. Historical campaign documents provide evidence, not execution authority.**
