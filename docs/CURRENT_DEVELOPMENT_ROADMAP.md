# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-25
> 文档角色：**CURRENT / 当前状态唯一权威**
> Repository: `Jazz0006/CampBoardGameHost`
> Stable `main`: `0311c3bb54ea71be69bc60a4aae642e0f39cd900`
> Current structural checkpoint: `78cbaab473f433fb98d69f1bebfd7a69ae11edaa`
> PR #48: **MERGED / CLOSED — historical multi-night exact baseline**
> PR #49: **MERGED / CLOSED — app-root S7.1/S7.2 structural checkpoint**
> PR #50: **MERGED / CLOSED — test execution tiers + path-aware CI**
> Current project priority: **APP-ROOT DECOMPOSITION — S9.1 APP JSON PRIMITIVES SEAM**
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
App-root S7.5                                       CLOSED / STACKED STRUCTURAL CHECKPOINT
App-root S7.6                                       CLOSED / STACKED STRUCTURAL CHECKPOINT
App-root S8.1                                       CLOSED / STACKED STRUCTURAL CHECKPOINT
App-root S8.2                                       CLOSED / STACKED STRUCTURAL CHECKPOINT
App-root S9.1                                       NEXT — APP JSON PRIMITIVES SEAM
PR #48 historical multi-night exact baseline       MERGED / CLOSED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN
Session -> B4 historical shadow seam               GREEN
Setup snapshot ownership / persistence             NOT STARTED
Production recommendation authority promotion      NOT STARTED / NOT AUTHORIZED
Test execution workflow S1–S3                      COMPLETE / MERGED via PR #50
```

## 2. Validation workflow

PR #50 established the authoritative local/CI validation strategy:

```text
T0 focused RED/GREEN
-> T1 :app:testFast
-> T2 affected regression / compile checks
-> triggered T3 only when semantic area requires
-> PR T4 applicable FULL gate
```

Stable `main` remains `0311c3bb54ea71be69bc60a4aae642e0f39cd900`.

For S7.3–S7.6 the remote audits prove lineage and exact structural diffs. No GitHub Actions workflow runs were observed on those stacked branch heads, so do **not** describe them as remotely CI-green. Local execution evidence is separate from remote structural evidence.

## 3. Protected architecture contracts

### Historical exact / A3-B4

- Durable observations are consumed exactly once by GLOBAL historical replay; setup-only knowledge constrains initial worlds only.
- Historical order and actor eligibility are separate concerns.
- Mechanically identical possible worlds converge even when hidden provenance differs.
- Exact historical construction remains Trouble Brewing-only and fails closed outside that boundary.
- `rolesBySeat` is immutable setup identity; `currentRolesBySeat` is dynamic historical current-role state.
- Storyteller-hidden targets and occurrence points never become player knowledge constraints.

Current production-isolated chain remains:

```text
real ClocktowerGameSession
-> GLOBAL action + observation history
-> ClocktowerB4HistoricalShadowCoordinator
-> B4DynamicPlayerWorldSetShadow(validated ruleset)
-> EnumeratedHistoricalExactBaseline
-> B4 shadow cardinality report
```

Still deliberately not connected to Host recommendation authority, A4/ZDD selector authority, history UI or other scripts.

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

`ClocktowerHostScreen.kt` remains under new-responsibility growth freeze. The 50 KiB target remains a maintainability guideline rather than a hard gate.

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
After S7.5 = 210,856 bytes
After S7.6 = 209,336 bytes
After S8.1 = 208,126 bytes
After S8.2 = 206,692 bytes
```

### S7.3 — shared host interaction UI — CLOSED

```text
head: b6d5d407d8d753bfe1fd4f9a5b16f58881468fa7
owner: HostInteractionUi.kt = 8,901 bytes
moved:
  HostProgressCard
  HostScriptCard
  HostInstructionBlock
  HostActionSection
  SelectablePlayerChips
  SelectableTwoPlayerChips
  SelectableSeatNumbers
remote exact-diff audit: PASS
GitHub Actions: none observed
```

### S7.4 — shared game-settings presentation — CLOSED

```text
RED:    4a46210cf11a0c45ceffef7bfaa647e53e99747f
repair: 843f61e5106b704417b1cd347c47393264215bb8
GREEN:  bcd8b9b2dad4f3cde42bef2cdb6d1eea7839dd7e
owner:  GameSettingsUi.kt = 3,112 bytes
moved:  GameSettingsHeader, StepperRow
remote exact-diff audit: PASS
GitHub Actions: none observed
```

`EmptyStateCard` remains Root-owned and deferred. Do not create `Utils.kt` / `CommonUi.kt` merely to move it.

### S7.5 — New Demon presentation — CLOSED

```text
BASE:   ea940199dd768aa6f35a418ab333c40401af658c
RED:    5ea3829631b33a13d4245b3932fc21662767a3a8
GREEN:  91ce3da44ec4b824c4e3e750ec9f6c5b99b618a3
owner:  clocktower/ui/ClocktowerNightScreen.kt
moved:  ClocktowerNewDemonConfirmationScreen
Root:   210,856 bytes
owner:  25,063 bytes
remote exact-diff audit: PASS
GitHub Actions: none observed
```

`ClocktowerHostScreen.kt` had the same blob SHA before and after S7.5, so new-Demon state, display routing and confirmation/transaction wiring were byte-for-byte unchanged.

### S7.6 — Werewolf presentation leftovers — CLOSED

