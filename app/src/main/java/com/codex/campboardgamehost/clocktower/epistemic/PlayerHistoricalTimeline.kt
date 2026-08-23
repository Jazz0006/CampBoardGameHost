package com.codex.campboardgamehost.clocktower.epistemic

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * One recipient-visible event on the shared historical timeline.
 *
 * Storyteller-only mechanical choices are deliberately not representable here. Exact player-world
 * replay may consume this stream without learning hidden Poison/Protect/Attack/RoleChange facts.
 */
internal sealed interface PlayerHistoricalEvent {
    val point: TimelinePoint

    data class PublicExecution(
        val actionId: String,
        val targetSeat: Int,
        override val point: TimelinePoint,
    ) : PlayerHistoricalEvent

    data class PublicDeath(
        val actionId: String,
        val targetSeat: Int,
        override val point: TimelinePoint,
    ) : PlayerHistoricalEvent

    data class PhaseAdvance(
        val actionId: String,
        val phase: StorytellerPhase,
        val round: Int,
        override val point: TimelinePoint,
    ) : PlayerHistoricalEvent

    data class Observation(
        val record: RecordedEpistemicObservation,
        override val point: TimelinePoint,
    ) : PlayerHistoricalEvent
}

/**
 * Projects the durable GLOBAL_V1 action/observation histories into one knowledge-safe replay stream
 * for a specific player. [TimelinePoint.globalSequence] remains the only cross-type ordering
 * authority; local round/sequence values are diagnostic context only.
 */
internal object PlayerHistoricalTimeline {
    fun project(
        recipientSeat: Int,
        actionTimeline: ActionFactTimeline,
        observationLog: EpistemicObservationLog,
    ): List<PlayerHistoricalEvent> {
        require(recipientSeat > 0) { "Historical replay recipient seat must be positive." }
        actionTimeline.requireCompatibleWith(observationLog)

        val actionEvents = actionTimeline.entries.mapNotNull { entry ->
            when (val fact = entry.fact) {
                is ActionFact.Execution -> PlayerHistoricalEvent.PublicExecution(
                    actionId = fact.actionId,
                    targetSeat = fact.targetSeat,
                    point = entry.point,
                )
                is ActionFact.Death -> PlayerHistoricalEvent.PublicDeath(
                    actionId = fact.actionId,
                    targetSeat = fact.targetSeat,
                    point = entry.point,
                )
                is ActionFact.PhaseAdvance -> PlayerHistoricalEvent.PhaseAdvance(
                    actionId = fact.actionId,
                    phase = fact.phase,
                    round = fact.round,
                    point = entry.point,
                )
                is ActionFact.Poison,
                is ActionFact.Protect,
                is ActionFact.Attack,
                is ActionFact.RoleChange,
                -> null
            }
        }

        val observationEvents = observationLog.records.mapNotNull { record ->
            val point = (record.timelineBinding as? ObservationTimelineBinding.Global)?.point
                ?: throw IllegalArgumentException(
                    "Player historical replay requires globally bound observations; LegacyLocal chronology cannot be merged with actions.",
                )
            val visible = record.visibility == ObservationVisibility.PUBLIC || recipientSeat in record.recipientSeats
            if (visible) PlayerHistoricalEvent.Observation(record, point) else null
        }

        return (actionEvents + observationEvents).sortedWith(
            compareBy<PlayerHistoricalEvent>({ it.point.globalSequence }, { stableTieBreaker(it) }),
        )
    }

    private fun stableTieBreaker(event: PlayerHistoricalEvent): String = when (event) {
        is PlayerHistoricalEvent.PublicExecution -> "action:${event.actionId}"
        is PlayerHistoricalEvent.PublicDeath -> "action:${event.actionId}"
        is PlayerHistoricalEvent.PhaseAdvance -> "action:${event.actionId}"
        is PlayerHistoricalEvent.Observation -> "observation:${event.record.recordId}"
    }
}
