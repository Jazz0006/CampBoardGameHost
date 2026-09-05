# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-05 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Live `main` at this documentation checkpoint:

```text
6d787172d4084e0af9ab74cb35e06f492cbb19fd
Merge PR #102: Fix manual Demon bluff consistency
```

Always re-query live GitHub state before implementation, validation or merge.

### Recently completed / integrated

The previous roadmap and handoff were stale by 2026-09-05. The following work is now integrated into `main`:

1. **PR #99 — R4D-6 Host Table preserved-lineage integration**
   - recovered surviving shared Host Table / `HostSeatPresentation` work;
   - preserved actual/shown Storyteller-private presentation and square-table improvements;
   - did not make presentation the gameplay-legality authority.

2. **PR #100 — UI-N1 inline wake cues + shared square-table readability**
   - the earlier explicit `WAKE -> ACT` acknowledgement-state design was deliberately simplified;
   - wake/actor cue and target selection now coexist on the same persistent square table;
   - no separate wake-phase gameplay or Compose state was introduced;
   - shared square-table density / seat-number presentation was updated.

3. **PR #101 — same-night dead role wake-step correction**
   - ordinary later-night role steps with no effective actor are omitted;
   - explicit death-trigger actors such as Ravenkeeper remain materialized.

4. **PR #102 — Manual Demon bluff consistency correction**
   - MANUAL mode now consumes the intended setup recommendation bluff triple instead of falling back to arbitrary first legal roles;
   - invalid/pending/partial bluff recommendation state fails closed rather than silently substituting values.

## 2. Current priority

The current engineering checkpoint is:

```text
AI-first architecture/test policy hardening
+ documentation consolidation
-> fresh Night Step UI cluster ownership audit
-> select first behavior-preserving decomposition slice
-> UI-R5 real-device stabilization / feature freeze
-> EPI-MQ / Productive Uncertainty
-> UX-R6 recommendation-provider replacement
```

The documentation hardening does not itself authorize production-code refactoring. The next production campaign begins with a **fresh live-state audit** of the Night Step UI cluster after the latest Host Table / inline-wake work.

Primary decomposition reference:

`docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`

