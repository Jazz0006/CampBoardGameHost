# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-10 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> **Single current project-status and execution-priority authority.**

## 1. Current project state

```text
Persistence Simplification                         COMPLETE / merged
D6.1 Clocktower session authority                 COMPLETE / merged
D6.2 UI Composition R0–R2                         COMPLETE / FULL accepted / merged
R3 deep transaction-application viability audit   COMPLETE / NO-GO
D6 decomposition campaign                         COMPLETE

UI-R5 pair-information baseline                   ACCEPTED / T4 + device
UI-R5 wake-actor + compact navigation             IMPLEMENTED / automated green
UI-R5 Chef square-table                           COMPLETE / user device PASS
UI-R5 Empath square-table                         IMPLEMENTED / automated green / device pending
UI-R5 Undertaker square-table                     IMPLEMENTED / automated green / device pending
UI-R5 Ravenkeeper square-table                    IMPLEMENTED / automated green / device pending
UI-R5 Spy square-table shell                      IMPLEMENTED / automated green / device pending
Dynamic night-trigger navigation bug              FIXED / T4 + R2 green / device retest pending
UI-R5 overall campaign                            ACTIVE — remaining-surface audit next
```

Historical D6/R3 evidence lives under `docs/archive/checkpoints/d6/`. Do not reopen D6 merely because a remaining source file is large.

## 2. Live UI-R5 branch / PR

```text
branch: codex/ui-r5-square-table-stabilization
Draft PR: #117
base main: 60842381dcbc3709ad453209846f66e9b4a7a777
latest full-T4 verified head: c1ab5578a2fb64e6506d27b898df9f4bbb1388fc
latest Ravenkeeper checkpoint: 06e01ec7c0410412b38d104f4a5f72bc72811ffe
latest Spy implementation: d45f96ddcdb1142a422d64ee87cf61c5475121f9
```

PR #117 remains open, Draft and unmerged. Do not merge without explicit user authorization.

Current full validation checkpoint:

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

`c1ab5578...` is a no-tree-change `[full-ci]` verification commit on top of production code `0f66b78...`. It remains the latest full-T4 checkpoint; per-role UI checkpoints below do not replace it.

Latest Ravenkeeper per-role checkpoint:

```text
implementation head: 06e01ec7c0410412b38d104f4a5f72bc72811ffe
CI #2087 / run 34415179389               PASS
- Android FAST unit tests                 PASS / executed
- Android full unit tests + debug APK     skipped / not requested for this per-role slice
- ASP contract tests                      skipped / UI-only change
- Real Clingo cross-validation            skipped / UI-only change
- CI gate                                 PASS
R2 #1954 / run 34415179369                PASS
```

Latest Spy per-role checkpoint:

```text
implementation head: d45f96ddcdb1142a422d64ee87cf61c5475121f9
CI #2092 / run 34415625342               PASS
- Android FAST unit tests                 PASS / executed
- Android full unit tests + debug APK     skipped / not requested for this per-role slice
- ASP contract tests                      skipped / UI-only change
- Real Clingo cross-validation            skipped / UI-only change
- CI gate                                 PASS
R2 #1959 / run 34415625336                PASS
```

Another logical full T4 remains required after the remaining-surface audit, any final migrations, and legacy retirement.

## 3. Current priority

> **CURRENT: perform the final read-only inventory of active night-role Storyteller surfaces, then migrate or retire only the surviving legacy surfaces that still have a clear UI-R5 benefit.**

Product target:

> All Storyteller night-role operations should use one coherent square-table interaction language where that interaction model is appropriate. Once a replacement path is accepted, retire the superseded text/list/manual-dialog path instead of keeping two parallel UIs.

Shared presentation hierarchy:

```text
1. current actor / player to wake        highest priority
2. role/action instruction
3. related target / information players  visually distinct from actor
4. result / recommendation
5. action controls
6. Previous / Next                       stable compact one-row navigation
```

Stable interaction rules:

- NightStep/current actor seat is authoritative;
- `isCurrentActor` is independent from target/information state;
- typed upstream domains own legality and registration witnesses;
- localized display text is never parsed back into semantic identity;
- Storyteller-only truth/reliability information never leaks to player-facing reveal;
- specialized thin center controls are preferred over one universal mega-picker;
- an existing player-facing reveal may remain the owner when UI-R5 only needs to migrate the Storyteller shell.

## 4. Completed / implemented UI-R5 slices

### Pair-information: Washerwoman / Librarian / Investigator

Accepted semantic/table baseline:

```text
b960be22d217ed2caa06b49c0319eace23470b42
CI #2024 PASS — full Android + APK / ASP / Real Clingo / gate
R2 #1891 PASS
user-reported real-device PASS
```

