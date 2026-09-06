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

private val HOST_TABLE_COMPACT_FALLBACK_SCALES = listOf(0.96f, 0.92f, 0.88f, 0.84f, 0.80f)

/**
 * Chooses the largest supported seat density that satisfies the strict rounded-perimeter capacity
 * invariant for the actual surface constraints.
 *
 * The normal player-count density is always attempted first. Only when it cannot fit do we descend
 * through the existing medium/compact tiers, followed by small geometry-only reductions of the
 * compact tier. Typography remains at the compact readable minimum while card geometry adapts to
 * the real viewport. If an unusually constrained surface still cannot satisfy the invariant,
 * rendering uses the smallest best-effort geometry rather than allowing a capacity assertion to
 * terminate the Activity.
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
    val tierCandidates = clocktowerSquareTableSeatDensityCandidates(
        playerCount = playerCount,
        detailedSeatCards = detailedSeatCards,
    )
    val compactDensity = tierCandidates.last()
    val scaledCompactCandidates = HOST_TABLE_COMPACT_FALLBACK_SCALES.map { scale ->
        compactDensity.copy(
            cardWidth = compactDensity.cardWidth * scale,
            cardHeight = compactDensity.cardHeight * scale,
        )
    }
    val densityCandidates = (tierCandidates + scaledCompactCandidates).distinct()

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
