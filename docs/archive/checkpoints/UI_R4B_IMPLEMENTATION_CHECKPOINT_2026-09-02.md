# UI-R4B — Night Action Square-Table Implementation Checkpoint

Date: 2026-09-02

## Stack

Base:
`hotfix/ravenkeeper-monk-target-legality@e369ede31d82c687139b6cb6679e91e91fdafcdb`

Draft PR:
#76 — `UI-R4B: unify night action square-table interactions`

Do not merge without explicit authorization.

## Product scope

UI-R4B standardizes the reusable square-table interaction language for common night target actions while preserving existing semantic authority.

Production behavior now includes:

- Poisoner single-target square-table selection using the existing alive-target candidate set;
- Butler master single-target square-table selection using the existing non-self candidate set;
- Monk protection single-target square-table selection using the corrected Monk target-legality authority;
- Demon kill single-target square-table selection using the existing alive-target candidate set;
- Ravenkeeper single-target square-table selection using the corrected any-player target authority;
- Chambermaid ordered two-target square-table selection using the existing Chambermaid candidate authority;
- Fortune Teller keeps its dedicated two-target/result owner and restores normal Finish/Next navigation;
- Ravenkeeper RoleReveal, Chambermaid Number, and Fortune Teller YesNo player displays retain typed semantic subject-seat highlighting;
- setup EitherOne pair highlighting remains unchanged.

No target legality, recommendation ranking, reliability semantics, or role-resolution truth is calculated in the new square-table owners.

## Typed presentation owners

Added:

- `ClocktowerNightActionSquareTableUi.kt`
- `ClocktowerChambermaidSquareTableUi.kt`
- `ClocktowerChambermaidPresentationSemantics.kt`

Durable tests cover supplied selectable-seat authority, selected-first/selected-second state, typed Chambermaid subject seats, and typed player-display subject highlighting.

## Large-file wiring

Protected wiring used an exact-anchor GitHub Actions one-shot.

Early fail-closed attempts:

- run `33596725845`: stopped before product push because the two Chambermaid materialization paths have different indentation and did not satisfy the initial repeated-anchor assumption;
- run `33597055746`: exact anchors and patch audit passed, then stopped before product push because the generalized highlight helper used Kotlin early returns inside an expression-body function.

Both failures left production files uncommitted.

Corrected one-shot:

- run `33597354601`: PASS
- exact parent / remote-head / four production blob locks: PASS
- focused owner baseline: PASS
- exact Chambermaid dual-site + Fortune Teller multi-site anchors: PASS
- Python patch: PASS
- `git diff --check`: PASS
- exact four-production-file allowlist: PASS
- focused GREEN after wiring: PASS
- `:app:testFast`: PASS
- exact diff audit: PASS
- remote-head recheck: PASS
- product push: PASS
- temporary workflow/script cleanup: PASS

Product wiring commit:
`8f9d78d936c8e8632d3eed6cde8d4ebd25f81ae5`

One-shot cleanup commit:
`14537105f27d61b18114d9ce3f8a2324b0b5db5c`

Temporary one-shot workflow/script are absent from the cleaned branch.

## Stacked diff before this checkpoint doc

Compared with hotfix base `e369ede31d82c687139b6cb6679e91e91fdafcdb`, the cleaned UI-R4B branch contains only:

- 3 dedicated production owners;
- 4 localized production wiring/presentation files;
- 3 durable typed tests;
- the UI-R4B scope plan.

No temporary `.github` files remain.

## Final validation requested by this checkpoint

The repository PR workflows listen only to PRs whose base is `main`. PR #76 is temporarily retargeted to `main` only for this acceptance run; after the checks finish it must be restored to `hotfix/ravenkeeper-monk-target-legality`.

This commit intentionally carries `[full-ci]` so the final field-test candidate receives ordinary broad validation in addition to the successful one-shot `:app:testFast` checkpoint.

Required final evidence:

- ordinary CI green;
- Android unit tests / debug APK build green;
- ASP contracts green;
- Real Clingo cross-validation green;
- CI gate green;
- R2 main-thread boundary green.

## Scope guards

Not included:

- EPI-MQ / Productive Uncertainty;
- recommendation scoring/ranking changes;
- legal-domain redesign;
- Mayor redirect redesign;
- Imp succession redesign;
- Host/App decomposition;
- unrelated persistence/history work;
- broad theme/animation changes.

After final CI/R2 is green, UI-R5 stabilization can be rebased/retargeted onto UI-R4B. Real-device walkthrough remains a separate pending acceptance gate because no Android phone is currently available for manual testing.
