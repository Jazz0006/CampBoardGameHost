# NEXT DEVELOPMENT HANDOFF — UI-NAV-1 Global Navigation Visual Unification

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — UI-NAV-1D COMPLETE / UI-NAV-1E NEXT**  
> Live main re-audited: `9c19484c0b6381c94440c2253079d5c2ba5f796f`  
> Branch: `codex/ui-nav-1-global-navigation-visual-unification`  
> Re-audited branch head before this docs update: `02342be73d0afbfe681e37c9df95b46812c7c846`  
> Draft PR: `#118 — UI: unify Storyteller navigation presentation` — **OPEN / DRAFT**  
> Audit baseline: `docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`

## 1. Objective

Standardize the application's flow-navigation presentation around a compact bottom action area while preserving existing navigation/gameplay ownership.

Accepted product direction:

```text
NO persistent global top bar.

Content area owns title / instructions / table / optional progress.

Bottom action geometry:
[ Previous ]   [ Host Tools ]   [ Next ]
```

Visual hierarchy:

```text
Previous   = secondary
Host Tools = tertiary / utility
Next       = primary
```

Core rule:

> **Standardize navigation presentation, not navigation ownership.**

The campaign remains presentation-first and must finish before returning to EPI-MQ-0.

## 2. Read first

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md` only when a square-table product detail needs confirmation.

Do not load EPI-MQ internals unless needed to confirm that a proposed UI change does not cross an ownership boundary.

## 3. Live GitHub / validation status re-audited on 2026-09-10

Before this documentation reconciliation:

```text
live main:
9c19484c0b6381c94440c2253079d5c2ba5f796f

feature branch:
codex/ui-nav-1-global-navigation-visual-unification

branch head / PR #118 head:
02342be73d0afbfe681e37c9df95b46812c7c846

PR #118:
OPEN
DRAFT
mergeable at audit time
```

The exact `02342be...` cleanup head has no ordinary check-runs. Its two Actions entries (`PR Scope Guard` and `R2 Mainline Regression`) ended `action_required`; this is workflow/start gating and must not be reported as a code/test failure.

The latest user-verified cleanup baseline remains:

```text
real Werewolf product deletion commit:
2c00d03b46a767d3fd38ff596d4f1371b7ee7403

later one-shot / diagnostic cleanup head:
02342be73d0afbfe681e37c9df95b46812c7c846

exact diff audit        PASS
:app:testFast           PASS
:app:assembleDebug      PASS
```

`GameKind.Werewolf` / `WerewolfRecovery` that still exist are legacy-data compatibility only. Recovery remains fail-closed. **Do not reintroduce Werewolf runtime and do not expand persistence migration.**

## 4. Architecture / scope fence

Current ownership remains:

```text
CampBoardGameHostApp/root
- Screen routing
- showHostTools / hostToolTab
- Settings state
- currentDealIndex

individual screens
- presentation callbacks / layout composition

ClocktowerGameSession / Planner / Reducer
- gameplay/session truth
```

UI-NAV may extract/reuse stateless visual primitives and screen-local composition. It must not create a second navigation or gameplay authority.

Do not:

- introduce Navigation Compose;
- redesign `Screen` / route ownership;
- add a navigation coordinator/controller or mega callback bag;
- broadly redesign `BackHandler`;
- create gameplay undo / rollback semantics;
- change persistence/recovery ordering or ownership;
- move Planner/Reducer/ClocktowerGameSession authority;
- change EPI-MQ or recommendation behavior;
- reopen D6 because a file is large.

Narrowly authorized behavior remains limited to the explicitly approved identity cursor and safe phase-boundary presentation described below.

## 5. Shared visual contract and live seams

`HostBottomActionBar` remains the shared stateless presentation primitive:

```text
Previous slot = secondary / OutlinedButton
Host Tools slot = utility / TextButton
Next slot = primary / Button
three stable equal-width slots
independent enabled / visible flags
48dp minimum slot height
single-line labels with overflow protection
no Screen/session/domain knowledge
```

Reuse existing owners:

- `showHostTools` / `hostToolTab` at App root;
- `HostGameToolsScreen` as the only Host Tools owner;
- existing per-screen `onBack`, `onCancel`, `onPrevious`, `onNext`, `onStart`, `onConfirm`, etc.;
- existing square-table presentation seams;
- App-root `currentDealIndex` for identity delivery;
- App-root Settings values/mutation callbacks.

Do not replace these seams merely for naming or aesthetic cleanup.

## 6. Execution status

### UI-NAV-1A — read-only audit — COMPLETE / GO

Accepted baseline audit:

`docs/UI_NAV_1_GLOBAL_NAVIGATION_VISUAL_AUDIT_2026-09-10.md`

That file remains a planning/audit baseline. This handoff records the later live-code reconciliation and supersedes stale execution-status wording from the original audit where necessary.

### UI-NAV-1B — shared bottom visual primitive — COMPLETE / PASS

Code checkpoint:

`6ea4f9504e898d67941b3d2e75defe9b54e83c1d`

Validation:

```text
CI run 34444205127
- Android FAST unit tests PASS
- CI gate PASS

