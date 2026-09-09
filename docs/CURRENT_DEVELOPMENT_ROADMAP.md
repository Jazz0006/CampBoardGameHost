# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-10 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current project state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / FULL accepted / merged
R3 deep transaction-application viability audit   COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE

UI-R5 pair-information baseline                   ACCEPTED / T4 + device
UI-R5 wake-actor + compact navigation             IMPLEMENTED / automated green
UI-R5 Chef square-table                           COMPLETE / user device PASS
UI-R5 Empath square-table                         IMPLEMENTED / automated green / device pending
UI-R5 Undertaker square-table                     IMPLEMENTED / automated green / device pending
UI-R5 Ravenkeeper square-table                    IMPLEMENTED / automated green / device pending
UI-R5 Spy square-table shell                      IMPLEMENTED / automated green / device pending
UI-R5 Clockmaker square-table                     IMPLEMENTED / automated green / device pending
Dynamic night-trigger navigation bug              FIXED / T4 + R2 green / device retest pending
UI-R5 remaining-night surface audit               COMPLETE
UI-R5 overall campaign                            ACTIVE — Sage next
```

Historical D6/R3 evidence lives under `docs/archive/checkpoints/d6/`. Do not reopen D6 merely because a remaining source file is large.

## 2. Live UI-R5 branch / PR

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
latest full-T4 verified head: c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
latest Ravenkeeper checkpoint: 06e01ec7c0410412b38d104f4a5f72bc72811ffe
latest Spy checkpoint: d45f96ddcdb1142a422d64ee87cf61c5475121f9
latest Clockmaker verified head: 9119ec83f036432ec9b5f0f3a920690ab9719e16
```

PR #117 remains open, Draft and unmerged. Do not merge without explicit user authorization.

Latest full validation checkpoint remains:

```text
c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
CI #2058 / run 34408187526              PASS
- Android full unit tests + debug APK     PASS
- ASP contract tests                      PASS
- Real Clingo cross-validation            PASS
- CI gate                                 PASS
R2 #1925 / run 34408187513                PASS
```

This remains the latest full-T4 checkpoint. Per-role UI checkpoints below do not replace it.

Latest per-role checkpoints:

```text
Ravenkeeper:
06e01ec7c0410412b38d104f4a5f72bc72811ffe
CI #2087 / run 34415179389  Android FAST + gate PASS
R2 #1954 / run 34415179369   PASS

Spy:
d45f96ddcdb1142a422d64ee87cf61c5475121f9
CI #2092 / run 34415625342  Android FAST + gate PASS
R2 #1959 / run 34415625336   PASS

Clockmaker:
production implementation: ae48eecc6c0931ee608cc1402c6355cc807f8e81
verified head:             9119ec83f036432ec9b5f0f3a920690ab9719e16
CI #2106 / run 34417802131  Android FAST + gate PASS
R2 #1973 / run 34417802113   PASS
```

CI #2105 for the initial Clockmaker head failed only because an old Spy source-wiring test required two generic guards to be textually adjacent. `9119ec83...` made that test ordering-independent; production code did not change.

## 3. Current priority

> **CURRENT: migrate Sage using typed presentation-only pair-seat identity, then retire superseded legacy UI and run final UI-R5 validation.**

Product target:

> Storyteller night-role operations should use one coherent square-table interaction language where that interaction model adds clarity. Pure private reveal/confirmation pages may remain specialized.

Shared presentation hierarchy:

```text
1. current actor / player to wake        highest priority
2. role/action instruction
3. related target / information players  visually distinct from actor
4. result / recommendation
5. action controls
6. Previous / Next                       stable compact one-row navigation
```

Stable rules:

- NightStep/current actor seat is authoritative;
- `isCurrentActor` is independent from target/information state;
- typed upstream domains own legality and registration witnesses;
- localized display text is never parsed back into semantic identity;
- Storyteller-only truth/reliability information never leaks to player-facing reveal;
- misleading/drunk/poisoned display choices must not gain epistemic propositions merely to support UI;
- specialized thin center controls are preferred over one universal mega-picker;
- an existing player-facing reveal may remain the owner when UI-R5 only needs to migrate the Storyteller shell.

## 4. Completed / implemented UI-R5 slices

### Pair-information — Washerwoman / Librarian / Investigator

Accepted baseline:

```text
b960be22d217ed2caa06b49c0319eace23470b42
CI #2024 full Android + APK / ASP / Real Clingo / gate PASS
R2 #1891 PASS
user-reported real-device PASS
```

### Chef

**COMPLETE / user-reported real-device PASS.**

### Empath

**IMPLEMENTED / automated green / device pending.** Uses typed `NumericResult.subjectSeats` for effective living neighbours; impaired arbitrary values do not fabricate contributor-seat witnesses.

### Undertaker

**IMPLEMENTED / automated green / device pending.** Executed seat is anchored by typed `RoleAt`; impaired role choices remain opaque/null-proposition and use existing unreliable publication behavior.

