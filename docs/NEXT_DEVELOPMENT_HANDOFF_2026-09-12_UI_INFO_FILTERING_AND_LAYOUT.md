# NEXT DEVELOPMENT HANDOFF — UI-INFO-1 Information Filtering & Layout

> Date: 2026-09-12 Australia/Sydney  
> Status: **CURRENT — next development task after UX-MODE-1 closeout**  
> Program: Clocktower Storyteller mobile UI  
> Expected starting point: merged `main` after PR #120

## 0. Start here in the next conversation

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
6. query live `main` and only then inspect the current production UI sources.

Do not assume the pre-merge PR #120 head remains the live baseline. Re-query `main` first.

## 1. Immediate goal

The next task is a focused mobile UI campaign:

> **Audit and improve what information is shown on Storyteller screens, then improve its visual hierarchy and layout.**

This is intentionally separate from the just-completed global navigation / system-inset work.

The first step is **read-only UI ownership/content audit**. Do not begin by editing production Compose.

## 2. Product problem

The square-table interaction model and global Previous / Host Tools / Next navigation are now established. The remaining usability issue is information density and hierarchy inside those surfaces.

The audit should answer, for each major Storyteller surface:

```text
What must always be visible?
What is useful only while making the current decision?
What is redundant / low-value / diagnostic-only?
What can be collapsed, shortened or removed?
What must remain visible in Beginner vs Experienced mode?
What typography / spacing / wrapping policy keeps the important information readable on a phone?
```

Primary phone portrait remains the design target, including dense 12–15 player layouts.

## 3. Scope

Audit at least these presentation families:

- night square-table action surfaces;
- pair-information roles;
- numeric-information roles;
- Fortune Teller and other target-selection surfaces;
- first-night evil-team / bluff information;
- Spy / registration-related Storyteller presentation where applicable;
- Day overview / nomination / vote and day-role square-table surfaces;
- player-facing information display where the same content hierarchy affects readability;
- any remaining generic text/info cards that are still part of normal production flow.

For each surface, classify visible content as:

```text
A — essential / always visible
B — contextual / visible only when relevant to the current action
C — advanced / Experienced-only where product semantics justify it
D — diagnostic / debug-only
E — redundant / remove
```

Do not decide category C merely because information is complex. Beginner vs Experienced differences remain governed by interaction authority, not by exposing a second rules/recommendation pipeline.

## 4. Layout audit questions

For every audited surface, record:

- primary task/instruction;
- current center-content hierarchy;
- seat label content and readability;
- role/player/result text size and wrapping behavior;
- vertical space consumed by headings, badges, explanations and buttons;
- duplicate information already encoded by seat highlighting or button state;
- long Chinese/English strings that can force overflow;
- whether content can survive small/short screens without pushing critical controls off-screen;
- whether 12–15 player density makes player name / role identity unreadable;
- whether the same information is presented twice in different wording.

Prefer structural simplification over shrinking fonts.

## 5. Existing product decisions that remain authoritative

From `BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`:

- normal UI should not present coarse recommendation-reason prose as strategically meaningful explanation;
- square-table geometry remains the primary interaction surface;
- center space is for current role / clue / instruction / confirmation content;
- player-facing display must not leak Storyteller-only hidden state;
- reliable/fixed vs Storyteller-discretionary distinction is operational Storyteller information;
- legal-domain authority remains upstream of presentation;
- Manual and recommendation continue to share the same complete legal semantic domain.

UX-MODE-1 also remains authoritative:

> Beginner and Experienced use the same rules / legal-candidate / recommendation pipeline. Mode changes interaction authority and presentation only.

## 6. Explicit non-goals

Do not use UI-INFO-1 to:

- change gameplay rules or legal candidates;
- redesign recommendation ranking;
- start EPI-MQ / Productive Uncertainty implementation;
- replace the current recommendation provider;
- change stable random-selection semantics;
- reopen global navigation placement;
- reopen Android system-bar/inset behavior unless a new concrete regression is reproduced;
- change player-owned choices into automatic Storyteller choices;
- perform another broad source-decomposition campaign.

## 7. Required first deliverable

Before production edits, produce a compact audit table covering the live surfaces with at least:

```text
surface / owner
current visible information
A/B/C/D/E classification
layout/readability problem
recommended minimum presentation
implementation seam
risk / test level
```

Then propose the smallest staged implementation order.

Prefer a few reusable presentation/layout primitives over per-role ad-hoc fixes, but only after the audit proves the common ownership boundary.

## 8. Testing expectations

Follow `docs/TESTING_STRATEGY.md`.

For pure presentation filtering/layout changes:

- establish focused durable contracts where ownership/content behavior can regress;
- use T0 focused tests during each slice;
- run T1 `:app:testFast` after meaningful production checkpoints;
- escalate to broader Android/build validation when shared shells or high-fanout presentation primitives change;
- real-device validation is required for visual density/readability claims that unit tests cannot prove.

Do not add brittle source-string tests when a typed/presentation seam is practical. If a temporary migration contract is unavoidable, retire it once durable ownership coverage exists.

## 9. UX-MODE-1 closeout baseline

PR #120 completed the Beginner/Experienced mode work plus the device/UI convergence fixes discovered during acceptance.

The final acceptance included real-device validation on POCO X8 Pro for:

- night square-table bottom navigation staying within device bounds;
- Day global navigation rendered below the square table rather than inside it;
- Day bottom safe area adapting to currently visible Android navigation bars rather than permanently reserving hidden-bar space.

Do not treat those as open blockers when starting UI-INFO-1.

## 10. Stable rule

> **UI-INFO-1 changes information selection and visual hierarchy, not semantic truth, legality or recommendation ownership. Simplify what the Storyteller sees before shrinking it.**
