# NEXT DEVELOPMENT HANDOFF — UI-R5 Remaining Night-role Square-table Convergence

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — SAGE NEXT / LEGACY RETIREMENT AFTER**  
> Scope: Storyteller night-role square-table UI convergence, legacy-surface retirement, and real-device stabilization  
> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117

## 1. Read first

1. root `AGENTS.md`;
2. `docs/README.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`;
7. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
8. live Sage materializer / presentation / NightStep UI code.

D6/R3 are closed; do not reload their archive by default.

## 2. Live branch / checkpoints

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
latest full-T4 verified head: c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
Ravenkeeper checkpoint: 06e01ec7c0410412b38d104f4a5f72bc72811ffe
Spy checkpoint: d45f96ddcdb1142a422d64ee87cf61c5475121f9
Clockmaker production implementation: ae48eecc6c0931ee608cc1402c6355cc807f8e81
Clockmaker verified head: 9119ec83f036432ec9b5f0f3a920690ab9719e16
```

PR #117 remains open, Draft and unmerged. Do not merge without explicit user authorization.

Clockmaker validation:

```text
CI #2106 / run 34417802131
- Android FAST unit tests  PASS
- CI gate                  PASS
- full Android + APK       skipped / per-role UI slice
- ASP                      skipped / UI-only
- Real Clingo              skipped / UI-only
R2 #1973 / run 34417802113 PASS
```

CI #2105 failed only because a pre-existing Spy source-wiring assertion required two generic guards to be adjacent in source. `9119ec83...` made that test ordering-independent; no production code changed in that fix.

Latest full T4 remains `c1ab5578...`; a new full T4 is due only after Sage + legacy retirement.

## 3. Completed final-audit decisions

The read-only surviving-surface audit is complete. Authority:

`docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`

Final classification:

```text
MIGRATE NEXT:
- Sage

IMPLEMENTED / ACCEPTED:
- Clockmaker
- Undertaker
- Ravenkeeper
- Spy shell
- Chef / Empath / Fortune Teller / Chambermaid
- shared single-target and night-ruling square-table interactions
- Washerwoman / Librarian / Investigator pair-information surface

KEEP SPECIALIZED:
- first-night Minion information
- first-night Demon information
- New Demon identity reveal/confirmation

UNREACHABLE LEGACY — RETIRE AFTER SAGE:
- ClocktowerPairManualSelectionDialog
- ClocktowerPairManualCenterControls
  retain/rehome shared pair-manual seat helpers and model still used by the accepted pair table
```

## 4. Clockmaker — complete for automation

Clockmaker now uses a dedicated read-only square-table Storyteller surface.

Contract:

- current actor highlighted independently;
- healthy direct number result preserved;
- drunk/poisoned manual choices remain the existing opaque `ClocktowerDisplayOption`s;
- automatic mode uses only the already-selected `automaticDisplayOption`;
- result text is not reverse-parsed into semantic state;
- existing reveal/publication paths remain authoritative;
- generic recommendation/result-first/unreliable/direct result controls are suppressed while Clockmaker owns the step;
- no Host/session/rules/history/persistence change.

Status: **IMPLEMENTED / automated green / device pending**.

## 5. Sage — exact next target

Sage is the final planned role migration.

Current production materializer already has:

- the dead Sage trigger actor;
- the actual Demon;
- the second shown player;
- reliable and impaired recommendation candidates.

Current weakness: the two shown seats are only encoded in `displaySecondary` text. Sage `ClocktowerDisplayOption.proposition` is currently null. UI must not parse localized/display strings to recover seat identity.

### Required architecture

Introduce a typed **presentation-only** subject-seat seam, separate from epistemic propositions.

Recommended form:

```text
ClocktowerDisplayOption.presentationSubjectSeats: List<Int> = emptyList()
ClocktowerNightStepUi.presentationSubjectSeats: List<Int> = emptyList()
```

Exact naming may differ if a clearer existing convention is found, but the semantics must be presentation-only.

Required behavior:

1. Sage direct reliable display carries exactly two typed subject seats.
2. Each Sage recommendation/unreliable option carries exactly two typed subject seats.
3. `withDisplayOption(...)` copies presentation subject seats into the resolved display step.
4. `ClocktowerPairPlayerRevealPresentation` may consume these typed presentation seats for Sage; it must never parse `displaySecondary`.
5. The Sage square table highlights those two information seats read-only while keeping the dead Sage actor highlight independent.
6. Manual impaired mode chooses only among existing legal/recommended options; do not invent arbitrary seat selection.
7. Automatic impaired mode shows only the already-selected automatic option.
8. Missing, duplicate, malformed or out-of-range subject-seat data fails closed.
9. Final reveal/history stays on existing resolution/publication paths.
10. **Do not add an `InformationProposition` solely for UI.** Impaired Sage options must remain `proposition = null`, preserving the current guard that prevents misinformation from becoming a reliable epistemic observation.

### Existing architectural precedent

`ClocktowerPairPlayerRevealPresentation` already requires typed pair identity and rejects display-text parsing. Reuse/extend that philosophy rather than inventing a localized-string parser.

## 6. Sage RED requirements

Establish tests before GREEN for at least:

```text
healthy Sage uses typed presentation seats without parsing displaySecondary
unreliable Sage options keep proposition = null while carrying presentation seats
automatic Sage uses only the selected automatic option
missing / duplicate / out-of-range presentation seats fail closed
dead Sage actor highlight is independent from the information pair
resolving impaired Sage still leaves displayProposition = null
NightStep wiring has one Sage square-table owner and suppresses generic result surfaces
```

Do not broaden the RED into rule changes or recommendation-provider changes.

## 7. Legacy retirement after Sage

`ClocktowerPairManualSelectionDialog` is no longer called by current NightStep UI, but its file still owns shared helpers used by `ClocktowerPairInformationSquareTableDialog`.

Retirement must remove only the dead dialog / center controls and retain or relocate:

- `clocktowerPairManualSquareTableSeat(...)`;
- `clocktowerPairManualSeatState(...)`;
- `ClocktowerPairManualSelectionModel` and still-valid tests.

After that, re-audit generic recommendation/result-first/unreliable/direct blocks and remove only branches proven unreachable/redundant after all specialized owners are active.

## 8. Validation strategy

Follow `docs/TESTING_STRATEGY.md`.

For Sage:

```text
RED
-> minimal typed presentation seam
-> Sage square-table UI + NightStep wiring
-> focused tests
-> exact diff audit, especially HostScreen / NightStep
-> Android FAST
-> R2
```

Do not run full T4 merely because Sage is one role slice unless a concrete risk requires escalation.

After Sage + retirement:

```text
final exact scope audit
-> final logical full T4
-> cross-role real-device acceptance
-> only then consider PR #117 merge-ready
```

Real-device acceptance remains separate from automation.

## 9. Scope fence

Preserve:

- `ClocktowerGameSession` writable authority;
- Planner/Reducer rule ownership;
- current recommendation candidate generation;
- revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- current Spy Grimoire reveal behavior;
- current dynamic Ravenkeeper-trigger correctness fix.

Do not broaden into EPI-MQ, recommendation-provider redesign, Persistence, unrelated gameplay changes, A4/ZDD, generalized App-root rewrite, or renewed D6 decomposition.

## 10. Remaining execution sequence

```text
1. Sage RED
2. Sage typed presentation-seat seam + square-table GREEN
3. Sage exact diff + FAST + R2
4. legacy pair-manual dialog retirement
5. final generic-result reachability audit / cleanup
6. final logical UI-R5 T4
7. cross-role real-device acceptance
8. UI-R5 closeout / merge only with explicit user authorization
```
