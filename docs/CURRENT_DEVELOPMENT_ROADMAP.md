# CampBoardGameHost — Current Development Roadmap

> Updated: 2026-09-06 Australia/Sydney
> Repository: `Jazz0006/CampBoardGameHost`  
> **This file is the single current project-status and execution-priority authority.**

## 1. Live development context

Live `main` verified for the current decomposition campaign:

```text
93f99e0576be7b93d479ffa931bae3e4083c25af
Fix Drunk shown-identity ownership boundary (#105)
```

Always re-query live GitHub state before implementation, validation or merge.

### Recently completed / integrated

The following work is integrated into `main`:

1. **PR #99 — R4D-6 Host Table preserved-lineage integration**
   - recovered surviving shared Host Table / `HostSeatPresentation` work;
   - preserved actual/shown Storyteller-private presentation and square-table improvements;
   - did not make presentation the gameplay-legality authority.

2. **PR #100 — UI-N1 inline wake cues + shared square-table readability**
   - the earlier explicit `WAKE -> ACT` acknowledgement-state design was deliberately simplified;
   - wake/actor cue and target selection now coexist on the same persistent square table;
   - no separate wake-phase gameplay or Compose state was introduced;
   - shared square-table density / seat-number presentation was updated.

3. **PR #101 — same-night dead role wake-step correction**
   - ordinary later-night role steps with no effective actor are omitted;
   - explicit death-trigger actors such as Ravenkeeper remain materialized.

4. **PR #102 — Manual Demon bluff consistency correction**
   - MANUAL mode now consumes the intended setup recommendation bluff triple instead of falling back to arbitrary first legal roles;
   - invalid/pending/partial bluff recommendation state fails closed rather than silently substituting values.

5. **PR #104 — obsolete source-wiring guard retirement**
   - removed low-value source-string / production-wiring / decomposition guards;
   - retained durable typed behavior/domain coverage;
   - reinforces the current risk-based, behavior-first test policy.

## 2. Completed hotfix — Drunk shown-identity ownership repair

The targeted setup/recommendation ownership repair documented in:

`docs/DRUNK_SHOWN_IDENTITY_OWNERSHIP_REPAIR_2026-09-05.md`

is now **merged into main through PR #105**. Historical implementation branch:

`codex/drunk-shown-identity-ownership-cleanup`

Validated code checkpoint:

`a7c24fb5e7a0909bc77b84b9dcab5cf8b6e459b7`

Post-validation temporary-workflow cleanup head:

`18d122fd1c3cab7461010ebb1b913ad1b2616198`

The permanent product flow is:

```text
template history/diversity selection
-> select setup template
-> commit actual role composition
-> if Drunk exists, choose one shown identity from template drunkAsOptions
-> committed setup is complete
-> recommendation reads committed GameState
-> recommendation generates remaining mutable Storyteller information/decisions
```

Permanent ownership rule:

- template history/de-duplication applies to template selection;
- Drunk shown identity has no independent history/cooldown/de-duplication rule;
- shown identity is a committed setup fact;
- recommendation may consume it but may not select, replace, emit or lock it;
- Host/UI must not convert committed `shownRole` back into recommendation lock state;
- Demon Bluff recommendation-quality redesign remains deferred.

Completed repair evidence:

- typed mutable-lock ownership contract added;
- Host committed shown-identity lock synthesis/preservation removed;
- recommendation history digest/cooldown no longer depends on committed Drunk shown identity;
- obsolete `generateDrunkCandidates()` path and tests retired;
- focused ownership/migration tests passed;
- `:app:testFast` passed;
- `:app:assembleDebug` passed;
- code/test-only diff and semantic ownership audit passed;
- temporary proof/checkpoint workflows self-cleaned.

A separate recommendation-quality parity finding remains intentionally deferred: generic impaired information derived from fixed `shownRole` is currently selected inside `SetupEvaluator`, not jointly enumerated with all aggregate setup-plan choices. This must be addressed as recommendation-quality work without returning shown-identity authority to recommendation.

PR #105 is merged; its historical branch/checkpoint identifiers above are provenance, not pending work.

## 3. Immediate current priority — Night Step UI decomposition

The fresh audit and revised route were accepted by the user on 2026-09-06 (Australia/Sydney).
The user authorized updating this route and implementing **D1 only**. Work is performed in a
complete local Git workspace; the Chat/connector large-file writer restrictions are waived for
this campaign. All behavior, state-lifetime, validation and merge constraints remain applicable.

