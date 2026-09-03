package com.codex.campboardgamehost

internal data class ClocktowerTableVoteState(
    val seats: List<HostSeatPresentation>,
    val nomineeSeatId: ClocktowerSeatId,
    val orderedSeatIds: List<ClocktowerSeatId>,
    val selectableSeatIds: Set<ClocktowerSeatId>,
    val selectedVoterSeatIds: Set<ClocktowerSeatId>,
    val interaction: HostTableInteractionState,
) {
    val voteCount: Int
        get() = selectedVoterSeatIds.size

    fun togglePendingVoter(seatId: ClocktowerSeatId): ClocktowerTableVoteState {
        if (seatId !in selectableSeatIds) return this
        val nextSelected = if (seatId in selectedVoterSeatIds) {
            selectedVoterSeatIds - seatId
        } else {
            selectedVoterSeatIds + seatId
        }
        return clocktowerTableVoteState(
            seats = seats,
            nomineeSeatId = nomineeSeatId,
            selectedVoterSeatIds = nextSelected,
            ghostVoteAuthority = ClocktowerGhostVoteAuthority(
                spentSeatIds = seats
                    .asSequence()
                    .filter { seat -> !seat.isAlive && seat.seatId !in selectableSeatIds }
                    .map(HostSeatPresentation::seatId)
                    .toSet(),
            ),
        )
    }
}

internal fun clocktowerTableVoteState(
    seats: List<HostSeatPresentation>,
    nomineeSeatId: ClocktowerSeatId,
    selectedVoterSeatIds: Set<ClocktowerSeatId> = emptySet(),
    ghostVoteAuthority: ClocktowerGhostVoteAuthority = ClocktowerGhostVoteAuthority(),
): ClocktowerTableVoteState {
    require(seats.isNotEmpty()) { "Table vote requires at least one physical seat" }
    val canonicalSeats = seats.sortedBy { seat -> seat.seatId.number }
    val expectedSeatIds = (1..canonicalSeats.size).map(::ClocktowerSeatId)
    require(canonicalSeats.map(HostSeatPresentation::seatId) == expectedSeatIds) {
        "Table vote requires unique contiguous physical seats from seat 1"
    }
    require(nomineeSeatId in expectedSeatIds) {
        "Table vote nominee must belong to the physical table"
    }

    val nomineeIndex = expectedSeatIds.indexOf(nomineeSeatId)
    val orderedSeatIds = expectedSeatIds.drop(nomineeIndex + 1) +
        expectedSeatIds.take(nomineeIndex + 1)
    val selectableSeatIds = canonicalSeats
        .asSequence()
        .filter { seat -> ghostVoteAuthority.canVote(seat.seatId, canonicalSeats) }
        .map(HostSeatPresentation::seatId)
        .toSet()
    require(selectedVoterSeatIds.all { seatId -> seatId in selectableSeatIds }) {
        "Pending voter selection must contain only currently selectable seats"
    }
    val orderedSelected = orderedSeatIds.filter { seatId -> seatId in selectedVoterSeatIds }

    return ClocktowerTableVoteState(
        seats = canonicalSeats,
        nomineeSeatId = nomineeSeatId,
        orderedSeatIds = orderedSeatIds,
        selectableSeatIds = selectableSeatIds,
        selectedVoterSeatIds = selectedVoterSeatIds,
        interaction = HostTableInteractionState(
            mode = HostTableInteractionMode.MultiSelection,
            selectableSeatIds = selectableSeatIds,
            selectedSeatIds = orderedSelected,
            highlightedSeatIds = setOf(nomineeSeatId),
            lockedSeatIds = expectedSeatIds.toSet() - selectableSeatIds,
        ),
    )
}
