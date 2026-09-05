# NEXT DEVELOPMENT HANDOFF — R4D-6 Full Integration -> UI-N1 Night Lifecycle

> Status: **CURRENT ACTIVE HANDOFF**  
> Date: 2026-09-04 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Purpose

This handoff now covers two immediately consecutive phases:

```text
Phase A — R4D-6 preserved-lineage full integration / reconciliation
Phase B — UI-N1 Night persistent Host Table lifecycle
```

The execution order was explicitly changed by the user on 2026-09-04 after the repository-wide audit found a preserved post-#92 R4D-6 lineage with useful completed Host Table work. The goal is to absorb that mature work while its design intent is still clear, then implement UI-N1 on top of the stronger shared table foundation.

This handoff is subordinate to root `AGENTS.md` and `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.

## 2. Phase A — R4D-6 full integration — ACTIVE

### 2.1 Historical source

Common validated historical base:

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

Historical compare showed 44 commits ahead of the #92 base.

### 2.2 Current integration state

Current-main checkpoint before Phase A:

```text
2ac88ef170373f7d98684baab99495675fe6a00a
```

Integration branch:

```text
codex/r4d6-full-integration
```

Internal integration PR:

```text
#98 — final salvage descendant -> integration branch
```

PR #98 completed a real three-way Git merge. It did **not** target `main`.

Draft main PR:

```text
#99 — R4D-6: absorb preserved Host Table closeout lineage
```

At integration head `b9a19b2be31ce2aa82698c54c67a9873752ffd80`, before the subsequent docs-authority commits:

```text
CI #1546 — success
R2 #1444 — success
```

Any later head must receive the appropriate current-head remote gates before main merge.

### 2.3 Functional absorption contract

Do not replay 44 historical commits one by one. Judge the final integrated tree against current main.

Default: **KEEP surviving product behavior**.

Remove/replace only when proven to be:

- temporary one-shot tooling;
- a duplicate with a stronger current-main equivalent;
- an obsolete implementation shape conflicting with current architecture;
- an unintended gameplay/rules/epistemic authority change outside UI scope.

Current-main authority always wins over historical implementation shape.

### 2.4 Intended surviving capabilities

The final integration is expected to preserve:

- Demon Successor square-table target selection;
- typed shared `HostSeatPresentation` consumption in Night/Fortune Teller/pair Manual surfaces;
- shared seat number badge;
- Storyteller-private actual vs shown role presentation, including Drunk;
- automatic post-deal Host role visibility;
- adaptive square-table density and bounded long-name readiness;
- shared seat-number presentation in Player Reveal;
- corresponding typed tests.

The final tree must not restore already-cleaned temporary patch workflow/script files.

### 2.5 Architecture boundaries during Phase A

Preserve existing ownership of:

- target/action legality;
- same-night effective-state rules;
- actual/shown identity semantics;
- registration/provenance;
- recommendation/manual legal candidate identity;
- durable observation/history ordering;
- Player Reveal privacy.

The R4D-6 integration may improve presentation and shared seat projections; it must not become new gameplay truth authority.

The integrated Night seat wiring is foundation work only. It must **not** be treated as the future Night lifecycle implementation.

### 2.6 Phase A validation / exit

Before merging #99 to main:

1. exact PR diff contains only intended production/test/docs files;
2. no temporary `.github` one-shot residue;
3. no current-main behavior/architecture regression is visible in the final patch;
4. typed tests introduced by the lineage remain meaningful and GREEN;
5. current-head CI and R2 are GREEN;
6. merge to main occurs only after explicit user authorization.

After Phase A lands, do not delete the historical lineage until the final post-UI-N1 residual verification confirms no unique behavior remains stranded there.

## 3. Phase B — UI-N1 Night persistent Host Table lifecycle — NEXT

Real-device testing showed that action-role selectors can replace or cover the wake instruction before the Storyteller has clearly acknowledged whom to wake. Fortune Teller is the clearest example, but the issue is architectural.

Use one lifecycle vocabulary:

```text
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

Roles may skip stages that do not apply.

Representative flows:

