package com.codex.campboardgamehost

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Shared Storyteller-private table shell.
 *
 * Pages provide stable seat presentation plus a bounded interaction state. The shell preserves the
 * canonical physical topology and delegates the center to the current Storyteller task.
 */
@Composable
internal fun HostTableShell(
    seats: List<HostSeatPresentation>,
    modifier: Modifier = Modifier,
    interaction: HostTableInteractionState = HostTableInteractionState(),
    onSeatClick: (ClocktowerSeatId) -> Unit = {},
    centerContent: @Composable BoxScope.() -> Unit = {},
) {
    val frames = remember(seats, interaction) {
        hostTableSeatFrames(
            seats = seats,
            interaction = interaction,
        )
    }
    val seatIdsByRenderKey = remember(frames) {
        frames.associate { frame -> frame.seat.seatId.renderKey() to frame.seat.seatId }
    }
    val renderSeats = remember(frames) {
        frames.map { frame -> frame.toSquareTableSeatUiModel() }
    }

    ClocktowerSquareTableSeatSurface(
        seats = renderSeats,
        modifier = modifier,
        interactionMode = if (interaction.mode == HostTableInteractionMode.ReadOnly) {
            ClocktowerSquareTableInteractionMode.ReadOnly
        } else {
            ClocktowerSquareTableInteractionMode.Selectable
        },
        onSeatClick = { renderKey ->
            seatIdsByRenderKey[renderKey]?.let(onSeatClick)
        },
        centerContent = centerContent,
    )
}

private fun HostTableSeatFrame.toSquareTableSeatUiModel(): ClocktowerSquareTableSeatUiModel =
    ClocktowerSquareTableSeatUiModel(
        seatId = seat.seatId.renderKey(),
        seatNumber = seat.seatId.number,
        label = hostTablePrimarySeatLabel(seat),
        state = squareTableSeatState(),
    )

private fun HostTableSeatFrame.squareTableSeatState(): ClocktowerSquareTableSeatState = when {
    isLocked -> ClocktowerSquareTableSeatState.Disabled
    selectionOrder == 1 -> ClocktowerSquareTableSeatState.SelectedFirst
    selectionOrder == 2 -> ClocktowerSquareTableSeatState.SelectedSecond
    isSelected -> ClocktowerSquareTableSeatState.SelectedFirst
    isCurrent -> ClocktowerSquareTableSeatState.HighlightedInformation
    isHighlighted -> ClocktowerSquareTableSeatState.HighlightedInformation
    isSelectable -> ClocktowerSquareTableSeatState.Selectable
    else -> ClocktowerSquareTableSeatState.Neutral
}

/**
 * Foundation density policy. Role detail stays typed on [HostSeatPresentation] and will be surfaced
 * mode-by-mode during migration instead of being re-derived from localized labels.
 */
private fun hostTablePrimarySeatLabel(seat: HostSeatPresentation): String =
    if (seat.isAlive) seat.playerName else "${seat.playerName} ☠"
