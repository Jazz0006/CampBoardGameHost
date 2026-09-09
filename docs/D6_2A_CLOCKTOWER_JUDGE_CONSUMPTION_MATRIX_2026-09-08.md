# D6.2a ClocktowerJudgeScreen Consumption / Responsibility Matrix

> Date: 2026-09-08 Australia/Sydney
> Branch: `codex/d6-2-ui-composition`
> Base main: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`
> Scope: **read-only characterization; no production edit**

## Result summary

`ClocktowerJudgeScreen` currently exposes:

```text
103 total parameters
 39 on... callbacks
  3 additional function-valued providers
 10 MutableState<T> parameters
```

The 42 function-valued parameters therefore reconcile as **39 callbacks + 3 providers**.

Two parameters have no consumer inside `ClocktowerJudgeScreen` beyond the signature:

- `records`
- `onPhaseChange`

They are dead-parameter cleanup candidates, but should not be mixed opportunistically into the first production ownership extraction if doing so broadens the slice.

## Legend

Kinds:

- `V` = value / read-only dependency at the Judge boundary
- `M` = `MutableState<T>`
- `P` = function-valued provider/read dependency
- `C` = callback/write/action dependency

Ownership / durability:

- `UI` = transient Compose/UI state or interaction data
- `D` = durable/session/domain/history/recovery-sensitive data or action
- `R` = read-only/shared composition or recommendation context
- `unused` = zero consumption inside Judge

Forwarding:

- `direct` = passed to an existing child substantially unchanged
- `adapted` = consumed by Judge/root logic or wrapped before child use
- `local` = consumed directly in Judge branch/composition logic
- `none` = zero consumer

## Complete 103-parameter matrix

| # | Parameter | Kind | Responsibility / phase | Actual consumer / forwarding path | Forwarding | Class / current owner | Cohesive destination or note |
|---:|---|:---:|---|---|---|---|---|
| 1 | `automaticStorytellerInfo` | V | recommendation / Setup + Night | Judge recommendation/materialization logic | local/adapted | R / App | recommendation read context; not first slice |
| 2 | `automaticStorytellerStyle` | V | recommendation / shared | recommendation logic | local | R / App | recommendation read context |
| 3 | `cards` | V | shared gameplay projection | Judge derives/reshapes data for Day, Night, Dawn and ability tables | adapted | R/D read / session→App | shared read projection; avoid mega context |
| 4 | `records` | V | legacy/shared | **no Judge consumer** | none | unused / App | separate dead-parameter cleanup candidate |
| 5 | `events` | V | history / Dawn / materialization | Judge filters/derives event context and forwards derived data | adapted | D read / session→App | shared durable read context |
| 6 | `script` | V | rules + recommendation / shared | Judge rules/recommendation/materialization | adapted | R / App | shared rules read context |
| 7 | `gameId` | V | recommendation identity | recommendation request context | local | R/D identity / App | recommendation read context |
| 8 | `gameSeed` | V | recommendation determinism / shared | recommendation context | local | R/D / App | recommendation read context |
| 9 | `gameStateRevision` | V | recommendation freshness | recommendation invalidation/request context | local | D read / session→App | recommendation read context |
| 10 | `playerInputRevision` | V | recommendation freshness | recommendation invalidation/request context | local | D read / session→App | recommendation read context |
| 11 | `rulesetRef` | V | recommendation/rules context | recommendation context | local | R / App | recommendation read context |
| 12 | `setupHistory` | V | Setup / First Night | setup/recommendation branch | local | D read / session→App | Setup/FirstNight seam |
| 13 | `setupRecommendationResultProvider` | P | Setup / First Night | Judge recommendation coordinator | local | R provider / App | Setup recommendation seam |
| 14 | `firstNightNaturalPairReadyProvider` | P | First Night | first-night recommendation readiness logic | local | R provider / App | FirstNight seam |
| 15 | `firstNightNaturalPairResultProvider` | P | First Night | first-night recommendation result logic | local | R provider / App | FirstNight seam |
| 16 | `onInitialRecommendationDemand` | C | Setup / First Night | Judge `LaunchedEffect`/recommendation request path before provider read | adapted | D-ish request action / App | Setup recommendation action seam |
| 17 | `phase` | V | global dispatcher | Judge selects Setup/FirstNight/Day/Night/Dawn/result branches | local | R/D read / session→App | remains dispatcher input initially |
| 18 | `round` | V | shared phase labeling/filtering | Day/Night/Dawn/event-derived presentation | adapted | D read / session→App | shared read context |
| 19 | `nightCheckpoint` | V | Night | night active/resume path | adapted | D / recovery/session | Night seam; recovery-sensitive |
| 20 | `pendingNightDeath` | V | Night + Dawn | night attack state and Dawn summary | adapted | D / session | Night/Dawn shared durable value |
| 21 | `demonAttackDraftTarget` | V | Night | Demon attack night interaction | adapted | UI-ish selection backed above / App | Night action seam |
| 22 | `selectedExecution` | V | Day | Day overview/nomination/execution presentation | adapted | D / session→App | Day mechanics seam |
| 23 | `poisonTarget` | V | Night / rules materialization | poison/night materialization | adapted | D / session | Night durable read |
| 24 | `poisonDraftTarget` | V | Night | Poisoner selection interaction | adapted | UI-ish selection / App | Night action seam |
| 25 | `fortuneTellerFirst` | V | Night | Fortune Teller interaction/materialization | adapted | UI-ish selection / App | Night FT seam |
| 26 | `fortuneTellerSecond` | V | Night | Fortune Teller interaction/materialization | adapted | UI-ish selection / App | Night FT seam |
| 27 | `chambermaidFirst` | V | Night | Chambermaid interaction/materialization | adapted | UI-ish selection / App | Night Chambermaid seam |
| 28 | `chambermaidSecond` | V | Night | Chambermaid interaction/materialization | adapted | UI-ish selection / App | Night Chambermaid seam |
| 29 | `ravenkeeperTarget` | V | Night | Ravenkeeper interaction/materialization | adapted | UI-ish selection / App | Night Ravenkeeper seam |
| 30 | `redHerring` | V | Setup / First Night / FT rules | recommendation/setup and FT truth calculation context | adapted | D / session | cross-cutting durable rule value |
| 31 | `recommendedDemonBluffRoleNames` | V | Setup / First Night | first-night demon information presentation | adapted | R / recommendation | Setup/FirstNight seam |
| 32 | `recommendedDrunkInvestigatorRoleName` | V | Setup / First Night | setup/first-night information | adapted | R / recommendation | Setup/FirstNight seam |
| 33 | `recommendedDrunkInvestigatorSeats` | V | Setup / First Night | setup/first-night information | adapted | R / recommendation | Setup/FirstNight seam |
| 34 | `butlerMaster` | V | **Night only actual Judge consumer** | Night summary/materialization/selection; Day does not consume this value | adapted | D / session | Night seam; do not misclassify as Day |
| 35 | `monkProtectedTarget` | V | Night / rules | night materialization/protection resolution | adapted | D / session | Night durable read |
| 36 | `monkProtectedDraftTarget` | V | Night | Monk selection interaction | adapted | UI-ish selection / App | Night action seam |
| 37 | `mayorRedirectTarget` | V | Night / rules | night attack materialization/resolution | adapted | D / session | Night durable read |
| 38 | `mayorRedirectDraftTarget` | V | Night | Mayor redirect interaction | adapted | UI-ish selection / App | Night action seam |
| 39 | `pendingNewDemonName` | V | Night | Imp succession/new demon flow | adapted | D / session | Night succession seam |
| 40 | `pendingNightNewDemonIdentityName` | V | Night | Night pending identity/reveal flow | adapted | D / session | Night succession seam |
| 41 | `demonSuccessorTarget` | V | Night | succession selection flow | adapted | UI/D transition / App/session | Night succession seam |
| 42 | `confirmedDemonSuccessorTarget` | V | Night | succession confirmation flow | adapted | D / session | Night succession seam |
| 43 | `virginUsed` | V | Day | Day nomination/Virgin eligibility | adapted | D / session | Day nomination seam |
| 44 | `slayerUsed` | V | Day | Day overview + Slayer eligibility/presentation | direct/adapted | D / session | keep above first Slayer UI-state slice |
| 45 | `slayerClaimedNames` | V | Day | Day overview + Slayer claimant candidate gating | direct/adapted | D / session | keep above first Slayer UI-state slice |
| 46 | `artistUsed` | V | Day | Day overview + Artist eligibility | adapted | D / session | Artist seam |
| 47 | `artistClaimedNames` | V | Day | Artist claimant candidate gating | adapted | D / session | Artist seam |
| 48 | `artistClaimantName` | V | Day / Artist | Artist table + confirmation flow | direct/adapted | UI selection / App | Artist candidate, but confirmation reads it above |
| 49 | `artistTruthfulAnswer` | V | Day / Artist | Artist answer selection + confirmation | direct/adapted | UI selection / App | Artist candidate; callback redesign required |
| 50 | `artistShownAnswer` | V | Day / Artist | Artist shown-answer selection + confirmation | direct/adapted | UI selection / App | Artist candidate; callback redesign required |
| 51 | `lastExecutedName` | V | Night / Undertaker | Undertaker materialization/information | adapted | D / session | Night information seam |
| 52 | `pendingKlutzName` | V | Day / Klutz | Klutz branch/table | direct/adapted | D / session/recovery | Day Klutz seam |
| 53 | `klutzChoiceName` | V | Day / Klutz | Klutz selection/confirmation | direct/adapted | UI/D transition / App/session | Day Klutz seam |
| 54 | `nightStartedState` | M | Night navigation | Judge night navigation; App also reads/writes checkpoint/recovery/reset | direct/adapted | UI + recovery-coupled / App | **not safe child-local extraction** |
| 55 | `nightStepIndexState` | M | Night navigation | Judge night cursor; App checkpoint/recovery/reset/restore | direct/adapted | UI + recovery-coupled / App | **not safe child-local extraction** |
| 56 | `dayModeState` | M | Day dispatcher | Judge Day branch; App Recovery/Klutz/reset writes | local | UI + recovery-coupled / App | keep above feature slices initially |
| 57 | `nominatorNameState` | M | Day nomination | nomination interaction/table | direct/adapted | UI / App | nomination seam; coupled with Day flow |
| 58 | `nomineeNameState` | M | Day nomination | nomination interaction/table | direct/adapted | UI / App | nomination seam |
| 59 | `currentVoteCountState` | M | Day voting | vote table/transaction interaction | direct/adapted | UI / App | vote seam |
| 60 | `ghostVoteAuthority` | V | Day nomination/voting | vote availability/transaction mechanics | adapted | D / session | durable Day mechanics; keep above transient UI |
| 61 | `highestVoteNameState` | M | Day voting | vote result/leading execution presentation; recovery restores mechanics | direct/adapted | UI + recovery-coupled / App | **not pure child-local** |
| 62 | `highestVoteCountState` | M | Day voting | vote result/leading count; recovery restores mechanics | direct/adapted | UI + recovery-coupled / App | **not pure child-local** |
| 63 | `slayerClaimantNameState` | M | Day / Slayer | Judge Slayer branch → `ClocktowerSlayerTableScreen`; App only declares/resets/forwards | direct | **UI / plumbing-only App owner** | **selected D6.2b ownership extraction** |
| 64 | `slayerTargetNameState` | M | Day / Slayer | Judge Slayer branch → `ClocktowerSlayerTableScreen`; App only declares/resets/forwards | direct | **UI / plumbing-only App owner** | **selected D6.2b ownership extraction** |
| 65 | `gameOutcome` | V | shared action/result gating | Day/Night/result display and action-enabled decisions | adapted | D / session | shared durable read; keep above feature UI |
| 66 | `onGhostVoteAuthorityChange` | C | Day voting | vote transaction/mechanics write | adapted | D action / session | Day vote durable action |
| 67 | `onRecordEvent` | C | shared Day + Night | semantic history/event recording from multiple branches | adapted | D action / session | cross-cutting durable boundary |
| 68 | `onRecordEpistemicObservation` | C | Night information | durable visible-information/epistemic recording | adapted | D action / session | Night durable boundary |
| 69 | `onPhaseChange` | C | intended phase nav | **no Judge consumer** | none | unused / App | separate dead-parameter cleanup candidate |
| 70 | `onMovePreviousNightStep` | C | Night navigation | used as night `onPrevious` path | direct/adapted | UI/D checkpoint action / App | Night navigation seam; recovery-sensitive |
| 71 | `onSelectNightDeath` | C | Night / demon attack | demon attack selection | adapted | UI action / App | Night attack seam |
| 72 | `onConfirmDemonAttack` | C | Night / demon attack | confirmation crosses resolution/session/event semantics | adapted | D action / App→session | keep durable action above Night UI |
| 73 | `onSelectExecution` | C | Day | execution selection/confirmation path | adapted | D action / App→session | Day mechanics seam |
| 74 | `onSelectPoisonTarget` | C | Night / Poisoner | selection-only action | adapted | UI action / App | Night Poisoner seam |
| 75 | `onConfirmPoisonTarget` | C | Night / Poisoner | commit poison state | adapted | D action / App→session | Night Poisoner durable boundary |
| 76 | `onSelectFortuneTellerFirst` | C | Night / FT | selection-only action | adapted | UI action / App | Night FT seam |
| 77 | `onSelectFortuneTellerSecond` | C | Night / FT | selection-only action | adapted | UI action / App | Night FT seam |
| 78 | `onSelectChambermaidFirst` | C | Night / Chambermaid | selection-only action | adapted | UI action / App | Night Chambermaid seam |
| 79 | `onSelectChambermaidSecond` | C | Night / Chambermaid | selection-only action | adapted | UI action / App | Night Chambermaid seam |
| 80 | `onSelectRavenkeeperTarget` | C | Night / Ravenkeeper | selection-only action | adapted | UI action / App | Night Ravenkeeper seam |
| 81 | `onSelectRedHerring` | C | Setup / First Night | setup selection/assignment | adapted | D action / App→session | Setup/FT rule seam |
| 82 | `onApplyRecommendation` | C | Setup / First Night | applies recommendation into game/setup state | adapted | D action / App→session | recommendation durable boundary |
| 83 | `onSelectButlerMaster` | C | Night / Butler | Butler master selection | adapted | D action / App→session | Night Butler seam |
| 84 | `onSelectMonkProtectedTarget` | C | Night / Monk | selection-only draft | adapted | UI action / App | Night Monk seam |
| 85 | `onConfirmMonkProtectedTarget` | C | Night / Monk | commits protection | adapted | D action / App→session | Night Monk durable boundary |
| 86 | `onSelectMayorRedirectTarget` | C | Night / Mayor | selection-only draft | adapted | UI action / App | Night Mayor seam |
| 87 | `onConfirmMayorRedirectTarget` | C | Night / Mayor | commits redirect decision | adapted | D action / App→session | Night Mayor durable boundary |
| 88 | `onSelectDemonSuccessor` | C | Night / succession | successor selection | adapted | UI/D transition / App | Night succession seam |
| 89 | `onConfirmDemonSuccessorTarget` | C | Night / succession | commits successor target | adapted | D action / App→session | Night succession durable boundary |
| 90 | `onConfirmNewDemon` | C | Night / succession | commits/reveals new demon transition | adapted | D action / App→session | Night succession durable boundary |
| 91 | `onSelectKlutzChoice` | C | Day / Klutz | Klutz selection | adapted | UI action / App | Day Klutz seam |
| 92 | `onConfirmKlutzChoice` | C | Day / Klutz | commits Klutz resolution | adapted | D action / App→session | Day Klutz durable boundary |
| 93 | `onSelectArtistClaimant` | C | Day / Artist | selection-only; App mutates artist claimant state | direct/adapted | UI action / App | Artist extraction candidate |
| 94 | `onSelectArtistTruthfulAnswer` | C | Day / Artist | selection-only; App mutates answer state | direct/adapted | UI action / App | Artist extraction candidate |
| 95 | `onSelectArtistShownAnswer` | C | Day / Artist | selection-only; App mutates shown answer | direct/adapted | UI action / App | Artist extraction candidate |
| 96 | `onConfirmArtistQuestion` | C | Day / Artist | App reads the three selected Artist values, records durable effects/event, resets and advances | adapted | D action / App→session | value-carrying redesign needed before ownership move |
| 97 | `onSlayerShot` | C | Day / Slayer | Slayer branch sends claimant + target + Recluse registration decision | direct/adapted | **D action / App→session** | **already value-complete; enables safe UI-state ownership move** |
| 98 | `onPreflightVirginExecution` | C | Day / nomination/Virgin | genuinely used in Virgin nomination preflight paths | adapted | D/domain decision / App | Day nomination durable boundary |
| 99 | `onVirginNomination` | C | Day / nomination/Virgin | commits Virgin nomination semantics | adapted | D action / App→session | Day nomination seam |
| 100 | `onAdvanceFromFirstNight` | C | First Night | phase/flow transition after first night | local/adapted | D flow action / App | FirstNight seam |
| 101 | `onConfirmDay` | C | Day | end/confirm Day transition | local/adapted | D flow action / App | Day flow boundary |
| 102 | `onConfirmNight` | C | Night | end/confirm Night transition | local/adapted | D flow action / App | Night flow boundary |
| 103 | `onShowResults` | C | terminal/result | result-screen navigation/action | local | UI/flow action / App | terminal seam; unrelated to ability extraction |

## MutableState ownership decision

The 10 `MutableState<T>` parameters are not one homogeneous group.

| State | Natural concern | External coupling found | D6.2a decision |
|---|---|---|---|
| `nightStartedState` | Night navigation cursor | checkpoint creation, recovery restore, reset | keep above child until a recovery-safe Night contract exists |
| `nightStepIndexState` | Night navigation cursor | checkpoint creation, recovery restore, reset | keep above child until a recovery-safe Night contract exists |
| `dayModeState` | Day dispatcher | Recovery and Klutz flow can write it | keep at Day dispatcher/root boundary initially |
| `nominatorNameState` | nomination transient UI | Day-flow reset/plumbing | later nomination slice candidate |
| `nomineeNameState` | nomination transient UI | Day-flow reset/plumbing | later nomination slice candidate |
| `currentVoteCountState` | vote transient UI | Day-flow reset/plumbing | later vote slice candidate |
| `highestVoteNameState` | vote mechanics/presentation | recovery restores vote mechanics | not pure child-local state |
| `highestVoteCountState` | vote mechanics/presentation | recovery restores vote mechanics | not pure child-local state |
| `slayerClaimantNameState` | Slayer selection UI | declaration/reset/forwarding only | **move down in D6.2b** |
| `slayerTargetNameState` | Slayer selection UI | declaration/reset/forwarding only | **move down in D6.2b** |

Important: extracting all 10 into one `DayState`/`NightState` bag would hide, rather than solve, different ownership semantics.

## Existing child seams / forwarding map

Real child boundaries already present and worth preserving:

```text
ClocktowerJudgeScreen
├─ ClocktowerDawnSummaryScreen
├─ ClocktowerDayOverviewScreen
├─ ClocktowerPendingNominationTableScreen
├─ ClocktowerVoteTableScreen
├─ ClocktowerSlayerTableScreen
├─ ClocktowerArtistTableScreen
├─ ClocktowerKlutzTableScreen
└─ ClocktowerNightActiveScreen
   └─ ClocktowerNightStepCardLocalized
