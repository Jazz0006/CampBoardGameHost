# Next Development Handoff — TBSP-6K Final Acceptance

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR: #57  
> Status: TBSP-6J ACCEPTED; TBSP-6K is next. Do not merge or mark Ready without explicit authorization.

## 1. Resume anchors

Re-query live `main`, PR #57 head/state/checks and branch comparison before execution.

Accepted logical checkpoints:

- TBSP-6H-B production code: `ff1c99fe97552dc65f3d1bf8326bdb451c8e25a0`
- TBSP-6I acceptance test checkpoint: `f7e877f6881cc74b9d8e7f4f8db2b2fb406b84d4`
- TBSP-6J production cleanup: `68d29c53a0a37f2c30b9d88ed8967d5d9548b4bc`
- TBSP-6J one-shot cleanup: `ab1a57393a9abfd774dcdf4776f81134ed19a81a`
- TBSP-6J docs checkpoint: `d3b3993327a86c7dbd091346b11e7e6a95541637`

6J validation:

- one-shot run `33342673392`: SUCCESS
- `:app:compileDebugKotlin`: SUCCESS
- focused `NoGreaterJoySetupRegressionTest --rerun-tasks`: SUCCESS
- `:app:testFast --rerun-tasks`: SUCCESS
- exact diff audit: SUCCESS
- final normal PR CI #1155 / run `33342927330`: SUCCESS
- final R2 #1078 / run `33342927359`: SUCCESS

## 2. Accepted TBSP state before 6K

TBSP-1 through TBSP-6J are accepted. P1–P16 remain the merge-blocking invariant set.

6J changed no behavior. The exact production diff only:

- removed unused `preparedSetupPlan` from `resetDealState`;
- removed the corresponding dead call-site argument;
- preserved the local legacy/NGJ `preparedSetupPlan` calculation and `recommendedDrunkShownRole` consumer.

The post-TBSP **MS-SETUP generic multi-script setup architecture** is planned in `docs/CURRENT_DEVELOPMENT_ROADMAP.md` and begins only after 6K. Do not pull that refactor into final TBSP acceptance.

## 3. TBSP-6K scope

6K is final integrated acceptance, not a new feature slice.

Required gate from the roadmap:

```text
all focused TBSP acceptance GREEN
-> :app:testFast
-> affected T2/T3 when required by docs/TESTING_STRATEGY.md
-> :app:testFull
-> R2
-> final GitHub CI
-> exact diff / scope audit
```

Before choosing commands, read root `AGENTS.md` and `docs/TESTING_STRATEGY.md` and use the current tier definitions. Do not invent extra tests merely for ceremony.

## 4. Final acceptance evidence to preserve

At minimum revalidate the integrated branch evidence for:

- curated TB preset parsing/semantic validation;
- deterministic history-aware preset/Drunk selection;
- deterministic deal materialization and identity provenance;
- invalid-data no-fallback behavior;
- restore/no-reroll behavior;
- true-completion rotation-history exactly-once semantics;
- non-blocking reveal setup prewarm;
- First Night READY/BUSY/MISS/stale lifecycle;
- No Greater Joy regression;
- accepted Dawn/Dusk exactly-once predecessor behavior through the appropriate broader suites.

Use existing owning tests rather than adding duplicate acceptance wrappers unless a real uncovered regression appears.

## 5. Scope exclusions

Do not in 6K:

- change TB dataset, selector weights, Drunk ownership or rotation semantics;
- implement MS-SETUP multi-script generic architecture;
- change No Greater Joy setup semantics;
- redesign 6G/6H lifecycle ownership;
- reopen A3/A4/ZDD, Mayor, Imp succession, Dawn/Dusk semantics or App/Host decomposition;
- merge PR #57 or mark it Ready without explicit user authorization.

If final acceptance exposes a real regression, stop and create the smallest explicit repair slice rather than hiding it inside the acceptance run.

## 6. Stop condition

When final acceptance evidence is GREEN, report exact checkpoints and PR state, update current documentation, and stop. Ready/merge remains a separate explicit user decision.
