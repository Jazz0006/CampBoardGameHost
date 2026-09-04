# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-04 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**  
> Keep this file short. Completed implementation detail belongs in historical/archive documents.

## 1. Live development context

```text
live main after roadmap normalization:
622985aac0231d385bfb386b40fe92a17fe60961

merged UI closeout:
PR #94 — UI stack closeout: integrate Storyteller workspace campaign

merged roadmap normalization:
PR #95 — normalize roadmap after UI stack closeout

active UI target:
Night persistent Host Table wake/action lifecycle

intended lifecycle vocabulary:
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE

algorithm campaign after UI stabilization:
EPI-MQ / Productive Uncertainty / PlayerWorldSet
-> UX-R6 legacy recommendation-provider replacement
```

Always re-query live GitHub state before implementation, validation or merge.

The Persistent Host Table / Storyteller Workspace campaign through PR #92 is now on `main` via PR #94. Do not reopen or re-stack the historical UI-R1 through UI-R4D slices merely because their original PRs were stacked.

## 2. Current execution order

### UI-N1 — Night persistent Host Table wake/action lifecycle — ACTIVE NEXT

Real-device testing identified a structural Night UX gap: an action-role square-table selector can cover or replace the wake instruction before the Storyteller has clearly acknowledged whom to wake.

The next Night slice should extend the existing persistent `HostTableShell`; it must not create a second table framework.

Target interaction model:

```text
stable physical table
-> WAKE: current actor/wake target emphasized on the table
-> ACT: choose one/two targets or provide role-specific input
-> RESOLVE: Storyteller/rules result or information selection when needed
-> SHOW: sanitized Player Reveal when player-facing information is required
-> COMPLETE: advance to the next Host interaction
```

Product requirements:

- the player being awakened remains in the same physical table slot;
- the waking/acting seat receives the strongest visual cue, including a clear directional/arrow-style indicator where useful;
- center content states the current phase and owns the current task;
- target selection uses existing typed legal-seat authority;
- actor/wake state and selected-target state remain semantically distinct;
- information and action roles share the same lifecycle vocabulary but may skip irrelevant stages;
- recommendation/Manual choice remains a Storyteller-authority step and must not be collapsed into Player Reveal;
- Player Reveal remains a separate sanitized full-screen phone-handoff boundary;
- navigation/back/restore must not silently lose or repeat the active stage;
- no recommendation-ranking, legality or rules redesign is part of this slice.

Representative flows:

