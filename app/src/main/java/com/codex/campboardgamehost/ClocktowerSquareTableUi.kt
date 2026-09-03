package com.codex.campboardgamehost

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.abs

internal enum class ClocktowerSquareTableSeatState {
    Neutral,
    Selectable,
    SelectedFirst,
    SelectedSecond,
    HighlightedInformation,
    Disabled,
}

internal enum class ClocktowerSquareTableEdge {
    Top,
    Right,
    Bottom,
    Left,
}

internal enum class ClocktowerSquareTableInteractionMode {
    ReadOnly,
    Selectable,
}

internal data class ClocktowerSquareTableSeatUiModel(
    val seatId: String,
    val seatNumber: Int,
    val label: String,
    val state: ClocktowerSquareTableSeatState = ClocktowerSquareTableSeatState.Neutral,
    val isInteractionEnabled: Boolean = state in setOf(
        ClocktowerSquareTableSeatState.Selectable,
        ClocktowerSquareTableSeatState.SelectedFirst,
        ClocktowerSquareTableSeatState.SelectedSecond,
    ),
    val motionKey: String = seatId,
)

internal data class ClocktowerSquareTableSeatPlacement(
    val seat: ClocktowerSquareTableSeatUiModel,
    val spatialSlot: HostTableSpatialSlot,
) {
    val edge: ClocktowerSquareTableEdge
        get() = spatialSlot.edge

    val indexOnEdge: Int
        get() = spatialSlot.indexOnEdge
}

internal fun clocktowerSquareTablePlacements(
    seats: List<ClocktowerSquareTableSeatUiModel>,
    layout: HostTableLayout,
): List<ClocktowerSquareTableSeatPlacement> {
    require(seats.map { it.seatId }.distinct().size == seats.size) {
        "Square-table seat identity must be unique"
    }
    require(seats.map { it.motionKey }.distinct().size == seats.size) {
        "Square-table motion identity must be unique"
    }
    require(layout.slots.size == seats.size) {
        "Square-table layout slot count must match seat count"
    }

    return seats.zip(layout.slots) { seat, spatialSlot ->
        ClocktowerSquareTableSeatPlacement(
            seat = seat,
            spatialSlot = spatialSlot,
        )
    }
}

private const val HOST_TABLE_SEAT_CARD_WIDTH = 64f
private const val HOST_TABLE_SEAT_CARD_HEIGHT = 50f
private const val HOST_TABLE_MINIMUM_SEPARATION = 4f
private const val HOST_TABLE_CENTER_WIDTH_FRACTION = 0.56f
private const val HOST_TABLE_CENTER_HEIGHT_FRACTION = 0.52f

/**
 * Current visual-density policy for the shared table surface.
 *
 * Geometry remains constraint-driven: these values describe one seat card and the preferred center
 * workspace, while edge capacity comes from [hostTableLayout]. The center is narrowed when needed
 * to preserve the requested seat/workspace clearance on smaller widths/heights.
 */
internal fun hostTableSurfaceLayoutConstraints(
    availableWidth: Float,
    availableHeight: Float,
): HostTableLayoutConstraints {
    val maximumCenterWidth = (
        availableWidth - 2f * (HOST_TABLE_SEAT_CARD_WIDTH + HOST_TABLE_MINIMUM_SEPARATION)
        ).coerceAtLeast(0f)
    val maximumCenterHeight = (
        availableHeight - 2f * (HOST_TABLE_SEAT_CARD_HEIGHT + HOST_TABLE_MINIMUM_SEPARATION)
        ).coerceAtLeast(0f)

    return HostTableLayoutConstraints(
        availableWidth = availableWidth,
        availableHeight = availableHeight,
        seatCardWidth = HOST_TABLE_SEAT_CARD_WIDTH,
        seatCardHeight = HOST_TABLE_SEAT_CARD_HEIGHT,
        minimumSafeSeparation = HOST_TABLE_MINIMUM_SEPARATION,
        centerWorkspaceWidth = minOf(
            availableWidth * HOST_TABLE_CENTER_WIDTH_FRACTION,
            maximumCenterWidth,
        ),
        centerWorkspaceHeight = minOf(
            availableHeight * HOST_TABLE_CENTER_HEIGHT_FRACTION,
            maximumCenterHeight,
        ),
    )
}

