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
UI-R5 pair-information square-table slice         IMPLEMENTED / FAST+R2 green / T4+device pending
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

### UI-R5 live implementation checkpoint

Active branch / PR:

```text
branch codex/ui-r5-square-table-stabilization
Draft PR #117
base main 60842381dcbc3709ad453209846f66e9b4a7a777
latest validated production/test head before docs checkpoint:
ecfacb7b11d39e65febef89fb3e058390f9eea5f
```

At that head:

```text
CI #2022 / run 34339511298       PASS
- Android FAST unit tests         PASS / executed
- full Android unit tests + APK   SKIPPED by ordinary PR policy
- CI gate                         PASS
R2 #1889 / run 34339511280        PASS
```

The pair-information flow for Washerwoman / Librarian / Investigator has been converged onto the square-table interaction language without changing domain, recommendation, persistence or player-display commit authority. A behavior-level exact-diff audit additionally found and fixed one presentation issue: Manual mode now delegates selectable/selected/disabled seat projection to the existing pair Manual helper, so an already-selected first seat exposes only legal second-seat continuations. A `[full-ci]` T4 checkpoint is still required for this logical slice, and real-device validation remains mandatory before UI-R5 can close.

## 2. Current priority

> **CURRENT: continue UI-R5 from the implemented pair-information square-table slice through T4 and real-device stabilization.**

The global execution order remains frozen as:

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

**Status: ACTIVE; pair-information ownership slice audited.**

Audit:

- surviving square-table composables and call sites;
- seat geometry / edge allocation logic;
- selection/highlight state models;
- Storyteller-only vs player-facing information boundaries;
- Manual pair-selection flow;
- Fortune Teller two-target/result flow;
- final player information display;
- dense 8–15 player portrait behavior and known real-device risks.

For the pair-information slice, the audit established that the existing ownership already contains the right reusable seams:

```text
ClocktowerPairManualAuthority          typed complete Manual legality
ClocktowerPairManualSelectionModel     two-seat draft interaction state
clocktowerPairManualSeatState          legal selectable/selected/disabled projection
ClocktowerSquareTableSeatSurface       shared square-table geometry/rendering
existing player-display resolution     commit/reveal handoff
```

The chosen consolidation therefore reuses those seams rather than introducing a new generic state/action bag or moving domain semantics into Compose.

### UI-R5.1 — characterization / layout contract

**Status: PARTIAL COMPLETE for pair-information flow.**

Durable evidence now protects:

- recommendation-to-Manual canonicalization by structured pair semantics;
- recommended pair seeding into Manual edit mode;
- fail-closed behavior for an invalid/stale recommendation;
- recommended read-only seat highlighting;
- Manual selectable/selected/disabled seat-state projection;
- legal second-seat availability after entering Manual edit from an existing recommendation.

For purely visual geometry where unit tests would only lock implementation shape, continue to prefer compile/static evidence plus real-device acceptance over artificial source-shape tests.

### UI-R5.2 — square-table consolidation

**Status: pair-information slice COMPLETE; broader campaign remains ACTIVE.**

The new `ClocktowerPairInformationSquareTableUi` composes the existing square-table surface with the existing pair Manual authority/state model. It owns only the Storyteller presentation transition:

```text
recommended read-only preview
-> in-place Manual edit
-> existing show/confirm handoff
```

It does **not** own recommendation ranking, legal-domain generation, durable observation commit or canonical GameState.

### UI-R5.3 — flow convergence

**Status: pair-information slice COMPLETE in code; remaining UI-R5 flow audit/real-device work continues.**

Washerwoman / Librarian / Investigator now follow:

```text
night step
-> full-screen square table with recommended pair highlighted
-> center information preview
-> [展示此信息] or [手动选择]
-> Manual edits the same table in place
-> center role becomes editable
-> same existing player-display/confirmation path
```

Manual player selection follows the existing Fortune Teller-style two-seat interaction pattern and the existing pair Manual legal-continuation projection rather than a separate player dropdown/list interaction.

Keep the current legal semantic domain as the source of truth. Presentation must not reconstruct semantics from localized labels.

### UI-R5.4 — real-device stabilization

**Status: PENDING / REQUIRED.**

Portrait phone is the primary target. Validate at minimum:

- dense player counts up to 15;
- player/seat text remains readable;
- no status/navigation inset collision;
- no seat/center-content overlap;
- important buttons remain reachable;
- selection/highlight does not rely on color alone;
- orientation/recomposition does not corrupt draft selection;
- player-facing display contains only intended visible information;
- Washerwoman / Librarian / Investigator recommended and Manual paths work on device;
- opening/closing player display returns to the correct night flow.

Real-device defects found here are part of UI-R5, not deferred merely because JVM tests are green.

### UI-R5.5 — acceptance / closeout

**Status: NOT READY.**

UI-R5 is complete only when:

- focused/FAST affected validation is green;
- broader Android validation is run at the logical acceptance checkpoint according to `TESTING_STRATEGY.md`;
- exact diff/ownership audit confirms no domain/persistence scope creep;
- real-device critical paths are executed and recorded;
- roadmap and active handoff are updated before EPI-MQ begins.

The current pair-information implementation has FAST/R2 evidence but has **not** yet satisfied the broader `[full-ci]` checkpoint or real-device acceptance.

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

Documentation-only closeout changes require no Android regression. UI-R5 production work should use the risk-based T0–T4 model. Ordinary PR synchronization may stop at FAST; the pair-information logical checkpoint must be explicitly escalated with `[full-ci]` before it is treated as T4 accepted. The final UI-R5 gate must also include recorded real-device validation because the previous D6.2 waiver is now intentionally closed.

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
