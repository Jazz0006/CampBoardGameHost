# UI-R5 Remaining Night Surface Audit — 2026-09-10

> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117  
> Status: **SAGE IMPLEMENTED / LEGACY RETIREMENT NEXT**

## 1. Audit conclusion

The surviving UI-R5 production work is no longer another role migration. Sage is implemented and automated-green. Remaining work is legacy retirement, generic-result reachability cleanup, final T4, and real-device acceptance.

```text
IMPLEMENTED / ACCEPTED SQUARE TABLE:
- Clockmaker
- Sage
- Undertaker
- Ravenkeeper
- Spy Storyteller shell
- Chef
- Empath
- Chambermaid
- Fortune Teller
- shared single-target interactions
- shared night-ruling interactions
- Washerwoman / Librarian / Investigator pair-information surface

KEEP SPECIALIZED:
- first-night Minion information
- first-night Demon information
- New Demon identity reveal/confirmation

UNREACHABLE LEGACY — RETIRE NEXT:
- ClocktowerPairManualSelectionDialog
- ClocktowerPairManualCenterControls

RETAIN / REHOME AS NEEDED:
- clocktowerPairManualSquareTableSeat(...)
- clocktowerPairManualSeatState(...)
- ClocktowerPairManualSelectionModel
```

## 2. Classification principle

UI-R5 targets one coherent **Storyteller operation layer**, not a mechanical rule that every player-facing reveal must become a table.

Use the square table when the Storyteller must identify a current actor, select/understand player seats, or interpret information spatially. Keep specialized reveal/confirmation pages when the operation is fundamentally a private information handoff and table geometry adds little semantic value.

The Spy and Sage implementations preserve that split: square-table Storyteller presentation may coexist with the existing specialized player-facing reveal path.

## 3. First-night inventory

| Interaction | Classification | Current action |
|---|---|---|
| Minion information | KEEP SPECIALIZED | no migration |
| Demon information | KEEP SPECIALIZED | no migration |
| Poisoner | ACCEPTED SQUARE TABLE | complete |
| Fortune Teller red herring setup | ACCEPTED SQUARE TABLE | complete |
| Clockmaker | ACCEPTED SQUARE TABLE | automated green / device pending |
| Washerwoman | ACCEPTED SQUARE TABLE | complete |
| Librarian | ACCEPTED SQUARE TABLE | complete |
| Investigator | ACCEPTED SQUARE TABLE | complete |
| Chef | ACCEPTED SQUARE TABLE | device accepted |
| Empath | ACCEPTED SQUARE TABLE | automated green / device pending |
| Chambermaid | ACCEPTED SQUARE TABLE | complete |
| Fortune Teller | ACCEPTED SQUARE TABLE | complete |
| Butler | ACCEPTED SQUARE TABLE | complete |
| Spy | ACCEPTED SQUARE TABLE | automated green / device pending |

## 4. Other-night inventory

| Interaction | Classification | Current action |
|---|---|---|
| Poisoner | ACCEPTED SQUARE TABLE | complete |
| Butler | ACCEPTED SQUARE TABLE | complete |
| Empath | ACCEPTED SQUARE TABLE | automated green / device pending |
| Chambermaid | ACCEPTED SQUARE TABLE | complete |
| Fortune Teller | ACCEPTED SQUARE TABLE | complete |
| Undertaker | ACCEPTED SQUARE TABLE | automated green / device pending |
| Monk | ACCEPTED SQUARE TABLE | complete |
| New Demon identity event | KEEP SPECIALIZED | retain current private confirmation |
| Imp / Demon kill | ACCEPTED SQUARE TABLE | complete |
| Demon successor event | ACCEPTED SQUARE TABLE | complete |
| Mayor redirect event | ACCEPTED SQUARE TABLE | complete |
| Sage | ACCEPTED SQUARE TABLE | automated green / device pending |
| Ravenkeeper | ACCEPTED SQUARE TABLE | automated green / device pending |
| Spy | ACCEPTED SQUARE TABLE | automated green / device pending |

## 5. Clockmaker checkpoint

Clockmaker uses a dedicated read-only square-table Storyteller surface while preserving the existing player reveal path.

```text
production implementation: ae48eecc6c0931ee608cc1402c6355cc807f8e81
verified head:             9119ec83f036432ec9b5f0f3a920690ab9719e16
CI #2106 / run 34417802131 PASS
R2 #1973 / run 34417802113 PASS
```

Real-device acceptance remains pending.

## 6. Sage architecture and implementation

### Recorded pre-flight decision

```text
Architecture pre-flight:
- current owner:
  ClocktowerHostScreen owned prepared Sage lifecycle facts plus Sage-specific pair
  recommendation/materialization; ClocktowerInformationStepBuilder owned generic information
  mechanics; ClocktowerSageSquareTableUi owned Sage presentation.
- proposed responsibility:
  Sage-specific pair candidate/recommendation projection, typed presentationSubjectSeats,
  localized Sage display metadata, and Sage NightStep materialization.
- authoritative state owner(s):
  Host/session/canonical night flow remain authoritative for current Demon identity,
  whether Sage died to the Demon, effective death-trigger ability state, card/seat order,
  and interaction ordering.
- narrow typed input/output seam:
  prepared Sage trigger actor + current Demon + resolved direct pair + effective ability state
  + roster and narrow recommendation/localization callbacks
  -> ClocktowerDisplayOption / ClocktowerNightStepUi.
- keep in current owner / extract:
  EXTRACT to ClocktowerSageStepMaterializer.
- reason:
  candidate construction, pair presentation identity, and Sage step metadata form one cohesive
  role-specific responsibility. Keeping them in protected Host would increase context radius.
```