Current active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-05_NIGHT_STEP_UI_DECOMPOSITION.md`

## 3. Night Step UI decomposition — NEXT ARCHITECTURE CAMPAIGN

### 3.1 Goal

Reduce the practical **change context radius** of Night Step changes. The target is not a numeric file-size score; it is clearer ownership and smaller feature-local reasoning scope.

The current concern is that `ClocktowerNightStepUi.kt` has historically mixed:

- external state/wiring;
- recommendation/selection orchestration;
- structured information preparation;
- interaction-local state;
- player-display projection;
- telemetry/diagnostics;
- action rendering.

### 3.2 Fresh-audit requirement

Do not execute the earlier S1–S5 proposal mechanically. PR #99/#100 changed the surrounding square-table/presentation surface, and PR #101/#102 changed current main after that.

Before implementation, inspect live `main` as one cluster:

```text
ClocktowerNightStepUi.kt
+ HostTableShell / square-table presentation owners
+ HostSeatPresentation / actor-cue owners
+ Pair Manual / Pair recommendation owners
+ StructuredNumericInformationAdapter.kt
+ StructuredBooleanInformationAdapter.kt
+ selection-semantics / target interaction owners
+ relevant typed tests
```

For each proposed seam identify:

- authoritative state owner;
- domain/rules owner;
- side-effect owner;
- rendering owner;
- narrow typed input/result contract;
- existing owning tests;
- source-shape tests that become obsolete;
- dependency direction before/after;
- expected reduction in change context radius.

### 3.3 Candidate slice order from pre-latest-UI audit

The earlier architecture reconnaissance identified these candidates:

1. Pair Manual presentation + local-state owner;
2. player-display projection;
3. structured information preparation;
4. narrowed interaction renderer families;
5. recommendation/audit/diagnostics cleanup.

**This ordering is provisional.** Pair Manual remains a strong first candidate because legal authority already exists, but the fresh audit may select a different first seam if current code ownership has changed.

### 3.4 Decomposition invariants

- no God `NightStepContext` / giant parameter bag;
- no generic `Utils` / `Helpers` dumping ground;
- UI does not become gameplay/domain legality authority;
- UI-local transient state remains at the lowest correct owner;
- extracted modules do not depend on the whole Host/screen simply to avoid parameters;
- do not widen `private -> internal/public` merely for file extraction;
- do not split one role into one file by default;
- a slice must remove one coherent responsibility from the broad owner;
- behavior-preserving refactors must remain behavior-preserving.

## 4. Test/evidence policy for the architecture campaign

Root `AGENTS.md` is normative and now integrates architecture pre-flight with risk-based test-first development.

For each slice:

```text
classify change type
-> identify real owner + durable contract
-> identify existing owning evidence
-> baseline when useful
-> add durable characterization only for a real uncovered risk
-> refactor
-> rerun smallest affected evidence
-> exact diff / invariant audit
-> retire superseded source-shape tests
```

Do **not** manufacture RED tests for file movement or decomposition. A genuinely new stable typed seam should receive durable contract coverage when existing tests do not already protect it.

## 5. UI-R5 — AFTER architecture checkpoint

Run a final real-device stabilization / feature-freeze pass after the selected Night Step ownership work reaches a clean checkpoint.

Cover at least:

- seating/start/reorder;
- game selection after seat confirmation;
- Minion/Demon introduction and bluff display;
- Pair recommendation + Manual;
- registration-sensitive information;
- Day Overview / nomination / vote flow;
- Night actor cue + target/action flow;
- same-night death-trigger behavior;
- Drunk actual/shown Storyteller presentation;
- long player/role names;
- Player Reveal privacy/readability/navigation.

UI-R5 is stabilization, not another broad visual redesign.

## 6. EPI-MQ / Productive Uncertainty — AFTER UI-R5

Primary authorities:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Quality ranking remains downstream of legal semantic authority.

A4/ZDD remains shadow/prototype unless separately reactivated and validated.

## 7. UX-R6 — AFTER EPI-MQ

Replace the legacy recommendation provider only after EPI-MQ correctness, quality, performance and rollout gates pass.

Preserve Manual independence, typed outcome identity, stabilized Storyteller UI and safe fallback behavior.

## 8. Explicitly deferred / not part of the next architecture slice

- Public Claim History;
- Sequential Vote redesign;
- broad unsupported-script expansion;
- A4/ZDD production rollout;
- broad App-root decomposition unrelated to the active ownership problem;
- recommendation-quality algorithm redesign during a UI decomposition slice;
- gameplay-rule changes hidden inside structural refactoring.

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

Permanent rules include:

- UI/presentation is downstream of legality/truth authority;
- Manual remains permanent Storyteller authority within the legal domain;
- exact typed outcome identity survives presentation and confirmation;
- durable visible observations exclude Storyteller-hidden facts;
- structural refactoring must not alter rules or transaction ordering.

### Persistent Host Table

```text
stable typed ClocktowerSeatId
-> stable physical table position
-> Storyteller-private typed seat presentation
-> phase/action-specific center task
```

Actor/wake cue and target state are orthogonal presentation concepts. The final UI-N1 product decision does not require a separate wake acknowledgement phase.

## 10. Documentation authority / lifecycle

`docs/README.md` is the navigation entrypoint. This roadmap is the current status/priority authority.

Only one `NEXT_DEVELOPMENT_HANDOFF_*.md` may remain active in `docs/` root. Historical handoffs and implementation checkpoints belong under `docs/archive/` and are evidence only.

If an archived document contains `NEXT`, `PASS`, `READY`, or a historical SHA/PR state, do not treat it as current without reactivation by this roadmap and a fresh live-state audit.
