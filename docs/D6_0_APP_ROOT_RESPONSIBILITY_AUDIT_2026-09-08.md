# D6.0 — Post-PS5 App Root Responsibility Re-audit

> Date: 2026-09-08 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Audit baseline: live `main` `2c495ee547e8327b0d3c3a811f5c891ba34fd863`  
> Working branch: `codex/d6-root-reaudit`  
> Status: **D6.0 AUDIT COMPLETE — PRODUCTION CODE UNCHANGED — D6.1 PROPOSED, NOT STARTED**

## 1. Executive decision

D6 should **not** continue as another file-size-driven extraction campaign.

The post-PS5 architecture shows that several responsibilities previously worth extracting already have concrete owners:

- Recovery: `RecoveryApplicationCoordinator`, `RecoveryLifecyclePersistence`, `RecoveryWriteGate`, typed `RecoverySnapshot` / restore planner;
- seating-first setup: `HostSeatingSetupFlow`, `HostSeatingRoster` and related UI/navigation helpers;
- Clocktower recommendation / night-resolution behavior: multiple dedicated coordinators, planners and authorities already exist;
- A4: dedicated cache / durability / invalidation / prewarm components already exist and should remain derived consumers;
- Archive is explicitly separate from Recovery and should remain so.

The highest-value remaining ownership defect is different:

> `CampBoardGameHostApp.kt` still acts as the production owner of substantial Clocktower durable/session state while `ClocktowerGameSession` already exists as the intended domain/session owner.

`ClocktowerGameSession` already owns `GameSnapshot` transitions, revisions and global semantic-history commits. Its source explicitly describes the current stateless transition functions as an adapter used **until the full game state is session-owned**. D6 should therefore complete that existing architecture rather than invent a parallel `Manager`, `Controller` or `StateAuthority`.

**Recommended first production slice: D6.1 — `ClocktowerGameSession` production authority cutover for the canonical session/snapshot/history state it already models.**

Do not start D6.1 until this audit/plan is reviewed and accepted.

---

## 2. Current App root facts

`CampBoardGameHostApp.kt` is currently approximately **238,759 bytes**.

That size is still a meaningful AI/human editing-cost signal, but the audit does not treat byte count as the extraction boundary. The file remains large because the root combines several kinds of responsibility:

1. Android / Compose application shell and screen routing;
2. app preferences and common-player settings;
3. generic game start/restart/recovery orchestration;
4. Undercover / Werewolf / Clocktower runtime wiring;
5. recovery snapshot construction and application adapters;
6. Clocktower canonical state and semantic-history mutations;
7. Clocktower day/night/storyteller orchestration callbacks;
8. recommendation / setup coordination wiring;
9. A4 cache/prewarm/invalidation wiring;
10. presentation callbacks into already-extracted screens and interaction components.

The architectural problem is therefore **mixed authority**, not simply excess declarations.

---

## 3. Responsibility map

