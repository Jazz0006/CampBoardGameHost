# D6.2z — Final PR diff / ownership review

> Date: 2026-09-09 Australia/Sydney  
> Status: COMPLETE / DIFF + OWNERSHIP PASS / FINAL FULL + REAL-DEVICE GATES PENDING  
> Branch: `codex/d6-2-ui-composition`; PR #115 OPEN / DRAFT / SCOPE FROZEN.  
> PR base: `d76b0854d58e7a0abc3bb6ac6ab53b061fcb871e`.  
> Latest production checkpoint: `a692cc722f1e597e747154bf05a2689fee9bed4c`.  
> Final validated production-equivalent code/test head: `b2263cd08bc2ce223598698324bf2b22243c91f2`.  
> D6.2y acceptance checkpoint: `4fa857105304d85500e7932241f3fd6758a7a892`.  
> Roadmap sync before this review: `322f144aa3c1a8340521762a5bac8bece60528ae`.

## Decision

The complete PR #115 changed-file set passes the final **diff/ownership review**.

No changed production file introduces a second durable state owner, broad Controller/ViewModel/context bag, hidden Recovery authority, new persistence writer, new recommendation lifecycle owner, or new Compose dependency in session/domain code.

The PR remains **not merge-ready** only because the final acceptance gates are incomplete:

1. a fresh FULL/T4 has not yet executed against the `b2263cd...`-equivalent production tree after D6.2m–s;
2. critical real-device coverage has not been performed or explicitly waived.

Do not reopen R0–R2 decomposition to fill this gap. Do not modify production or CI routing merely to manufacture a full run. Do not start R3 inside PR #115.

## 1. Changed-file classification

The PR currently changes one R2 workflow file, nine production Kotlin files, four test Kotlin files, and the D6 audit/progress/handoff documentation set.

### Workflow

- `.github/workflows/r2-write-probe.yml`

### Production

- `CampBoardGameHostApp.kt`
- `ClocktowerAppModels.kt`
- `ClocktowerChambermaidPresentationSemantics.kt`
- `ClocktowerChambermaidStepMaterializer.kt`
- `ClocktowerNightStepUi.kt`
- `ClocktowerNumericInformationOptionPreparation.kt`
- `clocktower/ui/ClocktowerDayScreen.kt`
- `clocktower/ui/ClocktowerHistoryScreen.kt`
- `clocktower/ui/ClocktowerHostScreen.kt`

### Tests

- `ClocktowerChambermaidPresentationSemanticsTest.kt`
- `ClocktowerChambermaidStepMaterializerTest.kt`
- `ClocktowerNumericInformationOptionPreparationTest.kt`
- `StructuredEmpathInformationAdapterTest.kt`

Everything after `b2263cd...` through the D6.2y/roadmap closeout is documentation-only. Therefore the production tree requiring final T4 is stable and unambiguous.

## 2. Workflow review — PASS

The sole workflow change retires only obsolete R2 source-existence assertions that referred to UI declarations deleted after reachability/ownership proof:

- old Day `ClocktowerNominationScreen`;
- old Day `ClocktowerVoteScreen`;
- old `ClocktowerSpecialDayActionScreen`;
- old History `ClocktowerGameRecordPanel`;
- old History `ClocktowerPlayerStatusRow`;
- old History `ClocktowerTimelineRow`.

The workflow continues to assert the retained live boundaries, including Dawn/Execution and Results/phase-label ownership.

This change does **not** weaken or alter Android test execution, ASP/Clingo routing, CI gate behavior, production build commands or persistence/recovery validation. It only stops enforcing source shapes that no longer exist by design.

## 3. `ClocktowerAppModels.kt` — PASS

The production diff removes only writerless `ClocktowerDayMode.ExecutionResult`.

Its removal follows the earlier reachability proof and legacy-tail deletion. No replacement state or route is introduced.

## 4. `ClocktowerDayScreen.kt` / `ClocktowerHistoryScreen.kt` — PASS

These are deletion-only cleanup slices already exercised by the D6.2l FULL gate.

### Day

Removed unused legacy declarations:

