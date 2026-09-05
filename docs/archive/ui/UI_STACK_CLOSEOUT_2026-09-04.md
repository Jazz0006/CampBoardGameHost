# UI Stack Closeout — 2026-09-04

## Purpose

Prepare the accumulated Storyteller UI / Persistent Host Table stack for one integration merge into `main` without reopening already validated slices.

## Live baseline at closeout start

- `main`: `bf37bbbced8b1ec71a1ffe209954d328de453c95`
- top stacked product checkpoint: PR #92 / `codex/ui-r4d6-4b-mayor-redirect-table`
- PR #92 head: `5501fb02cf37fa2da9ad63bbef7d78608784d787`
- closeout branch: `codex/ui-stack-closeout-2026-09-04`

The closeout branch starts from the exact #92 head and carries forward the field-test APK infrastructure already present on `main` (`app/build.gradle.kts`, `.github/workflows/field-test-apk.yml`, `docs/FIELD_TEST_APK_DISTRIBUTION.md`, `tools/setup_field_test_keystore.sh`).

## Included stacked work

The integration candidate includes the validated lineage from UI-R1 through the latest R4D-6 migration slice, including:

- reusable square-table seat surface;
- pair Manual selection;
- player-facing information reveal corrections;
- Fortune Teller square-table adjudication;
- Monk/Ravenkeeper legality hotfix;
- shared night-action square-table surfaces;
- persistent Host Table foundation and stable seat identity;
- seating-first setup and real-device F1-F7 corrections;
- Day Overview persistent workspace;
- nomination gesture;
- individual voting, ghost-vote authority, detailed voter history and typed vote transaction;
- Slayer / Artist / Klutz table migrations;
- Red Herring selection migration;
- Mayor redirect selection migration.

## Explicitly not included

- PR #74 UI-R5 early stabilization branch: superseded by the later hotfix / R4B / R4C / R4D lineage and should be closed rather than merged.
- EPI-MQ / Productive Uncertainty / PlayerWorldSet work.
- recommendation ranking redesign.
- Demon Successor migration and other still-unmigrated legacy interaction surfaces.
- the newly identified Night wake/action lifecycle redesign described below.

## Known accepted deferred issue

Real-device testing on 2026-09-04 identified that night action roles can enter a full-screen square-table action selector before the Storyteller receives a clear wake prompt. Fortune Teller is a concrete example; the structural issue also affects other action-role selectors.

This is accepted as a UX architecture gap for the next independent slice rather than patched locally during closeout.

Next-slice intent:

```text
existing Persistent Host Table
-> NIGHT WAKE indication on stable physical seat
-> center task area
-> ACT / target selection
-> RESOLVE when required
-> sanitized SHOW / Player Reveal when required
-> COMPLETE
```

The acting/waking seat should remain visible in the same persistent table and be emphasized with a strong seat highlight plus clock-hand/arrow-like directional cue. The center area owns the current task. This extends the existing Host Table architecture; it does not create a second table framework.

## Merge gate

Do not merge this closeout candidate until all of the following are true:

1. closeout PR targets live `main` and is mergeable;
2. PR diff confirms `main` field-test infrastructure is preserved rather than reverted;
3. final GitHub R2/main-thread boundary is GREEN;
4. final `[full-ci]` validation is GREEN, including full Android JVM, ASP contracts, Real Clingo cross-validation, and CI gate as classified by the repository workflows;
5. `git diff --check` / merge-candidate diff audit is clean;
6. roadmap reflects the actual stack through #92 and records the deferred Night wake/action slice;
7. no new product behavior is added during closeout.

## Merge / cleanup plan after acceptance

Use a merge commit for the closeout PR so historical validated checkpoint commits remain ancestors of `main`.

After the closeout PR is merged:

- close included stacked PRs as `included via UI stack closeout`;
- close PR #74 as `superseded`;
- re-query live `main` and verify the merged tree;
- create the next Night persistent-table wake/action branch from the new `main`;
- do not start EPI-MQ until the current UI stabilization decision says the UI campaign is stable.
