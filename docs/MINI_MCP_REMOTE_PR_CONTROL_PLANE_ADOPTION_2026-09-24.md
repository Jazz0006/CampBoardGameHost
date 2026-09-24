# Mini MCP Remote PR Control Plane Adoption

Date: 2026-09-24  
Status: M8G5 runtime adoption canary

## Purpose

CampBoardGameHost uses Mini MCP as the default controlled repository workspace. The M8G GitHub Remote PR Control Plane extends that same workspace with the minimal GitHub-native surface needed for normal development continuity:

- PR audit;
- bounded CI/check drill-down;
- failed job-log retrieval;
- guarded draft/ready transitions;
- guarded merge for repositories with an explicit merge policy.

GitHub remains the canonical remote system. Mini MCP is the preferred interface to that remote state when the live runtime exposes the reviewed M8G tools. GitHub Connector remains a fallback rather than a required second connector.

## Live capability rule

Do not infer capability from documentation alone.

Before using the remote control plane in a conversation, verify the live Mini MCP tool manifest. The expected M8G surface is:

```text
github_pr_audit({ repo, pr })
github_ci_detail({ repo, pr })
github_job_log({ repo, pr, job })
github_pr_mark_ready({ repo, pr, expectedHeadSha })
github_pr_convert_to_draft({ repo, pr, expectedHeadSha })
github_pr_merge({ repo, pr, expectedHeadSha, method })
```

If these tools are absent, use GitHub Connector or manual GitHub operations for the missing remote capability. Do not pretend the deployment has been refreshed merely because the code exists in the Mini MCP repository.

## Normal development loop

Preferred path when the live M8G surface is available:

```text
Mini MCP repo/read/search
-> targeted edit
-> local validation where supported
-> diff/status review
-> guarded stage
-> staged diff review
-> commit
-> network-observed remote audit
-> guarded push
-> github_pr_audit
-> github_ci_detail / github_job_log only when needed
-> repair and repeat if required
-> guarded PR lifecycle action only with user intent
```

GitHub Actions remains the independent Android acceptance surface while the Oracle ARM64 host lacks the required Android build environment.

## Remote-state authority

GitHub actual state is canonical for:

- branch/PR identity;
- exact PR head;
- base branch/base commit;
- CI/check results;
- mergeability;
- reviews and review threads;
- final merged state.

Mini MCP must re-read live GitHub state at the point where it matters. Cached conversation state, local Git state, and prior tool output are evidence, not authority for a later mutation.

## Ready/draft transitions

Ready/draft transitions are reversible lifecycle operations but still require:

- open PR;
- configured base repository/branch;
- exact previously reviewed head SHA;
- expected source draft state;
- mutation-result identity verification;
- live post-condition verification.

The GitHub API does not provide the same atomic exact-head compare for ready/draft transitions that it provides for merge. A detected race must be reported rather than silently treated as success.

## Merge safety

A pushed commit is never merge authorization.

A real merge requires clear user merge intent in the conversation. No magic phrase or fake authorization parameter is required, but a generic “continue” does not authorize merge unless the immediately preceding context explicitly identified that exact merge as the next action.

For `clocktower`, Mini MCP merge is enabled only through configured guarded squash policy. The merge path requires:

- an explicit repository merge policy;
- open, non-draft PR state;
- configured base repository/branch;
- exact reviewed PR head SHA;
- confirmed mergeability;
- required CI/check evidence bound to the exact PR test-merge commit;
- complete, non-truncated required evidence;
- passing `CI gate`;
- passing `verify-boundary`;
- unchanged head SHA, base SHA, and test-merge SHA immediately before mutation;
- GitHub's merge-endpoint `sha` guard;
- allow-listed squash method;
- verified merged post-condition.

If the merge request may have reached GitHub but the response or post-read is lost, treat the result as ambiguous. Re-audit the PR before any retry.

## Credential model

The default single-developer deployment uses one server-owned selected-repository GitHub credential:

`MINI_MCP_GITHUB_TOKEN`

Mini MCP resolves that token into the internal read, ready/draft, and merge credential slots. This simplifies operator maintenance without changing the public MCP surface or any exact-state/CI/merge safety gate.

Optional hardened deployments may override one or more traffic classes with:

- `MINI_MCP_GITHUB_READ_TOKEN`;
- `MINI_MCP_GITHUB_WRITE_TOKEN`;
- `MINI_MCP_GITHUB_MERGE_TOKEN`.

A dedicated non-admin/non-bypass merge principal remains an optional team/production hardening measure, not a requirement for this personal M8G5 canary.

Secrets never belong in this repository, documentation, prompts, or MCP caller input.

## Repository-specific merge policy

Current state:

- `clocktower`: guarded squash merge enabled with explicit CI gates;
- `mini`: merge disabled because the repository currently has no remote GitHub Actions CI gates.

Absence of a merge policy means fail closed.

## Fallback

Use GitHub Connector when:

- the live Mini MCP M8G tools are absent;
- runtime credentials are unavailable or invalid;
- GitHub-native functionality outside the intentionally narrow Mini MCP surface is required;
- recovery requires direct inspection outside the configured Mini MCP policy.

Do not broaden Mini MCP into arbitrary REST/GraphQL passthrough merely for connector parity.

## M8G5 acceptance

The adoption canary is complete only after a real controlled Draft PR proves:

1. live M8G tool manifest after tunnel restart and plugin Refresh;
2. real PR audit against exact head/base;
3. real CI/check inspection;
4. reversible draft/ready transition and post-condition;
5. explicitly authorized guarded merge;
6. post-merge audit;
7. normal project guidance updated to this workflow.

Until those runtime checks pass, treat this document as the target operating model rather than proof that deployment is complete.
