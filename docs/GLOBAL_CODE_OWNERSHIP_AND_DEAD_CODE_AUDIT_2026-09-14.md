# GLOBAL CODE OWNERSHIP AND DEAD-CODE AUDIT

> Date: 2026-09-14 Australia/Sydney
> Status: **IMPLEMENTATION IN PROGRESS — steps 1 and 2 complete; step 3 remote gates pass and awaits user acceptance; steps 4-6 remain**
> Baseline: PR #123 campaign head `486473bac8ab0c717ca84a54a32db035e9413901`
> Current implementation heads: step 1 `8f5d7668`; step 2 `590cac55`; step 3 PR #126 code head `eb0873bd`

## 1. Scope and method

This audit covers production Kotlin, tests, Android resources, manifest reachability, persistence and
recovery entry points, temporary/legacy compatibility paths, repeated code windows and major
UI/domain ownership boundaries.

Static reachability was checked against direct production callers, test-only callers, Android
Manifest entries, resource references and known persistence codecs. No application-defined dynamic
class loading, reflection-based dispatch, dynamic resource-ID lookup, Gson, Moshi or kotlinx
serialization entry point was found.

Local Gradle verification was unavailable because the Gradle 9.5 distribution was not cached and
the execution environment could not reach the wrapper download endpoint. This is an audit result,
not a new test-acceptance claim.

## 2. Pre-cleanup inventory baseline

| Metric | Result |
|---|---:|
| Production files | 333 |
| Production Kotlin files | 319 |
| Production Kotlin lines | 51,867 |
| Test Kotlin lines | 40,534 |
| Top-level declarations | 1,034 |
| Confirmed declaration-only dead roots, excluding Manifest entry points | 12 |
| Production declarations called only by tests | 23 |
| Confirmed dead Kotlin physical lines | at least 1,063 |
| Defined string resources | 332 |
| Static unreferenced string resources | 168 |
| Lexical unused-import candidates | 781 across 21 files |
| Overlapping normalized cross-file eight-line clone windows | 330 |

The clone-window total is a search signal, not 330 independent defects. Large overlapping runs and
mechanically similar Compose shells account for many windows.

These numbers describe the audit baseline before implementation. They are retained as historical
evidence and must not be presented as the post-cleanup repository inventory. Step 2 subsequently
removed 2,268 lines across its two commits; a fresh global inventory should be generated only after
the ownership campaign is complete, so intermediate structural moves are not mistaken for final
progress.

## 3. Confirmed dead production code

### 3.1 Entire obsolete setup UI subgraph

`AppPlayerSetupScreens.kt` is a 770-line dead subgraph. `Screen.Setup` routes to
`SeatingFirstSetupScreen`; `SetupScreen` has no caller. Its private descendants
`PlayerDragState`, `RoundTableSetupEditor`, `DraggableAvatar` and `BenchPlayerChip` are reachable
only from that dead entry.

### 3.2 Dead UI blocks and helpers

- `HostInteractionUi.kt`: `HostProgressCard`, `HostScriptCard`, its private-use
  `HostInstructionBlock`, `SelectablePlayerChips`, `SelectableTwoPlayerChips` and
  `SelectableSeatNumbers` are dead. `HostActionSection` is live and prevents whole-file deletion.
- `GameSettingsHeader` is dead; `StepperRow` remains live.
- `HostToolsTopBar` is dead; the remaining game-review screens are live.
- `RecommendationReasonSummary` is dead; the rest of the recommendation UI is live.
- `A4ObservationCacheUpdateReport` is dead; its coordinator, entries and enums remain live.
- `UNIFIED_SETUP_SELECTOR_BENCHMARK_LOG_TAG` and
  `UNIFIED_FIRST_NIGHT_POOL_BENCHMARK_LOG_TAG` are unused.

### 3.3 Obsolete parameters

`ClocktowerNightStepCardLocalized` receives but never reads six values:

