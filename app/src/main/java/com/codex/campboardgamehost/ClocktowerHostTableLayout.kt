package com.codex.campboardgamehost

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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
 * [ringIndex] is the shared ordering authority for rendering and drag insertion. Coordinates are
 * seat-card centers in the same units as [HostTableLayoutConstraints].
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
 * Geometry for the visible tabletop inside the seat-card ring.
 *
 * Keeping this separate from Compose styling means future wood/cloth/shadow treatments can change
 * without creating a second seat-position authority.
 */
internal data class HostTableTabletopGeometry(
    val width: Float,
    val height: Float,
    val cornerRadius: Float,
)

internal fun hostTableTabletopGeometry(
    constraints: HostTableLayoutConstraints,
): HostTableTabletopGeometry {
    val width = (
        constraints.availableWidth -
            2f * (constraints.seatCardWidth + constraints.minimumSafeSeparation)
        ).coerceAtLeast(0f)
    val height = (
        constraints.availableHeight -
            2f * (constraints.seatCardHeight + constraints.minimumSafeSeparation)
        ).coerceAtLeast(0f)
    val cornerRadius = minOf(
        maxOf(constraints.seatCardWidth, constraints.seatCardHeight) / 2f,
        width / 2f,
        height / 2f,
    ).coerceAtLeast(0f)

    return HostTableTabletopGeometry(
        width = width,
        height = height,
        cornerRadius = cornerRadius,
    )
}

/**
 * Computes one capacity-aware clockwise slot ring from a continuous rounded-rectangle path.
 *
 * Every player is sampled at the same path-length interval around the complete perimeter. The
 * resulting [HostTableSpatialSlot] list remains the single authority shared by rendering and drag
 * hit testing.
 */
internal fun hostTableLayout(
    playerCount: Int,
    constraints: HostTableLayoutConstraints,
): HostTableLayout {
    require(playerCount >= 0) { "Host-table player count cannot be negative" }
    if (playerCount == 0) return HostTableLayout(constraints, emptyList())

    val perimeter = RoundedRectangleSeatPerimeter(constraints)
    val pathStep = perimeter.totalLength / playerCount.toFloat()
    val nextIndexOnEdge = IntArray(ClocktowerSquareTableEdge.values().size)
    val slots = List(playerCount) { ringIndex ->
        val point = perimeter.pointAt(pathStep * ringIndex)
        val edgeIndex = point.edge.ordinal
        HostTableSpatialSlot(
            ringIndex = ringIndex,
            edge = point.edge,
            indexOnEdge = nextIndexOnEdge[edgeIndex]++,
            centerX = point.x,
            centerY = point.y,
        )
    }

    requireSafeSeatSeparation(
        slots = slots,
        constraints = constraints,
    )

    return HostTableLayout(
        constraints = constraints,
        slots = slots,
    )
}

private data class RoundedPerimeterPoint(
    val x: Float,
    val y: Float,
    val edge: ClocktowerSquareTableEdge,
)

