# MS-SETUP Recovery Scope Reduction Audit

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Status: **MS-S0.5 PRODUCT/ARCHITECTURE DECISION — ACCEPTED FOR PLANNING**

## 1. Decision

The product no longer promises arbitrary exact continuation of an unfinished game after exit/restart.

The supported recovery goal is now deliberately narrower:

```text
normal in-memory play
+ best-effort crash / Android process-death recovery
-> restore the latest stable committed domain checkpoint
-> preserve already committed game facts exactly
-> discard/restart transient UI state and uncommitted decisions when necessary
```

Explicit non-goals:

- “play half today and continue tomorrow” as a product contract;
- exact restoration to an arbitrary in-progress UI interaction;
- durable preservation of draft targets or temporary selection state merely so the same partially completed screen can be reconstructed;
- indefinite cross-version compatibility for unfinished-game saves.

An incompatible unfinished save may be rejected/discarded by an explicit compatibility policy. Completed-game history remains independent durable product data.

## 2. Live baseline audited

At the start of this audit:

```text
live main:
eed51bade5163790316a31e8295e2e841df90357

validated merged code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

PR #57:
MERGED

post-merge CI:
CI #1179 / run 33346311357 — SUCCESS
```

The new planning branch was created directly from live `main`:

```text
codex/ms-setup-generic-architecture
base: eed51bade5163790316a31e8295e2e841df90357
```

## 3. Audit conclusion

The perceived complexity is real, but the correct response is **scope reduction**, not deletion of all recovery/session machinery.

The codebase already has two different kinds of mechanisms that happen to use “restore/recovery” language:

1. exact reconstruction of an **unfinished night**, including draft/in-progress state;
2. domain transaction recovery / retry convergence that protects already committed game facts.

Only the first category is directly reduced by the product decision. The second category remains a correctness requirement.

## 4. Concrete evidence that exact unfinished-night restore exists

### 4.1 `ClocktowerNightCheckpoint`

`app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerNightCheckpoint.kt`

Its documented purpose is a persisted continuation point for an unfinished Clocktower night. It currently persists both committed and draft/in-progress values, including:

```text
nightStarted
nightStepIndex
confirmedAttackTarget
attackDraftTarget
confirmedPoisonTarget
poisonDraftTarget
confirmedMonkTarget
monkDraftTarget
confirmedMayorRedirectTarget
mayorRedirectDraftTarget
pendingNewDemonName
pendingNightNewDemonIdentityName
demonSuccessorDraftTarget
confirmedDemonSuccessorTarget
nextTimelineGlobalSequence
```

This is stronger than the newly required stable-checkpoint contract because it explicitly preserves drafts and an in-progress night cursor.

### 4.2 `NightTransactionRestoreComposition`

`app/src/main/java/com/codex/campboardgamehost/clocktower/session/NightTransactionRestoreComposition.kt`

This is explicitly described as pure restore composition for an unfinished Clocktower night. It rebuilds a checkpoint from persisted values and immediately reconstructs same-night derived mechanical state.

### 4.3 `NightTransactionRestoreCompositionTest`

`app/src/test/java/com/codex/campboardgamehost/clocktower/session/NightTransactionRestoreCompositionTest.kt`

The test suite explicitly states:

```text
persisted unfinished-night state must activate derived mechanics on restore
```

Its fixtures persist `nightStepIndex`, draft attack/succession targets and confirmed values, then assert that exact restored unfinished-night state reconstructs current derived mechanics.

Therefore exact in-progress night restoration is not hypothetical test debt; it is an implemented and tested product capability.

## 5. Recovery code that must not be removed just because the product scope changed

### 5.1 Stable committed facts

The following remain durable authorities where applicable:

- exact committed initial setup;
- actual roles;
- shown identities;
- stable seat identity;
- committed action facts;
- committed epistemic observations;
- durable timeline/global sequence identity;
- committed phase/round transitions;
- completed-game setup/diversity history.

### 5.2 Retry/idempotency/convergence mechanics

