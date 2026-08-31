# CampBoardGameHost — TBSP-6G-B Production Wiring Handoff

> Prepared: 2026-08-30 Australia/Sydney  
> Resume date: 2026-08-31  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR: #57 — OPEN / DRAFT / NOT MERGED  
> Active slice: **TBSP-6G-B App/Host production wiring**

## 1. Live/checkpoint state at handoff preparation

Verified repository state before this handoff file was added:

```text
live main:
0eafa9770ca9391928419dadf835f17a1ab00d29

main meaning:
PR #59 merged — repository-wide risk-based / evidence-driven testing policy
and historical structural/source-test debt cleanup

PR #57 head before this handoff doc:
8e72854c7a6a9df79825f571cf77f8bf3d598c04

latest code/test audit checkpoint before docs-only handoff work:
d6f73e8b05152cffa1a6fc220af438d2836275d8

testing-policy main sync merge into PR #57:
df3aa27553224bf2bbe2d5303961aecda083c239

source-only 6G-B RED retirement:
ba01e844f66ec76973db272354ab62d27142d8b1
```

Any later commit that only updates this handoff / PR metadata is a **docs-only carrier**. Re-query live GitHub state before implementation.

PR #57 must remain Draft. Do not merge or mark Ready without explicit user authorization.

## 2. Testing policy change that governs this slice

Root `AGENTS.md` now has higher-precedence risk-based rules:

```text
new/changed stable behavior with a real coverage gap
-> meaningful typed RED first

internal wiring/refactor where stable typed coverage already exists
-> existing GREEN evidence
-> production implementation
-> focused validation / compile / diff
```

Do **not** recreate a source-string RED merely to force App/Host wiring through RED/GREEN ceremony.

The obsolete test was:

```text
TroubleBrewingSetupRecommendationProductionWiringTest.kt
```

It was deleted because it asserted exact source call spelling rather than adding a stable contract.

PR #57 test audit is recorded in:

```text
docs/TBSP_PR57_TEST_AUDIT_2026-08-30.md
```

## 3. Accepted TBSP progress

Completed and accepted before the current slice:

```text
TBSP-1  frozen preset asset/parser/validator                   COMPLETE
TBSP-2  deterministic history-aware selector                  COMPLETE
TBSP-3  deterministic exact deal materialization              COMPLETE
TBSP-4  recommendation identity lock                          COMPLETE
TBSP-5  durable cross-game rotation history                   COMPLETE
TBSP-6A active setup provenance codec                         COMPLETE
TBSP-6B production setup preparer                             COMPLETE
TBSP-6C deal role resolver                                    COMPLETE
TBSP-6D Trouble Brewing production start cutover              COMPLETE
TBSP-6E active-game provenance persist/restore                COMPLETE
TBSP-6F true-completion rotation-history wiring               COMPLETE
TBSP-6G-A exact-request setup recommendation prewarm core      GREEN
TBSP-6G-B typed reveal/prewarm lifecycle seam                 GREEN
TBSP-6G-B App/Host production wiring                          CURRENT
```

Historical typed checkpoints already recorded in PR #57 metadata:

```text
6G-A typed GREEN:
3bff3a7241dc7b47140d1d1ec3889fb2813fca42

6G-B typed lifecycle GREEN:
7d34b217768e0669824c61bfb7945866ea2bfcaf
```

## 4. Stable typed contracts already in place

### `TroubleBrewingSetupRecommendationPrewarmCoordinator`

Current contract:

```text
same SetupCoordinationRequest
-> build once
-> prewarm reuses exact ready result
-> readyFor(same request) hits

changed request
-> readyFor misses
-> prewarm rebuilds
-> old request no longer counts as ready
```

Owning test:

```text
clocktower/session/TroubleBrewingSetupRecommendationPrewarmCoordinatorTest.kt
```

### `TroubleBrewingSetupRecommendationRevealCoordinator`

Current contract:

```text
onCommittedDeal(request)
-> enterReveal() first
-> dispatch background work second
-> recommendation build does not run synchronously during dispatch

resultFor(exact ready request)
-> reuse exact result

resultFor(miss/stale request)
-> safe fallback computation
```

Owning test:

```text
clocktower/session/TroubleBrewingSetupRecommendationRevealCoordinatorTest.kt
```

These tests are the primary 6G-B behavior evidence. Production wiring should consume them rather than create another testing-only seam.

## 5. Current production anchors

### App start path

`CampBoardGameHostApp.kt` currently has:

```text
startTroubleBrewingGame()
-> load/parse final dataset
-> load TB rotation history
-> TroubleBrewingProductionSetupPreparer.prepare(...)
-> TroubleBrewingDealRoleResolver.resolve(...)
-> commit PlayerCards
-> resetDealState(...)
-> committedTroubleBrewingSetupSelection = preparedSetup.selection
```

