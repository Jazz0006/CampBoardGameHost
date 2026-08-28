# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-28  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Latest accepted SNE-7.9D checkpoint: `06f06fb5c0689983c9baaf0bfe1765a8c69a8d16`  
> Current priority: **SNE-7.9E real restore + durable Dawn integration — START TESTS-FIRST**

## 1. Current campaign state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root decomposition through S9.1                CLOSED / MERGED
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
Same-night effective mechanical state / SNE-7      REOPENED / 7.9E ACTIVE
A3 setup-snapshot ownership / persistence          DEFERRED
Production recommendation authority promotion      NOT AUTHORIZED
```

PR #54 remains **draft and unmerged**. Do not resume unrelated App-root decomposition, A3 setup snapshot, A4/B4 authority promotion, recommendation tuning, generic custom-script Demon succession, or other Host/UI cleanup until SNE-7.9 is closed or explicitly paused.

## 2. Why SNE-7 remains open

The earlier SNE-7.1–7.8 closeout was reopened after final acceptance audit found real production integration defects beyond the typed seams then covered.

Corrective route:

```text
SNE-7.9A  Mayor redirect dependency invalidation          COMPLETE / GREEN
SNE-7.9B  Chambermaid stale-target revalidation           COMPLETE / GREEN
SNE-7.9C  canonical night-death resolution                COMPLETE / BROAD GREEN
SNE-7.9D  Demon succession legality / Dawn                COMPLETE / BROAD GREEN
SNE-7.9E  real restore + durable Dawn integration          ACTIVE / NEXT
```

The remaining original acceptance findings are now specifically:

1. real App restore restores raw `ClocktowerNightCheckpoint` fields but does not yet activate `NightTransactionReconstructor` as the derived same-night state boundary;
2. the existing lifecycle smoke deliberately stops before App-owned durable Dawn effects, so restore → reconstructed mechanics → canonical Dawn → durable ActionFact/observation/phase transition still lacks an end-to-end typed production seam.

## 3. Completed SNE-7.9 corrective slices

### 7.9A — Mayor redirect dependency invalidation

**COMPLETE / GREEN**

Accepted checkpoint:

```text
12d84cc9ed8076df9833d2fa268bc523283211b2
fix: invalidate stale Mayor redirect on upstream reconfirm
```

Changed reconfirmation of Poison / Monk / Demon attack invalidates dependent confirmed Mayor redirect and Demon successor while preserving editable drafts. Idempotent reconfirm preserves valid confirmations.

### 7.9B — Chambermaid stale-target revalidation

**COMPLETE / GREEN**

Accepted checkpoint:

```text
f9300f72b4ef63a521a87e9d2a087c7ae9db2f03
fix: revalidate Chambermaid selection authority
```

Canonical path:

```text
stored Chambermaid selection
+ current eligible set at Chambermaid cursor
→ revalidateTwoPlayerSelection
→ resolveChambermaidSelection
→ revalidated targets + wokeCount
```

### 7.9C — canonical night-death resolution

**COMPLETE / BROAD GREEN**

Accepted production death authority converges all three previously duplicated consumers:

```text
confirmed attack / poison / Monk facts
+ stable cards
→ resolveTroubleBrewingDawnDeathFacts
→ NightDawnResolutionPlanner.planValidatedNightDeath
→ DawnCommitIntent.death
```

Consumers now sharing that authority:

```text
Dawn durable materialization
public-alive observation preflight
Host death presentation / resolved mechanical event trigger
```

Key completion checkpoints:

```text
7139c8f9be8613ac082eafacf484f2c9c84a54f0  Dawn materialization authority
 e6c3d9ef7ab50e9e07d0ce570120bd63c68571af  public-alive preflight authority
 b6185ccf6b23583c112b040f831e65f2724f1035  Host death consumer authority