R2 run 34444205141 PASS
```

No Compose UI-test framework was invented merely to test layout mechanics; `docs/TESTING_STRATEGY.md` permits compile/static/exact-diff evidence for presentation-only work.

### UI-NAV-1C — Clocktower night / square-table / day host flow — COMPLETE / PASS

1C migrated ordinary Clocktower night-step navigation, shared square-table navigation, first-night/night-ready Storyteller prompt, private Dawn review, New Demon confirmation, Day Overview, Nomination, Vote, EndConfirm, Slayer, Artist and Klutz to the unified bottom visual language where safe.

Existing callback ownership was preserved. Where no safe Previous existed, the Previous slot remained reserved but disabled.

The persistent `ClocktowerJudge` top Host Tools chrome was removed only after equivalent bottom Host Tools access existed.

Privacy fences remain explicit:

- Dawn public announcement remains public/full-screen and has no Host Tools chrome;
- player-facing identity reveal remains isolated and has no Host Tools or cross-player navigation.

Production checkpoint:

`69b34fa3094abd820cdf18c4ff8770f2538699ea`

One-shot run `34455026774` passed exact diff, privacy assertions, `:app:testFast`, remote-head recheck, production push and cleanup.

The end-of-night phase transition was **not** moved merely to manufacture a Previous button. Current Dawn confirmation owns meaningful domain commit work; a pre-transition boundary must be separately characterized before any authoritative Night → Day commit timing is changed.

### UI-NAV-1D — remaining host/game/safe-setup flows — COMPLETE / PASS

Live reconciliation and implementation are complete.

Completed surviving surfaces:

```text
SeatingFirstSetupScreen
- Next reuses onConfirmSeats
- Previous disabled
- Host Tools disabled before a hosted game exists
- Settings gear retained for UI-NAV-1F

SeatingFirstGameSelectionScreen
- Previous reuses onBackToSeating
- game choice remains a content action
- no invented selected-game progression state

Screen.Game / GameScreen
- final persistent root Screen.Game HostToolsTopBar removed
- root-owned showHostTools/hostToolTab callback is passed into GameScreen
- Previous disabled
- Host Tools opens the same root-owned overlay
- Next reuses the existing End & Reveal / View Results callback

UndercoverSettingsScreen
- top Back retired
- Previous reuses onBack
- Next reuses onStart
- exact player-count enable rule preserved
- Host Tools disabled pre-game

ClocktowerSettingsScreen
- top Back and duplicate bottom return action retired
- Previous preserves local step-back; step 0 reuses onBack
- Next preserves local step-forward; final step reuses onStart
- canStart rule unchanged
- Host Tools disabled pre-game
```

Historical WerewolfJudge 1D work was superseded by deliberate Werewolf runtime deletion at `2c00d03b46a767d3fd38ff596d4f1371b7ee7403`; do not restore it.

Validation evidence:

```text
safe seating/setup checkpoint: e63250b0791d8905a796d85776be3817431cfbf2
- R2 run 34471425850 PASS

generic Game production checkpoint: c9d6607c8c3dc3700ee9e4229252b52c74a8cded
- exact one-shot run 34471666583 PASS
- exact source/diff audit PASS
- :app:testFast PASS
- :app:assembleDebug PASS

live settings audit: run 34472496605 PASS
Undercover checkpoint: 11955a254b71d234e820035b7d2a41dd3d608b69
Clocktower setup checkpoint: 412ab0b08c773767a065f1ca3a1a190b1712fadc
- exact two-production-file diff audit PASS
- R2 run 34472655199 PASS
- CI run 34472655245: Classify PASS / Android FAST PASS / CI gate PASS
```

No navigation owner, gameplay owner, persistence/recovery owner, or Settings owner changed. UI-NAV-1E is now the active production slice.

### UI-NAV-1E — Identity Reveal square-table controller — QUEUED AFTER 1D

The old progress-bar/pass-phone controller is not the final design.

Storyteller-side target:

```text
reuse square-table UI
current target player highlighted
center: N / total
center: Show identity to seat N · player name
explicit Show identity button
bottom: Previous / Host Tools / Next
```

Privacy-safe table controller may show only:

- seat number;
- player name;
- current highlight;
- local N / total;
- explicit Show identity action.

It must not show role, alignment, poison/drunk state, hidden state or any Storyteller secret.

Routing ownership stays narrow:

- `Screen.PassPhone` is reinterpreted as the Storyteller square-table reveal controller;
- `Screen.RevealCard` remains the player-facing isolated full-screen reveal;
- do not churn route/enum names merely for aesthetics.

`currentDealIndex` remains the single cursor. Do not add `selectedRevealSeat`, `furthestRevealedSeat`, or another completion authority unless a concrete correctness/recovery failure proves it necessary.

Behavior:

- first seat Previous disabled;
- Previous/Next only move the current target cursor;
- Previous/Next must never auto-reveal a role;
- Storyteller may move backwards and explicitly reveal a prior player again;
- v1 does not need arbitrary seat-tap navigation unless implementation is trivial and risk-free;
- player-facing RevealCard shows only that player's role + ability text + safe hide/finished control;
- RevealCard contains no square table, Previous, Next, Host Tools or other-player information;
- hiding identity returns to the **same** Storyteller controller seat and does not auto-advance.

Last-seat boundary:

```text
last identity seat
[ Previous ] [ Host Tools ] [ Next ]

