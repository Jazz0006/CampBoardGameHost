# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-15 Australia/Sydney  
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

CURRENT:
EPI-MQ-0.5 — generic epistemic capability / DEFERRED boundary

NEXT:
EPI-MQ-1 — neutral hypothetical observation evaluator
then EPI-MQ-2 / 2.5 / 3 / 4 / 5 under the unified truth+false route

PAUSED / DEFERRED:
Pair-information display latency / old-device ADB diagnosis
UX-R6 recommendation-provider replacement
```

Current product-code baseline after PR #134:

`1cdc35886aea654ad82bf4e2a388095880686ed5`

The current EPI-MQ route decision is:

`docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`

The earlier foundation audit remains useful historical/architectural evidence:

`docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md`

Completed campaign documents are historical evidence, not current execution authority.

## 2. Current priority — EPI-MQ restart

The user has explicitly reprioritized away from the Pair-display ADB investigation and back to consistency / productive-uncertainty work.

The key route correction is now authoritative:

> **EPI-MQ must ultimately decide both whether an impaired ability should tell the truth and, when false information is better, which legal lie to tell.**

The current fixed truthful-vs-false probability policy is a temporary/fallback mechanism, not the final Storyteller intelligence.

Truthful and false legal candidates must eventually compete in one epistemic-quality model based on their consequences for the recipient's possible worlds.

Do not permanently preserve a design where a fixed family roll decides truth-versus-false first and EPI-MQ only ranks candidates inside the false family.

## 3. Authoritative EPI-MQ sequence

```text
EPI-MQ-0.5  Capability Boundary
            - generic exact-evaluation capability contract
            - READY vs DEFERRED / unsupported semantics
            - DEFERRED != UNSAT

EPI-MQ-1    Neutral Hypothetical Evaluator
            - exact
            - mutation-free
            - recipient-knowledge-safe
            - BEFORE / AFTER diagnostics
            - candidate observation applied exactly once
            - B4 reuses neutral epistemic owner

EPI-MQ-2    Hard Consistency / Exposure Gates
            - contradiction
            - public-fact conflict
            - temporal inconsistency
            - impairment-exposure diagnostics
            - unsupported remains DEFERRED

EPI-MQ-2.5  Truth+False Shadow Integration
            - evaluate every legal impaired-information candidate
            - truthful and false in the same shadow model
            - compare old production choice vs new preferred choice
            - no production cutover yet

EPI-MQ-3    Unified Productive-Uncertainty Quality Model
            - truthful and false candidates compete together
            - world count is diagnostic, not the entire score
            - include coherence, mistaken-world quality, temporal consistency,
              explanation diversity, confirmation-lock and exposure risk

EPI-MQ-4    Unified Impaired-Information Production Cutover
            - EPI-MQ decides whether to tell truth or lie
            - if lying, EPI-MQ decides which legal lie
            - current fixed family policy becomes fallback for DEFERRED cases

EPI-MQ-5    Calibration / Fallback Refinement
            - tune weights/thresholds/diversity only after evidence
            - refine partially supported dynamic-script behavior
```

The former concept `False-family Selection Cutover` is superseded by **Unified Impaired-Information Production Cutover**.

## 4. Current implementation target — EPI-MQ-0.5

Start by establishing the capability boundary around exact hypothetical epistemic evaluation.

Required semantic distinction:

```text
READY
  exact hypothetical evaluation is supported

DEFERRED / UNSUPPORTED_SEMANTICS
  required role/script epistemic semantics are not implemented
```

Invariant:

```text
DEFERRED != UNSAT
```

EPI-MQ-0.5 must not:

- change production recommendation results;
- change current truth/false family probability;
- implement full Pukka/Moonchild semantics;
- cut ZDD over to production correctness authority;
- add a new candidate legality model;
- mix in Pair-display ADB work or UI redesign.

The high-level evaluator must be able to defer unsupported/custom script semantics even if lower-level current Trouble Brewing exact classes remain fail-closed internally.

## 5. EPI-MQ architectural ownership

Preserve the dependency direction:

```text
rules
  -> legal information shape / truthful semantics / registration semantics

recommendation
  -> legal typed candidates
  -> consume neutral diagnostics
  -> final selection policy

session
  -> canonical game state
  -> GLOBAL semantic timeline
  -> durable observation identity / commit authority

epistemic
  -> recipient-visible knowledge
  -> exact possible worlds
  -> historical replay
  -> neutral hypothetical-observation diagnostics

UI
  -> presentation and explicit user confirmation
```

Do not wire recommendation directly to `B4DynamicPlayerWorldSetShadow`.

The intended flow is conceptually:

```text
rules -> legal candidates
session/composition -> correctly bound hypothetical observation context
epistemic -> neutral candidate diagnostics
recommendation -> unified quality / selection
```

Avoid a dependency cycle such as:

```text
recommendation -> session -> epistemic -> recommendation
```

The exact composition seam should be confirmed during the EPI-MQ-1 fan-out audit.

## 6. Existing implementation that must be reused

Do not create parallel semantic authorities.

Reuse:

- `DecisionCandidate` / `DecisionEvaluation` typed legal candidates;
- `InformationDecisionContext<T>` validated candidate boundary;
- `EpistemicObservationDraft` as the unbound player-visible observation representation;
- `ClocktowerGameSession.preflightGlobalEpistemicObservation()` for non-mutating hypothetical binding;
- `EnumeratedHistoricalExactBaseline` and `EnumeratedHistoricalWorldReplay` as current historical exact correctness machinery;
- existing B4 historical exact behavior as characterization evidence during neutral evaluator extraction.

Current implementation has two levels:

```text
1. ImpairedInformationPolicy
   -> truthful-vs-false family probability

