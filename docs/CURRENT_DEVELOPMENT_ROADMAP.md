# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> **Single current project-status and execution-priority authority.**

## Live context

```text
live main: 2c495ee547e8327b0d3c3a811f5c891ba34fd863
PS5 merge commit: 1b502c75357a2de7c928c88668e6a9613521b4ac
merged PR: #112 Persistence Simplification: recent emergency recovery
merged branch head: 18e7be6b915d654ea6bbfa6a11ff5627fe467153
latest PS5 production GREEN inside merged history: e622960a9c75c0b110d2c92e34b46828cb949e0a
renewed post-hotfix full-T4 checkpoint: 686b52a79cd4ea183c81884d34d54223fecae124
```

PR #112 is **merged / closed**. Persistence Simplification / PS5 is complete. Current `main` is a post-merge docs/CI descendant; no later production-architecture change supersedes the PS5 application state relevant to D6.0.

## Current priority

> **D6.0 post-persistence App/root responsibility audit is COMPLETE. D6.1a production-wiring characterization is NEXT.**

D6.0 re-audited the merged architecture rather than mechanically resuming the old decomposition plan. The audit found that Recovery, seating-first setup, recommendation coordination and A4 cache lifecycle already have concrete owners. The highest-value remaining ownership defect is that `CampBoardGameHostApp.kt` still acts as production authority for substantial Clocktower canonical session/history state even though `ClocktowerGameSession` already exists as the intended owner.

Authoritative D6.0 audit:

`docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`

Current D6 handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`

Historical PS5 completion record:

`docs/archive/checkpoints/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md`

## Persistence Simplification / PS5 — COMPLETE

```text
PS0 COMPLETE
PS1 COMPLETE
PS2 COMPLETE
PS3 COMPLETE
PS4 COMPLETE
PS5 COMPLETE
  PS5.0 COMPLETE
  PS5.1a COMPLETE
  PS5.1b COMPLETE
  PS5.1c COMPLETE
  PS5.2a lifecycle pause/stop duplicate-write reduction COMPLETE
  PS5.2b SideEffect ownership audit COMPLETE
  original final automated T4 COMPLETE
  recovery quick-restart hotfix COMPLETE
  renewed post-hotfix T4 COMPLETE
  real-device process-loss/restart acceptance COMPLETE
  PR #112 MERGED
```

Final Recovery contract remains frozen unless a future task deliberately reopens it:

- current-version-only, 4-hour emergency continuity;
- Archive remains a separate contract;
- unsupported/old Recovery fails closed;
- failed writes retain a future retry opportunity;
- A4 cannot release rebuild before persistence succeeds;
- restore the game, not raw transient UI state.

Final persistence trigger topology:

```text
SideEffect -> ordinary persist -> RecoveryWriteGate
ON_PAUSE  -> forced persist   -> RecoveryWriteGate
ON_STOP   -> ordinary persist -> RecoveryWriteGate
```

`SideEffect` is retained by explicit architecture decision. Do not reopen its removal without profiling evidence and a universal durable-mutation ownership design.

Historical Persistence Simplification audit:

`docs/archive/checkpoints/PERSISTENCE_REQUIREMENT_REDUCTION_AUDIT_2026-09-07.md`

## Final PS5 production / validation evidence

Latest production GREEN:

```text
e622960a9c75c0b110d2c92e34b46828cb949e0a
fix: restore seating owner after recovery
```

The final real-device-discovered bug was a Recovery ownership gap: a restored active game had valid cards/player identities/game kind but did not reconstruct `HostSeatingSetupFlow`. Recovery now rehydrates confirmed seating plus the selected active game while preserving the existing fail-closed `playerNamesFor()` production-start invariant.

Tests-first hotfix evidence:

```text
RED 3bc389349bba00a155d55ec01508d6b271125ff0
CI 34191093216 — expected FAIL
R2 34191093300 — PASS

helper GREEN d8e1123d1535672dc04271ab41e18ec979ccaa7f
product GREEN e622960a9c75c0b110d2c92e34b46828cb949e0a
one-shot 34191324182 — PASS
  focused HostSeatingRosterTest PASS
  :app:testFast PASS
  exact App-only product diff audit PASS
  git diff --check PASS
