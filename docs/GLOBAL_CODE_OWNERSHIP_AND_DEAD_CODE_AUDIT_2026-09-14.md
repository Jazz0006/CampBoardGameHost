# GLOBAL CODE OWNERSHIP AND DEAD-CODE AUDIT

> Date: 2026-09-14 Australia/Sydney
> Status: **HISTORICAL AUDIT / implementation snapshot, not current execution authority**. The original snapshot recorded steps 1–3 complete, step 4 awaiting acceptance and steps 5–6 remaining. Current completion and priority are maintained only in [roadmap](CURRENT_DEVELOPMENT_ROADMAP.md).
> Baseline: PR #123 campaign head `486473bac8ab0c717ca84a54a32db035e9413901`
> Current implementation heads: step 1 `8f5d7668`; step 2 `590cac55`; step 3 merged as `b7756062`; step 4 PR #127 code head `fec12982`
> Current-route note (2026-09-25): SDE integration C0–C3 is complete at `8855d461` with full CI #3451 success; ownership and next priority remain governed by the roadmap. R2/base integration is still pending.

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
3. **COMPLETE — unify registration domain and automatic-selection ownership.** PR #126 merged as
   `b7756062`; candidate legality, registration facts and automatic selection now converge behind one
   domain/session boundary.
4. **COMPLETE — make production restore and restore tests consume the same composition boundary.**
   PR #127 merged as `092ca62f`; production and tests now consume one restore composition owner.
5. **COMPLETE — cut No Greater Joy setup over to the generic provider/source/shown-identity
   pipeline.** PR #128 merged as `0aad20e3`; seeded composition, seating and shown identity now have
   one production owner.
6. **IN PROGRESS — reduce Host/App gameplay ownership and converge square-table presentation.** This includes
   centralizing the duplicated phase conversion, moving mechanical projection out of Compose, and
   extracting only small shared presentation seams rather than a universal configurable screen. The
   live fan-out and architecture pre-flight are recorded in section 13.

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
- PR #126 code head `eb0873bdeb3ca00feaa83d113ec29083bd81c75c` was based exactly on merged
  `main` `d6773a2e14ea5242c9fa432893d40f3a566ec625`. Remote Android Full, ASP contracts,
  Real Clingo, aggregate CI and R2 boundary checks all passed; the PR merged as
  `b77560628054dee90c78a91d4bab8cb4185fda59` after explicit user authorization.
- Final producer/consumer search found no remaining `canMisregister` input or UI role/team legality
  reconstruction. Remaining direct `RegistrationFact` constructors are persistence/semantic-world
  decoding/projection adapters, not competing registration legality owners.

## 11. Step 4 live fan-out audit and architecture pre-flight

> Recorded before production editing on branch `codex/global-ownership-cleanup-4` from merged
> `main` `b77560628054dee90c78a91d4bab8cb4185fda59`.

### 11.1 Production and test entry-point classification

- Production has one direct reconstruction caller: `ClocktowerHostScreen` invokes
  `NightTransactionReconstructor.reconstruct` for an active other night, using the current typed
  `ClocktowerNightCheckpoint`, base `GameState` and canonical interaction plan.
- `NightTransactionRestoreComposition.restore` is production code but has no production caller. It
  decodes persisted checkpoint values and delegates to the same reconstructor; its callers are
  `NightTransactionRestoreCompositionTest` and
  `NightDawnRestoreRetryConvergenceAcceptanceTest`.
- `NightTransactionReconstructionContractTest`,
  `NightTransactionReconstructorSuccessionLegalityTest` and
  `NightTransactionHostIntegrationSmokeTest` call the lower reconstructor directly. They protect
  durable reconstruction, succession legality and Host-facing derived state, so they must consume
  the same composition entry as production rather than remain parallel entry points.
- App recovery decodes saved-game fields into App-owned state and reconstructs the current typed
  checkpoint before rendering Host. That is an intentional persistence/state adapter; Step 4 does
  not move App state ownership or add a second saved-game decoder.
