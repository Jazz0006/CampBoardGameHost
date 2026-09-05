# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-06 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Latest confirmed live `main` at this documentation checkpoint:

```text
93f99e0576be7b93d479ffa931bae3e4083c25af
Fix Drunk shown-identity ownership boundary (#105)
```

Always re-query live GitHub state before implementation, validation or merge.

### Recently completed / integrated

The following work is integrated into `main`:

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

5. **PR #104 — obsolete source-wiring guard retirement**
   - removed low-value source-string / production-wiring / decomposition guards;
   - retained durable typed behavior/domain coverage;
   - reinforces the current risk-based, behavior-first test policy.

6. **PR #105 — Drunk shown-identity ownership repair**
   - Drunk shown identity is committed setup state;
   - recommendation consumes but does not select/replace it;
   - Host/UI no longer synthesizes committed shown identity back into recommendation lock state;
   - recommendation history/cooldown no longer treats shown identity as an independent recommendation decision.

## 2. Completed ownership repair — Drunk shown identity

The repair documented in:

`docs/DRUNK_SHOWN_IDENTITY_OWNERSHIP_REPAIR_2026-09-05.md`

is now integrated into `main` through PR #105.

The permanent product flow is:

```text
template history/diversity selection
-> select setup template
-> commit actual role composition
-> if Drunk exists, choose one shown identity from template drunkAsOptions
-> committed setup is complete
-> recommendation reads committed GameState
-> recommendation generates remaining mutable Storyteller information/decisions
```

Permanent ownership rule:

- template history/de-duplication applies to template selection;
- Drunk shown identity has no independent history/cooldown/de-duplication rule;
- shown identity is a committed setup fact;
- recommendation may consume it but may not select, replace, emit or lock it;
- Host/UI must not convert committed `shownRole` back into recommendation lock state.

A separate recommendation-quality parity finding remains intentionally deferred to recommendation-quality work: generic impaired information derived from fixed `shownRole` is currently selected inside `SetupEvaluator`, not jointly enumerated with all aggregate setup-plan choices. This must be addressed without returning shown-identity authority to recommendation.

## 3. Immediate current priority — Night Step UI decomposition

Resume the Night Step UI decomposition campaign from a fresh live-state audit against current `main`.

The current execution order is:

```text
fresh Night Step UI cluster ownership audit
-> selected behavior-preserving decomposition slices / architecture checkpoint
-> UI-R5 real-device stabilization / feature freeze
-> Demon Bluff Recommendation V1
-> EPI-MQ / Productive Uncertainty
-> UX-R6 recommendation-provider replacement
-> Beginner Storyteller Mode policy rollout
```

Primary decomposition reference:

`docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`

