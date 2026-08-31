# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S5 COMPLETE / ACCEPTED — MS-S6A NEXT**

## 1. Live / accepted checkpoints

Campaign baseline main:

`eed51bade5163790316a31e8295e2e841df90357`

Merged / fully validated TBSP checkpoint:

`98ee982ef3590822cd06ac72a047b49afac3cfd6`

Current campaign branch:

`codex/ms-setup-generic-architecture`

Current campaign PR:

`#61 — MS-SETUP: generic multi-script setup architecture — DRAFT / OPEN`

Accepted code/test slice checkpoints:

```text
MS-S1:
f3d6b7f305ad09ab8e44f64cf476271ffc5c7a0b

MS-S1R:
2a6d447398c9ab857ab48dd6ff3e5995fb73dd7e

MS-S2:
d4001863f134ebbe7d26819f40ac34c7d1de200c

MS-S3:
6b15822e75680fb8e718f5db24358e1a935b5523

MS-S4:
6de0e8c99c89a091615c513255adbdb773b3cc69

MS-S5:
86c3ce651025de9ccbe1094b161becc171514e69
```

MS-S5 validation:

```text
RED:
d0145d2347490aa4b7b1f037188e1204cfac3832
CI #1243 / run 33362653627   EXPECTED RED
compileDebugUnitTestKotlin failed on missing S5 production types
R2 #1160 / run 33362653634   SUCCESS

GREEN:
86c3ce651025de9ccbe1094b161becc171514e69
CI #1245 / run 33362804682   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1162 / run 33362804691   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Authoritative S5 checkpoint:

`docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md`

S4.5 architecture correction remains authoritative:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

Later docs-only carrier commits do not replace the accepted S5 production checkpoint.

Always re-query live GitHub state before the next production write.

## 2. Frozen architecture

The causal order remains:

```text
Composition
-> Identity
-> Information
```

Implemented/future flow:

```text
script + playerCount + setupSeed
-> candidate legality/source                       [MS-S2/S3/S4 COMPLETE]
-> actual-composition diversity selection          [MS-S5 COMPLETE]
-> shown-identity policy/options                    [MS-S6A NEXT]
-> deterministic shown-identity commitment          [MS-S6B]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> recommendation consumes PlayerState.shownRole   [MS-S6C]
-> recommendation produces information only
```

Governing rule:

> Shown identity is a setup fact. It cannot participate in actual-role composition scoring and cannot later be selected/replaced by recommendation.

## 3. Accepted predecessor ownership

### MS-S1 / S1R

`CommittedClocktowerSetup` stores exact actual/shown identities. Persistence restores exact committed setup directly and never reruns selectors/recommendation.

### MS-S2

`SetupCandidate` is a canonical **pre-seat actual-role multiset**. Do not add shown-role fields to it.

### MS-S3

`TemplateRepository` owns actual-role template candidate lookup only. Template shown-identity metadata requires a separate later boundary.

### MS-S4

`GeneratedSetupCandidateSource` owns deterministic legal actual-role generation only, including current base distribution and one capped Baron setup modifier application.

### MS-S4.5

The architecture correction explicitly removed shown identity from composition authority. Legacy TB Drunk shown-role weighting must not be copied into generic S5 or future composition adapters.

## 4. MS-S5 accepted design

New production files:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversityHistory.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelector.kt
```

