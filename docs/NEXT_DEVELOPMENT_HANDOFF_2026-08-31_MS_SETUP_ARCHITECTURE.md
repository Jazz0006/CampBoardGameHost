# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S2 COMPLETE / ACCEPTED — MS-S3 NEXT**

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
```

MS-S2 validation:

```text
CI #1225 / run 33357219556   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1142 / run 33357219544   SUCCESS
Full Android                 SKIPPED by risk router
ASP / Real Clingo            SKIPPED by risk router
```

Later docs-only commits are carriers and do not replace the validated MS-S2 code/test head.

Always re-query live GitHub state before production writes.

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
MS-S3   optional TemplateRepository keyed by script + player count                  NEXT
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource
MS-S5   common deterministic SetupDiversityHistory / scorer / selector facade
MS-S6   generic shown-identity commitment policy
MS-S7   adapt TB 480-preset pipeline; preserve TB behavior/parity
MS-S8   adapt NGJ/no-template path; legality parity + deterministic seeded evidence
MS-S9   future no-template script needs no App-root branch

REC-R1  separate future unfinished-game stable-checkpoint simplification
```

Do not implement several slices together merely because they share a campaign.

## 4. MS-S1 accepted result

Authoritative checkpoint:

`docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md`

Accepted exact committed fact:

```text
CommittedClocktowerSetup
├─ script: ScriptId
├─ setupSeed: Long
├─ assignments: ordered seats with actualRole + shownRole
└─ provenance: sourceKind + providerId + candidateId?
```

Exact committed identities, not provenance, are restore authority.

## 5. MS-S1R accepted result

Authoritative checkpoint:

`docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md`

Current setup recovery is direct:

```text
persisted exact CommittedClocktowerSetup
-> decode + validate
-> restore exact setup
```

TB completion/diversity history uses a compact committed rotation record. Restore no longer reloads the 480-preset asset or reconstructs `TroubleBrewingSetupPresetSelection`. The old provenance-reconstruction codec was retired after call-site proof.

Broad unfinished-night recovery remains outside MS-SETUP and belongs to future REC-R1.

## 6. MS-S2 accepted result

Authoritative checkpoint:

`docs/MS_S2_SETUP_PROVIDER_CONTRACT_CHECKPOINT_2026-08-31.md`

Accepted production owner:

`app/src/main/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProvider.kt`

Accepted typed test:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProviderRegistryTest.kt`

Accepted concepts:

```text
SetupCandidate
SetupCandidateRequest
SetupCandidateSource
ClocktowerSetupProvider
ClocktowerSetupProviderRegistry
```

### Candidate boundary

A `SetupCandidate` is a **pre-seat actual-role multiset**:

```text
SetupCandidate
├─ script: ScriptId
├─ actualRoles: canonical/snapshotted List<RoleId>
├─ playerCount: derived
└─ provenance: SetupProvenance
```

It deliberately excludes seats, shown identities, persistence schema and diversity history/scoring.

The actual-role list is canonically sorted by `RoleId.value`; order therefore carries no seating semantics while duplicates remain representable.

### Request/source boundary

```text
SetupCandidateRequest
├─ script
├─ positive playerCount
└─ setupSeed

