# CampBoardGameHost — App-root Decomposition Resume Handoff (S5)

> Prepared: 2026-08-24 01:01 Australia/Sydney  
> Resume target: 2026-08-25  
> Branch: `codex/source-decomposition-app-root`  
> Last validated production head before this handoff: `cb18ead36b19fb43cac8ea799a7a4b2adc06da28`  
> Current structural status: **S0–S4 CLOSED; next = S5 architecture audit**  
> Product A3 remains deferred until this structural pass is complete.

## 1. Resume rule

Tomorrow, do not immediately ask Luna to edit production code.

Start by:

1. read `AGENTS.md`;
2. read `docs/CHATGPT_CODEX_LUNA_LOCAL_PATCH_WORKFLOW.md`;
3. read `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. read `docs/NEXT_DEVELOPMENT_HANDOFF_2026-08-24_APP_ROOT_DECOMPOSITION.md`;
5. read this file;
6. re-query live `codex/source-decomposition-app-root` and confirm it still descends from the documented head;
7. perform a fresh **Chat-owned S5 architecture audit** on the live `CampBoardGameHostApp.kt`;
8. select exactly one next slice only after dependency/visibility/ownership analysis;
9. establish characterization / ownership RED before production movement where useful;
10. give Luna a prescriptive mechanical task with no architecture decisions left open.

Never merge without explicit user authorization.

## 2. Branch baseline and current head

Fresh structural branch:

```text
codex/source-decomposition-app-root
```

Branch was created from stable live main:

```text
c444694e4ab3420ee8a79e7146b736923b81c8f0
```

Last validated production commit before this documentation handoff:

```text
cb18ead36b19fb43cac8ea799a7a4b2adc06da28
refactor: extract deal presentation
```

The documentation commits created after that production commit are allowed to advance branch HEAD. Tomorrow, re-query live HEAD instead of assuming `cb18ead...` is still the branch tip.

## 3. App-root size progress

Original root at start of this structural pass:

```text
CampBoardGameHostApp.kt = 325,556 bytes
```

After completed slices:

```text
After S1 = 293,536 bytes
After S2 = 283,470 bytes
After S3 = 277,453 bytes
After S4 = 261,232 bytes
```

Net reduction through S4:

```text
64,324 bytes
~19.8%
```

The 50 KiB target remains a maintainability guideline, not a reason to damage ownership boundaries.

## 4. S0 — dynamic-flow preservation guard — CLOSED

Purpose: freeze the already-established planner-first dynamic night-flow authority before app-root movement.

RED / guard commit:

```text
ec708cef92ede271fef977ee178959e25f5d47df
```

Key invariants:

- App root does not own `ClocktowerProductionFirstNightFlow`;
- App root does not own `ClocktowerProductionOtherNightFlow`;
- App root does not own `ClocktowerNightStepMaterializerRegistry`;
- production First/Other Night seams retain planner + projector authority;
- stable interaction identities remain independent of UI indexes;
- conditional/event interactions must not be flattened into static list semantics;
- `selectedClocktowerScript` / `currentClocktowerScript` may temporarily remain content/session identity but are not night-order authority.

Protected pipeline remains:

```text
script/content
-> interactions
-> planner ordering
-> stable identities
-> lazy materialization
-> Host UI
```

## 5. S1 — player setup presentation — CLOSED

Ownership RED:

```text
9f8e49c301960bea9d1750dab2ad926d587e9532
```

GREEN:

```text
85a57d550db627c310a429a5812746af24165cba
refactor: extract player setup presentation
```

New file:

```text
AppPlayerSetupScreens.kt = 35,163 bytes
```

Moved presentation ownership:

- `SetupScreen`
- `RoundTableSetupEditor`
- `DraggedPlayer`
- `PlayerDragState`
- direct private drag/avatar helpers

Root retained player state, mutation, restore/start callbacks and routing.

Approved visibility changes were limited to the exact cross-file dependencies identified during Chat audit.

## 6. S2 — settings presentation — CLOSED

Ownership RED:

```text
67538cd94cf4652a95d48674769c42593663b6c4
```

GREEN:

```text
44e9661615396c9dfe1f0501370521ba1a3f4cad
refactor: extract app settings presentation
```

New file:

```text
AppSettingsScreen.kt = 11,456 bytes
```

Moved:

- `SettingsScreen`

Root retained:

- `languageMode` state;
- storyteller automation state;
- common-player state;
- all preference writes;
- settings routing callbacks;
- shared `GameSettingsHeader`, `EmptyStateCard`, `StepperRow`.

Only planned visibility changes:

- `LanguageMode`: private -> internal
- `LanguageMode.labelResId()`: private -> internal

## 7. S3 — Clocktower landing presentation — CLOSED

Ownership RED:

```text
573f0c8d09c2220e3cd0fcc3ad713a9f7aa4ed19
```

GREEN:

```text
af43581b94301c2d57dfff121a941895dabb6e6d
refactor: extract clocktower landing presentation
```

New file:

```text
AppClocktowerLandingScreen.kt = 7,522 bytes
```

Moved:

- `ClocktowerLandingScreen`

Only declaration change:

- `ClocktowerLandingScreen`: private -> internal

Root retained:

- `screen`;
- `savedGamePreview`;
- `restoreSavedGame()`;
- `clearSavedGameState()`;
- landing route and callbacks;
- all active-game persistence ownership.

## 8. S4 — deal / reveal presentation cluster — CLOSED

S4 required extra Chat audit because the initial assumption that only `PassPhoneScreen` / `RevealCardScreen` / `FullScreenColumn` were needed was incomplete. The audit found two direct private Clocktower presentation dependencies still in root. The slice boundary was corrected before Luna production work.

Final ownership RED baseline:

```text
61ccbbec870d9b0c9bca069647d2c0a86af913ac
```

GREEN:

```text
cb18ead36b19fb43cac8ea799a7a4b2adc06da28
refactor: extract deal presentation
```

New file:

```text
AppDealScreens.kt = 17,749 bytes
```

Moved exactly five presentation functions:

- `PassPhoneScreen` -> internal
- `RevealCardScreen` -> internal
- `ClocktowerDealHandoffScreen` -> private in new file
- `ClocktowerPlayerRoleRevealScreen` -> private in new file
- `FullScreenColumn` -> private in new file

Only planned Root visibility changes:

- `Role.labelResId()`: private -> internal
- `ClocktowerRole.descriptionFor()`: private -> internal

Root retained:

- `currentDealIndex` state;
- `cards` and `currentGameKind`;
- `Screen.PassPhone` routing;
- `Screen.RevealCard` routing;
- `onReveal` navigation mutation;
- final-card branch to WerewolfJudge / ClocktowerJudge / Game;
- `currentDealIndex += 1`;
- persistence serialization / restoration / reset of deal index;
- A4 identity-reveal `LaunchedEffect`, including `currentDealIndex + 1` prioritization.

Remote exact diff audit: PASS.

## 9. Validation status through S4

User reported Luna local validation GREEN for completed slices.

Chat remotely audited each production push against its RED baseline and confirmed exact diff shape.

GitHub commit status / PR workflow runs were not present for these structural commits, so remote GitHub did not independently reproduce local Gradle execution. The accepted gate is:

```text
Luna local focused tests GREEN
+ full :app:testDebugUnitTest GREEN
+ :app:assembleDebug GREEN
+ git diff --check GREEN
+ Chat remote exact diff audit PASS
```

Do not silently weaken this gate for S5+.

## 10. Working model learned during S1–S4

This is now an explicit workflow requirement.

### ChatGPT / Chat owns

- candidate selection;
- dependency graph inspection;
- exact slice boundary;
- exact target filename;
- exact declarations to move;
- exact visibility changes;
- exact declarations that must remain in Root;
- forbidden files/regions;
- ownership/characterization RED design;
- focused/full validation commands;
- expected diff shape;
- failure/stop conditions;
- commit message;
- remote exact diff audit.

### Luna / Codex owns only mechanical local execution

Luna should not decide architecture.

Before handing a slice to Luna, Chat should resolve cross-file private dependencies itself. Avoid instructions such as:

- "choose whichever helper makes sense";
- "move related declarations if required";
- "inspect and decide";
- "possibly broaden visibility".

Instead, if compilation would require an unplanned dependency, Luna must STOP and report.

This workflow improvement was specifically requested after S1 and proved useful in S4, where Chat caught extra private dependencies before production movement.

## 11. Protected boundaries for S5 and later

Do not casually move these responsibilities merely to reduce Root size:

### Clocktower live transaction owner

Root still owns protected callback mutation / action / observation / revision ordering.

### Compose effect lifetime

Root still owns important effect lifetime including:

- A4 identity-reveal prewarm `LaunchedEffect`;
- observation rebuild `LaunchedEffect`;
- `rememberUpdatedState`;
- lifecycle `DisposableEffect`;
- `SideEffect`;
- persistence/durability effect timing.

### Persistence / restore / archive

Keep transitional persistence ownership stable until a specifically characterized slice proves a natural adapter boundary.

### Dynamic script / night-flow authority

Do not turn transitional `ClocktowerScript`, `ClocktowerNightAction`, `ClocktowerDisplayKind`, fixed role catalog or setup representations into permanent-looking flow architecture simply because they are easy to move.

Always ask:

> Is this reducing the legacy root, or merely moving transitional hardcode into a new permanent-looking owner?

## 12. S5 — exact next task

S5 has **not yet been selected**.

Tomorrow's first implementation-related action is not extraction. It is:

# Chat-owned S5 architecture audit

Use the live `CampBoardGameHostApp.kt` at the then-current branch head and audit remaining presentation / helper / game-specific root regions.

Do not automatically reuse the old pre-S1 ranking.

Candidate categories may include:

- another isolated presentation owner;
- a cohesive results/end-game presentation cluster;
- safe shared UI primitive ownership if a natural owner exists;
- pure stable helper/model extraction only if it does not freeze transitional Clocktower concepts;
- game-specific presentation/wiring only after callback/state ownership is fully mapped.

Avoid, unless deliberately re-characterized as a later high-risk slice:

- Clocktower setup authority;
- persistence JSON ownership;
- A4 effect implementation;
- Clocktower live transactions;
- planner/materializer/session authority.

For whichever S5 candidate is chosen, Chat must first determine:

1. exact new file;
2. exact declaration list;
3. direct helper dependencies;
4. exact visibility changes;
5. root symbols/callbacks/state that must remain;
6. expected production diff shape;
7. RED failure reasons;
8. focused/full/assemble commands;
9. exact diff audit criteria;
10. stop conditions.

Only then hand the mechanical GREEN task to Luna.

## 13. Structural route after S5

The route remains:

```text
continue App-root decomposition in small characterized slices
-> stop before ownership becomes artificial or high-risk
-> remeasure handwritten production files
-> audit ClocktowerDayScreen.kt (~63 KiB) only for natural seams
-> complete structural pass
-> only then resume product A3 historical multi-night exact baseline
```

Do not treat 50 KiB as a hard merge gate.

Do not begin A3 on this structural branch.

## 14. Tomorrow startup checklist

When resuming, the user should be able to say simply: "继续".

Chat should then:

1. query live branch head;
2. confirm documentation-only commits after `cb18ead...` are expected;
3. verify no unexpected production commit appeared after S4;
4. read this handoff;
5. inspect the current 261 KiB-ish Root rather than relying on stale snippets;
6. perform S5 candidate/dependency audit;
7. choose one slice;
8. create RED if appropriate;
9. provide a highly prescriptive Luna task;
10. stop after Luna pushes so Chat can perform remote exact diff audit.

No merge without explicit authorization.
