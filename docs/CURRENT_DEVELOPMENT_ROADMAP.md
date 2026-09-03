# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-03 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
live main at roadmap refresh:
d71ccb45b81c8cd0f7741abe4707f361c8312898

active campaign:
UI Information / Storyteller Workspace Campaign

latest validated executable checkpoint:
UI-R4D-2F / F4 — source-agnostic resolved pair Player Reveal transition
branch: codex/ui-r4d2-seating-first-setup
F4 RED anchor: 4c8b4e6a21d4da3a3b440c73f973b8a716e3bc08
F4 resolved-display contract checkpoint: 959258033c572864afaa182941d575a3ab9cf168
F4 product checkpoint: 83374634f9246eb5556a26a2f6020ae9251d0c3e
F4 executable cleanup head: c5a6c3a2687e6cf2ba52c19d355ecd36a7da8984
later branch heads after that checkpoint may be docs-only; always distinguish them from executable F4 state
PR #79: draft / open / mergeable / unmerged

validated F1 immediately below it:
UI-R4D-2F / F1 — constraint/capacity-aware HostTableLayout
validated executable checkpoint: f49e9f6a4be5109cd16fe724e24071179310004c
cleanup head: 37ea5e9b3b1283c6f1f5fc71e35603ff9e88aaad

validated foundation below R4D-2:
UI-R4D-1 — Persistent Host Table Foundation
branch: codex/ui-r4d-persistent-table-foundation
cleanup head: 524f55bac945f1be8ee9d9ec77e4e4ca6935781e
PR #78: draft / open / mergeable / unmerged

active development target:
UI-R4D-2F / F5 — dedicated typed pair Player Reveal / readable seat-number hierarchy

blocked until R4D-2F is clean:
UI-R4D-3 — Day Storyteller Workspace

stabilization after UI-R4D:
UI-R5 — Real-Device Stabilization / Feature Freeze

algorithm campaign after UI stabilization:
EPI-MQ / Productive Uncertainty / PlayerWorldSet
```

The UI campaign remains intentionally ahead of EPI-MQ. EPI-MQ is paused, not cancelled.

UI-R1 through UI-R4D-2 are stacked draft work and are **not yet on main**. Do not create the next UI branch from `main` or the stack will be lost. Do not start R4D-3 until the R4D-2F field-test correction gate below is closed.

## 2. Campaign status

```text
UX-R1   dependency / legal-authority audit                     COMPLETE
UX-R2A  shared pair legal-domain authority                    COMPLETE / MERGED
UX-R2B  pair Manual -> legal-domain authority                 COMPLETE / VERIFIED / MERGED
UX-R3   remove global storyteller mode selector               COMPLETE / VERIFIED / MERGED
UX-R4   Top-1 + 0–2 alternatives + persistent Manual         COMPLETE / VERIFIED / MERGED
UX-R5   small-domain specialization                           COMPLETE / VERIFIED / MERGED

UI-R1   reusable square-table seat surface                    COMPLETE / VERIFIED / DRAFT #70
UI-R2   pair Manual dedicated full-screen selection           COMPLETE / VERIFIED / DRAFT #71
UI-R3   player information presentation                       COMPLETE / VERIFIED / DRAFT #72
UI-R4   Fortune Teller two-target + result flow               COMPLETE / VERIFIED / DRAFT #73
HOTFIX  Monk/Ravenkeeper target legality                      COMPLETE / VERIFIED / DRAFT #75
UI-R4B  night-action square-table unification                 COMPLETE / VERIFIED / DRAFT #76
UI-R4C  real-device UI corrections                            COMPLETE / VERIFIED / DRAFT #77
UI-R4D-1 persistent Host table foundation                      COMPLETE / VERIFIED / DRAFT #78
UI-R4D-2 seating-first session flow / seat authority           STRUCTURALLY VERIFIED / DRAFT #79
UI-R4D-2F F1 responsive Host-table layout                      COMPLETE / VERIFIED
UI-R4D-2F F2 shared-slot drag-to-reorder                       COMPLETE / VERIFIED
UI-R4D-2F F3 seating return / Android Back                       COMPLETE / VERIFIED
UI-R4D-2F F4 Manual pair resolved-display transition                COMPLETE / VERIFIED
UI-R4D-2F F5 typed pair Player Reveal hierarchy                     ACTIVE NEXT
UI-R4D-2F F6-F7 remaining field-test closeout                       QUEUED
UI-R4D-3 day Storyteller workspace                             QUEUED AFTER R4D-2F
UI-R4D-4 public claim history                                  QUEUED
UI-R4D-5 nomination / vote state machine                       QUEUED
UI-R4D-6 unified Host seat presentation migration              QUEUED
UI-R5   real-device stabilization / feature freeze             QUEUED AFTER UI-R4D

