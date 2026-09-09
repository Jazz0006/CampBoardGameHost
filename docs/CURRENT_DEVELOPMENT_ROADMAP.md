# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## Live context

```text
main: d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e
D6.2 branch: codex/d6-2-ui-composition
Draft PR: #115 — OPEN / DRAFT / DO NOT AUTO-MERGE
Latest validated production checkpoint:
ad16ccc694e687ab10e62677cf0369fa26e9c2fd
refactor(clocktower): extract numeric option preparation
```

Closeout commits after `ad16ccc6...` are documentation-only unless a later handoff explicitly records a newer validated production checkpoint. Always re-query live branch head and keep **production checkpoint** distinct from **docs-only branch head**.

Historical D6.2g clean-head gates:

```text
R2 34289616209 — PASS
CI 34289616204 — PASS
  Android FAST — PASS
  Android FULL — correctly skipped
  ASP — correctly skipped
  Real Clingo — correctly skipped
  CI gate — PASS
```

The immediately preceding D6.2f production checkpoint passed FULL T4:

```text
cee19c1ab85b4b4400a4d38f958a9014dd10a5e3
R2 34288731422 — PASS
CI 34288731376 — PASS
  Android FULL + debug APK — PASS
  ASP — PASS
  Real Clingo — PASS
  CI gate — PASS
```

## Current priority

> **PS5 COMPLETE → D6.1 COMPLETE / MERGED → D6.2a–g COMPLETE / VALIDATED → D6.2h–n AUDITS / R0 CLEANUP COMPLETE → D6.2o NUMERIC OPTION PREPARATION COMPLETE / VALIDATED → NUMERIC ROLE-STEP INPUT AUDIT NEXT.**

D6.2 stays on `codex/d6-2-ui-composition`. Do not reopen PR #113 or move this work back to `codex/d6-root-reaudit`.

## Architecture direction

The surviving Storyteller UI is now square-table/table based. The old HostScriptCard-style fallback was proven unreachable and deleted.

D6.2 should therefore:

- optimize only live square-table/table ownership seams;
- distinguish transient UI selection from durable/Recovery authority;
- move ownership only when a real cohesive boundary exists;
- delete dead state rather than wrapping it;
- avoid broad `ClocktowerJudgeState`, `DayState`, `Actions`, Controller or ViewModel bags;
- preserve `ClocktowerGameSession` as canonical writable session/domain owner.

File size is a useful signal, not the goal.

## D6.1 — COMPLETE / MERGED

D6.1 established:

```text
ClocktowerGameSession
= canonical writable identity
+ revisions
+ semantic chronology
+ dynamic GameState mechanics

ClocktowerSessionView
= narrow immutable Compose-facing projection

App-root PlayerCard / flow variables
= presentation and orchestration mirrors
```

Merged PR #113:

```text
112572cbd3d990737a412cc4b8ead766d00867e8
```

## D6.2 baseline

Post-D6.1 residual audit measured:

```text
ClocktowerHostScreen.kt  329,172 bytes / 5,474 lines
CampBoardGameHostApp.kt  241,986 bytes / 4,315 lines
ClocktowerJudgeScreen    103 parameters / 39 callbacks
MutableState params       10
App-root clocktower vars  41
```

The dominant residual debt was UI-composition fan-out rather than canonical domain ownership.

## D6.2a — COMPLETE: consumption characterization

Authority:

- `docs/D6_2A_CLOCKTOWER_JUDGE_CONSUMPTION_MATRIX_2026-09-08.md`

Key result: parameters do not form one natural mega-state. Ownership must move by cohesive feature/phase boundary.

## D6.2b — COMPLETE / VALIDATED: Slayer transient ownership

Checkpoint:

```text
58bc1e51440d44f36e14d1a9d5a45cfe9c235955
```

Result:

```text
Slayer claimant/target -> Judge-local remember(gameId)
Durable onSlayerShot(...) boundary unchanged
Judge params 103 -> 101
App-root vars 41 -> 39
```

Validation:

```text
R2 34283098478 — PASS
CI 34283098477 — PASS / FULL
```

## D6.2c/d — COMPLETE / VALIDATED: Artist transient ownership

Audit:

- `docs/D6_2C_ARTIST_CONFIRMATION_CONTRACT_AUDIT_2026-09-08.md`

Validated production checkpoint:

```text
6a5723af0e9fb646d0c66e900a1c4d215d6a2fef
```

