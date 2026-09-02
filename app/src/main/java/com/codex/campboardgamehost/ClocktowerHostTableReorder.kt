package com.codex.campboardgamehost

/**
 * Resolves a drag pointer to the nearest slot on the already-computed clockwise Host-table ring.
 *
 * Rendering and drag insertion therefore consume the same [HostTableLayout] instead of maintaining
 * independent edge/corner geometry. Exact distance ties resolve to the smaller ring index so the
 * result remains deterministic.
 */
internal fun nearestHostTableRingIndex(
    layout: HostTableLayout,
    pointerX: Float,
    pointerY: Float,
): Int {
    require(pointerX.isFinite() && pointerY.isFinite()) {
        "Host-table drag coordinates must be finite"
    }
    require(layout.slots.isNotEmpty()) {
        "Host-table drag requires at least one spatial slot"
    }

    return layout.slots.minWith(
        compareBy<HostTableSpatialSlot>(
            { slot -> squaredDistance(pointerX, pointerY, slot.centerX, slot.centerY) },
            HostTableSpatialSlot::ringIndex,
        ),
    ).ringIndex
}

/**
 * Moves one item to its final physical ring index.
 *
 * [targetIndex] is deliberately the item's final index after the move, not an insertion index in the
 * pre-removal list. This keeps drag targets and button-based fallbacks aligned with rendered slots.
 */
internal fun <T> reorderHostTableItems(
    items: List<T>,
    fromIndex: Int,
    targetIndex: Int,
): List<T> {
    require(fromIndex in items.indices) {
        "Host-table reorder source index $fromIndex is outside ${items.indices}"
    }
    require(targetIndex in items.indices) {
        "Host-table reorder target index $targetIndex is outside ${items.indices}"
    }
    if (fromIndex == targetIndex) return items.toList()

    return items.toMutableList().apply {
        val moved = removeAt(fromIndex)
        add(targetIndex, moved)
    }
}

private fun squaredDistance(
    firstX: Float,
    firstY: Float,
    secondX: Float,
    secondY: Float,
): Double {
    val deltaX = (firstX - secondX).toDouble()
    val deltaY = (firstY - secondY).toDouble()
    return deltaX * deltaX + deltaY * deltaY
}