### Final ownership

`ClocktowerSageStepMaterializer.kt` now owns:

- Sage pair candidate construction;
- Sage recommendation projection;
- Sage presentation-only subject-seat identity;
- Sage display metadata;
- Sage NightStep materialization through `ClocktowerInformationStepBuilder`.

`ClocktowerHostScreen.kt` retains:

- `sageNightDeath` authority;
- current Demon authority;
- `sageDeathTriggerAbilityState` authority;
- card order / lifecycle facts;
- interaction registration and orchestration.

The old Host-local `recommendedSageOptions(...)` function is removed. Host contains one narrow delegation to `clocktowerSageStepMaterializer(...)`.

### Preserved Sage invariants

- direct reliable Sage carries exactly two typed presentation seats;
- each reliable or impaired Sage option carries exactly two typed seats;
- player-display resolution preserves those seats;
- pair reveal uses typed seats and never parses `displaySecondary`;
- dead Sage actor highlight remains independent from the two information-seat highlights;
- automatic mode uses only the already-selected automatic option;
- manual mode remains inside the existing recommendation/candidate domain;
- missing, duplicate, malformed, or out-of-range seats fail closed;
- impaired Sage options remain `proposition = null`;
- no misinformation is promoted into an `InformationProposition` merely for UI;
- global `clocktowerInformationCandidateId(...)` is unchanged.

### Sage validation

Persistent production/test slice from post-preflight clean head `7ca02b9c...` to cleanup head `053461b7...` contains only:

- added `ClocktowerSageStepMaterializer.kt`;
- modified `ClocktowerHostScreen.kt`;
- added `ClocktowerSageStepMaterializerTest.kt`;
- modified `ClocktowerSageSquareTableWiringTest.kt`.

One-shot run `34429902491` / job `102722989741`:

```text
focused Sage testDebugUnitTest --rerun-tasks  PASS
Android :app:testFast --rerun-tasks            PASS
production changed-file allowlist              PASS
git diff --check                               PASS
Host ownership assertions                      PASS
one-shot cleanup                               PASS
```

The cleanup commit was authored by `github-actions[bot]`, so its automatically created normal CI/R2 runs had `action_required` with zero jobs. A connector-authored docs-only checkpoint was then created without changing the production tree:

```text
checkpoint head: 356d211ea680fa77201684853f6140eeb4218560
CI #2131 / run 34430900223  PASS
R2 #1998 / run 34430900336 PASS
```

CI #2131 correctly classified the latest commit as docs-only and skipped Android/ASP/Clingo. The Android production-tree evidence is the explicitly executed one-shot FAST run above.

Status: **SAGE IMPLEMENTED / AUTOMATED GREEN / DEVICE PENDING**.

## 7. New Demon identity remains specialized

`ClocktowerCharacterInteractionRegistry` emits the New Demon identity event immediately before the current Imp interaction when succession produced a new Demon.

Its purpose is private successor identity confirmation before that player acts. It is not dead duplication and remains specialized unless a later usability defect justifies redesign.

## 8. Legacy pair-manual UI retirement — next

`ClocktowerPairManualSelectionDialog` is no longer called by current NightStep UI. `ClocktowerPairManualCenterControls` is part of the same unreachable legacy path.

Do **not** delete the whole pair-manual support file blindly. The accepted pair-information square table still consumes shared seat helpers/model.

Next slice should remove only the unreachable dialog/center controls while retaining or relocating:

- `clocktowerPairManualSquareTableSeat(...)`;
- `clocktowerPairManualSeatState(...)`;
- `ClocktowerPairManualSelectionModel`;
- still-valid typed tests.

Before production editing, apply the root `AGENTS.md` recorded architecture-preflight gate if the edit touches a protected/core or >1000 LOC handwritten production source and moves/adds responsibility.

## 9. Generic-result reachability audit after retirement

After pair-manual retirement, re-audit the generic recommendation/result-first/unreliable/direct result blocks in `ClocktowerNightStepUi`.

Delete only paths proven unreachable or superseded by accepted specialized owners. Do not use byte-count reduction as the justification, and do not reopen unrelated decomposition.

## 10. Remaining execution order

```text
1. retire unreachable pair-manual dialog / center controls
2. re-audit generic recommendation/result-first/unreliable/direct blocks
3. remove only proven dead branches
4. exact final scope/diff audit
5. final logical UI-R5 T4
6. cross-role real-device acceptance
7. PR #117 closeout / merge only with explicit user authorization
```

## 11. Current checkpoints

```text
latest full-T4 checkpoint:
c1ab5578a2fb64e6506d27b898df9f4bbb1388fc

Ravenkeeper per-role checkpoint:
06e01ec7c0410412b38d104f4a5f72bc72811ffe
CI #2087 PASS / R2 #1954 PASS

Spy per-role checkpoint:
d45f96ddcdb1142a422d64ee87cf61c5475121f9
CI #2092 PASS / R2 #1959 PASS

Clockmaker verified head:
9119ec83f036432ec9b5f0f3a920690ab9719e16
CI #2106 PASS / R2 #1973 PASS

Sage automated checkpoint:
356d211ea680fa77201684853f6140eeb4218560
CI #2131 PASS / R2 #1998 PASS
```

Real-device acceptance remains pending for Empath, Undertaker, Ravenkeeper, Spy, Clockmaker, Sage, and the previous dynamic Ravenkeeper-trigger reproduction unless explicitly reported otherwise by the user.
