# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S1R COMPLETE / ACCEPTED — MS-S2 NEXT**

## 1. Live baseline and accepted checkpoints

Campaign start baseline:

```text
live main:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated TBSP code checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

PR #57:
MERGED / CLOSED
```

Current campaign branch:

`codex/ms-setup-generic-architecture`

Current campaign PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN`

Accepted MS-S1 code/test checkpoint:

`f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b`

Accepted MS-S1R code/test checkpoint:

`2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e`

Accepted MS-S1R validation:

```text
CI #1220 / run 33356512627                 SUCCESS
Android full unit tests + debug APK         SUCCESS
Real Clingo cross-validation                SUCCESS
ASP contract tests                          SUCCESS
CI aggregate gate                           SUCCESS
R2 #1137 / run 33356512640                  SUCCESS
```

Later docs-only commits are carriers and do not replace the validated code/test head.

Always re-query live GitHub state before a production write.

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

Persistence/recovery is an outer consumer. Setup generation must not depend on Android storage, active-session restore, Host UI state, or unfinished-game recovery.

The App root must not gain a new `if (script == ...)` setup branch when a future script is added.

## 3. Completed slices

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

Do not implement multiple slices at once merely because they share the campaign.

## 4. MS-S1 accepted result

Authoritative checkpoint:

`docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md`

Accepted production owner:

`app/src/main/java/com/codex/campboardgamehost/clocktower/domain/CommittedClocktowerSetup.kt`

Accepted contract:

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

Persistence schema/version remains outside the domain model.

## 5. MS-S1R accepted result

Authoritative checkpoint:

`docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md`

Recovery authority is now:

```text
active save
├─ exact CommittedClocktowerSetup
└─ Trouble Brewing compact completion/diversity record
```

Direct restore:

```text
persisted exact committed setup
-> decode + validate
-> CommittedClocktowerSetup
```

Accepted outcomes:

- exact script, seed, ordered seats, actual identities, shown identities and provenance are persisted;
- current Trouble Brewing restore requires exact committed setup;
- restore no longer loads the frozen 480-preset asset to infer setup identities;
- restore no longer reconstructs `TroubleBrewingSetupPresetSelection`;
- a compact committed TB rotation record preserves completion/diversity-history inputs without keeping the whole selection as recovery authority;
- exact setup and compact record provider/candidate/player-count identities are cross-validated;
- `TroubleBrewingSetupProvenancePersistence` and its legacy typed contract were retired after production call-site proof;
- a surviving production wiring test explicitly forbids reintroducing the legacy provenance reconstruction path.

MS-S1R did not broaden into `ClocktowerNightCheckpoint`, draft recovery, `NightTransactionRestoreComposition`, Dawn/Dusk transaction semantics, or REC-R1 work.

## 6. Recovery product boundary

The product no longer promises arbitrary exact continuation of an unfinished game after exit/restart.

Supported recovery goal:

```text
best-effort crash / Android process-death recovery
-> latest supported stable committed domain checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart at the next safe domain/action boundary
```

Explicit non-goals include exact arbitrary mid-UI continuation, durable draft persistence solely for UI reconstruction, and indefinite compatibility for every historical unfinished-save shape.

Committed-domain retry/idempotency/convergence remains protected. Completed-game setup/diversity history remains separately durable.

## 7. MS-S2 immediate objective

MS-S2 establishes the **generic setup candidate/source/provider ownership contracts only**.

Target concepts:

```text
SetupCandidate
SetupCandidateSource
ClocktowerSetupPolicy / ClocktowerSetupProvider
ClocktowerSetupProviderRegistry
```

This slice must remain persistence-independent.

Do not yet implement:

- optional template repositories — MS-S3;
- generated setup algorithms — MS-S4;
- common diversity history/scoring/selection — MS-S5;
- generic shown-identity commitment — MS-S6;
- TB production adaptation — MS-S7;
- NGJ production adaptation — MS-S8.

## 8. MS-S2 audit targets before code

Audit the smallest relevant current surfaces:

1. `TroubleBrewingSetupPreset` / dataset / selection models;
2. `TroubleBrewingSetupPresetSelector` inputs and responsibilities;
3. `TroubleBrewingProductionSetupPreparer` provider/orchestration responsibilities;
4. `TroubleBrewingCommittedSetupAdapter` and deal-plan boundary, only to understand where candidate ends and materialization begins;
5. No Greater Joy / generic script role definitions and `clocktowerDistribution(playerCount)` entry points;
6. existing `ScriptId`, `RoleId`, `ClocktowerScript`, and ruleset/provider registry patterns elsewhere in the codebase that can be reused instead of duplicated.

Classify every candidate field as one of:

```text
GENERIC CANDIDATE FACT
TB-ONLY TEMPLATE METADATA
TB-ONLY DIVERSITY/SCORING METADATA
MATERIALIZATION/SHOWN-IDENTITY CONCERN
PERSISTENCE CONCERN — must not enter MS-S2
```

## 9. MS-S2 proposed contract direction

Prefer a small pure Kotlin boundary approximately like:

```text
SetupCandidate
├─ script: ScriptId
├─ candidateId: String?
├─ playerCount: Int / derived from roles
├─ actualRoles: role multiset or ordered pre-seat role list
└─ source/provider identity needed by later commitment provenance