Primary reference: `docs/CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`.
Active handoff: `docs/NEXT_DEVELOPMENT_HANDOFF_2026-09-05_NIGHT_STEP_UI_DECOMPOSITION.md`.

## 4. Revised ownership-decomposition route

### 4.1 Fresh audit findings

Baseline `93f99e05`:

| Production owner | Lines | Bytes | Boundary concern |
|---|---:|---:|---|
| ClocktowerHostScreen.kt | 5790 | 349860 | 103 parameters / 39 on-callbacks; duplicated legacy night wiring |
| CampBoardGameHostApp.kt | 4517 | 258783 | application state, persistence, setup and transaction orchestration |
| ClocktowerNightStepUi.kt | 1203 | 62631 | 49 parameters / 13 on-callbacks; preparation, selection, effects and rendering |

Pair Manual already has a dedicated full-screen dialog and selection model. The existing
`resolveClocktowerPlayerDisplay` pure function already covers ordinary resolved choices; typed
numeric/boolean paths still construct their own display copies. Its return type remains the full
Storyteller step, so it is not yet a sanitized player-only payload.

The earlier Pair-Manual-first S1–S5 order is superseded by the route below. File size remains a
signal; acceptance measures ownership clarity and the code needed to understand a normal change.

### 4.2 Implementation sequence

| Step | Scope | Acceptance boundary |
|---|---|---|
| D1 | Remove superseded Host night fallback | one active night wiring path; preserve empty-step fail-closed behavior and all active callbacks |
| D2 | Consolidate display-result projection | ordinary, numeric and boolean conversions preserve proposition, truth flag, confirmation, snapshot and existing fallback differences |
| D3 | Separate structured preparation from rendering | reuse numeric/boolean adapters; prepared narrow models consumed by UI |
| D4 | Extract interaction families | single-target, two-target, information-choice and Storyteller-ruling ownership; complete typed Pair Manual input without a God context |
| D5 | Consolidate information publication coordination | preserve registration, authorization, observation/history, telemetry and reveal ordering; prove rejection and duplicate-publication behavior |
| D6 | Follow-on Host/App-root decomposition | incremental checkpoint state, snapshot codec/restore and setup ownership; no wholesale ViewModel migration |

D1–D4 form the near-term structural checkpoint; D5 is separately risk-gated. D6 is a follow-on
campaign requiring its own live audit and implementation scope. This route does not authorize all
steps at once. After the selected architecture checkpoint, resume UI-R5, then EPI-MQ and UX-R6.

### 4.3 Completed slice — D1

Branch: `codex/night-ui-legacy-path-cleanup`, based on `93f99e05`.

Status: **D1 implemented and T1/R2 checkpoint validated; draft PR #106 open, not merged**.

