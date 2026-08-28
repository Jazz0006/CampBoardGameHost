# Next Development Handoff — SNE-7.9 Corrective Continuation

> Date: 2026-08-28  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Latest 7.9C code checkpoint: `b6185ccf6b23583c112b040f831e65f2724f1035`  
> Handoff status: **SNE-7 REOPENED — 7.9A/B complete; 7.9C implementation complete / focused GREEN; normal latest-head broad CI/R2 checkpoint pending; 7.9D starts only after that gate is GREEN; PR #54 stays draft/unmerged**

## 1. Startup contract

Read before changing code:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`;
8. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
9. `docs/TESTING_STRATEGY.md`.

Then re-query live `main`, active branch, PR #54 state/head and latest checks. Never assume a SHA below remains live after later work.

## 2. Corrected campaign state

The earlier `SNE-7.1–7.8 IMPLEMENTATION COMPLETE / FINAL VALIDATION` wording is superseded. Final acceptance audit exposed blocking correctness and integration defects beyond the typed seams then covered.

Authoritative state:

> **SNE-7 REOPENED — blocking correctness findings discovered during final acceptance audit.**

Corrective route:

```text
SNE-7.9A  Mayor redirect dependency invalidation
SNE-7.9B  Chambermaid stale-target revalidation
SNE-7.9C  canonical night-death resolution
SNE-7.9D  succession legality / Dawn
SNE-7.9E  real restore + durable Dawn integration
```

Do not mark PR #54 ready or merge it while this campaign remains active.

## 3. Completed corrective slices

### 7.9A — Mayor redirect dependency invalidation

**COMPLETE / GREEN**

```text
12d84cc9ed8076df9833d2fa268bc523283211b2
fix: invalidate stale Mayor redirect on upstream reconfirm
```

`NightCheckpointReducer` invalidates confirmed Mayor redirect and Demon successor when confirmed Poison / Monk / Demon attack values truly change, while preserving editable drafts. Idempotent reconfirm preserves dependent confirmation.

### 7.9B — Chambermaid stale-target revalidation

**COMPLETE / GREEN**

```text
f9300f72b4ef63a521a87e9d2a087c7ae9db2f03
fix: revalidate Chambermaid selection authority
```

Canonical path:

```text
stored Chambermaid selection
+ current eligible set
→ revalidateTwoPlayerSelection
→ resolveChambermaidSelection
→ revalidated targets / wokeCount
```

Production result, target display, recording, and active selection consume the revalidated result.

## 4. SNE-7.9C — canonical night-death resolution

**IMPLEMENTATION COMPLETE / FOCUSED GREEN / NORMAL BROAD CHECKPOINT PENDING**

### C1 — planner attack-outcome authority

```text
58fe6cd4e927128c3cb208dab9d12c8423ca5188
refactor: route Dawn death through attack outcome authority
```

### C2A — direct Trouble Brewing Demon attack adapter

```text
dee41713e25b2387b77419a74ea256082fe2a44a  RED
e0b25a8d822bd348e33b6e7a9378be89bd564da9  GREEN
```

Established `resolveTroubleBrewingDemonNightAttackOutcome(...)`. This seam owns direct Imp attack outcome only; Mayor redirect and succession remain separate rule-owned boundaries.

### C2B — canonical Dawn death facts

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

Broad C2B checkpoint validation:

```text
CI #882 SUCCESS
R2 #809 SUCCESS
Android full unit tests/build SUCCESS
Real Clingo cross-validation SUCCESS
```

### C2C — real Dawn production consumer cut-over

**COMPLETE / BROAD GREEN**

Tests-first chain:

```text
b3c52db2ee5e89b390de366c00034f367e9ec033  RED 1
e8af84b34f6df26a4ae442edd942af3b031aa4cd  GREEN 1
c5a9a8da8a6c76a223fd4765745c8797792ef84f  RED 2
7139c8f9be8613ac082eafacf484f2c9c84a54f0  GREEN 2
```

RED evidence:

```text
PR CI #884: 924 tests / exactly 1 expected initial C2C ownership failure
PR CI #886: 924 tests / exactly 1 expected duplicate-protection-authority failure
R2 #813 SUCCESS
Clingo SUCCESS
```

Focused GREEN set with `--rerun-tasks`:

```text
ClocktowerNightTransactionArchitectureGuardTest
ClocktowerDemonAttackDawnFactsTest
NightDawnResolutionPlannerAttackOutcomeContractTest
NightDawnResolutionPlannerMayorContractTest
```

Normal broad checkpoint after C2C:

```text
CI #888 SUCCESS
R2 #815 SUCCESS
Android full unit tests/build SUCCESS
Real Clingo cross-validation SUCCESS
```

Current Dawn authority:

```text
confirmed attack / poison / Monk facts
+ stable original cards ordering
→ resolveTroubleBrewingDawnDeathFacts
→ NightDawnDeathResolutionInput
→ NightDawnResolutionPlanner
→ DawnCommitIntent.death
→ App durable death materialization
```

App no longer re-derives Mayor death authority or re-gates a planner death with Monk/Soldier protection.

### C3 — public-alive observation preflight cut-over

**COMPLETE / FOCUSED GREEN**

```text
6be1e7e7c923582039697c260ef21dfc757bc833  RED
e6c3d9ef7ab50e9e07d0ce570120bd63c68571af  GREEN
```

The RED proved the observation preflight still owned an independent Demon poison / Mayor / Monk / Soldier death reconstruction. The RED PR CI completed 925 tests with exactly one expected ownership failure; Clingo stayed GREEN.

The GREEN path is now:

```text
confirmed checkpoint facts
→ resolveTroubleBrewingDawnDeathFacts
→ NightDawnResolutionPlanner
→ DawnCommitIntent.death.targetSeat
→ resolved public death name
→ preflight AliveAt(false) sequence
```

The preflight keeps timeline sequence responsibility only; it no longer owns mechanical death semantics.

Complete-worktree-safe focused runner proved exact RED head, intended RED, single-App-file GREEN scope, `git diff --check`, focused GREEN, remote-head guard and push.

### C4 — Host death consumer cut-over

**COMPLETE / FOCUSED GREEN**

```text
28b8dcbf367852cf8f22d83d2e7f563a89bd50b5  RED
0a73673bb5ef15a5099f1da8f0b2df16e0fb9ab1  shared resolver seam
b6185ccf6b23583c112b040f831e65f2724f1035  GREEN
```

C4 RED proof:

```text
PR CI #891
926 tests / exactly 1 expected Host canonical-death ownership failure
Real Clingo cross-validation SUCCESS
```

Established:

```text
resolveTroubleBrewingDawnDeathResolution(
    cards,
    script,
    gameSeed,
    checkpoint,
)
```

This helper is a composition seam, not a second rule engine. It feeds confirmed `ClocktowerNightCheckpoint` facts into the existing canonical Trouble Brewing Dawn facts + `NightDawnResolutionPlanner`.

App now passes the live checkpoint into Host:

```text
nightCheckpoint = currentClocktowerNightCheckpoint()
```

Host now consumes:

```text
canonicalNightDeathResolution
→ mayorRedirectEligible
→ resolvedDeathName
→ resolvedDeathSeat
→ nightDeathWillOccur
```

Host no longer re-gates death with independent Demon poison / functioning Monk / Soldier logic. Remaining Mayor redirect target legality in Host is UI selection/presentation logic, not death authority.

The first GREEN runner attempt caught two stale `demonPoisonedTonight` references used only by Imp action explanatory copy. No large-file GREEN was pushed from that failed attempt. The corrected runner derives checkpoint-backed display-only `demonPoisonedForActionExplanation`; it is not used by resolved death or `nightDeathWillOccur`.

Final complete-worktree-safe C4 runner proved:

```text
exact helper-head guard PASS
App + Host patch scope exact
git diff --check PASS
focused C4 GREEN PASS
remote-head guard PASS
push PASS
```

Remote audit of `b6185ccf...`:

```text
parent = 0a73673bb5ef15a5099f1da8f0b2df16e0fb9ab1
CampBoardGameHostApp.kt              +1 / -0
ClocktowerHostScreen.kt             +21 / -36
```

### 7.9C closure result

The original acceptance audit identified three duplicate production death consumers:

```text
Dawn
observation preflight
Host UI / resolved mechanical event trigger
```

All three now converge on canonical Trouble Brewing death facts + `NightDawnResolutionPlanner`. Architecture ownership guards explicitly protect each boundary. Therefore **7.9C implementation is complete**.

The bot-authored C4 GREEN push created CI #893 and R2 #820 with conclusion `action_required` and zero jobs because the workflow actor/triggering actor was `github-actions[bot]`. This is not a test failure and is not broad acceptance evidence. The normal user-authored docs checkpoint following C4 must provide the real latest-head broad CI/R2 gate.

## 5. Next slice — SNE-7.9D succession legality / Dawn

**NOT STARTED — START ONLY AFTER 7.9C NORMAL BROAD GREEN**

Original acceptance findings to address:

1. production Imp self-kill successor path can still reach Dawn without one validated succession authority;
2. `NightTransactionReconstructor` successor legality is too weak if it only checks player existence/alive/Minion type.

Tests-first 7.9D scope:

```text
confirmed Imp self-kill
+ current effective alive/current-role state
+ confirmed successor target
→ canonical legal successor set
→ DemonSuccessionResolution / planDemonSuccession()
→ validated Dawn succession intent
```

Reconstructor must validate the same legal successor semantics used by production, not a parallel approximation.

Do not expand this slice into:

```text
generic non-self Demon-death succession
custom-script Demon succession
real restore wiring
A4/B4 authority promotion
recommendation tuning
unrelated Host/UI refactor
```

## 6. Deferred SNE-7.9E — real restore + durable Dawn integration

**NOT STARTED**

After 7.9D:

```text
persist / restore
→ NightTransactionReconstructor
→ canonical same-night state
→ death / succession resolution
→ Dawn materialization
→ ActionFact / observation
→ phase transition
```

Existing smoke coverage is insufficient because it stops before App-owned durable side effects.

## 7. Architecture invariants

- rules determine legality; recommendation ranks legal choices; UI displays legal choices;
- stable seat identity never comes from a filtered list;
- draft state cannot impersonate confirmed mechanical fact;
- changed reconfirmation invalidates dependent confirmed facts while preserving editable drafts;
- same-night mechanics use projected effective state instead of early persisted/public mutation;
- persistent effects follow source ability lifetime;
- pure semantics may support future cases, but production activates only validated slices;
- `ClocktowerNightCheckpoint` remains the sole durable unfinished-night owner;
- transient `NightResolutionEvent` commands are not event sourcing;
- source ownership guards protect coarse structural responsibility, never formatting.

## 8. Writer / validation contract

Follow `AGENTS.md`.

```text
small tests/docs/source
  → Chat + GitHub connector

large CampBoardGameHostApp.kt edits
  → complete-worktree-safe exact patch path
```

Tests-first micro-cycle:

```text
RED
→ prove intended failure
→ minimal GREEN
→ focused --rerun-tasks
→ git diff --check
→ push
→ remote parent/diff/scope audit
```

Checkpoint validation:

```text
:app:testFast / triggered broader validation
→ latest-head GitHub CI/R2
```

Do not repeat identical focused tests merely to duplicate evidence.

## 9. Exact next-start instruction

```text
1. re-query PR #54 head after this docs checkpoint;
2. require normal latest-head CI + R2 success for the user-authored checkpoint;
3. if broad gate is GREEN, formally accept SNE-7.9C as broad GREEN;
4. then begin SNE-7.9D tests-first with succession legality audit/RED;
5. do not begin 7.9E and do not broaden 7.9D scope;
6. keep PR #54 draft/unmerged.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