SetupCandidateSource
└─ candidatesFor(request/context): List<SetupCandidate>

ClocktowerSetupProvider
├─ script identity
└─ candidate source/policy ownership

ClocktowerSetupProviderRegistry
└─ resolve(script): ClocktowerSetupProvider
```

Do not freeze this exact field shape until the TB/NGJ audit proves which facts are truly generic. In particular, avoid prematurely moving TB `styleTags`, minion-set features, similarity metadata, `drunkAsOptions`, dataset schema/version, or rotation weights into generic candidate core.

Shown identity is not automatically a candidate fact: MS-S6 owns the generic shown-identity commitment policy. Preserve TB's current selector-owned Drunk behavior until the later adaptation slice deliberately maps it.

## 10. MS-S2 required evidence

Use typed tests for durable contracts. Minimum useful evidence should prove:

1. generic candidate construction rejects structurally invalid/empty identities as appropriate;
2. source/provider identity is script-neutral and does not require Android/persistence classes;
3. registry resolves the provider for a registered `ScriptId`;
4. unregistered script behavior is explicit and deterministic;
5. duplicate provider registration/identity behavior is explicit;
6. generic contracts contain no TB-only mandatory metadata;
7. introducing a future provider does not require changing App-root branching in MS-S2 itself.

Existing tests count as evidence where they already prove a requirement. Do not add source-string RED solely because a pure Kotlin typed test is easier and stronger.

## 11. Protected predecessor invariants

Preserve throughout MS-SETUP:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random TB setup.
Identity reveal does not synchronously block on recommendation/First Night computation.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup summary.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority, and NGJ setup legality/current behavior until its explicit migration.

## 12. Workflow

Follow:

- root `AGENTS.md`;
- `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`;
- `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` only when a large/truncated file must be changed.

For MS-S2, prefer new/small pure Kotlin files and direct GitHub connector writes. App/Host production wiring should not be necessary in this slice.

## 13. Immediate next action — MS-S2 audit first

1. re-confirm live `main`, branch head, Draft PR #61 and checks;
2. inspect TB preset/selection/selector/preparer model boundaries;
3. inspect NGJ role/distribution setup entry points;
4. search for reusable registry/provider patterns already in the repo;
5. freeze the smallest generic candidate/source/provider contract;
6. establish typed test-first evidence where a durable new contract is being introduced;
7. implement only MS-S2 pure contracts;
8. run focused tests, then T1/CI as justified;
9. stop before MS-S3.

## 14. Explicit non-goals for MS-S2

Do not broaden into:

- template repository implementation;
- generated setup algorithm implementation;
- diversity scoring/rotation migration;
- generic Drunk/shown-identity policy;
- TB/NGJ production cutover;
- general unfinished-game recovery cleanup;
- Mayor redirect redesign;
- Imp succession redesign;
- Monk/Attack-Protect replay;
- A3/A4/ZDD;
- Host/App decomposition for its own sake;
- regeneration/reformatting of the frozen TB preset dataset;
- PR Ready/merge changes.

Keep PR #61 Draft. Do not merge, mark Ready, force-push or rebase without explicit user authorization.
