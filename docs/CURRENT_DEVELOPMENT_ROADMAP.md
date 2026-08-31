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
```

MS-S4 validation remains:

```text
CI #1236 / run 33359464789   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1153 / run 33359464788   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Later documentation commits are carriers and do not replace the accepted production checkpoint.

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
MS-S5    actual-composition diversity/scorer/selector         NEXT
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

New S4.5 authority:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

## 2. MS-SETUP target architecture

The setup pipeline is now frozen around the causal order:

```text
Composition
-> Identity
-> Information
```

Full target flow:

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

`CommittedClocktowerSetup` is the immutable exact initial setup fact. Persistence/recovery is an outer consumer and must not be a dependency of candidate generation, selection, shown-identity choice or setup commitment.

The App root must not gain new script-specific setup branches when future scripts are added.

### Frozen ownership rule

Shown identity is a setup fact, not a recommendation output.

Recommendation may consume `PlayerState.shownRole`, but may not select, replace, reroll or optimize the shown identity itself.

## 3. Accepted foundation through MS-S4

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

Accepted contracts remain:

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

It remains seed/diversity/shown-identity independent. Template-specific shown-identity metadata will be reached later through a companion policy/metadata boundary rather than by expanding `SetupCandidate`.

### MS-S4 — GeneratedSetupCandidateSource

Accepted production source:

`app/src/main/java/com/codex/campboardgamehost/clocktower/setup/GeneratedSetupCandidateSource.kt`

It owns only deterministic legal actual-role generation. It preserves the 5–15 base distribution, one capped Baron `+2 Outsider` adjustment, stable provider provenance, and no unseeded random/shuffle behavior.

It does not own seating, shown identity, diversity/history, persistence, UI or App/Host production wiring.

## 4. MS-S4.5 architecture correction — COMPLETE / ACCEPTED

Authoritative checkpoint:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

Global audit findings frozen by S4.5:

- TB preset metadata already carries `drunkAsOptions` with strict validator coverage;
- legacy TB setup scoring currently lets selected Drunk shown role affect preset weight;
- generic recommendation history also treats Drunk shown identity as a selectable recommendation dimension;
- `PlayerState.shownRole` already exists and `PlayerCard -> GameState` already carries committed shown identity into recommendation input;
- `TroubleBrewingSetupRecommendationLock` is therefore a migration bridge that turns an existing input fact back into a locked recommendation output;
- first-night information already has role-family infrastructure that should be reused rather than duplicated.

The corrected rule is:

> actual-role composition selection must be independent of shown-identity metadata/history.

## 5. Corrected TB parity definition

Trouble Brewing Setup Presets remains a protected predecessor, but `preserve TB behavior/parity` is now explicitly scoped.

Must preserve:

- frozen 480-preset dataset;
- template legality and player-count pools;
- actual-role composition semantics;
- exact-repeat policy where still applicable;
- actual-role overlap/novelty scoring;
- Minion-set diversity;
- style diversity;
- Baron/TB composition legality;
- `drunkAsOptions` legal metadata;
- deterministic deal/commit behavior after the corrected pipeline;
- true-completion history gating and accepted durability behavior.

Deliberately allowed to change:

- repeated Drunk shown role no longer alters actual-role preset weight;
- exact legacy seed/history -> preset identity is not required where the old result depended on that shown-role weighting;
- shown identity is selected only after the actual-role candidate is selected.

This is an intentional semantic correction, not a parity regression.

## 6. Remaining implementation campaign

```text
MS-S0   fresh live-state + TB/NGJ/setup ownership audit                            COMPLETE
MS-S0.5 recovery scope reduction audit + product boundary                          COMPLETE
MS-S1   generic persistence-independent CommittedClocktowerSetup + provenance      COMPLETE / ACCEPTED
MS-S1R  exact setup persistence authority migration + TB restore retirement         COMPLETE / ACCEPTED
MS-S2   generic SetupCandidate + source/provider registry contracts                 COMPLETE / ACCEPTED
MS-S3   optional TemplateRepository keyed by script + player count                  COMPLETE / ACCEPTED
MS-S4   deterministic seeded legal GeneratedSetupCandidateSource                    COMPLETE / ACCEPTED
MS-S4.5 shown-identity ownership architecture correction                           COMPLETE / ACCEPTED
MS-S5   actual-composition SetupDiversityHistory / scorer / selector                NEXT
MS-S6A  generic shown-identity policy/options boundary
MS-S6B  deterministic shown-identity commitment
MS-S6C  recommendation ownership inversion
MS-S7   adapt TB 480-template pipeline under corrected parity semantics
MS-S8   adapt NGJ/no-template production path
MS-S9   acceptance: future no-template script needs no App-root setup branch

REC-R1  separate future unfinished-game stable-checkpoint simplification
```

