package com.codex.campboardgamehost.clocktower.domain

/**
 * Presentation / interaction policy for the Storyteller experience mode.
 *
 * Rules legality and recommendation generation remain outside this policy. Both experience modes
 * consume the same recommendation pipeline; the mode only changes how much manual authority is
 * exposed to the host.
 */
data class StorytellerRecommendationUxPolicy(
    val automaticExecution: Boolean,
    val recommendationStyle: RecommendationStyle,
    val showManualAlternatives: Boolean,
    val recommendedOptionLimit: Int,
) {
    init {
        require(recommendedOptionLimit >= 1)
    }

    companion object {
        fun fromExperienceMode(mode: StorytellerExperienceMode): StorytellerRecommendationUxPolicy = when (mode) {
            StorytellerExperienceMode.BEGINNER -> StorytellerRecommendationUxPolicy(
                automaticExecution = true,
                recommendationStyle = RecommendationStyle.AGGRESSIVE,
                showManualAlternatives = false,
                recommendedOptionLimit = 1,
            )

            StorytellerExperienceMode.EXPERIENCED -> StorytellerRecommendationUxPolicy(
                automaticExecution = false,
                recommendationStyle = RecommendationStyle.AGGRESSIVE,
                showManualAlternatives = true,
                recommendedOptionLimit = 3,
            )
        }
    }
}