EPI-MQ / ALG
        Productive Uncertainty / PlayerWorldSet mainline      PAUSED UNTIL UI CAMPAIGN STABLE

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Do not redo Monk/Ravenkeeper legality, UI-R4B, completed R4C corrections, F1 layout, F2 drag ordering, F3 seating navigation, or F4 resolved-display authority.

## 3. Active authorities

Current next-development handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_PERSISTENT_HOST_TABLE.md`

Primary information-display reference:

`docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

Completed R4C handoff, now historical implementation context:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_R4C_FIELD_TEST_CORRECTIONS.md`

Existing semantic/UX authority remains:

`docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md`

The UI campaign may change presentation, navigation and interaction state, but must not take ownership of legal-domain or recommendation semantics.

## 4. Frozen permanent architecture

```text
Composition
-> committed actual identity
-> committed shown identity
-> perceived ability
-> complete healthy legal/truth semantic domain
-> interaction-scoped registration
-> RELIABLE / POISONED / DRUNK reliability state
-> recommendation/manual selection
-> AbilityObservation
-> durable player-visible history
-> UI
```

Permanent invariants:

- Drunk actual identity remains Drunk;
- shown identity is committed once and is not recommendation state;
- Healthy, Poisoned and Drunk of the same perceived role share role semantics before reliability;
- Spy/Recluse registration belongs to semantic truth construction, not recommendation heuristics;
- semantic legality/truth must not be owned by Host/UI compatibility projection;
- every supported information role must remain playable through a correct Manual/generated clue path even when recommendation support is absent;
- recommendation ranking remains downstream of the complete legal semantic domain;
- Manual is a permanent user authority path, not a recommendation style;
- exact typed outcome identity survives presentation and confirmation;
- stable seat identity must not depend on filtered-list position;
- draft selection and confirmed observation/history remain separate;
- callback / confirmation ordering must not change accidentally;
- `EvilInfo` and other non-role information must remain safe when `roleEnName == null`;
- A3 exact enumeration remains the algorithm correctness baseline;
- A4/ZDD remains shadow/prototype until separately validated.

UI-R4D adds another permanent presentation invariant:

```text
stable seatId -> stable physical table position for the whole session
```

A player must not move to a different on-screen table position merely because the app changes from setup to day, voting or night action.

## 5. Stable recommendation / Manual contract

```text
Complete legal semantic candidate domain
        |
        +--> Manual / direct legal selection
        |
        +--> Recommendation Provider
                 -> presentation