- `ClocktowerNightCheckpoint.fromPersistedValues` remains the compatibility codec for persisted
  checkpoint maps. Missing-field behavior and all persisted key meanings are intentionally exempt
  from structural change.

### 11.2 Architecture pre-flight

- current owner: reconstruction logic is implemented by `NightTransactionReconstructor`, while a
  test-only `NightTransactionRestoreComposition` facade owns persisted-map decoding and delegates to
  it; production and restore acceptance therefore enter through different boundaries.
- proposed responsibility: `NightTransactionRestoreComposition` becomes the single callable
  composition owner for both a typed current checkpoint and persisted checkpoint values. The
  reconstruction algorithm becomes private implementation inside that owner.
- authoritative state owner(s): `ClocktowerNightCheckpoint` plus base `GameState` remain durable
  inputs; the canonical flow owns interaction ordering; `ClocktowerGameSession`/App retain session,
  revision, timeline and persistence authority.
- narrow typed input/output seam: typed checkpoint + base game + canonical interaction IDs + Demon
  successor identity -> immutable `NightTransactionReconstruction`; persisted values first decode to
  the typed checkpoint and return that checkpoint beside the same reconstruction.
- keep in current owner / extract: keep checkpoint decoding in `fromPersistedValues`, canonical
  mechanics in the existing pure reconstruction algorithm and Host rendering downstream; fold the
  algorithm behind the composition object and remove the separately callable reconstructor entry.
- reason: this lets production, focused contracts and restore acceptance execute the same seam
  without a redundant serialize/decode round trip, changing persisted schema or moving mutable
  state into presentation.

### 11.3 Evidence plan

- This is a behavior-preserving ownership refactor with strong existing typed coverage; no new RED
  is required.
- Establish a GREEN baseline for the five reconstruction/restore/Host-integration test classes.
- Migrate production and those tests to the composition seam, then rerun the focused tests,
  `:app:testFast`, `:app:testFull :app:assembleDebug`, persistence/restore-triggered T2 evidence,
  `git diff --check` and a final direct-caller search.

### 11.4 Implemented ownership convergence and local evidence

- `NightTransactionRestoreComposition.compose` is now the single typed composition entry used by
  production Host and the focused reconstruction, succession and Host-integration contracts.
- `NightTransactionRestoreComposition.restore` decodes persisted checkpoint values and delegates to
  that same typed entry; restore-composition and retry-convergence acceptance tests continue to
  exercise the persisted path.
- The reconstruction algorithm and `NightTransactionReconstruction` result moved unchanged behind
  the composition owner. The separately callable `NightTransactionReconstructor` file/entry was
  deleted, and the final production search finds no direct reconstructor caller.
- No persisted fields, defaults, missing-field compatibility, transaction ordering, session state,
  canonical interaction ordering or mechanics were changed.
- Local GREEN evidence: the five focused restore/reconstruction/Host-integration classes before and
  after the move; forced `:app:testFast` (1,335 tests, zero failures); forced
  `:app:testFull :app:assembleDebug` (1,352 JVM tests, zero failures); `git diff --check`.
- PR #127 code head `fec12982d370fe1b0bdcde0dd313fb5366a3ad40` is based exactly on merged
  `main` `b77560628054dee90c78a91d4bab8cb4185fda59`. Remote Android Full, ASP contracts,
  Real Clingo, aggregate CI and R2 boundary checks all passed; PR #127 merged as
  `092ca62f7806bd353e683c2dee1c7a134e4304f5` after explicit user authorization.

## 12. Step 5 live fan-out audit and architecture pre-flight

> Recorded before production editing on branch `codex/global-ownership-cleanup-5` from merged
> `main` `092ca62f7806bd353e683c2dee1c7a134e4304f5`.

### 12.1 Production and test fan-out classification

