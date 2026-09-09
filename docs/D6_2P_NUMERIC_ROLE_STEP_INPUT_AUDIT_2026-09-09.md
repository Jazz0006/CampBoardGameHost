# D6.2p — Numeric role-step input audit

> Date: 2026-09-09 Australia/Sydney
> Status: READ-ONLY AUDIT COMPLETE; D6.2q scoped below.
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT.
> Audited head: `b5d6cadc96dd79539feeaed0d95e8297fe85758e`.
> Latest validated production checkpoint: `ad16ccc694e687ab10e62677cf0369fa26e9c2fd`.

## Decision

Do not introduce one shared numeric role-step input for Clockmaker, Chef, Empath and Chambermaid. Their common numeric display is already covered by D6.2o; their remaining preparation semantics differ materially.

| Role | Distinct preparation ownership |
|---|---|
| Clockmaker | fixed first-night truth and bounded distance; no current typed proposition or registration branch |
| Chef | whole-table numeric proposition plus Spy/Recluse registration alternatives |
| Empath | cursor-relative living neighbors, prior shown number and Spy/Recluse registration alternatives |
| Chambermaid | current ordered two-player selection, selected seat labels and value-bound proposition |

A common object would either contain nullable fields for unrelated roles or carry callbacks/maps from Host. The first safe post-D6.2o step is to consolidate Chambermaid selection presentation, which is duplicated in both first-night and other-night materializers and already has a dedicated typed owner.

## Current duplication

Both Chambermaid materializers independently:

- read `chambermaidResolution.selection.first/second`;
- resolve selected names to current seat indices;
- build the same three-space seat text;
- create the same `PLAYERS_WAKING_FOR_ABILITY` proposition for the displayed result;
- repeat the same work inside every recommended-value proposition factory.

`resolveChambermaidSelection` remains the authority for eligible names and woke count. `ClocktowerChambermaidPresentationSemantics.kt` already validates actor membership, two distinct current-player targets and the 0..2 result. That file is the correct owner for a small immutable selection presentation.

## D6.2q implementation contract

Allowlist:

- `app/src/main/java/com/codex/campboardgamehost/ClocktowerChambermaidPresentationSemantics.kt`;
- `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt`;
- `app/src/test/java/com/codex/campboardgamehost/ClocktowerChambermaidPresentationSemanticsTest.kt`.

Implementation:

1. Add an immutable Chambermaid selection presentation containing ordered selected names, resolved subject seats and optional display-secondary text.
2. Prepare it once from the current cards and `RevalidatedTwoPlayerSelection`, preserving partial-selection display behavior and selected order.
3. Let it create the typed numeric proposition from an explicit source seat and displayed value, preserving all existing validation.
4. Keep `clocktowerChambermaidDisplayProposition` as a compatibility wrapper over the new preparation seam.
5. Prepare the presentation beside `chambermaidResolution` in Host and use it in both Chambermaid materializers for display proposition, secondary text and recommendation proposition factories.
6. Keep the two materializer entries, localized text, action, result, recommendation call, selection state and registry order unchanged.

Tests must cover complete and partial selection projection, selected-seat order, proposition construction, invalid source/value, and the existing compatibility function. No source-string test is needed.

## Validation and stop conditions

Use exact three-file diff review, `git diff --check`, Android production/test compilation, FAST and R2. The prior D6.2l full gate remains sufficient unless the implementation changes workflow, rules or materializer order.

Stop if the extraction requires the recommendation coordinator, registration maps, Compose state, session, telemetry, publication callbacks or a general role-step context. D6.2q prepares immutable facts only; it does not move the full Chambermaid materializer.

Expected result: remove roughly 35–60 duplicated Host lines and make Chambermaid seat/proposition preparation independently testable. After validation, reassess whether a Chambermaid materializer can consume this prepared value plus localized content without receiving broad Host dependencies.
