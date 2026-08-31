# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; commit/PR values below are checkpoints, not substitutes for live state.

## 1. Current live development context

```text
live main at MS-S0/MS-S0.5 audit:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated TBSP code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

PR #57 — TBSP: integrate Trouble Brewing setup presets
MERGED / CLOSED

post-merge validation:
CI #1179 / run 33346311357 SUCCESS
Android :app:testFull + :app:assembleDebug SUCCESS
ASP contract tests SUCCESS
Real Clingo cross-validation SUCCESS
CI aggregate gate SUCCESS

current branch:
codex/ms-setup-generic-architecture
base: eed51bade5163790316a31e8295e2e841df90357

current campaign:
MS-SETUP generic multi-script setup architecture

status:
MS-S0   ownership audit                         COMPLETE
MS-S0.5 recovery scope reduction audit          COMPLETE
MS-S1   CommittedClocktowerSetup + provenance   NEXT
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

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

Preserve the frozen 480-preset dataset, deterministic preset/deal semantics, selector-owned Drunk shown identity, true-completion rotation history, non-blocking reveal/First Night precompute, and the accepted 6L durability behavior until each mechanism is deliberately migrated with parity evidence.

Detailed predecessor authority remains in:

- `docs/TBSP_6K_FINAL_ACCEPTANCE_CHECKPOINT_2026-08-31.md`;
- `docs/TBSP_6L_PROVENANCE_DURABILITY_REPAIR_2026-08-31.md`;
- `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`;
- `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md`.

## 3. Current MS-SETUP goal

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

Current ownership audit found:

- Trouble Brewing setup is currently owned by TB-specific preset/data/selector/rotation/materialization components plus App start wiring;
- No Greater Joy uses the legacy no-template role/distribution generation path;
- NGJ generation currently uses unseeded random/shuffle behavior before the game seed is established;
- `ClocktowerJudgeScreen` / Host consumes already prepared cards/seed/checkpoint and does not own initial setup generation;
- generic setup policy/randomization therefore must not move into Host;
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

## 6. Recovery work split — CURRENT PLAN

Do not perform broad legacy cleanup before replacement authority exists.

### MS-S1R — setup persistence authority migration

MS-S1R runs immediately after MS-S1.

It changes setup recovery from:

```text
TB provenance/source metadata
-> reload current template data
-> reconstruct setup
```

to:

```text
exact CommittedClocktowerSetup
-> persist exact setup
-> restore exact setup

provenance
-> audit/source metadata only
```

MS-S1R may retire superseded TB setup-reconstruction code after typed restore evidence and call-site proof.

It must not expand into general unfinished-night cleanup.

### REC-R1 — unfinished-game recovery simplification

REC-R1 is a separate future campaign outside MS-SETUP.

It may later simplify/retire exact-resume behavior involving:

- `ClocktowerNightCheckpoint` draft-heavy persisted state;
- exact `nightStepIndex` continuation;
- attack/poison/Monk/Mayor/succession draft persistence;
- `NightTransactionRestoreComposition` exact unfinished-interaction reconstruction;
- tests whose only contract is obsolete exact mid-interaction resume.

REC-R1 must preserve anything still needed for runtime confirmed-vs-draft separation or committed transaction correctness.

## 7. Current implementation campaign

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                         COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                       COMPLETE
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance   NEXT
MS-S1R  exact setup persistence authority migration + TB setup-restore retirement
MS-S2   generic SetupCandidate + source contract + setup policy/provider registry
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

## 8. MS-S1 exact scope

MS-S1 introduces only a pure domain model using existing `ScriptId` / `RoleId` types.

Target shape:

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

MS-S1 deliberately excludes persistence schema/version. Storage schema belongs to MS-S1R.

`playerCount` should remain derived from assignments unless a later durable contract proves duplication is necessary.

MS-S1 invariants:

- assignments are non-empty;
- exact ordered seats are canonical `1..N`;
- actual and shown identities are explicit committed `RoleId`s;
- provider ID is non-blank;
- optional candidate ID is non-blank when present;
- provenance is metadata, not reconstruction authority;
- no TB-only style/minion-set/rotation fields become mandatory generic state;
- no Android storage/session/UI dependencies.

No App/Host/TB/NGJ/persistence wiring belongs in MS-S1.

## 9. MS-S1 evidence

Use root `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

Because MS-S1 introduces durable validation invariants, use a small typed unit test. Do not add source-string behavior tests.

Useful evidence:

- valid setup preserves exact actual/shown identities and provenance;
- empty assignments rejected;
- duplicate/non-canonical/out-of-order seats rejected;
- blank provider rejected;
- blank candidate ID rejected when present.

Persistence round-trip belongs to MS-S1R. Seeded generation belongs to MS-S4. NGJ parity belongs to MS-S8. Future-script/App-branch acceptance belongs to MS-S9.

## 10. Protected predecessor correctness

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
Completion persistence is retry-safe and records the original committed setup.
```

Also preserve:

- Dawn poison exactly-once / retry convergence;
- Dusk/next-night poison expiry exactly-once / retry convergence;
- Fortune Teller current/effective-state authority;
- poisoned Spy fail-safe semantics;
- current living-Demon UI authority;
- No Greater Joy setup legality/current behavior until explicit migration parity proof.

## 11. Testing cadence

Follow:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Use risk-based evidence:

- durable new/changed contracts get the smallest valuable typed test;
- internal refactors do not require manufactured RED tests;
- T0 is the smallest directly relevant evidence;
- `:app:testFast` is a logical-checkpoint T1 gate, not a mandatory step after every micro-edit;
- persistence/schema changes in MS-S1R justify persistence-specific evidence and may justify earlier escalation;
- T4 remains an explicit full-acceptance/merge-level checkpoint rather than a micro-slice default;
- local/focused evidence does not replace required GitHub CI/R2 at an applicable checkpoint.

## 12. Writer / large-file workflow

For safe small/medium tests/docs/source, use the GitHub connector directly.

For large/truncated source with stable unique anchors, use the GitHub Actions one-shot workflow + separate Python patch script described in:

`docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`

Use Codex/Luna only when that one-shot path cannot be made safe or a complete local worktree is genuinely required.

Do not broaden App/Host decomposition merely because a future cutover touches a large file.

## 13. Current documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

Predecessor TBSP docs remain historical/normative where their accepted TB behavior is being migrated.

## 14. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap;
3. read the active MS-SETUP handoff;
4. read the recovery-scope audit when setup persistence/recovery is relevant;
5. re-query live `main`, `codex/ms-setup-generic-architecture`, relevant PR/checks;
6. treat `98ee982ef3590822cd06ac72a047b49afac3cfd6` as the fully validated merged TBSP code checkpoint unless live audit changes that fact;
7. do not reopen accepted TBSP behavior without a concrete regression or explicit migration slice;
8. current production slice is MS-S1 only;
9. after MS-S1 acceptance, MS-S1R migrates exact setup persistence authority before MS-S2;
10. do not perform broad unfinished-night cleanup inside MS-S1R;
11. REC-R1 owns later exact-resume simplification;
12. do not resume A3/A4/ZDD/Mayor/Imp/App-Host decomposition unless priority explicitly changes;
13. do not merge, mark Ready, force-push, rebase or broaden a PR without explicit user authorization.

## 15. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — MS-S1 NEXT |
| MS-S1R setup persistence authority migration | NEXT AFTER MS-S1 |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| GCR-5 reconstructor naming clarity | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| A3 immutable setup snapshot ownership/persistence | DEFERRED / NOT CURRENT |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1; no broad App decomposition |
