# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-04 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Latest integrated `main` before this checkpoint:

```text
2ac88ef170373f7d98684baab99495675fe6a00a
Merge PR #97: reconcile repository authority before UI-N1
```

Always re-query live GitHub state before implementation, validation or merge.

Current active integration branch:

```text
codex/r4d6-full-integration
```

Current Draft PR:

```text
#99 — R4D-6: absorb preserved Host Table closeout lineage
```

Internal integration PR #98 merged the final preserved post-#92 R4D-6 descendant into this branch through a real three-way Git merge. PR #98 targeted the integration branch, **not main**.

Current execution priority was explicitly changed by the user on 2026-09-04 after the repository-wide audit:

```text
R4D-6 FULL INTEGRATION / RECONCILIATION — ACTIVE
-> UI-N1 Night persistent Host Table lifecycle
```

This supersedes the earlier temporary ordering that placed the salvage audit after UI-N1.

Current active handoff remains:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-04_UI_N1_NIGHT_LIFECYCLE.md`

It now covers this pre-UI-N1 integration prerequisite plus the subsequent UI-N1 execution contract.

## 2. Current execution order

```text
R4D-6 full integration / reconciliation
-> UI-N1 Night persistent Host Table lifecycle
-> R4D-6 residual verification only
-> UI-R5 final real-device stabilization / feature freeze
-> EPI-MQ / Productive Uncertainty / PlayerWorldSet
-> UX-R6 legacy recommendation-provider replacement
```

Do not resume historical stacked UI PRs or archived handoffs as active work.

## 3. R4D-6 full integration / reconciliation — ACTIVE

### 3.1 Preserved lineage

Common historical base:

```text
PR #92 head:
5501fb02cf37fa2da9ad63bbef7d78608784d787
```

Preserved lineage:

```text
codex/ui-r4d6-4c-demon-successor-table
-> codex/ui-r4d6-closeout-seat-number-badge
-> codex/ui-r4d6-closeout-host-seat-role-presentation
-> codex/ui-r4d6-closeout-adaptive-seat-presentation
-> codex/ui-r4d6-closeout-postdeal-role-visibility
```

Final descendant:

```text
b0eabb24620a14ce704c6e3de5df9ec569e0c864
```

Historical compare from #92 showed this descendant 44 commits ahead of that base.

### 3.2 Integration method

The final descendant was **not** directly merged to main. Instead:

```text
current main
-> codex/r4d6-full-integration
-> real three-way merge of final salvage descendant via internal PR #98
-> audit final integration-vs-main tree
-> normal PR #99 CI/R2
-> final main merge only after explicit user authorization
```

This preserves post-#92 current-main work while recovering the lineage's surviving final product behavior.

### 3.3 Final net scope after three-way merge

The 44-commit history collapses to a small final tree difference against current main, centered on:

- typed `HostSeatPresentation` consumption across Host/Night/pair Manual surfaces;
- Demon Successor square-table migration;
- shared seat-number badge;
- Storyteller-private actual/shown role presentation, including Drunk;
- adaptive square-table seat density and bounded long-name support;
- post-deal Host role visibility;
- shared seat-number presentation in Player Reveal;
- corresponding typed presentation/layout tests.

Temporary one-shot workflow/script files are not part of the final tree.

### 3.4 Reconciliation rules

Default policy is **functional absorption**, not historical commit replay purity.

Keep the feature unless one of these is proven:

1. current main already contains a stronger equivalent;
2. the final branch artifact is temporary tooling rather than product/test code;
3. the change would restore an obsolete implementation shape that conflicts with current architecture;
4. the change would alter gameplay legality/rules authority outside the approved UI scope.

Current architecture remains authoritative over historical implementation shape.

In particular:

- Host/UI presentation does not own target legality or rule truth;
- actual and shown identity stay typed and separate;
- Player Reveal remains sanitized;
- Night seat/presentation unification does not define the forthcoming `WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE` lifecycle;
- no temporary one-shot workflow/script may be restored;
- no recommendation or epistemic algorithm redesign belongs here.

### 3.5 Validation gate

At the pre-doc-update integration head `b9a19b2be31ce2aa82698c54c67a9873752ffd80`:

```text
CI #1546 — success
R2 #1444 — success
```

Any later documentation or reconciliation commit moves the PR head and must receive the appropriate current-head remote gate before merge.

Exit condition:

- final PR diff contains only intended product/test/docs changes;
- no temporary workflow/script residue;
- no current-main architecture regression;
- CI/R2 green on current PR head;
- final merge to main explicitly authorized by the user.

## 4. UI-N1 — Night persistent Host Table wake/action lifecycle — NEXT

After R4D-6 full integration lands, implement the structural Night UX correction discovered on real devices.

Target lifecycle:

```text
stable physical table
-> WAKE
-> ACT
-> RESOLVE
-> SHOW
-> COMPLETE
```

Roles may skip irrelevant stages.

Representative flows:

```text
deterministic information:
WAKE -> SHOW -> COMPLETE

selectable/recommended information:
WAKE -> RESOLVE -> SHOW -> COMPLETE