```

Earlier C2B/C2C broad checkpoints were GREEN, including Android full unit tests/build and Real Clingo. Detailed C1–C4 tests-first provenance remains in `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`, Git history, and the continuation handoff.

### 7.9D — Demon succession legality / Dawn

**COMPLETE / BROAD GREEN**

7.9D closed the two acceptance findings that production Dawn and reconstruction were not consuming one validated Imp succession authority.

Tests-first chain:

```text
8bf44fc78b56b9e6e18971720dbf32304d56724f  RED — reject illegal restored Demon successor
b3eaa92a39eefd0859e908d91b2214bec5811614  canonical Trouble Brewing Imp succession resolver
9f24652e8a1010092c86e1ab85e4271dee7034d9  evaluate succession at pre-self-kill state
ba7772218126a6116c9eef96a62338c5e82948d8  GREEN — reconstructor validates canonical Choice legality

d396a80d6b863486fdb3068ac8decf549bb26841  RED — planner Forced / None coverage
f982e50ca702fe95f623581e0e62347849bf888a  GREEN — planner consumes None / Forced / Choice

50fed86a4b0f33d2bb31eaff62a64af9ab4abdd1  production App self-kill path uses resolver + planner

943b65b7938930736aae3f307f4894b1859e3a58  RED — successful self-kill with no successor must still reconstruct Imp death
6c28d418702024cc614bc6cb04091238aa663c3a  expose canonical attack outcome + succession result
fe7c8761e7c9a7404b2b73964a2c1f1ae38f503e  GREEN — reconstruct death independently from successor existence

06f06fb5c0689983c9baaf0bfe1765a8c69a8d16  [full-ci] protective checkpoint
```

Accepted semantics:

```text
confirmed Imp self attack
→ canonical DemonNightAttackOutcome
→ if attack really self-kills: old Imp MechanicalDeath is reconstructed
→ canonical DemonSuccessionResolution
   None   → no RoleChanged, but successful old-Imp death remains
   Forced → forced legal successor
   Choice → only confirmed target in canonical legal target set
→ NightDawnResolutionPlanner controls continuation / pending new Demon / Dawn gating
```

Important negative contract:

```text
functioning Monk protects Imp self attack
→ attack outcome = no death
→ no reconstructed MechanicalDeath
→ no reconstructed RoleChanged
→ stale successor fields cannot invent succession
```

Validation evidence:

```text
CI #911  expected RED
  Android FAST selected
  913 tests / exactly 1 intended failure
  FULL step skipped
  R2 #838 SUCCESS

CI #913  GREEN
  Android FAST SUCCESS
  FULL step skipped
  CI gate SUCCESS
  R2 #840 SUCCESS

CI #914  [full-ci] ACCEPTANCE SUCCESS
  Android full unit tests + assembleDebug SUCCESS
  ASP contract tests SUCCESS
  Real Clingo cross-validation SUCCESS
  CI gate SUCCESS
  R2 #841 SUCCESS
