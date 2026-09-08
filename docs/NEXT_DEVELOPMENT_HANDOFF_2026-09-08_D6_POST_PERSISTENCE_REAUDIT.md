# Next Development Handoff — D6 Post-Persistence Re-audit

> Date: 2026-09-08 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`
> Audit baseline: live `main` `2c495ee547e8327b0d3c3a811f5c891ba34fd863`
> Persistence Simplification merge commit: `1b502c75357a2de7c928c88668e6a9613521b4ac`
> Status: **D6.0 AUDIT COMPLETE — D6.1a PRODUCTION-WIRING CHARACTERIZATION NEXT — NO D6 PRODUCTION EXTRACTION STARTED**

## Read first

Treat these as authority before continuing D6:

1. root `AGENTS.md`
2. `docs/TESTING_STRATEGY.md`
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`
4. `docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`
5. this handoff
6. `docs/archive/checkpoints/PS5_PERSISTENCE_TRIGGER_PROGRESS_2026-09-08.md` only when persistence-history context is needed

Before any write, re-confirm live `main` and the D6 branch head. Documentation/CI-only commits may exist after the PS5 merge commit above.

## Completed predecessor

Persistence Simplification / PS5 is fully complete:

- PR #112 merged / closed;
- automated acceptance complete;
- renewed post-hotfix full T4 complete;
- real-device acceptance tests 1–5 complete;
- recovered 6-player No Greater Joy -> quick restart crash fixed and retested successfully.

Latest PS5 production GREEN inside merged history:

```text
e622960a9c75c0b110d2c92e34b46828cb949e0a
fix: restore seating owner after recovery
```

Renewed post-hotfix T4:

```text
CI 34191738571 — PASS
R2 34191738649 — PASS
```

Final pre-merge docs checks:

```text
CI 34192745929 — PASS
R2 34192745940 — PASS
```

## D6.0 — COMPLETE

The post-persistence App/root responsibility audit is now recorded in:

`docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md`

D6.0 deliberately did **not** change production code.

### D6.0 primary finding

Do not create a new session manager/authority. The repository already contains the intended owner:

`app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt`

`ClocktowerGameSession` already owns `GameSnapshot` transitions, revisions, timeline allocation and global semantic-history commits. Its compatibility/stateless path explicitly exists for the production Compose adapter until full game state becomes session-owned.

The remaining high-value defect is therefore:

> `CampBoardGameHostApp.kt` still acts as the real production mutation authority for substantial Clocktower canonical session/history state while the intended `ClocktowerGameSession` owner remains only partially cut over.

### Existing boundaries confirmed by D6.0

These should **not** be reopened as first-order D6 extractions:

- Recovery: existing coordinator/lifecycle/write-gate/typed snapshot owners remain authoritative;
- seating-first setup: `HostSeatingSetupFlow` already owns confirmed seating + selected game and supports recovered active games;
- Archive: remains separate from Recovery;
- recommendation/setup coordination: existing coordinators remain owners;
- A4: remains a derived cache/durability/invalidation consumer, not canonical session authority;
- UI helper extraction: secondary to state/behavior ownership.

## Why the old D6 approach is superseded

Do not mechanically resume any old size-first D6 sequence. Persistence Simplification and the D6.0 audit clarified ownership boundaries that directly change decomposition decisions:

- Recovery is a current-version-only emergency continuity contract, not long-lived Save Game;
- Archive is separate from Recovery;
- `RecoveryWriteGate` owns semantic duplicate suppression / retry state;
- lifecycle persistence is `ON_PAUSE force=true`, `ON_STOP force=false`;
- Compose `SideEffect` remains the generic ordinary persistence opportunity by explicit architecture decision;
- A4 durability ordering must not be broken;
- Recovery restore reconstructs `HostSeatingSetupFlow` ownership for an active recovered game;
- session/restart/recovery ownership must remain consistent across Undercover, Werewolf and Clocktower;
- `ClocktowerGameSession` already supplies the strongest intended Clocktower session boundary.

Any D6 extraction that moves state, callbacks, persistence/recovery wiring or session ownership must preserve those contracts.

## Current D6 goal

Complete ownership-first decomposition of the merged application architecture without changing user-visible behavior.

The goal is not merely “make files smaller”. The plan should reduce coupling, establish explicit state authority, improve dependency direction and make future AI/human edits cheaper without creating cross-file hidden state or callback spaghetti.

## Selected next campaign — D6.1

### D6.1 title

**`ClocktowerGameSession` Production Authority Cutover — canonical snapshot/history state**

### D6.1 target

Make one live/restored `ClocktowerGameSession` instance the production authority for the canonical state already represented by `GameSnapshot`, instead of maintaining parallel App-root authority and calling only stateless session helpers.

Initial authority scope should be limited to the existing `GameSnapshot` contract where production ownership is already clear:

- canonical `GameState`;
- `gameStateRevision`;
- `playerInputRevision`;
- `ActionFactTimeline`;
- `EpistemicObservationLog`;
- semantic-history mode;
- next global timeline sequence;
- existing game identity / seed / ruleset invariants.

Do not expand `GameSnapshot` merely to reduce App-file size.

## D6.1a — NEXT: production-wiring characterization

**Still no production extraction in D6.1a.**

Before the first production write:

