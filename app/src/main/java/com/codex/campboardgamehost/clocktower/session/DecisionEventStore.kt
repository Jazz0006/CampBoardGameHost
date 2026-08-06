package com.codex.campboardgamehost.clocktower.session

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
}

internal class InMemoryDecisionEventStore : DecisionEventStore {
    private val lock = Any()
    private val events = mutableListOf<StorytellerDecisionEvent>()

    override fun appendAtomically(
        event: StorytellerDecisionEvent,
        currentRevision: DecisionRevision,
    ): DecisionAppendResult = synchronized(lock) {
        val existing = events.firstOrNull { it.idempotencyKey == event.idempotencyKey }
        if (existing != null) {
            return@synchronized if (existing == event) {
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
}