- `CampBoardGameHostApp.startClocktowerGame` routes Trouble Brewing to its production preparer but
  keeps No Greater Joy on App-root `generateClocktowerAssignments`. That path owns unseeded role
  selection, Baron distribution adjustment, seat shuffling and Drunk shown-role selection.
- `ClocktowerAssignment` and `generateClocktowerAssignments` are therefore the parallel NGJ
  production implementation. App-root `clocktowerDistribution` is additionally consumed by the TB
  preset validator, setup-screen presentation and `NoGreaterJoySetupRegressionTest`; its duplicated
  data must move to one setup-layer distribution owner rather than simply be deleted. The NGJ 5/6
  expectations move to the typed production setup boundary.
- `GeneratedSetupCandidateSource` already owns deterministic legal composition generation and the
  capped Baron outsider adjustment used by NGJ. Its direct callers are tests only before Step 5.
- `ClocktowerSetupProvider` and `ClocktowerSetupProviderRegistry` already own script/provider
  attribution and cross-script rejection. The registry is test-only before Step 5 and must become
  part of the NGJ production preparation path.
- `SetupShownIdentityPolicyResolver` owns legal generated Drunk shown-role options, and
  `SetupShownIdentityCommitter` owns the deterministic selection. Both are test-only for NGJ before
  Step 5 and are must-inherit production consumers.
- `CommittedClocktowerSetup` is the exact post-commit actual/shown identity fact already retained by
  App state and recovery. NGJ currently leaves that state unset; the new production preparation
  boundary must materialize it directly.
- Trouble Brewing templates, rotation history, deal planner and recommendation precompute remain
  intentionally exempt. Step 5 must not change their provider, selection, persistence or reveal
  transaction.
- NGJ role-card localization is an intentional App presentation adapter: committed role IDs are
  resolved to the existing localized `ClocktowerRole` values after the setup transaction returns.

### 12.2 Architecture pre-flight

- current owner: NGJ setup semantics are split between App-root random generation and the test-only
  generic provider/source/shown-identity pipeline.
- proposed responsibility: one pure `NoGreaterJoyProductionSetupPreparer` resolves the registered
  generated provider, selects its single deterministic candidate, resolves and commits shown
  identity, deterministically assigns roles to canonical seats, and returns
  `CommittedClocktowerSetup`.
- authoritative state owner(s): the validated NGJ ruleset owns available characters and teams; the
  generic source owns actual-role composition; shown-identity policy/commitment own Drunk identity;
  the new preparer owns only composition of those immutable results; App/session retain game state,
  lifecycle, recovery and persistence authority.
- narrow typed input/output seam: validated NGJ ruleset + player count + setup seed -> immutable
  `CommittedClocktowerSetup` with generated provenance and canonical seat assignments.
- keep in current owner / extract: keep localization/card materialization and game lifecycle wiring
  in App; connect the existing generic setup owners behind the new typed preparer, centralize base
  distribution in the setup layer for source/validator/UI consumption, and delete the App-root
  generation implementation after all callers migrate.
- reason: this completes the already-designed production cutover without moving lifecycle state or
  introducing a generic App context, while making actual/shown identity reproducible from the one
  prepared seed and independently testable outside Compose.

### 12.3 Evidence plan

- Add a typed RED at the new production preparation boundary proving stable generated provenance,
  exact 5/6-player team distributions, deterministic seat/identity commitment and legal distinct
  Drunk shown identity.
- Preserve the existing generated-source, provider-registry, shown-policy/commitment and NGJ
  downstream semantic acceptance tests as owning baseline evidence.
- After cutover, rerun focused setup tests, `:app:testFast`, `:app:testFull :app:assembleDebug`,
  `git diff --check`, and final production producer/consumer searches.

### 12.4 Implemented production cutover and local evidence

- `NoGreaterJoyProductionSetupPreparer` is the production composition owner. It resolves the NGJ
  provider through `ClocktowerSetupProviderRegistry`, obtains the deterministic generated candidate,
  resolves and commits shown identity, deterministically assigns the canonical role multiset to
  seats, and returns the exact `CommittedClocktowerSetup` fact.
