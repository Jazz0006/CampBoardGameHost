package com.codex.campboardgamehost

/**
 * Stable UI-facing projection for an already-ranked recommendation stream.
 *
 * Ranking and candidate quality remain owned by the Recommendation Provider. This adapter only
 * limits the normal presentation surface to the first three real recommendations. Manual legality
 * is intentionally outside this model and remains backed by the complete legal semantic domain.
 *
 * [primary] and [alternatives] remain as compatibility accessors while migrated experienced-mode
 * surfaces consume [recommendations] and present all three entries as recommendations.
 */
internal data class ClocktowerRecommendationPresentation<T>(
    val primary: T?,
    val alternatives: List<T>,
) {
    init {
        require(alternatives.size <= 2) {
            "Normal recommendation presentation supports at most three ranked recommendations."
        }
        require(primary != null || alternatives.isEmpty()) {
            "Additional recommendations cannot exist without a primary recommendation."
        }
    }

    val recommendations: List<T>
        get() = buildList {
            primary?.let(::add)
            addAll(alternatives)
        }
}

internal fun <T> clocktowerRecommendationPresentation(
    rankedCandidates: List<T>,
): ClocktowerRecommendationPresentation<T> = ClocktowerRecommendationPresentation(
    primary = rankedCandidates.firstOrNull(),
    alternatives = rankedCandidates.drop(1).take(2),
)
