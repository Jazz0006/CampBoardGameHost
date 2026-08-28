# CampBoardGameHost 自动说书人 — 当前开发路线

> 状态日期：2026-08-28  
> 文档角色：**CURRENT / 当前状态唯一权威**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Active branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Latest canonical-death code checkpoint: `b6185ccf6b23583c112b040f831e65f2724f1035`  
> Current priority: **SNE-7 REOPENED — SNE-7.9C canonical night-death resolution IMPLEMENTATION COMPLETE / focused GREEN; normal user-authored broad CI/R2 checkpoint pending; SNE-7.9D is next only after that gate is GREEN**

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

The order remains intentional. 7.9C implementation is now closed; do not begin D until the normal user-authored latest-head broad CI/R2 checkpoint for the C3/C4 completion is GREEN.

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

**IMPLEMENTATION COMPLETE / FOCUSED GREEN / NORMAL BROAD CHECKPOINT PENDING**

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

**COMPLETE / BROAD GREEN**

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

Normal latest-head broad checkpoint after C2C:

```text
CI #888 SUCCESS
R2 #815 SUCCESS
Android full unit tests/build SUCCESS
Real Clingo cross-validation SUCCESS
```

C2C canonical Dawn flow:

```text
confirmed attack / poison / Monk facts
+ stable cards
→ resolveTroubleBrewingDawnDeathFacts
→ NightDawnResolutionInput
→ NightDawnResolutionPlanner.planValidatedNightDeath
→ DawnCommitIntent.death
→ App durable materialization
```

Ownership consequence:

- App no longer derives `originalDeathCard → Mayor ability` as an independent Dawn death authority.
- App no longer re-gates a non-null `DawnDeathIntent` with `if (protectedByMonk || protectedBySoldier)`.
- Soldier/Monk checks that remain in the Dawn block are presentation/record-detail logic for a canonical no-death result, not a second materialization gate.
- Ravenkeeper consequences are evaluated only after a canonical death is actually materialized.

#### C3 — public-alive observation preflight consumer cut-over

**COMPLETE / FOCUSED GREEN**

```text
6be1e7e7c923582039697c260ef21dfc757bc833  RED
e6c3d9ef7ab50e9e07d0ce570120bd63c68571af  GREEN
```

The RED proved that `nextNightPublicAliveObservationPreflightOrNull()` still independently reconstructed Demon poison / Mayor / Monk / Soldier death semantics. PR CI on the RED checkpoint completed 925 tests with exactly one expected architecture-ownership failure; Clingo remained GREEN.

The GREEN now routes the durable public-alive observation preflight through:

```text
resolveTroubleBrewingDawnDeathFacts
→ NightDawnResolutionPlanner.planValidatedNightDeath
→ DawnCommitIntent.death.targetSeat
→ resolved public death name
→ preflight observation sequence
```

The preflight retains only timeline sequencing responsibility. It no longer owns an independent mechanical death decision.

Focused complete-worktree-safe runner proved:

```text
exact RED-head guard PASS
intended C3 RED PASS
only CampBoardGameHostApp.kt changed in production GREEN
git diff --check PASS
focused C3 GREEN PASS
remote-head guard + push PASS
```

#### C4 — Host death presentation / trigger consumer cut-over

**COMPLETE / FOCUSED GREEN**

```text
28b8dcbf367852cf8f22d83d2e7f563a89bd50b5  RED
0a73673bb5ef15a5099f1da8f0b2df16e0fb9ab1  shared checkpoint-backed resolver seam
b6185ccf6b23583c112b040f831e65f2724f1035  GREEN
```

C4 RED was independently proven by PR CI #891:

```text
926 tests / exactly 1 expected Host canonical-death ownership failure
Real Clingo cross-validation SUCCESS
```

Established shared resolver:

```text
resolveTroubleBrewingDawnDeathResolution(
    cards,
    script,
    gameSeed,
    checkpoint,
)
```

The resolver does not replace or duplicate `NightDawnResolutionPlanner`; it composes the existing canonical Trouble Brewing Dawn facts with the planner using confirmed facts from the real `ClocktowerNightCheckpoint`.

App now passes:

```text
nightCheckpoint = currentClocktowerNightCheckpoint()
```

Host now derives:

```text
canonicalNightDeathResolution
→ mayorRedirectEligible
→ resolvedDeathName
→ resolvedDeathSeat
→ nightDeathWillOccur
```

Host no longer re-decides death with independent Demon-poison / functioning-Monk / Soldier gates. The remaining `MayorRedirectLegality.canReceiveRedirect` use is UI target-list presentation/selection legality, not a death-resolution authority.

A first focused GREEN runner attempt caught two stale `demonPoisonedTonight` references in Imp-action explanatory copy before any large-file GREEN was pushed. They were replaced with checkpoint-backed display-only `demonPoisonedForActionExplanation`; this value controls wording only and is not used by `nightDeathWillOccur` or resolved-death selection.

Final complete-worktree-safe C4 runner proved:

```text
exact helper-head guard PASS
large-file patch application PASS
only CampBoardGameHostApp.kt + ClocktowerHostScreen.kt changed
git diff --check PASS
focused C4 GREEN PASS
remote-head guard + push PASS
```

Remote audit of `b6185ccf...` confirmed:

```text
parent = 0a73673bb5ef15a5099f1da8f0b2df16e0fb9ab1
CampBoardGameHostApp.kt              +1 / -0
ClocktowerHostScreen.kt             +21 / -36
```

#### 7.9C authority closure

All three production death consumers identified by the acceptance audit now converge on canonical authority:

```text
Dawn durable materialization
public-alive observation preflight
Host death presentation / resolved mechanical event trigger
```

Architecture ownership guards protect all three boundaries. Therefore **7.9C implementation is complete**.

The immediate PR-triggered CI #893 / R2 #820 created by the GitHub Actions bot push of `b6185ccf...` ended as `action_required` with zero jobs because the workflow actor/triggering actor was `github-actions[bot]`. This is not a test failure and is not accepted as broad evidence. A normal user-authored docs checkpoint must trigger the real latest-head CI/R2 gate before 7.9D starts.

### SNE-7.9D — Demon succession legality / Dawn

**NOT STARTED / NEXT AFTER 7.9C BROAD GREEN**

After the normal latest-head broad gate for the 7.9C completion succeeds, address the earlier #4/#5 findings tests-first:

- production Imp self-kill successor path must consume validated `DemonSuccessionResolution / planDemonSuccession()` semantics;
- reconstructor must not accept successor merely because a player exists, is alive, and is a Minion;
- validate current legal successor set at the relevant effective night state;
- do not expand into deferred generic non-self Demon-death/custom-script succession.

### SNE-7.9E — real restore + durable Dawn integration

**NOT STARTED**

After D, address restore and durable integration:

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
1. latest 7.9C code checkpoint = b6185ccf6b23583c112b040f831e65f2724f1035;
2. this normal user-authored docs checkpoint must receive latest-head broad CI/R2 success;
3. only after that broad gate, accept SNE-7.9C as broad GREEN and begin SNE-7.9D tests-first;
4. 7.9D scope = validated Imp self-kill succession legality / Dawn + reconstructor legality only;
5. do not expand to generic non-self Demon-death/custom-script succession, restore, A4/B4, recommendation, or unrelated Host work;
6. keep PR #54 draft/unmerged.
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