@Composable
internal fun ClocktowerSquareTableSeatSurface(
    seats: List<ClocktowerSquareTableSeatUiModel>,
    modifier: Modifier = Modifier,
    interactionMode: ClocktowerSquareTableInteractionMode = ClocktowerSquareTableInteractionMode.ReadOnly,
    onSeatClick: (String) -> Unit = {},
    layout: HostTableLayout? = null,
    dragEnabled: Boolean = false,
    onSeatDragCommit: (String, Int) -> Unit = { _, _ -> },
    centerContent: @Composable BoxScope.() -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value
        val density = LocalDensity.current
        val resolvedLayout = layout ?: remember(availableWidth, availableHeight, seats.size) {
            hostTableLayout(
                playerCount = seats.size,
                constraints = hostTableSurfaceLayoutConstraints(
                    availableWidth = availableWidth,
                    availableHeight = availableHeight,
                ),
            )
        }
        require(abs(resolvedLayout.constraints.availableWidth - availableWidth) < 0.01f) {
            "Provided square-table layout width must match rendering constraints"
        }
        require(abs(resolvedLayout.constraints.availableHeight - availableHeight) < 0.01f) {
            "Provided square-table layout height must match rendering constraints"
        }

        var draggedMotionKey by remember { mutableStateOf<String?>(null) }
        var dragTargetRingIndex by remember { mutableStateOf<Int?>(null) }
        var dragPointerPosition by remember { mutableStateOf(Offset.Zero) }

        val dragSourceIndex = draggedMotionKey?.let { motionKey ->
            seats.indexOfFirst { seat -> seat.motionKey == motionKey }.takeIf { it >= 0 }
        }
        val previewSeats = if (
            dragEnabled &&
            dragSourceIndex != null &&
            dragTargetRingIndex != null &&
            dragTargetRingIndex in seats.indices
        ) {
            reorderHostTableItems(
                items = seats,
                fromIndex = dragSourceIndex,
                targetIndex = dragTargetRingIndex!!,
            )
        } else {
            seats
        }
        val placements = remember(previewSeats, resolvedLayout) {
            clocktowerSquareTablePlacements(
                seats = previewSeats,
                layout = resolvedLayout,
            )
        }
        val seatCardWidth = resolvedLayout.constraints.seatCardWidth
        val seatCardHeight = resolvedLayout.constraints.seatCardHeight
        val tabletopGeometry = remember(resolvedLayout.constraints) {
            hostTableTabletopGeometry(resolvedLayout.constraints)
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .width(tabletopGeometry.width.dp)
                .height(tabletopGeometry.height.dp),
            shape = RoundedCornerShape(tabletopGeometry.cornerRadius.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 2.dp,
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }

        placements.forEach { placement ->
            key(placement.seat.motionKey) {
                val slot = placement.spatialSlot
                val targetTopLeft = Offset(
                    x = slot.centerX - seatCardWidth / 2f,
                    y = slot.centerY - seatCardHeight / 2f,
                )
                val animatedTopLeft by animateOffsetAsState(
                    targetValue = targetTopLeft,
                    label = "host-table-${placement.seat.motionKey}",
                )
                val isDragged = placement.seat.motionKey == draggedMotionKey
                val displayedTopLeft = if (isDragged) {
                    Offset(
                        x = dragPointerPosition.x - seatCardWidth / 2f,
                        y = dragPointerPosition.y - seatCardHeight / 2f,
                    )
                } else {
                    animatedTopLeft
                }
                val dragModifier = if (dragEnabled && seats.size > 1) {
                    Modifier.pointerInput(
                        placement.seat.motionKey,
                        resolvedLayout,
                        density.density,
                    ) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                val sourceSlot = resolvedLayout.slots.first { candidate ->
                                    candidate.ringIndex == seats.indexOfFirst { seat ->
                                        seat.motionKey == placement.seat.motionKey
                                    }
                                }
                                draggedMotionKey = placement.seat.motionKey
                                dragTargetRingIndex = sourceSlot.ringIndex
                                dragPointerPosition = Offset(sourceSlot.centerX, sourceSlot.centerY)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val nextPointer = dragPointerPosition + Offset(
                                    x = dragAmount.x / density.density,
                                    y = dragAmount.y / density.density,
                                )
                                dragPointerPosition = nextPointer
                                dragTargetRingIndex = nearestHostTableRingIndex(
                                    layout = resolvedLayout,
                                    pointerX = nextPointer.x,
                                    pointerY = nextPointer.y,
                                )
                            },
                            onDragEnd = {
                                val draggedKey = draggedMotionKey
                                val targetRingIndex = dragTargetRingIndex
                                val sourceSeat = draggedKey?.let { motionKey ->
                                    seats.firstOrNull { seat -> seat.motionKey == motionKey }
                                }
                                draggedMotionKey = null
                                dragTargetRingIndex = null
                                dragPointerPosition = Offset.Zero
                                if (sourceSeat != null && targetRingIndex != null) {
                                    onSeatDragCommit(sourceSeat.seatId, targetRingIndex)
                                }
                            },
                            onDragCancel = {
                                draggedMotionKey = null
                                dragTargetRingIndex = null
                                dragPointerPosition = Offset.Zero
                            },
                        )
                    }
                } else {
                    Modifier
                }

                ClocktowerSquareTableSeat(
                    seat = placement.seat,
                    interactionMode = interactionMode,
                    onSeatClick = onSeatClick,
                    modifier = Modifier
                        .offset(
                            x = displayedTopLeft.x.dp,
                            y = displayedTopLeft.y.dp,
                        )
                        .size(
                            width = seatCardWidth.dp,
                            height = seatCardHeight.dp,
                        )
                        .zIndex(if (isDragged) 1f else 0f)
                        .then(dragModifier),
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .width(resolvedLayout.constraints.centerWorkspaceWidth.dp)
                .height(resolvedLayout.constraints.centerWorkspaceHeight.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            tonalElevation = 1.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
                content = centerContent,
            )
        }
    }
}

