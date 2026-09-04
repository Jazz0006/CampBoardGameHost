package com.codex.campboardgamehost

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal enum class ClocktowerSquareTableSeatState {
    Neutral,
    Selectable,
    SelectedFirst,
    SelectedSecond,
    Selected,
    SelectedHighlighted,
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
    val detailLabels: List<String> = emptyList(),
    val state: ClocktowerSquareTableSeatState = ClocktowerSquareTableSeatState.Neutral,
    val isInteractionEnabled: Boolean = state in setOf(
        ClocktowerSquareTableSeatState.Selectable,
        ClocktowerSquareTableSeatState.SelectedFirst,
        ClocktowerSquareTableSeatState.SelectedSecond,
        ClocktowerSquareTableSeatState.Selected,
        ClocktowerSquareTableSeatState.SelectedHighlighted,
    ),
    val motionKey: String = seatId,
    val badge: String? = null,
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
    detailedSeatCards: Boolean = false,
    playerCount: Int = 15,
): HostTableLayoutConstraints {
    val seatDensity = clocktowerSquareTableSeatDensity(
        playerCount = playerCount,
        detailedSeatCards = detailedSeatCards,
    )
    val maximumCenterWidth = (
        availableWidth - 2f * (seatDensity.cardWidth + HOST_TABLE_MINIMUM_SEPARATION)
        ).coerceAtLeast(0f)
    val maximumCenterHeight = (
        availableHeight - 2f * (seatDensity.cardHeight + HOST_TABLE_MINIMUM_SEPARATION)
        ).coerceAtLeast(0f)

    return HostTableLayoutConstraints(
        availableWidth = availableWidth,
        availableHeight = availableHeight,
        seatCardWidth = seatDensity.cardWidth,
        seatCardHeight = seatDensity.cardHeight,
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
    directionalGestureSourceSeatIds: Set<String> = emptySet(),
    directionalGestureTargetSeatIds: Set<String> = emptySet(),
    directionalLink: Pair<String, String>? = null,
    onDirectionalGestureCommit: (String, String) -> Unit = { _, _ -> },
    centerContent: @Composable BoxScope.() -> Unit = {},
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value
        val density = LocalDensity.current
        val detailedSeatCards = seats.any { seat -> seat.detailLabels.isNotEmpty() }
        val resolvedLayout = layout ?: remember(
            availableWidth,
            availableHeight,
            seats.size,
            detailedSeatCards,
        ) {
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
        require(abs(resolvedLayout.constraints.availableWidth - availableWidth) < 0.01f) {
            "Provided square-table layout width must match rendering constraints"
        }
        require(abs(resolvedLayout.constraints.availableHeight - availableHeight) < 0.01f) {
            "Provided square-table layout height must match rendering constraints"
        }
        require(!dragEnabled || directionalGestureSourceSeatIds.isEmpty()) {
            "Reorder drag and directional gesture cannot own the same square-table surface"
        }
        val knownSeatIds = seats.map { seat -> seat.seatId }.toSet()
        require((directionalGestureSourceSeatIds + directionalGestureTargetSeatIds).all { it in knownSeatIds }) {
            "Directional square-table gesture references an unknown seat"
        }
        directionalLink?.let { (sourceSeatId, targetSeatId) ->
            require(sourceSeatId in knownSeatIds && targetSeatId in knownSeatIds && sourceSeatId != targetSeatId) {
                "Directional square-table link must reference two distinct known seats"
            }
        }

        var draggedMotionKey by remember { mutableStateOf<String?>(null) }
        var dragTargetRingIndex by remember { mutableStateOf<Int?>(null) }
        var dragPointerPosition by remember { mutableStateOf(Offset.Zero) }
        var directionalDragSourceSeatId by remember { mutableStateOf<String?>(null) }
        var directionalDragTargetRingIndex by remember { mutableStateOf<Int?>(null) }
        var directionalDragPointerPosition by remember { mutableStateOf(Offset.Zero) }

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
        val seatDensity = clocktowerSquareTableSeatDensity(
            playerCount = seats.size,
            detailedSeatCards = detailedSeatCards,
        )
        val tabletopGeometry = remember(resolvedLayout.constraints) {
            hostTableTabletopGeometry(resolvedLayout.constraints)
        }
        val directionalTargetRingIndices = directionalGestureTargetSeatIds.mapNotNullTo(mutableSetOf()) { seatId ->
            seats.indexOfFirst { seat -> seat.seatId == seatId }.takeIf { it >= 0 }
        }
        val activeDirectionalSourceRingIndex = directionalDragSourceSeatId?.let { sourceSeatId ->
            seats.indexOfFirst { seat -> seat.seatId == sourceSeatId }.takeIf { it >= 0 }
        }
        val persistentDirectionalSourceRingIndex = directionalLink?.first?.let { sourceSeatId ->
            seats.indexOfFirst { seat -> seat.seatId == sourceSeatId }.takeIf { it >= 0 }
        }
        val persistentDirectionalTargetRingIndex = directionalLink?.second?.let { targetSeatId ->
            seats.indexOfFirst { seat -> seat.seatId == targetSeatId }.takeIf { it >= 0 }
        }
        val arrowStartDp = activeDirectionalSourceRingIndex
            ?.let { ringIndex -> resolvedLayout.slots[ringIndex] }
            ?.let { slot -> Offset(slot.centerX, slot.centerY) }
            ?: persistentDirectionalSourceRingIndex
                ?.let { ringIndex -> resolvedLayout.slots[ringIndex] }
                ?.let { slot -> Offset(slot.centerX, slot.centerY) }
        val arrowEndDp = if (activeDirectionalSourceRingIndex != null) {
            directionalDragPointerPosition
        } else {
            persistentDirectionalTargetRingIndex
                ?.let { ringIndex -> resolvedLayout.slots[ringIndex] }
                ?.let { slot -> Offset(slot.centerX, slot.centerY) }
        }
        val arrowIsValid = if (activeDirectionalSourceRingIndex != null) {
            directionalDragTargetRingIndex != null
        } else {
            directionalLink != null
        }
        val arrowColor = if (arrowIsValid) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline
        }
        val densityScale = density.density

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

        if (arrowStartDp != null && arrowEndDp != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0.5f),
            ) {
                val start = Offset(arrowStartDp.x * densityScale, arrowStartDp.y * densityScale)
                val end = Offset(arrowEndDp.x * densityScale, arrowEndDp.y * densityScale)
                val deltaX = end.x - start.x
                val deltaY = end.y - start.y
                if (deltaX * deltaX + deltaY * deltaY > 4f) {
                    val strokeWidth = 4.dp.toPx()
                    drawLine(
                        color = arrowColor,
                        start = start,
                        end = end,
                        strokeWidth = strokeWidth,
                    )
                    val angle = atan2(deltaY.toDouble(), deltaX.toDouble())
                    val headLength = 12.dp.toPx()
                    val headSpread = 0.55
                    val firstHead = Offset(
                        x = (end.x - headLength * cos(angle - headSpread)).toFloat(),
                        y = (end.y - headLength * sin(angle - headSpread)).toFloat(),
                    )
                    val secondHead = Offset(
                        x = (end.x - headLength * cos(angle + headSpread)).toFloat(),
                        y = (end.y - headLength * sin(angle + headSpread)).toFloat(),
                    )
                    drawLine(arrowColor, end, firstHead, strokeWidth)
                    drawLine(arrowColor, end, secondHead, strokeWidth)
                }
            }
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
                val directionalDragModifier = if (
                    !dragEnabled &&
                    placement.seat.seatId in directionalGestureSourceSeatIds &&
                    directionalGestureTargetSeatIds.any { targetSeatId -> targetSeatId != placement.seat.seatId }
                ) {
                    Modifier.pointerInput(
                        placement.seat.seatId,
                        resolvedLayout,
                        directionalGestureSourceSeatIds,
                        directionalGestureTargetSeatIds,
                        densityScale,
                    ) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                val sourceRingIndex = seats.indexOfFirst { seat ->
                                    seat.seatId == placement.seat.seatId
                                }
                                if (sourceRingIndex >= 0) {
                                    val sourceSlot = resolvedLayout.slots[sourceRingIndex]
                                    directionalDragSourceSeatId = placement.seat.seatId
                                    directionalDragTargetRingIndex = null
                                    directionalDragPointerPosition = Offset(sourceSlot.centerX, sourceSlot.centerY)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val sourceSeatId = directionalDragSourceSeatId
                                val sourceRingIndex = sourceSeatId?.let { id ->
                                    seats.indexOfFirst { seat -> seat.seatId == id }.takeIf { it >= 0 }
                                }
                                if (sourceRingIndex != null) {
                                    val nextPointer = directionalDragPointerPosition + Offset(
                                        x = dragAmount.x / densityScale,
                                        y = dragAmount.y / densityScale,
                                    )
                                    directionalDragPointerPosition = nextPointer
                                    directionalDragTargetRingIndex = resolveHostTableDirectionalTargetRingIndex(
                                        layout = resolvedLayout,
                                        pointerX = nextPointer.x,
                                        pointerY = nextPointer.y,
                                        sourceRingIndex = sourceRingIndex,
                                        eligibleTargetRingIndices = directionalTargetRingIndices,
                                    )
                                }
                            },
                            onDragEnd = {
                                val sourceSeatId = directionalDragSourceSeatId
                                val targetSeatId = directionalDragTargetRingIndex
                                    ?.let { ringIndex -> seats.getOrNull(ringIndex)?.seatId }
                                directionalDragSourceSeatId = null
                                directionalDragTargetRingIndex = null
                                directionalDragPointerPosition = Offset.Zero
                                if (sourceSeatId != null && targetSeatId != null && sourceSeatId != targetSeatId) {
                                    onDirectionalGestureCommit(sourceSeatId, targetSeatId)
                                }
                            },
                            onDragCancel = {
                                directionalDragSourceSeatId = null
                                directionalDragTargetRingIndex = null
                                directionalDragPointerPosition = Offset.Zero
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
                    density = seatDensity,
                    modifier = Modifier
                        .offset(
                            x = displayedTopLeft.x.dp,
                            y = displayedTopLeft.y.dp,
                        )
                        .size(
                            width = seatCardWidth.dp,
                            height = seatCardHeight.dp,
                        )
                        .zIndex(if (isDragged) 2f else 1f)
                        .then(dragModifier)
                        .then(directionalDragModifier),
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .width(resolvedLayout.constraints.centerWorkspaceWidth.dp)
                .height(resolvedLayout.constraints.centerWorkspaceHeight.dp)
                .zIndex(0.25f),
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
    density: ClocktowerSquareTableSeatDensity,
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
            .heightIn(min = 48.dp, max = density.cardHeight.dp)
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
                .padding(
                    horizontal = density.horizontalPaddingDp.dp,
                    vertical = density.verticalPaddingDp.dp,
                ),
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
                ClocktowerSeatNumberBadge(
                    seatNumber = seat.seatNumber,
                    languageCode = language,
                    scale = ClocktowerSeatNumberBadgeScale.Compact,
                    contentColor = palette.content,
                    containerColor = palette.content.copy(alpha = 0.08f),
                    borderColor = palette.content.copy(alpha = 0.45f),
                )
                seat.badge?.let { badge ->
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(start = 2.dp),
                    )
                }
            }
            Text(
                text = seat.label,
                maxLines = density.primaryMaxLines,
                overflow = TextOverflow.Ellipsis,
                fontSize = density.primaryFontSizeSp.sp,
                lineHeight = density.primaryLineHeightSp.sp,
                fontWeight = if (seat.state in setOf(
                        ClocktowerSquareTableSeatState.SelectedFirst,
                        ClocktowerSquareTableSeatState.SelectedSecond,
                        ClocktowerSquareTableSeatState.Selected,
                        ClocktowerSquareTableSeatState.SelectedHighlighted,
                        ClocktowerSquareTableSeatState.HighlightedInformation,
                    )
                ) {
                    FontWeight.Black
                } else {
                    FontWeight.SemiBold
                },
            )
            seat.detailLabels.forEach { detail ->
                Text(
                    text = detail,
                    maxLines = density.detailMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = density.detailFontSizeSp.sp,
                    lineHeight = density.detailLineHeightSp.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
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
        ClocktowerSquareTableSeatState.Selected -> ClocktowerSquareTableSeatPalette(
            container = colors.primaryContainer,
            content = colors.onPrimaryContainer,
            border = colors.primary,
            borderWidth = 3.dp,
        )
        ClocktowerSquareTableSeatState.SelectedHighlighted -> ClocktowerSquareTableSeatPalette(
            container = colors.tertiaryContainer,
            content = colors.onTertiaryContainer,
            border = colors.primary,
            borderWidth = 3.5.dp,
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
    ClocktowerSquareTableSeatState.Selected -> "✓"
    ClocktowerSquareTableSeatState.SelectedHighlighted -> "✓★"
    ClocktowerSquareTableSeatState.HighlightedInformation -> "★"
    ClocktowerSquareTableSeatState.Disabled -> "×"
}
