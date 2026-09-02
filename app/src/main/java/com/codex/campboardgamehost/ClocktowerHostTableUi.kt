package com.codex.campboardgamehost

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
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
    dragEnabled: Boolean = false,
    seatMotionKey: (HostSeatPresentation) -> String = { seat -> seat.seatId.renderKey() },
    onSeatDragCommit: (ClocktowerSeatId, Int) -> Unit = { _, _ -> },
    centerContent: @Composable BoxScope.() -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value
        val layout = remember(availableWidth, availableHeight, seats.size) {
            hostTableLayout(
                playerCount = seats.size,
                constraints = hostTableSurfaceLayoutConstraints(
                    availableWidth = availableWidth,
                    availableHeight = availableHeight,
                ),
            )
        }
        val frames = remember(seats, interaction, layout) {
            hostTableSeatFrames(
                seats = seats,
                interaction = interaction,
                layout = layout,
            )
        }
        val seatIdsByRenderKey = remember(frames) {
            frames.associate { frame -> frame.seat.seatId.renderKey() to frame.seat.seatId }
        }
        val renderSeats = frames.map { frame ->
            frame.toSquareTableSeatUiModel(
                motionKey = seatMotionKey(frame.seat),
            )
        }

        ClocktowerSquareTableSeatSurface(
            seats = renderSeats,
            modifier = Modifier.fillMaxSize(),
            interactionMode = if (interaction.mode == HostTableInteractionMode.ReadOnly) {
                ClocktowerSquareTableInteractionMode.ReadOnly
            } else {
                ClocktowerSquareTableInteractionMode.Selectable
            },
            onSeatClick = { renderKey ->
                seatIdsByRenderKey[renderKey]?.let(onSeatClick)
            },
            layout = layout,
            dragEnabled = dragEnabled,
            onSeatDragCommit = { renderKey, targetRingIndex ->
                seatIdsByRenderKey[renderKey]?.let { seatId ->
                    onSeatDragCommit(seatId, targetRingIndex)
                }
            },
            centerContent = centerContent,
        )
    }
}

private fun HostTableSeatFrame.toSquareTableSeatUiModel(
    motionKey: String,
): ClocktowerSquareTableSeatUiModel =
    ClocktowerSquareTableSeatUiModel(
        seatId = seat.seatId.renderKey(),
        seatNumber = seat.seatId.number,
        label = hostTablePrimarySeatLabel(seat),
        state = squareTableSeatState(),
        motionKey = motionKey,
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