| Responsibility cluster | State currently visible/owned at App root | Mutation / entry points | Side effects / dependencies | Existing owner or best target | D6 disposition |
|---|---|---|---|---|---|
| App shell / routing / locale | current screen, language, common-player UI state, routing state | settings, navigation, back handling | Compose, Activity/lifecycle, SharedPreferences | App/root shell | **Retain at root** unless later UI-only cleanup is clearly cohesive |
| Generic game lifecycle | selected game, active-game routing, start/restart/end flow | game start, quick restart, return/finish | all game types, router, recovery | App orchestration | Retain orchestration; reduce captured per-game state as owners mature |
| Recovery persistence | save opportunity, lifecycle flush, preview/restore coordination | SideEffect, pause/stop, recovery selection | recovery repository, typed snapshots, lifecycle | existing persistence owners | **Do not re-extract Recovery**; root remains adapter/composition layer |
| Seating-first setup | confirmed seating + selected game flow | confirm seats, choose game, recover active game | game-independent setup flow | `HostSeatingSetupFlow` | **Already owned; drop any seating-first D6 extraction** |
| Archive/history persistence | archive load/write adapter and review projection | game completion / review | archive JSON/repository, prefs | existing Archive boundary | Keep separate from Recovery; later adapter cleanup only if useful |
| Clocktower canonical game/session state | canonical `GameState` projection plus session revision/snapshot-related fields | setup/start, day/night transitions, recovery | Clocktower domain/session, recovery, recommendation | **existing `ClocktowerGameSession`** | **D6.1 highest priority** |
| Clocktower semantic chronology | action facts, epistemic observations, global sequence/revision-related transitions | observation/action commits, timeline transitions | `GameSnapshot`, epistemic model, recovery, A4 consumers | **existing `ClocktowerGameSession`** | **D6.1 highest priority** |
| Clocktower host/day/night orchestration | poison/protection/death/role-change/nomination/vote/night-resolution callback wiring | `ClocktowerJudgeScreen` and night-step callbacks | many existing rule/session planners and authorities | session-domain transitions + app presentation adapter | Later D6 after canonical authority cutover |
| Recommendation/setup orchestration | request/build/prewarm/reveal/selection wiring | setup and information actions | recommendation coordinators, setup services | existing Clocktower coordinators | Do not create another recommendation manager; simplify consumers later |
| A4 cache / prewarm / invalidation | derived cache lifecycle inputs and effect wiring | session/history changes, identity reveal | A4 cache, durability gate, coroutine/lifecycle | existing A4 components | **Keep derived consumer**; do not make A4 first owner |
| Clocktower presentation state | transient selection/dialog/table interaction state | UI gestures and display callbacks | Compose UI | already-extracted UI/state files where appropriate | Secondary; only extract when state is truly presentation-only |
| Other game runtimes | Undercover / Werewolf runtime state and screens | game-specific actions | their modules + root router | existing game modules / app orchestration | Out of D6 first slices; no removals/redesign |

---

## 4. Key ownership evidence

### 4.1 `ClocktowerGameSession` is already the intended owner

`ClocktowerGameSession` is not an experimental duplicate. It already provides:

- a canonical `snapshot: GameSnapshot`;
- `updateGameState(...)`;
- `recordPlayerInput(...)`;
- monotonic timeline allocation;
- atomic global `ActionFact` commit;
- atomic global epistemic-observation commit;
- legacy observation recording;
- completed-game signature/history operations;
- `create(...)` and `restore(snapshot)` entry points.

Its compatibility functions explicitly exist for the production Compose adapter **until the full game state is session-owned**.

Therefore D6 must complete this existing boundary, not add a parallel `ClocktowerSessionStateAuthority`, generic manager, or callback mirror of the App root.

### 4.2 `GameSnapshot` already defines the canonical session subset

`GameSnapshot` already contains the state that should be treated as one coherent session contract:

- game identity / seed / ruleset;
- `GameState`;
- game-state and player-input revisions;
- decision and cross-game history;
- `ActionFactTimeline`;
- `EpistemicObservationLog`;
- semantic-history mode;
- next global timeline sequence.

Its constructor enforces seed/script and semantic-history compatibility invariants.

D6.1 should first transfer **production authority for this already-modelled subset**. It must not expand the snapshot schema merely to make the App file smaller.

### 4.3 Root currently remains the real mutation authority

The App root still:

- stores many Clocktower state values as Compose/local mutable state;
- constructs semantic/action/observation transitions from those locals;
- calls `ClocktowerGameSession` stateless companion transitions instead of consistently mutating a live session instance;
- constructs recovery snapshots by collecting many captured App locals;
- restores those locals and then rebuilds projections/cache state;
- passes a large state + callback surface into `ClocktowerJudgeScreen`;
- directly coordinates poison, protection, death, role transformation, executions, observations and timeline changes.

That means the current dependency shape is partially inverted:

```text
App root owns Clocktower state
-> calls session/domain helpers
-> calls recovery/A4/recommendation consumers
```

The desired direction is:

```text
Clocktower domain models
        |
        v
ClocktowerGameSession   <--- canonical durable/session authority
   |        |       |
   |        |       +--> recommendation / flow consumers
   |        +----------> A4 derived cache consumer
   +-------------------> recovery adapter reads/restores explicit snapshot
        |
        v
App root / Compose = composition, routing, lifecycle and presentation orchestration
```

