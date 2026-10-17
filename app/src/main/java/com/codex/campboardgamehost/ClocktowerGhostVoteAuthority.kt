package com.codex.campboardgamehost

import com.codex.campboardgamehost.clocktower.domain.ActionFact
import com.codex.campboardgamehost.clocktower.epistemic.ActionFactTimeline

/** Read-only table projection of ghost votes already committed by the Session. */
internal data class ClocktowerGhostVoteAuthority(
    val spentSeatIds: Set<ClocktowerSeatId> = emptySet(),
) {
    fun canVote(seatId: ClocktowerSeatId, seats: List<HostSeatPresentation>): Boolean {
        val seat = seats.firstOrNull { it.seatId == seatId } ?: return false
        return seat.isAlive || seatId !in spentSeatIds
    }

    companion object {
        fun from(timeline: ActionFactTimeline): ClocktowerGhostVoteAuthority =
            ClocktowerGhostVoteAuthority(timeline.entries
                .flatMap { (it.fact as? ActionFact.Vote)?.ghostVoterSeats.orEmpty() }
                .mapTo(mutableSetOf(), ::ClocktowerSeatId))
    }
}