```

The CI hardening is therefore also verified in production use:

```text
ordinary app micro-commit → Android FAST
[full-ci] logical checkpoint → Android FULL + assemble + ASP + Clingo
```

7.9D remains intentionally limited to Trouble Brewing Imp self-kill succession. Generic non-self Demon-death/custom-script succession stays deferred.

## 4. Active slice — SNE-7.9E real restore + durable Dawn integration

**ACTIVE / TESTS-FIRST**

Target integration path:

```text
persist / restore
→ ClocktowerNightCheckpoint
→ NightTransactionReconstructor
→ reconstructed same-night effective state
→ canonical death / succession resolution
→ DawnCommitIntent
→ App-owned durable materialization
→ ActionFact / public observation exactly once
→ phase transition
```

Current audited gap:

- `activeGameSnapshotJson()` already persists the unfinished-night checkpoint;
- `restoreSavedGame()` already restores checkpoint fields and validates semantic-history compatibility;
- real App restore currently copies those raw checkpoint fields back into App state;
- real App restore does **not yet call `NightTransactionReconstructor`**;
- do not fix this by mutating `cards.eliminatedRound` or public/base roles during restore;
- reconstructed mechanical death / current role must remain derived same-night state until canonical Dawn materialization.

### 7.9E first implementation objective

Start with the smallest directly callable typed production seam that can prove:

```text
restored unfinished Trouble Brewing night
+ durable checkpoint
+ stable canonical interaction plan
→ deterministic NightTransactionReconstruction
→ old Imp mechanically dead / legal successor currently Imp when appropriate
→ public/base GameState remains unchanged
```

Then extend the same seam to canonical Dawn planning and durable-commit intent. Do not jump directly to a large Compose callback rewrite before the typed boundary exists.

The existing lifecycle smoke is useful input coverage but is not sufficient acceptance evidence because it stops before App-owned durable Dawn consequences.

## 5. Protected architecture contracts

```text
public/persisted base state
+ confirmed same-night mechanical facts
+ stable canonical interaction plan
+ checkpoint.nightStepIndex
→ current effective cursor/state
→ actor eligibility / ability functioning / target legality / truth / triggers / current role
```

Hard contracts:

- mechanical death and public announcement remain distinct;
- never write `eliminatedRound` early to make later-night logic work;
- stable seat/interaction identity never comes from re-indexing filtered views;
- draft UI state is never mechanical authority;
- navigation alone does not invalidate confirmed facts;
- changed reconfirmation invalidates dependent confirmations while preserving editable drafts;
- same-night `RoleChanged` is projected before Dawn materialization;
- persistent effects follow source ability lifetime;
- rules determine legality, recommendation ranks legal choices, UI displays legal choices;
- pure semantics may support future cases, production wiring activates only validated slices;
- `ClocktowerNightCheckpoint` remains the sole durable unfinished-night state;
- do not create event sourcing by replaying transient `NightResolutionEvent` commands;
- source ownership guards protect structural responsibility, not formatting.

## 6. Testing / writer / CI policy

Preferred proof order:

```text
typed pure/domain behavior
→ typed reducer/planner/session behavior
→ typed adapter/integration behavior
→ minimal coarse source ownership guard only where App/Compose is not directly JVM-callable
```

Micro-cycle:

```text
RED
→ prove exact intended failure
→ minimal GREEN
→ focused --rerun-tasks when local runner is used
→ git diff --check / remote exact scope audit
→ push
→ continue without waiting obsolete old-head CI
```

CI cadence:

```text
ordinary PR app micro-commit
  → :app:testFast

semantic paths
  → selected semantic gates as classified

logical acceptance checkpoint
  → commit message contains [full-ci]
  → :app:testFull + :app:assembleDebug
  → ASP + Real Clingo

main / workflow dispatch / workflow or build config
  → full validation
```

`CampBoardGameHostApp.kt` remains a large file. GitHub connector has now successfully handled a ~228 KiB App edit, but large-file writes must use an exact-head, detached-candidate, exact-diff-audit path before branch fast-forward. Never replace it from truncated source content.

## 7. Current exact next-start instruction

```text
1. accepted 7.9D checkpoint = 06f06fb5c0689983c9baaf0bfe1765a8c69a8d16;
2. CI #914 + R2 #841 are the broad GREEN acceptance evidence;
3. begin SNE-7.9E tests-first from real restore composition;
4. first prove restored checkpoint → NightTransactionReconstructor derived state without early public mutation;
5. then extend through canonical Dawn intent and exactly-once durable ActionFact / observation / phase transition;
6. do not expand to generic Demon succession, A4/B4, recommendation, or unrelated Host/UI work;
7. keep PR #54 draft/unmerged.
```

## 8. Startup order

Read:

1. root `AGENTS.md`;
2. this roadmap;
3. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-27_SAME_NIGHT_CONTINUATION.md`;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`;
8. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
9. `docs/TESTING_STRATEGY.md`;
10. re-query live `main`, PR #54 head/state and checks before editing.

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
