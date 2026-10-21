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