Typed contract:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelectorTest.kt`

Generic boundary:

```text
legal SetupCandidate values
+ actual-composition SetupDiversityHistory
+ SetupDiversityPolicy
+ deterministic selectionSeed
-> one selected SetupCandidate
```

### History authority

`SetupDiversityRecord` stores only:

```text
script
canonical actualRoles
derived playerCount
```

History is filtered by exact script + player count before weighting.

It has no fields for:

- shown role;
- `drunkAsOptions`;
- first-night information;
- recommendation decisions;
- TB style/dataset metadata;
- seating;
- persistence schema.

### Invariant-role exclusion

The scorer computes the role multiset common to all candidates in the current pool and removes those invariant occurrences before measuring overlap.

This means fixed roles such as TB's Imp do not inflate similarity while generic S5 remains unaware of role team/type.

Do not replace this with a script-specific `exclude Demon` rule in generic core.

### Fixed-point scoring

```text
FIXED_POINT_SCALE = 1_000_000
```

Scoring uses age-weighted actual-composition overlap and a minimum novelty floor. Default generic history weights are:

```text
100, 65, 40, 20, 10
```

No floating-point weighted draw is used.

### Exact repeat / overlap eligibility

Generic modes:

```text
ALLOW
REJECT_WHEN_ALTERNATIVE
REJECT
```

Default is `REJECT_WHEN_ALTERNATIVE`.

Optional last-game max-overlap filtering uses fixed-point thresholds and `0.05` fallback steps until the first non-empty eligibility level.

### Deterministic selection

Candidate order is canonicalized independently of caller order. Weighted selection uses existing MurmurHash3 with namespace:

```text
setup-diversity-v1|script|playerCount|selectionSeed
```

Single generated candidates can select normally even when `candidateId == null`.

## 5. S5 exact scope audit

Compared with pre-S5 docs carrier `165728aad3a4fece28ecb9380a8a50e0a9b2e7e8`, accepted S5 checkpoint `86c3ce651025de9ccbe1094b161becc171514e69` adds exactly:

```text
SetupDiversityHistory.kt
SetupDiversitySelector.kt
SetupDiversitySelectorTest.kt
```

No existing production source was modified.

No changes occurred to:

- TB selector/scorer/models/data;
- NGJ production setup;
- App/Host;
- persistence/recovery;
- recommendation;
- shown identity.

## 6. TB semantics after S5

Do not assume generic S5 is a line-for-line extraction of the old TB selector.

Legacy TB rotation scoring includes:

```text
actual-role overlap novelty
same-Minions soft penalty
primary-style soft penalty
same Drunk shown-role soft penalty
```

Ownership after S4.5/S5:

```text
generic S5:
actual composition overlap
exact-repeat / overlap eligibility
history decay / novelty
seeded deterministic weighted selection

TB S7 adapter:
same-Minions diversity
primary-style diversity

retired:
Drunk shown-role repetition affecting actual-composition selection
```

S7 must preserve the frozen 480-template dataset, legality, player-count pools, Minion/style diversity, corrected overlap semantics and completion-history behavior, while deliberately dropping shown-role-to-preset weighting.

## 7. Campaign sequence

```text
MS-S0    ownership audit                                         COMPLETE
MS-S0.5  recovery scope reduction audit                          COMPLETE
MS-S1    CommittedClocktowerSetup + provenance                   COMPLETE / ACCEPTED
MS-S1R   exact setup persistence authority migration             COMPLETE / ACCEPTED
MS-S2    SetupCandidate/source/provider contracts                COMPLETE / ACCEPTED
MS-S3    optional TemplateRepository                             COMPLETE / ACCEPTED
MS-S4    deterministic generated actual-role source              COMPLETE / ACCEPTED
MS-S4.5  shown-identity ownership correction                    COMPLETE / ACCEPTED
MS-S5    actual-composition diversity history/scorer/selector   COMPLETE / ACCEPTED
MS-S6A   shown-identity policy/options boundary                 NEXT
MS-S6B   deterministic shown-identity commitment
MS-S6C   recommendation ownership inversion
MS-S7    TB 480-template controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

Do not collapse several slices merely because their final setup transaction is related.

## 8. MS-S6A immediate objective

S6A is strictly a **shown-identity option/policy resolution** slice.

Target concept:

```text
selected SetupCandidate
+ validated ruleset
+ candidate provenance
-> legal shown-identity options/policy
```

S6A answers what identities are legal/allowed. It does **not** choose one identity.

### S6A audit first

Before writing production code, audit:

1. TB `TroubleBrewingSetupPreset.drunkAsOptions` storage/validation and how selected template provenance can retrieve the matching metadata without putting it into `SetupCandidate`;
2. `ValidatedClocktowerRuleset` / canonical character registry APIs needed to identify current script Townsfolk for GENERATED candidates;
3. current Drunk stable external-id / role-id mapping conventions;
4. whether a generic policy should represent only current Drunk or a future-extensible role-to-shown-options map without overengineering;
5. failure semantics for malformed template metadata, missing required metadata and empty legal generated option pools.

### TEMPLATE direction

Use durable candidate provenance:

```text
providerId + candidateId
-> template shown-identity metadata
```

The generic core must not depend directly on `TroubleBrewingSetupPreset`.

TB's existing `exactly three drunkAsOptions` requirement remains TB validation, not a generic cardinality rule.

### GENERATED direction

For current Drunk semantics:

```text
validated script Townsfolk
- actual roles already in selected SetupCandidate
-> legal shown-role options
```

If a shown identity is required and this set is empty, fail closed.

Do not use the current NGJ legacy fallback that may select an in-play Townsfolk.

## 9. Strict S6A non-goals

Do not in S6A:

- select one shown role — S6B;
- seat/shuffle players;
- use shown-role history/cooldown;
- change S5 composition score/selection;
- emit recommendation decisions;
- change recommendation ownership — S6C;
- cut TB production flow — S7;
- cut NGJ production flow — S8;
- change persistence/recovery;
- edit App/Host merely to expose the new pure contract.

Do not modify `SetupCandidate` to carry shown-role options.

## 10. MS-S6B / S6C boundaries

### S6B

After S6A returns legal options, S6B deterministically selects/commits exactly one shown identity. Selection must be seed-stable, canonical-order independent and free of unseeded `.random()` / `.shuffled()`.

No history-based shown-role diversity is required in the first implementation.

### S6C

Recommendation reads the already committed `PlayerState.shownRole` as perceived identity.

```text
actual Drunk + shownRole X
-> generate only X-compatible information
-> never choose/replace X
```

Audit/reuse existing first-night information families before adding any new fake-information generator.

## 11. Protected predecessor invariants

Preserve:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter diversity/rotation history.
Completion persistence is retry-safe.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority and NGJ legality/current behavior until explicit migration.

## 12. Validation / workflow

Follow:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

For S6A, use the smallest durable typed contract before production implementation when a genuine new behavior boundary is introduced. Prefer pure Kotlin tests over source-string assertions.

Use the GitHub connector for safe small/medium docs/tests/source. Keep App/Host out of S6A.

Keep PR #61 Draft and do not merge, mark Ready, rebase or force-push without explicit authorization.

## 13. Documentation authority

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md
docs/MS_SETUP_RECOVERY_SCOPE_REDUCTION_AUDIT_2026-08-31.md
docs/MS_S1_COMMITTED_SETUP_CHECKPOINT_2026-08-31.md
docs/MS_S1R_SETUP_PERSISTENCE_CHECKPOINT_2026-08-31.md
docs/MS_S2_SETUP_PROVIDER_CONTRACT_CHECKPOINT_2026-08-31.md
docs/MS_S3_TEMPLATE_REPOSITORY_CHECKPOINT_2026-08-31.md
docs/MS_S4_GENERATED_SETUP_CANDIDATE_SOURCE_CHECKPOINT_2026-08-31.md
docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md
docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 14. Resume guard

Treat `86c3ce651025de9ccbe1094b161becc171514e69` as the accepted S5 code/test checkpoint unless a later production commit deliberately supersedes it.

At the next development turn:

1. re-query live `main`, branch, Draft PR #61 and checks;
2. distinguish docs-only carrier head from the accepted S5 code/test checkpoint;
3. read S4.5 and S5 checkpoints before designing S6A;
4. audit template metadata retrieval and generated Townsfolk-option derivation first;
5. implement options/policy only — no identity selection or recommendation changes;
6. keep PR #61 Draft and unmerged.
