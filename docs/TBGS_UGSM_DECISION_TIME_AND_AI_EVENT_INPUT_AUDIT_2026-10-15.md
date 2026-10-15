# TBGS / UGSM decision-time state and AI event-input completeness audit — 2026-10-15

> Scope: **read-only, code-verified cross-project audit; no production rewiring, no paid API calls, no schema migration**.
>
> Host: `Jazz0006/CampBoardGameHost` current P2 stack through [Draft #313](https://github.com/Jazz0006/CampBoardGameHost/pull/313). Independent remote 1E prototype branch `feature/ai-event-jit-1e-real-history-audit-20261015` exists, but has not been reviewed/merged or accepted as a production contract.
>
> EvidenceLab: `Jazz0006/ClocktowerEvidenceLab` `main`. Host local `clocktower` remained clean at `486f2734abd378a7c4e13a550a2e62509e5134c2` and must not be confused with later remote stacked Draft heads.
>
> **Verdict**: TBGS-0 and TBGS-1 cross-project snapshot semantics **exist and were accepted**; runtime state can be projected now. A **complete arbitrary historical decision-time state** cannot yet be reconstructed from the currently proved production record. Do NOT build a second mutable GameState, general all-events log or premature generic rules engine. Reuse already durable semantic history plus a minimal committed-Storyteller-choice seam.

## 1. Already implemented: authoritative owners and transport

| Current owner | Actual capability | Limitation |
| --- | --- | --- |
| `ClocktowerGameSession` / `ClocktowerSessionState` | Canonical live `GameState`, versions, role/show/alive/poison, ordered `ActionFactTimeline` and `EpistemicObservationLog`; exposes `GameSnapshot` | Live current state is not automatically an arbitrary old state; `decisionHistory` does not prove complete UI choice provenance |
| `TroubleBrewingGameSnapshotV1` / `TroubleBrewingGameSnapshotProjector` / JSON codec | TB-only immutable `SETUP_PRECOMMIT`, `SETUP_COMMITTED`, `RUNTIME` projections; four-value `KNOWN/UNCOMMITTED/UNKNOWN/NOT_APPLICABLE`; deterministic role IDs and JSON | V1 `setupState` has only `hasDrunk` and `drunkAssignmentSeat`; `grimoireSeats` includes shown/actual/alive/poisoned; NO red herring, bluffs, votes, information, protections or history field |
| `StorytellerProviderHistoryPrefixMaterializerV1.captureLive` | Captures merged action+observation entries with `TimelinePoint.globalSequence` and a single live exclusive cutoff under `GLOBAL_V1` | Only records covered by those two streams; cannot claim all decisions or reconstruct a past `GameState` from a later state |
| `ClocktowerSemanticHistoryPersistence.encodeActionTimeline`, `EpistemicSemanticJson.encode(RecordedEpistemicObservation)` | Already versioned/typed JSON representations for mechanical actions and received information | Model-input V2 currently uses `fact.toString()` and `proposition.toString()`; do **not** mistake debug Kotlin text for the canonical JSON fields |
| `SdeHistoricalReplayInput` + `SdeHistoricalReplayInputJsonCodec` | **Existing combined read-only replay/export**: committed setup, ruleset, seat names, action timeline, observation log, versions and cursor; strict JSON encode/decode; used conditionally in recovery | Read-only input/transport, NOT complete generic arbitrary-time replay or full Storyteller-choice history |
| `DynamicActionReducer.reduce` and `HistoricalInformationAbilityStateResolver` | Pure, partial fold of poison, protection, attack, death/execution, role changes, phase transition; ability-state reconstruction before a particular recorded observation using action prefix | Does NOT replay full committed ST choices, received clues, decisions, special registrations, red herring/bluffs, all ability flags or social record; generated revision is derived, not proof of the exact original Host revision |
| Recovery `ClocktowerRecoveryHistory/Mechanics` | Current actions, observations, event-display records, optional causal journal, actual red herring and bluff fields can survive eligible short-lived recovery | A *recovery snapshot* is the current point, not a durable series of historical snapshots; SDE replay capture is conditional and optional |
| End-of-game `GameArchiveRecord/Codec` | Cards, elimination records, localized `ClocktowerEvent` display history and outcome | **Does not retain structured action timeline, observation log, recovered mechanics or full chronological decisions**; cannot reconstruct a rigorous historic AI prefix from archive alone |

Host code:
- `clocktower/domain/TroubleBrewingGameSnapshot.kt`
- `clocktower/setup/TroubleBrewingGameSnapshotProjector.kt`
- `clocktower/session/ClocktowerGameSession.kt`
- `clocktower/session/StorytellerProviderHistoryPrefixMaterializerV1.kt`
- `clocktower/domain/ActionFact.kt` (`DynamicActionReducer`)
- `clocktower/recommendation/sde/SdeHistoricalReplayInput.kt` and `SdeHistoricalReplayInputJsonCodec.kt`
- `persistence/ClocktowerSemanticHistoryPersistence.kt`, `clocktower/epistemic/EpistemicSemanticJson.kt`
- `persistence/RecoverySnapshot.kt` and `persistence/GameArchiveJsonCodec.kt`.

## 2. EvidenceLab actual cross-project parity and current replay limit

EvidenceLab owns `SetupCommitment`, `SemanticEvent`, provenance and `DecisionSlice/HistoricalPrefixBoundary`. `materialize_historical_prefix()` correctly cuts setup or event history **before** the decision and rejects unsupported setup chronology. It is a **prefix selector**, not a general BotC mechanical state reducer.

EvidenceLab's only accepted TBGS V1 state materializer is currently `materialize_tb_drunk_precommit_snapshot()`. It projects `SETUP_PRECOMMIT` just before a Drunk choice and explicitly rejects future-result backfill. G10 pre-Drunk Host/EvidenceLab exact JSON equality was accepted at 2115 bytes. The same general semantics are reusable; **runtime/multi-night cross-project state equivalence has not yet been demonstrated**. EvidenceLab also has eight READY/machine-materialized historical decision seeds across four game groups; these do not establish a complete arbitrary-time materializer.

EvidenceLab sources:
- `src/clocktower_evidence_lab/domain/history.py`
- `src/clocktower_evidence_lab/domain/decision.py`
- `src/clocktower_evidence_lab/domain/tb_snapshot_materializer.py`
- `src/clocktower_evidence_lab/domain/tb_snapshot.py`
- `docs/EL_TBGS_0_1_MAPPING_AND_IMPLEMENTATION_AUDIT_2026-09-30.md`
- `docs/EL_ML1B_READY_HISTORICAL_BENCHMARK_V1.json`.

Keep EvidenceLab's source-backed uncertainty/provenance separate from live Host confirmed truth. Do not ask EvidenceLab to implement Host rules or fabricate `UNKNOWN` data.

## 3. Field- and producer-coverage audit

| Fact needed by an expert continuing Host | Current physical producer/storage | Verdict / exact remaining gap |
| --- | --- | --- |
| Game identity, exact actual/shown seat roles, living state, impairment, phase | `ClocktowerSessionState`, TB V1 runtime projector | **Available now** at live capture; historical replay requires starting state + order/coverage |
| Drunk chosen during setup | `commitAndStartTroubleBrewingGame` creates committed actual Drunk, TB V1 marks seat | **Current value available**; exact real choice/commit time/source not represented as global sequenced event |
| Red herring, demon bluffs | `clocktowerRedHerring`, `clocktowerRecommendedDemonBluffRoleNames`, recovery mechanics; accepted V2 opening sets these in UI state | **Current/recovery values available**; not in TB V1, not a proved globally ordered ST commitment; overrides update input revision but not corresponding ordered fact |
| Number/boolean/pair/role actually shown | `ClocktowerHostScreen` publication path, `RecordedEpistemicObservation`, plus localized `ClocktowerEvent` | **Typed observations exist** and are timestamped globally in `GLOBAL_V1`; cannot assert 100% coverage, since unreliable information with absent `displayProposition` explicitly skips observation recording. Audit every publish path and fail/reconcile gaps, never infer from text silently |
| Poison, protection, attack, death, execution, no execution, phase, roles changed | Production `ActionFactDraft` commits into global timeline | **Typed producers exist**; exactly orderable with observations, no need for an additional action log; verify producer/report completeness at two fixtures |
| Full nominations, voters, ghost voters | `onConfirmedNomination` and `onConfirmedVote` commit `ActionFact.Nomination` / `Vote` | **Already fully modeled**, including `nominatorSeat`, `nomineeSeat`, `voterSeats`, `ghostVoterSeats`. Keep **ALL** confirmed nominations/votes for the AI suffix; do not treat votes as irrelevant |
| Special registration witness and special ability outcomes | Selective `StorytellerCausalDecisionJournalV1` / registration producer, `ActionFact` | **Partially typed**; don't upgrade a legal alternative witness to an actual ruling or claim a complete global ST decision stream |
| AI recommendation versus manual adoption/override, exact resulting source | UI adoption, playerInputRevision, in-memory pending/adopted suggestions; some typed decision infrastructure | **Incomplete durable, ordered semantic commitment**, and no proof every override is in global history; discarded AI proposal is NOT a gameplay fact |
| Player claims and social intent | Some context input and future P3 roadmap | P3; do not block P2; vote actions are facts, their motivations are hypotheses |
| Cross-game history | `CrossGameHistory` and setup-rotation history | Separate optional strategic enrichment, NOT part of TB V1 mechanics |

Concrete current-source places:
- `CampBoardGameHostApp.kt` `adoptJointOpeningAfterReveal`: commits Drunk, then assigns private bluffs/red herring after capturing strategic checkpoint.
- `CampBoardGameHostApp.kt` `onSelectRedHerring`, `onCommitDemonBluffs`: mutate UI/mechanics and versions.
- `CampBoardGameHostApp.kt` `onConfirmedNomination`/`onConfirmedVote`: global typed history including voter-seat list.
- `ClocktowerHostScreen.kt` `recordReliablePrivateInformation`: unreliable/no-proposition skip; reveal transaction records localized history separately.
- `StorytellerStrategicContinuationV2.encode`: currently complete real seat state + whole action/observation history, but legalCandidates and whole remainingChoices, typed facts rendered as Kotlin `toString()`.

## 4. Decision-time reconstruction: currently possible versus not yet proved

**Possible today**, using stable actual owners without introducing a second GameState:
1. Capture a **current** Host `GameSnapshot` and project a TB V1 snapshot.
2. Capture a full ordered `GLOBAL_V1` action/observation prefix at that **same** Host revision/cursor.
3. Build a latest-facts AI request, including typed nominations and every voter.
4. Save a read-only `SdeHistoricalReplayInput` for part of future replay/verification.

**Partly possible**: Given a legitimate committed initial snapshot and a complete subset of actual actions, `DynamicActionReducer` reconstructs some earlier mechanics; `HistoricalInformationAbilityStateResolver` already uses that to derive an ability's functioning state at an observation point.

**Not yet possible to assert**: For arbitrary `globalSequence = N`, faithfully reconstruct *all* real Host state, adopted ST decisions, registration witnesses, current mechanics, published information and original exact revisions. Current logs are not complete event-sourcing; text review and the **latest** GameState cannot be used to backfill old hidden facts without temporal leakage.

## 5. Proposed AI request assembly (read-only projection, no extra game object)

The user-approved model produces direct semantic information (number / bool / pair role + seats / single role) without any Host-enumerated answer table; Host validates strictly AFTER it arrives. Do not replace it with legacy candidate-ID selection.

Owner path:

```text
ClocktowerGameSession / committed setup / current Host mechanics
   ├─ TroubleBrewingGameSnapshotProjector.fromRuntime(...)   [current facts]
   ├─ StorytellerProviderHistoryPrefixMaterializerV1         [ordered actions + observations]
   ├─ minimal confirmed ST-choice projection                 [Drunk, red herring, bluffs, overrides]
   └─ current real pending task and frozen revision
                      |
                      v
         Pure CurrentExpertDecisionInputV1
           - gameId, position and cursor (Host freshness)
           - ONLY initial/full-snapshot fallback when necessary
           - events since last HOST-confirmed model anchor
           - complete nominations + voter lists
           - current single task, role/seat and operation context
           - no list of candidate answers
                      |
                      v
          Responses previous_response_id + prior global strategy
                      |
                      v
             Model semantic result (unvalidated)
                      |
                      v
       Host current-rules validation -> optional adoption -> publication
```

Reuse `ClocktowerSemanticHistoryPersistence` and `EpistemicSemanticJson` typed JSON representations; do **not** produce typed facts with Kotlin `toString()`, and do **not** parse Chinese `ClocktowerEvent.detail` as rule truth. The request object is a one-time **projection**, not a new mutable Host state owner. Keep phase/time/order, event IDs, and information visibility. Sending hidden Host data to the Storyteller model is permitted by task context; never share hidden facts on the player UI.

A continuous-model continuation uses an **acknowledged Host adoption anchor**, not every prior sent proposal. The suffix may contain multiple confirmed actions across night/day; no API call is required after each individual event. If exact coverage cannot be proved, provide a fresh full Host snapshot + complete available history, not a false claim that the suffix is exhaustive. A published result must never be retroactively changed; unaccepted proposals must not enter the history as facts.

## 6. Two minimum, mechanically grounded acceptance fixtures

**Fixture A — First night**:
- Committed setup: seat 4 **shown Chef**, actual Drunk; source of choice recorded.
- Seat 4 **actually given** numeric 0 (typed published observation).
- Poisoner seat 2 **confirmed** target seat 6 (typed Poison).
- Before seat 6 Washerwoman result is published, freeze `FIRST_NIGHT` cursor. No guessing an input role or legal effect: Host supplies actual game roles and statuses, and the model directly outputs a `PAIR` semantic result. Host checks it independently.
- Verify chronology, no future Washerwoman publication, stable external role IDs, no leaked unaccepted recommended information, recovery parity.

**Fixture B — Day 1 to Night 2**:
- Last Host-confirmed provider anchor already includes Night 1.
- Day 1: `Nomination(3,4)`; `Vote(3,4,[1,3,5])`; `Nomination(4,3)`; `Vote(4,3,[2,4,6,7,8])`; `Execution(3)`; `PhaseAdvance(NIGHT,2)`.
- Target decision: seat 2 shown Empath, actual Drunk, for its new numeric information.
- Verify **both** voting groups and voter seats retained, all events ordered, original Night-1 Empath info available in continuation/fallback, and Night-2 result absent from its own request.
- Verify post-validation on 0/1/2 as allowed by the actual rules; do not turn example `1` into a prescribed strategic choice.

Acceptance: actual Host-built input from real production writer + restored-input parity; independently review exact JSON and test output legality, not merely synthetic DTO tests.

## 7. Smallest rational development order

1. **Do not modify TBGS V1 merely to carry history.** Keep immutable TBGS state projection and a separately ordered history prefix; use the already available SDE historical interchange to avoid a third serialization stack.
2. Build **read-only, frozen** current-decision input from existing owners, with correct typed JSON, no `legalCandidates` in model input, and complete nomination/voter details. Prove what is already complete.
3. Fix only confirmed **missing producers** required by first two fixtures: actual Drunk selection timing/source, Red Herring and bluff commitments, manual override confirmations, and information publication when typed observation is missing. Feed into *existing* confirmed Host history/journal or a minimal chronological choice layer, not a new universal event store.
4. Implement direct-result Host rule validation; use existing rules/domains **internally** if efficient, without transmitting them or transforming the model into a candidate selector. Illegal outputs fail closed and count as model-quality defects, not automatically corrected suggestions.
5. Validate A/B request cutoffs including concurrent changes and 4-hour recovery. If anything is unproven, fall back to a full state/history input.
6. **Later / separate milestone**: extend generic time-point replay and EvidenceLab cross-project equivalence to a verified runtime/multi-night fixture. Do not block the first live AI continuation on a finished universal historical simulator.

## 8. PR / workflow implications at audit time

- [#310](https://github.com/Jazz0006/CampBoardGameHost/pull/310) single-decision Host validation / private cache and [#311](https://github.com/Jazz0006/CampBoardGameHost/pull/311) causal history suffix: reusable as safe mechanics, but must be reviewed against direct-value semantics.
- [#312](https://github.com/Jazz0006/CampBoardGameHost/pull/312) legacy `candidateId` response format is **superseded as the model-facing contract** by the new direct result design; do not accidentally merge it as final policy.
- [#313](https://github.com/Jazz0006/CampBoardGameHost/pull/313) filters routine votes and retains caches across such vote changes; **contradicts newly approved product requirement** and must be revised/superseded before production merge. An experimental, unmerged 1E branch already attempts to restore votes and define direct output, but it is not validated/live yet.
- This document itself is **an audit only**: no change to Host runtime, Android, EvidenceLab or paid OpenAI requests; no claim of end-to-end arbitrary-time replay.

## 9. Audit conclusion / explicit product decision

The previously accepted TBGS/UGSM direction was correct: **canonical current state + chronological confirmed events + a decision-time cutoff**, shared semantically between real Host and reconstructed EvidenceLab. The mature SDE export and partial reducer materially reduce new implementation work. The missing work is narrow: completeness of committed Storyteller choices and publication facts, stable typed transport to current model, safe anchor semantics and direct-result validation. **Do not reopen an all-game-events logging initiative or rewrite GameState.**
