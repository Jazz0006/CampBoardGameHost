package com.codex.campboardgamehost

internal fun hostSeatHasUnspentGhostVote(
    seatId: ClocktowerSeatId,
    isAlive: Boolean,
    ghostVoteAuthority: ClocktowerGhostVoteAuthority?,
): Boolean =
    !isAlive &&
        ghostVoteAuthority != null &&
        seatId !in ghostVoteAuthority.spentSeatIds
