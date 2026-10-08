# GSP-R1A — Canonical Current-Game History Capture / Prefix Coverage Audit

> Date: 2026-10-08 Australia/Sydney  
> Baseline inspected: `main@944b3f009a60c5c7d5605a8b190768e063455ae3` (local and GitHub aligned; clean local tree at preflight).  
> Scope: **code-path audit / documentation**, not a production provider cutover or tested R1B implementation.  
> Supersedes the *R1A-next* handoff in `GSP_R1_POST_APP_HOST_SPLIT_REAUDIT_2026-10-08.md`; RES-5 and PR #244–247 remain accepted.

## 1. Verdict and immediate gates

**R1A capture/prefix audit complete as a source-code inventory; R1B is the next implementation slice, with explicit coverage and prefix limits.** Current live Host/session writes globally ordered action facts and some typed observations. It **does not** produce a complete canonical longitudinal story, and the neutral `StorytellerProviderGameContextV1` exports neither timeline today. A generic provider cannot yet reason correctly from the complete gameplay history.

Three risks block any claim of full or historical replay-safe coverage:

1. **Uncaptured semantics**: registration judgments, some player reveal/evil information, Day public narrative/claims and some ability/player choice paths remain display events or transient UI/Recovery mechanics rather than canonical typed observations.
2. **No causal bridge between revision and global timeline**: `StorytellerDecisionEvent` carries game/input revisions but **not** a `TimelinePoint` cutoff. A revision-only `<=` filter does not prove absence of later action/observation in the same revision.
3. **Decision history does not survive as canonical Recovery-owned production state**: `ClocktowerRecommendationCoordinator` has its own in-memory `InMemoryDecisionEventStore`, but no production calls to `appendDecision` or `correctDecision` were found; `ClocktowerSessionState.decisionHistory` has a default empty archive, and current `ClocktowerRecoveryHistory` does not persist it. Do not equate the existence of archive types with production completeness.

**No new provider authority, network API, old heuristic/named-policy restoration, UI-text parsing, SDE-prefix dependency or broad App/Host refactor.** Manual/offline Game Engine remains fully functional without a provider.

## 2. Canonical owner/consumer map

| Surface | Actual producer / mutation | Readers / consumers | Recovery and test evidence | Verdict |
| --- | --- | --- | --- | --- |
| `ActionFactTimeline` | App `recordClocktowerAction` -> `ClocktowerGameSession.commitGlobalActionFact`; commit allocates game-wide point, dedupes actionId | Session view/snapshot, dusk/dawn planners, SDE/replay diagnostics | `ClocktowerRecoveryHistory.actionTimeline`, strict decoder and restore; `ClocktowerActionTimelinePersistenceTest`, `ClocktowerHistoricalActionObservationCaptureTest`, `NightDawnRestoreRetryConvergenceAcceptanceTest` | **Structurally durable; event coverage partial** |
| `EpistemicObservationLog` | App `recordEpistemicObservation` / `commitConfirmedInformationDecision` -> Session Global commit; Host `recordReliablePrivateInformation` emits some drafts | Session view/snapshot, epistemic replay/diagnostics, A4 invalidation, SDE | Recovery persists ordered observations; strict decoder, planner semantic validation; `ClocktowerGlobalObservationCommitTest`, `ClocktowerSemanticHistoryPersistenceTest`, handoff/integration tests | **Structurally durable; revelation coverage partial** |
| `DecisionHistoryArchive` | Session has default-valued field; separate `ClocktowerRecommendationCoordinator` event store and `DecisionHistoryRepository.project()` | `StorytellerProviderGameContextBuilderV1` (only currently verified usage in tests); historical review | No `decisionHistory` in current `ClocktowerRecoveryHistory`, no archive injection at App fresh/restore constructor; `DecisionEventStoreTest`, `DecisionHistoryRepositoryTest`, `StorytellerProviderRequestFactoryV1Test` prove types, **not production persistence** | **Cannot claim durable production decision capture** |
| `StorytellerProviderGameContextV1` | Host/session builder maps seat overrides and effective decisions with revision comparison | Provider request factory accepts optional context; defaults to `EMPTY` | Existing unit tests only; search of `app/src/main` finds no production call to builder or request factory | **No live Provider context materialization** |
| `ClocktowerInformationHistoryPayload` / `ClocktowerEvent` | Host reveal callback -> localized event text -> App event list; other role/day UI handlers do likewise | UI review, human-readable records, some display change summaries | Recovery `events` | **Presentation archive only; NOT semantic input** |
| Storyteller player context | `ClocktowerGameSession.updateStorytellerPlayerContext` changes only player-input revision | Provider context builder | Recovery `storytellerPlayerContextBySeat`; dedicated codec/session tests | **Owned/persisted; R2 editor/profile not yet in main** |

