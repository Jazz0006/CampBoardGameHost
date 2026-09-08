# CI docs-only main-push policy

> Date: 2026-09-08 Australia/Sydney
> Status: active

Pure documentation / explicitly safe non-executable pushes to `main` must not run heavy validation or rebuild the field-test APK.

Current policy:

- `workflow_dispatch` remains a full acceptance gate;
- PR iteration keeps the existing path-based routing and `[full-ci]` escalation;
- a `main` push containing any non-documentation/executable/configuration surface runs full T4;
- a `main` push containing only `docs/**`, `AGENTS.md`, `README.md`, or `player/**` runs only the CI change classifier plus aggregate gate;
- the Field Test APK push workflow is skipped for the same safe-only `main` pushes.

The CI routing fix was introduced by `07b5448a0013a6888fe133531c48c377f22ecacc` and the Field Test APK trigger fix by `10de791c7bcc981acc354327b969dfa5e7a49a33`.
