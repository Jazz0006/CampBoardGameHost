# Drunk Late-Binding and First-Night Decision Sequencing Audit — 2026-09-28

> Status: **HISTORICAL SOURCE ARCHITECTURE AUDIT** — implementation authority moved to `DLB_AND_APP_HOST_DECOMPOSITION_IMPLEMENTATION_ROUTE_2026-09-29.md` and the current DLB slice audit.
> Baseline: `main@325a47f3597b61788091ef91d9d41674f7e25d91`
> Audit branch: `codex/drunk-late-binding-sequencing-audit`
> Production code changed: **none**
> Product decision: Drunk identity becomes a Storyteller/SDE output after seats are assigned, rather than a template-owned input fixed before the deal.

## 1. Approved target

The new Trouble Brewing setup contract is:

```text
select template / visible-roster shape
-> if template has Drunk, realize one extra Townsfolk identity for the bag
-> assign all visible identities to seats
-> SDE chooses which dealt Townsfolk seat is actually Drunk
-> commit actualRole=Drunk while preserving that seat's shown Townsfolk role
-> continue first-night decisions against the committed effective state
```

The template must no longer bind one particular player/token to Drunk.

For the existing dataset, `drunk_as_options` can initially be retained as a low-risk asset bridge, but its meaning changes. One option supplies the extra Townsfolk identity needed for the visible/dealt roster; after seating, that added Townsfolk is no longer privileged. Every legally eligible dealt Townsfolk seat enters the Drunk candidate domain.

This gives every migrated preset a safe semantic fallback: if SDE chooses the extra Townsfolk as Drunk, the resulting actual-role roster is equivalent to today's setup. SDE may choose another Townsfolk only when the new global evaluation supports it.

Current asset coverage confirms 208 Drunk presets and 624 `drunk_as_options` entries (three per Drunk preset), so preserving the asset as a transitional visible-roster source avoids an unnecessary bulk data redesign in the first slice.

## 2. Current ownership that must change

Today the pipeline is:

```text
TroubleBrewingSetupPreset
  owns outsiders=["drunk"] + drunkAsOptions
-> TroubleBrewingSetupPresetSelector
  chooses selectedDrunkShownRole before seating
-> TroubleBrewingProductionSetupPreparer / SetupShownIdentityCommitter
  commits the shown identity before deal
-> TroubleBrewingSetupDealPlanner
  deals an actual Drunk token to one seat and binds the chosen shownRole
-> CommittedClocktowerSetup / GameState
  already contain exact Drunk seat
-> SetupRecommendationService / SDE consume Drunk identity as input
```

This is directly incompatible with the new product semantics.

The old historical branch `codex/drunk-shown-identity-ownership-cleanup` documented and implemented the opposite contract: Setup owned the Drunk shown identity and Recommendation was forbidden to select it. That branch is heavily diverged and is not a safe implementation base. Its useful ownership principle remains valid — rules/domain own legality, SDE ranks legal alternatives, and committed choices become immutable — but its specific Drunk-input assumption is superseded by this audit.

The current authoritative SDE route also still states that Drunk shown identity is already persistent before information recommendation. That sentence must be revised when implementation starts.

## 3. Existing architecture that can be reused

The current code already has several useful seams:

- `PlayerState` and `CommittedSetupSeat` explicitly separate `actualRole` from `shownRole`.
- First-night waking-role projection already includes a Drunk player's `shownRole` while separately tracking actual roles. This means a Drunk shown as Fortune Teller can still wake as Fortune Teller while actual-role-only setup interactions can be suppressed.
- `DynamicDecisionSnapshot` includes actual role, shown role, poisoned state and game-state revision in its digest.
- Poison target confirmation is already a canonical game-state boundary through `ClocktowerGameSession.commitPoisonTargetBoundary`; downstream dynamic recommendation keys therefore change after poison commits.
- Red Herring already has a typed legality owner and an SDE shadow adapter rather than moving legality into policy.
- DecisionTrace/replay already distinguish lifecycle stages, committed inputs and player-controlled inputs.

