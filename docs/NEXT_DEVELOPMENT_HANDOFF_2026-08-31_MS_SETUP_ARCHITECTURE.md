# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **CURRENT ACTIVE HANDOFF / MS-S0 + MS-S0.5 COMPLETE / MS-S1 NEXT**

## 1. Live baseline and planning branch

Fresh live-state audit before this checkpoint:

```text
live main:
eed51bade5163790316a31e8295e2e841df90357

merged PR:
#57 — TBSP: integrate Trouble Brewing setup presets
MERGED

validated merged code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

post-merge full CI:
CI #1179 / run 33346311357 — SUCCESS
Android :app:testFull + :app:assembleDebug — SUCCESS
ASP contract tests — SUCCESS
Real Clingo cross-validation — SUCCESS
CI aggregate gate — SUCCESS
```

Current planning branch:

```text
codex/ms-setup-generic-architecture
base: eed51bade5163790316a31e8295e2e841df90357
```

Always re-query live GitHub state before production implementation. These values are provenance, not a substitute for live state.

## 2. Closed predecessor campaign

Trouble Brewing Setup Presets is complete and merged.

Accepted TBSP checkpoints:

```text
production checkpoint:
4c8108c91be188d33435233efb9aba26397f6b87

final T4 trigger:
45a60a3c32c7471c68d89b7fb886c4dbb00f1781

merge / fully validated code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6
```

TBSP-1 through TBSP-6L are accepted. Preserve the frozen TB dataset, deterministic preset/deal semantics, selector-owned Drunk shown identity, true-completion rotation history, non-blocking reveal/First Night precompute and the 6L durability invariant while genericization proceeds.

The old TB-specific provenance restore path is now transitional compatibility code. The target generic architecture restores the exact committed setup rather than reconstructing it from provenance/template data.

No Greater Joy remains protected behavior until genericization explicitly proves parity.

## 3. MS-S0 ownership audit result

MS-S0 found no whole-plan blocker.

Current authority graph is approximately:

```text
App start wiring
├─ Trouble Brewing
│  ├─ frozen 480-preset data / validator
│  ├─ TB preset selector + TB diversity history/scorer
│  ├─ selector-owned Drunk shown-role choice
│  ├─ TB deal materializer
│  └─ committed PlayerCards + TB-specific provenance
└─ No Greater Joy / no-template
   ├─ role definitions + player-count distribution
   ├─ legacy broad/random legal composition path
   ├─ legacy Drunk shown-role handling
   └─ committed PlayerCards

prepared cards + game seed + checkpoint
-> ClocktowerJudgeScreen / Host orchestration
-> session/domain transactions
```

`ClocktowerJudgeScreen` consumes already prepared `cards`, `script`, `gameSeed`, `ClocktowerNightCheckpoint` and callbacks. It does not own initial setup candidate generation/materialization. New setup policy/randomization must therefore not move into Host.

NGJ's legacy generation uses unseeded `shuffled()`/random selection before the game seed is established, so MS-S4/MS-S8 must add a deterministic seeded generation contract while preserving legality and user-visible setup semantics rather than pretending the old random sequence can be byte-for-byte reproduced.

## 4. Generic setup target

Build a **generic Clocktower setup architecture** where every script can use the same setup pipeline regardless of whether curated setup templates exist.

Target contract:

```text
script + playerCount + seed + diversity history
-> resolve script/ruleset setup policy/provider
-> query optional template candidate source
-> if templates exist: build validated template candidates
-> otherwise: build legal generated candidates
-> common deterministic diversity selector
-> commit shown-identity decisions
-> CommittedClocktowerSetup
```

The setup engine ends at `CommittedClocktowerSetup`. Persistence/recovery is an outer consumer and must not be a dependency of setup generation.

The App root must not grow new `if (script == ...)` setup branches when future scripts are added.

## 5. Product decision — recovery scope reduced

The product no longer promises arbitrary exact continuation of an unfinished game after exit/restart.

Supported recovery goal:

```text
best-effort crash / Android process-death recovery
-> load latest supported stable committed domain checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart from the next safe domain/action boundary
```

Explicit non-goals:

- long-term “play half today, continue tomorrow” continuation;
- exact restoration to an arbitrary in-progress UI interaction;
- durable persistence of temporary/draft selections solely to recreate a partially completed screen;
- indefinite cross-version compatibility for unfinished-game saves.

Transient UI state and uncommitted decisions may be discarded/restarted. An explicitly incompatible unfinished save may be rejected/discarded by schema/version policy.

