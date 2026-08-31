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

MS-S6A accepted code/test checkpoint:
5823d66d0eb756a0005df86f1aea7db5902cae60

MS-S6B accepted code/test checkpoint:
d4cf3969aabcea7433b96b5b320171fbc821853e
```

MS-S6B validation:

```text
RED contract commit:
afc970cc9006ced2de24a99bcaa8d789a1d7a11a
CI #1256 / run 33365203015   EXPECTED RED
:app:testFast reached compileDebugUnitTestKotlin and failed on missing S6B commitment types
R2 #1173 / run 33365203021   SUCCESS

GREEN checkpoint:
d4cf3969aabcea7433b96b5b320171fbc821853e
CI #1257 / run 33365333667   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1174 / run 33365333672   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Later documentation commits are carriers and do not replace the accepted MS-S6B production checkpoint.

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
MS-S6A   shown-identity policy/options boundary               COMPLETE / ACCEPTED
MS-S6B   deterministic shown-identity commitment              COMPLETE / ACCEPTED
MS-S6C   recommendation ownership inversion                   NEXT
```

Active handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_MS_SETUP_ARCHITECTURE.md`

Latest accepted checkpoint:

`docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md`

Architecture correction authority:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

## 2. MS-SETUP target architecture

The setup pipeline remains frozen around:

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
-> MS-S6A resolve legal shown-identity options/policy          [COMPLETE]
-> MS-S6B deterministically commit shown identity              [COMPLETE]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> setup/first-night recommendation reads committed shownRole  [MS-S6C NEXT]
-> recommendation generates information only
```

`CommittedClocktowerSetup` is the immutable exact initial setup fact. Persistence/recovery is an outer consumer and must not be a dependency of candidate generation, composition selection, shown-identity legality, identity commitment or later materialization.

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

`SetupCandidate` remains deliberately a canonical **pre-seat actual-role multiset** with no seating, shown identity, persistence schema or diversity history.

Accepted contracts:

```text
SetupCandidate
SetupCandidateRequest
SetupCandidateSource
ClocktowerSetupProvider
ClocktowerSetupProviderRegistry
```

`SetupCandidateRequest` already carries `setupSeed`; S6B did not add seed or identity state to `SetupCandidate`.

### MS-S3 — optional TemplateRepository

`TemplateRepository` remains actual-role candidate storage/lookup only:

```text
TemplateBucketKey(script, playerCount)
TemplateRepository.find(script, playerCount)
```

It remains seed/diversity/shown-identity independent. Template-specific shown-identity metadata is reached through the separate S6A provenance-keyed policy source rather than by expanding `SetupCandidate`.

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

### S5 history and scoring

`SetupDiversityRecord` stores only script, canonical actual-role multiset and derived player count. History is filtered by exact script + player count before age weighting.

Roles/role occurrences common to every candidate in the current pool are removed from overlap scoring automatically, so fixed roles such as TB's Imp do not artificially inflate similarity.

Scoring uses integer fixed-point arithmetic (`1_000_000` scale), exact-repeat/overlap eligibility, age-weighted novelty, a minimum novelty floor and deterministic MurmurHash-based weighted selection independent of caller list order.

S5 contains no shown role, `drunkAsOptions`, clue data, recommendation decisions, TB style metadata, seating or persistence schema.

### S5 deliberate exclusions

TB same-Minions and primary-style diversity remain S7 adapter concerns.

The old selected/repeated Drunk shown role affecting actual-composition weight is intentionally retired and must not be reintroduced.

MS-S5 does not edit or wire TB/NGJ/App/Host production flow.

## 5. MS-S6A — COMPLETE / ACCEPTED

Authoritative checkpoint:

`docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md`

Accepted production files:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicy.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingShownIdentityPolicySource.kt
```

Accepted typed contract:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicyResolverTest.kt`

Accepted generic boundary:

```text
selected SetupCandidate
+ ValidatedClocktowerRuleset
+ template metadata lookup by durable provenance when sourceKind == TEMPLATE
-> SetupShownIdentityPolicy
```

### S6A policy representation

```text
ShownIdentityOverrideOptions
├─ actualRole
└─ canonical legalShownRoles

