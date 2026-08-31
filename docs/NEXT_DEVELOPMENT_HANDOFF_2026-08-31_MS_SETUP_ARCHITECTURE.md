# NEXT DEVELOPMENT HANDOFF — MS-SETUP Generic Multi-Script Setup Architecture

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **MS-S6B COMPLETE / ACCEPTED — MS-S6C NEXT**

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

MS-S6B:
d4cf3969aabcea7433b96b5b320171fbc821853e
```

MS-S6B validation:

```text
RED:
afc970cc9006ced2de24a99bcaa8d789a1d7a11a
CI #1256 / run 33365203015   EXPECTED RED
:app:testFast reached compileDebugUnitTestKotlin and failed on missing S6B commitment types
R2 #1173 / run 33365203021   SUCCESS

GREEN:
d4cf3969aabcea7433b96b5b320171fbc821853e
CI #1257 / run 33365333667   SUCCESS
Android FAST unit tests      SUCCESS
CI aggregate gate            SUCCESS
R2 #1174 / run 33365333672   SUCCESS
Full Android                 SKIPPED by risk router
ASP contract tests           SKIPPED by risk router
Real Clingo                  SKIPPED by risk router
```

Authoritative S6B checkpoint:

`docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md`

S6A policy checkpoint remains authoritative for legal option resolution:

`docs/MS_S6A_SHOWN_IDENTITY_POLICY_CHECKPOINT_2026-08-31.md`

S4.5 architecture correction remains authoritative:

`docs/MS_S4_5_SHOWN_IDENTITY_OWNERSHIP_CORRECTION_2026-08-31.md`

Later docs-only carrier commits do not replace accepted S6B production checkpoint `d4cf3969aabcea7433b96b5b320171fbc821853e`.

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
-> deterministic shown-identity commitment          [MS-S6B COMPLETE]
-> seat/deal materialization
-> CommittedClocktowerSetup(actualRole + shownRole)
-> recommendation consumes PlayerState.shownRole   [MS-S6C NEXT]
-> recommendation produces information only
```

Governing rule:

> Shown identity is a setup fact. It cannot participate in actual-role composition scoring and cannot later be selected/replaced by recommendation.

## 3. Accepted predecessor ownership

### MS-S1 / S1R

`CommittedClocktowerSetup` stores exact actual/shown identities. Persistence restores exact committed setup directly and never reruns selectors/recommendation.

### MS-S2

`SetupCandidate` is a canonical **pre-seat actual-role multiset**. It still has no shown-role field and must remain that way.

`SetupCandidateRequest` already carries `setupSeed`; seed did not move into `SetupCandidate` for S6B.

### MS-S3

`TemplateRepository` owns actual-role template candidate lookup only. It does not own template shown-identity metadata.

### MS-S4

`GeneratedSetupCandidateSource` owns deterministic legal actual-role generation only, including current base distribution and one capped Baron setup modifier application.

### MS-S4.5

Shown identity was explicitly removed from composition authority. Legacy TB Drunk shown-role weighting must not re-enter S5 or future composition adapters.

### MS-S5

`SetupDiversityRecord` / `SetupDiversityHistory` / fixed-point `SetupDiversityScorer` / deterministic `SetupDiversitySelector` operate on actual-role composition only.

### MS-S6A

`SetupShownIdentityPolicyResolver` owns legal shown-identity option resolution after actual-role composition selection.

Template candidates resolve metadata through durable `(providerId, candidateId)` provenance. Generated Drunk options come from validated script Townsfolk minus actual in-play roles. S6A returns legal options only and performs no selection.

## 4. MS-S6B accepted design

New production file:

`app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitment.kt`

Typed contract:

`app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitterTest.kt`

Generic boundary:

```text
selected SetupCandidate
+ resolved SetupShownIdentityPolicy
+ setupSeed
-> SetupShownIdentityCommitment
```

Accepted commitment types:

```text
ShownIdentityCommitment
├─ actualRole
└─ shownRole

SetupShownIdentityCommitment
├─ canonical overrides: 0..N
└─ shownRoleFor(actualRole)
```

Only roles whose shown identity differs from their actual identity are stored as override facts.

For any actual role without an override:

```text
shownRoleFor(actualRole) == actualRole
```

Therefore a S6A no-override policy produces an empty commitment and preserves ordinary actual=shown semantics without materializing redundant facts.

S6B is deliberately pre-seat. It does not construct `CommittedSetupSeat` or `CommittedClocktowerSetup` and does not touch `PlayerState`.

## 5. S6B deterministic selection contract

The generic identity commitment namespace is:

```text
setup-shown-identity-v1
```

Each S6A override is selected independently from its canonical legal option pool.

Stable hash material includes:

```text
script
candidate source kind
providerId
candidateId when present
canonical selected actual-role composition
override actualRole
canonical legal shown-role options
setupSeed
```

Fields are length-prefixed before hashing so delimiter-like text cannot ambiguously collapse adjacent fields.

Selection uses existing `MurmurHash3.low64Utf8`, then unsigned remainder into the canonical option list.

Accepted consequences:

- same selected candidate + same S6A policy + same setup seed -> same shown-identity commitment;
- candidate input order cannot change the result because `SetupCandidate` is canonical;
- option input order cannot change the result because S6A option lists are canonical;
- different setup seeds can explore different legal shown identities when multiple options exist;
- a single legal option commits directly;
- no unseeded `.random()` / `.shuffled()` participates;
- no shown-role history/cooldown participates in the first generic implementation.

## 6. S6B fail-closed behavior

S6B consumes legality rather than redefining it, but rejects inconsistent candidate/policy combinations.

Before selecting an option:

- each override actual role must exist in the selected candidate;
- an override cannot show the actual role as itself;
- no legal shown option may already be an actual in-play role.

S6A constructors already guarantee non-empty unique canonical legal option pools. S6B does not expand an invalid pool or silently fall back.

The accepted commitment cannot mutate the candidate or policy input values.

## 7. Legacy TB semantics after S6B

Current legacy TB production still follows:

```text
TroubleBrewingSetupPresetSelector
-> TroubleBrewingSetupDealPlanner
-> TroubleBrewingCommittedSetupAdapter
```

There is still no production TB `TroubleBrewingSetupPreset -> SetupCandidate -> S5 -> S6A -> S6B` cutover path.

Actual TB template-to-candidate adaptation and controlled production cutover remain **MS-S7**.

Legacy TB shown-role selection used `tb-drunk-v1` with dataset/preset-specific seed material. Generic S6B deliberately does not treat that namespace as generic authority.

Generic commitment now occurs downstream of actual composition and S6A legality under:

```text
setup-shown-identity-v1
```

This is consistent with the S4.5 architecture correction. S7 may audit controlled semantic differences, but must not reintroduce shown-role influence into S5 actual-composition selection.

## 8. S6B exact scope audit

Compared with pre-S6B docs carrier `7b68df45c44bfec8afdc545e637e5465c1dc08e0`, accepted production checkpoint `d4cf3969aabcea7433b96b5b320171fbc821853e` changes exactly:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitment.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityCommitterTest.kt
```

No existing production source was modified.

No changes occurred to:

- `SetupCandidate` / `SetupCandidateRequest` / `ClocktowerSetupProvider`;
- S5 composition diversity;
- S6A legality resolution;
- legacy TB selector/scorer/deal production flow;
- NGJ production flow;
- seat assignment/deal shuffle;
- `CommittedClocktowerSetup` materialization;
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
MS-S6B   deterministic shown-identity commitment               COMPLETE / ACCEPTED
MS-S6C   recommendation ownership inversion                    NEXT
MS-S7    TB 480-template controlled semantic cutover
MS-S8    NGJ/no-template production cutover
MS-S9    future-script generic acceptance

REC-R1   separate future unfinished-game stable-checkpoint work
```

Do not collapse several slices merely because their final setup transaction is related.

