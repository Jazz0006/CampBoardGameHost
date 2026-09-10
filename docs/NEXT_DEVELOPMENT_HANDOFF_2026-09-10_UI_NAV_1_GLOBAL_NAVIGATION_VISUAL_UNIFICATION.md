# NEXT DEVELOPMENT HANDOFF — UI-NAV-1 Global Navigation Visual Unification

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — VISUAL UNIFICATION BEFORE EPI-MQ**  
> Base main at campaign start: `9c19484c682044bb469d90fb7522810ca49ecac2`  
> Branch: `codex/ui-nav-1-global-navigation-visual-unification`  
> Audit: `docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`

## 1. Objective

Standardize the application's flow-navigation presentation around a compact bottom action area while preserving existing navigation/gameplay ownership.

Accepted product direction:

```text
NO persistent global top bar.

Content area owns title / instructions / table / optional progress.

Bottom action geometry:
[ Previous ]   [ Host Tools ]   [ Next ]
```

The campaign is intentionally short and must finish before returning to EPI-MQ-0.

### 1.1 Product refinement recorded on 2026-09-10

Clocktower identity delivery will no longer keep the old progress-bar/pass-phone controller as the final design.

Accepted target:

```text
Storyteller square-table identity controller
- current player highlighted
- center: `2 / 15`
- center: `Show identity to seat 2 · XXX`
- explicit `Show identity` action
- bottom: Previous / Host Tools / Next
```

The actual role reveal remains an isolated full-screen player-facing view. The player-facing view must not expose the table, Host Tools, Previous/Next, or other players' hidden information.

Identity delivery intentionally supports backwards navigation under Storyteller control. A single current cursor such as `currentDealIndex` is sufficient for this campaign; do not add a separate furthest-progress state merely to optimize rare identity re-checks.

The last ordinary identity seat should still use `Next`. `Next` leads to the existing/appropriate first-night boundary prompt; the boundary prompt then owns `Start Night`, with `Previous` available where the current flow can safely return to the last identity seat.

The same product language applies at the end of night: the last ordinary night step uses `Next`, then the existing dawn/broadcast/night-complete boundary should expose `Previous` plus the explicit phase-start/continue action when current ownership supports it.

If supporting that boundary requires moving authoritative gameplay phase-commit timing, stop and establish a dedicated flow characterization before changing production semantics.

