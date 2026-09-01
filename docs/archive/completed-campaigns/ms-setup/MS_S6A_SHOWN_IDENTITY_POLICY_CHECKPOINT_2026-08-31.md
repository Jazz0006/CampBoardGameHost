# MS-S6A Shown Identity Policy Checkpoint — 2026-08-31

Status: **COMPLETE / ACCEPTED**

Branch: `codex/ms-setup-generic-architecture`

Draft PR: `#61`

Accepted code/test checkpoint:

`5823d66d0eb756a0005df86f1aea7db5902cae60`

Test-first RED checkpoint:

`50a04d41a1bda6cc1c0a0b87b88a5135d521979d`

## Validation

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

Exact S6A diff from the pre-S6A docs carrier `7c2b71f169584ebad3f0873d39d32f48f4fade79` contains exactly:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicy.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/setup/TroubleBrewingShownIdentityPolicySource.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/setup/SetupShownIdentityPolicyResolverTest.kt
```

No existing production source was modified.

## Ownership audit result

The S6A audit confirmed:

- `SetupCandidate` remains a canonical actual-role-only pre-seat value with provenance;
- `SetupProvenance(providerId, candidateId)` is sufficient to recover template-specific identity metadata without expanding `SetupCandidate`;
- `TemplateRepository` remains actual-role template storage only;
- `ValidatedClocktowerRuleset.characters` / `characterRegistry` already provide the team and stable external-id/role-id lookup required for generated identity options;
- TB `TroubleBrewingSetupPresetValidator` already owns the exact `drunkAsOptions` constraints: no Drunk means empty options; Drunk means exactly three unique Townsfolk absent from actual in-play roles;
- the legacy TB production path still runs `TroubleBrewingSetupPresetSelector -> deal plan -> committed setup adapter`; there is not yet a production TB `SetupCandidate` cutover/adapter path;
- therefore S6A adds only a TB shown-identity metadata edge adapter, not the S7 TB candidate/production cutover.

## Accepted generic contract

S6A introduces:

```text
ShownIdentityOverrideOptions
├─ actualRole
└─ canonical legalShownRoles

SetupShownIdentityPolicy
└─ overrides: 0..N ShownIdentityOverrideOptions

TemplateShownIdentityPolicyKey
├─ providerId
└─ candidateId

TemplateShownIdentityPolicySource
└─ find(key) -> SetupShownIdentityPolicy?

SetupShownIdentityPolicyResolver
└─ resolve(selected SetupCandidate, ValidatedClocktowerRuleset)
   -> SetupShownIdentityPolicy
```

An empty `overrides` list is the explicit **no-override policy**.

The policy representation is plural/future-extensible, while the current resolver intentionally supports only the existing Drunk setup-time identity mechanic. Unsupported or inconsistent metadata fails closed rather than being guessed.

S6A exposes legal options only. It does not choose a shown role.

## TEMPLATE behavior

`TroubleBrewingShownIdentityPolicySource` is the TB edge adapter.

Current TB lookup identity is:

```text
datasetId as providerId
+ preset.id as candidateId
-> normalized generic shown-identity policy
```

For a TB preset containing Drunk:

```text
preset.drunkAsOptions external IDs
-> existing TB validator
-> characterRegistry externalId -> RoleId mapping
-> canonical ShownIdentityOverrideOptions(actualRole = Drunk, legalShownRoles = ...)
```

For a valid TB preset without Drunk, the source stores an explicit no-override policy.

The generic resolver does not depend on `TroubleBrewingSetupPreset` or the TB dataset model.

Unknown `candidateId` and cross-provider provenance return no metadata from the source and the resolver fails clearly; there is no silent fallback.

The resolver also rechecks that current Drunk shown options resolve to Townsfolk in the validated ruleset and are absent from the selected candidate's actual roles. This keeps the generic boundary fail-closed even if a future metadata source is malformed.

TB's exactly-three-option rule remains exclusively in `TroubleBrewingSetupPresetValidator`; generic S6A has no hard-coded cardinality of three.

## GENERATED behavior

For a GENERATED candidate:

```text
if actual Drunk is absent
-> explicit no-override policy

if actual Drunk is present
-> validated ruleset Townsfolk
   - candidate.actualRoles
-> canonical legal shown-role options
```

The resolver identifies the current Drunk mechanic through the canonical ruleset external ID `drunk`, then uses canonical `RoleId` values in the generic policy.

Generated options:

- contain only Townsfolk from the validated script/ruleset;
- exclude every actual in-play role in the selected candidate;
- are canonicalized independently of ruleset/input order;
- preserve the full legal option pool rather than selecting one role;
- fail closed when Drunk requires an override but no unused Townsfolk remains.

There is no fallback to an actual in-play Townsfolk.

## S6A typed evidence

`SetupShownIdentityPolicyResolverTest` covers:

- TEMPLATE provenance resolving TB Drunk options;
- TB `drunkAsOptions` normalization through the companion metadata source;
- candidate actual-role composition remaining unchanged and separate from metadata;
- GENERATED legal Townsfolk derivation;
- exclusion of actual in-play roles;
- canonical result under different ruleset/candidate input order;
- explicit no-override policy for generated and known-template no-Drunk cases;
- fail-closed generated empty option pool;
- unknown template candidate ID failure;
- cross-provider template provenance failure;
- fail-closed template metadata that points at an actual in-play role;
- preservation of multiple legal options, proving S6A does not perform seeded/random shown-role selection.

## Protected architecture

The causal ownership remains:

```text
Composition
-> Identity
-> Information
```

S6A does not change:

- `SetupCandidate`;
- `SetupDiversityRecord`, `SetupDiversityHistory`, `SetupDiversityScorer` or `SetupDiversitySelector`;
- legacy TB production selection/deal flow;
- NGJ production flow;
- seat assignment or deal shuffle;
- `PlayerState.shownRole`;
- recommendation or `StorytellerDecision.DrunkShownRole`;
- persistence/recovery;
- App/Host.

In particular, shown identity did not re-enter S5 actual-composition diversity.

## Next slice

**MS-S6B — deterministic shown-identity commitment.**

S6B may consume the accepted S6A policy and deterministically choose/commit one legal shown role. Before implementation, audit the smallest stable commitment output, setup-seed/namespace ownership and canonical ordering requirements.

S6B must not yet change recommendation ownership, TB/NGJ production cutovers, App/Host or persistence.

Do not start S6B automatically from this checkpoint.

Keep PR #61 Draft. Do not merge, mark Ready, rebase or force-push without explicit user authorization.
