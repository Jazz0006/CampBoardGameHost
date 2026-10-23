# Host UI Responsibility Refactor Plan — FINAL v3 (2026-10-23)

**Status (2026-10-23):** FINAL v3 implementation contract; Batch 2 temporary AI→UI adapter dependency explicitly approved (§Batch 2 and [review §13](HOST_UI_RESPONSIBILITY_REFACTOR_REVIEW_2026-10-23.md)). **Already merged to `main`:** Batch 1 [#355](https://github.com/Jazz0006/CampBoardGameHost/pull/355) → `7486d05c`; Batch 2a [#356](https://github.com/Jazz0006/CampBoardGameHost/pull/356) → `e7ac9481`; Batch 2 [#357](https://github.com/Jazz0006/CampBoardGameHost/pull/357) → `1fe855e8` (full Android CI #4597 / R2 #4151 successful on reviewed head `5871c355`). **P1 #358 is now merged** as `01ef5e14` (full PR CI #4606/R2 #4158; main CI #4607 successful). **Batch 3 #359 (`85d3833c`, CI #4608/R2 #4159) is implemented on its review branch, not yet merged**; see [acceptance](HOST_UI_BATCH3_ACCEPTANCE.md). **Batch 4 implemented for stacked review** on unmerged Batch 3; see [acceptance](HOST_UI_BATCH4_ACCEPTANCE.md). **Not yet implemented:** Batch 5. Historical source-size / PR / baseline facts below describe the original planning date unless explicitly marked current.
**Current implemented baseline before merging this docs PR:** `main` `1fe855e80efb006717275341b17b8a9d9e805fa7` (#357). The independent dead-nominee P1 [#352](https://github.com/Jazz0006/CampBoardGameHost/pull/352) was merged as `edcbf5a4`. The **original planning baseline** was `7baf014b` (#354, CI #4585 / R2 #4142); historic references to #352 as Draft are no longer current. Continue future batches from the latest `main`, not the original planning base.
**History:** v1 `133fe98b` and v2 `2687e6a3` were reviewed in [HOST_UI_RESPONSIBILITY_REFACTOR_REVIEW_2026-10-23.md](HOST_UI_RESPONSIBILITY_REFACTOR_REVIEW_2026-10-23.md) (§1–11 on v1, §12 on v2). v3 resolves every §12 item; §9 lists changes per version. Where this plan and the review differ, this plan governs.
**Scope:** `CampBoardGameHostApp.kt` (49,208 B), `clocktower/ui/ClocktowerHostScreen.kt` (51,102 B, over the 50,000 B hard limit), their adapter `ClocktowerSessionHost.kt` (30,907 B), and `ClocktowerNightHostProjection.kt` (11,883 B).
**Not in scope:** Session rules and confirmation APIs, AI prompts/parser/memory, setup diversity, persistence format, visual design.

## 1. Why the previous split did not help

The previous decomposition moved code into new files but left state and responsibility in place:

| Symptom | Evidence |
|---|---|
| App root is still one ~840-line composable with ~40 `remember` values mixing navigation, preferences, game lifecycle, restore/persist and dialogs | `CampBoardGameHostApp.kt:104-944` |
| Extracted helpers call back into the App through getter/setter lambdas | `openingDraft.beginPrepared(..., { storytellerOperationMode }, { screen }, ::commitAndStartTroubleBrewingGame, { screen = it })` (`:391-426`) |
| Manual invalidation because the Session is not observable | `clocktowerUiTick` + ~20 `refreshClocktowerUi()` calls across App and `ClocktowerSessionHost` |
| Duplication | `SettingsContent` called twice with identical 10 args (`:803`, `:909`); TB and NGJ `PlayerCard` construction (`:357`, `:466`); `persistWith { save }` twice; `confirmClocktowerStep` in two files; `StorytellerPhase→ClocktowerPhase` mapping in two files; name→seat lookup ~10 times |
| `ClocktowerJudgeScreen` has ~60 parameters | `ClocktowerHostScreen.kt:40-101` |
| Role-per-parameter fan-out: 11 game-lifetime per-role drafts in `ClocktowerSessionHost`, each with `onSelectX`/`onConfirmX`, reassembled by `when(action)` in the screen | `selectedNightName` (`:625`), `onSelectName` (`:838`), `advanceNightStep` (`:702-736`). Stages 3–5 grew the file 46.8 KB → 51.1 KB this way. |
| ~470 lines of non-UI decisions inside a composable | role-name string → numeric metric map (`:456`), AI pending/key/stale (`:482-575`), opening-plan revalidation (`:581-611`), duplicate unreliable-information logic (`:258-317`), succession/readiness (`:650-669`) |
| Host facts read through UI cards | `ClocktowerNightHostProjection(cards, sessionState)` derives Chambermaid/Mayor targets and drunk/poisoned ability state from `PlayerCard`; legal targets use `publicAliveCards` (`eliminatedRound` on cards) |

## 2. Binding invariants (every batch)

1. **One writer.** `ClocktowerGameSession` / `ClocktowerSessionState` is the only writable game state (roles, seats, alignment, life, status, phase, confirmed actions, ordered Observations). No batch adds or changes a Session confirmation API or the `ClocktowerSessionJson` format.
2. **One-way dependency.** Rule computation reads only Session, the built-in ruleset and the current input. `PlayerCard`, names, text, colours and layout are UI projections and never flow back into rule code. `clocktower/session` and `clocktower/domain` gain no Compose/Android imports.
3. **Drafts have no authority.** Current-step seat selection, previews, highlights and button state are disposable; never persisted, never a pending event, never a second step cursor. Their lifetime becomes *narrower* than today (current interaction instead of whole game).
4. **Rules separate from advice.** Host computes fixed results, structural constraints and the real Storyteller-discretion boundary. AI only advises within that boundary; it never makes a player's own target choice and never bypasses the Session confirmation entry points. Removing AI leaves MANUAL fully playable.
5. **Hints are not legality.** Gold/cyan dashed outlines, first-choice highlights and Pair shortlists help the Storyteller; they must not become a general Host legal domain, scoring, world enumeration or blanket veto (see §3.2 table).
6. **D2 unchanged.** Select → fullscreen preview → back to change → preview again; only this role's **Next** records the one last-previewed Observation; no interim previews recorded; no cross-role Previous; an interrupted unconfirmed step is simply redone.
7. **Preserve:** TB 5–15, NGJ 5–6, MANUAL/AI_ASSISTED/AI_AUTOMATIC, joint opening plan / later-night AI / strategic memory, both inter-game diversity behaviours, privacy, AtomicFile persistence, safe continuation, idempotency and stale checks.
8. **AI memory stays metadata.** Opening plan, strategy cursor and response ID stay in `ClocktowerActiveGameMetadata`, never merged into Session state.
9. **Replace means delete.** When a production call chain is migrated and verified, the replaced interfaces, UI cursors, duplicate state and exclusive low-value tests are removed in the same batch. No permanent dual path, no generic Context/Manager facade, no one-function-per-file extraction.
10. **Behaviour-preserving.** Batches restructure; they do not change game rules. Known rule questions found while restructuring are recorded in §8, not fixed inside a refactor batch.

## 3. Batches

Each batch is a separate PR from latest `main`, Draft until reviewed. Validate with `./gradlew :app:testFast` locally and full Android CI + R2 on the exact code SHA. **Full means the Android tests job log shows `./gradlew :app:testFull :app:assembleDebug`**: PR runs use `testFast` (which excludes `ClocktowerD2UiTest`) unless the commit message contains `[full-ci]`, so a green PR status alone is not acceptance. After merge, also check the `main` push run. `ClocktowerCompleteAppUiTest` (drives the real App root) is the regression net throughout. File-size numbers are references; the 50,000 B limit remains a hard requirement but is not by itself acceptance — each batch also has measurable ownership checks.

### Batch 1 — One interaction-scoped night draft

**Change**
- Replace the 11 game-lifetime drafts in `ClocktowerSessionHost` (demon attack, poison, monk, mayor, butler, ravenkeeper, FT×2, Chambermaid×2, red herring) with one UI value, reusing the existing interaction ID type:

  ```kotlin
  data class NightStepDraft(
      val interactionId: ClocktowerInteractionId, // existing type, no new ID scheme
      val selectedSeats: List<Int> = emptyList(),
  )
  ```
  `remember`ed by **`(gameId, phase, round, interactionId)`**. `ClocktowerInteractionId` carries no round (e.g. `other_night:role:Imp` is the same every night), so `(gameId, interactionId)` alone would let an unconfirmed Imp target survive from night 2 into night 3. This is the key the Imp self-kill successor already uses. Seats, not names: the Session works in seats and today every confirmation converts names with `clocktowerSeatFor`; convert once at the UI edge instead.
- The confirmation path re-checks the live `gameId`, phase, round and interaction ID against the draft before dispatching, keeping the existing revision/idempotency/stale checks. A late callback from a previous step, night or game never confirms into the current one. Drafts are never stored in Session or recorded as events.
- `selectedSeats` is not a generic candidate domain. The current `ClocktowerNightAction` / question type decides how many seats are needed, whether order matters and whether the input is complete. No strategy registry or factory.
- Screen API: one `draft`, one `onDraftChange(NightStepDraft)`, one `onConfirmNightStep(step, draft, preview)`.
- **Three owners, no overlap:** the night screen owns the Next *intent*, the current-interaction gate and the last preview; `ClocktowerSessionHost` is the single dispatcher (`when (action) → session.confirmX(...)`) and the only place that knows Session APIs; the Session is the only fact writer. Neither the screen nor the dispatcher adds legality branches or a Host veto beyond the existing Session APIs.
- `NightStepDraft` replaces only the 11 cross-step role targets. Existing short-lived, step-local UI editing state (e.g. Pair, Sage pages) that already keeps single-Observation semantics is not rewritten for uniformity.
- Delete: per-role `onSelectX`/`onConfirmX` parameters, `selectedNightName` and `onSelectName` reassembly, the manual draft resets in `onCompleteNight`/`onAdvanceFromFirstNight`. Downstream `ClocktowerNightStepCardLocalized` collapses `selectedName`/`fortuneTeller*`/`chambermaid*`/`redHerring` and five select callbacks into `draft` + `onDraftChange`.

**Behaviours to preserve exactly**
- Ordinary target changes call `session.recordPlayerInput()` exactly as today. That revision is a staleness mechanism for AI requests, **not** a game fact or action event; its timing is not changed.
- **Red Herring** stays draft-only: changing it does not call `recordPlayerInput()`; its accepted seat is always read from Session and written only on its step's Next.
- FT, Chambermaid and Ravenkeeper target changes invalidate the last preview; re-preview is required before Next.
- FT seats still feed the AI request fingerprint (`fortuneTellerSelectedNames`), `fortuneTellerSubjectSeats` and stale detection.
- Chambermaid is revalidated as two distinct eligible targets; other roles keep their approved selection behaviour.
- Imp self-kill successor and Demon bluffs are **not** night-step targets; their own inputs and confirmations stay as they are.

**Pre-implementation audit (required):** list every production reader/writer of the 11 drafts and confirm each is read only within its own interaction (`butlerMaster` → common materializers, `ravenkeeperTarget` → other-night materializers, `fortuneTeller*` → FT materializer/AI). Any cross-step reader must read the confirmed Session fact instead.

**Exit checks (measurable)**
- [ ] `ClocktowerJudgeScreen` and `ClocktowerNightStepCardLocalized` have no per-role draft parameters or `onSelectX`/`onConfirmX` callbacks.
- [ ] No `remember(gameId)` night-target state remains in `ClocktowerSessionHost`.
- [ ] Compose regressions: draft cleared on next interaction; **an unconfirmed night-2 Imp target is absent when the Imp wakes on night 3; a late callback from the previous night writes nothing**; target change invalidates preview and AI suggestion (FT/Chambermaid require re-preview); Red Herring change leaves `playerInputRevision` unchanged; D2 Next writes exactly one Observation.
- [ ] The consumer audit of all 11 drafts is written in the PR description; a key change alone does not replace removing cross-step reads.
- [ ] `ClocktowerD2UiTest` (only direct `ClocktowerJudgeScreen` harness) updated; `ClocktowerHostScreen.kt` < 50,000 B.

### Batch 2a — Seat-based Host night facts (replace card-based projection)

Removing `PlayerCard` from rule code (Batch 2) requires first moving the Host facts that are read through cards today.

`ClocktowerNightHostProjection.kt` mixes two things: card-based **fact evaluation** and the UI **step builders** `otherNightMaterializers()` (Undertaker, Monk, Imp, Mayor, Ravenkeeper, Sage steps, instructions and localized text). Batch 2a moves only the first.

**Change**
- Re-express the fact reads as seat-based reads of `ClocktowerSessionState`: current Demon seat, death-triggered wakes, effective ability subject and drunk/poisoned state per role, Chambermaid eligible seats, Mayor redirect eligible seats.
- Replace `publicAliveCards`-based target sets with Session `alive` reads, preserving the exact current sets (§2 inv. 10).
- Place rule-only helpers beside `clocktower/session/ClocktowerNightFlow.kt` / `HostInformationResolvers.kt`. Card-to-label conversion stays in UI.
- Delete the replaced card-based fact evaluation in the same batch — no two fact paths.
- **Keep** the step builders that production still uses, as pure UI projection that consumes the new Session facts / seat projection. They are deleted in Batch 3 when `ClocktowerNightHostScreen` takes over their assembly. Do not rewrite every role's UI just to remove the class early.

**Exit checks**
- [ ] No Host decision in the night path takes `PlayerCard`, player names/display text or `publicAliveCards` as its authoritative input (grep check on the new helpers and their callers).
- [ ] Remaining step builders only read already-computed Session facts / seat projections to produce display.
- [ ] Current target sets and night display compared item by item against `main`; unchanged. Existing night regressions pass; added JVM tests for drunk/poisoned subject and Mayor/Chambermaid seat sets.

### Batch 2 — Pure Host step model and separate AI overlay

**Change**: move decision code out of the composable into two plain-Kotlin functions:

1. `hostNightStep(sessionState, ruleset: ValidatedClocktowerRuleset, draft: NightStepDraft): HostNightStep` — built on the existing `sessionState.nextNightInteraction(ruleset)` (not a second step engine). Returns: the current interaction; required input shape; numeric/pair resolution; whether the step is private information or evil information; target readiness; Imp succession (`impSelfKillSuccession`); the real discretion boundary. It does **not** read Compose preview state, AI busy/error or `PlayerCard`.
2. `nightAiOverlay(...): NightAiOverlay` — **temporary read-only AI → UI adapter**, not part of Host Core: validates request identity, game/input revision, script/role/seat/metric/subjects; reports stale/blocked; returns the validated suggestion and Mayor recommendation. It must never mutate Session, never expand the current Host step's available choices, and must return an empty overlay in MANUAL. AI_AUTOMATIC still waits for actual player input and a live tap to show information. **Approved exception (2026-10-23):** its signature may still take `ClocktowerNightStepUi`, `PlayerCard`, localized text/language and existing AI matchers because those old helpers have active production consumers. This exception applies **only** to the AI/UI adapter, not to `hostNightStep`, Session or rule computation. Do not create replacement compatibility abstractions merely to hide these types in Batch 2/3.

`nextEnabled` is derived in the UI from `HostNightStep` readiness + current preview, not inside `hostNightStep`.

**Four categories — keep distinct, never merge into one "legal candidates" list**

| Category | Owner | Examples / constraint |
| --- | --- | --- |
| Fixed game result | Host/Session read-only evaluation | Healthy Chef number; fixed Fortune Teller Boolean. AI never guesses a unique answer. |
| Structural constraint | Current step / Session confirmation boundary | Existing seats, two distinct targets, correct interaction identity and revision |
| Real Storyteller discretion | Host returns the narrow range; AI or human decides | Active Spy/Recluse registration, drunk/poisoned misinformation, Mayor redirect |
| Visual hint / suggestion | UI projection and AI overlay | Gold/cyan dashed outlines, recommended highlight, Pair shortlist — **not** the full legal domain and never blocks a tap |

Real misplays stay recordable through the existing actual-shown-information path; Session never disguises them as the computed truth. No new audit events for ordinary choices.

Also:
- Derive numeric metric from `step.informationQuestion` / step metadata; delete the `"Chef"/"Empath"/"Clockmaker"/"Chambermaid"` string map.
- Merge the duplicated proposition logic of `unreliableInformationCanBeRecorded` / `recordReliablePrivateInformation` into one `shownPropositionFor(preview): InformationProposition?`.
- Use the App's `BuiltInClocktowerRulesetCatalog` instance instead of a second `fromContext` lookup (cached; clarity only).

**Exit checks**
- [x] `hostNightStep` reads the active `Session.nextNightInteraction(ruleset)` and current draft, not UI-provided role/action/reality or `PlayerCard`, `Context`, Compose, label strings or AI state (PR #357 audit issue 1; full CI #4597 / R2 #4151 passed).
- [x] **Approved temporary exception for `nightAiOverlay`**: read-only AI → UI matching may reference `ClocktowerNightStepUi`, `PlayerCard`, language and existing AI request/matcher helpers. It cannot supply Host facts, write Session, narrow legitimate manual selection to an AI suggestion, or unblock stale advice. The planned pure-semantic interface for the *AI module itself* is deferred to the separately scoped AI redesign; this is accepted, not a Batch 2 merge blocker.
- [ ] No role-name string literals in UI decision code for metric/role classification.
- [ ] JVM tests: unique numeric; Spy/Recluse discretion; drunk/poisoned source; Mayor redirect and Imp succession; wrong target; stale revision / wrong seat / wrong metric AI rejected; MANUAL overlay empty.
- [ ] With the AI overlay replaced by an empty overlay, a MANUAL game completes in `ClocktowerCompleteAppUiTest`.

**Deferred AI redesign boundary (not a Batch 3 prerequisite):** existing `pendingGlobalFirstNightPairDecision`, `pendingGlobalNightScalarDecision`, `ClocktowerResidualDisplayMatcherV2` and `ClocktowerJointOpeningDisplayMatcherV2` still serve production AI requests/matching. Keep them functional until the later AI recommendations redesign replaces their actual consumers in one migration; then remove the dead paths. Do not rebuild AI prompts/parser/strategic memory or add a second Host candidate/rules engine in this Host UI refactor. See the decision appended to [the architecture review](HOST_UI_RESPONSIBILITY_REFACTOR_REVIEW_2026-10-23.md).

### Batch 3 — Judge screen becomes a router

- `ClocktowerJudgeScreen` routes only: player fullscreen preview → New Demon → Demon bluffs → Day (`ClocktowerDayHostScreen`, already separate) → Night.
- New `ClocktowerNightHostScreen` consumes `HostNightStep`, `NightAiOverlay` and the current draft; owns `lastInformationPreview`/`playerDisplayStep`, bound to the current interaction ID (same-step return may re-preview; a preview never carries over to another step); owns the single Next handler. It keeps no authoritative step index.
- Delete the old Judge night selection/commit/AI path in the same batch — one Next callback chain only.
- Delete the remaining step-builder assembly in `ClocktowerNightHostProjection.kt` (kept by Batch 2a) once the night screen assembles its steps; remove the file if nothing remains.

**Exit checks**
- [x] `ClocktowerJudgeScreen` contains no night commit or AI decision logic; exactly one Next handler exists for night steps, and it reaches the Session only through the `ClocktowerSessionHost` dispatcher.
- [x] No step-builder assembly remains in `ClocktowerNightHostProjection.kt`.
- [x] D2, night death affecting later wakes, and all three modes pass in the real App regression. Reference sizes: router ≈ 3 KB, night screen ≈ 15–20 KB.

### Batch 4 — Observable Session in the UI adapter; delete the tick

The Session already provides `persistWith`, `confirmAtomically` (restores the previous in-memory state if a batch or its save fails) and `updateState` (persists before a non-batch change takes effect). This batch only replaces *how the UI is notified*; transaction semantics are untouched.

- Add a UI-layer `ClocktowerSessionUiState` holding the one Session instance and a Compose-observable snapshot of the last **accepted** `session.state`. It exposes `commit { session -> ... }`: run the Session call, on success publish `session.state`; on `IOException` keep the old snapshot and raise the save error. Never publish success first and roll back later. It has no setters for game data.
- Bind the holder to one Session instance: after restart/restore/new game, callbacks from the old instance (including async AI completions) cannot publish into the new game.
- Unify the two `confirmClocktowerStep` copies without losing return values (nomination returns `Boolean`), error handling or sequence checks.
- Recompute all display projections from the new snapshot (e.g. `ClocktowerSessionPlayerCards` must not stay keyed only on `language`).
- Do **not** add Compose types to `ClocktowerGameSession` (inv. 2); a plain listener is acceptable if a hook is needed.
- Delete `clocktowerUiTick`, `refreshClocktowerUi()` and `onChanged` only after every production caller is migrated.

**Exit checks**
- [x] Zero `refreshClocktowerUi` / `clocktowerUiTick` / `onChanged` references.
- [x] Regressions: save failure does not advance UI; double-tap Next is idempotent; D2 writes once; consecutive commits; restore an old game then continue; old-Session async callback after restart is ignored.

### Batch 5 — App root by ownership

`ClocktowerGameLifecycle` is a **concrete** coordinator for one active game — not a generic Controller/Manager. It orchestrates existing entry points only: create/start TB or NGJ (including TB setup commit), restart, finish, restore, persistence binding, and inter-game rotation recording. It never re-wraps Session APIs, never caches roles/phase/deaths/observations, and never owns a night cursor or a separate recovery engine.

**Ownership table**

| State / responsibility | Owner |
| --- | --- |
| Roles, night order, life, round, confirmed events, outcome | The one Session |
| Screen routing, dialog visibility, deal/reveal indices | App UI / navigation layer |
| **Saved default** operation mode for future games | `StorytellerAiSettings.preferredMode` (unchanged) |
| **Active game's** operation mode (incl. manual takeover) | `ClocktowerGameLifecycle` — the single in-memory owner, persisted through `ClocktowerActiveGameMetadata` as today. No other copy. |
| Player roster, common players, experience | `AppPreferencesState` over the existing `AppPreferencesStore` |
| Create / restore / finish / persist orchestration | `ClocktowerGameLifecycle` |
| AI opening plan, strategy cursor, response ID | `ClocktowerActiveGameMetadata` / AI module (unchanged) |

Why the lifecycle owns the active mode: today the only owner is a `remember` in the App root, saved via metadata; the preference is only the default. Leaving it in the App root would keep a game-lifecycle responsibility there.

Also:
- **The lifecycle does not navigate.** Its operations (e.g. `commitTroubleBrewingSetup`, `startNoGreaterJoy`, `restore`, `finish`) return explicit results — e.g. started / needs Drunk choice / restored to first-night deal / restored to judge / not ready / failed — and the App navigation layer maps each result to a `Screen`. The lifecycle holds no `screen`, dialog or other navigation state.
- `ClocktowerOpeningDraft` calls those few lifecycle operations and returns its own results to the App, instead of receiving getter/setter lambdas. It does not get a holder exposing all fields.
- `AppPreferencesState` is passed to `SettingsContent`, removing the duplicated 10-argument call.
- One `PlayerCard` builder shared by TB and NGJ; restore logic beside `ClocktowerActiveGameMetadata`; `CampBoardGameHostTheme`; one `StorytellerPhase.toClocktowerPhase()`; one `cards.seatOf(name)`.

**Exit checks**
- [ ] `CampBoardGameHostApp` calls no Session confirmation method and no setup committer directly.
- [ ] `ClocktowerOpeningDraft` receives no `{ screen }` / `{ screen = it }` / mode getter lambdas.
- [ ] Exactly one in-memory holder of the active operation mode; the old scattered mode getters/setters and callback chains are deleted.
- [ ] `ClocktowerGameLifecycle` has no `Screen`/navigation/dialog state.
- [ ] `ClocktowerGameLifecycle` exposes no game-fact fields (roles, phase, deaths, observations). Reference size: App root ≈ 20 KB.

### Batch 6 (independent) — Portable Session JSON contract

For a future TypeScript WebHost / EvidenceLab; no cross-language SDK, no second Session implementation, no format change.
- Document the v1 JSON exactly as `ClocktowerSessionJson.kt` writes it: version/fields, seat and role identifiers, ordered `ActionFact`s, recipient-scoped Observations, special states, optional fields.
- Add a few anonymized golden fixtures (TB mid-game, NGJ, Klutz/Slayer, information shown).
- Kotlin decode → encode → decode test for semantic round-trip, event order and recipient privacy; byte equality only if the encoder is already canonical.
- Never include Android UI state, provider credentials or UI drafts.

## 4. Order

| # | Work | Status / exit |
|---|---|---|
| 0 | Owner finalizes boundaries (this document, v3) | **Done** — two review rounds resolved (§7) |
| — | D2 Compose selector refresh | **Done** — #354 merged as `7baf014b`; main CI #4585 `testFull` success |
| 1 | Independent P1: living nominator may nominate a dead nominee | **Merged** #352 → `edcbf5a4`; code head `7aac192b` CI #4586 `testFull` / R2 #4143 success |
| 2 | Batch 1 | **Merged** #355 → `7486d05c` (full CI / R2 verified for PR head) |
| 3 | Batch 2a | **Merged** #356 → `e7ac9481`; full Android CI #4593 / R2 #4148 success |
| 4 | Batch 2 | **Merged** #357 → `1fe855e8`; code head `5871c355`, full Android CI #4597 / R2 #4151 success; approved temporary AI adapter exception |
| 5 | Independent P1: healthy Fortune Teller fixed Boolean | **Merged** #358 → `01ef5e14`; full PR CI #4606 / R2 #4158 and main CI #4607 passed. |
| 6 | Batch 3 | **Implemented for review, not merged.** See [Batch 3 acceptance](HOST_UI_BATCH3_ACCEPTANCE.md); Batch 4 follows acceptance. |
| 7 | Batch 4 | §Batch 4 exit checks |
| 8 | Batch 5 | §Batch 5 exit checks |
| — | Batch 6 | Independent; does not block Android work |

Product-behaviour items (§8) and the Stage 5 device checks are never folded into a refactor batch.

**Device checkpoints:** keep the Stage 5 device baseline. After Batch 3 (D2/night UI), Batch 4 (Session UI) and Batch 5 (lifecycle), spot-check on a real Android phone: 5- and 15-seat table, stacked drunk/poison/dead/ghost-vote markers, three-mode fullscreen handoff, and call / background / process kill → practical continuation. Robolectric/CI is not device acceptance; paid API calls still need separate authorization. Checks not actually run are reported as not run.

## 5. Per-PR checklist

- [ ] Branch from latest `main`; verify live HEAD, open PRs, clean worktree, and this batch's production consumers.
- [ ] State the new owner of each moved responsibility; list deleted responsibilities/parameters/callbacks/files; report net bytes per file and any file > 50,000 B.
- [ ] Confirm **none** added: writable game state, step cursor, generic Rules/candidate domain, shadow recovery, Host veto outside existing Session APIs.
- [ ] Confirm unchanged: Session API/JSON, D2 preview → Next, AI staleness/request identity, real player input in all three modes.
- [ ] A few high-value Session/Compose tests for the risky interaction; no source-shape assertions, no revived old-engine tests.
- [ ] Code commits carry `[full-ci]`; the Android tests job log for the **exact code SHA** shows `./gradlew :app:testFull :app:assembleDebug` succeeded, and R2 succeeded. Docs-only CI is not a build or device result. After merge, the `main` push run is checked too.
- [ ] Report what device, process-kill and paid LLM checks were and were not actually run.
- [ ] Old call chain and exclusive tests deleted in the same batch; no merge or paid request without authorization.

## 6. Risks

| Risk | Mitigation |
|---|---|
| A draft read by a later step is lost when drafts become interaction-scoped | Batch 1 consumer audit; regression test |
| A draft or late callback carries over between nights (interaction IDs have no round) | Key includes phase + round; confirmation re-checks live identity; night-2 → night-3 Imp test |
| `playerInputRevision` timing changes (AI staleness) | Preserve per-action rule; explicit Red Herring test |
| Batch 2a changes a target set while moving it off cards | Inv. 10; item-by-item comparison; existing night regressions pass unchanged |
| Batch 2a deletes step builders still in production use | Builders stay as UI projection until Batch 3 |
| Two places implement night commit branches | Screen = intent/gate; `ClocktowerSessionHost` = only dispatcher; Batch 3 exit check |
| D2 preview/Next regressions | `ClocktowerD2UiTest`, `ClocktowerCompleteAppUiTest`, verified `testFull` per batch |
| A green PR check that ran only `testFast` | Per-PR checklist requires the `testFull` step in the job log |
| UI shows success before a failed save rolls back | Batch 4 publishes only accepted snapshots; save-failure test |
| Old-Session async callback writes into a new game | Holder bound to one Session instance; restart test |
| Lifecycle drifts into a second authority or a navigation owner | Ownership table; lifecycle returns results only; Batch 5 exit checks |

## 7. Owner decisions (resolved)

| Item | Decision |
|---|---|
| Single Session, Host/AI separation, D2 final preview → Next, Batch 4 atomicity | Accepted as fixed contract |
| Batch 1 seat-based current-step draft, Red Herring revision exception | Accepted; key includes phase + round; stale confirmations rejected |
| Batch 2a seat-based Session fact reads | Accepted; deletes only replaced fact reads; step builders kept until Batch 3 |
| Batch 2/3 pure Host step, AI overlay, single Next | Accepted; Next intent (screen) and Session dispatch (`ClocktowerSessionHost`) have separate owners |
| Batch 5 concrete lifecycle and active operation mode | Accepted with conditions: navigation stays in the App; mode has exactly one holder |
| Measurable exit checks plus the 50,000 B hard limit as batch gates | Accepted |
| Pair options limit, Fortune Teller rule, Poison/Imp/Monk targets | Separate product-behaviour items (§8), not in refactor batches |
| Batch 6 portable JSON contract | Independent; does not block Android |

## 8. Product-behaviour items, deliberately out of scope

Refactor batches preserve current behaviour. These are recorded so the refactor does not cement them; each needs its own review and PR if pursued.
- **Poisoner / Imp / Monk targets** — owner decided (2026-10-23) to ignore these corner cases for now: Poisoner and Imp targets are limited to living players while role text says "choose a player"; the Monk target set includes all seats while role text says "not yourself" (the table UI may already exclude it).
- **Pair manual options** — `ClocktowerPairInformationSquareTableUi.kt` limits the role dropdown and show button to `resolvePair(...).options` after the Storyteller picks seats; when `options` is empty the Storyteller may be unable to show the information they actually decided on. This conflicts with the long-term rule that Host records what was really shown rather than refusing a misplay. It is an existing gap, not a new regression; Batch 2/2a must not promote the current option list into a general rule authority. Relaxing manual input needs a separate review of display semantics, private Observation and the confirmation contract.
- **Healthy Fortune Teller fixed Boolean** — independent P1, order step 5.

## 9. Changes by version

**v3 (final, after review §12):**
- Baseline moved to `main` `7baf014b` (#354); #352 status recorded as Draft, not merged.
- Batch 1: draft key `(gameId, phase, round, interactionId)` because interaction IDs carry no round; confirmation re-checks live identity; night-2 → night-3 and late-callback regressions; written draft-consumer audit; existing step-local Pair/Sage UI state not rewritten.
- Batch 1/3: explicit owners — screen owns Next intent/gate/preview, `ClocktowerSessionHost` is the only Session dispatcher, Session is the only writer; no extra veto.
- Batch 2a: moves only fact evaluation; step builders in `ClocktowerNightHostProjection.kt` stay as UI projection and are deleted in Batch 3.
- Batch 5: lifecycle returns operation results and never navigates (removed `navigateTo`); old mode getters/setters deleted.
- CI: "full" means the job log shows `testFull`; `[full-ci]` on code commits; check the `main` push run after merge.
- §7 decisions resolved; §8 adds the Pair manual-options gap.

**v2 (after review §1–11):**
- Invariants expanded to the review's list (one-way dependency, hints ≠ legality, behaviour-preserving).
- Batch 1: `selectedSeats: List<Int>` on the existing interaction ID type instead of `targets: List<String>`; revision semantics stated explicitly.
- New Batch 2a: card-based `ClocktowerNightHostProjection` / `publicAliveCards` moved to seat-based Session reads.
- Batch 2: `hostNightStep` takes no `PlayerCard`, builds on `nextNightInteraction`; four-category table; `nextEnabled` moved to UI.
- Batch 4: explicit atomicity/no-early-publish rules and Session-instance binding.
- Batch 5: ownership table; active operation mode owned by the lifecycle; narrow lifecycle operations for `ClocktowerOpeningDraft`.
- Batch 6: semantic round-trip and privacy requirements.
- Order: dead-nominee P1 first, Fortune Teller P1 after Batch 2, device checkpoints.
- Measurable exit checks for every batch; rule questions recorded as out of scope.
