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
UI-R5 pair-information semantic/table slice       ACCEPTED baseline / T4 + device
UI-R5 wake-actor + compact-navigation amendment   IMPLEMENTED / FAST + R2 green / device retest pending
UI-R5 overall campaign                            ACTIVE / night-role square-table convergence
```

D6.2 was merged through PR #115. Historical D6/R3 evidence lives under:

- `docs/archive/checkpoints/d6/`
- `docs/archive/checkpoints/d6/D6_DECOMPOSITION_CAMPAIGN_CLOSEOUT_INDEX_2026-09-09.md`

## 2. UI-R5 live branch and accepted baseline

Active branch / PR:

```text
branch codex/ui-r5-square-table-stabilization
Draft PR #117
base main 60842381dcbc3709ad453209846f66e9b4a7a777
```

The original Washerwoman / Librarian / Investigator pair-information implementation reached a production-equivalent T4 checkpoint at:

```text
b960be22d217ed2caa06b49c0319eace23470b42
```

Evidence:

```text
CI #2024 / run 34339861281             PASS
- Android full unit tests + debug APK   PASS / executed
- ASP contract tests                    PASS / executed
- Real Clingo cross-validation          PASS / executed
- CI gate                               PASS
R2 #1891 / run 34339861261              PASS
```

The user subsequently reported real-device PASS for that baseline. This historical acceptance remains valid for the pair legality / recommendation / Manual / reveal semantics that were not changed later.

A new product requirement then reopened presentation work: every night-action page must primarily tell the Storyteller **which player to wake**, visually distinguish that actor from information-related target seats, and keep Previous / Next as a stable single-row control.

Current production/test checkpoint for that amendment:

```text
d305e7abae646f293e979f1de27895569397cbb9
CI #2032 / run 34348340950              PASS
- Android FAST unit tests               PASS / executed
- full Android + APK                    SKIPPED by ordinary checkpoint policy
- CI gate                               PASS
R2 #1899 / run 34348340898              PASS
```

The amendment intentionally does not claim the old real-device result as validation of the changed presentation. Device retest remains pending.

## 3. Current priority

> **CURRENT: UI-R5 night-role square-table convergence and legacy-UI retirement.**

UI-R5 is **not** in closeout. The product target is now explicit:

> All Storyteller night-role operations should use one coherent square-table interaction language, and superseded text/list/manual-dialog surfaces should be retired once their replacement paths are accepted.

Shared presentation hierarchy:

```text
1. current actor / player to wake        highest priority
2. action / role instruction             primary task context
3. information-related / target players  visually distinct from actor
4. recommended/result information        secondary decision content
5. action buttons
6. Previous / Next                       stable compact single-row navigation
```

The actor is a separate visual dimension from selection state. Existing Fortune Teller semantics are the reference:

- `isCurrentActor` owns the strong actor border/highlight;
- `SelectedFirst` / `SelectedSecond` / other target states own information/target highlighting;
- the UI must not parse localized labels to recover actor identity;
- NightStep's typed/current actor seat remains authoritative.

The immediate migration sequence is:

```text
pair-information wake/navigation amendment + device retest
-> Chef
-> Undertaker
-> Ravenkeeper
-> audit/migrate any other surviving legacy night-role surfaces
-> delete/retire unreachable legacy UI
-> final T4 + real-device cross-role acceptance
-> UI-R5 closeout
```

Chef / Undertaker / Ravenkeeper are explicit known remaining old-interface targets, not an exhaustive list. Before declaring completion, audit every night action against the unified square-table contract.

Do not start EPI-MQ implementation before UI-R5 closeout. Do not reopen D6 merely because `CampBoardGameHostApp.kt` remains large.

Current active handoff:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`

Primary product/UI reference:

- `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

## 4. Implemented pair-information ownership

For Washerwoman / Librarian / Investigator, the reusable owners remain:

```text
ClocktowerPairManualAuthority          complete typed Manual legality
ClocktowerPairManualSelectionModel     two-seat draft interaction state
clocktowerPairManualSeatState          legal selectable/selected/disabled projection
ClocktowerSquareTableSeatSurface       shared geometry/rendering
ClocktowerNightActionWakeInstruction   shared prominent wake instruction
ClocktowerSquareTableStepNavigation    compact one-row square-table navigation
existing player-display resolution     final commit/reveal handoff
```

`ClocktowerPairInformationSquareTableUi.kt` composes these owners. It does not own recommendation ranking, legal-domain generation, durable history, Recovery, or canonical GameState.

The current amendment adds two presentation contracts without changing pair legality:

- the current actor uses the existing independent `isCurrentActor` square-table highlight;
- information-related seats retain their existing selected/Manual states;
- center content shows the wake instruction before the recommendation/manual information;
- square-table step navigation uses short non-wrapping Previous / Next labels in one row.

Characterization now also protects that actor highlighting is independent from pair-information seat state.

## 5. Validation state

### Historical accepted pair baseline

- focused/FAST: PASS;
- T4: PASS;
- exact scope audit: PASS;
- user-reported real-device acceptance: PASS.

### Wake/navigation amendment

- production/test head: `d305e7abae646f293e979f1de27895569397cbb9`;
- CI #2032: PASS;
- Android FAST: PASS / executed;
- R2 #1899: PASS;
- exact amendment diff from pre-amendment head: four files only;
- full Android/T4: not rerun at this ordinary iteration checkpoint;
- real-device retest: pending.

A new T4 checkpoint should be taken at a logical UI-R5 convergence milestone or before merge readiness, rather than after every small presentation migration.

## 6. Frozen architecture after D6/R3

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

## 7. Scope fence

UI-R5 authorizes migration/consolidation of Storyteller night-role presentation onto existing square-table interaction owners. It does **not** authorize:

- EPI-MQ scoring or explanation;
- recommendation-provider replacement;
- generalized architecture rewrite of `CampBoardGameHostApp.kt`;
- Persistence Simplification follow-up;
- gameplay/rules fixes unrelated to a UI-R5 defect;
- new scripts/characters merely to test layout;
- A4/ZDD production activation.

Do not change gameplay semantics merely to make a role easier to fit into the unified UI. Each role may need a distinct thin center-control composition while sharing the table, actor, target and navigation language.

## 8. UI-R5 completion condition

UI-R5 can close only when:

```text
all active night-role Storyteller paths audited
+ every appropriate role uses the unified square-table visual language
+ wake actor is explicit and visually distinct where a player is awakened
+ role-specific target/result semantics remain typed and correct
+ superseded legacy UI paths are unreachable and retired
+ focused/FAST affected validation green
+ final logical T4 checkpoint green
+ exact diff / scope audit green
+ real-device cross-role critical paths recorded
+ no hidden-information/domain/persistence regression
```

The project is not yet at this condition.

## 9. Current authoritative reading order

For a new development session read, in order:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-09_UI_R5_SQUARE_TABLE_STABILIZATION.md`;
5. `docs/UI_R5_PAIR_INFORMATION_REAL_DEVICE_ACCEPTANCE_2026-09-09.md` as historical pair-baseline evidence only;
6. `docs/TESTING_STRATEGY.md`;
7. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
8. live role-specific UI code for the next migration target.