`ClocktowerGameSession` must remain Android/Compose-independent and must not depend on the recovery repository or UI.

### 4.4 Seating and Recovery are not missing owners

`HostSeatingSetupFlow` already explicitly defines a small game-independent state boundary and includes `recoveredActiveGame(...)` for recovery reconstruction.

The persistence package already owns lifecycle persistence, restore planning/application, write-gating and typed snapshots.

So D6 should **consume those existing boundaries**, not reopen them.

---

## 5. Candidate ranking

Scoring below separates architectural value from execution risk. 5 = highest.

| Candidate | Cohesion | Root dependency reduction | Durable testability | Existing owner readiness | Execution risk | Priority |
|---|---:|---:|---:|---:|---:|---|
| Complete `ClocktowerGameSession` canonical production authority | 5 | 5 | 5 | 5 | 4 | **1 — D6.1** |
| Move additional Clocktower day/night mutations behind typed session-domain transitions | 5 | 5 | 4 | 4 | 5 | **2 — after D6.1** |
| Shrink `ClocktowerJudgeScreen` giant callback/state surface | 4 | 5 | 4 | 3 | 4 | **3 — after state authority is stable** |
| Simplify root recovery adapter after session cutover | 4 | 3 | 4 | 5 | 4 | **4** |
| Further A4 ownership extraction | 4 | 2 | 4 | 5 | 4 | **Defer; A4 should consume session state** |
| Seating extraction | 5 | 1 | 5 | 5 | 1 | **Drop — already owned** |
| Recovery extraction/redesign | 5 | 1 | 5 | 5 | 5 | **Drop — PS5 already established owner/contract** |
| More UI helper/file extraction | 3 | 2 | 3 | 4 | 2 | **Secondary only** |

---

## 6. D6.1 — recommended first production slice

### Name

**D6.1 — `ClocktowerGameSession` Production Authority Cutover: canonical snapshot/history state**

### Goal

Make one live/restored `ClocktowerGameSession` instance the production authority for the state that is **already represented by `GameSnapshot`**, rather than maintaining parallel App-root authority and invoking only stateless session helpers.

### Explicit in-scope authority

At minimum, the implementation audit should target the existing `GameSnapshot` contract:

- canonical `GameState`;
- `gameStateRevision`;
- `playerInputRevision`;
- `ActionFactTimeline`;
- `EpistemicObservationLog`;
- `semanticHistoryMode`;
- `nextTimelineGlobalSequence`;
- identity/seed/ruleset consistency already required by `GameSnapshot`.

Decision/cross-game history may remain as currently wired if moving them would broaden the first cutover; they are already part of `GameSnapshot` but should only move where the production authority is unambiguous.

### Explicitly not in D6.1

- do not move every Clocktower UI/transient field into `GameSnapshot`;
- do not add persistence schema migration;
- do not change gameplay/rules semantics;
- do not redesign Recovery or Archive;
- do not roll A4/ZDD into production;
- do not introduce a new generic session framework for all games;
- do not create a broad `Manager`/`Controller` whose constructor mirrors dozens of App callbacks;
- do not rewrite all day/night handlers in one slice;
- do not use App-file byte reduction as acceptance evidence by itself.

### Required invariants

1. The same Clocktower game seed/script/ruleset identity remains authoritative before and after cutover.
2. Every canonical state mutation increments the same revision(s) as before; no double increment and no lost increment.
3. Global semantic-history sequence remains monotonic and collision-free.
4. Action facts and player-visible observations remain committed exactly once under the same existing idempotency rules.
5. No actual storyteller-hidden target is newly exposed to durable player-visible history.
6. Recovery round-trip behavior remains current-version-only and schema-compatible; no cross-version migration is introduced.
7. Recovery restore reconstructs the live session owner before consumers rely on canonical Clocktower state.
8. `RecoveryWriteGate`, `ON_PAUSE force=true`, `ON_STOP force=false`, and ordinary Compose `SideEffect` persistence opportunity remain unchanged.
9. A4 durability/invalidation/prewarm ordering remains unchanged; A4 reads derived state and does not become canonical owner.
10. Game restart/end destroys/replaces the Clocktower session at the same logical boundary as today; no stale session state leaks across games.
11. Undercover and Werewolf recovery/start/restart behavior remains untouched.
12. User-visible UI behavior remains unchanged.

