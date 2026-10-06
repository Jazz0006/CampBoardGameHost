package com.codex.campboardgamehost.clocktower.session

/**
 * Engine-owned identity for one Storyteller decision.
 *
 * It deliberately carries no recommendation/policy identity. The request ID is a stable semantic
 * decision key within the current canonical game.
 */
internal data class StorytellerDecisionRequestIdentity(
    val gameId: String,
    val requestId: String,
) {
    init {
        require(gameId.isNotBlank()) { "Storyteller decision game ID cannot be blank." }
        require(requestId.isNotBlank()) { "Storyteller decision request ID cannot be blank." }
    }
}

/** Freshness boundary shared by every engine-owned Storyteller decision. */
internal data class StorytellerDecisionRevision(
    val gameStateRevision: Long,
    val playerInputRevision: Long,
) {
    init {
        require(gameStateRevision >= 0) { "gameStateRevision cannot be negative." }
        require(playerInputRevision >= 0) { "playerInputRevision cannot be negative." }
    }
}

/**
 * One rules-legal candidate in an engine-owned pending decision.
 *
 * [payload] is mechanical/domain data only; ranking, score and recommendation metadata do not
 * belong here.
 */
internal data class StorytellerDecisionCandidate<T>(
    val candidateId: String,
    val payload: T,
) {
    init {
        require(candidateId.isNotBlank()) { "Storyteller decision candidate ID cannot be blank." }
    }
}

internal enum class StorytellerDecisionBlockReason {
    STALE_CONTEXT,
    ILLEGAL_CANDIDATE,
}

internal sealed interface StorytellerDecisionConfirmation<out T> {
    data class Confirmed<T>(
        val candidate: StorytellerDecisionCandidate<T>,
    ) : StorytellerDecisionConfirmation<T>

    data class Blocked(
        val reason: StorytellerDecisionBlockReason,
    ) : StorytellerDecisionConfirmation<Nothing>
}

/**
 * Script/decision-neutral pending-decision seam owned by the Game Engine/session layer.
 *
 * It knows only identity, freshness and the complete rules-legal candidate domain. It does not know
 * where a candidate recommendation came from and it cannot mutate canonical game state.
 */
internal class PendingStorytellerDecision<T>(
    val requestIdentity: StorytellerDecisionRequestIdentity,
    val revision: StorytellerDecisionRevision,
    legalCandidates: List<StorytellerDecisionCandidate<T>>,
) {
    val legalCandidates: List<StorytellerDecisionCandidate<T>> = legalCandidates.toList()
    private val candidatesById = this.legalCandidates.associateBy { candidate -> candidate.candidateId }

    init {
        require(this.legalCandidates.isNotEmpty()) {
            "Pending Storyteller decision requires at least one legal candidate."
        }
        require(candidatesById.size == this.legalCandidates.size) {
            "Pending Storyteller decision candidate IDs must be unique."
        }
    }

    fun confirm(
        candidateId: String,
        currentRevision: StorytellerDecisionRevision,
    ): StorytellerDecisionConfirmation<T> {
        if (currentRevision != revision) {
            return StorytellerDecisionConfirmation.Blocked(StorytellerDecisionBlockReason.STALE_CONTEXT)
        }
        val candidate = candidatesById[candidateId]
            ?: return StorytellerDecisionConfirmation.Blocked(StorytellerDecisionBlockReason.ILLEGAL_CANDIDATE)
        return StorytellerDecisionConfirmation.Confirmed(candidate)
    }
}
