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

ROLE-ROTATION-1 recent role rotation               COMPLETE / T4 validated in draft PR #119; not merged
EPI-MQ / Productive Uncertainty                   NEXT after ROLE-ROTATION-1 merge unless reprioritized
UX-R6 recommendation-provider replacement         QUEUED after EPI-MQ unless reprioritized
```

Completed campaign evidence is historical. Do not use an old handoff/checkpoint as current execution authority.

## 2. ROLE-ROTATION-1 completed product behavior

Real play exposed a user-experience issue: across several games in one session, the same human player can receive the same starting identity or a high-impact special category too often. Examples include one player receiving Demon three times in five games or repeatedly receiving Butler.

Implemented behavior:

```text
preserve the legal role set
preserve setup legality and game balance
preserve randomness
strongly avoid same-player repeats from the immediately previous game
weakly avoid repeats from the two earlier completed games
if zero is impossible, minimize repeats according to the strict priority order
never block setup merely to satisfy the preference
```

This is a **soft assignment constraint**, not a new setup legality rule and not deterministic role rotation.

The implementation seam is the existing player-to-role deal planner, after legal role-set selection and shown-identity commitment. Existing Trouble Brewing setup-composition diversity history remains authoritative for setup diversity and is not repurposed as player rotation policy.

## 3. ROLE-ROTATION-1 product semantics

### 3.1 Lexicographic assignment objective

The assignment policy optimizes in this strict order:

```text
Priority 1 — immediately previous game exact starting identity repeat
minimize same-player / same-shownRoleId repeats

Priority 2 — immediately previous game special character-category repeat
among Priority-1-optimal assignments, minimize:
DEMON    -> DEMON
MINION   -> MINION
OUTSIDER -> OUTSIDER

TOWNSFOLK -> TOWNSFOLK carries no category-repeat penalty.

Priority 3 — exact starting identity repeats from the two earlier games
among Priorities-1/2-optimal assignments, minimize weighted repeats:
two games ago   weight 2
three games ago weight 1

Priority 4 — special-category repeats from the same two earlier games
among Priorities-1/2/3-optimal assignments, minimize weighted DEMON/MINION/OUTSIDER repeats:
two games ago   weight 2
three games ago weight 1

Priority 5 — randomness
among assignments tied on Priorities 1–4, use seeded independent tie-breaking.
```

The four behavioral costs are a lexicographic tuple, not one arbitrary weighted total. Older-history accumulation therefore cannot outweigh an avoidable repeat from the immediately previous game.

### 3.2 Actual-role versus shown-identity semantics

Exact identity repeat follows the player's starting experience and therefore compares **shown identity**.

Special category repeat follows the role's real setup type and therefore compares **actual character category**.

Example: a Drunk shown Investigator followed by a real Investigator is an exact shown-identity repeat, but OUTSIDER -> TOWNSFOLK is not a category repeat.

### 3.3 History horizon

ROLE-ROTATION-1 uses the **three most recent completed games**, newest first, across roster-size changes:

- immediately previous game = strong exact/category priorities;
- two games ago = weak weight 2;
- three games ago = weak weight 1;
- no wall-clock expiry in v1;
- an old/legacy game with no player-identity facts still occupies its chronological slot and is not silently skipped so that an older game becomes “last game”.

This is recent-session fairness, not permanent lifetime exclusion. A future wall-clock window may be added only if real play demonstrates a need.

### 3.4 Human-player key

The current minimal stable-player seam is the exact trimmed confirmed player name, independent of seat number. Do not add a broad player-account/profile system for this feature.

Known limitation: rename/case changes or reusing the same placeholder name for a different human cannot be recognized as identity continuity. A future stable player ID migration, if needed, is a separate product task.

## 4. ROLE-ROTATION-1 architecture fence

Preserve existing setup/session/persistence ownership.

Do not introduce:

- a second setup coordinator;
- deterministic round-robin dealing;
- changes to legal role counts or script composition;
- a broad player-account/profile system solely for anti-repeat;
- UI-NAV changes;
- EPI-MQ/ranking changes;
- broad persistence/recovery redesign;
- wall-clock expiry in this implementation.

Use the existing Trouble Brewing completion/rotation-history lifecycle. The durable player-starting-identity fact is frozen from the final prepared deal before gameplay can mutate identities.

Keep the existing setup-diversity projection filtered by player count. Player-role rotation uses a separate newest-first three-game projection across player-count changes and matches current players by stable player key.

## 5. Acceptance contracts

The implementation proves:

```text
avoidable exact repeat -> avoided
selected role multiset -> unchanged
unavoidable exact repeat -> setup succeeds with the minimum exact repeats
same DEMON category -> avoided when exact-repeat optimum is unchanged and an alternative exists
same MINION category -> avoided under the same rule
same OUTSIDER category -> avoided under the same rule
TOWNSFOLK category repeat -> not penalized
exact shown identity outranks special-category avoidance
immediately previous game outranks accumulated older-game preferences
two-games-ago weak repeat weight 2 outranks three-games-ago weight 1
Drunk exact-repeat semantics -> compare shown identity, category semantics -> compare actual type
equal-optimal assignments -> remain seeded/randomized
seat reorder -> stable-player matching follows player key, not seat
roster add/remove/player-count change -> safe and does not defeat matching for retained players
no usable rotation history -> legacy seeded assignment behavior remains unchanged
restart -> starting identity/category history survives through the existing completion lifecycle
legacy persisted history -> decodes safely with no player-rotation facts
```

Typed RED/GREEN evidence was used at the deal-planner, persistence/history, and production-preparer seams. Large-file App wiring used the repository-approved guarded one-shot workflow rather than an unsafe whole-file connector rewrite.

## 6. Implementation and validation closeout

```text
R1  DealPlanner typed RED/GREEN for exact shown-identity avoidance              COMPLETE
R2  DEMON/MINION/OUTSIDER category avoidance                                   COMPLETE
R3  Freeze final starting player identity/category facts                        COMPLETE
R4  Version/migrate Trouble Brewing completion + rotation persistence           COMPLETE
R5  Three-game recent player-history projection + production setup wiring       COMPLETE
R6  T2/T4 validation, exact diff audit, roadmap/handoff closeout                COMPLETE
```

Key checkpoints/evidence:

```text
pre-App-wiring GREEN checkpoint:
50254b45f73b4985e62ff3da78177b869f56204d