## 10. MS-S6C immediate objective — NEXT, DO NOT AUTO-START

S6C is strictly the recommendation ownership inversion after shown identity has become a committed setup fact.

Target behavior:

```text
actual Drunk + already committed shownRole X
-> recommendation consumes X as perceived identity
-> generate only X-compatible information
-> never choose, replace or reroll X
```

S6C must remove recommendation authority over shown identity without changing setup composition, legality or S6B commitment.

### S6C audit first

Before writing production code, audit:

1. `StorytellerDecision.DrunkShownRole` producers and every consumer;
2. setup/recommendation paths that currently choose, recommend or replace a Drunk shown role;
3. `PlayerState.shownRole` availability at recommendation request/evaluation boundaries;
4. existing first-night information candidate families that can consume an already-committed perceived identity;
5. legacy tests whose stable contract currently assumes recommendation owns shown identity;
6. the smallest typed replacement evidence needed before narrowing/removing recommendation-owned identity concepts;
7. whether any source-string migration guards become obsolete once a typed consumer seam is established.

### S6C first-version rule

Prefer reusing existing first-night information generation families.

Do not introduce a second setup selector or a new fake-information subsystem merely to complete ownership inversion.

Recommendation should receive the already-committed perceived role and generate information for that role. It should not produce a new setup identity decision.

## 11. Strict S6C non-goals

Do not in S6C:

- change `SetupCandidate`;
- regenerate or rescore actual-role candidates;
- allow shown identity/history to affect S5;
- change S6A legal option resolution;
- change S6B commitment selection/namespace;
- let recommendation feed back into setup identity;
- delete `StorytellerDecision.DrunkShownRole` before consumer audit and stable replacement evidence;
- cut TB production flow — S7;
- cut NGJ production flow — S8;
- change persistence/recovery;
- expand App/Host merely to expose the ownership inversion;
- perform unrelated seat/deal work.

If seat/deal materialization is needed before recommendation can consume committed shown identity in production, keep that integration explicitly separated from S6C semantics rather than silently expanding the slice.

## 12. MS-S7 / S8 boundaries

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
S6A legality cannot be rewritten by S6B or recommendation.
S6B commitment cannot feed back into S5.
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

S6B used a real typed RED before production implementation. The RED was an expected compile failure on the missing durable commitment seam; GREEN passed `:app:testFast` through GitHub CI and R2.

S6C changes recommendation semantics/ownership. Use the smallest durable typed consumer evidence first and then apply the affected recommendation validation selected by `docs/TESTING_STRATEGY.md`. If the consumer audit reaches setup recommendation scoring/quality paths, include the corresponding T2/T3 evidence rather than assuming FAST alone is sufficient.

Prefer typed behavior/integration tests over source-string assertions. Retire or narrow obsolete source-string migration guards when a stable typed replacement actually supersedes them.

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
docs/MS_S6B_SHOWN_IDENTITY_COMMITMENT_CHECKPOINT_2026-08-31.md
docs/TESTING_STRATEGY.md
docs/AI_DEVELOPMENT_WORKFLOW_V2_2026-08-27.md
```

## 16. Resume guard

Treat `d4cf3969aabcea7433b96b5b320171fbc821853e` as the accepted S6B code/test checkpoint unless a later production commit deliberately supersedes it.

At the next development turn:

1. re-query live `main`, branch, Draft PR #61 and checks;
2. distinguish docs-only carrier head from accepted S6B code/test checkpoint;
3. read S4.5, S5, S6A and S6B checkpoints before designing S6C;
4. audit `StorytellerDecision.DrunkShownRole`, recommendation ownership and `PlayerState.shownRole` consumption first;
5. treat S6B shown identity as already committed setup input and make recommendation generate information only;
6. do not change setup composition, S6A legality or S6B commitment;
7. do not perform TB/NGJ production cutovers before S7/S8;
8. keep persistence/recovery and App/Host out unless a later explicit integration slice requires them;
9. keep PR #61 Draft and unmerged.