```

Permanent conditions:

```text
recommendation unavailable != manual unavailable
```

Normal execution remains Storyteller-confirmed / ASSISTED.

For pair/combinatorial domains:

```text
Top-1 recommendation
+ 0–2 alternatives
+ persistent Manual over complete legal domain
```

For naturally small domains:

```text
primary recommendation
+ all remaining legal outcomes when the full domain comfortably fits
```

UI-R4D must not change these semantics.

## 6. Completed UI-R4C checkpoint

UI-R4C corrected the real-device issues that were safe to close before the larger table redesign:

- pair Manual role labels now use the current app language without changing typed role identity;
- player-facing final information is information-first full-screen rather than table-first;
- registration-sensitive Chef / Empath / Fortune Teller / Undertaker / Ravenkeeper flows use complete final-information result domains while preserving registration witness/provenance;
- focused UI-R4C tests passed;
- `:app:testFast` passed;
- `git diff --check` passed;
- exact product diff audit passed;
- temporary one-shot workflow/script self-removed;
- validated product commit is `cb62c4d48c822db10f2b0b18b4f8e19336c7abb1`;
- Draft PR #77 is stacked on UI-R4B and remains unmerged.

The original standalone R4C-2 proposal — adding richer role context only to the night-action square-table seat card — is **superseded** by UI-R4D. Do not implement a separate night-only R4C-2 patch.

## 7. Active target — UI-R4D Persistent Host Table / Storyteller Workspace

### Product principle

The core interaction principle is:

> **Players always remain seated around the same square table; the Storyteller changes the current task in the center of that table.**

Equivalent UI rule:

```text
table edge = WHO / stable game state
center     = WHAT THE STORYTELLER IS DOING NOW
```

The square table is no longer a night-action widget. It becomes the stable Storyteller workspace from initial seating through day, nomination, voting and night action.

### Explicit privacy exception

There is one deliberate full-screen exception:

```text
Storyteller workspace -> persistent square table
Player-facing reveal  -> sanitized information-only full screen, NO table
```

The Player Reveal screen is the safe phone-handoff boundary. Actual identity, shown/perceived identity, public claims, hidden state, registration provenance, recommendation metadata and voting/host state must never leak into Player Reveal.

## 8. UI-R4D interaction modes

The shared table should support a bounded set of interaction modes instead of separate screens inventing separate player selectors.

### R4D Mode A — Seating

Seat content:

```text
seat number
player name
```

Center:

- common/recent player choices;
- add-new-player input/action;
- seat editing/reordering where supported;
- primary CTA: `确定座位` / `Confirm seats`.

The preferred true setup flow becomes:

```text
Arrange players and seats
-> Confirm seats
-> Choose game
-> Game-specific settings
-> Start game
```

Do not merely hide a game that was already selected. Seating becomes a game-independent session foundation.

### R4D Mode B — Day Overview

The app is Storyteller-private except during explicit Player Reveal, so the normal day table may display actual identities.

Seat presentation can include:

```text
#3 Alice ☠
Washerwoman
Claim: Fortune Teller
```

Where actual and shown/perceived identity differ, keep them explicit rather than collapsing them. For the Drunk, for example:

```text
#5 David
Actual: Drunk
Perceived: Empath
Claim: Empath
```

The following are separate semantic concepts and must remain separate in data/model/presentation:

```text
Actual Role
Perceived / Shown Role
Public Claim
```

The day center should become operational rather than informational. Remove redundant large player-status lists and unnecessary explanatory chrome.

Trouble Brewing day actions should keep at least:

```text
Slayer action
Start nomination
Host tools
```

The Slayer action must remain available throughout the day because the button represents a public Slayer claim/action event, not proof that the acting player is actually a live unused Slayer.

### R4D Mode C — Nomination

Use the same stable table positions to choose:

```text
nominator
-> nominee
```

Do not replace the table with a separate unordered player list.

Core nomination legality should continue to be enforced by the appropriate rules/state authority, not by visual position.

### R4D Mode D — Vote

Voting should be represented as a sequential state machine, not as a free-edit checkbox grid.

The square table remains fixed while vote state changes:

```text
upcoming -> current -> counted/locked
```

The vote cursor should proceed clockwise and end with the nominee according to the app's Blood on the Clocktower voting rules authority.

Center summary should expose core operational state such as:

- nominator -> nominee;
- alive count;
- majority threshold;
- current vote count;
- current high score / on-the-block state;
- current voter;
- next/skip action;
- narrowly scoped undo-last-input correction.

Once the cursor has moved past a player's committed vote, ordinary editing of that prior vote should be locked. Any correction path must represent Storyteller input correction, not retroactive player vote changes.

The first implementation should automate core voting mechanics only:

- clockwise order;
- alive/dead status;
- dead player's remaining ghost vote;
- majority threshold;
- current vote total;
- tie / current on-the-block logic;
- durable nomination/vote history.

Do not turn UI-R4D into a complete special-character vote-modifier engine. Leave a clean extension seam for later role-specific validation/assistance.

### R4D Mode E — Night Action

Reuse the same stable table positions for existing night actions.

- legal targets highlighted;
- illegal targets disabled/non-actionable;
- one- or two-target selection preserves typed seat identity;
- center contains the action-specific controls/results;
- Storyteller seat presentation may include actual and shown/perceived roles where useful.

This mode absorbs the old standalone R4C-2 seat-detail idea into one unified Host Seat Presentation policy shared across the whole app.

### R4D Mode F — Player Reveal

This is intentionally **not** a table mode.

Player Reveal remains the R4C information-first full-screen surface and must stay sanitized.

## 9. Shared seat presentation architecture

Do not let Setup, Day, Vote and Night each invent their own seat-card identity logic.

Introduce a cohesive typed presentation seam conceptually equivalent to:

```text
HostTableSeatPresentation
```

It should consume stable semantic/session state such as:

- typed stable `seatId`;
- seat number/order;
- player name;
- actual role;
- shown/perceived role where different;
- alive/dead state;
- dead-vote availability where relevant;
- latest public claim where relevant;
- interaction flags such as selectable / selected / current / locked.

Presentation policy decides which fields are visible in each table mode. Do not infer identity from localized text in Compose.

Long names/roles must use bounded, adaptive typography/layout and must not silently become ambiguous.

## 10. Public role claim history

Public role claims are valuable Storyteller memory and future algorithm input.

Do **not** model this only as a mutable scalar such as:

```text
player.claimedRole = Soldier
```

Claims can change and the change itself is strategically meaningful.

Use a durable event/history model conceptually like:

```text
PublicRoleClaimEvent(
    seatId,
    claimedRoleId,
    dayIndex,
    sequence/time,
    ...
)
```

The table may project only the latest/current claim:

```text
Claim: Soldier
```

but history must retain the sequence of claims.

Initial product UI can support a single explicit role claim at a time while keeping the model extensible to later changed/retracted/multiple/ambiguous claims.

Recording claims must not yet alter recommendation ranking during UI-R4D. EPI-MQ may consume this richer public-state history later behind a separate algorithm task.

## 11. Recommended UI-R4D implementation slices

Do not attempt the entire workspace redesign in one large PR.

Recommended order:

```text
R4D-1  Persistent Table Foundation
       - stable seatId -> spatial slot
       - shared Host table shell
       - shared typed seat-presentation model
       - bounded interaction-mode state
       - center content/action slot