Code anchors: `CampBoardGameHostApp.kt` 680–896, 1043–1052, 1165–1185, 1356–1361, 1688–1712, 2082–2235, 2570–2611, 3030–3100; `ClocktowerHostScreen.kt` 299–345, 505–545, 897–964, 2693–2730; `ClocktowerGameSession.kt` 65–99, 265–333, 354–461; `StorytellerProviderGameContextBuilderV1.kt` 18–89; `RecoveryRestorePlanner.kt` 366–406.

## 3. Phase-by-phase real capture coverage

`ActionFactDraft` currently has exactly seven kinds: Poison, Protect, Attack, Execution, Death, RoleChange, PhaseAdvance. These are **the supported schema**, not a completeness claim.

| Gameplay period/event | Canonical write found | Absent / noncanonical / qualification |
| --- | --- | --- |
| Setup / FirstNight initial role identities and Drunk | Canonical committed state includes actual/shown seat identity; fresh session starts `GLOBAL_V1` | No distinct `ActionFactDraft.DrunkAssigned` or chronologically logged Drunk-selection choice/reveal; do not invent from display payload. Setup snapshot may describe current committed assignment, not decision history |
| FirstNight Poisoner/Monk | App confirmations commit Poison / Protect facts; poison expiry and carry handled separately | Unconfirmed draft choices are transient and must **not** be represented as committed. Effect/reliability must be interpreted with state and timing |
| FirstNight character information | Host reveal handoff uses authorized `ConfirmedInformationDecision` when available; fallback `displayProposition` or role-specific Chef/Empath/Fortune Teller/Investigator/Washerwoman/Librarian proposition | Not all information screens are proven to emit typed propositions. Demon bluffs/minion/demon info, Grimoire exposure and some role/step-specific text actions cannot be presumed present in `EpistemicObservationLog` |
| FirstNight Spy/Recluse registration | Domain validates registration options; Host logs localized `RoleAction` event (and structured candidate evaluations can carry registration witness) | Actual adjudication in Host `recordSpyRegistration` / `recordRecluseRegistration` is not committed as a dedicated canonical registration record. A candidate registration witness is not proof of a committed historical event |
| OtherNight Poison/Protect/Attack | App confirmations write typed actions, later materialization records death, poison carry/expiry and transitions | A planned/failed attack and actual death are different events. Protection or Mayor redirection explanatory text is not a generic typed semantic observation |
| OtherNight player information | Same Host `recordReliablePrivateInformation` handoff and information confirmations can write typed observations | Ravenkeeper/Undertaker/Chambermaid etc. must be verified against `displayProposition` per materializer and every fresh/restored path; do **not** assert 100% coverage based on one shared handoff |
| Day executions/deaths | `addClocktowerEvent` projects Execution/Death to typed actions and `AliveAt(false)` public observations; Slayer/dawn special paths preflight and separately commit with duplicate projection suppressed | Public AliveAt means publicly observed death, not complete public deliberation; Slayer attempt/use, nominations/votes, Artist/Klutz/Virgin public discussions may be UI events/flags and are not guaranteed typed semantic records |
| Role change/demon succession | `recordClocktowerRoleChangeAction` writes RoleChange facts when called | Displayed replacement identity and actual-role changes must remain distinct; do not infer who was told what from role-change fact alone |
| Phase boundaries | App commits PhaseAdvance (day, night, dawn transitions) | `ClocktowerEvent` local sequence is **not** global timeline authority |