---

## 7. D6.1 test-first / evidence strategy

D6.1 is primarily a **behavior-preserving ownership refactor**. Per `AGENTS.md` and `TESTING_STRATEGY.md`, do not manufacture a RED solely because production source changes.

### Existing evidence to use first

The current typed session test surface already includes, among others:

- `ClocktowerGameSessionTest`;
- `ClocktowerGlobalObservationCommitTest`;
- `ClocktowerHistoricalActionObservationCaptureTest`;
- `ClocktowerTimelineSequenceAllocatorTest`;
- `ClocktowerNightCheckpointTest`;
- `ClocktowerRecommendationCoordinatorTest`.

Persistence/recovery tests from PS1–PS5 remain the owning evidence for recovery semantics.

### Add a new RED only for a real uncovered contract

Before implementation, audit whether existing tests prove the **production wiring cutover** itself. If not, add the narrowest durable typed integration/characterization test, for example:

```text
start/restore a Clocktower production session
-> mutate canonical state through the intended session seam
-> build/restore current Recovery contract
-> assert one canonical snapshot/history/revision authority
```

Do **not** add a source-string assertion such as “App file no longer contains variable X” unless no stable typed boundary can express a genuinely important architecture invariant.

### Validation cadence

```text
D6.1 preflight
-> identify existing owning GREEN tests
-> meaningful T0 RED only if a real production-wiring coverage gap exists
-> structural cutover
-> T0 focused session + recovery/wiring evidence
-> git diff --check + exact changed-file/semantic audit
-> :app:testFast (T1) at logical GREEN
-> T2 affected: Clocktower session/history + persistence/recovery integration
-> R2 only where the changed production wiring/main-thread boundary selects it
-> [full-ci] T4 once at D6.1 logical acceptance checkpoint
```

Real Clingo is **not** selected merely for ownership movement. Escalate to exact/oracle validation only if epistemic semantics actually change, which D6.1 is not authorized to do.

---

## 8. D6.1 implementation staging

The precise anchors must be re-audited against live source immediately before each write, but the architectural order should be:

### D6.1a — production-wiring characterization

- map current creation/start/recovery sites for canonical `GameSnapshot` fields;
- map all App-root writes to timeline/log/revisions/canonical `GameState`;
- prove existing tests cover the intended behavior or add one durable wiring RED if genuinely missing;
- no production change yet.

### D6.1b — establish the live session instance

- create/restore one `ClocktowerGameSession` at the existing Clocktower game lifetime boundary;
- do not introduce a second persistent owner;
- project canonical reads from `session.snapshot` where safe;
- preserve UI-local/transient state separately.

### D6.1c — cut canonical mutations over

- route canonical `GameState` / revision / semantic-history mutation through instance methods;
- retire duplicate App-root mutation authority for the cut-over fields in the same logical slice;
- keep UI event projection distinct from durable semantic timeline where they are different concepts.

### D6.1d — recovery adapter cutover

- recovery snapshot construction reads the canonical session snapshot rather than rebuilding the same canonical fields from parallel root locals;
- recovery application restores/reconstructs the live `ClocktowerGameSession` before dependent Clocktower consumers run;
- Recovery schema and PS5 write/lifecycle contracts remain unchanged.

### D6.1e — acceptance

- focused tests;
- T1 FAST;
- affected T2 recovery/session tests;
- exact diff + ownership audit proving one canonical owner;
- one T4 checkpoint.

Intermediate commits must not leave two writable canonical authorities merely to make the patch easier. If a safe atomic cutover cannot be expressed with stable anchors, stop and use the local-worktree fallback defined by `AGENTS.md` rather than accepting a dual-authority intermediate state.

---

## 9. Expected changed-file scope for D6.1