R4D-2  Seating-First Session Flow
       - square table starts during player arrangement
       - common/recent players in center
       - add/remove/reorder seats
       - Confirm seats
       - game selection moves after seating confirmation

R4D-2F Real-Device Field-Test Corrections / Closeout Gate
       - replace equal-four-edge seat allocation with constraint/capacity-aware layout
       - restore drag-to-reorder using the same computed spatial slots as rendering
       - make return-to-edit-seating obvious and support Android system Back
       - fix Manual pair clue confirmation -> Player Reveal transition
       - create readable pair Player Reveal with large typed seat numbers
       - correct low-contrast typography/state colors
       - revalidate on device before R4D-3

R4D-3  Day Storyteller Workspace
       - migrate day overview to shared table
       - show actual identity / relevant perceived identity
       - keep Slayer action always available during day
       - nomination entry point
       - Host tools
       - remove redundant day information chrome/lists

R4D-4  Public Claim History
       - durable PublicRoleClaimEvent model
       - seat quick action to record/update claim
       - latest-claim projection on table
       - persistence/history coverage
       - no recommendation behavior change yet

R4D-5  Nomination / Vote State Machine
       - table-based nominator/nominee selection
       - sequential clockwise vote cursor
       - counted/locked states
       - narrowly scoped undo-last correction
       - dead vote / threshold / tie / on-the-block core logic
       - durable nomination/vote history

R4D-6  Unified Host Seat Presentation Migration
       - finish Setup/Day/Night adoption
       - actual vs shown/perceived role policy
       - Drunk presentation
       - adaptive long-name/role layout
       - remove remaining duplicated player-selector/presentation paths