@Composable
private fun ClocktowerSquareTableSeat(
    seat: ClocktowerSquareTableSeatUiModel,
    interactionMode: ClocktowerSquareTableInteractionMode,
    onSeatClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSelect = interactionMode == ClocktowerSquareTableInteractionMode.Selectable &&
        seat.isInteractionEnabled
    val palette = clocktowerSquareTableSeatPalette(seat.state)
    val language = LocalContext.current.resources.configuration.locales[0].language
    val clickModifier = if (canSelect) {
        Modifier.clickable { onSeatClick(seat.seatId) }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp, max = 62.dp)
            .then(clickModifier),
        shape = RoundedCornerShape(12.dp),
        color = palette.container,
        contentColor = palette.content,
        border = BorderStroke(palette.borderWidth, palette.border),
        tonalElevation = if (canSelect && seat.state != ClocktowerSquareTableSeatState.Neutral) 2.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                clocktowerSquareTableStateMarker(seat.state)?.let { marker ->
                    Text(
                        text = marker,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(end = 1.dp),
                    )
                }
                Text(
                    text = clocktowerSeatNumberLabel(seat.seatNumber, language),
                    fontSize = 15.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Text(
                text = seat.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = if (seat.state in setOf(
                        ClocktowerSquareTableSeatState.SelectedFirst,
                        ClocktowerSquareTableSeatState.SelectedSecond,
                        ClocktowerSquareTableSeatState.HighlightedInformation,
                    )
                ) {
                    FontWeight.Black
                } else {
                    FontWeight.SemiBold
                },
            )
        }
    }
}

private data class ClocktowerSquareTableSeatPalette(
    val container: Color,
    val content: Color,
    val border: Color,
    val borderWidth: androidx.compose.ui.unit.Dp,
)

@Composable
private fun clocktowerSquareTableSeatPalette(
    state: ClocktowerSquareTableSeatState,
): ClocktowerSquareTableSeatPalette {
    val colors = MaterialTheme.colorScheme
    return when (state) {
        ClocktowerSquareTableSeatState.Neutral -> ClocktowerSquareTableSeatPalette(
            container = colors.surfaceVariant,
            content = colors.onSurfaceVariant,
            border = colors.outline,
            borderWidth = 1.5.dp,
        )
        ClocktowerSquareTableSeatState.Selectable -> ClocktowerSquareTableSeatPalette(
            container = colors.surface,
            content = colors.onSurface,
            border = colors.primary,
            borderWidth = 2.dp,
        )
        ClocktowerSquareTableSeatState.SelectedFirst -> ClocktowerSquareTableSeatPalette(
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
            border = colors.primary,
            borderWidth = 3.dp,
        )
        ClocktowerSquareTableSeatState.SelectedSecond -> ClocktowerSquareTableSeatPalette(
            container = colors.secondaryContainer,
            content = colors.onSecondaryContainer,
            border = colors.secondary,
            borderWidth = 3.dp,
        )
        ClocktowerSquareTableSeatState.HighlightedInformation -> ClocktowerSquareTableSeatPalette(
            container = colors.tertiaryContainer,
            content = colors.onTertiaryContainer,
            border = colors.tertiary,
            borderWidth = 3.dp,
        )
        ClocktowerSquareTableSeatState.Disabled -> ClocktowerSquareTableSeatPalette(
            container = colors.surfaceVariant.copy(alpha = 0.55f),
            content = colors.onSurfaceVariant.copy(alpha = 0.72f),
            border = colors.outline.copy(alpha = 0.72f),
            borderWidth = 1.5.dp,
        )
    }
}

private fun clocktowerSquareTableStateMarker(
    state: ClocktowerSquareTableSeatState,
): String? = when (state) {
    ClocktowerSquareTableSeatState.Neutral -> null
    ClocktowerSquareTableSeatState.Selectable -> "○"
    ClocktowerSquareTableSeatState.SelectedFirst -> "①"
    ClocktowerSquareTableSeatState.SelectedSecond -> "②"
    ClocktowerSquareTableSeatState.HighlightedInformation -> "★"
    ClocktowerSquareTableSeatState.Disabled -> "×"
}
