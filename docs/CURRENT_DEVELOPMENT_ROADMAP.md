# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-04 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**  
> Keep this file concise. Historical implementation detail belongs in archive/checkpoint documents.

## 1. Live development context

Repository-authority cleanup checkpoint started from:

```text
upstream main at checkpoint start:
958d08099e2be38fb579d95086ed519fe4509f54

checkpoint branch:
codex/repository-authority-residual-reconciliation
```

The exact `main` SHA above is evidence for this checkpoint only. **Always re-query live GitHub `main` before implementation, validation or merge.** After this docs-only checkpoint is merged, do not continue treating `958d0809...` as the live head.

Recent integrated milestones:

```text
PR #94 — UI stack closeout: integrate Storyteller workspace campaign
PR #95 — normalize roadmap after UI stack closeout
PR #96 — address roadmap normalization review
```

Current active product target:

```text
UI-N1 — Night persistent Host Table wake/action lifecycle

WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

Current active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md`

## 2. Current execution order

```text
UI-N1 Night persistent Host Table lifecycle
-> UI-R4D residual migration audit
-> UI-R5 final real-device stabilization / feature freeze
-> EPI-MQ / Productive Uncertainty / PlayerWorldSet
-> UX-R6 legacy recommendation-provider replacement
```

Do not resume historical stacked UI PRs or archived handoffs as if they were active work.

## 3. UI-N1 — Night persistent Host Table wake/action lifecycle — ACTIVE NEXT

Real-device testing identified a structural Night UX gap: an action-role selector can cover/replace the wake instruction before the Storyteller has clearly acknowledged whom to wake.

Extend the existing persistent `HostTableShell`; do not create another table framework.

Target lifecycle:

```text
stable physical table
-> WAKE: current actor/wake target emphasized
-> ACT: choose target(s) or role-specific input
-> RESOLVE: Storyteller/rules result or information selection when needed
-> SHOW: sanitized Player Reveal when player-facing information is required
-> COMPLETE: durably accept/record and advance
```

Representative flows:

```text
deterministic information:
WAKE -> SHOW -> COMPLETE

selectable/recommended information such as Washerwoman/Librarian/Investigator:
WAKE -> RESOLVE -> SHOW -> COMPLETE

single-target action with no player-facing result:
WAKE -> ACT -> COMPLETE

Fortune Teller-style mixed flow:
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

Product requirements:

- the awakened/acting player remains in the same stable physical seat;
- WAKE receives the strongest actor cue, optionally including a directional/clock-hand-style indicator;
- actor/wake, legal target, selected target, illegal/disabled and dead states remain semantically distinct;
- center content owns the current phase/task;
- target legality remains typed upstream authority;
- recommendation/Manual remains Storyteller authority and is not collapsed into Player Reveal;
- Player Reveal remains a sanitized full-screen handoff boundary;
- back/navigation/recomposition/restore must not silently lose, repeat or advance the active stage;
- lifecycle state should be owned by a small typed presentation/session model when persistence across recomposition/navigation is required;
- no recommendation-ranking, gameplay-legality or rules redesign belongs in UI-N1.

Primary execution authority:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md`

## 4. UI-R4D residual migration audit — AFTER UI-N1

After UI-N1 reaches a coherent executable checkpoint, audit still-active Host interaction surfaces for incomplete migration to the shared table/presentation architecture.

Known audit targets:

- Demon Successor / other still-legacy player-selection surfaces;
- remaining legacy/non-persistent vote paths that still require compatibility handling;
- consistent actual-role vs shown/perceived-role Storyteller presentation;
- Drunk Storyteller presentation;
- long player-name / long role-name bounded readability;
- duplicated player selector / seat presentation paths that can now be retired safely.

This is a completion audit, not permission for a broad rewrite.

### Preserved post-#92 salvage lineage

A post-#92 R4D-6 implementation chain was intentionally excluded from PR #94 and remains outside current `main`.

Common base:

```text
PR #92 head:
5501fb02cf37fa2da9ad63bbef7d78608784d787
```

Known lineage:

```text
codex/ui-r4d6-4c-demon-successor-table
-> codex/ui-r4d6-closeout-seat-number-badge
-> codex/ui-r4d6-closeout-host-seat-role-presentation
-> codex/ui-r4d6-closeout-adaptive-seat-presentation
-> codex/ui-r4d6-closeout-postdeal-role-visibility
```

Furthest audited descendant:

```text
branch: codex/ui-r4d6-closeout-postdeal-role-visibility
head:   b0eabb24620a14ce704c6e3de5df9ec569e0c864
```

GitHub compare from the #92 head reports this descendant as **44 commits ahead** of the common base.

The lineage contains potentially reusable work around Demon Successor square-table migration, seat numbers, Host actual/shown role presentation, adaptive seat density, pair/manual presentation and post-deal role visibility.

