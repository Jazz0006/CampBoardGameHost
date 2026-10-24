package com.codex.campboardgamehost

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.abs

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

internal enum class ClocktowerSquareTableBadgeTone {
    Default,
    Warning,
}

internal data class ClocktowerSquareTableSeatUiModel(
    val seatId: String,
    val seatNumber: Int,
    val label: String,
    val detailLabels: List<String> = emptyList(),
    // Temporary interaction cues never replace the permanent alignment / life / impairment layers.
    val isCandidate: Boolean = false,
    val isSpecialCandidate: Boolean = false,
    val isSelected: Boolean = false,
    val selectionOrder: Int? = null,
    val isCurrentActor: Boolean = false,
    val isInteractionEnabled: Boolean = isCandidate || isSelected || selectionOrder != null,
    val motionKey: String = seatId,
    val contextMarker: String? = null,
    val badge: String? = null,
    val badgeTone: ClocktowerSquareTableBadgeTone = ClocktowerSquareTableBadgeTone.Default,
    val isAlive: Boolean = true,
    val hasUnspentGhostVote: Boolean = false,
    val isDrunk: Boolean = false,
    val isPoisoned: Boolean = false,
    val actualAlignment: com.codex.campboardgamehost.clocktower.domain.Alignment? = null,
)

/** No parallel mutable state: derive candidate stroke directly from this frame's inputs. */
internal fun clocktowerSquareTableCandidateDashDp(seat: ClocktowerSquareTableSeatUiModel): List<Float>? =
    when {
        seat.isSelected || seat.selectionOrder != null -> null
        seat.isSpecialCandidate -> listOf(5f, 4f)
        seat.isCandidate -> listOf(12f, 6f)
        else -> null
    }

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

/** A short hollow arrow at the table-facing edge, independent of target-selection borders. */
internal fun clocktowerWakeArrowPoints(
    slot: HostTableSpatialSlot,
    layout: HostTableLayout,
): List<Offset> {
    val constraints = layout.constraints
    val seatCenter = Offset(slot.centerX, slot.centerY)
    val direction = when (slot.edge) {
        ClocktowerSquareTableEdge.Top -> Offset(0f, -1f)
        ClocktowerSquareTableEdge.Right -> Offset(1f, 0f)
        ClocktowerSquareTableEdge.Bottom -> Offset(0f, 1f)
        ClocktowerSquareTableEdge.Left -> Offset(-1f, 0f)
    }
    val vertical = direction.y != 0f
    val edgeDistance = (if (vertical) constraints.seatCardHeight else constraints.seatCardWidth) / 2
    val faceSpan = if (vertical) constraints.seatCardWidth else constraints.seatCardHeight
    val cross = Offset(-direction.y, direction.x)
    val centerLeft = (constraints.availableWidth - constraints.centerWorkspaceWidth) / 2 + HOST_TABLE_CENTER_CONTENT_PADDING
    val centerRight = constraints.availableWidth - centerLeft
    val centerTop = (constraints.availableHeight - constraints.centerWorkspaceHeight) / 2 + HOST_TABLE_CENTER_CONTENT_PADDING
    val centerBottom = constraints.availableHeight - centerTop
    val controls = androidx.compose.ui.geometry.Rect(centerLeft, centerTop, centerRight, centerBottom)
    val cards = layout.slots.map {
        androidx.compose.ui.geometry.Rect(
            it.centerX - constraints.seatCardWidth / 2, it.centerY - constraints.seatCardHeight / 2,
            it.centerX + constraints.seatCardWidth / 2, it.centerY + constraints.seatCardHeight / 2,
        )
    }
    // Prefer the middle of the inward face. Near corners, shorten or slide along that same face.
    for (shift in listOf(0f, -0.25f, 0.25f, -0.4f, 0.4f)) {
        val tip = seatCenter - direction * (edgeDistance + 2f) + cross * (faceSpan * shift)
        for (length in listOf(16f, 12f, 8f, 6f)) for (halfHead in listOf(4.5f, 3f)) {
            val headLength = minOf(6f, length * 0.45f)
            fun point(back: Float, side: Float) = tip - direction * back + cross * side
            val points = listOf(tip, point(headLength, halfHead), point(headLength, 1.6f),
                point(length, 1.6f), point(length, -1.6f), point(headLength, -1.6f), point(headLength, -halfHead))
            val bounds = androidx.compose.ui.geometry.Rect(
                points.minOf { it.x } - 1f, points.minOf { it.y } - 1f,
                points.maxOf { it.x } + 1f, points.maxOf { it.y } + 1f,
            )
            if (bounds.left >= 0 && bounds.top >= 0 && bounds.right <= constraints.availableWidth &&
                bounds.bottom <= constraints.availableHeight && !bounds.overlaps(controls) &&
                cards.none { bounds.overlaps(it) }) return points
        }
    }
    return emptyList()
}