Next
-> identity-complete / first-night boundary

boundary
Identity display complete
Prepare for first night
[ Previous ] [ Host Tools ] [ Start Night ]
```

Boundary Previous returns to the final identity seat so the role can be shown again.

### UI-NAV-1F — Settings composition under Host Tools — LATER / ISOLATED

Preferred only if narrow:

- add Settings as a Host Tools tab/internal destination;
- reuse existing Settings content;
- keep all Settings state/mutation ownership at App root;
- do not duplicate Settings state;
- keep legacy `Screen.Settings` until equivalent behavior is characterized.

If this requires broad route/state ownership changes, **STOP and defer 1F**.

### UI-NAV-1G — validation / closeout

Run focused/full validation appropriate to changed presentation/ownership seams, exact diff audit, and real-device/emulator visual acceptance. Then close UI-NAV-1 and restore EPI-MQ-0 as current priority.

## 7. Phase-boundary rule

General UX language:

```text
last ordinary operation
-> Next
-> dedicated Storyteller phase-boundary prompt
-> Previous / Host Tools / explicit Start/Continue phase
```

Identity boundary may be implemented in 1E because its cursor behavior is explicitly approved.

Night boundary is separately constrained. Before changing it, audit exactly when the final night-role action:

- commits domain effects;
- advances phase;
- writes recovery/persistence;
- enters Dawn/broadcast presentation.

Desired only if the current architecture supports a narrow change:

```text
role effect commits normally
last role Next -> pre-transition boundary
boundary Start Day -> authoritative Night -> Day transition
boundary Previous -> safely returns to final night step
```

Do not introduce rollback architecture or broad state-machine redesign. If that narrow seam does not exist, document/defer the night boundary.

## 8. Testing contract

This is a UI/presentation campaign. Do not manufacture a fake gameplay/domain RED.

Prefer:

- focused characterization around changed callback ownership;
- Compose semantics tests where an existing suitable harness exists;
- compile;
- `:app:testFast`;
- exact diff audit;
- final Android gate;
- emulator/real-device visual acceptance.

Must preserve:

- original domain callbacks except the explicitly approved identity cursor/boundary behavior;
- special Next enabled/disabled rules;
- Host Tools opens the same App-root-owned `HostGameToolsScreen`;
- Settings persistence/state ownership;
- persistence/recovery semantics;
- player-facing RevealCard privacy boundary.

If a boundary requires moving authoritative phase transition timing, establish a meaningful behavior characterization before production change.

## 9. Stop conditions

Stop and re-audit if a slice appears to require:

- a new navigation state owner;
- gameplay undo/rollback semantics;
- recovery/persistence migration;
- a second Host Tools owner;
- a second identity-completion authority with no demonstrated need;
- duplicate Settings state;
- EPI-MQ/ranking/domain changes;
- broad route/state-machine redesign;
- moving authoritative Night → Day commit timing without dedicated characterization.

## 10. Immediate next action

Start **UI-NAV-1E Identity Reveal square-table controller**.

Current characterized behavior to replace:

- `Screen.PassPhone` is the old progress/pass-phone controller;
- `Screen.RevealCard.onHide` currently auto-increments `currentDealIndex`;
- the last reveal currently jumps directly to `Screen.ClocktowerJudge`.

Required 1E behavior:

- reinterpret `Screen.PassPhone` as the privacy-safe Storyteller square-table controller;
- Previous/Next move only the single existing `currentDealIndex`;
- explicit Show opens `Screen.RevealCard`;
- RevealCard Hide returns to the same controller seat without auto-advance;
- last-seat Next enters the existing `ClocktowerJudge` first-night ready/recommendation boundary while leaving `nightStarted=false`;
- that boundary should expose Previous back to the final identity seat if this remains a narrow callback-only change;
- do not expose table/Host Tools/cross-player information on RevealCard;
- do not add a second identity cursor or persistence migration.
