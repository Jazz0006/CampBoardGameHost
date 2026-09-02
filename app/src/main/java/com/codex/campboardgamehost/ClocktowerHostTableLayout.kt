package com.codex.campboardgamehost

import kotlin.math.floor

/**
 * Pure geometry inputs for the persistent Storyteller table.
 *
 * All values use one caller-chosen linear unit (the Compose caller uses dp). Layout policy must not
 * infer capacity from a device class or from a player-count lookup table.
 */
internal data class HostTableLayoutConstraints(
    val availableWidth: Float,
    val availableHeight: Float,
    val seatCardWidth: Float,
    val seatCardHeight: Float,
    val minimumSafeSeparation: Float,
    val centerWorkspaceWidth: Float,
    val centerWorkspaceHeight: Float,
) {
    init {
        require(availableWidth.isFinite() && availableWidth > 0f) {
            "Host-table available width must be finite and positive"
        }
        require(availableHeight.isFinite() && availableHeight > 0f) {
            "Host-table available height must be finite and positive"
        }
        require(seatCardWidth.isFinite() && seatCardWidth > 0f) {
            "Host-table seat-card width must be finite and positive"
        }
        require(seatCardHeight.isFinite() && seatCardHeight > 0f) {
            "Host-table seat-card height must be finite and positive"
        }
        require(minimumSafeSeparation.isFinite() && minimumSafeSeparation >= 0f) {
            "Host-table minimum separation must be finite and non-negative"
        }
        require(centerWorkspaceWidth.isFinite() && centerWorkspaceWidth >= 0f) {
            "Host-table center width must be finite and non-negative"
        }
        require(centerWorkspaceHeight.isFinite() && centerWorkspaceHeight >= 0f) {
            "Host-table center height must be finite and non-negative"
        }
        require(centerWorkspaceWidth <= availableWidth) {
            "Host-table center workspace cannot be wider than the available table"
        }
        require(centerWorkspaceHeight <= availableHeight) {
            "Host-table center workspace cannot be taller than the available table"
        }

        val horizontalWorkspaceMargin = (availableWidth - centerWorkspaceWidth) / 2f
        val verticalWorkspaceMargin = (availableHeight - centerWorkspaceHeight) / 2f
        require(horizontalWorkspaceMargin >= seatCardWidth + minimumSafeSeparation) {
            "Host-table center workspace leaves insufficient horizontal seat clearance"
        }
        require(verticalWorkspaceMargin >= seatCardHeight + minimumSafeSeparation) {
            "Host-table center workspace leaves insufficient vertical seat clearance"
        }
    }
}

/**
 * One deterministic point on the clockwise physical table ring.
 *
 * [ringIndex] is the shared ordering authority for rendering and future drag insertion. Coordinates
 * are seat-card centers in the same units as [HostTableLayoutConstraints].
 */
internal data class HostTableSpatialSlot(
    val ringIndex: Int,
    val edge: ClocktowerSquareTableEdge,
    val indexOnEdge: Int,
    val centerX: Float,
    val centerY: Float,
)

internal data class HostTableLayout(
    val constraints: HostTableLayoutConstraints,
    val slots: List<HostTableSpatialSlot>,
)

/**
 * Computes a capacity-aware clockwise slot ring from actual geometry.
 *
 * Horizontal and vertical edge capacities are deliberately independent. Portrait layouts therefore
 * naturally place more seats on the longer left/right edges without per-player-count tables.
 */
