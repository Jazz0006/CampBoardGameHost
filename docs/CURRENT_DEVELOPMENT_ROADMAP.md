# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-11 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / merged
R3 transaction-application viability audit        COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE
UI-R5 square-table convergence                    COMPLETE / merged via PR #117
UI-NAV-1 global navigation visual unification     COMPLETE / accepted / merge via PR #118

ROLE-ROTATION-1 consecutive role repeat avoidance CURRENT after PR #118 merge
EPI-MQ / Productive Uncertainty                   QUEUED after ROLE-ROTATION-1 unless reprioritized
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign evidence is historical. Do not use an old handoff/checkpoint as current execution authority.

## 2. Immediate priority — ROLE-ROTATION-1

Real play has exposed a user-experience issue: the same human player can randomly receive the same starting identity again in the following game.

Target behavior:

```text
preserve the legal role set
preserve setup legality and game balance
preserve randomness
prefer zero same-player / same-identity consecutive repeats
if zero is impossible, minimize repeats
randomize among equally optimal assignments
never block setup merely to satisfy the preference
```

This is a **soft assignment constraint**, not a new setup legality rule and not deterministic role rotation.

The first slice is read-only. Before changing production code, audit:

- the role-set selection owner;
- the player-to-role assignment owner;
- the randomness seam;
- existing recent-setup / rotation history and its persisted shape;
- stable human-player identity versus seat number;
- roster reorder/add/remove behavior;
- whether anti-repeat should compare actual starting role or player-visible starting identity, especially around Drunk semantics.

The active handoff is:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_ROLE_REPEAT_AVOIDANCE.md`

Do not begin implementation until that ownership/history audit is complete and the smallest seam is identified.

## 3. ROLE-ROTATION-1 architecture fence

Preserve existing setup/session/persistence ownership.

Do not introduce:

- a second setup coordinator;
- deterministic round-robin dealing;
- changes to legal role counts or script composition;
- a broad player-account/profile system solely for anti-repeat;
- UI-NAV changes;
- EPI-MQ/ranking changes;
- broad persistence/recovery redesign.

Prefer reusing an existing durable recent-game history if it already contains sufficient player-to-identity information. If it does not, add only the smallest typed durable record necessary and characterize recovery/version behavior first.

## 4. Tests-first acceptance target

The first implementation slice should prove:

```text
avoidable repeat -> avoided
selected role multiset -> unchanged
unavoidable repeat -> setup still succeeds with minimum repeats
equal-cost assignments -> remain randomized
seat reorder -> does not defeat stable-player matching where supported
roster change -> remains safe
restart -> history persists only if that is the intended existing contract
```

No fake gameplay/domain RED should be manufactured where only assignment preference changes. Follow `docs/TESTING_STRATEGY.md`.

## 5. UI-NAV-1 closeout

PR #118 standardizes the Storyteller navigation presentation without moving navigation/gameplay ownership:

```text
No persistent global top bar.
Content owns title / instructions / table / local progress.
Bottom visual language:
[ Previous ]   [ Host Tools ]   [ Next ]
```

Identity delivery uses a Storyteller square-table controller while the player-facing reveal remains isolated. Settings is composed under the existing root-owned Host Tools. The later real-device findings around identity-controller center width and night-flow bottom placement were corrected and accepted in device testing.

UI-NAV-1 is closed for feature development. Its audit and closeout documents are historical evidence only and are not part of the default reading chain.

## 6. Queued programs

### EPI-MQ / Productive Uncertainty

Still planned, but explicitly deferred behind ROLE-ROTATION-1 because the user reprioritized the next development target.

Queued handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`

When resumed, re-audit against then-live `main`; do not assume its historical baseline is still exact.

### UX-R6 recommendation-provider replacement

Remains after EPI-MQ unless the roadmap is explicitly reprioritized again.

## 7. Default reading order for a new development conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_ROLE_REPEAT_AVOIDANCE.md`;
5. live GitHub `main` / branch / PR state;
6. only the specialized domain/product documents required by the audited seam.

Historical archives, prior campaign handoffs, and old checkpoint documents should not be loaded by default.

## 8. Stable rule

> **Current roadmap + one active handoff define what happens next. Historical campaign documents provide evidence, not execution authority.**
