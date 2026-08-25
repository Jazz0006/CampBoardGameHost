# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-25
> 文档角色：**CURRENT / 当前状态唯一权威**
> Repository: `Jazz0006/CampBoardGameHost`
> Stable `main`: `0311c3bb54ea71be69bc60a4aae642e0f39cd900`
> Current branch/worktree: `codex/source-decomposition-app-root-s7-4-game-settings-ui`
> PR #48: **historical multi-night exact baseline — merged/closed; preserve current merged status**
> PR #49: **merged — app-root S7.1/S7.2 structural checkpoint**
> PR #50: **MERGED / CLOSED — test execution tiers + path-aware CI**
> Latest fully validated A3 code checkpoint: preserve the current merged repository status; do not revive stale PR #48 language
> Current project priority: **APP-ROOT DECOMPOSITION / POST-S7.3 ROUTE**
> Current test-workflow status: **S1–S3 COMPLETE / MERGED**
> Detailed test strategy: `docs/TESTING_STRATEGY.md`
> App-root S7 handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_S7.md`

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
App-root S7.3                                      COMPLETE / STACKED STRUCTURAL BRANCH
App-root S7.4                                      COMPLETE / LOCAL GREEN SLICE
PR #48 historical multi-night exact baseline       MERGED / CLOSED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN
Session -> B4 historical shadow seam               GREEN
Setup snapshot ownership / persistence             NOT STARTED
Production recommendation authority promotion      NOT STARTED / NOT AUTHORIZED
S1.1 test execution baseline timing                 COMPLETE
S1.2 slow-test/root-cause audit                     COMPLETE
S1.3 tier/escalation design                         COMPLETE
S1.4 documentation/governance sync                 COMPLETE
S2 executable Gradle suites                         COMPLETE
S3 CI impact/path-aware optimization                COMPLETE / MERGED via PR #50
```

S2 established executable Android JVM validation on `codex/test-execution-tiering`:

```text
:app:testFast   751 tests / 0 failures / 0 errors / 0 skipped
:app:testFull   770 tests / 0 failures / 0 errors / 0 skipped
FULL - FAST     exactly 19 testcases from the five approved expensive classes
```

`testFast` does not depend on `testDebugUnitTest`; `testFull` intentionally does. The detailed contract and trigger policy live in `docs/TESTING_STRATEGY.md`.

S3 established conservative path-aware CI routing while preserving FULL validation for every selected component. The validated S3 implementation checkpoint is:

```text
PR #50                                      DRAFT / OPEN / NOT MERGE-AUTHORIZED
S3 workflow head                            2135ea19d09ee19a3cb5e9357414efb6acf33f72
CI #684                                     SUCCESS
  Classify changes                          SUCCESS
  Android tests (:app:testFull + assemble)  SUCCESS
  ASP contract tests                        SUCCESS
  Real Clingo cross-validation              SUCCESS
  CI gate                                   SUCCESS
R2 #615                                     SUCCESS
```

The workflow change intentionally routes all gates, so PR #50 exercised the complete GitHub validation graph. The classifier uses base-to-head changed paths, explicit safe skips for docs/player surfaces, semantic escalation for exact Clocktower areas, fail-safe full routing for unknown surfaces, and `--no-renames` so moves cannot hide an executable old path behind a safe new destination.

The latest docs-only S3 closeout commits may move PR #50 head beyond the validated workflow SHA above. Before any ready/merge decision, re-query the live PR head and checks and confirm those docs-only commits retain GREEN CI/R2. Do not treat the historical `2135ea19…` evidence as authorization to merge a newer unchecked head.

S7.3 is complete on stacked branch `codex/source-decomposition-app-root-s7-3-host-interaction-ui`:

```text
head: b6d5d407d8d753bfe1fd4f9a5b16f58881468fa7
owner: HostInteractionUi.kt
root: CampBoardGameHostApp.kt = 220,403 bytes
owner: HostInteractionUi.kt = 8,901 bytes
exact moved declarations:
  HostProgressCard
  HostScriptCard
  HostInstructionBlock
  HostActionSection
  SelectablePlayerChips
  SelectableTwoPlayerChips
  SelectableSeatNumbers
remote exact-diff audit: PASS
GitHub Actions on b6d5d407 head: none observed
```

S7.4 is the completed local slice on `codex/source-decomposition-app-root-s7-4-game-settings-ui`:

```text
RED:    4a46210c  test: define shared game settings UI ownership
repair: 843f61e5  test: retire stale root ownership assertions
GREEN:  bcd8b9b2  refactor: extract shared game settings UI
owner: GameSettingsUi.kt
moved: GameSettingsHeader, StepperRow
```

The S7.4 local gates passed: focused T0 ownership tests, `:app:testFast`, `:app:assembleDebug`, and exact diff audit. No remote workflow run was observed for the S7.4 head.

PR #48 is merged/closed. Keep the completed A3 history separate from the current App-root S7 work. Future S7 slices still require a fresh live-state and dependency audit before implementation.

## 2. Protected architecture contracts

