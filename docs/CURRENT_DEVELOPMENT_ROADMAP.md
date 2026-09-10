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
UI-R5 Sage square-table + materializer ownership  IMPLEMENTED / automated green / device pending
UI-R5 legacy pair-manual retirement               COMPLETE / automated green
UI-R5 generic result reachability cleanup         COMPLETE / automated green
Dynamic night-trigger navigation bug              FIXED / T4 + R2 green / device retest pending
UI-R5 final logical T4                            PASS
UI-R5 overall campaign                            ACTIVE — automated acceptance complete / device acceptance next
```

Historical D6/R3 evidence lives under `docs/archive/checkpoints/d6/`. Do not reopen D6 merely because a remaining source file is large.

## 2. Live UI-R5 branch / PR

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
final logical T4 checkpoint: 2958f334fc7cccd59ed2e75a3bdaa60684492292
production tree immediately below T4 docs checkpoint: e84e01c2a8f5064814077d0583e684d2f42b09d7
```

PR #117 remains open, Draft and unmerged. Do not merge without explicit user authorization.

Final logical T4 evidence:

```text
2958f334fc7cccd59ed2e75a3bdaa60684492292
CI #2143 / run 34435295215             PASS
- Classify changes                       PASS
- Android full unit tests + debug APK    PASS
- ASP contract tests                     PASS
- Real Clingo cross-validation           PASS
- CI gate                                PASS
R2 #2010 / run 34435295217              PASS
```

`[full-ci]` correctly selected the full Android branch. The FAST step was skipped intentionally because the full Android unit suite + debug APK gate replaced it at T4.

## 3. Current priority

> **CURRENT: cross-role real-device acceptance and UI-R5 closeout. No further role migration or generic result-surface implementation is planned.**

Product target remains:

> Storyteller night-role operations use one coherent square-table interaction language where that model adds clarity. Pure private reveal/confirmation pages may remain specialized.

Stable UI-R5 rules now protected by implementation/tests:

- NightStep/current actor seat is authoritative;
- `isCurrentActor` is independent from target/information state;
- typed upstream domains own legality and registration witnesses;
- localized display text is never parsed back into semantic identity;
- Storyteller-only truth/reliability information never leaks to player-facing reveal;
- misleading/drunk/poisoned display choices do not gain epistemic propositions merely to support UI;
- specialized thin center controls are preferred over one universal mega-picker;
- existing player-facing reveal pages remain owners where UI-R5 only migrated the Storyteller shell;
- presentation routing stays in `ClocktowerNightStepUi`; domain/session ownership does not move into UI composition.

## 4. Completed UI-R5 implementation slices

### Pair information — Washerwoman / Librarian / Investigator

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

**IMPLEMENTED / automated green / device pending.** Target selection and final role display share one square-table owner. Target legality remains upstream. Impaired choices remain opaque/null-proposition.

### Spy

**IMPLEMENTED / automated green / device pending.** Storyteller shell is read-only square table. Healthy Spy retains the existing Grimoire reveal handoff; poisoned Spy does not gain a true-Grimoire reveal action.

### Clockmaker

**IMPLEMENTED / automated green / device pending.** Read-only square-table Storyteller owner; healthy direct number and impaired opaque choices remain on existing semantic/publication paths.

### Sage

**IMPLEMENTED / automated green / device pending.** Final ownership is:

```text
ClocktowerHostScreen
  -> current Demon / death trigger / effective ability state / card order
  -> prepared facts
ClocktowerSageStepMaterializer
  -> Sage candidate projection / display metadata / presentationSubjectSeats
ClocktowerNightStepUi + ClocktowerSageSquareTableUi
  -> result projection / rendering
```

Unreliable Sage remains `proposition = null`; typed presentation seats are not epistemic truth.

## 5. Legacy retirement and reachability cleanup

Authoritative audits:

- `docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`
- `docs/UI_R5_GENERIC_RESULT_REACHABILITY_AUDIT_2026-09-10.md`

### Pair-manual retirement

```text
9e67fd53b21089a84c897e73ca9abb4c0f4ce17d
CI #2133 / run 34433654157 PASS
R2 #2000 / run 34433654190 PASS
```

