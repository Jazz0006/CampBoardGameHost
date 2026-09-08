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

PR #112 is **merged / closed**. Persistence Simplification / PS5 is complete. Current `main` is a post-merge docs/CI descendant; no later production-architecture change supersedes the PS5 application state relevant to D6.

## Current priority

> **D6.0 and D6.1a are COMPLETE. D6.1b session-core compatibility RED/GREEN is NEXT.**

D6.0 re-audited the merged architecture rather than mechanically resuming the old decomposition plan. D6.1a then mapped the real production creation, recovery, revision, semantic-history, A4 and recommendation wiring before any production ownership write.

The highest-value remaining ownership defect is still that `CampBoardGameHostApp.kt` acts as writable production authority for substantial Clocktower identity/revision/history state while `ClocktowerGameSession` is only partially cut over.

D6.1a also found an important model mismatch that changes the implementation order: production No Greater Joy legitimately has no advanced `RulesetRef`, while `GameSnapshot` currently requires one; and App-root `gameStateRevision` cadence is broader than equality-based `ClocktowerGameSession.updateGameState()` semantics. Therefore D6 must first make the existing session core production-compatible before touching the giant App root.

Authoritative D6 docs:

- `docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`
- `docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`
- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md`

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
7. `ClocktowerGameSession` is the strongest existing architectural seam and remains the intended production session owner.

The desired dependency direction remains:

```text
Clocktower domain/session authority
-> recovery / recommendation / A4 projections and consumers
-> App root / Compose composition and presentation orchestration
```

## D6.1a production-wiring characterization — COMPLETE

D6.1a confirmed:

1. App root directly owns Clocktower game ID, seed, script/ruleset projection, both revisions, semantic-history mode, action timeline, observation list and global cursor.
2. `GameState` is currently derived from `cards` plus mechanical state rather than held as one canonical root object.
3. global action/observation transition semantics already delegate to stateless `ClocktowerGameSession` companion APIs, then copy results back into root state.
4. Recovery v2 stores identity/history plus cards/mechanics and reconstructs root state on restore; no live session owner is restored.
5. A4/recommendation/Judge use revisions as observable cache/request identity, so revision values and invalidation timing are behavioral contracts.
6. App-root `gameStateRevision` cadence is broader than `ClocktowerGameSession.updateGameState()` equality-based increment semantics; decomposition must preserve the existing cadence exactly.
7. `preflightClocktowerPublicAliveObservation()` is intentionally non-mutating and must remain so after live-session cutover.
8. No Greater Joy recovery intentionally succeeds with `rulesetRef = null`; D6 must not create a synthetic ref or a TB-only ownership model.
9. a plain mutable `ClocktowerGameSession.snapshot` is not Compose-observable by itself; App may keep a read-only observable projection, but not a second writable authority.

Detailed characterization:

`docs/D6_1A_PRODUCTION_WIRING_CHARACTERIZATION_2026-09-08.md`

## D6.1 — corrected implementation sequence

### D6.1b — NEXT: session-core production compatibility

Before touching `CampBoardGameHostApp.kt`, establish a typed pure-session contract proving the existing `ClocktowerGameSession` can represent common production identity/revision/history state for both Trouble Brewing and No Greater Joy without requiring a synthetic advanced `RulesetRef`.

Tests-first target:

1. NGJ production session create/restore works without synthetic `RulesetRef`.
2. game ID/script/seed, game/player revisions, semantic mode, action timeline, observation log and global cursor restore exactly.
3. explicit production game-state revision advancement preserves current cadence independently of `GameState` equality.
4. existing equality-based `updateGameState()` behavior remains available for operations where it is actually intended.
5. non-mutating global observation preflight remains possible.
6. when a real resolved `RulesetRef` exists, strict `GameSnapshot` projection retains existing seed/script/ruleset invariants.

Expected first-slice allowlist:

```text
app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt
app/src/test/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSessionTest.kt
possibly one new typed session production-contract test
D6 docs
```

Do not modify Recovery schema/planner/codec or the giant App root merely to make this RED/GREEN pass.

### D6.1c — App-root authority cutover: identity/revisions/chronology

After D6.1b is GREEN:

- create/restore/end exactly one Clocktower session owner;
- route identity, revisions, semantic mode, action timeline, observation log and cursor through it;
- preserve a one-way read-only Compose projection if required for recomposition;
- retire corresponding writable root duplicates atomically where practical;
- preserve A4 invalidation/durability ordering and non-mutating observation preflight.

### D6.1d — canonical `GameState` projection cutover

Only after revision/history authority is stable, route canonical `GameState` updates/projections through the session where behavior equivalence is proven. Do not rewrite all Clocktower mechanics in this slice.

### D6.1e — Recovery/A4/recommendation cleanup + acceptance

- Recovery reads session-owned identity/history while keeping v2 schema unchanged;
- recovery restore constructs the owner before dependent consumers run;
- A4 consumes strict session projection where a real ruleset ref is available;
- recommendation/Judge receive session-derived revisions;
- remove obsolete stateless production compatibility wiring only when no longer needed;
- run D6.1 acceptance.

### D6.1 non-goals

Do not mix this with:

- Recovery schema/version redesign or cross-version migration;
- fake/synthetic NGJ `RulesetRef` introduction;
- Trouble-Brewing-only session ownership;
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
meaningful typed T0 RED for uncovered stable session-production invariant
-> focused session GREEN
-> git diff --check + exact changed-file audit
-> :app:testFast at logical GREEN
-> affected T2 session/history + persistence/recovery integration when App wiring changes
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