Current active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-05_NIGHT_STEP_UI_DECOMPOSITION.md`

Demon Bluff V1 planning authority:

`docs/DEMON_BLUFF_RECOMMENDATION_V1_PLAN_2026-09-06.md`

The Drunk ownership hotfix is now merged; do not retain old handoff assumptions that it is pending.

## 4. Night Step UI decomposition — NEXT ARCHITECTURE CAMPAIGN

### 4.1 Goal

Reduce the practical **change context radius** of Night Step changes. The target is not a numeric file-size score; it is clearer ownership and smaller feature-local reasoning scope.

The current concern is that `ClocktowerNightStepUi.kt` has historically mixed:

- external state/wiring;
- recommendation/selection orchestration;
- structured information preparation;
- interaction-local state;
- player-display projection;
- telemetry/diagnostics;
- action rendering.

### 4.2 Fresh-audit requirement

Do not execute the earlier S1–S5 proposal mechanically. Recent Host Table, square-table, wake-cue, Manual bluff and Drunk-ownership work changed surrounding assumptions.

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

### 4.3 Candidate slice order from pre-latest-UI audit

The earlier architecture reconnaissance identified these candidates:

1. Pair Manual presentation + local-state owner;
2. player-display projection;
3. structured information preparation;
4. narrowed interaction renderer families;
5. recommendation/audit/diagnostics cleanup.

**This ordering is provisional.** Pair Manual remains a strong first candidate because legal authority already exists, but the fresh audit may select a different first seam if current code ownership has changed.

### 4.4 Decomposition invariants

- no God `NightStepContext` / giant parameter bag;
- no generic `Utils` / `Helpers` dumping ground;
- UI does not become gameplay/domain legality authority;
- UI-local transient state remains at the lowest correct owner;
- extracted modules do not depend on the whole Host/screen simply to avoid parameters;
- do not widen `private -> internal/public` merely for file extraction;
- do not split one role into one file by default;
- a slice must remove one coherent responsibility from the broad owner;
- behavior-preserving refactors must remain behavior-preserving.

## 5. Test/evidence policy for the architecture campaign

Root `AGENTS.md` is normative and integrates architecture pre-flight with risk-based test-first development.

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

## 6. UI-R5 — AFTER architecture checkpoint

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

## 7. Demon Bluff Recommendation V1 — AFTER UI-R5, BEFORE EPI-MQ

Primary authority:

`docs/DEMON_BLUFF_RECOMMENDATION_V1_PLAN_2026-09-06.md`

Demon Bluff recommendation is now a separate setup-time recommendation subsystem rather than ordinary EPI-MQ misinformation work.

V1 should:

- keep legal bluff-domain authority separate from quality ranking;
- rank complete three-role bluff packages, not greedily select three individually high-scoring roles;
- use explicit role-level bluff profiles / setup-aware features;
- consider evil-team synergy, combination diversity, beginner executability and contingency value;
- exhaustively evaluate legal triples for Trouble Brewing where practical;
- preserve compatibility with existing MANUAL and Host presentation flows;
- avoid pulling historical replay, public claims or full hypothetical-world reasoning forward from EPI-MQ.

V1 target:

> Maximize the practical usefulness of the legal three-role bluff package for an inexperienced evil team, without changing rules semantics or requiring the cognitive-consistency engine.

A future Bluff V2 may consume EPI-MQ capabilities after that foundation is mature.

## 8. EPI-MQ / Productive Uncertainty — AFTER Demon Bluff V1

Primary authorities:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Quality ranking remains downstream of legal semantic authority.

A4/ZDD remains shadow/prototype unless separately reactivated and validated.

Demon Bluff V1 must not be treated as a substitute for EPI-MQ. Bluff V2 may later use cognitive-consistency/world-sustainability signals, but those signals remain owned by EPI-MQ.

## 9. UX-R6 — AFTER EPI-MQ

Replace the legacy recommendation provider only after EPI-MQ correctness, quality, performance and rollout gates pass.

Preserve Manual independence, typed outcome identity, stabilized Storyteller UI and safe fallback behavior.

### 9.1 Beginner Storyteller Mode — AFTER cognitive-consistency / EPI-MQ foundation

Future product/strategy authority:

`docs/BEGINNER_STORYTELLER_MODE_POLICY_2026-09-06.md`

Do **not** implement Beginner Storyteller clue automation as a pre-EPI heuristic campaign.

The intended boundary is:

```text
legal semantic domain
-> cognitive-consistency / hypothetical-world evaluation
-> plausibility and safety gates
-> bounded evil-assistance utility ranking
-> automatic Beginner policy choice
-> minimal-decision UI
```

Beginner Mode should minimize Storyteller decisions, automatically resolve clue selection and registration-sensitive interactions, strongly prefer useful misinformation for Drunk/Poisoned information without creating an `impaired => always false` invariant, and prefer strategically useful Spy/Recluse registration without making misregistration mechanically mandatory.

The evil-assistance objective is a **downstream Storyteller policy**, not a semantic truth rule and not the objective of the cognitive-consistency engine itself.

## 10. Explicitly deferred / not part of the current architecture slice

- generic impaired-information / aggregate-plan interaction-quality redesign;
- Demon Bluff Recommendation V1 implementation during the active Night Step decomposition slice;
- Demon Bluff V2 cognitive-consistency enhancement before EPI-MQ;
- Beginner Storyteller automatic clue / bounded evil-assistance policy;
- Public Claim History;
- Sequential Vote redesign;
- broad unsupported-script expansion;
- A4/ZDD production rollout;
- broad App-root decomposition unrelated to the active ownership problem;
- recommendation-quality algorithm redesign during a UI decomposition slice;
- gameplay-rule changes hidden inside structural refactoring.

## 11. Permanent architecture invariants

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

## 12. Documentation authority / lifecycle

`docs/README.md` is the navigation entrypoint. This roadmap is the current status/priority authority.

Only one `NEXT_DEVELOPMENT_HANDOFF_*.md` may remain active in `docs/` root. Historical handoffs and implementation checkpoints belong under `docs/archive/` and are evidence only.

If an archived document contains `NEXT`, `PASS`, `READY`, or a historical SHA/PR state, do not treat it as current without reactivation by this roadmap and a fresh live-state audit.