`resetDealState(...)` already performs the existing reveal transition:

```text
resetClocktowerFlow()
-> screen = Screen.PassPhone
-> persistActiveGameStateIfNeeded()
```

Therefore do not invent a second reveal UI path. Use the existing reset operation as the `enterReveal` callback owned by `TroubleBrewingSetupRecommendationRevealCoordinator`.

### Existing App lifecycle precedent

Near the root Compose state, App already has remembered A4 lifecycle owners such as:

```text
val a4ShadowWorldSetCache = remember { ... }
val a4IdentityRevealPrewarmer = remember(...) { ... }
```

The TBSP prewarm/reveal coordinators should be remembered at the same application-lifecycle level rather than recreated on every recomposition.

### Judge setup recommendation path

`ClocktowerJudgeScreen` currently:

```text
val recommendationCoordinator = remember(gameSeed) { ClocktowerRecommendationCoordinator() }
...
LaunchedEffect(recommendationKey, lockedRecommendationDecisions) {
    onInitialRecommendationDemand()
    recommendationUiState = Loading
    val result = withContext(Dispatchers.Default) {
        runCatching {
            recommendationCoordinator.recommendSetup(
                SetupCoordinationRequest(...)
            )
        }
    }
    ...
}
```

The existing `withContext(Dispatchers.Default)` is important: a cache miss may call `resultFor(request)`, which falls back to `prewarm(request)`. That fallback must remain off the UI/main thread.

## 6. Exact 6G-B production behavior to implement

Required behavior:

```text
committed Trouble Brewing deal
-> enter existing PassPhone/RevealCard path immediately
-> dispatch exact setup recommendation request on Dispatchers.Default during reveal
-> do not mutate committed actual/shown identities

Judge first setup recommendation consumer
-> constructs its normal SetupCoordinationRequest
-> exact request hit: consume ready prewarm result
-> miss/stale request: compute through safe existing path on Dispatchers.Default
```

The request used for prewarm and the initial Judge request must be structurally equal:

```text
game:
committed TB PlayerCards -> toClocktowerGameState(TroubleBrewing, preparedSeed)

roles:
clocktowerRoleDefinitionsForScript(TroubleBrewing)

lockedDecisions:
selector/deal-owned Drunk shown identity only
(use TroubleBrewingSetupRecommendationLock / equivalent existing committed-identity representation)

history:
gameHistory.toClocktowerSetupHistory()
```

At initial Judge entry, poison target is expected to be null; do not broaden this slice into First Night Poisoner lifecycle.

## 7. Required production file allowlist

Production implementation should be limited to:

```text
app/src/main/java/com/codex/campboardgamehost/CampBoardGameHostApp.kt
app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt
```

Do not modify the typed coordinator classes unless live signatures materially differ from this handoff.

Because both target production files are large, root `AGENTS.md` says **do not perform unsafe whole-file replacement through the GitHub connector**. Use Luna/local Codex exact patch with a complete worktree.

## 8. Deterministic implementation shape for Luna/local Codex

Before editing, confirm branch HEAD and the exact anchors above. If materially different, stop and report rather than inventing a new design.

### `CampBoardGameHostApp.kt`

Make the minimum wiring changes:

1. import/use `rememberCoroutineScope` if not already present;
2. import the two TBSP coordinator classes;
3. create one remembered `TroubleBrewingSetupRecommendationPrewarmCoordinator` whose build function delegates to the existing setup recommendation behavior (`ClocktowerRecommendationCoordinator().recommendSetup(request)` or an equivalently existing stable owner — do not alter recommendation semantics);
4. create one remembered `TroubleBrewingSetupRecommendationRevealCoordinator` from that prewarmer;
5. in `startTroubleBrewingGame()`, after committed cards are available, build the exact initial `SetupCoordinationRequest` from the committed cards / prepared seed / TB role definitions / committed Drunk identity lock / current setup history;
6. replace the direct final reset+selection sequence with `onCommittedDeal(...)` so that:
   - `enterReveal` performs the existing `resetDealState(...)` and rebinds `committedTroubleBrewingSetupSelection = preparedSetup.selection`;
   - `launchBackground` dispatches the supplied work through the remembered coroutine scope on `Dispatchers.Default`;
7. pass the reveal coordinator's exact-request result provider to `ClocktowerJudgeScreen` for Trouble Brewing only; other scripts retain existing behavior.

Do not change:

```text
preset selection
seat shuffle
game seed
actual roles
shown roles
Drunk selected shown role
No Greater Joy setup path
A4/ZDD lifecycle
First Night world computation
```

### `ClocktowerHostScreen.kt`

Make the minimum consumer change:

1. add an optional setup-recommendation result provider parameter to `ClocktowerJudgeScreen` using the existing `SetupCoordinationRequest` and constrained-result type;
2. keep the existing recommendation request construction semantically unchanged;
3. assign that request to a local value once;
4. inside the existing `withContext(Dispatchers.Default)` / `runCatching` block:
   - call the supplied result provider when present;
   - otherwise call the existing `recommendationCoordinator.recommendSetup(request)` path;
