# CampBoardGameHost — TBSP-4 Recommendation Lock Completion Handoff

> Updated: 2026-08-30 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Campaign: **TBSP — Trouble Brewing Setup Preset Integration**  
> Status: **TBSP-4 COMPLETE / TBSP-5 NOT STARTED**  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR / CI carrier: **#57 — TBSP: integrate Trouble Brewing setup presets**  
> Live `main` baseline: `ba7cfa12853a8829ecf228c05cf2a22067f1e6e4`  
> TBSP-3 validated checkpoint: `8e918f69f6184a6389a23881af42127a3d761ef2`  
> TBSP-4 validated checkpoint: `f68d8326de6bf57ecfd632fef73689c4900f87a9`  
> Parent status authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Production cutover contract: `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`

## 1. Live state to preserve

At TBSP-4 completion:

```text
main:
ba7cfa12853a8829ecf228c05cf2a22067f1e6e4

branch:
codex/trouble-brewing-setup-presets-v2

PR #57:
OPEN
DRAFT
NOT MERGED

latest validated TBSP-4 code/test checkpoint:
f68d8326de6bf57ecfd632fef73689c4900f87a9
```

Validation at the TBSP-4 checkpoint:

```text
R2 main-thread boundary: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
full Android unit/build step: skipped by normal FAST routing
ASP contract tests: skipped
Real Clingo cross-validation: skipped
```

No local developer-selected T0 was executed from this Chat runtime. A direct temporary Git checkout was attempted after the user authorized direct Gradle execution, but this container could not resolve `github.com`; therefore the actual Gradle evidence remains the PR's GitHub Actions `:app:testFast` execution. Do not rewrite that as a local T0.

Do not merge PR #57 or mark it Ready without explicit user authorization.

## 2. TBSP-4 goal and resulting ownership

TBSP-4 integrates the selector-owned Drunk shown identity with the existing setup recommendation engine without making recommendation a second identity authority.

The accepted chain is now:

```text
TroubleBrewingSetupPresetSelector
    -> selectedDrunkShownRole
TroubleBrewingSetupDealPlanner
    -> committed actual/shown identity
TroubleBrewingSetupRecommendationLock
    -> locked StorytellerDecision.DrunkShownRole
SetupCoordinationRequest.lockedDecisions
    -> existing recommendation engine computes only compatible remaining setup information
```

The recommendation layer may generate compatible downstream decisions, but it cannot choose a different Drunk shown role.

## 3. New source ownership

Source:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/session/
    TroubleBrewingSetupRecommendationLock.kt
```

Focused TBSP-4 tests:

```text
app/src/test/java/com/codex/campboardgamehost/clocktower/session/
    TroubleBrewingSetupRecommendationLockTest.kt