```text
BASE:   91ce3da44ec4b824c4e3e750ec9f6c5b99b618a3
RED:    6e72afe4372a0ed7d329354a3bce018cb5799614
GREEN:  c2ab6dcba6e1d0fe862255160b782a8c6dab5fda
owner:  werewolf/WerewolfHostScreen.kt
moved:  WerewolfRoleLine, WerewolfPlayerStatusRow
Root:   209,336 bytes
owner:  31,193 bytes
remote exact-diff audit: PASS
GitHub Actions: none observed
```

S7 is now complete. The required STOP / remeasure / fresh S8 audit has been performed.

## 5. Fresh S8 audit and chosen first slice

The remaining Root front contains three qualitatively different areas:

```text
A. pure app-level value models
B. prefs / JSON / persistence / restore / archive code
C. legacy Clocktower role/setup/catalog/assignment code
```

### A — preferred S8.1: pure app game models

Create a same-package owner:

```text
AppGameModels.kt
```

Move exactly these already-`internal` pure declarations:

```text
GameKind
Role
PlayerCard
EliminationRecord
GameOutcome
SavedGamePreview
ArchivedGameReview
```

Keep these out of S8.1:

```text
private Screen
LanguageMode
PlayerCard.abilitySubject(...)
all Clocktower-specific enums/models
all prefs/JSON/persistence helpers
all Clocktower role/setup/catalog/assignment helpers
```

Rationale: this slice is declaration-only, cross-game, same-package, requires no visibility widening, and has no state/effect/session/schema behavior.

### B — persistence / JSON: DEFER

Do not include in S8.1:

- SharedPreferences keys and load/save functions;
- active-game save/load/clear;
- saved-game preview restore behavior;
- archive/history persistence;
- JSON nullable helpers/codecs;
- epistemic JSON serialization;
- saved-game schema/version logic.

These are restore/schema-sensitive and remain conditional S9 work unless an independently tested codec boundary is justified later.

### C — legacy Clocktower setup/catalog: AUDIT LATER

The Root still owns legacy role lists, script mappings, distribution and random assignment helpers. This area is more domain-cohesive but several declarations are currently `private`; extracting them would require visibility widening and careful comparison with the newer normalized `clocktower/catalog` architecture. Do not combine this with S8.1.

## 6. S8 closeout and S9.1 route

### S8.1 — pure app game models — CLOSED

```text
BASE:   a8e17af78cda13ee6f504f24142d02f3dcfdacb3
RED:    9b9d9c411a5d9c6c96cdf3315146338002123da7
repair: c58d8bda8dc04144d039f828918f1edafd8bc983
GREEN:  7b52eab31fb7c0e78156c5f3487cb062de03edfe
owner:  AppGameModels.kt = 1,245 bytes
root:   208,126 bytes
remote exact-diff audit: PASS
GitHub Actions: none observed
```

S8.1 moved exactly seven already-`internal` declarations with no production visibility widening. The next slice is a separate Clocktower-specific app value-model owner.

### S8.2 — Clocktower app value models — CLOSED

```text
docs base: 879148f9b150fd688b43446705ba8e3debb1af9a
RED: 798c83878825e6d7f9d7630449a673b6160c6447
planned stale repair: cf182bdfa2108bff00b02507fb5309bd4abf667f
T1 stale repair: 28afa7e7ebb8d69e09a628c96d15bbd41889bfae
GREEN: 78cbaab473f433fb98d69f1bebfd7a69ae11edaa
root: 206,692 bytes
owner: ClocktowerAppModels.kt = 1,469 bytes
remote exact-diff / structural audit: PASS
GitHub Actions: none observed
```

S8 pure declaration extraction is complete. S8.2 moved exactly the nine Clocktower app value models with no consumer changes and no production visibility widening.

### S9.1 — app JSON primitives seam — NEXT

Create `persistence/AppJsonPrimitives.kt` and move exactly nine pure JSON helpers from the Root. The only approved production visibility change is `private -> internal` for those nine helpers.

Persistence coordinator, SharedPreferences, full snapshot, restore transaction, archive, model codecs, role catalog, and session/state remain deferred.

S9.1 must be a mechanical declaration move only:

```text
CampBoardGameHostApp.kt
  -> persistence/AppJsonPrimitives.kt
```

Move exactly these nine pure JSON helpers, with the only approved production visibility changes being `private -> internal`:

```text
enumByName
JSONObject.putNullableString
JSONObject.putNullableInt
JSONObject.putNullableBoolean
JSONObject.optNullableString
JSONObject.optNullableInt
JSONObject.optNullableBoolean
stringsToJsonArray
JSONArray.toStringList
```

Keep persistence coordinator, SharedPreferences, full snapshot, restore transaction, archive, model codecs, role catalog, session/state, and A3/B4 semantics out of S9.1.

No callsite changes should be needed because the new owner remains in package `com.codex.campboardgamehost`.

The new owner must not contain Context/prefs, Compose state/effects, `ClocktowerGameSession`, persistence coordinator, model codecs, or epistemic JSON authority.

Validation remains:

```text
T0 focused RED/GREEN ownership tests
-> T1 :app:testFast
-> T2 :app:assembleDebug
-> no T3 expected for declaration-only model movement
-> applicable PR T4 later
```

## 7. Working discipline

1. Re-query live `main`, structural branch head and parent chain before each slice.
2. ChatGPT owns architecture/boundary judgment, test design, exact-diff criteria and remote audit.
3. Luna performs large-Root mechanical edits in the user's full local Git worktree.
4. Keep tests-first RED and GREEN provenance explicit.
5. Do not preserve obsolete temporary Root-location assertions; preserve the actual architectural invariant.
6. Do not merge, mark ready, rebase, force-push or broaden scope without explicit user authorization.
7. Do not update `docs/TESTING_STRATEGY.md` for structural slice bookkeeping.