```

Renewed post-hotfix reserved T4:

```text
checkpoint 686b52a79cd4ea183c81884d34d54223fecae124
CI 34191738571 — PASS
  Android testFull + :app:assembleDebug PASS
  ASP contract/golden tests             PASS
  Real Clingo 5.8 cross-validation      PASS
  CI gate                               PASS
R2 34191738649 — PASS
```

Final pre-merge docs verification:

```text
CI 34192745929 — PASS
R2 34192745940 — PASS
```

## Real-device acceptance — COMPLETE

On 2026-09-08 the previously defined real-device acceptance tests **1–5 all passed**.

The additional regression path that exposed the recovery bug was also retested successfully after the hotfix:

```text
6-player No Greater Joy
-> process-loss / relaunch / Recovery restore
-> continue recovered game
-> Host Tools
-> same players / quick restart
-> new game starts without crash
```

## D6.0 decision — COMPLETE

D6.0 established the following ownership decisions:

1. `CampBoardGameHostApp.kt` remains approximately 238,759 bytes, but byte count is secondary to ownership quality.
2. `HostSeatingSetupFlow` is already the seating-first state owner; do not re-extract seating.
3. PS5 persistence/recovery owners remain authoritative; do not redesign Recovery as part of D6.
4. Archive remains separate from Recovery.
5. A4 remains a derived cache/durability consumer and must not become canonical session authority.
6. Existing recommendation/session coordinators remain owners; do not create a generic recommendation manager.
7. `ClocktowerGameSession` is the strongest existing architectural seam and should become production authority for the canonical state already represented by `GameSnapshot`.

The dependency direction selected by D6.0 is:

```text
Clocktower domain models
-> ClocktowerGameSession canonical state/history authority
-> recovery / recommendation / A4 consumers
-> App root / Compose composition and presentation orchestration
```

## D6.1 — selected next campaign

### D6.1a — NEXT: production-wiring characterization

Before changing production code:

1. Reconfirm live `main` and branch head.
2. Map every production creation/restore site for the Clocktower `GameSnapshot` subset.
3. Map all App-root writes to canonical `GameState`, game/player revisions, `ActionFactTimeline`, `EpistemicObservationLog`, semantic-history mode and global sequence.
4. Identify which existing `ClocktowerGameSession` and Recovery tests already protect the intended cutover.
5. Add a new typed RED only if there is a real uncovered production-wiring invariant.
6. Finalize the exact D6.1 production changed-file allowlist and atomic cutover anchors.
7. Stop for review before the first production ownership write if the required patch expands beyond the D6.0 contract.

### D6.1 target

Make a live/restored `ClocktowerGameSession` instance the production authority for the canonical state already modelled by `GameSnapshot`, then retire parallel App-root mutation authority for those fields.

### D6.1 non-goals

Do not mix this with:

- Recovery schema/version redesign or cross-version migration;
- Archive redesign;
- seating redesign;
- Werewolf removal/refactor;
- A4/ZDD production rollout;
- gameplay/rules semantic changes;
- all-game generic session framework;
- broad Compose/navigation redesign;
- a generic manager/controller that mirrors the App root callback surface.

## D6 validation cadence

D6 is architecture/ownership work, so use the current risk-based strategy:

```text
existing owning GREEN evidence
-> meaningful T0 RED only for a real uncovered stable invariant
-> production cutover
-> focused T0
-> git diff --check + exact ownership/diff audit
-> :app:testFast at logical GREEN
-> affected T2 session/history + persistence/recovery integration
-> R2 only when selected by main-thread/structural risk
-> one [full-ci] T4 at D6.1 logical acceptance
```

Real Clingo is not selected merely for structural ownership movement. Escalate only if exact epistemic semantics change, which D6.1 is not authorized to do.

## Current non-goals

Until a later D6 re-audit explicitly changes scope, do not mix D6 with new Recovery schema redesign, cross-version migration, Archive redesign, Werewolf removal, A4/ZDD feature rollout, unrelated UI work or DataStore modernization.

## Later priority after D6

```text
D6 ownership decomposition
-> UI-R5 real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```
