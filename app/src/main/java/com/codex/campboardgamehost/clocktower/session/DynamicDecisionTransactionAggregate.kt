package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.domain.DynamicActionReducer
import com.codex.campboardgamehost.clocktower.domain.DynamicRecommendationKey
import com.codex.campboardgamehost.clocktower.domain.GameSnapshot
import com.codex.campboardgamehost.clocktower.domain.ReducedDynamicGameState
import com.codex.campboardgamehost.clocktower.domain.StorytellerDecisionEvent
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase
import com.codex.campboardgamehost.clocktower.epistemic.RecordedEpistemicObservation

/**
 * Copy-on-write transaction boundary for one committed storyteller decision.
 * It is deliberately independent of the legacy event store so a future durable
 * store has one atomic aggregate to persist instead of several ordered writes.
 */
internal data class DynamicTransactionState(
    val initialSnapshot: GameSnapshot,
    val initialPhase: StorytellerPhase,
    val initialRound: Int,
    val current: ReducedDynamicGameState = DynamicActionReducer.reduce(initialSnapshot, initialPhase, initialRound, emptyList()),
    val events: List<StorytellerDecisionEvent> = emptyList(),
)

internal data class DynamicCommitRequest(
    val expectedKey: DynamicRecommendationKey,
    val event: StorytellerDecisionEvent,
    val displayedObservation: RecordedEpistemicObservation? = null,
    val actionFacts: List<ActionFact> = emptyList(),
)

internal sealed interface DynamicCommitResult {
    data class Applied(val state: DynamicTransactionState) : DynamicCommitResult
    data class AlreadyApplied(val state: DynamicTransactionState, val event: StorytellerDecisionEvent) : DynamicCommitResult
    data class Stale(val currentKey: DynamicRecommendationKey) : DynamicCommitResult
    data class Conflict(val existing: StorytellerDecisionEvent) : DynamicCommitResult
    data class Failed(val cause: Throwable) : DynamicCommitResult
}

internal class DynamicDecisionTransactionAggregate(
    initial: DynamicTransactionState,
    private var currentKey: DynamicRecommendationKey,
) {
    private val lock = Any()
    private var state = initial

    fun snapshot(): DynamicTransactionState = synchronized(lock) { state }
    fun currentKey(): DynamicRecommendationKey = synchronized(lock) { currentKey }

    /** Called by the coordinator after it has captured a new immutable decision snapshot. */
    fun refreshCurrentKey(key: DynamicRecommendationKey) = synchronized(lock) { currentKey = key }

    fun commit(request: DynamicCommitRequest): DynamicCommitResult = synchronized(lock) {
        val existing = state.events.firstOrNull { it.idempotencyKey == request.event.idempotencyKey }
        if (existing != null) {
            return@synchronized if (existing.copy(status = request.event.status) == request.event) {
                DynamicCommitResult.AlreadyApplied(state, existing)
            } else {
                DynamicCommitResult.Conflict(existing)
            }
        }
        if (request.expectedKey != currentKey || !matchesCurrentRevision(request.event)) {
            return@synchronized DynamicCommitResult.Stale(currentKey)
        }
        try {
            val nextFacts = state.current.actionFacts + request.actionFacts
            val nextReduced = DynamicActionReducer.reduce(
                state.initialSnapshot, state.initialPhase, state.initialRound, nextFacts,
            )
            // Facts replay from the immutable mechanical baseline, while observations are
            // durable delivered facts and therefore carry forward from the committed state.
            val nextLog = request.displayedObservation?.let {
                state.current.snapshot.epistemicObservationLog.append(it)
            } ?: state.current.snapshot.epistemicObservationLog
            val nextSnapshot = nextReduced.snapshot.copy(
                playerInputRevision = state.current.snapshot.playerInputRevision + if (request.displayedObservation != null) 1 else 0,
                epistemicObservationLog = nextLog,
            )
            val committedEvent = request.event.copy(status = com.codex.campboardgamehost.clocktower.domain.DecisionEventStatus.APPLIED)
            val nextState = state.copy(
                current = nextReduced.copy(snapshot = nextSnapshot),
                events = state.events + committedEvent,
            )
            state = nextState
            DynamicCommitResult.Applied(nextState)
        } catch (failure: Throwable) {
            DynamicCommitResult.Failed(failure)
        }
    }

    private fun matchesCurrentRevision(event: StorytellerDecisionEvent): Boolean =
        event.gameStateRevision == state.current.snapshot.gameStateRevision &&
            event.playerInputRevision == state.current.snapshot.playerInputRevision
}