The main missing seam is a pre-commit Drunk candidate projection that can evaluate hypothetical actual-role substitutions before canonical setup is finalized.

## 4. Correct decision ownership

The first-night decision ecology must distinguish three different kinds of state:

**Storyteller/SDE controlled**
- Drunk seat selection.
- Demon bluff selection before it is shown.
- Red Herring selection.
- Storyteller-controlled impaired/ambiguous information.

**Player controlled**
- Poisoner target.
- Fortune Teller queried players.
- Other player target choices.

**Rule determined**
- Healthy deterministic information and mechanical consequences.

SDE may evaluate consequences of possible player choices when judging robustness, but future player choices must not be treated as already-known strategic inputs.

## 5. Sequencing rule: latest observable dependency, not one global precompute

The existing SDE route correctly says that first night must not be one immutable bundle and that Poisoner can invalidate uncommitted decisions. That architecture should be retained and generalized.

The new canonical sequencing principle is:

> A Storyteller-controlled value may remain planned/uncommitted until the latest semantically safe point, but it must be committed before the first interaction that can observe it or whose legal/result semantics depend on it.

For Trouble Brewing this yields:

```text
visible identities + seats fixed
-> DRUNK decision: SDE chooses one eligible Townsfolk seat
-> commit final actual/shown role map
-> player role reveal
-> first-night evil information / Demon bluffs as required by night order
-> Poisoner chooses target (player-controlled)
-> commit poison and invalidate affected uncommitted downstream plans
-> Spy sees the current Grimoire
-> later information resolves against current effective state + committed history
-> Red Herring must already be committed by its first observable dependency
-> Fortune Teller acts
-> remaining first-night actions
```

### Red Herring timing correction

The existing production flow already inserts a dedicated Red Herring interaction immediately before the Fortune Teller role interaction, and suppresses the setup interaction when Fortune Teller is only a Drunk shown identity rather than an actual role.

However, Red Herring cannot universally be delayed until that UI step. In Trouble Brewing the Spy acts before Fortune Teller and sees the Grimoire including reminder tokens. Therefore, when an actual Spy can observe the Grimoire, Red Herring must be committed before the Spy view. Without such an observer, the engine may safely defer the internal commitment until immediately before Fortune Teller.

This should be implemented as a dependency/observation barrier rather than a named `if Spy` policy hack so future scripts with other Grimoire viewers inherit the same rule.

## 6. Recommendation-algorithm impact

This is the central SDE change.

Today `SetupEvaluator.createContext` finds `game.players.firstOrNull { actualRole == Drunk }`; the whole setup recommendation surface therefore assumes exact Drunk identity is already an input. Drunk whole-bundle evaluators likewise require an already committed Drunk source.

The replacement should be a new legal candidate domain:

```text
eligible dealt Townsfolk seats
-> for each candidate seat:
     create a hypothetical projected GameState
       candidate.actualRole = Drunk
       candidate.actualType = Outsider
       candidate.actualAlignment = Good
       candidate.shownRole = original dealt Townsfolk role
     preserve every other seat
     evaluate the resulting first-night ecology
-> SDE ranks/diagnoses candidates
-> session/setup owner commits the selected candidate
```

Do not create a second rules engine. Candidate legality belongs in setup/rules code. SDE consumes legal candidates and evaluates consequences.

The hypothetical evaluation should reuse existing exact evaluators and current Drunk information domains wherever possible. After the Drunk seat is committed, the existing downstream Drunk pair/numeric/Fortune-Teller information evaluators can continue to operate on the canonical `actualRole=Drunk, shownRole=<Townsfolk>` state.

Because Poisoner acts later, Drunk selection must not consume the actual future Poisoner target. If global robustness against poisoning matters, evaluate that as unresolved/player-controlled branching rather than hindsight.

## 7. Setup asset migration strategy

Do not rewrite the 208 Drunk presets in the first implementation.

For a current preset such as:

```text
townsfolk = [Investigator, Undertaker, Virgin]
outsiders = [Drunk]
drunk_as_options = [Librarian, Fortune Teller, Slayer]
```

the selector may still choose one option deterministically, for example Librarian, but the result is reinterpreted as:

