# NEXT DEVELOPMENT HANDOFF — 2026-08-23

> Project: `Jazz0006/CampBoardGameHost`  
> Parent roadmap: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Development operations: `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`  
> Large-file execution: `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`  
> Current next product task: **Historical Action + Observation Capture**  
> Status: **CURRENT HANDOFF**

## 1. Trusted baseline

Latest validated product source baseline:

```text
main / PR #40 merge
205473868b50e159977a8ad34e2cf239a711a79d
```

PR #40 final feature head:

```text
4a083b45e1f0525ca49ff7d6968da7e6d373ca1e
```

PR #40 final validation:

```text
CI #439
  Android unit tests + debug APK      GREEN
  ASP contract tests                  GREEN
  Real Clingo cross-validation        GREEN
R2 #382                               GREEN
fresh Codex review                    CLEAN / 👍
review threads                        ALL RESOLVED
```

A new session must still query live `main`; do not assume the SHA above remains HEAD forever.

## 2. What just completed

### PR #39 — Storyteller Information Decision Foundation

Foundation is merged and owns the shared decision seam between recommendation and manual Storyteller selection.

Current semantic pipeline:

```text
actual / registered state
  ↓
legal information builder
  ↓
impairment policy
  ↓
InformationDecisionContext
  ├ recommendation
  └ manual legal candidates
  ↓
Storyteller confirm
  ↓
shared validation
  ↓
EpistemicObservationDraft
  ↓
ClocktowerGameSession
```

### PR #40 — Structured Manual Storyteller Information UI, first production slice

The first production rollout is Empath numeric information only.

Important final behavior:

- legal structured 0/1/2 choices;
- recommendation/manual use the same Foundation validation;
- healthy fallback can still display truthful information even when selector produced no automatic recommendation;
- later-night previous shown number is preserved;
- assisted recommendation uses actual display options;
- telemetry commit occurs only when the legacy selector actually produced a preview;
- fallback `onShowPlayerDisplay` remains outside telemetry guard.

Do not treat PR #40 as “all information roles manual UI complete.”

## 3. Developer workflow decision from PR #40 / #41 exploration

Large file editing through GitHub connector repeatedly hit the same boundary: a tiny logical change inside `ClocktowerHostScreen.kt` still requires whole-file replacement if connector file APIs are used.

A permanent remote trusted writer was explored in PR #41. Static CI and safety contracts could pass, but pre-merge end-to-end validation could not run because `issue_comment` workflows only activate after the workflow exists on the default branch.

Decision:

```text
remote permanent writer
  NOT ADOPTED
```

Standard large-file flow is now:

```text
ChatGPT
  -> patch + tests + Luna prompt
Codex Luna local worktree
  -> apply/check/test/commit/push
ChatGPT
  -> remote exact diff / CI / review / PR gate
```

For a patch task, Luna must push the tested commit to the current feature branch. Merely applying locally is not sufficient for ChatGPT to continue remote audit.

## 4. PR #41 status

PR #41 is no longer a remote writer rollout.

It is being reduced to docs/infrastructure only:

- keep `.gitattributes` LF policy;
- document ChatGPT ↔ Codex Luna patch workflow;
- update connector operations documentation;
- update current roadmap / handoff / README;
- record remote writer exploration as not adopted.

Do not merge remote writer workflow/parser into main.

No product R6 behavior belongs in PR #41.

## 5. Immediate next product objective

After developer-workflow docs are settled, create a new focused tests-first branch from latest `main` for:

# Historical Action + Observation Capture

The purpose is to capture enough semantic action/observation history for later historical reconstruction without prematurely implementing the historical solver.

First perform an audit of current production capture coverage:

- semantic event/action types already emitted;
- Global timeline sequence ownership;
- observation drafts already committed;
- death / execution / registration / night action coverage;
- persistence schema and restore behavior;
- gaps between actual game actions and durable semantic history.

Then define the smallest RED contracts.

## 6. Likely first-slice model

The exact names are not predetermined, but the boundary should resemble:

```text
production action occurs
  ↓
semantic action draft / record
  ↓
session-owned Global identity
  ↓
durable semantic history

information decision confirms
  ↓
EpistemicObservationDraft
  ↓
session-owned Global identity
  ↓
durable semantic history
```

Action and observation should be distinguishable but share timeline authority.

Do not create another timeline cursor.

## 7. Required questions before writing RED tests

Audit and answer:

1. Which physical/night/day actions are already represented semantically?
2. Which actions currently exist only as UI/session state mutations?
3. Which observation records are already globally sequenced?
4. Does any path assign identity outside `ClocktowerGameSession`?
5. What minimum history is required for the first multi-night reconstruction test?
6. Which legacy restored-game paths must remain compatible?
7. Can the first slice avoid history UI changes entirely?

## 8. Explicit non-goals

Do not combine the first Historical Capture PR with:

- history UI redesign;
- all-role structured manual UI rollout;
- misinformation probability tuning;
- Investigator balance changes;
- broad evil-side balance changes;
- Spy/Recluse registration rewrite;
- A3 multi-night solver expansion itself;
- B4 or ZDD production promotion;
- ML / personalized tuning.

If a true correctness bug is found during audit, classify it separately before broadening scope.

## 9. Development execution rules

For behavior changes:

```text
live main recheck
-> new focused branch
-> tests-only RED
-> real CI RED evidence
-> smallest GREEN
-> focused tests
-> full CI / R2
-> exact diff
-> final review
-> stop ready-for-merge
```

If GREEN touches a connector-truncated large file:

1. ChatGPT outputs exact patch;
2. ChatGPT outputs local test commands;
3. ChatGPT outputs Luna execution prompt;
4. user passes those to Codex Luna;
5. Luna apply/check/tests;
6. Luna commit + `git push origin HEAD`;
7. user returns commit SHA/result;
8. ChatGPT fetches GitHub and resumes audit.

Luna does not merge.

## 10. Stop condition

The next product development session should stop when:

```text
Historical Action + Observation Capture focused PR exists
RED provenance is real and documented
minimal production capture seam is implemented
Global identity authority remains in session
focused/full CI + R2 are GREEN
exact diff is clean
final review is clean
PR is ready for explicit merge authorization
```

Do not automatically continue into A3 historical multi-night solving in the same slice.
