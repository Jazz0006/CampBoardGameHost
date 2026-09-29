# DLB-5H1 First-Night Evil-Information Presentation Boundary Audit — 2026-09-30

> Status: **IMPLEMENTATION AUDIT / H1 GO**
> Baseline: `main@f56677ce4e92898504ba8039ea2766c9bdec336e`
> Scope: first-night Minion/Demon presentation extraction only. No recommendation-policy change, no setup legality change, no dependency-timing change, no session-state ownership change.

## 1. Decision

DLB-5H1 is a **GO** as a narrow behavior-preserving presentation extraction.

The current implementation already has stable upstream owners for every non-presentation responsibility:

- `ClocktowerProductionFirstNightFlow` + dependency projection/planner own first-night interaction order and latest-safe barriers.
- `demonBluffRoleNamesToCommitAtBarrier` owns the Demon-bluff commit-at-barrier decision.
- `resolveDemonBluffPresentation` consumes the already-committed role-name triple and fails closed for pending/invalid presentation state.
- `ClocktowerGameSession` / current production state remain canonical truth owners.
- `ClocktowerEvilInfoSquareTablePresentation` owns the read-only square-table renderer/surface after a `ClocktowerNightStepUi` already exists.

The remaining mixed responsibility is localized Minion/Demon information content assembly inside `ClocktowerHostScreen`.

## 2. Current mixed block

`ClocktowerHostScreen` currently assembles, in one large UI owner:

- Minion/Demon titles;
- small-game / no-Minion / no-Demon reasons;
- wake/action copy;
- player-facing Minion information;
- Demon Minion-list + bluff-list copy;
- host display primary/secondary fields;
- reveal-readiness gating from `DemonBluffPresentationResolution`;
- the two `ClocktowerNightStepUi` values consumed by the existing materializer registry.

This is presentation work. It does not need to own legality, candidate selection, state mutation or interaction ordering.

## 3. Target seam

Extract one presentation-only owner:

`ClocktowerFirstNightEvilInformationPresentation`

Conceptual contract:

```text
already-projected evil-team presentation facts
+ shouldGiveFirstNightEvilInfo
+ existing DemonBluffPresentationResolution
+ localization/language
        ->
prepared Minion/Demon ClocktowerNightStepUi presentation steps
```

Inputs may include the already-resolved actor references and seat labels needed by the current UI contract. The extracted owner must not derive a new canonical game model from them.

The caller continues to own:

- Minion/Demon membership projection;
- current actor/session facts;
- legal Demon-bluff domain;
- setup recommendation selection;
- Demon-bluff commitment;
- first-night dependency timing/order;
- materializer registration.

The extracted presentation owner may only transform those supplied facts into localized, read-only UI content.

H1 also retires the transitional `demonBluffRoleNamesForPresentation(...)` adapter. That adapter still accepted setup-plan/style inputs but intentionally ignored them after DLB-5.3; keeping that signature falsely suggests that player presentation may consume uncommitted recommendation inputs. Presentation now reads the committed `recommendedDemonBluffRoleNames` value directly, while `setupPlans/style` remain exclusively on the separate commit-at-barrier path. The two tests that existed only to protect the obsolete adapter signature are retired; the commit-at-barrier and Ready/Pending/Invalid resolution tests remain the durable behavior evidence.

## 4. Core-engine guardrail

This slice follows `CLOCKTOWER_CORE_ENGINE_BOUNDARY_TARGET_ARCHITECTURE_2026-09-30.md`:

- no second mutable state owner;
- no new broad `StorytellerDecisionContext`;
- no legality in presentation;
- no recommendation selection in presentation;
- no session mutation in presentation;
- no new reconstruction of canonical truth inside the extracted module.

`PlayerCard` references, if passed, are opaque presentation actors only; the extracted module must not inspect them to infer team/role truth.

## 5. Test strategy

This is a behavior-preserving ownership extraction, so no manufactured RED is required.

Use:

1. existing `ClocktowerEvilInfoSquareTablePresentationTest` as renderer/handoff characterization;
2. a focused typed test for the new presentation seam covering:
   - Minion info in normal 7+ player flow;
   - Demon info with committed Ready bluff triple;
   - Pending/Invalid bluff state keeps Demon reveal unavailable without inventing content;
   - small-game/no-evil-info path remains non-real/no-display;
3. existing first-night flow/order/dependency tests remain unchanged owners for sequencing;
4. focused test + `testFast`/CI/R2 at the logical checkpoint, escalating only if the final diff crosses a stronger subsystem boundary.

## 6. Explicit non-goals

DLB-5H1 must not:

- change first-night order;
- move or duplicate Demon-bluff commitment;
- choose or rank Demon bluffs;
- recompute bluff legality;
- alter Red Herring or Poisoner behavior;
- modify `BEGINNER_CONSERVATIVE_V1`;
- create a generic presentation catalog;
- broaden into H2 or canonical-session projection cleanup;
- perform DLB-6 retirement.

## 7. Implementation plan

1. add the narrow first-night evil-information presentation owner and owning typed test;
2. replace the duplicated Minion/Demon localized-content block in `ClocktowerHostScreen` with the extracted result;
3. keep materializer identities/order and all upstream commit/legality code untouched;
4. re-run producer/consumer search to verify no second presentation builder remains;
5. validate focused tests, diff/scope, CI/R2;
6. if behavior remains identical, mark DLB-5H1 COMPLETE and advance to the shadow/evidence cutover gate recorded by the current DLB route.