2. MalfunctionPolicy / pressure / history / stable selector
   -> candidate ranking inside family
```

This remains valid as a fallback path. It is no longer the intended final READY-path decision architecture.

## 7. Unified truth+false decision principle

The quality model must not reward or punish a candidate merely because it is truthful or false.

A truthful candidate can be the best misleading information when false alternatives expose impairment or contradict public/history evidence.

A false candidate can be best when truthful information strongly confirms the real evil structure while the false clue preserves several coherent mistaken worlds.

The target is not maximum uncertainty. It is **productive uncertainty**: coherent, playable mistaken beliefs that remain logically explainable and do not simply destroy all useful information.

Likely future diagnostics include:

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

World cardinality alone is insufficient.

## 8. Dynamic/custom-script and exact-engine constraints

The earlier extensibility decision remains in force:

- EPI-MQ depends on a generic capability/evaluation seam, not concrete `TroubleBrewing...` classes;
- unsupported semantics must return DEFERRED, never fake zero worlds/UNSAT;
- Trouble Brewing enumerated/historical exact remains current correctness authority;
- A4/ZDD remains representation/shadow work until separately authorized;
- ASP/Clingo remains cross-validation evidence;
- future roles such as Pukka/Moonchild are useful architecture stress tests, not prerequisites for EPI-MQ-0.5.

## 9. Hidden-information boundary

EPI-MQ may reason only from recipient-visible knowledge.

Do not directly constrain candidate quality with Storyteller-only facts such as:

- actual hidden roles beyond recipient knowledge;
- actual Poisoner/Pukka hidden target;
- hidden protection/attack target;
- hidden transition cause before observable;
- Fortune Teller red-herring identity outside legal FT semantics;
- demon bluffs unknown to the recipient;
- recommendation seed, score internals or Storyteller-only metadata.

Historical replay must regenerate hidden mechanics from rules/per-world state rather than copying the actual Storyteller-selected hidden target into all possible worlds.

## 10. Stable presentation contracts

Recent UI work remains stable and is not part of the EPI-MQ restart:

- Beginner and Experienced share one gameplay/rules/legal-candidate/recommendation/session/persistence pipeline;
- highlight = current waking / acted-on / selected player;
- `✓` = Storyteller-facing truth / typed registration truth hit;
- yellow corner badge = persistent host-only special state such as Fortune Teller Red Herring;
- poison marker = effective poison visual only, never interaction eligibility;
- Experienced recommendation surfaces show up to three real ranked recommendations followed by legal manual alternatives;
- bottom navigation is navigation/utility; in-table domain actions remain in the table center.

Do not mix EPI-MQ implementation with presentation redesign.

## 11. Testing and repository-writing policy

Follow:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` when required.

Durable new semantic seams require focused typed behavior tests. Pure extraction should rely on existing GREEN characterization where it already proves behavior instead of manufacturing ritual RED tests.

Minimum upcoming proof includes:

```text
supported Trouble Brewing -> READY
unsupported script/role semantics -> DEFERRED
DEFERRED != UNSAT
same history + same candidate -> deterministic diagnostics
hypothetical evaluation does not mutate live session
candidate observation is applied exactly once
hidden target changes do not leak into recipient result
truthful and false candidates are both evaluated in EPI-MQ-2.5 shadow mode
unified model can prefer truth in one state and falsehood in another
```

At logical checkpoints use the epistemic/enumeration escalation defined by `docs/TESTING_STRATEGY.md`.

## 12. Paused issue — Pair-display latency / old-device exit

The Pair-information player-display stall and older-device abnormal exit remain real but are no longer the current priority because the user explicitly reprioritized development.

The previous ADB investigation plan remains available in repository history and can be resumed later. Do not mix it into EPI-MQ commits unless new evidence shows a direct architectural connection.

## 13. Unrelated work

PR #109 (`Reproduce restored execution preflight crash`) remains unrelated unless its live state changes. Re-query before touching it and do not fold it into EPI-MQ without a separate decision.

## 14. Next-conversation reading order

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF.md`;
5. `docs/EPI_MQ_ROUTE_REAUDIT_2026-09-15.md`;
6. use `docs/EPI_MQ_0_AUDIT_AND_DYNAMIC_SCRIPT_EXTENSIBILITY_2026-09-11.md` as supporting foundation/history;
7. query live `main` and relevant open PR/check state;
8. begin EPI-MQ-0.5 implementation fan-out audit and typed capability contract work.

Historical dated handoffs are not execution authority.

## 15. Stable rule

> **EPI-MQ is now the current program. The exact evaluator must first gain a generic READY/DEFERRED capability boundary; the later unified quality model must evaluate truthful and false legal candidates together so the algorithm itself decides whether to tell the truth or lie, while the current fixed family probability survives only as a fallback for unsupported/deferred semantics.**
