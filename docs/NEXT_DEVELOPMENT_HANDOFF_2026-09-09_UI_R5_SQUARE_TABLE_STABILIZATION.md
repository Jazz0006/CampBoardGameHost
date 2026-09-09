# NEXT DEVELOPMENT HANDOFF — UI-R5 Square-table Storyteller Convergence

> Date: 2026-09-09 Australia/Sydney  
> Status: **ACTIVE — NIGHT-ROLE SQUARE-TABLE CONVERGENCE / LEGACY UI RETIREMENT**  
> Scope: presentation / interaction architecture + real-device stabilization

## 1. Read first

For the next clean development session, read in this order:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
7. live code for the next role-specific square-table migration.

Do not load the D6 archive by default. D6 decomposition is closed.

## 2. Product target

UI-R5 is not in closeout.

The active target is:

> All Storyteller night-role operations should use one coherent square-table interaction language. Once a replacement path is accepted, the superseded text/list/manual-dialog surface should be retired rather than kept as a parallel UI.

Shared presentation hierarchy:

```text
1. player to wake / current actor        highest visual priority
2. role/action instruction
3. related target or information seats  distinct from actor
4. recommendation / result content
5. action controls
6. Previous / Next                      stable compact single row
```

Use Fortune Teller as the reference for actor presentation:

- NightStep/current actor seat is authoritative;
- `isCurrentActor` owns the strong actor highlight;
- target/information states (`SelectedFirst`, `SelectedSecond`, etc.) remain independent;
- actor and target semantics may coexist on one seat without being conflated;
- never parse localized display text back into actor or target identity.

## 3. Live branch / PR

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
```

Do not merge without explicit authorization.

## 4. Historical pair-information baseline

Washerwoman / Librarian / Investigator were first migrated to a unified square-table recommendation + in-place Manual flow.

Production-equivalent accepted checkpoint:

```text
b960be22d217ed2caa06b49c0319eace23470b42
```

Evidence:

```text
CI #2024 / 34339861281              PASS
Android full unit tests + debug APK PASS / executed
ASP contract tests                  PASS / executed
Real Clingo cross-validation        PASS / executed
CI gate                             PASS
R2 #1891 / 34339861261              PASS
```

The user then reported real-device PASS for that baseline. The dedicated acceptance record is:

- `docs/UI_R5_PAIR_INFORMATION_REAL_DEVICE_ACCEPTANCE_2026-09-09.md`

That acceptance remains useful evidence for pair legality, recommendation seeding, Manual editing and reveal/return semantics. It must **not** be treated as device acceptance of presentation changes added afterward.

## 5. Current pair-information amendment

A later product review found an important missing layer: the pair-information square table showed information targets but did not make the **player who must be awakened** the primary instruction.

The current amendment implements:

```text
NightStep actor seat
-> existing isCurrentActor strong table highlight

NightStep command / wake instruction
-> prominent center wake text
-> role label below
-> recommended/manual information below that

information pair seats
-> existing SelectedFirst / SelectedSecond / Manual states
-> visually independent from actor highlight

navigation
-> shared ClocktowerSquareTableStepNavigation
-> compact equal-width Previous / Next buttons
-> short non-wrapping labels in one row
```

Production/test checkpoint:

```text
d305e7abae646f293e979f1de27895569397cbb9
```

Validation:

```text
CI #2032 / 34348340950              PASS
Android FAST unit tests             PASS / executed
full Android + debug APK            SKIPPED at ordinary iteration checkpoint
CI gate                             PASS
R2 #1899 / 34348340898              PASS
```

Exact amendment diff from pre-amendment head `bb03812cf1dd0a0f9e0079ed40d0497fcddfa0c3` is four files only:

```text
ClocktowerNightActionSquareTableUi.kt
ClocktowerNightStepUi.kt
ClocktowerPairInformationSquareTableUi.kt
ClocktowerPairInformationSquareTablePresentationTest.kt
```

`ClocktowerNightStepUi.kt` changes only two production lines for this amendment: pass `actionActorSeat` and `command` into the pair square-table composition.

Characterization now verifies that actor highlight and pair-information seat state are independent.

Real-device retest of this new presentation is pending. Do not reuse the earlier device PASS as proof for the changed wake/navigation layout.

## 6. Pair-information ownership to preserve

```text
ClocktowerPairManualAuthority
  -> complete typed Manual legality
  -> structured recommendation-to-Manual canonicalization