private const val HOST_TABLE_CENTER_CONTENT_PADDING = 12f
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
    playerCount: Int = 15,
    densityOverride: ClocktowerSquareTableSeatDensity? = null,
): HostTableLayoutConstraints {
    val seatDensity = densityOverride ?: clocktowerSquareTableSeatDensity(
        playerCount = playerCount,
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
        if (!canRenderHostTableSurface(availableWidth, availableHeight)) {
            return@BoxWithConstraints
        }
        val density = LocalDensity.current
        val resolvedLayout = layout ?: remember(
            availableWidth,
            availableHeight,
            seats.size,
        ) {
            // The full-screen Host navigation reduces available height on
            // 360×520 devices. Try every established card density before the
            // existing best-effort fallback; never crash a live 15-player game.
            val candidates = clocktowerSquareTableSeatDensityCandidates(seats.size)
                .map { candidate ->
                    hostTableSurfaceLayoutConstraints(
                        availableWidth = availableWidth,
                        availableHeight = availableHeight,
                        playerCount = seats.size,
                        densityOverride = candidate,
                    )
                }
            candidates.firstNotNullOfOrNull { constraints ->
                hostTableLayoutOrNull(seats.size, constraints)
            } ?: hostTableLayoutBestEffort(seats.size, candidates.last())
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
        val seatDensity = clocktowerSquareTableSeatDensityCandidates(seats.size)
            .firstOrNull {
                it.cardWidth == seatCardWidth && it.cardHeight == seatCardHeight
            } ?: clocktowerSquareTableSeatDensity(playerCount = seats.size)
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
            directionalDragTargetRingIndex?.let { resolvedLayout.slots[it] }
                ?.let { Offset(it.centerX, it.centerY) } ?: directionalDragPointerPosition
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
        val arrowOutlineColor = MaterialTheme.colorScheme.onPrimary
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
                    .zIndex(3f),
            ) {
                val delta = arrowEndDp - arrowStartDp
                val distance = delta.getDistance()
                if (distance > 2f) {
                    val direction = delta / distance
                    val cross = Offset(-direction.y, direction.x)
                    // End just inside the facing card edges, above their borders but away from labels.
                    val edgeDistance = minOf(
                        seatCardWidth / 2f / abs(direction.x),
                        seatCardHeight / 2f / abs(direction.y),
                    )
                    val start = arrowStartDp + direction * (edgeDistance - 3f).coerceAtLeast(0f)
                    val hasTarget = directionalDragTargetRingIndex != null || activeDirectionalSourceRingIndex == null
                    val end = if (hasTarget) arrowEndDp - direction * (edgeDistance - 3f).coerceAtLeast(0f) else arrowEndDp
                    val length = (end - start).x * direction.x + (end - start).y * direction.y
                    if (length > 2f) {
                        val headLength = minOf(18f, length * 0.55f)
                        val halfHead = minOf(12f, length * 0.45f)
                        val halfShaft = minOf(4f, halfHead / 2f)
                        val neck = end - direction * headLength
                        val points = listOf(start + cross * halfShaft, neck + cross * halfShaft,
                            neck + cross * halfHead, end, neck - cross * halfHead,
                            neck - cross * halfShaft, start - cross * halfShaft)
                        val path = Path().apply {
                            moveTo(points.first().x * densityScale, points.first().y * densityScale)
                            points.drop(1).forEach { lineTo(it.x * densityScale, it.y * densityScale) }
                            close()
                        }
                        // Contrasting casing keeps the outline visible on both alignment colors.
                        drawPath(path, arrowOutlineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 5.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round,
                        ))
                        drawPath(path, arrowColor, style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.5.dp.toPx(), join = androidx.compose.ui.graphics.StrokeJoin.Round,
                        ))
                    }
                }
            }
        }

        val wakeArrowColor = MaterialTheme.colorScheme.onBackground
        val wakeArrows = remember(placements, resolvedLayout) {
            placements.filter { it.seat.isCurrentActor }
                .map { clocktowerWakeArrowPoints(it.spatialSlot, resolvedLayout) }.filter { it.isNotEmpty() }
        }
        if (wakeArrows.isNotEmpty()) {
            Canvas(Modifier.fillMaxSize().zIndex(0.75f)) {
                wakeArrows.forEach { points ->
                    val path = Path().apply {
                        moveTo(points.first().x.dp.toPx(), points.first().y.dp.toPx())
                        points.drop(1).forEach { lineTo(it.x.dp.toPx(), it.y.dp.toPx()) }
                        close()
                    }
                    drawPath(path, wakeArrowColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
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
                    .padding(HOST_TABLE_CENTER_CONTENT_PADDING.dp),
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
    val language = LocalContext.current.resources.configuration.locales[0].language
    val palette = clocktowerSquareTableSeatPalette(seat.actualAlignment, seat.isAlive)
    val candidateDash = clocktowerSquareTableCandidateDashDp(seat)
    val lifeMarkers = clocktowerSquareTableLifeMarkers(
        isAlive = seat.isAlive,
        hasUnspentGhostVote = seat.hasUnspentGhostVote,
    )
    val ghostVoteMarkerColor = Color(0xFFFFD56B)
    val clickModifier = if (canSelect) {
        Modifier.clickable { onSeatClick(seat.seatId) }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp, max = density.cardHeight.dp)
            .semantics {
                if (seat.isCurrentActor) stateDescription = if (language == "en") "Current wake target" else "本步唤醒对象"
            }
            .then(clickModifier),
        shape = RoundedCornerShape(12.dp),
        color = palette.container,
        contentColor = palette.content,
        // Candidates own their entire outline; the Surface border would paint over their dashes.
        border = if (candidateDash != null) null else BorderStroke(
            if (seat.isSelected || seat.selectionOrder != null) 3.dp else 1.5.dp,
            if (seat.isSelected || seat.selectionOrder != null) Color(0xFFFFD56B) else palette.border,
        ),
        tonalElevation = if (canSelect) 2.dp else 0.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            // One stroke renderer for the supplied local candidate facts.
            candidateDash?.let { dash ->
                Canvas(Modifier.fillMaxSize()) {
                    val strokeWidth = 2.5.dp.toPx()
                    val inset = strokeWidth / 2f
                    drawRoundRect(
                        color = if (seat.isSpecialCandidate) Color(0xFF53D9EC) else Color(0xFFFFD56B),
                        topLeft = Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx() - inset),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidth,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                dash.map { it.dp.toPx() }.toFloatArray(),
                            ),
                        ),
                    )
                }
            }
            if (lifeMarkers.showDeathMark) {
                Text(
                    text = "亡",
                    color = Color(0xFFFFD56B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 3.dp, top = 2.dp),
                )
            }

            if (lifeMarkers.showUnspentGhostVote) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val markerInset = 7.dp.toPx()
                    drawCircle(
                        color = ghostVoteMarkerColor,
                        radius = 4.5.dp.toPx(),
                        center = Offset(size.width - markerInset, size.height - markerInset),
                    )
                }
            }

            if (seat.isDrunk) {
                Box(
                    Modifier.align(Alignment.CenterStart).padding(start = 2.dp)
                        .width(3.dp).height((density.cardHeight - 8f).dp)
                        .background(Color(0xFFAC72E0)),
                )
                Text(
                    text = "醉",
                    color = Color(0xFFE5C7FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 5.dp, bottom = 1.dp),
                )
            }
            if (seat.isPoisoned) {
                Box(
                    Modifier.align(Alignment.CenterEnd).padding(end = 2.dp)
                        .width(3.dp).height((density.cardHeight - 8f).dp)
                        .background(Color(0xFFB8D957)),
                )
                Text(
                    text = "毒",
                    color = Color(0xFFE2F59C),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 9.dp, bottom = 1.dp),
                )
            }

            Text(
                text = seat.seatNumber.toString(),
                color = palette.content,
                fontSize = (density.cardWidth * 0.25f).coerceIn(16f, 20f).sp,
                lineHeight = (density.cardWidth * 0.28f).coerceIn(18f, 22f).sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 4.dp, top = 2.dp),
            )

            val selectionMarker = when (seat.selectionOrder) {
                1 -> "①"
                2 -> "②"
                null -> if (seat.isSelected) "✓" else null
                else -> seat.selectionOrder.toString()
            }
            (selectionMarker ?: seat.contextMarker)?.let { marker ->
                Text(
                    text = marker,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = if (seat.isAlive) {
                        Modifier.align(Alignment.TopEnd).padding(end = 3.dp, top = 2.dp)
                    } else {
                        Modifier.align(Alignment.BottomCenter).padding(bottom = 1.dp)
                    },
                )
            }

            seat.badge?.let { badge ->
                Text(
                    text = badge,
                    color = if (seat.badgeTone == ClocktowerSquareTableBadgeTone.Warning) {
                        Color(0xFFFFC107)
                    } else {
                        palette.content
                    },
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 3.dp, bottom = 2.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = density.horizontalPaddingDp.dp,
                        vertical = density.verticalPaddingDp.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = seat.label,
                    maxLines = density.primaryMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = density.primaryFontSizeSp.sp,
                    lineHeight = density.primaryLineHeightSp.sp,
                    fontWeight = if (seat.isCurrentActor || seat.isSelected || seat.selectionOrder != null) {
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
}

/** Actual Session alignment controls background, never selection or shown role. */
internal fun clocktowerAlignmentSeatColor(
    alignment: com.codex.campboardgamehost.clocktower.domain.Alignment,
): Color = when (alignment) {
    com.codex.campboardgamehost.clocktower.domain.Alignment.GOOD -> ClocktowerContrastPolicy.goodSeat
    com.codex.campboardgamehost.clocktower.domain.Alignment.EVIL -> ClocktowerContrastPolicy.evilSeat
}

private data class ClocktowerSquareTableSeatPalette(
    val container: Color,
    val content: Color,
    val border: Color,
)

@Composable
private fun clocktowerSquareTableSeatPalette(
    alignment: com.codex.campboardgamehost.clocktower.domain.Alignment?,
    isAlive: Boolean,
): ClocktowerSquareTableSeatPalette {
    val colors = MaterialTheme.colorScheme
    val alignmentColor = alignment?.let(::clocktowerAlignmentSeatColor)
        ?: return ClocktowerSquareTableSeatPalette(colors.surfaceVariant, colors.onSurfaceVariant, colors.outline)
    val background = if (isAlive) alignmentColor else lerp(alignmentColor, Color(0xFF282A35), 0.28f)
    return ClocktowerSquareTableSeatPalette(background, Color.White, Color(0xFFCBD5E1))
}