**Poisoned/Drunk observations:** `RecordedEpistemicObservation.reliability` in the Host fallback is `RECEIVED_AS_FUNCTIONING`, describing the player's experience, not proving factual truth. Provider must retain observed proposition and separate actual ability/poison/Drunk chronology; never relabel received information as known true. The semantics of unreliable display without a proposition are deliberately skip/unknown, not an implicit false result.

## 4. Global versus legacy and historical prefix correctness

- Fresh App production sessions create `ClocktowerGameSession.createProduction(... GLOBAL_V1)`; recovered current-format sessions are restored as `GLOBAL_V1`. Session *general-purpose default* is `LEGACY_LOCAL` for compatibility; Global writes use `TimelinePoint.globalSequence` and allocate monotonically over the combined action/observation stream.
- `ClocktowerSemanticHistoryMode.requireCompatible` checks binding mode, collisions and cursor; `ActionFactTimeline` and `EpistemicObservationLog` defensively copy outer lists and validate canonical ordering. Empty history is a valid prefix, **not evidence that capture is complete**.
- `LEGACY_LOCAL` observations have round/local sequence only and cannot be interleaved with Global action facts. Never synthesize a `globalSequence`; such inputs get `LEGACY_LOCAL / UNRECONSTRUCTABLE_GLOBAL_ORDER` coverage and a conservative nonhistorical/declined materialization.
- The **only safe live cutoff** is an exact immutable Session history snapshot and an exclusive `nextTimelineGlobalSequence` cursor captured **before** the pending recommendation is produced/committed. Select `globalSequence < exclusiveCutoff`; validate gameId and the exact game/input revision + history mode captured together. `<= gameStateRevision` by itself is not an ordering predicate.
- **Historical/frozen cutoff** must be an explicitly recorded `gameId + decisionId + source revision + exclusive global cursor + snapshot identity` (and eventually frozen player context). Do not reconstruct a past cursor from revision or phase/local sequence. If no accepted cutoff record exists, return `HISTORICAL_CUTOFF_UNAVAILABLE`, never the full current timeline.
- `DecisionHistoryRepository.project()` applies *all archive corrections* before filtering the effective events by request revision. A later correction can therefore suppress an earlier decision in a historical rebuild. `DecisionCorrectionEvent` has no timestamp/revision/global point. Historical corrected-decision view is **unreconstructable** without new durable causal identity; guard this, do not assume the current applied projection is an accurate old prefix.
- Effective `StorytellerDecisionEvent` has separate `gameStateRevision` and `playerInputRevision`, but no shared sequence. Treat the decision archive as a **separate** typed stream with `UNKNOWN_CAUSAL_ORDER` where exact relation cannot be proven, never as action or observation facts or as a globally interleaved chronological list.
- `StorytellerProviderGameContextBuilderV1` currently accepts latest player overrides and uses both `<=` revision comparisons on decisions. This is suitable as a bounded *current* projection at best. Historical player overrides are mutable current-session state; they also need frozen at-decision inputs for genuine replay.

## 5. Recovery: what is proven and what is not

Production save path constructs `ClocktowerRecoveryHistory` from Session revisions, per-seat overrides, canonical action timeline, next global cursor, UI events, and epistemic observation records. Strict decoder rebuilds the action/observation histories and **derives** its cursor as `max(committed globalSequence)+1`; restore planner validates `GLOBAL_V1` and reference seats/rounds; App restores Session with the decoded data.

Therefore fresh/restored **committed** action and observation payloads have a viable equality basis. Still unproven for Provider until a dedicated test round-trips a *typed provider projection* at an identical logical cutoff. Additionally:

- The strict recovery schema has no `DecisionHistoryArchive`, no at-decision frozen history cutoffs and no per-event semantic coverage manifest.
- The decoder derives cursor from committed entries rather than persisting the raw transient Session cursor. This is currently compatible with producer usage (no production references found to `allocateTimelinePoint`), but a future consumer allocating uncommitted points would require an explicit new contract and equality test.
- `ClocktowerInformationHistoryPayload` events survive Recovery as text, but cannot fill holes in epistemic semantics.
- Recovery is short-horizon current-game continuation, not a generic archival/whole-game replay product. Do not smuggle a full archive migration into R1B.
- Missing records, incompatible mode, ambiguous cutoff, broken causal identity and unsupported proposition **must be surfaced as explicit unknown/incomplete**, not converted to empty/false/complete.

