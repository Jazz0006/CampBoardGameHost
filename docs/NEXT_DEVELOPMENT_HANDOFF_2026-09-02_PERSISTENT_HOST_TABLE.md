# Next Development Handoff — UI-R4D Persistent Host Table / Storyteller Workspace

> Date: 2026-09-02 Australia/Sydney  
> Status: **ACTIVE NEXT DEVELOPMENT TARGET**  
> Repository: `Jazz0006/CampBoardGameHost`  
> Do not merge stacked PRs without explicit user authorization.

## 1. Current verified checkpoint

The current UI stack is:

```text
main
-> UI-R1 reusable square-table surface
-> UI-R2 pair Manual square-table selection
-> UI-R3 player information presentation
-> UI-R4 Fortune Teller square-table adjudication
-> Monk/Ravenkeeper target-legality hotfix
-> UI-R4B night-action square-table unification
-> UI-R4C real-device UI corrections
```

Live `main` at handoff creation:

```text
967fdadaa3b3999d81e49c123d39ea5f0acd7de8
```

Latest validated executable UI-R4C checkpoint:

```text
branch: codex/ui-r4c-field-test-ui-corrections
product checkpoint: cb62c4d48c822db10f2b0b18b4f8e19336c7abb1
PR #77: draft / open / mergeable / unmerged
base: codex/ui-r4b-night-action-square-table
```

Validation already completed for that product checkpoint:

- focused UI-R4C tests: GREEN;
- `:app:testFast`: GREEN;
- `git diff --check`: GREEN;
- exact product diff audit: GREEN;
- temporary one-shot workflow/script removed.

Docs-only commits created while refreshing roadmap/handoff may advance the UI-R4C branch beyond the product checkpoint above. The next conversation must distinguish documentation head movement from new executable code.

## 2. Why UI-R4D replaces the old standalone R4C-2 idea

Real-device discussion originally identified a narrow requirement: night-action square-table seats should show more Storyteller context such as role identity.

Further design discussion showed that this should **not** be implemented as another night-only seat-card enhancement.

The square table already represents the physical seating order of the real game. Rebuilding player selectors and seat cards separately for setup, day, nomination, voting and night creates unnecessary visual/context switching and duplicated presentation policy.

Therefore the new design elevates the table into the persistent Storyteller workspace for the entire game.

The old standalone R4C-2 seat-detail patch is superseded by this broader UI-R4D design.

## 3. Product principle

The central product principle is:

> **Players always remain seated around the same square table; the Storyteller changes the current task in the center of that table.**

Equivalent interaction model:

```text
table edge = WHO / stable game state
center     = WHAT THE STORYTELLER IS DOING NOW
```

The square table is therefore a stable representation of the physical session, not a one-off night target picker.

### Permanent spatial invariant

```text
stable typed seatId
-> stable physical table slot
-> preserved across setup/day/nomination/vote/night
```

Changing interaction mode must not reorder or renumber players merely because the current legal-target set changes.

## 4. Privacy / device-handoff boundary

The persistent table is Storyteller-private.

There is one deliberate exception:

```text
Storyteller workspace -> persistent square table
Player-facing reveal  -> sanitized information-only full screen
```

This boundary was corrected in UI-R4C and must remain permanent.

The Storyteller can safely see actual identities and host-only state during normal setup/day/night operation because players are not supposed to be looking at the application then.

The Player Reveal screen is the explicit phone-handoff boundary and must not expose:

- actual role unless that exact fact is intentionally being revealed to the receiving player;
- Storyteller-only actual/shown comparison;
- poison/drunk/reliability state;
- public-claim metadata;
- registration witness/provenance;
- recommendation reason/truth flags;
- hidden vote/host state;
- any other non-player-visible context.

Do not reintroduce the square table into Player Reveal as part of UI-R4D.

## 5. Shared table modes

The implementation should converge on one persistent table shell with bounded interaction modes.

### 5.1 Seating Mode

The square table begins during initial player arrangement, before game-specific setup.

Seat edge contains only game-independent session identity:

```text
#3 Alice
```

Center can contain:

- common/recent players;
- add-new-player control;
- seat editing/reordering as supported;
- primary CTA: `确定座位` / `Confirm seats`.

Do not display redundant text such as `已加入` next to a player who is visibly already seated.

Preferred true navigation flow:

```text
Arrange players and seats
-> Confirm seats
-> Choose game
-> Game-specific settings
-> Start game
```

This is a real setup-flow reorder, not merely a visual hide/show change. The current player setup already receives `GameKind`, so implementation must audit navigation/session ownership rather than cosmetically hiding game state.

Seating should become a game-independent session foundation that can later be reused by other hosted games.

### 5.2 Day Overview Mode

The same seats remain in the same places.

