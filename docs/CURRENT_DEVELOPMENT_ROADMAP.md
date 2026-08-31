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

MS-S4 accepted code/test checkpoint:
6de0e8c99c89a091615c513255adbdb773b3cc69

MS-S5 accepted code/test checkpoint:
86c3ce651025de9ccbe1094b161becc171514e69
```

MS-S5 validation:

```text
RED contract commit:
d0145d2347490aa4b7b1f037188e1204cfac3832
CI #1243 / run 33362653627   EXPECTED RED — compile failed on missing S5 types
R2 #1160 / run 33362653634   SUCCESS

GREEN checkpoint:
86c3ce651025de9ccbe1094b161becc171514e69
CI #1245 / run 33362804682   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1162 / run 33362804691   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Later documentation commits are carriers and do not replace the accepted MS-S5 production checkpoint.

Current campaign status:

```text
MS-S0    ownership audit                                      COMPLETE
MS-S0.5  recovery scope reduction audit                       COMPLETE
MS-S1    CommittedClocktowerSetup + provenance                COMPLETE / ACCEPTED
MS-S1R   setup persistence authority migration                COMPLETE / ACCEPTED
MS-S2    candidate/source/provider contracts                  COMPLETE / ACCEPTED
MS-S3    optional TemplateRepository                          COMPLETE / ACCEPTED
MS-S4    deterministic generated actual-role source           COMPLETE / ACCEPTED
MS-S4.5  shown-identity ownership architecture correction    COMPLETE / ACCEPTED
MS-S5    actual-composition diversity/scorer/selector         COMPLETE / ACCEPTED
MS-S6A   shown-identity policy/options boundary               NEXT
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

Latest accepted checkpoint:

`docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md`

Architecture correction authority:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

## 2. MS-SETUP target architecture

The setup pipeline is frozen around:

```text
Composition
-> Identity
-> Information
```

Current target flow:

```text
script + playerCount + setupSeed
-> resolve script/ruleset setup provider
-> query optional template candidates or legal generated candidates
-> MS-S5 select one candidate using ACTUAL-COMPOSITION diversity only
-> MS-S6A resolve legal shown-identity options/policy
-> MS-S6B deterministically commit shown identity
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> setup/first-night recommendation reads committed shownRole
-> recommendation generates information only
```

`CommittedClocktowerSetup` is the immutable exact initial setup fact. Persistence/recovery is an outer consumer and must not be a dependency of candidate generation, composition selection, shown-identity policy/selection or setup commitment.

The App root must not gain new script-specific setup branches when future scripts are added.

### Frozen ownership rule

Shown identity is a setup fact, not a recommendation output.

Recommendation may consume `PlayerState.shownRole`, but may not select, replace, reroll or optimize the shown identity itself.

## 3. Accepted foundation through MS-S4.5

### MS-S1 — CommittedClocktowerSetup

```text
CommittedClocktowerSetup
├─ script
├─ setupSeed
├─ ordered seats
│  ├─ actualRole
│  └─ shownRole
└─ provenance
```

Exact committed identities are authority; provenance is audit/source metadata only.

### MS-S1R — exact persistence authority

```text
persist exact CommittedClocktowerSetup
-> direct decode/validate
-> restore exact actual/shown identities
```

Restore never reruns setup selection or recommendation.

### MS-S2 — candidate/provider contracts

`SetupCandidate` is deliberately a canonical **pre-seat actual-role multiset** with no seating, shown identity, persistence schema or diversity history.

Accepted contracts:

```text
SetupCandidate
SetupCandidateRequest
SetupCandidateSource
ClocktowerSetupProvider
ClocktowerSetupProviderRegistry
```

### MS-S3 — optional TemplateRepository

`TemplateRepository` remains actual-role candidate storage/lookup only:

```text
TemplateBucketKey(script, playerCount)
TemplateRepository.find(script, playerCount)
```

It remains seed/diversity/shown-identity independent. Template-specific shown-identity metadata will be reached through a companion provenance-keyed policy/metadata boundary rather than by expanding `SetupCandidate`.

### MS-S4 — GeneratedSetupCandidateSource

`GeneratedSetupCandidateSource` owns deterministic legal actual-role generation only. It preserves the 5–15 base distribution, one capped Baron `+2 Outsider` adjustment, stable provider provenance and no unseeded random/shuffle behavior.

It does not own seating, shown identity, diversity/history, persistence, UI or App/Host production wiring.

### MS-S4.5 — ownership correction

Authoritative checkpoint:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

The corrected rule is:

> actual-role composition selection must be independent of shown-identity metadata/history.

Legacy TB selected-Drunk-shown-role weighting is deliberately not part of future composition parity.

## 4. MS-S5 — COMPLETE / ACCEPTED

Authoritative checkpoint:

`docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md`

Accepted production files:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversityHistory.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelector.kt
```