Do not implement several slices at once merely because they share the campaign.

## 7. MS-S5 immediate objective

The next production slice is **actual-composition diversity only**:

```text
legal SetupCandidate values
+ actual-composition diversity history
+ deterministic selection seed/context
-> one selected SetupCandidate
```

MS-S5 may rank/choose legal candidates but must not become a second legality engine.

### MS-S5 forbidden inputs/responsibilities

Do not consume or score:

- `drunkAsOptions`;
- selected Drunk shown role;
- shown-role history;
- `PlayerState.shownRole`;
- first-night clue candidates;
- setup recommendation decisions.

Do not perform:

- seat assignment/shuffle;
- shown-identity commitment;
- Baron/setup-modifier reapplication;
- TB/NGJ production cutover;
- persistence changes;
- App/Host feature expansion.

Key durable invariant:

> Changing shown-identity metadata/history must not change MS-S5 actual-role candidate selection.

## 8. MS-S6 corrected decomposition

### MS-S6A — shown-identity policy/options

Conceptual ownership:

```text
selected SetupCandidate
+ validated ruleset
+ provenance
-> legal shown-identity options/policy
```

Template candidates resolve template-specific metadata through durable `(providerId, candidateId)` identity. Generated candidates derive legal current Drunk options from script/ruleset Townsfolk minus actual in-play roles.

Do not expand `SetupCandidate` with shown-role fields.

If a required shown identity has no legal options, fail closed.

### MS-S6B — deterministic commitment

Select one legal shown identity using stable seeded deterministic logic after composition selection. No unseeded random/shuffle and no recommendation participation.

History-based shown-identity rotation is not required for the first generic implementation. If later justified, it may only rank legal shown options after composition selection and must never feed back into MS-S5.

### MS-S6C — recommendation ownership inversion

Recommendation must treat `PlayerState.shownRole` as the perceived-identity input fact.

Target:

```text
actual Drunk + shownRole = X
-> generate only X-compatible information behavior
-> never output/replace X
```

Audit and reuse existing first-night role-information families before introducing new fake-information algorithms.

Legacy recommendation-owned Drunk identity concepts should be retired/narrowed only after typed replacement evidence and consumer audits.

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

Retire `selectedDrunkShownRole -> preset finalWeight` coupling.

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

Generated Drunk shown identity must be a legal script Townsfolk not already actually in play; do not use a broad in-play fallback.

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
- obsolete tests may be retired when their behavior is deliberately superseded and stable replacement evidence exists;
- do not manufacture source-string RED when typed behavior proof is practical;
- T0 is the smallest directly relevant evidence;
- `:app:testFast` is the logical-checkpoint T1 gate;
- T4 remains explicit full-acceptance/merge-level validation rather than a micro-slice default.

MS-S4.5 itself is docs-only and requires no manufactured Android test run. Normal docs-only GitHub routing is sufficient validation for its carrier commits.

## 12. Writer / scope rules

Safe small/medium docs/tests/source should continue through the GitHub connector.

Do not expand MS-S5 into App/Host production wiring. Do not edit `ClocktowerHostScreen.kt` for this campaign slice merely because setup state is visible there.

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
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 14. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap, active handoff and S4.5 checkpoint;
3. re-query live `main`, `codex/ms-setup-generic-architecture`, Draft PR #61 and checks;
4. distinguish docs-only carrier head from accepted MS-S4 production checkpoint `6de0e8c99c89a091615c513255adbdb773b3cc69`;
5. next production slice is MS-S5 actual-composition diversity only;
6. do not pull shown-identity work forward from S6A/S6B/S6C;
7. do not perform TB/NGJ production cutovers before S7/S8;
8. do not perform broad unfinished-night cleanup inside MS-SETUP; REC-R1 owns that later work;
9. keep PR #61 Draft and unmerged unless explicitly authorized otherwise.

## 15. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — MS-S4.5 ACCEPTED / MS-S5 NEXT |
| MS-S1R setup persistence authority migration | COMPLETE / ACCEPTED |
| MS-S2 generic candidate/provider contracts | COMPLETE / ACCEPTED |
| MS-S3 optional template repository | COMPLETE / ACCEPTED |
| MS-S4 deterministic generated source | COMPLETE / ACCEPTED |
| MS-S4.5 shown-identity ownership correction | COMPLETE / ACCEPTED |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| GCR-5 reconstructor naming clarity | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| A3 immutable setup snapshot ownership/persistence | SUPERSEDED BY MS-S1/MS-S1R |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1 |