```text
deterministic information:
WAKE -> SHOW -> COMPLETE

selectable/recommended information such as Washerwoman/Librarian/Investigator:
WAKE -> RESOLVE -> SHOW -> COMPLETE

single-target action without player-facing information:
WAKE -> ACT -> COMPLETE

Fortune Teller-style mixed flow:
WAKE -> ACT -> RESOLVE -> SHOW -> COMPLETE
```

## 4. UI-N1 product contract

- Reuse existing `HostTableShell` and stable `ClocktowerSeatId`; do not create a second table framework.
- The awakened player stays in the same physical seat.
- WAKE has the strongest actor cue and may use a directional/clock-hand-style indicator.
- ACT target state is distinct from actor/wake state.
- Center task content owns current phase label/instruction and phase-specific controls.
- Legal target authority remains typed and upstream.
- RESOLVE is Storyteller/rules result or information-choice authority when required.
- Recommendation and Manual remain Storyteller authority; do not collapse them into Player Reveal.
- SHOW remains a sanitized player-facing full-screen handoff boundary.
- COMPLETE advances only after existing domain/history authority has durably accepted the interaction as required.
- Back/navigation/recomposition/restore must not silently lose, repeat or advance lifecycle state.

## 5. UI-N1 state ownership

Prefer a small typed presentation/session lifecycle owner over transient Compose-only booleans whenever stage state must survive recomposition/navigation/restore.

The lifecycle owner is presentation/session state, not gameplay truth authority.

Preserve existing ownership of:

- role/action legality;
- same-night effective state;
- registration/provenance;
- recommendation/manual candidate identity;
- durable visible observation/history ordering;
- sanitized reveal content.

## 6. UI-N1 representative scope

Implement representative patterns first:

1. Fortune Teller or equivalent two-target + result + reveal;
2. one single-target role such as Monk/Poisoner;
3. one deterministic information role;
4. one selectable/recommended information role if required to prove RESOLVE authority cleanly.

Extend only as needed to make the lifecycle coherent.

## 7. Explicit non-goals

Neither Phase A nor UI-N1 may expand into:

- EPI-MQ / Productive Uncertainty ranking;
- recommendation-provider replacement;
- Public Claim History;
- Sequential Vote redesign;
- unsupported-script expansion;
- broad Host/App decomposition;
- Mayor legality redesign;
- broad rules-semantic rewrites.

## 8. Testing strategy

Follow `AGENTS.md` risk-based evidence rules.

Phase A mostly integrates already-tested behavior. Do not manufacture new RED tests solely because historical production code moved into current main lineage. Existing typed tests + current remote gates + exact diff audit are valid evidence where behavior is unchanged.

For UI-N1, new typed tests are appropriate for stable lifecycle invariants such as:

- explicit WAKE before action selection;
- actor/wake state distinct from legal/selected targets;
- roles skip only irrelevant stages;
- restore/back does not duplicate or silently advance stage;
- SHOW cannot expose Storyteller-private state;
- existing legality and committed outcome identity remain authoritative.

Avoid pixel/source-shape tests unless an explicit architecture boundary cannot be protected otherwise.

## 9. Resume protocol

If a new session resumes while #99 is still open:

1. read root `AGENTS.md`, roadmap and this handoff;
2. re-query live `main`, #99 head/state/checks and current diff;
3. do not repeat the historical 44-commit audit from scratch unless the integration head changed materially;
4. finish Phase A reconciliation/validation first;
5. do not merge #99 without explicit user authorization.

If #99 has already merged:

1. re-query live main;
2. start a fresh UI-N1 branch from that exact main;
3. audit current Night presentation/session state ownership;
4. establish the smallest durable lifecycle behavior evidence;
5. implement representative flows;
6. stop for remote diff/CI review before main merge.

## 10. Overall exit condition

This handoff is complete when:

1. the preserved R4D-6 final functionality has been safely reconciled into current main without restoring obsolete temporary tooling or rules authority; and
2. UI-N1 provides a coherent/restorable persistent-table Night wake/action lifecycle for representative role families while Player Reveal privacy and current semantic authority remain intact.

After that, perform only a narrow R4D residual verification before UI-R5.