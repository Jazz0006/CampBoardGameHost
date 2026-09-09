# NEXT DEVELOPMENT HANDOFF — UI-R5 Square-table Storyteller Consolidation / Real-device Stabilization

> Date: 2026-09-09 Australia/Sydney  
> Status: **ACTIVE NEXT HANDOFF**  
> Scope: presentation / interaction architecture + real-device stabilization  
> Precondition: D6/R3 documentation closeout merged to `main`, then create a fresh main-based feature branch.

## 1. Read first

For the next clean development session, read in this order:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
7. live code for the square-table surfaces selected by the audit.

Do **not** load the D6 archive by default. If historical ownership evidence is needed, start with:

- `docs/archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md`;
- `docs/archive/checkpoints/d6/D6_2H_SURVIVING_SQUARE_TABLE_OWNERSHIP_AUDIT_2026-09-09.md` only if a square-table ownership question specifically requires it.

## 2. Baseline to preserve

D6 decomposition is closed.

```text
D6.1                            COMPLETE / merged
D6.2 R0–R2                     COMPLETE / FULL accepted / merged
R3 transaction application     COMPLETE / NO-GO
```

D6.2 final production-equivalent code/test head:

```text
b2263cd08bc2ce223598698324bf2b22243c91f2
```

D6.2 merge:

```text
PR #115
c75e0f0bc4635ef42ffbece41470c3437a205910
```

The final D6.2 FULL gate passed, but real-device critical-path testing was explicitly waived for that merge. UI-R5 exists partly to close that practical validation gap.

## 3. Mission

The next product/engineering objective is:

> **square-table Storyteller UI consolidation / UI-R5 real-device stabilization**

The goal is not to reduce source-file size. The goal is to make the Storyteller's square-table interaction model coherent, reusable where ownership is genuinely shared, and reliable on a real phone.

After UI-R5 acceptance, and only after that acceptance, the roadmap proceeds to:

```text
EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

## 4. First task: UI-R5.0 read-only audit

Start from live `main` on a new branch. Suggested branch name:

```text
codex/ui-r5-square-table-stabilization
```

Before changing production code, inventory the active Storyteller square-table surfaces and their ownership.

At minimum inspect:

- the surviving square-table composable(s) and seat-placement helpers;
- call sites in Night/Host/Day surfaces;
- Manual pair-information selection;
- target-selection flows that already use the table;
- Fortune Teller two-target/result flow;
- final player-facing information display;
- seat-number / player-name / role/status presentation where relevant;
- selected/highlighted/disabled state representation;
- center-content sizing and edge allocation;
- system-bar/safe-inset handling;
- recomposition/orientation behavior for transient selection state.

Produce an ownership/duplication matrix first. For each candidate shared boundary classify:

```text
GO      = real repeated presentation responsibility with small typed inputs
KEEP    = specialized surface is already the correct owner
NO-GO   = extraction would require broad state/callback/context plumbing
```

Do not begin implementation merely because two functions look similar.

## 5. Product constraints

Preserve the active product decisions in `BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`:

- full-screen rectangular/square-table visual language;
- stable seat identity independent of filtered-list position;
- dedicated Manual selection surface;
- shared visual language between selection and player display;
- Fortune Teller directly selects two targets then resolves the legal result domain;
- Storyteller-only reliability/discretion indicators never leak to player-facing display;
- Manual legality comes from the complete typed legal semantic domain, not the recommendation shortlist;
- localized text is never parsed back into semantic identity.

UI-R5 may refine geometry and presentation, but it must not redefine those semantic contracts.

## 6. Real-device acceptance targets

Portrait phone is primary. The campaign must explicitly exercise dense layouts, not only comfortable small-player examples.

Minimum device-level targets:

1. 8–15 player seat layouts remain readable;
2. seat number and player identity are visually unambiguous;
3. role/status text, where that Storyteller surface intentionally shows it, remains useful rather than shrinking below practical readability;
4. center content does not collide with edge seats;
5. system/navigation/status insets do not cover actionable content;
6. tap targets remain operable on the user's real phone;
7. selected-first / selected-second / highlighted / unavailable states remain distinguishable without relying on color alone;
8. long player names degrade predictably rather than breaking geometry;
9. opening/closing the player-facing display returns to the correct existing flow;
10. no first-night display-crash regression;
11. no hidden Storyteller state appears on the player-facing screen.

If the audit shows that one single geometry cannot serve both dense Storyteller control and player-facing display cleanly, prefer a shared geometry engine with typed presentation variants rather than forcing every surface into one oversized universal composable.

## 7. Architecture constraints

UI-R5 must preserve the post-D6 ownership model:

- `ClocktowerGameSession` remains canonical writable state;
- rule/recommendation/legal-domain semantics remain upstream of presentation;
- Planner/Reducer responsibilities are not pulled into UI;
- square-table components may own layout, local draft selection presentation and visual-state rendering only when appropriate;
- durable observation/history/Recovery ordering remains in existing owners;
- App choreography is not moved merely to make a file smaller;
- no `UiContext`, `Actions`, callback mega-bag or second session owner;
- no Compose dependency enters session/domain modules;
- Undercover/Werewolf remain isolated.

## 8. Validation strategy

Use the repository's risk-based evidence model.

For UI/presentation-only geometry changes:

- do not manufacture domain REDs;
- use existing typed interaction/legality coverage where it already protects the flow;
- add a durable test only when a stable observable interaction contract is genuinely uncovered;
- run the narrowest compile/focused evidence during implementation;
- run `:app:testFast` at the logical checkpoint when Android production code changes;
- escalate affected/full validation according to `TESTING_STRATEGY.md`.

Real-device validation is a required UI-R5 acceptance artifact, not optional polish.

Record the exact device, Android version/build if practical, player counts exercised, critical flows exercised, and any waived paths. A waiver must be written as a waiver, never as a PASS.

## 9. Scope fence

Do not broaden UI-R5 into:

- EPI-MQ scoring or explanation;
- recommendation-provider replacement;
- generalized architecture rewrite of `CampBoardGameHostApp.kt`;
- Persistence Simplification follow-up;
- rules fixes unrelated to a UI-R5 defect;
- new scripts/characters merely to test layout;
- A4/ZDD production activation.

If a real-device bug exposes a genuine semantic defect, isolate it as a separate tested bug-fix slice rather than silently changing rules inside a visual refactor.

## 10. Completion condition

UI-R5 is ready to close only when:

```text
shared square-table ownership audited
+ approved consolidation implemented
+ focused/FAST affected validation green
+ exact diff / scope audit green
+ real-device critical paths recorded
+ no hidden-information/domain/persistence regression
+ roadmap/handoff updated
```

Only then change the active roadmap priority to EPI-MQ / Productive Uncertainty.
