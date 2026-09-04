package com.codex.campboardgamehost

/** Immutable voter snapshot captured at the confirmed-vote boundary for durable game history. */
internal data class ClocktowerConfirmedVoter(
    val seatId: ClocktowerSeatId,
    val playerName: String,
    val isGhostVote: Boolean,
)

internal data class ClocktowerConfirmedVoteRecord(
    val voters: List<ClocktowerConfirmedVoter>,
) {
    val voteCount: Int
        get() = voters.size

    fun voterDetail(
        playerLabel: (String) -> String,
        ghostVoteSuffix: String,
        noVotesLabel: String,
    ): String {
        if (voters.isEmpty()) return noVotesLabel
        return voters.joinToString(separator = "、") { voter ->
            buildString {
                append(playerLabel(voter.playerName))
                if (voter.isGhostVote) append(ghostVoteSuffix)
            }
        }
    }
}

internal fun ClocktowerTableVoteState.confirmedVoteRecord(): ClocktowerConfirmedVoteRecord {
    val seatsById = seats.associateBy(HostSeatPresentation::seatId)
    val voters = orderedSeatIds
        .asSequence()
        .filter(selectedVoterSeatIds::contains)
        .map { seatId ->
            val seat = seatsById.getValue(seatId)
            ClocktowerConfirmedVoter(
                seatId = seatId,
                playerName = seat.playerName,
                isGhostVote = !seat.isAlive,
            )
        }
        .toList()
    return ClocktowerConfirmedVoteRecord(voters)
}
