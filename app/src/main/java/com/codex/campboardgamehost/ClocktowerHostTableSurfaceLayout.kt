package com.codex.campboardgamehost

/**
 * Resolved table geometry plus the visual density that produced it.
 *
 * Keeping both together prevents callers from treating the preferred player-count density as if it
 * were guaranteed to fit the actual Compose viewport.
 */
internal data class ResolvedHostTableSurfaceLayout(
    val layout: HostTableLayout,
    val seatDensity: ClocktowerSquareTableSeatDensity,
)

/**
 * Chooses the largest supported seat density that satisfies the strict rounded-perimeter capacity
 * invariant for the actual surface constraints.
 *
 * The normal player-count density is always attempted first. Only when it cannot fit do we descend
 * through the existing medium/compact tiers. If an unusually constrained surface cannot safely fit
 * even the compact tier, rendering falls back to compact best-effort geometry rather than allowing
 * a capacity assertion to terminate the Activity.
 */
internal fun resolveHostTableSurfaceLayout(
    availableWidth: Float,
    availableHeight: Float,
    playerCount: Int,
    detailedSeatCards: Boolean = false,
): ResolvedHostTableSurfaceLayout {
    val preferredConstraints = hostTableSurfaceLayoutConstraints(
        availableWidth = availableWidth,
        availableHeight = availableHeight,
        detailedSeatCards = detailedSeatCards,
        playerCount = playerCount,
    )
    val densityCandidates = clocktowerSquareTableSeatDensityCandidates(
        playerCount = playerCount,
        detailedSeatCards = detailedSeatCards,
    )

    densityCandidates.forEach { density ->
        val constraints = preferredConstraints.copy(
            seatCardWidth = density.cardWidth,
            seatCardHeight = density.cardHeight,
        )
        val safeLayout = hostTableLayoutOrNull(
            playerCount = playerCount,
            constraints = constraints,
        )
        if (safeLayout != null) {
            return ResolvedHostTableSurfaceLayout(
                layout = safeLayout,
                seatDensity = density,
            )
        }
    }

    val fallbackDensity = densityCandidates.last()
    val fallbackConstraints = preferredConstraints.copy(
        seatCardWidth = fallbackDensity.cardWidth,
        seatCardHeight = fallbackDensity.cardHeight,
    )
    return ResolvedHostTableSurfaceLayout(
        layout = hostTableLayoutBestEffort(
            playerCount = playerCount,
            constraints = fallbackConstraints,
        ),
        seatDensity = fallbackDensity,
    )
}
