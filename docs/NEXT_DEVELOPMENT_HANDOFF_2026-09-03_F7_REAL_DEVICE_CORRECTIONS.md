# Next Development Handoff — 2026-09-03 — UI-R4D-2F / F7 Real-Device Corrections

## 1. Resume point

Resume on:

```text
branch: codex/ui-r4d2-seating-first-setup
Draft PR: #79 UI-R4D-2: seating-first setup flow
base: codex/ui-r4d-persistent-table-foundation / Draft PR #78
last validated executable checkpoint: F6 cleanup head e5109cc95ebb18fdb51a336c24fd4d96e388a0c4
roadmap updated after device feedback; later branch head is docs-only
next executable task: F7.1 Manual pair -> Player Reveal lifecycle regression
```

Always re-query live `main`, PR #78, PR #79, and the live branch head before implementation. Do not merge #78 or #79 without explicit user authorization.

R4D-3 remains blocked until F7.1-F7.6 are clean.

## 2. Required reading for the new conversation

Read first:

```text
AGENTS.md
docs/CURRENT_DEVELOPMENT_ROADMAP.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-03_F7_REAL_DEVICE_CORRECTIONS.md
docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-02_PERSISTENT_HOST_TABLE.md
docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md
docs/TESTING_STRATEGY.md
```

The 2026-09-03 handoff and current roadmap supersede older F7 wording where it conflicts with the real-device findings below.

## 3. Real-device evidence received 2026-09-03

Confirmed by user on device:

- drag-to-reorder works;
- player seat placement looks uneven and does not form the desired visually uniform rounded rectangle;
- Seating still exposes Earlier/Later buttons that are no longer wanted;
- Seating seat cards should not show selectable/selected colors or selection circles;
- Chinese seat labels should be `1号`, `2号`, ... instead of `#1`, `#2`, ...;
- `安排玩家与座位` has poor contrast/readability; previous color corrections did not fully solve screen-level theming;
- Game Selection hierarchy should move `选择游戏` immediately above the game buttons and move `重新安排座位` below them as a clearly secondary action;
- Player Reveal typography is larger but the pair/result vertical order is semantically reversed;
- Manual pair selection can choose a valid clue, press `展示此手动信息`, then close the Manual surface without opening Player Reveal and return to the information-selection screen.

Do not mark F7 complete from earlier automated evidence. Real-device acceptance failed.

## 4. Investigation conclusions already established

### 4.1 Current layout defect is structural presentation policy, not drag failure

`ClocktowerHostTableLayout.kt` currently:

- computes separate horizontal/vertical capacities;
- allocates a count to Top/Right/Bottom/Left;
- calls `evenlySpacedCenters()` independently for each edge.

Therefore spacing is uniform **inside each edge**, not around the complete table perimeter. Corner transitions and different edge lengths produce visually uneven spacing.

F2 remains valuable and should be preserved:

```text
HostTableLayout.slots
-> rendering
-> drag nearest-slot hit testing
-> ringIndex reorder
```

F7.2 should replace the slot-generation spacing policy with one continuous rounded-rectangle perimeter while retaining the single shared slot-ring authority.

### 4.2 Seating selection chrome is caused by setup using shared selectable states

`SeatingFirstSetupUi.kt` currently builds `HostTableInteractionState` with every seat selectable and maps the current player to a selected seat. The shared renderer therefore shows `○` / `①` markers and selected palettes.

Do not delete these states globally. Manual/two-target screens still need visible `①` / `②` non-color selection markers.

Correct direction:

- setup seats render Neutral;
- setup may still keep internal edit/remove subject state;
- drag remains the reorder authority;
- Earlier/Later controls are removed;
- Remove/edit remains reachable without selected-seat chrome.

### 4.3 Chinese seat-label localization is missing in shared table and Player Reveal

Current shared Host seat and pair Player Reveal renderers hard-code `#N`.

Required policy:

```text
zh -> N号
en -> #N
```

Prefer one small presentation helper/policy rather than scattered string concatenation.

### 4.4 Dark-theme defect is screen-root content-color ownership

`ClocktowerDarkTheme` installs a dark `MaterialTheme`, but Seating/Game Selection roots currently paint a background using `Modifier.background(...)` rather than establishing a Material surface/content-color boundary.