### H1 — one durable observation path
Historical construction separates setup seed knowledge from GLOBAL_V1 durable observation replay. Durable observations are consumed exactly once.

### H2 — order != eligibility
Night schedule owns canonical order. Historical possible-world state and role semantics own actor eligibility. Triggered exceptions such as Ravenkeeper remain explicit.

### H3 — mechanical identity != provenance
Mechanically identical states converge even when hidden paths/explanations differ. Hidden branch count is not exact world count.

### H4 — support boundary
Historical exact construction remains explicitly Trouble Brewing-only and fails closed outside that boundary.

### H5 — setup role vs current role

```text
rolesBySeat         immutable setup identity
currentRolesBySeat  dynamic historical current-role state
```

### H6 — incremental state-aware replay
GLOBAL history is replayed incrementally. Visible observations are evaluated against the mechanical world state at their own historical point. Hidden mechanics never invent synthetic `globalSequence` values.

### H7 — knowledge-safe hidden mechanics
Storyteller-hidden choices may exist in persisted history, but player possible worlds regenerate hidden mechanics from rules + possible-world state. Actual hidden targets and hidden occurrence points never become player constraints.

## 3. H7 completed checkpoints

```text
H7.1  dynamic current-Demon attack branching       GREEN
H7.2  dynamic current-Monk protection branching    GREEN
H7.3  mechanics materialization boundary           GREEN
H7.4  Imp self-kill succession branching           GREEN
H7.5  self-kill materializer integration           GREEN
H7.6  Mayor night-death branching primitive        GREEN
H7.7  Mayor materializer integration               GREEN
H7.8  canonical Other Night replay transition      GREEN
H7.9  public night-death reconciliation            GREEN
H7.10 no-public-death dawn reconciliation          GREEN
H7.11 persisted hidden-action payload isolation    GREEN
```

Key recent checkpoints:

```text
H7.10 RED   28b56c83b3c8efbca570f3b8c3c40219b3076535
      GREEN 3d446937b7cbef36b3fed679fff64b9582e450ac  CI #662 / R2 #595 GREEN
H7.11 RED   55fd836a7616d9e15a8f57691138ae3ecddc6567
      GREEN 9eea8142c0dd04c2fdf8164c78b5a1799b0b7698  CI #666 / R2 #599 GREEN
```

H7.11 established that persisted `Poison` / `Protect` / `Attack` / `RoleChange` may remain Storyteller truth while `PlayerHistoricalTimeline` projects their payloads and hidden GLOBAL occurrence points to nothing. Rule-derived hidden mechanics remain authoritative for player reasoning.

## 4. Historical exact pipeline

```text
full Storyteller GLOBAL history
-> validate shared timeline identity
-> PlayerHistoricalTimeline projection
   keep public / recipient-visible facts only
   discard hidden targets + hidden occurrence points
-> setup-only exact enumeration
-> rule-derived hidden branching at canonical boundaries
-> reconcile visible public outcomes
-> mechanical convergence
-> exact player world set
```

Trouble Brewing Other Night mechanics are rule-derived:

```text
possible world
-> legal living-current-Monk protection alternatives
-> legal living-current-Imp attack alternatives
-> resolve Demon attack outcome
   NO_DEATH
   TARGET_DIES
   IMP_SELF_KILL_SUCCESSOR_REQUIRED
   MAYOR_TARGET_OR_REDIRECT_CHOICE_REQUIRED
-> mechanical convergence
```

No known Trouble Brewing H1–H7 correctness blocker remains at this exact-baseline boundary.

## 5. Production-isolated B4 integration checkpoints

After H7.11, integration readiness was audited before touching Host or recommendation authority.

### B4 historical-exact shadow bridge — GREEN

Validated code checkpoint before the session seam:

```text
01a9aea20b4f224c6e7f911eef6c3fadb3f62be9
CI #672 SUCCESS
R2 #605 SUCCESS
```

`B4DynamicPlayerWorldSetShadow` now has two intentionally separate paths:

```text
B4DynamicPlayerWorldSetShadow()
-> legacy shadow path
-> hidden transitions may still DEFERRED_B4

internal validated-ruleset opt-in
-> EnumeratedHistoricalExactBaseline
-> historical exact replay
-> shadow cardinality report only
```

The validated-ruleset constructor is internal so this work does not accidentally widen public authority.

### Session -> B4 historical shadow seam — GREEN

RED:

```text
91016ba67d2ceabdfdd0d6e31a127d6023e40c89
ClocktowerB4HistoricalShadowCoordinatorTest.kt only
+137
production changes = 0
docs changes = 0
CI #673 expected compile RED: coordinator missing
R2 #606 / ASP / Real Clingo GREEN
```

GREEN:

```text
c678b25cd2a750a02f0cb1a05632d31e58ffd048
ClocktowerB4HistoricalShadowCoordinator.kt only from RED
+75
RED test unchanged
CI #675 SUCCESS
R2 #608 SUCCESS
Android / ASP / Real Clingo SUCCESS
```