- `spyRegistrationGood`;
- `spyRegisteredRoleEnName`;
- `spyRegistrationRecommendations`;
- `recluseRegistrationEvil`;
- `recluseRegisteredRoleEnName`;
- `recluseRegistrationRecommendations`.

The related change callbacks remain live through `ClocktowerAutomaticRegistrationEffect`; only the
unused value fan-in is confirmed dead.

### 3.4 Resources and imports

There are 168 static unreferenced strings, including 73 `clocktower_*` and 47 `werewolf_*` keys.
No dynamic resource lookup was found. Removal must update both locale files in one change.

There are 781 lexical unused-import candidates. Of these, 756 are concentrated in
`ClocktowerNightScreen.kt`, `ClocktowerDayScreen.kt`, `ClocktowerHistoryScreen.kt`,
`CampBoardGameHostApp.kt` and `ClocktowerHostScreen.kt`. Compiler/IDE import optimization must be
used as the final authority rather than deleting solely from the lexical count.

## 4. Test-only production surfaces

Twenty-three production declarations have no runtime production caller but are exercised by tests.
They must not be bulk-deleted because they fall into different categories:

1. stale facades, including `RecommendationService` and `ClocktowerScriptCatalog`;
2. test-only composition wrappers, especially `NightTransactionRestoreComposition`;
3. generic setup architecture not yet reached by production, including
   `GeneratedSetupCandidateSource`, `ClocktowerSetupProviderRegistry` and
   `SetupShownIdentityPolicyResolver`;
4. formal decision/epistemic architecture not yet cut over, including
   `StorytellerDecisionRequest`, `DynamicDecisionTransactionAggregate`,
   `TroubleBrewingRegistrationSemantics`, `SpyGrimoireTruthProjector` and the B4 shadow coordinator;
5. presentation/test helpers for Empath, Chambermaid, Undertaker, Ravenkeeper and Dawn projection;
6. diagnostic/calibration owners such as `SelectionDistributionReviewer` and
   `ToleranceCalibration`.

Each item needs an explicit decision: connect production, move to test fixtures, retain under a
documented future milestone, or delete. "Covered by tests" is not proof that deployed production
uses the abstraction.

## 5. Priority ownership findings

### P0 — Virgin Spy automatic decision identity was not game-scoped — FIXED IN STEP 1

The Virgin/Spy fallback in `ClocktowerHostScreen.kt` builds a deterministic key from Spy name and
legal roles only. It omits game ID, phase, round and sequence. Different games with the same name
and legal-role set therefore reuse the same pseudo-random ruling. Other night registration paths
already use `clocktowerTemporaryNightDecisionKey`.

Step 1 introduced one game/phase/round/registration-scoped identity owner and routed the Virgin Spy
fallback through it. The typed regression proves stability for the same identity and isolation
across game, phase, round and registration identity. The accepted implementation head is
`8f5d7668` on PR #124; its Android, ASP, real-Clingo, aggregate CI and R2 checks passed.

### P1 — No Greater Joy setup remains a parallel App-root implementation

Trouble Brewing production uses `TroubleBrewingProductionSetupPreparer`. No Greater Joy still uses
App-root `clocktowerDistribution`, `generateClocktowerAssignments`, random/shuffled selection and
App-local Drunk shown-role handling.

The generic deterministic `GeneratedSetupCandidateSource`, provider registry and shown-identity
policy exist but are test-only. The player-count distribution is duplicated between App root and
the generic source. This is an incomplete production cutover, not harmless unused architecture.

### P1 — Registration legality and selection have multiple production owners

The formal `TroubleBrewingRegistrationSemantics` exists only in tests. Production separately uses:

- Host-local `spyCanRegister`, `recluseCanRegister`, team filtering and state maps;
- `ClocktowerAutomaticRegistrationEffect` plus the temporary 90/10 policy for night UI;
- direct `WeightedStableSelector.selectStyle` paths for day Virgin/Slayer/Klutz behavior;
- a `unifiedRegistrationPool` that is itself test-only.

