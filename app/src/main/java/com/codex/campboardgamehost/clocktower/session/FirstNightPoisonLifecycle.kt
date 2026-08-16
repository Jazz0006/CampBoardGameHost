package com.codex.campboardgamehost.clocktower.session

/**
 * Pure lifecycle contract for the first-night Poisoner action. A selection is
 * an input preview; only confirm turns it into a mechanical state revision.
 */
internal data class FirstNightPoisonLifecycle(
    val gameStateRevision: Long,
    val playerInputRevision: Long,
    val draftTargetSeat: Int? = null,
    val confirmedTargetSeat: Int? = null,
    val invalidatedGeneration: Long = 0,
) {
    init {
        require(gameStateRevision >= 0 && playerInputRevision >= 0 && invalidatedGeneration >= 0)
        require(listOfNotNull(draftTargetSeat, confirmedTargetSeat).all { it > 0 })
    }

    fun selectDraft(targetSeat: Int?): FirstNightPoisonLifecycle =
        if (draftTargetSeat == targetSeat) this else copy(
            playerInputRevision = playerInputRevision + 1,
            draftTargetSeat = targetSeat,
            invalidatedGeneration = invalidatedGeneration + 1,
        )

    fun confirm(): FirstNightPoisonLifecycle =
        if (confirmedTargetSeat == draftTargetSeat) this else copy(
            gameStateRevision = gameStateRevision + 1,
            confirmedTargetSeat = draftTargetSeat,
            invalidatedGeneration = invalidatedGeneration + 1,
        )
}