Do not fix only `安排玩家与座位` with a hard-coded color. Prefer a correct root such as Material `Surface` semantics using `background` + `onBackground` so ordinary unstyled Text inherits readable dark-theme content color.

### 4.5 Game Selection hierarchy is currently one competing top row

Current top row contains:

```text
Edit seats | Choose game | player count
```

Desired hierarchy:

```text
Choose game
[game buttons]
----------------
Edit seats
```

Preserve F3 navigation semantics and confirmed-seating authority.

### 4.6 Pair Player Reveal has correct typed identity but wrong reading order

F5 established the permanent typed projection:

```text
clocktowerPairPlayerRevealPresentation(step, cards)
-> typed ClocktowerSeatId + playerName
-> no localized displaySecondary seat parsing
-> sanitized Player Reveal
```

Do not undo it.

Current renderer order is effectively:

```text
title
seats
primary result/role
footer/context
```

Desired examples:

`EitherOne`:

```text
男爵
在下面两位玩家之中
2号      10号
Alice    Ken
```

Fortune Teller `YesNo`:

```text
查询下面两位玩家
2号      10号
Alice    Ken
有
```

F7.5 should vary vertical order by display kind without changing F4 source convergence or F5 typed identity/privacy.

### 4.7 Manual pair failure is downstream of valid option resolution

Already confirmed code path:

```text
ClocktowerPairManualSelectionDialog
-> onConfirm(manualOption)
-> showRecommendedDisplayOption(manualOption)
-> onApplyRecommendedDisplayOption(option)
-> resolveClocktowerPlayerDisplay(step, option)
-> onShowPlayerDisplay(displayStep)
```

The Manual option reaches the same F4 source-agnostic projection used by recommendation.

Inside `ClocktowerHostScreen`, Player Reveal is owned by:

```text
var playerDisplayStep: ClocktowerNightStepUi?
```

and appears only after:

```text
playerDisplayStep = displayStep
```

Before that assignment, current callbacks guard publication with:

```text
informationDecisionPublicationAllowed(displayStep)
publishFirstNightInformation(displayStep)
```

For ordinary Manual pair options, the structured confirmation/revision guard is not currently the leading suspect because these options do not normally carry `informationDecisionConfirmation`.

The narrowed investigation boundary is `publishFirstNightInformation()` / `FirstNightInformationMigration` lifecycle. That code can return false when a decision is already considered displayed, before `playerDisplayStep` is assigned.

Important: this is a **narrowed hypothesis/boundary, not yet a completed root-cause proof**. F7.1 must establish the exact failing state sequence with a focused regression test before production repair.

Do not solve it by simply deleting the already-displayed guard. That guard protects duplicate first-night publication and exactly-once information history.

## 5. F7 implementation plan

### F7.1 — Manual pair -> Player Reveal lifecycle regression — FIRST

Goal:

> The first valid Manual pair confirmation opens the same sanitized Player Reveal as an equivalent recommendation, exactly once.

Tests-first:

1. create a focused behavior RED that reproduces the current legal Manual pair handoff failure;
2. characterize the exact lifecycle state that causes `publishFirstNightInformation()` to reject the handoff;
3. repair ownership/order at the narrowest boundary;
4. prove first Manual handoff opens Player Reveal;
5. prove duplicate/re-entry publication is still blocked;
6. prove exactly-once durable observation/event semantics remain intact;
7. prove recommendation and Manual resolving to the same outcome still produce identical finalized display fields.

Do not expand into ranking, EPI-MQ, A4/ZDD, registration redesign, Mayor redirect, Imp succession, or R4D-3.

### F7.2 — continuous rounded-rectangle perimeter slots

Replace edge-wise independent spacing with a continuous path model.

Conceptual target:

```text
rounded rectangle path
-> total perimeter length
-> deterministic start point/orientation
-> N equal path-length samples
-> HostTableSpatialSlot list
```

Keep:

- typed/stable ring index;
- one slot list for renderer and drag hit testing;
- drag/reorder semantics already accepted on device;
- center clearance and fail-closed capacity constraints.

Add meaningful pure geometry tests for 5 / 8 / 12 / 15 players. Prefer path-distance/ordering/non-overlap contracts, not pixel snapshots.

### F7.3 — Seating simplification + theme + seat localization

- remove Earlier/Later controls;
- preserve Remove/edit;
- setup seats remain visually Neutral;
- do not remove Manual selection states globally;
- `zh: N号`, `en: #N`;
- fix dark screen root content-color inheritance;
- verify title/body readability.