ClocktowerPairManualSelectionModel
  -> two-player draft state
  -> select/cancel/replace behavior

clocktowerPairManualSeatState
  -> legal selectable/selected/disabled pair projection

ClocktowerSquareTableSeatSurface
  -> shared geometry and seat rendering

ClocktowerNightActionWakeInstruction
  -> shared prominent wake instruction

ClocktowerSquareTableStepNavigation
  -> shared compact one-row step navigation

existing showRecommendedDisplayOption / player-display resolution
  -> final display and existing structured confirmation/observation path
```

`ClocktowerPairInformationSquareTableUi.kt` stays a thin composition owner. It must not absorb recommendation ranking, legal-domain generation, durable history, Recovery or GameState.

## 7. Next migration sequence

After the current pair wake/navigation presentation is accepted on device, continue role-by-role convergence.

Known old-interface targets explicitly identified by the user:

```text
Chef
Undertaker
Ravenkeeper
```

These are not assumed to share one interaction model:

- Chef is numeric information;
- Undertaker is a role/result information flow tied to the executed player;
- Ravenkeeper is a player-target + role-result flow.

They should share the **square-table shell, actor hierarchy and navigation language**, while keeping thin typed role-specific center controls. Do not force them through the pair picker merely for visual uniformity.

After those three, perform a complete read-only inventory of remaining night-role surfaces. Any surviving text/list/manual-dialog path that duplicates an accepted square-table flow is a retirement candidate.

Only after all active night-role paths are accounted for should UI-R5 enter closeout.

## 8. Real-device acceptance for the current amendment

At minimum verify on a portrait phone:

1. Washerwoman: correct actor seat gets the strong wake highlight;
2. the two information seats remain visibly different from the actor;
3. center first clearly says whom to wake;
4. recommendation remains below the wake instruction;
5. Manual mode keeps actor highlight while pair seats change selection state;
6. Previous / Next remain in one row without wrapping;
7. 8–15 player layouts, especially 15 players, do not collide with the new center hierarchy;
8. Show information / return path still works;
9. no first-night display-crash regression.

Record FAIL rather than silently waiving any observed layout defect.

## 9. Validation strategy

Follow `docs/TESTING_STRATEGY.md`.

For iterative presentation slices:

- use focused characterization for stable pure presentation contracts;
- run Android FAST at production-code checkpoints;
- keep R2 green;
- do not manufacture domain REDs for visual-only changes;
- use `[full-ci]` at logical UI-R5 convergence/acceptance checkpoints rather than after every small migration;
- distinguish actually executed full suites from skipped ones;
- real-device validation remains separate from automated acceptance.

Before PR #117 can become merge-ready, take a final logical T4 checkpoint after the role-convergence work and complete cross-role real-device acceptance.

## 10. Architecture / scope constraints

Preserve:

- `ClocktowerGameSession` as canonical writable state;
- Planner/Reducer rule ownership;
- typed upstream legal/recommendation domains;
- existing durable observation/history/Recovery ordering;
- no Compose dependencies in session/domain;
- no storyteller-hidden state leakage to player display;
- Undercover/Werewolf isolation;
- existing square-table geometry unless a concrete device defect requires a focused geometry fix.

Do not broaden UI-R5 into:

- EPI-MQ scoring/explanation;
- recommendation-provider replacement;
- generalized `CampBoardGameHostApp.kt` rewrite;
- Persistence Simplification follow-up;
- unrelated game-rule changes;
- A4/ZDD activation.

## 11. Completion condition

UI-R5 can close only when:

```text
all active night-role Storyteller paths audited
+ appropriate roles converged on the square-table visual language
+ wake actor explicit/distinct where applicable
+ typed role-specific target/result semantics preserved
+ obsolete legacy UI paths retired
+ affected focused/FAST validation green
+ final logical T4 green
+ exact diff/scope audit green
+ cross-role real-device critical paths accepted
+ roadmap/handoff synchronized
```

Current state is **ACTIVE**, not closeout-ready.
