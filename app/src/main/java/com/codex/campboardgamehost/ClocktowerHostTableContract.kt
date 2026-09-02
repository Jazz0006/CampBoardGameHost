package com.codex.campboardgamehost

/**
 * Stable physical seat identity for the Storyteller workspace.
 *
 * The number is the canonical Clocktower seat number. Callers may reorder presentation lists, but
 * they must not derive a new identity from a filtered/current-phase list index.
 */
internal data class ClocktowerSeatId(
    val number: Int,
) {
    init {
        require(number > 0) { "Seat number must be positive" }
    }
}

/** Host-private role presentation. Public claims deliberately do not belong to this type. */
internal data class HostRolePresentation(
    val roleId: String,
    val displayName: String,
)

/**
 * Stable seat-owned information available to the Storyteller table.
 *
 * Actual role and shown/perceived role remain separate semantic fields. Public claim history will
 * be introduced by its own durable R4D slice rather than being folded into either role field.
 */
internal data class HostSeatPresentation(
    val seatId: ClocktowerSeatId,
    val playerName: String,
    val isAlive: Boolean,
    val actualRole: HostRolePresentation? = null,
    val shownRole: HostRolePresentation? = null,
)

/** Bounded table interaction families. Domain legality remains outside this presentation contract. */
internal enum class HostTableInteractionMode {
    ReadOnly,
    Selection,
    OrderedSelection,
    Sequential,
}

/**
 * Host-table interaction state addressed only by typed physical seat identity.
 *
 * These fields describe presentation/interaction state; they do not decide which targets, votes,
 * nominations, or actions are legal.
 */
internal data class HostTableInteractionState(
    val mode: HostTableInteractionMode = HostTableInteractionMode.ReadOnly,
    val selectableSeatIds: Set<ClocktowerSeatId> = emptySet(),
    val selectedSeatIds: List<ClocktowerSeatId> = emptyList(),
    val highlightedSeatIds: Set<ClocktowerSeatId> = emptySet(),
    val currentSeatId: ClocktowerSeatId? = null,
    val lockedSeatIds: Set<ClocktowerSeatId> = emptySet(),
)

internal data class HostTableSeatFrame(
    val seat: HostSeatPresentation,
    val spatialSlot: HostTableSpatialSlot,
    val isSelectable: Boolean,
    val isSelected: Boolean,
    val selectionOrder: Int?,
    val isHighlighted: Boolean,
    val isCurrent: Boolean,
    val isLocked: Boolean,
)

/**
 * Projects phase-specific Host presentation into the permanent physical table topology.
 *
 * Input order is intentionally ignored. Spatial order is always canonical seat-number order so the
 * same [ClocktowerSeatId] consumes the same position in the supplied deterministic layout across
 * Setup, Day, Night and future modes.
 */
internal fun hostTableSeatFrames(
    seats: List<HostSeatPresentation>,
    interaction: HostTableInteractionState,
    layout: HostTableLayout,
): List<HostTableSeatFrame> {
    if (seats.isEmpty()) {
        require(interaction.referencedSeatIds().isEmpty()) {
            "Host-table interaction cannot reference seats when the table is empty"
        }
        require(layout.slots.isEmpty()) {
            "Host-table layout cannot contain slots when the table is empty"
        }
        return emptyList()
    }

    val canonicalSeats = seats.sortedBy { it.seatId.number }
    val canonicalSeatIds = canonicalSeats.map { it.seatId }
    val expectedSeatIds = (1..canonicalSeats.size).map(::ClocktowerSeatId)
    require(canonicalSeatIds == expectedSeatIds) {
        "Host-table physical seats must be unique and contiguous from seat 1"
    }
    require(layout.slots.size == canonicalSeats.size) {
        "Host-table layout slot count must match the physical seat count"
    }
    require(layout.slots.map { it.ringIndex } == layout.slots.indices.toList()) {
        "Host-table layout slots must form one deterministic ordered ring"
    }
    require(interaction.selectedSeatIds.distinct().size == interaction.selectedSeatIds.size) {
        "Selected host-table seat identity must be unique"
    }

    val knownSeatIds = canonicalSeatIds.toSet()
    val unknownSeatIds = interaction.referencedSeatIds() - knownSeatIds
    require(unknownSeatIds.isEmpty()) {
        "Host-table interaction references unknown physical seats: $unknownSeatIds"
    }

    val spatialSlotsBySeat = canonicalSeats.zip(layout.slots)
        .associate { (seat, spatialSlot) -> seat.seatId to spatialSlot }
    val selectionOrderBySeat = interaction.selectedSeatIds
        .mapIndexed { index, seatId -> seatId to index + 1 }
        .toMap()

    return canonicalSeats.map { seat ->
        val seatId = seat.seatId
        HostTableSeatFrame(
            seat = seat,
            spatialSlot = spatialSlotsBySeat.getValue(seatId),
            isSelectable = seatId in interaction.selectableSeatIds,
            isSelected = seatId in selectionOrderBySeat,
            selectionOrder = selectionOrderBySeat[seatId],
            isHighlighted = seatId in interaction.highlightedSeatIds,
            isCurrent = seatId == interaction.currentSeatId,
            isLocked = seatId in interaction.lockedSeatIds,
        )
    }
}

internal fun ClocktowerSeatId.renderKey(): String = "seat-$number"

private fun HostTableInteractionState.referencedSeatIds(): Set<ClocktowerSeatId> = buildSet {
    addAll(selectableSeatIds)
    addAll(selectedSeatIds)
    addAll(highlightedSeatIds)
    currentSeatId?.let(::add)
    addAll(lockedSeatIds)
}