This is mostly presentation work; use existing behavior tests plus small pure presentation/localization tests where stable. Do not invent brittle source-string tests.

### F7.4 — Game Selection hierarchy

Move Choose Game immediately above game buttons. Move Edit Seats below game buttons as clearly secondary. Preserve player count context and F3 Back behavior.

No game-selection authority change.

### F7.5 — Player Reveal semantic order

Preserve typed F5 projection and F4 source convergence.

Render policy:

```text
EitherOne: primary -> context/footer -> seats
YesNo:     query/context -> seats -> result
Number:    query/context -> seats -> result when two-subject semantics apply
```

Keep Player Reveal sanitized, table-free, and free of Storyteller-only fallback explanation.

### F7.6 — fresh APK + real-device acceptance

Validate:

- 5 / 8 / 12 / 15 perimeter balance/readability;
- no clipping/overlap;
- cross-corner drag still correct;
- setup simplification;
- Chinese/English seat labels;
- dark-theme readability;
- Game Selection hierarchy;
- recommendation pair reveal;
- Manual pair reveal;
- identical output for equivalent Manual/recommendation outcome;
- EitherOne / Fortune Teller YesNo / two-subject Number order;
- no Host/recommendation/internal explanation leak.

Only then mark F7 complete and unblock R4D-3.

## 6. Testing policy

Follow `AGENTS.md` + `docs/TESTING_STRATEGY.md`.

Use meaningful RED only where behavior/invariants change:

- F7.1 requires a real behavior RED;
- F7.2 requires pure geometry contracts before replacing the algorithm;
- F7.3-F7.5 presentation-only parts should not receive ceremonial pixel/source-shape tests.

At meaningful executable checkpoints:

```text
focused tests
-> :app:testFast
-> exact diff audit / git diff --check
```

Escalate only according to affected risk/classifier.

`ClocktowerHostScreen.kt` and `ClocktowerNightStepUi.kt` remain protected orchestration files. Prefer small cohesive owners and exact-anchor one-shot patching if those large files must be changed.

## 7. Permanent invariants that must survive F7

- stable typed seat identity;
- one ordered slot ring shared by render and drag;
- confirmed seating remains frozen game-start authority;
- Manual is a permanent user authority path;
- Manual and recommendation for the same resolved outcome converge before Player Reveal;
- Player Reveal derives pair seat identity from typed propositions, not localized strings;
- Player Reveal is sanitized and contains no Host table;
- draft selection and confirmed observation/history remain separate;
- first-night information publication/history remains exactly once;
- F2 drag semantics and F3 Back navigation already accepted/verified must not regress.

## 8. Scope guards

Do not start:

- UI-R4D-3 Day workspace;
- Public Claim integration;
- nomination/vote state machine;
- recommendation ranking changes;
- EPI-MQ / Productive Uncertainty;
- A4/ZDD rollout;
- Mayor redirect redesign;
- Imp succession redesign;
- unrelated Host/App decomposition;
- merge of #78/#79.

## 9. New-conversation opening instruction

Recommended user prompt:

```text
请读取根目录 AGENTS.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md 和 docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-03_F7_REAL_DEVICE_CORRECTIONS.md。先重新确认 live main、Draft PR #78/#79 和 codex/ui-r4d2-seating-first-setup 当前 head/checks，并区分 docs-only head 与最后验证的 F6 executable cleanup e5109cc95ebb18fdb51a336c24fd4d96e388a0c4。然后从 F7.1 Manual pair -> Player Reveal lifecycle regression 开始，先建立能复现真机问题的 focused behavior RED，再定位并修复 publication/lifecycle 边界。不要扩大到 F7.2 以后、R4D-3、EPI-MQ/A4/ZDD/Mayor/Imp，也不要 merge PR。
```
## F7 closeout — 2026-09-03

F7.1-F7.5 are executable and validated. F7.6 workflow run `33722208538` built a fresh debug APK from product checkpoint `40b604eae7ea489347357f88fd3d07be83ce5a78`. The user completed real-device acceptance and reported the F7 checklist passes.

F7 is COMPLETE. R4D-3 is unblocked. Continue from `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-03_UI_R4D3_DAY_WORKSPACE.md`; do not merge Draft PR #78/#79 without explicit authorization.
