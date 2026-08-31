# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Status: **MS-S0 / MS-S0.5 COMPLETE — MS-S1 NEXT**

## 1. Live baseline

Fresh audit baseline:

```text
live main:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated TBSP code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

PR #57:
MERGED / CLOSED

post-merge validation:
CI #1179 / run 33346311357 — SUCCESS
Android :app:testFull + :app:assembleDebug — SUCCESS
ASP contract tests — SUCCESS
Real Clingo cross-validation — SUCCESS
CI aggregate gate — SUCCESS
```

Planning / implementation branch:

```text
codex/ms-setup-generic-architecture
base: eed51bade5163790316a31e8295e2e841df90357
```

Always re-query live GitHub state before a write or checkpoint decision.

## 2. Product goal

Build one script-neutral Clocktower setup pipeline:

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

The setup engine ends at `CommittedClocktowerSetup`.

Persistence/recovery is an outer consumer. Setup generation must not depend on Android storage, active-session restore, Host UI state, or an unfinished-game recovery mechanism.

The App root must not gain a new `if (script == ...)` setup branch when a future script is added.

## 3. MS-S0 ownership audit — COMPLETE

Current production ownership is approximately:

```text
App start wiring
├─ Trouble Brewing
│  ├─ frozen 480-preset dataset + validator
│  ├─ TB preset selector
│  ├─ TB rotation history/scorer
│  ├─ selector-owned Drunk shown-role choice
│  ├─ TB deterministic deal materializer
│  └─ committed PlayerCards + TB-specific provenance
└─ No Greater Joy / no-template
   ├─ script role definitions + player-count distribution
   ├─ legacy broad/random composition path
   ├─ legacy Drunk shown-role handling
   └─ committed PlayerCards

prepared cards + seed
-> ClocktowerJudgeScreen / Host orchestration
-> session/domain transactions
```

`ClocktowerJudgeScreen` consumes already prepared `cards`, `script`, `gameSeed`, `ClocktowerNightCheckpoint` and callbacks. It does not own initial setup generation/materialization. New setup policy/randomization must not move into Host.

NGJ's current generation uses unseeded `shuffled()` / random selection before the game seed is established. MS-S4/MS-S8 must introduce deterministic seeded generation while preserving legality and user-visible semantics; exact legacy random sequence parity is not required or meaningful.

## 4. MS-S0.5 recovery scope decision — COMPLETE

Detailed audit:

`docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md`

The product no longer promises arbitrary exact continuation of an unfinished game after exit/restart.

Supported recovery goal:

```text
best-effort crash / Android process-death recovery
-> latest supported stable committed domain checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart at the next safe domain/action boundary
```

Explicit non-goals:

- “play half today and continue tomorrow” as a product contract;
- exact restoration to an arbitrary in-progress UI interaction;
- durable preservation of temporary/draft selections solely to recreate a partially completed screen;
- indefinite cross-version compatibility for unfinished-game saves.

Committed-domain retry/idempotency/convergence remains protected. Completed-game setup/diversity history remains separately durable.

## 5. Recovery work split — UPDATED PLAN

Do **not** perform broad recovery cleanup before a replacement authority exists.

The correct order is:

```text
introduce replacement authority
-> cut consumers over
-> prove parity / call-site ownership
-> retire superseded legacy path
```

Recovery work is split into two independent areas.

### 5.1 MS-S1R — setup persistence authority migration

This belongs immediately after MS-S1 because it is directly coupled to setup ownership.

Target:

```text
OLD
TB provenance / source metadata
-> reload current TB template data
-> reconstruct setup selection

NEW
exact CommittedClocktowerSetup
-> persist as authoritative setup fact
-> restore exact CommittedClocktowerSetup

provenance
-> source/audit metadata only
```

MS-S1R may retire TB setup-reconstruction plumbing only after exact generic setup persistence and restore are proven.

MS-S1R must **not** expand into general unfinished-night recovery cleanup.

### 5.2 REC-R1 — unfinished-game recovery simplification

This is a separate future campaign, outside MS-SETUP.

It will re-evaluate:

- `ClocktowerNightCheckpoint` draft-heavy persistence;
- exact `nightStepIndex` continuation;
- attack / poison / Monk / Mayor / succession draft persistence;
- `NightTransactionRestoreComposition` exact unfinished-interaction semantics;
- tests whose only remaining contract is exact mid-interaction resume.

REC-R1 must retain any state or recovery mechanism needed for confirmed-vs-draft runtime separation or committed transaction correctness.

Do not pull Mayor/Imp/Monk/Attack-Protect redesign into MS-SETUP merely because those fields appear in recovery code.

## 6. Revised implementation campaign

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

These are slice boundaries, not permission to implement all slices at once.

## 7. MS-S1 minimum domain contract

MS-S1 introduces only the exact committed setup fact and generic origin metadata.

