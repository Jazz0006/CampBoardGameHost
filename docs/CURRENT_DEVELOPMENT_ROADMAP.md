# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
live main at MS-SETUP campaign start:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated TBSP code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

PR #57 — TBSP: integrate Trouble Brewing setup presets
MERGED / CLOSED

current branch:
codex/ms-setup-generic-architecture

current Draft PR:
#61 — MS-SETUP: generic multi-script setup architecture
DRAFT / OPEN

MS-S1 validated code/test head:
f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b

MS-S1R accepted code/test head:
2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e

MS-S1R validation:
CI #1220 / run 33356512627 SUCCESS
Android full unit tests + debug APK SUCCESS
Real Clingo cross-validation SUCCESS
ASP contract tests SUCCESS
CI aggregate gate SUCCESS
R2 #1137 / run 33356512640 SUCCESS
```

Later docs-only commits are carriers and do not replace the accepted code/test checkpoint above.

Current campaign status:

```text
MS-S0    ownership audit                         COMPLETE
MS-S0.5  recovery scope reduction audit          COMPLETE
MS-S1    CommittedClocktowerSetup + provenance   COMPLETE / ACCEPTED
MS-S1R   setup persistence authority migration   COMPLETE / ACCEPTED
MS-S2    candidate/source/provider contracts     NEXT
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

Accepted checkpoints:

- `docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md`
- `docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md`

Recovery decision/audit:

`docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md`

## 2. Closed predecessor — TBSP

Trouble Brewing Setup Presets is complete and merged.

Important accepted checkpoints:

```text
TBSP production checkpoint:
4c8108c91be188d33435233efb9aba26397f6b87

final T4 trigger:
45a60a3c32c7471c68d89b7fb886c4dbb00f1781

merged / fully validated code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6
```

Preserve the frozen 480-preset dataset, deterministic preset/deal semantics, selector-owned Drunk shown identity, true-completion rotation history, non-blocking reveal/First Night precompute, and accepted durability behavior until each mechanism is deliberately migrated with parity evidence.

Detailed predecessor authority remains in:

- `docs/TBSP_6K_FINAL_ACCEPTANCE_CHECKPOINT_2026-08-31.md`;
- `docs/TBSP_6L_PROVENANCE_DURABILITY_REPAIR_2026-08-31.md`;
- `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`;
- `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`.

## 3. MS-SETUP target architecture

Build one script-neutral setup pipeline:

```text
script + playerCount + seed + diversity history
-> resolve script/ruleset setup policy/provider
-> query optional template candidates
-> templates exist: validated template candidates
-> no templates: legal generated candidates
-> common deterministic diversity selector
-> commit shown-identity decisions
-> CommittedClocktowerSetup
```

`CommittedClocktowerSetup` is the immutable exact initial setup fact. The setup engine ends there.

Persistence/recovery is an outer consumer and must not be a dependency of candidate generation, selection, shown-identity choice, or setup commitment.

The App root must not gain new script-specific setup branches when future scripts are added.

## 4. MS-S0 ownership findings — COMPLETE

The ownership audit established:

- Trouble Brewing setup is currently owned by TB-specific preset/data/selector/rotation/materialization components plus App start wiring;
- No Greater Joy uses the legacy no-template role/distribution generation path;
- NGJ generation currently uses unseeded random/shuffle behavior before the game seed is established;
- `ClocktowerJudgeScreen` / Host consumes already prepared cards/seed/checkpoint and does not own initial setup generation;
- generic setup policy/randomization must not move into Host;
- TB-specific rotation style/minion-set/preset metadata must not become mandatory generic-core state.

NGJ migration should preserve legality and user-visible setup semantics while introducing deterministic seeded generation; exact legacy random sequence parity is not a requirement.

## 5. Recovery product boundary — COMPLETE DECISION

The product no longer promises arbitrary exact continuation of an unfinished game after exit/restart.

Supported goal:

```text
best-effort crash / Android process-death recovery
-> latest supported stable committed domain checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart from next safe domain/action boundary
```

Explicit non-goals:

- exact arbitrary mid-UI / mid-interaction continuation;
- durable persistence of temporary draft selections solely for UI reconstruction;
- “play half today and continue tomorrow” as a supported product contract;
- indefinite compatibility for every historical unfinished-save format.

