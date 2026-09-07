> **HISTORICAL SNAPSHOT — archived 2026-09-07.**
> PR #106 is now merged. All NEXT/draft/unmerged/authorization statements below describe
> the earlier checkpoint only. Current authority: `docs/CURRENT_DEVELOPMENT_ROADMAP.md`.

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

### Immediate next action — UI-R5 acceptance preparation

D1–D5 implementation and global change-set audit are complete on draft PR #106; no blocking
regression was identified. Do not resume D6 immediately. Prepare a traceable installable APK and
execute the UI-R5 device matrix below, then resolve only demonstrated defects. No merge is authorized.

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

### 4.4 Completed slice — D2 display-result projection

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

### 4.5 Completed slice — D3 structured information preparation

The user requested continuing after D2. Implement D3 as its own commit on draft PR #106,
without merging or expanding into D4/D5.
Baseline: `807b0eee3ef7317a6de5452dee79c32ef76cd123` (D2 plus documentation).
Status: **D3 implemented and FAST/R2 checkpoint validated; draft PR #106 open, not merged**.

Validated code checkpoint: `84af55e0ee0af2c434965bed1c942fba0e42ccc5`.

- [CI run 34001487622](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34001487622)
  passed. Production/test Kotlin compilation and `:app:testFast` executed (not cached/up-to-date);
  Android logged `BUILD SUCCESSFUL in 1m 42s`.
- [R2 run 34001487585](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34001487585)
  and overall CI gate passed. FAST includes all nine new preparation tests and D2 regressions.
- Existing R2 script and exact local diff audit passed; the rendering/publication tail is identical
  except two equivalent variable references. Six remote blobs and the full tree matched local.
- Night Step UI dropped 128 lines; the new 175-line preparation owner has no Compose or mutable
  state dependency. One obsolete source-string assertion was replaced by typed priority coverage.
- This evidence update is docs-only; the checkpoint above remains the executable baseline.
  Full tests/APK and real-device validation were not performed for this T1 checkpoint.

Next: **D4 — interaction-family rendering ownership**. Start with narrow single-target/two-target/
information-choice/ruling contracts, preserve the existing square-table and selection lifetime.
D5 publication coordination and D6 Host/App-root state migration remain separate scopes.

New owner: `ClocktowerStructuredInformationPreparation.kt` (no Compose imports or mutable state).
It projects an existing Host step plus actor/target identity into numeric or Boolean adapter input.
The prepared request owns only information semantics; it never captures the Host, roster, callbacks
or a mutable session. `ClocktowerInformationDecisionIdentity` is the five-field game/phase/round/
sequence/revision identity shared by adapter calls, not an application Context object.

- preserve Empath legacy-subject/truth/display/text fallback order and 0..2 bounds;
- preserve Chef unreliable-only and explicit-bounds gating; registration routes stay separate;
- preserve first matching projected numeric truth classification;
- preserve automatic/default/unreliable/fallback recommendation selection without reranking;
- preserve exact ordered Boolean source/target matching and typed-only recommended value;
- call existing numeric/Boolean adapters with unchanged identity, style, history and pressure inputs;
- keep target editing, confirmation, registration, telemetry and publication with current owners.

Allowlist: new preparation owner, `ClocktowerNightStepUi.kt`, new
`ClocktowerStructuredInformationPreparationTest.kt`, `StructuredEmpathInformationAdapterTest.kt`,
this roadmap and active handoff. No changes to Host, lower adapters, session, schema or persistence.

Nine new typed tests characterize preparation boundaries and compare model choices, snapshots and
confirmed drafts with the existing adapters. The old assisted-Empath source-string recommendation
assertion is replaced by the direct priority-order test; telemetry and Host history assertions stay
because those owning seams have not moved. Tests were written before implementation; no executable
RED is claimed with local Gradle dependencies unavailable. Use existing PR FAST/R2 checkpoint.

### 4.6 Completed slice — D4.1 single-target and ruling renderers