```

### R4D-2 current checkpoint and 2026-09-03 real-device correction gate

R4D-2 has a **validated structural/authority checkpoint**, but it is **not field-test complete**. Keep Draft PR #79 open and do not advance to R4D-3 yet.

Validated structural behavior already established:

- `Confirm seats` freezes the current player order into immutable typed `ClocktowerSeatId` assignments;
- game selection is unavailable before seat confirmation;
- confirmed seating survives entry to and return from game-specific settings;
- Undercover, Werewolf, Trouble Brewing and other Clocktower production starts explicitly consume the frozen confirmed roster rather than mutable setup UI order;
- all unseated common players remain reachable in the scrollable center palette.

#### F1 — constraint/capacity-aware layout — COMPLETE

Validated behavior:

- actual width/height, seat dimensions, safe separation and center clearance determine edge capacity;
- portrait layouts naturally allocate more seats to the longer left/right edges;
- one deterministic clockwise `HostTableLayout.slots` ring is the rendering authority;
- slot `ringIndex` is contiguous and stable;
- insufficient perimeter capacity fails closed;
- F1 validated executable checkpoint `f49e9f6a4be5109cd16fe724e24071179310004c`;
- F1 cleanup head `37ea5e9b3b1283c6f1f5fc71e35603ff9e88aaad`;
- Actions run `33690432944`: focused contracts + `:app:testFast` + diff audit GREEN.

#### F2 — drag-to-reorder on computed slots — COMPLETE

Permanent architecture:

```text
pointer position
-> nearest slot from the SAME HostTableLayout.slots used by rendering
-> ringIndex
-> transient preview order
-> final reorder commit
```

Validated behavior:

- no second edge/corner drag geometry exists;
- long-press drag uses the same computed spatial slots as rendering;
- cross-corner Top -> Right movement resolves deterministically through adjacent ring indices;
- an explicit integrated typed test proves Top-last -> Right-first hit-test + insertion preserves the dragged player and all identities exactly once;
- drag preview animates non-dragged seats toward shared computed slots while the dragged card follows the pointer;
- setup animation uses stable player identity as motion identity while normal Host modes default to stable typed seat identity;
- fallback Earlier/Later controls and drag now share one **final target index** contract;
- corrected the previous forward-move bug where `Later` could remove and reinsert at the same position;
- final confirmation still freezes the resulting physical order into contiguous typed `ClocktowerSeatId`s.

Evidence:

- F2 RED anchor `1ac5bbf6ea8bf1979c85c011f2dedd359017ce1b`;
- RED run `33696408877`: failed only because the new typed reorder/hit-test contract did not yet exist;
- initial production focused GREEN run `33696750805`;
- exact large-file App patch product checkpoint `5857e2324fc8bf1fd6526adc95710751735dd3b6`;
- final large-file one-shot run `33697007750`: exact head/blob/anchor/allowlist + focused F1/F2 tests + `:app:testFast` + `git diff --check` GREEN, temporary writer self-removed;
- integrated cross-corner contract commit `bfb91ff9746ac0da2f4d182c5c319270da62ebdd`;
- final focused cross-corner run `33697631594`: GREEN;
- F2 executable cleanup head `0e3b23dd96c164affd8540e1926ffdcada0fc3c6`; later commits after this point are documentation-only unless separately validated.

Permanent F2 product/test diff relative to F1 touches only:

- `CampBoardGameHostApp.kt`;
- `ClocktowerHostTableReorder.kt`;
- `ClocktowerHostTableUi.kt`;
- `ClocktowerSquareTableUi.kt`;
- `SeatingFirstSetupUi.kt`;
- `ClocktowerHostTableReorderTest.kt`.

No temporary workflow/script is part of the permanent F2 diff.

#### F3 — seating return / Android Back — COMPLETE

Permanent architecture:

```text
Game Selection visible Edit seats
            \
             -> hostSeatingBackTransition(GameSelection)
             -> reopenSeating()
             -> Screen.Setup
            /
Android system Back

