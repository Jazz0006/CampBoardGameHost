# TBSP-6J Cleanup Checkpoint — 2026-08-31

## Scope

This checkpoint records the completed TBSP-6J behavior-preserving cleanup implementation and triggers a normal PR synchronize event for final repository CI/R2 validation. It does not begin TBSP-6K and does not authorize merging or marking PR #57 Ready.

## Production cleanup

- Product commit: `68d29c53a0a37f2c30b9d88ed8967d5d9548b4bc`
- Changed production file only:
  - `app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt`
- Exact cleanup:
  - removed unused `preparedSetupPlan: RecommendationPlan? = null` from `resetDealState`;
  - changed the only four-argument legacy/NGJ call to `resetDealState(GameKind.Clocktower, script, preparedSeed)`.
- The local legacy/NGJ `preparedSetupPlan` calculation remains unchanged and still feeds `recommendedDrunkShownRole`.
- One-shot workflow/script cleanup commit: `ab1a57393a9abfd774dcdf4776f81134ed19a81a`.

## One-shot validation

GitHub Actions run `33342673392` completed successfully with all of the following GREEN:

- exact bootstrap head and App source-blob verification;
- exact two-anchor mechanical patch;
- `:app:compileDebugKotlin`;
- focused `NoGreaterJoySetupRegressionTest` with `--rerun-tasks`;
- `:app:testFast --rerun-tasks`;
- `git diff --check`;
- exact changed-file audit limiting production changes to `CampBoardGameHostApp.kt`;
- explicit assertions that the local `preparedSetupPlan` calculation and `recommendedDrunkShownRole` consumer remain present;
- remote-head recheck before product push;
- product commit/push;
- temporary workflow/script self-cleanup.

## Exact production diff

The product commit contains no gameplay, setup-selection, persistence, recommendation, First Night, history, navigation, or NGJ semantic change. It is limited to deleting the dead parameter and its dead call-site argument.

## Final acceptance gate

The GITHUB_TOKEN-generated cleanup head reports CI/R2 `action_required` with zero jobs. Treat that as a workflow-trigger artifact, not GREEN evidence and not a code failure.

TBSP-6J is accepted only when the normal PR CI and R2 runs triggered by this docs-only checkpoint are GREEN against the same production tree. Stop after 6J acceptance; do not begin TBSP-6K in this checkpoint.
