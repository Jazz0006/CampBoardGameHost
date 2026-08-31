# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S3 COMPLETE / ACCEPTED — MS-S4 NEXT**

## 1. Accepted live checkpoints

Campaign baseline main:

`eed51bade5163790316a31e8295e2e841df90357`

Merged / fully validated TBSP checkpoint:

`98ee982ef3590822cd06ac72a047b49afac3cfd6`

Current campaign branch:

`codex/ms-setup-generic-architecture`

Current campaign PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN`

Accepted slice checkpoints:

```text
MS-S1:
f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b

MS-S1R:
2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e

MS-S2:
d4001863f134ebbe7d26819f40ac34c7d1de200c

MS-S3:
6b15822e75680fb8e718f5db24358e1a935b5523
```

MS-S3 validation:

```text
CI #1230 / run 33357908514   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1147 / run 33357908443   SUCCESS
Full Android                 SKIPPED by risk router
ASP / Real Clingo            SKIPPED by risk router
```

Later docs-only commits are carriers and do not replace the validated MS-S3 code/test head.

Always re-query live GitHub state before a production write.

## 2. Product goal

Build one script-neutral Clocktower setup pipeline:

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

The setup engine ends at `CommittedClocktowerSetup`. Persistence/recovery is an outer consumer. App/Host must not become the owner of script-specific setup policy.

## 3. Campaign sequence

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
MS-S9   future no-template script needs no App-root branch

REC-R1  separate future unfinished-game stable-checkpoint simplification
```

Do not implement several slices together merely because they share a campaign.

## 4. Accepted setup-domain boundary through MS-S3

### Exact committed fact — MS-S1

```text
CommittedClocktowerSetup
├─ script: ScriptId
├─ setupSeed: Long
├─ assignments: ordered seats with actualRole + shownRole
└─ provenance: sourceKind + providerId + candidateId?
```

Exact committed identities, not provenance, are restore authority.

### Direct setup persistence — MS-S1R

```text
persisted exact CommittedClocktowerSetup
-> decode + validate
-> restore exact setup
```

TB completion/diversity history uses a compact committed rotation record. Restore no longer reloads the 480-preset asset or reconstructs `TroubleBrewingSetupPresetSelection`.

### Candidate/provider boundary — MS-S2

```text
SetupCandidate
├─ script
├─ canonical pre-seat actual-role multiset
├─ derived playerCount
└─ provenance

SetupCandidateRequest(script, playerCount, setupSeed)
SetupCandidateSource
ClocktowerSetupProvider
ClocktowerSetupProviderRegistry
```

Candidates deliberately exclude seating, shown identity, persistence and diversity history.

### Optional template lookup — MS-S3

```text
TemplateBucketKey(script, playerCount)
TemplateRepository
├─ find(script, playerCount)
└─ SetupCandidateSource.candidates(request)
```

Accepted behavior:

- exact script/player-count lookup;
- absent bucket returns empty list as a normal result;
- caller collection mutation cannot alter repository contents;
- bucket/candidate script or player-count mismatch is rejected;
- only `TEMPLATE` candidates with durable `candidateId` are accepted;
- duplicate provider/candidate identity in a bucket is rejected;
- result order is deterministic;
- template lookup ignores setup seed;
- no Android/assets/JSON/persistence/diversity/shown-role dependency.

Authoritative checkpoint:

`docs/MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md`

## 5. Existing source ownership that MS-S4 must respect

The current legacy NGJ path in `CampBoardGameHostApp.kt` mixes three concerns:

```text
actual-role composition generation
+ seat shuffle
+ Drunk shown-role selection
```

MS-S4 must extract only the **generated pre-seat actual-role composition** concern. Do not copy the mixed legacy function wholesale.

The legacy composition currently uses:

- `clocktowerDistribution(playerCount)`;
- role pools grouped by team;
- one Demon;
- configured Minion count;
- Baron setup adjustment;
- configured Outsider/Townsfolk counts.

For NGJ small games, current Baron handling deliberately limits outsider increase so the result remains legal for the available composition. Preserve legality intent, but MS-S4 should derive generic setup legality from validated ruleset/catalog metadata rather than hard-coding App UI behavior where practical.

## 6. MS-S4 immediate objective

Introduce the smallest pure Kotlin deterministic seeded legal generated-candidate source.

Target:

