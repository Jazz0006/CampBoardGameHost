# D6.2j — Residual UI Composition Audit

> Date: 2026-09-09 Australia/Sydney
> Status: READ-ONLY SOURCE AUDIT COMPLETE; D6.2k scoped below, not implemented.
> Branch: codex/d6-2-ui-composition; PR #115 OPEN / DRAFT.
> Audited head: c6ed1af27868436eae064755f3ec2dfd6a3d27b5.
> Production checkpoint: 5e0891e7611787300b01d83b889f27a903c0768b.
> main: d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e.

## Decision

Do not continue localizing the remaining MutableState inputs. All five have legitimate external owners. The next bounded slice is D6.2k: remove dormant diagnostic composition and proven dead inputs/locals. After that, investigate a live rendering/preparation boundary; do not equate another parameter-count reduction with solving the remaining architecture.

No production code, test, dependency or workflow was changed by this audit. D6.2i FULL CI 34291666237 and R2 34291666241 remain the production evidence. Audited docs-head CI 34292113372 and R2 34292113412 passed. No Android tests were rerun for this documentation-only audit.

## Measured current boundary

| Owner | Bytes | Lines | Parameters | on-callbacks |
|---|---:|---:|---:|---:|
| CampBoardGameHostApp.kt | 237112 | 4238 | n/a | n/a |
| clocktower/ui/ClocktowerHostScreen.kt / Judge | 281354 | 4755 | 89 | 34 |
| ClocktowerNightStepUi.kt / NightStepCardLocalized | 47970 | 913 | 49 | 13 |

Judge also has three function providers and five MutableState inputs. The NightStep figure is the function signature, not a claim that every input is needed. App scalar-state counts remain 38 delegated remembered vars + 6 explicit remembered state vals = 44; derived values/state lists are excluded.

## Remaining mutable-input ownership

| Input | Concrete external use | Decision |
|---|---|---|
| nightStartedState | App current checkpoint, Recovery position capture/restore, reset; Judge start and readiness effect | Keep external |
| nightStepIndexState | App current checkpoint, Recovery safe cursor restoration, previous-step transaction output/reset | Keep external |
| dayModeState | App restore chooses Klutz/Overview; Artist confirmation, Day/Night Klutz and phase reset route it | Keep external |
| highestVoteNameState | Recovery mechanics capture/restore/reset; Judge nomination standing and execution selection | Keep external |
| highestVoteCountState | Recovery mechanics capture/restore/reset; Judge tie/threshold/standing | Keep external |

Evidence anchors: App `currentClocktowerNightCheckpoint`, `activeGameRecoverySnapshot`, restore assignments and `onMovePreviousNightStep`; persistence/RecoverySnapshot.kt `ClocktowerRecoveryPosition` fields and vote mechanics. Ghost-vote authority is passed as a value plus callback and is equally durable; changing its representation does not change ownership.

## Finding 1 — dormant diagnostic composition (high-confidence cleanup)

In Host, each counter below is initialized to zero, used only as a LaunchedEffect key and a zero-return guard, and never assigned/incremented or passed to a writer:

| Counter | Current declaration / effect line | Consequence |
|---|---|---|
| a4DeviceBenchmarkRuns | 340 / 387 | A4 diagnostic body cannot run |
| a4PrewarmCancellationProbeRuns | 342 / 461 | isolated cancellation probe body cannot run |
| unifiedSetupSelectorBenchmarkRuns | 345 / 1230 | setup selector benchmark body cannot run |

Associated report/error/result states have only initializers and writes within those unreachable bodies; no render consumer remains. `a4DiagnosticAvailable` is used only inside the dormant cancellation probe. Judge `rulesetRef` is used only by these diagnostic expressions/effects; it is not a live recommendation input in this implementation, contrary to the older D6.2a classification.

A fourth instance exists in ClocktowerNightStepUi.kt: `firstNightPoolBenchmarkRuns` (151), report/error states (157–158), and its effect (159–190). The counter is reset to zero by remember keys but has no other writer. Its benchmark body cannot run. The surrounding first-night candidate projections and selection pools DO have live consumers and must remain.

`debugDiagnosticsExpanded` has no writer in Host. It is forwarded to the sole `ClocktowerNightStepCardLocalized` call (4574), whose matching parameter (63) has no body use. Delete the complete dead chain; do not replace it with a constant argument or create a diagnostics state bag.

This is not proof that diagnostics are unnecessary as a product capability. It proves these current UI paths cannot provide them. Restoring a device diagnostics entry point would be separate feature work; do not invent that feature during cleanup.

Preserve the reusable benchmark harnesses and their typed tests. Preserve actual App A4 identity prewarming, cancellation/invalidation, runtime ruleset identity, selection telemetry and first-night publication. Similar names do not imply those live systems are dead.

## Finding 2 — dead Judge forwarding, live durable state

`confirmedDemonSuccessorTarget` appears once in Host, at the Judge signature (281), with no read. App forwards it at 2759, but App also captures/restores it through Recovery and writes it from checkpoint transactions.

Remove only the Judge parameter and its single App call-site argument. Do not delete `clocktowerConfirmedDemonSuccessorTarget`, the checkpoint member, Recovery fields, reducers or tests.

