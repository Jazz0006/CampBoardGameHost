# Next Development Handoff — TBSP-6K Final Acceptance

> Date: 2026-08-31 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Branch: `codex/trouble-brewing-setup-presets-v2`  
> Draft PR: #57  
> Status: **TBSP-6K COMPLETE / ACCEPTED.** This handoff is historical execution guidance. Do not merge or mark Ready without explicit authorization.

## 1. Accepted checkpoints

- TBSP-6H-B production code: `ff1c99fe97552dc65f3d1bf8326bdb451c8e25a0`
- TBSP-6I acceptance test checkpoint: `f7e877f6881cc74b9d8e7f4f8db2b2fb406b84d4`
- TBSP-6J production cleanup: `68d29c53a0a37f2c30b9d88ed8967d5d9548b4bc`
- TBSP-6J one-shot cleanup: `ab1a57393a9abfd774dcdf4776f81134ed19a81a`
- TBSP-6J docs checkpoint: `d3b3993327a86c7dbd091346b11e7e6a95541637`
- TBSP-6K T4 trigger: `6b80b7ade7235d890bd2a492ed8b33a19c43ffaa`

## 2. Final acceptance result

TBSP-1 through TBSP-6K are accepted. P1–P16 remain the accepted invariant set.

The 6K T4 checkpoint selected every full gate through `[full-ci]` and completed successfully:

- CI #1158 / run `33343377258`: SUCCESS
- Android full JVM + debug build: SUCCESS
  - `./gradlew :app:testFull :app:assembleDebug --no-daemon --rerun-tasks`
  - `:app:testDebugUnitTest`: executed
  - `:app:testFull`: SUCCESS
  - `:app:assembleDebug`: SUCCESS
- ASP contract tests: SUCCESS
- Real Clingo cross-validation: SUCCESS
- CI aggregate gate: SUCCESS
- R2 #1081 / run `33343377271`: SUCCESS

No repair slice was required. 6K changed no production or test code.

The detailed checkpoint is:

`docs/TBSP_6K_FINAL_ACCEPTANCE_CHECKPOINT_2026-08-31.md`

## 3. Accepted integrated behavior

Final acceptance preserves evidence for:

- curated TB preset parsing/semantic validation;
- deterministic history-aware preset/Drunk selection;
- deterministic deal materialization and identity provenance;
- invalid-data no-fallback behavior;
- restore/no-reroll behavior;
- true-completion rotation-history exactly-once semantics;
- non-blocking reveal setup prewarm;
- First Night READY/BUSY/MISS/stale lifecycle;
- No Greater Joy regression;
- accepted Dawn/Dusk exactly-once predecessor behavior through the full Android suite.

## 4. Next planned campaign

The current roadmap now queues **MS-SETUP generic multi-script setup architecture** as the next planned work. It has **not started** in this handoff.

Before MS-S1 production implementation, perform a fresh live-state and ownership audit. The target architecture is script-neutral setup selection with:

- optional per-script/player-count templates;
- deterministic legal generated setup as the default when no templates exist;
- one common diversity/history selector for template and generated candidates;
- generic committed shown-identity handling, including Drunk-style identities;
- script/ruleset setup modifiers outside App-root conditionals;
- parity protection for accepted TB and NGJ behavior.

Do not treat this historical 6K handoff as permission to begin the genericization without the fresh MS-SETUP audit/handoff.

## 5. Governance

PR #57 remains Draft / open / not merged. Ready/merge is a separate explicit user decision.

Do not reopen TBSP P1–P16, Dawn/Dusk exactly-once behavior, or No Greater Joy behavior without a concrete regression or explicit product change.