- `ClocktowerNominationScreen`;
- `ClocktowerVoteScreen`;
- `ClocktowerSpecialDayActionScreen`;
- directly orphaned imports.

Retained live Day declarations such as Dawn summary and execution confirmation remain in place.

### History

Removed unused legacy declarations:

- `ClocktowerGameRecordPanel`;
- `ClocktowerTimelineRow`;
- `ClocktowerPlayerStatusRow`;
- directly orphaned imports.

Retained live results and event-phase presentation remain in place.

The workflow change above is exactly the corresponding retirement of obsolete source-shape assertions.

## 5. `ClocktowerNightStepUi.kt` — PASS

The PR change is bounded cleanup:

- removes the permanently untriggered unified first-night pool device-benchmark effect/state;
- removes the dead `debugDiagnosticsExpanded` input and orphaned imports.

It does not rewrite the active structured-number, structured-Boolean, Fortune Teller, pair-information, registration, single-target, recommendation-selection, publication or navigation flows.

The later D6.2u/v audits correctly stopped further extraction because the residual file is already roughly 45.7 KiB and mainly routes specialized typed interaction owners.

## 6. `ClocktowerNumericInformationOptionPreparation.kt` — PASS

This new 55-line owner is a genuine pure seam.

It owns only:

- parsing the previous unreliable number from existing event presentation/history;
- projecting typed number recommendations into `ClocktowerDisplayOption` values.

Its dependencies are narrow recommendation/proposition/display/event inputs. It owns no Compose state, session state, Recovery, persistence, telemetry, durable publication or recommendation invocation.

The Host still builds the recommendation context and invokes the recommendation coordinator. This extraction therefore reduces duplicated projection logic without moving lifecycle authority.

## 7. Chambermaid presentation/materializer — PASS

### `ClocktowerChambermaidPresentationSemantics.kt`

Adds an immutable `ClocktowerChambermaidSelectionPresentation` containing only the selected target names and current subject seats, with typed proposition validation.

The compatibility proposition helper delegates through this presentation value rather than creating another rule owner.

### `ClocktowerChambermaidStepMaterializer.kt`

The new 50-line materializer owns only:

- Chambermaid localized step content;
- the exact `ClocktowerInformationStepBuilder` assembly;
- stable Chambermaid production identity.

The materializer receives a lazy display-option provider. It does not receive or own:

- Compose state;
- `ClocktowerGameSession`;
- Recovery/persistence;
- recommendation coordinator invocation;
- history lookup;
- selection telemetry;
- publication callbacks;
- broad night context.

The Host creates one materializer and reuses it in the two canonical phase registries, replacing two equivalent Chambermaid closures.

D6.2t correctly rejected extrapolating this into a generic Clockmaker/Chef/Empath materializer family.

## 8. `CampBoardGameHostApp.kt` — PASS

The App diff is primarily ownership deletion and dead private-code removal:

- removes the isolated private JSON decoder subgraph that had no live caller;
- removes unused random `clocktowerRolesFor(playerCount)`;
- removes App-owned transient Artist, Slayer, nomination and pending-vote-count state/reset/forwarding;
- removes dead Judge forwarding such as `records`, diagnostics-only `rulesetRef`, `confirmedDemonSuccessorTarget`, `onPhaseChange` and `onShowResults`;
- replaces the Artist callback family with one typed `onConfirmArtistQuestion(claimantName, truthfulAnswer, shownAnswer)` durable boundary.

The durable Artist side effects remain App-owned: claim tracking, `artistUsed`, record/event chronology, Day routing and game-state revision advance.

The App still owns the high-risk cross-lifetime transaction-application responsibilities that were intentionally deferred to R3. No attempt was made to hide them in a new `AppState`, `Actions`, transaction context or callback bag.

## 9. `ClocktowerHostScreen.kt` — PASS

This is the largest diff and received the closest ownership review.

### Dead/unreachable removal

The PR removes:

- dormant diagnostic benchmark/probe state and effects;
- diagnostics-only inputs/imports;
- unused pure role lookups;
- dead phase/result/plumbing variables and callbacks;
- the entire already-proven-unreachable legacy Storyteller tail.

