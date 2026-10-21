package com.codex.campboardgamehost

/** Public claimant and target mapped onto physical seats; no Host ability eligibility menu. */
internal data class ClocktowerSlayerTableState(
    val seats: List<HostSeatPresentation>,
    val interaction: HostTableInteractionState,
    val claimantSeatId: ClocktowerSeatId?,
    val targetSeatId: ClocktowerSeatId?,
) {
    val claimantName: String?
        get() = claimantSeatId?.let(::playerNameForSeat)

    val targetName: String?
        get() = targetSeatId?.let(::playerNameForSeat)

    val choosingClaimant: Boolean
        get() = claimantSeatId == null

    val hasEligibleClaimant: Boolean
        get() = choosingClaimant && interaction.selectableSeatIds.isNotEmpty()

    fun playerNameForSeat(seatId: ClocktowerSeatId): String =
        seats.single { seat -> seat.seatId == seatId }.playerName
}

internal fun clocktowerSlayerTableState(
    seats: List<HostSeatPresentation>,
    claimantName: String?,
    targetName: String?,
): ClocktowerSlayerTableState {
    val seatsByName = seats.associateBy(HostSeatPresentation::playerName)
    require(seatsByName.size == seats.size) {
        "Slayer table requires unique player names for the existing Host name-based action state"
    }
    val knownNames = seatsByName.keys
    require(claimantName == null || claimantName in knownNames) {
        "Slayer claimant must reference an existing seat"
    }
    require(targetName == null ||
        (targetName in knownNames && claimantName != null && targetName != claimantName)) {
        "Slayer target must be another existing seat after the claimant"
    }

    val claimantSeatId = claimantName?.let { seatsByName.getValue(it).seatId }
    val targetCandidateNames = if (claimantName == null) emptySet() else knownNames - claimantName
    val targetSeatId = targetName?.let { seatsByName.getValue(it).seatId }

    val selectableSeatIds = (if (claimantSeatId == null) knownNames else targetCandidateNames)
        .mapTo(mutableSetOf()) { name -> seatsByName.getValue(name).seatId }
    val selectedSeatIds = buildList {
        claimantSeatId?.let(::add)
        targetSeatId?.let(::add)
    }
    val allSeatIds = seats.mapTo(mutableSetOf()) { seat -> seat.seatId }
    val unlockedSeatIds = selectableSeatIds + selectedSeatIds

    return ClocktowerSlayerTableState(
        seats = seats,
        interaction = HostTableInteractionState(
            mode = if (claimantSeatId == null) {
                HostTableInteractionMode.Selection
            } else {
                HostTableInteractionMode.OrderedSelection
            },
            selectableSeatIds = selectableSeatIds,
            selectedSeatIds = selectedSeatIds,
            currentSeatId = claimantSeatId,
            lockedSeatIds = allSeatIds - unlockedSeatIds,
        ),
        claimantSeatId = claimantSeatId,
        targetSeatId = targetSeatId,
    )
}
