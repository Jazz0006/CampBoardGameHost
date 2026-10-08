# GSP-R1C1 — Recovery Prefix Parity / Missing Historical Cutoff Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Repository: `Jazz0006/CampBoardGameHost`  
> Status: **GSP-R1C1 COMPLETE / ACCEPTED; R1C overall IN PROGRESS**  
> Executable PR: [#252](https://github.com/Jazz0006/CampBoardGameHost/pull/252)  
> Squash merge: `fae562f0e52d2d8650b28b6b11964e64a41a3766`; submitted HEAD `8e3aa7daa9ca8f164b92be52b2314712470aebfb`  
> CI **#3853 GREEN** (Android FAST tests; Real Clingo; Android FULL/assemble skipped), R2 boundary **#3520 GREEN**.  
> Baseline: R1B accepted PR #250 and [GSP-R1B audit](GSP_R1B_NEUTRAL_LIVE_HISTORY_PREFIX_ACCEPTANCE_2026-10-08.md).  
> This closure is an acceptance record, **not** a new automatic recommendation path.

## 1. Accepted executable changes

### End-to-end genuine Recovery round trip for current-game Provider prefix

`RecoveryRestorePlannerTest.globalProviderPrefixIsIdenticalAcrossStrictRecoveryCodecPlannerAndRestoredSession`:

1. Start a `ClocktowerGameSession` in `GLOBAL_V1`.
2. Commit an ordered **FirstNight Poison action -> private Chef numeric observation -> Day Execution action -> public AliveAt observation -> OtherNight Protect action** via Session-owned global commit functions.
3. Freeze source revision, session state and exclusive global sequence; materialize the typed neutral `StorytellerProviderHistoryPrefixV1`.
4. Construct the **current-format** `RecoverySnapshot` with canonical session action timeline and observations.
5. Pass through real `RecoverySnapshotJsonCodec.encode` / `decodeStrict`, plus `RecoveryRestorePlanner.prepare` with resolved ruleset/seat validation.
6. Recreate restored `ClocktowerSessionState` and `ClocktowerGameSession.restoreProduction` from the decoded current-format fields, then materialize the Provider prefix at the **same logical cutoff**.
7. Assert **entire provider prefix equals before/after** (game ID, revision, cutoff=5 exclusive, action/observation typed payloads, interleaved sequences 0–4, observation reliability/visibility/recipients, coverage), and test subsequent new action uses global sequence 5.

The test exercises actual current-format serialization and planner, **not** merely an in-memory copy of the timeline. It does not prove parity for every missing producer, every possible cursor gap, or a historical replay archive.

### Historical cutoff missing => explicit unavailable

The neutral history contract and materializer now expose an explicit `unavailableHistoricalCutoff` / `withoutFrozenHistoricalCutoff` state. This has **no events, no invented global cutoff**, and marks each coverage dimension `UNRECONSTRUCTABLE / HISTORICAL_CUTOFF_UNAVAILABLE`. Tests show a subsequent action at the **same unchanged game/player revision** must not be mistakenly included as past decision evidence.

This is a **fail-closed gate**, not an implementation of frozen historical decision materialization.

### Spy/Recluse ambiguity preserved

The `StorytellerProviderHistoryPrefixMaterializerV1Test` fixture puts a functioning **Empath** between **Spy** and **Recluse**. Two legal combinations yield player-visible **1**. The committed neutral history contains only the actual `NumericResult(1)` observation; registration coverage remains `UNKNOWN`; the historical prefix contains **no implied selected registration witness**.

This validates the **Provider/History negative invariant** defined in [GSP registration ambiguity contract](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md). It **does not** fix the current Host UI result-first dedup/first-witness assignment behavior: that requires a separate typed producer/UI implementation.

## 2. What R1C1 has not completed

- **No historical frozen decision cutoff.** There is not yet a durable exact `gameId + decisionId + source revision + exclusive global cursor + snapshot identity + frozen inputs` captured on the actual decision boundary. Historical prefix requests without it correctly fail closed.
- **No causally ordered persisted decision/correction stream.** `DecisionHistoryArchive` historical corrections still cannot be projected as if they were known at older decisions. Current Recovery does not own durable decision history.
- **No complete typed registration ruling producer.** Spy/Recluse first-witness choices cannot safely be treated as explicit canonical rulings. Nor are public narrative/claims, Demon bluffs, Spy Grimoire and every private information route proven complete.
- **No full T4 Android acceptance** on this new executable checkpoint: Android FAST and Real Clingo were green, Android FULL/assemble were skipped by CI selection.
- **No production LLM API or authority.** Game Engine/manual gameplay remain unchanged.

## 3. Next phase: GSP-R1C2 — explicit frozen decision prefix & correction-causality contract

Before adding fields to production Recovery, re-audit exact decision commit producers and `StorytellerDecisionEvent` / `DecisionCorrectionEvent` / `DecisionHistoryRepository.project`; do not reinterpret `gameStateRevision` or `playerInputRevision` as an event clock.

Recommended bounded sequence:

1. Prove a **single authoritative decision-time capture seam** exists or identify the missing producer. Design a durable, typed at-decision envelope with exact `gameId`, `decisionId`, revisions, exclusive global cursor, snapshot digest/identity, and frozen player context. Capture it **before** recommendation or decision commit, not from the later Session state.
2. Decide ownership/persistence: current-only Host Recovery versus a separate durable chronological decision store, considering recovery schema compatibility and idempotency; **no broad Recovery migration solely to satisfy replay**. Candidate selection and registration alternatives are not automatically canonical observations/rulings.
3. Define correction provenance, causality and as-of cutoff projection. A later correction must not erase or rewrite what an earlier recommendation could see. If the required metadata is absent, preserve `DECISIONS_INCOMPLETE` and explicit historical unavailability.
4. Add focused RED/GREEN tests for two decisions in the same revision but different global cursors; later correction to first decision; fresh/recovered replay at the old point; no future leakage; mixed poison/reliable observation; ambiguous Spy/Recluse number 1 never selecting a witness.
5. Keep [R1A audit](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md), [R1B closure](GSP_R1B_NEUTRAL_LIVE_HISTORY_PREFIX_ACCEPTANCE_2026-10-08.md), [registration contract](GSP_REGISTRATION_AMBIGUITY_RESULT_FIRST_HISTORY_CONTRACT_2026-10-08.md), `AGENTS.md` and `TESTING_STRATEGY.md` authoritative; require independently GREEN checks for the actual executable head.

**Status rule:** GSP-R1C overall remains **IN PROGRESS** until exact decision prefix/correction and representative Recovery/no-future-leak coverage are accepted. A future UI/registration producer cutover is not implied by R1C1 acceptance.