The day table may display Storyteller-only identity context such as:

```text
#3 Alice ☠
洗衣妇
称：占卜师
```

Where actual and perceived/shown identity differ, show them as distinct concepts. Example:

```text
#5 David
实际：酒鬼
认为：共情者
称：共情者
```

The following must remain separate semantic/data fields:

```text
Actual Role
Perceived / Shown Role
Public Claim
```

Do not collapse them into one generic role label.

The day screen should remove redundant center information and become operational.

Remove/reduce:

- redundant center player-status list;
- `主持模式 / 私密操作入口` chrome;
- `自由讨论` section where it adds no real action.

Keep the table large and use the center for Storyteller operations.

For Trouble Brewing, the normal day center should retain at least:

```text
[杀手行动] [发起提名]
[主持工具]
```

### 5.3 Slayer Action Mode

The day `Slayer action` entry must remain available throughout the day.

Do not show/hide it based on whether:

- a real Slayer exists;
- the real Slayer is alive;
- the real Slayer already used the ability.

The button represents a **public claim/action event**, not proof of actual hidden role possession.

Recommended flow:

```text
choose player publicly claiming Slayer
-> choose target
-> resolve from actual hidden game state
```

This supports legitimate Slayer use, poisoned/drunk behavior, already-used ability handling and non-Slayer bluff claims without making the UI reveal hidden truth.

Keep this as a Trouble Brewing-specific explicit action for the first implementation. A generalized public-day-ability framework can be considered later if justified by additional scripts.

### 5.4 Nomination Mode

Use the same table to select:

```text
nominator
-> nominee
```

Do not switch to a separate unordered player list.

The table should visually distinguish the two selections while preserving stable seat positions.

Existing or future nomination legality belongs to the rules/session authority, not to visual table order.

### 5.5 Vote Mode

Voting should be a sequential interaction/state machine rather than a set of freely editable seat toggles.

Conceptual per-seat state:

```text
upcoming
current
counted/locked
```

The table remains fixed while the vote cursor advances clockwise, ending with the nominee according to the app's Blood on the Clocktower vote-order authority.

Center can show:

```text
Alice -> Bob
Alive: 7
Majority: 4
Current high score: 3
Current voter: Cathy

[Vote / confirm current seat]
[No vote / next]
[Undo last input]
```

Exact visual wording may differ, but the interaction semantics must remain sequential.

Once a player has been processed and the cursor advances, ordinary editing of that player's committed vote is locked.

A narrowly scoped `undo last input` path is acceptable to correct Storyteller data-entry mistakes. It must not become arbitrary retroactive vote editing.

First implementation should automate core voting mechanics only:

- clockwise order;
- nominee last;
- current voter progression;
- alive count;
- majority threshold;
- current vote count;
- current high score / on-the-block state;
- ties;
- dead player's remaining ghost vote and consumption;
- durable nomination/vote history.

Do not turn UI-R4D into a complete special-character voting modifier engine. Roles such as Butler can initially remain Storyteller-adjudicated with later warnings/assistance behind a clean extension seam.

### 5.6 Night Action Mode

Reuse the same stable table layout already proven useful by UI-R4B.

Night action behavior remains:

- legal targets highlighted/interactive;
- illegal targets disabled/non-actionable;
- one- or two-target selection preserves typed seat identity;
- action-specific controls/results live in the center.

The major new requirement is that seat identity/presentation should use the same unified Host Seat Presentation policy as Day/Setup instead of another night-specific card definition.

### 5.7 Player Reveal

Player Reveal is intentionally **not** a persistent-table mode.

It remains the UI-R4C information-first full-screen surface.

## 6. Shared Host Seat Presentation

Create one cohesive typed presentation boundary conceptually equivalent to:

```text
HostTableSeatPresentation
```

The exact Kotlin type/name can be chosen after code ownership audit, but the semantic responsibilities should remain explicit.

Potential inputs:

- stable typed `seatId`;
- seat number / physical order;
- player name;
- actual role;
- perceived/shown role when different;
- alive/dead state;
- dead-vote availability when relevant;
- latest public claim when relevant;
- current interaction flags such as selectable/selected/current/locked.

Presentation policy decides what to show in each mode.

Do not infer actual/shown/claim identity from localized labels inside Compose.

Do not parse presentation labels back into semantic ids.

### Long-name / long-role requirement

Seat cards must remain readable across realistic player/role names.

Use bounded/adaptive layout such as:

- adaptive typography;
- deterministic line limits;
- mode-dependent information density;
- explicit abbreviation only where it is deterministic and non-ambiguous.

Do not silently truncate different players into visually indistinguishable labels.

## 7. Public role claim recording

Public claims should become durable session/history data because they are useful both to the Storyteller and to future misinformation/clue reasoning.