Pair Manual legality, recommendation canonicalization and reveal semantics remain accepted. The later wake-actor/navigation presentation amendment is automated-green; historical device acceptance predates that amendment and is not silently extended to every later visual change.

### Chef

Status: **COMPLETE / user-reported real-device PASS**.

Chef uses the square-table shell with current-actor wake highlight, Storyteller-only evil hints, Recluse marker, one-result direct reveal, typed multi-result witnesses, Spy/Recluse registration witnesses, and fail-closed presentation for impaired arbitrary values.

### Empath

Status: **IMPLEMENTED / automated green / real-device acceptance pending**.

Empath uses authoritative `NumericResult.subjectSeats` for its two effective living neighbours; the UI does not recompute adjacency/dead-player skipping. Neighbour scope, actual-evil hint, Recluse marker and selected-result contribution remain separate presentation dimensions. Impaired arbitrary values do not fabricate contributor-seat witnesses.

### Undertaker

Status: **IMPLEMENTED / automated green / real-device acceptance pending**.

Undertaker uses a dedicated read-only square-table information surface:

- current Undertaker actor is highlighted independently from the information-context player;
- the executed player is anchored by the step-level typed `InformationProposition.RoleAt.seat`;
- reliable / registration result choices with typed propositions must agree with that execution seat;
- impaired role choices deliberately remain opaque existing display options rather than inventing player-visible propositions;
- the opaque role label is rendered from the existing display option only; presentation does not parse localized text back into semantic identity;
- `resolveClocktowerLegacyUnreliablePlayerDisplay` keeps `displayProposition = null` for impaired information, so misleading information is not recorded as a reliable observation;
- the executed player remains read-only context, never a selectable target;
- generic recommendation/result-first/unreliable/direct controls are suppressed while the specialized Undertaker owner is active.

Original Undertaker implementation checkpoint was `4e829660...`; the shared opaque-role projection hardening is included in the later Ravenkeeper checkpoint `06e01ec7...`.

Do not mark Undertaker device accepted until a real-device run is explicitly reported.

### Ravenkeeper

Status: **IMPLEMENTED / Android FAST + R2 green / real-device acceptance pending**.

Ravenkeeper now owns target selection and final role display on one square-table interaction:

- the dead Ravenkeeper remains the current actor/trigger owner and is highlighted independently from the selected target;
- target legality remains owned by the existing `clocktowerRavenkeeperTargetCards(cards)` domain;
- the step-level `RoleAt` anchors the selected target seat;
- reliable / Spy-Recluse registration options carrying typed propositions must agree with that selected seat or fail closed;
- drunk/poisoned role options remain opaque existing display choices with `proposition = null`, preserving the existing unreliable publication/history semantics;
- the UI never reconstructs target or role identity from localized text;
- the generic recommendation/result-first/unreliable/direct result surfaces are gated off while the Ravenkeeper square-table owner is active.

Checkpoint:

```text
06e01ec7c0410412b38d104f4a5f72bc72811ffe
CI #2087 / run 34415179389  Android FAST + gate PASS
R2 #1954 / run 34415179369   PASS
```

Do not mark Ravenkeeper device accepted until the user explicitly reports it.

### Spy

Status: **IMPLEMENTED / Android FAST + R2 green / real-device acceptance pending**.

The user explicitly chose a low-risk migration boundary: migrate the Storyteller operation shell, but temporarily keep the existing Grimoire reveal button/page.

Implemented behavior:

- Spy now receives a read-only square-table Storyteller surface;
- current Spy actor uses the shared current-actor highlight; no table seat is selectable;
- a healthy Spy retains the existing `clocktower_host_show_to_player` action and the exact existing `onShowPlayerDisplay(step)` handoff;
- the existing Grimoire generation, `GrimoireState`, player-facing page, observation and history behavior are unchanged;
- a poisoned Spy still wakes on the square-table step but has no true-Grimoire reveal action and receives the existing warning not to show the real Grimoire;
- generic direct/recommendation/result surfaces are suppressed while the Spy square-table owner is active, preventing a parallel legacy interaction.

Checkpoint:

```text
d45f96ddcdb1142a422d64ee87cf61c5475121f9
CI #2092 / run 34415625342  Android FAST + gate PASS
R2 #1959 / run 34415625336   PASS
exact Spy slice diff from 06e01ec7...: 4 files only
```

The Grimoire reveal page itself is intentionally **not** part of this slice and may remain on the existing UI until there is a separate reason to redesign it.

## 5. Dynamic night-trigger navigation bug

A real-device report exposed:

```text
Monk initially protects Ravenkeeper
-> go back
-> reconfirm Monk protecting another player
-> Demon kills Ravenkeeper
-> Ravenkeeper dies but ability step is skipped
```