```text
deterministic information with no Storyteller choice:
WAKE -> SHOW -> COMPLETE

selectable/recommended information such as Washerwoman/Librarian/Investigator:
WAKE -> RESOLVE -> SHOW -> COMPLETE

single-target action with no player-facing result:
WAKE -> ACT -> COMPLETE

Fortune Teller-style mixed flow:
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

Implementation should prefer a small typed presentation/session-stage owner rather than transient Compose-only state when lifecycle state must survive recomposition, navigation or restore.

### UI-R4D residual migration audit — AFTER UI-N1

After the Night lifecycle is stable, audit the remaining Host interaction surfaces for incomplete migration to the shared table/presentation architecture.

Known audit targets include:

- Demon Successor / other still-legacy player-selection surfaces;
- remaining legacy/non-persistent vote paths that still require compatibility handling;
- consistent actual-role vs shown/perceived-role Storyteller presentation;
- Drunk Storyteller presentation;
- long player-name / long role-name bounded readability;
- duplicated player selector / seat presentation paths that can now be retired safely.

This is a completion audit, not permission for a broad rewrite. Migrate only paths that are still active and materially inconsistent with the shared Host Table contract.

### UI-R5 — final real-device stabilization / feature freeze

After UI-N1 and the residual migration audit reach a coherent executable checkpoint, perform a full Storyteller workflow stabilization pass on real devices.

Required coverage includes:

- seating-first session start and reorder;
- game selection after seat confirmation;
- Minion/Demon introduction;
- pair recommendation and Manual;
- registration-sensitive information;
- Day Overview;
- Slayer / Artist / Klutz and other migrated Day actions;
- nomination and current vote-recording flow;
- night wake/action flow;
- Drunk actual/shown Storyteller presentation;
- long player/role names;
- Player Reveal privacy/readability and return navigation.

UI-R5 is a stabilization pass. Do not use it for another structural redesign.

### EPI-MQ / Productive Uncertainty — AFTER UI-R5

Once the interaction contract is stable on device, resume the misinformation-quality / epistemic recommendation campaign.

Primary authorities:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

The quality layer remains downstream of legal semantic authority and should evaluate candidate observations through recipient PlayerWorldSet BEFORE/AFTER reasoning, credibility, persistence, breakability, interaction value and Productive Uncertainty.

Do not redesign the stabilized UI contract merely to fit a new ranking engine.

### UX-R6 — replace legacy recommendation provider — AFTER EPI-MQ

After EPI-MQ has passed its correctness, quality, shadow-comparison, performance and rollout gates, replace the legacy recommendation provider behind the already-stable legal-domain / decision / presentation contract.

UX-R6 must preserve:

- the complete legal semantic candidate domain as upstream authority;
- Manual independence from recommendation availability;
- typed recommendation/confirmation identity;
- the stabilized Storyteller UI contract;
- Player Reveal privacy;
- existing fallback/degraded behavior until the new provider is proven production-safe.

Do not treat completion of EPI-MQ shadow evaluation as equivalent to production cutover; the legacy provider is replaced only by this explicit UX-R6 stage.

## 3. Explicitly deferred product features

The following items are intentionally **not active development targets**.

### Public Claim History — DEFERRED BY PRODUCT DECISION

Do not implement durable `PublicRoleClaimEvent`, claim editing/history UI, latest-claim seat projection, or recommendation use of public claims at this time.

The earlier design remains historical reference only. If this feature is reconsidered later, it must return as a new explicit product decision and independent slice.

### Sequential Vote UX — DEFERRED BY PRODUCT DECISION

The original R4D design proposed a strict per-player vote cursor:

```text
upcoming -> current -> counted/locked
```

The current merged product instead uses pending multi-selection of voters followed by one explicit `Confirm vote`, while preserving canonical clockwise ordering for recorded voter history, ghost-vote authority, vote transaction, threshold/tie/on-the-block behavior.

Accept the current merged interaction for now.

Do **not** convert voting to a sequential cursor, counted/locked seat progression, or `undo-last` workflow unless the product decision is explicitly reopened.

### Other deferred / later work

- complete special-character vote-modifier automation, including Butler assistance;
- public-claim-driven recommendation behavior;
- broad unsupported-script expansion;
- A4/ZDD production rollout before its own correctness/performance gates;
- theme/animation polish unrelated to usability;
- broad Host/App decomposition unrelated to an active ownership problem.

## 4. Permanent architecture invariants

### Epistemic / information authority

```text
Composition
-> committed actual identity
-> committed shown identity
-> perceived ability
-> complete healthy legal/truth semantic domain
-> interaction-scoped registration
-> RELIABLE / POISONED / DRUNK reliability state
-> recommendation/manual selection
-> AbilityObservation
-> durable player-visible history
-> UI
```

Permanent rules:

- Drunk actual identity remains Drunk;
- shown identity is committed once and is not recommendation state;
- Healthy, Poisoned and Drunk of the same perceived role share role semantics before reliability;
- Spy/Recluse registration belongs to semantic truth construction, not recommendation heuristics;
- semantic legality/truth must not be owned by Host/UI compatibility projection;
- every supported information role remains playable through a correct Manual/generated legal path even when recommendation support is absent;
- recommendation ranking remains downstream of complete legal semantic authority;
- Manual is a permanent user authority path, not a recommendation style;
- exact typed outcome identity survives presentation and confirmation;
- durable player-visible observations must not contain Storyteller-hidden facts;
- A3 exact enumeration remains the algorithm correctness baseline;
- A4/ZDD remains shadow/prototype until separately validated.

### Persistent Host Table

```text
stable typed seatId
-> stable physical table position for the whole session
```

The table contract is:

```text
table edge = WHO / stable physical players and relevant Host-private state
center     = WHAT THE STORYTELLER IS DOING NOW
```

Required invariants:

- filtered legal-target sets must not renumber or reposition players;
- interaction state uses typed seat identity, never localized text parsing;
- domain/rules owners decide legal targets and outcomes;
- table presentation may show Host-private actual/shown state where appropriate;
- Player Reveal is not a Host Table mode.

### Player Reveal privacy boundary

```text
Storyteller workspace -> persistent Host Table
Player-facing reveal  -> sanitized information-only full screen
```

Player Reveal must never expose unintended Host-only context such as:

- actual hidden role;
- Storyteller-only actual/shown comparison;
- poison/drunk/reliability state;
- registration witness/provenance;
- recommendation reasons/truth flags;
- hidden vote/Host state.

## 5. Stable recommendation / Manual contract

```text
Complete legal semantic candidate domain
        |
        +--> Manual / direct legal selection
        |
        +--> Recommendation Provider
                 -> presentation