The user requested continuing after D3. D4 is split into D4.1 (single-target/ruling) and D4.2
(two-target/Manual contracts) to keep interaction lifecycle verification bounded. This turn
implements D4.1 on draft PR #106; it does not claim the whole D4 campaign complete.
Baseline: `04c26112a3dab13f041b6ebdfcf096550036dc2a`.
Status: **D4.1 implemented and FAST/R2 checkpoint validated; draft PR #106 open, not merged**.

Validated code checkpoint: `519dbd34c7bbfad460290ea983c0986559aa0ef2`.

- [CI run 34004925613](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34004925613)
  passed. Production/test Kotlin compilation and `:app:testFast` actually executed;
  Android logged `BUILD SUCCESSFUL in 1m 47s`. FAST includes all six new contract tests.
- [R2 run 34004925601](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34004925601)
  and the overall CI gate passed. The existing R2 script also passed locally.
- Exact diff audit verified unchanged preparation, Fortune Teller/Chambermaid branches and
  downstream confirmation/publication. All six remote blobs and the full tree matched local.
- `ClocktowerNightStepUi.kt` decreased from 1043 to 913 lines (130 removed net); the extracted
  renderers consume narrow presentation/events and introduce no mutable state owner.
- This evidence update is docs-only. Full tests/APK and real-device validation were not performed.

- `ClocktowerSingleTargetInteractionPresentation.kt` holds immutable selection/presentation and
  the scoped SelectSeat/ShowResult/Previous/Next event contract. It receives already-authoritative
  target seat sets; it does not compute game-rule eligibility or own mutable selection.
- `ClocktowerSingleTargetInteractionUi.kt` renders two families: ordinary ability target tasks
  and Storyteller rulings. Both use the existing square-table dialog and accept only prepared
  presentation, seats, language, navigation availability and one typed event callback.
- Night Step retains the existing candidate helpers and maps seats/events to its existing callbacks.
  The renderers cannot access PlayerCard rosters, the whole step, recommendation services or Host.
- Preserve Red Herring visibility/no actor cue, disabled real-action behavior, Ravenkeeper reveal
  gating, automatic-ruling hiding, missing-Mayor disabling and the Mayor-dies secondary action.
- Key grouped renderer calls by action so switching roles does not accidentally retain transient
  child UI state that previously belonged to separate action branches. Parent selection remains.
- Fortune Teller/Chambermaid blocks and all downstream information confirmation, telemetry and
  publication remain unchanged. No lower square-table or Host modification.

Allowlist: the two new production files above, `ClocktowerNightStepUi.kt`,
`ClocktowerSingleTargetInteractionPresentationTest.kt`, roadmap and active handoff.
Six direct tests protect visibility, candidate/actor separation, result permission and ruling
availability. Existing target-legality and square-table tests remain. Tests precede implementation;
local Gradle dependencies remain unavailable, so no executable local RED is claimed.

Next after this checkpoint: **D4.2 — two-target/Manual contract audit and extraction where useful**.
Do not advance to D5 publication coordination until D4 scope is explicitly closed.

### 4.7 Completed slice — D4.2 Manual typed presentation and selection ownership

Baseline: `beaec9a64abbaf6f34eb97698483ed7ab2892e66`; live main remains
`93f99e0576be7b93d479ffa931bae3e4083c25af`. The user requested continuing after D4.1.
Status: **D4.2 and D4 checkpoint validated; draft PR #106 open, not merged**.

Validated code/test checkpoint: `e24c0518aa1e2fb826099f05930f3f912d068c89`.

- [CI run 34005528591](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34005528591)
  and [R2 run 34005528598](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34005528598)
  passed. Test Kotlin compilation and `:app:testFast` executed; `BUILD SUCCESSFUL in 44s`.
  Production compilation was reused from cache on this test-only correction.
- Initial implementation `f97ee5ddbeee8b182dc78b5b5ad91aa7cab8423b` compiled production/test
  Kotlin, but CI 34005378402 ran 1131 tests with one new fixture failure: empty AllOf is rejected
  by the domain constructor before selection. The correction uses a constructible mixed proposition;
  no production code changed. This was a test-fixture error, not a behavioral RED.
