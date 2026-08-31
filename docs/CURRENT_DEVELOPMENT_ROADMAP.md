# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation; checkpoints below do not replace live-state verification.

## 1. Current development context

```text
campaign baseline main:
eed51bade5163790316a31e8295e2e841df90357

merged / fully validated TBSP checkpoint:
98ee982ef3590822cd06ac72a047b49afac3cfd6

current branch:
codex/ms-setup-generic-architecture

current Draft PR:
#61 — MS-SETUP: generic multi-script setup architecture
DRAFT / OPEN

MS-S1 accepted code/test checkpoint:
f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b

MS-S1R accepted code/test checkpoint:
2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e

MS-S2 accepted code/test checkpoint:
d4001863f134ebbe7d26819f40ac34c7d1de200c

MS-S3 accepted code/test checkpoint:
6b15822e75680fb8e718f5db24358e1a935b5523
```

MS-S3 validation:

```text
CI #1230 / run 33357908514   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1147 / run 33357908443   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Later docs-only commits are carriers and do not replace the accepted code/test checkpoint.

Current campaign status:

```text
MS-S0    ownership audit                         COMPLETE
MS-S0.5  recovery scope reduction audit          COMPLETE
MS-S1    CommittedClocktowerSetup + provenance   COMPLETE / ACCEPTED
MS-S1R   setup persistence authority migration   COMPLETE / ACCEPTED
MS-S2    candidate/source/provider contracts     COMPLETE / ACCEPTED
MS-S3    optional template repository            COMPLETE / ACCEPTED
MS-S4    deterministic generated source          NEXT
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

Accepted checkpoints:

- `docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md`
- `docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md`
- `docs/MS_S2_SETUP_PROVIDER_CONTRACT_CHECKPOINT_2026-08-31.md`
- `docs/MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md`

Recovery decision/audit:

`docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md`

## 2. MS-SETUP target architecture

Build one script-neutral setup pipeline:

```text
script + playerCount + seed + diversity history
-> resolve script/ruleset setup provider
-> query optional template candidates
-> templates exist: validated template candidates
-> no templates: legal generated candidates
-> common deterministic diversity selector
-> commit shown-identity decisions
-> CommittedClocktowerSetup
```

`CommittedClocktowerSetup` is the immutable exact initial setup fact. Persistence/recovery is an outer consumer and must not be a dependency of candidate generation, selection, shown-identity choice, or setup commitment.

The App root must not gain new script-specific setup branches when future scripts are added.

## 3. Completed predecessor — TBSP

Trouble Brewing Setup Presets is complete and merged.

Preserve until deliberately migrated with parity evidence:

- frozen 480-preset dataset;
- deterministic preset/deal semantics;
- selector-owned Drunk shown identity;
- true-completion rotation history;
- non-blocking reveal/First Night precompute;
- accepted durability behavior.

Historical/normative TBSP docs remain authoritative where their behavior is still being adapted.

## 4. MS-S0 / MS-S0.5 — COMPLETE

Ownership audit established:

- TB setup is currently implemented by TB-specific preset/data/selector/rotation/materialization components plus App start wiring;
- NGJ uses a legacy no-template path that mixes actual-role generation, random seating and Drunk shown-role choice;
- NGJ generation currently uses unseeded random/shuffle behavior before the game seed is established;
- Host consumes prepared setup and does not own initial setup generation;
- TB-specific style/minion-set/rotation metadata must not become mandatory generic-core state.

Recovery scope decision:

```text
supported:
best-effort crash / Android process-death recovery
-> latest supported stable committed domain checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart from next safe domain/action boundary

not promised:
exact arbitrary mid-UI continuation
play-half-today/resume-tomorrow contract
indefinite compatibility for every historical unfinished save
```

Broad unfinished-night simplification remains future REC-R1 work.

## 5. MS-S1 — COMPLETE / ACCEPTED

Accepted pure domain model:

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