### Ravenkeeper

**IMPLEMENTED / automated green / device pending.** Target selection and final role display share one square-table owner. Target legality remains `clocktowerRavenkeeperTargetCards(cards)`. Impaired choices remain opaque/null-proposition.

### Spy

**IMPLEMENTED / automated green / device pending.** Storyteller shell is read-only square table. Healthy Spy keeps the existing Show-to-player action and exact `onShowPlayerDisplay(step)` handoff to the existing Grimoire page. Poisoned Spy has no true-Grimoire reveal action. Grimoire generation/history were not redesigned.

### Clockmaker

**IMPLEMENTED / automated green / device pending.** Clockmaker now uses a read-only square-table Storyteller owner:

- current actor highlighted independently;
- healthy direct number reveal preserved;
- impaired manual mode preserves existing opaque `displayOptions`;
- automatic mode uses only the already-selected `automaticDisplayOption`;
- shown number text is never reverse-parsed into semantic state;
- final reveal uses existing publication paths;
- generic recommendation/result-first/unreliable/direct result surfaces are suppressed while Clockmaker owns the step;
- no Host/session/rules/history/persistence behavior changed.

## 5. Remaining-night surface audit

Authoritative audit:

`docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`

Final classification:

```text
MIGRATE:
- Sage

KEEP SPECIALIZED:
- first-night Minion information
- first-night Demon information
- New Demon identity reveal/confirmation

UNREACHABLE LEGACY — RETIRE AFTER SAGE:
- ClocktowerPairManualSelectionDialog
- ClocktowerPairManualCenterControls
  (retain/rehome shared seat helpers and model still used by the accepted pair table)
```

All other active supported night interactions are already accepted/implemented square-table owners.

## 6. Sage migration contract

Sage is the final role migration before legacy retirement.

The current Sage materializer knows the dead Sage actor, the actual Demon, and the second shown player, but current `ClocktowerDisplayOption`s expose those two seats only through display text. UI-R5 must not parse `displaySecondary`.

Required design:

```text
typed presentation-only subject seats
-> no epistemic proposition invented for UI
-> reliable and impaired candidate pair identity remains available to presentation
-> impaired options remain proposition = null
-> dead Sage actor highlight independent from the two information seats
-> no arbitrary seat selection unless the existing legal domain explicitly supports it
-> final reveal/history stays on existing resolution/publication paths
-> malformed/missing/duplicate/out-of-range pair identity fails closed
```

Existing `ClocktowerPairPlayerRevealPresentation` is the architectural precedent: pair identity comes from typed data, never display text.

## 7. Dynamic night-trigger navigation bug

The previously reproduced sequence:

```text
Monk initially protects Ravenkeeper
-> go back
-> reconfirm Monk protecting another player
-> Demon kills Ravenkeeper
-> Ravenkeeper dies but ability step is skipped
```

was fixed in production at `0f66b78...`; full T4/R2 are green at `c1ab5578...`. The original device reproduction still needs explicit user retest before device acceptance is claimed.

## 8. Immediate execution sequence

```text
1. Sage typed presentation-seat RED
2. Sage square-table migration
3. Sage exact diff + Android FAST + R2
4. retire unreachable old pair-manual dialog/center controls
5. re-audit generic recommendation/result-first/unreliable/direct branches
6. exact scope audit
7. final logical UI-R5 T4
8. cross-role real-device acceptance
9. UI-R5 closeout / merge only with explicit user authorization
```

## 9. Validation strategy

Follow `docs/TESTING_STRATEGY.md`.

For Sage and retirement slices:

- tests-first where a stable semantic/presentation contract is changing;
- focused tests during implementation;
- Android FAST at production checkpoints;
- keep R2 green;
- exact diff audit, especially for `ClocktowerHostScreen.kt` and `ClocktowerNightStepUi.kt`;
- no full T4 until the final logical UI-R5 checkpoint unless risk requires escalation.

Real-device acceptance remains separate from automation.

## 10. Frozen architecture / scope fence

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- Planner/Reducer rule ownership;
- typed legal/recommendation/registration domains;
- exact revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- existing square-table geometry unless a concrete device defect requires a focused fix.

UI-R5 does **not** authorize EPI-MQ scoring, recommendation-provider replacement, generalized App-root rewrite, Persistence follow-up, unrelated game-rule changes, A4/ZDD activation, renewed D6 decomposition, or speculative redesign of the Spy Grimoire page.

## 11. Active handoff

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_R5_REMAINING_NIGHT_ROLES.md`
- `docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`
- `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

## 12. UI-R5 completion condition

UI-R5 can close only when:

```text
Sage migrated or explicitly reclassified
+ superseded reachable legacy UI retired
+ affected focused/FAST/R2 green
+ final logical T4 green
+ exact diff/scope audit green
+ cross-role real-device critical paths recorded
+ no hidden-information/domain/persistence regression
+ roadmap/handoff synchronized
```

The project is **not yet at this condition**.
