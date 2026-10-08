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

    /** Visible action identities omit concealed actual-role/ability-functioning metadata. */
    data class PublicSlayerShot(
        val actionId: String,
        val claimantSeat: Int,
        val targetSeat: Int,
        override val point: TimelinePoint,
    ) : PlayerHistoricalEvent

    data class PublicNomination(
        val actionId: String,
        val nominatorSeat: Int,
        val nomineeSeat: Int,
        override val point: TimelinePoint,
    ) : PlayerHistoricalEvent

    data class PublicVote(
        val actionId: String,
        val nominatorSeat: Int,
        val nomineeSeat: Int,
        val voterSeats: List<Int>,
        val ghostVoterSeats: List<Int>,
        override val point: TimelinePoint,
    ) : PlayerHistoricalEvent

    /** Only the publicly selected seat is visible. Learn-time functioning stays private. */
    data class PublicKlutzChoice(
        val actionId: String,
        val klutzSeat: Int,
        val chosenSeat: Int,
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
                is ActionFact.SlayerShot -> PlayerHistoricalEvent.PublicSlayerShot(
                    fact.actionId, fact.claimantSeat, fact.targetSeat, entry.point,
                )
                is ActionFact.Nomination -> PlayerHistoricalEvent.PublicNomination(
                    fact.actionId, fact.nominatorSeat, fact.nomineeSeat, entry.point,
                )
                is ActionFact.Vote -> PlayerHistoricalEvent.PublicVote(
                    fact.actionId, fact.nominatorSeat, fact.nomineeSeat,
                    fact.voterSeats, fact.ghostVoterSeats, entry.point,
                )
                is ActionFact.KlutzLearnedDeath -> null // never disclose actual Klutz role or functioning
                is ActionFact.KlutzChoice -> PlayerHistoricalEvent.PublicKlutzChoice(
                    fact.actionId, fact.klutzSeat, fact.chosenSeat, entry.point,
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
            val visible = record.visibility == ObservationVisibility.PUBLIC ||
                recipientSeat in record.recipientSeats
            if (visible) PlayerHistoricalEvent.Observation(record, point) else null
        }

        return (actionEvents + observationEvents).sortedWith(
            compareBy<PlayerHistoricalEvent>({ it.point.globalSequence }, { stableTieBreaker(it) }),
        )
    }

    private fun stableTieBreaker(event: PlayerHistoricalEvent): String = when (event) {
        is PlayerHistoricalEvent.PublicExecution -> "action:${event.actionId}"
        is PlayerHistoricalEvent.PublicDeath -> "action:${event.actionId}"
        is PlayerHistoricalEvent.PublicSlayerShot -> "action:${event.actionId}"
        is PlayerHistoricalEvent.PublicNomination -> "action:${event.actionId}"
        is PlayerHistoricalEvent.PublicVote -> "action:${event.actionId}"
        is PlayerHistoricalEvent.PublicKlutzChoice -> "action:${event.actionId}"
        is PlayerHistoricalEvent.PhaseAdvance -> "action:${event.actionId}"
        is PlayerHistoricalEvent.Observation -> "observation:${event.record.recordId}"
    }
}