- Exact transition, UI-tail, authority/parser and Night Step one-argument audits passed; local R2
  passed. Nine remote blobs/full tree matched local, followed by an exact test-file correction.
- Manual UI decreased from 421 to 246 lines; the pure selection model occupies 125 lines.
  No obsolete production convenience factory or source-string test was retained.
- This evidence update is docs-only. Full tests/APK and real-device validation remain unperformed.

Fresh audit decisions:

- Fortune Teller and Chambermaid already have separate narrow renderers. Their Boolean-confirmation
  vs determined/option-result paths differ; retain these existing owners and wiring. A generic
  two-target wrapper would add indirection without removing a responsibility.
- Manual UI still parsed AnyOf/AllOf grammar independently of its authority adapter and contained
  the pure selection model. Move the model to `ClocktowerPairManualSelectionModel.kt` and project
  typed Manual presentation in the existing `ClocktowerPairManualAuthority` using its existing
  pair-key parser. The renderer now accepts prepared presentation and does not interpret propositions.
- This projection consumes the existing authoritative candidates; it never creates legal outcomes,
  reranks recommendations or authorizes publication. Keep the exact original option as the result.
- Preserve malformed-option filtering, duplicate/zero first-match behavior and seat normalization.
  Original source options participate in presentation equality, including ignored options, to preserve
  the old candidate-list-based remember reset. Interaction key, open/close state, selection transitions,
  actor/seat rendering and confirmation-before-close callback order remain unchanged.

Allowlist: Night Step (one argument only), Manual authority, Manual selection UI, new pure selection
model, existing Manual selection/authority/display-resolution tests, roadmap and active handoff.
Existing selection and display tests migrate to the prepared-input API; three new characterization
cases cover malformed/duplicate inputs, zero/role switching and equality/reset identity. Existing
legal-domain projection test now verifies every projected option resolves through the selection model.
No new source-string tests. Local Gradle dependencies are still unavailable; no executable RED claimed.

D4 scope is closed at this successful D4.2 checkpoint: existing two-target owners are accepted,
Manual now has a typed preparation boundary, and single-target/ruling owners were completed in D4.1.
Next: **D5 publication coordination audit**, with a separately bounded transaction contract before
any extraction. D6 and real-device UI-R5 remain later gates. Keep PR #106 draft; do not merge.

### 4.8 Completed slice — D5.1 Host publication handoff ordering

Baseline: `123938fab065bfec0bc202e333a5b36bf2ba4dd2`; live main remains
`93f99e0576be7b93d479ffa931bae3e4083c25af`. User requested continuing after D4.
Status: **D5.1 implemented and full CI/R2 checkpoint validated; draft PR #106 open, not merged**.

Validated code checkpoint: `a76fb9accc956316e0d94cb277a552842130da4d`.

- [Full CI run 34006496894](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34006496894)
  passed: production/test Kotlin compilation, `:app:testDebugUnitTest` via `:app:testFull`,
  and `:app:assembleDebug` executed. Android logged `BUILD SUCCESSFUL in 3m 16s`.
- ASP contract tests, Real Clingo cross-validation and the overall CI gate passed.
  [R2 run 34006496861](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34006496861)
  passed as well. This full checkpoint includes the preceding D1–D4 changes.
- Five remote blobs/full tree matched local. Exact audit proved Host outside authorization/handoff
  unchanged, and history body unchanged modulo indentation; existing local R2 script passed.
- The six new contract tests supplement existing reveal/migration/session coverage. No source-string
  tests retired, because remaining telemetry/history guards cover unmoved owners.
- This evidence update is docs-only. Debug APK was built, but no real-device validation was performed.
  D5.2 remains open; D5 overall is not yet complete.

Fresh publication audit:

1. Night Step applies selected registration, then selection telemetry, then calls Host publication.
   Structured Boolean and numeric confirmation paths have intentionally different preprocessing.
2. Host authorizes exact confirmation snapshot/current revision (legacy unconfirmed display allowed).
3. Only authorized requests enter first-night migration/parity/deduplication. Already-published
   first-night decisions reopen reveal without a second observation/history record.