Rules for this lineage:

- preserve it until the residual migration audit completes;
- do not bulk merge or bulk cherry-pick it into UI-N1;
- treat it as historical implementation evidence because it predates the final #94/#95/#96 main lineage and overlaps Night presentation files;
- classify each surviving change as `REUSE / REIMPLEMENT / SUPERSEDED / DEFER` against post-UI-N1 `main`;
- delete the salvage branches only after that reconciliation is complete.

## 5. UI-R5 — final real-device stabilization / feature freeze

After UI-N1 and the residual migration audit reach a coherent executable checkpoint, perform one full Storyteller workflow stabilization pass on real devices.

Required coverage:

- seating-first session start and reorder;
- game selection after seat confirmation;
- Minion/Demon introduction;
- pair recommendation and Manual;
- registration-sensitive information;
- Day Overview;
- Slayer / Artist / Klutz and other migrated Day actions;
- nomination and current vote-recording flow;
- night wake/action lifecycle;
- Drunk actual/shown Storyteller presentation;
- long player/role names;
- Player Reveal privacy/readability and return navigation.

UI-R5 is a stabilization/feature-freeze pass. Do not use it for another structural redesign.

## 6. EPI-MQ / Productive Uncertainty — AFTER UI-R5

Once the interaction contract is stable on device, resume the misinformation-quality / epistemic recommendation campaign.

Primary authorities:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Target quality pipeline remains downstream of legal semantic authority:

```text
actual state + visible history + recipient knowledge + perceived ability semantic domain
-> legal observation candidate
-> hypothetical apply
-> recipient PlayerWorldSet AFTER
-> quality features
-> safety/fairness gates
-> Productive Uncertainty / Pareto ranking
-> Storyteller policy
-> generic selector
-> committed AbilityObservation
```

Do not redesign the stabilized UI contract merely to fit a new ranking engine.

A4/ZDD remains shadow/prototype unless separately reactivated and validated through its own correctness/performance gates.

## 7. UX-R6 — replace legacy recommendation provider — AFTER EPI-MQ

After EPI-MQ has passed correctness, quality, shadow-comparison, performance and rollout gates, replace the legacy recommendation provider behind the already-stable legal-domain / decision / presentation contract.

UX-R6 must preserve:

- complete legal semantic candidate domain as upstream authority;
- Manual independence from recommendation availability;
- typed recommendation/confirmation identity;
- stabilized Storyteller UI contract;
- Player Reveal privacy;
- existing fallback/degraded behavior until the new provider is production-safe.

EPI-MQ shadow success is not itself production cutover; provider replacement occurs only in explicit UX-R6 work.

## 8. Explicitly deferred product features

These are intentionally **not active development targets** unless the user explicitly reopens the product decision.

### Public Claim History — DEFERRED

Do not implement durable `PublicRoleClaimEvent`, claim editing/history UI, latest-claim seat projection or claim-driven recommendation behavior now.

### Sequential Vote UX — DEFERRED

The current merged product uses pending multi-selection followed by explicit `Confirm vote`, while preserving clockwise recorded voter history, ghost-vote authority, vote transaction, threshold/tie/on-the-block behavior.

Do not convert to a strict per-seat cursor/lock/undo-last lifecycle unless this product decision is explicitly reopened.

### Other deferred/later work

- complete special-character vote-modifier automation, including Butler assistance;
- public-claim-driven recommendation behavior;
- broad unsupported-script expansion;
- A4/ZDD production rollout before its own gates;
- theme/animation polish unrelated to usability;
- broad Host/App decomposition unrelated to an active ownership problem.

## 9. Permanent architecture invariants

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
stable typed ClocktowerSeatId
-> stable physical table position for the whole session
```

Presentation states must not become gameplay truth authority.

## 10. Development / validation authority

Root `AGENTS.md` is normative.

Use risk-based evidence, not RED ceremony. Add new typed tests for stable behavior/invariant changes; use existing characterization/compile/diff evidence for refactors when that is sufficient.

Before every implementation or merge:

1. re-query live `main` and relevant PR/head/checks;
2. distinguish historical checkpoint SHAs from live state;
3. keep scope within the active handoff;
4. use normal remote CI/R2 gates required by repository policy;
5. do not merge without explicit user authorization when merge approval has not already been given.

## 11. Documentation authority / lifecycle

`docs/README.md` is the navigation entrypoint; this roadmap is the status/priority authority.

Only one `NEXT_DEVELOPMENT_HANDOFF_*.md` may be active in the docs root. Historical handoffs belong under `docs/archive/handoffs/`; explicitly deferred unfinished handoffs belong under `docs/archive/deferred/`.

Historical branches, archive documents and old PRs are evidence only unless this roadmap explicitly reactivates them.