```text
visible/dealt Townsfolk = [Investigator, Undertaker, Virgin, Librarian]
hasDrunk = true
other visible roles = [Spy, Imp]
```

After seats are assigned, all four Townsfolk seats are peers in the Drunk candidate domain.

The term `selectedDrunkShownRole` must disappear from the new runtime contract. During transition, the selected asset option should be renamed/reframed as an added/replacement Townsfolk identity rather than a Drunk-bound identity.

Later, after calibration proves the new model, the dataset schema can be simplified further if maintaining three replacement-Townsfolk options is no longer useful.

## 8. Production fan-out requiring change

Primary setup ownership:
- `TroubleBrewingSetupPresetModels.kt`
- `TroubleBrewingSetupPresetJson.kt`
- `TroubleBrewingSetupPresetValidator.kt`
- `TroubleBrewingSetupPresetSelector.kt`
- `TroubleBrewingSetupPresetRotationScorer.kt`
- `TroubleBrewingShownIdentityPolicySource.kt`
- `SetupShownIdentityPolicy.kt`
- `TroubleBrewingProductionSetupPreparer.kt`
- `TroubleBrewingSetupDealPlanner.kt`
- `TroubleBrewingDealRoleResolver.kt`
- `TroubleBrewingCommittedSetupAdapter.kt`

History/persistence:
- `TroubleBrewingSetupRotationRecordFactory.kt`
- `TroubleBrewingSetupRotationHistory.kt`
- `TroubleBrewingSetupRotationHistoryStore.kt`
- `TroubleBrewingSetupCompletionPersistence.kt`

Recommendation/SDE:
- `SetupCandidateGenerator.kt`
- `SetupEvaluator.kt`
- `SetupRecommendationService.kt`
- first-night bundle candidate-space auditing
- Drunk whole-bundle evaluators
- `SdeDecisionCandidate.kt`
- DecisionTrace/replay codecs and commit correlation
- Red Herring setup-precommit adapters/coordinator

Runtime:
- `CampBoardGameHostApp.kt` setup startup sequence
- first-night recommendation prewarm
- poison commit/invalidation
- flow interaction dependency barriers

Current-only Recovery policy means this refactor does not need old-format migration support. New persisted state only needs to be internally self-consistent for the current version.

## 9. Recommended implementation route

### DLB-0 — contract tests / typed models

Introduce the explicit intermediate setup concept:
- selected preset / realized visible roster;
- `hasDrunk`;
- seat assignments of shown identities;
- no Drunk seat yet.

Add typed tests proving a Drunk preset produces one extra visible Townsfolk and does not bind it to Drunk.

### DLB-1 — visible-roster deal cutover

Refactor selector/preparer/deal planner so the selected `drunk_as_options` entry becomes an extra dealt Townsfolk, not `selectedDrunkShownRole`.

Remove the Drunk-specific pre-seat shown-identity commitment from Trouble Brewing production.

Preserve a fallback equivalence test: choosing the added Townsfolk as Drunk recreates today's final actual/shown setup.

### DLB-2 — Drunk legal candidate + hypothetical projector

Add rules/setup-owned generation of eligible Drunk seats.

Add a pure hypothetical state projector for one candidate. It must never mutate canonical session state.

Establish invariants for actual type/alignment, shown role stability, exact one-Drunk cardinality, and flow wake semantics.

### DLB-3 — SDE Drunk decision / shadow trace

Add a typed Drunk-seat SDE decision and commit-reference kind.

Evaluate every legal candidate using existing exact/bundle machinery against the projected state. Capture DecisionTrace/replay evidence.

Start shadow-only. Do not alter frozen `BEGINNER_CONSERVATIVE_V1` weights merely to force a winner. Use existing supported features and collect evidence for any new strategic preference that is not already represented.

### DLB-4 — canonical Drunk commit before reveal

Integrate the selected Drunk seat into setup/session ownership before player role reveal and before first-night prewarm.

Only after this commit create the final `CommittedClocktowerSetup` and canonical `GameState`.

Move rotation/completion persistence to consume the final committed setup rather than a pre-Drunk deal plan.