Key invariants: canonical seats `1..N`, exact actual/shown identities, immutable assignment snapshot, structural equality, generic provenance only, no Android/session/persistence dependency.

## 6. MS-S1R — COMPLETE / ACCEPTED

Setup recovery authority changed from TB provenance + current dataset reconstruction to direct exact setup persistence:

```text
exact CommittedClocktowerSetup
-> persist exact setup
-> direct decode/validate on restore

TB compact completion/diversity record
-> persist original committed rotation-relevant summary
-> direct restore
```

Accepted outcomes:

- restore no longer reloads the 480-preset asset to infer setup identities;
- restore no longer reconstructs `TroubleBrewingSetupPresetSelection`;
- old `TroubleBrewingSetupProvenancePersistence` authority and typed legacy contract were retired after call-site proof;
- exact setup and compact TB completion record are cross-validated;
- only true completed TB games enter rotation history;
- broad night/draft recovery remains untouched.

## 7. MS-S2 — COMPLETE / ACCEPTED

Accepted production contract:

`app/src/main/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProvider.kt`

Accepted concepts:

```text
SetupCandidate
SetupCandidateRequest
SetupCandidateSource
ClocktowerSetupProvider
ClocktowerSetupProviderRegistry
```

`SetupCandidate` is deliberately a **pre-seat actual-role multiset**, not a committed setup. Its role list is canonical/snapshotted and contains no seating, shown identities, persistence schema or diversity history.

`SetupCandidateRequest` contains only `script`, positive `playerCount`, and `setupSeed`. The seed is available for MS-S4 deterministic generation; diversity history remains MS-S5 ownership.

`ClocktowerSetupProvider` rejects cross-script requests, mismatched candidate player counts, cross-script candidates and cross-provider attribution. Empty candidate lists remain valid.

`ClocktowerSetupProviderRegistry` maps one provider per script, returns `null` for an unregistered script and rejects duplicate script registration.

No App/Host/TB/NGJ production wiring changed in MS-S2.

## 8. MS-S3 — COMPLETE / ACCEPTED

Accepted production contract:

`app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TemplateRepository.kt`

Accepted typed test:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/TemplateRepositoryTest.kt`

Accepted concepts:

```text
TemplateBucketKey(script, playerCount)
TemplateRepository
  find(script, playerCount) -> List<SetupCandidate>
  SetupCandidateSource.candidates(request)
```

Accepted repository behavior:

- exact script + player-count bucket lookup;
- missing bucket returns an empty list as a normal no-template result;
- immutable snapshot of caller collections;
- bucket script/player-count mismatches fail fast;
- only `SetupSourceKind.TEMPLATE` candidates are accepted;
- template candidates require durable `candidateId`;
- duplicate `(providerId, candidateId)` identity within a bucket is rejected;
- result order is canonical/deterministic;
- setup seed is deliberately ignored by template lookup;
- no Android, asset, JSON, persistence, diversity or shown-identity dependency.

TB dataset parsing and TB-specific validation remain outside generic repository ownership.

Authoritative checkpoint:

`docs/MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md`

## 9. Implementation campaign

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                            COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                          COMPLETE
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance      COMPLETE / ACCEPTED
MS-S1R  exact setup persistence authority migration + TB restore retirement         COMPLETE / ACCEPTED
MS-S2   generic SetupCandidate + source/provider registry contracts                 COMPLETE / ACCEPTED
MS-S3   optional TemplateRepository keyed by script + player count                  COMPLETE / ACCEPTED
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource                    NEXT
MS-S5   common deterministic SetupDiversityHistory / scorer / selector facade
MS-S6   generic shown-identity commitment policy
MS-S7   adapt TB 480-preset pipeline; preserve TB behavior/parity
MS-S8   adapt NGJ/no-template path; legality parity + deterministic seeded evidence
MS-S9   acceptance: future no-template script needs no App-root branch

REC-R1  separate future unfinished-game stable-checkpoint simplification
```

Do not implement several slices at once merely because they share the campaign.

## 10. MS-S4 — NEXT

