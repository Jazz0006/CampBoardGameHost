# Next Development Handoff — SNE-7.9 Corrective Continuation

> Date: 2026-08-28  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Latest C2C code checkpoint: `7139c8f9be8613ac082eafacf484f2c9c84a54f0`  
> Handoff status: **SNE-7 REOPENED — 7.9A/B complete; 7.9C active; C2C Dawn cut-over implementation complete / focused GREEN; remaining Host + observation consumers next; PR #54 stays draft/unmerged**

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

**COMPLETE**

```text
12d84cc9ed8076df9833d2fa268bc523283211b2
fix: invalidate stale Mayor redirect on upstream reconfirm
```

`NightCheckpointReducer` now invalidates confirmed Mayor redirect and Demon successor when confirmed Poison / Monk / Demon attack values truly change, while preserving editable drafts. Idempotent reconfirm preserves dependent confirmation.

### 7.9B — Chambermaid stale-target revalidation

**COMPLETE**

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

**IN PROGRESS**

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

Established `resolveTroubleBrewingDemonNightAttackOutcome(...)`. This seam owns only direct Imp attack outcome; Mayor redirect and succession remain separate rule-owned boundaries.

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

Broad C2B checkpoint validation at `7fa38d47...`:

```text
CI #882 SUCCESS
R2 #809 SUCCESS
Android full unit tests/build SUCCESS
Real Clingo cross-validation SUCCESS
```

### C2C — real Dawn production consumer cut-over

**IMPLEMENTATION COMPLETE / FOCUSED GREEN**

Tests-first checkpoint chain:

```text
b3c52db2ee5e89b390de366c00034f367e9ec033
  RED 1 — App Dawn must consume canonical TroubleBrewingDawnDeathFacts

e8af84b34f6df26a4ae442edd942af3b031aa4cd
  GREEN 1 — canonical facts enter NightDawnResolutionPlanner input

c5a9a8da8a6c76a223fd4765745c8797792ef84f
  RED 2 — App must not re-gate planner death intent with Monk/Soldier

7139c8f9be8613ac082eafacf484f2c9c84a54f0
  GREEN 2 — planner/DawnDeathIntent becomes sole Dawn materialization authority
```

Independent RED evidence:

```text
PR CI #884
  924 tests / exactly 1 expected initial C2C ownership failure

PR CI #886
  924 tests / exactly 1 expected duplicate-protection-authority failure
  R2 #813 SUCCESS
  Clingo SUCCESS
```

Focused GREEN set, executed with `--rerun-tasks`:

```text
ClocktowerNightTransactionArchitectureGuardTest
ClocktowerDemonAttackDawnFactsTest
NightDawnResolutionPlannerAttackOutcomeContractTest
NightDawnResolutionPlannerMayorContractTest
```

Complete-worktree-safe GREEN execution also established:

```text
exact expected RED parent/head guard PASS
only CampBoardGameHostApp.kt production file changed
git diff --check PASS
focused GREEN PASS
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

Important details:

- App no longer owns an inline `originalDeathCard → functionsAs("Mayor")` death decision.
- App no longer uses `if (protectedByMonk || protectedBySoldier)` to re-decide whether a non-null planner death should materialize.
- remaining Soldier/Monk calculations in Dawn are record-detail/presentation logic for canonical no-death outcomes only.
- Ravenkeeper consequences are evaluated only after actual canonical death materialization.

C2C is a completed sub-slice, but **SNE-7.9C is not complete**.

## 5. Remaining SNE-7.9C consumers

The original acceptance audit identified three independent death consumers:

```text
Host UI/effective state
observation preflight
Dawn
```

C2C closes the Dawn consumer. Remaining duplicate authorities are:

```text
Host
  resolvedNightDeathName
  nightDeathWillOccur

observation preflight
  independent Demon poison
  Mayor redirect
  Monk/Soldier death reconstruction
```

The next task is **not 7.9D**. First re-audit these two remaining consumers and choose the smaller tests-first cut-over.

Selection principle:

- prefer a typed adapter/integration seam over source-string behavior tests;
- do not introduce a second persisted/cached resolved-death owner;
- consumers should derive/consume canonical resolution from existing confirmed checkpoint facts;
- retain stable table seat identity;
- preserve UI/event presentation semantics separately from mechanical death authority.

## 6. Deferred slices

### 7.9D — Demon succession legality / Dawn

**NOT STARTED**

After 7.9C closes:

- production Imp self-kill successor path must consume validated `DemonSuccessionResolution / planDemonSuccession()`;
- reconstructor must validate the actual legal successor set, not merely existence/alive/Minion type;
- do not resurrect deferred generic non-self Demon-death/custom-script succession.

### 7.9E — real restore + durable Dawn integration

**NOT STARTED**

After D:

```text
persist / restore
→ NightTransactionReconstructor
→ canonical same-night state
→ death / succession resolution
→ Dawn materialization
→ ActionFact / observation
→ phase transition
```

Existing smoke coverage is not sufficient because it stops before App-owned durable side effects.

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

Do not repeat identical focused tests merely to duplicate evidence. Do not wait for obsolete old-head CI before progressing an already-focused-GREEN related slice.

## 9. Exact next-start instruction

```text
1. latest C2C code checkpoint = 7139c8f9be8613ac082eafacf484f2c9c84a54f0;
2. obtain ordinary latest-head broad CI/R2 evidence for the normal checkpoint commit;
3. re-audit observation preflight and Host death consumers against the canonical Dawn authority;
4. select the smaller remaining 7.9C consumer slice;
5. establish its typed/ownership RED before production modification;
6. do not begin 7.9D or 7.9E yet;
7. keep PR #54 draft/unmerged.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