Likewise, after deleting dormant diagnostics, remove only Judge `rulesetRef` and its App forwarding argument. App ruleset authority and its other consumers remain intact.

## Finding 3 — eight unused pure role lookups

Host lines 606–613 declare these names without any subsequent occurrence in the complete file:

```text
firstNightWasherwoman
firstNightLibrarian
firstNightInvestigator
chefPlayer
empathPlayers
fortuneTellerPlayers
poisonerPlayers
butlerPlayers
```

Each initializer only filters cards via `actualClocktowerRoleCards` plus firstOrNull/filter. The helper is a pure filter in ClocktowerHostCoreSemantics.kt and remains used by live Host code and characterization tests. Remove the eight declarations, not the helper or role materializers.

## What is still structurally expensive

Judge's first phase-render branch is Dawn at line 3739. Before it, the function hosts recommendation loading/application effects, registration maps and semantics, night reconstruction, information publication, dynamic scoring and night-step materialization. This mixes common setup with phase-specific work and increases the code a maintainer must understand for a UI change.

This is a source-structure finding, not a measured performance regression. Do not move all this preparation below phase checks: doing so could change effect/cancellation lifetimes, automatic setup application, registration history and private-information publication.

Day composition is not homogeneous either:

- Vote already has typed `ClocktowerTableVoteState` and `commitClocktowerVoteTransaction`; a future rendering/interaction extraction has a relatively stable seam.
- Nomination combines local pair selection with Virgin preflight, registration recording and durable execution callback ordering.
- Slayer still asks for registration recommendations before committing a shot.
- Artist selection depends on recommendation coordinator, dynamic advantage/history and a keyed automatic-answer effect.
- Klutz confirms registration before calling the durable continuation callback and can originate from Night.

Moving those branches into one DayState/Actions bag would hide the existing dependencies. A future extraction must reduce the set of concepts the renderer needs, preserve ownership, and name its effect lifecycle explicitly.

## Ranked follow-up

| Rank | Candidate | Benefit / risk | Decision |
|---|---|---|---|
| 1 | Dormant diagnostics + dead UI inputs/locals | Removes unreachable work, three Host effects and one child effect; low semantic risk with exact proof | D6.2k next |
| 2 | Vote composition / prepared table rendering boundary | Existing typed vote seam; moderate callback/standing-order risk | Investigate after k; no frozen API yet |
| 3 | Common preparation versus phase rendering | Largest improvement to local reasoning; high effect/recommendation lifecycle risk | Separate dependency/effect audit before design |
| 4 | Artist/Slayer/Klutz whole-branch extraction | Visible code movement but shared registration/recommendation coupling remains | Do not mechanically move |
| 5 | Night cursor/Day mode/standing localization | Violates current Recovery or external routing ownership | Reject as a simple UI-local move |

The cleanup is a small preparatory step. Do not spend repeated micro-slices polishing dead imports instead of addressing the live preparation/rendering boundary next.

## D6.2k implementation contract

Re-check live branch/main/PR/checks before writing. Preserve any docs-only descendants and use the actual branch head as parent.

Production allowlist (three files):

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
app/src/main/java/com/codex/campboardgamehost/ClocktowerNightStepUi.kt
```

1. Remove the three Host counter/report/error groups (nine states), their three guarded effects, and `a4DiagnosticAvailable`.
2. Remove Host `debugDiagnosticsExpanded`, its forwarding and the unused child parameter.
3. Remove the child firstNightPool benchmark counter/report/error group and its guarded effect only.
4. Remove Judge `rulesetRef` and `confirmedDemonSuccessorTarget` and only their two App forwarding arguments.
5. Remove the eight proven unused role lookups. Remove only directly orphaned imports within the allowlist where safe; no repository-wide import cleanup.
6. Keep all live recommendation effects/pools, telemetry, information publication, registration maps, A4 services/harnesses/tests, checkpoint/Recovery mechanics and remaining state lifetimes unchanged.

Expected measured postconditions, subject to fresh source verification:

```text
Judge parameters: 89 -> 87
Judge on-callbacks: 34 unchanged
Judge providers: 3 unchanged
Judge MutableState inputs: 5 unchanged
NightStepCardLocalized parameters: 49 -> 48
NightStepCardLocalized on-callbacks: 13 unchanged
App scalar state declarations: 44 unchanged
Removed dormant effects: 4
Removed remembered diagnostic states: 13 (10 Host, 3 child)
```

Use exact anchors/occurrences against complete files; line numbers above are evidence locators for the audited checkpoint, not patch instructions. Stop and re-audit if a writer/consumer appears. No new tests, state wrapper or visibility expansion is justified merely to force RED for deletion.

Validation: exact diff/allowlist/source absence and retained-boundary checks; compile + existing FAST suite and normal CI/R2. Full is not automatically needed immediately after D6.2i's full gate for proven dead deletion. Escalate to FULL if live behavior/effect lifetime, transaction boundaries or coverage uncertainty enters the diff. Typed benchmark harness tests stay in the suite; no tests were found referencing the removed UI counters/dead lookup names in app/src/test or .github. This source search does not replace compilation.

Work local edits remain authorized. If local dependency downloads are still blocked, document that and use remote validation without claiming a local pass. Keep PR #115 DRAFT; do not merge or force-push.