SetupShownIdentityPolicy
└─ overrides: 0..N
```

An empty override list is the explicit no-override policy.

The representation is future-extensible, but current resolution intentionally supports the existing Drunk setup-time identity mechanic only. Unsupported or inconsistent metadata fails closed.

S6A exposes legal options only. It does **not** choose or commit a shown identity.

### S6A TEMPLATE ownership

Template lookup uses:

```text
TemplateShownIdentityPolicyKey(providerId, candidateId)
-> TemplateShownIdentityPolicySource
-> normalized generic SetupShownIdentityPolicy
```

TB adapts existing preset metadata at the edge through `TroubleBrewingShownIdentityPolicySource`.

Current TB keying is:

```text
datasetId as providerId
+ preset.id as candidateId
```

TB `drunkAsOptions` remains validated by `TroubleBrewingSetupPresetValidator`. Its exactly-three requirement remains TB-specific rather than becoming a generic cardinality rule.

The generic resolver does not depend on `TroubleBrewingSetupPreset`.

Unknown candidate IDs and cross-provider provenance fail clearly rather than silently falling back.

### S6A GENERATED ownership

For current Drunk semantics:

```text
validated script Townsfolk
- selected candidate actualRoles
-> canonical legal shown-role options
```

If the selected generated candidate has no Drunk, S6A returns the explicit no-override policy.

If Drunk is present but no unused Townsfolk remains, S6A fails closed. It never falls back to an actual in-play Townsfolk.

Options are canonicalized independently of ruleset/input order and remain an option pool; S6A contains no seed/hash/random shown-role selection.

### S6A audit finding about TB cutover

There is still no production TB `TroubleBrewingSetupPreset -> SetupCandidate` cutover path. Current TB production remains the legacy preset-selection/deal pipeline.

S6A therefore added only the shown-identity metadata edge adapter. Actual TB candidate adaptation and controlled production cutover remain S7 responsibilities.

## 6. MS-S6B — COMPLETE / ACCEPTED

Authoritative checkpoint:

`docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md`

Accepted production file:

`app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitment.kt`

Accepted typed contract:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitterTest.kt`

Accepted generic boundary:

```text
selected SetupCandidate
+ resolved SetupShownIdentityPolicy
+ setupSeed
-> SetupShownIdentityCommitment
```

### S6B commitment representation

```text
ShownIdentityCommitment
├─ actualRole
└─ shownRole

SetupShownIdentityCommitment
├─ canonical override facts: 0..N
└─ shownRoleFor(actualRole)
```

Only actual roles whose shown identity differs are stored. If no override is committed:

```text
shownRoleFor(actualRole) == actualRole
```

This is deliberately pre-seat and does not create `CommittedSetupSeat` or `CommittedClocktowerSetup`.

### S6B deterministic selection

The generic hash namespace is:

```text
setup-shown-identity-v1
```

Stable hash material includes script, source kind, provider/candidate provenance, canonical actual-role composition, override actual role, canonical legal option pool and `setupSeed`.

Fields are length-prefixed before hashing. Selection uses `MurmurHash3.low64Utf8` and unsigned remainder into the canonical S6A option list.

Therefore:

- same candidate + policy + setupSeed -> same commitment;
- input order does not affect selection;
- different setup seeds can explore different legal options;
- single-option policy commits directly;
- no unseeded random/shuffle participates;
- no shown-role history/cooldown participates.

### S6B fail-closed contract

S6B consumes legality rather than redefining it, but rejects inconsistent candidate/policy combinations:

- override actual role must exist in the selected candidate;
- override cannot show itself as itself;
- a shown option cannot already be an actual in-play role.

There is no silent fallback.

### S6B exact scope

Compared with pre-S6B docs carrier `7b68df45c44bfec8afdc545e637e5465c1dc08e0`, accepted S6B checkpoint `d4cf3969aabcea7433b96b5b320171fbc821853e` adds exactly one production file and one typed test file.

No existing production source was modified.

S6B did not modify:

- `SetupCandidate` / `SetupCandidateRequest`;
- S5 composition diversity;
- S6A legality resolution;
- legacy TB selector/scorer/deal flow;
- NGJ production flow;
- seat/deal materialization;
- `CommittedClocktowerSetup`;
- `PlayerState.shownRole`;
- recommendation / `StorytellerDecision.DrunkShownRole`;
- persistence/recovery;
- App/Host.

Legacy TB's `tb-drunk-v1` namespace is not generic S6B authority; generic commitment is now downstream of S6A under `setup-shown-identity-v1`.

## 7. Corrected Trouble Brewing parity definition

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
- shown identity is selected only after the actual-role candidate is selected;
- generic shown-identity commitment uses the S6B namespace rather than treating legacy `tb-drunk-v1` as generic authority.

This is intentional semantic correction, not a parity regression.

## 8. Remaining implementation campaign

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
MS-S6A   generic shown-identity policy/options boundary         COMPLETE / ACCEPTED
MS-S6B   deterministic shown-identity commitment               COMPLETE / ACCEPTED
MS-S6C   recommendation ownership inversion                    NEXT
MS-S7    TB 480-template adapter + controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

Do not collapse several slices merely because they share the end-to-end setup flow.

## 9. MS-S6C immediate objective — NEXT, NOT STARTED

S6C changes recommendation ownership only after S6B has committed identity.

Target:

```text
actual Drunk + already committed shownRole X
-> recommendation consumes X as perceived identity
-> generate only X-compatible information
-> never choose, replace or reroll X
```

### S6C audit first

Before production changes, audit:

1. `StorytellerDecision.DrunkShownRole` producers and all consumers;
2. recommendation/setup paths that currently choose a Drunk shown role;
3. `PlayerState.shownRole` availability at recommendation boundaries;
4. existing first-night information families that can operate from an already-committed perceived role;
5. tests whose current contract still assumes recommendation owns shown identity;
6. the smallest typed replacement evidence required before narrowing/removing legacy recommendation-owned identity concepts.

S6C should reuse existing information generation where practical. Do not invent a second setup selector or a new fake-information subsystem merely to complete ownership inversion.

### S6C strict non-goals

Do not in S6C:

- change `SetupCandidate` or S5 composition selection;
- change S6A legal option resolution;
- change S6B commitment selection;
- let recommendation feed back into setup identity;
- perform TB production cutover — S7;
- perform NGJ production cutover — S8;
- perform seat/deal integration unless explicitly separated into a later setup materialization slice;
- change persistence/recovery;
- expand App/Host beyond what an explicit integration slice owns.

Do not delete `StorytellerDecision.DrunkShownRole` merely because S6B exists; retire or narrow it only after S6C consumer audit and stable replacement evidence.

## 10. MS-S7 / MS-S8 target cutovers

### MS-S7 — Trouble Brewing

```text
480 validated templates
-> template SetupCandidate values
-> MS-S5 composition selector
-> selected candidate
-> S6A template shown-identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

S7 owns adapting TB-specific Minion/style diversity around the generic S5 composition boundary and retiring `selectedDrunkShownRole -> preset finalWeight` coupling.

### MS-S8 — NGJ/no-template

```text
GeneratedSetupCandidateSource
-> MS-S5
-> S6A generated shown options
-> S6B deterministic identity commitment
-> seat/deal materialization
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

Retire legacy unseeded composition/shown-role selection and recommendation-time shown-role replacement only at the explicit controlled cutovers.

## 11. Protected predecessor correctness

Preserve throughout migration:

```text
TB actual roles originate from selected/committed setup.
Baron/setup modifiers are not applied twice.
Drunk actual identity remains Drunk.
Drunk shown identity is committed once and cannot be replaced by recommendation.
S5 actual-composition selection cannot consume shown identity.
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
Start commits setup only once; recomposition/navigation cannot reroll it.
Restore never reselects/rerolls an already committed setup.
Invalid template data never silently falls back to broad-random setup.
Background work cannot mutate committed identities.
Only true completed games enter setup diversity/rotation history.
Completion persistence is retry-safe and records the original committed setup summary.
```