- `CampBoardGameHostApp.startClocktowerGame` now creates the seed before NGJ preparation, consumes
  that committed fact to materialize localized cards, and retains the fact after resetting the game
  lifecycle. App no longer selects NGJ roles, shuffles seats, or chooses a second Drunk shown role.
- `clocktowerSetupDistribution` is the single setup-layer base-distribution owner consumed by the
  generated source, TB preset validator and setup-screen presentation adapter. The duplicated
  App-root distribution and `generateClocktowerAssignments` implementation were deleted.
- The typed RED failed only because `NoGreaterJoyProductionSetupPreparer` did not yet exist. It is
  GREEN after implementation and proves deterministic committed identities, generated provenance,
  canonical seats, legal distinct Drunk shown identity, and every legal 5/6-player Baron/non-Baron
  distribution across a seed matrix.
- Existing generated-source, provider-registry, shown-policy/commitment, NGJ semantic acceptance,
  NGJ product-surface and TB preset-validation focused tests pass. Forced `:app:testFast` passes.
  Forced `:app:testFull :app:assembleDebug` passes with 1,353 JVM tests and zero failures.
- `git diff --check` passes. Final production search finds no `generateClocktowerAssignments`,
  `ClocktowerAssignment`, App-root `clocktowerDistribution`, or second NGJ random setup path.
- PR #128 code head `02b8a7d79ebc369fbabcfa82d259078bf22dd18b` is based exactly on merged
  `main` `092ca62f7806bd353e683c2dee1c7a134e4304f5`. Remote Android Full, ASP contracts,
  Real Clingo, aggregate CI and R2 boundary checks all passed. PR #128 merged as
  `0aad20e341af912338f9fae7ea11aca5d71c539f` after explicit user authorization.

## 13. Step 6 live fan-out audit and architecture pre-flight

> Recorded before production editing on branch `codex/global-ownership-cleanup-6` from merged
> `main` `0aad20e341af912338f9fae7ea11aca5d71c539f`.

### 13.1 Phase-conversion fan-out

- The complete `ClocktowerPhase -> StorytellerPhase` mapping is repeated in
  `StructuredNumericInformationAdapter`, `StructuredBooleanInformationAdapter`, two App-root
  paths, and two Host paths. Every caller must inherit one exhaustive pure conversion.
- These paths cover structured numeric/boolean recommendation requests, semantic event publication,
  dynamic recommendation state and observation drafts. None is intentionally exempt.

### 13.2 Night mechanical-projection fan-out

- `ClocktowerHostScreen` currently composes canonical dawn-death resolution, Mayor redirect
  eligibility/candidates, current Demon authority, succession legality, Ravenkeeper/Sage triggers,
  other-night waking facts, canonical interactions, reconstruction events and effective night state
  inside the composable.
- Downstream consumers are role-specific materializers, Chambermaid eligibility, Fortune Teller
  current-role authority, registration subject projection, callback validation and the final Judge
  surface. They must inherit one immutable projection; presentation must not reconstruct mechanics.
- Existing rules/session owners remain authoritative for death, succession, interaction ordering,
  checkpoint reconstruction, poison lifecycle and ability functioning. The new projection composes
  those owners; it does not replace their algorithms or own mutable state.
- First-night behavior, non-Night fallback behavior and all callback/commit/persistence ordering are
  intentionally exempt from semantic change.

### 13.3 Square-table presentation fan-out

- Six production files directly repeat `ClocktowerHostFullScreenScaffold` plus
  `ClocktowerSquareTableSeatSurface` and the same `HostSeatPresentation -> seat content -> UI model`
  bridge: Fortune Teller, Chef, Empath, Undertaker, pair information and the existing generic night
  action surface.
- Existing role-specific selection state, badges, manual-edit back behavior, center controls and
  interaction eligibility remain with their cohesive UI owners.
