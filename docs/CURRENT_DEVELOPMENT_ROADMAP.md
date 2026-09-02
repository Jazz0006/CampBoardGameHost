# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-02 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status authority.**  
> Always re-query live GitHub state before implementation or merge.

## 1. Current development context

```text
live main at roadmap refresh:
967fdadaa3b3999d81e49c123d39ea5f0acd7de8

active campaign:
UI Information / Storyteller Workspace Campaign

latest validated executable checkpoint:
UI-R4C — Field-Test UI Corrections
branch: codex/ui-r4c-field-test-ui-corrections
validated product checkpoint: cb62c4d48c822db10f2b0b18b4f8e19336c7abb1
PR #77: draft / open / mergeable / unmerged

next development target:
UI-R4D — Persistent Host Table / Storyteller Workspace

stabilization after UI-R4D:
UI-R5 — Real-Device Stabilization / Feature Freeze

algorithm campaign after UI stabilization:
EPI-MQ / Productive Uncertainty / PlayerWorldSet
```

Docs-only commits created while refreshing roadmap/handoff may advance the UI-R4C branch beyond `cb62c4d48c822db10f2b0b18b4f8e19336c7abb1`. Always distinguish docs-only head movement from the latest validated executable checkpoint.

The UI campaign remains intentionally ahead of EPI-MQ. EPI-MQ is paused, not cancelled.

UI-R1 through UI-R4C are stacked draft work and are **not yet on main**. Do not create the next UI branch from `main` or the stack will be lost.

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
UI-R4D  persistent Host table / Storyteller workspace         ACTIVE NEXT
UI-R5   real-device stabilization / feature freeze            QUEUED AFTER UI-R4D

EPI-MQ / ALG
        Productive Uncertainty / PlayerWorldSet mainline      PAUSED UNTIL UI CAMPAIGN STABLE

UX-R6   replace legacy ranking behind stable UX contract      QUEUED AFTER EPI-MQ
```

Do not redo Monk/Ravenkeeper legality, UI-R4B, or completed R4C corrections.

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

The exact commit boundaries may be adjusted after code ownership audit, but preserve this dependency direction.

## 12. UI-R4D testing strategy

Authority: `docs/TESTING_STRATEGY.md` and root `AGENTS.md`.

Use risk-based tests-first. Do not create ceremonial source-shape tests for every Compose edit.

High-value permanent contracts:

### Stable spatial identity

- same typed `seatId` maps to the same table position across Seating -> Day -> Nomination/Vote -> Night;
- filtered legal-target sets do not renumber/reposition players.

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

## 13. Source ownership / growth guards

`ClocktowerHostScreen.kt` remains protected orchestration. Do not make it the implementation home for the persistent-table component, claim history or vote state machine.

`ClocktowerNightStepUi.kt` remains orchestration/wiring, not the owner of table-wide visual policy.

Prefer small cohesive owners for:

- host table shell/layout;
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

New conversation must first re-query live `main`, PR #75, PR #76, PR #77 and the UI-R4C branch head/checks. It must distinguish docs-only commits after `cb62c4d48c822db10f2b0b18b4f8e19336c7abb1` from new executable product changes.

The next implementation branch should be based on the **live UI-R4C stack head**, not on `main`, unless the stack has since been explicitly merged/rebased by the user.

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
| UI-R4D persistent Host table / Storyteller workspace | ACTIVE NEXT |
| UI-R5 real-device stabilization | QUEUED AFTER UI-R4D |
| EPI-MQ Productive Uncertainty | PAUSED UNTIL UI-R5 STABLE |
| ALG cognitive-consistency / PlayerWorldSet | PAUSED UNTIL UI-R5 STABLE |
| UX-R6 legacy ranking replacement | QUEUED AFTER EPI-MQ |
| A4/ZDD production rollout | SHADOW / FUTURE AFTER EXACT BASELINE GATES |
| REC-R1 | QUEUED SEPARATE CAMPAIGN |
| GCR-4 Chambermaid wake-history authority | DEFERRED FOLLOW-UP |
