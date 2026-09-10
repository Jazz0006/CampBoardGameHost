# NEXT DEVELOPMENT HANDOFF — UI-NAV-1 Global Navigation Visual Unification

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — UI-NAV-1C COMPLETE / UI-NAV-1D NEXT**  
> Base main at campaign start: `9c19484c682044bb469d90fb7522810ca49ecac2`  
> Branch: `codex/ui-nav-1-global-navigation-visual-unification`  
> Draft PR: `#118 — UI: unify Storyteller navigation presentation`  
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

### UI-NAV-1B — shared bottom visual primitive — COMPLETE

Implemented:

`app/src/main/java/com/codex/campboardgamehost/HostBottomActionBar.kt`

Contract:

```text
Previous slot = secondary / OutlinedButton
Host Tools slot = utility / TextButton
Next slot = primary / Button
three equal-width stable slots
independent enabled / visible flags
48dp minimum slot height
single-line labels with ellipsis protection
no flow/domain knowledge
```

Code checkpoint:

`6ea4f9504e898d67941b3d2e75defe9b54e83c1d`

Validation:

```text
Draft PR #118 opened
CI #2150 / run 34444205127
- Classify changes PASS
- Android FAST unit tests PASS
- full Android step correctly skipped for ordinary micro-commit cadence
- ASP / Real Clingo correctly skipped for UI-only scope
- CI gate PASS

R2 #2013 / run 34444205141 PASS
```

No Compose UI-test framework was introduced merely to test layout mechanics; current repository search found no existing Compose UI-test surface, and the test strategy explicitly permits compile/static/diff evidence for presentation-only work.

### UI-NAV-1C — Clocktower Storyteller flow first — COMPLETE / PASS

Completed in two implementation slices.

1C.1 migrated the ordinary night-step Previous/Next row, shared `ClocktowerSquareTableStepNavigation`, and remaining Clocktower night/square-table navigation consumers to `HostBottomActionBar` while preserving existing callbacks and enabled rules.

1C.2 migrated the remaining private Clocktower host surfaces:

```text
first-night / night-ready Storyteller prompt
Dawn private review
New Demon private confirmation
Day Overview
Nomination
Vote
EndConfirm
Slayer
Artist
Klutz
```

Ownership decisions preserved:

```text
Night-ready / Dawn / New Demon / Day Overview / Klutz:
- Previous slot reserved but disabled because no existing safe back owner exists.

Nomination / Vote:
- Previous reuses existing onCancel.

EndConfirm / Slayer / Artist:
- Previous reuses existing onBack.
```

The persistent `ClocktowerJudge` top Host Tools entry was removed only after equivalent safe bottom access was established. The root diff for that removal deleted only the `Screen.ClocktowerJudge` condition; WerewolfJudge and generic Game remain for 1D.

Privacy fences:

- Dawn public announcement remains full-screen/public and contains no Host Tools bottom chrome.
- player-facing identity display remains isolated and contains no Host Tools or cross-player navigation.

Validation evidence:

```text
1C.2 production checkpoint:
69b34fa3094abd820cdf18c4ff8770f2538699ea

1C.2 one-shot run:
34455026774 — PASS
- exact head / 11 production blob preflight PASS
- exact patch PASS
- exact 11-file production diff PASS
- privacy reverse assertions PASS
- ./gradlew :app:testFast --no-daemon PASS
- remote head recheck PASS
- production commit/push PASS
- one-shot cleanup PASS

cleanup head:
8b015914182c398f7d8a62bcb8ebd388209fec47
```

Regular CI/R2 attempts on that bot-authored cleanup head ended `action_required` with zero jobs. This is workflow-start gating, not a code/test failure. A normal user-authored documentation checkpoint follows and becomes the clean baseline for 1D.

The requested night-complete Previous was **not** invented where current ownership lacked a safe rollback/navigation callback. Moving authoritative phase-transition commit timing remains outside this presentation slice and still requires dedicated characterization if pursued later.

### UI-NAV-1D — remaining host/game flows — NEXT

Migrate only where existing actions map cleanly:

- Werewolf judge;
- generic Game;
- seating / game selection / game settings.

Do not force Results/Review modal actions into Previous/Host Tools/Next if their semantics are different.

Before removing the remaining root top Host Tools condition for `WerewolfJudge` or `Game`, prove equivalent safe bottom access on every host-facing surface that depended on it.

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

Start **UI-NAV-1D** from the latest normal user-authored documentation checkpoint.

Before editing, re-query live `main`, branch head, PR #118 and checks. Then audit the remaining top-chrome-dependent host surfaces:

- Werewolf judge;
- generic `Game`;
- seating / game selection / game settings screens whose existing actions map cleanly to the three-slot language.

For each surface, identify the current Previous/Back owner, current primary action owner, whether Host Tools is safe, and whether any player-facing/private surface must remain chrome-free. Remove `Screen.WerewolfJudge` or `Screen.Game` from the root top-bar condition only after equivalent bottom access is complete across that route. Do not mix identity-controller work into 1D; UI-NAV-1E remains the dedicated identity slice.