- The shared seam must own only navigation labels/scaffold, stable seat-content mapping, interaction
  mode and render-key-to-seat routing. It must allow callers to supply their role-specific visual
  state and center content; it must not become a universal role configuration object.

### 13.4 Architecture pre-flight

- current owner: phase translation is duplicated across adapters/App/Host; Host Compose assembles
  immutable night mechanics from canonical lower owners; six square-table screens repeat the same
  outer presentation bridge.
- proposed responsibility: a pure phase extension owns phase translation; a typed
  `ClocktowerNightHostProjection` composes existing rules/session results outside Compose; a small
  `ClocktowerHostSquareTableScaffold` owns only the repeated presentation shell and seat bridge.
- authoritative state owner(s): App/`ClocktowerGameSession` retain lifecycle, checkpoint, timeline,
  recovery and persistence state; existing rules/session algorithms retain mechanics; role UI owns
  transient selection/edit state; the new seams own only immutable conversion/composition.
- narrow typed input/output seam: phase -> storyteller phase; cards + ruleset + seed + checkpoint +
  resolved prior-night facts -> immutable host projection with typed queries; seats + navigation +
  caller-supplied seat visual -> rendered square-table shell.
- keep in current owner / extract: retain callbacks, mutable registration maps, recommendation
  invocation and role materializers in Host; extract only mechanical fact composition and effective
  state queries. Retain role-specific center content and event handling in each square-table file.
- reason: this removes duplicated interpretation and presentation plumbing while preserving clear
  dependency direction and avoiding a broad Host context, screen state bag or universal UI.

### 13.5 Evidence plan

- Add a typed phase-conversion contract and a typed night-host projection contract at their true
  ownership boundaries; establish meaningful RED before implementing each new seam.
- Use existing square-table state/presentation tests and full compilation as behavior-preserving
  refactor evidence; do not manufacture a UI source-string RED.
- Run focused phase, night transaction/effective-state/Host integration and square-table tests,
  forced `:app:testFast`, forced `:app:testFull :app:assembleDebug`, `git diff --check`, and final
  duplicate/direct-caller searches.

### 13.6 Implemented boundary and local evidence

- `ClocktowerPhase.toStorytellerPhase()` is now the sole complete four-value production conversion.
  Structured numeric/boolean adapters, both App paths and both Host paths consume it; the final
  production search finds the four-value mapping only in the adapter.
- `ClocktowerNightHostProjectionFactory` now composes the existing death, Mayor redirect, current
  Demon, succession, other-night flow, checkpoint reconstruction, poison chronology and ability
  functioning owners outside Compose. `ClocktowerHostScreen` consumes the immutable projection and
  retains only orchestration aliases, materialization and callback wiring.
- `ClocktowerHostSquareTableScaffold` now owns the common navigation labels, full-screen scaffold,
  square-table seat surface and render-key-to-stable-seat routing. The generic night-action surface
  plus Fortune Teller, Chef, Empath, Undertaker and pair-information screens all delegate to it.
  Role-specific state, badges, center controls and pair-edit back behavior remain local. The ordinary
  Host table remains an intentional overview-surface exemption.
- Meaningful typed REDs failed only because the new phase adapter and night-host projection factory
  did not yet exist. Both are GREEN after implementation. Focused phase, mechanical chronology,
  Host integration, Demon authority, square-table presentation and ownership tests pass.
- Forced `:app:testFast` passes. Forced `:app:testFull :app:assembleDebug` passes. `git diff --check`
  passes, the final direct-caller search leaves only the shared scaffold plus the intentional Host
  overview, and no unrelated `.DS_Store` artifacts remain in the worktree.
- PR #129 code head `ceca17cb30a9ea0ca6a2c13c80f8c20da1a1e742` is based exactly on merged
  `main` `0aad20e341af912338f9fae7ea11aca5d71c539f`. Remote Android Full, ASP contracts,
  real-Clingo, aggregate CI and R2 boundary checks all passed. The PR remains Draft and unmerged
  pending explicit user authorization.
