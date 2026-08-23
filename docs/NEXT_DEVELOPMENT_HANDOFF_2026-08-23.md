# NEXT DEVELOPMENT HANDOFF — 2026-08-23 — A13

> Project: `Jazz0006/CampBoardGameHost`  
> Parent roadmap: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`  
> Project AI instructions: `AGENTS.md`  
> Development operations: `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`  
> Large-file execution: `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`  
> Current task: **PR #43 Clocktower host source decomposition**  
> Immediate next step: **A13 planner-driven Other Night materialization — Chat audits/designs RED first; Luna only executes a later exact GREEN task if required**  
> Status: **CURRENT HANDOFF**

## 1. Trusted state at handoff creation

```text
repository: Jazz0006/CampBoardGameHost
live main: efd63b360ca9aba8c7890594449aa5e21817f560
main source: PR #44 correctness hotfix merge

PR: #43 — Refactor: decompose Clocktower host monolith
branch: codex/source-decomposition-clocktower-host
state: DRAFT / OPEN / NOT MERGED
last fully validated production head: 854c2464d8a742ba0438fa700bdd2848aa88f4cf
A12 CI #530: SUCCESS
A12 R2 #469: SUCCESS
A12 ASP: SUCCESS
A12 Real Clingo: SUCCESS
```

This handoff itself advances only documentation. New sessions must query the live PR/head/checks before doing any implementation and must not assume the documentation commit is still the branch tip.

## 2. Working model — mandatory unless user changes it

```text
ChatGPT / Chat
  -> live-state audit
  -> architecture and risk decisions
  -> slice boundaries
  -> tests-first / characterization design
  -> exact implementation specification
  -> remote diff and CI review

Codex / Luna
  -> constrained executor for large/local mechanical edits
  -> local validation
  -> commit/push only when instructed
```

Do not ask Luna to independently redesign, select decomposition boundaries, or perform the final architecture audit.

Use the GitHub connector for small/source-contract/doc edits when safe. For the large Host, prefer a Chat-authored exact task followed by Luna local execution.

Mac/Codex Gradle:

```bash
GRADLE_USER_HOME="$PWD/.gradle-codex"
```

Keep `.gradle-codex/` untracked.

## 3. What is already complete in PR #43

```text
A1  Core semantics owner
A2  Selection semantics owner
A3  Presentation models owner
A4  Recommendation screen/reason UI
A5  Recommendation card/editor UI
A6  Player display UI
A7  Spy/Recluse registration UI
A8  Night-step presentation UI
A9  Unreachable legacy fallback cleanup
A10 Generic stateless information-step packaging
A11 Night Step Materialization registry + production interactions seams
A11.1 integrate PR #44 actual-role correctness baseline
A12 planner-first / lazy First Night materialization
```

Important A11/A12 evidence:

```text
A11 RED:                   19bdaf5525c4979f36d44ed0213c0b3c60f4ff7d
A11 GREEN:                 c893300b8d8dbc7ea845849b81416259da32d485
A11 CI #520 / R2 #460:     SUCCESS / SUCCESS

PR #44 merge:              efd63b360ca9aba8c7890594449aa5e21817f560
A11.1 integration:         4eaa9863070b1eee571169bde737b249379e28ee
A11.1 CI #526 / R2 #465:   SUCCESS / SUCCESS

A12 RED:                   43f64fc6b2123a35bd9e89b3f6120a8adb7ec809
A12 expected RED CI #528:  FAILURE
A12 legacy test migration: 3715c5428b52bcce87781fb48ab715338227e19f
A12 GREEN:                 854c2464d8a742ba0438fa700bdd2848aa88f4cf
A12 exact GREEN diff:      ClocktowerHostScreen.kt only from 3715c542...
A12 CI #530 / R2 #469:     SUCCESS / SUCCESS
A12 ASP / Real Clingo:     SUCCESS / SUCCESS
```

## 4. Correctness prerequisite already resolved — Drunk shown Fortune Teller

Do not regress this boundary.

Production intentionally separates:

```text
firstNightWakingRoleIds
  = actual roles + Drunk shown role

firstNightActualRoleIds
  = actual roles only