MS-S4 introduces a deterministic seeded legal `GeneratedSetupCandidateSource` for scripts that do not have usable template candidates.

Required boundary:

```text
SetupCandidateRequest(script, playerCount, setupSeed)
+ validated script/ruleset character metadata
-> deterministic legal generated SetupCandidate values
```

Before production writes:

1. re-query live `main`, branch, Draft PR #61 and checks;
2. audit the legacy NGJ actual-role composition rules separately from its seat shuffle and Drunk shown-role handling;
3. audit generic ruleset/catalog metadata available for role team/type and setup modifiers;
4. freeze deterministic seed derivation and legal candidate generation ownership;
5. prove same request/seed gives identical generated candidates;
6. prove different seeds can produce valid variation without relying on global `Random`;
7. prove generated candidates have `SetupSourceKind.GENERATED` and correct provider attribution;
8. preserve player-count legality, including setup modifiers such as Baron, without applying them twice;
9. stop before diversity scoring/selection (MS-S5), shown identity (MS-S6), or NGJ production cutover (MS-S8).

MS-S4 must not silently fall back from malformed template data; template availability/failure policy remains separate from generated candidate legality.

## 11. Protected predecessor correctness

Preserve throughout migration:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid TB template data never silently falls back to broad random TB setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup summary.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority, and NGJ setup legality/current behavior until its explicit migration.

## 12. Testing cadence

Follow root `AGENTS.md`, `docs/TESTING_STRATEGY.md`, and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Use risk-based evidence:

- durable new contracts get the smallest valuable typed test;
- existing tests count as evidence;
- do not manufacture source-string RED when typed behavior proof is practical;
- T0 is the smallest directly relevant evidence;
- `:app:testFast` is a logical-checkpoint T1 gate;
- T4 is an explicit full-acceptance/merge-level checkpoint rather than a micro-slice default;
- local/focused evidence does not replace required GitHub CI/R2 at an applicable checkpoint.

MS-S3 is accepted at `6b15822e75680fb8e718f5db24358e1a935b5523` with CI #1230 and R2 #1147.

## 13. Writer / large-file workflow

Safe small/medium tests/docs/source: GitHub connector directly.

Large/truncated source with stable unique anchors: GitHub Actions one-shot + separate Python patch per `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md`.

Use Codex/Luna only if that path cannot safely perform the required write.

MS-S4 should remain pure Kotlin setup-domain work and should not require App/Host edits.

## 14. Current documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md
docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md
docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md
docs/MS_S2_SETUP_PROVIDER_CONTRACT_CHECKPOINT_2026-08-31.md
docs/MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 15. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap and the active handoff;
3. read MS-S1/MS-S1R/MS-S2/MS-S3 checkpoints as relevant;
4. re-query live `main`, `codex/ms-setup-generic-architecture`, Draft PR #61 and checks;
5. treat `98ee982ef3590822cd06ac72a047b49afac3cfd6` as the fully validated merged TBSP checkpoint unless live audit changes that fact;
6. treat `6b15822e75680fb8e718f5db24358e1a935b5523` as the accepted MS-S3 code/test checkpoint unless later production commits deliberately supersede it;
7. next production slice is MS-S4;
8. stop MS-S4 before diversity scoring/selection (MS-S5) and shown-identity commitment (MS-S6);
9. do not perform broad unfinished-night cleanup inside MS-SETUP; REC-R1 owns that later work;
10. keep PR #61 Draft and do not merge, mark Ready, force-push or rebase without explicit user authorization.

## 16. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — MS-S4 NEXT |
| MS-S1R setup persistence authority migration | COMPLETE / ACCEPTED |
| MS-S2 generic candidate/provider contracts | COMPLETE / ACCEPTED |
| MS-S3 optional template repository | COMPLETE / ACCEPTED |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| GCR-5 reconstructor naming clarity | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| A3 immutable setup snapshot ownership/persistence | SUPERSEDED BY MS-S1/MS-S1R |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1 |