Removed only the unreachable `ClocktowerPairManualSelectionDialog` and `ClocktowerPairManualCenterControls`; retained shared seat helpers and `ClocktowerPairManualSelectionModel` still used by the accepted pair square table.

### Generic result reachability cleanup

Production checkpoint:

```text
5cf762c81c333a52e7d744499ff1f1dd28e20c61
```

Final one-shot validation:

```text
run 34434862249 / job 102737709222 PASS
- exact head/blob gate PASS
- focused ownership RED proof PASS
- exact production patch PASS
- diff audit PASS
- focused ownership GREEN PASS
- Android FAST --rerun-tasks PASS
- production push PASS
- temporary workflow/script self-cleanup PASS
```

Persistent production changes:

- retire unreachable `nonPairResultFirstCandidates` branch;
- prevent Chambermaid specialized result owner from also exposing generic recommendation/unreliable result controls;
- retain still-live generic direct/recommendation compatibility paths.

Two obsolete Ravenkeeper/Undertaker source-shape assertions requiring the retired generic branch were removed. Their remaining specialized-owner and suppression guards stay intact.

## 6. Final scope audit

Compared from Sage completion head `053461b7d9868056593ca7a5e3ce2d8e68eb6330` to post-cleanup production tree `e84e01c2a8f5064814077d0583e684d2f42b09d7`.

Persistent production files changed after Sage:

```text
ClocktowerPairManualSelectionUi.kt
ClocktowerNightStepUi.kt
```

Persistent test changes:

```text
+ ClocktowerNightStepResultSurfaceOwnershipTest.kt
~ ClocktowerRavenkeeperSquareTableWiringTest.kt   (-1 obsolete assertion)
~ ClocktowerUndertakerSquareTableWiringTest.kt    (-1 obsolete assertion)
```

Remaining changes are documentation only. Temporary one-shot workflow/script files are absent from the final tree.

The T4 checkpoint `2958f334...` differs from `e84e01c2...` only by the active handoff document, so the validated production tree is unchanged.

## 7. Dynamic Ravenkeeper-trigger device retest

The previously reproduced sequence:

```text
Monk initially protects Ravenkeeper
-> go back
-> reconfirm Monk protecting another player
-> Demon kills Ravenkeeper
-> Ravenkeeper dies but ability step is skipped
```

was fixed in production at `0f66b78...`; automated full T4/R2 remains green. The original device reproduction still needs explicit user retest before device acceptance is claimed.

## 8. Remaining execution sequence

```text
1. cross-role real-device acceptance / pending device retests
2. record any concrete device defects as focused fixes only
3. if no blocking defect, synchronize final closeout docs
4. UI-R5 closeout
5. merge PR #117 only with explicit user authorization
```

Do not reopen generic migration/decomposition work solely because files remain large.

## 9. Validation strategy

Follow `docs/TESTING_STRATEGY.md`.

The final logical UI-R5 T4 is complete. Further validation should now be driven by actual device defects or closeout requirements. Real-device acceptance remains separate from automation and cannot be inferred from CI.

## 10. Frozen architecture / scope fence

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- Planner/Reducer rule ownership;
- typed legal/recommendation/registration domains;
- exact revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- current Spy Grimoire reveal behavior;
- current dynamic Ravenkeeper-trigger correctness fix;
- New Demon identity specialized confirmation surface.

UI-R5 does **not** authorize EPI-MQ scoring, recommendation-provider replacement, generalized App-root rewrite, Persistence follow-up, unrelated game-rule changes, A4/ZDD activation, renewed D6 decomposition, or speculative redesign of specialized private reveal pages.

## 11. Active handoff

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_R5_REMAINING_NIGHT_ROLES.md`
- `docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`
- `docs/UI_R5_GENERIC_RESULT_REACHABILITY_AUDIT_2026-09-10.md`

## 12. UI-R5 completion condition

Automated implementation/validation conditions are satisfied. UI-R5 can close after:

```text
cross-role real-device critical paths recorded
+ dynamic Ravenkeeper-trigger original device scenario retested
+ no blocking device regression remains
+ roadmap/handoff synchronized
+ explicit user decision on PR merge
```