```

Night callbacks are often adapted according to `ClocktowerNightAction` before reaching the step UI. This is evidence against creating one broad `ClocktowerJudgeActions` forwarding object merely to make the parent signature shorter.

## Focused test baseline for the selected candidate

A focused test already exists:

`app/src/test/java/com/codex/campboardgamehost/ClocktowerSlayerTableStateTest.kt`

It characterizes:

- claimant step exposes only unused eligible claimant seats;
- target step preserves claimant as first selection and allows only other living players;
- chosen target is the second ordered selection while claimant remains stable;
- a dead target is rejected rather than silently accepted.

Related broader safety coverage includes the existing host decomposition/selection characterization tests and Day overview/table tests.

D6.2a therefore does **not** justify adding a generic snapshot RED. Before production D6.2b, add a new typed RED only if the exact ownership move exposes a stable behavior/contract gap not already protected by the Slayer table-state test + compile/focused suite.

## Boundary ranking

### 1. Selected: Slayer active interaction / selection ownership

Why first:

- only two tightly cohesive transient selection states;
- App ownership is plumbing-only: declare, reset, forward;
- no recovery/checkpoint restore path was found for either state;
- existing `ClocktowerSlayerTableScreen` is a real child seam;
- `onSlayerShot(claimantName, targetName, recluseRegistersAsDemon)` already carries the complete selected values to the durable owner;
- no session/domain semantics need to move;
- removes real fan-out from both App and Judge rather than hiding it in a bag.

Expected narrow D6.2b scope:

```text
move Slayer claimant/target transient selection ownership down near Slayer UI
remove App-root Slayer MutableState declarations/reset plumbing
remove the two MutableState parameters from ClocktowerJudgeScreen
preserve slayerUsed/slayerClaimedNames/gameOutcome above
preserve onSlayerShot as the durable value-complete boundary
keep dayMode routing above for this first slice
preserve visible behavior exactly
```

Do not introduce `SlayerState`, `SlayerActions`, a controller, or a ViewModel merely for symmetry. A tiny typed UI model is acceptable only if implementation proves it removes real coupling instead of relocating it.

### 2. Artist active selection

Also cohesive, but `onConfirmArtistQuestion` currently reads claimant/truthful/shown selection values from App-owned state. Moving ownership down cleanly would first require a value-carrying confirmation boundary. Higher behavior risk than Slayer.

### 3. Nomination / vote subsets

Real cohesive Day concerns, but `dayModeState` and highest-vote state have recovery/mechanics coupling. Extract in smaller verified pieces, not as one Day mega-state.

### 4. Whole Day dispatcher

Potential later structural boundary after ability and vote ownership is cleaner. Too broad for the first slice.

### 5. Night navigation ownership

High fan-out reduction potential, but `nightStartedState` and `nightStepIndexState` participate in checkpoint/recovery. Defer until a recovery-safe navigation contract is designed and characterized.

## Separate cleanup candidates

`records` and `onPhaseChange` have no Judge consumer. They should eventually be removed from the call surface, but keeping them separate from the first Slayer ownership extraction gives a cleaner exact-diff/behavior audit.

## D6.2a stop point

D6.2a is complete when this matrix and the parent audit are committed and the branch diff is verified docs-only.

**No production implementation is authorized by this document itself.** The next production slice is D6.2b Slayer active selection ownership, tests/characterization-first and behavior-preserving.