## 2. Read first

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md` only when a square-table product detail needs confirmation.

Do not load EPI-MQ internals unless needed to confirm that a proposed UI change does not cross an ownership boundary.

## 3. Architecture pre-flight

```text
Architecture pre-flight:
- current owner: CampBoardGameHostApp/root routing owns Screen/showHostTools/Settings state and the current identity-deal cursor; individual screens own their presentation callbacks; ClocktowerGameSession/Planner/Reducer own gameplay/session truth.
- proposed responsibility: compact, reusable navigation presentation plus the explicitly accepted Storyteller-controlled identity-delivery presentation.
- authoritative state owner(s): unchanged — existing App root, screen flow, Planner/Reducer/session owners.
- narrow typed input/output seam: labels + enabled/visible presentation flags + existing onPrevious/onHostTools/onNext callbacks; identity controller additionally receives current player/index and explicit onShowIdentity.
- keep in current owner / extract: keep all flow/state ownership where it is; extract only stateless visual primitives and screen-local presentation composition.
- reason: achieve global visual consistency and reclaim top-screen space without creating a second navigation architecture.
```

## 4. Non-negotiable scope fence

Do not:

- introduce Navigation Compose;
- redesign `Screen` or route ownership;
- introduce a navigation coordinator/controller/callback bag;
- change BackHandler semantics outside the explicitly accepted identity/boundary behavior;
- create gameplay undo semantics;
- change persistence/recovery ordering or state;
- move Planner/Reducer/ClocktowerGameSession authority;
- change EPI-MQ or recommendation behavior;
- reopen D6 decomposition because a file is large.

This campaign standardizes navigation **presentation**, not navigation ownership.

Narrowly authorized behavior:

- Storyteller-controlled identity Previous/Next may move the identity-delivery cursor;
- Previous must never automatically reveal the prior player's role;
- Host Tools is allowed on the Storyteller identity controller, but forbidden on the player-facing identity reveal;
- existing phase-boundary/broadcast prompts may gain Previous when that can be implemented through the current flow owner;
- moving authoritative phase-transition commit timing is not implicitly authorized by visual work and requires a separately characterized flow slice.

## 5. Product contract

### No persistent top chrome

Retire the current persistent Host Tools top bar in active judge/game flows once equivalent safe bottom access exists.

Titles/instructions belong next to the screen content they describe. Square-table screens continue to use their existing title/instruction area.

### Bottom three-slot geometry

Target:

```text
[ Previous ]   [ Host Tools ]   [ Next ]
```

Visual hierarchy:

```text
Previous   = secondary
Host Tools = tertiary / utility
Next       = primary
```

Use stable positions and compact spacing. Keep usable phone touch targets.

The slot role is stable; the right-side label may remain semantically specific (`Next`, `Start Night`, `Start Day`, `Done`, `Confirm`, etc.) where the existing action is not literally ordinary progression.

### Capability remains owned outside the bar

The visual component must not infer flow state. Existing owners decide whether a slot is enabled/visible and which callback it invokes.

### Identity-delivery privacy contract

The Storyteller identity controller may show:

```text
seat number
player name
current highlighted seat
N / total
Show identity action
Previous / Host Tools / Next
```

It must not show hidden role/alignment information on the table.

The player-facing role display may show the selected player's role and role information, but must not expose:

```text
Host Tools
Previous / Next
other players' roles
Storyteller-only square-table state
```

Returning from the player-facing display returns to the same Storyteller-controlled seat. The Storyteller may then show again, move Previous, or move Next.

### Progress is optional

UI-NAV-1 does not require a shared progress model.

For identity delivery, `N / total` is local screen presentation rather than a persistent progress bar or a second authoritative completion tracker.

Night-flow progress remains an optional future/per-screen refinement.

### Phase-boundary interaction

Normal steps keep `Next`. Explicit cross-phase actions belong on the following Storyteller boundary/prompt surface.

Preferred language:

```text
last ordinary step -> Next -> boundary/prompt
boundary/prompt -> Previous | Host Tools | Start/Continue phase
```

Reuse existing prompts/broadcast screens where possible. Do not introduce a new domain phase owner in the UI.

## 6. Current live seams to reuse

- `showHostTools` / `hostToolTab` at App root already own the Host Tools overlay state.
- `HostGameToolsScreen` is already a full-screen overlay with local Roles / Records / History tab selection.
- Clocktower night-step presentation already receives `canGoPrevious`, `onPrevious`, `onNext`, and `showNavigationActions`.
- many UI-R5 square-table dialogs already use shared `ClocktowerSquareTableStepNavigation`.
- current deal/reveal routing already separates Storyteller pass-phone state from player-facing reveal state and keeps the deal index at App root.
- `SettingsScreen` receives Settings values/mutations from App root and therefore can later be reused as Host Tools content without transferring authoritative state.

Do not replace these seams merely to obtain prettier code.

## 7. Execution slices

### UI-NAV-1B — shared bottom visual primitive

Goal: establish the smallest stateless presentation primitive that can render the accepted three-slot language.

Candidate surface:

```text
HostBottomActionBar(...)
NavigationActionButton(...)
```

Allowed inputs should stay presentation-level: label, enabled/visible state, callback, visual role, modifier/icon if needed.

No domain `Screen`, phase, night-step, session, Planner or Reducer type may enter this primitive.

Validation:

- focused compile/static evidence;
- focused Compose/presentation test only if it protects stable semantics and existing test infrastructure supports it;
- exact diff audit proving no flow/domain state moved.

No production transition behavior changes in this slice.

### UI-NAV-1C — Clocktower Storyteller flow first

Migrate:

1. ordinary night-step Previous/Next row;
2. shared square-table `ClocktowerSquareTableStepNavigation` consumers;
3. remaining local square-table navigation variants;
4. Clocktower day flow;
5. replace Clocktower judge's top Host Tools entry with the safe bottom entry;
6. audit the current night-complete/broadcast boundary and add Previous there only if current ownership permits it without hidden rollback semantics.

Preserve all existing enabled rules and callbacks exactly unless a separately characterized boundary-flow change is explicitly opened.

Real-device spot-check after this slice is strongly preferred because square-table vertical-space recovery is a main product reason for the campaign.

### UI-NAV-1D — remaining host/game flows

Migrate only where existing actions map cleanly:

- Werewolf judge;
- generic Game;
- seating / game selection / game settings.

Do not force Results/Review modal actions into Previous/Host Tools/Next if their semantics are different.

### UI-NAV-1E — square-table identity delivery

Replace the old Clocktower pass-phone/progress presentation with a Storyteller-controlled square-table identity controller.

Required presentation/behavior:

```text
current target player highlighted on square table
center `N / total`
center `Show identity to seat N · player name`
explicit Show identity button
bottom Previous / Host Tools / Next
Previous disabled on first seat
Next remains ordinary Next even on last seat
player-facing role display remains full-screen and isolated
return from role display returns to same seat controller
```

Backwards identity navigation is allowed and intentionally supports re-checking a role. It changes only the current delivery cursor.

Do not add a separate completed/furthest identity tracker unless implementation evidence demonstrates a real requirement.

After the last identity seat, `Next` should reach the existing/appropriate first-night boundary/prompt. Add `Previous` there if it can safely return to the last identity seat through the existing owner.

### UI-NAV-1F — Settings composition under Host Tools

Treat separately from the visual primitive.

Preferred implementation if still narrow after code inspection:

- add Settings as a fourth Host Tools tab/internal content destination;
- extract/reuse Settings content instead of duplicating it;
- continue passing all values/mutation callbacks from App root;
- leave legacy `Screen.Settings` route in place until equivalent access/back behavior is characterized;
- only remove the old standalone entry when behavior equivalence is demonstrated.

If this requires broader route/state ownership change, **STOP and defer this slice** rather than expanding UI-NAV-1.

### UI-NAV-1G — validation and closeout

Run the required focused/full validation for changed ownership surfaces, perform exact diff/behavior audit, field-test the main Clocktower flow, then close UI-NAV-1 and restore EPI-MQ-0 as current priority.

## 8. Testing contract

UI/presentation-only changes do not require an artificial domain RED.

Characterize or assert only behavior that matters:

- Previous invokes the same existing callback/destination on already-supported flows;
- Next invokes the same existing callback/destination on ordinary steps;
- special Next enabled/disabled rules remain unchanged;
- Host Tools opens the same root-owned overlay and dismisses back to the same flow;
- identity controller Previous/Next changes only the deal/reveal cursor;
- identity Show reveals only the currently selected player's role;
- player-facing identity reveal has no Host Tools or cross-player navigation;
- last identity Next reaches the existing/appropriate first-night boundary/prompt;
- boundary Previous returns to the last ordinary step where explicitly supported;
- Settings mutations still reach the same App-root state/persistence callbacks;
- active-game persistence/recovery is not modified.

If the end-of-night/identity boundary requires moving the authoritative phase transition itself, establish a meaningful flow characterization before production implementation. Do not disguise it as a UI-only edit.

Avoid brittle tests for exact padding, colors or typography values unless a durable design token abstraction already exists.

Use emulator/real-device inspection for compactness, bottom reachability and reclaimed vertical area.

## 9. Stop conditions

Stop and re-audit before implementation if a slice appears to require:

- a new navigation state owner;
- gameplay undo semantics;
- recovery/persistence changes;
- a second Host Tools state owner;
- a second identity-completion authority with no demonstrated need;
- duplicating Settings state inside Host Tools;
- EPI-MQ/ranking/domain changes;
- moving authoritative phase-transition timing without a dedicated characterization.

## 10. Definition of done

UI-NAV-1 is complete when:

- persistent Host Tools top chrome is removed from migrated active host flows;
- the main Storyteller flow uses one compact bottom navigation visual language;
- square-table screens no longer maintain avoidable competing Previous/Next styles;
- existing callbacks/enabled semantics are preserved except for the explicitly accepted identity/boundary navigation additions;
- identity delivery uses the privacy-safe square-table Storyteller controller;
- backwards identity re-check is supported without automatically revealing another player's role;
- player-facing identity privacy is preserved;
- applicable phase-boundary/broadcast screens provide a safe Storyteller checkpoint without creating a second phase owner;
- Settings-under-Host-Tools is either safely completed as the isolated 1F slice or explicitly deferred with reason;
- progress remains screen-owned/optional;
- required validation passes;
- roadmap/docs are updated and EPI-MQ-0 becomes current again.

## 11. Immediate next action

Start **UI-NAV-1B**.

Before editing production code, re-query branch/main state and inspect the existing shared square-table navigation function plus ordinary night-step navigation row. Implement the smallest stateless three-slot visual primitive first; do not alter callbacks or flow state.

After 1B, UI-NAV-1C should reuse that primitive for the Clocktower flow before the larger identity-controller migration in 1E.