Use existing domain types `ScriptId` and `RoleId`.

Preferred minimum shape:

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

Important refinement from earlier planning: **do not put persistence `schemaVersion` into this pure domain model.** Schema/version belongs to the MS-S1R codec/checkpoint layer.

`playerCount` is derivable from the exact assignment list and therefore does not need to be duplicated unless implementation evidence shows a durable need.

## 8. MS-S1 durable invariants

The model should enforce only stable facts that future implementations should still obey:

- at least one assignment exists;
- seat identities are exact and canonical: ordered seats are `1..N`;
- every seat has an actual role and an explicit committed shown role;
- `providerId` is non-blank;
- optional `candidateId`, when present, is non-blank;
- provenance never substitutes for the exact assignments;
- no TB-only preset style/minion-set/rotation fields are mandatory generic state;
- no Android persistence/session/UI API is imported.

Do not add a serializer, repository, App/Host wiring, candidate generation, recommendation call, or TB/NGJ adapter in MS-S1.

## 9. MS-S1 evidence strategy

This slice introduces durable domain validation invariants, so the preferred evidence is a small typed unit test, not a source-string guard.

Useful assertions:

- a valid committed setup retains exact actual/shown identities and generic provenance;
- empty assignments are rejected;
- duplicate/non-canonical/out-of-order seats are rejected;
- blank provider identity is rejected;
- blank candidate identity is rejected when present.

Do not create tests for persistence round-trip yet. That belongs to MS-S1R.

Do not test seeded generation yet. That belongs to MS-S4.

Do not test NGJ parity yet. That belongs to MS-S8.

Do not create an App-source-string test merely to prove MS-S1 has no App branch.

## 10. MS-S1R acceptance boundary

Only after MS-S1 is accepted:

1. audit the exact active-game TB setup persistence/restore call chain again;
2. introduce a small generic codec/checkpoint representation for exact `CommittedClocktowerSetup`;
3. persist exact actual/shown identities + seat order + script + setup seed + provenance;
4. restore those facts directly without invoking template loading, selector, Drunk chooser, recommendation or randomization;
5. preserve supported compatibility behavior deliberately rather than indefinitely;
6. prove TB setup restore exactness with typed persistence/restore tests;
7. prove the setup selector/preparer is not invoked by restore;
8. retire only the old TB reconstruction code/test assertions that are fully superseded.

MS-S1R must stop before general night/draft recovery cleanup.

## 11. Protected predecessor invariants

Preserve throughout MS-SETUP:

```text
TB actual roles originate from the selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Same accepted TB inputs + seed reproduce the same committed setup.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random TB setup.
Identity reveal does not synchronously block on recommendation/First Night computation.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup.
```

Also preserve committed-domain correctness unrelated to arbitrary resume:

- Dawn poison exactly-once / retry convergence;
- Dusk/next-night poison expiry exactly-once / retry convergence;
- Fortune Teller current/effective-state authority;
- poisoned Spy fail-safe semantics;
- current living-Demon UI authority;
- NGJ setup legality/current behavior until explicit migration parity proof.

## 12. Development workflow

Follow:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
- `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` when a large/truncated source edit becomes necessary.

Use risk-based evidence, not RED ceremony. A meaningful new durable invariant should have typed test evidence; refactors do not need manufactured RED tests.

For large files:

```text
safe small/medium file
-> GitHub connector direct edit

large/truncated file + stable unique anchors
-> GitHub Actions one-shot workflow + separate Python patch script

one-shot cannot be made safe / complete local worktree genuinely required
-> Codex/Luna
```

## 13. Immediate next action

Proceed with **MS-S1 only**:

1. re-confirm branch head;
2. add the smallest typed domain test for the new committed-setup invariants;
3. implement the pure domain model;
4. run/obtain the smallest directly relevant evidence available plus exact diff audit;
5. stop at the MS-S1 checkpoint and assess before MS-S1R production changes.

No App/Host/TB/NGJ/persistence wiring belongs in MS-S1.

## 14. Explicit non-goals for MS-S1

Do not broaden into:

- arbitrary unfinished-game recovery cleanup;
- Mayor redirect redesign;
- Imp succession redesign;
- Monk/Attack-Protect replay;
- A3/A4/ZDD;
- App/Host decomposition for its own sake;
- regeneration/reformatting of the frozen TB preset dataset;
- generic candidate generation/selection;
- active-game persistence cutover;
- PR merge/Ready state changes.

## 15. Stop conditions

Stop and re-audit if:

- live branch moved unexpectedly;
- the pure committed setup cannot represent exact initial actual/shown identities without TB-specific mandatory fields;
- persistence/recovery must become an input dependency of setup generation;
- MS-S1 would require an App/Host cutover;
- accepted TB or NGJ behavior must change merely to fit the abstraction;
- a proposed cleanup would remove committed-state retry/idempotency/convergence rather than obsolete exact-resume behavior.
