# Next Development Handoff — SNE-7.9 Corrective Continuation

> Date: 2026-08-28  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/clocktower-same-night-effective-state-correctness`  
> Draft PR: #54  
> Stable `main`: `c8985cb4991f6c7e5ea02adedb932d2d86452da1`  
> Latest verified code checkpoint before this docs correction: `7fa38d47d682cfb324052d8b56c46563ffc0b815`  
> Handoff status: **SNE-7 REOPENED — SNE-7.9 corrective campaign active; 7.9A/B complete, 7.9C in progress, C2C next; PR #54 stays draft/unmerged**

## 1. Startup contract

Before changing code, read:

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/SNE_7_AUTHORITATIVE_NIGHT_TRANSACTION_BOUNDARY_2026-08-27.md`;
5. `docs/SAME_NIGHT_EFFECTIVE_STATE_DECISIONS_2026-08-27.md`;
6. `docs/DEVELOPMENT_LESSONS_2026-08-27_SAME_NIGHT_CAMPAIGN.md`;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`;
8. `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
9. `docs/TESTING_STRATEGY.md`.

Then re-query live `main`, active branch, PR #54 state/head and latest checks. Never assume a SHA below is still live after further work.

## 2. Why this handoff supersedes the earlier closeout wording

The earlier handoff said `SNE-7.1–7.8 IMPLEMENTATION COMPLETE` and treated final CI/PR acceptance as the only remaining work. Final acceptance audit later found blocking production defects and integration gaps that the existing typed seams and smoke tests did not cover.

The authoritative correction is:

> **SNE-7 REOPENED — blocking correctness findings discovered during final acceptance audit.**

Do not mark PR #54 ready or merge it while SNE-7.9 remains active.

## 3. Corrective route

```text
SNE-7.9A  Mayor redirect dependency invalidation
SNE-7.9B  Chambermaid stale-target revalidation
SNE-7.9C  canonical night-death resolution
SNE-7.9D  succession legality / Dawn
SNE-7.9E  real restore + durable Dawn integration
```

This order is deliberate. Fix the proven dependency bugs first, then converge death authority, then fix succession, then activate restore/durable integration.

## 4. Completed corrective slices

### 7.9A — Mayor redirect dependency invalidation

**COMPLETE**

Production checkpoint:

```text
12d84cc9ed8076df9833d2fa268bc523283211b2
fix: invalidate stale Mayor redirect on upstream reconfirm
```

Contracts now held by `NightCheckpointReducer`:

```text
ConfirmPoison value changed
→ clear confirmedMayorRedirectTarget
→ clear confirmedDemonSuccessorTarget
→ keep editable drafts

ConfirmMonkProtection value changed
→ same invalidation

ConfirmDemonAttack value changed
→ same invalidation

idempotent reconfirm
→ preserve dependent confirmations
```

Do not broaden the dependency direction. Mayor confirmation does not automatically invalidate successor confirmation.

### 7.9B — Chambermaid stale-target revalidation

**COMPLETE**

Production checkpoint:

```text
f9300f72b4ef63a521a87e9d2a087c7ae9db2f03
fix: revalidate Chambermaid selection authority
```

Canonical selection path:

```text
stored Chambermaid first/second
+ eligible names at current effective cursor
→ revalidateTwoPlayerSelection
→ resolveChambermaidSelection
→ revalidated selection + wokeCount
```

Production result, visible target labels, recorded targets, and active selection consume the revalidated result.

## 5. SNE-7.9C progress

### C1 — planner attack-outcome authority

Accepted checkpoint:

```text
58fe6cd4e927128c3cb208dab9d12c8423ca5188
refactor: route Dawn death through attack outcome authority
```

`NightDawnDeathResolutionInput` can consume `DemonNightAttackOutcome`; the planner treats canonical `NO_DEATH` as authoritative and derives Mayor redirect applicability from canonical outcome when supplied.