```

The bridge API is intentionally narrow:

```kotlin
TroubleBrewingSetupRecommendationLock.lockedDecisions(
    dealPlan,
    roleDefinitions,
)
```

For a Drunk deal it converts the external lowercase dataset identity such as:

```text
investigator
```

into the existing canonical recommendation identity:

```text
RoleId("Investigator")
```

and emits exactly:

```text
StorytellerDecision.DrunkShownRole(RoleId("Investigator"))
```

For a non-Drunk deal it emits no Drunk lock.

The bridge performs no random draw and does not invoke the recommender itself.

## 4. Existing recommendation capability reused, not rewritten

The existing architecture already supported constrained setup recommendation:

```text
SetupCoordinationRequest.lockedDecisions
-> ClocktowerRecommendationCoordinator
-> SetupRecommendationModule
-> SetupRecommendationService.recommendConstrained
-> SetupCandidateGenerator.generatePlans(... lockedDecisions ...)
```

`SetupCandidateGenerator` already filters candidate setup plans so every locked Drunk decision must be present in the generated option.

For a locked:

```text
DrunkShownRole(Investigator)
```

existing generation continues to enumerate compatible:

```text
DrunkInvestigatorInfo(
    shownMinion,
    candidateSeats,
)
```

Therefore TBSP-4 required no scoring retune and no new Investigator-information algorithm.

## 5. Accepted executable contracts

TBSP-4 now proves:

```text
1. selector/deal-plan owned Drunk shown role becomes one downstream locked decision;
2. recommendation plans cannot replace that shown role;
3. when the locked shown role is Investigator, compatible DrunkInvestigatorInfo remains available;
4. every returned constrained setup plan preserves the same locked Investigator identity;
5. a non-Drunk deal contributes no Drunk recommendation lock;
6. no second Drunk identity draw exists in the new bridge;
7. setup recommendation scoring was not changed for the lock integration.
```

This closes the pure recommendation-layer portion of production invariant P5:

```text
Later recommendation cannot replace selected Drunk shown role.
```

It is not yet a claim that the App production start path uses this chain; production cutover remains TBSP-6.

## 6. Tests-first history

### TBSP-4A — deal plan to locked Drunk decision

RED:

```text
ddb6e391c8fbd09e5e19cdee0817a28a78e81713
```

GitHub Actions ran:

```text
./gradlew :app:testFast --no-daemon --build-cache
```

Expected RED:

```text
TroubleBrewingSetupRecommendationLockTest.kt
Unresolved reference 'TroubleBrewingSetupRecommendationLock'
```

GREEN:

```text
dad24f91cea855e7a009ac5f173ef64a06e10668
```

Validation:

```text
R2: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
```

### TBSP-4B — locked Investigator survives recommendation

Lock-in test commit:

```text
ae4140ec2643edfbe160ca77aeb3604ebee66c73
```

This test was GREEN immediately because the pre-existing constrained recommendation engine already honored `lockedDecisions` correctly.

Do **not** invent a RED for TBSP-4B.

The test proves every returned setup plan keeps:

```text
DrunkShownRole(Investigator)
```

and contains exactly one compatible:

```text
DrunkInvestigatorInfo
```

Validation:

```text
R2: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
```

### TBSP-4C — non-Drunk boundary

Lock-in test / final TBSP-4 checkpoint:

```text
f68d8326de6bf57ecfd632fef73689c4900f87a9
```

The test proves a deal plan with no selected Drunk shown role contributes no setup recommendation lock.

It was GREEN without production changes.

Validation:

```text
R2: GREEN
Android :app:testFast: GREEN
CI gate: GREEN
```

## 7. Exact scope audit

TBSP-4 changed only the recommendation-lock seam and its focused tests.

It did **not** modify:

```text
CampBoardGameHostApp production start/deal wiring
legacy generateClocktowerAssignments path
cross-game rotation-history persistence
active-game persistence / restore ownership
recommendation scoring weights
No Greater Joy behavior
A3 immutable setup snapshot ownership
identity-reveal background precompute lifecycle
```

The production App therefore still uses the legacy broad-random setup path at this checkpoint.

## 8. Next logical slice — TBSP-5, NOT STARTED

The next campaign slice is:

```text
TBSP-5 — durable cross-game Trouble Brewing rotation-history storage
```

Conceptually it must persist enough completed-game setup history to reconstruct the selector's five-game rotation input without conflating it with A3 immutable active-game setup provenance.

Likely concerns to audit before any write:

```text
existing persistence ownership / storage format
when a selected preset becomes eligible for cross-game history
completed game vs started/abandoned game semantics
stable dataset/schema/preset provenance
selected Drunk shown-role history needed by the x0.40 consecutive-Drunk penalty
bounded five-game history per relevant player-count policy
restore/migration behavior
```

TBSP-5 is persistence work and was not included in the TBSP-4 authorization. Treat it as **NOT STARTED** until scope is explicitly confirmed.

## 9. TBSP-6 remains later production cutover

TBSP-6 still owns:

```text
legacy broad-random TB setup removal from production path
no Baron double application in production
selector + deal planner + recommendation lock production wiring
start-once / no-reroll lifecycle
active-game restore ownership
invalid preset fail-closed behavior
identity reveal beginning before expensive first-night/setup computation completes
background first-night/setup computation consuming committed identities without mutation
```

The normative contract remains:

```text
docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md
```

No Greater Joy remains outside the Trouble Brewing cutover.

## 10. Resume protocol

1. read root `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this TBSP-4 completion handoff;
4. read `docs/TESTING_STRATEGY.md`;
5. read `docs/TBSP_ROTATION_WEIGHT_CONTRACT_V1.md` before TBSP-5 history design;
6. read `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md` before TBSP-6 planning;
7. re-query live `main`, PR #57 head/state/checks and branch comparison;
8. distinguish validated TBSP-4 checkpoint `f68d8326de6bf57ecfd632fef73689c4900f87a9` from later docs-only commits;
9. treat TBSP-5 persistence as NOT STARTED unless explicitly authorized;
10. keep PR #57 Draft;
11. do not merge or mark Ready without explicit authorization;
12. do not change No Greater Joy or resume A3 as part of TBSP-5/TBSP-6 unless separately authorized.