Also preserve Dawn/Dusk retry convergence, Fortune Teller current/effective-state authority, poisoned Spy fail-safe semantics, current living-Demon UI authority and NGJ setup legality until explicit migration.

## 12. Testing cadence

Follow root `AGENTS.md`, `docs/TESTING_STRATEGY.md`, and `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

Use risk-based evidence:

- durable behavior/architecture contracts get the smallest valuable typed test;
- existing tests count as evidence;
- obsolete tests may be retired when deliberately superseded and stable replacement evidence exists;
- do not manufacture source-string RED when typed behavior proof is practical;
- T0 is the smallest directly relevant evidence;
- `:app:testFast` is the logical-checkpoint T1 gate;
- T4 remains explicit full-acceptance/merge-level validation rather than a micro-slice default.

S6B is accepted at `d4cf3969aabcea7433b96b5b320171fbc821853e` with CI #1257 / run `33365333667` and R2 #1174 / run `33365333672`.

S6C changes recommendation semantics/ownership and should use the affected recommendation evidence selected by `docs/TESTING_STRATEGY.md`; escalate beyond FAST when the concrete consumer audit shows a triggered T2/T3 family.

## 13. Writer / scope rules

Safe small/medium docs/tests/source should continue through the GitHub connector.

Keep pure setup-domain work out of App/Host. Do not edit App/Host merely to expose a new seam before an explicit integration slice owns that wiring.

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit authorization.

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
docs/MS_S4_GENERATED_SETUP_CANDIDATE_SOURCE_CHECKPOINT_2026-08-31.md
docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md
docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md
docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md
docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 15. New-conversation resume protocol

1. read root `AGENTS.md`;
2. read this roadmap, active handoff, S4.5 correction, S5, S6A and S6B checkpoints;
3. re-query live `main`, branch, Draft PR #61 and current checks;
4. distinguish later docs-only carrier head from accepted S6B code/test checkpoint `d4cf3969aabcea7433b96b5b320171fbc821853e`;
5. next production slice is S6C recommendation ownership inversion only;
6. treat S6B shown identity as already committed setup input; recommendation must not choose/replace it;
7. audit legacy `StorytellerDecision.DrunkShownRole` before deletion/narrowing;
8. do not perform TB/NGJ production cutovers before S7/S8;
9. do not perform broad unfinished-night cleanup inside MS-SETUP; REC-R1 owns that later work;
10. keep PR #61 Draft and unmerged unless explicitly authorized otherwise.

## 16. Deferred / queued work registry

| Area | Status |
|---|---|
| MS-SETUP generic multi-script setup architecture | CURRENT — MS-S6B ACCEPTED / MS-S6C NEXT |
| MS-S1R setup persistence authority migration | COMPLETE / ACCEPTED |
| MS-S2 generic candidate/provider contracts | COMPLETE / ACCEPTED |
| MS-S3 optional template repository | COMPLETE / ACCEPTED |
| MS-S4 deterministic generated source | COMPLETE / ACCEPTED |
| MS-S4.5 shown-identity ownership correction | COMPLETE / ACCEPTED |
| MS-S5 actual-composition diversity selector | COMPLETE / ACCEPTED |
| MS-S6A shown-identity policy/options boundary | COMPLETE / ACCEPTED |
| MS-S6B deterministic shown-identity commitment | COMPLETE / ACCEPTED |
| REC-R1 unfinished-game recovery simplification | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid actual wake-history authority | DEFERRED FOLLOW-UP |
| GCR-5 night checkpoint stable identity hardening | DEFERRED; re-evaluate under REC-R1 |
| GCR-5 reconstructor naming clarity | DEFERRED; re-evaluate under REC-R1 |
| Dawn systematic crash cut-point matrix | DEFERRED; committed-state convergence remains relevant |
| A3 immutable setup snapshot ownership/persistence | SUPERSEDED BY MS-S1/MS-S1R |
| App Root S9.2 Active Game Persistence Boundary | SUPERSEDED IN SCOPE BY MS-S1R + REC-R1 |