4. Fresh requests record private observation, format/write history, then set player-display state.
   The private recorder retains its own authorization recheck and existing legacy role fallback.

D5.1 extends the existing `ClocktowerPlayerRevealHandoff.kt` with typed authorization and a synchronous
ordered effect executor. It owns only sequencing, never session state or deduplication. Five scoped
callbacks preserve lazy authorization, short-circuiting, exactly-once invocation within one attempt,
and exception propagation. This is not an atomic/rollback transaction; earlier effects are not undone
if a later callback throws. No new freshness guarantee or non-first-night deduplication is claimed.

Host delegates authorization and effect order; history body stays byte-identical modulo indentation.
Night Step registration/telemetry/confirmation, migration, private observation, state and persistence
owners remain unchanged. Allowlist: existing handoff, Host, existing handoff test, roadmap/handoff.
Six new tests cover effect order, duplicate reopen, denial, each exception boundary, legacy allowance
and real-adapter confirmation with missing/mismatched/stale snapshots. Existing migration/session
coverage remains. No source-string retirement here: remaining telemetry/history guards own unmoved seams.

Because this touches central publication orchestration, use `[full-ci]` for the checkpoint (full Android
JVM tests, debug assemble and selected external gates), plus R2. Local Gradle dependencies are unavailable;
no executable local RED claimed. Current slice is D5.1 only; PR stays draft and merge is not authorized.
Next D5.2: audit the upstream registration/telemetry and first-night migration effects for a coherent
owner. Do not treat pre-authorization telemetry or partial failure as a behavior fix hidden in refactoring.

### 4.9 Current slice — D5.2 first-night migration preparation and resolution

Baseline: `b545637f6c8318c9192343e3130e272304be5520`; live main remains
`93f99e0576be7b93d479ffa931bae3e4083c25af`. User requested continuing after D5.1.
Status: **D5.2 validated; selected D1–D5 structural checkpoint complete; draft PR #106 unmerged**.

Validated code checkpoint: `7f2c67603bc70022033cd80a508baa926bf70db9`.

- [Full CI run 34008604939](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34008604939)
  passed. Production/test Kotlin compilation, `:app:testDebugUnitTest` via `:app:testFull`, and
  `:app:assembleDebug` executed; Android logged `BUILD SUCCESSFUL in 2m 31s`.
