# UI-NAV-1 — Global Navigation Visual Audit

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACCEPTED PLANNING BASELINE — READ-ONLY AUDIT COMPLETE**  
> Base audited: live `main` `9c19484c682044bb469d90fb7522810ca49ecac2`  
> Campaign: **Global Navigation Visual Unification**

## 1. Product decision

UI-NAV-1 standardizes navigation **presentation**, not navigation ownership.

Accepted product baseline:

```text
No persistent global top bar.

Screen/content owns:
- current title / stage label;
- current instructions;
- square-table or other task content;
- optional local progress presentation.

Bottom navigation geometry:
[ Previous ]   [ Host Tools ]   [ Next ]
```

The three positions are stable visual slots. Their visual hierarchy may differ:

- Previous: secondary;
- Host Tools: utility / tertiary;
- Next: primary.

The campaign must remain compact so the content and square table retain as much vertical space as possible.

**Progress is explicitly optional and screen-owned.** Identity reveal keeps its useful progress presentation. Night progress may be added later if a concrete screen design benefits from it, but UI-NAV-1 does not impose one global identity/night progress semantic contract and progress is not an acceptance criterion for this campaign.

## 2. Scope fence

This campaign does **not** authorize:

```text
- Screen ownership migration
- Navigation Compose introduction
- route/state-machine redesign
- BackHandler semantic changes
- persistence/recovery changes
- gameplay phase-transition semantic changes
- Planner / Reducer / ClocktowerGameSession ownership changes
- EPI-MQ production/ranking changes
- renewed D6 decomposition for file-size reasons
```

Small stateless presentation composables are allowed where they remove repeated visual implementations.

## 3. Current ownership findings

### 3.1 App/root navigation ownership

`CampBoardGameHostApp.kt` still owns the top-level `Screen` state and active flow routing. UI-NAV-1 has no reason to move that ownership.

Current active-game Host Tools state is also already owned at App root through `showHostTools` / `hostToolTab`. `HostGameToolsScreen` is shown as a full-screen overlay and dismissed back into the current flow.

This is the correct owner to retain. The new bottom Host Tools action should call the existing open mechanism rather than introducing a coordinator or navigation controller.

### 3.2 Current persistent top chrome

App root currently renders `HostToolsTopBar` only for:

```text
WerewolfJudge
ClocktowerJudge
Game
```

and only while results are not being shown.

That bar contains the current Host Tools entry. UI-NAV-1 should retire this persistent top bar and re-home the same entry into the bottom action area where capability is valid.

### 3.3 Host Tools composition

`HostGameToolsScreen` currently owns local tab selection and exposes three tabs:

```text
Roles
Records
History
```

It does not own gameplay/session state. It receives cards, records, events and history as inputs plus dismissal/new-game callbacks.

This makes a later `Settings` tab feasible without changing the authoritative state owner, provided Settings values/callbacks continue to come from App root.

### 3.4 Settings ownership

`SettingsScreen` is already a distinct Compose screen. Language mode, Storyteller automation mode, common players and mutation callbacks are supplied from App root.

Therefore moving the **entry/presentation** of Settings under Host Tools does not require moving Settings state ownership. However, this is a composition/navigation-entry change rather than pure button styling, so it must be isolated as a final small UI-NAV slice rather than mixed into the base navigation primitive.

### 3.5 Night-flow navigation seam

The current Clocktower night-step UI already receives explicit presentation callbacks/state such as:

```text
canGoPrevious
onPrevious
onNext
showNavigationActions
```

Normal night-step content therefore does not need a new navigation state model. Existing Next-enabled and special-step rules must be preserved exactly.

### 3.6 Square-table navigation seam

UI-R5 already established a shared `ClocktowerSquareTableStepNavigation` presentation seam for many square-table dialogs. It currently expresses the two navigation positions Previous / Next.

UI-NAV-1 should converge this existing seam onto the new shared bottom action visual language rather than creating a second square-table navigation abstraction.

Some older/special square-table interactions still implement their own Previous/Next/Done controls. These are visual divergence points and should migrate to the same primitive while preserving their current callbacks and enabled rules.

### 3.7 Identity reveal / pass-phone flow

Clocktower identity reveal already has its own privacy-oriented presentation, including useful local progress and a bottom primary action. Its progress should remain screen-owned.

Two capability restrictions are important:

1. **Do not invent Previous behavior.** Going back can expose a prior player's secret identity and is not a visual-only change.
2. **Do not expose Host Tools to the player holding the phone.** The existing Host Tools Roles tab can reveal all real identities. Current production avoids this by exposing Host Tools only in judge/game stages.

The fixed three-slot geometry may still be used visually, but unavailable actions must remain unavailable. Disabled/reserved slots are preferred over creating new navigation semantics.

## 4. Visual inventory / migration classification

