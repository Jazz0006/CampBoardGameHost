package com.codex.campboardgamehost

/**
 * Stable UI-facing projection for an already-ranked recommendation stream.
 *
 * Ranking and candidate quality remain owned by the Recommendation Provider. This adapter only
 * limits the normal presentation surface to one primary recommendation plus at most two
 * alternatives. Manual legality is intentionally outside this model and remains backed by the
 * complete legal semantic domain.
 */
internal data class ClocktowerRecommendationPresentation<T>(
    val primary: T?,
    val alternatives: List<T>,
) {
    init {
        require(alternatives.size <= 2) {
            "Normal recommendation presentation supports at most two alternatives."
        }
        require(primary != null || alternatives.isEmpty()) {
            "Alternatives cannot exist without a primary recommendation."
        }
    }
}

internal fun <T> clocktowerRecommendationPresentation(
    rankedCandidates: List<T>,
): ClocktowerRecommendationPresentation<T> = ClocktowerRecommendationPresentation(
    primary = rankedCandidates.firstOrNull(),
    alternatives = rankedCandidates.drop(1).take(2),
)