## 6. Selected neutral R1B contract (proposed implementation boundary)

1. Introduce **read-only immutable** neutral `StorytellerProviderHistoryPrefixV1` (or equivalent) under domain/provider-contract ownership, not in `recommendation` and not importing SDE/history-specific prefix types or UI `ClocktowerEvent`. Include `gameId`, `historyMode`, `exclusiveGlobalSequence`, `cutoffSource` (`LIVE_CAPTURED` / `FROZEN_EXPLICIT` / `UNAVAILABLE`), chronological typed action and observation entries, and a **per-category coverage report** with reason codes.
2. Represent each fact by its existing stable identity, `TimelinePoint` and neutral typed payload; each observation by recordId, point, visibility/recipients, reliability, proposition. Action/observation must have a common stable chronology (separate typed lists allowed if a deterministic combined chronological view is available). Do not flatten into localized strings. Enforce defensive collection ownership for nested mutable collections, not only outer lists.
3. Read a **single exact session/snapshot owner**; reject mismatched game/revisions, cursor > current state, mixed/legacy global claims, future entries or invalid duplicates. For the live entry, use the exclusive cursor from the Session at the instant the pending decision snapshot is captured; do not re-read a later mutable cursor after asynchronous work.
4. Keep `DecisionHistoryArchive` distinct. Until an at-decision event/correction bridge and Recovery persistence are established, emit `DECISIONS_INCOMPLETE` or explicitly unsequenced current-effective decisions. Do not represent missing decision history as an authoritative empty series, or claim corrected history is historically accurate.
5. Coverage is **per dimension**, at minimum mechanical events, player-received/private info, public info/claims, registration decisions, and prior decisions: `CAPTURED_PREFIX` (only the supported typed producer subset), `PARTIAL`, `UNKNOWN`, `UNRECONSTRUCTABLE`. Never a whole-game `COMPLETE` marker until all corresponding producer paths are proven.
6. R1B may implement **GLOBAL_V1 live-prefix materialization first** and an explicit frozen prefix only where a real captured cutoff exists. `LEGACY_LOCAL` and uncut historical requests must remain honest `UNAVAILABLE` / fail closed. Add no generic historical replay from current state. No API, parser, scoring, recommendation behavior or App/Host extraction.
7. Follow-on capture expansion needs separate producer-owned typed commits for missing reveal/registration/public facts, with Recovery compatibility and correct publication/commit timing. Do not add them opportunistically in the initial read-only materializer.

R1B test-first acceptance: typed GLOBAL_V1 intermixed action/observation ordering; strict `< cutoff`; no future leakage on same revision; same-request equality; mutate original inputs/nested collections after build; mismatch/invalid cursor/legacy unknown; unreadable historical cutoff; poison -> observation -> death/phase; no UI-string/SDE/recommendation dependency. Then R1C: fresh/Recovery equal provider prefix, hard decision/correction bridge scenarios, Drunk/Poison + Spy/Recluse mixed first/other night and Day cases, Android T0/T1 + affected T2 and T4 CI/R2. **Production API integration remains blocked until GSP-3A quality benchmark.**

## 7. Evidence status and branch governance

This R1A change is docs-only. Source-code paths and existing tests above were inspected; no new Gradle/Android suite was executed for the audit. Prior CI #3837/#3839/#3841/#3843 and R2 #3512/#3513/#3514/#3515 are **historical accepted checkpoints**, not new test results for this document. GitHub PR #230 (superseded Recovery player-context slice) and PR #232 (superseded GSP-2B3B player-context editing) both remain *open Drafts*; neither should be merged or used as an R1B prerequisite without a separate re-audit.

Required next sequence: **R1A documentation acceptance -> R1B bounded neutral materializer + focused tests -> R1C replay/Recovery/prefix-safety acceptance -> R2 player experience/claims/pressure -> R3 cross-game/diversity -> R4 structured prompt/response -> GSP-3A blinded provider evaluation**.
