# NEXT DEVELOPMENT HANDOFF — Experienced Night Flow Correctness

> Date: 2026-09-14 Australia/Sydney  
> Status: **CURRENT — immediate development priority**  
> Program: Clocktower Storyteller mobile flow correctness  
> Branch: `codex/experienced-night-flow-correctness`  
> Starting `main`: `940ba1df68365974ba366985d66d1cb583682ba7`

## 0. Start here in the next conversation

Read, in order:

1. root `AGENTS.md`;
2. `docs/TESTING_STRATEGY.md`;
3. `docs/CURRENT_DEVELOPMENT_ROADMAP.md`;
4. this handoff;
5. `docs/SNE7_ARCHITECTURE_HARDENING_2026-08-27.md` if present under its current live/archive path, or the current SNE-7 authoritative night-transaction document;
6. `docs/UI_NAV_1_CLOSEOUT_2026-09-10.md` if present under its current live/archive path;
7. `docs/SOURCE_STRING_TEST_RETIREMENT_2026-08-27.md`;
8. query live `main`, this branch, and relevant PR/check status before production edits.

Do not assume the recorded starting SHA remains current if development has advanced.

## 1. Why this work is now the priority

Beginner-mode UI testing has completed successfully enough to proceed to Experienced mode. Real-device testing in Experienced mode exposed a severe navigation/rendering regression:

> after the Demon selects and confirms a night kill target, the app can enter a black screen with no usable Previous or Next navigation.

A read-only ownership audit found that the defect should not be treated as a DemonKill-page-only bug. The current night flow has a broader ownership split around post-confirmation navigation, dynamic step expansion, and full-screen render ownership.

EPI-MQ is therefore temporarily paused. This correctness campaign takes priority because it affects the basic ability to complete a live game.

## 2. Confirmed architecture context

Existing project contracts remain authoritative:

- `ClocktowerNightCheckpoint.nightStepIndex` is the sole stored night navigation position;
- `NightCheckpointReducer` owns typed checkpoint transition semantics;
- `NightCheckpointHostTransaction` is the typed Host transaction boundary already used for protected night transitions such as Previous;
- durable night mechanics/history remain owned by the existing checkpoint/session/App transaction boundaries;
- `HostBottomActionBar` / `ClocktowerHostFullScreenScaffold` are presentation/navigation shells, not gameplay owners;
- Beginner and Experienced must share rules, legality and recommendation pipelines; mode may change interaction authority and presentation only.

Do not introduce a second persisted navigation coordinator, a second gameplay state owner, or mode-specific gameplay rules.

## 3. Audit findings that must guide implementation

### 3.1 P0 — post-confirmation night navigation can enter a non-canonical cursor state

The current dynamic-advance path can temporarily write a step index equal to the current `nightSteps.size` when a confirmation may cause the flow to expand dynamically.

That value is outside the currently renderable step domain. Rendering then relies on coercion and a later Compose effect to decide whether a new step appeared or the night should complete.

This creates multiple simultaneous interpretations of "current step":

```text
stored checkpoint cursor
render-coerced cursor
Compose-local deferred advance state
post-refresh dynamic flow result
```

The next implementation must remove the requirement for an out-of-range stored navigation cursor.

### 3.2 P0 — Previous and Next/post-confirm advance do not share one typed ownership boundary

Previous already has typed transaction/reducer ownership. Dynamic forward movement is still substantially orchestrated through Host/Compose state and `LaunchedEffect`.

This is the central ownership defect to correct. Do not solve the reproduced bug by adding a DemonKill-only conditional.

### 3.3 P0 — full-screen ownership is not currently proven total

The code can classify a night action as owning a full-screen surface while the actual render path is separately selected.

Required invariant:

> every actual night step classified as full-screen must deterministically resolve to a renderable host surface for the active mode, or explicitly fall back to a valid legacy surface; it must never resolve to "nothing".

The black-screen report plus loss of navigation is consistent with a violation of this invariant, although the exact field root cause still needs to be proven by executable evidence.

### 3.4 P1 — required single-target actions do not have one shared confirmation-eligibility contract

Required target-selection steps such as Demon attack, Poisoner, Monk, and similar actions should not expose an enabled Next/Confirm path unless the current selection is valid for that already-computed legal candidate domain.

Do not move legality into presentation. Add or reuse a derived interaction-eligibility contract only.

### 3.5 P1 — Experienced mode exercises dynamic branches Beginner can bypass automatically

Experienced mode reaches manual branches such as Mayor redirect, Demon succession, and discretionary information selection more often. These transitions need a direct regression matrix rather than assuming Beginner acceptance covers them.

## 4. Required target invariant

The first campaign-level invariant is:

> **After every legal night confirmation, the application must resolve to exactly one of two states: a valid renderable next night step, or an explicit night-completion/Dawn transition. There is no third out-of-range, blank, or ownerless UI state.**

A related cursor invariant:

> When a renderable night step list is non-empty, the stored UI navigation cursor must identify an actual step in that list. Dynamic expansion must not require persisting an out-of-range sentinel cursor.

## 5. Implementation sequence

### S1 — Night navigation ownership / reproduced black-screen regression

First perform a narrow live delta audit of:

- `ClocktowerDynamicNightAdvance`;
- `ClocktowerNightCheckpoint`;
- `NightCheckpointReducer`;
- `NightCheckpointHostTransaction`;
- the Host callbacks that confirm Demon attack / Mayor redirect / Demon successor;
- the Compose effect/state currently used for deferred night advance;
- Dawn transition ownership.