Exact allowlist is finalized only after D6.1a, but the expected scope should remain narrow:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/session/ClocktowerGameSession.kt   # only if owner API truly needs a small durable addition
one or a small number of existing/new typed session/recovery integration tests
docs/D6_* progress/checkpoint document(s)
docs/CURRENT_DEVELOPMENT_ROADMAP.md
```

Do not modify unrelated UI, rules, algorithm, archive, Werewolf or A4 implementation files without a new scope decision.

Because `CampBoardGameHostApp.kt` is large/truncated through connector reads, current `AGENTS.md` applies:

- connector writes small tests/docs directly;
- first fallback for stable localized App-root edits is a branch-locked, blob-locked one-shot GitHub Actions + separate Python patch;
- use Codex/Luna/local worktree only when that fail-closed remote patch genuinely cannot be made safe.

---

## 10. Post-D6.1 proposed ordering

These are **provisional campaign stages**. Re-audit after every completed ownership cutover rather than committing now to a giant fixed plan.

### D6.2 — Clocktower host mutation boundary

After canonical session authority exists, move cohesive day/night durable mutations behind typed session/domain transactions using the existing rule/session planners and authority components.

Goal: App root chooses/presents actions; domain/session owner applies durable game mutations.

Do not move all role logic at once.

### D6.3 — shrink the Clocktower host callback surface

Once state authority and transitions are stable, replace the giant state/callback fan-out between App root and `ClocktowerJudgeScreen` with a small typed presentation/state + command boundary.

Do not create a mega-controller that merely repackages every existing callback.

### D6.4 — simplify root recovery composition

With per-game state no longer spread across so many App locals, reduce recovery build/apply adapter complexity while preserving PS5 contracts exactly.

This is adapter cleanup, **not** a Recovery redesign.

### D6.5 — re-audit the remaining App root

Re-measure:

- file size;
- number of mutable state clusters;
- number of cross-owner callbacks;
- number of recovery-captured App locals;
- dependency directions;
- remaining UI-only cohesive extraction opportunities.

Only then decide whether another ownership slice is worthwhile.

---

## 11. Disposition of earlier decomposition ideas

| Earlier/obvious candidate | D6.0 decision |
|---|---|
| “Keep splitting the biggest file until below a byte threshold” | **Reject as primary strategy.** Size is a smell/secondary budget, not ownership. |
| Recovery extraction | **Drop.** PS5 already established the contract and owners. |
| Seating extraction | **Drop.** `HostSeatingSetupFlow` is already the state owner. |
| Archive combined with Recovery | **Reject.** Separation is an explicit PS5 architectural contract. |
| A4 extraction before session | **Reorder later.** A4 should consume canonical session state, not define it. |
| Recommendation manager extraction | **Drop generic-manager idea.** Existing recommendation/session coordinators already own behavior. |
| More Clocktower UI helper extraction | **Defer.** Useful only after authority movement or where a clean presentation-only boundary exists. |
| Complete existing `ClocktowerGameSession` ownership | **Promote to D6.1.** This is the strongest existing architectural seam. |

---

## 12. Stop / re-audit conditions

Stop D6.1 and return to architecture review if implementation appears to require any of the following:

- Recovery schema version change or cross-version migration;
- gameplay/rules semantic changes;
- new A4/ZDD production behavior;
- all-game generic session framework;
- replacement of current Archive architecture;
- major Compose/navigation redesign;
- a new object with dozens of callbacks mirroring the App root;
- dual writable canonical session authorities across a committed checkpoint;
- hidden Android/Compose dependency inside `ClocktowerGameSession`;
- broad unrelated file changes required merely to make the extraction compile.

---

## 13. D6.0 acceptance

D6.0 itself changes documentation only. Acceptance criteria are:

- current App/root responsibility map documented;
- already-extracted owners distinguished from residual App adapters;
- first owner selected by cohesion/authority/testability rather than size;
- dependency direction specified;
- D6.1 invariants and non-goals specified;
- tests/CI cadence specified;
- no production code changed;
- active roadmap/handoff updated to reference this audit;
- completed historical docs moved out of active docs root without losing traceability.

**Recommended next action after review: begin D6.1a production-wiring characterization, still without production edits until the exact canonical-write map and test gap are known.**
