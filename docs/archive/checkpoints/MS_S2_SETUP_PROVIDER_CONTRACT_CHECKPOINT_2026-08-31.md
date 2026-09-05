# MS-S2 — Generic Setup Candidate / Provider Contract Checkpoint

> Date: 2026-08-31 Australia/Sydney  
> Branch: `codex/ms-setup-generic-architecture`  
> Draft PR: `#61`  
> Status: **COMPLETE / ACCEPTED**

## Accepted checkpoint

Accepted code/test head:

`d4001863f134ebbe7d26819f40ac34c7d1de200c`

Test-first contract commit:

`293eed4f7f0557ffa75388b92ff40ca34d9a24d7` — `test(ms-s2): define setup candidate provider contracts`

Production contract commit:

`d4001863f134ebbe7d26819f40ac34c7d1de200c` — `feat(ms-s2): add generic setup candidate provider contracts`

Validation:

```text
CI #1225 / run 33357219556                 SUCCESS
Android FAST unit tests                    SUCCESS
CI aggregate gate                          SUCCESS
R2 #1142 / run 33357219544                 SUCCESS
Full Android                               SKIPPED by risk router
ASP contract tests                         SKIPPED by risk router
Real Clingo                                SKIPPED by risk router
```

Exact slice diff from the accepted MS-S2 planning/doc head changed only:

- `app/src/main/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProvider.kt`
- `app/src/test/java/com/codex/campboardgamehost/clocktower/setup/ClocktowerSetupProviderRegistryTest.kt`

No App/Host/TB/NGJ production wiring changed in MS-S2.

## Accepted ownership model

MS-S2 introduces four small pure Kotlin concepts:

```text
SetupCandidate
SetupCandidateRequest
SetupCandidateSource
ClocktowerSetupProvider
ClocktowerSetupProviderRegistry
```

### SetupCandidate

A candidate is a **pre-seat actual-role composition**, not a committed game setup.

```text
SetupCandidate
├─ script: ScriptId
├─ actualRoles: canonical role multiset
├─ playerCount: derived from actualRoles.size
└─ provenance: SetupProvenance
```

Accepted semantics:

- `actualRoles` is snapshotted from caller input;
- roles are canonically sorted by `RoleId.value`;
- duplicate role IDs, if a future ruleset permits them, remain represented because the candidate stores a multiset/list rather than a set;
- list order carries no seat meaning;
- candidate must contain at least one actual role;
- shown identities are deliberately absent;
- seat assignment is deliberately absent;
- persistence/schema data is deliberately absent;
- diversity-history/scoring data is deliberately absent.

This cleanly separates the current legacy NGJ concerns that are mixed together inside `generateClocktowerAssignments`: actual-role composition, seat randomization, and Drunk shown-role choice will be migrated in later slices rather than copied wholesale into the generic candidate model.

### SetupCandidateRequest

```text
SetupCandidateRequest
├─ script: ScriptId
├─ playerCount: Int > 0
└─ setupSeed: Long
```

The seed is present because the later deterministic generated candidate source (MS-S4) needs it. Diversity history is intentionally excluded because MS-S5 owns common diversity scoring/selection.

### SetupCandidateSource

`SetupCandidateSource` is a persistence-independent functional boundary:

```text
request -> List<SetupCandidate>
```

MS-S3 template repositories and MS-S4 deterministic generators can implement/adapt to this boundary without changing App or persistence ownership.

### ClocktowerSetupProvider

A provider owns one script plus a provider identity and candidate source.

It rejects:

- a request for another script before invoking its source;
- returned candidates for another script;
- returned candidates with a mismatched player count;
- returned candidates attributed to another `providerId`.

An empty candidate list remains legal. This is important because later policy/fallback composition must be able to distinguish “this source has no candidates” from malformed candidate data.

### ClocktowerSetupProviderRegistry

The registry maps `ScriptId -> ClocktowerSetupProvider`.

Accepted behavior:

- registered scripts resolve deterministically;
- unregistered scripts return `null` explicitly;
- duplicate registrations for the same script are rejected;
- registration does not depend on Android, persistence, Host, or App-root branching.

## TB audit classification that informed MS-S2

From `TroubleBrewingSetupPreset`:

Generic candidate facts:

```text
preset id -> provenance.candidateId
playerCount -> derived/validated against actual role multiset
actual role composition -> townsfolk + outsiders + minions + demons
```

TB-only template / scoring metadata that did **not** enter generic candidate core:

```text
source
complexity
styleTags
runtimeSelectionPolicy
historyWeights
lastGameMaxOverlap
similarity/exact-repeat policy
```

Shown-identity concern intentionally deferred:

```text
drunkAsOptions
selectedDrunkShownRole
```

Dataset/provider identity can later map into `SetupProvenance.providerId` when TB is adapted in MS-S7; dataset schema/version remains a TB data-validation concern rather than a mandatory generic candidate field.

## NGJ audit classification that informed MS-S2

Current legacy `generateClocktowerAssignments` combines:

```text
clocktowerRolesForScript(script)
+ clocktowerDistribution(playerCount)
+ Baron distribution adjustment
+ random role selection
+ random seat order
+ Drunk shown-role choice
```

MS-S2 deliberately models only the pre-seat actual-role candidate boundary. Later slices own:

- deterministic generated composition: MS-S4;
- common diversity selection: MS-S5;
- shown-identity commitment: MS-S6;
- NGJ production adaptation/parity: MS-S8.

## Explicit non-goals

MS-S2 did not implement:

- template repository lookup;
- generated setup algorithms;
- diversity history/scoring/selection;
- generic shown-identity policy;
- TB preset adaptation;
- NGJ production cutover;
- App-root provider registry wiring;
- setup persistence changes;
- general unfinished-game recovery changes.

## Next slice

MS-S3 is next:

```text
optional TemplateRepository keyed by script + player count
```

MS-S3 should adapt template data into `SetupCandidate` without moving TB-specific scoring/style/Drunk shown-role metadata into generic core, and should stop before deterministic generated setup implementation (MS-S4).
