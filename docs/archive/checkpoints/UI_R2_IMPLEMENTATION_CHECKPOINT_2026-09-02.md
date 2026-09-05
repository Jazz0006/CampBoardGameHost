# UI-R2 Implementation Checkpoint — Pair Manual Square Table

> Date: 2026-09-02 Australia/Sydney  
> Campaign: UI Information Presentation  
> Slice: UI-R2 — pair Manual dedicated full-screen selection  
> Status: implementation wired; final normal PR CI/R2 pending on this connector-authored checkpoint  
> Merge: **not authorized**

## Scope implemented

UI-R2 replaces the old inline first-night pair Manual drill-down for:

- Washerwoman;
- Librarian;
- Investigator.

The new interaction:

- consumes `step.manualInformationCandidates` as the established legal-domain authority;
- keeps selection-state logic in the dedicated `ClocktowerPairManualSelectionUi.kt` owner;
- uses the reusable UI-R1 square-table seat surface;
- constrains the second seat to legal continuations of the selected role/first seat;
- resolves confirmation back to the exact supplied typed `ClocktowerDisplayOption`;
- keeps legal zero-case handling data-driven;
- resets draft Manual state when the owning interaction changes;
- hides recommendation reason/warning prose from the ordinary product UI without deleting diagnostic `reasonCodes` / `warningCodes` or changing ranking/legal semantics.

`ClocktowerNightStepUi.kt` remains orchestration/wiring rather than the owner of the new selection UI.

## Durable tests

The retained UI-R2 test is behavior-first:

`ClocktowerPairManualSelectionModelTest`

It covers the new typed selection-state contract, including:

- roles and first-seat choices derive only from supplied legal candidates;
- first-seat selection constrains legal second seats;
- changing the first seat cannot retain a stale invalid second seat;
- selected role/seats resolve to the original exact typed legal option;
- legal zero-case handling remains explicit.

No permanent source-string/Compose-shape test was added.

## Large-file wiring evidence

The production wiring in `ClocktowerNightStepUi.kt` used the repository-required exact-anchor GitHub Actions one-shot path.

The final successful one-shot used source strings only as temporary patch-location safety anchors, not as product tests. Its acceptance evidence was:

- exact branch HEAD / blob locks;
- unique localized patch anchors;
- `git diff --check`;
- exact changed-file allowlist: `ClocktowerNightStepUi.kt` only;
- focused `ClocktowerPairManualSelectionModelTest` GREEN after wiring;
- `:app:testFast` GREEN;
- remote-head recheck before commit.

The one-shot temporary workflow/script self-removed after the product commit.

Product wiring checkpoint:

`4a500e878c1b15ace4441b8043556a395ffdfd15`

Cleanup checkpoint:

`4d940144fed813d2717fb33a990193d26bbc8ae8`

The cleanup bot push produced `action_required` PR runs, so this connector-authored `[full-ci]` checkpoint exists to obtain the normal final CI/R2 evidence required by the repository SOP.

## Scope guards preserved

This slice does **not**:

- start EPI-MQ / Productive Uncertainty;
- change recommendation ranking/scoring;
- redefine Manual legality;
- change Spy/Recluse semantic ownership;
- start Host/App decomposition;
- redesign Fortune Teller;
- implement UI-R3 player-facing information display;
- merge either UI-R1 or UI-R2.

After normal CI/R2 is green, PR #71 should be restored to its logical stacked base `codex/ui-r1-square-table-seat-surface` and remain draft/unmerged until explicitly authorized.
