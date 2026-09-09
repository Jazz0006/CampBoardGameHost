# NEXT DEVELOPMENT HANDOFF — UI-R5 Remaining Night-role Square-table Convergence

> Date: 2026-09-10 Australia/Sydney  
> Status: **ACTIVE — FINAL REMAINING-SURFACE AUDIT NEXT**  
> Scope: Storyteller night-role square-table UI convergence, legacy-surface retirement, and real-device stabilization  
> Branch: `codex/ui-r5-square-table-stabilization`  
> Draft PR: #117

## 1. Read first

For a new development conversation, read in this order:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
7. live flow/materializer/UI code for the current audit or migration slice.

Do not reload the D6 archive by default. D6/R3 are closed.

## 2. Live branch / PR checkpoint

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
latest full-T4 verified head: c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
latest Ravenkeeper checkpoint: 06e01ec7c0410412b38d104f4a5f72bc72811ffe
latest Spy implementation: d45f96ddcdb1142a422d64ee87cf61c5475121f9
```

PR #117 remains open, Draft and unmerged. Do not merge without explicit user authorization.

Latest full-T4 checkpoint:

```text
production fix: 0f66b78bbdc9a50e9f1c94db530e3aaaaf488880
verified head:  c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
CI #2058 / run 34408187526              PASS
- Android full unit tests + debug APK     PASS / executed
- ASP contract tests                      PASS / executed
- Real Clingo cross-validation            PASS / executed
- CI gate                                 PASS
R2 #1925 / run 34408187513                PASS
```

Latest Ravenkeeper per-role checkpoint:

```text
06e01ec7c0410412b38d104f4a5f72bc72811ffe
CI #2087 / run 34415179389  Android FAST + gate PASS
R2 #1954 / run 34415179369   PASS
```

Latest Spy per-role checkpoint:

```text
d45f96ddcdb1142a422d64ee87cf61c5475121f9
CI #2092 / run 34415625342  Android FAST + gate PASS
R2 #1959 / run 34415625336   PASS
```

The per-role checkpoints do not replace `c1ab5578...` as the latest full-T4 checkpoint. A new logical full T4 remains due after the remaining-surface audit, final migrations, and legacy retirement.

## 3. Product target

UI-R5 remains active:

> All Storyteller night-role operations should use one coherent square-table interaction language where appropriate. Once a replacement owner is accepted, retire the superseded Storyteller text/list/manual-dialog surface rather than keep parallel UI.

Shared hierarchy:

```text
1. current actor / player to wake        highest priority
2. role/action instruction
3. related target or information seats  distinct from actor
4. recommendation / result content
5. action controls
6. Previous / Next                      stable compact single row
```

Preserve these rules:

- NightStep/current actor seat is authoritative;
- `isCurrentActor` is independent from target/information state;
- target/result legality comes from typed upstream semantics;
- never recover identity or legality by parsing localized text;
- Storyteller-only information must never leak to player-facing display;
- keep role-specific center controls thin rather than force every role through one generic picker;
- player-facing reveal pages may remain specialized when only the Storyteller shell needs migration.

## 4. Completed / implemented UI-R5 slices

### 4.1 Pair-information — Washerwoman / Librarian / Investigator

Accepted baseline:

```text
b960be22d217ed2caa06b49c0319eace23470b42
CI #2024 PASS — full Android + APK / ASP / Real Clingo / gate
R2 #1891 PASS
user-reported real-device PASS
```

Later wake-actor / compact-navigation changes are automated-green. Historical device PASS predates some presentation amendments and is not silently extended to every later visual change.

### 4.2 Chef — COMPLETE / device accepted

Chef uses the square table with current-actor highlight, Storyteller-only evil hints, Recluse marker, typed result witnesses and Spy/Recluse registration semantics. The user reported real-device PASS.

### 4.3 Empath — IMPLEMENTED / automated green / device pending

Empath uses typed `NumericResult.subjectSeats` for living-neighbour scope and does not recompute adjacency in UI. Impaired arbitrary values do not fabricate contributor-seat witnesses.

### 4.4 Undertaker — IMPLEMENTED / automated green / device pending

Undertaker uses a read-only square-table information surface. The step-level typed `RoleAt` anchors the executed seat; reliable/registration choices must agree with it. Impaired role choices remain opaque display options with no fabricated player proposition. Generic parallel result surfaces are suppressed while the specialized owner is active.

Original Undertaker implementation checkpoint: `4e829660...`; shared opaque-role hardening is included at `06e01ec7...`.

### 4.5 Ravenkeeper — IMPLEMENTED / automated green / device pending

Ravenkeeper now keeps target selection and final role result inside one square-table interaction owner.

Key behavior:

- dead Ravenkeeper remains the current actor/trigger owner and is highlighted independently from the target;
- target legality remains `clocktowerRavenkeeperTargetCards(cards)`;
- the step-level `RoleAt` anchors the selected target seat;
- reliable / Spy-Recluse typed result options must agree with that target or fail closed;
- drunk/poisoned role options remain opaque existing `ClocktowerDisplayOption`s with `proposition = null`;
- player-facing unreliable display remains `resolveClocktowerLegacyUnreliablePlayerDisplay`, so misleading information is not recorded as reliable epistemic truth;
- no localized-text parsing is used to reconstruct semantic identity;
- generic recommendation/result-first/unreliable/direct controls are suppressed while Ravenkeeper owns the step.

Checkpoint:

```text
06e01ec7c0410412b38d104f4a5f72bc72811ffe
CI #2087 / run 34415179389  Android FAST + gate PASS
R2 #1954 / run 34415179369   PASS
```

Real-device acceptance is still pending.

### 4.6 Spy — IMPLEMENTED / automated green / device pending

The user explicitly selected a low-risk boundary for Spy:

> migrate the wake/Storyteller operation shell to the square table, but temporarily keep the existing Grimoire display button and existing player-facing Grimoire page.

Implemented behavior:

- Spy Storyteller step is a read-only square table;
- current Spy actor is highlighted; no seat is selectable;
- healthy Spy keeps the existing `clocktower_host_show_to_player` button;
- that button calls the exact existing `onShowPlayerDisplay(step)` handoff;
- Grimoire generation, `GrimoireState`, player-facing reveal page, observation and history behavior were not redesigned;
- poisoned Spy still gets the wake step, but no true-Grimoire reveal button is shown and the Storyteller is explicitly told not to reveal the true Grimoire;
- generic result/direct-display controls are suppressed while the Spy square-table owner is active.

Exact Spy slice from Ravenkeeper checkpoint:

```text
06e01ec7... -> d45f96dd...
4 files only:
- ClocktowerNightStepUi.kt                 modified, 19 add / 2 del
- ClocktowerSpySquareTableUi.kt            added
- ClocktowerSpySquareTablePresentationTest.kt added
- ClocktowerSpySquareTableWiringTest.kt    added
```

Validation:

```text
d45f96ddcdb1142a422d64ee87cf61c5475121f9
CI #2092 / run 34415625342  Android FAST + gate PASS
R2 #1959 / run 34415625336   PASS
```

Do not redesign the player-facing Grimoire page as part of UI-R5 unless a separate concrete reason emerges.

## 5. Ravenkeeper dynamic-trigger correctness bug — separate and already fixed

The prior real-device scenario was:

```text
Monk initially protects Ravenkeeper
-> Storyteller goes back
-> Monk is reconfirmed protecting another player
-> Demon kills Ravenkeeper
-> Ravenkeeper dies, but Ravenkeeper ability step was skipped
```

The production fix `0f66b78...` introduced deferred flow reconciliation for flow-expanding actions. Full T4 + R2 are green at `c1ab5578...`.

The original real-device scenario still requires user retest before device acceptance is claimed. Do not conflate this navigation/correctness path with Ravenkeeper's now-completed UI migration.

## 6. Immediate next work — read-only surviving-surface audit

Do **not** choose another role only from the previous checklist. Perform a fresh read-only inventory from live production flow/materializers and current UI wiring.

Audit both first night and other nights. For each active interaction record:

```text
interaction / role
current Storyteller UI owner
player-facing reveal owner, if any
classification
remaining duplicate legacy entry, if any
recommended action
```

Allowed classifications:

```text
ACCEPTED SQUARE TABLE
MIGRATE
KEEP SPECIALIZED — square-table conversion would reduce clarity or semantics
UNREACHABLE LEGACY — RETIRE
```

Known likely categories must still be verified rather than assumed:

- existing pair-information square tables;
- Chef / Empath;
- Fortune Teller / Chambermaid;
- single-target actions such as Poisoner / Butler / Monk / Demon kill / Red Herring;
- dynamic Mayor redirect / Demon successor;
- Undertaker / Ravenkeeper / Spy;
- generic information roles or special reveal steps that may still survive, including Clockmaker, Sage, first-night evil information, and new-Demon identity.

The audit must explicitly search for generic recommendation/result-first/unreliable/direct-display surfaces that remain reachable behind a specialized replacement.

## 7. Likely audit decision principle

Use square-table migration when the operation materially depends on:

- identifying a current actor;
- selecting or understanding one or more seats;
- visually distinguishing target/context/player roles;
- keeping a night interaction spatially consistent with the rest of UI-R5.

Prefer `KEEP SPECIALIZED` when a step is fundamentally a player-facing reveal or a non-spatial confirmation and forcing a square table would add noise without improving the Storyteller decision.

The Spy migration is the model for a hybrid boundary: square-table Storyteller shell, existing specialized reveal page retained.

## 8. Validation strategy

Follow `docs/TESTING_STRATEGY.md`.

Per remaining implementation slice:

- tests-first when a stable semantic/presentation contract needs characterization;
- no artificial domain RED for purely visual geometry;
- focused tests during implementation;
- Android FAST at production checkpoints;
- keep R2 green;
- exact diff audit, especially for large owner files;
- no full T4 after every small role unless risk requires it.

After the remaining-surface audit, final migrations, and legacy retirement:

```text
run final logical full T4
-> exact scope/diff audit
-> cross-role real-device acceptance
-> only then consider PR #117 merge-ready
```

Real-device acceptance remains separate from automation.

## 9. Architecture / scope fence

Preserve:

- `ClocktowerGameSession` canonical writable state;
- Planner/Reducer rule ownership;
- typed upstream legal/recommendation domains;
- exact revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- existing square-table geometry unless a concrete device defect requires a focused change.

Do not broaden into:

- EPI-MQ scoring/explanation;
- recommendation-provider replacement;
- generalized App-root rewrite;
- Persistence follow-up;
- unrelated gameplay changes;
- A4/ZDD activation;
- D6 decomposition reopening because of source-file size;
- speculative redesign of the Spy Grimoire reveal page.

## 10. Stop / completion condition

Do not merge PR #117 automatically.

UI-R5 becomes merge-ready only after:

```text
Undertaker accounted for
+ Ravenkeeper accounted for
+ Spy shell accounted for
+ all active first-night / other-night Storyteller surfaces audited
+ appropriate final square-table migrations complete
+ superseded reachable legacy surfaces retired
+ affected focused/FAST/R2 green
+ final logical T4 green
+ exact scope/diff audit green
+ cross-role real-device critical paths recorded
+ roadmap/handoff synchronized
```