Do not model claims only as a mutable scalar:

```text
player.claimedRole = Soldier
```

A player changing claims is itself meaningful history.

Use an event/history concept such as:

```text
PublicRoleClaimEvent(
    seatId,
    claimedRoleId,
    dayIndex,
    sequence/time,
    ...
)
```

Example history:

```text
Day 1: Alice claims Washerwoman
Day 2: Alice claims Soldier
Day 3: Alice claims Mayor
```

The table may show only the latest/current projection:

```text
称：市长
```

but the earlier events remain durable.

Recommended Day Overview seat interaction:

```text
tap seat
-> compact player sheet
   - actual identity
   - current public claim
   - modify/record public claim
   - player status
   - history
```

Public claim should be a seat/player-specific action, not a permanent center button.

Initial UI can support one explicit role claim at a time while keeping the event model extensible to future:

- changed/retracted claims;
- multiple-role / 3-for-3 claims;
- ambiguous claim state;
- notes.

Do not overbuild those extensions in the first slice.

### Algorithm boundary

UI-R4D records and exposes this history only.

It must **not** yet change recommendation ranking or misinformation generation based on claims.

Later EPI-MQ work may use public claims as part of observable/public game state behind a separately validated algorithm change.

## 8. Recommended implementation slices

Do not implement UI-R4D as one giant PR.

### R4D-1 — Persistent Table Foundation

Goal: establish the reusable architecture without moving every screen at once.

Implement:

- stable `seatId -> spatial slot` mapping;
- shared square-table shell;
- shared typed Host seat presentation model/policy;
- bounded interaction-mode state;
- center content/action slot;
- migration seams for existing Setup/Day/Night users.

Do not add large new gameplay features to this slice.

### R4D-2 — Seating-First Session Flow

Implement:

- table visible from initial player arrangement;
- common/recent player palette in center;
- add/remove/reorder players/seats;
- `Confirm seats` primary action;
- game selection after seat confirmation;
- game-specific settings after game choice.

Audit persistence/session ownership so seat identity remains stable after navigation.

### R4D-3 — Day Storyteller Workspace

Implement:

- Day Overview migrated to shared table;
- actual identity visible to Storyteller;
- perceived/shown role shown only where useful/different;
- simplified top/center UI;
- Slayer action always available during day;
- nomination entry point;
- Host tools;
- seat tap opens compact player action/details surface.

### R4D-4 — Public Claim History

Implement:

- durable `PublicRoleClaimEvent`-style model;
- persistence/restore;
- record/update claim interaction;
- latest-claim projection on seat;
- claim history view/access as appropriate.

Do not connect claims to recommendation scoring yet.

### R4D-5 — Nomination / Vote State Machine

Implement:

- table-based nominator/nominee selection;
- sequential vote session model;
- clockwise cursor;
- nominee last;
- current/counted/locked seat state;
- narrowly scoped undo-last correction;
- alive/dead and ghost-vote behavior;
- majority/tie/on-the-block transitions;
- durable nomination/vote history.

Keep role-specific vote modifiers out of the first automatic core unless an existing authoritative engine already provides a safe integration seam.

### R4D-6 — Unified Host Seat Presentation Migration

Finish migration and remove duplication:

- Setup / Day / Night consume the shared seat policy;
- actual vs perceived/shown identity policy is consistent;
- Drunk Storyteller presentation is correct;
- long-name/long-role layout is adaptive;
- old duplicated player-selector/seat-card paths are removed only after replacement is proven.

This slice absorbs the original R4C-2 requirement.

## 9. Architectural direction

The persistent table should be a presentation/session boundary, not a new God object.

Prefer separation resembling:

```text
session seat identity/order
        |
        v
Host seat presentation projection
        |
        v
Persistent table shell
        |
        +--> Seating center content
        +--> Day center content
        +--> Nomination center content
        +--> Vote center content
        +--> Night center content
```

Keep domain/rules authorities upstream.

The table should receive typed legal/selected/current/locked seat ids rather than deciding legal game rules from visual state.

## 10. Source ownership guards

`ClocktowerHostScreen.kt` remains protected orchestration.

Do not make it the home for:

- persistent table layout policy;
- claim history storage;
- nomination/vote state machine;
- long-name seat rendering policy.

`ClocktowerNightStepUi.kt` remains night orchestration/wiring, not the owner of the app-wide table shell.

Prefer small cohesive new/existing owners for:

- Host table shell/layout;
- Host seat presentation;
- seating/session state;
- public claim events/history;
- nomination/vote session state;
- mode-specific center controls.

Do not create generic `Utils`, `Helpers`, `Manager`, or giant context structures merely to reduce parameter count.

For large/protected files, follow `docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md` and root `AGENTS.md`.

