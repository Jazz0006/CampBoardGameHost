# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S6A COMPLETE / ACCEPTED — MS-S6B NEXT**

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

MS-S6A:
5823d66d0eb756a0005df86f1aea7db5902cae60
```

MS-S6A validation:

```text
RED:
50a04d41a1bda6cc1c0a0b87b88a5135d521979d
CI #1249 / run 33364240383   EXPECTED RED
:app:testFast reached compileDebugUnitTestKotlin and failed on missing S6A production types
R2 #1166 / run 33364240491   SUCCESS

GREEN:
5823d66d0eb756a0005df86f1aea7db5902cae60
CI #1252 / run 33364442563   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1169 / run 33364442584   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Authoritative S6A checkpoint:

`docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md`

S4.5 architecture correction remains authoritative:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

Later docs-only carrier commits do not replace accepted S6A production checkpoint `5823d66d0eb756a0005df86f1aea7db5902cae60`.

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
-> shown-identity policy/options                    [MS-S6A COMPLETE]
-> deterministic shown-identity commitment          [MS-S6B NEXT]
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

`SetupCandidate` is a canonical **pre-seat actual-role multiset**. It still has no shown-role field and must remain that way.

### MS-S3

`TemplateRepository` owns actual-role template candidate lookup only. It does not own template shown-identity metadata.

### MS-S4

`GeneratedSetupCandidateSource` owns deterministic legal actual-role generation only, including current base distribution and one capped Baron setup modifier application.

### MS-S4.5

Shown identity was explicitly removed from composition authority. Legacy TB Drunk shown-role weighting must not re-enter S5 or future composition adapters.

### MS-S5

`SetupDiversityRecord` / `SetupDiversityHistory` / fixed-point `SetupDiversityScorer` / deterministic `SetupDiversitySelector` operate on actual-role composition only.

## 4. MS-S6A accepted design

New production files:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicy.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingShownIdentityPolicySource.kt
```

Typed contract:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicyResolverTest.kt`

Generic boundary:

```text
selected SetupCandidate
+ ValidatedClocktowerRuleset
+ candidate provenance / template policy source when needed
-> SetupShownIdentityPolicy
```

Accepted policy types:

```text
ShownIdentityOverrideOptions
├─ actualRole
└─ canonical legalShownRoles

SetupShownIdentityPolicy
└─ overrides: 0..N

TemplateShownIdentityPolicyKey
├─ providerId
└─ candidateId

TemplateShownIdentityPolicySource
└─ find(key) -> SetupShownIdentityPolicy?
```

An empty override list is the explicit no-override policy.

The policy shape is plural/future-extensible, but the current resolver deliberately implements only the existing Drunk setup-time identity mechanic. Unsupported/inconsistent metadata fails closed.

S6A exposes legal options only. It does not make a seeded/random shown-role choice.

## 5. TEMPLATE identity metadata after S6A

For template candidates:

```text
candidate.provenance.providerId
+ candidate.provenance.candidateId
-> TemplateShownIdentityPolicySource
-> normalized SetupShownIdentityPolicy
```

Current TB edge adapter is `TroubleBrewingShownIdentityPolicySource`.

TB uses:

```text
dataset.datasetId as providerId
preset.id as candidateId
```

For a Drunk preset:

```text
preset.drunkAsOptions
-> TroubleBrewingSetupPresetValidator
-> characterRegistry externalId -> RoleId
-> canonical Drunk legal shown-role options
```

For a known template without Drunk, metadata resolves to explicit no-override.

Unknown candidate ID or cross-provider provenance fails rather than silently falling back.

The generic S6A file has no dependency on `TroubleBrewingSetupPreset`; TB's exactly-three-option constraint remains in the existing TB validator.

## 6. GENERATED identity options after S6A

For generated candidates:

```text
if no actual Drunk
-> explicit no-override

if actual Drunk
-> validated ruleset Townsfolk
   - candidate.actualRoles
-> canonical legal shown-role options
```

The current Drunk role is found through stable external ID `drunk`, then generic policy uses canonical `RoleId` values.

Generated S6A:

- includes only validated-script Townsfolk;
- excludes actual in-play roles;
- is independent of ruleset/input ordering;
- returns the whole legal pool rather than selecting one role;
- fails closed when Drunk requires an override but the pool is empty;
- never falls back to an actual in-play Townsfolk.

## 7. Important S6A audit finding: TB candidate cutover still does not exist

The audit confirmed that current TB production still follows the legacy path:

```text
TroubleBrewingSetupPresetSelector
-> TroubleBrewingSetupDealPlanner
-> TroubleBrewingCommittedSetupAdapter
```

There is not yet a production `TroubleBrewingSetupPreset -> SetupCandidate -> generic provider` cutover path.

Do **not** interpret `TroubleBrewingShownIdentityPolicySource` as that cutover. It is only the S6A metadata edge seam.

Actual TB template-to-candidate adaptation and controlled production cutover remain **MS-S7**.

## 8. S6A exact scope audit

Compared with pre-S6A docs carrier `7c2b71f169584ebad3f0873d39d32f48f4fade79`, accepted production checkpoint `5823d66d0eb756a0005df86f1aea7db5902cae60` changes exactly:

```text
SetupShownIdentityPolicy.kt
TroubleBrewingShownIdentityPolicySource.kt
SetupShownIdentityPolicyResolverTest.kt
```

No existing production source was modified.

No changes occurred to:

- `SetupCandidate` / `ClocktowerSetupProvider`;
- S5 composition diversity;
- legacy TB selector/scorer/deal production flow;
- NGJ production flow;
- seat assignment/deal shuffle;
- `PlayerState.shownRole`;
- recommendation / `StorytellerDecision.DrunkShownRole`;
- persistence/recovery;
- App/Host.

## 9. Campaign sequence

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
MS-S6A   shown-identity policy/options boundary                 COMPLETE / ACCEPTED
MS-S6B   deterministic shown-identity commitment               NEXT
MS-S6C   recommendation ownership inversion
MS-S7    TB 480-template controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

Do not collapse several slices merely because their final setup transaction is related.

## 10. MS-S6B immediate objective — NEXT, DO NOT AUTO-START

S6B is strictly the deterministic commitment stage after S6A legality resolution.

Target concept to audit first:

```text
selected SetupCandidate
+ SetupShownIdentityPolicy
+ stable setup seed / identity-selection namespace
-> deterministic committed shown-identity choices
```

S6B answers which one of S6A's legal shown identities is committed. It must not change which identities are legal.

### S6B audit first

Before writing production code, audit:

1. where `setupSeed` is available at the post-S5/pre-seat boundary;
2. the smallest stable generic output needed by later materialization;
3. whether commitment should be represented as role-to-shown-role facts or another similarly small pre-seat value;
4. canonical ordering and hash namespace requirements so input list order cannot affect selection;
5. fail-closed behavior for inconsistent/empty required policies;
6. whether any existing legacy TB/NGJ seed semantics are reusable without carrying old recommendation ownership forward.

### S6B first-version rule

No shown-role history/cooldown is required in the first generic implementation.

The first S6B should be deterministic and seed-stable only. If shown-identity diversity is ever added later, it may rank legal S6A options after composition selection and may never feed back into S5.

## 11. Strict S6B non-goals

Do not in S6B:

- change `SetupCandidate`;
- regenerate or rescore actual-role candidates;
- allow shown identity/history to affect S5;
- modify recommendation ownership — S6C;
- delete `StorytellerDecision.DrunkShownRole` before the S6C consumer audit;
- change `PlayerState.shownRole` production wiring merely to expose the new pure seam;
- cut TB production flow — S7;
- cut NGJ production flow — S8;
- change persistence/recovery;
- expand App/Host;
- perform unrelated seat/deal work.

## 12. MS-S6C / S7 / S8 boundaries

### S6C — recommendation ownership inversion

Recommendation reads already committed `PlayerState.shownRole` as perceived identity:

```text
actual Drunk + shownRole X
-> generate only X-compatible information
-> never choose/replace X
```

Audit/reuse existing first-night information families before adding any new fake-information generator.

### S7 — Trouble Brewing controlled cutover

```text
480 validated templates
-> template SetupCandidate values
-> S5 actual-composition selection
-> S6A template identity policy
-> S6B identity commitment
-> deal/materialize
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

S7 owns TB-specific Minion/style diversity adaptation and retirement of `selectedDrunkShownRole -> preset finalWeight` coupling.

### S8 — NGJ/no-template controlled cutover

```text
GeneratedSetupCandidateSource
-> S5
-> S6A generated identity options
-> S6B identity commitment
-> seat/deal materialization
-> CommittedClocktowerSetup
-> recommendation reads shownRole
```

## 13. Protected predecessor invariants

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

## 14. Validation / workflow

Follow:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`;
- `docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md`.

S6A used a real typed RED before production implementation. The RED was an expected compile failure on the missing durable S6A seam; GREEN passed `:app:testFast` through GitHub CI.

For S6B, again use the smallest durable typed contract if a new stable commitment boundary is introduced. Prefer pure Kotlin tests over source-string assertions.

Use the GitHub connector for safe small/medium docs/tests/source. Keep App/Host out until an explicit production integration slice requires them.

Keep PR #61 Draft and do not merge, mark Ready, rebase or force-push without explicit authorization.

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
docs/MS_S4_GENERATED_SETUP_CANDIDATE_SOURCE_CHECKPOINT_2026-08-31.md
docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md
docs/MS_S5_SETUP_DIVERSITY_SELECTOR_CHECKPOINT_2026-08-31.md
docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 16. Resume guard

Treat `5823d66d0eb756a0005df86f1aea7db5902cae60` as the accepted S6A code/test checkpoint unless a later production commit deliberately supersedes it.

At the next development turn:

1. re-query live `main`, branch, Draft PR #61 and checks;
2. distinguish docs-only carrier head from accepted S6A code/test checkpoint;
3. read S4.5, S5 and S6A checkpoints before designing S6B;
4. audit the seed/commitment output boundary first;
5. consume S6A legal options and implement deterministic commitment only;
6. do not change recommendation, TB/NGJ production flow, persistence or App/Host;
7. keep PR #61 Draft and unmerged.