Accepted typed contract:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupDiversitySelectorTest.kt`

Accepted generic boundary:

```text
legal SetupCandidate values
+ SetupDiversityHistory(actual-role composition only)
+ SetupDiversityPolicy
+ deterministic selection seed
-> one selected SetupCandidate
```

### S5 history

`SetupDiversityRecord` stores only:

```text
script
canonical actual-role multiset
derived playerCount
```

History is filtered by exact script + player count before age weighting. It contains no seating, shown role, `drunkAsOptions`, clue data, recommendation decisions, TB style metadata or persistence schema.

### S5 invariant-role exclusion

Roles/role occurrences common to every candidate in the current pool are removed from overlap scoring automatically.

This keeps fixed roles such as TB's Imp from artificially increasing similarity while keeping the generic core independent of character types and script-specific role knowledge.

### S5 deterministic policy

Scoring uses integer fixed-point arithmetic (`1_000_000` scale), not floating-point selection weights.

Generic policy supports:

- age-weighted actual-composition novelty;
- exact-repeat modes `ALLOW`, `REJECT_WHEN_ALTERNATIVE`, `REJECT`;
- optional last-game max-overlap eligibility;
- `0.05` fixed-point fallback increments to the first non-empty overlap level;
- minimum novelty floor;
- deterministic MurmurHash-based weighted selection independent of caller list order.

Single generated candidates with no template `candidateId` remain valid S5 inputs.

### S5 deliberate exclusions

MS-S5 does not copy the full TB rotation scorer.

The following remain TB adapter concerns for S7:

- same-Minions soft diversity semantics;
- primary-style soft diversity semantics.

The following old semantic is intentionally retired by the S4.5 architecture correction and must not be reintroduced:

- selected/repeated Drunk shown role changing actual-composition weight.

MS-S5 does not edit or wire TB/NGJ/App/Host production flow.

## 5. Corrected Trouble Brewing parity definition

Trouble Brewing Setup Presets remains a protected predecessor, but parity is explicitly scoped.

Must preserve at S7:

- frozen 480-preset dataset;
- template legality and player-count pools;
- actual-role composition semantics;
- exact-repeat policy where still applicable;
- actual-role overlap/novelty behavior;
- Minion-set diversity;
- style diversity;
- Baron/TB composition legality;
- `drunkAsOptions` legal metadata;
- deterministic deal/commit behavior under the corrected pipeline;
- true-completion history gating and accepted durability behavior.

Deliberately allowed to change:

- repeated Drunk shown role no longer alters actual-role preset weight;
- exact legacy seed/history -> preset identity is not required where the old result depended on shown-role weighting;
- shown identity is selected only after the actual-role candidate is selected.

This is intentional semantic correction, not a parity regression.

## 6. Remaining implementation campaign

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
MS-S6A   generic shown-identity policy/options boundary         NEXT
MS-S6B   deterministic shown-identity commitment
MS-S6C   recommendation ownership inversion
MS-S7    TB 480-template adapter + controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

Do not collapse several slices merely because they share the end-to-end setup flow.

## 7. MS-S6A immediate objective

The next production slice is **shown-identity policy/options resolution only**:

```text
selected SetupCandidate
+ validated ruleset
+ candidate provenance
-> legal shown-identity options/policy
```

S6A must answer what shown identities are legal/allowed for the already-selected actual composition. It must not select or commit one option yet.

### Template direction

A TEMPLATE candidate must be able to use durable `(providerId, candidateId)` provenance to find template-specific shown-identity metadata such as TB `drunkAsOptions`.

Do not add shown-role fields to `SetupCandidate` merely to transport this metadata.

Template-specific cardinality constraints, such as TB's exactly-three options, stay in the template validator/adapter rather than becoming generic rules.

### Generated direction

For the current Drunk mechanic, a GENERATED candidate should derive legal Townsfolk shown options from the validated script/ruleset minus actual in-play roles.

If a required shown identity has no legal option, fail closed. Never fall back to an actual in-play Townsfolk merely to continue setup.

### S6A strict non-goals

Do not in S6A:

- choose one shown role — S6B;
- use shown-role diversity/history — not required in first generic version;
- change recommendation — S6C;
- cut TB production over — S7;
- cut NGJ production over — S8;
- alter S5 composition selection;
- edit persistence/recovery;
- expand App/Host setup responsibility.

## 8. MS-S6B / S6C future boundaries

### MS-S6B — deterministic commitment

Select one legal shown identity using stable seeded deterministic logic after composition selection. No unseeded random/shuffle and no recommendation participation.

History-based shown-identity rotation is not required for the first generic implementation. If later justified, it may only rank legal shown options after composition selection and must never feed back into S5.

### MS-S6C — recommendation ownership inversion

Recommendation must treat `PlayerState.shownRole` as the perceived-identity input fact.

Target:

```text
actual Drunk + shownRole = X
-> generate only X-compatible information behavior
-> never output/replace X
```

Audit/reuse existing first-night role-information families before introducing new fake-information algorithms.

Legacy recommendation-owned Drunk identity concepts should retire/narrow only after typed replacement evidence and consumer audits.

## 9. MS-S7 / MS-S8 target cutovers

### MS-S7 — Trouble Brewing

```text
480 validated templates
-> template SetupCandidate values
-> MS-S5 composition selector
-> selected candidate
-> template shown-identity metadata
-> MS-S6 identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

