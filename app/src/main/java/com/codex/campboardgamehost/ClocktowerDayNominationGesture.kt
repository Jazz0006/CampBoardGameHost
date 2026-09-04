package com.codex.campboardgamehost

internal data class HostTableDirectionalGesturePolicy(
    val sourceSeatIds: Set<ClocktowerSeatId>,
    val targetSeatIds: Set<ClocktowerSeatId>,
)

internal data class HostTableDirectionalLink(
    val sourceSeatId: ClocktowerSeatId,
    val targetSeatId: ClocktowerSeatId,
) {
    init {
        require(sourceSeatId != targetSeatId) {
            "Directional host-table link requires two different seats"
        }
    }
}

internal fun clocktowerDayNominationGesturePolicy(
    tableState: ClocktowerDayOverviewTableState,
): HostTableDirectionalGesturePolicy {
    val aliveSeatIds = tableState.seats
        .asSequence()
        .filter(HostSeatPresentation::isAlive)
        .map(HostSeatPresentation::seatId)
        .toSet()
    return HostTableDirectionalGesturePolicy(
        sourceSeatIds = aliveSeatIds,
        targetSeatIds = aliveSeatIds,
    )
}

/**
 * Resolves against the same already-computed spatial ring used for rendering.
 *
 * The nearest physical slot wins first. If that nearest slot is self or is not a legal target, the
 * gesture fails closed instead of silently snapping to a farther legal player.
 */
internal fun resolveHostTableDirectionalTargetRingIndex(
    layout: HostTableLayout,
    pointerX: Float,
    pointerY: Float,
    sourceRingIndex: Int,
    eligibleTargetRingIndices: Set<Int>,
): Int? {
    require(sourceRingIndex in layout.slots.indices) {
        "Directional gesture source ring index is outside the rendered Host-table ring"
    }
    require(eligibleTargetRingIndices.all { it in layout.slots.indices }) {
        "Directional gesture target ring indices must belong to the rendered Host-table ring"
    }
    if (eligibleTargetRingIndices.isEmpty()) return null

    val nearestRingIndex = nearestHostTableRingIndex(
        layout = layout,
        pointerX = pointerX,
        pointerY = pointerY,
    )
    return nearestRingIndex.takeIf { candidate ->
        candidate != sourceRingIndex && candidate in eligibleTargetRingIndices
    }
}
