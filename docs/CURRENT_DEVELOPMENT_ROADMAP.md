# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-27  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Current priority: **SNE-7 closeout — finish legacy source-test cleanup, then continue SNE-7.4 production typed-seam migration**

## 1. Current campaign state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root decomposition through S9.1                CLOSED / MERGED
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
Same-night effective mechanical state              CURRENT / SNE-7 CLOSEOUT
A3 setup-snapshot ownership / persistence          DEFERRED
Production recommendation authority promotion      NOT AUTHORIZED
```

Do not resume App-root decomposition, A3 setup-snapshot work, A4/B4 authority promotion, recommendation tuning, or generic custom-script Demon-death succession until the current same-night campaign is closed or explicitly paused.

## 2. Accepted same-night foundation

Accepted production before SNE-7 includes:

```text
SNE-1..6A      effective mechanical death / consumer foundations
SNE-6B1       current-role projection foundation
SNE-6B2.1     pure Demon succession semantics
SNE-6B2.2     legality separated from recommendations
SNE-6B2.3     confirmed successor transaction/checkpoint
SNE-6B2.4     confirmed successor RoleChanged production projection
SNE-6B2.5A    Poisoner source lifetime follows current role
SNE-6B2.5B    roleActor resolves from effective current role
SNE-6B2.5C    Fortune Teller detects current/new Demon role
SNE-6B2.5D    Spy/Recluse registration follows current role without alive gating
SNE-6B2.6     exact confirmed successor materializes at Dawn; no draft/fallback
Mayor closeout shared legality + Host/UI wiring COMPLETE
```

Important accepted checkpoints remain:

```text
5a94c63536c04382f59963843c2ac10544962b02
  SNE-6B2.5 A–D

51179ecca667d5450550375735ca49aae932c06d
  SNE-6B2.6 exact Dawn materialization

2e8cb6a6a4763f9926956e5407d1c465e112e2bd
  Mayor Demon-exclusion Host/UI production wiring
```

The attempted generic non-self Demon-death 6C direction remains intentionally deferred. Do not resurrect it in SNE-7.

## 3. Current SNE-7 live status

The earlier roadmap/handoff said SNE-7.1 had not started. That is stale. Live code has progressed to:

```text
SNE-7.1  behavior-first night transaction matrix
         ESTABLISHED

SNE-7.2  NightCheckpointReducer
         IMPLEMENTED / typed pure seam

SNE-7.3  NightDawnResolutionPlanner + DawnCommitIntent
         IMPLEMENTED / typed pure seam

SNE-7.4  switch production Compose/App wiring to typed seams
         PARTIAL / CURRENT FUNCTIONAL FRONTIER

SNE-7.5  restore / process-death reconstruction matrix
         SCAFFOLD EXISTS / INCOMPLETE

SNE-7.6  limited Compose smoke/integration coverage
         NOT COMPLETE

SNE-7.7  source-string retirement
         IN PROGRESS

SNE-7.8  minimal architecture guards only
         NOT COMPLETE
```

Before this documentation refresh, live PR #54 head was:

```text
2aa528dbb898313c51b1a7fb06d11a60c883b84f
  test: remove low-value same-night wiring assertions
```

Validation at that head:

```text
R2 #730  SUCCESS
CI  #803  FAILURE
```

CI #803 ran 879 tests and failed 4 source-inspection assertions:

- `ClocktowerSameNightEffectiveStateProductionWiringTest`
  - later normal actor eligibility must consume effective same-night state
- `ClocktowerProductionOtherNightWiringTest`
  - three implementation-shape assertions

This is the immediate baseline-cleanup gate. Do not change correct production code merely to recover obsolete source strings. Apply the source-test policy in `AGENTS.md` and `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`.

## 4. Protected same-night architecture

```text
public/persisted base state
+ confirmed same-night mechanical facts
+ stable canonical interaction plan
+ checkpoint.nightStepIndex
→ derived current interaction / cursor
→ ClocktowerEffectiveNightState
→ actor eligibility
→ ability functioning
→ persistent-effect lifetime
→ target legality
→ information truth
→ triggers
→ current role
```

Hard contracts:

- mechanical death and public death announcement remain distinct;
- never write `eliminatedRound` early merely to make later-night logic work;
- stable seat/interaction identity never comes from re-indexing filtered views;
- draft UI state is never mechanical authority;
- same-night `RoleChanged` is projected before Dawn materialization;
- persistent effects follow source ability lifetime;
- death-trigger/even-if-dead exceptions are explicit;
- one canonical interaction plan prevents a newly-created Demon from receiving a second normal Demon action;
- outcome is not evaluated mid-transaction while mandatory succession remains unresolved;
- recommendation ranking remains downstream of rules legality;
- `ClocktowerNightCheckpoint` is the sole durable unfinished-night checkpoint state;
- `nightStepIndex` is the sole stored navigation position;
- navigation alone does not invalidate confirmed mechanics;
- draft editing alone does not invalidate confirmed mechanics;
- changed reconfirmation is the dependent-invalidation boundary;
- `NightResolutionEvent` is transient command input, not a durable event log;
- `NightCheckpointReducer` owns checkpoint-local transitions only;
- `NightDawnResolutionPlanner` owns pure validated consequences/intent only;
- `ClocktowerGameSession` / App boundary retains sequence, timeline and durable commit authority.

## 5. Immediate execution sequence

### Gate 0 — restore a trustworthy green baseline

Finish the source-string cleanup exposed by CI #803.

Rules:

```text
for each failing source assertion:
  identify the typed/behavioral contract that supersedes it
  if coverage exists → retire/narrow the obsolete assertion
  if coverage does not exist → add the smallest typed behavior test first
  do not reshape production around implementation-shaped strings
