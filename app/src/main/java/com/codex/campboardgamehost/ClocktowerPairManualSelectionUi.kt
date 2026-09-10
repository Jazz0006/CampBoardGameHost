package com.codex.campboardgamehost

internal fun clocktowerPairManualSquareTableSeat(
    seat: HostSeatPresentation,
    language: String,
    state: ClocktowerSquareTableSeatState,
): ClocktowerSquareTableSeatUiModel {
    val content = hostSeatContentPresentation(seat, language)
    return ClocktowerSquareTableSeatUiModel(
        seatId = seat.seatId.renderKey(),
        seatNumber = seat.seatId.number,
        label = content.primaryLabel,
        detailLabels = content.detailLabels,
        state = state,
    )
}

internal fun clocktowerPairManualSeatState(
    selection: ClocktowerPairManualSelectionModel,
    seatNumber: Int,
): ClocktowerSquareTableSeatState {
    if (selection.isZeroCaseSelected) return ClocktowerSquareTableSeatState.Disabled

    val roleId = selection.selectedRoleId ?: return ClocktowerSquareTableSeatState.Neutral
    val first = selection.selectedFirstSeat
    val second = selection.selectedSecondSeat
    if (seatNumber == first) return ClocktowerSquareTableSeatState.SelectedFirst
    if (seatNumber == second) return ClocktowerSquareTableSeatState.SelectedSecond

    val validFirst = selection.firstSeats(roleId)
    if (first == null) {
        return if (seatNumber in validFirst) {
            ClocktowerSquareTableSeatState.Selectable
        } else {
            ClocktowerSquareTableSeatState.Disabled
        }
    }

    val validSecond = selection.secondSeats(roleId, first)
    return if (seatNumber in validSecond) {
        ClocktowerSquareTableSeatState.Selectable
    } else {
        ClocktowerSquareTableSeatState.Disabled
    }
}
