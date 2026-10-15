package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.DecisionCorrectionEvent
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1

/**
 * AI-EVENT-JIT-1B: immutable, replay-safe extraction of events after an EXACT
 * Host-confirmed snapshot. The input anchor is not the last HTTP request.
 *
 * GLOBAL_V1 provides a total chronology ONLY for actions and observations.
 * Legacy Storyteller decisions/corrections have no provable relative position;
 * changed mechanical state or player context can likewise lack an event. We
 * explicitly require full snapshot fallback instead of inventing causal order.
 *
 * This is pure extraction, not a new GameState, persistent journal or provider.
 */
internal object HostCausalEventDeltaV2 {
    internal enum class SnapshotReason {
        MECHANICAL_STATE_CHANGED,
        PLAYER_CONTEXT_CHANGED,
        UNSEQUENCED_DECISION_ARCHIVE_CHANGED,
        UNACCOUNTED_REVISION_CHANGE,
    }

    internal data class Delta(
        val gameId: String,
        val fromExclusiveGlobalSequence: Long,
        val toExclusiveGlobalSequence: Long,
        val newlyLoggedFacts: List<StorytellerProviderHistoryEntryV1>,
        val newlyAppendedDecisions: List<StorytellerDecisionEvent>,
        val newlyAppendedCorrections: List<DecisionCorrectionEvent>,
        val snapshotReasons: Set<SnapshotReason>,
    ) {
        /** All currently represented streams are consistent; not a proof of full Game facts. */
        val noDetectedSnapshotFallback: Boolean get() = snapshotReasons.isEmpty()

        /**
         * Explicitly false until ALL real decisions, including UI-owned choices,
         * have a durable sequenced Host writer and an acknowledged provider
         * event anchor. Never claim complete event coverage from two log streams.
         */
        val allGameEventsProvenComplete: Boolean get() = false
    }

    /**
     * Call ONLY with the frozen Host state last acknowledged as a canonical
     * provider anchor. Failed, unadopted or expired provider responses must not
     * replace it. No event or model response is committed by this extractor.
     */
    fun since(
        lastConfirmed: ClocktowerSessionState,
        now: ClocktowerSessionState,
    ): Delta {
        require(lastConfirmed.gameId == now.gameId &&
            lastConfirmed.gameSeed == now.gameSeed &&
            lastConfirmed.gameState.script == now.gameState.script) {
            "Cannot delta across games, seeds or scripts."
        }
        require(lastConfirmed.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1 &&
            now.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        require(now.gameStateRevision >= lastConfirmed.gameStateRevision &&
            now.playerInputRevision >= lastConfirmed.playerInputRevision &&
            now.nextTimelineGlobalSequence >= lastConfirmed.nextTimelineGlobalSequence) {
            "Host revisions and causal cursor must be monotonic."
        }
        val oldPrefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
            lastConfirmed,
            StorytellerProviderRevisionV1(lastConfirmed.gameStateRevision,
                lastConfirmed.playerInputRevision))
        val newPrefix = StorytellerProviderHistoryPrefixMaterializerV1.captureLive(
            now, StorytellerProviderRevisionV1(
                now.gameStateRevision, now.playerInputRevision))
        require(newPrefix.entries.take(oldPrefix.entries.size) == oldPrefix.entries) {
            "Previously acknowledged actions/observations were deleted or rewritten."
        }
        val incremental = newPrefix.entries.drop(oldPrefix.entries.size)
        require(incremental.all {
            it.point.globalSequence >= lastConfirmed.nextTimelineGlobalSequence &&
                it.point.globalSequence < now.nextTimelineGlobalSequence
        })
        require(incremental.map { it.point.globalSequence }.distinct().size ==
            incremental.size) { "Duplicate causal sequence in increment." }

        val oldArchive = lastConfirmed.decisionHistory
        val newArchive = now.decisionHistory
        val archiveAppendedWithoutRewrite =
            newArchive.events.take(oldArchive.events.size) == oldArchive.events &&
                newArchive.corrections.take(oldArchive.corrections.size) ==
                    oldArchive.corrections

        val snapshotReasons = buildSet {
            if (lastConfirmed.gameState != now.gameState) {
                add(SnapshotReason.MECHANICAL_STATE_CHANGED)
            }
            if (lastConfirmed.storytellerPlayerContextBySeat !=
                now.storytellerPlayerContextBySeat) {
                add(SnapshotReason.PLAYER_CONTEXT_CHANGED)
            }
            if (oldArchive != newArchive) {
                add(SnapshotReason.UNSEQUENCED_DECISION_ARCHIVE_CHANGED)
            }
            if ((lastConfirmed.gameStateRevision != now.gameStateRevision ||
                    lastConfirmed.playerInputRevision != now.playerInputRevision) &&
                incremental.isEmpty() &&
                oldArchive == newArchive &&
                lastConfirmed.gameState == now.gameState &&
                lastConfirmed.storytellerPlayerContextBySeat ==
                    now.storytellerPlayerContextBySeat) {
                add(SnapshotReason.UNACCOUNTED_REVISION_CHANGE)
            }
        }
        return Delta(
            gameId = now.gameId,
            fromExclusiveGlobalSequence = lastConfirmed.nextTimelineGlobalSequence,
            toExclusiveGlobalSequence = now.nextTimelineGlobalSequence,
            newlyLoggedFacts = incremental.toList(),
            // An archive prefix rewrite may be a genuine status transition. The
            // delta MUST fall back to full snapshot, never fabricate the suffix.
            newlyAppendedDecisions = if (archiveAppendedWithoutRewrite)
                newArchive.events.drop(oldArchive.events.size) else emptyList(),
            newlyAppendedCorrections = if (archiveAppendedWithoutRewrite)
                newArchive.corrections.drop(oldArchive.corrections.size) else emptyList(),
            snapshotReasons = snapshotReasons,
        )
    }
}
