# TBSP-6K Final Acceptance Checkpoint

> Date: 2026-08-31 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Branch: `codex/trouble-brewing-setup-presets-v2`
> Draft PR: #57

## Status

TBSP-6K final integrated acceptance is **RUNNING**.

This is a validation-only checkpoint. It changes no production or test code.

Production tree under acceptance:

- TBSP-6H-B production checkpoint: `ff1c99fe97552dc65f3d1bf8326bdb451c8e25a0`
- TBSP-6I acceptance-test checkpoint: `f7e877f6881cc74b9d8e7f4f8db2b2fb406b84d4`
- TBSP-6J production cleanup: `68d29c53a0a37f2c30b9d88ed8967d5d9548b4bc`

Required final gate:

- full Android JVM suite via `:app:testFull`;
- debug assemble;
- ASP contract validation;
- real Clingo cross-validation;
- R2 main-thread boundary;
- final CI aggregate gate;
- final PR scope/diff audit.

The `[full-ci]` commit carrying this checkpoint intentionally selects the repository T4 route defined in `docs/TESTING_STRATEGY.md` and `.github/workflows/ci.yml`.

If any selected gate exposes a real regression, TBSP-6K must stop and the defect must move to a separate minimal repair slice. No production behavior change belongs inside this acceptance checkpoint.
