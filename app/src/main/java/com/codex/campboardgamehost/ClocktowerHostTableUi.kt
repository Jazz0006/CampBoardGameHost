package com.codex.campboardgamehost

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

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
    neutralSelectionChrome: Boolean = false,
    seatBadge: (HostSeatPresentation) -> String? = { null },
    seatMotionKey: (HostSeatPresentation) -> String = { seat -> seat.seatId.renderKey() },
    onSeatDragCommit: (ClocktowerSeatId, Int) -> Unit = { _, _ -> },
    directionalGesture: HostTableDirectionalGesturePolicy? = null,
    directionalLink: HostTableDirectionalLink? = null,
    onDirectionalGestureCommit: (ClocktowerSeatId, ClocktowerSeatId) -> Unit = { _, _ -> },
    centerContent: @Composable BoxScope.() -> Unit = {},
) {
    BoxWithConstraints(modifier = modifier) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value
        val language = LocalContext.current.resources.configuration.locales[0].language
        val detailedSeatCards = seats.any { seat ->
            hostSeatContentPresentation(seat, language).detailLabels.isNotEmpty()
        }
        val layout = remember(availableWidth, availableHeight, seats.size, detailedSeatCards) {
            hostTableLayout(
                playerCount = seats.size,
                constraints = hostTableSurfaceLayoutConstraints(
                    availableWidth = availableWidth,
                    availableHeight = availableHeight,
                    detailedSeatCards = detailedSeatCards,
                    playerCount = seats.size,
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
                neutralSelectionChrome = neutralSelectionChrome,
                interactionMode = interaction.mode,
                badge = seatBadge(frame.seat),
                language = language,
            )
        }
        val knownSeatIds = frames.map { frame -> frame.seat.seatId }.toSet()
        directionalGesture?.let { gesture ->
            require((gesture.sourceSeatIds + gesture.targetSeatIds).all { it in knownSeatIds }) {
                "Directional Host-table gesture references an unknown physical seat"
            }
        }
        directionalLink?.let { link ->
            require(link.sourceSeatId in knownSeatIds && link.targetSeatId in knownSeatIds) {
                "Directional Host-table link references an unknown physical seat"
            }
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
            directionalGestureSourceSeatIds = directionalGesture
                ?.sourceSeatIds
                ?.mapTo(mutableSetOf()) { seatId -> seatId.renderKey() }
                .orEmpty(),
            directionalGestureTargetSeatIds = directionalGesture
                ?.targetSeatIds
                ?.mapTo(mutableSetOf()) { seatId -> seatId.renderKey() }
                .orEmpty(),
            directionalLink = directionalLink?.let { link ->
                link.sourceSeatId.renderKey() to link.targetSeatId.renderKey()
            },
            onDirectionalGestureCommit = { sourceRenderKey, targetRenderKey ->
                val sourceSeatId = seatIdsByRenderKey[sourceRenderKey]
                val targetSeatId = seatIdsByRenderKey[targetRenderKey]
                if (sourceSeatId != null && targetSeatId != null) {
                    onDirectionalGestureCommit(sourceSeatId, targetSeatId)
                }
            },
            centerContent = centerContent,
        )
    }
}

private fun HostTableSeatFrame.toSquareTableSeatUiModel(
    motionKey: String,
    neutralSelectionChrome: Boolean,
    interactionMode: HostTableInteractionMode,
    badge: String?,
    language: String,
): ClocktowerSquareTableSeatUiModel {
    val content = hostSeatContentPresentation(seat, language)
    return ClocktowerSquareTableSeatUiModel(
        seatId = seat.seatId.renderKey(),
        seatNumber = seat.seatId.number,
        label = content.primaryLabel,
        detailLabels = content.detailLabels,
        state = if (neutralSelectionChrome) {
            ClocktowerSquareTableSeatState.Neutral
        } else {
            squareTableSeatState(interactionMode)
        },
        isInteractionEnabled = isSelectable && !isLocked,
        motionKey = motionKey,
        badge = badge,
    )
}

private fun HostTableSeatFrame.squareTableSeatState(
    interactionMode: HostTableInteractionMode,
): ClocktowerSquareTableSeatState = when {
    isLocked -> ClocktowerSquareTableSeatState.Disabled
    interactionMode == HostTableInteractionMode.MultiSelection && isSelected && isHighlighted ->
        ClocktowerSquareTableSeatState.SelectedHighlighted
    interactionMode == HostTableInteractionMode.MultiSelection && isSelected ->
        ClocktowerSquareTableSeatState.Selected
    interactionMode == HostTableInteractionMode.MultiSelection && isHighlighted ->
        ClocktowerSquareTableSeatState.HighlightedInformation
    selectionOrder == 1 -> ClocktowerSquareTableSeatState.SelectedFirst
    selectionOrder == 2 -> ClocktowerSquareTableSeatState.SelectedSecond
    isSelected -> ClocktowerSquareTableSeatState.SelectedFirst
    isCurrent -> ClocktowerSquareTableSeatState.HighlightedInformation
    isHighlighted -> ClocktowerSquareTableSeatState.HighlightedInformation
    isSelectable -> ClocktowerSquareTableSeatState.Selectable
    else -> ClocktowerSquareTableSeatState.Neutral
}