## 11. Tests-first / validation strategy

Follow `docs/TESTING_STRATEGY.md` and root `AGENTS.md`.

Use risk-based tests-first rather than ceremonial RED for every visual adjustment.

Permanent tests should protect behavior/semantic boundaries, not Compose source shape or exact pixels.

### 11.1 Stable spatial identity

Protect that:

- the same typed seat stays in the same spatial slot across modes;
- filtered target lists do not renumber/reorder players;
- save/restore does not silently change seating order.

### 11.2 Privacy boundary

Protect that Player Reveal does not consume/expose host-only seat state.

High-risk fields include:

- actual role;
- Storyteller-only shown/perceived comparison;
- public claim history;
- poison/drunk/reliability state;
- registration witness/provenance;
- recommendation metadata;
- hidden vote/host state.

### 11.3 Slayer public action

Protect that:

- day Slayer action is reachable regardless of hidden actual-role composition;
- the public actor/target selection remains separate from actual hidden resolution;
- availability is not leaked by hiding the button.

### 11.4 Public claims

Protect that:

- adding a later claim does not erase prior claims;
- latest-claim projection is deterministic;
- persistence restores the full event sequence.

### 11.5 Voting

Protect that:

- voter order is deterministic and rule-correct;
- current voter advances correctly;
- prior committed voters become locked from ordinary edit;
- undo-last is narrow and deterministic;
- dead vote is consumed correctly;
- majority/tie/on-the-block state transitions are correct;
- persistence/history preserves the vote session/result.

At each meaningful executable checkpoint:

```text
focused behavior tests
-> :app:testFast
```

At stack checkpoints use ordinary CI/R2 and `[full-ci]` when classifier/risk requires broad Android validation.

## 12. Branch / PR strategy

Do not branch from `main` while the UI stack remains unmerged.

Before implementation, re-query:

- live `main`;
- PR #75;
- PR #76;
- PR #77;
- UI-R4C live branch head;
- latest executable product checkpoint vs any docs-only commits.

The first UI-R4D implementation branch should be based on the **live UI-R4C stack head** unless the user has explicitly changed/merged the stack in the meantime.

Recommended implementation strategy is stacked narrow PRs/slices rather than one huge branch.

Possible branch naming:

```text
codex/ui-r4d-1-persistent-table-foundation
codex/ui-r4d-2-seating-first-flow
codex/ui-r4d-3-day-workspace
codex/ui-r4d-4-public-claim-history
codex/ui-r4d-5-vote-state-machine
codex/ui-r4d-6-seat-presentation-migration
```

Exact names may be shortened, but preserve dependency order.

Do not merge automatically.

## 13. Scope guards

UI-R4D must not include:

- EPI-MQ / Productive Uncertainty implementation;
- recommendation ranking/scoring/diversity changes;
- using public claims to change clue generation yet;
- new semantic legality ownership in UI code;
- PlayerWorldSet production recommendation integration;
- A4/ZDD rollout;
- Mayor redirect / Imp succession redesign;
- broad unrelated Host/App decomposition;
- unsupported-script expansion merely for table completeness;
- complete special-character voting automation;
- broad theme/animation polish.

If a correctness bug appears during migration, isolate and characterize it rather than hiding it inside visual refactoring.

## 14. UI-R5 after UI-R4D

UI-R5 remains the feature-freeze / real-device stabilization phase.

It begins only after UI-R4D reaches a coherent executable checkpoint.

R5 should walk through the full phone experience including:

- seating-first setup;
- game selection after seat confirmation;
- game-specific setup;
- Minion/Demon introduction;
- pair recommendation/Manual;
- registration-sensitive information;
- Day Overview;
- Slayer public-action flow;
- nomination/voting;
- public claim recording;
- Night actions;
- long player/role names;
- Drunk actual/shown Storyteller presentation;
- Player Reveal privacy/readability/navigation.

R5 is not another redesign phase.

## 15. EPI-MQ after UI stabilization

After UI-R5 is stable, restore EPI-MQ / Productive Uncertainty / PlayerWorldSet as the active algorithm campaign.

Public claim history may then become valuable future algorithm input, but algorithm work must sit behind the stabilized host interaction contract.

Relevant algorithm authorities remain:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

## 16. New-conversation resume instruction

Read first:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_PERSISTENT_HOST_TABLE.md
docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md
docs/TESTING_STRATEGY.md
docs/LARGE_FILE_GITHUB_ACTIONS_PYTHON_PATCH_WORKFLOW.md
```

Then re-query live GitHub state and begin from **R4D-1 Persistent Table Foundation**.

Do not restart the old standalone R4C-2 night-seat patch. Do not begin EPI-MQ before the UI campaign is stabilized. Do not merge any stacked PR without explicit user authorization.