### DLB-4A — Storyteller Drunk-selection interaction / UX

The late-bound Drunk decision must be a first-class setup interaction, not a hidden recommendation side effect.

Placement:

```text
visible identities assigned to seats
-> compute legal Drunk candidate seats + SDE recommendation
-> EXPERIENCED mode: show "Choose the Drunk" setup step
     - eligible dealt Townsfolk only
     - SDE recommendation preselected/highlighted
     - Storyteller may choose any legal candidate
     - confirm commits the Drunk seat
-> BEGINNER mode: automatically commit the SDE-selected legal candidate
     - no manual step is shown
     - flow advances directly to player identity reveal
-> final CommittedClocktowerSetup / GameState
-> player identity reveal
```

Manual authority is deliberately restricted to experienced Storyteller mode. The manual UI chooses only among the rules-owned legal candidate domain; it cannot create an illegal Drunk assignment. Once confirmed, manual and automatic selections enter the same canonical commit boundary, history, DecisionTrace/replay and Recovery semantics.

The UX should therefore consume one shared typed decision object rather than maintaining separate "automatic Drunk" and "manual Drunk" state paths. The mode changes selection authority/presentation only:

- BEGINNER: AUTO selects and commits the recommended candidate, then skips the screen;
- EXPERIENCED: ASSISTED presents the same candidate pool and recommendation but leaves final choice to the Storyteller.

This interaction belongs after seat assignment and before player role reveal. It is not a normal first-night wake step because the chosen actual/shown identity map must already be committed before any player sees their character.

### DLB-5 — staged first-night planner

Decompose the current monolithic setup recommendation surface:
- Demon bluffs commit at their actual first-night presentation dependency.
- Poisoner remains player-controlled and its confirmation invalidates later uncommitted decisions.
- information decisions are generated/re-evaluated just in time against current effective state and committed history.
- Red Herring becomes a lazy setup commitment with an observation/dependency barrier, rather than a value selected by the initial setup recommendation plan.

### DLB-6 — old contract retirement

Remove obsolete `selectedDrunkShownRole` production/persistence fields, the Trouble Brewing use of pre-seat `SetupShownIdentityPolicy`, obsolete score penalties/tests, and old setup-plan assumptions.

Re-run producer/consumer searches after removal.

### DLB-7 — acceptance

Required behavioral coverage includes:
- all Drunk templates realize a legal visible roster;
- every eligible dealt Townsfolk can be projected as Drunk;
- no Minion/Demon/Outsider seat becomes Drunk;
- committed Drunk retains original shown Townsfolk identity;
- Drunk-as-Fortune-Teller wakes as Fortune Teller but creates no real Fortune Teller Red Herring;
- actual Fortune Teller does create a Red Herring commitment;
- Spy-visible setup state includes Drunk, confirmed poison target, and Red Herring when those facts must already be observable;
- poison confirmation changes canonical revision and prevents stale downstream recommendation reuse;
- DecisionTrace/replay correlates the committed Drunk candidate;
- Recovery restores only the current finalized setup semantics;
- ordinary non-Drunk templates remain behaviorally unchanged.

## 10. Explicit non-goals

This refactor must not:
- let SDE own legality;
- choose the Poisoner target for the player;
- use future Fortune Teller/Poisoner choices as hindsight inputs;
- add fixture-specific role-name heuristics to force a preferred Drunk;
- add new policy weights without evidence;
- preserve obsolete save-format compatibility;
- rebuild the existing exact world engine or role-information legal domains.

## 11. Audit conclusion

The change is substantial but does not require replacing the template corpus or the existing SDE infrastructure.

The safest architecture is:

```text
template = visible-roster intent + hasDrunk
deal = shown identities + seats
SDE = choose actual Drunk by hypothetical whole-ecology comparison
commit = actual/shown role map
runtime = sequential decisions with dependency barriers and replanning
```

The highest-risk area is not the `PlayerState` model; that model is already suitable. The highest-risk area is the current early setup pipeline, where Drunk identity, Red Herring, Demon bluffs and first-night recommendations are coupled into a setup-time view of a state that should now remain partially unresolved.