SetupCandidateSource
request -> List<SetupCandidate>
```

The seed exists for later MS-S4 deterministic generation. Diversity history remains MS-S5 ownership.

### Provider/registry boundary

A `ClocktowerSetupProvider` owns one script, one provider ID and one candidate source. It rejects cross-script requests, cross-script candidates, mismatched player count and cross-provider candidate attribution.

`ClocktowerSetupProviderRegistry` maps a script to one provider; duplicate script registrations fail and unregistered scripts resolve explicitly to `null`.

No App/Host/TB/NGJ production wiring changed in MS-S2.

## 7. Why the candidate is pre-seat actual roles only

TB preset audit showed the generic subset is only:

```text
preset identity
player count / role count
actual role composition
source provenance
```

These TB fields remain outside generic candidate core:

```text
source text
complexity
styleTags
runtime selection policy
similarity / exact-repeat thresholds
history weights
rotation-scoring metadata
drunkAsOptions
selectedDrunkShownRole
```

The current NGJ legacy path combines actual-role composition, seat randomization and Drunk shown-role selection in one function. MS-S2 deliberately did not copy that mixed boundary. Later slices separate:

- generated actual-role composition — MS-S4;
- diversity selection — MS-S5;
- shown identity — MS-S6;
- NGJ production adaptation — MS-S8.

## 8. Recovery product boundary

Supported recovery goal remains:

```text
best-effort crash / process-death recovery
-> latest supported stable committed checkpoint
-> restore committed setup + committed game facts exactly
-> resume/restart at a safe domain/action boundary
```

Do not reintroduce arbitrary exact mid-UI resume as an MS-SETUP requirement.

## 9. MS-S3 immediate objective

MS-S3 introduces the smallest **optional template repository** keyed by script + player count and adapted to the MS-S2 candidate boundary.

Target concept:

```text
TemplateRepository
(script, playerCount)
-> zero or more template-backed SetupCandidate values
```

“Zero candidates” is a normal result because later policy must be able to fall back to generated candidates. It is not equivalent to malformed template data.

## 10. MS-S3 audit targets before code

Audit only the relevant existing surfaces:

1. `TroubleBrewingSetupPresetDataset.pools` and its key/player-count invariants;
2. `TroubleBrewingSetupPresetValidator` ownership — distinguish data validation from generic repository lookup;
3. `TroubleBrewingSetupPresetJson` only to understand current data ownership, not to make JSON/asset loading generic core;
4. MS-S2 `SetupCandidate` / `SetupCandidateSource` contract;
5. any existing repository/catalog pattern whose map/snapshot/duplicate behavior is worth reusing.

Classify fields as:

```text
GENERIC TEMPLATE LOOKUP KEY
GENERIC CANDIDATE FACT
TB DATASET VALIDATION METADATA
TB SCORING/POLICY METADATA
SHOWN-IDENTITY CONCERN
ANDROID/ASSET CONCERN — must stay outside generic repository
```

## 11. MS-S3 contract direction

Prefer a pure Kotlin repository roughly equivalent to:

```text
TemplateRepository
├─ immutable template candidate collection
└─ find(script: ScriptId, playerCount: Int): List<SetupCandidate>
```

Required behavior should include:

- exact script + player-count bucket lookup;
- deterministic/stable result order or explicit canonicalization;
- immutable snapshot semantics;
- zero-result lookup for absent script/player count;
- validation that stored candidate script/player count agrees with its bucket;
- no Android `Context`, assets, JSON, persistence, diversity history or shown-role dependencies.

Do not freeze TB dataset schema/version into the generic repository. TB dataset adaptation belongs later in MS-S7.

## 12. MS-S3 evidence strategy

Use typed tests. Minimum useful evidence:

1. matching script/player count returns only matching template candidates;
2. absent script/player count returns empty list;
3. caller collection mutation cannot mutate repository contents;
4. cross-script or cross-count bucket mismatch is rejected;
5. lookup order is deterministic;
6. generic repository remains independent of TB-only metadata and Android/persistence classes.

Existing evidence counts where applicable. Do not create source-string tests when typed behavior proof is stronger.

## 13. Protected predecessor invariants

Preserve throughout MS-SETUP:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid TB template data never silently falls back to broad-random TB setup.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority and NGJ setup legality until explicit migration.

## 14. Workflow

Follow:

- root `AGENTS.md`;
- `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

MS-S3 should be small pure Kotlin work, suitable for direct GitHub connector edits. It should not require App/Host modification.

## 15. Immediate next action — MS-S3 audit first

1. re-confirm live `main`, branch, Draft PR #61 and checks;
2. inspect TB dataset pools + validator and MS-S2 contracts;
3. freeze the smallest generic template repository contract;
4. establish typed test-first evidence for immutable/correct bucket lookup;
5. implement only MS-S3 pure repository code;
6. run focused/FAST evidence according to risk router;
7. stop before MS-S4 deterministic generator work.

## 16. Explicit non-goals for MS-S3

Do not broaden into:

- deterministic generated setup implementation;
- diversity scoring/rotation migration;
- shown-identity/Drunk commitment policy;
- TB production cutover;
- NGJ production cutover;
- Android asset-loading abstraction for setup templates;
- setup persistence changes;
- general unfinished-game recovery cleanup;
- Host/App decomposition;
- PR Ready/merge changes.

Keep PR #61 Draft. Do not merge, mark Ready, force-push or rebase without explicit user authorization.