Characterization proved Monk retarget semantics and Demon death resolution were already correct. The defect was navigation: `DemonKill` could dynamically extend checkpoint-derived `nightSteps`, while the same Compose callback still used the pre-confirmation list to decide that the night was complete.

Production fix `0f66b78...` defers completion for flow-expanding actions until refreshed `nightSteps` prove whether a new trigger step exists. Tests cover dynamic-last-step deferred advance, Host wiring for `DemonKill` / `MayorRedirect`, and Monk retarget followed by Ravenkeeper death.

Automated T4 and R2 are green at `c1ab5578...`. The original user reproduction still needs real-device retest before the bug-fix device gate is marked PASS. This correctness fix remains separate from Ravenkeeper presentation migration.

## 6. Immediate execution sequence

Ravenkeeper and Spy are now implemented. Continue with:

```text
fresh read-only inventory of every active first-night / other-night Storyteller surface
-> classify each as ACCEPTED SQUARE TABLE / MIGRATE / KEEP SPECIALIZED / UNREACHABLE LEGACY
-> migrate only surviving appropriate legacy surfaces
-> retire superseded reachable generic/text/list/manual surfaces
-> exact scope audit
-> final logical UI-R5 T4
-> cross-role real-device acceptance
-> UI-R5 closeout
```

The inventory must be based on live production flow/materializers and current UI wiring, not on an assumed role checklist.

## 7. Final surviving-surface audit

For every active first-night and other-night interaction classify the Storyteller surface as:

```text
ACCEPTED SQUARE TABLE
MIGRATE
KEEP SPECIALIZED
UNREACHABLE LEGACY — RETIRE
```

Audit requirements:

- identify every active production interaction emitted by the current flow/materializer path;
- identify its current UI owner;
- explicitly detect duplicated generic text/list/direct-display controls that remain reachable after a specialized square-table owner exists;
- distinguish player-facing reveal pages from Storyteller operation surfaces;
- do not migrate a specialized interaction merely for visual uniformity if doing so reduces clarity or violates semantic ownership;
- record the recommended next slice and expected benefit before changing production code.

## 8. Validation strategy

Follow `docs/TESTING_STRATEGY.md`.

For each remaining role slice:

- tests-first when a stable semantic/presentation contract needs characterization;
- no artificial domain RED for purely visual geometry;
- focused tests during implementation;
- Android FAST at each production-code checkpoint;
- keep R2 green;
- exact diff audit, especially for large owner files;
- no full T4 after every small role unless risk requires it.

A full T4 checkpoint already passed at `c1ab5578...`. Take another final logical T4 after the remaining migrations and legacy retirement before PR #117 becomes merge-ready.

Real-device acceptance remains separate from automation.

## 9. Frozen architecture / scope fence

Preserve:

- `ClocktowerGameSession` canonical writable ownership;
- Planner/Reducer rule ownership;
- typed legal/recommendation/registration domains;
- exact revision/history/Recovery ordering;
- no Compose dependency in session/domain;
- no Storyteller-hidden information leak;
- Undercover/Werewolf isolation;
- existing square-table geometry unless a concrete device defect requires a focused fix.

UI-R5 does **not** authorize:

- EPI-MQ scoring/explanation;
- recommendation-provider replacement;
- generalized App-root architecture rewrite;
- Persistence follow-up;
- unrelated game-rule changes;
- A4/ZDD activation;
- renewed D6 decomposition based on file size;
- redesign of the Spy Grimoire player-facing page merely for visual uniformity.

## 10. Active handoff

The single active handoff remains:

- `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_R5_REMAINING_NIGHT_ROLES.md`

Primary product/UI reference:

- `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`

## 11. UI-R5 completion condition

UI-R5 can close only when:

```text
Undertaker accounted for
+ Ravenkeeper accounted for
+ Spy shell accounted for
+ every active first-night / other-night Storyteller surface audited
+ appropriate square-table migrations complete
+ superseded reachable legacy UI retired
+ affected focused/FAST/R2 green
+ final logical T4 green
+ exact diff / scope audit green
+ cross-role real-device critical paths recorded
+ no hidden-information/domain/persistence regression
+ roadmap/handoff synchronized
```

The project is **not yet at this condition**.

## 12. Current authoritative reading order

For a new development conversation:

1. root `AGENTS.md`;
2. `docs/README.md`;
3. this roadmap;
4. `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-10_UI_R5_REMAINING_NIGHT_ROLES.md`;
5. `docs/TESTING_STRATEGY.md`;
6. `docs/BOCT_INFORMATION_DISPLAY_AND_MANUAL_SELECTION_UI_DESIGN_2026-09-02.md`;
7. live flow/materializer/UI code for the current audit or migration slice.
