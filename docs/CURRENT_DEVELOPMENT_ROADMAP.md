# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-09 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current project state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / FULL accepted / merged
R3 deep transaction-application viability audit   COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE
```

D6.2 was merged through PR #115. The final production-equivalent code/test head was:

```text
b2263cd08bc2ce223598698324bf2b22243c91f2
```

Final D6.2 FULL acceptance was triggered at:

```text
9ec4ce2f9e0d4114f84ee7bde90ff7e409caac8e
CI 34307304901 — PASS
R2 34307304900 — PASS
```

The D6.2 merge commit was:

```text
c75e0f0bc4635ef42ffbece41470c3437a205910
```

Real-device critical-path testing was explicitly waived for the D6.2 merge. That waiver does **not** carry forward to UI-R5.

Detailed D6/R3 evidence is historical and now lives under:

- `docs/archive/checkpoints/d6/`
- `docs/archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md`

## 2. Current priority

> **NEXT: square-table Storyteller UI consolidation / UI-R5 real-device stabilization.**

The global execution order is now frozen as:

```text
UI-R5 square-table Storyteller consolidation + real-device stabilization
-> EPI-MQ / Productive Uncertainty
-> UX-R6 legacy recommendation-provider replacement
```

Do not start EPI-MQ implementation before UI-R5 acceptance. Do not reopen D6 merely because `CampBoardGameHostApp.kt` remains large.

Current active handoff:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`

Primary product/UI reference:

- `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

## 3. UI-R5 mission

UI-R5 is a presentation/interaction stabilization campaign. Its purpose is to make the square-table Storyteller experience coherent and reliable on a real phone without changing domain authority.

Primary goals:

1. audit every surviving square-table Storyteller surface and identify duplicated geometry, seat-state rendering and interaction ownership;
2. establish one reusable square-table presentation vocabulary where a real shared boundary exists;
3. converge Manual clue selection, target selection and player-facing information display on the same seat geometry and visual-state rules;
4. stabilize portrait-phone behavior for real devices, especially dense 8–15 player layouts;
5. verify readability, safe insets, no clipping/overlap, usable tap targets and stable seat identity;
6. preserve hidden-information boundaries and the existing typed legal-domain / structured confirmation path;
7. complete real-device acceptance before moving to EPI-MQ.

### Explicit non-goals

UI-R5 does **not** authorize:

- recommendation/scoring changes;
- EPI-MQ / Productive Uncertainty implementation;
- new gameplay/rules semantics;
- Persistence/Recovery redesign;
- a second game/session state owner;
- broad ViewModel/controller/context bags merely to reduce Compose parameter counts;
- renewed App-root decomposition based only on file size;
- A4/ZDD production cutover.

## 4. UI-R5 execution order

### UI-R5.0 — fresh read-only UI audit

Start from a fresh branch based on live `main` after this documentation closeout is merged.

Audit:

- surviving square-table composables and call sites;
- seat geometry / edge allocation logic;
- selection/highlight state models;
- Storyteller-only vs player-facing information boundaries;
- Manual pair-selection flow;
- Fortune Teller two-target/result flow;
- final player information display;
- dense 8–15 player portrait behavior and known real-device risks.

The first output is an ownership/duplication map and a smallest-cohesive-boundary proposal. Do not begin by introducing a generic state/action bag.

### UI-R5.1 — characterization / layout contract

Before meaningful structural consolidation, establish the cheapest durable evidence for:

- stable seat identity and ordering;
- legal target/selectable/selected/highlighted/disabled presentation states;
- no player-facing hidden-state leak;
- existing typed Manual legality and Fortune Teller result legality;
- existing confirmed observation/history boundary.

For purely visual geometry where unit tests would only lock implementation shape, prefer compile/static evidence plus real-device acceptance over artificial source-shape tests.

### UI-R5.2 — square-table consolidation

Extract/reuse only boundaries that own real shared presentation behavior, for example geometry, seat placement, seat visual state or common interaction shell.

Do not move domain semantics, recommendation invocation, durable observation commit or canonical GameState ownership into the shared UI layer.

### UI-R5.3 — flow convergence

Bring the active Storyteller flows onto the shared square-table visual language where appropriate:

- Manual clue/seat selection;
- direct target selection;
- Fortune Teller two-target/result interaction;
- final player-facing information display.

Keep the current legal semantic domain as the source of truth. Presentation must not reconstruct semantics from localized labels.

### UI-R5.4 — real-device stabilization

Portrait phone is the primary target. Validate at minimum:

- dense player counts up to 15;
- player/seat text remains readable;
- no status/navigation inset collision;
- no seat/center-content overlap;
- important buttons remain reachable;
- selection/highlight does not rely on color alone;
- orientation/recomposition does not corrupt draft selection;
- player-facing display contains only intended visible information.

Real-device defects found here are part of UI-R5, not deferred merely because JVM tests are green.

### UI-R5.5 — acceptance / closeout

UI-R5 is complete only when:

- focused/FAST affected validation is green;
- broader Android validation is run at the logical acceptance checkpoint according to `TESTING_STRATEGY.md`;
- exact diff/ownership audit confirms no domain/persistence scope creep;
- real-device critical paths are executed and recorded;
- roadmap and active handoff are updated before EPI-MQ begins.

## 5. Frozen architecture after D6/R3

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- pure planner/reducer ownership of rules/transaction semantics and exactly-once intent planning;
- existing App ownership of remaining cross-boundary application choreography and Compose-facing projection;
- exact revision cadence and semantic chronology;
- action/observation idempotency and non-mutating preflight;
- Recovery v2 current-version-only policy and existing trigger topology;
- A4 durability/invalidation/prewarm ordering;
- no Compose dependency in session/domain;
- no storyteller-hidden target leak;
- Undercover/Werewolf isolation.

A future product requirement may justify a new application seam, but file size alone does not.

## 6. Testing / validation authority

Read and follow:

- root `AGENTS.md`;
- `docs/TESTING_STRATEGY.md`.

Documentation-only closeout changes require no Android regression. UI-R5 production work should use the risk-based T0–T4 model; the final UI-R5 gate must include recorded real-device validation because the previous D6.2 waiver is now intentionally closed.

## 7. Current authoritative reading order

For a new development session read, in order:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
7. specialized semantic/product docs only as required by the chosen slice.

Do not load the D6 archive by default. Use `docs/archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md` only when historical ownership evidence is needed.