Candidate legality, registration fact projection and automatic selection must converge behind one
domain/session boundary. UI may select/display a legal option but must not regenerate legality.

### P1 — Recovery composition tested is not the production entry

`NightTransactionRestoreComposition` is called only by tests, while production Host calls
`NightTransactionReconstructor` directly. The wrapper currently delegates to the same algorithm,
so this is not yet two reconstruction algorithms; it is a test/production entry-point mismatch.

Either production must use the composition boundary or the facade must be removed and tests moved
to the real production entry.

### P1 — Host UI constructs mechanical projections

`ClocktowerHostScreen.kt` uses canonical resolvers but still creates resolved mechanical events,
effective night state, Ravenkeeper/Sage triggers, Mayor redirect candidates, waking-role facts and
Spy/Recluse legal-role lists inside Compose ownership. `ClocktowerJudgeScreen` has 89 parameters and
the file has 28 remembered state containers; App root has approximately 73 remembered states.

The target architecture is a session-owned `NightHostProjection` or equivalent immutable model.
Presentation should render it and submit intents, not rebuild game facts.

### P2 — Repeated phase translation

The four-way `ClocktowerPhase -> StorytellerPhase` mapping is duplicated six times across the two
structured adapters, App root and Host. It must become one pure shared conversion.

### P2 — Square-table presentation clones

At least eight screens repeat the full-screen scaffold, navigation wiring, seat-model mapping and
seat-surface projection. Three single-target screens repeat the same target-seat presentation path.
Extract a small shared shell and seat projection; do not introduce a universal configuration
component that hides role-specific behavior.

### P2 — Recommendation facade ownership is false

`RecommendationService` claims to be the Android UI's single entry point, but production imports
and calls `SetupRecommendationService` directly. Make the facade authoritative or delete it.

## 6. Compatibility paths that remain architecturally meaningful

Do not delete these merely because their names include Legacy, Temporary or Fallback:

- `TemporaryAutomaticStorytellerPolicy` is the documented policy until EPI-MQ replaces it;
- `FirstNightInformationMigration` still provides parity shadow/fallback for non-authoritative
  first-night families; Washerwoman/Librarian/Investigator have already cut over to the shared pair
  domain, while Chef/Empath/Fortune Teller have not;
- legacy epistemic JSON readers are persistence compatibility boundaries;
- `LegacyRulesetCatalogAdapter` remains called by the built-in catalog;
- missing-field Ghost Vote recovery behavior is an intentional old-payload compatibility contract.

The problem is caller sprawl and duplicate candidate construction, not the existence of a named
compatibility boundary.

## 7. Areas without a duplicated authority finding

- System-bar hiding is owned by `MainActivity`; other files contain stale imports, not a second
  implementation.
- Host bottom navigation inset behavior is centralized in `ClocktowerHostBottomInsetPolicy` and
  consumed by `ClocktowerHostFullScreenScaffold`.
- App root does not contain a second recovery JSON decoder; strict decoding and restore planning
  remain separate owners.
- Death, poison and Demon succession core algorithms are not independently duplicated. The risk is
  UI reconstruction/projection around shared resolvers, not a second full rule engine.
- No writerless App/Host remembered state was proven by the static use scan.

## 8. User-confirmed implementation sequence and progress

1. **Fix Virgin decision identity — COMPLETE.** PR #124 head `8f5d7668` scopes automatic
   registration decisions to game, phase, round and registration identity.
2. **Independent behavior-neutral cleanup — COMPLETE.** PR #125 head `590cac55`:
   - deletes the obsolete 770-line Setup UI subgraph and confirmed dead UI/helpers;
   - removes the six unread registration value parameters while retaining live callbacks/state;
   - removes both unused benchmark constants;
   - removes all 168 originally identified unreferenced strings plus 13 strings orphaned by the
     dead-UI deletion from both locales (181 per locale);
   - removes 790 lexically confirmed unused imports (the original 781 plus 9 orphaned by cleanup);
   - leaves both locale key sets and format placeholders aligned, with zero missing static string
     references and zero remaining statically unreferenced main-string definitions.
