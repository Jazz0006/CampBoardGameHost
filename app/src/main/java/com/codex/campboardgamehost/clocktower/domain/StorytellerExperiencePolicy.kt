package com.codex.campboardgamehost.clocktower.domain

data class StorytellerExperiencePolicy(
    val automaticExecution: Boolean,
    val recommendationStyle: RecommendationStyle,
    val showManualAlternatives: Boolean,
    val recommendedOptionLimit: Int,
) {
    init {
        require(recommendedOptionLimit >= 1)
    }

    companion object {
        fun from(mode: StorytellerExperienceMode): StorytellerExperiencePolicy = when (mode) {
            StorytellerExperienceMode.BEGINNER -> StorytellerExperiencePolicy(
                automaticExecution = true,
                recommendationStyle = RecommendationStyle.AGGRESSIVE,
                showManualAlternatives = false,
                recommendedOptionLimit = 1,
            )

            StorytellerExperienceMode.EXPERIENCED -> StorytellerExperiencePolicy(
                automaticExecution = false,
                recommendationStyle = RecommendationStyle.AGGRESSIVE,
                showManualAlternatives = true,
                recommendedOptionLimit = 3,
            )
        }
    }
}
