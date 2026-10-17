package com.codex.campboardgamehost

/**
 * Shared UI/request cache identity. Switching operation modes invalidates a
 * previously accepted preview without requiring fabricated Host state changes.
 * Candidate order and IDs are Host-authoritative; this is NOT a legal-domain
 * substitute for the fresh full scope comparison at adoption.
 */
internal object ClocktowerResidualRequestIdentityV2 {
    fun build(
        gameId: String,
        round: Int,
        stepIndex: Int,
        requestId: String,
        gameStateRevision: Long,
        playerInputRevision: Long,
        exclusiveGlobalSequence: Long,
        legalCandidateIds: List<String>,
        mode: StorytellerOperationMode,
    ): String {
        require(mode != StorytellerOperationMode.MANUAL)
        return listOf(
            gameId, round, stepIndex, requestId,
            gameStateRevision, playerInputRevision,
            exclusiveGlobalSequence,
            legalCandidateIds.size, legalCandidateIds.hashCode(),
            mode.name,
        ).joinToString(":")
    }
}
