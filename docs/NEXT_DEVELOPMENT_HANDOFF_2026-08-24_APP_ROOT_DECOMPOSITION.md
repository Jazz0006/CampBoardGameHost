# CampBoardGameHost — App-root Decomposition Handoff

> Prepared: 2026-08-23  
> Intended start: after PR #43 is merged  
> Next structural target: `CampBoardGameHostApp.kt`  
> Product A3 is deliberately deferred until this structural pass is complete.

## 1. Start condition

Do not begin this task from PR #43 or from its feature branch.

In the new conversation:

1. read `AGENTS.md`;
2. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
3. read this handoff;
4. re-query live `main`, confirm PR #43 is merged, and record the actual merge commit;
5. create a fresh structural branch from live `main`;
6. do not start A3 product work in the same branch.

Never assume the SHA values from the previous PR remain current.

## 2. Why this task comes before A3

After PR #43 A13, the largest remaining handwritten production sources are approximately:

```text
CampBoardGameHostApp.kt      325,556 bytes
ClocktowerHostScreen.kt      295,644 bytes
ClocktowerDayScreen.kt        63,135 bytes
ClocktowerNightStepUi.kt      45,251 bytes
```

The earlier R2 MainActivity decomposition is already complete: `MainActivity.kt` is now only the Android Activity / immersive-mode / `setContent` shell. The remaining root monolith is `CampBoardGameHostApp.kt`, which still owns a broad mix of app navigation, game state, Clocktower session/persistence integration and cross-feature wiring.

A3 historical multi-night exact-baseline work is expected to touch history/session/world-state wiring. Doing that before auditing the app-root owner risks adding more responsibility to another ~325 KiB file.

Therefore the next sequence is:

```text
PR #43 merge
-> fresh App-root decomposition PR
-> final structural audit
-> A3 historical multi-night exact baseline
```

## 3. Architecture rule carried forward from PR #43

`ClocktowerHostScreen.kt` is under a **new-responsibility growth freeze**.

This does not mean it must be forced below 50 KiB. It means future features should not default to adding algorithm/policy/UI bodies there.

Host should normally retain only:

- current Compose-derived orchestration state;
- phase routing;
- thin wiring to stable owners;
- protected callback/audit/commit transactions;
- state/effect lifetime that has no better owner yet.

If a future feature would add hundreds of lines to Host, first identify whether a domain, epistemic, history, recommendation, session, materializer or dedicated UI owner should hold it instead.

## 4. App-root decomposition first-pass objective

This is **not** a rewrite to ViewModel/MVI/Redux and not an excuse to move state blindly.

First perform an ownership inventory of `CampBoardGameHostApp.kt` and classify top-level or major contiguous responsibilities such as:

- app navigation / landing / settings shell;
- general game/session lifecycle;
- Undercover root state/wiring;
- Werewolf root state/wiring;
- Clocktower setup/game root state;
- Clocktower persistence / restore / archive integration;
- Clocktower session/history/observation wiring;
- role/card handoff / reveal flow;
- result/end-game routing;
- shared small model declarations still living at app root;
- Compose effects and persistence effects whose lifetime is app-scoped.

Do not assume these exact categories are the final extraction boundaries. Re-audit the live file and choose only boundaries that are cohesive and behavior-preserving.

## 5. Preferred extraction order

Use the same risk discipline as PR #43:

1. pure models / stateless helpers with obvious ownership;
2. isolated presentation shells;
3. game-specific root wiring where callback/state contracts are already explicit;
4. persistence/history adapters with strong characterization tests;
5. only then consider larger state-owner extraction if a natural owner has emerged.

Do **not** begin by manufacturing a giant `AppState` or giant parameter/context bag simply to move code across files.

## 6. Protected invariants

Structural work must preserve:

- three game-mode entry paths and navigation behavior;
- Clocktower rule semantics and precedence;
- Clocktower setup/recommendation ordering;
- registration and impairment semantics;
- persistence/restore/archive behavior;
- historical action / observation ordering and identity;
- `ClocktowerGameSession` global timeline authority;
- Compose `remember`, `LaunchedEffect`, lifecycle and cancellation semantics;
- cross-game state reset behavior;
- transaction/callback ordering;
- PR #43 First/Other Night planner-first materialization architecture;
- same-night / next-night Demon transition semantics.

Do not combine functional product changes with these moves.

## 7. Tests-first / characterization strategy

Before the first extraction, inspect existing app-root/decomposition tests and identify missing ownership contracts.

Prefer characterization tests that lock:

- navigation ownership;
- persistence callback order;
- Clocktower session creation/restore boundaries;
- game reset and result/archive boundaries;
- source ownership when the contract genuinely represents architecture rather than an obsolete implementation detail.

For a pure structural move, a characterization RED is useful when it clearly demonstrates the intended owner transfer. Do not create brittle source-string tests merely to force a file-size target.

Each GREEN slice should have:

- focused tests;
- full `:app:testDebugUnitTest`;
- `:app:assembleDebug`;
- `git diff --check`;
- ASP / Real Clingo remote gates when normal CI runs them;
- exact changed-file audit.

## 8. File-size policy

~50 KiB remains a soft maintainability target, not a hard gate.

For this pass:

- >50 KiB: review warning / seek natural owner;
- >100 KiB: strong warning and explicit architecture audit;
- no automatic split if extraction would worsen ownership;
- no merge blocker solely because a cohesive owner remains above 50 KiB.

At the end of the App-root PR, remeasure all handwritten production files and document the top remaining large files.

`ClocktowerDayScreen.kt` (~63 KiB) should be audited after the App-root work. Split it only if a clean low-coupling owner exists; do not mechanically split for the threshold.

## 9. Explicitly out of scope

Do not mix into this structural PR:

- A3 historical multi-night exact-baseline behavior;
- B4/ZDD production promotion;
- history UI redesign;
- misinformation expansion;
- broader manual Storyteller UI rollout;
- new Clocktower roles/scripts unless required only to preserve compilation after a mechanical move;
- new state-management framework migration.

## 10. Stop point

The first new-conversation task is **audit + slice plan**, not uncontrolled mass extraction.

ChatGPT should first return:

- live main / new branch/head;
- current `CampBoardGameHostApp.kt` size;
- responsibility inventory;
- candidate extraction slices ranked by value/risk;
- protected state/effect/transaction boundaries;
- proposed RED/characterization plan;
- recommendation for the first implementation slice.

Only then begin constrained implementation.