### C2A — Trouble Brewing Demon attack production adapter

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

This adapter owns only the direct Imp attack outcome. Mayor redirect and Demon succession remain separate rule-owned choice boundaries. Do not generalize it into arbitrary custom-script Demon death.

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

`demonSafeSeats` reuses the same attack adapter for Soldier/Monk/poison safety instead of rebuilding those rules independently. Stable seat identity comes from original `cards` ordering.

Latest-head broad validation at `7fa38d47...`:

```text
CI #882 SUCCESS
R2 #809 SUCCESS
Android full unit tests/build SUCCESS
Real Clingo cross-validation SUCCESS
```

This does **not** mean 7.9C is complete. The new facts exist, but the real App Dawn consumer has not yet cut over to them.

## 6. Exact next slice — SNE-7.9C2C

### Goal

**Cut the real App Dawn death transaction over to `resolveTroubleBrewingDawnDeathFacts()` and make `NightDawnResolutionPlanner` the consumer of those canonical facts.**

Current production gap:

```text
App Dawn currently re-derives:
  originalDeathCard
  mayorCanRedirect
  originalDeathSeat

then calls planner without:
  attackOutcome
  demonSafeSeats

then later performs another Monk/Soldier protection check
```

C2C target flow:

```text
clocktowerPendingNightDeath
+ clocktowerConfirmedPoisonTarget
+ clocktowerConfirmedMonkProtectedTarget
+ cards
→ resolveTroubleBrewingDawnDeathFacts(...)
→ NightDawnDeathResolutionInput(
     attackOutcome = facts.attackOutcome,
     originalDeathSeat = facts.originalDeathSeat,
     mayorSeat = facts.mayorSeat,
     mayorRedirectMayApply = legacy compatibility value only if signature still requires it,
     demonSafeSeats = facts.demonSafeSeats,
     effectiveNightState = ...,
     demonRoleIds = ...,
   )
→ NightDawnResolutionPlanner.planValidatedNightDeath(...)
→ DawnCommitIntent.death
```

### C2C scope

Allowed behavior change:

- Dawn direct-attack / Mayor applicability / Soldier / functioning-Monk safety decision consumes the canonical typed facts instead of a second App-owned rule copy.

Keep unchanged unless minimally required by the cut-over:

- checkpoint ownership;
- durable event sequence allocation;
- ActionFact / observation writing outside this direct Dawn decision;
- Ravenkeeper/Klutz consequence handling after the canonical death intent is known;
- phase transition;
- Demon successor behavior;
- restore/reconstructor behavior;
- Host UI calculation;
- observation preflight calculation.

Explicitly **do not** migrate Host or observation preflight in C2C. Those are later 7.9C consumer slices.

### C2C RED strategy

Because the large Compose/App Dawn boundary is not directly JVM-callable, use a narrow coarse ownership guard only for the missing production cut-over, while keeping gameplay semantics in the existing typed adapter/planner tests.

RED should prove the Dawn transaction must:

1. call `resolveTroubleBrewingDawnDeathFacts`;
2. pass canonical `attackOutcome` into `NightDawnDeathResolutionInput`;
3. pass canonical `demonSafeSeats` into `NightDawnDeathResolutionInput`;
4. stop using the Dawn block's inline `originalDeathCard → functionsAs("Mayor")` computation as death authority.

Do not write exact-whitespace/source-format assertions. Anchor the real Dawn block and check structural tokens/forbidden legacy ownership only.

### Focused GREEN set

At minimum:

```text
ClocktowerDemonAttackDawnFactsTest
NightDawnResolutionPlannerAttackOutcomeContractTest
NightDawnResolutionPlannerMayorContractTest
new C2C Dawn production ownership RED/GREEN test
```

Also run any directly affected existing Dawn/source ownership test discovered during implementation. Use `--rerun-tasks`; then `git diff --check`.

