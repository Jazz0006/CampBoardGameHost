# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-25
> 文档角色：**CURRENT / 当前状态唯一权威**
> Repository: `Jazz0006/CampBoardGameHost`
> Stable `main`: `0311c3bb54ea71be69bc60a4aae642e0f39cd900`
> Current branch/worktree: `codex/source-decomposition-app-root-s7-5-new-demon-presentation`
> PR #48: **MERGED / CLOSED — historical multi-night exact baseline**
> PR #49: **MERGED / CLOSED — app-root S7.1/S7.2 structural checkpoint**
> PR #50: **MERGED / CLOSED — test execution tiers + path-aware CI**
> Current project priority: **APP-ROOT DECOMPOSITION — S7.5 NEW DEMON PRESENTATION**
> Detailed test strategy: `docs/TESTING_STRATEGY.md`
> App-root handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_S7.md`

## 1. Project status

```text
Phase A correctness foundation                     PASS
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic prerequisites                          CLOSED
PR #39 Information Decision Foundation             CLOSED / MERGED
PR #40 Structured Manual UI                        CLOSED / MERGED
PR #42 Historical Action + Observation Capture     CLOSED / MERGED
PR #44 Drunk / Fortune Teller hotfix               CLOSED / MERGED
PR #43 Clocktower host decomposition A1–A13        CLOSED / MERGED
App-root decomposition S0–S6                       CLOSED / MERGED CHECKPOINT
App-root S7.1/S7.2                                  CLOSED / MERGED via PR #49
App-root S7.3                                       CLOSED / STACKED STRUCTURAL CHECKPOINT
App-root S7.4                                       CLOSED / STACKED STRUCTURAL CHECKPOINT
App-root S7.5                                       ACTIVE — NEW DEMON PRESENTATION
PR #48 historical multi-night exact baseline       MERGED / CLOSED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN
Session -> B4 historical shadow seam               GREEN
Setup snapshot ownership / persistence             NOT STARTED
Production recommendation authority promotion      NOT STARTED / NOT AUTHORIZED
Test execution workflow S1–S3                      COMPLETE / MERGED via PR #50
```

## 2. Test execution workflow

PR #50 is merged. The validated S3 workflow checkpoint included:

```text
S3 workflow head                            2135ea19d09ee19a3cb5e9357414efb6acf33f72
CI #684                                     SUCCESS
  Classify changes                          SUCCESS
  Android tests (:app:testFull + assemble)  SUCCESS
  ASP contract tests                        SUCCESS
  Real Clingo cross-validation              SUCCESS
  CI gate                                   SUCCESS