```

Drunk shown Fortune Teller still wakes/acts at the Fortune Teller slot, but does not receive a real Fortune Teller Red Herring setup ability. `STORYTELLER_SETUP` is filtered against actual roles.

Do not merge these sets or generalize them away during A13.

## 5. A12 final architecture state

First Night is now production planner-first:

```text
ValidatedClocktowerRuleset
        ↓
ClocktowerProductionFirstNightFlow.interactions(...)
        ↓
ClocktowerHostInteractionProjector
        ↓
ClocktowerNightStepMaterializerRegistry(FIRST_NIGHT)
        ↓
only projected actionable interactions are lazily materialized
```

First Night no longer routes eager `filteredNightSteps` through `ClocktowerProductionFirstNightFlow.order(...)`.

The registry remains stateless and non-Compose. Its builder contract is ordinary:

```kotlin
() -> ClocktowerNightStepUi
```

Do not make it `@Composable`.

A12 intentionally left First Night materializer lambdas in the Host because extracting them would require a large context bag containing derived facts, recommendation helpers and registration state. File-size reduction is not worth architecture-negative ownership.

`ClocktowerHostScreen.kt` after A12: approximately 294,922 bytes.

## 6. Current Other Night transitional path — A13 target

Other Night still behaves as:

```text
Host eagerly constructs supported Other Night ClocktowerNightStepUi
        ↓
unfilteredNightSteps / filteredNightSteps
        ↓
ClocktowerProductionOtherNightFlow.order(
    ruleset,
    wakingRoleIds,
    resolvedFacts,
    productionSteps = filteredNightSteps,
    identityOf = ...
)
        ↓
planner-backed exact match / reorder
```

R5.5 already owns Other Night flow existence/order through planner/projector. A13 only closes the final materialization gap.

Target:

```text
ruleset + wakingRoleIds + resolvedFacts
        ↓
ClocktowerProductionOtherNightFlow.interactions(...)
        ↓
ClocktowerNightStepMaterializerRegistry(OTHER_NIGHT)
        ↓
lazy materialize projected interactions
        ↓