5. do not alter recommendation UI state, application semantics, lock behavior, re-evaluation behavior, or non-TB scripts.

No new source-string test is required for these edits.

## 9. Evidence and validation for tomorrow

Before production edit, existing typed tests are the baseline evidence. Do not manufacture RED.

After production wiring run at minimum:

```text
./gradlew :app:testFast --tests '*TroubleBrewingSetupRecommendationPrewarmCoordinatorTest' --rerun-tasks
./gradlew :app:testFast --tests '*TroubleBrewingSetupRecommendationRevealCoordinatorTest' --rerun-tasks
./gradlew :app:testFast --tests '*TroubleBrewingSetupRecommendationLockTest' --rerun-tasks
```

If the repository test task syntax requires the underlying test task rather than `testFast --tests`, use the existing project-supported focused command, but do not change test semantics.

Then:

```text
git diff --check
Android compile / focused test compilation
:app:testFast at the logical 6G-B checkpoint
```

After push, ChatGPT must independently verify:

```text
expected parent
exact two-production-file allowlist plus intentional docs/tests only
no source-wiring RED recreation
exact request construction matches Judge consumer
background dispatch is off-main
fallback remains inside Dispatchers.Default
no identity mutation/reroll
PR #57 still Draft
R2 / GitHub CI checkpoint
```

## 10. PR #57 test audit result

Risk-based review completed on 2026-08-30.

Retired:

```text
TroubleBrewingSetupRecommendationProductionWiringTest.kt
```

Narrowed to coarse App-root final-boundary guards:

```text
TroubleBrewingProductionStartWiringTest.kt
TroubleBrewingActiveGameProvenanceWiringTest.kt
TroubleBrewingCompletionRotationHistoryWiringTest.kt
```

Retained as durable typed behavior tests:

```text
TroubleBrewingSetupRecommendationLockTest
TroubleBrewingSetupRecommendationPrewarmCoordinatorTest
TroubleBrewingSetupRecommendationRevealCoordinatorTest
TroubleBrewingDealRoleResolverTest
TroubleBrewingProductionSetupPreparerTest
TroubleBrewingSetupDealPlannerTest
TroubleBrewingSetupPresetJsonTest
TroubleBrewingSetupPresetRotationScorerTest
TroubleBrewingSetupPresetSelectorTest
TroubleBrewingSetupPresetValidatorTest
TroubleBrewingSetupProvenancePersistenceTest
TroubleBrewingSetupRotationHistoryStoreTest
```

See `docs/TBSP_PR57_TEST_AUDIT_2026-08-30.md`.

## 11. After 6G-B

Do not immediately broaden the production patch.

Once 6G-B is independently GREEN and remotely audited, next sequence remains:

```text
6H — First Night background precompute / ready-or-safely-await lifecycle
6I — cutover acceptance matrix
6J — dormant cutover API cleanup
6K — final full acceptance
```

6H is **not** SetupRecommendationService prewarming. It is a separate first-night information/world-computation lifecycle.

Still out of scope unless separately authorized:

```text
A3 immutable setup snapshot
A4/ZDD redesign
Mayor redirect redesign
Imp succession redesign
No Greater Joy behavior changes
broad App/Host decomposition
preset dataset regeneration
rotation/recommendation weight retuning
```

Preserve accepted Dawn/Dusk poison exactly-once behavior.

## 12. New-window resume instruction

Use this in a new chat:

```text
请读取根目录 `AGENTS.md`、`docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-31_TBSP_6G_B_PRODUCTION_WIRING.md`、`docs/TBSP_PR57_TEST_AUDIT_2026-08-30.md`、`docs/TESTING_STRATEGY.md` 和 `docs/TBSP_PRODUCTION_CUTOVER_CONTRACT_V1.md`。重新确认 live `main`、Draft PR #57 当前 head/state/checks，并区分 latest code/test checkpoint 与其上的 docs-only commits。当前 6G-A typed prewarm core 和 6G-B typed reveal lifecycle 已 GREEN；旧的 6G-B source-string production RED 已按 risk-based policy 删除。请从 **TBSP-6G-B App/Host production wiring** 继续：不要创建新的 source RED；使用现有 typed tests 作为 baseline evidence；大文件 `CampBoardGameHostApp.kt` / `ClocktowerHostScreen.kt` 用 Luna/local Codex exact patch；实现 committed TB deal -> existing PassPhone reveal -> Dispatchers.Default background prewarm，以及 Judge exact-request ready reuse / miss safe fallback。完成 focused validation、`:app:testFast`、exact diff audit 和 GitHub CI/R2 后停止，不要进入 6H，不要 merge 或 mark Ready。
```
