package com.codex.campboardgamehost.clocktower.domain

/**
 * Normal product UX no longer exposes the legacy global automation/style selector.
 *
 * Persisted legacy modes are still accepted during migration, but they must not
 * silently keep automatic execution or a hidden global recommendation style alive.
 * The normal interaction is recommendation-on, Storyteller-confirmed (ASSISTED),
 * with per-interaction Manual authority handled by the interaction surface.
 */
data class StorytellerRecommendationUxPolicy(
    val automaticExecution: Boolean,
    val recommendationStyle: RecommendationStyle,
) {
    companion object {
        fun fromLegacyMode(@Suppress("UNUSED_PARAMETER") legacyMode: StorytellerAutomationMode): StorytellerRecommendationUxPolicy =
            StorytellerRecommendationUxPolicy(
                automaticExecution = false,
                recommendationStyle = RecommendationStyle.BALANCED,
            )
    }
}