internal fun hostTableLayout(
    playerCount: Int,
    constraints: HostTableLayoutConstraints,
): HostTableLayout {
    require(playerCount >= 0) { "Host-table player count cannot be negative" }
    if (playerCount == 0) return HostTableLayout(constraints, emptyList())

    val horizontalCenterStart = constraints.seatCardWidth / 2f
    val horizontalCenterEnd = constraints.availableWidth - constraints.seatCardWidth / 2f

    // Reserve one top/bottom seat-card band plus the requested separation before side-edge seats.
    // This prevents a side seat from colliding with a horizontal seat at either corner.
    val verticalCenterInset = constraints.seatCardHeight * 1.5f + constraints.minimumSafeSeparation
    val verticalCenterStart = verticalCenterInset
    val verticalCenterEnd = constraints.availableHeight - verticalCenterInset

    val horizontalCapacity = edgeCapacity(
        centerSpan = horizontalCenterEnd - horizontalCenterStart,
        cardExtent = constraints.seatCardWidth,
        minimumSafeSeparation = constraints.minimumSafeSeparation,
    )
    val verticalCapacity = edgeCapacity(
        centerSpan = verticalCenterEnd - verticalCenterStart,
        cardExtent = constraints.seatCardHeight,
        minimumSafeSeparation = constraints.minimumSafeSeparation,
    )
    val edgeCapacities = intArrayOf(
        horizontalCapacity,
        verticalCapacity,
        horizontalCapacity,
        verticalCapacity,
    )
    require(edgeCapacities.sum() >= playerCount) {
        "Host-table perimeter capacity ${edgeCapacities.sum()} is insufficient for $playerCount players"
    }

    val edgeCounts = allocateEdgeCounts(
        playerCount = playerCount,
        edgeCapacities = edgeCapacities,
    )

    val topX = evenlySpacedCenters(
        count = edgeCounts[ClocktowerSquareTableEdge.Top.ordinal],
        start = horizontalCenterStart,
        end = horizontalCenterEnd,
    )
    val rightY = evenlySpacedCenters(
        count = edgeCounts[ClocktowerSquareTableEdge.Right.ordinal],
        start = verticalCenterStart,
        end = verticalCenterEnd,
    )
    val bottomX = evenlySpacedCenters(
        count = edgeCounts[ClocktowerSquareTableEdge.Bottom.ordinal],
        start = horizontalCenterEnd,
        end = horizontalCenterStart,
    )
    val leftY = evenlySpacedCenters(
        count = edgeCounts[ClocktowerSquareTableEdge.Left.ordinal],
        start = verticalCenterEnd,
        end = verticalCenterStart,
    )

    val slots = buildList(playerCount) {
        topX.forEachIndexed { indexOnEdge, centerX ->
            add(
                HostTableSpatialSlot(
                    ringIndex = size,
                    edge = ClocktowerSquareTableEdge.Top,
                    indexOnEdge = indexOnEdge,
                    centerX = centerX,
                    centerY = constraints.seatCardHeight / 2f,
                ),
            )
        }
        rightY.forEachIndexed { indexOnEdge, centerY ->
            add(
                HostTableSpatialSlot(
                    ringIndex = size,
                    edge = ClocktowerSquareTableEdge.Right,
                    indexOnEdge = indexOnEdge,
                    centerX = constraints.availableWidth - constraints.seatCardWidth / 2f,
                    centerY = centerY,
                ),
            )
        }
        bottomX.forEachIndexed { indexOnEdge, centerX ->
            add(
                HostTableSpatialSlot(
                    ringIndex = size,
                    edge = ClocktowerSquareTableEdge.Bottom,
                    indexOnEdge = indexOnEdge,
                    centerX = centerX,
                    centerY = constraints.availableHeight - constraints.seatCardHeight / 2f,
                ),
            )
        }
        leftY.forEachIndexed { indexOnEdge, centerY ->
            add(
                HostTableSpatialSlot(
                    ringIndex = size,
                    edge = ClocktowerSquareTableEdge.Left,
                    indexOnEdge = indexOnEdge,
                    centerX = constraints.seatCardWidth / 2f,
                    centerY = centerY,
                ),
            )
        }
    }

    return HostTableLayout(
        constraints = constraints,
        slots = slots,
    )
}

private fun edgeCapacity(
    centerSpan: Float,
    cardExtent: Float,
    minimumSafeSeparation: Float,
): Int {
    if (centerSpan < 0f) return 0
    val pitch = cardExtent + minimumSafeSeparation
    return floor(centerSpan / pitch).toInt() + 1
}

/**
 * Fill the least-used fraction of each edge capacity first. This keeps opposite edges balanced when
 * possible while still allowing a longer edge to absorb proportionally more seats.
 */
private fun allocateEdgeCounts(
    playerCount: Int,
    edgeCapacities: IntArray,
): IntArray {
    val counts = IntArray(edgeCapacities.size)
    repeat(playerCount) {
        val nextEdge = edgeCapacities.indices
            .filter { edgeIndex -> counts[edgeIndex] < edgeCapacities[edgeIndex] }
            .minWithOrNull(
                compareBy<Int>(
                    { edgeIndex -> counts[edgeIndex].toDouble() / edgeCapacities[edgeIndex].toDouble() },
                    { edgeIndex -> edgeIndex },
                ),
            )
            ?: error("Host-table capacity allocation exhausted unexpectedly")
        counts[nextEdge] += 1
    }
    return counts
}

private fun evenlySpacedCenters(
    count: Int,
    start: Float,
    end: Float,
): List<Float> = when (count) {
    0 -> emptyList()
    1 -> listOf((start + end) / 2f)
    else -> {
        val step = (end - start) / (count - 1).toFloat()
        List(count) { index -> start + step * index }
    }
}