- ASP contracts, Real Clingo cross-validation and overall CI gate passed.
  [R2 run 34008604873](https://github.com/Jazz0006/CampBoardGameHost/actions/runs/34008604873)
  and the existing local R2 script passed.
- Seven remote blobs and the full tree matched local. Request conversion was exact modulo indentation;
  Host outside the migration slice stayed unchanged. Host decreased from 5586 to 5473 lines (113 net).
- All seven new adapter/resolution tests passed with the complete suite, including preceding D1–D5.1.
  This evidence update is docs-only. APK construction is verified; real-device validation is pending.

- Extract the existing Host-to-migration request conversion into
  `ClocktowerFirstNightInformationRequest.kt`. Explicit inputs are the display step, phase/round,
  roster, script/seed/poison, language and style; no captured Host or mutable context. The existing
  conversion body is preserved, including family/actor gating, selected-option fallback, stable
  deduplication, rank/metadata, localized legacy parsing and authoritative pair observation resolution.
- Move publication branch policy into `FirstNightInformationMigration.resolvePublication` with
  typed Published / AlreadyDisplayed / LegacyFallback results. Only Published replaces Host state.
  Host still records parity telemetry before invoking the resolution; the supplied shadow comes from
  the same current migration/request. Existing publish/display/lifecycle methods remain authoritative.
- Keep registration and selection telemetry in Night Step. Their role-specific timing precedes Host
  authorization and they do not share the migration state lifetime. Combining these into one generic
  transaction would either capture Host state or silently change callback/failure order.
- Preserve legacy mismatch fallback (allowed reveal without a migrated fact), pair-authority mismatch
  acceptance, duplicate reveal and first displayed fact. No rollback, new deduplication, rule, ranking,
  persistence or confirmation change. D5.1 handoff and observation/history/reveal tail remain unchanged.

Allowlist: new request adapter/test, Host, existing migration/test, roadmap and active handoff.
Seven new tests cover request gating, identity/reliability/fallback, duplicate ranking/metadata,
structured pair authority, mismatch fallback, pair publication and retention of displayed facts.
Existing lifecycle/poison/pair/confirmation tests remain; no obsolete source assertion is created.
Run full CI/R2 for this migration/publication checkpoint; no local executable RED is claimed.

The successful D5.2 validation closes the selected D1–D5 structural checkpoint. Remaining broad Host
state, legacy observation fallback and App/persistence ownership belong to D6, not mandatory extra
micro-extractions. Next is architecture acceptance and UI-R5 real-device stabilization before the
EPI-MQ/UX-R6 route. PR #106 remains draft; merge requires explicit user authorization.

### 4.10 Global audit and concrete next route — 2026-09-06

Reviewed PR head `76321c4e2c9f14fc62a3853f479841733de784c1`, main
`93f99e0576be7b93d479ffa931bae3e4083c25af`; full validated code `7f2c67603bc70022033cd80a508baa926bf70db9`.
Conclusion: no blocking behavior/ownership regression found across D1–D5 and immediate dependencies.
Correct one extra EOF blank line; all other edits in this audit are documentation. Full CI/R2 evidence
from D5.2 remains the executable baseline; semantic production content is unchanged.
Detailed findings: `CLOCKTOWER_NIGHT_STEP_UI_DECOMPOSITION_AUDIT_2026-09-05.md`, post-implementation audit.

Accepted residual limits: broad reveal payload, non-atomic publication and pre-authorization telemetry,
caller-matched migration shadow, and large Host/App state owners. Current calls preserve their contracts;
these are future design constraints, not reasons for more incidental extraction in PR #106.

Next steps:

1. Prepare an installable test APK from the accepted branch, recording SHA, package/version and build
   provenance. Existing CI proves assemble succeeded but does not itself prove a downloadable APK was
   retained. Reuse an available matching artifact; otherwise build/export using existing release tooling.
2. Run the following device matrix. Start with one complete 8–10-player manual/assisted game through
   the second night, then targeted automatic/edge scenarios and a 15-player layout pass. Record device,
   Android version, app SHA, scenario, expected/actual result and screenshot/log for each failure.
3. Fix only reproducible regressions or release-blocking usability defects with owning tests where useful.
   No broad redesign, recommendation-policy change or D6 state migration during stabilization.
4. On device pass, review current head/checks and present merge-ready evidence for explicit authorization.
   Reuse current code evidence when only docs/formatting changed; meaningful fixes require affected/full gates.
5. After UI-R5, continue EPI-MQ correctness/quality and UX-R6 provider replacement. D6 remains a separately
   planned ownership campaign, selected when state/persistence changes justify it.

| Device scenario | Acceptance condition |
|---|---|
| First-night Minion/Demon reveal | Opens/closes without crash; correct player-facing information and return step. |
| Single-target roles and back/next | Selection stays with the correct role; actor cue is independent of eligibility; no stale child UI across actions. |
| Fortune Teller / Chambermaid | Ordered two-seat selection, edit/deselect and deterministic/discretionary result controls remain correct. |
| Pair Manual | Role/pair/zero-case selection, cancel/reopen and confirm resolve the chosen clue; candidate changes reset appropriately. |
| Drunk/poison and Spy/Recluse routes | Correct actual/shown identity, legal candidate route and registration; no old result after a changed decision. |
| Reopen first-night result | Reveal opens again; history/observation is not duplicated and original migrated fact is retained. |
| Mayor/succession and death-trigger role | Manual/automatic gating, disabled targets and Ravenkeeper reveal remain usable. |
| 15 seats, long names, navigation/lifecycle | Labels/buttons usable, no inset overlap; background/foreground and supported restore flow preserve intended state. |

Device acceptance is pending. This plan does not claim physical-device execution or authorize merge.

### 4.11 Decomposition invariants

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
