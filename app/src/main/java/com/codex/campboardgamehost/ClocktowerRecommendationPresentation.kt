package com.codex.campboardgamehost

/**
 * Presentation-only cap: at most one advisory candidate.
 *
 * This does not assign provider trust or commit authority. Full Host-legal manual
 * alternatives are presented independently by the role-specific UI.
 */
internal data class ClocktowerRecommendationPresentation<T>(
    val primary: T?,
) {
    val recommendations: List<T>
        get() = listOfNotNull(primary)
}

internal fun <T> clocktowerRecommendationPresentation(
    rankedCandidates: List<T>,
): ClocktowerRecommendationPresentation<T> = ClocktowerRecommendationPresentation(
    primary = rankedCandidates.firstOrNull(),
)