Still protected:

- committed setup identities;
- committed action/observation history;
- stable timeline identity;
- Dawn/Dusk and other transaction retry/idempotency/convergence guarantees;
- completed-game setup/diversity history.

## 6. MS-S1R setup persistence migration — COMPLETE / ACCEPTED

MS-S1R changed setup recovery from:

```text
OLD
TB provenance/source metadata
-> reload current template data
-> reconstruct setup selection
```

to:

```text
NEW
exact CommittedClocktowerSetup
-> persist exact setup
-> restore exact setup

TB compact completion/diversity record
-> persist committed rotation-relevant summary
-> restore summary directly

provenance
-> audit/source metadata only
```

Accepted properties:

- active save persists exact script, setup seed, ordered seats, actual roles, shown roles and generic provenance;
- current Trouble Brewing restore requires and directly decodes exact committed setup;
- restore does not reload `trouble_brewing_setup_presets_v2_final.json`;
- restore does not reconstruct `TroubleBrewingSetupPresetSelection`;
- completion/diversity history consumes a compact committed rotation record;
- exact setup and compact record identities are cross-validated on restore;
- old `TroubleBrewingSetupProvenancePersistence` authority and its typed legacy contract were retired only after call-site proof;
- the production wiring guard explicitly forbids reintroduction of the legacy reconstruction path.

Authoritative checkpoint:

`docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md`

MS-S1R did not perform broad night/draft recovery cleanup.

## 7. REC-R1 — separate future unfinished-game recovery simplification

REC-R1 remains a separate future campaign outside MS-SETUP.

It may later simplify/retire exact-resume behavior involving:

- `ClocktowerNightCheckpoint` draft-heavy persisted state;
- exact `nightStepIndex` continuation;
- attack/poison/Monk/Mayor/succession draft persistence;
- `NightTransactionRestoreComposition` exact unfinished-interaction reconstruction;
- tests whose only contract is obsolete exact mid-interaction resume.

REC-R1 must preserve anything still needed for runtime confirmed-vs-draft separation or committed transaction correctness.

## 8. Implementation campaign

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                         COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                       COMPLETE
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance   COMPLETE / ACCEPTED
MS-S1R  exact setup persistence authority migration + TB setup-restore retirement COMPLETE / ACCEPTED
MS-S2   generic SetupCandidate + source contract + setup policy/provider registry NEXT
MS-S3   optional TemplateRepository keyed by script + player count
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource
MS-S5   common deterministic SetupDiversityHistory / scorer / selector facade
MS-S6   generic shown-identity commitment policy
MS-S7   adapt TB 480-preset pipeline; preserve TB behavior/parity
MS-S8   adapt NGJ/no-template path; legality parity + deterministic seeded evidence
MS-S9   acceptance: future no-template script needs no App-root branch; templates are provider/data registration only

REC-R1  separate future unfinished-game stable-checkpoint simplification
```

Do not implement several slices at once merely because they share a campaign.

## 9. MS-S1 — COMPLETE / ACCEPTED

Accepted production model:

`app/src/main/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetup.kt`

Accepted typed test:

`app/src/test/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetupTest.kt`

Accepted shape:

```text
CommittedClocktowerSetup
├─ script: ScriptId
├─ setupSeed: Long
├─ assignments: ordered List<CommittedSetupSeat>
│  ├─ seat: Int
│  ├─ actualRole: RoleId
│  └─ shownRole: RoleId
└─ provenance: SetupProvenance
   ├─ sourceKind: TEMPLATE | GENERATED
   ├─ providerId: String
   └─ candidateId: String?
