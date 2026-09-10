# NEXT DEVELOPMENT HANDOFF — UI-R5 Remaining Night-role Square-table Convergence

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — AUTOMATED ACCEPTANCE COMPLETE / REAL-DEVICE ACCEPTANCE NEXT**  
> Scope: Storyteller night-role square-table UI convergence, legacy-surface retirement, and real-device stabilization  
> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117

## 1. Read first

1. root `AGENTS.md`;
2. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. this handoff;
4. `docs/TESTING_STRATEGY.md`;
5. `docs/UI_R5_REMAINING_NIGHT_SURFACE_AUDIT_2026-09-10.md`;
6. `docs/UI_R5_GENERIC_RESULT_REACHABILITY_AUDIT_2026-09-10.md`.

D6/R3 are closed; do not reopen them by default.

## 2. Live branch / final automated checkpoint

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
final logical T4 checkpoint: 2958f334fc7cccd59ed2e75a3bdaa60684492292
validated production tree below docs checkpoint: e84e01c2a8f5064814077d0583e684d2f42b09d7
```

PR #117 remains open, Draft and unmerged. Do not merge without explicit user authorization.

Final T4 evidence:

```text
CI #2143 / run 34435295215 PASS
- Classify changes                       PASS
- Android full unit tests + debug APK    PASS
- ASP contract tests                     PASS
- Real Clingo cross-validation           PASS
- CI gate                                PASS
R2 #2010 / run 34435295217 PASS
```

`[full-ci]` correctly selected full Android + debug APK. The FAST step was skipped intentionally because the full Android branch replaced it at this acceptance checkpoint.

## 3. Sage ownership extraction — complete

Final dependency direction:

```text
ClocktowerHostScreen
  - owns current Demon / Sage death trigger / effective ability state / card order
  - passes prepared facts only
        |
        v
ClocktowerSageStepMaterializer
  - owns Sage pair candidate projection
  - owns Sage recommendation/display metadata
  - owns presentationSubjectSeats
  - builds the Sage information step through ClocktowerInformationStepBuilder
        |
        v
ClocktowerNightStepUi / ClocktowerSageSquareTableUi
  - owns result projection and rendering
```

Preserved invariants:

- Host/session/canonical flow remain authoritative for whether Sage was actually killed by the Demon;
- Host remains authoritative for current Demon and effective death-trigger ability state;
- unreliable Sage choices remain `proposition = null`;
- typed `presentationSubjectSeats` remain presentation-only, not epistemic truth;
- localized display text is not parsed back into semantic seat identity;
- global `clocktowerInformationCandidateId(...)` remains unchanged.

Sage one-shot validation:

```text
run 34429902491 / job 102722989741 PASS
```

## 4. Legacy pair-manual retirement — complete

Production checkpoint:

```text
9e67fd53b21089a84c897e73ca9abb4c0f4ce17d
refactor: retire legacy pair manual dialog
```

Removed only:

- `ClocktowerPairManualSelectionDialog`;
- `ClocktowerPairManualCenterControls`;
- their now-unused Compose imports.

Retained shared pair helpers/model used by the accepted pair-information square table.

Validation:

```text
CI #2133 / run 34433654157 PASS
R2 #2000 / run 34433654190 PASS
```

## 5. Generic result reachability cleanup — complete

Architecture pre-flight and reachability proof:

`docs/UI_R5_GENERIC_RESULT_REACHABILITY_AUDIT_2026-09-10.md`

Pre-flight decision: **KEEP presentation routing in `ClocktowerNightStepUi`; add no new domain/session ownership.**

Production checkpoint:

```text
5cf762c81c333a52e7d744499ff1f1dd28e20c61
refactor: retire superseded generic result surfaces
```

Persistent production changes:

1. retire `nonPairResultFirstCandidates` and its superseded render block;
2. exclude Chambermaid from generic recommendation rendering;
3. exclude Chambermaid from generic unreliable-result rendering;
4. retain still-live generic direct/recommendation compatibility paths.

Focused ownership RED was real:

```text
ClocktowerNightStepResultSurfaceOwnershipTest
2 tests executed
2 expected assertion failures before production cleanup
```

The same focused test passed after production cleanup.

The first FAST run then exposed two obsolete source-shape assertions in Ravenkeeper/Undertaker wiring tests that required the now-retired generic branch. Only those two assertions were removed; the remaining specialized-owner and suppression guards remain.

Final one-shot validation:

```text
run 34434862249 / job 102737709222 PASS
- exact trigger/head/blob gate PASS
- focused ownership RED proof PASS
- exact production patch PASS
- production diff audit PASS
- focused ownership GREEN PASS
- Android FAST --rerun-tasks PASS
- production push PASS
- one-shot self-cleanup PASS
```

## 6. Final scope audit — PASS

Compared Sage completion head:

```text
053461b7d9868056593ca7a5e3ce2d8e68eb6330
```

to post-cleanup production tree:

```text
e84e01c2a8f5064814077d0583e684d2f42b09d7
```

Persistent production changes after Sage are exactly:

```text
ClocktowerPairManualSelectionUi.kt
ClocktowerNightStepUi.kt
```

Persistent test changes are exactly:

```text
+ ClocktowerNightStepResultSurfaceOwnershipTest.kt
~ ClocktowerRavenkeeperSquareTableWiringTest.kt   (-1 obsolete assertion)
~ ClocktowerUndertakerSquareTableWiringTest.kt    (-1 obsolete assertion)
```

Other persistent changes are documentation. Temporary one-shot workflow/script files are absent from the final tree.

The T4 checkpoint `2958f334...` differs from `e84e01c2...` only by this handoff document; no production source changed between the post-cleanup tree and final full validation.

## 7. Current acceptance state

Automated implementation/validation is complete.

Already user-device accepted:

```text
Washerwoman / Librarian / Investigator pair-information baseline
Chef square-table
```

Still device-pending:

```text
Empath
Undertaker
Ravenkeeper square-table UI
Spy Storyteller shell / Grimoire handoff
Clockmaker
Sage
wake-actor / compact navigation cross-role behavior where not already exercised
```

The dynamic Ravenkeeper-trigger defect also needs the original real-device scenario retested:

```text
Monk initially protects Ravenkeeper
-> go back
-> reconfirm Monk protecting another player
-> Demon kills Ravenkeeper
-> Ravenkeeper death-trigger ability must now appear
```

Do not claim these device-pending paths accepted until the user reports the relevant real-device result.

## 8. Scope fence

Preserve:

- `ClocktowerGameSession` writable authority;
- Planner/Reducer rule ownership;
- current recommendation semantics and ranking;
- revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- current Spy Grimoire reveal behavior;
- current dynamic Ravenkeeper-trigger correctness fix;
- New Demon identity specialized confirmation surface.

Do not broaden into EPI-MQ, recommendation-provider redesign, Persistence, unrelated gameplay changes, A4/ZDD feature work, generalized App-root rewrite, or renewed D6 decomposition.

## 9. Remaining execution sequence

```text
1. cross-role real-device acceptance / pending device retests
2. record concrete defects only if reproduced
3. apply focused tests-first fixes only for confirmed defects
4. if device acceptance is clean, synchronize closeout docs
5. UI-R5 closeout
6. merge PR #117 only with explicit user authorization
```
