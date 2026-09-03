package com.codex.campboardgamehost

/**
 * Persistent-table presentation state for the Artist's public claimant selection.
 *
 * Artist answer truth/reliability and recommendation semantics remain owned by the existing Host
 * flow. This state only projects the already-authorized claimant candidates onto stable seats.
 */
internal data class ClocktowerArtistTableState(
    val seats: List<HostSeatPresentation>,
    val interaction: HostTableInteractionState,
    val claimantSeatId: ClocktowerSeatId?,
) {
    val claimantName: String?
        get() = claimantSeatId?.let(::playerNameForSeat)

    val hasEligibleClaimant: Boolean
        get() = interaction.selectableSeatIds.isNotEmpty()

    fun playerNameForSeat(seatId: ClocktowerSeatId): String =
        seats.single { seat -> seat.seatId == seatId }.playerName
}

internal fun clocktowerArtistTableState(
    seats: List<HostSeatPresentation>,
    claimantCandidateNames: Set<String>,
    claimantName: String?,
): ClocktowerArtistTableState {
    val seatsByName = seats.associateBy(HostSeatPresentation::playerName)
    require(seatsByName.size == seats.size) {
        "Artist table requires unique player names for the existing Host name-based action state"
    }
    val knownNames = seatsByName.keys
    require(claimantCandidateNames.all { it in knownNames }) {
        "Artist claimant candidates must reference known physical seats"
    }
    require(claimantName == null || claimantName in claimantCandidateNames) {
        "Selected Artist claimant must remain an eligible claimant"
    }

    val claimantSeatIds = claimantCandidateNames
        .mapTo(mutableSetOf()) { name -> seatsByName.getValue(name).seatId }
    val claimantSeatId = claimantName?.let { seatsByName.getValue(it).seatId }
    val allSeatIds = seats.mapTo(mutableSetOf()) { seat -> seat.seatId }

    return ClocktowerArtistTableState(
        seats = seats,
        interaction = HostTableInteractionState(
            mode = HostTableInteractionMode.Selection,
            selectableSeatIds = claimantSeatIds,
            selectedSeatIds = listOfNotNull(claimantSeatId),
            lockedSeatIds = allSeatIds - claimantSeatIds,
        ),
        claimantSeatId = claimantSeatId,
    )
}
