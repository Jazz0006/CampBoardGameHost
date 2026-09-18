# SDE-2A Drunk Ownership / Replanning Audit

> Date: 2026-09-18 Australia/Sydney  
> Branch: `sde-1-orchestration-seam`  
> PR: #144  
> Status: **COMPLETE**

## 1. Objective

Freeze the first SDE-2 ownership boundary for the Drunk before adding uncertainty orchestration.

The required distinction is:

```text
Drunk shown role
    persistent setup/session identity

unshown Drunk clue
    disposable planned information

shown / durably recorded Drunk clue
    immutable committed history
```

The audit specifically asks whether SDE needs a new dependency/revision owner for Poisoner-driven replanning.

Conclusion: **no**. Existing session revisions plus `PlannedDecisionRef` already provide the required invalidation boundary.

## 2. Drunk shown-role authority

Trouble Brewing setup resolves actual and shown identities before reveal.

Production path:

```text
TroubleBrewingProductionSetupPreparer
→ TroubleBrewingDealRoleResolver
→ resolved assignment.actualRole / assignment.shownRole
→ PlayerCard.clocktowerRole / clocktowerShownRole
→ ClocktowerGameSession.createProduction(initialState = cards.toClocktowerGameState(...))
→ CommittedClocktowerSetup
```

The Drunk's shown Townsfolk identity therefore exists before first-night information recommendation.

Additional ownership evidence:

- `TroubleBrewingSetupRecommendationLock.lockedDecisions(...)` returns no recommendation lock for the shown role;
- `SetupRecommendationService` rejects `StorytellerDecision.DrunkShownRole` as `shown-identity-is-committed-setup-fact`;
- `SetupCandidateGenerator.generatePlans(...)` does not allow a locked `DrunkShownRole`;
- `CampBoardGameHostApp.setClocktowerShownRole(...)` commits any legacy/missing shown-role change through `ClocktowerGameSession.commitShownRoleBoundary(...)`, which owns the game-state revision.

SDE must reference the shown role but never plan, rank, replace, or persist a second copy of it.

## 3. Drunk clue authority

Concrete first-night clue information is intentionally not committed at setup.

Production comments and state handling already enforce this:

- setup/recommendation application clears `clocktowerRecommendedDrunkInvestigatorRoleName` and `clocktowerRecommendedDrunkInvestigatorSeats`;
- the first-night information step derives `InformationReliability.DRUNK` from the Drunk's ineffective shown ability;
- pair legality/materialization remains in the existing first-night pair-information authority;
- `FirstNightInformationMigration` stores candidate sets only as unshown migration state and stores only the selected observation after display.

Therefore a Drunk clue is a PLANNED decision until the reveal/confirmation boundary.

## 4. Poisoner invalidation authority

Production Poisoner handling already has the two freshness transitions SDE needs.

### Draft target edit

`onSelectPoisonTarget` calls:

```text
ClocktowerGameSession.recordPlayerInput()
```

before updating the draft target.

Result:

```text
playerInputRevision + 1
gameStateRevision unchanged
```

Any `PlannedDecisionRef` made before the draft edit is stale without introducing another generation/revision counter.

### Confirmed target

`onConfirmPoisonTarget` commits through:

```text
ClocktowerGameSession.commitPoisonTargetBoundary(...)
```

Result:

```text
gameStateRevision + 1
canonical poisoned projection updated
```

The production host also clears provisional Drunk Investigator recommendation fields after confirmation.

Again, any pre-confirmation `PlannedDecisionRef` is stale through the existing revision authority.

## 5. Existing first-night lifecycle behavior

`FirstNightInformationMigration.invalidateUnshown()` already implements the correct fact boundary:

- all ready/unshown candidate state is discarded;
- displayed observations are retained;
- displayed decisions cannot be replaced by a changed request.

The host invokes this invalidation when the first-night poison target changes, including draft changes.

This is compatible with SDE planning but is not an SDE-owned lifecycle store.

## 6. Frozen SDE-2A contract

SDE-2A will add regression evidence, not a new state model.

Required assertions:

1. a Drunk shown role is unchanged by Poisoner draft edits;
2. a planned clue bound to the prior `playerInputRevision` becomes stale after a Poisoner draft edit;
3. a fresh plan made after the draft edit becomes stale after confirmed poison changes `gameStateRevision`;
4. the Drunk shown role remains unchanged by confirmed poison;
5. a durably committed clue remains in `ClocktowerGameSession.epistemicObservationLog` across later poison changes;
6. no SDE-specific revision/generation counter is introduced.

## 7. Explicit non-ownership

SDE-2A must not:

- generate or select the Drunk shown role;
- mutate `CommittedClocktowerSetup`;
- copy shown-role state into SDE lifecycle storage;
- use `FirstNightInformationLifecycle.generation` as a new SDE freshness authority;
- rewrite a displayed/committed clue after Poisoner changes;
- cut production clue selection over to SDE diagnostics.

## 8. Consequence for SDE-2B / SDE-2C

SDE-2B may introduce **interaction-local registration assumptions**, because Spy/Recluse uncertainty cannot be represented as canonical identity mutation.

SDE-2C should reuse the SDE-2A freshness rule:

```text
source revision changes
→ previous PlannedDecisionRef stale
→ discard plan
→ regenerate through existing legality owners
→ reevaluate exact consequences
```

No new replanning revision counter is justified by the Drunk/Poisoner path.


## 9. Executable closure evidence

SDE-2A required no new production lifecycle abstraction. The existing session revisions and `PlannedDecisionRef` contract satisfy the replanning boundary.

Focused executable regression:

`app/src/test/java/com/codex/campboardgamehost/clocktower/recommendation/sde/Sde2DrunkOwnershipReplanningTest.kt`

It proves:

- Poisoner draft input advances only `playerInputRevision` and invalidates the old plan;
- confirmed poison advances `gameStateRevision` and invalidates a plan made after the draft;
- the Drunk's shown Investigator identity survives both transitions;
- a durably committed Drunk clue remains unchanged in the canonical epistemic observation log across later poison replanning boundaries.

Final SDE-2A executable SHA:

`6c9d7fe8776fea4723004e22790ba11c1f140b8c`

Validation:

```text
R2 main-thread boundary        SUCCESS
Android FAST unit tests        SUCCESS (executed)
CI gate                        SUCCESS
ASP contract tests             SKIPPED by classifier
Real Clingo cross-validation   SKIPPED by classifier
Full Android/APK step          SKIPPED by classifier
```

CI run: `35291677931`.

Therefore the architectural conclusion is frozen:

> **Drunk shown identity is persistent session/setup truth; unshown clue plans are disposable; committed clue history is immutable; Poisoner replanning uses existing game/input revisions. No SDE-specific dependency store or replanning revision counter is introduced.**