Remote validated code checkpoint: `427413447c5d7e400247a583a806af6302d556f7`.
[Draft PR #106](https://github.com/Jazz0006/CampBoardGameHost/pull/106).
This evidence update is docs-only; it does not replace the validated code checkpoint.

Local evidence:

- removed the legacy night renderer and its duplicate callback graph;
- Host reduced from 5790 to 5592 lines (349860 to 335500 bytes);
- night-card call sites reduced from two to one;
- pre-route source, active night callback region and trailing Dawn/Day bodies match baseline exactly;
- `git diff --check` passed; production diff is restricted to the Host file;
- local focused Gradle invocation did not execute tests: distribution download failed with
  `Network is unreachable`; no local GREEN is claimed;
- the existing R2 boundary shell script executed locally and passed (exit 0); this is not remote CI;
- product/document implementation commit: `6844026d12e1aa24bfe156ee77519b06444db8c8`;
- the initial automatic approval rejection was resolved by explicit user authorization on 2026-09-06
  to publish this branch, create a draft PR and run existing CI; no merge is authorized;
- command-line Git publication lacks HTTPS credentials in this workspace. Transfer the exact local
  file blobs/tree through the connected GitHub API and verify tree identity before creating the PR;
- remote transfer completed: all six blobs and tree `fc495d804f195507b4804a484a426029c9e9074b`
  matched the audited local tree; the remote checkpoint has parent `93f99e05`;
- [CI run 34000384515](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34000384515)
  passed. `compileDebugKotlin`, `compileDebugUnitTestKotlin` and `:app:testFast` executed;
  Android job logged `BUILD SUCCESSFUL in 1m 43s`. The test task was not FROM-CACHE/UP-TO-DATE;
- [R2 run 34000384564](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34000384564)
  passed; overall CI gate passed;
- normal T1 validation intentionally skipped full tests/APK assemble and ASP/Clingo for this
  Host-only structural change. No full-suite, APK or real-device validation is claimed;
- no new test or workflow added. D1 is complete at its scoped checkpoint; D2 display-result
  projection is the next implementation slice. Do not merge or expand PR #106 without authorization.

Production allowlist: `app/src/main/java/com/codex/campboardgamehost/clocktower/ui/ClocktowerHostScreen.kt`.
Documentation allowlist: root README plus the docs README, this roadmap, active handoff and audit reference.
No test changes are planned unless validation exposes a real coverage gap.

Reachability proof:

- unstarted FirstNight and Night already render their ready screens and return;
- started nonempty nights render the persistent table/current step and return;
- a started empty night previously reached the old fallback and threw `IllegalArgumentException`
  from `coerceIn(0, -1)`; retain failure explicitly before indexing;
- remove only the old night block, preserving the surrounding Dawn/Day fallback bodies exactly.

No state moves, new API, callback reordering, automatic empty-night completion or semantic fixes.
The existing active Host remains the owner. Empty-night recovery UI is a separate behavior change.

Evidence: existing materializer, checkpoint transaction and first-night reveal-handoff coverage;
exact source-region comparison and `git diff --check`; compile and T1 at the logical checkpoint.
Do not manufacture a RED or a permanent source-shape test for a deletion. Local Gradle baseline
was attempted but blocked before execution by the Gradle distribution network restriction; use
existing PR CI supplied executable verification as recorded above; local tests did not execute.

### 4.4 Current slice — D2 display-result projection

The user's subsequent “continue” authorizes D2 as a separate commit on draft PR #106. D1 remains
validated at its recorded checkpoint. No merge or D3 implementation is authorized by this slice.

Baseline: `d9ddff9ad2263852b6b47cb6a5d983aa24b72bce` (D1 plus docs-only evidence).
Status: **D2 implemented and T1/R2 checkpoint validated; draft PR #106 remains open, not merged**.

Validated code checkpoint: `0450b48ddaae7e930a9ba6a58e6ee02a7c3c0232`.

- [CI run 34000901077](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34000901077)
  passed; production/test Kotlin compilation and `:app:testFast` executed, not FROM-CACHE or
  UP-TO-DATE. Android logged `BUILD SUCCESSFUL in 1m 42s`.
- [R2 run 34000901065](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34000901065)
  and the overall CI gate passed. FAST includes the six new typed tests and existing regressions.
- Five changed blobs and the remote tree matched the locally audited D2 snapshot exactly; only
  three display-construction regions changed in Night Step UI. No Host/session changes.
- This follow-up evidence update is docs-only. The code checkpoint above remains the executable
  verification baseline. Full tests/APK/device validation is not claimed for this T1 checkpoint.

Next: **D3 — separate structured information preparation from rendering**, starting with narrow
inputs to the existing numeric/Boolean adapters. Do not move recommendation/telemetry/publication
ownership as a side effect; D5 remains separate.

D2 owns presentation conversion only, in the existing `ClocktowerPlayerDisplayResolution.kt`:

- ordinary resolved choices use the existing `resolveClocktowerPlayerDisplay` contract;
- numeric conversion receives a template, chosen value/truth flag and the existing confirmation /
  expected snapshot, applying the same per-field null fallbacks (including explanation footer);
- Boolean conversion uses matched-option presentation when available, otherwise retains the base
  presentation/truth flag. Its proposition always comes from the supplied confirmed draft;
- the legacy unreliable picker retains its recommendation list while clearing display options;
- helpers preserve the supplied expected snapshot, including mismatches; they do not authorize,
  repair or publish a decision, recompute truth, or change observation/registration/telemetry order.

Production allowlist: `ClocktowerPlayerDisplayResolution.kt`, `ClocktowerNightStepUi.kt`.
Test allowlist: `ClocktowerPlayerDisplayResolutionTest.kt`.
Documentation: this roadmap and active handoff.

Six new typed tests cover confirmed numeric output, null/empty per-field fallbacks, Boolean
matched/unmatched options, preservation of publication rejection, and legacy-list compatibility.
They were written before the new helper implementation; no executable RED is claimed because the
local Gradle distribution remains unavailable. Existing ordinary/manual parity tests remain.
Existing Empath source tests still protect preparation/telemetry wiring, which D2 does not move;
do not retire them on the strength of display-projection tests. Revisit in D3/D5 at the real owner.

No Host, session, state lifetime, ranking, legal candidate set, schema or player-renderer change.
The remaining UI `step.copy` only suppresses recommendations; it is not display-field
projection and remains out of scope. Use existing PR FAST/R2 CI as the executable checkpoint.

### 4.5 Decomposition invariants

- no God `NightStepContext` / giant parameter bag;
- no generic `Utils` / `Helpers` dumping ground;
- UI does not become gameplay/domain legality authority;
- UI-local transient state remains at the lowest correct owner;
- extracted modules do not depend on the whole Host/screen simply to avoid parameters;
- do not widen `private -> internal/public` merely for file extraction;
- do not split one role into one file by default;
- a slice must remove one coherent responsibility from the broad owner;
- behavior-preserving refactors must remain behavior-preserving.

## 5. Test/evidence policy for the architecture campaign

Root `AGENTS.md` is normative and integrates architecture pre-flight with risk-based test-first development.

For each slice:

```text
classify change type
-> identify real owner + durable contract
-> identify existing owning evidence
-> baseline when useful
-> add durable characterization only for a real uncovered risk
-> refactor
-> rerun smallest affected evidence
-> exact diff / invariant audit
-> retire superseded source-shape tests
```

Do **not** manufacture RED tests for file movement or decomposition. A genuinely new stable typed seam should receive durable contract coverage when existing tests do not already protect it.

## 6. UI-R5 — AFTER architecture checkpoint

Run a final real-device stabilization / feature-freeze pass after the selected Night Step ownership work reaches a clean checkpoint.

Cover at least:

- seating/start/reorder;
- game selection after seat confirmation;
- Minion/Demon introduction and bluff display;
- Pair recommendation + Manual;
- registration-sensitive information;
- Day Overview / nomination / vote flow;
- Night actor cue + target/action flow;
- same-night death-trigger behavior;
- Drunk actual/shown Storyteller presentation;
- long player/role names;
- Player Reveal privacy/readability/navigation.

UI-R5 is stabilization, not another broad visual redesign.

## 7. EPI-MQ / Productive Uncertainty — AFTER UI-R5

Primary authorities:

- `docs/EPISTEMIC_MISINFORMATION_QUALITY_AND_PRODUCTIVE_UNCERTAINTY_PLAN_2026-09-01.md`
- `docs/CampBoardGameHost_自动说书人玩家认知一致性算法改进方案_v2_2.md`

Quality ranking remains downstream of legal semantic authority.

A4/ZDD remains shadow/prototype unless separately reactivated and validated.

## 8. UX-R6 — AFTER EPI-MQ

Replace the legacy recommendation provider only after EPI-MQ correctness, quality, performance and rollout gates pass.

Preserve Manual independence, typed outcome identity, stabilized Storyteller UI and safe fallback behavior.

## 9. Explicitly deferred / not part of the current architecture slice

- generic impaired-information / aggregate-plan interaction-quality redesign;
- Demon Bluff recommendation-quality redesign;
- Public Claim History;
- Sequential Vote redesign;
- broad unsupported-script expansion;
- A4/ZDD production rollout;
- broad App-root decomposition unrelated to the active ownership problem;
- recommendation-quality algorithm redesign during a UI decomposition slice;
- gameplay-rule changes hidden inside structural refactoring.

## 10. Permanent architecture invariants

### Epistemic / information authority

```text
Composition
-> committed actual identity
-> committed shown identity
-> perceived ability
-> complete healthy legal/truth semantic domain
-> interaction-scoped registration
-> RELIABLE / POISONED / DRUNK reliability state
-> recommendation/manual selection
-> AbilityObservation
-> durable player-visible history
-> UI
```

Permanent rules include:

- UI/presentation is downstream of legality/truth authority;
- Manual remains permanent Storyteller authority within the legal domain;
- exact typed outcome identity survives presentation and confirmation;
- durable visible observations exclude Storyteller-hidden facts;
- structural refactoring must not alter rules or transaction ordering.

### Persistent Host Table

```text
stable typed ClocktowerSeatId
-> stable physical table position
-> Storyteller-private typed seat presentation
-> phase/action-specific center task
```

Actor/wake cue and target state are orthogonal presentation concepts. The final UI-N1 product decision does not require a separate wake acknowledgement phase.

## 11. Documentation authority / lifecycle

`docs/README.md` is the navigation entrypoint. This roadmap is the current status/priority authority.

Only one `NEXT_DEVELOPMENT_HANDOFF_*.md` may remain active in `docs/` root. Historical handoffs and implementation checkpoints belong under `docs/archive/` and are evidence only.

If an archived document contains `NEXT`, `PASS`, `READY`, or a historical SHA/PR state, do not treat it as current without reactivation by this roadmap and a fresh live-state audit.
