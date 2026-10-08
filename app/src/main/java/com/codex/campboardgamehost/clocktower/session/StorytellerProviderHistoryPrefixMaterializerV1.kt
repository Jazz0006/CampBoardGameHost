package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryEntryV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderHistoryPrefixV1
import com.codex.campboardgamehost.clocktower.domain.StorytellerProviderRevisionV1
import com.codex.campboardgamehost.clocktower.domain.requireCompatible
import com.codex.campboardgamehost.clocktower.epistemic.ObservationTimelineBinding

/**
 * R1B: capture from ONE already-frozen session state, before a pending recommendation begins.
 * Only a GLOBAL_V1 live state has a provable exclusive chronological cutoff here.
 * Historical requests require a separately persisted at-decision frozen snapshot (R1C).
 */
internal object StorytellerProviderHistoryPrefixMaterializerV1 {
    fun captureLive(
        sessionState: ClocktowerSessionState,
        sourceRevision: StorytellerProviderRevisionV1,
    ): StorytellerProviderHistoryPrefixV1 {
        require(sessionState.gameStateRevision == sourceRevision.gameStateRevision) {
            "History game-state revision must match the same captured session."
        }
        require(sessionState.playerInputRevision == sourceRevision.playerInputRevision) {
            "History player-input revision must match the same captured session."
        }
        sessionState.semanticHistoryMode.requireCompatible(
            actionTimeline = sessionState.actionTimeline,
            observationLog = sessionState.epistemicObservationLog,
            nextTimelineGlobalSequence = sessionState.nextTimelineGlobalSequence,
        )
        if (sessionState.semanticHistoryMode == ClocktowerSemanticHistoryMode.LEGACY_LOCAL) {
            return StorytellerProviderHistoryPrefixV1.unavailableLegacy(
                sessionState.gameId, sourceRevision,
            )
        }

        val exclusiveCutoff = sessionState.nextTimelineGlobalSequence
        val actions = sessionState.actionTimeline.entries.map { entry ->
            StorytellerProviderHistoryEntryV1.Action(entry.fact, entry.point)
        }
        val observations = sessionState.epistemicObservationLog.records.map { record ->
            val binding = record.timelineBinding as? ObservationTimelineBinding.Global
                ?: error("GLOBAL_V1 provider history requires globally bound observations.")
            StorytellerProviderHistoryEntryV1.Observation(
                entryId = record.recordId,
                point = binding.point,
                sourceSeat = record.sourceSeat,
                sourceAbility = record.sourceAbility,
                visibility = record.visibility,
                recipientSeats = record.recipientSeats,
                reliability = record.reliability,
                proposition = record.proposition,
            )
        }
        return StorytellerProviderHistoryPrefixV1.capturedLive(
            gameId = sessionState.gameId,
            sourceRevision = sourceRevision,
            exclusiveGlobalSequence = exclusiveCutoff,
            entries = (actions + observations).sortedWith(
                compareBy<StorytellerProviderHistoryEntryV1> { it.point.globalSequence }
                    .thenBy { it.kind }
                    .thenBy { it.entryId },
            ),
        )
    }
}