Completed-game setup/diversity history remains separate durable product data.

Detailed audit authority:

`docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md`

## 6. Recovery audit findings

The complexity concern is confirmed by live source/tests, but not every recovery-named component is obsolete.

`ClocktowerNightCheckpoint` currently describes a persisted continuation point for an unfinished night and stores both confirmed and draft/in-progress values, including `nightStepIndex`, attack/poison/Monk/Mayor drafts and succession drafts.

`NightTransactionRestoreComposition` explicitly reconstructs an unfinished night. `NightTransactionRestoreCompositionTest` explicitly protects “persisted unfinished-night state” restore. That exact draft-resume contract may be retired after a stable-checkpoint replacement exists.

In contrast, `NightDawnRestoreRetryConvergenceAcceptanceTest` protects exactly-once/retry convergence to the same durable Dawn state. Dusk/poison recovery authorities similarly protect committed-domain correctness. Those are not removed merely because arbitrary resume is no longer a product goal.

Required classification:

```text
KEEP
- committed setup / actual + shown identities / seat identity
- committed action and observation history
- stable domain checkpoint concept
- transaction idempotency / retry / convergence
- completed-game setup/diversity history

SIMPLIFY / REPLACE
- checkpoint payload that mixes stable facts with exact draft-resume state
- unfinished-night restore composition where it promises exact draft reconstruction
- TB provenance-based setup reconstruction
- unfinished-save compatibility beyond an explicit supported schema boundary

RETIRE AFTER REPLACEMENT + CALL-SITE PROOF
- draft persistence used only for exact mid-interaction continuation
- obsolete exact-draft-resume test assertions
- legacy TB provenance decoder/reconstruction once exact generic committed setup owns restore
```

Do not mechanically delete any `RecoveryAuthority`, reducer, reconstructor or `draft` field without proving it is not needed for normal confirmed-vs-draft separation or transaction correctness.

## 7. Required generic setup semantics

- no-template is the default: a newly supported script must remain playable via legal deterministic seeded generation;
- curated templates are optional candidate sources, keyed by script/player count/version as appropriate;
- template-backed and generated candidates use the same common diversity/rotation selection layer;
- candidate identity is based on semantic role composition / shown-identity decisions, not seat reshuffling;
- generated candidates must contain no duplicate roles and must satisfy the script's legal team/setup modifiers;
- curated template candidates must be semantically validated before selection;
- Drunk-like shown identity is committed during setup generation, never lazily rerolled later;
- same seed + same candidate inputs/history produce the same setup selection;
- committed setup is an immutable exact domain fact;
- provenance records origin/audit metadata and is **not** the restore/reconstruction recipe;
- recovery restores the persisted exact committed setup and never invokes setup selection/recommendation/randomization again;
- completion history records the original committed setup, not a reconstruction;
- setup/ruleset modifiers belong to typed setup metadata/policy rather than App-root conditionals;
- adding a future script with no templates should not require setup-architecture changes;
- later adding templates for that script should require provider/data registration only, not App start rewiring.

## 8. Protected invariants

Preserve accepted TBSP behavior during migration, including:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown role is committed once and never replaced by recommendation.
Same seed + accepted TB inputs reproduce the same committed setup.
Start commits setup only once; recomposition/navigation cannot reroll it.
An active restore never selects a new setup.
Invalid template data does not silently fall back through a hidden broad-random path.
Identity reveal does not synchronously block on recommendation/First Night computation.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup.
```

Also preserve domain correctness that is independent of long-term resume:

- Dawn poison exactly-once / retry convergence;
- Dusk/next-night poison expiry exactly-once / retry convergence;
- Fortune Teller current/effective-state authority;
- poisoned Spy fail-safe semantics;
- current living-Demon UI authority;
- No Greater Joy setup legality/current behavior until explicit migration parity proof.

The old requirement “deterministic seed + provenance reconstructs exact setup” is replaced by the simpler authority rule: **persist and restore the exact committed setup itself**.

## 9. Revised implementation campaign

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                         COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                       COMPLETE (docs checkpoint)
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance
MS-S2   generic SetupCandidate + source contract + setup policy/provider registry
MS-S3   optional TemplateRepository keyed by script + player count
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource
MS-S5   common deterministic SetupDiversityHistory / scorer / selector facade
MS-S6   generic shown-identity commitment policy
MS-S6R  stable-domain-checkpoint persistence/recovery adapter + legacy retirement gate
MS-S7   adapt TB 480-preset pipeline; preserve TB parity; exact committed setup owns restore
MS-S8   adapt NGJ/no-template path; legality parity + deterministic seeded evidence
MS-S9   acceptance: no App-root setup branch for a future no-template script; templates require provider/data registration only
```

