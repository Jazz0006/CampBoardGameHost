package com.codex.campboardgamehost

/** Persistent-table presentation state for the Klutz's public living-player choice. */
internal data class ClocktowerKlutzTableState(
    val seats: List<HostSeatPresentation>,
    val interaction: HostTableInteractionState,
    val klutzSeatId: ClocktowerSeatId?,
    val choiceSeatId: ClocktowerSeatId?,
) {
    val klutzName: String?
        get() = klutzSeatId?.let(::playerNameForSeat)

    val choiceName: String?
        get() = choiceSeatId?.let(::playerNameForSeat)

    fun playerNameForSeat(seatId: ClocktowerSeatId): String =
        seats.single { seat -> seat.seatId == seatId }.playerName
}

internal fun clocktowerKlutzTableState(
    seats: List<HostSeatPresentation>,
    klutzName: String?,
    alivePlayerNames: Set<String>,
    choiceName: String?,
): ClocktowerKlutzTableState {
    val seatsByName = seats.associateBy(HostSeatPresentation::playerName)
    require(seatsByName.size == seats.size) {
        "Klutz table requires unique player names for the existing Host name-based action state"
    }
    val knownNames = seatsByName.keys
    require(klutzName == null || klutzName in knownNames) {
        "Klutz context must reference a known physical seat"
    }
    require(alivePlayerNames.all { it in knownNames }) {
        "Klutz choice candidates must reference known physical seats"
    }

    val eligibleChoiceNames = if (klutzName == null) {
        emptySet()
    } else {
        alivePlayerNames - klutzName
    }
    require(choiceName == null || choiceName in eligibleChoiceNames) {
        "Selected Klutz choice must be a living player other than the Klutz"
    }

    val klutzSeatId = klutzName?.let { seatsByName.getValue(it).seatId }
    val choiceSeatId = choiceName?.let { seatsByName.getValue(it).seatId }
    val selectableSeatIds = eligibleChoiceNames
        .mapTo(mutableSetOf()) { name -> seatsByName.getValue(name).seatId }
    val selectedSeatIds = listOfNotNull(choiceSeatId)
    val highlightedSeatIds = setOfNotNull(klutzSeatId)
    val allSeatIds = seats.mapTo(mutableSetOf()) { seat -> seat.seatId }

    return ClocktowerKlutzTableState(
        seats = seats,
        interaction = HostTableInteractionState(
            mode = HostTableInteractionMode.Selection,
            selectableSeatIds = selectableSeatIds,
            selectedSeatIds = selectedSeatIds,
            highlightedSeatIds = highlightedSeatIds,
            currentSeatId = choiceSeatId,
            lockedSeatIds = allSeatIds - selectableSeatIds - selectedSeatIds,
        ),
        klutzSeatId = klutzSeatId,
        choiceSeatId = choiceSeatId,
    )
}