3. **NEXT — unify registration domain and automatic-selection ownership.** Candidate legality,
   registration facts and automatic selection must converge behind one domain/session boundary.
4. Make production restore and restore tests consume the same composition boundary.
5. Cut No Greater Joy setup over to the generic provider/source/shown-identity pipeline.
6. Finally reduce Host/App gameplay ownership and converge square-table presentation. This includes
   centralizing the duplicated phase conversion, moving mechanical projection out of Compose, and
   extracting only small shared presentation seams rather than a universal configurable screen.

The six numbered items above are the authoritative sequence. Each behavior-changing ownership
cutover should remain independently reviewable; do not combine dead-code deletion, registration,
setup migration and broad Host/App decomposition in one PR.

Step 2 verification at `590cac55` includes full Android `testFull + assembleDebug`, ASP contracts,
real Clingo cross-validation, aggregate CI and R2 boundary checks. The obsolete R2 positive
existence guard for `RecommendationReasonSummary` was replaced with a production-wide absence
guard in `590cac55`.

## 9. Continuation gate

The user authorized merging steps 1 and 2 after this document, the roadmap and the new-conversation
handoff are updated. Merge PR #124 first, retarget PR #125 to `main`, revalidate its exact final
head, then merge PR #125. Do not open the step 3 PR in this closeout conversation; begin it from the
new conversation after re-querying live `main` and repository state.

## 10. Step 3 live fan-out audit and architecture pre-flight

> Recorded before production editing on branch `codex/global-ownership-cleanup-3` from merged
> `main` `d6773a2e14ea5242c9fa432893d40f3a566ec625`.

### 10.1 Production fan-out classification

- `TroubleBrewingRegistrationSemantics` is the formal legality model, but before step 3 it has no
  production caller. Its only direct caller is `EpistemicSemanticModelTest`.
- `RegistrationPolicy` is the production candidate/fact generator reached through
  `NightRecommendationModule` and `ClocktowerRecommendationCoordinator`. Before step 3 it trusts
  Host-supplied `allowedRoles` and `canMisregister`, so it is not the legality owner.
- `NaturalPairInformationCandidateGenerator` independently constructs Spy/Recluse registration
  facts for Washerwoman, Librarian and Investigator. This path must inherit the common legality and
  fact projection; its pair-outcome construction remains an intentional pair-information adapter.
- `ClocktowerAutomaticRegistrationEffect` and the Virgin automatic path independently turn
  Host-filtered role names into temporary-policy candidates. These are must-inherit automatic
  consumers; the temporary 90/10 policy remains the intentional selection-policy adapter.
- Virgin, Slayer and Klutz day flows call selection helpers inside Host. They are must-inherit
  consumers. Their existing selection probabilities/style behavior must remain unchanged.
- Host-local `spyCanRegister`, `recluseCanRegister`, role/team filtering, registered-role fallback,
  result-first numeric/role-reveal witnesses and legacy pair candidate construction are parallel
  legality/projection paths and must inherit the common domain result.
- Registration controls and localized labels are intentional presentation adapters. They may render
  domain-provided legal roles and submit a selected ruling, but must not filter teams/roles.

### 10.2 Mode, recovery and persistence classification

- Beginner and Experienced night flows share the same Host and night-step models. Beginner applies
  the temporary automatic policy; Experienced renders manual controls. Both must consume the same
  legal special-role set.
- Fresh and restored games reconstruct `PlayerCard`, confirmed poison state, phase/round and the
  effective other-night role/poison projection before Host registration decisions. This projection
  remains an intentional state adapter for step 3 and must feed the common registration domain.
- The in-progress Spy/Recluse ruling maps and their recorded flags are Compose-local draft state and
  are not fields in `ClocktowerRecoveryMechanics`. Recovery re-enters from durable cards/mechanics
  and deterministically recomputes any uncommitted automatic ruling.