Result:

```text
Artist claimant/truthful/shown -> Judge-local transient state
Durable callback -> onConfirmArtistQuestion(String, Boolean, Boolean)
Judge params 101 -> 95
callbacks 39 -> 36
App-root vars 39 -> 36
```

Validation:

```text
R2 34286858464 — PASS
CI 34286858453 — PASS / FULL
```

## D6.2e — COMPLETE: legacy Storyteller reachability proof

Authority:

- `docs/D6_2E_LEGACY_STORYTELLER_REACHABILITY_AUDIT_2026-09-09.md`

Exhaustive control-flow audit proved the trailing HostScriptCard/HostProgressCard Storyteller UI was unreachable because every legal phase/dayMode/nightStarted state already returned through the modern/table path.

## D6.2f — COMPLETE / VALIDATED: retire unreachable legacy tail

Authority:

- `docs/D6_2F_LEGACY_STORYTELLER_RETIREMENT_PROGRESS_2026-09-09.md`

Validated checkpoint:

```text
cee19c1ab85b4b4400a4d38f958a9014dd10a5e3
```

Net result:

```text
exactly 2 production files
0 additions / 685 deletions
ClocktowerHostScreen.kt
  330,257 -> 283,849 bytes
  5,491 -> 4,807 lines
```

Also removed writerless `ClocktowerDayMode.ExecutionResult`.

This was a major architecture cleanup: the legacy Storyteller composition path is gone rather than decomposed.

## D6.2g — COMPLETE / VALIDATED: dead plumbing exposed by retirement

Authority:

- `docs/D6_2G_RETIRED_STORYTELLER_PLUMBING_PROGRESS_2026-09-09.md`

Validated checkpoint:

```text
15342f9e22ac204602680e6ef831fb4e95c7b0bf
```

Removed zero-consumer Judge/App plumbing:

```text
records forwarding
onPhaseChange
onShowResults
phaseTitle
phaseProgress
phaseScript
phaseAction
recordCurrentVote()
```

Net diff:

```text
CampBoardGameHostApp.kt +0 / -26
ClocktowerHostScreen.kt +0 / -42
0 additions / 68 deletions
```

Post-D6.2g metrics:

```text
ClocktowerJudgeScreen
  parameters:              92   (103 -> 92)
  callbacks:               34   (39 -> 34)
  providers:                3
  MutableState params:      8   (10 -> 8)

App-root clocktower vars:   36   (41 -> 36)
```

## D6.2h — COMPLETE: surviving square-table ownership re-audit

Authority:

- `docs/D6_2H_SURVIVING_SQUARE_TABLE_OWNERSHIP_AUDIT_2026-09-09.md`

Key findings:

### Safe transient/dead sub-boundary

```text
nominatorNameState   -> transient square-table nomination selection
nomineeNameState     -> transient square-table nomination selection
currentVoteCountState -> dead; modern typed vote state owns pending vote count
```

`ClocktowerVoteTableScreen` already owns pending voter selection/count using typed `ClocktowerTableVoteState` and submits it as one value to the durable vote transaction.

### Must remain external for now

```text
dayModeState
  -> App/Recovery/Klutz/Artist routing still writes it

ghostVoteAuthority
highestVoteNameState
highestVoteCountState
  -> durable vote mechanics + Recovery serialization/restore
```

### Lifecycle proof

The nomination pair can be safely Judge-local with:

```kotlin
remember(gameId, round)
```

because Day completion advances `round` before entering Night, while Virgin and Klutz special paths preserve equivalent reset semantics. No Recovery contract requires restoring an in-progress nomination pair.

## D6.2i — COMPLETE / VALIDATED: Day nomination transient ownership

Production checkpoint: `5e0891e7611787300b01d83b889f27a903c0768b`.

- App no longer declares, resets or forwards the nomination pair or dead outer vote count.
- Judge owns `nominatorName` and `nomineeName` with `remember(gameId, round)`.
- Cancel nomination and confirm vote still clear both names; cancel vote retains the pair.
- Day mode, ghost-vote authority, highest-vote mechanics, Virgin callbacks and Recovery remain external and unchanged.

Verified boundary metrics:

```text
Judge parameters:                  92 -> 89
on... callbacks:                   34 unchanged
function-valued providers:          3 unchanged
MutableState parameters:            8 -> 5
App explicit remembered state vals: 9 -> 6
App delegated remembered vars:     38 unchanged
Combined scalar state declarations:47 -> 44
```

