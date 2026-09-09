# D6.2aa — Final acceptance gate

> Date: 2026-09-09 Australia/Sydney  
> Branch: `codex/d6-2-ui-composition`  
> PR: #115  
> Production-equivalent validated code/test head: `b2263cd08bc2ce223598698324bf2b22243c91f2`  
> Status before this gate: structural acceptance PASS; final PR diff/ownership PASS.

## Real-device waiver

The user explicitly authorized proceeding without the real-device critical-path verification for this merge. Real-device coverage is therefore **WAIVED FOR PR #115 MERGE**, not claimed as executed or passed.

This waiver applies only to the merge-readiness decision for the current D6.2 first-wave PR. It does not convert the missing real-device evidence into test evidence and does not remove the later UI-R5 / real-device stabilization work.

## Fresh FULL/T4 requirement

A fresh FULL/T4 remains required before merge.

The repository CI contract explicitly treats a PR synchronization commit whose message contains `[full-ci]` as a full acceptance checkpoint. This documentation-only commit intentionally uses that supported acceptance mechanism; it does not alter production source, tests, workflow routing, persistence, recommendation semantics, Recovery, A4 behavior, or runtime ordering.

The expected FULL gate is:

- Android full JVM tests + debug APK;
- ASP contract tests;
- Real Clingo cross-validation;
- CI gate;
- R2 boundary verification on the same documentation descendant / production-equivalent tree.

All commits after `b2263cd08bc2ce223598698324bf2b22243c91f2` are documentation-only. Therefore a successful FULL run on this commit validates the same production/test/workflow tree previously reviewed in D6.2z.

## Merge rule

Merge PR #115 only if:

1. this `[full-ci]` checkpoint completes successfully;
2. R2 is successful;
3. PR remains mergeable and has not moved unexpectedly;
4. no new production/test/workflow changes appear after the reviewed `b2263cd...` tree.

If those conditions hold, the user has explicitly authorized immediate merge.