S7 owns adapting TB-specific Minion/style diversity around the generic S5 composition boundary and retiring `selectedDrunkShownRole -> preset finalWeight` coupling.

### MS-S8 — NGJ/no-template

```text
GeneratedSetupCandidateSource
-> MS-S5
-> MS-S6 generated shown options
-> deterministic identity commitment
-> seat/deal materialization
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

Retire legacy unseeded composition/shown-role selection and recommendation-time shown-role replacement only at this explicit cutover.

## 10. Protected predecessor correctness

Preserve throughout migration:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup summary.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority and NGJ setup legality until explicit migration.

## 11. Testing cadence

Follow root `AGENTS.md`, `docs/TESTING_STRATEGY.md`, and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Use risk-based evidence:

- durable behavior/architecture contracts get the smallest valuable typed test;
- existing tests count as evidence;
- obsolete tests may be retired when deliberately superseded and stable replacement evidence exists;
- do not manufacture source-string RED when typed behavior proof is practical;
- T0 is the smallest directly relevant evidence;
- `:app:testFast` is the logical-checkpoint T1 gate;
- T4 remains explicit full-acceptance/merge-level validation rather than a micro-slice default.

S5 is accepted at `86c3ce651025de9ccbe1094b161becc171514e69` with CI #1245 / run `33362804682` and R2 #1162 / run `33362804691`.

## 12. Writer / scope rules

Safe small/medium docs/tests/source should continue through the GitHub connector.

Keep S6A pure Kotlin where practical. Do not edit App/Host production flow merely to wire an incomplete shown-identity policy early.

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit authorization.

## 13. Current documentation authority

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

## 14. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap, active handoff, S4.5 correction and S5 checkpoint;
3. re-query live `main`, branch, Draft PR #61 and current checks;
4. distinguish later docs-only carrier head from accepted S5 code/test checkpoint `86c3ce651025de9ccbe1094b161becc171514e69`;
5. next production slice is S6A shown-identity policy/options only;
6. do not select/commit shown identity before S6B;
7. do not change recommendation before S6C;
8. do not perform TB/NGJ production cutovers before S7/S8;
9. do not perform broad unfinished-night cleanup inside MS-SETUP; REC-R1 owns that later work;
10. keep PR #61 Draft and unmerged unless explicitly authorized otherwise.

## 15. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — MS-S5 ACCEPTED / MS-S6A NEXT |
| MS-S1R setup persistence authority migration | COMPLETE / ACCEPTED |
| MS-S2 generic candidate/provider contracts | COMPLETE / ACCEPTED |
| MS-S3 optional template repository | COMPLETE / ACCEPTED |
| MS-S4 deterministic generated source | COMPLETE / ACCEPTED |
| MS-S4.5 shown-identity ownership correction | COMPLETE / ACCEPTED |
| MS-S5 actual-composition diversity selector | COMPLETE / ACCEPTED |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| GCR-5 reconstructor naming clarity | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| A3 immutable setup snapshot ownership/persistence | SUPERSEDED BY MS-S1/MS-S1R |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1 |
