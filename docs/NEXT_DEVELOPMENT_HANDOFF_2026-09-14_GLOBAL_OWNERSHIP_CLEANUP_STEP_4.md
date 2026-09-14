# NEXT DEVELOPMENT HANDOFF — Global Ownership Cleanup Step 4

> Date: 2026-09-14 Australia/Sydney
> Status: **IMPLEMENTED on `codex/global-ownership-cleanup-4`; remote acceptance pending**
> Goal: **production and restore tests consume one unfinished-night composition boundary**

## 1. Baseline and scope

Step 4 began from merged `main` `b77560628054dee90c78a91d4bab8cb4185fda59`, which includes
Step 3 via PR #126. The live fan-out and architecture pre-flight are recorded in section 11 of
`docs/GLOBAL_CODE_OWNERSHIP_AND_DEAD_CODE_AUDIT_2026-09-14.md`.

This slice is limited to the unfinished-night reconstruction/restore entry-point mismatch. It does
not change persistence schema, recovery defaults, game mechanics, No Greater Joy setup, Host/App
state ownership or square-table presentation.

## 2. Implemented boundary

- `NightTransactionRestoreComposition.compose` accepts the typed checkpoint used by production.
- `NightTransactionRestoreComposition.restore` decodes persisted values and delegates to `compose`.
- Production Host and all focused reconstruction/restore/Host-integration contracts enter through
  this object.
- The former separately callable `NightTransactionReconstructor` implementation was moved unchanged
  behind the composition owner and its source file was removed.

## 3. Required acceptance

- Verify the PR base remains the Step 3 merge commit and the changed-file list stays within Step 4.
- Review the exact algorithm move for semantic identity; no persistence or gameplay behavior change
  is authorized.
- Require focused GREEN, `:app:testFast`, `:app:testFull :app:assembleDebug`, `git diff --check`,
  remote CI and R2 boundary validation.
- Keep the PR Draft and unmerged until the user explicitly authorizes merge.

## 4. Remaining sequence

5. Cut No Greater Joy setup over to the generic provider/source/shown-identity pipeline.
6. Finally reduce Host/App gameplay ownership and converge square-table presentation, including
   shared phase conversion and mechanical-projection ownership.

Do not begin Step 5 in the Step 4 PR.

## 5. Stable rule

> Persisted and typed checkpoints are two inputs to one composition owner; production and tests must
> not bypass it with a second callable reconstruction path.
