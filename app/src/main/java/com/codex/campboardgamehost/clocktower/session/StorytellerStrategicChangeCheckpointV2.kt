package com.codex.campboardgamehost.clocktower.session

import com.codex.campboardgamehost.clocktower.domain.ClocktowerSemanticHistoryMode
import com.codex.campboardgamehost.clocktower.domain.StorytellerPhase

/**
 * 3E generic *Host fact* checkpoint. There is no event-type or role-specific
 * strategist here: every state transition passes through one evidence gate.
 *
 * IMPORTANT: this is a request gate, NOT a generated catalogue of all future
 * choices. [openDecisions] must be supplied by a live Host pending-decision
 * authority, not model output or a guessed future role activation.
 */
internal object StorytellerStrategicChangeCheckpointV2 {
    internal data class Position(
        val phase: StorytellerPhase,
        val round: Int,
    ) {
        init {
            require(round >= 1)
        }
    }

    internal data class Baseline(
        val state: ClocktowerSessionState,
        val position: Position,
    ) {
        init {
            require(state.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        }
    }

    internal enum class ChangeKind {
        MECHANICAL_STATE,
        CONFIRMED_ACTION,
        PLAYER_RECEIVED_OBSERVATION,
        PLAYER_CONTEXT_OR_CLAIMS,
        STORYTELLER_DECISION_HISTORY,
        GAME_POSITION,
    }

    internal enum class Readiness {
        NO_MATERIAL_CHANGE,
        NO_CURRENT_HOST_OPEN_DECISION,
        HOST_REVISION_REQUIRED,
        READY_FOR_WHOLE_GAME_REVIEW,
    }

    internal data class Review(
        val readiness: Readiness,
        val changeKinds: Set<ChangeKind>,
        val gameId: String,
        val gameStateRevision: Long,
        val playerInputRevision: Long,
        val exclusiveGlobalSequence: Long,
        val openDecisions: List<StorytellerStrategicContinuationV2.OpenDecision>,
    ) {
        val needsReview: Boolean
            get() = readiness == Readiness.READY_FOR_WHOLE_GAME_REVIEW
    }

    fun capture(
        session: ClocktowerGameSession,
        position: Position,
    ): Baseline = Baseline(session.state, position)

    fun review(
        previouslyReviewed: Baseline,
        current: Baseline,
        cursor: StorytellerStrategicContinuationV2.MemoryCursor,
        openDecisions: List<StorytellerStrategicContinuationV2.OpenDecision>,
    ): Review {
        val before = previouslyReviewed.state
        val after = current.state
        require(before.gameId == after.gameId && cursor.gameId == after.gameId)
        require(before.gameSeed == after.gameSeed &&
            before.gameState.script == after.gameState.script)
        require(after.semanticHistoryMode == ClocktowerSemanticHistoryMode.GLOBAL_V1)
        require(cursor.gameStateRevision <= before.gameStateRevision &&
            cursor.playerInputRevision <= before.playerInputRevision &&
            cursor.exclusiveGlobalSequence <= before.nextTimelineGlobalSequence)
        require(after.gameStateRevision >= before.gameStateRevision &&
            after.playerInputRevision >= before.playerInputRevision &&
            after.nextTimelineGlobalSequence >= before.nextTimelineGlobalSequence) {
            "Never review a regressed Host chronology."
        }

        // A released/published observation is historical truth as RECEIVED.
        // Existing entries must never be silently rewritten or deleted.
        require(after.actionTimeline.entries.take(before.actionTimeline.entries.size) ==
            before.actionTimeline.entries) {
            "Cannot rewrite earlier confirmed Host actions."
        }
        require(after.epistemicObservationLog.records.take(
            before.epistemicObservationLog.records.size) ==
            before.epistemicObservationLog.records) {
            "Cannot rewrite already received Host observations."
        }
        // A corrected Storyteller decision must use the canonical correction
        // mechanism, not silently replace an already confirmed event.
        require(after.decisionHistory.events.take(before.decisionHistory.events.size) ==
            before.decisionHistory.events)
        require(after.decisionHistory.corrections.take(
            before.decisionHistory.corrections.size) ==
            before.decisionHistory.corrections)

        require(openDecisions.map { it.decisionId }.distinct().size == openDecisions.size)
        val legalSeats = after.gameState.players.mapTo(setOf()) { it.seat }
        require(openDecisions.all { it.sourceSeat == null || it.sourceSeat in legalSeats })

        // An already-published same-round/private-observation cannot become
        // "open" again merely because a model asks to reconsider it.
        val published = after.epistemicObservationLog.records.filter {
            it.phase == current.position.phase && it.round == current.position.round &&
                it.sourceSeat != null
        }.mapNotNull { it.sourceSeat }.toSet()
        require(openDecisions.none { it.family == "NIGHT_INFORMATION" &&
            it.sourceSeat != null && it.sourceSeat in published }) {
            "Cannot re-open a player-received information decision."
        }

        val changes = buildSet {
            if (before.gameState != after.gameState) add(ChangeKind.MECHANICAL_STATE)
            if (before.actionTimeline != after.actionTimeline) add(ChangeKind.CONFIRMED_ACTION)
            if (before.epistemicObservationLog != after.epistemicObservationLog)
                add(ChangeKind.PLAYER_RECEIVED_OBSERVATION)
            if (before.storytellerPlayerContextBySeat != after.storytellerPlayerContextBySeat)
                add(ChangeKind.PLAYER_CONTEXT_OR_CLAIMS)
            if (before.decisionHistory != after.decisionHistory)
                add(ChangeKind.STORYTELLER_DECISION_HISTORY)
            if (previouslyReviewed.position != current.position)
                add(ChangeKind.GAME_POSITION)
        }
        val progressed = after.gameStateRevision > before.gameStateRevision ||
            after.playerInputRevision > before.playerInputRevision ||
            after.nextTimelineGlobalSequence > before.nextTimelineGlobalSequence
        val readiness = when {
            changes.isEmpty() -> Readiness.NO_MATERIAL_CHANGE
            !progressed -> Readiness.HOST_REVISION_REQUIRED
            openDecisions.isEmpty() -> Readiness.NO_CURRENT_HOST_OPEN_DECISION
            else -> Readiness.READY_FOR_WHOLE_GAME_REVIEW
        }
        return Review(readiness, changes, after.gameId,
            after.gameStateRevision, after.playerInputRevision,
            after.nextTimelineGlobalSequence, openDecisions)
    }

    /**
     * A model response generated against an earlier gate can never commit an
     * updated policy after another Host transition or same-game intervention.
     */
    fun accept(
        reviewed: Review,
        current: Baseline,
        response: StorytellerStrategicContinuationV2.Result,
        cursor: StorytellerStrategicContinuationV2.MemoryCursor,
    ): StorytellerStrategicContinuationV2.MemoryCursor {
        require(reviewed.needsReview)
        val now = current.state
        require(now.gameId == reviewed.gameId &&
            now.gameStateRevision == reviewed.gameStateRevision &&
            now.playerInputRevision == reviewed.playerInputRevision &&
            now.nextTimelineGlobalSequence == reviewed.exclusiveGlobalSequence)
        require(response.affectedOpenDecisionIds.all { id ->
            reviewed.openDecisions.any { it.decisionId == id }
        })
        require(response.gameId == reviewed.gameId &&
            response.gameStateRevision == reviewed.gameStateRevision &&
            response.playerInputRevision == reviewed.playerInputRevision &&
            response.exclusiveGlobalSequence == reviewed.exclusiveGlobalSequence)
        return cursor.advance(response)
    }
}
