package com.codex.campboardgamehost

/**
 * Persistent-table presentation state for the Slayer's public claimant -> target flow.
 *
 * The domain continues to own whether a Slayer shot is effective. This state only projects the
 * already-authorized claimant/target candidates onto stable physical seat identity.
 */
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
    claimantCandidateNames: Set<String>,
    alivePlayerNames: Set<String>,
    claimantName: String?,
    targetName: String?,
): ClocktowerSlayerTableState {
    val seatsByName = seats.associateBy(HostSeatPresentation::playerName)
    require(seatsByName.size == seats.size) {
        "Slayer table requires unique player names for the existing Host name-based action state"
    }
    val knownNames = seatsByName.keys
    require(claimantCandidateNames.all { it in knownNames }) {
        "Slayer claimant candidates must reference known physical seats"
    }
    require(alivePlayerNames.all { it in knownNames }) {
        "Slayer target candidates must reference known physical seats"
    }
    require(claimantName == null || claimantName in claimantCandidateNames) {
        "Selected Slayer claimant must remain an eligible claimant"
    }

    val claimantSeatId = claimantName?.let { seatsByName.getValue(it).seatId }
    val targetCandidateNames = if (claimantName == null) {
        emptySet()
    } else {
        alivePlayerNames - claimantName
    }
    require(targetName == null || targetName in targetCandidateNames) {
        "Selected Slayer target must be a living player other than the claimant"
    }
    val targetSeatId = targetName?.let { seatsByName.getValue(it).seatId }

    val selectableSeatIds = if (claimantSeatId == null) {
        claimantCandidateNames.mapTo(mutableSetOf()) { name -> seatsByName.getValue(name).seatId }
    } else {
        targetCandidateNames.mapTo(mutableSetOf()) { name -> seatsByName.getValue(name).seatId }
    }
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