These are planning slices, not permission to implement all at once.

`MS-S6R` is deliberately outside the generic setup core. Completion/diversity history is also independent of active-game recovery.

## 10. MS-S1 minimum contract

MS-S1 should introduce the smallest persistence-independent exact committed setup model, using existing domain types where appropriate.

Target shape for implementation review:

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

MS-S1 invariants:

- the object completely describes the committed initial identities without consulting a template repository;
- shown identity is fixed at commit;
- provenance is origin/audit metadata only;
- TB-only preset style/minion-set/rotation metadata does not become mandatory generic core state;
- no Android storage/session API is imported into the setup model;
- no App/Host cutover occurs in MS-S1.

## 11. MS-S1 evidence strategy

Use root `AGENTS.md` + `docs/TESTING_STRATEGY.md` risk-based rules.

Do not manufacture a source-string RED simply because a new model is being introduced.

If MS-S1 introduces durable validation invariants, add the smallest typed tests for them. Candidate valuable assertions include:

- player count agrees with exact seat assignment count;
- stable seat identities are unique;
- every assignment has an actual role and committed shown identity according to the chosen representation;
- provenance source kind/provider identity is valid and does not substitute for exact assignment facts.

If MS-S1 remains only an immutable data-model introduction with no meaningful behavior gap, use compile/focused existing characterization evidence and exact diff audit instead of ceremonial RED.

Persistence round-trip tests belong to MS-S6R unless an intentionally small pure codec is explicitly included in MS-S1; seeded generated-candidate tests belong to MS-S4; NGJ parity belongs to MS-S8; no-App-branch acceptance belongs to MS-S9.

When exact-draft-resume tests are later retired, follow the `AGENTS.md` test-retirement rule: identify the old contract, confirm it is no longer required or superseded, and run the affected stable-checkpoint/transaction evidence.

## 12. First action next — MS-S1 only

Before production code:

1. re-query live `main` and `codex/ms-setup-generic-architecture` head;
2. re-read this handoff and the recovery audit checkpoint;
3. inspect existing setup/domain role/script types needed by the minimum model;
4. freeze exact MS-S1 field types/invariants;
5. identify the smallest typed evidence or baseline evidence required by risk;
6. implement only the generic committed model/provenance seam;
7. do not wire App, Host, TB selection, NGJ generation or active-game recovery in MS-S1;
8. exact diff audit and focused evidence before deciding whether the slice is ready for the next checkpoint.

## 13. Development workflow

Follow root `AGENTS.md` and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

For large/truncated files, especially `CampBoardGameHostApp.kt` and `ClocktowerHostScreen.kt`, use the current mandatory priority:

```text
small/medium safe file
-> GitHub connector direct edit

large/truncated file with stable unique anchors
-> GitHub Actions one-shot workflow + separate Python patch script

a one-shot patch cannot be made safe / complete local worktree genuinely required
-> Codex/Luna
```

Detailed large-file SOP:

`docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`

Use risk-based evidence, not RED ceremony. Add a new RED only for a durable new/changed behavior or uncovered invariant.

## 14. Explicit non-goals for early MS-SETUP

Do not broaden into:

- Mayor redirect behavior redesign;
- Imp succession redesign;
- A3 immutable setup snapshot campaign;
- A4/ZDD;
- Host/App decomposition for its own sake;
- end-to-end Attack/Protect replay;
- unrelated same-night correctness work;
- regeneration/reformatting of the frozen TB preset dataset;
- a broad recovery rewrite before the setup core exists;
- immediate deletion of legacy restore code before replacement/call-site proof.

## 15. Stop conditions

Stop and re-audit before implementation if:

- live `main` or the planning branch moved materially;
- the proposed `CommittedClocktowerSetup` cannot describe exact initial actual/shown identities without TB-specific fields;
- generic setup requires persistence/recovery as an input dependency;
- a proposed generic contract would require changing accepted TB behavior merely to fit the abstraction;
- NGJ legality cannot be characterized before replacing its generator;
- App-root setup branching would increase rather than decrease;
- stable-checkpoint recovery cannot preserve already committed game facts without rerunning setup/random/recommendation logic;
- recovery cleanup would remove a retry/idempotency/convergence guarantee rather than only an obsolete exact-draft-resume contract.