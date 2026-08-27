# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-28  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Latest C2C code checkpoint: `7139c8f9be8613ac082eafacf484f2c9c84a54f0`  
> Current priority: **SNE-7 REOPENED — SNE-7.9C canonical night-death resolution ACTIVE; C2C Dawn production cut-over COMPLETE / focused GREEN; remaining Host + observation consumers NEXT**

## 1. Current campaign state

```text
Phase A correctness foundation                     CLOSED
R5.5 Script & Dynamic Flow Foundation              CLOSED / MERGED
R6 semantic/history prerequisites                  CLOSED / MERGED
A3 Architecture Hardening H1–H7                    COMPLETE / GREEN
B4 historical-exact shadow bridge                  GREEN / production-isolated
App-root decomposition through S9.1                CLOSED / MERGED
App-root S9.2 Active Game Persistence Boundary     AUDIT COMPLETE / DEFERRED
Same-night effective mechanical state / SNE-7      REOPENED / 7.9 CORRECTIVE SLICES ACTIVE
A3 setup-snapshot ownership / persistence          DEFERRED
Production recommendation authority promotion      NOT AUTHORIZED
```

PR #54 remains draft and unmerged. Do not resume unrelated App-root decomposition, A3 setup snapshot, A4/B4 authority promotion, recommendation tuning, or generic custom-script Demon-death succession until SNE-7.9 is closed or explicitly paused.

## 2. Why SNE-7 was reopened

The earlier `SNE-7.1–7.8 IMPLEMENTATION COMPLETE / FINAL VALIDATION` state was withdrawn after final acceptance audit exposed real production correctness and integration defects that the existing typed seams did not cover.

Blocking findings were:

1. Chambermaid stored targets could become stale after upstream same-night changes.
2. Mayor redirect confirmation did not invalidate when confirmed Poison / Monk / Demon attack facts changed; stale redirect could bypass Monk protection and kill the wrong player.
3. Host, observation preflight, and Dawn did not consume one canonical night-death authority.
4. Production Demon succession still has an unvalidated Dawn path.
5. `NightTransactionReconstructor` successor legality is insufficient for production restore activation.
6. Real restore does not yet consume the reconstructor.
7. Existing integration smoke stops before App-owned durable Dawn effects.

Authoritative campaign status:

> **SNE-7 REOPENED — blocking correctness findings discovered during final acceptance audit.**

## 3. SNE-7.9 corrective route

```text
SNE-7.9A  Mayor redirect dependency invalidation
SNE-7.9B  Chambermaid stale-target revalidation
SNE-7.9C  canonical night-death resolution
SNE-7.9D  Demon succession legality / Dawn
SNE-7.9E  real restore + durable Dawn integration
```

The order remains intentional. Do not begin D/E while C still has duplicate production death authorities.

## 4. Current SNE-7.9 status

### SNE-7.9A — Mayor redirect dependency invalidation

**COMPLETE / GREEN**

Accepted production checkpoint:

```text
12d84cc9ed8076df9833d2fa268bc523283211b2
fix: invalidate stale Mayor redirect on upstream reconfirm
```

Contracts:

```text
ConfirmPoison confirmed value changed
ConfirmMonkProtection confirmed value changed
ConfirmDemonAttack confirmed value changed
  → confirmedMayorRedirectTarget = null
  → confirmedDemonSuccessorTarget = null
  → editable drafts preserved

idempotent reconfirm
  → dependent confirmations preserved
```

Dependency direction stays one-way; Mayor confirmation itself does not invalidate successor confirmation without a separately proven dependency.

### SNE-7.9B — Chambermaid stale-target revalidation

**COMPLETE / GREEN**

Accepted production checkpoint:

```text
f9300f72b4ef63a521a87e9d2a087c7ae9db2f03
fix: revalidate Chambermaid selection authority
```

Canonical path:

```text
stored Chambermaid first/second
+ current eligible names at Chambermaid cursor
→ revalidateTwoPlayerSelection
→ resolveChambermaidSelection
→ revalidated targets + wokeCount
```

Host result, player-visible display, recorded targets, and current UI selection consume the revalidated result.

### SNE-7.9C — canonical night-death resolution

**IN PROGRESS**

#### C1 — planner attack-outcome authority

```text
58fe6cd4e927128c3cb208dab9d12c8423ca5188
refactor: route Dawn death through attack outcome authority
```

`NightDawnResolutionPlanner` can consume canonical `DemonNightAttackOutcome`, including canonical `NO_DEATH` and Mayor redirect applicability.

#### C2A — Trouble Brewing direct Demon attack adapter

```text
dee41713e25b2387b77419a74ea256082fe2a44a  RED
e0b25a8d822bd348e33b6e7a9378be89bd564da9  GREEN
```

Established:

```text
resolveTroubleBrewingDemonNightAttackOutcome(
    cards,
    targetName,
    poisonedPlayerName,
    monkProtectedTargetName,
)
```

This seam owns only the direct Trouble Brewing Imp attack outcome. Mayor redirect and Demon succession remain separate rule-owned choice boundaries.

#### C2B — canonical Dawn death facts

```text
b2830c5846c14320e37371d706f678db7b10e996  RED
7fa38d47d682cfb324052d8b56c46563ffc0b815  GREEN
```

Established:

```text
TroubleBrewingDawnDeathFacts
  attackOutcome
  originalDeathSeat
  mayorSeat
  demonSafeSeats
```

`demonSafeSeats` reuses the same canonical attack adapter for Soldier / functioning-Monk / poison safety. Stable seat identity always comes from original `cards` ordering, never a filtered collection.

Broad validation at C2B checkpoint `7fa38d47...`:

```text
CI #882 SUCCESS
R2 #809 SUCCESS
Android full unit tests/build SUCCESS
Real Clingo cross-validation SUCCESS
```

#### C2C — Dawn production consumer cut-over

**IMPLEMENTATION COMPLETE / FOCUSED GREEN**

Checkpoint chain:

```text
b3c52db2ee5e89b390de366c00034f367e9ec033
  RED 1 — real Dawn must consume canonical TroubleBrewingDawnDeathFacts

e8af84b34f6df26a4ae442edd942af3b031aa4cd
  GREEN 1 — Dawn passes canonical attackOutcome / originalDeathSeat / mayorSeat / demonSafeSeats into planner

c5a9a8da8a6c76a223fd4765745c8797792ef84f
  RED 2 — App must not re-gate planner DawnDeathIntent with a second Monk/Soldier materialization branch

7139c8f9be8613ac082eafacf484f2c9c84a54f0
  GREEN 2 — NightDawnResolutionPlanner / DawnDeathIntent is the sole Dawn death materialization authority
```

Independent RED provenance:

```text
RED 1: PR CI #884
       924 tests / exactly 1 expected C2C ownership failure
       R2 + Clingo otherwise GREEN

RED 2: PR CI #886
       924 tests / exactly 1 expected duplicate-protection-authority failure
       R2 #813 SUCCESS
       Clingo SUCCESS
```

Focused C2C GREEN proved with `--rerun-tasks`:

```text
ClocktowerNightTransactionArchitectureGuardTest
ClocktowerDemonAttackDawnFactsTest
NightDawnResolutionPlannerAttackOutcomeContractTest
NightDawnResolutionPlannerMayorContractTest
```

The complete-worktree-safe runner also proved:

```text
exact RED-head guard PASS
only CampBoardGameHostApp.kt changed in each GREEN production patch
git diff --check PASS
focused C2C GREEN PASS
```

C2C canonical Dawn flow is now:

```text
confirmed attack / poison / Monk facts
+ stable cards
→ resolveTroubleBrewingDawnDeathFacts
→ NightDawnResolutionInput
→ NightDawnResolutionPlanner.planValidatedNightDeath
→ DawnCommitIntent.death
→ App durable materialization
```

Important ownership consequence:

- App no longer derives `originalDeathCard → Mayor ability` as an independent Dawn death authority.
- App no longer re-gates a non-null `DawnDeathIntent` with `if (protectedByMonk || protectedBySoldier)`.
- Soldier/Monk checks that remain in the Dawn block are presentation/record-detail logic for a canonical no-death result, not a second materialization gate.
- Ravenkeeper consequences are evaluated only after a canonical death is actually materialized.

**C2C completion does not close SNE-7.9C.** Two duplicate production consumers from the acceptance audit still remain:

```text
Host
  resolvedNightDeathName / nightDeathWillOccur

observation preflight
  independent Demon poison / Mayor redirect / Monk / Soldier death reconstruction
```

These consumers must converge on the canonical death authority before SNE-7.9C can be marked complete.

### SNE-7.9D — Demon succession legality / Dawn

**NOT STARTED**

After C is complete, address the earlier #4/#5 findings:

- production Imp self-kill successor path must consume validated `DemonSuccessionResolution / planDemonSuccession()` semantics;
- reconstructor must not accept successor merely because a player exists, is alive, and is a Minion;
- do not expand into deferred generic non-self Demon-death/custom-script succession.

### SNE-7.9E — real restore + durable Dawn integration

**NOT STARTED**

After D, address #7/#8:

```text
persist / restore
→ NightTransactionReconstructor
→ reconstructed same-night state
→ canonical death / succession resolution
→ Dawn durable materialization
→ ActionFact / observation
→ phase transition
```

The existing lifecycle smoke remains useful but insufficient because it deliberately stops before App-owned durable effects.

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
- changed reconfirmation invalidates dependent confirmations while preserving editable drafts;
- same-night `RoleChanged` is projected before Dawn materialization;
- persistent effects follow source ability lifetime;
- rules determine legality, recommendation ranks legal choices, UI displays legal choices;
- pure semantics may support future cases, production wiring activates only validated slices;
- `ClocktowerNightCheckpoint` remains the sole durable unfinished-night state;
- do not create event sourcing by replaying transient `NightResolutionEvent` commands;
- source ownership guards protect structural responsibility, not formatting.

## 6. Testing / writer policy

Preferred proof order:

```text
typed pure/domain behavior
→ typed reducer/planner/session behavior
→ typed adapter/integration behavior
→ minimal coarse source ownership guard only where App/Compose is not directly JVM-callable
```

Cadence:

```text
micro-slice
  → exact RED
  → minimal GREEN
  → focused --rerun-tasks
  → git diff --check
  → push
  → Chat remote parent/diff/scope audit

logical checkpoint
  → :app:testFast + triggered T2/T3
  → latest-head GitHub CI/R2
```

Do not duplicate an already valid focused test run. Do not wait for old-head CI before continuing an already-focused-GREEN related micro-slice.

`CampBoardGameHostApp.kt` remains a large file; edits require a complete-worktree-safe path, not truncated whole-file connector replacement.

## 7. Current exact next-start instruction

```text
1. latest C2C code checkpoint = 7139c8f9be8613ac082eafacf484f2c9c84a54f0;
2. accept C2C only after the latest normal user-authored checkpoint receives broad CI/R2 success;
3. re-audit the two remaining SNE-7.9C consumers:
     a. observation preflight
     b. Host resolvedNightDeathName / nightDeathWillOccur;
4. choose the smaller tests-first consumer cut-over next;
5. do not introduce a second durable death state owner;
6. do not begin 7.9D or 7.9E while C remains open;
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