The new internal session-layer coordinator consumes a real `ClocktowerGameSession` current snapshot as GLOBAL history authority and forwards its `actionTimeline` + `epistemicObservationLog` to the exact B4 shadow. The focused test commits real persisted `Protect`, `Attack` and `RoleChange` through `ClocktowerGameSession` and proves the coordinator report equals the direct exact-shadow report.

It remains fail-closed unless setup/current snapshots share compatible game/seed/ruleset/seat identity, current revision is not older than setup, and current semantic history mode is `GLOBAL_V1`.

## 6. Authority boundary at the pause point

Current production-isolated chain:

```text
real ClocktowerGameSession
-> GLOBAL action + observation history
-> ClocktowerB4HistoricalShadowCoordinator
-> B4DynamicPlayerWorldSetShadow(validated ruleset)
-> EnumeratedHistoricalExactBaseline
-> B4 shadow cardinality report
```

Still deliberately **not connected**:

```text
ClocktowerRecommendationCoordinator
Host recommendation authority
A4/ZDD selector authority
history UI
other scripts
```

No persistence schema or `ClocktowerGameSession` state model was changed for this seam.

## 7. A3 next blocker — PAUSED, NOT STARTED

The next A3 design problem is **setup snapshot ownership**.

The exact replay requires an explicit immutable setup snapshot, while current app/session persistence stores the current snapshot and GLOBAL history rather than a second durable setup snapshot. Before any runtime authority wiring, decide tests-first:

```text
1. who owns the immutable setup snapshot during a live session;
2. whether it is persisted;
3. how restore reconstructs or retrieves it;
4. whether restored sessions temporarily fail closed for B4 shadow when setup provenance is unavailable;
5. only after that, where a real runtime shadow invocation belongs.
```

Do not casually add a second state copy to `ClocktowerGameSession` or persistence without resolving restore and schema semantics.

Do not start or combine with:

```text
Host authority promotion
A4/ZDD promotion
other scripts
history UI / misinformation expansion
App-root S7
```

## 8. Immediate priority — APP-ROOT DECOMPOSITION / POST-S7.3 ROUTE

PR #50 is merged/closed and S7.1–S7.4 have completed their defined local structural slices. The current route is:

```text
S7.1/S7.2 merged via PR #49
-> S7.3 COMPLETE: HostInteractionUi.kt
-> S7.4 COMPLETE: GameSettingsUi.kt
-> S7.5 ClocktowerNewDemonConfirmationScreen -> clocktower/ui/ClocktowerNightScreen.kt
-> S7.6 WerewolfRoleLine + WerewolfPlayerStatusRow -> werewolf/WerewolfHostScreen.kt
-> STOP / remeasure / fresh S8 audit
```

S7.5 remains subject to a fresh dependency/transaction guard. Do not jump directly into persistence, setup, or model extraction. `EmptyStateCard` remains a deferred generic app presentation primitive; do not create a `Utils.kt` or `CommonUi.kt` bucket merely because it is small.

Authoritative S7 handoff:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_S7.md
```

Known structural state at the S7.4 local checkpoint:

```text
CampBoardGameHostApp.kt
after S7.3: 220,403 bytes
after S7.4: 218,086 bytes
GameSettingsUi.kt: 3,112 bytes
```

The 50 KiB target remains a maintainability guideline rather than a hard gate. S7 must **not automatically reuse an old ranking**. Re-audit the live `CampBoardGameHostApp.kt` and choose the next highest-value, lowest-risk cohesive ownership slice. If no natural low-risk seam remains, stop rather than manufacture an artificial owner.

S7.4 was created from S7.3 checkpoint `b6d5d407d8d753bfe1fd4f9a5b16f58881468fa7`, whose parent is stable main `0311c3bb54ea71be69bc60a4aae642e0f39cd900`.

Protected S7 boundaries include:

```text
- Clocktower live transaction ordering
- Compose effect lifetime
- persistence / restore / archive ownership
- A4 lifecycle/cache/prewarm effects
- Clocktower planner/projector/materializer/session authority
- A3/B4 behavior
```

For S7:

```text
- audit first; do not immediately implement;
- identify exact new owner/declarations/dependencies/visibility changes;
- characterize Root state/effect/transaction ownership that must remain;
- define focused RED, production allowlist, validation commands and STOP conditions;
- preserve behavior exactly;
- exact-diff audit every implementation slice;
- do not mix A3/B4 code into decomposition commits;
- do not merge without explicit authorization.
```

## 9. Working discipline

1. Re-query live `main`, relevant branches/PRs and checks before editing.
2. Keep RED and GREEN separate where tests-first semantics apply.
3. Treat hidden Storyteller payload isolation as a protected invariant even while A3 is paused.
4. Keep decomposition and A3/B4 work on their own branches/PRs.
5. Do not merge, mark ready, rebase, force-push, or broaden scope without explicit user authorization.
6. **Do not update documentation for every small RED/GREEN slice or SHA/CI change.** Update docs only for meaningful phase closure, roadmap change, or cross-conversation handoff.