Then establish the smallest durable typed RED that reproduces the invalid post-confirmation state or proves the missing invariant.

Preferred direction:

- keep the stored cursor at a currently valid step while dynamic flow refresh is pending;
- represent pending forward resolution as transient command/state only if genuinely required;
- after recomputing the flow, resolve through one typed result such as valid `MoveTo(actualIndex)` or `CompleteNight`;
- route forward navigation through the same authoritative typed boundary as the rest of night navigation where architecturally appropriate;
- preserve existing checkpoint/session/history ownership and commit timing.

Do not encode an out-of-range step as a navigation sentinel.

### S2 — Full-screen surface totality

Audit `ClocktowerNightFullScreenOwnership` and every production producer/consumer of the full-screen classification.

Establish a durable contract that every claimed full-screen night step resolves to a concrete render surface in both Beginner and Experienced modes.

Prefer a typed render-plan/presentation boundary if the existing architecture naturally supports it. Do not create an abstraction solely to satisfy test ceremony.

The shared Activity-root host scaffold remains the navigation-shell owner.

### S3 — Skilled interaction eligibility

Audit shared single-target presentation and all target-selection consumers.

Establish a shared derived confirmation-eligibility contract:

```text
required target action
+ selection exists
+ selection belongs to already-authoritative legal domain
-> confirmation enabled
```

Presentation must consume upstream legality, not reimplement it.

Cover at least Demon attack, Poisoner and Monk, then map the remaining single-target fan-out and classify must-inherit vs intentional exemption.

### S4 — Experienced night-flow regression matrix

Before campaign closeout, exercise at least:

- ordinary Demon attack -> Dawn;
- Demon attacks Mayor -> Experienced manual redirect path;
- Imp self-kill -> manual Demon successor path -> new-Demon/Dawn continuation;
- Previous -> edit draft -> reconfirm;
- restore/reconstruction at relevant intermediate checkpoints;
- representative single-target, pair-information, numeric-information and manual-information steps;
- Beginner regression for the same shared owners so this fix does not create a second mode pipeline.

Real-device validation is required after the logical checkpoint because the original report is a device-visible navigation/render failure.

## 6. Test strategy

Follow `AGENTS.md` and `docs/TESTING_STRATEGY.md`.

For S1, this is a genuine regression/behavior gap. Prefer:

```text
stable invariant
-> smallest typed T0 RED
-> minimal production fix
-> exact T0 GREEN --rerun-tasks where required
-> git diff --check
-> remote exact diff/scope audit
```

At logical checkpoints run T1 `:app:testFast` plus triggered T2/T3 evidence.

Do not add a source-string test merely to assert local variable names, callback spelling, or `LaunchedEffect` implementation shape.

Existing source-wiring tests that encode the out-of-range/deferred implementation must be reclassified after the typed contract is established. If they protect only the old implementation shape, narrow or retire them in the same campaign rather than preserving the defective design to keep them green.

## 7. Scope fences

This campaign may:

- correct night navigation ownership;
- correct dynamic post-confirmation resolution;
- establish full-screen render totality;
- correct shared confirmation eligibility for required target actions;
- add typed regression/integration evidence;
- make the smallest Host/App wiring changes required to consume the corrected typed seams;
- retire/narrow superseded implementation-shaped tests.

This campaign must not:

- redesign recommendation ranking;
- change gameplay rules or legal candidate semantics unless an independently proven rule bug is found;
- resume EPI-MQ in the same branch;
- perform broad UI visual redesign;
- perform another general Host/App decomposition campaign;
- create Beginner-only or Experienced-only gameplay pipelines;
- move durable history, ActionFact, Dawn materialization, or persistence authority merely for navigation convenience;
- introduce Navigation Compose or a second phase coordinator.

## 8. Large-file / writer rule

The likely production consumers include very large Host/App files. Apply `AGENTS.md` exactly:

- small/medium tests/docs/helpers: GitHub connector direct write;
- localized edit in a large/truncated file with stable unique anchors: GitHub Actions one-shot workflow + separate Python patch script is the first fallback;
- Codex/Luna only when the remote one-shot path cannot be made safe or the work genuinely requires a complete worktree/broad mechanical change.

Do not patch large files by guessed line number or by partial whole-file replacement.

## 9. Live-state notes at handoff creation

Recorded baseline:

```text
main:
940ba1df68365974ba366985d66d1cb583682ba7
Merge PR #122 — UI: mark dead players clearly on square table

working branch:
codex/experienced-night-flow-correctness
```

Open draft PR #109 (`codex/execution-restore-crash-repro`) is unrelated diagnostic work. Do not modify, merge, or stack this campaign on PR #109.

Reconfirm all live refs/checks in the next conversation.

## 10. Paused work

EPI-MQ-0.5 remains designed and queued, not cancelled.

Its existing handoff remains the resume point after this flow-correctness campaign is accepted. Do not mix epistemic capability work into this branch.

## 11. First action in the next conversation

After live-state confirmation, begin **S1 read-only delta audit + typed RED design**.

Do not edit production code until the RED/owning contract and exact file scope are identified.

## 12. Stable rule

> **Fix the ownership boundary that permits a blank/invalid night state. Do not patch the DemonKill screen as a special case.**
