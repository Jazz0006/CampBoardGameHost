package com.codex.campboardgamehost

internal data class ClocktowerSquareTableLifeMarkers(
    val showDeathMark: Boolean,
    val showUnspentGhostVote: Boolean,
)

internal fun clocktowerSquareTableLifeMarkers(
    isAlive: Boolean,
    hasUnspentGhostVote: Boolean,
): ClocktowerSquareTableLifeMarkers = ClocktowerSquareTableLifeMarkers(
    showDeathMark = !isAlive,
    showUnspentGhostVote = !isAlive && hasUnspentGhostVote,
)