Game-specific Settings visible/system Back
-> hostSeatingBackTransition(GameSettings)
-> returnToGameSelection()
-> Screen.GameSelection
-> confirmed seating preserved
```

Validated behavior:

- Game Selection visible `Edit seats` and Android system Back consume the same typed transition;
- both explicitly release the old confirmation and return to editable seating;
- Undercover, Werewolf and Clocktower settings visible/system Back consume the same typed settings transition;
- settings Back preserves confirmed seating while clearing only the selected game and returning to Game Selection;
- no second direct App-level `reopenSeating()` / `returnToGameSelection()` navigation path remains for these surfaces.

Evidence:

- F3 RED anchor `8cf3eb5babd2d6075b872004c6dd2b8dc060f788`;
- RED run `33698824258`: failed only because the new typed back-navigation contract did not yet exist;
- typed contract checkpoint `7792a799874f8a6fdec1e89f8b08d7f4fc7c8c19`;
- typed focused GREEN run `33698991966`;
- exact large-file App product checkpoint `bbf75d6eee1a1759e8795b4418ea39c08f551cfc`;
- final one-shot run `33699228036`: exact head/blob/anchor audit + `HostSeatingRosterTest` + `:app:testFast` + `git diff --check` GREEN;
- F3 executable cleanup head `5c0027eb180ee5c95ca52ee8cca03d7204258c61`;
- all temporary F3 workflows/scripts self-removed.

Permanent F3 product/test diff relative to the F2/docs baseline touches only:

- `CampBoardGameHostApp.kt`;
- `HostSeatingBackNavigation.kt`;
- `HostSeatingRosterTest.kt`.

#### F4 — resolved Manual/recommended pair display transition — COMPLETE

Permanent architecture:

```text
Manual pair selection -----\
                            -> resolved ClocktowerDisplayOption
Recommendation selection --/          |
                                       v
                         resolveClocktowerPlayerDisplay(step, option)
                                       |
                                       v
                         SAME sanitized Player Reveal payload
                                       |
                                       v
                         SAME Player Reveal renderer
