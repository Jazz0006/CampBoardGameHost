# UI-R5 Square-table Convergence — Closeout Index

> Closed: 2026-09-10 Australia/Sydney  
> Merged PR: #117  
> Main merge commit: `b47b00fd0c727e048dcb1b57260b8dd6fff466a1`

## Final automated validation

```text
final logical T4: 2958f334fc7cccd59ed2e75a3bdaa60684492292
CI #2143 / run 34435295215 PASS
- Android full unit tests + debug APK PASS
- ASP contract tests PASS
- Real Clingo cross-validation PASS
- CI gate PASS
R2 #2010 / run 34435295217 PASS

post-merge main CI #2146 / run 34437247431 PASS
Field Test APK #34 / run 34437247434 PASS
```

## Completed implementation scope

UI-R5 converged the Storyteller night-role experience toward square-table presentation where that interaction model adds clarity, including pair-information, Chef, Empath, Undertaker, Ravenkeeper, Spy, Clockmaker and Sage surfaces. It also retired the unreachable legacy pair-manual dialog/center controls and removed the superseded generic non-pair result-first surface while preserving live generic compatibility paths.

The campaign preserved typed semantic ownership: UI presentation does not parse localized display strings back into game semantics, unreliable display choices do not gain epistemic propositions merely for rendering, and Host/session/rule ownership remains outside UI composition.

## Historical handoffs

Archived under `docs/archive/handoffs/`:

- `NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`
- `NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_R5_REMAINING_NIGHT_ROLES.md`

These files are historical execution context only.

## Historical checkpoint / audit evidence

Archived beside this index:

- `UI_R5_PAIR_INFORMATION_REAL_DEVICE_ACCEPTANCE_2026-09-09.md`
- `UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`
- `UI_R5_GENERIC_RESULT_REACHABILITY_AUDIT_2026-09-10.md`

The durable product-design reference remains in the active docs root:

- `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

## Device acceptance note

Automated implementation is complete and merged, but full cross-role real-device acceptance was not claimed.

User-reported device PASS exists for:

- Washerwoman / Librarian / Investigator pair-information baseline;
- Chef.

Post-merge field testing should still exercise Empath, Undertaker, Ravenkeeper, Spy, Clockmaker and Sage, plus the original dynamic-trigger regression scenario:

```text
Monk initially protects Ravenkeeper
-> change Monk protection target
-> Demon kills Ravenkeeper
-> Ravenkeeper ability step must appear
```

Any resulting defect should be handled as a focused bugfix from current `main`; it does not reopen UI-R5 as a broad migration/decomposition campaign.

## Current development authority

Do not resume work from this closeout index. Current execution authority is:

- `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
- the single active handoff linked from `docs/README.md`.
