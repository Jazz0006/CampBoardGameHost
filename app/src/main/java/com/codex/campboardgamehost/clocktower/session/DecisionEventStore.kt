package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.DecisionCorrectionEvent
import com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus
import com.codex.campboardgamehost.clocktower.domain.DecisionHistoryArchive
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent

internal data class DecisionRevision(
    val gameStateRevision: Long,
    val playerInputRevision: Long,
) {
    init {
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0) { "playerInputRevision cannot be negative." }
    }
}

internal sealed interface DecisionAppendResult {
    data class Appended(val event: StorytellerDecisionEvent) : DecisionAppendResult
    data class Existing(val event: StorytellerDecisionEvent) : DecisionAppendResult
    data class StaleRequest(
        val requested: DecisionRevision,
        val current: DecisionRevision,
    ) : DecisionAppendResult
    data class IdempotencyConflict(
        val existing: StorytellerDecisionEvent,
        val rejected: StorytellerDecisionEvent,
    ) : DecisionAppendResult
}

internal interface DecisionEventStore {
    fun appendAtomically(
        event: StorytellerDecisionEvent,
        currentRevision: DecisionRevision,
    ): DecisionAppendResult

    fun allEvents(): List<StorytellerDecisionEvent>
    fun transitionStatus(eventId: String, expected: DecisionEventStatus, next: DecisionEventStatus): Boolean
    fun appendCorrection(correction: DecisionCorrectionEvent): Boolean
    fun archive(): DecisionHistoryArchive
}

internal class InMemoryDecisionEventStore(
    initialArchive: DecisionHistoryArchive = DecisionHistoryArchive(),
) : DecisionEventStore {
    private val lock = Any()
    private val events = initialArchive.events.toMutableList()
    private val corrections = initialArchive.corrections.toMutableList()

    init {
        require(events.map { it.eventId }.distinct().size == events.size) { "Restored event IDs must be unique." }
        require(events.map { it.idempotencyKey }.distinct().size == events.size) { "Restored idempotency keys must be unique." }
        validateCorrectionChain(corrections, events)
    }

    override fun appendAtomically(
        event: StorytellerDecisionEvent,
        currentRevision: DecisionRevision,
    ): DecisionAppendResult = synchronized(lock) {
        val existing = events.firstOrNull { it.idempotencyKey == event.idempotencyKey }
        if (existing != null) {
            return@synchronized if (existing.copy(status = event.status) == event) {
                DecisionAppendResult.Existing(existing)
            } else {
                DecisionAppendResult.IdempotencyConflict(existing, event)
            }
        }
        require(events.none { it.eventId == event.eventId }) {
            "eventId already exists under a different idempotency key."
        }

        val requestedRevision = DecisionRevision(
            gameStateRevision = event.gameStateRevision,
            playerInputRevision = event.playerInputRevision,
        )
        if (requestedRevision != currentRevision) {
            return@synchronized DecisionAppendResult.StaleRequest(requestedRevision, currentRevision)
        }
        events += event
        DecisionAppendResult.Appended(event)
    }

    override fun allEvents(): List<StorytellerDecisionEvent> = synchronized(lock) { events.toList() }

    override fun transitionStatus(
        eventId: String,
        expected: DecisionEventStatus,
        next: DecisionEventStatus,
    ): Boolean = synchronized(lock) {
        val index = events.indexOfFirst { it.eventId == eventId }
        if (index < 0 || events[index].status != expected || !isAllowedTransition(expected, next)) {
            return@synchronized false
        }
        events[index] = events[index].copy(status = next)
        true
    }

    override fun appendCorrection(correction: DecisionCorrectionEvent): Boolean = synchronized(lock) {
        if (corrections.any {
                it.eventId == correction.eventId ||
                    it.replacedEventId == correction.replacedEventId ||
                    it.replacementEventId == correction.replacementEventId
            }
        ) {
            return@synchronized false
        }
        if (events.none { it.eventId == correction.replacedEventId } || events.none { it.eventId == correction.replacementEventId }) {
            return@synchronized false
        }
        val proposed = corrections + correction
        runCatching { validateCorrectionChain(proposed, events) }.getOrElse { return@synchronized false }
        corrections += correction
        true
    }

    override fun archive(): DecisionHistoryArchive = synchronized(lock) {
        DecisionHistoryArchive(events.toList(), corrections.toList())
    }

    private fun isAllowedTransition(from: DecisionEventStatus, to: DecisionEventStatus): Boolean = when (from) {
        DecisionEventStatus.PROPOSED -> to == DecisionEventStatus.CONFIRMED || to == DecisionEventStatus.FAILED
        DecisionEventStatus.CONFIRMED -> to == DecisionEventStatus.APPLIED || to == DecisionEventStatus.FAILED
        DecisionEventStatus.APPLIED, DecisionEventStatus.FAILED -> false
    }

    private fun validateCorrectionChain(
        candidateCorrections: List<DecisionCorrectionEvent>,
        candidateEvents: List<StorytellerDecisionEvent>,
    ) {
        val eventIds = candidateEvents.map { it.eventId }.toSet()
        require(candidateCorrections.map { it.eventId }.distinct().size == candidateCorrections.size)
        require(candidateCorrections.map { it.replacedEventId }.distinct().size == candidateCorrections.size)
        require(candidateCorrections.map { it.replacementEventId }.distinct().size == candidateCorrections.size)
        require(candidateCorrections.all { it.replacedEventId in eventIds && it.replacementEventId in eventIds })
        candidateCorrections.forEach { start ->
            val visited = mutableSetOf<String>()
            var current: String? = start.replacedEventId
            while (current != null) {
                require(visited.add(current)) { "Correction chain cannot contain a cycle." }
                current = candidateCorrections.firstOrNull { it.replacedEventId == current }?.replacementEventId
            }
        }
    }
}