```

Validated behavior:

- Manual and recommendation paths already converge on the same `showRecommendedDisplayOption(option)` commit path;
- final Player Reveal projection is now owned by source-agnostic `resolveClocktowerPlayerDisplay(step, option)` with no Manual/recommended source parameter;
- the resolved payload copies the exact player-visible display fields, typed proposition and truth-selection value from the chosen option;
- Storyteller candidate lists are cleared before Player Reveal, preserving the phone-handoff privacy boundary;
- `ClocktowerNightStepUi.kt` keeps callback/audit ordering unchanged and delegates only the final projection;
- no Player Reveal visual hierarchy was changed in F4; readable pair seat-number hierarchy remains F5.

Evidence:

- F4 RED anchor `4c8b4e6a21d4da3a3b440c73f973b8a716e3bc08`;
- RED run `33700178383`: failed only on missing `resolveClocktowerPlayerDisplay` references;
- resolved-display contract checkpoint `959258033c572864afaa182941d575a3ab9cf168`;
- focused GREEN run `33700330132`;
- exact large-file wiring product checkpoint `83374634f9246eb5556a26a2f6020ae9251d0c3e`;
- final one-shot run `33700851236`: exact branch/blob/anchor audit + exact `ClocktowerNightStepUi.kt` diff (`+1/-14`) + `ClocktowerPlayerDisplayResolutionTest` + pair Manual/presentation focused contracts + `:app:testFast` + `git diff --check` GREEN;
- F4 executable cleanup head `c5a6c3a2687e6cf2ba52c19d355ecd36a7da8984`;
- all temporary F4 workflows/scripts self-removed.

Permanent F4 product/test diff relative to the F3/docs baseline touches only:

- `ClocktowerNightStepUi.kt`;
- `ClocktowerPlayerDisplayResolution.kt`;
- `ClocktowerPlayerDisplayResolutionTest.kt`.

#### F5 — next active slice

Create the dedicated typed pair Player Reveal hierarchy with readable large seat numbers. Preserve the F4 source-agnostic resolved-display authority; F5 changes presentation, not Manual/recommendation semantics.

#### Remaining F6-F7

- F6 high-contrast seat/state typography corrections;
- F7 real-device closeout for 5 / 8 / 12 / 15 players, cross-corner drag and Manual reveal.

Only after F7 is clean should R4D-3 become active.

## 12. UI-R4D testing strategy

Authority: `docs/TESTING_STRATEGY.md` and root `AGENTS.md`.

Use risk-based tests-first. Do not create ceremonial source-shape tests for every Compose edit.

High-value permanent contracts:

### Stable spatial identity / responsive layout

- same typed `seatId` maps to the same table position across Seating -> Day -> Nomination/Vote -> Night;
- filtered legal-target sets do not renumber/reposition players;
- table slot allocation is deterministic for the same constraints/player count;
- portrait constraints may allocate more seats to left/right than top/bottom according to actual edge capacity;
- layout and drag insertion use the same computed ordered slot ring;
- drag reorder across an edge corner preserves deterministic seat order.

### Seating navigation / Manual reveal

- confirmed seating can be explicitly reopened from Game Selection without losing the editable roster;
- Android Back from Game Selection performs the same reopen-seating transition;
- Back from game-specific Settings returns to Game Selection while preserving confirmation;
- a resolved Manual pair option commits the intended display and opens sanitized Player Reveal exactly once.

### Privacy boundary

Player Reveal must never expose:

- actual role;
- shown/perceived role beyond the intended player-visible information;
- public claim metadata;
- poison/drunk/reliability state;
- registration witness/provenance;
- recommendation reasons/truth flags;
- vote/host-only state.

### Day / Slayer

- Slayer action remains reachable during the whole day regardless of whether a real Slayer exists, is alive, or has already used the ability;
- actual hidden game state determines resolution after the public claim/action is recorded.

### Public claims

- adding a new claim preserves prior claim history;
- latest-claim projection is deterministic;
- save/restore preserves claim sequence.

### Voting

- voter order is correct and stable;
- advancing the cursor locks prior committed input from ordinary editing;
- undo-last is narrowly scoped;
- dead vote can be consumed only according to core rules;
- majority threshold, tie and on-the-block transitions are correct;
- nomination/vote history persists correctly.

At meaningful executable checkpoints:

```text
focused behavior tests
-> :app:testFast
```

Use ordinary CI/R2 / `[full-ci]` according to repository classifier and risk when a slice reaches its validation checkpoint.

Note: PR #79 is intentionally stacked on PR #78 rather than based on `main`, while the repository's ordinary CI/R2 pull-request triggers target `main`. Therefore normal PR CI/R2 does not automatically execute for this stacked PR. F1/F2/F3/F4 executable evidence comes from the dedicated checkpoint workflows above; do not misreport absence of main-target PR CI as a failure.

## 13. Source ownership / growth guards

`ClocktowerHostScreen.kt` remains protected orchestration. Do not make it the implementation home for the persistent-table component, claim history or vote state machine.

`ClocktowerNightStepUi.kt` remains orchestration/wiring, not the owner of table-wide visual policy.

Prefer small cohesive owners for:

- host table shell/layout;
- host drag/reorder interaction contract;
- host seat presentation model/policy;
- seating/session state;
- public claim event/history;
- nomination/vote session state;
- mode-specific center controls.

Do not create a generic `Utils`, `Helpers`, `Manager`, or God context merely to reduce argument count.

For large/protected files, continue using the repository-approved exact-anchor one-shot Python patch workflow when necessary.

## 14. UI-R4D scope guards

Do not expand UI-R4D into:

- EPI-MQ / Productive Uncertainty implementation;
- recommendation ranking/scoring/diversity changes;
- using public claims to change clue recommendations yet;
- new legal-domain ownership inside UI code;
- A4/ZDD production rollout;
- Mayor redirect / Imp succession redesign;
- broad Host/App decomposition unrelated to the table boundary;
- broad unsupported-script expansion;
- complete special-character voting modifier automation;
- theme/animation polish unrelated to usability.

If a correctness bug is discovered, isolate and characterize it rather than hiding it in table refactoring.

## 15. UI-R5 after UI-R4D

UI-R5 remains a feature-freeze / real-device stabilization pass.

It becomes active only after the intended UI-R4D slices reach a coherent executable checkpoint.

R5 should validate the complete Storyteller workflow on device, including:

- seating-first session start;
- game selection after seat confirmation;
- Minion/Demon introduction;
- pair recommendation and Manual;
- registration-sensitive information;
- day overview;
- Slayer public-action flow;
- nomination and vote recording;
- public claim recording;
- night actions;
- long player/role names;
- Drunk actual/shown presentation in Storyteller workspace;
- player-facing reveal privacy/readability and return navigation.

R5 is not the place for another structural redesign.

## 16. EPI-MQ after UI stabilization

After UI-R5 reaches a stable real-device checkpoint, restore EPI-MQ as the active algorithm campaign.

Primary authorities remain:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

The new public-claim history and stable session state may later become inputs to EPI-MQ, but the algorithm must improve recommendations behind the stabilized interaction contract rather than redesigning the UI again.

## 17. Documentation authority / new-conversation resume protocol

Current active set:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_PERSISTENT_HOST_TABLE.md
docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md
docs/CLUE_RECOMMENDATION_AND_MANUAL_SELECTION_UX_DECISION_2026-09-01.md
docs/TESTING_STRATEGY.md
docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md
```