private class RoundedRectangleSeatPerimeter(
    constraints: HostTableLayoutConstraints,
) {
    private val left = constraints.seatCardWidth / 2f
    private val top = constraints.seatCardHeight / 2f
    private val right = constraints.availableWidth - constraints.seatCardWidth / 2f
    private val bottom = constraints.availableHeight - constraints.seatCardHeight / 2f
    private val pathWidth = right - left
    private val pathHeight = bottom - top
    private val radius = minOf(
        maxOf(constraints.seatCardWidth, constraints.seatCardHeight) +
            constraints.minimumSafeSeparation,
        pathWidth / 2f,
        pathHeight / 2f,
    )
    private val horizontalStraight = pathWidth - 2f * radius
    private val verticalStraight = pathHeight - 2f * radius
    private val quarterArc = PI.toFloat() * radius / 2f

    val totalLength: Float =
        2f * horizontalStraight + 2f * verticalStraight + 4f * quarterArc

    init {
        require(totalLength.isFinite() && totalLength > 0f) {
            "Host-table rounded perimeter must have positive finite length"
        }
    }

    fun pointAt(rawDistance: Float): RoundedPerimeterPoint {
        var distance = ((rawDistance % totalLength) + totalLength) % totalLength

        if (distance <= horizontalStraight) {
            return RoundedPerimeterPoint(
                x = left + radius + distance,
                y = top,
                edge = ClocktowerSquareTableEdge.Top,
            )
        }
        distance -= horizontalStraight

        if (distance <= quarterArc) {
            return arcPoint(
                centerX = right - radius,
                centerY = top + radius,
                startAngleRadians = -PI.toFloat() / 2f,
                arcDistance = distance,
                firstEdge = ClocktowerSquareTableEdge.Top,
                secondEdge = ClocktowerSquareTableEdge.Right,
            )
        }
        distance -= quarterArc

        if (distance <= verticalStraight) {
            return RoundedPerimeterPoint(
                x = right,
                y = top + radius + distance,
                edge = ClocktowerSquareTableEdge.Right,
            )
        }
        distance -= verticalStraight

        if (distance <= quarterArc) {
            return arcPoint(
                centerX = right - radius,
                centerY = bottom - radius,
                startAngleRadians = 0f,
                arcDistance = distance,
                firstEdge = ClocktowerSquareTableEdge.Right,
                secondEdge = ClocktowerSquareTableEdge.Bottom,
            )
        }
        distance -= quarterArc

        if (distance <= horizontalStraight) {
            return RoundedPerimeterPoint(
                x = right - radius - distance,
                y = bottom,
                edge = ClocktowerSquareTableEdge.Bottom,
            )
        }
        distance -= horizontalStraight

        if (distance <= quarterArc) {
            return arcPoint(
                centerX = left + radius,
                centerY = bottom - radius,
                startAngleRadians = PI.toFloat() / 2f,
                arcDistance = distance,
                firstEdge = ClocktowerSquareTableEdge.Bottom,
                secondEdge = ClocktowerSquareTableEdge.Left,
            )
        }
        distance -= quarterArc

        if (distance <= verticalStraight) {
            return RoundedPerimeterPoint(
                x = left,
                y = bottom - radius - distance,
                edge = ClocktowerSquareTableEdge.Left,
            )
        }
        distance -= verticalStraight

        return arcPoint(
            centerX = left + radius,
            centerY = top + radius,
            startAngleRadians = PI.toFloat(),
            arcDistance = distance,
            firstEdge = ClocktowerSquareTableEdge.Left,
            secondEdge = ClocktowerSquareTableEdge.Top,
        )
    }

    private fun arcPoint(
        centerX: Float,
        centerY: Float,
        startAngleRadians: Float,
        arcDistance: Float,
        firstEdge: ClocktowerSquareTableEdge,
        secondEdge: ClocktowerSquareTableEdge,
    ): RoundedPerimeterPoint {
        val angle = startAngleRadians + arcDistance / radius
        return RoundedPerimeterPoint(
            x = centerX + radius * cos(angle.toDouble()).toFloat(),
            y = centerY + radius * sin(angle.toDouble()).toFloat(),
            edge = if (arcDistance <= quarterArc / 2f) firstEdge else secondEdge,
        )
    }
}

private fun requireSafeSeatSeparation(
    slots: List<HostTableSpatialSlot>,
    constraints: HostTableLayoutConstraints,
) {
    val epsilon = 0.001f
    slots.forEachIndexed { firstIndex, first ->
        slots.drop(firstIndex + 1).forEach { second ->
            val horizontalClearance = abs(first.centerX - second.centerX) + epsilon >=
                constraints.seatCardWidth + constraints.minimumSafeSeparation
            val verticalClearance = abs(first.centerY - second.centerY) + epsilon >=
                constraints.seatCardHeight + constraints.minimumSafeSeparation
            require(horizontalClearance || verticalClearance) {
                "Host-table rounded perimeter capacity is insufficient for ${slots.size} players"
            }
        }
    }
}