| Region | Current pattern | UI-NAV-1 target | Change class | Main risk |
|---|---|---|---|---|
| Landing | dedicated landing content | no forced global nav | none / out of core | avoid chrome for a non-flow screen |
| Seating / game selection / game settings | screen-local Back/Start/Settings controls | compact bottom action language where existing callbacks map cleanly | visual + small composition | do not alter seating back transitions |
| Pass phone / identity reveal | local progress + one primary action | preserve progress; align bottom action geometry without adding unsafe capabilities | visual only | identity/privacy leak |
| Werewolf judge | global HostToolsTopBar + screen-local flow controls | remove top bar; bottom Host Tools uses existing root overlay | visual + callback plumbing | preserve judge progression |
| Clocktower normal night step | two-button Previous/Next | shared three-slot bottom bar | visual + callback plumbing | preserve special Next enabled rules |
| Clocktower square-table night step | shared two-slot nav plus a few local variants | shared three-slot bottom bar | visual convergence | do not move table/domain state |
| Clocktower day flow | flow-specific controls + top Host Tools bar | same bottom navigation language where actions exist | visual + callback plumbing | nominations/execution must retain semantics |
| Generic Game | independent controls + top Host Tools bar | remove top bar; converge relevant navigation/tool entry | visual | do not relabel destructive/end actions as ordinary Next |
| Results / review | dialogs/full-screen review | no forced three-slot bar unless it maps naturally | mostly unchanged | avoid confusing modal actions with flow navigation |
| Settings | standalone Screen.Settings | optional Host Tools Settings tab in final slice | composition-only | preserve root-owned values/callbacks |

## 5. Frozen visual contract

### 5.1 No persistent top bar

There is no global application top bar in active flow screens.

Titles and instructions remain adjacent to the content they describe. Square-table screens keep title/instruction content in the table/content region.

### 5.2 Stable bottom three-slot geometry

Target structure:

```text
[ Previous ]   [ Host Tools ]   [ Next ]
```

Guidance:

- one horizontal row;
- compact outer padding and inter-button spacing;
- stable slot positions across migrated screens;
- touch targets remain usable on phones;
- Next has the strongest emphasis;
- Previous is secondary;
- Host Tools is visually quieter than Next but clearly discoverable;
- disabled actions remain visually present where preserving geometry is useful and where exposing an active action would change behavior.

The **slot identity** is stable, but the right label may remain action-specific (`Next`, `Done`, `Confirm`, etc.) where the existing action is semantically terminal/committing. UI-NAV-1 must not rename a destructive or committing action in a way that hides its meaning merely to obtain identical text.

### 5.3 Capability is not inferred by the visual component

The shared component may know only presentation facts such as:

```text
label
visible/enabled
onClick
visual role
optional icon
modifier
```

It must not accept or infer:

```text
Screen
ClocktowerGameSession
StorytellerPhase
ClocktowerNightStep
RecoveryState
Planner
Reducer
```

The owning screen/root decides whether Previous, Host Tools or Next is actually available.

## 6. Recommended implementation slices

### UI-NAV-1B — shared bottom presentation primitive

Create or adapt a small stateless bottom action primitive, tentatively:

```text
HostBottomActionBar
NavigationActionButton
```

First prove it can represent:

- Previous enabled/disabled;
- Host Tools enabled/disabled;
- Next enabled/disabled;
- action-specific right labels;
- identical stable geometry without knowing flow/domain state.

No production transition behavior changes in this slice.

### UI-NAV-1C — Clocktower Storyteller flow migration

Migrate first because it has the strongest existing seams and is the primary product surface:

```text
normal night step
square-table night interactions
Clocktower day flow
```

Remove `HostToolsTopBar` from the Clocktower judge path only after the bottom Host Tools entry is confirmed equivalent.

### UI-NAV-1D — remaining host/game flow migration

Converge:

```text
Werewolf judge
generic Game
setup / game-selection / game-settings screens where existing callbacks map safely
```

Do not force result/review modals into the three-slot model when their actions are not flow Previous/Host Tools/Next equivalents.

### UI-NAV-1E — identity reveal visual alignment

Keep current privacy/progress behavior. Align only safe geometry/spacing/action styling.

Do not add backwards identity reveal and do not enable the all-role Host Tools overlay while the phone is in player hands.

### UI-NAV-1F — Settings under Host Tools

Isolated composition slice:

- add Settings as a Host Tools tab or equivalent internal destination;
- keep all Settings values and mutations supplied by App root;
- reuse/extract Settings content rather than duplicating it;
- preserve the legacy `Screen.Settings` route until removal is independently proven safe for setup/back/recovery behavior;
- remove the old standalone Settings entry only after equivalent access is verified.

This slice may be skipped/deferred if it requires route ownership changes beyond presentation composition.

### UI-NAV-1G — validation / closeout

Validate behavior invariants and real-device presentation, then return priority to EPI-MQ-0.

## 7. Validation strategy

This is primarily UI/presentation work. Do not manufacture a domain RED solely to satisfy tests-first ceremony.

Protect behavior through existing/focused characterization where useful:

```text
Previous callback/destination unchanged
Next callback/destination unchanged
Host Tools opens/dismisses the same overlay
existing enabled/disabled rules unchanged
identity reveal does not gain unsafe back/tool capability
Settings mutations still reach the same App-root owners
active-game persistence/recovery behavior unaffected
```

Use Compose semantics tests only for stable behavior contracts, not brittle exact-padding assertions.

Use focused compile/test gates during slices, then the normal Android validation gate required by `docs/TESTING_STRATEGY.md`. Visual dimensions and compactness require emulator/real-device inspection; CI is not proof of final visual acceptance.

## 8. GO / decision

**GO — short visual-unification campaign before EPI-MQ-0.**

Reason:

- the main Clocktower night/square-table flows already expose narrow presentation callbacks;
- Host Tools already has a root-owned overlay state that can be reused;
- removing the top Host Tools bar frees meaningful vertical space;
- the existing UI-R5 square-table work provides a natural shared presentation seam;
- the campaign can stay outside gameplay/session/epistemic ownership;
- privacy-sensitive identity reveal can preserve behavior by reserving/disable-only slots rather than inventing new actions.

EPI-MQ-0 should resume immediately after UI-NAV-1 closes.