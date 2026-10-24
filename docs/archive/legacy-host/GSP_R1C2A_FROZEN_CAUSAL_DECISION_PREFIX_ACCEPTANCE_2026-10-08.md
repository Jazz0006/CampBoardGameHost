# GSP-R1C2A — Frozen Predecision Prefix and Independent Causal Journal Acceptance

> Date: 2026-10-08 Australia/Sydney  
> Project: `Jazz0006/CampBoardGameHost`  
> Status: **GSP-R1C2A COMPLETE / ACCEPTED; R1C2 overall IN PROGRESS**  
> Executable [PR #256](https://github.com/Jazz0006/CampBoardGameHost/pull/256): squash merge `38503670d93d925603e12cfc6388990550421c44`; accepted exact head `6b60bcfcf309eaf11f91d4e7ea73c22eeb448740`  
> CI **#3864 GREEN** (Android FAST); R2 boundary **#3527 GREEN**. Android FULL/debug assemble, ASP and Real Clingo **not executed** for this change. No T4 claim.

## What was actually implemented

The production-code foundation `clocktower/session/StorytellerCausalDecisionJournalV1.kt` adds **two separate neutral boundaries**, both written as non-network, engine-read-only code:

1. `StorytellerDecisionPrefixCaptureV1.capture(request, sessionState)` requires an exact `GLOBAL_V1` live Session source, matching game/seed/game revision/player-input revision, typed semantic snapshot position, known actual/shown roles, living status and poison status, and exactly the materialized provider history prefix (including exclusive global cursor). It produces `FrozenStorytellerDecisionPrefixV1` with immutable typed snapshot identity, read-only history, player-context copy and legal candidate IDs. **A revision match alone is insufficient**; stale same-revision history fails closed.
2. `StorytellerCausalDecisionJournalV1` is a **standalone in-memory reference chronology** with append-only decision captures, commits and corrections. It does **not** pretend that decision event order equals game-state revision or mechanical history globalSequence. `effectiveAt(decisionId)` projects only commits/corrections which happened **before that frozen capture**; a later replacement cannot rewrite old decision input. Corrections require two already-committed outcomes with replacement later than the replaced commit. Context for a new request draws prior decisions from this explicit journal, not revision-filtered final `DecisionHistoryRepository.project()`.

The accepted test `StorytellerCausalDecisionJournalV1Test` exercises:
- two decisions at the same game/player revision but **different global mechanical cutoffs**;
- another capture at the same global cutoff **after a correction** (separate decision order);
- historical prefix unaffected by future events, decisions and later corrections;
- Spy/Recluse-relevant Empath numeric result observed without an invented registration ruling;
- stale same-revision request rejected after an action; frozen player inputs independent of subsequent caller mutations;
- invalid legacy/unavailable history; unverified candidate registration facts rejected; duplicate, backward and premature corrections rejected.

**General rule:** actual displayed information is an observation; possible Spy/Recluse registration witnesses are not facts. PR #254's shared Result–Witness separation remains authoritative. This new journal refuses registration-bearing committed outcomes until a trusted typed explicit-ruling producer is accepted.

## Honest coverage / limits

**This is not yet a production durable decision history.** The existing `StorytellerProviderRequestFactoryV1` has no production caller; the new journal is not wired into Host gameplay and has no accepted save/recovery codec. The legacy `DecisionEventStore` and revision-filtered `DecisionHistoryRepository` have not been cut over, and cannot be considered causal. No old decision can be reconstructed using the current session, revision, free-text localized events, or current journal's state. `StorytellerProviderHistoryPrefixV1` coverage remains PARTIAL/UNKNOWN as in R1B.

There is **no new LLM API or automatic recommendation authority**; manual/offline Game Engine behavior is unchanged.

## Next bounded stage: GSP-R1C2B — authoritative producer + durable replay admission

1. Audit actual production **pending decision + confirmation + correction** paths first. The neutral `StorytellerProviderRequestFactoryV1` is currently unused by production; find the smallest real Host-owned producer. Ensure it captures before applying decision effects or publishing observations, not after. Existing player/result-first semantics and registration ambiguity cannot be compromised.
2. Choose a minimal durable store/schema with **typed decision capture identity, source revision, exact global cutoff, frozen snapshot/player inputs, monotonically ordered decision facts and correction provenance**, not gameplay text; adopt a migration policy for current-only Recovery and explicit unavailability for earlier saves. Avoid broad Recovery rewrites for unrelated games.
3. Require append-idempotence, unique decision/event/correction IDs, no same-decision replacement of old captures, no future leakage, typed explicit ruling provenance, and as-of replay after actual `RecoverySnapshotJsonCodec.encode/decodeStrict + RecoveryRestorePlanner.prepare + restored Session`.
4. Add failed-path and recovery parity tests including two same-revision decisions separated by a mechanical event, later correction of the first decision, and a result-first Chef/Empath Spy–Recluse ambiguity with no selected witness. Keep trusted typed registration producer a **dedicated audited slice**, not a presumed property of role-action UI text.
5. Full R1C acceptance only when a real production data producer, durable causal storage, and Recovery/as-of no-future-leak tests are green. Until then, declare R1C **IN PROGRESS** and report unrecorded past decisions as unavailable, never as complete/empty historical evidence.

References: [R1A audit](GSP_R1A_CANONICAL_HISTORY_CAPTURE_PREFIX_COVERAGE_AUDIT_2026-10-08.md), [R1B acceptance](GSP_R1B_NEUTRAL_LIVE_HISTORY_PREFIX_ACCEPTANCE_2026-10-08.md), [R1C1 acceptance](GSP_R1C1_RECOVERY_PREFIX_PARITY_AND_HISTORICAL_CUTOFF_ACCEPTANCE_2026-10-08.md), [Registration Result–Witness accepted fix](GSP_REGISTRATION_RESULT_WITNESS_SEPARATION_FIX_ACCEPTANCE_2026-10-08.md), AGENTS.md, TESTING_STRATEGY.md.