**Metric correction:** the old expected `App-root vars 36 -> 33` had no reproducible counting definition. The counts above include root `clocktower*` declarations directly initialized with `remember { mutableStateOf(...) }`, both delegated `var` and explicit `val`; they exclude derived views and state lists. Earlier campaign figures are historical, not the current measured total.

Exact production diff: App +0/-12; Host +2/-11; exactly two files and one commit from `bffa52095585bd9ee37e77fce31c2a71219ce895`.

Acceptance:

```text
CI 34291666237 — PASS / FULL
  Android full JVM + debug APK — PASS
  ASP contracts — PASS
  Real Clingo — PASS
  CI gate — PASS
R2 34291666241 — PASS
```

Local Gradle could not download its distribution because of restricted network access. Compilation and the existing nomination/vote tests were validated by the remote full JVM suite, not by a claimed local GREEN. No new tests or production seams were introduced.

Detailed evidence: `docs/D6_2I_DAY_NOMINATION_OWNERSHIP_PROGRESS_2026-09-09.md`.

## D6.2j — residual composition audit COMPLETE

Authority: `docs/D6_2J_RESIDUAL_COMPOSITION_AUDIT_2026-09-09.md`.

The D6.2j audit confirmed legitimate external ownership for all five remaining Judge MutableState inputs. At that checkpoint, four diagnostic effects had permanently zero trigger counters; Judge also had dead `confirmedDemonSuccessorTarget` forwarding, diagnostics-only `rulesetRef`, an unused child diagnostics parameter and eight unused pure role lookups.

## D6.2k — dormant diagnostic / dead input cleanup COMPLETE

Production checkpoint: `c3a25f640e7e6c9ef2537d7d9387eb27b28b1ece`.

Implemented the D6.2j three-file contract: removed four untriggerable diagnostic effects, 13 diagnostic remembered states, two Judge inputs, one child input, eight unused pure role lookups and directly orphaned imports. Exact production diff: +0 / -257. Judge parameters 89 -> 87; NightStep 49 -> 48. Judge callbacks 34, providers three, MutableState inputs five and App scalar state 44 are unchanged.

CI 34293746781 PASS (Android compile + FAST and CI gate); R2 34293746779 PASS. FULL was not selected for this bounded dead-code deletion; the D6.2i full gate remains historical evidence. No local Android GREEN or real-device verification is claimed.

Detailed evidence: `docs/D6_2K_DORMANT_DIAGNOSTIC_CLEANUP_PROGRESS_2026-09-09.md`.

## D6.2l — retired Day/History UI and obsolete R2 assertions COMPLETE

Production/workflow checkpoint: `69655de1d992ec6ec7cf45b2639a048ae4fb32e4`.

Removed the six proven unused Day/History declarations and their obsolete R2 assertions. Exact diff: two Kotlin files +0 / -612, workflow +2 / -8. Day now 616 lines / 31846 bytes; History 530 lines / 29188 bytes. All six retained function blocks are byte-identical to the parent; current square-table rendering, App/Host and persistence are untouched.

FULL CI 34294224391 PASS: full Android JVM tests + debug APK, ASP, Real Clingo and CI gate. R2 34294224399 PASS; the exact R2 shell body also passed locally. No local Android GREEN or real-device verification is claimed.

Evidence: `docs/D6_2L_RETIRED_DAY_HISTORY_UI_PROGRESS_2026-09-09.md`.

## D6.2m — private App decoder island cleanup COMPLETE

Production checkpoint: `4c16f4c09ae7df693a3b16d6910ab026373e05cd`.

Removed the isolated private JSON decoder call subgraph, unused random `clocktowerRolesFor(playerCount)` and two directly orphaned imports. Exact diff: App +0 / -94, now 4142 lines / 233013 bytes. Current GameArchiveJsonCodec/AppGameStateJsonCodec, Recovery v2 and `generateClocktowerAssignments` paths remain intact.

CI 34294858685 PASS: Android compile + executed FAST and CI gate. R2 34294858697 PASS. FULL was correctly not selected; D6.2l immediately before it passed full JVM tests and debug APK, ASP and Real Clingo. No local Android GREEN or real-device verification is claimed.

Evidence: `docs/D6_2M_APP_DECODER_CLEANUP_PROGRESS_2026-09-09.md`.

## D6.2n — live information preparation boundary re-audit COMPLETE