Names containing `RecoveryAuthority`, `Reducer` or `Reconstructor` must not be deleted mechanically.

For example, `NightDawnRestoreRetryConvergenceAcceptanceTest` proves that partially materialized state plus durable history converges to the same durable Dawn state as uninterrupted execution and remains exactly-once under retry. That protects transactional correctness, not merely “return to the same UI screen.”

Likewise existing poison expiry/recovery authorities that prevent duplicate or missing committed effects remain protected unless a later focused ownership audit proves they exist only for obsolete draft-resume behavior.

## 6. KEEP / SIMPLIFY / RETIRE classification

### KEEP

Keep these concepts and semantics:

- `ClocktowerGameSession` authority for committed global timeline/domain facts;
- a stable domain checkpoint abstraction;
- committed action/observation history and timeline identity;
- exact committed setup facts;
- process-death recovery from the latest supported stable checkpoint;
- Dawn/Dusk and other transaction retry/idempotency/convergence guarantees;
- completed-game history and setup diversity history;
- compatibility/version guards for persisted data.

A retained checkpoint does not need to mean “resume the exact screen.” It means “recover the latest committed domain state from which play can safely continue.”

### SIMPLIFY / REPLACE

These areas should be migrated rather than immediately deleted:

1. **`ClocktowerNightCheckpoint` payload**  
   Separate stable committed facts from transient draft fields. Retain only the minimum cursor/boundary necessary to know what committed action comes next. Do not persist a draft merely to recreate a partially completed interaction.

2. **`NightTransactionRestoreComposition` / `NightTransactionReconstructor` restore entry path**  
   Preserve reconstruction required for committed effective state, but stop promising exact recreation of an arbitrary draft/in-progress night transaction. Recovery should restart the current uncommitted interaction from a stable boundary when needed.

3. **TB setup restore**  
   Current TB persistence restores selection/provenance through TB-specific data. MS-SETUP must instead persist the exact generic committed setup as the restore authority. Provenance becomes source/audit metadata and must not be the recipe used to choose or rebuild a possibly different setup.

4. **unfinished-save compatibility**  
   Prefer an explicit schema/version compatibility gate. Supporting every historical unfinished-state shape indefinitely is no longer a product requirement.

### RETIRE AFTER REPLACEMENT / CALL-SITE PROOF

Do not delete these immediately. Retire them after the stable-checkpoint replacement is present and affected typed tests prove no required contract is lost:

- persistence fields whose only purpose is exact restoration of unconfirmed attack/poison/Monk/Mayor/succession draft selections;
- exact `nightStepIndex` restoration if a smaller stable action-boundary/cursor authority replaces it;
- tests whose only protected contract is exact reconstruction of an obsolete unfinished draft, including the obsolete portion of `NightTransactionRestoreCompositionTest`;
- legacy TB provenance-decoder/reconstruction plumbing after generic exact committed-setup persistence owns recovery;
- migration/scaffolding whose sole purpose is indefinite cross-version continuation of unfinished games.

Do **not** retire a field/test merely because it contains `draft`, `restore`, `recovery` or `reconstruct` in its name. First prove it is not also required for confirmed-vs-draft separation, transaction safety, or committed effective-state calculation during normal play.

## 7. Setup ownership boundary after this decision

Generic setup must terminate at an immutable committed domain fact:

```text
script + playerCount + seed + diversity history
-> resolve setup policy/provider
-> template candidates OR legal generated candidates
-> common deterministic diversity selector
-> shown-identity commitment
-> CommittedClocktowerSetup
```

`CommittedClocktowerSetup` is persistence-independent. The setup engine must not know whether the App supports restart recovery.

An outer persistence/recovery adapter may serialize the exact committed setup:

```text
CommittedClocktowerSetup
+ committed game facts
-> stable checkpoint storage
-> process death
-> exact stable checkpoint load
-> continue from the next safe domain/action boundary
```

Recovery must never call the setup candidate source, selector, shown-identity chooser or recommendation system to recreate an already committed setup.