C2C is a production transaction-boundary cut-over. After focused GREEN and remote diff audit, run the appropriate checkpoint broader gate (`:app:testFast` plus GitHub CI/R2) before accepting the slice if the current testing strategy classifies it as a logical checkpoint.

## 7. What remains after C2C

Do not jump directly to 7.9D merely because C2C is GREEN. Re-audit the remaining 7.9C consumers.

Known remaining duplicate authorities from the acceptance audit:

```text
Host
  resolvedNightDeathName / nightDeathWillOccur

observation preflight
  independent Demon poison / Mayor / Monk / Soldier death reconstruction

Dawn
  C2C target: canonical cut-over
```

After C2C, choose the next narrow 7.9C consumer slice based on which remaining path can consume the canonical result without introducing a second state owner.

Only after Host / observation / Dawn have converged sufficiently should SNE-7.9C be marked complete.

## 8. Deferred corrective slices

### 7.9D — succession legality / Dawn

**NOT STARTED**

Must address:

- production Imp self-kill successor path must consume validated `DemonSuccessionResolution / planDemonSuccession()` rather than trusting a stale confirmed name;
- `NightTransactionReconstructor` must not accept a successor solely because the player exists, is alive, and is a Minion;
- do not broaden into generic non-self Demon death / Scarlet Woman/custom-script behavior beyond the already-authorized Trouble Brewing path.

### 7.9E — real restore + durable Dawn integration

**NOT STARTED**

Must address:

- real App restore consumes `NightTransactionReconstructor` rather than direct field projection only;
- add integration coverage crossing real restore to canonical resolution and App-owned durable Dawn effects;
- cover ActionFact/observation/phase materialization as appropriate;
- existing lifecycle smoke remains useful but is insufficient because it stops before App-owned durable side effects.

## 9. Protected architecture rules

- rules determine legality; recommendation only ranks legal choices;
- stable identity never comes from a filtered list index;
- draft state cannot become a confirmed mechanical fact without the explicit confirmation boundary;
- navigation alone does not roll back confirmed mechanics;
- changed reconfirmation invalidates dependent confirmation while preserving editable draft;
- same-night mechanics use projected effective state; do not mutate persisted/public role/death early;
- persistent effects follow source ability lifetime;
- pure semantics may support future cases, but production activates only validated slices;
- `ClocktowerNightCheckpoint` remains the sole durable unfinished-night checkpoint;
- do not introduce event sourcing by replaying transient `NightResolutionEvent` commands;
- source ownership tests must protect structural responsibility, not formatting.

## 10. Writer and validation contract

Follow `AGENTS.md`.

```text
Chat / GitHub connector
  small tests/docs/source edits
  architecture / exact patch decisions
  remote diff / parent / scope audit

complete-worktree-safe path
  large CampBoardGameHostApp.kt edit
  exact localized patch
  focused Gradle execution
  git diff --check
```

Micro-cycle:

```text
RED
→ prove intended failure
→ minimal GREEN
→ focused --rerun-tasks
→ git diff --check
→ push
→ remote parent/diff/scope audit
```

Do not wait for old-head CI between focused-GREEN micro-slices. Do not repeat an identical focused test merely to duplicate already-valid evidence.

## 11. Exact next-start instruction

```text
1. verify live PR #54 head still descends from 7fa38d47;
2. create C2C production-ownership RED only; no production change in the RED commit;
3. prove the RED fails specifically because real Dawn does not consume canonical Dawn death facts;
4. apply the smallest Dawn production cut-over;
5. run focused GREEN with --rerun-tasks and git diff --check;
6. remote-audit parent/diff/scope;
7. run checkpoint broader validation if required by TESTING_STRATEGY;
8. keep PR #54 draft/unmerged;
9. do not begin 7.9D/7.9E until 7.9C consumer convergence is complete.
```

Never merge, mark ready, rebase, force-push, or broaden PR #54 without explicit user authorization.