The removed diagnostic effects were not the live App-level A4 identity/cache prewarming paths. Live A4 prewarming/cache behavior remains external and unchanged.

### Transient state localization

The Host/Judge now owns truly transient interaction values with appropriate Compose lifetime:

- nomination pair: `remember(gameId, round)`;
- Slayer claimant/target: `remember(gameId)`;
- Artist claimant/truthful/shown answer: `remember(gameId)`.

Durable commit callbacks remain external.

### Vote boundary

The old local vote-count helper/state is removed from the residual flow. The live square-table vote path uses the typed table state and `commitClocktowerVoteTransaction` boundary reviewed in D6.2w.

### Numeric information

Host still owns recommendation invocation and role-specific context. Only pure previous-number parsing/display-option projection moved to the numeric helper.

### Chambermaid

Two phase-local assembly closures are replaced by one role-local materializer while the lazy recommendation provider and all recommendation/history/style lifecycle remain in Host.

### Legacy tail

The large trailing HostScriptCard-style Storyteller composition was deleted only after D6.2e proved every legal current phase/day/night state returned through the modern/table path. The deletion therefore removes an unreachable second presentation implementation instead of replacing live behavior.

No new broad owner was introduced in exchange for this reduction.

## 10. Test diff — PASS

The test additions are aligned with the newly extracted typed seams:

- Chambermaid selection/presentation proposition semantics;
- Chambermaid materializer fields, placeholder behavior and lazy provider behavior;
- numeric history parsing and display projection metadata.

The `StructuredEmpathInformationAdapterTest` source-boundary correction changes only the source slice marker after the Chambermaid literal moved out of Host; it does not restore the obsolete Host source shape or alter the original Empath assertion.

## 11. No post-validation source drift

A compare from final validated code/test head `b2263cd...` to the current documentation closeout shows only documentation changes.

No production Kotlin, test Kotlin or workflow file changed after `b2263cd...`.

Therefore the pending final FULL/T4 can target the exact `b2263cd...` production-equivalent tree without ambiguity.

## 12. Final acceptance hold

### Already satisfied

- R0–R2 structural acceptance: **PASS**;
- final PR diff/ownership review: **PASS**;
- final code/test FAST evidence: **PASS**;
- final code/test R2 evidence: **PASS**;
- no broad replacement state/action/context bag: **PASS**;
- no production drift after final validated code/test head: **PASS**.

### Still required

- fresh FULL/T4 on the `b2263cd...`-equivalent production tree: **PENDING**;
- real-device critical Storyteller path: **PENDING**, unless explicitly waived by user/project policy.

The connected GitHub capability available in this session can inspect and rerun existing workflow runs, but it does not expose an action to dispatch a fresh workflow run for this ref. Re-running the older D6.2l FULL would execute against the old commit and would not satisfy this gate.

Do not create a fake production/workflow change merely to trigger FULL. The missing gate remains an explicit external/manual acceptance action.

## 13. R3 remains blocked from this PR

The current App still contains deep transaction application, but the rules/planning side is already substantially typed:

- `NightCheckpointHostTransaction` emits checkpoint + revision intent without durable side effects;
- `NightDawnResolutionPlanner` returns pure continuation/checkpoint/`DawnCommitIntent`;
- `NightDawnDurableMaterializationPlanner` produces a pure exactly-once materialization plan with stable IDs for partial-persistence repair.

If D6 continues after PR #115 is accepted/merged, start a new branch and perform a read-only R3 viability audit of the **application sequencing** around these plans. Do not add another rules resolver and do not move the current App callback wholesale.

## Exit status

```text
D6.2 R0–R2 structural acceptance      PASS
D6.2 final PR diff / ownership review PASS
Current PR decomposition scope         FROZEN
Fresh final FULL/T4                    PENDING
Real-device critical path              PENDING / no claim
Merge-ready recommendation             HOLD
Merge authorization                    NOT GRANTED
R3 implementation                      NOT STARTED / NOT ON THIS PR
```
