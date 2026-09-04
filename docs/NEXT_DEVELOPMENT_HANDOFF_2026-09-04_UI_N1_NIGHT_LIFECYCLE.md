# NEXT DEVELOPMENT HANDOFF — UI-N1 Night Persistent Host Table Lifecycle

> Status: **CURRENT ACTIVE HANDOFF**  
> Date: 2026-09-04 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`

## 1. Purpose

Implement the next independent Storyteller UI slice after the UI stack closeout: a persistent Night Host Table lifecycle that makes wake/action/result/show progression explicit without replacing the existing square-table architecture.

This handoff is subordinate to root `AGENTS.md` and `docs/CURRENT_DEVELOPMENT_ROADMAP.md`. Always re-query live `main` before creating the implementation branch.

## 2. Problem being solved

Real-device testing showed that action-role selectors can replace or cover the wake instruction before the Storyteller has clearly acknowledged whom to wake. Fortune Teller is the clearest example, but the problem is architectural rather than role-specific.

The Night interaction should remain on the same physical Host Table and use one shared lifecycle vocabulary:

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

## 3. Product contract

- Reuse the existing `HostTableShell` and stable `ClocktowerSeatId`; do not create a second table framework.
- The player being awakened remains in the same physical seat.
- WAKE has the strongest actor/wake visual treatment and may use a directional/clock-hand-style cue.
- ACT target state is visually and semantically distinct from the actor/wake state.
- The center task area owns the current phase label/instruction and phase-specific controls.
- Legal target authority remains typed and upstream of presentation.
- RESOLVE is the Storyteller/rules decision boundary when a result or information choice is required.
- Recommendation and Manual remain Storyteller-authority paths; do not collapse them into Player Reveal.
- SHOW remains a sanitized player-facing full-screen handoff boundary.
- COMPLETE advances only after the current interaction has been durably accepted/recorded as required by existing domain/history authority.
- Back/navigation/recomposition/restore must not silently lose, repeat, or advance lifecycle state.

## 4. State ownership

Prefer a small typed presentation/session lifecycle owner over transient Compose-only booleans whenever stage state must survive recomposition, navigation or restore.

The lifecycle owner is presentation/session state. It must not become new gameplay truth authority.

Preserve existing ownership of:

- current role/action legality;
- same-night effective-state rules;
- registration/provenance;
- recommendation/manual candidate identity;
- durable player-visible observation/history ordering;
- sanitized Player Reveal content.

## 5. Representative implementation scope

Use a small set of representative role families first rather than migrating every Night role in one PR:

1. Fortune Teller or equivalent two-target + result + reveal flow;
2. one single-target action role such as Monk/Poisoner where existing legality authority can be reused;
3. one deterministic information role that can demonstrate `WAKE -> SHOW -> COMPLETE`.

After these patterns are stable, extend only as needed to make the Night lifecycle coherent.

## 6. Explicit non-goals

Do not include in UI-N1:

- EPI-MQ / Productive Uncertainty ranking;
- recommendation-provider replacement;
- public claim history;
- sequential vote redesign;
- broad unsupported-script expansion;
- broad Host/App decomposition;
- Mayor legality redesign;
- direct integration of the historical R4D-6 salvage branch;
- broad Demon Successor/seat-presentation cleanup unless a minimal compatibility adjustment is strictly required for UI-N1.

## 7. Historical R4D-6 salvage source — preserve, do not merge directly

A post-#92 implementation lineage was intentionally excluded from PR #94 closeout and remains outside current `main`.

Common validated base:

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

Furthest descendant checkpoint at this audit:

```text
branch: codex/ui-r4d6-closeout-postdeal-role-visibility
head:   b0eabb24620a14ce704c6e3de5df9ec569e0c864
```

GitHub compare from #92 head reports this descendant as 44 commits ahead of the #92 base.

The lineage contains potentially reusable work around Demon Successor square-table migration, seat number badges, Host actual/shown role presentation, adaptive seat density, pair/manual seat presentation and post-deal role visibility.

**Do not merge or bulk cherry-pick this branch into UI-N1.** It predates the final closeout/main lineage and overlaps Night presentation files. Treat it as historical implementation evidence only.

After UI-N1, the `UI-R4D residual migration audit` must review this final descendant and classify each surviving idea/change as:

```text
REUSE / REIMPLEMENT / SUPERSEDED / DEFER
```

Do not delete these salvage branches until that residual audit is complete.

## 8. Validation strategy

Follow `AGENTS.md` risk-based evidence rules.

New typed tests are appropriate for stable lifecycle invariants, for example:

- explicit WAKE stage exists before action selection;
- actor/wake seat state differs from legal/selected target state;
- role families skip only irrelevant stages;
- restore/back does not duplicate or silently advance active stage;
- SHOW cannot expose Storyteller-private state;
- existing target legality and committed outcome identity remain authoritative.

Avoid source-string/pixel tests and tests that only freeze Compose implementation shape.

Use the smallest focused GREEN evidence during development; require normal PR CI/R2 and the repository-prescribed broader gate at the logical checkpoint.

## 9. Start protocol

When implementation begins:

1. re-read root `AGENTS.md`, `docs/CURRENT_DEVELOPMENT_ROADMAP.md`, and this handoff;
2. re-query live `main` and open PR state;
3. create a fresh UI-N1 branch from exact current `main`;
4. audit current Night presentation/session state ownership before adding a new lifecycle type;
5. establish the smallest durable lifecycle RED/characterization evidence;
6. implement representative flows without broad migration;
7. stop for remote diff/CI review before merge.

## 10. Exit condition

UI-N1 is complete when the persistent table provides a coherent and restorable Night wake/action lifecycle for representative role families, Player Reveal privacy remains intact, no second table framework exists, and remote validation is green.

After UI-N1, return to `CURRENT_DEVELOPMENT_ROADMAP.md` and execute the `UI-R4D residual migration audit`; do not jump directly to EPI-MQ.