App-wiring product checkpoint:
e2ab42f6159166e56520e13a790c5af3caad124a

large-file one-shot cleanup checkpoint:
df50b64d7087ee7a82efc5288fab67e3a018a212

one-shot run:
34555558005 — SUCCESS
focused setup/factory tests — PASS
forced :app:testFast — PASS
exact diff audit — PASS

T4 acceptance checkpoint:
4d75ed4a147304e3a01c2bc233dbdc33c802ea6f
CI #2274 / run 34556109636 — SUCCESS
Android full unit tests + debug APK — PASS
ASP contract tests — PASS
Real Clingo cross-validation — PASS
R2 main-thread boundary #2136 / run 34556109649 — PASS
```

Net diff from `50254b45...` through the Path-B cleanup contains only the intended `CampBoardGameHostApp.kt` wiring change; temporary workflow/script files do not remain in the branch.

PR #119 remains draft and must not be merged without explicit user authorization.

## 7. UI-NAV-1 closeout

PR #118 standardizes the Storyteller navigation presentation without moving navigation/gameplay ownership:

```text
No persistent global top bar.
Content owns title / instructions / table / local progress.
Bottom visual language:
[ Previous ]   [ Host Tools ]   [ Next ]
```

Identity delivery uses a Storyteller square-table controller while the player-facing reveal remains isolated. Settings is composed under the existing root-owned Host Tools. The later real-device findings around identity-controller center width and night-flow bottom placement were corrected and accepted in device testing.

UI-NAV-1 is closed for feature development. Its audit and closeout documents are historical evidence only and are not part of the default reading chain.

## 8. Queued programs

### EPI-MQ / Productive Uncertainty

This is the next queued program after ROLE-ROTATION-1 is merged, unless the roadmap is explicitly reprioritized.

Queued handoff:

`docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_EPI_MQ_0_BASELINE_REAUDIT.md`

When resumed, re-audit against then-live `main`; do not assume its historical baseline is still exact.

### UX-R6 recommendation-provider replacement

Remains after EPI-MQ unless the roadmap is explicitly reprioritized again.

## 9. Default reading order for a new development conversation

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. this roadmap;
4. live GitHub `main` / branch / PR state;
5. if PR #119 is still open, `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-11_ROLE_REPEAT_AVOIDANCE.md` for closeout evidence;
6. after ROLE-ROTATION-1 merge, use the handoff for whichever program the roadmap marks current/next.

Historical archives, prior campaign handoffs, and old checkpoint documents should not be loaded by default.

## 10. Stable rule

> **Current roadmap + one active handoff define what happens next. Historical campaign documents provide evidence, not execution authority.**