```text
SetupCandidateRequest
+ validated script/ruleset role metadata
-> deterministic generated SetupCandidate values
```

The generated source must produce **pre-seat actual-role compositions only**.

It must not assign seats or shown identities.

## 7. MS-S4 audit targets before code

Audit only surfaces required to generate a legal candidate:

1. current NGJ `generateClocktowerAssignments` actual-role composition portion;
2. `clocktowerDistribution(playerCount)` ownership and all supported player-count distributions;
3. `ValidatedClocktowerRuleset`, `ClocktowerCharacterDefinition.team`, `setup`, special metadata and any existing setup-modifier representation;
4. current Baron handling in TB and NGJ;
5. existing deterministic hash/selection utilities such as `MurmurHash3` / stable selector patterns;
6. MS-S2 `SetupCandidateRequest`, candidate provenance and provider attribution expectations.

Do not audit unrelated Host/night/recovery mechanics in this slice.

## 8. MS-S4 required contract direction

Prefer a source approximately equivalent to:

```text
GeneratedSetupCandidateSource
├─ script/ruleset metadata
├─ providerId
└─ candidates(request) -> deterministic legal generated candidates
```

Required behavior:

- request script must match the source/ruleset script;
- same script + playerCount + setupSeed yields identical generated candidate output;
- generation must not use global `Random`, `.random()` or unseeded `.shuffled()`;
- generated candidates use `SetupSourceKind.GENERATED`;
- provider attribution is stable and compatible with `ClocktowerSetupProvider`;
- actual role count exactly equals requested player count;
- team/type distribution is legal after setup modifiers;
- actual-role composition contains no seat semantics;
- shown identity / Drunk disguise remains MS-S6 ownership;
- invalid ruleset/input fails explicitly rather than broad-random fallback.

A different seed should be capable of producing variation when more than one legal composition exists, but exact legacy random-sequence parity is not required.

## 9. MS-S4 evidence strategy

Use typed tests. Minimum useful evidence:

1. deterministic repeatability for same request/seed;
2. legal player-count/team composition;
3. deterministic variation evidence across selected seeds where the role pool permits it;
4. stable generated provenance/provider identity;
5. cross-script request rejection;
6. setup-modifier correctness, especially Baron-style outsider/townsfolk shifts;
7. no shown-role or seating information enters `SetupCandidate`;
8. no production App/Host wiring changes.

Existing tests around NGJ distribution/legal start behavior remain useful parity evidence but do not by themselves prove deterministic generation.

## 10. Explicit MS-S4 non-goals

Do not broaden into:

- common diversity history/scoring/selection — MS-S5;
- Drunk/shown-identity commitment — MS-S6;
- TB 480-preset adaptation — MS-S7;
- NGJ production cutover — MS-S8;
- setup persistence changes;
- App/Host decomposition;
- general unfinished-game recovery cleanup;
- arbitrary exact resume work;
- PR Ready/merge changes.

## 11. Recovery product boundary

Supported recovery goal remains:

```text
best-effort crash / process-death recovery
-> latest supported stable committed checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart at a safe domain/action boundary
```

Broad unfinished-night simplification remains future REC-R1 work and is outside MS-S4.

## 12. Protected predecessor invariants

Preserve throughout MS-SETUP:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority and NGJ setup legality until explicit migration.

## 13. Workflow

Follow:

- root `AGENTS.md`;
- `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Use risk-based typed evidence, not RED ceremony.

MS-S4 should remain small pure Kotlin work, suitable for direct GitHub connector edits. Do not touch the large App root in MS-S4.

## 14. Immediate next action — MS-S4 audit first

1. re-confirm live `main`, branch, Draft PR #61 and checks;
2. inspect the legacy NGJ composition logic and supported distributions;
3. inspect validated ruleset/catalog role metadata and setup-modifier support;
4. decide the smallest generic legality input needed by the generator;
5. freeze deterministic seed derivation and source API;
6. establish typed test-first evidence;
7. implement only generated pre-seat candidate production;
8. run focused/FAST evidence according to risk router;
9. stop before MS-S5.

## 15. Documentation authority

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

## 16. Resume guard

Treat `6b15822e75680fb8e718f5db24358e1a935b5523` as the accepted MS-S3 code/test checkpoint unless a later production commit deliberately supersedes it.

Next production slice is MS-S4. Keep PR #61 Draft. Do not merge, mark Ready, force-push or rebase without explicit user authorization.