## 8. Host/App ownership result relevant to MS-SETUP

`ClocktowerJudgeScreen` receives already prepared `cards`, `script`, `gameSeed`, `ClocktowerNightCheckpoint` and callback/state inputs. Its public orchestration boundary does not own initial setup candidate generation/materialization.

Therefore MS-SETUP must not add setup policy/randomization responsibility to Host. Host remains an orchestration/presentation owner under the existing growth freeze.

App currently owns legacy start/setup/persistence wiring. The MS-SETUP migration should reduce script-specific start ownership in App rather than add another `if (script == ...)` setup path.

## 9. Effect on accepted TBSP invariants

Previously accepted TB behavior remains protected during migration:

- committed TB actual/shown roles cannot reroll;
- recommendation/background work cannot replace shown identity;
- true-completion history records the original committed setup;
- an active restore must never select a new setup.

However the future generic contract is intentionally stronger and simpler:

> restore the exact persisted `CommittedClocktowerSetup`; do not derive the committed setup from provenance/template data.

Existing TB-specific provenance restore is therefore transitional compatibility code, not the target generic architecture.

## 10. Effect on testing strategy

Follow risk-based testing from `AGENTS.md` / `docs/TESTING_STRATEGY.md`.

Do not create source-string RED tests merely to prove that draft persistence fields disappear.

Durable evidence should instead prove:

```text
committed setup survives stable-checkpoint round trip exactly
actual roles unchanged
shown identities unchanged
seat identities unchanged
committed action facts unchanged
committed history sequence unchanged
restore does not reselect/recommend/reroll setup
an uncommitted interaction may restart from its stable boundary
domain transaction retry remains exactly-once/convergent
```

When obsolete exact-draft-resume tests are retired, record what they used to protect and verify that the surviving stable-checkpoint / transaction tests cover every still-required invariant, per the test-retirement policy in `AGENTS.md`.

## 11. Revised MS-SETUP slices

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                         COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                       COMPLETE (docs checkpoint)
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance
MS-S2   generic SetupCandidate + source contract + setup policy/provider registry
MS-S3   optional TemplateRepository keyed by script + player count
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource
MS-S5   common deterministic SetupDiversityHistory / scorer / selector facade
MS-S6   generic shown-identity commitment policy
MS-S6R  stable-domain-checkpoint persistence/recovery adapter and legacy-retirement gate
MS-S7   TB 480-preset adapter/parity; exact committed setup becomes restore authority
MS-S8   NGJ/no-template adapter/parity + deterministic seeded generation evidence
MS-S9   future-script acceptance: no App-root setup branch; templates are provider/data registration only
```

`MS-S6R` is deliberately outside the setup core. Completion/diversity history also remains conceptually separate from active-game recovery.

## 12. MS-S1 minimum contract after audit

The minimum model should represent the exact committed fact without embedding storage APIs or TB-specific preset semantics. Candidate shape for implementation review:

```text
CommittedClocktowerSetup
├─ schemaVersion
├─ script / ruleset identity
├─ playerCount
├─ setupSeed
├─ exact ordered seat assignments
│  ├─ stable seat identity
│  ├─ actualRoleId
│  └─ shownRoleId
└─ provenance
   ├─ sourceKind: TEMPLATE | GENERATED
   ├─ providerId
   └─ candidateId? / source identity as appropriate
```

Invariants:

- exact committed setup is sufficient to describe the initial identities;
- shown identity is fixed at commit;
- provenance is not a reconstruction recipe;
- TB-only `presetStyle`, minion-set weighting and rotation metadata do not become mandatory generic fields;
- persistence is an outer consumer, not a dependency of this model.

## 13. Next implementation order

The next production slice is **MS-S1 only** after re-checking live branch/head.

MS-S1 should add the smallest durable typed model/invariants with focused typed evidence where there is a real invariant. Do not manufacture a source-string RED for a pure data/model introduction. Do not migrate App/Host/TB/NGJ persistence in MS-S1.

No code was changed by this audit checkpoint.