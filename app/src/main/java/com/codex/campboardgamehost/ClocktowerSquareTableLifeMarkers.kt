package com.codex.campboardgamehost

internal data class ClocktowerSquareTableLifeMarkers(
    val showDeathCross: Boolean,
    val showUnspentGhostVote: Boolean,
)

internal fun clocktowerSquareTableLifeMarkers(
    isAlive: Boolean,
    hasUnspentGhostVote: Boolean,
): ClocktowerSquareTableLifeMarkers = ClocktowerSquareTableLifeMarkers(
    showDeathCross = !isAlive,
    showUnspentGhostVote = !isAlive && hasUnspentGhostVote,
)