Historical implementation context:

```text
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_R4C_FIELD_TEST_CORRECTIONS.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_UI_INFORMATION_CAMPAIGN.md
```

New conversation must first re-query live `main`, Draft PR #78 and Draft PR #79, plus the live `codex/ui-r4d2-seating-first-setup` head/checks. Older stacked PRs remain historical dependencies, but the immediate execution boundary is PR #79.

Resume from **R4D-2F / F3** after confirming the current live head and distinguishing docs-only head movement from F2 executable cleanup head `0e3b23dd96c164affd8540e1926ffdcada0fc3c6`. Do **not** start R4D-3 until F1-F7 closeout is complete. Do not merge #78 or #79 without explicit user authorization.

## 18. Deferred / queued registry

| Area | Status |
|---|---|
| UI-R1 square-table foundation | COMPLETE / VERIFIED / DRAFT |
| UI-R2 pair Manual redesign | COMPLETE / VERIFIED / DRAFT |
| UI-R3 player information presentation | COMPLETE / VERIFIED / DRAFT; player reveal corrected by R4C |
| UI-R4 Fortune Teller interaction | COMPLETE / VERIFIED / DRAFT |
| Monk/Ravenkeeper legality hotfix | COMPLETE / VERIFIED / DRAFT |
| UI-R4B night action surface | COMPLETE / VERIFIED / DRAFT |
| UI-R4C field-test UI corrections | COMPLETE / VERIFIED / DRAFT #77 |
| old standalone R4C-2 night seat-detail patch | SUPERSEDED BY UI-R4D UNIFIED SEAT PRESENTATION |
| UI-R4D-1 persistent Host table foundation | COMPLETE / VERIFIED / DRAFT #78 |
| UI-R4D-2 seating-first session flow / seat authority | STRUCTURALLY VERIFIED / DRAFT #79 |
| UI-R4D-2F / F1 responsive layout | COMPLETE / VERIFIED |
| UI-R4D-2F / F2 shared-slot drag reorder | COMPLETE / VERIFIED |
| UI-R4D-2F / F3 seating return + Android Back | ACTIVE NEXT |
| UI-R4D-2F / F4-F7 remaining closeout | QUEUED |
| UI-R4D-3 Day Storyteller workspace | QUEUED AFTER R4D-2F |
| UI-R4D-4 public claim history | QUEUED |
| UI-R4D-5 nomination/vote state machine | QUEUED |
| UI-R4D-6 unified Host seat presentation migration | QUEUED |
| UI-R5 real-device stabilization | QUEUED AFTER UI-R4D |
| EPI-MQ / Productive Uncertainty | PAUSED UNTIL UI STABLE |