R2 #615                                     SUCCESS
```

The final integration subsequently completed and stable `main` is now `0311c3bb54ea71be69bc60a4aae642e0f39cd900`.

Current local structural validation follows `docs/TESTING_STRATEGY.md`:

```text
T0 focused RED/GREEN
-> T1 :app:testFast
-> T2 affected regression / compile checks
-> triggered T3 only when semantic area requires
-> PR T4 applicable FULL gate
```

Do not run the full Android suite merely because an old decomposition handoff once required it. Escalate when impact is uncertain.

## 3. Protected architecture contracts

### Historical exact / A3-B4

- Durable observations are consumed exactly once by GLOBAL historical replay; setup-only knowledge constrains initial worlds only.
- Historical order and actor eligibility are separate concerns.
- Mechanically identical possible worlds converge even when hidden provenance differs.
- Exact historical construction remains Trouble Brewing-only and fails closed outside that boundary.
- `rolesBySeat` is immutable setup identity; `currentRolesBySeat` is dynamic historical current-role state.
- Hidden Storyteller choices may exist in persisted truth, but actual hidden targets/occurrence points do not become player constraints.

Current production-isolated chain remains:

```text
real ClocktowerGameSession
-> GLOBAL action + observation history
-> ClocktowerB4HistoricalShadowCoordinator
-> B4DynamicPlayerWorldSetShadow(validated ruleset)
-> EnumeratedHistoricalExactBaseline
-> B4 shadow cardinality report
```

Still deliberately not connected to Host recommendation authority, A4/ZDD selector authority, history UI, or other scripts.

### App-root decomposition

Do not move merely for byte reduction:

- `CampBoardGameHostApp()` orchestration/state ownership;
- Compose effect lifetime;
- persistence / restore / archive timing;
- Clocktower live transaction ordering;
- `ClocktowerGameSession` authority;
- action/observation/history ordering;
- A4 lifecycle/cache/prewarm effects;
- planner/projector/materializer authority;
- Host recommendation/registration transaction semantics;
- A3/B4 historical-exact behavior.

`ClocktowerHostScreen.kt` remains under new-responsibility growth freeze, not a byte-target campaign.

The 50 KiB target is a maintainability guideline, not a hard gate.

## 4. App-root decomposition progress

Original Root:

```text
CampBoardGameHostApp.kt = 325,556 bytes
```

Validated progression:

```text
After S1   = 293,536 bytes
After S2   = 283,470 bytes
After S3   = 277,453 bytes
After S4   = 261,232 bytes
After S5   = 235,741 bytes
After S6   = 229,822 bytes
After S7.1 = 229,007 bytes
After S7.2 = 228,172 bytes
After S7.3 = 220,403 bytes
After S7.4 = 218,086 bytes
```

### S7.3 — shared host interaction UI — CLOSED

Checkpoint:

```text
head: b6d5d407d8d753bfe1fd4f9a5b16f58881468fa7
owner: HostInteractionUi.kt = 8,901 bytes
```

Moved exactly:

```text
HostProgressCard
HostScriptCard
HostInstructionBlock
HostActionSection
SelectablePlayerChips
SelectableTwoPlayerChips
SelectableSeatNumbers
```

Remote exact-diff audit: PASS. No GitHub Actions run was observed on that branch head; do not call it remotely CI-green.

### S7.4 — shared game-settings presentation — CLOSED

Lineage from S7.3:

```text
4a46210cf11a0c45ceffef7bfaa647e53e99747f  RED — ownership contract
843f61e5106b704417b1cd347c47393264215bb8  stale structural-test repair
bcd8b9b2dad4f3cde42bef2cdb6d1eea7839dd7e  GREEN — extraction
7c36d7d1fb95d9bae66439acd3523e86ab9b6e72  docs closeout
```

Owner:

```text
GameSettingsUi.kt = 3,112 bytes
```

Moved exactly:

```text
GameSettingsHeader
StepperRow
```

`EmptyStateCard` remains Root-owned and deferred. Do not create `Utils.kt` / `CommonUi.kt` merely to move it.

Reported local gates: focused T0 GREEN, `:app:testFast` GREEN after stale ownership-contract migration, `:app:assembleDebug` GREEN, exact local diff audit PASS. Remote exact-diff audit also PASS. No GitHub Actions run was observed on the S7.4 branch; do not describe it as remotely CI-green.

## 5. Immediate route

Fresh post-S7.3 audit established the remaining S7 order:

```text
S7.3 shared host interaction UI             CLOSED
-> S7.4 shared game-settings presentation  CLOSED
-> S7.5 New Demon presentation             ACTIVE
-> S7.6 Werewolf presentation leftovers
-> STOP / remeasure Root / fresh S8 audit
```

### S7.5

Move only after the tests-first dependency/transaction guard:

```text
ClocktowerNewDemonConfirmationScreen
CampBoardGameHostApp.kt
  -> clocktower/ui/ClocktowerNightScreen.kt
```

The move must remain presentation-only. Host/session state, Imp self-kill succession, new-Demon selection, player-display routing, confirmation callback, audit/history ordering and transaction authority must stay where they currently belong.

### S7.6 candidate

After S7.5 closes, audit:

```text
WerewolfRoleLine
WerewolfPlayerStatusRow
-> werewolf/WerewolfHostScreen.kt
```

Then STOP and remeasure before any S8 work.

## 6. Deferred higher-risk work

The Root front section still contains models, prefs, JSON/persistence schema, setup/catalog/assignment helpers and small presentation adapters. Do not start these as part of S7.

Potential S8 work requires a fresh audit. Persistence/schema work is conditional and higher risk because it touches restore compatibility, timeline identity, GLOBAL sequence identity and saved-game semantics.

A3 setup-snapshot ownership remains paused and separate from decomposition work. Do not combine S7 with Host authority promotion, A4/ZDD promotion, other scripts, history UI, misinformation expansion, persistence schema or setup-snapshot work.

## 7. Working discipline

1. Re-query live `main`, structural branch head and parent chain before each slice.
2. ChatGPT owns architecture/boundary judgment, test design, symbol inventory, exact-diff acceptance criteria and remote audit.
3. Luna performs large-Root mechanical edits in the user's full local Git worktree.
4. Keep tests-first RED and GREEN provenance explicit.
5. Classify stale structural assertions by the invariant they should protect; do not preserve obsolete Root-location facts.
6. Do not merge, mark ready, rebase, force-push or broaden scope without explicit user authorization.
7. Documentation is updated for meaningful route/phase changes and cross-conversation handoffs, not every small SHA.