```

Permanent condition:

```text
recommendation unavailable != manual unavailable
```

Normal execution remains Storyteller-confirmed / ASSISTED.

For pair/combinatorial domains:

```text
Top-1 recommendation
+ 0–2 alternatives
+ persistent Manual over complete legal domain
```

For naturally small domains:

```text
primary recommendation
+ all remaining legal outcomes when the full domain comfortably fits
```

## 6. Testing and source-ownership policy

Authorities:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` for protected large-source edits.

Use risk-based behavior-first testing. Do not create ceremonial source-shape or pixel tests for ordinary visual/refactor work.

High-value Night-lifecycle contracts should protect observable state transitions and privacy rather than exact Compose structure.

`ClocktowerHostScreen.kt` remains orchestration; do not make it the implementation home for the app-wide table shell, Night lifecycle policy, voting engine or recommendation semantics.

`ClocktowerNightStepUi.kt` remains Night orchestration/wiring; avoid growing it into another monolith. Prefer cohesive small owners for lifecycle/presentation state and reusable table controls.

## 7. Current documentation authority

Current execution authority:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/TESTING_STRATEGY.md
```

Current UI/product reference:

```text
docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md
docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md
```

Completed/historical campaign detail:

```text
docs/COMPLETED_DEVELOPMENT_HISTORY.md
docs/UI_STACK_CLOSEOUT_2026-09-04.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_PERSISTENT_HOST_TABLE.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-03_UI_R4D3_DAY_WORKSPACE.md
```

Algorithm authorities after UI stabilization:

```text
docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md
docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md
docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md
```

## 8. Resume protocol

For a new development conversation:

1. read root `AGENTS.md` and this roadmap;
2. re-query live `main` and any active PR/head/checks;
3. if UI-N1 has not yet started, branch from current `main` and begin with the Night wake/action lifecycle only;
4. preserve current target legality, information authority, Player Reveal privacy and stable seat identity;
5. do not start Public Claim History or Sequential Vote redesign;
6. do not resume EPI-MQ until UI-R5 stabilization says the interaction contract is stable;
7. after EPI-MQ is validated, retain UX-R6 as the explicit production cutover that replaces the legacy recommendation provider behind the stable contract.

## 9. Completed work / archive rule

Do not grow this file by re-adding completed slice-by-slice evidence.

Completed campaign details, exact checkpoints, CI evidence and historical implementation notes belong in:

- `docs/COMPLETED_DEVELOPMENT_HISTORY.md`;
- campaign-specific historical handoff/checkpoint documents;
- `docs/UI_STACK_CLOSEOUT_2026-09-04.md` for the UI stack merged by PR #94.

When a future active slice completes and merges, move its detailed acceptance/validation notes out of this roadmap and leave only permanent invariants plus the next active route.
