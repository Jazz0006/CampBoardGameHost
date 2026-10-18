package com.codex.campboardgamehost.clocktower.setup

/** Stable references to a dealt seat, not a gameplay-dealt seat. */
internal data class TroubleBrewingDrunkCandidate(
    val seat: Int,
    val playerName: String,
    val shownRoleId: String,
) {
    init { require(seat > 0 && playerName.isNotBlank() && shownRoleId.isNotBlank()) }
}

internal data class TroubleBrewingDrunkSelectionRequest(
    val candidates: List<TroubleBrewingDrunkCandidate>,
    val recommendedCandidate: TroubleBrewingDrunkCandidate?,
) {
    init {
        require(candidates.isNotEmpty()) { "Drunk selection requires dealt seats." }
        require(candidates.distinct().size == candidates.size) { "Drunk candidates must be unique." }
        require(recommendedCandidate == null || recommendedCandidate in candidates) {
            "Drunk recommendation must belong to the current dealt seats."
        }
    }
}

internal sealed interface TroubleBrewingDrunkSelectionRoute {
    data object NoSelectionNeeded : TroubleBrewingDrunkSelectionRoute
    data class ManualSelection(val request: TroubleBrewingDrunkSelectionRequest) : TroubleBrewingDrunkSelectionRoute
}

/** A pending assignment always needs explicit confirmation of one dealt seat. */
internal object TroubleBrewingDrunkSelectionRouter {
    fun route(
        preparedSetup: TroubleBrewingPreparedSetup,
        recommendedCandidate: TroubleBrewingDrunkCandidate?,
    ): TroubleBrewingDrunkSelectionRoute {
        val intermediate = preparedSetup.intermediateSetup
        val seats = if (intermediate.visibleRoster.hasDrunk)
            intermediate.shownSeatAssignments.map {
                TroubleBrewingDrunkCandidate(it.seat, it.playerName, it.shownRoleId)
            } else emptyList()
        if (!intermediate.visibleRoster.hasDrunk) {
            require(seats.isEmpty()) { "No-Drunk setup cannot expose Drunk candidates." }
            require(recommendedCandidate == null) { "No-Drunk setup cannot carry a Drunk recommendation." }
            return TroubleBrewingDrunkSelectionRoute.NoSelectionNeeded
        }
        require(seats.isNotEmpty()) { "Drunk setup requires a dealt seat." }
        require(recommendedCandidate == null || recommendedCandidate in seats) {
            "Drunk recommendation must belong to the current dealt seats."
        }
        return TroubleBrewingDrunkSelectionRoute.ManualSelection(
                TroubleBrewingDrunkSelectionRequest(seats, recommendedCandidate),
            )
    }
}