single-target action without player-facing result:
WAKE -> ACT -> COMPLETE

Fortune Teller-style mixed flow:
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

Product requirements:

- reuse existing `HostTableShell` and stable `ClocktowerSeatId`;
- awakened/acting player remains in the same physical seat;
- WAKE has the strongest actor cue;
- actor/wake, legal target, selected target, illegal/disabled and dead states stay semantically distinct;
- center content owns phase/task;
- target legality remains typed upstream authority;
- recommendation/Manual remains Storyteller authority and is not collapsed into Player Reveal;
- SHOW remains sanitized full-screen Player Reveal;
- back/navigation/recomposition/restore must not silently lose, repeat or advance lifecycle state;
- lifecycle state is presentation/session state, not gameplay truth authority.

## 5. R4D-6 residual verification — AFTER UI-N1

Because the preserved lineage is now being absorbed before UI-N1, the later R4D step narrows from a broad migration audit to **residual verification only**.

Audit only active surfaces still proven inconsistent after full integration + UI-N1, for example:

- any surviving legacy/non-persistent Host selector;
- vote compatibility paths still materially inconsistent with the shared table contract;
- Drunk/actual-vs-shown presentation gaps not covered by integration;
- long player/role-name cases still failing bounded readability;
- duplicate selector/seat presentation owners that are now safe to retire.

Do not re-open work already absorbed successfully by PR #99.

## 6. UI-R5 — final real-device stabilization / feature freeze

After UI-N1 and residual verification, run one full Storyteller real-device stabilization pass covering:

- seating/start/reorder;
- game selection after seat confirmation;
- Minion/Demon introduction;
- pair recommendation + Manual;
- registration-sensitive information;
- Day Overview and migrated Day actions;
- nomination/current vote flow;
- Night wake/action lifecycle;
- Drunk actual/shown Storyteller presentation;
- long player/role names;
- Player Reveal privacy/readability/navigation.

UI-R5 is stabilization/feature freeze, not another structural redesign.

## 7. EPI-MQ / Productive Uncertainty — AFTER UI-R5

Primary authorities:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Quality ranking remains downstream of legal semantic authority:

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

A4/ZDD remains shadow/prototype unless separately reactivated and validated.

## 8. UX-R6 — production recommendation-provider replacement — AFTER EPI-MQ

Replace the legacy recommendation provider only after EPI-MQ correctness, quality, shadow, performance and rollout gates pass.

Preserve:

- complete legal semantic candidate domain as upstream authority;
- Manual independence;
- typed recommendation/confirmation identity;
- stabilized Storyteller UI;
- Player Reveal privacy;
- safe fallback/degraded behavior until cutover is proven.

## 9. Explicitly deferred product features

### Public Claim History — DEFERRED

Do not implement durable public-claim events/history/projection or claim-driven recommendation now.

### Sequential Vote UX — DEFERRED

Keep the accepted pending multi-selection + explicit `Confirm vote` workflow. Preserve canonical clockwise recorded history, ghost-vote authority, vote transaction, threshold/tie/on-block behavior.

Do not introduce sequential cursor/lock/undo-last unless the product decision is explicitly reopened.

### Other deferred/later work

- complete special-character vote modifier automation, including Butler assistance;
- broad unsupported-script expansion;
- A4/ZDD production rollout before its own gates;
- theme/animation polish unrelated to usability;
- broad Host/App decomposition unrelated to an active ownership problem.

## 10. Permanent architecture invariants

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
- shown identity is committed once;
- Healthy, Poisoned and Drunk of the same perceived role share role semantics before reliability;
- Spy/Recluse registration belongs to semantic truth construction, not recommendation heuristics;
- semantic legality/truth is not owned by Host/UI projection;
- every supported information role remains playable through a correct Manual/generated path even when recommendation is unavailable;
- recommendation remains downstream of complete legal semantic authority;
- Manual is permanent user authority;
- exact typed outcome identity survives presentation/confirmation;
- durable player-visible observations exclude Storyteller-hidden facts;
- A3 exact enumeration remains correctness baseline;
- A4/ZDD remains shadow/prototype until separately validated.

### Persistent Host Table

```text
stable typed ClocktowerSeatId
-> stable physical table position for the whole session
```

Storyteller-private presentation may show actual/shown role state; Player Reveal may not leak it.

## 11. Development / validation authority

Root `AGENTS.md` is normative.

Use risk-based evidence, not RED ceremony. Add tests for stable behavior/invariant changes; use existing characterization/compile/diff evidence for refactors when sufficient.

Before implementation or merge:

1. re-query live `main` and relevant PR/head/checks;
2. distinguish historical checkpoint SHAs from live state;
3. keep scope within this roadmap/handoff;
4. require current-head GitHub CI/R2 gates as prescribed;
5. do not merge to main without explicit user authorization.

## 12. Documentation authority / lifecycle

`docs/README.md` is the navigation entrypoint; this roadmap is the status/priority authority.

Only one `NEXT_DEVELOPMENT_HANDOFF_*.md` may be active in docs root. Historical branches, archive documents and old PRs are evidence only unless this roadmap explicitly reactivates them.