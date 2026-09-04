# UI-R4D-3 Day Storyteller Workspace — Development Handoff

> Date: 2026-09-03 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Stack: Draft PR #78 -> Draft PR #79 -> UI-R4D-3

## 1. Entry gate

UI-R4D-2F / F7 is complete and accepted on a real Android device.

Final validated product checkpoint before device acceptance:

`40b604eae7ea489347357f88fd3d07be83ce5a78`

F7.6 field APK workflow:

`33722208538`

The field APK was built from that product checkpoint and the user confirmed the F7 real-device acceptance checks pass. Later #79 commits are CI/build cleanup only and have no permanent file diff from the product checkpoint.

Do not merge PR #78 or #79 without explicit authorization.

## 2. Active target

Start **UI-R4D-3.1 Day Overview** only.

Goal:

Move the existing Day overview/dashboard and its primary phase controls into the shared persistent Host table center workspace while preserving all existing Day state and action legality.

Current production Day state already exists in `ClocktowerHostScreen.kt` and uses typed `ClocktowerDayMode` plus existing game/history transitions. R4D-3.1 is a UI ownership/migration slice, not a new Day rules engine.

## 3. R4D-3.1 required behavior

Preserve:

- the same confirmed physical seating / typed `ClocktowerSeatId` authority established by R4D-2;
- the same shared `HostTableShell` / stable slot ring used by Setup and Host surfaces;
- `ClocktowerDayMode.Overview` semantics;
- the existing transition into nomination mode;
- the existing End Day behavior, including durable Day-ended history and transition to Night;
- existing Slayer availability and legality semantics;
- existing nomination/vote state and callbacks without redesigning them in this slice;
- existing alive/dead truth and seat identity.

Target presentation boundary:

```text
Clocktower Day state / callbacks
        |
        v
small Day Overview center presentation/control owner
        |
        v
shared persistent Host table center slot
        |
        v
same stable seats around the table
```

`ClocktowerHostScreen.kt` remains orchestration. Do not make the protected large screen the long-term implementation home for Day center visual policy.

## 4. Scope exclusions

Do **not** expand R4D-3.1 into:

- R4D-3.2 / Public Claim durable state or claim editing;
- R4D-3.3 / nomination-vote-execute state-machine redesign or table migration;
- R4D-3.4 / broader alive/dead Day-anchor integration;
- recommendation ranking or EPI-MQ;
- Mayor redirect, Imp succession, A4/ZDD, or unrelated rules work;
- changes to F4/F5 Player Reveal privacy/source convergence;
- seating geometry/reorder changes already accepted in F7.

## 5. Testing policy

R4D-3.1 is medium risk because it moves Day controls across UI ownership boundaries while preserving state semantics.

Prefer meaningful behavior contracts for any newly extracted Day Overview transition/presentation policy. Do not create source-string or pixel-shape RED tests.

At the executable checkpoint run:

```text
focused Day/Host-table behavior tests
-> relevant 5 / 8 / 12 / 15 table/layout contracts
-> :app:testFast
-> exact changed-file audit
-> git diff --check
```

If the implementation is only a mechanical extraction with no new decision logic, existing behavioral contracts plus a focused pure transition/presentation contract are sufficient; do not invent ceremonial tests.

## 6. Initial audit findings

- `ClocktowerHostScreen.kt` already owns `dayMode` state and inline Day Overview / nomination branches.
- existing Day Overview includes the current highest-vote context, entry into nomination mode, and End Day transition.
- the shared table owner is `HostTableShell` in `ClocktowerHostTableUi.kt`; it already accepts bounded `centerContent` while preserving the canonical physical seat topology.
- therefore R4D-3.1 should extract/migrate the Day Overview center content rather than introduce another table or another seat mapping.

## 7. Stop condition

Stop R4D-3.1 after Day Overview is migrated, focused behavior is green, `:app:testFast` is green, and the exact diff is clean.

Do not continue automatically into Public Claim or Nomination/Vote migration in the same implementation slice.
