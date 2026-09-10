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
- current owner: CampBoardGameHostApp/root routing owns Screen/showHostTools/Settings state; individual screens own their presentation callbacks; ClocktowerGameSession/Planner/Reducer own gameplay/session truth.
- proposed responsibility: compact, reusable navigation presentation only.
- authoritative state owner(s): unchanged — existing App root, screen flow, Planner/Reducer/session owners.
- narrow typed input/output seam: labels + enabled/visible presentation flags + existing onPrevious/onHostTools/onNext callbacks.
- keep in current owner / extract: keep all flow/state ownership where it is; extract only stateless visual primitives.
- reason: achieve global visual consistency and reclaim top-screen space without creating a second navigation architecture.
```

## 4. Non-negotiable scope fence

Do not:

- introduce Navigation Compose;
- redesign `Screen` or route ownership;
- introduce a navigation coordinator/controller/callback bag;
- change BackHandler semantics;
- create new Previous/undo behavior;
- change gameplay progression or special Next-enabled rules;
- change persistence/recovery ordering or state;
- move Planner/Reducer/ClocktowerGameSession authority;
- change EPI-MQ or recommendation behavior;
- reopen D6 decomposition because a file is large.

This campaign standardizes navigation **presentation**, not navigation ownership.

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

The slot role is stable; the right-side label may remain semantically specific (`Next`, `Done`, `Confirm`, etc.) where the existing action is not literally ordinary progression.

### Capability remains owned outside the bar

The visual component must not infer flow state. Existing owners decide whether a slot is enabled/visible and which callback it invokes.

Important privacy exception:

- during PassPhone / RevealCard, do not enable Host Tools because its Roles tab exposes all real identities;
- do not invent Previous/reveal-back behavior during identity handoff;
- the same three-slot geometry may reserve disabled positions if useful for layout stability.

### Progress is optional

Identity reveal keeps its current useful progress presentation.

Night-flow progress is an optional future/per-screen refinement. UI-NAV-1 does not require a shared identity/night progress model and does not make progress a completion gate.

## 6. Current live seams to reuse

- `showHostTools` / `hostToolTab` at App root already own the Host Tools overlay state.
- `HostGameToolsScreen` is already a full-screen overlay with local Roles / Records / History tab selection.
- Clocktower night-step presentation already receives `canGoPrevious`, `onPrevious`, `onNext`, and `showNavigationActions`.
- many UI-R5 square-table dialogs already use shared `ClocktowerSquareTableStepNavigation`.
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

Validation:

- focused compile;
- focused Compose/presentation test only if it protects stable semantics;
- exact diff audit proving no flow/domain state moved.

### UI-NAV-1C — Clocktower Storyteller flow first

Migrate:

1. ordinary night-step Previous/Next row;
2. shared square-table `ClocktowerSquareTableStepNavigation` consumers;
3. remaining local square-table navigation variants;
4. Clocktower day flow;
5. replace Clocktower judge's top Host Tools entry with the safe bottom entry.

Preserve all existing enabled rules and callbacks exactly.

Real-device spot-check after this slice is strongly preferred because square-table vertical-space recovery is a main product reason for the campaign.

### UI-NAV-1D — remaining host/game flows

Migrate only where existing actions map cleanly:

- Werewolf judge;
- generic Game;
- seating / game selection / game settings.

Do not force Results/Review modal actions into Previous/Host Tools/Next if their semantics are different.

### UI-NAV-1E — identity reveal alignment

Preserve identity reveal progress and privacy semantics.

Only align bottom visual geometry/styles where safe.

Explicitly verify:

```text
no all-role Host Tools access while phone is with player
no new previous-player identity access
existing Reveal/Hide/Next sequence unchanged
```

### UI-NAV-1F — Settings composition under Host Tools

Treat separately from the visual primitive.

Preferred implementation if still narrow after code inspection:

- add Settings as a fourth Host Tools tab/internal content destination;
- extract/reuse Settings content instead of duplicating it;
- continue passing all values/mutation callbacks from App root;
- leave legacy `Screen.Settings` route in place until equivalent setup/back access is characterized;
- only remove the old standalone entry when behavior equivalence is demonstrated.

If this requires broader route/state ownership change, **STOP and defer this slice** rather than expanding UI-NAV-1.

### UI-NAV-1G — validation and closeout

Run the required focused/full validation for changed ownership surfaces, perform exact diff/behavior audit, field-test the main Clocktower flow, then close UI-NAV-1 and restore EPI-MQ-0 as current priority.

## 8. Testing contract

UI/presentation-only changes do not require an artificial domain RED.

Characterize or assert only behavior that matters:

- Previous invokes the same existing callback/destination;
- Next invokes the same existing callback/destination;
- special Next enabled/disabled rules remain unchanged;
- Host Tools opens the same root-owned overlay and dismisses back to the same flow;
- identity reveal does not gain unsafe actions;
- Settings mutations still reach the same App-root state/persistence callbacks;
- active-game persistence/recovery is not modified.

Avoid brittle tests for exact padding, colors or typography values unless a durable design token abstraction already exists.

Use emulator/real-device inspection for compactness, bottom reachability and reclaimed vertical area.

## 9. Stop conditions

Stop and re-audit before implementation if a slice appears to require:

- a new navigation state owner;
- gameplay transition redesign;
- new backwards/undo semantics;
- recovery/persistence changes;
- a second Host Tools state owner;
- duplicating Settings state inside Host Tools;
- EPI-MQ/ranking/domain changes.

## 10. Definition of done

UI-NAV-1 is complete when:

- persistent Host Tools top chrome is removed from migrated active host flows;
- the main Storyteller flow uses one compact bottom navigation visual language;
- square-table screens no longer maintain avoidable competing Previous/Next styles;
- existing callbacks/enabled semantics are preserved;
- identity privacy is preserved;
- Settings-under-Host-Tools is either safely completed as the isolated 1F slice or explicitly deferred with reason;
- progress remains screen-owned/optional;
- required validation passes;
- roadmap/docs are updated and EPI-MQ-0 becomes current again.

## 11. Immediate next action

Start **UI-NAV-1B**.

Before editing production code, re-query branch/main state and inspect the existing shared square-table navigation function plus ordinary night-step navigation row. Implement the smallest stateless three-slot visual primitive first; do not alter callbacks or flow state.