- Committed Virgin/Slayer/Klutz consequences, event history and semantic decision-event
  `RegistrationFact` values are durable consumers. Step 3 must not change the recovery JSON schema,
  saved field meanings, event ordering, or established automatic decision keys.

### 10.3 Architecture pre-flight

- current owner: split between formal epistemic semantics, dynamic recommendation policy,
  Natural Pair generation, Host-local legality/filtering and temporary automatic selection.
- proposed responsibility: one pure Trouble Brewing registration domain resolves special-ability
  eligibility, complete legal candidates and their typed registration facts for a supplied
  interaction; recommendation, pair-information and automatic/manual adapters consume that result.
- authoritative state owner(s): `ClocktowerGameSession`/App remain authoritative for durable game
  and timeline state; effective-night projectors remain authoritative for interaction-time role and
  poison facts; the registration domain is authoritative only for interpreting those facts.
- narrow typed input/output seam: effective subject facts + interaction identity/question + script
  role definitions -> ordered legal registration candidates carrying `RegistrationFact` witnesses.
- keep in current owner / extract: extract legality and fact construction to the rules/domain layer;
  retain recommendation scoring in `RegistrationPolicy`, temporary probabilities in
  `TemporaryAutomaticStorytellerPolicy`, state projection in existing session/Host adapters and UI
  labels/selection intent in presentation.
- reason: this removes parallel rule interpretation without moving mutable state, persistence,
  scoring, or presentation ownership and keeps the step independently reviewable from steps 4-6.

### 10.4 Evidence plan

- Add one typed RED for the uncovered stable rule that a poisoned Spy/Recluse has no special
  registration profile.
- Establish the existing `EpistemicSemanticModelTest`, `RegistrationPolicyTest`, Natural Pair tests,
  temporary-ruling tests and Host selection characterization as the T0 baseline.
- After cutover, rerun those focused tests, `:app:testFast`, the registration-triggered ZDD/golden
  evidence, `git diff --check`, and the final producer/consumer search.

### 10.5 Implemented ownership convergence and local evidence

- `TroubleBrewingRegistrationDomain` now owns Spy/Recluse special eligibility, impairment handling,
  allowed-role normalization and typed `RegistrationFact` construction.
- Formal epistemic semantics, dynamic recommendations and Natural Pair generation consume that
  domain. A caller-supplied role set cannot broaden the subject character's registration ability.
- Beginner temporary automatic rulings and Experienced manual controls consume the same typed
  resolution. Virgin, Slayer and Klutz retain their existing decision keys, style selection and
  probability policy; registration selection now enters through the shared registration pool.
- Host retains only effective interaction-time state projection and presentation wiring. Recovery
  fields, JSON schema, timeline/event ownership and commit ordering are unchanged.
- The meaningful RED was the previously uncovered poisoned Spy/Recluse semantic contract; it failed
  before the production cutover and passed afterward.
- Local GREEN evidence: focused registration/epistemic/Natural Pair/temporary/Host-selection tests;
  forced `:app:testFast`; forced ZDD, enumerated-world and A3 golden tests; forced
  `:app:testFull :app:assembleDebug` (1,352 JVM tests, zero failures); ASP corpus validation (52
  scenarios) and 14 ASP harness tests.
- PR #126 code head `eb0873bdeb3ca00feaa83d113ec29083bd81c75c` is based exactly on merged
  `main` `d6773a2e14ea5242c9fa432893d40f3a566ec625`. Remote Android Full, ASP contracts,
  Real Clingo, aggregate CI and R2 boundary checks all passed; the PR remains Draft and unmerged
  pending explicit user acceptance.
- Final producer/consumer search found no remaining `canMisregister` input or UI role/team legality
  reconstruction. Remaining direct `RegistrationFact` constructors are persistence/semantic-world
  decoding/projection adapters, not competing registration legality owners.