ClocktowerNightStepUi
```

Do not redesign `ClocktowerFlowPlanner`, `ClocktowerHostInteractionProjector`, registry semantics, or stable interaction IDs unless a new audit proves an actual missing seam.

## 7. A13 first action — audit before RED

The new Chat must first read live source and enumerate the exact currently supported Other Night identities. Do not rely only on the following remembered candidate list:

```text
Poisoner
Butler
Empath
Chambermaid
Fortune Teller
Undertaker
Monk
Imp / DemonKill
new Demon identity
Demon succession
Mayor redirect
Sage
Ravenkeeper
Spy
```

Audit these sources before deciding RED:

- `ClocktowerHostScreen.kt` Other Night eager `buildList`;
- `ClocktowerProductionOtherNightFlow.kt`;
- `ClocktowerProductionNightStepIdentity.kt`;
- `ClocktowerHostInteractionProjector.kt` conditional/event projection;
- existing Other Night wiring/source-contract tests;
- `ClocktowerLegacyPlannerDifferentialTest.kt` and any other test still asserting the transitional `.order()` source call;
- `advanceNightStep` and related callback/transaction tests.

Special attention: identify all event interactions and whether any step existence currently also depends on Host conditions in addition to projector-resolved facts. If so, determine whether that is redundant, intentional UI behavior, or a correctness gap before writing GREEN.

## 8. A13 tests-first intent

A13 RED should lock production authority, not file size.

Expected contract direction, subject to live-source audit:

- Other Night must call `ClocktowerProductionOtherNightFlow.interactions(...)`;
- it must pass the existing `otherNightWakingRoleIds` and `otherNightResolvedFacts` through the canonical production seam;
- Other Night must use `ClocktowerNightStepMaterializerRegistry` with `ClocktowerNightFlowPhase.OTHER_NIGHT`;
- `materialize(projectedInteractions)` must determine UI step existence/order;
- Other Night must no longer call `ClocktowerProductionOtherNightFlow.order(...)` from production Host;
- Other Night must no longer pass `productionSteps = filteredNightSteps` / `identityOf = ...`;
- First Night A12 planner-first path must remain unchanged;
- resolved conditional/event interaction order must remain projector/planner-authoritative;
- missing/duplicate materializer fail-closed behavior remains registry-owned;
- `advanceNightStep` transaction remains Host-owned.

Before committing RED, proactively migrate any test whose only obsolete requirement is “production must contain OtherNightFlow.order()” while preserving the deeper canonical planner ownership assertion. Do not create a test contradiction like the one encountered in A12.

## 9. High-risk Host ownership — DO NOT MOVE IN A13

The following stay in `ClocktowerJudgeScreen`:

- Compose `remember` state and `LaunchedEffect` lifecycle;
- recommendationCoordinator and telemetry recorder lifetime;
- Spy/Recluse registration mutable state/maps;
- all derived facts used by recommendation/information unless a tiny pure value can be captured safely;
- player display lifecycle and information observation ordering;
- history/session authority;
- phase routing outside the Other Night materialization seam;
- all day UI routing;
- A3 historical product work.

Most important: **do not move `advanceNightStep`.**

Preserve its exact transactional ordering, including relevant operations such as:

```text
confirm poison / monk / demon draft
-> mayor / successor resolution audit
-> registration recording
-> semantic event recording
-> step index / finalization
```

Read the live method and tests for exact ordering before A13 GREEN; the list above is a conceptual boundary, not a substitute for source.

## 10. Compose/resource rule for A13

Registry builders are not composable. If any Other Night step constructor directly calls `stringResource()`, do exactly what A12 did:

- resolve only required Strings in surrounding Compose scope;
- capture plain Strings in lazy builders;
- keep the `ClocktowerNightStepUi` constructor itself lazy;
- do not make registry/materialize composable;
- do not substitute `context.getString()` simply to bypass the constraint;
- do not alter resource IDs, format arguments or UI copy.

## 11. A13 GREEN expected scope discipline

The ideal A13 production GREEN should be as narrow as possible and is expected to primarily touch the large `ClocktowerHostScreen.kt`.

Do not automatically edit Flow/Projector/Registry APIs. If the live audit reveals a truly missing production seam, stop and make a separate architecture decision before GREEN.

For each Other Night materializer, compare old eager constructor vs new `build = { ... }` field-by-field. Except for lambda wrapping and necessary resource precomputation, semantics must stay identical.

Do not chase the 50 KiB guideline during A13.

## 12. Validation expectations

After a valid RED and GREEN:

```text
focused A13 wiring tests
+ materializer registry tests
+ OtherNight flow/projector tests
+ conditional event tests
+ transaction/advanceNightStep characterization
+ First Night A12 regression tests
```

Then:

```bash
GRADLE_USER_HOME="$PWD/.gradle-codex" ./gradlew :app:testDebugUnitTest
GRADLE_USER_HOME="$PWD/.gradle-codex" ./gradlew :app:assembleDebug
git diff --check
```

Run standard ASP checks locally if available; Real Clingo can be satisfied remotely if unavailable locally. Final acceptance requires GitHub CI / ASP / Real Clingo / R2 and Chat remote exact-diff audit.

## 13. Stop after A13 GREEN audit

Do not automatically start A14.

After A13 is fully green:

1. re-measure Host;
2. audit responsibility cohesion;
3. decide whether any natural low-coupling extraction exists;
4. decide whether optional A14 day routing has enough value;
5. otherwise prepare PR #43 final review/merge-readiness audit.

A14 is optional. File size is a soft guideline, not a merge gate.

## 14. Product work after PR #43

Only after PR #43 is complete, final-audited, and explicitly authorized for merge should product development proceed to:

```text
A3 historical multi-night exact baseline
using EnumeratedWorldSet
```

Do not mix A3, B4/ZDD production promotion, history UI redesign, misinformation expansion, or broader manual UI into PR #43.

## 15. New-conversation startup checklist

1. Read `AGENTS.md`.
2. Read `docs/README.md`.
3. Read `docs/SINGLE_DEVELOPER_GITHUB_CONNECTOR_WORKFLOW.md`.
4. Read `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`.
5. Read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.
6. Read this handoff.
7. Query live `main`, PR #43 state/head, and current checks.
8. Verify no unreviewed code commit appeared after the A12 validated head/documentation checkpoint.
9. Audit exact Other Night identities and existing source-contract tests.
10. Design and establish A13 RED before any production GREEN.
11. Keep PR #43 draft/unmerged.
12. Do not merge without explicit user authorization.