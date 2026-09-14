# NEXT DEVELOPMENT HANDOFF — Experienced Night Flow Correctness

> Date: 2026-09-14 Australia/Sydney  
> Status: **CURRENT — S1 COMPLETE; S2 read-only audit next**  
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

### 3.1 P0 — post-confirmation night navigation can enter a non-canonical cursor state — S1 FIXED

The former dynamic-advance path could temporarily write a step index equal to the current `nightSteps.size` when a confirmation might expand the flow dynamically. That value was outside the currently renderable step domain, while rendering depended on coercion and a later Compose effect.

S1 removed the out-of-range navigation sentinel. A dynamic final-step confirmation now keeps the current renderable cursor and returns transient `AwaitRefreshedFlow(currentStepIndex)` state. The refreshed flow then resolves through a typed result to either `MoveTo(actualValidIndex)` or `CompleteNight`.

The durable checkpoint/session/history/Dawn ownership remains unchanged.

### 3.2 P0 — Previous and Next/post-confirm advance ownership — S1 narrowed

Previous remains owned by the existing typed checkpoint/Host transaction boundary. S1 established a typed post-confirm forward-resolution seam without moving mechanics/history/Dawn ownership and without introducing a second persisted navigation coordinator.

The remaining campaign work is not to widen this abstraction unnecessarily; continue only where executable evidence identifies a missing invariant.

### 3.3 P0 — full-screen ownership is not currently proven total

The code can classify a night action as owning a full-screen surface while the actual render path is separately selected.

Required invariant:

> every actual night step classified as full-screen must deterministically resolve to a renderable host surface for the active mode, or explicitly fall back to a valid legacy surface; it must never resolve to "nothing".

S1 corrected the invalid cursor path, but it did not prove full-screen surface totality. This is now the immediate S2 target.

### 3.4 P1 — required single-target actions do not have one shared confirmation-eligibility contract

Required target-selection steps such as Demon attack, Poisoner, Monk, and similar actions should not expose an enabled Next/Confirm path unless the current selection is valid for that already-computed legal candidate domain.

Do not move legality into presentation. Add or reuse a derived interaction-eligibility contract only.

### 3.5 P1 — Experienced mode exercises dynamic branches Beginner can bypass automatically

Experienced mode reaches manual branches such as Mayor redirect, Demon succession, and discretionary information selection more often. These transitions need a direct regression matrix rather than assuming Beginner acceptance covers them.

## 4. Required target invariant

The campaign-level invariant remains:

> **After every legal night confirmation, the application must resolve to exactly one of two states: a valid renderable next night step, or an explicit night-completion/Dawn transition. There is no third out-of-range, blank, or ownerless UI state.**

The S1 cursor invariant is now executable:

> When a renderable night step list is non-empty, the stored UI navigation cursor identifies an actual step in that list. Dynamic expansion does not persist an out-of-range sentinel cursor.

## 5. Implementation sequence

### S1 — Night navigation ownership / reproduced black-screen regression — COMPLETE

Completed evidence:

- typed RED commit: `52299383dfe9d2f2f6eef9718179a73234248a45`;
- RED proved the dynamic final-step path exposed an invalid cursor under the old implementation;
- production checkpoint: `7796ba13012ec12801abba3c75a7a61b8706cd28` — `fix: keep dynamic night cursor renderable`;
- cleanup head after removing the temporary one-shot writer: `99c47bed6180839c0756b286540933cea90cc3b9`;
- focused GREEN passed;
- `:app:testFast --rerun-tasks` passed;
- `git diff HEAD --check` and exact four-file production/test allowlist passed;
- obsolete `ClocktowerDynamicNightAdvanceWiringTest` was retired because it protected only the defective source/implementation shape.

Stable S1 contract:

```text
current valid step
+ confirmation may expand flow
-> AwaitRefreshedFlow(current valid index)

refreshed flow contains a real next step
-> MoveTo(actual valid next index)

refreshed flow contains no next step
-> CompleteNight
```

Do not reintroduce an out-of-range step as a navigation sentinel.

### S2 — Full-screen surface totality — CURRENT NEXT STEP

Begin with a read-only ownership/fan-out audit of `ClocktowerNightFullScreenOwnership` and every production producer/consumer of the full-screen classification.

Establish a durable contract that every claimed full-screen night step resolves to a concrete render surface in both Beginner and Experienced modes.

Prefer a typed render-plan/presentation boundary if the existing architecture naturally supports it. Do not create an abstraction solely to satisfy test ceremony.

The shared Activity-root host scaffold remains the navigation-shell owner.

Do not modify production until the classification → render-surface ownership boundary, must-inherit fan-out, intentional exemptions, and smallest typed evidence are identified.

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

S1 evidence order was completed as required:

```text
stable invariant
-> smallest durable typed T0 RED
-> minimal production fix
-> exact T0 GREEN
-> T1 testFast
-> git diff --check
-> remote exact diff/scope audit
```

For S2, begin read-only. Do not create a source-string test merely to assert local variable names, callback spelling, branch spelling, or render-function names. First identify whether a typed presentation/render-plan contract already exists or whether a minimal new typed contract is genuinely required.

Existing source-wiring tests that protect only implementation shape may be narrowed or retired only after durable typed evidence protects the actual invariant.

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

## 9. Live-state notes

Recorded baseline remains:

```text
main:
940ba1df68365974ba366985d66d1cb583682ba7
Merge PR #122 — UI: mark dead players clearly on square table

working branch:
codex/experienced-night-flow-correctness

S1 production checkpoint:
7796ba13012ec12801abba3c75a7a61b8706cd28

S1 cleanup head before this docs update:
99c47bed6180839c0756b286540933cea90cc3b9

campaign PR:
#123 — draft / do not merge yet
```

The bot-authored cleanup head can cause GitHub PR CI/R2 to report `action_required` with zero jobs because it removes the temporary workflow. Treat that as workflow approval state, not a failing test. The production checkpoint itself was validated inside the successful one-shot run before push.

Open draft PR #109 (`codex/execution-restore-crash-repro`) remains unrelated diagnostic work. Do not modify, merge, or stack this campaign on PR #109.

Reconfirm all live refs/checks in the next conversation.

## 10. Paused work

EPI-MQ-0.5 remains designed and queued, not cancelled.

Its existing handoff remains the resume point after this flow-correctness campaign is accepted. Do not mix epistemic capability work into this branch.

## 11. First action in the next conversation

After live-state confirmation, begin **S2 read-only full-screen surface totality audit**.

Map:

```text
full-screen classification producer
-> every production consumer
-> mode-specific branching
-> concrete rendered host surface / explicit legacy fallback
```

Classify every fan-out path as must-inherit or intentional exemption. Do not modify production until the smallest durable typed evidence and exact file scope are identified.

## 12. Stable rule

> **Fix the ownership boundary that permits a blank/invalid night state. Do not patch the DemonKill screen as a special case.**