1. Reconfirm live `main` and branch head.
2. Map every production creation/start/restore site for the Clocktower `GameSnapshot` subset.
3. Map all App-root writes to canonical `GameState`, revisions, action timeline, epistemic observation log, semantic-history mode and global sequence.
4. Distinguish durable semantic timeline from UI-only event projection; do not conflate them.
5. Map Recovery build/apply reads/writes for the same canonical subset.
6. Map A4/recommendation consumers that read this state and record ordering constraints.
7. Identify which existing session/recovery tests already protect the intended behavior.
8. Add a typed RED only if a real stable production-wiring invariant is uncovered.
9. Finalize the exact D6.1 changed-file allowlist and fail-closed large-file patch anchors.
10. Stop for architecture review if safe cutover requires broader semantics/schema changes than D6.0 authorizes.

## D6.1 invariants

The implementation must preserve all of these:

1. same Clocktower game identity / seed / script / ruleset authority;
2. no lost or double game/player revision increments;
3. monotonic collision-free global semantic-history sequence;
4. action facts and player-visible observations committed exactly once under existing idempotency rules;
5. no storyteller-hidden action target newly leaks into durable player-visible history;
6. Recovery remains current-version-only and schema-compatible;
7. restored Clocktower session owner exists before dependent consumers read canonical state;
8. `RecoveryWriteGate`, pause/stop forcing rules and ordinary `SideEffect` persistence opportunity remain unchanged;
9. A4 durability/invalidation/prewarm ordering remains unchanged;
10. restart/end does not leak stale Clocktower session state across games;
11. Undercover/Werewolf behavior remains untouched;
12. user-visible behavior remains unchanged.

## Tests / validation

D6.1 is primarily a behavior-preserving ownership refactor. Existing typed evidence counts as test-first evidence.

Current session tests include:

- `ClocktowerGameSessionTest`
- `ClocktowerGlobalObservationCommitTest`
- `ClocktowerHistoricalActionObservationCaptureTest`
- `ClocktowerTimelineSequenceAllocatorTest`
- `ClocktowerNightCheckpointTest`
- `ClocktowerRecommendationCoordinatorTest`

Persistence/recovery tests from PS1–PS5 remain the owner of recovery semantics.

Validation cadence:

```text
identify existing owning GREEN evidence
-> meaningful T0 RED only if a real production-wiring coverage gap exists
-> structural production cutover
-> focused T0 session + recovery/wiring evidence
-> git diff --check + exact ownership/diff audit
-> :app:testFast at logical GREEN
-> affected T2 session/history + persistence/recovery integration
-> R2 only when selected by changed main-thread/structural boundary
-> one [full-ci] T4 at D6.1 logical acceptance
```

Real Clingo is not required for ownership movement alone. Escalate only if exact epistemic semantics change; such semantic change is outside D6.1 scope.

## Expected D6.1 scope

Final allowlist is established in D6.1a, but expected files are narrow:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt   # only if a small durable owner API addition is truly needed
one or a small number of typed session/recovery integration tests
docs/D6_* progress/checkpoint document(s)
docs/CURRENT_DEVELOPMENT_ROADMAP.md
```

Because `CampBoardGameHostApp.kt` is large/truncated through connector reads, obey current `AGENTS.md` writer priority: use a branch/head/blob-locked one-shot GitHub Actions + separate Python patch as the first fallback for a stable localized App-root patch; use local-worktree/Codex only when that approach genuinely cannot be made safe.

## Provisional post-D6.1 order

Re-audit after each completed ownership cutover.

```text
D6.1 canonical ClocktowerGameSession production authority
-> D6.2 cohesive Clocktower durable day/night mutation boundaries
-> D6.3 shrink giant ClocktowerJudgeScreen state/callback surface
-> D6.4 simplify root Recovery composition after per-game authority moves down
-> D6.5 re-audit remaining root responsibilities / file size / callback fan-out
```

This order is provisional beyond D6.1; do not turn it into a rigid multi-month plan without re-audit evidence.

## Guardrails carried forward

- Reconfirm remote/live branch head before every write sequence.
- Behavior changes require tests-first RED.
- Structural extraction should preserve exact behavior; existing typed characterization is preferred where it already covers the contract.
- Run focused tests first, then `:app:testFast` at logical GREEN checkpoints.
- Use R2 for main-thread / structural boundary validation where applicable.
- Reserve full T4 for campaign/slice acceptance according to `docs/TESTING_STRATEGY.md` rather than every tiny edit.
- Run `git diff --check` and exact changed-file/reference audits.
- Treat giant-file edits fail-closed.
- Do not mix D6 with Recovery redesign, cross-version migration, Archive redesign, Werewolf removal, A4/ZDD feature rollout, unrelated UI changes or DataStore modernization.
- Do not create a new generic `Manager`/`Controller` that merely mirrors the giant App callback surface.
- Do not commit an intermediate state with two writable canonical Clocktower session authorities.

## Suggested next-conversation prompt

```text
请读取根目录 AGENTS.md、docs/TESTING_STRATEGY.md、docs/CURRENT_DEVELOPMENT_ROADMAP.md、docs/D6_0_APP_ROOT_RESPONSIBILITY_AUDIT_2026-09-08.md 和 docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-08_D6_POST_PERSISTENCE_REAUDIT.md。

先重新确认 live main 和 codex/d6-root-reaudit 当前 head。D6.0 已完成，尚未开始 production extraction。

从 D6.1a 开始：完整映射 CampBoardGameHostApp.kt 中 GameSnapshot 已覆盖的 canonical Clocktower state 的 creation/start/restore/read/write 路径，以及 Recovery/A4/recommendation consumers。确认现有 ClocktowerGameSession/recovery 测试是否已经保护 production cutover；只有存在真实稳定覆盖缺口时才建立 typed RED。最后给出 D6.1 的 exact production allowlist、atomic cutover anchors、验证命令和 stop conditions。先不要修改 production code。
```