```

Accepted invariants:

- assignments are non-empty;
- exact ordered seats are canonical `1..N`;
- actual and shown identities are explicit committed `RoleId`s;
- provider ID is non-blank;
- optional candidate ID is non-blank when present;
- caller-owned mutable assignment lists cannot mutate the committed setup after construction;
- equivalent committed facts have structural equality/hash identity;
- provenance is metadata, not reconstruction authority;
- no TB-only style/minion-set/rotation fields are mandatory generic state;
- no Android storage/session/UI dependencies.

Persistence schema/version remains outside the domain model. `playerCount` is derived from assignments.

## 10. MS-S2 — NEXT

MS-S2 establishes only the generic candidate/source/provider ownership contracts needed by later slices.

Target concepts:

```text
SetupCandidate
SetupCandidateSource
ClocktowerSetupPolicy / provider contract
ClocktowerSetupProviderRegistry
```

Before production writes:

1. re-query live `main`, branch, Draft PR #61 and checks;
2. audit existing TB preset candidate shape, selector inputs, source/provider identity, and NGJ script/distribution entry points;
3. identify which fields are genuinely generic candidate facts versus TB-only scoring/template metadata;
4. define a persistence-independent source query boundary using existing `ScriptId` / `RoleId` where appropriate;
5. establish registry semantics for script -> setup provider/policy without adding App-root script branches;
6. prove duplicate/unregistered provider behavior explicitly;
7. stop before implementing template repositories, generated setup algorithms, common diversity scoring, shown-identity commitment, or production TB/NGJ cutover.

MS-S2 should prefer small pure Kotlin contracts and typed tests. Do not manufacture source-string RED when typed construction/lookup evidence is practical.

## 11. Protected predecessor correctness

Preserve during migration:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid TB template data never silently falls back to broad random TB setup.
Background recommendation/First Night work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup summary.
```

Also preserve:

- Dawn poison exactly-once / retry convergence;
- Dusk/next-night poison expiry exactly-once / retry convergence;
- Fortune Teller current/effective-state authority;
- poisoned Spy fail-safe semantics;
- current living-Demon UI authority;
- No Greater Joy setup legality/current behavior until explicit migration parity proof.

## 12. Testing cadence

Follow:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Use risk-based evidence:

- durable new/changed contracts get the smallest valuable typed test;
- internal refactors do not require manufactured RED tests;
- T0 is the smallest directly relevant evidence;
- `:app:testFast` is a logical-checkpoint T1 gate, not mandatory after every micro-edit;
- persistence/schema changes require persistence-specific evidence and may justify earlier escalation;
- T4 remains an explicit full-acceptance/merge-level checkpoint rather than a micro-slice default;
- local/focused evidence does not replace required GitHub CI/R2 at an applicable checkpoint.

MS-S1R is accepted at `2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e` with CI #1220 and R2 #1137. Later docs-only commits do not replace that validated code/test checkpoint.

## 13. Writer / large-file workflow

For safe small/medium tests/docs/source, use the GitHub connector directly.

For large/truncated source with stable unique anchors, use the GitHub Actions one-shot workflow + separate Python patch script described in:

`docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`

Use Codex/Luna only when that one-shot path cannot be made safe or a complete local worktree is genuinely required.

Do not broaden App/Host decomposition merely because a future cutover touches a large file.

## 14. Current documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md
docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md
docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Predecessor TBSP docs remain historical/normative where accepted TB behavior is being migrated.

## 15. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read the active MS-SETUP handoff;
4. read MS-S1/MS-S1R checkpoints and recovery-scope audit as relevant;
5. re-query live `main`, `codex/ms-setup-generic-architecture`, Draft PR #61 and current checks;
6. treat `98ee982ef3590822cd06ac72a047b49afac3cfd6` as the fully validated merged TBSP code checkpoint unless live audit changes that fact;
7. treat `2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e` as the accepted MS-S1R code/test checkpoint unless later production commits deliberately supersede it;
8. next production slice is MS-S2;
9. keep MS-S2 persistence-independent and stop before MS-S3+ implementation;
10. do not perform broad unfinished-night cleanup inside MS-SETUP; REC-R1 owns that later work;
11. do not resume A3/A4/ZDD/Mayor/Imp/App-Host decomposition unless priority explicitly changes;
12. keep PR #61 Draft and do not merge, mark Ready, force-push or rebase without explicit user authorization.

## 16. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — MS-S2 NEXT |
| MS-S1R setup persistence authority migration | COMPLETE / ACCEPTED |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| GCR-5 reconstructor naming clarity | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| A3 immutable setup snapshot ownership/persistence | SUPERSEDED BY MS-S1/MS-S1R |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1; no broad App decomposition |