Authority: `docs/D6_2N_LIVE_INFORMATION_PREPARATION_OWNERSHIP_AUDIT_2026-09-09.md`.

The six numeric recommendation call sites share a pure history/parser and recommendation-result projection seam. The recommendation invocation remains in Host; typed registration facts, role-step assembly, structured decision lifecycle, telemetry and publication remain with their current owners. Moving Chef/Empath/Chambermaid closures first would require a broad captured context, so materializer movement is deferred until this dependency is removed.

## D6.2o — numeric option preparation COMPLETE

Production/test checkpoint: `ad16ccc694e687ab10e62677cf0369fa26e9c2fd`.

Extracted the pure prior-number parser and recommendation-to-display projector into a 55-line typed adapter. Host retains recommendation context construction and invocation; all six numeric role consumers and materializer order are unchanged. Host is 4520 lines / 267039 bytes, down 25 lines in this slice. Typed adapter tests cover localized history and full display metadata.

CI 34296404234 PASS: production/test Kotlin compile, executed FAST and CI gate. R2 34296404229 PASS. Evidence: `docs/D6_2O_NUMERIC_OPTION_PREPARATION_PROGRESS_2026-09-09.md`.

## Next — numeric role-step input audit

Re-audit Clockmaker, Chef, Empath and Chambermaid inputs after D6.2o before moving materializer closures. Define a cohesive immutable input only if registration-aware Chef/Empath and pair-aware Chambermaid differences remain explicit. Do not pass cards, registration maps, coordinator/session or arbitrary callbacks through a shared context.

PR #115 remains OPEN / DRAFT. Preserve benchmark harnesses/tests, live A4 prewarming, recommendation effects, telemetry and Recovery.

## Remaining work — global audit and outcome targets

Authority: `docs/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md`.

The user requested a whole residual audit after D6.2j. D6.2k–m are implemented and validated. Dormant diagnostics, retired Day/History UI plus obsolete R2 assertions, and the private App decoder island are gone. R0 dead-code cleanup is complete.

Then prioritize information preparation/role materializers and NightStep/Day interaction boundaries. App transaction application and Recovery/A4 composition are a later, higher-risk wave, using existing session/planner authority. Do not mechanically move all callbacks or conditionally remount live effects.

First-wave planning targets: Host roughly 2200–3000 lines, NightStep roughly 350–600 lines, and common feature changes understood through 2–4 cohesive owners/tests. These are estimates to remeasure, not permission to introduce parameter bags or alter behavior. App may remain large until its optional transaction wave. The global audit defines scope, effort bands, invariants, testing and stop criteria; it does not authorize a broad production rewrite.

## Execution method

The user explicitly authorized direct local edits in Work. D6.2i used a complete local Git checkout and exact diff auditing. CLI push lacked credentials; the connected GitHub API uploaded the local bytes, verified both blob SHAs and the complete tree SHA, and fast-forwarded the feature branch. No bootstrap workflow or force-push was needed.

## Frozen invariants

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- exact game/player revision cadence and ordering;
- one monotonic collision-free semantic chronology;
- action/observation idempotency and non-mutating preflight;
- no storyteller-hidden target leak;
- Recovery v2 current-version-only policy;
- SideEffect / ON_PAUSE / ON_STOP + `RecoveryWriteGate` topology;
- A4 durability/invalidation/prewarm ordering;
- no Compose dependency in session/domain;
- gameplay/recommendation semantics unless separately authorized;
- Undercover/Werewolf isolation.

## Active handoff

Read this roadmap, `AGENTS.md`, `docs/TESTING_STRATEGY.md`, `docs/D6_2_UI_COMPOSITION_AUDIT_2026-09-08.md` and `docs/D6_2I_DAY_NOMINATION_OWNERSHIP_PROGRESS_2026-09-09.md` and `docs/D6_2J_RESIDUAL_COMPOSITION_AUDIT_2026-09-09.md` plus `docs/D6_REMAINING_DECOMPOSITION_GLOBAL_AUDIT_2026-09-09.md` for the overall route; read `docs/D6_2M_APP_DECODER_CLEANUP_PROGRESS_2026-09-09.md` for the latest validated production checkpoint and next boundary audit. The D6.2i handoff and D6.2j implementation contract are now historical; do not implement completed slices again.

## Later priority after D6

```text
D6 ownership/composition decomposition
-> square-table Storyteller UI consolidation / UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
