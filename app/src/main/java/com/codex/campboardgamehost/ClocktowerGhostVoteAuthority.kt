package com.codex.campboardgamehost

import org.json.JSONArray
import org.json.JSONObject

/** Durable Storyteller authority for the one post-death vote owned by each physical seat. */
internal data class ClocktowerGhostVoteAuthority(
    val spentSeatIds: Set<ClocktowerSeatId> = emptySet(),
) {
    fun canVote(
        seatId: ClocktowerSeatId,
        seats: List<HostSeatPresentation>,
    ): Boolean {
        val seat = seats.firstOrNull { candidate -> candidate.seatId == seatId } ?: return false
        return seat.isAlive || seatId !in spentSeatIds
    }

    /**
     * Commits ghost-vote consumption only for dead seats in one confirmed vote.
     * Pending selection never calls this boundary, and confirming twice is idempotent.
     */
    fun confirmVote(
        selectedVoterSeatIds: Set<ClocktowerSeatId>,
        seats: List<HostSeatPresentation>,
    ): ClocktowerGhostVoteAuthority {
        val seatsById = seats.associateBy(HostSeatPresentation::seatId)
        require(selectedVoterSeatIds.all(seatsById::containsKey)) {
            "Confirmed voters must belong to the physical table"
        }
        val newlySpent = selectedVoterSeatIds.filterTo(mutableSetOf()) { seatId ->
            seatsById.getValue(seatId).isAlive.not()
        }
        if (newlySpent.isEmpty()) return this
        return copy(spentSeatIds = spentSeatIds + newlySpent)
    }
}

/** Additive active-game field; missing legacy payloads deliberately restore as unspent. */
internal object ClocktowerGhostVoteAuthorityPersistence {
    const val ROOT_KEY: String = "clocktowerSpentGhostVoteSeatIds"

    fun encode(authority: ClocktowerGhostVoteAuthority): JSONArray = JSONArray().apply {
        authority.spentSeatIds
            .sortedBy(ClocktowerSeatId::number)
            .forEach { seatId -> put(seatId.number) }
    }

    fun decode(json: JSONObject): ClocktowerGhostVoteAuthority {
        val payload = json.optJSONArray(ROOT_KEY) ?: return ClocktowerGhostVoteAuthority()
        val spentSeatIds = buildSet {
            for (index in 0 until payload.length()) {
                val number = payload.optInt(index, 0)
                if (number > 0) add(ClocktowerSeatId(number))
            }
        }
        return ClocktowerGhostVoteAuthority(spentSeatIds)
    }
}