```

After the cleanup, obtain a focused/full-enough validation result that confirms the four failures are gone before treating the baseline as clean.

### SNE-7.4A — Poison production reducer wiring

This is the next functional slice.

Current production still duplicates Poison checkpoint semantics in Compose/App callbacks. Cut those callbacks over to `NightCheckpointReducer`:

```text
Poison draft edit
  → NightResolutionEvent.EditPoisonDraft
  → NightCheckpointReducer.reduce

Poison confirm
  → NightResolutionEvent.ConfirmPoison
  → NightCheckpointReducer.reduce
```

Acceptance:

- typed RED first at the smallest real callable application seam;
- draft edit does not change confirmed poison;
- draft edit does not invalidate confirmed successor;
- unchanged reconfirm preserves dependent successor;
- changed reconfirm commits the draft and invalidates dependent successor;
- durable event/timeline/history side effects remain exactly-once and outside reducer ownership;
- no second persisted night-state model;
- focused T0 GREEN + `git diff --check`.

Expected follow-on micro-slices:

```text
SNE-7.4B  Monk
SNE-7.4C  Demon attack
SNE-7.4D  Mayor redirect
SNE-7.4E  Demon successor
SNE-7.4F  Dawn planner authority closeout
```

### Then continue

```text
SNE-7.5  finish real restore/process-death reconstruction matrix
SNE-7.6  2–4 Compose smoke/integration tests
SNE-7.7  finish source-string retirement
SNE-7.8  retain only minimal architecture guards

→ logical checkpoint :app:testFast + triggered T2/T3
→ latest production-head GitHub CI/R2
→ exact campaign audit
→ PR remains draft until explicit user authorization
```

## 6. Restore/reconstruction boundary

`NightTransactionReconstructor` exists but is not a completed authority. Do not treat a scaffold or ignored contract as GREEN.

Required reconstruction model:

```text
GameState
+ decoded ClocktowerNightCheckpoint
+ reconstructed ruleset/canonical plan
→ derived effective night state
```

Required cases include:

- legacy draft-only successor does not invent confirmation/`RoleChanged`;
- confirmed successor + Previous remains authoritative;
- draft edit without Confirm leaves prior confirmation authoritative;
- invalid confirmed successor fails closed;
- missing interaction and out-of-range navigation restore safely;
- stale Mayor redirect to Demon fails closed;
- current effective role may differ from public/base role;
- identical durable inputs reconstruct identical effective state.

Do not reconstruct by replaying transient UI commands.

## 7. Mayor product restriction remains unchanged

For current Trouble Brewing automatic-host production:

```text
Mayor redirect target MUST NOT be the current Demon.
```

This is an intentional product/house-rule restriction, not an official universal BotC rule. Generic non-self Demon death + Scarlet Woman succession remains deferred for future dynamic/custom-script support.

## 8. Testing and source-inspection policy

Preferred proof order:

```text
typed pure/domain behavior
→ typed reducer/planner/session behavior
→ typed adapter/integration behavior
→ minimal architecture source guard only where runtime proof is impractical
```

Do not preserve exact local variables, formatting, whitespace, or inline expression order merely to keep a source-string test GREEN.

For source assertions retained long-term, protect ownership boundaries rather than implementation spelling.

## 9. Development workflow authority

Current execution authority:

```text
AGENTS.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
docs/TESTING_STRATEGY.md
docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md
docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md
```

Cadence:

```text
micro-slice → exact T0 RED/GREEN
related slices → remote diff audit, do not wait for old-head CI
logical checkpoint → T1 + triggered T2/T3
latest checkpoint head → GitHub CI/R2
merge → full required gate + explicit user authorization
```

## 10. Deferred work after same-night correctness

After SNE-7 closes, re-audit rather than automatically resuming old work. Known candidates:

1. App-root S9.2 Active Game Persistence Boundary;
2. A3 immutable setup-snapshot ownership/persistence;
3. broader dynamic/custom-script generic Demon-death succession;
4. production recommendation-authority promotion only if explicitly authorized.

## 11. Startup order for the next conversation

Read in this order:

1. root `AGENTS.md`;
2. this roadmap;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-27_SAME_NIGHT_CONTINUATION.md`;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
8. `docs/TESTING_STRATEGY.md`;
9. re-query live `main`, PR #54 head/state and latest checks before editing.

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
