# Host UI Responsibility Refactor Plan (2026-10-23)

**Status:** PLAN — approved for review, not yet implemented. Baseline `main` `5dd880f9` (code identical to `b551d7f2`, Stage 5 #350).
**Scope:** `CampBoardGameHostApp.kt` (49,208 B), `clocktower/ui/ClocktowerHostScreen.kt` (51,102 B, over the 50,000 B hard limit), and their direct adapter `ClocktowerSessionHost.kt` (30,907 B).
**Not in scope:** Session rules, AI prompts/parser/memory, setup diversity, persistence format, visual design. Independent P1 defects (living nominator → dead nominee; healthy Fortune Teller fixed Boolean) remain separate tasks, but Batches 1–2 are sequenced first so those fixes land in the new structure.

## 1. Why the previous split did not help

The previous decomposition moved code into new files but left state and responsibility in place:

| Symptom | Evidence |
|---|---|
| App root is still one ~840-line composable with ~40 `remember` values mixing navigation, preferences, game lifecycle, restore/persist and dialogs | `CampBoardGameHostApp.kt:104-944` |
| Extracted helpers call back into the App through getter/setter lambdas | `openingDraft.beginPrepared(..., { storytellerOperationMode }, { screen }, ::commitAndStartTroubleBrewingGame, { screen = it })` (`:391-426`) |
| Manual invalidation because the Session is not observable | `clocktowerUiTick` + ~20 `refreshClocktowerUi()` calls across App and `ClocktowerSessionHost` |
| Duplication | `SettingsContent` called twice with identical 10 args (`:803`, `:909`); TB and NGJ `PlayerCard` construction (`:357`, `:466`); `persistWith { save }` twice; `confirmClocktowerStep` in two files; `StorytellerPhase→ClocktowerPhase` mapping in two files; name→seat lookup ~10 times |
| `ClocktowerJudgeScreen` has ~60 parameters | `ClocktowerHostScreen.kt:40-101` |
| Role-per-parameter fan-out: 11 per-role draft variables in `ClocktowerSessionHost`, each with `onSelectX`/`onConfirmX`, reassembled by `when(action)` in the screen | `selectedNightName` (`:625`), `onSelectName` (`:838`), `advanceNightStep` (`:702-736`). Stages 3–5 grew the file 46.8 KB → 51.1 KB this way. |
| ~470 lines of non-UI decisions inside a composable | role-name string → numeric metric map (`:456`), AI pending/key/stale (`:482-575`), opening-plan revalidation (`:581-611`), duplicate unreliable-information logic (`:258-317`), succession/readiness (`:650-669`) |

## 2. Binding invariants (every batch)

1. **One writer.** `ClocktowerGameSession` / `ClocktowerSessionState` remains the only writable game state and the only producer of confirmed events. No batch adds or changes a Session confirmation API or `ClocktowerSessionJson` format.
2. **Session stays platform-free.** `clocktower/session` and `clocktower/domain` gain no Compose/Android imports. Observability is added in the UI adapter, not inside Session.
3. **Drafts are UI-only.** Night drafts and previews are never persisted or serialized; they are *narrower* in lifetime than today (keyed to the current interaction, not the whole game). D2 holds: change → re-preview; only current-step Next records exactly one final Observation; no cross-role Previous.
4. **Host rules separate from AI.** Host-determined values (resolution, legal targets, readiness, succession, Next enablement) are computed without AI inputs. The AI overlay may only select among Host-legal options; deleting it must leave MANUAL fully working.
5. **AI memory stays metadata.** Opening plan, strategy cursor and response ID remain in `ClocktowerActiveGameMetadata`, never merged into Session state.
6. Preserve TB 5–15, NGJ 5–6, all three modes, both inter-game diversity behaviors, privacy, AtomicFile persistence, stale/idempotency checks.
7. No generic Context/Manager facade, no one-function-per-file extraction, no permanent dual path: each batch deletes the replaced code in the same change.

## 3. Batches

Each batch is a separate PR from latest `main`, Draft until reviewed, validated with `./gradlew :app:testFast` locally and full Android CI + R2 on the exact code SHA. `ClocktowerCompleteAppUiTest` (drives the real App root) is the regression net throughout.

### Batch 1 — One current-step night draft (highest payoff)

**Change**
- Replace the 11 per-role draft variables in `ClocktowerSessionHost` (demon attack, poison, monk, mayor, butler, ravenkeeper, FT×2, Chambermaid×2, red herring) with one UI value:
  `NightStepDraft(interactionId, targets: List<String>)`, `remember`ed by `(gameId, interactionId)`.
- Screen API: one `draft`, one `onDraftChange(NightStepDraft)`, one `onConfirmNightStep(step, draft, preview)`.
- `ClocktowerSessionHost` keeps the single `when (action) → session.confirmX(...)` dispatch — the only place that knows Session APIs.
- Delete: per-role `onSelectX`/`onConfirmX` parameters, `selectedNightName` and `onSelectName` reassembly, the manual draft reset block in `onCompleteNight`/`onAdvanceFromFirstNight`.
- Down-stream `ClocktowerNightStepCardLocalized`: collapse `selectedName`/`fortuneTeller*`/`chambermaid*`/`redHerring` and their five select callbacks into `draft` + `onDraftChange`.

**Behaviors to preserve exactly**
- Every target change calls `session.recordPlayerInput()` **except** Red Herring (draft-only, validated against current seats).
- FT, Chambermaid and Ravenkeeper target changes clear `lastInformationPreview`.
- Chambermaid selection is revalidated against `chambermaidTargetCards`.
- FT targets still feed `requestLiveGlobalAdvice(fortuneTellerSelectedNames = ...)` and `fortuneTellerSubjectSeats`.
- Demon-bluff draft and Imp self-kill successor are not night-step targets; keep them as they are.

**Pre-implementation audit (required)**: confirm every current draft is only read during its own interaction (`butlerMaster` → common materializers, `ravenkeeperTarget` → other-night materializers, `fortuneTeller*` → FT materializer/AI). Any reader from a later step must read the confirmed Session fact instead.

**Tests**: update the one direct harness `ClocktowerD2UiTest` (calls `ClocktowerJudgeScreen`); add a regression that a draft does not leak into the next interaction and that Red Herring change does not advance `playerInputRevision`.

**Expected**: ~35 parameters/lambdas removed across `ClocktowerHostScreen.kt`, `ClocktowerSessionHost.kt`, `ClocktowerNightStepUi.kt`; `ClocktowerHostScreen.kt` back under 50,000 B.

### Batch 2 — Host step model and AI overlay as plain Kotlin

**Change**: move the decision code out of the composable into two pure functions:

1. `hostNightStep(sessionState, ruleset, cards, draft): HostNightStep` — Host rules only: current step & index, numeric/pair resolution, legal target set, `playerTargetReady`, succession (`impSelfKillSuccession`), private-information/evil-info classification, `nextEnabled` (excluding AI). Rule-only helpers live next to `clocktower/session/HostInformationResolvers.kt`; UI-presentation helpers stay in the UI package.
2. `nightAiOverlay(hostStep, aiInputs): NightAiOverlay` — request identity/key, blocked/stale state, opening-plan match, validated suggested option, Mayor recommendation. It can only return an option present in `hostStep`'s legal domain.

Also:
- Derive the numeric metric from `step.informationQuestion` / step metadata, deleting the `"Chef"/"Empath"/"Clockmaker"/"Chambermaid"` string map.
- Merge `unreliableInformationCanBeRecorded` and `recordReliablePrivateInformation`'s duplicated proposition computation into one `shownPropositionFor(preview): InformationProposition?`; the recording path builds the `EpistemicObservationDraft` from it.
- Use the App's `BuiltInClocktowerRulesetCatalog` instance instead of a second `fromContext` lookup (cached, so this is clarity, not performance).

**Tests**: JVM unit tests for `hostNightStep` (unique numeric, discretionary registration, drunk/poisoned source, succession choice, Next gating) and `nightAiOverlay` (stale revision, wrong seat/metric rejected, MANUAL returns nothing). These cover the P1 Fortune Teller work's future home.

### Batch 3 — Split the judge screen by what it shows

- `ClocktowerJudgeScreen` becomes a short router: player fullscreen preview → New Demon → Demon bluffs → Day (`ClocktowerDayHostScreen`, already separate) → Night.
- New `ClocktowerNightHostScreen` renders `HostNightStep` + `NightAiOverlay`, owns `lastInformationPreview`/`playerDisplayStep` and the Next handler.
- Target sizes: router ≈ 3 KB, night screen ≈ 15–20 KB, model ≈ 10–12 KB.

### Batch 4 — Observable Session in the UI adapter; delete the tick

- Add a small UI-layer holder (e.g. `ClocktowerSessionUiState`) that owns `mutableStateOf<ClocktowerSessionState>` and exposes `commit { session -> ... }`: run the Session call, catch `IOException` → save error, publish `session.state`. This also unifies the two `confirmClocktowerStep` copies.
- Delete `clocktowerUiTick`, `refreshClocktowerUi()` and the `onChanged` parameter.
- **Do not** add Compose types to `ClocktowerGameSession` (invariant 2). If a hook is needed, a plain listener is acceptable.

### Batch 5 — App root by ownership

- **Preferences holder** (`AppPreferencesState`): language mode, common players, player experience, with their store writes. `SettingsContent` takes it, removing the duplicated 10-argument call.
- **Game lifecycle owner** (`ClocktowerGameLifecycle`, plain class remembered at the root): start TB / NGJ, commit TB setup, restart, finish, restore, persist, `screen`, `storytellerOperationMode`, ruleset ref, rotation record. It replaces existing local closures 1:1, never wraps Session APIs and holds no game facts; `ClocktowerOpeningDraft` calls it directly instead of receiving getter/setter lambdas. **Owner decision requested:** confirm this is acceptable under the AGENTS.md "no generalized Context/Manager facade" rule (it is a single concrete owner, not a generic facade).
- Small cleanups: one `PlayerCard` builder shared by TB and NGJ; restore logic moved beside `ClocktowerActiveGameMetadata`; `CampBoardGameHostTheme` composable; one `StorytellerPhase.toClocktowerPhase()` extension; one `cards.seatOf(name)` helper.
- Expected: `CampBoardGameHostApp.kt` ≈ 20 KB, mostly `when (screen)` routing.

### Batch 6 (optional, before any WebHost work) — Portable data contract

The shareable artifact for a future TypeScript WebHost is the Session data (players by seat, ordered action timeline, recipient-scoped observations, per-seat context), not the Kotlin rules. Today the format is defined only by `ClocktowerSessionJson.kt`.
- Write a short schema document of the version-1 JSON.
- Commit a few golden fixtures (TB mid-game, NGJ, Klutz/Slayer facts) with a Kotlin decode→encode round-trip test, so another implementation can test against the same files.
- No format change.

## 4. Sequencing and reporting

Order: **1 → 2 → 3 → 4 → 5**, Batch 6 independent. Batches 1+2 may share a PR if review prefers. Each batch reports: deleted code and exclusive tests, net byte change per file, remaining files > 50,000 B, Session/event risk, exact CI/R2 run IDs and verdict. Device acceptance (Stage 5 checks) and paid API remain separately reported and are not claimed by this work.

## 5. Risks

| Risk | Mitigation |
|---|---|
| A draft silently read by a later step is lost when drafts become interaction-scoped | Batch 1 pre-implementation consumer audit; regression test |
| `playerInputRevision` semantics change (AI staleness) | Preserve per-action revision rule; explicit test for Red Herring exception |
| D2 preview/Next regressions | `ClocktowerD2UiTest`, `ClocktowerCompleteAppUiTest`, full Android CI on each batch |
| Lifecycle owner drifting into a second authority | Invariant 1/5; review checklist: owner holds references and navigation only |
| Restore path changes | Keep `ClocktowerActiveGameMetadata` format; existing App-recreation tests |
