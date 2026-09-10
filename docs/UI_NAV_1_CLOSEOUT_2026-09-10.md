# UI-NAV-1 Global Navigation Visual Unification — Closeout

> Date: 2026-09-10 Australia/Sydney
> Branch: `codex/ui-nav-1-global-navigation-visual-unification`
> Draft PR: #118 — `UI: unify Storyteller navigation presentation`
> Status at creation: **IMPLEMENTATION COMPLETE / T4 ACCEPTANCE STARTED / DEVICE VISUAL ACCEPTANCE PENDING**

## 1. Product result

UI-NAV-1 standardizes navigation presentation without creating a new navigation owner.

Accepted visual language:

```text
No persistent global top bar.
Content owns stage title / instructions / table / local progress.
Bottom action geometry:
[ Previous ]   [ Host Tools ]   [ Next ]
```

`Previous` remains secondary, `Host Tools` remains tertiary/utility, and `Next` remains primary. Existing route, session, gameplay, persistence and recovery owners remain authoritative.

## 2. Implementation checkpoints

```text
UI-NAV-1B shared HostBottomActionBar:
6ea4f9504e898d67941b3d2e75defe9b54e83c1d

UI-NAV-1C Clocktower night / square-table / day host flow:
69b34fa3094abd820cdf18c4ff8770f2538699ea
one-shot 34455026774 PASS

Werewolf runtime product deletion:
2c00d03b46a767d3fd38ff596d4f1371b7ee7403

UI-NAV-1D safe setup:
e63250b0791d8905a796d85776be3817431cfbf2

UI-NAV-1D generic Game:
c9d6607c8c3dc3700ee9e4229252b52c74a8cded
one-shot 34471666583 PASS

UI-NAV-1D Undercover settings:
11955a254b71d234e820035b7d2a41dd3d608b69

UI-NAV-1D Clocktower settings:
412ab0b08c773767a065f1ca3a1a190b1712fadc

UI-NAV-1E identity reveal controller:
ba2bd1f934dc4f49519fea4f7fbeba309235004b
one-shot 34474761127 PASS

UI-NAV-1F Settings under Host Tools:
6dbb0d5bccd9adca8487947fb7e66f60f64937dc
one-shot 34475818260 PASS

1F cleanup head:
1abba7b11052ff8d044eef71f115b4d61f4f5ec1

authoritative docs reconciliation cleanup head before T4:
e3d97fa3f99239ec30088fdde15e8b508e1238cb
```

## 3. Final ownership/privacy contracts

The closeout audit confirms:

- `HostBottomActionBar` remains a stateless presentation primitive;
- App root remains the owner of `Screen` routing, `showHostTools`, `hostToolTab`, `currentDealIndex`, Settings state and Settings persistence mutations;
- `HostGameToolsScreen` remains the single Host Tools owner;
- Settings is composed into Host Tools through reusable `SettingsContent`; legacy `Screen.Settings` remains available and no duplicate Settings state exists;
- Clocktower identity delivery uses only the existing `currentDealIndex` cursor;
- the Storyteller identity controller receives privacy-safe seat/name/highlight data only;
- the player-facing RevealCard remains isolated from square-table data, Host Tools and cross-player navigation;
- RevealCard Hide returns to the same Storyteller seat and does not auto-advance;
- first-night identity-complete boundary Previous returns to the final identity seat;
- later-night ready boundaries do not gain fabricated Previous semantics;
- authoritative Night → Day commit timing was not moved merely for visual uniformity;
- no Navigation Compose, navigation coordinator, second phase owner, gameplay rollback system, persistence/recovery redesign, or EPI-MQ behavior was introduced;
- Werewolf runtime remains removed; compatibility residues remain fail-closed and were not expanded.

## 4. Whole-PR scope audit before T4

PR #118 was re-audited immediately before this checkpoint:

```text
base main: 9c19484c682044bb469d90fb7522810ca49ecac2
pre-T4 branch head: e3d97fa3f99239ec30088fdde15e8b508e1238cb
PR state: OPEN / DRAFT / mergeable
changed files: 49 before this closeout document
```

The changed-file audit contains production UI/navigation files, the deliberate Werewolf runtime deletion and compatibility regression coverage, and campaign documentation. No temporary UI-NAV one-shot workflow or patch script remains in the net PR diff.

The apparent non-UI persistence/Werewolf changes are not accidental scope drift: they are the already-audited Werewolf product-removal sequence retained in this branch. The runtime must not be restored during closeout.

## 5. T4 acceptance contract

`docs/TESTING_STRATEGY.md` requires an explicit `[full-ci]` logical checkpoint for T4. This document is intentionally committed with that marker so the standard CI classifier selects all full gates even though the checkpoint commit itself is documentation-only.

Required T4 result:

```text
Classify changes                  PASS / full checkpoint selected
Android testFull                  PASS
Debug APK assemble                PASS
ASP contract tests                PASS
Real Clingo cross-validation      PASS
CI gate                           PASS
R2 main-thread boundary           PASS
```

The preceding 1E/1F `testFast + assembleDebug` one-shots are implementation gates, not substitutes for this final T4 acceptance.

## 6. Device/visual acceptance after automated T4

Automated PASS does not prove phone layout quality. Final device acceptance should explicitly exercise:

- setup → game selection → game settings bottom navigation;
- identity square-table controller, Previous/Next cursor movement, explicit Show Identity, Hide back to the same seat, and final-seat first-night boundary;
- ordinary Clocktower night square-table flow and Host Tools access;
- day host flow and generic Game bottom actions;
- Host Tools four-tab layout, especially narrow-screen fit of the History label after Settings was added;
- Settings tab mutations and persistence, plus legacy Settings entry behavior;
- privacy: no role/secret leakage on the Storyteller identity table and no Host Tools/table/cross-player data on the player RevealCard.

If automated T4 passes, implementation may be recorded complete while device-only visual findings remain a separate follow-up. PR #118 stays Draft until that acceptance decision is made explicitly.

## 7. Post-closeout priority

After UI-NAV-1 is accepted, return the roadmap to **EPI-MQ-0 baseline re-audit**. Do not start EPI-MQ production ranking changes from stale historical assumptions; use the existing EPI-MQ handoff and re-audit against merged/live architecture first.
